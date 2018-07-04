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

-- // create table data balance
-- Migration SQL that makes the change goes here.




CREATE TABLE events.data_balance
(
   id bigserial NOT NULL, 
   user_id bigint NOT NULL,
   balance double precision ,
   balance_message character varying(256),
   phone character varying(128),
   info jsonb,
   message character varying(256),
   recorded_at timestamp with time zone,
   created_at timestamp with time zone NOT NULL DEFAULT LOCALTIMESTAMP, 
   updated_at timestamp with time zone, 
   CONSTRAINT "PK_data_balance" PRIMARY KEY (id),
   CONSTRAINT "FK_data_balance_user_id" FOREIGN KEY (user_id) REFERENCES events.users (id) ON UPDATE NO ACTION ON DELETE NO ACTION ) 
WITH (
  OIDS = FALSE
)
;

CREATE INDEX idx_data_balance_id ON events.data_balance (id ASC NULLS LAST);
CREATE INDEX idx_data_balance_balance ON events.data_balance (balance ASC NULLS LAST);

--//@UNDO
-- SQL to undo the change goes here.

DROP TABLE events.data_balance;
