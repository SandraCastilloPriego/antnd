/*
 * Copyright 2007-2013 VTT Biotechnology
 * This file is part of AntND.
 *
 * AntND is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * AntND is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * AntND; if not, write to the Free Software Foundation, Inc., 51 Franklin St,
 * Fifth Floor, Boston, MA 02110-1301 USA
 */
package ND.modules.simulation.FBA;

import ND.data.impl.datasets.SimpleBasicDataset;
import ND.modules.simulation.antNoGraph.ReactionFA;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.sbml.jsbml.KineticLaw;
import org.sbml.jsbml.LocalParameter;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.SpeciesReference;

/**
 *
 * @author Sandra Castillo <Sandra.castillo@vtt.fi>
 */
public class Simulation {

    private final HashMap<String, String[]> bounds;
    private final Map<String, Double[]> sources;
    private final SimpleBasicDataset networkDS;
    private final List<String> sourcesList;
    private final List<String> objectives;
    private final List<String> cofactors;

    private HashMap<String, ReactionFA> reactions;
    private final HashMap<String, SpeciesFA> compounds;
    private Ant antResult;

    public Simulation(SimpleBasicDataset networkDS, List<String> cofactors, HashMap<String, String[]> bounds, Map<String, Double[]> sources, List<String> sourcesList) {
        this.networkDS = networkDS;
        this.cofactors = cofactors;
        this.objectives = new ArrayList();
        this.bounds = bounds;
        this.sources = sources;
        this.sourcesList = sourcesList;

        this.reactions = new HashMap<>();
        this.compounds = new HashMap<>();
    }

    public void createWorld() {
        SBMLDocument doc = this.networkDS.getDocument();
        Model m = doc.getModel();
        for (Species s : m.getListOfSpecies()) {
            SpeciesFA specie = new SpeciesFA(s.getId(), s.getName());
            this.compounds.put(s.getId(), specie);

            if (!this.sources.isEmpty() && this.sources.containsKey(s.getId())) {
                Ant ant = new Ant(specie.getId());
                ant.initAnt(Math.abs(this.sources.get(s.getId())[0]));
                specie.addAnt(ant);
                this.sourcesList.add(s.getId());
            }
        }

        for (Reaction r : m.getListOfReactions()) {
            boolean biomass = false;

            ReactionFA reaction = new ReactionFA(r.getId());
            String[] b = this.bounds.get(r.getId());
            if (b != null) {
                reaction.setBounds(Double.valueOf(b[3]), Double.valueOf(b[4]));
            } else {
                try {
                    KineticLaw law = r.getKineticLaw();
                    LocalParameter lbound = law.getLocalParameter("LOWER_BOUND");
                    LocalParameter ubound = law.getLocalParameter("UPPER_BOUND");
                    reaction.setBounds(lbound.getValue(), ubound.getValue());
                } catch (Exception ex) {
                    reaction.setBounds(-1000, 1000);
                }
            }
            for (SpeciesReference s : r.getListOfReactants()) {

                Species sp = s.getSpeciesInstance();

                reaction.addReactant(sp.getId(), sp.getName(), s.getStoichiometry());
                SpeciesFA spFA = this.compounds.get(sp.getId());
                if (biomass) {
                    spFA.setPool(Math.abs(s.getStoichiometry()));
                }
                if (spFA != null) {
                    spFA.addReaction(r.getId());
                } else {
                    System.out.println(sp.getId());
                }
            }

            for (SpeciesReference s : r.getListOfProducts()) {
                Species sp = s.getSpeciesInstance();

                if (sp.getName().contains("boundary") && reaction.getlb() < 0) {
                    SpeciesFA specie = this.compounds.get(sp.getId());
                    if (specie.getAnt() == null) {
                        Ant ant = new Ant(specie.getId());
                        Double[] sb = new Double[2];
                        sb[0] = reaction.getlb();
                        sb[1] = reaction.getub();
                        ant.initAnt(Math.abs(reaction.getlb()));
                        specie.addAnt(ant);
                        this.sourcesList.add(sp.getId());
                        this.sources.put(sp.getId(), sb);
                    }
                }

                reaction.addProduct(sp.getId(), sp.getName(), s.getStoichiometry());
                SpeciesFA spFA = this.compounds.get(sp.getId());
                if (spFA != null) {
                    spFA.addReaction(r.getId());
                } else {
                    System.out.println(sp.getId());
                }
            }
            this.reactions.put(r.getId(), reaction);
        }
        List<String> toBeRemoved = new ArrayList<>();
        for (String compound : compounds.keySet()) {
            if (compounds.get(compound).getReactions().isEmpty()) {
                toBeRemoved.add(compound);
            }
        }
        for (String compound : toBeRemoved) {
            this.compounds.remove(compound);
        }

    }

