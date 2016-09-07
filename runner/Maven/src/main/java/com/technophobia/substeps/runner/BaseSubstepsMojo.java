package com.technophobia.substeps.runner;

import com.technophobia.substeps.report.ExecutionReportBuilder;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.substeps.report.IExecutionResultsCollector;
import org.substeps.report.IReportBuilder;

import java.util.List;

/**
 * Created by ian on 06/09/16.
 */
public abstract class BaseSubstepsMojo extends AbstractMojo {


    @Parameter(defaultValue = "${session}")
    protected MavenSession session;
    /**
     * See <a href="./executionConfig.html">ExecutionConfig</a>
     */

    @Parameter
    protected List<ExecutionConfig> executionConfigs;

    /**
     * The execution report builder you wish to use
     */
    @Deprecated
    @Parameter
    protected final ExecutionReportBuilder executionReportBuilder = null;


    /**
     * The execution result collector - a specialisation of ExecutionListener
     */
    @Parameter
    protected IExecutionResultsCollector executionResultsCollector;

    /**
     * List of classes containing step implementations e.g.
     * <param>com.technophobia.substeps.StepImplmentations<param>
     */
    @Parameter
    protected List<String> stepImplementationArtifacts;

    /**
     */
    @Parameter(defaultValue = "${project}", readonly = true)
    protected MavenProject project;


    /**
     *
     */
    @Parameter
    protected IReportBuilder reportBuilder;


    /**
     * if true a jvm will be spawned to run substeps otherwise substeps will
     * execute within the same jvm as maven
     */
    @Parameter(property = "runTestsInForkedVM", defaultValue = "false")
    protected boolean runTestsInForkedVM = false;


    public IReportBuilder getReportBuilder(){
        return reportBuilder;
    }

    public List<ExecutionConfig> getExecutionConfigs() {
        return executionConfigs;
    }

    public void setExecutionConfigs(List<ExecutionConfig> executionConfigs) {
        this.executionConfigs = executionConfigs;
    }

    public ExecutionReportBuilder getExecutionReportBuilder() {
        return executionReportBuilder;
    }


    public List<String> getStepImplementationArtifacts() {
        return stepImplementationArtifacts;
    }

    public void setStepImplementationArtifacts(List<String> stepImplementationArtifacts) {
        this.stepImplementationArtifacts = stepImplementationArtifacts;
    }

    public IExecutionResultsCollector getExecutionResultsCollector() {
        return executionResultsCollector;
    }

    public boolean isRunTestsInForkedVM() {
        return runTestsInForkedVM;
    }

    public void setRunTestsInForkedVM(boolean runTestsInForkedVM) {
        this.runTestsInForkedVM = runTestsInForkedVM;
    }

}
