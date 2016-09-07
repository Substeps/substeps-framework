/*
 *  Copyright Technophobia Ltd 2012
 *
 *   This file is part of Substeps Maven Runner.
 *
 *    Substeps Maven Runner is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU Lesser General Public License as published by
 *    the Free Software Foundation, either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    Substeps Maven Runner is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public License
 *    along with Substeps.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.technophobia.substeps.runner;

import com.technophobia.substeps.execution.node.RootNode;
import com.technophobia.substeps.report.ExecutionReportBuilder;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.metadata.ArtifactMetadataSource;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.*;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectBuilder;
import org.substeps.report.IExecutionResultsCollector;
import org.substeps.report.IReportBuilder;

import java.util.List;

/**
 * Mojo to run a number SubStep features, each contained within any number of
 * executionConfigs, encapsulating the required config and setup and tear down
 * details
 */
@Mojo(name = "build-report",
        defaultPhase = LifecyclePhase.VERIFY,
        requiresDependencyResolution = ResolutionScope.TEST,
        requiresProject = true)
//,configurator = "include-project-dependencies")
public class SubstepsReportBuilderMojo extends BaseSubstepsMojo {


    /**
     * See <a href="./executionConfig.html">ExecutionConfig</a>
     */

//    @Parameter
//    private List<ExecutionConfig> executionConfigs;

    /**
     * The execution report builder you wish to use
     */
//    @Parameter
//    private final ExecutionReportBuilder executionReportBuilder = null;


    /**
     * The execution result collector - a specialisation of ExecutionListener
     */
//    @Parameter
//    private IExecutionResultsCollector executionResultsCollector;



    /**
     * When running in forked mode, a port is required to communicate between
     * maven and substeps, to set explicitly use -DjmxPort=9999
     */
//    @Parameter(defaultValue = "9999")
//    private Integer jmxPort;

    /**
     * A space delimited string of vm arguments to pass to the forked jvm
     */
//    @Parameter
//    private String vmArgs = null;

    /**
     * if true a jvm will be spawned to run substeps otherwise substeps will
     * execute within the same jvm as maven
     */
//    @Parameter(property = "runTestsInForkedVM", defaultValue = "false")
//
//    private boolean runTestsInForkedVM = false;

    /**
     * List of classes containing step implementations e.g.
     * <param>com.technophobia.substeps.StepImplmentations<param>
     */
//    @Parameter
//    private List<String> stepImplementationArtifacts;

    /**
     */
//    @Parameter(defaultValue = "${project}", readonly = true)
//    private MavenProject project;

//    private final BuildFailureManager buildFailureManager = new BuildFailureManager();
//
//    /**
//     * at component
//     */
//    @Component
//    private ArtifactResolver artifactResolver;
//
//    /**
//     * at component
//     */
//    @Component
//
//    private ArtifactFactory artifactFactory;
//
//    /**
//     * at component
//     */
//    @Component
//
//    private MavenProjectBuilder mavenProjectBuilder;
//
//    /**
//     */
////    @Parameter(defaultValue = "${localRepository}")
////    private ArtifactRepository localRepository;
//
//    /**
//     */
////    @Parameter(defaultValue = "${project.remoteArtifactRepositories}")
////    private List remoteRepositories;
//
//    /**
//     */
//    @Parameter(defaultValue = "${plugin.artifacts}")
//    private List<Artifact> pluginDependencies;
//
//    /**
//     * //     * at component
//     */
//    @Component
//    private ArtifactMetadataSource artifactMetadataSource;
//
//    private MojoRunner runner;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {


        getLog().info("Building substeps report from data in: " + this.executionResultsCollector.getDataDir().toString());

        StringBuilder buf = new StringBuilder();
        for (String s : this.session.getGoals()){
            buf.append(s);
            buf.append(" ");
        }

        this.getLog().info("this.session.getGoals(): " + buf.toString());


