package com.technophobia.substeps.execution;

import com.technophobia.substeps.execution.node.BasicScenarioNode;
import com.technophobia.substeps.execution.node.FeatureNode;
import com.technophobia.substeps.execution.node.OutlineScenarioNode;
import com.technophobia.substeps.execution.node.OutlineScenarioRowNode;
import com.technophobia.substeps.execution.node.RootNode;
import com.technophobia.substeps.execution.node.StepImplementationNode;
import com.technophobia.substeps.execution.node.SubstepNode;

public interface ExecutionNodeVisitor<RETURN_TYPE> {

    RETURN_TYPE visit(RootNode rootNode);

    RETURN_TYPE visit(FeatureNode featureNode);

    RETURN_TYPE visit(BasicScenarioNode basicScenarioNode);

    RETURN_TYPE visit(OutlineScenarioNode outlineNode);
    
    RETURN_TYPE visit(OutlineScenarioRowNode outlineScenarioRowNode);

    RETURN_TYPE visit(SubstepNode substepNode);

    RETURN_TYPE visit(StepImplementationNode stepImplementationNode);
    
}
