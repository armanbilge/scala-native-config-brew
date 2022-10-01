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

import scala.scalanative.sbtplugin.ScalaNativePlugin

import ScalaNativePlugin.autoImport._
import sbt._
import sbt.Keys._
import com.armanbilge.scalanative.brew.Brew
import com.armanbilge.scalanative.brew.BrewNativeConfig

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
      nativeConfig := brewNativeConfig.value(nativeConfig.value),
      envVars := {
        val brewLdLibPath = brewNativeConfig.value.ldLibraryPath
        val oldEnv = envVars.value
        val oldLdLibPath = oldEnv.get("LD_LIBRARY_PATH")
        val newLdLibPath = oldLdLibPath.fold(brewLdLibPath)(old => s"$old:$brewLdLibPath")
        oldEnv.updated("LD_LIBRARY_PATH", newLdLibPath)
      }
    )

  private lazy val brewNativeConfig =
    Def.task[BrewNativeConfig](BrewNativeConfig(brew.value, nativeBrewFormulas.value.toList))

  private lazy val brew = Def.task[Brew](nativeBrew.value.fold(Brew())(Brew(_)))

}
