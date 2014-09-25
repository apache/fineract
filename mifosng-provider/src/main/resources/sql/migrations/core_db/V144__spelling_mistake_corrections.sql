RENAME TABLE `x_table_cloumn_code_mappings` TO `x_table_column_code_mappings`;

ALTER TABLE `acc_gl_journal_entry`
	CHANGE COLUMN `is_running_balance_caculated` `is_running_balance_calculated` TINYINT(4) NOT NULL DEFAULT '0' AFTER `lastmodified_date`;