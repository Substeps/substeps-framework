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

import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.sun.tools.javadoc.Main;
import com.technophobia.substeps.model.SubSteps;
import com.technophobia.substeps.runner.BaseSubstepsMojo;
import com.technophobia.substeps.runner.ExecutionConfig;
import com.typesafe.config.Config;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.substeps.config.SubstepsConfigLoader;
import org.substeps.runner.NewSubstepsExecutionConfig;

import java.io.*;
import java.net.MalformedURLException;
import java.nio.charset.Charset;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

/**
 * A Maven plugin to generate a json representation of the step implementations in this library.  That json is then used in verious plugins and IDE's etc.
 */
@Mojo(name = "generate-docs",
        defaultPhase = LifecyclePhase.GENERATE_RESOURCES,
        requiresDependencyResolution = ResolutionScope.TEST,
        requiresProject = true,
        configurator = "include-project-dependencies")
public class SubstepsGlossaryMojo extends BaseSubstepsMojo {

    private final Logger log = LoggerFactory.getLogger(SubstepsGlossaryMojo.class);



    /**
     * @parameter
     */
    @Parameter
    private GlossaryPublisher glossaryPublisher = null;

    private List<StepImplementationsDescriptor> runJavaDoclet(final String classToDocument) {

        final List<StepImplementationsDescriptor> classStepTagList = new ArrayList<StepImplementationsDescriptor>();

        project.getBasedir();

        String sourceRoot = null;
        // what's the path to this class ?
        String path = resolveClassToPath(classToDocument, project.getBuild().getSourceDirectory());
        if (path != null) {
            // file is in the source path
            sourceRoot = project.getBuild().getSourceDirectory();
        } else {
            path = resolveClassToPath(classToDocument, project.getBuild().getTestSourceDirectory());
            if (path != null) {
                // file is in the test tree
                sourceRoot = project.getBuild().getTestSourceDirectory();
            }
        }

        if (sourceRoot == null || path == null) {

            log.error("unabled to locate source file");
            // TODO exception ?
        } else {

            CustomDoclet.setExpressionList(classStepTagList);

            final String[] args = {"-doclet", "com.technophobia.substeps.glossary.CustomDoclet",
                    "-sourcepath", sourceRoot,
                    path
            };

            // the custom doclet generates quite a lot of noise around things missing from the classpath etc -
            // not important in this context, so consume and discard apart from the errors..

            StringWriter esw = new StringWriter();
            PrintWriter err = new PrintWriter(esw);

            StringWriter wsw = new StringWriter();
            PrintWriter warn = new PrintWriter(wsw);

            StringWriter nsw = new StringWriter();
            PrintWriter notice = new PrintWriter(nsw);

            String warnings = esw.toString();
            if (!warnings.isEmpty()) {
                getLog().warn("Substeps CustomDoclet warnings:\n" + warnings);
            }

            Main.execute("SubstepsDoclet", err, warn, notice, "com.technophobia.substeps.glossary.CustomDoclet", args);
        }

        return classStepTagList;
    }


    /**
     * @param classToDocument = fqn of a class, dotted syntax
     * @param dir             the directory of the source
     * @return
     */
    private String resolveClassToPath(final String classToDocument, final String dir) {
        String fullpath = null;
        log.debug("resolving class to path: " + classToDocument + " in " + dir);

        final String filepath = dir + File.separator
                + classToDocument.replace('.', File.separator.charAt(0)) + ".java";

        log.debug("looking for file: " + filepath);

        final File f = new File(filepath);

        if (f.exists()) {
            fullpath = f.getAbsolutePath();
        }
        return fullpath;
    }


    @Override
    public void executeConfig(Config cfg) throws MojoExecutionException, MojoFailureException {
        // no op
    }

    @Override
    public void executeBeforeAllConfigs(Config masterConfig) throws MojoExecutionException, MojoFailureException{
        // no op
    }

    @Override
    public void executeAfterAllConfigs(Config masterConfig) throws MojoExecutionException, MojoFailureException{

        setupBuildEnvironmentInfo();

        final HashSet<String> loadedClasses = new HashSet<String>();

        final List<StepImplementationsDescriptor> classStepTags = new ArrayList<StepImplementationsDescriptor>();

        final PluginDescriptor pluginDescriptor = (PluginDescriptor) getPluginContext().get("pluginDescriptor");
        final ClassRealm classRealm = pluginDescriptor.getClassRealm();
        final File classes = new File(project.getBuild().getOutputDirectory());
        try
        {
            classRealm.addURL(classes.toURI().toURL());
        }
        catch (MalformedURLException e)
        {
            log.error("MalformedURLException adding outputdir", e);
        }

        if (masterConfig != null){
            List<Config> configs = SubstepsConfigLoader.splitMasterConfig(masterConfig);

            Set<String> stepImplementationClassNames = new LinkedHashSet<>();
            Set<String> stepImplsToExclude = new LinkedHashSet<>();

            for (Config executionConfig : configs) {

                stepImplementationClassNames.addAll(NewSubstepsExecutionConfig.getStepImplementationClassNames(executionConfig));

                List<String> excluded = NewSubstepsExecutionConfig.getStepImplementationClassNamesGlossaryExcluded(executionConfig);

                if (excluded != null) {
                    stepImplsToExclude.addAll(excluded);
                }
            }

            if (stepImplsToExclude != null ) {
                stepImplementationClassNames.removeAll(stepImplsToExclude);
            }

            for (final String classToDocument : stepImplementationClassNames) {

                classStepTags.addAll(getStepTags(loadedClasses, classRealm, classToDocument));
            }
        }
        else {

            for (ExecutionConfig cfg : executionConfigs) {

                for (final String classToDocument : cfg.getStepImplementationClassNames()) {
                    classStepTags.addAll(getStepTags(loadedClasses, classRealm, classToDocument));
                }
            }
        }


        if (!classStepTags.isEmpty()) {

            if (glossaryPublisher != null) {

                glossaryPublisher.publish(classStepTags);

            }

            saveJsonFile(classStepTags);
        } else {
            log.error("no results to write out");
        }

    }


