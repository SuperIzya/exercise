name := "tokagroup-exercise"

version := "0.1"
scalaVersion := "2.12.7"

enablePlugins(PackPlugin)

lazy val akkaVersion = "2.5.18"

packMain := Map("watchZK" -> "com.tokagroup.exercise.Main")

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-http" % "10.1.5",
  "com.typesafe.akka" %% "akka-stream" % akkaVersion,
  "ch.qos.logback" % "logback-core" % "1.2.3",
  "ch.megard" %% "akka-http-cors" % "0.3.1",
  "com.typesafe.play" %% "play-json" % "2.6.10",
  "org.apache.zookeeper" % "zookeeper" % "3.3.4" excludeAll(
    ExclusionRule(organization = "javax.jms", name = "jms"),
    ExclusionRule(organization = "com.sun.jdmk", name = "jmxtools"),
    ExclusionRule(organization = "com.sun.jmx", name = "jmxri")
  )
)