package com.github.romastyi.module

import com.github.romastyi.api.client.PetStoreClient
import com.github.romastyi.api.client.dsl.UserJwtRequest
import com.github.romastyi.api.domain.UserModel
import com.github.romastyi.api.service.PetStoreService
import play.api.libs.ws.{WSClient, WSRequest}
import play.boilerplate.api.client.dsl.ServiceLocator
import scaldi.Module

import scala.concurrent.Future

class PetStoreClientComponents extends Module {
  implicit lazy val ws: WSClient = inject[WSClient]
  implicit lazy val locator: ServiceLocator = inject[ServiceLocator]('config)/*('consul)*/
  implicit val handler: PetStoreClient.RequestHandler = new PetStoreClient.RequestHandler {
    override def handleRequest(operationId: String, request: WSRequest, user: Option[UserModel]): Future[WSRequest] = {
      Future.successful(UserJwtRequest.withSession(request, user))
    }
    override def onSuccess(operationId: String, response: AnyRef, user: Option[UserModel]): Unit = ()
    override def onError(operationId: String, cause: Throwable, user: Option[UserModel]): Unit = ()
  }
  bind [PetStoreService] to new PetStoreClient
}
