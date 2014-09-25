ALTER TABLE `m_savings_product`
	CHANGE COLUMN `allow_overdraft_min_balance` `enforce_min_required_balance` TINYINT(1) NOT NULL DEFAULT '0' AFTER `min_required_balance`;

ALTER TABLE `m_savings_account`
	CHANGE COLUMN `allow_overdraft_min_balance` `enforce_min_required_balance` TINYINT(1) NOT NULL DEFAULT '0' AFTER `min_required_balance`;
