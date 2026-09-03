# Pulsaride — Dispatch Console (frontend)

React + Vite ops console for interacting with the whole pipeline:
submit patient requests, manage doctors, watch live matching on a
triage-style board, and view Evaluation's KPIs. Talks directly to
your locally-running services via `fetch` — no separate BFF.

## Setup

```bash
npm install
npm run dev
```

Opens at `http://localhost:5173`.

## Before running

1. **CORS must be enabled** on Ingestion, Pro Registry, and Dispatch
   Core (Java/Spring Boot) — otherwise every request from this app
   will fail with a CORS error in the browser console, not a clean
   error message. Add to each service:

   ```java
   @Configuration
   public class CorsConfig {
       @Bean
       public WebMvcConfigurer corsConfigurer() {
           return new WebMvcConfigurer() {
               @Override
               public void addCorsMappings(CorsRegistry registry) {
                   registry.addMapping("/**")
                           .allowedOrigins("*")
                           .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE");
               }
           };
       }
   }
   ```

   And on Evaluation (FastAPI):
   ```python
   from fastapi.middleware.cors import CORSMiddleware
   app.add_middleware(CORSMiddleware, allow_origins=["*"], allow_methods=["*"], allow_headers=["*"])
   ```

2. **Evaluation needs a `GET /requests/recent` endpoint** (joins
   `request_lifecycle` with each request's latest proposal) for the
   Live board to work. See `kpi.py`'s `get_recent_requests()` from
   the chat this was built in if not already added.

3. **Pro Registry's doctor list gracefully degrades**: the Doctors
   panel tries `GET /pros`; if that endpoint doesn't exist yet, it
   falls back to tracking doctors created in the current browser
   session only. Add a real list endpoint whenever convenient — the
   frontend picks it up automatically, no changes needed here.

## Config

The four service base URLs are editable directly in the top config
bar and persisted to `localStorage`, so you only need to set them
once per browser. Defaults assume:

- Ingestion: `http://localhost:8080` (never explicitly confirmed —
  adjust if yours differs)
- Pro Registry: `http://localhost:9091`
- Dispatch Core: `http://localhost:9092`
- Evaluation: `http://localhost:8091`

## Structure

```
src/
  api.js               — fetch wrappers for all four services
  App.jsx              — tab state + config state
  components/
    Nav.jsx             — sidebar nav with logo
    ConfigBar.jsx       — editable service URLs
    SubmitPanel.jsx     — free-text request submission
    DoctorsPanel.jsx    — create/list/toggle doctors
    BoardPanel.jsx      — live request board, urgency color-coded,
                          manual accept/refuse/close (stands in for
                          a real WebSocket doctor client)
    KpisPanel.jsx       — stat cards + Recharts outcome pie chart
```