    /* public void createWorld(List<String> path, String compound) {
     SBMLDocument doc = this.networkDS.getDocument();
     Model m = doc.getModel();
     for (Species s : m.getListOfSpecies()) {
     SpeciesFA specie = new SpeciesFA(s.getId(), s.getName());
     this.compounds.put(s.getId(), specie);

     if (compound.equals(s.getId())) {
     Ant ant = new Ant(specie.getId());
     ant.initAnt(0.0);
     specie.addAnt(ant);
     this.sourcesList.add(s.getId());
     }
     }
     for (Reaction r : m.getListOfReactions()) {
     if (!path.contains(r.getId())) {
     continue;
     }
     boolean biomass = false;

     ReactionFA reaction = new ReactionFA(r.getId());
     String[] b = this.bounds.get(r.getId());
     if (b != null) {
     reaction.setBounds(Double.valueOf(b[3]), Double.valueOf(b[4]));
     } else {
     try {
     KineticLaw law = r.getKineticLaw();
     LocalParameter lbound = law.getLocalParameter("LOWER_BOUND");
     LocalParameter ubound = law.getLocalParameter("UPPER_BOUND");
     reaction.setBounds(lbound.getValue(), ubound.getValue());
     } catch (Exception ex) {
     reaction.setBounds(-1000, 1000);
     }
     }
     for (SpeciesReference s : r.getListOfReactants()) {

     Species sp = s.getSpeciesInstance();
     if (sp.getName().contains("boundary") && reaction.getlb() < 0) {
     SpeciesFA specie = this.compounds.get(sp.getId());
     if (specie.getAnt() == null) {
     Ant ant = new Ant(specie.getId() + " : " + specie.getName());
     Double[] sb = new Double[2];
     sb[0] = reaction.getlb();
     sb[1] = reaction.getub();
     ant.initAnt(Math.abs(reaction.getlb()));
     specie.addAnt(ant);
     this.sourcesList.add(s.getId());

     this.sources.put(s.getId(), sb);
     }
     }
     reaction.addReactant(sp.getId(), sp.getName(), s.getStoichiometry());
     SpeciesFA spFA = this.compounds.get(sp.getId());
     if (biomass) {
     spFA.setPool(Math.abs(s.getStoichiometry()));
     }
     if (spFA != null) {
     spFA.addReaction(r.getId());
     } else {
     System.out.println(sp.getId());
     }
     }

     for (SpeciesReference s : r.getListOfProducts()) {
     Species sp = s.getSpeciesInstance();
     reaction.addProduct(sp.getId(), sp.getName(), s.getStoichiometry());
     SpeciesFA spFA = this.compounds.get(sp.getId());
     if (spFA != null) {
     spFA.addReaction(r.getId());
     } else {
     System.out.println(sp.getId());
     }
     }
     this.reactions.put(r.getId(), reaction);
     }
     }*/
    public void cicle(/*String objectiveID*/) {
        for (String compound : compounds.keySet()) {
            if (this.compounds.get(compound).getAnt() == null) {
                continue;
            }
            List<String> possibleReactions = getPossibleReactions(compound);

            for (String reactionChoosen : possibleReactions) {

                ReactionFA rc = this.reactions.get(reactionChoosen);
                Boolean direction = true;
                List<String> toBeAdded, toBeRemoved;
                if (rc.hasReactant(compound)) {
                    toBeAdded = rc.getProducts();
                    toBeRemoved = rc.getReactants();
                } else {
                    toBeAdded = rc.getReactants();
                    toBeRemoved = rc.getProducts();
                    direction = false;

                }

                // get the ants that must be removed from the reactants ..
                // creates a superAnt with all the paths until this reaction joined..
                //  List<List<Ant>> paths = new ArrayList<>();
                List<Ant> com = new ArrayList<>();
                for (String s : toBeRemoved) {
                    SpeciesFA spfa = this.compounds.get(s);
                    if (spfa.getAnt() != null && !this.cofactors.contains(s)) {
                        com.add(spfa.getAnt());
                    }

                    /*List<Ant> a = spfa.getAnts();
                     // System.out.println("id: " + s + "paths: " + a.size());
                     if (a.size() > 0) {
                     paths.add(a);
                     }*/
                }

                //  List<List<Ant>> combinations = Permutation.permutations(paths);
                //   List<Ant> superAnts = new ArrayList<>();
                // (List<Ant> com : combinations) {
                Ant superAnt = new Ant(null);
                //        double flux = getFlux(com);
                superAnt.joinGraphs(reactionChoosen, direction, com, rc);
              //      superAnts.add(superAnt);
                //  }

                // move the ants to the products...   
                for (String s : toBeAdded) {
                    // for (Ant superAnt : superAnts) {
                    // if (!superAnt.isLost() /*&& !this.cofactors.contains(s)*/) {

                    SpeciesFA spfa = this.compounds.get(s);
                    Ant newAnt = superAnt.clone();
                    newAnt.setLocation(compound);
                    double flux = this.getFlux(newAnt, s, false);
                    newAnt.setFlux(flux);
                    // Ant combinedAnt = this.combineFluxes(newAnt, spfa.getAnt());
                    //double combinedFlux = this.getFlux(combinedAnt, s);
                    //combinedAnt.setFlux(combinedFlux);
                    /*if (flux > combinedFlux) {
                     spfa.addAnt(newAnt);
                     } else {
                     spfa.addAnt(combinedAnt);
                     }*/
                    spfa.addAnt(newAnt);
                    /* if ((newAnt.contains("r_1054"))) {
                     if (spfa.getAnt() == null) {
                     spfa.addAnt(newAnt);
                     } else if (spfa.getAnt().contains("r_1054")) {
                     if (spfa.getAnt().getPathSize() > newAnt.getPathSize()) {
                     spfa.addAnt(newAnt);
                     }
                     } else {
                     spfa.addAnt(newAnt);
                     }
                     }
                     if (spfa.getAnt() == null || newAnt.getPathSize() < spfa.getAnt().getPathSize() && !spfa.getAnt().contains("r_1054")) {
                     spfa.addAnt(newAnt);
                     }*/
                    //  }
                    //}
                }

            }
        }
    }

