import sbt._
import Keys._
import PlayProject._

object Build extends Build {

  val appName = "play20-rememberme"
  val appVersion = "1.0-SNAPSHOT"

  val appDependencies = Seq(
    "org.mindrot" % "jbcrypt" % "0.3m"
  )

  val module = Project(id = "remember-me", base = file("renderer"),
    settings = Defaults.defaultSettings ++ PlayProject.intellijCommandSettings("SCALA"))

  val main = PlayProject(appName, appVersion, appDependencies, mainLang = SCALA).settings(
    resolvers += "jbcrypt repo" at "http://mvnrepository.com/",

    // Allow these classes to be seen in templates automatically.
    templatesImport ++= Seq(
      "security.MyContext",
      "models.User"
    )
  ).aggregate(module).dependsOn(module)

}
