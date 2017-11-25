import play.boilerplate.PlayBoilerplatePlugin
import PlayBoilerplatePlugin.Keys._
import CommonSettings._

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
      "com.github.romastyi" %% "play-boilerplate-api" % "0.0.1-SNAPSHOT",
      "com.ecwid.consul" % "consul-api" % "1.2.4"
    )
  )
  .disablePlugins(PlayLayoutPlugin)
  .enablePlugins(PlayBoilerplatePlugin)
  .dependsOn(`api`)

lazy val `api` = project.in(file("api"))
  .settings(common: _ *)
  .settings(
    libraryDependencies ++= Seq(
      "com.typesafe.play" %% "play-ws" % PlayVersion,
      "com.github.romastyi" %% "play-boilerplate-api" % "0.0.1-SNAPSHOT",
      "com.ecwid.consul" % "consul-api" % "1.2.4",
      "jp.t2v" %% "play2-auth" % "0.14.2",
      "com.pauldijou" %% "jwt-play" % "0.9.2"
    )
  )

lazy val `auth-api` = ApiProject("auth-api", file("auth-api"))
  .dependsOn(`api`)
lazy val `auth-impl` = ImplProject("auth-impl", file("auth-impl"), `auth-api`)
  .settings(generateServer := false)

lazy val `petStore-api` = ApiProject("petStore-api", file("petStore-api"))
  .dependsOn(`api`, `auth-api`)
lazy val `petStore-impl` = ImplProject("petStore-impl", file("petStore-impl"), `petStore-api`)
  .settings(routesImport += "com.github.romastyi.api.controller.PetStoreController._")

lazy val `web-gateway` = project.in(file("web-gateway"))
  .settings(common: _ *)
  .enablePlugins(PlayScala)
  .disablePlugins(PlayLayoutPlugin)
  .settings(
    sourceDirectory in Assets := (sourceDirectory in Compile).value / "assets"
  )
  .dependsOn(`api`, `auth-impl`, `petStore-api`)

lazy val root = project.in(file("."))
  .aggregate(
    client,
    `api`,
    `auth-api`, `auth-impl`,
    `petStore-api`, `petStore-impl`,
    `web-gateway`
  )
