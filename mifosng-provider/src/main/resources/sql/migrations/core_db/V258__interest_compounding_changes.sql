ALTER TABLE `m_product_loan_recalculation_details`
	ADD COLUMN `compounding_frequency_type_enum` SMALLINT(1) NULL DEFAULT NULL,
	ADD COLUMN `compounding_frequency_interval` SMALLINT(3) NULL DEFAULT NULL,
	ADD COLUMN `compounding_freqency_date` DATE NULL DEFAULT NULL;
	
	
ALTER TABLE `m_loan_recalculation_details`
	ADD COLUMN `compounding_frequency_type_enum` SMALLINT(1) NULL DEFAULT NULL,
	ADD COLUMN `compounding_frequency_interval` SMALLINT(3) NULL DEFAULT NULL,
	ADD COLUMN `compounding_freqency_date` DATE NULL DEFAULT NULL;
