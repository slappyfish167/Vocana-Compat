#!/usr/bin/env bash
# Resolve modpack/mods.txt into packwiz metadata (mods/*.pw.toml), one entry at a time.
#
# Usage: scripts/resolve.sh [mods.txt]          (run from the modpack directory, with packwiz on PATH)
# Environment:
#   INCLUDE_CURSEFORGE=true   also resolve cf:<slug> entries (default: skip them)
#   GITHUB_STEP_SUMMARY       when set (GitHub Actions), the report is appended there too
#   RESOLVE_DELAY             seconds to wait between entries (default 1; keeps clear of Modrinth's rate limit)
#
# Every entry is added by URL (https://modrinth.com/mod/<id>), never by bare slug: a bare slug that Modrinth does not
# know makes packwiz fall back to a search and, with -y, install the first hit. A URL fails instead.
#
# Writes .resolve/results.tsv and .resolve/report.md. Exits 1 if a `required` entry failed, else 0.
set -uo pipefail

list="${1:-mods.txt}"
include_cf="${INCLUDE_CURSEFORGE:-false}"
delay="${RESOLVE_DELAY:-1}"
work=".resolve"
mkdir -p "$work/logs"
results="$work/results.tsv"
report="$work/report.md"
: > "$results"

if [[ ! -f pack.toml ]]; then
  echo "resolve.sh: run from the directory holding pack.toml" >&2
  exit 2
fi
if ! command -v packwiz > /dev/null; then
  echo "resolve.sh: packwiz is not on PATH (go install github.com/packwiz/packwiz@latest)" >&2
  exit 2
fi

meta_files() { find . -path ./.resolve -prune -o -name '*.pw.toml' -print | sort; }

set_side() { # $1 = meta file, $2 = side
  if grep -q '^side = ' "$1"; then
    sed -i "s/^side = .*/side = \"$2\"/" "$1"
  else
    sed -i "0,/^filename = /s//side = \"$2\"\n&/" "$1"
  fi
}

# Runs packwiz up to three times; a rate-limit or network error waits and retries.
add_entry() { # $1 = source (mr|cf), $2 = id, $3 = log file
  local url attempt
  if [[ "$1" == cf ]]; then url="https://www.curseforge.com/minecraft/mc-mods/$2"; else url="https://modrinth.com/mod/$2"; fi
  for attempt in 1 2 3; do
    if [[ "$1" == cf ]]; then
      packwiz -y curseforge add "$url" > "$3" 2>&1 && return 0
    else
      packwiz -y modrinth add "$url" > "$3" 2>&1 && return 0
    fi
    if grep -qiE '429|too many requests|rate limit|timeout|connection reset|EOF|50[234]' "$3"; then
      echo "  ...transient failure on attempt $attempt, waiting before retry"
      sleep $((attempt * 30))
    else
      return 1
    fi
  done
  return 1
}

