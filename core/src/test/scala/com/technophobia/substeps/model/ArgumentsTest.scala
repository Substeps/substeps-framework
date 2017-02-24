package com.technophobia.substeps.model

import com.technophobia.substeps.runner.ExecutionContext
import org.scalatest.{FunSuite, Matchers}

/**
  * Created by ian on 13/01/17.
  */
class Sample(name : String, other : Other){
  def getName() = name
  def getOther() = other
}

class Other(name : String){
  def getName() = name
}

class ArgumentsTest extends FunSuite with Matchers{

  test("test Arguments expression evaluation"){
    val res1 = Arguments.evaluateExpression("${doesnt.exist}")
    res1 should be (null)

    ExecutionContext.put(Scope.SUITE, "key", new Sample("suite", new Other("o1")))

    ExecutionContext.put(Scope.SCENARIO, "key", new Sample("scenario", new Other("o2")))

    val res2 = Arguments.evaluateExpression("${key.name}")
    res2 should be ("scenario")

    val res3 = Arguments.evaluateExpression("${key.other.name}")
    res3 should be ("o2")

  }
}
