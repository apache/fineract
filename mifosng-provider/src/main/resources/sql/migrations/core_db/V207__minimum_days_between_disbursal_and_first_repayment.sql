ALTER TABLE `m_product_loan`
	ADD COLUMN `min_days_between_disbursal_and_first_repayment` INT(3) NULL AFTER `interest_recalculation_enabled`;