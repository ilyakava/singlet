name := "singlet"

version := "1.0"

scalaVersion := "2.10.3"

libraryDependencies ++= Seq(
	"com.google.code.findbugs" % "jsr305" % "2.0.2",
	"com.sharneng" % "gm4java" % "1.1.0",
	"org.clapper" % "avsl_2.10" % "1.0"
)

resolvers ++= Seq(
  "java m2" at "http://download.java.net/maven/2"
)