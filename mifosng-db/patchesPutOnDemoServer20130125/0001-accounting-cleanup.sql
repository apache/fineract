-- Make classification an enumeration for GL Account table
update acc_gl_account
set acc_gl_account.classification=1 where acc_gl_account.classification="ASSET";

update acc_gl_account
set acc_gl_account.classification=2 where acc_gl_account.classification="LIABILITY";

update acc_gl_account
set acc_gl_account.classification=3 where acc_gl_account.classification="EQUITY";

update acc_gl_account
set acc_gl_account.classification=4 where acc_gl_account.classification="INCOME";

update acc_gl_account
set acc_gl_account.classification=5 where acc_gl_account.classification="EXPENSE";


ALTER TABLE `acc_gl_account`
	ALTER `classification` DROP DEFAULT;
ALTER TABLE `acc_gl_account`
	CHANGE COLUMN `classification` `classification_enum` SMALLINT(5) NOT NULL AFTER `header_account`;

-- Rename is header account to usage in GL Account Table

update acc_gl_account
set acc_gl_account.header_account=2 where acc_gl_account.header_account=1;

update acc_gl_account
set acc_gl_account.header_account=1 where acc_gl_account.header_account=0;

ALTER TABLE `acc_gl_account`
	CHANGE COLUMN `header_account` `account_usage` TINYINT(1) NOT NULL DEFAULT '2' AFTER `manual_journal_entries_allowed`;

-- Update Type in journal entries table

update acc_gl_journal_entry
set acc_gl_journal_entry.type=1 where acc_gl_journal_entry.type="CREDIT";

update acc_gl_journal_entry
set acc_gl_journal_entry.type=2 where acc_gl_journal_entry.type="DEBIT";

ALTER TABLE `acc_gl_journal_entry`
	ALTER `type` DROP DEFAULT;
ALTER TABLE `acc_gl_journal_entry`
	CHANGE COLUMN `type` `type_enum` SMALLINT(50) NOT NULL AFTER `entry_date`;