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
import net.sf.dynamicreports.report.builder.chart.BarChartBuilder;
import net.sf.dynamicreports.report.builder.column.TextColumnBuilder;
import net.sf.dynamicreports.report.builder.style.FontBuilder;
import net.sf.dynamicreports.report.datasource.DRDataSource;
import net.sf.dynamicreports.report.definition.ReportParameters;
import net.sf.dynamicreports.report.definition.chart.DRIChartCustomizer;
import net.sf.dynamicreports.report.exception.DRException;
import net.sf.jasperreports.engine.JRDataSource;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.sbml.jsbml.KineticLaw;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.SpeciesReference;

/**
 *
 * @author scsandra
 */
public class ExchangesReportBar {

    private Dataset data;

    public ExchangesReportBar(Dataset dataset) {
        this.data = dataset;
    }

    public JasperReportBuilder build() throws DRException {
        JasperReportBuilder report = report();
        FontBuilder boldFont = stl.fontArialBold().setFontSize(12);

        TextColumnBuilder<String> reactionColumn = col.column("Reaction", "Reaction", type.stringType());
        TextColumnBuilder<BigDecimal> fluxesColumn = col.column("Real fluxes", "Fluxes", type.bigDecimalType());

        BarChartBuilder barChart = cht.barChart()
            .setTitle("Exchanges Fluxes")
            .setShowValues(true)
            .setTitleFont(boldFont)
            .addCustomizer(new ChartCustomizer())
            .setCategory(reactionColumn)
            .series(cht.serie(fluxesColumn));

        report
            .setTemplate(Templates.reportTemplate)
            .columns(reactionColumn, fluxesColumn)
            .title(Templates.createTitleComponentSmall("Exchange Fluxes Barplot"))
            .summary(              
                
                cmp.horizontalList(barChart).setHeight(300),
                cmp.verticalGap(10),
                cmp.text(this.getInitialInfo()).setStyle(Templates.boldStyle)
                
            )
            // .pageFooter(cmp.line(),
            //   cmp.pageNumber().setStyle(Templates.boldCenteredStyle))
            .setDataSource(createDataSources(this.data.getDocument().getModel()));
        //.show(false);
        return report;
    }

    private class ChartCustomizer implements DRIChartCustomizer, Serializable {

        public void customize(JFreeChart chart, ReportParameters reportParameters) {
            CategoryAxis domainAxis = chart.getCategoryPlot().getDomainAxis();
            domainAxis.setCategoryLabelPositions(CategoryLabelPositions.createUpRotationLabelPositions(Math.PI / 6.0));
        }
    }

    public String getInitialInfo() {
        Model m = this.data.getDocument().getModel();
        String info = "";
        info += "Number of active reactions: " + m.getNumReactions() + "\n";
        info += "Number of active reactions where the flux > abs(0.0001): " + getBigFluxes(m) + "\n";
        return info;
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

    public JRDataSource createDataSources(Model m) {
        Map<String, Double> fluxes = new HashMap<>();

        for (Reaction r : m.getListOfReactions()) {
            if (r.getName().contains("exchange") || r.getName().contains("growth")) {
                KineticLaw law = r.getKineticLaw();
                double flux = law.getLocalParameter("FLUX_VALUE").getValue();
                fluxes.put(r.getName(), flux);

            }
        }

        DRDataSource dataset = new DRDataSource("Reaction", "Fluxes");

        for (String r : fluxes.keySet()) {
            double flux = fluxes.get(r);
            dataset.add(r, new BigDecimal(flux));

        }

        return dataset;

    }
}
