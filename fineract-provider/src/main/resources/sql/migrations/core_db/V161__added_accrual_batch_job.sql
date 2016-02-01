ALTER TABLE `m_loan_repayment_schedule`
	ADD COLUMN `accrual_interest_derived` DECIMAL(19,6) NULL DEFAULT NULL AFTER `interest_waived_derived`,
	ADD COLUMN `accrual_fee_charges_derived` DECIMAL(19,6) NULL DEFAULT NULL AFTER `fee_charges_waived_derived`,
	ADD COLUMN `accrual_penalty_charges_derived` DECIMAL(19,6) NULL DEFAULT NULL AFTER `penalty_charges_waived_derived`;

INSERT INTO `job` (`name`, `display_name`, `cron_expression`, `create_time`, `task_priority`, `group_name`, `previous_run_start_time`, `next_run_time`, `job_key`, `initializing_errorlog`, `is_active`, `currently_running`, `updates_allowed`, `scheduler_group`, `is_misfired`) VALUES ('Add Accrual Transactions', 'Add Accrual Transactions', '0 1 0 1/1 * ? *', now(), 5, NULL, NULL, NULL, NULL, NULL, 1, 0, 1, 0, 0);
