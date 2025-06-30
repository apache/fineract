#!/bin/bash
# Usage: ./split-tests.sh <total-shards> <shard-index>
set -e

TOTAL_SHARDS=$1
SHARD_INDEX=$2

if [[ -z "$TOTAL_SHARDS" || -z "$SHARD_INDEX" ]]; then
  echo "ERROR: You must provide <total-shards> and <shard-index>."
  exit 1
fi

echo "🔍 Searching for eligible JUnit test classes..."

ALL_TESTS=$(find . -type f -path "*/src/test/java/*Test.java" \
  | while read filepath; do
      filename=$(basename "$filepath")

      # Skip abstract class or interface by name
      if [[ "$filename" =~ ^Abstract.*Test\.java$ || "$filename" =~ .*AbstractTest\.java$ ]]; then
        echo "Skipping abstract-named file: $filename" >&2
        continue
      fi

      # Skip if file doesn't contain valid JUnit annotations
      if ! grep -qE "@Test|@Nested|@ParameterizedTest" "$filepath"; then
        continue
      fi

      # Extract module directory path (everything before /src/test/java)
      module_path="${filepath%%/src/test/java/*}"
      # Convert from ./custom/acme/loan/job to :custom:acme:loan:job
      module_name=$(echo "$module_path" | sed 's|^\./||; s|/|:|g; s|^|:|')

      # Extract fully qualified test class name
      class_name=$(echo "$filepath" | sed 's|^.*src/test/java/||; s|/|.|g; s|.java$||')

      echo "$module_name,$class_name"
    done \
  | sort)

TOTAL_COUNT=$(echo "$ALL_TESTS" | wc -l)
echo "Found $TOTAL_COUNT eligible test classes."

SELECTED_CLASSES=$(echo "$ALL_TESTS" \
  | awk -v ts="$TOTAL_SHARDS" -v si="$SHARD_INDEX" 'NR % ts == (si - 1)')

OUTPUT_FILE="shard-tests_${SHARD_INDEX}.txt"
echo "$SELECTED_CLASSES" > "$OUTPUT_FILE"

echo "Selected $(wc -l < "$OUTPUT_FILE") classes for shard $SHARD_INDEX of $TOTAL_SHARDS:"
cat "$OUTPUT_FILE"
