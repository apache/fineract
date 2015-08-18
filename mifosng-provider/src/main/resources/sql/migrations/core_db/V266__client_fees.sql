-- add permissions for Client Fees

INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ('portfolio', 'CREATE_CLIENTCHARGE', 'CLIENTCHARGE', 'CREATE', 0);
INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ('portfolio', 'DELETE_CLIENTCHARGE', 'CLIENTCHARGE', 'DELETE', 0);
INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ('portfolio', 'WAIVE_CLIENTCHARGE', 'CLIENTCHARGE', 'WAIVE', 0);
INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ('portfolio', 'PAY_CLIENTCHARGE', 'CLIENTCHARGE', 'PAY', 0);
INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ('portfolio', 'INACTIVATE_CLIENTCHARGE', 'CLIENTCHARGE', 'INACTIVATE', 0);
INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ('portfolio', 'UPDATE_CLIENTCHARGE', 'CLIENTCHARGE', 'UPDATE', 0);

