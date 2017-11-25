package com.github.romastyi.controller

import com.github.romastyi.api.controller.UserAuthConfig
import play.api.mvc.{Controller, RequestHeader, Result}

import scala.concurrent.{ExecutionContext, Future}

trait UserAuthConfigImpl extends UserAuthConfig { this: Controller =>

  override def loginSucceeded(request: RequestHeader)(implicit context: ExecutionContext): Future[Result] = {
    Future.successful(Redirect(routes.IndexController.index()))
  }

  override def logoutSucceeded(request: RequestHeader)(implicit context: ExecutionContext): Future[Result] = {
    Future.successful(Redirect(routes.LoginController.login()))
  }

  override def authenticationFailed(request: RequestHeader)(implicit context: ExecutionContext): Future[Result] = {
    Future.successful(Redirect(routes.LoginController.login()))
  }

}
