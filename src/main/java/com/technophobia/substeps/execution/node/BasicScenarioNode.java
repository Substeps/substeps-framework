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
    private final List<StepNode> steps;
    private final String scenarioName;

    private final Set<String> tags;

    public BasicScenarioNode(String scenarioName, SubstepNode background, List<StepNode> steps, Set<String> tags,
            int depth) {

        this.scenarioName = scenarioName;
        this.background = background;
        this.steps = steps;
        this.tags = tags;
        this.setDepth(depth);
    }

    public List<StepNode> getSteps() {

        return steps;
    }

    public SubstepNode getBackground() {

        return background;
    }

    @Override
    public <RETURN_TYPE> RETURN_TYPE dispatch(ExecutionNodeVisitor<RETURN_TYPE> executionNodeVisitor) {

        return executionNodeVisitor.visit(this);
    }

    @Override
    public <RETURN_TYPE> List<RETURN_TYPE> accept(ExecutionNodeVisitor<RETURN_TYPE> executionNodeVisitor) {

        List<RETURN_TYPE> toReturn = Lists.newArrayList();

        toReturn.add(executionNodeVisitor.visit(this));

        if (this.background != null) {

            toReturn.addAll(this.background.accept(executionNodeVisitor));
        }

        for (StepNode step : steps) {

            toReturn.addAll(step.accept(executionNodeVisitor));
        }

        return toReturn;
    }

    @Override
    public List<StepNode> getChildren() {
        return steps;
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
