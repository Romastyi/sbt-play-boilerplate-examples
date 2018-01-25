package com.github.romastyi.module

import com.github.romastyi.api.client.PetStoreClient
import com.github.romastyi.api.client.dsl.UserJwtRequest
import com.github.romastyi.api.domain.UserModel
import com.github.romastyi.api.service.PetStoreService
import play.api.libs.ws.WSClient
import play.boilerplate.api.client.dsl.{Compat, ServiceLocator}
import scaldi.Module

import scala.concurrent.{ExecutionContext, Future}

class PetStoreClientComponents extends Module {
  val handler: PetStoreClient.RequestHandler = new PetStoreClient.RequestHandler {
    override def handleRequest(operationId: String, request: Compat.WSRequest, user: Option[UserModel]): Future[Compat.WSRequest] = {
      Future.successful(UserJwtRequest.withSession(request, user))
    }
    override def onSuccess(operationId: String, response: Compat.WSResponse, user: Option[UserModel]): Unit = ()
    override def onError(operationId: String, cause: Throwable, user: Option[UserModel]): Unit = ()
  }
  bind [PetStoreService] to {
    implicit val ws: WSClient = inject[WSClient]
    implicit val locator: ServiceLocator = inject[ServiceLocator]('config)/*('consul)*/
    implicit val ec: ExecutionContext = inject[ExecutionContext]
    new PetStoreClient(handler)
  }
}
