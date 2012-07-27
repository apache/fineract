-- add derived fields for loan transactions and extra

ALTER TABLE `portfolio_loan_transaction` 
ADD COLUMN `principal_portion_derived` decimal(19,6) NOT NULL DEFAULT 0,
ADD COLUMN `interest_portion_derived` decimal(19,6) NOT NULL DEFAULT 0,
ADD COLUMN `interest_waived_derived` decimal(19,6) NOT NULL DEFAULT 0;


ALTER TABLE `portfolio_loan_repayment_schedule`
ADD COLUMN `interest_waived_derived` decimal(19,6) NOT NULL DEFAULT 0;

INSERT INTO `ref_loan_status` (`id`,`display_name`) VALUES (700, 'Overpaid');
