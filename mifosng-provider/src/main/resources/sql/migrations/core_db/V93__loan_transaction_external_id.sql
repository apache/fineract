ALTER TABLE m_loan_transaction
ADD COLUMN `external_id` VARCHAR(100) NULL DEFAULT NULL  AFTER `is_reversed`  ,
ADD UNIQUE INDEX `external_id_UNIQUE` (`external_id` ASC) ;