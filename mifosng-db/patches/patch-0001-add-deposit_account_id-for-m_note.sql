ALTER TABLE `m_note`
ADD COLUMN `deposit_account_id` bigint(20) DEFAULT NULL AFTER `loan_transaction_id`, 
ADD KEY `FK_m_note_m_deposit_account` (`deposit_account_id`),
ADD CONSTRAINT `FK_m_note_m_deposit_account` FOREIGN KEY (`deposit_account_id`) REFERENCES `m_deposit_account` (`id`);
