name := "ELKWeb"
organization := "de.tu-dresden.lat"

version := "0.1"

scalaVersion := "2.12.6"


resolvers += Classpaths.typesafeReleases

val ScalatraVersion = "2.6.3"

libraryDependencies ++= Seq(
  "org.scalatra" %% "scalatra" % ScalatraVersion,
  "org.scalatra" %% "scalatra-scalate" % ScalatraVersion,
  "org.scalatra"            %% "scalatra-forms"    % ScalatraVersion,
  "org.phenoscape" %% "scowl" % "1.3",
  "net.sourceforge.owlapi" %  "owlapi-distribution"    % "4.2.7",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.0",
  "org.postgresql" % "postgresql" % "9.3-1100-jdbc4",
  "org.scalikejdbc" %% "scalikejdbc"       % "3.3.2",
  "org.eclipse.jetty" % "jetty-webapp" % "9.4.9.v20180320" % "container",
  "javax.servlet" % "javax.servlet-api" % "3.1.0" % "provided",
  "com.google.code.gson" % "gson" % "1.7.1",
  "de.tu-dresden" %% "el2db" % "0.1.0-SNAPSHOT"
)


enablePlugins(ScalatraPlugin)
