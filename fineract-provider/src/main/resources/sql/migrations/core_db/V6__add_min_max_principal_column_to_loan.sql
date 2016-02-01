ALTER TABLE `m_product_loan`
	ADD COLUMN `min_principal_amount` DECIMAL(19,6) NOT NULL AFTER `principal_amount`,
	ADD COLUMN `max_principal_amount` DECIMAL(19,6) NOT NULL AFTER `min_principal_amount`;

ALTER TABLE `m_loan`
	ADD COLUMN `min_principal_amount` DECIMAL(19,6) NOT NULL AFTER `principal_amount`,
	ADD COLUMN `max_principal_amount` DECIMAL(19,6) NOT NULL AFTER `min_principal_amount`;