/*
 *	Copyright Technophobia Ltd 2012
 *
 *   This file is part of Substeps.
 *
 *    Substeps is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU Lesser General Public License as published by
 *    the Free Software Foundation, either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    Substeps is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public License
 *    along with Substeps.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.technophobia.substeps.runner;

import com.technophobia.substeps.execution.ExecutionResult;
import com.technophobia.substeps.execution.Feature;
import com.technophobia.substeps.execution.node.RootNode;
import com.technophobia.substeps.execution.node.TestBasicScenarioNodeBuilder;
import com.technophobia.substeps.execution.node.TestFeatureNodeBuilder;
import com.technophobia.substeps.execution.node.TestRootNodeBuilder;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.lang.reflect.Method;

/**
 * @author ian
 * 
 */
public class BuildFailureManagerTest {

    // these are referenced via reflection
    public void nonFailingMethod() {
        System.out.println("no fail");
    }

    public void failingMethod() {
        System.out.println("uh oh");
        throw new IllegalStateException("that's it, had enough");
    }


    private RootNode getCriticalErrorNodeTree(){
        Method nonFailMethod = null;
        Method failMethod = null;
        try {
            nonFailMethod = this.getClass().getMethod("nonFailingMethod");
            failMethod = this.getClass().getMethod("failingMethod");
        } catch (final Exception e) {
            Assert.fail(e.getMessage());
        }
        Assert.assertNotNull(nonFailMethod);
        Assert.assertNotNull(failMethod);

        TestRootNodeBuilder rootBuilder = new TestRootNodeBuilder();
        TestFeatureNodeBuilder featureBuilder = rootBuilder.addFeature(new Feature("test feature", "file")).addTags(
                "@can_fail");
        TestBasicScenarioNodeBuilder scenarioNodeBuilder = featureBuilder.addBasicScenario("scenarioName")
                .addStepImpl(getClass(), nonFailMethod).addStepImpl(getClass(), failMethod)
                .addStepImpl(getClass(), nonFailMethod);

        RootNode rootNode = rootBuilder.build();

        final Throwable rootFail = new Exception("t1");

        SubstepExecutionFailure rootNodeFailure = new SubstepExecutionFailure(rootFail, rootNode, ExecutionResult.FAILED);

        SubstepExecutionFailure featureFail = new SubstepExecutionFailure(rootFail, featureBuilder.getBuilt(), ExecutionResult.FAILED);

        SubstepExecutionFailure scenarioFailure = new SubstepExecutionFailure(rootFail, scenarioNodeBuilder.getBuilt(), ExecutionResult.FAILED);

        scenarioNodeBuilder.getBuilt().getChildren().get(0).setLine("stepNode1");
        SubstepExecutionFailure  stepFail = new SubstepExecutionFailure(rootFail, scenarioNodeBuilder.getBuilt().getChildren().get(0), ExecutionResult.FAILED);

        scenarioNodeBuilder.getBuilt().getChildren().get(1).setLine("stepNode2");
        new SubstepExecutionFailure(rootFail, scenarioNodeBuilder.getBuilt().getChildren().get(1), ExecutionResult.NOT_RUN);

        scenarioNodeBuilder.getBuilt().getChildren().get(2).setLine("stepNode3");
        new SubstepExecutionFailure(rootFail, scenarioNodeBuilder.getBuilt().getChildren().get(2), ExecutionResult.NOT_RUN);

        return rootNode;

    }

