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

CREATE TABLE IF NOT EXISTS `m_rate` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(250) NOT NULL,
  `percentage` decimal(10,2) NOT NULL,
  `active` tinyint(1) DEFAULT '0',
  `product_apply` smallint(5) NOT NULL,
  `created_date` datetime NULL DEFAULT NULL,
  `createdby_id` bigint(20) NOT NULL,
  `lastmodifiedby_id` bigint(20) NOT NULL,
  `lastmodified_date` datetime NULL DEFAULT NULL,
  `approve_user` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_M_RATE_CREATE_USER` (`createdby_id`),
  KEY `FK_M_RATE_APPROVE_USER` (`approve_user`),
  CONSTRAINT `FK_M_RATE_APPROVE_USER` FOREIGN KEY (`approve_user`) REFERENCES `m_appuser` (`id`),
  CONSTRAINT `FK_M_RATE_CREATE_USER` FOREIGN KEY (`createdby_id`) REFERENCES `m_appuser` (`id`)
);


CREATE TABLE IF NOT EXISTS `m_loan_rate` (
  `loan_id` bigint(20) NOT NULL,
  `rate_id` bigint(20) NOT NULL,
  PRIMARY KEY (`loan_id`,`rate_id`),
  KEY `FK_M_LOAN_RATE_RATE` (`rate_id`),
  CONSTRAINT `FK_M_LOAN_RATE_LOAN` FOREIGN KEY (`loan_id`) REFERENCES `m_loan` (`id`),
  CONSTRAINT `FK_M_LOAN_RATE_RATE` FOREIGN KEY (`rate_id`) REFERENCES `m_rate` (`id`)
);


CREATE TABLE IF NOT EXISTS `m_product_loan_rate` (
  `product_loan_id` bigint(20) NOT NULL,
  `rate_id` bigint(20) NOT NULL,
  PRIMARY KEY (`product_loan_id`,`rate_id`),
  KEY `FK_M_PRODUCT_LOAN_RATE_RATE` (`rate_id`),
  CONSTRAINT `FK_M_PRODUCT_LOAN_RATE_LOAN` FOREIGN KEY (`product_loan_id`) REFERENCES `m_product_loan` (`id`),
  CONSTRAINT `FK_M_PRODUCT_LOAN_RATE_RATE` FOREIGN KEY (`rate_id`) REFERENCES `m_rate` (`id`)
);


INSERT INTO `m_permission`
(`grouping`,`code`,`entity_name`,`action_name`,`can_maker_checker`) VALUES
  ('organisation', 'READ_RATE', 'RATE', 'CREATE', '1'),
  ('organisation', 'CREATE_RATE', 'RATE', 'CREATE', '1'),
  ('organisation', 'UPDATE_RATE', 'RATE', 'UPDATE', '1');

INSERT INTO `c_configuration`
(`name`, `value`, `enabled`, `is_trap_door`, `description`) VALUES
('sub-rates', 0, 0, 0, 'Enable Rates Module');
