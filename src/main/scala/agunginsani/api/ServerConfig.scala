package agunginsani.api

import com.comcast.ip4s.{Host, Ipv4Address, Ipv6Address, Port}

final case class ConfigError(message: String)

final case class ServerConfig(host: Host, port: Port)

object ServerConfig:
  def fromEnv(env: Map[String, String]): Either[ConfigError, ServerConfig] =
    val hostText = env.getOrElse("API_HOST", "0.0.0.0")
    val portText = env.getOrElse("API_PORT", "8080")
    val host =
      Ipv4Address.fromString(hostText).map(address => address: Host)
        .orElse(Ipv6Address.fromString(hostText).map(address => address: Host))

    for
      parsedHost <- host.toRight(
        ConfigError(s"API_HOST must be an IPv4 or IPv6 address; received '$hostText'")
      )
      portNumber <- portText.toIntOption.toRight(
        ConfigError(s"API_PORT must be an integer from 1 to 65535; received '$portText'")
      )
      _ <- Either.cond(
        portNumber >= 1 && portNumber <= 65535,
        (),
        ConfigError(s"API_PORT must be from 1 to 65535; received '$portText'")
      )
      port <- Port.fromInt(portNumber).toRight(
        ConfigError(s"API_PORT is not a valid TCP port: '$portText'")
      )
    yield ServerConfig(parsedHost, port)
