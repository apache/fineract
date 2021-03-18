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

INSERT INTO `m_permission`(`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`)
    VALUES ("portfolio","BULK_RESCHEDULE_RESCHEDULELOAN","RESCHEDULELOAN","BULK_RESCHEDULE",0);

ALTER TABLE `m_loan_reschedule_request`
    ADD `change_schedule` BOOLEAN DEFAULT FALSE AFTER `reschedule_reason_comment`,
    ADD `repay_every` SMALLINT DEFAULT NULL AFTER `change_schedule`,
    ADD `repayment_period_frequency_enum` SMALLINT DEFAULT NULL AFTER `repay_every`,
    ADD `start_date_semi_month` date DEFAULT NULL AFTER `repayment_period_frequency_enum`,
    ADD `close_date_semi_month` date DEFAULT NULL AFTER `start_date_semi_month`;
