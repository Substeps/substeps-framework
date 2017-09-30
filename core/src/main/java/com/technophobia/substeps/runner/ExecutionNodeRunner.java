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

import com.google.common.collect.Lists;
import com.technophobia.substeps.execution.DryRunImplementationCache;
import com.technophobia.substeps.execution.ExecutionResult;
import com.technophobia.substeps.execution.ImplementationCache;
import com.technophobia.substeps.execution.MethodExecutor;
import com.technophobia.substeps.execution.node.*;
import com.technophobia.substeps.model.*;
import com.technophobia.substeps.model.exception.NoTestsRunException;
import com.technophobia.substeps.runner.builder.ExecutionNodeTreeBuilder;
import com.technophobia.substeps.runner.node.RootNodeRunner;
import com.technophobia.substeps.runner.setupteardown.SetupAndTearDown;
import com.technophobia.substeps.runner.syntax.SyntaxBuilder;
import com.typesafe.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.substeps.report.ReportingUtil;
import org.substeps.runner.NewSubstepsExecutionConfig;

import java.io.File;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Takes a tree of execution nodes and executes them, all variables, args,
 * backgrounds already pre-determined
 *
 * @author ian
 */
public class ExecutionNodeRunner implements SubstepsRunner {

    private static final String DRY_RUN_KEY = "dryRun";

    private static final Logger log = LoggerFactory.getLogger(ExecutionNodeRunner.class);

    private RootNode rootNode;

    private final INotificationDistributor notificationDistributor = new NotificationDistributor();

    private RootNodeExecutionContext nodeExecutionContext;

    private final MethodExecutor methodExecutor = new ImplementationCache();

    private final RootNodeRunner rootNodeRunner = new RootNodeRunner();

    private List<SubstepExecutionFailure> failures;

    private final ReportingUtil reportingUtil = new ReportingUtil();

    // map of nodes to each of the parents, where this node is used
    private final Map<ExecutionNodeUsage, List<ExecutionNodeUsage>> callerHierarchy = new HashMap<ExecutionNodeUsage, List<ExecutionNodeUsage>>();

    @Override
    public void addNotifier(final IExecutionListener notifier) {

        this.notificationDistributor.addListener(notifier);
    }

    public RootNode prepareExecutionConfig(final Config config , final Syntax syntax, final TestParameters parameters,
                                           final SetupAndTearDown setupAndTearDown ,
                                           final MethodExecutor methodExecutorToUse,
                                           TagManager nonFatalTagmanager ) {


        final ExecutionNodeTreeBuilder nodeTreeBuilder = new ExecutionNodeTreeBuilder(parameters, config);

        // building the tree can throw critical failures if exceptions are found
        this.rootNode = nodeTreeBuilder.buildExecutionNodeTree(NewSubstepsExecutionConfig.getDescription(config));

        setupExecutionListeners(NewSubstepsExecutionConfig.getExecutionListenerClasses(config));



        if (NewSubstepsExecutionConfig.isCheckForUncalledAndUnused(config)) {
            processUncalledAndUnused(syntax, NewSubstepsExecutionConfig.getDataOutputDirectory(config));
        }

        ExecutionContext.put(Scope.SUITE, INotificationDistributor.NOTIFIER_DISTRIBUTOR_KEY,
                this.notificationDistributor);


        this.nodeExecutionContext = new RootNodeExecutionContext(this.notificationDistributor,
                Lists.<SubstepExecutionFailure>newArrayList(), setupAndTearDown, nonFatalTagmanager,
                methodExecutorToUse);

        return this.rootNode;

    }

    public static Class<?>[] buildInitialisationClassList(List<Class<?>> stepImplClassList, List<Class<?>> initialisationClassList){

        List<Class<?>> finalInitialisationClassList = null;
        if (stepImplClassList != null) {

            final InitialisationClassSorter orderer = new InitialisationClassSorter();

            for (final Class<?> c : stepImplClassList) {

                final SubSteps.StepImplementations annotation = c.getAnnotation(SubSteps.StepImplementations.class);

                if (annotation != null) {
                    final Class<?>[] initClasses = annotation.requiredInitialisationClasses();

                    if (initClasses != null) {

                        orderer.addOrderedInitialisationClasses(initClasses);
                    }
                }
            }

            finalInitialisationClassList = orderer.getOrderedList();
        }
        if (finalInitialisationClassList == null && initialisationClassList != null) {
            finalInitialisationClassList = initialisationClassList;
        }
        else {
            finalInitialisationClassList.addAll(initialisationClassList);
        }

        if (finalInitialisationClassList != null) {
            return finalInitialisationClassList.toArray(new Class<?>[]{});
        }
        else {
            return null;
        }
    }


