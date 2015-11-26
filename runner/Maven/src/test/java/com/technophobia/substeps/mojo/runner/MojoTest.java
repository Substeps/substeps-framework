/*
 *	Copyright Technophobia Ltd 2012
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

import java.io.File;
import java.lang.reflect.Field;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;

import com.technophobia.substeps.report.ExecutionReportBuilder;
import com.technophobia.substeps.runner.ExecutionConfig;
import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.apache.maven.plugin.testing.MojoRule;
import org.junit.Assert;
import org.junit.Ignore;
import static org.hamcrest.Matchers.*;


import com.technophobia.substeps.runner.SubstepsRunnerMojo;
import org.junit.Rule;
import org.junit.Test;

/**
 * @author ian
 * 
 */
public class MojoTest extends AbstractMojoTestCase {

    protected void setUp() throws Exception {
        // required for mojo lookups to work
        super.setUp();
    }

    public void testMojoGoal() throws Exception {

        File testPom = new File( getBasedir(),
                "src/test/resources/sample-pom.xml" );

        final SubstepsRunnerMojo mojo = (SubstepsRunnerMojo) lookupMojo("run-features", testPom);

         Assert.assertNotNull("expecting a mojo", mojo);

        ExecutionConfig executionConfig = mojo.getExecutionConfigs().get(0);
        Assert.assertNotNull(executionConfig);

        Assert.assertThat(executionConfig, is (notNullValue()));

        Assert.assertThat(executionConfig.getDescription(), is ("Self Test Features"));

        Assert.assertThat(executionConfig.getTags(), is("@non-visual"));
        Assert.assertThat(executionConfig.isFastFailParseErrors(), is(false));
        Assert.assertThat(executionConfig.getFeatureFile(), is("/target/test-classes/features"));
        Assert.assertThat(executionConfig.getSubStepsFileName(), is("/target/test-classes/substeps"));

        Assert.assertThat(executionConfig.getStepImplementationClassNames(), arrayContaining("com.technophobia.webdriver.substeps.impl.BaseWebdriverSubStepImplementations"));

        Assert.assertThat(executionConfig.getExecutionListeners(), arrayContaining("com.technophobia.substeps.runner.logger.StepExecutionLogger"));

        ExecutionReportBuilder executionReportBuilder = mojo.getExecutionReportBuilder();

        Assert.assertNotNull(executionReportBuilder);
        Assert.assertThat(executionReportBuilder, instanceOf(StubExecutionReportBuilder.class));

        StubExecutionReportBuilder stub = (StubExecutionReportBuilder)executionReportBuilder;

        Assert.assertThat(stub.getReportTitle(), is("sample pom - report title"));

        Assert.assertThat(stub.getOutputDirectory(), is(new File("/some/folder")));

        Assert.assertThat(mojo.isRunTestsInForkedVM(), is(false));
    }
}
