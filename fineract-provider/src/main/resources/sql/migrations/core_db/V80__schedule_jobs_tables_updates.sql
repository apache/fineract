RENAME TABLE `scheduled_jobs` TO `job`;
RENAME TABLE `scheduled_job_runhistory` TO `job_run_history`;

ALTER TABLE `job`
	ALTER `job_name` DROP DEFAULT,
	ALTER `job_display_name` DROP DEFAULT;
ALTER TABLE `job`
	CHANGE COLUMN `job_name` `name` VARCHAR(50) NOT NULL,
	CHANGE COLUMN `job_display_name` `display_name` VARCHAR(50) NOT NULL;

ALTER TABLE `job`
	CHANGE COLUMN `job_initializing_errorlog` `initializing_errorlog` TEXT NULL;


ALTER TABLE `job_run_history`
	ALTER `triggertype` DROP DEFAULT;
ALTER TABLE `job_run_history`
	CHANGE COLUMN `errormessage` `error_message` VARCHAR(500) NULL DEFAULT NULL,
	CHANGE COLUMN `triggertype` `trigger_type` VARCHAR(25) NOT NULL,
	CHANGE COLUMN `errorlog` `error_log` TEXT NULL;
