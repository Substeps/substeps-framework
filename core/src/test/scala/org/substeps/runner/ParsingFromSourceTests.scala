package org.substeps.runner

import java.io.File
import java.nio.charset.Charset
import java.nio.file.{Path, Paths}
import java.util

import com.google.common.io.{Files, MoreFiles}
import com.technophobia.substeps.execution.node.{ExecutionNode, IExecutionNode}
import com.technophobia.substeps.execution.{ExecutionResult, ImplementationCache}
import com.technophobia.substeps.model.SubSteps.StepImplementations
import com.technophobia.substeps.model._
import com.technophobia.substeps.parser.FileContents
import com.technophobia.substeps.runner._
import com.technophobia.substeps.runner.builder.ExecutionNodeTreeBuilder
import com.technophobia.substeps.runner.setupteardown.SetupAndTearDown
import com.technophobia.substeps.runner.syntax._
import com.typesafe.config._
import org.hamcrest.Matchers._
import org.json4s.NoTypeHints
import org.json4s.native.Serialization
import org.json4s.native.Serialization.read
import org.junit.Assert
import org.scalatest._
import org.slf4j.LoggerFactory
import org.substeps.config.SubstepsConfigLoader
import org.substeps.report.{ExecutionResultsCollector, NodeDetail}

import scala.collection.JavaConverters._

trait FeatureFilesFromSource {

  def createFeatureFile(content: String, featureFileName: String): FeatureFile = {
    val featureFileContentsFromSource = new FileContents(content.split("\n").toList.asJava, new File(featureFileName))

    val parser: FeatureFileParser = new FeatureFileParser

    val featureFile: FeatureFile = parser.getFeatureFile(featureFileContentsFromSource)
    featureFile
  }

}

/**
  * Created by ian on 20/05/16.
  */
class ParsingFromSourceTests extends FlatSpec with Matchers with FeatureFilesFromSource with WritesResultsData {

  val UTF8 = Charset.forName("UTF-8")

  private val log = LoggerFactory.getLogger(classOf[ParsingFromSourceTests])

  "feature file parsing" should "work from string source" in {

    val theFeature =
      """# Copyright Technophobia Ltd 2012
Feature: A feature for testing line numbers of scenarios and steps in the parser
	In order to develop a new cuke framework I need some tests

#   Scenario: A commented out scenario
#    Given I'm an un-registered user
#    And I am on the registration page

Background: background to the proceeding scenario
    Given whatever

Tags: @tag1 @tag2
  Scenario: Execute simple cuke annotated methods
		Given something with an@emailaddress.com
		Given series of substeps is executed 			# defined in substeps
		Given substep with param1 "wobble" and param2 "wibble"
		When an event occurs
		When an event occurs # ha work this line number out
		When an event occurs
		When an event occurs

Tags: @tag3
Scenario: A second scenario
		Then bad things happen
		And people get upset
		Whatever yee hah
		And a parameter fred is supplied


Scenario Outline: User submitting blank registration form
        When I register as a new user
      And I enter a <Object> of <Data>
          And I click Submit
        Then I am <Result>
            And I am not registered with the system
            And I am shown the registration page

  Examples:
        |Object|Data|Result|
        |Title||Please enter a title.|
        |First name||Please enter a valid first name.|
        |Last name||Please enter a valid last name.|
        |Email||Please enter a valid email address|
        |Professional title||Please enter your professional title|
        |Password||That password is too short (or too long). Please make sure your password is a minimum of 8 characters.|
        |Retype Password||no error message|
        |Password reminder||Your password reminder cannot be blank or contain your password |


Scenario: inline table
    And I click the 'Create user' navigation link
    Then I see the 'Create user' page with heading 'Create User step 1 of 5'
    And I type the following into the 'Enter new user details' fields
        |New user email address|New user forename |New user surname |New user telephone number|
        |table1.newEmail       |table1.newForename|table1.newSurname|table1.newTel|
    And I click the 'Next' button
    And whatever"""

    val fileContentsFromSource = new FileContents(theFeature.split("\n").toList.asJava, new File("temp_feature_file.feature"))

    val uri = this.getClass.getClassLoader.getResource("features/lineNumbers.feature")

    val fileContentsFromFile = FileContents.fromFile(new File(uri.getFile))

    fileContentsFromSource.getLines.size() should be (fileContentsFromFile.getLines.size())

    // need to compare line by line and trim each one

    val parser: FeatureFileParser = new FeatureFileParser

    val feature: FeatureFile = parser.getFeatureFile(fileContentsFromSource)

    //    parser.loadFeatureFile(new File(featureFile))

    Assert.assertNotNull(feature)

    val scenarios: java.util.List[Scenario] = feature.getScenarios

    Assert.assertThat(scenarios.size, is(4))

    val sc1: Scenario = scenarios.get(0)

    Assert.assertNotEquals(sc1.getSourceStartOffset, -1)

    // Assert.assertThat(sc1.getSourceEndOffset(), is(not(-1)));
    Assert.assertThat(sc1.getSourceStartLineNumber, is(13))

    val steps: java.util.List[Step] = sc1.getSteps
    Assert.assertThat(steps.size, is(7))

    val s1: Step = steps.get(0)
    Assert.assertThat(s1.getSourceLineNumber, is(14))
    val s2: Step = steps.get(1)
    Assert.assertThat(s2.getSourceLineNumber, is(15))

    val s3: Step = steps.get(2)
    Assert.assertThat(s3.getSourceLineNumber, is(16))

    val s4: Step = steps.get(3)
    Assert.assertThat(s4.getSourceLineNumber, is(17))

    val s5: Step = steps.get(4)
    Assert.assertThat(s5.getSourceLineNumber, is(18))

    val s6: Step = steps.get(5)
    Assert.assertThat(s6.getSourceLineNumber, is(19))

    val s7: Step = steps.get(6)
    Assert.assertThat(s7.getSourceLineNumber, is(20))

    val sc2: Scenario = scenarios.get(1)

    Assert.assertThat(sc2.getSourceStartLineNumber, is(23))

    Assert.assertThat(sc2.getSteps.get(0).getSourceLineNumber, is(24))

    val sc3: Scenario = scenarios.get(2)
    Assert.assertThat(sc3.getSourceStartLineNumber, is(30))

    val sc4: Scenario = scenarios.get(3)

    Assert.assertThat(sc4.getSteps.get(3).getSourceLineNumber, is(56))
  }


