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

ALTER TABLE `m_client`
	ADD COLUMN `client_type_cv_id` INT(11) NULL DEFAULT NULL AFTER `default_savings_account`,
	ADD COLUMN `client_classification_cv_id` INT(11) NULL DEFAULT NULL AFTER `client_type_cv_id`,
	ADD CONSTRAINT `FK_m_client_type_m_code_value` FOREIGN KEY (`client_type_cv_id`) REFERENCES `m_code_value` (`id`),
	ADD CONSTRAINT `FK_m_client_classification_m_code_value` FOREIGN KEY (`client_classification_cv_id`) REFERENCES `m_code_value` (`id`),
	ADD CONSTRAINT `FK1_m_client_gender_m_code_value` FOREIGN KEY (`gender_cv_id`) REFERENCES `m_code_value` (`id`);

INSERT INTO `m_code` (`code_name`, `is_system_defined`) VALUES ('ClientType', 1);
INSERT INTO `m_code` (`code_name`, `is_system_defined`) VALUES ('ClientClassification', 1);


CREATE TABLE `m_interest_incentives` (
	`id` BIGINT(20) NOT NULL AUTO_INCREMENT,
	`interest_rate_slab_id` BIGINT(20) NOT NULL,
	`entiry_type` SMALLINT(2) NOT NULL,
	`attribute_name` SMALLINT(2) NOT NULL,
	`condition_type` SMALLINT(2) NOT NULL,
	`attribute_value` VARCHAR(50) NOT NULL,
	`incentive_type` SMALLINT(2) NOT NULL,
	`amount` DECIMAL(19,6) NOT NULL,
	PRIMARY KEY (`id`),
	INDEX `FK_m_interest_incentives_m_interest_rate_slab` (`interest_rate_slab_id`),
	CONSTRAINT `FK_m_interest_incentives_m_interest_rate_slab` FOREIGN KEY (`interest_rate_slab_id`) REFERENCES `m_interest_rate_slab` (`id`)
);


CREATE TABLE `m_savings_interest_incentives` (
	`id` BIGINT(20) NOT NULL AUTO_INCREMENT,
	`deposit_account_interest_rate_slab_id` BIGINT(20) NOT NULL,
	`entiry_type` SMALLINT(2) NOT NULL,
	`attribute_name` SMALLINT(2) NOT NULL,
	`condition_type` SMALLINT(2) NOT NULL,
	`attribute_value` VARCHAR(50) NOT NULL,
	`incentive_type` SMALLINT(2) NOT NULL,
	`amount` DECIMAL(19,6) NOT NULL,
	PRIMARY KEY (`id`),
	INDEX `FK_m_savings_interest_incentives_m_savings_interest_rate_slab` (`deposit_account_interest_rate_slab_id`),
	CONSTRAINT `FK_m_savings_interest_incentives_m_savings_interest_rate_slab` FOREIGN KEY (`deposit_account_interest_rate_slab_id`) REFERENCES `m_savings_account_interest_rate_slab` (`id`)
);

ALTER TABLE `m_interest_rate_slab`
	DROP COLUMN `interest_rate_for_female`,
	DROP COLUMN `interest_rate_for_children`,
	DROP COLUMN `interest_rate_for_senior_citizen`;

ALTER TABLE `m_savings_account_interest_rate_slab`
	DROP COLUMN `interest_rate_for_female`,
	DROP COLUMN `interest_rate_for_children`,
	DROP COLUMN `interest_rate_for_senior_citizen`;