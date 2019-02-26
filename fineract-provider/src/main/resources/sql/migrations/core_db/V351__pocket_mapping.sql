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

CREATE TABLE `m_pocket` (
	`id` BIGINT(20) NOT NULL AUTO_INCREMENT,
	`app_user_id` BIGINT(20) NOT NULL,
	PRIMARY KEY (`id`),
	UNIQUE INDEX `app_user_id` (`app_user_id`),
	CONSTRAINT `FK__m_appuser__pocket` FOREIGN KEY (`app_user_id`) REFERENCES `m_appuser` (`id`)
);

CREATE TABLE `m_pocket_accounts_mapping` (
	`id` BIGINT(20) NOT NULL AUTO_INCREMENT,
	`pocket_id` BIGINT(20) NOT NULL,
	`account_id` BIGINT(20) NOT NULL,
	`account_type` INT(5) NOT NULL,
	`account_number` VARCHAR(20) NOT NULL,
	PRIMARY KEY (`id`),
	UNIQUE INDEX `unique_pocket_mapping` (`pocket_id`, `account_id`, `account_type`),
	CONSTRAINT `FK__m_pocket` FOREIGN KEY (`pocket_id`) REFERENCES `m_pocket` (`id`)
);

INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ('portfolio', 'LINK_ACCOUNT_TO_POCKET', 'POCKET', 'LINK_ACCOUNT_TO', 0),
('portfolio', 'DELINK_ACCOUNT_FROM_POCKET', 'POCKET', 'DELINK_ACCOUNT_FROM', 0);
