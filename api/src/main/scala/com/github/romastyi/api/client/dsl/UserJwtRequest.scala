package com.github.romastyi.api.client.dsl

import com.github.romastyi.api.domain.{UserJwtSession, UserModel}
import pdi.jwt.JwtSession
import play.api.libs.ws.WSRequest

object UserJwtRequest {

  def withSession(request: WSRequest, user: Option[UserModel]): WSRequest = {
    user.map(UserJwtSession.newSession).fold(request)(
      session => request.withHeaders(JwtSession.REQUEST_HEADER_NAME -> (JwtSession.TOKEN_PREFIX + session.serialize))
    )
  }

}
