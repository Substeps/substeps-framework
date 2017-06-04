package com.technophobia.substeps.runner;

import com.google.common.collect.Lists;
import com.google.common.io.Files;
import com.technophobia.substeps.model.Configuration;
import com.technophobia.substeps.model.exception.SubstepsConfigurationException;
import com.technophobia.substeps.report.ExecutionReportBuilder;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigRenderOptions;
import com.typesafe.config.ConfigValueFactory;
//import org.apache.maven.execution.MavenSession;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Resource;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Parameter;
//import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProject;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.substeps.config.SubstepsConfigLoader;
import org.substeps.report.IExecutionResultsCollector;
import org.substeps.report.IReportBuilder;
import org.substeps.runner.NewSubstepsExecutionConfig;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
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


//    @Parameter
//    protected List<String> executionConfigFiles = null;

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
                        .withValue("project.build.directory", ConfigValueFactory.fromAnyRef(this.project.getBuild().getDirectory()))
                        .withValue("basedir", ConfigValueFactory.fromAnyRef(this.project.getBasedir().getAbsolutePath()))
                        .withValue("project.build.testOutputDirectory", ConfigValueFactory.fromAnyRef(this.project.getBuild().getTestOutputDirectory()))
                        .withValue("project.build.outputDirectory", ConfigValueFactory.fromAnyRef(this.project.getBuild().getDirectory()))
                ;

        return cfg;
    }

    @Override
    public final void execute() throws MojoExecutionException, MojoFailureException {

        checkPomSettings();

        ensureValidConfiguration();

        setupBuildEnvironmentInfo();

        //this.runner = this.runTestsInForkedVM ? createForkedRunner() : createInProcessRunner();

        Config mavenConfigSettings = buildMavenFallbackConfig();

        Config masterConfig = SubstepsConfigLoader.loadResolvedConfig(mavenConfigSettings);

        this.getLog().info("\n\n *** USING COMBINED CONFIG:\n\n" +
        NewSubstepsExecutionConfig.render(masterConfig) + "\n\n");

        List<Config> configs = SubstepsConfigLoader.splitMasterConfig(masterConfig);
            //NewSubstepsExecutionConfig.toConfigList(executionConfigFiles, mavenConfigSettings);

        try {
            executeBeforeAllConfigs(masterConfig);

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

    public abstract void executeConfig(Config cfg) throws MojoExecutionException, MojoFailureException;

    public abstract void executeBeforeAllConfigs(Config masterConfig) throws MojoExecutionException, MojoFailureException;

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
        if (executionConfigs != null && !executionConfigs.isEmpty()) {
            Config cfg = createExecutionConfigFromPom();

            ConfigRenderOptions options =
                    ConfigRenderOptions.defaults().setComments(false).setFormatted(true).setJson(false).setOriginComments(false);

            String configSrc = sanitize(cfg.root().render(options));

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

                Files.write(configSrc, out , Charset.defaultCharset());

                throw new MojoExecutionException(this, "Substeps execution config has changed and moved to a config file", "A new config file has been written to: " + out.getAbsolutePath() + ",\n this will need checking and renaming to application.conf");

            } catch (IOException e) {

                this.getLog().info("failed to write application.conf file");

                throw new MojoExecutionException(this, "Substeps execution config has changed and moved to a config file",

                        "Create a config file on the classpath with the following content:\n\n" + configSrc + "\n\n" +
                                "And replace the configuration section:\n\n" +
                                " <configuration>\n\t<executionConfigFiles>\n\t\t<param> ** config file ** </param>\n\t</executionConfigFiles>\n</configuration>"
                );

            }


        }

    }

    public Config createExecutionConfigFromPom() {

        Map execConfig1 = new HashMap();

        if (this.executionConfigs.size() > 1){
            this.getLog().debug("\n\n ** There are multiple execution configs, the generated config may need some manual editing **\n\n");
        }

        List<Map<String, Object>> executionConfigList = this.executionConfigs.stream().map(ec -> toMap(ec)).collect(Collectors.toList());

        String baseDescription = this.executionConfigs.get(0).getDescription();


        List<Map<String, Object>> execConfigs = new ArrayList<>();
        execConfigs.add(execConfig1);

        Config cfg = ConfigFactory.empty()
                        .withValue("org.substeps.config.description", ConfigValueFactory.fromAnyRef(baseDescription));

        if (this.vmArgs != null){
            cfg = cfg.withValue("org.substeps.config.vmArgs", ConfigValueFactory.fromAnyRef(this.vmArgs));
        }

        if (this.jmxPort != 9999){
            cfg = cfg.withValue("org.substeps.config.jmxPort", ConfigValueFactory.fromAnyRef(this.jmxPort));
        }

        // this should hopefully cater for when the data is being written elsewhere..
        if (!executionConfigList.get(0).containsKey("dataOutputDir")){
            cfg = cfg.withValue("org.substeps.config.rootDataDir", ConfigValueFactory.fromAnyRef(deClutter(this.executionConfigs.get(0).getDataOutputDirectory().getPath())));

            executionConfigList.get(0).put("dataOutputDir", "");
        }

        cfg = cfg.withValue("org.substeps.executionConfigs", ConfigValueFactory.fromIterable(executionConfigList));


        if (!this.executionResultsCollector.getClass().getName().equals("org.substeps.report.ExecutionResultsCollector")){
            cfg = cfg.withValue("org.substeps.config.executionResultsCollector", ConfigValueFactory.fromAnyRef(this.executionResultsCollector.getClass().getName()));
        }

        if (!this.getReportBuilder().getClass().getName().equals("org.substeps.report.ReportBuilder")){
            cfg = cfg.withValue("org.substeps.config.reportBuilder", ConfigValueFactory.fromAnyRef(this.getReportBuilder().getClass().getName()));
        }


        return cfg;
    }

    // necessary so that the variables get substituted correctly
    private String sanitize(String src){
        src = StringUtils.replace(src, "\"${project.build.testOutputDirectory}", "${project.build.testOutputDirectory}\"");
        src = StringUtils.replace(src, "\"${project.build.outputDirectory}", "${project.build.outputDirectory}\"");
        src = StringUtils.replace(src, "\"${project.build.directory}", "${project.build.directory}\"");
        src = StringUtils.replace(src, "\"${basedir}", "${basedir}\"");

        return src;
    }

    private String deClutter(String value){
        String baseDir = this.project.getBasedir().getAbsolutePath();
        String testOut = this.project.getBuild().getTestOutputDirectory();
        String srcOut = this.project.getBuild().getOutputDirectory();
        String target = this.project.getBuild().getDirectory();

        this.getLog().info("declutter value: " + value);

        String rtn = value;
        if (value.startsWith(testOut)){
            rtn = "${project.build.testOutputDirectory}" + StringUtils.removeStart(value, testOut);
        }
        else if (value.startsWith(srcOut)){
            rtn = "${project.build.outputDirectory}" + StringUtils.removeStart(value, target);

        }
        else if (value.startsWith(target)){
            rtn = "${project.build.directory}" +  StringUtils.removeStart(value, target);
        }
        else if (value.startsWith(baseDir)){
            rtn = "${basedir}" + StringUtils.removeStart(value, target);

        }
        return rtn;
    }

    private Map<String, Object> toMap(ExecutionConfig executionConfig){
        Map<String, Object> execConfig1 = new HashMap<>();

        execConfig1.put("description", executionConfig.getDescription());
        execConfig1.put("featureFile", deClutter(executionConfig.getFeatureFile()));

        // this is the default root data dir

        String rootDataDir =         this.project.getBuild().getDirectory() + File.separator + "substeps_data";

        if (StringUtils.startsWith(executionConfig.getDataOutputDirectory().getPath(), rootDataDir)){
            execConfig1.put("dataOutputDir",StringUtils.removeStart(executionConfig.getDataOutputDirectory().getPath(), rootDataDir));
        }
        // else the root data dir is being somewhere else, so we'll set the root data value accordingly


        execConfig1.put("nonFatalTags", executionConfig.getNonFatalTags());
        execConfig1.put("substepsFile", deClutter(executionConfig.getSubStepsFileName()));
        execConfig1.put("tags", executionConfig.getTags());

        if (executionConfig.getNonStrictKeywordPrecedence() != null) {
            execConfig1.put("nonStrictKeyWordPrecedence", Lists.newArrayList(executionConfig.getNonStrictKeywordPrecedence()));
        }

        execConfig1.put("stepImplementationClassNames", Lists.newArrayList(executionConfig.getStepImplementationClassNames()));

        if (executionConfig.getExecutionListeners().length > 1 || !executionConfig.getExecutionListeners()[0].equals("com.technophobia.substeps.runner.logger.StepExecutionLogger")){
            execConfig1.put("executionListeners", Lists.newArrayList(executionConfig.getExecutionListeners()));
        }


        if (executionConfig.getInitialisationClass() != null) {
            execConfig1.put("initialisationClasses", Lists.newArrayList(executionConfig.getInitialisationClass()));
        }

//        if (executionConfig.getExecutionListeners() != null) {
//            execConfig1.put("executionListeners", Lists.newArrayList(executionConfig.getExecutionListeners()));
//        }

        return execConfig1;
    }

}
