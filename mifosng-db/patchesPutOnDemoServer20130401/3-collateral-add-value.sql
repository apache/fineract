ALTER TABLE `m_loan_collateral`
	ADD COLUMN `value` DECIMAL(19,6) NULL DEFAULT NULL AFTER `type_cv_id`;

INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) 
VALUES('portfolio', 'CREATE_COLLATERAL', 'COLLATERAL', 'CREATE', '0'),
('portfolio', 'READ_COLLATERAL', 'COLLATERAL', 'READ', '0'),
('portfolio', 'UPDATE_COLLATERAL', 'COLLATERAL', 'UPDATE', '0'),
('portfolio', 'DELETE_COLLATERAL', 'COLLATERAL', 'DELETE', '0'),
('portfolio', 'CREATE_COLLATERAL_CHECKER', 'COLLATERAL', 'CREATE', '0'),
('portfolio', 'UPDATE_COLLATERAL_CHECKER', 'COLLATERAL', 'UPDATE', '0'),
('portfolio', 'DELETE_COLLATERAL_CHECKER', 'COLLATERAL', 'DELETE', '0');

