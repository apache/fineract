ALTER TABLE `acc_accounting_rule`
	CHANGE COLUMN `debit_account_id` `debit_account_id` BIGINT(20) NULL DEFAULT NULL AFTER `office_id`,
	CHANGE COLUMN `credit_account_id` `credit_account_id` BIGINT(20) NULL DEFAULT NULL AFTER `allow_multiple_debits`;