#!/usr/bin/env bash
#
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
#
# Fail unless a cucumber run actually executed at least one scenario.
#
# A failing scenario already fails the build through cucumber's exit code, but cucumber exits 0
# when its filters match nothing. Any job that gates on a tag or a feature path therefore needs
# this check: without it, a renamed tag, a moved feature file or a mis-quoted -Pcucumber.tags
# leaves the job green having tested nothing.
#
# Usage: assert-cucumber-ran.sh <cucumber-output-log> [description of what should have run]

set -euo pipefail

log="${1:?usage: assert-cucumber-ran.sh <cucumber-output-log> [expectation]}"
expectation="${2:-Check that the configured feature path and tag expression still match something.}"

if [ ! -s "$log" ]; then
  echo "::error::No cucumber output in $log; cannot tell whether anything ran."
  exit 1
fi

# Strip ANSI colour codes first: cucumber's summary line is colourised on a TTY-less runner too.
summary="$(sed -E 's/\x1B\[[0-9;]*[mK]//g' "$log" | grep -E '^[0-9]+ scenarios?' | tail -1 || true)"

if [ -z "$summary" ]; then
  echo "::error::No cucumber summary line in $log; cannot tell whether anything ran."
  exit 1
fi

if [ "${summary%% *}" = "0" ]; then
  echo "::error::Cucumber ran no scenarios. $expectation"
  exit 1
fi

echo "Cucumber summary: $summary"
