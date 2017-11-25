package com.github.romastyi.api.domain

import pdi.jwt.JwtSession

object UserJwtSession {

  def newSession(user: UserModel): JwtSession = JwtSession() + ("user", user)
  def getUser(s: JwtSession): Option[UserModel] = s.getAs[UserModel]("user")

}
