ALTER TABLE `m_note`
ADD COLUMN `share_account_id` BIGINT(20) NULL DEFAULT NULL AFTER `savings_account_transaction_id`;