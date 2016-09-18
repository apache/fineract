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

CREATE TABLE `m_loan_interest_recalculation_additional_details` (
	`id` BIGINT NOT NULL AUTO_INCREMENT,
	`loan_repayment_schedule_id` BIGINT NOT NULL,
	`effective_date` DATE NOT NULL,
	`amount` DECIMAL(19,6) NOT NULL,
	PRIMARY KEY (`id`),
	CONSTRAINT `FK_additional_details_repayment_schedule_id` FOREIGN KEY (`loan_repayment_schedule_id`) REFERENCES `m_loan_repayment_schedule` (`id`)
);


ALTER TABLE `m_loan` DROP `repayment_frequency_nth_day_enum`, DROP `repayment_frequency_day_of_week_enum`;


ALTER TABLE `m_product_loan_recalculation_details` ADD `rest_frequency_nth_day_enum` INT(5), ADD `rest_frequency_on_day` INT(5), ADD `rest_frequency_weekday_enum` INT(5),
ADD `compounding_frequency_nth_day_enum` INT(5), ADD `compounding_frequency_on_day` INT(5), ADD `compounding_frequency_weekday_enum` INT(5), ADD `is_compounding_to_be_posted_as_transaction` TINYINT(1) NOT NULL DEFAULT '0', ADD `allow_compounding_on_eod` TINYINT(1) NOT NULL DEFAULT '0';


ALTER TABLE `m_loan_recalculation_details` ADD `rest_frequency_nth_day_enum` INT(5), ADD `rest_frequency_on_day` INT(5), ADD `rest_frequency_weekday_enum` INT(5),
ADD `compounding_frequency_nth_day_enum` INT(5), ADD `compounding_frequency_on_day` INT(5), 
ADD `is_compounding_to_be_posted_as_transaction` TINYINT(1) NOT NULL DEFAULT '0',
ADD `compounding_frequency_weekday_enum` INT(5), ADD `allow_compounding_on_eod` TINYINT(1) NOT NULL DEFAULT '0';


UPDATE m_product_loan_recalculation_details plr SET plr.compounding_frequency_weekday_enum = (WEEKDAY(plr.compounding_freqency_date) + 1) WHERE plr.compounding_frequency_type_enum = 3 AND plr.compounding_freqency_date IS NOT NULL;


UPDATE m_product_loan_recalculation_details plr SET plr.compounding_frequency_on_day = DAYOFMONTH(plr.compounding_freqency_date) WHERE plr.compounding_frequency_type_enum = 4 AND plr.compounding_freqency_date IS NOT NULL;


UPDATE m_loan_recalculation_details lrd SET lrd.compounding_frequency_weekday_enum = (WEEKDAY(lrd.compounding_freqency_date) + 1) WHERE lrd.compounding_frequency_type_enum = 3 AND lrd.compounding_freqency_date IS NOT NULL;


UPDATE m_loan_recalculation_details lrd SET lrd.compounding_frequency_on_day = DAYOFMONTH(lrd.compounding_freqency_date) WHERE lrd.compounding_frequency_type_enum = 4 AND lrd.compounding_freqency_date IS NOT NULL;


UPDATE m_product_loan_recalculation_details plr SET plr.rest_frequency_weekday_enum = (WEEKDAY(plr.rest_freqency_date) + 1) WHERE plr.rest_frequency_type_enum = 3 AND plr.rest_freqency_date IS NOT NULL;


UPDATE m_product_loan_recalculation_details plr SET plr.rest_frequency_on_day = DAYOFMONTH(plr.rest_freqency_date) WHERE plr.rest_frequency_type_enum = 4 AND plr.rest_freqency_date IS NOT NULL;


UPDATE m_loan_recalculation_details lrd SET lrd.rest_frequency_weekday_enum = (WEEKDAY(lrd.rest_freqency_date) + 1) WHERE lrd.rest_frequency_type_enum = 3 AND lrd.rest_freqency_date IS NOT NULL;


UPDATE m_loan_recalculation_details lrd SET lrd.rest_frequency_on_day = DAYOFMONTH(lrd.rest_freqency_date) WHERE lrd.rest_frequency_type_enum = 4 AND lrd.rest_freqency_date IS NOT NULL;


ALTER TABLE `m_product_loan_recalculation_details` DROP `rest_freqency_date`, DROP `compounding_freqency_date`;


ALTER TABLE `m_loan_recalculation_details` DROP `rest_freqency_date`, DROP `compounding_freqency_date`;


ALTER TABLE `job`
	CHANGE COLUMN `name` `name` VARCHAR(100) NOT NULL AFTER `id`,
	CHANGE COLUMN `display_name` `display_name` VARCHAR(100) NOT NULL AFTER `name`;

INSERT INTO `job` (`name`, `display_name`, `cron_expression`, `create_time`, `task_priority`, `group_name`, `previous_run_start_time`, `next_run_time`, `job_key`, `initializing_errorlog`, `is_active`, `currently_running`, `updates_allowed`, `scheduler_group`, `is_misfired`) VALUES ('Add Accrual Transactions For Loans With Income Posted As Transactions', 'Add Accrual Transactions For Loans With Income Posted As Transactions', '0 1 0 1/1 * ? *', now(), 5, NULL, NULL, NULL, NULL, NULL, 1, 0, 1, 3, 0);

UPDATE `job` SET `task_priority`=6 WHERE  `name`='Update Non Performing Assets';