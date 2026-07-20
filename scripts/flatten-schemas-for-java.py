#!/usr/bin/env python3
"""
Flattens contracts/events/*.schema.json (which use allOf + $ref to
common/envelope.schema.json) into single flat object schemas, written to
contracts/events-flattened/.

WHY THIS EXISTS:
jsonschema2pojo 1.2.1 silently fails to generate classes for schemas using
root-level `allOf` combined with a `$ref` to an external file — it reads
the file, does not error, but never emits a class either (confirmed via
`mvn generate-sources -X`: "Reading schema" logged for all files, but
"Adding com.pulsaride.events.X" only logged for the one schema that has
no allOf). datamodel-code-generator (Python side) has no such problem.

Rather than rewrite the source-of-truth contracts (losing the clean
envelope/payload separation, and forcing 10x duplication that could drift),
this script generates a build artifact used ONLY as jsonschema2pojo's
input. The files in contracts/events/ remain the real source of truth.

Output schemas are semantically identical to the originals: same required
fields, same property constraints, same descriptions — just with the
envelope's properties merged directly into each event's top-level
properties instead of composed via allOf.

Usage:
    python3 scripts/flatten-schemas-for-java.py
"""

import json
import glob
import os
import sys

REPO_ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
SOURCE_DIR = os.path.join(REPO_ROOT, "contracts", "events")
COMMON_DIR = os.path.join(SOURCE_DIR, "common")
OUTPUT_DIR = os.path.join(REPO_ROOT, "contracts", "events-flattened")


def load(path):
    with open(path) as f:
        return json.load(f)


def flatten_event_schema(schema, envelope):
    """
    Merges envelope properties/required directly into the schema's
    top-level properties/required, and merges in the payload-branch
    properties/required too. Produces a single flat `type: object` schema
    equivalent to what the allOf would have validated.
    """
    if "allOf" not in schema:
        # Not a composed schema (e.g. envelope itself) — nothing to flatten.
        return schema

    merged_properties = {}
    merged_required = []
    title = schema.get("title")
    description = schema.get("description")

    for branch in schema["allOf"]:
        if "$ref" in branch:
            # Resolve the $ref — in our contracts this is always the envelope.
            ref_target = envelope
            merged_properties.update(ref_target.get("properties", {}))
            merged_required.extend(ref_target.get("required", []))
        else:
            merged_properties.update(branch.get("properties", {}))
            merged_required.extend(branch.get("required", []))

    # Dedup required while preserving order
    seen = set()
    deduped_required = []
    for r in merged_required:
        if r not in seen:
            seen.add(r)
            deduped_required.append(r)

    flat = {
        "$schema": schema.get("$schema", "https://json-schema.org/draft/2020-12/schema"),
        "$id": schema.get("$id"),
        "title": title,
        "description": description,
        "type": "object",
        "properties": merged_properties,
        "required": deduped_required,
        "additionalProperties": False,
    }
    return flat


def main():
    envelope_path = os.path.join(COMMON_DIR, "envelope.schema.json")
    if not os.path.exists(envelope_path):
        print(f"ERROR: envelope schema not found at {envelope_path}", file=sys.stderr)
        sys.exit(1)
    envelope = load(envelope_path)

    os.makedirs(OUTPUT_DIR, exist_ok=True)

    event_files = sorted(glob.glob(os.path.join(SOURCE_DIR, "*.schema.json")))
    if not event_files:
        print(f"ERROR: no event schemas found in {SOURCE_DIR}", file=sys.stderr)
        sys.exit(1)

    count = 0
    for path in event_files:
        schema = load(path)
        flat = flatten_event_schema(schema, envelope)
        out_path = os.path.join(OUTPUT_DIR, os.path.basename(path))
        with open(out_path, "w") as f:
            json.dump(flat, f, indent=2, ensure_ascii=False)
            f.write("\n")
        count += 1
        print(f"flattened: {os.path.basename(path)}")

    print(f"\n{count} schema(s) flattened into {OUTPUT_DIR}")
    print("Point jsonschema2pojo's sourceDirectory at this folder, not contracts/events.")


if __name__ == "__main__":
    main()
