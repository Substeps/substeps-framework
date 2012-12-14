package com.technophobia.substeps.execution.node;

import java.util.List;

import com.google.common.collect.Lists;
import com.technophobia.substeps.execution.ExecutionNodeVisitor;
import com.technophobia.substeps.model.Scope;


public class OutlineScenarioNode extends ScenarioNode {

    private final List<OutlineScenarioRowNode> outlineRows;
    private final String scenarioName;

    public OutlineScenarioNode(String scenarioName, List<OutlineScenarioRowNode> outlineRows) {

        this.scenarioName = scenarioName;
        this.outlineRows = outlineRows;
    }

    public List<OutlineScenarioRowNode> getOutlineRows() {

        return outlineRows;
    }

    @Override
    public <RETURN_TYPE> RETURN_TYPE dispatch(ExecutionNodeVisitor<RETURN_TYPE> executionNodeVisitor) {

        return executionNodeVisitor.visit(this);
    }

    @Override
    public <RETURN_TYPE> List<RETURN_TYPE> accept(ExecutionNodeVisitor<RETURN_TYPE> executionNodeVisitor) {

        List<RETURN_TYPE> toReturn = Lists.newArrayList();

        toReturn.add(executionNodeVisitor.visit(this));

        for(OutlineScenarioRowNode outlineRow : outlineRows) {
            
            toReturn.addAll(outlineRow.accept(executionNodeVisitor));
        }

        return toReturn;
    }

    @Override
    public String getDescription() {

        return "Scenario #: " + scenarioName;
    }
}
