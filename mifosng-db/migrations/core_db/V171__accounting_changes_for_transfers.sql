INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ('accounting', 'READ_OFFICEGLACCOUNT', 'OFFICEGLACCOUNT', 'READ', 0);

INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ('accounting', 'CREATE_OFFICEGLACCOUNT', 'OFFICEGLACCOUNT', 'CREATE', 0);

INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ('accounting', 'DELETE_OFFICEGLACCOUNT', 'OFFICEGLACCOUNT', 'DELETE', 0);

INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ('accounting', 'UPDATE_OFFICEGLACCOUNT', 'OFFICEGLACCOUNT', 'UPDATE', 0);

CREATE TABLE `acc_gl_office_mapping` (
	`id` BIGINT(20) NOT NULL AUTO_INCREMENT,
	`gl_account_id` BIGINT(20) NOT NULL DEFAULT '0',
	`office_id` BIGINT(20) NOT NULL,
	`financial_account_type` SMALLINT(5) NOT NULL,
	PRIMARY KEY (`id`),
	INDEX `FK_office_mapping_acc_gl_account` (`gl_account_id`),
	INDEX `FK_office_mapping_office` (`office_id`),
	CONSTRAINT `FK_office_mapping_acc_gl_account` FOREIGN KEY (`gl_account_id`) REFERENCES `acc_gl_account` (`id`),
	CONSTRAINT `FK_office_mapping_office` FOREIGN KEY (`office_id`) REFERENCES `m_office` (`id`)
)
COLLATE='utf8_general_ci'
ENGINE=InnoDB;