  "substep definition parsing" should "work from source" in {

    val substepDefFile =
"""
  |# Copyright Technophobia Ltd 2012
  |# a definition of a substep
  |Define: Given series of substeps is executed
  |		When an event occurs
  |		Then bad things happen
  |		And people get upset
  |
  |Define: Given substep with param1 <name1> and param2 <name2>
  |		Then method with param <name1>
  |		Then another method with param <name2>
  |
  |Define: Given a step with a parameter <parameter>
  |		Then the substep gets the whole <parameter>
  |
  |Define: Given a substep
  |    Given something
  |
  |Define:  Given another substep
  |  Given something
  |
  |Define:  Given yet another substep
  |  Given something
  |
  |Define: Then a method with a quoted '<param>'
  |	Then method with param <param>
  |
  |Define: A step def that isn't actually called
  |  Given something
  |
""".stripMargin

      val subStepParser: SubStepDefinitionParser = new SubStepDefinitionParser(true, new DefaultSyntaxErrorReporter)

      val fileContentsFromSource = new FileContents(substepDefFile.split("\n").toList.asJava, new File("temp_substep_def.substeps"))

      val parentMap = subStepParser.parseSubstepFileContents(fileContentsFromSource)

      parentMap.keySet().size() should be (8)
    // compare with "./target/test-classes/substeps/simple.substeps"

    // TODO more assertions here
  }


  "running some failing features marked as non critical" must "not fail the build and be visible" in {

    // TODO - complete this test

    val simpleFeature =
      """
        | Tags: all
        | Feature: a simple feature
        |
        | Scenario: A basic failing scenario
        |   PassingSubstepDef
        |   FailingSubstepDef
        |   NotRun
      """.stripMargin

    val substepDef =
      """
        |Define: PassingSubstepDef
        |  AnotherPassingStepImpl
        |
        |Define: FailingSubstepDef
        | GenerateFailure
        |
      """.stripMargin

    @StepImplementations
    class StepImpls () extends ProvidesScreenshot {

      @SubSteps.Step("AnotherPassingStepImpl")
      def anotherPassingStepImpl() = log.debug("pass")

      @SubSteps.Step("NotRun")
      def notRun() = log.debug("not run")

      @SubSteps.Step("GenerateFailure")
      def generateFailure() = throw new IllegalStateException("something went wrong")

      override def getScreenshotBytes: Array[Byte] = "fake screenshot bytes".getBytes
    }


    val subStepParser: SubStepDefinitionParser = new SubStepDefinitionParser(true, new DefaultSyntaxErrorReporter)

    val substepDeffileContentsFromSource = new FileContents(substepDef.split("\n").toList.asJava, new File("temp_substep_def.substeps"))

    val parentMap = subStepParser.parseSubstepFileContents(substepDeffileContentsFromSource)

    val featureFile = createFeatureFile(simpleFeature, "simple_feature_file.feature")

    val stepImplementationClasses : List[java.lang.Class[_]] = List(classOf[StepImpls])

    val executionConfig = new SubstepsExecutionConfig

    executionConfig.setStepImplementationClasses(stepImplementationClasses.asJava)

    executionConfig.setTags("all")
    executionConfig.setNonFatalTags("all")

    val syntax: Syntax = SyntaxBuilder.buildSyntax(stepImplementationClasses.asJava, parentMap)

    val tagManager = new TagManager(executionConfig.getTags)

    val nonFatalTAgManager = TagManager.fromTags(executionConfig.getNonFatalTags)

    val parameters: TestParameters = new TestParameters(tagManager, syntax, List(featureFile).asJava)

//    val cfgWrapper = new ExecutionConfigWrapper(executionConfig)

    val cfg: Config = NewSubstepsExecutionConfig.toConfig(executionConfig)


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

    val setupAndTearDown: SetupAndTearDown = new SetupAndTearDown(executionConfig.getInitialisationClasses, methodExecutorToUse)

//    val cfg = NewSubstepsExecutionConfig.toConfig(executionConfig)


    val rootNode2 = runner.prepareExecutionConfig(cfg, syntax, parameters, setupAndTearDown, methodExecutorToUse, nonFatalTAgManager)

    executionCollector.initOutputDirectories(rootNode2)

    log.debug("rootNode 2:\n" + rootNode2.toDebugString)

    val finalRootNode = runner.run()

    log.debug("finalRootNode :\n" + rootNode2.toDebugString)

    finalRootNode.getResult.getResult should be (ExecutionResult.NON_CRITICAL_FAILURE)
  }


