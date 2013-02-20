ALTER TABLE `acc_gl_journal_entry`
	CHANGE COLUMN `portfolio_generated` `manual_entry` TINYINT(1) NOT NULL DEFAULT '0' AFTER `reversed`;

update acc_gl_journal_entry
set acc_gl_journal_entry.manual_entry=1
where acc_gl_journal_entry.entity_type is null;

update acc_gl_journal_entry
set acc_gl_journal_entry.manual_entry=0
where acc_gl_journal_entry.entity_type is not null;

