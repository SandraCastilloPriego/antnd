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
package ND.modules.analysis.Report;

import ND.data.Dataset;
import ND.main.NDCore;
import ND.taskcontrol.AbstractTask;
import ND.taskcontrol.TaskStatus;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.BoxLayout;
import javax.swing.JInternalFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.labels.PieSectionLabelGenerator;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot3D;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.general.PieDataset;
import org.jfree.util.Rotation;
import org.sbml.jsbml.KineticLaw;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.SpeciesReference;

/**
 *
 * @author scsandra
 */
public class ReportFBATask extends AbstractTask {

    private Dataset data;
    private double finishedPercentage = 0.0f;

    public ReportFBATask(Dataset data) {
        this.data = data;
    }

    @Override
    public String getTaskDescription() {
        return "Writing report... ";
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
        setStatus(TaskStatus.PROCESSING);
        Model m = data.getDocument().getModel();
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
        panel.setBackground(Color.white);

        String info = "";
        info += "Number of active reactions: " + m.getNumReactions() + "\n";
        info += "Number of active reactions where the flux > abs(0.0001): " + getBigFluxes(m) + "\n";
        JTextArea area = new JTextArea(info);
        area.setEditable(false);
        panel.add(area);
        

        List<PieDataset> datasets = createPieDataset(m);
        JFreeChart exchangesPos = createPieChart(datasets.get(0), "Exchanges out");
        JFreeChart exchangesNeg = createPieChart(datasets.get(1), "Exchanges in");
        JPanel chartpanel = new JPanel();
        chartpanel.add(new ChartPanel(exchangesNeg));
        chartpanel.add(new ChartPanel(exchangesPos));
        chartpanel.setBackground(Color.white);
        panel.add(chartpanel);
        
        
        
        CategoryDataset dataset = createBarExchangeDataset(m);
        JFreeChart fluxesChart = createBarChart(dataset, "Exchange reactions");
        JPanel fPanel = new JPanel();
        fPanel.add(new ChartPanel(fluxesChart));
        fPanel.setPreferredSize(new Dimension(500, 500));
        fPanel.setBackground(Color.white);
        panel.add(fPanel);

        dataset = createBarDataset(m);
        fluxesChart = createBarChart(dataset, "Important fluxes");
        panel.add(new ChartPanel(fluxesChart));

        JInternalFrame frameTable = new JInternalFrame("Report", true, true, true, true);
        JScrollPane scrollPanel = new JScrollPane(panel);
        frameTable.setSize(new Dimension(700, 500));
        frameTable.add(scrollPanel);

        NDCore.getDesktop().addInternalFrame(frameTable);
        frameTable.setVisible(true);
        setStatus(TaskStatus.FINISHED);
    }

    public static List<PieDataset> createPieDataset(Model m) {
        List<PieDataset> datasets = new ArrayList<>();
        Map<String, Double> fluxes = new HashMap<>();
        Map<String, Double> carbons = new HashMap<>();
        double totalInCarbon = 0;

        for (Reaction r : m.getListOfReactions()) {
            if (r.getName().contains("exchange") || r.getName().contains("growth")) {
                KineticLaw law = r.getKineticLaw();
                double flux = law.getLocalParameter("FLUX_VALUE").getValue();

                for (SpeciesReference c : r.getListOfProducts()) {
                    Species sp = c.getSpeciesInstance();
                    String notes = sp.getNotesString();
                    try {

                        // System.out.println(notes);
                        String carbonsString = notes.substring(notes.indexOf("CARBONS:") + 8, notes.lastIndexOf("</p>"));
                        carbons.put(r.getName(), Double.valueOf(carbonsString));
                        // System.out.println(carbonsString);
                        if (Double.valueOf(carbonsString) > 0.00001) {
                            fluxes.put(r.getName(), flux);
                        }
                        if (flux < -0.00001) {
                            totalInCarbon += Double.valueOf(carbonsString);
                        } else {
                            //totalOutCarbon += Double.valueOf(carbonsString);
                        }
                    } catch (Exception e) {
                    }
                }
            }
        }
        DefaultPieDataset datasetOut = new DefaultPieDataset();
        DefaultPieDataset datasetIn = new DefaultPieDataset();

        System.out.println(totalInCarbon);
        if (totalInCarbon == 0) {
            for (String r : fluxes.keySet()) {
                double flux = fluxes.get(r);
                if (flux < 0) {
                    datasetIn.setValue(r, Math.abs(flux));
                } else {
                    datasetOut.setValue(r, flux);
                }
            }
        } else {
            for (String r : fluxes.keySet()) {
                double flux = fluxes.get(r);
                System.out.println((carbons.get(r)) / totalInCarbon);
                if (flux < -0.00001) {
                    datasetIn.setValue(r, (carbons.get(r) / totalInCarbon) * Math.abs(flux));
                } else {
                    datasetOut.setValue(r, (carbons.get(r) / totalInCarbon) * flux);
                }
            }

        }

        datasets.add(datasetOut);
        datasets.add(datasetIn);

        return datasets;

    }

