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

/***Set defaults for income from Penalties for existing Saving Products**/
ALTER TABLE `m_loan_transaction`
	ADD COLUMN `overpayment_portion_derived` DECIMAL(19,6) NULL DEFAULT NULL AFTER `penalty_charges_portion_derived`;

/**Add dummy liability account if organization already has a loan product with accounting enabled**/
INSERT INTO `acc_gl_account` (`name`, `hierarchy`, `gl_code`,`account_usage`, `classification_enum`,`description`)
select 'Loan Overpayments (Temp)', '.', '22000-Temp', 1, 2,'Temporary account to track Loan overpayments Liabilities'
FROM m_product_loan WHERE accounting_type != 1
limit 1;


/**Map a liability account for every loan which has accounting enabled**/
INSERT INTO `acc_product_mapping` (`gl_account_id`,`product_id`,`product_type`,`financial_account_type`)
select (select max(id) from acc_gl_account where classification_enum=2 and account_usage=1 LIMIT 1), mapping.product_id, mapping.product_type, 11
from acc_product_mapping mapping
where mapping.financial_account_type = 2 and mapping.product_type=1;