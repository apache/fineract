ALTER TABLE `m_savings_account`
	ADD COLUMN `account_type_enum` SMALLINT(5) NOT NULL DEFAULT '1' AFTER `status_enum`;