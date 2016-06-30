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
import org.jfree.chart.renderer.category.BarRenderer;
import org.sbml.jsbml.KineticLaw;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.Reaction;

/**
 *
 * @author scsandra
 */
public class ImportantFluxesReport {

    private Dataset data;

    public ImportantFluxesReport(Dataset dataset) {
        this.data = dataset;
    }

    public JasperReportBuilder build() throws DRException {
        JasperReportBuilder report = report();
        FontBuilder boldFont = stl.fontArialBold().setFontSize(12);

        TextColumnBuilder<String> reactionColumn = col.column("Reaction", "Reaction", type.stringType());
        TextColumnBuilder<BigDecimal> fluxesColumn = col.column("Fluxes", "Flux", type.bigDecimalType());

        BarChartBuilder barChart = cht.barChart()
            .setTitle("Important Fluxes")
            .setHeight(500)
            .addCustomizer(new ChartCustomizer())
            .setTitleFont(boldFont)
            .setShowValues(true)
            .setCategory(reactionColumn)
            .series(cht.serie(fluxesColumn));

        report
            .setTemplate(Templates.reportTemplate)
            .title(Templates.createTitleComponentSmall("Higher fluxes"))
            .columns(reactionColumn, fluxesColumn)
            .summary(
                cmp.horizontalList(barChart)
            )
            //.pageFooter(cmp.line(),
             //   cmp.pageNumber().setStyle(Templates.boldCenteredStyle))
            .setDataSource(createBarDataset(this.data.getDocument().getModel()));
            //.show(false);
        return report;
    }

    private class ChartCustomizer implements DRIChartCustomizer, Serializable {

        public void customize(JFreeChart chart, ReportParameters reportParameters) {
            CategoryAxis domainAxis = chart.getCategoryPlot().getDomainAxis();
            domainAxis.setCategoryLabelPositions(CategoryLabelPositions.createUpRotationLabelPositions(Math.PI / 6.0));
        }
    }

    public JRDataSource createBarDataset(Model m) {
        Map<String, Double> Fdata = new HashMap<>();
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
                Fdata.put(r.getName(), realFlux);
            }

        }
        DRDataSource dataset = new DRDataSource("Reaction", "Flux");
        for (String d : Fdata.keySet()) {
            dataset.add(d, new BigDecimal(Fdata.get(d)));
        }

        return dataset;

    }

}
