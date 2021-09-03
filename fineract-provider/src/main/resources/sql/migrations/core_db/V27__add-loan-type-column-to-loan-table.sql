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

ALTER TABLE `m_loan`
    ADD COLUMN `loan_type_enum` SMALLINT NOT NULL AFTER `loan_status_id`;

-- create collateral management table
CREATE TABLE IF NOT EXISTS `m_collateral_management` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(50) NOT NULL,
  `quality` varchar(40) NOT NULL,
  `base_price` decimal(20,5) NOT NULL,
  `unit_type` varchar(10) NOT NULL,
  `pct_to_base` decimal(20,5) NOT NULL,
  `currency` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  KEY `fkCurrencyCode` (`currency`),
  CONSTRAINT `fkCurrencyCode` FOREIGN KEY (`currency`) REFERENCES `m_currency` (`id`) ON DELETE CASCADE
);

-- create client collateral management
CREATE TABLE IF NOT EXISTS `m_client_collateral_management` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `quantity` decimal(20,5) NOT NULL,
  `client_id` bigint(20) DEFAULT NULL,
  `collateral_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  KEY `fkClient` (`client_id`),
  KEY `fkCollateral` (`collateral_id`),
  CONSTRAINT `fkClient` FOREIGN KEY (`client_id`) REFERENCES `m_client` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fkCollateral` FOREIGN KEY (`collateral_id`) REFERENCES `m_collateral_management` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
);


-- create loan collateral management
CREATE TABLE IF NOT EXISTS `m_loan_collateral_management` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `quantity` decimal(20,5) NOT NULL,
  `loan_id` bigint(20) DEFAULT NULL,
  `client_collateral_id` bigint(20) DEFAULT NULL,
  `is_released` tinyint(4) DEFAULT '0',
  `transaction_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  KEY `fkLoan` (`loan_id`),
  KEY `fkTransaction` (`transaction_id`),
  KEY `fkClientCollateral` (`client_collateral_id`),
  CONSTRAINT `fkClientCollateral` FOREIGN KEY (`client_collateral_id`) REFERENCES `m_client_collateral_management` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fkLoan` FOREIGN KEY (`loan_id`) REFERENCES `m_loan` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fkTransaction` FOREIGN KEY (`transaction_id`) REFERENCES `m_loan_transaction` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
);
