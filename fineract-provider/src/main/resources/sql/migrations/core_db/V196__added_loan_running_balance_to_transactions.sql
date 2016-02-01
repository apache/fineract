ALTER TABLE `m_loan_transaction`
	ADD COLUMN `outstanding_loan_balance_derived` DECIMAL(19,6) NULL DEFAULT NULL AFTER `overpayment_portion_derived`;