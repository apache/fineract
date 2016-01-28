ALTER TABLE `m_loan_charge`
	ADD COLUMN `is_active` TINYINT(1) NOT NULL DEFAULT '1' AFTER `max_cap`;