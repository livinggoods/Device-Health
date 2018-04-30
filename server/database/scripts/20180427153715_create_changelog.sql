--
--    Copyright 2010-2016 the original author or authors.
--
--    Licensed under the Apache License, Version 2.0 (the "License");
--    you may not use this file except in compliance with the License.
--    You may obtain a copy of the License at
--
--       http://www.apache.org/licenses/LICENSE-2.0
--
--    Unless required by applicable law or agreed to in writing, software
--    distributed under the License is distributed on an "AS IS" BASIS,
--    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
--    See the License for the specific language governing permissions and
--    limitations under the License.
--

-- // Create Changelog

-- Default DDL for changelog table that will keep
-- a record of the migrations that have been run.

-- You can modify this to suit your database before
-- running your first migration.

-- Be sure that ID and DESCRIPTION fields exist in
-- BigInteger and String compatible fields respectively.

--// Create Changelog

-- Default DDL for changelog table that will keep
-- a record of the migrations that have been run.

-- You can modify this to suit your database before
-- running your first migration.

-- Be sure that ID and DESCRIPTION fields exist in
-- BigInteger and String compatible fields respectively.

CREATE SCHEMA shared
       AUTHORIZATION "device_health_admin";
COMMENT ON SCHEMA shared IS 'Shared schema';

CREATE SCHEMA events
       AUTHORIZATION "device_health_admin";
COMMENT ON SCHEMA events IS 'Events schema';


ALTER DATABASE device_health_${environment} SET search_path=public, shared, events;



CREATE TABLE shared.${changelog} (
ID NUMERIC(20,0) NOT NULL,
APPLIED_AT VARCHAR(25) NOT NULL,
DESCRIPTION VARCHAR(255) NOT NULL
);

ALTER TABLE shared.${changelog}
ADD CONSTRAINT PK_${changelog}
PRIMARY KEY (id);

--//@UNDO

DROP TABLE ${changelog};

DROP SCHEMA events;
DROP SCHEMA shared;

