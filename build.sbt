ThisBuild / tlBaseVersion := "0.2"

ThisBuild / organization := "com.armanbilge"
ThisBuild / organizationName := "Arman Bilge"
ThisBuild / developers += tlGitHubDev("armanbilge", "Arman Bilge")
ThisBuild / startYear := Some(2022)
ThisBuild / tlSonatypeUseLegacyHost := false

ThisBuild / githubWorkflowOSes :=
  Seq("ubuntu-22.04", "macos-11", "macos-12")

ThisBuild / githubWorkflowBuildSbtStepPreamble := Seq()

ThisBuild / githubWorkflowBuildPreamble += {
  val brew = "/home/linuxbrew/.linuxbrew/bin/brew"
  WorkflowStep.Run(
    List(s"$brew install curl"),
    name = Some("Install curl"),
    cond = Some("startsWith(matrix.os, 'ubuntu')")
  )
}

val scala2_12 = "2.12.18"
val scala2_13 = "2.13.12"
val scala3 = "3.3.1"
val scalaNativeVersion = "0.4.14"

lazy val root = project
  .in(file("."))
  .aggregate(core2_12, core2_13, core3, sbtPlugin, ghaPlugin)
  .enablePlugins(NoPublishPlugin)

lazy val core = projectMatrix
  .in(file("core"))
  .settings(
    name := "scala-native-config-brew",
    libraryDependencies ++= Seq(
      "org.scala-native" %% "tools" % scalaNativeVersion,
      "io.circe" %% "circe-jawn" % "0.14.6"
    )
  )
  .defaultAxes(VirtualAxis.jvm)
  .jvmPlatform(scalaVersions = Seq(scala2_12, scala2_13, scala3))

lazy val core2_12 = core.jvm(scala2_12)
lazy val core2_13 = core.jvm(scala2_13)
lazy val core3 = core.jvm(scala3)

lazy val sbtPlugin = project
  .in(file("sbt-plugin"))
  .enablePlugins(SbtPlugin)
  .dependsOn(core2_12)
  .settings(
    name := "sbt-scala-native-config-brew",
    addSbtPlugin("org.scala-native" % "sbt-scala-native" % scalaNativeVersion),
    Test / test := {
      scripted.toTask("").value
    },
    scriptedBufferLog := false,
    scriptedLaunchOpts := {
      scriptedLaunchOpts.value ++ Seq("-Xmx1024M", "-Dplugin.version=" + version.value)
    },
    scripted := scripted.dependsOn(core2_12 / publishLocal).evaluated
  )

lazy val ghaPlugin = project
  .in(file("gha-plugin"))
  .enablePlugins(SbtPlugin)
  .dependsOn(sbtPlugin)
  .settings(
    name := "sbt-scala-native-config-brew-github-actions",
    addSbtPlugin("org.typelevel" % "sbt-typelevel-github-actions" % "0.6.6")
  )
