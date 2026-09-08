package com.harness.demo;

public class BuildInfo {

    public String version() {
        return "5";
    }

    public String artifactName() {
        return "sei-scm-demo";
    }

    public String buildSummary(String greeting) {
        return "build-summary: artifact=" + artifactName()
                + " version=" + version()
                + " greeting-length=" + greeting.length();
    }
}
