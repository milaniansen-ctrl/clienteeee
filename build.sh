#!/usr/bin/env sh
set -eu
cd "$(dirname "$0")"
if ! command -v gradle >/dev/null 2>&1; then
  echo "ERROR: Install Gradle 8.14+ and Java 21 first." >&2
  exit 1
fi
exec gradle clean build
