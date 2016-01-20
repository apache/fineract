ALTER TABLE `m_savings_account`
ADD COLUMN `nominal_annual_interest_rate_overdraft` DECIMAL(19,6) NULL DEFAULT 0 AFTER `overdraft_limit`,
ADD COLUMN `total_overdraft_interest_derived` DECIMAL(19,6) NULL DEFAULT 0 AFTER `total_interest_posted_derived`,
ADD COLUMN `min_overdraft_for_interest_calculation` DECIMAL(19,6) NULL DEFAULT 0 AFTER `nominal_annual_interest_rate_overdraft`;

ALTER TABLE `m_savings_product`
ADD COLUMN `nominal_annual_interest_rate_overdraft` DECIMAL(19,6) NULL DEFAULT 0 AFTER `overdraft_limit`,
ADD COLUMN `min_overdraft_for_interest_calculation` DECIMAL(19,6) NULL DEFAULT 0 AFTER `nominal_annual_interest_rate_overdraft`;
