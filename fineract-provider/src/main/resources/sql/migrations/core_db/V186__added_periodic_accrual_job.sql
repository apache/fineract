INSERT INTO `job` (`name`, `display_name`, `cron_expression`, `create_time`, `task_priority`, `scheduler_group`) VALUES ('Add Periodic Accrual Transactions', 'Add Periodic Accrual Transactions', '0 2 0 1/1 * ? *', now(), 4, 3);

UPDATE `job` SET `scheduler_group`=3 WHERE  `name`='Add Accrual Transactions';