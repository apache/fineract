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

--code inserts
INSERT INTO `m_code` (`id`, `code_name`, `is_system_defined`) VALUES (150, 'INDIAN STATES', 0);
INSERT INTO `m_code` (`id`, `code_name`, `is_system_defined`) VALUES (151, 'COUNTRY ID', 0);
INSERT INTO `m_code` (`id`, `code_name`, `is_system_defined`) VALUES (152, 'ADDRESS TYPE ID', 0);

--add new column in portfolio
alter table `m_portfolio_command_source` add column `status` TINYINT(4) NULL DEFAULT NULL;

--configuration
INSERT INTO `c_configuration` (`id`, `name`, `value`, `date_value`, `enabled`, `is_trap_door`, `description`) VALUES (29, 'Enable-Address', NULL, NULL, 1, 0, NULL);

--add address table
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
COLLATE='utf8_general_ci'
ENGINE=InnoDB
AUTO_INCREMENT=20
;


--entity address table
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
COLLATE='utf8_general_ci'
ENGINE=InnoDB
AUTO_INCREMENT=28
;


--field configuration
CREATE TABLE `m_field_configuration` (
	`id` BIGINT(20) NOT NULL AUTO_INCREMENT,
	`entity` VARCHAR(100) NOT NULL,
	`table` VARCHAR(100) NOT NULL,
	`field` VARCHAR(100) NOT NULL,
	`is_enabled` TINYINT(4) NOT NULL,
	`is_mandatory` TINYINT(4) NOT NULL,
	`validation_regex` VARCHAR(50) NULL DEFAULT NULL,
	PRIMARY KEY (`id`)
)
COLLATE='utf8_general_ci'
ENGINE=InnoDB
AUTO_INCREMENT=19
;


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET NAMES utf8mb4 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
-- Dumping data for table mifostenant-default.m_field_configuration: ~18 rows (approximately)
/*!40000 ALTER TABLE `m_field_configuration` DISABLE KEYS */;
INSERT INTO `m_field_configuration` (`id`, `entity`, `table`, `field`, `is_enabled`, `is_mandatory`, `validation_regex`) VALUES
	(1, 'CLIENT', 'm_address', 'addressType', 1, 0, ''),
	(2, 'CLIENT', 'm_address', 'street', 1, 1, ''),
	(3, 'CLIENT', 'm_address', 'address_line_1', 1, 0, ''),
	(4, 'CLIENT', 'm_address', 'address_line_2', 1, 0, ''),
	(5, 'CLIENT', 'm_address', 'address_line_3', 1, 0, ''),
	(6, 'CLIENT', 'm_address', 'town_village', 0, 0, ''),
	(7, 'CLIENT', 'm_address', 'city', 1, 0, ''),
	(8, 'CLIENT', 'm_address', 'county_district', 0, 0, ''),
	(9, 'CLIENT', 'm_address', 'state_province_id', 1, 0, ''),
	(10, 'CLIENT', 'm_address', 'country_id', 1, 0, ''),
	(11, 'CLIENT', 'm_address', 'postal_code', 1, 0, ''),
	(12, 'CLIENT', 'm_address', 'latitude', 0, 0, ''),
	(13, 'CLIENT', 'm_address', 'longitude', 0, 0, ''),
	(14, 'CLIENT', 'm_address', 'created_by', 1, 0, ''),
	(15, 'CLIENT', 'm_address', 'created_on', 1, 0, ''),
	(16, 'CLIENT', 'm_address', 'updated_by', 1, 0, ''),
	(17, 'CLIENT', 'm_address', 'updated_on', 1, 0, ''),
	(18, 'CLIENT', 'm_address', 'is_active', 1, 0, NULL);
/*!40000 ALTER TABLE `m_field_configuration` ENABLE KEYS */;
/*!40101 SET SQL_MODE=IFNULL(@OLD_SQL_MODE, '') */;
/*!40014 SET FOREIGN_KEY_CHECKS=IF(@OLD_FOREIGN_KEY_CHECKS IS NULL, 1, @OLD_FOREIGN_KEY_CHECKS) */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;