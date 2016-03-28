ALTER TABLE m_product_loan
	ADD COLUMN `recurring_moratorium_principal_periods` SMALLINT(5) AFTER `grace_on_principal_periods`;
	
ALTER TABLE m_loan
	ADD COLUMN `recurring_moratorium_principal_periods` SMALLINT(5) AFTER `grace_on_principal_periods`;