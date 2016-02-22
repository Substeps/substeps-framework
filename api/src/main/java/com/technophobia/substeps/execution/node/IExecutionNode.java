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

import java.io.Serializable;
import java.util.List;

import com.technophobia.substeps.execution.ExecutionNodeResult;
import com.technophobia.substeps.execution.ExecutionNodeVisitor;

public interface IExecutionNode extends Serializable{

    /**
     * @return the depth of this node in the tree
     */
    int getDepth();

    void setDepth(final int depth);

    /**
     * @return the id
     */
    long getId();

    /**
     * @return the line
     */
    String getLine();

    /**
     * @param line
     *            the line to set
     */
    void setLine(final String line);

    IExecutionNode getParent();

    void setParent(IExecutionNode parent);

    /**
     * 
     * @return the id of this node as a Long
     */
    Long getLongId();

    /**
     * @return the result
     */
    ExecutionNodeResult getResult();

    /**
     * @return the filename
     */
    String getFilename();

    String getFileUri();

    void setFileUri(final String fileUri);

    int getLineNumber();

    void setLineNumber(final int lineNumber);

    boolean hasError();

    boolean hasPassed();

    String toDebugString();

    String getDescription();

    <RETURN_TYPE> RETURN_TYPE dispatch(ExecutionNodeVisitor<RETURN_TYPE> executionNodeVisitor);

    <RETURN_TYPE> List<RETURN_TYPE> accept(ExecutionNodeVisitor<RETURN_TYPE> executionNodeVisitor);
}