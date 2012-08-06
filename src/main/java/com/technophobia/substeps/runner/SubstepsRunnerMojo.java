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

import java.io.File;
import java.util.List;
import java.util.Properties;

import junit.framework.Assert;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;

import com.technophobia.substeps.execution.ExecutionNode;
import com.technophobia.substeps.report.ExecutionReportBuilder;
import com.technophobia.substeps.report.ReportData;

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
public class SubstepsRunnerMojo extends AbstractMojo {



    /**
     * Location of the file.
     * 
     * @parameter expression="${project.build.directory}"
     * @required
     */
    private File outputDirectory;

    /**
     * @parameter default-value="${project.build.directory}"
     */
    private File outputDir;

    /**
     * @parameter default-value="${project}"
     * @required
     * @readonly
     */
    private MavenProject project;

    /**
     * @parameter
     */
    private Properties systemProperties;

    /**
     * @parameter
     */
    private List<ExecutionConfig> executionConfigs;

    /**
     * @parameter
     */
    private final ExecutionReportBuilder executionReportBuilder = null;



    public void execute() throws MojoExecutionException, MojoFailureException {

        final Runner runner = new Runner();
        final BuildFailureManager buildFailureManager = new BuildFailureManager();

        executeInternal(runner, buildFailureManager, executionConfigs);
    }
    
    // internal execute method to make easier to test
    private void executeInternal(final Runner runner, final BuildFailureManager buildFailureManager, 
    		final List<ExecutionConfig> executionConfigList) throws MojoFailureException {

        final ReportData data = new ReportData();

        Assert.assertNotNull("executionConfigs cannot be null", executionConfigList);
        Assert.assertFalse("executionConfigs can't be empty", executionConfigList.isEmpty());

        for (final ExecutionConfig executionConfig : executionConfigList) {
            final ExecutionNode rootNode = runner.runExecutionConfig(executionConfig);

            if (executionConfig.getDescription() != null) {

                rootNode.setLine(executionConfig.getDescription());
            }

            data.addRootExecutionNode(rootNode);

            buildFailureManager.checkRootNodeForFailure(rootNode, executionConfig.getNonFatalTags());
        }

        if (executionReportBuilder != null) {
            executionReportBuilder.buildReport(data);
        }

        buildFailureManager.determineBuildFailure();

    }
}
