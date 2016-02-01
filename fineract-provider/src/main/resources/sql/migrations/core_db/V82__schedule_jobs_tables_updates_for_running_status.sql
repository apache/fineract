ALTER TABLE `job`
	ADD COLUMN `currently_running` TINYINT(1) NOT NULL DEFAULT '0' AFTER `is_active`;

ALTER TABLE `job`
	CHANGE COLUMN `trigger_key` `job_key` VARCHAR(500) NULL DEFAULT NULL;