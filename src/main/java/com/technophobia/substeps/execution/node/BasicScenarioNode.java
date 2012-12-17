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

import java.util.Collections;
import java.util.List;

import com.google.common.collect.Lists;
import com.technophobia.substeps.execution.ExecutionNodeVisitor;

public class BasicScenarioNode extends ScenarioNode<SubstepNode> {

    private static final long serialVersionUID = 1L;

    private final SubstepNode background;
    private final SubstepNode step;
    private final String scenarioName;

    public BasicScenarioNode(String scenarioName, SubstepNode background, SubstepNode step, int depth) {

        this.scenarioName = scenarioName;
        this.background = background;
        this.step = step;
        this.setDepth(depth);
    }

    public SubstepNode getStep() {

        return step;
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

        if (this.step != null) {

            toReturn.addAll(this.step.accept(executionNodeVisitor));
        } else {
            // TODO RB20121214 Add failure
        }

        return toReturn;
    }

    @Override
    public List<SubstepNode> getChildren() {
        return Collections.singletonList(step);
    }

    @Override
    public String getDescription() {

        return scenarioName;
    }

    public String getScenarioName() {

        return scenarioName;
    }
}
