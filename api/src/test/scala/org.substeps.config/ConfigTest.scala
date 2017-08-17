package org.substeps.config

import java.util
import java.util.List

import com.typesafe.config.{Config, ConfigFactory, ConfigRenderOptions}
import org.hamcrest.core.IsNot.not
import org.junit.Assert.assertThat
import org.scalatest.{FlatSpec, Matchers}
import org.substeps.runner.NewSubstepsExecutionConfig

/**
  * Created by ian on 03/04/17.
  */
class ConfigTest extends FlatSpec with Matchers{

  "loading config" must "use legacy values as fallback" in {

    val cfg = ConfigFactory.load("test-conf-with-defaults.conf")

//    println("cfg root:\n" + NewSubstepsExecutionConfig.render(cfg))

    val substepsCfg = cfg.getConfig("org.substeps")

    NewSubstepsExecutionConfig.checkMasterConfigForLegacyDefaults(cfg)

    println("#8888888888888888888\n\nsubstepsCfg root:\n" + SubstepsConfigLoader.render(substepsCfg))


  }

  "rendering of config" must "not include remote credentials" in {


      System.setProperty("environment", "travis")
      System.setProperty("SAUCE_USERNAME", "saucelabsuser")
      System.setProperty("SAUCE_ACCESS_KEY", "access-token")
      val cfg: Config = SubstepsConfigLoader.loadResolvedConfig
      val options: ConfigRenderOptions = ConfigRenderOptions.defaults.setComments(false).setFormatted(true).setJson(false).setOriginComments(false)
      val cfgString: String = cfg.root.withoutKey("awt").withoutKey("java").withoutKey("line").withoutKey("os").withoutKey("sun").withoutKey("user").render(options)
      val configs: util.List[Config] = SubstepsConfigLoader.splitMasterConfig(cfg)
      val renderedConfig: String = SubstepsConfigLoader.render(cfg)

      System.out.println("MASTER CFG:\n" + renderedConfig + "\n\n\n\n\n")

      renderedConfig should not contain ("saucelabsuser")
      renderedConfig should not contain ("access-token")

  }
}
