#!/bin/bash

# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements. See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership. The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License. You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied. See the License for the
# specific language governing permissions and limitations
# under the License.

# Usage: ./split-tests.sh <total-shards> <shard-index>
set -e

TOTAL_SHARDS=$1
SHARD_INDEX=$2

if [[ -z "$TOTAL_SHARDS" || -z "$SHARD_INDEX" ]]; then
  echo "ERROR: You must provide <total-shards> and <shard-index>."
  exit 1
fi

if ! [[ "$TOTAL_SHARDS" =~ ^[1-9][0-9]*$ ]]; then
  echo "ERROR: <total-shards> must be a positive integer."
  exit 1
fi

if ! [[ "$SHARD_INDEX" =~ ^[1-9][0-9]*$ ]]; then
  echo "ERROR: <shard-index> must be a positive integer."
  exit 1
fi

if [[ "$SHARD_INDEX" -gt "$TOTAL_SHARDS" ]]; then
  echo "ERROR: <shard-index> ($SHARD_INDEX) must be between 1 and <total-shards> ($TOTAL_SHARDS)."
  exit 1
fi

echo "🔍 Searching for eligible JUnit test classes..."

ALL_TESTS=$(find . -type f -path "*/src/test/java/*.java" \
  | while read filepath; do
      filename=$(basename "$filepath")

      # Skip abstract class or interface by name
      if [[ "$filename" =~ ^Abstract.*Test\.java$ || "$filename" =~ .*AbstractTest\.java$ ]]; then
        echo "Skipping abstract-named file: $filename" >&2
        continue
      fi

      # Check for valid JUnit test annotations (exact word match)
      if ! grep -q -w "@Test\|@Nested\|@ParameterizedTest" "$filepath"; then
        continue
      fi

      # Extract module directory path (everything before /src/test/java)
      module_path="${filepath%%/src/test/java/*}"
      # Convert from ./custom/acme/loan/job to :custom:acme:loan:job
      module_name=$(echo "$module_path" | sed 's|^\./||; s|/|:|g; s|^|:|')

      # Extract fully qualified test class name
      class_name=$(echo "$filepath" | sed 's|^.*src/test/java/||; s|/|.|g; s|.java$||')

      # Read a class-level @Order annotation, if present. Stop at the first
      # class declaration so method-level @Order annotations are ignored.
      order=$(awk '
        {
          if (match($0, /@Order[[:space:]]*\([[:space:]]*-?[0-9]+[[:space:]]*\)/)) {
            annotation = substr($0, RSTART, RLENGTH)
            gsub(/[^0-9-]/, "", annotation)
            class_order = annotation
          }

          if ($0 ~ /(^|[[:space:]])(class|interface|enum)[[:space:]]+[A-Za-z_][A-Za-z0-9_]*/) {
            print class_order
            exit
          }
        }
      ' "$filepath")

      if [[ -n "$order" ]]; then
        # The first field keeps ordered tests ahead of unordered tests.
        echo "0,$order,$module_name,$class_name"
      else
        # The second field is a placeholder because unordered tests are last.
        echo "1,0,$module_name,$class_name"
      fi
    done \
  | sort -t, -k1,1n -k2,2n -k3,3 -k4,4)

TOTAL_COUNT=$(echo "$ALL_TESTS" | wc -l)
echo "Found $TOTAL_COUNT eligible test classes."

SELECTED_CLASSES=$(echo "$ALL_TESTS" \
  | awk -v ts="$TOTAL_SHARDS" -v si="$SHARD_INDEX" 'NR % ts == (si - 1)')

OUTPUT_FILE="shard-tests_${SHARD_INDEX}.txt"
echo "$SELECTED_CLASSES" | cut -d, -f3- > "$OUTPUT_FILE"

echo "Selected $(wc -l < "$OUTPUT_FILE") classes for shard $SHARD_INDEX of $TOTAL_SHARDS:"
cat "$OUTPUT_FILE"