    public static CategoryDataset createBarExchangeDataset(Model m) {
        final DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (Reaction r : m.getListOfReactions()) {
            if (r.getName().contains("exchange") || r.getName().contains("growth")) {
                KineticLaw law = r.getKineticLaw();
                double flux = law.getLocalParameter("FLUX_VALUE").getValue();
                if (flux < 0) {
                    dataset.addValue(flux, "Exchanges In", r.getName());
                } else {
                    dataset.addValue(flux, "Exchanges Out", r.getName());
                }
            }

        }
        return dataset;
    }

    public static CategoryDataset createBarDataset(Model m) {
        Map<String, Double> data = new HashMap<>();
        for (Reaction r : m.getListOfReactions()) {
            KineticLaw law = r.getKineticLaw();
            double flux = law.getLocalParameter("FLUX_VALUE").getValue();
            double realFlux = 0;
            if (flux > 500) {
                realFlux = Math.abs(1000 - flux);
            } else if (flux < -500) {
                realFlux = (1000 - Math.abs(flux)) * -1;
            } else {
                realFlux = flux;
            }
            if (realFlux > 1 || realFlux < -1) {
                data.put(r.getName(), realFlux);
            }

        }
        final DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (String d : data.keySet()) {
            dataset.addValue(data.get(d), "Fluxes", d);
        }

        return dataset;

    }

    public static JFreeChart createPieChart(PieDataset dataset, String title) {
        JFreeChart chart = ChartFactory.createPieChart3D(
            title, // chart title 
            dataset, // data    
            true, // include legend   
            true,
            false);

        final PiePlot3D plot = (PiePlot3D) chart.getPlot();

        PieSectionLabelGenerator gen = new StandardPieSectionLabelGenerator(
            "{0}: {2}", new DecimalFormat("0.000"), new DecimalFormat("0%"));
        plot.setLabelGenerator(gen);
        plot.setStartAngle(290);
        plot.setDirection(Rotation.CLOCKWISE);
        plot.setForegroundAlpha(0.5f);
        plot.setBackgroundPaint(new Color(0, 0, 0, 0));
        return chart;
    }

    public static JFreeChart createBarChart(CategoryDataset dataset, String title) {
        JFreeChart barChart = ChartFactory.createBarChart3D(
            title,
            "Reactions",
            "Fluxes",
            dataset,
            PlotOrientation.VERTICAL,
            true, true, false);

        CategoryPlot plot = barChart.getCategoryPlot();
        CategoryAxis axis = (CategoryAxis) plot.getDomainAxis();
        axis.setCategoryLabelPositions(CategoryLabelPositions.DOWN_45);
        plot.setBackgroundPaint(new Color(0, 0, 0, 0));
        return barChart;
    }

    private String getBigFluxes(Model m) {
        int i = 0;
        for (Reaction r : m.getListOfReactions()) {
            KineticLaw law = r.getKineticLaw();
            double flux = law.getLocalParameter("FLUX_VALUE").getValue();
            if (Math.abs(flux) >= 0.0001) {
                i++;
            }
        }
        return String.valueOf(i);
    }

}
