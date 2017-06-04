package org.substeps.runner

import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

import com.technophobia.substeps.execution.ImplementationCache
import com.technophobia.substeps.model.SubSteps.StepImplementations
import com.technophobia.substeps.model._
import com.technophobia.substeps.runner._
import com.technophobia.substeps.runner.builder.ExecutionNodeTreeBuilder
import com.technophobia.substeps.runner.setupteardown.SetupAndTearDown
import com.technophobia.substeps.runner.syntax.SyntaxBuilder
import com.typesafe.config.Config
import org.scalatest.{FlatSpec, ShouldMatchers}
import org.slf4j.LoggerFactory
import org.substeps.config.SubstepsConfigLoader
import org.substeps.report.ExecutionResultsCollector

import scala.collection.JavaConverters._

trait WritesResultsData{

  def getBaseDir(rootDir : File, prefix : String = "substeps-results_") = {
    new File( rootDir, prefix + LocalDateTime.now().format(DateTimeFormatter.ofPattern("YYYYMMdd_HHmm_ss_SSS")))
  }
}

/**
  * Created by ian on 13/01/17.
  */
class ExpressionEvaluationTest extends FlatSpec with ShouldMatchers with FeatureFilesFromSource with WritesResultsData{

  private val log = LoggerFactory.getLogger(classOf[ParsingFromSourceTests])


  "step execution" must "evaluate expressions" in {

    val simpleFeature =
      """
        | Feature: a simple feature
        | Scenario: config and runtime expression scenario
        |   A step with a value from config "${users.default.name}" and one "hardcoded1"
        |   SetupContext
        |   A step with a "hardcoded2" param and another from context "${key.other.name}"
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

      var configArg : String = ""
      var hardcodeArg1 : String = ""
      var hardcodeArg2 : String = ""
      var contextArg1 : String = ""


      @SubSteps.Step("""A step with a value from config "([^"]*)" and one "([^"]*)"""")
      def stepPassedFromConfig(arg : String, arg2: String) = {
        configArg = arg
        hardcodeArg1 = arg2
        log.debug("stepPassedFromConfig: " + arg)
      }

      @SubSteps.Step("SetupContext")
      def setupContext() = {
        ExecutionContext.put(Scope.SCENARIO, "key", new Sample("scenario", new Other("fromContext")))

        log.debug("SetupContext")
      }

      @SubSteps.Step("""A step with a "([^"]*)" param and another from context "([^"]*)"""")
      def stepPassedFromContext(arg : String, arg2: String) ={
        hardcodeArg2 = arg
        contextArg1 = arg2
        log.debug("stepPassedFromContext: " + arg2)
      }
    }

    val featureFile = createFeatureFile(simpleFeature, "expression-evaluation.feature")

    val stepImplementationClasses : List[java.lang.Class[_]] = List(classOf[StepImpls])

    val executionConfig = new SubstepsExecutionConfig

    executionConfig.setStepImplementationClasses(stepImplementationClasses.asJava)

    val syntax: Syntax = SyntaxBuilder.buildSyntax(stepImplementationClasses.asJava, new PatternMap[ParentStep]())

    val parameters: TestParameters = new TestParameters(new TagManager(""), syntax, List(featureFile).asJava)

//    val cfgWrapper = new ExecutionConfigWrapper(executionConfig)

//    val cfg = NewSubstepsExecutionConfig.toConfig(executionConfig)

    val cfg = SubstepsConfigLoader.splitMasterConfig(SubstepsConfigLoader.loadResolvedConfig).get(0)
    NewSubstepsExecutionConfig.setThreadLocalConfig(cfg)


    val nodeTreeBuilder: ExecutionNodeTreeBuilder = new ExecutionNodeTreeBuilder(parameters, cfg)

    // building the tree can throw critical failures if exceptions are found
    val rootNode = nodeTreeBuilder.buildExecutionNodeTree("test description")

    log.debug("rootNode 1:\n" + rootNode.toDebugString)

    val executionCollector = new ExecutionResultsCollector
    val dataDir = getBaseDir(new File("target"))
    executionCollector.setDataDir(new File(dataDir, "1"))
    executionCollector.setPretty(true)

    executionConfig.setDataOutputDirectory(dataDir)

    val runner = new ExecutionNodeRunner()


    runner.addNotifier(executionCollector)

    val methodExecutorToUse = new ImplementationCache()

    val stepImpls : StepImpls = methodExecutorToUse.getImplementation(classOf[StepImpls])


    val setupAndTearDown: SetupAndTearDown = new SetupAndTearDown(executionConfig.getInitialisationClasses, methodExecutorToUse)



    val rootNode2 = runner.prepareExecutionConfig(cfg, syntax, parameters, setupAndTearDown, methodExecutorToUse, null)

    executionCollector.initOutputDirectories(rootNode2)

    log.debug("rootNode 2:\n" + rootNode2.toDebugString)

    val finalRootNode = runner.run()

    log.debug("finalRootNode:\n" + finalRootNode.toDebugString)

    stepImpls.contextArg1 should be ("fromContext")

    stepImpls.hardcodeArg1 should be ("hardcoded1")

    stepImpls.hardcodeArg2 should be ("hardcoded2")

    stepImpls.configArg should be ("bob")

  }


}
