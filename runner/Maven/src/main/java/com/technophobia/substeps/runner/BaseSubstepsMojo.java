package com.technophobia.substeps.runner;

import com.technophobia.substeps.model.Configuration;
import com.technophobia.substeps.report.ExecutionReportBuilder;
import com.typesafe.config.Config;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.substeps.report.IExecutionResultsCollector;
import org.substeps.report.IReportBuilder;

import java.io.File;
import java.util.List;

/**
 * Created by ian on 06/09/16.
 */
public abstract class BaseSubstepsMojo extends AbstractMojo {

    protected static final String STEP_IMPLS_JSON_FILENAME = "stepimplementations.json";


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
    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    protected MavenProject project;

    /**
     * Location of the file.
     */
    @Parameter(defaultValue = "${project.build.outputDirectory}", required = true)
    protected File outputDirectory;

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

    public String getVersion(){
        return project.getVersion();
    }

    protected void setupBuildEnvironmentInfo(){



        MavenProject root = project;

        while (root.hasParent()){
            root = root.getParent();
        }
        System.setProperty("SUBSTEPS_CURRENT_PROJECT_VERSION", project.getVersion());


        FileRepositoryBuilder builder = new FileRepositoryBuilder();
        try {
            Repository repository = builder.setGitDir(new File(root.getBasedir(), ".git"))
                    .readEnvironment() // scan environment GIT_* variables
                    .findGitDir() // scan up the file system tree
                    .build();

            Git git = new Git(repository);

            String branchName = git.getRepository().getBranch();

            if (branchName != null) {
                System.setProperty("SUBSTEPS_CURRENT_BRANCHNAME", branchName);
            }
        }
        catch (Exception e){
            // this is best efforts...
            getLog().debug("Not important - Exception trying to get hold of the current branch", e);

        }

    }
}
