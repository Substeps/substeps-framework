package org.substeps.report

import java.io.{BufferedWriter, File, FileNotFoundException}
import java.nio.charset.Charset
import java.time.{Instant, LocalDateTime, ZoneId, ZoneOffset}
import java.time.format.DateTimeFormatter

import com.google.common.io.Files
import com.technophobia.substeps.execution.ExecutionResult
import com.technophobia.substeps.report.{DefaultExecutionReportBuilder, DetailedJsonBuilder}
import org.apache.commons.io.FileUtils
import org.apache.commons.lang3.StringEscapeUtils
import org.json4s._
import org.json4s.native.Serialization
import org.json4s.native.Serialization._
import org.slf4j.{Logger, LoggerFactory}

import scala.beans.BeanProperty


object ReportBuilder {
  def openStateFor(result: String) = {

    result match {
      case "CHILD_FAILED" =>     Map ("state" -> "open")
      case "FAILED" => Map ("state" -> "open")
      case "NON_CRITICAL_FAILURE"  => Map ("state" -> "open")
      case _ => Map()
    }

  }

  def iconFor(result: String): String = {
    icons.getOrElse(result, "PARSE_FAILURE")
  }


  val icons = Map("PASSED" -> "PASSED",
    "NOT_RUN" -> "NOT_RUN",
    "PARSE_FAILURE" -> "PARSE_FAILURE",
    "FAILED" -> "FAILED",
    "CHILD_FAILED" -> "CHILD_FAILED",
    "NON_CRITICAL_FAILURE" -> "NON_CRITICAL_FAILURE")


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


  var counter : Long = 1L

  def uniqueId (realId : Long) = {
    val id = s"$counter-$realId"
    counter = counter + 1
    id
  }

}

case class FeatureDetails(summary : FeatureSummary, nodeDetails :  List[NodeDetail])

case class SourceDataModel(rootNodeSummary : RootNodeSummary, featuresList : List[FeatureDetails]) {
  def this(rootNodeSummary : RootNodeSummary, features : List[FeatureDetails]) = this(List(rootNodeSummary), List(features))
}

case object SourceDataModel{

//  def merge (list : List[SourceDataModel]) : SourceDataModel = {
//
//    val rootNodeSummaries =
//    list.flatMap(srcData => {
//      srcData.rootNodeSummary
//    })
//
//    val allFeatures =
//      list.flatMap(srcData => {
//        srcData.featuresList
//      })
//
//    SourceDataModel(rootNodeSummaries, allFeatures)
//  }

}

/**
  * Created by ian on 30/06/16.
  */
class ReportBuilder extends IReportBuilder with ReportFrameTemplate with UsageTreeTemplate with GlossaryTemplate {

  // TODO need to leave here as legacy to ensure old behaviour still works, although new structure will mandate change ??
//  @BeanProperty
//  var reportDir : File = new File(".")

  private val log: Logger = LoggerFactory.getLogger(classOf[ReportBuilder])


  def safeCopy(src : File, dest : File) = {
    try {
      FileUtils.copyFile(src, dest)
    }
    catch {
      case e : FileNotFoundException => log.warn("failed to find source file: " + src.getAbsolutePath)
    }
  }

  def buildGlossaryData(sourceJsonFile : File) = {
    implicit val formats = Serialization.formats(NoTypeHints)

    val glossaryElements =

      if (sourceJsonFile.exists()) {
        val data = read[List[StepImplDesc]](sourceJsonFile)

        data.map(sid => sid.expressions.map(sd => {

          val escapedExpression =
            sd.expression.replaceAll("\\$$", "").replaceAll("<", "&lt;").replaceAll(">", "&gt;")

          GlossaryElement(sd.section, escapedExpression, sid.className, sd.regex, sd.example, sd.description, sd.parameterNames, sd.parameterClassNames)
        })).flatten
      }
      else {
        List()
      }
    glossaryElements
  }

  override def buildFromDirectory(sourceDataDir: File, reportDir : File): Unit = {
    buildFromDirectory(sourceDataDir, reportDir, null)
  }

