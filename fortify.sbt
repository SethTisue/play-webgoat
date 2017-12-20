// this makes it so sbt can resolve the plugin
credentials += Credentials(
  Path.userHome / ".lightbend" / "commercial.credentials")
resolvers += Resolver.url(
  "lightbend-commercial-releases",
  new URL("http://repo.lightbend.com/commercial-releases/"))(
  Resolver.ivyStylePatterns)

// enable the plugin
addCompilerPlugin(
  "com.lightbend" %% "scala-fortify" % "1.0.1"
    classifier "assembly" cross CrossVersion.patch)

// configure the plugin
scalacOptions += "-P:fortify:build=play-webgoat"

// include the .nst files when publishing
val nstTask = taskKey[File]("generate NST files")
nstTask := {
  (compile in Compile).value
  val nstDir =
    file(System.getProperty("user.home")) /
      ".fortify" / "sca17.2" / "build" / "play-webgoat"
  val nstZip = (crossTarget in (Compile, packageBin)).value / "nsts.zip"
  IO.zip(Path.allSubpaths(nstDir), nstZip)
  nstZip
}
addArtifact(
  Artifact("play-webgoat-nsts", "zip", "zip"),
  nstTask
)
