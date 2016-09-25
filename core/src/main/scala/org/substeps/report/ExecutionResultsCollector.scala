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
import org.slf4j.{Logger, LoggerFactory}

import scala.collection.JavaConversions._
import scala.collection.JavaConverters._
import org.json4s._
import org.json4s.native.Serialization
import org.json4s.native.Serialization.{write, writePretty}

import scala.collection.mutable

/**
  * Created by ian on 05/05/16.
  */
object ExecutionResultsCollector{
  def getBaseDir(rootDir : File) = {
    new File( rootDir, "substeps-results_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("YYYYMMdd_HHmm_ss_SSS")))
  }
}


// TODO - remove the ctor params to allow injection from Maven via setter... eurgh  builder instead ?  when to init ?
class ExecutionResultsCollector extends  IExecutionResultsCollector {

  private val log: Logger = LoggerFactory.getLogger(classOf[ExecutionResultsCollector])

//  val rootDir = new File(baseDir, "substeps-results_" +timestamp)

  var dataDir: File = new File(".")
  var pretty : Boolean = false

  def getDataDir = dataDir



  def setDataDir(dir : File) = this.dataDir = dir
  def setPretty(pretty : Boolean) = this.pretty = pretty

  @transient
  lazy val UTF8 = Charset.forName("UTF-8")

  var featureToResultsDirMap: Map[FeatureNode, File] = Map()

  val scenarioSummaryMap = new mutable.HashMap[Long, (BasicScenarioNode, File)]

  def onNodeFailed(node: IExecutionNode, cause: Throwable): Unit = {

    node match {
      case scenarioNode :  BasicScenarioNode => {
        log.debug("basic scenario failed")

        val feature = getFeatureFromNode(scenarioNode)

        featureToResultsDirMap.get(feature) match {
          case None => log.error("no report dir for feature: " + feature.getFilename)
          case Some(dir) => {

            // write out a results file for this scenario
            val resultsFile = new File(dir, scenarioNode.getScenarioName.replaceAllLiterally(" ", "_") + "_results.json")
            val screenshotsDir = new File(dir, scenarioNode.getScenarioName.replaceAllLiterally(" ", "_"))

            scenarioSummaryMap += scenarioNode.getId -> (scenarioNode, resultsFile)

            Files.write(generateJson(scenarioNode, screenshotsDir), resultsFile, UTF8)
          }

        }

      }
      case featureNode: FeatureNode => {
        log.debug("feature node failed")

        // write a summary file for the feature
        featureToResultsDirMap.get(featureNode) match {
          case None => log.error("no report dir for feature: " + featureNode.getFilename)
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

      case _ => log.debug("other node failed")
    }

  }

  def onNodeStarted(node: IExecutionNode): Unit = {

    // do we care about nodes starting ?

  }

  def getFeatureFromNode(node: IExecutionNode) : FeatureNode = {

    log.debug("getFeatureFromNode: " + node.getClass)

    node.getParent match {
      case f : FeatureNode => f
      case _ =>  getFeatureFromNode(node.getParent)
    }
  }


  def onNodeFinished(node: IExecutionNode): Unit = {

    node match {
      case scenarioNode :  BasicScenarioNode => {
        log.debug("basic scenario finished")
        val feature = getFeatureFromNode(scenarioNode)

        featureToResultsDirMap.get(feature) match {
          case None => log.error("no report dir for feature: " + feature.getFilename)
          case Some(dir) => {

            // write out a results file for this scenario
            val resultsFile = new File(dir, scenarioNode.getScenarioName.replaceAllLiterally(" ", "_") + "_results.json")

            val screenshotsDir = new File(dir, scenarioNode.getScenarioName.replaceAllLiterally(" ", "_"))

            scenarioSummaryMap += scenarioNode.getId -> (scenarioNode, resultsFile)

            Files.write(generateJson(scenarioNode, screenshotsDir), resultsFile, UTF8)
          }

        }
      }
      case featureNode: FeatureNode => {
        log.debug("feature node finished")

        // write a summary file for the feature
        featureToResultsDirMap.get(featureNode) match {
          case None => log.error("no report dir for feature: " + featureNode.getFilename)
          case Some(dir) => {
            val summaryFile = new File(dir, dir.getName + ".json")

            Files.write(generateJson(featureNode), summaryFile, UTF8)

          }
        }
      }
      case rootNode : RootNode => {
        log.debug("root node finished")
        val summaryFile = new File(dataDir, "results.json")

        Files.write(generateJson(rootNode), summaryFile, UTF8)

      }
      case _ => log.debug("other node finished")
    }

  }

  def onNodeIgnored(node: IExecutionNode): Unit = {

    node match {
      case scenarioNode :  BasicScenarioNode => {

      }
      case _ => log.debug("other node ignored")
    }


  }

  def generateJson(rootNode : RootNode) = {

    val features =
      rootNode.getChildren.asScala.map(f => {

        val featureResultsDir =
          featureToResultsDirMap.get(f) match {
            case None => {
              log.error("no report dir for feature: " + f.getFilename)
              ""
            }
            case Some(dir) => dir.getName
          }

        FeatureSummaryForRootNode(f.getId, featureResultsDir, f.getResult.getResult.toString)

      })

    val data =
      RootNodeSummary("RootNode", rootNode.getDescription, rootNode.getResult.getResult.name(), rootNode.getId,
        Some(rootNode.getResult.getRunningDuration), features.toList)

    implicit val formats = Serialization.formats(NoTypeHints)

    if(pretty) writePretty(data) else write(data)

  }

  def generateJson(featureNode: FeatureNode) = {

    val scenarios =
    featureNode.getChildren.asScala.flatMap(childNode => {

      childNode match {
        case basicScenarioNode : BasicScenarioNode => {
          val (sNode, resultsFile) = scenarioSummaryMap.get(basicScenarioNode.getId).get

          List(ScenarioSummary(sNode.getId, resultsFile.getName, sNode.getResult.getResult.toString, basicScenarioNode.getTags.toList))

        }
        case outline : OutlineScenarioNode  => {

          val basicScenarioNodes =
          outline.getChildren.asScala.flatMap(outlineScenarioRow =>{
            outlineScenarioRow.getChildren
          })


          basicScenarioNodes.map(outlineScenario => {
            val (sNode, resultsFile) = scenarioSummaryMap.get(outlineScenario.getId).get

            ScenarioSummary(sNode.getId, resultsFile.getName, sNode.getResult.getResult.toString, sNode.getTags.toList)

          })
        }
        case other => {
          log.error("had another child type: " + other.getClass)
          List()
        }
      }


    })

    val data =
      FeatureSummary("FeatureNode", featureNode.getFilename, featureNode.getResult.getResult.name(), featureNode.getId,
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
              NodeDetail.getData(child, screenshotsDir )
            })
          }
          case _ => List()
        }

