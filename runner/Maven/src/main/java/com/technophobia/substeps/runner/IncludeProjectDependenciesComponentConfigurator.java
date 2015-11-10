/*
 * NB. Although this file is part of the Technophobia SubSteps runner, this implementation was taken from
 * http://maven.40175.n5.nabble.com/Adding-project-dependencies-and-generated-classes-to-classpath-of-my-plugin-td110119.html
 */
package com.technophobia.substeps.runner;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugin.logging.SystemStreamLog;
import org.codehaus.classworlds.ClassRealm;
import org.codehaus.plexus.component.configurator.AbstractComponentConfigurator;
import org.codehaus.plexus.component.configurator.ComponentConfigurationException;
import org.codehaus.plexus.component.configurator.ConfigurationListener;
import org.codehaus.plexus.component.configurator.converters.composite.ObjectWithFieldsConverter;
import org.codehaus.plexus.component.configurator.converters.special.ClassRealmConverter;
import org.codehaus.plexus.component.configurator.expression.ExpressionEvaluationException;
import org.codehaus.plexus.component.configurator.expression.ExpressionEvaluator;
import org.codehaus.plexus.configuration.PlexusConfiguration;

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
public class IncludeProjectDependenciesComponentConfigurator extends
        AbstractComponentConfigurator {

    // don't think we have access to the correctly injected maven logger at this
    // point - it gets passed into the Mojo, however this is the default impl so
    // use this instead
    private final Log log = new SystemStreamLog();


    @Override
    public void configureComponent(final Object component,
            final PlexusConfiguration configuration,
            final ExpressionEvaluator expressionEvaluator,
            final ClassRealm containerRealm,
            final ConfigurationListener listener)
            throws ComponentConfigurationException {

        addProjectDependenciesToClassRealm(expressionEvaluator, containerRealm);

        this.converterLookup.registerConverter(new ClassRealmConverter(
                containerRealm));

        final ObjectWithFieldsConverter converter = new ObjectWithFieldsConverter();

        converter.processConfiguration(this.converterLookup, component,
                containerRealm.getClassLoader(), configuration,
                expressionEvaluator, listener);

    }


    private void addProjectDependenciesToClassRealm(
            final ExpressionEvaluator expressionEvaluator,
            final ClassRealm containerRealm)
            throws ComponentConfigurationException {

        List<String> testClasspathElements = null;

        try {
            // noinspection unchecked

            testClasspathElements = (List<String>) expressionEvaluator
                    .evaluate("${project.testClasspathElements}");

        } catch (final ExpressionEvaluationException e) {
            throw new ComponentConfigurationException(
                    "There was a problem evaluating: ${project.runtimeClasspathElements}",
                    e);
        }

        if (testClasspathElements != null) {
            // Add the project test dependencies to the ClassRealm
            final URL[] testUrls = buildURLs(testClasspathElements);

            this.log.info("Adding the following jars to the Substeps classpath, tests will be executed with this classpath\n");

            for (final String s : testClasspathElements) {
                this.log.info("\t" + s);
            }
            this.log.info("\n\n");

            for (final URL url : testUrls) {

                containerRealm.addConstituent(url);

            }
        }

    }


    private URL[] buildURLs(final List<String> runtimeClasspathElements)
            throws ComponentConfigurationException {
        // Add the projects classes and dependencies
        final List<URL> urls = new ArrayList<URL>(
                runtimeClasspathElements.size());
        for (final String element : runtimeClasspathElements) {
            try {
                final URL url = new File(element).toURI().toURL();
                urls.add(url);

            } catch (final MalformedURLException e) {
                throw new ComponentConfigurationException(
                        "Unable to access project dependency: " + element, e);
            }
        }

        // Add the plugin's dependencies (so Trove stuff works if Trove isn't on
        return urls.toArray(new URL[urls.size()]);
    }

}