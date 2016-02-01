ALTER TABLE `m_savings_account`
	ADD COLUMN `version` INT(15) NOT NULL DEFAULT '1' AFTER `on_hold_funds_derived`;
