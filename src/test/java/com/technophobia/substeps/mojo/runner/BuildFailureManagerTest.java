package com.technophobia.substeps.mojo.runner;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import org.apache.maven.plugin.MojoFailureException;
import org.junit.Assert;
import org.junit.Test;

import com.technophobia.substeps.execution.ExecutionNode;
import com.technophobia.substeps.execution.ExecutionResult;
import com.technophobia.substeps.execution.Feature;
import com.technophobia.substeps.runner.BuildFailureManager;


/**
 * @author ian
 *
 */
public class BuildFailureManagerTest
{
    public void nonFailingMethod() {
        System.out.println("no fail");
    }


    public void failingMethod() {
        System.out.println("uh oh");
        throw new IllegalStateException("that's it, had enough");
    }

	
	private ExecutionNode getData(){
		
	       Method nonFailMethod = null;
	        Method failMethod = null;
	        try {
	            nonFailMethod = this.getClass().getMethod("nonFailingMethod");
	            failMethod = this.getClass().getMethod("failingMethod");
	        } catch (final Exception e) {
	            Assert.fail(e.getMessage());
	        }
	        Assert.assertNotNull(nonFailMethod);
	        Assert.assertNotNull(failMethod);
		
		final ExecutionNode rootNode = new ExecutionNode();
		
		final Throwable rootFail = new Exception("t1");
		rootNode.getResult().setFailed(rootFail);
		
        // add a feature
        final ExecutionNode featureNode = new ExecutionNode();
        final Feature feature = new Feature("test feature", "file");
        featureNode.getResult().setFailed(rootFail);
        
        featureNode.setFeature(feature);
        rootNode.addChild(featureNode);
        final Set<String> tags = new HashSet<String>();
        tags.add("@can_fail");
        
        featureNode.setTags(tags);
        
        
        final ExecutionNode scenarioNode = new ExecutionNode();
        scenarioNode.setScenarioName("scenarioName");
        scenarioNode.getResult().setFailed(rootFail);
        
        featureNode.addChild(scenarioNode);
        scenarioNode.setOutline(false);
        scenarioNode.setTags(tags);
//        final ExecutionNode scenarioOutlineNode = new ExecutionNode();
//        scenarioNode.addChild(scenarioOutlineNode);
//        scenarioOutlineNode.setRowNumber(1);
//
//        final ExecutionNode scenarioOutlineNode2 = new ExecutionNode();
//        scenarioNode.addChild(scenarioOutlineNode2);
//        scenarioOutlineNode2.setRowNumber(2);
        
        final ExecutionNode stepNode1 = new ExecutionNode();
        stepNode1.getResult().setFailed(rootFail);
        
//        scenarioOutlineNode.addChild(stepNode1);
        scenarioNode.addChild(stepNode1);

        stepNode1.setTargetClass(this.getClass());
        stepNode1.setTargetMethod(nonFailMethod);
        stepNode1.setLine("stepNode1");
        
        final ExecutionNode stepNode2 = new ExecutionNode();
//        scenarioOutlineNode.addChild(stepNode2);
        scenarioNode.addChild(stepNode2);

        stepNode2.setTargetClass(this.getClass());
        stepNode2.setTargetMethod(failMethod);
        stepNode2.setLine("stepNode2");
        stepNode2.getResult().setResult(ExecutionResult.NOT_RUN);
        
        final ExecutionNode stepNode3 = new ExecutionNode();
        scenarioNode.addChild(stepNode3);
//        scenarioOutlineNode.addChild(stepNode3);

        stepNode3.setTargetClass(this.getClass());
        stepNode3.setTargetMethod(nonFailMethod);
        stepNode3.setLine("stepNode3");
        stepNode3.getResult().setResult(ExecutionResult.NOT_RUN);
        
//        final ExecutionNode stepNode1b = new ExecutionNode();
//        scenarioOutlineNode2.addChild(stepNode1b);
//        stepNode1b.setTargetClass(this.getClass());
//        stepNode1b.setTargetMethod(nonFailMethod);
//        stepNode1b.setLine("stepNode1b");
//        
//        final ExecutionNode stepNode2b = new ExecutionNode();
//        scenarioOutlineNode2.addChild(stepNode2b);
//        stepNode2b.setTargetClass(this.getClass());
//        stepNode2b.setTargetMethod(nonFailMethod);
//        stepNode2b.setLine("stepNode2b");
//        
//        final ExecutionNode stepNode3b = new ExecutionNode();
//        scenarioOutlineNode2.addChild(stepNode3b);
//        stepNode3b.setTargetClass(this.getClass());
//        stepNode3b.setTargetMethod(nonFailMethod);
//        stepNode3b.setLine("stepNode3b");
        
        return rootNode;
	}
	
	@Test
	public void testNonCriticalFailures(){
		
		final BuildFailureManager bfm = new BuildFailureManager();
		
		final ExecutionNode rootNode = getData();
		// set up the scenario
		
		final String nonFatalTags = "@can_fail";
		
		bfm.checkRootNodeForFailure(rootNode, nonFatalTags);
		
		try
		{
			bfm.determineBuildFailure();
		}
		catch (final MojoFailureException e)
		{

			e.printStackTrace();
		}
	}
}
