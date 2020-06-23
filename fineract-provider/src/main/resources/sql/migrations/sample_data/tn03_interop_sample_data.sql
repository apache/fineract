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

-- To use: Change saving_account_no before every run!

USE `tn03`;

-- data initialization
-- user+roles

-- client
SET @client_name = 'InteropCustomer';
SET @saving_account_no = '9062b90de19b43989005';
SET @saving_account_ext_id = '9062b90de19b43989005d9';
SET @IBAN = 'IC11in02tn03' + @saving_account_ext_id;
SET @MSISDN = '27710203999';

INSERT INTO `m_client` (`account_no`, `external_id`, `status_enum`, `sub_status`, `activation_date`, `office_joining_date`,
                        `office_id`, `transfer_to_office_id`, `staff_id`, `firstname`, `middlename`, `lastname`, `fullname`,
                        `display_name`, `mobile_no`, `gender_cv_id`, `date_of_birth`, `image_id`, `closure_reason_cv_id`,
                        `closedon_date`, `updated_by`, `updated_on`, `submittedon_date`, `submittedon_userid`, `activatedon_userid`,
                        `closedon_userid`, `default_savings_product`, `default_savings_account`, `client_type_cv_id`, `client_classification_cv_id`,
                        `reject_reason_cv_id`, `rejectedon_date`, `rejectedon_userid`, `withdraw_reason_cv_id`, `withdrawn_on_date`,
                        `withdraw_on_userid`, `reactivated_on_date`, `reactivated_on_userid`, `legal_form_enum`, `reopened_on_date`,
                        `reopened_by_userid`)
VALUES (@saving_account_no, NULL, 300, NULL, ADDDATE(curdate(), -100), NULL, 1, NULL, NULL, NULL, NULL, NULL,
 @client_name, @client_name, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, ADDDATE(curdate(), -100),
  NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL,NULL, NULL);

-- saving product, account
SET @last_saving_prod_id = -1;
SELECT COALESCE(max(id), 1) from m_savings_product into @last_saving_prod_id;

SET @saving_prod_name = concat('Saving Product', @last_saving_prod_id);
SET @saving_prod_id = -1;
SELECT id FROM m_savings_product WHERE name = @saving_prod_name INTO @saving_prod_id;

SET @client_id = -1;
SELECT id FROM m_client WHERE fullname = @client_name INTO @client_id;

INSERT INTO `m_savings_account`
(`account_no`, `external_id`, `client_id`, `group_id`, `product_id`, `field_officer_id`, `status_enum`,
 `sub_status_enum`, `account_type_enum`, `deposit_type_enum`, `submittedon_date`, `submittedon_userid`,
 `approvedon_date`, `approvedon_userid`, `activatedon_date`, `activatedon_userid`,
 `currency_code`, `currency_digits`, `currency_multiplesof`, `nominal_annual_interest_rate`,
 `interest_compounding_period_enum`, `interest_posting_period_enum`, `interest_calculation_type_enum`,
 `interest_calculation_days_in_year_type_enum`, `min_required_opening_balance`, `withdrawal_fee_for_transfer`,
 `allow_overdraft`, `account_balance_derived`, `min_required_balance`, `enforce_min_required_balance`,
 `version`, `withhold_tax`)
VALUES (@saving_account_no, @saving_account_ext_id, @client_id, NULL, @saving_prod_id, NULL, 300, 0, 1, 100, ADDDATE(curdate(), -100),
  NULL, ADDDATE(curdate(), -100), NULL, ADDDATE(curdate(), -100), NULL, 'TZS', 2, NULL, 1.000000, 1, 4, 1, -- 29. - 4
  360, NULL, 1, 1, 100000000.000000, 0.000000, 1, 1, 0);

-- interop_identifier
SET @saving_acc_id = -1;
SELECT id FROM m_savings_account WHERE account_no = @saving_account_no INTO @saving_acc_id;

INSERT INTO interop_identifier (id, account_id, type, a_value, sub_value_or_type, created_by, created_on, modified_by, modified_on)
VALUES (NULL, @saving_acc_id, 'IBAN', @IBAN, NULL, 'operator', CURDATE(), 'operator',
        CURDATE());
INSERT INTO interop_identifier (id, account_id, type, a_value, sub_value_or_type, created_by, created_on, modified_by, modified_on)
VALUES (NULL, @saving_acc_id, 'MSISDN', @MSISDN, NULL, 'operator', CURDATE(), 'operator', CURDATE());

-- charge, mapping
-- gl_account, mappings
-- ASSET-1, LIABILITY-2, EQUITY-3, INCOME-4, EXPENSE-5
SET @charge_name = 'Interoperation Withdraw Fee';

INSERT INTO `m_savings_account_charge` (`savings_account_id`, `charge_id`, `is_penalty`, `charge_time_enum`, `charge_calculation_enum`,
                                        `amount`, `amount_outstanding_derived`,`is_paid_derived`, `waived`, `is_active`)
VALUES (@saving_acc_id, (SELECT id FROM m_charge WHERE name = @charge_name), 0, 5, 1, 1.000000, 0.000000, 0, 0, 1);
