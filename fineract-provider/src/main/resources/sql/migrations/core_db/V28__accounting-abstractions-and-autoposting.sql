CREATE TABLE `acc_accounting_rule` (
	`id` BIGINT(20) NOT NULL,
	`name` VARCHAR(100) NULL DEFAULT NULL,
	`office_id` BIGINT(20) NULL DEFAULT NULL,
	`debit_account_id` BIGINT(20) NOT NULL,
	`credit_account_id` BIGINT(20) NOT NULL,
	`description` VARCHAR(500) NULL DEFAULT NULL,
	`system_defined` TINYINT(1) NOT NULL DEFAULT '0',
	PRIMARY KEY (`id`),
	UNIQUE INDEX `accounting_rule_name_unique` (`name`),
	INDEX `FK_acc_accounting_rule_acc_gl_account_debit` (`debit_account_id`),
	INDEX `FK_acc_accounting_rule_acc_gl_account_credit` (`credit_account_id`),
	INDEX `FK_acc_accounting_rule_m_office` (`office_id`),
	CONSTRAINT `FK_acc_accounting_rule_acc_gl_account_credit` FOREIGN KEY (`credit_account_id`) REFERENCES `acc_gl_account` (`id`),
	CONSTRAINT `FK_acc_accounting_rule_acc_gl_account_debit` FOREIGN KEY (`debit_account_id`) REFERENCES `acc_gl_account` (`id`),
	CONSTRAINT `FK_acc_accounting_rule_m_office` FOREIGN KEY (`office_id`) REFERENCES `m_office` (`id`)
)
COLLATE='utf8_general_ci'
ENGINE=InnoDB;

CREATE TABLE `acc_auto_posting` (
	`id` BIGINT(20) NOT NULL AUTO_INCREMENT,
	`name` VARCHAR(100) NOT NULL,
	`description` VARCHAR(500) NULL DEFAULT NULL,
	`office_id` BIGINT(20) NULL DEFAULT NULL,
	`product_type_enum` SMALLINT(5) NOT NULL,
	`product_id` BIGINT(20) NULL DEFAULT NULL,
	`charge_id` BIGINT(20) NULL DEFAULT NULL,
	`event` INT(11) NOT NULL,
	`event_attribute` INT(11) NULL DEFAULT NULL,
	`accounting_rule_id` BIGINT(20) NOT NULL,
	PRIMARY KEY (`id`),
	UNIQUE INDEX `auto_posting_name_unique` (`name`),
	INDEX `FK_acc_auto_posting_m_office` (`office_id`),
	INDEX `FK_acc_auto_posting_acc_accounting_rule` (`accounting_rule_id`),
	INDEX `FK_acc_auto_posting_m_code` (`event`),
	INDEX `FK_acc_auto_posting_m_charge` (`charge_id`),
	INDEX `FK_acc_auto_posting_m_code_value` (`event_attribute`),
	CONSTRAINT `FK_acc_auto_posting_acc_accounting_rule` FOREIGN KEY (`accounting_rule_id`) REFERENCES `acc_accounting_rule` (`id`),
	CONSTRAINT `FK_acc_auto_posting_m_charge` FOREIGN KEY (`charge_id`) REFERENCES `m_charge` (`id`),
	CONSTRAINT `FK_acc_auto_posting_m_code` FOREIGN KEY (`event`) REFERENCES `m_code` (`id`),
	CONSTRAINT `FK_acc_auto_posting_m_code_value` FOREIGN KEY (`event_attribute`) REFERENCES `m_code_value` (`id`),
	CONSTRAINT `FK_acc_auto_posting_m_office` FOREIGN KEY (`office_id`) REFERENCES `m_office` (`id`)
)
COLLATE='utf8_general_ci'
ENGINE=InnoDB;
