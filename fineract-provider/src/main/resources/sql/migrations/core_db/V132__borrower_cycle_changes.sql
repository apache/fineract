CREATE TABLE `m_product_loan_variations_borrower_cycle` (
	`id` BIGINT(20) NOT NULL AUTO_INCREMENT,
	`loan_product_id` BIGINT(20) NOT NULL DEFAULT '0',
	`borrower_cycle_number` INT(3) NOT NULL DEFAULT '0',
	`value_condition` INT(1) NOT NULL DEFAULT '0',
	`param_type` INT(1) NOT NULL DEFAULT '0',
	`default_value` DECIMAL(19,6) NOT NULL DEFAULT '0.000000',
	`max_value` DECIMAL(19,6) NULL DEFAULT NULL,
	`min_value` DECIMAL(19,6) NULL DEFAULT NULL,
	PRIMARY KEY (`id`),
	INDEX `borrower_cycle_loan_product_FK` (`loan_product_id`),
	CONSTRAINT `borrower_cycle_loan_product_FK` FOREIGN KEY (`loan_product_id`) REFERENCES `m_product_loan` (`id`)
)
COLLATE='utf8_general_ci'
ENGINE=InnoDB;


ALTER TABLE `m_product_loan`
	ADD COLUMN `use_borrower_cycle` TINYINT(1) NOT NULL DEFAULT '0' AFTER `include_in_borrower_cycle`;