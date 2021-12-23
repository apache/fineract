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


ALTER TABLE `m_charge` ADD COLUMN `is_free_withdrawal` INT NOT NULL DEFAULT '0' AFTER `fee_frequency`;


ALTER TABLE `m_charge`
    ADD COLUMN `free_withdrawal_charge_frequency` INT NULL DEFAULT '0' AFTER `is_free_withdrawal`;


ALTER TABLE `m_charge`
    ADD COLUMN `restart_frequency` INT NULL DEFAULT '0' AFTER `free_withdrawal_charge_frequency`;


ALTER TABLE `m_charge`
    ADD COLUMN `restart_frequency_enum` INT NULL DEFAULT '0' AFTER `restart_frequency`;


ALTER TABLE `m_savings_account_charge`
    ADD COLUMN `free_withdrawal_count` INT NULL DEFAULT '0' AFTER `fee_interval`;

ALTER TABLE `m_savings_account_charge`
    ADD COLUMN `charge_reset_date` DATE NULL DEFAULT NULL AFTER `free_withdrawal_count`;
