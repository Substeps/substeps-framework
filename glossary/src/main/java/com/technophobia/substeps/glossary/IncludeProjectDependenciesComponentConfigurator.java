/*
 * NB. Although this file is part of the Technophobia SubSteps glossary builder, this implementation was taken from
 * http://maven.40175.n5.nabble.com/Adding-project-dependencies-and-generated-classes-to-classpath-of-my-plugin-td110119.html
 */
package com.technophobia.substeps.glossary;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.codehaus.plexus.classworlds.realm.ClassRealm;
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
 * A custom ComponentConfigurator which adds the project's runtime classpath elements to the
 * 
 * @author Brian Jackson
 * @since Aug 1, 2008 3:04:17 PM
 * 
 * @plexus.component role="org.codehaus.plexus.component.configurator.ComponentConfigurator"
 *                   role-hint="include-project-dependencies"
 *
 * @plexus.requirement role="org.codehaus.plexus.component.configurator.converters.lookup.ConverterLookup"
 *                     role-hint="default"
 */
public class IncludeProjectDependenciesComponentConfigurator extends AbstractComponentConfigurator {

    private static final Logger logger = LoggerFactory
            .getLogger(IncludeProjectDependenciesComponentConfigurator.class);


    @Override
    public void configureComponent(final Object component,
                                   final PlexusConfiguration configuration,
                                   final ExpressionEvaluator expressionEvaluator,
                                   final org.codehaus.plexus.classworlds.realm.ClassRealm containerRealm,
                                   final ConfigurationListener listener)
            throws ComponentConfigurationException {

        addProjectDependenciesToClassRealm(expressionEvaluator, containerRealm);

        converterLookup.registerConverter(new ClassRealmConverter(containerRealm));

        final ObjectWithFieldsConverter converter = new ObjectWithFieldsConverter();

        converter.processConfiguration(converterLookup, component, containerRealm,
                configuration, expressionEvaluator, listener);
    }


    private void addProjectDependenciesToClassRealm(final ExpressionEvaluator expressionEvaluator,
            final ClassRealm containerRealm) throws ComponentConfigurationException {

        List<String> testClasspathElements = null;

        try {
            // noinspection unchecked

            testClasspathElements = (List<String>) expressionEvaluator
                    .evaluate("${project.testClasspathElements}");

        } catch (final ExpressionEvaluationException e) {
            throw new ComponentConfigurationException(
                    "There was a problem evaluating: ${project.runtimeClasspathElements}", e);
        }

        if (testClasspathElements != null) {
            // Add the project test dependencies to the ClassRealm
            final URL[] testUrls = buildURLs(testClasspathElements);
            for (final URL url : testUrls) {
                containerRealm.addURL(url);

            }
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

                // System.out.println("Added to project class loader: " + url);
                if (logger.isDebugEnabled()) {
                    logger.debug("Added to project class loader: " + url);
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