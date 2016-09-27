package com.github.mkolisnyk.cucumber.reporting;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Locale;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;

import com.github.mkolisnyk.cucumber.reporting.interfaces.AggregatedReport;
import com.github.mkolisnyk.cucumber.reporting.types.enums.CucumberReportLink;
import com.github.mkolisnyk.cucumber.reporting.types.enums.CucumberReportTypes;
import com.github.mkolisnyk.cucumber.reporting.types.result.CucumberBeforeAfterResult;
import com.github.mkolisnyk.cucumber.reporting.types.result.CucumberEmbedding;
import com.github.mkolisnyk.cucumber.reporting.types.result.CucumberFeatureResult;
import com.github.mkolisnyk.cucumber.reporting.types.result.CucumberScenarioResult;
import com.github.mkolisnyk.cucumber.reporting.types.result.CucumberStepResult;
import com.github.mkolisnyk.cucumber.reporting.utils.helpers.StringConversionUtils;
import com.github.mkolisnyk.cucumber.runner.runtime.ExtendedRuntimeOptions;

/**
 * @author Myk Kolisnyk
 */
public class CucumberDetailedResults extends AggregatedReport {
    public CucumberDetailedResults() {
        super();
    }

    public CucumberDetailedResults(ExtendedRuntimeOptions extendedOptions) {
        super(extendedOptions);
        this.setScreenShotLocation(extendedOptions.getScreenShotLocation());
        this.setScreenShotWidth(extendedOptions.getScreenShotSize());
    }

    private String screenShotLocation;
    private String screenShotWidth;

    /**
     * @return the screenShotLocation
     */
    public final String getScreenShotLocation() {
        return screenShotLocation;
    }

    /**
     * @param screenShotLocationValue the screenShotLocation to set
     */
    public final void setScreenShotLocation(String screenShotLocationValue) {
        this.screenShotLocation = screenShotLocationValue;
    }

    /**
     * @return the screenShotWidth
     */
    public final String getScreenShotWidth() {
        return screenShotWidth;
    }

    /**
     * @param screenShotWidthValue the screenShotWidth to set
     */
    public final void setScreenShotWidth(String screenShotWidthValue) {
        this.screenShotWidth = screenShotWidthValue;
    }

    private String getReportBase() throws IOException {
        InputStream is = this.getClass().getResourceAsStream("/results-report-tmpl.html");
        String result = IOUtils.toString(is);
        return result;
    }

    private String escapeHtml(String input) {
        return StringEscapeUtils.escapeHtml(input);
    }

    public class OverviewStats {
        private int featuresPassed;
        private int featuresFailed;
        private int featuresUndefined;
        private int scenariosPassed;
        private int scenariosFailed;
        private int scenariosUndefined;
        private int stepsPassed;
        private int stepsFailed;
        private int stepsUndefined;
        private float overallDuration;

        public OverviewStats() {
            featuresPassed = 0;
            featuresFailed = 0;
            featuresUndefined = 0;
            scenariosPassed = 0;
            scenariosFailed = 0;
            scenariosUndefined = 0;
            stepsPassed = 0;
            stepsFailed = 0;
            stepsUndefined = 0;
            overallDuration = 0.f;
        }

        public final int getFeaturesPassed() {
            return featuresPassed;
        }

        public final int getFeaturesFailed() {
            return featuresFailed;
        }

        public final int getFeaturesUndefined() {
            return featuresUndefined;
        }

        public final int getScenariosPassed() {
            return scenariosPassed;
        }

        public final int getScenariosFailed() {
            return scenariosFailed;
        }

        public final int getScenariosUndefined() {
            return scenariosUndefined;
        }

        public final int getStepsPassed() {
            return stepsPassed;
        }

        public final int getStepsFailed() {
            return stepsFailed;
        }

        public final int getStepsUndefined() {
            return stepsUndefined;
        }

        public final float getOverallDuration() {
            return overallDuration;
        }

        public final void addFeaturesPassed(int featuresPassedValue) {
            this.featuresPassed += featuresPassedValue;
        }

        public final void addFeaturesFailed(int featuresFailedValue) {
            this.featuresFailed += featuresFailedValue;
        }

