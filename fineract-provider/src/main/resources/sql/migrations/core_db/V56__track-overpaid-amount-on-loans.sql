ALTER TABLE `m_loan`
ADD COLUMN `total_overpaid_derived` DECIMAL(19,6) NULL DEFAULT NULL AFTER `total_outstanding_derived`;