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

import com.google.common.io.CharStreams;
import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.sun.tools.javadoc.Main;
import com.technophobia.substeps.model.SubSteps;
import com.technophobia.substeps.runner.BaseSubstepsMojo;
import com.technophobia.substeps.runner.ExecutionConfig;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
    private final GlossaryPublisher glossaryPublisher = null;

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
                    // "-docletpath",
                    // "/home/ian/projects/webdriverbdd-utils/target/classes",
                    // // path to this jar ?
                    "-sourcepath", sourceRoot, // "./src/main/java", // path to
                    // the step impls / classpath ?
                    path // javadocStr
                    // //"/home/ian/projects/github/substeps-webdriver/src/main/java/com/technophobia/webdriver/substeps/impl/AssertionWebDriverSubStepImplementations.java"
            }; // list of step impls to have a butcher sat
            // "/home/ian/projects/github/substeps-webdriver/src/main/java/com/technophobia/webdriver/substeps/impl/AssertionWebDriverSubStepImplementations.java"

            Main.execute(args);

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
    public void execute() throws MojoExecutionException, MojoFailureException {

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
//            e.printStackTrace();
        }


        for(ExecutionConfig cfg : executionConfigs) {


            for (final String classToDocument : cfg.getStepImplementationClassNames()) {

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

                            if (additionalStepImpls != null){
                                for(Class c : additionalStepImpls.value()){

                                    classStepTags.addAll(runJavaDoclet(c.getCanonicalName()));
                                }
                            }

                        } catch (ClassNotFoundException e) {
                            log.error("failed to load class: " + classToDocument, e);

                        }

                        classStepTags.addAll(runJavaDoclet(classToDocument));
                    }
                }
            }
        }


        if (!classStepTags.isEmpty()) {

            if (glossaryPublisher != null) {

                glossaryPublisher.publish(classStepTags);

            }

            // always do this
//            saveXMLFile(classStepTags);

            // and this!
            saveJsonFile(classStepTags);
        } else {
            log.error("no results to write out");
        }
    }

    /**
     * @param classStepTags
     */
    private void saveJsonFile(final List<StepImplementationsDescriptor> classStepTags) {

        Gson gson = new GsonBuilder().create();

        final String json = gson.toJson(classStepTags);

        writeOutputFile(json, STEP_IMPLS_JSON_FILENAME);
    }

//    /**
//     * @param classStepTags
//     */
//    private void saveXMLFile(final List<StepImplementationsDescriptor> classStepTags) {
//        // got them all now serialize
//
//        final String xml = serializer.toXML(classStepTags);
//
//        writeOutputFile(xml, XMLSubstepsGlossarySerializer.XML_FILE_NAME);
//
//    }

    private void writeOutputFile(String xml, String filename) {
        final File output = new File(outputDirectory, filename);

        if (!outputDirectory.exists()) {
            if (!outputDirectory.mkdirs()) {

                throw new IllegalStateException("unable to create output directory");
            }
        }

        try {
            Files.write(xml, output, Charset.forName("UTF-8"));
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


    private String convertClassNameToPath(final String className) {
        return className.replace('.', '/') + ".class";
    }


    private JarFile getJarFileForClass(final String className) {

        JarFile jarFile = null;
        final Set<Artifact> artifacts = project.getArtifacts();
        if (artifacts != null) {
            for (final Artifact a : artifacts) {
                // does this jar contain this class?
                try {
                    final JarFile tempJarFile = new JarFile(a.getFile());

                    final JarEntry jarEntry = tempJarFile
                            .getJarEntry(convertClassNameToPath(className));

                    if (jarEntry != null) {
                        jarFile = tempJarFile;
                        break;
                    }
                } catch (final IOException e) {
                    log.error("IO Exception opening jar file", e);
                }
            }
        }
        return jarFile;
    }
}
