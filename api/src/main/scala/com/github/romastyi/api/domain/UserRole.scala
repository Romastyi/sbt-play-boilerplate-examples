package com.github.romastyi.api.domain

import play.api.libs.json._

/**
  * Created by romastyi on 06.05.17.
  */

object UserRole extends Enumeration {

  val admin, api, user = Value

  val all: Set[Value] = values

  implicit val jsonUserRole: Format[Value] = new Format[Value] {
    override def writes(o: Value): JsValue = JsString(o.toString)
    override def reads(json: JsValue): JsResult[Value] = json match {
      case JsString(name) => JsSuccess(withName(name))
      case _ => JsError("Wrong value for UserRole.Value")
    }
  }
}
