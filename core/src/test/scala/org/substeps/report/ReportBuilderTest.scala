package org.substeps.report

import java.io.File
import java.nio.charset.Charset
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

import com.google.common.base.Strings
import com.google.common.io.Files

import scala.collection.JavaConversions._
import scala.collection.JavaConverters._
import org.scalatest._

import org.hamcrest.text.IsEqualIgnoringWhiteSpace.equalToIgnoringWhiteSpace

/**
  * Created by ian on 30/06/16.
  */
class ReportBuilderTest extends FlatSpec with ShouldMatchers{

  val UTF8 = Charset.forName("UTF-8")

  "ReportBuilder" should "read a correct model from the source data dir" in {

    val now: LocalDateTime = LocalDateTime.now

    val outputDir = new File("target/substeps-report_" + now.format(DateTimeFormatter.ofPattern("YYYYMMddHHmm")))
    val reportBuilder = new ReportBuilder
    reportBuilder.reportDir = outputDir

    val model = reportBuilder.readModel(new File("src/test/resources/sample-results-data"))

    val rootNodeSummary = model._1
    val featureSummaryAndNodeDetails = model._2

    featureSummaryAndNodeDetails should have size (2)

    featureSummaryAndNodeDetails(0)._1.scenarios should have size (2)
  }



  "ReportBuilder" should "build the usage tree report from raw data input" in {
    val now: LocalDateTime = LocalDateTime.now

    val outputDir = new File("target/substeps-report_" + now.format(DateTimeFormatter.ofPattern("YYYYMMddHHmm")))

    val reportBuilder = new ReportBuilder
    reportBuilder.reportDir = outputDir

    reportBuilder.buildFromDirectory(new File("src/test/resources/sample-results-data"))

    // TODO few more assertions please !
  }


  "ReportBuilder" should "build a report from raw data input" in {

    // TODO - could we create an object with reflection ? or a trait ?

    val now: LocalDateTime = LocalDateTime.now

    val outputDir = new File("target/substeps-report_" + now.format(DateTimeFormatter.ofPattern("YYYYMMddHHmm")))

    val reportBuilder = new ReportBuilder
    reportBuilder.reportDir = outputDir


    reportBuilder.buildFromDirectory(new File("src/test/resources/sample-results-data"))




    outputDir.exists() should be (true)

    val reportFiles = outputDir.listFiles().toList

//    reportFiles should have size (6)

    val detail_data = reportFiles.find(f => f.getName == "detail_data.js")

    detail_data shouldBe defined

    val newDetailDatajs = Files.toString(detail_data.get, UTF8)

    val baselineDetailDatajs = Files.toString(new File("src/test/resources/sample_feature_report_data/detail_data.js"), UTF8)


    println(s"DIFF: baseline\n\n${baselineDetailDatajs}\n\n\ntestoutput:\n\n${newDetailDatajs}")
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



  }



//  case class Attr(id : String)
//  case class Data (title : String, attr : Attr, icon : String)
//
//  case class DataHolder(id : String, title : String, icon : String) {
//    def toMap = Map("data" -> Map("title" -> title, "attr" -> Map("id" -> id), "icon" -> icon))
//  }
//
//
//  case class State(opened : Boolean)
//  case class JsTreeNode(id : String, text: String, icon : String, children : Option[List[JsTreeNode]], state : State)
//
//  case class Node(state:String, children : List[DataHolder])


}
