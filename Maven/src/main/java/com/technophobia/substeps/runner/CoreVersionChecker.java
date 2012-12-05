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
    private ArtifactFactory artifactFactory;
    private ArtifactResolver artifactResolver;

    private List<ArtifactRepository> remoteRepositories;
    private ArtifactRepository localRepository;

    private MavenProjectBuilder mavenProjectBuilder;

    private final static String SUBSTEPS_GROUP_ID = "com.technophobia.substeps";
    private final static String API_ARTIFACT_ID = "substeps-core-api";
    private final static String CORE_ARTIFACT_ID = "substeps-core";

    private Predicate<Dependency> IS_SUBSTEPS_CORE = new Predicate<Dependency>() {

        public boolean apply(Dependency dependency) {

            return SUBSTEPS_GROUP_ID.equals(dependency.getGroupId())
                    && CORE_ARTIFACT_ID.equals(dependency.getArtifactId());

        }
    };

    private Predicate<Dependency> IS_SUBSTEPS_API = new Predicate<Dependency>() {

        public boolean apply(Dependency dependency) {

            return SUBSTEPS_GROUP_ID.equals(dependency.getGroupId())
                    && API_ARTIFACT_ID.equals(dependency.getArtifactId());

        }
    };

    private Predicate<Artifact> ARTIFACT_IS_SUBSTEPS_API = new Predicate<Artifact>() {

        public boolean apply(Artifact artifact) {

            return SUBSTEPS_GROUP_ID.equals(artifact.getGroupId()) && API_ARTIFACT_ID.equals(artifact.getArtifactId());

        }
    };
    private Log log;

    public static void assertCompatibleVersion(Log log, ArtifactFactory artifactFactory,
            ArtifactResolver artifactResolver, List<ArtifactRepository> remoteRepositories,
            ArtifactRepository localRepository, MavenProjectBuilder mavenProjectBuilder, MavenProject runningProject,
            List<Artifact> pluginsDependencies) throws MojoExecutionException {

        new CoreVersionChecker(log, artifactFactory, artifactResolver, remoteRepositories, localRepository,
                mavenProjectBuilder).checkVersion(runningProject, pluginsDependencies);
    }

    public CoreVersionChecker(Log log, ArtifactFactory artifactFactory, ArtifactResolver artifactResolver,
            List<ArtifactRepository> remoteRepositories, ArtifactRepository localRepository,
            MavenProjectBuilder mavenProjectBuilder) {
        this.log = log;
        this.artifactFactory = artifactFactory;
        this.artifactResolver = artifactResolver;
        this.remoteRepositories = remoteRepositories;
        this.localRepository = localRepository;
        this.mavenProjectBuilder = mavenProjectBuilder;
    }

    public void checkVersion(MavenProject runningProject, List<Artifact> pluginsDependencies)
            throws MojoExecutionException {

        Dependency substepsCoreDependency = Iterables.find((List<Dependency>) runningProject.getTestDependencies(),
                IS_SUBSTEPS_CORE, null);

        if (substepsCoreDependency == null) {

            log.warn("Invalid plugin configuration, no version of " + CORE_ARTIFACT_ID + " found");

        } else {
            MavenProject coreProject = loadProject(substepsCoreDependency);

            Dependency apiDependencyInCore = Iterables.find((List<Dependency>) coreProject.getDependencies(),
                    IS_SUBSTEPS_API, null);

            Artifact apiArtifactInPlugin = Iterables.find(pluginsDependencies, ARTIFACT_IS_SUBSTEPS_API, null);

            assertSameVersion(apiDependencyInCore, apiArtifactInPlugin);
        }
    }

    private MavenProject loadProject(Dependency substepsCoreDependency) throws MojoExecutionException {

        Artifact corePomArtifact = artifactFactory.createArtifact(SUBSTEPS_GROUP_ID, CORE_ARTIFACT_ID,
                substepsCoreDependency.getVersion(), "test", "pom");
        try {

            artifactResolver.resolve(corePomArtifact, remoteRepositories, localRepository);

            return mavenProjectBuilder.buildFromRepository(corePomArtifact, remoteRepositories, localRepository);

        } catch (ArtifactResolutionException e) {

            throw new MojoExecutionException(EXCEPTION_PREFIX
                    + "unable to find pom for version of core in dependencies", e);
        } catch (ArtifactNotFoundException e) {
            throw new MojoExecutionException(EXCEPTION_PREFIX
                    + "unable to find pom for version of core in dependencies", e);
        } catch (ProjectBuildingException pbe) {

            throw new MojoExecutionException(EXCEPTION_PREFIX + "unable to build pom of core", pbe);
        }

    }

    private void assertSameVersion(Dependency apiDependencyInCore, Artifact apiArtifactInPlugin)
            throws MojoExecutionException {

        if (apiDependencyInCore == null) {

            log.warn(EXCEPTION_PREFIX + "no version of the api found in core");
        } else if (apiArtifactInPlugin == null) {

            throw new MojoExecutionException(EXCEPTION_PREFIX
                    + "no version of the api found in this plugins depdendencies");
        } else {

            if (!apiDependencyInCore.getVersion().equals(apiArtifactInPlugin.getVersion())) {

                throw new MojoExecutionException(
                        "Configuration invalid, the version of core references is using version '"
                                + apiDependencyInCore.getVersion()
                                + "' of the substeps API whilst this plugin is compiled against '"
                                + apiArtifactInPlugin.getVersion() + "'");
            }
        }
    }
}
