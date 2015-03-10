#!/bin/bash

# Exit the script if any command returns a non-true return value (http://www.davidpashley.com/articles/writing-robust-shell-scripts/)
set -e

git --version
# TODO UNCOMMENT THIS once https://github.com/openMF/mifosx/pull/1291 is merged!
# git pull
# This does NOT get the most recent revision from the submodule repository.
# Instead, it only gets the revision of the submodule that is recorded in the revision of the main repository.
## NOT git submodule update --remote
# The following is very important, because even if the main (API) repo is latest,
# the sub-module initially will be a fixed old rev. and only this grabs real latest:
git submodule foreach 'git checkout develop && git pull --ff-only origin develop'

cd apps/community-app
./build.sh
cd ../../mifosng-provider/
./gradlew -Penv=dev clean dist
