-- Charges have a reference to an Income or Liability account that gets credited when a payment is made
ALTER TABLE `m_charge`
	ADD COLUMN `income_or_liability_account_id` BIGINT(20) NULL AFTER `fee_frequency`,
	ADD CONSTRAINT `FK_m_charge_acc_gl_account` FOREIGN KEY (`income_or_liability_account_id`) REFERENCES `acc_gl_account` (`id`);

--Journal entries also refer to Client transactions
ALTER TABLE `acc_gl_journal_entry`
	ADD COLUMN `client_transaction_id` BIGINT(20) NULL DEFAULT NULL AFTER `savings_transaction_id`,
	ADD CONSTRAINT `FK_acc_gl_journal_entry_m_client_transaction` FOREIGN KEY (`client_transaction_id`) REFERENCES `m_client_transaction` (`id`);
