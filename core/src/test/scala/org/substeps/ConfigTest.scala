package org.substeps

import java.util

import com.technophobia.substeps.Data
import org.scalatest.{FunSuite, Matchers}
import com.typesafe.config.{ConfigFactory, ConfigRenderOptions, ConfigValueFactory}

/**
  * Created by ian on 20/12/16.
  */
class ConfigTest extends FunSuite with Matchers{


  test("config merging of arrays"){

    val base =
      """test{
        | arrayKey=["one", "two"]
        |}
      """.stripMargin

    val baseConfig = ConfigFactory.parseString(base)

    val fallback =       """test{
                           | simpleFallback="basefallback"
                           | arrayKey=["base"]
                           |}
                         """.stripMargin


    val fallBackConfig = ConfigFactory.parseString(fallback)

//    val cfg = baseConfig.withFallback(fallBackConfig.)
//
//    println("config:\n" + cfg.root().render())


  }

  test("test subconfig"){

    val src =
      """org.substeps.webdriver{
        |window{
        | maximise=true
        | height=2
        | width=7
        |}
        |}
      """.stripMargin

    val cfg = ConfigFactory.parseString(src)

    val c = cfg.getConfig("org.substeps.webdriver.window")

    println("c:\n" + c.root().render())
  }

  test("test arrays conversion"){
    import scala.collection.JavaConversions._
    import scala.collection.JavaConverters._

    val keywordPrecedenceList = Data.ARRAY//.asScala//util.Arrays.asList(Data.ARRAY)

    val list = keywordPrecedenceList.toList
     // for (s <- Data.ARRAY) yield s

    println("keywordPrecedenceList: " + list)
  }


  test("test property substitution with env vars in conf files") {

    val configVal: String = runTest

    configVal should be ("https://username:somekey@ondemand.saucelabs.com:443/wd/hub")

  }

  test("test property substitution with env vars in properties files doesnt work") {

    System.setProperty("substeps.use.properties", "true")

    val configVal: String = runTest

    configVal should be ("https://${USERNAME}:${ACCESS_KEY}@ondemand.saucelabs.com:443/wd/hub")

  }

  import scala.collection.JavaConversions._
  import scala.collection.JavaConverters._

  test("test config paths"){

    val cfg = ConfigFactory.parseString("""org{
                                          |    substeps {
                                          |        config {
                                          |            executionConfigs=[
                                          |                {
                                          |                    dataOutputDir=out
                                          |                    description=description
                                          |                    executionListeners=[
                                          |                        "org.substeps.execListener"
                                          |                    ]
                                          |                    featureFile=ff
                                          |                    initialisationClasses=[
                                          |                        "com.abc.Init",
                                          |                        "com.abc.B"
                                          |                    ]
                                          |                    nonFatalTags=nonfatal
                                          |                    nonStrictKeyWordPrecedence=[
                                          |                        given,
                                          |                        when
                                          |                    ]
                                          |                    stepImplementationClassNames=[
                                          |                        "com.xyz.StepImpl",
                                          |                        "com.abc.StepImpl"
                                          |                    ]
                                          |                    substepsFile=substeps
                                          |                    tags="@all"
                                          |                },
                                          |                                {
                                          |                    dataOutputDir=out
                                          |                    description=description2
                                          |                    executionListeners=[
                                          |                        "org.substeps.execListener"
                                          |                    ]
                                          |                    featureFile=ff2
                                          |                    initialisationClasses=[
                                          |                        "com.abc.Init2",
                                          |                        "com.abc.B2"
                                          |                    ]
                                          |                    nonFatalTags=nonfatal2
                                          |                    nonStrictKeyWordPrecedence=[
                                          |                        given,
                                          |                        when
                                          |                    ]
                                          |                    stepImplementationClassNames=[
                                          |                        "com.xyz.StepImpl2",
                                          |                        "com.abc.StepImpl2"
                                          |                    ]
                                          |                    substepsFile=substeps2
                                          |                    tags="@all2"
                                          |                }
                                          |
                                          |            ]
                                          |            executionResultsCollector="com.technophobia.substeps.mojo.runner.ToConfigTest$Collector"
                                          |            jmxPort=null
                                          |            reportBuilder="com.technophobia.substeps.mojo.runner.ToConfigTest$TestReportBuilder"
                                          |            vmArgs=null
                                          |        }
                                          |    }
                                          |}
                                          |""".stripMargin)


    val exeConfigList = cfg.getConfigList("org.substeps.config.executionConfigs").asScala

    val initClasses = exeConfigList(0).getStringList("initialisationClasses").asScala

    println(initClasses)

    val baseConfig = cfg.withoutPath("org.substeps.config.executionConfigs")

    val ec = exeConfigList(0)
    val options = ConfigRenderOptions.defaults.setComments(false).setFormatted(true).setJson(false).setOriginComments(false)

    println("ec root : " + ec.root().render(options))

    val resolved = baseConfig.withValue("org.substeps.config.executionConfig", ec.root())

    println("resolved root : " + resolved.root().render(options ))

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

    ConfigFactory.invalidateCaches()


    val config = ConfigFactory.load(s"localhost.$ext")

    println("cfg: " +
      config.root().render())

    val configVal = config.getString("some.val")

    configVal

  }
}
