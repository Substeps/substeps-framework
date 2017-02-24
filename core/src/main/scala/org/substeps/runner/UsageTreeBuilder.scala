package org.substeps.runner

import com.technophobia.substeps.execution.node._

import scala.collection.JavaConversions._
import scala.collection.JavaConverters._


/**
  * Created by ian on 12/09/16.
  */
object UsageTreeBuilder {

  def buildUsageTree(rootNode : RootNode) = {

    val stepImplInvocations = getStepImplNodes(rootNode)

    val uniqueStepImpls = stepImplInvocations.groupBy(_.getTargetMethod)

    uniqueStepImpls.foreach(e => {

      val methodString = e._1.getDeclaringClass.getSimpleName() + "." + e._1.getName()

      println(s"Method: $methodString \nUsages:")

      e._2.foreach(stepImpl => {
        val hierarchy = getHierarchy(stepImpl)
        print("*")
        hierarchy.foreach(node => println(s"${node.getId} ${node.getDescription} ${node.getDepth}"))
      })
    })
  }

  def getHierarchy(node : IExecutionNode) : List[IExecutionNode] = {
    Option(node) match {
      case None => List()
      case Some(n) => List(n) ++ getHierarchy(node.getParent)
    }
  }

  def getStepImplNodes(rootNode : RootNode) = {


    val features = rootNode.getChildren.asScala

    features.flatMap(f => {

      val scenarios = f.getChildren

      val basicScenarioNodes =
        scenarios.flatMap (sc => {
          sc match {
            case scOutline : OutlineScenarioNode => {

              val outlineBasicScenarioRowNodes =
                scOutline.getChildren.asScala.map(row => {
                  row.getBasicScenarioNode
                })

              outlineBasicScenarioRowNodes
            }
            case basicScenario : BasicScenarioNode => {
              List(basicScenario)
            }
          }
        })


        basicScenarioNodes.flatMap(b => {

          b.getChildren.flatMap(stepNode =>  getStepImplNodesFor(stepNode))
        })
    }).toList
  }


  def getStepImplNodesFor(stepNode : StepNode) : List[StepImplementationNode]= {

      stepNode match {
        case stepImpl : StepImplementationNode => List(stepImpl)
        case substepNode : SubstepNode => substepNode.getChildren.toList.flatMap(child => getStepImplNodesFor(child))

      }
  }
}

