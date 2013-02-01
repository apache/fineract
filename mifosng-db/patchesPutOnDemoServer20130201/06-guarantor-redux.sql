-- Guarantor table is no longer an external Table
DELETE FROM `x_registered_table` WHERE  `registered_table_name`='m_guarantor_external' LIMIT 1;

-- drop guarantor table
drop table m_guarantor_external;

-- New m_guarantor table
CREATE TABLE `m_guarantor` (
	`id` BIGINT(20) NOT NULL AUTO_INCREMENT,
	`loan_id` BIGINT(20) NOT NULL,
	`type_enum` SMALLINT(5) NOT NULL,
	`entity_id` BIGINT(20) NULL DEFAULT NULL,
	`firstname` VARCHAR(50) NULL DEFAULT NULL,
	`lastname` VARCHAR(50) NULL DEFAULT NULL,
	`dob` DATE NULL DEFAULT NULL,
	`address_line_1` VARCHAR(500) NULL DEFAULT NULL,
	`address_line_2` VARCHAR(500) NULL DEFAULT NULL,
	`city` VARCHAR(50) NULL DEFAULT NULL,
	`state` VARCHAR(50) NULL DEFAULT NULL,
	`country` VARCHAR(50) NULL DEFAULT NULL,
	`zip` VARCHAR(20) NULL DEFAULT NULL,
	`house_phone_number` VARCHAR(20) NULL DEFAULT NULL,
	`mobile_number` VARCHAR(20) NULL DEFAULT NULL,
	`comment` VARCHAR(500) NULL DEFAULT NULL,
	PRIMARY KEY (`id`),
	INDEX `FK_m_guarantor_m_loan` (`loan_id`),
	CONSTRAINT `FK_m_guarantor_m_loan` FOREIGN KEY (`loan_id`) REFERENCES `m_loan` (`id`)
)
ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Update loan table

ALTER TABLE `m_loan`
	DROP FOREIGN KEY `FK_m_loan_guarantor`;

update m_loan
set guarantor_id=null;
	
ALTER TABLE `m_loan`
	ADD CONSTRAINT `FK_m_loan_guarantor` FOREIGN KEY (`guarantor_id`) REFERENCES `m_guarantor` (`id`);
	
-- Insert new permissions
INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ('portfolio', 'READ_GUARANTOR', 'GUARANTOR', 'READ', 0);

INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ('portfolio', 'CREATE_GUARANTOR', 'GUARANTOR', 'CREATE', 1);

INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ('portfolio', 'UPDATE_GUARANTOR', 'GUARANTOR', 'UPDATE', 1);

INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ('portfolio', 'DELETE_GUARANTOR', 'GUARANTOR', 'DELETE', 1);

INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ('portfolio', 'CREATE_GUARANTOR_CHECKER', 'GUARANTOR', 'CREATE', 0);

INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ('portfolio', 'UPDATE_GUARANTOR_CHECKER', 'GUARANTOR', 'UPDATE', 0);

INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ('portfolio', 'DELETE_GUARANTOR_CHECKER', 'GUARANTOR', 'DELETE', 0);


