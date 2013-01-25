Alter TABLE `m_saving_schedule` 
 ADD COLUMN `interest_accured` decimal(21,4) DEFAULT '0.0000' AFTER `deposit_paid`
  