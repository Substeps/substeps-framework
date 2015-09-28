/*
 *	Copyright Technophobia Ltd 2012
 *
 *   This file is part of Substeps.
 *
 *    Substeps is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU Lesser General Public License as published by
 *    the Free Software Foundation, either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    Substeps is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public License
 *    along with Substeps.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.technophobia.substeps.runner;

import java.util.Collections;
import java.util.List;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.technophobia.substeps.execution.AbstractExecutionNodeVisitor;
import com.technophobia.substeps.execution.node.IExecutionNode;
import com.technophobia.substeps.execution.node.NodeWithChildren;
import com.technophobia.substeps.execution.node.RootNode;

/**
 * @author ian
 * 
 */
public class BuildFailureManager extends AbstractExecutionNodeVisitor<String> {

    private final List<List<IExecutionNode>> criticalFailures = Lists.newArrayList();
    private final List<List<IExecutionNode>> nonCriticalFailures = Lists.newArrayList();

    public String getBuildFailureInfo() {
        return getBuildInfoString("NON CRITICAL FAILURES:", this.nonCriticalFailures)
                + getBuildInfoString("CRITICAL FAILURES:", this.criticalFailures);
    }

    public void addExecutionResult(RootNode rootNode) {

        addFailuresToLists(rootNode, Collections.<IExecutionNode> emptyList());
    }

    private String getBuildInfoString(final String msg, final List<List<IExecutionNode>> failures) {

        final StringBuilder buf = new StringBuilder();

        if (failures != null && !failures.isEmpty()) {

            buf.append("\n");
            buf.append(msg);

            for (List<IExecutionNode> failurePath : failures) {

                buf.append("\n\n");

                IExecutionNode lastNode = failurePath.get(failurePath.size() - 1);

                ThrowableInfo throwableInfo = lastNode.getResult().getFailure().getThrowableInfo();

                //Throwable throwable = lastNode.getResult().getFailure().getCause();

                if (throwableInfo != null && throwableInfo.getMessage() != null) {

                    buf.append(throwableInfo.getMessage() + "\n");
                }

                buf.append("Trace:\n\n");

                for (IExecutionNode node : failurePath) {

                    buf.append(node.getId() + ":");
                    buf.append(Strings.repeat("   ", node.getDepth()));
                    buf.append(node.getDescription() + "\n");

                }
            }
        }

        return buf.toString();
    }

    private void addFailuresToLists(IExecutionNode node, List<IExecutionNode> parents) {

        List<IExecutionNode> path = Lists.newArrayList(parents);
        path.add(node);

        if (node.getResult().getFailure() != null) {

            SubstepExecutionFailure failure = node.getResult().getFailure();

            if (failure.isNonCritical()) {

                nonCriticalFailures.add(path);
            } else {
                criticalFailures.add(path);
            }
        } else if (node instanceof NodeWithChildren<?>) {

            for (IExecutionNode childNode : ((NodeWithChildren<?>) node).getChildren()) {

                addFailuresToLists(childNode, path);
            }
        }
    }

    public boolean testSuiteCompletelyPassed() {
        return (this.criticalFailures == null && this.nonCriticalFailures == null)
                ||

                (this.criticalFailures != null && this.criticalFailures.isEmpty() && this.nonCriticalFailures != null && this.nonCriticalFailures
                        .isEmpty());
    }

    public boolean testSuiteSomeFailures() {
        return (testSuiteFailed()) || (this.nonCriticalFailures != null && !this.nonCriticalFailures.isEmpty());
    }

    public boolean testSuiteFailed() {
        return (this.criticalFailures != null && !this.criticalFailures.isEmpty());
    }
}
