// this makes it so sbt can resolve the plugin
credentials += Credentials(
  Path.userHome / ".lightbend" / "commercial.credentials")
resolvers += "lightbend-commercial-releases" at
  "https://repo.lightbend.com/commercial-releases/"

// enable the plugin
addCompilerPlugin(
  "com.lightbend" %% "scala-fortify" % "1.0.7"
    classifier "assembly" cross CrossVersion.patch)

// configure the plugin
scalacOptions += "-P:fortify:build=play-webgoat"
scalacOptions += "-P:fortify:scaversion=18.1"
