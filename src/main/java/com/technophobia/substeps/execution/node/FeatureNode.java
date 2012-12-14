package com.technophobia.substeps.execution.node;

import java.util.List;

import com.google.common.collect.Lists;
import com.technophobia.substeps.execution.ExecutionNodeVisitor;
import com.technophobia.substeps.execution.Feature;
import com.technophobia.substeps.model.Scope;

public class FeatureNode extends ExecutionNode {

    private final List<ScenarioNode> scenarios;
    private final Feature feature;

    public FeatureNode(Feature feature, List<ScenarioNode> scenarios) {
        
        this.feature = feature;
        this.scenarios = scenarios;
    }

    public List<ScenarioNode> getScenarios() {

        return scenarios;
    }

    @Override
    public <RETURN_TYPE> RETURN_TYPE dispatch(ExecutionNodeVisitor<RETURN_TYPE> executionNodeVisitor) {

        return executionNodeVisitor.visit(this);
    }

    @Override
    public <RETURN_TYPE> List<RETURN_TYPE> accept(ExecutionNodeVisitor<RETURN_TYPE> executionNodeVisitor) {

        List<RETURN_TYPE> toReturn = Lists.newArrayList();

        toReturn.add(executionNodeVisitor.visit(this));

        for(ScenarioNode scenario : scenarios) {
            
            toReturn.addAll(scenario.accept(executionNodeVisitor));
        }

        return toReturn;
    }

    @Override
    public String getDescription() {

        return feature.getName();
    }
}
