/*
 *  Copyright Technophobia Ltd 2012
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

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.technophobia.substeps.execution.ExecutionResult;
import com.technophobia.substeps.execution.Feature;
import com.technophobia.substeps.execution.ImplementationCache;
import com.technophobia.substeps.execution.node.*;
import com.technophobia.substeps.model.Arguments;
import com.technophobia.substeps.model.exception.NoTestsRunException;
import com.technophobia.substeps.model.exception.SubstepsConfigurationException;
import com.technophobia.substeps.model.exception.SubstepsRuntimeException;
import com.technophobia.substeps.model.exception.UnimplementedStepException;
import com.technophobia.substeps.runner.setupteardown.Annotations.BeforeAllFeatures;
import com.technophobia.substeps.runner.setupteardown.SetupAndTearDown;
import com.technophobia.substeps.stepimplementations.MockStepImplementations;
import com.technophobia.substeps.steps.TestStepImplementations;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.substeps.config.SubstepsConfigLoader;
import org.substeps.report.IExecutionResultsCollector;
import org.substeps.runner.NewSubstepsExecutionConfig;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Matcher;

import static org.hamcrest.CoreMatchers.any;
import static org.hamcrest.CoreMatchers.is;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

/**
 * @author ian
 */
public class ExecutionNodeRunnerTest {

    private ExecutionNodeRunner runner = null;

    @Before
    public void setup() {
        IExecutionResultsCollector mockCollector = Mockito.mock(IExecutionResultsCollector.class);

        runner = new ExecutionNodeRunner();
        runner.addNotifier(mockCollector);
    }

    @Test
    public void testScenarioStepWithParameters() {

        // this failure used to be more dramatic - now the parameter name is
        // passed instead - not such a big failure

        final String feature = "./target/test-classes/features/error4.feature";
        final String tags = "scenario_with_params";
        final String substeps = "./target/test-classes/substeps/simple.substeps";
        final IExecutionListener notifier = mock(IExecutionListener.class);

        final List<SubstepExecutionFailure> failures = new ArrayList<>();

        final RootNode rootNode = runExecutionTest(feature, tags, substeps, notifier, failures);

        Assert.assertThat(rootNode.getResult().getResult(), is(ExecutionResult.PASSED));

    }

    @Test
    public void testParseErrorResultsInFailedTest() {

        // a missing substep

        final String feature = "./target/test-classes/features/error.feature";
        final String tags = "@bug_missing_sub_step_impl";
        final String substeps = "./target/test-classes/substeps/error.substeps";
        final IExecutionListener notifier = mock(IExecutionListener.class);

        final List<SubstepExecutionFailure> failures = new ArrayList<>();

        final RootNode rootNode = runExecutionTest(feature, tags, substeps, notifier, failures);

        // check the rootNode tree is in the state we expect
        Assert.assertThat(rootNode.getResult().getResult(), is(ExecutionResult.FAILED));

        final FeatureNode featureNode = rootNode.getChildren().get(0);
        final ScenarioNode<?> scenarioNode = featureNode.getChildren().get(0);

        Assert.assertThat(scenarioNode.getResult().getResult(), is(ExecutionResult.PARSE_FAILURE));

        verify(notifier, times(1)).onNodeFailed(eq(scenarioNode), argThat(any(UnimplementedStepException.class)));

        Assert.assertThat(failures.size(), is(2));

        Assert.assertThat(failures.get(0).getThrowableInfo().getThrowableClass(), is(UnimplementedStepException.class.getName()));
        final File substepsFile = new File("./target/test-classes/substeps/error.substeps");
        final String msg = "[SingleWord] in source file: " + substepsFile.getAbsolutePath()
                + " line 5 is not a recognised step or substep implementation";

        Assert.assertThat(failures.get(0).getThrowableInfo().getMessage(), is(msg));

        Assert.assertThat(failures.get(1).getThrowableInfo().getThrowableClass(), is(NoTestsRunException.class.getName()));
        Assert.assertThat(failures.get(1).getThrowableInfo().getMessage(), is("No tests executed"));

    }

