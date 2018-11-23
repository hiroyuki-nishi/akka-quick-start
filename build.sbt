name := "akka-quick"

version := "0.1"

scalaVersion := "2.12.6"

lazy val root = (project in file("."))
  .aggregate(infrastructure, application, useCase, domain)


lazy val infrastructure = (project in file("infrastructure")).settings(
  libraryDependencies ++= Seq(
    "com.amazonaws" % "aws-java-sdk-dynamodb" % "1.11.29",
  )
).dependsOn(application)

lazy val application = (project in file("application")).settings(
  libraryDependencies ++= Seq(
    "com.typesafe.akka" %% "akka-http"   % "10.1.5",
    "com.typesafe.akka" %% "akka-http-core" % "10.1.5",
    "com.typesafe.akka" %% "akka-http-spray-json" % "10.1.5",
    "com.typesafe.akka" %% "akka-testkit" % "2.5.12" % Test,
    "com.typesafe.akka" %% "akka-stream" % "2.5.12",
    "com.typesafe" % "config" % "1.3.3",
    "org.scalatest" %% "scalatest" % "3.0.5" % "test",
    "org.sangria-graphql" %% "sangria" % "1.4.2",
    "org.sangria-graphql" %% "sangria-spray-json" % "1.0.0",
    "org.sangria-graphql" %% "sangria-slowlog" % "0.1.8"
  )
).dependsOn(domain)

lazy val useCase = (project in file("use-case")).settings(
  libraryDependencies ++= Seq(
    "org.sangria-graphql" %% "sangria" % "1.4.2",
    "org.sangria-graphql" %% "sangria-circe" % "1.2.1",
//    TODO -nishi 依存性の理解
    "de.heikoseeberger" %% "akka-http-circe" % "1.20.0",
  )
).dependsOn(domain, infrastructure)

lazy val domain = (project in file("domain")).settings(
  libraryDependencies ++= Seq(
    "com.typesafe.akka" %% "akka-actor" % "2.5.12"
  )
)
