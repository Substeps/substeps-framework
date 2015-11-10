package com.technophobia.substeps.ant;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.technophobia.substeps.execution.node.RootNode;
import com.technophobia.substeps.report.ExecutionReportBuilder;
import com.technophobia.substeps.runner.BuildFailureManager;
import com.technophobia.substeps.runner.ExecutionNodeRunnerFactory;
import com.technophobia.substeps.runner.SubstepExecutionFailure;
import com.technophobia.substeps.runner.SubstepsExecutionConfig;
import com.technophobia.substeps.runner.SubstepsRunner;

public class SubStepsTask extends Task {

    private final Logger log = LoggerFactory.getLogger(SubStepsTask.class);
    private final List<AntExecutionConfig> configs = new ArrayList<AntExecutionConfig>();
    private ExecutionReportBuilder executionReportBuilder = null;
    private String outputDir;
    private static final String REPORT_DIR_DEFAULT = ".";

    @Override
    public void execute() throws BuildException {
        final BuildFailureManager buildFailureManager = new BuildFailureManager();

        List<SubstepsExecutionConfig> configs = new ArrayList<SubstepsExecutionConfig>();
        for (AntExecutionConfig c : this.configs) {
            configs.add(c);
        }

        executeInternal(buildFailureManager, configs);
    }

    public void addConfiguredExecutionConfig(AntExecutionConfig config) {
        this.configs.add(config);
    }

    public void setOutputDir(String outputDir) {
        this.outputDir = outputDir;
    }

    private void executeInternal(final BuildFailureManager buildFailureManager,
            final List<SubstepsExecutionConfig> executionConfigList) throws RuntimeException {

        Assert.assertNotNull("executionConfigs cannot be null", executionConfigList);
        Assert.assertFalse("executionConfigs can't be empty", executionConfigList.isEmpty());

        executionReportBuilder = ExecutionReportBuilder.createDefaultExecutionReportBuilder();

        executionReportBuilder
                .setOutputDirectory(new File(this.outputDir == null ? REPORT_DIR_DEFAULT : this.outputDir));

        for (final SubstepsExecutionConfig executionConfig : executionConfigList) {
            final List<SubstepExecutionFailure> failures = new ArrayList<SubstepExecutionFailure>();
            final RootNode rootNode = runExecutionConfig(executionConfig, failures);

            if (executionConfig.getDescription() != null) {
                rootNode.setLine(executionConfig.getDescription());
            }

            buildFailureManager.addExecutionResult(rootNode);

            executionReportBuilder.addRootExecutionNode(rootNode);
        }

        executionReportBuilder.buildReport();

        if (buildFailureManager.testSuiteFailed()) {
            throw new RuntimeException("Substep Execution failed:\n" + buildFailureManager.getBuildFailureInfo());

        } else if (!buildFailureManager.testSuiteCompletelyPassed()) {
            // print out the failure string (but won't include any failures)
            log.info(buildFailureManager.getBuildFailureInfo());
        }
        // else - we're all good

    }

    private RootNode runExecutionConfig(final SubstepsExecutionConfig theConfig,
            final List<SubstepExecutionFailure> failures) {

        final SubstepsRunner runner = ExecutionNodeRunnerFactory.createRunner();
        runner.prepareExecutionConfig(theConfig);
        final RootNode rootNode = runner.run();
        final List<SubstepExecutionFailure> localFailures = runner.getFailures();
        failures.addAll(localFailures);
        return rootNode;
    }
}
