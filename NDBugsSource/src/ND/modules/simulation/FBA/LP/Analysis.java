package ND.modules.simulation.FBA.LP;

import ND.modules.simulation.FBA.Ant;
import ND.modules.simulation.antNoGraph.ReactionFA;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.SpeciesReference;

public abstract class Analysis {

    protected double maxObj = Double.NaN;
    private Ant ant;
    private String objective;
    List< Double> objectiveList;
    private Map<String, Integer> reactionPositionMap;
    private Map<String, Integer> metabolitePositionMap;
    List<ReactionFA> reactionsList;
    List<String> metabolitesList;
    ModelCompressor compressor;

    protected void setVars() {
        Map<String, Boolean> path = this.ant.getPath();
        for (ReactionFA r : reactionsList) {
            String varName = r.getId();
            if (path.containsKey(varName)) {
                if (path.get(varName)) {
                    this.getSolver().setVar(varName, VarType.CONTINUOUS, 0, 1000);
                } else {
                    this.getSolver().setVar(varName, VarType.CONTINUOUS, -1000, 0);
                }
            } else {
                this.getSolver().setVar(varName, VarType.CONTINUOUS, r.getlb(), r.getub());
            }
        }
    }

    protected void setConstraints() {
        setConstraints(ConType.EQUAL, 0.0);
    }

    protected void setConstraints(ConType conType, double bValue) {

        ArrayList< Map< Integer, Double>> sMatrix = this.getSMatrix();
        for (int i = 0; i < sMatrix.size(); i++) {
            this.getSolver().addConstraint(sMatrix.get(i), conType, bValue);
        }
    }

    protected ArrayList< Map< Integer, Double>> getSMatrix() {
        this.metabolitePositionMap = new HashMap<>();
        ArrayList< Map< Integer, Double>> sMatrix = new ArrayList< Map< Integer, Double>>(
            this.metabolitesList.size());
        for (int i = 0; i < this.metabolitesList.size(); i++) {
            this.metabolitePositionMap.put(this.metabolitesList.get(i), Integer.valueOf(i));
            Map< Integer, Double> sRow = new HashMap<>();
            sMatrix.add(sRow);
        }

        for (ReactionFA reaction : this.reactionsList) {
            for (String reactant : reaction.getReactants()) {
                if (this.metabolitesList.contains(reactant)) {
                    double sto = reaction.getStoichiometry(reactant);
                    sto = Math.abs(sto) * -1;
                    sMatrix.get(this.metabolitePositionMap.get(reactant)).put(this.reactionPositionMap.get(reaction.getId()), sto);
                }
            }

            for (String product : reaction.getProducts()) {
                if (this.metabolitesList.contains(product)) {
                    double sto = reaction.getStoichiometry(product);
                    sto = Math.abs(sto);
                    sMatrix.get(this.metabolitePositionMap.get(product)).put(this.reactionPositionMap.get(reaction.getId()), sto);
                }
            }
        }

        return sMatrix;
    }

    protected void setObjective() {
        this.getSolver().setObjType(ObjType.Maximize);
        Map< Integer, Double> map = new HashMap< Integer, Double>();
        for (int i = 0; i < objectiveList.size(); i++) {
            if (objectiveList.get(i) != 0.0) {
                map.put(i, objectiveList.get(i));
            }
        }
        this.getSolver().setObj(map);
    }

    public void setModel(Ant m, String Objective, HashMap<String, ReactionFA> reactions, List<String> cofactors, Map<String, Double[]> sources) {
        this.ant = m;
        this.objective = Objective;
        this.prepareReactions(cofactors, reactions, sources);
    }

    public void setSolverParameters() {
        this.setVars();
        this.setConstraints();
        this.setObjective();
    }

    public Map<String, Double> run() throws Exception {
        this.setSolverParameters();
        this.maxObj = this.getSolver().optimize();
        ArrayList<Double> fluxes = this.getSolver().getSoln();
        int i = 0;
        Map<String, Double> fluxesMap = new HashMap<>();
        for (ReactionFA reaction : this.reactionsList) {
            fluxesMap.put(reaction.getId(), fluxes.get(i));
            reaction.setFlux(fluxes.get(i));
            System.out.print(reaction.getId() + " -> " + fluxes.get(i) + " ,");
            i++;
        }
        //System.out.println("\n");
        return fluxesMap;
    }

    public abstract Solver getSolver();

    public double getMaxObj() {
        return this.maxObj;
    }

