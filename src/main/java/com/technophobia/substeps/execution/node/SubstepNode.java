package com.technophobia.substeps.execution.node;

import java.util.List;

import com.google.common.collect.Lists;
import com.technophobia.substeps.execution.ExecutionNodeVisitor;


public class SubstepNode extends StepNode {

    private final List<StepNode> substeps;

    public SubstepNode(List<StepNode> substeps) {

        this.substeps = substeps;
    }

    public List<StepNode> getSubsteps() {

        return substeps;
    }
    
    @Override
    public <RETURN_TYPE> RETURN_TYPE dispatch(ExecutionNodeVisitor<RETURN_TYPE> executionNodeVisitor) {

        return executionNodeVisitor.visit(this);
    }

    @Override
    public <RETURN_TYPE> List<RETURN_TYPE> accept(ExecutionNodeVisitor<RETURN_TYPE> executionNodeVisitor) {

        List<RETURN_TYPE> toReturn = Lists.newArrayList();

        toReturn.add(executionNodeVisitor.visit(this));

        for(StepNode stepNode : substeps) {
            
            toReturn.addAll(stepNode.accept(executionNodeVisitor));
        }

        return toReturn;
    }

    @Override
    public String getDescription() {

        return "Substep";
    }
}
