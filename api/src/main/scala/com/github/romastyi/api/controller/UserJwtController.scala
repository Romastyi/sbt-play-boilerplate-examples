package com.github.romastyi.api.controller

import com.github.romastyi.api.domain.{UserAuthority, UserJwtSession, UserModel}
import pdi.jwt._
import play.api.mvc.{Controller, RequestHeader}
import play.api.mvc.Security.AuthenticatedBuilder

trait UserJwtController { this: Controller =>

  def realm: String = "Secured"

  case class Authenticated(authority: UserAuthority) extends AuthenticatedBuilder[UserModel](
    resolveUser(authority),
    onUnauthorized = { _ =>
      Unauthorized("Access token is missing or invalid")
        .withHeaders("WWW-Authenticate" -> s"""${JwtSession.TOKEN_PREFIX} realm="$realm"""") }
  )

  private def resolveUser(authority: UserAuthority): RequestHeader => Option[UserModel] = {
    r => UserJwtSession.getUser(r.jwtSession).flatMap(authority.validateUser)
  }

}
