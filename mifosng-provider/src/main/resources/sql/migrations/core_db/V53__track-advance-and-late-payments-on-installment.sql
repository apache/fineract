ALTER TABLE `m_loan_repayment_schedule`
ADD COLUMN `total_paid_in_advance_derived` DECIMAL(19,6) NULL DEFAULT NULL AFTER `penalty_charges_waived_derived`,
ADD COLUMN `total_paid_late_derived` DECIMAL(19,6) NULL DEFAULT NULL  AFTER `total_paid_in_advance_derived`;