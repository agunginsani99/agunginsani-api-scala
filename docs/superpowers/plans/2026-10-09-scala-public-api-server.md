# Scala Public API Server Bootstrap Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Turn the empty workspace into a runnable Scala 3/http4s public API server with strict environment configuration and a tested health endpoint.

**Architecture:** Keep listener configuration parsing, health-route definition, and process startup in separate small modules. The entry point composes them with cats-effect and http4s Ember; the health route has no external dependencies.

**Tech Stack:** Scala 3.3.6, sbt 1.10.7, http4s 0.23.30, cats-effect 3.7.1, MUnit Cats Effect 2.0.0.

---

## File map

- Create `build.sbt` — Scala version, application metadata, HTTP/runtime dependencies, and test dependency.
- Create `project/build.properties` — pin the sbt launcher version.
- Create `src/main/scala/expenses/api/ServerConfig.scala` — pure parsing and validation for `API_HOST` and `API_PORT`.
- Create `src/test/scala/expenses/api/ServerConfigSuite.scala` — configuration defaults, overrides, and failure cases.
- Create `src/main/scala/expenses/api/HealthRoutes.scala` — the single `GET /health` route.
- Create `src/test/scala/expenses/api/HealthRoutesSuite.scala` — route method, path, status, content type, and response-body tests.
- Create `src/main/scala/expenses/api/Main.scala` — read process environment, fail visibly on invalid config, and run Ember.
- Create `README.md` — describe the service, environment variables, and local run/test commands.

This workspace has no Git repository, so the tasks intentionally contain no Git commit commands.

### Task 1: Create the sbt project

**Files:**
- Create: `build.sbt`
- Create: `project/build.properties`

- [ ] **Step 1: Create the Scala 3 build definition**

Create `build.sbt` with:

```scala
ThisBuild / scalaVersion := "3.3.6"
ThisBuild / version := "0.1.0-SNAPSHOT"

lazy val root = (project in file("."))
  .settings(
    name := "expenses-tracker-scala",
    Compile / run / fork := true,
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-effect" % "3.7.1",
      "org.http4s" %% "http4s-ember-server" % "0.23.30",
      "org.http4s" %% "http4s-dsl" % "0.23.30",
      "org.typelevel" %% "munit-cats-effect" % "2.0.0" % Test
    )
  )
```

- [ ] **Step 2: Pin sbt**

Create `project/build.properties` with:

```properties
sbt.version=1.10.7
```

- [ ] **Step 3: Resolve dependencies and validate the empty project**

Run: `sbt test`

Expected: sbt resolves the declared artifacts and exits successfully. No test suites exist yet, so zero tests are reported.

### Task 2: Parse listener configuration

**Files:**
- Create: `src/test/scala/expenses/api/ServerConfigSuite.scala`
- Create: `src/main/scala/expenses/api/ServerConfig.scala`

- [ ] **Step 1: Write configuration tests first**

Create `ServerConfigSuite.scala`:

```scala
package expenses.api

import munit.FunSuite

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
```

- [ ] **Step 2: Run the focused tests and verify they fail**

Run: `sbt "testOnly expenses.api.ServerConfigSuite"`

Expected: compilation fails because `ServerConfig` does not exist yet.

- [ ] **Step 3: Implement the parser and typed config**

Create `ServerConfig.scala`:

```scala
package expenses.api

import com.comcast.ip4s.{Host, Port}

final case class ConfigError(message: String)

final case class ServerConfig(host: Host, port: Port)

object ServerConfig:
  def fromEnv(env: Map[String, String]): Either[ConfigError, ServerConfig] =
    val hostText = env.getOrElse("API_HOST", "0.0.0.0")
    val portText = env.getOrElse("API_PORT", "8080")

    for
      host <-
        Host
          .fromString(hostText)
          .toRight(ConfigError(s"API_HOST must be an IPv4 or IPv6 address; received '$hostText'"))
      portNumber <-
        portText.toIntOption.toRight(
          ConfigError(s"API_PORT must be an integer from 1 to 65535; received '$portText'")
        )
      _ <-
        Either.cond(
          portNumber >= 1 && portNumber <= 65535,
          (),
          ConfigError(s"API_PORT must be from 1 to 65535; received '$portText'")
        )
      port <-
        Port
          .fromInt(portNumber)
          .toRight(ConfigError(s"API_PORT is not a valid TCP port: '$portText'"))
    yield ServerConfig(host, port)
```

