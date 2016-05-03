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
import org.apache.commons.math.stat.correlation.PearsonsCorrelation;
import weka.classifiers.functions.SimpleLinearRegression;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.SparseInstance;

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

    public Bug(ReactionFA row, Dataset dataset, int bugLife, String objective, HashMap<String, ReactionFA> reactions) {
        this.rowList = new ArrayList<>();
        if (row != null) {
            this.rowList.add(row);
        }
        this.dataset = dataset;
        this.life = bugLife;
        this.objective = objective;
        this.reactions = reactions;
        this.evaluation();
    }

    public Bug(Bug father, Bug mother, Dataset dataset, int bugLife) {
        this.dataset = dataset;
        this.life = bugLife;
        this.rowList = new ArrayList<>();
        this.reactions = father.getReactions();
        this.objective = father.getObjective();

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
        double slope = this.getParetoR();
        System.out.println("Correlation: " + slope);
        if (slope == 0) {
            this.score = 0;
        } else {
            this.score = slope;
        }
        /* setObjective(this.objective);

         FBA fba = new FBA();
         //fba.setSoverType(true);
         fba.setModel(this.reactions, this.dataset.getDocument().getModel(), this.rowList);
         try {
         // boolean isTakingCO2 = true;
         fba.run();
         double flux = fba.getMaxObj();
         setObjective("r_2111");
         fba = new FBA();
         fba.run();
         double fluxBiomass = fba.getMaxObj();

         System.out.println(fluxBiomass + " -- " + flux);

         /*  double refB = 0;
         if (fluxBiomass > this.referenceBiomass) {
         refB = 1;
         } else if (fluxBiomass == 0) {
         refB = 0;
         } else {
         refB = fluxBiomass / this.referenceBiomass;
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
         if (refB == 0) {
         refB = 0.00001;
         }
         System.out.println(refB + " -- " + refO);
         this.score = 2 * ((refB * refO) / (refB + refO));*/
        //score = fba.getMaxObj();
          /*  if (score == Double.POSITIVE_INFINITY || score == Double.NaN) {
         score = 0.0;
         }

         if (fba.getMaxObj() < 0.000001) {
         score = 0.0;
         }
         // score = flux;

         */
        String solution = "";
        for (ReactionFA r : this.rowList) {
            solution += r.getId() + " - ";
        }
        // if(!isTakingCO2) score = 0;
        System.out.println(solution + ": " + score);
        /*} catch (Exception ex) {
         System.out.println(ex);
         }*/

    }

    public void setObjective(String objective) {
        for (ReactionFA r : this.reactions.values()) {
            if (r.getId().equals(objective)) {
                r.setObjective(1);
            } else {
                r.setObjective(0);
            }
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

    private double getParetoSlope() {
        try {
            FastVector fvWekaAttributes = new FastVector(2);
            Attribute fluxes = new Attribute("Biomassfluxes");
            Attribute fluxesobj = new Attribute("Objectivefluxes");
            fvWekaAttributes.addElement(fluxes);
            fvWekaAttributes.addElement(fluxesobj);
            Instances data = new Instances("Data", fvWekaAttributes, 0);
            data.setClass(data.attribute("Objectivefluxes"));

            setObjective(this.objective);
            this.reactions.get("r_2111").setBounds(0.01, 0.01);
            FBA fba = new FBA();
            fba.setModel(this.reactions, this.dataset.getDocument().getModel(), this.rowList);
            fba.run();
            double values[] = new double[2];
            double v = fba.getMaxObj();
            if (v > 0) {
                values[0] = v;
                values[1] = 0.01;
                Instance inst = new SparseInstance(1.0, values);
                data.add(inst);
            }

            setObjective(this.objective);
            this.reactions.get("r_2111").setBounds(0.05, 0.05);
            fba = new FBA();
            fba.setModel(this.reactions, this.dataset.getDocument().getModel(), this.rowList);
            fba.run();
            values = new double[2];
            v = fba.getMaxObj();
            if (v > 0) {
                values[0] = v;
                values[1] = 0.05;
                Instance inst = new SparseInstance(1.0, values);
                data.add(inst);
            }

            setObjective(this.objective);
            this.reactions.get("r_2111").setBounds(0.1, 0.1);
            fba = new FBA();
            fba.setModel(this.reactions, this.dataset.getDocument().getModel(), this.rowList);
            fba.run();
            values = new double[2];
            v = fba.getMaxObj();
            if (v > 0) {
                values[0] = v;
                values[1] = 0.1;
                Instance inst = new SparseInstance(1.0, values);
                data.add(inst);
            }

            setObjective(this.objective);
            this.reactions.get("r_2111").setBounds(0.2, 0.2);
            fba = new FBA();
            fba.setModel(this.reactions, this.dataset.getDocument().getModel(), this.rowList);
            fba.run();
            values = new double[2];
            v = fba.getMaxObj();
            if (v > 0) {
                values[0] = v;
                values[1] = 0.2;
                Instance inst = new SparseInstance(1.0, values);
                data.add(inst);
            }

            SimpleLinearRegression sr = new SimpleLinearRegression();
            sr.buildClassifier(data);
            return sr.getSlope();

        } catch (Exception e) {
            e.printStackTrace();
            return 0.0;
        }

    }

    private double getParetoR() {
        try {
            double[] valuesX = new double[4];
            double[] valuesY = new double[4];
            double production = 0.0;
            setObjective("r_2111");
            
            this.reactions.get(this.objective).setBounds(0.01, 0.01);
            FBA fba = new FBA();
            fba.setModel(this.reactions, this.dataset.getDocument().getModel(), this.rowList);
            fba.run();
            double v = fba.getMaxObj();
            if (v > 0) {
                valuesX[0] = v;
                valuesY[0] = 0.01;                
            }

            this.reactions.get(this.objective).setBounds(0.5, 0.5);
            fba = new FBA();
            fba.setModel(this.reactions, this.dataset.getDocument().getModel(), this.rowList);
            fba.run();
            v = fba.getMaxObj();
            if (v > 0) {
                valuesX[1] = v;
                valuesY[1] = 0.5;

            }

            setObjective(this.objective);
            this.reactions.get(this.objective).setBounds(1.0, 1.0);
            fba = new FBA();
            fba.setModel(this.reactions, this.dataset.getDocument().getModel(), this.rowList);
            fba.run();
            v = fba.getMaxObj();
            if (v > 0) {
                valuesX[2] = v;
                valuesY[2] = 1.0;

            }

            setObjective(this.objective);
            this.reactions.get(this.objective).setBounds(1.9, 1.9);
            fba = new FBA();
            fba.setModel(this.reactions, this.dataset.getDocument().getModel(), this.rowList);
            fba.run();
            v = fba.getMaxObj();
            if (v > 0) {
                valuesX[3] = v;
                valuesY[3] = 1.9;

            }

            //CorrelationAttributeEval sr = new CorrelationAttributeEval();
            // sr.buildClassifier(data);
            //  return sr.g;
            
                return new PearsonsCorrelation().correlation(valuesX, valuesY) * -1;
            

        } catch (Exception e) {
            e.printStackTrace();
            return -10.0;
        }

    }

}
