import sbt._
import Keys._
import PlayProject._

object ApplicationBuild extends Build {

  val appName = "play20-rememberme"
  val appVersion = "1.0-SNAPSHOT"

  val appDependencies = Seq(
    "org.mindrot" % "jbcrypt" % "0.3m"
  )

  lazy val play20_auth_module = Project("play20-auth-module", file("module"))

  val main = PlayProject("sample", appVersion, appDependencies, mainLang = SCALA).settings(
    resolvers += "jbcrypt repo" at "http://mvnrepository.com/"
  ).dependsOn(play20_auth_module)

}
