package agunginsani.api

import cats.effect.IO
import munit.CatsEffectSuite
import org.http4s.{Method, Request, Status}
import org.http4s.implicits.*
import agunginsani.api.HealthRoutes

final class HealthRoutesSuite extends CatsEffectSuite:
  private val app = HealthRoutes.routes.orNotFound

  test("GET /health returns the health JSON"):
    for
      response <- app.run(Request[IO](Method.GET, uri"/health"))
      body <- response.as[String]
    yield
      assertEquals(response.status, Status.Ok)
      assertEquals(
        response.contentType.map(_.mediaType),
        Some(org.http4s.MediaType.application.json)
      )
      assertEquals(body, """{"status":"ok"}""")

  test("other methods and paths are not treated as health checks"):
    for
      wrongPath <- app.run(Request[IO](Method.GET, uri"/missing"))
      wrongMethod <- app.run(Request[IO](Method.POST, uri"/health"))
    yield
      assertEquals(wrongPath.status, Status.NotFound)
      assertEquals(wrongMethod.status, Status.NotFound)
