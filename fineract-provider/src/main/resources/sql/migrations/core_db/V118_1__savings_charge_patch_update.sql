ALTER TABLE `m_savings_account`
	ADD COLUMN `total_fees_charge_derived` DECIMAL(19,6) NULL DEFAULT NULL AFTER `total_withdrawal_fees_derived`,
	ADD COLUMN `total_penalty_charge_derived` DECIMAL(19,6) NULL DEFAULT NULL AFTER `total_fees_charge_derived`;