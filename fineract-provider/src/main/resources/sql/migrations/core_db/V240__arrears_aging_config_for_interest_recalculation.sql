ALTER TABLE `m_product_loan_recalculation_details`
	ADD COLUMN `arrears_based_on_original_schedule` TINYINT(1) NOT NULL DEFAULT '0';
	
ALTER TABLE `m_product_loan`
	ADD COLUMN `account_moves_out_of_npa_only_on_arrears_completion` TINYINT(1) NOT NULL DEFAULT '0';	