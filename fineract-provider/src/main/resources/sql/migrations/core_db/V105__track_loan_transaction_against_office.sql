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

ALTER TABLE `m_loan_transaction`
    ADD COLUMN `office_id` BIGINT(20) NULL DEFAULT NULL AFTER `loan_id`;

/**update client loans**/
UPDATE m_loan_transaction lt set lt.office_id = (SELECT c.office_id AS officeId FROM m_loan l JOIN m_client c on l.client_id=c.id where l.id=lt.loan_id) where lt.loan_id in (Select l.id from m_loan l where l.client_id is not null);

/**update group loans**/
UPDATE m_loan_transaction lt set lt.office_id = (SELECT g.office_id AS officeId FROM m_loan l JOIN m_group g on l.group_id=g.id where l.id=lt.loan_id) where lt.loan_id in (Select l.id from m_loan l where l.group_id is not null);

/**Add foreign key constraints**/
ALTER TABLE `m_loan_transaction`
    CHANGE COLUMN `office_id` `office_id` BIGINT(20) NOT NULL AFTER `loan_id`;

ALTER TABLE `m_loan_transaction`
    ADD CONSTRAINT `FK_m_loan_transaction_m_office` FOREIGN KEY (`office_id`) REFERENCES `m_office` (`id`);