ALTER TABLE `m_loan_transaction`
	ADD COLUMN 	(
		`created_date` DATETIME,
		`appuser_id` BIGINT(20)
	);

ALTER TABLE `m_savings_account_transaction`
	ADD COLUMN `appuser_id` BIGINT(20);