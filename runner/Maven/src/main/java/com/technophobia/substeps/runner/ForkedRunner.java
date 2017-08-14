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
package com.technophobia.substeps.runner;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.technophobia.substeps.execution.AbstractExecutionNodeVisitor;
import com.technophobia.substeps.execution.ExecutionNodeResult;
import com.technophobia.substeps.execution.ExecutionResult;
import com.technophobia.substeps.execution.node.IExecutionNode;
import com.technophobia.substeps.execution.node.RootNode;
import com.typesafe.config.Config;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.metadata.ArtifactMetadataSource;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.*;
import org.apache.maven.project.artifact.InvalidDependencyVersionException;
import org.substeps.execution.ExecutionNodeResultNotificationHandler;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.*;
import java.util.concurrent.CountDownLatch;

public class ForkedRunner implements MojoRunner, ExecutionNodeResultNotificationHandler {

    public static final String UNABLE_TO_RESOLVE_ARTIFACT_FOR_SUBSTEP_IMPLEMENTATION = "Unable to resolve artifact for substep implementation: ";
    private final Log log;

    private Process forkedJVMProcess = null;

    private SubstepsJMXClient substepsJmxClient = new SubstepsJMXClient();

    private final int jmxPort;

    private final String vmArgs;

    private final List<String> testClasspathElements;

    private final List<String> stepImplementationArtifacts;

    private final ArtifactResolver artifactResolver;

    private final ArtifactFactory artifactFactory;

    private final ProjectBuilder projectBuilder;

    private final ArtifactRepository localRepository;

    private final List<ArtifactRepository> remoteRepositories;

    private final ArtifactMetadataSource artifactMetadataSource;

    private ForkedProcessCloser shutdownHook;

    private final InputStreamConsumer consumer;

    private final CountDownLatch countDownLatch = new CountDownLatch(1);

    private Map<Long, IExecutionNode> nodeMap = new HashMap<>();

    private List<IExecutionListener> listeners = new ArrayList<>();


    ForkedRunner(final Log log, final int jmxPort, final String vmArgs, final List<String> testClasspathElements,
                 final List<String> stepImplementationArtifacts, final ArtifactResolver artifactResolver,
                 final ArtifactFactory artifactFactory, final ProjectBuilder projectBuilder,
                 final ArtifactRepository localRepository, final List<ArtifactRepository> remoteRepositories,
                 final ArtifactMetadataSource artifactMetadataSource) throws MojoExecutionException {

        this.log = log;
        this.jmxPort = jmxPort;
        this.vmArgs = vmArgs;
        this.testClasspathElements = testClasspathElements;
        this.stepImplementationArtifacts = stepImplementationArtifacts;
        this.artifactResolver = artifactResolver;
        this.artifactFactory = artifactFactory;
        this.projectBuilder = projectBuilder;
        this.localRepository = localRepository;
        this.remoteRepositories = remoteRepositories;
        this.artifactMetadataSource = artifactMetadataSource;

        this.substepsJmxClient = new SubstepsJMXClient();

        this.substepsJmxClient.setNotificiationHandler(this);

        this.consumer = startMBeanJVM();

        initialiseClient();
    }

    private void initialiseClient() throws MojoExecutionException {

        this.substepsJmxClient.init(this.jmxPort);
    }

    @Override
    public void shutdown() {

        this.substepsJmxClient.shutdown();

        if (this.forkedJVMProcess != null) {

            try {
                this.log.info("waiting for forked process to return");

                final int waitFor = this.forkedJVMProcess.waitFor();

                this.log.info("wait for forked VM returned with exit code: " + waitFor);

                this.shutdownHook.notifyShutdownSuccessful();

                // now we can close the streams
                if (this.consumer != null) {
                    this.consumer.closeStreams();
                }

            } catch (final InterruptedException e) {
                // not sure what we can do at this point...
                log.error("InterruptedException waiting for shutdown", e);
            }
        }

        this.log.info("forked process returned");

    }


    private InputStreamConsumer startMBeanJVM() throws MojoExecutionException {
        // launch the jvm process that will contain the Substeps MBean Server
        // build up the class path based on this projects classpath

        InputStreamConsumer localConsumer = null;

        final List<String> command = buildSubstepsRunnerCommand();

        final ProcessBuilder processBuilder = new ProcessBuilder(command);

        processBuilder.redirectErrorStream(true);

        try {

            this.log.info("Starting substeps process with command " + Joiner.on(" ").join(processBuilder.command()));

            this.forkedJVMProcess = processBuilder.start();

            // need to add the shutdown hook straight away
            this.shutdownHook = ForkedProcessCloser.addHook(this.substepsJmxClient, this.forkedJVMProcess, this.log);

            localConsumer = new InputStreamConsumer(this.forkedJVMProcess.getInputStream(), this.log);

            final Thread t = new Thread(localConsumer);
            t.start();

        } catch (final IOException e) {
            log.error("IOException starting", e);
        }

        this.log.info("process started");

        return localConsumer;
    }

