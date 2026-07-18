#!/usr/bin/env bash
set -euo pipefail

# Regenerates Python (Pydantic) models from contracts/events/*.schema.json.
# Run this from the repo root whenever a schema changes.
#
# Requires: pip install datamodel-code-generator

cd "$(dirname "$0")/.."

echo "Generating Python models..."
# IMPORTANT: datamodel-code-generator ALWAYS overwrites __init__.py with an
# empty stub on every run, regardless of what's there — confirmed by testing,
# not assumed. Our __init__.py has hand-written re-exports (see below), so we
# back it up, let the generator do its thing, then restore it. (Not using
# rsync here — kept this portable across Git Bash / WSL / plain bash.)
INIT_FILE="services-python/shared/events/__init__.py"
INIT_BACKUP=$(mktemp)
cp "$INIT_FILE" "$INIT_BACKUP"

datamodel-codegen \
  --input contracts/events \
  --input-file-type jsonschema \
  --output services-python/shared/events \
  --output-model-type pydantic_v2.BaseModel \
  --field-constraints \
  --use-title-as-name \
  --use-schema-description \
  --formatters black isort

cp "$INIT_BACKUP" "$INIT_FILE"
rm -f "$INIT_BACKUP"

echo "Done. __init__.py was preserved (hand-written re-exports untouched)."
echo "If you added a NEW event type, you still need to add its import to"
echo "services-python/shared/events/__init__.py manually — this script won't do it."

# --- Java generation ---
# jsonschema2pojo 1.2.1 silently fails on schemas using root-level allOf +
# a $ref to an external file (confirmed via -X debug log). Workaround:
# flatten the schemas into single-file equivalents first.
echo "Flattening schemas for Java (working around jsonschema2pojo allOf+\$ref limitation)..."
python3 scripts/flatten-schema.py

echo ""
echo "Now run the Java generation yourself (needs Maven + JDK 17, not available in this environment):"
echo "  cd services-java/shared-events"
echo "  mvn clean generate-sources"
echo "  mvn clean compile   # to verify it actually builds"