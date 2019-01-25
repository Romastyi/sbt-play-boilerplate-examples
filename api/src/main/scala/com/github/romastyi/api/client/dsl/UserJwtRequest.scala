package com.github.romastyi.api.client.dsl

import com.github.romastyi.api.domain.{UserJwtSession, UserModel}
import pdi.jwt.JwtSession
import play.boilerplate.api.client.dsl.Compat._

object UserJwtRequest {

  def withSession(request: WSRequest, user: Option[UserModel]): WSRequest = {
    user.map(UserJwtSession.newSession).fold(request)(
      session => request.addHttpHeaders(JwtSession.REQUEST_HEADER_NAME -> (JwtSession.TOKEN_PREFIX + session.serialize))
    )
  }

}
