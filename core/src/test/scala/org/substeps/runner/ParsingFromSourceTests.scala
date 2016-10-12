package org.substeps.runner

import java.io.File
import java.nio.charset.Charset

import com.google.common.io.Files
import com.technophobia.substeps.execution.ImplementationCache
import com.technophobia.substeps.model._
import com.technophobia.substeps.model.SubSteps.StepImplementations
import com.technophobia.substeps.parser.FileContents
import com.technophobia.substeps.report.DefaultExecutionReportBuilder
import com.technophobia.substeps.runner.builder.ExecutionNodeTreeBuilder
import com.technophobia.substeps.runner._
import com.technophobia.substeps.runner.setupteardown.SetupAndTearDown
import com.technophobia.substeps.runner.syntax._
import com.technophobia.substeps.steps.TestStepImplementations
import org.hamcrest.Matchers._
import org.json4s.NoTypeHints
import org.json4s.native.Serialization
import org.junit.Assert
import org.scalatest._
import org.substeps.report.{ExecutionResultsCollector, NodeDetail}

import scala.collection.JavaConverters._
import scala.collection.JavaConverters._
import org.json4s._
import org.json4s.native.Serialization
import org.json4s.native.Serialization.{read, write}
import org.slf4j.{Logger, LoggerFactory}

import scala.collection.JavaConversions._
import scala.collection.JavaConverters._

/**
  * Created by ian on 20/05/16.
  */
class ParsingFromSourceTests extends FlatSpec with ShouldMatchers {

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

    val fileContentsFromFile = FileContents.fromFile(new File("./target/test-classes/features/lineNumbers.feature"))

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

  def createFeatureFile(content: String, featureFileName: String): FeatureFile = {
    val featureFileContentsFromSource = new FileContents(content.split("\n").toList.asJava, new File(featureFileName))

    val parser: FeatureFileParser = new FeatureFileParser

    val featureFile: FeatureFile = parser.getFeatureFile(featureFileContentsFromSource)
    featureFile
  }


  /**
    * NB. this is the test that generates the source data, then used to test out report building
    */

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

    val cfgWrapper = new ExecutionConfigWrapper(executionConfig)
    val nodeTreeBuilder: ExecutionNodeTreeBuilder = new ExecutionNodeTreeBuilder(parameters, cfgWrapper)

    // building the tree can throw critical failures if exceptions are found
    val rootNode = nodeTreeBuilder.buildExecutionNodeTree("test description")

    log.debug("rootNode 1:\n" + rootNode.toDebugString)

    val executionCollector = new ExecutionResultsCollector
    val dataDir = ExecutionResultsCollector.getBaseDir(new File("target"))
    executionCollector.setDataDir(dataDir)
    executionCollector.setPretty(true)

    executionConfig.setDataOutputDirectory(dataDir.getAbsolutePath)

    val runner = new ExecutionNodeRunner()


    runner.addNotifier(executionCollector)

    val methodExecutorToUse = new ImplementationCache()

    val setupAndTearDown: SetupAndTearDown = new SetupAndTearDown(executionConfig.getInitialisationClasses, methodExecutorToUse)


    val rootNode2 = runner.prepareExecutionConfig(new ExecutionConfigWrapper(executionConfig), syntax, parameters, setupAndTearDown, methodExecutorToUse, null)

    executionCollector.initOutputDirectories(rootNode2)

    log.debug("rootNode 2:\n" + rootNode2.toDebugString)

    val finalRootNode = runner.run()

    // what are we expecting now:
//    val rootDir = executionCollector.getRootReportsDir
    dataDir.exists() should be (true)

    val featureDirs = dataDir.listFiles().toList.filter(f => f.isDirectory)

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

    val resultSummaryFile = dataDir.listFiles().toList.filter(f => f.isFile)

    resultSummaryFile.size should be (1)

    // used to compare the output from this report builder and the new one
//    val reportBuilder = new DefaultExecutionReportBuilder
//    reportBuilder.setOutputDirectory(new File("feature-report"))
//    reportBuilder.addRootExecutionNode(finalRootNode)
//    reportBuilder.buildReport()
  }

  def validateSimpleFeatureResults(featureDir: File) = {

    val featureResults = featureDir.listFiles().toList
    featureResults.length should be (4)

    val passingScenario = featureResults.find(s => s.getName.contains("passing_scenario"))

    passingScenario shouldBe defined

    implicit val formats = Serialization.formats(NoTypeHints)

    val passingScenarioContent = read[NodeDetail](Files.toString(passingScenario.get, UTF8))

    passingScenarioContent.children.size should be (3)

    passingScenarioContent.children(0).children should be (empty)

    passingScenarioContent.children(1).children.size should be (1)

    // failing scenario

    // some extra debug in here to work out what's going on
    log.debug("got feature results files in dir: " + featureDir.getAbsolutePath + " files: " + featureResults.mkString(","))

    val failingScenario = featureResults.find(s => s.getName.contains("failing_scenario_results.json"))

    failingScenario shouldBe defined

    val failingScenarioContent = read[NodeDetail](Files.toString(failingScenario.get, UTF8))

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





}


