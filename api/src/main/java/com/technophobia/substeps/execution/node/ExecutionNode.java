/*
 *  Copyright Technophobia Ltd 2012
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

import com.google.common.base.Strings;
import com.technophobia.substeps.execution.ExecutionNodeResult;
import com.technophobia.substeps.execution.ExecutionNodeVisitor;
import com.technophobia.substeps.execution.ExecutionResult;

import java.io.File;
import java.io.Serializable;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * represents a node on the tree of features, scenarios, substeps etc including
 * outlines and backgrounds
 *
 * @author ian
 */
public abstract class ExecutionNode implements Serializable, IExecutionNode {

    private static final long serialVersionUID = 1L;

    private static transient AtomicLong counter = new AtomicLong(1);

    private final long id; // for uniqueness

    /**
     * An {@link ExecutionNode} can be seen as compiled substeps code - ready to
     * run. We include the fileUri and line number to tie this back to the
     * substeps source - the files from which the compiled substeps were
     * generated. This information could be seen as debug information - however
     * it is useful in other places - for example in editor plugins where we
     * have the compiled code but need to show where it came from to the user.
     */
    private String fileUri;
    private int lineNumber;

    private int depth = 0;

    private String line;

    private IExecutionNode parent;

    private final ExecutionNodeResult result;


    public ExecutionNode() {
        this.id = counter.getAndIncrement();
        this.result = new ExecutionNodeResult(this.id);
        this.parent = null;
    }


    /**
     * @return the depth
     */
    @Override
    public int getDepth() {
        return this.depth;
    }


    /**
     * @param depth the depth to set
     */
    @Override
    public void setDepth(final int depth) {
        this.depth = depth;
    }


    /**
     * @return the id
     */
    @Override
    public long getId() {
        return this.id;
    }


    /**
     * @return the line
     */
    @Override
    public String getLine() {
        return this.line;
    }


    /**
     * @param line the line to set
     */
    @Override
    public void setLine(final String line) {
        this.line = line;
    }

    @Override
    public Long getLongId() {
        return Long.valueOf(this.id);
    }

    @Override
    public void setParent(final IExecutionNode parent) {
        this.parent = parent;
    }


    /**
     * @return The parent of this node
     */
    @Override
    public IExecutionNode getParent() {
        return parent;
    }


    /**
     * @return the result
     */
    @Override
    public ExecutionNodeResult getResult() {
        return this.result;
    }


    /**
     * @return the filename
     */
    @Override
    public String getFilename() {
        return new File(getFileUri()).getName();
    }

    @Override
    public String getFileUri() {

        return this.fileUri != null ? this.fileUri : "";
    }

    @Override
    public void setFileUri(final String fileUri) {
        this.fileUri = fileUri;
    }

    @Override
    public int getLineNumber() {
        return this.lineNumber;
    }

    @Override
    public void setLineNumber(final int lineNumber) {
        this.lineNumber = lineNumber;
    }


    @Override
    public boolean hasError() {
        return result.getResult() == ExecutionResult.FAILED || result.getResult() == ExecutionResult.PARSE_FAILURE;
    }

    @Override
    public boolean hasPassed() {
        return this.result.getResult() == ExecutionResult.PASSED;
    }

    @Override
    public abstract String getDescription();

    @Override
    public abstract <RETURN_TYPE> RETURN_TYPE dispatch(ExecutionNodeVisitor<RETURN_TYPE> executionNodeVisitor);

    @Override
    public abstract <RETURN_TYPE> List<RETURN_TYPE> accept(ExecutionNodeVisitor<RETURN_TYPE> executionNodeVisitor);

    @Override
    public String toDebugString() {
        String debugString = Strings.repeat("\t", getDepth());
        debugString +=
                "id: " + getId() + ", type: " + getClass().getSimpleName() + ", description: " + getDescription();
        return debugString;
    }

}
