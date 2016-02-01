INSERT INTO `job` (`name`, `display_name`, `cron_expression`, `create_time`, `task_priority`, `scheduler_group`) VALUES ('Recalculate Interest For Loans', 'Recalculate Interest For Loans', '0 1 0 1/1 * ? *', now(), 4, 3);

UPDATE `job` SET `scheduler_group`=3 WHERE  `name`='Update Non Performing Assets';

UPDATE `job` SET `task_priority`=3 WHERE  `name`='Add Accrual Transactions';

UPDATE `job` SET `task_priority`=2 WHERE  `name`='Add Periodic Accrual Transactions';