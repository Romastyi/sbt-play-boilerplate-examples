package com.github.romastyi.module

import com.github.romastyi.api.client.PetStoreClient
import com.github.romastyi.api.client.dsl.UserJwtRequest
import com.github.romastyi.api.domain.UserModel
import com.github.romastyi.api.service.PetStoreService
/*
import com.github.romastyi.api.silhouette.JWTEnv
import com.mohiva.play.silhouette.api.util.Credentials
import com.mohiva.play.silhouette.api.Silhouette
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
*/
import play.api.libs.ws.{WSClient, WSRequest, WSResponse}
import play.boilerplate.api.client.dsl.ServiceLocator
import scaldi.Module

import scala.concurrent.{ExecutionContext, Future}

class PetStoreClientComponents extends Module {

/*
  lazy val silhouette: Silhouette[JWTEnv] = inject[Silhouette[JWTEnv]]
  lazy val credentialsProvider: CredentialsProvider = inject[CredentialsProvider]
*/

  val handler: PetStoreClient.RequestHandler = new PetStoreClient.RequestHandler {
/*
    import play.api.libs.typedmap.TypedMap
    import play.api.mvc.{Headers, RequestHeader}
    import play.api.mvc.request.{RemoteConnection, RequestTarget}
    // FIXME Just workaround trick !!!
    def wsRequest2RequestHeader(request: WSRequest): RequestHeader = {
      new RequestHeader {
        override def connection: RemoteConnection = null
        override def method: String = request.method
        override def version: String = ""
        override def attrs: TypedMap = null
        override def headers: Headers = new Headers(Nil)
        override def target: RequestTarget = RequestTarget(request.uri.toString, request.uri.getPath, request.queryString)
      }
    }
    override def handleRequest(operationId: String, request: WSRequest, loggedUser: Option[UserModel]): Future[WSRequest] = {
      loggedUser match {
        case Some(user) =>
          implicit val fakeRequest: RequestHeader = wsRequest2RequestHeader(request)
          for {
            loginInfo <- credentialsProvider.loginInfo(Credentials(user.email, user.password))
            authenticator <- silhouette.env.authenticatorService.create(loginInfo)
            token <- silhouette.env.authenticatorService.init(authenticator)
            r = silhouette.env.authenticatorService.embed(token, fakeRequest)
          } yield request.addHttpHeaders(r.headers.headers: _ *)
        case None =>
          Future.successful(request)
      }
    }
*/

    override def handleRequest(operationId: String, request: WSRequest, user: Option[UserModel]): Future[WSRequest] = {
      Future.successful(UserJwtRequest.withSession(request, user))
    }
    override def onSuccess(operationId: String, response: WSResponse, user: Option[UserModel]): Unit = ()
    override def onError(operationId: String, cause: Throwable, user: Option[UserModel]): Unit = ()
  }

  bind [PetStoreService] to {
    implicit val ec: ExecutionContext = inject[ExecutionContext]
    implicit val ws: WSClient = inject[WSClient]
    implicit val locator: ServiceLocator = inject[ServiceLocator]('config)/*('consul)*/
    new PetStoreClient(handler)
  }

}
