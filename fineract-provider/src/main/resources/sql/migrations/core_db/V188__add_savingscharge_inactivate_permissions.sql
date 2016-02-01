INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ('portfolio', 'INACTIVATE_SAVINGSACCOUNTCHARGE', 'SAVINGSACCOUNTCHARGE', 'INACTIVATE', 0);
INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ('portfolio', 'INACTIVATE_SAVINGSACCOUNTCHARGE_CHECKER', 'SAVINGSACCOUNTCHARGE', 'INACTIVATE_CHECKER', 0);

ALTER TABLE `m_savings_account_charge`
	ADD COLUMN `inactivated_on_date` DATE NULL DEFAULT NULL AFTER `is_active`;