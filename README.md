# scala-native-config-brew

[Homebrew](https://brew.sh/) your Scala Native config! An sbt plugin that configures `compileOptions`, `linkingOptions`, and `LD_LIBRARY_PATH` to point to brew-installed libraries.

### Install

```scala
addSbtPlugin("com.armanbilge" % "sbt-scala-native-config-brew" % "<version>")
```

### Usage

```scala
enablePlugins(ScalaNativeBrewedConfigPlugin)
nativeBrewFormulas += "curl"
```

The plugin has two additional requirements:
1. brew is already installed in your system. Use the `nativeBrew` setting to point it at a non-default location.
2. The desired formulae have already been installed. The plugin will not install them for you; it only configures Scala Native to use an existing installation.

Future releases may introduce capabilities to automatically install formulae and self-bootstrap without an existing brew installation. Please open an issue if these features are important to you.

### GitHub Actions

An additional plugin makes available a `nativeBrewInstallWorkflowStep` for use with [sbt-typelevel-github-actions](https://typelevel.org/sbt-typelevel/gha.html).

```scala
addSbtPlugin("com.armanbilge" % "sbt-scala-native-config-brew-github-actions" % "<version>")
```
