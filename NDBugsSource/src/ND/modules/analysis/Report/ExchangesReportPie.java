/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ND.modules.analysis.Report;

import ND.data.Dataset;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import static net.sf.dynamicreports.report.builder.DynamicReports.cht;
import static net.sf.dynamicreports.report.builder.DynamicReports.cmp;
import static net.sf.dynamicreports.report.builder.DynamicReports.col;
import static net.sf.dynamicreports.report.builder.DynamicReports.report;
import static net.sf.dynamicreports.report.builder.DynamicReports.stl;
import static net.sf.dynamicreports.report.builder.DynamicReports.type;
import net.sf.dynamicreports.report.builder.chart.Pie3DChartBuilder;
import net.sf.dynamicreports.report.builder.column.TextColumnBuilder;
import net.sf.dynamicreports.report.builder.style.FontBuilder;
import net.sf.dynamicreports.report.datasource.DRDataSource;
import net.sf.dynamicreports.report.definition.ReportParameters;
import net.sf.dynamicreports.report.definition.chart.DRIChartCustomizer;
import net.sf.dynamicreports.report.exception.DRException;
import net.sf.jasperreports.engine.JRDataSource;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PiePlot3D;
import org.sbml.jsbml.KineticLaw;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.SpeciesReference;

/**
 *
 * @author scsandra
 */
public class ExchangesReportPie {

    private Dataset data;

    public ExchangesReportPie(Dataset dataset) {
        this.data = dataset;
    }

    public JasperReportBuilder build() throws DRException {
        JasperReportBuilder report = report();
        FontBuilder boldFont = stl.fontArialBold().setFontSize(12);

        TextColumnBuilder<String> reactionColumn = col.column("Reaction", "Reaction", type.stringType());
        TextColumnBuilder<BigDecimal> fluxOutColumn = col.column("Flux out", "Flux", type.bigDecimalType());
        TextColumnBuilder<BigDecimal> fluxInColumn = col.column("Flux in", "Flux2", type.bigDecimalType());

        Pie3DChartBuilder itemChart = cht.pie3DChart()
            .setTitle("Exchanges Out")
            .setShowValues(true)
            .setTitleFont(boldFont)
            .setKey(reactionColumn)
            .addCustomizer(new ChartCustomizer())
            .addSerie(
                cht.serie(fluxOutColumn));

        Pie3DChartBuilder itemChart2 = cht.pie3DChart()
            .setTitle("Exchanges In")
            .setShowValues(true)
            .setTitleFont(boldFont)
            .setKey(reactionColumn)
            .addCustomizer(new ChartCustomizer())
            .addSerie(
                cht.serie(fluxInColumn));

        
        
        report
            .setTemplate(Templates.reportTemplate)
            .title(Templates.createTitleComponentSmall("Exchange Fluxes Pie plot relative to carbon"))
            .columns(reactionColumn, fluxInColumn, fluxOutColumn)
            .summary(                
                cmp.horizontalList(itemChart2, itemChart)
            )
           // .pageFooter(cmp.line(),
             //   cmp.pageNumber().setStyle(Templates.boldCenteredStyle))
            .setDataSource(createDataSources(this.data.getDocument().getModel()));
            //.show(false);
        return report;
    }

   
    private class ChartCustomizer implements DRIChartCustomizer, Serializable {
      public void customize(JFreeChart chart, ReportParameters reportParameters) {
         PiePlot3D plot = (PiePlot3D)chart.getPlot();
         plot.setIgnoreZeroValues(true); 
      }
   }

    public JRDataSource createDataSources(Model m) {
        Map<String, Double> fluxes = new HashMap<>();
        Map<String, Double> carbons = new HashMap<>();
        double totalInCarbon = 0;

        for (Reaction r : m.getListOfReactions()) {
            if (r.getName().contains("exchange") || r.getName().contains("growth")) {
                KineticLaw law = r.getKineticLaw();
                double flux = law.getLocalParameter("FLUX_VALUE").getValue();

                for (SpeciesReference c : r.getListOfProducts()) {
                    Species sp = c.getSpeciesInstance();

                    try {
                        String notes = sp.getNotesString();
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

        DRDataSource dataset = new DRDataSource("Reaction", "Flux", "Flux2", "Fluxes");

      //  System.out.println(totalInCarbon);
        if (totalInCarbon == 0) {
            for (String r : fluxes.keySet()) {
                double flux = fluxes.get(r);
                if (flux <= 0) {
                    dataset.add(r, new BigDecimal(0.0), new BigDecimal(Math.abs(flux)));
                } else {
                    dataset.add(r, new BigDecimal(flux), new BigDecimal(0.0));
                }
            }
        } else {
            for (String r : fluxes.keySet()) {
                double flux = fluxes.get(r);
              //  System.out.println((carbons.get(r)) / totalInCarbon);
                if (flux <= 0) {
                    dataset.add(r, new BigDecimal(0.0), new BigDecimal((carbons.get(r) / totalInCarbon) * Math.abs(flux)));
                } else {
                    dataset.add(r, new BigDecimal((carbons.get(r) / totalInCarbon) * flux), new BigDecimal(0.0));
                }
            }

        }

        return dataset;

    }

   
}
