ALTER TABLE `m_role` ADD COLUMN `is_disabled` TINYINT(1) NOT NULL DEFAULT 0 AFTER `description`;
INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ('authorisation', 'DISABLE_ROLE', 'ROLE', 'DISABLE', 0);
INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ('authorisation', 'DISABLE_ROLE_CHECKER', 'ROLE', 'DISABLE_CHECKER', 0);
INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ('authorisation', 'ENABLE_ROLE', 'ROLE', 'ENABLE', 0);
INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ('authorisation', 'ENABLE_ROLE_CHECKER', 'ROLE', 'ENABLE_CHECKER', 0);