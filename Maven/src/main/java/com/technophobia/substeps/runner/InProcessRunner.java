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
