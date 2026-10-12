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

# ---------------------------------------------------------------------------
# check-liquibase-ddl-safety.sh
#
# Scans Liquibase XML changesets introduced in a PR for dangerous DDL
# operations on critical tables and invalid DATETIME precision. Designed for
# CI but also runs locally.
#
# Uses only bash builtins + grep/sed (no xmllint dependency).
#
# Usage:
#   ./scripts/check-liquibase-ddl-safety.sh \
#       --base-ref origin/develop \
#       --head-ref HEAD \
#       --config config/liquibase \
#       --output-dir /tmp/ddl-safety-report
#
# Exit codes:
#   0 — No blocking violations found (may have warnings)
#   1 — Blocking violations found on critical tables
#   2 — Script error / bad arguments
# ---------------------------------------------------------------------------
set -euo pipefail

# ---- Defaults ----
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
CONFIG_DIR="$REPO_ROOT/config/liquibase"
OUTPUT_DIR=""
BASE_REF=""
HEAD_REF=""
FILES_OVERRIDE=""  # For local testing: pass specific files instead of using git diff

# ---- Argument parsing ----
while [[ $# -gt 0 ]]; do
    case "$1" in
        --base-ref)   BASE_REF="$2"; shift 2 ;;
        --head-ref)   HEAD_REF="$2"; shift 2 ;;
        --config)     CONFIG_DIR="$2"; shift 2 ;;
        --output-dir) OUTPUT_DIR="$2"; shift 2 ;;
        --files)      FILES_OVERRIDE="$2"; shift 2 ;;
        -h|--help)
            echo "Usage: $0 --base-ref <ref> --head-ref <ref> [--config <dir>] [--output-dir <dir>] [--files <file1,file2,...>]"
            exit 0
            ;;
        *) echo "Unknown argument: $1"; exit 2 ;;
    esac
done

# ---- Validate inputs ----
if [[ -z "$FILES_OVERRIDE" ]] && { [[ -z "$BASE_REF" ]] || [[ -z "$HEAD_REF" ]]; }; then
    echo "Error: Either --base-ref + --head-ref or --files is required."
    exit 2
fi

if [[ -n "$OUTPUT_DIR" ]]; then
    mkdir -p "$OUTPUT_DIR"
fi

# ---- Load configuration ----
CRITICAL_TABLES_FILE="$CONFIG_DIR/critical-tables.txt"
DANGEROUS_OPS_FILE="$CONFIG_DIR/dangerous-operations.txt"

if [[ ! -f "$CRITICAL_TABLES_FILE" ]]; then
    echo "Error: Critical tables config not found: $CRITICAL_TABLES_FILE"
    exit 2
fi
if [[ ! -f "$DANGEROUS_OPS_FILE" ]]; then
    echo "Error: Dangerous operations config not found: $DANGEROUS_OPS_FILE"
    exit 2
fi

# Load critical tables into array (skip comments and blank lines)
CRITICAL_TABLES=()
while IFS= read -r line; do
    line="${line%%#*}"    # strip inline comments
    line="$(echo "$line" | tr -d '[:space:]')"
    [[ -z "$line" ]] && continue
    CRITICAL_TABLES+=("$line")
done < "$CRITICAL_TABLES_FILE"

# Load dangerous operations into indexed arrays. Avoid associative arrays so
# this script can also start on macOS' default Bash 3.
OP_ELEMENTS=()
OP_SEVERITIES=()
OP_RISKS=()
while IFS='|' read -r element severity risk; do
    element="${element%%#*}"
    element="$(echo "$element" | tr -d '[:space:]')"
    [[ -z "$element" ]] && continue
    severity="$(echo "$severity" | tr -d '[:space:]')"
    risk="$(echo "$risk" | sed 's/^[[:space:]]*//;s/[[:space:]]*$//')"
    OP_ELEMENTS+=("$element")
    OP_SEVERITIES+=("$severity")
    OP_RISKS+=("$risk")
done < "$DANGEROUS_OPS_FILE"

# ---- Find changed XML files ----
CHANGED_FILES=()
if [[ -n "$FILES_OVERRIDE" ]]; then
    IFS=',' read -ra CHANGED_FILES <<< "$FILES_OVERRIDE"
else
    MERGE_BASE=$(git -C "$REPO_ROOT" merge-base "$BASE_REF" "$HEAD_REF" 2>/dev/null || echo "$BASE_REF")
    while IFS= read -r f; do
        [[ -n "$f" ]] && CHANGED_FILES+=("$f")
    done < <(git -C "$REPO_ROOT" diff --name-only --diff-filter=AM "$MERGE_BASE".."$HEAD_REF" -- '*.xml' | grep -E '/db/(changelog|custom-changelog)/' || true)
