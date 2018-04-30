--SELECT * FROM pg_authid

--Create and grant permissions
--CREATE ROLE "device_health_admin" WITH NOINHERIT LOGIN ENCRYPTED PASSWORD 'md512f891c8d70729da378192737c19e4aa'; --admin
--CREATE ROLE "device_health_user" WITH NOINHERIT LOGIN ENCRYPTED PASSWORD 'md5de7655f697c2e3129c0b5fe66cacc984'; --user

GRANT USAGE ON SCHEMA shared TO "device_health_admin";
GRANT USAGE ON SCHEMA shared TO "device_health_user";
GRANT USAGE ON SCHEMA events TO "device_health_admin";
GRANT USAGE ON SCHEMA events TO "device_health_user";

GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA shared TO "device_health_user";
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA events TO "device_health_user";

GRANT SELECT, UPDATE ON ALL SEQUENCES IN SCHEMA shared TO "device_health_user";
GRANT SELECT, UPDATE ON ALL SEQUENCES IN SCHEMA events TO "device_health_user";

ALTER DEFAULT PRIVILEGES IN SCHEMA public
  GRANT ALL ON tables TO device_health_admin;

  ALTER DEFAULT PRIVILEGES IN SCHEMA shared
  GRANT ALL ON tables TO device_health_admin;

  ALTER DEFAULT PRIVILEGES IN SCHEMA events
  GRANT ALL ON tables TO device_health_admin;

--GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA shared TO "device_health_admin";
--GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA events TO "device_health_admin";

ALTER ROLE "device_health_user" SET search_path= public, shared, events;