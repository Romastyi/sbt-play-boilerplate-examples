package com.github.romastyi.api.client.dsl

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import play.api.Configuration
import play.api.libs.ws.WSClient
import play.api.libs.ws.ahc.AhcWSClient
import play.boilerplate.api.client.dsl._
import scaldi.Module

class WebServicesComponents extends Module {
  bind [WSClient] to {
    implicit val system: ActorSystem = inject[ActorSystem]
    implicit val mat: ActorMaterializer = ActorMaterializer()
    AhcWSClient()
  }
  bind [CircuitBreakersPanel] to {
    implicit val system: ActorSystem = inject[ActorSystem]
    val config = inject[Configuration].underlying
    AkkaCircuitBreakersPanel.instance(config.getConfig("circuit-breaker"))
  }
  // Service locators
  bind [ServiceLocator] identifiedBy 'config to {
    implicit val circuitBreaker: CircuitBreakersPanel = inject[CircuitBreakersPanel]
    val config = inject[Configuration].underlying
    ServiceLocator.DefaultImpl(config.getConfig("discovery"))
  }
  bind [ServiceLocator] identifiedBy 'consul to {
    implicit val circuitBreaker: CircuitBreakersPanel = inject[CircuitBreakersPanel]
    val config = inject[Configuration].underlying
    ConsulServiceLocator.instance(config.getConfig("discovery"))
  }
}
