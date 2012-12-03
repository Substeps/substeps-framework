package com.technophobia.substeps.report;

public class UnableToLoadExectuionReportBuilder extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public UnableToLoadExectuionReportBuilder(String executionReportBuilderClassName, Throwable cause) {

        super("Unable to load report builder with class name '" + executionReportBuilderClassName + "'", cause);
    }

}
