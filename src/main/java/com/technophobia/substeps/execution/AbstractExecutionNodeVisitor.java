package com.technophobia.substeps.execution;

import com.technophobia.substeps.execution.node.BasicScenarioNode;
import com.technophobia.substeps.execution.node.ExecutionNode;
import com.technophobia.substeps.execution.node.FeatureNode;
import com.technophobia.substeps.execution.node.OutlineScenarioNode;
import com.technophobia.substeps.execution.node.OutlineScenarioRowNode;
import com.technophobia.substeps.execution.node.RootNode;
import com.technophobia.substeps.execution.node.StepImplementationNode;
import com.technophobia.substeps.execution.node.SubstepNode;

/**
 * Allows a concrete visitor to override only methods which it is interested in, also provides
 * 
 * visit(ExecutionNode) which can be overridden for default behaviour affecting any node.
 * 
 * 
 * 
 * 
 * @author rbarefield
 *
 */
public abstract class AbstractExecutionNodeVisitor<RETURN_TYPE> implements ExecutionNodeVisitor<RETURN_TYPE> {

    public RETURN_TYPE visit(RootNode rootNode) {

        return visit((ExecutionNode)rootNode);
    }

    public RETURN_TYPE visit(FeatureNode featureNode) {

        return visit((ExecutionNode)featureNode);
    }

    public RETURN_TYPE visit(BasicScenarioNode basicScenarioNode) {
        
        return visit((ExecutionNode)basicScenarioNode);
    }

    public RETURN_TYPE visit(OutlineScenarioNode outlineNode) {
        
        return visit((ExecutionNode)outlineNode);
    }
    
    public RETURN_TYPE visit(OutlineScenarioRowNode outlineScenarioRowNode) {
        
        return visit((ExecutionNode)outlineScenarioRowNode);
    }

    public RETURN_TYPE visit(SubstepNode substepNode) {
        
        return visit((ExecutionNode)substepNode);
    }

    public RETURN_TYPE visit(StepImplementationNode stepImplementationNode) {
        
        return visit((ExecutionNode)stepImplementationNode);
    }

    public RETURN_TYPE visit(ExecutionNode node) {
        
        return null;
    }
}
