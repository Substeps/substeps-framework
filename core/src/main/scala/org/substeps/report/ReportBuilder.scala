package org.substeps.report

import java.io.{BufferedWriter, File}
import java.nio.charset.Charset

import com.google.common.io.Files
import com.technophobia.substeps.execution.ExecutionResult
import com.technophobia.substeps.report.{DefaultExecutionReportBuilder, DetailedJsonBuilder}
import org.json4s._
import org.json4s.native.Serialization
import org.json4s.native.Serialization._
import org.slf4j.{Logger, LoggerFactory}


object ReportBuilder {
  def openStateFor(result: String) = {

    result match {
      case "CHILD_FAILED" =>     Map ("state" -> "open")
      case "FAILED" => Map ("state" -> "open")
      case _ => Map()
    }

  }

  def iconFor(result: String): String = {
    icons.getOrElse(result, "img/PARSE_FAILURE.png")
  }


  val icons = Map("PASSED" -> "img/PASSED.png",
    "NOT_RUN" -> "img/NOT_RUN.png",
    "PARSE_FAILURE" -> "img/PARSE_FAILURE.png",
    "FAILED" -> "img/FAILED.png",
    "CHILD_FAILED" -> "img/FAILED.png")


  /* TODO
  full range of states:
  from ExecutionResult
      IGNORED(false), NOT_INCLUDED(false), NOT_RUN(false), RUNNING(false), PASSED(false), FAILED(true), NON_CRITICAL_FAILURE(false), PARSE_FAILURE(true),
    SETUP_TEARDOWN_FAILURE(true), CHILD_FAILED(true);
  */

  // total, run, passed, failed, skipped
  val resultToCounters = Map("PASSED" -> Counters.build(1,1,1,0,0),
    "NOT_RUN" -> Counters.build(1,0,0,0,1),
    "PARSE_FAILURE" -> Counters.build(1,0,0,1,0),
    "FAILED" -> Counters.build(1,1,0,1,0),
    "CHILD_FAILED" -> Counters.build(1,1,0,1,0))
}

/**
  * Created by ian on 30/06/16.
  */
class ReportBuilder (outputDir : File) extends ReportFrameTemplate {

  private val log: Logger = LoggerFactory.getLogger(classOf[ReportBuilder])

  def writeStatsJs(statsJsFile: File, stats: (List[Counters], List[Counters])) = {

    val writer = Files.newWriter(statsJsFile, Charset.defaultCharset)

    implicit val formats = Serialization.formats(NoTypeHints)

    writer.append("var featureStatsData = ")
    writer.append(writePretty(stats._1))
    writer.append(";\nvar scenarioStatsData = ")
    writer.append(writePretty(stats._2))
    writer.append(";\n")

    writer.flush()
    writer.close()
  }

  def buildExecutionStats(srcData: (RootNodeSummary, List[(FeatureSummary, List[NodeDetail])])): ExecutionStats = {

    val featureAndScenarioCounters =
    srcData._2.map(f => {

      val scenarioCounters =
        f._2.map(scenario => {
          ReportBuilder.resultToCounters.get(scenario.result) match {

            case Some(counter) => counter
            case _ => throw new IllegalStateException("unhandled result type in stats counter: " + scenario.result)
          }
        })

      val fCounters =
      ReportBuilder.resultToCounters.get(f._1.result) match {

        case Some(counter) => counter
        case _ => throw new IllegalStateException("unhandled result type in stats counter: " + f._1.result)
      }

      (fCounters, scenarioCounters)
    })

    var featureCounter = Counters.ZERO
    featureAndScenarioCounters.foreach(fc => {
        featureCounter = featureCounter + fc._1
      } )


    val scenarioCounters =
    featureAndScenarioCounters.flatMap(fands => fands._2)

    var scenarioCounter = Counters.ZERO
    scenarioCounters.foreach(sc => {
      scenarioCounter = scenarioCounter + sc
    } )


    val allNodeDetails =
    srcData._2.flatMap(f => {
      val scenarios = f._2

      scenarios.flatMap(scenario => scenario.flattenTree())
    })

    val stepImplNodeDetails = allNodeDetails.filter(nd => nd.nodeType == "Step")
    val substepNodeDetails = allNodeDetails.filter(nd => nd.nodeType == "SubstepNode")

    val substepCounters =
      substepNodeDetails.foldLeft(Counters.ZERO) { (counters, nodeDetail) =>

        ReportBuilder.resultToCounters.get(nodeDetail.result) match {
          case Some(c) => counters + c
          case None => counters
        }
      }

    val stepImplCounters =
      stepImplNodeDetails.foldLeft(Counters.ZERO) { (counters, nodeDetail) =>

        ReportBuilder.resultToCounters.get(nodeDetail.result) match {
          case Some(c) => counters + c
          case None => counters
        }
      }

    ExecutionStats(featureCounter, scenarioCounter, substepCounters, stepImplCounters)

  }


