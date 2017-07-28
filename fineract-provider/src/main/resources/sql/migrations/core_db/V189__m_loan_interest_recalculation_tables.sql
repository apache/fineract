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
	ADD COLUMN `days_in_month_enum` SMALLINT(5) NOT NULL DEFAULT '1' AFTER `overdue_days_for_npa`,
	ADD COLUMN `days_in_year_enum` SMALLINT(5) NOT NULL DEFAULT '1' AFTER `days_in_month_enum`,
	ADD COLUMN `interest_recalculation_enabled` TINYINT NOT NULL DEFAULT '0' AFTER `days_in_year_enum`;

ALTER TABLE `m_loan`
	ADD COLUMN `days_in_month_enum` SMALLINT(5) NOT NULL DEFAULT '1' AFTER `accrued_till`,
	ADD COLUMN `days_in_year_enum` SMALLINT(5) NOT NULL DEFAULT '1' AFTER `days_in_month_enum`,
	ADD COLUMN `interest_recalculation_enabled` TINYINT NOT NULL DEFAULT '0' AFTER `days_in_year_enum`;

CREATE TABLE `m_product_loan_recalculation_details` (
	`id` BIGINT(20) NOT NULL AUTO_INCREMENT,
	`product_id` BIGINT(20) NOT NULL,
	`compound_type_enum` SMALLINT(5) NOT NULL,
	`reschedule_strategy_enum` SMALLINT(5) NOT NULL,
	PRIMARY KEY (`id`),
	CONSTRAINT `FK_m_product_loan_m_product_loan_recalculation_details` FOREIGN KEY (`product_id`) REFERENCES `m_product_loan` (`id`)
)
COLLATE='latin1_swedish_ci'
ENGINE=InnoDB;

CREATE TABLE `m_loan_recalculation_details` (
	`id` BIGINT(20) NOT NULL AUTO_INCREMENT,
	`loan_id` BIGINT(20) NOT NULL,
	`compound_type_enum` SMALLINT(5) NOT NULL,
	`reschedule_strategy_enum` SMALLINT(5) NOT NULL,
	PRIMARY KEY (`id`),
	CONSTRAINT `FK_m_loan_m_loan_recalculation_details` FOREIGN KEY (`loan_id`) REFERENCES `m_loan` (`id`)
)
COLLATE='latin1_swedish_ci'
ENGINE=InnoDB;
