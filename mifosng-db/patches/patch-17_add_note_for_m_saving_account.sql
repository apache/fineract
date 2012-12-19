ALTER TABLE m_note 
 ADD COLUMN `saving_account_id` bigint(20) DEFAULT NULL AFTER `deposit_account_id`,
 ADD KEY `FK_m_note_m_saving_account` (`saving_account_id`),
 ADD CONSTRAINT `FK_m_note_m_saving_account` FOREIGN KEY (`saving_account_id`) REFERENCES `m_saving_account` (`id`);