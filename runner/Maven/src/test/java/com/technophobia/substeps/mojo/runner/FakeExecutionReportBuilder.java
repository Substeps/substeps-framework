package com.technophobia.substeps.mojo.runner;

import com.technophobia.substeps.execution.node.IExecutionNode;
import com.technophobia.substeps.execution.node.RootNode;
import org.substeps.report.IExecutionResultsCollector;

import java.io.File;

/**
 * Created by ian on 05/09/16.
 */
public class FakeExecutionReportBuilder implements IExecutionResultsCollector{

    private File dataDir;
    private boolean pretty = false;

    @Override
    public void initOutputDirectories(RootNode rootNode) {
    }

    public File getDataDir() {
        return dataDir;
    }

    public boolean isPretty() {
        return pretty;
    }


    @Override
    public void setDataDir(File dataDir) {
        this.dataDir = dataDir;
    }

    @Override
    public void setPretty(boolean pretty) {
        this.pretty = pretty;
    }

    @Override
    public void onNodeFailed(IExecutionNode rootNode, Throwable cause) {

    }

    @Override
    public void onNodeStarted(IExecutionNode node) {

    }

    @Override
    public void onNodeFinished(IExecutionNode node) {

    }

    @Override
    public void onNodeIgnored(IExecutionNode node) {

    }
}
