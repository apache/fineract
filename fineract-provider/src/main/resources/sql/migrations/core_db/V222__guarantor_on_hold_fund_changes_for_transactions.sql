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

ALTER TABLE `m_deposit_account_on_hold_transaction`
	ADD COLUMN `created_date` DATETIME NOT NULL;
	
ALTER TABLE `m_guarantor_transaction`
	ALTER `loan_transaction_id` DROP DEFAULT;
	
ALTER TABLE `m_guarantor_transaction`
	CHANGE COLUMN `loan_transaction_id` `loan_transaction_id` BIGINT(20) NULL ;
	
INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ('portfolio', 'RECOVERGUARANTEES_LOAN', 'LOAN', 'RECOVERGUARANTEES', 0);
INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ('portfolio', 'RECOVERGUARANTEES_LOAN_CHECKER', 'LOAN', 'RECOVERGUARANTEES_CHECKER', 0);
ALTER TABLE `m_loan`
	DROP COLUMN `guarantee_outstanding_amount_derived`;
	