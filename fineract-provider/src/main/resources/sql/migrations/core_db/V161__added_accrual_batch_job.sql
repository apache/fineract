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

ALTER TABLE `m_loan_repayment_schedule`
	ADD COLUMN `accrual_interest_derived` DECIMAL(19,6) NULL DEFAULT NULL AFTER `interest_waived_derived`,
	ADD COLUMN `accrual_fee_charges_derived` DECIMAL(19,6) NULL DEFAULT NULL AFTER `fee_charges_waived_derived`,
	ADD COLUMN `accrual_penalty_charges_derived` DECIMAL(19,6) NULL DEFAULT NULL AFTER `penalty_charges_waived_derived`;

INSERT INTO `job` (`name`, `display_name`, `cron_expression`, `create_time`, `task_priority`, `group_name`, `previous_run_start_time`, `next_run_time`, `job_key`, `initializing_errorlog`, `is_active`, `currently_running`, `updates_allowed`, `scheduler_group`, `is_misfired`) VALUES ('Add Accrual Transactions', 'Add Accrual Transactions', '0 1 0 1/1 * ? *', now(), 5, NULL, NULL, NULL, NULL, NULL, 1, 0, 1, 0, 0);
