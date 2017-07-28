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

CREATE TABLE `m_portfolio_account_associations` (
	`id` BIGINT NOT NULL AUTO_INCREMENT,
	`loan_account_id` BIGINT NULL DEFAULT NULL,
	`savings_account_id` BIGINT NULL DEFAULT NULL,
	`linked_loan_account_id` BIGINT NULL DEFAULT NULL,
	`linked_savings_account_id` BIGINT NULL DEFAULT NULL,
	PRIMARY KEY (`id`),
	CONSTRAINT `account_association_loan_fk` FOREIGN KEY (`loan_account_id`) REFERENCES `m_loan` (`id`),
	CONSTRAINT `account_association_savings_fk` FOREIGN KEY (`savings_account_id`) REFERENCES `m_savings_account` (`id`),
	CONSTRAINT `linked_loan_fk` FOREIGN KEY (`linked_loan_account_id`) REFERENCES `m_loan` (`id`),
	CONSTRAINT `linked_savings_fk` FOREIGN KEY (`linked_savings_account_id`) REFERENCES `m_savings_account` (`id`)
)
COLLATE='utf8_general_ci'
ENGINE=InnoDB;

ALTER TABLE `m_charge`
	ADD COLUMN `charge_payment_mode_enum` SMALLINT(5) NOT NULL DEFAULT '0' AFTER `charge_calculation_enum`;

ALTER TABLE `m_loan_charge`
	ADD COLUMN `charge_payment_mode_enum` SMALLINT(5) NOT NULL DEFAULT '0' AFTER `charge_calculation_enum`;

INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ('transaction_loan', 'PAY_LOANCHARGE', 'LOANCHARGE', 'PAY', 0);

INSERT INTO `job` (`name`, `display_name`, `cron_expression`, `create_time`, `task_priority`, `group_name`, `previous_run_start_time`, `next_run_time`, `job_key`, `initializing_errorlog`, `is_active`, `currently_running`, `updates_allowed`, `scheduler_group`, `is_misfired`) VALUES ('Transfer Fee For Loans From Savings', 'Transfer Fee For Loans From Savings', '0 1 0 1/1 * ? *', now(), 5, NULL, NULL, NULL, NULL, NULL, 1, 0, 1, 0, 0);


