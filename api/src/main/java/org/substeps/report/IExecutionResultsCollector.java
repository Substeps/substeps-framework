package org.substeps.report;

import com.technophobia.substeps.execution.node.RootNode;
import com.technophobia.substeps.runner.IExecutionListener;

import java.io.File;
import java.io.Serializable;

/**
 * Created by ian on 24/05/16.
 */
public interface IExecutionResultsCollector extends IExecutionListener, Serializable {
    void initOutputDirectories(RootNode rootNode);

    void setDataDir(File dataDir);
    void setPretty(boolean pretty);

    File getDataDir();
}
