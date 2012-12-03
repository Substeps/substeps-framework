package com.technophobia.substeps.runner;

public final class ExecutionNodeRunnerFactory {

    private static final String EXECUTION_NODE_RUNNER_CLASSNAME = "com.technophobia.substeps.runner.ExecutionNodeRunner";

    private ExecutionNodeRunnerFactory() {
        // This class should not be instantiated
    }

    public static SubstepsRunner createRunner() {

        try {
            return (SubstepsRunner) Class.forName(EXECUTION_NODE_RUNNER_CLASSNAME).newInstance();
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
            throw new UnableToLoadExecutionNodeRunnerExeception(e);
        }
    }
}
