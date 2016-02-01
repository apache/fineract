ALTER TABLE `m_product_loan`
	ADD COLUMN `grace_on_arrears_ageing` SMALLINT(5) NULL DEFAULT NULL AFTER `max_outstanding_loan_balance`;

ALTER TABLE `m_loan`
	ADD COLUMN `grace_on_arrears_ageing` SMALLINT(5) NULL DEFAULT NULL AFTER `max_outstanding_loan_balance`;