    /**
     * @return
     * @throws MojoExecutionException
     * @throws DependencyResolutionRequiredException
     */
    private List<String> buildSubstepsRunnerCommand() throws MojoExecutionException {

        final String classpath = createClasspathString();

        final List<String> command = Lists.newArrayList();

        // attempt to use JAVA_HOME
        String javaHome = System.getenv("JAVA_HOME");
        if (javaHome == null) {
            javaHome = System.getenv("java_home");
        }

        if (javaHome == null) {
            // not sure how we'd get here - maven running without JAVA_HOME
            // set..??
            this.log.warn("unable to resolve JAVA_HOME variable, assuming java is on the path...");
            command.add("java");
        } else {
            command.add(javaHome + File.separator + "bin" + File.separator + "java");
        }

        command.add("-Dfile.encoding=UTF-8");
        command.add("-Dcom.sun.management.jmxremote.port=" + this.jmxPort);
        command.add("-Dcom.sun.management.jmxremote.authenticate=false");
        command.add("-Dcom.sun.management.jmxremote.ssl=false");
        command.add("-Djava.rmi.server.hostname=localhost");

        if (this.vmArgs != null && !this.vmArgs.isEmpty()) {
            final String[] args = this.vmArgs.split(" ");
            for (final String arg : args) {
                command.add(arg);
                this.log.info("Adding jvm arg: " + arg);
            }
        }

        command.add("-classpath");
        command.add(classpath);
        command.add("com.technophobia.substeps.jmx.SubstepsJMXServer");
        return command;
    }

    private String createClasspathString() throws MojoExecutionException {

        final List<String> classPathElements = Lists.newArrayList();

        classPathElements.addAll(this.testClasspathElements);
        classPathElements.addAll(resolveStepImplementationArtifacts());

        return Joiner.on(File.pathSeparator).join(classPathElements);
    }

    @SuppressWarnings("unchecked")
    private List<String> resolveStepImplementationArtifacts() throws MojoExecutionException {

        final List<String> stepImplementationArtifactJars = Lists.newArrayList();
        if (this.stepImplementationArtifacts != null) {
            for (final String stepImplementationArtifactString : this.stepImplementationArtifacts) {

                final String[] artifactDetails = stepImplementationArtifactString.split(":");

                if (artifactDetails.length != 3) {
                    throw new MojoExecutionException(
                            "Invalid artifact format found in substepImplementationArtifact, must be in format groupId:artifactId:version but was '"
                                    + stepImplementationArtifactString + "'");
                }

                try {

                    final Artifact stepImplementationJarArtifact = this.artifactFactory.createArtifact(
                            artifactDetails[0], artifactDetails[1], artifactDetails[2], "test", "jar");
                    this.artifactResolver.resolve(stepImplementationJarArtifact, this.remoteRepositories,
                            this.localRepository);

                    addArtifactPath(stepImplementationArtifactJars, stepImplementationJarArtifact);

                    final Artifact stepImplementationPomArtifact = this.artifactFactory.createArtifact(
                            artifactDetails[0], artifactDetails[1], artifactDetails[2], "test", "pom");
                    this.artifactResolver.resolve(stepImplementationPomArtifact, this.remoteRepositories,
                            this.localRepository);

                    DefaultProjectBuildingRequest request = new DefaultProjectBuildingRequest();
                    request.setRemoteRepositories(remoteRepositories);
                    request.setLocalRepository(this.localRepository);

                    ProjectBuildingResult buildResult = this.projectBuilder.build(stepImplementationPomArtifact, request);

                    final MavenProject stepImplementationProject = buildResult.getProject();

                    final Set<Artifact> stepImplArtifacts = stepImplementationProject.createArtifacts(
                            this.artifactFactory, null, null);

                    final Set<Artifact> transitiveDependencies = this.artifactResolver.resolveTransitively(
                            stepImplArtifacts, stepImplementationPomArtifact,
                            stepImplementationProject.getManagedVersionMap(), this.localRepository,
                            this.remoteRepositories, this.artifactMetadataSource).getArtifacts();

                    for (final Artifact transitiveDependency : transitiveDependencies) {
                        addArtifactPath(stepImplementationArtifactJars, transitiveDependency);
                    }

                } catch (final ArtifactResolutionException e) {

                    throw new MojoExecutionException(UNABLE_TO_RESOLVE_ARTIFACT_FOR_SUBSTEP_IMPLEMENTATION
                            + stepImplementationArtifactString, e);

                } catch (final InvalidDependencyVersionException e) {

                    throw new MojoExecutionException(UNABLE_TO_RESOLVE_ARTIFACT_FOR_SUBSTEP_IMPLEMENTATION
                            + stepImplementationArtifactString, e);
                } catch (final ProjectBuildingException e) {

                    throw new MojoExecutionException(UNABLE_TO_RESOLVE_ARTIFACT_FOR_SUBSTEP_IMPLEMENTATION
                            + stepImplementationArtifactString, e);
                } catch (final ArtifactNotFoundException e) {

                    throw new MojoExecutionException(UNABLE_TO_RESOLVE_ARTIFACT_FOR_SUBSTEP_IMPLEMENTATION
                            + stepImplementationArtifactString, e);
                }

            }
        }
        return stepImplementationArtifactJars;
    }

