INSERT INTO `m_code` (`code_name`, `is_system_defined`) VALUES ('GROUPROLE', 1);

INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ('portfolio', 'ASSIGNROLE_GROUP', 'GROUP', 'ASSIGNROLE', 0);
INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ('portfolio', 'UNASSIGNROLE_GROUP', 'GROUP', 'UNASSIGNROLE', 0);
INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ('portfolio', 'UPDATEROLE_GROUP', 'GROUP', 'UPDATEROLE', 0);

CREATE TABLE `m_group_roles` (
	`id` BIGINT(20) NOT NULL AUTO_INCREMENT,
	`client_id` BIGINT(20) NULL DEFAULT NULL,
	`group_id` BIGINT(20) NULL DEFAULT NULL,
	`role_cv_id` INT(11) NULL DEFAULT NULL,
	PRIMARY KEY (`id`),
	INDEX `FKGroupRoleClientId` (`client_id`),
	INDEX `FKGroupRoleGroupId` (`group_id`),
	INDEX `FK_grouprole_m_codevalue` (`role_cv_id`),
	UNIQUE INDEX `UNIQUE_GROUP_ROLES` (`client_id`, `group_id`, `role_cv_id`),
	CONSTRAINT `FKGroupRoleClientId` FOREIGN KEY (`client_id`) REFERENCES `m_client` (`id`),
	CONSTRAINT `FKGroupRoleGroupId` FOREIGN KEY (`group_id`) REFERENCES `m_group` (`id`),
	CONSTRAINT `FK_grouprole_m_codevalue` FOREIGN KEY (`role_cv_id`) REFERENCES `m_code_value` (`id`)
)
COLLATE='utf8_general_ci'
ENGINE=InnoDB;