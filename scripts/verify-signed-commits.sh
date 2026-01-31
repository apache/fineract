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

# Usage: ./scripts/verify-signed-commits.sh [--base-ref <ref>] [--head-ref <ref>] [--strict] [--help]
set -e

BASE_REF="origin/develop"
HEAD_REF="HEAD"
STRICT_MODE=false

show_help() {
    cat << 'EOF'
Usage: ./scripts/verify-signed-commits.sh [OPTIONS]

Options:
  --base-ref <ref>   Base reference (default: origin/develop)
  --head-ref <ref>   Head reference (default: HEAD)
  --strict           Exit with error if unsigned commits found
  --help             Show this help
EOF
}

while [[ $# -gt 0 ]]; do
    case $1 in
        --base-ref) BASE_REF="$2"; shift 2 ;;
        --head-ref) HEAD_REF="$2"; shift 2 ;;
        --strict) STRICT_MODE=true; shift ;;
        --help) show_help; exit 0 ;;
        *) echo "Unknown option: $1"; show_help; exit 1 ;;
    esac
done

MERGE_BASE=$(git merge-base "$BASE_REF" "$HEAD_REF" 2>/dev/null || echo "")
if [ -z "$MERGE_BASE" ]; then
    COMMIT_RANGE="$HEAD_REF~10..$HEAD_REF"
else
    COMMIT_RANGE="$MERGE_BASE..$HEAD_REF"
fi

echo "Verifying commit signatures in range: $COMMIT_RANGE"

COMMITS=$(git log --format="%H%x1f%G?%x1f%an%x1f%s" "$COMMIT_RANGE" 2>/dev/null || echo "")
if [ -z "$COMMITS" ]; then
    echo "No commits to verify."
    exit 0
fi

UNSIGNED_COUNT=0

while IFS=$'\x1f' read -r HASH SIG_STATUS AUTHOR SUBJECT; do
    [ -z "$HASH" ] && continue
    SHORT_HASH="${HASH:0:7}"

    case "$SIG_STATUS" in
        N)
            UNSIGNED_COUNT=$((UNSIGNED_COUNT + 1))
            if [ -n "$GITHUB_ACTIONS" ]; then
                echo "::error title=Unsigned Commit::Commit $SHORT_HASH by $AUTHOR is not signed."
            else
                echo "❌ Unsigned: $SHORT_HASH - $SUBJECT ($AUTHOR)"
            fi
            ;;
        *)
            echo "✅ Signed: $SHORT_HASH - $SUBJECT"
            ;;
    esac
done <<< "$COMMITS"

echo ""
echo "Summary: $UNSIGNED_COUNT unsigned commit(s) found."

if [ "$STRICT_MODE" = true ] && [ "$UNSIGNED_COUNT" -gt 0 ]; then
    if [ -n "$GITHUB_ACTIONS" ]; then
        echo "::error::$UNSIGNED_COUNT unsigned commit(s). See CONTRIBUTING.md#signing-your-commits"
    else
        echo "❌ $UNSIGNED_COUNT unsigned commit(s). See CONTRIBUTING.md#signing-your-commits"
    fi
    exit 1
fi

exit 0