    private void addArtifactPath(final List<String> stepImplementationArtifactJars, final Artifact artifact) {
        final String path = artifact.getFile().getPath();
        this.log.info("Adding dependency to classpath for forked jvm: " + path);
        stepImplementationArtifactJars.add(path);
    }


    protected RootNode getRootNodeFromBytes(byte[] bytes) {
        RootNode rn = null;
        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        ObjectInputStream ois;
        try {
            ois = new ObjectInputStream(bis);
            rn = (RootNode)ois.readObject();

        } catch (IOException e) {
            log.debug("IOException getting rootNodeFromBytes", e);
        } catch (ClassNotFoundException e) {

            log.debug("ClassNotFoundException getting rootNodeFromBytes", e);

        }
        finally{
            try {
                bis.close();
            } catch (IOException e) {
                log.debug("IOException getting rootNodeFromBytes", e);
            }
        }
        return rn;
    }


    @Override
    public RootNode prepareExecutionConfig(Config theConfig) {

        // TODO
        byte[] bytes = substepsJmxClient.prepareExecutionConfigAsBytes(theConfig.root().render());

        RootNode rootNode = getRootNodeFromBytes(bytes);

        log.debug("rootNode.toDebugString():\n" + rootNode.toDebugString());

        List<IExecutionNode> nodes = flattenTree(rootNode);
        for (IExecutionNode n : nodes){
            nodeMap.put(n.getId(), n);
        }

        return rootNode;
    }

    @Override
    public RootNode run() {

        this.log.info("Running substeps tests in forked jvm");

        RootNode resultNode = getRootNodeFromBytes(substepsJmxClient.runAsBytes());

        // lets wait for any notifications
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            if (countDownLatch.getCount() > 0) {
                log.error("premature interupted ex waiting for complete notification");
            }
        }
        return  resultNode;
    }

    @Override
    public List<SubstepExecutionFailure> getFailures() {

        return this.substepsJmxClient.getFailures();
    }

    @Override
    public void addNotifier(final IExecutionListener listener) {

        this.substepsJmxClient.addNotifier(listener);
        listeners.add(listener);

    }

    @Override
    public void handleNotification(ExecutionNodeResult resultNotification) {

        // reinflate the resultNotification back into something the collector can use..

        IExecutionNode iExecutionNode = nodeMap.get(resultNotification.getExecutionNodeId());

        ExecutionNodeResult nodeResult = iExecutionNode.getResult();

        nodeResult.setResult(resultNotification.getResult()); // how many results!
        nodeResult.setSubstepExecutionFailure(resultNotification.getFailure());
        nodeResult.setStartedAt(resultNotification.getStartedAt());
        nodeResult.setCompletedAt(resultNotification.getCompletedAt());
        nodeResult.setScreenshot(resultNotification.getScreenshot());


        if (resultNotification.getResult().isFailure()){
            listeners.forEach(l -> l.onNodeFailed(iExecutionNode, nodeResult.getThrown()));
        }

        else if (resultNotification.getResult() == ExecutionResult.PASSED){
            listeners.forEach(l -> l.onNodeFinished(iExecutionNode));
        }
        else if (resultNotification.getResult() == ExecutionResult.IGNORED){
            listeners.forEach(l -> l.onNodeIgnored(iExecutionNode));
        }
        else if (resultNotification.getResult() == ExecutionResult.RUNNING){
            // no op
        }
        else {
            log.warn("unhandled result notification: " + resultNotification.getResult());
        }

    }


    private List<IExecutionNode> flattenTree(final IExecutionNode node) {

        return node.accept(new AbstractExecutionNodeVisitor<IExecutionNode>() {

            @Override
            public IExecutionNode visit(IExecutionNode node) {
                return node;
            }
        });
    }

    @Override
    public void handleCompleteMessage() {
        countDownLatch.countDown();
    }
}
