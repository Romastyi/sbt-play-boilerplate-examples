package eu.unicredit

/**
  * Created by romastyi on 06.05.17.
  */

case class UserModel(id: Long, email: String, password: String, role: UserRole.Value)

object UserModel {

  def authenticate(email: String, pass: String): Option[UserModel] = {
    println(email)
    println(pass.map(_ => '*'))
    val regex = """user(\d)""".r
    (email, pass) match {
      case (regex(id), "pass") =>
        Some(UserModel(id.toLong, email, pass, UserRole.apply(id.toInt)))
      case _ =>
        None
    }
  }

}
