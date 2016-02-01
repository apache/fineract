ALTER TABLE `m_product_loan_recalculation_details`
	ADD COLUMN `rest_frequency_type_enum` SMALLINT(1) NOT NULL AFTER `reschedule_strategy_enum`,
	ADD COLUMN `rest_frequency_interval` SMALLINT(3) NOT NULL DEFAULT '0' AFTER `rest_frequency_type_enum`,
	ADD COLUMN `rest_freqency_date` DATE NULL DEFAULT NULL AFTER `rest_frequency_interval`;

ALTER TABLE `m_loan_recalculation_details`
	ADD COLUMN `rest_frequency_type_enum` SMALLINT(1) NOT NULL AFTER `reschedule_strategy_enum`,
	ADD COLUMN `rest_frequency_interval` SMALLINT(3) NOT NULL DEFAULT '0' AFTER `rest_frequency_type_enum`,
	ADD COLUMN `rest_freqency_date` DATE NULL DEFAULT NULL AFTER `rest_frequency_interval`;