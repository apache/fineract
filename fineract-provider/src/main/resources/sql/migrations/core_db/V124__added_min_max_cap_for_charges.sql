ALTER TABLE `m_charge`
ADD COLUMN `min_cap` DECIMAL(19,6) NULL AFTER `is_deleted`,
ADD COLUMN `max_cap` DECIMAL(19,6) NULL AFTER `min_cap`;

ALTER TABLE `m_loan_charge`
ADD COLUMN `min_cap` DECIMAL(19,6) NULL AFTER `waived`,
ADD COLUMN `max_cap` DECIMAL(19,6) NULL AFTER `min_cap`;
