ALTER TABLE `acc_gl_journal_entry`
	ADD COLUMN `loan_transaction_id` BIGINT(20) NULL AFTER `transaction_id`,
	ADD COLUMN `savings_transaction_id` BIGINT(20) NULL AFTER `loan_transaction_id`,
	ADD CONSTRAINT `FK_acc_gl_journal_entry_m_loan_transaction` FOREIGN KEY (`loan_transaction_id`) REFERENCES `m_loan_transaction` (`id`),
	ADD CONSTRAINT `FK_acc_gl_journal_entry_m_savings_account_transaction` FOREIGN KEY (`savings_transaction_id`) REFERENCES `m_savings_account_transaction` (`id`);

UPDATE acc_gl_journal_entry je SET je.savings_transaction_id=je.transaction_id,je.transaction_id=Concat('S',je.transaction_id) WHERE  je.entity_type_enum=2;

UPDATE acc_gl_journal_entry je SET je.loan_transaction_id=je.transaction_id,je.transaction_id=Concat('L',je.transaction_id) WHERE  je.entity_type_enum=1;
