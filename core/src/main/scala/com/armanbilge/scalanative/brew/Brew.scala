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

import io.circe.Decoder
import io.circe.jawn

import java.nio.file.Path
import java.nio.file.Paths
import scala.scalanative.build.Platform
import scala.sys.process._

import Brew._

final class Brew private (bin: String) {

  def cellar(): Path = Paths.get(Process(List(bin, "--cellar")).!!)

  def info(formulas: List[String]): List[FormulaInfo] =
    jawn
      .decode[List[FormulaInfo]](Process(List(bin, "info", "--json") ++ formulas).!!)
      .toTry
      .get

}

object Brew {

  def apply(): Brew = {
    val bin =
      if (Platform.isMac) {
        val isArm =
          Option(System.getProperty("os.arch")).exists(_.toLowerCase().contains("aarch64"))
        if (isArm)
          "/opt/homebrew/bin/brew"
        else
          "/usr/local/bin/brew"
      } else if (Platform.isLinux) "/home/linuxbrew/.linuxbrew/bin/brew"
      else throw new RuntimeException("unsupported OS")

    apply(bin)
  }

  def apply(bin: String): Brew = new Brew(bin)

  final class FormulaInfo private (val fullName: String, val installed: List[InstalledFormula])
  object FormulaInfo {
    implicit def decoder: Decoder[FormulaInfo] =
      Decoder.forProduct2("full_name", "installed")(new FormulaInfo(_, _))
  }

  final class InstalledFormula private (
      val version: String,
      val runtimeDependencies: List[RuntimeDependency]
  )
  object InstalledFormula {
    implicit def decoder: Decoder[InstalledFormula] =
      Decoder.forProduct2("version", "runtime_dependencies")(new InstalledFormula(_, _))
  }

  final class RuntimeDependency private (val fullName: String, val version: String)
  object RuntimeDependency {
    implicit def decoder: Decoder[RuntimeDependency] =
      Decoder.forProduct2("full_name", "version")(new RuntimeDependency(_, _))
  }

}