    val tags = Option(scenarioNode.getTags) match {
      case Some(tags) => Some(tags.toList)
      case _ => None
    }



      val data =
      if (!result.getResult.isFailure) {

        NodeDetail.withChildrenNodeDetail("BasicScenarioNode", scenarioNode.getParent.getFilename, scenarioNode.getResult.getResult.name(), scenarioNode.getId.toInt,
          Option(result.getRunningDuration), scenarioNode.getDescription, None, children.toList, tags )
      }
      else {

        if (result.getResult == ExecutionResult.CHILD_FAILED){

          NodeDetail.withChildrenNodeDetail("BasicScenarioNode", scenarioNode.getParent.getFilename, scenarioNode.getResult.getResult.name(), scenarioNode.getId.toInt,
            Option(result.getRunningDuration), scenarioNode.getDescription, None, children.toList, tags )

        }
        else {

          val stackTrace = result.getFailure.getCause.getStackTrace.toList.map(elem => elem.toString)

          NodeDetail.inErrorwithChildrenNodeDetail("BasicScenarioNode", scenarioNode.getParent.getFilename, scenarioNode.getResult.getResult.name(), scenarioNode.getId.toInt,
            Option(result.getRunningDuration), scenarioNode.getDescription, None, children.toList,
            Some(result.getFailure.getCause.getMessage), Some(stackTrace), None, tags)
        }

      }

      implicit val formats = Serialization.formats(NoTypeHints)

      if(pretty) writePretty(data) else write(data)

  }



  def initOutputDirectories(rootNode: RootNode) {

    if (!dataDir.exists()){
      if (!dataDir.mkdir()){
        throw new SubstepsRuntimeException("Failed to create root execution results dir")
      }
    }

    log.debug("collecting data into " + dataDir.getAbsolutePath)


    // create subdirs for each feature

    val featureNodes = rootNode.getChildren.asScala

    val featureNames =
    featureNodes.map(featureNode => featureNode.getFilename)

    val dupes =
      if (featureNames.distinct.size < featureNames.size){
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

      featureNode -> featureResultsDir
    }).toMap
  }
}

case class FeatureSummary(nodeType: String, filename: String, result : String, id : Long,
                          executionDurationMillis : Option[Long], description : String, scenarios: List[ScenarioSummary], tags : List[String])

case class ScenarioSummary(nodeId : Long, filename : String, result: String, tags : List[String])


case class FeatureSummaryForRootNode(nodeId : Long, resultsDir: String, result : String)

case class RootNodeSummary(nodeType: String, description: String, result : String, id : Long,
                           executionDurationMillis : Option[Long], features : List[FeatureSummaryForRootNode])


import scala.collection.JavaConverters._


