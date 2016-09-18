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

ALTER TABLE `m_savings_product`
ADD COLUMN `annual_fee_amount` DECIMAL(19,6) NULL DEFAULT NULL AFTER `withdrawal_fee_type_enum`,
ADD COLUMN `annual_fee_on_month` SMALLINT(5) NULL DEFAULT NULL AFTER `annual_fee_amount`,
ADD COLUMN `annual_fee_on_day` SMALLINT(5) NULL DEFAULT NULL AFTER `annual_fee_on_month`;

ALTER TABLE `m_savings_account`
ADD COLUMN `annual_fee_amount` DECIMAL(19,6) NULL DEFAULT NULL AFTER `withdrawal_fee_type_enum`,
ADD COLUMN `annual_fee_on_month` SMALLINT(5) NULL DEFAULT NULL AFTER `annual_fee_amount`,
ADD COLUMN `annual_fee_on_day` SMALLINT(5) NULL DEFAULT NULL AFTER `annual_fee_on_month`,
ADD COLUMN `annual_fee_next_due_date` DATE NULL DEFAULT NULL AFTER `annual_fee_on_day`;

ALTER TABLE `m_savings_account`
ADD COLUMN `total_annual_fees_derived` DECIMAL(19,6) NULL DEFAULT NULL AFTER `total_withdrawal_fees_derived`;