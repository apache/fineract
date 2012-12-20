ALTER TABLE m_saving_account 
  ADD COLUMN `deposit_every`  bigint(20) DEFAULT NULL AFTER `tenure_type`;