package com.github.romastyi.module

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.github.romastyi.api.client.{PetStoreClient, PetStoreCredentials}
import com.github.romastyi.api.client.dsl.UserJwtRequest
import com.github.romastyi.api.service.PetStoreService
import play.api.libs.ws.StandaloneWSClient
import play.api.libs.ws.ahc.StandaloneAhcWSClient
import play.boilerplate.api.client.dsl.{Compat, Credentials, RequestHandler, ServiceLocator}
import play.shaded.ahc.org.asynchttpclient.AsyncHttpClient
import scaldi.Module

import scala.concurrent.{ExecutionContext, Future}

class PetStoreClientComponents extends Module {

  bind [StandaloneWSClient] to {
    implicit val sys: ActorSystem = inject[ActorSystem]
    implicit val mat: ActorMaterializer = ActorMaterializer()
    new StandaloneAhcWSClient(inject[AsyncHttpClient])
  }

  val handler: RequestHandler[PetStoreClient] = new RequestHandler[PetStoreClient] {
    override def beforeRequest(operationId: String, request: Compat.WSRequest, credentials: Credentials[PetStoreClient]): Future[Compat.WSRequest] = {
      credentials match {
        case PetStoreCredentials(logged) =>
          Future.successful(UserJwtRequest.withSession(request, Some(logged)))
        case _ =>
          Future.successful(request)
      }
    }
    override def onSuccess(operationId: String, response: Compat.WSResponse, credentials: Credentials[PetStoreClient]): Unit = ()
    override def onError(operationId: String, cause: Throwable, credentials: Credentials[PetStoreClient]): Unit = ()
  }

  bind [PetStoreService[Future]] to {
    implicit val ec: ExecutionContext = inject[ExecutionContext]
    implicit val ws: StandaloneWSClient = inject[StandaloneWSClient]
    implicit val locator: ServiceLocator = inject[ServiceLocator]('config)/*('consul)*/
    new PetStoreClient(handler)
  }

}
