ALTER TABLE `m_loan_charge` 
ADD COLUMN `calculation_percentage` decimal(19,6) DEFAULT NULL AFTER `charge_calculation_enum`;
ALTER TABLE `m_loan_charge` 
ADD COLUMN `calculation_on_amount` decimal(19,6) DEFAULT NULL AFTER `calculation_percentage`;
ALTER TABLE `m_loan_charge` 
ADD COLUMN `amount_paid_derived` decimal(19,6) DEFAULT NULL AFTER `amount`;
ALTER TABLE `m_loan_charge` 
ADD COLUMN `amount_outstanding_derived` decimal(19,6) NOT NULL default 0 AFTER `amount_paid_derived`;
ALTER TABLE `m_loan_charge` 
ADD COLUMN `is_paid_derived` tinyint(1) NOT NULL default 0 AFTER `amount_outstanding_derived`;