    private RootNode getNonCriticalErrorNodeTree() {

        Method nonFailMethod = null;
        Method failMethod = null;
        try {
            nonFailMethod = this.getClass().getMethod("nonFailingMethod");
            failMethod = this.getClass().getMethod("failingMethod");
        } catch (final Exception e) {
            Assert.fail(e.getMessage());
        }
        Assert.assertNotNull(nonFailMethod);
        Assert.assertNotNull(failMethod);

        TestRootNodeBuilder rootBuilder = new TestRootNodeBuilder();
        TestFeatureNodeBuilder featureBuilder = rootBuilder.addFeature(new Feature("test feature", "file")).addTags(
                "@can_fail");
        TestBasicScenarioNodeBuilder scenarioNodeBuilder = featureBuilder.addBasicScenario("scenarioName")
                .addStepImpl(getClass(), nonFailMethod).addStepImpl(getClass(), failMethod)
                .addStepImpl(getClass(), nonFailMethod);

        RootNode rootNode = rootBuilder.build();

        final Throwable rootFail = new Exception("t1");

        SubstepExecutionFailure rootNodeFailure = new SubstepExecutionFailure(rootFail, rootNode, ExecutionResult.FAILED);


        SubstepExecutionFailure featureFail = new SubstepExecutionFailure(rootFail, featureBuilder.getBuilt(), ExecutionResult.FAILED);
        featureFail.setNonCritical(true);

        SubstepExecutionFailure scenarioFailure = new SubstepExecutionFailure(rootFail, scenarioNodeBuilder.getBuilt(), ExecutionResult.FAILED);
        scenarioFailure.setNonCritical(true);

        scenarioNodeBuilder.getBuilt().getChildren().get(0).setLine("stepNode1");
        SubstepExecutionFailure  stepFail = new SubstepExecutionFailure(rootFail, scenarioNodeBuilder.getBuilt().getChildren().get(0), ExecutionResult.FAILED);
        stepFail.setNonCritical(true);

        scenarioNodeBuilder.getBuilt().getChildren().get(1).setLine("stepNode2");
        new SubstepExecutionFailure(rootFail, scenarioNodeBuilder.getBuilt().getChildren().get(1), ExecutionResult.NOT_RUN);

        scenarioNodeBuilder.getBuilt().getChildren().get(2).setLine("stepNode3");
        new SubstepExecutionFailure(rootFail, scenarioNodeBuilder.getBuilt().getChildren().get(2), ExecutionResult.NOT_RUN);

        return rootNode;
    }


    private RootNode getBeforesErrorNodeTree(){
        Method nonFailMethod = null;
        Method failMethod = null;
        try {
            nonFailMethod = this.getClass().getMethod("nonFailingMethod");
            failMethod = this.getClass().getMethod("failingMethod");
        } catch (final Exception e) {
            Assert.fail(e.getMessage());
        }
        Assert.assertNotNull(nonFailMethod);
        Assert.assertNotNull(failMethod);

        TestRootNodeBuilder rootBuilder = new TestRootNodeBuilder();
        TestFeatureNodeBuilder featureBuilder = rootBuilder.addFeature(new Feature("test feature", "file")).addTags(
                "@can_fail");
        TestBasicScenarioNodeBuilder scenarioNodeBuilder = featureBuilder.addBasicScenario("scenarioName")
                .addStepImpl(getClass(), nonFailMethod).addStepImpl(getClass(), failMethod)
                .addStepImpl(getClass(), nonFailMethod);

        RootNode rootNode = rootBuilder.build();

        final Throwable rootFail = new Exception("t1");

//        new SubstepExecutionFailure(rootFail, scenarioNodeBuilder.getBuilt(), true);


//        SubstepExecutionFailure featureFail = new SubstepExecutionFailure(rootFail, featureBuilder.getBuilt(), ExecutionResult.FAILED);


        scenarioNodeBuilder.getBuilt().getChildren().get(0).setLine("stepNode1");

        scenarioNodeBuilder.getBuilt().getChildren().get(1).setLine("stepNode2");

        scenarioNodeBuilder.getBuilt().getChildren().get(2).setLine("stepNode3");

        final Throwable t = new IllegalStateException("No tests executed");

        // the setup failure, representing an @BeforeScenario as per AbstractScenarioNodeRunner.runSetup

        SubstepExecutionFailure sef = new SubstepExecutionFailure(t, rootNode, ExecutionResult.FAILED);

        return rootNode;

    }




//    @Ignore("is this a valid failure or is it test data related")
    @Test
    public void testNonCriticalFailures2() {

        BuildFailureManager bfm = new BuildFailureManager();

        RootNode rootNode = getNonCriticalErrorNodeTree();

        bfm.addExecutionResult(rootNode);

//        System.out.println("build info: " +
//                bfm.getBuildFailureInfo());

        // just one non crit error
        Assert.assertFalse(bfm.testSuiteCompletelyPassed());
        Assert.assertTrue(bfm.testSuiteSomeFailures());
        Assert.assertFalse(bfm.testSuiteFailed());


        // A critical error
        rootNode = getCriticalErrorNodeTree();
        bfm = new BuildFailureManager();
        bfm.addExecutionResult(rootNode);

        Assert.assertFalse(bfm.testSuiteCompletelyPassed());
        Assert.assertTrue(bfm.testSuiteSomeFailures());
        Assert.assertTrue(bfm.testSuiteFailed());


        // an @befores error
        rootNode = getBeforesErrorNodeTree();
        bfm = new BuildFailureManager();
        bfm.addExecutionResult(rootNode);

        System.out.println("build info: " +
                bfm.getBuildFailureInfo());

        Assert.assertFalse(bfm.testSuiteCompletelyPassed());
        Assert.assertTrue(bfm.testSuiteSomeFailures());
        Assert.assertTrue(bfm.testSuiteFailed());

    }



}
