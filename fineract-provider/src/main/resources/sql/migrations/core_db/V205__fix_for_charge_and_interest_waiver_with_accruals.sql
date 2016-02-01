ALTER TABLE `m_loan_charge_paid_by`
	ADD COLUMN `installment_number` SMALLINT(5) NULL AFTER `amount`;
	
ALTER TABLE `m_loan_transaction`
	ADD COLUMN `unrecognized_income_portion` DECIMAL(19,6) NULL DEFAULT NULL AFTER `overpayment_portion_derived`;