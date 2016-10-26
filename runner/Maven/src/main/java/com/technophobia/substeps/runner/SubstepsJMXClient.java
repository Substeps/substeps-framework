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

import com.technophobia.substeps.execution.ExecutionNodeResult;
import com.technophobia.substeps.execution.node.RootNode;
import com.technophobia.substeps.jmx.SubstepsServerMBean;
import com.technophobia.substeps.model.exception.SubstepsConfigurationException;
import org.apache.maven.plugin.MojoExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.substeps.execution.ExecutionNodeResultNotificationHandler;

import javax.management.*;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import javax.naming.ServiceUnavailableException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author ian
 */
public class SubstepsJMXClient implements SubstepsRunner, NotificationListener {

    private static Logger log = LoggerFactory.getLogger(SubstepsJMXClient.class);
    private static final int JMX_CLIENT_TIMEOUT_SECS = 10;
    private SubstepsServerMBean mbean;

    private JMXConnector cntor = null;
    private MBeanServerConnection mbsc = null;

    public void setNotificiationHandler(ExecutionNodeResultNotificationHandler notificiationHandler) {
        this.notificiationHandler = notificiationHandler;
    }

    private ExecutionNodeResultNotificationHandler notificiationHandler = null;

    public byte[] runAsBytes() {

        return this.mbean.runAsBytes();
    }


    public void init(final int portNumber) throws MojoExecutionException {

        final String url = "service:jmx:rmi:///jndi/rmi://:" + portNumber + "/jmxrmi";

        // The address of the connector server

        try {
            final JMXServiceURL serviceURL = new JMXServiceURL(url);

            final Map<String, ?> environment = null;

            // Create the JMXCconnectorServer
            this.cntor = getConnector(serviceURL, environment);

            if (this.cntor != null){

                // Obtain a "stub" for the remote MBeanServer
                mbsc = this.cntor.getMBeanServerConnection();

            final ObjectName objectName = new ObjectName(SubstepsServerMBean.SUBSTEPS_JMX_MBEAN_NAME);
            this.mbean = MBeanServerInvocationHandler.newProxyInstance(mbsc, objectName, SubstepsServerMBean.class,
                    false);

            addNotificationListener(objectName);

			}

        } catch (final IOException e) {

            throw new MojoExecutionException("Failed to connect to substeps server", e);

        } catch (final MalformedObjectNameException e) {

            throw new MojoExecutionException("Failed to connect to substeps server", e);
        }
    }

    protected void addNotificationListener(ObjectName objectName) throws IOException {

        boolean added = false;
        int tries = 0;

        while (!added || tries < 3) {

            try {
                tries++;
                mbsc.addNotificationListener(objectName, this, null, null);
                added = true;
            } catch (InstanceNotFoundException e) {
                log.debug("adding notification InstanceNotFoundException", e);
            }
        }
    }


    protected JMXConnector getConnector(JMXServiceURL serviceURL, Map<String, ?> environment) throws IOException {
        // Create the JMXCconnectorServer

        JMXConnector connector = null;

        long timeout = System.currentTimeMillis() + (JMX_CLIENT_TIMEOUT_SECS * 1000);

        while (connector == null && System.currentTimeMillis() < timeout) {

            try {
                log.debug("trying to connect to: " + serviceURL);
                connector = JMXConnectorFactory.connect(serviceURL, environment);

                log.debug("connected");
            } catch (IOException e) {

                log.debug("e.getCause(): " + e.getCause().getClass());

                if (!(e.getCause() instanceof ServiceUnavailableException)) {
                    log.error("not a ServiceUnavailableException", e);
                    break;
                }

                log.debug("ConnectException sleeping..");
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e1) {
                    log.debug("InterruptedException:", e1);
                }
            }
        }
        if (connector == null) {

            log.error("failed to get the JMXConnector in time");
        }

        return connector;
    }

    public byte[] prepareExecutionConfigAsBytes(final SubstepsExecutionConfig cfg) {

        try {
            return this.mbean.prepareExecutionConfigAsBytes(cfg);
        }
        catch (SubstepsConfigurationException ex){
            log.error("Failed to init tests: " + ex.getMessage());
            return null;
        }
    }

    @Override
    public RootNode prepareExecutionConfig(final SubstepsExecutionConfig cfg) {

        try {
            final ObjectName objectName = new ObjectName(SubstepsServerMBean.SUBSTEPS_JMX_MBEAN_NAME);
            Object  rootNode = mbsc.invoke(objectName,
                    "prepareExecutionConfig",
                    new Object[]{cfg},
                    new String[]{SubstepsExecutionConfig.class.getName()});
            return (RootNode)rootNode;

        } catch (Exception e) {
            log.error("Exception thrown preparing exectionConfig", e);
        }
        return null;
    }

	@Override
    public List<SubstepExecutionFailure> getFailures() {

        return this.mbean.getFailures();
    }

    @Override
    public RootNode run() {

        return this.mbean.run();
    }

    @Override
    public void addNotifier(final IExecutionListener listener) {

        //
        this.mbean.addNotifier(listener);
    }


    public boolean shutdown() {

        boolean successfulShutdown = false;
        try {
            if (this.mbean != null) {
                this.mbean.shutdown();
            }
                successfulShutdown = true;

        } catch (final RuntimeException re) {

            log.debug("Unable to connect to server to shutdown, it may have already closed", re);

        }
        return successfulShutdown;
    }

    @Override
    public void handleNotification(Notification notification, Object handback) {


        if (notification.getType().compareTo("ExNode") == 0) {
            byte[] rawBytes = (byte[]) notification.getUserData();

            ExecutionNodeResult result = getFromBytes(rawBytes);

            log.trace("received a JMX event msg: " + notification.getMessage() +
                    " seq: " + notification.getSequenceNumber() + " exec result node id: " + result.getExecutionNodeId());

                    notificiationHandler.handleNotification(result);
        } else if (notification.getType().compareTo("ExecConfigComplete") == 0) {
                    notificiationHandler.handleCompleteMessage();
        } else {
            log.error("unknown notificaion type");
        }
    }

    protected static <T> T getFromBytes(byte[] bytes) {
        T rn = null;
        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        ObjectInputStream ois;
        try {
            ois = new ObjectInputStream(bis);
            rn = (T) ois.readObject();

        } catch (IOException e) {
            log.error("IOException reading object input stream", e);
        } catch (ClassNotFoundException e) {

            log.error("ClassNotFoundException", e);

        } finally {
            try {
                bis.close();
            } catch (IOException e) {
                log.error("IOException closing object input stream", e);
            }
        }
        return rn;
    }

}
