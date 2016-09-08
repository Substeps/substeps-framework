package org.substeps.report

import java.io.File
import java.lang.reflect.Method

import com.google.common.collect.ImmutableSet
import com.technophobia.substeps.execution.Feature
import com.technophobia.substeps.execution.node.{RootNode, TestBasicScenarioNodeBuilder, _}
import com.technophobia.substeps.model.{FeatureFile, Scenario, Step}
import com.technophobia.substeps.parser.FileContents
import com.technophobia.substeps.runner.FeatureFileParser
import org.hamcrest.Matchers._
import org.junit.Assert
import org.scalatest.{FlatSpec, FunSpec, ShouldMatchers}

import scala.collection.JavaConversions._
import scala.collection.JavaConverters._
/**
  * Created by ian on 19/05/16.
  */
class ExecutionResultsCollectorTest extends FlatSpec with ShouldMatchers{

  // TODO build up a full Execution hierarchy

  lazy val singleFeatureSingleScenario = {

    val nonFailMethod: Method = getNonFailMethod
    val failMethod: Method = getFailMethod
    Assert.assertNotNull(nonFailMethod)
    Assert.assertNotNull(failMethod)

    val scenarioName: String = "scenarioName"
    val rootNodeBuilder: TestRootNodeBuilder = new TestRootNodeBuilder
    val featureBuilder: TestFeatureNodeBuilder = rootNodeBuilder.addFeature(new Feature("test feature", "file"))

    // TODO - 2 simple scenarios here, one pass, one fail

    val scenario1Builder = featureBuilder.addBasicScenario("basic passing scenario 1")

    scenario1Builder.addTags(ImmutableSet.of("toRun", "canFail"))
    scenario1Builder.addStepImpl(getClass, nonFailMethod).addStepImpl(getClass, failMethod).addStepImpl(getClass, nonFailMethod)

    val substepBuilder = scenario1Builder.addSubstep()


    val scenario2Builder: TestBasicScenarioNodeBuilder = featureBuilder.addBasicScenario("basic failing scenario 2")
    scenario2Builder.addStepImpl(getClass, nonFailMethod).addStepImpl(getClass, nonFailMethod).addStepImpl(getClass, nonFailMethod)
    scenario2Builder.addTags(ImmutableSet.of("toRun", "canFail"))

    //    val outlineScenarioBuilder: TestOutlineScenarioNodeBuilder = featureBuilder.addOutlineScenario(scenarioName)
//    val rowBuilder1: TestOutlineScenarioRowNodeBuilder = outlineScenarioBuilder.addRow(1)
//    val rowBuilder2: TestOutlineScenarioRowNodeBuilder = outlineScenarioBuilder.addRow(2)
//
//    val row1ScenarioBuilder: TestBasicScenarioNodeBuilder = rowBuilder1.setBasicScenario(scenarioName)
//    row1ScenarioBuilder.addStepImpl(getClass, nonFailMethod).addStepImpl(getClass, failMethod).addStepImpl(getClass, nonFailMethod)
//    val row2ScenarioBuilder: TestBasicScenarioNodeBuilder = rowBuilder2.setBasicScenario(scenarioName)
//    row2ScenarioBuilder.addStepImpl(getClass, nonFailMethod).addStepImpls(3, getClass, failMethod)

    rootNodeBuilder.build
  }

  
  

  
  
  


  def nonFailingMethod {
    System.out.println("no fail")
  }

  def failingMethod {
    System.out.println("uh oh")
    throw new IllegalStateException("that's it, had enough")
  }

  private def getNonFailMethod: Method = {
    return getMethodOrFail("nonFailingMethod")
  }

  private def getFailMethod: Method = {
    return getMethodOrFail("failingMethod")
  }

  private def getMethodOrFail(method: String): Method = {
    try {
      return this.getClass.getMethod(method)
    }
    catch {
      case e: Exception => {
        Assert.fail(e.getMessage)
        return null
      }
    }
  }
}
