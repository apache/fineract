ALTER TABLE `m_deposit_account_on_hold_transaction`
	ADD COLUMN `created_date` DATETIME NOT NULL;
	
ALTER TABLE `m_guarantor_transaction`
	ALTER `loan_transaction_id` DROP DEFAULT;
	
ALTER TABLE `m_guarantor_transaction`
	CHANGE COLUMN `loan_transaction_id` `loan_transaction_id` BIGINT(20) NULL ;
	
INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ('portfolio', 'RECOVERGUARANTEES_LOAN', 'LOAN', 'RECOVERGUARANTEES', 0);
INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ('portfolio', 'RECOVERGUARANTEES_LOAN_CHECKER', 'LOAN', 'RECOVERGUARANTEES_CHECKER', 0);
ALTER TABLE `m_loan`
	DROP COLUMN `guarantee_outstanding_amount_derived`;
	