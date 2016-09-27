package com.github.mkolisnyk.cucumber.reporting;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import com.github.mkolisnyk.cucumber.reporting.types.enums.CucumberReportLink;
import com.github.mkolisnyk.cucumber.reporting.types.enums.CucumberReportTypes;
import com.github.mkolisnyk.cucumber.reporting.types.result.CucumberFeatureResult;
import com.github.mkolisnyk.cucumber.reporting.utils.helpers.StringConversionUtils;
import com.github.mkolisnyk.cucumber.runner.runtime.ExtendedRuntimeOptions;

public class CucumberFeatureOverview extends CucumberResultsOverview {

    public CucumberFeatureOverview() {
        super();
    }

    public CucumberFeatureOverview(ExtendedRuntimeOptions extendedOptions) {
        super(extendedOptions);
    }

    @Override
    public CucumberReportLink getReportDocLink() {
        return CucumberReportLink.FEATURE_OVERVIEW_URL;
    }

    @Override
    public CucumberReportTypes getReportType() {
        return CucumberReportTypes.FEATURE_OVERVIEW;
    }

    //@Override
    protected String getReportBase() throws IOException {
        InputStream is = this.getClass().getResourceAsStream("/feature-overview-tmpl-2.html");
        String result = IOUtils.toString(is);
        return result;
    }

    private String getStatusLetter(double rate) {
        String scale = "ABCDEF";
        int index = (int) ((1. - rate) * (double) (scale.length() - 1));
        return scale.substring(index, index + 1);
    }

    private String getFeatureStatusLetter(CucumberFeatureResult feature) {
        feature.valuate();
        double rate = (double) feature.getPassed()
                / (double) (feature.getPassed() + feature.getFailed()
                        + feature.getSkipped() + feature.getUndefined());
        return getStatusLetter(rate);
    }

    private double getOverallRate(CucumberFeatureResult[] results) {
        int[][] statuses = this.getStatuses(results);
        int[] scenarioStatuses = statuses[1];
        return (double) scenarioStatuses[0]
                / (double) (scenarioStatuses[0] + scenarioStatuses[1] + scenarioStatuses[2]);
    }

    private String getGradeString(CucumberFeatureResult feature) {
        String gradeString = "A&nbsp;B&nbsp;C&nbsp;D&nbsp;E&nbsp;F";
        gradeString = gradeString.replaceAll(getFeatureStatusLetter(feature), "<font size=\"5\"><b>$0</b></font>");
        return gradeString;
    }
    public String generateFeaturesTable(CucumberFeatureResult[] features) {
        String tableContent = "";
        for (CucumberFeatureResult feature : features) {
            tableContent = tableContent.concat(
                String.format(
                    Locale.US,
                    "<tr><td><b>&nbsp;&nbsp;&nbsp;&nbsp;%s</b></td>"
                    + "<td><center><font size=\"3\">%s</font></center></td></tr>",
                    feature.getName(), getGradeString(feature)
                )
            );
        }
        return tableContent;
    }

    public String generateFeatureOverviewChart(CucumberFeatureResult[] features) throws Exception {
        final double maxRate = 100.;
        String content = this.getReportBase();
        content = content.replaceAll("__TITLE__", "Features Overview Chart");
        double rate = getOverallRate(features);
        String grade = getStatusLetter(rate);
        content = content.replaceAll("__GRADE__", grade);
        content = content.replaceAll("__RATE__", String.format(Locale.US, "%.0f%%", rate * maxRate));
        String featuresTable = generateFeaturesTable(features);
        content = content.replaceAll("__FEATURES__", featuresTable);
        content = StringConversionUtils.replaceHtmlEntitiesWithCodes(content);
        content = content.replaceAll("[$]", "&#36;");
        return content;
    }

    @Override
    public void execute(boolean toPDF) throws Exception {
        // TODO Auto-generated method stub
        execute(true, toPDF);
    }

    @Override
    public void execute() throws Exception {
        // TODO Auto-generated method stub
        execute(false);
    }

    @Override
    public void execute(boolean aggregate, boolean toPDF) throws Exception {
        validateParameters();
        CucumberFeatureResult[] features = readFileContent(aggregate);
        File outFile = new File(
                this.getOutputDirectory() + File.separator + this.getOutputName()
                + "-feature-overview-chart.html");
        FileUtils.writeStringToFile(outFile, generateFeatureOverviewChart(features));
        if (toPDF) {
            this.exportToPDF(outFile, "feature-overview-chart");
        }
    }
}
