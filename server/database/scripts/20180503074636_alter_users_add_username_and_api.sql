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

-- // alter users add username and api
-- Migration SQL that makes the change goes here.

truncate  table events.users cascade;


ALTER TABLE events.users
ADD COLUMN version_code int NOT NULL DEFAULT 1;
ALTER TABLE events.users
ADD COLUMN version_name character varying(128);

ALTER TABLE events.users
ADD COLUMN username character varying(128) NOT NULL;
ALTER TABLE events.users
ADD COLUMN password character varying(128);

CREATE INDEX idx_users_username ON events.users (username ASC);

-- //@UNDO
-- SQL to undo the change goes here.

ALTER TABLE events.users DROP COLUMN password;
ALTER TABLE events.users DROP COLUMN username ;
ALTER TABLE events.users DROP COLUMN version_name;
ALTER TABLE events.users DROP COLUMN version_code;