    @Test
    public void testSubStepDefinitionMatchesStepImplFailure() {

        final String feature = "./target/test-classes/features/error3.feature";
        final String tags = "@duplicate_step_step_def";
        final String substeps = "./target/test-classes/substeps/duplicates2.substeps";
        final IExecutionListener notifier = mock(IExecutionListener.class);

        final List<SubstepExecutionFailure> failures = new ArrayList<>();

        final RootNode rootNode = runExecutionTest(feature, tags, substeps, notifier, failures);

        // check the rootNode tree is in the state we expect
        Assert.assertThat(rootNode.getResult().getResult(), is(ExecutionResult.FAILED));

        final FeatureNode featureNode = rootNode.getChildren().get(0);
        final ScenarioNode<?> scenarioNode = featureNode.getChildren().get(0);

        Assert.assertThat(scenarioNode.getResult().getResult(), is(ExecutionResult.PARSE_FAILURE));

        verify(notifier, times(1)).onNodeFailed(eq(scenarioNode), argThat(any(SubstepsConfigurationException.class)));

        Assert.assertThat(failures.size(), is(2));

        Assert.assertThat(failures.get(0).getThrowableInfo().getThrowableClass(), is(SubstepsConfigurationException.class.getName()));

        Assert.assertThat(
                failures.get(0).getThrowableInfo().getMessage(),
                is("line: [Given something] in [" + feature.replace('/', File.separatorChar)
                        + "] matches step implementation method: [public void "
                        + TestStepImplementations.class.getName()
                        + ".given()] AND matches a sub step definition: [Given something] in [duplicates2.substeps]"));

        Assert.assertThat(failures.get(1).getThrowableInfo().getThrowableClass(), is(NoTestsRunException.class.getName()));
        Assert.assertThat(failures.get(1).getThrowableInfo().getMessage(), is("No tests executed"));

    }

    @Ignore("can't get to fail as I would expect for some reason")
    @Test
    public void testParseError2ResultsInFailedTest() {

        // an example outline with null values

        final String feature = "./target/test-classes/features/error2.feature";
        final String tags = "@invalid_scenario_outline";
        final String substeps = "./target/test-classes/substeps/simple.substeps";
        final IExecutionListener notifier = mock(IExecutionListener.class);

        // TODO - checkfailures - test currently ignored anyway..
        final List<SubstepExecutionFailure> failures = new ArrayList<>();
        final RootNode rootNode = runExecutionTest(feature, tags, substeps, notifier, failures);

        System.out.println("\n\n\n\n\n*************\n\n" + rootNode.toDebugString());

        // check the rootNode tree is in the state we expect
        Assert.assertThat(rootNode.getResult().getResult(), is(ExecutionResult.FAILED));

        final FeatureNode featureNode = rootNode.getChildren().get(0);

        final OutlineScenarioNode scenarioOutlineNode2 = (OutlineScenarioNode) featureNode.getChildren().get(1);

        Assert.assertThat(scenarioOutlineNode2.getResult().getResult(), is(ExecutionResult.PARSE_FAILURE));

        verify(notifier, times(1)).onNodeFailed(eq(scenarioOutlineNode2),
                argThat(any(SubstepsConfigurationException.class)));

    }

    @Test
    public void regExTest() {
        // replacing: <message> with: You must enter the following information
        // to proceed:$Sort code.$Bank Account Name.$Bank Account Number. in
        // string: Then a method with a quoted '<message>'

        String rtn = "Then a method with a quoted '<message>'";
        final String key = "message";
        final String val = "You must enter the following information to proceed:$Sort code.$Bank Account Name.$Bank Account Number.";

        rtn = rtn.replaceAll("<" + key + ">", Matcher.quoteReplacement(val));

        // rtn = Pattern.compile("<" + key +
        // ">").matcher(rtn).replaceAll(Matcher.quoteReplacement(val));

        Assert.assertThat(
                rtn,
                is("Then a method with a quoted 'You must enter the following information to proceed:$Sort code.$Bank Account Name.$Bank Account Number.'"));

    }

    /**
     * @param feature
     * @param tags
     * @param substeps
     * @param notifier
     * @return
     */
    private RootNode runExecutionTest(final String feature, final String tags, final String substeps,
                                      final IExecutionListener notifier, final Class<?>[] initialisationClasses,
                                      final List<SubstepExecutionFailure> failures, final List<Class<?>> stepImplementationClasses) {
        final SubstepsExecutionConfig executionConfig = new SubstepsExecutionConfig();

        Assert.assertTrue(failures.isEmpty());

        executionConfig.setTags(tags);
        executionConfig.setFeatureFile(feature);
        executionConfig.setSubStepsFileName(substeps);
        executionConfig.setDescription("ExecutionNodeRunner Test feature set");

        executionConfig.setStepImplementationClasses(stepImplementationClasses);

        executionConfig.setStrict(false);
        executionConfig.setNonStrictKeywordPrecedence(new String[]{"Given", "And"});

        // this results in test failure rather than exception
        executionConfig.setFastFailParseErrors(false);

        if (initialisationClasses != null) {
            executionConfig.setInitialisationClasses(initialisationClasses);
        }

        return runExecutionTest(notifier, executionConfig, failures);
    }

