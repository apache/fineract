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

ALTER TABLE `acc_gl_journal_entry`
	ADD COLUMN `loan_transaction_id` BIGINT(20) NULL AFTER `transaction_id`,
	ADD COLUMN `savings_transaction_id` BIGINT(20) NULL AFTER `loan_transaction_id`,
	ADD CONSTRAINT `FK_acc_gl_journal_entry_m_loan_transaction` FOREIGN KEY (`loan_transaction_id`) REFERENCES `m_loan_transaction` (`id`),
	ADD CONSTRAINT `FK_acc_gl_journal_entry_m_savings_account_transaction` FOREIGN KEY (`savings_transaction_id`) REFERENCES `m_savings_account_transaction` (`id`);

UPDATE acc_gl_journal_entry je SET je.savings_transaction_id=je.transaction_id,je.transaction_id=Concat('S',je.transaction_id) WHERE  je.entity_type_enum=2;

UPDATE acc_gl_journal_entry je SET je.loan_transaction_id=je.transaction_id,je.transaction_id=Concat('L',je.transaction_id) WHERE  je.entity_type_enum=1;
