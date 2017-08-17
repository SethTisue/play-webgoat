credentials += Credentials(Path.userHome / ".lightbend" / "commercial.credentials")
resolvers += Resolver.url(
  "lightbend-commercial-releases",
  new URL("http://repo.lightbend.com/commercial-releases/"))(
  Resolver.ivyStylePatterns)

val FortifyConfig = config("fortify").extend(Compile).hide
inConfig(FortifyConfig)(Defaults.compileSettings)

val fortifyJar = taskKey[File]("JAR containing scala-fortify compiler plugin")
fortifyJar := new java.io.File(
  "/Users/tisue/.ivy2/cache/" +
    "com.lightbend/scala-fortify_2.12/jars/scala-fortify_2.12-e940f40a-assembly.jar")

sources in FortifyConfig := (sources in Compile).value

scalacOptions in FortifyConfig += s"-Xplugin:${fortifyJar.value}"
scalacOptions in FortifyConfig += s"-Xplugin-require:fortify"
scalacOptions in FortifyConfig += s"-P:fortify:out=${target.value}"
scalacOptions in FortifyConfig += "-Ystop-before:jvm"

val translateCommand = Command.command("translate") { (state: State) =>
  Project.runTask(clean in Compile, state)
  Project.runTask(compile in FortifyConfig, state)
  state
}
val scanCommand = Command.command("scan") { (state: State) =>
  val fpr = "scan.fpr"
  IO.delete(new java.io.File(fpr))
  val targetDir = {
    val extracted: Extracted = Project.extract(state)
    import extracted._
    val thisScope = Load.projectScope(currentRef)
      (target in thisScope get extracted.structure.data).get
  }
  val nstFiles = (targetDir ** "*.nst").get.map(_.toString)
    (Seq("sourceanalyzer", "-filter", "filter.txt", "-f", fpr, "-scan") ++ nstFiles).!
    state
}

commands ++= Seq(translateCommand, scanCommand)

//ivyConfigurations += FortifyConfig

/*
fortifyJar := {
  val deps =
    (libraryDependencies in FortifyConfig).value
      .filter(_.configurations.fold(false)(_.startsWith("plugin->")))
  update.value.configuration("plugin").map(_.modules).getOrElse(Nil).filter { m =>
    deps.foreach(println)
    deps.exists { d =>
      d.organization == m.module.organization &&
        d.name         == m.module.name &&    // if we use %% below then one has _2.12 and one doesn't?!
        d.revision     == m.module.revision
    }
  }.flatMap(_.artifacts.map(_._2)).head
}
 */

// autoCompilerPlugins := false
// autoCompilerPlugins in FortifyConfig := true
// libraryDependencies +=
//   (compilerPlugin("com.lightbend" % "scala-fortify_2.12" % "e940f40a" % FortifyConfig)
//     classifier "assembly"
//     exclude("com.typesafe.conductr", "ent-suite-licenses-parser")
//     exclude("default", "scala-st-nodes"))
