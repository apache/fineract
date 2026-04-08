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

SCAN_DIR="${1:-$(dirname "$0")/../}"

echo "Scanning for JPA annotations (@Entity, @MappedSuperclass, @Converter) in: $SCAN_DIR"
echo "================================================================"

TEMP_FILE="/tmp/jpa_classes_grouped.txt"
> "$TEMP_FILE"

find "$SCAN_DIR" -type f -name "*.java" -exec grep -lE '@Entity|@MappedSuperclass|@Converter' {} + | while IFS= read -r file; do
  # Extract package safely
  package=$(awk '
    /^[[:space:]]*package[[:space:]]+/ {
      gsub(/^[[:space:]]*package[[:space:]]+/, "", $0)
      gsub(/;.*/, "", $0)
      print
      exit
    }
  ' "$file")

  class_name="${file##*/}"
  class_name="${class_name%.java}"

  # Derive module from file path, not from package
  module="unknown"
  case "$file" in
    *"/fineract-"*"/src/main/java/"*)
      module_part="${file%%/src/main/java/*}"
      module="${module_part##*/}"
      ;;
  esac

  if [[ -n "$package" ]]; then
    fqcn="${package}.${class_name}"
    printf '%s\t%s\n' "$module" "$fqcn" >> "$TEMP_FILE"
  fi
done

echo ""
echo "========================================================================================"
echo "JPA CLASSES GROUPED BY MODULE"
echo "========================================================================================"
echo ""

current_module=""

sort -t $'\t' -k1,1 -k2,2 "$TEMP_FILE" | while IFS=$'\t' read -r module fqcn; do
  if [[ "$module" != "$current_module" ]]; then
    [[ -n "$current_module" ]] && echo ""
    echo "<!--  $module module: -->"
    current_module="$module"
  fi

  echo "<class>${fqcn}</class>"
done

echo ""
echo "========================================================================================"
echo "SUMMARY BY MODULE TYPE"
echo "========================================================================================"

echo ""
echo "📊 Module Statistics:"
cut -f1 "$TEMP_FILE" | sort | uniq -c | while read -r count module; do
  echo "  $module: $count classes"
done

echo ""
echo "📊 Annotation Types:"
entity_count=$(find "$SCAN_DIR" -type f -name "*.java" -exec grep -l '@Entity' {} + | wc -l | awk '{print $1}')
converter_count=$(find "$SCAN_DIR" -type f -name "*.java" -exec grep -l '@Converter' {} + | wc -l | awk '{print $1}')
mappedsuperclass_count=$(find "$SCAN_DIR" -type f -name "*.java" -exec grep -l '@MappedSuperclass' {} + | wc -l | awk '{print $1}')

echo "  @Entity: $entity_count classes"
echo "  @Converter: $converter_count classes"
echo "  @MappedSuperclass: $mappedsuperclass_count classes"

echo ""
echo "========================================================================================"
echo "TOTAL JPA-ANNOTATED CLASSES: $(wc -l < "$TEMP_FILE" | awk '{print $1}')"
echo "========================================================================================"

rm -f "$TEMP_FILE"
