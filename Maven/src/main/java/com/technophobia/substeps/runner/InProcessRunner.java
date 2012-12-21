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

import java.util.List;

import org.apache.maven.plugin.logging.Log;

import com.technophobia.substeps.execution.node.RootNode;

public class InProcessRunner implements MojoRunner {

    SubstepsRunner executionNodeRunner = ExecutionNodeRunnerFactory.createRunner();
    private final Log log;

    InProcessRunner(Log log) {

        this.log = log;
    }

    public RootNode run() {
        log.info("Running substeps tests in process");
        return executionNodeRunner.run();
    }

    public RootNode prepareExecutionConfig(SubstepsExecutionConfig theConfig) {

        return executionNodeRunner.prepareExecutionConfig(theConfig);
    }

    public List<SubstepExecutionFailure> getFailures() {
        return executionNodeRunner.getFailures();
    }

    public void addNotifier(INotifier notifier) {
        executionNodeRunner.addNotifier(notifier);
    }

    public void shutdown() {
        // nop
    }

}
