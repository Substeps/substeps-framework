package com.technophobia.substeps.runner;

import java.util.List;

import org.apache.maven.plugin.logging.Log;

import com.technophobia.substeps.execution.ExecutionNode;

public class InProcessRunner implements MojoRunner {

    SubstepsRunner executionNodeRunner = ExecutionNodeRunnerFactory.createRunner();
    private Log log;

    InProcessRunner(Log log) {

        this.log = log;
    }

    @Override
    public List<SubstepExecutionFailure> run() {
        log.info("Running substeps tests in process");
        return executionNodeRunner.run();
    }

    @Override
    public void prepareExecutionConfig(SubstepsExecutionConfig theConfig) {

        executionNodeRunner.prepareExecutionConfig(theConfig);
    }

    @Override
    public ExecutionNode getRootNode() {
        return executionNodeRunner.getRootNode();
    }

    @Override
    public void addNotifier(INotifier notifier) {
        executionNodeRunner.addNotifier(notifier);
    }

    @Override
    public void shutdown() {
        // nop
    }

}
