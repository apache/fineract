ALTER TABLE `m_loan_charge`
	ADD COLUMN `waived` TINYINT(1) NOT NULL DEFAULT '0' AFTER `is_paid_derived`;