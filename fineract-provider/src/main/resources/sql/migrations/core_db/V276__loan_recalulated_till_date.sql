ALTER TABLE `m_loan`
	ADD COLUMN `interest_recalcualated_on` DATE NULL DEFAULT NULL AFTER `accrued_till`;