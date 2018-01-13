package com.github.romastyi.api.client.dsl

import com.github.romastyi.api.domain.{UserJwtSession, UserModel}
import pdi.jwt.JwtSession
import play.boilerplate.api.client.dsl.Compat

object UserJwtRequest {

  def withSession(request: Compat.WSRequest, user: Option[UserModel]): Compat.WSRequest = {
    user.map(UserJwtSession.newSession).fold(request)(
      session => request.withHeaders(JwtSession.HEADER_NAME -> (JwtSession.TOKEN_PREFIX + session.serialize))
    )
  }

}
