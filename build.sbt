import sbt._
import sys.process._

scalaVersion := "3.8.1"

ThisBuild / scalacOptions ++= Seq(
  "-deprecation",
  "-encoding",
  "UTF-8",
  "-feature",
  "-unchecked",
  "-language:postfixOps",
  "-Wunused:all",
  "-Wvalue-discard",
  "-Wnonunit-statement",
) ++ (if (sys.env.contains("CI")) Seq("-Werror") else Nil)

ThisBuild / libraryDependencySchemes += "dev.zio" %% "zio-json" % "always"

ThisBuild / semanticdbEnabled := true
ThisBuild / semanticdbVersion := scalafixSemanticdb.revision

addCommandAlias("fmt", "scalafmtSbt; scalafmtAll; scalafixAll")
addCommandAlias("check", "scalafmtSbtCheck; scalafmtCheckAll; scalafixAll --check")

val zioVersion     = "2.1.24"
val zioHttpVersion = "3.10.1"
val quillVersion   = "4.8.6"

libraryDependencies ++= Seq(
  "dev.zio"       %% "zio"            % zioVersion,
  "dev.zio"       %% "zio-http"       % zioHttpVersion,
  "io.getquill"   %% "quill-jdbc-zio" % quillVersion,
  "org.postgresql" % "postgresql"     % "42.7.3",
)

lazy val nativeImage = taskKey[Unit]("Build native image")

nativeImage := {
  val graalVm         = sys.env.getOrElse("GRAALVM_HOME", s"${sys.env("HOME")}/.jdk/graal")
  val nativeImagePath = file(s"$graalVm/bin/native-image")
  val classes         = (Compile / classDirectory).value.getAbsolutePath
  val cp              = (Runtime / fullClasspath).value.map(_.data.getAbsolutePath).mkString(":")
  val resources       = (Runtime / resourceDirectories).value.map(_.getAbsolutePath).mkString(":")
  val cmd             =
    s"$nativeImagePath -cp $classes:$cp:$resources --initialize-at-run-time=io.netty.channel.epoll,io.netty.channel.unix,io.netty.channel.kqueue,org.postgresql.Driver --no-fallback --enable-http -H:+UnlockExperimentalVMOptions app.Main app-native"
  if (cmd.! != 0) sys.error("native-image failed")
}
