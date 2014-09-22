CREATE TABLE `m_loan_charge_paid_by` (
	`id` BIGINT(20) NOT NULL AUTO_INCREMENT,
	`loan_transaction_id` BIGINT(20) NOT NULL,
	`loan_charge_id` BIGINT(20) NOT NULL,
	`amount` DECIMAL(19,6) NOT NULL,
	PRIMARY KEY (`id`),
	INDEX `FK__m_loan_transaction` (`loan_transaction_id`),
	INDEX `FK__m_loan_charge` (`loan_charge_id`),
	CONSTRAINT `FK__m_loan_charge` FOREIGN KEY (`loan_charge_id`) REFERENCES `m_loan_charge` (`id`),
	CONSTRAINT `FK__m_loan_transaction` FOREIGN KEY (`loan_transaction_id`) REFERENCES `m_loan_transaction` (`id`)
)
COLLATE='utf8_general_ci'
ENGINE=InnoDB;
