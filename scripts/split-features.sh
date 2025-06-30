#!/bin/bash

# Exit on any error
set -e

# Get the number of shards (default to 10 if not provided)
NUM_SHARDS=${1:-10}
# Convert to 0-based index for internal calculations
SHARD_INDEX_ZERO_BASED=$((${2:-1} - 1))
SHARD_INDEX=${2:-1}  # Keep original 1-based index for output

# Directory containing feature files
FEATURES_DIR="fineract-e2e-tests-runner/src/test/resources/features"
TEMP_FILE="/tmp/feature_scenarios_$(date +%s).txt"

# Check if features directory exists
if [ ! -d "$FEATURES_DIR" ]; then
  echo "Error: Features directory not found at $FEATURES_DIR"
  exit 1
fi

# Function to count scenarios in a feature file
count_scenarios() {
  local file="$1"
  # Count scenario and scenario outline keywords, excluding commented lines
  grep -v '^[[:space:]]*#' "$file" | grep -c 'Scenario\( Outline\)\?:' || echo "0"
}

# Process each feature file and count scenarios
echo "Analyzing feature files to count scenarios..."
> "$TEMP_FILE"

while IFS= read -r -d $'\0' file; do
  # Remove the 'fineract-e2e-tests-runner/' prefix
  rel_path="${file#fineract-e2e-tests-runner/}"
  scenario_count=$(count_scenarios "$file")
  echo "$scenario_count $rel_path"
done < <(find "$FEATURES_DIR" -type f -name '*.feature' -print0) | sort -nr > "$TEMP_FILE"

# Read the sorted list of features
SORTED_FEATURES=()
while IFS= read -r line; do
  # Extract just the file path (removing the scenario count)
  path=$(echo "$line" | awk '{$1=""; print $0}' | sed 's/^ //')
  [ -n "$path" ] && SORTED_FEATURES+=("$path")
done < "$TEMP_FILE"
TOTAL_FEATURES=${#SORTED_FEATURES[@]}

# Check if any feature files were found
if [ $TOTAL_FEATURES -eq 0 ]; then
  echo "Warning: No feature files found in $FEATURES_DIR"
  # Create an empty feature list file
  FEATURE_LIST_FILE="feature_shard_${SHARD_INDEX}.txt"
  > "$FEATURE_LIST_FILE"
  echo "Created empty feature list file: $FEATURE_LIST_FILE"
  rm -f "$TEMP_FILE"
  exit 0
fi

# Create a file to store the feature file paths
# Use the 1-based index for the filename
FEATURE_LIST_FILE="feature_shard_${SHARD_INDEX}.txt"
> "$FEATURE_LIST_FILE"

# Distribute features to shards in a round-robin fashion to balance scenario counts
for ((i=0; i<TOTAL_FEATURES; i++)); do
  if (( i % NUM_SHARDS == SHARD_INDEX_ZERO_BASED )); then
    echo "${SORTED_FEATURES[$i]}" >> "$FEATURE_LIST_FILE"
  fi
done

# Count scenarios in this shard
SHARD_SCENARIOS=0
while IFS= read -r line; do
  [ -z "$line" ] && continue
  count=$(grep -m 1 -F "$line" "$TEMP_FILE" | awk '{print $1}')
  SHARD_SCENARIOS=$((SHARD_SCENARIOS + count))
done < "$FEATURE_LIST_FILE"

# Get the list of features in this shard for output
SHARD_FEATURES=()
while IFS= read -r line; do
  [ -n "$line" ] && SHARD_FEATURES+=("$line")
done < "$FEATURE_LIST_FILE"
NUM_FEATURES=${#SHARD_FEATURES[@]}

# Output the shard information
echo "Shard $SHARD_INDEX (1-based): $NUM_FEATURES features with $SHARD_SCENARIOS total scenarios"
if [ $NUM_FEATURES -gt 0 ]; then
  echo "First feature: ${SHARD_FEATURES[0]}"
  if [ $NUM_FEATURES -gt 1 ]; then
    echo "Last feature: ${SHARD_FEATURES[$((NUM_FEATURES-1))]}"
  fi
  echo "Features in this shard (scenario count):"
  while IFS= read -r line; do
    [ -z "$line" ] && continue
    count=$(grep -m 1 -F "$line" "$TEMP_FILE" | awk '{print $1}')
    echo "  - $line ($count scenarios)"
  done < "$FEATURE_LIST_FILE"
fi

echo "Feature list written to $FEATURE_LIST_FILE"

# Clean up temp file
rm -f "$TEMP_FILE"

# Set output for GitHub Actions
if [ -n "$GITHUB_OUTPUT" ]; then
  echo "FEATURE_LIST_FILE=$FEATURE_LIST_FILE" >> $GITHUB_OUTPUT
fi
