ALTER TABLE `m_savings_product` 
ADD COLUMN `annual_fee_amount` DECIMAL(19,6) NULL DEFAULT NULL AFTER `withdrawal_fee_type_enum`,
ADD COLUMN `annual_fee_on_month` SMALLINT(5) NULL DEFAULT NULL AFTER `annual_fee_amount`,
ADD COLUMN `annual_fee_on_day` SMALLINT(5) NULL DEFAULT NULL AFTER `annual_fee_on_month`;

ALTER TABLE `m_savings_account`
ADD COLUMN `annual_fee_amount` DECIMAL(19,6) NULL DEFAULT NULL AFTER `withdrawal_fee_type_enum`,
ADD COLUMN `annual_fee_on_month` SMALLINT(5) NULL DEFAULT NULL AFTER `annual_fee_amount`,
ADD COLUMN `annual_fee_on_day` SMALLINT(5) NULL DEFAULT NULL AFTER `annual_fee_on_month`,
ADD COLUMN `annual_fee_next_due_date` DATE NULL DEFAULT NULL AFTER `annual_fee_on_day`;

ALTER TABLE `m_savings_account` 
ADD COLUMN `total_annual_fees_derived` DECIMAL(19,6) NULL DEFAULT NULL AFTER `total_withdrawal_fees_derived`;