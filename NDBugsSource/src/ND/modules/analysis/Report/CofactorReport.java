/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ND.modules.analysis.Report;

import ND.data.Dataset;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
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
import org.sbml.jsbml.ListOf;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.SpeciesReference;

/**
 *
 * @author scsandra
 */
public class CofactorReport {

    private Dataset data;

    public CofactorReport(Dataset dataset) {
        this.data = dataset;
    }

    public JasperReportBuilder build() throws DRException {
        JasperReportBuilder report = report();
        FontBuilder boldFont = stl.fontArialBold().setFontSize(12);

        TextColumnBuilder<String> CofactorColumn = col.column("Cofactor", "Cofactor", type.stringType());
        TextColumnBuilder<BigDecimal> CountColumn = col.column("Count", "Count", type.bigDecimalType());

        BarChartBuilder barChart = cht.barChart()
            .setTitle("Cofactors")
            .setHeight(500)
            .addCustomizer(new ChartCustomizer())
            .setTitleFont(boldFont)
            .setShowValues(true)
            .setCategory(CofactorColumn)
            .series(cht.serie(CountColumn));

        report
            .setTemplate(Templates.reportTemplate)
            .title(Templates.createTitleComponentSmall("Cofactors"))
            .columns(CofactorColumn, CountColumn)
            .summary(
                cmp.horizontalList(barChart)
            )
            //.pageFooter(cmp.line(),
            //   cmp.pageNumber().setStyle(Templates.boldCenteredStyle))
            .setDataSource(createDataset(this.data.getDocument().getModel()));
        //.show(false);
        return report;
    }

    private class ChartCustomizer implements DRIChartCustomizer, Serializable {

        public void customize(JFreeChart chart, ReportParameters reportParameters) {
            CategoryAxis domainAxis = chart.getCategoryPlot().getDomainAxis();
            domainAxis.setCategoryLabelPositions(CategoryLabelPositions.createUpRotationLabelPositions(Math.PI / 6.0));
        }
    }

    public JRDataSource createDataset(Model m) {
        DRDataSource dataset = new DRDataSource("Cofactor", "Count");
        Map<String, Double> cofactors = new HashMap<>();
        for (Reaction r : m.getListOfReactions()) {
            KineticLaw law = r.getKineticLaw();
            double flux =  law.getLocalParameter("FLUX_VALUE").getValue();
            
            if (flux > 0) {
                getCofactorAmounts(r.getListOfProducts(), cofactors, Math.abs(flux), "Produced", m);
                getCofactorAmounts(r.getListOfReactants(), cofactors, Math.abs(flux), "Consumed", m);
            } else {
                getCofactorAmounts(r.getListOfProducts(), cofactors, Math.abs(flux), "Consumed", m);
                getCofactorAmounts(r.getListOfReactants(), cofactors, Math.abs(flux), "Produced", m);
            }
        }

        List<String> sortedKeys = new ArrayList(cofactors.keySet());
        Collections.sort(sortedKeys);
        for (String d : sortedKeys) {
            double amount = removeLoops(cofactors.get(d));
            if(amount > 0)
                dataset.add(d, new BigDecimal(amount));
        }

        return dataset;

    }

    private void getCofactorAmounts(ListOf<SpeciesReference> listOfProducts, Map<String, Double> cofactors, double flux, String consumedProduced, Model m) {
        for (SpeciesReference spr : listOfProducts) {
            Species sp = spr.getSpeciesInstance();
            if (isCofactor(sp.getName())) {
                String key = sp.getName() + " - " + m.getCompartment(sp.getCompartment()).getName() + " - " + consumedProduced;
                if (cofactors.containsKey(key)) {
                    Double count = cofactors.get(key) + (spr.getStoichiometry() * flux);
                    cofactors.put(key, count);
                } else {
                    cofactors.put(key, (spr.getStoichiometry() * flux));
                }
            }
        }
    }

    private boolean isCofactor(String name) {
        if(name.contains("port")) return false;
        if (name.startsWith("NADH") || name.startsWith("ATP") || name.startsWith("NADPH")) {
            return true;
        } else {
            return false;
        }
    }

    private double removeLoops(double flux) {
        double correctedFlux = Math.abs(flux);
        while(correctedFlux >= 999.0){
            correctedFlux = correctedFlux - 1000;
        }
        if(correctedFlux < 0) correctedFlux = 0;
        return correctedFlux;
    }
}
