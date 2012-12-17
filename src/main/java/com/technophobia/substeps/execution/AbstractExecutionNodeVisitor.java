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

/**
 * Allows a concrete visitor to override only methods which it is interested in,
 * also provides
 * 
 * visit(ExecutionNode) which can be overridden for default behaviour affecting
 * any node or visit(NodeWithChildren) which can be overridden for default
 * behaviour for nodes which have child nodes.
 * 
 * 
 * @author rbarefield
 * 
 */
public abstract class AbstractExecutionNodeVisitor<RETURN_TYPE> implements ExecutionNodeVisitor<RETURN_TYPE> {

    public RETURN_TYPE visit(RootNode rootNode) {

        return visit((NodeWithChildren<?>) rootNode);
    }

    public RETURN_TYPE visit(FeatureNode featureNode) {

        return visit((NodeWithChildren<?>) featureNode);
    }

    public RETURN_TYPE visit(BasicScenarioNode basicScenarioNode) {

        return visit((NodeWithChildren<?>) basicScenarioNode);
    }

    public RETURN_TYPE visit(OutlineScenarioNode outlineNode) {

        return visit((NodeWithChildren<?>) outlineNode);
    }

    public RETURN_TYPE visit(OutlineScenarioRowNode outlineScenarioRowNode) {

        return visit((NodeWithChildren<?>) outlineScenarioRowNode);
    }

    public RETURN_TYPE visit(SubstepNode substepNode) {

        return visit((NodeWithChildren<?>) substepNode);
    }

    public RETURN_TYPE visit(StepImplementationNode stepImplementationNode) {

        return visit((IExecutionNode) stepImplementationNode);
    }

    public RETURN_TYPE visit(NodeWithChildren<?> node) {
        return visit((IExecutionNode) node);
    }

    public RETURN_TYPE visit(IExecutionNode node) {

        return null;
    }
}
