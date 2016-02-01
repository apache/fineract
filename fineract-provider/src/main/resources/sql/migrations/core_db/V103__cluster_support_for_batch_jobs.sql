ALTER TABLE `job`
	ADD COLUMN `is_misfired` TINYINT(1) NOT NULL DEFAULT '0' AFTER `scheduler_group`;

CREATE TABLE `scheduler_detail` (
	`id` SMALLINT(2) NOT NULL AUTO_INCREMENT,
	`is_suspended` TINYINT(1) NOT NULL DEFAULT '0',
	`execute_misfired_jobs` TINYINT(1) NOT NULL DEFAULT '1',
	`reset_scheduler_on_bootup` TINYINT(1) NOT NULL DEFAULT '1',
	PRIMARY KEY (`id`)
)
COLLATE='utf8_general_ci'
ENGINE=InnoDB;

INSERT INTO `scheduler_detail` (`is_suspended`, `execute_misfired_jobs`, `reset_scheduler_on_bootup`) VALUES (FALSE,TRUE,TRUE);
