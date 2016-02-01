ALTER TABLE `m_savings_product`
	ADD COLUMN `min_balance_for_interest_calculation` DECIMAL(19,6) NULL DEFAULT NULL AFTER `allow_overdraft_min_balance`;

ALTER TABLE `m_savings_account`
	ADD COLUMN `min_balance_for_interest_calculation` DECIMAL(19,6) NULL DEFAULT NULL AFTER `allow_overdraft_min_balance`;
