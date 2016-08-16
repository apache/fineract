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

INSERT INTO `c_configuration` (`id`, `name`,`value`, `enabled`, `description`) 
VALUES (NULL, 'min-clients-in-group', '5', '0',"Minimum number of Clients that a Group should have");

INSERT INTO `c_configuration` (`id`, `name`, `value`, `enabled`, `description`)
VALUES (NULL, 'max-clients-in-group', '5', '0', "Maximum number of Clients that a Group can have");

ALTER TABLE `m_group_level`
	DROP COLUMN `min_clients`,
	DROP COLUMN `max_clients`;
