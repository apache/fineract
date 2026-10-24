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
# check-liquibase-changelog-registration.sh
#
# Checks that every Liquibase changelog part (a numbered XML file under a
# parts/ directory) is included exactly once by a master changelog, and that
# it is reachable from db.changelog-master.xml (spring.liquibase.changeLog).
# Liquibase only sees files reachable from there, so a part whose <include>,
# or whose module's <include>, was lost, for example while resolving a merge
# conflict, never runs and nothing reports it.
#
# Errors:
#   - a part that no master changelog includes
#   - a changelog that includes parts but is not reachable from
#     db.changelog-master.xml
#   - a file that is included more than once
#   - an <include> that points at a file that does not exist
# Warning (only with --base-ref):
#   - a part added since the base ref that reuses a number the base ref already
#     has in the same directory
#
# Uses only bash, git, grep, awk, sort and comm (no xmllint dependency).
#
# Usage:
#   ./scripts/check-liquibase-changelog-registration.sh [--base-ref origin/develop]
#
# Exit codes:
#   0 — No errors found (may have warnings)
#   1 — Errors found
#   2 — Script error / bad arguments
# ---------------------------------------------------------------------------
set -euo pipefail
export LC_ALL=C

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
BASE_REF=""

# ---- Argument parsing ----
while [[ $# -gt 0 ]]; do
    case "$1" in
        --base-ref)
            if [[ $# -lt 2 ]]; then
                echo "Error: --base-ref needs a value."
                exit 2
            fi
            BASE_REF="$2"; shift 2 ;;
        -h|--help)
            echo "Usage: $0 [--base-ref <ref>]"
            exit 0
            ;;
        *) echo "Unknown argument: $1"; exit 2 ;;
    esac
done

cd "$REPO_ROOT"

if [[ -n "$BASE_REF" ]] && ! git rev-parse --verify --quiet "$BASE_REF^{commit}" > /dev/null; then
    echo "Error: unknown ref: $BASE_REF"
    exit 2
fi

TMP_DIR="$(mktemp -d)"
trap 'rm -rf "$TMP_DIR"' EXIT

PART_PATTERN='/db/changelog/(.*/)?parts/[0-9][^/]*\.xml$'

# ---- Reporting ----
ERRORS=0
WARNINGS=0

emit() {
    local annotation="$1" label="$2" file="$3" message="$4"
    if [[ -n "${GITHUB_ACTIONS:-}" ]]; then
        echo "::${annotation} file=${file}::${message}"
    else
        printf '%-6s %s: %s\n' "$label" "$file" "$message"
    fi
}

error() { ERRORS=$((ERRORS + 1)); emit error ERROR "$1" "$2"; }
warn() { WARNINGS=$((WARNINGS + 1)); emit warning WARN "$1" "$2"; }

# Prints "<rel|cp> <file>" for every <include> in a changelog. XML comments are
# dropped first, so a commented-out <include> counts as missing. <includeAll>
# is not matched. Attribute values may use double or single quotes.
list_includes() {
    awk -v q="'" '
    BEGIN {
        file_attr = "[ \t]file[ \t]*=[ \t]*(\"[^\"]*\"|" q "[^" q "]*" q ")"
        relative_attr = "relativeToChangelogFile[ \t]*=[ \t]*[\"" q "]true[\"" q "]"
    }
    { sub(/\r$/, ""); text = text $0 " " }
    END {
        while ((start = index(text, "<!--")) > 0) {
            rest = substr(text, start + 4)
            stop = index(rest, "-->")
            if (stop == 0) { text = substr(text, 1, start - 1); break }
            text = substr(text, 1, start - 1) substr(rest, stop + 3)
        }
        while (match(text, /<include[ \t][^>]*>/)) {
            tag = substr(text, RSTART, RLENGTH)
            text = substr(text, RSTART + RLENGTH)
            if (!match(tag, file_attr)) continue
            path = substr(tag, RSTART, RLENGTH)
            sub(/^[^=]*=[ \t]*/, "", path)
            path = substr(path, 2, length(path) - 2)
            style = (tag ~ relative_attr) ? "rel" : "cp"
            print style " " path
        }
    }
    ' "$1"
}

# ---- Collect changelog files ----
# Tracked and untracked files, minus ignored ones: build/ holds copies of every
# changelog. Index entries deleted from the working tree are dropped.
git ls-files --cached --others --exclude-standard -- '*/db/changelog/*.xml' \
    | while IFS= read -r f; do
        if [[ -f "$f" ]]; then echo "$f"; fi
    done | sort -u > "$TMP_DIR/files"

grep -E "$PART_PATTERN" "$TMP_DIR/files" > "$TMP_DIR/parts" || true
tr '\n' '\0' < "$TMP_DIR/files" | xargs -0 grep -lE '<include([[:space:]]|$)' > "$TMP_DIR/masters" || true

# Classpath includes (no relativeToChangelogFile) name a path under some
# module's src/main/resources. Lookup table: "<classpath path><TAB><repo path>".
awk '{ i = index($0, "/src/main/resources/"); if (i > 0) print substr($0, i + 20) "\t" $0 }' \
    "$TMP_DIR/files" > "$TMP_DIR/classpath"

# The root changelog, as set by spring.liquibase.changeLog.
ROOT_CLASSPATH="db/changelog/db.changelog-master.xml"
ROOT="$(awk -F'\t' -v p="$ROOT_CLASSPATH" '$1 == p { print $2 }' "$TMP_DIR/classpath")"
if [[ -z "$ROOT" || "$ROOT" == *$'\n'* ]]; then
    echo "Error: expected exactly one $ROOT_CLASSPATH under a src/main/resources directory."
    exit 2
fi

# ---- Resolve every <include> to a repository path ----
# One line per include: "<resolved path><TAB><master><TAB><file as written>".
# Note: "../" in an include path is not normalized; no master uses it.
while IFS= read -r master; do
    while IFS=' ' read -r style target; do
        if [[ "$style" == "rel" ]]; then
            resolved="${master%/*}/$target"
        else
            resolved="$(awk -F'\t' -v p="${target#/}" '$1 == p { print $2; exit }' "$TMP_DIR/classpath")"
            if [[ -z "$resolved" ]]; then
                resolved="$target"
            fi
        fi
        printf '%s\t%s\t%s\n' "$resolved" "$master" "$target"
    done < <(list_includes "$master")
done < "$TMP_DIR/masters" > "$TMP_DIR/includes"

# ---- Check: <include> points at a file that does not exist ----
while IFS=$'\t' read -r master target; do
    error "$master" "includes $target, which does not exist"
done < <(awk -F'\t' 'NR == FNR { known[$0] = 1; next } !($1 in known) { print $2 "\t" $3 }' \
    "$TMP_DIR/files" "$TMP_DIR/includes")

# ---- Check: file included more than once ----
cut -f1 "$TMP_DIR/includes" | sort > "$TMP_DIR/included"
while IFS= read -r path; do
    by="$(awk -F'\t' -v p="$path" '$1 == p { n++; if (!seen[$2]++) list = list (list == "" ? "" : ", ") $2 }
        END { print n " times, by " list }' "$TMP_DIR/includes")"
    error "$path" "included $by"
done < <(uniq -d "$TMP_DIR/included")

# ---- Check: part not included by any master ----
while IFS= read -r part; do
    error "$part" "not included by any master changelog"
done < <(sort -u "$TMP_DIR/included" | comm -23 "$TMP_DIR/parts" -)

# ---- Check: changelog with parts not reachable from the root ----
# Walks the includes from the root. A changelog that is not reached and that no
# other changelog includes is where the chain breaks, so it is reported once,
# with the number of parts below it that never run.
while IFS=$'\t' read -r master count; do
    error "$master" "not reachable from $ROOT, so its $count part(s) never run"
done < <(awk -F'\t' -v root="$ROOT" '
    function walk(start, seen,    queue, head, tail, node, n, kids, i) {
        head = 0
        tail = 0
        queue[tail++] = start
        seen[start] = 1
        while (head < tail) {
            node = queue[head++]
            n = split(children[node], kids, "\t")
            for (i = 1; i <= n; i++) {
                if (kids[i] != "" && !(kids[i] in seen)) {
                    seen[kids[i]] = 1
                    queue[tail++] = kids[i]
                }
            }
        }
    }
    FILENAME == ARGV[1] { is_part[$0] = 1; next }
    {
        children[$2] = children[$2] "\t" $1
        is_master[$2] = 1
        included[$1] = 1
    }
    END {
        split("", reached)
        walk(root, reached)
        for (master in is_master) {
            if ((master in reached) || (master in included)) continue
            split("", below)
            walk(master, below)
            count = 0
            for (file in below) if ((file in is_part) && !(file in reached)) count++
            if (count > 0) print master "\t" count
        }
    }
' "$TMP_DIR/parts" "$TMP_DIR/includes" | sort)

# ---- Warn: new part reuses a number the base ref already has ----
# Liquibase identifies a changeset by id, author and file path, so a reused
# number still runs. It only blurs the order, and renumbering is safe only
# before the part is merged.
if [[ -n "$BASE_REF" ]]; then
    git ls-tree -r --name-only "$BASE_REF" | grep -E "$PART_PATTERN" | sort > "$TMP_DIR/base-parts" || true
    while IFS= read -r part; do
        dir="${part%/*}"
        name="${part##*/}"
        num="${name%%[!0-9]*}"
        # Prints the next free number in the directory if the base already uses num there.
        next_free="$(awk -v dir="$dir/" -v num="$num" -v base="$TMP_DIR/base-parts" '
            index($0, dir) == 1 {
                name = substr($0, length(dir) + 1)
                if (index(name, "/") > 0 || !match(name, /^[0-9]+/)) next
                n = substr(name, 1, RLENGTH) + 0
                if (n > max) max = n
                if (FILENAME == base && n == num + 0) reused = 1
            }
            END { if (reused) printf ("%0" length(num) "d\n", max + 1) }
        ' "$TMP_DIR/base-parts" "$TMP_DIR/parts")"
        if [[ -n "$next_free" ]]; then
            warn "$part" "reuses $num, which $BASE_REF already has; next free is $next_free"
        fi
    done < <(comm -23 "$TMP_DIR/parts" "$TMP_DIR/base-parts")
fi

# ---- Summary ----
PART_COUNT=$(($(wc -l < "$TMP_DIR/parts")))
MASTER_COUNT=$(($(wc -l < "$TMP_DIR/masters")))

if [[ $((ERRORS + WARNINGS)) -gt 0 ]]; then
    echo ""
fi
if [[ $ERRORS -gt 0 ]]; then
    echo "FAILED: $ERRORS error(s), $WARNINGS warning(s)."
    echo "Every numbered file under a parts/ directory must be included exactly once by a master changelog"
    echo "and be reachable from $ROOT."
    exit 1
fi
echo "OK: $PART_COUNT parts across $MASTER_COUNT master changelogs, each included exactly once and reachable from $ROOT_CLASSPATH ($WARNINGS warning(s))."
