name := "SpreadsheetApp"

version := "0.1"

scalaVersion := "2.13.12"

libraryDependencies ++= Seq(
  "org.scala-lang.modules" %% "scala-parser-combinators" % "2.3.0",
  "org.scala-lang" % "scala-reflect" % scalaVersion.value
)

scalacOptions := Seq(
  "-deprecation",
  "-feature",
  "-unchecked",
  "-encoding",
  "utf8"
//   "-Wshadow" // 用於替代 -Xlint
  // "-Werror" // 如果你希望在遇到警告時失敗，可以保留這行，否則可以注釋掉
)

javacOptions ++= Seq("-encoding", "UTF-8")
