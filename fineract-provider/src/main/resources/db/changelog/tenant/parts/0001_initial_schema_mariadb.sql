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

SET FOREIGN_KEY_CHECKS=0;

CREATE TABLE `acc_accounting_rule` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(100) DEFAULT NULL,
  `office_id` bigint(20) DEFAULT NULL,
  `debit_account_id` bigint(20) DEFAULT NULL,
  `allow_multiple_debits` tinyint(1) NOT NULL DEFAULT 0,
  `credit_account_id` bigint(20) DEFAULT NULL,
  `allow_multiple_credits` tinyint(1) NOT NULL DEFAULT 0,
  `description` varchar(500) DEFAULT NULL,
  `system_defined` tinyint(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`),
  KEY `FK_acc_accounting_rule_acc_gl_account_credit` (`credit_account_id`),
  KEY `FK_acc_accounting_rule_acc_gl_account_debit` (`debit_account_id`),
  KEY `FK_acc_accounting_rule_m_office` (`office_id`),
  CONSTRAINT `FK_acc_accounting_rule_acc_gl_account_credit` FOREIGN KEY (`credit_account_id`) REFERENCES `acc_gl_account` (`id`),
  CONSTRAINT `FK_acc_accounting_rule_acc_gl_account_debit` FOREIGN KEY (`debit_account_id`) REFERENCES `acc_gl_account` (`id`),
  CONSTRAINT `FK_acc_accounting_rule_m_office` FOREIGN KEY (`office_id`) REFERENCES `m_office` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `acc_gl_account` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(200) NOT NULL,
  `parent_id` bigint(20) DEFAULT NULL,
  `hierarchy` varchar(50) DEFAULT NULL,
  `gl_code` varchar(45) NOT NULL,
  `disabled` tinyint(1) NOT NULL DEFAULT 0,
  `manual_journal_entries_allowed` tinyint(1) NOT NULL DEFAULT 1,
  `account_usage` tinyint(4) NOT NULL DEFAULT 2,
  `classification_enum` smallint(6) NOT NULL,
  `tag_id` int(11) DEFAULT NULL,
  `description` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `gl_code` (`gl_code`),
  KEY `FKGLACC000000002` (`tag_id`),
  KEY `FK_ACC_0000000001` (`parent_id`),
  CONSTRAINT `FKGLACC000000002` FOREIGN KEY (`tag_id`) REFERENCES `m_code_value` (`id`),
  CONSTRAINT `FK_ACC_0000000001` FOREIGN KEY (`parent_id`) REFERENCES `acc_gl_account` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `acc_gl_closure` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `office_id` bigint(20) NOT NULL,
  `closing_date` date NOT NULL,
  `is_deleted` tinyint(1) NOT NULL DEFAULT 0,
  `createdby_id` bigint(20) DEFAULT NULL,
  `lastmodifiedby_id` bigint(20) DEFAULT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `lastmodified_date` datetime(6) DEFAULT NULL,
  `comments` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `office_id_closing_date` (`office_id`,`closing_date`),
  KEY `FK_acc_gl_closure_m_appuser` (`createdby_id`),
  KEY `FK_acc_gl_closure_m_appuser_2` (`lastmodifiedby_id`),
  KEY `FK_acc_gl_closure_m_office` (`office_id`),
  CONSTRAINT `FK_acc_gl_closure_m_appuser` FOREIGN KEY (`createdby_id`) REFERENCES `m_appuser` (`id`),
  CONSTRAINT `FK_acc_gl_closure_m_appuser_2` FOREIGN KEY (`lastmodifiedby_id`) REFERENCES `m_appuser` (`id`),
  CONSTRAINT `FK_acc_gl_closure_m_office` FOREIGN KEY (`office_id`) REFERENCES `m_office` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `acc_gl_financial_activity_account` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `gl_account_id` bigint(20) NOT NULL DEFAULT 0,
  `financial_activity_type` smallint(6) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `financial_activity_type` (`financial_activity_type`),
  KEY `FK_office_mapping_acc_gl_account` (`gl_account_id`),
  CONSTRAINT `FK_office_mapping_acc_gl_account` FOREIGN KEY (`gl_account_id`) REFERENCES `acc_gl_account` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `acc_gl_journal_entry` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `account_id` bigint(20) NOT NULL,
  `office_id` bigint(20) NOT NULL,
  `reversal_id` bigint(20) DEFAULT NULL,
  `currency_code` varchar(3) NOT NULL,
  `transaction_id` varchar(50) NOT NULL,
  `loan_transaction_id` bigint(20) DEFAULT NULL,
  `savings_transaction_id` bigint(20) DEFAULT NULL,
  `client_transaction_id` bigint(20) DEFAULT NULL,
  `reversed` tinyint(1) NOT NULL DEFAULT 0,
  `ref_num` varchar(100) DEFAULT NULL,
  `manual_entry` tinyint(1) NOT NULL DEFAULT 0,
  `entry_date` date NOT NULL,
  `type_enum` smallint(6) NOT NULL,
  `amount` decimal(19,6) NOT NULL,
  `description` varchar(500) DEFAULT NULL,
  `entity_type_enum` smallint(6) DEFAULT NULL,
  `entity_id` bigint(20) DEFAULT NULL,
  `created_by` bigint(20) DEFAULT NULL,
  `last_modified_by` bigint(20) DEFAULT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `lastmodified_date` datetime(6) DEFAULT NULL,
  `is_running_balance_calculated` tinyint(1) NOT NULL DEFAULT 0,
  `office_running_balance` decimal(19,6) NOT NULL DEFAULT 0.000000,
  `organization_running_balance` decimal(19,6) NOT NULL DEFAULT 0.000000,
  `payment_details_id` bigint(20) DEFAULT NULL,
  `share_transaction_id` bigint(20) DEFAULT NULL,
  `transaction_date` date DEFAULT NULL COMMENT 'Unfinished. Not maintained.',
  `created_on_utc` datetime(6) DEFAULT NULL,
  `last_modified_on_utc` datetime(6) DEFAULT NULL,
  `submitted_on_date` date NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_acc_gl_journal_entry_acc_gl_account` (`account_id`),
  KEY `FK_acc_gl_journal_entry_acc_gl_journal_entry` (`reversal_id`),
  KEY `FK_acc_gl_journal_entry_m_appuser` (`created_by`),
  KEY `FK_acc_gl_journal_entry_m_appuser_2` (`last_modified_by`),
  KEY `FK_acc_gl_journal_entry_m_client_transaction` (`client_transaction_id`),
  KEY `FK_acc_gl_journal_entry_m_loan_transaction` (`loan_transaction_id`),
  KEY `FK_acc_gl_journal_entry_m_office` (`office_id`),
  KEY `FK_acc_gl_journal_entry_m_payment_detail` (`payment_details_id`),
  KEY `FK_acc_gl_journal_entry_m_savings_account_transaction` (`savings_transaction_id`),
  KEY `FK_acc_gl_journal_entry_m_share_account_transaction` (`share_transaction_id`),
  KEY `transaction_date_index` (`transaction_date`),
  KEY `FK_acc_gl_journal_entry_submitted_on_date` (`submitted_on_date`),
  KEY `idx_acc_gl_journal_entry_transaction_id` (`transaction_id`),
  CONSTRAINT `FK_acc_gl_journal_entry_acc_gl_account` FOREIGN KEY (`account_id`) REFERENCES `acc_gl_account` (`id`),
  CONSTRAINT `FK_acc_gl_journal_entry_acc_gl_journal_entry` FOREIGN KEY (`reversal_id`) REFERENCES `acc_gl_journal_entry` (`id`),
  CONSTRAINT `FK_acc_gl_journal_entry_m_appuser` FOREIGN KEY (`created_by`) REFERENCES `m_appuser` (`id`),
  CONSTRAINT `FK_acc_gl_journal_entry_m_appuser_2` FOREIGN KEY (`last_modified_by`) REFERENCES `m_appuser` (`id`),
  CONSTRAINT `FK_acc_gl_journal_entry_m_client_transaction` FOREIGN KEY (`client_transaction_id`) REFERENCES `m_client_transaction` (`id`),
  CONSTRAINT `FK_acc_gl_journal_entry_m_loan_transaction` FOREIGN KEY (`loan_transaction_id`) REFERENCES `m_loan_transaction` (`id`),
  CONSTRAINT `FK_acc_gl_journal_entry_m_office` FOREIGN KEY (`office_id`) REFERENCES `m_office` (`id`),
  CONSTRAINT `FK_acc_gl_journal_entry_m_payment_detail` FOREIGN KEY (`payment_details_id`) REFERENCES `m_payment_detail` (`id`),
  CONSTRAINT `FK_acc_gl_journal_entry_m_savings_account_transaction` FOREIGN KEY (`savings_transaction_id`) REFERENCES `m_savings_account_transaction` (`id`),
  CONSTRAINT `FK_acc_gl_journal_entry_m_share_account_transaction` FOREIGN KEY (`share_transaction_id`) REFERENCES `m_share_account_transactions` (`id`),
  CONSTRAINT `FK_journal_entry_created_by` FOREIGN KEY (`created_by`) REFERENCES `m_appuser` (`id`),
  CONSTRAINT `FK_journal_entry_last_modified_by` FOREIGN KEY (`last_modified_by`) REFERENCES `m_appuser` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `acc_gl_journal_entry_annual_summary` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `gl_code` varchar(45) NOT NULL,
  `product_id` bigint(20) NOT NULL,
  `office_id` bigint(20) NOT NULL,
  `opening_balance_amount` decimal(19,6) NOT NULL,
  `currency_code` varchar(3) NOT NULL,
  `owner_external_id` varchar(100) DEFAULT NULL,
  `manual_entry` tinyint(1) NOT NULL DEFAULT 0,
  `year_end_date` date NOT NULL,
  `created_by` bigint(20) DEFAULT NULL,
  `created_on_utc` datetime DEFAULT NULL,
  `last_modified_by` bigint(20) DEFAULT NULL,
  `last_modified_on_utc` datetime DEFAULT NULL,
  `originator_external_ids` varchar(1000) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_annual_report_created_by` (`created_by`),
  KEY `FK_annual_report_last_modified_by` (`last_modified_by`),
  KEY `acc_gl_journal_entry_annual_summary_idx_1` (`gl_code`,`product_id`),
  CONSTRAINT `FK_annual_report_created_by` FOREIGN KEY (`created_by`) REFERENCES `m_appuser` (`id`),
  CONSTRAINT `FK_annual_report_last_modified_by` FOREIGN KEY (`last_modified_by`) REFERENCES `m_appuser` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `acc_product_mapping` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `gl_account_id` bigint(20) DEFAULT NULL,
  `product_id` bigint(20) DEFAULT NULL,
  `product_type` smallint(6) DEFAULT NULL,
  `payment_type` int(11) DEFAULT NULL,
  `charge_id` bigint(20) DEFAULT NULL,
  `financial_account_type` smallint(6) DEFAULT NULL,
  `charge_off_reason_id` int(11) DEFAULT NULL,
  `capitalized_income_classification_id` int(11) DEFAULT NULL,
  `buydown_fee_classification_id` int(11) DEFAULT NULL,
  `write_off_reason_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_acc_product_mapping_m_charge` (`charge_id`),
  KEY `FK_acc_product_mapping_m_payment_type` (`payment_type`),
  KEY `fk_acc_product_mapping_charge_off_reason` (`charge_off_reason_id`),
  KEY `IND_acc_product_mapping_product_id` (`product_id`),
  KEY `FK_acc_product_mapping_gl_account_id` (`gl_account_id`),
  KEY `fk_acc_product_mapping_capitalized_income_classification` (`capitalized_income_classification_id`),
  KEY `fk_acc_product_mapping_buydown_fee_classification` (`buydown_fee_classification_id`),
  CONSTRAINT `FK_acc_product_mapping_gl_account_id` FOREIGN KEY (`gl_account_id`) REFERENCES `acc_gl_account` (`id`),
  CONSTRAINT `FK_acc_product_mapping_m_charge` FOREIGN KEY (`charge_id`) REFERENCES `m_charge` (`id`),
  CONSTRAINT `FK_acc_product_mapping_m_payment_type` FOREIGN KEY (`payment_type`) REFERENCES `m_payment_type` (`id`),
  CONSTRAINT `fk_acc_product_mapping_buydown_fee_classification` FOREIGN KEY (`buydown_fee_classification_id`) REFERENCES `m_code_value` (`id`),
  CONSTRAINT `fk_acc_product_mapping_capitalized_income_classification` FOREIGN KEY (`capitalized_income_classification_id`) REFERENCES `m_code_value` (`id`),
  CONSTRAINT `fk_acc_product_mapping_charge_off_reason` FOREIGN KEY (`charge_off_reason_id`) REFERENCES `m_code_value` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `acc_rule_tags` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `acc_rule_id` bigint(20) NOT NULL,
  `tag_id` int(11) NOT NULL,
  `acc_type_enum` smallint(6) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UNIQUE_ACCOUNT_RULE_TAGS` (`acc_rule_id`,`tag_id`,`acc_type_enum`),
  KEY `FK_acc_accounting_rule_id` (`acc_rule_id`),
  KEY `FK_m_code_value_id` (`tag_id`),
  CONSTRAINT `FK_acc_accounting_rule_id` FOREIGN KEY (`acc_rule_id`) REFERENCES `acc_accounting_rule` (`id`),
  CONSTRAINT `FK_m_code_value_id` FOREIGN KEY (`tag_id`) REFERENCES `m_code_value` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `batch_custom_job_parameters` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `parameter_json` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL CHECK (json_valid(`parameter_json`)),
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `c_account_number_format` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `account_type_enum` smallint(6) NOT NULL,
  `prefix_type_enum` smallint(6) DEFAULT NULL,
  `prefix_character` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `account_type_enum` (`account_type_enum`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `c_cache` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `cache_type_enum` tinyint(4) NOT NULL DEFAULT 1,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `c_configuration` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(100) DEFAULT NULL,
  `value` int(11) DEFAULT NULL,
  `date_value` date DEFAULT NULL,
  `string_value` varchar(100) DEFAULT NULL,
  `enabled` tinyint(1) NOT NULL DEFAULT 0,
  `is_trap_door` tinyint(1) NOT NULL DEFAULT 0,
  `description` varchar(300) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `c_external_service` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `c_external_service_properties` (
  `name` varchar(150) NOT NULL,
  `value` varchar(250) DEFAULT NULL,
  `external_service_id` bigint(20) NOT NULL,
  KEY `FK_c_external_service_properties_c_external_service` (`external_service_id`),
  CONSTRAINT `FK_c_external_service_properties_c_external_service` FOREIGN KEY (`external_service_id`) REFERENCES `c_external_service` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `glim_accounts` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `group_id` bigint(20) NOT NULL DEFAULT 0,
  `account_number` varchar(50) NOT NULL,
  `principal_amount` decimal(19,6) NOT NULL DEFAULT 0.000000,
  `child_accounts_count` int(11) NOT NULL,
  `accepting_child` tinyint(1) NOT NULL DEFAULT 0,
  `loan_status_id` smallint(6) NOT NULL DEFAULT 0,
  `application_id` decimal(10,0) DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `account_number` (`account_number`),
  KEY `FK_group_id` (`group_id`),
  CONSTRAINT `FK_group_id` FOREIGN KEY (`group_id`) REFERENCES `m_group` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `gsim_accounts` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `group_id` bigint(20) NOT NULL DEFAULT 0,
  `account_number` varchar(50) NOT NULL,
  `parent_deposit` decimal(19,6) NOT NULL DEFAULT 0.000000,
  `child_accounts_count` int(11) NOT NULL,
  `accepting_child` tinyint(1) NOT NULL DEFAULT 0,
  `savings_status_id` smallint(6) NOT NULL DEFAULT 0,
  `application_id` decimal(10,0) DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `account_number` (`account_number`),
  KEY `FK_gsim_group_id` (`group_id`),
  CONSTRAINT `FK_gsim_group_id` FOREIGN KEY (`group_id`) REFERENCES `m_group` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `interop_identifier` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `account_id` bigint(20) NOT NULL,
  `type` varchar(32) NOT NULL,
  `a_value` varchar(128) NOT NULL,
  `sub_value_or_type` varchar(128) DEFAULT NULL,
  `created_by` varchar(32) NOT NULL,
  `created_on` timestamp(6) NULL DEFAULT NULL,
  `modified_by` varchar(32) DEFAULT NULL,
  `modified_on` timestamp(6) NULL DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_interop_identifier_account` (`account_id`,`type`),
  UNIQUE KEY `uk_interop_identifier_value` (`type`,`a_value`,`sub_value_or_type`),
  KEY `fk_interop_identifier_account` (`account_id`),
  CONSTRAINT `fk_interop_identifier_account` FOREIGN KEY (`account_id`) REFERENCES `m_savings_account` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `job` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL,
  `display_name` varchar(100) NOT NULL,
  `cron_expression` varchar(20) NOT NULL,
  `create_time` datetime(6) DEFAULT NULL,
  `task_priority` smallint(6) NOT NULL DEFAULT 5,
  `group_name` varchar(50) DEFAULT NULL,
  `previous_run_start_time` datetime(6) DEFAULT NULL,
  `next_run_time` datetime(6) DEFAULT NULL,
  `job_key` varchar(500) DEFAULT NULL,
  `initializing_errorlog` text DEFAULT NULL,
  `is_active` tinyint(1) NOT NULL DEFAULT 1,
  `currently_running` tinyint(1) NOT NULL DEFAULT 0,
  `updates_allowed` tinyint(1) NOT NULL DEFAULT 1,
  `scheduler_group` smallint(6) NOT NULL DEFAULT 0,
  `is_misfired` tinyint(1) NOT NULL DEFAULT 0,
  `node_id` int(11) DEFAULT NULL,
  `is_mismatched_job` tinyint(1) DEFAULT 1,
  `short_name` varchar(8) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `short_name` (`short_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `job_parameters` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `job_id` bigint(20) NOT NULL,
  `parameter_name` varchar(100) NOT NULL,
  `parameter_value` text DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_job_id` (`job_id`),
  CONSTRAINT `FK_job_id` FOREIGN KEY (`job_id`) REFERENCES `job` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `job_run_history` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `job_id` bigint(20) NOT NULL,
  `version` bigint(20) NOT NULL,
  `start_time` datetime(6) DEFAULT NULL,
  `end_time` datetime(6) DEFAULT NULL,
  `status` varchar(10) NOT NULL,
  `error_message` text DEFAULT NULL,
  `trigger_type` varchar(25) NOT NULL,
  `error_log` text DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `scheduledjobsFK` (`job_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `m_account_transfer_details` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `from_office_id` bigint(20) NOT NULL,
  `to_office_id` bigint(20) NOT NULL,
  `from_client_id` bigint(20) DEFAULT NULL,
  `to_client_id` bigint(20) DEFAULT NULL,
  `from_savings_account_id` bigint(20) DEFAULT NULL,
  `to_savings_account_id` bigint(20) DEFAULT NULL,
  `from_loan_account_id` bigint(20) DEFAULT NULL,
  `to_loan_account_id` bigint(20) DEFAULT NULL,
  `transfer_type` smallint(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_m_account_transfer_details_from_client` (`from_client_id`),
  KEY `FK_m_account_transfer_details_from_loan_account` (`from_loan_account_id`),
  KEY `FK_m_account_transfer_details_from_office` (`from_office_id`),
  KEY `FK_m_account_transfer_details_from_savings_account` (`from_savings_account_id`),
  KEY `FK_m_account_transfer_details_to_client` (`to_client_id`),
  KEY `FK_m_account_transfer_details_to_loan_account` (`to_loan_account_id`),
  KEY `FK_m_account_transfer_details_to_office` (`to_office_id`),
  KEY `FK_m_account_transfer_details_to_savings_account` (`to_savings_account_id`),
  CONSTRAINT `FK_m_account_transfer_details_from_client` FOREIGN KEY (`from_client_id`) REFERENCES `m_client` (`id`),
  CONSTRAINT `FK_m_account_transfer_details_from_loan_account` FOREIGN KEY (`from_loan_account_id`) REFERENCES `m_loan` (`id`),
  CONSTRAINT `FK_m_account_transfer_details_from_office` FOREIGN KEY (`from_office_id`) REFERENCES `m_office` (`id`),
  CONSTRAINT `FK_m_account_transfer_details_from_savings_account` FOREIGN KEY (`from_savings_account_id`) REFERENCES `m_savings_account` (`id`),
  CONSTRAINT `FK_m_account_transfer_details_to_client` FOREIGN KEY (`to_client_id`) REFERENCES `m_client` (`id`),
  CONSTRAINT `FK_m_account_transfer_details_to_loan_account` FOREIGN KEY (`to_loan_account_id`) REFERENCES `m_loan` (`id`),
  CONSTRAINT `FK_m_account_transfer_details_to_office` FOREIGN KEY (`to_office_id`) REFERENCES `m_office` (`id`),
  CONSTRAINT `FK_m_account_transfer_details_to_savings_account` FOREIGN KEY (`to_savings_account_id`) REFERENCES `m_savings_account` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `m_account_transfer_standing_instructions` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(250) NOT NULL,
  `account_transfer_details_id` bigint(20) NOT NULL,
  `priority` tinyint(4) NOT NULL,
  `status` tinyint(4) NOT NULL,
  `instruction_type` tinyint(4) NOT NULL,
  `amount` decimal(19,6) DEFAULT NULL,
  `valid_from` date NOT NULL,
  `valid_till` date DEFAULT NULL,
  `recurrence_type` tinyint(4) NOT NULL,
  `recurrence_frequency` smallint(6) DEFAULT NULL,
  `recurrence_interval` smallint(6) DEFAULT NULL,
  `recurrence_on_day` smallint(6) DEFAULT NULL,
  `recurrence_on_month` smallint(6) DEFAULT NULL,
  `last_run_date` date DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`),
  KEY `FK_m_standing_instructions_account_transfer_details` (`account_transfer_details_id`),
  CONSTRAINT `FK_m_standing_instructions_account_transfer_details` FOREIGN KEY (`account_transfer_details_id`) REFERENCES `m_account_transfer_details` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `m_account_transfer_standing_instructions_history` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `standing_instruction_id` bigint(20) NOT NULL,
  `status` varchar(20) NOT NULL,
  `execution_time` datetime(6) DEFAULT NULL,
  `amount` decimal(19,6) NOT NULL,
  `error_log` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_m_account_transfer_standing_instructions_history` (`standing_instruction_id`),
  CONSTRAINT `FK_m_account_transfer_standing_instructions_m_history` FOREIGN KEY (`standing_instruction_id`) REFERENCES `m_account_transfer_standing_instructions` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `m_account_transfer_transaction` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `account_transfer_details_id` bigint(20) NOT NULL,
  `from_savings_transaction_id` bigint(20) DEFAULT NULL,
  `from_loan_transaction_id` bigint(20) DEFAULT NULL,
  `to_savings_transaction_id` bigint(20) DEFAULT NULL,
  `to_loan_transaction_id` bigint(20) DEFAULT NULL,
  `is_reversed` tinyint(1) NOT NULL,
  `transaction_date` date NOT NULL,
  `currency_code` varchar(3) NOT NULL,
  `currency_digits` smallint(6) NOT NULL,
  `currency_multiplesof` smallint(6) DEFAULT NULL,
  `amount` decimal(19,6) NOT NULL,
  `description` varchar(200) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_m_account_transfer_transaction_account_detail` (`account_transfer_details_id`),
  KEY `FK_m_account_transfer_transaction_from_m_loan_transaction` (`from_loan_transaction_id`),
  KEY `FK_m_account_transfer_transaction_from_m_savings_transaction` (`from_savings_transaction_id`),
  KEY `FK_m_account_transfer_transaction_to_m_loan_transaction` (`to_loan_transaction_id`),
  KEY `FK_m_account_transfer_transaction_to_m_savings_transaction` (`to_savings_transaction_id`),
  CONSTRAINT `FK_m_account_transfer_transaction_account_detail` FOREIGN KEY (`account_transfer_details_id`) REFERENCES `m_account_transfer_details` (`id`),
  CONSTRAINT `FK_m_account_transfer_transaction_from_m_loan_transaction` FOREIGN KEY (`from_loan_transaction_id`) REFERENCES `m_loan_transaction` (`id`),
  CONSTRAINT `FK_m_account_transfer_transaction_from_m_savings_transaction` FOREIGN KEY (`from_savings_transaction_id`) REFERENCES `m_savings_account_transaction` (`id`),
  CONSTRAINT `FK_m_account_transfer_transaction_to_m_loan_transaction` FOREIGN KEY (`to_loan_transaction_id`) REFERENCES `m_loan_transaction` (`id`),
  CONSTRAINT `FK_m_account_transfer_transaction_to_m_savings_transaction` FOREIGN KEY (`to_savings_transaction_id`) REFERENCES `m_savings_account_transaction` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `m_address` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `street` varchar(100) DEFAULT NULL,
  `address_line_1` varchar(100) DEFAULT NULL,
  `address_line_2` varchar(100) DEFAULT NULL,
  `address_line_3` varchar(100) DEFAULT NULL,
  `town_village` varchar(100) DEFAULT NULL,
  `city` varchar(100) DEFAULT NULL,
  `county_district` varchar(100) DEFAULT NULL,
  `state_province_id` int(11) DEFAULT NULL,
  `country_id` int(11) DEFAULT NULL,
  `postal_code` varchar(10) DEFAULT NULL,
  `latitude` decimal(10,8) DEFAULT 0.00000000,
  `longitude` decimal(10,8) DEFAULT 0.00000000,
  `created_by` varchar(100) DEFAULT NULL,
  `created_on` date DEFAULT NULL,
  `updated_by` varchar(100) DEFAULT NULL,
  `updated_on` date DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `address_fields_codefk1` (`state_province_id`),
  KEY `address_fields_codefk2` (`country_id`),
  CONSTRAINT `address_fields_codefk1` FOREIGN KEY (`state_province_id`) REFERENCES `m_code_value` (`id`),
  CONSTRAINT `address_fields_codefk2` FOREIGN KEY (`country_id`) REFERENCES `m_code_value` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `m_adhoc` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(100) DEFAULT NULL,
  `query` varchar(2000) DEFAULT NULL,
  `table_name` varchar(100) DEFAULT NULL,
  `table_fields` varchar(1000) DEFAULT NULL,
  `email` varchar(500) DEFAULT NULL,
  `is_active` tinyint(1) DEFAULT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `createdby_id` bigint(20) NOT NULL,
  `lastmodifiedby_id` bigint(20) NOT NULL,
  `lastmodified_date` datetime(6) DEFAULT NULL,
  `report_run_frequency_code` int(11) DEFAULT NULL,
  `report_run_every` int(11) DEFAULT NULL,
  `last_run` timestamp(6) NULL DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `createdby_id` (`createdby_id`),
  KEY `lastmodifiedby_id` (`lastmodifiedby_id`),
  CONSTRAINT `createdby_id` FOREIGN KEY (`createdby_id`) REFERENCES `m_appuser` (`id`),
  CONSTRAINT `lastmodifiedby_id` FOREIGN KEY (`lastmodifiedby_id`) REFERENCES `m_appuser` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `m_appuser` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `is_deleted` tinyint(1) NOT NULL DEFAULT 0,
  `office_id` bigint(20) DEFAULT NULL,
  `staff_id` bigint(20) DEFAULT NULL,
  `username` varchar(100) NOT NULL,
  `firstname` varchar(100) NOT NULL,
  `lastname` varchar(100) NOT NULL,
  `password` varchar(255) NOT NULL,
  `email` varchar(100) NOT NULL,
  `firsttime_login_remaining` tinyint(1) NOT NULL,
  `nonexpired` tinyint(1) NOT NULL,
  `nonlocked` tinyint(1) NOT NULL,
  `nonexpired_credentials` tinyint(1) NOT NULL,
  `enabled` tinyint(1) NOT NULL,
  `last_time_password_updated` date NOT NULL DEFAULT '1970-01-01',
  `password_never_expires` tinyint(1) NOT NULL DEFAULT 0 COMMENT 'define if the password, should be check for validity period or not',
  `cannot_change_password` tinyint(1) DEFAULT 0,
  `password_reset_required` tinyint(1) NOT NULL DEFAULT 0,
  `failed_login_attempts` int(11) NOT NULL DEFAULT 0,
  `is_login_retries_enabled` tinyint(1) NOT NULL DEFAULT 0,
  `temporary_password` varchar(255) DEFAULT NULL,
  `temporary_password_expiry_time` datetime DEFAULT NULL,
  `is_password_reset_enabled` tinyint(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `username_org` (`username`),
  KEY `FKB3D587CE0DD567A` (`office_id`),
  KEY `fk_m_appuser_002x` (`staff_id`),
  KEY `last_time_password_updated` (`last_time_password_updated`),
  CONSTRAINT `FKB3D587CE0DD567A` FOREIGN KEY (`office_id`) REFERENCES `m_office` (`id`),
  CONSTRAINT `fk_m_appuser_002` FOREIGN KEY (`staff_id`) REFERENCES `m_staff` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `m_appuser_previous_password` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) NOT NULL,
  `password` varchar(255) NOT NULL,
  `removal_date` date NOT NULL,
  PRIMARY KEY (`id`),
  KEY `user_id` (`user_id`),
  CONSTRAINT `m_appuser_previous_password_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `m_appuser` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `m_appuser_role` (
  `appuser_id` bigint(20) NOT NULL,
  `role_id` bigint(20) NOT NULL,
  PRIMARY KEY (`appuser_id`,`role_id`),
  KEY `FK7662CE5915CEC7AB` (`role_id`),
  KEY `FK7662CE59B4100309` (`appuser_id`),
  CONSTRAINT `FK7662CE5915CEC7AB` FOREIGN KEY (`role_id`) REFERENCES `m_role` (`id`),
  CONSTRAINT `FK7662CE59B4100309` FOREIGN KEY (`appuser_id`) REFERENCES `m_appuser` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `m_batch_business_steps` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `job_name` varchar(100) NOT NULL,
  `step_name` varchar(100) NOT NULL,
  `step_order` smallint(6) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `m_business_date` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `type` varchar(100) NOT NULL,
  `date` date NOT NULL,
  `created_by` bigint(20) NOT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `version` bigint(20) NOT NULL,
  `last_modified_by` bigint(20) NOT NULL,
  `lastmodified_date` datetime(6) DEFAULT NULL,
  `created_on_utc` datetime(6) DEFAULT NULL,
  `last_modified_on_utc` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `type` (`type`),
  KEY `FK_business_date_created_by` (`created_by`),
  KEY `FK_business_date_last_modified_by` (`last_modified_by`),
  CONSTRAINT `FK_business_date_created_by` FOREIGN KEY (`created_by`) REFERENCES `m_appuser` (`id`),
  CONSTRAINT `FK_business_date_last_modified_by` FOREIGN KEY (`last_modified_by`) REFERENCES `m_appuser` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `m_calendar` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `title` varchar(70) NOT NULL,
  `description` varchar(100) DEFAULT NULL,
  `location` varchar(50) DEFAULT NULL,
  `start_date` date NOT NULL,
  `end_date` date DEFAULT NULL,
  `duration` smallint(6) DEFAULT NULL,
  `calendar_type_enum` smallint(6) NOT NULL,
  `repeating` tinyint(1) NOT NULL DEFAULT 0,
  `recurrence` varchar(100) DEFAULT NULL,
  `remind_by_enum` smallint(6) DEFAULT NULL,
  `first_reminder` smallint(6) DEFAULT NULL,
  `second_reminder` smallint(6) DEFAULT NULL,
  `created_by` bigint(20) NOT NULL,
  `last_modified_by` bigint(20) NOT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `lastmodified_date` datetime(6) DEFAULT NULL,
  `meeting_time` time DEFAULT NULL,
  `created_on_utc` datetime(6) DEFAULT NULL,
  `last_modified_on_utc` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_calendar_created_by` (`created_by`),
  KEY `FK_calendar_last_modified_by` (`last_modified_by`),
  CONSTRAINT `FK_calendar_created_by` FOREIGN KEY (`created_by`) REFERENCES `m_appuser` (`id`),
  CONSTRAINT `FK_calendar_last_modified_by` FOREIGN KEY (`last_modified_by`) REFERENCES `m_appuser` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `m_calendar_history` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `calendar_id` bigint(20) NOT NULL,
  `title` varchar(70) NOT NULL,
  `description` varchar(100) DEFAULT NULL,
  `location` varchar(50) DEFAULT NULL,
  `start_date` date NOT NULL,
  `end_date` date DEFAULT NULL,
  `duration` smallint(6) DEFAULT NULL,
  `calendar_type_enum` smallint(6) NOT NULL,
  `repeating` tinyint(1) NOT NULL DEFAULT 0,
  `recurrence` varchar(100) DEFAULT NULL,
  `remind_by_enum` smallint(6) DEFAULT NULL,
  `first_reminder` smallint(6) DEFAULT NULL,
  `second_reminder` smallint(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_m_calendar_m_calendar_history` (`calendar_id`),
  CONSTRAINT `FK_m_calendar_m_calendar_history` FOREIGN KEY (`calendar_id`) REFERENCES `m_calendar` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `m_calendar_instance` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `calendar_id` bigint(20) NOT NULL,
  `entity_id` bigint(20) NOT NULL,
  `entity_type_enum` smallint(6) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_m_calendar_m_calendar_instance` (`calendar_id`),
  KEY `idx_m_calendar_instance_entity_id_entity_type` (`entity_id`,`entity_type_enum`),
  CONSTRAINT `FK_m_calendar_m_calendar_instance` FOREIGN KEY (`calendar_id`) REFERENCES `m_calendar` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `m_cashier_transactions` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `cashier_id` bigint(20) NOT NULL,
  `txn_type` smallint(6) NOT NULL,
  `txn_amount` decimal(19,6) NOT NULL,
  `txn_date` date NOT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `entity_type` varchar(50) DEFAULT NULL,
  `entity_id` bigint(20) DEFAULT NULL,
  `txn_note` varchar(200) DEFAULT NULL,
  `currency_code` varchar(3) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `IK_m_teller_transactions_m_cashier` (`cashier_id`),
  CONSTRAINT `FK_m_teller_transactions_m_cashiers` FOREIGN KEY (`cashier_id`) REFERENCES `m_cashiers` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `m_cashiers` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `staff_id` bigint(20) DEFAULT NULL,
  `teller_id` bigint(20) DEFAULT NULL,
  `description` varchar(100) DEFAULT NULL,
  `start_date` date DEFAULT NULL,
  `end_date` date DEFAULT NULL,
  `start_time` varchar(10) DEFAULT NULL,
  `end_time` varchar(10) DEFAULT NULL,
  `full_day` tinyint(1) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `IK_m_cashiers_m_staff` (`staff_id`),
  KEY `IK_m_cashiers_m_teller` (`teller_id`),
  CONSTRAINT `FK_m_cashiers_m_staff` FOREIGN KEY (`staff_id`) REFERENCES `m_staff` (`id`),
  CONSTRAINT `FK_m_cashiers_m_teller` FOREIGN KEY (`teller_id`) REFERENCES `m_tellers` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `m_charge` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(100) DEFAULT NULL,
  `currency_code` varchar(3) NOT NULL,
  `charge_applies_to_enum` smallint(6) NOT NULL,
  `charge_time_enum` smallint(6) NOT NULL,
  `charge_calculation_enum` smallint(6) NOT NULL,
  `charge_payment_mode_enum` smallint(6) DEFAULT NULL,
  `amount` decimal(19,6) NOT NULL,
  `fee_on_day` smallint(6) DEFAULT NULL,
  `fee_interval` smallint(6) DEFAULT NULL,
  `fee_on_month` smallint(6) DEFAULT NULL,
  `is_penalty` tinyint(1) NOT NULL DEFAULT 0,
  `is_active` tinyint(1) NOT NULL,
  `is_deleted` tinyint(1) NOT NULL DEFAULT 0,
  `min_cap` decimal(19,6) DEFAULT NULL,
  `max_cap` decimal(19,6) DEFAULT NULL,
  `fee_frequency` smallint(6) DEFAULT NULL,
  `is_free_withdrawal` tinyint(1) NOT NULL DEFAULT 0,
  `free_withdrawal_charge_frequency` int(11) DEFAULT 0,
  `restart_frequency` int(11) DEFAULT 0,
  `restart_frequency_enum` int(11) DEFAULT 0,
  `is_payment_type` tinyint(1) DEFAULT 0,
  `payment_type_id` int(11) DEFAULT NULL,
  `income_or_liability_account_id` bigint(20) DEFAULT NULL,
  `tax_group_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`),
  KEY `FK_m_charge_acc_gl_account` (`income_or_liability_account_id`),
  KEY `FK_m_charge_m_tax_group` (`tax_group_id`),
  KEY `FK_m_payment_detail_m_payment_types` (`payment_type_id`),
  CONSTRAINT `FK_m_charge_acc_gl_account` FOREIGN KEY (`income_or_liability_account_id`) REFERENCES `acc_gl_account` (`id`),
  CONSTRAINT `FK_m_charge_m_tax_group` FOREIGN KEY (`tax_group_id`) REFERENCES `m_tax_group` (`id`),
  CONSTRAINT `FK_m_payment_detail_m_payment_types` FOREIGN KEY (`payment_type_id`) REFERENCES `m_payment_type` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `m_client` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `account_no` varchar(20) NOT NULL,
  `external_id` varchar(100) DEFAULT NULL,
  `status_enum` int(11) NOT NULL DEFAULT 300,
  `sub_status` int(11) DEFAULT NULL,
  `activation_date` date DEFAULT NULL,
  `office_joining_date` date DEFAULT NULL,
  `office_id` bigint(20) NOT NULL,
  `transfer_to_office_id` bigint(20) DEFAULT NULL,
  `staff_id` bigint(20) DEFAULT NULL,
  `firstname` varchar(50) DEFAULT NULL,
  `middlename` varchar(50) DEFAULT NULL,
  `lastname` varchar(50) DEFAULT NULL,
  `fullname` varchar(160) DEFAULT NULL,
  `display_name` varchar(160) DEFAULT NULL,
  `mobile_no` varchar(50) DEFAULT NULL,
  `is_staff` tinyint(1) NOT NULL DEFAULT 0,
  `gender_cv_id` int(11) DEFAULT NULL,
  `date_of_birth` date DEFAULT NULL,
  `image_id` bigint(20) DEFAULT NULL,
  `closure_reason_cv_id` int(11) DEFAULT NULL,
  `closedon_date` date DEFAULT NULL,
  `updated_by` bigint(20) DEFAULT NULL,
  `updated_on` date DEFAULT NULL,
  `submittedon_date` date DEFAULT NULL,
  `activatedon_userid` bigint(20) DEFAULT NULL,
  `closedon_userid` bigint(20) DEFAULT NULL,
  `default_savings_product` bigint(20) DEFAULT NULL,
  `default_savings_account` bigint(20) DEFAULT NULL,
  `client_type_cv_id` int(11) DEFAULT NULL,
  `client_classification_cv_id` int(11) DEFAULT NULL,
  `reject_reason_cv_id` int(11) DEFAULT NULL,
  `rejectedon_date` date DEFAULT NULL,
  `rejectedon_userid` bigint(20) DEFAULT NULL,
  `withdraw_reason_cv_id` int(11) DEFAULT NULL,
  `withdrawn_on_date` date DEFAULT NULL,
  `withdraw_on_userid` bigint(20) DEFAULT NULL,
  `reactivated_on_date` date DEFAULT NULL,
  `reactivated_on_userid` bigint(20) DEFAULT NULL,
  `legal_form_enum` int(11) DEFAULT NULL,
  `reopened_on_date` date DEFAULT NULL,
  `reopened_by_userid` bigint(20) DEFAULT NULL,
  `email_address` varchar(150) DEFAULT NULL,
  `proposed_transfer_date` date DEFAULT NULL,
  `created_on_utc` datetime(6) DEFAULT NULL,
  `created_by` bigint(20) NOT NULL,
  `last_modified_by` bigint(20) NOT NULL,
  `last_modified_on_utc` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `account_no` (`account_no`),
  UNIQUE KEY `external_id` (`external_id`),
  UNIQUE KEY `mobile_no` (`mobile_no`),
  KEY `FK1_m_client_gender_m_code_value` (`gender_cv_id`),
  KEY `FKCE00CAB3E0DD567A` (`office_id`),
  KEY `FK_m_client_classification_m_code_value` (`client_classification_cv_id`),
  KEY `FK_m_client_m_code` (`closure_reason_cv_id`),
  KEY `FK_m_client_m_image` (`image_id`),
  KEY `FK_m_client_m_office` (`transfer_to_office_id`),
  KEY `FK_m_client_m_savings_account` (`default_savings_account`),
  KEY `FK_m_client_m_savings_product` (`default_savings_product`),
  KEY `FK_m_client_substatus_m_code_value` (`sub_status`),
  KEY `FK_m_client_type_m_code_value` (`client_type_cv_id`),
  KEY `FK_m_client_type_m_code_value_withdraw` (`withdraw_reason_cv_id`),
  KEY `FK_m_client_type_mcode_value_reject` (`reject_reason_cv_id`),
  KEY `client_staff_id` (`staff_id`),
  KEY `FK_client_created_by` (`created_by`),
  KEY `FK_client_last_modified_by` (`last_modified_by`),
  CONSTRAINT `FK1_m_client_gender_m_code_value` FOREIGN KEY (`gender_cv_id`) REFERENCES `m_code_value` (`id`),
  CONSTRAINT `FKCE00CAB3E0DD567A` FOREIGN KEY (`office_id`) REFERENCES `m_office` (`id`),
  CONSTRAINT `FK_client_created_by` FOREIGN KEY (`created_by`) REFERENCES `m_appuser` (`id`),
  CONSTRAINT `FK_client_last_modified_by` FOREIGN KEY (`last_modified_by`) REFERENCES `m_appuser` (`id`),
  CONSTRAINT `FK_m_client_classification_m_code_value` FOREIGN KEY (`client_classification_cv_id`) REFERENCES `m_code_value` (`id`),
  CONSTRAINT `FK_m_client_m_code` FOREIGN KEY (`closure_reason_cv_id`) REFERENCES `m_code_value` (`id`),
  CONSTRAINT `FK_m_client_m_image` FOREIGN KEY (`image_id`) REFERENCES `m_image` (`id`),
  CONSTRAINT `FK_m_client_m_office` FOREIGN KEY (`transfer_to_office_id`) REFERENCES `m_office` (`id`),
  CONSTRAINT `FK_m_client_m_savings_account` FOREIGN KEY (`default_savings_account`) REFERENCES `m_savings_account` (`id`),
  CONSTRAINT `FK_m_client_m_savings_product` FOREIGN KEY (`default_savings_product`) REFERENCES `m_savings_product` (`id`),
  CONSTRAINT `FK_m_client_m_staff` FOREIGN KEY (`staff_id`) REFERENCES `m_staff` (`id`),
  CONSTRAINT `FK_m_client_substatus_m_code_value` FOREIGN KEY (`sub_status`) REFERENCES `m_code_value` (`id`),
  CONSTRAINT `FK_m_client_type_m_code_value` FOREIGN KEY (`client_type_cv_id`) REFERENCES `m_code_value` (`id`),
  CONSTRAINT `FK_m_client_type_m_code_value_withdraw` FOREIGN KEY (`withdraw_reason_cv_id`) REFERENCES `m_code_value` (`id`),
  CONSTRAINT `FK_m_client_type_mcode_value_reject` FOREIGN KEY (`reject_reason_cv_id`) REFERENCES `m_code_value` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `m_client_address` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `client_id` bigint(20) NOT NULL DEFAULT 0,
  `address_id` bigint(20) NOT NULL DEFAULT 0,
  `address_type_id` int(11) NOT NULL DEFAULT 0,
  `is_active` tinyint(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `addressIdFk` (`address_id`),
  KEY `address_codefk` (`address_type_id`),
  KEY `clientaddressfk` (`client_id`),
  CONSTRAINT `address_codefk` FOREIGN KEY (`address_type_id`) REFERENCES `m_code_value` (`id`),
  CONSTRAINT `clientaddressfk` FOREIGN KEY (`client_id`) REFERENCES `m_client` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `m_client_attendance` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `client_id` bigint(20) NOT NULL DEFAULT 0,
  `meeting_id` bigint(20) NOT NULL,
  `attendance_type_enum` smallint(6) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_client_meeting_attendance` (`client_id`,`meeting_id`),
  KEY `FK_m_meeting_m_client_attendance` (`meeting_id`),
  CONSTRAINT `FK_m_client_m_client_attendance` FOREIGN KEY (`client_id`) REFERENCES `m_client` (`id`),
  CONSTRAINT `FK_m_meeting_m_client_attendance` FOREIGN KEY (`meeting_id`) REFERENCES `m_meeting` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `m_client_charge` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `client_id` bigint(20) NOT NULL,
  `charge_id` bigint(20) NOT NULL,
  `is_penalty` tinyint(1) NOT NULL,
  `charge_time_enum` smallint(6) NOT NULL,
  `charge_due_date` date DEFAULT NULL,
  `charge_calculation_enum` smallint(6) NOT NULL,
  `amount` decimal(19,6) NOT NULL,
  `amount_paid_derived` decimal(19,6) DEFAULT NULL,
  `amount_waived_derived` decimal(19,6) DEFAULT NULL,
  `amount_writtenoff_derived` decimal(19,6) DEFAULT NULL,
  `amount_outstanding_derived` decimal(19,6) NOT NULL,
  `is_paid_derived` tinyint(1) DEFAULT NULL,
  `waived` tinyint(1) DEFAULT NULL,
  `is_active` tinyint(1) DEFAULT NULL,
  `inactivated_on_date` date DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_m_client_charge_m_charge` (`charge_id`),
  KEY `FK_m_client_charge_m_client` (`client_id`),
  CONSTRAINT `FK_m_client_charge_m_charge` FOREIGN KEY (`charge_id`) REFERENCES `m_charge` (`id`),
  CONSTRAINT `FK_m_client_charge_m_client` FOREIGN KEY (`client_id`) REFERENCES `m_client` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `m_client_charge_paid_by` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `client_transaction_id` bigint(20) NOT NULL,
  `client_charge_id` bigint(20) NOT NULL,
  `amount` decimal(19,6) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_m_client_charge_paid_by_m_client_charge` (`client_charge_id`),
  KEY `FK_m_client_charge_paid_by_m_client_transaction` (`client_transaction_id`),
  CONSTRAINT `FK_m_client_charge_paid_by_m_client_charge` FOREIGN KEY (`client_charge_id`) REFERENCES `m_client_charge` (`id`),
  CONSTRAINT `FK_m_client_charge_paid_by_m_client_transaction` FOREIGN KEY (`client_transaction_id`) REFERENCES `m_client_transaction` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `m_client_collateral_management` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `quantity` decimal(20,5) NOT NULL,
  `client_id` bigint(20) DEFAULT NULL,
  `collateral_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fkClient` (`client_id`),
  KEY `fkCollateral` (`collateral_id`),
  CONSTRAINT `fkClient` FOREIGN KEY (`client_id`) REFERENCES `m_client` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fkCollateral` FOREIGN KEY (`collateral_id`) REFERENCES `m_collateral_management` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `m_client_identifier` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `client_id` bigint(20) NOT NULL,
  `document_type_id` int(11) NOT NULL,
  `document_key` varchar(50) NOT NULL,
  `status` int(11) NOT NULL DEFAULT 300,
  `active` int(11) DEFAULT NULL,
  `description` varchar(500) DEFAULT NULL,
  `created_by` bigint(20) NOT NULL,
  `last_modified_by` bigint(20) NOT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `lastmodified_date` datetime(6) DEFAULT NULL,
  `created_on_utc` datetime(6) DEFAULT NULL,
  `last_modified_on_utc` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_identifier_key` (`document_type_id`,`document_key`),
  UNIQUE KEY `unique_active_client_identifier` (`client_id`,`document_type_id`,`active`),
  KEY `FK_m_client_document_m_client` (`client_id`),
  KEY `FK_m_client_document_m_code_value` (`document_type_id`),
  KEY `FK_client_identifier_created_by` (`created_by`),
  KEY `FK_client_identifier_last_modified_by` (`last_modified_by`),
  CONSTRAINT `FK_client_identifier_created_by` FOREIGN KEY (`created_by`) REFERENCES `m_appuser` (`id`),
  CONSTRAINT `FK_client_identifier_last_modified_by` FOREIGN KEY (`last_modified_by`) REFERENCES `m_appuser` (`id`),
  CONSTRAINT `FK_m_client_document_m_client` FOREIGN KEY (`client_id`) REFERENCES `m_client` (`id`),
  CONSTRAINT `FK_m_client_document_m_code_value` FOREIGN KEY (`document_type_id`) REFERENCES `m_code_value` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `m_client_non_person` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `client_id` bigint(20) NOT NULL,
  `constitution_cv_id` int(11) NOT NULL,
  `incorp_no` varchar(50) DEFAULT NULL,
  `incorp_validity_till` date DEFAULT NULL,
  `main_business_line_cv_id` int(11) DEFAULT NULL,
  `remarks` varchar(150) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `client_id` (`client_id`),
  CONSTRAINT `FK_client_id` FOREIGN KEY (`client_id`) REFERENCES `m_client` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `m_client_transaction` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `client_id` bigint(20) NOT NULL,
  `office_id` bigint(20) NOT NULL,
  `currency_code` varchar(3) NOT NULL,
  `payment_detail_id` bigint(20) DEFAULT NULL,
  `is_reversed` tinyint(1) NOT NULL,
  `external_id` varchar(50) DEFAULT NULL,
  `transaction_date` date NOT NULL,
  `transaction_type_enum` smallint(6) NOT NULL,
  `amount` decimal(19,6) NOT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `created_on_utc` datetime(6) DEFAULT NULL,
  `created_by` bigint(20) NOT NULL,
  `last_modified_by` bigint(20) NOT NULL,
  `last_modified_on_utc` datetime(6) DEFAULT NULL,
  `submitted_on_date` date NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `external_id` (`external_id`),
  KEY `FK_m_client_transaction_m_client` (`client_id`),
  KEY `FK_client_transaction_created_by` (`created_by`),
  KEY `FK_client_transaction_last_modified_by` (`last_modified_by`),
  KEY `FK_client_transaction_submitted_on_date` (`submitted_on_date`),
  CONSTRAINT `FK_client_transaction_created_by` FOREIGN KEY (`created_by`) REFERENCES `m_appuser` (`id`),
  CONSTRAINT `FK_client_transaction_last_modified_by` FOREIGN KEY (`last_modified_by`) REFERENCES `m_appuser` (`id`),
  CONSTRAINT `FK_m_client_transaction_m_client` FOREIGN KEY (`client_id`) REFERENCES `m_client` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `m_client_transfer_details` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `client_id` bigint(20) NOT NULL,
  `from_office_id` bigint(20) NOT NULL,
  `to_office_id` bigint(20) NOT NULL,
  `proposed_transfer_date` date DEFAULT NULL,
  `transfer_type` tinyint(4) NOT NULL,
  `submitted_on` date NOT NULL,
  `submitted_by` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_m_client_transfer_details_m_appuser` (`submitted_by`),
  KEY `FK_m_client_transfer_details_m_client` (`client_id`),
  KEY `FK_m_client_transfer_details_m_office` (`from_office_id`),
  KEY `FK_m_client_transfer_details_m_office_2` (`to_office_id`),
  CONSTRAINT `FK_m_client_transfer_details_m_appuser` FOREIGN KEY (`submitted_by`) REFERENCES `m_appuser` (`id`),
  CONSTRAINT `FK_m_client_transfer_details_m_client` FOREIGN KEY (`client_id`) REFERENCES `m_client` (`id`),
  CONSTRAINT `FK_m_client_transfer_details_m_office` FOREIGN KEY (`from_office_id`) REFERENCES `m_office` (`id`),
  CONSTRAINT `FK_m_client_transfer_details_m_office_2` FOREIGN KEY (`to_office_id`) REFERENCES `m_office` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `m_code` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `code_name` varchar(100) DEFAULT NULL,
  `is_system_defined` tinyint(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `code_name` (`code_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `m_code_value` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `code_id` int(11) NOT NULL,
  `code_value` varchar(100) DEFAULT NULL,
  `code_description` varchar(500) DEFAULT NULL,
  `order_position` int(11) NOT NULL DEFAULT 0,
  `code_score` int(11) DEFAULT NULL,
  `is_active` tinyint(1) NOT NULL DEFAULT 1,
  `is_mandatory` tinyint(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `code_value` (`code_id`,`code_value`),
  KEY `FKCFCEA42640BE071Z` (`code_id`),
  CONSTRAINT `FKCFCEA42640BE071Z` FOREIGN KEY (`code_id`) REFERENCES `m_code` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `m_collateral_management` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(50) NOT NULL,
  `quality` varchar(40) NOT NULL,
  `base_price` decimal(20,5) NOT NULL,
  `unit_type` varchar(10) NOT NULL,
  `pct_to_base` decimal(20,5) NOT NULL,
  `currency` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fkCurrencyCode` (`currency`),
  CONSTRAINT `fkCurrencyCode` FOREIGN KEY (`currency`) REFERENCES `m_currency` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `m_creditbureau` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL,
  `product` varchar(100) NOT NULL,
  `country` varchar(100) NOT NULL,
  `implementation_key` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique impl` (`name`,`product`,`country`,`implementation_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `m_creditbureau_configuration` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `configkey` varchar(50) DEFAULT NULL,
  `value` longtext DEFAULT NULL,
  `organisation_creditbureau_id` bigint(20) DEFAULT NULL,
  `description` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `mcbconfig` (`configkey`,`organisation_creditbureau_id`),
  KEY `cbConfigfk1` (`organisation_creditbureau_id`),
  CONSTRAINT `cbConfigfk1` FOREIGN KEY (`organisation_creditbureau_id`) REFERENCES `m_creditbureau` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `m_creditbureau_loanproduct_mapping` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `organisation_creditbureau_id` bigint(20) NOT NULL,
  `loan_product_id` bigint(20) NOT NULL,
  `is_creditcheck_mandatory` tinyint(1) DEFAULT NULL,
  `skip_creditcheck_in_failure` tinyint(1) DEFAULT NULL,
  `stale_period` int(11) DEFAULT NULL,
  `is_active` tinyint(1) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `cblpunique_key` (`organisation_creditbureau_id`,`loan_product_id`),
  KEY `fk_cb_lp2` (`loan_product_id`),
  CONSTRAINT `cblpfk2` FOREIGN KEY (`organisation_creditbureau_id`) REFERENCES `m_organisation_creditbureau` (`id`),
  CONSTRAINT `fk_cb_lp2` FOREIGN KEY (`loan_product_id`) REFERENCES `m_product_loan` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `m_creditbureau_token` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `username` varchar(128) DEFAULT NULL,
  `token` mediumtext DEFAULT NULL,
  `token_type` varchar(128) DEFAULT NULL,
  `expires_in` varchar(128) DEFAULT NULL,
  `issued` varchar(128) DEFAULT NULL,
  `expiry_date` date DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `m_creditreport` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `credit_bureau_id` bigint(20) DEFAULT NULL,
  `national_id` varchar(128) DEFAULT NULL,
  `credit_reports` blob DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `cbId` (`credit_bureau_id`),
  CONSTRAINT `cbId` FOREIGN KEY (`credit_bureau_id`) REFERENCES `m_creditbureau` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `m_currency` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `code` varchar(3) NOT NULL,
  `decimal_places` smallint(6) NOT NULL,
  `currency_multiplesof` smallint(6) DEFAULT NULL,
  `display_symbol` varchar(10) DEFAULT NULL,
  `name` varchar(50) NOT NULL,
  `internationalized_name_code` varchar(50) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `m_delinquency_bucket` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL,
  `created_by` bigint(20) NOT NULL,
  `created_on_utc` datetime(6) DEFAULT NULL,
  `version` bigint(20) NOT NULL,
  `last_modified_by` bigint(20) NOT NULL,
  `last_modified_on_utc` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `m_delinquency_bucket_mappings` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `delinquency_range_id` bigint(20) NOT NULL,
  `delinquency_bucket_id` bigint(20) NOT NULL,
  `created_by` bigint(20) NOT NULL,
  `created_on_utc` datetime(6) DEFAULT NULL,
  `version` bigint(20) NOT NULL,
  `last_modified_by` bigint(20) NOT NULL,
  `last_modified_on_utc` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_m_delinquency_range_mapping` (`delinquency_range_id`),
  KEY `FK_m_delinquency_bucket_mapping` (`delinquency_bucket_id`),
  CONSTRAINT `FK_m_delinquency_bucket_mapping` FOREIGN KEY (`delinquency_bucket_id`) REFERENCES `m_delinquency_bucket` (`id`),
  CONSTRAINT `FK_m_delinquency_range_mapping` FOREIGN KEY (`delinquency_range_id`) REFERENCES `m_delinquency_range` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `m_delinquency_range` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `classification` varchar(100) NOT NULL,
  `min_age_days` bigint(20) NOT NULL,
  `max_age_days` bigint(20) DEFAULT NULL,
  `created_by` bigint(20) NOT NULL,
  `created_on_utc` datetime(6) DEFAULT NULL,
  `version` bigint(20) NOT NULL,
  `last_modified_by` bigint(20) NOT NULL,
  `last_modified_on_utc` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `classification` (`classification`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `m_deposit_account_on_hold_transaction` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `savings_account_id` bigint(20) NOT NULL,
  `amount` decimal(19,6) NOT NULL,
  `transaction_type_enum` smallint(6) NOT NULL,
  `transaction_date` date NOT NULL,
  `is_reversed` tinyint(1) NOT NULL DEFAULT 0,
  `created_date` datetime DEFAULT NULL,
  `created_by` bigint(20) NOT NULL,
  `last_modified_by` bigint(20) NOT NULL,
  `created_on_utc` datetime(6) NOT NULL,
  `last_modified_on_utc` datetime(6) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_deposit_on_hold_transaction_m_savings_account` (`savings_account_id`),
  KEY `FK_deposit_account_on_hold_transaction_created_by` (`created_by`),
  KEY `FK_deposit_account_on_hold_transaction_last_modified_by` (`last_modified_by`),
  CONSTRAINT `FK_deposit_account_on_hold_transaction_created_by` FOREIGN KEY (`created_by`) REFERENCES `m_appuser` (`id`),
  CONSTRAINT `FK_deposit_account_on_hold_transaction_last_modified_by` FOREIGN KEY (`last_modified_by`) REFERENCES `m_appuser` (`id`),
  CONSTRAINT `FK_deposit_on_hold_transaction_m_savings_account` FOREIGN KEY (`savings_account_id`) REFERENCES `m_savings_account` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `m_deposit_account_recurring_detail` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `savings_account_id` bigint(20) NOT NULL DEFAULT 0,
  `mandatory_recommended_deposit_amount` decimal(19,6) DEFAULT NULL,
  `is_mandatory` tinyint(1) NOT NULL DEFAULT 0,
  `allow_withdrawal` tinyint(1) NOT NULL DEFAULT 0,
  `adjust_advance_towards_future_payments` tinyint(1) NOT NULL DEFAULT 1,
  `is_calendar_inherited` tinyint(1) NOT NULL DEFAULT 0,
  `total_overdue_amount` decimal(19,6) DEFAULT NULL,
  `no_of_overdue_installments` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKDARD00000000000001` (`savings_account_id`),
  CONSTRAINT `FKDARD00000000000001` FOREIGN KEY (`savings_account_id`) REFERENCES `m_savings_account` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `m_deposit_account_term_and_preclosure` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `savings_account_id` bigint(20) NOT NULL DEFAULT 0,
  `min_deposit_term` int(11) DEFAULT NULL,
  `max_deposit_term` int(11) DEFAULT NULL,
  `min_deposit_term_type_enum` smallint(6) DEFAULT NULL,
  `max_deposit_term_type_enum` smallint(6) DEFAULT NULL,
  `in_multiples_of_deposit_term` int(11) DEFAULT NULL,
  `in_multiples_of_deposit_term_type_enum` smallint(6) DEFAULT NULL,
  `pre_closure_penal_applicable` tinyint(1) DEFAULT NULL,
  `pre_closure_penal_interest` decimal(19,6) DEFAULT NULL,
  `pre_closure_penal_interest_on_enum` smallint(6) DEFAULT NULL,
  `deposit_period` int(11) DEFAULT NULL,
  `deposit_period_frequency_enum` smallint(6) DEFAULT NULL,
  `deposit_amount` decimal(19,6) DEFAULT NULL,
  `maturity_amount` decimal(19,6) DEFAULT NULL,
  `maturity_date` date DEFAULT NULL,
  `on_account_closure_enum` smallint(6) DEFAULT NULL,
  `expected_firstdepositon_date` date DEFAULT NULL,
  `transfer_interest_to_linked_account` tinyint(1) NOT NULL DEFAULT 0,
  `transfer_to_savings_account_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKDATP00000000000001` (`savings_account_id`),
  KEY `transfer_to_savings_account_id` (`transfer_to_savings_account_id`),
  CONSTRAINT `FKDATP00000000000001` FOREIGN KEY (`savings_account_id`) REFERENCES `m_savings_account` (`id`),
  CONSTRAINT `m_deposit_account_term_and_preclosure_ibfk_1` FOREIGN KEY (`transfer_to_savings_account_id`) REFERENCES `m_savings_account` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `m_deposit_product_interest_rate_chart` (
  `deposit_product_id` bigint(20) NOT NULL,
  `interest_rate_chart_id` bigint(20) NOT NULL,
  UNIQUE KEY `deposit_product_id_interest_rate_chart_id` (`deposit_product_id`,`interest_rate_chart_id`),
  KEY `FKDPIRC00000000000002` (`interest_rate_chart_id`),
  CONSTRAINT `FKDPIRC00000000000001` FOREIGN KEY (`deposit_product_id`) REFERENCES `m_savings_product` (`id`),
  CONSTRAINT `FKDPIRC00000000000002` FOREIGN KEY (`interest_rate_chart_id`) REFERENCES `m_interest_rate_chart` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `m_deposit_product_recurring_detail` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `savings_product_id` bigint(20) NOT NULL DEFAULT 0,
  `is_mandatory` tinyint(1) NOT NULL DEFAULT 1,
  `allow_withdrawal` tinyint(1) NOT NULL DEFAULT 0,
  `adjust_advance_towards_future_payments` tinyint(1) NOT NULL DEFAULT 1,
  PRIMARY KEY (`id`),
  KEY `FKDPRD00000000000001` (`savings_product_id`),
  CONSTRAINT `FKDPRD00000000000001` FOREIGN KEY (`savings_product_id`) REFERENCES `m_savings_product` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `m_deposit_product_term_and_preclosure` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `savings_product_id` bigint(20) NOT NULL DEFAULT 0,
  `min_deposit_term` int(11) DEFAULT NULL,
  `max_deposit_term` int(11) DEFAULT NULL,
  `min_deposit_term_type_enum` smallint(6) DEFAULT NULL,
  `max_deposit_term_type_enum` smallint(6) DEFAULT NULL,
  `in_multiples_of_deposit_term` int(11) DEFAULT NULL,
  `in_multiples_of_deposit_term_type_enum` smallint(6) DEFAULT NULL,
  `pre_closure_penal_applicable` tinyint(1) DEFAULT NULL,
  `pre_closure_penal_interest` decimal(19,6) DEFAULT NULL,
  `pre_closure_penal_interest_on_enum` smallint(6) DEFAULT NULL,
  `min_deposit_amount` decimal(19,6) DEFAULT NULL,
  `max_deposit_amount` decimal(19,6) DEFAULT NULL,
  `deposit_amount` decimal(19,6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKDPTP00000000000001` (`savings_product_id`),
  CONSTRAINT `FKDPTP00000000000001` FOREIGN KEY (`savings_product_id`) REFERENCES `m_savings_product` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `m_document` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `parent_entity_type` varchar(50) NOT NULL,
  `parent_entity_id` int(11) NOT NULL DEFAULT 0,
  `name` varchar(250) NOT NULL,
  `file_name` varchar(250) NOT NULL,
  `size` int(11) DEFAULT 0,
  `type` varchar(500) DEFAULT NULL,
  `description` varchar(1000) DEFAULT NULL,
  `location` varchar(500) NOT NULL DEFAULT '0',
  `storage_type_enum` smallint(6) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `m_entity_datatable_check` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `application_table_name` varchar(200) NOT NULL,
  `x_registered_table_name` varchar(50) NOT NULL,
  `status_enum` int(11) NOT NULL,
  `system_defined` tinyint(1) NOT NULL DEFAULT 0,
  `product_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_entity_check` (`application_table_name`,`x_registered_table_name`,`status_enum`,`product_id`),
  KEY `product_id` (`product_id`),
  KEY `x_registered_table_name` (`x_registered_table_name`),
  CONSTRAINT `m_entity_datatable_check_ibfk_1` FOREIGN KEY (`x_registered_table_name`) REFERENCES `x_registered_table` (`registered_table_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `m_entity_relation` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `from_entity_type` int(11) NOT NULL,
  `to_entity_type` int(11) NOT NULL,
  `code_name` varchar(50) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `from_entity_type_to_entity_type_code_name` (`from_entity_type`,`to_entity_type`,`code_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `m_entity_to_entity_access` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `entity_type` varchar(50) NOT NULL,
  `entity_id` bigint(20) NOT NULL,
  `access_type_code_value_id` int(11) NOT NULL,
  `second_entity_type` varchar(50) NOT NULL,
  `second_entity_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id_uniq_m_entity_to_entity_access` (`entity_type`,`entity_id`,`access_type_code_value_id`,`second_entity_type`,`second_entity_id`),
  KEY `FK_access_type_code_m_code_value` (`access_type_code_value_id`),
  KEY `IDX_OFFICE` (`entity_type`,`entity_id`),
  CONSTRAINT `FK_access_type_code_m_code_value` FOREIGN KEY (`access_type_code_value_id`) REFERENCES `m_code_value` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `m_entity_to_entity_mapping` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `rel_id` bigint(20) NOT NULL DEFAULT 0,
  `from_id` bigint(20) NOT NULL DEFAULT 0,
  `to_id` bigint(20) NOT NULL,
  `start_date` date DEFAULT NULL,
  `end_date` date DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `rel_id_from_id_to_id` (`rel_id`,`from_id`,`to_id`),
  CONSTRAINT `FK__rel_id_m_entity_relation_id` FOREIGN KEY (`rel_id`) REFERENCES `m_entity_relation` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `m_external_event` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `type` varchar(100) NOT NULL,
  `created_at` timestamp(6) NULL DEFAULT NULL,
  `status` varchar(100) NOT NULL,
  `business_date` date NOT NULL,
  `data` blob NOT NULL,
  `idempotency_key` varchar(100) NOT NULL,
  `sent_at` timestamp(6) NULL DEFAULT NULL,
  `schema` varchar(300) NOT NULL,
  `category` varchar(100) NOT NULL,
  `aggregate_root_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `m_external_event_status_index` (`status`),
  KEY `m_external_event_business_date_index` (`business_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `m_external_event_configuration` (
  `type` varchar(100) NOT NULL,
  `enabled` tinyint(1) NOT NULL,
  PRIMARY KEY (`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `m_family_members` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `client_id` bigint(20) NOT NULL,
  `firstname` varchar(50) NOT NULL,
  `middlename` varchar(50) DEFAULT NULL,
  `lastname` varchar(50) DEFAULT NULL,
  `qualification` varchar(50) DEFAULT NULL,
  `relationship_cv_id` int(11) NOT NULL,
  `marital_status_cv_id` int(11) DEFAULT NULL,
  `gender_cv_id` int(11) DEFAULT NULL,
  `date_of_birth` date DEFAULT NULL,
  `age` int(11) DEFAULT NULL,
  `profession_cv_id` int(11) DEFAULT NULL,
  `mobile_number` varchar(50) DEFAULT NULL,
  `is_dependent` tinyint(1) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_m_family_members_client_id_m_client` (`client_id`),
  KEY `FK_m_family_members_gender_m_code_value` (`gender_cv_id`),
  KEY `FK_m_family_members_marital_status_m_code_value` (`marital_status_cv_id`),
  KEY `FK_m_family_members_profession_m_code_value` (`profession_cv_id`),
  KEY `FK_m_family_members_relationship_m_code_value` (`relationship_cv_id`),
  CONSTRAINT `FK_m_family_members_client_id_m_client` FOREIGN KEY (`client_id`) REFERENCES `m_client` (`id`),
  CONSTRAINT `FK_m_family_members_gender_m_code_value` FOREIGN KEY (`gender_cv_id`) REFERENCES `m_code_value` (`id`),
  CONSTRAINT `FK_m_family_members_marital_status_m_code_value` FOREIGN KEY (`marital_status_cv_id`) REFERENCES `m_code_value` (`id`),
  CONSTRAINT `FK_m_family_members_profession_m_code_value` FOREIGN KEY (`profession_cv_id`) REFERENCES `m_code_value` (`id`),
  CONSTRAINT `FK_m_family_members_relationship_m_code_value` FOREIGN KEY (`relationship_cv_id`) REFERENCES `m_code_value` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `m_field_configuration` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `entity` varchar(100) NOT NULL,
  `subentity` varchar(100) NOT NULL,
  `field` varchar(100) NOT NULL,
  `is_enabled` tinyint(1) NOT NULL,
  `is_mandatory` tinyint(1) NOT NULL,
  `validation_regex` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `m_floating_rates` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(200) NOT NULL,
  `is_base_lending_rate` tinyint(1) NOT NULL DEFAULT 0,
  `is_active` tinyint(1) NOT NULL DEFAULT 1,
  `created_by` bigint(20) NOT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `last_modified_by` bigint(20) NOT NULL,
  `lastmodified_date` datetime(6) DEFAULT NULL,
  `created_on_utc` datetime(6) DEFAULT NULL,
  `last_modified_on_utc` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`),
  KEY `FK_floating_rates_created_by` (`created_by`),
  KEY `FK_floating_rates_last_modified_by` (`last_modified_by`),
  CONSTRAINT `FK_floating_rates_created_by` FOREIGN KEY (`created_by`) REFERENCES `m_appuser` (`id`),
  CONSTRAINT `FK_floating_rates_last_modified_by` FOREIGN KEY (`last_modified_by`) REFERENCES `m_appuser` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `m_floating_rates_periods` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `floating_rates_id` bigint(20) NOT NULL,
  `from_date` date DEFAULT NULL,
  `interest_rate` decimal(19,6) NOT NULL,
  `is_differential_to_base_lending_rate` tinyint(1) NOT NULL DEFAULT 0,
  `is_active` tinyint(1) NOT NULL DEFAULT 1,
  `created_by` bigint(20) NOT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `last_modified_by` bigint(20) NOT NULL,
  `lastmodified_date` datetime(6) DEFAULT NULL,
  `created_on_utc` datetime(6) DEFAULT NULL,
  `last_modified_on_utc` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_mappings_m_floating_rates` (`floating_rates_id`),
  KEY `FK_floating_rates_period_created_by` (`created_by`),
  KEY `FK_floating_rates_period_last_modified_by` (`last_modified_by`),
  CONSTRAINT `FK_floating_rates_period_created_by` FOREIGN KEY (`created_by`) REFERENCES `m_appuser` (`id`),
  CONSTRAINT `FK_floating_rates_period_last_modified_by` FOREIGN KEY (`last_modified_by`) REFERENCES `m_appuser` (`id`),
  CONSTRAINT `FK_mappings_m_floating_rates` FOREIGN KEY (`floating_rates_id`) REFERENCES `m_floating_rates` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `m_fund` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) DEFAULT NULL,
  `external_id` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`),
  UNIQUE KEY `external_id` (`external_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `m_group` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `external_id` varchar(100) DEFAULT NULL,
  `status_enum` int(11) NOT NULL DEFAULT 300,
  `activation_date` date DEFAULT NULL,
  `office_id` bigint(20) NOT NULL,
  `staff_id` bigint(20) DEFAULT NULL,
  `parent_id` bigint(20) DEFAULT NULL,
  `level_id` int(11) NOT NULL,
  `display_name` varchar(100) NOT NULL,
  `hierarchy` varchar(100) DEFAULT NULL,
  `closure_reason_cv_id` int(11) DEFAULT NULL,
  `closedon_date` date DEFAULT NULL,
  `activatedon_userid` bigint(20) DEFAULT NULL,
  `submittedon_date` date DEFAULT NULL,
  `submittedon_userid` bigint(20) DEFAULT NULL,
  `closedon_userid` bigint(20) DEFAULT NULL,
  `account_no` varchar(20) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`display_name`,`level_id`),
  UNIQUE KEY `external_id` (`external_id`),
  UNIQUE KEY `uk_m_group_external_id` (`external_id`,`level_id`),
  KEY `FK_m_group_level` (`level_id`),
  KEY `FK_m_group_m_code` (`closure_reason_cv_id`),
  KEY `Parent_Id_reference` (`parent_id`),
  KEY `group_office_id` (`office_id`),
  KEY `staff_id` (`staff_id`),
  CONSTRAINT `FK_m_group_level` FOREIGN KEY (`level_id`) REFERENCES `m_group_level` (`id`),
  CONSTRAINT `FK_m_group_m_code` FOREIGN KEY (`closure_reason_cv_id`) REFERENCES `m_code_value` (`id`),
  CONSTRAINT `FK_m_group_m_staff` FOREIGN KEY (`staff_id`) REFERENCES `m_staff` (`id`),
  CONSTRAINT `Parent_Id_reference` FOREIGN KEY (`parent_id`) REFERENCES `m_group` (`id`),
  CONSTRAINT `m_group_ibfk_1` FOREIGN KEY (`office_id`) REFERENCES `m_office` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `m_group_client` (
  `group_id` bigint(20) NOT NULL,
  `client_id` bigint(20) NOT NULL,
  PRIMARY KEY (`group_id`,`client_id`),
  KEY `client_id` (`client_id`),
  CONSTRAINT `m_group_client_ibfk_1` FOREIGN KEY (`group_id`) REFERENCES `m_group` (`id`),
  CONSTRAINT `m_group_client_ibfk_2` FOREIGN KEY (`client_id`) REFERENCES `m_client` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `m_group_level` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `parent_id` int(11) DEFAULT NULL,
  `super_parent` tinyint(1) NOT NULL,
  `level_name` varchar(100) NOT NULL,
  `recursable` tinyint(1) NOT NULL,
  `can_have_clients` tinyint(1) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `Parent_levelId_reference` (`parent_id`),
  CONSTRAINT `Parent_levelId_reference` FOREIGN KEY (`parent_id`) REFERENCES `m_group_level` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `m_group_roles` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `client_id` bigint(20) DEFAULT NULL,
  `group_id` bigint(20) DEFAULT NULL,
  `role_cv_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UNIQUE_GROUP_ROLES` (`client_id`,`group_id`,`role_cv_id`),
  KEY `FKGroupRoleClientId` (`client_id`),
  KEY `FKGroupRoleGroupId` (`group_id`),
  KEY `FK_grouprole_m_codevalue` (`role_cv_id`),
  CONSTRAINT `FKGroupRoleClientId` FOREIGN KEY (`client_id`) REFERENCES `m_client` (`id`),
  CONSTRAINT `FKGroupRoleGroupId` FOREIGN KEY (`group_id`) REFERENCES `m_group` (`id`),
  CONSTRAINT `FK_grouprole_m_codevalue` FOREIGN KEY (`role_cv_id`) REFERENCES `m_code_value` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `m_guarantor` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `loan_id` bigint(20) NOT NULL,
  `client_reln_cv_id` int(11) DEFAULT NULL,
  `type_enum` smallint(6) NOT NULL,
  `entity_id` bigint(20) DEFAULT NULL,
  `firstname` varchar(50) DEFAULT NULL,
  `lastname` varchar(50) DEFAULT NULL,
  `dob` date DEFAULT NULL,
  `address_line_1` varchar(500) DEFAULT NULL,
  `address_line_2` varchar(500) DEFAULT NULL,
  `city` varchar(50) DEFAULT NULL,
  `state` varchar(50) DEFAULT NULL,
  `country` varchar(50) DEFAULT NULL,
  `zip` varchar(20) DEFAULT NULL,
  `house_phone_number` varchar(20) DEFAULT NULL,
  `mobile_number` varchar(20) DEFAULT NULL,
  `comment` varchar(500) DEFAULT NULL,
  `is_active` tinyint(1) NOT NULL DEFAULT 1,
  PRIMARY KEY (`id`),
  KEY `FK_m_guarantor_m_code_value` (`client_reln_cv_id`),
  KEY `FK_m_guarantor_m_loan` (`loan_id`),
  CONSTRAINT `FK_m_guarantor_m_code_value` FOREIGN KEY (`client_reln_cv_id`) REFERENCES `m_code_value` (`id`),
  CONSTRAINT `FK_m_guarantor_m_loan` FOREIGN KEY (`loan_id`) REFERENCES `m_loan` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `m_guarantor_funding_details` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `guarantor_id` bigint(20) NOT NULL,
  `account_associations_id` bigint(20) NOT NULL,
  `amount` decimal(19,6) NOT NULL,
  `amount_released_derived` decimal(19,6) DEFAULT NULL,
  `amount_remaining_derived` decimal(19,6) DEFAULT NULL,
  `amount_transfered_derived` decimal(19,6) DEFAULT NULL,
  `status_enum` smallint(6) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_m_guarantor_fund_details_account_associations_id` (`account_associations_id`),
  KEY `FK_m_guarantor_fund_details_m_guarantor` (`guarantor_id`),
  CONSTRAINT `FK_m_guarantor_fund_details_account_associations_id` FOREIGN KEY (`account_associations_id`) REFERENCES `m_portfolio_account_associations` (`id`),
  CONSTRAINT `FK_m_guarantor_fund_details_m_guarantor` FOREIGN KEY (`guarantor_id`) REFERENCES `m_guarantor` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `m_guarantor_transaction` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `guarantor_fund_detail_id` bigint(20) NOT NULL,
  `loan_transaction_id` bigint(20) DEFAULT NULL,
  `deposit_on_hold_transaction_id` bigint(20) NOT NULL,
  `is_reversed` tinyint(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `FK_guarantor_transaction_guarantor_fund_detail` (`guarantor_fund_detail_id`),
  KEY `FK_guarantor_transaction_m_deposit_account_on_hold_transaction` (`deposit_on_hold_transaction_id`),
  KEY `FK_guarantor_transaction_m_loan_transaction` (`loan_transaction_id`),
  CONSTRAINT `FK_guarantor_transaction_guarantor_fund_detail` FOREIGN KEY (`guarantor_fund_detail_id`) REFERENCES `m_guarantor_funding_details` (`id`),
  CONSTRAINT `FK_guarantor_transaction_m_deposit_account_on_hold_transaction` FOREIGN KEY (`deposit_on_hold_transaction_id`) REFERENCES `m_deposit_account_on_hold_transaction` (`id`),
  CONSTRAINT `FK_guarantor_transaction_m_loan_transaction` FOREIGN KEY (`loan_transaction_id`) REFERENCES `m_loan_transaction` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `m_holiday` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL,
  `from_date` date DEFAULT NULL,
  `to_date` date DEFAULT NULL,
  `repayments_rescheduled_to` date DEFAULT NULL,
  `status_enum` int(11) NOT NULL DEFAULT 100,
  `processed` tinyint(1) NOT NULL DEFAULT 0,
  `description` varchar(100) DEFAULT NULL,
  `rescheduling_type` int(11) NOT NULL DEFAULT 2,
  PRIMARY KEY (`id`),
  UNIQUE KEY `holiday_name` (`name`,`from_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `m_holiday_office` (
  `holiday_id` bigint(20) NOT NULL,
  `office_id` bigint(20) NOT NULL,
  PRIMARY KEY (`holiday_id`,`office_id`),
  KEY `m_holiday_id_ibfk_1` (`holiday_id`),
  KEY `m_office_id_ibfk_2` (`office_id`),
  CONSTRAINT `m_holiday_id_ibfk_1` FOREIGN KEY (`holiday_id`) REFERENCES `m_holiday` (`id`),
  CONSTRAINT `m_office_id_ibfk_2` FOREIGN KEY (`office_id`) REFERENCES `m_office` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `m_hook` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `template_id` smallint(6) NOT NULL,
  `is_active` tinyint(1) NOT NULL DEFAULT 1,
  `name` varchar(45) NOT NULL,
  `createdby_id` bigint(20) DEFAULT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `lastmodifiedby_id` bigint(20) DEFAULT NULL,
  `lastmodified_date` datetime(6) DEFAULT NULL,
  `ugd_template_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_template_id_idx` (`template_id`),
  KEY `fk_ugd_template_id` (`ugd_template_id`),
  CONSTRAINT `fk_template_id` FOREIGN KEY (`template_id`) REFERENCES `m_hook_templates` (`id`),
  CONSTRAINT `fk_ugd_template_id` FOREIGN KEY (`ugd_template_id`) REFERENCES `m_template` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `m_hook_configuration` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `hook_id` bigint(20) DEFAULT NULL,
  `field_type` varchar(45) NOT NULL,
  `field_name` varchar(100) NOT NULL,
  `field_value` varchar(100) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_hook_configuration_hook` (`hook_id`),
  CONSTRAINT `fk_hook_id_cfg` FOREIGN KEY (`hook_id`) REFERENCES `m_hook` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `m_hook_registered_events` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `hook_id` bigint(20) NOT NULL,
  `entity_name` varchar(45) NOT NULL,
  `action_name` varchar(45) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_hook_id_idx` (`hook_id`),
  CONSTRAINT `fk_hook_idc` FOREIGN KEY (`hook_id`) REFERENCES `m_hook` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `m_hook_schema` (
  `id` smallint(6) NOT NULL AUTO_INCREMENT,
  `hook_template_id` smallint(6) NOT NULL,
  `field_type` varchar(45) NOT NULL,
  `field_name` varchar(100) NOT NULL,
  `placeholder` varchar(100) DEFAULT NULL,
  `optional` tinyint(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `fk_hook_template_id_idx` (`hook_template_id`),
  CONSTRAINT `fk_hook_template_id` FOREIGN KEY (`hook_template_id`) REFERENCES `m_hook_templates` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `m_hook_templates` (
  `id` smallint(6) NOT NULL AUTO_INCREMENT,
  `name` varchar(45) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `m_image` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `location` varchar(500) DEFAULT NULL,
  `storage_type_enum` smallint(6) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `m_import_document` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `document_id` int(11) NOT NULL,
  `import_time` datetime(6) DEFAULT NULL,
  `end_time` datetime(6) DEFAULT NULL,
  `entity_type` tinyint(4) NOT NULL,
  `completed` tinyint(1) DEFAULT 0,
  `total_records` bigint(20) DEFAULT 0,
  `success_count` bigint(20) DEFAULT 0,
  `failure_count` bigint(20) DEFAULT 0,
  `createdby_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_m_import_m_appuser` (`createdby_id`),
  KEY `import_document_id` (`document_id`),
  CONSTRAINT `FK_m_import_m_appuser` FOREIGN KEY (`createdby_id`) REFERENCES `m_appuser` (`id`),
  CONSTRAINT `FK_m_import_m_document` FOREIGN KEY (`document_id`) REFERENCES `m_document` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `m_interest_incentives` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `interest_rate_slab_id` bigint(20) NOT NULL,
  `entiry_type` smallint(6) NOT NULL,
  `attribute_name` smallint(6) NOT NULL,
  `condition_type` smallint(6) NOT NULL,
  `attribute_value` varchar(50) NOT NULL,
  `incentive_type` smallint(6) NOT NULL,
  `amount` decimal(19,6) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_m_interest_incentives_m_interest_rate_slab` (`interest_rate_slab_id`),
  CONSTRAINT `FK_m_interest_incentives_m_interest_rate_slab` FOREIGN KEY (`interest_rate_slab_id`) REFERENCES `m_interest_rate_slab` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `m_interest_rate_chart` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(100) DEFAULT NULL,
  `description` varchar(200) DEFAULT NULL,
  `from_date` date NOT NULL,
  `end_date` date DEFAULT NULL,
  `is_primary_grouping_by_amount` tinyint(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `m_interest_rate_slab` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `interest_rate_chart_id` bigint(20) NOT NULL,
  `description` varchar(200) DEFAULT NULL,
  `period_type_enum` smallint(6) DEFAULT NULL,
  `from_period` int(11) DEFAULT NULL,
  `to_period` int(11) DEFAULT NULL,
  `amount_range_from` decimal(19,6) DEFAULT NULL,
  `amount_range_to` decimal(19,6) DEFAULT NULL,
  `annual_interest_rate` decimal(19,6) NOT NULL,
  `currency_code` varchar(3) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKIRS00000000000001` (`interest_rate_chart_id`),
  CONSTRAINT `FKIRS00000000000001` FOREIGN KEY (`interest_rate_chart_id`) REFERENCES `m_interest_rate_chart` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `m_journal_entry_aggregation_summary` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `gl_account_id` bigint(20) NOT NULL,
  `product_id` bigint(20) NOT NULL,
  `office_id` bigint(20) NOT NULL,
  `entity_type_enum` smallint(6) NOT NULL,
  `aggregated_on_date` date NOT NULL,
  `submitted_on_date` date NOT NULL,
  `external_owner_id` bigint(20) DEFAULT NULL,
  `debit_amount` decimal(19,6) DEFAULT NULL,
  `credit_amount` decimal(19,6) DEFAULT NULL,
  `manual_entry` tinyint(1) NOT NULL DEFAULT 0,
  `job_execution_id` bigint(20) NOT NULL,
  `created_by` bigint(20) DEFAULT NULL,
  `created_on_utc` datetime DEFAULT NULL,
  `last_modified_by` bigint(20) DEFAULT NULL,
  `last_modified_on_utc` datetime DEFAULT NULL,
  `originator_external_ids` varchar(1000) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_GL_JOURNAL_ENTRY_AGGREGATION_SUMMARY_ON_CREATED_BY` (`created_by`),
  KEY `FK_GL_JOURNAL_ENTRY_AGGREGATION_SUMMARY_ON_LAST_MODIFIED_BY` (`last_modified_by`),
  KEY `FK_GL_JOURNAL_ENTRY_AGGREGATION_SUMMARY_ON_OFFICE_ID` (`office_id`),
  KEY `FK_GL_JOURNAL_ENTRY_AGGREGATION_SUMMARY_ON_GL_ACCOUNT_ID` (`gl_account_id`),
  KEY `FK_GL_JOURNAL_ENTRY_AGGREGATION_SUMMARY_ON_JOB_EXECUTION_ID` (`job_execution_id`),
  KEY `idx_journal_entry_aggregation_summary` (`submitted_on_date`,`gl_account_id`),
  KEY `idx_m_jour_ent_aggr_sum_prod_id_entity_type` (`product_id`,`entity_type_enum`),
  KEY `idx_m_jour_ent_aggr_sum_aggr_date` (`aggregated_on_date`),
  CONSTRAINT `FK_GL_JOURNAL_ENTRY_AGGREGATION_SUMMARY_ON_CREATED_BY` FOREIGN KEY (`created_by`) REFERENCES `m_appuser` (`id`),
  CONSTRAINT `FK_GL_JOURNAL_ENTRY_AGGREGATION_SUMMARY_ON_GL_ACCOUNT_ID` FOREIGN KEY (`gl_account_id`) REFERENCES `acc_gl_account` (`id`),
  CONSTRAINT `FK_GL_JOURNAL_ENTRY_AGGREGATION_SUMMARY_ON_JOB_EXECUTION_ID` FOREIGN KEY (`job_execution_id`) REFERENCES `BATCH_JOB_EXECUTION` (`JOB_EXECUTION_ID`),
  CONSTRAINT `FK_GL_JOURNAL_ENTRY_AGGREGATION_SUMMARY_ON_LAST_MODIFIED_BY` FOREIGN KEY (`last_modified_by`) REFERENCES `m_appuser` (`id`),
  CONSTRAINT `FK_GL_JOURNAL_ENTRY_AGGREGATION_SUMMARY_ON_OFFICE_ID` FOREIGN KEY (`office_id`) REFERENCES `m_office` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `m_journal_entry_aggregation_tracking` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `aggregated_on_date_from` date NOT NULL,
  `aggregated_on_date_to` date NOT NULL,
  `submitted_on_date` date NOT NULL,
  `job_execution_id` bigint(20) NOT NULL,
  `created_by` bigint(20) DEFAULT NULL,
  `created_on_utc` datetime DEFAULT NULL,
  `last_modified_by` bigint(20) DEFAULT NULL,
  `last_modified_on_utc` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx2_m_journal_entry_aggregation_tracking` (`aggregated_on_date_to`),
  KEY `FK_GL_JOURNAL_ENTRY_AGGREGATION_TRACKING_ON_JOB_EXECUTION_ID` (`job_execution_id`),
  KEY `FK_GL_JOURNAL_ENTRY_AGGREGATION_TRACKING_ON_CREATED_BY` (`created_by`),
  KEY `FK_GL_JOURNAL_ENTRY_AGGREGATION_TRACKING_ON_LAST_MODIFIED_BY` (`last_modified_by`),
  KEY `idx_m_journal_entry_aggregation_tracking` (`submitted_on_date`),
  CONSTRAINT `FK_GL_JOURNAL_ENTRY_AGGREGATION_TRACKING_ON_CREATED_BY` FOREIGN KEY (`created_by`) REFERENCES `m_appuser` (`id`),
  CONSTRAINT `FK_GL_JOURNAL_ENTRY_AGGREGATION_TRACKING_ON_JOB_EXECUTION_ID` FOREIGN KEY (`job_execution_id`) REFERENCES `BATCH_JOB_EXECUTION` (`JOB_EXECUTION_ID`),
  CONSTRAINT `FK_GL_JOURNAL_ENTRY_AGGREGATION_TRACKING_ON_LAST_MODIFIED_BY` FOREIGN KEY (`last_modified_by`) REFERENCES `m_appuser` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `m_loan` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `account_no` varchar(20) NOT NULL,
  `external_id` varchar(100) DEFAULT NULL,
  `client_id` bigint(20) DEFAULT NULL,
  `group_id` bigint(20) DEFAULT NULL,
  `glim_id` bigint(20) DEFAULT NULL,
  `product_id` bigint(20) DEFAULT NULL,
  `fund_id` bigint(20) DEFAULT NULL,
  `loan_officer_id` bigint(20) DEFAULT NULL,
  `loanpurpose_cv_id` int(11) DEFAULT NULL,
  `loan_status_id` smallint(6) NOT NULL,
  `loan_type_enum` smallint(6) NOT NULL,
  `currency_code` varchar(3) NOT NULL,
  `currency_digits` smallint(6) NOT NULL,
  `currency_multiplesof` smallint(6) DEFAULT NULL,
  `principal_amount_proposed` decimal(19,6) NOT NULL,
  `principal_amount` decimal(19,6) NOT NULL,
  `approved_principal` decimal(19,6) NOT NULL,
  `net_disbursal_amount` decimal(19,6) NOT NULL,
  `arrearstolerance_amount` decimal(19,6) DEFAULT NULL,
  `is_floating_interest_rate` tinyint(1) DEFAULT 0,
  `interest_rate_differential` decimal(19,6) DEFAULT 0.000000,
  `nominal_interest_rate_per_period` decimal(19,6) DEFAULT NULL,
  `interest_period_frequency_enum` smallint(6) DEFAULT NULL,
  `annual_nominal_interest_rate` decimal(19,6) DEFAULT NULL,
  `interest_method_enum` smallint(6) NOT NULL,
  `interest_calculated_in_period_enum` smallint(6) NOT NULL DEFAULT 1,
  `allow_partial_period_interest_calcualtion` tinyint(1) NOT NULL DEFAULT 0,
  `term_frequency` smallint(6) NOT NULL DEFAULT 0,
  `term_period_frequency_enum` smallint(6) NOT NULL DEFAULT 2,
  `repay_every` smallint(6) NOT NULL,
  `repayment_period_frequency_enum` smallint(6) NOT NULL,
  `number_of_repayments` smallint(6) NOT NULL,
  `grace_on_principal_periods` smallint(6) DEFAULT NULL,
  `recurring_moratorium_principal_periods` smallint(6) DEFAULT NULL,
  `grace_on_interest_periods` smallint(6) DEFAULT NULL,
  `grace_interest_free_periods` smallint(6) DEFAULT NULL,
  `amortization_method_enum` smallint(6) NOT NULL,
  `submittedon_date` date DEFAULT NULL,
  `approvedon_date` date DEFAULT NULL,
  `approvedon_userid` bigint(20) DEFAULT NULL,
  `expected_disbursedon_date` date DEFAULT NULL,
  `expected_firstrepaymenton_date` date DEFAULT NULL,
  `interest_calculated_from_date` date DEFAULT NULL,
  `disbursedon_date` date DEFAULT NULL,
  `disbursedon_userid` bigint(20) DEFAULT NULL,
  `expected_maturedon_date` date DEFAULT NULL,
  `maturedon_date` date DEFAULT NULL,
  `closedon_date` date DEFAULT NULL,
  `closedon_userid` bigint(20) DEFAULT NULL,
  `total_charges_due_at_disbursement_derived` decimal(19,6) DEFAULT NULL,
  `principal_disbursed_derived` decimal(19,6) NOT NULL DEFAULT 0.000000,
  `principal_repaid_derived` decimal(19,6) NOT NULL DEFAULT 0.000000,
  `principal_writtenoff_derived` decimal(19,6) NOT NULL DEFAULT 0.000000,
  `principal_outstanding_derived` decimal(19,6) NOT NULL DEFAULT 0.000000,
  `interest_charged_derived` decimal(19,6) NOT NULL DEFAULT 0.000000,
  `interest_repaid_derived` decimal(19,6) NOT NULL DEFAULT 0.000000,
  `interest_waived_derived` decimal(19,6) NOT NULL DEFAULT 0.000000,
  `interest_writtenoff_derived` decimal(19,6) NOT NULL DEFAULT 0.000000,
  `interest_outstanding_derived` decimal(19,6) NOT NULL DEFAULT 0.000000,
  `fee_charges_charged_derived` decimal(19,6) NOT NULL DEFAULT 0.000000,
  `fee_charges_repaid_derived` decimal(19,6) NOT NULL DEFAULT 0.000000,
  `fee_charges_waived_derived` decimal(19,6) NOT NULL DEFAULT 0.000000,
  `fee_charges_writtenoff_derived` decimal(19,6) NOT NULL DEFAULT 0.000000,
  `fee_charges_outstanding_derived` decimal(19,6) NOT NULL DEFAULT 0.000000,
  `penalty_charges_charged_derived` decimal(19,6) NOT NULL DEFAULT 0.000000,
  `penalty_charges_repaid_derived` decimal(19,6) NOT NULL DEFAULT 0.000000,
  `penalty_charges_waived_derived` decimal(19,6) NOT NULL DEFAULT 0.000000,
  `penalty_charges_writtenoff_derived` decimal(19,6) NOT NULL DEFAULT 0.000000,
  `penalty_charges_outstanding_derived` decimal(19,6) NOT NULL DEFAULT 0.000000,
  `total_expected_repayment_derived` decimal(19,6) NOT NULL DEFAULT 0.000000,
  `total_repayment_derived` decimal(19,6) NOT NULL DEFAULT 0.000000,
  `total_expected_costofloan_derived` decimal(19,6) NOT NULL DEFAULT 0.000000,
  `total_costofloan_derived` decimal(19,6) NOT NULL DEFAULT 0.000000,
  `total_waived_derived` decimal(19,6) NOT NULL DEFAULT 0.000000,
  `total_writtenoff_derived` decimal(19,6) NOT NULL DEFAULT 0.000000,
  `total_outstanding_derived` decimal(19,6) NOT NULL DEFAULT 0.000000,
  `total_overpaid_derived` decimal(19,6) DEFAULT NULL,
  `rejectedon_date` date DEFAULT NULL,
  `rejectedon_userid` bigint(20) DEFAULT NULL,
  `rescheduledon_date` date DEFAULT NULL,
  `rescheduledon_userid` bigint(20) DEFAULT NULL,
  `withdrawnon_date` date DEFAULT NULL,
  `withdrawnon_userid` bigint(20) DEFAULT NULL,
  `writtenoffon_date` date DEFAULT NULL,
  `loan_transaction_strategy_id` bigint(20) DEFAULT NULL,
  `sync_disbursement_with_meeting` tinyint(1) DEFAULT NULL,
  `loan_counter` smallint(6) DEFAULT NULL,
  `loan_product_counter` smallint(6) DEFAULT NULL,
  `fixed_emi_amount` decimal(19,6) DEFAULT NULL,
  `max_outstanding_loan_balance` decimal(19,6) DEFAULT NULL,
  `grace_on_arrears_ageing` smallint(6) DEFAULT NULL,
  `is_npa` tinyint(1) NOT NULL DEFAULT 0,
  `total_recovered_derived` decimal(19,6) DEFAULT NULL,
  `accrued_till` date DEFAULT NULL,
  `interest_recalcualated_on` date DEFAULT NULL,
  `days_in_month_enum` smallint(6) NOT NULL DEFAULT 1,
  `days_in_year_enum` smallint(6) NOT NULL DEFAULT 1,
  `interest_recalculation_enabled` tinyint(1) NOT NULL DEFAULT 0,
  `guarantee_amount_derived` decimal(19,6) DEFAULT NULL,
  `create_standing_instruction_at_disbursement` tinyint(1) DEFAULT NULL,
  `version` int(11) NOT NULL DEFAULT 1,
  `writeoff_reason_cv_id` int(11) DEFAULT NULL,
  `loan_sub_status_id` smallint(6) DEFAULT NULL,
  `is_topup` tinyint(1) NOT NULL DEFAULT 0,
  `is_equal_amortization` tinyint(1) NOT NULL DEFAULT 0,
  `fixed_principal_percentage_per_installment` decimal(5,2) DEFAULT NULL,
  `created_on_utc` datetime(6) DEFAULT NULL,
  `created_by` bigint(20) NOT NULL,
  `last_modified_by` bigint(20) NOT NULL,
  `last_modified_on_utc` datetime(6) DEFAULT NULL,
  `principal_adjustments_derived` decimal(19,6) NOT NULL DEFAULT 0.000000,
  `is_fraud` tinyint(1) NOT NULL DEFAULT 0,
  `loan_transaction_strategy_code` varchar(100) NOT NULL DEFAULT '-',
  `loan_transaction_strategy_name` varchar(100) NOT NULL DEFAULT '-',
  `last_closed_business_date` date DEFAULT NULL,
  `overpaidon_date` date DEFAULT NULL,
  `is_charged_off` tinyint(1) NOT NULL DEFAULT 0,
  `charged_off_on_date` date DEFAULT NULL,
  `charge_off_reason_cv_id` bigint(20) DEFAULT NULL,
  `charged_off_by_userid` bigint(20) DEFAULT NULL,
  `enable_down_payment` tinyint(1) NOT NULL DEFAULT 0,
  `disbursed_amount_percentage_for_down_payment` decimal(9,6) DEFAULT NULL,
  `enable_installment_level_delinquency` tinyint(1) NOT NULL DEFAULT 0,
  `enable_accrual_activity_posting` tinyint(1) NOT NULL DEFAULT 0,
  `days_in_year_custom_strategy` varchar(100) DEFAULT NULL,
  `enable_income_capitalization` tinyint(1) NOT NULL DEFAULT 0,
  `capitalized_income_calculation_type` varchar(100) DEFAULT NULL,
  `capitalized_income_strategy` varchar(100) DEFAULT NULL,
  `capitalized_income_type` varchar(10) DEFAULT NULL,
  `capitalized_income_derived` decimal(19,6) NOT NULL DEFAULT 0.000000,
  `capitalized_income_adjustment_derived` decimal(19,6) NOT NULL DEFAULT 0.000000,
  `total_principal_derived` decimal(19,6) NOT NULL DEFAULT 0.000000,
  `enable_buy_down_fee` tinyint(1) NOT NULL DEFAULT 0,
  `buy_down_fee_calculation_type` varchar(100) DEFAULT NULL,
  `buy_down_fee_strategy` varchar(100) DEFAULT NULL,
  `buy_down_fee_income_type` varchar(100) DEFAULT NULL,
  `allow_full_term_for_tranche` tinyint(1) NOT NULL DEFAULT 0,
  `repayment_start_date_type_enum` smallint(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `account_no` (`account_no`),
  UNIQUE KEY `external_id` (`external_id`),
  KEY `FK7C885877240145` (`fund_id`),
  KEY `FKB6F935D87179A0CB` (`client_id`),
  KEY `FKB6F935D8C8D4B434` (`product_id`),
  KEY `FK_approvedon_userid` (`approvedon_userid`),
  KEY `FK_closedon_userid` (`closedon_userid`),
  KEY `FK_disbursedon_userid` (`disbursedon_userid`),
  KEY `FK_glim_id` (`glim_id`),
  KEY `FK_loan_ltp_strategy` (`loan_transaction_strategy_id`),
  KEY `FK_m_loan_m_staff` (`loan_officer_id`),
  KEY `FK_m_loanpurpose_codevalue` (`loanpurpose_cv_id`),
  KEY `FK_rejectedon_userid` (`rejectedon_userid`),
  KEY `FK_withdrawnon_userid` (`withdrawnon_userid`),
  KEY `FK_writeoffreason_m_loan_m_code_value` (`writeoff_reason_cv_id`),
  KEY `fk_m_group_client_001_idx` (`group_id`,`client_id`),
  KEY `group_id` (`group_id`),
  KEY `FK_loan_created_by` (`created_by`),
  KEY `FK_loan_last_modified_by` (`last_modified_by`),
  KEY `FK_loan_status_id` (`loan_status_id`),
  KEY `FK_loan_expected_maturedon_date` (`expected_maturedon_date`),
  KEY `FK_loan_last_closed_business_date` (`last_closed_business_date`),
  KEY `FK_loan_overpaidon_date` (`overpaidon_date`),
  CONSTRAINT `FK7C885877240145` FOREIGN KEY (`fund_id`) REFERENCES `m_fund` (`id`),
  CONSTRAINT `FKB6F935D87179A0CB` FOREIGN KEY (`client_id`) REFERENCES `m_client` (`id`),
  CONSTRAINT `FKB6F935D8C8D4B434` FOREIGN KEY (`product_id`) REFERENCES `m_product_loan` (`id`),
  CONSTRAINT `FK_approvedon_userid` FOREIGN KEY (`approvedon_userid`) REFERENCES `m_appuser` (`id`),
  CONSTRAINT `FK_closedon_userid` FOREIGN KEY (`closedon_userid`) REFERENCES `m_appuser` (`id`),
  CONSTRAINT `FK_disbursedon_userid` FOREIGN KEY (`disbursedon_userid`) REFERENCES `m_appuser` (`id`),
  CONSTRAINT `FK_glim_id` FOREIGN KEY (`glim_id`) REFERENCES `glim_accounts` (`id`),
  CONSTRAINT `FK_loan_created_by` FOREIGN KEY (`created_by`) REFERENCES `m_appuser` (`id`),
  CONSTRAINT `FK_loan_last_modified_by` FOREIGN KEY (`last_modified_by`) REFERENCES `m_appuser` (`id`),
  CONSTRAINT `FK_m_loan_m_staff` FOREIGN KEY (`loan_officer_id`) REFERENCES `m_staff` (`id`),
  CONSTRAINT `FK_m_loanpurpose_codevalue` FOREIGN KEY (`loanpurpose_cv_id`) REFERENCES `m_code_value` (`id`),
  CONSTRAINT `FK_rejectedon_userid` FOREIGN KEY (`rejectedon_userid`) REFERENCES `m_appuser` (`id`),
  CONSTRAINT `FK_withdrawnon_userid` FOREIGN KEY (`withdrawnon_userid`) REFERENCES `m_appuser` (`id`),
  CONSTRAINT `FK_writeoffreason_m_loan_m_code_value` FOREIGN KEY (`writeoff_reason_cv_id`) REFERENCES `m_code_value` (`id`),
  CONSTRAINT `m_loan_ibfk_1` FOREIGN KEY (`group_id`) REFERENCES `m_group` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `m_loan_account_locks` (
  `loan_id` bigint(20) NOT NULL,
  `lock_owner` varchar(100) NOT NULL,
  `error` varchar(255) DEFAULT NULL,
  `version` bigint(20) NOT NULL,
  `stacktrace` text DEFAULT NULL,
  `lock_placed_on` datetime(6) DEFAULT NULL,
  `lock_placed_on_cob_business_date` date DEFAULT NULL,
  PRIMARY KEY (`loan_id`),
  CONSTRAINT `fk_loan_id` FOREIGN KEY (`loan_id`) REFERENCES `m_loan` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `m_loan_amortization_allocation_mapping` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `loan_id` bigint(20) NOT NULL,
  `base_loan_transaction_id` bigint(20) NOT NULL,
  `date` date NOT NULL,
  `amortization_loan_transaction_id` bigint(20) NOT NULL,
  `amortization_type` varchar(20) NOT NULL,
  `amount` decimal(19,6) NOT NULL,
  `created_by` bigint(20) DEFAULT NULL,
  `created_on_utc` datetime DEFAULT NULL,
  `last_modified_by` bigint(20) DEFAULT NULL,
  `last_modified_on_utc` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_laam_created_by` (`created_by`),
  KEY `FK_laam_last_modified_by` (`last_modified_by`),
  KEY `FK_laam_loan_id` (`loan_id`),
  KEY `FK_laam_base_transaction_id` (`base_loan_transaction_id`),
  KEY `FK_laam_amortization_transaction_id` (`amortization_loan_transaction_id`),
  CONSTRAINT `FK_laam_amortization_transaction_id` FOREIGN KEY (`amortization_loan_transaction_id`) REFERENCES `m_loan_transaction` (`id`),
  CONSTRAINT `FK_laam_base_transaction_id` FOREIGN KEY (`base_loan_transaction_id`) REFERENCES `m_loan_transaction` (`id`),
  CONSTRAINT `FK_laam_created_by` FOREIGN KEY (`created_by`) REFERENCES `m_appuser` (`id`),
  CONSTRAINT `FK_laam_last_modified_by` FOREIGN KEY (`last_modified_by`) REFERENCES `m_appuser` (`id`),
  CONSTRAINT `FK_laam_loan_id` FOREIGN KEY (`loan_id`) REFERENCES `m_loan` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `m_loan_approved_amount_history` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `loan_id` bigint(20) NOT NULL,
  `new_approved_amount` decimal(19,6) NOT NULL,
  `old_approved_amount` decimal(19,6) NOT NULL,
  `created_by` bigint(20) DEFAULT NULL,
  `created_on_utc` datetime DEFAULT NULL,
  `last_modified_by` bigint(20) DEFAULT NULL,
  `last_modified_on_utc` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_loan_approved_amount_history_created_by` (`created_by`),
  KEY `FK_loan_approved_amount_history_last_modified_by` (`last_modified_by`),
  KEY `FK_loan_approved_amount_history_loan_id` (`loan_id`),
  CONSTRAINT `FK_loan_approved_amount_history_created_by` FOREIGN KEY (`created_by`) REFERENCES `m_appuser` (`id`),
  CONSTRAINT `FK_loan_approved_amount_history_last_modified_by` FOREIGN KEY (`last_modified_by`) REFERENCES `m_appuser` (`id`),
  CONSTRAINT `FK_loan_approved_amount_history_loan_id` FOREIGN KEY (`loan_id`) REFERENCES `m_loan` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `m_loan_arrears_aging` (
  `loan_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `principal_overdue_derived` decimal(19,6) NOT NULL DEFAULT 0.000000,
  `interest_overdue_derived` decimal(19,6) NOT NULL DEFAULT 0.000000,
  `fee_charges_overdue_derived` decimal(19,6) NOT NULL DEFAULT 0.000000,
  `penalty_charges_overdue_derived` decimal(19,6) NOT NULL DEFAULT 0.000000,
  `total_overdue_derived` decimal(19,6) NOT NULL DEFAULT 0.000000,
  `overdue_since_date_derived` date DEFAULT NULL,
  PRIMARY KEY (`loan_id`),
  CONSTRAINT `m_loan_arrears_aging_ibfk_1` FOREIGN KEY (`loan_id`) REFERENCES `m_loan` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `m_loan_buy_down_fee_balance` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `version` bigint(20) NOT NULL,
  `loan_id` bigint(20) NOT NULL,
  `loan_transaction_id` bigint(20) NOT NULL,
  `amount` decimal(19,6) NOT NULL,
  `date` date NOT NULL,
  `unrecognized_amount` decimal(19,6) NOT NULL,
  `charged_off_amount` decimal(19,6) DEFAULT NULL,
  `amount_adjustment` decimal(19,6) DEFAULT NULL,
  `created_by` bigint(20) DEFAULT NULL,
  `created_on_utc` datetime DEFAULT NULL,
  `last_modified_by` bigint(20) DEFAULT NULL,
  `last_modified_on_utc` datetime DEFAULT NULL,
  `is_deleted` tinyint(1) NOT NULL DEFAULT 0,
  `is_closed` tinyint(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `m_loan_buy_down_fee_balance_idx_1` (`loan_id`,`loan_transaction_id`),
  KEY `FK_loan_buy_down_fees_balance_created_by` (`created_by`),
  KEY `FK_loan_buy_down_fees_balance_last_modified_by` (`last_modified_by`),
  KEY `FK_loan_buy_down_fees_balance_transaction_id` (`loan_transaction_id`),
  KEY `m_loan_buy_down_fee_balance_idx_2` (`loan_id`),
  CONSTRAINT `FK_loan_buy_down_fees_balance_created_by` FOREIGN KEY (`created_by`) REFERENCES `m_appuser` (`id`),
  CONSTRAINT `FK_loan_buy_down_fees_balance_last_modified_by` FOREIGN KEY (`last_modified_by`) REFERENCES `m_appuser` (`id`),
  CONSTRAINT `FK_loan_buy_down_fees_balance_loan_id` FOREIGN KEY (`loan_id`) REFERENCES `m_loan` (`id`),
  CONSTRAINT `FK_loan_buy_down_fees_balance_transaction_id` FOREIGN KEY (`loan_transaction_id`) REFERENCES `m_loan_transaction` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `m_loan_capitalized_income_balance` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `version` bigint(20) NOT NULL,
  `loan_id` bigint(20) NOT NULL,
  `loan_transaction_id` bigint(20) NOT NULL,
  `amount` decimal(19,6) NOT NULL,
  `date` date NOT NULL,
  `unrecognized_amount` decimal(19,6) NOT NULL,
  `charged_off_amount` decimal(19,6) DEFAULT NULL,
  `amount_adjustment` decimal(19,6) DEFAULT NULL,
  `created_by` bigint(20) DEFAULT NULL,
  `created_on_utc` datetime DEFAULT NULL,
  `last_modified_by` bigint(20) DEFAULT NULL,
  `last_modified_on_utc` datetime DEFAULT NULL,
  `is_deleted` tinyint(1) NOT NULL DEFAULT 0,
  `is_closed` tinyint(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `m_loan_capitalized_income_balance_idx_1` (`loan_id`,`loan_transaction_id`),
  KEY `FK_loan_capitalized_income_balance_created_by` (`created_by`),
  KEY `FK_loan_capitalized_income_balance_last_modified_by` (`last_modified_by`),
  KEY `FK_loan_capitalized_income_balance_transaction_id` (`loan_transaction_id`),
  KEY `m_loan_capitalized_income_balance_idx_2` (`loan_id`),
  CONSTRAINT `FK_loan_capitalized_income_balance_created_by` FOREIGN KEY (`created_by`) REFERENCES `m_appuser` (`id`),
  CONSTRAINT `FK_loan_capitalized_income_balance_last_modified_by` FOREIGN KEY (`last_modified_by`) REFERENCES `m_appuser` (`id`),
  CONSTRAINT `FK_loan_capitalized_income_balance_loan_id` FOREIGN KEY (`loan_id`) REFERENCES `m_loan` (`id`),
  CONSTRAINT `FK_loan_capitalized_income_balance_transaction_id` FOREIGN KEY (`loan_transaction_id`) REFERENCES `m_loan_transaction` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `m_loan_charge` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `loan_id` bigint(20) NOT NULL,
  `charge_id` bigint(20) NOT NULL,
  `is_penalty` tinyint(1) NOT NULL DEFAULT 0,
  `charge_time_enum` smallint(6) NOT NULL,
  `due_for_collection_as_of_date` date DEFAULT NULL,
  `charge_calculation_enum` smallint(6) NOT NULL,
  `charge_payment_mode_enum` smallint(6) NOT NULL DEFAULT 0,
  `calculation_percentage` decimal(19,6) DEFAULT NULL,
  `calculation_on_amount` decimal(19,6) DEFAULT NULL,
  `charge_amount_or_percentage` decimal(19,6) DEFAULT NULL,
  `amount` decimal(19,6) NOT NULL,
  `amount_paid_derived` decimal(19,6) DEFAULT NULL,
  `amount_waived_derived` decimal(19,6) DEFAULT NULL,
  `amount_writtenoff_derived` decimal(19,6) DEFAULT NULL,
  `amount_outstanding_derived` decimal(19,6) NOT NULL DEFAULT 0.000000,
  `is_paid_derived` tinyint(1) NOT NULL DEFAULT 0,
  `waived` tinyint(1) NOT NULL DEFAULT 0,
  `min_cap` decimal(19,6) DEFAULT NULL,
  `max_cap` decimal(19,6) DEFAULT NULL,
  `is_active` tinyint(1) NOT NULL DEFAULT 1,
  `external_id` varchar(100) DEFAULT NULL,
  `submitted_on_date` date DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `external_id` (`external_id`),
  KEY `charge_id` (`charge_id`),
  KEY `m_loan_charge_ibfk_2` (`loan_id`),
  CONSTRAINT `m_loan_charge_ibfk_1` FOREIGN KEY (`charge_id`) REFERENCES `m_charge` (`id`),
  CONSTRAINT `m_loan_charge_ibfk_2` FOREIGN KEY (`loan_id`) REFERENCES `m_loan` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `m_loan_charge_paid_by` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `loan_transaction_id` bigint(20) NOT NULL,
  `loan_charge_id` bigint(20) NOT NULL,
  `amount` decimal(19,6) NOT NULL,
  `installment_number` smallint(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK__m_loan_charge` (`loan_charge_id`),
  KEY `FK__m_loan_transaction` (`loan_transaction_id`),
  CONSTRAINT `FK__m_loan_charge` FOREIGN KEY (`loan_charge_id`) REFERENCES `m_loan_charge` (`id`),
  CONSTRAINT `FK__m_loan_transaction` FOREIGN KEY (`loan_transaction_id`) REFERENCES `m_loan_transaction` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `m_loan_collateral` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `loan_id` bigint(20) NOT NULL,
  `type_cv_id` int(11) NOT NULL,
  `value` decimal(19,6) DEFAULT NULL,
  `description` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_collateral_code_value` (`type_cv_id`),
  KEY `FK_collateral_m_loan` (`loan_id`),
  CONSTRAINT `FK_collateral_code_value` FOREIGN KEY (`type_cv_id`) REFERENCES `m_code_value` (`id`),
  CONSTRAINT `FK_collateral_m_loan` FOREIGN KEY (`loan_id`) REFERENCES `m_loan` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `m_loan_collateral_management` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `quantity` decimal(20,5) NOT NULL,
  `loan_id` bigint(20) DEFAULT NULL,
  `client_collateral_id` bigint(20) DEFAULT NULL,
  `is_released` tinyint(1) DEFAULT 0,
  `transaction_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fkClientCollateral` (`client_collateral_id`),
  KEY `fkLoan` (`loan_id`),
  KEY `fkTransaction` (`transaction_id`),
  CONSTRAINT `fkClientCollateral` FOREIGN KEY (`client_collateral_id`) REFERENCES `m_client_collateral_management` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fkLoan` FOREIGN KEY (`loan_id`) REFERENCES `m_loan` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fkTransaction` FOREIGN KEY (`transaction_id`) REFERENCES `m_loan_transaction` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `m_loan_delinquency_tag_history` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `delinquency_range_id` bigint(20) NOT NULL,
  `loan_id` bigint(20) NOT NULL,
  `addedon_date` date NOT NULL,
  `liftedon_date` date DEFAULT NULL,
  `created_by` bigint(20) NOT NULL,
  `created_on_utc` datetime(6) DEFAULT NULL,
  `version` bigint(20) NOT NULL,
  `last_modified_by` bigint(20) NOT NULL,
  `last_modified_on_utc` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_m_delinquency_tags_range` (`delinquency_range_id`),
  KEY `ind_m_loan_delinquency_tag_history_loan_id` (`loan_id`),
  KEY `ind_m_loan_delinquency_tag_history_liftedon_date` (`liftedon_date`),
  CONSTRAINT `FK_m_delinquency_tags_loan` FOREIGN KEY (`loan_id`) REFERENCES `m_loan` (`id`),
  CONSTRAINT `FK_m_delinquency_tags_range` FOREIGN KEY (`delinquency_range_id`) REFERENCES `m_delinquency_range` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `m_loan_disbursement_detail` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `loan_id` bigint(20) NOT NULL,
  `expected_disburse_date` date DEFAULT NULL,
  `disbursedon_date` date DEFAULT NULL,
  `principal` decimal(19,6) NOT NULL,
  `net_disbursal_amount` decimal(19,6) DEFAULT NULL,
  `is_reversed` tinyint(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `FK_loan_disbursement_detail_loan_id` (`loan_id`),
  CONSTRAINT `FK_loan_disbursement_detail_loan_id` FOREIGN KEY (`loan_id`) REFERENCES `m_loan` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `m_loan_installment_charge` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `loan_charge_id` bigint(20) NOT NULL,
  `loan_schedule_id` bigint(20) NOT NULL,
  `due_date` date DEFAULT NULL,
  `amount` decimal(19,6) NOT NULL,
  `amount_paid_derived` decimal(19,6) DEFAULT NULL,
  `amount_waived_derived` decimal(19,6) DEFAULT NULL,
  `amount_writtenoff_derived` decimal(19,6) DEFAULT NULL,
  `amount_outstanding_derived` decimal(19,6) NOT NULL DEFAULT 0.000000,
  `is_paid_derived` tinyint(1) NOT NULL DEFAULT 0,
  `waived` tinyint(1) NOT NULL DEFAULT 0,
  `amount_through_charge_payment` decimal(19,6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_loan_charge_id_charge_schedule` (`loan_charge_id`),
  KEY `FK_loan_schedule_id_charge_schedule` (`loan_schedule_id`),
  CONSTRAINT `FK_loan_charge_id_charge_schedule` FOREIGN KEY (`loan_charge_id`) REFERENCES `m_loan_charge` (`id`),
  CONSTRAINT `FK_loan_schedule_id_charge_schedule` FOREIGN KEY (`loan_schedule_id`) REFERENCES `m_loan_repayment_schedule` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `m_loan_interest_recalculation_additional_details` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `loan_repayment_schedule_id` bigint(20) NOT NULL,
  `effective_date` date NOT NULL,
  `amount` decimal(19,6) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_additional_details_repayment_schedule_id` (`loan_repayment_schedule_id`),
  CONSTRAINT `FK_additional_details_repayment_schedule_id` FOREIGN KEY (`loan_repayment_schedule_id`) REFERENCES `m_loan_repayment_schedule` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `m_loan_officer_assignment_history` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `loan_id` bigint(20) NOT NULL,
  `loan_officer_id` bigint(20) DEFAULT NULL,
  `start_date` date NOT NULL,
  `end_date` date DEFAULT NULL,
  `createdby_id` bigint(20) DEFAULT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `lastmodified_date` datetime(6) DEFAULT NULL,
  `lastmodifiedby_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_m_loan_officer_assignment_history_0001` (`loan_id`),
  KEY `fk_m_loan_officer_assignment_history_0002` (`loan_officer_id`),
  CONSTRAINT `fk_m_loan_officer_assignment_history_0001` FOREIGN KEY (`loan_id`) REFERENCES `m_loan` (`id`),
  CONSTRAINT `fk_m_loan_officer_assignment_history_0002` FOREIGN KEY (`loan_officer_id`) REFERENCES `m_staff` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `m_loan_overdue_installment_charge` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `loan_charge_id` bigint(20) NOT NULL,
  `loan_schedule_id` bigint(20) NOT NULL,
  `frequency_number` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_m_loan_overdue_installment_charge_m_loan_charge` (`loan_charge_id`),
  KEY `FK_m_loan_overdue_installment_charge_m_loan_repayment_schedule` (`loan_schedule_id`),
  CONSTRAINT `FK_m_loan_overdue_installment_charge_m_loan_charge` FOREIGN KEY (`loan_charge_id`) REFERENCES `m_loan_charge` (`id`),
  CONSTRAINT `FK_m_loan_overdue_installment_charge_m_loan_repayment_schedule` FOREIGN KEY (`loan_schedule_id`) REFERENCES `m_loan_repayment_schedule` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `m_loan_rate` (
  `loan_id` bigint(20) NOT NULL,
  `rate_id` bigint(20) NOT NULL,
  PRIMARY KEY (`loan_id`,`rate_id`),
  KEY `FK_M_LOAN_RATE_RATE` (`rate_id`),
  CONSTRAINT `FK_M_LOAN_RATE_LOAN` FOREIGN KEY (`loan_id`) REFERENCES `m_loan` (`id`),
  CONSTRAINT `FK_M_LOAN_RATE_RATE` FOREIGN KEY (`rate_id`) REFERENCES `m_rate` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `m_loan_reage_parameter` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `frequency_type` varchar(100) DEFAULT NULL,
  `number_of_installments` smallint(6) NOT NULL,
  `start_date` date NOT NULL,
  `created_by` bigint(20) NOT NULL,
  `last_modified_by` bigint(20) NOT NULL,
  `created_on_utc` datetime(6) NOT NULL,
  `last_modified_on_utc` datetime(6) NOT NULL,
  `loan_transaction_id` bigint(20) NOT NULL,
  `frequency_number` smallint(6) NOT NULL DEFAULT 1,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `m_loan_recalculation_details` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `loan_id` bigint(20) NOT NULL,
  `compound_type_enum` smallint(6) NOT NULL,
  `reschedule_strategy_enum` smallint(6) NOT NULL,
  `rest_frequency_type_enum` smallint(6) NOT NULL,
  `rest_frequency_interval` smallint(6) NOT NULL DEFAULT 0,
  `compounding_frequency_type_enum` smallint(6) DEFAULT NULL,
  `compounding_frequency_interval` smallint(6) DEFAULT NULL,
  `rest_frequency_nth_day_enum` int(11) DEFAULT NULL,
  `rest_frequency_on_day` int(11) DEFAULT NULL,
  `rest_frequency_weekday_enum` int(11) DEFAULT NULL,
  `compounding_frequency_nth_day_enum` int(11) DEFAULT NULL,
  `compounding_frequency_on_day` int(11) DEFAULT NULL,
  `is_compounding_to_be_posted_as_transaction` tinyint(1) NOT NULL DEFAULT 0,
  `compounding_frequency_weekday_enum` int(11) DEFAULT NULL,
  `allow_compounding_on_eod` tinyint(1) NOT NULL DEFAULT 0,
  `disallow_interest_calc_on_past_due` tinyint(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `FK_m_loan_m_loan_recalculation_details` (`loan_id`),
  CONSTRAINT `FK_m_loan_m_loan_recalculation_details` FOREIGN KEY (`loan_id`) REFERENCES `m_loan` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `m_loan_repayment_schedule` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `loan_id` bigint(20) NOT NULL,
  `fromdate` date DEFAULT NULL,
  `duedate` date NOT NULL,
  `installment` smallint(6) NOT NULL,
  `principal_amount` decimal(19,6) DEFAULT NULL,
  `principal_completed_derived` decimal(19,6) DEFAULT NULL,
  `principal_writtenoff_derived` decimal(19,6) DEFAULT NULL,
  `interest_amount` decimal(19,6) DEFAULT NULL,
  `interest_completed_derived` decimal(19,6) DEFAULT NULL,
  `interest_writtenoff_derived` decimal(19,6) DEFAULT NULL,
  `interest_waived_derived` decimal(19,6) DEFAULT NULL,
  `accrual_interest_derived` decimal(19,6) DEFAULT NULL,
  `reschedule_interest_portion` decimal(19,6) DEFAULT NULL,
  `fee_charges_amount` decimal(19,6) DEFAULT NULL,
  `fee_charges_completed_derived` decimal(19,6) DEFAULT NULL,
  `fee_charges_writtenoff_derived` decimal(19,6) DEFAULT NULL,
  `fee_charges_waived_derived` decimal(19,6) DEFAULT NULL,
  `accrual_fee_charges_derived` decimal(19,6) DEFAULT NULL,
  `penalty_charges_amount` decimal(19,6) DEFAULT NULL,
  `penalty_charges_completed_derived` decimal(19,6) DEFAULT NULL,
  `penalty_charges_writtenoff_derived` decimal(19,6) DEFAULT NULL,
  `penalty_charges_waived_derived` decimal(19,6) DEFAULT NULL,
  `accrual_penalty_charges_derived` decimal(19,6) DEFAULT NULL,
  `total_paid_in_advance_derived` decimal(19,6) DEFAULT NULL,
  `total_paid_late_derived` decimal(19,6) DEFAULT NULL,
  `completed_derived` tinyint(1) NOT NULL,
  `obligations_met_on_date` date DEFAULT NULL,
  `created_by` bigint(20) NOT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `lastmodified_date` datetime(6) DEFAULT NULL,
  `last_modified_by` bigint(20) NOT NULL,
  `recalculated_interest_component` tinyint(1) NOT NULL DEFAULT 0,
  `created_on_utc` datetime(6) DEFAULT NULL,
  `last_modified_on_utc` datetime(6) DEFAULT NULL,
  `is_additional` tinyint(1) NOT NULL DEFAULT 0,
  `credits_amount` decimal(19,6) DEFAULT NULL,
  `is_down_payment` tinyint(1) NOT NULL DEFAULT 0,
  `credited_interest` decimal(19,6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK488B92AA40BE0710` (`loan_id`),
  KEY `FK_loan_repayment_schedule_created_by` (`created_by`),
  KEY `FK_loan_repayment_schedule_last_modified_by` (`last_modified_by`),
  CONSTRAINT `FK488B92AA40BE0710` FOREIGN KEY (`loan_id`) REFERENCES `m_loan` (`id`),
  CONSTRAINT `FK_loan_repayment_schedule_created_by` FOREIGN KEY (`created_by`) REFERENCES `m_appuser` (`id`),
  CONSTRAINT `FK_loan_repayment_schedule_last_modified_by` FOREIGN KEY (`last_modified_by`) REFERENCES `m_appuser` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `m_loan_repayment_schedule_history` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `loan_id` bigint(20) NOT NULL,
  `loan_reschedule_request_id` bigint(20) DEFAULT NULL,
  `fromdate` date DEFAULT NULL,
  `duedate` date NOT NULL,
  `installment` smallint(6) NOT NULL,
  `principal_amount` decimal(19,6) DEFAULT NULL,
  `interest_amount` decimal(19,6) DEFAULT NULL,
  `fee_charges_amount` decimal(19,6) DEFAULT NULL,
  `penalty_charges_amount` decimal(19,6) DEFAULT NULL,
  `createdby_id` bigint(20) DEFAULT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `lastmodified_date` datetime(6) DEFAULT NULL,
  `lastmodifiedby_id` bigint(20) DEFAULT NULL,
  `version` int(11) NOT NULL,
  `created_on_utc` datetime(6) DEFAULT NULL,
  `last_modified_on_utc` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `loan_id` (`loan_id`),
  KEY `loan_reschedule_request_id` (`loan_reschedule_request_id`),
  CONSTRAINT `m_loan_repayment_schedule_history_ibfk_1` FOREIGN KEY (`loan_id`) REFERENCES `m_loan` (`id`),
  CONSTRAINT `m_loan_repayment_schedule_history_ibfk_2` FOREIGN KEY (`loan_reschedule_request_id`) REFERENCES `m_loan_reschedule_request` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `m_loan_reschedule_request` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `loan_id` bigint(20) NOT NULL,
  `status_enum` smallint(6) NOT NULL,
  `reschedule_from_installment` smallint(6) NOT NULL COMMENT 'Rescheduling will start from this installment',
  `reschedule_from_date` date NOT NULL COMMENT 'Rescheduling will start from the installment with due date similar to this date.',
  `recalculate_interest` tinyint(1) DEFAULT NULL COMMENT 'If set to 1, interest will be recalculated starting from the reschedule period.',
  `reschedule_reason_cv_id` int(11) DEFAULT NULL COMMENT 'ID of code value of reason for rescheduling',
  `reschedule_reason_comment` varchar(500) DEFAULT NULL COMMENT 'Text provided in addition to the reason code value',
  `submitted_on_date` date NOT NULL,
  `submitted_by_user_id` bigint(20) NOT NULL,
  `approved_on_date` date DEFAULT NULL,
  `approved_by_user_id` bigint(20) DEFAULT NULL,
  `rejected_on_date` date DEFAULT NULL,
  `rejected_by_user_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `approved_by_user_id` (`approved_by_user_id`),
  KEY `loan_reschedule_request_loan_id` (`loan_id`),
  KEY `rejected_by_user_id` (`rejected_by_user_id`),
  KEY `reschedule_reason_cv_id` (`reschedule_reason_cv_id`),
  KEY `submitted_by_user_id` (`submitted_by_user_id`),
  CONSTRAINT `m_loan_reschedule_request_ibfk_1` FOREIGN KEY (`loan_id`) REFERENCES `m_loan` (`id`),
  CONSTRAINT `m_loan_reschedule_request_ibfk_2` FOREIGN KEY (`reschedule_reason_cv_id`) REFERENCES `m_code_value` (`id`),
  CONSTRAINT `m_loan_reschedule_request_ibfk_3` FOREIGN KEY (`submitted_by_user_id`) REFERENCES `m_appuser` (`id`),
  CONSTRAINT `m_loan_reschedule_request_ibfk_4` FOREIGN KEY (`approved_by_user_id`) REFERENCES `m_appuser` (`id`),
  CONSTRAINT `m_loan_reschedule_request_ibfk_5` FOREIGN KEY (`rejected_by_user_id`) REFERENCES `m_appuser` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `m_loan_reschedule_request_term_variations_mapping` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `loan_reschedule_request_id` bigint(20) NOT NULL,
  `loan_term_variations_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK__m_loan_reschedule_request` (`loan_reschedule_request_id`),
  KEY `FK__m_loan_term_variations` (`loan_term_variations_id`),
  CONSTRAINT `FK__m_loan_reschedule_request` FOREIGN KEY (`loan_reschedule_request_id`) REFERENCES `m_loan_reschedule_request` (`id`),
  CONSTRAINT `FK__m_loan_term_variations` FOREIGN KEY (`loan_term_variations_id`) REFERENCES `m_loan_term_variations` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `m_loan_term_variations` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `loan_id` bigint(20) NOT NULL,
  `term_type` smallint(6) NOT NULL,
  `applicable_date` date NOT NULL,
  `decimal_value` decimal(19,6) DEFAULT NULL,
  `date_value` date DEFAULT NULL,
  `is_specific_to_installment` tinyint(1) NOT NULL DEFAULT 0,
  `applied_on_loan_status` smallint(6) NOT NULL,
  `is_active` tinyint(1) NOT NULL DEFAULT 1,
  `parent_id` bigint(20) DEFAULT NULL,
  `created_on_utc` datetime DEFAULT NULL,
  `created_by` bigint(20) DEFAULT NULL,
  `last_modified_by` bigint(20) DEFAULT NULL,
  `last_modified_on_utc` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_loan_id_m_loan_id` (`loan_id`),
  KEY `FK_loan_term_variations_created_by` (`created_by`),
  KEY `FK_loan_term_variations_last_modified_by` (`last_modified_by`),
  CONSTRAINT `FK_loan_id_m_loan_id` FOREIGN KEY (`loan_id`) REFERENCES `m_loan` (`id`),
  CONSTRAINT `FK_loan_term_variations_created_by` FOREIGN KEY (`created_by`) REFERENCES `m_appuser` (`id`),
  CONSTRAINT `FK_loan_term_variations_last_modified_by` FOREIGN KEY (`last_modified_by`) REFERENCES `m_appuser` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `m_loan_topup` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `loan_id` bigint(20) NOT NULL,
  `closure_loan_id` bigint(20) NOT NULL,
  `account_transfer_details_id` bigint(20) DEFAULT NULL,
  `topup_amount` decimal(19,6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `m_loan_topup_FK_account_transfer_details_id` (`account_transfer_details_id`),
  KEY `m_loan_topup_FK_closure_loan_id` (`closure_loan_id`),
  KEY `m_loan_topup_FK_loan_id` (`loan_id`),
  CONSTRAINT `m_loan_topup_FK_account_transfer_details_id` FOREIGN KEY (`account_transfer_details_id`) REFERENCES `m_account_transfer_details` (`id`),
  CONSTRAINT `m_loan_topup_FK_closure_loan_id` FOREIGN KEY (`closure_loan_id`) REFERENCES `m_loan` (`id`),
  CONSTRAINT `m_loan_topup_FK_loan_id` FOREIGN KEY (`loan_id`) REFERENCES `m_loan` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `m_loan_tranche_charges` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `loan_id` bigint(20) NOT NULL,
  `charge_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_m_loan_tranche_charges_m_charge` (`charge_id`),
  KEY `FK_m_loan_tranche_charges_m_loan` (`loan_id`),
  CONSTRAINT `FK_m_loan_tranche_charges_m_charge` FOREIGN KEY (`charge_id`) REFERENCES `m_charge` (`id`),
  CONSTRAINT `FK_m_loan_tranche_charges_m_loan` FOREIGN KEY (`loan_id`) REFERENCES `m_loan` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `m_loan_tranche_disbursement_charge` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `loan_charge_id` bigint(20) NOT NULL,
  `disbursement_detail_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_m_loan_tranche_disbursement_charge_m_loan_charge` (`loan_charge_id`),
  KEY `FK_m_loan_tranche_disbursement_charge_m_loan_disbursement_detail` (`disbursement_detail_id`),
  CONSTRAINT `FK_m_loan_tranche_disbursement_charge_m_loan_charge` FOREIGN KEY (`loan_charge_id`) REFERENCES `m_loan_charge` (`id`),
  CONSTRAINT `FK_m_loan_tranche_disbursement_charge_m_loan_disbursement_detail` FOREIGN KEY (`disbursement_detail_id`) REFERENCES `m_loan_disbursement_detail` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `m_loan_transaction` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `loan_id` bigint(20) NOT NULL,
  `office_id` bigint(20) NOT NULL,
  `payment_detail_id` bigint(20) DEFAULT NULL,
  `is_reversed` tinyint(1) NOT NULL,
  `external_id` varchar(100) DEFAULT NULL,
  `transaction_type_enum` smallint(6) NOT NULL,
  `transaction_date` date NOT NULL,
  `amount` decimal(19,6) NOT NULL,
  `principal_portion_derived` decimal(19,6) DEFAULT NULL,
  `interest_portion_derived` decimal(19,6) DEFAULT NULL,
  `fee_charges_portion_derived` decimal(19,6) DEFAULT NULL,
  `penalty_charges_portion_derived` decimal(19,6) DEFAULT NULL,
  `overpayment_portion_derived` decimal(19,6) DEFAULT NULL,
  `unrecognized_income_portion` decimal(19,6) DEFAULT NULL,
  `outstanding_loan_balance_derived` decimal(19,6) DEFAULT NULL,
  `submitted_on_date` date NOT NULL,
  `manually_adjusted_or_reversed` tinyint(1) DEFAULT 0,
  `created_date` datetime(6) DEFAULT NULL,
  `created_by` bigint(20) NOT NULL,
  `last_modified_by` bigint(20) NOT NULL,
  `created_on_utc` datetime(6) DEFAULT NULL,
  `last_modified_on_utc` datetime(6) DEFAULT NULL,
  `charge_refund_charge_type` varchar(1) DEFAULT NULL,
  `reversal_external_id` varchar(100) DEFAULT NULL,
  `reversed_on_date` date DEFAULT NULL,
  `version` bigint(20) NOT NULL DEFAULT 1,
  PRIMARY KEY (`id`),
  UNIQUE KEY `external_id` (`external_id`),
  UNIQUE KEY `UNIQUE_REVERSAL_EXTERNAL_ID_LOAN_TRANSACTION` (`reversal_external_id`),
  KEY `FKCFCEA42640BE0710` (`loan_id`),
  KEY `FK_m_loan_transaction_m_office` (`office_id`),
  KEY `FK_m_loan_transaction_m_payment_detail` (`payment_detail_id`),
  KEY `FK_loan_transaction_created_by` (`created_by`),
  KEY `FK_loan_transaction_last_modified_by` (`last_modified_by`),
  CONSTRAINT `FKCFCEA42640BE0710` FOREIGN KEY (`loan_id`) REFERENCES `m_loan` (`id`),
  CONSTRAINT `FK_loan_transaction_created_by` FOREIGN KEY (`created_by`) REFERENCES `m_appuser` (`id`),
  CONSTRAINT `FK_loan_transaction_last_modified_by` FOREIGN KEY (`last_modified_by`) REFERENCES `m_appuser` (`id`),
  CONSTRAINT `FK_m_loan_transaction_m_office` FOREIGN KEY (`office_id`) REFERENCES `m_office` (`id`),
  CONSTRAINT `FK_m_loan_transaction_m_payment_detail` FOREIGN KEY (`payment_detail_id`) REFERENCES `m_payment_detail` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `m_loan_transaction_relation` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `from_loan_transaction_id` bigint(20) NOT NULL,
  `to_loan_transaction_id` bigint(20) DEFAULT NULL,
  `relation_type_enum` int(11) NOT NULL,
  `created_by` bigint(20) NOT NULL,
  `created_on_utc` datetime(6) DEFAULT NULL,
  `last_modified_by` bigint(20) NOT NULL,
  `last_modified_on_utc` datetime(6) DEFAULT NULL,
  `to_loan_charge_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_m_loan_transaction_from_relation` (`from_loan_transaction_id`),
  KEY `FK_m_loan_transaction_to_relation` (`to_loan_transaction_id`),
  KEY `FK_m_loan_charge_to_relation` (`to_loan_charge_id`),
  CONSTRAINT `FK_m_loan_charge_to_relation` FOREIGN KEY (`to_loan_charge_id`) REFERENCES `m_loan_charge` (`id`),
  CONSTRAINT `FK_m_loan_transaction_from_relation` FOREIGN KEY (`from_loan_transaction_id`) REFERENCES `m_loan_transaction` (`id`),
  CONSTRAINT `FK_m_loan_transaction_to_relation` FOREIGN KEY (`to_loan_transaction_id`) REFERENCES `m_loan_transaction` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `m_loan_transaction_repayment_schedule_mapping` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `loan_transaction_id` bigint(20) NOT NULL,
  `loan_repayment_schedule_id` bigint(20) NOT NULL,
  `amount` decimal(19,6) NOT NULL,
  `principal_portion_derived` decimal(19,6) DEFAULT NULL,
  `interest_portion_derived` decimal(19,6) DEFAULT NULL,
  `fee_charges_portion_derived` decimal(19,6) DEFAULT NULL,
  `penalty_charges_portion_derived` decimal(19,6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_mappings_m_loan_repayment_schedule` (`loan_repayment_schedule_id`),
  KEY `FK_mappings_m_loan_transaction` (`loan_transaction_id`),
  CONSTRAINT `FK_mappings_m_loan_repayment_schedule` FOREIGN KEY (`loan_repayment_schedule_id`) REFERENCES `m_loan_repayment_schedule` (`id`),
  CONSTRAINT `FK_mappings_m_loan_transaction` FOREIGN KEY (`loan_transaction_id`) REFERENCES `m_loan_transaction` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `m_loanproduct_provisioning_entry` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `history_id` bigint(20) NOT NULL,
  `criteria_id` bigint(20) NOT NULL,
  `currency_code` varchar(3) NOT NULL,
  `office_id` bigint(20) NOT NULL,
  `product_id` bigint(20) NOT NULL,
  `category_id` bigint(20) NOT NULL,
  `overdue_in_days` bigint(20) DEFAULT 0,
  `reseve_amount` decimal(20,6) DEFAULT 0.000000,
  `liability_account` bigint(20) DEFAULT NULL,
  `expense_account` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `category_id` (`category_id`),
  KEY `fk_loanproduct_provisioning_entry_criteria` (`criteria_id`),
  KEY `expense_account` (`expense_account`),
  KEY `history_id` (`history_id`),
  KEY `loanproduct_provisioning_entry_liability_account` (`liability_account`),
  KEY `loanproduct_provisioning_entry_office_id` (`office_id`),
  KEY `loanproduct_provisioning_entry_product_id` (`product_id`),
  CONSTRAINT `m_loanproduct_provisioning_entry_ibfk_1` FOREIGN KEY (`history_id`) REFERENCES `m_provisioning_history` (`id`),
  CONSTRAINT `m_loanproduct_provisioning_entry_ibfk_2` FOREIGN KEY (`criteria_id`) REFERENCES `m_provisioning_criteria` (`id`),
  CONSTRAINT `m_loanproduct_provisioning_entry_ibfk_3` FOREIGN KEY (`office_id`) REFERENCES `m_office` (`id`),
  CONSTRAINT `m_loanproduct_provisioning_entry_ibfk_4` FOREIGN KEY (`product_id`) REFERENCES `m_product_loan` (`id`),
  CONSTRAINT `m_loanproduct_provisioning_entry_ibfk_5` FOREIGN KEY (`category_id`) REFERENCES `m_provision_category` (`id`),
  CONSTRAINT `m_loanproduct_provisioning_entry_ibfk_6` FOREIGN KEY (`liability_account`) REFERENCES `acc_gl_account` (`id`),
  CONSTRAINT `m_loanproduct_provisioning_entry_ibfk_7` FOREIGN KEY (`expense_account`) REFERENCES `acc_gl_account` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `m_loanproduct_provisioning_mapping` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `product_id` bigint(20) NOT NULL,
  `criteria_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `product_id` (`product_id`),
  KEY `fk_loanproduct_provisioning_mapping_criteria` (`criteria_id`),
  CONSTRAINT `m_loanproduct_provisioning_mapping_ibfk_1` FOREIGN KEY (`product_id`) REFERENCES `m_product_loan` (`id`),
  CONSTRAINT `m_loanproduct_provisioning_mapping_ibfk_2` FOREIGN KEY (`criteria_id`) REFERENCES `m_provisioning_criteria` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `m_mandatory_savings_schedule` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `savings_account_id` bigint(20) NOT NULL,
  `fromdate` date DEFAULT NULL,
  `duedate` date NOT NULL,
  `installment` smallint(6) NOT NULL,
  `deposit_amount` decimal(19,6) DEFAULT NULL,
  `deposit_amount_completed_derived` decimal(19,6) DEFAULT NULL,
  `total_paid_in_advance_derived` decimal(19,6) DEFAULT NULL,
  `total_paid_late_derived` decimal(19,6) DEFAULT NULL,
  `completed_derived` tinyint(1) NOT NULL,
  `obligations_met_on_date` date DEFAULT NULL,
  `created_by` bigint(20) NOT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `lastmodified_date` datetime(6) DEFAULT NULL,
  `last_modified_by` bigint(20) NOT NULL,
  `created_on_utc` datetime(6) NOT NULL,
  `last_modified_on_utc` datetime(6) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKMSS0000000001` (`savings_account_id`),
  KEY `FK_mandatory_savings_schedule_created_by` (`created_by`),
  KEY `FK_mandatory_savings_schedule_last_modified_by` (`last_modified_by`),
  CONSTRAINT `FKMSS0000000001` FOREIGN KEY (`savings_account_id`) REFERENCES `m_savings_account` (`id`),
  CONSTRAINT `FK_mandatory_savings_schedule_created_by` FOREIGN KEY (`created_by`) REFERENCES `m_appuser` (`id`),
  CONSTRAINT `FK_mandatory_savings_schedule_last_modified_by` FOREIGN KEY (`last_modified_by`) REFERENCES `m_appuser` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `m_meeting` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `calendar_instance_id` bigint(20) NOT NULL,
  `meeting_date` date NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_calendar_instance_id_meeting_date` (`calendar_instance_id`,`meeting_date`),
  CONSTRAINT `FK_m_calendar_instance_m_meeting` FOREIGN KEY (`calendar_instance_id`) REFERENCES `m_calendar_instance` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `m_note` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `client_id` bigint(20) DEFAULT NULL,
  `group_id` bigint(20) DEFAULT NULL,
  `loan_id` bigint(20) DEFAULT NULL,
  `loan_transaction_id` bigint(20) DEFAULT NULL,
  `savings_account_id` bigint(20) DEFAULT NULL,
  `savings_account_transaction_id` bigint(20) DEFAULT NULL,
  `share_account_id` bigint(20) DEFAULT NULL,
  `note_type_enum` smallint(6) NOT NULL,
  `note` varchar(1000) DEFAULT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `created_by` bigint(20) NOT NULL,
  `lastmodified_date` datetime(6) DEFAULT NULL,
  `last_modified_by` bigint(20) NOT NULL,
  `created_on_utc` datetime(6) DEFAULT NULL,
  `last_modified_on_utc` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK7C9708924D26803` (`loan_transaction_id`),
  KEY `FK7C9708940BE0710` (`loan_id`),
  KEY `FK7C97089541F0A56` (`created_by`),
  KEY `FK7C970897179A0CB` (`client_id`),
  KEY `FK7C970898F889C3F` (`last_modified_by`),
  KEY `FK_m_note_m_group` (`group_id`),
  KEY `FK_savings_account_id` (`savings_account_id`),
  KEY `savings_account_transaction_id` (`savings_account_transaction_id`),
  CONSTRAINT `FK7C9708924D26803` FOREIGN KEY (`loan_transaction_id`) REFERENCES `m_loan_transaction` (`id`),
  CONSTRAINT `FK7C9708940BE0710` FOREIGN KEY (`loan_id`) REFERENCES `m_loan` (`id`),
  CONSTRAINT `FK7C97089541F0A56` FOREIGN KEY (`created_by`) REFERENCES `m_appuser` (`id`),
  CONSTRAINT `FK7C970897179A0CB` FOREIGN KEY (`client_id`) REFERENCES `m_client` (`id`),
  CONSTRAINT `FK7C970898F889C3F` FOREIGN KEY (`last_modified_by`) REFERENCES `m_appuser` (`id`),
  CONSTRAINT `FK_m_note_m_group` FOREIGN KEY (`group_id`) REFERENCES `m_group` (`id`),
  CONSTRAINT `FK_note_created_by` FOREIGN KEY (`created_by`) REFERENCES `m_appuser` (`id`),
  CONSTRAINT `FK_note_last_modified_by` FOREIGN KEY (`last_modified_by`) REFERENCES `m_appuser` (`id`),
  CONSTRAINT `FK_savings_account_id` FOREIGN KEY (`savings_account_id`) REFERENCES `m_savings_account` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `m_office` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `parent_id` bigint(20) DEFAULT NULL,
  `hierarchy` varchar(100) DEFAULT NULL,
  `external_id` varchar(100) DEFAULT NULL,
  `name` varchar(50) NOT NULL,
  `opening_date` date NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`),
  UNIQUE KEY `external_id` (`external_id`),
  KEY `FK2291C477E2551DCC` (`parent_id`),
  CONSTRAINT `FK2291C477E2551DCC` FOREIGN KEY (`parent_id`) REFERENCES `m_office` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `m_office_transaction` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `from_office_id` bigint(20) DEFAULT NULL,
  `to_office_id` bigint(20) DEFAULT NULL,
  `currency_code` varchar(3) NOT NULL,
  `currency_digits` int(11) NOT NULL,
  `transaction_amount` decimal(19,6) NOT NULL,
  `transaction_date` date NOT NULL,
  `description` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK1E37728B783C5C25` (`from_office_id`),
  KEY `FK1E37728B93C6C1B6` (`to_office_id`),
  CONSTRAINT `FK1E37728B783C5C25` FOREIGN KEY (`from_office_id`) REFERENCES `m_office` (`id`),
  CONSTRAINT `FK1E37728B93C6C1B6` FOREIGN KEY (`to_office_id`) REFERENCES `m_office` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `m_organisation_creditbureau` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `alias` varchar(50) NOT NULL,
  `creditbureau_id` bigint(20) NOT NULL,
  `is_active` tinyint(1) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `morgcb` (`alias`,`creditbureau_id`),
  KEY `orgcb_cbfk` (`creditbureau_id`),
  CONSTRAINT `orgcb_cbfk` FOREIGN KEY (`creditbureau_id`) REFERENCES `m_creditbureau` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `m_organisation_currency` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `code` varchar(3) NOT NULL,
  `decimal_places` smallint(6) NOT NULL,
  `currency_multiplesof` smallint(6) DEFAULT NULL,
  `name` varchar(50) NOT NULL,
  `display_symbol` varchar(10) DEFAULT NULL,
  `internationalized_name_code` varchar(50) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `m_password_validation_policy` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `regex` text NOT NULL,
  `description` text NOT NULL,
  `active` tinyint(1) NOT NULL DEFAULT 0,
  `key` varchar(255) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `m_payment_detail` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `payment_type_id` int(11) DEFAULT NULL,
  `account_number` varchar(100) DEFAULT NULL,
  `check_number` varchar(100) DEFAULT NULL,
  `receipt_number` varchar(100) DEFAULT NULL,
  `bank_number` varchar(100) DEFAULT NULL,
  `routing_code` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_m_payment_detail_m_payment_type` (`payment_type_id`),
  CONSTRAINT `FK_m_payment_detail_m_payment_type` FOREIGN KEY (`payment_type_id`) REFERENCES `m_payment_type` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `m_payment_type` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `value` varchar(100) DEFAULT NULL,
  `description` varchar(500) DEFAULT NULL,
  `is_cash_payment` tinyint(1) DEFAULT 0,
  `order_position` int(11) NOT NULL DEFAULT 0,
  `code_name` varchar(100) DEFAULT NULL,
  `is_system_defined` tinyint(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `m_permission` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `grouping` varchar(45) DEFAULT NULL,
  `code` varchar(100) NOT NULL,
  `entity_name` varchar(100) DEFAULT NULL,
  `action_name` varchar(100) DEFAULT NULL,
  `can_maker_checker` tinyint(1) NOT NULL DEFAULT 1,
  PRIMARY KEY (`id`),
  UNIQUE KEY `code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `m_portfolio_account_associations` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `loan_account_id` bigint(20) DEFAULT NULL,
  `savings_account_id` bigint(20) DEFAULT NULL,
  `linked_loan_account_id` bigint(20) DEFAULT NULL,
  `linked_savings_account_id` bigint(20) DEFAULT NULL,
  `association_type_enum` smallint(6) NOT NULL DEFAULT 1,
  `is_active` tinyint(1) NOT NULL DEFAULT 1,
  PRIMARY KEY (`id`),
  KEY `account_association_loan_fk` (`loan_account_id`),
  KEY `account_association_savings_fk` (`savings_account_id`),
  KEY `linked_loan_fk` (`linked_loan_account_id`),
  KEY `linked_savings_fk` (`linked_savings_account_id`),
  CONSTRAINT `account_association_loan_fk` FOREIGN KEY (`loan_account_id`) REFERENCES `m_loan` (`id`),
  CONSTRAINT `account_association_savings_fk` FOREIGN KEY (`savings_account_id`) REFERENCES `m_savings_account` (`id`),
  CONSTRAINT `linked_loan_fk` FOREIGN KEY (`linked_loan_account_id`) REFERENCES `m_loan` (`id`),
  CONSTRAINT `linked_savings_fk` FOREIGN KEY (`linked_savings_account_id`) REFERENCES `m_savings_account` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `m_portfolio_command_source` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `action_name` varchar(50) NOT NULL,
  `entity_name` varchar(50) NOT NULL,
  `office_id` bigint(20) DEFAULT NULL,
  `group_id` bigint(20) DEFAULT NULL,
  `client_id` bigint(20) DEFAULT NULL,
  `loan_id` bigint(20) DEFAULT NULL,
  `savings_account_id` bigint(20) DEFAULT NULL,
  `api_get_url` varchar(100) NOT NULL,
  `resource_id` bigint(20) DEFAULT NULL,
  `subresource_id` bigint(20) DEFAULT NULL,
  `command_as_json` text NOT NULL,
  `maker_id` bigint(20) NOT NULL,
  `made_on_date` datetime(6) DEFAULT NULL,
  `checker_id` bigint(20) DEFAULT NULL,
  `checked_on_date` datetime(6) DEFAULT NULL,
  `status` smallint(6) DEFAULT NULL,
  `product_id` bigint(20) DEFAULT NULL,
  `transaction_id` varchar(100) DEFAULT NULL,
  `creditbureau_id` bigint(20) DEFAULT NULL,
  `organisation_creditbureau_id` bigint(20) DEFAULT NULL,
  `made_on_date_utc` datetime(6) DEFAULT NULL,
  `checked_on_date_utc` datetime(6) DEFAULT NULL,
  `job_name` varchar(100) DEFAULT NULL,
  `idempotency_key` varchar(50) NOT NULL,
  `resource_external_id` varchar(100) DEFAULT NULL,
  `subresource_external_id` varchar(100) DEFAULT NULL,
  `result` text DEFAULT NULL,
  `result_status_code` int(11) DEFAULT NULL,
  `loan_external_id` varchar(100) DEFAULT NULL,
  `is_sanitized` tinyint(1) NOT NULL DEFAULT 0,
  `client_ip` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UNIQUE_PORTFOLIO_COMMAND_SOURCE` (`action_name`,`entity_name`,`idempotency_key`),
  KEY `FK_m_checker_m_appuser` (`checker_id`),
  KEY `FK_m_maker_m_appuser` (`maker_id`),
  KEY `action_name` (`action_name`),
  KEY `checked_on_date` (`checked_on_date`),
  KEY `entity_name` (`entity_name`,`resource_id`),
  KEY `made_on_date` (`made_on_date`),
  KEY `portfolio_command_source_office_id` (`office_id`),
  KEY `processing_result_enum` (`status`),
  KEY `portfolio_command_source_composite_index` (`action_name`,`entity_name`,`idempotency_key`),
  CONSTRAINT `FK_m_checker_m_appuser` FOREIGN KEY (`checker_id`) REFERENCES `m_appuser` (`id`),
  CONSTRAINT `FK_m_maker_m_appuser` FOREIGN KEY (`maker_id`) REFERENCES `m_appuser` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `m_product_loan` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `short_name` varchar(4) NOT NULL,
  `currency_code` varchar(3) NOT NULL,
  `currency_digits` smallint(6) NOT NULL,
  `currency_multiplesof` smallint(6) DEFAULT NULL,
  `principal_amount` decimal(19,6) DEFAULT NULL,
  `min_principal_amount` decimal(19,6) DEFAULT NULL,
  `max_principal_amount` decimal(19,6) DEFAULT NULL,
  `arrearstolerance_amount` decimal(19,6) DEFAULT NULL,
  `name` varchar(100) NOT NULL,
  `description` varchar(500) DEFAULT NULL,
  `fund_id` bigint(20) DEFAULT NULL,
  `is_linked_to_floating_interest_rates` tinyint(1) NOT NULL DEFAULT 0,
  `allow_variabe_installments` tinyint(1) NOT NULL DEFAULT 0,
  `nominal_interest_rate_per_period` decimal(19,6) DEFAULT NULL,
  `min_nominal_interest_rate_per_period` decimal(19,6) DEFAULT NULL,
  `max_nominal_interest_rate_per_period` decimal(19,6) DEFAULT NULL,
  `interest_period_frequency_enum` smallint(6) DEFAULT NULL,
  `annual_nominal_interest_rate` decimal(19,6) DEFAULT NULL,
  `interest_method_enum` smallint(6) NOT NULL,
  `interest_calculated_in_period_enum` smallint(6) NOT NULL DEFAULT 1,
  `allow_partial_period_interest_calcualtion` tinyint(1) NOT NULL DEFAULT 0,
  `repay_every` smallint(6) NOT NULL,
  `repayment_period_frequency_enum` smallint(6) NOT NULL,
  `number_of_repayments` smallint(6) NOT NULL,
  `min_number_of_repayments` smallint(6) DEFAULT NULL,
  `max_number_of_repayments` smallint(6) DEFAULT NULL,
  `grace_on_principal_periods` smallint(6) DEFAULT NULL,
  `recurring_moratorium_principal_periods` smallint(6) DEFAULT NULL,
  `grace_on_interest_periods` smallint(6) DEFAULT NULL,
  `grace_interest_free_periods` smallint(6) DEFAULT NULL,
  `amortization_method_enum` smallint(6) NOT NULL,
  `accounting_type` smallint(6) NOT NULL,
  `loan_transaction_strategy_id` bigint(20) DEFAULT NULL,
  `external_id` varchar(100) DEFAULT NULL,
  `include_in_borrower_cycle` tinyint(1) NOT NULL DEFAULT 0,
  `use_borrower_cycle` tinyint(1) NOT NULL DEFAULT 0,
  `start_date` date DEFAULT NULL,
  `close_date` date DEFAULT NULL,
  `allow_multiple_disbursals` tinyint(1) NOT NULL DEFAULT 0,
  `max_disbursals` int(11) DEFAULT NULL,
  `max_outstanding_loan_balance` decimal(19,6) DEFAULT NULL,
  `grace_on_arrears_ageing` smallint(6) DEFAULT NULL,
  `overdue_days_for_npa` smallint(6) DEFAULT NULL,
  `days_in_month_enum` smallint(6) NOT NULL DEFAULT 1,
  `days_in_year_enum` smallint(6) NOT NULL DEFAULT 1,
  `interest_recalculation_enabled` tinyint(1) NOT NULL DEFAULT 0,
  `min_days_between_disbursal_and_first_repayment` int(11) DEFAULT NULL,
  `hold_guarantee_funds` tinyint(1) NOT NULL DEFAULT 0,
  `principal_threshold_for_last_installment` decimal(5,2) NOT NULL DEFAULT 50.00,
  `account_moves_out_of_npa_only_on_arrears_completion` tinyint(1) NOT NULL DEFAULT 0,
  `can_define_fixed_emi_amount` tinyint(1) NOT NULL DEFAULT 0,
  `instalment_amount_in_multiples_of` decimal(19,6) DEFAULT NULL,
  `can_use_for_topup` tinyint(1) NOT NULL DEFAULT 0,
  `sync_expected_with_disbursement_date` tinyint(1) DEFAULT 0,
  `is_equal_amortization` tinyint(1) NOT NULL DEFAULT 0,
  `fixed_principal_percentage_per_installment` decimal(5,2) DEFAULT NULL,
  `disallow_expected_disbursements` tinyint(1) NOT NULL DEFAULT 0,
  `allow_approved_disbursed_amounts_over_applied` tinyint(1) NOT NULL DEFAULT 0,
  `over_applied_calculation_type` varchar(10) DEFAULT NULL,
  `over_applied_number` int(11) DEFAULT NULL,
  `delinquency_bucket_id` bigint(20) DEFAULT NULL,
  `loan_transaction_strategy_code` varchar(100) NOT NULL DEFAULT '-',
  `loan_transaction_strategy_name` varchar(100) NOT NULL DEFAULT '-',
  `due_days_for_repayment_event` int(11) DEFAULT NULL,
  `overdue_days_for_repayment_event` int(11) DEFAULT NULL,
  `enable_down_payment` tinyint(1) NOT NULL DEFAULT 0,
  `disbursed_amount_percentage_for_down_payment` decimal(9,6) DEFAULT NULL,
  `enable_installment_level_delinquency` tinyint(1) NOT NULL DEFAULT 0,
  `enable_accrual_activity_posting` tinyint(1) NOT NULL DEFAULT 0,
  `days_in_year_custom_strategy` varchar(100) DEFAULT NULL,
  `enable_income_capitalization` tinyint(1) NOT NULL DEFAULT 0,
  `capitalized_income_calculation_type` varchar(100) DEFAULT NULL,
  `capitalized_income_strategy` varchar(100) DEFAULT NULL,
  `capitalized_income_type` varchar(10) DEFAULT NULL,
  `enable_buy_down_fee` tinyint(1) NOT NULL DEFAULT 0,
  `buy_down_fee_calculation_type` varchar(100) DEFAULT NULL,
  `buy_down_fee_strategy` varchar(100) DEFAULT NULL,
  `buy_down_fee_income_type` varchar(100) DEFAULT NULL,
  `allow_full_term_for_tranche` tinyint(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `short_name` (`short_name`),
  UNIQUE KEY `name` (`name`),
  UNIQUE KEY `external_id` (`external_id`),
  KEY `FKA6A8A7D77240145` (`fund_id`),
  KEY `FK_ltp_strategy` (`loan_transaction_strategy_id`),
  KEY `FK_m_product_loan_m_delinquency_bucket` (`delinquency_bucket_id`),
  CONSTRAINT `FKA6A8A7D77240145` FOREIGN KEY (`fund_id`) REFERENCES `m_fund` (`id`),
  CONSTRAINT `FK_m_product_loan_m_delinquency_bucket` FOREIGN KEY (`delinquency_bucket_id`) REFERENCES `m_delinquency_bucket` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `m_product_loan_charge` (
  `product_loan_id` bigint(20) NOT NULL,
  `charge_id` bigint(20) NOT NULL,
  PRIMARY KEY (`product_loan_id`,`charge_id`),
  KEY `fk_product_loan_charge_charge` (`charge_id`),
  CONSTRAINT `m_product_loan_charge_ibfk_1` FOREIGN KEY (`charge_id`) REFERENCES `m_charge` (`id`),
  CONSTRAINT `m_product_loan_charge_ibfk_2` FOREIGN KEY (`product_loan_id`) REFERENCES `m_product_loan` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `m_product_loan_configurable_attributes` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `loan_product_id` bigint(20) NOT NULL,
  `amortization_method_enum` tinyint(1) NOT NULL DEFAULT 1,
  `interest_method_enum` tinyint(1) NOT NULL DEFAULT 1,
  `loan_transaction_strategy_code` tinyint(1) DEFAULT NULL,
  `interest_calculated_in_period_enum` tinyint(1) NOT NULL DEFAULT 1,
  `arrearstolerance_amount` tinyint(1) NOT NULL DEFAULT 1,
  `repay_every` tinyint(1) NOT NULL DEFAULT 1,
  `moratorium` tinyint(1) NOT NULL DEFAULT 1,
  `grace_on_arrears_ageing` tinyint(1) NOT NULL DEFAULT 1,
  PRIMARY KEY (`id`),
  KEY `fk_m_product_loan_configurable_attributes_0001` (`loan_product_id`),
  CONSTRAINT `fk_m_product_loan_configurable_attributes_0001` FOREIGN KEY (`loan_product_id`) REFERENCES `m_product_loan` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `m_product_loan_floating_rates` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `loan_product_id` bigint(20) NOT NULL,
  `floating_rates_id` bigint(20) NOT NULL,
  `interest_rate_differential` decimal(19,6) NOT NULL DEFAULT 0.000000,
  `min_differential_lending_rate` decimal(19,6) NOT NULL DEFAULT 0.000000,
  `default_differential_lending_rate` decimal(19,6) NOT NULL DEFAULT 0.000000,
  `max_differential_lending_rate` decimal(19,6) NOT NULL DEFAULT 0.000000,
  `is_floating_interest_rate_calculation_allowed` tinyint(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `FK_mappings_m_floating_rates_id` (`floating_rates_id`),
  KEY `FK_mappings_m_product_loan_id` (`loan_product_id`),
  CONSTRAINT `FK_mappings_m_floating_rates_id` FOREIGN KEY (`floating_rates_id`) REFERENCES `m_floating_rates` (`id`),
  CONSTRAINT `FK_mappings_m_product_loan_id` FOREIGN KEY (`loan_product_id`) REFERENCES `m_product_loan` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `m_product_loan_guarantee_details` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `loan_product_id` bigint(20) NOT NULL,
  `mandatory_guarantee` decimal(19,5) NOT NULL,
  `minimum_guarantee_from_own_funds` decimal(19,5) DEFAULT NULL,
  `minimum_guarantee_from_guarantor_funds` decimal(19,5) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_guarantee_details_loan_product` (`loan_product_id`),
  CONSTRAINT `FK_guarantee_details_loan_product` FOREIGN KEY (`loan_product_id`) REFERENCES `m_product_loan` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `m_product_loan_rate` (
  `product_loan_id` bigint(20) NOT NULL,
  `rate_id` bigint(20) NOT NULL,
  PRIMARY KEY (`product_loan_id`,`rate_id`),
  KEY `FK_M_PRODUCT_LOAN_RATE_RATE` (`rate_id`),
  CONSTRAINT `FK_M_PRODUCT_LOAN_RATE_LOAN` FOREIGN KEY (`product_loan_id`) REFERENCES `m_product_loan` (`id`),
  CONSTRAINT `FK_M_PRODUCT_LOAN_RATE_RATE` FOREIGN KEY (`rate_id`) REFERENCES `m_rate` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `m_product_loan_recalculation_details` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `product_id` bigint(20) NOT NULL,
  `compound_type_enum` smallint(6) NOT NULL,
  `reschedule_strategy_enum` smallint(6) NOT NULL,
  `rest_frequency_type_enum` smallint(6) NOT NULL,
  `rest_frequency_interval` smallint(6) NOT NULL DEFAULT 0,
  `arrears_based_on_original_schedule` tinyint(1) NOT NULL DEFAULT 0,
  `pre_close_interest_calculation_strategy` smallint(6) NOT NULL DEFAULT 1,
  `compounding_frequency_type_enum` smallint(6) DEFAULT NULL,
  `compounding_frequency_interval` smallint(6) DEFAULT NULL,
  `rest_frequency_nth_day_enum` int(11) DEFAULT NULL,
  `rest_frequency_on_day` int(11) DEFAULT NULL,
  `rest_frequency_weekday_enum` int(11) DEFAULT NULL,
  `compounding_frequency_nth_day_enum` int(11) DEFAULT NULL,
  `compounding_frequency_on_day` int(11) DEFAULT NULL,
  `compounding_frequency_weekday_enum` int(11) DEFAULT NULL,
  `is_compounding_to_be_posted_as_transaction` tinyint(1) NOT NULL DEFAULT 0,
  `allow_compounding_on_eod` tinyint(1) NOT NULL DEFAULT 0,
  `disallow_interest_calc_on_past_due` tinyint(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `FK_m_product_loan_m_product_loan_recalculation_details` (`product_id`),
  CONSTRAINT `FK_m_product_loan_m_product_loan_recalculation_details` FOREIGN KEY (`product_id`) REFERENCES `m_product_loan` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `m_product_loan_variable_installment_config` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `loan_product_id` bigint(20) NOT NULL,
  `minimum_gap` int(11) NOT NULL,
  `maximum_gap` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_mappings_m_variable_product_loan_id` (`loan_product_id`),
  CONSTRAINT `FK_mappings_m_variable_product_loan_id` FOREIGN KEY (`loan_product_id`) REFERENCES `m_product_loan` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `m_product_loan_variations_borrower_cycle` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `loan_product_id` bigint(20) NOT NULL DEFAULT 0,
  `borrower_cycle_number` int(11) NOT NULL DEFAULT 0,
  `value_condition` int(11) NOT NULL DEFAULT 0,
  `param_type` int(11) NOT NULL DEFAULT 0,
  `default_value` decimal(19,6) NOT NULL DEFAULT 0.000000,
  `max_value` decimal(19,6) DEFAULT NULL,
  `min_value` decimal(19,6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `borrower_cycle_loan_product_FK` (`loan_product_id`),
  CONSTRAINT `borrower_cycle_loan_product_FK` FOREIGN KEY (`loan_product_id`) REFERENCES `m_product_loan` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `m_product_mix` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `product_id` bigint(20) NOT NULL,
  `restricted_product_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_m_product_mix_product_id_to_m_product_loan` (`product_id`),
  KEY `FK_m_product_mix_restricted_product_id_to_m_product_loan` (`restricted_product_id`),
  CONSTRAINT `FK_m_product_mix_product_id_to_m_product_loan` FOREIGN KEY (`product_id`) REFERENCES `m_product_loan` (`id`),
  CONSTRAINT `FK_m_product_mix_restricted_product_id_to_m_product_loan` FOREIGN KEY (`restricted_product_id`) REFERENCES `m_product_loan` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `m_provision_category` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `category_name` varchar(100) NOT NULL,
  `description` varchar(300) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `category_name` (`category_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `m_provisioning_criteria` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `criteria_name` varchar(200) NOT NULL,
  `createdby_id` bigint(20) DEFAULT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `lastmodifiedby_id` bigint(20) DEFAULT NULL,
  `lastmodified_date` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `criteria_name` (`criteria_name`),
  KEY `fk_provisioning_criteria_createdby` (`createdby_id`),
  KEY `provisioning_criteria_lastmodifiedby_id` (`lastmodifiedby_id`),
  CONSTRAINT `m_provisioning_criteria_ibfk_1` FOREIGN KEY (`createdby_id`) REFERENCES `m_appuser` (`id`),
  CONSTRAINT `m_provisioning_criteria_ibfk_2` FOREIGN KEY (`lastmodifiedby_id`) REFERENCES `m_appuser` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `m_provisioning_criteria_definition` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `criteria_id` bigint(20) NOT NULL,
  `category_id` bigint(20) NOT NULL,
  `min_age` bigint(20) NOT NULL,
  `max_age` bigint(20) NOT NULL,
  `provision_percentage` decimal(5,2) NOT NULL,
  `liability_account` bigint(20) DEFAULT NULL,
  `expense_account` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_provisioning_criteria_definition_category` (`category_id`),
  KEY `fk_loanproduct_criteria_definition_criteria` (`criteria_id`),
  KEY `fk_provisioning_criteria_definition_expense_account` (`expense_account`),
  KEY `provisioning_criteria_definition_liability_account` (`liability_account`),
  CONSTRAINT `m_provisioning_criteria_definition_ibfk_1` FOREIGN KEY (`criteria_id`) REFERENCES `m_provisioning_criteria` (`id`),
  CONSTRAINT `m_provisioning_criteria_definition_ibfk_2` FOREIGN KEY (`category_id`) REFERENCES `m_provision_category` (`id`),
  CONSTRAINT `m_provisioning_criteria_definition_ibfk_3` FOREIGN KEY (`liability_account`) REFERENCES `acc_gl_account` (`id`),
  CONSTRAINT `m_provisioning_criteria_definition_ibfk_4` FOREIGN KEY (`expense_account`) REFERENCES `acc_gl_account` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `m_provisioning_history` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `journal_entry_created` tinyint(1) DEFAULT 0,
  `createdby_id` bigint(20) DEFAULT NULL,
  `created_date` date DEFAULT NULL,
  `lastmodifiedby_id` bigint(20) DEFAULT NULL,
  `lastmodified_date` date DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_provisioning_history_createdby` (`createdby_id`),
  KEY `provisioning_history_lastmodifiedby_id` (`lastmodifiedby_id`),
  CONSTRAINT `m_provisioning_history_ibfk_1` FOREIGN KEY (`createdby_id`) REFERENCES `m_appuser` (`id`),
  CONSTRAINT `m_provisioning_history_ibfk_2` FOREIGN KEY (`lastmodifiedby_id`) REFERENCES `m_appuser` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `m_rate` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(250) NOT NULL,
  `percentage` decimal(10,2) NOT NULL,
  `active` tinyint(1) DEFAULT 0,
  `product_apply` smallint(6) NOT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `createdby_id` bigint(20) NOT NULL,
  `lastmodifiedby_id` bigint(20) NOT NULL,
  `lastmodified_date` datetime(6) DEFAULT NULL,
  `approve_user` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_M_RATE_APPROVE_USER` (`approve_user`),
  KEY `FK_M_RATE_CREATE_USER` (`createdby_id`),
  CONSTRAINT `FK_M_RATE_APPROVE_USER` FOREIGN KEY (`approve_user`) REFERENCES `m_appuser` (`id`),
  CONSTRAINT `FK_M_RATE_CREATE_USER` FOREIGN KEY (`createdby_id`) REFERENCES `m_appuser` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `m_repayment_with_post_dated_checks` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `check_no` bigint(20) NOT NULL,
  `amount` decimal(20,5) NOT NULL,
  `loan_id` bigint(20) DEFAULT NULL,
  `repayment_id` bigint(20) DEFAULT NULL,
  `account_no` bigint(20) NOT NULL,
  `bank_name` varchar(200) NOT NULL,
  `repayment_date` date NOT NULL,
  `status` smallint(6) DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `check_no` (`check_no`),
  KEY `fkloanrepayment` (`loan_id`),
  KEY `fkrepaymentwithpostdated` (`repayment_id`),
  CONSTRAINT `fkloanrepayment` FOREIGN KEY (`loan_id`) REFERENCES `m_loan` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fkrepaymentwithpostdated` FOREIGN KEY (`repayment_id`) REFERENCES `m_loan_repayment_schedule` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `m_report_mailing_job` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL,
  `description` text DEFAULT NULL,
  `start_datetime` datetime(6) DEFAULT NULL,
  `recurrence` varchar(100) DEFAULT NULL,
  `created_date` date NOT NULL,
  `createdby_id` bigint(20) NOT NULL,
  `lastmodified_date` date DEFAULT NULL,
  `lastmodifiedby_id` bigint(20) DEFAULT NULL,
  `email_recipients` text NOT NULL,
  `email_subject` varchar(100) NOT NULL,
  `email_message` text NOT NULL,
  `email_attachment_file_format` varchar(10) NOT NULL,
  `stretchy_report_id` int(11) NOT NULL,
  `stretchy_report_param_map` text DEFAULT NULL,
  `previous_run_datetime` datetime(6) DEFAULT NULL,
  `next_run_datetime` datetime(6) DEFAULT NULL,
  `previous_run_status` varchar(10) DEFAULT NULL,
  `previous_run_error_log` text DEFAULT NULL,
  `previous_run_error_message` text DEFAULT NULL,
  `number_of_runs` int(11) NOT NULL DEFAULT 0,
  `is_active` tinyint(1) NOT NULL DEFAULT 0,
  `is_deleted` tinyint(1) NOT NULL DEFAULT 0,
  `run_as_userid` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`),
  KEY `fk_report_mailing_job_createdby` (`createdby_id`),
  KEY `report_mailing_job_lastmodifiedby_id` (`lastmodifiedby_id`),
  KEY `run_as_userid` (`run_as_userid`),
  KEY `stretchy_report_id` (`stretchy_report_id`),
  CONSTRAINT `m_report_mailing_job_ibfk_1` FOREIGN KEY (`createdby_id`) REFERENCES `m_appuser` (`id`),
  CONSTRAINT `m_report_mailing_job_ibfk_2` FOREIGN KEY (`lastmodifiedby_id`) REFERENCES `m_appuser` (`id`),
  CONSTRAINT `m_report_mailing_job_ibfk_3` FOREIGN KEY (`stretchy_report_id`) REFERENCES `stretchy_report` (`id`),
  CONSTRAINT `m_report_mailing_job_ibfk_4` FOREIGN KEY (`run_as_userid`) REFERENCES `m_appuser` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `m_report_mailing_job_configuration` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(50) NOT NULL,
  `value` varchar(200) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `m_report_mailing_job_run_history` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `job_id` bigint(20) NOT NULL,
  `start_datetime` datetime(6) DEFAULT NULL,
  `end_datetime` datetime(6) DEFAULT NULL,
  `status` varchar(10) NOT NULL,
  `error_message` text DEFAULT NULL,
  `error_log` text DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `job_id` (`job_id`),
  CONSTRAINT `m_report_mailing_job_run_history_ibfk_1` FOREIGN KEY (`job_id`) REFERENCES `m_report_mailing_job` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `m_role` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL,
  `description` varchar(500) NOT NULL,
  `is_disabled` tinyint(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `m_role_permission` (
  `role_id` bigint(20) NOT NULL,
  `permission_id` bigint(20) NOT NULL,
  PRIMARY KEY (`role_id`,`permission_id`),
  KEY `FK8DEDB048103B544B` (`permission_id`),
  KEY `FK8DEDB04815CEC7AB` (`role_id`),
  CONSTRAINT `FK8DEDB048103B544B` FOREIGN KEY (`permission_id`) REFERENCES `m_permission` (`id`),
  CONSTRAINT `FK8DEDB04815CEC7AB` FOREIGN KEY (`role_id`) REFERENCES `m_role` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `m_savings_account` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `account_no` varchar(20) NOT NULL,
  `external_id` varchar(100) DEFAULT NULL,
  `client_id` bigint(20) DEFAULT NULL,
  `group_id` bigint(20) DEFAULT NULL,
  `gsim_id` bigint(20) DEFAULT NULL,
  `product_id` bigint(20) DEFAULT NULL,
  `field_officer_id` bigint(20) DEFAULT NULL,
  `status_enum` smallint(6) NOT NULL DEFAULT 300,
  `sub_status_enum` smallint(6) NOT NULL DEFAULT 0,
  `account_type_enum` smallint(6) NOT NULL DEFAULT 1,
  `deposit_type_enum` smallint(6) NOT NULL DEFAULT 100,
  `submittedon_date` date NOT NULL,
  `submittedon_userid` bigint(20) DEFAULT NULL,
  `approvedon_date` date DEFAULT NULL,
  `approvedon_userid` bigint(20) DEFAULT NULL,
  `rejectedon_date` date DEFAULT NULL,
  `rejectedon_userid` bigint(20) DEFAULT NULL,
  `withdrawnon_date` date DEFAULT NULL,
  `withdrawnon_userid` bigint(20) DEFAULT NULL,
  `activatedon_date` date DEFAULT NULL,
  `activatedon_userid` bigint(20) DEFAULT NULL,
  `closedon_date` date DEFAULT NULL,
  `closedon_userid` bigint(20) DEFAULT NULL,
  `currency_code` varchar(3) NOT NULL,
  `currency_digits` smallint(6) NOT NULL,
  `currency_multiplesof` smallint(6) DEFAULT NULL,
  `nominal_annual_interest_rate` decimal(19,6) NOT NULL,
  `interest_compounding_period_enum` smallint(6) NOT NULL,
  `interest_posting_period_enum` smallint(6) NOT NULL DEFAULT 4,
  `interest_calculation_type_enum` smallint(6) NOT NULL,
  `interest_calculation_days_in_year_type_enum` smallint(6) NOT NULL,
  `min_required_opening_balance` decimal(19,6) DEFAULT NULL,
  `lockin_period_frequency` decimal(19,6) DEFAULT NULL,
  `lockin_period_frequency_enum` smallint(6) DEFAULT NULL,
  `withdrawal_fee_for_transfer` tinyint(1) DEFAULT 1,
  `allow_overdraft` tinyint(1) NOT NULL DEFAULT 0,
  `overdraft_limit` decimal(19,6) DEFAULT NULL,
  `nominal_annual_interest_rate_overdraft` decimal(19,6) DEFAULT 0.000000,
  `min_overdraft_for_interest_calculation` decimal(19,6) DEFAULT 0.000000,
  `lockedin_until_date_derived` date DEFAULT NULL,
  `total_deposits_derived` decimal(19,6) DEFAULT NULL,
  `total_withdrawals_derived` decimal(19,6) DEFAULT NULL,
  `total_withdrawal_fees_derived` decimal(19,6) DEFAULT NULL,
  `total_fees_charge_derived` decimal(19,6) DEFAULT NULL,
  `total_penalty_charge_derived` decimal(19,6) DEFAULT NULL,
  `total_annual_fees_derived` decimal(19,6) DEFAULT NULL,
  `total_interest_earned_derived` decimal(19,6) DEFAULT NULL,
  `total_interest_posted_derived` decimal(19,6) DEFAULT NULL,
  `total_overdraft_interest_derived` decimal(19,6) DEFAULT 0.000000,
  `total_withhold_tax_derived` decimal(19,6) DEFAULT NULL,
  `account_balance_derived` decimal(19,6) NOT NULL DEFAULT 0.000000,
  `min_required_balance` decimal(19,6) DEFAULT NULL,
  `enforce_min_required_balance` tinyint(1) NOT NULL DEFAULT 0,
  `min_balance_for_interest_calculation` decimal(19,6) DEFAULT NULL,
  `start_interest_calculation_date` date DEFAULT NULL,
  `on_hold_funds_derived` decimal(19,6) DEFAULT NULL,
  `version` int(11) NOT NULL DEFAULT 1,
  `withhold_tax` tinyint(1) NOT NULL DEFAULT 0,
  `tax_group_id` bigint(20) DEFAULT NULL,
  `last_interest_calculation_date` date DEFAULT NULL,
  `total_savings_amount_on_hold` decimal(19,6) DEFAULT NULL,
  `interest_posted_till_date` date DEFAULT NULL,
  `reason_for_block` varchar(256) DEFAULT NULL,
  `max_allowed_lien_limit` decimal(19,6) DEFAULT NULL,
  `is_lien_allowed` tinyint(1) NOT NULL DEFAULT 0,
  `created_by` bigint(20) NOT NULL,
  `last_modified_by` bigint(20) NOT NULL,
  `created_on_utc` datetime(6) NOT NULL,
  `last_modified_on_utc` datetime(6) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `account_no` (`account_no`),
  UNIQUE KEY `external_id` (`external_id`),
  KEY `FKSA00000000000001` (`client_id`),
  KEY `FKSA00000000000002` (`group_id`),
  KEY `FKSA00000000000003` (`product_id`),
  KEY `FK_gsim_id` (`gsim_id`),
  KEY `FK_savings_account_tax_group` (`tax_group_id`),
  KEY `FK_savings_account_created_by` (`created_by`),
  KEY `FK_savings_account_last_modified_by` (`last_modified_by`),
  CONSTRAINT `FKSA00000000000001` FOREIGN KEY (`client_id`) REFERENCES `m_client` (`id`),
  CONSTRAINT `FKSA00000000000002` FOREIGN KEY (`group_id`) REFERENCES `m_group` (`id`),
  CONSTRAINT `FKSA00000000000003` FOREIGN KEY (`product_id`) REFERENCES `m_savings_product` (`id`),
  CONSTRAINT `FK_gsim_id` FOREIGN KEY (`gsim_id`) REFERENCES `gsim_accounts` (`id`),
  CONSTRAINT `FK_savings_account_created_by` FOREIGN KEY (`created_by`) REFERENCES `m_appuser` (`id`),
  CONSTRAINT `FK_savings_account_last_modified_by` FOREIGN KEY (`last_modified_by`) REFERENCES `m_appuser` (`id`),
  CONSTRAINT `FK_savings_account_tax_group` FOREIGN KEY (`tax_group_id`) REFERENCES `m_tax_group` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `m_savings_account_charge` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `savings_account_id` bigint(20) NOT NULL,
  `charge_id` bigint(20) NOT NULL,
  `is_penalty` tinyint(1) NOT NULL DEFAULT 0,
  `charge_time_enum` smallint(6) NOT NULL,
  `charge_due_date` date DEFAULT NULL,
  `fee_on_month` smallint(6) DEFAULT NULL,
  `fee_on_day` smallint(6) DEFAULT NULL,
  `fee_interval` smallint(6) DEFAULT NULL,
  `free_withdrawal_count` int(11) DEFAULT 0,
  `charge_reset_date` date DEFAULT NULL,
  `charge_calculation_enum` smallint(6) NOT NULL,
  `calculation_percentage` decimal(19,6) DEFAULT NULL,
  `calculation_on_amount` decimal(19,6) DEFAULT NULL,
  `amount` decimal(19,6) NOT NULL,
  `amount_paid_derived` decimal(19,6) DEFAULT NULL,
  `amount_waived_derived` decimal(19,6) DEFAULT NULL,
  `amount_writtenoff_derived` decimal(19,6) DEFAULT NULL,
  `amount_outstanding_derived` decimal(19,6) NOT NULL DEFAULT 0.000000,
  `is_paid_derived` tinyint(1) NOT NULL DEFAULT 0,
  `waived` tinyint(1) NOT NULL DEFAULT 0,
  `is_active` tinyint(1) NOT NULL DEFAULT 1,
  `inactivated_on_date` date DEFAULT NULL,
  `created_by` bigint(20) NOT NULL,
  `last_modified_by` bigint(20) NOT NULL,
  `created_on_utc` datetime(6) NOT NULL,
  `last_modified_on_utc` datetime(6) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_savings_account_charge_charge` (`charge_id`),
  KEY `m_savings_account_charge_ibfk_2` (`savings_account_id`),
  KEY `FK_savings_account_charge_created_by` (`created_by`),
  KEY `FK_savings_account_charge_last_modified_by` (`last_modified_by`),
  CONSTRAINT `FK_savings_account_charge_created_by` FOREIGN KEY (`created_by`) REFERENCES `m_appuser` (`id`),
  CONSTRAINT `FK_savings_account_charge_last_modified_by` FOREIGN KEY (`last_modified_by`) REFERENCES `m_appuser` (`id`),
  CONSTRAINT `m_savings_account_charge_ibfk_1` FOREIGN KEY (`charge_id`) REFERENCES `m_charge` (`id`),
  CONSTRAINT `m_savings_account_charge_ibfk_2` FOREIGN KEY (`savings_account_id`) REFERENCES `m_savings_account` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `m_savings_account_charge_paid_by` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `savings_account_transaction_id` bigint(20) NOT NULL,
  `savings_account_charge_id` bigint(20) NOT NULL,
  `amount` decimal(19,6) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK__m_savings_account_charge` (`savings_account_charge_id`),
  KEY `FK__m_savings_account_transaction` (`savings_account_transaction_id`),
  CONSTRAINT `FK__m_savings_account_charge` FOREIGN KEY (`savings_account_charge_id`) REFERENCES `m_savings_account_charge` (`id`),
  CONSTRAINT `FK__m_savings_account_transaction` FOREIGN KEY (`savings_account_transaction_id`) REFERENCES `m_savings_account_transaction` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `m_savings_account_interest_rate_chart` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `savings_account_id` bigint(20) NOT NULL,
  `name` varchar(100) DEFAULT NULL,
  `description` varchar(200) DEFAULT NULL,
  `from_date` date NOT NULL,
  `end_date` date DEFAULT NULL,
  `is_primary_grouping_by_amount` tinyint(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `FKSAIRC00000000000001` (`savings_account_id`),
  CONSTRAINT `FKSAIRC00000000000001` FOREIGN KEY (`savings_account_id`) REFERENCES `m_savings_account` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `m_savings_account_interest_rate_slab` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `savings_account_interest_rate_chart_id` bigint(20) NOT NULL,
  `description` varchar(200) DEFAULT NULL,
  `period_type_enum` smallint(6) DEFAULT NULL,
  `from_period` int(11) DEFAULT NULL,
  `to_period` int(11) DEFAULT NULL,
  `amount_range_from` decimal(19,6) DEFAULT NULL,
  `amount_range_to` decimal(19,6) DEFAULT NULL,
  `annual_interest_rate` decimal(19,6) NOT NULL,
  `currency_code` varchar(3) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKSAIRS00000000000001` (`savings_account_interest_rate_chart_id`),
  CONSTRAINT `FKSAIRS00000000000001` FOREIGN KEY (`savings_account_interest_rate_chart_id`) REFERENCES `m_savings_account_interest_rate_chart` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `m_savings_account_transaction` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `savings_account_id` bigint(20) NOT NULL,
  `office_id` bigint(20) NOT NULL,
  `payment_detail_id` bigint(20) DEFAULT NULL,
  `transaction_type_enum` smallint(6) NOT NULL,
  `is_reversed` tinyint(1) NOT NULL,
  `transaction_date` date NOT NULL,
  `amount` decimal(19,6) NOT NULL,
  `overdraft_amount_derived` decimal(19,6) DEFAULT NULL,
  `balance_end_date_derived` date DEFAULT NULL,
  `balance_number_of_days_derived` int(11) DEFAULT NULL,
  `running_balance_derived` decimal(19,6) DEFAULT NULL,
  `cumulative_balance_derived` decimal(19,6) DEFAULT NULL,
  `created_date` datetime DEFAULT NULL,
  `created_by` bigint(20) NOT NULL,
  `is_manual` tinyint(1) DEFAULT 0,
  `release_id_of_hold_amount` bigint(20) DEFAULT NULL,
  `is_loan_disbursement` tinyint(1) DEFAULT NULL,
  `ref_no` varchar(128) DEFAULT NULL,
  `original_transaction_id` bigint(20) DEFAULT NULL,
  `is_reversal` tinyint(1) NOT NULL DEFAULT 0,
  `reason_for_block` varchar(256) DEFAULT NULL,
  `is_lien_transaction` tinyint(1) NOT NULL DEFAULT 0,
  `submitted_on_date` date NOT NULL,
  `last_modified_by` bigint(20) NOT NULL,
  `created_on_utc` datetime(6) NOT NULL,
  `last_modified_on_utc` datetime(6) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKSAT0000000001` (`savings_account_id`),
  KEY `FK_m_savings_account_transaction_m_office` (`office_id`),
  KEY `FK_m_savings_account_transaction_m_payment_detail` (`payment_detail_id`),
  KEY `FK_savings_account_transaction_created_by` (`created_by`),
  KEY `FK_savings_account_transaction_last_modified_by` (`last_modified_by`),
  CONSTRAINT `FKSAT0000000001` FOREIGN KEY (`savings_account_id`) REFERENCES `m_savings_account` (`id`),
  CONSTRAINT `FK_m_savings_account_transaction_m_office` FOREIGN KEY (`office_id`) REFERENCES `m_office` (`id`),
  CONSTRAINT `FK_m_savings_account_transaction_m_payment_detail` FOREIGN KEY (`payment_detail_id`) REFERENCES `m_payment_detail` (`id`),
  CONSTRAINT `FK_savings_account_transaction_created_by` FOREIGN KEY (`created_by`) REFERENCES `m_appuser` (`id`),
  CONSTRAINT `FK_savings_account_transaction_last_modified_by` FOREIGN KEY (`last_modified_by`) REFERENCES `m_appuser` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `m_savings_account_transaction_tax_details` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `savings_transaction_id` bigint(20) NOT NULL,
  `tax_component_id` bigint(20) NOT NULL,
  `amount` decimal(19,6) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_savings_account_transaction_tax_details_savings_transaction` (`savings_transaction_id`),
  KEY `FK_savings_account_transaction_tax_details_tax_component` (`tax_component_id`),
  CONSTRAINT `FK_savings_account_transaction_tax_details_savings_transaction` FOREIGN KEY (`savings_transaction_id`) REFERENCES `m_savings_account_transaction` (`id`),
  CONSTRAINT `FK_savings_account_transaction_tax_details_tax_component` FOREIGN KEY (`tax_component_id`) REFERENCES `m_tax_component` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `m_savings_interest_incentives` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `deposit_account_interest_rate_slab_id` bigint(20) NOT NULL,
  `entiry_type` smallint(6) NOT NULL,
  `attribute_name` smallint(6) NOT NULL,
  `condition_type` smallint(6) NOT NULL,
  `attribute_value` varchar(50) NOT NULL,
  `incentive_type` smallint(6) NOT NULL,
  `amount` decimal(19,6) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_m_savings_interest_incentives_m_savings_interest_rate_slab` (`deposit_account_interest_rate_slab_id`),
  CONSTRAINT `FK_m_savings_interest_incentives_m_savings_interest_rate_slab` FOREIGN KEY (`deposit_account_interest_rate_slab_id`) REFERENCES `m_savings_account_interest_rate_slab` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `m_savings_officer_assignment_history` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `account_id` bigint(20) NOT NULL,
  `savings_officer_id` bigint(20) DEFAULT NULL,
  `start_date` date NOT NULL,
  `end_date` date DEFAULT NULL,
  `created_by` bigint(20) NOT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `lastmodified_date` datetime(6) DEFAULT NULL,
  `last_modified_by` bigint(20) NOT NULL,
  `created_on_utc` datetime(6) NOT NULL,
  `last_modified_on_utc` datetime(6) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_m_savings_officer_assignment_history_0001` (`account_id`),
  KEY `fk_m_savings_officer_assignment_history_0002` (`savings_officer_id`),
  KEY `FK_savings_officer_assignment_history_created_by` (`created_by`),
  KEY `FK_savings_officer_assignment_history_last_modified_by` (`last_modified_by`),
  CONSTRAINT `FK_savings_officer_assignment_history_created_by` FOREIGN KEY (`created_by`) REFERENCES `m_appuser` (`id`),
  CONSTRAINT `FK_savings_officer_assignment_history_last_modified_by` FOREIGN KEY (`last_modified_by`) REFERENCES `m_appuser` (`id`),
  CONSTRAINT `fk_m_savings_officer_assignment_history_0001` FOREIGN KEY (`account_id`) REFERENCES `m_savings_account` (`id`),
  CONSTRAINT `fk_m_savings_officer_assignment_history_0002` FOREIGN KEY (`savings_officer_id`) REFERENCES `m_staff` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `m_savings_product` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL,
  `short_name` varchar(4) NOT NULL,
  `description` varchar(500) DEFAULT NULL,
  `deposit_type_enum` smallint(6) NOT NULL DEFAULT 100,
  `currency_code` varchar(3) NOT NULL,
  `currency_digits` smallint(6) NOT NULL,
  `currency_multiplesof` smallint(6) DEFAULT NULL,
  `nominal_annual_interest_rate` decimal(19,6) NOT NULL,
  `interest_compounding_period_enum` smallint(6) NOT NULL,
  `interest_posting_period_enum` smallint(6) NOT NULL DEFAULT 4,
  `interest_calculation_type_enum` smallint(6) NOT NULL,
  `interest_calculation_days_in_year_type_enum` smallint(6) NOT NULL,
  `min_required_opening_balance` decimal(19,6) DEFAULT NULL,
  `lockin_period_frequency` decimal(19,6) DEFAULT NULL,
  `lockin_period_frequency_enum` smallint(6) DEFAULT NULL,
  `accounting_type` smallint(6) NOT NULL,
  `withdrawal_fee_amount` decimal(19,6) DEFAULT NULL,
  `withdrawal_fee_type_enum` smallint(6) DEFAULT NULL,
  `withdrawal_fee_for_transfer` tinyint(1) DEFAULT 1,
  `allow_overdraft` tinyint(1) NOT NULL DEFAULT 0,
  `overdraft_limit` decimal(19,6) DEFAULT NULL,
  `nominal_annual_interest_rate_overdraft` decimal(19,6) DEFAULT 0.000000,
  `min_overdraft_for_interest_calculation` decimal(19,6) DEFAULT 0.000000,
  `min_required_balance` decimal(19,6) DEFAULT NULL,
  `enforce_min_required_balance` tinyint(1) NOT NULL DEFAULT 0,
  `min_balance_for_interest_calculation` decimal(19,6) DEFAULT NULL,
  `withhold_tax` tinyint(1) NOT NULL DEFAULT 0,
  `tax_group_id` bigint(20) DEFAULT NULL,
  `is_dormancy_tracking_active` tinyint(1) DEFAULT NULL,
  `days_to_inactive` int(11) DEFAULT NULL,
  `days_to_dormancy` int(11) DEFAULT NULL,
  `days_to_escheat` int(11) DEFAULT NULL,
  `max_allowed_lien_limit` decimal(19,6) DEFAULT NULL,
  `is_lien_allowed` tinyint(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`),
  UNIQUE KEY `short_name` (`short_name`),
  KEY `FK_savings_product_tax_group` (`tax_group_id`),
  CONSTRAINT `FK_savings_product_tax_group` FOREIGN KEY (`tax_group_id`) REFERENCES `m_tax_group` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `m_savings_product_charge` (
  `savings_product_id` bigint(20) NOT NULL,
  `charge_id` bigint(20) NOT NULL,
  PRIMARY KEY (`savings_product_id`,`charge_id`),
  KEY `fk_savings_product_charge_charge` (`charge_id`),
  CONSTRAINT `m_savings_product_charge_ibfk_1` FOREIGN KEY (`charge_id`) REFERENCES `m_charge` (`id`),
  CONSTRAINT `m_savings_product_charge_ibfk_2` FOREIGN KEY (`savings_product_id`) REFERENCES `m_savings_product` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `m_share_account` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `account_no` varchar(50) NOT NULL,
  `product_id` bigint(20) NOT NULL,
  `client_id` bigint(20) NOT NULL,
  `external_id` varchar(100) DEFAULT NULL,
  `status_enum` smallint(6) NOT NULL DEFAULT 300,
  `total_approved_shares` bigint(20) DEFAULT NULL,
  `total_pending_shares` bigint(20) DEFAULT NULL,
  `submitted_date` date NOT NULL,
  `submitted_userid` bigint(20) DEFAULT NULL,
  `approved_date` date DEFAULT NULL,
  `approved_userid` bigint(20) DEFAULT NULL,
  `rejected_date` date DEFAULT NULL,
  `rejected_userid` bigint(20) DEFAULT NULL,
  `activated_date` date DEFAULT NULL,
  `activated_userid` bigint(20) DEFAULT NULL,
  `closed_date` date DEFAULT NULL,
  `closed_userid` bigint(20) DEFAULT NULL,
  `currency_code` varchar(3) NOT NULL,
  `currency_digits` smallint(6) NOT NULL,
  `currency_multiplesof` smallint(6) DEFAULT NULL,
  `savings_account_id` bigint(20) NOT NULL,
  `minimum_active_period_frequency` decimal(19,6) DEFAULT NULL,
  `minimum_active_period_frequency_enum` smallint(6) DEFAULT NULL,
  `lockin_period_frequency` decimal(19,6) DEFAULT NULL,
  `lockin_period_frequency_enum` smallint(6) DEFAULT NULL,
  `allow_dividends_inactive_clients` tinyint(1) DEFAULT 0,
  `created_date` datetime(6) DEFAULT NULL,
  `lastmodifiedby_id` bigint(20) DEFAULT NULL,
  `lastmodified_date` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `m_share_account_ibfk_1` (`product_id`),
  KEY `m_share_account_ibfk_2` (`savings_account_id`),
  KEY `m_share_account_ibfk_3` (`submitted_userid`),
  KEY `m_share_account_ibfk_4` (`approved_userid`),
  KEY `m_share_account_ibfk_5` (`rejected_userid`),
  KEY `m_share_account_ibfk_6` (`activated_userid`),
  KEY `m_share_account_ibfk_7` (`closed_userid`),
  KEY `m_share_account_ibfk_8` (`lastmodifiedby_id`),
  KEY `m_share_account_ibfk_9` (`client_id`),
  CONSTRAINT `m_share_account_ibfk_1` FOREIGN KEY (`product_id`) REFERENCES `m_share_product` (`id`),
  CONSTRAINT `m_share_account_ibfk_2` FOREIGN KEY (`savings_account_id`) REFERENCES `m_savings_account` (`id`),
  CONSTRAINT `m_share_account_ibfk_3` FOREIGN KEY (`submitted_userid`) REFERENCES `m_appuser` (`id`),
  CONSTRAINT `m_share_account_ibfk_4` FOREIGN KEY (`approved_userid`) REFERENCES `m_appuser` (`id`),
  CONSTRAINT `m_share_account_ibfk_5` FOREIGN KEY (`rejected_userid`) REFERENCES `m_appuser` (`id`),
  CONSTRAINT `m_share_account_ibfk_6` FOREIGN KEY (`activated_userid`) REFERENCES `m_appuser` (`id`),
  CONSTRAINT `m_share_account_ibfk_7` FOREIGN KEY (`closed_userid`) REFERENCES `m_appuser` (`id`),
  CONSTRAINT `m_share_account_ibfk_8` FOREIGN KEY (`lastmodifiedby_id`) REFERENCES `m_appuser` (`id`),
  CONSTRAINT `m_share_account_ibfk_9` FOREIGN KEY (`client_id`) REFERENCES `m_client` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `m_share_account_charge` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `account_id` bigint(20) NOT NULL,
  `charge_id` bigint(20) NOT NULL,
  `charge_time_enum` smallint(6) NOT NULL,
  `charge_calculation_enum` smallint(6) NOT NULL,
  `charge_payment_mode_enum` smallint(6) NOT NULL DEFAULT 0,
  `calculation_percentage` decimal(19,6) DEFAULT NULL,
  `calculation_on_amount` decimal(19,6) DEFAULT NULL,
  `charge_amount_or_percentage` decimal(19,6) DEFAULT NULL,
  `amount` decimal(19,6) NOT NULL,
  `amount_paid_derived` decimal(19,6) DEFAULT NULL,
  `amount_waived_derived` decimal(19,6) DEFAULT NULL,
  `amount_writtenoff_derived` decimal(19,6) DEFAULT NULL,
  `amount_outstanding_derived` decimal(19,6) NOT NULL DEFAULT 0.000000,
  `is_paid_derived` tinyint(1) NOT NULL DEFAULT 0,
  `waived` tinyint(1) NOT NULL DEFAULT 0,
  `min_cap` decimal(19,6) DEFAULT NULL,
  `max_cap` decimal(19,6) DEFAULT NULL,
  `is_active` tinyint(1) NOT NULL DEFAULT 1,
  PRIMARY KEY (`id`),
  KEY `fk_share_account_charge_charge` (`charge_id`),
  KEY `m_share_account_charge_ibfk_2` (`account_id`),
  CONSTRAINT `m_share_account_charge_ibfk_1` FOREIGN KEY (`charge_id`) REFERENCES `m_charge` (`id`),
  CONSTRAINT `m_share_account_charge_ibfk_2` FOREIGN KEY (`account_id`) REFERENCES `m_share_account` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `m_share_account_charge_paid_by` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `share_transaction_id` bigint(20) DEFAULT NULL,
  `charge_transaction_id` bigint(20) DEFAULT NULL,
  `amount` decimal(20,2) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `m_share_account_transactions_charge_mapping_ibfk1` (`share_transaction_id`),
  KEY `m_share_account_transactions_charge_mapping_ibfk2` (`charge_transaction_id`),
  CONSTRAINT `m_share_account_transactions_charge_mapping_ibfk1` FOREIGN KEY (`share_transaction_id`) REFERENCES `m_share_account_transactions` (`id`),
  CONSTRAINT `m_share_account_transactions_charge_mapping_ibfk2` FOREIGN KEY (`charge_transaction_id`) REFERENCES `m_share_account_charge` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `m_share_account_dividend_details` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `dividend_pay_out_id` bigint(20) NOT NULL,
  `account_id` bigint(20) NOT NULL,
  `amount` decimal(19,6) NOT NULL,
  `status` smallint(6) NOT NULL,
  `savings_transaction_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_m_share_account_dividend_details_account_id` (`account_id`),
  KEY `FK_m_share_account_dividend_details_dividend_pay_out_id` (`dividend_pay_out_id`),
  CONSTRAINT `FK_m_share_account_dividend_details_account_id` FOREIGN KEY (`account_id`) REFERENCES `m_share_account` (`id`),
  CONSTRAINT `FK_m_share_account_dividend_details_dividend_pay_out_id` FOREIGN KEY (`dividend_pay_out_id`) REFERENCES `m_share_product_dividend_pay_out` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `m_share_account_transactions` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `account_id` bigint(20) NOT NULL,
  `transaction_date` date DEFAULT NULL,
  `total_shares` bigint(20) DEFAULT NULL,
  `unit_price` decimal(10,2) DEFAULT NULL,
  `amount` decimal(20,2) DEFAULT NULL,
  `charge_amount` decimal(20,2) DEFAULT NULL,
  `amount_paid` decimal(20,2) DEFAULT NULL,
  `status_enum` smallint(6) NOT NULL DEFAULT 300,
  `type_enum` smallint(6) DEFAULT NULL,
  `is_active` tinyint(1) NOT NULL DEFAULT 1,
  PRIMARY KEY (`id`),
  KEY `m_share_account_purchased_shares_ibfk_1` (`account_id`),
  CONSTRAINT `m_share_account_purchased_shares_ibfk_1` FOREIGN KEY (`account_id`) REFERENCES `m_share_account` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `m_share_product` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(200) NOT NULL,
  `short_name` varchar(4) NOT NULL,
  `external_id` varchar(100) DEFAULT NULL,
  `description` varchar(500) NOT NULL,
  `start_date` date DEFAULT NULL,
  `end_date` date DEFAULT NULL,
  `currency_code` varchar(3) NOT NULL,
  `currency_digits` smallint(6) NOT NULL,
  `currency_multiplesof` smallint(6) DEFAULT NULL,
  `total_shares` bigint(20) NOT NULL,
  `issued_shares` bigint(20) DEFAULT NULL,
  `totalsubscribed_shares` bigint(20) DEFAULT NULL,
  `unit_price` decimal(10,2) NOT NULL,
  `capital_amount` decimal(20,2) NOT NULL,
  `minimum_client_shares` bigint(20) DEFAULT NULL,
  `nominal_client_shares` bigint(20) NOT NULL,
  `maximum_client_shares` bigint(20) DEFAULT NULL,
  `minimum_active_period_frequency` decimal(19,6) DEFAULT NULL,
  `minimum_active_period_frequency_enum` smallint(6) DEFAULT NULL,
  `lockin_period_frequency` decimal(19,6) DEFAULT NULL,
  `lockin_period_frequency_enum` smallint(6) DEFAULT NULL,
  `allow_dividends_inactive_clients` tinyint(1) DEFAULT 0,
  `createdby_id` bigint(20) DEFAULT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `lastmodifiedby_id` bigint(20) DEFAULT NULL,
  `lastmodified_date` datetime(6) DEFAULT NULL,
  `accounting_type` smallint(6) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`),
  KEY `m_share_product_ibfk_1` (`createdby_id`),
  KEY `m_share_product_ibfk_2` (`lastmodifiedby_id`),
  CONSTRAINT `m_share_product_ibfk_1` FOREIGN KEY (`createdby_id`) REFERENCES `m_appuser` (`id`),
  CONSTRAINT `m_share_product_ibfk_2` FOREIGN KEY (`lastmodifiedby_id`) REFERENCES `m_appuser` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `m_share_product_charge` (
  `product_id` bigint(20) NOT NULL,
  `charge_id` bigint(20) NOT NULL,
  PRIMARY KEY (`product_id`,`charge_id`),
  KEY `m_share_product_charge_ibfk_1` (`charge_id`),
  CONSTRAINT `m_share_product_charge_ibfk_1` FOREIGN KEY (`charge_id`) REFERENCES `m_charge` (`id`),
  CONSTRAINT `m_share_product_charge_ibfk_2` FOREIGN KEY (`product_id`) REFERENCES `m_share_product` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `m_share_product_dividend_pay_out` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `product_id` bigint(20) NOT NULL,
  `amount` decimal(19,6) NOT NULL,
  `dividend_period_start_date` date NOT NULL,
  `dividend_period_end_date` date NOT NULL,
  `status` smallint(6) NOT NULL,
  `createdby_id` bigint(20) DEFAULT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `lastmodifiedby_id` bigint(20) DEFAULT NULL,
  `lastmodified_date` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_m_share_product_dividend_pay_out_createdby_id` (`createdby_id`),
  KEY `FK_m_share_product_dividend_pay_out_lastmodifiedby_id` (`lastmodifiedby_id`),
  KEY `FK_m_share_product_dividend_pay_out_product_id` (`product_id`),
  CONSTRAINT `FK_m_share_product_dividend_pay_out_createdby_id` FOREIGN KEY (`createdby_id`) REFERENCES `m_appuser` (`id`),
  CONSTRAINT `FK_m_share_product_dividend_pay_out_lastmodifiedby_id` FOREIGN KEY (`lastmodifiedby_id`) REFERENCES `m_appuser` (`id`),
  CONSTRAINT `FK_m_share_product_dividend_pay_out_product_id` FOREIGN KEY (`product_id`) REFERENCES `m_share_product` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `m_share_product_market_price` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `product_id` bigint(20) NOT NULL,
  `from_date` date DEFAULT NULL,
  `share_value` decimal(10,2) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `m_share_product_market_price_ibfk_1` (`product_id`),
  CONSTRAINT `m_share_product_market_price_ibfk_1` FOREIGN KEY (`product_id`) REFERENCES `m_share_product` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `m_staff` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `is_loan_officer` tinyint(1) NOT NULL DEFAULT 0,
  `office_id` bigint(20) DEFAULT NULL,
  `firstname` varchar(50) DEFAULT NULL,
  `lastname` varchar(50) DEFAULT NULL,
  `display_name` varchar(102) NOT NULL,
  `mobile_no` varchar(50) DEFAULT NULL,
  `external_id` varchar(100) DEFAULT NULL,
  `organisational_role_enum` smallint(6) DEFAULT NULL,
  `organisational_role_parent_staff_id` bigint(20) DEFAULT NULL,
  `is_active` tinyint(1) NOT NULL DEFAULT 1,
  `joining_date` date DEFAULT NULL,
  `image_id` bigint(20) DEFAULT NULL,
  `email_address` varchar(150) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `display_name` (`display_name`),
  UNIQUE KEY `mobile_no` (`mobile_no`),
  UNIQUE KEY `external_id` (`external_id`),
  KEY `FK_m_staff_m_image` (`image_id`),
  KEY `FK_m_staff_m_office` (`office_id`),
  CONSTRAINT `FK_m_staff_m_image` FOREIGN KEY (`image_id`) REFERENCES `m_image` (`id`),
  CONSTRAINT `FK_m_staff_m_office` FOREIGN KEY (`office_id`) REFERENCES `m_office` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `m_staff_assignment_history` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `centre_id` bigint(20) DEFAULT NULL,
  `staff_id` bigint(20) NOT NULL,
  `start_date` date NOT NULL,
  `end_date` date DEFAULT NULL,
  `createdby_id` bigint(20) DEFAULT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `lastmodified_date` datetime(6) DEFAULT NULL,
  `lastmodifiedby_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_m_staff_assignment_history_centre_id_m_group` (`centre_id`),
  KEY `FK_m_staff_assignment_history_m_staff` (`staff_id`),
  CONSTRAINT `FK_m_staff_assignment_history_centre_id_m_group` FOREIGN KEY (`centre_id`) REFERENCES `m_group` (`id`),
  CONSTRAINT `FK_m_staff_assignment_history_m_staff` FOREIGN KEY (`staff_id`) REFERENCES `m_staff` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `m_survey_components` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `survey_id` bigint(20) NOT NULL,
  `a_key` varchar(32) NOT NULL,
  `a_text` varchar(255) NOT NULL,
  `description` varchar(4000) DEFAULT NULL,
  `sequence_no` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `survey_id` (`survey_id`),
  CONSTRAINT `m_survey_components_ibfk_1` FOREIGN KEY (`survey_id`) REFERENCES `m_surveys` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `m_survey_lookup_tables` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `survey_id` bigint(20) NOT NULL,
  `a_key` varchar(255) NOT NULL,
  `description` int(11) DEFAULT NULL,
  `value_from` int(11) NOT NULL,
  `value_to` int(11) NOT NULL,
  `score` decimal(5,2) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `survey_lookup_tables_survey_id` (`survey_id`),
  CONSTRAINT `m_survey_lookup_tables_ibfk_1` FOREIGN KEY (`survey_id`) REFERENCES `m_surveys` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `m_survey_questions` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `survey_id` bigint(20) NOT NULL,
  `component_key` varchar(32) DEFAULT NULL,
  `a_key` varchar(32) NOT NULL,
  `a_text` varchar(255) NOT NULL,
  `description` varchar(4000) DEFAULT NULL,
  `sequence_no` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `survey_questions_survey_id` (`survey_id`),
  CONSTRAINT `m_survey_questions_ibfk_1` FOREIGN KEY (`survey_id`) REFERENCES `m_surveys` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `m_survey_responses` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `question_id` bigint(20) NOT NULL,
  `a_text` varchar(255) NOT NULL,
  `a_value` int(11) NOT NULL,
  `sequence_no` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `survey_responses_question_id` (`question_id`),
  CONSTRAINT `m_survey_responses_ibfk_1` FOREIGN KEY (`question_id`) REFERENCES `m_survey_questions` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `m_survey_scorecards` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `survey_id` bigint(20) NOT NULL,
  `question_id` bigint(20) NOT NULL,
  `response_id` bigint(20) NOT NULL,
  `user_id` bigint(20) NOT NULL,
  `client_id` bigint(20) NOT NULL,
  `created_on` datetime(6) DEFAULT NULL,
  `a_value` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_survey_scorecards_client` (`client_id`),
  KEY `survey_scorecards_question_id` (`question_id`),
  KEY `response_id` (`response_id`),
  KEY `survey_scorecards_survey_id` (`survey_id`),
  KEY `survey_scorecards_user_id` (`user_id`),
  CONSTRAINT `m_survey_scorecards_ibfk_1` FOREIGN KEY (`survey_id`) REFERENCES `m_surveys` (`id`),
  CONSTRAINT `m_survey_scorecards_ibfk_2` FOREIGN KEY (`question_id`) REFERENCES `m_survey_questions` (`id`),
  CONSTRAINT `m_survey_scorecards_ibfk_3` FOREIGN KEY (`response_id`) REFERENCES `m_survey_responses` (`id`),
  CONSTRAINT `m_survey_scorecards_ibfk_4` FOREIGN KEY (`user_id`) REFERENCES `m_appuser` (`id`),
  CONSTRAINT `m_survey_scorecards_ibfk_5` FOREIGN KEY (`client_id`) REFERENCES `m_client` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `m_surveys` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `a_key` varchar(32) NOT NULL,
  `a_name` varchar(255) NOT NULL,
  `description` varchar(4000) DEFAULT NULL,
  `country_code` varchar(2) NOT NULL,
  `valid_from` date DEFAULT NULL,
  `valid_to` date DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `m_tax_component` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(50) NOT NULL,
  `percentage` decimal(19,6) NOT NULL,
  `debit_account_type_enum` smallint(6) DEFAULT NULL,
  `debit_account_id` bigint(20) DEFAULT NULL,
  `credit_account_type_enum` smallint(6) DEFAULT NULL,
  `credit_account_id` bigint(20) DEFAULT NULL,
  `start_date` date NOT NULL,
  `createdby_id` bigint(20) NOT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `lastmodifiedby_id` bigint(20) NOT NULL,
  `lastmodified_date` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_tax_component_createdby` (`createdby_id`),
  KEY `FK_tax_component_credit_gl_account` (`credit_account_id`),
  KEY `FK_tax_component_debit_gl_account` (`debit_account_id`),
  KEY `FK_tax_component_lastmodifiedby` (`lastmodifiedby_id`),
  CONSTRAINT `FK_tax_component_createdby` FOREIGN KEY (`createdby_id`) REFERENCES `m_appuser` (`id`),
  CONSTRAINT `FK_tax_component_credit_gl_account` FOREIGN KEY (`credit_account_id`) REFERENCES `acc_gl_account` (`id`),
  CONSTRAINT `FK_tax_component_debit_gl_account` FOREIGN KEY (`debit_account_id`) REFERENCES `acc_gl_account` (`id`),
  CONSTRAINT `FK_tax_component_lastmodifiedby` FOREIGN KEY (`lastmodifiedby_id`) REFERENCES `m_appuser` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `m_tax_component_history` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `tax_component_id` bigint(20) NOT NULL,
  `percentage` decimal(19,6) NOT NULL,
  `start_date` date NOT NULL,
  `end_date` date NOT NULL,
  `createdby_id` bigint(20) NOT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `lastmodifiedby_id` bigint(20) NOT NULL,
  `lastmodified_date` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_tax_component_history_createdby` (`createdby_id`),
  KEY `FK_tax_component_history_lastmodifiedby` (`lastmodifiedby_id`),
  KEY `FK_tax_component_history_tax_component_id` (`tax_component_id`),
  CONSTRAINT `FK_tax_component_history_createdby` FOREIGN KEY (`createdby_id`) REFERENCES `m_appuser` (`id`),
  CONSTRAINT `FK_tax_component_history_lastmodifiedby` FOREIGN KEY (`lastmodifiedby_id`) REFERENCES `m_appuser` (`id`),
  CONSTRAINT `FK_tax_component_history_tax_component_id` FOREIGN KEY (`tax_component_id`) REFERENCES `m_tax_component` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `m_tax_group` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(50) NOT NULL,
  `createdby_id` bigint(20) NOT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `lastmodifiedby_id` bigint(20) NOT NULL,
  `lastmodified_date` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_tax_group_createdby` (`createdby_id`),
  KEY `FK_tax_group_lastmodifiedby` (`lastmodifiedby_id`),
  CONSTRAINT `FK_tax_group_createdby` FOREIGN KEY (`createdby_id`) REFERENCES `m_appuser` (`id`),
  CONSTRAINT `FK_tax_group_lastmodifiedby` FOREIGN KEY (`lastmodifiedby_id`) REFERENCES `m_appuser` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `m_tax_group_mappings` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `tax_group_id` bigint(20) NOT NULL,
  `tax_component_id` bigint(20) NOT NULL,
  `start_date` date NOT NULL,
  `end_date` date DEFAULT NULL,
  `createdby_id` bigint(20) NOT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `lastmodifiedby_id` bigint(20) NOT NULL,
  `lastmodified_date` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_tax_group_mappings_createdby` (`createdby_id`),
  KEY `FK_tax_group_mappings_lastmodifiedby` (`lastmodifiedby_id`),
  KEY `FK_tax_group_mappings_tax_component` (`tax_component_id`),
  KEY `FK_tax_group_mappings_tax_group` (`tax_group_id`),
  CONSTRAINT `FK_tax_group_mappings_createdby` FOREIGN KEY (`createdby_id`) REFERENCES `m_appuser` (`id`),
  CONSTRAINT `FK_tax_group_mappings_lastmodifiedby` FOREIGN KEY (`lastmodifiedby_id`) REFERENCES `m_appuser` (`id`),
  CONSTRAINT `FK_tax_group_mappings_tax_component` FOREIGN KEY (`tax_component_id`) REFERENCES `m_tax_component` (`id`),
  CONSTRAINT `FK_tax_group_mappings_tax_group` FOREIGN KEY (`tax_group_id`) REFERENCES `m_tax_group` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `m_tellers` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `office_id` bigint(20) NOT NULL,
  `debit_account_id` bigint(20) DEFAULT NULL,
  `credit_account_id` bigint(20) DEFAULT NULL,
  `name` varchar(50) NOT NULL,
  `description` varchar(100) DEFAULT NULL,
  `valid_from` date DEFAULT NULL,
  `valid_to` date DEFAULT NULL,
  `state` smallint(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`),
  KEY `FK_m_tellers_gl_account_credit_account_id` (`credit_account_id`),
  KEY `FK_m_tellers_gl_account_debit_account_id` (`debit_account_id`),
  KEY `IK_m_tellers_m_office` (`office_id`),
  CONSTRAINT `FK_m_tellers_gl_account_credit_account_id` FOREIGN KEY (`credit_account_id`) REFERENCES `acc_gl_account` (`id`),
  CONSTRAINT `FK_m_tellers_gl_account_debit_account_id` FOREIGN KEY (`debit_account_id`) REFERENCES `acc_gl_account` (`id`),
  CONSTRAINT `FK_m_tellers_m_office` FOREIGN KEY (`office_id`) REFERENCES `m_office` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `m_template` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  `text` longtext NOT NULL,
  `entity` int(11) DEFAULT NULL,
  `type` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `m_template_m_templatemappers` (
  `m_template_id` bigint(20) NOT NULL,
  `mappers_id` bigint(20) NOT NULL,
  UNIQUE KEY `mappers_id` (`mappers_id`),
  KEY `m_template_id` (`m_template_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `m_templatemappers` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `mapperkey` varchar(255) DEFAULT NULL,
  `mapperorder` int(11) DEFAULT NULL,
  `mappervalue` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `m_trial_balance` (
  `office_id` bigint(20) NOT NULL,
  `account_id` bigint(20) NOT NULL,
  `amount` decimal(19,6) NOT NULL,
  `entry_date` date NOT NULL,
  `created_date` date DEFAULT NULL,
  `closing_balance` decimal(19,6) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `m_working_days` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `recurrence` varchar(100) DEFAULT NULL,
  `repayment_rescheduling_enum` smallint(6) DEFAULT NULL,
  `extend_term_daily_repayments` tinyint(1) DEFAULT 0,
  `extend_term_holiday_repayment` tinyint(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `mix_taxonomy` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(100) DEFAULT NULL,
  `namespace_id` int(11) DEFAULT NULL,
  `dimension` varchar(100) DEFAULT NULL,
  `type` int(11) DEFAULT NULL,
  `description` varchar(1000) DEFAULT NULL,
  `need_mapping` tinyint(1) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `mix_taxonomy_mapping` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `identifier` varchar(50) NOT NULL DEFAULT '',
  `config` varchar(200) DEFAULT NULL,
  `last_update_date` datetime(6) DEFAULT NULL,
  `currency` varchar(11) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `mix_xbrl_namespace` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `prefix` varchar(20) NOT NULL DEFAULT '',
  `url` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `prefix` (`prefix`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `notification_generator` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `object_type` text DEFAULT NULL,
  `object_identifier` bigint(20) DEFAULT NULL,
  `action` text DEFAULT NULL,
  `actor` bigint(20) DEFAULT NULL,
  `is_system_generated` tinyint(1) DEFAULT 0,
  `notification_content` text DEFAULT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `notification_mapper` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `notification_id` bigint(20) DEFAULT NULL,
  `user_id` bigint(20) DEFAULT NULL,
  `is_read` tinyint(1) DEFAULT 0,
  `created_at` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `notification_id` (`notification_id`),
  KEY `notification_mapper_user` (`user_id`),
  CONSTRAINT `notification_mapper_ibfk_2` FOREIGN KEY (`user_id`) REFERENCES `m_appuser` (`id`),
  CONSTRAINT `notification_mapper_ibfk_3` FOREIGN KEY (`notification_id`) REFERENCES `notification_generator` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `oauth_access_token` (
  `token_id` varchar(256) DEFAULT NULL,
  `token` blob DEFAULT NULL,
  `authentication_id` varchar(256) DEFAULT NULL,
  `user_name` varchar(256) DEFAULT NULL,
  `client_id` varchar(256) DEFAULT NULL,
  `authentication` blob DEFAULT NULL,
  `refresh_token` varchar(256) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `oauth_client_details` (
  `client_id` varchar(128) NOT NULL,
  `resource_ids` varchar(256) DEFAULT NULL,
  `client_secret` varchar(256) DEFAULT NULL,
  `scope` varchar(256) DEFAULT NULL,
  `authorized_grant_types` varchar(256) DEFAULT NULL,
  `web_server_redirect_uri` varchar(256) DEFAULT NULL,
  `authorities` varchar(256) DEFAULT NULL,
  `access_token_validity` int(11) DEFAULT NULL,
  `refresh_token_validity` int(11) DEFAULT NULL,
  `additional_information` varchar(4096) DEFAULT NULL,
  `autoapprove` tinyint(1) DEFAULT 0,
  PRIMARY KEY (`client_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `oauth_refresh_token` (
  `token_id` varchar(256) DEFAULT NULL,
  `token` blob DEFAULT NULL,
  `authentication` blob DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `ppi_likelihoods` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `code` varchar(100) NOT NULL,
  `name` varchar(250) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `ppi_likelihoods_ppi` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `likelihood_id` bigint(20) NOT NULL,
  `ppi_name` varchar(250) NOT NULL,
  `enabled` int(11) NOT NULL DEFAULT 100,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `ppi_scores` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `score_from` int(11) NOT NULL,
  `score_to` int(11) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `r_enum_value` (
  `enum_name` varchar(100) NOT NULL,
  `enum_id` int(11) NOT NULL,
  `enum_message_property` varchar(100) NOT NULL,
  `enum_value` varchar(100) NOT NULL,
  `enum_type` tinyint(1) NOT NULL,
  PRIMARY KEY (`enum_name`,`enum_id`),
  UNIQUE KEY `enum_message_property` (`enum_name`,`enum_message_property`),
  UNIQUE KEY `enum_value` (`enum_name`,`enum_value`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `ref_loan_transaction_processing_strategy` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `code` varchar(100) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `sort_order` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `request_audit_table` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `lastname` varchar(100) NOT NULL,
  `username` varchar(100) NOT NULL,
  `mobile_number` varchar(50) DEFAULT NULL,
  `firstname` varchar(100) NOT NULL,
  `authentication_token` varchar(100) DEFAULT NULL,
  `password` varchar(250) NOT NULL,
  `email` varchar(100) NOT NULL,
  `client_id` bigint(20) NOT NULL,
  `created_date` date NOT NULL,
  `account_number` varchar(100) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK1_request_audit_table_m_client` (`client_id`),
  CONSTRAINT `FK1_request_audit_table_m_client` FOREIGN KEY (`client_id`) REFERENCES `m_client` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `rpt_sequence` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `scheduled_email_campaign` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `campaign_name` varchar(100) NOT NULL,
  `campaign_type` int(11) NOT NULL,
  `business_rule_id` int(11) DEFAULT NULL,
  `param_value` text DEFAULT NULL,
  `status_enum` int(11) NOT NULL,
  `email_subject` varchar(100) NOT NULL,
  `email_message` text NOT NULL,
  `email_attachment_file_format` varchar(10) DEFAULT NULL,
  `stretchy_report_id` int(11) DEFAULT NULL,
  `stretchy_report_param_map` text DEFAULT NULL,
  `closedon_date` date DEFAULT NULL,
  `closedon_userid` bigint(20) DEFAULT NULL,
  `submittedon_date` date DEFAULT NULL,
  `submittedon_userid` bigint(20) DEFAULT NULL,
  `approvedon_date` date DEFAULT NULL,
  `approvedon_userid` bigint(20) DEFAULT NULL,
  `recurrence` varchar(100) DEFAULT NULL,
  `next_trigger_date` datetime(6) DEFAULT NULL,
  `last_trigger_date` datetime(6) DEFAULT NULL,
  `recurrence_start_date` datetime(6) DEFAULT NULL,
  `is_visible` tinyint(1) DEFAULT 1,
  `previous_run_error_log` text DEFAULT NULL,
  `previous_run_error_message` text DEFAULT NULL,
  `previous_run_status` varchar(10) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `scheduled_email_campaign_ibfk_1` (`stretchy_report_id`),
  CONSTRAINT `scheduled_email_campaign_ibfk_1` FOREIGN KEY (`stretchy_report_id`) REFERENCES `stretchy_report` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `scheduled_email_configuration` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(50) NOT NULL,
  `value` varchar(200) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `scheduled_email_messages_outbound` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `group_id` bigint(20) DEFAULT NULL,
  `client_id` bigint(20) DEFAULT NULL,
  `staff_id` bigint(20) DEFAULT NULL,
  `email_campaign_id` bigint(20) DEFAULT NULL,
  `status_enum` int(11) NOT NULL DEFAULT 100,
  `email_address` varchar(50) NOT NULL,
  `email_subject` varchar(50) NOT NULL,
  `message` text NOT NULL,
  `campaign_name` varchar(200) DEFAULT NULL,
  `submittedon_date` date DEFAULT NULL,
  `error_message` text DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `SEFKCLIENT00000001` (`client_id`),
  KEY `SEFKGROUP000000001` (`group_id`),
  KEY `SEFKSTAFF000000001` (`staff_id`),
  KEY `fk_schedule_email_campign1` (`email_campaign_id`),
  CONSTRAINT `SEFKCLIENT00000001` FOREIGN KEY (`client_id`) REFERENCES `m_client` (`id`),
  CONSTRAINT `SEFKGROUP000000001` FOREIGN KEY (`group_id`) REFERENCES `m_group` (`id`),
  CONSTRAINT `SEFKSTAFF000000001` FOREIGN KEY (`staff_id`) REFERENCES `m_staff` (`id`),
  CONSTRAINT `fk_schedule_email_campign1` FOREIGN KEY (`email_campaign_id`) REFERENCES `scheduled_email_campaign` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `scheduler_detail` (
  `id` smallint(6) NOT NULL AUTO_INCREMENT,
  `is_suspended` tinyint(1) NOT NULL DEFAULT 0,
  `execute_misfired_jobs` tinyint(1) NOT NULL DEFAULT 1,
  `reset_scheduler_on_bootup` tinyint(1) NOT NULL DEFAULT 1,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `sms_campaign` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `campaign_name` varchar(100) NOT NULL,
  `campaign_type` int(11) NOT NULL,
  `campaign_trigger_type` int(11) NOT NULL,
  `report_id` int(11) NOT NULL,
  `provider_id` bigint(20) DEFAULT NULL,
  `param_value` text DEFAULT NULL,
  `status_enum` int(11) NOT NULL,
  `message` text NOT NULL,
  `submittedon_date` date DEFAULT NULL,
  `submittedon_userid` bigint(20) DEFAULT NULL,
  `approvedon_date` date DEFAULT NULL,
  `approvedon_userid` bigint(20) DEFAULT NULL,
  `closedon_date` date DEFAULT NULL,
  `closedon_userid` bigint(20) DEFAULT NULL,
  `recurrence` varchar(100) DEFAULT NULL,
  `next_trigger_date` datetime(6) DEFAULT NULL,
  `last_trigger_date` datetime(6) DEFAULT NULL,
  `recurrence_start_date` datetime(6) DEFAULT NULL,
  `is_visible` tinyint(1) DEFAULT 1,
  `is_notification` tinyint(1) DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `campaign_name_UNIQUE` (`campaign_name`),
  KEY `report_id` (`report_id`),
  CONSTRAINT `sms_campaign_ibfk_1` FOREIGN KEY (`report_id`) REFERENCES `stretchy_report` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `sms_messages_outbound` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `group_id` bigint(20) DEFAULT NULL,
  `client_id` bigint(20) DEFAULT NULL,
  `staff_id` bigint(20) DEFAULT NULL,
  `status_enum` int(11) NOT NULL DEFAULT 100,
  `mobile_no` varchar(50) DEFAULT NULL,
  `message` varchar(1000) NOT NULL,
  `campaign_id` bigint(20) DEFAULT NULL,
  `external_id` varchar(100) DEFAULT NULL,
  `submittedon_date` date DEFAULT NULL,
  `delivered_on_date` datetime(6) DEFAULT NULL,
  `is_notification` tinyint(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `FKCAMPAIGN00000001` (`campaign_id`),
  KEY `FKCLIENT00000001` (`client_id`),
  KEY `FKGROUP000000001` (`group_id`),
  KEY `FKSTAFF000000001` (`staff_id`),
  CONSTRAINT `FKCAMPAIGN00000001` FOREIGN KEY (`campaign_id`) REFERENCES `sms_campaign` (`id`),
  CONSTRAINT `FKCLIENT00000001` FOREIGN KEY (`client_id`) REFERENCES `m_client` (`id`),
  CONSTRAINT `FKGROUP000000001` FOREIGN KEY (`group_id`) REFERENCES `m_group` (`id`),
  CONSTRAINT `FKSTAFF000000001` FOREIGN KEY (`staff_id`) REFERENCES `m_staff` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `stretchy_parameter` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `parameter_name` varchar(45) NOT NULL,
  `parameter_variable` varchar(45) DEFAULT NULL,
  `parameter_label` varchar(45) NOT NULL,
  `parameter_displayType` varchar(45) NOT NULL,
  `parameter_FormatType` varchar(10) NOT NULL,
  `parameter_default` varchar(45) NOT NULL,
  `special` varchar(1) DEFAULT NULL,
  `selectOne` varchar(1) DEFAULT NULL,
  `selectAll` varchar(1) DEFAULT NULL,
  `parameter_sql` text DEFAULT NULL,
  `parent_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `parameter_name` (`parameter_name`),
  KEY `fk_stretchy_parameter_001_idx` (`parent_id`),
  CONSTRAINT `fk_stretchy_parameter_001` FOREIGN KEY (`parent_id`) REFERENCES `stretchy_parameter` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `stretchy_report` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `report_name` varchar(100) NOT NULL,
  `report_type` varchar(20) NOT NULL,
  `report_subtype` varchar(20) DEFAULT NULL,
  `report_category` varchar(45) DEFAULT NULL,
  `report_sql` text DEFAULT NULL,
  `description` text DEFAULT NULL,
  `core_report` tinyint(1) DEFAULT 0,
  `use_report` tinyint(1) DEFAULT 0,
  `self_service_user_report` tinyint(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `report_name` (`report_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `stretchy_report_parameter` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `report_id` int(11) NOT NULL,
  `parameter_id` int(11) NOT NULL,
  `report_parameter_name` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `report_parameter_unique` (`report_id`,`parameter_id`),
  KEY `fk_report_parameter_001_idx` (`report_id`),
  KEY `fk_report_parameter_002_idx` (`parameter_id`),
  CONSTRAINT `fk_report_parameter_001` FOREIGN KEY (`report_id`) REFERENCES `stretchy_report` (`id`) ON DELETE CASCADE ON UPDATE NO ACTION,
  CONSTRAINT `fk_report_parameter_002` FOREIGN KEY (`parameter_id`) REFERENCES `stretchy_parameter` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `twofactor_access_token` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `token` varchar(32) NOT NULL,
  `appuser_id` bigint(20) NOT NULL,
  `valid_from` datetime(6) DEFAULT NULL,
  `valid_to` datetime(6) DEFAULT NULL,
  `enabled` tinyint(1) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `token_appuser_UNIQUE` (`token`,`appuser_id`),
  KEY `token` (`token`),
  KEY `user` (`appuser_id`),
  CONSTRAINT `fk_2fa_access_token_user_id` FOREIGN KEY (`appuser_id`) REFERENCES `m_appuser` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `twofactor_configuration` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(40) NOT NULL,
  `value` varchar(1024) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `x_registered_table` (
  `registered_table_name` varchar(50) NOT NULL,
  `application_table_name` varchar(50) NOT NULL,
  `entity_subtype` varchar(50) DEFAULT NULL,
  `category` int(11) NOT NULL DEFAULT 100,
  PRIMARY KEY (`registered_table_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `x_table_column_code_mappings` (
  `column_alias_name` varchar(50) NOT NULL,
  `code_id` int(11) NOT NULL,
  PRIMARY KEY (`column_alias_name`),
  KEY `FK_x_code_id` (`code_id`),
  CONSTRAINT `FK_x_code_id` FOREIGN KEY (`code_id`) REFERENCES `m_code` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
