package org.substeps.runner

import java.io.File

import com.google.common.collect.Lists
import com.technophobia.substeps.model.{Configuration, SubSteps}
import com.technophobia.substeps.model.exception.SubstepsConfigurationException
import com.technophobia.substeps.runner.{IExecutionListener, SubstepsExecutionConfig}
import com.typesafe.config._
import org.slf4j.{Logger, LoggerFactory}
import org.substeps.report.{IExecutionResultsCollector, IReportBuilder}

import scala.collection.JavaConverters._

/**
  * Created by ian on 05/03/17.
  */

trait SubstepsConfigKeys {

  val `stepDepthDescriptionKey` = "step.depth.description"
  val `logUncallEdAndUnusedStepImplsKey` =   "log.unused.uncalled"
  val `prettyPrintReportDataKey` = "report.data.pretty.print"

  val `rootDataDirKey` = "org.substeps.config.rootDataDir"
  val `substepsReportDir` = "org.substeps.config.reportDir"

  val `executionConfigDataOutputDir` = "org.substeps.executionConfig.dataOutputDir"

  val `checkForUncalledAndUnused` = "org.substeps.config.checkForUncalledAndUnused"

  val `substepsConfigKey` = "org.substeps.config"

  val `substepsFileKey` = "org.substeps.executionConfig.substepsFile"

  val `initialisationClassesKey` = "org.substeps.executionConfig.initialisationClasses"

  val `executionResultsCollectorKey` = "org.substeps.config.executionResultsCollector"

  val `runTestsInForkedVMKey` = "org.substeps.config.runTestsInForkedVM"

  val `rootNodeDescriptionProviderKey` = "org.substeps.config.report.rootNodeDescriptionProvider"

  val `reportBuilderKey` = "org.substeps.config.reportBuilder"

  val `executionListenersKey` = "org.substeps.executionConfig.executionListeners"

  val `stepImplementationClassNamesKey` = "org.substeps.executionConfig.stepImplementationClassNames"

  val `stepImplementationsExcludedInGloosary` = "org.substeps.config.glossary.excludeStepImplementationClassNames"


  val `executionConfigDescriptionKey` = "org.substeps.executionConfig.description"

  val `executionConfigTagsKey` = "org.substeps.executionConfig.tags"

  val `executionConfigNonFatalTagsKey` = "org.substeps.executionConfig.nonFatalTags"

  val `nonStrictKeyWordPrecedenceKey` = "org.substeps.executionConfig.nonStrictKeyWordPrecedence"

  val `scenarioNameKey` = "org.substeps.executionConfig.scenarioName"

  val `featureFileKey` = "org.substeps.executionConfig.featureFile"

  val `fastFailParseErrorsKey` = "org.substeps.executionConfig.fastFailParseErrors"

  val `jmxPortKey` = "org.substeps.config.jmxPort"
  val `vmArgsKey` = "org.substeps.config.vmArgs"

}

case class ParameterSubstitution(substituteParameters: Boolean, startDelimiter: String, endDelimiter: String, normalizeValues: Boolean, normalizeFrom: String, normalizeTo: String )

object ParameterSubstitution{
  def apply(cfg : Config) : ParameterSubstitution = {

    ParameterSubstitution(
    cfg.getBoolean("parameter.substitution.enabled"),
    cfg.getString("parameter.substitution.start"),
    cfg.getString("parameter.substitution.end"),
    cfg.getBoolean("parameter.substitution.normalizeValue"),
    cfg.getString("parameter.substitution.normalize.from"),
    cfg.getString("parameter.substitution.normalize.to"))
  }
}

object JSubstepsConfigKeys extends SubstepsConfigKeys

object NewSubstepsExecutionConfig extends SubstepsConfigKeys {

  private val log : Logger = LoggerFactory.getLogger("org.substeps.runner.NewSubstepsExecutionConfig")

  // TODO - could use an execution context to scope this
  private val threadLocalContext = new ThreadLocal[Config]

  def setThreadLocalConfig(cfg :Config) = {
    threadLocalContext.set(cfg)
  }

  def getJmxPort() : Int = {
    threadLocalConfig().getInt(`jmxPortKey`)
  }

  def getVMArgs() : String = {
    getOrNull(threadLocalConfig(), `vmArgsKey`)
  }

  def threadLocalConfig() : Config = threadLocalContext.get()

  def getParameterSubstituionConfig() : ParameterSubstitution = {

    val cfg = threadLocalConfig().getConfig(`substepsConfigKey`)

    ParameterSubstitution(cfg)

  }

