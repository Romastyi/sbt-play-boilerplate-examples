package com.github.romastyi.api.service
import com.github.romastyi.api.domain.UserModel
import com.github.romastyi.api.model.LoginUser

import scala.concurrent.Future

final class AuthServiceImpl extends AuthService {

  import AuthService._

  /**
    * Login by user
    *
    *
    */
  override def login(loginUser: LoginUser): Future[LoginResponse] = {
    UserModel.authenticate(loginUser.username, loginUser.password) match {
      case Some(_) => Future.successful(LoginOk)
      case None => Future.successful(LoginForbidden)
    }
  }

}