section="(none)"
declare -a listed=()
total=0
while IFS= read -r raw || [[ -n "$raw" ]]; do
  line="${raw%$'\r'}"
  if [[ "$line" =~ ^##[[:space:]]+(.*)$ ]]; then
    section="${BASH_REMATCH[1]}"
    continue
  fi
  line="${line%%#*}"
  read -r -a fields <<< "$line"
  [[ ${#fields[@]} -eq 0 ]] && continue

  id="${fields[0]}"
  required=no
  side=""
  for f in "${fields[@]:1}"; do
    case "$f" in
      required) required=yes ;;
      side=client|side=server|side=both) side="${f#side=}" ;;
      *) echo "warning: unknown flag '$f' on '$id' (ignored)" ;;
    esac
  done

  source=mr
  if [[ "$id" == cf:* ]]; then source=cf; id="${id#cf:}"; fi
  listed+=("$id")
  total=$((total + 1))
  log="$work/logs/${source}-${id}.log"

  if [[ "$source" == cf && "$include_cf" != true ]]; then
    printf '%s\t%s\t%s\t%s\t%s\t%s\t%s\n' "$section" "cf:$id" "$required" "${side:--}" skipped "-" "CurseForge extras off" >> "$results"
    echo "skip     cf:$id (include_curseforge is off)"
    continue
  fi

  before="$(meta_files)"
  echo "add      $([[ $source == cf ]] && echo cf:)$id"
  if add_entry "$source" "$id" "$log"; then
    meta="$(find . -path ./.resolve -prune -o -name "$id.pw.toml" -print | head -n 1)"
    if [[ -z "$meta" ]]; then
      # Added under its canonical slug (the entry was a project id or a renamed slug): take the newest new file.
      meta="$(comm -13 <(echo "$before") <(meta_files) | head -n 1)"
    fi
    if [[ -z "$meta" ]]; then
      # Already present, for example pulled in earlier as a dependency and not rewritten.
      printf '%s\t%s\t%s\t%s\t%s\t%s\t%s\n' "$section" "$id" "$required" "${side:--}" resolved "-" "already present" >> "$results"
      continue
    fi
    [[ -n "$side" ]] && set_side "$meta" "$side"
    file="$(sed -n 's/^filename = "\(.*\)"$/\1/p' "$meta" | head -n 1)"
    mside="$(sed -n 's/^side = "\(.*\)"$/\1/p' "$meta" | head -n 1)"
    printf '%s\t%s\t%s\t%s\t%s\t%s\t%s\n' "$section" "$([[ $source == cf ]] && echo cf:)$id" "$required" "${mside:-both}" resolved "${file:--}" "${meta#./}" >> "$results"
  else
    reason="$(grep -v '^\s*$' "$log" | tail -n 1 | tr '\t|' '  ' | cut -c1-160)"
    printf '%s\t%s\t%s\t%s\t%s\t%s\t%s\n' "$section" "$([[ $source == cf ]] && echo cf:)$id" "$required" "${side:--}" failed "-" "${reason:-packwiz failed}" >> "$results"
    echo "  FAILED: ${reason:-see $log}"
  fi
  sleep "$delay"
done < "$list"

# Dependencies packwiz added on its own (metadata files no listed entry accounts for).
mapfile -t all_meta < <(meta_files)
declare -a deps=()
for m in "${all_meta[@]}"; do
  base="$(basename "$m" .pw.toml)"
  if ! grep -qF "${m#./}" <(cut -f7 "$results") && [[ ! " ${listed[*]} " == *" $base "* ]]; then
    deps+=("$base")
  fi
done

resolved=$(awk -F'\t' '$5=="resolved"' "$results" | wc -l)
failed=$(awk -F'\t' '$5=="failed"' "$results" | wc -l)
skipped=$(awk -F'\t' '$5=="skipped"' "$results" | wc -l)
req_failed=$(awk -F'\t' '$5=="failed" && $3=="yes"' "$results" | wc -l)

{
  echo "## Vocana: Resonance — mod resolution"
  echo
  echo "| Listed | Resolved | Failed | Skipped | Added as dependencies | Jars in pack |"
  echo "|---:|---:|---:|---:|---:|---:|"
  echo "| $total | $resolved | $failed | $skipped | ${#deps[@]} | ${#all_meta[@]} |"
  echo
  if (( req_failed > 0 )); then
    echo "**$req_failed required mod(s) failed — the build stops here.**"
    echo
  fi
  if (( failed > 0 )); then
    echo "### Failed"
    echo
    echo "| Section | Mod | Required | Reason |"
    echo "|---|---|---|---|"
    awk -F'\t' '$5=="failed" {printf "| %s | `%s` | %s | %s |\n", $1, $2, $3, $7}' "$results"
    echo
  fi
  echo "<details><summary>All entries</summary>"
  echo
  echo "| Section | Mod | Required | Side | Status | File |"
  echo "|---|---|---|---|---|---|"
  awk -F'\t' '{printf "| %s | `%s` | %s | %s | %s | %s |\n", $1, $2, $3, $4, $5, $6}' "$results"
  echo
  echo "</details>"
  if (( ${#deps[@]} > 0 )); then
    echo
    echo "<details><summary>Dependencies added by packwiz</summary>"
    echo
    # shellcheck disable=SC2016 # the backticks are Markdown
    printf '%s\n' "${deps[@]}" | sed 's/.*/- `&`/'
    echo
    echo "</details>"
  fi
} > "$report"

cat "$report"
if [[ -n "${GITHUB_STEP_SUMMARY:-}" ]]; then
  cat "$report" >> "$GITHUB_STEP_SUMMARY"
fi

if (( req_failed > 0 )); then
  awk -F'\t' '$5=="failed" && $3=="yes" {print "::error::required mod " $2 " did not resolve: " $7}' "$results"
  exit 1
fi
exit 0