  def validateExecutionConfig(implicit cfg : Config) : Unit = {

    val substepsFile = getStringOrThrow(`substepsFileKey`, "no substeps file or directory specified")
    validateFile(substepsFile, s"Substeps file path $substepsFile doesn't exist" )


  }

  private def validateFile(path : String, msg : String) = {

    if (!new File(path).exists()){
      throw new SubstepsConfigurationException(msg)
    }
  }

  private def getStringOrThrow(path: String, msg : String)(implicit cfg : Config) : String = {
    if (cfg.hasPath(path)){
      cfg.getString(path)
    }
    else
      throw new SubstepsConfigurationException(msg)
  }

  def getDataSubdir(cfg: Config) : String = {
    cfg.getString(`executionConfigDataOutputDir`)
  }

  def getDataDirForConfig(cfg: Config) : File = {

    if (!cfg.hasPath(`rootDataDirKey`)){
      throw new SubstepsConfigurationException("org.substeps.config.rootDataDir must be defined")
    }

    if (!cfg.hasPath(`executionConfigDataOutputDir`)){
      throw new SubstepsConfigurationException("org.substeps.config.executionConfig.dataOutputDir must be defined as the relative path from org.substeps.config.rootDataDir")
    }

    val dir = new File(cfg.getString(`rootDataDirKey`), cfg.getString(`executionConfigDataOutputDir`))

//    if (!dir.exists()){
//      throw new SubstepsConfigurationException(s"data dir ${dir.getAbsolutePath()} for execution config doesn't exist")
//    }
    dir
  }



  // transitional method
  def toConfig(substepsConfig : SubstepsExecutionConfig) : Config = {

    import com.google.common.collect.Lists

    val execConfig1 = new java.util.HashMap[String, AnyRef]
    execConfig1.put("description", Option(substepsConfig.getDescription).getOrElse("Substeps test execution description"))
    execConfig1.put("featureFile", substepsConfig.getFeatureFile)
    if (substepsConfig.getDataOutputDirectory != null) execConfig1.put("dataOutputDir", substepsConfig.getDataOutputDirectory.getPath)
    if (substepsConfig.getNonFatalTags != null) execConfig1.put("nonFatalTags", substepsConfig.getNonFatalTags)
    execConfig1.put("substepsFile", substepsConfig.getSubStepsFileName)
    execConfig1.put("tags", substepsConfig.getTags)
    execConfig1.put("fastFailParseErrors", Boolean.box(substepsConfig.isFastFailParseErrors))

    if (substepsConfig.getScenarioName != null) execConfig1.put("scenarioName", substepsConfig.getScenarioName)

    // this isn't used directly - it's inferred
    // substepsConfig.isStrict
    if (substepsConfig.getNonStrictKeywordPrecedence != null && !substepsConfig.getNonStrictKeywordPrecedence.isEmpty)  execConfig1.put("nonStrictKeyWordPrecedence", substepsConfig.getNonStrictKeywordPrecedence.toList.asJava)

    val stepImplClassNameList =
      if (Option(substepsConfig.getStepImplementationClasses).isEmpty){
        substepsConfig.getStepImplementationClassNames.toList
      }
      else {
        substepsConfig.getStepImplementationClasses.asScala.map(c => c.getName)
      }


    execConfig1.put("stepImplementationClassNames", stepImplClassNameList.asJava)
//
    if (substepsConfig.getExecutionListeners != null) execConfig1.put("executionListeners", substepsConfig.getExecutionListeners.toList.asJava)
    if (substepsConfig.getInitialisationClass != null) execConfig1.put("initialisationClasses", substepsConfig.getInitialisationClass.toList.asJava)
    if (substepsConfig.getExecutionListeners != null) execConfig1.put("executionListeners", substepsConfig.getExecutionListeners.toList.asJava)

    if (!substepsConfig.isFastFailParseErrors) execConfig1.put("fastFailParseErrors", Boolean.box(substepsConfig.isFastFailParseErrors))





    execConfig1.put("dataOutputDir", "1")


    val cfg = ConfigFactory.empty
      .withValue("org.substeps.executionConfigs", ConfigValueFactory.fromIterable(List(ConfigValueFactory.fromMap(execConfig1)).asJava) )
//      .withValue("org.substeps.config.jmxPort", ConfigValueFactory.fromAnyRef(this.jmxPort))
//      .withValue("org.substeps.config.vmArgs", ConfigValueFactory.fromAnyRef(this.vmArgs))

      // these are provided in ref.conf
      //.withValue("org.substeps.config.executionResultsCollector", ConfigValueFactory.fromAnyRef("org.substeps.report.ExecutionResultsCollector"))
      //.withValue("org.substeps.config.reportBuilder", ConfigValueFactory.fromAnyRef("org.substeps.report.ReportBuilder"))


    // this is to pick up the reference.conf
    cfg.withFallback(ConfigFactory.load(ConfigParseOptions.defaults(), ConfigResolveOptions.noSystem().setAllowUnresolved(true)))
  }



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
      .withValue("remote.token",  ConfigValueFactory.fromAnyRef("******"))
      .withValue("remote.username",  ConfigValueFactory.fromAnyRef("******"))
      // TODO - correct the webdriver paths here or work out how to mask certain fields - surely some config ?

