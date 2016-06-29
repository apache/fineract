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
	ADD COLUMN `currency_multiplesof` SMALLINT(5) NULL AFTER `currency_digits`;
ALTER TABLE `m_savings_product`
	ADD COLUMN `currency_multiplesof` SMALLINT(5) NULL AFTER `currency_digits`;
ALTER TABLE `m_loan`
	ADD COLUMN `currency_multiplesof` SMALLINT(5) NULL AFTER `currency_digits`;
ALTER TABLE `m_product_loan`
	ADD COLUMN `currency_multiplesof` SMALLINT(5) NULL AFTER `currency_digits`;
ALTER TABLE `m_currency`
	ADD COLUMN `currency_multiplesof` SMALLINT(5) NULL AFTER `decimal_places`;
ALTER TABLE `m_organisation_currency`
	ADD COLUMN `currency_multiplesof` SMALLINT(5) NULL AFTER `decimal_places`;