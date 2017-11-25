package com.github.romastyi.api.controller

import com.github.romastyi.api.domain.{UserJwtSession, UserModel}
import com.github.romastyi.api.model.LoginUser
import com.github.romastyi.api.model.json.AuthJson._
import pdi.jwt._
import play.api.mvc.{Action, AnyContent, Controller}

class AuthController extends Controller {

  def login(): Action[LoginUser] = Action(parse.json[LoginUser]) { implicit request =>
    val loginUser = request.body
    UserModel.authenticate(loginUser.username, loginUser.password) match {
      case Some(user) => Ok.withJwtSession(UserJwtSession.newSession(user))
      case None => Forbidden
    }
  }

  def logout(): Action[AnyContent] = Action { implicit request =>
    Ok.withoutJwtSession
  }

}
