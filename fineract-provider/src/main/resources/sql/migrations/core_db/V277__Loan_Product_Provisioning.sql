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

CREATE TABLE `m_provision_category` (
	`id` BIGINT(20) NOT NULL AUTO_INCREMENT,
	`category_name` VARCHAR(100) NOT NULL,
	`description` VARCHAR(300) NULL,
	PRIMARY KEY (`id`),
	UNIQUE INDEX `category_name` (`category_name`)
)
COLLATE='utf8_general_ci'
ENGINE=InnoDB
AUTO_INCREMENT=1
;

INSERT INTO `m_provision_category` (`category_name`, `description`) VALUES ('STANDARD', 'Punctual Payment without any dues');
INSERT INTO `m_provision_category` (`category_name`, `description`) VALUES ('SUB-STANDARD', 'Principal and/or Interest overdue by x days');
INSERT INTO `m_provision_category` (`category_name`, `description`) VALUES ('DOUBTFUL', 'Principal and/or Interest overdue by x days and less than y');
INSERT INTO `m_provision_category` (`category_name`, `description`) VALUES ('LOSS', 'Principal and/or Interest overdue by y days');

CREATE TABLE `m_provisioning_criteria` (
	`id` BIGINT(20) NOT NULL AUTO_INCREMENT,
	`criteria_name` VARCHAR(200) NOT NULL,
	`createdby_id` BIGINT(20) NULL DEFAULT NULL,
	`created_date` DATETIME NULL DEFAULT NULL,
	`lastmodifiedby_id` BIGINT(20) NULL DEFAULT NULL,
	`lastmodified_date` DATETIME NULL DEFAULT NULL,
	PRIMARY KEY (`id`),
	UNIQUE INDEX `criteria_name` (`criteria_name`),
	FOREIGN KEY (`createdby_id`) REFERENCES `m_appuser` (`id`),
	FOREIGN KEY (`lastmodifiedby_id`) REFERENCES `m_appuser` (`id`)
);

CREATE TABLE `m_provisioning_criteria_definition` (
	`id` BIGINT(20) NOT NULL AUTO_INCREMENT,
	`criteria_id` BIGINT(20) NOT NULL,
	`category_id` BIGINT(20) NOT NULL,
	`min_age` BIGINT(20) NOT NULL,
	`max_age` BIGINT(20) NOT NULL,
	`provision_percentage` DECIMAL(5,2) NOT NULL,
	`liability_account` BIGINT(20),
	`expense_account` BIGINT(20),
	PRIMARY KEY (`id`),
	FOREIGN KEY (`criteria_id`) REFERENCES `m_provisioning_criteria` (`id`),
	FOREIGN KEY (`category_id`) REFERENCES `m_provision_category` (`id`),
	FOREIGN KEY (`liability_account`) REFERENCES `acc_gl_account` (`id`),
	FOREIGN KEY (`expense_account`) REFERENCES `acc_gl_account` (`id`)
);

CREATE TABLE `m_loanproduct_provisioning_mapping` (
	`id` BIGINT(20) NOT NULL AUTO_INCREMENT,
	`product_id` BIGINT(20) NOT NULL,
	`criteria_id` BIGINT(20) NOT NULL,
	PRIMARY KEY (`id`),
	UNIQUE INDEX `product_id` (`product_id`),
	FOREIGN KEY (`product_id`) REFERENCES `m_product_loan` (`id`),
	FOREIGN KEY (`criteria_id`) REFERENCES `m_provisioning_criteria` (`id`)
);

CREATE TABLE `m_provisioning_history` (
	`id` BIGINT(20) NOT NULL AUTO_INCREMENT,
	`journal_entry_created` BIT(1) DEFAULT 0,
	`createdby_id` BIGINT(20) NULL DEFAULT NULL,
	`created_date` DATE NULL DEFAULT NULL,
	`lastmodifiedby_id` BIGINT(20) NULL DEFAULT NULL,
	`lastmodified_date` DATE NULL DEFAULT NULL,
	PRIMARY KEY (`id`),
	FOREIGN KEY (`createdby_id`) REFERENCES `m_appuser` (`id`),
	FOREIGN KEY (`lastmodifiedby_id`) REFERENCES `m_appuser` (`id`)
);

