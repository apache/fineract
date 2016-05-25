ALTER TABLE `m_product_loan`
	ADD COLUMN `is_subsidy_applicable` TINYINT(1) NULL DEFAULT NULL;
	
ALTER TABLE `m_loan`
	ADD COLUMN `is_subsidy_applicable` TINYINT(1) NULL DEFAULT NULL;
	
INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) 
	VALUES ('transaction_loan', 'SUBSIDYADD_LOAN_CHECKER', 'LOAN', 'SUBSIDYADD_CHECKER', 0);
	
INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) 
	VALUES ('transaction_loan', 'SUBSIDYADD_LOAN', 'LOAN', 'SUBSIDYADD', 0);	
	
INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) 
	VALUES ('transaction_loan', 'SUBSIDYREVOKE_LOAN', 'LOAN', 'SUBSIDYREVOKE', 0);	
	
INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) 
	VALUES ('transaction_loan', 'SUBSIDYREVOKE_LOAN_CHECKER', 'LOAN', 'SUBSIDYREVOKE_CHECKER', 0);
	
ALTER TABLE `m_loan_transaction`
	ADD COLUMN `transaction_sub_type_enum` SMALLINT(5) NULL AFTER `appuser_id`;