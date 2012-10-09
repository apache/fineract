ALTER TABLE `m_product_deposit` 
  ADD COLUMN `is_lock_in_period_allowed` tinyint(1) NOT NULL DEFAULT '0' AFTER `pre_closure_interest_rate`,
  ADD COLUMN `lock_in_period` bigint(20) DEFAULT NULL AFTER `is_lock_in_period_allowed`,
  ADD COLUMN `lock_in_period_type` smallint(5) NOT NULL DEFAULT '2' AFTER `lock_in_period`;

ALTER TABLE `m_deposit_account` 
  ADD COLUMN `is_lock_in_period_allowed` tinyint(1) NOT NULL DEFAULT '0' AFTER `pre_closure_interest_rate`,
  ADD COLUMN `lock_in_period` bigint(20) DEFAULT NULL AFTER `is_lock_in_period_allowed`,
  ADD COLUMN `lock_in_period_type` smallint(5) NOT NULL DEFAULT '2' AFTER `lock_in_period`;  