    private List<String> getPossibleReactions(String compound) {

        List<String> possibleReactions = new ArrayList<>();
        SpeciesFA sp = this.compounds.get(compound);
        Ant ant = sp.getAnt();
        if (!this.sources.containsKey(compound) && ant == null) {
            return possibleReactions;
        }

        List<String> connectedReactions = sp.getReactions();
        for (String reaction : connectedReactions) {

            ReactionFA r = this.reactions.get(reaction);
            if (r == null) {
                continue;
            }
            boolean isPossible = true;

            if (r.getlb() == 0 && r.getub() == 0) {
                isPossible = false;
            }

            if (r.hasReactant(compound)) {

                if (r.getub() > 0) {
                    List<String> reactants = r.getReactants();
                    for (String reactant : reactants) {

                        if (!allEnoughAnts(reactant, reaction)) {
                            isPossible = false;
                            break;
                        }

                    }

                } else {
                    isPossible = false;
                }

            } else {
                if (r.getlb() < 0) {
                    List<String> products = r.getProducts();
                    for (String product : products) {
                        if (!allEnoughAnts(product, reaction)) {
                            isPossible = false;
                            break;
                        }

                    }
                } else {
                    isPossible = false;
                }

            }

            if (isPossible) {
                possibleReactions.add(reaction);
            }

        }
        return possibleReactions;
    }

    private boolean allEnoughAnts(String species, String reaction) {
        SpeciesFA s = this.compounds.get(species);
        Ant ant = s.getAnt();
        if (ant != null) {
            return !ant.contains(reaction);
        } else if (cofactors.contains(species)) {
            //this.objectives.add(species);
            return true;
        }
        return false;
    }

    public List<String> getNewObjectives() {
        return this.objectives;
    }

    public Ant getResult() {
        return this.antResult;
    }

    public Map<String, SpeciesFA> getCompounds() {
        return this.compounds;
    }

    public Map<String, ReactionFA> getReactions() {
        return this.reactions;
    }

