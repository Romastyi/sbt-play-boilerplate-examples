package com.github.romastyi.api.client

import com.github.romastyi.api.client.dsl.UserJwtRequest
import play.boilerplate.api.client.dsl.{Compat, Credentials, RequestHandler}

import scala.concurrent.Future

trait PetStoreClientRequestHandler extends RequestHandler[PetStoreClient] {
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

object PetStoreClientRequestHandler extends PetStoreClientRequestHandler