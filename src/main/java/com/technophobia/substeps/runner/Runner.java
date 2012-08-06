package com.technophobia.substeps.runner;

import com.technophobia.substeps.execution.ExecutionNode;


/**
 * @author ian
 *
 */
public class Runner
{
	 /**
     * @param notifier
     * @return
     */
    public ExecutionNode runExecutionConfig(final ExecutionConfig theConfig) {

        final ExecutionNodeRunner runner = new ExecutionNodeRunner();

        // TODO - If we want to have some fedback of nodes starting / passing /
        // failing etc then we could add
        // and INotifier to runner to receive call backs

        final ExecutionNode rootNode = runner.prepareExecutionConfig(theConfig);

        runner.run();

        return rootNode;
    }

}
