package org.substeps.config

import com.typesafe.config.ConfigFactory
import org.scalatest.{FlatSpec, ShouldMatchers}
import org.substeps.runner.NewSubstepsExecutionConfig

/**
  * Created by ian on 03/04/17.
  */
class ConfigTest extends FlatSpec with ShouldMatchers{

  "loading config" must "use legacy values as fallback" in {

    val cfg = ConfigFactory.load("test-conf-with-defaults.conf")

    println("cfg root:\n" + NewSubstepsExecutionConfig.render(cfg))

    val substepsCfg = cfg.getConfig("org.substeps")

    NewSubstepsExecutionConfig.checkMasterConfigForLegacyDefaults(cfg)

    println("#8888888888888888888\n\nsubstepsCfg root:\n" + NewSubstepsExecutionConfig.render(substepsCfg))


  }
}
