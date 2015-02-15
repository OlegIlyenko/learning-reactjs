name := "learning-reactjs"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala, net.litola.SassPlugin)

scalaVersion := "2.11.5"

libraryDependencies ++= Seq(
  jdbc,
  anorm,
  cache,
  ws,
  "org.scaldi" %% "scaldi-play" % "0.5.3",
  "org.webjars" %% "webjars-play" % "2.3.0-2",
  "org.webjars" % "bootstrap" % "3.3.2",
  "org.webjars" % "jquery" % "2.1.3",
  "org.webjars" % "react" % "0.12.2",
  "org.webjars" % "react-bootstrap" % "0.13.2",
  "org.webjars" % "rxjs" % "2.3.24",
  "org.webjars" % "react-router" % "0.12.0",
  "org.webjars" % "refluxjs" % "0.2.4",
  "org.webjars" % "lodash" % "3.1.0",
  "org.webjars" % "d3js" % "3.5.3"
)

ReactJsKeys.harmony := true
sassOptions := Seq("--compass")