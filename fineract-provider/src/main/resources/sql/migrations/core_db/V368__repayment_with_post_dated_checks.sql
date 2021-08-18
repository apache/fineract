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

-- create m_repayment_with_post_dated_checks management
CREATE TABLE `m_repayment_with_post_dated_checks` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `check_no` bigint(20) NOT NULL UNIQUE,
  `amount` decimal(20,5) NOT NULL,
  `loan_id` bigint(20) DEFAULT NULL,
  `repayment_id` bigint(20) DEFAULT NULL,
  `account_no` bigint(20) NOT NULL,
  `bank_name` VARCHAR(200) NOT NULL,
  `repayment_date` DATE NOT NULL,
  `is_paid` SMALLINT(1) DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  CONSTRAINT `fkloan` FOREIGN KEY (`loan_id`) REFERENCES `m_loan` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fkrepayment` FOREIGN KEY (`repayment_id`) REFERENCES `m_loan_repayment_schedule` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
);

INSERT INTO `m_permission`
(`grouping`,`code`,`entity_name`,`action_name`,`can_maker_checker`) VALUES
('portfolio', 'UPDATE_REPAYMENT_WITH_POSTDATEDCHECKS', 'REPAYMENT_WITH_POSTDATEDCHECKS', 'UPDATE', '0'),
('portfolio', 'DELETE_REPAYMENT_WITH_POSTDATEDCHECKS', 'REPAYMENT_WITH_POSTDATEDCHECKS', 'DELETE', '0'),
('portfolio', 'BOUNCE_REPAYMENT_WITH_POSTDATEDCHECKS', 'REPAYMENT_WITH_POSTDATEDCHECKS', 'BOUNCE', '0');