    /* public double getFlux(){
     Map<String, Double> fluxes = new HashMap<>();
     String source = 
     for(String p : path){
     if(this.reactions.containsKey(p)){
     for(String reactants : )
     }
     }
        
     }*/
    public double getFlux(Ant ant, String objective, boolean real) {
        Map<String, Boolean> path = ant.clone().getPath();
        Map<String, FluxNode> fluxes = new HashMap<>();
        //this.step(sourcesList, path, fluxes);
        List<String> species = this.createMiniWorld(path, fluxes, real);
        for (int i = 0; i < 10; i++) {
            this.MiniCicle(path, species, fluxes);
        }
        if (fluxes.get(objective) == null) {
            return 0.0;
        }
        return fluxes.get(objective).getFlux();

    }

    /*private void step(List<String> sources, List<String> path, Map<String, FluxNode> fluxes) {
     List<String> newSources = new ArrayList<>();
     for (String source : sources) {
     List<String> rxns = this.compounds.get(source).getReactions();

     for (String r : rxns) {
     if (!path.contains(r)) {
     continue;
     }
     path.remove(r);
     ReactionFA reaction = this.reactions.get(r);
     if (reaction.hasReactant(source)) {
     if (!fluxes.containsKey(source)) {
     FluxNode n = new FluxNode(source, Math.abs(reaction.getub()) / reaction.getStoichiometry(source));
     fluxes.put(source, n);
     }
     Double Flux = reaction.getub();
     for (String reactants : reaction.getReactants()) {
     if (fluxes.containsKey(reactants)) {
     double flux = fluxes.get(reactants).getFlux();
     if (Flux > flux) {
     Flux = flux;
     }
     }
     }

     for (String products : reaction.getProducts()) {
     if (!fluxes.containsKey(products)) {
     FluxNode n = new FluxNode(source, Flux * reaction.getStoichiometry(products));
     fluxes.put(source, n);
     } else {
     fluxes.get(source).setFlux(Flux * reaction.getStoichiometry(products));
     }
     newSources.add(products);
     }
     }
     if (reaction.hasProduct(source)) {
     if (!fluxes.containsKey(source)) {
     FluxNode n = new FluxNode(source, Math.abs(reaction.getlb()) / reaction.getStoichiometry(source));
     fluxes.put(source, n);
     }

     Double Flux = reaction.getlb();
     for (String products : reaction.getProducts()) {
     if (fluxes.containsKey(products)) {
     double flux = fluxes.get(products).getFlux();
     if (Flux > flux) {
     Flux = flux;
     }
     }
     }
     for (String reactants : reaction.getReactants()) {
     if (!fluxes.containsKey(reactants)) {
     FluxNode n = new FluxNode(source, Flux * reaction.getStoichiometry(reactants));
     fluxes.put(source, n);
     } else {
     fluxes.get(source).setFlux(Flux * reaction.getStoichiometry(reactants));
     }
     newSources.add(reactants);
     }
     }

     }
     }
     if (newSources.size() > 0) {
     this.step(newSources, path, fluxes);
     }
     }
     */
    private List<String> createMiniWorld(Map<String, Boolean> path, Map<String, FluxNode> fluxes, boolean real) {
        List<String> species = new ArrayList<>();

        for (String s : path.keySet()) {
            if (this.sources.containsKey(s)) {
                List<String> possibleReactions = getConnectedReactions(path, s);
                double outReactions = 0;
                for (String r : possibleReactions) {
                    ReactionFA reaction = this.reactions.get(r);
                    outReactions += reaction.getStoichiometry(s);
                }

                FluxNode n = new FluxNode(s, "initialReaction", Math.abs(this.sources.get(s)[0]));
                n.setOutReactions(outReactions);
                fluxes.put(s, n);
                species.add(s);
            }

            if (this.reactions.containsKey(s)) {
                ReactionFA reaction = this.reactions.get(s);
                for (String reactants : reaction.getReactants()) {
                    if (!species.contains(reactants)) {
                        species.add(reactants);
                    }
                }
                for (String products : reaction.getProducts()) {
                    if (!species.contains(products)) {
                        species.add(products);
                    }
                }
            }
        }
        for (String c : this.cofactors) {
            List<String> possibleReactions = getConnectedReactions(path, c);
            double outReactions = 0;
            for (String r : possibleReactions) {
                ReactionFA reaction = this.reactions.get(r);
                outReactions += reaction.getStoichiometry(c);
            }
            FluxNode n;
            if (!real) {
                n = new FluxNode(c, "initialReaction", getBalacedFlux(c, path));
            } else {
                n = new FluxNode(c, "initialReaction", Double.MAX_VALUE);
            }
            n.setOutReactions(outReactions);
            fluxes.put(c, n);
        }
        /* for (String specie : species) {
         List<String> possibleReactions = getConnectedReactions(path, fluxes, specie);
         System.out.println(specie + " : " + possibleReactions.toString());
         if (fluxes.containsKey(specie)) {

         }
         }*/
        return species;
    }

