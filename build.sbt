import sbtassembly.AssemblyKeys


name := "BbkProject"

version := "1.08"

scalaVersion := "2.11.7"

libraryDependencies += "org.apache.httpcomponents" % "httpclient" % "4.5"

libraryDependencies += "org.mongodb" %% "casbah" % "2.8.1"

libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.3.9"

libraryDependencies += "org.apache.commons" % "commons-math3" % "3.5"


// Spray dependencies
libraryDependencies ++= {
  val akkaV = "2.3.9"
  val sprayV = "1.3.3"
  Seq(
    "io.spray"            %%  "spray-can"     % sprayV,
    "io.spray"            %%  "spray-routing" % sprayV,
    "io.spray"            %%  "spray-testkit" % sprayV  % "test",
  //  "io.spray"            %%  "spray-json"    % "1.3.2",
    "com.typesafe.akka"   %%  "akka-actor"    % akkaV,
    "com.typesafe.akka"   %%  "akka-testkit"  % akkaV   % "test",
    "org.specs2"          %%  "specs2-core"   % "2.3.11" % "test"
  )
}

libraryDependencies ++= Seq("org.slf4j" % "slf4j-api" % "1.7.5",
  "org.slf4j" % "slf4j-simple" % "1.7.5",
  "org.clapper" %% "grizzled-slf4j" % "1.0.2")

libraryDependencies += "org.json4s" %% "json4s-native" % "3.2.10"

libraryDependencies += "org.codehaus.mojo" % "ideauidesigner-maven-plugin" % "1.0-beta-1"

mainClass in assembly := Some("com.PredictionAlgorithm.Main")

mainClass in (Compile, run) := Some("com.PredictionAlgorithm.Main")


assemblyMergeStrategy in assembly := {
  case PathList(ps @ _*) if ps.contains("uiDesigner") => MergeStrategy.first
  case x =>
    val oldStrategy = (assemblyMergeStrategy in assembly).value
    oldStrategy(x)
}