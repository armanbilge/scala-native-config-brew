# scala-native-config-brew

[Homebrew](https://brew.sh/) your Scala Native config! An sbt plugin that configures `compileOptions`, `linkingOptions`, and `LD_LIBRARY_PATH` to point to brew-installed libraries.

### Install

```scala
addSbtPlugin("com.armanbilge" % "sbt-scala-native-config-brew" % "0.1.0")
```

### Usage

```scala
enablePlugins(ScalaNativeBrewedConfigPlugin)
nativeBrewFormulas += "curl"
```

The plugin has two additional requirements:
1. brew is already installed in your system. Use the `nativeBrew` setting to point it at a non-default location.
2. The desired formulae have already been installed. The plugin will not install them for you; it only configures Scala Native to use an existing installation.

However, these are only soft requirements: if brew or the requested formulae are not installed, the plugin will log a warning and not update any configuration. So it will not interfere with a user who does not have or want brew.

Future releases may introduce capabilities to automatically install formulae and self-bootstrap without an existing brew installation. Please open an issue if these features are important to you.

### GitHub Actions

An additional plugin integrates with [sbt-typelevel-github-actions](https://typelevel.org/sbt-typelevel/gha.html).

```scala
addSbtPlugin("com.armanbilge" % "sbt-scala-native-config-brew-github-actions" % "0.1.0")

ThisBuild / githubWorkflowBuildPreamble ++= nativeBrewInstallWorkflowSteps.value
```

### Mill

The core logic is published for Scala 2.12, 2.13, and 3, independently of sbt. This should facilitate the development of a Mill plugin.

```
com.armanbilge::scala-native-config-brew:0.1.0
```

### See also

The much more ambitious [sbt-vcpkg](sbt-vcpkg) project.
