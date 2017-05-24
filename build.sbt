import play.boilerplate.generators._
import play.boilerplate.PlayBoilerplatePlugin
import PlayBoilerplatePlugin.Keys._

lazy val common = Seq(
  organization := "eu.unicredit",
  scalaVersion := "2.11.7",
  version := "0.0.10-SNAPSHOT",
  scalacOptions ++= Seq(
    "-feature",
    "-language:postfixOps"),
  /* Play 2.3.x */
  /*
    sourceDirectory in Compile <<= baseDirectory(_ / "src" / "main"),
    _root_.play.PlayImport.PlayKeys.confDirectory <<= baseDirectory(_ / "src" / "main" / "resources"),
    resourceDirectory in Compile <<= baseDirectory(_ / "src" / "main" / "resources"),
    scalaSource in Compile <<= baseDirectory(_ / "src" / "main" / "scala"),
    libraryDependencies ++= Seq(
      "com.typesafe.play" %% "play-json" % "2.3.10"
    )
  */
  /* Play 2.4.x */
  libraryDependencies ++= Seq(
    "com.typesafe.play" %% "play-json" % "2.4.11"
  )
  /* Play 2.5.x */
  /*
    libraryDependencies ++=
      DefaultModelGenerator.dependencies ++
      DefaultJsonGenerator.dependencies
  */
)

lazy val server = project.
  in(file("server")).
  settings(common: _*).
  settings(
    name := "codegen-server",
    swaggerCodeProvidedPackage := "eu.unicredit",
    swaggerGenerateServer := true,
    swaggerServerRoutesFile := (resourceDirectory in Compile).value / "generated.routes",
    swaggerInjectionProvider := new injection.ScaldiInjectionProvider(),
    swaggerSecurityProvider := new security.Play2AuthSecurityProvider(
      "UserModel", "UserAuthConfig", "session"
    ) {

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
  )
  .enablePlugins(PlayScala)
  /* Play 2.3.x */
  /*
    .settings(
      libraryDependencies ++= Seq(
        "jp.t2v" %% "play2-auth" % "0.13.5",
        "org.scaldi" %% "scaldi-play-23" % "0.5.6"
      )
    )
  */
  /* Play 2.4.x */
  .settings(
  routesGenerator := StaticRoutesGenerator,
  libraryDependencies ++= Seq(
    "jp.t2v" %% "play2-auth" % "0.14.2",
    "org.scaldi" %% "scaldi-play" % "0.5.12"
  )
)
  .disablePlugins(PlayLayoutPlugin)
  /* Play 2.5.x */
  /*
    .settings(
      routesGenerator := StaticRoutesGenerator,
      libraryDependencies ++=
        DefaultServerGenerator.dependencies ++ Seq(
          "jp.t2v" %% "play2-auth" % "0.14.2",
          "org.scaldi" %% "scaldi-play" % "0.5.15"
        )
    )
    .disablePlugins(PlayLayoutPlugin)
  */
  .enablePlugins(PlayBoilerplatePlugin)

lazy val client = project.
  in(file("client")).
  settings(common: _*).
  settings(
    name := "codegen-client",
    swaggerCodeProvidedPackage := "",
    swaggerGenerateClient := true
  )
  .enablePlugins(PlayScala)
  /* Play 2.3.x */
  /*
    .settings(
      libraryDependencies ++= Seq(
        "com.typesafe.play" %% "play-ws" % "2.3.10"
      )
    )
  */
  /* Play 2.4.x */
  .settings(
  libraryDependencies ++= Seq(
    "com.typesafe.play" %% "play-ws" % "2.4.11"
  )
)
  .disablePlugins(PlayLayoutPlugin)
  /* Play 2.5.x */
  /*
    .settings(
      libraryDependencies ++=
        DefaultClientGenerator.dependencies
    )
    .disablePlugins(PlayLayoutPlugin)
  */
  .enablePlugins(PlayBoilerplatePlugin)

lazy val root = project.in(file(".")).aggregate(server, client)
