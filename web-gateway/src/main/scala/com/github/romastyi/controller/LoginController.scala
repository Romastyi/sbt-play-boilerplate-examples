package com.github.romastyi.controller

import com.github.romastyi.api.domain.UserModel
import jp.t2v.lab.play2.auth.LoginLogout
import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.concurrent.Execution.Implicits._
import play.api.mvc._

import scala.concurrent.Future

class LoginController extends Controller with LoginLogout with UserAuthConfigImpl {

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
      user => gotoLoginSucceeded(user.get.id)
    )
  }

  def logout: Action[AnyContent] = Action.async { implicit request =>
    gotoLogoutSucceeded
  }

}
