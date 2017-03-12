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

import com.technophobia.substeps.helper.AssertHelper;
import com.technophobia.substeps.model.SubSteps.StepImplementations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 * Wraps an ExecutionConfig providing extra functionality for core
 *
 * @author rbarefield
 */
public class ExecutionConfigWrapper {

    private static final Logger log = LoggerFactory.getLogger(ExecutionConfigWrapper.class);

    private final SubstepsExecutionConfig executionConfig;

    public ExecutionConfigWrapper(final SubstepsExecutionConfig executionConfig) {
        this.executionConfig = executionConfig;
    }

    public SubstepsExecutionConfig getExecutionConfig(){
        return executionConfig;
    }

    public void initProperties() {

        if (executionConfig.getStepImplementationClasses() == null) {
            executionConfig.setStepImplementationClasses(getClassesFromConfig(executionConfig.getStepImplementationClassNames()));
        }

        if (executionConfig.getSystemProperties() != null) {

            log.debug("Configuring system properties [" + executionConfig.getSystemProperties().size() + "] for execution");
            final Properties existing = System.getProperties();
            executionConfig.getSystemProperties().putAll(existing);
            System.setProperties(executionConfig.getSystemProperties());
        }

        determineInitialisationClasses();

        log.debug(printParameters());
    }

    private static List<Class<?>> getClassesFromConfig(final String[] config) {
        List<Class<?>> stepImplementationClassList = null;
        for (final String className : config) {
            if (stepImplementationClassList == null) {
                stepImplementationClassList = new ArrayList<Class<?>>();
            }
            Class<?> implClass;
            try {
                implClass = Class.forName(className);
                stepImplementationClassList.add(implClass);

            } catch (final ClassNotFoundException e) {
                throw new AssertionError(e);
            }
        }
        return stepImplementationClassList;
    }

    private String printParameters() {
        return "ExecutionConfig [cfg: " + executionConfig.printParameters() + "]";
    }

    public List<Class<? extends IExecutionListener>> getExecutionListenerClasses() {

        final List<Class<? extends IExecutionListener>> notifierClassList = new ArrayList<Class<? extends IExecutionListener>>();

        final String[] classList = executionConfig.getExecutionListeners();
        if (classList != null) {
            for (final String className : classList) {

                try {
                    final Class<?> implClass = Class.forName(className);

                    if (IExecutionListener.class.isAssignableFrom(implClass)) {
                        notifierClassList.add((Class<? extends IExecutionListener>) implClass);
                    } else {
                        AssertHelper.fail("Execution Listener does not extend com.technophobia.substeps.runner.IExecutionListener");
                    }

                } catch (final ClassNotFoundException e) {
                    throw new AssertionError(e);
                }
            }
        }
        return notifierClassList;

    }

    public static Class<?>[] buildInitialisationClassList(List<Class<?>> stepImplClassList, List<Class<?>> initialisationClassList){

        List<Class<?>> finalInitialisationClassList = null;
        if (stepImplClassList != null) {

            final InitialisationClassSorter orderer = new InitialisationClassSorter();

            for (final Class<?> c : stepImplClassList) {

                final StepImplementations annotation = c.getAnnotation(StepImplementations.class);

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
        // TODO - either init classes on the step impls, but not both!
//        if (finalInitialisationClassList != null) {
//            executionConfig.setInitialisationClasses(finalInitialisationClassList.toArray(new Class<?>[]{}));
//        }

        if (finalInitialisationClassList != null) {
            return finalInitialisationClassList.toArray(new Class<?>[]{});
        }
        else {
            return null;
        }
    }

    public Class<?>[] determineInitialisationClasses() {

        List<Class<?>> initialisationClassList = null;
        if (executionConfig.getStepImplementationClasses() != null) {

            final InitialisationClassSorter orderer = new InitialisationClassSorter();

            for (final Class<?> c : executionConfig.getStepImplementationClasses()) {

                final StepImplementations annotation = c.getAnnotation(StepImplementations.class);

                if (annotation != null) {
                    final Class<?>[] initClasses = annotation.requiredInitialisationClasses();

                    if (initClasses != null) {

                        orderer.addOrderedInitialisationClasses(initClasses);
                    }
                }
            }

            initialisationClassList = orderer.getOrderedList();
        }
        if (initialisationClassList == null && executionConfig.getInitialisationClass() != null) {
            initialisationClassList = getClassesFromConfig(executionConfig.getInitialisationClass());
        }

        if (initialisationClassList != null) {
            executionConfig.setInitialisationClasses(initialisationClassList.toArray(new Class<?>[]{}));
        }

        return executionConfig.getInitialisationClasses();
    }
}