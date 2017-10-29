package com.github.romastyi

/**
  * Created by romastyi on 06.05.17.
  */

import jp.t2v.lab.play2.auth._
import play.api.mvc.Results._
import play.api.mvc.{RequestHeader, Result}

import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.{ClassTag, classTag}

trait UserAuthConfig extends AuthConfig {

  override type Id = Long
  override type User = UserModel
  override type Authority = UserAuthority

  override val idTag: ClassTag[Id] = classTag[Long]

  override def sessionTimeoutInSeconds: Int = 3600

  override def resolveUser(id: Long)(implicit context: ExecutionContext): Future[Option[UserModel]] = {
    Future.successful(Some(
      UserModel(id, s"user$id", "pass", UserRole.apply(id.toInt))
    ))
  }

  override def loginSucceeded(request: RequestHeader)(implicit context: ExecutionContext): Future[Result] = {
    Future.successful(
      Ok("login succeeded")
    )
  }

  override def logoutSucceeded(request: RequestHeader)(implicit context: ExecutionContext): Future[Result] = {
    Future.successful(
      Ok("logout succeeded")
    )
  }

  override def authenticationFailed(request: RequestHeader)(implicit context: ExecutionContext): Future[Result] = {
    Future.successful(
      Forbidden("no permission")
    )
  }

  /* Play 2.3.x */
/*
  override def authorizationFailed(request: RequestHeader)(implicit context: ExecutionContext): Future[Result] = {
    Future.successful(
      Unauthorized("no permissions")
    )
  }
*/

  override def authorizationFailed(request: RequestHeader, user: UserModel, authority: Option[UserAuthority])(implicit context: ExecutionContext): Future[Result] = {
    Future.successful(
      Unauthorized(s"User: $user\nNeed authority: $authority")
    )
  }

  override def authorize(user: UserModel, authority: UserAuthority)(implicit context: ExecutionContext): Future[Boolean] = {
    Future.successful(authority.roles.contains(user.role))
  }

  override lazy val idContainer: AsyncIdContainer[Long] = AsyncIdContainer(new TransparentIdContainer[Long])

}
