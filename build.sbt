import sbt.Keys.libraryDependencies

ThisBuild / resolvers ++= Seq(
    "Apache Development Snapshot Repository" at "https://repository.apache.org/content/repositories/snapshots/",
    Resolver.mavenLocal,
    "Confluent Repository" at "http://packages.confluent.io/maven/"
)

name := "Streamy"
version := "0.1-SNAPSHOT"
organization := "com.abhioncbr"
ThisBuild / scalaVersion := "2.11.12"

val versions = new {
    val jsonPath = "2.0.0"
    val playVersion = "2.7.0"
    val scoptVersion = "3.5.0"
    val flinkVersion = "1.7.1"
    val jacksonVersion = "2.6.0"
    val graphLibVersion = "1.12.5"
    val circeYamlVersion = "0.8.0"
}


val allDependencies = Seq(
    // Flink libraries
    "org.apache.flink" %% "flink-table" % versions.flinkVersion,
    "org.apache.flink" %% "flink-connector-kafka" % versions.flinkVersion,
    "org.apache.flink" %% "flink-scala" % versions.flinkVersion % "provided" ,
    "org.apache.flink" %% "flink-statebackend-rocksdb" % versions.flinkVersion,
    "org.apache.flink" %% "flink-streaming-scala" % versions.flinkVersion % "provided",
    "org.apache.flink" % "flink-avro-confluent-registry" % versions.flinkVersion
            exclude("com.fasterxml.jackson.core", "jackson-databind"),

    // jackson libraries
    "com.fasterxml.jackson.core" % "jackson-databind" % versions.jacksonVersion,
    "com.fasterxml.jackson.module" %% "jackson-module-scala" % versions.jacksonVersion,
    "com.fasterxml.jackson.datatype" % "jackson-datatype-joda" % versions.jacksonVersion,

    // required for confluent kafka avro schema
    "org.apache.avro" % "avro" % "1.8.2",
    "io.confluent" % "kafka-avro-serializer" % "4.1.1" exclude("com.fasterxml.jackson.core", "jackson-databind"),

    // job param config library
    "com.github.scopt" %% "scopt" % versions.scoptVersion,

    // yaml parsing libraries
    "io.circe" %% "circe-yaml" % versions.circeYamlVersion,
    "io.circe" %% "circe-generic" % versions.circeYamlVersion,

    // scala graph processing library
    "org.scala-graph" %% "graph-core" % versions.graphLibVersion,

    // required for unit test cases
    "org.scalatest" %% "scalatest" % "3.0.1" % Test,
    "org.apache.flink" % "flink-test-utils_2.11" % "1.6.1" % Test,

    // required for logging
    "org.slf4j" % "slf4j-api" % "1.7.21" % Test,
    "org.slf4j" % "slf4j-simple" % "1.7.21" % Test,

    // require for json parsing
    "com.typesafe.play" %% "play-json" % versions.playVersion,
    "com.jayway.jsonpath" % "json-path" % versions.jsonPath
)

lazy val root = (project in file(".")).
        settings(
            libraryDependencies ++= allDependencies
        )

assembly / mainClass := Some("com.abhioncbr.Streamy.LaunchStreamy")

// make run command include the provided dependencies
Compile / run  := Defaults.runTask(Compile / fullClasspath,
    Compile / run / mainClass,
    Compile / run / runner
).evaluated

// stays inside the sbt console when we press "ctrl-c" while a Flink programme executes with "run" or "runMain"
Compile / run / fork := true
Global / cancelable := true

parallelExecution       in    Test      := false

assemblyMergeStrategy in assembly := {
    case PathList("io", "confluent", xs @ _*) => MergeStrategy.last
    case x =>
        val oldStrategy = (assemblyMergeStrategy in assembly).value
        oldStrategy(x)
}

// exclude Scala library from assembly
assembly / assemblyOption  := (assembly / assemblyOption).value.copy(includeScala = false)
