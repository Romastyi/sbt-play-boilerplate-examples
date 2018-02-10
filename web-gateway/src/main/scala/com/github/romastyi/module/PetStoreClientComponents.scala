package com.github.romastyi.module

import com.github.romastyi.api.client.{PetStoreClient, PetStoreCredentials}
import com.github.romastyi.api.client.dsl.UserJwtRequest
import com.github.romastyi.api.service.PetStoreService
import play.api.libs.ws.{WSClient, WSRequest, WSResponse}
import play.boilerplate.api.client.dsl.{Credentials, RequestHandler, ServiceLocator}
import scaldi.Module

import scala.concurrent.{ExecutionContext, Future}

class PetStoreClientComponents extends Module {

  val handler: RequestHandler[PetStoreClient] = new RequestHandler[PetStoreClient] {
    override def beforeRequest(operationId: String, request: WSRequest, credentials: Credentials[PetStoreClient]): Future[WSRequest] = {
      credentials match {
        case PetStoreCredentials(logged) =>
          Future.successful(UserJwtRequest.withSession(request, Some(logged)))
        case _ =>
          Future.successful(request)
      }
    }
    override def onSuccess(operationId: String, response: WSResponse, credentials: Credentials[PetStoreClient]): Unit = ()
    override def onError(operationId: String, cause: Throwable, credentials: Credentials[PetStoreClient]): Unit = ()
  }

  bind [PetStoreService] to {
    implicit val ec: ExecutionContext = inject[ExecutionContext]
    implicit val ws: WSClient = inject[WSClient]
    implicit val locator: ServiceLocator = inject[ServiceLocator]('config)/*('consul)*/
    new PetStoreClient(handler)
  }

}
