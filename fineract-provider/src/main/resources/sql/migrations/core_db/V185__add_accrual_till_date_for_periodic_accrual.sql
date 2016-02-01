ALTER TABLE `m_loan`
	ADD COLUMN `accrued_till` DATE NULL DEFAULT NULL AFTER `total_recovered_derived`;