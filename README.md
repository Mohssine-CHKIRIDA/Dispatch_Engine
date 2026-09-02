# Pulsaride

Pulsaride is a healthcare request dispatch pipeline built from Python AI services, Java services, Redis Streams, PostgreSQL with pgvector, and Ollama or hosted LLM providers.

## Prerequisites

Install the following tools:

- Docker Desktop with Docker Compose
- Python 3.10 or newer
- Java 21 and Maven 3.9 or newer for the Java services
- Git

On Windows, use `python` rather than `python3` when that is the interpreter where the dependencies are installed. Check with:

```powershell
python --version
java -version
mvn -version
docker compose version
```

## Configuration

The root `.env` file contains the default PostgreSQL settings used by Docker Compose. The defaults are:

```text
POSTGRES_USER=pulsaride
POSTGRES_PASSWORD=pulsaride
POSTGRES_DB=pulsaride
```

The Python services also read environment variables from a `.env` file in their working directory or a parent directory. To use a hosted LLM provider, add its key to the root `.env`, for example:

```text
GROQ_API_KEY=your-key
# or
NVIDIA_API_KEY=your-key
```

Without a hosted provider key, the LLM Gateway can use Ollama locally. Provider order and model settings can be configured with `PROVIDER_ORDER`, `GROQ_MODEL`, `NVIDIA_MODEL`, `OLLAMA_MODEL`, and the other variables documented in `services-python/llm-gateway/README.md`.

## Start Infrastructure

From the repository root, start Redis, PostgreSQL, Redis Commander, and Ollama:

```powershell
docker compose up -d
```

Check the containers and logs:

```powershell
docker compose ps
docker compose logs -f redis postgres ollama
```

Services use these host ports:

| Component       | URL or port              |
| --------------- | ------------------------ |
| Redis           | `localhost:6379`         |
| PostgreSQL      | `localhost:5433`         |
| Redis Commander | `http://localhost:8081`  |
| Ollama          | `http://localhost:11434` |

Pull the default local model before using Ollama:

```powershell
docker compose exec ollama ollama pull mistral
```

## Install Python Dependencies

Install each service's dependencies. Running these commands from the repository root is supported:

```powershell
python -m pip install -r services-python/llm-gateway/requirements.txt
python -m pip install -r services-python/embedding-service/requirements.txt
python -m pip install -r services-python/nlp-triage/requirements.txt
python -m pip install -r services-python/urgency-scoring/requirements.txt
python -m pip install -r services-python/scenario-simulator/requirements.txt
```

The embedding service downloads its Sentence Transformers model on first startup, so its first launch may take a while.

## Run the Python Pipeline on Windows

With Docker infrastructure running, launch the gateway, embedding service, NLP triage, and urgency scoring in separate console windows:

```powershell
python runAll.py
```

`runAll.py` does not start Docker containers. It also does not currently start the matching service or any Java service. Close each service console, or press `Ctrl+C` in it, to stop the Python processes.

Verify the HTTP services:

```powershell
Invoke-RestMethod http://localhost:8000/health
Invoke-RestMethod http://localhost:8090/health
```

The gateway's `providers_configured` response shows which LLM providers are available.

## Publish Test Requests

In a separate PowerShell terminal, publish the fixed scenario set to Redis and record its evaluation data:

```powershell
Set-Location services-python/scenario-simulator
python run.py
```

To continuously publish random scenarios instead:

```powershell
python runLoop.py
```

Press `Ctrl+C` to stop the simulator. The default database connection is the PostgreSQL container on port `5433`.

## Run Individual Python Services

The launcher is Windows-specific. To run a service manually, start it from that service's directory:

```powershell
Set-Location services-python/llm-gateway
python -m uvicorn app:app --reload --port 8000
```

```powershell
Set-Location services-python/embedding-service
python -m uvicorn main:app --reload --port 8090
```

```powershell
Set-Location services-python/nlp-triage
python main.py
```

```powershell
Set-Location services-python/urgency-scoring
python main.py
```

## Build and Run Java Services

The Java services are separate Maven projects rather than modules in one root Maven build. Generate or install the shared event models first when required, then build an individual service:

```powershell
python scripts/flatten-schemas-for-java.py
Set-Location services-java/shared-events
mvn clean install
```

For an individual Java service, run:

```powershell
Set-Location services-java/ingestion
mvn clean package
mvn spring-boot:run
```

The same pattern applies to `dispatch-core`, `notification-gateway`, and `pro-registry`. Their Redis and database connection settings are in each service's `src/main/resources/application.yml`.

## Regenerate Event Models

The JSON Schemas in `contracts/events/` are the source of truth. After changing one, regenerate the Python models and Java input schemas:

```powershell
python -m pip install datamodel-code-generator
python scripts/generate_python_models.py
python scripts/flatten-schemas-for-java.py
```

Then rebuild the Java shared-events project:

```powershell
Set-Location services-java/shared-events
mvn clean compile
```

Do not edit generated files under `services-python/shared/events/` or `contracts/events-flattened/` by hand. See `GENERATING_MODELS.md` for details.

## Tests and Checks

Run the LLM Gateway tests:

```powershell
Set-Location services-python/llm-gateway
python -m pytest
```

Compile a Java service without running it:

```powershell
Set-Location services-java/dispatch-core
mvn test
```

## Stop and Reset Infrastructure

Stop the containers while keeping their data volumes:

```powershell
docker compose down
```

To remove the stored Redis, PostgreSQL, and Ollama data as well:

```powershell
docker compose down -v
```

The second command is destructive: it removes the local database, Redis data, and downloaded Ollama models.
