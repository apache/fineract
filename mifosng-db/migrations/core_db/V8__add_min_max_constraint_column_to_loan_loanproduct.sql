ALTER TABLE `m_product_loan`
	ADD COLUMN `min_nominal_interest_rate_per_period` DECIMAL(19,6) NOT NULL AFTER `nominal_interest_rate_per_period`,
	ADD COLUMN `max_nominal_interest_rate_per_period` DECIMAL(19,6) NOT NULL AFTER `min_nominal_interest_rate_per_period`,
	ADD COLUMN `min_number_of_repayments` SMALLINT(5) NOT NULL AFTER `number_of_repayments`,
	ADD COLUMN `max_number_of_repayments` SMALLINT(5) NOT NULL AFTER `min_number_of_repayments`;
	
ALTER TABLE `m_loan`
	ADD COLUMN `min_nominal_interest_rate_per_period` DECIMAL(19,6) NOT NULL AFTER `nominal_interest_rate_per_period`,
	ADD COLUMN `max_nominal_interest_rate_per_period` DECIMAL(19,6) NOT NULL AFTER `min_nominal_interest_rate_per_period`,
	ADD COLUMN `min_number_of_repayments` SMALLINT(5) NOT NULL AFTER `number_of_repayments`,
	ADD COLUMN `max_number_of_repayments` SMALLINT(5) NOT NULL AFTER `min_number_of_repayments`;	
	
ALTER TABLE `m_loan`
	ALTER `min_principal_amount` DROP DEFAULT,
	ALTER `max_principal_amount` DROP DEFAULT;
ALTER TABLE `m_loan`
	CHANGE COLUMN `min_principal_amount` `min_principal_amount` DECIMAL(19,6) NULL AFTER `principal_amount`,
	CHANGE COLUMN `max_principal_amount` `max_principal_amount` DECIMAL(19,6) NULL AFTER `min_principal_amount`,
	CHANGE COLUMN `min_nominal_interest_rate_per_period` `min_nominal_interest_rate_per_period` DECIMAL(19,6) NULL AFTER `nominal_interest_rate_per_period`,
	CHANGE COLUMN `max_nominal_interest_rate_per_period` `max_nominal_interest_rate_per_period` DECIMAL(19,6) NULL AFTER `min_nominal_interest_rate_per_period`,
	CHANGE COLUMN `min_number_of_repayments` `min_number_of_repayments` SMALLINT(5) NULL AFTER `number_of_repayments`,
	CHANGE COLUMN `max_number_of_repayments` `max_number_of_repayments` SMALLINT(5) NULL AFTER `min_number_of_repayments`;	
	
ALTER TABLE `m_product_loan`
	ALTER `min_principal_amount` DROP DEFAULT,
	ALTER `max_principal_amount` DROP DEFAULT,
	ALTER `min_nominal_interest_rate_per_period` DROP DEFAULT,
	ALTER `max_nominal_interest_rate_per_period` DROP DEFAULT,
	ALTER `min_number_of_repayments` DROP DEFAULT,
	ALTER `max_number_of_repayments` DROP DEFAULT;
ALTER TABLE `m_product_loan`
	CHANGE COLUMN `min_principal_amount` `min_principal_amount` DECIMAL(19,6) NULL AFTER `principal_amount`,
	CHANGE COLUMN `max_principal_amount` `max_principal_amount` DECIMAL(19,6) NULL AFTER `min_principal_amount`,
	CHANGE COLUMN `min_nominal_interest_rate_per_period` `min_nominal_interest_rate_per_period` DECIMAL(19,6) NULL AFTER `nominal_interest_rate_per_period`,
	CHANGE COLUMN `max_nominal_interest_rate_per_period` `max_nominal_interest_rate_per_period` DECIMAL(19,6) NULL AFTER `min_nominal_interest_rate_per_period`,
	CHANGE COLUMN `min_number_of_repayments` `min_number_of_repayments` SMALLINT(5) NULL AFTER `number_of_repayments`,
	CHANGE COLUMN `max_number_of_repayments` `max_number_of_repayments` SMALLINT(5) NULL AFTER `min_number_of_repayments`;