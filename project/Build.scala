import sbt._
import Keys._
import PlayProject._
import sbtbuildinfo.Plugin._

object ApplicationBuild extends Build {

    val appName         = "sultan"
    val appVersion      = "0.0.1"

    val appDependencies = Seq(
      "org.scala-lang" % "scala-compiler"         % "2.9.1",
      "org.specs2"    %% "specs2"                 % "1.12.1"           % "test",
      "mysql"         % "mysql-connector-java"    % "5.1.21",
      "org.mindrot"   % "jbcrypt"                 % "0.3m",
      "org.pegdown"   % "pegdown"                 % "1.1.0",
      "javax.mail"    % "mail"                    % "1.4.1",
      "org.apache.commons" % "commons-email"      % "1.2",
      "org.pegdown"   % "pegdown"                 % "1.1.0"
      "joda-time"     % "joda-time"               % "2.1"
    )

    val main = PlayProject(appName, appVersion, appDependencies, mainLang = SCALA, settings = Defaults.defaultSettings ++ buildInfoSettings).settings(
      // Add your own project settings here
      scalaVersion := "2.9.1"
    ).settings(
      sourceGenerators in Compile <+= buildInfo,
      buildInfoKeys := Seq[BuildInfoKey](name, version, scalaVersion, sbtVersion),
      buildInfoPackage := "sultan"
    )
}
