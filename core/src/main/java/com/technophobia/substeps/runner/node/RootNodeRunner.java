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

import com.technophobia.substeps.execution.ExecutionResult;
import com.technophobia.substeps.execution.node.FeatureNode;
import com.technophobia.substeps.execution.node.RootNode;
import com.technophobia.substeps.execution.node.RootNodeExecutionContext;
import com.technophobia.substeps.model.Scope;
import com.technophobia.substeps.model.exception.SubstepsRuntimeException;
import com.technophobia.substeps.runner.SubstepExecutionFailure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RootNodeRunner extends AbstractNodeRunner<RootNode, Void> {

    FeatureNodeRunner featureNodeRunner = new FeatureNodeRunner();

    private static final Logger log = LoggerFactory.getLogger(RootNodeRunner.class);


    @Override
    protected boolean execute(RootNode node, RootNodeExecutionContext context) {

        boolean success = addExpectedChildrenFailureIfNoChildren(node, node.getChildren(), context);

        for (FeatureNode feature : node.getChildren()) {

            success &= featureNodeRunner.run(feature, context);
        }

        return success;
    }

    @Override
    protected Scope getScope() {

        return Scope.SUITE;

    }

    // TODO rootnode recording of result is sutbly different - the rootnode is just the sum of its parts, but features can be tagged with non fatal tags

    @Override
    protected void recordResult(final RootNode node, final boolean success, final RootNodeExecutionContext context) {

        if (success) {
            if (log.isTraceEnabled()) {

                log.trace("node success");
            }

            node.getResult().setFinished();
            context.getNotificationDistributor().onNodeFinished(node);

        } else {

            if (log.isDebugEnabled()) {

                log.debug("node failures");
            }

            // have a look at the constituent features
            boolean rootNodeStateSet = false;
            for (FeatureNode featureNode : node.getChildren()) {

                if (FeatureNode.hasCriticalFailure(featureNode)){

                    // we've got one valid feature failure, fail the root node
                    SubstepsRuntimeException e = new SubstepsRuntimeException("At least one critical Feature failed");
                    SubstepExecutionFailure.setResult(e, node, ExecutionResult.FAILED);

                    context.getNotificationDistributor().onNodeFailed(node, e);
                    rootNodeStateSet = true;
                    break;
                }

                if (FeatureNode.hasNonCriticalFailure(featureNode)){
                    SubstepsRuntimeException e = new SubstepsRuntimeException("At least one NON critical Feature failed");
                    SubstepExecutionFailure.setResult(e, node, ExecutionResult.NON_CRITICAL_FAILURE);

                    context.getNotificationDistributor().onNodeFailed(node, e);
                    rootNodeStateSet = true;
                }
            }

            if (!rootNodeStateSet) {
                // got this far, must be ok
                node.getResult().setFinished();
                context.getNotificationDistributor().onNodeFinished(node);
            }

        }
    }


}
