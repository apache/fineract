/**Permissions for proposing and accepting a transfer**/
INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`)
VALUES ('portfolio', 'PROPOSEANDACCEPTTRANSFER_CLIENT', 'CLIENT', 'PROPOSEANDACCEPTTRANSFER', 0);

INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`)
VALUES ('portfolio', 'PROPOSEANDACCEPTTRANSFER_CLIENT_CHECKER', 'CLIENT', 'PROPOSEANDACCEPTTRANSFER', 0);
