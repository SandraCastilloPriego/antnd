/*
 * Copyright 2007-2010 VTT Biotechnology
 * This file is part of GopiBugs.
 *
 * GopiBugs is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * GopiBugs is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * GopiBugs; if not, write to the Free Software Foundation, Inc., 51 Franklin St,
 * Fifth Floor, Boston, MA 02110-1301 USA
 */
package ND.modules.simulation.control;

import ND.data.impl.datasets.SimpleBasicDataset;
import ND.main.NDCore;
import ND.modules.Network.Graph;
import ND.modules.simulation.Bug;
import ND.modules.simulation.World;
import ND.parameters.SimpleParameterSet;
import ND.taskcontrol.AbstractTask;
import ND.taskcontrol.TaskStatus;
import java.awt.Dimension;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javax.swing.JInternalFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 *
 * @author scsandra
 */
public class StartSimulationTask extends AbstractTask {

        private SimpleBasicDataset networkDS;
        private double finishedPercentage = 0.0f;
        private sinkThread thread;
        private World world;
        private int stoppingCriteria, maxNumberOfNodes, maxNumberOfBugs;
        private double mutationRate;
        private JTextArea textArea;
        private List<Bug> bestBug;
        private double bestBugWeight = 0;
        private int counter = 0;
        private File weights;

        public StartSimulationTask(SimpleBasicDataset dataset, SimpleParameterSet parameters) {

                networkDS = dataset;

                this.weights = parameters.getParameter(StartSimulationParameters.weights).getValue();
                this.maxNumberOfNodes = parameters.getParameter(StartSimulationParameters.numberOfNodes).getValue();
                this.maxNumberOfBugs = parameters.getParameter(StartSimulationParameters.numberOfBugs).getValue();
                this.stoppingCriteria = parameters.getParameter(StartSimulationParameters.numberOfcycles).getValue();
                this.mutationRate = parameters.getParameter(StartSimulationParameters.mutationRate).getValue();
                this.bestBug = new ArrayList<>();


        }

        @Override
        public String getTaskDescription() {
                return "Starting simulation... ";
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
                        JInternalFrame frame2 = new JInternalFrame("Results", true, true, true, true);
                        frame2.setSize(new Dimension(700, 700));

                        textArea = new JTextArea("");
                        textArea.setSize(new Dimension(700, 700));
                        JScrollPane panel = new JScrollPane(textArea);
                        frame2.add(panel);
                        NDCore.getDesktop().addInternalFrame(frame2);

                        world = new World(networkDS, this.maxNumberOfNodes, this.maxNumberOfBugs, this.weights, this.mutationRate);
                        thread = new sinkThread();
                        thread.start();
                } catch (Exception e) {
                        e.printStackTrace();
                        setStatus(TaskStatus.ERROR);
                }
        }

        public class sinkThread extends Thread {

                @Override
                public void run() {
                        while (1 == 1) {

                                if (getStatus() == TaskStatus.PROCESSING) {
                                        world.cicle();
                                        List<Bug> bugs = world.getPopulation();
                                        try {
                                                Collections.sort(bugs, new Comparator<Bug>() {
                                                        @Override
                                                        public int compare(Bug o1, Bug o2) {
                                                                if (o1.getWeight() < o2.getWeight()) {
                                                                        return 1;
                                                                } else if (o1.getWeight() == o2.getWeight()) {
                                                                        return 0;
                                                                } else {
                                                                        return -1;
                                                                }
                                                        }
                                                });
                                                for (int i = 0; i < 100; i++) {
                                                        if (bugs.size() > i) {
                                                                Bug b = bugs.get(i).clone();
                                                                if (bestBug.isEmpty() || b.getWeight() > bestBugWeight) {
                                                                        bestBug.add(b);
                                                                        counter = 0;
                                                                }
                                                        }
                                                }
                                                bestBugWeight = getBestWeight();
                                                counter++;
                                                System.out.println("counter: " + counter);
                                        } catch (NullPointerException e) {
                                        }

                                        printResult(bestBug, bugs.size(), false);
                                        if (counter == stoppingCriteria) {
                                                printFinalResult(bestBug);
                                                break;
                                        }
                                } else if (getStatus() == TaskStatus.CANCELED || getStatus() == TaskStatus.ERROR) {
                                        setStatus(TaskStatus.FINISHED);
                                        printResult(bestBug, 0.0 ,true);
                                        break;
                                }

                        }
                        setStatus(TaskStatus.FINISHED);
                }

                private double getBestWeight() {
                        Collections.sort(bestBug, new Comparator<Bug>() {
                                @Override
                                public int compare(Bug o1, Bug o2) {
                                        if (o1.getWeight() < o2.getWeight()) {
                                                return 1;
                                        } else if (o1.getWeight() == o2.getWeight()) {
                                                return 0;
                                        } else {
                                                return -1;
                                        }
                                }
                        });

                        double weight = Double.MAX_VALUE;
                        for (int i = 0; i < bestBug.size(); i++) {
                                if (i < 100) {
                                        if (bestBug.get(i).getWeight() < weight) {
                                                weight = bestBug.get(i).getWeight();
                                        }
                                } else {
                                        bestBug.remove(i);
                                }
                        }
                        return weight;
                }

                private void printFinalResult(List<Bug> bestBug) {
                     Bug b = bestBug.get(0);
                     Graph g = b.getSubNetwork();
                     textArea.setText(textArea.getText() + "\n" + g.toString());                
                }
        }

        public void printResult(List<Bug> bugs, double bugsSize, boolean complete) {
                if (bugs.size() > 0) {
                        int numberPrint = 100;
                        if (numberPrint > bugs.size()) {
                                numberPrint = bugs.size();
                        }

                        String result = "Counter: " + this.counter + " - " + bugsSize + "\n";

                        if (complete) {
                                result = this.textArea.getText() + "\n";
                        }
                        for (int i = 0; i < numberPrint; i++) {
                                Bug bug = bugs.get(i);
                                result += bug.toString();
                        }

                        // System.out.println(result);
                        this.textArea.setText(result);
                }

        }
}