  def buildExecutionStatsByTag(srcData: (RootNodeSummary, List[(FeatureSummary, List[NodeDetail])])) = {

    val allNodeDetails =
      srcData._2.flatMap(f => {
        val scenarios = f._2

        scenarios.flatMap(scenario => scenario.flattenTree())
      })

    val scenarioNodeDetails = allNodeDetails.filter(nd => nd.nodeType == "BasicScenarioNode")

    def getCountersForTags(nodeDetails : List[NodeDetail]) = {
      val tags = nodeDetails.flatMap(n => n.tags).flatten.distinct

      tags.map(t => {
        val counters = nodeDetails.filter(n => n.tags.isDefined && n.tags.get.contains(t)).flatMap(n => ReportBuilder.resultToCounters.get(n.result))

        (counters.foldLeft(Counters.ZERO) { (a, b) => a + b }).copy(tag = Some(t))

      })
    }

    val untaggedCounters = scenarioNodeDetails.filter(n => n.tags.isEmpty).flatMap(n => ReportBuilder.resultToCounters.get(n.result))
    val totalUntaggedScenarios = (untaggedCounters.foldLeft(Counters.ZERO) { (a, b) => a + b }).copy(tag = Some("un-tagged"))

    val scenarioCountersWithTags = getCountersForTags(scenarioNodeDetails)



    val featureSummaries = srcData._2.map(_._1)
    val allFeatureTags = featureSummaries.flatMap(f => f.tags).distinct

    val featureCountersWithTags =
    allFeatureTags.map(t => {
      val counters = featureSummaries.filter(f => f.tags.contains(t)).flatMap(n => ReportBuilder.resultToCounters.get(n.result))
      (counters.foldLeft(Counters.ZERO) { (a, b) => a + b }).copy(tag = Some(t))
    })

    val untaggedFeatureSummaries = featureSummaries.filter(n => n.tags.isEmpty).flatMap(n => ReportBuilder.resultToCounters.get(n.result))
    val totalUntaggedFeatureSummaries = (untaggedFeatureSummaries.foldLeft(Counters.ZERO) { (a, b) => a + b }).copy(tag = Some("un-tagged"))


    val result = (featureCountersWithTags :+ totalUntaggedFeatureSummaries, scenarioCountersWithTags :+ totalUntaggedScenarios)
    result
  }

  def buildFromDirectory(sourceDataDir: File): Unit = {

    outputDir.mkdir()

    val detailData = createFile( "detail_data.js")

    val srcData: (RootNodeSummary, List[(FeatureSummary, List[NodeDetail])]) = readModel(sourceDataDir)

    createDetailData(detailData,srcData)

    val resultsTreeJs = createFile( "substeps-results-tree.js")

    createTreeData2(resultsTreeJs,srcData)

    val reportFrameHTML = createFile( "report_frame.html")

    
    val stats : ExecutionStats = buildExecutionStats(srcData)

    val reportFrameHtml = buildReportFrame("title", "dateTime", stats)
    val writer = Files.newWriter(reportFrameHTML, Charset.defaultCharset)
    writer.append(reportFrameHtml)
    writer.flush()
    writer.close()

    copyStaticResources()

    val statsByTag = buildExecutionStatsByTag(srcData)

    val statsJsFile = createFile("substeps-stats-by-tag.js")
    writeStatsJs(statsJsFile, statsByTag)
  }

