package com.github.romastyi

import play.api.libs.ws.ning.NingWSClient
import play.api.{Application, GlobalSettings}
import test.api

import scala.concurrent.Await
import scala.concurrent.duration._

object GlobalClient extends GlobalSettings {

  import play.api.http.HeaderNames._

  override def onStart(app: Application) {

    val ws = NingWSClient()
    val config = app.configuration.underlying
    val circuitBreakers = AkkaCircuitBreakersPanel.instance(config.getConfig("circuit-breaker"))(app.actorSystem)
    val locator = ConsulServiceLocator.instance(config, circuitBreakers)
    /*ServiceLocator.static {
      case _ => new java.net.URI("http://localhost:9000")
    }*/

    /* Here insert SESSION_ID after login on server. */
    val sessionId = "22670a5fecc24cf76ab1ee98803c0a89dd291c641" // user1
    val client = new api.client.PetStoreClient(ws, locator)(COOKIE -> s"PLAY2AUTH_SESS_ID=$sessionId")

    val dog = api.model.NewPet(id = Some(1), name = "dog", tag = None)
    val cat = api.model.NewPet(id = Some(2), name = "cat", tag = None)

    println("pets now are " + Await.result(client.findPets(None, Some(100)), 30 seconds))

    Await.result(client.addPet(dog), 30 seconds)
    Await.result(client.addPet(cat), 30 seconds)

    println("pet 1 is a " + Await.result(client.findPetById(1), 30 seconds))
    println("pet 2 is a " + Await.result(client.findPetById(2), 30 seconds))

    println("pets now are " + Await.result(client.findPets(None, Some(100)), 30 seconds))
  }

}
