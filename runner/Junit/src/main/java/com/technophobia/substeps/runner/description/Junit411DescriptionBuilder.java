package com.technophobia.substeps.runner.description;

import com.technophobia.substeps.execution.node.IExecutionNode;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;

public class Junit411DescriptionBuilder extends AbstractReflectiveDescriptionBuilder {

    protected Class<?>[] constructorParameterTypes() {
        return new Class<?>[]{Class.class, String.class, Array.newInstance(Annotation.class, 0).getClass()};
    }

    protected Object[] constructorArguments(IExecutionNode node, DescriptorStatus status) {
        return new Object[]{null, getDescriptionForNode(node, status), new Annotation[0]};
    }
}
