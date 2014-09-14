ALTER TABLE `m_savings_account`
ADD COLUMN `start_interest_calculation_date` DATE NULL DEFAULT NULL AFTER `min_balance_for_interest_calculation`;
