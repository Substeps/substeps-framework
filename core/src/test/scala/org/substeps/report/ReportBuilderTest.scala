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
    val reportBuilder = new ReportBuilder(outputDir)

    val model = reportBuilder.readModel(new File("src/test/resources/sample-results-data"))

    val rootNodeSummary = model._1
    val featureSummaryAndNodeDetails = model._2

    featureSummaryAndNodeDetails should have size (2)

    featureSummaryAndNodeDetails(0)._1.scenarios should have size (2)
  }





  "ReportBuilder" should "build a report from raw data input" in {

    // TODO - could we create an object with reflection ? or a trait ?

    val now: LocalDateTime = LocalDateTime.now

    val outputDir = new File("target/substeps-report_" + now.format(DateTimeFormatter.ofPattern("YYYYMMddHHmm")))

    val reportBuilder = new ReportBuilder(outputDir)



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


  import org.json4s._
  import org.json4s.native.Serialization
  import org.json4s.native.Serialization.writePretty


  case class Attr(id : String)
  case class Data (title : String, attr : Attr, icon : String)

  case class DataHolder(id : String, title : String, icon : String) {
    def toMap = Map("data" -> Map("title" -> title, "attr" -> Map("id" -> id), "icon" -> icon))
  }


  case class State(opened : Boolean)
  case class JsTreeNode(id : String, text: String, icon : String, children : Option[List[JsTreeNode]], state : State)

  case class Node(state:String, children : List[DataHolder])


  "jstree case classes" should "serialize correctly" in {

    val st1 = JsTreeNode("6", "st1", "icon", None, State(true))
    val st2 = JsTreeNode("7", "st2", "icon", None, State(true))

    val s1 = JsTreeNode("4", "s1", "icon", Some(List(st1, st2)), State(true))

    val st3 = JsTreeNode("8", "st3", "icon", None, State(true))
    val st4 = JsTreeNode("9", "st4", "icon", None, State(true))

    val s2 = JsTreeNode("5", "s2", "icon", Some(List(st3, st4)), State(true))

    val f1 = JsTreeNode("2", "f1", "icon", Some(List(s1, s2)), State(true))

    val root = JsTreeNode("1", "root", "icon", Some(List(f1)), State(true))

    implicit val formats = Serialization.formats(NoTypeHints)

    println(writePretty(root))


  }

  "report_data case classes" should "serialize correctly" in {

    implicit val formats = Serialization.formats(NoTypeHints)


    val dataMap2 = Map("data" -> Map("title" -> "the title 2", "attr" -> Map("id" -> "2"), "icon" -> "img/PASSED.png"))
    val dataMap3 = Map("data" -> Map("title" -> "the title 3", "attr" -> Map("id" -> "3"), "icon" -> "img/PASSED.png"))
    val dataMap4 = Map("data" -> Map("title" -> "the title 4", "attr" -> Map("id" -> "4"), "icon" -> "img/PASSED.png"))

    val dataMap1 = Map("data" -> Map("title" -> "the title 1", "attr" -> Map("id" -> "1"), "icon" -> "img/PASSED.png"))

    val root = Map("state" -> "open", "children" -> List(dataMap1, dataMap2, dataMap3,dataMap4))

    println("using maps")
    println(writePretty(root))
    println("\n\n")

    val d1 = new DataHolder("1", "title 1", "img/PASSED.png")
    val d2 = new DataHolder("2", "title 2", "img/PASSED.png")
    val d3 = new DataHolder("3", "title 3", "img/PASSED.png")
    val d4 = new DataHolder("4", "title 4", "img/PASSED.png")

    println("objects..")
    println(writePretty(Node("open", List(d1,d2,d4,d4))))


  }
}
