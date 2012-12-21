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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.technophobia.substeps.execution.ExecutionResult;
import com.technophobia.substeps.execution.Feature;
import com.technophobia.substeps.execution.node.FeatureNode;
import com.technophobia.substeps.execution.node.RootNode;
import com.technophobia.substeps.execution.node.TestBasicScenarioNodeBuilder;
import com.technophobia.substeps.execution.node.TestFeatureNodeBuilder;
import com.technophobia.substeps.execution.node.TestRootNodeBuilder;

/**
 * @author ian
 * 
 */
public class BuildFailureManagerTest {

    public void nonFailingMethod() {
        System.out.println("no fail");
    }

    public void failingMethod() {
        System.out.println("uh oh");
        throw new IllegalStateException("that's it, had enough");
    }

    private RootNode getData() {

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

        rootNode.getResult().setFailed(rootFail);
        featureBuilder.getBuilt().getResult().setFailed(rootFail);
        scenarioNodeBuilder.getBuilt().getResult().setFailed(rootFail);

        scenarioNodeBuilder.getBuilt().getChildren().get(0).setLine("stepNode1");

        scenarioNodeBuilder.getBuilt().getChildren().get(1).setLine("stepNode2");
        scenarioNodeBuilder.getBuilt().getChildren().get(1).getResult().setResult(ExecutionResult.NOT_RUN);

        scenarioNodeBuilder.getBuilt().getChildren().get(2).setLine("stepNode3");
        scenarioNodeBuilder.getBuilt().getChildren().get(2).getResult().setResult(ExecutionResult.NOT_RUN);

        return rootNode;
    }

    @Test
    public void testNonCriticalFailures() {

        BuildFailureManager bfm = new BuildFailureManager();

        final RootNode rootNode = getData();
        // set up the scenario

        final List<SubstepExecutionFailure> failures = new ArrayList<SubstepExecutionFailure>();

        TestFeatureNodeBuilder featureBuilder = new TestFeatureNodeBuilder(new Feature("test feature", "file"));

        FeatureNode featureNode = featureBuilder.build();
        rootNode.getChildren().add(featureNode);

        final Throwable rootFail = new Exception("t1");

        featureNode.getResult().setFailed(rootFail);

        final SubstepExecutionFailure f1 = new SubstepExecutionFailure(rootFail, featureNode);

        f1.setNonCritical(true);

        failures.add(f1);

        bfm.addExecutionResult(rootNode);

        // just one non crit error
        Assert.assertFalse(bfm.testSuiteCompletelyPassed());
        Assert.assertTrue(bfm.testSuiteSomeFailures());
        Assert.assertFalse(bfm.testSuiteFailed());

        // see what happens with an @beforefailure
        failures.clear();

        failures.add(new SubstepExecutionFailure(rootFail, featureNode, true));
        bfm = new BuildFailureManager();
        bfm.addExecutionResult(rootNode);

        // just an @before fail
        Assert.assertFalse(bfm.testSuiteCompletelyPassed());
        Assert.assertTrue(bfm.testSuiteSomeFailures());
        Assert.assertTrue(bfm.testSuiteFailed());

        failures.clear();
        failures.add(new SubstepExecutionFailure(rootFail, featureNode));
        bfm = new BuildFailureManager();
        bfm.addExecutionResult(rootNode);

        // a normal critical fail
        Assert.assertFalse(bfm.testSuiteCompletelyPassed());
        Assert.assertTrue(bfm.testSuiteSomeFailures());
        Assert.assertTrue(bfm.testSuiteFailed());

    }
}
