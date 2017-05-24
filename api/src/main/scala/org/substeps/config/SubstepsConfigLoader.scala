package org.substeps.config

import com.typesafe.config._
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

  val options: ConfigRenderOptions = ConfigRenderOptions.defaults.setComments(false).setFormatted(true).setJson(false).setOriginComments(false)

  def render(cfg: Config): String = cfg.withOnlyPath("org.substeps").root().render(options)

  def loadResolvedConfig(): Config = {
    loadResolvedConfig(ConfigFactory.empty())
  }

    def loadResolvedConfig(mavenConfigSettings: Config): Config = {
    val environment = System.getProperty("ENVIRONMENT", "localhost") + ".conf"
    loadResolvedConfig(environment, mavenConfigSettings)
  }


  def loadResolvedConfig(environmentOverrides: String, mavenConfigSettings: Config): Config = {

    val envConfig = ConfigFactory.parseResources(environmentOverrides, ConfigParseOptions.defaults().setAllowMissing(true))

    val masterCfg =
      ConfigFactory.load(ConfigParseOptions.defaults(), ConfigResolveOptions.noSystem().setAllowUnresolved(true))

    resolveConfig(masterCfg, mavenConfigSettings, envConfig)
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

    val masterConfig = envConfig.withFallback(initialMasterConfig)

    val exeConfigList = masterConfig.getConfigList("org.substeps.executionConfigs").asScala

    val baseExecutionConfig = masterConfig.getConfig("org.substeps.baseExecutionConfig")

    val resolvedExecutionConfigs =
      exeConfigList.map(exeCfg => {

        val thisExecConfig = exeCfg.withFallback(baseExecutionConfig)

        thisExecConfig.resolve(ConfigResolveOptions.defaults().setAllowUnresolved(true)).root()

      }).toList.asJava

    masterConfig.withoutPath("org.substeps.executionConfigs")
      .withValue("org.substeps.executionConfigs", ConfigValueFactory.fromIterable(resolvedExecutionConfigs)).resolveWith(mavenConfigSettings)

  }


}
