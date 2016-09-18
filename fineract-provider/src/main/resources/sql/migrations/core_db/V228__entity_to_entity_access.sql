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

-- Code required to store various types of Entity to Entity Access types that Mifos Supports
insert into m_code (code_name, is_system_defined)
values ('Entity to Entity Access Types', 1);

-- Three Code Values required to support:
-- a) Loan Products restricted to specific Offices
-- b) Savings Products restricted to specific Offices
-- c) Fees/Charges restricted to specific Offices
insert into m_code_value (code_id, code_value, order_position)
values (
	(select id from m_code where code_name = 'Entity to Entity Access Types'),
	'Office Access to Loan Products',  0);

insert into m_code_value (code_id, code_value, order_position)
values (
	(select id from m_code where code_name = 'Entity to Entity Access Types'),
	'Office Access to Savings Products',  0);
	
insert into m_code_value (code_id, code_value, order_position)
values (
	(select id from m_code where code_name = 'Entity to Entity Access Types'),
	'Office Access to Fees/Charges',  0);

-- Table where the actual restrictions will be stored
CREATE TABLE `m_entity_to_entity_access` (
	`id` BIGINT(20) NOT NULL AUTO_INCREMENT,
	`entity_type` VARCHAR(50) NOT NULL,
	`entity_id` BIGINT(20) NOT NULL,
	`access_type_code_value_id` INT(11) NOT NULL,
	`second_entity_type` VARCHAR(50) NOT NULL,
	`second_entity_id` BIGINT(20) NOT NULL,
	PRIMARY KEY (`id`),
	INDEX `IDX_OFFICE` (`entity_type`,`entity_id`),
	UNIQUE KEY `id_uniq_m_entity_to_entity_access` (`entity_type`,`entity_id`,`access_type_code_value_id`,`second_entity_type`,`second_entity_id`),
	CONSTRAINT `FK_access_type_code_m_code_value` FOREIGN KEY (`access_type_code_value_id`) REFERENCES `m_code_value` (`id`)
);

-- Global Configurations for Entity access restrictions
insert into c_configuration (name, value, enabled, description)
	values ('office-specific-products-enabled', 0, 0,
	'Whether products and fees should be office specific or not? This property should NOT be changed once Mifos is Live.');

insert into c_configuration (name, value, enabled, description)
	values ('restrict-products-to-user-office', 0, 0,
	'This should be enabled only if, products & fees are office specific (i.e. office-specific-products-enabled is enabled). This property specifies if the products should be auto-restricted to office of the user who created the proudct? Note: This property should NOT be changed once Mifos is Live.');
