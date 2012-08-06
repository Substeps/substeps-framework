package com.technophobia.substeps.runner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.maven.plugin.MojoFailureException;

import com.technophobia.substeps.execution.ExecutionNode;


/**
 * @author ian
 *
 */
public class BuildFailureManager
{
    private List<ExecutionNode> failedNodes = null;
    private List<ExecutionNode> nonFatalFailedNodes = null;

    public static void printRed(final String msg) {

        // TODO
        System.out.println(msg);
    }

    
    /**
     * @param rootNode
     * @param executionConfig
     */
    public void checkRootNodeForFailure(final ExecutionNode rootNode,
            final String nonFatalTags) {

    	
        TagManager nonFatalTagManager = null;
        if (nonFatalTags != null) {
            nonFatalTagManager = new TagManager(nonFatalTags);
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
     * @throws MojoFailureException
     * 
     */
    public void determineBuildFailure() throws MojoFailureException {

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
    public String getFailureString() {
        return "NON CRITICAL FAILURES:\n\n" + buildInfoString(nonFatalFailedNodes)
                + "\n\nCRITICAL FAILURES:\n\n" + buildInfoString(failedNodes);
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

}
