package expenses.api

import cats.effect.IO
import org.http4s.{HttpRoutes, MediaType, Response, Status}
import org.http4s.dsl.io.*

object HealthRoutes:
  val routes: HttpRoutes[IO] = HttpRoutes.of[IO]:
    case GET -> Root / "health" =>
      val response = Response[IO](status = Status.Ok)
        .withEntity("""{"status":"ok"}""")
        .withContentType(org.http4s.headers.`Content-Type`(MediaType.application.json))
      IO.pure(response)