        reportBuilder.buildFromDirectory(this.executionResultsCollector.getDataDir());

//        ensureValidConfiguration();
//
//        this.runner = this.runTestsInForkedVM ? createForkedRunner() : createInProcessRunner();
//
//        executeConfigs();
//
//        processBuildData();
//
//        this.runner.shutdown();
    }


//    private void assertCompatibleCoreVersion() throws MojoExecutionException {
//
//        CoreVersionChecker.assertCompatibleVersion(getLog(), this.artifactFactory, this.artifactResolver,
//                this.remoteRepositories, this.localRepository, this.mavenProjectBuilder, this.project,
//                this.pluginDependencies);
//    }

//
//    private ForkedRunner createForkedRunner() throws MojoExecutionException {
//
//        try {
//
//            if (this.project == null) {
//                this.getLog().error("this.project is null");
//            }
//            return new ForkedRunner(getLog(), this.jmxPort, this.vmArgs, this.project.getTestClasspathElements(),
//                    this.stepImplementationArtifacts, this.artifactResolver, this.artifactFactory,
//                    this.mavenProjectBuilder, this.localRepository, this.remoteRepositories,
//                    this.artifactMetadataSource);
//        } catch (final DependencyResolutionRequiredException e) {
//
//            throw new MojoExecutionException("Unable to resolve dependencies", e);
//        }
//    }
//
//
//    private InProcessRunner createInProcessRunner() {
//
//        return new InProcessRunner(getLog());
//    }


//    private void executeConfigs() throws MojoExecutionException {
//
//        if (this.executionConfigs == null || this.executionConfigs.isEmpty()) {
//
//            throw new MojoExecutionException("executionConfigs cannot be null or empty");
//        }
//
//        try {
//            for (final ExecutionConfig executionConfig : this.executionConfigs) {
//
//                runExecutionConfig(executionConfig);
//            }
//        } catch (final Exception e) {
//
//            // to cater for any odd exceptions thrown out.. at least this way
//            // jvm shouldn't just die, unless it was going to die anyway
//            throw new MojoExecutionException("Unhandled exception: " + e.getMessage(), e);
//        }
//    }


//    private void runExecutionConfig(final ExecutionConfig theConfig) throws MojoExecutionException {
//
//        final RootNode iniitalRootNode = this.runner.prepareExecutionConfig(theConfig.asSubstepsExecutionConfig());
//
//        this.executionResultsCollector.initOutputDirectories(iniitalRootNode);
//
//        this.runner.addNotifier(this.executionResultsCollector);
//
//        final RootNode rootNode = this.runner.run();
//
//        if (theConfig.getDescription() != null) {
//
//            rootNode.setLine(theConfig.getDescription());
//        }
//
//        addToReport(rootNode);
//
//        this.buildFailureManager.addExecutionResult(rootNode);
//    }




//    private void addToReport(final RootNode rootNode) {
//
//        if (this.executionReportBuilder != null) {
//            this.executionReportBuilder.addRootExecutionNode(rootNode);
//        }
//    }


    /**
     * @throws MojoFailureException
     */
//    private void processBuildData() throws MojoFailureException {
//
//        if (this.executionReportBuilder != null) {
//            this.getLog().debug("Using old mechanism for building Substeps execution reports");
//            this.executionReportBuilder.buildReport();
//        }
//
//
//
//        if (this.buildFailureManager.testSuiteFailed()) {
//
//            throw new MojoFailureException("Substep Execution failed:\n"
//                    + this.buildFailureManager.getBuildFailureInfo());
//
//        } else if (!this.buildFailureManager.testSuiteCompletelyPassed()) {
//            // print out the failure string (but won't include any failures)
//            getLog().info(this.buildFailureManager.getBuildFailureInfo());
//        }
//    }
//
//
//    private void ensureValidConfiguration() throws MojoExecutionException {
//
//        ensureForkedIfStepImplementationArtifactsSpecified();
//    }


