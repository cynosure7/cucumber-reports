package com.github.mkolisnyk.cucumber.reporting;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import com.cedarsoftware.util.io.JsonReader;
import com.github.mkolisnyk.cucumber.reporting.interfaces.ConfigurableReport;
import com.github.mkolisnyk.cucumber.reporting.types.consolidated.ConsolidatedItemInfo;
import com.github.mkolisnyk.cucumber.reporting.types.consolidated.ConsolidatedReportBatch;
import com.github.mkolisnyk.cucumber.reporting.types.consolidated.ConsolidatedReportModel;
import com.github.mkolisnyk.cucumber.reporting.types.enums.CucumberReportLink;
import com.github.mkolisnyk.cucumber.reporting.types.enums.CucumberReportTypes;
import com.github.mkolisnyk.cucumber.reporting.utils.helpers.StringConversionUtils;
import com.github.mkolisnyk.cucumber.runner.runtime.ExtendedRuntimeOptions;

public class CucumberConsolidatedReport extends ConfigurableReport<ConsolidatedReportBatch> {
    public CucumberConsolidatedReport() {
        super();
    }
    public CucumberConsolidatedReport(ExtendedRuntimeOptions extendedOptions) {
        super(extendedOptions);
    }
    protected String getReportBase() throws IOException {
        InputStream is = this.getClass().getResourceAsStream("/consolidated-tmpl.html");
        String result = IOUtils.toString(is);
        return result;
    }
    private String retrieveBody(String content) {
        return content.split("<body>")[1].split("</body>")[0];
    }
    private String amendHtmlHeaders(String content) {
        final int totalHeadingTypes = 6;
        for (int i = totalHeadingTypes; i > 0; i--) {
            content = content.replaceAll("<h" + i + ">", "<h" + (i + 1) + ">");
            content = content.replaceAll("</h" + i + ">", "</h" + (i + 1) + ">");
        }
        return content;
    }
    private String generateLocalLink(String title) {
        String result = title.toLowerCase();
        return result.replaceAll("[^a-z0-9]", "-");
    }
    private String generateTableOfContents(ConsolidatedReportModel model) throws Exception {
        String contents = "<ol>";
        for (ConsolidatedItemInfo item : model.getItems()) {
            contents = contents.concat(
                String.format(Locale.US, "<li><a href=\"#%s\">%s</a></li>",
                        generateLocalLink(item.getTitle()), item.getTitle()));
        }
        contents += "</ol>";
        return contents;
    }
    private String generateConsolidatedReport(ConsolidatedReportModel model) throws Exception {
        String result = getReportBase();
        result = result.replaceAll("__TITLE__", model.getTitle());
        result = result.replaceAll("__REFRESH__", "");
        String reportContent = "";
        if (model.isUseTableOfContents()) {
            reportContent = reportContent.concat(
                    String.format(Locale.US, "<h1>Table of Contents</h1>%s", generateTableOfContents(model)));
        }
        int index = 0;
        reportContent = reportContent.concat("<table class=\"noborder\">");
        for (ConsolidatedItemInfo item : model.getItems()) {
            String formatString = "<td class=\"noborder\">"
                    + "<div class=\"content\"><a id=\"%s\"><h1>%s</h1></a>%s</div></td>";
            if (index % model.getCols() == 0) {
                formatString = "<tr class=\"noborder\" valigh=\"top\">" + formatString;
            }
            String content = FileUtils.readFileToString(new File(item.getPath()));
            content = this.amendHtmlHeaders(content);
            content = this.retrieveBody(content);
            reportContent = reportContent.concat(
                String.format(Locale.US, formatString,
                    generateLocalLink(item.getTitle()), item.getTitle(), content));
            if (index % model.getCols() == model.getCols() - 1) {
                reportContent = reportContent.concat("</tr>");
            }
            index++;
        }
        if (index % model.getCols() != 0) {
            reportContent = reportContent.concat("</tr>");
        }
        reportContent = reportContent.concat("</table>");
        reportContent = StringConversionUtils.replaceHtmlEntitiesWithCodes(reportContent);
        reportContent = reportContent.replaceAll("[$]", "&#36;");
        result = result.replaceAll("__REPORT__", reportContent);
        return result;
    }

    public void executeConsolidatedReport(ConsolidatedReportModel model, boolean toPDF) throws Exception {
        File outFile = new File(
                this.getOutputDirectory() + File.separator + this.getOutputName()
                + "-" + model.getReportSuffix() + ".html");
        FileUtils.writeStringToFile(outFile, generateConsolidatedReport(model));
        if (toPDF) {
            this.exportToPDF(outFile, model.getReportSuffix());
        }
    }
    public void executeConsolidatedReport(ConsolidatedReportModel model) throws Exception {
        executeConsolidatedReport(model, false);
    }
    @Override
    public CucumberReportTypes getReportType() {
        return CucumberReportTypes.CONSOLIDATED_REPORT;
    }
    @Override
    public void validateParameters() {
    }
    @Override
    public CucumberReportLink getReportDocLink() {
        return CucumberReportLink.CONSOLIDATED_URL;
    }
    @Override
    public void execute(ConsolidatedReportBatch batch, boolean toPDF)
            throws Exception {
        for (ConsolidatedReportModel model : batch.getModels()) {
            executeConsolidatedReport(model, toPDF);
        }
    }
    @Override
    public void execute(File config, boolean toPDF) throws Exception {
        ConsolidatedReportBatch model = (ConsolidatedReportBatch) JsonReader.jsonToJava(
                FileUtils.readFileToString(config));
        this.execute(model, toPDF);
    }
    @Override
    public void execute(boolean aggregate, boolean toPDF) throws Exception {
    }
    @Override
    public void execute(ConsolidatedReportBatch batch, boolean aggregate,
            boolean toPDF) throws Exception {
        execute(batch, toPDF);
    }
    @Override
    public void execute(File config, boolean aggregate, boolean toPDF)
            throws Exception {
        execute(config, toPDF);
    }
}
