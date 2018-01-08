package com.github.romastyi.api.silhouette

import com.github.romastyi.api.domain.{UserModel, UserRole}
import com.mohiva.play.silhouette.api.{Authenticator, Authorization}
import play.api.mvc.Request

import scala.concurrent.Future

case class WithRoles[A <: Authenticator](roles: Set[UserRole.Value]) extends Authorization[UserModel, A] {
  override def isAuthorized[B](identity: UserModel, authenticator: A)(implicit request: Request[B]): Future[Boolean] = {
    Future.successful(roles contains identity.role)
  }
}
