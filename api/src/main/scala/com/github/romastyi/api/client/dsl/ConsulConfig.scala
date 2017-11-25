package com.github.romastyi.api.client.dsl

import com.typesafe.config.Config

final case class ConsulConfig(agentHostname: String, agentPort: Int, scheme: String)

object ConsulConfig {

  def fromConfig(config: Config): ConsulConfig = ConsulConfig(
    agentHostname = config.getString("consul.agent-hostname"),
    agentPort = config.getInt("consul.agent-port"),
    scheme = config.getString("consul.uri-scheme")
  )

}