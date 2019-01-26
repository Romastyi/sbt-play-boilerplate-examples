import play.boilerplate.PlayBoilerplatePlugin
import PlayBoilerplatePlugin.Keys._
import PlayBoilerplatePlugin.Generators
import CommonSettings._

lazy val `api` = project.in(file("api"))
  .settings(common: _ *)
  .settings(
    resolvers += Resolver.jcenterRepo,
    libraryDependencies ++= Seq(
      "com.typesafe.play" %% "play-ws" % PlayVersion,
      boilerplateClientApi,
      // Consul service discovery
      consul,
      // JWT
      "com.pauldijou" %% "jwt-play" % "0.14.1",
      // silhouette
      "com.mohiva" %% "play-silhouette" % SilhouetteVersion,
      "com.mohiva" %% "play-silhouette-password-bcrypt" % SilhouetteVersion,
      "com.mohiva" %% "play-silhouette-crypto-jca" % SilhouetteVersion,
      "com.mohiva" %% "play-silhouette-persistence" % SilhouetteVersion
    )
  )

lazy val `auth-api` = MyApiProject("auth-api", file("auth-api"))
  .dependsOn(`api`)
lazy val `auth-impl` = MyImplProject("auth-impl", file("auth-impl"), `auth-api`)
  .settings(
    generators --= Seq(Generators.injectedController, Generators.sirdRoutes),
    generators += Generators.sirdRoutesWithPrefix("/internal")
  )

lazy val `petStore-api` = MyApiProject("petStore-api", file("petStore-api"))
  .dependsOn(`api`, `auth-api`)
lazy val `petStore-impl` = MyImplProject("petStore-impl", file("petStore-impl"), `petStore-api`)
  .settings(
    libraryDependencies ++= Seq(
      "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2" % "test",
      "org.scalacheck" %% "scalacheck" % "1.14.0" % "test",
      "org.mockito" % "mockito-core" % "2.23.4" % "test"
    )
  )

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
    `api`,
    `auth-api`, `auth-impl`,
    `petStore-api`, `petStore-impl`,
    `web-gateway`
  )
  .enablePlugins(FixUnresolvedImports)
