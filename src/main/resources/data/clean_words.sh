#!/bin/bash

# Usage: ./clean_words.sh input.txt output.txt

INPUT="$1"
OUTPUT="$2"

# Check arguments
if [ -z "$INPUT" ] || [ -z "$OUTPUT" ]; then
    echo "Usage: $0 input.txt output.txt"
    exit 1
fi

# Remove accents, extract words, and write to output
cat "$INPUT" | \
    tr '[:upper:]' '[:lower:]' | \
    sed 'y/áéíóúüñ/aeiouun/' | \
    grep -oE '\w+' > "$OUTPUT"

