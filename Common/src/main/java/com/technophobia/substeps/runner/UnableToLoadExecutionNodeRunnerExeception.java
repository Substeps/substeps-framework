package com.technophobia.substeps.runner;

/**
 * Indicates the ExecutionNodeRunner could not be loaded due to the underlying
 * exception
 * 
 * @author rbarefield
 * 
 */
public class UnableToLoadExecutionNodeRunnerExeception extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public UnableToLoadExecutionNodeRunnerExeception(Throwable cause) {
        super("Unable to load an ExecutionNodeRunner, is there a version of substeps-core on the classpath", cause);
    }

}
