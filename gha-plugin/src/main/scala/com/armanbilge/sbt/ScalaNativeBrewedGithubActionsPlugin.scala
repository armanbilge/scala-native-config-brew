/*
 * Copyright 2022 Arman Bilge
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.armanbilge.sbt

import org.typelevel.sbt.gha.GenerativeKeys._
import org.typelevel.sbt.gha.GenerativePlugin
import org.typelevel.sbt.gha.WorkflowStep
import sbt._

import ScalaNativeBrewedConfigPlugin.autoImport._

object ScalaNativeBrewedGithubActionsPlugin extends AutoPlugin {

  override def requires: Plugins = ScalaNativeBrewedConfigPlugin && GenerativePlugin

  override def trigger: PluginTrigger = allRequirements

  object autoImport {
    lazy val nativeBrewInstallWorkflowSteps =
      settingKey[Seq[WorkflowStep]]("Workflow step that installs all the necessary formulae")
  }

  import autoImport._

  private val nativeBrewAllTheFormulas = settingKey[Set[String]]("All the formulae")

  override def buildSettings: Seq[Setting[_]] = Seq(
    nativeBrewAllTheFormulas := Set.empty,
    nativeBrewInstallWorkflowSteps := {
      val oses = githubWorkflowOSes.value
      val formulas = nativeBrewAllTheFormulas.value.toList.sorted

      val linuxBrew = "/home/linuxbrew/.linuxbrew/bin/brew"

      val ubuntuStep =
        if (oses.exists(_.contains("ubuntu")))
          List(
            WorkflowStep.Run(
              List(s"$linuxBrew install ${formulas.mkString(" ")}"),
              name = Some("Install brew formulae (ubuntu)"),
              cond = Some("startsWith(matrix.os, 'ubuntu')")
            )
          )
        else Nil

      val macosStep =
        if (oses.exists(_.contains("macos")))
          List(
            WorkflowStep.Run(
              List(s"brew install ${formulas.mkString(" ")}"),
              name = Some("Install brew formulae (macOS)"),
              cond = Some("startsWith(matrix.os, 'macos')")
            )
          )
        else Nil

      ubuntuStep ++ macosStep
    }
  )

  override def projectSettings: Seq[Setting[_]] =
    inConfig(Compile)(perConfigSettings) ++
      inConfig(Test)(perConfigSettings) ++
      inConfig(IntegrationTest)(perConfigSettings)

  private def perConfigSettings = Seq(
    ThisBuild / Zero / nativeBrewAllTheFormulas ++= nativeBrewFormulas.value
  )

}
