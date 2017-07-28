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

ALTER TABLE `m_product_loan_recalculation_details`
	ADD COLUMN `rest_frequency_type_enum` SMALLINT(1) NOT NULL AFTER `reschedule_strategy_enum`,
	ADD COLUMN `rest_frequency_interval` SMALLINT(3) NOT NULL DEFAULT '0' AFTER `rest_frequency_type_enum`,
	ADD COLUMN `rest_freqency_date` DATE NULL DEFAULT NULL AFTER `rest_frequency_interval`;

ALTER TABLE `m_loan_recalculation_details`
	ADD COLUMN `rest_frequency_type_enum` SMALLINT(1) NOT NULL AFTER `reschedule_strategy_enum`,
	ADD COLUMN `rest_frequency_interval` SMALLINT(3) NOT NULL DEFAULT '0' AFTER `rest_frequency_type_enum`,
	ADD COLUMN `rest_freqency_date` DATE NULL DEFAULT NULL AFTER `rest_frequency_interval`;