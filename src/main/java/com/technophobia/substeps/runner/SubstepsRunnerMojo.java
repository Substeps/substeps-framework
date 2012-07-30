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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

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

    public static void printRed(final String msg) {

        // TODO
        System.out.println(msg);
    }

    // private final Logger log =
    // LoggerFactory.getLogger(SubstepsRunnerMojo.class);

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

    private List<ExecutionNode> failedNodes = null;
    private List<ExecutionNode> nonFatalFailedNodes = null;


    public void execute() throws MojoExecutionException, MojoFailureException {

        final ReportData data = new ReportData();

        Assert.assertNotNull("executionConfigs cannot be null", executionConfigs);
        Assert.assertFalse("executionConfigs can't be empty", executionConfigs.isEmpty());

        for (final ExecutionConfig executionConfig : executionConfigs) {
            final ExecutionNode rootNode = runExecutionConfig(executionConfig);

            if (executionConfig.getDescription() != null) {

                rootNode.setLine(executionConfig.getDescription());
            }

            data.addRootExecutionNode(rootNode);

            checkRootNodeForFailure(rootNode, executionConfig);
        }

        if (executionReportBuilder != null) {
            executionReportBuilder.buildReport(data);
        }

        determineBuildFailure();

    }


    /**
     * @throws MojoFailureException
     * 
     */
    private void determineBuildFailure() throws MojoFailureException {

        if (failedNodes != null && !failedNodes.isEmpty()) {

            // fail
            throw new MojoFailureException("SubStep Execution failed:\n" + getFailureString());
        }

        if (nonFatalFailedNodes != null && !nonFatalFailedNodes.isEmpty()) {

            System.out.println("NON CRITICAL FAILURES:\n\n" + buildInfoString(nonFatalFailedNodes));
        }

    }


    /**
     * @return
     */
    public String getNonFatalInfo() {
        return buildInfoString(nonFatalFailedNodes);
    }


    private String buildInfoString(final List<ExecutionNode> nodes) {
        final StringBuilder buf = new StringBuilder();

        final Set<ExecutionNode> dealtWith = new HashSet<ExecutionNode>();

        if (nodes != null) {
            for (final ExecutionNode node : nodes) {
                if (!dealtWith.contains(node)) {
                    final List<ExecutionNode> hierarchy = new ArrayList<ExecutionNode>();

                    hierarchy.add(node);

                    // go up the tree as far as we can go
                    ExecutionNode parent = node.getParent();
                    while (parent != null && nodes.contains(parent)) {
                        hierarchy.add(parent);
                        parent = parent.getParent();
                    }

                    Collections.reverse(hierarchy);

                    for (final ExecutionNode node2 : hierarchy) {
                        buf.append(node2.getDebugStringForThisNode());
                        dealtWith.add(node2);
                    }
                }
            }
        }
        return buf.toString();
    }


    /**
     * @return
     */
    public String getFailureString() {
        return "NON CRITICAL FAILURES:\n\n" + buildInfoString(nonFatalFailedNodes)
                + "\n\nCRITICAL FAILURES:\n\n" + buildInfoString(failedNodes);
    }


    /**
     * @param rootNode
     * @param executionConfig
     */
    private void checkRootNodeForFailure(final ExecutionNode rootNode,
            final ExecutionConfig executionConfig) {

        TagManager nonFatalTagManager = null;
        if (executionConfig.getNonFatalTags() != null) {
            nonFatalTagManager = new TagManager(executionConfig.getNonFatalTags());
        }

        // any of these failures should be tagged correctly so we can assess for
        // criticality
        final List<ExecutionNode> failures = rootNode.getFailedChildNodes();

        if (failures != null) {

            for (final ExecutionNode fail : failures) {

                boolean handled = false;
                if (nonFatalTagManager != null) {

                    printRed("non fatal tag mgr");

                    final Set<String> tags = fail.getTagsFromHierarchy();

                    final StringBuilder buf = new StringBuilder();

                    if (tags != null) {
                        for (final String s : tags) {
                            buf.append(s).append(" ");
                        }
                    }
                    printRed("node has tags: " + buf.toString());

                    final Set<String> acceptedTags = nonFatalTagManager.getAcceptedTags();

                    final StringBuilder buf2 = new StringBuilder();
                    for (final String s : acceptedTags) {
                        buf2.append(s).append(" ");
                    }

                    printRed("acceptedTags: " + buf2.toString());

                    if (nonFatalTagManager.acceptTaggedScenario(tags)) {
                        // this node is allowed to fail, add to the list of
                        // warnings

                        if (nonFatalFailedNodes == null) {
                            nonFatalFailedNodes = new ArrayList<ExecutionNode>();
                        }
                        nonFatalFailedNodes.add(fail);
                        handled = true;
                        printRed("failure permissable");
                    }
                }

                if (!handled) {

                    printRed("** failure not permissable");

                    if (failedNodes == null) {
                        failedNodes = new ArrayList<ExecutionNode>();
                    }

                    failedNodes.add(fail);
                }

            }
        }

        if (rootNode.hasError()) {
            if (failedNodes == null) {
                failedNodes = new ArrayList<ExecutionNode>();
            }

            failedNodes.add(rootNode);
        }

    }


    /**
     * @param notifier
     * @return
     */
    private ExecutionNode runExecutionConfig(final ExecutionConfig theConfig) {

        final ExecutionNodeRunner runner = new ExecutionNodeRunner();

        // TODO - If we want to have some fedback of nodes starting / passing /
        // failing etc then we could add
        // and INotifier to runner to receive call backs

        final ExecutionNode rootNode = runner.prepareExecutionConfig(theConfig);

        runner.run();

        return rootNode;
    }

}
