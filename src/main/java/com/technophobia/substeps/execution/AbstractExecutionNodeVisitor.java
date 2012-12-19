package com.technophobia.substeps.execution;

import com.technophobia.substeps.execution.node.BasicScenarioNode;
import com.technophobia.substeps.execution.node.FeatureNode;
import com.technophobia.substeps.execution.node.IExecutionNode;
import com.technophobia.substeps.execution.node.NodeWithChildren;
import com.technophobia.substeps.execution.node.OutlineScenarioNode;
import com.technophobia.substeps.execution.node.OutlineScenarioRowNode;
import com.technophobia.substeps.execution.node.RootNode;
import com.technophobia.substeps.execution.node.StepImplementationNode;
import com.technophobia.substeps.execution.node.SubstepNode;
import com.technophobia.substeps.execution.node.TaggedNode;

/**
 * Allows a concrete visitor to override only methods which it is interested in,
 * also provides
 * 
 * visit(ExecutionNode) which can be overridden for default behaviour affecting
 * any node
 * 
 * visit(NodeWithChildren) which can be overridden for default behaviour for
 * nodes which have child nodes.
 * 
 * visit(TaggedNode) which can be overridden for default behaviour affecting
 * nodes which have tags, if not overridden these nodes will call
 * visit(NodeWithChildren) or visit(IExecutionNode) depending on type.
 * 
 * @author rbarefield
 * 
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
