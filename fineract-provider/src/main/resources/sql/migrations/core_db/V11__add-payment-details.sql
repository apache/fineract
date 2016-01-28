/*
New table for storing transaction details (for both loan and savings)
*/

CREATE TABLE `m_payment_detail` (
	`id` BIGINT(20) NOT NULL AUTO_INCREMENT,
	`payment_type_enum` SMALLINT(5) NOT NULL,
	`account_number` VARCHAR(100) NULL DEFAULT NULL,
	`check_number` VARCHAR(100) NULL DEFAULT NULL,
	`receipt_number` VARCHAR(100) NULL DEFAULT NULL,
	`bank_number` VARCHAR(100) NULL DEFAULT NULL,
	`routing_code` VARCHAR(100) NULL DEFAULT NULL,
	PRIMARY KEY (`id`)
)
COLLATE='utf8_general_ci'
ENGINE=InnoDB;

/*
Update loan transaction to add a link to transaction detail table
*/

ALTER TABLE `m_loan_transaction`
	ADD COLUMN `payment_detail_id` BIGINT(20) NULL AFTER `loan_id`,
	ADD CONSTRAINT `FK_m_loan_transaction_m_payment_detail` FOREIGN KEY (`payment_detail_id`) REFERENCES `m_payment_detail` (`id`);
