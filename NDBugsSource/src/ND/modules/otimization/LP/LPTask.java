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
import ND.main.NDCore;
import ND.modules.configuration.cofactors.CofactorConfParameters;
import ND.modules.simulation.antNoGraph.network.Edge;
import ND.modules.simulation.antNoGraph.network.Graph;
import ND.modules.simulation.antNoGraph.network.Node;
import ND.modules.simulation.antNoGraph.uniqueId;
import ND.parameters.SimpleParameterSet;
import ND.taskcontrol.AbstractTask;
import ND.taskcontrol.TaskStatus;
import com.csvreader.CsvReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.sf.javailp.Constraint;
import net.sf.javailp.Linear;
import net.sf.javailp.Operator;
import net.sf.javailp.Problem;
import net.sf.javailp.Result;
import net.sf.javailp.Solver;
import net.sf.javailp.SolverFactory;
import net.sf.javailp.SolverFactoryGLPK;
import net.sf.javailp.VarType;
import org.sbml.jsbml.KineticLaw;
import org.sbml.jsbml.ListOf;
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
        private final String objectiveReaction;
        private final boolean maximize;
        private final List<String> species;
        private final List<String> reactions;
        private Map<String, Double> exchange;
        private final String NAD, NADP, ADP;
        private final boolean steadyState;

        public LPTask(SimpleBasicDataset dataset, SimpleParameterSet parameters) {
                this.networkDS = dataset;
                this.boundsFile = parameters.getParameter(LPParameters.bounds).getValue();
                this.exchangeFile = parameters.getParameter(LPParameters.exchange).getValue();
                this.objectiveReaction = parameters.getParameter(LPParameters.objective).getValue();
                this.maximize = parameters.getParameter(LPParameters.maximize).getValue();
                this.steadyState = parameters.getParameter(LPParameters.steadyState).getValue();

                CofactorConfParameters conf = new CofactorConfParameters();
                this.NAD = conf.getParameter(CofactorConfParameters.NAD).getValue();
                this.NADP = conf.getParameter(CofactorConfParameters.NADP).getValue();
                this.ADP = conf.getParameter(CofactorConfParameters.ADP).getValue();

                this.reactions = new ArrayList<>();
                this.species = new ArrayList<>();
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

                        this.exchange = this.readExchangeReactions();
                        //   System.out.println("0");

                        HashMap<String, String[]> bounds = readBounds();
                        SBMLDocument doc = this.networkDS.getDocument();
                        Model m = doc.getModel();
                        //     System.out.println("1");
                        double[][] A = createMatrix(m);
                        finishedPercentage = 0.2f;
                        //    System.out.println("2");
                        double[] b = createB();
                        finishedPercentage = 0.3f;
                        //  System.out.println("3");
                        double[] objective = createObjective();
                        finishedPercentage = 0.4f;
                        // System.out.println("4");
                        double[] lb = createLB(bounds);
                        // System.out.println("5");
                        double[] ub = createUP(bounds);
                        //System.out.println("5.5");
                        finishedPercentage = 0.5f;
                        try {
                                Result solution = optimize(A, b, objective, lb, ub);
                                processSolution(solution);
                                finishedPercentage = 1.0f;
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
                                Model m = this.networkDS.getDocument().getModel();
                                while (exchange.readRecord()) {
                                        try {
                                                String[] exchangeRow = exchange.getValues();

                                                if (m.containsSpecies(exchangeRow[0])) {
                                                        exchangeMap.put(exchangeRow[0], Double.parseDouble(exchangeRow[1]));
                                                }
                                        } catch (IOException | NumberFormatException e) {
                                                e.printStackTrace();
                                        }
                                }
                        } catch (IOException ex) {
                        }
                        if (steadyState) {
                                exchangeMap.put(this.NAD, 100.0);
                                exchangeMap.put(this.NADP, 100.0);
                                exchangeMap.put(this.ADP, 100.0);
                        }
                        return exchangeMap;
                } catch (FileNotFoundException ex) {
                }
                return null;
        }

        private Result optimize(double[][] A, double[] b, double[] objective, double[] lb, double[] ub) {
                SolverFactory factory = new SolverFactoryGLPK();
                //factory.setParameter(Solver.VERBOSE, 0);
                factory.setParameter(Solver.TIMEOUT, 100);
                Problem problem = new Problem();
                List<String> variables = new ArrayList<>();
                //Objective Function
                Linear linear = new Linear();
                for (int i = 0; i < objective.length; i++) {
                        String var = this.reactions.get(i);
                        //System.out.println(var + " - " + objective[i]);
                        variables.add(var);
                        linear.add(objective[i], var);
                }
                // if (maximize) {
                problem.setObjective(linear);
                /* } else {
                 problem.setObjective(linear;
                 }*/
                // Inequalities
                for (int i = 0; i < b.length; i++) {
                        linear = new Linear();
                        for (int e = 0; e < A.length; e++) {
                                linear.add(A[e][i], variables.get(e));
                        }
                        problem.add(new Constraint(this.species.get(i), linear, Operator.EQ, b[i]));
                }
                for (int i = 0; i < variables.size(); i++) {
                        problem.setVarLowerBound(variables.get(i), lb[i]);
                        problem.setVarUpperBound(variables.get(i), ub[i]);
                        //  System.out.println(variables.get(i) + " - " + lb[i] + " - " + ub[i]);
                }
                for (String var : variables) {
                        problem.setVarType(var, VarType.REAL);
                }
                Solver solver = factory.get(); // you should use this solver only once for one problem
                Result result = solver.solve(problem);

                //System.out.println(result.toString());
                return result;
        }

        private double[][] createMatrix(Model m) {
                for (Species s : m.getListOfSpecies()) {
                        this.species.add(s.getId());
                }
                /* for (Species s : m.getListOfSpecies()) {
                 this.species.add(s.getId() + "out");
                 }*/
                /*int countex = 0;
                 for (String ex : exchange.keySet()) {
                 if (species.contains(ex)) {
                 countex++;
                 }
                 }*/
                double[][] A = new double[m.getNumReactions() + m.getNumSpecies()][m.getNumSpecies() /**
                         * 2
                         */
                        ];

                /*if (!this.species.contains(this.NAD)) {
                 this.species.add(this.NAD);
                 }
                
                 if (!this.species.contains(this.NADP)) {
                 this.species.add(this.NADP);
                 }
                
                 if (!this.species.contains(this.ADP)) {
                 this.species.add(this.ADP);
                 }*/
                int count = 0;
                for (Reaction r : m.getListOfReactions()) {
                        this.reactions.add(r.getId());
                        for (SpeciesReference reactants : r.getListOfReactants()) {
                                Species reactant = reactants.getSpeciesInstance();
                                int index = species.indexOf(reactant.getId());
                                A[count][index] = -reactants.getStoichiometry();
                        }
                        for (SpeciesReference products : r.getListOfProducts()) {
                                Species product = products.getSpeciesInstance();
                                int index = species.indexOf(product.getId());
                                A[count][index] = products.getStoichiometry();
                        }
                        count++;
                }
                //add exchange reactions
                for (String ex : exchange.keySet()) {
                        if (species.contains(ex)) {
                                reactions.add(ex);
                                int index = species.indexOf(ex);
                                A[count++][index] = -1;
                        }
                }

                // System.out.println(m.getNumSpecies() + " - " + count);
                for (Species sp : m.getListOfSpecies()) {
                        if (!exchange.containsKey(sp.getId()) /*&& !sp.getId().contains("Growth")*/) {
                                reactions.add(sp.getId());
                                int index = species.indexOf(sp.getId());
                                A[count++][index] = -1;
                        }
                }
                // System.out.println(m.getNumSpecies() + " - " + count);

                /*for (int i = 0; i < A.length; i++) {
                 for (int e = 0; e < A[0].length; e++) {
                 System.out.print(A[i][e] + " ");
                 }
                 System.out.print("\n");
                 }
                 for (String sp : this.species) {
                 System.out.println(sp);
                 }*/
                return A;
        }

        private double[] createB() {
                //for each reaction 0 is added except for the exchange reactions
                double[] b = new double[this.species.size()];
                for (String r : species) {
                        int index = species.indexOf(r);
                        if (exchange.containsKey(r)) {
                                b[index] = 0;
                        } else {
                                b[index] = 0;
                        }
                }
                return b;
        }

        private double[] createObjective() {
                double[] objective = new double[this.reactions.size()];
                for (int index = 0; index < reactions.size(); index++) {
                        String r = reactions.get(index);
                        if (r.contains(this.objectiveReaction)) {
                                if (maximize) {
                                        objective[index] = -1;
                                } else {
                                        objective[index] = 1;
                                }
                        } else {
                                objective[index] = 0;
                        }
                        // System.out.println(objective[index]);
                }
                return objective;
        }

        private double[] createLB(HashMap<String, String[]> bounds) {
                double[] lb = new double[this.reactions.size()];
                Model m = this.networkDS.getDocument().getModel();
                for (int index = 0; index < reactions.size(); index++) {
                        String r = reactions.get(index);
                        // System.out.println(r);
                        if (exchange.containsKey(r)) {
                                lb[index] = -exchange.get(r);
                        } else {
                                String[] b = bounds.get(r);
                                if (b == null) {
                                        Reaction reaction = m.getReaction(r);
                                        if (reaction != null) {
                                                KineticLaw law = reaction.getKineticLaw();
                                                if (law != null) {
                                                        LocalParameter lbound = law.getLocalParameter("LOWER_BOUND");
                                                        lb[index] = lbound.getValue();
                                                } else {
                                                        lb[index] = 0.0;
                                                }
                                        } else {
                                                lb[index] = 0.0;
                                        }
                                } else {
                                        lb[index] = Double.valueOf(b[3]);
                                }
                        }
                        //  System.out.println(r + " - " + lb[index]);
                }
                return lb;
        }

        private double[] createUP(HashMap<String, String[]> bounds) {
                double[] ub = new double[this.reactions.size()];
                Model m = this.networkDS.getDocument().getModel();
                for (int index = 0; index < reactions.size(); index++) {
                        String r = reactions.get(index);
                        if (exchange.containsKey(r)) {
                                if (r.contains("C00031")) {
                                        ub[index] = -(this.exchange.get(r)-0.001);
                                } else {
                                        ub[index] = 1000;
                                }
                        } else {
                                String[] b = bounds.get(r);
                                if (b == null) {
                                        Reaction reaction = m.getReaction(r);
                                        if (reaction != null) {
                                                KineticLaw law = reaction.getKineticLaw();
                                                if (law != null) {
                                                        LocalParameter lbound = law.getLocalParameter("UPPER_BOUND");
                                                        ub[index] = lbound.getValue();
                                                } else {
                                                        ub[index] = 1000.0;
                                                }
                                        } else {
                                                ub[index] = 1000.0;
                                        }
                                } else {
                                        ub[index] = Double.valueOf(b[4]);
                                }
                        }// System.out.println(r + " - " + ub[index]);

                }
                return ub;
        }

        private void processSolution(Result solution) {
                System.out.println("Objective: " + solution.getObjective());
                System.out.println(solution);
                Map<String, Double> solutionMap = new HashMap<>();
                for (String reaction : this.reactions) {
                        //System.out.println(reaction + " " + solution.get(reaction));
                        solutionMap.put(reaction, (Double) solution.get(reaction));
                }

                createDataFile(solutionMap, (double) solution.getObjective());

        }

        private void createDataFile(Map<String, Double> solution, double objective) {

                SBMLDocument newDoc = this.networkDS.getDocument().clone();
                Model m = this.networkDS.getDocument().getModel();
                Model newModel = newDoc.getModel();

                for (Reaction reaction : m.getListOfReactions()) {
                        if (solution.containsKey(reaction.getId()) && Math.abs(solution.get(reaction.getId())) < 0.00000001) {
                                newModel.removeReaction(reaction.getId());
                        }
                }

                for (Species sp : m.getListOfSpecies()) {
                        if (!this.isInReactions(newModel.getListOfReactions(), sp)) {
                                newModel.removeSpecies(sp.getId());
                        }
                }

                SimpleBasicDataset dataset = new SimpleBasicDataset();

                dataset.setDocument(newDoc);
                dataset.setDatasetName("LPOptimization  - " + newModel.getId() + ".sbml");
                dataset.addInfo("LP Optimization: maximizing: " + this.maximize + "\nSteady state: " + this.steadyState + "\nOjective: " + objective + "\nSolution: " + solution + "\n---------------------------");
                Path path = Paths.get(this.networkDS.getPath());
                Path fileName = path.getFileName();
                String name = fileName.toString();
                String p = this.networkDS.getPath().replace(name, "");
                p = p + dataset.getDatasetName();
                dataset.setPath(p);

                NDCore.getDesktop().AddNewFile(dataset);

                dataset.setGraph(createGraph(solution, newModel));
                dataset.setSources(this.networkDS.getSources());
                dataset.setBiomass(this.networkDS.getBiomassId());

        }

        private boolean isInReactions(ListOf<Reaction> listOfReactions, Species sp) {
                for (Reaction r : listOfReactions) {
                        if (r.hasProduct(sp) || r.hasReactant(sp)) {
                                return true;
                        }
                }
                return false;
        }

        private Graph createGraph(Map<String, Double> solution, Model newModel) {
                List<Node> nodes = new ArrayList<>();
                List<Edge> edges = new ArrayList<>();
                for (Reaction r : newModel.getListOfReactions()) {
                        Node n = new Node(r.getId() + " - " + solution.get(r.getId()));
                        nodes.add(n);
                        List<SpeciesReference> reactants;
                        List<SpeciesReference> products;
                        if (solution.get(r.getId()) > 0) {
                                reactants = r.getListOfReactants();
                                products = r.getListOfProducts();
                        } else {
                                products = r.getListOfReactants();
                                reactants = r.getListOfProducts();
                        }

                        for (SpeciesReference sp : reactants) {
                                Node newNode = this.getNode(nodes, sp.getSpeciesInstance().getId());
                                if (newNode == null) {
                                        if (this.exchange.containsKey(sp.getSpeciesInstance().getId())) {
                                                newNode = new Node("sp:" + sp.getSpeciesInstance().getId() + " - " + this.exchange.get(sp.getSpeciesInstance().getId()));
                                        } else {
                                                newNode = new Node("sp:" + sp.getSpeciesInstance().getId());
                                        }
                                }
                                nodes.add(newNode);
                                Edge edge = new Edge(sp.getSpeciesInstance().getId() + "-" + uniqueId.nextId(), newNode, n);
                                edges.add(edge);
                        }

                        for (SpeciesReference sp : products) {
                                Node newNode = this.getNode(nodes, sp.getSpeciesInstance().getId());
                                if (newNode == null) {
                                        if (this.exchange.containsKey(sp.getSpeciesInstance().getId())) {
                                                newNode = new Node("sp:" + sp.getSpeciesInstance().getId() + " - " + this.exchange.get(sp.getSpeciesInstance().getId()));
                                        } else {
                                                newNode = new Node("sp:" + sp.getSpeciesInstance().getId());
                                        }
                                }
                                nodes.add(newNode);
                                Edge edge = new Edge(sp.getSpeciesInstance().getId() + "-" + uniqueId.nextId(), n, newNode);
                                edges.add(edge);
                        }

                }

                return new Graph(nodes, edges);
        }

        private Node getNode(List<Node> nodes, String s) {
                for (Node n : nodes) {
                        if (n.getId().contains(s)) {
                                return n;
                        }
                }
                return null;
        }

}
