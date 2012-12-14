package com.technophobia.substeps.execution.node;

import java.util.List;

import com.google.common.collect.Lists;
import com.technophobia.substeps.execution.ExecutionNodeVisitor;
import com.technophobia.substeps.model.Scope;


public class OutlineScenarioRowNode extends ExecutionNode {

    private final int rowIndex;
    private final BasicScenarioNode basicScenarioNode;

    public OutlineScenarioRowNode(int rowIndex, BasicScenarioNode basicScenarioNode) {

        this.rowIndex = rowIndex;
        this.basicScenarioNode = basicScenarioNode;
    }
    
    public BasicScenarioNode getBasicScenarioNode() {
        return basicScenarioNode;
    }

    @Override
    public <RETURN_TYPE> RETURN_TYPE dispatch(ExecutionNodeVisitor<RETURN_TYPE> executionNodeVisitor) {

        return executionNodeVisitor.visit(this);
    }

    @Override
    public <RETURN_TYPE> List<RETURN_TYPE> accept(ExecutionNodeVisitor<RETURN_TYPE> executionNodeVisitor) {

        List<RETURN_TYPE> toReturn = Lists.newArrayList();

        toReturn.add(executionNodeVisitor.visit(this));

        if(basicScenarioNode != null) {
            toReturn.addAll(basicScenarioNode.accept(executionNodeVisitor));
        }

        return toReturn;
    }

    @Override
    public String getDescription() {
        
        return rowIndex + " " + basicScenarioNode.getScenarioName() + ":";
    }
}
