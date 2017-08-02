import java.io.File

import com.technophobia.substeps.execution.node.{IExecutionNode, RootNode}
import org.substeps.report.{IExecutionResultsCollector, IReportBuilder}
package org.substeps.config {

  import java.io.File

  import com.technophobia.substeps.mojo.runner.StubExecutionReportBuilder
  import com.technophobia.substeps.runner.ExecutionConfig
  import com.typesafe.config.ConfigRenderOptions
  import org.apache.maven.model.Build
  import org.apache.maven.plugin.logging.Log
  import org.apache.maven.project.MavenProject
  import org.scalatest.{FlatSpec, FunSuite, Matchers}
  import org.substeps.report.{IExecutionResultsCollector, IReportBuilder}
  //import org.scalamock.scalatest.MockFactory

  import org.mockito.Mockito.mock
  import org.mockito.Mockito.when
  import scala.collection.JavaConversions._
  import scala.collection.JavaConverters._

  /**
    * Created by ian on 23/06/17.
    */
  class SubstepsConfigConverterTest extends FlatSpec with Matchers with BaselineGeneratedConfig {

    val options: ConfigRenderOptions = ConfigRenderOptions.defaults.setComments(false).setFormatted(true).setJson(false).setOriginComments(false)
    val compareOptions: ConfigRenderOptions = ConfigRenderOptions.defaults.setComments(false).setFormatted(false).setJson(false).setOriginComments(false)


    "minimal diversion from default config" should "result in minimal config" in {
      val project = mock(classOf[MavenProject])
      when(project.getBasedir).thenReturn(new File("."))

      val build = mock(classOf[Build])
      when(build.getTestOutputDirectory).thenReturn("testout")
      when(build.getOutputDirectory).thenReturn("out")
      when(build.getDirectory).thenReturn("dir")
      //
      when(project.getBuild).thenReturn(build)

      val ec1 = new ExecutionConfig
      ec1.setDescription("d")

      ec1.setTags("tags")
      ec1.setNonFatalTags("nonFatalTags")
      ec1.setFeatureFile("featureFile")
      ec1.setSubStepsFileName("subStepsFileName")
      ec1.setStrict(true)
      ec1.setFastFailParseErrors(true)
      //    ec1.setSystemProperties(Properties systemProperties)
      //    ec1.setNonStrictKeywordPrecedence(String[] nonStrictKeywordPrecedence)
      ec1.setStepImplementationClassNames(Array("1", "2"))
      ec1.setInitialisationClass(Array("1", "2"))
      ec1.setExecutionListeners(Array("com.technophobia.substeps.runner.logger.StepExecutionLogger"))
      ec1.setDataOutputDirectory(new File("target"))

      ec1.setCheckForUncalledAndUnused(false)

      val jmxPort = 9999
      val vmArgs = null

      val ec = List[ExecutionConfig](ec1).asJava

      // these will be the mock impls in this files
      val executionResultsCollector: IExecutionResultsCollector = new org.substeps.report.ExecutionResultsCollector
      val reportBuilder: IReportBuilder = new org.substeps.report.ReportBuilder

      val cfg = SubstepsConfigConverter.convert(new TestLogger,
        ec,
        project,
        vmArgs,
        jmxPort,
        executionResultsCollector,
        reportBuilder
      )
      println("Pretty cfg: " + cfg.root().render(options))

      println("minimal\n" + cfg.root().render(compareOptions))

      cfg should be(minimalConfig)

    }



    "special snowflake config" should "result in special snowflake config" in {

      val project = mock(classOf[MavenProject])
      when(project.getBasedir).thenReturn(new File("."))

      val build = mock(classOf[Build])
      when(build.getTestOutputDirectory).thenReturn("testout")
      when(build.getOutputDirectory).thenReturn("out")
      when(build.getDirectory).thenReturn("dir")
      //
      when(project.getBuild).thenReturn(build)

      val ec1 = new ExecutionConfig
      ec1.setDescription("d1")
      ec1.setTags("tags")
      ec1.setNonFatalTags("nonFatalTags")
      ec1.setFeatureFile("featureFile")
      ec1.setSubStepsFileName("subStepsFileName")
      ec1.setStrict(false)
      ec1.setFastFailParseErrors(false)
      ec1.setNonStrictKeywordPrecedence(Array("Given", "when", "Then"))
      ec1.setStepImplementationClassNames(Array("1", "2"))
      ec1.setInitialisationClass(Array("1", "2"))
      ec1.setExecutionListeners(Array("org.substeps.ExecutionListener"))
      ec1.setDataOutputDirectory(new File("1"))
      ec1.setCheckForUncalledAndUnused(true)

      val ec2 = new ExecutionConfig
      ec2.setDescription("d2")
      ec2.setTags("tags")
      ec2.setNonFatalTags("nonFatalTags and something else")
      ec2.setFeatureFile("featureFile")
      ec2.setSubStepsFileName("subStepsFileName")
      ec2.setStrict(false)
      ec2.setFastFailParseErrors(false)
      ec2.setNonStrictKeywordPrecedence(Array("Given", "when", "Then"))
      ec2.setStepImplementationClassNames(Array("1", "2"))
      ec2.setInitialisationClass(Array("1", "2"))
      ec2.setExecutionListeners(Array("org.substeps.ExecutionListener"))
      ec2.setDataOutputDirectory(new File("2"))
      ec2.setCheckForUncalledAndUnused(true)


      val jmxPort = 8888
      val vmArgs = "-Dsomething=whatevs"

      val ec = List[ExecutionConfig](ec1, ec2).asJava

      // these will be the mock impls in this files
      val executionResultsCollector: IExecutionResultsCollector = new ExecutionResultsCollector2
      val reportBuilder: IReportBuilder = new ReportBuilder2

      val cfg = SubstepsConfigConverter.convert(new TestLogger,
        ec,
        project,
        vmArgs,
        jmxPort,
        executionResultsCollector,
        reportBuilder,
        true
      )
      println("Pretty cfg: " + cfg.root().render(options))

      println("minimal\n" + cfg.root().render(compareOptions))

      cfg should be(minimalConfigv2)

    }


  }

  class TestLogger extends Log {
    override def debug(charSequence: CharSequence): Unit = println("DEBUG: " + charSequence)

    override def debug(charSequence: CharSequence, throwable: Throwable): Unit = println("DEBUG: " + charSequence)

    override def debug(throwable: Throwable): Unit = println("DEBUG: " + throwable)

    override def isWarnEnabled: Boolean = true

    override def error(charSequence: CharSequence): Unit = println("ERROR: " + charSequence)

    override def error(charSequence: CharSequence, throwable: Throwable): Unit = println("ERROR: " + charSequence)

    override def error(throwable: Throwable): Unit = println("ERROR: " + throwable)

    override def warn(charSequence: CharSequence): Unit = println("WARN: " + charSequence)

    override def warn(charSequence: CharSequence, throwable: Throwable): Unit = println("WARN: " + charSequence)

    override def warn(throwable: Throwable): Unit = println("WARN: " + throwable)

    override def isInfoEnabled: Boolean = true

    override def isErrorEnabled: Boolean = true

    override def isDebugEnabled: Boolean = true

    override def info(charSequence: CharSequence): Unit = println("INFO: " + charSequence)

    override def info(charSequence: CharSequence, throwable: Throwable): Unit = println("INFO: " + charSequence)

    override def info(throwable: Throwable): Unit = println("INFO: " + throwable)
  }


  class ExecutionResultsCollector2 extends IExecutionResultsCollector{
    override def initOutputDirectories(rootNode: RootNode): Unit = ???

    override def setDataDir(dataDir: File): Unit = ???

    override def setPretty(pretty: Boolean): Unit = ???

    override def getDataDir: File = ???

    /**
      * @param rootNode the node that was been executed
      * @param cause    the Throwable thrown while executing the node
      */
    override def onNodeFailed(rootNode: IExecutionNode, cause: Throwable): Unit = ???

    /**
      * @param node the node that has is going to be executed
      */
    override def onNodeStarted(node: IExecutionNode): Unit = ???

    /**
      * @param node the node that has finished been executed
      */
    override def onNodeFinished(node: IExecutionNode): Unit = ???

    /**
      * @param node the node that has been ignored
      */
    override def onNodeIgnored(node: IExecutionNode): Unit = ???
  }

  class ReportBuilder2 extends IReportBuilder{
    override def buildFromDirectory(sourceDataDir: File, reportDir: File): Unit = ???

    override def buildFromDirectory(sourceDataDir: File, reportDir: File, stepImplsJson: File): Unit = ???
  }


}





