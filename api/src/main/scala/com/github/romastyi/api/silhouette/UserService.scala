package com.github.romastyi.api.silhouette

import com.github.romastyi.api.domain.{UserModel, UserRole}
import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.api.services.IdentityService

import scala.concurrent.Future

object UserService extends IdentityService[UserModel] {
  override def retrieve(loginInfo: LoginInfo): Future[Option[UserModel]] = {
    Future.successful(UserModel.findByEmail(loginInfo.providerKey))
  }
}
