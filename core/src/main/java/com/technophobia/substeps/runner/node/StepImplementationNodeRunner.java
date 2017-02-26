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
package com.technophobia.substeps.runner.node;

import com.technophobia.substeps.execution.node.RootNodeExecutionContext;
import com.technophobia.substeps.execution.node.StepImplementationNode;
import com.technophobia.substeps.model.*;
import com.technophobia.substeps.model.exception.SubstepsRuntimeException;
import com.technophobia.substeps.model.parameter.Converter;
import com.technophobia.substeps.runner.ExecutionNodeRunner;
import com.technophobia.substeps.runner.ProvidesScreenshot;
import com.technophobia.substeps.runner.SubstepExecutionFailure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StepImplementationNodeRunner extends AbstractNodeRunner<StepImplementationNode, Void> {


    @Override
    protected boolean execute(StepImplementationNode node, RootNodeExecutionContext context) {

        boolean success = false;

        try {

            // run through any args - if there's any expressions in there, evaluate them now
            Object[] evaluatedArgs = null;

            if (node.getMethodArgs() != null && node.getMethodArgs().length > 0) {
                List<Object> evaluatedArgsList = new ArrayList<>();
                for (Object o : node.getMethodArgs()) {

                    if (o instanceof String) {

                        Object result = Arguments.evaluateExpression((String) o);
                        evaluatedArgsList.add(result);

                    } else {
                        evaluatedArgsList.add(o);
                    }
                }
                evaluatedArgs = evaluatedArgsList.toArray();
                node.setMethodArgs(evaluatedArgs);


                SubSteps.Step stepAnnotation = node.getTargetMethod().getAnnotation(SubSteps.Step.class);
                String rawSourceLine = stepAnnotation.value();

                for (Object o : evaluatedArgsList){
                    rawSourceLine = rawSourceLine.replaceFirst("\\([^\\)]*\\)", o.toString());
                }
                node.setLine(rawSourceLine);
            }


            context.getMethodExecutor().executeMethod(node.getTargetClass(), node.getTargetMethod(),
                    evaluatedArgs);
            context.setTestsHaveRun();
            success = true;

        } catch (final SubstepsRuntimeException e) {
            Throwable cause = e.getCause();

            if (cause instanceof InvocationTargetException){
                InvocationTargetException ite = (InvocationTargetException)cause;
                addFailure(node, context, ite.getTargetException());

            } else {
                addFailure(node, context, cause);
            }

        }

        return success;
    }

    private void addFailure(StepImplementationNode node, RootNodeExecutionContext context, Throwable t) {

        byte[] screenshotBytes = attemptScreenshot(node, context);

        SubstepExecutionFailure failure;
        if (context.isNodeFailureNonCritical(node)){
            failure = SubstepExecutionFailure.nonCriticalFailure(t, node, screenshotBytes);
        }
        else {
            failure = SubstepExecutionFailure.criticalFailure(t,node, screenshotBytes);
        }

        context.addFailure(failure);
    }

    @Override
    protected Scope getScope() {

        return Scope.STEP;
    }

    @SuppressWarnings("unchecked")
    private <T> byte[] attemptScreenshot(StepImplementationNode node, RootNodeExecutionContext context) {

        return ProvidesScreenshot.class.isAssignableFrom(node.getTargetClass()) ? getScreenshot(context,
                (Class<? extends ProvidesScreenshot>) node.getTargetClass()) : null;
    }

    private <T extends ProvidesScreenshot> byte[] getScreenshot(RootNodeExecutionContext context,
                                                                Class<T> screenshotClass) {

        T screenshotTakingInstance = context.getMethodExecutor().getImplementation(screenshotClass);
        return screenshotTakingInstance.getScreenshotBytes();
    }

}
