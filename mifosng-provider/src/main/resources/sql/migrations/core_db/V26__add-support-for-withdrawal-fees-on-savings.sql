ALTER TABLE `m_savings_product`
ADD COLUMN `withdrawal_fee_amount` DECIMAL(19,6) NULL DEFAULT NULL AFTER `lockin_period_frequency_enum`,
ADD COLUMN `withdrawal_fee_type_enum` SMALLINT(5) NULL DEFAULT NULL AFTER `withdrawal_fee_amount`;


ALTER TABLE `m_savings_account`
ADD COLUMN `withdrawal_fee_amount` DECIMAL(19,6) NULL DEFAULT NULL AFTER `lockin_period_frequency_enum`,
ADD COLUMN `withdrawal_fee_type_enum` SMALLINT(5) NULL DEFAULT NULL AFTER `withdrawal_fee_amount`;


ALTER TABLE `m_savings_account`
ADD COLUMN `total_withdrawal_fees_derived` DECIMAL(19,6) NULL DEFAULT NULL AFTER `total_withdrawals_derived`;