    private SubstepsExecutionConfig buildConfig(final String feature, final String tags,
                                                final String substeps,
                                                final Class<?>[] initialisationClasses,
                                                final List<Class<?>> stepImplementationClasses,
                                                boolean isStrict, String[] keywordPrecedence) {

        final SubstepsExecutionConfig executionConfig = new SubstepsExecutionConfig();

        executionConfig.setTags(tags);
        executionConfig.setFeatureFile(feature);
        executionConfig.setSubStepsFileName(substeps);
        executionConfig.setDescription("ExecutionNodeRunner Test feature set");

        executionConfig.setStepImplementationClasses(stepImplementationClasses);

        executionConfig.setStrict(isStrict);
        executionConfig.setNonStrictKeywordPrecedence(keywordPrecedence);

        // this results in test failure rather than exception
        executionConfig.setFastFailParseErrors(false);

        if (initialisationClasses != null) {
            executionConfig.setInitialisationClasses(initialisationClasses);
        }
        return executionConfig;
    }

    private RootNode runExecutionTest(final IExecutionListener notifier,
                                      final SubstepsExecutionConfig executionConfig,
                                      final List<SubstepExecutionFailure> failures) {


        Config masterConfig = NewSubstepsExecutionConfig.toConfig(executionConfig);
        Config cfg = SubstepsConfigLoader.splitMasterConfig(masterConfig).get(0);


//        Config masterConfig = NewSubstepsExecutionConfig.loadMasterConfig(NewSubstepsExecutionConfig.toConfig(executionConfig));
//        Config cfg = NewSubstepsExecutionConfig.splitConfigAsOne(masterConfig);


        runner.addNotifier(notifier);

        runner.prepareExecutionConfig(cfg);

        final RootNode rootNode = runner.run();

        final List<SubstepExecutionFailure> localFailures = runner.getFailures();

        failures.addAll(localFailures);

        return rootNode;

    }

    private RootNode runExecutionTest(final String feature, final String tags, final String substeps,
                                      final IExecutionListener notifier, final List<SubstepExecutionFailure> failures) {

        final List<Class<?>> stepImplementationClasses = new ArrayList<>();
        stepImplementationClasses.add(TestStepImplementations.class);

        return runExecutionTest(feature, tags, substeps, notifier, null, failures, stepImplementationClasses);
    }

