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

import com.google.common.io.Files;
import com.technophobia.substeps.execution.node.RootNode;
import com.technophobia.substeps.model.exception.SubstepsRuntimeException;
import com.typesafe.config.*;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.metadata.ArtifactMetadataSource;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.*;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuilder;
import org.substeps.runner.NewSubstepsExecutionConfig;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

/**
 * Mojo to run a number SubStep features, each contained within any number of
 * executionConfigs, encapsulating the required config and setup and tear down
 * details
 */
@Mojo(name = "run-features",
        defaultPhase = LifecyclePhase.INTEGRATION_TEST,
        requiresDependencyResolution = ResolutionScope.TEST,
        requiresProject = true,
        configurator = "include-project-dependencies")
public class SubstepsRunnerMojo extends BaseSubstepsMojo {


    private final BuildFailureManager buildFailureManager = new BuildFailureManager();

    /**
     * at component
     */
    @Component
    private ArtifactResolver artifactResolver;

    /**
     * at component
     */
    @Component

    private ArtifactFactory artifactFactory;

    /**
     * at component
     */
    @Component

    private ProjectBuilder projectBuilder;

    /**
     */
    @Parameter(defaultValue = "${localRepository}")

    private org.apache.maven.artifact.repository.ArtifactRepository localRepository;

    /**
     */
    @Parameter(defaultValue = "${project.remoteArtifactRepositories}")

    private List remoteRepositories;

    /**
     */
    @Parameter(defaultValue = "${plugin.artifacts}")

    private List<Artifact> pluginDependencies;


    /**
     * //     * at component
     */
    @Component
    private ArtifactMetadataSource artifactMetadataSource;


    @Override
    public void executeAfterAllConfigs(Config masterConfig) throws MojoExecutionException, MojoFailureException{
        processBuildData();
    }

    @Override
    public void executeBeforeAllConfigs(Config masterConfig) throws MojoExecutionException, MojoFailureException{

        // write out the master config to the root data dir
        File rootDataDir = NewSubstepsExecutionConfig.getRootDataDir(masterConfig);

        File outFile = new File(rootDataDir, "masterConfig.conf");

        mkdirOrException(rootDataDir);

        try {
            String renderedConfig = NewSubstepsExecutionConfig.render(masterConfig);
            this.getLog().info("\n\n *** USING COMBINED CONFIG:\n\n" + renderedConfig + "\n\n");

            Files.write(renderedConfig, outFile, Charset.forName("UTF-8"));
        }
        catch (IOException e){
            throw new MojoExecutionException(e.getMessage(), e);
        }
    }

    private void mkdirOrException(File dir)  {
        if (!dir.exists()){

            if (!dir.mkdirs()){
                throw new SubstepsRuntimeException("Failed to create dir: " + dir.getAbsolutePath());
            }
        }

    }


    private ForkedRunner createForkedRunner() throws MojoExecutionException {

        try {

            if (this.project == null) {
                this.getLog().error("this.project is null");
            }

            return new ForkedRunner(getLog(), NewSubstepsExecutionConfig.getJmxPort(), NewSubstepsExecutionConfig.getVMArgs(), this.project.getTestClasspathElements(),
                    this.stepImplementationArtifacts, this.artifactResolver, this.artifactFactory,
                    this.projectBuilder, this.localRepository, this.remoteRepositories,
                    this.artifactMetadataSource);
        } catch (final DependencyResolutionRequiredException e) {

            throw new MojoExecutionException("Unable to resolve dependencies", e);
        }
    }


    private InProcessRunner createInProcessRunner() {

        return new InProcessRunner(getLog());
    }




