ALTER TABLE `m_loan_transaction` 
ADD COLUMN `is_reversed` TINYINT(1) NOT NULL AFTER `penalty_charges_portion_derived`;

-- === update is_reversed column to true for all fields where contra_id is not null

UPDATE `m_loan_transaction` SET `is_reversed` = 1 WHERE contra_id is not null;

ALTER TABLE `m_loan_transaction` DROP FOREIGN KEY `FKCFCEA426FC69F3F1`;
ALTER TABLE `m_loan_transaction` 
DROP COLUMN `contra_id` , 
DROP INDEX `FKCFCEA426FC69F3F1`;


ALTER TABLE `m_loan_transaction` 
DROP COLUMN `lastmodifiedby_id`,
DROP COLUMN `lastmodified_date`,
DROP COLUMN `created_date`,
DROP COLUMN `createdby_id`;