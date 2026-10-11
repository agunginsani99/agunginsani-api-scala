package agunginsani.api

import munit.FunSuite
import agunginsani.api.ServerConfig

final class ServerConfigSuite extends FunSuite:
  test("uses public listener defaults when variables are absent"):
    val result = ServerConfig.fromEnv(Map.empty)
    assertEquals(
      result.map(config => (config.host.toString, config.port.value)),
      Right(("0.0.0.0", 8080))
    )

  test("accepts valid host and port overrides"):
    val result = ServerConfig.fromEnv(
      Map("API_HOST" -> "127.0.0.1", "API_PORT" -> "9000")
    )
    assertEquals(
      result.map(config => (config.host.toString, config.port.value)),
      Right(("127.0.0.1", 9000))
    )

  test("rejects a malformed host"):
    assert(ServerConfig.fromEnv(Map("API_HOST" -> "not-an-ip")).isLeft)

  test("rejects a non-integer port"):
    assert(ServerConfig.fromEnv(Map("API_PORT" -> "abc")).isLeft)

  test("rejects ports outside 1 through 65535"):
    List("0", "65536", "-1").foreach { value =>
      assert(ServerConfig.fromEnv(Map("API_PORT" -> value)).isLeft)
    }
