ALTER TABLE `acc_gl_journal_entry`
	ADD COLUMN `organization_running_balance` DECIMAL(19,6) NOT NULL DEFAULT '0.000000' AFTER `office_running_balance`;

INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ('accounting', 'UPDATERUNNINGBALANCE_JOURNALENTRY', 'JOURNALENTRY', 'UPDATERUNNINGBALANCE', 0);
