import play.boilerplate.PlayBoilerplatePlugin
import PlayBoilerplatePlugin.Keys._
import PlayBoilerplatePlugin.{ApiProject, Generators, ImplProject, Imports}
import play.boilerplate.generators.injection._
import play.boilerplate.generators.security.Play2AuthSecurityProvider
import play.boilerplate.generators.security.SecurityProvider._
import play.PlayScala
import sbt._
import sbt.Keys._

object CommonSettings {

  val Version = "0.0.3"
  val PlayVersion: String = play.core.PlayVersion.current

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
      Imports.scaldi(PlayVersion)
    ),
    resolvers += Opts.resolver.sonatypeSnapshots
  )

  val boilerplateApi = Imports.api(PlayVersion)

  object Auth {

    import treehugger.forest._
    import definitions._
    import treehuggerDSL._

    def parseAuthority(scopes: Seq[SecurityScope]): Seq[Tree] = {
      val roles = scopes.find(_.scope == "roles").map { s =>
        ImmutableSetClass APPLY s.values.map(r => REF(s"UserRole.$r"))
      }.getOrElse {
        REF("UserRole.all")
      }
      Seq(REF("UserAuthority") APPLY roles)
    }

  }

  object AuthSecurityProvider extends Play2AuthSecurityProvider(
    "UserModel",
    "UserAuthConfig",
    "session",
    Seq(
      "com.github.romastyi.api.domain.UserModel",
      "com.github.romastyi.api.domain.UserRole",
      "com.github.romastyi.api.domain.UserAuthority"
    )
  ) {

    import treehugger.forest._

    override def parseAuthority(scopes: Seq[SecurityScope]): Seq[Tree] =
      Auth.parseAuthority(scopes)

  }

  object JwtSecurityProvider extends DefaultSecurity("jwt") {

    import treehugger.forest._
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

      val authority = Auth.parseAuthority(scopes)
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

  def MyApiProject(name: String, dir: File): Project = ApiProject(name, dir)(PlayVersion)
    .settings(common: _ *)
    .settings(
      generatorDestPackage := "com.github.romastyi.api",
      securityProvider := JwtSecurityProvider
    )

  def MyImplProject(name: String, dir: File, api: Project): Project = ImplProject(name, dir, api)(PlayVersion)
    .settings(common: _ *)
    .settings(
      generatorDestPackage := "com.github.romastyi.api",
      generators -= Generators.injectedRoutes,
      generators += Generators.dynamicRoutes,
      securityProvider := JwtSecurityProvider,
      injectionProvider := ScaldiInjectionProvider,
      javaOptions in Runtime += "-Dconfig.file=" + (baseDirectory.value / "resources" / "reference.conf").getAbsolutePath
    )
    .enablePlugins(PlayScala)

}
