package org.substeps.config

import java.io.File
import java.util

import com.google.common.collect.Lists
import com.technophobia.substeps.runner.ExecutionConfig
import com.typesafe.config.{Config, ConfigFactory, ConfigRenderOptions, ConfigValueFactory}
import org.apache.commons.lang3.StringUtils
import org.apache.maven.plugin.logging.Log
import org.apache.maven.project.MavenProject
import org.substeps.report.{IExecutionResultsCollector, IReportBuilder}

import scala.collection.JavaConversions._
import scala.collection.JavaConverters._

/**
  * 
  * Created by ian on 23/06/17.
  */
object SubstepsConfigConverter {


  def buildBaseConfigMap(executionConfigList: List[util.Map[String, Object]]) : util.Map[String,Object]= {

    val potentialCommonKeys = List("featureFile", "substepsFile", "initialisationClasses", "nonFatalTags", "tags", "stepImplementationClassNames", "nonStrictKeyWordPrecedence", "executionListeners")

    val baseKeysAndValues: Map[String, Object] =
    potentialCommonKeys.flatMap(k => {
      val valsForKey =
        executionConfigList.flatMap(ecMap => {
          Option(ecMap.get(k))
        }).distinct

      if (valsForKey.size == 1){
        Some((k,valsForKey.head))
      }
      else
        None
    }).toMap

    baseKeysAndValues.asJava
  }

  def cleanExecutionConfigList(executionConfigList: List[util.Map[String, Object]], baseConfigMap: util.Map[String, Object]): List[util.Map[String, Object]] = {
    // remove all keys that are in the base map from each map in the list

    executionConfigList.map(ecMap => {

      baseConfigMap.keySet().asScala.foreach(k => ecMap.remove(k))
      ecMap
    })
  }

  def convert(log: Log, ec: java.util.List[ExecutionConfig],
              proj : MavenProject,
              vmArgs : String,
              jmxPort : Int,
              executionResultsCollector : IExecutionResultsCollector,
              reportBuilder : IReportBuilder,
              runTestsInForkedVM : Boolean = false) : Config  = {

    implicit val logger : Log = log
    implicit val project : MavenProject = proj

    val executionConfigs = ec.asScala.toList

    val multipleExecutionConfigs =
    if (executionConfigs.size > 1) {
      logger.info("\n\n ** There are multiple execution configs, the generated config may need some manual editing.  Substeps data will be written to a single dir with sub dirs for each exeecution config **\n\n")
      true
    }
    else false

    var idx = 0
    val executionConfigList = executionConfigs.map(e => {
      idx = idx + 1
      toMap(e, multipleExecutionConfigs, idx)
    })

    val baseConfigMap = buildBaseConfigMap(executionConfigList)
    val cleanedExecutionConfigList : List[util.Map[String, Object]] = cleanExecutionConfigList(executionConfigList, baseConfigMap)


    val baseDescription: String = executionConfigs.head.getDescription

    var cfg: Config = ConfigFactory.empty.withValue("org.substeps.config.description", ConfigValueFactory.fromAnyRef(baseDescription))
    if (vmArgs != null) cfg = cfg.withValue("org.substeps.config.vmArgs", ConfigValueFactory.fromAnyRef(vmArgs))
    if (jmxPort != 9999) cfg = cfg.withValue("org.substeps.config.jmxPort", ConfigValueFactory.fromAnyRef(jmxPort))

    if (multipleExecutionConfigs){
      val rootDataDir: String = project.getBuild.getDirectory + File.separator + "substeps_data"
      cfg = cfg.withValue("org.substeps.config.rootDataDir", ConfigValueFactory.fromAnyRef(deClutter(rootDataDir)))
    }
    else {
      // this should hopefully cater for when the data is being written elsewhere..
      if (!executionConfigList.get(0).containsKey("dataOutputDir")) {
        cfg = cfg.withValue("org.substeps.config.rootDataDir", ConfigValueFactory.fromAnyRef(deClutter(executionConfigs.get(0).getDataOutputDirectory.getPath)))
        executionConfigList.get(0).put("dataOutputDir", "")
      }

    }



    val ecList: util.List[util.Map[String, Object]] = cleanedExecutionConfigList.asJava

    cfg = cfg.withValue("org.substeps.executionConfigs", ConfigValueFactory.fromIterable(ecList))

    cfg = cfg.withValue("org.substeps.baseExecutionConfig", ConfigValueFactory.fromMap(baseConfigMap))


    if (!(executionResultsCollector.getClass.getName == "org.substeps.report.ExecutionResultsCollector")) cfg = cfg.withValue("org.substeps.config.executionResultsCollector", ConfigValueFactory.fromAnyRef(executionResultsCollector.getClass.getName))
    if (!(reportBuilder.getClass.getName == "org.substeps.report.ReportBuilder")) cfg = cfg.withValue("org.substeps.config.reportBuilder", ConfigValueFactory.fromAnyRef(reportBuilder.getClass.getName))

    executionConfigs.find(e => e.isCheckForUncalledAndUnused).map(_ => {
      cfg = cfg.withValue("org.substeps.config.checkForUncalledAndUnused", ConfigValueFactory.fromAnyRef(true))
    })

    if (runTestsInForkedVM) cfg = cfg.withValue("org.substeps.config.runTestsInForkedVM", ConfigValueFactory.fromAnyRef(runTestsInForkedVM))


    cfg

  }


