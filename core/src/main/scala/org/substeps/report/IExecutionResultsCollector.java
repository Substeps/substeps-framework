package org.substeps.report;

import com.technophobia.substeps.execution.node.RootNode;
import com.technophobia.substeps.runner.IExecutionListener;

import java.io.File;

/**
 * Created by ian on 24/05/16.
 */
public interface IExecutionResultsCollector extends IExecutionListener {
    void initOutputDirectories(RootNode rootNode);

    File getRootExecutionDataDirectory();
}
