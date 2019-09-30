package com.technophobia.substeps.runner;

import com.google.common.io.Files;
import com.technophobia.substeps.model.exception.SubstepsConfigurationException;
import com.technophobia.substeps.report.ExecutionReportBuilder;
import com.typesafe.config.Config;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Resource;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.substeps.config.SubstepsConfigConverter;
import org.substeps.config.SubstepsConfigLoader;
import org.substeps.report.IExecutionResultsCollector;
import org.substeps.report.IReportBuilder;
import org.substeps.runner.NewSubstepsExecutionConfig;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

/**
 * Created by ian on 06/09/16.
 */
public abstract class BaseSubstepsMojo extends AbstractMojo {

    protected static final String STEP_IMPLS_JSON_FILENAME = "stepimplementations.json";


    @Parameter(defaultValue = "${session}")
    protected MavenSession session;


    /**
     * will be removed in a later release, use .conf files instead
     * @deprecated use .config files for runtime configuration instead
     */
    @Deprecated
    @Parameter
    protected List<ExecutionConfig> executionConfigs;

    /**
     * The execution report builder you wish to use
     */
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
    // TODO - this appears to be only necessary for running in forked mode - in addition to the test classpath
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
    protected File outputDirectory;  // TODO still referenced by the glossary plugin

    /**
     *
     */
    @Parameter
    protected IReportBuilder reportBuilder;


    /**
     * if true a jvm will be spawned to run substeps otherwise substeps will
     * execute within the same jvm as maven
     * @deprecated use .config files for runtime configuration instead
     */
    @Deprecated
    @Parameter(property = "runTestsInForkedVM", defaultValue = "false")
    protected boolean runTestsInForkedVM = false;


    /**
     * When running in forked mode, a port is required to communicate between
     * maven and substeps, to set explicitly use -DjmxPort=9999
     * @deprecated use .config files for runtime configuration instead
     */
    @Parameter(defaultValue = "9999")
    @Deprecated
    protected Integer jmxPort;

    /**
     * A space delimited string of vm arguments to pass to the forked jvm
     * @deprecated use .config files for runtime configuration instead
     */
    @Deprecated
    @Parameter
    protected String vmArgs = null;





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

    public void setRunTestsInForkedVM(boolean runTestsInForkedVM) {
        this.runTestsInForkedVM = runTestsInForkedVM;
    }

    public String getVersion(){
        return project.getVersion();
    }

    public void setJmxPort(Integer jmxPort) {
        this.jmxPort = jmxPort;
    }

    public void setVmArgs(String vmArgs) {
        this.vmArgs = vmArgs;
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

    private Config buildMavenFallbackConfig(){

        Config cfg = SubstepsConfigLoader.buildMavenFallbackConfig(this.project.getBuild().getDirectory(),
                this.project.getBasedir().getAbsolutePath(),
                this.project.getBuild().getTestOutputDirectory());

        return cfg;
    }

    @Override
    public final void execute() throws MojoExecutionException, MojoFailureException {

        checkPomSettings();

        ensureValidConfiguration();

        setupBuildEnvironmentInfo();

        Config mavenConfigSettings = buildMavenFallbackConfig();

        Config masterConfig = SubstepsConfigLoader.loadResolvedConfig(mavenConfigSettings);

        List<Config> configs = SubstepsConfigLoader.splitMasterConfig(masterConfig);

        try {
            executeBeforeAllConfigs(masterConfig);

            executeNewConfigs(configs);
        }
        finally {
            executeAfterAllConfigs(masterConfig);
        }
    }

    private void executeNewConfigs(List<Config> configs) throws MojoExecutionException {
        // executionConfigFiles will already have been checked for null

        for (Config executionConfig : configs){

            NewSubstepsExecutionConfig.setThreadLocalConfig(executionConfig);

            try{
                NewSubstepsExecutionConfig.validateExecutionConfig(executionConfig);

            }
            catch (SubstepsConfigurationException e){
                throw new MojoExecutionException("Substeps configuration problem", e);
            }
            try {
                executeConfig(executionConfig);

            } catch (final Exception e) {

                // to cater for any odd exceptions thrown out.. at least this way
                // jvm shouldn't just die, unless it was going to die anyway
                throw new MojoExecutionException("Unhandled exception: " + e.getMessage(), e);
            }

        }
    }

    public abstract void executeConfig(Config cfg) throws MojoExecutionException;

    public abstract void executeBeforeAllConfigs(Config masterConfig) throws MojoExecutionException;

    public abstract void executeAfterAllConfigs(Config masterConfig) throws MojoFailureException;

    private void ensureValidConfiguration() throws MojoExecutionException {

        ensureForkedIfStepImplementationArtifactsSpecified();
    }


    private void ensureForkedIfStepImplementationArtifactsSpecified() throws MojoExecutionException {
        // TODO - need to change this to take into account new config
        if (this.stepImplementationArtifacts != null && !this.stepImplementationArtifacts.isEmpty()
                && !this.runTestsInForkedVM) {
            throw new MojoExecutionException(
                    "Invalid configuration of substeps runner, if stepImplementationArtifacts are specified runTestsInForkedVM must be true");
        }

    }


    protected void checkPomSettings() throws MojoExecutionException{
        if (executionConfigs != null && !executionConfigs.isEmpty()) {

            Config cfg = SubstepsConfigConverter.convert(getLog(), this.executionConfigs, this.project, this.vmArgs, this.jmxPort, this.executionResultsCollector, this.reportBuilder, this.runTestsInForkedVM);

            String configSrc = SubstepsConfigConverter.renderSanitizedConfig(cfg, getLog());

            try {
                List<Resource> testResources = this.project.getTestResources();

                String resourcesDir = "";

                if (testResources != null && !testResources.isEmpty()) {
                    this.getLog().info("test resources src root: " + testResources.get(0).getDirectory());
                    resourcesDir = testResources.get(0).getDirectory();
                }


                if (resourcesDir == null) {

                    List<Resource> resources = this.project.getResources();
                    if (resources != null && !resources.isEmpty()) {
                        this.getLog().info("resources src root: " + resources.get(0).getDirectory());
                        resourcesDir = resources.get(0).getDirectory();
                    }
                }

                File out = new File(resourcesDir, "migrated-application.conf");

                Files.asCharSink(out, Charset.defaultCharset()).write(configSrc);


                throw new MojoExecutionException(this, "Substeps execution config has changed and moved to a config file", "A new config file has been written to: " + out.getAbsolutePath() + ",\n this will need checking and renaming to application.conf");

            } catch (IOException e) {

                this.getLog().info("failed to write application.conf file", e);

                throw new MojoExecutionException(this, "Substeps execution config has changed and moved to a config file",

                        "Create a config file on the classpath with the following content:\n\n" + configSrc + "\n\n" +
                                "And replace the configuration section:\n\n" +
                                " <configuration>\n\t<executionConfigFiles>\n\t\t<param> ** config file ** </param>\n\t</executionConfigFiles>\n</configuration>"
                );

            }


        }

    }


}