  def renderSanitizedConfig(cfg : Config)(implicit logger : Log) : String = {

    val options = ConfigRenderOptions.defaults.setComments(false).setFormatted(true).setJson(false).setOriginComments(false)
    sanitize(cfg.root.render(options))
  }


  // necessary so that the variables get substituted correctly
  private def sanitize(s: String): String = {
    var src = s
    src = StringUtils.replace(src, "\"${project.build.testOutputDirectory}", "${project.build.testOutputDirectory}\"")
    src = StringUtils.replace(src, "\"${project.build.outputDirectory}", "${project.build.outputDirectory}\"")
    src = StringUtils.replace(src, "\"${project.build.directory}", "${project.build.directory}\"")
    src = StringUtils.replace(src, "\"${basedir}", "${basedir}\"")
    src
  }

  private def deClutter(value: String)(implicit logger : Log, project : MavenProject): String = {
    val baseDir: String = project.getBasedir.getAbsolutePath
    val testOut: String = project.getBuild.getTestOutputDirectory
    val srcOut: String = project.getBuild.getOutputDirectory
    val target: String = project.getBuild.getDirectory
    logger.info("declutter value: " + value)
    var rtn: String = value
    if (value.startsWith(testOut)) rtn = "${project.build.testOutputDirectory}" + StringUtils.removeStart(value, testOut)
    else if (value.startsWith(srcOut)) rtn = "${project.build.outputDirectory}" + StringUtils.removeStart(value, target)
    else if (value.startsWith(target)) rtn = "${project.build.directory}" + StringUtils.removeStart(value, target)
      else if (value.startsWith(baseDir)) rtn = "${basedir}" + StringUtils.removeStart(value, target)
    rtn
  }

  private def toMap(executionConfig: ExecutionConfig, multipleExecutionConfigs : Boolean, idx : Int)(implicit logger : Log, project : MavenProject): util.Map[String, Object] = {
    val execConfig1: util.Map[String, Object] = new util.HashMap[String, Object]
    execConfig1.put("description", executionConfig.getDescription)
    execConfig1.put("featureFile", deClutter(executionConfig.getFeatureFile))

    if (multipleExecutionConfigs){
      // go with defaults
      execConfig1.put("dataOutputDir", idx.toString)
    }
    else {
      // this is the default root data dir
      val rootDataDir: String = project.getBuild.getDirectory + File.separator + "substeps_data"

      if (StringUtils.startsWith(executionConfig.getDataOutputDirectory.getPath, rootDataDir)) {
        execConfig1.put("dataOutputDir", StringUtils.removeStart(executionConfig.getDataOutputDirectory.getPath, rootDataDir))
      }

    }
    // else the root data dir is being somewhere else, so we'll set the root data value accordingly

    if (executionConfig.getNonFatalTags != null) execConfig1.put("nonFatalTags", executionConfig.getNonFatalTags)
    execConfig1.put("substepsFile", deClutter(executionConfig.getSubStepsFileName))
    execConfig1.put("tags", executionConfig.getTags)

    if (executionConfig.getNonStrictKeywordPrecedence != null) execConfig1.put("nonStrictKeyWordPrecedence", executionConfig.getNonStrictKeywordPrecedence.toList.asJava)
    execConfig1.put("stepImplementationClassNames", executionConfig.getStepImplementationClassNames.toList.asJava)

    if (executionConfig.getExecutionListeners.length > 1 || !(executionConfig.getExecutionListeners()(0) == "com.technophobia.substeps.runner.logger.StepExecutionLogger")) execConfig1.put("executionListeners", executionConfig.getExecutionListeners.toList.asJava)
    if (executionConfig.getInitialisationClass != null) execConfig1.put("initialisationClasses", executionConfig.getInitialisationClass.toList.asJava)
    execConfig1
  }

}
