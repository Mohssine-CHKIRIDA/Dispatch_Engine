# Shared event models — how to (re)generate

Both `services-python/shared/events/` and `services-java/shared-events/` are
**generated code**. Never hand-edit files inside them — edit the source
schemas in `contracts/events/*.schema.json` instead, then regenerate.

## Prerequisites (one-time setup)

- **Python 3.10+** with `pip install datamodel-code-generator`
- **Java 17+** and **Maven 3.9+** on your PATH (`java -version` / `mvn -version` to check)
- Docker Compose stack running (`docker compose up -d` from repo root) if you
  want to actually test the models against real Redis/Postgres afterward

## Regenerating everything

### Python

From the repo root:

```bash
python scripts/generate_python_models.py
```

(Windows PowerShell: same command, `python` not `python3` — see note below.)

This regenerates `services-python/shared/events/*.py` from
`contracts/events/*.schema.json`. **`__init__.py` is preserved automatically**
— the script backs it up before generating and restores it after, because
`datamodel-code-generator` always overwrites it with an empty stub otherwise
(confirmed behavior, not a guess).

If you add a **new** event type (a new `.schema.json` file), you must add its
import to `services-python/shared/events/__init__.py` by hand — the script
won't do this for you.

### Java

Two steps: flatten the schemas, then run Maven.

```bash
python scripts/flatten-schemas-for-java.py
cd services-java/shared-events
mvn clean generate-sources
mvn clean compile
```

Or on Windows PowerShell, same commands work as-is (just make sure it's
`python`, not `python3` — see note below).

**Why the flatten step exists**: our schemas use `allOf` + `$ref` to share
common fields (`event_id`, `request_id`, etc.) across all 10 event types via
`contracts/events/common/envelope.schema.json`. This works perfectly for the
Python generator. It does **not** work for `jsonschema2pojo` (the Java
generator) — confirmed via `mvn generate-sources -X`: the plugin reads every
schema file but silently produces zero output for any schema using
`allOf` + a `$ref` to an external file, with no error or warning, while
still reporting `BUILD SUCCESS`. `scripts/flatten-schemas-for-java.py` works
around this by merging the envelope fields directly into each event schema
before handing it to `jsonschema2pojo`, producing a disposable, gitignored
copy in `contracts/events-flattened/`. **`contracts/events/` remains the
real source of truth** — always edit schemas there, never in
`events-flattened/`.

One consequence of this workaround: unlike the Python models (which use real
inheritance — `RequestSubmitted extends EventEnvelope` isn't literal Java,
but you get the idea via Pydantic's `EventEnvelope` base class), the Java
classes are flat — each one duplicates the envelope fields directly rather
than inheriting them from a shared base class. Functionally identical,
just not DRY. If a future `jsonschema2pojo` version fixes this, the flatten
step can be dropped and `sourceDirectory` in `services-java/shared-events/pom.xml`
pointed back at `contracts/events` directly.

## Verifying it actually worked

Generation succeeding is not the same as generation being *correct* — we hit
a silent-failure bug precisely because `BUILD SUCCESS` doesn't mean "all
files were generated." Always check counts:

```bash
# Python: should list ~20 files (10 events + payloads + envelope)
ls services-python/shared/events/*.py services-python/shared/events/common/*.py

# Java: should list ~20 .java files (10 event classes + 10 payload classes)
find services-java/shared-events/target/generated-sources -name "*.java"
```

If either count looks suspiciously low (e.g. only 1-2 files), something
silently failed — don't assume it's fine just because the command exited
with success.

Quick Python smoke test:
```bash
cd services-python
python -c "from shared.events import RequestSubmitted, SubmittedPayload, EventEnvelope; print('OK')"
```

## `python` vs `python3` (Windows note)

On Windows, `python` and `python3` can point to **completely different
installations** — e.g. `python` might be your real install with all
packages, while `python3` is an empty Microsoft Store stub. If you get
`ModuleNotFoundError` for a package you know you installed, try the other
command. Always check with:
```powershell
where python
where python3
```
and use whichever one actually has your `pip install`s.

## When you add or change a schema

1. Edit `contracts/events/*.schema.json` (or add a new file there)
2. Regenerate Python: `python scripts/generate_python_models.py`
3. If new event type: add its import to `services-python/shared/events/__init__.py`
4. Regenerate Java: `python scripts/flatten-schemas-for-java.py` then `mvn clean generate-sources` in `services-java/shared-events/`
5. Run `mvn clean compile` to confirm Java still builds
6. Run the Python smoke test above
7. Commit the changed schema + regenerated files together (generated files
   ARE committed to the repo for Python/Java source, unlike
   `contracts/events-flattened/` which is gitignored as a pure build artifact)
