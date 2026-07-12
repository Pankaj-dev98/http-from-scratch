# http-from-scratch

`http-from-scratch` is a Maven project that implements a small HTTP/1.1 server directly on top of TCP sockets. It parses raw request bytes, validates the request line and headers, enforces size limits, and writes well-formed HTTP responses back to the client.

## What it does

- Parses raw HTTP/1.1 requests from a socket stream
- Handles request lines, headers, and optional bodies
- Rejects malformed requests early
- Limits header size and count to reduce buffer-overflow style abuse
- Supports standard `Content-Length` request bodies
- Serves dynamic responses as `text/html`, `text/plain`, and other MIME types through response headers
- Proxies selected paths to `httpbingo.org`
- Streams proxied data with `Transfer-Encoding: chunked`
- Emits HTTP trailers for streamed responses
- Calls external services through Java `HttpClient`
- Uses virtual threads for concurrent connection handling

## Routes

The server starts on port `42069` by default and honors the `PORT` environment variable for deployment platforms.

Available routes:

- `/` - serves the home page from `src/main/resources/root-page.html`
- `/funfact` - fetches a random fun fact from an external API and returns it as plain text
- `/httpbingo/*` - proxies the request to `httpbingo.org` and streams the upstream response

Examples:

- `/httpbingo/stream/10`
- `/httpbingo/stream/100`
- `/httpbingo/json`
- `/httpbingo/anything`

## Proxy behavior

Requests that begin with `/httpbingo/` are forwarded to `https://httpbingo.org`. The response is streamed back to the client using chunked encoding instead of buffering the entire body in memory.

For streamed proxy responses, the server also adds trailers:

- `X-Content-SHA256`
- `X-Content-Length`

This lets the server compute integrity metadata while streaming data through.

## Safety and validation

The request parser is intentionally defensive:

- Maximum header size: `8 KB`
- Maximum header count: `100`
- Request bodies must match the declared `Content-Length`
- Invalid request lines, headers, and HTTP versions are rejected

This keeps the parser bounded and reduces the risk of runaway reads or malformed input exhausting memory.

## Response types

The server can return different content types depending on the route and payload:

- `text/html` for the home page and HTML error responses
- `text/plain` for the fun fact endpoint
- `application/json` and other upstream content types when proxying `httpbingo`

## Build

```bash
mvn test
mvn package
```

Packaging compiles the server and copies static assets into `target/classes`.

## Run

```bash
java -cp target/classes org.orgless.Main
```

For local development:

```bash
PORT=42069 java -cp target/classes org.orgless.Main
```

If you are running from an IDE, start `org.orgless.Main`.

## Docker

```bash
docker build -t http-from-scratch .
docker run -p 42069:42069 -e PORT=42069 http-from-scratch
```

This repository includes a `Dockerfile` so the server can be deployed to container-based platforms such as Render, Railway, Fly.io, or any VPS that accepts a Docker image.

## Notes

- The project targets Java 21 for broader deployment compatibility while retaining virtual threads.
- Tests cover request parsing, headers, and body handling.