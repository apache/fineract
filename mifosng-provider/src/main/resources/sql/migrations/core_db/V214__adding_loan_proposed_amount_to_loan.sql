   	ALTER TABLE `m_loan` ADD COLUMN `principal_amount_proposed` DECIMAL(19,6) NOT NULL AFTER `currency_multiplesof`;
	UPDATE m_loan SET principal_amount_proposed = approved_principal;