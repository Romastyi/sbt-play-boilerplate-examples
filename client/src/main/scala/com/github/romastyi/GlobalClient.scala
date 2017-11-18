package com.github.romastyi

import akka.actor.ActorSystem
import play.api.{Application, GlobalSettings}
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.json.Json
import play.api.libs.ws.{WSClient, WSRequest}
import play.api.libs.ws.ning.NingWSClient
import play.boilerplate.utils.{CircuitBreakersPanel, ServiceLocator}
import test.api.client.PetStoreClient
import test.api.model.NewPet

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._

object GlobalClient extends GlobalSettings {

  import play.api.http.HeaderNames._

  override def onStart(app: Application) {

    val config = app.configuration.underlying

    implicit val ws: WSClient = NingWSClient()
    implicit val system: ActorSystem = app.actorSystem
    implicit val circuitBreakers: CircuitBreakersPanel = AkkaCircuitBreakersPanel.instance(config.getConfig("circuit-breaker"))
    implicit val locator: ServiceLocator = ConsulServiceLocator.instance(config)
      /*ServiceLocator.Static {
        case _ => new java.net.URI("http://localhost:9000")
      }*/
    /* Here insert SESSION_ID after login on server. */
//    val sessionId = "22670a5fecc24cf76ab1ee98803c0a89dd291c641" // user1
    implicit val handler: PetStoreClient.RequestHandler = new PetStoreClient.RequestHandler {
      override def handleRequest(operationId: String, request: WSRequest): Future[WSRequest] = {
        locator.doServiceCall("petStore", "login") { uri =>
          for {
            response <- ws.url(s"$uri/login").post(Json.obj("email" -> "user1", "password" -> "pass"))
          } yield response.cookie("PLAY2AUTH_SESS_ID").flatMap(_.value) match {
            case Some(sessionId) => request.withHeaders(COOKIE -> s"PLAY2AUTH_SESS_ID=$sessionId")
            case None => request
          }
        }
      }
      override def onSuccess(operationId: String, response: AnyRef): Unit = ()
      override def onError(operationId: String, cause: Throwable): Unit = ()
    }

    val client = new PetStoreClient

    val dog = NewPet(id = Some(1), name = "dog", tag = None)
    val cat = NewPet(id = Some(2), name = "cat", tag = None)

    println("pets now are " + Await.result(client.findPets(None, Some(100)), 30 seconds))

    Await.result(client.addPet(dog), 30 seconds)
    Await.result(client.addPet(cat), 30 seconds)

    println("pet 1 is a " + Await.result(client.findPetById(1), 30 seconds))
    println("pet 2 is a " + Await.result(client.findPetById(2), 30 seconds))

    println("pets now are " + Await.result(client.findPets(None, Some(100)), 30 seconds))
  }

}
