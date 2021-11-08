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

ALTER TABLE `m_account_transfer_details`
ADD COLUMN `to_share_account_id` BIGINT(20) DEFAULT NULL AFTER `from_loan_account_id`;

ALTER TABLE `m_account_transfer_details`
ADD CONSTRAINT `FK_m_share_account_id`
FOREIGN KEY (`to_share_account_id`) REFERENCES `m_share_account` (`id`) ON DELETE CASCADE;

ALTER TABLE `m_account_transfer_transaction`
ADD COLUMN `to_share_transaction_id` BIGINT(20) DEFAULT NULL AFTER `from_loan_transaction_id`;

ALTER TABLE `m_account_transfer_transaction`
ADD CONSTRAINT `FK_m_share_transaction_to_share_account`
FOREIGN KEY (`to_share_transaction_id`) REFERENCES `m_share_account_transactions` (`id`) ON DELETE CASCADE;
