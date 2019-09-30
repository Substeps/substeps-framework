package org.substeps.report

import java.io.File
import java.net.URI
import java.nio.charset.Charset
import java.nio.file.Paths
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

import com.google.common.io.Files
import com.technophobia.substeps.execution.{ExecutionNodeResult, ExecutionResult}
import com.technophobia.substeps.execution.node._
import com.technophobia.substeps.model.exception.SubstepsRuntimeException
import com.technophobia.substeps.runner.IExecutionListener
import com.typesafe.config.Config
import org.slf4j.{Logger, LoggerFactory}

import scala.collection.JavaConversions._
import scala.collection.JavaConverters._
import org.json4s._
import org.json4s.native.Serialization
import org.json4s.native.Serialization.{write, writePretty}
import org.substeps.config.SubstepsConfigLoader
import org.substeps.runner.NewSubstepsExecutionConfig

import scala.collection.mutable

/**
  * Created by ian on 05/05/16.
  */
object ExecutionResultsCollector{

  val UTF8 = Charset.forName("UTF-8")

  // TODO - only called in tests
  def writeMasterConfig(masterConfig: Config) = {

    val rootDataDir: File = NewSubstepsExecutionConfig.getRootDataDir(masterConfig)
    val outFile = new File(rootDataDir, "masterConfig.conf")

    mkdirOrException(rootDataDir)

    Files.write(SubstepsConfigLoader
      .render(masterConfig), outFile, UTF8)
  }

  def mkdirOrException(dir : File) = {
    if (!dir.exists()){

      if (!dir.mkdirs()){
        throw new SubstepsRuntimeException("Failed to create dir: " + dir.getAbsolutePath)
      }
    }

  }





}


class ExecutionResultsCollector extends  IExecutionResultsCollector {

  import ExecutionResultsCollector._

  @transient
  private lazy val log: Logger = LoggerFactory.getLogger(classOf[ExecutionResultsCollector])

  var dataDir: File = new File(".")
  var pretty : Boolean = false

  def getDataDir = dataDir



  def setDataDir(dir : File) = this.dataDir = dir
  def setPretty(pretty : Boolean) = this.pretty = pretty


  var featureToResultsDirMap: Map[Long, File] = Map()

  val scenarioSummaryMap = new mutable.HashMap[Long, (BasicScenarioNode, File)]

  def onNodeFailed(node: IExecutionNode, cause: Throwable): Unit = {

    log.trace("nodeFailed: " + node.getId)



    node match {
      case scenarioNode :  BasicScenarioNode => {
        log.debug(s"basic scenario id ${scenarioNode.getId} failed")

        val feature = getFeatureFromNode(scenarioNode)

        featureToResultsDirMap.get(feature.getId) match {
          case None => {
            log.error("scenario node failed - no report dir for feature: " + feature.getFilename + " id: " + feature.getId)

          }
          case Some(dir) => {

            val name: String = getScenarioName(node, scenarioNode)

            // write out a results file for this scenario
            val resultsFile = new File(dir, name + "_results.json")
            val screenshotsDir = new File(dir, name)

            scenarioSummaryMap += scenarioNode.getId -> (scenarioNode, resultsFile)

            Files.write(generateJson(scenarioNode, screenshotsDir), resultsFile, UTF8)
          }

        }

      }
      case featureNode: FeatureNode => {
        log.debug("feature node failed")

        // write a summary file for the feature
        featureToResultsDirMap.get(featureNode.getId) match {
          case None => {
            log.error("feature node failed - no report dir for feature: " + featureNode.getFilename+ " id: " + featureNode.getId)
          }
          case Some(dir) => {
            val summaryFile = new File(dir, dir.getName + ".json")

            Files.write(generateJson(featureNode), summaryFile, UTF8)
          }
        }
      }
      case rootNode : RootNode => {
        log.debug("root node failed")
        val summaryFile = new File(dataDir, "results.json")

        Files.write(generateJson(rootNode), summaryFile, UTF8)
      }

      case n => {

        // outlinescenariorownode
        log.warn("other node failed " + node.getClass  + " id: " + n.getId)
      }
    }

  }

