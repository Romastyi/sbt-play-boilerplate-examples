package com.github.romastyi.api.client.dsl

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import play.api.{Application, Configuration}
import play.api.libs.ws.WSClient
import play.api.libs.ws.ahc.AhcWSClient
import play.boilerplate.api.client.dsl.{CircuitBreakersPanel, ServiceLocator}
import scaldi.Module

class WebServicesComponents extends Module {
  private lazy val config = inject[Configuration].underlying
  implicit lazy val system: ActorSystem = inject[Application].actorSystem
  implicit lazy val materializer: ActorMaterializer = ActorMaterializer()
  implicit lazy val circuitBreakers: CircuitBreakersPanel = AkkaCircuitBreakersPanel.instance(config.getConfig("circuit-breaker"))
  bind [WSClient] to AhcWSClient()
  bind [CircuitBreakersPanel] to circuitBreakers
  bind [ServiceLocator] identifiedBy 'config to ServiceLocator.DefaultImpl(config.getConfig("discovery"))
  bind [ServiceLocator] identifiedBy 'consul to ConsulServiceLocator.instance(config.getConfig("discovery"))
}