    @Override
    public RootNode prepareExecutionConfig(Config cfg) {

        NewSubstepsExecutionConfig.setThreadLocalConfig(cfg);


        final String dryRunProperty = System.getProperty(DRY_RUN_KEY);
        final boolean dryRun = dryRunProperty != null && Boolean.parseBoolean(dryRunProperty);

        final MethodExecutor methodExecutorToUse = dryRun ? new DryRunImplementationCache() : this.methodExecutor;

        if (dryRun) {
            log.info("**** DRY RUN ONLY **");
        }

        List<Class<?>> stepImplementationClasses = NewSubstepsExecutionConfig.getStepImplementationClasses(cfg);
        Class<?>[] initialisationClasses = NewSubstepsExecutionConfig.getInitialisationClasses(cfg);

        ArrayList<Class<?>> initClassList = null;
        if (initialisationClasses != null) {
            initClassList = Lists.newArrayList(initialisationClasses);
        }

        Class<?>[] finalInitClasses = buildInitialisationClassList(stepImplementationClasses, initClassList);

        final SetupAndTearDown setupAndTearDown = new SetupAndTearDown(finalInitClasses,
                methodExecutorToUse);


        final String loggingConfigName = NewSubstepsExecutionConfig.getDescription(cfg);

        setupAndTearDown.setLoggingConfigName(loggingConfigName);

        final TagManager tagmanager = new TagManager(NewSubstepsExecutionConfig.getTags(cfg));

        final TagManager nonFatalTagmanager = TagManager.fromTags(NewSubstepsExecutionConfig.getNonFatalTags(cfg));

        File subStepsFile = null;

        if (NewSubstepsExecutionConfig.getSubStepsFileName(cfg) != null) {
            subStepsFile = new File(NewSubstepsExecutionConfig.getSubStepsFileName(cfg));
        }

        final Syntax syntax = SyntaxBuilder.buildSyntax(stepImplementationClasses, subStepsFile,
                NewSubstepsExecutionConfig.isStrict(cfg), NewSubstepsExecutionConfig.getNonStrictKeywordPrecedence(cfg));

        final TestParameters parameters = new TestParameters(tagmanager, syntax, NewSubstepsExecutionConfig.getFeatureFile(cfg),
                NewSubstepsExecutionConfig.getScenarioName(cfg));

        parameters.setFailParseErrorsImmediately(NewSubstepsExecutionConfig.isFastFailParseErrors(cfg));
        parameters.init();

        return prepareExecutionConfig(cfg, syntax, parameters, setupAndTearDown, methodExecutorToUse, nonFatalTagmanager);
    }


    private void setupExecutionListeners( final List<Class<? extends IExecutionListener>> executionListenerClasses) {
        // add any listeners (including the step execution logger)

        // TODO - pass the base dir in or get from config

        for (final Class<? extends IExecutionListener> listener : executionListenerClasses) {

            log.info("adding executionListener: " + listener.getClass());

            try {
                this.notificationDistributor.addListener(listener.newInstance());
            } catch (final Exception e) {
                // not the end of the world...
                log.warn("failed to instantiate ExecutionListener: " + listener.getClass(), e);
            }
        }
    }

    /**
     * @param syntax
     */
    private void processUncalledAndUnused(final Syntax syntax, final File dataOutputDir) {
        final List<StepImplementation> uncalledStepImplementations = syntax.getUncalledStepImplementations();

        if (!dataOutputDir.exists()){
            dataOutputDir.mkdir();
        }

        reportingUtil.writeUncalledStepImpls(uncalledStepImplementations, dataOutputDir);

        buildCallHierarchy();

        checkForUncalledParentSteps(syntax, dataOutputDir);

    }

