package com.github.romastyi.controller

/**
  * Created by romastyi on 06.05.17.
  */

import com.github.romastyi.{UserAuthConfig, UserModel}
import jp.t2v.lab.play2.auth.LoginLogout
import play.api.data.Form
import play.api.data.Forms._
import play.api.http.MimeTypes
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.mvc.{Action, AnyContent, Controller}
/* Play 2.3.x */
//import twirl.html

import scala.concurrent.Future

object LoginController extends Controller with LoginLogout with UserAuthConfig {

  /** Your application's login form.  Alter it to fit your application */
  val loginForm = Form {
    mapping("email" -> text, "password" -> text)(UserModel.authenticate)(_.map(u => (u.email, "")))
      .verifying("Invalid email or password", result => result.isDefined)
  }

  /** Alter the login page action to suit your application. */
  def login: Action[AnyContent] = Action.async { implicit request =>
    val accept = request.headers.get(ACCEPT).getOrElse("")
    if (accept.contains(MimeTypes.HTML)) {
      Future.successful(Ok(html.login(loginForm)))
    } else {
      request.body.asJson match {
        case Some(json) =>
          val username = (json \ "email").as[String]
          val password = (json \ "password").as[String]
          UserModel.authenticate(username, password) match {
            case Some(user) => gotoLoginSucceeded(user.id)
            case None => Future.successful(Unauthorized)
          }
        case None =>
          Future.successful(BadRequest)
      }
    }
  }

  /**
    * Return the `gotoLogoutSucceeded` method's result in the logout action.
    *
    * Since the `gotoLogoutSucceeded` returns `Future[Result]`,
    * you can add a procedure like the following.
    *
    *   gotoLogoutSucceeded.map(_.flashing(
    *     "success" -> "You've been logged out"
    *   ))
    */
  def logout: Action[AnyContent] = Action.async { implicit request =>
    // do something...
    gotoLogoutSucceeded
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

}
