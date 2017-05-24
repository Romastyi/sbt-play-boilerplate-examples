import play.api._
/* Play 2.5.x */
/*
import akka.actor._
import akka.stream._
import play.api.libs.ws.ahc._
*/
/* Play 2.4.x */
import play.api.libs.ws.ning.NingWSClient

import scala.concurrent.Await
import scala.concurrent.duration._

object Global extends GlobalSettings {

  import play.api.http.HeaderNames._

  override def onStart(app: Application) {

    /* Play 2.5.x */
    /*
        implicit val system = ActorSystem()
        implicit val materializer = ActorMaterializer()
        val ws = AhcWSClient()
    */
    /* Play 2.4.x */
    val ws = NingWSClient()

    /* Here insert SESSION_ID after login on server. */
    val sessionId = "22670a5fecc24cf76ab1ee98803c0a89dd291c641"
    val client =
      new swagger.codegen.client.PetStoreClient(ws)("http://localhost:9000", COOKIE -> s"PLAY2AUTH_SESS_ID=$sessionId")

    val dog = swagger.codegen.newPet(id = Some(1), name = "dog", tag = None)
    val cat = swagger.codegen.newPet(id = Some(2), name = "cat", tag = None)

    println("pets now are "+Await.result(client.findPets(None, Some(100)), 30 seconds))

    Await.result(client.addPet(dog), 30 seconds)
    Await.result(client.addPet(cat), 30 seconds)

    println("pet 1 is a "+Await.result(client.findPetById(1), 30 seconds))
    println("pet 2 is a "+Await.result(client.findPetById(2), 30 seconds))

    println("pets now are "+Await.result(client.findPets(None, Some(100)), 30 seconds))
  }

}
