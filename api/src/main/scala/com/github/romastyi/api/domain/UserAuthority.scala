package com.github.romastyi.api.domain

/**
  * Created by romastyi on 06.05.17.
  */

final case class UserAuthority(roles: Set[UserRole.Value]) {
  def validateUser(user: UserModel): Option[UserModel] = {
    if (roles.contains(user.role)) {
      Some(user)
    } else {
      None
    }
  }
}
