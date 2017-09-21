package org.substeps.config

import java.io.File

import com.technophobia.substeps.model.Configuration
import com.technophobia.substeps.model.exception.SubstepsConfigurationException
import com.typesafe.config._
import org.slf4j.LoggerFactory
import org.substeps.runner.NewSubstepsExecutionConfig

import scala.collection.JavaConverters._

/**
  * A singleton object that will load and resolve a configuration.  Reference.conf's from substep libraries will be merged with any project specific overrides in
  * application.conf (this can be overriden using the standarad typesafe mechanism; -Dconfig.file=/path_to_/alternative.conf).
  * Furthermore, an environment config file will be overlayed, specified using the original -Denvironment variable.  In maven contexts, a config will be
  * constructed to enable the resolution of variables.
  *
  * From the single master config, the config can be split, one whole, self contained config per executionConfigs element.  As part of that split, the baseExecutionConfig element is
  * overlayed with each executionConfig
  *
  * Created by ian on 22/05/17.
  */
object SubstepsConfigLoader {

  val log = LoggerFactory.getLogger("org.substeps.config.SubstepsConfigLoader")
  val options: ConfigRenderOptions = ConfigRenderOptions.defaults.setComments(false).setFormatted(true).setJson(false).setOriginComments(false)

  def render(c: Config): String = {

    val cfg = c.resolve(ConfigResolveOptions.defaults().setAllowUnresolved(true))

    val cleaned =
    if (cfg.hasPath("org.substeps.webdriver.remote.credentials.username")){

      val remoteUserName = cfg.getString("org.substeps.webdriver.remote.credentials.username")
      val remoteToken = cfg.getString("org.substeps.webdriver.remote.credentials.token")

      (cfg.withoutPath("org.substeps.webdriver.remote.credentials")
        .withValue("org.substeps.webdriver.remote.credentials", ConfigValueFactory.fromMap(Map("username" -> "*****", "token" -> "*****").asJava)),
        Some(remoteUserName),
        Some(remoteToken))

    }
    else {
      (cfg, None, None)
    }

    val rendered =
      cleaned match {
        case (cleanedConfig, Some(userName), Some(token)) => {

            cleanedConfig.withOnlyPath("org.substeps")
              .root().render(options).replaceAll(userName, "******").replaceAll(token, "******")

        }
        case   (cleanedConfig, _, _) => {
            cleanedConfig.withOnlyPath("org.substeps")
              .root().render(options)

        }
      }
    rendered
  }

  def loadResolvedConfig(): Config = {

    // TODO - a switch to be able to use another set of defaults - sbt perhaps ?
    loadResolvedConfig(defaultMavenLikeFallbackConfig)
  }

  def loadResolvedConfig(mavenConfigSettings: Config, environmentConfigFile : File): Config = {

    val envConfig = ConfigFactory.parseFile(environmentConfigFile, ConfigParseOptions.defaults().setAllowMissing(true))

    log.debug("Env config from file (" + environmentConfigFile.getAbsolutePath() + "):\n" + SubstepsConfigLoader.render(envConfig))


    loadResolvedConfig(mavenConfigSettings, envConfig)
  }


  def loadResolvedConfig(mavenConfigSettings: Config): Config = {
    val envConfig = loadEnvironmentOverrides()

    loadResolvedConfig(mavenConfigSettings, envConfig)
  }

  private def loadResolvedConfig(mavenConfigSettings: Config, envConfig : Config): Config = {

    val masterCfg =
      ConfigFactory.load(ConfigParseOptions.defaults(), ConfigResolveOptions.noSystem().setAllowUnresolved(true))

    resolveConfig(masterCfg, mavenConfigSettings, envConfig)
  }

  def environmentConfigFile() = {

    val envConfigFile =
      Option(System.getProperty("ENVIRONMENT")) match {
      case None => System.getProperty("environment", "localhost") + ".conf"
      case Some(env) => env + ".conf"
    }

    log.info("Loading environment config file: " + envConfigFile)
    envConfigFile
  }

  def loadEnvironmentOverrides() = {

    val useProps = System.getProperty("substeps.use.dot.properties")
    if (useProps != null && useProps.toBoolean) {
      throw new SubstepsConfigurationException("Using legacy properties has been deprecated, please use a .conf file and HOCON syntax for greater functionality")
    }

    val envConfig = ConfigFactory.parseResources(environmentConfigFile(), ConfigParseOptions.defaults().setAllowMissing(true))

    log.debug("Env config:\n" + SubstepsConfigLoader.render(envConfig))
    envConfig
  }



  def splitMasterConfig(masterConfig: Config): java.util.List[Config] = {

    val exeConfigList = masterConfig.getConfigList("org.substeps.executionConfigs").asScala

    val baseExecutionConfig = masterConfig.getConfig("org.substeps.baseExecutionConfig")

    val baseConfig = masterConfig.withoutPath("org.substeps.executionConfigs").withoutPath("org.substeps.baseExecutionConfig")

    exeConfigList.map(exeCfg => {

      val thisExecConfig = exeCfg.withFallback(baseExecutionConfig)

      baseConfig.withValue("org.substeps.executionConfig", thisExecConfig.root()).resolve()
    }).toList.asJava

  }

  def resolveConfig(initialMasterConfig: Config, mavenConfigSettings: Config, envConfig: Config): Config = {

    log.debug("resolveConfig")

    val masterConfig = envConfig.withFallback(initialMasterConfig).resolve(ConfigResolveOptions.defaults().setAllowUnresolved(true))

    log.debug("masterConfig:\n" + SubstepsConfigLoader.render(masterConfig))

    val baseExecutionConfig = masterConfig.getConfig("org.substeps.baseExecutionConfig")

    val resolvedExecutionConfigs =

    if (masterConfig.hasPath("org.substeps.executionConfigs")){
      val exeConfigList = masterConfig.getConfigList("org.substeps.executionConfigs").asScala

      exeConfigList.map(exeCfg => {
        // local exe config blocks should override the base.
        val thisExecConfig = exeCfg.withFallback(baseExecutionConfig)

        thisExecConfig.resolve(ConfigResolveOptions.defaults().setAllowUnresolved(true)).root()

      })
    }
    else {
      List(baseExecutionConfig.resolve(ConfigResolveOptions.defaults().setAllowUnresolved(true)).root())
    }


    masterConfig.withoutPath("org.substeps.executionConfigs")
    .withValue("org.substeps.executionConfigs", ConfigValueFactory.fromIterable(resolvedExecutionConfigs.toList.asJava))
    .resolve(ConfigResolveOptions.defaults().setAllowUnresolved(true))
    .resolveWith(mavenConfigSettings, ConfigResolveOptions.defaults().setAllowUnresolved(true))
  }

  def defaultMavenLikeFallbackConfig : Config = {
    buildMavenFallbackConfig("target", ".", "target")
  }

  def buildMavenFallbackConfig(projectBuildDir : String, baseDir : String, testOutputDir : String ) : Config= {
      ConfigFactory.empty.withValue("project.build.directory", ConfigValueFactory.fromAnyRef(projectBuildDir))
      .withValue("basedir", ConfigValueFactory.fromAnyRef(baseDir))
        .withValue("project.build.testOutputDirectory", ConfigValueFactory.fromAnyRef(testOutputDir))
          .withValue("project.build.outputDirectory", ConfigValueFactory.fromAnyRef(projectBuildDir))
  }


}
