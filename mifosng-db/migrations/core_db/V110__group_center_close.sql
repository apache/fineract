INSERT INTO `m_code` (`code_name`, `is_system_defined`) VALUES ('GroupClosureReason', 1);

ALTER TABLE `m_group` ADD COLUMN `closure_reason_cv_id` INT(11) NULL DEFAULT NULL,
ADD COLUMN `closedon_date` DATE NULL DEFAULT NULL,
ADD CONSTRAINT `FK_m_group_m_code` FOREIGN KEY (`closure_reason_cv_id`) REFERENCES `m_code_value` (`id`);

INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`) VALUES ('portfolio', 'CLOSE_GROUP', 'GROUP', 'CLOSE');
INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`) VALUES ('portfolio', 'CLOSE_CENTER', 'CENTER', 'CLOSE');
