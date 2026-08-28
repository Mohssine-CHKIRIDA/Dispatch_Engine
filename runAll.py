"""
Launches the whole Pulsaride AI pipeline - LLM Gateway, IA1 (nlp-triage),
IA2 (urgency-scoring), IA3 (matching) - each in its own console window,
so you can still watch each service's logs live exactly like running
them manually, but from one command.

WINDOWS ONLY (uses subprocess.CREATE_NEW_CONSOLE, which has no
equivalent on macOS/Linux - see the note at the bottom if you need a
cross-platform version).

Usage:
    Place this file at the repo root (next to docker-compose.yml,
    alongside services-python/), then:

        python run_all.py

Does NOT start Redis/Postgres/Ollama (those are Docker containers per
your docker-compose.yml) - it only checks they're already up and warns
you if they aren't, since every Python service here depends on them.
"""

from __future__ import annotations

import subprocess
import sys
import time
from pathlib import Path

# ---------------------------------------------------------------------
# EDIT THIS if your folder names differ from what's listed here.
# (name, path relative to this script, command to run)
# ---------------------------------------------------------------------
SERVICES = [
    ("llm-gateway", "services-python/llm-gateway", [sys.executable, "-m", "uvicorn", "app:app", "--port", "8000"]),
    ("embedding-service (IA3)", "services-python/embedding-service", [sys.executable, "-m", "uvicorn", "main:app", "--port", "8090"]),
    ("nlp-triage (IA1)", "services-python/nlp-triage", [sys.executable, "main.py"]),
    ("urgency-scoring (IA2)", "services-python/urgency-scoring", [sys.executable, "main.py"]),
    #("matching (IA3)", "services-python/matching", [sys.executable, "main.py"]),
]

# Seconds to wait after starting each service before starting the next.
# Gateway needs a moment to be ready before IA1/IA2 start hitting it;
# small stagger just keeps early startup logs cleaner, not strictly
# required (each service already tolerates transient connection errors
# on a single message without crashing).
STAGGER_SECONDS = 3

REQUIRED_CONTAINERS = ["redis", "postgres", "ollama"]


def check_docker_containers() -> None:
    try:
        result = subprocess.run(
            ["docker", "ps", "--format", "{{.Names}}"],
            capture_output=True, text=True, timeout=10,
        )
    except FileNotFoundError:
        print("[WARN] 'docker' command not found - skipping container check.")
        return
    except subprocess.TimeoutExpired:
        print("[WARN] 'docker ps' timed out - skipping container check.")
        return

    running = result.stdout.lower()
    missing = [name for name in REQUIRED_CONTAINERS if name not in running]
    if missing:
        print(f"[WARN] These containers don't appear to be running: {missing}")
        print("       (matched by substring against container names - check `docker ps` yourself if unsure)")
        print("       Services that depend on them will fail or hang until they're up.\n")
    else:
        print("[OK] Redis/Postgres/Ollama all appear to be running.\n")


def main() -> None:
    root = Path(__file__).resolve().parent

    print("Checking Docker containers...")
    check_docker_containers()

    for name, rel_path, command in SERVICES:
        service_dir = root / rel_path
        if not service_dir.is_dir():
            print(f"[SKIP] {name}: directory not found at {service_dir}")
            print(f"       Edit the SERVICES list at the top of this script if your folder layout differs.")
            continue

        print(f"[START] {name}  (cwd={service_dir})")
        subprocess.Popen(
            command,
            cwd=str(service_dir),
            creationflags=subprocess.CREATE_NEW_CONSOLE,
        )
        time.sleep(STAGGER_SECONDS)

    print("\nAll services launched in separate windows.")
    print("Close this window any time - it doesn't keep the others alive or tied to it.")
    print("To stop everything, close each service's console window individually (Ctrl+C or the X button).")


if __name__ == "__main__":
    main()