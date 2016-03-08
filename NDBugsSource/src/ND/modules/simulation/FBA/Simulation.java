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
import ND.modules.simulation.FBA.LP.FBA;
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
    private final List<String> cofactors;

    private final HashMap<String, ReactionFA> reactions;
    private final HashMap<String, SpeciesFA> compounds;
    private Ant antResult;

    public Simulation(SimpleBasicDataset networkDS, List<String> cofactors, List<String> cofactors2, HashMap<String, String[]> bounds, Map<String, Double[]> sources, List<String> sourcesList, String objective) {
        this.networkDS = networkDS;
        this.cofactors = cofactors;
        this.bounds = bounds;
        this.sources = new HashMap<>();

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

            if (r.getListOfProducts().isEmpty()) {
                for (SpeciesReference s : r.getListOfReactants()) {
                    Species sp = s.getSpeciesInstance();
                    SpeciesFA specie = this.compounds.get(sp.getId());
                    if (specie.getAnt() == null) {
                        Ant ant = new Ant(specie.getId());
                        Double[] sb = new Double[2];
                        sb[0] = reaction.getlb();
                        sb[1] = reaction.getub();
                        if (reaction.getlb() < 0.0) {
                            ant.initAnt(Math.abs(reaction.getlb()));
                            specie.addAnt(ant);
                            this.sourcesList.add(sp.getId());
                            this.sources.put(sp.getId(), sb);
                        }
                    }
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

    public void cicle() {
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
                List<Ant> com = new ArrayList<>();
                for (String s : toBeRemoved) {
                    SpeciesFA spfa = this.compounds.get(s);
                    if (spfa.getAnt() != null) {
                        com.add(spfa.getAnt());
                    }

                }
                Ant superAnt = new Ant(null);
                superAnt.joinGraphs(reactionChoosen, direction, com, rc);
                // move the ants to the products...   
                for (String s : toBeAdded) {
                    SpeciesFA spfa = this.compounds.get(s);
                    Ant newAnt = superAnt.clone();
                    if (!hasOutput(newAnt, spfa)) {
                        newAnt.setLocation(compound);
                        double flux = this.getFlux(newAnt, s);
                        System.out.println(s + "-> " + flux);
                        //this.fixPath(newAnt);
                        newAnt.setFlux(flux);
                        spfa.addAnt(newAnt);
                    }

                }

            }
        }
    }

    private boolean hasOutput(Ant ant, SpeciesFA sp) {
        boolean hasOutput = false;
        for (String r : ant.getPath().keySet()) {
            if (reactions.containsKey(r)) {
                ReactionFA reaction = this.reactions.get(r);
                if ((ant.getPath().get(r) && reaction.hasReactant(sp.getId())) || (!ant.getPath().get(r) && reaction.hasProduct(sp.getId()))) {
                    hasOutput = true;
                }
            }

        }
        return hasOutput;
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
                    boolean all = true;
                    for (String reactant : reactants) {

                        if (!allEnoughAnts(reactant, reaction)) {
                            isPossible = false;
                            break;
                        }
                        if (!cofactors.contains(reactant)) {
                            all = false;
                        }

                    }
                    if (all) {
                        isPossible = false;
                    }

                } else {
                    isPossible = false;
                }

            } else {
                if (r.getlb() < 0) {
                    List<String> products = r.getProducts();
                    boolean all = true;
                    for (String product : products) {
                        if (!allEnoughAnts(product, reaction)) {
                            isPossible = false;
                            break;
                        }
                        if (!cofactors.contains(product)) {
                            all = false;
                        }

                    }
                    if (all) {
                        isPossible = false;
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
            //return true;
        } else if (cofactors.contains(species)) {
            return true;
        }
        return false;
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
    public double getFlux(Ant ant, String objective) {
        FBA fba = new FBA();
        ReactionFA objectiveReaction = new ReactionFA("objective");
        objectiveReaction.addReactant(objective, -1.0);
        objectiveReaction.setBounds(0, 1000);
        this.reactions.put("objective", objectiveReaction);
        fba.setModel(ant, "objective", this.reactions, this.cofactors, this.sources, this.compounds, true);
        try {
            Map<String, Double> soln = fba.run();
            if (fba.getMaxObj() > 0) {
                for (String r : soln.keySet()) {
                    if (this.reactions.containsKey(r)) {
                        this.reactions.get(r).setFlux(soln.get(r));
                    }
                }
            }
        } catch (Exception ex) {
            System.out.println(ex);
        }
        //this.reactions.remove("objective");
        return fba.getMaxObj();
    }

//    public double getFlux(Ant ant, String objective, boolean real, boolean verbose) {
//        this.doneReactions.clear();
//        Map<String, Boolean> path = ant.clone().getPath();
//        Map<String, FluxNode> fluxes = new HashMap<>();
//        //this.step(sourcesList, path, fluxes);
//        List<String> species = this.createMiniWorld(path, fluxes, real, verbose);
//        for (int i = 0; i < 50; i++) {
//            this.MiniCicle(species, fluxes, verbose);
//        }
//        if (fluxes.get(objective) == null) {
//            return 0.0;
//        }
//        return fluxes.get(objective).getFlux();
//
//    }
//
//    private List<String> createMiniWorld(Map<String, Boolean> path, Map<String, FluxNode> fluxes, boolean real, boolean verbose) {
//        List<String> species = new ArrayList<>();
//
//        for (String s : path.keySet()) {
//            if (this.sources.containsKey(s)) {
//                FluxNode n = this.initFluxNode(s, path, "initialReaction", Math.abs(this.sources.get(s)[0]));
//                fluxes.put(s, n);
//                species.add(s);
//            }
//
//            if (this.reactions.containsKey(s)) {
//                ReactionFA reaction = this.reactions.get(s);
//                reaction.resetFlux();
//                for (String reactants : reaction.getReactants()) {
//                    if (!species.contains(reactants)) {
//                        FluxNode n = this.initFluxNode(reactants, path, null, 0.0);
//                        fluxes.put(reactants, n);
//                        species.add(reactants);
//                    }
//                    /* FluxNode n = fluxes.get(reactants);
//                     if (path.get(s)) {
//                     n.setOutReactions(reaction);
//                     } else {
//                     n.setInReactions(reaction);
//                     }*/
//                    // if(verbose)n.print();
//                }
//                for (String products : reaction.getProducts()) {
//                    if (!species.contains(products)) {
//                        FluxNode n = this.initFluxNode(products, path, null, 0.0);
//                        fluxes.put(products, n);
//                        species.add(products);
//                    }
//                    /* FluxNode n = fluxes.get(products);
//                     if (!path.get(s)) {
//                     n.setOutReactions(reaction);
//                     } else {
//                     n.setInReactions(reaction);
//                     }**/
//                    // if(verbose)n.print();
//                }
//            }
//        }
//
//        for (String c : this.cofactors) {
//            FluxNode n;
//            n = this.initFluxNode(c, path, "cofactor", -1);
//            if (!real) {
//                if (this.cofactors.contains(c)) {
//                    n.setBalancedFlux();
//                }
//            }
//            fluxes.put(c, n);
//            species.add(c);
//        }
//        return species;
//    }
//
//    private FluxNode initFluxNode(String specie, Map<String, Boolean> path, String initReaction, double flux) {
//
//        FluxNode n;
//        if (initReaction == null) {
//            n = new FluxNode(specie);
//        } else {
//            n = new FluxNode(specie, initReaction, flux);
//        }
//        n.setOutReactions(this.getConnectedOutReactions2(path, specie));
//        n.setInReactions(this.getConnectedInReactions(path, specie));
//
//        return n;
//    }
//
//    private void MiniCicle(List<String> species, Map<String, FluxNode> fluxes, boolean verbose) {
//        for (String specie : species) {
//            fluxes.get(specie).updateFlux(verbose);
//        }
//    }
//
    private Ant combineFluxes(Ant newAnt, Ant ant) {
        int size = 0;
        Ant combinedAnt = new Ant(null);
        if (newAnt != null) {
            combinedAnt.setLocation(newAnt.getLocation());
            Map<String, Boolean> path1 = newAnt.clone().getPath();
            for (String path : path1.keySet()) {
                if (!combinedAnt.contains(path)) {
                    combinedAnt.getPath().put(path, path1.get(path));
                    if (this.reactions.containsKey(path)) {
                        size++;
                    }
                }
            }
        }
        if (ant != null) {
            combinedAnt.setLocation(ant.getLocation());

            Map<String, Boolean> path2 = ant.clone().getPath();
            for (String path : path2.keySet()) {
                if (!combinedAnt.contains(path)) {
                    combinedAnt.getPath().put(path, path2.get(path));
                    if (this.reactions.containsKey(path)) {
                        size++;
                    }
                }
            }
        }
        combinedAnt.setPathSize(size);
        return combinedAnt;
    }
//
//    private List<ReactionFA> getConnectedInReactions(Map<String, Boolean> path, String specie) {
//        List<ReactionFA> possibleReactions = new ArrayList<>();
//        for (String reaction : this.compounds.get(specie).getReactions()) {
//            boolean isPossible = true;
//            if (path.containsKey(reaction)) {
//                ReactionFA reactionFA = this.reactions.get(reaction);
//                if (reactionFA.hasReactant(specie)) {
//                    if (reactionFA.getlb() == 0 || path.get(reaction)) {
//                        isPossible = false;
//                    }
//                }
//                if (reactionFA.hasProduct(specie)) {
//                    if (reactionFA.getub() == 0 || !path.get(reaction)) {
//                        isPossible = false;
//                    }
//
//                }
//                if (isPossible) {
//                    possibleReactions.add(reactionFA);
//                    //in++;
//                }
//            }
//        }
//        return possibleReactions;
//    }
//
//    private List<ReactionFA> getConnectedOutReactions2(Map<String, Boolean> path, String specie) {
//        if (this.compounds.get(specie) == null) {
//            System.out.println(specie);
//        }
//        List<ReactionFA> possibleReactions = new ArrayList<>();
//        for (String reaction : this.compounds.get(specie).getReactions()) {
//            if (path.containsKey(reaction)) {
//                boolean isPossible = true;
//                ReactionFA reactionFA = this.reactions.get(reaction);
//                if (reactionFA.hasReactant(specie)) {
//                    if (reactionFA.getub() == 0 || !path.get(reaction)) {
//                        isPossible = false;
//                    }
//
//                }
//                if (reactionFA.hasProduct(specie)) {
//                    if (reactionFA.getlb() == 0 || path.get(reaction)) {
//                        isPossible = false;
//                    }
//                }
//
//                if (isPossible) {
//                    //out += reactionFA.getStoichiometry(specie);
//                    if (!possibleReactions.contains(reactionFA)) {
//                        possibleReactions.add(reactionFA);
//                    }
//                }
//            }
//        }
//        return possibleReactions;
//    }

    private void fixPath(Ant newAnt) {
        List<String> toBeRemoved = new ArrayList<>();
        for (String p : newAnt.getPath().keySet()) {
            ReactionFA reaction = reactions.get(p);
            if (reaction != null && Math.abs(reaction.getFlux()) < 0.00000001) {
                toBeRemoved.add(p);
            }
        }
        for (String p : toBeRemoved) {
            newAnt.getPath().remove(p);
        }
    }

    public List<String> getCofactors() {
        return this.cofactors;
    }

    public Map<String, Double[]> getSourceMap() {
        return this.sources;
    }
}
