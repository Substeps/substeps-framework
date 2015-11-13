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

public class OutlineScenarioNode extends ScenarioNode<OutlineScenarioRowNode> {

    private static final long serialVersionUID = 1L;

    private final String scenarioName;

    private final Set<String> tags;

    public OutlineScenarioNode(final String scenarioName, final List<OutlineScenarioRowNode> outlineRows, final Set<String> tags,
            final int depth) {
        super(outlineRows);
        this.scenarioName = scenarioName;
        this.tags = tags;
        this.setDepth(depth);
        setLine(scenarioName);
    }

    @Override
    public <RETURN_TYPE> RETURN_TYPE dispatch(final ExecutionNodeVisitor<RETURN_TYPE> executionNodeVisitor) {

        return executionNodeVisitor.visit(this);
    }

    @Override
    public <RETURN_TYPE> List<RETURN_TYPE> accept(final ExecutionNodeVisitor<RETURN_TYPE> executionNodeVisitor) {

        final List<RETURN_TYPE> toReturn = Lists.newArrayList();

        toReturn.add(executionNodeVisitor.visit(this));

        for (final OutlineScenarioRowNode outlineRow : getChildren()) {

            toReturn.addAll(outlineRow.accept(executionNodeVisitor));
        }

        return toReturn;
    }

    @Override
    public String getDescription() {

        return "Scenario #: " + scenarioName;
    }

    public Set<String> getTags() {
        return tags;
    }
}