    private void prepareReactions(List<String> cofactors, HashMap<String, ReactionFA> reactions, Map<String, Double[]> sources) {
        this.reactionsList = new ArrayList<>();
        this.metabolitesList = new ArrayList<>();
        this.reactionPositionMap = new HashMap<>();
        this.objectiveList = new ArrayList<>();

        for (String reaction : this.ant.getPath().keySet()) {
            // System.out.print(reaction + " - ");
            if (reactions.containsKey(reaction)) {
                ReactionFA r = reactions.get(reaction);
                this.reactionsList.add(r);

                for (String reactant : r.getReactants()) {
                    if (!metabolitesList.contains(reactant) /*&& !cofactors.contains(reactant)*/) {
                        this.metabolitesList.add(reactant);
                    }
                }
                for (String product : r.getProducts()) {
                    if (!metabolitesList.contains(product)/*&& !cofactors.contains(product)*/) {
                        this.metabolitesList.add(product);
                    }
                }
            }

        }
        //  System.out.print("\n");

        for (String source : sources.keySet()) {
            if (metabolitesList.contains(source)) {
                ReactionFA Source = new ReactionFA("ExS" + source);
                Source.addReactant(source, -1.0);
                Source.setBounds(sources.get(source)[0], sources.get(source)[1]);
                this.reactionsList.add(Source);
            }
        }
        
        List<String> deadEnd = this.getDeadEnds();
        //  this.fixCofactorIssue(deadEnd, cofactors);
        for (String metabolite : deadEnd) {
            if (!cofactors.contains(metabolite) && !sources.containsKey(metabolite) && !cofactors.contains(metabolite)) {
                /*ReactionFA exchange = new ReactionFA("Ex_" + metabolite);
                exchange.addReactant(metabolite, -1.0);
                exchange.setBounds(0, 1000);
                this.reactionsList.add(exchange);*/
            } else if (cofactors.contains(metabolite) && this.metabolitesList.contains(metabolite)) {
                ReactionFA newCofactor = new ReactionFA("ExC_" + metabolite);
                newCofactor.addReactant(metabolite, -1.0);
                newCofactor.setBounds(-0.1, Double.POSITIVE_INFINITY);
                this.reactionsList.add(newCofactor);
                //this.metabolitesList.remove(metabolite);
            }

        }

        for (String cofactor : cofactors) {
            if (metabolitesList.contains(cofactor) && !deadEnd.contains(cofactor)) {
                ReactionFA newCofactor = new ReactionFA("ExC_" + cofactor);
                newCofactor.addReactant(cofactor, 1.0);
                newCofactor.setBounds(0, Double.POSITIVE_INFINITY);
                this.reactionsList.add(newCofactor);
                //  this.metabolitesList.remove(cofactor);
            }
        }
        int i = 0;
        for (ReactionFA reaction : this.reactionsList) {
            // System.out.println(reaction.getId()+ "bounds: "+ reaction.getlb() +  " - " + reaction.getub());
            if (reaction.getId().equals(this.objective)) {
                this.objectiveList.add(1.0);
                //   System.out.println("Objective 1");
            } else {
                this.objectiveList.add(0.0);
                //    System.out.println("Objective 0");
            }
            this.reactionPositionMap.put(reaction.getId(), i++);
        }

    }

    private List<String> getDeadEnds() {
        List<String> deadEnd = new ArrayList<>();
        for (String sp : this.metabolitesList) {
            boolean isInSource = false;
            boolean isInDestination = false;
            for (ReactionFA r : this.reactionsList) {
                for (String reactant : r.getReactants()) {
                    if (reactant.equals(sp)) {
                        isInSource = true;
                    }

                }
                for (String product : r.getProducts()) {
                    if (product.equals(sp)) {
                        isInDestination = true;
                    }
                }
            }
            if (!isInSource || !isInDestination) {
                deadEnd.add(sp);
            }
        }
        return deadEnd;
    }

    private void fixCofactorIssue(List<String> deadEnd, List<String> cofactors) {
        for (String cofactor : cofactors) {
            if (deadEnd.contains(cofactor)) {
                for (ReactionFA reaction : this.reactionsList) {
                    if (reaction.hasReactant(cofactor)) {
                        for (String product : reaction.getProducts()) {
                            if (cofactors.contains(product)) {
                                if (deadEnd.contains(product)) {
                                    //   if (cofactor.equals("C00003")) {
                                    this.metabolitesList.remove(cofactor);
                                    this.metabolitesList.remove(product);
                                    //    }
                                }
                            }
                        }
                    }

                    if (reaction.hasProduct(cofactor)) {
                        for (String reactant : reaction.getReactants()) {
                            if (cofactors.contains(reactant)) {
                                if (deadEnd.contains(reactant)) {
                                    //    if (cofactor.equals("C00003")) {
                                    this.metabolitesList.remove(cofactor);
                                    this.metabolitesList.remove(reactant);
                                    //    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
