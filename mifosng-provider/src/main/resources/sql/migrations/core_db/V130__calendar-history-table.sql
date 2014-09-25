CREATE TABLE `m_calendar_history` (
	`id` BIGINT(20) NOT NULL AUTO_INCREMENT,
	`calendar_id` BIGINT(20) NOT NULL,
	`title` VARCHAR(50) NOT NULL,
	`description` VARCHAR(100) NULL DEFAULT NULL,
	`location` VARCHAR(50) NULL DEFAULT NULL,
	`start_date` DATE NOT NULL,
	`end_date` DATE NULL DEFAULT NULL,
	`duration` SMALLINT(6) NULL DEFAULT NULL,
	`calendar_type_enum` SMALLINT(5) NOT NULL,
	`repeating` TINYINT(1) NOT NULL DEFAULT '0',
	`recurrence` VARCHAR(100) NULL DEFAULT NULL,
	`remind_by_enum` SMALLINT(5) NULL DEFAULT NULL,
	`first_reminder` SMALLINT(11) NULL DEFAULT NULL,
	`second_reminder` SMALLINT(11) NULL DEFAULT NULL,
	PRIMARY KEY (`id`),
	INDEX `FK_m_calendar_m_calendar_history` (`calendar_id`),
	CONSTRAINT `FK_m_calendar_m_calendar_history` FOREIGN KEY (`calendar_id`) REFERENCES `m_calendar` (`id`)
);