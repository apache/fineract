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

CREATE TABLE `m_share_product` (
`id` BIGINT(20) NOT NULL AUTO_INCREMENT,
`name` VARCHAR(200) NOT NULL,
`short_name` VARCHAR(4) NOT NULL,
`external_id` VARCHAR(100) NULL DEFAULT NULL,
`description` VARCHAR(500) NOT NULL,
`start_date` DATETIME NULL DEFAULT NULL,
`end_date` DATETIME NULL DEFAULT NULL,
`currency_code` VARCHAR(3) NOT NULL,
`currency_digits` SMALLINT(5) NOT NULL,
`currency_multiplesof` SMALLINT(5) NULL DEFAULT NULL,
`total_shares` BIGINT(20) NOT NULL,
`issued_shares` BIGINT(20) NULL DEFAULT NULL,
`totalsubscribed_shares` BIGINT(20) NULL DEFAULT NULL,
`unit_price` DECIMAL(10,2) NOT NULL,
`capital_amount` DECIMAL(20,2) NOT NULL,
`minimum_client_shares` BIGINT(20) NULL DEFAULT NULL,
`nominal_client_shares` BIGINT(20) NOT NULL,
`maximum_client_shares` BIGINT(20) NULL DEFAULT NULL,
`minimum_active_period_frequency` DECIMAL(19,6) NULL DEFAULT NULL,
`minimum_active_period_frequency_enum` SMALLINT(5) NULL DEFAULT NULL,
`lockin_period_frequency` DECIMAL(19,6) NULL DEFAULT NULL,
`lockin_period_frequency_enum` SMALLINT(5) NULL DEFAULT NULL,
`allow_dividends_inactive_clients` SMALLINT(1) NULL DEFAULT 0,
`createdby_id` BIGINT(20) NULL DEFAULT NULL,
`created_date` DATETIME NULL DEFAULT NULL,
`lastmodifiedby_id` BIGINT(20) NULL DEFAULT NULL,
`lastmodified_date` DATETIME NULL DEFAULT NULL,
`accounting_type` SMALLINT(2) NOT NULL,
PRIMARY KEY (`id`),
UNIQUE INDEX `name` (`name`),
CONSTRAINT `m_share_product_ibfk_1` FOREIGN KEY (`createdby_id`) REFERENCES `m_appuser` (`id`),
CONSTRAINT `m_share_product_ibfk_2` FOREIGN KEY (`lastmodifiedby_id`) REFERENCES `m_appuser` (`id`)
) ;

CREATE TABLE `m_share_product_market_price` (
`id` BIGINT(20) NOT NULL AUTO_INCREMENT,
`product_id` BIGINT(20) NOT NULL,
`from_date` DATE NULL DEFAULT NULL,
`share_value` DECIMAL(10,2) NOT NULL,
PRIMARY KEY (`id`),
CONSTRAINT `m_share_product_market_price_ibfk_1` FOREIGN KEY (`product_id`) REFERENCES `m_share_product` (`id`)
) ;

CREATE TABLE `m_share_product_charge` (
`product_id` BIGINT(20) NOT NULL,
`charge_id` BIGINT(20) NOT NULL,
PRIMARY KEY (`product_id`, `charge_id`),
CONSTRAINT `m_share_product_charge_ibfk_1` FOREIGN KEY (`charge_id`) REFERENCES `m_charge` (`id`),
CONSTRAINT `m_share_product_charge_ibfk_2` FOREIGN KEY (`product_id`) REFERENCES `m_share_product` (`id`)
) ;

