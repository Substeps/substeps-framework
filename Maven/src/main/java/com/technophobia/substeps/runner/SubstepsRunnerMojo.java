/*
 *	Copyright Technophobia Ltd 2012
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

import java.util.List;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.metadata.ArtifactMetadataSource;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectBuilder;

import com.technophobia.substeps.execution.node.RootNode;
import com.technophobia.substeps.report.ExecutionReportBuilder;

/**
 * Mojo to run a number SubStep features, each contained within any number of
 * executionConfigs, encapsulating the required config and setup and tear down
 * details
 * 
 * @goal run-features
 * @requiresDependencyResolution test
 * @phase integration-test
 * 
 * @configurator include-project-dependencies
 */
@SuppressWarnings("unchecked")
public class SubstepsRunnerMojo extends AbstractMojo {

    /**
     * 
     * See <a href="./executionConfig.html">ExecutionConfig</a>
     * 
     * @parameter
     */
    private List<ExecutionConfig> executionConfigs;

    /**
     * The execution report builder you wish to use
     * 
     * @parameter
     */
    private final ExecutionReportBuilder executionReportBuilder = null;

    /**
     * When running in forked mode, a port is required to communicate between
     * maven and substeps, to set explicitly use -DjmxPort=9999
     * 
     * @parameter default-value="9999" expression="${jmxPort}"
     */
    private Integer jmxPort;

    /**
     * A space delimited string of vm arguments to pass to the forked jvm
     * 
     * @parameter
     */
    private final String vmArgs = null;

    /**
     * if true a jvm will be spawned to run substeps otherwise substeps will
     * execute within the same jvm as maven
     * 
     * @parameter default-value=true;
     * @required
     */
    private boolean runTestsInForkedVM;

    /**
     * List of classes containing step implementations e.g.
     * <param>com.technophobia.substeps.StepImplmentations<param>
     * 
     * @parameter
     */
    private List<String> stepImplementationArtifacts;

    /**
     * @parameter default-value="${project}"
     * @required
     * @readonly
     */
    private MavenProject project;

    private final BuildFailureManager buildFailureManager = new BuildFailureManager();

    /**
     * @component
     */
    private ArtifactResolver artifactResolver;

    /**
     * @component
     */
    private ArtifactFactory artifactFactory;

    /**
     * @component
     */
    private MavenProjectBuilder mavenProjectBuilder;

    /**
     * @parameter default-value="${localRepository}"
     * @readonly
     */
    private org.apache.maven.artifact.repository.ArtifactRepository localRepository;

    /**
     * @parameter default-value="${project.remoteArtifactRepositories}"
     * @readonly
     */
    private List remoteRepositories;

    /**
     * @parameter expression="${plugin.artifacts}"
     * @readonly
     */
    private List<Artifact> pluginDependencies;

    /**
     * @component
     */
    private ArtifactMetadataSource artifactMetadataSource;

    private MojoRunner runner;


    public void execute() throws MojoExecutionException, MojoFailureException {

        assertCompatibleCoreVersion();

        ensureValidConfiguration();

        this.runner = this.runTestsInForkedVM ? createForkedRunner() : createInProcessRunner();

        executeConfigs();

        processBuildData();

        this.runner.shutdown();
    }


    private void assertCompatibleCoreVersion() throws MojoExecutionException {

        CoreVersionChecker.assertCompatibleVersion(getLog(), this.artifactFactory, this.artifactResolver,
                this.remoteRepositories, this.localRepository, this.mavenProjectBuilder, this.project,
                this.pluginDependencies);
    }


    private ForkedRunner createForkedRunner() throws MojoExecutionException {

        try {

            return new ForkedRunner(getLog(), this.jmxPort, this.vmArgs, this.project.getTestClasspathElements(),
                    this.stepImplementationArtifacts, this.artifactResolver, this.artifactFactory,
                    this.mavenProjectBuilder, this.localRepository, this.remoteRepositories,
                    this.artifactMetadataSource);
        } catch (final DependencyResolutionRequiredException e) {

            throw new MojoExecutionException("Unable to resolve dependencies", e);
        }
    }


    private InProcessRunner createInProcessRunner() {

        return new InProcessRunner(getLog());
    }


    private void executeConfigs() throws MojoExecutionException {

        if (this.executionConfigs == null || this.executionConfigs.isEmpty()) {

            throw new MojoExecutionException("executionConfigs cannot be null or empty");
        }

        try {
            for (final ExecutionConfig executionConfig : this.executionConfigs) {

                runExecutionConfig(executionConfig);
            }
        } catch (final Throwable t) {

            // to cater for any odd exceptions thrown out.. at least this way
            // jvm shouldn't just die, unless it was going to die anyway
            throw new MojoExecutionException("Unhandled exception: " + t.getMessage(), t);
        }
    }


    private void runExecutionConfig(final ExecutionConfig theConfig) throws MojoExecutionException {

        this.runner.prepareExecutionConfig(theConfig.asSubstepsExecutionConfig());

        final RootNode rootNode = this.runner.run();

        if (theConfig.getDescription() != null) {

            rootNode.setLine(theConfig.getDescription());
        }

        addToReport(rootNode);

        this.buildFailureManager.addExecutionResult(rootNode);
    }


    private void addToReport(final RootNode rootNode) {

        if (this.executionReportBuilder != null) {
            this.executionReportBuilder.addRootExecutionNode(rootNode);
        }
    }


    /**
     *
     * @throws MojoFailureException
     */
    private void processBuildData() throws MojoFailureException {

        if (this.executionReportBuilder != null) {
            this.executionReportBuilder.buildReport();
        }

        if (this.buildFailureManager.testSuiteFailed()) {

            throw new MojoFailureException("Substep Execution failed:\n"
                    + this.buildFailureManager.getBuildFailureInfo());

        } else if (!this.buildFailureManager.testSuiteCompletelyPassed()) {
            // print out the failure string (but won't include any failures)
            getLog().info(this.buildFailureManager.getBuildFailureInfo());
        }
    }


    private void ensureValidConfiguration() throws MojoExecutionException {

        ensureForkedIfStepImplementationArtifactsSpecified();
    }


    private void ensureForkedIfStepImplementationArtifactsSpecified() throws MojoExecutionException {

        if (this.stepImplementationArtifacts != null && !this.stepImplementationArtifacts.isEmpty()
                && !this.runTestsInForkedVM) {
            throw new MojoExecutionException(
                    "Invalid configuration of substeps runner, if stepImplementationArtifacts are specified runTestsInForkedVM must be true");
        }

    }

}