      .render(options)
  }




//  def toConfigList(cfgFileList : java.util.List[String], mavenConfig : Config) = {
//
//    cfgFileList.asScala.flatMap(cfgFile =>{
//
//      loadConfig(cfgFile, Some(mavenConfig))
//    }).asJava
//  }

//  def loadConfig( cfgFile: String): List[Config] = {
//    loadConfig(cfgFile, None)
//  }

//  def loadConfig( cfgFile: String, mvnConfigOption : Option[Config]): List[Config] = {
//
//    val base = ConfigFactory.load(cfgFile, ConfigParseOptions.defaults(), ConfigResolveOptions.defaults().setAllowUnresolved(true))
//
//    loadConfig(base, mvnConfigOption)
//  }
//
//
//  def loadConfig( base: Config, mvnConfigOption : Option[Config]): List[Config] = {
//
//    val masterConfig = loadMasterConfig(base, mvnConfigOption)
//    // there might be multilple execonfigs in there - return multiple configs for each one
//    splitConfig(masterConfig)
//  }



//  def loadMasterConfig( base: Config): Config = {
//    loadMasterConfig(base, None)
//  }
//
//  def loadMasterConfig( base: Config, mvnConfigOption : Option[Config]): Config = {
//    val environment = System.getProperty("ENVIRONMENT", "localhost") + ".conf"
//
//    val envConfig = ConfigFactory.load(environment)
//
//
//    val masterConfig =
//      mvnConfigOption match {
//        case Some(mvnConfig) => {
//          log.debug("MAVEN CONFIG:\n" + render(mvnConfig))
//
//          envConfig.withFallback(base).withFallback(mvnConfig).resolve()
//        }
//        case None =>   envConfig.withFallback(base).resolve()
//
//      }
//
//    log.debug("LOADED MASTER CONFIG:\n" + render(masterConfig))
//    masterConfig
//  }

  val legacyConfigKeys = List("step.depth.description",
    "log.unused.uncalled",
    "report.data.pretty.print",
    "report.data.base.dir",
    "base.url",
    "driver.type",
    "default.webdriver.timeout.secs",
    "webdriver.locale",
    "htmlunit.disable.javascript",
    "htmlunit.proxy.host",
    "htmlunit.proxy.port",
    "network.proxy.host",
    "network.proxy.port",
    "step.depth.description",
    "log.pagesource.onerror",
    "webdriver.manager.properties")

  def checkMasterConfigForLegacyDefaults(masterConfig : Config) = {

    legacyConfigKeys.find(key => masterConfig.hasPath(key)) match {
      case Some(k) => {
        log.warn(
          "\n" +
            "****************************************************************************\n" +
            "*               YOUR CONFIG CONTAINS LEGACY OVERRIDE KEYS !!               *\n" +
            "****************************************************************************\n" +
            "\nAll Substeps keys have now moved under org.substeps and can be overriden like this:\n"
            + NewSubstepsExecutionConfig.render(masterConfig.getConfig("org.substeps")))
        log.warn("Overrides will currently still work but support for them will be removed in a subsequent release")
      }
      case _ =>
    }

  }




