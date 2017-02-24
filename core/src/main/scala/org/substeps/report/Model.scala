package org.substeps.report

/**
  * Created by ian on 01/07/16.
  */
class Model {

}

case object Counters {

  val ZERO = build(0,0,0,0,0)

  def build(total : Int, run: Int, passed: Int, failed: Int, skipped: Int) = {

//    println(s"$total : Int, $run: Int, $passed: Int, $failed: Int, $skipped: Int")

    val passedPC = pc(passed, total)
    val failedPC = pc(failed, total)
    val skippedPC = pc(skipped, total)

    new Counters(total, run, passed, failed, skipped,
      passedPC,
      skippedPC,
      failedPC)
  }

  def pc (num : Int, total : Int) = {

    (num, total) match {
      case (0,_) => 0.0
      case (x,y) if x == y => 100.0
      case _ =>   {
        val numerator = BigDecimal.valueOf(num.toLong)
        val denominator = BigDecimal(total.toDouble)
        ((numerator / denominator) * 100).setScale(2, BigDecimal.RoundingMode.DOWN).doubleValue()
      }
    }

  }
}

case class Counters(total : Int, run: Int, passed: Int, failed: Int, skipped: Int,
                    successPC : Double,
                    skippedPC : Double,
                    failedPC : Double, tag: Option[String] = None ) {

  def + (that: Counters) : Counters = {
    Counters.build(this.total + that.total,
      this.run + that.run,
    this.passed + that.passed,
    this.failed + that.failed,
    this.skipped + that.skipped)
  }

}


case class ExecutionStats (featuresCounter : Counters, scenarioCounters : Counters, stepCounters : Counters, stepImplCounters : Counters, tag : Option[String] = None)


case class State(opened : Boolean)

case object State {
  def forResult(result : String) = {
    result match {
      case "CHILD_FAILED" =>     State(true)
      case "FAILED" => State(true)
      case "NON_CRITICAL_FAILURE"  => State(true)
      case _ => State(false)
    }

  }
}

case class JsTreeNode(id : String, text: String, icon : String, children : Option[List[JsTreeNode]], state : State, li_attr : Option[Map[String,String]] = None)


case class StepImplDesc(expressions : List[StepDesc],className: String )

case class StepDesc(expression: String ,
                    regex: String ,
                    example: String ,
                    section: String ,
                    description: String ,
                    parameterNames: List[String] ,
                    parameterClassNames: List[String]
                   )

case class GlossaryElement(
                            section: String ,
                            expression: String ,

                            className: String,
                            regex: String ,
                            example: String ,
                            description: String ,
                            parameterNames: List[String] ,
                            parameterClassNames: List[String]
                          )