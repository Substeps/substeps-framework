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
package com.technophobia.substeps.execution.node;

import com.google.common.collect.Lists;
import com.technophobia.substeps.execution.ExecutionNodeVisitor;
import com.technophobia.substeps.execution.ExecutionResult;
import com.technophobia.substeps.execution.Feature;

import java.util.List;
import java.util.Set;

public class FeatureNode extends NodeWithChildren<ScenarioNode<?>> implements TaggedNode {

    private static final long serialVersionUID = 1L;

    private final Feature feature;
    private final Set<String> tags;

    public FeatureNode(final Feature feature, final List<ScenarioNode<?>> scenarios, final Set<String> tags) {
        super(scenarios);
        this.feature = feature;
        this.tags = tags;
        this.setDepth(1);
        setLine(feature.getName());
    }

    @Override
    public <RETURN_TYPE> RETURN_TYPE dispatch(final ExecutionNodeVisitor<RETURN_TYPE> executionNodeVisitor) {

        return executionNodeVisitor.visit(this);
    }

    @Override
    public <RETURN_TYPE> List<RETURN_TYPE> accept(final ExecutionNodeVisitor<RETURN_TYPE> executionNodeVisitor) {

        final List<RETURN_TYPE> toReturn = Lists.newArrayList();

        toReturn.add(executionNodeVisitor.visit(this));

        for (final ScenarioNode<?> scenario : getChildren()) {

            toReturn.addAll(scenario.accept(executionNodeVisitor));
        }

        return toReturn;
    }

    @Override
    public String getDescription() {

        return feature.getName();
    }

    @Override
    public Set<String> getTags() {
        return tags;
    }

    public static boolean hasCriticalFailure(ExecutionNode node) {
        boolean rtn = false;

        if (node.getResult().getResult() == ExecutionResult.CHILD_FAILED){
            // there is a failure at a parent level, is the cause critical ?
            // any child nodes with failures that are critical ?

            if (node instanceof NodeWithChildren){
                List<ExecutionNode> children = ((NodeWithChildren) node).getChildren();

                for (ExecutionNode child : children){
                   rtn = hasCriticalFailure(child);
                    if (rtn){
                        break;
                    }
                }
            }

        }
        else if (node.getResult().getResult() == ExecutionResult.FAILED) {
            // real failure, is it critical ?
            rtn = !node.getResult().getFailure().isNonCritical();
        }
        return rtn;
    }
}
