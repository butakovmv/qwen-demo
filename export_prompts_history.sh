#!/bin/bash
set -euo pipefail
out="${1:-prompts_history.md}"
tmp=$(mktemp)
trap 'rm -f "$tmp"' EXIT
opencode export ses_16d455cbcffeIN2FQbZZ9DHTcH 2>/dev/null > "$tmp"
jq -r '
[.messages[]
  | select(.info.role == "user")
  | .parts[]
  | select(.type == "text")
  | .text]
  | to_entries[]
  | "## Prompt \(.key + 1)\n\n\(.value)\n"
' "$tmp" > "$out"
echo "Saved $out ($(wc -l < "$out") lines)"
