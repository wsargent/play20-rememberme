import sbt._
import Keys._
import PlayProject._

object Build extends Build {

  val appName = "play20-rememberme"
  val appVersion = "1.0-SNAPSHOT"

  val appDependencies = Seq(
    "org.mindrot" % "jbcrypt" % "0.3m"
  )

  val main = PlayProject(appName, appVersion, appDependencies, mainLang = SCALA).settings(
    resolvers += "jbcrypt repo" at "http://mvnrepository.com/",

    // Allow these classes to be seen in templates automatically.
    templatesImport ++= Seq(
      "authentication.Context",
      "models.User"
    )
  )

}
