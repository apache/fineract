#!/bin/bash
set -e

# https://developer.cloudbees.com/bin/view/DEV/Node+Builds
curl -s -o use-node https://repository-cloudbees.forge.cloudbees.com/distributions/ci-addons/node/use-node
NODE_VERSION=0.11.12 . ./use-node

./build.sh
