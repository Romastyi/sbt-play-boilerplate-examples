package com.github.romastyi.api.controller

import com.github.romastyi.api.domain.{UserJwtSession, UserModel}
import com.github.romastyi.api.model.LoginUser
import com.github.romastyi.api.model.json.AuthJson._
/*
import com.github.romastyi.api.silhouette.JWTEnv
import com.mohiva.play.silhouette.api.{LoginEvent, Silhouette}
import com.mohiva.play.silhouette.api.util.Credentials
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
*/
import pdi.jwt.JwtSession._
import play.api.mvc.{Action, _}
import scaldi.{Injectable, Injector}

import scala.concurrent.{ExecutionContext, Future}

class AuthController(implicit val inj: Injector) extends InjectedController with Injectable {

  implicit lazy val ec: ExecutionContext = controllerComponents.executionContext

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

/*
  private val silhouette = inject[Silhouette[JWTEnv]]
  private val credentialsProvider = inject[CredentialsProvider]

  def login(): Action[LoginUser] = Action.async(parse.json[LoginUser]) { implicit request =>
    val loginUser = request.body
    for {
      loginInfo <- credentialsProvider.authenticate(Credentials(loginUser.username, loginUser.password))
      result <- UserModel.authenticate(loginUser.username, loginUser.password) match {
        case Some(user) =>
          for {
            authenticator <- silhouette.env.authenticatorService.create(loginInfo)
            _ = silhouette.env.eventBus.publish(LoginEvent(user, request))
            token <- silhouette.env.authenticatorService.init(authenticator)
            r <- silhouette.env.authenticatorService.embed(token, Ok)
          } yield r
        case None =>
          Future.successful(Forbidden)
      }
    } yield result
  }

  def logout(): Action[AnyContent] = silhouette.UserAwareAction.async { implicit request =>
    request.authenticator match {
      case Some(authenticator) =>
        silhouette.env.authenticatorService.discard(authenticator, Ok)
      case None =>
        Future.successful(Ok)
    }
  }
*/

}