    private List<StepImplementationsDescriptor> getStepTags(HashSet<String> loadedClasses, ClassRealm classRealm, String classToDocument) {

        final List<StepImplementationsDescriptor> classStepTags = new ArrayList<StepImplementationsDescriptor>();


        log.debug("documenting: " + classToDocument);

        // have we loaded info for this class already ?
        if (!loadedClasses.contains(classToDocument)) {

            // where is this class ?
            final JarFile jarFileForClass = getJarFileForClass(classToDocument);
            if (jarFileForClass != null) {

                log.debug("loading info from jar");

                // look for the xml file in the jar, load up from
                // there
                loadStepTagsFromJar(jarFileForClass, classStepTags, loadedClasses);
            } else {
                log.debug("loading step info from paths");
                // if it's in the project, run the javadoc and collect the
                // details

                // TODO - if this class is annotated with AdditionalStepImplementations, lookup those instead..

                try {
                    Class<?> stepImplClass = classRealm.loadClass(classToDocument);

                    SubSteps.AdditionalStepImplementations additionalStepImpls = stepImplClass.getDeclaredAnnotation(SubSteps.AdditionalStepImplementations.class);

                    if (additionalStepImpls != null) {
                        for (Class c : additionalStepImpls.value()) {

                            classStepTags.addAll(runJavaDoclet(c.getCanonicalName()));
                        }
                    }

                } catch (ClassNotFoundException e) {
                    log.error("failed to load class: " + classToDocument, e);

                }

                classStepTags.addAll(runJavaDoclet(classToDocument));
            }
        }
        return classStepTags;
    }

    /**
     * @param classStepTags
     */
    private void saveJsonFile(final List<StepImplementationsDescriptor> classStepTags) {

        Gson gson = new GsonBuilder().create();

        final String json = gson.toJson(classStepTags);

        writeOutputFile(json, STEP_IMPLS_JSON_FILENAME);
    }

    private void writeOutputFile(String xml, String filename) {
        final File output = new File(outputDirectory, filename);

        if (!outputDirectory.exists() && !outputDirectory.mkdirs()) {
            throw new IllegalStateException("unable to create output directory");
        }

        try {

            Files.asCharSink(output, Charset.forName("UTF-8")).write(xml);

        } catch (final IOException e) {
            log.error("error writing file", e);
        }
    }


    /**
     * @param jarFileForClass
     * @param classStepTags
     * @param loadedClasses
     */
    private void loadStepTagsFromJar(final JarFile jarFileForClass,
                                     final List<StepImplementationsDescriptor> classStepTags, final Set<String> loadedClasses) {

        // TODO - change this to load from the json version

        final ZipEntry entry = jarFileForClass
                .getEntry("stepimplementations.json");

        if (entry != null) {


            try {
                final InputStream is = jarFileForClass.getInputStream(entry);
                InputStreamReader isr = new InputStreamReader(is);

                Gson gson = new GsonBuilder().create();
                List<StepImplementationsDescriptor>stepDescriptors = gson.fromJson(isr, new TypeToken<List<StepImplementationsDescriptor>>() {
                }.getType());

                classStepTags.addAll(stepDescriptors);


            } catch (final IOException e) {
                log.error("Error loading from jarfile: ", e);
            }

            for (final StepImplementationsDescriptor descriptor : classStepTags) {
                loadedClasses.add(descriptor.getClassName());
            }
        } else {
            log.error("couldn't locate file in jar: stepimplementations.json");
        }
    }


    private static String convertClassNameToPath(final String className) {
        return className.replace('.', '/') + ".class";
    }


    private JarFile getJarFileForClass(final String className) {

        JarFile jarFile = null;
        final Set<Artifact> artifacts = project.getArtifacts();
        if (artifacts != null) {
            for (final Artifact a : artifacts) {
                // does this jar contain this class?
                JarFile tempJarFile = null;
                try {
                    tempJarFile = new JarFile(a.getFile());

                    final JarEntry jarEntry = tempJarFile
                            .getJarEntry(convertClassNameToPath(className));

                    if (jarEntry != null) {
                        jarFile = tempJarFile;
                        break;
                    }
                } catch (final IOException e) {
                    log.error("IO Exception opening jar file", e);
                }
                finally {
                    if (tempJarFile != null){
                        try {
                            tempJarFile.close();
                        } catch (IOException e) {
                            log.debug("ioexcception closing jar file", e);
                        }
                    }
                }
            }
        }
        return jarFile;
    }
}
