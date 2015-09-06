import sbtassembly.AssemblyKeys


name := "BbkProject"

version := "2.14"

scalaVersion := "2.11.7"

resolvers += "Akka Snapshot Repository" at "http://repo.akka.io/snapshots/"

libraryDependencies += "org.apache.httpcomponents" % "httpclient" % "4.5"

libraryDependencies += "org.mongodb" %% "casbah" % "2.8.2"


libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.4-SNAPSHOT"

libraryDependencies += "org.apache.commons" % "commons-math3" % "3.5"

libraryDependencies += "org.apache.commons" % "commons-lang3" % "3.4"

// Spray dependencies
libraryDependencies ++= {
  val sprayV = "1.3.3"
  Seq(
    "io.spray"            %%  "spray-can"     % sprayV,
    "io.spray"            %%  "spray-routing-shapeless2" % sprayV
   // "io.spray"            %%  "spray-testkit" % sprayV  % "test",
  //  "org.specs2"          %%  "specs2-core"   % "2.3.11" % "test"
  )
}

libraryDependencies += "com.wandoulabs.akka" %% "spray-websocket" % "0.1.4"

//libraryDependencies ++= Seq("org.slf4j" % "slf4j-api" % "1.7.5",
 // "org.slf4j" % "slf4j-simple" % "1.7.5",
 // "org.clapper" %% "grizzled-slf4j" % "1.0.2")

libraryDependencies += "org.json4s" %% "json4s-native" % "3.2.10"

libraryDependencies += "org.codehaus.mojo" % "ideauidesigner-maven-plugin" % "1.0-beta-1"

libraryDependencies += "me.lessis" %% "courier" % "0.1.3"

mainClass in assembly := Some("com.predictionalgorithm.Main")

mainClass in (Compile, run) := Some("com.predictionalgorithm.Main")


assemblyMergeStrategy in assembly := {
  case PathList(ps @ _*) if ps.contains("uiDesigner") => MergeStrategy.first
  case x =>
    val oldStrategy = (assemblyMergeStrategy in assembly).value
    oldStrategy(x)
}