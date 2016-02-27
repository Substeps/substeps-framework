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
package com.technophobia.substeps.execution;

import com.technophobia.substeps.execution.node.*;

/**
 * Allows a concrete visitor to override only methods which it is interested in,
 * also provides
 * <p/>
 * visit(ExecutionNode) which can be overridden for default behaviour affecting
 * any node
 * <p/>
 * visit(NodeWithChildren) which can be overridden for default behaviour for
 * nodes which have child nodes.
 * <p/>
 * visit(TaggedNode) which can be overridden for default behaviour affecting
 * nodes which have tags, if not overridden these nodes will call
 * visit(NodeWithChildren) or visit(IExecutionNode) depending on type.
 *
 * @author rbarefield
 */
public abstract class AbstractExecutionNodeVisitor<RETURN_TYPE> implements ExecutionNodeVisitor<RETURN_TYPE> {

    public RETURN_TYPE visit(RootNode rootNode) {

        return visit((NodeWithChildren<?>) rootNode);
    }

    public RETURN_TYPE visit(FeatureNode featureNode) {

        return visit((TaggedNode) featureNode);
    }

    public RETURN_TYPE visit(BasicScenarioNode basicScenarioNode) {

        return visit((TaggedNode) basicScenarioNode);
    }

    public RETURN_TYPE visit(OutlineScenarioNode outlineNode) {

        return visit((TaggedNode) outlineNode);
    }

    public RETURN_TYPE visit(OutlineScenarioRowNode outlineScenarioRowNode) {

        return visit((TaggedNode) outlineScenarioRowNode);
    }

    public RETURN_TYPE visit(SubstepNode substepNode) {

        return visit((TaggedNode) substepNode);
    }

    public RETURN_TYPE visit(StepImplementationNode stepImplementationNode) {

        return visit((TaggedNode) stepImplementationNode);
    }

    public RETURN_TYPE visit(TaggedNode node) {

        if (node instanceof NodeWithChildren<?>) {

            return visit((NodeWithChildren<?>) node);
        }

        return visit((IExecutionNode) node);
    }

    public RETURN_TYPE visit(NodeWithChildren<?> node) {
        return visit((IExecutionNode) node);
    }

    public RETURN_TYPE visit(IExecutionNode node) {

        return null;
    }
}
