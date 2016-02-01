ALTER TABLE `m_loan_charge`
	ADD COLUMN `charge_amount_or_percentage` DECIMAL(19,6) NULL DEFAULT NULL AFTER `calculation_on_amount`;