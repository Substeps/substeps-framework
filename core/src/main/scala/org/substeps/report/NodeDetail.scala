package org.substeps.report

import java.io.File
import java.nio.file.Paths

import com.google.common.io.Files
import com.technophobia.substeps.execution.ExecutionResult
import com.technophobia.substeps.execution.node.{BasicScenarioNode, StepImplementationNode, StepNode, SubstepNode}

import scala.collection.JavaConverters._



case class NodeDetail(nodeType: String, filename: String, lineNumber : Int, result : String, id : Long,
                      executionDurationMillis : Option[Long], description : String,  method : Option[String],
                      children : List[NodeDetail] = List(),
                      exceptionMessage : Option[String] , stackTrace : Option[List[String]] ,
                      screenshot : Option[String] , tags : Option[List[String]] , source : Option[String] , sourceParameterNames : Option[List[String]] ) {

  def flattenTree() : List[NodeDetail] = {
    NodeDetail.flatten(this)
  }
}



case object NodeDetail {

  def sequenceIds(nodeDetails: List[NodeDetail], toAdd: Long): scala.List[NodeDetail] = {

    nodeDetails.map(n => {
      n.copy(id = n.id + toAdd, children = sequenceIds(n.children, toAdd))
    })
  }


  def basicScenarioNodeInError(scenarioNode: BasicScenarioNode, children: List[NodeDetail], tags: Option[List[String]]) = {

    val stackTrace = scenarioNode.getResult.getFailure.getCause.getStackTrace.toList.map(elem => elem.toString)

    val result = if (scenarioNode.getResult.getFailure.isNonCritical) "NON_CRITICAL_FAILURE" else scenarioNode.getResult.getResult.name()


    NodeDetail("BasicScenarioNode", scenarioNode.getParent.getFilename, scenarioNode.getLineNumber, result, scenarioNode.getId.toInt,
      Option(scenarioNode.getResult.getRunningDuration), scenarioNode.getDescription, None, children,
      Some(scenarioNode.getResult.getFailure.getCause.getMessage), Some(stackTrace), None, tags, None, None)

  }


  def basicScenarioNodeNotInError(scenarioNode: BasicScenarioNode, children: List[NodeDetail], tags: Option[List[String]]) = {

    NodeDetail("BasicScenarioNode", scenarioNode.getParent.getFilename, scenarioNode.getLineNumber, scenarioNode.getResult.getResult.name(), scenarioNode.getId.toInt,
      Option(scenarioNode.getResult.getRunningDuration), scenarioNode.getDescription, None, children,
      None, None, None, tags , None, None)

  }

  def stepImplInError(stepImpl: StepImplementationNode, screenshotFile : Option[String]) = {

    val stackTrace = stepImpl.getResult.getFailure.getCause.getStackTrace.toList.map(elem => elem.toString)

    val paramNames =
      if (Option(stepImpl.getParameterNames).isDefined)  Some(stepImpl.getParameterNames.asScala.toList) else None


    NodeDetail("Step", stepImpl.getFilename,  stepImpl.getLineNumber, stepImpl.getResult.getResult.name(), stepImpl.getId.toInt,
      stepImpl.getResult.getRunningDuration, stepImpl.getDescription, Some(stepImpl.getTargetMethod.toString), List(),
      Some(stepImpl.getResult.getFailure.getCause.getMessage), Some(stackTrace), screenshotFile, None, Some(stepImpl.getSourceLine), paramNames)

  }

  def stepImplNotInError(stepImpl: StepImplementationNode) = {

    val paramNames =
     if (Option(stepImpl.getParameterNames).isDefined)  Some(stepImpl.getParameterNames.asScala.toList) else None

    NodeDetail("Step", stepImpl.getFilename,  stepImpl.getLineNumber,stepImpl.getResult.getResult.name(), stepImpl.getId.toInt,
      stepImpl.getResult.getRunningDuration, stepImpl.getDescription, Some(stepImpl.getTargetMethod.toString ), List(),
      None, None, None, None , Some(stepImpl.getSourceLine), paramNames)

  }



  def substepNodeInError(substepNode: SubstepNode, children: List[NodeDetail]) = {

    val stackTrace = substepNode.getResult.getFailure.getCause.getStackTrace.toList.map(elem => elem.toString)

    val paramNames =
      if (Option(substepNode.getParameterNames).isDefined)  Some(substepNode.getParameterNames.asScala.toList) else None

    NodeDetail("SubstepNode", substepNode.getFilename,  substepNode.getLineNumber,substepNode.getResult.getResult.name(), substepNode.getId.toInt,
      substepNode.getResult.getRunningDuration, substepNode.getDescription, None, children,
      Some(substepNode.getResult.getFailure.getCause.getMessage), Some(stackTrace), None, None, Some(substepNode.getSourceLine), paramNames)

  }

  def substepNodeNotInError(substepNode: SubstepNode, children: List[NodeDetail]): NodeDetail = {

    val paramNames =
      if (Option(substepNode.getParameterNames).isDefined)  Some(substepNode.getParameterNames.asScala.toList) else None


    NodeDetail("SubstepNode", substepNode.getFilename, substepNode.getLineNumber, substepNode.getResult.getResult.name(), substepNode.getId.toInt,
      substepNode.getResult.getRunningDuration, substepNode.getDescription, None, children ,
      None, None, None, None , Some(substepNode.getSourceLine), paramNames)


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

  def getData(stepNode: StepNode, screenshotsDir : File, baseDataDir : File) : NodeDetail = {
    stepNode match {
      case stepImpl : StepImplementationNode => {

        val result = stepImpl.getResult

        // no error

        if (!result.getResult.isFailure) {

          NodeDetail.stepImplNotInError(stepImpl)
        }
        else {


          val screenshotFile =
            if (Option(result.getFailure.getScreenshot).isDefined){

              if(!screenshotsDir.exists()) {
                screenshotsDir.mkdir()
              }
              val screenshotFile = new File(screenshotsDir, scala.util.Random.alphanumeric.take(10).mkString)

              Files.write(result.getFailure.getScreenshot, screenshotFile)

              Some(screenshotFile.getAbsolutePath.stripPrefix(baseDataDir.getAbsolutePath))
            }
            else {
              None
            }

          NodeDetail.stepImplInError(stepImpl, screenshotFile)

        }
      }
      case substepNode : SubstepNode => {

        val result = substepNode.getResult

        // may have children
        val children =
        Option(substepNode.getChildren) match {
          case Some(childNodes) => {
            childNodes.asScala.map(child => {
              NodeDetail.getData(child, screenshotsDir, baseDataDir)
            })
          }
          case _ => List()
        }



        if (!result.getResult.isFailure) {

          NodeDetail.substepNodeNotInError(substepNode, children.toList)

        }
        else {

          if (result.getResult == ExecutionResult.CHILD_FAILED){

            NodeDetail.substepNodeNotInError(substepNode, children.toList)
          }
          else {

            NodeDetail.substepNodeInError(substepNode, children.toList)

          }
        }
      }
    }
  }

}
