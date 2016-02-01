ALTER TABLE `m_product_loan`
	ADD COLUMN `multi_disburse_loan` TINYINT(1) NOT NULL DEFAULT '0' AFTER `close_date`,
	ADD COLUMN `max_tranche_count` INT(2) NULL DEFAULT NULL AFTER `multi_disburse_loan`,
	ADD COLUMN `outstanding_loan_balance` DECIMAL(19,6) NULL DEFAULT NULL AFTER `max_tranche_count`;

CREATE TABLE `m_loan_disbursement_detail` (
	`id` BIGINT(20) NOT NULL AUTO_INCREMENT,
	`loan_id` BIGINT(20) NOT NULL,
	`expected_disburse_date` DATETIME NOT NULL,
	`disbursedon_date` DATETIME NULL,
	`principal` DECIMAL(19,6) NOT NULL,
	`approved_principal` DECIMAL(19,6) NOT NULL,
	PRIMARY KEY (`id`),
	CONSTRAINT `FK_loan_disbursement_detail_loan_id` FOREIGN KEY (`loan_id`) REFERENCES `m_loan` (`id`)
)
COLLATE='utf8_general_ci'
ENGINE=InnoDB;

ALTER TABLE `m_loan`
	ADD COLUMN `fixed_emi_amount` DECIMAL(19,6) NULL AFTER `loan_product_counter`,
	ADD COLUMN `approved_principal` DECIMAL(19,6) NOT NULL AFTER `principal_amount`,
	ADD COLUMN `max_outstanding_loan_balance` DECIMAL(19,6) NULL DEFAULT NULL AFTER `fixed_emi_amount`;

UPDATE m_loan ml  SET ml.approved_principal = ml.principal_amount;

INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ('portfolio', 'UPDATE_DISBURSEMENTDETAIL', 'DISBURSEMENTDETAIL', 'UPDATE', 0);

CREATE TABLE `m_loan_term_variations` (
	`id` BIGINT(20) NOT NULL AUTO_INCREMENT,
	`loan_id` BIGINT(20) NOT NULL,
	`term_type` SMALLINT(2) NOT NULL,
	`applicable_from` DATE NOT NULL,
	`term_value` DECIMAL(19,6) NOT NULL,
	PRIMARY KEY (`id`),
	CONSTRAINT `FK_loan_id_m_loan_id` FOREIGN KEY (`loan_id`) REFERENCES `m_loan` (`id`)
)
COLLATE='utf8_general_ci'
ENGINE=InnoDB;
