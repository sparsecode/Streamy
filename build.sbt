/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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

val artifactsGroupIds = new {
val flinkGroupId = "org.apache.flink"
}

val classLiteral = "LaunchStreamy"
val mainClassLiteral = s"$organization.$name.$classLiteral"

val allDependencies = Seq(
// Flink libraries
artifactsGroupIds.flinkGroupId %% "flink-table" % versions.flinkVersion,
artifactsGroupIds.flinkGroupId %% "flink-connector-kafka" % versions.flinkVersion,
artifactsGroupIds.flinkGroupId %% "flink-scala" % versions.flinkVersion % "provided",
artifactsGroupIds.flinkGroupId %% "flink-statebackend-rocksdb" % versions.flinkVersion,
artifactsGroupIds.flinkGroupId %% "flink-streaming-scala" % versions.flinkVersion % "provided",
artifactsGroupIds.flinkGroupId % "flink-avro-confluent-registry" % versions.flinkVersion
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
artifactsGroupIds.flinkGroupId % "flink-test-utils_2.11" % "1.6.1" % Test,

// required for logging
"org.slf4j" % "slf4j-api" % "1.7.21" % Test,
"org.slf4j" % "slf4j-simple" % "1.7.21" % Test,

// require for json parsing
"com.typesafe.play" %% "play-json" % versions.playVersion,
"com.jayway.jsonpath" % "json-path" % versions.jsonPath
)

lazy val root = (project in file(".")).settings(libraryDependencies ++= allDependencies)
assembly / mainClass := Some(mainClassLiteral)

// make run command include the provided dependencies
Compile / run  := Defaults.runTask(Compile / test / fullClasspath,
Compile / run / mainClass,
Compile / run / runner
).evaluated

// stays inside the sbt console when we press "ctrl-c" while a Flink programme executes with "run" or "runMain"
Compile / run / fork := true
Global / cancelable := true
coverageEnabled := true

parallelExecution in Test  := false

assemblyMergeStrategy in assembly := {
case PathList("io", "confluent", _ @ _*) => MergeStrategy.last
case x: Any => val oldStrategy = (assemblyMergeStrategy in assembly).value
oldStrategy(x)
}

// exclude Scala library from assembly
assembly / assemblyOption := (assembly / assemblyOption).value.copy(includeScala = false)
