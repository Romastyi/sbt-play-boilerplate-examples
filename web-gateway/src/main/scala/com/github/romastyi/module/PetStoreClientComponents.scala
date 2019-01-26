package com.github.romastyi.module

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.github.romastyi.api.client.{PetStoreClient, PetStoreClientRequestHandler}
import com.github.romastyi.api.service.PetStoreService
import play.api.libs.ws.StandaloneWSClient
import play.api.libs.ws.ahc.StandaloneAhcWSClient
import play.boilerplate.api.client.dsl.ServiceLocator
import play.shaded.ahc.org.asynchttpclient.AsyncHttpClient
import scaldi.Module

import scala.concurrent.{ExecutionContext, Future}

class PetStoreClientComponents extends Module {

  bind [StandaloneWSClient] to {
    implicit val sys: ActorSystem = inject[ActorSystem]
    implicit val mat: ActorMaterializer = ActorMaterializer()
    new StandaloneAhcWSClient(inject[AsyncHttpClient])
  }

  bind [PetStoreService[Future]] to {
    implicit val ec: ExecutionContext = inject[ExecutionContext]
    implicit val ws: StandaloneWSClient = inject[StandaloneWSClient]
    implicit val locator: ServiceLocator = inject[ServiceLocator]('config)/*('consul)*/
    new PetStoreClient(PetStoreClientRequestHandler)
  }

}