    @Override
    public void executeConfig(final Config executionConfig) throws MojoExecutionException {
        // executionConfig will be whole, self contained and already split and resolved from a masterConfig

        MojoRunner runner = null;

        try {
            runner = NewSubstepsExecutionConfig.isRunInForkedVM(executionConfig) ? createForkedRunner() : createInProcessRunner();

            final RootNode iniitalRootNode = runner.prepareExecutionConfig(executionConfig);

            this.executionResultsCollector = NewSubstepsExecutionConfig.getExecutionResultsCollector(executionConfig);

            this.executionResultsCollector.setDataDir(NewSubstepsExecutionConfig.getDataOutputDirectory(executionConfig));

            this.executionResultsCollector.initOutputDirectories(iniitalRootNode);

            runner.addNotifier(this.executionResultsCollector);

            final RootNode rootNode = runner.run();

            String description = NewSubstepsExecutionConfig.getDescription(executionConfig);

            if (description != null) {

                rootNode.setLine(description);
            }

            addToLegacyReport(rootNode);

            this.buildFailureManager.addExecutionResult(rootNode);
        }
        finally {
            if (runner != null) {
                runner.shutdown();
            }
        }
    }

    private void addToLegacyReport(final RootNode rootNode) {

        if (reportBuilder == null && this.executionReportBuilder != null) {

            getLog().warn("\nExecutionReportBuilder is deprecated, replace with:\n\t<reportBuilder implementation=\"org.substeps.report.ReportBuilder\">\n" +
                    "\t\t<reportDir>${project.build.directory}/substeps_report</reportDir>\n" +
                    "\t</reportBuilder>\n");

            this.executionReportBuilder.addRootExecutionNode(rootNode);
        }
    }


    /**
     * @throws MojoFailureException
     */
    private void processBuildData() throws MojoFailureException {

        if (reportBuilder == null && this.executionReportBuilder != null) {

            this.executionReportBuilder.buildReport();
        }


        StringBuilder buf = new StringBuilder();
        for (String s : this.session.getGoals()){
            buf.append(s);
            buf.append(" ");
        }

        this.getLog().info("this.session.getGoals(): " + buf.toString());

        List<String> goals = this.session.getGoals();

        if (this.buildFailureManager.testSuiteFailed()) {

            MojoFailureException e = new MojoFailureException("Substep Execution failed:\n"
                    + this.buildFailureManager.getBuildFailureInfo());


            // actually throwing an exception results in the build terminating immediately
            // - not really desireable as it stops the report from being built in the verify phase

            if (goals.contains("verify") || goals.contains("install") || goals.contains("deploy")){

                // we don't want to throw the exception - it will be thrown in the reportbuildermojo
                getLog().info("Not immediately failing the build, deferring..");
                this.session.getResult().addException(e);
            }
            else {
                throw e;
            }

        } else if (!this.buildFailureManager.testSuiteCompletelyPassed()) {
            // print out the failure string (but won't include any failures)
            getLog().info(this.buildFailureManager.getBuildFailureInfo());
        }
    }








    public MavenProject getProject() {
        return project;
    }

    public void setProject(MavenProject project) {
        this.project = project;
    }

    public BuildFailureManager getBuildFailureManager() {
        return buildFailureManager;
    }

    public ArtifactResolver getArtifactResolver() {
        return artifactResolver;
    }

    public void setArtifactResolver(ArtifactResolver artifactResolver) {
        this.artifactResolver = artifactResolver;
    }

    public ArtifactFactory getArtifactFactory() {
        return artifactFactory;
    }

    public void setArtifactFactory(ArtifactFactory artifactFactory) {
        this.artifactFactory = artifactFactory;
    }

    public ProjectBuilder getProjectBuilder() {
        return projectBuilder;
    }

    public void setProjectBuilder(ProjectBuilder projectBuilder) {
        this.projectBuilder = projectBuilder;
    }

    public ArtifactRepository getLocalRepository() {
        return localRepository;
    }

    public void setLocalRepository(ArtifactRepository localRepository) {
        this.localRepository = localRepository;
    }

    public List getRemoteRepositories() {
        return remoteRepositories;
    }

    public void setRemoteRepositories(List remoteRepositories) {
        this.remoteRepositories = remoteRepositories;
    }

    public List<Artifact> getPluginDependencies() {
        return pluginDependencies;
    }

    public void setPluginDependencies(List<Artifact> pluginDependencies) {
        this.pluginDependencies = pluginDependencies;
    }

    public ArtifactMetadataSource getArtifactMetadataSource() {
        return artifactMetadataSource;
    }

    public void setArtifactMetadataSource(ArtifactMetadataSource artifactMetadataSource) {
        this.artifactMetadataSource = artifactMetadataSource;
    }

}
