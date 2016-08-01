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

/*add accounting type field to savings product*/
ALTER TABLE `m_savings_product`
	ADD COLUMN `accounting_type` SMALLINT(5) NOT NULL AFTER `lockin_period_frequency_enum`;

/*update existing savings products to have "No" accounting*/
update m_savings_product set accounting_type=1;

/*track payment details for savings transactions*/
ALTER TABLE `m_savings_account_transaction`
ADD COLUMN `payment_detail_id` BIGINT(20) NULL DEFAULT NULL AFTER `savings_account_id`,
ADD CONSTRAINT `FK_m_savings_account_transaction_m_payment_detail` FOREIGN KEY (`payment_detail_id`) REFERENCES `m_payment_detail` (`id`);
