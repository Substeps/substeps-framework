package com.technophobia.substeps.runner.description;

import com.technophobia.substeps.execution.node.IExecutionNode;

import java.util.ArrayList;
import java.util.List;

public class DescriptorStatus {

    private final List<MutableInteger> indexlist = new ArrayList<MutableInteger>();

    private static class MutableInteger {

        private int count = 0;

        public void increment() {
            count++;
        }
    }

    public DescriptorStatus() {
        indexlist.add(new MutableInteger()); // ROOT

    }

    public String getIndexStringForNode(final IExecutionNode node) {

        // is this the first time at this depth?
        if (node.getDepth() > indexlist.size()) {

            // add a new Int
            indexlist.add(new MutableInteger());
        }
        if (node.getDepth() < indexlist.size()) {

            final List<MutableInteger> delete = new ArrayList<MutableInteger>();

            for (int i = node.getDepth(); i < indexlist.size(); i++) {
                delete.add(indexlist.get(i));
            }
            indexlist.removeAll(delete);
        }

        final MutableInteger last = indexlist.get(node.getDepth() - 1);
        // increment the last one at this depth

        last.increment();

        final StringBuilder buf = new StringBuilder();
        boolean first = true;
        for (int i = 0; i < node.getDepth(); i++) {
            if (!first) {
                buf.append("-");
            }
            buf.append(indexlist.get(i).count);
            first = false;
        }

        return buf.toString();
    }

}