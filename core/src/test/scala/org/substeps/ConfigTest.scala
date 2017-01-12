package org.substeps

import org.scalatest.{FunSuite, Matchers}
import com.typesafe.config.ConfigFactory

/**
  * Created by ian on 20/12/16.
  */
class ConfigTest extends FunSuite with Matchers{

  test("test property substitution with env vars in conf files") {

    val configVal: String = runTest

    configVal should be ("https://username:somekey@ondemand.saucelabs.com:443/wd/hub")

  }

  test("test property substitution with env vars in properties files doesnt work") {

    System.setProperty("substeps.use.properties", "true")

    val configVal: String = runTest

    configVal should be ("https://${USERNAME}:${ACCESS_KEY}@ondemand.saucelabs.com:443/wd/hub")

  }


  private def runTest = {
    System.setProperty("USERNAME", "username")
    System.setProperty("ACCESS_KEY", "somekey")

    val useProps = System.getProperty("substeps.use.properties")

    val ext =
      Option(useProps) match {
        case Some(x) => {
          "properties"
        }
        case None => "conf"
      }

    val config = ConfigFactory.load(s"localhost.$ext")

    val configVal = config.getString("some.val")
    configVal
  }
}
