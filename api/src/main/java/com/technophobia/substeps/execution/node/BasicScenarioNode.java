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

public class BasicScenarioNode extends ScenarioNode<StepNode> {

    private static final long serialVersionUID = 1L;

    private final SubstepNode background;
    private final String scenarioName;

    private final Set<String> tags;

    public BasicScenarioNode(final String scenarioName, final SubstepNode background, final List<StepNode> steps, final Set<String> tags,
            final int depth) {
        super(steps);
        this.scenarioName = scenarioName;
        this.background = background;
        this.tags = tags;
        this.setDepth(depth);
        setLine(scenarioName);
    }

    public List<StepNode> getSteps() {

        return getChildren();
    }

    public SubstepNode getBackground() {

        return background;
    }

    @Override
    public <RETURN_TYPE> RETURN_TYPE dispatch(final ExecutionNodeVisitor<RETURN_TYPE> executionNodeVisitor) {

        return executionNodeVisitor.visit(this);
    }

    @Override
    public <RETURN_TYPE> List<RETURN_TYPE> accept(final ExecutionNodeVisitor<RETURN_TYPE> executionNodeVisitor) {

        final List<RETURN_TYPE> results = Lists.newArrayList();

        results.add(executionNodeVisitor.visit(this));

        if (this.background != null) {

            results.addAll(this.background.accept(executionNodeVisitor));
        }

        for (final StepNode step : getChildren()) {

            results.addAll(step.accept(executionNodeVisitor));
        }

        return results;
    }

    @Override
    public String getDescription() {

        return scenarioName;
    }

    public String getScenarioName() {

        return scenarioName;
    }

    public Set<String> getTags() {
        return tags;
    }
}
