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

package com.technophobia.substeps.runner;

import java.io.Serializable;

import com.google.common.base.Function;
import com.technophobia.substeps.execution.ExecutionResult;
import com.technophobia.substeps.execution.node.IExecutionNode;
import com.technophobia.substeps.execution.node.RootNode;

/**
 * Represents the failure of an execution - could be a step method, or a setup method, may or may not be critical
 *
 * @author ian
 */
public class SubstepExecutionFailure implements Serializable {

    public static final Function<SubstepExecutionFailure, Long> GET_NODE_ID = new Function<SubstepExecutionFailure, Long>() {

        public Long apply(final SubstepExecutionFailure failure) {
            return failure.getExeccutionNode() == null ? null : failure.getExeccutionNode().getId();
        }

    };

    private static final long serialVersionUID = 4981517213059529046L;

    private transient final Throwable cause;
    private IExecutionNode executionNode;
    private boolean setupOrTearDown = false;
    private boolean nonCritical = false;

    private byte[] screenshot;

    private final ThrowableInfo throwableInfo;


    public SubstepExecutionFailure(final Throwable cause) {

        this.cause = cause;

        this.throwableInfo = new ThrowableInfo(cause);
    }


    /**
     * @param cause the cause of the failure
     * @param node  the node that failed execution
     */
    public SubstepExecutionFailure(final Throwable cause, final IExecutionNode node) {
        this.cause = cause;
        this.executionNode = node;
        this.executionNode.getResult().setFailure(this);
        this.throwableInfo = new ThrowableInfo(cause);

    }


    /**
     * @param cause      the cause of the failure
     * @param node       the node that failed execution
     * @param screenshot the bytes representing the screenshot taken when the node execution failed
     */
    public SubstepExecutionFailure(final Throwable cause, final IExecutionNode node, final byte[] screenshot) {
        this(cause, node);
        this.setScreenshot(screenshot);
    }


    /**
     * @param cause           the cause of the failure
     * @param node            the node that failed execution
     * @param setupOrTearDown thrown during setup or tear down phase
     */
    public SubstepExecutionFailure(final Throwable cause, final IExecutionNode node, final boolean setupOrTearDown) {
        this(cause, node);
        this.setupOrTearDown = setupOrTearDown;
    }


    /**
     * @param cause  the cause of the failure
     * @param node   the node that failed execution
     * @param result the execution result
     */
    public SubstepExecutionFailure(final Throwable cause, final IExecutionNode node, final ExecutionResult result) {
        this(cause, node);
        node.getResult().setResult(result);
    }


    /**
     * @return the execcutionNode
     */
    public IExecutionNode getExeccutionNode() {
        return this.executionNode;
    }


    /**
     * @param execcutionNode the execcutionNode to set
     */
    public void setExeccutionNode(final IExecutionNode execcutionNode) {
        this.executionNode = execcutionNode;
    }


    /**
     * @return the setupOrTearDown
     */
    public boolean isSetupOrTearDown() {
        return this.setupOrTearDown;
    }


    /**
     * @param setupOrTearDown the setupOrTearDown to set
     */
    public void setSetupOrTearDown(final boolean setupOrTearDown) {
        this.setupOrTearDown = setupOrTearDown;
    }


    /**
     * @return the cause
     */
    public Throwable getCause() {
        return this.cause;
    }


    public ThrowableInfo getThrowableInfo() {
        return this.throwableInfo;
    }


    /**
     * @param isNonCritical is the error non critical
     */
    public void setNonCritical(final boolean isNonCritical) {
        this.nonCritical = isNonCritical;

    }


    /**
     * @return the nonCritical
     */
    public boolean isNonCritical() {
        return this.nonCritical;
    }


    public byte[] getScreenshot() {
        return screenshot;
    }


    public void setScreenshot(final byte[] screenshot) {
        this.screenshot = screenshot;
    }
}
