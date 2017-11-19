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

lazy val `petStore-api` = ApiProject("petStore-api", file("petStore-api"))
  .dependsOn(`auth-api`)
lazy val `petStore-impl` = ImplProject("petStore-impl", file("petStore-impl"), `petStore-api`)
  .settings(routesImport += "com.github.romastyi.api.controller.PetStoreController._")

lazy val `web-gateway` = project.in(file("web-gateway"))
  .settings(common: _ *)
  .enablePlugins(PlayScala)
  .disablePlugins(PlayLayoutPlugin)
  .dependsOn(`auth-impl`, `petStore-api`)

lazy val root = project.in(file("."))
  .aggregate(
    client,
    `auth-api`, `auth-impl`,
    `petStore-api`, `petStore-impl`,
    `web-gateway`
  )
