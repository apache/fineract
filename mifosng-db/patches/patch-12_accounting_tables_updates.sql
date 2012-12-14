DROP TABLE IF EXISTS `acc_gl_journal_entry`;

CREATE TABLE `acc_gl_journal_entry` (
	`id` BIGINT(20) NOT NULL AUTO_INCREMENT,
	`account_id` BIGINT(20) NOT NULL,
	`office_id` BIGINT(20) NOT NULL,
	`reversal_id` BIGINT(20) NULL DEFAULT NULL,
	`transaction_id` VARCHAR(50) NOT NULL,
	`reversed` TINYINT(1) NOT NULL DEFAULT '0',
	`portfolio_generated` TINYINT(1) NOT NULL DEFAULT '0',
	`entry_date` DATE NOT NULL,
	`type` VARCHAR(50) NOT NULL,
	`amount` DECIMAL(19,6) NOT NULL,
	`description` VARCHAR(500) NULL DEFAULT NULL,
	`entity_type` VARCHAR(50) NULL DEFAULT NULL,
	`entity_id` BIGINT(20) NULL DEFAULT NULL,
	`createdby_id` BIGINT(20) NOT NULL,
	`lastmodifiedby_id` BIGINT(20) NOT NULL,
	`created_date` DATETIME NOT NULL,
	`lastmodified_date` DATETIME NOT NULL,
	PRIMARY KEY (`id`),
	INDEX `FK_acc_gl_journal_entry_m_office` (`office_id`),
	INDEX `FK_acc_gl_journal_entry_m_appuser` (`createdby_id`),
	INDEX `FK_acc_gl_journal_entry_m_appuser_2` (`lastmodifiedby_id`),
	INDEX `FK_acc_gl_journal_entry_acc_gl_journal_entry` (`reversal_id`),
	INDEX `FK_acc_gl_journal_entry_acc_gl_account` (`account_id`),
	CONSTRAINT `FK_acc_gl_journal_entry_acc_gl_account` FOREIGN KEY (`account_id`) REFERENCES `acc_gl_account` (`id`),
	CONSTRAINT `FK_acc_gl_journal_entry_acc_gl_journal_entry` FOREIGN KEY (`reversal_id`) REFERENCES `acc_gl_journal_entry` (`id`),
	CONSTRAINT `FK_acc_gl_journal_entry_m_appuser` FOREIGN KEY (`createdby_id`) REFERENCES `m_appuser` (`id`),
	CONSTRAINT `FK_acc_gl_journal_entry_m_appuser_2` FOREIGN KEY (`lastmodifiedby_id`) REFERENCES `m_appuser` (`id`),
	CONSTRAINT `FK_acc_gl_journal_entry_m_office` FOREIGN KEY (`office_id`) REFERENCES `m_office` (`id`)
)
COLLATE='utf8_general_ci'
ENGINE=InnoDB DEFAULT CHARSET=utf8;

ALTER TABLE `acc_gl_account`
	ALTER `description` DROP DEFAULT;
ALTER TABLE `acc_gl_account`
	CHANGE COLUMN `description` `description` VARCHAR(500) NULL AFTER `classification`;

