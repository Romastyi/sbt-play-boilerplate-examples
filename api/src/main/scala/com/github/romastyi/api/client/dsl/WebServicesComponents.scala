package com.github.romastyi.api.client.dsl

import akka.actor.ActorSystem
import play.api.{Application, Configuration}
import play.api.libs.ws.{WS, WSClient}
import play.boilerplate.api.client.dsl.{AkkaCircuitBreakersPanel, CircuitBreakersPanel, ServiceLocator}
import scaldi.Module

class WebServicesComponents extends Module {
  private lazy val config = inject[Configuration].underlying
  implicit lazy val app: Application = inject[Application]
  implicit lazy val system: ActorSystem = ActorSystem()
  implicit lazy val circuitBreakers: CircuitBreakersPanel = AkkaCircuitBreakersPanel.instance(config.getConfig("circuit-breaker"))
  bind [WSClient] to WS.client
  bind [CircuitBreakersPanel] to circuitBreakers
  bind [ServiceLocator] identifiedBy 'config to ServiceLocator.DefaultImpl(config.getConfig("discovery"))
  bind [ServiceLocator] identifiedBy 'consul to ConsulServiceLocator.instance(config.getConfig("discovery"))
}
