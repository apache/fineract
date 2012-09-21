ALTER TABLE `m_loan` 
DROP COLUMN `total_charges_due_at_disbursement_derived`,
ADD COLUMN `total_charges_due_at_disbursement_derived` decimal(19,6) DEFAULT NULL AFTER `interest_rebate_amount`;
