ALTER TABLE `m_product_loan`
	ADD COLUMN `principal_threshold_for_last_instalment` DECIMAL(5,2) NOT NULL DEFAULT '50';
	
update m_product_loan pl set pl.principal_threshold_for_last_instalment = 0 where pl.allow_multiple_disbursals = 0;

	
	
	