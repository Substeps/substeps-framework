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

import java.util.Collections;
import java.util.List;
import java.util.Set;

public class OutlineScenarioRowNode extends NodeWithChildren<BasicScenarioNode> implements TaggedNode {

    private static final long serialVersionUID = 1L;

    private final int rowIndex;

    private final Set<String> tags;

    public OutlineScenarioRowNode(int rowIndex, BasicScenarioNode basicScenarioNode, Set<String> tags, int depth) {
        super(Collections.singletonList(basicScenarioNode));
        this.rowIndex = rowIndex;
        this.tags = tags;
        this.setDepth(depth);
    }

    public BasicScenarioNode getBasicScenarioNode() {
        return getChildren().get(0);
    }

    @Override
    public <RETURN_TYPE> RETURN_TYPE dispatch(ExecutionNodeVisitor<RETURN_TYPE> executionNodeVisitor) {

        return executionNodeVisitor.visit(this);
    }

    @Override
    public <RETURN_TYPE> List<RETURN_TYPE> accept(ExecutionNodeVisitor<RETURN_TYPE> executionNodeVisitor) {

        List<RETURN_TYPE> toReturn = Lists.newArrayList();

        toReturn.add(executionNodeVisitor.visit(this));

        if (getChildren().size() == 1) {
            toReturn.addAll(getChildren().get(0).accept(executionNodeVisitor));
        }

        return toReturn;
    }

    @Override
    public String getDescription() {

        return rowIndex + " " + getChildren().get(0).getScenarioName() + ":";
    }

    public Set<String> getTags() {
        return tags;
    }
}