  // from ExecutionNodeRunnerTest.testValidErrorPlusNonCriticalFailures
  "mixed critical and non critical failures" must "be reported as such" in {

    val f1 = ("critical-failure.feature",
      """
        | Tags: toRun
        | Feature: crit fail feature
        | Scenario: scenario crit fail
        |   nonFail
        |   Fail
        |   nonFail
        |
      """.stripMargin)


    val f2 = ("non-critical-failure.feature",
      """
        | Tags: canFail
        | Feature: non crit fail feature
        |
        | Tags: toRun canFail
        | Scenario: scenario non crit fail
        |   nonFail
        |   Fail
        |   nonFail
        |
      """.stripMargin)

    val f3 = ("pass.feature",
      """
        | Feature: pass feature
        | Tags: toRun
        | Scenario: scenario pass
        |   nonFail
        |   nonFail
        |   nonFail
      """.stripMargin)


    //noinspection TypeAnnotation
    @StepImplementations
    class StepImpls () extends ProvidesScreenshot {

      @SubSteps.Step("nonFail")
      def nonFailingMethod() =  System.out.println("no fail")

      @SubSteps.Step("Fail")
      def failingMethod() = throw new IllegalStateException("that's it, had enough")

      override def getScreenshotBytes: Array[Byte] = "fake screenshot bytes".getBytes
    }

    val features = List(f1,f2,f3)

    val (finalRootNode, failures) = runFeatures("", features, List(classOf[StepImpls]), Some("toRun"), Some("canFail"))

    println("finalRootNode:\n" + finalRootNode.toDebugString)

    // some assertions
    val bfm = new BuildFailureManager();




    val criticalFailuresField = classOf[BuildFailureManager].getDeclaredField("criticalFailures")
    criticalFailuresField.setAccessible(true)
    val criticalFailures =  criticalFailuresField.get(bfm).asInstanceOf[java.util.List[java.util.List[IExecutionNode]]].asScala  // (List<List<IExecutionNode>>)

    val nonCriticalFailuresField = classOf[BuildFailureManager].getDeclaredField("nonCriticalFailures")
    nonCriticalFailuresField.setAccessible(true)
    val nonCriticalFailures =  nonCriticalFailuresField.get(bfm).asInstanceOf[java.util.List[java.util.List[IExecutionNode]]].asScala // (List<List<IExecutionNode>>)


    bfm.addExecutionResult(finalRootNode)

    criticalFailures should have size(1)
    nonCriticalFailures should have size(1)


    finalRootNode.getResult().getResult() should be (ExecutionResult.FAILED)

    val featureList = finalRootNode.getChildren.asScala

    featureList(0).getResult.getResult should be (ExecutionResult.CHILD_FAILED)
    featureList(1).getResult.getResult should be (ExecutionResult.CHILD_FAILED)
    featureList(2).getResult.getResult should be (ExecutionResult.PASSED)

    val sc1 = featureList(0).getChildren.asScala(0)
    val sc2 = featureList(1).getChildren.asScala(0)
    val sc3 = featureList(2).getChildren.asScala(0)

    sc1.getResult.getResult should be (ExecutionResult.CHILD_FAILED)
    sc2.getResult.getResult should be (ExecutionResult.CHILD_FAILED)
    sc3.getResult.getResult should be (ExecutionResult.PASSED)

    val scenario1Results =
      sc1.getChildren.asScala.toList.map(st => (st.asInstanceOf[ExecutionNode]).getResult.getResult)

    import ExecutionResult._

    scenario1Results should be (List(PASSED, FAILED, NOT_RUN))

    val scenario2Results =
      sc2.getChildren.asScala.toList.map(st => (st.asInstanceOf[ExecutionNode]).getResult.getResult)

    scenario2Results should be (List(PASSED, NON_CRITICAL_FAILURE, NOT_RUN))


    failures should have size(2)

  }


  def runFeatures(suiteDescription : String, features : List[(String, String)],
                  stepImplementationClasses : List[java.lang.Class[_]],
                  tags : Option[String],
                  nonFatalTags : Option[String]
                 ) = {

//    val subStepParser: SubStepDefinitionParser = new SubStepDefinitionParser(true, new DefaultSyntaxErrorReporter)
//
//    val substepDeffileContentsFromSource = new FileContents(substepDef.split("\n").toList.asJava, new File("temp_substep_def.substeps"))
//
    val parentMap = new PatternMap[ParentStep]

    val featureFileList =
      features.map(f => {
        createFeatureFile(f._2, f._1)
      })


    //val stepImplementationClasses : List[java.lang.Class[_]] = List(classOf[StepImpls])

    val executionConfig = new SubstepsExecutionConfig

    executionConfig.setStepImplementationClasses(stepImplementationClasses.asJava)

    val syntax: Syntax = SyntaxBuilder.buildSyntax(stepImplementationClasses.asJava, parentMap)



    val parameters: TestParameters = new TestParameters(new TagManager(tags.getOrElse(null)), syntax, featureFileList.asJava)

//    val cfgWrapper = new ExecutionConfigWrapper(executionConfig)

    val cfg = NewSubstepsExecutionConfig.toConfig(executionConfig)


    val nodeTreeBuilder: ExecutionNodeTreeBuilder = new ExecutionNodeTreeBuilder(parameters, cfg)

    // building the tree can throw critical failures if exceptions are found
    val rootNode = nodeTreeBuilder.buildExecutionNodeTree(suiteDescription)

    log.debug("node tree builder rootNode 1:\n" + rootNode.toDebugString)

    val executionCollector = new ExecutionResultsCollector
    val dataDir = getBaseDir(new File("target"))
    executionCollector.setDataDir(new File(dataDir, "1"))
    executionCollector.setPretty(true)

    executionConfig.setDataOutputDirectory(dataDir)

    val runner = new ExecutionNodeRunner()


    runner.addNotifier(executionCollector)

    val methodExecutorToUse = new ImplementationCache()

    val setupAndTearDown: SetupAndTearDown = new SetupAndTearDown(executionConfig.getInitialisationClasses, methodExecutorToUse)

//    val cfg = NewSubstepsExecutionConfig.toConfig(executionConfig)


    val rootNode2 = runner.prepareExecutionConfig(cfg, syntax, parameters, setupAndTearDown, methodExecutorToUse, TagManager.fromTags(nonFatalTags.getOrElse(null)))

    executionCollector.initOutputDirectories(rootNode2)

    log.debug("prepared rootNode 2:\n" + rootNode2.toDebugString)

    val finalRootNode = runner.run()

    (finalRootNode, runner.getFailures.asScala)


  }


  /**
    * NB. this is the test that generates the source data, then used to test out report building
    */

  // TODO - missing the config to generate the unused and unimplemented - which then gets included in the report
  // No configuration setting found for key 'org.substeps.config.executionConfig.dataOutputDir = "src/test/resources/sample-results-data/1"

