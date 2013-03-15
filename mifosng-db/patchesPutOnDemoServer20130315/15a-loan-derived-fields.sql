ALTER TABLE `m_loan` 
DROP COLUMN `lastmodifiedby_id`,
DROP COLUMN `lastmodified_date`,
DROP COLUMN `created_date`,
DROP COLUMN `createdby_id`;


ALTER TABLE `m_loan` 
CHANGE COLUMN `total_charges_due_at_disbursement_derived` `total_charges_due_at_disbursement_derived` DECIMAL(19,6) NULL DEFAULT NULL  AFTER `closedon_userid` ;

ALTER TABLE `m_loan` 
ADD COLUMN `principal_disbursed_derived` DECIMAL(19,6) NOT NULL DEFAULT 0.0 AFTER `total_charges_due_at_disbursement_derived` , 
ADD COLUMN `principal_repaid_derived` DECIMAL(19,6) NOT NULL DEFAULT 0.0 AFTER `principal_disbursed_derived` , 
ADD COLUMN `principal_writtenoff_derived` DECIMAL(19,6) NOT NULL DEFAULT 0.0 AFTER `principal_repaid_derived` ,
ADD COLUMN `principal_outstanding_derived` DECIMAL(19,6) NOT NULL DEFAULT 0.0 AFTER `principal_writtenoff_derived`,
ADD COLUMN `principal_overdue_derived` DECIMAL(19,6) NOT NULL DEFAULT 0.0 AFTER `principal_outstanding_derived`,
ADD COLUMN `interest_charged_derived` DECIMAL(19,6) NOT NULL DEFAULT 0.0 AFTER `principal_overdue_derived` ,
ADD COLUMN `interest_repaid_derived` DECIMAL(19,6) NOT NULL DEFAULT 0.0 AFTER `interest_charged_derived` ,
ADD COLUMN `interest_waived_derived` DECIMAL(19,6) NOT NULL DEFAULT 0.0 AFTER `interest_repaid_derived` ,
ADD COLUMN `interest_writtenoff_derived` DECIMAL(19,6) NOT NULL DEFAULT 0.0 AFTER `interest_waived_derived` ,
ADD COLUMN `interest_outstanding_derived` DECIMAL(19,6) NOT NULL DEFAULT 0.0 AFTER `interest_writtenoff_derived`,
ADD COLUMN `interest_overdue_derived` DECIMAL(19,6) NOT NULL DEFAULT 0.0 AFTER `interest_outstanding_derived`,
ADD COLUMN `fee_charges_charged_derived` DECIMAL(19,6) NOT NULL DEFAULT 0.0 AFTER `interest_overdue_derived` ,
ADD COLUMN `fee_charges_repaid_derived` DECIMAL(19,6) NOT NULL DEFAULT 0.0 AFTER `fee_charges_charged_derived` ,
ADD COLUMN `fee_charges_waived_derived` DECIMAL(19,6) NOT NULL DEFAULT 0.0 AFTER `fee_charges_repaid_derived` ,
ADD COLUMN `fee_charges_writtenoff_derived` DECIMAL(19,6) NOT NULL DEFAULT 0.0 AFTER `fee_charges_waived_derived` ,
ADD COLUMN `fee_charges_outstanding_derived` DECIMAL(19,6) NOT NULL DEFAULT 0.0 AFTER `fee_charges_writtenoff_derived`,
ADD COLUMN `fee_charges_overdue_derived` DECIMAL(19,6) NOT NULL DEFAULT 0.0 AFTER `fee_charges_outstanding_derived`,
ADD COLUMN `penalty_charges_charged_derived` DECIMAL(19,6) NOT NULL DEFAULT 0.0 AFTER `fee_charges_overdue_derived` ,
ADD COLUMN `penalty_charges_repaid_derived` DECIMAL(19,6) NOT NULL DEFAULT 0.0 AFTER `penalty_charges_charged_derived` ,
ADD COLUMN `penalty_charges_waived_derived` DECIMAL(19,6) NOT NULL DEFAULT 0.0 AFTER `penalty_charges_repaid_derived` ,
ADD COLUMN `penalty_charges_writtenoff_derived` DECIMAL(19,6) NOT NULL DEFAULT 0.0 AFTER `penalty_charges_waived_derived`,
ADD COLUMN `penalty_charges_outstanding_derived` DECIMAL(19,6) NOT NULL DEFAULT 0.0 AFTER `penalty_charges_writtenoff_derived`,
ADD COLUMN `penalty_charges_overdue_derived` DECIMAL(19,6) NOT NULL DEFAULT 0.0 AFTER `penalty_charges_outstanding_derived`,
ADD COLUMN `total_expected_repayment_derived` DECIMAL(19,6) NOT NULL DEFAULT 0.0 AFTER `penalty_charges_overdue_derived`,
ADD COLUMN `total_repayment_derived` DECIMAL(19,6) NOT NULL DEFAULT 0.0 AFTER `total_expected_repayment_derived`,
ADD COLUMN `total_expected_costofloan_derived` DECIMAL(19,6) NOT NULL DEFAULT 0.0 AFTER `total_repayment_derived`,
ADD COLUMN `total_costofloan_derived` DECIMAL(19,6) NOT NULL DEFAULT 0.0 AFTER `total_expected_costofloan_derived`,
ADD COLUMN `total_waived_derived` DECIMAL(19,6) NOT NULL DEFAULT 0.0 AFTER `total_costofloan_derived`,
ADD COLUMN `total_writtenoff_derived` DECIMAL(19,6) NOT NULL DEFAULT 0.0 AFTER `total_waived_derived`,
ADD COLUMN `total_outstanding_derived` DECIMAL(19,6) NOT NULL DEFAULT 0.0 AFTER `total_writtenoff_derived`,
ADD COLUMN `total_overdue_derived` DECIMAL(19,6) NOT NULL DEFAULT 0.0 AFTER `total_outstanding_derived`,
ADD COLUMN `overdue_since_date_derived` DATE NULL AFTER `total_overdue_derived`;