  def copyStaticResources() = {

    val defaultBuilder = new DefaultExecutionReportBuilder

    defaultBuilder.copyStaticResources(outputDir)
  }

  def createFile(name : String) = {
    val f = new File(outputDir, name)
    f.createNewFile()
    f
  }

  def readModel(srcDir : File) = {

    implicit val formats = Serialization.formats(NoTypeHints)

    val resultsFileOption: Option[RootNodeSummary] =
    srcDir.listFiles().toList.find(f => f.getName == "results.json").map(resultsFile => {
      read[RootNodeSummary](Files.toString(resultsFile, Charset.defaultCharset()))
    })

    val featureSummaries =

      resultsFileOption match {
      case Some(resultsFile) => {

        resultsFile.features.flatMap(featureSummary => {

          val featureFileResultsDir = new File(srcDir, featureSummary.resultsDir)
          loadFeatureData(featureFileResultsDir) match {

            case Some (f) => {

              val scenarioDetails =
              f.scenarios.map(scenarioSummary => {

                val scenarioResultsFile = new File(featureFileResultsDir, scenarioSummary.filename)

                read[NodeDetail](Files.toString(scenarioResultsFile, Charset.defaultCharset()))
              })

              Some(f, scenarioDetails)

            }
            case None => None
          }
        })
      }
      case None => List()
    }

    (resultsFileOption.get, featureSummaries)

  }

  def loadFeatureData(srcDir : File) = {

    implicit val formats = Serialization.formats(NoTypeHints)

    srcDir.listFiles().toList.find(f => f.getName == srcDir.getName + ".json").map(featureResultsFile =>{
      read[FeatureSummary](Files.toString(featureResultsFile, Charset.defaultCharset()))
    })

  }


  def createTreeData2(file : File, srcData : (RootNodeSummary, List[(FeatureSummary, List[NodeDetail])])) = {

    val children =
      srcData._2.map(features => {

        val featureScenarios = features._2.map(n => buildForJsonTreeNode(n))

        val childrenOption = if (featureScenarios.isEmpty) None else Some(featureScenarios)

        // create a node for the feature:
        val icon = ReportBuilder.iconFor(features._1.result)

        val state = State.forResult(features._1.result)

        JsTreeNode(features._1.id.toString, features._1.description, icon, childrenOption, state)

      })

    val childrenOption = if (children.isEmpty) None else Some(children)

    val icon = ReportBuilder.iconFor(srcData._1.result)

    val state = State.forResult(srcData._1.result)

    val rootNode = JsTreeNode(srcData._1.id.toString, srcData._1.description, icon, childrenOption, state)

    val writer = Files.newWriter(file, Charset.defaultCharset)
    writer.append("var treeData = ")

    implicit val formats = Serialization.formats(NoTypeHints)

    writer.append(writePretty(rootNode))
    writer.flush()
    writer.close()
  }


  def buildForNode(node : NodeDetail) : Map[String, Object] = {

    val children =
    node.children.map(child => {
      buildForNode(child)
    })

    val childrenMap = if (children.isEmpty) Map() else Map("children" -> children)

    val icon = ReportBuilder.iconFor(node.result)

    val openStateMap = ReportBuilder.openStateFor(node.result)

    val dataMap = Map("title" -> node.description, "attr" -> Map("id" -> node.id.toString), "icon" -> icon) ++ openStateMap

    val result =   Map("data" -> dataMap) ++ childrenMap

    result
  }


  def buildForJsonTreeNode(node : NodeDetail) : JsTreeNode = {

    val children =
      node.children.map(child => {
        buildForJsonTreeNode(child)
      })


    val childrenOption = if (children.isEmpty) None else Some(children)

    val icon = ReportBuilder.iconFor(node.result)

    val state = State.forResult(node.result)

    JsTreeNode(node.id.toString, node.description, icon, childrenOption, state)

  }




