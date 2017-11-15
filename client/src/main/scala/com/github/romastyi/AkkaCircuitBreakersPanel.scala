package com.github.romastyi

import akka.actor.ActorSystem
import akka.pattern.{CircuitBreaker => AkkaCircuitBreaker}
import com.typesafe.config.Config
import play.boilerplate.utils.{CircuitBreaker, CircuitBreakerConfig, CircuitBreakersPanel}

import scala.concurrent.{ExecutionContext, Future}

final class AkkaCircuitBreakersPanel(config: Config)(implicit system: ActorSystem) extends CircuitBreakersPanel.FromConfig(config) {

  override def createCircuitBreaker(breakerConfig: CircuitBreakerConfig): CircuitBreaker = {
    new CircuitBreaker {

      lazy val breaker = new AkkaCircuitBreaker(
        system.scheduler,
        breakerConfig.maxFailures,
        breakerConfig.callTimeout,
        breakerConfig.resetTimeout
      )(ExecutionContext.global)

      override def withCircuitBreaker[T](block: => Future[T]): Future[T] = {
        breaker.withCircuitBreaker(block)
      }

    }
  }

}

object AkkaCircuitBreakersPanel {

  def instance(config: Config)(implicit system: ActorSystem): CircuitBreakersPanel = new AkkaCircuitBreakersPanel(config)

}