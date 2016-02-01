ALTER TABLE `acc_gl_journal_entry`
	ADD COLUMN `is_running_balance_caculated` TINYINT NOT NULL DEFAULT '0' AFTER `lastmodified_date`,
	ADD COLUMN `office_running_balance` DECIMAL(19,6) NOT NULL DEFAULT '0.000000' AFTER `is_running_balance_caculated`;

INSERT INTO `job` (`name`, `display_name`, `cron_expression`, `create_time`, `task_priority`, `group_name`, `previous_run_start_time`, `next_run_time`, `job_key`, `initializing_errorlog`, `is_active`, `currently_running`, `updates_allowed`, `scheduler_group`, `is_misfired`) VALUES ('Update Accounting Running Balances', 'Update Accounting Running Balances', '0 1 0 1/1 * ? *', now(), 5, NULL, NULL, NULL, NULL, NULL, 1, 0, 1, 0, 0);