    private void MiniCicle(Map<String, Boolean> path, List<String> species, Map<String, FluxNode> fluxes) {
        for (String specie : species) {
            if (fluxes.containsKey(specie)) {
                List<String> possibleReactions = getPossibleReactions(path, fluxes, specie);
                for (String reaction : possibleReactions) {
                    updateFlux(reaction, path, fluxes, specie);
                }
            }
        }
    }

    private void updateFlux(String reaction, Map<String, Boolean> path, Map<String, FluxNode> fluxes, String specie) {
        ReactionFA reactionFA = this.reactions.get(reaction);
        if (path.get(reaction)) {
            // System.out.println("In reactants " + specie + " : " + reactionFA.getub());
            double Flux = reactionFA.getub();
            for (String reactant : reactionFA.getReactants()) {
                //if (!this.cofactors.contains(reactant)) {
                FluxNode fluxNode = fluxes.get(reactant);
                double flux = fluxNode.getFlux() / reactionFA.getStoichiometry(reactant);
                if (Flux > flux) {
                    Flux = flux;
                }
                 //   System.out.println(reactant + " : " + flux + "-> " + Flux);

                // }
            }
            for (String product : reactionFA.getProducts()) {
                FluxNode fluxNode;
                if (fluxes.containsKey(product)) {
                    fluxNode = fluxes.get(product);
                    fluxNode.setFlux(reactionFA.getId(), Flux * reactionFA.getStoichiometry(product));
                } else {
                    List<String> possibleReactions = getConnectedReactions(path, product);
                    double outReactions = 0;
                    for (String r : possibleReactions) {
                        ReactionFA reactio = this.reactions.get(r);
                        outReactions += reactio.getStoichiometry(product);
                    }
                    fluxNode = new FluxNode(product, reactionFA.getId(), Flux * reactionFA.getStoichiometry(product));
                    if (outReactions > 0) {
                        fluxNode.setOutReactions(outReactions);
                    }
                }
                fluxes.put(product, fluxNode);
                reactionFA.setFlux(Flux);
            }
        } else {
            double Flux = Math.abs(reactionFA.getlb());
            //  System.out.println("in Products " + specie + " : " + reactionFA.getlb());
            for (String product : reactionFA.getProducts()) {
                // if (!this.cofactors.contains(product)) {
                FluxNode fluxNode = fluxes.get(product);
                double flux = fluxNode.getFlux() / reactionFA.getStoichiometry(product);
                if (Flux > flux) {
                    Flux = flux;
                }
                //         System.out.println(product + " : " + flux + "-> " + Flux);
                // }
            }
            for (String reactant : reactionFA.getReactants()) {
                FluxNode fluxNode;
                if (fluxes.containsKey(reactant)) {
                    fluxNode = fluxes.get(reactant);
                    fluxNode.setFlux(reactionFA.getId(), Flux * reactionFA.getStoichiometry(reactant));

                } else {
                    List<String> possibleReactions = getConnectedReactions(path, reactant);
                    double outReactions = 0;
                    for (String r : possibleReactions) {
                        ReactionFA reactio = this.reactions.get(r);
                        outReactions += reactio.getStoichiometry(reactant);
                    }
                    fluxNode = new FluxNode(reactant, reactionFA.getId(), Flux * reactionFA.getStoichiometry(reactant));
                    if (outReactions > 0) {
                        fluxNode.setOutReactions(outReactions);
                    }
                }
                fluxNode.addReaction(reaction);
                fluxes.put(reactant, fluxNode);
                reactionFA.setFlux(Flux);
            }
        }
    }

