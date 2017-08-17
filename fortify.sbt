credentials += Credentials(Path.userHome / ".lightbend" / "commercial.credentials")
resolvers += Resolver.url(
  "lightbend-commercial-releases",
  new URL("http://repo.lightbend.com/commercial-releases/"))(
  Resolver.ivyStylePatterns)

val FortifyConfig = config("fortify").extend(Compile).hide
inConfig(FortifyConfig)(Defaults.compileSettings)

val fortifyJar = taskKey[File]("JAR containing scala-fortify compiler plugin")
fortifyJar := {
  val deps =
    libraryDependencies.value
      .filter(_.configurations.fold(false)(_.startsWith(FortifyConfig.name)))
  update.value.configuration(FortifyConfig.name).map(_.modules).getOrElse(Nil).filter { m =>
    deps.exists { d =>
      d.organization == m.module.organization &&
        s"${d.name}_${scalaBinaryVersion.value}" == m.module.name &&
        d.revision == m.module.revision
    }
  }.flatMap(_.artifacts.map(_._2)).head
}

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

ivyConfigurations += FortifyConfig

libraryDependencies +=
  ("com.lightbend" %% "scala-fortify" % "e940f40a" % FortifyConfig
    classifier "assembly"
    exclude("com.typesafe.conductr", "ent-suite-licenses-parser")
    exclude("default", "scala-st-nodes"))