  def onNodeStarted(node: IExecutionNode): Unit = {

    // do we care about nodes starting ?
    log.trace("nodeStarted: " + node.getId)
  }

  def getFeatureFromNode(node: IExecutionNode) : FeatureNode = {

    log.trace("getFeatureFromNode: " + node.getClass)

    node.getParent match {
      case f : FeatureNode => f
      case _ =>  getFeatureFromNode(node.getParent)
    }
  }


  def onNodeFinished(node: IExecutionNode): Unit = {

    log.trace("nodeFinished: " + node.getId)


    node match {
      case scenarioNode :  BasicScenarioNode => {
        log.trace(s"basic scenario id ${scenarioNode.getId} finished")
        val feature = getFeatureFromNode(scenarioNode)

        featureToResultsDirMap.get(feature.getId) match {
          case None => {
            log.error("basic scenario node finished - no report dir for feature: " + feature.getFilename + " id: " + feature.getId)
          }
          case Some(dir) => {

            val name: String = getScenarioName(node, scenarioNode)

            // write out a results file for this scenario
            val resultsFile = new File(dir, name + "_results.json")

            val screenshotsDir = new File(dir, name)

            scenarioSummaryMap += scenarioNode.getId -> (scenarioNode, resultsFile)

            Files.write(generateJson(scenarioNode, screenshotsDir), resultsFile, UTF8)
          }

        }
      }
      case featureNode: FeatureNode => {
        log.trace("feature node finished")

        // write a summary file for the feature
        featureToResultsDirMap.get(featureNode.getId) match {
          case None => {
            log.error("feature node finished - no report dir for feature: " + featureNode.getFilename + " id: " + featureNode.getId)
          }
          case Some(dir) => {
            val summaryFile = new File(dir, dir.getName + ".json")

            Files.write(generateJson(featureNode), summaryFile, UTF8)

          }
        }
      }
      case rootNode : RootNode => {
        log.trace("root node finished")
        val summaryFile = new File(dataDir, "results.json")

        Files.write(generateJson(rootNode), summaryFile, UTF8)

      }
      case stepImplNode : StepImplementationNode => {
        log.trace(s"stepImpl ${stepImplNode.getId} Node finished")

      }
      case n => log.trace(s"other node ${n.getId} finished")
    }

  }

  private def getScenarioName(node: IExecutionNode, scenarioNode: BasicScenarioNode) = {
    val idx =
      node.getParent match {
        case row: OutlineScenarioRowNode => row.getParent.asInstanceOf[OutlineScenarioNode].getChildren.indexOf(row)
        case featureNode : FeatureNode => featureNode.getChildren.indexOf(node)
        case _ => 0
      }
    val name = scenarioNode.getScenarioName.replaceAllLiterally(" ", "_") + "_" + idx
    name
  }

  def onNodeIgnored(node: IExecutionNode): Unit = {

    log.trace("nodeIgnored: " + node.getId)

    node match {
      case scenarioNode :  BasicScenarioNode => {

      }
      case _ => log.trace("other node ignored")
    }


  }

  def generateJson(rootNode : RootNode) = {

    val features =
      rootNode.getChildren.asScala.map(f => {

        val featureResultsDir =
          featureToResultsDirMap.get(f.getId) match {
            case None => {
              log.error("generateJson for RootNode - no report dir for feature: " + f.getFilename)
              ""
            }
            case Some(dir) => dir.getName
          }

        FeatureSummaryForRootNode(f.getId, featureResultsDir, f.getResult.getResult.toString)

      })


    val data =
      RootNodeSummary("RootNode", rootNode.getDescription, rootNode.getResult.getResult.name(), rootNode.getId,
        Some(rootNode.getResult.getRunningDuration), features.toList, Option(rootNode.getTags), Option(rootNode.getNonFatalTags), rootNode.getTimestamp, rootNode.getEnvironment)

    implicit val formats = Serialization.formats(NoTypeHints)

    if(pretty) writePretty(data) else write(data)

  }

