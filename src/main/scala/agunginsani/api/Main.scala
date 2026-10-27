package agunginsani.api

import cats.effect.{IO, IOApp}
import cats.syntax.all.*
import org.http4s.ember.server.EmberServerBuilder

object Main extends IOApp.Simple:
  def run: IO[Unit] =
    IO.fromEither(
      ServerConfig
        .fromEnv(sys.env)
        .left
        .map(error => new IllegalArgumentException(error.message))
    ).flatMap { config =>
      EmberServerBuilder
        .default[IO]
        .withHost(config.host)
        .withPort(config.port)
        .withHttpApp(
          (HealthRoutes.routes <+> LeetCodeRoutes.routes <+> SpreadsheetRoutes.routes).orNotFound
        )
        .build
        .useForever
    }
