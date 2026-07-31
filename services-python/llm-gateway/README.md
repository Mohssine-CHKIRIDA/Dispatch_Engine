# LLM Gateway

Single entry point for any Pulsaride service that needs an LLM completion.
Supports plain-text completions (e.g. IA2 urgency scoring) and forced
tool-calling (e.g. IA1 triage extraction), with Redis caching and
provider fallback in both modes.

## Setup

```
pip install -r requirements.txt
cp .env.example .env   # fill in NVIDIA_API_KEY at minimum
uvicorn app:app --reload --port 8000
```

## .env variables

```
GROQ_API_KEY=...
GROQ_BASE_URL=https://api.groq.ai/v1                  # default shown
GROQ_MODEL=groq-wave-1                                # default shown

NVIDIA_API_KEY=...
NVIDIA_BASE_URL=https://integrate.api.nvidia.com/v1   # default shown
NVIDIA_MODEL=moonshotai/kimi-k2.6                       # default shown

ANTHROPIC_API_KEY=                                      # optional, stub until set
ANTHROPIC_MODEL=claude-haiku-4-5-20251001

OLLAMA_BASE_URL=http://localhost:11434
OLLAMA_MODEL=mistral

PROVIDER_ORDER=nvidia,ollama                            # comma-separated, in priority order
REDIS_HOST=localhost
REDIS_PORT=6379
CACHE_TTL_SECONDS=3600
```

A provider missing its required config (e.g. no `NVIDIA_API_KEY`) is
silently skipped at startup rather than crashing the Gateway - check
`GET /health` to see what actually got wired up.

## API

### `GET /health`

```json
{
  "status": "ok",
  "providers_configured": ["nvidia", "ollama"],
  "provider_order": ["nvidia", "ollama"]
}
```

### `POST /v1/complete`

Plain text:

```json
{
  "system_prompt": "You are a helpful assistant.",
  "user_prompt": "Summarize this in one sentence: ...",
  "temperature": 0.5
}
```

→ `{"provider": "nvidia", "model": "...", "cached": false, "text": "...", "tool_call": null}`

Forced tool call (what IA1 needs):

```json
{
  "system_prompt": "...",
  "user_prompt": "...",
  "temperature": 0.6,
  "tools": [
    /* the EXTRACTION_FUNCTION schema */
  ],
  "tool_choice": {
    "type": "function",
    "function": { "name": "extract_patient_request" }
  }
}
```

→ `{"provider": "nvidia", "model": "moonshotai/kimi-k2.6", "cached": false, "text": null, "tool_call": {"name": "extract_patient_request", "arguments": {...}}}`

`tool_choice` without `tools` is a `422`. All providers failing is a `502`
with each provider's error message joined together.

## Migrating IA1's `ExtractionClient` to use the Gateway

Right now `nlp-triage/providers.py` calls NVIDIA directly with the `openai`
SDK. To route it through the Gateway instead (recommended - centralizes
caching/fallback), replace the body of `ExtractionClient.extract()` so it
POSTs to the Gateway rather than calling `OpenAI(...)` directly:

```python
import httpx

class ExtractionClient:
    def __init__(self, gateway_url: str):
        self._gateway_url = gateway_url.rstrip("/")

    def extract(self, raw_text: str, request_id: uuid.UUID) -> RequestExtracted:
        response = httpx.post(
            f"{self._gateway_url}/v1/complete",
            json={
                "system_prompt": SYSTEM_PROMPT,
                "user_prompt": f"Patient request:\n\n{raw_text}",
                "temperature": 0.6,
                "tools": [EXTRACTION_FUNCTION],
                "tool_choice": {
                    "type": "function",
                    "function": {"name": "extract_patient_request"},
                },
            },
            timeout=30.0,
        )
        response.raise_for_status()
        data = response.json()
        if not data.get("tool_call"):
            raise RuntimeError("Gateway did not return a tool call")

        extraction = Extraction.model_validate(data["tool_call"]["arguments"])
        return RequestExtracted.build(
            request_id=request_id,
            model=data["model"],
            extraction=extraction,
        )
```

IA1's own `NVIDIA_API_KEY`/`NVIDIA_BASE_URL`/`LLM_MODEL` env vars then move
out of `nlp-triage`'s `.env` and into the Gateway's - IA1 only needs a
`GATEWAY_URL` pointing at this service.

## Known caveat: Ollama fallback for tool-calling

Kimi K2.6 via NVIDIA reliably honors forced `tool_choice`. Local Mistral
7B via Ollama is much less consistent at strict function-calling. If
NVIDIA fails and the Gateway falls back to Ollama for a tool-calling
request, `OllamaProvider` will raise `ProviderError` (triggering a 502)
if Ollama doesn't return a well-formed tool call - it won't silently
hand back garbage, but it also won't reliably rescue a tool-calling
request the way it can for plain text. Worth re-testing this once IA1 is
actually wired up to hit the Gateway for real.
