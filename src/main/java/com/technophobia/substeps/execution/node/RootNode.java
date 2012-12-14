package com.technophobia.substeps.execution.node;

import java.util.List;

import com.google.common.collect.Lists;
import com.technophobia.substeps.execution.ExecutionNodeVisitor;
import com.technophobia.substeps.execution.Feature;

public class RootNode extends ExecutionNode {

    private final List<FeatureNode> features;
    
    public RootNode(List<FeatureNode> features) {
        
        this.features = features;
    }

    public List<FeatureNode> getFeatures() {
        
        return features;
    }

    @Override
    public <RETURN_TYPE> RETURN_TYPE dispatch(ExecutionNodeVisitor<RETURN_TYPE> executionNodeVisitor) {

        return executionNodeVisitor.visit(this);
    }

    @Override
    public <RETURN_TYPE> List<RETURN_TYPE> accept(ExecutionNodeVisitor<RETURN_TYPE> executionNodeVisitor) {

        List<RETURN_TYPE> toReturn = Lists.newArrayList();

        toReturn.add(executionNodeVisitor.visit(this));

        for(FeatureNode feature : features) {
            
            toReturn.addAll(feature.accept(executionNodeVisitor));
        }

        return toReturn;
    }

    @Override
    public String getDescription() {

        return "executionNodeRoot";
    }
    
}
