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

import com.armanbilge.scalanative.brew.Brew
import com.armanbilge.scalanative.brew.BrewNativeConfig
import sbt.Keys._
import sbt._

import scala.scalanative.sbtplugin.ScalaNativePlugin
import scala.util.Failure
import scala.util.Success
import scala.util.Try

import ScalaNativePlugin.autoImport._

object ScalaNativeBrewedConfigPlugin extends AutoPlugin {

  override def requires: Plugins = ScalaNativePlugin

  object autoImport {
    lazy val nativeBrew = settingKey[Option[String]]("Path to the brew binary")
    lazy val nativeBrewFormulas = settingKey[Set[String]]("Set of required formulae")
  }
  import autoImport._

  override def globalSettings: Seq[Setting[_]] = Seq(
    nativeBrew := None
  )

  override def buildSettings: Seq[Setting[_]] = Seq(
    nativeBrewFormulas := Set()
  )

  override def projectSettings: Seq[Setting[_]] =
    inConfig(Compile)(perConfigSettings) ++
      inConfig(Test)(perConfigSettings) ++
      inConfig(IntegrationTest)(perConfigSettings)

  private def perConfigSettings =
    Seq(
      nativeConfig := {
        val log = streams.value.log
        val oldConfig = nativeConfig.value
        brewNativeConfig.value match {
          case Success(configurator) => configurator.apply(oldConfig)
          case Failure(_) =>
            val formulas = nativeBrewFormulas.value.mkString(", ")
            log.warn(s"Cannot find brew-installed $formulas.")
            log.warn(
              s"nativeCompileOptions and nativeLinkingOptions must be manually configured."
            )
            oldConfig
        }
      },
      envVars := {
        val log = streams.value.log
        val oldEnv = envVars.value
        brewNativeConfig.value.map(_.ldLibraryPath) match {
          case Success(brewLdLibPath) =>
            val oldLdLibPath = oldEnv.get("LD_LIBRARY_PATH")
            val newLdLibPath = oldLdLibPath.fold(brewLdLibPath)(old => s"$old:$brewLdLibPath")
            oldEnv.updated("LD_LIBRARY_PATH", newLdLibPath)
          case Failure(_) =>
            val formulas = nativeBrewFormulas.value.mkString(", ")
            log.warn(s"Cannot find brew-installed $formulas.")
            log.warn(s"LD_LIBRARY_PATH must be manually configured.")
            oldEnv
        }
      }
    )

  private lazy val brewNativeConfig =
    Def.task[Try[BrewNativeConfig]](
      brew.value.flatMap(brew => Try(BrewNativeConfig(brew, nativeBrewFormulas.value.toList)))
    )

  private lazy val brew =
    Def.task[Try[Brew]](nativeBrew.value.fold(Try(Brew()))(bin => Try(Brew(bin))))

}
