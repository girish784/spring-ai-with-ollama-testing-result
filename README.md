# Spring AI Customer Support Agent

This is a Spring Boot implementation of the notebook application in the parent folder.

It provides:

- V1 routing agent: customer message -> support department.
- V2 planning agent: routing + SOP retrieval + action plan generation.
- Local BM25-style retrieval over the original SOP text files.
- Spring AI `ChatClient` integration with Ollama for local LLM routing and planning.

## Requirements

- Java 21
- Maven
- Docker Desktop, for the Docker setup
- Java 21 and Maven, only if running without Docker

Spring AI 1.1.6 supports Spring Boot 3.4.x and 3.5.x, so this project uses Spring Boot 3.5.0.

## Run

```bash
set OLLAMA_BASE_URL=http://localhost:11434
set OLLAMA_MODEL=llama3.2:3b
mvn spring-boot:run
```

PowerShell:

```powershell
$env:OLLAMA_BASE_URL="http://localhost:11434"
$env:OLLAMA_MODEL="llama3.2:3b"
mvn spring-boot:run
```

## Docker

Start Ollama and the Spring Boot app together:

```bash
docker compose up --build
```

The compose file starts:

- `ollama`: local Ollama server on port `11434`
- `ollama-pull`: pulls the configured model
- `customer-support-ai`: Spring Boot API on port `8081`

Optional model override:

```bash
set OLLAMA_MODEL=llama3.2:3b
docker compose up --build
```

Swagger UI:

```text
http://localhost:8081/swagger-ui.html
```

OpenAPI JSON:

```text
http://localhost:8081/v3/api-docs
```

Health check:

```bash
curl http://localhost:8081/api/health
```

## Logs

Follow all Docker logs:

```bash
docker compose logs -f
```

Follow only the API logs:

```bash
docker compose logs -f customer-support-ai
```

Follow only Ollama logs:

```bash
docker compose logs -f ollama
```

The Spring Boot app also writes a file log to:

```text
logs/customer-support-ai.log
```

If `curl` returns `(52) Empty reply from server`, first check:

```bash
docker compose ps
docker compose logs --tail=200 customer-support-ai
docker compose logs --tail=200 ollama
```

Common causes are: the API container restarted, Ollama is still pulling the model, the configured model name is not available, or the request is being sent to the wrong port.

## API

Route a message:

```bash
curl -X POST http://localhost:8081/api/v1/route \
  -H "Content-Type: application/json" \
  -d "{\"message\":\"I was charged twice for my order\"}"
```

Create a plan:

```bash
curl -X POST http://localhost:8081/api/v2/plan \
  -H "Content-Type: application/json" \
  -d "{\"message\":\"I bought a jacket last month, but it is too big. Can I return it?\"}"
```

List SOPs:

```bash
curl http://localhost:8081/api/sops
```
