ALTER TABLE `m_charge`
	ADD COLUMN `fee_on_day` SMALLINT(5) NULL AFTER `amount`,
	ADD COLUMN `fee_interval` SMALLINT(5) NULL AFTER `fee_on_day`,
	ADD COLUMN `fee_on_month` SMALLINT(5) NULL AFTER `fee_interval`;


ALTER TABLE `m_savings_account_charge`
	ADD COLUMN `fee_interval` SMALLINT(5) NULL DEFAULT NULL AFTER `fee_on_day`;