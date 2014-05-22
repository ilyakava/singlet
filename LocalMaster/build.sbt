name := "LocalMaster"

version := "1.0"

scalaVersion := "2.10.3"

libraryDependencies ++= Seq(
	"org.clapper" % "avsl_2.10" % "1.0",
	"com.typesafe.akka" %% "akka-actor" % "2.3.1",
	"com.typesafe.akka" %% "akka-remote" % "2.3.1"
)

resolvers ++= Seq(
  "java m2" at "http://download.java.net/maven/2"
)