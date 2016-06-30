/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ND.modules.analysis.Report;

import ND.data.Dataset;
import ND.main.NDCore;
import ND.modules.simulation.FBAreal.FBA;
import ND.modules.simulation.antNoGraph.ReactionFA;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import static net.sf.dynamicreports.report.builder.DynamicReports.cht;
import static net.sf.dynamicreports.report.builder.DynamicReports.cmp;
import static net.sf.dynamicreports.report.builder.DynamicReports.col;
import static net.sf.dynamicreports.report.builder.DynamicReports.report;
import static net.sf.dynamicreports.report.builder.DynamicReports.stl;
import static net.sf.dynamicreports.report.builder.DynamicReports.type;
import net.sf.dynamicreports.report.builder.chart.XyChartSerieBuilder;
import net.sf.dynamicreports.report.builder.chart.XyLineChartBuilder;
import net.sf.dynamicreports.report.builder.column.TextColumnBuilder;
import net.sf.dynamicreports.report.builder.style.FontBuilder;
import net.sf.dynamicreports.report.constant.SplitType;
import net.sf.dynamicreports.report.datasource.DRDataSource;
import net.sf.dynamicreports.report.exception.DRException;
import net.sf.jasperreports.engine.JRDataSource;
import org.sbml.jsbml.KineticLaw;
import org.sbml.jsbml.LocalParameter;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.SpeciesReference;

/**
 *
 * @author scsandra
 */
public class OxygenReport {

    private Dataset data;
    private HashMap<String, ReactionFA> reactions;
    private ReactionFA oxygen;
    Map<String, Double> Fdata;

    public OxygenReport(Dataset dataset) {
        this.data = dataset;
    }

    public JasperReportBuilder build() throws DRException {
        JRDataSource source = this.createDataSource(this.data.getDocument().getModel());

        if (source == null) {
            return null;
        }

        JasperReportBuilder report = report();
        FontBuilder boldFont = stl.fontArialBold().setFontSize(12);
        List<TextColumnBuilder> columns = new ArrayList<>();

        TextColumnBuilder<Double> XColumn = col.column(oxygen.getName(), oxygen.getName(), type.doubleType());

        for (String data : Fdata.keySet()) {
            // System.out.println(data);
            if (!data.equals(this.oxygen.getName())) {
                TextColumnBuilder<Double> reactionColumn = col.column(data, data, type.doubleType());
                columns.add(reactionColumn);
            }
        }
        List<XyLineChartBuilder> charts = new ArrayList<>();
        List<XyLineChartBuilder> chartsRelative = new ArrayList<>();
        List<XyChartSerieBuilder> series = new ArrayList<>();
        List<XyChartSerieBuilder> seriesRelative = new ArrayList<>();

        for (TextColumnBuilder column : columns) {
            if (column.getName().contains("Relative")) {
                seriesRelative.add(cht.xySerie(column));
            } else {
                series.add(cht.xySerie(column));
            }
        }

        XyLineChartBuilder lineChart = cht.xyLineChart()
            .setTitle("Oxygen variations (All exchanges)")
            .setTitleFont(boldFont)
            .setHeight(650)
            .setXValue(XColumn)
            .series(series.toArray(new XyChartSerieBuilder[series.size()]))
            .setShowValues(true)
            .setXAxisFormat(cht.axisFormat().setLabel("Oxygen level"))
            .setYAxisFormat(cht.axisFormat().setLabel("Exchanges"));
        charts.add(lineChart);

        XyLineChartBuilder lineChart2 = cht.xyLineChart()
            .setTitle("Oxygen variations (All exchanges) - Relative to carbon source")
            .setTitleFont(boldFont)
            .setHeight(650)
            .setXValue(XColumn)
            .series(seriesRelative.toArray(new XyChartSerieBuilder[seriesRelative.size()]))
            .setShowValues(true)
            .setXAxisFormat(cht.axisFormat().setLabel("Oxygen level"))
            .setYAxisFormat(cht.axisFormat().setLabel("Exchanges"));
        charts.add(lineChart2);

        for (TextColumnBuilder column : columns) {
            XyLineChartBuilder lineChart3 = cht.xyLineChart()
                .setTitle("Oxygen variations (" + column.getName() + ")")
                .setTitleFont(boldFont)
                .setHeight(300)
                .setXValue(XColumn)
                .series(cht.xySerie(column))
                .setShowValues(true)
                .setXAxisFormat(cht.axisFormat().setLabel("Oxygen level"))
                .setYAxisFormat(cht.axisFormat().setLabel(column.getName()));

            if (column.getName().contains("Relative")) {
                chartsRelative.add(lineChart3);
            } else {
                charts.add(lineChart3);
            }

        }

        report
            .setTemplate(Templates.reportTemplate)
            .setSummarySplitType(SplitType.IMMEDIATE)
            .title(Templates.createTitleComponentSmall("Changes in the fluxes due to the Oxygen levels"))
            .columns(columns.toArray(new TextColumnBuilder[columns.size()]))
            .summary(
                cmp.verticalList(charts.toArray(new XyLineChartBuilder[charts.size()])),
                cmp.verticalList(chartsRelative.toArray(new XyLineChartBuilder[chartsRelative.size()])),
                cmp.verticalGap(10)
            )
            .setDataSource(source);
        return report;
    }

