import play.boilerplate.generators._
import play.boilerplate.PlayBoilerplatePlugin
import PlayBoilerplatePlugin.Keys._

lazy val common = Seq(
  organization := "com.github.romastyi",
  scalaVersion := "2.11.12",
  version := "0.0.1-SNAPSHOT",
  scalacOptions ++= Seq(
    "-feature",
    "-language:postfixOps"),
  libraryDependencies ++= Seq(
    "com.typesafe.play" %% "play-json" % "2.4.11"
  )
)

lazy val customSecurityProvider = new security.Play2AuthSecurityProvider("UserModel", "UserAuthConfig", "session") {

  import treehugger.forest._
  import definitions._
  import treehuggerDSL._

  override def parseAuthority(scopes: Seq[SecurityScope]): Seq[Tree] = {
    val roles = scopes.find(_.scope == "roles").map { s =>
      ImmutableSetClass APPLY s.values.map(r => REF(s"UserRole.$r"))
    }.getOrElse {
      REF("UserRole.all")
    }
    Seq(REF("UserAuthority") APPLY roles)
  }

}

lazy val server = project.
  in(file("server")).
  settings(common: _*).
  settings(
    name := "codegen-server",
    routesImport += "test.api.controller.PetStoreController._",
    generatorProvidedPackage := "com.github.romastyi",
    generateServer := true,
    generateRoutes := true,
    generatorSettings := new GenSettings {
      override def apply(fileName: String, basePackageName: String, codeProvidedPackage: String): GeneratorSettings =
        DefaultGeneratorSettings(
          fileName,
          basePackageName,
          codeProvidedPackage,
          securityProvider = customSecurityProvider,
          injectionProvider = new injection.ScaldiInjectionProvider()
        )
    }
  )
  .enablePlugins(PlayScala)
  .settings(
    routesGenerator := StaticRoutesGenerator,
    libraryDependencies ++= Seq(
      "jp.t2v" %% "play2-auth" % "0.14.2",
      "org.scaldi" %% "scaldi-play" % "0.5.12"
    )
  )
  .disablePlugins(PlayLayoutPlugin)
  .enablePlugins(PlayBoilerplatePlugin)

lazy val client = project.
  in(file("client")).
  settings(common: _*).
  settings(
    name := "codegen-client",
    generateClient := true
  )
  .enablePlugins(PlayScala)
  .settings(
    libraryDependencies ++= Seq(
      "com.typesafe.play" %% "play-ws" % "2.4.11",
      "com.github.romastyi" %% "play-boilerplate-utils" % "0.0.1-SNAPSHOT"
    )
  )
  .disablePlugins(PlayLayoutPlugin)
  .enablePlugins(PlayBoilerplatePlugin)

lazy val root = project.in(file(".")).aggregate(server, client)
