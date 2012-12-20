ALTER TABLE m_product_savings 
  ADD COLUMN `deposit_every`  bigint(20) DEFAULT NULL AFTER `tenure_type`;