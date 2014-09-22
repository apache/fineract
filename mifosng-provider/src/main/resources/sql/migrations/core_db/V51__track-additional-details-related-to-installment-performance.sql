ALTER TABLE `m_loan_repayment_schedule`
ADD COLUMN `obligations_met_on_date` DATE NULL DEFAULT NULL AFTER `completed_derived`;

ALTER TABLE `m_loan_repayment_schedule`
CHANGE COLUMN `interest_waived_derived` `interest_waived_derived` DECIMAL(19,6) NULL DEFAULT NULL AFTER `interest_writtenoff_derived` ;