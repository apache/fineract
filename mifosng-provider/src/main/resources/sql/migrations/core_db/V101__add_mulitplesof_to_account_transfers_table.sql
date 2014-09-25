ALTER TABLE `m_savings_account_transfer`
ADD COLUMN `currency_multiplesof` SMALLINT(5) NULL DEFAULT NULL AFTER `currency_digits`;