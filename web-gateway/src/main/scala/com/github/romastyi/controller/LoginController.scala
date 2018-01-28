package com.github.romastyi.controller

import com.github.romastyi.api.domain.UserModel
import com.github.romastyi.api.silhouette.SessionEnv
import com.mohiva.play.silhouette.api.{LoginEvent, Silhouette}
import com.mohiva.play.silhouette.api.util.Credentials
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import play.api.data.Form
import play.api.data.Forms._
import play.api.mvc._
import scaldi.{Injectable, Injector}

import scala.concurrent.{ExecutionContext, Future}

class LoginController(implicit inj: Injector) extends InjectedController with Injectable {

  implicit lazy val ec: ExecutionContext = controllerComponents.executionContext

  private lazy val silhouette = inject[Silhouette[SessionEnv]]
  private lazy val credentialsProvider = inject[CredentialsProvider]

  /** Your application's login form.  Alter it to fit your application */
  val loginForm = Form {
    mapping("email" -> text, "password" -> text)(UserModel.authenticate)(_.map(u => (u.email, "")))
      .verifying("Invalid email or password", result => result.isDefined)
  }

  /** Alter the login page action to suit your application. */
  def login: Action[AnyContent] = Action.async { implicit request =>
    Future.successful(Ok(html.login(loginForm)))
  }

  /**
    * Return the `gotoLoginSucceeded` method's result in the login action.
    *
    * Since the `gotoLoginSucceeded` returns `Future[Result]`,
    * you can add a procedure like the `gotoLogoutSucceeded`.
    */
  def authenticate: Action[AnyContent] = Action.async { implicit request =>
    loginForm.bindFromRequest.fold(
      formWithErrors => Future.successful(BadRequest(html.login(formWithErrors))),
      {
        case Some(user) =>
          for {
            loginInfo <- credentialsProvider.loginInfo(Credentials(user.email, user.password))
            authenticator <- silhouette.env.authenticatorService.create(loginInfo)
            _ = silhouette.env.eventBus.publish(LoginEvent(user, request))
            token <- silhouette.env.authenticatorService.init(authenticator)
            result <- silhouette.env.authenticatorService.embed(token, Redirect(routes.PetStoreController.index()))
          } yield result
        case None =>
          Future.successful(Forbidden)
      }
    )
  }

  def logout: Action[AnyContent] = silhouette.UserAwareAction.async { implicit request =>
    request.authenticator match {
      case Some(authenticator) =>
        silhouette.env.authenticatorService.discard(authenticator, Redirect(routes.LoginController.login()))
      case None =>
        Future.successful(Redirect(routes.LoginController.login()))
    }
  }

}
