/*
 *  Copyright Technophobia Ltd 2012
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
package com.technophobia.substeps.runner.node;

import com.technophobia.substeps.execution.AbstractExecutionNodeVisitor;
import com.technophobia.substeps.execution.ExecutionResult;
import com.technophobia.substeps.execution.node.IExecutionNode;
import com.technophobia.substeps.execution.node.RootNode;
import com.technophobia.substeps.execution.node.RootNodeExecutionContext;
import com.technophobia.substeps.model.Scope;
import com.technophobia.substeps.model.exception.SubstepsException;
import com.technophobia.substeps.model.exception.SubstepsRuntimeException;
import com.technophobia.substeps.runner.ExecutionContext;
import com.technophobia.substeps.runner.SubstepExecutionFailure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public abstract class AbstractNodeRunner<NODE_TYPE extends IExecutionNode, VISITOR_RETURN_TYPE> extends
        AbstractExecutionNodeVisitor<VISITOR_RETURN_TYPE> {

    private static final Logger log = LoggerFactory.getLogger(AbstractNodeRunner.class);

    public final boolean run(final NODE_TYPE node, final RootNodeExecutionContext context) {

        boolean success = false;

        if (beforeExecute(node, context)) {
            try {

                success = execute(node, context);

                log.trace("execute returned {}", success);

            } catch (final Exception e) {

                log.trace("Exception caught in {}, rethrowing...", AbstractNodeRunner.class.getSimpleName(), e);
                throw new SubstepsException(e);

            } finally {

                afterExecute(node, success, context);
            }
        }

        return success;
    }

    private boolean beforeExecute(final NODE_TYPE node, final RootNodeExecutionContext context) {

        boolean shouldContinue;

        if (node.hasError()) {

            context.getNotificationDistributor().onNodeFailed(node, node.getResult().getThrown());
            context.addFailure(new SubstepExecutionFailure(node.getResult().getThrown(), node));
            shouldContinue = false;

        } else {
            node.getResult().setStarted();
            context.getNotificationDistributor().onNodeStarted(node);

            shouldContinue = runSetup(node, context);
        }

        if (!shouldContinue) {
            log.debug("shouldContinue = false for {}", node);
        }

        return shouldContinue;
    }

    private void afterExecute(final NODE_TYPE node, final boolean success, final RootNodeExecutionContext context) {

        recordResult(node, success, context);
        runTearDown(node, context);
    }

    protected abstract boolean execute(NODE_TYPE node, RootNodeExecutionContext context);

    protected abstract Scope getScope();

    private boolean runSetup(final NODE_TYPE node, final RootNodeExecutionContext context) {

        try {
            context.getSetupAndTeardown().runSetup(getScope(), node);
            return true;
        } catch (final Throwable t) {

            log.warn("setup failed", t);
            context.addFailure(new SubstepExecutionFailure(t, node, true));
            context.getNotificationDistributor().onNodeFailed(node, t);

            return false;
        }
    }

    protected void recordResult(final NODE_TYPE node, final boolean success, final RootNodeExecutionContext context) {

        if (success) {
            if (log.isTraceEnabled()) {

                log.trace("node success");
            }

            node.getResult().setFinished();
            context.getNotificationDistributor().onNodeFinished(node);

        } else {

            final List<SubstepExecutionFailure> failures = context.getFailures();

            // it is possible to get here, without having got any failures - initialization exception for example

            final Throwable lastException;
            if (!failures.isEmpty()) {

                final SubstepExecutionFailure lastFailure = failures.get(failures.size() - 1);
                // just notify on the last one in..?
                lastException = lastFailure.getCause();
                node.getResult().setScreenshot(lastFailure.getScreenshot());
                if (node.getResult().getResult() == ExecutionResult.RUNNING) {

                    if (lastFailure.getExeccutionNode() == node) {
                        node.getResult().setFailure(lastFailure);
                    }
                    else {
                        // it's a child node that's failed - no need to copy the details
                        node.getResult().setChildFailure();
                    }
                }
                else {
                    log.debug("node not running, not setting...");
                }
            } else {
                lastException = new SubstepsRuntimeException("Error throw during startup, initialisation issue ?");
                lastException.fillInStackTrace();
                SubstepExecutionFailure.setResult(lastException, node, ExecutionResult.FAILED);
            }

            context.getNotificationDistributor().onNodeFailed(node, lastException);
        }
    }

    private void runTearDown(final NODE_TYPE node, final RootNodeExecutionContext context) {

        try {

            context.getSetupAndTeardown().runTearDown(getScope());
            ExecutionContext.clear(getScope());

        } catch (final SubstepsRuntimeException t) {
            log.warn("tear down failed", t);

            context.addFailure(new SubstepExecutionFailure(t, node, true));
        }
    }

    protected boolean addExpectedChildrenFailureIfNoChildren(final NODE_TYPE node,
                                                             final List<? extends IExecutionNode> children, final RootNodeExecutionContext context) {

        final boolean hasChildren = children != null && !children.isEmpty();
        if (!hasChildren) {

            String msg;
            if (node instanceof RootNode){
                msg = "\n\n ** No tests were executed, check Tag configuration in your pom.xml and the tags in the included features **\n\n";
            }else {
                msg = "node should have children but doesn't";
            }

            context.addFailure(new SubstepExecutionFailure(new SubstepsRuntimeException(msg), node));
        }

        return hasChildren;
    }

}
