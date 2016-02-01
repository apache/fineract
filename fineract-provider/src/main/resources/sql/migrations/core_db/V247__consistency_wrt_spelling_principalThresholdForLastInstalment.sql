
ALTER TABLE `m_product_loan`
	CHANGE COLUMN `principal_threshold_for_last_instalment` `principal_threshold_for_last_installment` DECIMAL(5,2) NOT NULL DEFAULT '50.00' AFTER `hold_guarantee_funds`;
