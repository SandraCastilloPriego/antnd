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

import ND.data.Dataset;
import ND.modules.simulation.antNoGraph.ReactionFA;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 *
 * @author bicha
 */
public final class Bug {

    private Map<ReactionFA, status> rowList;
    private HashMap<String, ReactionFA> reactions;
    private double life = 300;
    double score = 0;
    Dataset dataset;
    String objective;
    int count = 1;
    double referenceBiomass, referenceObjective;
    private static Random rnd = new Random();

    public Bug(ReactionFA row, Dataset dataset, int bugLife, String objective, HashMap<String, ReactionFA> reactions, double referenceBiomass, double referenceObjective) {
        this.rowList = new HashMap<>();
        if (row != null) {
            this.rowList.put(row, this.getStatus(row));
        }
        this.dataset = dataset;
        this.life = bugLife;
        this.objective = objective;
        this.reactions = reactions;
        this.referenceBiomass = referenceBiomass;
        this.referenceObjective = referenceObjective;
        this.evaluation();
    }

    public static enum status {

        LB, UP, KO;
    }

    public status getStatus(ReactionFA row) {
        if (row.isBidirecctional()) {
            if (rnd.nextBoolean()) {
                return status.LB;
            } else {
                return status.UP;
            }
        } else {
            return status.KO;
        }
    }

    public Bug(Bug father, Bug mother, Dataset dataset, int bugLife) {
        this.dataset = dataset;
        this.life = bugLife;
        this.rowList = new HashMap<>();
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

    private boolean containsGene(Map<ReactionFA, status> list, ReactionFA gene) {
        for (ReactionFA originalGene : list.keySet()) {
            if (originalGene.getId().equals(gene.getId())) {
                return true;
            }
        }
        return false;
    }

    private Map<ReactionFA, status> selectGenes(Map<ReactionFA, status> father, Map<ReactionFA, status> mother) {
        Map<ReactionFA, status> finalGenes = new HashMap<>();
        Map<ReactionFA, status> tempGenes = new HashMap<>();

        double correctFinal = 0;

        // Add all the father genes to the finalGenes and tempGenes lists
        for (ReactionFA gene : father.keySet()) {
            finalGenes.put(gene, father.get(gene));
            tempGenes.put(gene, father.get(gene));
        }

        // Add all the mother genes to the finalGenes and tempGenes lists trying not to duplicate them
        for (ReactionFA gene : mother.keySet()) {
            if (!this.containsGene(finalGenes, gene)) {
                finalGenes.put(gene, mother.get(gene));
                tempGenes.put(gene, mother.get(gene));
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
               /* if(this.reactions.containsKey(r) && this.reactions.get(r).getId().equals(this.objective)){
                 flux += soln.get(r);
                 }*/
                if (this.reactions.containsKey(r) && this.reactions.get(r).hasProduct(this.objective) /*|| this.reactions.get(r).hasReactant(this.objective)*/) {
                    if (soln.get(r) > 0) {
                        flux += soln.get(r);
                    }
                }
                if (this.reactions.containsKey(r) && this.reactions.get(r).hasReactant(this.objective)) {
                    if (soln.get(r) < 0) {
                        flux -= soln.get(r);
                    }
                }
                /*if(r.equals("r_1672")&& soln.get(r)>0){
                 isTakingCO2 = false;
                 }*/
            }

            /*double refB = 0;
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
             }*/
            if (fba.getMaxObj() < 0.005) {
                score = 0.0;
            } else {
                score = flux;
            }

            if(score > 1500){
                score = score -2000;
            }else if(score > 500){
                score = score -1000;
            }if(score < -1500){
                score = score + 2000;
            }else if(score < -500){
                score = score +1000;
            }
            String solution = "";
            for (ReactionFA r : this.rowList.keySet()) {
                solution += r.getId() + " - ";
            }

            // if(!isTakingCO2) score = 0;
            System.out.println(solution + ": " + score);

            //mutation
          /*  int index = rnd.nextInt(this.rowList.size() - 1);
            int i = 0;
            ReactionFA selected = null;
            for (ReactionFA r : this.rowList.keySet()) {
                if (i == index) {
                    selected = r;
                }
                i++;
            }

            if (this.rowList.get(selected) == status.LB) {
                this.rowList.put(selected, status.UP);
            } else if (this.rowList.get(selected) == status.UP) {
                this.rowList.put(selected, status.LB);
            }*/

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

    public Map<ReactionFA, status> getRows() {
        return this.rowList;
    }

    public double getLife() {
        return life;
    }

    boolean isDead() {
        if (this.rowList.size() > 1) {
            life = life - (100 - this.score)/100;
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
        for (ReactionFA val : bug.getRows().keySet()) {
            if (!this.rowList.containsKey(val)) {
                return false;
            }
        }
        return true;

    }

    @Override
    public String toString() {
        String rows = "";
        for (ReactionFA row : this.rowList.keySet()) {
            rows += row.getId();
            rows += "-";
            rows += this.rowList.get(row).toString();
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
