--SELECT * FROM pg_authid
DROP DATABASE IF EXISTS device_health_development;
CREATE DATABASE device_health_development;

--Create and grant permissions
DROP ROLE IF EXISTS "device_health_admin";
CREATE ROLE "device_health_admin" WITH NOINHERIT LOGIN ENCRYPTED PASSWORD 'md512f891c8d70729da378192737c19e4aa'; --admin
DROP ROLE IF EXISTS "device_health_user";
CREATE ROLE "device_health_user" WITH NOINHERIT LOGIN ENCRYPTED PASSWORD 'md5de7655f697c2e3129c0b5fe66cacc984'; --user


ALTER DATABASE device_health_development OWNER TO device_health_admin;
GRANT ALL ON DATABASE device_health_development TO device_health_admin;

--create database device_health_development;
--connect device_health_development;

CREATE SCHEMA if not exists shared
  AUTHORIZATION "device_health_admin";
COMMENT ON SCHEMA shared IS 'Shared schema';

CREATE SCHEMA if not exists events
     AUTHORIZATION "device_health_admin";
COMMENT ON SCHEMA events IS 'Events schema';


ALTER DATABASE device_health_development SET search_path=public, shared, events;

GRANT USAGE ON SCHEMA shared TO "device_health_user";
GRANT USAGE ON SCHEMA events TO "device_health_user";

GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA shared TO "device_health_user";
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA events TO "device_health_user";

ALTER DEFAULT PRIVILEGES IN SCHEMA events
   GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO "device_health_user";
ALTER DEFAULT PRIVILEGES IN SCHEMA shared
   GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO "device_health_user";

ALTER DEFAULT PRIVILEGES IN SCHEMA events
  GRANT USAGE, SELECT ON SEQUENCES TO device_health_user;

ALTER DEFAULT PRIVILEGES IN SCHEMA shared
  GRANT USAGE, SELECT ON SEQUENCES TO device_health_user;


GRANT SELECT, UPDATE ON ALL SEQUENCES IN SCHEMA shared TO "device_health_user";
GRANT SELECT, UPDATE ON ALL SEQUENCES IN SCHEMA events TO "device_health_user";


ALTER DEFAULT PRIVILEGES IN SCHEMA public
GRANT ALL ON tables TO device_health_admin;

ALTER DEFAULT PRIVILEGES IN SCHEMA shared
GRANT ALL ON tables TO device_health_admin;

ALTER DEFAULT PRIVILEGES IN SCHEMA events
GRANT ALL ON tables TO device_health_admin;

GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA shared TO "device_health_admin";
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA events TO "device_health_admin";

ALTER ROLE "device_health_user" SET search_path= public, shared, events;