ALTER TABLE `m_savings_product`
	ADD COLUMN `withdrawal_fee_for_transfer` TINYINT NULL DEFAULT '1' AFTER `withdrawal_fee_type_enum`;

ALTER TABLE `m_savings_account`
	ADD COLUMN `withdrawal_fee_for_transfer` TINYINT NULL DEFAULT '1' AFTER `withdrawal_fee_type_enum`;
