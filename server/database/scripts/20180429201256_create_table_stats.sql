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


CREATE TABLE events.stats
(
   id bigserial NOT NULL, 
   user_id bigint NOT NULL,
   latitude double precision ,
   longitude double precision ,
   accuracy double precision  ,
   provider character varying(128),
   recorded_at timestamp with time zone,
   created_at timestamp with time zone NOT NULL DEFAULT LOCALTIMESTAMP, 
   updated_at timestamp with time zone, 
   CONSTRAINT "PK_stats" PRIMARY KEY (id),
   CONSTRAINT "FK_stats_user_id" FOREIGN KEY (user_id) REFERENCES events.users (id) ON UPDATE NO ACTION ON DELETE NO ACTION ) 
WITH (
  OIDS = FALSE
)
;

CREATE INDEX idx_stats_id ON events.stats (id ASC NULLS LAST);

--//@UNDO
-- SQL to undo the change goes here.

DROP INDEX events.idx_stats_id;

DROP TABLE events.stats;


