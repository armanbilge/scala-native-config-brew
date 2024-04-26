enablePlugins(ScalaNativeBrewedConfigPlugin)
enablePlugins(ScalaNativeJUnitPlugin)
nativeBrewFormulas += "curl"

Compile / nativeConfig := {
  val nc = nativeConfig.value
  nc.withLinkingOptions(
    nc.linkingOptions :+ "-lcrypto" // this tends to piss off macOS
  )
}

testOptions += Tests.Argument("-a", "-s", "-v")
