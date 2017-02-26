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
package com.technophobia.substeps.glossary;

import com.sun.javadoc.Doclet;
import com.sun.javadoc.*;
import com.technophobia.substeps.model.SubSteps.Step;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author imoore
 */
public class CustomDoclet extends Doclet {

    private static final Logger log = LoggerFactory.getLogger(CustomDoclet.class);
    private static List<StepImplementationsDescriptor> classStepTagsList;


    public static void setExpressionList(final List<StepImplementationsDescriptor> expressionList) {
        classStepTagsList = expressionList;
    }


    public static List<StepImplementationsDescriptor> getExpressions() {
        return classStepTagsList;
    }


    public static boolean start(final RootDoc root) {

        final ClassDoc[] classes = root.classes();

        for (final ClassDoc cd : classes) {

            final StepImplementationsDescriptor classStepTags = new StepImplementationsDescriptor(
                    cd.qualifiedName());

            classStepTagsList.add(classStepTags);

            Class<?> implClass = null;
            Method[] implMethods = null;
            try {

                implClass = root.getClass().getClassLoader().loadClass(cd.qualifiedTypeName());
                implMethods = implClass.getMethods();

                final MethodDoc[] methods = cd.methods();

                for (final MethodDoc md : methods) {

                    final Method underlyingMethod = getMethod(implMethods, md);

                    if (underlyingMethod != null) {
                        final Step annotation = underlyingMethod.getAnnotation(Step.class);

                        if (annotation != null)

                        {
                            final StepDescriptor expression = new StepDescriptor();

                            classStepTags.addStepTags(expression);

                            expression.setDescription(md.commentText().replaceAll("\n", " "));

                            expression.setExample(getSingleJavadocTagValue(md, "example"));
                            expression.setSection(getSingleJavadocTagValue(md, "section"));

                            String line = annotation.value();
                            expression.setRegex(line);

                            final Parameter[] parameters = md.parameters();
                            if (parameters != null && parameters.length > 0) {

                                String[] paramNames = new String[parameters.length];
                                String[] paramTypes = new String[parameters.length];

                                int i = 0;
                                for (final Parameter p : parameters) {
                                    // replace any captures with <variable name>

                                    line = line.replaceFirst("\\([^\\)]*\\)", "<" + p.name() + ">");

                                    paramNames[i] = p.name();
                                    paramTypes[i] = p.typeName();
                                    i++;
                                }

                                expression.setParameterClassNames(paramTypes);
                                expression.setParameterNames(paramNames);
                            }
                            line = line.replaceAll("\\?", "");
                            line = line.replaceAll("\\\\", "");
                            expression.setExpression(line);

                        }
                    }
                }
            } catch (final ClassNotFoundException e) {
                log.error("ClassNotFoundException", e);
            }

        }

        return true;
    }


    /**
     * @param md
     * @param tagName
     */
    private static String getSingleJavadocTagValue(final MethodDoc md, final String tagName) {
        String rtn = null;
        final Tag[] tags = md.tags(tagName);
        if (tags != null && tags.length > 0) {
            rtn = tags[0].text().replace("@" + tagName, "");
            rtn.replaceAll("\n", " ");
        }

        return rtn != null ? rtn : "";
    }


    /**
     * @param implMethods
     * @param md
     * @return
     */
    private static Method getMethod(final Method[] implMethods, final MethodDoc md) {
        Method rtn = null;

        int desiredNumberOfParams = 0;

        final Parameter[] parameters = md.parameters();
        if (parameters != null) {
            desiredNumberOfParams = parameters.length;
        }

        final List<Method> candidateMethods = new ArrayList<Method>();

        // try and match by name
        for (final Method m : implMethods) {
            if (m.getName().equals(md.name()) && m.getParameterTypes().length == desiredNumberOfParams) {
                candidateMethods.add(m);
            }
        }

        if (candidateMethods.size() > 1) {
            throw new IllegalStateException("need to impl parameter type matching");
        }

        if (!candidateMethods.isEmpty()) {
            rtn = candidateMethods.get(0);
        }

        return rtn;
    }

}
