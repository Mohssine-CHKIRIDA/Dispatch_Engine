#!/usr/bin/env python3
"""
Regenerates services-python/shared/events/*.py from contracts/events/*.schema.json.

Pure Python (no bash/Git Bash/WSL required) so it runs identically on
Windows, macOS, and Linux.

IMPORTANT: datamodel-code-generator ALWAYS overwrites
services-python/shared/events/__init__.py with an empty stub on every run,
regardless of what's already there — confirmed by testing, not assumed.
Our __init__.py has hand-written re-exports (see its docstring), so this
script backs it up before generating and restores it after.

Usage:
    python scripts/generate_python_models.py

Requires: pip install datamodel-code-generator
"""

import shutil
import subprocess
import sys
from pathlib import Path

REPO_ROOT = Path(__file__).resolve().parent.parent
CONTRACTS_DIR = REPO_ROOT / "contracts" / "events"
OUTPUT_DIR = REPO_ROOT / "services-python" / "shared" / "events"
INIT_FILE = OUTPUT_DIR / "__init__.py"
INIT_BACKUP = REPO_ROOT / "_init_backup_tmp.py"


def main():
    if not CONTRACTS_DIR.exists():
        print(f"ERROR: {CONTRACTS_DIR} not found — run this from the repo root.", file=sys.stderr)
        sys.exit(1)

    if not INIT_FILE.exists():
        print(f"WARNING: {INIT_FILE} doesn't exist yet — nothing to back up. "
              "If this is the very first run, you'll need to hand-write it "
              "afterward (see GENERATING_MODELS.md).")
    else:
        shutil.copy(INIT_FILE, INIT_BACKUP)
        print(f"Backed up {INIT_FILE.name}")

    print("Generating Python models...")
    result = subprocess.run(
        [
            "datamodel-codegen",
            "--input", str(CONTRACTS_DIR),
            "--input-file-type", "jsonschema",
            "--output", str(OUTPUT_DIR),
            "--output-model-type", "pydantic_v2.BaseModel",
            "--field-constraints",
            "--use-title-as-name",
            "--use-schema-description",
            "--formatters", "black", "isort",
        ],
    )

    if result.returncode != 0:
        print("ERROR: datamodel-codegen failed — see output above.", file=sys.stderr)
        # Restore the backup even on failure, so a bad run doesn't leave
        # __init__.py wiped out.
        if INIT_BACKUP.exists():
            shutil.copy(INIT_BACKUP, INIT_FILE)
            INIT_BACKUP.unlink()
        sys.exit(1)

    if INIT_BACKUP.exists():
        shutil.copy(INIT_BACKUP, INIT_FILE)
        INIT_BACKUP.unlink()
        print(f"Restored {INIT_FILE.name} (hand-written re-exports preserved)")

    print("\nDone. If you added a NEW event type, remember to add its import")
    print(f"to {INIT_FILE.relative_to(REPO_ROOT)} by hand — this script won't do it.")


if __name__ == "__main__":
    main()
