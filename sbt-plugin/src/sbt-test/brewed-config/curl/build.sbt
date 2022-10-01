enablePlugins(ScalaNativeBrewedConfigPlugin)
enablePlugins(ScalaNativeJUnitPlugin)
nativeBrewFormulas += "curl"
nativeLinkingOptions += "-lcrypto" // this tends to piss off macOS
testOptions += Tests.Argument("-a", "-s", "-v")
