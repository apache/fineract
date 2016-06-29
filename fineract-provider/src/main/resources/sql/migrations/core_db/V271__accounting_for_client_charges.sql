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

-- Charges have a reference to an Income or Liability account that gets credited when a payment is made
ALTER TABLE `m_charge`
	ADD COLUMN `income_or_liability_account_id` BIGINT(20) NULL AFTER `fee_frequency`,
	ADD CONSTRAINT `FK_m_charge_acc_gl_account` FOREIGN KEY (`income_or_liability_account_id`) REFERENCES `acc_gl_account` (`id`);

--Journal entries also refer to Client transactions
ALTER TABLE `acc_gl_journal_entry`
	ADD COLUMN `client_transaction_id` BIGINT(20) NULL DEFAULT NULL AFTER `savings_transaction_id`,
	ADD CONSTRAINT `FK_acc_gl_journal_entry_m_client_transaction` FOREIGN KEY (`client_transaction_id`) REFERENCES `m_client_transaction` (`id`);
