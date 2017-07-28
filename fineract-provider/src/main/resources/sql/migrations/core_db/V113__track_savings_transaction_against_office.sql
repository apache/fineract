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

ALTER TABLE `m_savings_account_transaction`
    ADD COLUMN `office_id` BIGINT(20) NULL DEFAULT NULL AFTER `savings_account_id`;

/**update client savings**/
UPDATE m_savings_account_transaction st set st.office_id = (SELECT c.office_id AS officeId FROM m_savings_account sa JOIN m_client c on sa.client_id=c.id where sa.id=st.savings_account_id) where st.savings_account_id in (Select sa.id from m_savings_account sa where sa.client_id is not null);

/**update group savings**/
UPDATE m_savings_account_transaction st set st.office_id = (SELECT g.office_id AS officeId FROM m_savings_account sa JOIN m_group g on sa.group_id=g.id where sa.id=st.savings_account_id) where st.savings_account_id in (Select sa.id from m_savings_account sa where sa.group_id is not null);

/**Add foreign key constraints**/
ALTER TABLE `m_savings_account_transaction`
    CHANGE COLUMN `office_id` `office_id` BIGINT(20) NOT NULL AFTER `savings_account_id`;

ALTER TABLE `m_savings_account_transaction`
    ADD CONSTRAINT `FK_m_savings_account_transaction_m_office` FOREIGN KEY (`office_id`) REFERENCES `m_office` (`id`);