CREATE TABLE `m_loanproduct_provisioning_entry` (
	`id` BIGINT(20) NOT NULL AUTO_INCREMENT,
	`history_id` BIGINT(20) NOT NULL,
	`criteria_id` BIGINT(20) NOT NULL,
	`currency_code` VARCHAR(3) NOT NULL,
	`office_id` BIGINT(20) NOT NULL,
	`product_id` BIGINT(20) NOT NULL,
	`category_id` BIGINT(20) NOT NULL,
	`overdue_in_days` BIGINT(20) DEFAULT 0,
	`reseve_amount` DECIMAL(20,6) DEFAULT 0,
	`liability_account` BIGINT(20) NULL DEFAULT NULL,
	`expense_account` BIGINT(20) NULL DEFAULT NULL,
	PRIMARY KEY (`id`),
	FOREIGN KEY (`history_id`) REFERENCES `m_provisioning_history` (`id`),
	FOREIGN KEY (`criteria_id`) REFERENCES `m_provisioning_criteria` (`id`),
	FOREIGN KEY (`office_id`) REFERENCES `m_office` (`id`),
	FOREIGN KEY (`product_id`) REFERENCES `m_product_loan` (`id`),
	FOREIGN KEY (`category_id`) REFERENCES `m_provision_category` (`id`),
	FOREIGN KEY (`liability_account`) REFERENCES `acc_gl_account` (`id`),
	FOREIGN KEY (`expense_account`) REFERENCES `acc_gl_account` (`id`)
);

INSERT INTO `job` (`name`, `display_name`, `cron_expression`, `create_time`, `task_priority`, `group_name`, `previous_run_start_time`, `next_run_time`, `job_key`, `initializing_errorlog`, `is_active`, `currently_running`, `updates_allowed`, `scheduler_group`, `is_misfired`) VALUES ('Generate Loan Loss Provisioning', 'Generate Loan Loss Provisioning', '0 0 0 1/1 * ? *', now(), 5, NULL, NULL, NULL, NULL, NULL, 1, 0, 1, 0, 0);

INSERT INTO `m_permission`(`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES
('LOAN_PROVISIONING', 'CREATE_PROVISIONCATEGORY', 'PROVISIONCATEGORY', 'CREATE', 0),
('LOAN_PROVISIONING', 'DELETE_PROVISIONCATEGORY', 'PROVISIONCATEGORY', 'DELETE', 0),
('LOAN_PROVISIONING', 'CREATE_PROVISIONCRITERIA', 'PROVISIONINGCRITERIA', 'CREATE', 0),
('LOAN_PROVISIONING', 'UPDATE_PROVISIONCRITERIA', 'PROVISIONINGCRITERIA', 'UPDATE', 0),
('LOAN_PROVISIONING', 'DELETE_PROVISIONCRITERIA', 'PROVISIONINGCRITERIA', 'DELETE', 0),
('LOAN_PROVISIONING', 'CREATE_PROVISIONENTRIES', 'PROVISIONINGENTRIES', 'CREATE', 0),
('LOAN_PROVISIONING', 'CREATE_PROVISIONJOURNALENTRIES', 'PROVISIONINGENTRIES', 'CREATE', 0),
('LOAN_PROVISIONING', 'RECREATE_PROVISIONENTRIES', 'PROVISIONINGENTRIES', 'RECREATE', 0);

INSERT INTO `m_appuser` ( `is_deleted`, `office_id`, `staff_id`, `username`, `firstname`, `lastname`, `password`, `email`, `firsttime_login_remaining`, `nonexpired`, `nonlocked`, `nonexpired_credentials`, `enabled`, `last_time_password_updated`, `password_never_expires`) VALUES
	(0, 1, NULL, 'system', 'system', 'system', '5787039480429368bf94732aacc771cd0a3ea02bcf504ffe1185ab94213bc63a', 'demomfi@mifos.org', b'0', b'1', b'1', b'1', b'1', '2014-03-07', 0);

