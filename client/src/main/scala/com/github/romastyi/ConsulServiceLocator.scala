package com.github.romastyi

import java.net.{InetAddress, URI}

import com.ecwid.consul.v1.catalog.model.CatalogService
import com.ecwid.consul.v1.{ConsulClient, QueryParams}
import com.typesafe.config.Config
import play.boilerplate.utils.{CircuitBreakersPanel, ServiceLocator}

import scala.collection.JavaConverters._
import scala.concurrent.Future

final class ConsulServiceLocator(client: ConsulClient, config: ConsulConfig, circuitBreakers: CircuitBreakersPanel)
  extends ServiceLocator(circuitBreakers) {

  override def locate(name: String): Future[Option[URI]] = {
    Future {
      client.getCatalogService(name, QueryParams.DEFAULT).getValue.asScala.headOption.map(toURI)
    }
  }

  private def toURI(service: CatalogService): URI = {
    val address = service.getServiceAddress
    val serviceAddress = if (address.trim.isEmpty || address == "localhost") {
      InetAddress.getLoopbackAddress.getHostAddress
    } else address
    new URI(s"${config.scheme}://$serviceAddress:${service.getServicePort}")
  }

}

object ConsulServiceLocator {

  def instance(config: Config, circuitBreakers: CircuitBreakersPanel): ServiceLocator = {
    val consulConfig = ConsulConfig.fromConfig(config)
    val consulClient = new ConsulClient(consulConfig.agentHostname, consulConfig.agentPort)
    new ConsulServiceLocator(consulClient, consulConfig, circuitBreakers)
  }

}