CREATE TABLE `m_share_account` (
`id` BIGINT(20) NOT NULL AUTO_INCREMENT,
`account_no` VARCHAR(50) NOT NULL,
`product_id` BIGINT(20) NOT NULL,
`client_id` BIGINT(20) NOT NULL,
`external_id` VARCHAR(100) NULL DEFAULT NULL,
`status_enum` SMALLINT(5) NOT NULL DEFAULT '300',
`total_approved_shares` BIGINT(20) NULL DEFAULT NULL,
`total_pending_shares` BIGINT(20) NULL DEFAULT NULL,
`submitted_date` DATE NOT NULL,
`submitted_userid` BIGINT(20) NULL DEFAULT NULL,
`approved_date` DATE NULL DEFAULT NULL,
`approved_userid` BIGINT(20) NULL DEFAULT NULL,
`rejected_date` DATE NULL DEFAULT NULL,
`rejected_userid` BIGINT(20) NULL DEFAULT NULL,
`activated_date` DATE NULL DEFAULT NULL,
`activated_userid` BIGINT(20) NULL DEFAULT NULL,
`closed_date` DATE NULL DEFAULT NULL,
`closed_userid` BIGINT(20) NULL DEFAULT NULL,
`currency_code` VARCHAR(3) NOT NULL,
`currency_digits` SMALLINT(5) NOT NULL,
`currency_multiplesof` SMALLINT(5) NULL DEFAULT NULL,
`savings_account_id` BIGINT(20) NOT NULL,
`minimum_active_period_frequency` DECIMAL(19,6) NULL DEFAULT NULL,
`minimum_active_period_frequency_enum` SMALLINT(5) NULL DEFAULT NULL,
`lockin_period_frequency` DECIMAL(19,6) NULL DEFAULT NULL,
`lockin_period_frequency_enum` SMALLINT(5) NULL DEFAULT NULL,
`allow_dividends_inactive_clients` SMALLINT(1) NULL DEFAULT 0,
`created_date` DATETIME NULL DEFAULT NULL,
`lastmodifiedby_id` BIGINT(20) NULL DEFAULT NULL,
`lastmodified_date` DATETIME NULL DEFAULT NULL,
PRIMARY KEY (`id`),
CONSTRAINT `m_share_account_ibfk_1` FOREIGN KEY (`product_id`) REFERENCES `m_share_product` (`id`),
CONSTRAINT `m_share_account_ibfk_2` FOREIGN KEY (`savings_account_id`) REFERENCES `m_savings_account` (`id`),
CONSTRAINT `m_share_account_ibfk_3` FOREIGN KEY (`submitted_userid`) REFERENCES `m_appuser` (`id`),
CONSTRAINT `m_share_account_ibfk_4` FOREIGN KEY (`approved_userid`) REFERENCES `m_appuser` (`id`),
CONSTRAINT `m_share_account_ibfk_5` FOREIGN KEY (`rejected_userid`) REFERENCES `m_appuser` (`id`),
CONSTRAINT `m_share_account_ibfk_6` FOREIGN KEY (`activated_userid`) REFERENCES `m_appuser` (`id`),
CONSTRAINT `m_share_account_ibfk_7` FOREIGN KEY (`closed_userid`) REFERENCES `m_appuser` (`id`),
CONSTRAINT `m_share_account_ibfk_8` FOREIGN KEY (`lastmodifiedby_id`) REFERENCES `m_appuser` (`id`),
CONSTRAINT `m_share_account_ibfk_9` FOREIGN KEY (`client_id`) REFERENCES `m_client` (`id`)
) ;

CREATE TABLE `m_share_account_charge` (
	`id` BIGINT(20) NOT NULL AUTO_INCREMENT,
	`account_id` BIGINT(20) NOT NULL,
	`charge_id` BIGINT(20) NOT NULL,
	`charge_time_enum` SMALLINT(5) NOT NULL,
	`charge_calculation_enum` SMALLINT(5) NOT NULL,
	`charge_payment_mode_enum` SMALLINT(5) NOT NULL DEFAULT '0',
	`calculation_percentage` DECIMAL(19,6) NULL DEFAULT NULL,
	`calculation_on_amount` DECIMAL(19,6) NULL DEFAULT NULL,
	`charge_amount_or_percentage` DECIMAL(19,6) NULL DEFAULT NULL,
	`amount` DECIMAL(19,6) NOT NULL,
	`amount_paid_derived` DECIMAL(19,6) NULL DEFAULT NULL,
	`amount_waived_derived` DECIMAL(19,6) NULL DEFAULT NULL,
	`amount_writtenoff_derived` DECIMAL(19,6) NULL DEFAULT NULL,
	`amount_outstanding_derived` DECIMAL(19,6) NOT NULL DEFAULT '0.000000',
	`is_paid_derived` TINYINT(1) NOT NULL DEFAULT '0',
	`waived` TINYINT(1) NOT NULL DEFAULT '0',
	`min_cap` DECIMAL(19,6) NULL DEFAULT NULL,
	`max_cap` DECIMAL(19,6) NULL DEFAULT NULL,
	`is_active` TINYINT(1) NOT NULL DEFAULT '1',
	PRIMARY KEY (`id`),
	INDEX `charge_id` (`charge_id`),
	INDEX `m_share_account_charge_ibfk_2` (`account_id`),
	CONSTRAINT `m_share_account_charge_ibfk_1` FOREIGN KEY (`charge_id`) REFERENCES `m_charge` (`id`),
	CONSTRAINT `m_share_account_charge_ibfk_2` FOREIGN KEY (`account_id`) REFERENCES `m_share_account` (`id`)
);


