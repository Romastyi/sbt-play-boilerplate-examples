import play.boilerplate.generators._
import play.boilerplate.PlayBoilerplatePlugin
import PlayBoilerplatePlugin.Keys._
import play.sbt.{PlayLayoutPlugin, PlayScala}
import sbt._
import sbt.Keys._

object CommonSettings {

  val Version = "0.0.1-SNAPSHOT"
  val PlayVersion = "2.4.11"
  val ScaldiVersion = "0.5.12"

  val common = Seq(
    organization := "com.github.romastyi",
    scalaVersion := "2.11.12",
    version := PlayVersion + "_" + Version,
    scalacOptions ++= Seq(
      "-feature",
      "-language:postfixOps"
    ),
    libraryDependencies ++= Seq(
      "com.typesafe.play" %% "play" % PlayVersion,
      "com.typesafe.play" %% "play-json" % PlayVersion,
      "org.scaldi" %% "scaldi-play" % ScaldiVersion
    )
  )

  object SecurityProvider extends security.Play2AuthSecurityProvider("UserModel", "UserAuthConfig", "session") {

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

  object ApiGenSettings extends GenSettings {
    override def apply(fileName: String, basePackageName: String, codeProvidedPackage: String): GeneratorSettings =
      DefaultGeneratorSettings(
        fileName,
        basePackageName,
        codeProvidedPackage,
        securityProvider = SecurityProvider
      )
  }

  def ApiProject(name: String, dir: File): Project = Project(name, dir)
    .settings(common: _ *)
    .settings(
      generatorDestPackage := "com.github.romastyi.api",
      generateModel := true,
      generateJson := true,
      generateService := true,
      generateClient := true,
      generatorSettings := ApiGenSettings,
      unmanagedResourceDirectories in Compile += generatorSourceDir.value,
      exportJars := true,
      libraryDependencies ++= Seq(
        "com.typesafe.play" %% "play-ws" % PlayVersion,
        "com.github.romastyi" %% "play-boilerplate-utils" % "0.0.1-SNAPSHOT"
      )
    )
    .enablePlugins(PlayBoilerplatePlugin)

  object ImplGenSettings extends GenSettings {
    override def apply(fileName: String, basePackageName: String, codeProvidedPackage: String): GeneratorSettings =
      DefaultGeneratorSettings(
        fileName,
        basePackageName,
        codeProvidedPackage,
        securityProvider = SecurityProvider,
        injectionProvider = new injection.ScaldiInjectionProvider()
      )
  }

  def ImplProject(name: String, dir: File, api: Project): Project = Project(name, dir)
    .settings(common: _ *)
    .settings(
      generatorDestPackage := "com.github.romastyi.api",
      generateModel := false,
      generateJson := false,
      generateServer := true,
      generateService := false,
      generateRoutes := true,
      generatorSettings := ImplGenSettings,
      generatorsSources += {
        val dependencies = (exportedProducts in Compile in api).value
        val toDirectory = (sourceManaged in Compile).value
        ClasspathJarsWatcher(dependencies, toDirectory)
      }
    )
    .enablePlugins(PlayBoilerplatePlugin)
    .enablePlugins(PlayScala)
    .disablePlugins(PlayLayoutPlugin)
    .dependsOn(api)

}
