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
package com.technophobia.substeps.mojo.runner;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import junit.framework.Assert;

import org.junit.Ignore;
import org.junit.Test;

import com.technophobia.substeps.execution.node.ExecutionNode;
import com.technophobia.substeps.execution.node.IExecutionNode;
import com.technophobia.substeps.execution.node.RootNode;
import com.technophobia.substeps.glossary.SubstepsGlossaryMojo;
import com.technophobia.substeps.runner.SubstepsExecutionConfig;

/**
 * 
 * 
 * 
 * @author imoore
 * 
 */
public class SubstepsGlossaryMojoTest {

    @Ignore("incomplete test")
    @Test
    public void testCriticalNonCriticalFailures() throws SecurityException, NoSuchMethodException,
            IllegalArgumentException, IllegalAccessException, InvocationTargetException, NoSuchFieldException {

        final SubstepsGlossaryMojo mojo = new SubstepsGlossaryMojo();

        final Method method = mojo.getClass().getMethod("checkRootNodeForFailure", ExecutionNode.class,
                SubstepsExecutionConfig.class);

        final Field failedNodesField = mojo.getClass().getField("failedNodes");
        final Field nonFatalFailedNodesField = mojo.getClass().getField("nonFatalFailedNodes");

        failedNodesField.setAccessible(true);
        nonFatalFailedNodesField.setAccessible(true);

        Assert.assertNotNull(method);
        method.setAccessible(true);

        final SubstepsExecutionConfig execConfig = new SubstepsExecutionConfig();

        final IExecutionNode rootNode = new RootNode("", null);

        method.invoke(mojo, rootNode, execConfig);

        final List<ExecutionNode> failedNodes = (List<ExecutionNode>) failedNodesField.get(mojo);
        final List<ExecutionNode> nonFatalFailedNodes = (List<ExecutionNode>) nonFatalFailedNodesField.get(mojo);

    }

}
