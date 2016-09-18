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

﻿DELETE FROM `m_organisation_currency` WHERE id>0;

INSERT INTO `m_organisation_currency`
(
`code`,
`decimal_places`,
`name`,
`display_symbol`,
`internationalized_name_code`)
VALUES
('UGX', 2, 'Uganda Shilling', 'USh', 'currency.UGX');


UPDATE `m_appuser` SET `username` = 'admin' WHERE id=1;

INSERT INTO `m_appuser`
(`is_deleted`,
`office_id`,
`username`,
`firstname`,
`lastname`,
`password`,
`email`,
`firsttime_login_remaining`,
`nonexpired`,
`nonlocked`,
`nonexpired_credentials`,
`enabled`)
VALUES
(0, 1, 'keithwoodlock', 'Keith', 'Woodlock', 
'4f607e9b6cffbe7d3db92d4bfa3391c7aa751727b4ea29d08fddf9dd72e6e7e3', 'keithwoodlock@gmail.com', 0, 1, 1, 1, 1);

INSERT INTO `m_appuser_role`
(`appuser_id`,`role_id`) VALUES (2,1);

UPDATE `m_office` SET `name` = 'CEDA Microfinance Ltd.' WHERE id=1;

INSERT INTO `m_office`
(
`parent_id`,
`hierarchy`,
`external_id`,
`name`,
`opening_date`)
VALUES 
(1, '.2.', 2, 'Uganda (Kampala)', '2009-01-01');


INSERT INTO `m_staff`
(
`is_loan_officer`,
`office_id`,
`firstname`,
`lastname`,
`display_name`)
VALUES
(1, 1, 'CEDA HO', 'LoanOfficer', 'LoanOfficer, CEDA HO'),
(1, 2, 'Kampala', 'LoanOfficer', 'LoanOfficer, Kampala');


INSERT INTO `m_charge`
(
`name`,
`currency_code`,
`charge_applies_to_enum`,
`charge_time_enum`,
`charge_calculation_enum`,
`amount`,
`is_penalty`,
`is_active`,
`is_deleted`)
VALUES ('Bank Fee (per installment)', 'UGX', 1, 2, 1, 1500.000000, 0, 1, 0);


INSERT INTO `m_product_loan`
(
`currency_code`,
`currency_digits`,
`principal_amount`,
`min_principal_amount`,
`max_principal_amount`,
`arrearstolerance_amount`,
`name`,
`description`,
`fund_id`,
`nominal_interest_rate_per_period`,
`interest_period_frequency_enum`,
`annual_nominal_interest_rate`,
`interest_method_enum`,
`interest_calculated_in_period_enum`,
`repay_every`,
`repayment_period_frequency_enum`,
`number_of_repayments`,
`amortization_method_enum`,
`accounting_type`,
`loan_transaction_strategy_id`)
VALUES
('UGX', 2, 1000000.000000, 0, 1000000000000.000000, null , 'Kampala Product (with cash accounting)', 
'Typical Kampala loan product with cash accounting enabled for testing.', null , 
24.000000, 3, 24.000000, 1, 1, 1, 2, 12, 1, 2, 2);

-- mapping of that loan product to GL Accounts
INSERT INTO `acc_product_mapping` 
VALUES (1,4,1,1,1),(2,8,1,1,2),(3,34,1,1,3),(4,37,1,1,4),(5,35,1,1,5),(6,97,1,1,6);