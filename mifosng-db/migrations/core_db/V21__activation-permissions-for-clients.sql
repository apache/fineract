INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ('portfolio', 'ACTIVATE_CLIENT', 'CLIENT', 'ACTIVATE', 1);
INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ('portfolio', 'ACTIVATE_CLIENT_CHECKER', 'CLIENT', 'ACTIVATE', 0);


ALTER TABLE `m_client` 
CHANGE COLUMN `external_id` `external_id` VARCHAR(100) NULL DEFAULT NULL AFTER `account_no`, 
CHANGE COLUMN `status_enum` `status_enum` INT(5) NOT NULL DEFAULT '300' AFTER `external_id`, 
CHANGE COLUMN `joined_date` `activation_date` DATE NULL DEFAULT NULL  AFTER `status_enum`;