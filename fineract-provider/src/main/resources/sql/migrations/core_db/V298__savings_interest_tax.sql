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

CREATE TABLE `m_tax_component` (
	`id` BIGINT(20) NOT NULL AUTO_INCREMENT,
	`name` VARCHAR(50) NOT NULL,
	`percentage` DECIMAL(19,6) NOT NULL,
	`debit_account_type_enum` SMALLINT(2) NULL DEFAULT NULL,
	`debit_account_id` BIGINT(20) NULL DEFAULT NULL,
	`credit_account_type_enum` SMALLINT(2) NULL DEFAULT NULL,
	`credit_account_id` BIGINT(20) NULL DEFAULT NULL,
	`start_date` DATE NOT NULL,
	`createdby_id` BIGINT(20) NOT NULL,
	`created_date` DATETIME NOT NULL,
	`lastmodifiedby_id` BIGINT(20) NOT NULL,
	`lastmodified_date` DATETIME NOT NULL,
	PRIMARY KEY (`id`),
	INDEX `FK_tax_component_debit_gl_account` (`debit_account_id`),
	INDEX `FK_tax_component_credit_gl_account` (`credit_account_id`),
	INDEX `FK_tax_component_createdby` (`createdby_id`),
	INDEX `FK_tax_component_lastmodifiedby` (`lastmodifiedby_id`),
	CONSTRAINT `FK_tax_component_createdby` FOREIGN KEY (`createdby_id`) REFERENCES `m_appuser` (`id`),
	CONSTRAINT `FK_tax_component_credit_gl_account` FOREIGN KEY (`credit_account_id`) REFERENCES `acc_gl_account` (`id`),
	CONSTRAINT `FK_tax_component_debit_gl_account` FOREIGN KEY (`debit_account_id`) REFERENCES `acc_gl_account` (`id`),
	CONSTRAINT `FK_tax_component_lastmodifiedby` FOREIGN KEY (`lastmodifiedby_id`) REFERENCES `m_appuser` (`id`)
);

CREATE TABLE `m_tax_component_history` (
	`id` BIGINT(20) NOT NULL AUTO_INCREMENT,
	`tax_component_id` BIGINT(20) NOT NULL,
	`percentage` DECIMAL(19,6) NOT NULL,
	`start_date` DATE NOT NULL,
	`end_date` DATE NOT NULL,
	`createdby_id` BIGINT(20) NOT NULL,
	`created_date` DATETIME NOT NULL,
	`lastmodifiedby_id` BIGINT(20) NOT NULL,
	`lastmodified_date` DATETIME NOT NULL,
	PRIMARY KEY (`id`),
	INDEX `FK_tax_component_history_tax_component_id` (`tax_component_id`),
	INDEX `FK_tax_component_history_createdby` (`createdby_id`),
	INDEX `FK_tax_component_history_lastmodifiedby` (`lastmodifiedby_id`),
	CONSTRAINT `FK_tax_component_history_createdby` FOREIGN KEY (`createdby_id`) REFERENCES `m_appuser` (`id`),
	CONSTRAINT `FK_tax_component_history_lastmodifiedby` FOREIGN KEY (`lastmodifiedby_id`) REFERENCES `m_appuser` (`id`),
	CONSTRAINT `FK_tax_component_history_tax_component_id` FOREIGN KEY (`tax_component_id`) REFERENCES `m_tax_component` (`id`)
);

CREATE TABLE `m_tax_group` (
	`id` BIGINT(20) NOT NULL AUTO_INCREMENT,
	`name` VARCHAR(50) NOT NULL,
	`createdby_id` BIGINT(20) NOT NULL,
	`created_date` DATETIME NOT NULL,
	`lastmodifiedby_id` BIGINT(20) NOT NULL,
	`lastmodified_date` DATETIME NOT NULL,
	PRIMARY KEY (`id`),
	INDEX `FK_tax_group_createdby` (`createdby_id`),
	INDEX `FK_tax_group_lastmodifiedby` (`lastmodifiedby_id`),
	CONSTRAINT `FK_tax_group_createdby` FOREIGN KEY (`createdby_id`) REFERENCES `m_appuser` (`id`),
	CONSTRAINT `FK_tax_group_lastmodifiedby` FOREIGN KEY (`lastmodifiedby_id`) REFERENCES `m_appuser` (`id`)	
);