fi

if [[ ${#CHANGED_FILES[@]} -eq 0 ]]; then
    echo "No Liquibase changelog XML files changed."
    if [[ -n "$OUTPUT_DIR" ]]; then
        echo "No Liquibase changelog XML files changed in this PR." > "$OUTPUT_DIR/report.md"
        echo '{"violations":[],"summary":{"total":0,"critical":0,"warnings":0}}' > "$OUTPUT_DIR/report.json"
    fi
    exit 0
fi

echo "Found ${#CHANGED_FILES[@]} changed Liquibase XML file(s):"
printf "  %s\n" "${CHANGED_FILES[@]}"
echo ""

# ---- Helper functions ----
is_critical_table() {
    local table="$1"
    for ct in "${CRITICAL_TABLES[@]}"; do
        if [[ "$table" == "$ct" ]]; then
            return 0
        fi
    done
    return 1
}

# ---- Helper: flatten multiline XML tags into single lines ----
# Liquibase tags like <renameColumn\n  tableName="X"\n  oldColumnName="Y"/> span
# multiple lines. This function joins lines between '<tagName' and the closing
# '>' or '/>' so that grep can match attributes regardless of line breaks.
flatten_xml_tags() {
    local content="$1"
    echo "$content" | awk '
    BEGIN { buf="" }
    {
        if (buf != "") {
            buf = buf " " $0
            if (match(buf, />/)) {
                print buf
                buf = ""
            }
        } else if (match($0, /<[a-zA-Z]/) && !match($0, />/)) {
            buf = $0
        } else {
            print $0
        }
    }
    END { if (buf != "") print buf }
    '
}

# Track tables created in the same PR (for false-positive mitigation)
CREATED_TABLES=()
for file in "${CHANGED_FILES[@]}"; do
    local_file="$REPO_ROOT/$file"
    [[ -f "$local_file" ]] || continue
    flat=$(flatten_xml_tags "$(cat "$local_file")")
    while IFS= read -r tbl; do
        [[ -n "$tbl" ]] && CREATED_TABLES+=("$tbl")
    done < <(echo "$flat" | grep -oP '<createTable[^>]*tableName="\K[^"]+' 2>/dev/null || true)
done

is_created_in_pr() {
    local table="$1"
    if [[ ${#CREATED_TABLES[@]} -eq 0 ]]; then
        return 1
    fi

    for ct in "${CREATED_TABLES[@]}"; do
        if [[ "$table" == "$ct" ]]; then
            return 0
        fi
    done
    return 1
}

# ---- Violation tracking ----
declare -a VIOLATIONS=()
BLOCKING_COUNT=0
WARNING_COUNT=0

add_violation() {
    local severity="$1"
    local file="$2"
    local operation="$3"
    local table="$4"
    local risk="$5"
    local detail="${6:-}"

    # False positive check: table created in same PR
    if is_created_in_pr "$table"; then
        severity="INFO"
        risk="$risk (table created in same PR - likely safe)"
    fi

    local effective_severity="$severity"
    if [[ "$severity" == "BLOCKING" ]]; then
        ((BLOCKING_COUNT++)) || true
    elif [[ "$severity" == "CRITICAL" ]] || [[ "$severity" == "HIGH" ]]; then
        if is_critical_table "$table"; then
            effective_severity="CRITICAL"
            ((BLOCKING_COUNT++)) || true
        else
            effective_severity="WARNING"
            ((WARNING_COUNT++)) || true
        fi
    elif [[ "$severity" == "INFO" ]]; then
        ((WARNING_COUNT++)) || true
    fi

    local detail_str=""
    if [[ -n "$detail" ]]; then
        detail_str=" ($detail)"
    fi

    VIOLATIONS+=("${effective_severity}|${file}|${operation}|${table}${detail_str}|${risk}")
}

find_invalid_datetime_precision() {
    local local_file="$1"
    awk '
    BEGIN {}
    {
        remaining = $0
        while (match(remaining, /(type|columnDataType)[[:space:]]*=[[:space:]]*"[^"]*"/)) {
            attr = substr(remaining, RSTART, RLENGTH)
            value = attr
            sub(/^[^"]*"/, "", value)
            sub(/"$/, "", value)

            normalized = toupper(value)
            gsub(/[[:space:]]/, "", normalized)

            if (normalized ~ /^DATETIME(\([0-9]+\))?$/ && normalized != "DATETIME(6)") {
                print FNR "|" attr
            }

            remaining = substr(remaining, RSTART + RLENGTH)
        }
    }
    ' "$local_file"
}

find_invalid_datetime_precision_in_sql_blocks() {
    local local_file="$1"
    awk '
    BEGIN { in_sql = 0 }
    /<sql([[:space:]>]|$)/ { in_sql = 1 }
    in_sql {
        remaining = $0
        while (match(toupper(remaining), /(^|[^[:alnum:]_])DATETIME[[:space:]]*(\([[:space:]]*[0-9]+[[:space:]]*\))?([^[:alnum:]_]|$)/)) {
            token = substr(remaining, RSTART, RLENGTH)
            gsub(/^[^[:alnum:]_]+/, "", token)
            gsub(/[^[:alnum:]_)]+$/, "", token)
            normalized = toupper(token)
            gsub(/[[:space:]]/, "", normalized)

            if (normalized != "DATETIME(6)") {
                print FNR "|" token
            }

            remaining = substr(remaining, RSTART + RLENGTH)
        }
    }
    /<\/sql>/ { in_sql = 0 }
    ' "$local_file"
}

# ---- Scan each changed file ----
for file in "${CHANGED_FILES[@]}"; do
    local_file="$REPO_ROOT/$file"
    if [[ ! -f "$local_file" ]]; then
        echo "Warning: File not found (deleted?): $file"
        continue
    fi

    # Validate it's actually a Liquibase changelog
    if ! grep -q 'databaseChangeLog' "$local_file"; then
        continue
    fi

    echo "Scanning: $file"

    # Flatten multiline XML tags so attribute matching works across line breaks
    FLAT_CONTENT=$(flatten_xml_tags "$(cat "$local_file")")

    # ---- Check DATETIME precision ----
    # MySQL DATETIME defaults to precision 0. Use DATETIME(6) explicitly in
    # Liquibase changelogs to preserve microsecond precision.
    while IFS='|' read -r line match; do
        [[ -z "$line" ]] && continue
        add_violation "BLOCKING" "$file" "datetime-precision" "n/a" \
            "Use DATETIME(6) instead of DATETIME; MySQL DATETIME defaults to precision 0" \
            "line: $line, match: $match"
    done < <(find_invalid_datetime_precision "$local_file")

    while IFS='|' read -r line match; do
        [[ -z "$line" ]] && continue
        add_violation "BLOCKING" "$file" "raw-SQL-datetime-precision" "n/a" \
            "Use DATETIME(6) instead of DATETIME in raw SQL blocks; MySQL DATETIME defaults to precision 0" \
            "line: $line, match: $match"
    done < <(find_invalid_datetime_precision_in_sql_blocks "$local_file")

    # ---- Check simple dangerous operations ----
    for op_index in "${!OP_ELEMENTS[@]}"; do
        op="${OP_ELEMENTS[$op_index]}"
        op_severity="${OP_SEVERITIES[$op_index]}"
        op_risk="${OP_RISKS[$op_index]}"
        # Find all occurrences of <operation ... tableName="xxx" ...> or <operation ... tableName="xxx"/>
        while IFS= read -r table; do
            [[ -z "$table" ]] && continue

            # Try to get extra detail depending on operation type
            detail=""
            case "$op" in
                dropColumn)
                    # <dropColumn tableName="X" columnName="Y"/> or nested <column name="Y"/>
                    col=$(echo "$FLAT_CONTENT" | grep -oP "<dropColumn[^>]*tableName=\"${table}\"[^>]*columnName=\"\K[^\"]*" 2>/dev/null | head -1 || true)
                    if [[ -z "$col" ]]; then
                        col=$(echo "$FLAT_CONTENT" | grep -A5 "<dropColumn[^>]*tableName=\"${table}\"" 2>/dev/null | grep -oP '<column[^>]*name="\K[^"]+' | paste -sd',' || true)
                    fi
                    [[ -n "$col" ]] && detail="column: $col"
                    ;;
                renameColumn)
                    old=$(echo "$FLAT_CONTENT" | grep -oP "<renameColumn[^>]*tableName=\"${table}\"[^>]*oldColumnName=\"\K[^\"]*" 2>/dev/null | head -1 || true)
                    new=$(echo "$FLAT_CONTENT" | grep -oP "<renameColumn[^>]*tableName=\"${table}\"[^>]*newColumnName=\"\K[^\"]*" 2>/dev/null | head -1 || true)
                    [[ -n "$old" ]] && detail="$old -> $new"
                    ;;
                addNotNullConstraint)
                    col=$(echo "$FLAT_CONTENT" | grep -oP "<addNotNullConstraint[^>]*tableName=\"${table}\"[^>]*columnName=\"\K[^\"]*" 2>/dev/null | paste -sd',' || true)
                    [[ -n "$col" ]] && detail="column: $col"
                    ;;
                modifyDataType)
                    col=$(echo "$FLAT_CONTENT" | grep -oP "<modifyDataType[^>]*tableName=\"${table}\"[^>]*columnName=\"\K[^\"]*" 2>/dev/null | head -1 || true)
                    [[ -n "$col" ]] && detail="column: $col"
                    ;;
            esac

            add_violation "$op_severity" "$file" "$op" "$table" "$op_risk" "$detail"
        done < <(echo "$FLAT_CONTENT" | grep -oP "<${op}[^>]*tableName=\"\K[^\"]*" 2>/dev/null | sort -u || true)
    done

    # ---- Check addColumn with NOT NULL and no default ----
    # Strategy: find <addColumn tableName="X"> blocks, then check for nullable="false" without defaultValue
    while IFS= read -r table; do
        [[ -z "$table" ]] && continue
        # Extract the block from <addColumn tableName="X"> to next </addColumn> or />
        block=$(sed -n "/<addColumn[^>]*tableName=\"${table}\"/,/<\/addColumn>/p" "$local_file" 2>/dev/null || true)
        if [[ -z "$block" ]]; then
            continue
        fi
        # Check for nullable="false"
        if echo "$block" | grep -qi 'nullable="false"'; then
            # Check if any defaultValue variant is present
            if ! echo "$block" | grep -qiP 'defaultValue|defaultValueNumeric|defaultValueBoolean|defaultValueDate|defaultValueComputed|valueComputed'; then
                add_violation "HIGH" "$file" "addColumn+NOT_NULL" "$table" \
                    "Adding a NOT NULL column without default value breaks old code INSERTs"
            fi
        fi
    done < <(echo "$FLAT_CONTENT" | grep -oP '<addColumn[^>]*tableName="\K[^"]+' 2>/dev/null | sort -u || true)

    # ---- Check raw SQL blocks for dangerous DDL ----
    # Extract text between <sql> and </sql> tags
    sql_content=$(sed -n '/<sql>/,/<\/sql>/p' "$local_file" 2>/dev/null || true)
    if [[ -n "$sql_content" ]]; then
        while IFS= read -r sql_match; do
            [[ -z "$sql_match" ]] && continue
            sql_match="$(echo "$sql_match" | sed 's/^[[:space:]]*//;s/[[:space:]]*$//')"
            # Try to extract table name from the SQL match (e.g., "DROP TABLE m_loan" -> "m_loan")
            sql_table=$(echo "$sql_match" | grep -ioP '(DROP\s+TABLE\s+|ALTER\s+TABLE\s+)\K\S+' | head -1 || true)
            sql_table="${sql_table//\`/}"  # strip backticks
            sql_table="${sql_table//\"/}"  # strip quotes
            sql_table="${sql_table//;/}"   # strip semicolons
            [[ -z "$sql_table" ]] && sql_table="unknown"
            add_violation "HIGH" "$file" "raw-SQL" "$sql_table" \
                "Raw SQL contains potentially dangerous DDL: $sql_match"
        done < <(echo "$sql_content" | grep -ioP '(DROP\s+(TABLE|COLUMN)\s+\S+|ALTER\s+TABLE\s+\S+\s+(RENAME|DROP)\s+\S+)' 2>/dev/null || true)
    fi
done

echo ""

# ---- Generate reports ----
TOTAL_VIOLATIONS=${#VIOLATIONS[@]}

if [[ $TOTAL_VIOLATIONS -eq 0 ]]; then
    echo "No Liquibase safety violations detected."
    if [[ -n "$OUTPUT_DIR" ]]; then
        cat > "$OUTPUT_DIR/report.md" << 'MDEOF'
## Liquibase DDL Safety Check - PASSED

No Liquibase safety violations detected in this PR's Liquibase migrations.
All migrations are safe for rolling deployments.
MDEOF
        echo '{"violations":[],"summary":{"total":0,"critical":0,"warnings":0}}' > "$OUTPUT_DIR/report.json"
    fi
    exit 0
fi

# Build markdown report
MD_REPORT=""
if [[ $BLOCKING_COUNT -gt 0 ]]; then
    MD_REPORT+="## Liquibase DDL Safety Check - BLOCKED"$'\n\n'
    MD_REPORT+="**${BLOCKING_COUNT} blocking violation(s) found**."$'\n'
    MD_REPORT+="These changes violate Liquibase migration safety rules and must be reviewed."$'\n\n'
else
    MD_REPORT+="## Liquibase DDL Safety Check - WARNINGS"$'\n\n'
    MD_REPORT+="**${WARNING_COUNT} warning(s) found** (non-blocking)."$'\n\n'
fi

MD_REPORT+="| Severity | File | Operation | Table | Risk |"$'\n'
MD_REPORT+="|----------|------|-----------|-------|------|"$'\n'

JSON_VIOLATIONS="["
first_json=true

for v in "${VIOLATIONS[@]}"; do
    IFS='|' read -r sev vfile op tbl risk <<< "$v"

    # Shorten file path for readability
    short_file="${vfile#fineract-provider/src/main/resources/}"
    short_file="${short_file#fineract-loan/src/main/resources/}"
    short_file="${short_file#fineract-investor/src/main/resources/}"
    short_file="${short_file#fineract-savings/src/main/resources/}"
    short_file="${short_file#fineract-progressive-loan/src/main/resources/}"

    MD_REPORT+="| $sev | \`$short_file\` | $op | \`$tbl\` | $risk |"$'\n'

    # JSON
    if [[ "$first_json" == "true" ]]; then
        first_json=false
    else
        JSON_VIOLATIONS+=","
    fi
    # Escape JSON-special characters
    json_risk="${risk//\\/\\\\}"
    json_risk="${json_risk//\"/\\\"}"
    json_risk="${json_risk//$'\n'/\\n}"
    json_risk="${json_risk//$'\t'/\\t}"
    json_tbl="${tbl//\\/\\\\}"
    json_tbl="${json_tbl//\"/\\\"}"
    JSON_VIOLATIONS+="{\"severity\":\"$sev\",\"file\":\"$vfile\",\"operation\":\"$op\",\"table\":\"$json_tbl\",\"risk\":\"$json_risk\"}"
done

JSON_VIOLATIONS+="]"

MD_REPORT+=$'\n'
MD_REPORT+="**Total: $TOTAL_VIOLATIONS violation(s) ($BLOCKING_COUNT blocking, $WARNING_COUNT warnings)**"$'\n\n'

if [[ $BLOCKING_COUNT -gt 0 ]]; then
    MD_REPORT+="### How to resolve"$'\n\n'
    MD_REPORT+="If this change is **intentional** and has been reviewed for rolling deployment safety:"$'\n'
    MD_REPORT+="- Add the \`ddl-safety-override\` label to this PR"$'\n\n'
    MD_REPORT+="For **safe migration patterns**, consider:"$'\n'
    MD_REPORT+="- **Instead of DROP COLUMN**: Mark as deprecated, remove in a future release after all instances are updated"$'\n'
    MD_REPORT+="- **Instead of RENAME COLUMN**: Add new column, backfill, update code, drop old column in a later release"$'\n'
    MD_REPORT+="- **Instead of ADD NOT NULL**: Add as nullable first, backfill defaults, add constraint in a later release"$'\n'
    MD_REPORT+="- **Instead of DATETIME**: Use \`DATETIME(6)\` to preserve MySQL microsecond precision"$'\n'
fi

# Print to console
echo "========================================"
echo "$MD_REPORT"
echo "========================================"

# Write to files
if [[ -n "$OUTPUT_DIR" ]]; then
    echo "$MD_REPORT" > "$OUTPUT_DIR/report.md"
    cat > "$OUTPUT_DIR/report.json" << JSONEOF
{"violations":$JSON_VIOLATIONS,"summary":{"total":$TOTAL_VIOLATIONS,"critical":$BLOCKING_COUNT,"warnings":$WARNING_COUNT}}
JSONEOF
    echo "Reports written to $OUTPUT_DIR/"
fi

# Exit code
if [[ $BLOCKING_COUNT -gt 0 ]]; then
    echo ""
    echo "FAILED: $BLOCKING_COUNT blocking violation(s)."
    exit 1
else
    echo ""
    echo "PASSED with $WARNING_COUNT warning(s) (non-blocking)."
    exit 0
fi
