ThisBuild / tlBaseVersion := "0.0"

ThisBuild / organization := "com.armanbilge"
ThisBuild / organizationName := "Arman Bilge"
ThisBuild / developers += tlGitHubDev("armanbilge", "Arman Bilge")
ThisBuild / startYear := Some(2022)
ThisBuild / tlSonatypeUseLegacyHost := false

ThisBuild / githubWorkflowBuildSbtStepPreamble := Seq()

val scala2_12 = "2.12.16"
val scala2_13 = "2.13.8"
val scala3 = "3.1.3"
val scalaNativeVersion = "0.4.7"

lazy val root = project.in(file(".")).aggregate(core2_12, core2_13, core3, sbtPlugin, ghaPlugin)

lazy val core = projectMatrix
  .in(file("core"))
  .settings(
    name := "scala-native-config-brew",
    libraryDependencies ++= Seq(
      "org.scala-native" %% "tools" % scalaNativeVersion
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
    addSbtPlugin("org.typelevel" % "sbt-typelevel-github-actions" % "0.4.15")
  )