    private JRDataSource createDataSource(Model m) {
        Fdata = new HashMap<>();
        Map<String, String> reactionIds = new HashMap<>();

        for (Reaction r : m.getListOfReactions()) {
            if (r.getName().contains("exchange") || r.getName().contains("growth")) {
                KineticLaw law = r.getKineticLaw();
                double flux = law.getLocalParameter("FLUX_VALUE").getValue();
                Fdata.put(r.getName(), flux);
                for (SpeciesReference c : r.getListOfProducts()) {
                    Species sp = c.getSpeciesInstance();

                    try {
                        String notes = sp.getNotesString();
                        String carbonsString = notes.substring(notes.indexOf("CARBONS:") + 8, notes.lastIndexOf("</p>"));

                        if (Double.valueOf(carbonsString) > 0) {
                            Fdata.put(r.getName() + "-Relative", flux);
                        }
                    } catch (Exception e) {
                    }
                }

                reactionIds.put(r.getName(), r.getId());
            }
        }

        String parent = this.data.getParent();
        Dataset parentDataset = NDCore.getDesktop().getParentDataset(parent);

        if (parentDataset != null) {
            DRDataSource dataSource = new DRDataSource((String[]) Fdata.keySet().toArray(new String[Fdata.keySet().size()]));

            Model model = parentDataset.getDocument().getModel();
            this.createReactions(model);
            if (oxygen != null) {
                this.setFluxes(model, reactionIds, dataSource, 0.0);
                this.setFluxes(model, reactionIds, dataSource, -0.05);
                this.setFluxes(model, reactionIds, dataSource, -0.4);
                this.setFluxes(model, reactionIds, dataSource, -0.8);
                this.setFluxes(model, reactionIds, dataSource, -1.6);
                this.setFluxes(model, reactionIds, dataSource, -2);
                this.setFluxes(model, reactionIds, dataSource, -10);

            }
            return dataSource;
        }
        return null;

    }

    private void setFluxes(Model model, Map<String, String> reactionIds, DRDataSource dataSource, double oxygenBound) {
        Map<String, Double> carbons = new HashMap<>();
        double totalInCarbon = 0;

        FBA fba = new FBA();
        oxygen.setBounds(oxygenBound, Double.POSITIVE_INFINITY);

        fba.setModel(this.reactions, model);
        try {
            Map<String, Double> soln = fba.run();

            for (String r : Fdata.keySet()) {
                Reaction reaction = model.getReaction(reactionIds.get(r));
                if (reaction != null) {
                    for (SpeciesReference c : reaction.getListOfProducts()) {
                        Species sp = c.getSpeciesInstance();

                        try {
                            String notes = sp.getNotesString();
                            String carbonsString = notes.substring(notes.indexOf("CARBONS:") + 8, notes.lastIndexOf("</p>"));

                            if (Double.valueOf(carbonsString) > 0) {
                                carbons.put(reaction.getName(), Double.valueOf(carbonsString));

                                if (soln.get(reactionIds.get(r)) < -0.00001) {
                                    totalInCarbon += Double.valueOf(carbonsString);
                                }
                            }
                        } catch (Exception e) {
                        }
                    }
                }
            }

            System.out.println(totalInCarbon);
            for (String r : Fdata.keySet()) {
                if (r.equals(this.oxygen.getName())) {
                    Fdata.put(r, Math.abs(soln.get(reactionIds.get(r))));
                } else {
                    Fdata.put(r, soln.get(reactionIds.get(r)));
                    if (carbons.containsKey(r)) {
                        Fdata.put(r + "-Relative", (carbons.get(r) / totalInCarbon) * Math.abs(soln.get(reactionIds.get(r))));
                    }

                }

            }

            if (fba.getMaxObj() > 0) {
                dataSource.add(Fdata.values().toArray());
                // System.out.println(Arrays.toString(Fdata.values().toArray()));
            }
        } catch (Exception ex) {
            System.out.println(ex);
        }

    }

    private void createReactions(Model m) {
        this.reactions = new HashMap<>();

        for (Reaction r : m.getListOfReactions()) {
            ReactionFA reaction = new ReactionFA(r.getId(), r.getName());
            if (r.getName().contains("oxygen") && r.getName().contains("exchange")) {
                this.oxygen = reaction;
            }
            try {
                KineticLaw law = r.getKineticLaw();
                LocalParameter lbound = law.getLocalParameter("LOWER_BOUND");
                LocalParameter ubound = law.getLocalParameter("UPPER_BOUND");
                LocalParameter objective = law.getLocalParameter("OBJECTIVE_COEFFICIENT");
                reaction.setObjective(objective.getValue());
                reaction.setBounds(lbound.getValue(), ubound.getValue());
            } catch (Exception ex) {
                reaction.setBounds(-1000, 1000);
            }

            for (SpeciesReference s : r.getListOfReactants()) {

                Species sp = s.getSpeciesInstance();
                reaction.addReactant(sp.getId(), sp.getName(), s.getStoichiometry());
            }

            for (SpeciesReference s : r.getListOfProducts()) {
                Species sp = s.getSpeciesInstance();
                reaction.addProduct(sp.getId(), sp.getName(), s.getStoichiometry());
            }
            this.reactions.put(r.getId(), reaction);
        }
    }
}
