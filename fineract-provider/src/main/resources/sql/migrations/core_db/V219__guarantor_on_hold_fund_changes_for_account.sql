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

ALTER TABLE `m_portfolio_account_associations`
	ADD COLUMN `is_active` TINYINT(1) NOT NULL DEFAULT '1';
	
ALTER TABLE `m_guarantor`
	ADD COLUMN `is_active` TINYINT(1) NOT NULL DEFAULT '1';
	
CREATE TABLE `m_guarantor_funding_details` (
	`id` BIGINT(20) NOT NULL AUTO_INCREMENT,
	`guarantor_id` BIGINT(20) NOT NULL,
	`account_associations_id` BIGINT(20) NOT NULL,
	`amount` DECIMAL(19,6) NOT NULL,
	`amount_released_derived` DECIMAL(19,6) NULL,
	`amount_remaining_derived` DECIMAL(19,6) NULL,
	`amount_transfered_derived` DECIMAL(19,6) NULL,
	`status_enum` SMALLINT(3) NOT NULL,
	PRIMARY KEY (`id`),
	CONSTRAINT `FK_m_guarantor_fund_details_m_guarantor` FOREIGN KEY (`guarantor_id`) REFERENCES `m_guarantor` (`id`),
	CONSTRAINT `FK_m_guarantor_fund_details_account_associations_id` FOREIGN KEY (`account_associations_id`) REFERENCES `m_portfolio_account_associations` (`id`)
);

CREATE TABLE `m_deposit_account_on_hold_transaction` (
	`id` BIGINT(20) NOT NULL AUTO_INCREMENT,
	`savings_account_id` BIGINT(20) NOT NULL,
	`amount` DECIMAL(19,6) NOT NULL,
	`transaction_type_enum` SMALLINT(1) NOT NULL,
	`transaction_date` DATE NOT NULL,
	`is_reversed` TINYINT(1) NOT NULL DEFAULT '0',
	PRIMARY KEY (`id`),
	CONSTRAINT `FK_deposit_on_hold_transaction_m_savings_account` FOREIGN KEY (`savings_account_id`) REFERENCES `m_savings_account` (`id`)
);

CREATE TABLE `m_guarantor_transaction` (
	`id` BIGINT(20) NOT NULL AUTO_INCREMENT,
	`guarantor_fund_detail_id` BIGINT(20) NOT NULL,
	`loan_transaction_id` BIGINT(20) NOT NULL,
	`deposit_on_hold_transaction_id` BIGINT(20) NOT NULL,
	`is_reversed` TINYINT(1) NOT NULL DEFAULT '0',
	PRIMARY KEY (`id`),
	CONSTRAINT `FK_guarantor_transaction_m_deposit_account_on_hold_transaction` FOREIGN KEY (`deposit_on_hold_transaction_id`) REFERENCES `m_deposit_account_on_hold_transaction` (`id`),
	CONSTRAINT `FK_guarantor_transaction_guarantor_fund_detail` FOREIGN KEY (`guarantor_fund_detail_id`) REFERENCES `m_guarantor_funding_details` (`id`),
	CONSTRAINT `FK_guarantor_transaction_m_loan_transaction` FOREIGN KEY (`loan_transaction_id`) REFERENCES `m_loan_transaction` (`id`)
);

ALTER TABLE `m_loan`
	ADD COLUMN `guarantee_amount_derived` DECIMAL(19,6) NULL DEFAULT NULL AFTER `interest_recalculation_enabled`,
	ADD COLUMN `guarantee_outstanding_amount_derived` DECIMAL(19,6) NULL DEFAULT NULL AFTER `guarantee_amount_derived`;