case class NodeDetail(nodeType: String, filename: String, result : String, id : Long,
                      executionDurationMillis : Option[Long], description : String,  method : Option[String],
                      children : List[NodeDetail] = List(),
                      exceptionMessage : Option[String] = None, stackTrace : Option[List[String]] = None,
                      screenshot : Option[String] = None, tags : Option[List[String]] = None) {

  def flattenTree() : List[NodeDetail] = {
    NodeDetail.flatten(this)
  }
}



case object NodeDetail {

  def basicLeafNodeDetail(nodeType: String, filename: String, result : String, id : Long,
                          executionDurationMillis : Option[Long], description : String,  method : Option[String]) : NodeDetail = {
    // tags not important
    NodeDetail(nodeType, filename, result, id, executionDurationMillis, description, method)
  }

  def withChildrenNodeDetail(nodeType: String, filename: String, result : String, id : Long,
                             executionDurationMillis : Option[Long], description : String,  method : Option[String], children : List[NodeDetail], tags : Option[List[String]] = None) : NodeDetail = {
    // might have tags
    NodeDetail(nodeType, filename, result, id, executionDurationMillis, description, method, children, None, None, None, tags)
  }



  def inErrorwithChildrenNodeDetail(nodeType: String, filename: String, result : String, id : Long,
                             executionDurationMillis : Option[Long], description : String,  method : Option[String],
                                    children : List[NodeDetail], exceptionMessage : Option[String],
                                    stackTrace : Option[List[String]] , screenshot : Option[String],
                                    tags : Option[List[String]] = None) : NodeDetail = {
    // might have tags

    NodeDetail(nodeType, filename, result, id, executionDurationMillis, description, method, children, exceptionMessage, stackTrace, screenshot, tags)
  }




  implicit def Long2longOption(x: java.lang.Long): Option[Long] = {
    if (Option(x).isDefined) Some(x.longValue) else None
  }

  def flatten(node : NodeDetail) : List[NodeDetail] = {

    val children =
      node.children.flatMap(child => {
        flatten(child)
      })

    List(node) ++ children
  }
  def getData(stepNode: StepNode, screenshotsDir : File) : NodeDetail = {
    stepNode match {
      case stepImpl : StepImplementationNode => {

        val result = stepImpl.getResult

        // no error

        if (!result.getResult.isFailure) {
          NodeDetail("Step", stepImpl.getFilename, stepImpl.getResult.getResult.name(), stepImpl.getId.toInt,
            result.getRunningDuration, stepImpl.getDescription, Some(stepImpl.getTargetMethod.toString ) )

        }
        else {

          val stackTrace = result.getFailure.getCause.getStackTrace.toList.map(elem => elem.toString)

          val screenshotFile =
          if (Option(result.getFailure.getScreenshot).isDefined){

            if(!screenshotsDir.exists()) {
              screenshotsDir.mkdir()
            }
            val screenshotFile = new File(screenshotsDir, scala.util.Random.alphanumeric.take(10).mkString)

            Files.write(result.getFailure.getScreenshot, screenshotFile)

            val path = Paths.get(screenshotsDir.getName, screenshotFile.getName)
            Some(path.toString)
          }
          else {
            None
          }

          NodeDetail("Step", stepImpl.getFilename, stepImpl.getResult.getResult.name(), stepImpl.getId.toInt,
            result.getRunningDuration, stepImpl.getDescription, Some(stepImpl.getTargetMethod.toString), List(),
            Some(result.getFailure.getCause.getMessage), Some(stackTrace), screenshotFile )

        }
      }
      case substepNode : SubstepNode => {

        val result = substepNode.getResult

        // may have children
        val children =
          Option(substepNode.getChildren) match {
            case Some(childNodes) => {
              childNodes.asScala.map(child => {
                NodeDetail.getData(child, screenshotsDir)
              })
            }
            case _ => List()
          }



        if (!result.getResult.isFailure) {
          NodeDetail("SubstepNode", substepNode.getFilename, substepNode.getResult.getResult.name(), substepNode.getId.toInt,
            result.getRunningDuration, substepNode.getDescription, None, children.toList  )

        }
        else {

          if (result.getResult == ExecutionResult.CHILD_FAILED){

            NodeDetail("SubstepNode", substepNode.getFilename, substepNode.getResult.getResult.name(), substepNode.getId.toInt,
              result.getRunningDuration, substepNode.getDescription, None, children.toList)

          }
          else {
            val stackTrace = result.getFailure.getCause.getStackTrace.toList.map(elem => elem.toString)

            NodeDetail("SubstepNode", substepNode.getFilename, substepNode.getResult.getResult.name(), substepNode.getId.toInt,
              result.getRunningDuration, substepNode.getDescription, None, children.toList,
              Some(result.getFailure.getCause.getMessage), Some(stackTrace) )

          }
        }
      }
    }
  }

}

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
