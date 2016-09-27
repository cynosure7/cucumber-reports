package com.github.mkolisnyk.cucumber.reporting.types.enums;

public enum CucumberReportLink {
    COMMON_URL("http://mkolisnyk.github.io/cucumber-reports"),
    BREAKDOWN_URL(COMMON_URL + "/breakdown-report"),
    CHART_URL(COMMON_URL + "/chart-report"),
    CONSOLIDATED_URL(COMMON_URL + "/consolidated-report"),
    COVERAGE_OVERVIEW_URL(COMMON_URL + "/coverage-report"),
    DETAILED_URL(COMMON_URL + "/detailed-report"),
    FEATURE_MAP_URL(COMMON_URL + "/"),
    FEATURE_OVERVIEW_URL(COMMON_URL + "/overview-chart-report"),
    KNOWN_ERRORS_URL(COMMON_URL + "/known-errors-report"),
    RESULTS_OVERVIEW_URL(COMMON_URL + "/overview-report"),
    RETROSPECTIVE_OVERVIEW_URL(COMMON_URL + "/retrospective-results-report"),
    USAGE_URL(COMMON_URL + "/usage-report");
    private String value;

    CucumberReportLink(String valueData) {
        this.value = valueData;
    }
    public String toString() {
        return this.value;
    }
}
