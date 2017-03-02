package com.technophobia.substeps.mojo.runner;

import com.google.common.collect.Lists;
import com.technophobia.substeps.execution.node.IExecutionNode;
import com.technophobia.substeps.execution.node.RootNode;
import com.technophobia.substeps.runner.ExecutionConfig;
import com.technophobia.substeps.runner.SubstepsRunnerMojo;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigRenderOptions;
import org.junit.Test;
import org.substeps.report.IExecutionResultsCollector;
import org.substeps.report.IReportBuilder;

import java.io.File;
import java.lang.reflect.Field;

/**
 * Created by ian on 01/03/17.
 */
public class ToConfigTest {

    private static class TestReportBuilder implements IReportBuilder{

        @Override
        public void buildFromDirectory(File sourceDataDir) {

        }

        @Override
        public void buildFromDirectory(File sourceDataDir, File stepImplsJson) {

        }
    }

    private static class Collector implements IExecutionResultsCollector{

        @Override
        public void initOutputDirectories(RootNode rootNode) {

        }

        @Override
        public void setDataDir(File dataDir) {

        }

        @Override
        public void setPretty(boolean pretty) {

        }

        @Override
        public File getDataDir() {
            return null;
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


    @Test
    public void testMojoConfigToTypesfaeConfig() throws Exception {

        SubstepsRunnerMojo mojo = new SubstepsRunnerMojo();


        ExecutionConfig ec = new ExecutionConfig();

        ec.setDescription("description");
        ec.setFeatureFile("ff");
        ec.setDataOutputDirectory(new File("out"));
        ec.setNonFatalTags("nonfatal");
        ec.setSubStepsFileName("substeps");
        ec.setTags("@all");
        ec.setNonStrictKeywordPrecedence(new String[] {"given", "when"});
        ec.setStepImplementationClassNames(new String[] {"com.xyz.StepImpl", "com.abc.StepImpl"});
        ec.setExecutionListeners(new String[] {"org.substeps.execListener"});
        ec.setInitialisationClass(new String[] {"com.abc.Init"});


        mojo.setExecutionConfigs(Lists.newArrayList(ec));

        Field f = mojo.getClass().getSuperclass().getDeclaredField("executionResultsCollector");
        f.setAccessible(true);

        f.set(mojo, new Collector());

        Field f2 = mojo.getClass().getSuperclass().getDeclaredField("reportBuilder");
        f2.setAccessible(true);

        f2.set(mojo, new TestReportBuilder() );

        Config cfg = mojo.createExecutionConfigFromPom();




        ConfigRenderOptions options =
                ConfigRenderOptions.defaults().setComments(false).setFormatted(true).setJson(false).setOriginComments(false);


        System.out.println("********************* new cfg.root().render(): " +
        cfg.root().render(options));

    }
}
