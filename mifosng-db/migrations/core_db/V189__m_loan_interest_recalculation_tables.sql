ALTER TABLE `m_product_loan`
	ADD COLUMN `days_in_month_enum` SMALLINT(5) NOT NULL DEFAULT '1' AFTER `overdue_days_for_npa`,
	ADD COLUMN `days_in_year_enum` SMALLINT(5) NOT NULL DEFAULT '1' AFTER `days_in_month_enum`,
	ADD COLUMN `interest_recalculation_enabled` TINYINT NOT NULL DEFAULT '0' AFTER `days_in_year_enum`;

ALTER TABLE `m_loan`
	ADD COLUMN `days_in_month_enum` SMALLINT(5) NOT NULL DEFAULT '1' AFTER `accrued_till`,
	ADD COLUMN `days_in_year_enum` SMALLINT(5) NOT NULL DEFAULT '1' AFTER `days_in_month_enum`,
	ADD COLUMN `interest_recalculation_enabled` TINYINT NOT NULL DEFAULT '0' AFTER `days_in_year_enum`;

CREATE TABLE `m_product_loan_recalculation_details` (
	`id` BIGINT(20) NOT NULL AUTO_INCREMENT,
	`product_id` BIGINT(20) NOT NULL,
	`compound_type_enum` SMALLINT(5) NOT NULL,
	`reschedule_strategy_enum` SMALLINT(5) NOT NULL,
	PRIMARY KEY (`id`),
	CONSTRAINT `FK_m_product_loan_m_product_loan_recalculation_details` FOREIGN KEY (`product_id`) REFERENCES `m_product_loan` (`id`)
)
COLLATE='latin1_swedish_ci'
ENGINE=InnoDB;

CREATE TABLE `m_loan_recalculation_details` (
	`id` BIGINT(20) NOT NULL AUTO_INCREMENT,
	`loan_id` BIGINT(20) NOT NULL,
	`compound_type_enum` SMALLINT(5) NOT NULL,
	`reschedule_strategy_enum` SMALLINT(5) NOT NULL,
	PRIMARY KEY (`id`),
	CONSTRAINT `FK_m_loan_m_loan_recalculation_details` FOREIGN KEY (`loan_id`) REFERENCES `m_loan` (`id`)
)
COLLATE='latin1_swedish_ci'
ENGINE=InnoDB;
