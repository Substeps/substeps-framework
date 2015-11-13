package com.technophobia.substeps.runner;

import com.google.common.base.Strings;
import com.technophobia.substeps.execution.AbstractExecutionNodeVisitor;
import com.technophobia.substeps.execution.node.IExecutionNode;
import com.technophobia.substeps.execution.node.NodeWithChildren;

public class TreePrinter extends AbstractExecutionNodeVisitor<Void> {

    String asString = "";

    public static String asString(IExecutionNode executionNode) {

        TreePrinter treePrinter = new TreePrinter();
        executionNode.dispatch(treePrinter);
        return treePrinter.asString;
    }

    private TreePrinter() {
        // Private cons
    }

    @Override
    public Void visit(IExecutionNode node) {

        asString += "\n";
        return null;
    }

    @Override
    public Void visit(NodeWithChildren<?> nodeWithChildren) {

        visit((IExecutionNode) nodeWithChildren);

        for (IExecutionNode childNode : nodeWithChildren.getChildren()) {
            asString += Strings.repeat("\t", nodeWithChildren.getDepth());
            childNode.dispatch(this);
            asString += "\n";
        }
        return null;
    }

}
