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
-- !both tn03, tn04 tenants

-- saving product, account
SET @last_saving_prod_id = -1;
SELECT COALESCE(max(id), 1) from m_savings_product into @last_saving_prod_id;

SET @saving_prod_name = concat('Saving Product', @last_saving_prod_id);

INSERT INTO `m_savings_product`
(`name`, `short_name`, `description`, `deposit_type_enum`, `currency_code`, `currency_digits`,
 `currency_multiplesof`, `nominal_annual_interest_rate`, `interest_compounding_period_enum`,
 `interest_posting_period_enum`, `interest_calculation_type_enum`, `interest_calculation_days_in_year_type_enum`,
 `min_required_opening_balance`, `accounting_type`, `withdrawal_fee_amount`, `withdrawal_fee_type_enum`,
 `withdrawal_fee_for_transfer`, `allow_overdraft`, `min_required_balance`, `enforce_min_required_balance`,
 `min_balance_for_interest_calculation`, `withhold_tax`, `tax_group_id`, `is_dormancy_tracking_active`)
VALUES (@saving_prod_name, concat('SP', @last_saving_prod_id), 'Saving Product', 100, 'TZS', 2, NULL, 0.000000, 1,
                           4, 1, 360, NULL, 2, NULL, NULL, 0, 0, 0.000000, 1, NULL, 0, NULL, 0);

SET @saving_prod_id = -1;
SELECT id FROM m_savings_product WHERE name = @saving_prod_name INTO @saving_prod_id;

-- interop_identifier

-- charge, mapping
-- gl_account, mappings
-- ASSET-1, LIABILITY-2, EQUITY-3, INCOME-4, EXPENSE-5

SET @payment_type_id = -1;
SELECT id FROM m_payment_type WHERE value = 'Money Transfer' INTO @payment_type_id;

SET @saving_gl_name = 'Interoperation Saving';
INSERT INTO `acc_gl_account` (`name`, `parent_id`, `hierarchy`, `gl_code`, `disabled`, `manual_journal_entries_allowed`, `account_usage`, `classification_enum`, `description`)
VALUES (@saving_gl_name, NULL, NULL, 'Interop_Saving', 0, 1, 1, 1, 'Interoperation Saving Asset'); -- account_usage: DETAIL, classification_enum: ASSET

INSERT INTO `acc_product_mapping` (`gl_account_id`, `product_id`, `product_type`, `payment_type`, `charge_id`, `financial_account_type`)
VALUES ((SELECT id FROM acc_gl_account WHERE name = @saving_gl_name), @saving_prod_id, 2, @payment_type_id, NULL, 1); -- product_type: SAVING, financial_account_type: ASSET

SET @nostro_gl_name = 'Interoperation NOSTRO';
INSERT INTO `acc_gl_account` (`name`, `parent_id`, `hierarchy`, `gl_code`, `disabled`, `manual_journal_entries_allowed`, `account_usage`, `classification_enum`, `description`)
VALUES (@nostro_gl_name, NULL, NULL, 'Interop_Nostro', 0, 0, 1, 2, 'Interoperation NOSTRO Liability'); -- account_usage: DETAIL, classification_enum: LIABILITY

INSERT INTO `acc_product_mapping` (`gl_account_id`, `product_id`, `product_type`, `payment_type`, `charge_id`, `financial_account_type`)
VALUES ((SELECT id FROM acc_gl_account WHERE name = @nostro_gl_name), @saving_prod_id, 2, NULL, NULL, 2); -- product_type: SAVING, financial_account_type: LIABILITY

SET @fee_gl_name = 'Interoperation Fee';
INSERT INTO `acc_gl_account` (`name`, `parent_id`, `hierarchy`, `gl_code`, `disabled`, `manual_journal_entries_allowed`, `account_usage`, `classification_enum`, `description`)
VALUES (@fee_gl_name, NULL, NULL, 'Interop_Fee', 0, 0, 1, 4, 'Interoperation Fee Income'); -- account_usage: DETAIL, classification_enum: INCOME

SET @fee_gl_id = -1;
SELECT id FROM acc_gl_account WHERE name = @fee_gl_name INTO @fee_gl_id;

INSERT INTO `acc_product_mapping` (`gl_account_id`, `product_id`, `product_type`, `payment_type`, `charge_id`, `financial_account_type`)
VALUES (@fee_gl_id, @saving_prod_id, 2, NULL, NULL, 4); -- product_type: SAVING, financial_account_type: INCOME

SET @charge_name = 'Interoperation Withdraw Fee';
INSERT INTO `m_charge`
(`name`,`currency_code`,`charge_applies_to_enum`,`charge_time_enum`,`charge_calculation_enum`,`charge_payment_mode_enum`,
 `amount`,`fee_on_day`,`fee_interval`,`fee_on_month`,`is_penalty`,`is_active`,`is_deleted`,`min_cap`,`max_cap`,`fee_frequency`,
 `income_or_liability_account_id`,`tax_group_id`)
VALUES (@charge_name, 'TZS', 2, 5, 1, NULL, 1.000000, NULL, NULL, NULL, 0, 0, 0, NULL, NULL, NULL, @fee_gl_id, NULL);

