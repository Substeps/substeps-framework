/*
 *  Copyright Technophobia Ltd 2012
 *
 *   This file is part of Substeps Maven Runner.
 *
 *    Substeps Maven Runner is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU Lesser General Public License as published by
 *    the Free Software Foundation, either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    Substeps Maven Runner is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public License
 *    along with Substeps.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.technophobia.substeps.mojo.runner;

import com.technophobia.substeps.report.ExecutionReportBuilder;
import com.technophobia.substeps.runner.ExecutionConfig;
import com.technophobia.substeps.runner.SubstepsReportBuilderMojo;
import com.technophobia.substeps.runner.SubstepsRunnerMojo;
import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.codehaus.plexus.configuration.PlexusConfiguration;
import org.junit.Assert;

import java.io.File;

import static org.hamcrest.Matchers.*;

/**
 * @author ian
 */
public class SubstepsRunnerMojoConfigTest extends AbstractMojoTestCase {

    public void setUp() throws Exception {
        // required for mojo lookups to work
        super.setUp();
    }

    public void testMojoGoal() throws Exception {

        File testPom = new File(getBasedir(),
                "src/test/resources/sample-pom.xml");

        Assert.assertNotNull(testPom);
        Assert.assertTrue(testPom.exists());

        PlexusConfiguration pluginConfiguration = this.extractPluginConfiguration("substeps-maven-plugin", testPom);
        final SubstepsRunnerMojo mojo = (SubstepsRunnerMojo)lookupMojo("org.substeps", "substeps-maven-plugin", "1.0.2-SNAPSHOT", "run-features", pluginConfiguration);

        Assert.assertNotNull("expecting a mojo", mojo);

        ExecutionConfig executionConfig = mojo.getExecutionConfigs().get(0);
        Assert.assertNotNull(executionConfig);

        Assert.assertThat(executionConfig, is(notNullValue()));

        Assert.assertThat(executionConfig.getDescription(), is("Self Test Features"));

        Assert.assertThat(executionConfig.getTags(), is("@non-visual"));
        Assert.assertThat(executionConfig.isFastFailParseErrors(), is(false));
        Assert.assertThat(executionConfig.getFeatureFile(), is("/target/test-classes/features"));
        Assert.assertThat(executionConfig.getSubStepsFileName(), is("/target/test-classes/substeps"));

        Assert.assertThat(executionConfig.getStepImplementationClassNames(), arrayContaining("com.technophobia.webdriver.substeps.impl.BaseWebdriverSubStepImplementations"));

        Assert.assertThat(executionConfig.getExecutionListeners(), arrayContaining("com.technophobia.substeps.runner.logger.StepExecutionLogger"));

        ExecutionReportBuilder executionReportBuilder = mojo.getExecutionReportBuilder();

        Assert.assertNotNull(executionReportBuilder);
        Assert.assertThat(executionReportBuilder, instanceOf(StubExecutionReportBuilder.class));

        StubExecutionReportBuilder stub = (StubExecutionReportBuilder) executionReportBuilder;

        Assert.assertThat(stub.getReportTitle(), is("sample pom - report title"));

        Assert.assertThat(stub.getOutputDirectory(), is(new File("/some/folder")));

        Assert.assertThat(mojo.isRunTestsInForkedVM(), is(false));

        Assert.assertThat((FakeExecutionReportBuilder) mojo.getExecutionResultsCollector(), isA(FakeExecutionReportBuilder.class));

        Assert.assertThat( ((FakeExecutionReportBuilder) mojo.getExecutionResultsCollector()).isPretty(), is(false));

        Assert.assertThat( ((FakeExecutionReportBuilder) mojo.getExecutionResultsCollector()).getDataDir(), is(new File("/home/somewhere")));


        SubstepsReportBuilderMojo mojo2 = (SubstepsReportBuilderMojo)lookupMojo("org.substeps", "substeps-maven-plugin", "1.0.2-SNAPSHOT", "build-report", pluginConfiguration);

        Assert.assertNotNull("expecting another mojo", mojo2);

        Assert.assertThat((FakeExecutionReportBuilder) mojo2.getExecutionResultsCollector(), isA(FakeExecutionReportBuilder.class));

        Assert.assertThat( ((FakeExecutionReportBuilder) mojo2.getExecutionResultsCollector()).isPretty(), is(false));

        Assert.assertThat( ((FakeExecutionReportBuilder) mojo2.getExecutionResultsCollector()).getDataDir(), is(new File("/home/somewhere")));

        Assert.assertThat((FakeReportBuilder) mojo2.getReportBuilder(), isA(FakeReportBuilder.class));

    }
}
