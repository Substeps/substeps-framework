package com.technophobia.substeps.mojo.runner;

import com.technophobia.substeps.execution.node.RootNode;
import com.technophobia.substeps.report.ExecutionReportBuilder;

import java.io.File;

/**
 * Created by ian on 21/11/15.
 */
public class StubExecutionReportBuilder extends ExecutionReportBuilder {

    public File getOutputDirectory() {
        return outputDirectory;
    }

    private File outputDirectory;

    public String getReportTitle() {
        return reportTitle;
    }

    private String reportTitle;

    @Override
    public void addRootExecutionNode(RootNode node) {

    }

    @Override
    public void buildReport() {

    }

    @Override
    public void setOutputDirectory(File file) {
        this.outputDirectory = file;
    }

    public void setReportTitle(String title) {
        this.reportTitle = title;
    }
}
