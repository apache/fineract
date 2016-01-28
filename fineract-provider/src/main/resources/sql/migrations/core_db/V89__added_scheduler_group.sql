ALTER TABLE `job`
	ADD COLUMN `scheduler_group` SMALLINT(2) NOT NULL DEFAULT '0' AFTER `updates_allowed`;