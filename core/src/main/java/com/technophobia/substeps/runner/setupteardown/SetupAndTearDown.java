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
package com.technophobia.substeps.runner.setupteardown;

import com.technophobia.substeps.execution.MethodExecutor;
import com.technophobia.substeps.model.Scope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.lang.reflect.Method;
import java.util.List;

/**
 * Class to encapsulate setup and tear down methods and the ordering of them
 *
 * @author imoore
 */
public class SetupAndTearDown {

    public static final String CLASS_NAME = "className";
    private final Logger log = LoggerFactory.getLogger(SetupAndTearDown.class);

    private String loggingConfigName = null;

    private final MethodExecutor methodExecutor;

    private final BeforeAndAfterMethods beforeAndAfterMethods;

    public SetupAndTearDown(final Class<?>[] classes, final MethodExecutor methodExecutor) {

        this.beforeAndAfterMethods = new BeforeAndAfterMethods(classes);
        this.methodExecutor = methodExecutor;
        this.methodExecutor.addImplementationClasses(classes);
    }

    public String getLoggingConfigName() {
        return this.loggingConfigName;
    }


    public void setLoggingConfigName(final String loggingConfigName) {
        this.loggingConfigName = loggingConfigName;
    }




    public void runBeforeAll() throws Throwable {

        prepareLoggingConfig();

        runAllMethods(MethodState.BEFORE_ALL);
    }


    public void runAfterAll() throws Throwable {
        runAllMethods(MethodState.AFTER_ALL);

        removeLoggingConfig();
    }


    public void runBeforeFeatures() throws Throwable {
        runAllMethods(MethodState.BEFORE_FEATURES);
    }


    public void runAfterFeatures() throws Throwable {
        runAllMethods(MethodState.AFTER_FEATURES);
    }


    public void runBeforeScenarios() throws Throwable {
        runAllMethods(MethodState.BEFORE_SCENARIOS);
    }


    public void runAfterScenarios() throws Throwable {
        runAllMethods(MethodState.AFTER_SCENARIOS);

    }


    private void runAllMethods(final MethodState methodState) throws Throwable {

        final List<Method> setupAndTearDownMethods = this.beforeAndAfterMethods.getSetupAndTearDownMethods(methodState);

        this.methodExecutor.executeMethods(setupAndTearDownMethods);
    }


    private void prepareLoggingConfig() {

        if (this.loggingConfigName == null) {

            MDC.put(CLASS_NAME, "SubSteps");
            this.log.info("no logging config name supplied, defaulting to Substeps");
        } else {
            MDC.put(CLASS_NAME, this.loggingConfigName);
        }
    }


    private void removeLoggingConfig() {
        MDC.remove(CLASS_NAME);
    }


    public void runSetup(final Scope currentScope) throws Throwable {
        this.log.trace("running setup for scope: " + currentScope);

        switch (currentScope) {
            case SUITE: {
                runBeforeAll();
                break;
            }
            case FEATURE: {
                runBeforeFeatures();
                break;
            }
            case SCENARIO: {
                runBeforeScenarios();
                break;
            }
            case SCENARIO_OUTLINE_ROW:
            case SCENARIO_OUTLINE:
            default: {
                // no op STEP, SCENARIO_BACKGROUND
            }
        }

    }


    public void runTearDown(final Scope currentScope) throws Throwable {
        this.log.trace("runTearDown: " + currentScope);

        // TODO: could implement this as methods on Scope itself
        switch (currentScope) {
            case SUITE: {
                runAfterAll();

                break;
            }
            case FEATURE: {
                runAfterFeatures();

                break;
            }
            case SCENARIO: {
                runAfterScenarios();

                // TODO: for outline scenarios this might mean setup and tear down gets run an extra time each...
                break;
            }
            case SCENARIO_OUTLINE_ROW:
            case SCENARIO_OUTLINE:
            default: {
                // no op STEP, SCENARIO_BACKGROUND
            }
        }

    }
}
