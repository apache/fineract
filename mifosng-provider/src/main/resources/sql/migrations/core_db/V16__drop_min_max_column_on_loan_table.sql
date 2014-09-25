ALTER TABLE `m_loan`
	DROP COLUMN `min_principal_amount`,
	DROP COLUMN `max_principal_amount`,
	DROP COLUMN `min_nominal_interest_rate_per_period`,
	DROP COLUMN `max_nominal_interest_rate_per_period`,
	DROP COLUMN `min_number_of_repayments`,
	DROP COLUMN `max_number_of_repayments`;