        public final void addFeaturesUndefined(int featuresUndefinedValue) {
            this.featuresUndefined += featuresUndefinedValue;
        }

        public final void addScenariosPassed(int scenariosPassedValue) {
            this.scenariosPassed += scenariosPassedValue;
        }

        public final void addScenariosFailed(int scenariosFailedValue) {
            this.scenariosFailed += scenariosFailedValue;
        }

        public final void addScenariosUndefined(int scenariosUndefinedValue) {
            this.scenariosUndefined += scenariosUndefinedValue;
        }

        public final void addStepsPassed(int stepsPassedValue) {
            this.stepsPassed += stepsPassedValue;
        }

        public final void addStepsFailed(int stepsFailedValue) {
            this.stepsFailed += stepsFailedValue;
        }

        public final void addStepsUndefined(int stepsUndefinedValue) {
            this.stepsUndefined += stepsUndefinedValue;
        }

        public final void addOverallDuration(float overallDurationValue) {
            this.overallDuration += overallDurationValue;
        }
    }
    public OverviewStats valuateOverviewStats(CucumberFeatureResult[] results) {
        OverviewStats stats = new OverviewStats();
        for (CucumberFeatureResult result : results) {
            result.valuate();
            stats.addOverallDuration(result.getDuration());
            if (result.getStatus().equals("passed")) {
                stats.addFeaturesPassed(1);
            } else if (result.getStatus().equals("failed")) {
                stats.addFeaturesFailed(1);
            } else {
                stats.addFeaturesUndefined(1);
            }
            stats.addScenariosPassed(result.getPassed());
            stats.addScenariosFailed(result.getFailed());
            stats.addScenariosUndefined(result.getUndefined() + result.getSkipped());

            for (CucumberScenarioResult scenario : result.getElements()) {
                stats.addStepsPassed(scenario.getPassed());
                stats.addStepsFailed(scenario.getFailed());
                stats.addStepsUndefined(scenario.getUndefined() + scenario.getSkipped());
            }
        }
        return stats;
    }
    private String generateOverview(CucumberFeatureResult[] results) {
        final int secondsInMinute = 60;
        final int secondsInHour = 3600;
        final float highestPercent = 100.f;
        float overallDuration = 0.f;
        OverviewStats stats = valuateOverviewStats(results);
        overallDuration = stats.getOverallDuration();
        return String.format(
                Locale.US,
                "<table>"
                + "<tr><th></th><th>Passed</th><th>Failed</th><th>Undefined</th><th>%%Passed</th></tr>"
                + "<tr><th>Features</th><td class=\"passed\">%d</td><td class=\"failed\">%d</td>"
                    + "<td class=\"undefined\">%d</td><td>%.2f</td></tr>"
                + "<tr><th>Scenarios</th><td class=\"passed\">%d</td><td class=\"failed\">%d</td>"
                    + "<td class=\"undefined\">%d</td><td>%.2f</td></tr>"
                + "<tr><th>Steps</th><td class=\"passed\">%d</td><td class=\"failed\">%d</td>"
                    + "<td class=\"undefined\">%d</td><td>%.2f</td></tr></table>"
                + "<div><b>Overall Duration: %dh %02dm %02ds</b></div>",
                stats.getFeaturesPassed(),
                stats.getFeaturesFailed(),
                stats.getFeaturesUndefined(),
                highestPercent * (float) stats.getFeaturesPassed()
                    / (float) (stats.getFeaturesPassed() + stats.getFeaturesFailed() + stats.getFeaturesUndefined()),
                stats.getScenariosPassed(),
                stats.getScenariosFailed(),
                stats.getScenariosUndefined(),
                highestPercent * (float) stats.getScenariosPassed()
                    / (float) (stats.getScenariosPassed() + stats.getScenariosFailed() + stats.getScenariosUndefined()),
                stats.getStepsPassed(),
                stats.getStepsFailed(),
                stats.getStepsUndefined(),
                highestPercent * (float) stats.getStepsPassed()
                    / (float) (stats.getStepsPassed() + stats.getStepsFailed() + stats.getStepsUndefined()),
                (int) overallDuration / secondsInHour,
                ((int) overallDuration % secondsInHour) / secondsInMinute,
                ((int) overallDuration % secondsInHour) % secondsInMinute);
    }
    private String generateNameFromId(String scId) {
        String result = scId.replaceAll("[; !@#$%^&*()+=]", "_");
        return result;
    }
    private String generateTableOfContents(CucumberFeatureResult[] results) {
        String reportContent = "";
        reportContent += "<a id=\"top\"></a><h1>Table of Contents</h1><ol>";
        for (CucumberFeatureResult result : results) {
            reportContent += String.format(
                    Locale.US,
                    "<li> <span class=\"%s\"><a href=\"#feature-%s\">%s</a></span><ol>",
                    result.getStatus(),
                    escapeHtml(result.getId()),
                    escapeHtml(result.getName()));
            for (CucumberScenarioResult scenario : result.getElements()) {
                if (scenario.getKeyword().contains("Scenario")) {
                    reportContent += String.format(
                            Locale.US,
                            "<li> <span class=\"%s\"><a href=\"#sc-%s\">%s</a></span></li>",
                            scenario.getStatus(),
                            escapeHtml(scenario.getId()),
                            escapeHtml(scenario.getName()));
                }
            }
            reportContent += "</ol></li>";
        }
        reportContent += "</ol>";
        return reportContent;
    }
    private String generateStepRows(CucumberStepResult step) {
        String reportContent = "";
        if (step.getRows() != null) {
            reportContent += String.format(
                    Locale.US,
                    "<tr class=\"%s\"><td style=\"padding-left:20px\" colspan=\"2\"><table>",
                    step.getResult().getStatus());
            for (int i = 0; i < step.getRows().length; i++) {
                reportContent += "<tr>";
                for (int j = 0; j < step.getRows()[i].length; j++) {
                    reportContent += String.format(Locale.US,
                            "<td>%s</td>", escapeHtml(step.getRows()[i][j]));
                }
                reportContent += "</tr>";
            }
            reportContent += "</table></td></tr>";
        }
        return reportContent;
    }
    private String generateDocString(CucumberStepResult step) {
        String reportContent = "";
        if (StringUtils.isNotBlank(step.getDocString())) {
            reportContent += String.format(
                    Locale.US,
                    "<tr class=\"%s_description\"><td style=\"padding-left:20px\" colspan=\"2\">",
                    step.getResult().getStatus());
            reportContent += String.format(
                    Locale.US,
                    "<br>%s</br>",
                    escapeHtml(step.getDocString()).replaceAll("\n", "</br><br>"));
            reportContent += "</td></tr>";
        }
        return reportContent;
    }
    private String getExtensionFromMime(String mime) {
        if (mime.contains("png")) {
            return "png";
        }
        if (mime.contains("jpg") || mime.contains("jpeg")) {
            return "jpg";
        }
        return "txt";
    }
    private String embeddingScreenShots(CucumberScenarioResult scenario, CucumberStepResult step) throws IOException {
        String reportContent = "";
        String scenarioId = scenario.getId();
        if (StringUtils.isBlank(scenarioId)) {
            scenarioId = "background";
            // Add more precise background generation
        }
        if (step.getEmbeddings() != null) {
            int index = 0;
            long base = new Date().getTime();
            for (CucumberEmbedding embedding : step.getEmbeddings()) {
                String embedPath = this.getScreenShotLocation()
                        + this.generateNameFromId(scenarioId) + (base + index) + "."
                        + getExtensionFromMime(embedding.getMimeType());
                File embedShot = new File(this.getOutputDirectory() + embedPath);
                FileUtils.writeByteArrayToFile(embedShot, embedding.getData());
                String widthString = "";
                if (StringUtils.isNotBlank(this.getScreenShotWidth())) {
                    widthString = String.format(Locale.US, "width=\"%s\"", this.getScreenShotWidth());
                }
                reportContent += String.format(Locale.US,
                        "<tr class=\"%s\"><td colspan=\"2\"><img src=\"%s\" %s /></td></tr>",
                        step.getResult().getStatus(),
                        embedPath,
                        widthString
                );
                index++;
            }
        }
        return reportContent;
    }
    private String generateScreenShot(CucumberScenarioResult scenario, CucumberStepResult step) throws IOException {
        String reportContent = "";
        if (step.getResult().getStatus().trim().equalsIgnoreCase("failed")) {
            reportContent += String.format(
                    Locale.US,
                    "<tr class=\"%s\"><td colspan=\"2\"><div>%s%s</br></div></td></tr>",
                    step.getResult().getStatus(),
                    "<br>",
                    escapeHtml(step.getResult().getErrorMessage()).replaceAll(System.lineSeparator(),
                            "</br><br>" + System.lineSeparator())
            );
            String scenarioId = scenario.getId();
            if (StringUtils.isBlank(scenarioId)) {
                scenarioId = "background";
                // Add more precise background generation
            }
            String filePath = this.getScreenShotLocation()
                    + this.generateNameFromId(scenarioId) + ".png";
            File shot = new File(this.getOutputDirectory() + filePath);
            if (shot.exists()) {
                String widthString = "";
                if (StringUtils.isNotBlank(this.getScreenShotWidth())) {
                    widthString = String.format(Locale.US, "width=\"%s\"", this.getScreenShotWidth());
                }
                reportContent += String.format(Locale.US,
                        "<tr class=\"%s\"><td colspan=\"2\"><img src=\"%s\" %s /></td></tr>",
                        step.getResult().getStatus(),
                        filePath,
                        widthString
                );
            }
        }
        return reportContent;
    }
    private String generateBeforeAfterRow(CucumberBeforeAfterResult results, String name) {
        if (results != null) {
            String error = escapeHtml(results.getResult().getErrorMessage());
            if (StringUtils.isBlank(error)) {
                error = "";
            } else {
                error = "<br>" + error.replaceAll(System.lineSeparator(), "</br><br>") + "</br>";
            }
            return String.format(
                    Locale.US,
                    "<tr class=\"%s\"><td>%s</td><td colspan=\"2\"></td><td width=\"100\">%s</td></tr>"
                    + "<tr class=\"%s\"><td colspan=\"4\">%s</td></tr>",
                    results.getResult().getStatus(),
                    name,
                    results.getResult().getDurationTimeString("HH:mm:ss:S"),
                    results.getResult().getStatus(),
                    error
            );
        }
        return "";
    }
    private String generateStepsReport(CucumberFeatureResult[] results) throws IOException {
        String content = this.getReportBase();
        content = content.replaceAll("__TITLE__", "Detailed Results Report");
        content = content.replaceAll("__OVERVIEW__", generateOverview(results));
        String reportContent = "";
        reportContent += generateTableOfContents(results);
        reportContent += "<h1>Detailed Results Report</h1><table width=\"700px\">";
        for (CucumberFeatureResult result : results) {
            String featureDescriptionHeading = "";
            if (StringUtils.isNotBlank(result.getDescription())) {
                featureDescriptionHeading = String.format(Locale.US, "<tr class=\"%s_description\">"
                        + "<td colspan=\"4\"><br>%s</br></td></tr>", result.getStatus(),
                    escapeHtml(result.getDescription()).replaceAll(System.lineSeparator(),
                            "</br><br>" + System.lineSeparator()));
            }
            reportContent += String.format(
                    Locale.US,
                    "<tr class=\"%s\"><td colspan=\"4\"><b>Feature:</b> <a id=\"feature-%s\">%s</a></td></tr>"
                    + "%s"
                    + "<tr class=\"%s\"><td><small><b>Passed:</b> %d</small></td>"
                        + "<td><small><b>Failed:</b> %d</small></td>"
                    + "<td><small><b>Undefined:</b> %d</small></td><td><small>Duration: %.2fs</small></td></tr>"
                    + "<tr class=\"%s\">"
                    + "<td colspan=\"4\" style=\"padding-left:20px\"> <table width=\"100%%\">",
                    result.getStatus(),
                    escapeHtml(result.getId()),
                    escapeHtml(result.getName()),
                    featureDescriptionHeading,
                    result.getStatus(),
                    result.getPassed(),
                    result.getFailed(),
                    result.getUndefined() + result.getSkipped(),
                    result.getDuration(),
                    result.getStatus());
            for (CucumberScenarioResult scenario : result.getElements()) {
                String descriptionHeading = "";
                if (StringUtils.isNotBlank(scenario.getDescription())) {
                    descriptionHeading = String.format(Locale.US, "<tr class=\"%s_description\">"
                        + "<td colspan=\"4\"><br>%s</br></td></tr>", scenario.getStatus(),
                        escapeHtml(scenario.getDescription()).replaceAll(System.lineSeparator(),
                                "</br><br>" + System.lineSeparator()));
                }
                reportContent += String.format(
                        Locale.US,
                        "<tr class=\"%s\"><td colspan=\"4\"><b>%s:</b> <a id=\"sc-%s\">%s</a></td></tr>"
                        + "%s"
                           + "<tr class=\"%s\">"
                        + "<td><small><b>Passed:</b> %d</small></td><td><small><b>Failed:</b> %d</small></td>"
                        + "<td><small><b>Undefined:</b> %d</small></td><td><small>Duration: %.2fs</small></td></tr>"
                           + "%s"
                        + "<tr class=\"%s\">"
                        + "<td colspan=\"4\" style=\"padding-left:20px\"> <table width=\"100%%\">",
                        scenario.getStatus(),
                        scenario.getKeyword(),
                        escapeHtml(scenario.getId()),
                        escapeHtml(scenario.getName()),
                        descriptionHeading,
                        scenario.getStatus(),
                        scenario.getPassed(),
                        scenario.getFailed(),
                        scenario.getUndefined() + scenario.getSkipped(),
                        scenario.getDuration(),
                        this.generateBeforeAfterRow(scenario.getBefore(), "Before"),
                        scenario.getStatus());
                for (CucumberStepResult step : scenario.getSteps()) {
                    reportContent += String.format(
                            Locale.US,
                            "<tr class=\"%s\"><td><b>%s</b> %s</td><td width=\"100\">%s</td></tr>",
                            step.getResult().getStatus(),
                            step.getKeyword(),
                            escapeHtml(step.getName()),
                            step.getResult().getDurationTimeString("HH:mm:ss:S")
                    );
                    reportContent += this.generateStepRows(step);
                    reportContent += this.generateDocString(step);
                    reportContent += this.generateScreenShot(scenario, step);
                    reportContent += this.embeddingScreenShots(scenario, step);
                }
                reportContent += "</table></td></tr>"
                        + this.generateBeforeAfterRow(scenario.getAfter(), "After")
                        + "<tr><td colspan=\"5\">"
                        + "<sup><a href=\"#top\">Back to Table of Contents</a></sup></td></tr>";
            }
            reportContent += "</table></td></tr><tr><td colspan=\"5\"></td></tr>";
        }
        reportContent += "</table>";
        reportContent = StringConversionUtils.replaceHtmlEntitiesWithCodes(reportContent);
        reportContent = reportContent.replaceAll("[$]", "&#36;");
        content = content.replaceAll("__REPORT__", reportContent);
        return content;
    }

    @Override
    public int[][] getStatuses(CucumberFeatureResult[] results) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public CucumberReportTypes getReportType() {
        return CucumberReportTypes.DETAILED_REPORT;
    }

    @Override
    public void validateParameters() {
        // TODO Auto-generated method stub
    }

    @Override
    public CucumberReportLink getReportDocLink() {
        // TODO Auto-generated method stub
        return CucumberReportLink.DETAILED_URL;
    }

    @Override
    public void execute(boolean aggregate, boolean toPDF) throws Exception {
        CucumberFeatureResult[] features = readFileContent(aggregate);
        String formatName = "";
        if (aggregate) {
            formatName = "%s%s%s-agg-test-results.html";
        } else {
            formatName = "%s%s%s-test-results.html";
        }
        File outFile = new File(
                String.format(Locale.US,
                        formatName,
                        this.getOutputDirectory(), File.separator, this.getOutputName()));
        String content = generateStepsReport(features);
        FileUtils.writeStringToFile(outFile, content, "UTF-8");
        if (toPDF) {
            this.exportToPDF(outFile, "test-results");
        }
    }
}
