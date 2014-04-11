/*
 * Copyright 2013-2014 VTT Biotechnology
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
package ND.modules.otimization.LP;

import ND.data.impl.datasets.SimpleBasicDataset;
import ND.parameters.SimpleParameterSet;
import ND.taskcontrol.AbstractTask;
import ND.taskcontrol.TaskStatus;
import com.csvreader.CsvReader;
import com.joptimizer.functions.ConvexMultivariateRealFunction;
import com.joptimizer.functions.LinearMultivariateRealFunction;
import com.joptimizer.optimizers.JOptimizer;
import com.joptimizer.optimizers.OptimizationRequest;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
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
 * @author scsandra
 */
public class LPTask extends AbstractTask {

        private final SimpleBasicDataset networkDS;
        private double finishedPercentage = 0.0f;
        private final File boundsFile, exchangeFile;
        private final List<String> sourcesList;
        private final String objective;
        private final boolean maximize;
        Map<String, Integer> reactions;
        Map<String, Integer> species;

        public LPTask(SimpleBasicDataset dataset, SimpleParameterSet parameters) {
                this.networkDS = dataset;
                this.boundsFile = parameters.getParameter(LPParameters.bounds).getValue();
                this.exchangeFile = parameters.getParameter(LPParameters.exchange).getValue();
                this.objective = parameters.getParameter(LPParameters.objective).getValue();
                this.maximize = parameters.getParameter(LPParameters.maximize).getValue();

                this.sourcesList = new ArrayList<>();
                this.reactions = new HashMap<>();
                this.species = new HashMap<>();
        }

        @Override
        public String getTaskDescription() {
                return "Starting LP optimization... ";
        }

        @Override
        public double getFinishedPercentage() {
                return finishedPercentage;
        }

        @Override
        public void cancel() {
                setStatus(TaskStatus.CANCELED);
        }

        @Override
        public void run() {
                try {
                        setStatus(TaskStatus.PROCESSING);

                        Map<String, Double> exchange = this.readExchangeReactions();
                        HashMap<String, String[]> bounds = readBounds();
                        SBMLDocument doc = this.networkDS.getDocument();
                        Model m = doc.getModel();

                        double[][] A = createMatrix(m, exchange);
                        finishedPercentage = 0.2f;
                        double[] b = createB(exchange);
                        finishedPercentage = 0.3f;
                        double[] objective = createObjective();
                        finishedPercentage = 0.4f;
                        double[] lb = createLB(bounds, exchange);
                        double[] ub = createUP(bounds, exchange);
                        try {
                                double[] solution = optimize(A, b, objective, lb, ub);
                                for (double x : solution) {
                                        System.out.println(x);
                                }
                        } catch (Exception ex) {
                                System.out.println(ex.toString());
                        }
                        setStatus(TaskStatus.FINISHED);

                } catch (Exception e) {
                        System.out.println(e.toString());
                        setStatus(TaskStatus.ERROR);
                }
        }

        private HashMap<String, String[]> readBounds() {
                HashMap<String, String[]> b = new HashMap<>();
                try {
                        SBMLDocument doc = this.networkDS.getDocument();
                        Model m = doc.getModel();

                        CsvReader reader = new CsvReader(new FileReader(this.boundsFile.getAbsolutePath()));

                        while (reader.readRecord()) {
                                String[] data = reader.getValues();
                                String reactionName = data[0].replace("-", "");
                                b.put(reactionName, data);

                                Reaction r = m.getReaction(reactionName);
                                if (r != null && r.getKineticLaw() == null) {
                                        KineticLaw law = new KineticLaw();
                                        LocalParameter lbound = new LocalParameter("LOWER_BOUND");
                                        lbound.setValue(Double.valueOf(data[3]));
                                        law.addLocalParameter(lbound);
                                        LocalParameter ubound = new LocalParameter("UPPER_BOUND");
                                        ubound.setValue(Double.valueOf(data[4]));
                                        law.addLocalParameter(ubound);
                                        r.setKineticLaw(law);
                                }
                        }
                } catch (FileNotFoundException ex) {
                } catch (IOException ex) {
                }
                return b;
        }

        private Map<String, Double> readExchangeReactions() {
                try {
                        CsvReader exchange = new CsvReader(new FileReader(this.exchangeFile.getAbsoluteFile()), '\t');
                        Map<String, Double> exchangeMap = new HashMap<>();

                        try {
                                while (exchange.readRecord()) {
                                        try {
                                                String[] exchangeRow = exchange.getValues();
                                                exchangeMap.put(exchangeRow[0], Double.parseDouble(exchangeRow[1]));
                                                this.sourcesList.add(exchangeRow[0]);
                                        } catch (IOException | NumberFormatException e) {
                                                e.printStackTrace();
                                        }
                                }
                        } catch (IOException ex) {
                        }

                        return exchangeMap;
                } catch (FileNotFoundException ex) {
                }
                return null;
        }