CREATE TABLE `m_share_account_transactions` (
`id` BIGINT(20) NOT NULL AUTO_INCREMENT,
`account_id` BIGINT(20) NOT NULL,
`transaction_date` DATE NULL DEFAULT NULL,
`total_shares` BIGINT(20) NULL DEFAULT NULL,
`unit_price` DECIMAL(10,2) NULL DEFAULT NULL,
`amount` DECIMAL(20,2) NULL DEFAULT NULL,
`charge_amount` DECIMAL(20,2) NULL DEFAULT NULL,
`amount_paid` DECIMAL(20,2) NULL DEFAULT NULL,
`status_enum` SMALLINT(5) NOT NULL DEFAULT '300',
`type_enum` SMALLINT(5) NULL DEFAULT NULL,
`is_active` TINYINT(1) NOT NULL DEFAULT '1',
PRIMARY KEY (`id`),
CONSTRAINT `m_share_account_purchased_shares_ibfk_1` FOREIGN KEY (`account_id`) REFERENCES `m_share_account` (`id`)
) ;

CREATE TABLE `m_share_account_charge_paid_by` (
`id` BIGINT(20) NOT NULL AUTO_INCREMENT,
`share_transaction_id` BIGINT(20) NULL DEFAULT NULL,
`charge_transaction_id` BIGINT(20) NULL DEFAULT NULL,
`amount` DECIMAL(20,2) NOT NULL,
PRIMARY KEY (`id`),
CONSTRAINT `m_share_account_transactions_charge_mapping_ibfk1` FOREIGN KEY (`share_transaction_id`) REFERENCES `m_share_account_transactions` (`id`),
CONSTRAINT `m_share_account_transactions_charge_mapping_ibfk2` FOREIGN KEY (`charge_transaction_id`) REFERENCES `m_share_account_charge` (`id`)
) ;

CREATE TABLE `m_share_product_dividend_pay_out` (
	`id` BIGINT(20) NOT NULL AUTO_INCREMENT,
	`product_id` BIGINT(20) NOT NULL,
	`amount` DECIMAL(19,6) NOT NULL,
	`dividend_period_start_date` DATE NOT NULL,
	`dividend_period_end_date` DATE NOT NULL,
	`status` SMALLINT(3) NOT NULL,
	`createdby_id` BIGINT(20) NULL DEFAULT NULL,
	`created_date` DATETIME NULL DEFAULT NULL,
	`lastmodifiedby_id` BIGINT(20) NULL DEFAULT NULL,
	`lastmodified_date` DATETIME NULL DEFAULT NULL,
	PRIMARY KEY (`id`),
	CONSTRAINT `FK_m_share_product_dividend_pay_out_product_id` FOREIGN KEY (`product_id`) REFERENCES `m_share_product` (`id`),
	CONSTRAINT `FK_m_share_product_dividend_pay_out_createdby_id` FOREIGN KEY (`createdby_id`) REFERENCES `m_appuser` (`id`),
	CONSTRAINT `FK_m_share_product_dividend_pay_out_lastmodifiedby_id` FOREIGN KEY (`lastmodifiedby_id`) REFERENCES `m_appuser` (`id`)
);

