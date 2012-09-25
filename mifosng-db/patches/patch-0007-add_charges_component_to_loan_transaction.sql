ALTER TABLE `m_loan_transaction` 
ADD COLUMN `charges_portion_derived` decimal(19,6) DEFAULT NULL AFTER `interest_waived_derived`;
