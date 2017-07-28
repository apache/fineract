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

CREATE TABLE IF NOT EXISTS `m_interest_rate_chart` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(100) DEFAULT NULL,
  `description` varchar(200) DEFAULT NULL,
  `from_date` date NOT NULL,
  `end_date` date DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


CREATE TABLE IF NOT EXISTS `m_interest_rate_slab` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `interest_rate_chart_id` bigint(20) NOT NULL,
  `description` varchar(200) DEFAULT NULL,
  `period_type_enum` smallint(5) NOT NULL DEFAULT '1',
  `from_period` int(11) NOT NULL DEFAULT '0',
  `to_period` int(11) DEFAULT NULL,
  `amount_range_from` decimal(19,6) DEFAULT NULL,
  `amount_range_to` decimal(19,6) DEFAULT NULL,
  `annual_interest_rate` decimal(19,6) NOT NULL,
  `currency_code` varchar(3) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKIRS00000000000001` (`interest_rate_chart_id`),
  CONSTRAINT `FKIRS00000000000001` FOREIGN KEY (`interest_rate_chart_id`) REFERENCES `m_interest_rate_chart` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `m_deposit_product_interest_rate_chart` (
  `deposit_product_id` bigint(20) NOT NULL,
  `interest_rate_chart_id` bigint(20) NOT NULL,
  UNIQUE KEY `deposit_product_id_interest_rate_chart_id` (`deposit_product_id`,`interest_rate_chart_id`),
  KEY `FKDPIRC00000000000002` (`interest_rate_chart_id`),
  CONSTRAINT `FKDPIRC00000000000001` FOREIGN KEY (`deposit_product_id`) REFERENCES `m_savings_product` (`id`),
  CONSTRAINT `FKDPIRC00000000000002` FOREIGN KEY (`interest_rate_chart_id`) REFERENCES `m_interest_rate_chart` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


CREATE TABLE IF NOT EXISTS `m_deposit_product_term_and_preclosure` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `savings_product_id` bigint(20) NOT NULL DEFAULT '0',
  `min_deposit_term` int(11) DEFAULT NULL,
  `max_deposit_term` int(11) DEFAULT NULL,
  `min_deposit_term_type_enum` smallint(5) DEFAULT NULL,
  `max_deposit_term_type_enum` smallint(5) DEFAULT NULL,
  `in_multiples_of_deposit_term` int(11) DEFAULT NULL,
  `in_multiples_of_deposit_term_type_enum` smallint(5) DEFAULT NULL,
  `interest_free_period_applicable` smallint(5) DEFAULT NULL,
  `interest_free_from_period` int(11) DEFAULT NULL,
  `interest_free_to_period` int(11) DEFAULT NULL,
  `interest_free_period_frequency_enum` smallint(5) DEFAULT NULL,
  `pre_closure_penal_applicable` smallint(5) DEFAULT NULL,
  `pre_closure_penal_interest` decimal(19,6) DEFAULT NULL,
  `pre_closure_penal_interest_on_enum` smallint(5) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKDPTP00000000000001` (`savings_product_id`),
  CONSTRAINT `FKDPTP00000000000001` FOREIGN KEY (`savings_product_id`) REFERENCES `m_savings_product` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