        private double[] optimize(double[][] A, double[] b, double[] objetive, double[] lb, double[] ub) throws Exception {
                // Objective function f(x) = q.x + r 
                LinearMultivariateRealFunction objectiveFunction = new LinearMultivariateRealFunction(objetive, 0);
                //inequalities (polyhedral feasible set G.X<H ) No idea                
                ConvexMultivariateRealFunction[] inequalities = new ConvexMultivariateRealFunction[lb.length + ub.length];
                int count = 0;
                for (int i = 0; i < lb.length; i++) {
                        inequalities[count++] = new LinearMultivariateRealFunction(A[i], -lb[i]);
                }
                for (int i = 0; i < ub.length; i++) {
                        inequalities[count++] = new LinearMultivariateRealFunction(A[i], ub[i]);
                }

                //optimization problem
                OptimizationRequest or = new OptimizationRequest();
                or.setF0(objectiveFunction);
                or.setFi(inequalities);
                or.setA(A);

                //zero for every reaction except for exchange reactions
                or.setB(b);

                or.setToleranceFeas(1.E-9);
                or.setTolerance(1.E-9);

                //optimization
                JOptimizer opt = new JOptimizer();
                opt.setOptimizationRequest(or);
                int returnCode = opt.optimize();
                System.out.println(returnCode);
                double[] sol = opt.getOptimizationResponse().getSolution();
                return sol;
        }

        private double[][] createMatrix(Model m, Map<String, Double> exchange) {
                double[][] A = new double[m.getNumReactions() + this.sourcesList.size()][m.getNumSpecies()];

                int count = 0;

                for (Species s : m.getListOfSpecies()) {
                        species.put(s.getId(), count++);
                }

                count = 0;
                for (Reaction r : m.getListOfReactions()) {
                        reactions.put(r.getId(), count);
                        for (SpeciesReference reactants : r.getListOfReactants()) {
                                Species reactant = reactants.getSpeciesInstance();
                                int index = species.get(reactant.getId());
                                A[count][index] = -1.0;
                        }
                        for (SpeciesReference products : r.getListOfProducts()) {
                                Species product = products.getSpeciesInstance();
                                int index = species.get(product.getId());
                                A[count][index] = 1.0;
                        }
                        count++;
                }
                //add exchange reactions
                for (String ex : exchange.keySet()) {
                        reactions.put(ex, count);
                        int index = species.get(ex);
                        if (exchange.get(ex) < 0) {
                                A[count++][index] = -1;
                        } else {
                                A[count++][index] = 1;
                        }
                }

                for (int i = 0; i < A.length; i++) {
                        for (int e = 0; e < A[0].length; e++) {
                                if (A[i][e] != 1.0 && A[i][e] != -1.0) {
                                        A[i][e] = 0.0;
                                }
                        }
                }

                return A;
        }

        private double[] createB(Map<String, Double> exchange) {
                //for each reaction 0 is added except for the exchange reactions
                double[] b = new double[this.reactions.size()];
                for (String r : reactions.keySet()) {
                        int index = reactions.get(r);
                        if (exchange.containsKey(r)) {
                                b[index] = exchange.get(r);
                        } else {
                                b[index] = 0;
                        }
                }
                return b;
        }

        private double[] createObjective() {
                double[] objective = new double[this.reactions.size()];
                for (String r : reactions.keySet()) {
                        int index = reactions.get(r);
                        if (r.contains(this.objective)) {
                                if (maximize) {
                                        objective[index] = 1;
                                } else {
                                        objective[index] = -1;
                                }
                        } else {
                                objective[index] = 0;
                        }
                }
                return objective;
        }

        private double[] createLB(HashMap<String, String[]> bounds, Map<String, Double> exchange) {
                double[] lb = new double[this.reactions.size()];
                for (String r : reactions.keySet()) {
                        int index = reactions.get(r);
                        if (exchange.containsKey(r)) {
                                lb[index] = exchange.get(r);
                        } else {
                                String[] b = bounds.get(r);
                                lb[index] = Double.valueOf(b[3]);
                        }
                }
                return lb;
        }

        private double[] createUP(HashMap<String, String[]> bounds, Map<String, Double> exchange) {
                double[] lb = new double[this.reactions.size()];
                for (String r : reactions.keySet()) {
                        int index = reactions.get(r);
                        if (exchange.containsKey(r)) {
                                lb[index] = exchange.get(r);
                        } else {
                                String[] b = bounds.get(r);
                                lb[index] = Double.valueOf(b[4]);
                        }
                }
                return lb;
        }
}
