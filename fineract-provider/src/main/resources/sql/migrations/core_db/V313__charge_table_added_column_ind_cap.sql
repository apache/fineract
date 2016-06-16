ALTER TABLE `m_charge`
	ADD COLUMN `ind_cap_for_group_loans` tinyint(1) NOT NULL DEFAULT '0';
	
ALTER TABLE `m_loan_charge`
	ADD COLUMN `ind_cap_for_group_loans` tinyint(1) NOT NULL DEFAULT '0';
