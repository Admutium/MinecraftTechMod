#!/usr/bin/env bash
# Exercise geometry without loading Minecraft, Loom, a display or a game server.
set -euo pipefail
repo_dir="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")/.." && pwd)"
cache_dir="${TECHMOD_TEST_CACHE:-${TMPDIR:-/tmp}/techmod-core-tests}"
mkdir -p "$cache_dir/classes"
junit_jar="$cache_dir/junit-platform-console-standalone-1.11.4.jar"
expected_sha512=d8ba605f8f223d94b6a5a82debd641da9d5755cd8d68178afba45e3146d574f7b70dcc9d08864ca0abc0379dba055a26e0cdbef6499b862fcb4e54f1448d6bb8
if [[ ! -f "$junit_jar" ]]; then
    curl --fail --location --retry 2 https://repo.maven.apache.org/maven2/org/junit/platform/junit-platform-console-standalone/1.11.4/junit-platform-console-standalone-1.11.4.jar -o "$junit_jar"
fi
printf '%s  %s\n' "$expected_sha512" "$junit_jar" | sha512sum --check --status
java_bin="${JAVA_HOME:+$JAVA_HOME/bin/}java"
javac_bin="${JAVA_HOME:+$JAVA_HOME/bin/}javac"
"$javac_bin" --release 25 -cp "$junit_jar" -d "$cache_dir/classes" \
    "$repo_dir"/src/main/java/dev/admutium/techmod/cable/{Vec,Face,Node,SurfaceTopology,CableSpan}.java \
    "$repo_dir/src/test/java/dev/admutium/techmod/cable/CableGeometryTest.java"
"$java_bin" -jar "$junit_jar" execute --class-path "$cache_dir/classes" \
    --select-class dev.admutium.techmod.cable.CableGeometryTest --disable-banner
