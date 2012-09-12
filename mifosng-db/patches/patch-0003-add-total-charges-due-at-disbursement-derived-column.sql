ALTER TABLE `m_loan`
ADD COLUMN `total_charges_due_at_disbursement_derived` bigint(20) DEFAULT NULL AFTER `interest_rebate_amount`;
