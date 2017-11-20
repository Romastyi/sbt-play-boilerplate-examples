package com.github.romastyi.api.domain

/**
  * Created by romastyi on 06.05.17.
  */

object UserRole extends Enumeration {
  val admin, api, user = Value
  val all: Set[Value] = values
}