  override def buildFromDirectory(sourceDataDir: File, reportDir : File, stepImplsJson : File): Unit = {

    val created = reportDir.mkdir()

    log.debug("creating report in " + reportDir.getAbsolutePath + " create returns: " + created)

    implicit val rDir : File = reportDir

    val dataDir = new File(reportDir, "data")
    dataDir.mkdir()

    FileUtils.copyDirectory(new File(sourceDataDir.getPath), dataDir)

    safeCopy(new File(sourceDataDir, "uncalled.stepdefs.js"), new File(reportDir, "uncalled.stepdefs.js"))
    safeCopy(new File(sourceDataDir, "uncalled.stepimpls.js"), new File(reportDir, "uncalled.stepimpls.js"))

    val detailData = createFile( "detail_data.js")

    val srcDataList: List[SourceDataModel] = readModels(dataDir)

//    val srcData = SourceDataModel.merge(srcDataList)

    createDetailData(detailData,srcDataList)

    val resultsTreeJs = createFile( "substeps-results-tree.js")

    createTreeData2(resultsTreeJs,srcData)

    val reportFrameHTML = createFile( "report_frame.html")


    val stats : ExecutionStats = buildExecutionStats(srcData)

    // TODO - only taking the head element
    val localDate = LocalDateTime.ofInstant(Instant.ofEpochMilli(srcData.rootNodeSummary.head.timestamp), ZoneId.systemDefault())
    val dateTimeString = localDate.format(DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm:ss"))


    // TODO - only taking the head element

    val reportFrameHtml = buildReportFrame(srcData.rootNodeSummary.head, stats, dateTimeString)


    withWriter(reportFrameHTML, writer => writer.append(reportFrameHtml))

    copyStaticResources()

    val statsByTag = buildExecutionStatsByTag(srcData)

    val statsJsFile = createFile("substeps-stats-by-tag.js")
    writeStatsJs(statsJsFile, statsByTag)


    val usageTreeDataFile = createFile("substeps-usage-tree.js")

    createUsageTree(usageTreeDataFile, srcData)

    val usageTreeHTMLFile  = createFile( "usage-tree.html")

    val usageTreeHtml = buildUsageTree()

    withWriter(usageTreeHTMLFile, writer => writer.append(usageTreeHtml) )

    createGlossary(stepImplsJson, dateTimeString)
  }

  def createGlossary(stepImplsJson : File, dateTimeString :String)(implicit reportDir : File): Unit = {

    if (Option(stepImplsJson).isDefined) {
      val glossaryHTML = createFile("glossary.html")
      val glossaryContent = buildGlossaryReport(dateTimeString)

      val glossaryData = buildGlossaryData(stepImplsJson)
      val glossaryJsFile = createFile("glossary-data.js")
      writeGlossaryJs(glossaryJsFile, glossaryData)

      withWriter(glossaryHTML, writer => writer.append(glossaryContent))
    }
  }

  def writeGlossaryJs(glossaryJSFile : File, glossaryData : List[GlossaryElement]) = {

    withWriter(glossaryJSFile, writer => {
      implicit val formats = Serialization.formats(NoTypeHints)
      writer.append("var glossary=")
      writer.append(writePretty(glossaryData))
      writer.append(";\n")
    })
  }

  def writeStatsJs(statsJsFile: File, stats: (List[Counters], List[Counters])): Unit = {

    withWriter(statsJsFile, writer => {

      implicit val formats = Serialization.formats(NoTypeHints)

      writer.append("var featureStatsData = ")
      writer.append(writePretty(stats._1))
      writer.append(";\nvar scenarioStatsData = ")
      writer.append(writePretty(stats._2))
      writer.append(";\n")
    })
  }

  def buildExecutionStats(srcData: SourceDataModel): ExecutionStats = { //(RootNodeSummary, List[(FeatureSummary, List[NodeDetail])])): ExecutionStats = {

    val featureAndScenarioCounters =
    srcData.features.map(f => {

      val scenarioCounters =
        f.nodeDetails.map(scenario => {
          ReportBuilder.resultToCounters.get(scenario.result) match {

            case Some(counter) => counter
            case _ => throw new IllegalStateException("unhandled result type in stats counter: " + scenario.result)
          }
        })

      val fCounters =
      ReportBuilder.resultToCounters.get(f.summary.result) match {

        case Some(counter) => counter
        case _ => throw new IllegalStateException("unhandled result type in stats counter: " + f.summary.result)
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
    srcData.features.flatMap(f => {
      val scenarios = f.nodeDetails

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


  def buildExecutionStatsByTag(srcData: SourceDataModel) = { //(RootNodeSummary, List[(FeatureSummary, List[NodeDetail])])) = {

    val allNodeDetails =
      srcData.features.flatMap(f => {
        val scenarios = f.nodeDetails

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



    val featureSummaries = srcData.features.map(_.summary)
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

  def getHierarchy(nodeDetail : NodeDetail, allNodes : List[NodeDetail]) :  List[NodeDetail] = {

    allNodes.find(n => n.children.exists(c => c.id == nodeDetail.id)) match {

      case None => List(nodeDetail)
      case Some(parent) => List(nodeDetail) ++ getHierarchy(parent, allNodes)

    }
  }

  def getCallHierarchy(thisNodeDetail : NodeDetail, allNodes : List[NodeDetail]) : List[NodeDetail] = {

    allNodes.find(n => n.children.exists(c => c.id == thisNodeDetail.id)) match {
      case None => List() // this node is not a child of any other node
      case Some(parent) => parent +: getCallHierarchy(parent, allNodes) // prepend
    }

  }


  def createUsageTree(usageTreeDataFile: File, srcData: SourceDataModel) = { //(RootNodeSummary, List[(FeatureSummary, List[NodeDetail])])) = {

    val allNodeDetails =
      srcData.features.flatMap(f => {
        val scenarios = f.nodeDetails
        scenarios.flatMap(scenario => scenario.flattenTree())
      })


    val methodNodes = getJSTreeCallHierarchyForStepImpls( allNodeDetails)

    val substepDefNodes = getJSTreeCallHierarchyForSubstepDefs( allNodeDetails)


    withWriter(usageTreeDataFile, writer => {
      writer.append("var stepImplUsageTreeData=")

      implicit val formats = Serialization.formats(NoTypeHints)

      writer.append(writePretty(methodNodes))
      writer.append(";\nvar substepDefUsageTreeData=")

      writer.append(writePretty(substepDefNodes))

      writer.append(";")

    })
  }

  def getJSTreeCallHierarchyForSubstepDefs(allNodeDetails : List[NodeDetail]): Iterable[JsTreeNode] = {

    val substepNodeDetails = allNodeDetails.filter(nd => nd.nodeType == "SubstepNode")
    val substepDefsByUniqueMethood = substepNodeDetails.groupBy(_.source.get)

    var nextId = allNodeDetails.map(n => n.id) match {
      case Nil => 1
      case ids => ids.max + 1
    }


    substepDefsByUniqueMethood.map(e => {

      val substepDef = e._1

      // child nodes for each usage
      val substepJsTreeNodes =

      e._2.map(substep => {

        val callHierarchy = getCallHierarchy(substep, allNodeDetails)

        var lastChildOption: Option[List[JsTreeNode]] = None

        callHierarchy.reverse.foreach(n => {

          val last = JsTreeNode(ReportBuilder.uniqueId(n.id), n.description, n.nodeType + " " + n.result, lastChildOption, State(false)) // might want to have more info - failures for example

          lastChildOption = Some(List(last))

        })
        lastChildOption.get.head.copy(li_attr = Some(Map("data-substep-def-call" -> substep.description)))
      })


      // calculate the pass / fail / not run %

      val total = e._2.size

      val failures = e._2.count(n => n.result =="FAILED" || n.result =="CHILD_FAILED")

      val passes = e._2.count(n => n.result =="PASSED")

      val notRun = e._2.count(n => n.result =="NOT_RUN")

      val failPC = Counters.pc(failures, total)
      val passPC = Counters.pc(passes, total)
      val notRunPC = Counters.pc(notRun, total)


      JsTreeNode(ReportBuilder.uniqueId(nextId), StringEscapeUtils.ESCAPE_HTML4.translate(substepDef), "SubstepDefinition", Some(substepJsTreeNodes), State(true),
        Some(Map("data-substep-def" -> "true", "data-substepdef-passpc" -> s"${passPC}",
        "data-substepdef-failpc" -> s"${failPC}", "data-substepdef-notrunpc" -> s"${notRunPC}")))
    })
  }


  def getJSTreeCallHierarchyForStepImpls(allNodeDetails : List[NodeDetail]): Iterable[JsTreeNode] = {

    val stepImplNodeDetails = allNodeDetails.filter(nd => nd.nodeType == "Step")

    val stepImplsbyUniqueMethood = stepImplNodeDetails.groupBy(_.method.get)


    var nextId = allNodeDetails.map(n => n.id) match {
      case Nil => 1
      case ids => ids.max + 1
    }


    val stepImplNodeDetailsToUsages =
    stepImplsbyUniqueMethood.toList.map(e => {
      // convert the method reference to an examplar instance of such a node detail

      (allNodeDetails.filter(n => n.method.contains(e._1)),  e._2)

    }).sortBy(_._1.head.source.get)



    stepImplNodeDetailsToUsages.map(e => {

      val exemplarNodeDetail = e._1.head

      val nodeIds =
        e._1.map(n => {
          n.id
        })

      // child nodes for each usage
      val stepImplJsTreeNodes =

      e._2.map(stepImpl => {

        val callHierarchy = getCallHierarchy(stepImpl, allNodeDetails)

        var lastChildOption: Option[List[JsTreeNode]] = None

        callHierarchy.reverse.foreach(n => {

          val last = JsTreeNode(ReportBuilder.uniqueId(n.id), n.description, n.nodeType + " " + n.result, lastChildOption, State(false)) // might want to have more info - failures for example

          lastChildOption = Some(List(last))

        })
        lastChildOption.get.head
      })

      // calculate the pass / fail / not run %

      val total = e._2.size

      val failures = e._2.count(n => n.result =="FAILED" || n.result =="CHILD_FAILED")

      val passes = e._2.count(n => n.result =="PASSED")

      val notRun = e._2.count(n => n.result =="NOT_RUN")

      val failPC = Counters.pc(failures, total)
      val passPC = Counters.pc(passes, total)
      val notRunPC = Counters.pc(notRun, total)


      // this jstree node represents the method
      val jstreeNode = JsTreeNode(ReportBuilder.uniqueId(nextId),
        StringEscapeUtils.ESCAPE_HTML4.translate(exemplarNodeDetail.source.get), "method", Some(stepImplJsTreeNodes), State(true),
        Some(Map("data-stepimpl-method" -> s"${exemplarNodeDetail.method.get}", "data-stepimpl-passpc" -> s"${passPC}",
          "data-stepimpl-failpc" -> s"${failPC}", "data-stepimpl-notrunpc" -> s"${notRunPC}", "data-stepimpl-node-ids" -> nodeIds.mkString(","))))

      nextId = nextId + 1

      jstreeNode

    })
  }



  def copyStaticResources()(implicit reportDir : File) = {

    val defaultBuilder = new DefaultExecutionReportBuilder

    defaultBuilder.copyStaticResources(reportDir)
  }

  def createFile(name : String)(implicit reportDir : File) = {
    val f = new File(reportDir, name)
    f.createNewFile()
    f
  }

  def writeResultSummary(resultsFile: RootNodeSummary)(implicit reportDir : File) = {

    val file = createFile("results-summary.js")

    withWriter(file, writer => {
      writer.append("var resultsSummary=")

      val map = Map("description" -> resultsFile.description,
      "timestamp" -> resultsFile.timestamp,
      "result" -> resultsFile.result,
      "environment" -> resultsFile.environment,
      "nonFatalTags" -> resultsFile.nonFatalTags,
        "tags" -> resultsFile.tags)

      implicit val formats = Serialization.formats(NoTypeHints)

      writer.append(writePretty(map)).append(";")

    })

  }

  def readModels(rootDir : File)(implicit reportDir : File) : List[SourceDataModel] = {

    val dirFiles = rootDir.listFiles().toList
    val dirFileNames = dirFiles.map(f => f.getName)

    // is there are results.json in there ?
    if (dirFileNames.contains("results.json")){
        List(readModel(rootDir))
    }
    else {
      // go deeper
      dirFiles.flatMap(f => readModels(f))
    }
  }

  def readModel(srcDir : File)(implicit reportDir : File) : SourceDataModel = {

    implicit val formats = Serialization.formats(NoTypeHints)

    val resultsFileOption: Option[RootNodeSummary] = {

      val files = srcDir.listFiles().toList
      files.find(f => f.getName == "results.json").map(resultsFile => {
        read[RootNodeSummary](Files.toString(resultsFile, Charset.defaultCharset()))
      })
    }
    val featureSummaries =

      resultsFileOption match {
      case Some(resultsFile) => {

        writeResultSummary(resultsFile)

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

    val featureDetailList =
    featureSummaries.map(fs => new FeatureDetails(fs._1, fs._2))

    new SourceDataModel(resultsFileOption.get, featureDetailList)
    //(resultsFileOption.get, featureSummaries)

  }

  def loadFeatureData(srcDir : File) = {

    implicit val formats = Serialization.formats(NoTypeHints)

    srcDir.listFiles().toList.find(f => f.getName == srcDir.getName + ".json").map(featureResultsFile =>{
      read[FeatureSummary](Files.toString(featureResultsFile, Charset.defaultCharset()))
    })

  }


  def createTreeData2(file : File, srcData : SourceDataModel) = { //(RootNodeSummary, List[(FeatureSummary, List[NodeDetail])])) = {

    val rootChildren =
    srcData.featuresList.map(features => {
      val children =
        features.map(feature => {
          val featureScenarios = feature.nodeDetails.map(n => buildForJsonTreeNode(n))

          val childrenOption = if (featureScenarios.isEmpty) None else Some(featureScenarios)

          // create a node for the feature:
          // TODO - only taking the head element

          val icon = ReportBuilder.iconFor(feature.summary.result) //srcData.rootNodeSummary.head.result)

          val state = State.forResult(feature.summary.result)

          JsTreeNode(feature.summary.id.toString, feature.summary.description, icon, childrenOption, state)

        })


      val childrenOption = if (children.isEmpty) None else Some(children)

      // TODO - only taking the head element

      val icon = ReportBuilder.iconFor(srcData.rootNodeSummary.head.result)

      val state = State.forResult(srcData.rootNodeSummary.head.result)

      val rootNode = JsTreeNode(srcData.rootNodeSummary.head.id.toString, srcData.rootNodeSummary.head.description, icon, childrenOption, state)
      rootNode
    })


    val ooberRootNode =
      if (rootChildren.size > 1){


        // TODO - determine what the root icon should be...

        val icon = ReportBuilder.iconFor(srcData.rootNodeSummary.head.result)

        val state = State.forResult(srcData.rootNodeSummary.head.result)

      JsTreeNode(srcData.rootNodeSummary.head.id.toString, srcData.rootNodeSummary.head.description, icon, Some(ooberRootNode), state)

      }
      else {
        rootChildren.head
      }



    withWriter(file, writer =>{
      writer.append("var treeData = ")

      implicit val formats = Serialization.formats(NoTypeHints)

      writer.append(writePretty(ooberRootNode))
    })

  }


  def withWriter(file: File, op: BufferedWriter => Any) = {
    val writer = Files.newWriter(file, Charset.defaultCharset)
    op(writer)
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




  def createDetailData(file : File, srcDataList : List[SourceDataModel]) = {

    withWriter(file, writer => {

      writer.append("var detail = new Array();\n")

      srcDataList.foreach(srcData => {

        val featureSummaries = srcData.featuresList.map(f => f.summary) //srcData._2.map(_._1)

        srcData.featuresList.foreach(feature =>{


          val nodeDetailList = feature.nodeDetails //features.flatMap(f => f.nodeDetails)//  srcData._2.flatMap(_._2)

          writeRootNode(writer, srcData.rootNodeSummary, featureSummaries)

          featureSummaries.foreach(f => {
            writeFeatureNode(writer, f, nodeDetailList)
          })
        })

      })

    })
  }


  def writeFeatureNode(writer: BufferedWriter, f: FeatureSummary, nodeDetailList: List[NodeDetail]) = {

    writer.append(s"""detail[${f.id}]={"nodetype":"FeatureNode","filename":"${f.filename}","result":"${f.result}","id":${f.id},
       |"runningDurationMillis":${f.executionDurationMillis.getOrElse(-1)},"runningDurationString":"${f.executionDurationMillis.getOrElse(-1)} milliseconds",
       |"description":"${StringEscapeUtils.escapeEcmaScript(f.description)}",
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
    |"runningDurationString":"${nodeDetail.executionDurationMillis.getOrElse("n/a")} milliseconds","description":"${StringEscapeUtils.escapeEcmaScript(nodeDetail.description)}",""".stripMargin.replaceAll("\n", ""))

    nodeDetail.method.map(s => writer.append(s""""method":"${StringEscapeUtils.escapeEcmaScript(s)}",""") )

    writer.append(s""""lineNum":"${nodeDetail.lineNumber}",""")

    nodeDetail.exceptionMessage.map(s => writer.append(s""""emessage":"${StringEscapeUtils.escapeEcmaScript(s)}",""") )


    nodeDetail.screenshot.map(s => writer.append(s"""screenshot:"data${s}",""") )
    nodeDetail.stackTrace.map(s => writer.append(s"""stacktrace:[${s.mkString("\"", "\",\n\"", "\"")}],"""))

    writer.append(s""""children":[""")

    writer.append(nodeDetail.children.map(n => {
      s"""{"result":"${n.result}","description":"${StringEscapeUtils.escapeEcmaScript(n.description)}"}"""
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

//    rootNodeSummaries.foreach(rootNodeSummary => {

      writer.append(s"""detail[${rootNodeSummary.id}]={"nodetype":"RootNode","filename":"","result":"${rootNodeSummary.result}","id":${rootNodeSummary.id},"runningDurationMillis":${rootNodeSummary.executionDurationMillis.get},"runningDurationString":"${rootNodeSummary.executionDurationMillis.get} milliseconds","children":[""")

      writer.append(
        rootNodeSummary.features.map(f => {

          val fs = featureSummaries.find(fs => fs.id == f.nodeId).get

          s"""{"result":"${f.result}","description":"${StringEscapeUtils.escapeEcmaScript(fs.description)}"}"""
        }).mkString(","))

      writer.append("]};\n")

//    })
  }
}
