enablePlugins(ScalaNativeBrewedConfigPlugin)
enablePlugins(ScalaNativeJUnitPlugin)
nativeBrewFormulas += "curl"
testOptions += Tests.Argument("-a", "-s", "-v")
