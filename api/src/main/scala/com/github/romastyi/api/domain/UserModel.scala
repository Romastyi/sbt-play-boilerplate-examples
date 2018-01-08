package com.github.romastyi.api.domain

import com.mohiva.play.silhouette.api.Identity
import play.api.libs.json._

/**
  * Created by romastyi on 06.05.17.
  */

case class UserModel(id: Long, email: String, password: String, role: UserRole.Value) extends Identity

object UserModel {

  private val repo = List(
    UserModel(1, "admin@example.com", "pass", UserRole.admin),
    UserModel(2, "api@example.com"  , "pass", UserRole.api),
    UserModel(3, "user@example.com" , "pass", UserRole.user)
  )

  def authenticate(email: String, pass: String): Option[UserModel] = {
    repo.find(
      user => user.email == email && user.password == pass
    )
  }

  def findByEmail(email: String): Option[UserModel] = {
    repo.find(
      user => user.email == email
    )
  }

  implicit val jsonUserModel: Format[UserModel] = Json.format

}
