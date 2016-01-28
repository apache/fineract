ALTER TABLE `m_client`
	ADD COLUMN `default_savings_account` BIGINT(20) NULL DEFAULT NULL AFTER `default_savings_product`,
	ADD CONSTRAINT `FK_m_client_m_savings_account` FOREIGN KEY (`default_savings_account`) REFERENCES `m_savings_account` (`id`);

INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ('portfolio', 'UPDATESAVINGSACCOUNT_CLIENT', 'CLIENT', 'UPDATESAVINGSACCOUNT', 0);
