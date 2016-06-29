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

INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ('portfolio', 'ACTIVATE_CENTER', 'CENTER', 'ACTIVATE', 1);
INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ('portfolio', 'ACTIVATE_CENTER_CHECKER', 'CENTER', 'ACTIVATE', 0);

INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ('portfolio', 'ACTIVATE_GROUP', 'GROUP', 'ACTIVATE', 1);
INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ('portfolio', 'ACTIVATE_GROUP_CHECKER', 'GROUP', 'ACTIVATE', 0);


ALTER TABLE `m_group` DROP FOREIGN KEY `FK_m_group_level`;
ALTER TABLE `m_group`
CHANGE COLUMN `external_id` `external_id` VARCHAR(100) NULL DEFAULT NULL AFTER `id`,
CHANGE COLUMN `status_enum` `status_enum` INT(5) NOT NULL DEFAULT '300' AFTER `external_id`,
CHANGE COLUMN `name` `display_name` VARCHAR(100) NOT NULL  AFTER `level_id`,
CHANGE COLUMN `level_Id` `level_id` INT(11) NOT NULL,
ADD CONSTRAINT `FK_m_group_level` FOREIGN KEY (`level_id`) REFERENCES `m_group_level` (`id`);


ALTER TABLE `m_group`
DROP COLUMN `is_deleted`,
ADD COLUMN `activation_date` DATE NULL DEFAULT NULL AFTER `status_enum` ;