ALTER TABLE `job_run_history`
	CHANGE COLUMN `error_message` `error_message` TEXT NULL DEFAULT NULL AFTER `status`;

ALTER TABLE `m_deposit_account_term_and_preclosure`
	ADD COLUMN `transfer_interest_to_linked_account` TINYINT(1) NOT NULL DEFAULT '0' AFTER `expected_firstdepositon_date`;

UPDATE `job` SET `scheduler_group`=1 WHERE  `name`='Post Interest For Savings';

INSERT INTO `job` (`name`, `display_name`, `cron_expression`, `create_time`, `task_priority`, `group_name`, `previous_run_start_time`, `next_run_time`, `job_key`, `initializing_errorlog`, `is_active`, `currently_running`, `updates_allowed`, `scheduler_group`, `is_misfired`) VALUES ('Transfer Interest To Savings', 'Transfer Interest To Savings', '0 2 0 1/1 * ? *', now(), 4, NULL, NULL, NULL, NULL, NULL, 1, 0, 1, 1, 0);