  def generateJson(featureNode: FeatureNode) = {

    val scenarios =
    featureNode.getChildren.asScala.flatMap(childNode => {

      childNode match {
        case basicScenarioNode : BasicScenarioNode => {

          scenarioSummaryMap.get(basicScenarioNode.getId) match {
            case Some((sNode, resultsFile)) => {

              List(ScenarioSummary(sNode.getId, resultsFile.getName, sNode.getResult.getResult.toString, basicScenarioNode.getTags.toList))

            }
            case None => {
              log.error("failed to find scenario summary for id: " + basicScenarioNode.getId)
              List()
            }
          }


        }
        case outline : OutlineScenarioNode  => {

          val basicScenarioNodes =
          outline.getChildren.asScala.flatMap(outlineScenarioRow =>{
            outlineScenarioRow.getChildren
          })


          basicScenarioNodes.map(outlineScenario => {

            println("failing outlineScenario.getId: " + outlineScenario.getId)

            val (sNode, resultsFile) = scenarioSummaryMap.get(outlineScenario.getId).get

            ScenarioSummary(sNode.getId, resultsFile.getName, sNode.getResult.getResult.toString, sNode.getTags.toList)

          })
        }
        case other => {
          log.error("had another child type: " + other.getClass + " other id: " + other.getId)
          List()
        }
      }


    })

    val result = if (Option(featureNode.getResult.getFailure).isDefined && featureNode.getResult.getFailure.isNonCritical) "NON_CRITICAL_FAILURE" else featureNode.getResult.getResult.name()

    val data =
      FeatureSummary("FeatureNode", featureNode.getFilename, result, featureNode.getId,
        Some(featureNode.getResult.getRunningDuration), featureNode.getDescription, scenarios.toList, featureNode.getTags.toList)


    implicit val formats = Serialization.formats(NoTypeHints)

    if(pretty) writePretty(data) else write(data)

  }


  def generateJson(scenarioNode : BasicScenarioNode, screenshotsDir : File) = {

  val result = scenarioNode.getResult

      // may have children
      val children =
       Option(scenarioNode.getChildren) match {
          case Some(childNodes) => {
            childNodes.asScala.map(child => {
              NodeDetail.getData(child, screenshotsDir, dataDir )
            })
          }
          case _ => List()
        }

    val tags = Option(scenarioNode.getTags) match {
      case Some(tags) => Some(tags.toList)
      case _ => None
    }

    Option(result.getFailure).map(sef => {

      log.info(" *** got Substep Exec Failure: critical : " + sef.isNonCritical)

    })


      val data =
      if (!result.getResult.isFailure) {

        NodeDetail.basicScenarioNodeNotInError(scenarioNode, children.toList, tags)

      }
      else {

        if (result.getResult == ExecutionResult.CHILD_FAILED){

          NodeDetail.basicScenarioNodeNotInError(scenarioNode, children.toList, tags)

        }
        else {

          NodeDetail.basicScenarioNodeInError(scenarioNode, children.toList, tags)

        }

      }

      implicit val formats = Serialization.formats(NoTypeHints)

      if(pretty) writePretty(data) else write(data)

  }



  def initOutputDirectories(rootNode: RootNode) {

    mkdirOrException(dataDir)

    log.info("collecting data into " + dataDir.getAbsolutePath)

    // create subdirs for each feature

    val featureNodes = rootNode.getChildren.asScala

    log.trace("init dirs for " + featureNodes.size + " features")

    val featureNames =
      featureNodes.map(featureNode => featureNode.getFilename)

    val dupes =
      if (featureNames.distinct.size <= featureNames.size){
      // take into account the same feature files in different dirs - featureNode.getFileUri

      val uniqueFeatureNamesMap: Map[String, mutable.Buffer[String]] = featureNames.groupBy(identity)

      uniqueFeatureNamesMap.filter(kv => kv._2.size > 1)
    }
      else Map()


      featureToResultsDirMap =
    featureNodes.map(featureNode => {


      val featureResultsDir =
        if (dupes.contains(featureNode.getFilename)){

          val fullPath = Paths.get(new URI(featureNode.getFileUri))

          val path = fullPath.subpath(fullPath.getNameCount -2, fullPath.getNameCount)

          new File(dataDir, path.toString.replace(File.pathSeparator, "_") + ".results")

      }
      else {
        new File(dataDir, featureNode.getFilename + ".results")

      }

      featureResultsDir.mkdir()

      log.trace("mapping feature node id " + featureNode.getId + " to dir: "  + featureResultsDir.getAbsolutePath)

      featureNode.getId -> featureResultsDir
    }).toMap
  }



}



