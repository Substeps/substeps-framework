package com.technophobia.substeps.runner.description;

import com.technophobia.substeps.execution.node.IExecutionNode;
import org.junit.runner.Description;

public interface DescriptionBuilder {

    Description descriptionFor(IExecutionNode executionNode, DescriptorStatus descriptorStatus);
}
