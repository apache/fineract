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

CREATE TABLE `m_account_transfer_details` (
	`id` BIGINT(20) NOT NULL AUTO_INCREMENT,
	`from_office_id` BIGINT(20) NOT NULL,
	`to_office_id` BIGINT(20) NOT NULL,
	`from_client_id` BIGINT(20) NULL DEFAULT NULL,
	`to_client_id` BIGINT(20) NULL DEFAULT NULL,
	`from_savings_account_id` BIGINT(20) NULL DEFAULT NULL,
	`to_savings_account_id` BIGINT(20) NULL DEFAULT NULL,
	`from_loan_account_id` BIGINT(20) NULL DEFAULT NULL,
	`to_loan_account_id` BIGINT(20) NULL DEFAULT NULL,
	`transfer_type` SMALLINT(2) NULL DEFAULT NULL,
	`from_savings_transaction_id` BIGINT(20) NULL DEFAULT NULL,
	`from_loan_transaction_id` BIGINT(20) NULL DEFAULT NULL,
	`to_savings_transaction_id` BIGINT(20) NULL DEFAULT NULL,
	`to_loan_transaction_id` BIGINT(20) NULL DEFAULT NULL,
	`is_reversed` TINYINT(1) NOT NULL,
	`transaction_date` DATE NOT NULL,
	`currency_code` VARCHAR(3) NOT NULL,
	`currency_digits` SMALLINT(5) NOT NULL,
	`currency_multiplesof` SMALLINT(5) NULL DEFAULT NULL,
	`amount` DECIMAL(19,6) NOT NULL,
	`description` VARCHAR(200) NOT NULL,
	PRIMARY KEY (`id`),
	INDEX `FK_m_account_transfer_details_from_office` (`from_office_id`),
	INDEX `FK_m_account_transfer_details_to_office` (`to_office_id`),
	INDEX `FK_m_account_transfer_details_from_client` (`from_client_id`),
	INDEX `FK_m_account_transfer_details_to_client` (`to_client_id`),
	INDEX `FK_m_account_transfer_details_from_savings_account` (`from_savings_account_id`),
	INDEX `FK_m_account_transfer_details_to_savings_account` (`to_savings_account_id`),
	INDEX `FK_m_account_transfer_details_from_loan_account` (`from_loan_account_id`),
	INDEX `FK_m_account_transfer_details_to_loan_account` (`to_loan_account_id`),
	CONSTRAINT `FK_m_account_transfer_details_from_client` FOREIGN KEY (`from_client_id`) REFERENCES `m_client` (`id`),
	CONSTRAINT `FK_m_account_transfer_details_from_loan_account` FOREIGN KEY (`from_loan_account_id`) REFERENCES `m_loan` (`id`),
	CONSTRAINT `FK_m_account_transfer_details_from_office` FOREIGN KEY (`from_office_id`) REFERENCES `m_office` (`id`),
	CONSTRAINT `FK_m_account_transfer_details_from_savings_account` FOREIGN KEY (`from_savings_account_id`) REFERENCES `m_savings_account` (`id`),
	CONSTRAINT `FK_m_account_transfer_details_to_client` FOREIGN KEY (`to_client_id`) REFERENCES `m_client` (`id`),
	CONSTRAINT `FK_m_account_transfer_details_to_loan_account` FOREIGN KEY (`to_loan_account_id`) REFERENCES `m_loan` (`id`),
	CONSTRAINT `FK_m_account_transfer_details_to_office` FOREIGN KEY (`to_office_id`) REFERENCES `m_office` (`id`),
	CONSTRAINT `FK_m_account_transfer_details_to_savings_account` FOREIGN KEY (`to_savings_account_id`) REFERENCES `m_savings_account` (`id`)
)
COLLATE='utf8_general_ci'
ENGINE=InnoDB;

CREATE TABLE `m_account_transfer_standing_instructions` (
	`id` BIGINT(20) NOT NULL AUTO_INCREMENT,
	`name` VARCHAR(250) NOT NULL,
	`account_transfer_details_id` BIGINT(20) NOT NULL,
	`priority` TINYINT(2) NOT NULL,
	`status` TINYINT(2) NOT NULL,
	`instruction_type` TINYINT(2) NOT NULL,
	`amount` DECIMAL(19,6) NULL DEFAULT NULL,
	`valid_from` DATE NOT NULL,
	`valid_till` DATE NULL DEFAULT NULL,
	`recurrence_type` TINYINT(1) NOT NULL,
	`recurrence_frequency` SMALLINT(5) NULL DEFAULT NULL,
	`recurrence_interval` SMALLINT(5) NULL DEFAULT NULL,
	`recurrence_on_day` SMALLINT(2) NULL DEFAULT NULL,
	`recurrence_on_month` SMALLINT(2) NULL DEFAULT NULL,
	`last_run_date` DATE NULL DEFAULT NULL,
	PRIMARY KEY (`id`),
	UNIQUE INDEX `name` (`name`),
	INDEX `FK_m_standing_instructions_account_transfer_details` (`account_transfer_details_id`),
	CONSTRAINT `FK_m_standing_instructions_account_transfer_details` FOREIGN KEY (`account_transfer_details_id`) REFERENCES `m_account_transfer_details` (`id`)
)
COLLATE='utf8_general_ci'
ENGINE=InnoDB;


