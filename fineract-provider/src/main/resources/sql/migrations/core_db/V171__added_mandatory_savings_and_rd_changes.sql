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

INSERT INTO `c_configuration` (`name`, `value`, `enabled`) VALUES ('age_limit_for_senior_citizen', 65, 1);
INSERT INTO `c_configuration` (`name`, `value`, `enabled`) VALUES ('age_limit_for_children', 15, 1);


ALTER TABLE `m_interest_rate_slab`
	ADD COLUMN `interest_rate_for_female` DECIMAL(19,6) NULL AFTER `annual_interest_rate`,
	ADD COLUMN `interest_rate_for_children` DECIMAL(19,6) NULL AFTER `interest_rate_for_female`,
	ADD COLUMN `interest_rate_for_senior_citizen` DECIMAL(19,6) NULL AFTER `interest_rate_for_children`;

ALTER TABLE `m_savings_account_interest_rate_slab`
	ADD COLUMN `interest_rate_for_female` DECIMAL(19,6) NULL AFTER `annual_interest_rate`,
	ADD COLUMN `interest_rate_for_children` DECIMAL(19,6) NULL AFTER `interest_rate_for_female`,
	ADD COLUMN `interest_rate_for_senior_citizen` DECIMAL(19,6) NULL AFTER `interest_rate_for_children`;

ALTER TABLE `m_deposit_account_recurring_detail`
	CHANGE COLUMN `recurring_deposit_amount` `mandatory_recommended_deposit_amount` DECIMAL(19,6) NULL DEFAULT NULL,
	ADD COLUMN `is_mandatory` TINYINT NOT NULL DEFAULT '0',
	ADD COLUMN `allow_withdrawal` TINYINT NOT NULL DEFAULT '0',
	ADD COLUMN `adjust_advance_towards_future_payments` TINYINT NOT NULL DEFAULT '1',
	ADD COLUMN `is_calendar_inherited` TINYINT NOT NULL DEFAULT '0',
	ADD COLUMN `total_overdue_amount` DECIMAL(19,6) NULL DEFAULT NULL,
	ADD COLUMN `no_of_overdue_installments` INT NULL DEFAULT NULL,
	DROP COLUMN `recurring_deposit_type_enum`,
	DROP COLUMN `recurring_deposit_frequency`,
	DROP COLUMN `recurring_deposit_frequency_type_enum`;


ALTER TABLE `m_deposit_product_term_and_preclosure`
	DROP COLUMN `interest_free_period_applicable`,
	DROP COLUMN `interest_free_from_period`,
	DROP COLUMN `interest_free_to_period`,
	DROP COLUMN `interest_free_period_frequency_enum`;

ALTER TABLE `m_deposit_product_recurring_detail`
	ADD COLUMN `is_mandatory` tinyint NOT NULL DEFAULT '1',
	ADD COLUMN `allow_withdrawal` tinyint NOT NULL DEFAULT '0',
	ADD COLUMN `adjust_advance_towards_future_payments` tinyint NOT NULL DEFAULT '1',
	DROP COLUMN `recurring_deposit_type_enum`,
	DROP COLUMN `recurring_deposit_frequency`,
	DROP COLUMN `recurring_deposit_frequency_type_enum`;

ALTER TABLE `m_deposit_account_term_and_preclosure`
	DROP COLUMN `interest_free_period_applicable`,
	DROP COLUMN `interest_free_from_period`,
	DROP COLUMN `interest_free_to_period`,
	DROP COLUMN `interest_free_period_frequency_enum`;

CREATE TABLE `m_mandatory_savings_schedule` (
	`id` BIGINT NOT NULL AUTO_INCREMENT,
	`savings_account_id` BIGINT NOT NULL,
	`fromdate` DATE NULL DEFAULT NULL,
	`duedate` DATE NOT NULL,
	`installment` SMALLINT NOT NULL,
	`deposit_amount` DECIMAL(19,6) NULL DEFAULT NULL,
	`deposit_amount_completed_derived` DECIMAL(19,6) NULL DEFAULT NULL,
	`total_paid_in_advance_derived` DECIMAL(19,6) NULL DEFAULT NULL,
	`total_paid_late_derived` DECIMAL(19,6) NULL DEFAULT NULL,
	`completed_derived` BIT(1) NOT NULL,
	`obligations_met_on_date` DATE NULL DEFAULT NULL,
	`createdby_id` BIGINT NULL DEFAULT NULL,
	`created_date` DATETIME NULL DEFAULT NULL,
	`lastmodified_date` DATETIME NULL DEFAULT NULL,
	`lastmodifiedby_id` BIGINT NULL DEFAULT NULL,
	PRIMARY KEY (`id`),
	INDEX `FKMSS0000000001` (`savings_account_id`),
	CONSTRAINT `FKMSS0000000001` FOREIGN KEY (`savings_account_id`) REFERENCES `m_savings_account` (`id`)
);


INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ('organisation', 'READ_RECURRINGDEPOSITPRODUCT', 'RECURRINGDEPOSITPRODUCT', 'READ', 0);
INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ('organisation', 'READ_FIXEDDEPOSITPRODUCT', 'FIXEDDEPOSITPRODUCT', 'READ', 0);