name := "dynamoDBLocal"

version := "0.1"

scalaVersion := "2.12.6"

lazy val infrastructure = (project in file("infrastructure")).settings(
  libraryDependencies ++= Seq(
    "com.typesafe.slick" %% "slick" % "3.2.0",
    "com.h2database" % "h2" % "1.4.193",
    "org.slf4j" % "slf4j-nop" % "1.7.12"
  )
).dependsOn(application)

lazy val application = (project in file("application")).settings(
  libraryDependencies ++= Seq(
    "com.amazonaws" % "aws-java-sdk-dynamodb" % "1.11.29",
    "com.typesafe.akka" %% "akka-http"   % "10.1.5",
    "com.typesafe.akka" %% "akka-http-core" % "10.1.5",
    "com.typesafe.akka" %% "akka-http-spray-json" % "10.1.5",
    "com.typesafe.akka" %% "akka-testkit" % "2.5.12" % Test,
    "com.typesafe.akka" %% "akka-stream" % "2.5.12",
    "com.typesafe" % "config" % "1.3.3",
    "org.scalatest" %% "scalatest" % "3.0.5" % "test",
    "org.specs2" %% "specs2-core" % "4.2.0" % "test",
    "org.sangria-graphql" %% "sangria-circe" % "1.2.1",
    "org.sangria-graphql" %% "sangria" % "1.4.2",
    "org.sangria-graphql" %% "sangria-spray-json" % "1.0.0",
    "org.sangria-graphql" %% "sangria-slowlog" % "0.1.8"
  )
).dependsOn(domain)

lazy val domain = (project in file("domain")).settings(
  libraryDependencies ++= Seq(
    "com.typesafe.akka" %% "akka-actor" % "2.5.12"
  )
)
