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

CREATE TABLE `m_floating_rates` (
	`id` BIGINT(20) NOT NULL AUTO_INCREMENT,
	`name` VARCHAR(200) NOT NULL,
	`is_base_lending_rate` BIT(1) NOT NULL DEFAULT 0,
	`is_active` BIT(1) NOT NULL DEFAULT 1,
	`createdby_id` BIGINT(20) NOT NULL,
	`created_date` DATETIME NOT NULL,
	`lastmodifiedby_id` BIGINT(20) NOT NULL,
	`lastmodified_date` DATETIME NOT NULL,
	PRIMARY KEY (`id`),
	UNIQUE INDEX `unq_name` (`name`)
);

CREATE TABLE `m_floating_rates_periods` (
	`id` BIGINT(20) NOT NULL AUTO_INCREMENT,
	`floating_rates_id` BIGINT(20) NOT NULL,
	`from_date` DATETIME NOT NULL,
	`interest_rate` DECIMAL(19,6) NOT NULL,
	`is_differential_to_base_lending_rate` BIT(1) NOT NULL DEFAULT 0,
	`is_active` BIT(1) NOT NULL DEFAULT 1,
	`createdby_id` BIGINT(20) NOT NULL,
	`created_date` DATETIME NOT NULL,
	`lastmodifiedby_id` BIGINT(20) NOT NULL,
	`lastmodified_date` DATETIME NOT NULL,
	PRIMARY KEY (`id`),
	CONSTRAINT `FK_mappings_m_floating_rates` FOREIGN KEY (`floating_rates_id`) REFERENCES `m_floating_rates` (`id`)
);

ALTER TABLE `m_product_loan`
ADD COLUMN `is_linked_to_floating_interest_rates` BIT(1) NOT NULL DEFAULT 0 AFTER `fund_id`,
MODIFY COLUMN `nominal_interest_rate_per_period` DECIMAL(19,6) NULL DEFAULT NULL,
MODIFY COLUMN `interest_period_frequency_enum` SMALLINT(5) NULL DEFAULT NULL,
MODIFY COLUMN `annual_nominal_interest_rate` DECIMAL(19,6) NULL DEFAULT NULL;

CREATE TABLE `m_product_loan_floating_rates` (
	`id` BIGINT(20) NOT NULL AUTO_INCREMENT,
	`loan_product_id` BIGINT(20) NOT NULL,
	`floating_rates_id` BIGINT(20) NOT NULL,
	`interest_rate_differential` DECIMAL(19,6) NOT NULL DEFAULT 0,
	`min_differential_lending_rate` DECIMAL(19,6) NOT NULL DEFAULT 0,
	`default_differential_lending_rate` DECIMAL(19,6) NOT NULL DEFAULT 0,
	`max_differential_lending_rate` DECIMAL(19,6) NOT NULL DEFAULT 0,
	`is_floating_interest_rate_calculation_allowed` BIT(1) NOT NULL DEFAULT 0,
	PRIMARY KEY (`id`),
	CONSTRAINT `FK_mappings_m_product_loan_id` FOREIGN KEY (`loan_product_id`) REFERENCES `m_product_loan` (`id`),
	CONSTRAINT `FK_mappings_m_floating_rates_id` FOREIGN KEY (`floating_rates_id`) REFERENCES `m_floating_rates` (`id`)
);

ALTER TABLE `m_loan`
ADD COLUMN `is_floating_interest_rate` BIT(1) NULL DEFAULT 0 AFTER `arrearstolerance_amount`,
ADD COLUMN `interest_rate_differential` DECIMAL(19,6) NULL DEFAULT 0 AFTER `is_floating_interest_rate`,
MODIFY COLUMN `nominal_interest_rate_per_period` DECIMAL(19,6) NULL DEFAULT NULL,
MODIFY COLUMN `interest_period_frequency_enum` SMALLINT(5) NULL DEFAULT NULL,
MODIFY COLUMN `annual_nominal_interest_rate` DECIMAL(19,6) NULL DEFAULT NULL;

INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) 
VALUES ('portfolio', 'READ_FLOATINGRATE', 'FLOATINGRATE', 'READ', 0),
	('portfolio', 'CREATE_FLOATINGRATE', 'FLOATINGRATE', 'CREATE', 1),
	('portfolio', 'CREATE_FLOATINGRATE_CHECKER', 'FLOATINGRATE', 'CREATE_CHECKER', 0), 
	('portfolio', 'UPDATE_FLOATINGRATE', 'FLOATINGRATE', 'UPDATE', 1),
	('portfolio', 'UPDATE_FLOATINGRATE_CHECKER', 'FLOATINGRATE', 'UPDATE_CHECKER', 0);
