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

CREATE TABLE `m_product_loan_variations_borrower_cycle` (
	`id` BIGINT(20) NOT NULL AUTO_INCREMENT,
	`loan_product_id` BIGINT(20) NOT NULL DEFAULT '0',
	`borrower_cycle_number` INT(3) NOT NULL DEFAULT '0',
	`value_condition` INT(1) NOT NULL DEFAULT '0',
	`param_type` INT(1) NOT NULL DEFAULT '0',
	`default_value` DECIMAL(19,6) NOT NULL DEFAULT '0.000000',
	`max_value` DECIMAL(19,6) NULL DEFAULT NULL,
	`min_value` DECIMAL(19,6) NULL DEFAULT NULL,
	PRIMARY KEY (`id`),
	INDEX `borrower_cycle_loan_product_FK` (`loan_product_id`),
	CONSTRAINT `borrower_cycle_loan_product_FK` FOREIGN KEY (`loan_product_id`) REFERENCES `m_product_loan` (`id`)
)
COLLATE='utf8_general_ci'
ENGINE=InnoDB;


ALTER TABLE `m_product_loan`
	ADD COLUMN `use_borrower_cycle` TINYINT(1) NOT NULL DEFAULT '0' AFTER `include_in_borrower_cycle`;