//  def splitConfigAsJava(masterConfig : Config) = {
//    splitConfig(masterConfig).asJava
//  }
//
//  def splitConfig(masterConfig : Config): List[Config] = {
//    val exeConfigList = masterConfig.getConfigList("org.substeps.config.executionConfigs").asScala
//
//    val baseConfig = masterConfig.withoutPath("org.substeps.config.executionConfigs")
//
//    exeConfigList.map(ec => {
//      baseConfig.withValue("org.substeps.config.executionConfig", ec.root())
//
//    }).toList
//  }
//
//  def splitConfigAsOne(masterConfig : Config): Config = {
//    splitConfig(masterConfig).head
//  }







  def getInitialisationClasses(cfg : Config) :  Array[Class[_]]= {

    if (cfg.hasPath(`initialisationClassesKey`)) {
      val initClassNames = cfg.getStringList(`initialisationClassesKey`).asScala

      initClassNames.map(className => {
        Class.forName(className)
      }).toArray
    }
    else null
  }


  def getExecutionResultsCollector(cfg : Config) : IExecutionResultsCollector = {
      val collector = cfg.getString(`executionResultsCollectorKey`)

      Class.forName(collector).newInstance().asInstanceOf[IExecutionResultsCollector]
  }

  def isRunInForkedVM(cfg : Config) : Boolean = {
    cfg.getBoolean(`runTestsInForkedVMKey`)
  }


  def getRootNodeDescriptor[T](cfg : Config) : T = {
    val descriptorClass = cfg.getString(`rootNodeDescriptionProviderKey`)
    Class.forName(descriptorClass).newInstance().asInstanceOf[T]
  }



  def getReportBuilder(cfg : Config) : IReportBuilder = {
    val rb = cfg.getString(`reportBuilderKey`)

    Class.forName(rb).newInstance().asInstanceOf[IReportBuilder]
  }

  def getExecutionListenerClasses(cfg : Config) : java.util.List[Class[_ <: IExecutionListener]] = {

    val configList =
    if (cfg.hasPath(`executionListenersKey`)) {
      val initClassNames = cfg.getStringList(`executionListenersKey`).asScala

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
  def getStepImplementationClasses(cfg : Config) : java.util.List[Class[_]] = getClassList(cfg, `stepImplementationClassNamesKey`)

  def getStepImplementationClassNames (cfg : Config) = getStringListOrNull(cfg, `stepImplementationClassNamesKey`)

  def getStepImplementationClassNamesGlossaryExcluded(cfg : Config) = getStringListOrNull(cfg, `stepImplementationsExcludedInGloosary`)

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

    if (cfg.hasPath(`executionConfigDescriptionKey`)){
      cfg.getString(`executionConfigDescriptionKey`)
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


  def getTags(cfg : Config) : String = getOrNull(cfg, `executionConfigTagsKey`)

  def getNonFatalTags(cfg : Config) : String = getOrNull(cfg, `executionConfigNonFatalTagsKey`)

  def getNonStrictKeywordPrecedence(cfg : Config) : java.util.List[String]  = getStringListOrNull(cfg, `nonStrictKeyWordPrecedenceKey`)


  def getSubStepsFileName(cfg : Config) : String = {
    // it's possible not to have any substeps files..
    getOrNull(cfg, `substepsFileKey`)
  }

  // for use when running a single scenario
  def getScenarioName(cfg : Config) : String = getOrNull(cfg, `scenarioNameKey`)

  def getFeatureFile(cfg : Config) : String = cfg.getString(`featureFileKey`)

  def isStrict(cfg : Config) : Boolean = !cfg.hasPath(`nonStrictKeyWordPrecedenceKey`)


  def isFastFailParseErrors(cfg : Config) : Boolean = {
    getBooleanOr(cfg, `fastFailParseErrorsKey`, true)
  }

  def getBooleanOr(cfg : Config, path: String, default : Boolean) : Boolean = {
    if (cfg.hasPath(path)){
      cfg.getBoolean(path)
    }
    else default
  }

  def isCheckForUncalledAndUnused(cfg : Config) : Boolean = getBooleanOr(cfg, `checkForUncalledAndUnused`, false)

  def getRootDataDir(masterConfig: Config) : File = {
    new File(masterConfig.getString(`rootDataDirKey`))
  }


  def getDataOutputDirectory(cfg : Config) : File = {
    new File(getRootDataDir(cfg), cfg.getString(`executionConfigDataOutputDir`))
  }

  def getReportDir(cfg: Config) : File = {
    new File(cfg.getString(`substepsReportDir`))
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


  def substepsConfig: Config = threadLocalConfig()

  def stepDepthForDescription = substepsConfig.getInt(`stepDepthDescriptionKey`)

  def logUncalledAndUnusedStepImpls = substepsConfig.getBoolean(`logUncallEdAndUnusedStepImplsKey`)

  def prettyPrintReportData = substepsConfig.getBoolean(`prettyPrintReportDataKey`)

//  def reportDataBaseDir = substepsConfig.getString(`reportDataBaseDirKey`)

}
