#!/bin/sh
set -eu

POSTGRES_HOST="${POSTGRES_HOST:-postgres}"
POSTGRES_PORT_INTERNAL="${POSTGRES_PORT_INTERNAL:-5432}"

if [ -z "${SPRING_DATASOURCE_URL:-}" ] && [ -n "${POSTGRES_DB:-}" ]; then
  export SPRING_DATASOURCE_URL="jdbc:postgresql://${POSTGRES_HOST}:${POSTGRES_PORT_INTERNAL}/${POSTGRES_DB}"
fi

if [ -z "${SPRING_DATASOURCE_USERNAME:-}" ] && [ -n "${POSTGRES_USER:-}" ]; then
  export SPRING_DATASOURCE_USERNAME="${POSTGRES_USER}"
fi

if [ -z "${SPRING_DATASOURCE_PASSWORD:-}" ] && [ -n "${POSTGRES_PASSWORD:-}" ]; then
  export SPRING_DATASOURCE_PASSWORD="${POSTGRES_PASSWORD}"
fi

exec java ${JAVA_OPTS:-} -jar /app/app.jar "$@"

