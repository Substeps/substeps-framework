package org.substeps.runner

import java.io.File

import com.technophobia.substeps.model.exception.SubstepsConfigurationException
import com.technophobia.substeps.runner.IExecutionListener
import com.typesafe.config._
import org.slf4j.{Logger, LoggerFactory}
import org.substeps.report.{IExecutionResultsCollector, IReportBuilder}

import scala.collection.JavaConverters._

/**
  * Created by ian on 05/03/17.
  */
case object NewSubstepsExecutionConfig {

  private val log : Logger = LoggerFactory.getLogger("org.substeps.runner.NewSubstepsExecutionConfig")

  // TODO - could use an execution context to scope this
  private val threadLocalContext = new ThreadLocal[Config]

  def addConfigToContext(cfg :Config) = {
    // TODO add to a threadlocal context
    threadLocalContext.set(cfg)
  }

  def threadLocalConfig() : Config = threadLocalContext.get()

  val options: ConfigRenderOptions = ConfigRenderOptions.defaults.setComments(false).setFormatted(true).setJson(false).setOriginComments(false)

  def render(cfg : Config) : String = {
    // TODO - trim out bits of the config we're not interested in
    cfg.root().render(options)
  }

  def loadConfig( cfgFile: String, mvnConfig : Config): List[Config] = {

    val environment = System.getProperty("ENVIRONMENT", "localhost") + ".conf"

    val envConfig = ConfigFactory.load(environment)

    ConfigFactory.load()

    val base = ConfigFactory.load(cfgFile, ConfigParseOptions.defaults(), ConfigResolveOptions.defaults().setAllowUnresolved(true))

    log.debug("MAVEN CONFIG:\n" + render(mvnConfig))

    val initialConfig = envConfig.withFallback(base).withFallback(mvnConfig).resolve()

    log.debug("LOADED INITIAL CONFIG:\n" + render(initialConfig))

    // there might be multilple execonfigs in there - return multiple configs for each one

    val exeConfigList = initialConfig.getConfigList("org.substeps.config.executionConfigs").asScala

    val baseConfig = initialConfig.withoutPath("org.substeps.config.executionConfigs")

    exeConfigList.map(ec => {
      baseConfig.withValue("org.substeps.config.executionConfig", ec.root())

    }).toList
  }



  def toConfigList(cfgFileList : java.util.List[String], mavenConfig : Config) = {

    cfgFileList.asScala.flatMap(cfgFile =>{

      loadConfig(cfgFile, mavenConfig)
    }).asJava
  }

  def getInitialisationClasses(cfg : Config) :  Array[Class[_]]= {

    if (cfg.hasPath("org.substeps.config.executionConfig.initialisationClasses")) {
      val initClassNames = cfg.getStringList("org.substeps.config.executionConfig.initialisationClasses").asScala

      initClassNames.map(className => {
        Class.forName(className)
      }).toArray
    }
    else null
  }


  def getExecutionResultsCollector(cfg : Config) : IExecutionResultsCollector = {
      val collector = cfg.getString("org.substeps.config.executionResultsCollector")

      Class.forName(collector).newInstance().asInstanceOf[IExecutionResultsCollector]
  }

  def getReportBuilder(cfg : Config) : IReportBuilder = {
    val rb = cfg.getString("org.substeps.config.reportBuilder")

    Class.forName(rb).newInstance().asInstanceOf[IReportBuilder]
  }

  def getExecutionListenerClasses(cfg : Config) : java.util.List[Class[_ <: IExecutionListener]] = {

    if (cfg.hasPath("org.substeps.config.executionListeners")) {
      val initClassNames = cfg.getStringList("org.substeps.config.executionListeners").asScala

      initClassNames.map(className => {

        val implClass = Class.forName(className)

        if (classOf[IExecutionListener].isAssignableFrom(implClass)){
          implClass.asInstanceOf[Class[_ <: IExecutionListener]]
        }
        else {
          throw new SubstepsConfigurationException("Execution Listener does not extend com.technophobia.substeps.runner.IExecutionListener")
        }
      }).toList.asJava
    }
    else null


  }

  // TODO type bind this
  def getStepImplementationClasses(cfg : Config) : java.util.List[Class[_]] = getClassList(cfg, "org.substeps.config.executionConfig.stepImplementationClassNames")

  def getStepImplementationClassNames (cfg : Config) = getStringListOrNull(cfg, "org.substeps.config.executionConfig.stepImplementationClassNames")

  def getClassList(cfg : Config, path : String) : java.util.List[Class[_]] = {

    if (cfg.hasPath(path)) {
      val initClassNames = cfg.getStringList(path).asScala

      initClassNames.map(className => {
        Class.forName(className)
      }).toList.asJava
    }
    else null
  }

  def getDescription(cfg : Config) : String ={

    if (cfg.hasPath("org.substeps.config.executionConfig.description")){
      cfg.getString("org.substeps.config.executionConfig.description")
    }
    else "SubStepsMojo"

  }

  def getOrNull(cfg : Config, path : String) : String = {
    if (cfg.hasPath(path)){
      cfg.getString(path)
    }
    else null
  }

  def getStringListOrNull(cfg : Config, path : String) : java.util.List[String] = {
    if (cfg.hasPath(path)){
      cfg.getStringList(path)
    }
    else null
  }


  def getTags(cfg : Config) : String = getOrNull(cfg, "org.substeps.config.executionConfig.tags")

  def getNonFatalTags(cfg : Config) : String = getOrNull(cfg, "org.substeps.config.executionConfig.nonFatalTags")

  def getNonStrictKeywordPrecedence(cfg : Config) : java.util.List[String]  = getStringListOrNull(cfg, "nonStrictKeyWordPrecedence")


  def getSubStepsFileName(cfg : Config) : String = {

    println("getSubStepsFileName cfg:\n" + cfg.root().render(options))

    cfg.getString("org.substeps.config.executionConfig.substepsFile")
  }

  def getScenarioName(cfg : Config) : String = getOrNull(cfg, "scenarioName")

  def getFeatureFile(cfg : Config) : String = cfg.getString("org.substeps.config.executionConfig.featureFile")

  def isStrict(cfg : Config) : Boolean = {
    if (cfg.hasPath("nonStrictKeyWordPrecedence")){
      false
    }
    else true
  }

  def isFastFailParseErrors(cfg : Config) : Boolean = {
    getBooleanOr(cfg, "fastFailParseErrors", true)
  }

  def getBooleanOr(cfg : Config, path: String, default : Boolean) : Boolean = {
    if (cfg.hasPath(path)){
      false
    }
    else default
  }

  def isCheckForUncalledAndUnused(cfg : Config) : Boolean = getBooleanOr(cfg, "checkForUncalledAndUnused", true)

  def getDataOutputDirectory(cfg : Config) : File = {
    new File(cfg.getString("org.substeps.config.executionConfig.dataOutputDir"))
  }

  def getReportDir(cfg: Config) : File = {
    new File(cfg.getString("org.substeps.config.reportDir"))
  }
}
