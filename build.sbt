name := "dynamoDBLocal"

version := "0.1"

scalaVersion := "2.12.6"

libraryDependencies ++= Seq(
  "com.amazonaws" % "aws-java-sdk-dynamodb" % "1.11.29",
  "com.typesafe.akka" %% "akka-actor" % "2.5.12",
  "com.typesafe.akka" %% "akka-testkit" % "2.5.12" % Test,
  "com.typesafe.akka" %% "akka-stream" % "2.5.12",
  "org.scalatest" %% "scalatest" % "3.0.5" % "test"
)