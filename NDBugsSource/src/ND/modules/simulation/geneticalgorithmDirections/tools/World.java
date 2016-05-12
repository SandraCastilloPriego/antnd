/*
 * Copyright 2010 - 2012
 * This file is part of ALVS.
 * ALVS is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * ALVS is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * ALVS; if not, write to the Free Software Foundation, Inc., 51 Franklin St,
 * Fifth Floor, Boston, MA 02110-1301 USA
 */
package ND.modules.simulation.geneticalgorithmDirections.tools;

import ND.modules.simulation.geneticalgorithm.tools.*;
import ND.data.Dataset;
import ND.modules.simulation.antNoGraph.ReactionFA;
import java.util.*;
import javax.swing.JTextArea;
import org.sbml.jsbml.KineticLaw;
import org.sbml.jsbml.LocalParameter;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.SpeciesReference;

/**
 *
 * @author bicha
 */
public final class World {

    Dataset trainingDataset;
    List<Bug> population;
    Random rand;
    int bugLife;
    JTextArea text;
    int bugsLimitNumber;
    String objective;
    private HashMap<String, ReactionFA> reactions;

    public World(Dataset training, List<String> reactionIds, int bugLife, JTextArea text,
        int bugsLimitNumber, String objective) {
        this.trainingDataset = training;
        this.population = new ArrayList<>();
        this.rand = new Random();
        this.bugLife = bugLife;
        this.text = text;
        this.bugsLimitNumber = bugsLimitNumber;
        this.objective = objective;
        this.createReactions();
        
       /* this.setBiomassObjective();
        double referenceBiomass = this.getReference(true);
        
        this.setObjectiveObjective();
        double referenceObjective = this.getReference(true);
        this.reactions.remove("objective");*/

        if (training != null) {
            System.out.println("Adding bugs");
            int i =0;
            for (ReactionFA reaction : this.reactions.values()) {
                if (isNotExchange(reaction)&& reactionIds.contains(reaction.getId())) {
                    System.out.println("Bug " + i++ + ": " +reaction.getId());
                    this.addBug(reaction, 0.5, 2.5);
                }
            }

        }
    }
    
    
    private void createReactions() {
        System.out.println("Creating reactions");
        SBMLDocument doc = this.trainingDataset.getDocument();
        this.reactions = new HashMap<>();
        Model m = doc.getModel();

        for (Reaction r : m.getListOfReactions()) {
            ReactionFA reaction = new ReactionFA(r.getId());

            try {
                KineticLaw law = r.getKineticLaw();
                LocalParameter lbound = law.getLocalParameter("LOWER_BOUND");
                LocalParameter ubound = law.getLocalParameter("UPPER_BOUND");
                LocalParameter objective = law.getLocalParameter("OBJECTIVE_COEFFICIENT");
                reaction.setObjective(objective.getValue());
                reaction.setBounds(lbound.getValue(), ubound.getValue());
            } catch (Exception ex) {
                reaction.setBounds(-1000, 1000);
            }

            for (SpeciesReference s : r.getListOfReactants()) {

                Species sp = s.getSpeciesInstance();
                reaction.addReactant(sp.getId(), sp.getName(), s.getStoichiometry());
            }

            for (SpeciesReference s : r.getListOfProducts()) {
                Species sp = s.getSpeciesInstance();
                reaction.addProduct(sp.getId(), sp.getName(), s.getStoichiometry());
            }
            //reaction.setObjective(0.0);            
            this.reactions.put(r.getId(), reaction);            
        }
        
        
    }
    
    

    public List<Bug> getBugs() {
        return this.population;
    }

    private void addBug(ReactionFA row, double referenceBiomass, double referenceObjective) {
        Bug bug = new Bug(row, trainingDataset, bugLife, objective, this.reactions, referenceBiomass, referenceObjective);
        this.population.add(bug);
    }

    public synchronized void cicle() {
        death();
        Comparator<Bug> c = new Comparator<Bug>() {
            public int compare(Bug o1, Bug o2) {
                return Double.compare(o1.getScore(), o2.getScore());
            }
        };

        Collections.sort(this.population, c);
        if (population.size() > this.bugsLimitNumber) {
            this.purgeBugs();
        }
        Collections.reverse(this.population);

        reproduce(this.population);

    }

    public void purgeBugs() {
        try {
            if (this.population.size() > this.bugsLimitNumber) {
                for (int i = 0; i < this.bugsLimitNumber - this.population.size(); i++) {
                    Bug b = population.get(i);
                    if (b.getRows().size() > 1) {
                        b.kill();
                    }
                }
            }
        } catch (Exception e) {
        }
    }

    private synchronized void death() {
        List<Bug> deadBugs = new ArrayList<Bug>();
        for (Bug bug : population) {
            try {
                if (bug.isDead()) {
                    deadBugs.add(bug);
                }
            } catch (Exception e) {
            }
        }
        for (Bug bug : deadBugs) {
            this.population.remove(bug);
        }
    }

    public void reproduce(List<Bug> bugsInside) {
        List<Reproduction> allThreads = new ArrayList<>();
        try {
            for (int j = 0; j < 50; j++) {
                Reproduction thread = new Reproduction(bugsInside, this.population, rand, this.trainingDataset, this.bugLife);
                allThreads.add(thread);
                thread.run();
            }

            while (!allThreads.isEmpty()) {
                Iterator<Reproduction> ite = allThreads.iterator();
                while (ite.hasNext()) {
                    Reproduction thread = ite.next();
                    if (!thread.isAlive()) {
                        ite.remove();
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Something failed during reproduction");
            e.printStackTrace();
        }
        //   Bug mother = bugsInside.get(this.rand.nextInt(bugsInside.size()));
        // Bug father = bugsInside.get(this.rand.nextInt(bugsInside.size()));
        //System.out.println("Reproducing: Mother= " + mother.toString()+ " Father= "+ father.toString());
        //  Thread reproduce = new Thread(new Runnable() {
        //    public void run() {
        //      if (!mother.isSameBug(father)) {
        //        population.add(new Bug(father, mother, trainingDataset, bugLife, maxVariables, range));
        //  }
        //  }
        //});
        //reproduce.start();
    }

    private boolean isNotExchange(ReactionFA reaction) {
        Model model = this.trainingDataset.getDocument().getModel();
        for (String product : reaction.getProducts()) {
            String sp = model.getSpecies(product).getName();
            if (sp.contains("boundary")) {
                return false;
            }
        }
        return true;
    }

}
