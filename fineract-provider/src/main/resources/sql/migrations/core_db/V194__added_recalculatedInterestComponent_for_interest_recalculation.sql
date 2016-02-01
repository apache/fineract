ALTER TABLE `m_loan_repayment_schedule`
	ADD COLUMN `recalculated_interest_component` TINYINT(1) NOT NULL DEFAULT '0';