    private List<String> getPossibleReactions(Map<String, Boolean> path, Map<String, FluxNode> fluxes, String specie) {
        List<String> possibleReactions = new ArrayList<>();

        for (String reaction : this.compounds.get(specie).getReactions()) {
            boolean isPossible = true;
            if (path.containsKey(reaction)) {
                ReactionFA reactionFA = this.reactions.get(reaction);
                if (reactionFA.hasReactant(specie)) {
                    if (reactionFA.getub() == 0 || !path.get(reaction)) {
                        isPossible = false;
                    }
                    for (String reactant : reactionFA.getReactants()) {
                        if (!fluxes.containsKey(reactant)) {
                            isPossible = false;
                        }
                    }
                }
                if (reactionFA.hasProduct(specie)) {
                    if (reactionFA.getlb() == 0 || path.get(reaction)) {
                        isPossible = false;
                    }
                    for (String product : reactionFA.getProducts()) {
                        if (!fluxes.containsKey(product)) {
                            isPossible = false;
                        }
                    }
                }
                if (isPossible) {
                    possibleReactions.add(reaction);
                }
            }
        }
        return possibleReactions;
    }

    private Ant combineFluxes(Ant newAnt, Ant ant) {
        Ant combinedAnt = new Ant(null);
        if (newAnt != null) {
            combinedAnt.setLocation(newAnt.getLocation());
            Map<String, Boolean> path1 = newAnt.clone().getPath();
            for (String path : path1.keySet()) {
                if (!combinedAnt.contains(path)) {
                    combinedAnt.getPath().put(path, path1.get(path));
                }
            }
        }
        if (ant != null) {
            combinedAnt.setLocation(ant.getLocation());

            Map<String, Boolean> path2 = ant.clone().getPath();
            for (String path : path2.keySet()) {
                if (!combinedAnt.contains(path)) {
                    combinedAnt.getPath().put(path, path2.get(path));
                }
            }
        }
        return combinedAnt;
    }

    private Double getBalacedFlux(String c, Map<String, Boolean> path) {
        int produced = this.getConnectedInReactions(path, c);
        int consumed = this.getConnectedOutReactions(path, c);
        System.out.println(c + " : " + produced + "/" + consumed);
        if (produced >= consumed) {
            return Double.POSITIVE_INFINITY;
        }
        return 0.10;
    }

    private List<String> getConnectedReactions(Map<String, Boolean> path, String specie) {
        List<String> possibleReactions = new ArrayList<>();

        for (String reaction : this.compounds.get(specie).getReactions()) {
            boolean isPossible = true;
            if (path.containsKey(reaction)) {
                ReactionFA reactionFA = this.reactions.get(reaction);
                if (reactionFA.hasReactant(specie)) {
                    if (reactionFA.getub() == 0 || !path.get(reaction)) {
                        isPossible = false;
                    }

                }
                if (reactionFA.hasProduct(specie)) {
                    if (reactionFA.getlb() == 0 || path.get(reaction)) {
                        isPossible = false;
                    }

                }
                if (isPossible) {
                    possibleReactions.add(reaction);
                }
            }
        }
        return possibleReactions;
    }

    private int getConnectedInReactions(Map<String, Boolean> path, String specie) {
        int in = 0;
        for (String reaction : this.compounds.get(specie).getReactions()) {
            boolean isPossible = true;
            if (path.containsKey(reaction)) {
                ReactionFA reactionFA = this.reactions.get(reaction);
                if (reactionFA.hasReactant(specie)) {
                    isPossible = false;
                }
                if (reactionFA.hasProduct(specie)) {
                    if (reactionFA.getub() == 0 || !path.get(reaction)) {
                        isPossible = false;
                    }

                }
                if (isPossible) {
                    in += reactionFA.getStoichiometry(specie);
                }
            }
        }
        return in;
    }

    private int getConnectedOutReactions(Map<String, Boolean> path, String specie) {
        int out = 0;

        for (String reaction : this.compounds.get(specie).getReactions()) {
            boolean isPossible = true;
            if (path.containsKey(reaction)) {
                ReactionFA reactionFA = this.reactions.get(reaction);
                if (reactionFA.hasReactant(specie)) {
                    if (reactionFA.getub() == 0 || !path.get(reaction)) {
                        isPossible = false;
                    }

                }
                if (reactionFA.hasProduct(specie)) {
                    isPossible = false;
                }

                if (isPossible) {
                    out += reactionFA.getStoichiometry(specie);
                }
            }
        }
        return out;
    }

}
