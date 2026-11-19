#!/usr/bin/env bash
set -euo pipefail
export LC_ALL=C
cd -- "$(dirname -- "$0")"

# Regenerate the typed image map without temporary files; numeric card codes need an alias.
{
  for file in *.svg; do
    name=${file%.svg}
    alias=$name
    [[ $name =~ ^[0-9] ]] && alias=x$name
    printf 'import %s from "./%s"\n' "$alias" "$file"
  done
  printf '\nconst svgImages = {\n'
  for file in *.svg; do
    name=${file%.svg}
    alias=$name
    [[ $name =~ ^[0-9] ]] && alias=x$name
    printf '  "%s": %s,\n' "$name" "$alias"
  done
  printf '}\n\nexport default svgImages\n'
} > svgImages.ts
