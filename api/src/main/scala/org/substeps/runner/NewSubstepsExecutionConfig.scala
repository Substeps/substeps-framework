package org.substeps.runner

import java.io.File
import java.util

import com.google.common.collect.Lists
import com.technophobia.substeps.model.exception.SubstepsConfigurationException
import com.technophobia.substeps.runner.{IExecutionListener, SubstepsExecutionConfig}
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

  // transitional method
  def toConfig(substepsConfig : SubstepsExecutionConfig) : Config = {

    import com.google.common.collect.Lists

    val execConfig1 = new java.util.HashMap[String, AnyRef]
    execConfig1.put("description", substepsConfig.getDescription)
    execConfig1.put("featureFile", substepsConfig.getFeatureFile)
    if (substepsConfig.getDataOutputDirectory != null) execConfig1.put("dataOutputDir", substepsConfig.getDataOutputDirectory.getPath)
    if (substepsConfig.getNonFatalTags != null) execConfig1.put("nonFatalTags", substepsConfig.getNonFatalTags)
    execConfig1.put("substepsFile", substepsConfig.getSubStepsFileName)
    execConfig1.put("tags", substepsConfig.getTags)
    execConfig1.put("fastFailParseErrors", Boolean.box(substepsConfig.isFastFailParseErrors))

    if (substepsConfig.getNonStrictKeywordPrecedence != null)  execConfig1.put("nonStrictKeyWordPrecedence", substepsConfig.getNonStrictKeywordPrecedence.toList.asJava)

//
    execConfig1.put("stepImplementationClassNames", substepsConfig.getStepImplementationClasses.asScala.map(c => c.getName).asJava)
//
    if (substepsConfig.getExecutionListeners != null) execConfig1.put("executionListeners", substepsConfig.getExecutionListeners.toList.asJava)
    if (substepsConfig.getInitialisationClass != null) execConfig1.put("initialisationClasses", substepsConfig.getInitialisationClass.toList.asJava)
    if (substepsConfig.getExecutionListeners != null) execConfig1.put("executionListeners", substepsConfig.getExecutionListeners.toList.asJava)



    ConfigFactory.empty
      .withValue("org.substeps.config.executionConfig", ConfigValueFactory.fromMap(execConfig1))
//      .withValue("org.substeps.config.jmxPort", ConfigValueFactory.fromAnyRef(this.jmxPort))
//      .withValue("org.substeps.config.vmArgs", ConfigValueFactory.fromAnyRef(this.vmArgs))
      .withValue("org.substeps.config.executionResultsCollector", ConfigValueFactory.fromAnyRef("org.substeps.report.ExecutionResultsCollector"))
      .withValue("org.substeps.config.reportBuilder", ConfigValueFactory.fromAnyRef("org.substeps.report.ReportBuilder"))

  }

  def threadLocalConfig() : Config = threadLocalContext.get()

  val options: ConfigRenderOptions = ConfigRenderOptions.defaults.setComments(false).setFormatted(true).setJson(false).setOriginComments(false)

  def render(cfg : Config) : String = {
    // TODO - trim out bits of the config we're not interested in
    cfg.root()
      .withoutKey("awt")
      .withoutKey("java")
      .withoutKey("line")
      .withoutKey("os")
      .withoutKey("sun")
      .withoutKey("user")
      .render(options)
  }

  def loadConfig( cfgFile: String, mvnConfigOption : Option[Config]): List[Config] = {

    val base = ConfigFactory.load(cfgFile, ConfigParseOptions.defaults(), ConfigResolveOptions.defaults().setAllowUnresolved(true))

    loadConfig(base, mvnConfigOption)
  }

  def loadMasterConfig( base: Config, mvnConfigOption : Option[Config]): Config = {
    val environment = System.getProperty("ENVIRONMENT", "localhost") + ".conf"

    val envConfig = ConfigFactory.load(environment)


    val masterConfig =
      mvnConfigOption match {
        case Some(mvnConfig) => {
          log.debug("MAVEN CONFIG:\n" + render(mvnConfig))

          envConfig.withFallback(base).withFallback(mvnConfig).resolve()
        }
        case None =>   envConfig.withFallback(base).resolve()

      }

    log.debug("LOADED MASTER CONFIG:\n" + render(masterConfig))
    masterConfig
  }

  def splitConfig(masterConfig : Config): List[Config] = {
    val exeConfigList = masterConfig.getConfigList("org.substeps.config.executionConfigs").asScala

    val baseConfig = masterConfig.withoutPath("org.substeps.config.executionConfigs")

    exeConfigList.map(ec => {
      baseConfig.withValue("org.substeps.config.executionConfig", ec.root())

    }).toList

  }

  def loadConfig( base: Config, mvnConfigOption : Option[Config]): List[Config] = {

    val masterConfig = loadMasterConfig(base, mvnConfigOption)
    // there might be multilple execonfigs in there - return multiple configs for each one
    splitConfig(masterConfig)
  }




  def toConfigList(cfgFileList : java.util.List[String], mavenConfig : Config) = {

    cfgFileList.asScala.flatMap(cfgFile =>{

      loadConfig(cfgFile, Some(mavenConfig))
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

    val configList =
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
      }).toList
    }
    else List()

    configList.asJava
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

  def getNonStrictKeywordPrecedence(cfg : Config) : java.util.List[String]  = getStringListOrNull(cfg, "org.substeps.config.executionConfig.nonStrictKeyWordPrecedence")


  def getSubStepsFileName(cfg : Config) : String = {

    println("getSubStepsFileName cfg:\n" + cfg.root().render(options))

    cfg.getString("org.substeps.config.executionConfig.substepsFile")
  }

  def getScenarioName(cfg : Config) : String = getOrNull(cfg, "scenarioName")

  def getFeatureFile(cfg : Config) : String = cfg.getString("org.substeps.config.executionConfig.featureFile")

  def isStrict(cfg : Config) : Boolean = !cfg.hasPath("org.substeps.config.executionConfig.nonStrictKeyWordPrecedence")


  def isFastFailParseErrors(cfg : Config) : Boolean = {
    getBooleanOr(cfg, "org.substeps.config.executionConfig.fastFailParseErrors", true)
  }

  def getBooleanOr(cfg : Config, path: String, default : Boolean) : Boolean = {
    if (cfg.hasPath(path)){
      false
    }
    else default
  }

  def isCheckForUncalledAndUnused(cfg : Config) : Boolean = getBooleanOr(cfg, "org.substeps.config.checkForUncalledAndUnused", false)

  def getRootDataDir(masterConfig: Config) : File = {
    new File(masterConfig.getString("org.substeps.config.rootDataDir"))
  }


  def getDataOutputDirectory(cfg : Config) : File = {
    new File(cfg.getString("org.substeps.config.executionConfig.dataOutputDir"))
  }

  def getReportDir(cfg: Config) : File = {
    new File(cfg.getString("org.substeps.config.reportDir"))
  }

//  def buildInitialisationClassList(cfg: Config) : Array[Class[_]] = {
//
//    val stepImplementationClasses = NewSubstepsExecutionConfig.getStepImplementationClasses(cfg)
//    val initialisationClasses = NewSubstepsExecutionConfig.getInitialisationClasses(cfg)
//    var initClassList = null
//    if (initialisationClasses != null) initClassList = Lists.newArrayList(initialisationClasses)
//
//    ExecutionConfigWrapper.buildInitialisationClassList(stepImplementationClasses, initClassList);
//  }
}
