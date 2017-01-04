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

INSERT INTO `c_configuration` (`name`, `value`, `date_value`, `enabled`, `is_trap_door`, `description`) VALUES ( 'daily-tpt-limit', 0, NULL, 0, 0, 'Daily limit for third party transfers');

CREATE TABLE `m_selfservice_beneficiaries_tpt` (
	`id` BIGINT NOT NULL AUTO_INCREMENT,
	`app_user_id` BIGINT NOT NULL,
	`name` VARCHAR(50) NOT NULL,
	`office_id` BIGINT NOT NULL,
	`client_id` BIGINT NOT NULL,
	`account_id` BIGINT NOT NULL,
	`account_type` SMALLINT(4) NOT NULL,
	`transfer_limit` BIGINT NULL DEFAULT 0,
	`is_active` BIT(1) NOT NULL DEFAULT 0,
	PRIMARY KEY (`id`),
	UNIQUE INDEX `name` (`name`, `app_user_id`, `is_active`)
)
COLLATE='utf8_general_ci'
ENGINE=InnoDB
;

INSERT INTO `m_permission`(`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES
('SSBENEFICIARYTPT', 'READ_SSBENEFICIARYTPT', 'SSBENEFICIARYTPT', 'READ', 0),
('SSBENEFICIARYTPT', 'CREATE_SSBENEFICIARYTPT', 'SSBENEFICIARYTPT', 'CREATE', 0),
('SSBENEFICIARYTPT', 'UPDATE_SSBENEFICIARYTPT', 'SSBENEFICIARYTPT', 'UPDATE', 0),
('SSBENEFICIARYTPT', 'DELETE_SSBENEFICIARYTPT', 'SSBENEFICIARYTPT', 'DELETE', 0);
