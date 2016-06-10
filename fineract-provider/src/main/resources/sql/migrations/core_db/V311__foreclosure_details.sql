INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES 
('portfolio', 'FORECLOSURE_LOAN', 'LOAN', 'FORECLOSURE', 0), 
('portfolio', 'FORECLOSURE_LOAN_CHECKER', 'LOAN', 'FORECLOSURE_CHECKER', 0);


ALTER TABLE `m_loan`
	ADD COLUMN `loan_sub_status_id` SMALLINT(5) NULL DEFAULT NULL;