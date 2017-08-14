package org.substeps.report

import java.io.File
import java.nio.charset.Charset
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

import com.google.common.base.Strings
import com.google.common.io.Files
import com.technophobia.substeps.glossary.StepImplementationsDescriptor
import com.typesafe.config.ConfigFactory

import scala.collection.JavaConversions._
import scala.collection.JavaConverters._
import org.scalatest._
import org.hamcrest.text.IsEqualIgnoringWhiteSpace.equalToIgnoringWhiteSpace
import org.json4s.native.JsonMethods.parse
import org.substeps.config.SubstepsConfigLoader

/**
  * Created by ian on 30/06/16.
  */
class ReportBuilderTest extends FlatSpec with Matchers{

  val UTF8 = Charset.forName("UTF-8")

  def getOutputDir = {
    val now: LocalDateTime = LocalDateTime.now
    val f = new File("target/substeps-report_" + now.format(DateTimeFormatter.ofPattern("YYYYMMddHHmmssSSS")))
    f.mkdir()
    f
  }

  "reading of uncalled step impls" must "deserialize" in {
    import org.json4s._
    import org.json4s.native.JsonMethods._

    implicit val formats = DefaultFormats

    val uri = this.getClass.getClassLoader.getResource("uncalled/uncalled.stepdefs.js")

    Option(uri) shouldBe defined

    println("uri get file: " + uri.getFile)
    
    val rawUncalledStepDefs = Files.toString(new File( uri.getFile), Charset.forName("UTF-8"))

    val uncalledStepDefs: List[UncalledStepDef] = parse(rawUncalledStepDefs).extract[List[UncalledStepDef]]

    println("done")
  }


  "ReportBuilder" should "read a correct model from the source data dir" in {

    val now: LocalDateTime = LocalDateTime.now

    implicit val outputDir = getOutputDir
    val reportBuilder = new ReportBuilder

    val uri = this.getClass.getClassLoader.getResource("sample-results-data")

    val dataDir = new File(uri.getFile)
    val masterConfig = ConfigFactory.parseFile(new File(dataDir, "masterConfig.conf"))//"src/test/resources/sample-results-data/masterConfig.conf"))

    Option(masterConfig) shouldBe defined


    val executionConfigs = SubstepsConfigLoader.splitMasterConfig(masterConfig).asScala.toList


    val modelList = reportBuilder readModels(dataDir, executionConfigs)

    val model = modelList.head

    val rootNodeSummary = model.rootNodeSummary
    val featureSummaryAndNodeDetails = model.featuresList

    featureSummaryAndNodeDetails should have size (2)

    featureSummaryAndNodeDetails(0).nodeDetails should have size (2)

  }

//  "ReportBuilder" should "build a report from real raw data input" in {
//    val outputDir = getOutputDir
//
//    val reportBuilder = new ReportBuilder
//
//    reportBuilder.buildFromDirectory(new File("/home/ian/projects/github/substeps-webdriver/target/substeps_data"), outputDir)
//
//  }

  /**
    * @see ParsingFromSourceTests line 537:
    *      "run some features to test the generation of raw report data" must "create the raw data files"
    *
    *      for the test that generates the sampple data
    */
  "ReportBuilder" should "build a report from raw data input" in {

    val now: LocalDateTime = LocalDateTime.now

    val outputDir = getOutputDir

    val reportBuilder = new ReportBuilder

    val uri = this.getClass.getClassLoader.getResource("sample-results-data")

    // "src/test/resources/sample-results-data"
    reportBuilder.buildFromDirectory(new File(uri.getFile), outputDir)

    outputDir.exists() should be (true)

    val reportFiles = outputDir.listFiles().toList

//    reportFiles should have size (6)

    val detail_data = reportFiles.find(f => f.getName == "detail_data.js")

    detail_data shouldBe defined

    val newDetailDatajs = Files.toString(detail_data.get, UTF8)

    val baselineDetailDatajs = Files.toString(new File("src/test/resources/sample_feature_report_data/detail_data.js"), UTF8)


    //println(s"DIFF: baseline\n\n${baselineDetailDatajs}\n\n\ntestoutput:\n\n${newDetailDatajs}")
    //  equalToIgnoringWhiteSpace(baselineDetailDatajs).matches(newDetailDatajs) should be (true)


    val treeHtml = reportFiles.find(f => f.getName == "tree.html")

    treeHtml should not be defined

    val report_datajson = reportFiles.find(f => f.getName == "substeps-results-tree.js")

    report_datajson shouldBe defined
    // TODO - how to test that this is valid ?



        val report_frame = reportFiles.find(f => f.getName == "report_frame.html")

        report_frame shouldBe defined


    // TODO - the other files
//
//
    val substepsStatsjs = reportFiles.find(f => f.getName == "substeps-stats-by-tag.js")

    substepsStatsjs shouldBe defined

    // TODO - usage data exists ?
  }


}
