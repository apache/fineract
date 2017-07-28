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
ADD COLUMN `allow_variabe_installments` BIT(1) NOT NULL DEFAULT 0 AFTER `is_linked_to_floating_interest_rates` ;

CREATE TABLE `m_product_loan_variable_installment_config` (
	`id` BIGINT(20) NOT NULL AUTO_INCREMENT,
	`loan_product_id` BIGINT(20) NOT NULL,
	`minimum_gap` INT(4) NOT NULL,
	`maximum_gap` INT(4) NOT NULL,
	PRIMARY KEY (`id`),
	CONSTRAINT `FK_mappings_m_variable_product_loan_id` FOREIGN KEY (`loan_product_id`) REFERENCES `m_product_loan` (`id`)	
);

INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ('portfolio', 'CREATESCHEDULEEXCEPTIONS_LOAN', 'LOAN', 'CREATESCHEDULEEXCEPTIONS', 0), ('portfolio', 'CREATESCHEDULEEXCEPTIONS_LOAN_CHECKER', 'LOAN', 'CREATESCHEDULEEXCEPTIONS_CHECKER', 0), ('portfolio', 'DELETESCHEDULEEXCEPTIONS_LOAN', 'LOAN', 'DELETESCHEDULEEXCEPTIONS', 0),('portfolio', 'DELETESCHEDULEEXCEPTIONS_LOAN_CHECKER', 'LOAN', 'DELETESCHEDULEEXCEPTIONS_CHECKER', 0);


ALTER TABLE `m_loan_term_variations`
	ALTER `applicable_from` DROP DEFAULT,
	ALTER `term_value` DROP DEFAULT;
ALTER TABLE `m_loan_term_variations`
	CHANGE COLUMN `applicable_from` `applicable_date` DATE NOT NULL AFTER `term_type`,
	CHANGE COLUMN `term_value` `decimal_value` DECIMAL(19,6) NULL AFTER `applicable_date`,
	ADD COLUMN `date_value` DATE NULL AFTER `decimal_value`,
	ADD COLUMN `is_specific_to_installment` TINYINT NOT NULL DEFAULT '0' AFTER `date_value`,
	ADD COLUMN `applied_on_loan_status` SMALLINT(5) NOT NULL AFTER `is_specific_to_installment`;
	
UPDATE `m_loan_term_variations` SET `applied_on_loan_status`=300;