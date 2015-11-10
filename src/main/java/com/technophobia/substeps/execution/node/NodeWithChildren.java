/*
 *	Copyright Technophobia Ltd 2012
 *
 *   This file is part of Substeps.
 *
 *    Substeps is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU Lesser General Public License as published by
 *    the Free Software Foundation, either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    Substeps is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public License
 *    along with Substeps.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.technophobia.substeps.execution.node;

import java.util.List;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

public abstract class NodeWithChildren<CHILD_TYPE extends IExecutionNode> extends ExecutionNode {

    private static final long serialVersionUID = 1L;

    private final List<CHILD_TYPE> children;

    public NodeWithChildren(List<CHILD_TYPE> children){
        this.children = children;

        attachParentTo(children);
    }

    public boolean hasChildren() {
        return children != null && !children.isEmpty();
    }

    public List<CHILD_TYPE> getChildren(){
        return children;
    }

    @Override
    public String toDebugString() {

        List<String> debugLines = Lists.newArrayList(super.toDebugString());
        if (children != null) {

            for (CHILD_TYPE child : children) {

                if (child != null) {

                    debugLines.add(child.toDebugString());
                }
            }
        }
        return Joiner.on("\n").join(debugLines);
    }

    private void attachParentTo(List<CHILD_TYPE> children) {
        for (CHILD_TYPE child : children){
            child.setParent(this);
        }
    }
}