//    private void ensureForkedIfStepImplementationArtifactsSpecified() throws MojoExecutionException {
//
//        if (this.stepImplementationArtifacts != null && !this.stepImplementationArtifacts.isEmpty()
//                && !this.runTestsInForkedVM) {
//            throw new MojoExecutionException(
//                    "Invalid configuration of substeps runner, if stepImplementationArtifacts are specified runTestsInForkedVM must be true");
//        }
//
//    }
//
//
//    public List<ExecutionConfig> getExecutionConfigs() {
//        return executionConfigs;
//    }
//
//    public void setExecutionConfigs(List<ExecutionConfig> executionConfigs) {
//        this.executionConfigs = executionConfigs;
//    }
//
//    public ExecutionReportBuilder getExecutionReportBuilder() {
//        return executionReportBuilder;
//    }
//
//    public Integer getJmxPort() {
//        return jmxPort;
//    }
//
//    public void setJmxPort(Integer jmxPort) {
//        this.jmxPort = jmxPort;
//    }
//
//    public String getVmArgs() {
//        return vmArgs;
//    }
//
//    public void setVmArgs(String vmArgs) {
//        this.vmArgs = vmArgs;
//    }
//
//    public boolean isRunTestsInForkedVM() {
//        return runTestsInForkedVM;
//    }
//
//    public void setRunTestsInForkedVM(boolean runTestsInForkedVM) {
//        this.runTestsInForkedVM = runTestsInForkedVM;
//    }
//
//    public List<String> getStepImplementationArtifacts() {
//        return stepImplementationArtifacts;
//    }
//
//    public void setStepImplementationArtifacts(List<String> stepImplementationArtifacts) {
//        this.stepImplementationArtifacts = stepImplementationArtifacts;
//    }
//
//    public MavenProject getProject() {
//        return project;
//    }

//    public void setProject(MavenProject project) {
//        this.project = project;
//    }

//    public BuildFailureManager getBuildFailureManager() {
//        return buildFailureManager;
//    }
//
//    public ArtifactResolver getArtifactResolver() {
//        return artifactResolver;
//    }
//
//    public void setArtifactResolver(ArtifactResolver artifactResolver) {
//        this.artifactResolver = artifactResolver;
//    }
//
//    public ArtifactFactory getArtifactFactory() {
//        return artifactFactory;
//    }
//
//    public void setArtifactFactory(ArtifactFactory artifactFactory) {
//        this.artifactFactory = artifactFactory;
//    }
//
//    public MavenProjectBuilder getMavenProjectBuilder() {
//        return mavenProjectBuilder;
//    }
//
//    public void setMavenProjectBuilder(MavenProjectBuilder mavenProjectBuilder) {
//        this.mavenProjectBuilder = mavenProjectBuilder;
//    }

//    public ArtifactRepository getLocalRepository() {
//        return localRepository;
//    }
//
//    public void setLocalRepository(ArtifactRepository localRepository) {
//        this.localRepository = localRepository;
//    }
//
//    public List getRemoteRepositories() {
//        return remoteRepositories;
//    }
//
//    public void setRemoteRepositories(List remoteRepositories) {
//        this.remoteRepositories = remoteRepositories;
//    }

//    public List<Artifact> getPluginDependencies() {
//        return pluginDependencies;
//    }
//
//    public void setPluginDependencies(List<Artifact> pluginDependencies) {
//        this.pluginDependencies = pluginDependencies;
//    }
//
//    public ArtifactMetadataSource getArtifactMetadataSource() {
//        return artifactMetadataSource;
//    }
//
//    public void setArtifactMetadataSource(ArtifactMetadataSource artifactMetadataSource) {
//        this.artifactMetadataSource = artifactMetadataSource;
//    }
//
//    public MojoRunner getRunner() {
//        return runner;
//    }
//
//    public void setRunner(MojoRunner runner) {
//        this.runner = runner;
//    }

//    public IExecutionResultsCollector getExecutionResultsCollector() {
//        return executionResultsCollector;
//    }
}
