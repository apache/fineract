CREATE TABLE `c_account_number_format` (
	`id` BIGINT(20) NOT NULL AUTO_INCREMENT,
	`account_type_enum` SMALLINT(1) NOT NULL,
	`prefix_type_enum` SMALLINT(2) NULL DEFAULT NULL,
	PRIMARY KEY (`id`),
	UNIQUE INDEX `account_type_enum` (`account_type_enum`)
)
COLLATE='utf8_general_ci'
ENGINE=InnoDB
;


/*permissions*/

insert into `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`)
VALUES ('configuration', 'CREATE_ACCOUNTNUMBERFORMAT', 'ACCOUNTNUMBERFORMAT', 'CREATE', 0);
insert into `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`)
VALUES ('configuration', 'READ_ACCOUNTNUMBERFORMAT', 'ACCOUNTNUMBERFORMAT', 'READ', 0);
insert into `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`)
VALUES ('configuration', 'UPDATE_ACCOUNTNUMBERFORMAT', 'ACCOUNTNUMBERFORMAT', 'UPDATE', 0);
insert into `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`)
VALUES ('configuration', 'DELETE_ACCOUNTNUMBERFORMAT', 'HOOK', 'DELETE', 0);