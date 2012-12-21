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

    public abstract List<CHILD_TYPE> getChildren();

    public boolean hasChildren() {
        return this.getChildren() != null && !this.getChildren().isEmpty();
    }

    @Override
    public String toDebugString() {

        List<String> debugLines = Lists.newArrayList(super.toDebugString());
        if (getChildren() != null) {

            for (CHILD_TYPE child : getChildren()) {

                if (child != null) {

                    debugLines.add(child.toDebugString());
                }
            }
        }
        return Joiner.on("\n").join(debugLines);
    }

}