package org.substeps.report {
  import java.io.File

  import com.technophobia.substeps.execution.node.{IExecutionNode, RootNode}

  class ExecutionResultsCollector extends IExecutionResultsCollector{
    override def initOutputDirectories(rootNode: RootNode): Unit = ???

    override def setDataDir(dataDir: File): Unit = ???

    override def setPretty(pretty: Boolean): Unit = ???

    override def getDataDir: File = ???

    /**
      * @param rootNode the node that was been executed
      * @param cause    the Throwable thrown while executing the node
      */
    override def onNodeFailed(rootNode: IExecutionNode, cause: Throwable): Unit = ???

    /**
      * @param node the node that has is going to be executed
      */
    override def onNodeStarted(node: IExecutionNode): Unit = ???

    /**
      * @param node the node that has finished been executed
      */
    override def onNodeFinished(node: IExecutionNode): Unit = ???

    /**
      * @param node the node that has been ignored
      */
    override def onNodeIgnored(node: IExecutionNode): Unit = ???
  }

  class ReportBuilder extends IReportBuilder{
    override def buildFromDirectory(sourceDataDir: File, reportDir: File): Unit = ???

    override def buildFromDirectory(sourceDataDir: File, reportDir: File, stepImplsJson: File): Unit = ???
  }
}



// org.substeps.report.ExecutionResultsCollector
// org.substeps.report.ReportBuilder
