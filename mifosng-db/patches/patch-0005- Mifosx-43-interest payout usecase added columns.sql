ALTER TABLE `m_deposit_account_transaction`
 ADD COLUMN `interest` decimal(19,6) NOT NULL AFTER `amount`,
 ADD COLUMN `total` decimal(19,6) NOT NULL AFTER `interest`;
 
ALTER TABLE `m_deposit_account`
 ADD COLUMN `interest_paid` decimal(19,6) NOT NULL DEFAULT 0 AFTER `actual_total_amount`,
 ADD COLUMN `is_interest_withdrawable` tinyint(1) NOT NULL DEFAULT '0' AFTER `interest_paid`;
 
ALTER TABLE `m_product_deposit`
 ADD COLUMN `is_compounding_interest_allowed` tinyint(1) NOT NULL DEFAULT '0' AFTER `maturity_max_interest_rate`;
 
ALTER TABLE `m_deposit_account`
 ADD COLUMN `is_compounding_interest_allowed` tinyint(1) NOT NULL DEFAULT '0' AFTER `actual_total_amount`; 