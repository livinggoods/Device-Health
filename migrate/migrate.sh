#!/bin/bash
: ${DB_ENV_POSTGRES_USER:=postgres}
: ${DB_ENV_POSTGRES_SCHEMA:=postgres}


while ! nc -q 1 $DB_PORT_5432_TCP_ADDR $DB_PORT_5432_TCP_PORT </dev/null;
do
  echo "Waiting for database"
  sleep 10;
done

# psql -h db -d  -U postgres -W postgres -p 5432 -a -q -f /migrate/misc_scripts/create_user_roles.sql
#psql postgresql://postgres:postgres@db/device_health_development -f /migrate/misc_scripts/create_user_roles.sql
# "$@"
/opt/mybatis-migrations-3.2.0/bin/migrate up