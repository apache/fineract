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

ALTER TABLE `m_savings_account`
	ADD COLUMN `on_hold_funds_derived` DECIMAL(19,6) NULL DEFAULT NULL;
	
ALTER TABLE `m_portfolio_account_associations`
	ADD COLUMN `association_type_enum` SMALLINT(1) NOT NULL DEFAULT '1';

CREATE TABLE `m_product_loan_guarantee_details` (
	`id` BIGINT(20) NOT NULL AUTO_INCREMENT,
	`loan_product_id` BIGINT(20) NOT NULL,
	`mandatory_guarantee` DECIMAL(19,5) NOT NULL,
	`minimum_guarantee_from_own_funds` DECIMAL(19,5) NULL,
	`minimum_guarantee_from_guarantor_funds` DECIMAL(19,5) NULL,
	PRIMARY KEY (`id`),
	CONSTRAINT `FK_guarantee_details_loan_product` FOREIGN KEY (`loan_product_id`) REFERENCES `m_product_loan` (`id`)
);	

ALTER TABLE `m_product_loan`
	ADD COLUMN `hold_guarantee_funds` TINYINT(1) NOT NULL DEFAULT '0';