  // missing
  // org.substeps.config.description
  // org.substeps.config.executionConfigs[0].dataOutputDir



  "run some features to test the generation of raw report data" must "create the raw data files" in {

    val simpleFeature =
      """
        | Tags: feature-level-tag
        | Feature: a simple feature
        | Scenario: A basic passing scenario
        |   PassingStepImpl
        |   PassingSubstepDef
        |   WithParamsSubstepDef "a" and "b"
        |
        | Tags: scenario-level-tag
        | Scenario: A basic failing scenario
        |   PassingSubstepDef
        |   WithParamsSubstepDef "c" and "d"
        |   FailingSubstepDef
        |   NotRun
      """.stripMargin


    val outlineFeature =
      """
 Tags: feature-level-tag
 Feature: an outline feature

 Tags: scenario-level-tag
 Scenario Outline: a basic scenario outline <iteration>
   PassingStepImpl
   PassingSubstepDef
   PassingStepImpl with <param>

 Examples:
  |iteration  | param   |
  |one        | param 1 |
  |two        | param 2 |
  |three      | fail    |
      """


    val substepDef =
      """
        |Define: PassingSubstepDef
        |  AnotherPassingStepImpl
        |
        |Define: FailingSubstepDef
        | GenerateFailure
        |
        |Define: WithParamsSubstepDef "<one>" and "<two>"
        | WithParams "<one>" "<two>"
        |
      """.stripMargin

    //noinspection TypeAnnotation
    @StepImplementations
    class StepImpls () extends ProvidesScreenshot {

      @SubSteps.Step("PassingStepImpl")
      def passingStepImpl() = log.debug("pass")

      @SubSteps.Step("AnotherPassingStepImpl")
      def anotherPassingStepImpl() = log.debug("pass")

      @SubSteps.Step("NotRun")
      def notRun() = log.debug("not run")

      @SubSteps.Step("GenerateFailure")
      def generateFailure() = throw new IllegalStateException("something went wrong")

      @SubSteps.Step("PassingStepImpl with (.*)")
      def passingStepImplWithParam(arg : String) = {
        if (arg =="fail")   throw new IllegalStateException("something went wrong")
      }

      @SubSteps.Step("""WithParams "([^"]*)" "([^"]*)"""")
      def twoParams(arg1 : String, arg2 : String) = {
        log.debug(s"pass two params $arg1 $arg2")
      }

      override def getScreenshotBytes: Array[Byte] = "fake screenshot bytes".getBytes
    }


    val subStepParser: SubStepDefinitionParser = new SubStepDefinitionParser(true, new DefaultSyntaxErrorReporter)

    val substepDeffileContentsFromSource = new FileContents(substepDef.split("\n").toList.asJava, new File("temp_substep_def.substeps"))

    val parentMap = subStepParser.parseSubstepFileContents(substepDeffileContentsFromSource)


    val featureFile = createFeatureFile(simpleFeature, "simple_feature_file.feature")

    val featureFile2 = createFeatureFile(outlineFeature, "outline_feature_file.feature")


    val stepImplementationClasses : List[java.lang.Class[_]] = List(classOf[StepImpls])

    val executionConfig = new SubstepsExecutionConfig

    executionConfig.setStepImplementationClasses(stepImplementationClasses.asJava)

    val syntax: Syntax = SyntaxBuilder.buildSyntax(stepImplementationClasses.asJava, parentMap)

    val parameters: TestParameters = new TestParameters(new TagManager(""), syntax, List(featureFile, featureFile2).asJava)

    val rootDataDir = getBaseDir(new File("target"))

    println("foor the tests, setting root data dir to be " + rootDataDir.getAbsolutePath)

    val masterConfig = NewSubstepsExecutionConfig.toConfig(executionConfig).withValue("org.substeps.config.rootDataDir", ConfigValueFactory.fromAnyRef(rootDataDir.getPath))

    //val masterConfig = NewSubstepsExecutionConfig.loadMasterConfig(baseCfg, None)

    val configs = SubstepsConfigLoader.splitMasterConfig(masterConfig).asScala


    // write out the master config to the root data dir
    ExecutionResultsCollector.writeMasterConfig(masterConfig)

    val cfg = configs.head

    NewSubstepsExecutionConfig.setThreadLocalConfig(cfg)


    val nodeTreeBuilder: ExecutionNodeTreeBuilder = new ExecutionNodeTreeBuilder(parameters, cfg)

    // building the tree can throw critical failures if exceptions are found
    val rootNode = nodeTreeBuilder.buildExecutionNodeTree("test description")

    log.debug("rootNode 1:\n" + rootNode.toDebugString)

    val executionCollector = new ExecutionResultsCollector
    val configDataDir = new File(rootDataDir, "1")
    executionCollector.setDataDir(configDataDir)
    executionCollector.setPretty(true)

    executionConfig.setDataOutputDirectory(rootDataDir)

    val runner = new ExecutionNodeRunner()


    runner.addNotifier(executionCollector)

    val methodExecutorToUse = new ImplementationCache()

    val setupAndTearDown: SetupAndTearDown = new SetupAndTearDown(executionConfig.getInitialisationClasses, methodExecutorToUse)



    val rootNode2 = runner.prepareExecutionConfig(cfg, syntax, parameters, setupAndTearDown, methodExecutorToUse, null)

    executionCollector.initOutputDirectories(rootNode2)

    log.debug("rootNode 2:\n" + rootNode2.toDebugString)

    val finalRootNode = runner.run()

    // what are we expecting now:
    rootDataDir.exists() should be (true)

    val masterConfigOption = rootDataDir.listFiles().toList.find(f => f.getName == "masterConfig.conf")

    masterConfigOption shouldBe defined

    val featureDirs = configDataDir.listFiles().toList.filter(f => f.isDirectory)

    featureDirs.size should be (2)

    for (fDir <- featureDirs){

      if (fDir.getName.contains("simple")){

        validateSimpleFeatureResults(fDir)

      } else if (fDir.getName.contains("outline")){


        val scenarioResults = fDir.listFiles()

        scenarioResults.length should be (5)

      } else {
        Assert.fail("unexpected sub dir")
      }

    }

    val resultSummaryFile = configDataDir.listFiles().toList.filter(f => f.isFile)

    resultSummaryFile.size should be (1)

    // used to compare the output from this report builder and the new one
//    val reportBuilder = new DefaultExecutionReportBuilder
//    reportBuilder.setOutputDirectory(new File("feature-report"))
//    reportBuilder.addRootExecutionNode(finalRootNode)
//    reportBuilder.buildReport()
  }

  private def validateSimpleFeatureResults(featureDir: File) = {

    val featureResults = featureDir.listFiles().toList
    featureResults.length should be (4)

    val passingScenario = featureResults.find(s => s.getName.contains("passing_scenario"))

    passingScenario shouldBe defined

    implicit val formats = Serialization.formats(NoTypeHints)


    val passingScenarioContent = read[NodeDetail](Files.asCharSource( passingScenario.get, UTF8).read())

    passingScenarioContent.children.size should be (3)

    passingScenarioContent.children(0).children should be (empty)

    passingScenarioContent.children(1).children.size should be (1)

    // failing scenario

    // some extra debug in here to work out what's going on
    log.debug("got feature results files in dir: " + featureDir.getAbsolutePath + " files: " + featureResults.mkString(","))

    val failingScenario = featureResults.find(s => s.getName.contains("failing_scenario_1_results.json"))

    failingScenario shouldBe defined


    val failingScenarioContent = read[NodeDetail](Files.asCharSource( failingScenario.get, UTF8).read())

    failingScenarioContent.children.size should be (4)

    failingScenarioContent.children(0).children.size should be (1)
    failingScenarioContent.children(0).result should be ("PASSED")

    failingScenarioContent.children(2).children.size should be (1)
    failingScenarioContent.children(2).result should be ("CHILD_FAILED")

    // NB. no need for the parent to also have the error
    failingScenarioContent.children(2).exceptionMessage shouldBe empty
    failingScenarioContent.children(2).stackTrace shouldBe empty

    failingScenarioContent.children(2).children(0).result should be("FAILED")
    failingScenarioContent.children(2).children(0).exceptionMessage shouldBe defined
    failingScenarioContent.children(2).children(0).stackTrace shouldBe defined

    failingScenarioContent.children(3).children should be (empty)
    failingScenarioContent.children(3).result should be ("NOT_RUN")

    // TODO val failingScenario = scenarioResults.find(s => s.getName.contains("failing"))


    val featureSummary = featureResults.find(s => s.getName.contains("simple_feature_file.feature.results.json"))

    featureSummary shouldBe defined



  }


  "running the same features with multiple configurations" must "generate data in a way that the report builder can pick it up" in {

    val simpleFeature =
      """
        | Feature: a simple feature
        | Scenario: A basic passing scenario
        |   PassingStepImpl
        |   PassingSubstepDef
        |   WithParamsSubstepDef "a" and "b"
        |
        | Scenario: A basic failing scenario
        |   PassingSubstepDef
        |   WithParamsSubstepDef "c" and "d"
        |   FailingSubstepDef
        |   NotRun
      """.stripMargin

    val substepDef =
      """
        |Define: PassingSubstepDef
        |  AnotherPassingStepImpl
        |
        |Define: FailingSubstepDef
        | GenerateFailure
        |
        |Define: WithParamsSubstepDef "<one>" and "<two>"
        | WithParams "<one>" "<two>"
        |
      """.stripMargin

    //noinspection TypeAnnotation
    @StepImplementations
    class StepImpls  extends ProvidesScreenshot {

      @SubSteps.Step("PassingStepImpl")
      def passingStepImpl() = log.debug("pass")

      @SubSteps.Step("AnotherPassingStepImpl")
      def anotherPassingStepImpl() = log.debug("pass")

      @SubSteps.Step("NotRun")
      def notRun() = log.debug("not run")

      @SubSteps.Step("GenerateFailure")
      def generateFailure() = throw new IllegalStateException("something went wrong")

      @SubSteps.Step("PassingStepImpl with (.*)")
      def passingStepImplWithParam(arg : String) = {
        if (arg =="fail")   throw new IllegalStateException("something went wrong")
      }

      @SubSteps.Step("""WithParams "([^"]*)" "([^"]*)"""")
      def twoParams(arg1 : String, arg2 : String) = {
        log.debug(s"pass two params $arg1 $arg2")
      }

      override def getScreenshotBytes: Array[Byte] = "fake screenshot bytes".getBytes
    }


    val subStepParser: SubStepDefinitionParser = new SubStepDefinitionParser(true, new DefaultSyntaxErrorReporter)

    val substepDeffileContentsFromSource = new FileContents(substepDef.split("\n").toList.asJava, new File("temp_substep_def.substeps"))

    val parentMap = subStepParser.parseSubstepFileContents(substepDeffileContentsFromSource)


    val featureFile = createFeatureFile(simpleFeature, "simple_feature_file.feature")



    val stepImplementationClasses : List[java.lang.Class[_]] = List(classOf[StepImpls])

    val stepImplClassName = new StepImpls().getClass.getName

    // one data dir
    val dataDir = getBaseDir(new File("target"))
    val dataDirPath = dataDir.getAbsolutePath

    val dataOutoutDir1 = """${org.substeps.config.rootDataDir}"/1""""
    val dataOutoutDir2 = """${org.substeps.config.rootDataDir}"/2""""


    val cfgFileContents =
      s"""
        | org {
        | substeps {
        | baseExecutionConfig {
        |         featureFile="simple_feature_file.feature"
        |         stepImplementationClassNames=[ "${stepImplClassName}"]
        |         substepsFile="temp_substep_def.substeps"
        | }
        |     executionConfigs=[
        |         {
        |         dataOutputDir=1
        |         description="Parsing from source Test Features 1"
        |         },
        |         {
        |         dataOutputDir=2
        |         description="Parsing from source Test Features 2"
        |         }
        |        ]
        |  config {
        |     rootDataDir="${dataDirPath}"
        |     description="Parsing from source test suite"
        |     }
        |  }
        | }
      """.stripMargin

    println("CONFIG contents\n" + cfgFileContents)

    val baseCfg = ConfigFactory.parseString(cfgFileContents)

    println("BASE CFG: " + baseCfg.root().render())




    val masterConfig =
      baseCfg.withFallback(ConfigFactory.load(ConfigParseOptions.defaults(), ConfigResolveOptions.noSystem().setAllowUnresolved(true)))
      //NewSubstepsExecutionConfig.loadMasterConfig(baseCfg, None)
        .withValue("org.substeps.config.reportDir", ConfigValueFactory.fromAnyRef(getBaseDir(new File("target"), "substeps-report_").toString))

    //val masterConfig = NewSubstepsExecutionConfig.toConfig(executionConfig).withValue("org.substeps.config.rootDataDir", ConfigValueFactory.fromAnyRef(rootDataDir.getPath))

    val configs = SubstepsConfigLoader.splitMasterConfig(masterConfig).asScala


    //val configs = NewSubstepsExecutionConfig.splitConfig(masterConfig)

    val syntax: Syntax = SyntaxBuilder.buildSyntax(stepImplementationClasses.asJava, parentMap)

    val parameters: TestParameters = new TestParameters(new TagManager(""), syntax, List(featureFile).asJava)

    val rootDataDir: File = NewSubstepsExecutionConfig.getRootDataDir(masterConfig)

    // write out the master config to the root data dir
    ExecutionResultsCollector.writeMasterConfig(masterConfig)


    configs.foreach(cfg => {

      NewSubstepsExecutionConfig.setThreadLocalConfig(cfg)


      val nodeTreeBuilder: ExecutionNodeTreeBuilder = new ExecutionNodeTreeBuilder(parameters, cfg)
      // building the tree can throw critical failures if exceptions are found
      val rootNode = nodeTreeBuilder.buildExecutionNodeTree("test description")

      log.debug("rootNode 1:\n" + rootNode.toDebugString)

      val executionCollector = new ExecutionResultsCollector

      val dataDirForReportBuilder = NewSubstepsExecutionConfig.getDataOutputDirectory(cfg)

      executionCollector.setDataDir(dataDirForReportBuilder)
      executionCollector.setPretty(true)

      val runner = new ExecutionNodeRunner()

      runner.addNotifier(executionCollector)

      val methodExecutorToUse = new ImplementationCache()
      val stepImplementationClasses = NewSubstepsExecutionConfig.getStepImplementationClasses(cfg)
      val initialisationClasses = NewSubstepsExecutionConfig.getInitialisationClasses(cfg)

      val initClassList : java.util.List[Class[_]] =

         if (initialisationClasses != null) initialisationClasses.toList.asJava else null

      val finalInitClasses = ExecutionNodeRunner.buildInitialisationClassList(stepImplementationClasses, initClassList)
      val setupAndTearDown = new SetupAndTearDown(finalInitClasses, methodExecutorToUse)

      val rootNode2 = runner.prepareExecutionConfig(cfg, syntax, parameters, setupAndTearDown, methodExecutorToUse, null)

      executionCollector.initOutputDirectories(rootNode2)

//      log.debug("rootNode 2:\n" + rootNode2.toDebugString)

      val finalRootNode = runner.run()

      log.debug("finalRootNode:\n" + finalRootNode.toDebugString)



    })

    val localReportBuilder = NewSubstepsExecutionConfig.getReportBuilder(masterConfig)
    val reportDir = NewSubstepsExecutionConfig.getReportDir(masterConfig)



    localReportBuilder.buildFromDirectory(rootDataDir, reportDir, null)


    // TODO reportbuilder mods


    // change where the reportbuilder looks for it's data
    // look for subdirs ?  need a way to tie them together ?  additional results file ?

    // --> looking in subsirs, only looking at first results though.  check the test

       // cater for having two results trees of ids with duplicate ids - ie two vms





  }


  def checkNumberOfFiles(dataDirPath: String, expected: Int) = {

    val dir = new File(dataDirPath)
    val files =
      getFilesRecursively(dir.toPath)


    files.size should be (expected)

    files
  }

  def getFilesRecursively(path : Path) : List[Path]= {

    MoreFiles.listFiles(path).asScala.toList.flatMap(p => {

      if (p.toFile.isDirectory){
        p :: getFilesRecursively(p)
      }
      else List(p)

    })
  }

  "various failure scenarios" must "result in data being captured to be able to create the report" in {

    // need a single feature, with a background, scenario, scenario outline + failing step impls + fail setup


    val theFeature =
"""
Feature: failing outline feature

Scenario Outline: a failing outline scenario idx <idx>
  TheStep <idx>

Examples:
  |idx |
  |1   |
  |2   |
  |3   |
"""

    val standardFeature =
      """
Feature: failing feature

Scenario: a failing scenario
  TheStep 1

Scenario: another failing scenario
   TheStep 2

"""

    object Fail{
      var counter = 0
      var failCount = -1
      var failType = "none"
      var beforeFail = true
      var failingParameter = "2"

      def isFailure(arg : String) = {

        println("isFail arg:" + arg)

        if (arg == failingParameter) throw new IllegalStateException("something went wrong")
      }

      def isSetupFail(isBefore : Boolean, name : String) = {
        counter = counter + 1
        println(s"is setup before: $isBefore type: $name counter: $counter is fail? ")

        if (isBefore == beforeFail && name == failType && ( (failCount != -1 && counter == failCount) || failCount == -1) ) throw new IllegalStateException(s"setup fail on iteration $counter")
      }

      def reset = counter = 0
    }

    class InitClass {

      import com.technophobia.substeps.runner.setupteardown.Annotations._


      @AfterEveryScenario
      def afterEveryScenario () = Fail.isSetupFail(false, "scenario")

      @AfterEveryFeature
      def afterEveryFeature () = Fail.isSetupFail(false, "feature")

      @BeforeEveryScenario
      def beforeEveryScenario () = Fail.isSetupFail(true, "scenario")

      @BeforeEveryFeature
      def beforeEveryFeature () = Fail.isSetupFail(true, "feature")

      @AfterAllFeatures
      def afterAllFeatures () = Fail.isSetupFail(false, "suite")

      @BeforeAllFeatures
      def beforeAllFeatures () = Fail.isSetupFail(true, "suite")

    }

    //noinspection TypeAnnotation
    @StepImplementations
    class StepImpls2  extends ProvidesScreenshot {

      @SubSteps.Step("TheStep (.*)")
      def theStep(arg : String) = {
        Fail.isFailure(arg)
    //    if (arg == failingParameter)   throw new IllegalStateException("something went wrong")
      }

      override def getScreenshotBytes: Array[Byte] = "fake screenshot bytes".getBytes
    }

    val subStepParser: SubStepDefinitionParser = new SubStepDefinitionParser(true, new DefaultSyntaxErrorReporter)

    val outlineFeatureFile = createFeatureFile(theFeature, "failing_outline_feature_file.feature")
    val standardFeatureFile = createFeatureFile(standardFeature, "failing_feature_file.feature")

    implicit val stepImplementationClasses : List[Class[_]] = List(classOf[StepImpls2])

    val implementation = new StepImpls2()
    val setup = new InitClass()

    implicit val classNames = Tuple2(setup.getClass.getName, implementation.getClass.getName)


    implicit val methodExecutorToUse = new TestImplementationCache()
    methodExecutorToUse.addImpl(implementation.getClass, implementation)
    methodExecutorToUse.addImpl(setup.getClass, setup)


    // TODO begin loop, start checking scenarios where it might fail - this might need to be higher to accommodate different folders

    // how to set up the test scenarios, run test, assert accordingly

    import org.json4s._
    import org.json4s.native.JsonMethods._
    implicit val formats = DefaultFormats


    // all ok, no failures

    if (true) {

      Fail.failingParameter = "NONE"
      Fail.failType = "scenario"
      Fail.beforeFail = true
      Fail.failCount = -1 // all, feature, scenario 1 setup, scenario 1 tear down, scenario 2


      val dataDirPath1 = getBaseDir(new File("target")).getAbsolutePath
      val reportOutDir1 = getBaseDir(new File("target"), "substeps-report_")

      Fail.failType = "none"
      runFailingTestScenario(
        dataDirPath1, reportOutDir1, List(outlineFeatureFile, standardFeatureFile))
      // tests should pass - no errors

      val paths1 = checkNumberOfFiles(dataDirPath1, 12)

      val results1 =
        Files.asCharSource( paths1.find(p => p.endsWith("results.json")).get.toFile, Charset.defaultCharset()).read()

      val jval: JValue = parse(results1)

      (jval \ "result").toOption match {
        case None => fail("no result in the results.json")
        case Some(x) => x.extract[String] should be("PASSED")
      }
    }

    ///////////////////////////////////////////////////////////////
    // every scenario setup fails

    if (true) {
      val dataDirPath2 = getBaseDir(new File("target")).getAbsolutePath
      val reportOutDir2 = getBaseDir(new File("target"), "substeps-report_")
      Fail.failType = "scenario"
      runFailingTestScenario(
        dataDirPath2, reportOutDir2, List(outlineFeatureFile, standardFeatureFile))
      // TODO all scenarios and outline iterations should fail, marked as not run

      val paths2 = checkNumberOfFiles(dataDirPath2, 12)

      val resultsPaths2 = paths2.filter(p => p.toString.endsWith("results.json"))

      resultsPaths2.size should be(8)

      val allResults2 =
        resultsPaths2.flatMap(p => {

          val resultsContents = Files.asCharSource( p.toFile, Charset.defaultCharset()).read()

          val resultsFields =
            parse(resultsContents) filterField {
              case JField("result", _) => true
              case _ => false
            }


          resultsFields.map(rf => rf._2.extract[String])
        })

      allResults2.filter(r => r == "FAILED").size should be(11)
      allResults2.filter(r => r == "NOT_RUN").size should be(5)
      allResults2.filter(r => r == "CHILD_FAILED").size should be(4)
    }

    ////////////////////////////////////////////////////////////////////////
    // second scenario outline setup fails

    if (true) {

      val dataDirPath3 = getBaseDir(new File("target")).getAbsolutePath
      val reportOutDir3 = getBaseDir(new File("target"), "substeps-report_")
      Fail.reset
      Fail.failType = "scenario"
      Fail.failingParameter = "none"
      Fail.failCount = 5 // all, feature, scenario 1 setup, scenario1 tear down, scenario 2 BOOM
      runFailingTestScenario(
        dataDirPath3, reportOutDir3, List(outlineFeatureFile))
      // scenario outline, fail the second iteration only, carries on running the others

      val paths3 = checkNumberOfFiles(dataDirPath3, 8)

      val resultsPaths3 = paths3.find(p => p.toString.endsWith("feature.results.json"))


      val feaatureResultsContents3 = Files.asCharSource( resultsPaths3.get.toFile, Charset.defaultCharset()).read()

      val jresults3 = parse(feaatureResultsContents3)

      val resultsFields3 =
        jresults3 filterField {
          case JField("result", _) => true
          case _ => false
        }

      val resultsValues3 = resultsFields3.map(rf => rf._2.extract[String])

      resultsValues3 should be(List("CHILD_FAILED", "PASSED", "FAILED", "PASSED"))
    }

    //////////////////////////////////////////////////////
    // first feature setup fails, second is ok

    if (true) {

      // TODO - no stack trace for the failed feature, scenarios not expanded to the steps underneath

      val dataDirPath4 = getBaseDir(new File("target")).getAbsolutePath
      val reportOutDir4 = getBaseDir(new File("target"), "substeps-report_")
      Fail.reset
      Fail.failCount = 2 // all, f1 setup (BOOM)..
      Fail.failType = "feature"
      Fail.failingParameter = "none"
      runFailingTestScenario(
        dataDirPath4, reportOutDir4, List(standardFeatureFile, outlineFeatureFile))

      val paths4 = checkNumberOfFiles(dataDirPath4, 10)

      val results4 = Files.asCharSource( paths4.find(p => p.endsWith("results.json")).get.toFile, Charset.defaultCharset()).read()

      val jresults4: JValue = parse(results4)

      val resultsFields4 =
        jresults4 filterField {
          case JField("result", _) => true
          case _ => false
        }

      val resultsValues4 = resultsFields4.map(rf => rf._2.extract[String])

      resultsValues4 should be(List("FAILED", "FAILED", "PASSED"))

    }

    ////////////////////////////////////////////////////////////
    // suite set up fails
    // TODO - there's some errors in this scenario

    if (false) {
      // suite setup failure
      val dataDirPath5 = getBaseDir(new File("target")).getAbsolutePath
      val reportOutDir5 = getBaseDir(new File("target"), "substeps-report_")
      Fail.reset
      Fail.failCount = 1 // all (BOOM)..
      Fail.failType = "suite"
      runFailingTestScenario(
        dataDirPath5, reportOutDir5, List(standardFeatureFile, outlineFeatureFile))
      // TODO - only one feature listed - no stack trace, not details

      // TODO will fail
      checkNumberOfFiles(dataDirPath5, 10)

    }


  }

  type InitClassName = String
  type StepImplClassName = String


  private def runFailingTestScenario(dataDirPath: String,
                                     reportOutDir: File, featureList: List[FeatureFile])(implicit stepImplementationClasses: List[Class[_]],
                                                                                         classNames : (String, String),
                                                                                         methodExecutorToUse: TestImplementationCache) = {
    val initClassName = classNames._1
    val stepImplClassName= classNames._2

    val cfgFileContents =
      s"""
         | org {
         | substeps {
         | baseExecutionConfig {
         |         featureFile="failing_feature_file.feature"
         |         stepImplementationClassNames=[ "${stepImplClassName}"]
         |  initialisationClasses=[
         |    "${initClassName}"
         |  ]
         |
         | }
         |     executionConfigs=[
         |         {
         |         dataOutputDir=1
         |         description="Parsing from source Test Features 1"
         |         }
         |        ]
         |  config {
         |     rootDataDir="${dataDirPath}"
         |     description="Parsing from source test suite"
         |     }
         |  }
         | }
      """.stripMargin

    println("CONFIG contents\n" + cfgFileContents)

    val baseCfg = ConfigFactory.parseString(cfgFileContents)

    println("BASE CFG: " + baseCfg.root().render())

    val masterConfig =
      baseCfg.withFallback(ConfigFactory.load(ConfigParseOptions.defaults(), ConfigResolveOptions.noSystem().setAllowUnresolved(true)))
        .withValue("org.substeps.config.reportDir", ConfigValueFactory.fromAnyRef(reportOutDir.toString))

    val configs = SubstepsConfigLoader.splitMasterConfig(masterConfig).asScala

    val syntax: Syntax = SyntaxBuilder.buildSyntax(stepImplementationClasses.asJava, new PatternMap[ParentStep])

    val parameters: TestParameters = new TestParameters(new TagManager(""), syntax, featureList.asJava)

    val rootDataDir: File = NewSubstepsExecutionConfig.getRootDataDir(masterConfig)

    // write out the master config to the root data dir, the report builder needs to pick it up
    ExecutionResultsCollector.writeMasterConfig(masterConfig)


    configs.foreach(cfg => {

      NewSubstepsExecutionConfig.setThreadLocalConfig(cfg)

      val executionCollector = new ExecutionResultsCollector

      val dataDirForReportBuilder = NewSubstepsExecutionConfig.getDataOutputDirectory(cfg)

      executionCollector.setDataDir(dataDirForReportBuilder)
      executionCollector.setPretty(true)

      val runner = new ExecutionNodeRunner()

      runner.addNotifier(executionCollector)

      val stepImplementationClasses = NewSubstepsExecutionConfig.getStepImplementationClasses(cfg)
      val initialisationClasses = NewSubstepsExecutionConfig.getInitialisationClasses(cfg)

      val initClassList: util.List[Class[_]] =

        if (initialisationClasses != null) initialisationClasses.toList.asJava else null

      val finalInitClasses = ExecutionNodeRunner.buildInitialisationClassList(stepImplementationClasses, initClassList)
      val setupAndTearDown = new SetupAndTearDown(finalInitClasses, methodExecutorToUse)

      val rootNode2 = runner.prepareExecutionConfig(cfg, syntax, parameters, setupAndTearDown, methodExecutorToUse, null)

      executionCollector.initOutputDirectories(rootNode2)

      log.debug("rootNode 2:\n" + rootNode2.toDebugString)

      val finalRootNode = runner.run()

      log.debug("finalRootNode:\n" + finalRootNode.toDebugString)
    })

    // TODO - ASSERTIONS !

    // try all combinations

    val localReportBuilder = NewSubstepsExecutionConfig.getReportBuilder(masterConfig)
    val reportDir = NewSubstepsExecutionConfig.getReportDir(masterConfig)


    localReportBuilder.buildFromDirectory(rootDataDir, reportDir, null)
  }
}

class TestImplementationCache extends ImplementationCache{

  def addImpl(key : Class[_], instance : Object) = {
    instanceMap.put(key, instance)
  }
}