- [ ] **Step 4: Run the focused tests and verify they pass**

Run: `sbt "testOnly expenses.api.ServerConfigSuite"`

Expected: all five configuration tests pass.

### Task 3: Add the health route

**Files:**
- Create: `src/test/scala/expenses/api/HealthRoutesSuite.scala`
- Create: `src/main/scala/expenses/api/HealthRoutes.scala`

- [ ] **Step 1: Write route contract tests first**

Create `HealthRoutesSuite.scala`:

```scala
package expenses.api

import cats.effect.IO
import munit.CatsEffectSuite
import org.http4s.{Method, Request, Status}
import org.http4s.implicits.*

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
```

- [ ] **Step 2: Run the focused tests and verify they fail**

Run: `sbt "testOnly expenses.api.HealthRoutesSuite"`

Expected: compilation fails because `HealthRoutes` does not exist yet.

- [ ] **Step 3: Implement the route**

Create `HealthRoutes.scala`:

```scala
package expenses.api

import cats.effect.IO
import org.http4s.{HttpRoutes, MediaType, Response, Status}
import org.http4s.dsl.io.*

object HealthRoutes:
  val routes: HttpRoutes[IO] = HttpRoutes.of[IO]:
    case GET -> Root / "health" =>
      val response = Response[IO](status = Status.Ok)
        .withEntity("""{"status":"ok"}""")
        .withContentType(MediaType.application.json)
      IO.pure(response)
```

- [ ] **Step 4: Run the focused tests and verify they pass**

Run: `sbt "testOnly expenses.api.HealthRoutesSuite"`

Expected: both route tests pass, including the exact JSON body and JSON content type.

### Task 4: Wire startup and document operation

**Files:**
- Create: `src/main/scala/expenses/api/Main.scala`
- Create: `README.md`

- [ ] **Step 1: Add the cats-effect application entry point**

Create `Main.scala`:

```scala
package expenses.api

import cats.effect.{IO, IOApp}
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
        .withHttpApp(HealthRoutes.routes.orNotFound)
        .build
        .useForever
    }
```

- [ ] **Step 2: Document configuration and commands**

Create `README.md`:

````markdown
# Expenses Tracker API

A minimal Scala 3/http4s server bootstrap.

## Run

Start with the defaults:

```sh
sbt run
```

The server listens on `0.0.0.0:8080`. Override the listener with environment
variables:

```sh
API_HOST=127.0.0.1 API_PORT=9000 sbt run
```

`API_HOST` must be an IPv4 or IPv6 address. `API_PORT` must be an integer from
1 through 65535. Invalid supplied values stop startup with an error.

The `GET /health` endpoint returns HTTP 200 with `{"status":"ok"}`.

Run tests with:

```sh
sbt test
```
````

- [ ] **Step 3: Run the complete automated test suite**

Run: `sbt test`

Expected: all configuration and route tests pass.

- [ ] **Step 4: Smoke-test a configured server**

Start the server in one terminal:

```sh
API_HOST=127.0.0.1 API_PORT=18080 sbt run
```

In another terminal, run:

```sh
curl --fail --silent --show-error http://127.0.0.1:18080/health
```

Expected response body: `{"status":"ok"}`. Stop the server with Ctrl+C.

- [ ] **Step 5: Verify invalid configuration fails startup**

Run:

```sh
API_PORT=invalid sbt run
```

Expected: the process exits unsuccessfully and reports that `API_PORT` must be an integer from 1 to 65535. It must not start on port 8080.

## Self-review against the design

- The sbt project, Scala 3/http4s/Ember/cats-effect stack, and standard source layout are established in Task 1.
- Defaults, valid overrides, malformed host, non-integer port, and out-of-range ports are implemented and tested in Task 2.
- The exact `GET /health` status, JSON media type/body, and unmatched requests are implemented and tested in Task 3.
- Process startup, strict environment validation at launch, run instructions, full tests, and a live endpoint smoke test are covered in Task 4.
- The plan adds no CRUD, persistence, authentication, CORS, OpenAPI, or deployment work.
