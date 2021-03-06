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

-- // update users drop unique chv id constraint
-- Migration SQL that makes the change goes here.

ALTER TABLE events.users drop  constraint "UNIQUE_CHV_ID";
ALTER TABLE events.users ADD COLUMN fcm_token character varying(512);
ALTER TABLE events.users drop COLUMN if exists update_interval ;
ALTER TABLE events.users ADD COLUMN setting jsonb;
-- //@UNDO
-- SQL to undo the change goes here.

ALTER TABLE events.users DROP COLUMN setting;
ALTER TABLE events.users drop COLUMN if exists update_interval ;
ALTER TABLE events.users add COLUMN update_interval integer not null default 300;

ALTER TABLE events.users add CONSTRAINT "UNIQUE_CHV_ID" UNIQUE(chv_id);
ALTER TABLE events.users drop COLUMN if exists fcm_token ;

