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

package com.technophobia.substeps.execution;

/**
 * @author ian
 */
public enum ExecutionResult {
    IGNORED(false), NOT_INCLUDED(false), NOT_RUN(false), RUNNING(false), PASSED(false), FAILED(true), NON_CRITICAL_FAILURE(false), PARSE_FAILURE(true),
    SETUP_TEARDOWN_FAILURE(true), CHILD_FAILED(true);
    // NON_CRITICAL_FAILURE is for when there is a failure but the config allows
    // this to fail without failing the build

    private boolean isFailure;

    public boolean isFailure() {
        return isFailure;
    }

    ExecutionResult(boolean isFailure) {
        this.isFailure = isFailure;
    }
}
