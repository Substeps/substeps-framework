package com.technophobia.substeps.runner.description;

import com.technophobia.substeps.execution.node.IExecutionNode;
import com.technophobia.substeps.report.ExecutionReportBuilder;
import com.technophobia.substeps.runner.EclipseDescriptionProvider;

public abstract class AbstractDescriptionBuilder implements DescriptionBuilder{

    protected String getDescriptionForNode(final IExecutionNode node, final DescriptorStatus status) {
        final StringBuilder buf = new StringBuilder();

        ExecutionReportBuilder.buildDescriptionString(status.getIndexStringForNode(node) + ": ", node, buf);

        // TODO - think on Jenkins the report looks like the dot is being
        // interpreted as package delimiter

        return buf.toString();
    }
}