CREATE TABLE `m_tax_group_mappings` (
	`id` BIGINT(20) NOT NULL AUTO_INCREMENT,
	`tax_group_id` BIGINT(20) NOT NULL,
	`tax_component_id` BIGINT(20) NOT NULL,
	`start_date` DATE NOT NULL,
	`end_date` DATE NULL DEFAULT NULL,
	`createdby_id` BIGINT(20) NOT NULL,
	`created_date` DATETIME NOT NULL,
	`lastmodifiedby_id` BIGINT(20) NOT NULL,
	`lastmodified_date` DATETIME NOT NULL,
	PRIMARY KEY (`id`),
	INDEX `FK_tax_group_mappings_tax_group` (`tax_group_id`),
	INDEX `FK_tax_group_mappings_tax_component` (`tax_component_id`),
	INDEX `FK_tax_group_mappings_createdby` (`createdby_id`),
	INDEX `FK_tax_group_mappings_lastmodifiedby` (`lastmodifiedby_id`),
	CONSTRAINT `FK_tax_group_mappings_createdby` FOREIGN KEY (`createdby_id`) REFERENCES `m_appuser` (`id`),
	CONSTRAINT `FK_tax_group_mappings_lastmodifiedby` FOREIGN KEY (`lastmodifiedby_id`) REFERENCES `m_appuser` (`id`),
	CONSTRAINT `FK_tax_group_mappings_tax_component` FOREIGN KEY (`tax_component_id`) REFERENCES `m_tax_component` (`id`),
	CONSTRAINT `FK_tax_group_mappings_tax_group` FOREIGN KEY (`tax_group_id`) REFERENCES `m_tax_group` (`id`)
);

ALTER TABLE `m_charge`
	ADD COLUMN `tax_group_id` BIGINT(20) NULL DEFAULT NULL,
	ADD CONSTRAINT `FK_m_charge_m_tax_group` FOREIGN KEY (`tax_group_id`) REFERENCES `m_tax_group` (`id`);
	
ALTER TABLE `m_savings_product`
	ADD COLUMN `withhold_tax` TINYINT NOT NULL DEFAULT '0',
	ADD COLUMN `tax_group_id` BIGINT NULL DEFAULT NULL,
	ADD CONSTRAINT `FK_savings_product_tax_group` FOREIGN KEY (`tax_group_id`) REFERENCES `m_tax_group` (`id`);
	
ALTER TABLE `m_savings_account`
	ADD COLUMN `withhold_tax` TINYINT NOT NULL DEFAULT '0',
	ADD COLUMN `tax_group_id` BIGINT NULL DEFAULT NULL,
	ADD COLUMN `total_withhold_tax_derived` DECIMAL(19,6) NULL DEFAULT NULL AFTER `total_overdraft_interest_derived`,
	ADD CONSTRAINT `FK_savings_account_tax_group` FOREIGN KEY (`tax_group_id`) REFERENCES `m_tax_group` (`id`);	
	
CREATE TABLE `m_savings_account_transaction_tax_details` (
	`id` BIGINT(20) NOT NULL AUTO_INCREMENT,
	`savings_transaction_id` BIGINT(20) NOT NULL,
	`tax_component_id` BIGINT(20) NOT NULL,
	`amount` DECIMAL(19,6) NOT NULL,
	PRIMARY KEY (`id`),
	CONSTRAINT `FK_savings_account_transaction_tax_details_savings_transaction` FOREIGN KEY (`savings_transaction_id`) REFERENCES `m_savings_account_transaction` (`id`),
	CONSTRAINT `FK_savings_account_transaction_tax_details_tax_component` FOREIGN KEY (`tax_component_id`) REFERENCES `m_tax_component` (`id`)
);

	

INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ('organisation', 'READ_TAXCOMPONENT', 'TAXCOMPONENT', 'READ', 0),('organisation', 'CREATE_TAXCOMPONENT', 'TAXCOMPONENT', 'CREATE', 0),('organisation', 'CREATE_TAXCOMPONENT_CHECKER', 'TAXCOMPONENT', 'CREATE_CHECKER', 0), ('organisation', 'UPDATE_TAXCOMPONENT', 'TAXCOMPONENT', 'UPDATE', 0),('organisation', 'UPDATE_TAXCOMPONENT_CHECKER', 'TAXCOMPONENT', 'UPDATE_CHECKER', 0),('organisation', 'READ_TAXGROUP', 'TAXGROUP', 'READ', 0),('organisation', 'CREATE_TAXGROUP', 'TAXGROUP', 'CREATE', 0),('organisation', 'CREATE_TAXGROUP_CHECKER', 'TAXGROUP', 'CREATE_CHECKER', 0), ('organisation', 'UPDATE_TAXGROUP', 'TAXGROUP', 'UPDATE', 0),('organisation', 'UPDATE_TAXGROUP_CHECKER', 'TAXGROUP', 'UPDATE_CHECKER', 0),('portfolio', 'UPDATEWITHHOLDTAX_SAVINGSACCOUNT', 'SAVINGSACCOUNT', 'UPDATEWITHHOLDTAX', 0),('portfolio', 'UPDATEWITHHOLDTAX_SAVINGSACCOUNT_CHECKER', 'SAVINGSACCOUNT', 'UPDATEWITHHOLDTAX_CHECKER', 0);