CREATE TABLE `m_share_account_dividend_details` (
	`id` BIGINT(20) NOT NULL AUTO_INCREMENT,
	`dividend_pay_out_id` BIGINT(20) NOT NULL,
	`account_id` BIGINT(20) NOT NULL,
	`amount` DECIMAL(19,6) NOT NULL,
	`status` SMALLINT(3) NOT NULL,
	`savings_transaction_id` BIGINT(20) NULL,
	PRIMARY KEY (`id`),
	CONSTRAINT `FK_m_share_account_dividend_details_dividend_pay_out_id` FOREIGN KEY (`dividend_pay_out_id`) REFERENCES `m_share_product_dividend_pay_out` (`id`),
	CONSTRAINT `FK_m_share_account_dividend_details_account_id` FOREIGN KEY (`account_id`) REFERENCES `m_share_account` (`id`)
);

ALTER TABLE `acc_gl_journal_entry`
ADD COLUMN `share_transaction_id` BIGINT(20) NULL AFTER `payment_details_id`,
ADD CONSTRAINT `FK_acc_gl_journal_entry_m_share_account_transaction` FOREIGN KEY (`share_transaction_id`) REFERENCES `m_share_account_transactions` (`id`);
	
INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ('SHAREPRODUCT', 'CREATE_DIVIDEND_SHAREPRODUCT', 'SHAREPRODUCT', 'CREATE_DIVIDEND', 0), ('SHAREPRODUCT', 'CREATE_DIVIDEND_SHAREPRODUCT_CHECKER', 'SHAREPRODUCT', 'CREATE_DIVIDEND_CHECKER', 0),('SHAREPRODUCT', 'APPROVE_DIVIDEND_SHAREPRODUCT', 'SHAREPRODUCT', 'APPROVE_DIVIDEND', 0), ('SHAREPRODUCT', 'APPROVE_DIVIDEND_SHAREPRODUCT_CHECKER', 'SHAREPRODUCT', 'APPROVE_DIVIDEND_CHECKER', 0),('SHAREPRODUCT', 'DELETE_DIVIDEND_SHAREPRODUCT', 'SHAREPRODUCT', 'DELETE_DIVIDEND', 0), ('SHAREPRODUCT', 'DELETE_DIVIDEND_SHAREPRODUCT_CHECKER', 'SHAREPRODUCT', 'DELETE_DIVIDEND_CHECKER', 0),('SHAREPRODUCT', 'READ_DIVIDEND_SHAREPRODUCT', 'SHAREPRODUCT', 'READ_DIVIDEND', 0),('SHAREACCOUNT', 'APPROVE_SHAREACCOUNT', 'SHAREACCOUNT', 'APPROVE', 0),
('SHAREACCOUNT', 'ACTIVATE_SHAREACCOUNT', 'SHAREACCOUNT', 'ACTIVATE', 0),
('SHAREACCOUNT', 'UNDOAPPROVAL_SHAREACCOUNT', 'SHAREACCOUNT', 'UNDOAPPROVAL', 0),
('SHAREACCOUNT', 'REJECT_SHAREACCOUNT', 'SHAREACCOUNT', 'REJECT', 0),
('SHAREACCOUNT', 'APPLYADDITIONALSHARES_SHAREACCOUNT', 'SHAREACCOUNT', 'APPLYADDITIONALSHARES', 0),
('SHAREACCOUNT', 'APPROVEADDITIONALSHARES_SHAREACCOUNT', 'SHAREACCOUNT', 'APPROVEADDITIONALSHARES', 0),
('SHAREACCOUNT', 'REJECTADDITIONALSHARES_SHAREACCOUNT', 'SHAREACCOUNT', 'REJECTADDITIONALSHARES', 0),
('SHAREACCOUNT', 'REDEEMSHARES_SHAREACCOUNT', 'SHAREACCOUNT', 'REDEEMSHARES', 0),
('SHAREACCOUNT', 'CLOSE_SHAREACCOUNT', 'SHAREACCOUNT', 'CLOSE', 0)
;

INSERT INTO `job` (`name`, `display_name`, `cron_expression`, `create_time`) VALUES ('Post Dividends For Shares', 'Post Dividends For Shares', '0 0 0 1/1 * ? *', now());


