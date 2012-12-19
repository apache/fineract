ALTER TABLE `m_saving_account` 
 ADD COLUMN `interest_posted_amount` decimal(19,6) DEFAULT '0.000000' AFTER `outstanding_amount`,
 ADD COLUMN `last_interest_posted_date` date DEFAULT NULL AFTER `interest_posted_amount`,
 ADD COLUMN `next_interest_posting_date` date DEFAULT NULL AFTER `last_interest_posted_date`;