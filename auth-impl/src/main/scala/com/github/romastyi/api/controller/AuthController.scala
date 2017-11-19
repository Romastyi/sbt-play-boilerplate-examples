package com.github.romastyi.api.controller

import com.github.romastyi.api.domain.UserModel
import com.github.romastyi.api.model.LoginUser
import com.github.romastyi.api.model.json.AuthJson._
import jp.t2v.lab.play2.auth.LoginLogout
import play.api.mvc.{Action, AnyContent, Controller}
import play.api.libs.concurrent.Execution.Implicits._

import scala.concurrent.Future

class AuthController extends Controller with LoginLogout with UserAuthConfig {

  def login(): Action[LoginUser] = Action.async(parse.json[LoginUser]) { implicit request =>
    val loginUser = request.body
    UserModel.authenticate(loginUser.username, loginUser.password) match {
      case Some(user) => gotoLoginSucceeded(user.id)
      case None => Future.successful(Forbidden)
    }
  }

  def logout(): Action[AnyContent] = Action.async { implicit request =>
    gotoLogoutSucceeded
  }

}
