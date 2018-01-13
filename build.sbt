import play.PlayImport.PlayKeys._
import play.boilerplate.PlayBoilerplatePlugin
import PlayBoilerplatePlugin.Keys._
import PlayBoilerplatePlugin.Generators
import CommonSettings._

lazy val `api` = project.in(file("api"))
  .settings(common: _ *)
  .settings(
    libraryDependencies ++= Seq(
      "com.typesafe.play" %% "play-ws" % PlayVersion,
      boilerplateApi,
      // Consul service discovery
      "com.ecwid.consul" % "consul-api" % "1.2.4",
      // Play2-Auth library
      "jp.t2v" %% "play2-auth" % "0.13.2",
      // JWT
      "com.pauldijou" %% "jwt-play" % "0.2.1"
    )
  )

lazy val `auth-api` = MyApiProject("auth-api", file("auth-api"))
  .dependsOn(`api`)
lazy val `auth-impl` = MyImplProject("auth-impl", file("auth-impl"), `auth-api`)
  .settings(generators -= Generators.controller)

lazy val `petStore-api` = MyApiProject("petStore-api", file("petStore-api"))
  .dependsOn(`api`, `auth-api`)
lazy val `petStore-impl` = MyImplProject("petStore-impl", file("petStore-impl"), `petStore-api`)
  .settings(routesImport += "com.github.romastyi.api.controller.PetStoreController._")

lazy val `web-gateway` = project.in(file("web-gateway"))
  .settings(common: _ *)
  .enablePlugins(PlayScala)
  .settings(
    sourceDirectory in Assets := (sourceDirectory in Compile).value / "assets"
  )
  .dependsOn(`api`, `auth-impl`, `petStore-api`)

lazy val root = project.in(file("."))
  .aggregate(
    `api`,
    `auth-api`, `auth-impl`,
    `petStore-api`, `petStore-impl`,
    `web-gateway`
  )
