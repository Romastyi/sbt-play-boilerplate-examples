import play.boilerplate.generators._
import play.boilerplate.PlayBoilerplatePlugin
import PlayBoilerplatePlugin.Keys._
import play.boilerplate.generators.injection.InjectionProvider
import play.boilerplate.generators.security.SecurityProvider._
import play.boilerplate.generators.security.SilhouetteSecurityProvider
import play.sbt.{PlayImport, PlayLayoutPlugin, PlayScala}
import sbt._
import sbt.Keys._

object CommonSettings {

  val Version = "0.0.2"
  val PlayVersion: String = play.core.PlayVersion.current
  val ScaldiVersion = "0.5.17"
  val SilhouetteVersion = "5.0.0"

  val common = Seq(
    organization := "com.github.romastyi",
    scalaVersion := "2.12.4",
    version := PlayVersion + "_" + Version,
    scalacOptions ++= Seq(
      "-feature",
      "-language:postfixOps"
    ),
    libraryDependencies ++= Seq(
      "com.typesafe.play" %% "play" % PlayVersion,
      "com.typesafe.play" %% "play-json" % PlayVersion,
      "org.scaldi" %% "scaldi-play" % ScaldiVersion
    ),
    libraryDependencies += PlayImport.guice,
    resolvers += Opts.resolver.sonatypeSnapshots
  )

  val boilerplateApi = PlayBoilerplatePlugin.Imports.api(PlayVersion)

  object AuthSecurityProvider extends SilhouetteSecurityProvider("session") {

    import treehugger.forest._
    import definitions._
    import treehuggerDSL._

    override def envType: Type = TYPE_REF("SessionEnv")

    override def userType: Type = TYPE_REF("UserModel")

    override def parseAuthority(scopes: Seq[SecurityScope]): Seq[Tree] = {
      val roles = scopes.find(_.scope == "roles").map { s =>
        ImmutableSetClass APPLY s.values.map(r => REF(s"UserRole.$r"))
      }.getOrElse {
        REF("UserRole.all")
      }
      Seq(REF("WithRoles") APPLYTYPE genericAuthenticator APPLY roles)
    }

    override def controllerImports: Seq[Import] = super.controllerImports ++ Seq(
      IMPORT("com.github.romastyi.api.silhouette", "_"),
      IMPORT("com.github.romastyi.api.domain", "_")
    )

    override def serviceImports: Seq[Import] = Seq(
      IMPORT("com.github.romastyi.api.domain", "_")
    )

  }

/*
  object JwtSecurityProvider  extends SilhouetteSecurityProvider("jwt") {

    import treehugger.forest._
    import treehuggerDSL._

    override def envType: Type = TYPE_REF("JWTEnv")

  }
*/

  object JwtSecurityProvider extends DefaultSecurity("jwt") {

    import treehugger.forest._
    import definitions._
    import treehuggerDSL._

    override def controllerImports: Seq[Import] = {
      Seq(
        IMPORT("com.github.romastyi.api.controller", "UserJwtController"),
        IMPORT("com.github.romastyi.api.domain", "_")
      )
    }

    override def controllerParents: Seq[Type] = Seq(TYPE_REF("UserJwtController"))
    override def controllerSelfTypes: Seq[Type] = Nil
    override def controllerDependencies: Seq[InjectionProvider.Dependency] = Nil
    override def serviceImports: Seq[Import] = Seq(IMPORT("com.github.romastyi.api.domain", "_"))

    override def composeActionSecurity(scopes: Seq[SecurityScope]): ActionSecurity = {

      val roles = scopes.find(_.scope == "roles").map { s =>
        ImmutableSetClass APPLY s.values.map(r => REF(s"UserRole.$r"))
      }.getOrElse {
        REF("UserRole.all")
      }
      val authority = Seq(REF("UserAuthority") APPLY roles)

      val userType: Type = TYPE_REF("UserModel")
      val userValue: ValDef = VAL("user", userType) := REF("request") DOT "user"

      new ActionSecurity {
        override def actionMethod(parser: Tree): Tree = {
          REF("Authenticated") APPLY authority DOT "async" APPLY parser
        }
        override val securityParams: Map[String, Type] = {
          Map("user" -> userType)
        }
        override val securityValues: Map[String, ValDef] = {
          Map("user" -> userValue)
        }
      }

    }

  }

  object ApiGenSettings extends GenSettings {
    override def apply(fileName: String, basePackageName: String, codeProvidedPackage: String): GeneratorSettings =
      DefaultGeneratorSettings(
        fileName,
        basePackageName,
        codeProvidedPackage,
        securityProvider = JwtSecurityProvider
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
        boilerplateApi
      )
    )
    .enablePlugins(PlayBoilerplatePlugin)

  object ImplGenSettings extends GenSettings {
    override def apply(fileName: String, basePackageName: String, codeProvidedPackage: String): GeneratorSettings =
      DefaultGeneratorSettings(
        fileName,
        basePackageName,
        codeProvidedPackage,
        securityProvider = JwtSecurityProvider,
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
      generateRoutesCodeGenerator := InjectedRoutesCodeGenerator,
      generatorSettings := ImplGenSettings,
      generatorsSources += {
        val dependencies = (exportedProducts in Compile in api).value
        val toDirectory = (sourceManaged in Compile).value
        ClasspathJarsWatcher(dependencies, toDirectory)
      },
      javaOptions in Runtime += "-Dconfig.file=" + (baseDirectory.value / "resources" / "reference.conf").getAbsolutePath
    )
    .enablePlugins(PlayBoilerplatePlugin)
    .enablePlugins(PlayScala)
    .disablePlugins(PlayLayoutPlugin)
    .dependsOn(api)

}
