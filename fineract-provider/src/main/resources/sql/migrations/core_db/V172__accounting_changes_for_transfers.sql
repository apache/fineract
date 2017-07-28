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

INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ('accounting', 'READ_OFFICEGLACCOUNT', 'OFFICEGLACCOUNT', 'READ', 0);

INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ('accounting', 'CREATE_OFFICEGLACCOUNT', 'OFFICEGLACCOUNT', 'CREATE', 0);

INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ('accounting', 'DELETE_OFFICEGLACCOUNT', 'OFFICEGLACCOUNT', 'DELETE', 0);

INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ('accounting', 'UPDATE_OFFICEGLACCOUNT', 'OFFICEGLACCOUNT', 'UPDATE', 0);

CREATE TABLE `acc_gl_office_mapping` (
	`id` BIGINT(20) NOT NULL AUTO_INCREMENT,
	`gl_account_id` BIGINT(20) NOT NULL DEFAULT '0',
	`office_id` BIGINT(20) NOT NULL,
	`financial_account_type` SMALLINT(5) NOT NULL,
	PRIMARY KEY (`id`),
	INDEX `FK_office_mapping_acc_gl_account` (`gl_account_id`),
	INDEX `FK_office_mapping_office` (`office_id`),
	CONSTRAINT `FK_office_mapping_acc_gl_account` FOREIGN KEY (`gl_account_id`) REFERENCES `acc_gl_account` (`id`),
	CONSTRAINT `FK_office_mapping_office` FOREIGN KEY (`office_id`) REFERENCES `m_office` (`id`)
)
COLLATE='utf8_general_ci'
ENGINE=InnoDB;
