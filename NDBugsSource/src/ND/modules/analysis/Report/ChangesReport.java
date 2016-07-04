/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ND.modules.analysis.Report;

import ND.data.Dataset;
import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import static net.sf.dynamicreports.report.builder.DynamicReports.report;
import static net.sf.dynamicreports.report.builder.DynamicReports.stl;
import net.sf.dynamicreports.report.builder.style.FontBuilder;
import static net.sf.dynamicreports.report.builder.DynamicReports.cmp;
import net.sf.dynamicreports.report.exception.DRException;

/**
 *
 * @author scsandra
 */
public class ChangesReport {

    private Dataset data;

    public ChangesReport(Dataset dataset) {
        this.data = dataset;
    }

    public JasperReportBuilder build() throws DRException {
        JasperReportBuilder report = report();
        FontBuilder boldFont = stl.fontArialBold().setFontSize(12);
        String info = data.getInfo().getText();
        report
            .setTemplate(Templates.reportTemplate)      
            .title(Templates.createTitleComponentSmall("Changes In the model"))
            .summary(
                cmp.text(info)
            );

        return report;
    }

}
