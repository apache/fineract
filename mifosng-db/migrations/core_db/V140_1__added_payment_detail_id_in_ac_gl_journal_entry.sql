ALTER TABLE `acc_gl_journal_entry`
	ADD COLUMN `payment_details_id` BIGINT(20) NULL AFTER `organization_running_balance`,
	ADD CONSTRAINT `FK_acc_gl_journal_entry_m_payment_detail` FOREIGN KEY (`payment_details_id`) REFERENCES `m_payment_detail` (`id`);