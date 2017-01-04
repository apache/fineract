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
DROP COLUMN `nominal_interest_rate_period_frequency_enum`,
CHANGE COLUMN `nominal_interest_rate_per_period` `nominal_annual_interest_rate` DECIMAL(19,6) NOT NULL,
CHANGE COLUMN `interest_period_enum` `interest_compounding_period_enum` SMALLINT(5) NOT NULL;


ALTER TABLE `m_savings_account`
DROP COLUMN `annual_nominal_interest_rate`,
DROP COLUMN `nominal_interest_rate_period_frequency_enum`,
CHANGE COLUMN `nominal_interest_rate_per_period` `nominal_annual_interest_rate` DECIMAL(19,6) NOT NULL,
CHANGE COLUMN `interest_period_enum` `interest_compounding_period_enum` SMALLINT(5) NOT NULL;