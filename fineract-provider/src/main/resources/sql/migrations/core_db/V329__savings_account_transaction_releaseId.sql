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
 
 -- permissions 
INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES 
('transaction_savings', 'HOLDAMOUNT_SAVINGSACCOUNT', 'SAVINGSACCOUNT', 'HOLDAMOUNT', 0),
('transaction_savings', 'HOLDAMOUNT_SAVINGSACCOUNT_CHECKER', 'SAVINGSACCOUNT', 'HOLDAMOUNT_CHECKER', 0),
('transaction_savings', 'BLOCKDEBIT_SAVINGSACCOUNT', 'SAVINGSACCOUNT', 'BLOCKDEBIT', 0),
('transaction_savings', 'BLOCKDEBIT_SAVINGSACCOUNT_CHECKER', 'SAVINGSACCOUNT', 'BLOCKDEBIT_CHECKER', 0),
('transaction_savings', 'UNBLOCKDEBIT_SAVINGSACCOUNT', 'SAVINGSACCOUNT', 'UNBLOCKDEBIT', 0),
('transaction_savings', 'UNBLOCKDEBIT_SAVINGSACCOUNT_CHECKER', 'SAVINGSACCOUNT', 'UNBLOCKDEBIT_CHECKER', 0),
('transaction_savings', 'BLOCKCREDIT_SAVINGSACCOUNT', 'SAVINGSACCOUNT', 'BLOCKCREDIT', 0),
('transaction_savings', 'BLOCKCREDIT_SAVINGSACCOUNT_CHECKER', 'SAVINGSACCOUNT', 'BLOCKCREDIT_CHECKER', 0),
('transaction_savings', 'UNBLOCKCREDIT_SAVINGSACCOUNT', 'SAVINGSACCOUNT', 'UNBLOCKCREDIT', 0),
('transaction_savings', 'UNBLOCKCREDIT_SAVINGSACCOUNT_CHECKER', 'SAVINGSACCOUNT', 'UNBLOCKCREDIT_CHECKER', 0),
('transaction_savings', 'BLOCK_SAVINGSACCOUNT', 'SAVINGSACCOUNT', 'BLOCK', 0),
('transaction_savings', 'BLOCK_SAVINGSACCOUNT_CHECKER', 'SAVINGSACCOUNT', 'BLOCK_CHECKER', 0),
('transaction_savings', 'UNBLOCK_SAVINGSACCOUNT', 'SAVINGSACCOUNT', 'UNBLOCK', 0),
('transaction_savings', 'UNBLOCK_SAVINGSACCOUNT_CHECKER', 'SAVINGSACCOUNT', 'UNBLOCK_CHECKER', 0),
('transaction_savings', 'RELEASEAMOUNT_SAVINGSACCOUNT', 'SAVINGSACCOUNT', 'RELEASEAMOUNT', 0),
('transaction_savings', 'RELEASEAMOUNT_SAVINGSACCOUNT_CHECKER', 'SAVINGSACCOUNT', 'RELEASEAMOUNT_CHECKER', 0);

--  modify `m_savings_account_transaction` 

ALTER TABLE `m_savings_account_transaction` ADD COLUMN `release_id_of_hold_amount` BIGINT(20) NULL DEFAULT NULL;



