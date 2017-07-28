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

CREATE TABLE `scheduled_jobs` (
	`id` BIGINT(20) NOT NULL AUTO_INCREMENT,
	`job_name` VARCHAR(50) NOT NULL COLLATE 'latin1_swedish_ci',
	`job_display_name` VARCHAR(50) NOT NULL COLLATE 'latin1_swedish_ci',
	`cron_expression` VARCHAR(20) NOT NULL COLLATE 'latin1_swedish_ci',
	`create_time` DATETIME NOT NULL,
	`task_priority` SMALLINT(6) NOT NULL DEFAULT '5',
	`group_name` VARCHAR(50) NULL DEFAULT NULL COLLATE 'latin1_swedish_ci',
	`previous_run_start_time` DATETIME NULL DEFAULT NULL,
	`next_run_time` DATETIME NULL DEFAULT NULL,
	`trigger_key` VARCHAR(500) NULL DEFAULT NULL COLLATE 'latin1_swedish_ci',
	`job_initializing_errorlog` TEXT NULL COLLATE 'latin1_swedish_ci',
	`is_active` TINYINT(1) NOT NULL DEFAULT '1',
	PRIMARY KEY (`id`)
)
COLLATE='utf8_general_ci'
ENGINE=InnoDB;

CREATE TABLE `scheduled_job_runhistory` (
	`id` BIGINT(20) NOT NULL AUTO_INCREMENT,
	`job_id` BIGINT(20) NOT NULL,
	`version` BIGINT(20) NOT NULL,
	`start_time` DATETIME NOT NULL,
	`end_time` DATETIME NOT NULL,
	`status` VARCHAR(10) NOT NULL COLLATE 'latin1_swedish_ci',
	`errormessage` VARCHAR(500) NULL DEFAULT NULL COLLATE 'latin1_swedish_ci',
	`triggertype` VARCHAR(25) NOT NULL COLLATE 'latin1_swedish_ci',
	`errorlog` TEXT NULL COLLATE 'latin1_swedish_ci',
	PRIMARY KEY (`id`),
	INDEX `scheduledjobsFK` (`job_id`),
	CONSTRAINT `scheduledjobsFK` FOREIGN KEY (`job_id`) REFERENCES `scheduled_jobs` (`id`)
)
COLLATE='utf8_general_ci'
ENGINE=InnoDB;


INSERT INTO `scheduled_jobs` (`job_name`, `job_display_name`, `cron_expression`, `create_time`, `task_priority`, `group_name`, `previous_run_start_time`, `next_run_time`, `trigger_key`, `job_initializing_errorlog`, `is_active`) VALUES ('Update loan Summary', 'Update loan Summary', '0 0 22 1/1 * ? *', now(), 5, NULL, NULL, NULL, NULL, NULL, 1);
INSERT INTO `scheduled_jobs` (`job_name`, `job_display_name`, `cron_expression`, `create_time`, `task_priority`, `group_name`, `previous_run_start_time`, `next_run_time`, `trigger_key`, `job_initializing_errorlog`, `is_active`) VALUES ('Update Loan Arrears Ageing', 'Update Loan Arrears Ageing', '0 1 0 1/1 * ? *', now(), 5, NULL, NULL, NULL, NULL, NULL, 1);
INSERT INTO `scheduled_jobs` (`job_name`, `job_display_name`, `cron_expression`, `create_time`, `task_priority`, `group_name`, `previous_run_start_time`, `next_run_time`, `trigger_key`, `job_initializing_errorlog`, `is_active`) VALUES ('Update Loan Paid In Advance', 'Update Loan Paid In Advance', '0 5 0 1/1 * ? *', now(), 5, NULL, NULL, NULL, NULL, NULL, 1);
INSERT INTO `scheduled_jobs` (`job_name`, `job_display_name`, `cron_expression`, `create_time`, `task_priority`, `group_name`, `previous_run_start_time`, `next_run_time`, `trigger_key`, `job_initializing_errorlog`, `is_active`) VALUES ('Apply Annual Fee For Savings', 'Apply Annual Fee For Savings', '0 20 22 1/1 * ? *', now(), 5, NULL, NULL, NULL, NULL, NULL, 1);
INSERT INTO `scheduled_jobs` (`job_name`, `job_display_name`, `cron_expression`, `create_time`, `task_priority`, `group_name`, `previous_run_start_time`, `next_run_time`, `trigger_key`, `job_initializing_errorlog`, `is_active`) VALUES ('Apply Holidays To Loans', 'Apply Holidays To Loans', '0 0 12 * * ?', now(), 5, NULL, NULL, NULL, NULL, NULL, 1);
