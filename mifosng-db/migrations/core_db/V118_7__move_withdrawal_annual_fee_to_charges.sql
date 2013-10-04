ALTER TABLE `m_savings_account_charge`
	ADD COLUMN `is_active` TINYINT(1) NOT NULL DEFAULT '1' AFTER `waived`;

ALTER TABLE `m_savings_account`
	DROP COLUMN `withdrawal_fee_amount`,
	DROP COLUMN `withdrawal_fee_type_enum`;

ALTER TABLE `m_savings_account_charge`
	ADD COLUMN `fee_on_month` SMALLINT(5) NULL DEFAULT NULL AFTER `due_for_collection_as_of_date`,
	ADD COLUMN `fee_on_day` SMALLINT(5) NULL DEFAULT NULL AFTER `fee_on_month`;

ALTER TABLE `m_savings_account_charge`
	CHANGE COLUMN `due_for_collection_as_of_date` `charge_due_date` DATE NULL DEFAULT NULL AFTER `charge_time_enum`;		


ALTER TABLE `m_savings_account`
	DROP COLUMN `annual_fee_amount`,
	DROP COLUMN `annual_fee_on_month`,
	DROP COLUMN `annual_fee_on_day`,
	DROP COLUMN `annual_fee_next_due_date`;

ALTER TABLE `m_savings_product`
	DROP COLUMN `annual_fee_amount`,
	DROP COLUMN `annual_fee_on_month`,
	DROP COLUMN `annual_fee_on_day`;		