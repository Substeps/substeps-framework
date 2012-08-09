/*
 *	Copyright Technophobia Ltd 2012
 *
 *   This file is part of Substeps Maven Runner.
 *
 *    Substeps Maven Runner is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU Lesser General Public License as published by
 *    the Free Software Foundation, either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    Substeps Maven Runner is distributed in the hope that it will be useful,
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

import com.technophobia.substeps.execution.ExecutionNode;
import com.technophobia.substeps.runner.BuildFailureManager;
import com.technophobia.substeps.runner.ExecutionConfig;
import com.technophobia.substeps.runner.SubstepsRunnerMojo;

/**
 * 
 * 
 * 
 * @author imoore
 * 
 */
public class SubstepsRunnerMojoTest {

	//@Test
	public void testNonCriticalFailures() throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException{
	
		
		
        final SubstepsRunnerMojo mojo = new SubstepsRunnerMojo();

        final Method executeInternalMethod = mojo.getClass().getMethod("executeInternal",
        		BuildFailureManager.class, List.class);
        
        Assert.assertNotNull(executeInternalMethod);
        executeInternalMethod.setAccessible(true);

        final BuildFailureManager bfm = null;
        final List<ExecutionConfig> cfgList = null;
        
        executeInternalMethod.invoke(bfm, cfgList);

	}
	
    @Ignore("incomplete test")
    @Test
    public void testCriticalNonCriticalFailures() throws SecurityException, NoSuchMethodException,
            IllegalArgumentException, IllegalAccessException, InvocationTargetException,
            NoSuchFieldException {

        final SubstepsRunnerMojo mojo = new SubstepsRunnerMojo();

        final Method method = mojo.getClass().getMethod("checkRootNodeForFailure",
                ExecutionNode.class, ExecutionConfig.class);

        final Field failedNodesField = mojo.getClass().getField("failedNodes");
        final Field nonFatalFailedNodesField = mojo.getClass().getField("nonFatalFailedNodes");

        failedNodesField.setAccessible(true);
        nonFatalFailedNodesField.setAccessible(true);

        Assert.assertNotNull(method);
        method.setAccessible(true);

        final ExecutionConfig execConfig = new ExecutionConfig();

        final ExecutionNode rootNode = new ExecutionNode();

        method.invoke(mojo, rootNode, execConfig);

        final List<ExecutionNode> failedNodes = (List<ExecutionNode>) failedNodesField.get(mojo);
        final List<ExecutionNode> nonFatalFailedNodes = (List<ExecutionNode>) nonFatalFailedNodesField
                .get(mojo);

    }

}
