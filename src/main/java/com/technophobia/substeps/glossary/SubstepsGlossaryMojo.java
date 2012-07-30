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
package com.technophobia.substeps.glossary;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.Files;
import com.sun.tools.javadoc.Main;

/**
 * 
 * @goal generate-docs
 * @requiresDependencyResolution test
 * @phase generate-resources
 * 
 * @configurator include-project-dependencies
 */
public class SubstepsGlossaryMojo extends AbstractMojo {

    private final Logger log = LoggerFactory.getLogger(SubstepsGlossaryMojo.class);

    /**
     * Location of the file.
     * 
     * @parameter expression="${project.build.outputDirectory}"
     * @required
     */
    private File outputDirectory;

    /**
     * @parameter default-value="${project}"
     * @required
     * @readonly
     */
    private MavenProject project;

    /**
     * @parameter
     * @required
     */
    private String[] stepImplementationClassNames;

    /**
     * @parameter
     */
    private final GlossaryPublisher glossaryPublisher = null;

    private final XMLSubstepsGlossarySerializer serializer = new XMLSubstepsGlossarySerializer();


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

            final String[] args = { "-doclet", "com.technophobia.substeps.glossary.CustomDoclet",
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
     * @param classToDocument
     *            = fqn of a class, dotted syntax
     * @param isSourcePath
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


    public void execute() throws MojoExecutionException, MojoFailureException {

        final HashSet<String> loadedClasses = new HashSet<String>();

        final List<StepImplementationsDescriptor> classStepTags = new ArrayList<StepImplementationsDescriptor>();

        for (final String classToDocument : stepImplementationClassNames) {

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

                    classStepTags.addAll(runJavaDoclet(classToDocument));
                }
            }
        }

        if (!classStepTags.isEmpty()) {

            if (glossaryPublisher != null) {

                glossaryPublisher.publish(classStepTags);

            }

            // always do this
            saveXMLFile(classStepTags);
        } else {
            log.error("no results to write out");
        }
    }


    /**
     * @param classStepTags
     * 
     */
    private void saveXMLFile(final List<StepImplementationsDescriptor> classStepTags) {
        // got them all now serialize

        final String xml = serializer.toXML(classStepTags);

        final File output = new File(outputDirectory, XMLSubstepsGlossarySerializer.XML_FILE_NAME);

        if (!outputDirectory.exists()) {
            if (!outputDirectory.mkdirs()) {

                throw new IllegalStateException("unable to create output directory");
            }
        }

        try {
            Files.write(xml, output, Charset.forName("UTF-8"));
        } catch (final IOException e) {
            e.printStackTrace();
        }

    }


    /**
     * @param jarFileForClass
     * @param classStepTags
     * @param loadedClasses
     */
    private void loadStepTagsFromJar(final JarFile jarFileForClass,
            final List<StepImplementationsDescriptor> classStepTags, final Set<String> loadedClasses) {
        final ZipEntry entry = jarFileForClass
                .getEntry(XMLSubstepsGlossarySerializer.XML_FILE_NAME);

        if (entry != null) {

            final List<StepImplementationsDescriptor> classStepTagList = serializer
                    .loadStepImplementationsDescriptorFromJar(jarFileForClass);

            classStepTags.addAll(classStepTagList);

            for (final StepImplementationsDescriptor descriptor : classStepTagList) {
                loadedClasses.add(descriptor.getClassName());
            }
        } else {
            log.error("couldn't locate file in jar: " + XMLSubstepsGlossarySerializer.XML_FILE_NAME);
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
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
        return jarFile;
    }
}
