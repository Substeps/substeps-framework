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

package com.technophobia.substeps.execution;

import java.io.Serializable;
import java.util.EnumSet;

import com.technophobia.substeps.runner.SubstepExecutionFailure;

public class ExecutionNodeResult implements Serializable {

    private static final long serialVersionUID = -1444083371334604179L;

    private ExecutionResult result = ExecutionResult.NOT_RUN;

    private Throwable thrown = null;

    private final long executionNodeId;

    private Long startedAt;
    private Long completedAt;

    private byte[] screenshot;

    private SubstepExecutionFailure substepExecutionFailure;

    public ExecutionNodeResult(final long id) {
        this.executionNodeId = id;
    }

    /**
     * @return the result
     */
    public ExecutionResult getResult() {
        return result;
    }

    /**
     * @param result
     *            the result to set
     */
    public void setResult(final ExecutionResult result) {
        this.result = result;
    }

    /**
     * @return the failureStackTrace
     */
    public Throwable getThrown() {
        return substepExecutionFailure == null ? thrown : substepExecutionFailure.getCause();
    }

    /**
     * @param failureStackTrace
     *            the failureStackTrace to set
     */
    public void setThrown(final Throwable failureStackTrace) {
        thrown = failureStackTrace;
    }

    /**
     * 
     */
    public void setFinished() {
        result = ExecutionResult.PASSED;
        recordComplete();
    }

    /**
     * 
     */
    public void setStarted() {
        result = ExecutionResult.RUNNING;
        startedAt = System.currentTimeMillis();
    }

    /**
     * @return the executionNodeId
     */
    public long getExecutionNodeId() {
        return executionNodeId;
    }

    public Long getRunningDuration() {

        return startedAt != null && completedAt != null ? completedAt - startedAt : null;
    }

    private void recordComplete() {
        completedAt = System.currentTimeMillis();
    }

    public byte[] getScreenshot() {

        return this.screenshot;
    }

    public void setScreenshot(byte[] screenshot) {

        this.screenshot = screenshot;
    }

    public void setFailure(SubstepExecutionFailure substepExecutionFailure) {

        // this is to prevent a failure from overwriting a parse or setup / tear down failure
        EnumSet<ExecutionResult> excluded = EnumSet.of(ExecutionResult.PARSE_FAILURE, ExecutionResult.SETUP_TEARDOWN_FAILURE);

        if (!excluded.contains(this.result)) {
            this.result = ExecutionResult.FAILED;
        }
        this.substepExecutionFailure = substepExecutionFailure;
    }

    public SubstepExecutionFailure getFailure() {
        return substepExecutionFailure;
    }

}
