ALTER TABLE `ref_loan_transaction_processing_strategy`
DROP COLUMN `lastmodified_date` ,
DROP COLUMN `lastmodifiedby_id` ,
DROP COLUMN `created_date` ,
DROP COLUMN `createdby_id` ;


INSERT INTO `ref_loan_transaction_processing_strategy` (`id`, `code`, `name`) VALUES
(5,'principal-interest-penalties-fees-order-strategy', 'Principal Interest Penalties Fees Order');

INSERT INTO `ref_loan_transaction_processing_strategy` (`id`,`code`, `name`)
VALUES (6,'interest-principal-penalties-fees-order-strategy', 'Interest Principal Penalties Fees Order');