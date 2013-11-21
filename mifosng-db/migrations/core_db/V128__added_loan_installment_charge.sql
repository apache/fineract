CREATE TABLE `m_loan_installment_charge` (
	`id` BIGINT(20) NOT NULL AUTO_INCREMENT,
	`loan_charge_id` BIGINT(20) NOT NULL,
	`loan_schedule_id` BIGINT(20) NOT NULL,
	`due_date` DATE NULL DEFAULT NULL,
	`amount` DECIMAL(19,6) NOT NULL,
	`amount_paid_derived` DECIMAL(19,6) NULL DEFAULT NULL,
	`amount_waived_derived` DECIMAL(19,6) NULL DEFAULT NULL,
	`amount_writtenoff_derived` DECIMAL(19,6) NULL DEFAULT NULL,
	`amount_outstanding_derived` DECIMAL(19,6) NOT NULL DEFAULT '0.000000',
	`is_paid_derived` TINYINT(1) NOT NULL DEFAULT '0',
	`waived` TINYINT(1) NOT NULL DEFAULT '0',
	`amount_through_charge_payment` DECIMAL(19,6) NULL DEFAULT NULL,
	PRIMARY KEY (`id`),
	CONSTRAINT `FK_loan_charge_id_charge_schedule` FOREIGN KEY (`loan_charge_id`) REFERENCES `m_loan_charge` (`id`),
	CONSTRAINT `FK_loan_schedule_id_charge_schedule` FOREIGN KEY (`loan_schedule_id`) REFERENCES `m_loan_repayment_schedule` (`id`)
)
COLLATE='utf8_general_ci'
ENGINE=InnoDB;