CREATE TABLE IF NOT EXISTS `m_deposit_product_recurring_detail` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `savings_product_id` bigint(20) NOT NULL DEFAULT '0',
  `recurring_deposit_type_enum` smallint(5) DEFAULT NULL,
  `recurring_deposit_frequency` int(11) DEFAULT NULL,
  `recurring_deposit_frequency_type_enum` smallint(5) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKDPRD00000000000001` (`savings_product_id`),
  CONSTRAINT `FKDPRD00000000000001` FOREIGN KEY (`savings_product_id`) REFERENCES `m_savings_product` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `m_savings_account_interest_rate_chart` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `savings_account_id` bigint(20) NOT NULL,
  `name` varchar(100) DEFAULT NULL,
  `description` varchar(200) DEFAULT NULL,
  `from_date` date NOT NULL,
  `end_date` date DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKSAIRC00000000000001` (`savings_account_id`),
  CONSTRAINT `FKSAIRC00000000000001` FOREIGN KEY (`savings_account_id`) REFERENCES `m_savings_account` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `m_savings_account_interest_rate_slab` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `savings_account_interest_rate_chart_id` bigint(20) NOT NULL,
  `description` varchar(200) DEFAULT NULL,
  `period_type_enum` smallint(5) NOT NULL DEFAULT '1',
  `from_period` int(11) NOT NULL DEFAULT '0',
  `to_period` int(11) DEFAULT NULL,
  `amount_range_from` decimal(19,6) DEFAULT NULL,
  `amount_range_to` decimal(19,6) DEFAULT NULL,
  `annual_interest_rate` decimal(19,6) NOT NULL,
  `currency_code` varchar(3) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKSAIRS00000000000001` (`savings_account_interest_rate_chart_id`),
  CONSTRAINT `FKSAIRS00000000000001` FOREIGN KEY (`savings_account_interest_rate_chart_id`) REFERENCES `m_savings_account_interest_rate_chart` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `m_deposit_account_recurring_detail` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `savings_account_id` bigint(20) NOT NULL DEFAULT '0',
  `recurring_deposit_amount` decimal(19,6) DEFAULT NULL,
  `recurring_deposit_type_enum` smallint(5) DEFAULT NULL,
  `recurring_deposit_frequency` int(11) DEFAULT NULL,
  `recurring_deposit_frequency_type_enum` smallint(5) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKDARD00000000000001` (`savings_account_id`),
  CONSTRAINT `FKDARD00000000000001` FOREIGN KEY (`savings_account_id`) REFERENCES `m_savings_account` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `m_deposit_account_term_and_preclosure` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `savings_account_id` bigint(20) NOT NULL DEFAULT '0',
  `min_deposit_term` int(11) DEFAULT NULL,
  `max_deposit_term` int(11) DEFAULT NULL,
  `min_deposit_term_type_enum` smallint(5) DEFAULT NULL,
  `max_deposit_term_type_enum` smallint(5) DEFAULT NULL,
  `in_multiples_of_deposit_term` int(11) DEFAULT NULL,
  `in_multiples_of_deposit_term_type_enum` smallint(5) DEFAULT NULL,
  `interest_free_period_applicable` smallint(5) DEFAULT NULL,
  `interest_free_from_period` int(11) DEFAULT NULL,
  `interest_free_to_period` int(11) DEFAULT NULL,
  `interest_free_period_frequency_enum` smallint(5) DEFAULT NULL,
  `pre_closure_penal_applicable` smallint(5) DEFAULT NULL,
  `pre_closure_penal_interest` decimal(19,6) DEFAULT NULL,
  `pre_closure_penal_interest_on_enum` smallint(5) DEFAULT NULL,
  `deposit_period` int(11) DEFAULT NULL,
  `deposit_period_frequency_enum` smallint(5) DEFAULT NULL,
  `deposit_amount` decimal(19,6) DEFAULT NULL,
  `maturity_amount` decimal(19,6) DEFAULT NULL,
  `maturity_date` date DEFAULT NULL,
  `on_account_closure_enum` smallint(5) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKDATP00000000000001` (`savings_account_id`),
  CONSTRAINT `FKDATP00000000000001` FOREIGN KEY (`savings_account_id`) REFERENCES `m_savings_account` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


ALTER TABLE `m_savings_product`
	ADD COLUMN `deposit_type_enum` SMALLINT(5) NOT NULL DEFAULT '100' AFTER `description`;

ALTER TABLE `m_savings_account`
	ADD COLUMN `deposit_type_enum` SMALLINT(5) NOT NULL DEFAULT '100' AFTER `account_type_enum`;

ALTER TABLE `m_client`
  ADD COLUMN `gender_cv_id` INT(11) NULL DEFAULT NULL AFTER `mobile_no`,
  ADD COLUMN `date_of_birth` DATE NULL DEFAULT NULL AFTER `gender_cv_id`;


INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ( 'portfolio', 'CREATE_INTERESTRATECHART', 'INTERESTRATECHART', 'CREATE', 0);
INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ( 'portfolio', 'CREATE_INTERESTRATECHART_CHECKER', 'INTERESTRATECHART', 'CREATE', 0);
INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ( 'portfolio', 'UPDATE_INTERESTRATECHART', 'INTERESTRATECHART', 'UPDATE', 0);
INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ( 'portfolio', 'DELETE_INTERESTRATECHART', 'INTERESTRATECHART', 'DELETE', 0);
INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ( 'portfolio', 'UPDATE_INTERESTRATECHART_CHECKER', 'INTERESTRATECHART', 'UPDATE', 0);
INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ( 'portfolio', 'DELETE_INTERESTRATECHART_CHECKER', 'INTERESTRATECHART', 'DELETE', 0);
INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ( 'portfolio', 'CREATE_CHARTSLAB', 'CHARTSLAB', 'CREATE', 0);
INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ( 'portfolio', 'CREATE_CHARTSLAB_CHECKER', 'CHARTSLAB', 'CREATE', 0);
INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ( 'portfolio', 'UPDATE_CHARTSLAB', 'CHARTSLAB', 'UPDATE', 0);
INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ( 'portfolio', 'DELETE_CHARTSLAB', 'CHARTSLAB', 'DELETE', 0);
INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ( 'portfolio', 'UPDATE_CHARTSLAB_CHECKER', 'CHARTSLAB', 'UPDATE', 0);
INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ( 'portfolio', 'DELETE_CHARTSLAB_CHECKER', 'CHARTSLAB', 'DELETE', 0);
INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ( 'portfolio', 'CREATE_FIXEDDEPOSITPRODUCT', 'FIXEDDEPOSITPRODUCT', 'CREATE', 0);
INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ( 'portfolio', 'CREATE_FIXEDDEPOSITPRODUCT_CHECKER', 'FIXEDDEPOSITPRODUCT', 'CREATE', 0);
INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ( 'portfolio', 'UPDATE_FIXEDDEPOSITPRODUCT', 'FIXEDDEPOSITPRODUCT', 'UPDATE', 0);
INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ( 'portfolio', 'DELETE_FIXEDDEPOSITPRODUCT', 'FIXEDDEPOSITPRODUCT', 'DELETE', 0);
INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ( 'portfolio', 'UPDATE_FIXEDDEPOSITPRODUCT_CHECKER', 'FIXEDDEPOSITPRODUCT', 'UPDATE', 0);
INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ( 'portfolio', 'DELETE_FIXEDDEPOSITPRODUCT_CHECKER', 'FIXEDDEPOSITPRODUCT', 'DELETE', 0);
INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ( 'portfolio', 'CREATE_RECURRINGDEPOSITPRODUCT', 'RECURRINGDEPOSITPRODUCT', 'CREATE', 0);
INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ( 'portfolio', 'CREATE_RECURRINGDEPOSITPRODUCT_CHECKER', 'RECURRINGDEPOSITPRODUCT', 'CREATE', 0);
INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ( 'portfolio', 'UPDATE_RECURRINGDEPOSITPRODUCT', 'RECURRINGDEPOSITPRODUCT', 'UPDATE', 0);
INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ( 'portfolio', 'DELETE_RECURRINGDEPOSITPRODUCT', 'RECURRINGDEPOSITPRODUCT', 'DELETE', 0);
INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ( 'portfolio', 'UPDATE_RECURRINGDEPOSITPRODUCT_CHECKER', 'RECURRINGDEPOSITPRODUCT', 'UPDATE', 0);
INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ( 'portfolio', 'DELETE_RECURRINGDEPOSITPRODUCT_CHECKER', 'RECURRINGDEPOSITPRODUCT', 'DELETE', 0);

INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ( 'portfolio', 'READ_FIXEDDEPOSITACCOUNT', 'FIXEDDEPOSITACCOUNT', 'READ', 0);
INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ( 'portfolio', 'CREATE_FIXEDDEPOSITACCOUNT', 'FIXEDDEPOSITACCOUNT', 'CREATE', 0);
INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ( 'portfolio', 'CREATE_FIXEDDEPOSITACCOUNT_CHECKER', 'FIXEDDEPOSITACCOUNT', 'CREATE', 0);
INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ( 'portfolio', 'UPDATE_FIXEDDEPOSITACCOUNT', 'FIXEDDEPOSITACCOUNT', 'UPDATE', 0);
INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ( 'portfolio', 'UPDATE_FIXEDDEPOSITACCOUNT_CHECKER', 'FIXEDDEPOSITACCOUNT', 'UPDATE', 0);
INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ( 'portfolio', 'DELETE_FIXEDDEPOSITACCOUNT', 'FIXEDDEPOSITACCOUNT', 'DELETE', 0);
INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ( 'portfolio', 'DELETE_FIXEDDEPOSITACCOUNT_CHECKER', 'FIXEDDEPOSITACCOUNT', 'DELETE', 0);
INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ( 'transaction_savings', 'DEPOSIT_FIXEDDEPOSITACCOUNT', 'FIXEDDEPOSITACCOUNT', 'DEPOSIT', 0);
INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ( 'transaction_savings', 'DEPOSIT_FIXEDDEPOSITACCOUNT_CHECKER', 'FIXEDDEPOSITACCOUNT', 'DEPOSIT', 0);
INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ( 'transaction_savings', 'WITHDRAWAL_FIXEDDEPOSITACCOUNT', 'FIXEDDEPOSITACCOUNT', 'WITHDRAWAL', 0);
INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ( 'transaction_savings', 'WITHDRAWAL_FIXEDDEPOSITACCOUNT_CHECKER', 'FIXEDDEPOSITACCOUNT', 'WITHDRAWAL', 0);
INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ( 'transaction_savings', 'ACTIVATE_FIXEDDEPOSITACCOUNT', 'FIXEDDEPOSITACCOUNT', 'ACTIVATE', 0);
INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ( 'transaction_savings', 'ACTIVATE_FIXEDDEPOSITACCOUNT_CHECKER', 'FIXEDDEPOSITACCOUNT', 'ACTIVATE', 0);
INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ( 'transaction_savings', 'CALCULATEINTEREST_FIXEDDEPOSITACCOUNT', 'FIXEDDEPOSITACCOUNT', 'CALCULATEINTEREST', 0);
INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ( 'transaction_savings', 'CALCULATEINTEREST_FIXEDDEPOSITACCOUNT_CHECKER', 'FIXEDDEPOSITACCOUNT', 'CALCULATEINTEREST', 0);
INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ( 'transaction_savings', 'POSTINTEREST_FIXEDDEPOSITACCOUNT', 'FIXEDDEPOSITACCOUNT', 'POSTINTEREST', 1);
INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ( 'transaction_savings', 'POSTINTEREST_FIXEDDEPOSITACCOUNT_CHECKER', 'FIXEDDEPOSITACCOUNT', 'POSTINTEREST', 0);
INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ( 'transaction_savings', 'APPROVE_FIXEDDEPOSITACCOUNT', 'FIXEDDEPOSITACCOUNT', 'APPROVE', 1);
INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ( 'transaction_savings', 'REJECT_FIXEDDEPOSITACCOUNT', 'FIXEDDEPOSITACCOUNT', 'REJECT', 1);
INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ( 'transaction_savings', 'WITHDRAW_FIXEDDEPOSITACCOUNT', 'FIXEDDEPOSITACCOUNT', 'WITHDRAW', 1);
INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ( 'transaction_savings', 'APPROVALUNDO_FIXEDDEPOSITACCOUNT', 'FIXEDDEPOSITACCOUNT', 'APPROVALUNDO', 1);
INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ( 'transaction_savings', 'CLOSE_FIXEDDEPOSITACCOUNT', 'FIXEDDEPOSITACCOUNT', 'CLOSE', 1);
INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ( 'transaction_savings', 'APPROVE_FIXEDDEPOSITACCOUNT_CHECKER', 'FIXEDDEPOSITACCOUNT', 'APPROVE', 0);
INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ( 'transaction_savings', 'REJECT_FIXEDDEPOSITACCOUNT_CHECKER', 'FIXEDDEPOSITACCOUNT', 'REJECT', 0);
INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ( 'transaction_savings', 'WITHDRAW_FIXEDDEPOSITACCOUNT_CHECKER', 'FIXEDDEPOSITACCOUNT', 'WITHDRAW', 0);
INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ( 'transaction_savings', 'APPROVALUNDO_FIXEDDEPOSITACCOUNT_CHECKER', 'FIXEDDEPOSITACCOUNT', 'APPROVALUNDO', 0);
INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ( 'transaction_savings', 'CLOSE_FIXEDDEPOSITACCOUNT_CHECKER', 'FIXEDDEPOSITACCOUNT', 'CLOSE', 0);
INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ( 'transaction_savings', 'UNDOTRANSACTION_FIXEDDEPOSITACCOUNT', 'FIXEDDEPOSITACCOUNT', 'UNDOTRANSACTION', 1);
INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ( 'transaction_savings', 'UNDOTRANSACTION_FIXEDDEPOSITACCOUNT_CHECKER', 'FIXEDDEPOSITACCOUNT', 'UNDOTRANSACTION', 0);
INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ( 'transaction_savings', 'ADJUSTTRANSACTION_FIXEDDEPOSITACCOUNT', 'FIXEDDEPOSITACCOUNT', 'ADJUSTTRANSACTION', 0);
INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ( 'portfolio', 'READ_RECURRINGDEPOSITACCOUNT', 'RECURRINGDEPOSITACCOUNT', 'READ', 0);
INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ( 'portfolio', 'CREATE_RECURRINGDEPOSITACCOUNT', 'RECURRINGDEPOSITACCOUNT', 'CREATE', 0);
INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ( 'portfolio', 'CREATE_RECURRINGDEPOSITACCOUNT_CHECKER', 'RECURRINGDEPOSITACCOUNT', 'CREATE', 0);
INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ( 'portfolio', 'UPDATE_RECURRINGDEPOSITACCOUNT', 'RECURRINGDEPOSITACCOUNT', 'UPDATE', 0);
INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ( 'portfolio', 'UPDATE_RECURRINGDEPOSITACCOUNT_CHECKER', 'RECURRINGDEPOSITACCOUNT', 'UPDATE', 0);
INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ( 'portfolio', 'DELETE_RECURRINGDEPOSITACCOUNT', 'RECURRINGDEPOSITACCOUNT', 'DELETE', 0);
INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ( 'portfolio', 'DELETE_RECURRINGDEPOSITACCOUNT_CHECKER', 'RECURRINGDEPOSITACCOUNT', 'DELETE', 0);
INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ( 'transaction_savings', 'DEPOSIT_RECURRINGDEPOSITACCOUNT', 'RECURRINGDEPOSITACCOUNT', 'DEPOSIT', 0);
INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ( 'transaction_savings', 'DEPOSIT_RECURRINGDEPOSITACCOUNT_CHECKER', 'RECURRINGDEPOSITACCOUNT', 'DEPOSIT', 0);
INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ( 'transaction_savings', 'WITHDRAWAL_RECURRINGDEPOSITACCOUNT', 'RECURRINGDEPOSITACCOUNT', 'WITHDRAWAL', 0);
INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ( 'transaction_savings', 'WITHDRAWAL_RECURRINGDEPOSITACCOUNT_CHECKER', 'RECURRINGDEPOSITACCOUNT', 'WITHDRAWAL', 0);
INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ( 'transaction_savings', 'ACTIVATE_RECURRINGDEPOSITACCOUNT', 'RECURRINGDEPOSITACCOUNT', 'ACTIVATE', 0);
INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ( 'transaction_savings', 'ACTIVATE_RECURRINGDEPOSITACCOUNT_CHECKER', 'RECURRINGDEPOSITACCOUNT', 'ACTIVATE', 0);
INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ( 'transaction_savings', 'CALCULATEINTEREST_RECURRINGDEPOSITACCOUNT', 'RECURRINGDEPOSITACCOUNT', 'CALCULATEINTEREST', 0);
INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ( 'transaction_savings', 'CALCULATEINTEREST_RECURRINGDEPOSITACCOUNT_CHECKER', 'RECURRINGDEPOSITACCOUNT', 'CALCULATEINTEREST', 0);
INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ( 'transaction_savings', 'POSTINTEREST_RECURRINGDEPOSITACCOUNT', 'RECURRINGDEPOSITACCOUNT', 'POSTINTEREST', 1);
INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ( 'transaction_savings', 'POSTINTEREST_RECURRINGDEPOSITACCOUNT_CHECKER', 'RECURRINGDEPOSITACCOUNT', 'POSTINTEREST', 0);
INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ( 'transaction_savings', 'APPROVE_RECURRINGDEPOSITACCOUNT', 'RECURRINGDEPOSITACCOUNT', 'APPROVE', 1);
INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ( 'transaction_savings', 'REJECT_RECURRINGDEPOSITACCOUNT', 'RECURRINGDEPOSITACCOUNT', 'REJECT', 1);
INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ( 'transaction_savings', 'WITHDRAW_RECURRINGDEPOSITACCOUNT', 'RECURRINGDEPOSITACCOUNT', 'WITHDRAW', 1);
INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ( 'transaction_savings', 'APPROVALUNDO_RECURRINGDEPOSITACCOUNT', 'RECURRINGDEPOSITACCOUNT', 'APPROVALUNDO', 1);
INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ( 'transaction_savings', 'CLOSE_RECURRINGDEPOSITACCOUNT', 'RECURRINGDEPOSITACCOUNT', 'CLOSE', 1);
INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ( 'transaction_savings', 'APPROVE_RECURRINGDEPOSITACCOUNT_CHECKER', 'RECURRINGDEPOSITACCOUNT', 'APPROVE', 0);
INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ( 'transaction_savings', 'REJECT_RECURRINGDEPOSITACCOUNT_CHECKER', 'RECURRINGDEPOSITACCOUNT', 'REJECT', 0);
INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ( 'transaction_savings', 'WITHDRAW_RECURRINGDEPOSITACCOUNT_CHECKER', 'RECURRINGDEPOSITACCOUNT', 'WITHDRAW', 0);
INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ( 'transaction_savings', 'APPROVALUNDO_RECURRINGDEPOSITACCOUNT_CHECKER', 'RECURRINGDEPOSITACCOUNT', 'APPROVALUNDO', 0);
INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ( 'transaction_savings', 'CLOSE_RECURRINGDEPOSITACCOUNT_CHECKER', 'RECURRINGDEPOSITACCOUNT', 'CLOSE', 0);
INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ( 'transaction_savings', 'UNDOTRANSACTION_RECURRINGDEPOSITACCOUNT', 'RECURRINGDEPOSITACCOUNT', 'UNDOTRANSACTION', 1);
INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ( 'transaction_savings', 'UNDOTRANSACTION_RECURRINGDEPOSITACCOUNT_CHECKER', 'RECURRINGDEPOSITACCOUNT', 'UNDOTRANSACTION', 0);
INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ( 'transaction_savings', 'ADJUSTTRANSACTION_RECURRINGDEPOSITACCOUNT', 'RECURRINGDEPOSITACCOUNT', 'ADJUSTTRANSACTION', 0);
INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ('transaction_savings', 'PREMATURECLOSE_FIXEDDEPOSITACCOUNT_CHECKER', 'FIXEDDEPOSITACCOUNT', 'PREMATURECLOSE', 0);
INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`)                      VALUES ('transaction_savings', 'PREMATURECLOSE_FIXEDDEPOSITACCOUNT', 'FIXEDDEPOSITACCOUNT', 'PREMATURECLOSE');
INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ('transaction_savings', 'PREMATURECLOSE_RECURRINGDEPOSITACCOUNT_CHECKER', 'RECURRINGDEPOSITACCOUNT', 'PREMATURECLOSE', 0);
INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`)                      VALUES ('transaction_savings', 'PREMATURECLOSE_RECURRINGDEPOSITACCOUNT', 'RECURRINGDEPOSITACCOUNT', 'PREMATURECLOSE');