case class FeatureSummary(nodeType: String, filename: String, result : String, id : Long,
                          executionDurationMillis : Option[Long], description : String, scenarios: List[ScenarioSummary], tags : List[String])

object FeatureSummary {
  def sequenceIds(summary: FeatureSummary, toAdd: Long): FeatureSummary = {
    summary.copy(id = summary.id + toAdd, scenarios = ScenarioSummary.sequenceIds(summary.scenarios, toAdd))
  }
}

case class ScenarioSummary(nodeId : Long, filename : String, result: String, tags : List[String])

object ScenarioSummary {
  def sequenceIds(scenarios: List[ScenarioSummary], toAdd : Long): scala.List[ScenarioSummary] = {

    scenarios.map(s => {
      s.copy(nodeId = s.nodeId + toAdd)
    })
  }
}


case class FeatureSummaryForRootNode(nodeId : Long, resultsDir: String, result : String)

object FeatureSummaryForRootNode {
  def sequenceIds(features: List[FeatureSummaryForRootNode], toAdd: Long): scala.List[FeatureSummaryForRootNode] = {

    features.map(f => {
      f.copy(nodeId = f.nodeId + toAdd)
    })
  }
}

case class RootNodeSummary(nodeType: String, description: String, result : String, id : Long,
                           executionDurationMillis : Option[Long], features : List[FeatureSummaryForRootNode], tags : Option[String], nonFatalTags : Option[String], timestamp : Long, environment : String)

object RootNodeSummary {
  def sequenceIds(rootNodeSummary: RootNodeSummary, toAdd: Long): RootNodeSummary = {

    rootNodeSummary.copy(id = rootNodeSummary.id + toAdd, features = FeatureSummaryForRootNode.sequenceIds(rootNodeSummary.features, toAdd))
  }
}


case class SubstepsNode(id : Long, nodeType: String, description: String, children : List[SubstepsNode])

// TODO - build a new node up, look at the akkaruner2. build up a new hierarchy
// utils class to build up new model - could use the same model for running with, convert the runner, listener and notifier... more big changes

//object SubstepsNode {
//
//  def toSubstepNodeTree(rootNode: NodeWithChildren, nodeType = "RootNode") : SubstepsNode= {
//
//    val children = rootNode.getChildren
//
//    val substepNodeChildren =
//      if (children == null) List() else {
//
//        val childList = children.asScala.toList
//        childList.map(c => {
//          toSubstepNodeTree(c)
//        })
//      }
//    SubstepsNode(rootNode.getId, nodeType, rootNode.getDescription, substepNodeChildren)
//  }
//
//  def toSubstepNodeTree(featureNode: FeatureNode) : SubstepsNode = {
//    ???
//  }
//
//}



import scala.collection.JavaConverters._



case class Id(id: String)
case class Data(title:String, attr: Id, icon : String, children: Option[List[Data]])

case object Data{

  val resultsToIcons = Map(
    ExecutionResult.PASSED -> "img/PASSED.png",
    ExecutionResult.NOT_RUN -> "img/NOT_RUN.png",
    ExecutionResult.PARSE_FAILURE -> "img/PARSE_FAILURE.png",
    ExecutionResult.FAILED -> "img/FAILED.png"
  )



  def getIcon(result : ExecutionNodeResult) = {
    resultsToIcons.getOrElse(result.getResult, "unknown")

  }

  def getData(stepNode: StepNode) : Data = {
    stepNode match {
      case stepImpl : StepImplementationNode => {

        Data(stepImpl.getDescription, Id(stepImpl.getId.toString), getIcon(stepImpl.getResult), None)
      }
      case substepNode : SubstepNode => {

        // may have children
        val children =
        substepNode.getChildren.asScala.map(child => {
          getData(child)
        })

        Data(substepNode.getDescription, Id(substepNode.getId.toString), getIcon(substepNode.getResult), Some(children.toList))
      }
    }

  }
}
