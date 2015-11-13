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

import java.util.List;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectBuilder;
import org.apache.maven.project.ProjectBuildingException;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

/**
 * 
 * The version of core which can be used with the maven plugin can be changed
 * but it must share the same version of the API.
 * 
 * @author rbarefield
 */
public class CoreVersionChecker {

    private static final String EXCEPTION_PREFIX = "Exception whilst checking core version, ";
    private final ArtifactFactory artifactFactory;
    private final ArtifactResolver artifactResolver;

    private final List<ArtifactRepository> remoteRepositories;
    private final ArtifactRepository localRepository;

    private final MavenProjectBuilder mavenProjectBuilder;

    private final static String SUBSTEPS_GROUP_ID = "com.technophobia.substeps";
    private final static String API_ARTIFACT_ID = "substeps-core-api";
    private final static String CORE_ARTIFACT_ID = "substeps-core";

    private final Predicate<Dependency> IS_SUBSTEPS_CORE = new Predicate<Dependency>() {

        public boolean apply(final Dependency dependency) {

            return SUBSTEPS_GROUP_ID.equals(dependency.getGroupId())
                    && CORE_ARTIFACT_ID.equals(dependency.getArtifactId());

        }
    };

    private final Predicate<Dependency> IS_SUBSTEPS_API = new Predicate<Dependency>() {

        public boolean apply(final Dependency dependency) {

            return SUBSTEPS_GROUP_ID.equals(dependency.getGroupId())
                    && API_ARTIFACT_ID.equals(dependency.getArtifactId());

        }
    };

    private final Predicate<Artifact> ARTIFACT_IS_SUBSTEPS_API = new Predicate<Artifact>() {

        public boolean apply(final Artifact artifact) {

            return SUBSTEPS_GROUP_ID.equals(artifact.getGroupId()) && API_ARTIFACT_ID.equals(artifact.getArtifactId());

        }
    };
    private final Log log;

    public static void assertCompatibleVersion(final Log log, final ArtifactFactory artifactFactory,
            final ArtifactResolver artifactResolver, final List<ArtifactRepository> remoteRepositories,
            final ArtifactRepository localRepository, final MavenProjectBuilder mavenProjectBuilder,
            final MavenProject runningProject, final List<Artifact> pluginsDependencies) throws MojoExecutionException {

        new CoreVersionChecker(log, artifactFactory, artifactResolver, remoteRepositories, localRepository,
                mavenProjectBuilder).checkVersion(runningProject, pluginsDependencies);
    }

    public CoreVersionChecker(final Log log, final ArtifactFactory artifactFactory,
            final ArtifactResolver artifactResolver, final List<ArtifactRepository> remoteRepositories,
            final ArtifactRepository localRepository, final MavenProjectBuilder mavenProjectBuilder) {
        this.log = log;
        this.artifactFactory = artifactFactory;
        this.artifactResolver = artifactResolver;
        this.remoteRepositories = remoteRepositories;
        this.localRepository = localRepository;
        this.mavenProjectBuilder = mavenProjectBuilder;
    }

    public void checkVersion(final MavenProject runningProject, final List<Artifact> pluginsDependencies)
            throws MojoExecutionException {

        final Dependency substepsCoreDependency = Iterables.find(
                (List<Dependency>) runningProject.getTestDependencies(), IS_SUBSTEPS_CORE, null);

        if (substepsCoreDependency == null) {

            log.warn("Invalid plugin configuration, no version of " + CORE_ARTIFACT_ID + " found");

        } else {
            final MavenProject coreProject = loadProject(substepsCoreDependency);

            final Dependency apiDependencyInCore = Iterables.find((List<Dependency>) coreProject.getDependencies(),
                    IS_SUBSTEPS_API, null);

            final Artifact apiArtifactInPlugin = Iterables.find(pluginsDependencies, ARTIFACT_IS_SUBSTEPS_API, null);

            assertSameVersion(apiDependencyInCore, apiArtifactInPlugin);
        }
    }

    private MavenProject loadProject(final Dependency substepsCoreDependency) throws MojoExecutionException {

        final Artifact corePomArtifact = artifactFactory.createArtifact(SUBSTEPS_GROUP_ID, CORE_ARTIFACT_ID,
                substepsCoreDependency.getVersion(), "test", "pom");
        try {

            artifactResolver.resolve(corePomArtifact, remoteRepositories, localRepository);

            return mavenProjectBuilder.buildFromRepository(corePomArtifact, remoteRepositories, localRepository);

        } catch (final ArtifactResolutionException e) {

            throw new MojoExecutionException(EXCEPTION_PREFIX
                    + "unable to find pom for version of core in dependencies", e);
        } catch (final ArtifactNotFoundException e) {
            throw new MojoExecutionException(EXCEPTION_PREFIX
                    + "unable to find pom for version of core in dependencies", e);
        } catch (final ProjectBuildingException pbe) {

            throw new MojoExecutionException(EXCEPTION_PREFIX + "unable to build pom of core", pbe);
        }

    }

    private void assertSameVersion(final Dependency apiDependencyInCore, final Artifact apiArtifactInPlugin)
            throws MojoExecutionException {

        if (apiDependencyInCore == null) {

            log.warn(EXCEPTION_PREFIX + "no version of the api found in core");
        } else if (apiArtifactInPlugin == null) {

            throw new MojoExecutionException(EXCEPTION_PREFIX
                    + "no version of the api found in this plugins depdendencies");
        } else {

            if (!apiDependencyInCore.getVersion().equals(apiArtifactInPlugin.getVersion())) {

                // Throwing an exception here is too prohibitive - makes running
                // against snapshot versions fail eg.
                // Configuration invalid, the version of core references is
                // using version '1.1.3-SNAPSHOT' of the substeps API whilst
                // this plugin is compiled against '1.1.3-20140609.141134-3'

                log.warn("Configuration * may * be invalid, the version of core references is using version '"
                        + apiDependencyInCore.getVersion()
                        + "' of the substeps API whilst this plugin is compiled against '"
                        + apiArtifactInPlugin.getVersion() + "'");
            }
        }
    }
}
