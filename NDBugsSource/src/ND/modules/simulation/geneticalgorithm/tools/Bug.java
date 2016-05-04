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
package ND.modules.simulation.geneticalgorithm.tools;

import ND.data.Dataset;
import ND.modules.simulation.antNoGraph.ReactionFA;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author bicha
 */
public final class Bug {

    private List<ReactionFA> rowList;
    private HashMap<String, ReactionFA> reactions;
    private double life = 300;
    double score = 0;
    Dataset dataset;
    String objective;
    int count = 1;
    double referenceBiomass, referenceObjective;

    public Bug(ReactionFA row, Dataset dataset, int bugLife, String objective, HashMap<String, ReactionFA> reactions, double referenceBiomass, double referenceObjective) {
        this.rowList = new ArrayList<>();
        if (row != null) {
            this.rowList.add(row);
        }
        this.dataset = dataset;
        this.life = bugLife;
        this.objective = objective;
        this.reactions = reactions;
        this.referenceBiomass = referenceBiomass;
        this.referenceObjective = referenceObjective;
        this.evaluation();
    }

    public Bug(Bug father, Bug mother, Dataset dataset, int bugLife) {
        this.dataset = dataset;
        this.life = bugLife;
        this.rowList = new ArrayList<>();
        this.reactions = father.getReactions();
        this.objective = father.getObjective();
        this.referenceBiomass = father.referenceBiomass;
        this.referenceObjective = father.referenceObjective;

        if (father.getRows().isEmpty() || mother.getRows().isEmpty()) {
            this.life = 0;
        } else {
            this.rowList = selectGenes(father.getRows(), mother.getRows());
            if (this.rowList.isEmpty()) {
                this.life = 0;

            } else {
                this.life = bugLife;
                this.evaluation();
            }
        }

    }

    private boolean containsGene(List<ReactionFA> list, ReactionFA gene) {
        for (ReactionFA originalGene : list) {
            if (originalGene.getId().equals(gene.getId())) {
                return true;
            }
        }
        return false;
    }

    private List<ReactionFA> selectGenes(List<ReactionFA> father, List<ReactionFA> mother) {
        List<ReactionFA> finalGenes = new ArrayList<>();
        List<ReactionFA> tempGenes = new ArrayList<>();

        double correctFinal = 0;

        // Add all the father genes to the finalGenes and tempGenes lists
        for (ReactionFA gene : father) {
            finalGenes.add(gene);
            tempGenes.add(gene);
        }

        // Add all the mother genes to the finalGenes and tempGenes lists trying not to duplicate them
        for (ReactionFA gene : mother) {
            if (!this.containsGene(finalGenes, gene)) {
                finalGenes.add(gene);
                tempGenes.add(gene);
                //        System.out.print(gene.getID() + ", ");
            }
        }
        /*
         //System.out.println(data.numInstances());
         correctFinal = this.getMeasure(finalGenes);
         // System.out.println("Genes: "+finalGenes.size()+" Value:"+correctFinal);

         // For each gene in the temporary list
         for (ReactionFA tempGene : tempGenes) {
         // remove gene from the final gene list
         finalGenes.remove(tempGene);
         // create a new dataset
         if (finalGenes.size() > 0) {                
         double correct = this.getMeasure(finalGenes);
         // if the f-value is better or equal than the previous one update it. 
         // If not, put the gene again to the finalGenes list.
         if (correct >= correctFinal) {
         correctFinal = correct;
         } else {
         //            System.out.println("added back");
         finalGenes.add(tempGene);
         }
         }
         }*/

        return finalGenes;
    }

//    private double getMeasure(List<ReactionFA> finalGenes) {
//        FBA fba = new FBA();
//        fba.setModel(this.reactions, this.dataset.getDocument().getModel(), finalGenes);
//        try {
//            Double flux = 0.0;
//            Map<String, Double> soln = fba.run();
//            for (String r : soln.keySet()) {
//                if (this.reactions.containsKey(r) && (this.reactions.get(r).hasProduct(this.objective) || this.reactions.get(r).hasReactant(this.objective))) {
//                    flux += soln.get(r);
//                }
//            }
//            return flux;
//        } catch (Exception ex) {
//            System.out.println(ex);
//        }
//        return 0.0;
//    }
    public void evaluation() {
        FBA fba = new FBA();
        //fba.setSoverType(true);
        fba.setModel(this.reactions, this.dataset.getDocument().getModel(), this.rowList);
        try {
            // boolean isTakingCO2 = true;
            double flux = 0.0;
            Map<String, Double> soln = fba.run();
            for (String r : soln.keySet()) {
                //System.out.println(r);
                if(this.reactions.containsKey(r) && this.reactions.get(r).getId().equals(this.objective)){
                    flux += soln.get(r);
                }
//                if (this.reactions.containsKey(r) && this.reactions.get(r).hasProduct(this.objective) /*|| this.reactions.get(r).hasReactant(this.objective)*/) {
//                    if (soln.get(r) > 0) {
//                        flux += soln.get(r);
//                    }
//                }
//                if (this.reactions.containsKey(r) && this.reactions.get(r).hasReactant(this.objective)) {
//                    if (soln.get(r) < 0) {
//                        flux -= soln.get(r);
//                    }
//                }
                /*if(r.equals("r_1672")&& soln.get(r)>0){
                 isTakingCO2 = false;
                 }*/
            }

            double refB = 0;
            if (fba.getMaxObj() > this.referenceBiomass) {
                refB = 1;
            } else {
                refB = fba.getMaxObj() / this.referenceBiomass;
            }
            double refO = 0;
            if (flux > this.referenceObjective) {
                refO = 1;
            } else if (flux < 0) {
                refO = 0;
            } else {
                refO = flux / this.referenceObjective;
            }
            if (refO == 0) {
                refO = 0.00001;
            }
            this.score = 2 * ((refB * refO) / (refB + refO));
            //score = fba.getMaxObj();
            if (score == Double.POSITIVE_INFINITY || score == Double.NaN) {
                score = 0.0;
            }

            //  if(fba.getMaxObj()<0.000001) score = 0.0;
            score = flux;

            String solution = "";
            for (ReactionFA r : this.rowList) {
                solution += r.getId() + " - ";
            }
            // if(!isTakingCO2) score = 0;
            System.out.println(solution + ": " + score);
        } catch (Exception ex) {
            System.out.println(ex);
        }

    }

    public double getScore() {
        if (score == Double.NaN) {
            return 0.0;
        }
        return score;
    }

    public List<ReactionFA> getRows() {
        return this.rowList;
    }

    public double getLife() {
        return life;
    }

    boolean isDead() {
        if (this.rowList.size() > 1) {
            life = life - (1 - this.score);
        }
        if (this.rowList.isEmpty()) {
            life = 0;
        }
        if (this.life < 1 || this.life == Double.NaN) {
            return true;
        } else {
            return false;
        }
    }

    public void kill() {
        this.life = -1;
    }

    public boolean isSameBug(Bug bug) {
        if (bug.getRows().size() != this.rowList.size()) {
            return false;
        }
        for (ReactionFA val : bug.getRows()) {
            if (!this.rowList.contains(val)) {
                return false;
            }
        }
        return true;

    }

    @Override
    public String toString() {
        String rows = "";
        for (ReactionFA row : this.rowList) {
            rows += row.getId();
            rows += ",";
        }
        return rows;
    }

    private HashMap<String, ReactionFA> getReactions() {
        return this.reactions;
    }

    private String getObjective() {
        return this.objective;
    }

}
