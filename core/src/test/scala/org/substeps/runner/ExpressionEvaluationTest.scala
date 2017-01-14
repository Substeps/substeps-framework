package org.substeps.runner

import java.io.File

import com.technophobia.substeps.execution.ImplementationCache
import com.technophobia.substeps.model.SubSteps.StepImplementations
import com.technophobia.substeps.model._
import com.technophobia.substeps.runner._
import com.technophobia.substeps.runner.builder.ExecutionNodeTreeBuilder
import com.technophobia.substeps.runner.setupteardown.SetupAndTearDown
import com.technophobia.substeps.runner.syntax.SyntaxBuilder
import org.scalatest.{FlatSpec, ShouldMatchers}
import org.slf4j.LoggerFactory
import org.substeps.report.ExecutionResultsCollector

import scala.collection.JavaConverters._


/**
  * Created by ian on 13/01/17.
  */
class ExpressionEvaluationTest extends FlatSpec with ShouldMatchers with FeatureFilesFromSource{

  private val log = LoggerFactory.getLogger(classOf[ParsingFromSourceTests])


  "step execution" must "evaluate expressions" in {

    val simpleFeature =
      """
        | Feature: a simple feature
        | Scenario: config and runtime expression scenario
        |   A step with a value from config "${users.default.name}" and one "hardcoded"
        |   SetupContext
        |   A step with a "hardcoded" param and another from context "${key.other.name}"
        |
      """.stripMargin


    class Sample(name : String, other : Other){
      def getName() = name
      def getOther() = other
    }

    class Other(name : String){
      def getName() = name
    }

    @StepImplementations
    class StepImpls (){

      @SubSteps.Step("""A step with a value from config "([^"]*)" and one "([^"]*)"""")
      def stepPassedFromConfig(arg : String, arg2: String) = log.debug("stepPassedFromConfig: " + arg)

      @SubSteps.Step("SetupContext")
      def setupContext() = {
        ExecutionContext.put(Scope.SCENARIO, "key", new Sample("scenario", new Other("fromContext")))

        log.debug("SetupContext")
      }

      @SubSteps.Step("""A step with a "([^"]*)" param and another from context "([^"]*)"""")
      def stepPassedFromContext(arg : String, arg2: String) = log.debug("stepPassedFromContext: " + arg2)
    }

    val featureFile = createFeatureFile(simpleFeature, "expression-evaluation.feature")

    val stepImplementationClasses : List[java.lang.Class[_]] = List(classOf[StepImpls])

    val executionConfig = new SubstepsExecutionConfig

    executionConfig.setStepImplementationClasses(stepImplementationClasses.asJava)

    val syntax: Syntax = SyntaxBuilder.buildSyntax(stepImplementationClasses.asJava, new PatternMap[ParentStep]())

    val parameters: TestParameters = new TestParameters(new TagManager(""), syntax, List(featureFile).asJava)

    val cfgWrapper = new ExecutionConfigWrapper(executionConfig)
    val nodeTreeBuilder: ExecutionNodeTreeBuilder = new ExecutionNodeTreeBuilder(parameters, cfgWrapper)

    // building the tree can throw critical failures if exceptions are found
    val rootNode = nodeTreeBuilder.buildExecutionNodeTree("test description")

    log.debug("rootNode 1:\n" + rootNode.toDebugString)

    val executionCollector = new ExecutionResultsCollector
    val dataDir = ExecutionResultsCollector.getBaseDir(new File("target"))
    executionCollector.setDataDir(dataDir)
    executionCollector.setPretty(true)

    executionConfig.setDataOutputDirectory(dataDir)

    val runner = new ExecutionNodeRunner()


    runner.addNotifier(executionCollector)

    val methodExecutorToUse = new ImplementationCache()

    val setupAndTearDown: SetupAndTearDown = new SetupAndTearDown(executionConfig.getInitialisationClasses, methodExecutorToUse)


    val rootNode2 = runner.prepareExecutionConfig(new ExecutionConfigWrapper(executionConfig), syntax, parameters, setupAndTearDown, methodExecutorToUse, null)

    executionCollector.initOutputDirectories(rootNode2)

    log.debug("rootNode 2:\n" + rootNode2.toDebugString)

    val finalRootNode = runner.run()

    log.debug("finalRootNode:\n" + finalRootNode.toDebugString)

    //    // what are we expecting now:
    //    //    val rootDir = executionCollector.getRootReportsDir
    //    dataDir.exists() should be (true)
    //
    //    val featureDirs = dataDir.listFiles().toList.filter(f => f.isDirectory)
    //
    //    featureDirs.size should be (2)
    //
    //    for (fDir <- featureDirs){
    //
    //      if (fDir.getName.contains("simple")){
    //
    //        validateSimpleFeatureResults(fDir)
    //
    //      } else if (fDir.getName.contains("outline")){
    //
    //
    //        val scenarioResults = fDir.listFiles()
    //
    //        scenarioResults.length should be (5)
    //
    //      } else {
    //        Assert.fail("unexpected sub dir")
    //      }
    //
    //    }
    //
    //    val resultSummaryFile = dataDir.listFiles().toList.filter(f => f.isFile)
    //
    //    resultSummaryFile.size should be (1)


  }


}
