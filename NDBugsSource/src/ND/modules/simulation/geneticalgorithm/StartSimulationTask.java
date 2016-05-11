/*
 * Copyright 2010 - 2012 VTT Biotechnology
 * This file is part of ALVS.
 *
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
package ND.modules.simulation.geneticalgorithm;

import ND.data.Dataset;
import ND.main.NDCore;
import ND.modules.simulation.antNoGraph.ReactionFA;
import ND.modules.simulation.geneticalgorithm.tools.Bug;
import ND.modules.simulation.geneticalgorithm.tools.Result;
import ND.modules.simulation.geneticalgorithm.tools.World;
import ND.parameters.SimpleParameterSet;
import ND.taskcontrol.AbstractTask;
import ND.taskcontrol.TaskStatus;
import com.csvreader.CsvReader;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JInternalFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 *
 * @author scsandra
 */
public class StartSimulationTask extends AbstractTask {

    private final Dataset training;
    private sinkThread thread;
    private final int bugLife;
    private int maxBugs = 1000;
    private JTextArea textArea;
    private final List<Result> results;
    private final String objective;
    int[] counter;
    boolean stoppingCriteria;
    private final File reactionFile;

    public StartSimulationTask(Dataset dataset, SimpleParameterSet parameters) {
        this.stoppingCriteria = true;
        training = dataset;
        this.objective = parameters.getParameter(StartSimulationParameters.objective).getValue();
        this.bugLife = parameters.getParameter(StartSimulationParameters.bugLife).getValue();
        this.reactionFile = parameters.getParameter(StartSimulationParameters.reactions).getValue();
        this.results = new ArrayList<Result>();
    }

    @Override
    public String getTaskDescription() {
        return "Start simulation... ";
    }

    @Override
    public double getFinishedPercentage() {
        return 0.0f;
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
            frame2.setSize(new Dimension(800, 700));

            textArea = new JTextArea("");
            textArea.setSize(new Dimension(700, 700));
            JPanel mainPanel = new JPanel(new BorderLayout());
            mainPanel.add(textArea, BorderLayout.CENTER);

            JScrollPane panel = new JScrollPane(mainPanel);

            frame2.add(panel);

            NDCore.getDesktop().addInternalFrame(frame2);

            System.out.println("starting Cicles");
            this.startCicle();

            setStatus(TaskStatus.FINISHED);
        } catch (Exception e) {
            System.out.println(e.toString());
            setStatus(TaskStatus.ERROR);
        }
    }

    private void startCicle() {
        List<String> reactionIds = ReadFile();
        World world = new World(training, reactionIds, this.bugLife, textArea, this.maxBugs, this.objective);
        thread = new sinkThread(world);
        thread.start();
    }

    private List<String> ReadFile() {
        try {
            CsvReader lines;
            List<String> reactionIds = new ArrayList<>();
            lines = new CsvReader(new FileReader(this.reactionFile));
            lines.getHeaders();
            while (lines.readRecord()) {
                String[] reaction = lines.getValues();
                reactionIds.addAll(Arrays.asList(reaction));
            }
            this.maxBugs = reactionIds.size() + 100;
            return reactionIds;
        } catch (FileNotFoundException ex) {
            
        } catch (IOException ex) {
        }
        return null;
    }

    public class sinkThread extends Thread {

        World world;

        public sinkThread(World world) {
            this.world = world;
        }

        @Override
        public void run() {
            while (stoppingCriteria) {
                // for (int i = 0; i < 1; i++) {
                world.cicle();
                //}

                printResult(world.getBugs());

            }
        }
    }

    public void printResult(List<Bug> bugs) {

        FileWriter fw = null;
        
            Comparator<Result> c = new Comparator<Result>() {
                public int compare(Result o1, Result o2) {
                    return Double.compare(o1.getScore(), o2.getScore());
                }
            };
            Comparator<Result> c2 = new Comparator<Result>() {
                public int compare(Result o1, Result o2) {
                    return Double.compare(o1.getValues().size(), o2.getValues().size());
                }
            };
            Comparator<Bug> c3 = new Comparator<Bug>() {
                public int compare(Bug o1, Bug o2) {
                    return Double.compare(o1.getScore(), o2.getScore());
                }
            };
            int count = 0;
            try {
                Collections.sort(bugs, c3);
                Collections.reverse(bugs);
                
            } catch (Exception e) {
                e.printStackTrace();
            }
            for (Bug bug : bugs) {
                if (count < 300) {
                    Result result = new Result(bug.getScore());
                    for (ReactionFA row : bug.getRows()) {
                        result.addValue(String.valueOf(row.getId()));
                    }
                    result.score = bug.getScore();
                    
                    boolean isIt = false;
                    for (Result r : this.results) {
                        if (r.isIt(result.getValues())) {
                            r.count();
                            isIt = true;
                        }
                    }
                    if (!isIt) {
                        this.results.add(result);
                    }
                }
                count++;
                
            }
            try {
                Collections.sort(results, c2);
                Collections.reverse(results);
                Collections.sort(results, c);
                Collections.reverse(results);
            } catch (Exception e) {
                e.printStackTrace();
            }
            int contbug = 0;
            String result = "";
            for (Result r : results) {
            result += r.toString();
            contbug++;
            if (contbug > 500) {
                break;
            }
            }
            this.textArea.setText(result);
         try {
            fw = new FileWriter(new File("/home/scsandra/Documents/CellFactory2015/LifGA/resultMalateLast.txt"));
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(result);
            bw.close();

        } catch (IOException ex) {
            
        } finally {
            try {
                fw.close();
            } catch (IOException ex) {
                
            }
        }
    }

}
