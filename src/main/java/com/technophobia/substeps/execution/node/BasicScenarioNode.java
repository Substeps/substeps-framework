package com.technophobia.substeps.execution.node;

import java.util.Iterator;
import java.util.List;

import com.google.common.collect.Lists;
import com.technophobia.substeps.execution.ExecutionNodeVisitor;
import com.technophobia.substeps.model.Scope;


public class BasicScenarioNode extends ScenarioNode {

    private final SubstepNode background;
    private final SubstepNode step;
    private final String scenarioName;
    
    public BasicScenarioNode(String scenarioName, SubstepNode background, SubstepNode step) {

        this.scenarioName = scenarioName;
        this.background = background;
        this.step = step;
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

        if(this.background != null) {
            
            toReturn.addAll(this.background.accept(executionNodeVisitor));
        }

        if(this.step != null) {
            
            toReturn.addAll(this.step.accept(executionNodeVisitor));
        }
        
        return toReturn;
    }

    @Override
    public String getDescription() {

        return scenarioName;
    }

    public String getScenarioName() {

        return scenarioName;
    }
}
