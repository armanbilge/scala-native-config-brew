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

package com.armanbilge.scalanative.brew

import java.nio.file.Path
import scala.scalanative.build.NativeConfig

import BrewNativeConfig._

final class BrewNativeConfig private (cellar: Path, formulas: List[ResolvedFormula]) {

  def apply(nativeConfig: NativeConfig): NativeConfig = {
    nativeConfig
      .withCompileOptions(nativeConfig.compileOptions ++ compileOptions)
      .withLinkingOptions(nativeConfig.linkingOptions ++ linkingOptions)
  }

  def compileOptions: List[String] = includeDirs.map(p => s"-I$p")

  def linkingOptions: List[String] = libDirs.map(p => s"-L$p")

  def ldLibraryPath: String = libDirs.mkString(":")

  private def dirs = formulas.map(_.locate(cellar))
  private def includeDirs = dirs.map(_.resolve("include"))
  private def libDirs = dirs.map(_.resolve("lib"))
}

object BrewNativeConfig {

  def apply(brew: Brew, formulas: List[String]): BrewNativeConfig = {
    val cellar = brew.cellar()
    val resolved = brew.info(formulas).flatMap { info =>
      if (info.installed.isEmpty)
        throw new RuntimeException(s"Formula ${info.fullName} is not installed!")

      val installed = info.installed.head

      val deps = installed.runtimeDependencies.map { dep =>
        new ResolvedFormula(dep.fullName, dep.version)
      }

      new ResolvedFormula(info.fullName, installed.version) :: deps
    }

    new BrewNativeConfig(cellar, resolved.toSet.toList)
  }

  private final class ResolvedFormula(val name: String, val version: String) {
    def locate(cellar: Path): Path =
      cellar.resolve(name).resolve(version)
  }

}
