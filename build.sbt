import play.boilerplate.generators._
import play.boilerplate.PlayBoilerplatePlugin
import PlayBoilerplatePlugin.Keys._
import CommonSettings._

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
      "com.github.romastyi" %% "play-boilerplate-utils" % "0.0.1-SNAPSHOT",
      "com.ecwid.consul" % "consul-api" % "1.2.4"
    )
  )
  .disablePlugins(PlayLayoutPlugin)
  .enablePlugins(PlayBoilerplatePlugin)

lazy val `auth-api` = ApiProject("auth-api", file("auth-api"))
  .settings(libraryDependencies += "jp.t2v" %% "play2-auth" % "0.14.2")
lazy val `auth-impl` = ImplProject("auth-impl", file("auth-impl"), `auth-api`)
  .settings(generateServer := false)

lazy val root = project.in(file(".")).aggregate(server, client, `auth-api`, `auth-impl`)
