ALTER TABLE `m_deposit_account` 
ADD COLUMN `withdrawnon_date` datetime DEFAULT NULL AFTER `pre_closure_interest_rate`,
ADD COLUMN `rejectedon_date` datetime DEFAULT NULL AFTER `withdrawnon_date`,
ADD COLUMN `closedon_date` datetime DEFAULT NULL AFTER `rejectedon_date`;