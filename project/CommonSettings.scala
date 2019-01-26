import play.boilerplate.PlayBoilerplatePlugin
import PlayBoilerplatePlugin.Keys._
import PlayBoilerplatePlugin.{Generators, Imports, ApiProject, ImplProject}
import play.boilerplate.generators.injection._
import play.boilerplate.generators.security.SecurityProvider._
import play.boilerplate.generators.security.SilhouetteSecurityProvider
import play.sbt.{PlayImport, PlayLayoutPlugin, PlayScala}
import sbt._
import sbt.Keys._

object CommonSettings {

  val Version = "0.0.4"
  val PlayVersion: String = play.core.PlayVersion.current
  val SilhouetteVersion = "5.0.0"

  val common: Seq[Def.SettingsDefinition] = Seq(
    organization := "com.github.romastyi",
    scalaVersion := "2.12.4",
    version := PlayVersion + "_" + Version,
    scalacOptions ++= Seq(
      "-feature",
      "-language:postfixOps"
    ),
    libraryDependencies ++= Seq(
      "com.typesafe.play" %% "play" % PlayVersion,
      Imports.scaldi(PlayVersion)
    ),
    libraryDependencies += PlayImport.guice,
    resolvers += Opts.resolver.sonatypeSnapshots
  )

  val boilerplateClientApi = Imports.clientApi(PlayVersion)
  val consul = Imports.component("api-client-consul")

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
      val userValue: ValDef = VAL("logged", userType) := REF("request") DOT "user"

      new ActionSecurity {
        override def actionMethod(parser: Tree): Tree = {
          REF("Authenticated") APPLY authority DOT "async" APPLY parser
        }
        override val securityParams: Seq[(String, Type)] = {
          ("logged" -> userType) :: Nil
        }
        override val securityValues: Seq[(String, ValDef)] = {
          ("logged" -> userValue) :: Nil
        }
        override val securityDocs: Seq[(String, String)] = {
          ("logged" -> "Current logged user") :: Nil
        }
      }

    }

  }

  def MyApiProject(name: String, dir: File): Project = ApiProject(name, dir)(PlayVersion)
    .settings(common: _ *)
    .settings(
      generatorDestPackage := "com.github.romastyi.api",
      securityProviders := Seq(JwtSecurityProvider)
    )

  def MyImplProject(name: String, dir: File, api: Project): Project = ImplProject(name, dir, api)(PlayVersion)
    .settings(common: _ *)
    .settings(
      generatorDestPackage := "com.github.romastyi.api",
      generators --= Seq(Generators.controller, Generators.injectedRoutes),
      generators ++= Seq(Generators.injectedController, Generators.sirdRoutes),
      securityProviders := Seq(JwtSecurityProvider),
      injectionProvider := ScaldiInjectionProvider,
      javaOptions in Runtime += "-Dconfig.file=" + (baseDirectory.value / "resources" / "reference.conf").getAbsolutePath
    )
    .enablePlugins(PlayScala)
    .disablePlugins(PlayLayoutPlugin)

}
