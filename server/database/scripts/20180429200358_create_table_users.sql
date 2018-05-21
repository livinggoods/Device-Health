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

-- // create table users
-- Migration SQL that makes the change goes here.


CREATE TABLE events.users
(
   id bigserial NOT NULL, 
   chv_id character varying(128) , 
   phone character varying(64),
   android_id character varying(64) not null,
   update_interval integer not null default 60,
   created_at timestamp with time zone NOT NULL DEFAULT LOCALTIMESTAMP, 
   updated_at timestamp with time zone, 
   CONSTRAINT "PK_USERS" PRIMARY KEY (id),
   CONSTRAINT "UNIQUE_CHV_ID" UNIQUE(chv_id)) 
WITH (
  OIDS = FALSE
)
;

CREATE INDEX idx_users_id ON events.users (id ASC NULLS LAST);
CREATE INDEX idx_users_chv_id ON events.users (chv_id ASC NULLS LAST);
CREATE INDEX idx_users_phone ON events.users (phone ASC NULLS LAST);


insert into events.users(chv_id,phone,android_id,update_interval)
values(0,'0799000000',0,60);

--//@UNDO
-- SQL to undo the change goes here.

DROP INDEX events.idx_users_id;
DROP INDEX events.idx_users_chv_id;
DROP INDEX events.idx_users_phone;

DROP TABLE events.users;


