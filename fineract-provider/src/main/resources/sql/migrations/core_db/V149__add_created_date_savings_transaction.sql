ALTER TABLE `m_savings_account_transaction`
	ADD COLUMN `created_date` DATETIME NOT NULL AFTER `cumulative_balance_derived`;

update m_savings_account_transaction sat set sat.created_date=sat.transaction_date;