CREATE TABLE `m_entity_relation` (
	`id` BIGINT(20) NOT NULL AUTO_INCREMENT,
	`from_entity_type` INT(10) NOT NULL,
	`to_entity_type` INT(10) NOT NULL,
	`code_name` VARCHAR(50) NOT NULL,
	PRIMARY KEY (`id`),
	UNIQUE INDEX `from_entity_type_to_entity_type_code_name` (`from_entity_type`, `to_entity_type`, `code_name`)
)
COLLATE='utf8_general_ci'
ENGINE=InnoDB
AUTO_INCREMENT=1
;

CREATE TABLE `m_entity_to_entity_mapping` (
	`id` BIGINT(20) NOT NULL AUTO_INCREMENT,
	`rel_id` BIGINT(20) NOT NULL DEFAULT '0',
	`from_id` BIGINT(20) NOT NULL DEFAULT '0',
	`to_id` BIGINT(20) UNSIGNED NOT NULL,
	`start_date` DATE NULL DEFAULT NULL,
	`end_date` DATE NULL DEFAULT NULL,
	PRIMARY KEY (`id`),
	UNIQUE INDEX `rel_id_from_id_to_id` (`rel_id`, `from_id`, `to_id`),
	CONSTRAINT `FK__rel_id_m_entity_relation_id` FOREIGN KEY (`rel_id`) REFERENCES `m_entity_relation` (`id`)
)
COLLATE='utf8_general_ci'
ENGINE=InnoDB
AUTO_INCREMENT=1
;

INSERT INTO `m_entity_relation` (`from_entity_type`, `to_entity_type`, `code_name`) VALUES (1, 2, 'office_access_to_loan_products');
INSERT INTO `m_entity_relation` (`from_entity_type`, `to_entity_type`, `code_name`) VALUES (1, 3, 'office_access_to_savings_products');
INSERT INTO `m_entity_relation` (`from_entity_type`, `to_entity_type`, `code_name`) VALUES (1, 4, 'office_access_to_fees/charges');
INSERT INTO `m_entity_relation` (`from_entity_type`, `to_entity_type`, `code_name`) VALUES (5, 2, 'role_access_to_loan_products');
INSERT INTO `m_entity_relation` (`from_entity_type`, `to_entity_type`, `code_name`) VALUES (5, 3, 'role_access_to_savings_products');

INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ('infrastructure', 'CREATE_ENTITYMAPPING', 'ENTITYMAPPING', 'CREATE', 0);
INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ('infrastructure', 'UPDATE_ENTITYMAPPING', 'ENTITYMAPPING', 'UPDATE', 0);
INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ('infrastructure', 'DELETE_ENTITYMAPPING', 'ENTITYMAPPING', 'DELETE', 0);