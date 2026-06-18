#!/bin/sh
set -e

if [ -n "$DB_URL" ]; then
  case "$DB_URL" in
    jdbc:*) ;;
    postgresql://*) export DB_URL="jdbc:${DB_URL}" ;;
    postgres://*) export DB_URL="jdbc:postgresql://${DB_URL#postgres://}" ;;
  esac
fi

exec java -jar app.jar
