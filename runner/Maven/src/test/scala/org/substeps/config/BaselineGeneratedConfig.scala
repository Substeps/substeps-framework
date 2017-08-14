package org.substeps.config

import com.typesafe.config.{Config, ConfigFactory}

/**
  * Created by ian on 26/06/17.
  */
trait BaselineGeneratedConfig {

  val minimalConfig = ConfigFactory.parseString("""org {
                                                  |    substeps {
                                                  |        baseExecutionConfig {
                                                  |            featureFile=featureFile
                                                  |            initialisationClasses=[
                                                  |                "1",
                                                  |                "2"
                                                  |            ]
                                                  |            nonFatalTags=nonFatalTags
                                                  |            stepImplementationClassNames=[
                                                  |                "1",
                                                  |                "2"
                                                  |            ]
                                                  |            substepsFile=subStepsFileName
                                                  |            tags=tags
                                                  |        }
                                                  |        config {
                                                  |            description=d
                                                  |            rootDataDir=target
                                                  |        }
                                                  |        executionConfigs=[
                                                  |            {
                                                  |                dataOutputDir=""
                                                  |                description=d
                                                  |            }
                                                  |        ]
                                                  |    }
                                                  |}""".stripMargin)


  val minimalConfigv2 = ConfigFactory.parseString("""org {
                                                    |    substeps {
                                                    |        baseExecutionConfig {
                                                    |            executionListeners=[
                                                    |                "org.substeps.ExecutionListener"
                                                    |            ]
                                                    |            featureFile=featureFile
                                                    |            initialisationClasses=[
                                                    |                "1",
                                                    |                "2"
                                                    |            ]
                                                    |            nonStrictKeyWordPrecedence=[
                                                    |                Given,
                                                    |                when,
                                                    |                Then
                                                    |            ]
                                                    |            stepImplementationClassNames=[
                                                    |                "1",
                                                    |                "2"
                                                    |            ]
                                                    |            substepsFile=subStepsFileName
                                                    |            tags=tags
                                                    |        }
                                                    |        config {
                                                    |            checkForUncalledAndUnused=true
                                                    |            description=d1
                                                    |            executionResultsCollector="org.substeps.config.ExecutionResultsCollector2"
                                                    |            jmxPort=8888
                                                    |            reportBuilder="org.substeps.config.ReportBuilder2"
                                                    |            rootDataDir="${project.build.directory}/substeps_data"
                                                    |            runTestsInForkedVM=true
                                                    |            vmArgs="-Dsomething=whatevs"
                                                    |        }
                                                    |        executionConfigs=[
                                                    |            {
                                                    |                dataOutputDir="1"
                                                    |                description=d1
                                                    |                nonFatalTags=nonFatalTags
                                                    |            },
                                                    |            {
                                                    |                dataOutputDir="2"
                                                    |                description=d2
                                                    |                nonFatalTags="nonFatalTags and something else"
                                                    |            }
                                                    |        ]
                                                    |    }
                                                    |}""".stripMargin)

}
