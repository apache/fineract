ALTER TABLE `m_savings_product`
	ADD COLUMN `allow_overdraft` TINYINT(1) NOT NULL DEFAULT '0' AFTER `withdrawal_fee_for_transfer`,
	ADD COLUMN `overdraft_limit` DECIMAL(19,6) NULL DEFAULT NULL AFTER `allow_overdraft`;

ALTER TABLE `m_savings_account`
	ADD COLUMN `allow_overdraft` TINYINT(1) NOT NULL DEFAULT '0' AFTER `withdrawal_fee_for_transfer`,
	ADD COLUMN `overdraft_limit` DECIMAL(19,6) NULL DEFAULT NULL AFTER `allow_overdraft`;

ALTER TABLE `m_savings_account_transaction`
	ADD COLUMN `overdraft_amount_derived` DECIMAL(19,6) NULL DEFAULT NULL AFTER `amount`;

ALTER TABLE `m_client`
	ADD COLUMN `default_savings_product` BIGINT(20) NULL DEFAULT NULL AFTER `closedon_userid`,
	ADD CONSTRAINT `FK_m_client_m_savings_product` FOREIGN KEY (`default_savings_product`) REFERENCES `m_savings_product` (`id`);