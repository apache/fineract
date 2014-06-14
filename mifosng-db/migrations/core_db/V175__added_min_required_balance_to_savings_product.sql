ALTER TABLE `m_savings_product`
	ADD COLUMN `min_required_balance` DECIMAL(19,6) NULL AFTER `overdraft_limit`,
	ADD COLUMN `allow_overdraft_min_balance` TINYINT(1) NOT NULL DEFAULT '0' AFTER `min_required_balance`;

ALTER TABLE `m_savings_account`
	ADD COLUMN `min_required_balance` DECIMAL(19,6) NULL AFTER `account_balance_derived`,
	ADD COLUMN `allow_overdraft_min_balance` TINYINT(1) NOT NULL DEFAULT '0' AFTER `min_required_balance`;
