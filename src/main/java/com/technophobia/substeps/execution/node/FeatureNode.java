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
package com.technophobia.substeps.execution.node;

import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.technophobia.substeps.execution.ExecutionNodeVisitor;
import com.technophobia.substeps.execution.Feature;

public class FeatureNode extends NodeWithChildren<ScenarioNode<?>> implements TaggedNode {

    private static final long serialVersionUID = 1L;

    private final Feature feature;
    private final Set<String> tags;

    public FeatureNode(Feature feature, List<ScenarioNode<?>> scenarios, Set<String> tags) {
        super(scenarios);
        this.feature = feature;
        this.tags = tags;
        this.setDepth(1);
    }

    @Override
    public <RETURN_TYPE> RETURN_TYPE dispatch(ExecutionNodeVisitor<RETURN_TYPE> executionNodeVisitor) {

        return executionNodeVisitor.visit(this);
    }

    @Override
    public <RETURN_TYPE> List<RETURN_TYPE> accept(ExecutionNodeVisitor<RETURN_TYPE> executionNodeVisitor) {

        List<RETURN_TYPE> toReturn = Lists.newArrayList();

        toReturn.add(executionNodeVisitor.visit(this));

        for (ScenarioNode<?> scenario : getChildren()) {

            toReturn.addAll(scenario.accept(executionNodeVisitor));
        }

        return toReturn;
    }

    @Override
    public String getDescription() {

        return feature.getName();
    }

    public Set<String> getTags() {
        return tags;
    }

}
