import sbt._
import Keys._

import play.Project._

object ApplicationBuild extends Build {

  val appName = "play20-rememberme"
  val appVersion = "1.0-SNAPSHOT"

  val appDependencies = Seq(
   // "org.mindrot" % "jbcrypt" % "0.3m",
   jdbc,
   anorm,
   filters
  )

  // XXX You have to comment this out running "play idea", otherwise you get an exception 
  val module = Project(id = "remember-me", base = file("module"))

  val main = play.Project(appName, appVersion, appDependencies).settings(
    resolvers += "jbcrypt repo" at "http://mvnrepository.com/",

    // Allow these classes to be seen in templates automatically.
    templatesImport ++= Seq(
      "security.MyContext",
      "models.User"
    )
  ).aggregate(module).dependsOn(module)

}
