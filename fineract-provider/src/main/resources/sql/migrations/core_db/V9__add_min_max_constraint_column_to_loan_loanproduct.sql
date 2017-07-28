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

ALTER TABLE `m_product_loan`
	ADD COLUMN `min_nominal_interest_rate_per_period` DECIMAL(19,6) NOT NULL AFTER `nominal_interest_rate_per_period`,
	ADD COLUMN `max_nominal_interest_rate_per_period` DECIMAL(19,6) NOT NULL AFTER `min_nominal_interest_rate_per_period`,
	ADD COLUMN `min_number_of_repayments` SMALLINT(5) NOT NULL AFTER `number_of_repayments`,
	ADD COLUMN `max_number_of_repayments` SMALLINT(5) NOT NULL AFTER `min_number_of_repayments`;

ALTER TABLE `m_loan`
	ADD COLUMN `min_nominal_interest_rate_per_period` DECIMAL(19,6) NOT NULL AFTER `nominal_interest_rate_per_period`,
	ADD COLUMN `max_nominal_interest_rate_per_period` DECIMAL(19,6) NOT NULL AFTER `min_nominal_interest_rate_per_period`,
	ADD COLUMN `min_number_of_repayments` SMALLINT(5) NOT NULL AFTER `number_of_repayments`,
	ADD COLUMN `max_number_of_repayments` SMALLINT(5) NOT NULL AFTER `min_number_of_repayments`;

ALTER TABLE `m_loan`
	ALTER `min_principal_amount` DROP DEFAULT,
	ALTER `max_principal_amount` DROP DEFAULT;
ALTER TABLE `m_loan`
	CHANGE COLUMN `min_principal_amount` `min_principal_amount` DECIMAL(19,6) NULL AFTER `principal_amount`,
	CHANGE COLUMN `max_principal_amount` `max_principal_amount` DECIMAL(19,6) NULL AFTER `min_principal_amount`,
	CHANGE COLUMN `min_nominal_interest_rate_per_period` `min_nominal_interest_rate_per_period` DECIMAL(19,6) NULL AFTER `nominal_interest_rate_per_period`,
	CHANGE COLUMN `max_nominal_interest_rate_per_period` `max_nominal_interest_rate_per_period` DECIMAL(19,6) NULL AFTER `min_nominal_interest_rate_per_period`,
	CHANGE COLUMN `min_number_of_repayments` `min_number_of_repayments` SMALLINT(5) NULL AFTER `number_of_repayments`,
	CHANGE COLUMN `max_number_of_repayments` `max_number_of_repayments` SMALLINT(5) NULL AFTER `min_number_of_repayments`;

ALTER TABLE `m_product_loan`
	ALTER `min_principal_amount` DROP DEFAULT,
	ALTER `max_principal_amount` DROP DEFAULT,
	ALTER `min_nominal_interest_rate_per_period` DROP DEFAULT,
	ALTER `max_nominal_interest_rate_per_period` DROP DEFAULT,
	ALTER `min_number_of_repayments` DROP DEFAULT,
	ALTER `max_number_of_repayments` DROP DEFAULT;
ALTER TABLE `m_product_loan`
	CHANGE COLUMN `min_principal_amount` `min_principal_amount` DECIMAL(19,6) NULL AFTER `principal_amount`,
	CHANGE COLUMN `max_principal_amount` `max_principal_amount` DECIMAL(19,6) NULL AFTER `min_principal_amount`,
	CHANGE COLUMN `min_nominal_interest_rate_per_period` `min_nominal_interest_rate_per_period` DECIMAL(19,6) NULL AFTER `nominal_interest_rate_per_period`,
	CHANGE COLUMN `max_nominal_interest_rate_per_period` `max_nominal_interest_rate_per_period` DECIMAL(19,6) NULL AFTER `min_nominal_interest_rate_per_period`,
	CHANGE COLUMN `min_number_of_repayments` `min_number_of_repayments` SMALLINT(5) NULL AFTER `number_of_repayments`,
	CHANGE COLUMN `max_number_of_repayments` `max_number_of_repayments` SMALLINT(5) NULL AFTER `min_number_of_repayments`;