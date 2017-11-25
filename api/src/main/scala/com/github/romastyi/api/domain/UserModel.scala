package com.github.romastyi.api.domain

import play.api.libs.json._

import scala.util.control.Exception._
/**
  * Created by romastyi on 06.05.17.
  */

case class UserModel(id: Long, email: String, password: String, role: UserRole.Value)

object UserModel {

  def authenticate(email: String, pass: String): Option[UserModel] = {
    val regex = """(\w+)\@.*""".r
    (email, pass) match {
      case (regex(username), "pass") =>
        for {
          role <- catching(classOf[NoSuchElementException]) opt UserRole.withName(username)
        } yield UserModel(role.id.toLong, email, pass, role)
      case _ =>
        None
    }
  }

  implicit val jsonUserModel: Format[UserModel] = Json.format

}
