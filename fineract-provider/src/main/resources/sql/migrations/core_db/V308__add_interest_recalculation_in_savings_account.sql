ALTER TABLE `m_savings_account`
	ADD COLUMN `last_interest_calculation_date` DATE NULL DEFAULT NULL AFTER `tax_group_id`;



