-- Example: INSERT INTO `acc_gl_account` 
--	(
-- `name`, `parent_id`, `hierarchy`, `gl_code`, `disabled`, `manual_journal_entries_allowed`,
--	`account_usage`, `classification_enum`, `tag_id`, `description`
--	)
--	VALUES 
--	(
--	'Opening Balances Contra Account', NULL, '.', 'OBCA', 0, 1, 
--	1, 3, NULL, NULL
--	);

INSERT INTO `c_configuration` 
	(
		`name`, 
		`value`, 
		`enabled`
	)
	VALUES 
	(
		'office-opening-balances-contra-account', 
		0, -- Or Example: (SELECT id FROM acc_gl_account WHERE gl_code = 'OBCA' ),
		1);

ALTER TABLE `c_configuration`
	ADD UNIQUE INDEX `name_UNIQUE` (`name`);
	
INSERT INTO `m_permission`
	(
		`grouping`, `code`, `entity_name`, `action_name`
	) 
	VALUES 
	(
		'accounting', 'DEFINEOPENINGBALANCE_JOURNALENTRY', 'JOURNALENTRY', 'DEFINEOPENINGBALANCE'
	);
