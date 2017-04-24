package com.technophobia.substeps.runner;

import com.google.common.collect.Lists;
import com.technophobia.substeps.model.Configuration;
import com.technophobia.substeps.report.ExecutionReportBuilder;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigRenderOptions;
import com.typesafe.config.ConfigValueFactory;
//import org.apache.maven.execution.MavenSession;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Parameter;
//import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProject;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.substeps.report.IExecutionResultsCollector;
import org.substeps.report.IReportBuilder;
import org.substeps.runner.NewSubstepsExecutionConfig;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    // TODO - this appears to be only necessary for running in forked mode - in addition to the test classpath
    @Parameter
    protected List<String> stepImplementationArtifacts;


    @Parameter
    protected List<String> executionConfigFiles = null;

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


    /**
     * When running in forked mode, a port is required to communicate between
     * maven and substeps, to set explicitly use -DjmxPort=9999
     */
    @Parameter(defaultValue = "9999")
    private Integer jmxPort;

    /**
     * A space delimited string of vm arguments to pass to the forked jvm
     */
    @Parameter
    private String vmArgs = null;





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

    public Integer getJmxPort() {
        return jmxPort;
    }

    public void setJmxPort(Integer jmxPort) {
        this.jmxPort = jmxPort;
    }

    public String getVmArgs() {
        return vmArgs;
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

        Config cfg =
                ConfigFactory.empty()
                        .withValue("project.build.directory",
                                ConfigValueFactory.fromAnyRef(this.project.getBuild().getDirectory()))
                        .withValue("basedir", ConfigValueFactory.fromAnyRef(this.project.getBasedir().getAbsolutePath()));

        return cfg;
    }

    @Override
    public final void execute() throws MojoExecutionException, MojoFailureException {

        checkPomSettings();

        ensureValidConfiguration();

        setupBuildEnvironmentInfo();

        //this.runner = this.runTestsInForkedVM ? createForkedRunner() : createInProcessRunner();

        Config mavenConfigSettings = buildMavenFallbackConfig();

        List<Config> configs = NewSubstepsExecutionConfig.toConfigList(executionConfigFiles, mavenConfigSettings);

        try {
            executeBeforeAllConfigs(configs);

            executeNewConfigs(configs);
            //executeConfigs();

            //processBuildData();
        }
        finally {
            executeAfterAllConfigs(configs);

            //this.runner.shutdown();
        }
    }

    private void executeNewConfigs(List<Config> configs) throws MojoExecutionException {
        // executionConfigFiles will already have been checked for null


        for (Config executionConfig : configs){
            try {
                executionConfig(executionConfig);

            } catch (final Exception e) {

                // to cater for any odd exceptions thrown out.. at least this way
                // jvm shouldn't just die, unless it was going to die anyway
                throw new MojoExecutionException("Unhandled exception: " + e.getMessage(), e);
            }

        }
    }

    public abstract void executionConfig(Config cfg) throws MojoExecutionException, MojoFailureException;

    public abstract void executeBeforeAllConfigs(List<Config> configs) throws MojoExecutionException, MojoFailureException;

    public abstract void executeAfterAllConfigs(List<Config> configs) throws MojoExecutionException, MojoFailureException;

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
        if (executionConfigFiles == null) {
            Config cfg = createExecutionConfigFromPom();

            ConfigRenderOptions options =
                    ConfigRenderOptions.defaults().setComments(false).setFormatted(true).setJson(false).setOriginComments(false);

            throw new MojoExecutionException(this, "Substeps execution config has changed and moved to a config file",

                    "Create a config file on the classpath with the following content:\n\n" + cfg.root().render(options) + "\n\n" +
                            "And replace the configuration section:\n\n" +
                            " <configuration>\n\t<executionConfigFiles>\n\t\t<param> ** config file ** </param>\n\t</executionConfigFiles>\n</configuration>"
            );
        }

    }

    public Config createExecutionConfigFromPom() {

        Map execConfig1 = new HashMap();

        List<Map<String, Object>> executionConfigList = this.executionConfigs.stream().map(ec -> toMap(ec)).collect(Collectors.toList());

        String baseDescription = this.executionConfigs.get(0).getDescription();

        List<Map<String, Object>> execConfigs = new ArrayList<>();
        execConfigs.add(execConfig1);

        Config cfg =
                ConfigFactory.empty().withValue("org.substeps.config.executionConfigs",
                        ConfigValueFactory.fromIterable(executionConfigList))
                        .withValue("org.substeps.config.description", ConfigValueFactory.fromAnyRef(baseDescription))
                        .withValue("org.substeps.config.jmxPort", ConfigValueFactory.fromAnyRef(this.jmxPort))
                        .withValue("org.substeps.config.vmArgs", ConfigValueFactory.fromAnyRef(this.vmArgs))
                        .withValue("org.substeps.config.executionResultsCollector", ConfigValueFactory.fromAnyRef(this.executionResultsCollector.getClass().getName()))
                        .withValue("org.substeps.config.reportBuilder", ConfigValueFactory.fromAnyRef(this.getReportBuilder().getClass().getName()));
        return cfg;
    }


    private static Map<String, Object> toMap(ExecutionConfig executionConfig){
        Map<String, Object> execConfig1 = new HashMap<>();

        execConfig1.put("description", executionConfig.getDescription());
        execConfig1.put("featureFile", executionConfig.getFeatureFile());
        execConfig1.put("dataOutputDir", executionConfig.getDataOutputDirectory().getPath());
        execConfig1.put("nonFatalTags", executionConfig.getNonFatalTags());
        execConfig1.put("substepsFile", executionConfig.getSubStepsFileName());
        execConfig1.put("tags", executionConfig.getTags());

        if (executionConfig.getNonStrictKeywordPrecedence() != null) {
            execConfig1.put("nonStrictKeyWordPrecedence", Lists.newArrayList(executionConfig.getNonStrictKeywordPrecedence()));
        }

        execConfig1.put("stepImplementationClassNames", Lists.newArrayList(executionConfig.getStepImplementationClassNames()));
        execConfig1.put("executionListeners", Lists.newArrayList(executionConfig.getExecutionListeners()));

        if (executionConfig.getInitialisationClass() != null) {
            execConfig1.put("initialisationClasses", Lists.newArrayList(executionConfig.getInitialisationClass()));
        }

        if (executionConfig.getExecutionListeners() != null) {
            execConfig1.put("executionListeners", Lists.newArrayList(executionConfig.getExecutionListeners()));
        }

        return execConfig1;
    }

}
