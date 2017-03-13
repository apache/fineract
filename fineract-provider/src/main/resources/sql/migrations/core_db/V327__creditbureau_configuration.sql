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
 
 -- credit bureau tables 
CREATE TABLE `m_creditbureau` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL,
  `product` varchar(100) NOT NULL,
  `country` varchar(100) NOT NULL,
  `implementationKey` varchar(100) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique impl` (`name`,`product`,`country`,`implementationKey`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


CREATE TABLE `m_organisation_creditbureau` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `alias` varchar(50) NOT NULL,
  `creditbureau_id` bigint(20) NOT NULL,
  `is_active` tinyint(4) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `morgcb` (`alias`,`creditbureau_id`),
  KEY `orgcb_cbfk` (`creditbureau_id`),
  CONSTRAINT `orgcb_cbfk` FOREIGN KEY (`creditbureau_id`) REFERENCES `m_creditbureau` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;




CREATE TABLE `m_creditbureau_configuration` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `configkey` varchar(50) DEFAULT NULL,
  `value` varchar(50) DEFAULT NULL,
  `organisation_creditbureau_id` bigint(20) DEFAULT NULL,
  `description` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `mcbconfig` (`configkey`,`organisation_creditbureau_id`),
  KEY `cbConfigfk1` (`organisation_creditbureau_id`),
  CONSTRAINT `cbConfigfk1` FOREIGN KEY (`organisation_creditbureau_id`) REFERENCES `m_organisation_creditbureau` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;





CREATE TABLE `m_creditbureau_loanproduct_mapping` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `organisation_creditbureau_id` bigint(20) NOT NULL,
  `loan_product_id` bigint(20) NOT NULL,
  `is_creditcheck_mandatory` tinyint(1) DEFAULT NULL,
  `skip_creditcheck_in_failure` tinyint(1) DEFAULT NULL,
  `stale_period` int(11) DEFAULT NULL,
  `is_active` tinyint(1) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `cblpunique_key` (`organisation_creditbureau_id`,`loan_product_id`),
  KEY `fk_cb_lp2` (`loan_product_id`),
  CONSTRAINT `cblpfk2` FOREIGN KEY (`organisation_creditbureau_id`) REFERENCES `m_organisation_creditbureau` (`id`),
  CONSTRAINT `fk_cb_lp2` FOREIGN KEY (`loan_product_id`) REFERENCES `m_product_loan` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


--  modify `m_portfolio_command_source` command

ALTER TABLE `m_portfolio_command_source`
	ADD COLUMN `creditbureau_id` BIGINT(20) NULL DEFAULT NULL AFTER `transaction_id`,
	ADD COLUMN `organisation_creditbureau_id` BIGINT(20) NULL DEFAULT NULL AFTER `creditbureau_id`;


-- permissions
INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ('configuration', 'CREATE_CREDITBUREAU_LOANPRODUCT_MAPPING', 'CREDITBUREAU_LOANPRODUCT_MAPPING', 'CREATE', 0);
INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ('configuration', 'CREATE_ORGANISATIONCREDITBUREAU', 'ORGANISATIONCREDITBUREAU', 'CREATE', 0);
INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ('configuration', 'UPDATE_ORGANISATIONCREDITBUREAU', 'ORGANISATIONCREDITBUREAU', 'UPDATE', 0);
INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ('configuration', 'UPDATE_CREDITBUREAU_LOANPRODUCT_MAPPING', 'CREDITBUREAU_LOANPRODUCT_MAPPING', 'UPDATE', 0);
