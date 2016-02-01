UPDATE m_permission  SET
action_name="UPDATE" 
WHERE
code = "UPDATE_TELLER";

INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`) VALUES ('cash_mgmt', 'DELETE_TELLER', 'TELLER', 'DELETE');

	
ALTER TABLE `m_cashier_transactions`
	DROP FOREIGN KEY `FK_m_teller_transactions_m_cashiers`;
	
ALTER TABLE `m_cashier_transactions`
	ADD CONSTRAINT `FK_m_teller_transactions_m_cashiers` FOREIGN KEY (`cashier_id`) REFERENCES `m_cashiers` (`id`) ON UPDATE CASCADE ON DELETE CASCADE;