    /**
     * @param syntax
     */
    private void checkForUncalledParentSteps(final Syntax syntax, File outputDir) {

        final Set<ExecutionNodeUsage> calledExecutionNodes = callerHierarchy.keySet();

        List<Step> uncalledSubstepDefs = new ArrayList<>();

        for (final ParentStep p : syntax.getSortedRootSubSteps()) {

            // is there an executionnodeusage that is going to match ?

            final Step parent = p.getParent();

            if (thereIsNotAStepThatMatchesThisPattern(parent.getPattern(), calledExecutionNodes)) {
                uncalledSubstepDefs.add(parent);
            }
        }
        reportingUtil.writeUncalledStepDefs(uncalledSubstepDefs, outputDir);
    }

    private boolean thereIsNotAStepThatMatchesThisPattern(final String stepPattern, final Set<ExecutionNodeUsage> calledExecutionNodes) {
        boolean found = false;

        final Iterator<ExecutionNodeUsage> it = calledExecutionNodes.iterator();

        while (it.hasNext() && !found) {
            final ExecutionNodeUsage u = it.next();

            if (stepPattern != null && u.getDescription() != null) {

                found = Pattern.matches(stepPattern, u.getDescription());
            }

        }
        // NB. return true if no match found!
        return !found;
    }


    private void buildCallHierarchy() {

        final ExecutionNodeUsage rootUsage = new ExecutionNodeUsage(this.rootNode);

        callerHierarchy.put(rootUsage, null); // nothing calls this

        for (final FeatureNode feature : this.rootNode.getChildren()) {

            addToCallHierarchy(feature);

            for (final ScenarioNode scenario : feature.getChildren()) {

                addToCallHierarchy(scenario);

                processChildrenForCallHierarchy(scenario.getChildren());
            }
        }
    }

    private void processChildrenForCallHierarchy(final List children) {
        for (final Object obj : children) {

            final IExecutionNode node = (IExecutionNode) obj;

            addToCallHierarchy(node);

            log.trace("looking at node description: " + node.getDescription() + " line: " + node.getLine());

            if (NodeWithChildren.class.isAssignableFrom(node.getClass())) {
                final NodeWithChildren nodeWithChildren = (NodeWithChildren) node;
                log.trace("proccessing children...");
                processChildrenForCallHierarchy(nodeWithChildren.getChildren());
            }
        }
    }

    /**
     * @param node
     */
    private void addToCallHierarchy(final IExecutionNode node) {

        final ExecutionNodeUsage usage = new ExecutionNodeUsage(node);

        log.trace("building usage for desc: " + node.getDescription() + " line: " + node.getLine());

        List<ExecutionNodeUsage> immediateParents = callerHierarchy.get(usage);
        if (immediateParents == null) {
            log.trace("no uses already for node...");
            immediateParents = new ArrayList<ExecutionNodeUsage>();
            callerHierarchy.put(usage, immediateParents);
        }
//        else {
//            log.trace("got existing usages of node: ");
//            for (final ExecutionNodeUsage u : immediateParents) {
//                log.trace("already found: " + u.toString());
//            }
//        }
        log.trace("adding used by descr: " + node.getParent().getDescription() + " line: " + node.getParent().getLine());

        immediateParents.add(new ExecutionNodeUsage(node.getParent()));

    }

    @Override
    public RootNode run() {

        // TODO - why is this here twice?
        ExecutionContext.put(Scope.SUITE, INotificationDistributor.NOTIFIER_DISTRIBUTOR_KEY,
                this.notificationDistributor);

        this.rootNodeRunner.run(this.rootNode, this.nodeExecutionContext);

        if (!this.nodeExecutionContext.haveTestsBeenRun()) {

            final Throwable t = new NoTestsRunException();

            SubstepExecutionFailure sef = new SubstepExecutionFailure(t, this.rootNode, ExecutionResult.FAILED);

            this.notificationDistributor.onNodeFailed(this.rootNode, t);

            this.nodeExecutionContext.addFailure(sef);
        }

        this.failures = this.nodeExecutionContext.getFailures();

        return this.rootNode;
    }

    @Override
    public List<SubstepExecutionFailure> getFailures() {

        return this.failures;
    }

}
