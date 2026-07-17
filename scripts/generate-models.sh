#!/usr/bin/env bash
set -euo pipefail

# Regenerates Python (Pydantic) models from contracts/events/*.schema.json.
# Run this from the repo root whenever a schema changes.
#
# Requires: pip install datamodel-code-generator

cd "$(dirname "$0")/.."

echo "Generating Python models..."
datamodel-codegen \
  --input contracts/events \
  --input-file-type jsonschema \
  --output services-python/shared/events \
  --output-model-type pydantic_v2.BaseModel \
  --field-constraints \
  --use-title-as-name \
  --use-schema-description \
  --formatters black isort

echo "NOTE: this overwrites services-python/shared/events/*.py and common/*.py,"
echo "but does NOT touch __init__.py (hand-written re-exports) unless you delete it first."
echo "If you add a new event type, remember to add its import to __init__.py manually."

echo "Done. Run 'python3 -m pytest services-python/shared/events/' (once tests exist) to verify."

# --- Java generation (manual, not automatable in this sandbox) ---
# jsonschema2pojo requires Maven Central access. Run locally with:
#
#   mvn io.github.joelittlejohn.jsonschema2pojo:jsonschema2pojo-maven-plugin:generate \
#     -Dsource=contracts/events -Dtargetpackage=com.pulsaride.events \
#     -DoutputDirectory=services-java/shared-events/src/main/java
#
# or configure the plugin properly in a shared-events pom.xml (see Step 4 notes).
