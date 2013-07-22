ALTER TABLE `job`
	ADD COLUMN `updates_allowed` TINYINT(1) NOT NULL DEFAULT '1' AFTER `currently_running`;