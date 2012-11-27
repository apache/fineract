DROP TABLE IF EXISTS `m_acc_coa`;

DROP TABLE IF EXISTS `acc_gl_account`;
CREATE TABLE `acc_gl_account` (
	`id` BIGINT(20) NOT NULL AUTO_INCREMENT,
	`name` VARCHAR(45) NOT NULL,
	`parent_id` BIGINT(20) NULL DEFAULT NULL,
	`gl_code` VARCHAR(45) NOT NULL,
	`disabled` TINYINT(1) NOT NULL DEFAULT '0',
	`manual_journal_entries_allowed` TINYINT(1) NOT NULL DEFAULT '1',
	`header_account` TINYINT(1) NOT NULL DEFAULT '0',
	`classification` VARCHAR(45) NOT NULL,
	`description` VARCHAR(500) NOT NULL,
	PRIMARY KEY (`id`),
	UNIQUE INDEX `acc_gl_code` (`gl_code`),
	INDEX `FK_ACC_0000000001` (`parent_id`),
	CONSTRAINT `FK_ACC_0000000001` FOREIGN KEY (`parent_id`) REFERENCES `acc_gl_account` (`id`)
)
COLLATE='utf8_general_ci'
ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `acc_gl_closure`;
CREATE TABLE `acc_gl_closure` (
	`id` BIGINT(20) NOT NULL AUTO_INCREMENT,
	`office_id` BIGINT(20) NOT NULL,
	`closing_date` DATE NOT NULL,
	`is_deleted` INT(20) NOT NULL DEFAULT '0',
	`createdby_id` BIGINT(20) NULL DEFAULT NULL,
	`lastmodifiedby_id` BIGINT(20) NULL DEFAULT NULL,
	`created_date` DATETIME NULL DEFAULT NULL,
	`lastmodified_date` DATETIME NULL DEFAULT NULL,
	`comments` VARCHAR(500) NULL DEFAULT NULL,
	PRIMARY KEY (`id`),
	INDEX `FK_acc_gl_closure_m_office` (`office_id`),
	INDEX `FK_acc_gl_closure_m_appuser` (`createdby_id`),
	INDEX `FK_acc_gl_closure_m_appuser_2` (`lastmodifiedby_id`),
	UNIQUE INDEX `office_id_closing_date` (`office_id`, `closing_date`),
	CONSTRAINT `FK_acc_gl_closure_m_appuser` FOREIGN KEY (`createdby_id`) REFERENCES `m_appuser` (`id`),
	CONSTRAINT `FK_acc_gl_closure_m_appuser_2` FOREIGN KEY (`lastmodifiedby_id`) REFERENCES `m_appuser` (`id`),
	CONSTRAINT `FK_acc_gl_closure_m_office` FOREIGN KEY (`office_id`) REFERENCES `m_office` (`id`)
)
COLLATE='utf8_general_ci'
ENGINE=InnoDB DEFAULT CHARSET=utf8;