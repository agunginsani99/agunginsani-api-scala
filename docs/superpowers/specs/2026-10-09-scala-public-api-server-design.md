# Scala Public API Server Bootstrap

## Goal

Create a minimal, runnable public HTTP server foundation for this currently
empty Scala workspace. The bootstrap provides a health endpoint and
environment-based listener configuration, without introducing application
business features.

## Scope

### In scope

- Scala 3 project built with sbt.
- http4s with the Ember server and cats-effect runtime.
- `GET /health`, returning HTTP 200 and the JSON body
  `{"status":"ok"}`.
- `API_HOST` and `API_PORT` environment variables.
- A small automated test suite for configuration and the health route.

### Out of scope

- CRUD or other domain/API routes.
- Database or external-service integration.
- Authentication, authorization, CORS, and OpenAPI.
- Deployment manifests or containerization.

## Architecture

The application entry point loads and validates listener configuration,
constructs the health route, and starts the Ember server under cats-effect.
Keep configuration parsing, route definition, and server startup as
independently testable responsibilities, using small modules or functions
within the project's initial source layout.

The route is deliberately independent of external services. It responds only
to `GET /health` with status 200 and the JSON object `{"status":"ok"}`.
Other methods or paths retain http4s' normal not-found behavior.

## Listener configuration

Read the following environment variables:

| Variable | Default | Meaning |
| --- | --- | --- |
| `API_HOST` | `0.0.0.0` | IPv4 or IPv6 address on which the server listens |
| `API_PORT` | `8080` | TCP port on which the server listens |

Defaults apply only when a variable is absent. A provided but malformed host,
non-integer port, or port outside `1–65535` is a startup error. Report the
invalid setting clearly and fail startup; do not silently substitute a default.
`0.0.0.0` is intentional so the server is reachable outside its local process,
including from a container network.

## Build and test shape

Use the standard sbt layout (`build.sbt`, `project/build.properties`,
`src/main/scala`, and `src/test/scala`). Select stable dependency versions
compatible with Scala 3 and the http4s APIs at implementation time. Use a
lightweight Scala 3-compatible test framework with cats-effect support, such
as MUnit Cats Effect.

Tests should verify:

- Missing environment values resolve to the documented defaults.
- Valid host and port overrides are accepted.
- Invalid host values and invalid/out-of-range ports are rejected.
- `GET /health` returns status 200 and the exact JSON response.
- Unsupported method/path combinations do not accidentally return the health
  response.

## Operational behavior

On valid configuration, the process starts the HTTP listener and remains alive
until cancelled or terminated. Configuration errors must be surfaced as
startup failures with actionable messages. The health response indicates only
that the server process and route are available; it does not claim readiness
of dependencies, since this bootstrap has none.

## Acceptance criteria

1. The empty workspace becomes a Scala 3 sbt project that can compile and run.
2. A server configured with defaults listens on `0.0.0.0:8080`.
3. `API_HOST` and `API_PORT` can override the defaults when valid.
4. Invalid supplied configuration fails startup instead of silently falling
   back.
5. `GET /health` returns HTTP 200 with `{"status":"ok"}`.
6. Automated tests cover configuration validation and the route contract.
7. No out-of-scope API or security feature is introduced.
