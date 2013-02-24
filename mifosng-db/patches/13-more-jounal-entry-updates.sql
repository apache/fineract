update acc_gl_journal_entry
set   acc_gl_journal_entry.entity_type=1
where acc_gl_journal_entry.entity_type='LOAN';

ALTER TABLE `acc_gl_journal_entry`
	ALTER `type_enum` DROP DEFAULT;
ALTER TABLE `acc_gl_journal_entry`
	CHANGE COLUMN `type_enum` `type_enum` SMALLINT(5) NOT NULL AFTER `entry_date`,
	CHANGE COLUMN `entity_type` `entity_type_enum` SMALLINT(5) NULL DEFAULT NULL AFTER `description`;


