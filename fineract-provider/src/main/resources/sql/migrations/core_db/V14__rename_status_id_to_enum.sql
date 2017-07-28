--
-- Licensed to the Apache Software Foundation (ASF) under one
-- or more contributor license agreements. See the NOTICE file
-- distributed with this work for additional information
-- regarding copyright ownership. The ASF licenses this file
-- to you under the Apache License, Version 2.0 (the
-- "License"); you may not use this file except in compliance
-- with the License. You may obtain a copy of the License at
--
-- http://www.apache.org/licenses/LICENSE-2.0
--
-- Unless required by applicable law or agreed to in writing,
-- software distributed under the License is distributed on an
-- "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
-- KIND, either express or implied. See the License for the
-- specific language governing permissions and limitations
-- under the License.
--


ALTER TABLE m_group CHANGE COLUMN `status_id` `status_enum` INT(5) NOT NULL DEFAULT '300'  ;

ALTER TABLE m_client CHANGE COLUMN `status_id` `status_enum` INT(5) NOT NULL DEFAULT '300'  ;

update r_enum_value
set enum_name = 'status_enum'
where enum_name = 'status_id';