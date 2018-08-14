--SELECT * FROM pg_authid
--DROP DATABASE IF EXISTS device_health_development;
--CREATE DATABASE if not exists device_health_development;

--Create and grant permissions

DROP ROLE IF EXISTS device_health_user;
CREATE ROLE device_health_user WITH NOINHERIT LOGIN ENCRYPTED PASSWORD 'md5de7655f697c2e3129c0b5fe66cacc984'; --user


--create database device_health_development;
--connect device_health_development;

CREATE SCHEMA if not exists shared;
COMMENT ON SCHEMA shared IS 'Shared schema';

CREATE SCHEMA if not exists events;
COMMENT ON SCHEMA events IS 'Events schema';

ALTER DATABASE device_health_development SET search_path=public, shared, events;
ALTER ROLE device_health_user SET search_path= public, shared, events;

GRANT USAGE ON SCHEMA shared TO device_health_user;
GRANT USAGE ON SCHEMA events TO device_health_user;

GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA shared TO device_health_user;
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA events TO device_health_user;

ALTER DEFAULT PRIVILEGES IN SCHEMA events
   GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO device_health_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA shared
   GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO device_health_user;

ALTER DEFAULT PRIVILEGES IN SCHEMA events
  GRANT USAGE, SELECT ON SEQUENCES TO device_health_user;

ALTER DEFAULT PRIVILEGES IN SCHEMA shared
  GRANT USAGE, SELECT ON SEQUENCES TO device_health_user;


GRANT SELECT, UPDATE ON ALL SEQUENCES IN SCHEMA shared TO device_health_user;
GRANT SELECT, UPDATE ON ALL SEQUENCES IN SCHEMA events TO device_health_user;
