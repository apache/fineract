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

# Usage: ./scripts/check-dependency-licenses.sh [--base-sha <sha>] [--help]
set -e

BASE_SHA=""
CLEANUP_WORKTREE=""

show_help() {
    cat << 'EOF'
Usage: ./scripts/check-dependency-licenses.sh [OPTIONS]

Options:
  --base-sha <sha>   Base SHA or ref to compare against (e.g. origin/develop).
                     If specified, the script only gates on new/changed violations.
                     If not specified, the script gates on all violations.
  --help             Show this help
EOF
}

while [[ $# -gt 0 ]]; do
    case $1 in
        --base-sha) BASE_SHA="$2"; shift 2 ;;
        --help) show_help; exit 0 ;;
        *) echo "Unknown option: $1"; show_help; exit 1 ;;
    esac
done

# Ensure we run from repository root
cd "$(git rev-parse --show-toplevel)"

# Temporary directories and files
export ALLOWED_LICENSES_FILE="$(pwd)/build/allowed-licenses.json"
export LICENSE_CHECK_INIT_SCRIPT="$(pwd)/build/license-check-init.gradle"
mkdir -p build

cleanup() {
    echo "Cleaning up temporary files and worktrees..."
    rm -f "$ALLOWED_LICENSES_FILE" "$LICENSE_CHECK_INIT_SCRIPT"
    if [ -z "$GITHUB_ACTIONS" ]; then
        rm -f head-violations.json base-violations.json new-violations.json
    fi
    if [ -n "$CLEANUP_WORKTREE" ] && [ -d "$CLEANUP_WORKTREE" ]; then
        git worktree remove "$CLEANUP_WORKTREE" --force || true
    fi
}
trap cleanup EXIT

# 1. Write the allowed licenses configuration
cat > "$ALLOWED_LICENSES_FILE" <<'EOF'
{
  "allowedLicenses": [
    { "moduleLicense": "Apache-2.0" },
    { "moduleLicense": "Apache License, Version 2.0" },
    { "moduleLicense": "The Apache Software License, Version 2.0" },
    { "moduleLicense": "Apache 2.0" },
    { "moduleLicense": "MIT" },
    { "moduleLicense": "MIT License" },
    { "moduleLicense": "The MIT License" },
    { "moduleLicense": "BSD-2-Clause" },
    { "moduleLicense": "BSD-3-Clause" },
    { "moduleLicense": "BSD License 3" },
    { "moduleLicense": "Eclipse Public License - v 1.0" },
    { "moduleLicense": "Eclipse Public License - v 2.0" },
    { "moduleLicense": "Eclipse Distribution License - v 1.0" },
    { "moduleLicense": "Common Development and Distribution License" },
    { "moduleLicense": "CDDL/GPLv2+CE" },
    { "moduleLicense": "Bouncy Castle Licence" },
    { "moduleLicense": "Public Domain" },
    { "moduleLicense": "ISC" }
  ]
}
EOF

# Write the init script
cat > "$LICENSE_CHECK_INIT_SCRIPT" <<'EOF'
allprojects {
    plugins.withId('com.github.jk1.dependency-license-report') {
        licenseReport {
            allowedLicensesFile = new File(System.getenv('ALLOWED_LICENSES_FILE'))
        }
    }
}
EOF

# 2. Check licenses on PR head / current branch
echo "Checking licenses on current branch..."
./gradlew checkLicense --init-script "$LICENSE_CHECK_INIT_SCRIPT" --continue || true

# Collect head violations
find . -path '*/licenses/dependencies-without-allowed-license.json' -exec cat {} \; \
  | jq -s '[.[].dependenciesWithoutAllowedLicenses[]?] | unique_by(.moduleName + "::" + .moduleLicense)' \
  > head-violations.json

# 3. Handle base branch if BASE_SHA is specified
if [ -n "$BASE_SHA" ]; then
    echo "Comparing against base: $BASE_SHA"
    WORKTREE_PATH="../fineract-pr-base"
    CLEANUP_WORKTREE="$WORKTREE_PATH"

    # Add worktree
    git worktree add "$WORKTREE_PATH" "$BASE_SHA"

    # Run check on base
    echo "Checking licenses on base branch..."
    (cd "$WORKTREE_PATH" && ./gradlew checkLicense --init-script "$LICENSE_CHECK_INIT_SCRIPT" --continue) || true

    # Collect base violations
    find "$WORKTREE_PATH" -path '*/licenses/dependencies-without-allowed-license.json' -exec cat {} \; \
      | jq -s '[.[].dependenciesWithoutAllowedLicenses[]?] | unique_by(.moduleName + "::" + .moduleLicense)' \
      > base-violations.json

    # Gate on licenses that are new or changed relative to the base branch
    jq -n --slurpfile head head-violations.json --slurpfile base base-violations.json '
      ($base[0] | map(.moduleName + "::" + .moduleLicense)) as $baseKeys
      | $head[0] | map(select((.moduleName + "::" + .moduleLicense) as $k | ($baseKeys | index($k)) == null))
    ' > new-violations.json
else
    # Gate on all violations if no base SHA is provided
    cp head-violations.json new-violations.json
fi

COUNT=$(jq 'length' new-violations.json)

if [ -n "$GITHUB_STEP_SUMMARY" ]; then
    {
      echo "## SBOM license check"
      echo "New/changed dependency licenses not on the allowlist: $COUNT"
    } >> "$GITHUB_STEP_SUMMARY"
fi

if [ "$COUNT" -gt 0 ]; then
    if [ -n "$BASE_SHA" ]; then
        echo "New/changed dependency licenses not on the allowlist, relative to the base branch ($BASE_SHA):"
    else
        echo "Dependency licenses not on the allowlist:"
    fi
    jq -r '.[] | "- \(.moduleName) \(.moduleVersion): \(.moduleLicense)"' new-violations.json

    if [ -n "$GITHUB_STEP_SUMMARY" ]; then
        jq -r '.[] | "- \(.moduleName) \(.moduleVersion): \(.moduleLicense)"' new-violations.json >> "$GITHUB_STEP_SUMMARY"
    fi
    exit 1
fi

echo "No new or changed dependency licenses require review."
if [ -n "$GITHUB_STEP_SUMMARY" ]; then
    echo "No new or changed dependency licenses require review." >> "$GITHUB_STEP_SUMMARY"
fi

exit 0
