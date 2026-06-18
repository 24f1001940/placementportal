#!/bin/sh
set -e

if [ -z "$DB_URL" ] && [ -n "$PGHOST" ] && [ -n "$PGDATABASE" ]; then
  export DB_URL="jdbc:postgresql://${PGHOST}:${PGPORT:-5432}/${PGDATABASE}"
fi

if [ -z "$DB_URL" ] && [ -n "$DATABASE_URL" ]; then
  DB_URL_VALUE=""
  case "$DATABASE_URL" in
    postgres://*) DB_URL_VALUE="${DATABASE_URL#postgres://}" ;;
    postgresql://*) DB_URL_VALUE="${DATABASE_URL#postgresql://}" ;;
    *) export DB_URL="$DATABASE_URL" ;;
  esac

  if [ -n "$DB_URL_VALUE" ]; then
    DB_HOST_PATH="$DB_URL_VALUE"
    if [ "${DB_URL_VALUE#*@}" != "$DB_URL_VALUE" ]; then
      DB_CREDENTIALS="${DB_URL_VALUE%%@*}"
      DB_HOST_PATH="${DB_URL_VALUE#*@}"
      if [ -z "$DB_USERNAME" ]; then
        export DB_USERNAME="${DB_CREDENTIALS%%:*}"
      fi
      if [ -z "$DB_PASSWORD" ] && [ "${DB_CREDENTIALS#*:}" != "$DB_CREDENTIALS" ]; then
        export DB_PASSWORD="${DB_CREDENTIALS#*:}"
      fi
    fi
    export DB_URL="jdbc:postgresql://${DB_HOST_PATH}"
  fi
fi

if [ -n "$DB_URL" ]; then
  case "$DB_URL" in
    jdbc:*) ;;
    postgresql://*) export DB_URL="jdbc:${DB_URL}" ;;
    postgres://*) export DB_URL="jdbc:postgresql://${DB_URL#postgres://}" ;;
  esac
fi

case "$DB_URL" in
  jdbc:postgresql:*) export DB_DRIVER="${DB_DRIVER:-org.postgresql.Driver}" ;;
esac

if [ -z "$DB_USERNAME" ] && [ -n "$PGUSER" ]; then
  export DB_USERNAME="$PGUSER"
fi

if [ -z "$DB_PASSWORD" ] && [ -n "$PGPASSWORD" ]; then
  export DB_PASSWORD="$PGPASSWORD"
fi

if [ -z "$UPLOAD_DIR" ] && [ -n "$RAILWAY_VOLUME_MOUNT_PATH" ]; then
  export UPLOAD_DIR="${RAILWAY_VOLUME_MOUNT_PATH}/resumes"
fi

exec java -jar app.jar
