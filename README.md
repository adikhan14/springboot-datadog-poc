# Bookmark Service — Spring Boot + Datadog POC

A Spring Boot REST API for managing bookmarks, instrumented with Datadog APM tracing and structured JSON logging.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Framework | Spring Boot 3.5 / Java 17 |
| Database | H2 (in-memory) |
| Logging | Log4j2 + JsonTemplateLayout |
| Tracing | Datadog Java Agent + OpenTracing API |
| Runtime | Docker / Amazon Corretto 17 |

---

## Project Structure

```
src/main/java/com/learning/dd/poc/
├── controller/
│   └── BookmarkController.java      # REST endpoints
├── filter/
│   └── RequestIdFilter.java         # MDC + span request ID injection
├── model/
│   └── Bookmark.java                # JPA entity
├── repository/
│   └── BookmarkRepository.java      # Spring Data JPA
├── service/
│   └── BookmarkService.java         # CRUD business logic
└── tracing/
    └── SpanUtils.java               # Datadog span tag helpers

src/main/resources/
├── application.yml                  # App configuration
├── log4j2-spring.xml                # Log4j2 appender config
└── log4j2-json-template.json        # JSON log format template
```

---

## Prerequisites

- Java 17
- Maven 3.8+
- Docker Desktop
- Datadog account + API key

---

## Running Locally (without Docker) ⚠️ Not Tested

### 1. Build

```bash
mvn package -DskipTests
```

### 2. Download Datadog Java Agent

```bash
curl -L -o dd-java-agent.jar https://dtdg.co/latest-java-tracer
```

### 3. Run

```bash
java -javaagent:./dd-java-agent.jar \
  -Ddd.service=bookmark-service \
  -Ddd.env=dev \
  -Ddd.version=1.0 \
  -Ddd.agent.host=localhost \
  -Ddd.logs.injection=true \
  -jar target/springboot-datadog-poc-0.0.1-SNAPSHOT.jar
```

App starts on **http://localhost:8081**

---

## Running with Docker

### 1. Build the JAR

```bash
mvn package -DskipTests
```

### 2. Download Datadog Java Agent

```bash
curl -L -o dd-java-agent.jar https://dtdg.co/latest-java-tracer
```

### 3. Start the Datadog Agent

```bash
docker run -d --name dd-agent \
  --hostname dd-agent-local \
  -e DD_API_KEY=<YOUR_API_KEY> \
  -e DD_SITE="datadoghq.com" \
  -e DD_APM_ENABLED=true \
  -e DD_APM_NON_LOCAL_TRAFFIC=true \
  -e DD_LOGS_ENABLED=true \
  -e DD_LOGS_CONFIG_CONTAINER_COLLECT_ALL=true \
  -v /var/run/docker.sock:/var/run/docker.sock:ro \
  -v /proc/:/host/proc/:ro \
  -v /sys/fs/cgroup/:/host/sys/fs/cgroup:ro \
  -v /var/lib/docker/containers:/var/lib/docker/containers:ro \
  -p 8126:8126 \
  gcr.io/datadoghq/agent:latest
```

### 4. Build the Docker Image

```bash
docker build -t bookmark-service-img .
```

### 5. Run the App Container

```bash
docker run -d --name bookmark-service \
  -p 8081:8081 \
  -e DD_SERVICE=bookmark-service \
  -e DD_ENV=dev \
  -e DD_VERSION=1.0 \
  -e DD_AGENT_HOST=host.docker.internal \
  -e DD_LOGS_INJECTION=true \
  -e JAVA_TOOL_OPTIONS="-javaagent:/dd-java-agent.jar" \
  -v $(pwd)/dd-java-agent.jar:/dd-java-agent.jar \
  bookmark-service-img
```

### 6. Verify

```bash
docker logs bookmark-service
```

App starts on **http://localhost:8081**

---

## API Endpoints

Base URL: `http://localhost:8081/api/bookmarks`

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/bookmarks` | Get all bookmarks |
| `GET` | `/api/bookmarks/{id}` | Get bookmark by ID |
| `POST` | `/api/bookmarks` | Create a bookmark |
| `PUT` | `/api/bookmarks/{id}` | Update a bookmark |
| `DELETE` | `/api/bookmarks/{id}` | Delete a bookmark |

### Create Bookmark — Request Body

```json
{
  "title": "Datadog Documentation",
  "description": "Official Datadog docs for APM and log management",
  "url": "https://docs.datadoghq.com"
}
```

A Postman collection is included: `bookmark-service.postman_collection.json`

### H2 Console

Available at `http://localhost:8081/h2-console`

- JDBC URL: `jdbc:h2:mem:bookmarksdb`
- Username: `sa`
- Password: *(empty)*

---

## Observability

### Structured JSON Logs (Log4j2)

Every log line is a JSON object:

```json
{
  "@timestamp": "2026-05-14T10:00:00.000Z",
  "level": "INFO",
  "logger": "c.l.d.p.controller.BookmarkController",
  "thread": "http-nio-8081-exec-1",
  "message": "Created bookmark id=1",
  "mdc": {
    "requestId": "b3a1c2d4-...",
    "dd.trace_id": "1234567890",
    "dd.span_id": "9876543210"
  },
  "app": "springboot-datadog-poc"
}
```

`dd.trace_id` and `dd.span_id` are injected automatically by the Datadog agent when `DD_LOGS_INJECTION=true`, enabling log-to-trace correlation in the Datadog UI.

### Request ID

Every HTTP request is assigned a `requestId` (UUID) via `RequestIdFilter`:
- Generated automatically if not present, or forwarded from the `X-Request-Id` request header
- Added to MDC → appears in every log line for that request
- Set as a tag on the root Datadog span (`request_id`)
- Echoed back in the `X-Request-Id` response header

### Datadog APM Spans

Custom tags set on the root span (`POST /api/bookmarks`, `GET /api/bookmarks/{id}`, etc.):

| Tag | Value | Set by |
|---|---|---|
| `request_id` | UUID per request | `RequestIdFilter` |
| `bookmark.id` | Bookmark entity ID | `BookmarkController` |

`bookmark.id` is set on the **root span** using `MutableSpan.getLocalRootSpan()` so it is visible on the top-level HTTP span in the Datadog trace viewer, not buried in a child span.
