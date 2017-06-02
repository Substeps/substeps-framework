package com.technophobia.substeps.mojo.runner;

import com.google.common.collect.Lists;
import com.google.common.io.Files;
import com.technophobia.substeps.execution.node.IExecutionNode;
import com.technophobia.substeps.execution.node.RootNode;
import com.technophobia.substeps.runner.ExecutionConfig;
import com.technophobia.substeps.runner.SubstepsRunnerMojo;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.model.Build;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.junit.Assert;
import org.junit.Test;
import org.substeps.report.IExecutionResultsCollector;
import org.substeps.report.IReportBuilder;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.Charset;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by ian on 01/03/17.
 */
public class ToConfigTest {

//    private static class TestReportBuilder implements IReportBuilder{
//
//        @Override
//        public void buildFromDirectory(File sourceDataDir) {
//
//        }
//
//        @Override
//        public void buildFromDirectory(File sourceDataDir, File stepImplsJson) {
//
//        }
//    }

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

        MavenProject project = mock(MavenProject.class);

        when (project.getBasedir()).thenReturn(new File("."));
        Build build = mock(Build.class);
        when(build.getTestOutputDirectory()).thenReturn("testout");
        when(build.getOutputDirectory()).thenReturn("out");
        when(build.getDirectory()).thenReturn("dir");

        when(project.getBuild()).thenReturn(build);
        mojo.setProject(project);
        mojo.setJmxPort(9999);



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

        IReportBuilder reportBuilder = mock(IReportBuilder.class);

        f2.set(mojo, reportBuilder );

        try {
            mojo.execute();
            Assert.fail("mojo should have thrown an exception");
        }
        catch (MojoExecutionException e){

            // pass
            System.out.println("msg: " + e.getMessage());


            System.out.println("long msg\n" + e.getLongMessage());

        }

    }

}
