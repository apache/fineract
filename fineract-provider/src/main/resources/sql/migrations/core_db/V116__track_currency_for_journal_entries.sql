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

/**Add currency code**/
ALTER TABLE `acc_gl_journal_entry`
		ADD COLUMN `currency_code` VARCHAR(3) NULL DEFAULT NULL AFTER `reversal_id`;


/**Update currency codes for loans**/
UPDATE acc_gl_journal_entry journal_entry SET currency_code = (
SELECT m_loan.currency_code
FROM m_loan, m_loan_transaction
WHERE m_loan.id = m_loan_transaction.loan_id AND m_loan_transaction.id=journal_entry.transaction_id)
WHERE journal_entry.entity_type_enum=1;


/**Update currency codes for savings**/
UPDATE acc_gl_journal_entry journal_entry SET currency_code = (
SELECT m_savings_account.currency_code
FROM m_savings_account, m_savings_account_transaction
WHERE m_savings_account.id = m_savings_account_transaction.savings_account_id AND m_savings_account_transaction.id=journal_entry.transaction_id)
WHERE journal_entry.entity_type_enum=2;

/**Update currency codes for manual journal entries***/
update acc_gl_journal_entry set currency_code = (select code from m_organisation_currency limit 1)
where acc_gl_journal_entry.currency_code is NULL;

/**Make currency code not null**/
ALTER TABLE `acc_gl_journal_entry`
	ALTER `currency_code` DROP DEFAULT;
ALTER TABLE `acc_gl_journal_entry`
	CHANGE COLUMN `currency_code` `currency_code` VARCHAR(3) NOT NULL AFTER `reversal_id`;