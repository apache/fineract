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

ALTER TABLE `job_run_history`
	CHANGE COLUMN `error_message` `error_message` TEXT NULL DEFAULT NULL AFTER `status`;

ALTER TABLE `m_deposit_account_term_and_preclosure`
	ADD COLUMN `transfer_interest_to_linked_account` TINYINT(1) NOT NULL DEFAULT '0' AFTER `expected_firstdepositon_date`;

UPDATE `job` SET `scheduler_group`=1 WHERE  `name`='Post Interest For Savings';

INSERT INTO `job` (`name`, `display_name`, `cron_expression`, `create_time`, `task_priority`, `group_name`, `previous_run_start_time`, `next_run_time`, `job_key`, `initializing_errorlog`, `is_active`, `currently_running`, `updates_allowed`, `scheduler_group`, `is_misfired`) VALUES ('Transfer Interest To Savings', 'Transfer Interest To Savings', '0 2 0 1/1 * ? *', now(), 4, NULL, NULL, NULL, NULL, NULL, 1, 0, 1, 1, 0);