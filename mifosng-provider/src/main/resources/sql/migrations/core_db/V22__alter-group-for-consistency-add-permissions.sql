INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ('portfolio', 'ACTIVATE_CENTER', 'CENTER', 'ACTIVATE', 1);
INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ('portfolio', 'ACTIVATE_CENTER_CHECKER', 'CENTER', 'ACTIVATE', 0);

INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ('portfolio', 'ACTIVATE_GROUP', 'GROUP', 'ACTIVATE', 1);
INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ('portfolio', 'ACTIVATE_GROUP_CHECKER', 'GROUP', 'ACTIVATE', 0);


ALTER TABLE `m_group` DROP FOREIGN KEY `FK_m_group_level`;
ALTER TABLE `m_group`
CHANGE COLUMN `external_id` `external_id` VARCHAR(100) NULL DEFAULT NULL AFTER `id`,
CHANGE COLUMN `status_enum` `status_enum` INT(5) NOT NULL DEFAULT '300' AFTER `external_id`,
CHANGE COLUMN `name` `display_name` VARCHAR(100) NOT NULL  AFTER `level_id`,
CHANGE COLUMN `level_Id` `level_id` INT(11) NOT NULL,
ADD CONSTRAINT `FK_m_group_level` FOREIGN KEY (`level_id`) REFERENCES `m_group_level` (`id`);


ALTER TABLE `m_group`
DROP COLUMN `is_deleted`,
ADD COLUMN `activation_date` DATE NULL DEFAULT NULL AFTER `status_enum` ;