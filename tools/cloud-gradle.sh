#!/usr/bin/env bash
# Use the cloud's existing HTTPS proxy and public CA certificates, retaining TLS verification.
set -euo pipefail
repo_dir="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")/.." && pwd)"
if [[ -z "${JAVA_HOME:-}" ]]; then
    for java_dir in /workspace/toolchains/jdk-25*; do
        if [[ -x "$java_dir/bin/java" ]]; then export JAVA_HOME="$java_dir"; break; fi
    done
fi
: "${JAVA_HOME:?Install JDK 25 and set JAVA_HOME}"
export PATH="$JAVA_HOME/bin:$PATH"
export GRADLE_USER_HOME="${GRADLE_USER_HOME:-/workspace/.gradle}"
if [[ -n "${HTTPS_PROXY:-}" ]]; then
    read -r proxy_host proxy_port < <(python3 - <<'PY'
import os
from urllib.parse import urlsplit
proxy=urlsplit(os.environ['HTTPS_PROXY'])
if not proxy.hostname or any(c.isspace() for c in proxy.hostname):
    raise SystemExit('Invalid HTTPS proxy hostname')
print(proxy.hostname, proxy.port or 8080)
PY
)
    trust_dir="/workspace/toolchains/techmod-trust"
    mkdir -p "$trust_dir"
    cp "$JAVA_HOME/lib/security/cacerts" "$trust_dir/cacerts"
    for certificate in /usr/local/share/ca-certificates/*.crt; do
        [[ -f "$certificate" ]] || continue
        "$JAVA_HOME/bin/keytool" -importcert -noprompt -keystore "$trust_dir/cacerts" \
            -storepass changeit -alias "techmod-$(basename "$certificate")" -file "$certificate" >/dev/null 2>&1
    done
    # GRADLE_OPTS covers the wrapper download; -D arguments propagate to the Gradle build JVM.
    proxy_options="-Dhttps.proxyHost=$proxy_host -Dhttps.proxyPort=$proxy_port -Dhttp.proxyHost=$proxy_host -Dhttp.proxyPort=$proxy_port -Djavax.net.ssl.trustStore=$trust_dir/cacerts"
    export GRADLE_OPTS="${GRADLE_OPTS:-} $proxy_options"
    cd "$repo_dir"
    exec ./gradlew "$@" "-Dhttps.proxyHost=$proxy_host" "-Dhttps.proxyPort=$proxy_port" \
        "-Dhttp.proxyHost=$proxy_host" "-Dhttp.proxyPort=$proxy_port" "-Djavax.net.ssl.trustStore=$trust_dir/cacerts"
fi
cd "$repo_dir"
exec ./gradlew "$@"