  def createDetailData(file : File, srcData : (RootNodeSummary, List[(FeatureSummary, List[NodeDetail])])) = {
    val writer = Files.newWriter(file, Charset.defaultCharset)
    writer.append("var detail = new Array();\n")

    val featureSummaries = srcData._2.map( _._1)

    val nodeDetailList = srcData._2.flatMap(_._2)

    writeRootNode(writer, srcData._1, featureSummaries)

    featureSummaries.foreach(f => {
      writeFeatureNode(writer, f, nodeDetailList)
    })

    writer.flush()
    writer.close()
  }


  def writeFeatureNode(writer: BufferedWriter, f: FeatureSummary, nodeDetailList: List[NodeDetail]) = {

    writer.append(s"""detail[${f.id}]={"nodetype":"FeatureNode","filename":"${f.filename}","result":"${f.result}","id":${f.id},
       |"runningDurationMillis":${f.executionDurationMillis.getOrElse(-1)},"runningDurationString":"${f.executionDurationMillis.getOrElse(-1)} milliseconds",
       |"description":"${f.description}",
       |"children":[""".stripMargin.replaceAll("\n", ""))

    val children =
    f.scenarios.map(sc => {

      val nodeDetail = nodeDetailList.find(n => n.id == sc.nodeId).get

      s"""{"result":"${sc.result}","description":"${nodeDetail.description}"}"""
    })

    writer.append(children.mkString(","))
    writer.append("]};\n")

    f.scenarios.foreach(sc => {

      val scenarioNodeDetail = nodeDetailList.find(n => n.id == sc.nodeId).get

      writeNodeDetail(writer, scenarioNodeDetail)
    })
  }

  def writeNodeDetail(writer: BufferedWriter, nodeDetail: NodeDetail) : Unit = {

    writer.append(s"""detail[${nodeDetail.id}]={"nodetype":"${nodeDetail.nodeType}","filename":"${nodeDetail.filename}","result":"${nodeDetail.result}","id":${nodeDetail.id},
    |"runningDurationMillis":${nodeDetail.executionDurationMillis.getOrElse(-1)},
    |"runningDurationString":"${nodeDetail.executionDurationMillis.getOrElse("n/a")} milliseconds","description":"${nodeDetail.description}",""".stripMargin.replaceAll("\n", ""))


    nodeDetail.exceptionMessage.map(s => writer.append(s""""emessage":"${s}",""") )
    nodeDetail.screenshot.map(s => writer.append(s"""screenshot:"${s}",""") )
    nodeDetail.stackTrace.map(s => writer.append(s"""stacktrace:[${s.mkString("\"", "\",\n\"", "\"")}],"""))

    writer.append(s""""children":[""")

    writer.append(nodeDetail.children.map(n => {
      s"""{"result":"${n.result}","description":"${n.description}"}"""
    }).mkString(","))

    writer.append("]};\n")

    nodeDetail.children.foreach(child => {
      writeNodeDetail(writer, child)
    })
  }



  def writeRootNode(writer : BufferedWriter, rootNodeSummary : RootNodeSummary, featureSummaries : List[FeatureSummary]) = {

//    "emessage":"At least one critical Feature failed",
//    "stacktrace": "",
// "description":null,

    writer.append(s"""detail[${rootNodeSummary.id}]={"nodetype":"RootNode","filename":"","result":"${rootNodeSummary.result}","id":${rootNodeSummary.id},"runningDurationMillis":${rootNodeSummary.executionDurationMillis.get},"runningDurationString":"${rootNodeSummary.executionDurationMillis.get} milliseconds","children":[""")

    writer.append(
    rootNodeSummary.features.map (f => {

      val fs = featureSummaries.find(fs => fs.id == f.nodeId).get

      s"""{"result":"${f.result}","description":"${fs.description}"}"""
    }).mkString(","))

    writer.append("]};\n")

  }
}
