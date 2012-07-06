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

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.codehaus.classworlds.ClassRealm;
import org.codehaus.plexus.component.configurator.AbstractComponentConfigurator;
import org.codehaus.plexus.component.configurator.ComponentConfigurationException;
import org.codehaus.plexus.component.configurator.ConfigurationListener;
import org.codehaus.plexus.component.configurator.converters.composite.ObjectWithFieldsConverter;
import org.codehaus.plexus.component.configurator.converters.special.ClassRealmConverter;
import org.codehaus.plexus.component.configurator.expression.ExpressionEvaluationException;
import org.codehaus.plexus.component.configurator.expression.ExpressionEvaluator;
import org.codehaus.plexus.configuration.PlexusConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A custom ComponentConfigurator which adds the project's runtime classpath
 * elements to the
 * 
 * @author Brian Jackson
 * @since Aug 1, 2008 3:04:17 PM
 * 
 * @plexus.component 
 *                   role="org.codehaus.plexus.component.configurator.ComponentConfigurator"
 *                   role-hint="include-project-dependencies"
 * @plexus.requirement role=
 *                     "org.codehaus.plexus.component.configurator.converters.lookup.ConverterLookup"
 *                     role-hint="default"
 */
public class IncludeProjectDependenciesComponentConfigurator extends AbstractComponentConfigurator {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(IncludeProjectDependenciesComponentConfigurator.class);


    @Override
    public void configureComponent(final Object component, final PlexusConfiguration configuration,
            final ExpressionEvaluator expressionEvaluator, final ClassRealm containerRealm,
            final ConfigurationListener listener) throws ComponentConfigurationException {

        addProjectDependenciesToClassRealm(expressionEvaluator, containerRealm);

        converterLookup.registerConverter(new ClassRealmConverter(containerRealm));

        final ObjectWithFieldsConverter converter = new ObjectWithFieldsConverter();

        converter.processConfiguration(converterLookup, component, containerRealm.getClassLoader(),
                configuration, expressionEvaluator, listener);
    }


    private void addProjectDependenciesToClassRealm(final ExpressionEvaluator expressionEvaluator,
            final ClassRealm containerRealm) throws ComponentConfigurationException {
        List<String> runtimeClasspathElements;

        List<String> testClasspathElements;
        try {
            // noinspection unchecked
            runtimeClasspathElements = (List<String>) expressionEvaluator
                    .evaluate("${project.runtimeClasspathElements}");

            testClasspathElements = (List<String>) expressionEvaluator
                    .evaluate("${project.testClasspathElements}");

        } catch (final ExpressionEvaluationException e) {
            throw new ComponentConfigurationException(
                    "There was a problem evaluating: ${project.runtimeClasspathElements}", e);
        }

        runtimeClasspathElements.addAll(testClasspathElements);

        Collections.reverse(runtimeClasspathElements);

        // Add the project dependencies to the ClassRealm
        final URL[] urls = buildURLs(runtimeClasspathElements);
        for (final URL url : urls) {
            containerRealm.addConstituent(url);
        }
    }


    private URL[] buildURLs(final List<String> runtimeClasspathElements)
            throws ComponentConfigurationException {
        // Add the projects classes and dependencies
        final List<URL> urls = new ArrayList<URL>(runtimeClasspathElements.size());
        for (final String element : runtimeClasspathElements) {
            try {
                final URL url = new File(element).toURI().toURL();
                urls.add(url);
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Added to project class loader: " + url);
                }
            } catch (final MalformedURLException e) {
                throw new ComponentConfigurationException("Unable to access project dependency: "
                        + element, e);
            }
        }

        // Add the plugin's dependencies (so Trove stuff works if Trove isn't on
        return urls.toArray(new URL[urls.size()]);
    }

}