# Expenses Tracker API

A small Scala 3 HTTP API server built with http4s and cats-effect.

## Run the server

Start the server with:

```sh
sbt run
```

By default, it listens on `0.0.0.0:8080`, binding on all available network interfaces. To override the bind address and port:

```sh
API_HOST=127.0.0.1 API_PORT=9000 sbt run
```

`API_HOST` must be an IPv4 or IPv6 address literal. `API_PORT` must be an integer from 1 through 65535. The defaults (`0.0.0.0` and `8080`) apply only when the corresponding environment variable is absent; invalid values cause startup to fail with a configuration diagnostic.

## Health check

The server responds to `GET /health` with HTTP 200 and the JSON body:

```json
{"status":"ok"}
```

## Tests

Run the test suite with:

```sh
sbt test
```