    private void setPrivateField(final Object target, final String fieldName, final Object value) {

        Field field;
        try {
            field = target.getClass().getDeclaredField(fieldName);
            final boolean currentAccessibility = field.isAccessible();

            field.setAccessible(true);

            field.set(target, value);

            field.setAccessible(currentAccessibility);
        } catch (final Exception e) {
            Assert.fail(e.getMessage());
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T getPrivateField(final Object object, final String fieldName) {

        Field field;
        try {
            field = object.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return (T) field.get(object);
        } catch (final Exception e) {
            Assert.fail(e.getMessage());
            e.printStackTrace();
            return null; // Unreachable
        }

    }

    /**
     * If we use a root node with no children, we should get two failures, one
     * for there being no children on a node which should have children, another
     * for there being no tests run
     */
    @Test
    public void testNoTestsExecutedResultsInTwoFailures() {

        final ExecutionNodeRunner runner = new ExecutionNodeRunner();

        final RootNode node = new RootNode("Description", Collections.emptyList(), "env", "tags", "non-fatal-tags");

        final INotificationDistributor notificationDistributor = getPrivateField(runner, "notificationDistributor");
        final SetupAndTearDown setupAndTearDown = mock(SetupAndTearDown.class);

        final RootNodeExecutionContext nodeExecutionContext = new RootNodeExecutionContext(notificationDistributor,
                Lists.newArrayList(), setupAndTearDown, null, new ImplementationCache());

        setPrivateField(runner, "rootNode", node);
        setPrivateField(runner, "nodeExecutionContext", nodeExecutionContext);

        final IExecutionListener mockNotifer = mock(IExecutionListener.class);
        runner.addNotifier(mockNotifer);

        runner.run();
        final List<SubstepExecutionFailure> failures = runner.getFailures();

        verify(mockNotifer, times(1)).onNodeFailed(argThat(is(node)), argThat(any(NoTestsRunException.class)));
        // verify(mockNotifer, times(1)).onNodeFailed(argThat(is(node)), argThat(any(SubstepsRuntimeException.class)));

        Assert.assertFalse("expecting some failures", failures.isEmpty());

        Assert.assertThat(failures.size(), is(2));
    }

    @Test
    public void testScenarioOutlineFailsWithNoExamples() {

        final OutlineScenarioNode outlineNode = new OutlineScenarioNode("scenarioName",
                Collections.emptyList(), Collections.emptySet(), 2);
        final FeatureNode featureNode = new FeatureNode(new Feature("test feature", "file"),
                Collections.singletonList(outlineNode), Collections.emptySet());
        final ExecutionNode rootNode = new RootNode("Description", Collections.singletonList(featureNode), "env", "tags", "non-fatal-tags");

//        final ExecutionNodeRunner runner = new ExecutionNodeRunner();

        final INotificationDistributor notificationDistributor = getPrivateField(runner, "notificationDistributor");
        final SetupAndTearDown setupAndTearDown = mock(SetupAndTearDown.class);
        final RootNodeExecutionContext nodeExecutionContext = new RootNodeExecutionContext(notificationDistributor,
                Lists.newArrayList(), setupAndTearDown, null, new ImplementationCache());

        setPrivateField(runner, "rootNode", rootNode);
        setPrivateField(runner, "nodeExecutionContext", nodeExecutionContext);

        final IExecutionListener mockNotifer = mock(IExecutionListener.class);
        runner.addNotifier(mockNotifer);

        runner.run();
        final List<SubstepExecutionFailure> failures = runner.getFailures();

        // the failure is called on the root twice, once for the child not
        // having tests, the other for
        // not having any run any tests
        // NB. notifications of failed nodes not the same as the actual failure
        // list returned
        // list contains just those nodes that have actually failed, not the
        // entire tree.

        verify(mockNotifer, times(2)).onNodeFailed(argThat(is(rootNode)), argThat(any(Throwable.class)));

        verify(mockNotifer, times(1)).onNodeFailed(argThat(is(featureNode)), argThat(any(Throwable.class)));

        verify(mockNotifer, times(1)).onNodeFailed(argThat(is(outlineNode)), argThat(any(Throwable.class)));

        Assert.assertFalse("expecting some failures", failures.isEmpty());

        // two failures, one for the scenario outline not having any examples,
        // other for the root node for not having run any tests
        Assert.assertThat(failures.size(), is(2));

        Assert.assertThat(failures.size(), is(2));

        Assert.assertThat(failures.get(0).getThrowableInfo().getThrowableClass(), is(SubstepsRuntimeException.class.getName()));

        Assert.assertThat(failures.get(0).getThrowableInfo().getMessage(), is("node should have children but doesn't"));

        Assert.assertThat(failures.get(1).getThrowableInfo().getThrowableClass(), is(NoTestsRunException.class.getName()));
        Assert.assertThat(failures.get(1).getThrowableInfo().getMessage(), is("No tests executed"));
    }

    private Method getNonFailMethod() {

        return getMethodOrFail("nonFailingMethod");
    }

    private Method getFailMethod() {

        return getMethodOrFail("failingMethod");
    }

    private Method getMethodOrFail(final String method) {

        try {

            return this.getClass().getMethod(method);

        } catch (final Exception e) {

            Assert.fail(e.getMessage());
            return null; // Unreachable
        }
    }

    @Test
    public void testStepFailureFailsFeature() {

        final Method nonFailMethod = getNonFailMethod();
        final Method failMethod = getFailMethod();
        Assert.assertNotNull(nonFailMethod);
        Assert.assertNotNull(failMethod);

        final String scenarioName = "scenarioName";
        final TestRootNodeBuilder rootNodeBuilder = new TestRootNodeBuilder();
        final TestFeatureNodeBuilder featureBuilder = rootNodeBuilder.addFeature(new Feature("test feature", "file"));

        final TestOutlineScenarioNodeBuilder outlineScenarioBuilder = featureBuilder.addOutlineScenario(scenarioName);
        final TestOutlineScenarioRowNodeBuilder rowBuilder1 = outlineScenarioBuilder.addRow(1);
        final TestOutlineScenarioRowNodeBuilder rowBuilder2 = outlineScenarioBuilder.addRow(2);

        final TestBasicScenarioNodeBuilder row1ScenarioBuilder = rowBuilder1.setBasicScenario(scenarioName);
        row1ScenarioBuilder.addStepImpl(getClass(), nonFailMethod).addStepImpl(getClass(), failMethod)
                .addStepImpl(getClass(), nonFailMethod);
        final TestBasicScenarioNodeBuilder row2ScenarioBuilder = rowBuilder2.setBasicScenario(scenarioName);
        row2ScenarioBuilder.addStepImpls(3, getClass(), nonFailMethod);

        final RootNode rootNode = rootNodeBuilder.build();

//        final ExecutionNodeRunner runner = new ExecutionNodeRunner();

        final INotificationDistributor notificationDistributor = getPrivateField(runner, "notificationDistributor");
        final SetupAndTearDown setupAndTearDown = mock(SetupAndTearDown.class);
        final RootNodeExecutionContext nodeExecutionContext = new RootNodeExecutionContext(notificationDistributor,
                Lists.newArrayList(), setupAndTearDown, null, new ImplementationCache());

        setPrivateField(runner, "rootNode", rootNode);
        setPrivateField(runner, "nodeExecutionContext", nodeExecutionContext);

        runner.run();
        final List<SubstepExecutionFailure> failures = runner.getFailures();

        System.out.println(
        rootNode.toDebugString());

        Assert.assertThat(rootNode.getResult().getResult(), is(ExecutionResult.FAILED));
        Assert.assertThat(featureBuilder.getBuilt().getResult().getResult(), is(ExecutionResult.CHILD_FAILED));
        Assert.assertThat(row1ScenarioBuilder.getBuilt().getResult().getResult(), is(ExecutionResult.CHILD_FAILED));
        Assert.assertThat(row2ScenarioBuilder.getBuilt().getResult().getResult(), is(ExecutionResult.PASSED));
        Assert.assertThat(rowBuilder1.getBuilt().getResult().getResult(), is(ExecutionResult.CHILD_FAILED));
        Assert.assertThat(rowBuilder2.getBuilt().getResult().getResult(), is(ExecutionResult.PASSED));

        Assert.assertThat(outlineScenarioBuilder.getBuilt().getResult().getResult(), is(ExecutionResult.CHILD_FAILED));
        Assert.assertThat(row1ScenarioBuilder.getBuilt().getChildren().get(0).getResult().getResult(),
                is(ExecutionResult.PASSED));
        Assert.assertThat(row1ScenarioBuilder.getBuilt().getChildren().get(1).getResult().getResult(),
                is(ExecutionResult.FAILED));
        Assert.assertThat(row1ScenarioBuilder.getBuilt().getChildren().get(2).getResult().getResult(),
                is(ExecutionResult.NOT_RUN));

        Assert.assertThat(row2ScenarioBuilder.getBuilt().getChildren().get(0).getResult().getResult(),
                is(ExecutionResult.PASSED));
        Assert.assertThat(row2ScenarioBuilder.getBuilt().getChildren().get(1).getResult().getResult(),
                is(ExecutionResult.PASSED));
        Assert.assertThat(row2ScenarioBuilder.getBuilt().getChildren().get(2).getResult().getResult(),
                is(ExecutionResult.PASSED));

        Assert.assertFalse("expecting some failures", failures.isEmpty());

        // just one failure for the actual step that failed
        Assert.assertThat(failures.size(), is(1));

    }

    @BeforeAllFeatures
    public void failingSetupMethod() {

        throw new IllegalStateException("something has gone wrong");
    }

    @Test
    public void testBeforeAllFeaturesSetupFailureFailsTheBuild() {

        final Method nonFailMethod = getNonFailMethod();
        final Method failMethod = getFailMethod();
        Assert.assertNotNull(nonFailMethod);
        Assert.assertNotNull(failMethod);

        final String scenarioName = "scenarioName";
        final TestRootNodeBuilder rootNodeBuilder = new TestRootNodeBuilder();
        final TestFeatureNodeBuilder featureBuilder = rootNodeBuilder.addFeature(new Feature("test feature", "file"));

        final TestOutlineScenarioNodeBuilder outlineScenarioBuilder = featureBuilder.addOutlineScenario(scenarioName);
        final TestOutlineScenarioRowNodeBuilder rowBuilder1 = outlineScenarioBuilder.addRow(1);
        final TestOutlineScenarioRowNodeBuilder rowBuilder2 = outlineScenarioBuilder.addRow(2);

        final TestBasicScenarioNodeBuilder row1ScenarioBuilder = rowBuilder1.setBasicScenario(scenarioName);
        row1ScenarioBuilder.addStepImpl(getClass(), nonFailMethod).addStepImpl(getClass(), failMethod)
                .addStepImpl(getClass(), nonFailMethod);
        final TestBasicScenarioNodeBuilder row2ScenarioBuilder = rowBuilder2.setBasicScenario(scenarioName);
        row2ScenarioBuilder.addStepImpl(getClass(), nonFailMethod).addStepImpls(3, getClass(), failMethod);

        final RootNode rootNode = rootNodeBuilder.build();

        final Class<?>[] setupClasses = new Class[]{this.getClass()};
        final SetupAndTearDown setupAndTearDown = new SetupAndTearDown(setupClasses, new ImplementationCache());

//        final ExecutionNodeRunner runner = new ExecutionNodeRunner();

        final INotificationDistributor notificationDistributor = getPrivateField(runner, "notificationDistributor");
        final RootNodeExecutionContext nodeExecutionContext = new RootNodeExecutionContext(notificationDistributor,
                Lists.newArrayList(), setupAndTearDown, null, new ImplementationCache());

        setPrivateField(runner, "rootNode", rootNode);
        setPrivateField(runner, "nodeExecutionContext", nodeExecutionContext);

        runner.run();
        final List<SubstepExecutionFailure> failures = runner.getFailures();

        Assert.assertThat(rootNode.getResult().getResult(), is(ExecutionResult.FAILED));
        Assert.assertThat(featureBuilder.getBuilt().getResult().getResult(), is(ExecutionResult.NOT_RUN));

        Assert.assertFalse("expecting some failures", failures.isEmpty());

        // two failures - one for the @before failure and another because no
        // tests run
        Assert.assertThat(failures.size(), is(2));

        Assert.assertTrue("failure should be marked as setup or tear down", failures.get(0).isSetupOrTearDown());


        Assert.assertThat(failures.get(1).getThrowableInfo().getThrowableClass(), is(NoTestsRunException.class.getName()));

        Assert.assertThat(failures.get(1).getThrowableInfo().getMessage(), is("No tests executed"));
    }

    public void nonFailingMethod() {
        System.out.println("no fail");
    }

    public void failingMethod() {
        System.out.println("uh oh");
        throw new IllegalStateException("that's it, had enough");
    }

    @Test
    public void testParametersSubstitutionWhenNotStrictPlainScenario() {
        String tags = "scenario-with-params-fail";
        final MockStepImplementations stepImpls = new MockStepImplementations();
        final MockStepImplementations spy = spy(stepImpls);

        testParametersSubstitutionWhenNotStrict(tags, spy);

        verify(spy, times(1)).meth13("no sub");
        verify(spy, times(1)).meth13("sub");

    }


    @Test
    public void testParametersSubstitutionWhenNotStrictOK() {
        String tags = "outline-scenario-with-params-pass";

        final MockStepImplementations stepImpls = new MockStepImplementations();
        final MockStepImplementations spy = spy(stepImpls);

        testParametersSubstitutionWhenNotStrict(tags, spy);

        verify(spy, times(1)).meth13("table no sub"); // this one works

    }

    @Test
    public void testParametersSubstitutionWhenNotStrictFail() {
        String tags = "outline-scenario-with-params-fail";
        final MockStepImplementations stepImpls = new MockStepImplementations();
        final MockStepImplementations spy = spy(stepImpls);

        testParametersSubstitutionWhenNotStrict(tags, spy);

        verify(spy, times(1)).meth13("table sub");  // being passed through as " "

    }

    public void testParametersSubstitutionWhenNotStrict(String tags, MockStepImplementations spy) {

        final String feature = "./target/test-classes/features/OutlineScenario.feature";
//        final String tags = "outline-scenario-with-params";
        final String substeps = "./target/test-classes/substeps/outline_scenario/outline_scenario.substeps";
        final IExecutionListener notifier = mock(IExecutionListener.class);

        final List<SubstepExecutionFailure> failures = new ArrayList<>();


        final Map<Class<?>, Object> implsCache = getImplsCache(runner);

        implsCache.put(MockStepImplementations.class, spy);

        final List<Class<?>> stepImplementationClasses = new ArrayList<>();
        stepImplementationClasses.add(MockStepImplementations.class);

        SubstepsExecutionConfig cfg = buildConfig(feature, tags, substeps, null, stepImplementationClasses, false, new String[]{"Given", "And"});


        final RootNode rootNode = runExecutionTest(notifier, cfg, failures);

        Assert.assertThat(rootNode.getResult().getResult(), is(ExecutionResult.PASSED));

        if (!tags.contains("scenario-with-params-fail")) {

            // this test method called in s several contexts, the above tag calls a different set of scenarios altogether
            OutlineScenarioNode osn = (OutlineScenarioNode) rootNode.getChildren().get(0).getChildren().get(0);

            Assert.assertThat(osn.getChildren().get(0).getChildren().get(0).getScenarioName(), is("scenario with table failing iter 1"));
        }

        // TODO - not working either ! no sub twice
        //verify(spy, times(1)).meth13("no sub");
        //verify(spy, times(1)).meth13("sub");

        // TODO - this isn't working
        //verify(spy, times(1)).meth13("table no sub"); // this one works
//       verify(spy, times(1)).meth13("table sub");  // being passed through as " "

        //  verify(spy, times(1)).meth13("no params");

    }


    protected Map<Class<?>, Object> getImplsCache(final ExecutionNodeRunner runner) {
        Map<Class<?>, Object> implsCache = null;

        try {

            final Field implCacheField = runner.getClass().getDeclaredField("methodExecutor");
            implCacheField.setAccessible(true);

            final ImplementationCache cache = (ImplementationCache) implCacheField
                    .get(runner);

            final Field instanceMapField = ImplementationCache.class
                    .getDeclaredField("instanceMap");
            instanceMapField.setAccessible(true);

            implsCache = (Map<Class<?>, Object>) instanceMapField.get(cache);

        } catch (final Exception e) {
            e.printStackTrace();
        }

        Assert.assertNotNull("implsCache should not be null", implsCache);

        return implsCache;
    }


    @Test
    public void testArgSubstituion() {

        Config cfg = ConfigFactory.load("localhost.conf");

        String parameterFromConfig = cfg.getString("users.default.name");
        Assert.assertThat(parameterFromConfig, is("bob"));

        final String srcString1 = "Given a substep that takes one parameter \"src1\"";
        final String srcString2 = "And a substep that takes one parameter \"src2\"";
        final String srcString3 = "And a substep that takes one parameter \"${users.default.name}\"";

        final String patternString = "Given a substep that takes one parameter \"([^\"]*)\"";
        final String[] keywordPrecedence = new String[]{"Given", "And"};
        String[] args1 = Arguments.getArgs(patternString, srcString1, keywordPrecedence, cfg);


        String[] args2 = Arguments.getArgs(patternString, srcString2, keywordPrecedence, cfg);

        Assert.assertNotNull(args2);
        Assert.assertThat(args2[0], is("src2"));

        Assert.assertNotNull(args1);
        Assert.assertThat(args1[0], is("src1"));

        String[] args3 = Arguments.getArgs(patternString, srcString3, keywordPrecedence, cfg);
        Assert.assertNotNull(args3);
        Assert.assertThat(args3[0], is("bob"));

    }

    @Ignore("wip")
    @Test
    public void testNonCriticalFailures() {
        // possible bug around the cascading of errors - a non critical gets bubbled up to become a critical...

        // one feature, two scenarios, first one fails (non crit), second one passes

        final Method nonFailMethod = getNonFailMethod();
        final Method failMethod = getFailMethod();
        Assert.assertNotNull(nonFailMethod);
        Assert.assertNotNull(failMethod);

        final TestRootNodeBuilder rootNodeBuilder = new TestRootNodeBuilder();
        final TestFeatureNodeBuilder featureBuilder = rootNodeBuilder.addFeature(new Feature("test feature", "file"));

        featureBuilder.addTags("toRun", "canFail");

        TestBasicScenarioNodeBuilder scenario1Builder = featureBuilder.addBasicScenario("scenario 1");
        scenario1Builder.addTags(ImmutableSet.of("toRun", "canFail"));
        scenario1Builder.addStepImpl(getClass(), nonFailMethod).addStepImpl(getClass(), failMethod).addStepImpl(getClass(), nonFailMethod);

        TestBasicScenarioNodeBuilder scenario2Builder = featureBuilder.addBasicScenario("scenario 2");
        scenario2Builder.addStepImpl(getClass(), nonFailMethod).addStepImpl(getClass(), nonFailMethod).addStepImpl(getClass(), nonFailMethod);
        scenario2Builder.addTags(ImmutableSet.of("toRun", "canFail"));


        final RootNode rootNode = rootNodeBuilder.build();

        BasicScenarioNode scenario1 = scenario1Builder.getBuilt();
        BasicScenarioNode scenario2 = scenario2Builder.getBuilt();

        final ExecutionNodeRunner runner = new ExecutionNodeRunner();

        final INotificationDistributor notificationDistributor = getPrivateField(runner, "notificationDistributor");
        final SetupAndTearDown setupAndTearDown = mock(SetupAndTearDown.class);

        TagManager nonFatalTagManager = new TagManager("canFail");

        final RootNodeExecutionContext nodeExecutionContext = new RootNodeExecutionContext(notificationDistributor,
                Lists.newArrayList(), setupAndTearDown, nonFatalTagManager, new ImplementationCache());

        setPrivateField(runner, "rootNode", rootNode);
        setPrivateField(runner, "nodeExecutionContext", nodeExecutionContext);

        runner.run();

        BuildFailureManager bfm = new BuildFailureManager();
        bfm.addExecutionResult(rootNode);

        Assert.assertFalse("non critical failure incorrectly reported as critical", bfm.testSuiteFailed());
        Assert.assertFalse("non critical failure reporting issue", bfm.testSuiteCompletelyPassed());

        final List<SubstepExecutionFailure> failures = runner.getFailures();

        Assert.assertThat(rootNode.getResult().getResult(), is(ExecutionResult.FAILED));
        Assert.assertThat(featureBuilder.getBuilt().getResult().getResult(), is(ExecutionResult.FAILED));


        Assert.assertThat(scenario1.getResult().getResult(), is(ExecutionResult.FAILED));
        Assert.assertThat(scenario2.getResult().getResult(), is(ExecutionResult.PASSED));

        // TODO check that the number of errors is correct

        Assert.assertFalse("expecting some failures", failures.isEmpty());

        // just one failure for the actual step that failed
        Assert.assertThat(failures.size(), is(1));

    }



    @Test
    public void testExecutionNodeTreeBuildingWithScenarioName() {

        // this failure used to be more dramatic - now the parameter name is
        // passed instead - not such a big failure

        final String feature = "./target/test-classes/features/allFeatures.feature";
        final String scenarioName = "Simple top level scenario";
        final String substeps = "./target/test-classes/substeps/simple.substeps";
        final IExecutionListener notifier = mock(IExecutionListener.class);

        final List<SubstepExecutionFailure> failures = new ArrayList<>();


        final List<Class<?>> stepImplementationClasses = new ArrayList<>();
        stepImplementationClasses.add(MockStepImplementations.class);

        final SubstepsExecutionConfig executionConfig = new SubstepsExecutionConfig();

        Assert.assertTrue(failures.isEmpty());

        executionConfig.setScenarioName(scenarioName);
        executionConfig.setFeatureFile(feature);
        executionConfig.setSubStepsFileName(substeps);
        executionConfig.setDescription("ExecutionNodeRunner Test feature set");

        executionConfig.setStepImplementationClasses(stepImplementationClasses);

        executionConfig.setStrict(false);
        executionConfig.setNonStrictKeywordPrecedence(new String[]{"Given", "And"});

        executionConfig.setFastFailParseErrors(false);

        Config masterConfig = NewSubstepsExecutionConfig.toConfig(executionConfig);
        Config cfg = SubstepsConfigLoader.splitMasterConfig(masterConfig).get(0);


//        Config masterConfig = NewSubstepsExecutionConfig.loadMasterConfig(NewSubstepsExecutionConfig.toConfig(executionConfig));
//        Config cfg = NewSubstepsExecutionConfig.splitConfigAsOne(masterConfig);

        final RootNode rootNode = runner.prepareExecutionConfig(cfg);

        Assert.assertThat(rootNode.getChildren().size(), is(1));

        Assert.assertThat(rootNode.getChildren().get(0).getChildren().size(), is(1));
    }

}