CREATE TABLE `m_account_transfer_transaction` (
	`id` BIGINT(20) NOT NULL AUTO_INCREMENT,
	`account_transfer_details_id` BIGINT(20) NOT NULL,
	`from_savings_transaction_id` BIGINT(20) NULL DEFAULT NULL,
	`from_loan_transaction_id` BIGINT(20) NULL DEFAULT NULL,
	`to_savings_transaction_id` BIGINT(20) NULL DEFAULT NULL,
	`to_loan_transaction_id` BIGINT(20) NULL DEFAULT NULL,
	`is_reversed` TINYINT(1) NOT NULL,
	`transaction_date` DATE NOT NULL,
	`currency_code` VARCHAR(3) NOT NULL,
	`currency_digits` SMALLINT(5) NOT NULL,
	`currency_multiplesof` SMALLINT(5) NULL DEFAULT NULL,
	`amount` DECIMAL(19,6) NOT NULL,
	`description` VARCHAR(200) NOT NULL,
	PRIMARY KEY (`id`),
	INDEX `FK_m_account_transfer_transaction_from_m_savings_transaction` (`from_savings_transaction_id`),
	INDEX `FK_m_account_transfer_transaction_to_m_savings_transaction` (`to_savings_transaction_id`),
	INDEX `FK_m_account_transfer_transaction_to_m_loan_transaction` (`to_loan_transaction_id`),
	INDEX `FK_m_account_transfer_transaction_from_m_loan_transaction` (`from_loan_transaction_id`),
	INDEX `FK_m_account_transfer_transaction_account_detail` (`account_transfer_details_id`),
	CONSTRAINT `FK_m_account_transfer_transaction_account_detail` FOREIGN KEY (`account_transfer_details_id`) REFERENCES `m_account_transfer_details` (`id`),
	CONSTRAINT `FK_m_account_transfer_transaction_from_m_loan_transaction` FOREIGN KEY (`from_loan_transaction_id`) REFERENCES `m_loan_transaction` (`id`),
	CONSTRAINT `FK_m_account_transfer_transaction_from_m_savings_transaction` FOREIGN KEY (`from_savings_transaction_id`) REFERENCES `m_savings_account_transaction` (`id`),
	CONSTRAINT `FK_m_account_transfer_transaction_to_m_loan_transaction` FOREIGN KEY (`to_loan_transaction_id`) REFERENCES `m_loan_transaction` (`id`),
	CONSTRAINT `FK_m_account_transfer_transaction_to_m_savings_transaction` FOREIGN KEY (`to_savings_transaction_id`) REFERENCES `m_savings_account_transaction` (`id`)
)
COLLATE='utf8_general_ci'
ENGINE=InnoDB;



INSERT INTO `m_account_transfer_details` (`from_office_id`, `to_office_id`, `from_client_id`, `to_client_id`, `from_savings_account_id`, `to_savings_account_id`, `from_loan_account_id`, `to_loan_account_id`, `from_savings_transaction_id`, `from_loan_transaction_id`, `to_savings_transaction_id`, `to_loan_transaction_id`, `is_reversed`, `transaction_date`, `currency_code`, `currency_digits`, `currency_multiplesof`, `amount`, `description`) select at.from_office_id,at.to_office_id,at.from_client_id,at.to_client_id,at.from_savings_account_id,at.to_savings_account_id,at.from_loan_account_id,at.to_loan_account_id,at.from_savings_transaction_id,at.from_loan_transaction_id,at.to_savings_transaction_id,at.to_loan_transaction_id,at.is_reversed,at.transaction_date,at.currency_code,at.currency_digits,at.currency_multiplesof,at.amount,at.description from m_savings_account_transfer at;

INSERT INTO `m_account_transfer_transaction` (`account_transfer_details_id`, `from_savings_transaction_id`, `from_loan_transaction_id`, `to_savings_transaction_id`, `to_loan_transaction_id`, `is_reversed`, `transaction_date`, `currency_code`, `currency_digits`, `currency_multiplesof`, `amount`, `description`) select ad.id,ad.from_savings_transaction_id,ad.from_loan_transaction_id,ad.to_savings_transaction_id,ad.to_loan_transaction_id,ad.is_reversed,ad.transaction_date,ad.currency_code,ad.currency_digits,ad.currency_multiplesof,ad.amount,ad.description from m_account_transfer_details ad;

ALTER TABLE `m_account_transfer_details`
	DROP COLUMN `from_savings_transaction_id`,
	DROP COLUMN `from_loan_transaction_id`,
	DROP COLUMN `to_savings_transaction_id`,
	DROP COLUMN `to_loan_transaction_id`,
	DROP COLUMN `is_reversed`,
	DROP COLUMN `transaction_date`,
	DROP COLUMN `currency_code`,
	DROP COLUMN `currency_digits`,
	DROP COLUMN `currency_multiplesof`,
	DROP COLUMN `amount`,
	DROP COLUMN `description`;

DROP TABLE `m_savings_account_transfer`;

INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ('account_transfer', 'READ_STANDINGINSTRUCTION ', 'STANDINGINSTRUCTION ', 'READ', 0);
INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ('account_transfer', 'CREATE_STANDINGINSTRUCTION ', 'STANDINGINSTRUCTION ', 'CREATE', 0);
INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ('account_transfer', 'UPDATE_STANDINGINSTRUCTION ', 'STANDINGINSTRUCTION ', 'UPDATE', 0);
INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ('account_transfer', 'DELETE_STANDINGINSTRUCTION ', 'STANDINGINSTRUCTION ', 'DELETE', 0);


INSERT INTO `job` (`name`, `display_name`, `cron_expression`, `create_time`, `task_priority`, `group_name`, `previous_run_start_time`, `next_run_time`, `job_key`, `initializing_errorlog`, `is_active`, `currently_running`, `updates_allowed`, `scheduler_group`, `is_misfired`) VALUES ('Execute Standing Instruction', 'Execute Standing Instruction', '0 0 0 1/1 * ? *', now(), 5, NULL, NULL, NULL, NULL, NULL, 1, 0, 1, 0, 0);
