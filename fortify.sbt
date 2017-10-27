// this makes it so sbt can resolve the plugin
credentials += Credentials(Path.userHome / ".lightbend" / "commercial.credentials")
resolvers += Resolver.url(
  "lightbend-commercial-releases",
  new URL("http://repo.lightbend.com/commercial-releases/"))(
  Resolver.ivyStylePatterns)

// enable & configure the plugin
libraryDependencies += compilerPlugin(
  "com.lightbend" %% "scala-fortify" % "fd6aecb7" classifier "assembly")
scalacOptions += s"-P:fortify:build=play-webgoat"

// configure the plugin

// `translate` task
val translate: TaskKey[Unit] = taskKey("Fortify Translation")
translate := Def.sequential(
  clean in Compile,
  compile in Compile
).value

// `scan` task
val fpr = "scan.fpr"
val scan: TaskKey[Unit] = taskKey("Fortify Scan")
scan := {
  Seq("bash","-c", s"rm -rf ${fpr}").!
  Seq("bash","-c", s"sourceanalyzer -filter filter.txt -f ${fpr} -scan target/*.nst").!
}
