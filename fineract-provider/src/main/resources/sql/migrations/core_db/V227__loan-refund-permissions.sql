INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`)
VALUES
	('transaction_savings', 'REFUNDBYTRANSFER_ACCOUNTTRANSFER_CHECKER', 'ACCOUNTTRANSFER', 'REFUNDBYTRANSFER', 0);

	INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`)
VALUES
	( 'transaction_savings', 'REFUNDBYTRANSFER_ACCOUNTTRANSFER', 'ACCOUNTTRANSFER', 'REFUNDBYTRANSFER', 1);

	INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`)
VALUES
	('transaction_loan', 'REFUNDBYCASH_LOAN', 'LOAN', 'REFUNDBYCASH', 1);
	
INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`)
VALUES
	('transaction_loan', 'REFUNDBYCASH_LOAN_CHECKER', 'LOAN', 'REFUNDBYCASH', 0);	