-- loan product
/*
SET @last_ext_id = -1;
SELECT COALESCE(max(external_id), 1) FROM m_product_loan INTO @last_ext_id;

INSERT INTO `m_product_loan`
VALUES
(CONCAT('IP', @last_product_id), 'EUR', 2, 1, 50000.000000, NULL, NULL, NULL, concat('Interoperation Customer Product', @last_product_id),
  'Demo Interoperation Product', NULL, b'0', b'0', 1.000000, 1.000000, NULL, NULL, 3, 1.000000, 0, 1, 1, 1, 2, 1200, NULL,
  NULL, NULL, NULL, NULL, 1, 1, 3, @last_ext_id + 1, 0, 0,ADDDATE(curdate(),-100),ADDDATE(curdate(),100), 0, 0, NULL, NULL,
  NULL, 1, 30, 0, 0, 0.00, 0, 1, 0, 0, 0);

SET @product_id = -1;
SELECT id FROM m_product_loan WHERE name = concat('Interoperation Customer Product', @last_product_id) INTO @product_id;

-- charge, mapping
INSERT INTO `m_charge` VALUES (
  NULL, concat('Loan Withdraw Fee_', @product_id), 'TZS', 1, 2,
        1, 0, 1.000000, NULL, NULL,
        NULL, 0, 1, 0, NULL,
  NULL, NULL, NULL, NULL);

INSERT INTO `m_product_loan_charge` VALUES
  (@product_id, (SELECT id
                 FROM m_charge
                 WHERE name = concat('Loan Withdraw Fee_', @product_id)));

-- gl_account, mappings
-- ASSET-1, LIABILITY-2, EQUITY-3, INCOME-4, EXPENSE-5
SET @liab_acc_name = concat('Loan Payable Liability_', @product_id);
INSERT INTO `acc_gl_account` VALUES (
  NULL, @liab_acc_name, NULL, NULL, concat('0360009420', @product_id),
        0, 1, 1, 1, NULL, 'Loan Payable Liability');

INSERT INTO `acc_product_mapping` VALUES (
  NULL,
  (SELECT id
   FROM acc_gl_account
   WHERE name = @liab_acc_name),
  @product_id,
  NULL, NULL, NULL, 2);

SET @nostro_acc_name = concat('Loan NOSTRO_', @product_id);
INSERT INTO `acc_gl_account` VALUES (
  NULL, @nostro_acc_name, NULL, NULL, concat('0360009421', @product_id),
        0, 1, 1, 1, NULL, 'Loan NOSTRO');

INSERT INTO `acc_product_mapping` VALUES (
  NULL,
  (SELECT id
   FROM acc_gl_account
   WHERE name = @nostro_acc_name),
  @product_id,
  NULL, NULL, NULL, 1);

SET @cash_acc_name = concat('Loan Product Cash_', @product_id);
INSERT INTO `acc_gl_account` VALUES (
  NULL, @cash_acc_name, NULL, NULL, concat('0360009422', @product_id),
        0, 1, 1, 1, NULL, 'Loan Product Cash');

INSERT INTO `acc_product_mapping` VALUES (
  NULL,
  (SELECT id
   FROM acc_gl_account
   WHERE name = @cash_acc_name),
  @product_id,
  NULL, NULL, NULL, 1);

SET @expen_acc_name = concat('Loan Product Expenses_', @product_id);
INSERT INTO `acc_gl_account` VALUES (
  NULL, @expen_acc_name, NULL, NULL, concat('0360009423', @product_id),
        0, 1, 1, 1, NULL, 'Loan Product Expenses');

INSERT INTO `acc_product_mapping` VALUES (
  NULL,
  (SELECT id
   FROM acc_gl_account
   WHERE name = @expen_acc_name),
  @product_id,
  NULL, NULL, NULL, 5);

SET @accrue_acc_name = concat('Loan Product Accrue Liability_', @product_id);
INSERT INTO `acc_gl_account` VALUES (
  NULL, @accrue_acc_name, NULL, NULL, concat('0360009424', @product_id),
        0, 1, 1, 1, NULL, 'Loan Product Accrue Liability');

INSERT INTO `acc_product_mapping` VALUES (
  NULL,
  (SELECT id
   FROM acc_gl_account
   WHERE name = @accrue_acc_name),
  @product_id,
  NULL, NULL, NULL, 2);

SET @equ_acc_name = concat('Loan Product Equity_', @product_id);
INSERT INTO `acc_gl_account` VALUES (
  NULL, @equ_acc_name, NULL, NULL, concat('0360009425', @product_id),
        0, 1, 1, 1, NULL, 'Loan Product Equity');

INSERT INTO `acc_product_mapping` VALUES (
  NULL,
  (SELECT id
   FROM acc_gl_account
   WHERE name = @equ_acc_name),
  @product_id,
  NULL, NULL, NULL, 3);

SET @feer_acc_name = concat('Loan Product Fees Revenue_', @product_id);
INSERT INTO `acc_gl_account` VALUES (
  NULL, @feer_acc_name, NULL, NULL, concat('0360009426', @product_id),
        0, 1, 1, 1, NULL, 'Loan Product Fees Revenue');

INSERT INTO `acc_product_mapping` VALUES (
  NULL,
  (SELECT id
   FROM acc_gl_account
   WHERE name = @feer_acc_name),
  @product_id,
  NULL, NULL, NULL, 4);*/
