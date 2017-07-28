--
-- Licensed to the Apache Software Foundation (ASF) under one
-- or more contributor license agreements. See the NOTICE file
-- distributed with this work for additional information
-- regarding copyright ownership. The ASF licenses this file
-- to you under the Apache License, Version 2.0 (the
-- "License"); you may not use this file except in compliance
-- with the License. You may obtain a copy of the License at
--
-- http://www.apache.org/licenses/LICENSE-2.0
--
-- Unless required by applicable law or agreed to in writing,
-- software distributed under the License is distributed on an
-- "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
-- KIND, either express or implied. See the License for the
-- specific language governing permissions and limitations
-- under the License.
--



-- code inserts
INSERT INTO `m_code` (`code_name`, `is_system_defined`) VALUES ('STATE',1);
INSERT INTO `m_code` (`code_name`, `is_system_defined`) VALUES ('COUNTRY',1);
INSERT INTO `m_code` (`code_name`, `is_system_defined`) VALUES ('ADDRESS_TYPE',1);


-- configuration
INSERT INTO `c_configuration` (`name`, `value`, `date_value`, `enabled`, `is_trap_door`, `description`) VALUES ('Enable-Address', NULL, NULL, 0, 0, NULL);

-- add address table
CREATE TABLE `m_address` (
	`id` BIGINT(20) NOT NULL AUTO_INCREMENT,
	`street` VARCHAR(100) NULL DEFAULT NULL,
	`address_line_1` VARCHAR(100) NULL DEFAULT NULL,
	`address_line_2` VARCHAR(100) NULL DEFAULT NULL,
	`address_line_3` VARCHAR(100) NULL DEFAULT NULL,
	`town_village` VARCHAR(100) NULL DEFAULT NULL,
	`city` VARCHAR(100) NULL DEFAULT NULL,
	`county_district` VARCHAR(100) NULL DEFAULT NULL,
	`state_province_id` INT(11) NULL DEFAULT NULL,
	`country_id` INT(11) NULL DEFAULT NULL,
	`postal_code` VARCHAR(10) NULL DEFAULT NULL,
	`latitude` DECIMAL(10,8) UNSIGNED NULL DEFAULT '0.00000000',
	`longitude` DECIMAL(10,8) UNSIGNED NULL DEFAULT '0.00000000',
	`created_by` VARCHAR(100) NULL DEFAULT NULL,
	`created_on` DATE NULL DEFAULT NULL,
	`updated_by` VARCHAR(100) NULL DEFAULT NULL,
	`updated_on` DATE NULL DEFAULT NULL,
	PRIMARY KEY (`id`),
	INDEX `address_fields_codefk1` (`state_province_id`),
	INDEX `address_fields_codefk2` (`country_id`),
	CONSTRAINT `address_fields_codefk1` FOREIGN KEY (`state_province_id`) REFERENCES `m_code_value` (`id`),
	CONSTRAINT `address_fields_codefk2` FOREIGN KEY (`country_id`) REFERENCES `m_code_value` (`id`)
)
ENGINE=InnoDB
AUTO_INCREMENT=1
;


-- entity address table
CREATE TABLE `m_client_address` (
	`id` BIGINT(20) NOT NULL AUTO_INCREMENT,
	`client_id` BIGINT(20) NOT NULL DEFAULT '0',
	`address_id` BIGINT(20) NOT NULL DEFAULT '0',
	`address_type_id` INT(11) NOT NULL DEFAULT '0',
	`is_active` TINYINT(4) NOT NULL DEFAULT '0',
	PRIMARY KEY (`id`),
	INDEX `addressIdFk` (`address_id`),
	INDEX `address_codefk` (`address_type_id`),
	INDEX `clientaddressfk` (`client_id`),
	CONSTRAINT `address_codefk` FOREIGN KEY (`address_type_id`) REFERENCES `m_code_value` (`id`),
	CONSTRAINT `clientaddressfk` FOREIGN KEY (`client_id`) REFERENCES `m_client` (`id`)
)
ENGINE=InnoDB
AUTO_INCREMENT=1
;


-- field configuration
CREATE TABLE `m_field_configuration` (
	`id` BIGINT(20) NOT NULL AUTO_INCREMENT,
	`entity` VARCHAR(100) NOT NULL,
	`subentity` VARCHAR(100) NOT NULL,
	`field` VARCHAR(100) NOT NULL,
	`is_enabled` TINYINT(4) NOT NULL,
	`is_mandatory` TINYINT(4) NOT NULL,
	`validation_regex` VARCHAR(50) NULL DEFAULT NULL,
	PRIMARY KEY (`id`)
)
ENGINE=InnoDB
AUTO_INCREMENT=1
;


INSERT INTO `m_field_configuration` ( `entity`, `subentity`, `field`, `is_enabled`, `is_mandatory`, `validation_regex`) VALUES
	('ADDRESS', 'CLIENT', 'addressType', 1, 0, ''),
	('ADDRESS', 'CLIENT', 'street', 1, 1, ''),
	('ADDRESS', 'CLIENT', 'addressLine1', 1, 0, ''),
	('ADDRESS', 'CLIENT', 'addressLine2', 1, 0, ''),
	('ADDRESS', 'CLIENT', 'addressLine3', 1, 0, ''),
	('ADDRESS', 'CLIENT', 'townVillage', 0, 0, ''),
	('ADDRESS', 'CLIENT', 'city', 1, 0, ''),
	('ADDRESS', 'CLIENT', 'countyDistrict', 0, 0, ''),
	('ADDRESS', 'CLIENT', 'stateProvinceId', 1, 0, ''),
	('ADDRESS', 'CLIENT', 'countryId', 1, 0, ''),
	('ADDRESS', 'CLIENT', 'postalCode', 1, 0, ''),
	('ADDRESS', 'CLIENT', 'latitude', 0, 0, ''),
	('ADDRESS', 'CLIENT', 'longitude', 0, 0, ''),
	('ADDRESS', 'CLIENT', 'createdBy', 1, 0, ''),
	('ADDRESS', 'CLIENT', 'createdOn', 1, 0, ''),
	('ADDRESS', 'CLIENT', 'updatedBy', 1, 0, ''),
	('ADDRESS', 'CLIENT', 'updatedOn', 1, 0, ''),
	('ADDRESS', 'CLIENT', 'isActive', 1, 0, '');

-- inserts for permission
INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ('portfolio', 'CREATE_ADDRESS', 'ADDRESS', 'CREATE', 0);
INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ('portfolio', 'CREATE_ADDRESS_CHECKER', 'ADDRESS', 'CREATE_CHECKER', 1);
INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ('portfolio', 'UPDATE_ADDRESS', 'ADDRESS', 'UPDATE', 0);
INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ('portfolio', 'UPDATE_ADDRESS_CHECKER', 'ADDRESS', 'UPDATE_CHECKER', 1);
INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ('portfolio', 'READ_ADDRESS', 'ADDRESS', 'READ', 0);
INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ('portfolio', 'DELETE_ADDRESS', 'ADDRESS', 'DELETE', 0);
INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ('portfolio', 'DELETE_ADDRESS_CHECKER', 'ADDRESS', 'DELETE_CHECKER', 1);
