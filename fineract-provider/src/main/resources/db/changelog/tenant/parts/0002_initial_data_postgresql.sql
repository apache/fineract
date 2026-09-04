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

-- PostgreSQL database dump

-- Dumped from database version 18.3 (Debian 18.3-1.pgdg13+1)
-- Dumped by pg_dump version 18.3 (Debian 18.3-1.pgdg13+1)

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
-- SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

-- Data for Name: m_code; Type: TABLE DATA; Schema: public; Owner: -

INSERT INTO public.m_code (id, code_name, is_system_defined) VALUES ('1', 'Customer Identifier', 't');
INSERT INTO public.m_code (id, code_name, is_system_defined) VALUES ('2', 'LoanCollateral', 't');
INSERT INTO public.m_code (id, code_name, is_system_defined) VALUES ('3', 'LoanPurpose', 't');
INSERT INTO public.m_code (id, code_name, is_system_defined) VALUES ('4', 'Gender', 't');
INSERT INTO public.m_code (id, code_name, is_system_defined) VALUES ('5', 'YesNo', 't');
INSERT INTO public.m_code (id, code_name, is_system_defined) VALUES ('6', 'GuarantorRelationship', 't');
INSERT INTO public.m_code (id, code_name, is_system_defined) VALUES ('7', 'AssetAccountTags', 't');
INSERT INTO public.m_code (id, code_name, is_system_defined) VALUES ('8', 'LiabilityAccountTags', 't');
INSERT INTO public.m_code (id, code_name, is_system_defined) VALUES ('9', 'EquityAccountTags', 't');
INSERT INTO public.m_code (id, code_name, is_system_defined) VALUES ('10', 'IncomeAccountTags', 't');
INSERT INTO public.m_code (id, code_name, is_system_defined) VALUES ('11', 'ExpenseAccountTags', 't');
INSERT INTO public.m_code (id, code_name, is_system_defined) VALUES ('13', 'GROUPROLE', 't');
INSERT INTO public.m_code (id, code_name, is_system_defined) VALUES ('14', 'ClientClosureReason', 't');
INSERT INTO public.m_code (id, code_name, is_system_defined) VALUES ('15', 'GroupClosureReason', 't');
INSERT INTO public.m_code (id, code_name, is_system_defined) VALUES ('16', 'ClientType', 't');
INSERT INTO public.m_code (id, code_name, is_system_defined) VALUES ('17', 'ClientClassification', 't');
INSERT INTO public.m_code (id, code_name, is_system_defined) VALUES ('18', 'ClientSubStatus', 't');
INSERT INTO public.m_code (id, code_name, is_system_defined) VALUES ('19', 'ClientRejectReason', 't');
INSERT INTO public.m_code (id, code_name, is_system_defined) VALUES ('20', 'ClientWithdrawReason', 't');
INSERT INTO public.m_code (id, code_name, is_system_defined) VALUES ('21', 'Entity to Entity Access Types', 't');
INSERT INTO public.m_code (id, code_name, is_system_defined) VALUES ('22', 'CenterClosureReason', 't');
INSERT INTO public.m_code (id, code_name, is_system_defined) VALUES ('23', 'LoanRescheduleReason', 't');
INSERT INTO public.m_code (id, code_name, is_system_defined) VALUES ('24', 'Constitution', 't');
INSERT INTO public.m_code (id, code_name, is_system_defined) VALUES ('25', 'Main Business Line', 't');
INSERT INTO public.m_code (id, code_name, is_system_defined) VALUES ('26', 'WriteOffReasons', 't');
INSERT INTO public.m_code (id, code_name, is_system_defined) VALUES ('27', 'STATE', 't');
INSERT INTO public.m_code (id, code_name, is_system_defined) VALUES ('28', 'COUNTRY', 't');
INSERT INTO public.m_code (id, code_name, is_system_defined) VALUES ('29', 'ADDRESS_TYPE', 't');
INSERT INTO public.m_code (id, code_name, is_system_defined) VALUES ('30', 'MARITAL STATUS', 't');
INSERT INTO public.m_code (id, code_name, is_system_defined) VALUES ('31', 'RELATIONSHIP', 't');
INSERT INTO public.m_code (id, code_name, is_system_defined) VALUES ('32', 'PROFESSION', 't');
INSERT INTO public.m_code (id, code_name, is_system_defined) VALUES ('33', 'PaymentType', 't');
INSERT INTO public.m_code (id, code_name, is_system_defined) VALUES ('34', 'Customer Documents', 't');
INSERT INTO public.m_code (id, code_name, is_system_defined) VALUES ('35', 'SavingsAccountBlockReasons', 't');
INSERT INTO public.m_code (id, code_name, is_system_defined) VALUES ('36', 'DebitTransactionFreezeReasons', 't');
INSERT INTO public.m_code (id, code_name, is_system_defined) VALUES ('37', 'CreditTransactionFreezeReasons', 't');
INSERT INTO public.m_code (id, code_name, is_system_defined) VALUES ('38', 'SavingsTransactionFreezeReasons', 't');
INSERT INTO public.m_code (id, code_name, is_system_defined) VALUES ('39', 'ChargeOffReasons', 't');


-- Data for Name: m_code_value; Type: TABLE DATA; Schema: public; Owner: -

INSERT INTO public.m_code_value (id, code_id, code_value, code_description, order_position, code_score, is_active, is_mandatory) VALUES ('1', '1', 'Passport', NULL, '1', NULL, 't', 'f');
INSERT INTO public.m_code_value (id, code_id, code_value, code_description, order_position, code_score, is_active, is_mandatory) VALUES ('2', '1', 'Id', NULL, '2', NULL, 't', 'f');
INSERT INTO public.m_code_value (id, code_id, code_value, code_description, order_position, code_score, is_active, is_mandatory) VALUES ('3', '1', 'Drivers License', NULL, '3', NULL, 't', 'f');
INSERT INTO public.m_code_value (id, code_id, code_value, code_description, order_position, code_score, is_active, is_mandatory) VALUES ('4', '1', 'Any Other Id Type', NULL, '4', NULL, 't', 'f');
INSERT INTO public.m_code_value (id, code_id, code_value, code_description, order_position, code_score, is_active, is_mandatory) VALUES ('5', '6', 'Spouse', NULL, '0', NULL, 't', 'f');
INSERT INTO public.m_code_value (id, code_id, code_value, code_description, order_position, code_score, is_active, is_mandatory) VALUES ('6', '6', 'Parent', NULL, '0', NULL, 't', 'f');
INSERT INTO public.m_code_value (id, code_id, code_value, code_description, order_position, code_score, is_active, is_mandatory) VALUES ('7', '6', 'Sibling', NULL, '0', NULL, 't', 'f');
INSERT INTO public.m_code_value (id, code_id, code_value, code_description, order_position, code_score, is_active, is_mandatory) VALUES ('8', '6', 'Business Associate', NULL, '0', NULL, 't', 'f');
INSERT INTO public.m_code_value (id, code_id, code_value, code_description, order_position, code_score, is_active, is_mandatory) VALUES ('9', '6', 'Other', NULL, '0', NULL, 't', 'f');
INSERT INTO public.m_code_value (id, code_id, code_value, code_description, order_position, code_score, is_active, is_mandatory) VALUES ('10', '21', 'Office Access to Loan Products', NULL, '0', NULL, 't', 'f');
INSERT INTO public.m_code_value (id, code_id, code_value, code_description, order_position, code_score, is_active, is_mandatory) VALUES ('11', '21', 'Office Access to Savings Products', NULL, '0', NULL, 't', 'f');
INSERT INTO public.m_code_value (id, code_id, code_value, code_description, order_position, code_score, is_active, is_mandatory) VALUES ('12', '21', 'Office Access to Fees/Charges', NULL, '0', NULL, 't', 'f');
INSERT INTO public.m_code_value (id, code_id, code_value, code_description, order_position, code_score, is_active, is_mandatory) VALUES ('13', '13', 'Leader', 'Group Leader Role', '1', NULL, 't', 'f');
INSERT INTO public.m_code_value (id, code_id, code_value, code_description, order_position, code_score, is_active, is_mandatory) VALUES ('14', '33', 'Money Transfer', NULL, '1', NULL, 't', 'f');


-- Data for Name: acc_gl_account; Type: TABLE DATA; Schema: public; Owner: -



-- Data for Name: m_office; Type: TABLE DATA; Schema: public; Owner: -

INSERT INTO public.m_office (id, parent_id, hierarchy, external_id, name, opening_date) VALUES ('1', NULL, '.', '1', 'Head Office', '2009-01-01');


-- Data for Name: acc_accounting_rule; Type: TABLE DATA; Schema: public; Owner: -



-- Data for Name: m_image; Type: TABLE DATA; Schema: public; Owner: -



-- Data for Name: m_staff; Type: TABLE DATA; Schema: public; Owner: -



-- Data for Name: m_appuser; Type: TABLE DATA; Schema: public; Owner: -

INSERT INTO public.m_appuser (id, is_deleted, office_id, staff_id, username, firstname, lastname, password, email, firsttime_login_remaining, nonexpired, nonlocked, nonexpired_credentials, enabled, last_time_password_updated, password_never_expires, cannot_change_password, password_reset_required, failed_login_attempts, is_login_retries_enabled, temporary_password, temporary_password_expiry_time, is_password_reset_enabled) VALUES ('1', 'f', '1', NULL, 'mifos', 'App', 'Administrator', '{SHA-256}{1}5787039480429368bf94732aacc771cd0a3ea02bcf504ffe1185ab94213bc63a', 'demomfi@mifos.org', 'f', 't', 't', 't', 't', '2026-08-21', 'f', NULL, 'f', '0', 'f', NULL, NULL, 'f');
INSERT INTO public.m_appuser (id, is_deleted, office_id, staff_id, username, firstname, lastname, password, email, firsttime_login_remaining, nonexpired, nonlocked, nonexpired_credentials, enabled, last_time_password_updated, password_never_expires, cannot_change_password, password_reset_required, failed_login_attempts, is_login_retries_enabled, temporary_password, temporary_password_expiry_time, is_password_reset_enabled) VALUES ('2', 'f', '1', NULL, 'system', 'system', 'system', '{SHA-256}{2}5787039480429368bf94732aacc771cd0a3ea02bcf504ffe1185ab94213bc63a', 'demomfi@mifos.org', 'f', 't', 't', 't', 't', '2026-08-21', 'f', NULL, 'f', '0', 'f', NULL, NULL, 'f');
INSERT INTO public.m_appuser (id, is_deleted, office_id, staff_id, username, firstname, lastname, password, email, firsttime_login_remaining, nonexpired, nonlocked, nonexpired_credentials, enabled, last_time_password_updated, password_never_expires, cannot_change_password, password_reset_required, failed_login_attempts, is_login_retries_enabled, temporary_password, temporary_password_expiry_time, is_password_reset_enabled) VALUES ('3', 'f', '1', NULL, 'interopUser', 'Interop', 'User', '{SHA-256}{3}5787039480429368bf94732aacc771cd0a3ea02bcf504ffe1185ab94213bc63a', 'email@email.com', 'f', 't', 't', 't', 't', '2026-08-21', 'f', NULL, 'f', '0', 'f', NULL, NULL, 'f');


-- Data for Name: acc_gl_closure; Type: TABLE DATA; Schema: public; Owner: -



-- Data for Name: acc_gl_financial_activity_account; Type: TABLE DATA; Schema: public; Owner: -



-- Data for Name: m_group_level; Type: TABLE DATA; Schema: public; Owner: -

INSERT INTO public.m_group_level (id, parent_id, super_parent, level_name, recursable, can_have_clients) VALUES ('1', NULL, 't', 'Center', 't', 'f');
INSERT INTO public.m_group_level (id, parent_id, super_parent, level_name, recursable, can_have_clients) VALUES ('2', '1', 'f', 'Group', 'f', 't');


-- Data for Name: m_group; Type: TABLE DATA; Schema: public; Owner: -



-- Data for Name: glim_accounts; Type: TABLE DATA; Schema: public; Owner: -



-- Data for Name: gsim_accounts; Type: TABLE DATA; Schema: public; Owner: -



-- Data for Name: m_tax_group; Type: TABLE DATA; Schema: public; Owner: -



-- Data for Name: m_savings_product; Type: TABLE DATA; Schema: public; Owner: -



-- Data for Name: m_client; Type: TABLE DATA; Schema: public; Owner: -



-- Data for Name: m_client_transaction; Type: TABLE DATA; Schema: public; Owner: -



-- Data for Name: m_delinquency_bucket; Type: TABLE DATA; Schema: public; Owner: -



-- Data for Name: m_fund; Type: TABLE DATA; Schema: public; Owner: -



-- Data for Name: m_product_loan; Type: TABLE DATA; Schema: public; Owner: -



-- Data for Name: m_loan; Type: TABLE DATA; Schema: public; Owner: -



-- Data for Name: m_payment_type; Type: TABLE DATA; Schema: public; Owner: -

INSERT INTO public.m_payment_type (id, value, description, is_cash_payment, order_position, code_name, is_system_defined) VALUES ('1', 'Money Transfer', 'Money Transfer', 'f', '1', NULL, 'f');
INSERT INTO public.m_payment_type (id, value, description, is_cash_payment, order_position, code_name, is_system_defined) VALUES ('2', 'Repayment Adjustment Chargeback', 'Repayment Adjustment Chargeback', 'f', '1', 'REPAYMENT_ADJUSTMENT_CHARGEBACK', 't');
INSERT INTO public.m_payment_type (id, value, description, is_cash_payment, order_position, code_name, is_system_defined) VALUES ('3', 'Repayment Adjustment Refund', 'Repayment Adjustment Refund', 'f', '1', 'REPAYMENT_ADJUSTMENT_REFUND', 't');


-- Data for Name: m_payment_detail; Type: TABLE DATA; Schema: public; Owner: -



-- Data for Name: m_loan_transaction; Type: TABLE DATA; Schema: public; Owner: -



-- Data for Name: m_savings_account; Type: TABLE DATA; Schema: public; Owner: -



-- Data for Name: m_savings_account_transaction; Type: TABLE DATA; Schema: public; Owner: -



-- Data for Name: m_share_product; Type: TABLE DATA; Schema: public; Owner: -



-- Data for Name: m_share_account; Type: TABLE DATA; Schema: public; Owner: -



-- Data for Name: m_share_account_transactions; Type: TABLE DATA; Schema: public; Owner: -



-- Data for Name: acc_gl_journal_entry; Type: TABLE DATA; Schema: public; Owner: -



-- Data for Name: acc_gl_journal_entry_annual_summary; Type: TABLE DATA; Schema: public; Owner: -



-- Data for Name: m_charge; Type: TABLE DATA; Schema: public; Owner: -



-- Data for Name: acc_product_mapping; Type: TABLE DATA; Schema: public; Owner: -



-- Data for Name: acc_rule_tags; Type: TABLE DATA; Schema: public; Owner: -



-- Data for Name: batch_custom_job_parameters; Type: TABLE DATA; Schema: public; Owner: -



-- Data for Name: batch_job_instance; Type: TABLE DATA; Schema: public; Owner: -



-- Data for Name: batch_job_execution; Type: TABLE DATA; Schema: public; Owner: -



-- Data for Name: batch_job_execution_context; Type: TABLE DATA; Schema: public; Owner: -



-- Data for Name: batch_job_execution_params; Type: TABLE DATA; Schema: public; Owner: -



-- Data for Name: batch_step_execution; Type: TABLE DATA; Schema: public; Owner: -



-- Data for Name: batch_step_execution_context; Type: TABLE DATA; Schema: public; Owner: -



-- Data for Name: c_account_number_format; Type: TABLE DATA; Schema: public; Owner: -



-- Data for Name: c_cache; Type: TABLE DATA; Schema: public; Owner: -

INSERT INTO public.c_cache (id, cache_type_enum) VALUES ('1', '1');


-- Data for Name: c_configuration; Type: TABLE DATA; Schema: public; Owner: -

INSERT INTO public.c_configuration (id, name, value, date_value, string_value, enabled, is_trap_door, description) VALUES ('1', 'maker-checker', NULL, NULL, NULL, 'f', 'f', NULL);
INSERT INTO public.c_configuration (id, name, value, date_value, string_value, enabled, is_trap_door, description) VALUES ('4', 'amazon-s3', NULL, NULL, NULL, 'f', 'f', NULL);
INSERT INTO public.c_configuration (id, name, value, date_value, string_value, enabled, is_trap_door, description) VALUES ('5', 'reschedule-future-repayments', NULL, NULL, NULL, 't', 'f', NULL);
INSERT INTO public.c_configuration (id, name, value, date_value, string_value, enabled, is_trap_door, description) VALUES ('6', 'reschedule-repayments-on-holidays', NULL, NULL, NULL, 'f', 'f', NULL);
INSERT INTO public.c_configuration (id, name, value, date_value, string_value, enabled, is_trap_door, description) VALUES ('7', 'allow-transactions-on-holiday', NULL, NULL, NULL, 'f', 'f', NULL);
INSERT INTO public.c_configuration (id, name, value, date_value, string_value, enabled, is_trap_door, description) VALUES ('8', 'allow-transactions-on-non-workingday', NULL, NULL, NULL, 'f', 'f', NULL);
INSERT INTO public.c_configuration (id, name, value, date_value, string_value, enabled, is_trap_door, description) VALUES ('9', 'constraint-approach-for-datatables', NULL, NULL, NULL, 'f', 'f', NULL);
INSERT INTO public.c_configuration (id, name, value, date_value, string_value, enabled, is_trap_door, description) VALUES ('10', 'penalty-wait-period', '2', NULL, NULL, 't', 'f', NULL);
INSERT INTO public.c_configuration (id, name, value, date_value, string_value, enabled, is_trap_door, description) VALUES ('11', 'force-password-reset-days', '0', NULL, NULL, 'f', 'f', NULL);
INSERT INTO public.c_configuration (id, name, value, date_value, string_value, enabled, is_trap_door, description) VALUES ('12', 'grace-on-penalty-posting', '0', NULL, NULL, 't', 'f', NULL);
INSERT INTO public.c_configuration (id, name, value, date_value, string_value, enabled, is_trap_door, description) VALUES ('15', 'savings-interest-posting-current-period-end', NULL, NULL, NULL, 'f', 'f', 'Recommended to be changed only once during start of production. When set as false(default), interest will be posted on the first date of next period. If set as true, interest will be posted on last date of current period. There is no difference in the interest amount posted.');
INSERT INTO public.c_configuration (id, name, value, date_value, string_value, enabled, is_trap_door, description) VALUES ('16', 'financial-year-beginning-month', '1', NULL, NULL, 't', 'f', 'Recommended to be changed only once during start of production. Allowed values 1 - 12 (January - December). Interest posting periods are evaluated based on this configuration.');
INSERT INTO public.c_configuration (id, name, value, date_value, string_value, enabled, is_trap_door, description) VALUES ('17', 'min-clients-in-group', '5', NULL, NULL, 'f', 'f', 'Minimum number of Clients that a Group should have');
INSERT INTO public.c_configuration (id, name, value, date_value, string_value, enabled, is_trap_door, description) VALUES ('18', 'max-clients-in-group', '5', NULL, NULL, 'f', 'f', 'Maximum number of Clients that a Group can have');
INSERT INTO public.c_configuration (id, name, value, date_value, string_value, enabled, is_trap_door, description) VALUES ('19', 'meetings-mandatory-for-jlg-loans', NULL, NULL, NULL, 'f', 'f', 'Enforces all JLG loans to follow a meeting schedule belonging to parent group or Center');
INSERT INTO public.c_configuration (id, name, value, date_value, string_value, enabled, is_trap_door, description) VALUES ('20', 'office-specific-products-enabled', '0', NULL, NULL, 'f', 'f', 'Whether products and fees should be office specific or not? This property should NOT be changed once Mifos is Live.');
INSERT INTO public.c_configuration (id, name, value, date_value, string_value, enabled, is_trap_door, description) VALUES ('21', 'restrict-products-to-user-office', '0', NULL, NULL, 'f', 'f', 'This should be enabled only if, products & fees are office specific (i.e. office-specific-products-enabled is enabled). This property specifies if the products should be auto-restricted to office of the user who created the proudct? Note: This property should NOT be changed once Mifos is Live.');
INSERT INTO public.c_configuration (id, name, value, date_value, string_value, enabled, is_trap_door, description) VALUES ('22', 'office-opening-balances-contra-account', '0', NULL, NULL, 't', 'f', NULL);
INSERT INTO public.c_configuration (id, name, value, date_value, string_value, enabled, is_trap_door, description) VALUES ('23', 'rounding-mode', '6', NULL, NULL, 't', 't', '0 - UP, 1 - DOWN, 2- CEILING, 3- FLOOR, 4- HALF_UP, 5- HALF_DOWN, 6 - HALF_EVEN');
INSERT INTO public.c_configuration (id, name, value, date_value, string_value, enabled, is_trap_door, description) VALUES ('24', 'backdate-penalties-enabled', '0', NULL, NULL, 't', 'f', 'If this parameter is disabled penalties will only be added to instalments due moving forward, any old overdue instalments will not be affected.');
INSERT INTO public.c_configuration (id, name, value, date_value, string_value, enabled, is_trap_door, description) VALUES ('25', 'organisation-start-date', '0', NULL, NULL, 'f', 'f', NULL);
INSERT INTO public.c_configuration (id, name, value, date_value, string_value, enabled, is_trap_door, description) VALUES ('26', 'paymenttype-applicable-for-disbursement-charges', NULL, NULL, NULL, 'f', 'f', 'Is the Disbursement Entry need to be considering the fund source of the paymnet type');
INSERT INTO public.c_configuration (id, name, value, date_value, string_value, enabled, is_trap_door, description) VALUES ('27', 'interest-charged-from-date-same-as-disbursal-date', '0', NULL, NULL, 'f', 'f', NULL);
INSERT INTO public.c_configuration (id, name, value, date_value, string_value, enabled, is_trap_door, description) VALUES ('28', 'skip-repayment-on-first-day-of-month', '14', NULL, NULL, 'f', 'f', 'skipping repayment on first day of month');
INSERT INTO public.c_configuration (id, name, value, date_value, string_value, enabled, is_trap_door, description) VALUES ('29', 'change-emi-if-repaymentdate-same-as-disbursementdate', '0', NULL, NULL, 't', 'f', 'In tranche loans, if repayment date is same as tranche disbursement date then allow to change the emi amount');
INSERT INTO public.c_configuration (id, name, value, date_value, string_value, enabled, is_trap_door, description) VALUES ('30', 'daily-tpt-limit', '0', NULL, NULL, 'f', 'f', 'Daily limit for third party transfers');
INSERT INTO public.c_configuration (id, name, value, date_value, string_value, enabled, is_trap_door, description) VALUES ('31', 'enable-address', NULL, NULL, NULL, 'f', 'f', NULL);
INSERT INTO public.c_configuration (id, name, value, date_value, string_value, enabled, is_trap_door, description) VALUES ('32', 'sub-rates', '0', NULL, NULL, 'f', 'f', 'Enable Rates Module');
INSERT INTO public.c_configuration (id, name, value, date_value, string_value, enabled, is_trap_door, description) VALUES ('33', 'loan-reschedule-is-first-payday-allowed-on-holiday', '0', NULL, NULL, 'f', 'f', 'If enabled, while loan reschedule the first repayment date can be on a holiday/non working day');
INSERT INTO public.c_configuration (id, name, value, date_value, string_value, enabled, is_trap_door, description) VALUES ('35', 'account-mapping-for-payment-type', NULL, NULL, 'Asset', 't', 'f', 'Asset: default for asset, Use comma seperated values for Liability, Asset and Expense accounts');
INSERT INTO public.c_configuration (id, name, value, date_value, string_value, enabled, is_trap_door, description) VALUES ('36', 'account-mapping-for-charge', NULL, NULL, 'Income', 't', 'f', 'Income: default for Income, Use comma seperated values for Income, Liability and Expense accounts');
INSERT INTO public.c_configuration (id, name, value, date_value, string_value, enabled, is_trap_door, description) VALUES ('37', 'fixed-deposit-transfer-interest-next-day-for-period-end-posting', NULL, NULL, NULL, 'f', 'f', 'Transfer fixed transfer interest next day(t+1) for period end posting');
INSERT INTO public.c_configuration (id, name, value, date_value, string_value, enabled, is_trap_door, description) VALUES ('38', 'allow-backdated-transaction-before-interest-posting', '0', NULL, NULL, 't', 'f', 'Avoid retrieving all transactions in a savings account');
INSERT INTO public.c_configuration (id, name, value, date_value, string_value, enabled, is_trap_door, description) VALUES ('39', 'allow-backdated-transaction-before-interest-posting-date-for-days', '0', NULL, NULL, 'f', 'f', 'One time configuration to relax the backdated transactions');
INSERT INTO public.c_configuration (id, name, value, date_value, string_value, enabled, is_trap_door, description) VALUES ('40', 'custom-account-number-length', NULL, NULL, NULL, 'f', 'f', 'if enabled, the value if this configuration will set accounnumber length');
INSERT INTO public.c_configuration (id, name, value, date_value, string_value, enabled, is_trap_door, description) VALUES ('41', 'random-account-number', NULL, NULL, NULL, 'f', 'f', 'if enabled, the client accounts, saving accounts, loan accounts will be created with Random Account Number');
INSERT INTO public.c_configuration (id, name, value, date_value, string_value, enabled, is_trap_door, description) VALUES ('42', 'is-interest-to-be-recovered-first-when-greater-than-emi', '0', NULL, NULL, 'f', 'f', 'If enabled, when interest amount is greater than EMI, the additional interest is recovered first before principal');
INSERT INTO public.c_configuration (id, name, value, date_value, string_value, enabled, is_trap_door, description) VALUES ('43', 'is-principal-compounding-disabled-for-overdue-loans', '0', NULL, NULL, 'f', 'f', 'If enabled, it donot consider principal of an unpaid installment for calculating interest of next installment. this is for testing back-dated loan schedule');
INSERT INTO public.c_configuration (id, name, value, date_value, string_value, enabled, is_trap_door, description) VALUES ('44', 'enable-business-date', NULL, NULL, NULL, 'f', 'f', 'Whether the logical business date functionality is enabled in the system');
INSERT INTO public.c_configuration (id, name, value, date_value, string_value, enabled, is_trap_door, description) VALUES ('45', 'enable-automatic-cob-date-adjustment', NULL, NULL, NULL, 't', 'f', 'Whether the cob date will be automatically recalculated based on the business date');
INSERT INTO public.c_configuration (id, name, value, date_value, string_value, enabled, is_trap_door, description) VALUES ('46', 'enable-post-reversal-txns-for-reverse-transactions', NULL, NULL, NULL, 'f', 'f', NULL);
INSERT INTO public.c_configuration (id, name, value, date_value, string_value, enabled, is_trap_door, description) VALUES ('47', 'purge-external-events-older-than-days', '30', NULL, NULL, 'f', 'f', 'Number of days criteria to purge old external events sent to message channel');
INSERT INTO public.c_configuration (id, name, value, date_value, string_value, enabled, is_trap_door, description) VALUES ('48', 'days-before-repayment-is-due', '1', NULL, NULL, 'f', 'f', 'Number of days before repayment is due to raise event');
INSERT INTO public.c_configuration (id, name, value, date_value, string_value, enabled, is_trap_door, description) VALUES ('49', 'days-after-repayment-is-overdue', '1', NULL, NULL, 'f', 'f', 'Number of days after repayment overdue to raise event');
INSERT INTO public.c_configuration (id, name, value, date_value, string_value, enabled, is_trap_door, description) VALUES ('50', 'enable-auto-generated-external-id', NULL, NULL, NULL, 'f', 'f', NULL);
INSERT INTO public.c_configuration (id, name, value, date_value, string_value, enabled, is_trap_door, description) VALUES ('51', 'purge-processed-commands-older-than-days', '30', NULL, NULL, 'f', 'f', 'Number of days criteria to purge old processed commands');
INSERT INTO public.c_configuration (id, name, value, date_value, string_value, enabled, is_trap_door, description) VALUES ('52', 'enable-cob-bulk-event', NULL, NULL, NULL, 'f', 'f', 'Whether bulk event for COB is enabled in the system');
INSERT INTO public.c_configuration (id, name, value, date_value, string_value, enabled, is_trap_door, description) VALUES ('53', 'external-event-batch-size', '1000', NULL, NULL, 'f', 'f', 'External event producer batch size');
INSERT INTO public.c_configuration (id, name, value, date_value, string_value, enabled, is_trap_door, description) VALUES ('54', 'report-export-s3-folder-name', NULL, NULL, 'reports', 't', 'f', NULL);
INSERT INTO public.c_configuration (id, name, value, date_value, string_value, enabled, is_trap_door, description) VALUES ('55', 'loan-arrears-delinquency-display-data', '0', NULL, NULL, 't', 'f', '0 - Both, 1 - Arrears, 2- Delinquency');
INSERT INTO public.c_configuration (id, name, value, date_value, string_value, enabled, is_trap_door, description) VALUES ('56', 'charge-accrual-date', NULL, NULL, 'due-date', 't', 'f', 'due-date: default for due-date, Use comma seperated values for due-date, submitted-date');
INSERT INTO public.c_configuration (id, name, value, date_value, string_value, enabled, is_trap_door, description) VALUES ('57', 'asset-externalization-of-non-active-loans', NULL, NULL, NULL, 't', 'f', 'If enabled: when a loan state is changed to non-active -> pending transfers will be handled');
INSERT INTO public.c_configuration (id, name, value, date_value, string_value, enabled, is_trap_door, description) VALUES ('58', 'enable-same-maker-checker', NULL, NULL, NULL, 'f', 'f', NULL);
INSERT INTO public.c_configuration (id, name, value, date_value, string_value, enabled, is_trap_door, description) VALUES ('59', 'next-payment-due-date', NULL, NULL, 'earliest-unpaid-date', 't', 'f', 'earliest-unpaid-date: default for next-payment-due-date, Use earliest-unpaid-date or next-unpaid-due-date');
INSERT INTO public.c_configuration (id, name, value, date_value, string_value, enabled, is_trap_door, description) VALUES ('60', 'enable-payment-hub-integration', '0', NULL, 'enable payment hub integration', 'f', 'f', 'Use payment hub api''s for account withdrawal and loan disbursement to linked interop account');
INSERT INTO public.c_configuration (id, name, value, date_value, string_value, enabled, is_trap_door, description) VALUES ('61', 'enable-immediate-charge-accrual-post-maturity', NULL, NULL, NULL, 'f', 'f', 'Whether the system creates accruals immediately for charge creation after the maturity date');
INSERT INTO public.c_configuration (id, name, value, date_value, string_value, enabled, is_trap_door, description) VALUES ('62', 'password-reuse-check-history-count', '3', NULL, NULL, 'f', 'f', 'When enabled, prevents password reuse. The value specifies how many previous passwords to check (e.g., 3 = last 3 passwords). Set to 0 to check ALL previous passwords. Disable this setting to allow password reuse.');
INSERT INTO public.c_configuration (id, name, value, date_value, string_value, enabled, is_trap_door, description) VALUES ('63', 'force-password-reset-on-first-login', '0', NULL, NULL, 'f', 'f', 'If enabled, users must reset their password upon first login or after an admin reset. Value is unused.');
INSERT INTO public.c_configuration (id, name, value, date_value, string_value, enabled, is_trap_door, description) VALUES ('64', 'allow-force-withdrawal-on-savings-account', '0', NULL, NULL, 'f', 'f', 'If enabled, allows withdrawals to put the account into negative balance up to the configured limit.');
INSERT INTO public.c_configuration (id, name, value, date_value, string_value, enabled, is_trap_door, description) VALUES ('65', 'force-withdrawal-on-savings-account-limit', '0', NULL, NULL, 'f', 'f', 'The maximum negative balance allowed when force withdrawal is enabled.');
INSERT INTO public.c_configuration (id, name, value, date_value, string_value, enabled, is_trap_door, description) VALUES ('66', 'max-login-retry-attempts', '5', NULL, NULL, 'f', 'f', 'Maximum number of failed login attempts before an account is locked');
INSERT INTO public.c_configuration (id, name, value, date_value, string_value, enabled, is_trap_door, description) VALUES ('67', 'allow-cash-and-non-cash-accrual', '0', NULL, NULL, 't', 'f', 'When enabled (true), accrual transactions are created at disbursement for None, Cash, and Upfront Accrual accounting types (legacy behavior). When disabled (false), accrual transactions at disbursement are created only for Upfront Accrual accounting type.');
INSERT INTO public.c_configuration (id, name, value, date_value, string_value, enabled, is_trap_door, description) VALUES ('68', 'block-transactions-on-closed-overpaid-loans', NULL, NULL, NULL, 'f', 'f', 'If enabled: monetary transactions are blocked on closed and overpaid loan accounts');
INSERT INTO public.c_configuration (id, name, value, date_value, string_value, enabled, is_trap_door, description) VALUES ('69', 'last-day-of-financial-year', '31', NULL, NULL, 't', 'f', '31: default for last day of fiscal year');
INSERT INTO public.c_configuration (id, name, value, date_value, string_value, enabled, is_trap_door, description) VALUES ('70', 'last-month-of-financial-year', '12', NULL, NULL, 't', 'f', '12: default for last month of fiscal year');
INSERT INTO public.c_configuration (id, name, value, date_value, string_value, enabled, is_trap_door, description) VALUES ('71', 'income-expense-gl-accounts', NULL, NULL, '', 't', 'f', 'List of income/expense GL account codes for retained earning calculation. Can provide multiple accounts separated by comma or ranges separated by dashes (e.g. 4000,5000-5999,7000). Empty by default, please set before executing job');
INSERT INTO public.c_configuration (id, name, value, date_value, string_value, enabled, is_trap_door, description) VALUES ('72', 'retained-gl-account', NULL, NULL, '', 't', 'f', 'Default retained gl account code. Empty by default, please set before executing job');
INSERT INTO public.c_configuration (id, name, value, date_value, string_value, enabled, is_trap_door, description) VALUES ('73', 'office-id', '1', NULL, NULL, 't', 'f', 'OfficeId for which the report and the retained earning job will be executed');
INSERT INTO public.c_configuration (id, name, value, date_value, string_value, enabled, is_trap_door, description) VALUES ('74', 'retained-earning-used-by-report-name', NULL, NULL, 'Trial Balance Summary Report with Asset Owner', 't', 'f', 'Report name which will be using using retained earning');


-- Data for Name: c_external_service; Type: TABLE DATA; Schema: public; Owner: -

INSERT INTO public.c_external_service (id, name) VALUES ('3', 'MESSAGE_GATEWAY');
INSERT INTO public.c_external_service (id, name) VALUES ('4', 'NOTIFICATION');
INSERT INTO public.c_external_service (id, name) VALUES ('1', 'S3');
INSERT INTO public.c_external_service (id, name) VALUES ('2', 'SMTP_Email_Account');


-- Data for Name: c_external_service_properties; Type: TABLE DATA; Schema: public; Owner: -

INSERT INTO public.c_external_service_properties (name, value, external_service_id) VALUES ('s3_access_key', NULL, '1');
INSERT INTO public.c_external_service_properties (name, value, external_service_id) VALUES ('s3_bucket_name', NULL, '1');
INSERT INTO public.c_external_service_properties (name, value, external_service_id) VALUES ('s3_secret_key', NULL, '1');
INSERT INTO public.c_external_service_properties (name, value, external_service_id) VALUES ('username', 'support@cloudmicrofinance.com', '2');
INSERT INTO public.c_external_service_properties (name, value, external_service_id) VALUES ('password', 'support81', '2');
INSERT INTO public.c_external_service_properties (name, value, external_service_id) VALUES ('host', 'localhost', '2');
INSERT INTO public.c_external_service_properties (name, value, external_service_id) VALUES ('port', '3025', '2');
INSERT INTO public.c_external_service_properties (name, value, external_service_id) VALUES ('useTLS', 'true', '2');
INSERT INTO public.c_external_service_properties (name, value, external_service_id) VALUES ('host_name', 'localhost', '3');
INSERT INTO public.c_external_service_properties (name, value, external_service_id) VALUES ('port_number', '9191', '3');
INSERT INTO public.c_external_service_properties (name, value, external_service_id) VALUES ('end_point', '/', '3');
INSERT INTO public.c_external_service_properties (name, value, external_service_id) VALUES ('tenant_app_key', NULL, '3');
INSERT INTO public.c_external_service_properties (name, value, external_service_id) VALUES ('server_key', 'AAAAToBmqQQ:APA91bEodkE12CwFl8VHqanUbeJYg1E05TiheVz59CZZYrBnCq3uM40UYhHfdP-JfeTQ0L0zoLqS8orjvW_ze0_VF8DSuyyqkrDibflhtUainsI0lwgVz5u1YP3q3c3erqjlySEuRShS', '4');
INSERT INTO public.c_external_service_properties (name, value, external_service_id) VALUES ('gcm_end_point', 'https://gcm-http.googleapis.com/gcm/send', '4');
INSERT INTO public.c_external_service_properties (name, value, external_service_id) VALUES ('fcm_end_point', 'https://fcm.googleapis.com/fcm/send', '4');
INSERT INTO public.c_external_service_properties (name, value, external_service_id) VALUES ('fromEmail', 'support@cloudmicrofinance.com', '2');
INSERT INTO public.c_external_service_properties (name, value, external_service_id) VALUES ('fromName', 'support@cloudmicrofinance.com', '2');






-- Data for Name: interop_identifier; Type: TABLE DATA; Schema: public; Owner: -



-- Data for Name: job; Type: TABLE DATA; Schema: public; Owner: -

INSERT INTO public.job (id, name, display_name, cron_expression, create_time, task_priority, group_name, previous_run_start_time, next_run_time, job_key, initializing_errorlog, is_active, currently_running, updates_allowed, scheduler_group, is_misfired, node_id, is_mismatched_job, short_name) VALUES ('2', 'Update Loan Arrears Ageing', 'Update Loan Arrears Ageing', '0 1 0 1/1 * ? *', '2026-08-21 12:32:42.944851', '5', NULL, NULL, NULL, 'Update Loan Arrears AgeingJobDetail1 _ DEFAULT', NULL, 't', 'f', 't', '0', 'f', '1', 't', 'LA_ARAG');
INSERT INTO public.job (id, name, display_name, cron_expression, create_time, task_priority, group_name, previous_run_start_time, next_run_time, job_key, initializing_errorlog, is_active, currently_running, updates_allowed, scheduler_group, is_misfired, node_id, is_mismatched_job, short_name) VALUES ('5', 'Apply Holidays To Loans', 'Apply Holidays To Loans', '0 0 12 * * ?', '2026-08-21 12:32:42.944851', '5', NULL, NULL, NULL, 'Apply Holidays To LoansJobDetail1 _ DEFAULT', NULL, 't', 'f', 't', '0', 'f', '1', 't', 'LA_AHOL');
INSERT INTO public.job (id, name, display_name, cron_expression, create_time, task_priority, group_name, previous_run_start_time, next_run_time, job_key, initializing_errorlog, is_active, currently_running, updates_allowed, scheduler_group, is_misfired, node_id, is_mismatched_job, short_name) VALUES ('7', 'Transfer Fee For Loans From Savings', 'Transfer Fee For Loans From Savings', '0 1 0 1/1 * ? *', '2026-08-21 12:32:42.944851', '5', NULL, NULL, NULL, 'Transfer Fee For Loans From SavingsJobDetail1 _ DEFAULT', NULL, 't', 'f', 't', '0', 'f', '1', 't', 'LA_TFFS');
INSERT INTO public.job (id, name, display_name, cron_expression, create_time, task_priority, group_name, previous_run_start_time, next_run_time, job_key, initializing_errorlog, is_active, currently_running, updates_allowed, scheduler_group, is_misfired, node_id, is_mismatched_job, short_name) VALUES ('39', 'Accrual Activity Posting', 'Accrual Activity Posting', '0 0 1 * * ?', '2026-08-21 12:32:46.937149', '5', NULL, NULL, NULL, 'Accrual Activity Posting1 _ DEFAULT', NULL, 'f', 'f', 't', '0', 'f', '1', 't', 'ACC_ACPO');
INSERT INTO public.job (id, name, display_name, cron_expression, create_time, task_priority, group_name, previous_run_start_time, next_run_time, job_key, initializing_errorlog, is_active, currently_running, updates_allowed, scheduler_group, is_misfired, node_id, is_mismatched_job, short_name) VALUES ('12', 'Apply penalty to overdue loans', 'Apply penalty to overdue loans', '0 0 0 1/1 * ? *', '2026-08-21 12:32:42.944851', '5', NULL, NULL, NULL, 'Apply penalty to overdue loansJobDetail1 _ DEFAULT', NULL, 't', 'f', 't', '0', 'f', '1', 't', 'LA_OPEN');
INSERT INTO public.job (id, name, display_name, cron_expression, create_time, task_priority, group_name, previous_run_start_time, next_run_time, job_key, initializing_errorlog, is_active, currently_running, updates_allowed, scheduler_group, is_misfired, node_id, is_mismatched_job, short_name) VALUES ('13', 'Update Non Performing Assets', 'Update Non Performing Assets', '0 0 0 1/1 * ? *', '2026-08-21 12:32:42.944851', '6', NULL, NULL, NULL, 'Update Non Performing AssetsJobDetail1 _ DEFAULT', NULL, 't', 'f', 't', '3', 'f', '1', 't', 'LA_UNPA');
INSERT INTO public.job (id, name, display_name, cron_expression, create_time, task_priority, group_name, previous_run_start_time, next_run_time, job_key, initializing_errorlog, is_active, currently_running, updates_allowed, scheduler_group, is_misfired, node_id, is_mismatched_job, short_name) VALUES ('17', 'Recalculate Interest For Loans', 'Recalculate Interest For Loans', '0 1 0 1/1 * ? *', '2026-08-21 12:32:42.944851', '4', NULL, NULL, NULL, 'Recalculate Interest For LoansJobDetail1 _ DEFAULT', NULL, 't', 'f', 't', '3', 'f', '1', 't', 'LA_RINT');
INSERT INTO public.job (id, name, display_name, cron_expression, create_time, task_priority, group_name, previous_run_start_time, next_run_time, job_key, initializing_errorlog, is_active, currently_running, updates_allowed, scheduler_group, is_misfired, node_id, is_mismatched_job, short_name) VALUES ('19', 'Generate Loan Loss Provisioning', 'Generate Loan Loss Provisioning', '0 0 0 1/1 * ? *', '2026-08-21 12:32:42.944851', '5', NULL, NULL, NULL, 'Generate Loan Loss ProvisioningJobDetail1 _ DEFAULT', NULL, 't', 'f', 't', '0', 'f', '1', 't', 'LA_GLPR');
INSERT INTO public.job (id, name, display_name, cron_expression, create_time, task_priority, group_name, previous_run_start_time, next_run_time, job_key, initializing_errorlog, is_active, currently_running, updates_allowed, scheduler_group, is_misfired, node_id, is_mismatched_job, short_name) VALUES ('22', 'Add Accrual Transactions For Loans With Income Posted As Transactions', 'Add Accrual Transactions For Loans With Income Posted As Transactions', '0 1 0 1/1 * ? *', '2026-08-21 12:32:42.944851', '5', NULL, NULL, NULL, 'Add Accrual Transactions For Loans With Income Posted As TransactionsJobDetail1 _ DEFAULT', NULL, 't', 'f', 't', '3', 'f', '1', 't', 'LA_AATR');
INSERT INTO public.job (id, name, display_name, cron_expression, create_time, task_priority, group_name, previous_run_start_time, next_run_time, job_key, initializing_errorlog, is_active, currently_running, updates_allowed, scheduler_group, is_misfired, node_id, is_mismatched_job, short_name) VALUES ('34', 'Loan COB', 'Loan COB', '0 0 0 * * ?', '2026-08-21 12:32:45.751405', '5', NULL, NULL, NULL, 'Loan COB dayJobDetail1 _ DEFAULT', NULL, 'f', 'f', 't', '0', 'f', '1', 'f', 'LA_ECOB');
INSERT INTO public.job (id, name, display_name, cron_expression, create_time, task_priority, group_name, previous_run_start_time, next_run_time, job_key, initializing_errorlog, is_active, currently_running, updates_allowed, scheduler_group, is_misfired, node_id, is_mismatched_job, short_name) VALUES ('35', 'Loan Delinquency Classification', 'Loan Delinquency Classification', '0 0 22 1/1 * ? *', '2026-08-21 12:32:45.783257', '5', NULL, NULL, NULL, 'Loan Delinquency ClassificationDetail1 _ DEFAULT', NULL, 't', 'f', 't', '0', 'f', '1', 't', 'LA_DECL');
INSERT INTO public.job (id, name, display_name, cron_expression, create_time, task_priority, group_name, previous_run_start_time, next_run_time, job_key, initializing_errorlog, is_active, currently_running, updates_allowed, scheduler_group, is_misfired, node_id, is_mismatched_job, short_name) VALUES ('4', 'Apply Annual Fee For Savings', 'Apply Annual Fee For Savings', '0 20 22 1/1 * ? *', '2026-08-21 12:32:42.944851', '5', NULL, NULL, NULL, 'Apply Annual Fee For SavingsJobDetail1 _ DEFAULT', NULL, 't', 'f', 't', '0', 'f', '1', 't', 'SA_AANF');
INSERT INTO public.job (id, name, display_name, cron_expression, create_time, task_priority, group_name, previous_run_start_time, next_run_time, job_key, initializing_errorlog, is_active, currently_running, updates_allowed, scheduler_group, is_misfired, node_id, is_mismatched_job, short_name) VALUES ('6', 'Post Interest For Savings', 'Post Interest For Savings', '0 0 0 1/1 * ? *', '2026-08-21 12:32:42.944851', '5', NULL, NULL, NULL, 'Post Interest For SavingsJobDetail1 _ DEFAULT', NULL, 't', 'f', 't', '1', 'f', '1', 't', 'SA_PINT');
INSERT INTO public.job (id, name, display_name, cron_expression, create_time, task_priority, group_name, previous_run_start_time, next_run_time, job_key, initializing_errorlog, is_active, currently_running, updates_allowed, scheduler_group, is_misfired, node_id, is_mismatched_job, short_name) VALUES ('8', 'Pay Due Savings Charges', 'Pay Due Savings Charges', '0 0 12 * * ?', '2013-09-23 00:00:00', '5', NULL, NULL, NULL, 'Pay Due Savings ChargesJobDetail1 _ DEFAULT', NULL, 't', 'f', 't', '0', 'f', '1', 't', 'SA_PDCH');
INSERT INTO public.job (id, name, display_name, cron_expression, create_time, task_priority, group_name, previous_run_start_time, next_run_time, job_key, initializing_errorlog, is_active, currently_running, updates_allowed, scheduler_group, is_misfired, node_id, is_mismatched_job, short_name) VALUES ('14', 'Transfer Interest To Savings', 'Transfer Interest To Savings', '0 2 0 1/1 * ? *', '2026-08-21 12:32:42.944851', '4', NULL, NULL, NULL, 'Transfer Interest To SavingsJobDetail1 _ DEFAULT', NULL, 't', 'f', 't', '1', 'f', '1', 't', 'SA_TINT');
INSERT INTO public.job (id, name, display_name, cron_expression, create_time, task_priority, group_name, previous_run_start_time, next_run_time, job_key, initializing_errorlog, is_active, currently_running, updates_allowed, scheduler_group, is_misfired, node_id, is_mismatched_job, short_name) VALUES ('15', 'Update Deposit Accounts Maturity details', 'Update Deposit Accounts Maturity details', '0 0 0 1/1 * ? *', '2026-08-21 12:32:42.944851', '5', NULL, NULL, NULL, 'Update Deposit Accounts Maturity detailsJobDetail1 _ DEFAULT', NULL, 't', 'f', 't', '0', 'f', '1', 't', 'SA_MATD');
INSERT INTO public.job (id, name, display_name, cron_expression, create_time, task_priority, group_name, previous_run_start_time, next_run_time, job_key, initializing_errorlog, is_active, currently_running, updates_allowed, scheduler_group, is_misfired, node_id, is_mismatched_job, short_name) VALUES ('18', 'Generate Mandatory Savings Schedule', 'Generate Mandatory Savings Schedule', '0 5 0 1/1 * ? *', '2026-08-21 12:32:42.944851', '5', NULL, NULL, NULL, 'Generate Mandatory Savings ScheduleJobDetail1 _ DEFAULT', NULL, 't', 'f', 't', '0', 'f', '1', 't', 'SA_GSCH');
INSERT INTO public.job (id, name, display_name, cron_expression, create_time, task_priority, group_name, previous_run_start_time, next_run_time, job_key, initializing_errorlog, is_active, currently_running, updates_allowed, scheduler_group, is_misfired, node_id, is_mismatched_job, short_name) VALUES ('21', 'Update Savings Dormant Accounts', 'Update Savings Dormant Accounts', '0 0 0 1/1 * ? *', '2026-08-21 12:32:42.944851', '3', NULL, NULL, NULL, 'Update Savings Dormant AccountsJobDetail1 _ DEFAULT', NULL, 't', 'f', 't', '1', 'f', '1', 't', 'SA_UDOR');
INSERT INTO public.job (id, name, display_name, cron_expression, create_time, task_priority, group_name, previous_run_start_time, next_run_time, job_key, initializing_errorlog, is_active, currently_running, updates_allowed, scheduler_group, is_misfired, node_id, is_mismatched_job, short_name) VALUES ('20', 'Post Dividends For Shares', 'Post Dividends For Shares', '0 0 0 1/1 * ? *', '2026-08-21 12:32:42.944851', '5', NULL, NULL, NULL, 'Post Dividends For SharesJobDetail1 _ DEFAULT', NULL, 't', 'f', 't', '0', 'f', '1', 't', 'SH_PDIV');
INSERT INTO public.job (id, name, display_name, cron_expression, create_time, task_priority, group_name, previous_run_start_time, next_run_time, job_key, initializing_errorlog, is_active, currently_running, updates_allowed, scheduler_group, is_misfired, node_id, is_mismatched_job, short_name) VALUES ('9', 'Update Accounting Running Balances', 'Update Accounting Running Balances', '0 1 0 1/1 * ? *', '2026-08-21 12:32:42.944851', '5', NULL, NULL, NULL, 'Update Accounting Running BalancesJobDetail1 _ DEFAULT', NULL, 't', 'f', 't', '0', 'f', '1', 't', 'ACC_RBAL');
INSERT INTO public.job (id, name, display_name, cron_expression, create_time, task_priority, group_name, previous_run_start_time, next_run_time, job_key, initializing_errorlog, is_active, currently_running, updates_allowed, scheduler_group, is_misfired, node_id, is_mismatched_job, short_name) VALUES ('11', 'Add Accrual Transactions', 'Add Accrual Transactions', '0 1 0 1/1 * ? *', '2026-08-21 12:32:42.944851', '3', NULL, NULL, NULL, 'Add Accrual TransactionsJobDetail1 _ DEFAULT', NULL, 't', 'f', 't', '3', 'f', '1', 't', 'ACC_AATR');
INSERT INTO public.job (id, name, display_name, cron_expression, create_time, task_priority, group_name, previous_run_start_time, next_run_time, job_key, initializing_errorlog, is_active, currently_running, updates_allowed, scheduler_group, is_misfired, node_id, is_mismatched_job, short_name) VALUES ('16', 'Add Periodic Accrual Transactions', 'Add Periodic Accrual Transactions', '0 2 0 1/1 * ? *', '2026-08-21 12:32:42.944851', '2', NULL, NULL, NULL, 'Add Periodic Accrual TransactionsJobDetail1 _ DEFAULT', NULL, 't', 'f', 't', '3', 'f', '1', 't', 'ACC_APTR');
INSERT INTO public.job (id, name, display_name, cron_expression, create_time, task_priority, group_name, previous_run_start_time, next_run_time, job_key, initializing_errorlog, is_active, currently_running, updates_allowed, scheduler_group, is_misfired, node_id, is_mismatched_job, short_name) VALUES ('10', 'Execute Standing Instruction', 'Execute Standing Instruction', '0 0 0 1/1 * ? *', '2026-08-21 12:32:42.944851', '5', NULL, NULL, NULL, 'Execute Standing InstructionJobDetail1 _ DEFAULT', NULL, 't', 'f', 't', '0', 'f', '1', 't', 'STI_EXEC');
INSERT INTO public.job (id, name, display_name, cron_expression, create_time, task_priority, group_name, previous_run_start_time, next_run_time, job_key, initializing_errorlog, is_active, currently_running, updates_allowed, scheduler_group, is_misfired, node_id, is_mismatched_job, short_name) VALUES ('23', 'Execute Report Mailing Jobs', 'Execute Report Mailing Jobs', '0 0/15 * * * ?', '2026-08-21 12:32:42.944851', '5', NULL, NULL, NULL, 'Execute Report Mailing JobsJobDetail1 _ DEFAULT', NULL, 't', 'f', 't', '0', 'f', '1', 't', 'RMJ_EXEC');
INSERT INTO public.job (id, name, display_name, cron_expression, create_time, task_priority, group_name, previous_run_start_time, next_run_time, job_key, initializing_errorlog, is_active, currently_running, updates_allowed, scheduler_group, is_misfired, node_id, is_mismatched_job, short_name) VALUES ('27', 'Execute Email', 'Execute Email', '0 0/10 * * * ?', '2026-08-21 12:32:42.944851', '5', NULL, NULL, NULL, 'Execute EmailJobDetail1 _ DEFAULT', NULL, 't', 'f', 't', '0', 'f', '1', 't', 'EM_EXEC');
INSERT INTO public.job (id, name, display_name, cron_expression, create_time, task_priority, group_name, previous_run_start_time, next_run_time, job_key, initializing_errorlog, is_active, currently_running, updates_allowed, scheduler_group, is_misfired, node_id, is_mismatched_job, short_name) VALUES ('28', 'Update Email Outbound with campaign message', 'Update Email Outbound with campaign message', '0 0/15 * * * ?', '2026-08-21 12:32:42.944851', '5', NULL, NULL, NULL, 'Update Email Outbound with campaign messageJobDetail1 _ DEFAULT', NULL, 't', 'f', 't', '0', 'f', '1', 't', 'EM_UOUT');
INSERT INTO public.job (id, name, display_name, cron_expression, create_time, task_priority, group_name, previous_run_start_time, next_run_time, job_key, initializing_errorlog, is_active, currently_running, updates_allowed, scheduler_group, is_misfired, node_id, is_mismatched_job, short_name) VALUES ('24', 'Update SMS Outbound with Campaign Message', 'Update SMS Outbound with Campaign Message', '0 0 5 1/1 * ? *', '2026-08-21 12:32:42.944851', '3', NULL, NULL, NULL, 'Update SMS Outbound with Campaign MessageJobDetail1 _ DEFAULT', NULL, 't', 'f', 't', '4', 'f', '1', 't', 'SMS_UOUT');
INSERT INTO public.job (id, name, display_name, cron_expression, create_time, task_priority, group_name, previous_run_start_time, next_run_time, job_key, initializing_errorlog, is_active, currently_running, updates_allowed, scheduler_group, is_misfired, node_id, is_mismatched_job, short_name) VALUES ('25', 'Send Messages to SMS Gateway', 'Send Messages to SMS Gateway', '0 0 5 1/1 * ? *', '2026-08-21 12:32:42.944851', '2', NULL, NULL, NULL, 'Send Messages to SMS GatewayJobDetail1 _ DEFAULT', NULL, 't', 'f', 't', '4', 'f', '1', 't', 'SMS_SMSG');
INSERT INTO public.job (id, name, display_name, cron_expression, create_time, task_priority, group_name, previous_run_start_time, next_run_time, job_key, initializing_errorlog, is_active, currently_running, updates_allowed, scheduler_group, is_misfired, node_id, is_mismatched_job, short_name) VALUES ('26', 'Get Delivery Reports from SMS Gateway', 'Get Delivery Reports from SMS Gateway', '0 0 5 1/1 * ? *', '2026-08-21 12:32:42.944851', '1', NULL, NULL, NULL, 'Get Delivery Reports from SMS GatewayJobDetail1 _ DEFAULT', NULL, 't', 'f', 't', '4', 'f', '1', 't', 'SMS_DRPT');
INSERT INTO public.job (id, name, display_name, cron_expression, create_time, task_priority, group_name, previous_run_start_time, next_run_time, job_key, initializing_errorlog, is_active, currently_running, updates_allowed, scheduler_group, is_misfired, node_id, is_mismatched_job, short_name) VALUES ('29', 'Generate AdhocClient Schedule', 'Generate AdhocClient Schedule', '0 0 12 1/1 * ? *', '2026-08-21 12:32:42.944851', '5', NULL, NULL, NULL, 'Generate AdhocClient ScheduleJobDetail1 _ DEFAULT', NULL, 't', 'f', 't', '0', 'f', '1', 't', 'ADH_GSCH');
INSERT INTO public.job (id, name, display_name, cron_expression, create_time, task_priority, group_name, previous_run_start_time, next_run_time, job_key, initializing_errorlog, is_active, currently_running, updates_allowed, scheduler_group, is_misfired, node_id, is_mismatched_job, short_name) VALUES ('30', 'Update Trial Balance Details', 'Update Trial Balance Details', '0 1 0 1/1 * ? *', '2026-08-21 12:32:42.944851', '5', NULL, NULL, NULL, 'Update Trial Balance DetailsJobDetail1 _ DEFAULT', NULL, 't', 'f', 't', '0', 'f', '1', 't', 'TBL_UDET');
INSERT INTO public.job (id, name, display_name, cron_expression, create_time, task_priority, group_name, previous_run_start_time, next_run_time, job_key, initializing_errorlog, is_active, currently_running, updates_allowed, scheduler_group, is_misfired, node_id, is_mismatched_job, short_name) VALUES ('31', 'Execute All Dirty Jobs', 'Execute All Dirty Jobs', '0 1 0 1/1 * ? *', '2026-08-21 12:32:42.944851', '5', NULL, NULL, NULL, 'Execute All Dirty JobsJobDetail1 _ DEFAULT', NULL, 't', 'f', 't', '0', 'f', '0', 'f', 'JOB_EXEC');
INSERT INTO public.job (id, name, display_name, cron_expression, create_time, task_priority, group_name, previous_run_start_time, next_run_time, job_key, initializing_errorlog, is_active, currently_running, updates_allowed, scheduler_group, is_misfired, node_id, is_mismatched_job, short_name) VALUES ('32', 'Increase Business Date by 1 day', 'Increase Business Date by 1 day', '0 0 0 1/1 * ? *', '2026-08-21 12:32:45.30157', '99', NULL, NULL, NULL, 'Increase Business Date by 1 dayJobDetail1 _ DEFAULT', NULL, 'f', 'f', 't', '0', 'f', '1', 'f', 'BDT_INC1');
INSERT INTO public.job (id, name, display_name, cron_expression, create_time, task_priority, group_name, previous_run_start_time, next_run_time, job_key, initializing_errorlog, is_active, currently_running, updates_allowed, scheduler_group, is_misfired, node_id, is_mismatched_job, short_name) VALUES ('33', 'Increase COB Date by 1 day', 'Increase COB Date by 1 day', '0 0 0 1/1 * ? *', '2026-08-21 12:32:45.304493', '98', NULL, NULL, NULL, 'Increase COB Date by 1 dayJobDetail1 _ DEFAULT', NULL, 'f', 'f', 't', '0', 'f', '1', 'f', 'BDT_COB1');
INSERT INTO public.job (id, name, display_name, cron_expression, create_time, task_priority, group_name, previous_run_start_time, next_run_time, job_key, initializing_errorlog, is_active, currently_running, updates_allowed, scheduler_group, is_misfired, node_id, is_mismatched_job, short_name) VALUES ('36', 'Send Asynchronous Events', 'Send Asynchronous Events', '0 0/1 * * *  ?', '2026-08-21 12:32:45.860204', '5', NULL, NULL, NULL, 'Send Asynchronous Events _ DEFAULT', NULL, 't', 'f', 't', '0', 'f', '1', 't', 'ASE_SEND');
INSERT INTO public.job (id, name, display_name, cron_expression, create_time, task_priority, group_name, previous_run_start_time, next_run_time, job_key, initializing_errorlog, is_active, currently_running, updates_allowed, scheduler_group, is_misfired, node_id, is_mismatched_job, short_name) VALUES ('37', 'Purge External Events', 'Purge External Events', '0 1 0 1/1 * ? *', '2026-08-21 12:32:45.914659', '5', NULL, NULL, NULL, 'Purge External Events _ DEFAULT', NULL, 'f', 'f', 't', '0', 'f', '1', 't', 'EXE_PURG');
INSERT INTO public.job (id, name, display_name, cron_expression, create_time, task_priority, group_name, previous_run_start_time, next_run_time, job_key, initializing_errorlog, is_active, currently_running, updates_allowed, scheduler_group, is_misfired, node_id, is_mismatched_job, short_name) VALUES ('38', 'Purge Processed Commands', 'Purge Processed Commands', '0 0 1 * * ?', '2026-08-21 12:32:46.267461', '5', NULL, NULL, NULL, 'Purge Processed Commands _ DEFAULT', NULL, 'f', 'f', 't', '0', 'f', '1', 't', 'COM_PURG');
INSERT INTO public.job (id, name, display_name, cron_expression, create_time, task_priority, group_name, previous_run_start_time, next_run_time, job_key, initializing_errorlog, is_active, currently_running, updates_allowed, scheduler_group, is_misfired, node_id, is_mismatched_job, short_name) VALUES ('40', 'Journal Entry Aggregation', 'Journal Entry Aggregation', '0 0 6 * * ?', '2026-08-21 12:32:47.784412', '5', NULL, NULL, NULL, 'Journal Entry Aggregation _ DEFAULT', NULL, 'f', 'f', 't', '0', 'f', '1', 't', 'JRNL_AGG');
INSERT INTO public.job (id, name, display_name, cron_expression, create_time, task_priority, group_name, previous_run_start_time, next_run_time, job_key, initializing_errorlog, is_active, currently_running, updates_allowed, scheduler_group, is_misfired, node_id, is_mismatched_job, short_name) VALUES ('41', 'Retained Earning Job', 'Retained Earning Job', '0 0 6 * * ?', '2026-08-21 12:32:48.15403', '5', NULL, NULL, NULL, 'Retained Earning _ DEFAULT', NULL, 'f', 'f', 't', '0', 'f', '1', 'f', 'RE_ERNG');


-- Data for Name: job_parameters; Type: TABLE DATA; Schema: public; Owner: -

INSERT INTO public.job_parameters (id, job_id, parameter_name, parameter_value) VALUES ('1', '17', 'thread-pool-size', '10');
INSERT INTO public.job_parameters (id, job_id, parameter_name, parameter_value) VALUES ('2', '17', 'batch-size', '100');
INSERT INTO public.job_parameters (id, job_id, parameter_name, parameter_value) VALUES ('3', '17', 'officeId', '1');
INSERT INTO public.job_parameters (id, job_id, parameter_name, parameter_value) VALUES ('4', '6', 'thread-pool-size', '10');
INSERT INTO public.job_parameters (id, job_id, parameter_name, parameter_value) VALUES ('5', '6', 'batch-size', '100');


-- Data for Name: job_run_history; Type: TABLE DATA; Schema: public; Owner: -



-- Data for Name: m_account_transfer_details; Type: TABLE DATA; Schema: public; Owner: -



-- Data for Name: m_account_transfer_standing_instructions; Type: TABLE DATA; Schema: public; Owner: -



-- Data for Name: m_account_transfer_standing_instructions_history; Type: TABLE DATA; Schema: public; Owner: -



-- Data for Name: m_account_transfer_transaction; Type: TABLE DATA; Schema: public; Owner: -



-- Data for Name: m_address; Type: TABLE DATA; Schema: public; Owner: -



-- Data for Name: m_adhoc; Type: TABLE DATA; Schema: public; Owner: -



-- Data for Name: m_appuser_previous_password; Type: TABLE DATA; Schema: public; Owner: -



-- Data for Name: m_role; Type: TABLE DATA; Schema: public; Owner: -

INSERT INTO public.m_role (id, name, description, is_disabled) VALUES ('1', 'Super user', 'This role provides all application permissions.', 'f');
INSERT INTO public.m_role (id, name, description, is_disabled) VALUES ('2', 'Self Service User', 'self service user role', 't');


-- Data for Name: m_appuser_role; Type: TABLE DATA; Schema: public; Owner: -

INSERT INTO public.m_appuser_role (appuser_id, role_id) VALUES ('1', '1');
INSERT INTO public.m_appuser_role (appuser_id, role_id) VALUES ('3', '1');


-- Data for Name: m_batch_business_steps; Type: TABLE DATA; Schema: public; Owner: -

INSERT INTO public.m_batch_business_steps (id, job_name, step_name, step_order) VALUES ('1', 'LOAN_CLOSE_OF_BUSINESS', 'APPLY_CHARGE_TO_OVERDUE_LOANS', '1');
INSERT INTO public.m_batch_business_steps (id, job_name, step_name, step_order) VALUES ('2', 'LOAN_CLOSE_OF_BUSINESS', 'LOAN_DELINQUENCY_CLASSIFICATION', '2');
INSERT INTO public.m_batch_business_steps (id, job_name, step_name, step_order) VALUES ('3', 'LOAN_CLOSE_OF_BUSINESS', 'CHECK_LOAN_REPAYMENT_DUE', '3');
INSERT INTO public.m_batch_business_steps (id, job_name, step_name, step_order) VALUES ('4', 'LOAN_CLOSE_OF_BUSINESS', 'CHECK_LOAN_REPAYMENT_OVERDUE', '4');
INSERT INTO public.m_batch_business_steps (id, job_name, step_name, step_order) VALUES ('5', 'LOAN_CLOSE_OF_BUSINESS', 'UPDATE_LOAN_ARREARS_AGING', '5');
INSERT INTO public.m_batch_business_steps (id, job_name, step_name, step_order) VALUES ('6', 'LOAN_CLOSE_OF_BUSINESS', 'ADD_PERIODIC_ACCRUAL_ENTRIES', '6');


-- Data for Name: m_business_date; Type: TABLE DATA; Schema: public; Owner: -



-- Data for Name: m_calendar; Type: TABLE DATA; Schema: public; Owner: -



-- Data for Name: m_calendar_history; Type: TABLE DATA; Schema: public; Owner: -



-- Data for Name: m_calendar_instance; Type: TABLE DATA; Schema: public; Owner: -



-- Data for Name: m_tellers; Type: TABLE DATA; Schema: public; Owner: -



-- Data for Name: m_cashiers; Type: TABLE DATA; Schema: public; Owner: -



-- Data for Name: m_cashier_transactions; Type: TABLE DATA; Schema: public; Owner: -



-- Data for Name: m_client_address; Type: TABLE DATA; Schema: public; Owner: -



-- Data for Name: m_meeting; Type: TABLE DATA; Schema: public; Owner: -



-- Data for Name: m_client_attendance; Type: TABLE DATA; Schema: public; Owner: -



-- Data for Name: m_client_charge; Type: TABLE DATA; Schema: public; Owner: -



-- Data for Name: m_client_charge_paid_by; Type: TABLE DATA; Schema: public; Owner: -



-- Data for Name: m_currency; Type: TABLE DATA; Schema: public; Owner: -

INSERT INTO public.m_currency (id, code, decimal_places, currency_multiplesof, display_symbol, name, internationalized_name_code) VALUES ('1', 'AED', '2', NULL, NULL, 'UAE Dirham', 'currency.AED');
INSERT INTO public.m_currency (id, code, decimal_places, currency_multiplesof, display_symbol, name, internationalized_name_code) VALUES ('2', 'AFN', '2', NULL, NULL, 'Afghanistan Afghani', 'currency.AFN');
INSERT INTO public.m_currency (id, code, decimal_places, currency_multiplesof, display_symbol, name, internationalized_name_code) VALUES ('3', 'ALL', '2', NULL, NULL, 'Albanian Lek', 'currency.ALL');
INSERT INTO public.m_currency (id, code, decimal_places, currency_multiplesof, display_symbol, name, internationalized_name_code) VALUES ('4', 'AMD', '2', NULL, NULL, 'Armenian Dram', 'currency.AMD');
INSERT INTO public.m_currency (id, code, decimal_places, currency_multiplesof, display_symbol, name, internationalized_name_code) VALUES ('5', 'ANG', '2', NULL, NULL, 'Netherlands Antillian Guilder', 'currency.ANG');
INSERT INTO public.m_currency (id, code, decimal_places, currency_multiplesof, display_symbol, name, internationalized_name_code) VALUES ('6', 'AOA', '2', NULL, NULL, 'Angolan Kwanza', 'currency.AOA');
INSERT INTO public.m_currency (id, code, decimal_places, currency_multiplesof, display_symbol, name, internationalized_name_code) VALUES ('7', 'ARS', '2', NULL, '$', 'Argentine Peso', 'currency.ARS');
INSERT INTO public.m_currency (id, code, decimal_places, currency_multiplesof, display_symbol, name, internationalized_name_code) VALUES ('8', 'AUD', '2', NULL, 'A$', 'Australian Dollar', 'currency.AUD');
INSERT INTO public.m_currency (id, code, decimal_places, currency_multiplesof, display_symbol, name, internationalized_name_code) VALUES ('9', 'AWG', '2', NULL, NULL, 'Aruban Guilder', 'currency.AWG');
INSERT INTO public.m_currency (id, code, decimal_places, currency_multiplesof, display_symbol, name, internationalized_name_code) VALUES ('10', 'AZM', '2', NULL, NULL, 'Azerbaijanian Manat', 'currency.AZM');
INSERT INTO public.m_currency (id, code, decimal_places, currency_multiplesof, display_symbol, name, internationalized_name_code) VALUES ('11', 'BAM', '2', NULL, NULL, 'Bosnia and Herzegovina Convertible Marks', 'currency.BAM');
INSERT INTO public.m_currency (id, code, decimal_places, currency_multiplesof, display_symbol, name, internationalized_name_code) VALUES ('12', 'BBD', '2', NULL, NULL, 'Barbados Dollar', 'currency.BBD');
INSERT INTO public.m_currency (id, code, decimal_places, currency_multiplesof, display_symbol, name, internationalized_name_code) VALUES ('13', 'BDT', '2', NULL, NULL, 'Bangladesh Taka', 'currency.BDT');
INSERT INTO public.m_currency (id, code, decimal_places, currency_multiplesof, display_symbol, name, internationalized_name_code) VALUES ('14', 'BGN', '2', NULL, NULL, 'Bulgarian Lev', 'currency.BGN');
INSERT INTO public.m_currency (id, code, decimal_places, currency_multiplesof, display_symbol, name, internationalized_name_code) VALUES ('15', 'BHD', '3', NULL, NULL, 'Bahraini Dinar', 'currency.BHD');
INSERT INTO public.m_currency (id, code, decimal_places, currency_multiplesof, display_symbol, name, internationalized_name_code) VALUES ('16', 'BIF', '0', NULL, NULL, 'Burundi Franc', 'currency.BIF');
INSERT INTO public.m_currency (id, code, decimal_places, currency_multiplesof, display_symbol, name, internationalized_name_code) VALUES ('17', 'BMD', '2', NULL, NULL, 'Bermudian Dollar', 'currency.BMD');
INSERT INTO public.m_currency (id, code, decimal_places, currency_multiplesof, display_symbol, name, internationalized_name_code) VALUES ('18', 'BND', '2', NULL, 'B$', 'Brunei Dollar', 'currency.BND');
INSERT INTO public.m_currency (id, code, decimal_places, currency_multiplesof, display_symbol, name, internationalized_name_code) VALUES ('19', 'BOB', '2', NULL, 'Bs.', 'Bolivian Boliviano', 'currency.BOB');
INSERT INTO public.m_currency (id, code, decimal_places, currency_multiplesof, display_symbol, name, internationalized_name_code) VALUES ('20', 'BRL', '2', NULL, 'R$', 'Brazilian Real', 'currency.BRL');
INSERT INTO public.m_currency (id, code, decimal_places, currency_multiplesof, display_symbol, name, internationalized_name_code) VALUES ('21', 'BSD', '2', NULL, NULL, 'Bahamian Dollar', 'currency.BSD');
INSERT INTO public.m_currency (id, code, decimal_places, currency_multiplesof, display_symbol, name, internationalized_name_code) VALUES ('22', 'BTN', '2', NULL, NULL, 'Bhutan Ngultrum', 'currency.BTN');
INSERT INTO public.m_currency (id, code, decimal_places, currency_multiplesof, display_symbol, name, internationalized_name_code) VALUES ('23', 'BWP', '2', NULL, NULL, 'Botswana Pula', 'currency.BWP');
INSERT INTO public.m_currency (id, code, decimal_places, currency_multiplesof, display_symbol, name, internationalized_name_code) VALUES ('24', 'BYR', '0', NULL, NULL, 'Belarussian Ruble', 'currency.BYR');
INSERT INTO public.m_currency (id, code, decimal_places, currency_multiplesof, display_symbol, name, internationalized_name_code) VALUES ('25', 'BZD', '2', NULL, 'BZ$', 'Belize Dollar', 'currency.BZD');
INSERT INTO public.m_currency (id, code, decimal_places, currency_multiplesof, display_symbol, name, internationalized_name_code) VALUES ('26', 'CAD', '2', NULL, NULL, 'Canadian Dollar', 'currency.CAD');
INSERT INTO public.m_currency (id, code, decimal_places, currency_multiplesof, display_symbol, name, internationalized_name_code) VALUES ('27', 'CDF', '2', NULL, NULL, 'Franc Congolais', 'currency.CDF');
INSERT INTO public.m_currency (id, code, decimal_places, currency_multiplesof, display_symbol, name, internationalized_name_code) VALUES ('28', 'CHF', '2', NULL, NULL, 'Swiss Franc', 'currency.CHF');
INSERT INTO public.m_currency (id, code, decimal_places, currency_multiplesof, display_symbol, name, internationalized_name_code) VALUES ('29', 'CLP', '0', NULL, '$', 'Chilean Peso', 'currency.CLP');
INSERT INTO public.m_currency (id, code, decimal_places, currency_multiplesof, display_symbol, name, internationalized_name_code) VALUES ('30', 'CNY', '2', NULL, NULL, 'Chinese Yuan Renminbi', 'currency.CNY');
INSERT INTO public.m_currency (id, code, decimal_places, currency_multiplesof, display_symbol, name, internationalized_name_code) VALUES ('31', 'COP', '2', NULL, '$', 'Colombian Peso', 'currency.COP');
INSERT INTO public.m_currency (id, code, decimal_places, currency_multiplesof, display_symbol, name, internationalized_name_code) VALUES ('32', 'CRC', '2', NULL, '₡', 'Costa Rican Colon', 'currency.CRC');
INSERT INTO public.m_currency (id, code, decimal_places, currency_multiplesof, display_symbol, name, internationalized_name_code) VALUES ('33', 'CSD', '2', NULL, NULL, 'Serbian Dinar', 'currency.CSD');
INSERT INTO public.m_currency (id, code, decimal_places, currency_multiplesof, display_symbol, name, internationalized_name_code) VALUES ('34', 'CUP', '2', NULL, '$MN', 'Cuban Peso', 'currency.CUP');
INSERT INTO public.m_currency (id, code, decimal_places, currency_multiplesof, display_symbol, name, internationalized_name_code) VALUES ('35', 'CVE', '2', NULL, NULL, 'Cape Verde Escudo', 'currency.CVE');
INSERT INTO public.m_currency (id, code, decimal_places, currency_multiplesof, display_symbol, name, internationalized_name_code) VALUES ('36', 'CYP', '2', NULL, NULL, 'Cyprus Pound', 'currency.CYP');
INSERT INTO public.m_currency (id, code, decimal_places, currency_multiplesof, display_symbol, name, internationalized_name_code) VALUES ('37', 'CZK', '2', NULL, NULL, 'Czech Koruna', 'currency.CZK');
INSERT INTO public.m_currency (id, code, decimal_places, currency_multiplesof, display_symbol, name, internationalized_name_code) VALUES ('38', 'DJF', '0', NULL, NULL, 'Djibouti Franc', 'currency.DJF');
INSERT INTO public.m_currency (id, code, decimal_places, currency_multiplesof, display_symbol, name, internationalized_name_code) VALUES ('39', 'DKK', '2', NULL, NULL, 'Danish Krone', 'currency.DKK');
INSERT INTO public.m_currency (id, code, decimal_places, currency_multiplesof, display_symbol, name, internationalized_name_code) VALUES ('40', 'DOP', '2', NULL, 'RD$', 'Dominican Peso', 'currency.DOP');
INSERT INTO public.m_currency (id, code, decimal_places, currency_multiplesof, display_symbol, name, internationalized_name_code) VALUES ('41', 'DZD', '2', NULL, NULL, 'Algerian Dinar', 'currency.DZD');
INSERT INTO public.m_currency (id, code, decimal_places, currency_multiplesof, display_symbol, name, internationalized_name_code) VALUES ('42', 'EEK', '2', NULL, NULL, 'Estonian Kroon', 'currency.EEK');
INSERT INTO public.m_currency (id, code, decimal_places, currency_multiplesof, display_symbol, name, internationalized_name_code) VALUES ('43', 'EGP', '2', NULL, NULL, 'Egyptian Pound', 'currency.EGP');
INSERT INTO public.m_currency (id, code, decimal_places, currency_multiplesof, display_symbol, name, internationalized_name_code) VALUES ('44', 'ERN', '2', NULL, NULL, 'Eritrea Nafka', 'currency.ERN');
INSERT INTO public.m_currency (id, code, decimal_places, currency_multiplesof, display_symbol, name, internationalized_name_code) VALUES ('45', 'ETB', '2', NULL, NULL, 'Ethiopian Birr', 'currency.ETB');
INSERT INTO public.m_currency (id, code, decimal_places, currency_multiplesof, display_symbol, name, internationalized_name_code) VALUES ('46', 'EUR', '2', NULL, '€', 'Euro', 'currency.EUR');
INSERT INTO public.m_currency (id, code, decimal_places, currency_multiplesof, display_symbol, name, internationalized_name_code) VALUES ('47', 'FJD', '2', NULL, NULL, 'Fiji Dollar', 'currency.FJD');
INSERT INTO public.m_currency (id, code, decimal_places, currency_multiplesof, display_symbol, name, internationalized_name_code) VALUES ('48', 'FKP', '2', NULL, NULL, 'Falkland Islands Pound', 'currency.FKP');
INSERT INTO public.m_currency (id, code, decimal_places, currency_multiplesof, display_symbol, name, internationalized_name_code) VALUES ('49', 'GBP', '2', NULL, NULL, 'Pound Sterling', 'currency.GBP');
INSERT INTO public.m_currency (id, code, decimal_places, currency_multiplesof, display_symbol, name, internationalized_name_code) VALUES ('50', 'GEL', '2', NULL, NULL, 'Georgian Lari', 'currency.GEL');
INSERT INTO public.m_currency (id, code, decimal_places, currency_multiplesof, display_symbol, name, internationalized_name_code) VALUES ('51', 'GHC', '2', NULL, 'GHc', 'Ghana Cedi', 'currency.GHC');
INSERT INTO public.m_currency (id, code, decimal_places, currency_multiplesof, display_symbol, name, internationalized_name_code) VALUES ('52', 'GIP', '2', NULL, NULL, 'Gibraltar Pound', 'currency.GIP');
INSERT INTO public.m_currency (id, code, decimal_places, currency_multiplesof, display_symbol, name, internationalized_name_code) VALUES ('53', 'GMD', '2', NULL, NULL, 'Gambian Dalasi', 'currency.GMD');
INSERT INTO public.m_currency (id, code, decimal_places, currency_multiplesof, display_symbol, name, internationalized_name_code) VALUES ('54', 'GNF', '0', NULL, NULL, 'Guinea Franc', 'currency.GNF');
INSERT INTO public.m_currency (id, code, decimal_places, currency_multiplesof, display_symbol, name, internationalized_name_code) VALUES ('55', 'GTQ', '2', NULL, 'Q', 'Guatemala Quetzal', 'currency.GTQ');
INSERT INTO public.m_currency (id, code, decimal_places, currency_multiplesof, display_symbol, name, internationalized_name_code) VALUES ('56', 'GYD', '2', NULL, NULL, 'Guyana Dollar', 'currency.GYD');
INSERT INTO public.m_currency (id, code, decimal_places, currency_multiplesof, display_symbol, name, internationalized_name_code) VALUES ('57', 'HKD', '2', NULL, NULL, 'Hong Kong Dollar', 'currency.HKD');
INSERT INTO public.m_currency (id, code, decimal_places, currency_multiplesof, display_symbol, name, internationalized_name_code) VALUES ('58', 'HNL', '2', NULL, 'L', 'Honduras Lempira', 'currency.HNL');
INSERT INTO public.m_currency (id, code, decimal_places, currency_multiplesof, display_symbol, name, internationalized_name_code) VALUES ('59', 'HRK', '2', NULL, NULL, 'Croatian Kuna', 'currency.HRK');
INSERT INTO public.m_currency (id, code, decimal_places, currency_multiplesof, display_symbol, name, internationalized_name_code) VALUES ('60', 'HTG', '2', NULL, 'G', 'Haiti Gourde', 'currency.HTG');
INSERT INTO public.m_currency (id, code, decimal_places, currency_multiplesof, display_symbol, name, internationalized_name_code) VALUES ('61', 'HUF', '2', NULL, NULL, 'Hungarian Forint', 'currency.HUF');
INSERT INTO public.m_currency (id, code, decimal_places, currency_multiplesof, display_symbol, name, internationalized_name_code) VALUES ('62', 'IDR', '2', NULL, NULL, 'Indonesian Rupiah', 'currency.IDR');
INSERT INTO public.m_currency (id, code, decimal_places, currency_multiplesof, display_symbol, name, internationalized_name_code) VALUES ('63', 'ILS', '2', NULL, NULL, 'New Israeli Shekel', 'currency.ILS');
INSERT INTO public.m_currency (id, code, decimal_places, currency_multiplesof, display_symbol, name, internationalized_name_code) VALUES ('64', 'INR', '2', NULL, '₹', 'Indian Rupee', 'currency.INR');
INSERT INTO public.m_currency (id, code, decimal_places, currency_multiplesof, display_symbol, name, internationalized_name_code) VALUES ('65', 'IQD', '3', NULL, NULL, 'Iraqi Dinar', 'currency.IQD');
INSERT INTO public.m_currency (id, code, decimal_places, currency_multiplesof, display_symbol, name, internationalized_name_code) VALUES ('66', 'IRR', '2', NULL, NULL, 'Iranian Rial', 'currency.IRR');
INSERT INTO public.m_currency (id, code, decimal_places, currency_multiplesof, display_symbol, name, internationalized_name_code) VALUES ('67', 'ISK', '0', NULL, NULL, 'Iceland Krona', 'currency.ISK');
INSERT INTO public.m_currency (id, code, decimal_places, currency_multiplesof, display_symbol, name, internationalized_name_code) VALUES ('68', 'JMD', '2', NULL, NULL, 'Jamaican Dollar', 'currency.JMD');
INSERT INTO public.m_currency (id, code, decimal_places, currency_multiplesof, display_symbol, name, internationalized_name_code) VALUES ('69', 'JOD', '3', NULL, NULL, 'Jordanian Dinar', 'currency.JOD');
INSERT INTO public.m_currency (id, code, decimal_places, currency_multiplesof, display_symbol, name, internationalized_name_code) VALUES ('70', 'JPY', '0', NULL, NULL, 'Japanese Yen', 'currency.JPY');
INSERT INTO public.m_currency (id, code, decimal_places, currency_multiplesof, display_symbol, name, internationalized_name_code) VALUES ('71', 'KES', '2', NULL, 'KSh', 'Kenyan Shilling', 'currency.KES');
INSERT INTO public.m_currency (id, code, decimal_places, currency_multiplesof, display_symbol, name, internationalized_name_code) VALUES ('72', 'KGS', '2', NULL, NULL, 'Kyrgyzstan Som', 'currency.KGS');
INSERT INTO public.m_currency (id, code, decimal_places, currency_multiplesof, display_symbol, name, internationalized_name_code) VALUES ('73', 'KHR', '2', NULL, NULL, 'Cambodia Riel', 'currency.KHR');
INSERT INTO public.m_currency (id, code, decimal_places, currency_multiplesof, display_symbol, name, internationalized_name_code) VALUES ('74', 'KMF', '0', NULL, NULL, 'Comoro Franc', 'currency.KMF');
INSERT INTO public.m_currency (id, code, decimal_places, currency_multiplesof, display_symbol, name, internationalized_name_code) VALUES ('75', 'KPW', '2', NULL, NULL, 'North Korean Won', 'currency.KPW');
INSERT INTO public.m_currency (id, code, decimal_places, currency_multiplesof, display_symbol, name, internationalized_name_code) VALUES ('76', 'KRW', '0', NULL, NULL, 'Korean Won', 'currency.KRW');
INSERT INTO public.m_currency (id, code, decimal_places, currency_multiplesof, display_symbol, name, internationalized_name_code) VALUES ('77', 'KWD', '3', NULL, NULL, 'Kuwaiti Dinar', 'currency.KWD');
INSERT INTO public.m_currency (id, code, decimal_places, currency_multiplesof, display_symbol, name, internationalized_name_code) VALUES ('78', 'KYD', '2', NULL, NULL, 'Cayman Islands Dollar', 'currency.KYD');
INSERT INTO public.m_currency (id, code, decimal_places, currency_multiplesof, display_symbol, name, internationalized_name_code) VALUES ('79', 'KZT', '2', NULL, NULL, 'Kazakhstan Tenge', 'currency.KZT');
INSERT INTO public.m_currency (id, code, decimal_places, currency_multiplesof, display_symbol, name, internationalized_name_code) VALUES ('80', 'LAK', '2', NULL, NULL, 'Lao Kip', 'currency.LAK');
INSERT INTO public.m_currency (id, code, decimal_places, currency_multiplesof, display_symbol, name, internationalized_name_code) VALUES ('81', 'LBP', '2', NULL, 'L£', 'Lebanese Pound', 'currency.LBP');
INSERT INTO public.m_currency (id, code, decimal_places, currency_multiplesof, display_symbol, name, internationalized_name_code) VALUES ('82', 'LKR', '2', NULL, NULL, 'Sri Lanka Rupee', 'currency.LKR');
INSERT INTO public.m_currency (id, code, decimal_places, currency_multiplesof, display_symbol, name, internationalized_name_code) VALUES ('83', 'LRD', '2', NULL, NULL, 'Liberian Dollar', 'currency.LRD');
INSERT INTO public.m_currency (id, code, decimal_places, currency_multiplesof, display_symbol, name, internationalized_name_code) VALUES ('84', 'LSL', '2', NULL, NULL, 'Lesotho Loti', 'currency.LSL');
INSERT INTO public.m_currency (id, code, decimal_places, currency_multiplesof, display_symbol, name, internationalized_name_code) VALUES ('85', 'LTL', '2', NULL, NULL, 'Lithuanian Litas', 'currency.LTL');
INSERT INTO public.m_currency (id, code, decimal_places, currency_multiplesof, display_symbol, name, internationalized_name_code) VALUES ('86', 'LVL', '2', NULL, NULL, 'Latvian Lats', 'currency.LVL');
INSERT INTO public.m_currency (id, code, decimal_places, currency_multiplesof, display_symbol, name, internationalized_name_code) VALUES ('87', 'LYD', '3', NULL, NULL, 'Libyan Dinar', 'currency.LYD');
INSERT INTO public.m_currency (id, code, decimal_places, currency_multiplesof, display_symbol, name, internationalized_name_code) VALUES ('88', 'MAD', '2', NULL, NULL, 'Moroccan Dirham', 'currency.MAD');
INSERT INTO public.m_currency (id, code, decimal_places, currency_multiplesof, display_symbol, name, internationalized_name_code) VALUES ('89', 'MDL', '2', NULL, NULL, 'Moldovan Leu', 'currency.MDL');
INSERT INTO public.m_currency (id, code, decimal_places, currency_multiplesof, display_symbol, name, internationalized_name_code) VALUES ('90', 'MGA', '2', NULL, NULL, 'Malagasy Ariary', 'currency.MGA');
INSERT INTO public.m_currency (id, code, decimal_places, currency_multiplesof, display_symbol, name, internationalized_name_code) VALUES ('91', 'MKD', '2', NULL, NULL, 'Macedonian Denar', 'currency.MKD');
INSERT INTO public.m_currency (id, code, decimal_places, currency_multiplesof, display_symbol, name, internationalized_name_code) VALUES ('92', 'MMK', '2', NULL, 'K', 'Myanmar Kyat', 'currency.MMK');
INSERT INTO public.m_currency (id, code, decimal_places, currency_multiplesof, display_symbol, name, internationalized_name_code) VALUES ('93', 'MNT', '2', NULL, NULL, 'Mongolian Tugrik', 'currency.MNT');
INSERT INTO public.m_currency (id, code, decimal_places, currency_multiplesof, display_symbol, name, internationalized_name_code) VALUES ('94', 'MOP', '2', NULL, NULL, 'Macau Pataca', 'currency.MOP');
INSERT INTO public.m_currency (id, code, decimal_places, currency_multiplesof, display_symbol, name, internationalized_name_code) VALUES ('95', 'MRO', '2', NULL, NULL, 'Mauritania Ouguiya', 'currency.MRO');
INSERT INTO public.m_currency (id, code, decimal_places, currency_multiplesof, display_symbol, name, internationalized_name_code) VALUES ('96', 'MTL', '2', NULL, NULL, 'Maltese Lira', 'currency.MTL');
INSERT INTO public.m_currency (id, code, decimal_places, currency_multiplesof, display_symbol, name, internationalized_name_code) VALUES ('97', 'MUR', '2', NULL, NULL, 'Mauritius Rupee', 'currency.MUR');
INSERT INTO public.m_currency (id, code, decimal_places, currency_multiplesof, display_symbol, name, internationalized_name_code) VALUES ('98', 'MVR', '2', NULL, NULL, 'Maldives Rufiyaa', 'currency.MVR');
INSERT INTO public.m_currency (id, code, decimal_places, currency_multiplesof, display_symbol, name, internationalized_name_code) VALUES ('99', 'MWK', '2', NULL, NULL, 'Malawi Kwacha', 'currency.MWK');
INSERT INTO public.m_currency (id, code, decimal_places, currency_multiplesof, display_symbol, name, internationalized_name_code) VALUES ('100', 'MXN', '2', NULL, '$', 'Mexican Peso', 'currency.MXN');
INSERT INTO public.m_currency (id, code, decimal_places, currency_multiplesof, display_symbol, name, internationalized_name_code) VALUES ('101', 'MYR', '2', NULL, NULL, 'Malaysian Ringgit', 'currency.MYR');
INSERT INTO public.m_currency (id, code, decimal_places, currency_multiplesof, display_symbol, name, internationalized_name_code) VALUES ('102', 'MZM', '2', NULL, NULL, 'Mozambique Metical', 'currency.MZM');
INSERT INTO public.m_currency (id, code, decimal_places, currency_multiplesof, display_symbol, name, internationalized_name_code) VALUES ('103', 'NAD', '2', NULL, NULL, 'Namibia Dollar', 'currency.NAD');
INSERT INTO public.m_currency (id, code, decimal_places, currency_multiplesof, display_symbol, name, internationalized_name_code) VALUES ('104', 'NGN', '2', NULL, NULL, 'Nigerian Naira', 'currency.NGN');
INSERT INTO public.m_currency (id, code, decimal_places, currency_multiplesof, display_symbol, name, internationalized_name_code) VALUES ('105', 'NIO', '2', NULL, 'C$', 'Nicaragua Cordoba Oro', 'currency.NIO');
INSERT INTO public.m_currency (id, code, decimal_places, currency_multiplesof, display_symbol, name, internationalized_name_code) VALUES ('106', 'NOK', '2', NULL, NULL, 'Norwegian Krone', 'currency.NOK');
INSERT INTO public.m_currency (id, code, decimal_places, currency_multiplesof, display_symbol, name, internationalized_name_code) VALUES ('107', 'NPR', '2', NULL, NULL, 'Nepalese Rupee', 'currency.NPR');
INSERT INTO public.m_currency (id, code, decimal_places, currency_multiplesof, display_symbol, name, internationalized_name_code) VALUES ('108', 'NZD', '2', NULL, NULL, 'New Zealand Dollar', 'currency.NZD');
INSERT INTO public.m_currency (id, code, decimal_places, currency_multiplesof, display_symbol, name, internationalized_name_code) VALUES ('109', 'OMR', '3', NULL, NULL, 'Rial Omani', 'currency.OMR');
INSERT INTO public.m_currency (id, code, decimal_places, currency_multiplesof, display_symbol, name, internationalized_name_code) VALUES ('110', 'PAB', '2', NULL, 'B/.', 'Panama Balboa', 'currency.PAB');
INSERT INTO public.m_currency (id, code, decimal_places, currency_multiplesof, display_symbol, name, internationalized_name_code) VALUES ('111', 'PEN', '2', NULL, 'S/.', 'Peruvian Nuevo Sol', 'currency.PEN');
INSERT INTO public.m_currency (id, code, decimal_places, currency_multiplesof, display_symbol, name, internationalized_name_code) VALUES ('112', 'PGK', '2', NULL, NULL, 'Papua New Guinea Kina', 'currency.PGK');
INSERT INTO public.m_currency (id, code, decimal_places, currency_multiplesof, display_symbol, name, internationalized_name_code) VALUES ('113', 'PHP', '2', NULL, NULL, 'Philippine Peso', 'currency.PHP');
INSERT INTO public.m_currency (id, code, decimal_places, currency_multiplesof, display_symbol, name, internationalized_name_code) VALUES ('114', 'PKR', '2', NULL, NULL, 'Pakistan Rupee', 'currency.PKR');
INSERT INTO public.m_currency (id, code, decimal_places, currency_multiplesof, display_symbol, name, internationalized_name_code) VALUES ('115', 'PLN', '2', NULL, NULL, 'Polish Zloty', 'currency.PLN');
INSERT INTO public.m_currency (id, code, decimal_places, currency_multiplesof, display_symbol, name, internationalized_name_code) VALUES ('116', 'PYG', '0', NULL, '₲', 'Paraguayan Guarani', 'currency.PYG');
INSERT INTO public.m_currency (id, code, decimal_places, currency_multiplesof, display_symbol, name, internationalized_name_code) VALUES ('117', 'QAR', '2', NULL, NULL, 'Qatari Rial', 'currency.QAR');
INSERT INTO public.m_currency (id, code, decimal_places, currency_multiplesof, display_symbol, name, internationalized_name_code) VALUES ('118', 'RON', '2', NULL, NULL, 'Romanian Leu', 'currency.RON');
INSERT INTO public.m_currency (id, code, decimal_places, currency_multiplesof, display_symbol, name, internationalized_name_code) VALUES ('119', 'RUB', '2', NULL, NULL, 'Russian Ruble', 'currency.RUB');
INSERT INTO public.m_currency (id, code, decimal_places, currency_multiplesof, display_symbol, name, internationalized_name_code) VALUES ('120', 'RWF', '0', NULL, NULL, 'Rwanda Franc', 'currency.RWF');
INSERT INTO public.m_currency (id, code, decimal_places, currency_multiplesof, display_symbol, name, internationalized_name_code) VALUES ('121', 'SAR', '2', NULL, NULL, 'Saudi Riyal', 'currency.SAR');
INSERT INTO public.m_currency (id, code, decimal_places, currency_multiplesof, display_symbol, name, internationalized_name_code) VALUES ('122', 'SBD', '2', NULL, NULL, 'Solomon Islands Dollar', 'currency.SBD');
INSERT INTO public.m_currency (id, code, decimal_places, currency_multiplesof, display_symbol, name, internationalized_name_code) VALUES ('123', 'SCR', '2', NULL, NULL, 'Seychelles Rupee', 'currency.SCR');
INSERT INTO public.m_currency (id, code, decimal_places, currency_multiplesof, display_symbol, name, internationalized_name_code) VALUES ('124', 'SDD', '2', NULL, NULL, 'Sudanese Dinar', 'currency.SDD');
INSERT INTO public.m_currency (id, code, decimal_places, currency_multiplesof, display_symbol, name, internationalized_name_code) VALUES ('125', 'SEK', '2', NULL, NULL, 'Swedish Krona', 'currency.SEK');
INSERT INTO public.m_currency (id, code, decimal_places, currency_multiplesof, display_symbol, name, internationalized_name_code) VALUES ('126', 'SGD', '2', NULL, NULL, 'Singapore Dollar', 'currency.SGD');
INSERT INTO public.m_currency (id, code, decimal_places, currency_multiplesof, display_symbol, name, internationalized_name_code) VALUES ('127', 'SHP', '2', NULL, NULL, 'St Helena Pound', 'currency.SHP');
INSERT INTO public.m_currency (id, code, decimal_places, currency_multiplesof, display_symbol, name, internationalized_name_code) VALUES ('128', 'SIT', '2', NULL, NULL, 'Slovenian Tolar', 'currency.SIT');
INSERT INTO public.m_currency (id, code, decimal_places, currency_multiplesof, display_symbol, name, internationalized_name_code) VALUES ('129', 'SKK', '2', NULL, NULL, 'Slovak Koruna', 'currency.SKK');
INSERT INTO public.m_currency (id, code, decimal_places, currency_multiplesof, display_symbol, name, internationalized_name_code) VALUES ('130', 'SLL', '2', NULL, NULL, 'Sierra Leone Leone', 'currency.SLL');
INSERT INTO public.m_currency (id, code, decimal_places, currency_multiplesof, display_symbol, name, internationalized_name_code) VALUES ('131', 'SOS', '2', NULL, NULL, 'Somali Shilling', 'currency.SOS');
INSERT INTO public.m_currency (id, code, decimal_places, currency_multiplesof, display_symbol, name, internationalized_name_code) VALUES ('132', 'SRD', '2', NULL, NULL, 'Surinam Dollar', 'currency.SRD');
INSERT INTO public.m_currency (id, code, decimal_places, currency_multiplesof, display_symbol, name, internationalized_name_code) VALUES ('133', 'STD', '2', NULL, NULL, 'Sao Tome and Principe Dobra', 'currency.STD');
INSERT INTO public.m_currency (id, code, decimal_places, currency_multiplesof, display_symbol, name, internationalized_name_code) VALUES ('134', 'SVC', '2', NULL, NULL, 'El Salvador Colon', 'currency.SVC');
INSERT INTO public.m_currency (id, code, decimal_places, currency_multiplesof, display_symbol, name, internationalized_name_code) VALUES ('135', 'SYP', '2', NULL, NULL, 'Syrian Pound', 'currency.SYP');
INSERT INTO public.m_currency (id, code, decimal_places, currency_multiplesof, display_symbol, name, internationalized_name_code) VALUES ('136', 'SZL', '2', NULL, NULL, 'Eswatini Lilangeni', 'currency.SZL');
INSERT INTO public.m_currency (id, code, decimal_places, currency_multiplesof, display_symbol, name, internationalized_name_code) VALUES ('137', 'THB', '2', NULL, NULL, 'Thai Baht', 'currency.THB');
INSERT INTO public.m_currency (id, code, decimal_places, currency_multiplesof, display_symbol, name, internationalized_name_code) VALUES ('138', 'TJS', '2', NULL, NULL, 'Tajik Somoni', 'currency.TJS');
INSERT INTO public.m_currency (id, code, decimal_places, currency_multiplesof, display_symbol, name, internationalized_name_code) VALUES ('139', 'TMM', '2', NULL, NULL, 'Turkmenistan Manat', 'currency.TMM');
INSERT INTO public.m_currency (id, code, decimal_places, currency_multiplesof, display_symbol, name, internationalized_name_code) VALUES ('140', 'TND', '3', NULL, 'DT', 'Tunisian Dinar', 'currency.TND');
INSERT INTO public.m_currency (id, code, decimal_places, currency_multiplesof, display_symbol, name, internationalized_name_code) VALUES ('141', 'TOP', '2', NULL, NULL, 'Tonga Pa''anga', 'currency.TOP');
INSERT INTO public.m_currency (id, code, decimal_places, currency_multiplesof, display_symbol, name, internationalized_name_code) VALUES ('142', 'TRY', '2', NULL, NULL, 'Turkish Lira', 'currency.TRY');
INSERT INTO public.m_currency (id, code, decimal_places, currency_multiplesof, display_symbol, name, internationalized_name_code) VALUES ('143', 'TTD', '2', NULL, NULL, 'Trinidad and Tobago Dollar', 'currency.TTD');
INSERT INTO public.m_currency (id, code, decimal_places, currency_multiplesof, display_symbol, name, internationalized_name_code) VALUES ('144', 'TWD', '2', NULL, NULL, 'New Taiwan Dollar', 'currency.TWD');
INSERT INTO public.m_currency (id, code, decimal_places, currency_multiplesof, display_symbol, name, internationalized_name_code) VALUES ('145', 'TZS', '2', NULL, NULL, 'Tanzanian Shilling', 'currency.TZS');
INSERT INTO public.m_currency (id, code, decimal_places, currency_multiplesof, display_symbol, name, internationalized_name_code) VALUES ('146', 'UAH', '2', NULL, NULL, 'Ukraine Hryvnia', 'currency.UAH');
INSERT INTO public.m_currency (id, code, decimal_places, currency_multiplesof, display_symbol, name, internationalized_name_code) VALUES ('147', 'UGX', '2', NULL, 'USh', 'Uganda Shilling', 'currency.UGX');
INSERT INTO public.m_currency (id, code, decimal_places, currency_multiplesof, display_symbol, name, internationalized_name_code) VALUES ('148', 'USD', '2', NULL, '$', 'US Dollar', 'currency.USD');
INSERT INTO public.m_currency (id, code, decimal_places, currency_multiplesof, display_symbol, name, internationalized_name_code) VALUES ('149', 'UYU', '2', NULL, '$U', 'Peso Uruguayo', 'currency.UYU');
INSERT INTO public.m_currency (id, code, decimal_places, currency_multiplesof, display_symbol, name, internationalized_name_code) VALUES ('150', 'UZS', '2', NULL, NULL, 'Uzbekistan Sum', 'currency.UZS');
INSERT INTO public.m_currency (id, code, decimal_places, currency_multiplesof, display_symbol, name, internationalized_name_code) VALUES ('151', 'VEB', '2', NULL, 'Bs.F.', 'Venezuelan Bolivar', 'currency.VEB');
INSERT INTO public.m_currency (id, code, decimal_places, currency_multiplesof, display_symbol, name, internationalized_name_code) VALUES ('152', 'VND', '2', NULL, NULL, 'Vietnamese Dong', 'currency.VND');
INSERT INTO public.m_currency (id, code, decimal_places, currency_multiplesof, display_symbol, name, internationalized_name_code) VALUES ('153', 'VUV', '0', NULL, NULL, 'Vanuatu Vatu', 'currency.VUV');
INSERT INTO public.m_currency (id, code, decimal_places, currency_multiplesof, display_symbol, name, internationalized_name_code) VALUES ('154', 'WST', '2', NULL, NULL, 'Samoa Tala', 'currency.WST');
INSERT INTO public.m_currency (id, code, decimal_places, currency_multiplesof, display_symbol, name, internationalized_name_code) VALUES ('155', 'XAF', '0', NULL, NULL, 'CFA Franc BEAC', 'currency.XAF');
INSERT INTO public.m_currency (id, code, decimal_places, currency_multiplesof, display_symbol, name, internationalized_name_code) VALUES ('156', 'XCD', '2', NULL, NULL, 'East Caribbean Dollar', 'currency.XCD');
INSERT INTO public.m_currency (id, code, decimal_places, currency_multiplesof, display_symbol, name, internationalized_name_code) VALUES ('157', 'XDR', '5', NULL, NULL, 'SDR (Special Drawing Rights)', 'currency.XDR');
INSERT INTO public.m_currency (id, code, decimal_places, currency_multiplesof, display_symbol, name, internationalized_name_code) VALUES ('158', 'XOF', '0', NULL, 'CFA', 'CFA Franc BCEAO', 'currency.XOF');
INSERT INTO public.m_currency (id, code, decimal_places, currency_multiplesof, display_symbol, name, internationalized_name_code) VALUES ('159', 'XPF', '0', NULL, NULL, 'CFP Franc', 'currency.XPF');
INSERT INTO public.m_currency (id, code, decimal_places, currency_multiplesof, display_symbol, name, internationalized_name_code) VALUES ('160', 'YER', '2', NULL, NULL, 'Yemeni Rial', 'currency.YER');
INSERT INTO public.m_currency (id, code, decimal_places, currency_multiplesof, display_symbol, name, internationalized_name_code) VALUES ('161', 'ZAR', '2', NULL, 'R', 'South African Rand', 'currency.ZAR');
INSERT INTO public.m_currency (id, code, decimal_places, currency_multiplesof, display_symbol, name, internationalized_name_code) VALUES ('162', 'ZMK', '2', NULL, NULL, 'Zambian Kwacha', 'currency.ZMK');
INSERT INTO public.m_currency (id, code, decimal_places, currency_multiplesof, display_symbol, name, internationalized_name_code) VALUES ('163', 'ZWD', '2', NULL, NULL, 'Zimbabwe Dollar', 'currency.ZWD');
INSERT INTO public.m_currency (id, code, decimal_places, currency_multiplesof, display_symbol, name, internationalized_name_code) VALUES ('164', 'SSP', '2', NULL, 'SS£', 'South Sudanese Pound', 'currency.SSP');


-- Data for Name: m_collateral_management; Type: TABLE DATA; Schema: public; Owner: -



-- Data for Name: m_client_collateral_management; Type: TABLE DATA; Schema: public; Owner: -



-- Data for Name: m_client_identifier; Type: TABLE DATA; Schema: public; Owner: -



-- Data for Name: m_client_non_person; Type: TABLE DATA; Schema: public; Owner: -



-- Data for Name: m_client_transfer_details; Type: TABLE DATA; Schema: public; Owner: -



-- Data for Name: m_creditbureau; Type: TABLE DATA; Schema: public; Owner: -

INSERT INTO public.m_creditbureau (id, name, product, country, implementation_key) VALUES ('1', 'THITSAWORKS', '1', 'Myanmar', '1');


-- Data for Name: m_creditbureau_configuration; Type: TABLE DATA; Schema: public; Owner: -

INSERT INTO public.m_creditbureau_configuration (id, configkey, value, organisation_creditbureau_id, description) VALUES ('1', 'Password', '', '1', '');
INSERT INTO public.m_creditbureau_configuration (id, configkey, value, organisation_creditbureau_id, description) VALUES ('2', 'SubscriptionId', '', '1', '');
INSERT INTO public.m_creditbureau_configuration (id, configkey, value, organisation_creditbureau_id, description) VALUES ('3', 'SubscriptionKey', '', '1', '');
INSERT INTO public.m_creditbureau_configuration (id, configkey, value, organisation_creditbureau_id, description) VALUES ('4', 'Username', '', '1', '');
INSERT INTO public.m_creditbureau_configuration (id, configkey, value, organisation_creditbureau_id, description) VALUES ('5', 'tokenurl', '', '1', '');
INSERT INTO public.m_creditbureau_configuration (id, configkey, value, organisation_creditbureau_id, description) VALUES ('6', 'searchurl', '', '1', '');
INSERT INTO public.m_creditbureau_configuration (id, configkey, value, organisation_creditbureau_id, description) VALUES ('7', 'creditReporturl', '', '1', '');
INSERT INTO public.m_creditbureau_configuration (id, configkey, value, organisation_creditbureau_id, description) VALUES ('8', 'addCreditReporturl', '', '1', '');


-- Data for Name: m_organisation_creditbureau; Type: TABLE DATA; Schema: public; Owner: -



-- Data for Name: m_creditbureau_loanproduct_mapping; Type: TABLE DATA; Schema: public; Owner: -



-- Data for Name: m_creditbureau_token; Type: TABLE DATA; Schema: public; Owner: -



-- Data for Name: m_creditreport; Type: TABLE DATA; Schema: public; Owner: -



-- Data for Name: m_delinquency_range; Type: TABLE DATA; Schema: public; Owner: -



-- Data for Name: m_delinquency_bucket_mappings; Type: TABLE DATA; Schema: public; Owner: -



-- Data for Name: m_deposit_account_on_hold_transaction; Type: TABLE DATA; Schema: public; Owner: -



-- Data for Name: m_deposit_account_recurring_detail; Type: TABLE DATA; Schema: public; Owner: -



-- Data for Name: m_deposit_account_term_and_preclosure; Type: TABLE DATA; Schema: public; Owner: -



-- Data for Name: m_interest_rate_chart; Type: TABLE DATA; Schema: public; Owner: -



-- Data for Name: m_deposit_product_interest_rate_chart; Type: TABLE DATA; Schema: public; Owner: -



-- Data for Name: m_deposit_product_recurring_detail; Type: TABLE DATA; Schema: public; Owner: -



-- Data for Name: m_deposit_product_term_and_preclosure; Type: TABLE DATA; Schema: public; Owner: -



-- Data for Name: m_document; Type: TABLE DATA; Schema: public; Owner: -



-- Data for Name: x_registered_table; Type: TABLE DATA; Schema: public; Owner: -



-- Data for Name: m_entity_datatable_check; Type: TABLE DATA; Schema: public; Owner: -



-- Data for Name: m_entity_relation; Type: TABLE DATA; Schema: public; Owner: -

INSERT INTO public.m_entity_relation (id, from_entity_type, to_entity_type, code_name) VALUES ('1', '1', '2', 'office_access_to_loan_products');
INSERT INTO public.m_entity_relation (id, from_entity_type, to_entity_type, code_name) VALUES ('2', '1', '3', 'office_access_to_savings_products');
INSERT INTO public.m_entity_relation (id, from_entity_type, to_entity_type, code_name) VALUES ('3', '1', '4', 'office_access_to_fees/charges');
INSERT INTO public.m_entity_relation (id, from_entity_type, to_entity_type, code_name) VALUES ('4', '5', '2', 'role_access_to_loan_products');
INSERT INTO public.m_entity_relation (id, from_entity_type, to_entity_type, code_name) VALUES ('5', '5', '3', 'role_access_to_savings_products');


-- Data for Name: m_entity_to_entity_access; Type: TABLE DATA; Schema: public; Owner: -



-- Data for Name: m_entity_to_entity_mapping; Type: TABLE DATA; Schema: public; Owner: -



-- Data for Name: m_external_event; Type: TABLE DATA; Schema: public; Owner: -



-- Data for Name: m_external_event_configuration; Type: TABLE DATA; Schema: public; Owner: -

INSERT INTO public.m_external_event_configuration (type, enabled) VALUES ('ClientActivateBusinessEvent', 'f');
INSERT INTO public.m_external_event_configuration (type, enabled) VALUES ('ClientCreateBusinessEvent', 'f');
INSERT INTO public.m_external_event_configuration (type, enabled) VALUES ('ClientRejectBusinessEvent', 'f');
INSERT INTO public.m_external_event_configuration (type, enabled) VALUES ('FixedDepositAccountCreateBusinessEvent', 'f');
INSERT INTO public.m_external_event_configuration (type, enabled) VALUES ('RecurringDepositAccountCreateBusinessEvent', 'f');
INSERT INTO public.m_external_event_configuration (type, enabled) VALUES ('CentersCreateBusinessEvent', 'f');
INSERT INTO public.m_external_event_configuration (type, enabled) VALUES ('GroupsCreateBusinessEvent', 'f');
INSERT INTO public.m_external_event_configuration (type, enabled) VALUES ('LoanAddChargeBusinessEvent', 'f');
INSERT INTO public.m_external_event_configuration (type, enabled) VALUES ('LoanDeleteChargeBusinessEvent', 'f');
INSERT INTO public.m_external_event_configuration (type, enabled) VALUES ('LoanUpdateChargeBusinessEvent', 'f');
INSERT INTO public.m_external_event_configuration (type, enabled) VALUES ('LoanWaiveChargeBusinessEvent', 'f');
INSERT INTO public.m_external_event_configuration (type, enabled) VALUES ('LoanWaiveChargeUndoBusinessEvent', 'f');
INSERT INTO public.m_external_event_configuration (type, enabled) VALUES ('LoanProductCreateBusinessEvent', 'f');
INSERT INTO public.m_external_event_configuration (type, enabled) VALUES ('LoanChargePaymentPostBusinessEvent', 'f');
INSERT INTO public.m_external_event_configuration (type, enabled) VALUES ('LoanChargePaymentPreBusinessEvent', 'f');
INSERT INTO public.m_external_event_configuration (type, enabled) VALUES ('LoanChargeRefundBusinessEvent', 'f');
INSERT INTO public.m_external_event_configuration (type, enabled) VALUES ('LoanCreditBalanceRefundPostBusinessEvent', 'f');
INSERT INTO public.m_external_event_configuration (type, enabled) VALUES ('LoanCreditBalanceRefundPreBusinessEvent', 'f');
INSERT INTO public.m_external_event_configuration (type, enabled) VALUES ('LoanDisbursalTransactionBusinessEvent', 'f');
INSERT INTO public.m_external_event_configuration (type, enabled) VALUES ('LoanForeClosurePostBusinessEvent', 'f');
INSERT INTO public.m_external_event_configuration (type, enabled) VALUES ('LoanForeClosurePreBusinessEvent', 'f');
INSERT INTO public.m_external_event_configuration (type, enabled) VALUES ('LoanRefundPostBusinessEvent', 'f');
INSERT INTO public.m_external_event_configuration (type, enabled) VALUES ('LoanRefundPreBusinessEvent', 'f');
INSERT INTO public.m_external_event_configuration (type, enabled) VALUES ('LoanTransactionGoodwillCreditPostBusinessEvent', 'f');
INSERT INTO public.m_external_event_configuration (type, enabled) VALUES ('LoanTransactionGoodwillCreditPreBusinessEvent', 'f');
INSERT INTO public.m_external_event_configuration (type, enabled) VALUES ('LoanTransactionMakeRepaymentPostBusinessEvent', 'f');
INSERT INTO public.m_external_event_configuration (type, enabled) VALUES ('LoanTransactionMakeRepaymentPreBusinessEvent', 'f');
INSERT INTO public.m_external_event_configuration (type, enabled) VALUES ('LoanTransactionMerchantIssuedRefundPostBusinessEvent', 'f');
INSERT INTO public.m_external_event_configuration (type, enabled) VALUES ('LoanTransactionMerchantIssuedRefundPreBusinessEvent', 'f');
INSERT INTO public.m_external_event_configuration (type, enabled) VALUES ('LoanTransactionPayoutRefundPostBusinessEvent', 'f');
INSERT INTO public.m_external_event_configuration (type, enabled) VALUES ('LoanTransactionPayoutRefundPreBusinessEvent', 'f');
INSERT INTO public.m_external_event_configuration (type, enabled) VALUES ('LoanTransactionRecoveryPaymentPostBusinessEvent', 'f');
INSERT INTO public.m_external_event_configuration (type, enabled) VALUES ('LoanTransactionRecoveryPaymentPreBusinessEvent', 'f');
INSERT INTO public.m_external_event_configuration (type, enabled) VALUES ('LoanUndoWrittenOffBusinessEvent', 'f');
INSERT INTO public.m_external_event_configuration (type, enabled) VALUES ('LoanWaiveInterestBusinessEvent', 'f');
INSERT INTO public.m_external_event_configuration (type, enabled) VALUES ('LoanWrittenOffPostBusinessEvent', 'f');
INSERT INTO public.m_external_event_configuration (type, enabled) VALUES ('LoanWrittenOffPreBusinessEvent', 'f');
INSERT INTO public.m_external_event_configuration (type, enabled) VALUES ('LoanAcceptTransferBusinessEvent', 'f');
INSERT INTO public.m_external_event_configuration (type, enabled) VALUES ('LoanAdjustTransactionBusinessEvent', 'f');
INSERT INTO public.m_external_event_configuration (type, enabled) VALUES ('LoanApplyOverdueChargeBusinessEvent', 'f');
INSERT INTO public.m_external_event_configuration (type, enabled) VALUES ('LoanApprovedBusinessEvent', 'f');
INSERT INTO public.m_external_event_configuration (type, enabled) VALUES ('LoanBalanceChangedBusinessEvent', 'f');
INSERT INTO public.m_external_event_configuration (type, enabled) VALUES ('LoanChargebackTransactionBusinessEvent', 'f');
INSERT INTO public.m_external_event_configuration (type, enabled) VALUES ('LoanCloseAsRescheduleBusinessEvent', 'f');
INSERT INTO public.m_external_event_configuration (type, enabled) VALUES ('LoanCloseBusinessEvent', 'f');
INSERT INTO public.m_external_event_configuration (type, enabled) VALUES ('LoanCreatedBusinessEvent', 'f');
INSERT INTO public.m_external_event_configuration (type, enabled) VALUES ('LoanDisbursalBusinessEvent', 'f');
INSERT INTO public.m_external_event_configuration (type, enabled) VALUES ('LoanInitiateTransferBusinessEvent', 'f');
INSERT INTO public.m_external_event_configuration (type, enabled) VALUES ('LoanInterestRecalculationBusinessEvent', 'f');
INSERT INTO public.m_external_event_configuration (type, enabled) VALUES ('LoanReassignOfficerBusinessEvent', 'f');
INSERT INTO public.m_external_event_configuration (type, enabled) VALUES ('LoanRejectedBusinessEvent', 'f');
INSERT INTO public.m_external_event_configuration (type, enabled) VALUES ('LoanRejectTransferBusinessEvent', 'f');
INSERT INTO public.m_external_event_configuration (type, enabled) VALUES ('LoanRemoveOfficerBusinessEvent', 'f');
INSERT INTO public.m_external_event_configuration (type, enabled) VALUES ('LoanRescheduledDueCalendarChangeBusinessEvent', 'f');
INSERT INTO public.m_external_event_configuration (type, enabled) VALUES ('LoanRescheduledDueHolidayBusinessEvent', 'f');
INSERT INTO public.m_external_event_configuration (type, enabled) VALUES ('LoanScheduleVariationsAddedBusinessEvent', 'f');
INSERT INTO public.m_external_event_configuration (type, enabled) VALUES ('LoanScheduleVariationsDeletedBusinessEvent', 'f');
INSERT INTO public.m_external_event_configuration (type, enabled) VALUES ('LoanStatusChangedBusinessEvent', 'f');
INSERT INTO public.m_external_event_configuration (type, enabled) VALUES ('LoanUndoApprovalBusinessEvent', 'f');
INSERT INTO public.m_external_event_configuration (type, enabled) VALUES ('LoanUndoDisbursalBusinessEvent', 'f');
INSERT INTO public.m_external_event_configuration (type, enabled) VALUES ('LoanUndoLastDisbursalBusinessEvent', 'f');
INSERT INTO public.m_external_event_configuration (type, enabled) VALUES ('LoanUpdateDisbursementDataBusinessEvent', 'f');
INSERT INTO public.m_external_event_configuration (type, enabled) VALUES ('LoanWithdrawTransferBusinessEvent', 'f');
INSERT INTO public.m_external_event_configuration (type, enabled) VALUES ('SavingsDepositBusinessEvent', 'f');
INSERT INTO public.m_external_event_configuration (type, enabled) VALUES ('SavingsWithdrawalBusinessEvent', 'f');
INSERT INTO public.m_external_event_configuration (type, enabled) VALUES ('SavingsActivateBusinessEvent', 'f');
INSERT INTO public.m_external_event_configuration (type, enabled) VALUES ('SavingsApproveBusinessEvent', 'f');
INSERT INTO public.m_external_event_configuration (type, enabled) VALUES ('SavingsCloseBusinessEvent', 'f');
INSERT INTO public.m_external_event_configuration (type, enabled) VALUES ('SavingsCreateBusinessEvent', 'f');
INSERT INTO public.m_external_event_configuration (type, enabled) VALUES ('SavingsPostInterestBusinessEvent', 'f');
INSERT INTO public.m_external_event_configuration (type, enabled) VALUES ('SavingsRejectBusinessEvent', 'f');
INSERT INTO public.m_external_event_configuration (type, enabled) VALUES ('ShareAccountApproveBusinessEvent', 'f');
INSERT INTO public.m_external_event_configuration (type, enabled) VALUES ('ShareAccountCreateBusinessEvent', 'f');
INSERT INTO public.m_external_event_configuration (type, enabled) VALUES ('ShareProductDividentsCreateBusinessEvent', 'f');
INSERT INTO public.m_external_event_configuration (type, enabled) VALUES ('LoanRepaymentDueBusinessEvent', 'f');
INSERT INTO public.m_external_event_configuration (type, enabled) VALUES ('LoanRepaymentOverdueBusinessEvent', 'f');
INSERT INTO public.m_external_event_configuration (type, enabled) VALUES ('LoanChargeAdjustmentPostBusinessEvent', 'f');
INSERT INTO public.m_external_event_configuration (type, enabled) VALUES ('LoanChargeAdjustmentPreBusinessEvent', 'f');
INSERT INTO public.m_external_event_configuration (type, enabled) VALUES ('LoanDelinquencyRangeChangeBusinessEvent', 'f');
INSERT INTO public.m_external_event_configuration (type, enabled) VALUES ('LoanAccountsStayedLockedBusinessEvent', 'f');
INSERT INTO public.m_external_event_configuration (type, enabled) VALUES ('LoanChargeOffPreBusinessEvent', 'f');
INSERT INTO public.m_external_event_configuration (type, enabled) VALUES ('LoanChargeOffPostBusinessEvent', 'f');
INSERT INTO public.m_external_event_configuration (type, enabled) VALUES ('LoanUndoChargeOffBusinessEvent', 'f');
INSERT INTO public.m_external_event_configuration (type, enabled) VALUES ('LoanAccrualTransactionCreatedBusinessEvent', 'f');
INSERT INTO public.m_external_event_configuration (type, enabled) VALUES ('LoanRescheduledDueAdjustScheduleBusinessEvent', 'f');
INSERT INTO public.m_external_event_configuration (type, enabled) VALUES ('LoanReAgeTransactionBusinessEvent', 'f');
INSERT INTO public.m_external_event_configuration (type, enabled) VALUES ('LoanUndoReAgeTransactionBusinessEvent', 'f');
INSERT INTO public.m_external_event_configuration (type, enabled) VALUES ('LoanReAmortizeTransactionBusinessEvent', 'f');
INSERT INTO public.m_external_event_configuration (type, enabled) VALUES ('LoanUndoReAmortizeTransactionBusinessEvent', 'f');
INSERT INTO public.m_external_event_configuration (type, enabled) VALUES ('LoanReAmortizeBusinessEvent', 'f');
INSERT INTO public.m_external_event_configuration (type, enabled) VALUES ('LoanUndoReAmortizeBusinessEvent', 'f');
INSERT INTO public.m_external_event_configuration (type, enabled) VALUES ('LoanReAgeBusinessEvent', 'f');
INSERT INTO public.m_external_event_configuration (type, enabled) VALUES ('LoanUndoReAgeBusinessEvent', 'f');
INSERT INTO public.m_external_event_configuration (type, enabled) VALUES ('LoanTransactionInterestPaymentWaiverPostBusinessEvent', 'f');
INSERT INTO public.m_external_event_configuration (type, enabled) VALUES ('LoanTransactionInterestPaymentWaiverPreBusinessEvent', 'f');
INSERT INTO public.m_external_event_configuration (type, enabled) VALUES ('LoanTransactionAccrualActivityPostBusinessEvent', 'f');
INSERT INTO public.m_external_event_configuration (type, enabled) VALUES ('LoanTransactionAccrualActivityPreBusinessEvent', 'f');
INSERT INTO public.m_external_event_configuration (type, enabled) VALUES ('LoanTransactionInterestRefundPostBusinessEvent', 'f');
INSERT INTO public.m_external_event_configuration (type, enabled) VALUES ('LoanTransactionInterestRefundPreBusinessEvent', 'f');
INSERT INTO public.m_external_event_configuration (type, enabled) VALUES ('LoanCapitalizedIncomeAmortizationTransactionCreatedBusinessEvent', 'f');
INSERT INTO public.m_external_event_configuration (type, enabled) VALUES ('LoanCapitalizedIncomeAdjustmentTransactionCreatedBusinessEvent', 'f');
INSERT INTO public.m_external_event_configuration (type, enabled) VALUES ('LoanCapitalizedIncomeAmortizationAdjustmentTransactionCreatedBusinessEvent', 'f');
INSERT INTO public.m_external_event_configuration (type, enabled) VALUES ('LoanCapitalizedIncomeTransactionCreatedBusinessEvent', 'f');
INSERT INTO public.m_external_event_configuration (type, enabled) VALUES ('DocumentCreatedBusinessEvent', 'f');
INSERT INTO public.m_external_event_configuration (type, enabled) VALUES ('DocumentDeletedBusinessEvent', 'f');
INSERT INTO public.m_external_event_configuration (type, enabled) VALUES ('LoanWithdrawnByApplicantBusinessEvent', 'f');
INSERT INTO public.m_external_event_configuration (type, enabled) VALUES ('LoanApplicationModifiedBusinessEvent', 'f');
INSERT INTO public.m_external_event_configuration (type, enabled) VALUES ('LoanBuyDownFeeTransactionCreatedBusinessEvent', 'f');
INSERT INTO public.m_external_event_configuration (type, enabled) VALUES ('LoanBuyDownFeeAdjustmentTransactionCreatedBusinessEvent', 'f');
INSERT INTO public.m_external_event_configuration (type, enabled) VALUES ('LoanBuyDownFeeAmortizationTransactionCreatedBusinessEvent', 'f');
INSERT INTO public.m_external_event_configuration (type, enabled) VALUES ('LoanBuyDownFeeAmortizationAdjustmentTransactionCreatedBusinessEvent', 'f');
INSERT INTO public.m_external_event_configuration (type, enabled) VALUES ('LoanApprovedAmountChangedBusinessEvent', 'f');
INSERT INTO public.m_external_event_configuration (type, enabled) VALUES ('SavingsAccountForceWithdrawalBusinessEvent', 'f');


-- Data for Name: m_family_members; Type: TABLE DATA; Schema: public; Owner: -



-- Data for Name: m_field_configuration; Type: TABLE DATA; Schema: public; Owner: -

INSERT INTO public.m_field_configuration (id, entity, subentity, field, is_enabled, is_mandatory, validation_regex) VALUES ('1', 'ADDRESS', 'CLIENT', 'addressType', 't', 'f', '');
INSERT INTO public.m_field_configuration (id, entity, subentity, field, is_enabled, is_mandatory, validation_regex) VALUES ('3', 'ADDRESS', 'CLIENT', 'addressLine1', 't', 'f', '');
INSERT INTO public.m_field_configuration (id, entity, subentity, field, is_enabled, is_mandatory, validation_regex) VALUES ('4', 'ADDRESS', 'CLIENT', 'addressLine2', 't', 'f', '');
INSERT INTO public.m_field_configuration (id, entity, subentity, field, is_enabled, is_mandatory, validation_regex) VALUES ('5', 'ADDRESS', 'CLIENT', 'addressLine3', 't', 'f', '');
INSERT INTO public.m_field_configuration (id, entity, subentity, field, is_enabled, is_mandatory, validation_regex) VALUES ('6', 'ADDRESS', 'CLIENT', 'townVillage', 'f', 'f', '');
INSERT INTO public.m_field_configuration (id, entity, subentity, field, is_enabled, is_mandatory, validation_regex) VALUES ('7', 'ADDRESS', 'CLIENT', 'city', 't', 'f', '');
INSERT INTO public.m_field_configuration (id, entity, subentity, field, is_enabled, is_mandatory, validation_regex) VALUES ('8', 'ADDRESS', 'CLIENT', 'countyDistrict', 'f', 'f', '');
INSERT INTO public.m_field_configuration (id, entity, subentity, field, is_enabled, is_mandatory, validation_regex) VALUES ('9', 'ADDRESS', 'CLIENT', 'stateProvinceId', 't', 'f', '');
INSERT INTO public.m_field_configuration (id, entity, subentity, field, is_enabled, is_mandatory, validation_regex) VALUES ('10', 'ADDRESS', 'CLIENT', 'countryId', 't', 'f', '');
INSERT INTO public.m_field_configuration (id, entity, subentity, field, is_enabled, is_mandatory, validation_regex) VALUES ('11', 'ADDRESS', 'CLIENT', 'postalCode', 't', 'f', '');
INSERT INTO public.m_field_configuration (id, entity, subentity, field, is_enabled, is_mandatory, validation_regex) VALUES ('12', 'ADDRESS', 'CLIENT', 'latitude', 'f', 'f', '');
INSERT INTO public.m_field_configuration (id, entity, subentity, field, is_enabled, is_mandatory, validation_regex) VALUES ('13', 'ADDRESS', 'CLIENT', 'longitude', 'f', 'f', '');
INSERT INTO public.m_field_configuration (id, entity, subentity, field, is_enabled, is_mandatory, validation_regex) VALUES ('14', 'ADDRESS', 'CLIENT', 'createdBy', 't', 'f', '');
INSERT INTO public.m_field_configuration (id, entity, subentity, field, is_enabled, is_mandatory, validation_regex) VALUES ('15', 'ADDRESS', 'CLIENT', 'createdOn', 't', 'f', '');
INSERT INTO public.m_field_configuration (id, entity, subentity, field, is_enabled, is_mandatory, validation_regex) VALUES ('16', 'ADDRESS', 'CLIENT', 'updatedBy', 't', 'f', '');
INSERT INTO public.m_field_configuration (id, entity, subentity, field, is_enabled, is_mandatory, validation_regex) VALUES ('17', 'ADDRESS', 'CLIENT', 'updatedOn', 't', 'f', '');
INSERT INTO public.m_field_configuration (id, entity, subentity, field, is_enabled, is_mandatory, validation_regex) VALUES ('18', 'ADDRESS', 'CLIENT', 'isActive', 't', 'f', '');


-- Data for Name: m_floating_rates; Type: TABLE DATA; Schema: public; Owner: -



-- Data for Name: m_floating_rates_periods; Type: TABLE DATA; Schema: public; Owner: -



-- Data for Name: m_group_client; Type: TABLE DATA; Schema: public; Owner: -



-- Data for Name: m_group_roles; Type: TABLE DATA; Schema: public; Owner: -



-- Data for Name: m_guarantor; Type: TABLE DATA; Schema: public; Owner: -



-- Data for Name: m_portfolio_account_associations; Type: TABLE DATA; Schema: public; Owner: -



-- Data for Name: m_guarantor_funding_details; Type: TABLE DATA; Schema: public; Owner: -



-- Data for Name: m_guarantor_transaction; Type: TABLE DATA; Schema: public; Owner: -



-- Data for Name: m_holiday; Type: TABLE DATA; Schema: public; Owner: -



-- Data for Name: m_holiday_office; Type: TABLE DATA; Schema: public; Owner: -



-- Data for Name: m_hook_templates; Type: TABLE DATA; Schema: public; Owner: -

INSERT INTO public.m_hook_templates (id, name) VALUES ('1', 'Web');
INSERT INTO public.m_hook_templates (id, name) VALUES ('2', 'SMS Bridge');
INSERT INTO public.m_hook_templates (id, name) VALUES ('3', 'Elastic Search');
INSERT INTO public.m_hook_templates (id, name) VALUES ('4', 'Message Gateway');


-- Data for Name: m_template; Type: TABLE DATA; Schema: public; Owner: -



-- Data for Name: m_hook; Type: TABLE DATA; Schema: public; Owner: -



-- Data for Name: m_hook_configuration; Type: TABLE DATA; Schema: public; Owner: -



-- Data for Name: m_hook_registered_events; Type: TABLE DATA; Schema: public; Owner: -



-- Data for Name: m_hook_schema; Type: TABLE DATA; Schema: public; Owner: -

INSERT INTO public.m_hook_schema (id, hook_template_id, field_type, field_name, placeholder, optional) VALUES ('1', '1', 'string', 'Payload URL', NULL, 'f');
INSERT INTO public.m_hook_schema (id, hook_template_id, field_type, field_name, placeholder, optional) VALUES ('2', '1', 'string', 'Content Type', 'json / form', 'f');
INSERT INTO public.m_hook_schema (id, hook_template_id, field_type, field_name, placeholder, optional) VALUES ('3', '2', 'string', 'Payload URL', NULL, 'f');
INSERT INTO public.m_hook_schema (id, hook_template_id, field_type, field_name, placeholder, optional) VALUES ('4', '2', 'string', 'SMS Provider', NULL, 'f');
INSERT INTO public.m_hook_schema (id, hook_template_id, field_type, field_name, placeholder, optional) VALUES ('5', '2', 'string', 'Phone Number', NULL, 'f');
INSERT INTO public.m_hook_schema (id, hook_template_id, field_type, field_name, placeholder, optional) VALUES ('6', '2', 'string', 'SMS Provider Token', NULL, 'f');
INSERT INTO public.m_hook_schema (id, hook_template_id, field_type, field_name, placeholder, optional) VALUES ('7', '2', 'string', 'SMS Provider Account Id', NULL, 'f');
INSERT INTO public.m_hook_schema (id, hook_template_id, field_type, field_name, placeholder, optional) VALUES ('8', '3', 'string', 'Payload URL', 'http://<host>/<index name>/<type name>', 'f');
INSERT INTO public.m_hook_schema (id, hook_template_id, field_type, field_name, placeholder, optional) VALUES ('9', '3', 'string', 'Content Type', 'json', 'f');
INSERT INTO public.m_hook_schema (id, hook_template_id, field_type, field_name, placeholder, optional) VALUES ('10', '3', 'string', 'Index Name', NULL, 't');
INSERT INTO public.m_hook_schema (id, hook_template_id, field_type, field_name, placeholder, optional) VALUES ('11', '4', 'string', 'SMS Provider Id', NULL, 'f');


-- Data for Name: m_import_document; Type: TABLE DATA; Schema: public; Owner: -



-- Data for Name: m_interest_rate_slab; Type: TABLE DATA; Schema: public; Owner: -



-- Data for Name: m_interest_incentives; Type: TABLE DATA; Schema: public; Owner: -



-- Data for Name: m_journal_entry_aggregation_summary; Type: TABLE DATA; Schema: public; Owner: -



-- Data for Name: m_journal_entry_aggregation_tracking; Type: TABLE DATA; Schema: public; Owner: -



-- Data for Name: m_loan_account_locks; Type: TABLE DATA; Schema: public; Owner: -



-- Data for Name: m_loan_amortization_allocation_mapping; Type: TABLE DATA; Schema: public; Owner: -



-- Data for Name: m_loan_approved_amount_history; Type: TABLE DATA; Schema: public; Owner: -



-- Data for Name: m_loan_arrears_aging; Type: TABLE DATA; Schema: public; Owner: -



-- Data for Name: m_loan_buy_down_fee_balance; Type: TABLE DATA; Schema: public; Owner: -



-- Data for Name: m_loan_capitalized_income_balance; Type: TABLE DATA; Schema: public; Owner: -



-- Data for Name: m_loan_charge; Type: TABLE DATA; Schema: public; Owner: -



-- Data for Name: m_loan_charge_paid_by; Type: TABLE DATA; Schema: public; Owner: -



-- Data for Name: m_loan_collateral; Type: TABLE DATA; Schema: public; Owner: -



-- Data for Name: m_loan_collateral_management; Type: TABLE DATA; Schema: public; Owner: -



-- Data for Name: m_loan_delinquency_tag_history; Type: TABLE DATA; Schema: public; Owner: -



-- Data for Name: m_loan_disbursement_detail; Type: TABLE DATA; Schema: public; Owner: -



-- Data for Name: m_loan_repayment_schedule; Type: TABLE DATA; Schema: public; Owner: -



-- Data for Name: m_loan_installment_charge; Type: TABLE DATA; Schema: public; Owner: -



-- Data for Name: m_loan_interest_recalculation_additional_details; Type: TABLE DATA; Schema: public; Owner: -



-- Data for Name: m_loan_officer_assignment_history; Type: TABLE DATA; Schema: public; Owner: -



-- Data for Name: m_loan_overdue_installment_charge; Type: TABLE DATA; Schema: public; Owner: -



-- Data for Name: m_rate; Type: TABLE DATA; Schema: public; Owner: -



-- Data for Name: m_loan_rate; Type: TABLE DATA; Schema: public; Owner: -



-- Data for Name: m_loan_reage_parameter; Type: TABLE DATA; Schema: public; Owner: -



-- Data for Name: m_loan_recalculation_details; Type: TABLE DATA; Schema: public; Owner: -



-- Data for Name: m_loan_reschedule_request; Type: TABLE DATA; Schema: public; Owner: -



-- Data for Name: m_loan_repayment_schedule_history; Type: TABLE DATA; Schema: public; Owner: -



-- Data for Name: m_loan_term_variations; Type: TABLE DATA; Schema: public; Owner: -



-- Data for Name: m_loan_reschedule_request_term_variations_mapping; Type: TABLE DATA; Schema: public; Owner: -



-- Data for Name: m_loan_topup; Type: TABLE DATA; Schema: public; Owner: -



-- Data for Name: m_loan_tranche_charges; Type: TABLE DATA; Schema: public; Owner: -



-- Data for Name: m_loan_tranche_disbursement_charge; Type: TABLE DATA; Schema: public; Owner: -



-- Data for Name: m_loan_transaction_relation; Type: TABLE DATA; Schema: public; Owner: -



-- Data for Name: m_loan_transaction_repayment_schedule_mapping; Type: TABLE DATA; Schema: public; Owner: -



-- Data for Name: m_provision_category; Type: TABLE DATA; Schema: public; Owner: -

INSERT INTO public.m_provision_category (id, category_name, description) VALUES ('1', 'STANDARD', 'Punctual Payment without any dues');
INSERT INTO public.m_provision_category (id, category_name, description) VALUES ('2', 'SUB-STANDARD', 'Principal and/or Interest overdue by x days');
INSERT INTO public.m_provision_category (id, category_name, description) VALUES ('3', 'DOUBTFUL', 'Principal and/or Interest overdue by x days and less than y');
INSERT INTO public.m_provision_category (id, category_name, description) VALUES ('4', 'LOSS', 'Principal and/or Interest overdue by y days');


-- Data for Name: m_provisioning_criteria; Type: TABLE DATA; Schema: public; Owner: -



-- Data for Name: m_provisioning_history; Type: TABLE DATA; Schema: public; Owner: -



-- Data for Name: m_loanproduct_provisioning_entry; Type: TABLE DATA; Schema: public; Owner: -



-- Data for Name: m_loanproduct_provisioning_mapping; Type: TABLE DATA; Schema: public; Owner: -



-- Data for Name: m_mandatory_savings_schedule; Type: TABLE DATA; Schema: public; Owner: -



-- Data for Name: m_note; Type: TABLE DATA; Schema: public; Owner: -



-- Data for Name: m_office_transaction; Type: TABLE DATA; Schema: public; Owner: -



-- Data for Name: m_organisation_currency; Type: TABLE DATA; Schema: public; Owner: -

INSERT INTO public.m_organisation_currency (id, code, decimal_places, currency_multiplesof, name, display_symbol, internationalized_name_code) VALUES ('21', 'USD', '2', NULL, 'US Dollar', '$', 'currency.USD');


-- Data for Name: m_password_validation_policy; Type: TABLE DATA; Schema: public; Owner: -

INSERT INTO public.m_password_validation_policy (id, regex, description, active, key) VALUES ('2', '^(?=.*\d)(?=.*[a-z])(?=.*[A-Z])(?!.*\s).{6,50}$', 'Password must be at least 6 characters, no more than 50 characters long, must include at least one upper case letter, one lower case letter, one numeric digit and no space', 'f', 'secure');
INSERT INTO public.m_password_validation_policy (id, regex, description, active, key) VALUES ('1', '^.{1,50}$', 'Password most be at least 1 character and not more that 50 characters long', 'f', 'simple');
INSERT INTO public.m_password_validation_policy (id, regex, description, active, key) VALUES ('3', '^(?!.*(.)\1)(?!.*\s)(?=.*\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[^\w\s]).{12,50}$', 'Password must be 12 to 50 characters long, containing at least one uppercase letter, one lowercase letter, one numeric digit, and one special character, with no spaces or consecutive repeating characters', 't', 'strong');


-- Data for Name: m_permission; Type: TABLE DATA; Schema: public; Owner: -

INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('1', 'special', 'ALL_FUNCTIONS', NULL, NULL, 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('2', 'special', 'ALL_FUNCTIONS_READ', NULL, NULL, 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('3', 'special', 'CHECKER_SUPER_USER', NULL, NULL, 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('4', 'special', 'REPORTING_SUPER_USER', NULL, NULL, 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('5', 'authorisation', 'READ_PERMISSION', 'PERMISSION', 'READ', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('6', 'authorisation', 'PERMISSIONS_ROLE', 'ROLE', 'PERMISSIONS', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('7', 'authorisation', 'CREATE_ROLE', 'ROLE', 'CREATE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('8', 'authorisation', 'CREATE_ROLE_CHECKER', 'ROLE', 'CREATE_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('9', 'authorisation', 'READ_ROLE', 'ROLE', 'READ', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('10', 'authorisation', 'UPDATE_ROLE', 'ROLE', 'UPDATE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('11', 'authorisation', 'UPDATE_ROLE_CHECKER', 'ROLE', 'UPDATE_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('12', 'authorisation', 'DELETE_ROLE', 'ROLE', 'DELETE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('13', 'authorisation', 'DELETE_ROLE_CHECKER', 'ROLE', 'DELETE_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('14', 'authorisation', 'CREATE_USER', 'USER', 'CREATE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('15', 'authorisation', 'CREATE_USER_CHECKER', 'USER', 'CREATE_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('16', 'authorisation', 'READ_USER', 'USER', 'READ', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('17', 'authorisation', 'UPDATE_USER', 'USER', 'UPDATE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('18', 'authorisation', 'UPDATE_USER_CHECKER', 'USER', 'UPDATE_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('19', 'authorisation', 'DELETE_USER', 'USER', 'DELETE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('20', 'authorisation', 'DELETE_USER_CHECKER', 'USER', 'DELETE_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('21', 'configuration', 'READ_CONFIGURATION', 'CONFIGURATION', 'READ', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('22', 'configuration', 'UPDATE_CONFIGURATION', 'CONFIGURATION', 'UPDATE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('23', 'configuration', 'UPDATE_CONFIGURATION_CHECKER', 'CONFIGURATION', 'UPDATE_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('24', 'configuration', 'READ_CODE', 'CODE', 'READ', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('25', 'configuration', 'CREATE_CODE', 'CODE', 'CREATE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('26', 'configuration', 'CREATE_CODE_CHECKER', 'CODE', 'CREATE_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('27', 'configuration', 'UPDATE_CODE', 'CODE', 'UPDATE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('28', 'configuration', 'UPDATE_CODE_CHECKER', 'CODE', 'UPDATE_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('29', 'configuration', 'DELETE_CODE', 'CODE', 'DELETE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('30', 'configuration', 'DELETE_CODE_CHECKER', 'CODE', 'DELETE_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('31', 'configuration', 'READ_CODEVALUE', 'CODEVALUE', 'READ', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('32', 'configuration', 'CREATE_CODEVALUE', 'CODEVALUE', 'CREATE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('33', 'configuration', 'CREATE_CODEVALUE_CHECKER', 'CODEVALUE', 'CREATE_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('34', 'configuration', 'UPDATE_CODEVALUE', 'CODEVALUE', 'UPDATE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('35', 'configuration', 'UPDATE_CODEVALUE_CHECKER', 'CODEVALUE', 'UPDATE_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('36', 'configuration', 'DELETE_CODEVALUE', 'CODEVALUE', 'DELETE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('37', 'configuration', 'DELETE_CODEVALUE_CHECKER', 'CODEVALUE', 'DELETE_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('38', 'configuration', 'READ_CURRENCY', 'CURRENCY', 'READ', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('39', 'configuration', 'UPDATE_CURRENCY', 'CURRENCY', 'UPDATE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('40', 'configuration', 'UPDATE_CURRENCY_CHECKER', 'CURRENCY', 'UPDATE_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('41', 'configuration', 'UPDATE_PERMISSION', 'PERMISSION', 'UPDATE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('42', 'configuration', 'UPDATE_PERMISSION_CHECKER', 'PERMISSION', 'UPDATE_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('43', 'configuration', 'READ_DATATABLE', 'DATATABLE', 'READ', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('44', 'configuration', 'REGISTER_DATATABLE', 'DATATABLE', 'REGISTER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('45', 'configuration', 'REGISTER_DATATABLE_CHECKER', 'DATATABLE', 'REGISTER_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('46', 'configuration', 'DEREGISTER_DATATABLE', 'DATATABLE', 'DEREGISTER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('47', 'configuration', 'DEREGISTER_DATATABLE_CHECKER', 'DATATABLE', 'DEREGISTER_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('48', 'configuration', 'READ_AUDIT', 'AUDIT', 'READ', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('49', 'configuration', 'CREATE_CALENDAR', 'CALENDAR', 'CREATE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('50', 'configuration', 'READ_CALENDAR', 'CALENDAR', 'READ', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('51', 'configuration', 'UPDATE_CALENDAR', 'CALENDAR', 'UPDATE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('52', 'configuration', 'DELETE_CALENDAR', 'CALENDAR', 'DELETE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('53', 'configuration', 'CREATE_CALENDAR_CHECKER', 'CALENDAR', 'CREATE_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('54', 'configuration', 'UPDATE_CALENDAR_CHECKER', 'CALENDAR', 'UPDATE_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('55', 'configuration', 'DELETE_CALENDAR_CHECKER', 'CALENDAR', 'DELETE_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('57', 'organisation', 'READ_CHARGE', 'CHARGE', 'READ', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('58', 'organisation', 'CREATE_CHARGE', 'CHARGE', 'CREATE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('59', 'organisation', 'CREATE_CHARGE_CHECKER', 'CHARGE', 'CREATE_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('60', 'organisation', 'UPDATE_CHARGE', 'CHARGE', 'UPDATE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('61', 'organisation', 'UPDATE_CHARGE_CHECKER', 'CHARGE', 'UPDATE_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('62', 'organisation', 'DELETE_CHARGE', 'CHARGE', 'DELETE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('63', 'organisation', 'DELETE_CHARGE_CHECKER', 'CHARGE', 'DELETE_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('64', 'organisation', 'READ_FUND', 'FUND', 'READ', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('65', 'organisation', 'CREATE_FUND', 'FUND', 'CREATE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('66', 'organisation', 'CREATE_FUND_CHECKER', 'FUND', 'CREATE_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('67', 'organisation', 'UPDATE_FUND', 'FUND', 'UPDATE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('68', 'organisation', 'UPDATE_FUND_CHECKER', 'FUND', 'UPDATE_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('69', 'organisation', 'DELETE_FUND', 'FUND', 'DELETE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('70', 'organisation', 'DELETE_FUND_CHECKER', 'FUND', 'DELETE_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('71', 'organisation', 'READ_LOANPRODUCT', 'LOANPRODUCT', 'READ', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('72', 'organisation', 'CREATE_LOANPRODUCT', 'LOANPRODUCT', 'CREATE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('73', 'organisation', 'CREATE_LOANPRODUCT_CHECKER', 'LOANPRODUCT', 'CREATE_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('74', 'organisation', 'UPDATE_LOANPRODUCT', 'LOANPRODUCT', 'UPDATE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('75', 'organisation', 'UPDATE_LOANPRODUCT_CHECKER', 'LOANPRODUCT', 'UPDATE_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('76', 'organisation', 'DELETE_LOANPRODUCT', 'LOANPRODUCT', 'DELETE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('77', 'organisation', 'DELETE_LOANPRODUCT_CHECKER', 'LOANPRODUCT', 'DELETE_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('78', 'organisation', 'READ_OFFICE', 'OFFICE', 'READ', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('79', 'organisation', 'CREATE_OFFICE', 'OFFICE', 'CREATE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('80', 'organisation', 'CREATE_OFFICE_CHECKER', 'OFFICE', 'CREATE_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('81', 'organisation', 'UPDATE_OFFICE', 'OFFICE', 'UPDATE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('82', 'organisation', 'UPDATE_OFFICE_CHECKER', 'OFFICE', 'UPDATE_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('83', 'organisation', 'READ_OFFICETRANSACTION', 'OFFICETRANSACTION', 'READ', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('84', 'organisation', 'DELETE_OFFICE_CHECKER', 'OFFICE', 'DELETE_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('85', 'organisation', 'CREATE_OFFICETRANSACTION', 'OFFICETRANSACTION', 'CREATE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('86', 'organisation', 'CREATE_OFFICETRANSACTION_CHECKER', 'OFFICETRANSACTION', 'CREATE_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('87', 'organisation', 'DELETE_OFFICETRANSACTION', 'OFFICETRANSACTION', 'DELETE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('88', 'organisation', 'DELETE_OFFICETRANSACTION_CHECKER', 'OFFICETRANSACTION', 'DELETE_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('89', 'organisation', 'READ_STAFF', 'STAFF', 'READ', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('90', 'organisation', 'CREATE_STAFF', 'STAFF', 'CREATE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('91', 'organisation', 'CREATE_STAFF_CHECKER', 'STAFF', 'CREATE_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('92', 'organisation', 'UPDATE_STAFF', 'STAFF', 'UPDATE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('93', 'organisation', 'UPDATE_STAFF_CHECKER', 'STAFF', 'UPDATE_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('94', 'organisation', 'DELETE_STAFF', 'STAFF', 'DELETE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('95', 'organisation', 'DELETE_STAFF_CHECKER', 'STAFF', 'DELETE_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('96', 'organisation', 'READ_SAVINGSPRODUCT', 'SAVINGSPRODUCT', 'READ', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('97', 'organisation', 'CREATE_SAVINGSPRODUCT', 'SAVINGSPRODUCT', 'CREATE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('98', 'organisation', 'CREATE_SAVINGSPRODUCT_CHECKER', 'SAVINGSPRODUCT', 'CREATE_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('99', 'organisation', 'UPDATE_SAVINGSPRODUCT', 'SAVINGSPRODUCT', 'UPDATE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('100', 'organisation', 'UPDATE_SAVINGSPRODUCT_CHECKER', 'SAVINGSPRODUCT', 'UPDATE_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('101', 'organisation', 'DELETE_SAVINGSPRODUCT', 'SAVINGSPRODUCT', 'DELETE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('102', 'organisation', 'DELETE_SAVINGSPRODUCT_CHECKER', 'SAVINGSPRODUCT', 'DELETE_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('103', 'portfolio', 'READ_LOAN', 'LOAN', 'READ', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('104', 'portfolio', 'CREATE_LOAN', 'LOAN', 'CREATE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('105', 'portfolio', 'CREATE_LOAN_CHECKER', 'LOAN', 'CREATE_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('106', 'portfolio', 'UPDATE_LOAN', 'LOAN', 'UPDATE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('107', 'portfolio', 'UPDATE_LOAN_CHECKER', 'LOAN', 'UPDATE_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('108', 'portfolio', 'DELETE_LOAN', 'LOAN', 'DELETE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('109', 'portfolio', 'DELETE_LOAN_CHECKER', 'LOAN', 'DELETE_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('110', 'portfolio', 'READ_CLIENT', 'CLIENT', 'READ', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('111', 'portfolio', 'CREATE_CLIENT', 'CLIENT', 'CREATE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('112', 'portfolio', 'CREATE_CLIENT_CHECKER', 'CLIENT', 'CREATE_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('113', 'portfolio', 'UPDATE_CLIENT', 'CLIENT', 'UPDATE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('114', 'portfolio', 'UPDATE_CLIENT_CHECKER', 'CLIENT', 'UPDATE_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('115', 'portfolio', 'DELETE_CLIENT', 'CLIENT', 'DELETE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('116', 'portfolio', 'DELETE_CLIENT_CHECKER', 'CLIENT', 'DELETE_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('117', 'portfolio', 'READ_CLIENTIMAGE', 'CLIENTIMAGE', 'READ', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('118', 'portfolio', 'CREATE_CLIENTIMAGE', 'CLIENTIMAGE', 'CREATE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('119', 'portfolio', 'CREATE_CLIENTIMAGE_CHECKER', 'CLIENTIMAGE', 'CREATE_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('120', 'portfolio', 'DELETE_CLIENTIMAGE', 'CLIENTIMAGE', 'DELETE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('121', 'portfolio', 'DELETE_CLIENTIMAGE_CHECKER', 'CLIENTIMAGE', 'DELETE_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('122', 'portfolio', 'READ_CLIENTNOTE', 'CLIENTNOTE', 'READ', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('123', 'portfolio', 'CREATE_CLIENTNOTE', 'CLIENTNOTE', 'CREATE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('124', 'portfolio', 'CREATE_CLIENTNOTE_CHECKER', 'CLIENTNOTE', 'CREATE_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('125', 'portfolio', 'UPDATE_CLIENTNOTE', 'CLIENTNOTE', 'UPDATE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('126', 'portfolio', 'UPDATE_CLIENTNOTE_CHECKER', 'CLIENTNOTE', 'UPDATE_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('127', 'portfolio', 'DELETE_CLIENTNOTE', 'CLIENTNOTE', 'DELETE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('128', 'portfolio', 'DELETE_CLIENTNOTE_CHECKER', 'CLIENTNOTE', 'DELETE_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('129', 'portfolio_group', 'READ_GROUPNOTE', 'GROUPNOTE', 'READ', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('130', 'portfolio_group', 'CREATE_GROUPNOTE', 'GROUPNOTE', 'CREATE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('131', 'portfolio_group', 'UPDATE_GROUPNOTE', 'GROUPNOTE', 'UPDATE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('132', 'portfolio_group', 'DELETE_GROUPNOTE', 'GROUPNOTE', 'DELETE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('133', 'portfolio_group', 'CREATE_GROUPNOTE_CHECKER', 'GROUPNOTE', 'CREATE_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('134', 'portfolio_group', 'UPDATE_GROUPNOTE_CHECKER', 'GROUPNOTE', 'UPDATE_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('135', 'portfolio_group', 'DELETE_GROUPNOTE_CHECKER', 'GROUPNOTE', 'DELETE_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('136', 'portfolio', 'READ_LOANNOTE', 'LOANNOTE', 'READ', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('137', 'portfolio', 'CREATE_LOANNOTE', 'LOANNOTE', 'CREATE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('138', 'portfolio', 'UPDATE_LOANNOTE', 'LOANNOTE', 'UPDATE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('139', 'portfolio', 'DELETE_LOANNOTE', 'LOANNOTE', 'DELETE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('140', 'portfolio', 'CREATE_LOANNOTE_CHECKER', 'LOANNOTE', 'CREATE_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('141', 'portfolio', 'UPDATE_LOANNOTE_CHECKER', 'LOANNOTE', 'UPDATE_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('142', 'portfolio', 'DELETE_LOANNOTE_CHECKER', 'LOANNOTE', 'DELETE_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('143', 'portfolio', 'READ_LOANTRANSACTIONNOTE', 'LOANTRANSACTIONNOTE', 'READ', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('144', 'portfolio', 'CREATE_LOANTRANSACTIONNOTE', 'LOANTRANSACTIONNOTE', 'CREATE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('145', 'portfolio', 'UPDATE_LOANTRANSACTIONNOTE', 'LOANTRANSACTIONNOTE', 'UPDATE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('146', 'portfolio', 'DELETE_LOANTRANSACTIONNOTE', 'LOANTRANSACTIONNOTE', 'DELETE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('147', 'portfolio', 'CREATE_LOANTRANSACTIONNOTE_CHECKER', 'LOANTRANSACTIONNOTE', 'CREATE_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('148', 'portfolio', 'UPDATE_LOANTRANSACTIONNOTE_CHECKER', 'LOANTRANSACTIONNOTE', 'UPDATE_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('149', 'portfolio', 'DELETE_LOANTRANSACTIONNOTE_CHECKER', 'LOANTRANSACTIONNOTE', 'DELETE_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('150', 'portfolio', 'READ_SAVINGNOTE', 'SAVINGNOTE', 'READ', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('151', 'portfolio', 'CREATE_SAVINGNOTE', 'SAVINGNOTE', 'CREATE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('152', 'portfolio', 'UPDATE_SAVINGNOTE', 'SAVINGNOTE', 'UPDATE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('153', 'portfolio', 'DELETE_SAVINGNOTE', 'SAVINGNOTE', 'DELETE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('154', 'portfolio', 'CREATE_SAVINGNOTE_CHECKER', 'SAVINGNOTE', 'CREATE_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('155', 'portfolio', 'UPDATE_SAVINGNOTE_CHECKER', 'SAVINGNOTE', 'UPDATE_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('156', 'portfolio', 'DELETE_SAVINGNOTE_CHECKER', 'SAVINGNOTE', 'DELETE_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('157', 'portfolio', 'READ_CLIENTIDENTIFIER', 'CLIENTIDENTIFIER', 'READ', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('158', 'portfolio', 'CREATE_CLIENTIDENTIFIER', 'CLIENTIDENTIFIER', 'CREATE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('159', 'portfolio', 'CREATE_CLIENTIDENTIFIER_CHECKER', 'CLIENTIDENTIFIER', 'CREATE_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('160', 'portfolio', 'UPDATE_CLIENTIDENTIFIER', 'CLIENTIDENTIFIER', 'UPDATE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('161', 'portfolio', 'UPDATE_CLIENTIDENTIFIER_CHECKER', 'CLIENTIDENTIFIER', 'UPDATE_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('162', 'portfolio', 'DELETE_CLIENTIDENTIFIER', 'CLIENTIDENTIFIER', 'DELETE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('163', 'portfolio', 'DELETE_CLIENTIDENTIFIER_CHECKER', 'CLIENTIDENTIFIER', 'DELETE_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('164', 'portfolio', 'READ_DOCUMENT', 'DOCUMENT', 'READ', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('165', 'portfolio', 'CREATE_DOCUMENT', 'DOCUMENT', 'CREATE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('166', 'portfolio', 'CREATE_DOCUMENT_CHECKER', 'DOCUMENT', 'CREATE_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('167', 'portfolio', 'UPDATE_DOCUMENT', 'DOCUMENT', 'UPDATE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('168', 'portfolio', 'UPDATE_DOCUMENT_CHECKER', 'DOCUMENT', 'UPDATE_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('169', 'portfolio', 'DELETE_DOCUMENT', 'DOCUMENT', 'DELETE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('170', 'portfolio', 'DELETE_DOCUMENT_CHECKER', 'DOCUMENT', 'DELETE_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('171', 'portfolio_group', 'READ_GROUP', 'GROUP', 'READ', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('172', 'portfolio_group', 'CREATE_GROUP', 'GROUP', 'CREATE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('173', 'portfolio_group', 'CREATE_GROUP_CHECKER', 'GROUP', 'CREATE_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('174', 'portfolio_group', 'UPDATE_GROUP', 'GROUP', 'UPDATE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('175', 'portfolio_group', 'UPDATE_GROUP_CHECKER', 'GROUP', 'UPDATE_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('176', 'portfolio_group', 'DELETE_GROUP', 'GROUP', 'DELETE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('177', 'portfolio_group', 'DELETE_GROUP_CHECKER', 'GROUP', 'DELETE_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('178', 'portfolio_group', 'UNASSIGNSTAFF_GROUP', 'GROUP', 'UNASSIGNSTAFF', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('179', 'portfolio_group', 'UNASSIGNSTAFF_GROUP_CHECKER', 'GROUP', 'UNASSIGNSTAFF_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('180', 'portfolio', 'CREATE_LOANCHARGE', 'LOANCHARGE', 'CREATE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('181', 'portfolio', 'CREATE_LOANCHARGE_CHECKER', 'LOANCHARGE', 'CREATE_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('182', 'portfolio', 'UPDATE_LOANCHARGE', 'LOANCHARGE', 'UPDATE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('183', 'portfolio', 'UPDATE_LOANCHARGE_CHECKER', 'LOANCHARGE', 'UPDATE_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('184', 'portfolio', 'DELETE_LOANCHARGE', 'LOANCHARGE', 'DELETE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('185', 'portfolio', 'DELETE_LOANCHARGE_CHECKER', 'LOANCHARGE', 'DELETE_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('186', 'portfolio', 'WAIVE_LOANCHARGE', 'LOANCHARGE', 'WAIVE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('187', 'portfolio', 'WAIVE_LOANCHARGE_CHECKER', 'LOANCHARGE', 'WAIVE_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('188', 'portfolio', 'READ_SAVINGSACCOUNT', 'SAVINGSACCOUNT', 'READ', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('189', 'portfolio', 'CREATE_SAVINGSACCOUNT', 'SAVINGSACCOUNT', 'CREATE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('190', 'portfolio', 'CREATE_SAVINGSACCOUNT_CHECKER', 'SAVINGSACCOUNT', 'CREATE_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('191', 'portfolio', 'UPDATE_SAVINGSACCOUNT', 'SAVINGSACCOUNT', 'UPDATE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('192', 'portfolio', 'UPDATE_SAVINGSACCOUNT_CHECKER', 'SAVINGSACCOUNT', 'UPDATE_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('193', 'portfolio', 'DELETE_SAVINGSACCOUNT', 'SAVINGSACCOUNT', 'DELETE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('194', 'portfolio', 'DELETE_SAVINGSACCOUNT_CHECKER', 'SAVINGSACCOUNT', 'DELETE_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('195', 'portfolio', 'READ_GUARANTOR', 'GUARANTOR', 'READ', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('196', 'portfolio', 'CREATE_GUARANTOR', 'GUARANTOR', 'CREATE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('197', 'portfolio', 'CREATE_GUARANTOR_CHECKER', 'GUARANTOR', 'CREATE_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('198', 'portfolio', 'UPDATE_GUARANTOR', 'GUARANTOR', 'UPDATE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('199', 'portfolio', 'UPDATE_GUARANTOR_CHECKER', 'GUARANTOR', 'UPDATE_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('200', 'portfolio', 'DELETE_GUARANTOR', 'GUARANTOR', 'DELETE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('201', 'portfolio', 'DELETE_GUARANTOR_CHECKER', 'GUARANTOR', 'DELETE_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('202', 'portfolio', 'READ_COLLATERAL', 'COLLATERAL', 'READ', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('203', 'portfolio', 'CREATE_COLLATERAL', 'COLLATERAL', 'CREATE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('204', 'portfolio', 'UPDATE_COLLATERAL', 'COLLATERAL', 'UPDATE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('205', 'portfolio', 'DELETE_COLLATERAL', 'COLLATERAL', 'DELETE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('206', 'portfolio', 'CREATE_COLLATERAL_CHECKER', 'COLLATERAL', 'CREATE_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('207', 'portfolio', 'UPDATE_COLLATERAL_CHECKER', 'COLLATERAL', 'UPDATE_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('208', 'portfolio', 'DELETE_COLLATERAL_CHECKER', 'COLLATERAL', 'DELETE_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('209', 'transaction_loan', 'APPROVE_LOAN', 'LOAN', 'APPROVE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('211', 'transaction_loan', 'REJECT_LOAN', 'LOAN', 'REJECT', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('213', 'transaction_loan', 'WITHDRAW_LOAN', 'LOAN', 'WITHDRAW', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('215', 'transaction_loan', 'APPROVALUNDO_LOAN', 'LOAN', 'APPROVALUNDO', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('216', 'transaction_loan', 'DISBURSE_LOAN', 'LOAN', 'DISBURSE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('218', 'transaction_loan', 'DISBURSALUNDO_LOAN', 'LOAN', 'DISBURSALUNDO', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('219', 'transaction_loan', 'REPAYMENT_LOAN', 'LOAN', 'REPAYMENT', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('221', 'transaction_loan', 'ADJUST_LOAN', 'LOAN', 'ADJUST', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('222', 'transaction_loan', 'WAIVEINTERESTPORTION_LOAN', 'LOAN', 'WAIVEINTERESTPORTION', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('223', 'transaction_loan', 'WRITEOFF_LOAN', 'LOAN', 'WRITEOFF', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('224', 'transaction_loan', 'CLOSE_LOAN', 'LOAN', 'CLOSE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('225', 'transaction_loan', 'CLOSEASRESCHEDULED_LOAN', 'LOAN', 'CLOSEASRESCHEDULED', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('226', 'transaction_loan', 'UPDATELOANOFFICER_LOAN', 'LOAN', 'UPDATELOANOFFICER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('227', 'transaction_loan', 'UPDATELOANOFFICER_LOAN_CHECKER', 'LOAN', 'UPDATELOANOFFICER_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('228', 'transaction_loan', 'REMOVELOANOFFICER_LOAN', 'LOAN', 'REMOVELOANOFFICER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('229', 'transaction_loan', 'REMOVELOANOFFICER_LOAN_CHECKER', 'LOAN', 'REMOVELOANOFFICER_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('230', 'transaction_loan', 'BULKREASSIGN_LOAN', 'LOAN', 'BULKREASSIGN', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('231', 'transaction_loan', 'BULKREASSIGN_LOAN_CHECKER', 'LOAN', 'BULKREASSIGN_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('232', 'transaction_loan', 'APPROVE_LOAN_CHECKER', 'LOAN', 'APPROVE_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('234', 'transaction_loan', 'REJECT_LOAN_CHECKER', 'LOAN', 'REJECT_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('236', 'transaction_loan', 'WITHDRAW_LOAN_CHECKER', 'LOAN', 'WITHDRAW_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('238', 'transaction_loan', 'APPROVALUNDO_LOAN_CHECKER', 'LOAN', 'APPROVALUNDO_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('239', 'transaction_loan', 'DISBURSE_LOAN_CHECKER', 'LOAN', 'DISBURSE_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('241', 'transaction_loan', 'DISBURSALUNDO_LOAN_CHECKER', 'LOAN', 'DISBURSALUNDO_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('242', 'transaction_loan', 'REPAYMENT_LOAN_CHECKER', 'LOAN', 'REPAYMENT_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('244', 'transaction_loan', 'ADJUST_LOAN_CHECKER', 'LOAN', 'ADJUST_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('245', 'transaction_loan', 'WAIVEINTERESTPORTION_LOAN_CHECKER', 'LOAN', 'WAIVEINTERESTPORTION_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('246', 'transaction_loan', 'WRITEOFF_LOAN_CHECKER', 'LOAN', 'WRITEOFF_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('247', 'transaction_loan', 'CLOSE_LOAN_CHECKER', 'LOAN', 'CLOSE_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('248', 'transaction_loan', 'CLOSEASRESCHEDULED_LOAN_CHECKER', 'LOAN', 'CLOSEASRESCHEDULED_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('249', 'transaction_loan', 'UNDO_WAIVECHARGE', 'WAIVECHARGE', 'UNDO', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('250', 'transaction_savings', 'DEPOSIT_SAVINGSACCOUNT', 'SAVINGSACCOUNT', 'DEPOSIT', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('251', 'transaction_savings', 'DEPOSIT_SAVINGSACCOUNT_CHECKER', 'SAVINGSACCOUNT', 'DEPOSIT_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('252', 'transaction_savings', 'WITHDRAWAL_SAVINGSACCOUNT', 'SAVINGSACCOUNT', 'WITHDRAWAL', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('253', 'transaction_savings', 'WITHDRAWAL_SAVINGSACCOUNT_CHECKER', 'SAVINGSACCOUNT', 'WITHDRAWAL_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('254', 'transaction_savings', 'ACTIVATE_SAVINGSACCOUNT', 'SAVINGSACCOUNT', 'ACTIVATE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('255', 'transaction_savings', 'ACTIVATE_SAVINGSACCOUNT_CHECKER', 'SAVINGSACCOUNT', 'ACTIVATE_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('256', 'transaction_savings', 'CALCULATEINTEREST_SAVINGSACCOUNT', 'SAVINGSACCOUNT', 'CALCULATEINTEREST', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('257', 'transaction_savings', 'CALCULATEINTEREST_SAVINGSACCOUNT_CHECKER', 'SAVINGSACCOUNT', 'CALCULATEINTEREST_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('258', 'accounting', 'CREATE_GLACCOUNT', 'GLACCOUNT', 'CREATE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('259', 'accounting', 'UPDATE_GLACCOUNT', 'GLACCOUNT', 'UPDATE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('260', 'accounting', 'DELETE_GLACCOUNT', 'GLACCOUNT', 'DELETE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('261', 'accounting', 'CREATE_GLCLOSURE', 'GLCLOSURE', 'CREATE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('262', 'accounting', 'UPDATE_GLCLOSURE', 'GLCLOSURE', 'UPDATE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('263', 'accounting', 'DELETE_GLCLOSURE', 'GLCLOSURE', 'DELETE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('264', 'accounting', 'CREATE_JOURNALENTRY', 'JOURNALENTRY', 'CREATE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('265', 'accounting', 'REVERSE_JOURNALENTRY', 'JOURNALENTRY', 'REVERSE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('266', 'report', 'READ_Active Loans - Details', 'Active Loans - Details', 'READ', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('267', 'report', 'READ_Active Loans - Summary', 'Active Loans - Summary', 'READ', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('268', 'report', 'READ_Active Loans by Disbursal Period', 'Active Loans by Disbursal Period', 'READ', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('269', 'report', 'READ_Active Loans in last installment', 'Active Loans in last installment', 'READ', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('270', 'report', 'READ_Active Loans in last installment Summary', 'Active Loans in last installment Summary', 'READ', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('271', 'report', 'READ_Active Loans Passed Final Maturity', 'Active Loans Passed Final Maturity', 'READ', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('272', 'report', 'READ_Active Loans Passed Final Maturity Summary', 'Active Loans Passed Final Maturity Summary', 'READ', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('273', 'report', 'READ_Aging Detail', 'Aging Detail', 'READ', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('274', 'report', 'READ_Aging Summary (Arrears in Months)', 'Aging Summary (Arrears in Months)', 'READ', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('275', 'report', 'READ_Aging Summary (Arrears in Weeks)', 'Aging Summary (Arrears in Weeks)', 'READ', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('276', 'report', 'READ_Balance Sheet', 'Balance Sheet', 'READ', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('277', 'report', 'READ_Branch Expected Cash Flow', 'Branch Expected Cash Flow', 'READ', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('278', 'report', 'READ_Client Listing', 'Client Listing', 'READ', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('279', 'report', 'READ_Client Loans Listing', 'Client Loans Listing', 'READ', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('280', 'report', 'READ_Expected Payments By Date - Basic', 'Expected Payments By Date - Basic', 'READ', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('281', 'report', 'READ_Expected Payments By Date - Formatted', 'Expected Payments By Date - Formatted', 'READ', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('282', 'report', 'READ_Funds Disbursed Between Dates Summary', 'Funds Disbursed Between Dates Summary', 'READ', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('283', 'report', 'READ_Funds Disbursed Between Dates Summary by Office', 'Funds Disbursed Between Dates Summary by Office', 'READ', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('284', 'report', 'READ_Income Statement', 'Income Statement', 'READ', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('285', 'report', 'READ_Loan Account Schedule', 'Loan Account Schedule', 'READ', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('286', 'report', 'READ_Loans Awaiting Disbursal', 'Loans Awaiting Disbursal', 'READ', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('287', 'report', 'READ_Loans Awaiting Disbursal Summary', 'Loans Awaiting Disbursal Summary', 'READ', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('288', 'report', 'READ_Loans Awaiting Disbursal Summary by Month', 'Loans Awaiting Disbursal Summary by Month', 'READ', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('289', 'report', 'READ_Loans Pending Approval', 'Loans Pending Approval', 'READ', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('290', 'report', 'READ_Obligation Met Loans Details', 'Obligation Met Loans Details', 'READ', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('291', 'report', 'READ_Obligation Met Loans Summary', 'Obligation Met Loans Summary', 'READ', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('292', 'report', 'READ_Portfolio at Risk', 'Portfolio at Risk', 'READ', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('293', 'report', 'READ_Portfolio at Risk by Branch', 'Portfolio at Risk by Branch', 'READ', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('294', 'report', 'READ_Rescheduled Loans', 'Rescheduled Loans', 'READ', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('295', 'report', 'READ_Trial Balance', 'Trial Balance', 'READ', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('296', 'report', 'READ_Written-Off Loans', 'Written-Off Loans', 'READ', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('297', 'transaction_savings', 'POSTINTEREST_SAVINGSACCOUNT', 'SAVINGSACCOUNT', 'POSTINTEREST', 't');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('298', 'transaction_savings', 'POSTINTEREST_SAVINGSACCOUNT_CHECKER', 'SAVINGSACCOUNT', 'POSTINTEREST_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('299', 'portfolio_center', 'READ_CENTER', 'CENTER', 'READ', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('300', 'portfolio_center', 'CREATE_CENTER', 'CENTER', 'CREATE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('301', 'portfolio_center', 'CREATE_CENTER_CHECKER', 'CENTER', 'CREATE_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('302', 'portfolio_center', 'UPDATE_CENTER', 'CENTER', 'UPDATE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('303', 'portfolio_center', 'UPDATE_CENTER_CHECKER', 'CENTER', 'UPDATE_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('304', 'portfolio_center', 'DELETE_CENTER', 'CENTER', 'DELETE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('305', 'portfolio_center', 'DELETE_CENTER_CHECKER', 'CENTER', 'DELETE_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('306', 'configuration', 'READ_REPORT', 'REPORT', 'READ', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('307', 'configuration', 'CREATE_REPORT', 'REPORT', 'CREATE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('308', 'configuration', 'CREATE_REPORT_CHECKER', 'REPORT', 'CREATE_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('309', 'configuration', 'UPDATE_REPORT', 'REPORT', 'UPDATE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('310', 'configuration', 'UPDATE_REPORT_CHECKER', 'REPORT', 'UPDATE_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('311', 'configuration', 'DELETE_REPORT', 'REPORT', 'DELETE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('312', 'configuration', 'DELETE_REPORT_CHECKER', 'REPORT', 'DELETE_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('313', 'portfolio', 'ACTIVATE_CLIENT', 'CLIENT', 'ACTIVATE', 't');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('314', 'portfolio', 'ACTIVATE_CLIENT_CHECKER', 'CLIENT', 'ACTIVATE_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('315', 'portfolio_center', 'ACTIVATE_CENTER', 'CENTER', 'ACTIVATE', 't');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('316', 'portfolio_center', 'ACTIVATE_CENTER_CHECKER', 'CENTER', 'ACTIVATE_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('317', 'portfolio_group', 'ACTIVATE_GROUP', 'GROUP', 'ACTIVATE', 't');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('318', 'portfolio_group', 'ACTIVATE_GROUP_CHECKER', 'GROUP', 'ACTIVATE_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('319', 'portfolio_group', 'ASSOCIATECLIENTS_GROUP', 'GROUP', 'ASSOCIATECLIENTS', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('320', 'portfolio_group', 'DISASSOCIATECLIENTS_GROUP', 'GROUP', 'DISASSOCIATECLIENTS', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('321', 'portfolio_group', 'SAVECOLLECTIONSHEET_GROUP', 'GROUP', 'SAVECOLLECTIONSHEET', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('322', 'portfolio_center', 'SAVECOLLECTIONSHEET_CENTER', 'CENTER', 'SAVECOLLECTIONSHEET', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('324', 'accounting', 'DELETE_ACCOUNTINGRULE', 'ACCOUNTINGRULE', 'DELETE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('325', 'accounting', 'CREATE_ACCOUNTINGRULE', 'ACCOUNTINGRULE', 'CREATE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('326', 'accounting', 'UPDATE_ACCOUNTINGRULE', 'ACCOUNTINGRULE', 'UPDATE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('327', 'report', 'READ_GroupSummaryCounts', 'GroupSummaryCounts', 'READ', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('328', 'report', 'READ_GroupSummaryAmounts', 'GroupSummaryAmounts', 'READ', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('329', 'configuration', 'CREATE_DATATABLE', 'DATATABLE', 'CREATE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('330', 'configuration', 'CREATE_DATATABLE_CHECKER', 'DATATABLE', 'CREATE_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('331', 'configuration', 'UPDATE_DATATABLE', 'DATATABLE', 'UPDATE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('332', 'configuration', 'UPDATE_DATATABLE_CHECKER', 'DATATABLE', 'UPDATE_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('333', 'configuration', 'DELETE_DATATABLE', 'DATATABLE', 'DELETE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('334', 'configuration', 'DELETE_DATATABLE_CHECKER', 'DATATABLE', 'DELETE_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('335', 'organisation', 'CREATE_HOLIDAY', 'HOLIDAY', 'CREATE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('336', 'portfolio_group', 'ASSIGNROLE_GROUP', 'GROUP', 'ASSIGNROLE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('337', 'portfolio_group', 'UNASSIGNROLE_GROUP', 'GROUP', 'UNASSIGNROLE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('338', 'portfolio_group', 'UPDATEROLE_GROUP', 'GROUP', 'UPDATEROLE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('347', 'report', 'READ_TxnRunningBalances', 'TxnRunningBalances', 'READ', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('348', 'portfolio', 'UNASSIGNSTAFF_CLIENT', 'CLIENT', 'UNASSIGNSTAFF', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('349', 'portfolio', 'ASSIGNSTAFF_CLIENT', 'CLIENT', 'ASSIGNSTAFF', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('350', 'portfolio', 'CLOSE_CLIENT', 'CLIENT', 'CLOSE', 't');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('351', 'report', 'READ_FieldAgentStats', 'FieldAgentStats', 'READ', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('352', 'report', 'READ_FieldAgentPrograms', 'FieldAgentPrograms', 'READ', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('353', 'report', 'READ_ProgramDetails', 'ProgramDetails', 'READ', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('354', 'report', 'READ_ChildrenStaffList', 'ChildrenStaffList', 'READ', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('355', 'report', 'READ_CoordinatorStats', 'CoordinatorStats', 'READ', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('356', 'report', 'READ_BranchManagerStats', 'BranchManagerStats', 'READ', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('357', 'report', 'READ_ProgramDirectorStats', 'ProgramDirectorStats', 'READ', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('358', 'report', 'READ_ProgramStats', 'ProgramStats', 'READ', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('359', 'transaction_savings', 'APPROVE_SAVINGSACCOUNT', 'SAVINGSACCOUNT', 'APPROVE', 't');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('360', 'transaction_savings', 'REJECT_SAVINGSACCOUNT', 'SAVINGSACCOUNT', 'REJECT', 't');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('361', 'transaction_savings', 'WITHDRAW_SAVINGSACCOUNT', 'SAVINGSACCOUNT', 'WITHDRAW', 't');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('362', 'transaction_savings', 'APPROVALUNDO_SAVINGSACCOUNT', 'SAVINGSACCOUNT', 'APPROVALUNDO', 't');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('363', 'transaction_savings', 'CLOSE_SAVINGSACCOUNT', 'SAVINGSACCOUNT', 'CLOSE', 't');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('364', 'transaction_savings', 'APPROVE_SAVINGSACCOUNT_CHECKER', 'SAVINGSACCOUNT', 'APPROVE_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('365', 'transaction_savings', 'REJECT_SAVINGSACCOUNT_CHECKER', 'SAVINGSACCOUNT', 'REJECT_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('366', 'transaction_savings', 'WITHDRAW_SAVINGSACCOUNT_CHECKER', 'SAVINGSACCOUNT', 'WITHDRAW_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('367', 'transaction_savings', 'APPROVALUNDO_SAVINGSACCOUNT_CHECKER', 'SAVINGSACCOUNT', 'APPROVALUNDO_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('368', 'transaction_savings', 'CLOSE_SAVINGSACCOUNT_CHECKER', 'SAVINGSACCOUNT', 'CLOSE_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('369', 'transaction_savings', 'UNDOTRANSACTION_SAVINGSACCOUNT', 'SAVINGSACCOUNT', 'UNDOTRANSACTION', 't');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('370', 'transaction_savings', 'UNDOTRANSACTION_SAVINGSACCOUNT_CHECKER', 'SAVINGSACCOUNT', 'UNDOTRANSACTION_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('371', 'portfolio', 'CREATE_PRODUCTMIX', 'PRODUCTMIX', 'CREATE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('372', 'portfolio', 'UPDATE_PRODUCTMIX', 'PRODUCTMIX', 'UPDATE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('373', 'portfolio', 'DELETE_PRODUCTMIX', 'PRODUCTMIX', 'DELETE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('374', 'jobs', 'UPDATE_SCHEDULER', 'SCHEDULER', 'UPDATE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('375', 'transaction_savings', 'APPLYANNUALFEE_SAVINGSACCOUNT', 'SAVINGSACCOUNT', 'APPLYANNUALFEE', 't');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('376', 'transaction_savings', 'APPLYANNUALFEE_SAVINGSACCOUNT_CHECKER', 'SAVINGSACCOUNT', 'APPLYANNUALFEE_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('377', 'portfolio_group', 'ASSIGNSTAFF_GROUP', 'GROUP', 'ASSIGNSTAFF', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('378', 'transaction_savings', 'READ_ACCOUNTTRANSFER', 'ACCOUNTTRANSFER', 'READ', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('379', 'transaction_savings', 'CREATE_ACCOUNTTRANSFER', 'ACCOUNTTRANSFER', 'CREATE', 't');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('380', 'transaction_savings', 'CREATE_ACCOUNTTRANSFER_CHECKER', 'ACCOUNTTRANSFER', 'CREATE_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('381', 'transaction_savings', 'ADJUSTTRANSACTION_SAVINGSACCOUNT', 'SAVINGSACCOUNT', 'ADJUSTTRANSACTION', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('382', 'portfolio', 'CREATE_MEETING', 'MEETING', 'CREATE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('383', 'portfolio', 'UPDATE_MEETING', 'MEETING', 'UPDATE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('384', 'portfolio', 'DELETE_MEETING', 'MEETING', 'DELETE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('385', 'portfolio', 'SAVEORUPDATEATTENDANCE_MEETING', 'MEETING', 'SAVEORUPDATEATTENDANCE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('386', 'portfolio_group', 'TRANSFERCLIENTS_GROUP', 'GROUP', 'TRANSFERCLIENTS', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('387', 'portfolio_group', 'TRANSFERCLIENTS_GROUP_CHECKER', 'GROUP', 'TRANSFERCLIENTS_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('390', 'portfolio', 'PROPOSETRANSFER_CLIENT', 'CLIENT', 'PROPOSETRANSFER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('391', 'portfolio', 'PROPOSETRANSFER_CLIENT_CHECKER', 'CLIENT', 'PROPOSETRANSFER_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('392', 'portfolio', 'ACCEPTTRANSFER_CLIENT', 'CLIENT', 'ACCEPTTRANSFER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('393', 'portfolio', 'ACCEPTTRANSFER_CLIENT_CHECKER', 'CLIENT', 'ACCEPTTRANSFER_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('394', 'portfolio', 'REJECTTRANSFER_CLIENT', 'CLIENT', 'REJECTTRANSFER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('395', 'portfolio', 'REJECTTRANSFER_CLIENT_CHECKER', 'CLIENT', 'REJECTTRANSFER_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('396', 'portfolio', 'WITHDRAWTRANSFER_CLIENT', 'CLIENT', 'WITHDRAWTRANSFER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('397', 'portfolio', 'WITHDRAWTRANSFER_CLIENT_CHECKER', 'CLIENT', 'WITHDRAWTRANSFER_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('398', 'portfolio', 'CLOSE_GROUP', 'GROUP', 'CLOSE', 't');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('399', 'portfolio', 'CLOSE_CENTER', 'CENTER', 'CLOSE', 't');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('400', 'xbrlmapping', 'UPDATE_XBRLMAPPING', 'XBRLMAPPING', 'UPDATE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('401', 'configuration', 'READ_CACHE', 'CACHE', 'READ', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('402', 'configuration', 'UPDATE_CACHE', 'CACHE', 'UPDATE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('403', 'transaction_loan', 'PAY_LOANCHARGE', 'LOANCHARGE', 'PAY', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('404', 'portfolio', 'CREATE_SAVINGSACCOUNTCHARGE', 'SAVINGSACCOUNTCHARGE', 'CREATE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('405', 'portfolio', 'CREATE_SAVINGSACCOUNTCHARGE_CHECKER', 'SAVINGSACCOUNTCHARGE', 'CREATE_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('406', 'portfolio', 'UPDATE_SAVINGSACCOUNTCHARGE', 'SAVINGSACCOUNTCHARGE', 'UPDATE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('407', 'portfolio', 'UPDATE_SAVINGSACCOUNTCHARGE_CHECKER', 'SAVINGSACCOUNTCHARGE', 'UPDATE_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('408', 'portfolio', 'DELETE_SAVINGSACCOUNTCHARGE', 'SAVINGSACCOUNTCHARGE', 'DELETE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('409', 'portfolio', 'DELETE_SAVINGSACCOUNTCHARGE_CHECKER', 'SAVINGSACCOUNTCHARGE', 'DELETE_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('410', 'portfolio', 'WAIVE_SAVINGSACCOUNTCHARGE', 'SAVINGSACCOUNTCHARGE', 'WAIVE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('411', 'portfolio', 'WAIVE_SAVINGSACCOUNTCHARGE_CHECKER', 'SAVINGSACCOUNTCHARGE', 'WAIVE_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('412', 'portfolio', 'PAY_SAVINGSACCOUNTCHARGE', 'SAVINGSACCOUNTCHARGE', 'PAY', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('413', 'portfolio', 'PAY_SAVINGSACCOUNTCHARGE_CHECKER', 'SAVINGSACCOUNTCHARGE', 'PAY_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('414', 'portfolio', 'PROPOSEANDACCEPTTRANSFER_CLIENT', 'CLIENT', 'PROPOSEANDACCEPTTRANSFER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('415', 'portfolio', 'PROPOSEANDACCEPTTRANSFER_CLIENT_CHECKER', 'CLIENT', 'PROPOSEANDACCEPTTRANSFER_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('416', 'organisation', 'DELETE_TEMPLATE', 'TEMPLATE', 'DELETE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('417', 'organisation', 'CREATE_TEMPLATE', 'TEMPLATE', 'CREATE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('418', 'organisation', 'UPDATE_TEMPLATE', 'TEMPLATE', 'UPDATE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('419', 'organisation', 'READ_TEMPLATE', 'TEMPLATE', 'READ', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('420', 'accounting', 'UPDATERUNNINGBALANCE_JOURNALENTRY', 'JOURNALENTRY', 'UPDATERUNNINGBALANCE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('421', 'organisation', 'READ_SMS', 'SMS', 'READ', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('422', 'organisation', 'CREATE_SMS', 'SMS', 'CREATE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('423', 'organisation', 'CREATE_SMS_CHECKER', 'SMS', 'CREATE_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('424', 'organisation', 'UPDATE_SMS', 'SMS', 'UPDATE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('425', 'organisation', 'UPDATE_SMS_CHECKER', 'SMS', 'UPDATE_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('426', 'organisation', 'DELETE_SMS', 'SMS', 'DELETE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('427', 'organisation', 'DELETE_SMS_CHECKER', 'SMS', 'DELETE_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('428', 'organisation', 'CREATE_HOLIDAY_CHECKER', 'HOLIDAY', 'CREATE_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('429', 'organisation', 'ACTIVATE_HOLIDAY', 'HOLIDAY', 'ACTIVATE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('430', 'organisation', 'ACTIVATE_HOLIDAY_CHECKER', 'HOLIDAY', 'ACTIVATE_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('431', 'organisation', 'UPDATE_HOLIDAY', 'HOLIDAY', 'UPDATE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('432', 'organisation', 'UPDATE_HOLIDAY_CHECKER', 'HOLIDAY', 'UPDATE_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('433', 'organisation', 'DELETE_HOLIDAY', 'HOLIDAY', 'DELETE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('434', 'organisation', 'DELETE_HOLIDAY_CHECKER', 'HOLIDAY', 'DELETE_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('435', 'transaction_loan', 'UNDOWRITEOFF_LOAN', 'LOAN', 'UNDOWRITEOFF', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('436', 'portfolio', 'READ_SAVINGSACCOUNTCHARGE', 'SAVINGSACCOUNTCHARGE', 'READ', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('437', 'accounting', 'CREATE_JOURNALENTRY_CHECKER', 'JOURNALENTRY', 'CREATE_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('438', 'portfolio', 'UPDATE_DISBURSEMENTDETAIL', 'DISBURSEMENTDETAIL', 'UPDATE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('439', 'portfolio', 'UPDATESAVINGSACCOUNT_CLIENT', 'CLIENT', 'UPDATESAVINGSACCOUNT', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('440', 'accounting', 'READ_ACCOUNTINGRULE', 'ACCOUNTINGRULE', 'READ', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('441', 'accounting', 'READ_JOURNALENTRY', 'JOURNALENTRY', 'READ', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('442', 'accounting', 'READ_GLACCOUNT', 'GLACCOUNT', 'READ', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('443', 'accounting', 'READ_GLCLOSURE', 'GLCLOSURE', 'READ', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('444', 'organisation', 'READ_HOLIDAY', 'HOLIDAY', 'READ', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('445', 'jobs', 'READ_SCHEDULER', 'SCHEDULER', 'READ', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('446', 'portfolio', 'READ_PRODUCTMIX', 'PRODUCTMIX', 'READ', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('447', 'portfolio', 'READ_MEETING', 'MEETING', 'READ', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('448', 'jobs', 'EXECUTEJOB_SCHEDULER', 'SCHEDULER', 'EXECUTEJOB', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('449', 'account_transfer', 'READ_STANDINGINSTRUCTION ', 'STANDINGINSTRUCTION ', 'READ', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('450', 'account_transfer', 'CREATE_STANDINGINSTRUCTION ', 'STANDINGINSTRUCTION ', 'CREATE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('451', 'account_transfer', 'UPDATE_STANDINGINSTRUCTION ', 'STANDINGINSTRUCTION ', 'UPDATE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('452', 'account_transfer', 'DELETE_STANDINGINSTRUCTION ', 'STANDINGINSTRUCTION ', 'DELETE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('453', 'portfolio', 'CREATE_INTERESTRATECHART', 'INTERESTRATECHART', 'CREATE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('454', 'portfolio', 'CREATE_INTERESTRATECHART_CHECKER', 'INTERESTRATECHART', 'CREATE_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('455', 'portfolio', 'UPDATE_INTERESTRATECHART', 'INTERESTRATECHART', 'UPDATE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('456', 'portfolio', 'DELETE_INTERESTRATECHART', 'INTERESTRATECHART', 'DELETE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('457', 'portfolio', 'UPDATE_INTERESTRATECHART_CHECKER', 'INTERESTRATECHART', 'UPDATE_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('458', 'portfolio', 'DELETE_INTERESTRATECHART_CHECKER', 'INTERESTRATECHART', 'DELETE_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('459', 'portfolio', 'CREATE_CHARTSLAB', 'CHARTSLAB', 'CREATE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('460', 'portfolio', 'CREATE_CHARTSLAB_CHECKER', 'CHARTSLAB', 'CREATE_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('461', 'portfolio', 'UPDATE_CHARTSLAB', 'CHARTSLAB', 'UPDATE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('462', 'portfolio', 'DELETE_CHARTSLAB', 'CHARTSLAB', 'DELETE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('463', 'portfolio', 'UPDATE_CHARTSLAB_CHECKER', 'CHARTSLAB', 'UPDATE_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('464', 'portfolio', 'DELETE_CHARTSLAB_CHECKER', 'CHARTSLAB', 'DELETE_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('465', 'portfolio', 'CREATE_FIXEDDEPOSITPRODUCT', 'FIXEDDEPOSITPRODUCT', 'CREATE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('466', 'portfolio', 'CREATE_FIXEDDEPOSITPRODUCT_CHECKER', 'FIXEDDEPOSITPRODUCT', 'CREATE_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('467', 'portfolio', 'UPDATE_FIXEDDEPOSITPRODUCT', 'FIXEDDEPOSITPRODUCT', 'UPDATE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('468', 'portfolio', 'DELETE_FIXEDDEPOSITPRODUCT', 'FIXEDDEPOSITPRODUCT', 'DELETE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('469', 'portfolio', 'UPDATE_FIXEDDEPOSITPRODUCT_CHECKER', 'FIXEDDEPOSITPRODUCT', 'UPDATE_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('470', 'portfolio', 'DELETE_FIXEDDEPOSITPRODUCT_CHECKER', 'FIXEDDEPOSITPRODUCT', 'DELETE_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('471', 'portfolio', 'CREATE_RECURRINGDEPOSITPRODUCT', 'RECURRINGDEPOSITPRODUCT', 'CREATE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('472', 'portfolio', 'CREATE_RECURRINGDEPOSITPRODUCT_CHECKER', 'RECURRINGDEPOSITPRODUCT', 'CREATE_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('473', 'portfolio', 'UPDATE_RECURRINGDEPOSITPRODUCT', 'RECURRINGDEPOSITPRODUCT', 'UPDATE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('474', 'portfolio', 'DELETE_RECURRINGDEPOSITPRODUCT', 'RECURRINGDEPOSITPRODUCT', 'DELETE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('475', 'portfolio', 'UPDATE_RECURRINGDEPOSITPRODUCT_CHECKER', 'RECURRINGDEPOSITPRODUCT', 'UPDATE_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('476', 'portfolio', 'DELETE_RECURRINGDEPOSITPRODUCT_CHECKER', 'RECURRINGDEPOSITPRODUCT', 'DELETE_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('477', 'portfolio', 'READ_FIXEDDEPOSITACCOUNT', 'FIXEDDEPOSITACCOUNT', 'READ', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('478', 'portfolio', 'CREATE_FIXEDDEPOSITACCOUNT', 'FIXEDDEPOSITACCOUNT', 'CREATE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('479', 'portfolio', 'CREATE_FIXEDDEPOSITACCOUNT_CHECKER', 'FIXEDDEPOSITACCOUNT', 'CREATE_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('480', 'portfolio', 'UPDATE_FIXEDDEPOSITACCOUNT', 'FIXEDDEPOSITACCOUNT', 'UPDATE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('481', 'portfolio', 'UPDATE_FIXEDDEPOSITACCOUNT_CHECKER', 'FIXEDDEPOSITACCOUNT', 'UPDATE_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('482', 'portfolio', 'DELETE_FIXEDDEPOSITACCOUNT', 'FIXEDDEPOSITACCOUNT', 'DELETE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('483', 'portfolio', 'DELETE_FIXEDDEPOSITACCOUNT_CHECKER', 'FIXEDDEPOSITACCOUNT', 'DELETE_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('484', 'transaction_savings', 'DEPOSIT_FIXEDDEPOSITACCOUNT', 'FIXEDDEPOSITACCOUNT', 'DEPOSIT', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('485', 'transaction_savings', 'DEPOSIT_FIXEDDEPOSITACCOUNT_CHECKER', 'FIXEDDEPOSITACCOUNT', 'DEPOSIT_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('486', 'transaction_savings', 'WITHDRAWAL_FIXEDDEPOSITACCOUNT', 'FIXEDDEPOSITACCOUNT', 'WITHDRAWAL', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('487', 'transaction_savings', 'WITHDRAWAL_FIXEDDEPOSITACCOUNT_CHECKER', 'FIXEDDEPOSITACCOUNT', 'WITHDRAWAL_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('488', 'transaction_savings', 'ACTIVATE_FIXEDDEPOSITACCOUNT', 'FIXEDDEPOSITACCOUNT', 'ACTIVATE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('489', 'transaction_savings', 'ACTIVATE_FIXEDDEPOSITACCOUNT_CHECKER', 'FIXEDDEPOSITACCOUNT', 'ACTIVATE_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('490', 'transaction_savings', 'CALCULATEINTEREST_FIXEDDEPOSITACCOUNT', 'FIXEDDEPOSITACCOUNT', 'CALCULATEINTEREST', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('491', 'transaction_savings', 'CALCULATEINTEREST_FIXEDDEPOSITACCOUNT_CHECKER', 'FIXEDDEPOSITACCOUNT', 'CALCULATEINTEREST_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('492', 'transaction_savings', 'POSTINTEREST_FIXEDDEPOSITACCOUNT', 'FIXEDDEPOSITACCOUNT', 'POSTINTEREST', 't');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('493', 'transaction_savings', 'POSTINTEREST_FIXEDDEPOSITACCOUNT_CHECKER', 'FIXEDDEPOSITACCOUNT', 'POSTINTEREST_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('494', 'transaction_savings', 'APPROVE_FIXEDDEPOSITACCOUNT', 'FIXEDDEPOSITACCOUNT', 'APPROVE', 't');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('495', 'transaction_savings', 'REJECT_FIXEDDEPOSITACCOUNT', 'FIXEDDEPOSITACCOUNT', 'REJECT', 't');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('496', 'transaction_savings', 'WITHDRAW_FIXEDDEPOSITACCOUNT', 'FIXEDDEPOSITACCOUNT', 'WITHDRAW', 't');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('497', 'transaction_savings', 'APPROVALUNDO_FIXEDDEPOSITACCOUNT', 'FIXEDDEPOSITACCOUNT', 'APPROVALUNDO', 't');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('498', 'transaction_savings', 'CLOSE_FIXEDDEPOSITACCOUNT', 'FIXEDDEPOSITACCOUNT', 'CLOSE', 't');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('499', 'transaction_savings', 'APPROVE_FIXEDDEPOSITACCOUNT_CHECKER', 'FIXEDDEPOSITACCOUNT', 'APPROVE_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('500', 'transaction_savings', 'REJECT_FIXEDDEPOSITACCOUNT_CHECKER', 'FIXEDDEPOSITACCOUNT', 'REJECT_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('501', 'transaction_savings', 'WITHDRAW_FIXEDDEPOSITACCOUNT_CHECKER', 'FIXEDDEPOSITACCOUNT', 'WITHDRAW_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('502', 'transaction_savings', 'APPROVALUNDO_FIXEDDEPOSITACCOUNT_CHECKER', 'FIXEDDEPOSITACCOUNT', 'APPROVALUNDO_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('503', 'transaction_savings', 'CLOSE_FIXEDDEPOSITACCOUNT_CHECKER', 'FIXEDDEPOSITACCOUNT', 'CLOSE_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('504', 'transaction_savings', 'UNDOTRANSACTION_FIXEDDEPOSITACCOUNT', 'FIXEDDEPOSITACCOUNT', 'UNDOTRANSACTION', 't');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('505', 'transaction_savings', 'UNDOTRANSACTION_FIXEDDEPOSITACCOUNT_CHECKER', 'FIXEDDEPOSITACCOUNT', 'UNDOTRANSACTION_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('506', 'transaction_savings', 'ADJUSTTRANSACTION_FIXEDDEPOSITACCOUNT', 'FIXEDDEPOSITACCOUNT', 'ADJUSTTRANSACTION', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('507', 'portfolio', 'READ_RECURRINGDEPOSITACCOUNT', 'RECURRINGDEPOSITACCOUNT', 'READ', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('508', 'portfolio', 'CREATE_RECURRINGDEPOSITACCOUNT', 'RECURRINGDEPOSITACCOUNT', 'CREATE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('509', 'portfolio', 'CREATE_RECURRINGDEPOSITACCOUNT_CHECKER', 'RECURRINGDEPOSITACCOUNT', 'CREATE_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('510', 'portfolio', 'UPDATE_RECURRINGDEPOSITACCOUNT', 'RECURRINGDEPOSITACCOUNT', 'UPDATE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('511', 'portfolio', 'UPDATE_RECURRINGDEPOSITACCOUNT_CHECKER', 'RECURRINGDEPOSITACCOUNT', 'UPDATE_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('512', 'portfolio', 'DELETE_RECURRINGDEPOSITACCOUNT', 'RECURRINGDEPOSITACCOUNT', 'DELETE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('513', 'portfolio', 'DELETE_RECURRINGDEPOSITACCOUNT_CHECKER', 'RECURRINGDEPOSITACCOUNT', 'DELETE_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('514', 'transaction_savings', 'DEPOSIT_RECURRINGDEPOSITACCOUNT', 'RECURRINGDEPOSITACCOUNT', 'DEPOSIT', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('515', 'transaction_savings', 'DEPOSIT_RECURRINGDEPOSITACCOUNT_CHECKER', 'RECURRINGDEPOSITACCOUNT', 'DEPOSIT_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('516', 'transaction_savings', 'WITHDRAWAL_RECURRINGDEPOSITACCOUNT', 'RECURRINGDEPOSITACCOUNT', 'WITHDRAWAL', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('517', 'transaction_savings', 'WITHDRAWAL_RECURRINGDEPOSITACCOUNT_CHECKER', 'RECURRINGDEPOSITACCOUNT', 'WITHDRAWAL_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('518', 'transaction_savings', 'ACTIVATE_RECURRINGDEPOSITACCOUNT', 'RECURRINGDEPOSITACCOUNT', 'ACTIVATE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('519', 'transaction_savings', 'ACTIVATE_RECURRINGDEPOSITACCOUNT_CHECKER', 'RECURRINGDEPOSITACCOUNT', 'ACTIVATE_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('520', 'transaction_savings', 'CALCULATEINTEREST_RECURRINGDEPOSITACCOUNT', 'RECURRINGDEPOSITACCOUNT', 'CALCULATEINTEREST', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('521', 'transaction_savings', 'CALCULATEINTEREST_RECURRINGDEPOSITACCOUNT_CHECKER', 'RECURRINGDEPOSITACCOUNT', 'CALCULATEINTEREST_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('522', 'transaction_savings', 'POSTINTEREST_RECURRINGDEPOSITACCOUNT', 'RECURRINGDEPOSITACCOUNT', 'POSTINTEREST', 't');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('523', 'transaction_savings', 'POSTINTEREST_RECURRINGDEPOSITACCOUNT_CHECKER', 'RECURRINGDEPOSITACCOUNT', 'POSTINTEREST_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('524', 'transaction_savings', 'APPROVE_RECURRINGDEPOSITACCOUNT', 'RECURRINGDEPOSITACCOUNT', 'APPROVE', 't');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('525', 'transaction_savings', 'REJECT_RECURRINGDEPOSITACCOUNT', 'RECURRINGDEPOSITACCOUNT', 'REJECT', 't');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('526', 'transaction_savings', 'WITHDRAW_RECURRINGDEPOSITACCOUNT', 'RECURRINGDEPOSITACCOUNT', 'WITHDRAW', 't');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('527', 'transaction_savings', 'APPROVALUNDO_RECURRINGDEPOSITACCOUNT', 'RECURRINGDEPOSITACCOUNT', 'APPROVALUNDO', 't');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('528', 'transaction_savings', 'CLOSE_RECURRINGDEPOSITACCOUNT', 'RECURRINGDEPOSITACCOUNT', 'CLOSE', 't');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('529', 'transaction_savings', 'APPROVE_RECURRINGDEPOSITACCOUNT_CHECKER', 'RECURRINGDEPOSITACCOUNT', 'APPROVE_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('530', 'transaction_savings', 'REJECT_RECURRINGDEPOSITACCOUNT_CHECKER', 'RECURRINGDEPOSITACCOUNT', 'REJECT_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('531', 'transaction_savings', 'WITHDRAW_RECURRINGDEPOSITACCOUNT_CHECKER', 'RECURRINGDEPOSITACCOUNT', 'WITHDRAW_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('532', 'transaction_savings', 'APPROVALUNDO_RECURRINGDEPOSITACCOUNT_CHECKER', 'RECURRINGDEPOSITACCOUNT', 'APPROVALUNDO_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('533', 'transaction_savings', 'CLOSE_RECURRINGDEPOSITACCOUNT_CHECKER', 'RECURRINGDEPOSITACCOUNT', 'CLOSE_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('534', 'transaction_savings', 'UNDOTRANSACTION_RECURRINGDEPOSITACCOUNT', 'RECURRINGDEPOSITACCOUNT', 'UNDOTRANSACTION', 't');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('535', 'transaction_savings', 'UNDOTRANSACTION_RECURRINGDEPOSITACCOUNT_CHECKER', 'RECURRINGDEPOSITACCOUNT', 'UNDOTRANSACTION_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('536', 'transaction_savings', 'ADJUSTTRANSACTION_RECURRINGDEPOSITACCOUNT', 'RECURRINGDEPOSITACCOUNT', 'ADJUSTTRANSACTION', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('537', 'transaction_savings', 'PREMATURECLOSE_FIXEDDEPOSITACCOUNT_CHECKER', 'FIXEDDEPOSITACCOUNT', 'PREMATURECLOSE_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('538', 'transaction_savings', 'PREMATURECLOSE_FIXEDDEPOSITACCOUNT', 'FIXEDDEPOSITACCOUNT', 'PREMATURECLOSE', 't');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('539', 'transaction_savings', 'PREMATURECLOSE_RECURRINGDEPOSITACCOUNT_CHECKER', 'RECURRINGDEPOSITACCOUNT', 'PREMATURECLOSE_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('540', 'transaction_savings', 'PREMATURECLOSE_RECURRINGDEPOSITACCOUNT', 'RECURRINGDEPOSITACCOUNT', 'PREMATURECLOSE', 't');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('541', 'transaction_loan', 'DISBURSETOSAVINGS_LOAN', 'LOAN', 'DISBURSETOSAVINGS', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('542', 'transaction_loan', 'RECOVERYPAYMENT_LOAN', 'LOAN', 'RECOVERYPAYMENT', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('543', 'organisation', 'READ_RECURRINGDEPOSITPRODUCT', 'RECURRINGDEPOSITPRODUCT', 'READ', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('544', 'organisation', 'READ_FIXEDDEPOSITPRODUCT', 'FIXEDDEPOSITPRODUCT', 'READ', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('545', 'accounting', 'READ_FINANCIALACTIVITYACCOUNT', 'FINANCIALACTIVITYACCOUNT', 'READ', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('546', 'accounting', 'CREATE_FINANCIALACTIVITYACCOUNT', 'FINANCIALACTIVITYACCOUNT', 'CREATE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('547', 'accounting', 'DELETE_FINANCIALACTIVITYACCOUNT', 'FINANCIALACTIVITYACCOUNT', 'DELETE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('548', 'accounting', 'UPDATE_FINANCIALACTIVITYACCOUNT', 'FINANCIALACTIVITYACCOUNT', 'UPDATE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('549', 'datatable', 'UPDATE_LIKELIHOOD', 'likelihood', 'UPDATE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('550', 'survey', 'REGISTER_SURVEY', 'survey', 'CREATE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('551', 'accounting', 'EXECUTE_PERIODICACCRUALACCOUNTING', 'PERIODICACCRUALACCOUNTING', 'EXECUTE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('552', 'portfolio', 'INACTIVATE_SAVINGSACCOUNTCHARGE', 'SAVINGSACCOUNTCHARGE', 'INACTIVATE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('553', 'portfolio', 'INACTIVATE_SAVINGSACCOUNTCHARGE_CHECKER', 'SAVINGSACCOUNTCHARGE', 'INACTIVATE_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('554', 'portfolio_center', 'DISASSOCIATEGROUPS_CENTER', 'CENTER', 'DISASSOCIATEGROUPS', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('555', 'portfolio_center', 'ASSOCIATEGROUPS_CENTER', 'CENTER', 'ASSOCIATEGROUPS', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('556', 'portfolio_center', 'DISASSOCIATEGROUPS_CENTER_CHECKER', 'CENTER', 'DISASSOCIATEGROUPS_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('557', 'portfolio_center', 'ASSOCIATEGROUPS_CENTER_CHECKER', 'CENTER', 'ASSOCIATEGROUPS_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('558', 'loan_reschedule', 'READ_RESCHEDULELOAN', 'RESCHEDULELOAN', 'READ', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('559', 'loan_reschedule', 'CREATE_RESCHEDULELOAN', 'RESCHEDULELOAN', 'CREATE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('560', 'loan_reschedule', 'REJECT_RESCHEDULELOAN', 'RESCHEDULELOAN', 'REJECT', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('561', 'loan_reschedule', 'APPROVE_RESCHEDULELOAN', 'RESCHEDULELOAN', 'APPROVE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('562', 'configuration', 'CREATE_HOOK', 'HOOK', 'CREATE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('563', 'configuration', 'READ_HOOK', 'HOOK', 'READ', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('564', 'configuration', 'UPDATE_HOOK', 'HOOK', 'UPDATE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('565', 'configuration', 'DELETE_HOOK', 'HOOK', 'DELETE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('566', 'portfolio', 'REMOVESAVINGSOFFICER_SAVINGSACCOUNT', 'SAVINGSACCOUNT', 'REMOVESAVINGSOFFICER', 't');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('567', 'portfolio', 'UPDATESAVINGSOFFICER_SAVINGSACCOUNT', 'SAVINGSACCOUNT', 'UPDATESAVINGSOFFICER', 't');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('578', 'report', 'READ_Client Loan Account Schedule', 'Client Loan Account Schedule', 'READ', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('580', 'report', 'READ_Client Saving Transactions', 'Client Saving Transactions', 'READ', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('581', 'report', 'READ_Client Savings Summary', 'Client Savings Summary', 'READ', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('582', 'report', 'READ_ClientSummary ', 'ClientSummary ', 'READ', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('583', 'report', 'READ_ClientTrendsByDay', 'ClientTrendsByDay', 'READ', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('584', 'report', 'READ_ClientTrendsByMonth', 'ClientTrendsByMonth', 'READ', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('585', 'report', 'READ_ClientTrendsByWeek', 'ClientTrendsByWeek', 'READ', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('586', 'report', 'READ_Demand_Vs_Collection', 'Demand_Vs_Collection', 'READ', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('587', 'report', 'READ_Disbursal_Vs_Awaitingdisbursal', 'Disbursal_Vs_Awaitingdisbursal', 'READ', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('591', 'report', 'READ_GroupNamesByStaff', 'GroupNamesByStaff', 'READ', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('592', 'report', 'READ_GroupSavingSummary', 'GroupSavingSummary', 'READ', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('593', 'report', 'READ_LoanCyclePerProduct', 'LoanCyclePerProduct', 'READ', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('598', 'report', 'READ_LoanTrendsByDay', 'LoanTrendsByDay', 'READ', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('599', 'report', 'READ_LoanTrendsByMonth', 'LoanTrendsByMonth', 'READ', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('600', 'report', 'READ_LoanTrendsByWeek', 'LoanTrendsByWeek', 'READ', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('606', 'report', 'READ_Savings Transactions', 'Savings Transactions', 'READ', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('609', 'configuration', 'CREATE_ACCOUNTNUMBERFORMAT', 'ACCOUNTNUMBERFORMAT', 'CREATE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('610', 'configuration', 'READ_ACCOUNTNUMBERFORMAT', 'ACCOUNTNUMBERFORMAT', 'READ', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('611', 'configuration', 'UPDATE_ACCOUNTNUMBERFORMAT', 'ACCOUNTNUMBERFORMAT', 'UPDATE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('612', 'configuration', 'DELETE_ACCOUNTNUMBERFORMAT', 'HOOK', 'DELETE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('613', 'portfolio', 'RECOVERGUARANTEES_LOAN', 'LOAN', 'RECOVERGUARANTEES', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('614', 'portfolio', 'RECOVERGUARANTEES_LOAN_CHECKER', 'LOAN', 'RECOVERGUARANTEES_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('615', 'portfolio', 'REJECT_CLIENT', 'CLIENT', 'REJECT', 't');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('616', 'portfolio', 'REJECT_CLIENT_CHECKER', 'CLIENT', 'REJECT_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('617', 'portfolio', 'WITHDRAW_CLIENT', 'CLIENT', 'WITHDRAW', 't');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('618', 'portfolio', 'WITHDRAW_CLIENT_CHECKER', 'CLIENT', 'WITHDRAW_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('619', 'portfolio', 'REACTIVATE_CLIENT', 'CLIENT', 'REACTIVATE', 't');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('620', 'portfolio', 'REACTIVATE_CLIENT_CHECKER', 'CLIENT', 'REACTIVATE_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('621', 'transaction_savings', 'UPDATEDEPOSITAMOUNT_RECURRINGDEPOSITACCOUNT', 'RECURRINGDEPOSITACCOUNT', 'UPDATEDEPOSITAMOUNT', 't');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('623', 'transaction_savings', 'REFUNDBYTRANSFER_ACCOUNTTRANSFER_CHECKER', 'ACCOUNTTRANSFER', 'REFUNDBYTRANSFER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('624', 'transaction_savings', 'REFUNDBYTRANSFER_ACCOUNTTRANSFER', 'ACCOUNTTRANSFER', 'REFUNDBYTRANSFER', 't');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('625', 'transaction_loan', 'REFUNDBYCASH_LOAN', 'LOAN', 'REFUNDBYCASH', 't');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('626', 'transaction_loan', 'REFUNDBYCASH_LOAN_CHECKER', 'LOAN', 'REFUNDBYCASH', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('627', 'cash_mgmt', 'CREATE_TELLER', 'TELLER', 'CREATE', 't');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('628', 'cash_mgmt', 'UPDATE_TELLER', 'TELLER', 'UPDATE', 't');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('629', 'cash_mgmt', 'ALLOCATECASHIER_TELLER', 'TELLER', 'ALLOCATE', 't');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('630', 'cash_mgmt', 'UPDATECASHIERALLOCATION_TELLER', 'TELLER', 'UPDATECASHIERALLOCATION', 't');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('631', 'cash_mgmt', 'DELETECASHIERALLOCATION_TELLER', 'TELLER', 'DELETECASHIERALLOCATION', 't');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('632', 'cash_mgmt', 'ALLOCATECASHTOCASHIER_TELLER', 'TELLER', 'ALLOCATECASHTOCASHIER', 't');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('633', 'cash_mgmt', 'SETTLECASHFROMCASHIER_TELLER', 'TELLER', 'SETTLECASHFROMCASHIER', 't');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('634', 'authorisation', 'DISABLE_ROLE', 'ROLE', 'DISABLE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('635', 'authorisation', 'DISABLE_ROLE_CHECKER', 'ROLE', 'DISABLE_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('636', 'authorisation', 'ENABLE_ROLE', 'ROLE', 'ENABLE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('637', 'authorisation', 'ENABLE_ROLE_CHECKER', 'ROLE', 'ENABLE_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('638', 'accounting', 'DEFINEOPENINGBALANCE_JOURNALENTRY', 'JOURNALENTRY', 'DEFINEOPENINGBALANCE', 't');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('639', 'collection_sheet', 'READ_COLLECTIONSHEET', 'COLLECTIONSHEET', 'READ', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('640', 'collection_sheet', 'SAVE_COLLECTIONSHEET', 'COLLECTIONSHEET', 'SAVE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('641', 'infrastructure', 'CREATE_ENTITYMAPPING', 'ENTITYMAPPING', 'CREATE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('642', 'infrastructure', 'UPDATE_ENTITYMAPPING', 'ENTITYMAPPING', 'UPDATE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('643', 'infrastructure', 'DELETE_ENTITYMAPPING', 'ENTITYMAPPING', 'DELETE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('644', 'organisation', 'READ_WORKINGDAYS', 'WORKINGDAYS', 'READ', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('645', 'organisation', 'UPDATE_WORKINGDAYS', 'WORKINGDAYS', 'UPDATE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('646', 'organisation', 'UPDATE_WORKINGDAYS_CHECKER', 'WORKINGDAYS', 'UPDATE_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('647', 'authorisation', 'READ_PASSWORD_PREFERENCES', 'PASSWORD_PREFERENCES', 'READ', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('648', 'authorisation', 'UPDATE_PASSWORD_PREFERENCES', 'PASSWORD_PREFERENCES', 'UPDATE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('649', 'authorisation', 'UPDATE_PASSWORD_PREFERENCES_CHECKER', 'PASSWORD_PREFERENCES', 'UPDATE_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('650', 'portfolio', 'CREATE_PAYMENTTYPE', 'PAYMENTTYPE', 'CREATE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('651', 'portfolio', 'UPDATE_PAYMENTTYPE', 'PAYMENTTYPE', 'UPDATE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('652', 'portfolio', 'DELETE_PAYMENTTYPE', 'PAYMENTTYPE', 'DELETE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('653', 'cash_mgmt', 'DELETE_TELLER', 'TELLER', 'DELETE', 't');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('654', 'report', 'READ_General Ledger Report', 'General Ledger Report', 'READ', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('655', 'portfolio', 'READ_STAFFIMAGE', 'STAFFIMAGE', 'READ', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('656', 'portfolio', 'CREATE_STAFFIMAGE', 'STAFFIMAGE', 'CREATE', 't');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('657', 'portfolio', 'CREATE_STAFFIMAGE_CHECKER', 'STAFFIMAGE', 'CREATE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('658', 'portfolio', 'DELETE_STAFFIMAGE', 'STAFFIMAGE', 'DELETE', 't');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('659', 'portfolio', 'DELETE_STAFFIMAGE_CHECKER', 'STAFFIMAGE', 'DELETE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('660', 'report', 'READ_Active Loan Summary per Branch', 'Active Loan Summary per Branch', 'READ', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('661', 'report', 'READ_Disbursal Report', 'Disbursal Report', 'READ', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('662', 'report', 'READ_Balance Outstanding', 'Balance Outstanding', 'READ', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('663', 'report', 'READ_Collection Report', 'Collection Report', 'READ', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('664', 'portfolio', 'READ_PAYMENTTYPE', 'PAYMENTTYPE', 'READ', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('666', 'externalservices', 'UPDATE_EXTERNALSERVICES', 'EXTERNALSERVICES', 'UPDATE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('667', 'portfolio', 'READ_CLIENTCHARGE', 'CLIENTCHARGE', 'READ', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('668', 'portfolio', 'CREATE_CLIENTCHARGE', 'CLIENTCHARGE', 'CREATE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('669', 'portfolio', 'DELETE_CLIENTCHARGE', 'CLIENTCHARGE', 'DELETE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('670', 'portfolio', 'WAIVE_CLIENTCHARGE', 'CLIENTCHARGE', 'WAIVE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('671', 'portfolio', 'PAY_CLIENTCHARGE', 'CLIENTCHARGE', 'PAY', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('673', 'portfolio', 'UPDATE_CLIENTCHARGE', 'CLIENTCHARGE', 'UPDATE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('674', 'portfolio', 'CREATE_CLIENTCHARGE_CHECKER', 'CLIENTCHARGE', 'CREATE_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('675', 'portfolio', 'DELETE_CLIENTCHARGE_CHECKER', 'CLIENTCHARGE', 'DELETE_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('676', 'portfolio', 'WAIVE_CLIENTCHARGE_CHECKER', 'CLIENTCHARGE', 'WAIVE_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('677', 'portfolio', 'PAY_CLIENTCHARGE_CHECKER', 'CLIENTCHARGE', 'PAY_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('679', 'portfolio', 'UPDATE_CLIENTCHARGE_CHECKER', 'CLIENTCHARGE', 'UPDATE_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('680', 'transaction_client', 'READTRANSACTION_CLIENT', 'CLIENT', 'READTRANSACTION', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('681', 'transaction_client', 'UNDOTRANSACTION_CLIENT', 'CLIENT', 'UNDOTRANSACTION', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('682', 'transaction_client', 'UNDOTRANSACTION_CLIENT_CHECKER', 'CLIENT', 'UNDOTRANSACTION_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('683', 'LOAN_PROVISIONING', 'CREATE_PROVISIONCATEGORY', 'PROVISIONCATEGORY', 'CREATE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('684', 'LOAN_PROVISIONING', 'DELETE_PROVISIONCATEGORY', 'PROVISIONCATEGORY', 'DELETE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('685', 'LOAN_PROVISIONING', 'CREATE_PROVISIONCRITERIA', 'PROVISIONINGCRITERIA', 'CREATE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('686', 'LOAN_PROVISIONING', 'UPDATE_PROVISIONCRITERIA', 'PROVISIONINGCRITERIA', 'UPDATE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('687', 'LOAN_PROVISIONING', 'DELETE_PROVISIONCRITERIA', 'PROVISIONINGCRITERIA', 'DELETE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('688', 'LOAN_PROVISIONING', 'CREATE_PROVISIONENTRIES', 'PROVISIONINGENTRIES', 'CREATE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('771', 'organisation', 'DELETE_EMAIL_CHECKER', 'EMAIL', 'DELETE_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('689', 'LOAN_PROVISIONING', 'CREATE_PROVISIONJOURNALENTRIES', 'PROVISIONINGENTRIES', 'CREATE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('690', 'LOAN_PROVISIONING', 'RECREATE_PROVISIONENTRIES', 'PROVISIONINGENTRIES', 'RECREATE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('691', 'portfolio', 'READ_FLOATINGRATE', 'FLOATINGRATE', 'READ', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('692', 'portfolio', 'CREATE_FLOATINGRATE', 'FLOATINGRATE', 'CREATE', 't');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('693', 'portfolio', 'CREATE_FLOATINGRATE_CHECKER', 'FLOATINGRATE', 'CREATE_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('694', 'portfolio', 'UPDATE_FLOATINGRATE', 'FLOATINGRATE', 'UPDATE', 't');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('695', 'portfolio', 'UPDATE_FLOATINGRATE_CHECKER', 'FLOATINGRATE', 'UPDATE_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('696', 'portfolio', 'CREATESCHEDULEEXCEPTIONS_LOAN', 'LOAN', 'CREATESCHEDULEEXCEPTIONS', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('697', 'portfolio', 'CREATESCHEDULEEXCEPTIONS_LOAN_CHECKER', 'LOAN', 'CREATESCHEDULEEXCEPTIONS_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('698', 'portfolio', 'DELETESCHEDULEEXCEPTIONS_LOAN', 'LOAN', 'DELETESCHEDULEEXCEPTIONS', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('699', 'portfolio', 'DELETESCHEDULEEXCEPTIONS_LOAN_CHECKER', 'LOAN', 'DELETESCHEDULEEXCEPTIONS_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('700', 'transaction_loan', 'DISBURSALLASTUNDO_LOAN', 'LOAN', 'DISBURSALLASTUNDO', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('701', 'transaction_loan', 'DISBURSALLASTUNDO_LOAN_CHECKER', 'LOAN', 'DISBURSALLASTUNDO_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('702', 'SHAREPRODUCT', 'CREATE_SHAREPRODUCT', 'SHAREPRODUCT', 'CREATE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('703', 'SHAREPRODUCT', 'UPDATE_SHAREPRODUCT', 'SHAREPRODUCT', 'CREATE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('704', 'SHAREACCOUNT', 'CREATE_SHAREACCOUNT', 'SHAREACCOUNT', 'CREATE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('705', 'SHAREACCOUNT', 'UPDATE_SHAREACCOUNT', 'SHAREACCOUNT', 'CREATE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('706', 'organisation', 'READ_TAXCOMPONENT', 'TAXCOMPONENT', 'READ', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('707', 'organisation', 'CREATE_TAXCOMPONENT', 'TAXCOMPONENT', 'CREATE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('708', 'organisation', 'CREATE_TAXCOMPONENT_CHECKER', 'TAXCOMPONENT', 'CREATE_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('709', 'organisation', 'UPDATE_TAXCOMPONENT', 'TAXCOMPONENT', 'UPDATE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('710', 'organisation', 'UPDATE_TAXCOMPONENT_CHECKER', 'TAXCOMPONENT', 'UPDATE_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('711', 'organisation', 'READ_TAXGROUP', 'TAXGROUP', 'READ', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('712', 'organisation', 'CREATE_TAXGROUP', 'TAXGROUP', 'CREATE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('713', 'organisation', 'CREATE_TAXGROUP_CHECKER', 'TAXGROUP', 'CREATE_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('714', 'organisation', 'UPDATE_TAXGROUP', 'TAXGROUP', 'UPDATE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('715', 'organisation', 'UPDATE_TAXGROUP_CHECKER', 'TAXGROUP', 'UPDATE_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('716', 'portfolio', 'UPDATEWITHHOLDTAX_SAVINGSACCOUNT', 'SAVINGSACCOUNT', 'UPDATEWITHHOLDTAX', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('717', 'portfolio', 'UPDATEWITHHOLDTAX_SAVINGSACCOUNT_CHECKER', 'SAVINGSACCOUNT', 'UPDATEWITHHOLDTAX_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('718', 'SHAREPRODUCT', 'CREATE_DIVIDEND_SHAREPRODUCT', 'SHAREPRODUCT', 'CREATE_DIVIDEND', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('719', 'SHAREPRODUCT', 'CREATE_DIVIDEND_SHAREPRODUCT_CHECKER', 'SHAREPRODUCT', 'CREATE_DIVIDEND_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('720', 'SHAREPRODUCT', 'APPROVE_DIVIDEND_SHAREPRODUCT', 'SHAREPRODUCT', 'APPROVE_DIVIDEND', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('721', 'SHAREPRODUCT', 'APPROVE_DIVIDEND_SHAREPRODUCT_CHECKER', 'SHAREPRODUCT', 'APPROVE_DIVIDEND_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('722', 'SHAREPRODUCT', 'DELETE_DIVIDEND_SHAREPRODUCT', 'SHAREPRODUCT', 'DELETE_DIVIDEND', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('723', 'SHAREPRODUCT', 'DELETE_DIVIDEND_SHAREPRODUCT_CHECKER', 'SHAREPRODUCT', 'DELETE_DIVIDEND_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('724', 'SHAREPRODUCT', 'READ_DIVIDEND_SHAREPRODUCT', 'SHAREPRODUCT', 'READ_DIVIDEND', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('725', 'SHAREACCOUNT', 'APPROVE_SHAREACCOUNT', 'SHAREACCOUNT', 'APPROVE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('726', 'SHAREACCOUNT', 'ACTIVATE_SHAREACCOUNT', 'SHAREACCOUNT', 'ACTIVATE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('727', 'SHAREACCOUNT', 'UNDOAPPROVAL_SHAREACCOUNT', 'SHAREACCOUNT', 'UNDOAPPROVAL', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('728', 'SHAREACCOUNT', 'REJECT_SHAREACCOUNT', 'SHAREACCOUNT', 'REJECT', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('729', 'SHAREACCOUNT', 'APPLYADDITIONALSHARES_SHAREACCOUNT', 'SHAREACCOUNT', 'APPLYADDITIONALSHARES', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('730', 'SHAREACCOUNT', 'APPROVEADDITIONALSHARES_SHAREACCOUNT', 'SHAREACCOUNT', 'APPROVEADDITIONALSHARES', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('731', 'SHAREACCOUNT', 'REJECTADDITIONALSHARES_SHAREACCOUNT', 'SHAREACCOUNT', 'REJECTADDITIONALSHARES', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('732', 'SHAREACCOUNT', 'REDEEMSHARES_SHAREACCOUNT', 'SHAREACCOUNT', 'REDEEMSHARES', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('733', 'SHAREACCOUNT', 'CLOSE_SHAREACCOUNT', 'SHAREACCOUNT', 'CLOSE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('734', 'SSBENEFICIARYTPT', 'READ_SSBENEFICIARYTPT', 'SSBENEFICIARYTPT', 'READ', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('735', 'SSBENEFICIARYTPT', 'CREATE_SSBENEFICIARYTPT', 'SSBENEFICIARYTPT', 'CREATE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('736', 'SSBENEFICIARYTPT', 'UPDATE_SSBENEFICIARYTPT', 'SSBENEFICIARYTPT', 'UPDATE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('737', 'SSBENEFICIARYTPT', 'DELETE_SSBENEFICIARYTPT', 'SSBENEFICIARYTPT', 'DELETE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('738', 'portfolio', 'FORECLOSURE_LOAN', 'LOAN', 'FORECLOSURE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('739', 'portfolio', 'FORECLOSURE_LOAN_CHECKER', 'LOAN', 'FORECLOSURE_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('740', 'portfolio', 'CREATE_ADDRESS', 'ADDRESS', 'CREATE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('742', 'portfolio', 'UPDATE_ADDRESS', 'ADDRESS', 'UPDATE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('744', 'portfolio', 'READ_ADDRESS', 'ADDRESS', 'READ', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('745', 'portfolio', 'DELETE_ADDRESS', 'ADDRESS', 'DELETE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('747', 'jobs', 'CREATE_REPORTMAILINGJOB', 'REPORTMAILINGJOB', 'CREATE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('748', 'jobs', 'UPDATE_REPORTMAILINGJOB', 'REPORTMAILINGJOB', 'UPDATE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('749', 'jobs', 'DELETE_REPORTMAILINGJOB', 'REPORTMAILINGJOB', 'DELETE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('750', 'jobs', 'READ_REPORTMAILINGJOB', 'REPORTMAILINGJOB', 'READ', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('751', 'portfolio', 'UNDOREJECT_CLIENT', 'CLIENT', 'UNDOREJECT', 't');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('753', 'portfolio', 'UNDOWITHDRAWAL_CLIENT', 'CLIENT', 'UNDOWITHDRAWAL', 't');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('755', 'organisation', 'READ_SMSCAMPAIGN', 'SMSCAMPAIGN', 'READ', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('756', 'organisation', 'CREATE_SMSCAMPAIGN', 'SMSCAMPAIGN', 'CREATE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('757', 'organisation', 'CREATE_SMSCAMPAIGN_CHECKER', 'SMSCAMPAIGN', 'CREATE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('758', 'organisation', 'UPDATE_SMSCAMPAIGN', 'SMSCAMPAIGN', 'UPDATE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('759', 'organisation', 'UPDATE_SMSCAMPAIGN_CHECKER', 'SMSCAMPAIGN', 'UPDATE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('760', 'organisation', 'DELETE_SMSCAMPAIGN', 'SMSCAMPAIGN', 'DELETE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('761', 'organisation', 'DELETE_SMSCAMPAIGN_CHECKER', 'SMSCAMPAIGN', 'DELETE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('762', 'organisation', 'ACTIVATE_SMSCAMPAIGN', 'SMSCAMPAIGN', 'ACTIVATE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('763', 'organisation', 'CLOSE_SMSCAMPAIGN', 'SMSCAMPAIGN', 'CLOSE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('764', 'organisation', 'REACTIVATE_SMSCAMPAIGN', 'SMSCAMPAIGN', 'REACTIVATE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('765', 'organisation', 'READ_EMAIL', 'EMAIL', 'READ', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('766', 'organisation', 'CREATE_EMAIL', 'EMAIL', 'CREATE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('767', 'organisation', 'CREATE_EMAIL_CHECKER', 'EMAIL', 'CREATE_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('768', 'organisation', 'UPDATE_EMAIL', 'EMAIL', 'UPDATE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('769', 'organisation', 'UPDATE_EMAIL_CHECKER', 'EMAIL', 'UPDATE_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('770', 'organisation', 'DELETE_EMAIL', 'EMAIL', 'DELETE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('743', 'portfolio', 'UPDATE_ADDRESS_CHECKER', 'ADDRESS', 'UPDATE_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('746', 'portfolio', 'DELETE_ADDRESS_CHECKER', 'ADDRESS', 'DELETE_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('754', 'portfolio', 'UNDOWITHDRAWAL_CLIENT_CHECKER', 'CLIENT', 'UNDOWITHDRAWAL_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('772', 'organisation', 'READ_EMAIL_CAMPAIGN', 'EMAIL_CAMPAIGN', 'READ', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('773', 'organisation', 'CREATE_EMAIL_CAMPAIGN', 'EMAIL_CAMPAIGN', 'CREATE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('774', 'organisation', 'CREATE_EMAIL_CAMPAIGN_CHECKER', 'EMAIL_CAMPAIGN', 'CREATE_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('775', 'organisation', 'UPDATE_EMAIL_CAMPAIGN', 'EMAIL_CAMPAIGN', 'UPDATE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('776', 'organisation', 'UPDATE_EMAIL_CAMPAIGN_CHECKER', 'EMAIL_CAMPAIGN', 'UPDATE_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('777', 'organisation', 'DELETE_EMAIL_CAMPAIGN', 'EMAIL_CAMPAIGN', 'DELETE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('778', 'organisation', 'DELETE_EMAIL_CAMPAIGN_CHECKER', 'EMAIL_CAMPAIGN', 'DELETE_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('779', 'organisation', 'CLOSE_EMAIL_CAMPAIGN', 'EMAIL_CAMPAIGN', 'CLOSE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('780', 'organisation', 'ACTIVATE_EMAIL_CAMPAIGN', 'EMAIL_CAMPAIGN', 'ACTIVATE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('781', 'organisation', 'REACTIVATE_EMAIL_CAMPAIGN', 'EMAIL_CAMPAIGN', 'REACTIVATE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('782', 'organisation', 'READ_EMAIL_CONFIGURATION', 'EMAIL_CONFIGURATION', 'READ', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('783', 'organisation', 'UPDATE_EMAIL_CONFIGURATION', 'EMAIL_CONFIGURATION', 'UPDATE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('784', 'report', 'READ_Active Clients - Email', 'Active Clients - Email', 'READ', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('785', 'report', 'READ_Prospective Clients - Email', 'Prospective Clients - Email', 'READ', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('786', 'report', 'READ_Active Loan Clients - Email', 'Active Loan Clients - Email', 'READ', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('787', 'report', 'READ_Loans in arrears - Email', 'Loans in arrears - Email', 'READ', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('788', 'report', 'READ_Loans disbursed to clients - Email', 'Loans disbursed to clients - Email', 'READ', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('789', 'report', 'READ_Loan payments due - Email', 'Loan payments due - Email', 'READ', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('790', 'report', 'READ_Dormant Prospects - Email', 'Dormant Prospects - Email', 'READ', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('791', 'report', 'READ_Active Group Leaders - Email', 'Active Group Leaders - Email', 'READ', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('792', 'report', 'READ_Loan Payments Due (Overdue Loans) - Email', 'Loan Payments Due (Overdue Loans) - Email', 'READ', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('793', 'report', 'READ_Loan Payments Received (Active Loans) - Email', 'Loan Payments Received (Active Loans) - Email', 'READ', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('794', 'report', 'READ_Loan Payments Received (Overdue Loans) - Email', 'Loan Payments Received (Overdue Loans)  - Email', 'READ', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('795', 'report', 'READ_Loan Fully Repaid - Email', 'Loan Fully Repaid - Email', 'READ', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('796', 'report', 'READ_Loans Outstanding after final instalment date - Email', 'Loans Outstanding after final instalment date - Email', 'READ', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('797', 'report', 'READ_Happy Birthday - Email', 'Happy Birthday - Email', 'READ', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('798', 'report', 'READ_Loan Rejected - Email', 'Loan Rejected - Email', 'READ', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('799', 'report', 'READ_Loan Approved - Email', 'Loan Approved - Email', 'READ', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('800', 'report', 'READ_Loan Repayment - Email', 'Loan Repayment - Email', 'READ', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('801', 'datatable', 'READ_ENTITY_DATATABLE_CHECK', 'ENTITY_DATATABLE_CHECK', 'READ', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('802', 'datatable', 'CREATE_ENTITY_DATATABLE_CHECK', 'ENTITY_DATATABLE_CHECK', 'CREATE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('803', 'datatable', 'DELETE_ENTITY_DATATABLE_CHECK', 'ENTITY_DATATABLE_CHECK', 'DELETE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('804', 'configuration', 'CREATE_CREDITBUREAU_LOANPRODUCT_MAPPING', 'CREDITBUREAU_LOANPRODUCT_MAPPING', 'CREATE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('805', 'configuration', 'CREATE_ORGANISATIONCREDITBUREAU', 'ORGANISATIONCREDITBUREAU', 'CREATE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('806', 'configuration', 'UPDATE_ORGANISATIONCREDITBUREAU', 'ORGANISATIONCREDITBUREAU', 'UPDATE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('807', 'configuration', 'UPDATE_CREDITBUREAU_LOANPRODUCT_MAPPING', 'CREDITBUREAU_LOANPRODUCT_MAPPING', 'UPDATE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('808', 'configuration', 'GET_CREDITREPORT', 'CREDITREPORT', 'GET', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('809', 'configuration', 'CREATE_CREDITBUREAU_CONFIGURATION', 'CREDITBUREAU_CONFIGURATION', 'CREATE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('810', 'configuration', 'UPDATE_CREDITBUREAU_CONFIGURATION', 'CREDITBUREAU_CONFIGURATION', 'UPDATE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('811', 'configuration', 'SAVE_CREDITREPORT', 'CREDITREPORT', 'SAVE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('812', 'configuration', 'DELETE_CREDITREPORT', 'CREDITREPORT', 'DELETE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('813', 'portfolio', 'CREATE_FAMILYMEMBERS', 'FAMILYMEMBERS', 'CREATE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('814', 'portfolio', 'UPDATE_FAMILYMEMBERS', 'FAMILYMEMBERS', 'UPDATE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('815', 'portfolio', 'DELETE_FAMILYMEMBERS', 'FAMILYMEMBERS', 'DELETE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('816', 'transaction_savings', 'HOLDAMOUNT_SAVINGSACCOUNT', 'SAVINGSACCOUNT', 'HOLDAMOUNT', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('817', 'transaction_savings', 'HOLDAMOUNT_SAVINGSACCOUNT_CHECKER', 'SAVINGSACCOUNT', 'HOLDAMOUNT_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('818', 'transaction_savings', 'BLOCKDEBIT_SAVINGSACCOUNT', 'SAVINGSACCOUNT', 'BLOCKDEBIT', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('819', 'transaction_savings', 'BLOCKDEBIT_SAVINGSACCOUNT_CHECKER', 'SAVINGSACCOUNT', 'BLOCKDEBIT_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('820', 'transaction_savings', 'UNBLOCKDEBIT_SAVINGSACCOUNT', 'SAVINGSACCOUNT', 'UNBLOCKDEBIT', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('821', 'transaction_savings', 'UNBLOCKDEBIT_SAVINGSACCOUNT_CHECKER', 'SAVINGSACCOUNT', 'UNBLOCKDEBIT_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('822', 'transaction_savings', 'BLOCKCREDIT_SAVINGSACCOUNT', 'SAVINGSACCOUNT', 'BLOCKCREDIT', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('823', 'transaction_savings', 'BLOCKCREDIT_SAVINGSACCOUNT_CHECKER', 'SAVINGSACCOUNT', 'BLOCKCREDIT_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('824', 'transaction_savings', 'UNBLOCKCREDIT_SAVINGSACCOUNT', 'SAVINGSACCOUNT', 'UNBLOCKCREDIT', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('825', 'transaction_savings', 'UNBLOCKCREDIT_SAVINGSACCOUNT_CHECKER', 'SAVINGSACCOUNT', 'UNBLOCKCREDIT_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('826', 'transaction_savings', 'BLOCK_SAVINGSACCOUNT', 'SAVINGSACCOUNT', 'BLOCK', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('827', 'transaction_savings', 'BLOCK_SAVINGSACCOUNT_CHECKER', 'SAVINGSACCOUNT', 'BLOCK_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('828', 'transaction_savings', 'UNBLOCK_SAVINGSACCOUNT', 'SAVINGSACCOUNT', 'UNBLOCK', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('829', 'transaction_savings', 'UNBLOCK_SAVINGSACCOUNT_CHECKER', 'SAVINGSACCOUNT', 'UNBLOCK_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('830', 'transaction_savings', 'RELEASEAMOUNT_SAVINGSACCOUNT', 'SAVINGSACCOUNT', 'RELEASEAMOUNT', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('831', 'transaction_savings', 'RELEASEAMOUNT_SAVINGSACCOUNT_CHECKER', 'SAVINGSACCOUNT', 'RELEASEAMOUNT_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('832', 'authorisation', 'UPDATE_ADHOC', 'ADHOC', 'UPDATE', 't');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('833', 'authorisation', 'UPDATE_ADHOC_CHECKER', 'ADHOC', 'UPDATE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('834', 'authorisation', 'DELETE_ADHOC', 'ADHOC', 'DELETE', 't');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('835', 'authorisation', 'DELETE_ADHOC_CHECKER', 'ADHOC', 'DELETE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('836', 'authorisation', 'CREATE_ADHOC', 'ADHOC', 'CREATE', 't');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('837', 'authorisation', 'CREATE_ADHOC_CHECKER', 'ADHOC', 'CREATE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('838', 'authorisation', 'INVALIDATE_TWOFACTOR_ACCESSTOKEN', 'TWOFACTOR_ACCESSTOKEN', 'INVALIDATE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('839', 'configuration', 'READ_TWOFACTOR_CONFIGURATION', 'TWOFACTOR_CONFIGURATION', 'READ', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('840', 'configuration', 'UPDATE_TWOFACTOR_CONFIGURATION', 'TWOFACTOR_CONFIGURATION', 'UPDATE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('841', 'special', 'BYPASS_TWOFACTOR', NULL, NULL, 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('842', 'infrastructure', 'READ_IMPORT', 'IMPORT', 'READ', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('843', 'portfolio', 'LINK_ACCOUNT_TO_POCKET', 'POCKET', 'LINK_ACCOUNT_TO', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('844', 'portfolio', 'DELINK_ACCOUNT_FROM_POCKET', 'POCKET', 'DELINK_ACCOUNT_FROM', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('845', 'interop', 'READ_INTERID', 'INTERID', 'READ', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('846', 'interop', 'READ_INTERREQUEST', 'INTERREQUEST', 'READ', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('847', 'interop', 'READ_INTERQUOTE', 'INTERQUOTE', 'READ', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('848', 'interop', 'READ_INTERTRANSFER', 'INTERTRANSFER', 'READ', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('849', 'interop', 'PREPARE_INTERTRANSFER', 'INTERTRANSFER', 'PREPARE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('850', 'interop', 'RELEASE_INTERTRANSFER', 'INTERTRANSFER', 'RELEASE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('851', 'interop', 'CREATE_INTERID', 'INTERID', 'CREATE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('852', 'interop', 'CREATE_INTERREQUEST', 'INTERREQUEST', 'CREATE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('853', 'interop', 'CREATE_INTERQUOTE', 'INTERQUOTE', 'CREATE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('854', 'interop', 'CREATE_INTERTRANSFER', 'INTERTRANSFER', 'CREATE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('855', 'interop', 'DELETE_INTERID', 'INTERID', 'DELETE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('856', 'organisation', 'READ_RATE', 'RATE', 'CREATE', 't');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('857', 'organisation', 'CREATE_RATE', 'RATE', 'CREATE', 't');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('858', 'organisation', 'UPDATE_RATE', 'RATE', 'UPDATE', 't');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('859', 'portfolio', 'CREATE_GSIMACCOUNT', 'GSIMACCOUNT', 'CREATE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('860', 'portfolio', 'APPROVE_GSIMACCOUNT', 'GSIMACCOUNT', 'APPROVE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('861', 'portfolio', 'ACTIVATE_GSIMACCOUNT', 'GSIMACCOUNT', 'ACTIVATE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('862', 'portfolio', 'APPROVALUNDO_GSIMACCOUNT', 'GSIMACCOUNT', 'APPROVALUNDO', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('863', 'portfolio', 'UPDATE_GSIMACCOUNT', 'GSIMACCOUNT', 'UPDATE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('864', 'portfolio', 'REJECT_GSIMACCOUNT', 'GSIMACCOUNT', 'REJECT', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('865', 'portfolio', 'DEPOSIT_GSIMACCOUNT', 'GSIMACCOUNT', 'DEPOSIT', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('866', 'portfolio', 'CLOSE_GSIMACCOUNT', 'GSIMACCOUNT', 'CLOSE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('867', 'portfolio', 'APPROVE_GLIMLOAN', 'GLIMLOAN', 'APPROVE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('868', 'portfolio', 'DISBURSE_GLIMLOAN', 'GLIMLOAN', 'DISBURSE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('869', 'portfolio', 'REPAYMENT_GLIMLOAN', 'GLIMLOAN', 'REPAYMENT', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('870', 'portfolio', 'UNDODISBURSAL_GLIMLOAN', 'GLIMLOAN', 'UNDODISBURSAL', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('871', 'portfolio', 'UNDOAPPROVAL_GLIMLOAN', 'GLIMLOAN', 'UNDOAPPROVAL', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('872', 'portfolio', 'REJECT_GLIMLOAN', 'GLIMLOAN', 'REJECT', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('873', 'portfolio', 'CREATE_CLIENT_COLLATERAL_PRODUCT', 'CLIENT_COLLATERAL_PRODUCT', 'CREATE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('874', 'portfolio', 'CREATE_COLLATERAL_PRODUCT', 'COLLATERAL_PRODUCT', 'CREATE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('875', 'portfolio', 'DELETE_CLIENT_COLLATERAL_PRODUCT', 'CLIENT_COLLATERAL_PRODUCT', 'DELETE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('876', 'portfolio', 'DELETE_COLLATERAL_PRODUCT', 'COLLATERAL_PRODUCT', 'DELETE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('877', 'portfolio', 'DELETE_LOAN_COLLATERAL_PRODUCT', 'LOAN_COLLATERAL_PRODUCT', 'DELETE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('878', 'portfolio', 'UPDATE_CLIENT_COLLATERAL_PRODUCT', 'CLIENT_COLLATERAL_PRODUCT', 'UPDATE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('879', 'portfolio', 'UPDATE_COLLATERAL_PRODUCT', 'COLLATERAL_PRODUCT', 'UPDATE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('880', 'portfolio', 'UPDATE_REPAYMENT_WITH_POSTDATEDCHECKS', 'REPAYMENT_WITH_POSTDATEDCHECKS', 'UPDATE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('881', 'portfolio', 'DELETE_REPAYMENT_WITH_POSTDATEDCHECKS', 'REPAYMENT_WITH_POSTDATEDCHECKS', 'DELETE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('882', 'portfolio', 'BOUNCE_REPAYMENT_WITH_POSTDATEDCHECKS', 'REPAYMENT_WITH_POSTDATEDCHECKS', 'BOUNCE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('883', 'transaction_savings', 'REVERSETRANSACTION_SAVINGSACCOUNT', 'SAVINGSACCOUNT', 'REVERSETRANSACTION', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('884', 'transaction_savings', 'REVERSETRANSACTION_SAVINGSACCOUNT_CHECKER', 'SAVINGSACCOUNT', 'REVERSETRANSACTION_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('885', 'transaction_loan', 'CREDITBALANCEREFUND_LOAN', 'LOAN', 'CREDITBALANCEREFUND', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('886', 'transaction_loan', 'MERCHANTISSUEDREFUND_LOAN', 'LOAN', 'MERCHANTISSUEDREFUND', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('887', 'transaction_loan', 'PAYOUTREFUND_LOAN', 'LOAN', 'PAYOUTREFUND', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('888', 'transaction_loan', 'GOODWILLCREDIT_LOAN', 'LOAN', 'GOODWILLCREDIT', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('889', 'organisation', 'READ_BUSINESS_DATE', 'BUSINESS_DATE', 'READ', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('890', 'organisation', 'UPDATE_BUSINESS_DATE', 'BUSINESS_DATE', 'UPDATE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('891', 'transaction_loan', 'CHARGEREFUND_LOAN', 'LOAN', 'CHARGEREFUND', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('892', 'organisation', 'READ_DELINQUENCY_BUCKET', 'DELINQUENCY_BUCKET', 'READ', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('893', 'organisation', 'CREATE_DELINQUENCY_BUCKET', 'DELINQUENCY_BUCKET', 'CREATE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('894', 'organisation', 'UPDATE_DELINQUENCY_BUCKET', 'DELINQUENCY_BUCKET', 'UPDATE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('895', 'organisation', 'DELETE_DELINQUENCY_BUCKET', 'DELINQUENCY_BUCKET', 'DELETE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('896', 'organisation', 'CREATE_DELINQUENCY_RANGE', 'DELINQUENCY_RANGE', 'CREATE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('897', 'organisation', 'UPDATE_DELINQUENCY_RANGE', 'DELINQUENCY_RANGE', 'UPDATE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('898', 'organisation', 'DELETE_DELINQUENCY_RANGE', 'DELINQUENCY_RANGE', 'DELETE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('899', 'organisation', 'READ_DELINQUENCY_TAGS', 'DELINQUENCY_TAGS', 'READ', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('900', 'organisation', 'UPDATE_DELINQUENCY_TAGS', 'DELINQUENCY_TAGS', 'UPDATE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('901', 'organisation', 'UPDATEDELINQUENCY_LOAN', 'LOAN', 'UPDATEDELINQUENCY', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('902', 'organisation', 'UPDATE_BATCH_BUSINESS_STEP', 'BATCH_BUSINESS_STEP', 'UPDATE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('903', 'transaction_loan', 'CHARGEBACK_LOAN', 'LOAN', 'CHARGEBACK', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('904', 'organisation', 'EXECUTE_INLINE_JOB', 'INLINE_JOB', 'EXECUTE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('905', 'portfolio', 'SETFRAUD_LOAN', 'LOAN', 'SETFRAUD', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('906', 'configuration', 'UPDATE_EXTERNAL_EVENT_CONFIGURATION', 'EXTERNAL_EVENT_CONFIGURATION', 'UPDATE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('907', 'configuration', 'READ_EXTERNAL_EVENT_CONFIGURATION', 'EXTERNAL_EVENT_CONFIGURATION', 'READ', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('908', 'transaction_loan', 'BYPASS_LOAN_WRITE_PROTECTION', 'LOAN', 'BYPASS', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('909', 'transaction_loan', 'ADJUSTMENT_LOANCHARGE', 'LOANCHARGE', 'ADJUSTMENT', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('910', 'transaction_loan', 'CHARGEOFF_LOAN', 'LOAN', 'CHARGEOFF', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('911', 'transaction_loan', 'UNDOCHARGEOFF_LOAN', 'LOAN', 'UNDOCHARGEOFF', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('912', 'organisation', 'CREATE_DELINQUENCY_ACTION', 'DELINQUENCY_ACTION', 'CREATE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('913', 'transaction_loan', 'DISBURSEWITHOUTAUTODOWNPAYMENT_LOAN', 'LOAN', 'DISBURSEWITHOUTAUTODOWNPAYMENT', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('914', 'transaction_loan', 'INTERESTPAYMENTWAIVER_LOAN', 'LOAN', 'INTERESTPAYMENTWAIVER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('915', 'investor', 'CANCEL_ASSET_OWNER_TRANSACTION', 'ASSET_OWNER_TRANSACTION', 'CANCEL', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('916', 'portfolio', 'REAGE_LOAN', 'LOAN', 'REAGE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('917', 'portfolio', 'UNDO_REAGE_LOAN', 'LOAN', 'UNDO_REAGE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('918', 'portfolio', 'REAMORTIZE_LOAN', 'LOAN', 'REAMORTIZE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('919', 'portfolio', 'UNDO_REAMORTIZE_LOAN', 'LOAN', 'UNDO_REAMORTIZE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('920', 'portfolio', 'CREATE_INTEREST_PAUSE', 'INTEREST_PAUSE', 'CREATE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('921', 'portfolio', 'UPDATE_INTEREST_PAUSE', 'INTEREST_PAUSE', 'UPDATE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('922', 'portfolio', 'DELETE_INTEREST_PAUSE', 'INTEREST_PAUSE', 'DELETE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('923', 'LOAN_PROVISIONING', 'UPDATE_PROVISIONCATEGORY', 'PROVISIONCATEGORY', 'UPDATE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('924', 'portfolio', 'DEACTIVATEOVERDUE_LOANCHARGE', 'LOANCHARGE', 'DEACTIVATEOVERDUE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('925', 'authorisation', 'CHANGEPWD_USER', 'USER', 'CHANGEPWD', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('926', 'portfolio', 'CLOSE_CLIENT_CHECKER', 'CLIENT', 'CLOSE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('927', 'portfolio', 'CLOSE_GROUP_CHECKER', 'GROUP', 'CLOSE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('928', 'portfolio', 'CLOSE_CENTER_CHECKER', 'CENTER', 'CLOSE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('929', 'portfolio', 'REMOVESAVINGSOFFICER_SAVINGSACCOUNT_CHECKER', 'SAVINGSACCOUNT', 'REMOVESAVINGSOFFICER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('930', 'portfolio', 'UPDATESAVINGSOFFICER_SAVINGSACCOUNT_CHECKER', 'SAVINGSACCOUNT', 'UPDATESAVINGSOFFICER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('931', 'cash_mgmt', 'CREATE_TELLER_CHECKER', 'TELLER', 'CREATE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('932', 'cash_mgmt', 'UPDATE_TELLER_CHECKER', 'TELLER', 'UPDATE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('933', 'cash_mgmt', 'ALLOCATECASHIER_TELLER_CHECKER', 'TELLER', 'ALLOCATE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('934', 'cash_mgmt', 'UPDATECASHIERALLOCATION_TELLER_CHECKER', 'TELLER', 'UPDATECASHIERALLOCATION', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('935', 'cash_mgmt', 'DELETECASHIERALLOCATION_TELLER_CHECKER', 'TELLER', 'DELETECASHIERALLOCATION', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('936', 'cash_mgmt', 'ALLOCATECASHTOCASHIER_TELLER_CHECKER', 'TELLER', 'ALLOCATECASHTOCASHIER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('937', 'cash_mgmt', 'SETTLECASHFROMCASHIER_TELLER_CHECKER', 'TELLER', 'SETTLECASHFROMCASHIER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('938', 'accounting', 'DEFINEOPENINGBALANCE_JOURNALENTRY_CHECKER', 'JOURNALENTRY', 'DEFINEOPENINGBALANCE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('939', 'cash_mgmt', 'DELETE_TELLER_CHECKER', 'TELLER', 'DELETE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('940', 'organisation', 'READ_RATE_CHECKER', 'RATE', 'CREATE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('941', 'organisation', 'CREATE_RATE_CHECKER', 'RATE', 'CREATE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('942', 'organisation', 'UPDATE_RATE_CHECKER', 'RATE', 'UPDATE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('943', 'accounting', 'UPDATEOPENINGBALANCE_JOURNALENTRY', 'JOURNALENTRY', 'UPDATEOPENINGBALANCE', 't');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('944', 'accounting', 'UPDATEOPENINGBALANCE_JOURNALENTRY_CHECKER', 'JOURNALENTRY', 'UPDATEOPENINGBALANCE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('945', 'account_transfer', 'CREATE_STANDINGINSTRUCTION', 'STANDINGINSTRUCTION', 'CREATE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('946', 'account_transfer', 'UPDATE_STANDINGINSTRUCTION', 'STANDINGINSTRUCTION', 'UPDATE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('947', 'account_transfer', 'DELETE_STANDINGINSTRUCTION', 'STANDINGINSTRUCTION', 'DELETE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('948', 'collection_sheet', 'UPDATE_COLLECTIONSHEET', 'COLLECTIONSHEET', 'UPDATE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('949', 'transaction_savings', 'POSTINTERESTASONDATE_SAVINGSACCOUNT', 'SAVINGSACCOUNT', 'POSTINTERESTASONDATE', 't');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('950', 'transaction_savings', 'POSTINTERESTASONDATE_SAVINGSACCOUNT_CHECKER', 'SAVINGSACCOUNT', 'POSTINTERESTASONDATE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('951', 'transaction_loan', 'CAPITALIZEDINCOME_LOAN', 'LOAN', 'CAPITALIZEDINCOME', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('952', 'transaction_loan', 'CAPITALIZEDINCOMEADJUSTMENT_LOAN', 'LOAN', 'CAPITALIZEDINCOMEADJUSTMENT', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('953', 'transaction_loan', 'BUYDOWNFEEADJUSTMENT_LOAN', 'LOAN', 'BUYDOWNFEEADJUSTMENT', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('954', 'transaction_loan', 'BUYDOWNFEE_LOAN', 'LOAN', 'BUYDOWNFEE', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('955', 'portfolio', 'CONTRACT_TERMINATION_UNDO_LOAN', 'LOAN', 'CONTRACT_TERMINATION_UNDO', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('956', 'transaction_loan', 'UPDATE_APPROVED_AMOUNT_LOAN', 'LOAN', 'UPDATE_APPROVED_AMOUNT', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('957', 'transaction_loan', 'MANUAL_INTEREST_REFUND_TRANSACTION_LOAN', 'LOAN', 'MANUAL_INTEREST_REFUND_TRANSACTION', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('741', 'portfolio', 'CREATE_ADDRESS_CHECKER', 'ADDRESS', 'CREATE_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('752', 'portfolio', 'UNDOREJECT_CLIENT_CHECKER', 'CLIENT', 'UNDOREJECT_CHECKER', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('622', 'transaction_savings', 'UPDATEDEPOSITAMOUNT_RECURRINGDEPOSITACCOUNT_CHECKER', 'RECURRINGDEPOSITACCOUNT', 'UPDATEDEPOSITAMOUNT', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('958', 'portfolio', 'READ_FAMILYMEMBERS', 'FAMILYMEMBERS', 'READ', 'f');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('959', 'portfolio', 'FORCE_WITHDRAWAL_SAVINGSACCOUNT', 'SAVINGSACCOUNT', 'FORCE_WITHDRAWAL', 't');
INSERT INTO public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) VALUES ('960', 'portfolio', 'FORCE_WITHDRAWAL_SAVINGSACCOUNT_CHECKER', 'SAVINGSACCOUNT', 'FORCE_WITHDRAWAL_CHECKER', 'f');


-- Data for Name: m_portfolio_command_source; Type: TABLE DATA; Schema: public; Owner: -



-- Data for Name: m_product_loan_charge; Type: TABLE DATA; Schema: public; Owner: -



-- Data for Name: m_product_loan_configurable_attributes; Type: TABLE DATA; Schema: public; Owner: -



-- Data for Name: m_product_loan_floating_rates; Type: TABLE DATA; Schema: public; Owner: -



-- Data for Name: m_product_loan_guarantee_details; Type: TABLE DATA; Schema: public; Owner: -



-- Data for Name: m_product_loan_rate; Type: TABLE DATA; Schema: public; Owner: -



-- Data for Name: m_product_loan_recalculation_details; Type: TABLE DATA; Schema: public; Owner: -



-- Data for Name: m_product_loan_variable_installment_config; Type: TABLE DATA; Schema: public; Owner: -



-- Data for Name: m_product_loan_variations_borrower_cycle; Type: TABLE DATA; Schema: public; Owner: -



-- Data for Name: m_product_mix; Type: TABLE DATA; Schema: public; Owner: -



-- Data for Name: m_provisioning_criteria_definition; Type: TABLE DATA; Schema: public; Owner: -



-- Data for Name: m_repayment_with_post_dated_checks; Type: TABLE DATA; Schema: public; Owner: -



-- Data for Name: stretchy_report; Type: TABLE DATA; Schema: public; Owner: -

INSERT INTO public.stretchy_report (id, report_name, report_type, report_subtype, report_category, report_sql, description, core_report, use_report, self_service_user_report) VALUES ('1', 'Client Listing', 'Table', NULL, 'Client', 'SELECT Concat(REPEAT(''..'', (( Length(ounder.hierarchy) - Length( REPLACE(ounder.hierarchy, ''.'', '''')) - 1 ))) , ounder.name)  AS "Office/Branch", c.account_no  AS "Client Account No.", c.display_name  AS name, r.enum_message_property AS "Status", c.activation_date AS "Activation", c.external_id AS "External Id" FROM   m_office o JOIN m_office ounder ON ounder.hierarchy LIKE Concat(o.hierarchy, ''%'') AND ounder.hierarchy LIKE Concat(''${currentUserHierarchy}'', ''%'') JOIN m_client c ON c.office_id = ounder.id LEFT JOIN r_enum_value r ON r.enum_name = ''status_enum'' AND r.enum_id = c.status_enum WHERE  o.id = ''${officeId}'' ORDER  BY ounder.hierarchy, c.account_no', 'Individual Client Report    Lists the small number of defined fields on the client table.  Would expect to copy this   report and add any ''one to one'' additional data for specific tenant needs.    Can be run for any size MFI but you''d expect it only to be run within a branch for   larger ones.  Depending on how many columns are displayed, there is probably is a limit of about 20/50k clients returned for html display (export to excel doesn''t   have that client browser/memory impact).', 't', 't', 'f');
INSERT INTO public.stretchy_report (id, report_name, report_type, report_subtype, report_category, report_sql, description, core_report, use_report, self_service_user_report) VALUES ('2', 'Client Loans Listing', 'Table', NULL, 'Client', 'SELECT    Concat(REPEAT(''..'', ((Length(ounder.hierarchy) - Length(REPLACE(ounder.hierarchy, ''.'', '''')) - 1))), ounder.name) AS "Office/Branch", c.account_no AS "Client Account No.", c.display_name AS name, r.enum_message_property  AS "Client Status", lo.display_name  AS "Loan Officer", l.account_no AS "Loan Account No.", l.external_id  AS "External Id", p.name AS loan, st.enum_message_property AS "Status", f.name AS fund, purp.code_value  AS "Loan Purpose", Coalesce(cur.display_symbol, l.currency_code)  AS currency, l.principal_amount, l.arrearstolerance_amount AS "Arrears Tolerance Amount", l.number_of_repayments  AS "Expected No. Repayments", l.annual_nominal_interest_rate  AS "Annual Nominal Interest Rate", l.nominal_interest_rate_per_period  AS "Nominal Interest Rate Per Period", ipf.enum_message_property AS "Interest Rate Frequency", im.enum_message_property  AS "Interest Method", icp.enum_message_property AS "Interest Calculated in Period", l.term_frequency  AS "Term Frequency", tf.enum_message_property  AS "Term Frequency Period", l.repay_every AS "Repayment Frequency", rf.enum_message_property  AS "Repayment Frequency Period", am.enum_message_property  AS "Amortization", l.total_charges_due_at_disbursement_derived AS "Total Charges Due At Disbursement", DATE_TRUNC(''day'', l.submittedon_date) AS submitted, DATE_TRUNC(''day'', l.approvedon_date)                   approved, l.expected_disbursedon_date AS "Expected Disbursal", DATE_TRUNC(''day'', l.expected_firstrepaymenton_date) AS "Expected First Repayment", DATE_TRUNC(''day'', l.interest_calculated_from_date)  AS "Interest Calculated From" , DATE_TRUNC(''day'', l.disbursedon_date) AS disbursed, DATE_TRUNC(''day'', l.expected_maturedon_date)  AS "Expected Maturity", DATE_TRUNC(''day'', l.maturedon_date) AS "Matured On", DATE_TRUNC(''day'', l.closedon_date)  AS closed, DATE_TRUNC(''day'', l.rejectedon_date)  AS rejected, DATE_TRUNC(''day'', l.rescheduledon_date) AS rescheduled, DATE_TRUNC(''day'', l.withdrawnon_date) AS withdrawn, DATE_TRUNC(''day'', l.writtenoffon_date)  AS "Written Off" FROM      m_office o JOIN      m_office ounder ON        ounder.hierarchy LIKE concat(o.hierarchy, ''%'') AND       ounder.hierarchy LIKE concat(''${currentUserHierarchy}'', ''%'') JOIN      m_client c ON        c.office_id = ounder.id LEFT JOIN r_enum_value r ON        r.enum_name = ''status_enum'' AND       r.enum_id = c.status_enum LEFT JOIN m_loan l ON        l.client_id = c.id LEFT JOIN m_staff lo ON        lo.id = l.loan_officer_id LEFT JOIN m_product_loan p ON        p.id = l.product_id LEFT JOIN m_fund f ON        f.id = l.fund_id LEFT JOIN r_enum_value st ON        st.enum_name = ''loan_status_id'' AND       st.enum_id = l.loan_status_id LEFT JOIN r_enum_value ipf ON        ipf.enum_name = ''interest_period_frequency_enum'' AND       ipf.enum_id = l.interest_period_frequency_enum LEFT JOIN r_enum_value im ON        im.enum_name = ''interest_method_enum'' AND       im.enum_id = l.interest_method_enum LEFT JOIN r_enum_value tf ON        tf.enum_name = ''term_period_frequency_enum'' AND       tf.enum_id = l.term_period_frequency_enum LEFT JOIN r_enum_value icp ON        icp.enum_name = ''interest_calculated_in_period_enum'' AND       icp.enum_id = l.interest_calculated_in_period_enum LEFT JOIN r_enum_value rf ON        rf.enum_name = ''repayment_period_frequency_enum'' AND       rf.enum_id = l.repayment_period_frequency_enum LEFT JOIN r_enum_value am ON        am.enum_name = ''amortization_method_enum'' AND       am.enum_id = l.amortization_method_enum LEFT JOIN m_code_value purp ON        purp.id = l.loanpurpose_cv_id LEFT JOIN m_currency cur ON        cur.code = l.currency_code WHERE     o.id = ''${officeId}'' AND       ( l.currency_code = ''${currencyId}'' OR        ''-1'' = ''${currencyId}'') AND       ( l.product_id = ''${loanProductId}'' OR        ''-1'' = ''${loanProductId}'') AND       ( coalesce(l.loan_officer_id, -10) = ${loanOfficerId} OR        ''-1'' = ${loanOfficerId}) AND       ( coalesce(l.fund_id, -10) = ${fundId} OR        -1 = ${fundId}) AND       ( coalesce(l.loanpurpose_cv_id, -10) = ${loanPurposeId} OR        -1 = ${loanPurposeId}) ORDER BY  ounder.hierarchy, 2 , l.id', 'Individual Client Report    Pretty   wide report that lists the basic details of client loans.      Can be run for any size MFI but you''d expect it only to be run within a branch for larger ones.    There is probably is a limit of about 20/50k clients returned for html display (export to excel doesn''t have that client browser/memory impact).', 't', 't', 'f');
INSERT INTO public.stretchy_report (id, report_name, report_type, report_subtype, report_category, report_sql, description, core_report, use_report, self_service_user_report) VALUES ('5', 'Loans Awaiting Disbursal', 'Table', NULL, 'Loan', 'SELECT    Concat(REPEAT(''..'', ((Length(ounder.hierarchy) - Length(REPLACE(ounder.hierarchy, ''.'', '''')) - 1))), ounder.name) AS "Office/Branch", c.account_no AS "Client Account No", c.display_name AS name, l.account_no AS "Loan Account No.", pl.name  AS "Product", f.name AS fund, Coalesce(cur.display_symbol, l.currency_code)  AS currency, l.principal_amount AS principal, l.term_frequency AS "Term Frequency", tf.enum_message_property AS "Term Frequency Period", l.annual_nominal_interest_rate AS "Annual Nominal Interest Rate", DATE_TRUNC(''day'', l.approvedon_date) AS "Approved", extract(day FROM (l.expected_disbursedon_date::TIMESTAMP - CURRENT_DATE)) AS "Days to Disbursal", DATE_TRUNC(''day'', l.expected_disbursedon_date) AS "Expected Disbursal", purp.code_value AS "Loan Purpose", lo.display_name AS "Loan Officer" FROM      m_office o JOIN      m_office ounder ON        ounder.hierarchy LIKE concat(o.hierarchy, ''%'') AND       ounder.hierarchy LIKE concat(''${currentUserHierarchy}'', ''%'') JOIN      m_client c ON        c.office_id = ounder.id JOIN      m_loan l ON        l.client_id = c.id JOIN      m_product_loan pl ON        pl.id = l.product_id LEFT JOIN m_staff lo ON        lo.id = l.loan_officer_id LEFT JOIN m_currency cur ON        cur.code = l.currency_code LEFT JOIN m_fund f ON        f.id = l.fund_id LEFT JOIN m_code_value purp ON        purp.id = l.loanpurpose_cv_id LEFT JOIN r_enum_value tf ON        tf.enum_name = ''term_period_frequency_enum'' AND       tf.enum_id = l.term_period_frequency_enum WHERE     o.id = ''${officeId}'' AND       ( l.currency_code = ''${currencyId}'' OR        ''-1'' = ''${currencyId}'') AND       ( l.product_id = ''${loanProductId}'' OR        ''-1'' = ''${loanProductId}'') AND       ( coalesce(l.loan_officer_id, -10) = ''${loanOfficerId}'' OR        ''-1'' = ''${loanOfficerId}'') AND       ( coalesce(l.fund_id, -10) = ''${fundId}'' OR        -1 = ''${fundId}'') AND       ( coalesce(l.loanpurpose_cv_id, -10) = ''${loanPurposeId}'' OR        -1 = ''${loanPurposeId}'') AND       l.loan_status_id = 200 ORDER BY  ounder.hierarchy, extract(day FROM (l.expected_disbursedon_date::TIMESTAMP - CURRENT_DATE)), c.account_no', 'Individual Client Report', 't', 't', 'f');
INSERT INTO public.stretchy_report (id, report_name, report_type, report_subtype, report_category, report_sql, description, core_report, use_report, self_service_user_report) VALUES ('6', 'Loans Awaiting Disbursal Summary', 'Table', NULL, 'Loan', 'SELECT    Concat(REPEAT(''..'', ((Length(ounder.hierarchy) - Length(REPLACE(ounder.hierarchy, ''.'', '''')) - 1))), ounder.name) AS "Office/Branch", pl.name  AS "Product", Coalesce(cur.display_symbol, l.currency_code)  AS currency, f.name AS fund, Sum(l.principal_amount)  AS principal FROM      m_office o JOIN      m_office ounder ON        ounder.hierarchy LIKE Concat(o.hierarchy, ''%'') AND       ounder.hierarchy LIKE Concat(''${currentUserHierarchy}'', ''%'') JOIN      m_client c ON        c.office_id = ounder.id JOIN      m_loan l ON        l.client_id = c.id JOIN      m_product_loan pl ON        pl.id = l.product_id LEFT JOIN m_staff lo ON        lo.id = l.loan_officer_id LEFT JOIN m_currency cur ON        cur.code = l.currency_code LEFT JOIN m_fund f ON        f.id = l.fund_id LEFT JOIN m_code_value purp ON        purp.id = l.loanpurpose_cv_id WHERE     o.id = ''${officeId}'' AND       ( l.currency_code = ''${currencyId}'' OR        ''-1'' = ''${currencyId}'') AND       ( l.product_id = ''${loanProductId}'' OR        ''-1'' = ''${loanProductId}'') AND       ( coalesce(l.loan_officer_id, -10) = ''${loanOfficerId}'' OR        ''-1'' = ''${loanOfficerId}'') AND       ( coalesce(l.fund_id, -10) = ''${fundId}'' OR        -1 = ''${fundId}'') AND       ( coalesce(l.loanpurpose_cv_id, -10) = ''${loanPurposeId}'' OR        -1 = ''${loanPurposeId}'') AND       l.loan_status_id = 200 GROUP BY  ounder.hierarchy, pl.name, l.currency_code, f.name, ounder.name, cur.display_symbol ORDER BY  ounder.hierarchy, pl.name, l.currency_code, f.name', 'Individual Client Report', 't', 't', 'f');
INSERT INTO public.stretchy_report (id, report_name, report_type, report_subtype, report_category, report_sql, description, core_report, use_report, self_service_user_report) VALUES ('7', 'Loans Awaiting Disbursal Summary by Month', 'Table', NULL, 'Loan', 'SELECT    Concat(REPEAT(''..'', ((Length(ounder.hierarchy) - Length(REPLACE(ounder.hierarchy, ''.'', '''')) - 1))), ounder.name) AS "Office/Branch", pl.name  AS "Product", Coalesce(cur.display_symbol, l.currency_code)  AS "currency", extract(year from l.expected_disbursedon_date) AS "Year", to_char(l.expected_disbursedon_date, ''Month'')  AS "Month", Sum(l.principal_amount)  AS "principal" FROM      m_office o JOIN      m_office ounder ON        ounder.hierarchy LIKE Concat(o.hierarchy, ''%'') AND       ounder.hierarchy LIKE Concat(''${currentUserHierarchy}'', ''%'') JOIN      m_client c ON        c.office_id = ounder.id JOIN      m_loan l ON        l.client_id = c.id JOIN      m_product_loan pl ON        pl.id = l.product_id LEFT JOIN m_staff lo ON        lo.id = l.loan_officer_id LEFT JOIN m_currency cur ON        cur.code = l.currency_code LEFT JOIN m_fund f ON        f.id = l.fund_id LEFT JOIN m_code_value purp ON        purp.id = l.loanpurpose_cv_id WHERE     o.id = ''${officeId}'' AND       ( l.currency_code = ''${currencyId}'' OR        ''-1'' = ''${currencyId}'') AND       ( l.product_id = ''${loanProductId}'' OR        ''-1'' = ''${loanProductId}'') AND       ( Coalesce(l.loan_officer_id, -10) = ''${loanOfficerId}'' OR        ''-1'' = ''${loanOfficerId}'') AND       ( coalesce(l.fund_id, -10) = ''${fundId}'' OR        -1 = ''${fundId}'') AND       ( coalesce(l.loanpurpose_cv_id, -10) = ''${loanPurposeId}'' OR        -1 = ''${loanPurposeId}'') AND       l.loan_status_id = 200 GROUP BY  ounder.hierarchy, ounder.name, cur.display_symbol, pl.name, l.currency_code, l.expected_disbursedon_date ORDER BY  ounder.hierarchy, pl.name, l.currency_code, extract(year from l.expected_disbursedon_date), to_char(l.expected_disbursedon_date, ''Month'')', 'Individual Client Report', 't', 't', 'f');
INSERT INTO public.stretchy_report (id, report_name, report_type, report_subtype, report_category, report_sql, description, core_report, use_report, self_service_user_report) VALUES ('8', 'Loans Pending Approval', 'Table', NULL, 'Loan', 'SELECT    Concat(REPEAT(''..'', ((Length(ounder.hierarchy) - Length(REPLACE(ounder.hierarchy, ''.'', '''')) - 1))), ounder.name) AS "Office/Branch", c.account_no AS "Client Account No.", c.display_name AS "Client Name", Coalesce(cur.display_symbol, l.currency_code)  AS currency, pl.name  AS "Product", l.account_no AS "Loan Account No.", l.principal_amount AS "Loan Amount", l.term_frequency AS "Term Frequency", tf.enum_message_property AS "Term Frequency Period", l.annual_nominal_interest_rate AS " Annual Nominal Interest Rate", Extract(day FROM (CURRENT_DATE - l.submittedon_date::TIMESTAMP)) AS "Days Pending Approval", purp.code_value AS "Loan Purpose", lo.display_name AS "Loan Officer" FROM      m_office o JOIN      m_office ounder ON        ounder.hierarchy LIKE concat(o.hierarchy, ''%'') AND       ounder.hierarchy LIKE concat(''${currentUserHierarchy}'', ''%'') JOIN      m_client c ON        c.office_id = ounder.id JOIN      m_loan l ON        l.client_id = c.id JOIN      m_product_loan pl ON        pl.id = l.product_id LEFT JOIN m_staff lo ON        lo.id = l.loan_officer_id LEFT JOIN m_currency cur ON        cur.code = l.currency_code LEFT JOIN m_code_value purp ON        purp.id = l.loanpurpose_cv_id LEFT JOIN r_enum_value tf ON        tf.enum_name = ''term_period_frequency_enum'' AND       tf.enum_id = l.term_period_frequency_enum WHERE     o.id = ''${officeId}'' AND       ( l.currency_code = ''${currencyId}'' OR        ''-1'' = ''${currencyId}'') AND       ( l.product_id = ''${loanProductId}'' OR        ''-1'' = ''${loanProductId}'') AND       ( coalesce(l.loan_officer_id, -10) = ''${loanOfficerId}'' OR        ''-1'' = ''${loanOfficerId}'') AND       ( coalesce(l.loanpurpose_cv_id, -10) = ''${loanPurposeId}'' OR        -1 = ''${loanPurposeId}'') AND       l.loan_status_id = 100 ORDER BY  ounder.hierarchy, l.submittedon_date, l.account_no', 'Individual Client Report', 't', 't', 'f');
INSERT INTO public.stretchy_report (id, report_name, report_type, report_subtype, report_category, report_sql, description, core_report, use_report, self_service_user_report) VALUES ('11', 'Active Loans - Summary', 'Table', NULL, 'Loan', 'SELECT   Concat(REPEAT(''..'', ((Length(mo.hierarchy) - Length(REPLACE(mo.hierarchy, ''.'', '''')) - 1))), mo.name) AS "Office/Branch", x.currency AS currency, x.client_count AS "No. of Clients", x.active_loan_count  AS "No. Active                    Loans", x. loans_in_arrears_count  AS "No. of Loans in                    Arrears", x.principal  AS "Total Loans Disbursed", x.principal_repaid AS "Principal Repaid", x.principal_outstanding  AS "Principal Outstanding", x.principal_overdue  AS "Principal Overdue", x.interest AS "Total Interest", x.interest_repaid  AS "Interest Repaid", x.interest_outstanding AS "Interest Outstanding", x.interest_overdue AS "Interest Overdue", x.fees AS "Total Fees", x.fees_repaid  AS "Fees Repaid", x.fees_outstanding AS "Fees Outstanding", x.fees_overdue AS "Fees Overdue", x.penalties  AS "Total Penalties", x.penalties_repaid AS "Penalties Repaid", x.penalties_outstanding  AS "Penalties Outstanding", x.penalties_overdue  AS "Penalties Overdue", ( CASE WHEN ${parType} = 1 THEN cast(round((x.principal_overdue * 100) / x.principal_outstanding, 2) AS                                                                                                                                     CHAR) WHEN ${parType} = 2 THEN cast(round(((x.principal_overdue + x.interest_overdue) * 100) / (x.principal_outstanding + x.interest_outstanding), 2) AS                                                                                   CHAR) WHEN ${parType} = 3 THEN cast(round(((x.principal_overdue + x.interest_overdue + x.fees_overdue) * 100) / (x.principal_outstanding + x.interest_outstanding + x.fees_outstanding), 2) AS                                             CHAR) WHEN ${parType} = 4 THEN cast(round(((x.principal_overdue + x.interest_overdue + x.fees_overdue + x.penalties_overdue) * 100) / (x.principal_outstanding + x.interest_outstanding + x.fees_outstanding + x.penalties_overdue), 2) AS CHAR) ELSE ''invalid PAR Type'' end) AS "Portfolio at Risk %" FROM     m_office mo JOIN ( SELECT    ounder.id AS branch, coalesce(cur.display_symbol, l.currency_code) AS currency, count(DISTINCT(c.id)) AS client_count, count(DISTINCT(l.id)) AS active_loan_count, count(DISTINCT(coalesce(laa.loan_id, l.id, NULL) )) AS loans_in_arrears_count, sum(l.principal_disbursed_derived)  AS principal, sum(l.principal_repaid_derived) AS principal_repaid, sum(l.principal_outstanding_derived)  AS principal_outstanding, sum(laa.principal_overdue_derived)  AS principal_overdue, sum(l.interest_charged_derived) AS interest, sum(l.interest_repaid_derived)  AS interest_repaid, sum(l.interest_outstanding_derived) AS interest_outstanding, sum(laa.interest_overdue_derived) AS interest_overdue, sum(l.fee_charges_charged_derived)  AS fees, sum(l.fee_charges_repaid_derived) AS fees_repaid, sum(l.fee_charges_outstanding_derived)  AS fees_outstanding, sum(laa.fee_charges_overdue_derived)  AS fees_overdue, sum(l.penalty_charges_charged_derived)  AS penalties, sum(l.penalty_charges_repaid_derived) AS penalties_repaid, sum(l.penalty_charges_outstanding_derived)  AS penalties_outstanding, sum(laa.penalty_charges_overdue_derived)  AS penalties_overdue FROM      m_office o JOIN      m_office ounder ON        ounder.hierarchy LIKE concat(o.hierarchy, ''%'') AND       ounder.hierarchy LIKE concat(''${currentUserHierarchy}'', ''%'') JOIN      m_client c ON        c.office_id = ounder.id JOIN      m_loan l ON        l.client_id = c.id LEFT JOIN m_loan_arrears_aging laa ON        laa.loan_id = l.id LEFT JOIN m_currency cur ON        cur.code = l.currency_code WHERE     o.id = ''${officeId}'' AND       ( l.currency_code = ''${currencyId}'' OR        ''-1'' = ''${currencyId}'') AND       ( l.product_id = ''${loanProductId}'' OR        ''-1'' = ''${loanProductId}'') AND       ( coalesce(l.loan_officer_id, -10) = ${loanOfficerId} OR        ''-1'' = ${loanOfficerId}) AND       ( coalesce(l.fund_id, -10) = ${fundId} OR        -1 = ${fundId}) AND       ( coalesce(l.loanpurpose_cv_id, -10) = ${loanPurposeId} OR        -1 = ${loanPurposeId}) AND       l.loan_status_id = 300 GROUP BY  ounder.id, l.currency_code, cur.display_symbol) x ON       x.branch = mo.id ORDER BY mo.hierarchy, x.currency', NULL, 't', 't', 'f');
INSERT INTO public.stretchy_report (id, report_name, report_type, report_subtype, report_category, report_sql, description, core_report, use_report, self_service_user_report) VALUES ('12', 'Active Loans - Details', 'Table', NULL, 'Loan', 'SELECT    Concat(REPEAT(''..'', ((Length(ounder.hierarchy) - Length(REPLACE(ounder.hierarchy, ''.'', '''')) - 1))), ounder.name) AS "Office/Branch", Coalesce(cur.display_symbol, l.currency_code)  AS currency, lo.display_name  AS "Loan Officer", c.display_name AS "Client", l.account_no AS "Loan Account No.", pl.name  AS "Product", f.name AS fund, l.principal_amount AS "Loan Amount", l.annual_nominal_interest_rate AS "Annual Nominal Interest Rate", DATE_TRUNC(''day'', l.disbursedon_date)  AS "Disbursed Date", DATE_TRUNC(''day'', l.expected_maturedon_date) AS "Expected Matured On", l.principal_repaid_derived AS "Principal Repaid", l.principal_outstanding_derived  AS "Principal Outstanding", laa.principal_overdue_derived  AS "Principal Overdue", l.interest_repaid_derived  AS "Interest Repaid", l.interest_outstanding_derived AS "Interest Outstanding", laa.interest_overdue_derived AS "Interest Overdue", l.fee_charges_repaid_derived AS "Fees Repaid", l.fee_charges_outstanding_derived  AS "Fees Outstanding", laa.fee_charges_overdue_derived  AS "Fees Overdue", l.penalty_charges_repaid_derived AS "Penalties Repaid", l.penalty_charges_outstanding_derived  AS "Penalties Outstanding", penalty_charges_overdue_derived  AS "Penalties Overdue" FROM      m_office o JOIN      m_office ounder ON        ounder.hierarchy LIKE Concat(o.hierarchy, ''%'') AND       ounder.hierarchy LIKE Concat(''${currentUserHierarchy}'', ''%'') JOIN      m_client c ON        c.office_id = ounder.id JOIN      m_loan l ON        l.client_id = c.id JOIN      m_product_loan pl ON        pl.id = l.product_id LEFT JOIN m_staff lo ON        lo.id = l.loan_officer_id LEFT JOIN m_currency cur ON        cur.code = l.currency_code LEFT JOIN m_fund f ON        f.id = l.fund_id LEFT JOIN m_loan_arrears_aging laa ON        laa.loan_id = l.id WHERE     o.id = ''${officeId}'' AND       ( l.currency_code = ''${currencyId}'' OR        ''-1'' = ''${currencyId}'') AND       ( l.product_id = ''${loanProductId}'' OR        ''-1'' = ''${loanProductId}'') AND       ( Coalesce(l.loan_officer_id, -10) = ${loanOfficerId} OR        ''-1'' = ${loanOfficerId}) AND       ( coalesce(l.fund_id, -10) = ${fundId} OR        -1 = ${fundId}) AND       ( coalesce(l.loanpurpose_cv_id, -10) = ${loanPurposeId} OR        -1 = ${loanPurposeId}) AND       l.loan_status_id = 300 GROUP BY  l.id, ounder.hierarchy , ounder.name, cur.display_symbol, lo.display_name, c.display_name, pl.name, f.name, laa.principal_overdue_derived, laa.interest_overdue_derived, laa.fee_charges_overdue_derived, laa.penalty_charges_overdue_derived , c.account_no ORDER BY  ounder.hierarchy, l.currency_code, c.account_no, l.account_no', 'Individual Client   Report', 't', 't', 'f');
INSERT INTO public.stretchy_report (id, report_name, report_type, report_subtype, report_category, report_sql, description, core_report, use_report, self_service_user_report) VALUES ('13', 'Obligation Met Loans Details', 'Table', NULL, 'Loan', 'SELECT    Concat(REPEAT(''..'', ((Length(ounder.hierarchy ) - Length(REPLACE(ounder.hierarchy, ''.'', '''')) - 1))), ounder.name) AS "Office/Branch", Coalesce(cur.display_symbol, l.currency_code) AS currency, c.account_no  AS "Client Account No.", c.display_name  AS "Client", l.account_no  AS "Loan Account No.", pl.name AS "Product", f.name  AS fund, l.principal_amount  AS "Loan Amount", l.total_repayment_derived AS "Total Repaid", l.annual_nominal_interest_rate  AS "Annual Nominal Interest Rate", DATE_TRUNC(''day'', l.disbursedon_date) AS "Disbursed", DATE_TRUNC(''day'', l.closedon_date)  AS "Closed", l.principal_repaid_derived  AS "Principal Repaid", l.interest_repaid_derived AS "Interest Repaid", l.fee_charges_repaid_derived  AS "Fees Repaid", l.penalty_charges_repaid_derived  AS "Penalties Repaid", lo.display_name AS "Loan Officer" FROM      m_office o JOIN      m_office ounder ON        ounder.hierarchy LIKE Concat(o.hierarchy, ''%'') AND       ounder.hierarchy LIKE Concat(''${currentUserHierarchy}'', ''%'') JOIN      m_client c ON        c.office_id = ounder.id JOIN      m_loan l ON        l.client_id = c.id JOIN      m_product_loan pl ON        pl.id = l.product_id LEFT JOIN m_staff lo ON        lo.id = l.loan_officer_id LEFT JOIN m_currency cur ON        cur.code = l.currency_code LEFT JOIN m_fund f ON        f.id = l.fund_id WHERE     o.id = ''${officeId}'' AND       ( l.currency_code = ''${currencyId}'' OR        ''-1'' = ''${currencyId}'') AND       ( l.product_id = ''${loanProductId}'' OR        ''-1'' = ''${loanProductId}'') AND       ( Coalesce(l.loan_officer_id, -10) = ${loanOfficerId} OR        ''-1'' = ${loanOfficerId}) AND       ( coalesce(l.fund_id, -10) = ${fundId} OR        -1 = ${fundId}) AND       ( coalesce(l.loanpurpose_cv_id, -10) = ${loanPurposeId} OR        -1 = ${loanPurposeId}) AND       ( CASE WHEN ${obligDateType} = 1 THEN l.closedon_date BETWEEN ''${startDate}'' AND ''${endDate}'' WHEN ${obligDateType} = 2 THEN l.disbursedon_date BETWEEN ''${startDate}'' AND ''${endDate}'' ELSE 1 = 1 end) AND       l.loan_status_id = 600 GROUP BY  l.id, ounder.hierarchy, ounder.name, cur.display_symbol, c.account_no, c.display_name, pl.name, f.name, lo.display_name ORDER BY  ounder.hierarchy, l.currency_code, c.account_no, l.account_no', 'Individual Client   Report', 't', 't', 'f');
INSERT INTO public.stretchy_report (id, report_name, report_type, report_subtype, report_category, report_sql, description, core_report, use_report, self_service_user_report) VALUES ('14', 'Obligation Met Loans Summary', 'Table', NULL, 'Loan', 'SELECT    Concat(REPEAT(''..'', ((Length(ounder.hierarchy ) - Length(REPLACE(ounder.hierarchy, ''.'', '''')) - 1))), ounder.name) AS "Office/Branch", Coalesce(cur.display_symbol, l.currency_code) AS currency, Count(DISTINCT(c.id)) AS "No. of Clients", Count(DISTINCT(l.id)) AS "No. of Loans", Sum(l.principal_amount) AS "Total Loan Amount", Sum(l.principal_repaid_derived) AS "Total Principal Repaid", Sum(l.interest_repaid_derived)  AS "Total Interest Repaid", Sum(l.fee_charges_repaid_derived) AS "Total Fees Repaid", Sum(l.penalty_charges_repaid_derived) AS "Total Penalties Repaid", Sum(l.interest_waived_derived)  AS "Total Interest Waived", Sum(l.fee_charges_waived_derived) AS "Total Fees Waived", Sum(l.penalty_charges_waived_derived) AS "Total Penalties Waived" FROM      m_office o JOIN      m_office ounder ON        ounder.hierarchy LIKE concat(o.hierarchy, ''%'') AND       ounder.hierarchy LIKE concat(''${currentUserHierarchy}'', ''%'') JOIN      m_client c ON        c.office_id = ounder.id JOIN      m_loan l ON        l.client_id = c.id JOIN      m_product_loan pl ON        pl.id = l.product_id LEFT JOIN m_staff lo ON        lo.id = l.loan_officer_id LEFT JOIN m_currency cur ON        cur.code = l.currency_code LEFT JOIN m_fund f ON        f.id = l.fund_id WHERE     o.id = ''${officeId}'' AND       ( l.currency_code = ''${currencyId}'' OR        ''-1'' = ''${currencyId}'') AND       ( l.product_id = ''${loanProductId}'' OR        ''-1'' = ''${loanProductId}'') AND       ( coalesce(l.loan_officer_id, -10) = ${loanOfficerId} OR        ''-1'' = ${loanOfficerId}) AND       ( coalesce(l.fund_id, -10) = ${fundId} OR        -1 = ${fundId}) AND       ( coalesce(l.loanpurpose_cv_id, -10) = ${loanPurposeId} OR        -1 = ${loanPurposeId}) AND       ( CASE WHEN ${obligDateType} = 1 THEN l.closedon_date BETWEEN ''${startDate}'' AND ''${endDate}'' WHEN ${obligDateType} = 2 THEN l.disbursedon_date BETWEEN ''${startDate}'' AND ''${endDate}'' ELSE 1 = 1 end) AND       l.loan_status_id = 600 GROUP BY  ounder.hierarchy, l.currency_code, ounder.name, cur.display_symbol ORDER BY  ounder.hierarchy, l.currency_code', 'Individual Client   Report', 't', 't', 'f');
INSERT INTO public.stretchy_report (id, report_name, report_type, report_subtype, report_category, report_sql, description, core_report, use_report, self_service_user_report) VALUES ('15', 'Portfolio at Risk', 'Table', NULL, 'Loan', 'SELECT x."Currency", x."Principal Outstanding", x."Principal Overdue", x."Interest Outstanding", x."Interest Overdue", x."Fees Outstanding", x."Fees Overdue", x."Penalties Outstanding", x."Penalties Overdue", ( CASE WHEN ${parType} = 1 THEN cast(round((x."Principal Overdue" * 100) / x."Principal Outstanding", 2) AS CHAR) WHEN ${parType} = 2 THEN cast(round(((x."Principal Overdue" + x."Interest Overdue") * 100) / (x."Principal Outstanding" + x."Interest Outstanding"), 2) AS CHAR) WHEN ${parType} = 3 THEN cast(round(((x."Principal Overdue" + x."Interest Overdue" + x."Fees Overdue") * 100) / (x."Principal Outstanding" + x."Interest Outstanding" + x."Fees Outstanding"), 2) AS CHAR) WHEN ${parType} = 4 THEN cast(round(((x."Principal Overdue" + x."Interest Overdue" + x."Fees Overdue" + x."Penalties Overdue") * 100) / (x."Principal Outstanding" + x."Interest Outstanding" + x."Fees Outstanding" + x."Penalties Overdue"), 2) AS CHAR) ELSE ''invalid PAR Type'' end) AS "Portfolio at Risk %" FROM   ( SELECT    coalesce(cur.display_symbol, l.currency_code) AS "Currency", sum(l.principal_outstanding_derived)  AS "Principal Outstanding", sum(laa.principal_overdue_derived)  AS "Principal Overdue", sum(l.interest_outstanding_derived) AS "Interest Outstanding", sum(laa.interest_overdue_derived) AS "Interest Overdue", sum(l.fee_charges_outstanding_derived)  AS "Fees Outstanding", sum(laa.fee_charges_overdue_derived)  AS "Fees Overdue", sum(penalty_charges_outstanding_derived)  AS "Penalties Outstanding", sum(laa.penalty_charges_overdue_derived)  AS "Penalties Overdue" FROM      m_office o JOIN      m_office ounder ON        ounder.hierarchy LIKE concat(o.hierarchy, ''%'') AND       ounder.hierarchy LIKE concat(''${currentUserHierarchy}'', ''%'') JOIN      m_client c ON        c.office_id = ounder.id JOIN      m_loan l ON        l.client_id = c.id LEFT JOIN m_staff lo ON        lo.id = l.loan_officer_id LEFT JOIN m_currency cur ON        cur.code = l.currency_code LEFT JOIN m_fund f ON        f.id = l.fund_id LEFT JOIN m_code_value purp ON        purp.id = l.loanpurpose_cv_id LEFT JOIN m_product_loan p ON        p.id = l.product_id LEFT JOIN m_loan_arrears_aging laa ON        laa.loan_id = l.id WHERE     o.id = ''${officeId}'' AND       ( l.currency_code = ''${currencyId}'' OR        ''-1'' = ''${currencyId}'') AND       ( l.product_id = ''${loanProductId}'' OR        ''-1'' = ''${loanProductId}'') AND       ( coalesce(l.loan_officer_id, -10) = ${loanOfficerId} OR        ''-1'' = ${loanOfficerId}) AND       ( coalesce(l.fund_id, -10) = ${fundId} OR        -1 = ${fundId}) and (coalesce(l.loanpurpose_cv_id, -10) = ${loanPurposeId} OR        -1 = ${loanPurposeId}) AND       l.loan_status_id = 300 GROUP BY  l.currency_code, cur.display_symbol ORDER BY  l.currency_code) x', 'Covers all loans.    For larger MFIs … we should add some derived fields on loan (or a 1:1 loan related table like mifos 2.x does)  Principle, Interest, Fees, Penalties Outstanding and Overdue (possibly waived and written off too)', 't', 't', 'f');
INSERT INTO public.stretchy_report (id, report_name, report_type, report_subtype, report_category, report_sql, description, core_report, use_report, self_service_user_report) VALUES ('16', 'Portfolio at Risk by Branch', 'Table', NULL, 'Loan', 'SELECT   Concat(REPEAT(''..'', ((Length(mo.hierarchy ) - Length(REPLACE(mo.hierarchy, ''.'', '''')) - 1))), mo.name) AS "Office/Branch", x."Currency", x."Principal Outstanding" , x."Principal Overdue", x."Interest Outstanding", x."Interest Overdue", x."Fees Outstanding", x."Fees Overdue", x."Penalties Outstanding", x."Penalties Overdue", ( CASE WHEN ${parType} = 1 THEN cast(round((x."Principal Overdue" * 100) / x."Principal Outstanding", 2) AS  CHAR) WHEN ${parType} = 2 THEN cast(round(((x."Principal Overdue" + x."Interest Overdue") * 100) / (x."Principal Outstanding" + x."Interest Outstanding"), 2) AS      CHAR) WHEN ${parType} = 3 THEN cast(round(((x."Principal Overdue" + x."Interest Overdue" + x."Fees Overdue") * 100) / (x."Principal Outstanding" + x."Interest Outstanding" + x."Fees Outstanding"), 2) AS CHAR) WHEN ${parType} = 4 THEN cast(round(((x."Principal Overdue" + x."Interest Overdue" + x."Fees Overdue" + x."Penalties Overdue") * 100) / (x."Principal Outstanding" + x."Interest Outstanding" + x."Fees Outstanding" + x."Penalties Overdue"), 2) AS           CHAR) ELSE ''invalid PAR Type'' end) AS "Portfolio at Risk %" FROM     m_office mo JOIN ( SELECT    ounder.id  AS "branch", coalesce(cur.display_symbol, l.currency_code) AS "Currency", sum(l.principal_outstanding_derived)  AS "Principal Outstanding", sum(laa.principal_overdue_derived)  AS "Principal Overdue", sum(l.interest_outstanding_derived) AS "Interest Outstanding", sum(laa.interest_overdue_derived) AS "Interest Overdue", sum(l.fee_charges_outstanding_derived)  AS "Fees Outstanding", sum(laa.fee_charges_overdue_derived)  AS "Fees Overdue", sum(penalty_charges_outstanding_derived)  AS "Penalties Outstanding", sum(laa.penalty_charges_overdue_derived)  AS "Penalties Overdue" FROM      m_office o JOIN      m_office ounder ON        ounder.hierarchy LIKE concat(o.hierarchy, ''%'') AND       ounder.hierarchy LIKE concat(''${currentUserHierarchy}'', ''%'') JOIN      m_client c ON        c.office_id = ounder.id JOIN      m_loan l ON        l.client_id = c.id LEFT JOIN m_staff lo ON        lo.id = l.loan_officer_id LEFT JOIN m_currency cur ON        cur.code = l.currency_code LEFT JOIN m_fund f ON        f.id = l.fund_id LEFT JOIN m_code_value purp ON        purp.id = l.loanpurpose_cv_id LEFT JOIN m_product_loan p ON        p.id = l.product_id LEFT JOIN m_loan_arrears_aging laa ON        laa.loan_id = l.id WHERE     o.id = ''${officeId}'' AND       ( l.currency_code = ''${currencyId}'' OR        ''-1'' = ''${currencyId}'') AND       ( l.product_id = ''${loanProductId}'' OR        ''-1'' = ''${loanProductId}'') AND       ( coalesce(l.loan_officer_id, -10) = ${loanOfficerId} OR        ''-1'' = ${loanOfficerId}) AND       ( coalesce(l.fund_id, -10) = ${fundId} OR        -1 = ${fundId}) and (coalesce(l.loanpurpose_cv_id, -10) = ${loanPurposeId} OR        -1 = ${loanPurposeId}) AND       l.loan_status_id = 300 GROUP BY  ounder.id, l.currency_code, cur.display_symbol ) x ON       x.branch = mo.id ORDER BY mo.hierarchy, x."Currency"', 'Covers all loans.    For larger MFIs … we should add some derived fields on loan (or a 1:1 loan related table like mifos 2.x does)  Principle, Interest, Fees, Penalties Outstanding and Overdue (possibly waived and written off too)', 't', 't', 'f');
INSERT INTO public.stretchy_report (id, report_name, report_type, report_subtype, report_category, report_sql, description, core_report, use_report, self_service_user_report) VALUES ('20', 'Funds Disbursed Between Dates Summary', 'Table', NULL, 'Fund', 'SELECT    Coalesce(f.name, ''-'') AS fund, Coalesce(cur.display_symbol, l.currency_code) AS currency, Round(Sum(l.principal_amount), 4) AS disbursed_amount FROM      m_office ounder JOIN      m_client c ON        c.office_id = ounder.id JOIN      m_loan l ON        l.client_id = c.id JOIN      m_currency cur ON        cur.code = l.currency_code LEFT JOIN m_fund f ON        f.id = l.fund_id WHERE     disbursedon_date BETWEEN ''${startDate}'' AND ''${endDate}'' AND       ( coalesce(l.fund_id, -10) = ${fundId} OR        -1 = ${fundId}) AND       ( l.currency_code = ''${currencyId}'' OR        ''-1'' = ''${currencyId}'') AND       ounder.hierarchy LIKE concat(''${currentUserHierarchy}'', ''%'') GROUP BY  coalesce(f.name, ''-'') , coalesce(cur.display_symbol, l.currency_code) ORDER BY  coalesce(f.name, ''-'') , coalesce(cur.display_symbol, l.currency_code)', NULL, 't', 't', 'f');
INSERT INTO public.stretchy_report (id, report_name, report_type, report_subtype, report_category, report_sql, description, core_report, use_report, self_service_user_report) VALUES ('21', 'Funds Disbursed Between Dates Summary by Office', 'Table', NULL, 'Fund', 'SELECT    Concat(REPEAT(''..'', ((Length(ounder.hierarchy) - Length(REPLACE(ounder.hierarchy, ''.'', '''')) - 1))), ounder.name) AS "Office/Branch", Coalesce(f.name, ''-'')  AS fund, Coalesce(cur.display_symbol, l.currency_code)  AS currency, Round(Sum(l.principal_amount), 4)  AS disbursed_amount FROM      m_office o JOIN      m_office ounder ON        ounder.hierarchy LIKE Concat(o.hierarchy, ''%'') AND       ounder.hierarchy LIKE Concat(''${currentUserHierarchy}'', ''%'') JOIN      m_client c ON        c.office_id = ounder.id JOIN      m_loan l ON        l.client_id = c.id JOIN      m_currency cur ON        cur.code = l.currency_code LEFT JOIN m_fund f ON        f.id = l.fund_id WHERE     disbursedon_date BETWEEN ''${startDate}'' AND ''${endDate}'' AND       o.id = ''${officeId}'' AND       ( coalesce(l.fund_id, -10) = ${fundId} OR        -1 = ${fundId}) AND       ( l.currency_code = ''${currencyId}'' OR        ''-1'' = ''${currencyId}'') GROUP BY  ounder.name, coalesce(f.name, ''-'') , coalesce(cur.display_symbol, l.currency_code), ounder.hierarchy ORDER BY  ounder.name, coalesce(f.name, ''-'') , coalesce(cur.display_symbol, l.currency_code)', NULL, 't', 't', 'f');
INSERT INTO public.stretchy_report (id, report_name, report_type, report_subtype, report_category, report_sql, description, core_report, use_report, self_service_user_report) VALUES ('51', 'Written-Off Loans', 'Table', NULL, 'Loan', 'SELECT    Concat(REPEAT(''..'', ((Length(ounder.hierarchy) - Length(REPLACE(ounder.hierarchy, ''.'', '''')) - 1))), ounder.name) AS "Office/Branch", Coalesce(cur.display_symbol, ml.currency_code) AS currency, c.account_no AS "Client Account No.", c.display_name AS "Client Name", ml.account_no  AS "Loan Account No.", mpl.name AS "Product Name", ml.disbursedon_date  AS "Disbursed Date", lt.transaction_date  AS "Written Off date", ml.principal_amount  AS "Loan Amount", Coalesce(lt.principal_portion_derived, 0)  AS "Written-Off                    Principal", Coalesce(lt.interest_portion_derived, 0) AS "Written-Off Interest", Coalesce(lt.fee_charges_portion_derived,0) AS "Written-Off                    Fees", Coalesce(lt.penalty_charges_portion_derived,0) AS "Written-Off Penalties", n.note AS "Reason For Write-Off", Coalesce(ms.display_name,''-'')  AS "Loan Officer Name" FROM      m_office o JOIN      m_office ounder ON        ounder.hierarchy LIKE Concat(o.hierarchy, ''%'') AND       ounder.hierarchy LIKE Concat(''${currentUserHierarchy}'', ''%'') JOIN      m_client c ON        c.office_id = ounder.id JOIN      m_loan ml ON        ml.client_id = c.id JOIN      m_product_loan mpl ON        mpl.id=ml.product_id LEFT JOIN m_staff ms ON        ms.id=ml.loan_officer_id JOIN      m_loan_transaction lt ON        lt.loan_id = ml.id LEFT JOIN m_note n ON        n.loan_transaction_id = lt.id LEFT JOIN m_currency cur ON        cur.code = ml.currency_code WHERE     lt.transaction_type_enum = 6 /*write-off */ AND       lt.is_reversed IS FALSE AND       ml.loan_status_id=601 AND       o.id=''${officeId}'' AND       ( mpl.id=''${loanProductId}'' OR        ''${loanProductId}''=-1) AND       lt.transaction_date BETWEEN ''${startDate}'' AND ''${endDate}'' AND       ( ml.currency_code = ''${currencyId}'' OR        ''-1'' = ''${currencyId}'') ORDER BY  ounder.hierarchy, coalesce(cur.display_symbol, ml.currency_code), ml.account_no', 'Individual Lending Report. Written Off Loans', 't', 't', 'f');
INSERT INTO public.stretchy_report (id, report_name, report_type, report_subtype, report_category, report_sql, description, core_report, use_report, self_service_user_report) VALUES ('52', 'Aging Detail', 'Table', NULL, 'Loan', 'SELECT     Concat(Repeat(''..'', ((Length(ounder.hierarchy) - Length(Replace(ounder.hierarchy , ''.'', '''')) - 1))), ounder.NAME) AS "Office/Branch", COALESCE(cur.display_symbol, ml.currency_code)  AS currency, mc.account_no AS "Client Account No.", mc.display_name  AS "Client Name", ml.account_no  AS "Account Number", ml.principal_amount  AS "Loan Amount", ml.principal_disbursed_derived  AS "Original Principal", ml.interest_charged_derived AS "Original Interest", ml.principal_repaid_derived AS "Principal Paid", ml.interest_repaid_derived  AS "Interest Paid", laa.principal_overdue_derived AS "Principal Overdue", laa.interest_overdue_derived  AS "Interest Overdue", Extract(day FROM (CURRENT_DATE::timestamp - laa.overdue_since_date_derived::timestamp))  AS "Days in Arrears", CASE WHEN Extract(day FROM (CURRENT_DATE::timestamp - laa.overdue_since_date_derived::timestamp))<7 THEN ''<1'' WHEN extract(day FROM (CURRENT_DATE::timestamp - laa.overdue_since_date_derived::timestamp))<8 THEN '' 1'' WHEN extract(day FROM (CURRENT_DATE::timestamp - laa.overdue_since_date_derived::timestamp))<15 THEN ''2'' WHEN extract(day FROM (CURRENT_DATE::timestamp - laa.overdue_since_date_derived::timestamp))<22 THEN '' 3'' WHEN extract(day FROM (CURRENT_DATE::timestamp - laa.overdue_since_date_derived::timestamp))<29 THEN '' 4'' WHEN extract(day FROM (CURRENT_DATE::timestamp - laa.overdue_since_date_derived::timestamp))<36 THEN '' 5'' WHEN extract(day FROM (CURRENT_DATE::timestamp - laa.overdue_since_date_derived::timestamp))<43 THEN '' 6'' WHEN extract(day FROM (CURRENT_DATE::timestamp - laa.overdue_since_date_derived::timestamp))<50 THEN '' 7'' WHEN extract(day FROM (CURRENT_DATE::timestamp - laa.overdue_since_date_derived::timestamp))<57 THEN '' 8'' WHEN extract(day FROM (CURRENT_DATE::timestamp - laa.overdue_since_date_derived::timestamp))<64 THEN '' 9'' WHEN extract(day FROM (CURRENT_DATE::timestamp - laa.overdue_since_date_derived::timestamp))<71 THEN ''10'' WHEN extract(day FROM (CURRENT_DATE::timestamp - laa.overdue_since_date_derived::timestamp))<78 THEN ''11'' WHEN extract(day FROM (CURRENT_DATE::timestamp - laa.overdue_since_date_derived::timestamp))<85 THEN ''12'' ELSE ''12+'' END AS "Weeks In Arrears Band", CASE WHEN extract(day FROM (CURRENT_DATE::timestamp - laa.overdue_since_date_derived::timestamp))<31 THEN ''0 - 30'' WHEN extract(day FROM (CURRENT_DATE::timestamp - laa.overdue_since_date_derived::timestamp))<61 THEN ''30 - 60'' WHEN extract(day FROM (CURRENT_DATE::timestamp - laa.overdue_since_date_derived::timestamp))<91 THEN ''60 - 90'' WHEN extract(day FROM (CURRENT_DATE::timestamp - laa.overdue_since_date_derived::timestamp))<181 THEN ''90 - 180'' WHEN extract(day FROM (CURRENT_DATE::timestamp - laa.overdue_since_date_derived::timestamp))<361 THEN ''180 - 360'' ELSE ''> 360'' END AS "Days in Arrears Band" FROM       m_office mo JOIN       m_office ounder ON         ounder.hierarchy LIKE concat(mo.hierarchy, ''%'') AND        ounder.hierarchy LIKE concat(''${currentUserHierarchy}'', ''%'') INNER JOIN m_client mc ON         mc.office_id=ounder.id INNER JOIN m_loan ml ON         ml.client_id = mc.id INNER JOIN r_enum_value rev ON         rev.enum_id=ml.loan_status_id AND        rev.enum_name = ''loan_status_id'' INNER JOIN m_loan_arrears_aging laa ON         laa.loan_id=ml.id LEFT JOIN  m_currency cur ON         cur.code = ml.currency_code WHERE      ml.loan_status_id=300 AND        mo.id=''${officeId}'' GROUP BY   ounder.hierarchy, ounder.name, cur.display_symbol, ml.currency_code, mc.account_no, mc.display_name, ml.account_no, ml.principal_amount, ml.principal_disbursed_derived, ml.interest_charged_derived, ml.principal_repaid_derived, ml.interest_repaid_derived, laa.principal_overdue_derived, laa.interest_overdue_derived, laa.overdue_since_date_derived ORDER BY   ounder.hierarchy, COALESCE(cur.display_symbol, ml.currency_code), ml.account_no', 'Loan arrears aging (Weeks)', 't', 't', 'f');
INSERT INTO public.stretchy_report (id, report_name, report_type, report_subtype, report_category, report_sql, description, core_report, use_report, self_service_user_report) VALUES ('113', 'ProgramDirectorStats', 'Table', NULL, 'Quipo', 'SELECT    Coalesce(cur.display_symbol, l.currency_code) AS currency, /*This query will return more than one entry if more than one currency is used */ Count(DISTINCT(c.id))  AS activeclients, Count(*) AS activeloans, Sum(l.principal_disbursed_derived) AS disbursedamount, Sum(l.principal_outstanding_derived) AS loanoutstandingamount, Round((Sum(l.principal_outstanding_derived) * 100) / Sum(l.principal_disbursed_derived),2) AS loanoutstandingpc, Sum(Coalesce(lpa.principal_in_advance_derived,0.0))  AS loanpaidinadvance, sum( CASE WHEN ( CURRENT_DATE - 28 * INTERVAL ''1 day'') > coalesce(laa.overdue_since_date_derived, CURRENT_DATE) THEN l.principal_outstanding_derived ELSE 0 end) AS portfolioatrisk, round((sum( CASE WHEN ( CURRENT_DATE - 28 * INTERVAL ''1 day'' ) > coalesce(laa.overdue_since_date_derived, CURRENT_DATE) THEN l.principal_outstanding_derived ELSE 0 end) * 100) / sum(l.principal_outstanding_derived), 2) AS portfolioatriskpc, count(DISTINCT( CASE WHEN ( CURRENT_DATE - 28 * INTERVAL ''1 day'' ) > coalesce(laa.overdue_since_date_derived, CURRENT_DATE) THEN c.id ELSE NULL end)) AS clientsindefault, round((count(DISTINCT( CASE WHEN ( CURRENT_DATE - 28 * INTERVAL ''1 day'' ) > coalesce(laa.overdue_since_date_derived, CURRENT_DATE) THEN c.id ELSE NULL end)) * 100) / count(DISTINCT(c.id)), 2)  AS clientsindefaultpc, (sum(l.principal_disbursed_derived) / count(*)) AS averageloanamount FROM      m_staff pd JOIN      m_staff bm ON        bm.organisational_role_parent_staff_id = pd.id JOIN      m_staff coord ON        coord.organisational_role_parent_staff_id = bm.id JOIN      m_staff fa ON        fa.organisational_role_parent_staff_id = coord.id JOIN      m_office o ON        o.id = fa.office_id AND       o.hierarchy LIKE concat(''${currentUserHierarchy}'', ''%'') JOIN      m_group pgm ON        pgm.staff_id = fa.id JOIN      m_loan l ON        l.group_id = pgm.id AND       l.client_id IS NOT NULL LEFT JOIN m_currency cur ON        cur.code = l.currency_code LEFT JOIN m_loan_arrears_aging laa ON        laa.loan_id = l.id LEFT JOIN m_loan_paid_in_advance lpa ON        lpa.loan_id = l.id JOIN      m_client c ON        c.id = l.client_id WHERE     pd.id = ${staffId} AND       l.loan_status_id = 300 GROUP BY  l.currency_code, cur.display_symbol', 'Program DirectorStatistics', 'f', 'f', 'f');
INSERT INTO public.stretchy_report (id, report_name, report_type, report_subtype, report_category, report_sql, description, core_report, use_report, self_service_user_report) VALUES ('53', 'Aging Summary (Arrears in Weeks)', 'Table', NULL, 'Loan', 'SELECT    Coalesce(periods.currencyname, periods.currency) AS currency, periods.period_no AS "Weeks In Arrears (Up To)", coalesce(ars.loanid, 0) AS "No Of Loans", coalesce(ars.principal,0.0) AS "Original Principal", coalesce(ars.interest,0.0) AS "Original Interest", coalesce(ars.prinpaid,0.0) AS "Principal Paid", coalesce(ars.intpaid,0.0) AS "Interest Paid", coalesce(ars.prinoverdue,0.0) AS "Principal Overdue", coalesce(ars.intoverdue,0.0) AS "Interest Overdue" FROM      ( SELECT curs.code AS currency, curs.name AS currencyname, pers.* FROM   ( SELECT ''On Schedule'' period_no, 1             pid UNION SELECT ''1'', 2 UNION SELECT ''2'', 3 UNION SELECT ''3'', 4 UNION SELECT ''4'', 5 UNION SELECT ''5'', 6 UNION SELECT ''6'', 7 UNION SELECT ''7'', 8 UNION SELECT ''8'', 9 UNION SELECT ''9'', 10 UNION SELECT ''10'', 11 UNION SELECT ''11'', 12 UNION SELECT ''12'', 13 UNION SELECT ''12+'', 14) pers, ( SELECT     DISTINCT ON (moc.code) moc.code, moc.name FROM       m_office mo2 INNER JOIN m_office ounder2 ON         ounder2.hierarchy LIKE concat(mo2.hierarchy, ''%'') AND        ounder2.hierarchy LIKE concat(''${currentUserHierarchy}'', ''%'') INNER JOIN m_client mc2 ON         mc2.office_id=ounder2.id INNER JOIN m_loan ml2 ON         ml2.client_id = mc2.id INNER JOIN m_organisation_currency moc ON         moc.code = ml2.currency_code WHERE      ml2.loan_status_id=300 /* active */ AND        mo2.id=''${officeId}'' AND        ( ml2.currency_code = ''${currencyId}'' OR         ''-1'' = ''${currencyId}'') GROUP BY moc.code, moc.name) curs) periods LEFT JOIN ( SELECT   z.currency, z.arrperiod, count(z.loanid)  AS loanid, sum(z.principal) AS principal, sum(z.interest)  AS interest, sum(z.prinpaid)  AS prinpaid, sum(z.intpaid) AS intpaid, sum(z.prinoverdue) AS prinoverdue, sum(z.intoverdue)  AS intoverdue FROM     ( SELECT x.loanid, x.currency, x.principal, x.interest, x.prinpaid, x.intpaid, x.prinoverdue, x.intoverdue, CASE WHEN extract(day FROM (CURRENT_DATE::TIMESTAMP - MINOVERDUEDATE::TIMESTAMP))<1 THEN ''On Schedule'' WHEN extract(day FROM (CURRENT_DATE::TIMESTAMP - MINOVERDUEDATE::TIMESTAMP))<8 THEN ''1'' WHEN extract(day FROM (CURRENT_DATE::TIMESTAMP - MINOVERDUEDATE::TIMESTAMP))<15 THEN ''2'' WHEN extract(day FROM (CURRENT_DATE::TIMESTAMP - MINOVERDUEDATE::TIMESTAMP))<22 THEN ''3'' WHEN extract(day FROM (CURRENT_DATE::TIMESTAMP - MINOVERDUEDATE::TIMESTAMP))<29 THEN ''4'' WHEN extract(day FROM (CURRENT_DATE::TIMESTAMP - MINOVERDUEDATE::TIMESTAMP))<36 THEN ''5'' WHEN extract(day FROM (CURRENT_DATE::TIMESTAMP - MINOVERDUEDATE::TIMESTAMP))<43 THEN ''6'' WHEN extract(day FROM (CURRENT_DATE::TIMESTAMP - MINOVERDUEDATE::TIMESTAMP))<50 THEN ''7'' WHEN extract(day FROM (CURRENT_DATE::TIMESTAMP - MINOVERDUEDATE::TIMESTAMP))<57 THEN ''8'' WHEN extract(day FROM (CURRENT_DATE::TIMESTAMP - MINOVERDUEDATE::TIMESTAMP))<64 THEN ''9'' WHEN extract(day FROM (CURRENT_DATE::TIMESTAMP - MINOVERDUEDATE::TIMESTAMP))<71 THEN ''10'' WHEN extract(day FROM (CURRENT_DATE::TIMESTAMP - MINOVERDUEDATE::TIMESTAMP))<78 THEN ''11'' WHEN extract(day FROM (CURRENT_DATE::TIMESTAMP - MINOVERDUEDATE::TIMESTAMP))<85 THEN ''12'' ELSE ''12+'' end AS arrperiod FROM   ( SELECT     ml.id  AS loanid, ml.currency_code AS currency, ml.principal_disbursed_derived AS principal, ml.interest_charged_derived  AS interest, ml.principal_repaid_derived  AS prinpaid, ml.interest_repaid_derived  AS intpaid, laa.principal_overdue_derived  AS prinoverdue, laa.interest_overdue_derived AS intoverdue, coalesce(laa.overdue_since_date_derived, CURRENT_DATE) AS minoverduedate FROM       m_office mo INNER JOIN m_office ounder ON         ounder.hierarchy LIKE concat(mo.hierarchy, ''%'') AND        ounder.hierarchy LIKE concat(''${currentUserHierarchy}'', ''%'') INNER JOIN m_client mc ON         mc.office_id=ounder.id INNER JOIN m_loan ml ON         ml.client_id = mc.id LEFT JOIN  m_loan_arrears_aging laa ON         laa.loan_id = ml.id WHERE      ml.loan_status_id=300 AND        mo.id=''${officeId}'' AND        ( ml.currency_code = ''${currencyId}'' OR         ''-1'' = ''${currencyId}'') GROUP BY   ml.id, laa.principal_overdue_derived, laa.interest_overdue_derived, laa.overdue_since_date_derived) x ) z GROUP BY z.currency, z.arrperiod ) ars ON        ars.arrperiod=periods.period_no AND       ars.currency = periods.currency ORDER BY  periods.currency, periods.pid', 'Loan amount in arrears by branch', 't', 't', 'f');
INSERT INTO public.stretchy_report (id, report_name, report_type, report_subtype, report_category, report_sql, description, core_report, use_report, self_service_user_report) VALUES ('54', 'Rescheduled Loans', 'Table', NULL, 'Loan', 'SELECT    Concat(REPEAT(''..'', ((Length(ounder.hierarchy) - Length(REPLACE(ounder.hierarchy, ''.'', '''')) - 1))), ounder.name) AS "Office/Branch", Coalesce(cur.display_symbol, ml.currency_code) AS currency, c.account_no AS "Client Account No.", c.display_name AS "Client Name", ml.account_no  AS "Loan Account No.", mpl.name AS "Product Name", ml.disbursedon_date  AS "Disbursed Date", lt.transaction_date  AS "Written Off date", ml.principal_amount  AS "Loan Amount", Coalesce(lt.principal_portion_derived, 0)  AS "Rescheduled Principal", Coalesce(lt.interest_portion_derived, 0) AS "Rescheduled Interest", Coalesce(lt.fee_charges_portion_derived,0) AS "Rescheduled Fees", Coalesce(lt.penalty_charges_portion_derived,0) AS "Rescheduled Penalties", n.note AS "Reason For Rescheduling", Coalesce(ms.display_name,''-'')  AS "Loan Officer Name" FROM      m_office o JOIN      m_office ounder ON        ounder.hierarchy LIKE Concat(o.hierarchy, ''%'') AND       ounder.hierarchy LIKE Concat(''${currentUserHierarchy}'', ''%'') JOIN      m_client c ON        c.office_id = ounder.id JOIN      m_loan ml ON        ml.client_id = c.id JOIN      m_product_loan mpl ON        mpl.id=ml.product_id LEFT JOIN m_staff ms ON        ms.id=ml.loan_officer_id JOIN      m_loan_transaction lt ON        lt.loan_id = ml.id LEFT JOIN m_note n ON        n.loan_transaction_id = lt.id LEFT JOIN m_currency cur ON        cur.code = ml.currency_code WHERE     lt.transaction_type_enum = 7 /*marked for rescheduling */ AND       lt.is_reversed IS FALSE AND       ml.loan_status_id=602 AND       o.id=''${officeId}'' AND       ( mpl.id=''${loanProductId}'' OR        ''${loanProductId}''=-1) AND       lt.transaction_date BETWEEN ''${startDate}'' AND ''${endDate}'' AND       ( ml.currency_code = ''${currencyId}'' OR        ''-1'' = ''${currencyId}'') ORDER BY  ounder.hierarchy, coalesce(cur.display_symbol, ml.currency_code), ml.account_no', 'Individual Lending Report. Rescheduled Loans.  The ability to reschedule (or mark that you have rescheduled the loan elsewhere) is a legacy of the older Mifos product.  Needed for migration.', 't', 't', 'f');
INSERT INTO public.stretchy_report (id, report_name, report_type, report_subtype, report_category, report_sql, description, core_report, use_report, self_service_user_report) VALUES ('55', 'Active Loans Passed Final Maturity', 'Table', NULL, 'Loan', 'SELECT    Concat(REPEAT(''..'', ((Length(ounder.hierarchy) - Length(REPLACE(ounder.hierarchy, ''.'', '''')) - 1))), ounder.name) AS "Office/Branch", Coalesce(cur.display_symbol, l.currency_code)  AS currency, lo.display_name  AS "Loan Officer", c.display_name AS "Client", l.account_no AS "Loan Account No.", pl.name  AS "Product", f.name AS fund, l.principal_amount AS "Loan Amount", l.annual_nominal_interest_rate AS "Annual Nominal Interest Rate", DATE_TRUNC(''day'', l.disbursedon_date)  AS "Disbursed Date", DATE_TRUNC(''day'', l.expected_maturedon_date) AS "Expected Matured On", l.principal_repaid_derived AS "Principal Repaid", l.principal_outstanding_derived  AS "Principal Outstanding", laa.principal_overdue_derived  AS "Principal Overdue", l.interest_repaid_derived  AS "Interest Repaid", l.interest_outstanding_derived AS "Interest Outstanding", laa.interest_overdue_derived AS "Interest Overdue", l.fee_charges_repaid_derived AS "Fees Repaid", l.fee_charges_outstanding_derived  AS "Fees Outstanding", laa.fee_charges_overdue_derived  AS "Fees Overdue", l.penalty_charges_repaid_derived AS "Penalties Repaid", l.penalty_charges_outstanding_derived  AS "Penalties Outstanding", laa.penalty_charges_overdue_derived  AS "Penalties Overdue" FROM      m_office o JOIN      m_office ounder ON        ounder.hierarchy LIKE Concat(o.hierarchy, ''%'') AND       ounder.hierarchy LIKE Concat(''${currentUserHierarchy}'', ''%'') JOIN      m_client c ON        c.office_id = ounder.id JOIN      m_loan l ON        l.client_id = c.id JOIN      m_product_loan pl ON        pl.id = l.product_id LEFT JOIN m_staff lo ON        lo.id = l.loan_officer_id LEFT JOIN m_currency cur ON        cur.code = l.currency_code LEFT JOIN m_fund f ON        f.id = l.fund_id LEFT JOIN m_loan_arrears_aging laa ON        laa.loan_id = l.id WHERE     o.id = ''${officeId}'' AND       ( l.currency_code = ''${currencyId}'' OR        ''-1'' = ''${currencyId}'') AND       ( l.product_id = ''${loanProductId}'' OR        ''-1'' = ''${loanProductId}'') AND       ( Coalesce(l.loan_officer_id, -10) = ${loanOfficerId} OR        ''-1'' = ${loanOfficerId}) AND       ( coalesce(l.fund_id, -10) = ${fundId} OR        -1 = ${fundId}) AND       ( coalesce(l.loanpurpose_cv_id, -10) = ${loanPurposeId} OR        -1 = ${loanPurposeId}) AND       l.loan_status_id = 300 AND       l.expected_maturedon_date < CURRENT_DATE GROUP BY  l.id, ounder.hierarchy, ounder.name, cur.display_symbol, lo.display_name, c.display_name, f.name, pl.name, laa.principal_overdue_derived, laa.interest_overdue_derived, laa.fee_charges_overdue_derived, laa.penalty_charges_overdue_derived, c.account_no ORDER BY  ounder.hierarchy, l.currency_code, c.account_no, l.account_no', 'Individual Client   Report', 't', 't', 'f');
INSERT INTO public.stretchy_report (id, report_name, report_type, report_subtype, report_category, report_sql, description, core_report, use_report, self_service_user_report) VALUES ('56', 'Active Loans Passed Final Maturity Summary', 'Table', NULL, 'Loan', 'SELECT   Concat(REPEAT(''..'', ((Length(mo.hierarchy ) - Length(REPLACE(mo.hierarchy, ''.'', '''')) - 1))), mo.name) AS "Office/Branch", x.currency  AS currency, x.client_count  AS "No. of Clients", x.active_loan_count AS "No. Active                    Loans", x. arrears_loan_count AS "No. of Loans in Arrears", x.principal AS "Total Loans Disbursed", x.principal_repaid  AS "Principal Repaid", x.principal_outstanding AS "Principal Outstanding", x.principal_overdue AS "Principal Overdue", x.interest  AS "Total Interest", x.interest_repaid AS "Interest Repaid", x.interest_outstanding  AS "Interest Outstanding", x.interest_overdue  AS "Interest Overdue", x.fees  AS "Total Fees", x.fees_repaid AS "Fees Repaid", x.fees_outstanding  AS "Fees Outstanding", x.fees_overdue  AS "Fees Overdue", x.penalties AS "Total Penalties", x.penalties_repaid  AS "Penalties Repaid", x.penalties_outstanding AS "Penalties Outstanding", x.penalties_overdue AS "Penalties Overdue", ( CASE WHEN ${parType} = 1 THEN cast(round((x.principal_overdue * 100) / x.principal_outstanding, 2) AS                                                                                                                                     CHAR) WHEN ${parType} = 2 THEN cast(round(((x.principal_overdue + x.interest_overdue) * 100) / (x.principal_outstanding + x.interest_outstanding), 2) AS                                                                                   CHAR) WHEN ${parType} = 3 THEN cast(round(((x.principal_overdue + x.interest_overdue + x.fees_overdue) * 100) / (x.principal_outstanding + x.interest_outstanding + x.fees_outstanding), 2) AS                                             CHAR) WHEN ${parType} = 4 THEN cast(round(((x.principal_overdue + x.interest_overdue + x.fees_overdue + x.penalties_overdue) * 100) / (x.principal_outstanding + x.interest_outstanding + x.fees_outstanding + x.penalties_overdue), 2) AS CHAR) ELSE ''invalid PAR Type'' end) AS "Portfolio at Risk %" FROM     m_office mo JOIN ( SELECT    ounder.id  AS branch, coalesce(cur.display_symbol, l.currency_code)  AS currency, count(DISTINCT(c.id))  AS client_count, count(DISTINCT(l.id))  AS active_loan_count, count(DISTINCT(laa.loan_id) )  AS arrears_loan_count, sum(l.principal_disbursed_derived) AS principal, sum(l.principal_repaid_derived)  AS principal_repaid, sum(l.principal_outstanding_derived) AS principal_outstanding, sum(coalesce(laa.principal_overdue_derived,0)) AS principal_overdue, sum(l.interest_charged_derived)  AS interest, sum(l.interest_repaid_derived) AS interest_repaid, sum(l.interest_outstanding_derived)  AS interest_outstanding, sum(coalesce(laa.interest_overdue_derived,0))  AS interest_overdue, sum(l.fee_charges_charged_derived) AS fees, sum(l.fee_charges_repaid_derived)  AS fees_repaid, sum(l.fee_charges_outstanding_derived) AS fees_outstanding, sum(coalesce(laa.fee_charges_overdue_derived,0)) AS fees_overdue, sum(l.penalty_charges_charged_derived) AS penalties, sum(l.penalty_charges_repaid_derived)  AS penalties_repaid, sum(l.penalty_charges_outstanding_derived) AS penalties_outstanding, sum(coalesce(laa.penalty_charges_overdue_derived,0)) AS penalties_overdue FROM      m_office o JOIN      m_office ounder ON        ounder.hierarchy LIKE concat(o.hierarchy, ''%'') AND       ounder.hierarchy LIKE concat(''${currentUserHierarchy}'', ''%'') JOIN      m_client c ON        c.office_id = ounder.id JOIN      m_loan l ON        l.client_id = c.id LEFT JOIN m_currency cur ON        cur.code = l.currency_code LEFT JOIN m_loan_arrears_aging laa ON        laa.loan_id = l.id WHERE     o.id = ''${officeId}'' AND       ( l.currency_code = ''${currencyId}'' OR        ''-1'' = ''${currencyId}'') AND       ( l.product_id = ''${loanProductId}'' OR        ''-1'' = ''${loanProductId}'') AND       ( coalesce(l.loan_officer_id, -10) = ${loanOfficerId} OR        ''-1'' = ${loanOfficerId}) AND       ( coalesce(l.fund_id, -10) = ${fundId} OR        -1 = ${fundId}) and (coalesce(l.loanpurpose_cv_id, -10) = ${loanPurposeId} OR        -1 = ${loanPurposeId}) AND       l.loan_status_id = 300 AND       l.expected_maturedon_date < CURRENT_DATE GROUP BY  ounder.id, cur.display_symbol, l.currency_code) x ON       x.branch = mo.id ORDER BY mo.hierarchy, x.currency', NULL, 't', 't', 'f');
INSERT INTO public.stretchy_report (id, report_name, report_type, report_subtype, report_category, report_sql, description, core_report, use_report, self_service_user_report) VALUES ('57', 'Active Loans in last installment', 'Table', NULL, 'Loan', 'SELECT    Concat(REPEAT(''..'', ((Length(lastinstallment.hierarchy) - Length(REPLACE(lastinstallment.hierarchy, ''.'', '''')) - 1))), lastinstallment.branch) AS "Office/Branch", lastinstallment.currency, lastinstallment."Loan Officer", lastinstallment."Client Account No", lastinstallment."Client", lastinstallment."Loan Account No", lastinstallment."Product", lastinstallment."Fund", lastinstallment."Loan Amount", lastinstallment."Annual Nominal Interest Rate", lastinstallment."Disbursed", lastinstallment."Expected Matured On" , l.principal_repaid_derived  AS "Principal Repaid", l.principal_outstanding_derived AS "Principal Outstanding", laa.principal_overdue_derived AS "Principal Overdue", l.interest_repaid_derived AS "Interest Repaid", l.interest_outstanding_derived  AS "Interest Outstanding", laa.interest_overdue_derived  AS "Interest Overdue", l.fee_charges_repaid_derived  AS "Fees Repaid", l.fee_charges_outstanding_derived AS "Fees Outstanding", laa.fee_charges_overdue_derived AS "Fees Overdue", l.penalty_charges_repaid_derived  AS "Penalties Repaid", l.penalty_charges_outstanding_derived AS "Penalties Outstanding", laa.penalty_charges_overdue_derived AS "Penalties Overdue" FROM      ( SELECT    l.id AS loanid, l.number_of_repayments, Min(r.installment), ounder.id, ounder.hierarchy, ounder.name AS branch, Coalesce(cur.display_symbol, l.currency_code) AS currency, lo.display_name AS "Loan Officer", c.account_no  AS "Client Account No", c.display_name  AS "Client", l.account_no  AS "Loan Account No", pl.name AS "Product", f.name  AS "Fund", l.principal_amount  AS "Loan Amount", l.annual_nominal_interest_rate  AS "Annual Nominal Interest Rate", DATE_TRUNC(''day'', l.disbursedon_date) AS "Disbursed", DATE_TRUNC(''day'', l.expected_maturedon_date)  AS "Expected Matured On" FROM      m_office o JOIN      m_office ounder ON        ounder.hierarchy LIKE Concat(o.hierarchy, ''%'') AND       ounder.hierarchy LIKE Concat(''${currentUserHierarchy}'', ''%'') JOIN      m_client c ON        c.office_id = ounder.id JOIN      m_loan l ON        l.client_id = c.id JOIN      m_product_loan pl ON        pl.id = l.product_id LEFT JOIN m_staff lo ON        lo.id = l.loan_officer_id LEFT JOIN m_currency cur ON        cur.code = l.currency_code LEFT JOIN m_fund f ON        f.id = l.fund_id LEFT JOIN m_loan_repayment_schedule r ON        r.loan_id = l.id WHERE     o.id = ''${officeId}'' AND       ( l.currency_code = ''${currencyId}'' OR        ''-1'' = ''${currencyId}'') AND       ( l.product_id = ''${loanProductId}'' OR        ''-1'' = ''${loanProductId}'') AND       ( Coalesce(l.loan_officer_id, -10) = ${loanOfficerId} OR        ''-1'' = ${loanOfficerId}) AND       ( coalesce(l.fund_id, -10) = ${fundId} OR        -1 = ${fundId}) AND       ( coalesce(l.loanpurpose_cv_id, -10) = ${loanPurposeId} OR        -1 = ${loanPurposeId}) AND       l.loan_status_id = 300 AND       r.completed_derived IS FALSE AND       r.duedate >= CURRENT_DATE GROUP BY  l.id, ounder.id, cur.display_symbol, lo.display_name, c.account_no, c.display_name, pl.name, f.name HAVING    l.number_of_repayments = min(r.installment)) lastinstallment JOIN      m_loan l ON        l.id = lastinstallment.loanid LEFT JOIN m_loan_arrears_aging laa ON        laa.loan_id = l.id ORDER BY  lastinstallment.hierarchy, lastinstallment.currency, lastinstallment."Client Account No", lastinstallment."Loan Account No"', 'Individual Client   Report', 't', 't', 'f');
INSERT INTO public.stretchy_report (id, report_name, report_type, report_subtype, report_category, report_sql, description, core_report, use_report, self_service_user_report) VALUES ('179', 'Loan Repayment', 'SMS', 'Triggered', NULL, 'select ml.id as loanId, mc.id, mc.firstname, COALESCE(mc.middlename, '''') as middlename, mc.lastname, mc.display_name as FullName, mobile_no as mobileNo, mc.group_name as GroupName, round(ml.principal_amount, ml.currency_digits) as LoanAmount, round(ml."total_outstanding_derived", ml.currency_digits) as LoanOutstanding, ml."account_no" as LoanAccountId, round(mlt.amountPaid, ml.currency_digits) as repaymentAmount FROM m_office mo JOIN m_office ounder ON ounder.hierarchy LIKE CONCAT(mo.hierarchy, ''%'') AND ounder.hierarchy like CONCAT(''.'', ''%'') LEFT JOIN (select ml.id as loanId, COALESCE(mc.id, mc2.id) as id, COALESCE(mc.firstname, mc2.firstname) as firstname, COALESCE(mc.middlename, COALESCE(mc2.middlename, (''''))) as middlename, COALESCE(mc.lastname, mc2.lastname) as lastname, COALESCE(mc.display_name, mc2.display_name) as display_name, COALESCE(mc.status_enum, mc2.status_enum) as status_enum, COALESCE(mc.mobile_no, mc2.mobile_no) as mobile_no, COALESCE(mg.office_id, mc2.office_id) as office_id, COALESCE(mg.staff_id, mc2.staff_id) as staff_id, mg.id as group_id, mg.display_name as group_name from m_loan ml left join m_group mg on mg.id = ml.group_id left join m_group_client mgc on mgc.group_id = mg.id left join m_client mc on mc.id = mgc.client_id left join m_client mc2 on mc2.id = ml.client_id order by loanId) mc on mc.office_id = ounder.id right join m_loan as ml on mc.loanId = ml.id right join(select mlt.amount as amountPaid, mlt.id, mlt.loan_id from m_loan_transaction mlt where mlt.is_reversed = false group by mlt.loan_id, mlt.id) as mlt on mlt.loan_id = ml.id right join m_loan_repayment_schedule as mls1 on ml.id = mls1.loan_id and mls1."completed_derived" = false and mls1.installment = (SELECT MIN(installment) from m_loan_repayment_schedule where loan_id = ml.id and duedate <= CURRENT_DATE and completed_derived = false) where mc.status_enum = 300 and mobile_no is not null and ml."loan_status_id" = 300 and (mo.id = ''${officeId}'' or ''${officeId}'' = -1) and (mc.staff_id = ${loanOfficerId} or ${loanOfficerId} = -1) and (ml.loan_type_enum = ${loanType} or ${loanType} = -1) and ml.id in (select mla.loan_id from m_loan_arrears_aging mla) group by ml.id, mc.id, mc.firstname, mc.middlename, mc.lastname, mc.display_name, mc.mobile_no, mc.group_name, mlt.amountPaid', 'Loan Repayment', 'f', 'f', 'f');
INSERT INTO public.stretchy_report (id, report_name, report_type, report_subtype, report_category, report_sql, description, core_report, use_report, self_service_user_report) VALUES ('58', 'Active Loans in last installment Summary', 'Table', NULL, 'Loan', 'SELECT   Concat(REPEAT(''..'', ((Length(mo.hierarchy ) - Length(REPLACE(mo.hierarchy, ''.'', '''')) - 1))), mo.name) AS "Office/Branch", x.currency  AS currency, x.client_count  AS "No. of Clients", x.active_loan_count AS "No. Active Loans", x. arrears_loan_count AS "No. of Loans in Arrears", x.principal AS "Total Loans Disbursed", x.principal_repaid  AS "Principal Repaid", x.principal_outstanding AS "Principal Outstanding", x.principal_overdue AS "Principal Overdue", x.interest  AS "Total Interest", x.interest_repaid AS "Interest Repaid", x.interest_outstanding  AS "Interest Outstanding", x.interest_overdue  AS "Interest Overdue", x.fees  AS "Total Fees", x.fees_repaid AS "Fees Repaid", x.fees_outstanding  AS "Fees Outstanding", x.fees_overdue  AS "Fees Overdue", x.penalties AS "Total Penalties", x.penalties_repaid  AS "Penalties Repaid", x.penalties_outstanding AS "Penalties Outstanding", x.penalties_overdue AS "Penalties Overdue", ( CASE WHEN ${parType} = 1 THEN cast(round((x.principal_overdue * 100) / x.principal_outstanding, 2) AS                                                                                                                                     CHAR) WHEN ${parType} = 2 THEN cast(round(((x.principal_overdue + x.interest_overdue) * 100) / (x.principal_outstanding + x.interest_outstanding), 2) AS                                                                                   CHAR) WHEN ${parType} = 3 THEN cast(round(((x.principal_overdue + x.interest_overdue + x.fees_overdue) * 100) / (x.principal_outstanding + x.interest_outstanding + x.fees_outstanding), 2) AS                                             CHAR) WHEN ${parType} = 4 THEN cast(round(((x.principal_overdue + x.interest_overdue + x.fees_overdue + x.penalties_overdue) * 100) / (x.principal_outstanding + x.interest_outstanding + x.fees_outstanding + x.penalties_overdue), 2) AS CHAR) ELSE ''invalid PAR Type'' end) AS "Portfolio at Risk %" FROM     m_office mo JOIN ( SELECT    lastinstallment.branchid AS branchid, lastinstallment.currency, count(DISTINCT(lastinstallment.clientid))  AS client_count, count(DISTINCT(lastinstallment.loanid))  AS active_loan_count, count(DISTINCT(laa.loan_id) )  AS arrears_loan_count, sum(l.principal_disbursed_derived) AS principal, sum(l.principal_repaid_derived)  AS principal_repaid, sum(l.principal_outstanding_derived) AS principal_outstanding, sum(coalesce(laa.principal_overdue_derived,0)) AS principal_overdue, sum(l.interest_charged_derived)  AS interest, sum(l.interest_repaid_derived) AS interest_repaid, sum(l.interest_outstanding_derived)  AS interest_outstanding, sum(coalesce(laa.interest_overdue_derived,0))  AS interest_overdue, sum(l.fee_charges_charged_derived) AS fees, sum(l.fee_charges_repaid_derived)  AS fees_repaid, sum(l.fee_charges_outstanding_derived) AS fees_outstanding, sum(coalesce(laa.fee_charges_overdue_derived,0)) AS fees_overdue, sum(l.penalty_charges_charged_derived) AS penalties, sum(l.penalty_charges_repaid_derived)  AS penalties_repaid, sum(l.penalty_charges_outstanding_derived) AS penalties_outstanding, sum(coalesce(laa.penalty_charges_overdue_derived,0)) AS penalties_overdue FROM      ( SELECT    l.id AS loanid, l.number_of_repayments, min(r.installment), ounder.id AS branchid, ounder.hierarchy, ounder.name AS branch, coalesce(cur.display_symbol, l.currency_code) AS currency, lo.display_name AS "Loan Officer", c.id  AS clientid, c.account_no  AS "Client Account No", c.display_name  AS "Client", l.account_no  AS "Loan Account No", pl.name AS "Product", f.name  AS fund, l.principal_amount  AS "Loan Amount", l.annual_nominal_interest_rate  AS "Annual Nominal Interest Rate", DATE_TRUNC(''day'', l.disbursedon_date) AS "Disbursed", DATE_TRUNC(''day'', l.expected_maturedon_date)  AS "Expected Matured On" FROM      m_office o JOIN      m_office ounder ON        ounder.hierarchy LIKE concat(o.hierarchy, ''%'') AND       ounder.hierarchy LIKE concat(''${currentUserHierarchy}'', ''%'') JOIN      m_client c ON        c.office_id = ounder.id JOIN      m_loan l ON        l.client_id = c.id JOIN      m_product_loan pl ON        pl.id = l.product_id LEFT JOIN m_staff lo ON        lo.id = l.loan_officer_id LEFT JOIN m_currency cur ON        cur.code = l.currency_code LEFT JOIN m_fund f ON        f.id = l.fund_id LEFT JOIN m_loan_repayment_schedule r ON        r.loan_id = l.id WHERE     o.id = ''${officeId}'' AND       ( l.currency_code = ''${currencyId}'' OR        ''-1'' = ''${currencyId}'') AND       ( l.product_id = ''${loanProductId}'' OR        ''-1'' = ''${loanProductId}'') AND       ( coalesce(l.loan_officer_id, -10) = ${loanOfficerId} OR        ''-1'' = ${loanOfficerId}) AND       ( coalesce(l.fund_id, -10) = ${fundId} OR        -1 = ${fundId}) AND       ( coalesce(l.loanpurpose_cv_id, -10) = ${loanPurposeId} OR        -1 = ${loanPurposeId}) AND       l.loan_status_id = 300 AND       r.completed_derived IS FALSE AND       r.duedate >= CURRENT_DATE GROUP BY  l.id, ounder.id, cur.display_symbol, lo.display_name, c.id, pl.name, f.name HAVING    l.number_of_repayments = min(r.installment)) lastinstallment JOIN      m_loan l ON        l.id = lastinstallment.loanid LEFT JOIN m_loan_arrears_aging laa ON        laa.loan_id = l.id GROUP BY  lastinstallment.branchid, lastinstallment.currency) x ON       x.branchid = mo.id ORDER BY mo.hierarchy, x.currency', 'Individual Client   Report', 't', 't', 'f');
INSERT INTO public.stretchy_report (id, report_name, report_type, report_subtype, report_category, report_sql, description, core_report, use_report, self_service_user_report) VALUES ('59', 'Active Loans by Disbursal Period', 'Table', NULL, 'Loan', 'SELECT    Concat(REPEAT(''..'', ((Length(ounder.hierarchy) - Length(REPLACE(ounder.hierarchy, ''.'', '''')) - 1))), ounder.name) AS "Office/Branch", Coalesce(cur.display_symbol, l.currency_code)  AS currency, c.account_no AS "Client Account No", c.display_name AS "Client", l.account_no AS "Loan Account No", pl.name  AS "Product", f.name AS fund, l.principal_amount AS "Loan Principal Amount", l.annual_nominal_interest_rate AS "Annual Nominal Interest Rate", DATE_TRUNC(''day'', l.disbursedon_date)  AS "Disbursed Date", l.total_expected_repayment_derived AS "Total Loan (P+I+F+Pen)", l.total_repayment_derived  AS "Total Repaid (P+I+F+Pen)", lo.display_name  AS "Loan Officer" FROM      m_office o JOIN      m_office ounder ON        ounder.hierarchy LIKE Concat(o.hierarchy, ''%'') AND       ounder.hierarchy LIKE Concat(''${currentUserHierarchy}'', ''%'') JOIN      m_client c ON        c.office_id = ounder.id JOIN      m_loan l ON        l.client_id = c.id JOIN      m_product_loan pl ON        pl.id = l.product_id LEFT JOIN m_staff lo ON        lo.id = l.loan_officer_id LEFT JOIN m_currency cur ON        cur.code = l.currency_code LEFT JOIN m_fund f ON        f.id = l.fund_id LEFT JOIN m_loan_arrears_aging laa ON        laa.loan_id = l.id WHERE     o.id = ''${officeId}'' AND       (l.currency_code = ''${currencyId}'' OR ''-1'' = ''${currencyId}'') AND       (l.product_id = ''${loanProductId}'' OR ''-1'' = ''${loanProductId}'') AND       (Coalesce(l.loan_officer_id, -10) = ${loanOfficerId} OR ''-1'' = ${loanOfficerId}) AND       (coalesce(l.fund_id, -10) = ${fundId} OR -1 = ${fundId}) AND       (coalesce(l.loanpurpose_cv_id, -10) = ${loanPurposeId} OR -1 = ${loanPurposeId}) AND       l.disbursedon_date BETWEEN ''${startDate}'' AND ''${endDate}'' AND       l.loan_status_id = 300 GROUP BY  l.id, ounder.hierarchy, ounder.name, cur.display_symbol, c.account_no, c.display_name, pl.name, f.name, lo.display_name ORDER BY  ounder.hierarchy, l.currency_code, c.account_no, l.account_no', 'Individual Client   Report', 't', 't', 'f');
INSERT INTO public.stretchy_report (id, report_name, report_type, report_subtype, report_category, report_sql, description, core_report, use_report, self_service_user_report) VALUES ('61', 'Aging Summary (Arrears in Months)', 'Table', NULL, 'Loan', 'SELECT    Coalesce(periods.currencyname, periods.currency) AS currency, periods.period_no AS "Days In Arrears", coalesce(ars.loanid, 0) AS "No Of Loans", coalesce(ars.principal,0.0) AS "Original Principal", coalesce(ars.interest,0.0) AS "Original Interest", coalesce(ars.prinpaid,0.0) AS "Principal Paid", coalesce(ars.intpaid,0.0) AS "Interest Paid", coalesce(ars.prinoverdue,0.0) AS "Principal Overdue", coalesce(ars.intoverdue,0.0) AS "Interest Overdue" FROM ( SELECT curs.code AS currency, curs.name AS currencyname, pers.* FROM   ( SELECT ''On Schedule'' period_no, 1 pid UNION SELECT ''0 - 30'', 2 UNION SELECT ''30 - 60'', 3 UNION SELECT ''60 - 90'', 4 UNION SELECT ''90 - 180'', 5 UNION SELECT ''180 - 360'', 6 UNION SELECT ''> 360'', 7 ) pers, ( SELECT  DISTINCT ON (moc.code) moc.code, moc.name FROM       m_office mo2 INNER JOIN m_office ounder2 ON         ounder2.hierarchy LIKE concat(mo2.hierarchy, ''%'') AND        ounder2.hierarchy LIKE concat(''${currentUserHierarchy}'', ''%'') INNER JOIN m_client mc2 ON         mc2.office_id=ounder2.id INNER JOIN m_loan ml2 ON         ml2.client_id = mc2.id INNER JOIN m_organisation_currency moc ON         moc.code = ml2.currency_code WHERE      ml2.loan_status_id=300 /* active */ AND        mo2.id=''${officeId}'' AND        ( ml2.currency_code = ''${currencyId}'' OR         ''-1'' = ''${currencyId}'') GROUP BY moc.code, moc.name) curs) periods LEFT JOIN /* table of aging periods per currency with gaps if no applicable loans */ ( SELECT   z.currency, z.arrperiod, count(z.loanid)  AS loanid, sum(z.principal) AS principal, sum(z.interest)  AS interest, sum(z.prinpaid)  AS prinpaid, sum(z.intpaid) AS intpaid, sum(z.prinoverdue) AS prinoverdue, sum(z.intoverdue)  AS intoverdue FROM     ( SELECT x.loanid, x.currency, x.principal, x.interest, x.prinpaid, x.intpaid, x.prinoverdue, x.intoverdue, CASE WHEN extract(day FROM (CURRENT_DATE::TIMESTAMP - MINOVERDUEDATE::TIMESTAMP))<1 THEN ''On Schedule'' WHEN extract(day FROM (CURRENT_DATE::TIMESTAMP - MINOVERDUEDATE::TIMESTAMP))<31 THEN ''0 - 30'' WHEN extract(day FROM (CURRENT_DATE::TIMESTAMP - MINOVERDUEDATE::TIMESTAMP))<61 THEN ''30 - 60'' WHEN extract(day FROM (CURRENT_DATE::TIMESTAMP - MINOVERDUEDATE::TIMESTAMP))<91 THEN ''60 - 90'' WHEN extract(day FROM (CURRENT_DATE::TIMESTAMP - MINOVERDUEDATE::TIMESTAMP))<181 THEN ''90 - 180'' WHEN extract(day FROM (CURRENT_DATE::TIMESTAMP - MINOVERDUEDATE::TIMESTAMP))<361 THEN ''180 - 360'' ELSE ''> 360'' end AS arrperiod FROM   ( SELECT     ml.id  AS loanid, ml.currency_code AS currency, ml.principal_disbursed_derived AS principal, ml.interest_charged_derived  AS interest, ml.principal_repaid_derived  AS prinpaid, ml.interest_repaid_derived AS intpaid, laa.principal_overdue_derived  AS prinoverdue, laa.interest_overdue_derived AS intoverdue, coalesce(laa.overdue_since_date_derived, CURRENT_DATE) AS minoverduedate FROM       m_office mo INNER JOIN m_office ounder ON         ounder.hierarchy LIKE concat(mo.hierarchy, ''%'') AND        ounder.hierarchy LIKE concat(''${currentUserHierarchy}'', ''%'') INNER JOIN m_client mc ON         mc.office_id=ounder.id INNER JOIN m_loan ml ON         ml.client_id = mc.id LEFT JOIN  m_loan_arrears_aging laa ON         laa.loan_id = ml.id WHERE      ml.loan_status_id=300 AND        mo.id=''${officeId}'' AND        ( ml.currency_code = ''${currencyId}'' OR         ''-1'' = ''${currencyId}'') GROUP BY   ml.id, laa.principal_overdue_derived, laa.interest_overdue_derived, laa.overdue_since_date_derived) x ) z GROUP BY z.currency, z.arrperiod ) ars ON        ars.arrperiod=periods.period_no AND       ars.currency = periods.currency ORDER BY  periods.currency, periods.pid', 'Loan amount in arrears by branch', 't', 't', 'f');
INSERT INTO public.stretchy_report (id, report_name, report_type, report_subtype, report_category, report_sql, description, core_report, use_report, self_service_user_report) VALUES ('93', 'Expected Payments By Date - Basic', 'Table', NULL, 'Loan', 'SELECT    ounder.name AS "Office", coalesce(ms.display_name,''-'') AS "Loan Officer", mc.account_no AS "Client Account Number", mc.display_name AS "Name", mp.name AS "Product", ml.account_no AS "Loan Account Number", mr.duedate AS "Due Date", mr.installment AS "Installment", cu.display_symbol AS "Currency", mr.principal_amount - coalesce(mr.principal_completed_derived,0) AS "Principal Due", mr.interest_amount - coalesce(coalesce(mr.interest_completed_derived,mr.interest_waived_derived),0) AS "Interest Due", coalesce(mr.fee_charges_amount,0) - coalesce(coalesce(mr.fee_charges_completed_derived,mr.fee_charges_waived_derived),0) AS "Fees Due", coalesce(mr.penalty_charges_amount,0) - coalesce(coalesce(mr.penalty_charges_completed_derived,mr.penalty_charges_waived_derived),0) AS "Penalty Due", (mr.principal_amount- coalesce(mr.principal_completed_derived,0)) + (mr.interest_amount- coalesce(coalesce(mr.interest_completed_derived,mr.interest_waived_derived),0)) + (coalesce(mr.fee_charges_amount,0)- coalesce(coalesce(mr.fee_charges_completed_derived,mr.fee_charges_waived_derived),0)) + (coalesce(mr.penalty_charges_amount,0)- coalesce(coalesce(mr.penalty_charges_completed_derived,mr.penalty_charges_waived_derived),0)) AS "Total Due", mlaa.total_overdue_derived AS "Total Overdue" FROM      m_office mo JOIN      m_office ounder ON        ounder.hierarchy LIKE concat(mo.hierarchy, ''%'') AND       ounder.hierarchy LIKE concat(''${currentUserHierarchy}'', ''%'') LEFT JOIN m_client mc ON        mc.office_id=ounder.id LEFT JOIN m_loan ml ON        ml.client_id=mc.id AND       ml.loan_status_id=300 LEFT JOIN m_loan_arrears_aging mlaa ON        mlaa.loan_id=ml.id LEFT JOIN m_loan_repayment_schedule mr ON        mr.loan_id=ml.id AND       mr.completed_derived=false LEFT JOIN m_product_loan mp ON        mp.id=ml.product_id LEFT JOIN m_staff ms ON        ms.id=ml.loan_officer_id LEFT JOIN m_currency cu ON        cu.code=ml.currency_code WHERE     mo.id=''${officeId}'' AND       ( coalesce(ml.loan_officer_id, -10) = ${loanOfficerId} OR        ''-1'' = ${loanOfficerId}) AND       mr.duedate BETWEEN ''${startDate}'' AND ''${endDate}'' ORDER BY  ounder.id, mr.duedate, ml.account_no', 'Test', 't', 't', 'f');
INSERT INTO public.stretchy_report (id, report_name, report_type, report_subtype, report_category, report_sql, description, core_report, use_report, self_service_user_report) VALUES ('96', 'GroupSummaryCounts', 'Table', NULL, NULL, 'SELECT    x.* FROM      m_office o,\n          m_group g,\n          (\n                    SELECT    a.activeclients,\n                              (b.activeclientloans + c.activegrouploans) AS activeloans,\n                              b.activeclientloans,\n                              c.activegrouploans,\n                              (b.activeclientborrowers + c.activegroupborrowers) AS activeborrowers,\n                              b.activeclientborrowers,\n                              c.activegroupborrowers,\n                              (b.overdueclientloans + c.overduegrouploans) AS overdueloans,\n                              b.overdueclientloans,\n                              c.overduegrouploans\n                    FROM      (\n                                     SELECT Count(*) AS activeclients\n                                     FROM   m_group topgroup\n                                     JOIN   m_group g\n                                     ON     g.hierarchy LIKE Concat(topgroup.hierarchy, ''%'')\n                                     JOIN   m_group_client gc\n                                     ON     gc.group_id = g.id\n                                     JOIN   m_client c\n                                     ON     c.id = gc.client_id\n                                     WHERE  topgroup.id = ${groupId}\n                                     AND    c.status_enum = 300) a,\n                              (\n                                     SELECT count(*) AS activeclientloans,\n                                            count(DISTINCT(l.client_id)) AS activeclientborrowers,\n                                            coalesce(sum(\n                                            CASE\n                                                   WHEN laa.loan_id IS NOT NULL THEN 1\n                                                   ELSE 0\n                                            end),\n                                            0) AS overdueclientloans\n                    FROM      m_group topgroup\n                    JOIN      m_group g\n                    ON        g.hierarchy LIKE concat(topgroup.hierarchy, ''%'')\n                    JOIN      m_loan l\n                    ON        l.group_id = g.id\n                    AND       l.client_id IS NOT NULL\n                    LEFT JOIN m_loan_arrears_aging laa\n                    ON        laa.loan_id = l.id\n                    WHERE     topgroup.id = ${groupId}\n                    AND       l.loan_status_id = 300) b,\n          (\n                 SELECT count(*)  AS activegrouploans,\n                        count(DISTINCT(l.group_id)) AS activegroupborrowers,\n                        coalesce(sum(\n                        CASE\n                               WHEN laa.loan_id IS NOT NULL THEN 1\n                               ELSE 0\n                        end),\n                        0) AS overduegrouploans\nFROM      m_group topgroup JOIN      m_group g ON        g.hierarchy LIKE concat(topgroup.hierarchy, ''%'') JOIN      m_loan l ON        l.group_id = g.id AND       l.client_id IS NULL LEFT JOIN m_loan_arrears_aging laa ON        laa.loan_id = l.id WHERE     topgroup.id = ${groupId} AND       l.loan_status_id = 300) c ) x WHERE g.id = ${groupId} AND o.id = g.office_id AND o.hierarchy LIKE concat(''${currentUserHierarchy}'', ''%'')\n', 'Utility query for getting group summary count details for a group_id', 't', 'f', 'f');
INSERT INTO public.stretchy_report (id, report_name, report_type, report_subtype, report_category, report_sql, description, core_report, use_report, self_service_user_report) VALUES ('97', 'GroupSummaryAmounts', 'Table', NULL, NULL, '\nSELECT    Coalesce(cur.display_symbol, l.currency_code)  AS currency,\n          Coalesce(Sum(l.principal_disbursed_derived),0) AS totaldisbursedamount,\n          Coalesce(Sum(l.principal_outstanding_derived),0) AS totalloanoutstandingamount,\n          Count(laa.loan_id) AS overdueloans,\n          Coalesce(Sum(laa.total_overdue_derived), 0)  AS totalloanoverdueamount\nFROM      m_group topgroup JOIN      m_office o ON        o.id = topgroup.office_id AND       o.hierarchy LIKE Concat(''${currentUserHierarchy}'', ''%'') JOIN      m_group g ON        g.hierarchy LIKE Concat(topgroup.hierarchy, ''%'') JOIN      m_loan l ON        l.group_id = g.id LEFT JOIN m_loan_arrears_aging laa ON        laa.loan_id = l.id LEFT JOIN m_currency cur ON        cur.code = l.currency_code WHERE     topgroup.id = ${groupId} AND       l.disbursedon_date IS NOT NULL GROUP BY  l.currency_code,\n          cur.display_symbol\n', 'Utility query for getting group summary currency amount details for a group_id', 't', 'f', 'f');
INSERT INTO public.stretchy_report (id, report_name, report_type, report_subtype, report_category, report_sql, description, core_report, use_report, self_service_user_report) VALUES ('106', 'TxnRunningBalances', 'Table', NULL, 'Transaction', 'select DATE ${startDate} AS "Transaction Date", ''Opening Balance'' AS "Transaction Type", null AS "Office", null AS "Loan Officer", null AS "Loan Account No", null AS "Loan Product", null AS "Currency", null AS "Client Account No", null AS "Client", null AS "Principal", null AS "Interest", COALESCE(round(sum(CASE WHEN txn.transaction_type_enum  = 1 /* disbursement */ THEN COALESCE(txn.interest_portion_derived, 0.00) ELSE 0 END), 2), 0.00) AS "Outstanding Principal", COALESCE(round(sum(CASE WHEN txn.transaction_type_enum in (2, 5, 8) /* repayment, repayment at disbursal, recovery repayment */ THEN COALESCE(txn.interest_portion_derived, 0.00) ELSE 0 END), 2), 0.00) AS "Interest Income", COALESCE(round(sum(CASE WHEN txn.transaction_type_enum = 6 THEN COALESCE(txn.principal_portion_derived, 0.00) ELSE 0 END), 2), 0.00) AS "Principal Write Off" from m_office o join m_office ounder on ounder.hierarchy like concat(o.hierarchy, ''%'') and ounder.hierarchy like concat(''${currentUserHierarchy}'', ''%'') join m_client c on c.office_id = ounder.id join m_loan l on l.client_id = c.id join m_product_loan lp on lp.id = l.product_id join m_loan_transaction txn on txn.loan_id = l.id left join m_currency cur on cur.code = l.currency_code where txn.is_reversed = false and txn.transaction_type_enum not in (10, 11) and o.id = ''${officeId}'' and txn.transaction_date < DATE ${startDate} union all select txn.transaction_date AS "Transaction Date", cast(COALESCE(re.enum_message_property, concat(''Unknown Transaction Type Value:'', '' '', txn.transaction_type_enum)) as char) AS "Transaction Type", ounder.name AS "Office", lo.display_name AS "Loan Officer", l.account_no AS "Loan Account No", lp.name AS "Loan Product", COALESCE(cur.display_symbol, l.currency_code) AS "Currency", c.account_no AS "Client Account No", c.display_name AS "Client", COALESCE(txn.principal_portion_derived, 0.00) AS "Principal", COALESCE(txn.interest_portion_derived, 0.00) AS "Interest", COALESCE(round(sum(CASE WHEN txn.transaction_type_enum = 1 /* disbursement */ THEN COALESCE(txn.amount, 0.00) ELSE -1 * COALESCE(txn.principal_portion_derived, 0.00) END), 2), 0.00) AS "Outstanding Principal", COALESCE(round(sum(CASE WHEN txn.transaction_type_enum in (2, 5, 8) /* repayment, repayment at disbursal, recovery repayment */ THEN COALESCE(txn.interest_portion_derived, 0.00) ELSE 0 END), 2), 0.00) AS "Interest Income", COALESCE(round(sum(CASE WHEN txn.transaction_type_enum = 6 THEN COALESCE(txn.principal_portion_derived, 0.00) ELSE 0 END), 2), 0.00) AS "Principal Write Off" from m_office o join m_office ounder on ounder.hierarchy like concat(o.hierarchy, ''%'') and ounder.hierarchy like concat(''${currentUserHierarchy}'', ''%'') join m_client c on c.office_id = ounder.id join m_loan l on l.client_id = c.id left join m_staff lo on lo.id = l.loan_officer_id join m_product_loan lp on lp.id = l.product_id join m_loan_transaction txn on txn.loan_id = l.id left join m_currency cur on cur.code = l.currency_code left join r_enum_value re on re.enum_name = ''transaction_type_enum'' AND re.enum_id = txn.transaction_type_enum where txn.is_reversed = false and txn.transaction_type_enum not in (10, 11) and (COALESCE(l.loan_officer_id, -10) = 9 or ''-1'' = 9) and o.id = ''${officeId}'' and txn.transaction_date >= DATE ${startDate} and txn.transaction_date <= DATE ${endDate} group by txn.id, ounder.id, lo.id, l.id, lp.id, cur.id, c.id, re.enum_message_property', 'Running Balance Txn report for Individual Lending.\nSuitable for small MFI''s.  Larger could use it using the branch or other parameters.\nBasically, suck it and see if its quick enough for you out-of-te box or whether it needs performance work in your situation.\n', 'f', 'f', 'f');
INSERT INTO public.stretchy_report (id, report_name, report_type, report_subtype, report_category, report_sql, description, core_report, use_report, self_service_user_report) VALUES ('107', 'FieldAgentStats', 'Table', NULL, 'Quipo', 'select COALESCE(cur.display_symbol, l.currency_code) as Currency, /*This query will return more than one entry if more than one currency is used */ count(distinct(c.id)) as activeClients, count(*) as activeLoans, sum(l.principal_disbursed_derived) as disbursedAmount, sum(l.principal_outstanding_derived) as loanOutstandingAmount, round((sum(l.principal_outstanding_derived) * 100) / sum(l.principal_disbursed_derived),2) as loanOutstandingPC, sum(COALESCE(lpa.principal_in_advance_derived,0.0)) as LoanPaidInAdvance, sum( CASE WHEN (CURRENT_DATE - 28 * INTERVAL ''1 day'') > COALESCE(laa.overdue_since_date_derived, CURRENT_DATE) THEN l.principal_outstanding_derived ELSE 0 END) as portfolioAtRisk, round((sum( CASE WHEN (CURRENT_DATE - 28 * INTERVAL ''1 day'') > COALESCE(laa.overdue_since_date_derived, CURRENT_DATE) THEN l.principal_outstanding_derived ELSE 0 END) * 100) / sum(l.principal_outstanding_derived), 2) as portfolioAtRiskPC, count(distinct( CASE WHEN (CURRENT_DATE - 28 * INTERVAL ''1 day'') > COALESCE(laa.overdue_since_date_derived, CURRENT_DATE) THEN c.id ELSE null END)) as clientsInDefault, round((count(distinct( CASE WHEN (CURRENT_DATE - 28 * INTERVAL ''1 day'') > COALESCE(laa.overdue_since_date_derived, CURRENT_DATE) THEN c.id ELSE null END))) * 100 / count(distinct(c.id)),2) as clientsInDefaultPC, sum(l.principal_disbursed_derived) / count(*) as averageLoanAmount from m_staff fa join m_office o on o.id = fa.office_id AND o.hierarchy like concat(''${currentUserHierarchy}'', ''%'') join m_group pgm on pgm.staff_id = fa.id join m_loan l on l.group_id = pgm.id and l.client_id is not null left join m_currency cur on cur.code = l.currency_code left join m_loan_arrears_aging laa on laa.loan_id = l.id left join m_loan_paid_in_advance lpa on lpa.loan_id = l.id join m_client c on c.id = l.client_id where fa.id = ${staffId} and l.loan_status_id = 300 group by l.currency_code, cur.id', 'Field Agent Statistics', 'f', 'f', 'f');
INSERT INTO public.stretchy_report (id, report_name, report_type, report_subtype, report_category, report_sql, description, core_report, use_report, self_service_user_report) VALUES ('108', 'FieldAgentPrograms', 'Table', NULL, 'Quipo', 'select pgm.id, pgm.display_name as name, sts.enum_message_property as status  from m_group pgm  join m_office o on o.id = pgm.office_id AND o.hierarchy like concat(''${currentUserHierarchy}'', ''%'') left join r_enum_value sts on sts.enum_name = ''status_enum'' and sts.enum_id = pgm.status_enum  where pgm.staff_id = ${staffId}', 'List of Field Agent Programs', 'f', 'f', 'f');
INSERT INTO public.stretchy_report (id, report_name, report_type, report_subtype, report_category, report_sql, description, core_report, use_report, self_service_user_report) VALUES ('109', 'ProgramDetails', 'Table', NULL, 'Quipo', 'select l.id as loanId, l.account_no as loanAccountNo, c.id as clientId, c.account_no as clientAccountNo, pgm.display_name as programName,  (select count(*) from m_loan cy where cy.group_id = pgm.id and cy.client_id =c.id and cy.disbursedon_date <= l.disbursedon_date) as loanCycleNo,  c.display_name as clientDisplayName, COALESCE(cur.display_symbol, l.currency_code) as Currency, COALESCE(l.principal_repaid_derived,0.0) as loanRepaidAmount, COALESCE(l.principal_outstanding_derived, 0.0) as loanOutstandingAmount, COALESCE(lpa.principal_in_advance_derived,0.0) as LoanPaidInAdvance,  COALESCE(laa.principal_overdue_derived, 0.0) as loanInArrearsAmount, CASE WHEN COALESCE(laa.principal_overdue_derived, 0.00) > 0 THEN ''Yes'' ELSE ''No'' END as inDefault,  CASE WHEN (CURRENT_DATE - 28 * INTERVAL ''1 day'') > COALESCE(laa.overdue_since_date_derived, CURRENT_DATE) THEN l.principal_outstanding_derived ELSE 0 END as portfolioAtRisk   from m_group pgm  join m_office o on o.id = pgm.office_id AND o.hierarchy like concat(''${currentUserHierarchy}'', ''%'')  join m_loan l on l.group_id = pgm.id and l.client_id is not null  left join m_currency cur on cur.code = l.currency_code  join m_client c on c.id = l.client_id left join m_loan_arrears_aging laa on laa.loan_id = l.id  left join m_loan_paid_in_advance lpa on lpa.loan_id = l.id  where pgm.id = ${programId}  and l.loan_status_id = 300 order by c.display_name, l.account_no', 'List of Loans in a Program', 'f', 'f', 'f');
INSERT INTO public.stretchy_report (id, report_name, report_type, report_subtype, report_category, report_sql, description, core_report, use_report, self_service_user_report) VALUES ('110', 'ChildrenStaffList', 'Table', NULL, 'Quipo', 'select s.id, s.display_name, s.firstname, s.lastname, s.organisational_role_enum,\ns.organisational_role_parent_staff_id,\nsp.display_name AS "organisational_role_parent_staff_display_name"\nfrom m_staff s\njoin m_staff sp on s.organisational_role_parent_staff_id = sp.id\nwhere s.organisational_role_parent_staff_id = ${staffId}', 'Get Next Level Down Staff', 'f', 'f', 'f');
INSERT INTO public.stretchy_report (id, report_name, report_type, report_subtype, report_category, report_sql, description, core_report, use_report, self_service_user_report) VALUES ('111', 'CoordinatorStats', 'Table', NULL, 'Quipo', 'select COALESCE(cur.display_symbol, l.currency_code) as Currency, /*This query will return more than one entry if more than one currency is used */ count(distinct(c.id)) as activeClients, count(*) as activeLoans, sum(l.principal_disbursed_derived) as disbursedAmount, sum(l.principal_outstanding_derived) as loanOutstandingAmount, round((sum(l.principal_outstanding_derived) * 100) / sum(l.principal_disbursed_derived),2) as loanOutstandingPC, sum(COALESCE(lpa.principal_in_advance_derived,0.0)) as LoanPaidInAdvance, sum( CASE WHEN (CURRENT_DATE - 28 * INTERVAL ''1 day'') > COALESCE(laa.overdue_since_date_derived, CURRENT_DATE) THEN l.principal_outstanding_derived ELSE 0 END) as portfolioAtRisk, round((sum( CASE WHEN (CURRENT_DATE - 28 * INTERVAL ''1 day'') > COALESCE(laa.overdue_since_date_derived, CURRENT_DATE) THEN l.principal_outstanding_derived ELSE 0 END) * 100) / sum(l.principal_outstanding_derived), 2) as portfolioAtRiskPC, count(distinct( CASE WHEN (CURRENT_DATE - 28 * INTERVAL ''1 day'') > COALESCE(laa.overdue_since_date_derived, CURRENT_DATE) THEN c.id ELSE null END)) as clientsInDefault, round((count(distinct( CASE WHEN (CURRENT_DATE - 28 * INTERVAL ''1 day'') > COALESCE(laa.overdue_since_date_derived, CURRENT_DATE) THEN c.id ELSE null END))) * 100 / count(distinct(c.id)),2) as clientsInDefaultPC, sum(l.principal_disbursed_derived) / count(*) as averageLoanAmount from m_staff coord join m_staff fa on fa.organisational_role_parent_staff_id = coord.id join m_office o on o.id = fa.office_id AND o.hierarchy like concat(''${currentUserHierarchy}'', ''%'') join m_group pgm on pgm.staff_id = fa.id join m_loan l on l.group_id = pgm.id and l.client_id is not null left join m_currency cur on cur.code = l.currency_code left join m_loan_arrears_aging laa on laa.loan_id = l.id left join m_loan_paid_in_advance lpa on lpa.loan_id = l.id join m_client c on c.id = l.client_id where coord.id = ${staffId} and l.loan_status_id = 300 group by l.currency_code, cur.id', 'Coordinator Statistics', 'f', 'f', 'f');
INSERT INTO public.stretchy_report (id, report_name, report_type, report_subtype, report_category, report_sql, description, core_report, use_report, self_service_user_report) VALUES ('112', 'BranchManagerStats', 'Table', NULL, 'Quipo', 'SELECT    Coalesce(cur.display_symbol, l.currency_code)  AS currency, Count(DISTINCT(c.id))  AS activeclients, Count(*) AS activeloans, Sum(l.principal_disbursed_derived) AS disbursedamount, Sum(l.principal_outstanding_derived) AS loanoutstandingamount, Round((Sum(l.principal_outstanding_derived) * 100) / Sum(l.principal_disbursed_derived),2) AS loanoutstandingpc, Sum(Coalesce(lpa.principal_in_advance_derived,0.0))  AS loanpaidinadvance, sum( CASE WHEN ( CURRENT_DATE - 28 * INTERVAL ''1 day'') > coalesce(laa.overdue_since_date_derived, CURRENT_DATE) THEN l.principal_outstanding_derived ELSE 0 end) AS portfolioatrisk, round((sum( CASE WHEN ( CURRENT_DATE - 28 * INTERVAL ''1 day'' ) > coalesce(laa.overdue_since_date_derived, CURRENT_DATE) THEN l.principal_outstanding_derived ELSE 0 end) * 100) / sum(l.principal_outstanding_derived), 2) AS portfolioatriskpc, count(DISTINCT( CASE WHEN ( CURRENT_DATE - 28 * INTERVAL ''1 day'' ) > coalesce(laa.overdue_since_date_derived, CURRENT_DATE) THEN c.id ELSE NULL end)) AS clientsindefault, round((count(DISTINCT( CASE WHEN ( CURRENT_DATE - 28 * INTERVAL ''1 day'' ) > coalesce(laa.overdue_since_date_derived, CURRENT_DATE) THEN c.id ELSE NULL end)) * 100) / count(DISTINCT(c.id)), 2)  AS clientsindefaultpc, (sum(l.principal_disbursed_derived) / count(*)) AS averageloanamount FROM      m_staff bm JOIN      m_staff coord ON        coord.organisational_role_parent_staff_id = bm.id JOIN      m_staff fa ON        fa.organisational_role_parent_staff_id = coord.id JOIN      m_office o ON        o.id = fa.office_id AND       o.hierarchy LIKE concat(''${currentUserHierarchy}'', ''%'') JOIN      m_group pgm ON        pgm.staff_id = fa.id JOIN      m_loan l ON        l.group_id = pgm.id AND       l.client_id IS NOT NULL LEFT JOIN m_currency cur ON        cur.code = l.currency_code LEFT JOIN m_loan_arrears_aging laa ON        laa.loan_id = l.id LEFT JOIN m_loan_paid_in_advance lpa ON        lpa.loan_id = l.id JOIN      m_client c ON        c.id = l.client_id WHERE     bm.id = ${staffId} AND       l.loan_status_id = 300 GROUP BY  l.currency_code, cur.display_symbol', 'Branch Manager Statistics', 'f', 'f', 'f');
INSERT INTO public.stretchy_report (id, report_name, report_type, report_subtype, report_category, report_sql, description, core_report, use_report, self_service_user_report) VALUES ('114', 'ProgramStats', 'Table', NULL, 'Quipo', 'select COALESCE(cur.display_symbol, l.currency_code) as Currency, /*This query will return more than one entry if more than one currency is used */ count(distinct(c.id)) as activeClients, count(*) as activeLoans, sum(l.principal_disbursed_derived) as disbursedAmount, sum(l.principal_outstanding_derived) as loanOutstandingAmount, round((sum(l.principal_outstanding_derived) * 100) / sum(l.principal_disbursed_derived),2) as loanOutstandingPC, sum(COALESCE(lpa.principal_in_advance_derived,0.0)) as LoanPaidInAdvance, sum( CASE WHEN (CURRENT_DATE - 28 * INTERVAL ''1 day'') > COALESCE(laa.overdue_since_date_derived, CURRENT_DATE) THEN l.principal_outstanding_derived ELSE 0 END) as portfolioAtRisk, round((sum( CASE WHEN (CURRENT_DATE - 28 * INTERVAL ''1 day'') > COALESCE(laa.overdue_since_date_derived, CURRENT_DATE) THEN l.principal_outstanding_derived ELSE 0 END) * 100) / sum(l.principal_outstanding_derived), 2) as portfolioAtRiskPC, count(distinct( CASE WHEN (CURRENT_DATE - 28 * INTERVAL ''1 day'') > COALESCE(laa.overdue_since_date_derived, CURRENT_DATE) THEN c.id ELSE null END)) as clientsInDefault, round((count(distinct( CASE WHEN (CURRENT_DATE - 28 * INTERVAL ''1 day'') > COALESCE(laa.overdue_since_date_derived, CURRENT_DATE) THEN c.id ELSE null END))) * 100 / count(distinct(c.id)),2) as clientsInDefaultPC, sum(l.principal_disbursed_derived) / count(*) as averageLoanAmount from m_group pgm join m_office o on o.id = pgm.office_id AND o.hierarchy like concat(''${currentUserHierarchy}'', ''%'') join m_loan l on l.group_id = pgm.id and l.client_id is not null left join m_currency cur on cur.code = l.currency_code left join m_loan_arrears_aging laa on laa.loan_id = l.id left join m_loan_paid_in_advance lpa on lpa.loan_id = l.id join m_client c on c.id = l.client_id where pgm.id = ${programId} and l.loan_status_id = 300 group  by l.currency_code, cur.id', 'Program Statistics', 'f', 'f', 'f');
INSERT INTO public.stretchy_report (id, report_name, report_type, report_subtype, report_category, report_sql, description, core_report, use_report, self_service_user_report) VALUES ('115', 'ClientSummary ', 'Table', NULL, NULL, 'SELECT x.* FROM m_client c, m_office o, (SELECT a.loanCycle, a.activeLoans, b.lastLoanAmount, d.activeSavings, d.totalSavings FROM (SELECT COALESCE(MAX(l.loan_counter),0) AS loanCycle, COUNT(l.id) AS activeLoans FROM m_loan l WHERE l.loan_status_id=300 AND l.client_id=${clientId}) a, (SELECT count(l.id), COALESCE(l.principal_amount,0) AS lastLoanAmount FROM m_loan l WHERE l.client_id=${clientId} AND l.disbursedon_date = (SELECT COALESCE(MAX(disbursedon_date),NOW()) FROM m_loan where client_id=${clientId} and loan_status_id=300) group by l.principal_amount) b, (SELECT COUNT(s.id) AS activeSavings, COALESCE(SUM(s.account_balance_derived),0) AS totalSavings FROM m_savings_account s WHERE s.status_enum=300 AND s.client_id=${clientId}) d) x WHERE c.id=${clientId} AND o.id = c.office_id AND o.hierarchy LIKE CONCAT(''${currentUserHierarchy}'', ''%'')', 'Utility query for getting the client summary details', 't', 'f', 'f');
INSERT INTO public.stretchy_report (id, report_name, report_type, report_subtype, report_category, report_sql, description, core_report, use_report, self_service_user_report) VALUES ('116', 'LoanCyclePerProduct', 'Table', NULL, NULL, 'SELECT lp.name AS "productName", MAX(l.loan_product_counter) AS "loanProductCycle" FROM m_loan l JOIN m_product_loan lp ON l.product_id=lp.id WHERE lp.include_in_borrower_cycle=true AND l.loan_product_counter IS NOT NULL AND l.client_id=${clientId} GROUP BY lp.id', 'Utility query for getting the client loan cycle details', 't', 'f', 'f');
INSERT INTO public.stretchy_report (id, report_name, report_type, report_subtype, report_category, report_sql, description, core_report, use_report, self_service_user_report) VALUES ('117', 'GroupSavingSummary', 'Table', NULL, NULL, 'select COALESCE(cur.display_symbol, sa.currency_code) as currency, count(sa.id) as totalSavingAccounts, COALESCE(sum(sa.account_balance_derived),0) as totalSavings from m_group topgroup join m_office o on o.id = topgroup.office_id and o.hierarchy like concat(''${currentUserHierarchy}'', ''%'') join m_group g on g.hierarchy like concat(topgroup.hierarchy, ''%'') join m_savings_account sa on sa.group_id = g.id left join m_currency cur on cur.code = sa.currency_code where topgroup.id = ${groupId} and sa.activatedon_date is not null group by sa.currency_code, cur.id', 'Utility query for getting group or center saving summary details for a group_id', 't', 'f', 'f');
INSERT INTO public.stretchy_report (id, report_name, report_type, report_subtype, report_category, report_sql, description, core_report, use_report, self_service_user_report) VALUES ('148', 'GroupNamesByStaff', 'Table', '', '', 'SELECT gr.id AS id, gr.display_name AS name FROM   m_group gr WHERE  gr.level_id=1 AND    gr.staff_id = ${staffId}', '', 't', 'f', 'f');
INSERT INTO public.stretchy_report (id, report_name, report_type, report_subtype, report_category, report_sql, description, core_report, use_report, self_service_user_report) VALUES ('149', 'ClientTrendsByDay', 'Table', '', 'Client', 'SELECT COUNT(cl.id) AS count, cl.activation_date AS days             FROM m_office o LEFT JOIN m_client cl on o.id = cl.office_id             WHERE o.hierarchy like concat((select ino.hierarchy from m_office ino where ino.id = ${officeId}),''%'' )                 AND (cl.activation_date BETWEEN (current_date - INTERVAL ''12 DAY'') AND DATE(NOW()- INTERVAL ''1 DAY''))             GROUP BY days             ', 'Retrieves the number of clients joined in last 12 days', 't', 'f', 'f');
INSERT INTO public.stretchy_report (id, report_name, report_type, report_subtype, report_category, report_sql, description, core_report, use_report, self_service_user_report) VALUES ('151', 'ClientTrendsByMonth', 'Table', '', 'Client', 'SELECT COUNT(cl.id) AS count, TRIM(TO_CHAR(cl.activation_date, ''Month'')) AS Months             FROM m_office o LEFT JOIN m_client cl ON o.id = cl.office_id             WHERE o.hierarchy LIKE CONCAT((SELECT ino.hierarchy FROM m_office ino WHERE ino.id = ${officeId}), ''%'')                 AND (cl.activation_date BETWEEN (CURRENT_DATE - INTERVAL ''12 months'') AND CURRENT_DATE)             GROUP BY TRIM(TO_CHAR(cl.activation_date, ''Month'')), EXTRACT(MONTH FROM cl.activation_date)             ORDER BY EXTRACT(MONTH FROM cl.activation_date) ASC', '', 't', 'f', 'f');
INSERT INTO public.stretchy_report (id, report_name, report_type, report_subtype, report_category, report_sql, description, core_report, use_report, self_service_user_report) VALUES ('154', 'LoanTrendsByMonth', 'Table', '', 'Loan', 'SELECT COUNT(ln.id) AS lcount, TRIM(TO_CHAR(ln.disbursedon_date, ''Month'')) AS Months             FROM m_office o                 LEFT JOIN m_client cl on o.id = cl.office_id                 LEFT JOIN m_loan ln on cl.id = ln.client_id             WHERE o.hierarchy like concat((select ino.hierarchy from m_office ino where ino.id = ${officeId}),''%'' )                 AND (ln.disbursedon_date BETWEEN (CURRENT_DATE - INTERVAL ''12 months'') AND CURRENT_DATE)             GROUP BY TRIM(TO_CHAR(ln.disbursedon_date, ''Month'')), EXTRACT(MONTH FROM ln.disbursedon_date)             ORDER BY EXTRACT(MONTH FROM ln.disbursedon_date) ASC', '', 't', 'f', 'f');
INSERT INTO public.stretchy_report (id, report_name, report_type, report_subtype, report_category, report_sql, description, core_report, use_report, self_service_user_report) VALUES ('152', 'LoanTrendsByDay', 'Table', '', 'Loan', 'SELECT COUNT(ln.id) AS lcount, ln.disbursedon_date AS days             FROM m_office o LEFT JOIN m_client cl on o.id = cl.office_id                 LEFT JOIN m_loan ln on cl.id = ln.client_id             WHERE o.hierarchy like concat((select ino.hierarchy from m_office ino where ino.id = ${officeId}),''%'' )                 AND (ln.disbursedon_date BETWEEN (current_date - INTERVAL ''12 DAY'') AND DATE(NOW()- INTERVAL ''1 DAY''))             GROUP BY days             ', 'Retrieves Number of loans disbursed for last 12 days', 't', 'f', 'f');
INSERT INTO public.stretchy_report (id, report_name, report_type, report_subtype, report_category, report_sql, description, core_report, use_report, self_service_user_report) VALUES ('150', 'ClientTrendsByWeek', 'Table', '', 'Client', 'SELECT COUNT(cl.id) AS count, EXTRACT(WEEK FROM cl.activation_date) AS Weeks             FROM m_office o LEFT JOIN m_client cl on o.id = cl.office_id             WHERE o.hierarchy like concat((select ino.hierarchy from m_office ino where ino.id = ${officeId}),''%'' )                 AND (cl.activation_date BETWEEN (CURRENT_DATE - INTERVAL ''12 weeks'') AND CURRENT_DATE)             GROUP BY EXTRACT(WEEK FROM cl.activation_date)', '', 't', 'f', 'f');
INSERT INTO public.stretchy_report (id, report_name, report_type, report_subtype, report_category, report_sql, description, core_report, use_report, self_service_user_report) VALUES ('165', 'Savings Accounts Dormancy Report', 'Table', NULL, 'Savings', 'select cl.display_name AS "Client Display Name", sa.account_no AS "Account Number", cl.mobile_no AS "Mobile Number", (select COALESCE(max(sat.transaction_date), sa.activatedon_date) from m_savings_account_transaction as sat where sat.is_reversed = false and sat.transaction_type_enum in (1, 2) and sat.savings_account_id = sa.id) AS "Date of Last Activity", EXTRACT(DAY FROM (CURRENT_DATE - (select COALESCE(max(sat.transaction_date), sa.activatedon_date) from m_savings_account_transaction as sat where sat.is_reversed = false and sat.transaction_type_enum in (1, 2) and sat.savings_account_id = sa.id)::TIMESTAMP)) AS "Days Since Last Activity" from m_savings_account as sa inner join m_savings_product as sp on (sa.product_id = sp.id and sp.is_dormancy_tracking_active = true) left join m_client as cl on sa.client_id = cl.id where sa.sub_status_enum = ${subStatus} and cl.office_id = ''${officeId}''', NULL, 't', 't', 'f');
INSERT INTO public.stretchy_report (id, report_name, report_type, report_subtype, report_category, report_sql, description, core_report, use_report, self_service_user_report) VALUES ('166', 'Active Clients', 'SMS', 'NonTriggered', 'Client', 'SELECT c.id AS id, c.firstname AS firstname, c.middlename AS middlename, c.lastname  AS lastname, c.display_name  AS fullname, c.mobile_no AS mobileno, Concat(REPEAT(''..'', ((Length(ounder.hierarchy) - Length( REPLACE(ounder.hierarchy, ''.'', '''')) - 1))), ounder.name) AS officename, o.id AS officenumber FROM  m_office o JOIN  m_office ounder ON ounder.hierarchy LIKE Concat(o.hierarchy, ''%'') JOIN  m_client c ON c.office_id = ounder.id LEFT JOIN r_enum_value r ON r.enum_name = ''status_enum'' AND r.enum_id = c.status_enum WHERE o.id = ''${officeId}'' AND c.status_enum = 300 AND (coalesce(c.staff_id, -10) = ${loanOfficerId} OR''-1'' = ${loanOfficerId}) GROUP BY c.id, ounder.hierarchy, ounder.name, o.id ORDER BY ounder.hierarchy, c.account_no', 'All clients with the status ‘Active’', 'f', 't', 'f');
INSERT INTO public.stretchy_report (id, report_name, report_type, report_subtype, report_category, report_sql, description, core_report, use_report, self_service_user_report) VALUES ('167', 'Prospective Clients', 'SMS', 'NonTriggered', 'Client', 'SELECT c.id AS id, c.firstname AS firstName, c.middlename AS middleName, c.lastname AS lastName, c.display_name AS fullName, c.mobile_no AS mobileNo, CONCAT(REPEAT(''..'', ((LENGTH(ounder.hierarchy) - LENGTH(REPLACE(ounder.hierarchy, ''.'', '''')) - 1))), ounder.name) AS officeName, o.id AS officeNumber FROM m_office o JOIN m_office ounder ON ounder.hierarchy LIKE CONCAT(o.hierarchy, ''%'') JOIN m_client c ON c.office_id = ounder.id LEFT JOIN r_enum_value r ON r.enum_name = ''status_enum'' AND r.enum_id = c.status_enum LEFT JOIN m_loan l ON l.client_id = c.id WHERE o.id = ''${officeId}'' AND c.status_enum = 300 AND (COALESCE(c.staff_id, -10) = ${loanOfficerId} OR ''-1'' = ${loanOfficerId}) AND l.client_id IS NULL GROUP BY c.id, ounder.id, o.id ORDER BY ounder.hierarchy, c.account_no', 'All clients with the status ‘Active’ who have never had a loan before', 'f', 't', 'f');
INSERT INTO public.stretchy_report (id, report_name, report_type, report_subtype, report_category, report_sql, description, core_report, use_report, self_service_user_report) VALUES ('168', 'Active Loan Clients', 'SMS', 'NonTriggered', 'Client', 'SELECT c.id AS id, c.firstname AS firstName, c.middlename AS middleName, c.lastname AS lastName, c.display_name AS fullName, c.mobile_no AS mobileNo, l.principal_amount AS loanAmount, (COALESCE(l.principal_outstanding_derived, 0) + COALESCE(l.interest_outstanding_derived, 0) + COALESCE(l.fee_charges_outstanding_derived, 0) + COALESCE(l.penalty_charges_outstanding_derived, 0)) AS loanOutstanding, l.principal_disbursed_derived AS loanDisbursed, ounder.id AS officeNumber, l.account_no AS loanAccountId, gua.lastname AS guarantorLastName, COUNT(gua.id) AS numberOfGuarantors, g.display_name AS groupName FROM m_office o JOIN m_office ounder ON ounder.hierarchy LIKE CONCAT(o.hierarchy, ''%'') JOIN m_client c ON c.office_id = ounder.id JOIN m_loan l ON l.client_id = c.id JOIN m_product_loan pl ON pl.id = l.product_id LEFT JOIN m_group_client gc ON gc.client_id = c.id LEFT JOIN m_group g ON g.id = gc.group_id LEFT JOIN m_staff lo ON lo.id = l.loan_officer_id LEFT JOIN m_currency cur ON cur.code = l.currency_code LEFT JOIN m_guarantor gua ON gua.loan_id = l.id WHERE o.id = ''${officeId}'' AND (COALESCE(l.loan_officer_id, -10) = ${loanOfficerId} OR ''-1'' = ${loanOfficerId}) AND l.loan_status_id = 300 AND (EXTRACT(DAY FROM (CURRENT_DATE - l.disbursedon_date::TIMESTAMP)) BETWEEN ${cycleX} AND ${cycleY}) GROUP BY l.id, c.id, ounder.id, gua.id, g.id ORDER BY ounder.hierarchy, l.currency_code, c.account_no, l.account_no', 'All clients with an outstanding loan between cycleX and cycleY days', 'f', 't', 'f');
INSERT INTO public.stretchy_report (id, report_name, report_type, report_subtype, report_category, report_sql, description, core_report, use_report, self_service_user_report) VALUES ('169', 'Loan in arrears', 'SMS', 'NonTriggered', 'Loan', 'SELECT mc.id AS id, mc.firstname AS firstName, mc.middlename AS middleName, mc.lastname AS lastName, mc.display_name AS fullName, mc.mobile_no AS mobileNo, ml.principal_amount AS loanAmount, (COALESCE(ml.principal_outstanding_derived, 0) + COALESCE(ml.interest_outstanding_derived, 0) + COALESCE(ml.fee_charges_outstanding_derived, 0) + COALESCE(ml.penalty_charges_outstanding_derived, 0)) AS loanOutstanding, ml.principal_disbursed_derived AS loanDisbursed, laa.overdue_since_date_derived AS paymentDueDate, COALESCE(laa.total_overdue_derived, 0) AS totalDue, ounder.id AS officeNumber, ml.account_no AS loanAccountId, gua.lastname AS guarantorLastName, COUNT(gua.id) AS numberOfGuarantors, g.display_name AS groupName FROM m_office mo JOIN m_office ounder ON ounder.hierarchy LIKE CONCAT(mo.hierarchy, ''%'') INNER JOIN m_client mc ON mc.office_id=ounder.id INNER JOIN m_loan ml ON ml.client_id = mc.id INNER JOIN r_enum_value rev ON rev.enum_id=ml.loan_status_id AND rev.enum_name = ''loan_status_id'' INNER JOIN m_loan_arrears_aging laa ON laa.loan_id=ml.id LEFT JOIN m_currency cur ON cur.code = ml.currency_code LEFT JOIN m_group_client gc ON gc.client_id = mc.id LEFT JOIN m_group g ON g.id = gc.group_id LEFT JOIN m_staff lo ON lo.id = ml.loan_officer_id LEFT JOIN m_guarantor gua ON gua.loan_id = ml.id WHERE ml.loan_status_id=300 AND mo.id=''${officeId}'' AND (COALESCE(ml.loan_officer_id, -10) = ${loanOfficerId} OR ''-1'' = ${loanOfficerId}) AND (EXTRACT(DAY FROM (CURRENT_DATE - laa.overdue_since_date_derived::TIMESTAMP)) BETWEEN ${fromX} AND ${toY}) GROUP BY ml.id, mc.id, laa.loan_id, ounder.id, gua.id, g.id ORDER BY ounder.hierarchy, ml.currency_code, mc.account_no, ml.account_no', 'All clients with an outstanding loan in arrears between fromX and toY days', 'f', 't', 'f');
INSERT INTO public.stretchy_report (id, report_name, report_type, report_subtype, report_category, report_sql, description, core_report, use_report, self_service_user_report) VALUES ('170', 'Loan payments due', 'SMS', 'NonTriggered', 'Loan', 'SELECT cl.id AS id, cl.firstname  AS firstName, cl.middlename  AS middleName, cl.lastname AS lastName, cl.display_name AS fullName, cl.mobile_no AS mobileNo, l.principal_amount AS loanAmount, of.id AS officeNumber, (COALESCE(l.principal_outstanding_derived, 0) + COALESCE(l.interest_outstanding_derived, 0) + COALESCE(l.fee_charges_outstanding_derived, 0) + COALESCE(l.penalty_charges_outstanding_derived, 0)) AS loanOutstanding, l.principal_disbursed_derived AS loanDisbursed, ls.duedate AS paymentDueDate, (COALESCE(SUM(ls.principal_amount), 0) - COALESCE(SUM(ls.principal_writtenoff_derived), 0) + COALESCE(SUM(ls.interest_amount), 0) - COALESCE(SUM(ls.interest_writtenoff_derived), 0) - COALESCE(SUM(ls.interest_waived_derived), 0) + COALESCE(SUM(ls.fee_charges_amount), 0) - COALESCE(SUM(ls.fee_charges_writtenoff_derived), 0) - COALESCE(SUM(ls.fee_charges_waived_derived), 0) + COALESCE(SUM(ls.penalty_charges_amount), 0) - COALESCE(SUM(ls.penalty_charges_writtenoff_derived), 0) - COALESCE(SUM(ls.penalty_charges_waived_derived), 0)) AS totalDue, laa.total_overdue_derived AS totalOverdue, l.account_no AS loanAccountId, gua.lastname AS guarantorLastName, COUNT(gua.id) AS numberOfGuarantors, gp.display_name AS groupName FROM m_office of LEFT JOIN m_client cl ON of.id = cl.office_id LEFT JOIN m_loan l ON cl.id = l.client_id LEFT JOIN m_group_client gc ON gc.client_id = cl.id LEFT JOIN m_group gp ON gp.id = l.group_id LEFT JOIN m_loan_repayment_schedule ls ON l.id = ls.loan_id LEFT JOIN m_guarantor gua ON gua.loan_id = l.id INNER JOIN m_loan_arrears_aging laa ON laa.loan_id=l.id WHERE of.id = ''${officeId}'' AND (COALESCE (l.loan_officer_id, -10) = ${loanOfficerId} OR ''-1'' = ${loanOfficerId}) AND (EXTRACT(DAY FROM (CURRENT_DATE - ls.duedate::TIMESTAMP)) BETWEEN ${fromX} AND ${toY}) AND (of.hierarchy LIKE CONCAT((SELECT ino.hierarchy FROM m_office ino WHERE ino.id = ''${officeId}''), ''%'')) GROUP BY l.id, cl.id, of.id, ls.id, laa.loan_id, gua.id, gp.id ORDER BY of.hierarchy, l.currency_code, cl.account_no, l.account_no', 'All clients with an unpaid installment due on their loan between fromX and toY days', 'f', 't', 'f');
INSERT INTO public.stretchy_report (id, report_name, report_type, report_subtype, report_category, report_sql, description, core_report, use_report, self_service_user_report) VALUES ('171', 'Dormant Prospects', 'SMS', 'NonTriggered', 'Client', 'SELECT c.id AS id, CONCAT(REPEAT(''..'', ((LENGTH(ounder.hierarchy) - LENGTH(REPLACE(ounder.hierarchy, ''.'', '''')) - 1))), ounder.name) AS officeName, c.firstname AS firstName, c.middlename AS middleName, c.lastname AS lastName, c.display_name AS fullName, c.mobile_no AS mobileNo, o.id AS officeNumber, DATE_PART(''MONTH'', AGE(CURRENT_DATE, c.activation_date)) AS dormant FROM m_office o JOIN m_office ounder ON ounder.hierarchy LIKE CONCAT(o.hierarchy, ''%'') JOIN m_client c ON c.office_id = ounder.id LEFT JOIN r_enum_value r ON r.enum_name = ''status_enum'' AND r.enum_id = c.status_enum LEFT JOIN m_loan l ON l.client_id = c.id WHERE o.id = ''${officeId}'' AND c.status_enum = 300 AND (COALESCE(c.staff_id, -10) = ${loanOfficerId} OR ''-1'' = ${loanOfficerId}) AND l.client_id IS NULL AND (DATE_PART(''MONTH'', AGE(CURRENT_DATE, c.activation_date)) > 3) GROUP BY c.id, ounder.id, o.id ORDER BY ounder.hierarchy, c.account_no', 'All individuals who have not yet received a loan but were also entered into the system more than 3 months', 'f', 't', 'f');
INSERT INTO public.stretchy_report (id, report_name, report_type, report_subtype, report_category, report_sql, description, core_report, use_report, self_service_user_report) VALUES ('172', 'Active group leaders', 'SMS', 'NonTriggered', 'Client', 'SELECT c.id AS id, c.firstname AS firstName, c.middlename AS middleName, c.lastname AS lastName, c.display_name AS fullName, c.mobile_no AS mobileNo, CONCAT(REPEAT(''..'', ((LENGTH(ounder.hierarchy) - LENGTH(REPLACE(ounder.hierarchy, ''.'', '''')) - 1))), ounder.name) AS officeName, o.id AS officeNumber FROM m_office o JOIN m_office ounder ON ounder.hierarchy LIKE CONCAT(o.hierarchy, ''%'') JOIN m_group g ON g.office_id = ounder.id JOIN m_client c ON c.office_id = ounder.id LEFT JOIN m_group_client gc ON gc.group_id = g.id AND gc.client_id = c.id LEFT JOIN m_group_roles gr ON gr.group_id = g.id AND gr.client_id = c.id LEFT JOIN m_staff ms ON ms.id = c.staff_id LEFT JOIN r_enum_value r ON r.enum_name = ''status_enum'' AND r.enum_id = c.status_enum LEFT JOIN m_code_value cv ON cv.id = gr.role_cv_id LEFT JOIN m_code code ON code.id = cv.code_id WHERE o.id = ''${officeId}'' AND g.status_enum = 300 AND c.status_enum = 300 AND (COALESCE(c.staff_id, -10) = ${loanOfficerId} OR ''-1'' = ${loanOfficerId}) AND code.code_name = ''GROUPROLE'' AND cv.code_value = ''Leader'' GROUP BY c.id, ounder.id, o.id ORDER BY ounder.hierarchy, c.account_no', 'All active group chairmen', 'f', 't', 'f');
INSERT INTO public.stretchy_report (id, report_name, report_type, report_subtype, report_category, report_sql, description, core_report, use_report, self_service_user_report) VALUES ('173', 'Loan payments due (Overdue Loans)', 'SMS', 'NonTriggered', 'Loan', 'SELECT mc.id AS id, mc.firstname AS firstName, mc.middlename AS middleName, mc.lastname AS lastName, mc.display_name AS fullName, mc.mobile_no AS mobileNo, ml.principal_amount AS loanAmount, (COALESCE(ml.principal_outstanding_derived, 0) + COALESCE(ml.interest_outstanding_derived, 0) + COALESCE(ml.fee_charges_outstanding_derived, 0) + COALESCE(ml.penalty_charges_outstanding_derived, 0)) AS loanOutstanding, ml.principal_disbursed_derived AS loanDisbursed, laa.overdue_since_date_derived AS paymentDueDate, (COALESCE(SUM(ls.principal_amount), 0) - COALESCE(SUM(ls.principal_writtenoff_derived), 0) + COALESCE(SUM(ls.interest_amount), 0) - COALESCE(SUM(ls.interest_writtenoff_derived), 0) - COALESCE(SUM(ls.interest_waived_derived), 0) + COALESCE(SUM(ls.fee_charges_amount), 0) - COALESCE(SUM(ls.fee_charges_writtenoff_derived), 0) - COALESCE(SUM(ls.fee_charges_waived_derived), 0) + COALESCE(SUM(ls.penalty_charges_amount), 0) - COALESCE(SUM(ls.penalty_charges_writtenoff_derived), 0) - COALESCE(SUM(ls.penalty_charges_waived_derived), 0)) AS totalDue, laa.total_overdue_derived AS totalOverdue, ounder.id AS officeNumber, ml.account_no AS loanAccountId, gua.lastname AS guarantorLastName, COUNT(gua.id) AS numberOfGuarantors, g.display_name AS groupName FROM m_office mo JOIN m_office ounder ON ounder.hierarchy LIKE CONCAT(mo.hierarchy, ''%'') INNER JOIN m_client mc ON mc.office_id = ounder.id INNER JOIN m_loan ml ON ml.client_id = mc.id INNER JOIN r_enum_value rev ON rev.enum_id = ml.loan_status_id AND rev.enum_name = ''loan_status_id'' INNER JOIN m_loan_arrears_aging laa ON laa.loan_id = ml.id LEFT JOIN m_loan_repayment_schedule ls ON ls.loan_id = ml.id LEFT JOIN m_currency cur ON cur.code = ml.currency_code LEFT JOIN m_group_client gc ON gc.client_id = mc.id LEFT JOIN m_group g ON g.id = gc.group_id LEFT JOIN m_staff lo ON lo.id = ml.loan_officer_id LEFT JOIN m_guarantor gua ON gua.loan_id = ml.id WHERE ml.loan_status_id = 300 AND mo.id = ''${officeId}'' AND (COALESCE(ml.loan_officer_id, -10) = ${loanOfficerId} OR ''-1'' = ${loanOfficerId}) AND (EXTRACT(DAY FROM (CURRENT_DATE - ls.duedate::TIMESTAMP)) BETWEEN ${fromX} AND ${toY}) AND (EXTRACT(DAY FROM (CURRENT_DATE - laa.overdue_since_date_derived::TIMESTAMP)) BETWEEN ${overdueX} AND ${overdueY}) GROUP BY ml.id, mc.id, laa.loan_id, ounder.id, gua.id, g.id ORDER BY ounder.hierarchy, ml.currency_code, mc.account_no, ml.account_no', 'Loan Payments Due between fromX to toY days for clients in arrears between overdueX and overdueY days', 'f', 't', 'f');
INSERT INTO public.stretchy_report (id, report_name, report_type, report_subtype, report_category, report_sql, description, core_report, use_report, self_service_user_report) VALUES ('186', 'Savings Deposit', 'SMS', 'Triggered', NULL, 'SELECT sc.savingsId AS savingsId, sc.id AS clientId, sc.firstname, COALESCE(sc.middlename,'''') AS middlename, sc.lastname, sc.display_name AS FullName, sc.mobile_no AS mobileNo, ms."account_no" AS savingsAccountNo, ROUND(mst.amountPaid, ms.currency_digits) AS depositAmount, ms.account_balance_derived AS balance, mst.transactionDate AS transactionDate FROM m_office mo JOIN m_office ounder ON ounder.hierarchy LIKE CONCAT(mo.hierarchy, ''%'') AND ounder.hierarchy LIKE CONCAT(''.'', ''%'') LEFT JOIN (SELECT sa.id AS savingsId, mc.id AS id, mc.firstname AS firstname, mc.middlename AS middlename, mc.lastname AS lastname, mc.display_name AS display_name, mc.status_enum AS status_enum, mc.mobile_no AS mobile_no, mc.office_id AS office_id, mc.staff_id AS staff_id FROM m_savings_account sa LEFT JOIN m_client mc ON mc.id = sa.client_id ORDER BY savingsId) sc ON sc.office_id = ounder.id RIGHT JOIN m_savings_account AS ms ON sc.savingsId = ms.id RIGHT JOIN(SELECT st.amount AS amountPaid, st.id, st.savings_account_id, st.id AS savingsTransactionId, st.transaction_date AS transactionDate FROM m_savings_account_transaction st WHERE st.is_reversed = false GROUP BY st.savings_account_id, st.amount, st.id) AS mst ON mst.savings_account_id = ms.id WHERE sc.mobile_no IS NOT NULL AND (mo.id = ''${officeId}'' OR ''${officeId}'' = -1) AND (sc.staff_id = ${loanOfficerId} OR ${loanOfficerId} = -1) AND mst.savingsTransactionId = ${savingsTransactionId}', 'Savings Deposit', 'f', 't', 'f');
INSERT INTO public.stretchy_report (id, report_name, report_type, report_subtype, report_category, report_sql, description, core_report, use_report, self_service_user_report) VALUES ('176', 'Happy Birthday', 'SMS', 'NonTriggered', 'Client', 'SELECT c.id AS id, c.firstname AS firstName, c.middlename AS middleName,  c.lastname AS lastName, c.display_name AS fullName,  c.mobile_no AS mobileNo, CONCAT(REPEAT(''..'', ((LENGTH(ounder.hierarchy) - LENGTH( REPLACE(ounder.hierarchy, ''.'', '''')) - 1))), ounder.name) AS officeName,   o.id AS officeNumber, c.date_of_birth AS dateOfBirth,  CASE WHEN c.date_of_birth IS NULL THEN 0 ELSE CEIL(EXTRACT(DAY FROM (CURRENT_DATE - c.date_of_birth))/365) END AS age  FROM m_office o  JOIN m_office ounder ON ounder.hierarchy LIKE CONCAT(o.hierarchy, ''%'')  JOIN m_client c ON c.office_id = ounder.id  LEFT JOIN r_enum_value r ON r.enum_name = ''status_enum'' AND r.enum_id = c.status_enum  LEFT JOIN m_staff ms ON ms.id = c.staff_id  WHERE o.id = ''${officeId}'' AND c.status_enum = 300 AND (COALESCE(c.staff_id, -10) = -1 OR ''-1'' = -1) AND c.date_of_birth IS NOT NULL AND (DATE_TRUNC(''day'', c.date_of_birth)=DATE_TRUNC(''day'', NOW())) AND (DATE_TRUNC(''month'', c.date_of_birth)=DATE_TRUNC(''month'', NOW()))  ORDER BY ounder.hierarchy, c.account_no', 'This sends a message to all clients with the status Active on their Birthday', 'f', 't', 'f');
INSERT INTO public.stretchy_report (id, report_name, report_type, report_subtype, report_category, report_sql, description, core_report, use_report, self_service_user_report) VALUES ('177', 'Loan fully repaid', 'SMS', 'NonTriggered', 'Loan', 'SELECT c.id AS id, c.firstname AS firstName, c.middlename AS middleName, c.lastname AS lastName, c.display_name AS fullName, c.mobile_no AS mobileNo, l.principal_amount AS loanAmount, (COALESCE(l.principal_outstanding_derived, 0) + COALESCE(l.interest_outstanding_derived, 0) + COALESCE(l.fee_charges_outstanding_derived, 0) + COALESCE(l.penalty_charges_outstanding_derived, 0)) AS loanOutstanding, l.principal_disbursed_derived AS loanDisbursed, o.id AS officeNumber, l.account_no AS loanAccountId, gua.lastname AS guarantorLastName, COUNT(gua.id) AS numberOfGuarantors, ls.duedate AS dueDate, laa.total_overdue_derived AS totalDue, gp.display_name AS groupName, l.total_repayment_derived AS "totalFullyPaid" FROM m_office o JOIN m_office ounder ON ounder.hierarchy LIKE CONCAT(o.hierarchy, ''%'') JOIN m_client c ON c.office_id = ounder.id JOIN m_loan l ON l.client_id = c.id LEFT JOIN m_staff lo ON lo.id = l.loan_officer_id LEFT JOIN m_currency cur ON cur.code = l.currency_code LEFT JOIN m_group_client gc ON gc.client_id = c.id LEFT JOIN m_group gp ON gp.id = l.group_id LEFT JOIN m_loan_repayment_schedule ls ON l.id = ls.loan_id LEFT JOIN m_guarantor gua ON gua.loan_id = l.id LEFT JOIN m_loan_arrears_aging laa ON laa.loan_id = l.id WHERE o.id = ''${officeId}'' AND (COALESCE(l.loan_officer_id, -10) = ${loanOfficerId} OR ''-1'' = ${loanOfficerId}) AND (EXTRACT(DAY FROM (CURRENT_DATE - l.closedon_date::TIMESTAMP)) BETWEEN ${fromX} AND ${toY}) AND (l.loan_status_id IN (600, 700)) GROUP BY l.id, c.id, o.id, gua.id, ls.id,laa.loan_id, gp.id, ounder.id ORDER BY ounder.hierarchy, l.currency_code, c.account_no, l.account_no', 'All loans that have been fully repaid (Closed or Overpaid) in the last fromX to toY days', 'f', 't', 'f');
INSERT INTO public.stretchy_report (id, report_name, report_type, report_subtype, report_category, report_sql, description, core_report, use_report, self_service_user_report) VALUES ('178', 'Loan outstanding after final instalment date', 'SMS', 'NonTriggered', 'Loan', 'SELECT c.id AS id, c.firstname AS firstName, c.middlename AS middleName, c.lastname AS lastName, c.display_name AS fullName, c.mobile_no AS mobileNo, l.principal_amount AS loanAmount, o.id AS officeNumber, (COALESCE(l.principal_outstanding_derived, 0) + COALESCE(l.interest_outstanding_derived, 0) + COALESCE(l.fee_charges_outstanding_derived, 0) + COALESCE(l.penalty_charges_outstanding_derived, 0)) AS loanOutstanding, l.principal_disbursed_derived AS loanDisbursed, ls.duedate AS paymentDueDate, (COALESCE(SUM(ls.principal_amount), 0) - COALESCE(SUM(ls.principal_writtenoff_derived), 0) + COALESCE(SUM(ls.interest_amount), 0) - COALESCE(SUM(ls.interest_writtenoff_derived), 0) - COALESCE(SUM(ls.interest_waived_derived), 0) + COALESCE(SUM(ls.fee_charges_amount), 0) - COALESCE(SUM(ls.fee_charges_writtenoff_derived), 0) - COALESCE(SUM(ls.fee_charges_waived_derived), 0) + COALESCE(SUM(ls.penalty_charges_amount), 0) - COALESCE(SUM(ls.penalty_charges_writtenoff_derived), 0) - COALESCE(SUM(ls.penalty_charges_waived_derived), 0)) AS totalDue, laa.total_overdue_derived AS totalOverdue, l.account_no AS loanAccountId, gua.lastname AS guarantorLastName, COUNT(gua.id) AS numberOfGuarantors, gp.display_name AS groupName FROM m_office o JOIN m_office ounder ON ounder.hierarchy LIKE CONCAT(o.hierarchy, ''%'') JOIN m_client c ON c.office_id = ounder.id JOIN m_loan l ON l.client_id = c.id LEFT JOIN m_staff lo ON lo.id = l.loan_officer_id LEFT JOIN m_currency cur ON cur.code = l.currency_code LEFT JOIN m_loan_arrears_aging laa ON laa.loan_id = l.id LEFT JOIN m_group_client gc ON gc.client_id = c.id LEFT JOIN m_group gp ON gp.id = l.group_id LEFT JOIN m_loan_repayment_schedule ls ON l.id = ls.loan_id LEFT JOIN m_guarantor gua ON gua.loan_id = l.id WHERE o.id = ''${officeId}'' AND (COALESCE(l.loan_officer_id, -10) = ${loanOfficerId} OR ''-1'' = ${loanOfficerId}) AND l.loan_status_id = 300 AND l.expected_maturedon_date < CURRENT_DATE AND (EXTRACT(DAY FROM (CURRENT_DATE - l.expected_maturedon_date::TIMESTAMP)) BETWEEN ${fromX} AND ${toY}) GROUP BY l.id, c.id, o.id, ls.id, laa.loan_id, gua.id, gp.id, ounder.id ORDER BY ounder.hierarchy, l.currency_code, c.account_no, l.account_no', 'All active loans (with an outstanding balance) between fromX to toY days after the final instalment date on their loan schedule', 'f', 't', 'f');
INSERT INTO public.stretchy_report (id, report_name, report_type, report_subtype, report_category, report_sql, description, core_report, use_report, self_service_user_report) VALUES ('180', 'Loan Approved', 'SMS', 'Triggered', NULL, 'SELECT mc.id, mc.firstname, mc.middlename as middlename, mc.lastname, mc.display_name as FullName, mc.mobile_no as mobileNo, mc.group_name as GroupName, mo.name as officename, ml.id as loanId, ml.account_no as accountnumber, ml.principal_amount_proposed as loanamount, ml.annual_nominal_interest_rate as annualinterestrate FROM m_office mo JOIN m_office ounder ON ounder.hierarchy LIKE CONCAT(mo.hierarchy, ''%'') AND ounder.hierarchy like CONCAT(''.'', ''%'') LEFT JOIN ( select  ml.id as loanId, COALESCE(mc.id,mc2.id) as id, COALESCE(mc.firstname,mc2.firstname) as firstname, COALESCE(mc.middlename,COALESCE(mc2.middlename,(''''))) as middlename,  COALESCE(mc.lastname,mc2.lastname) as lastname,  COALESCE(mc.display_name,mc2.display_name) as display_name,  COALESCE(mc.status_enum,mc2.status_enum) as status_enum, COALESCE(mc.mobile_no,mc2.mobile_no) as mobile_no, COALESCE(mg.office_id,mc2.office_id) as office_id, COALESCE(mg.staff_id,mc2.staff_id) as staff_id, mg.id as group_id, mg.display_name as group_name from m_loan ml left join m_group mg on mg.id = ml.group_id left join m_group_client mgc on mgc.group_id = mg.id left join m_client mc on mc.id = mgc.client_id left join m_client mc2 on mc2.id = ml.client_id order by loanId ) mc on mc.office_id = ounder.id  left join m_loan ml on ml.id = mc.loanId WHERE mc.status_enum = 300 and mc.mobile_no is not null and (mo.id = ''${officeId}'' or ''${officeId}'' = -1) and (mc.staff_id = ${loanOfficerId} or ${loanOfficerId} = -1)and (ml.id = ${loanId} or ${loanId} = -1)and (mc.id = ${clientId} or ${clientId} = -1)and (mc.group_id = ${groupId} or ${groupId} = -1)and (ml.loan_type_enum = ${loanType} or ${loanType} = -1)', 'Loan and client data of approved loan', 'f', 'f', 'f');
INSERT INTO public.stretchy_report (id, report_name, report_type, report_subtype, report_category, report_sql, description, core_report, use_report, self_service_user_report) VALUES ('181', 'Loan Rejected', 'SMS', 'Triggered', NULL, 'SELECT mc.id, mc.firstname, mc.middlename as middlename, mc.lastname, mc.display_name as FullName, mc.mobile_no as mobileNo, mc.group_name as GroupName,  mo.name as officename, ml.id as loanId, ml.account_no as accountnumber, ml.principal_amount_proposed as loanamount, ml.annual_nominal_interest_rate as annualinterestrate  FROM m_office mo  JOIN m_office ounder ON ounder.hierarchy LIKE CONCAT(mo.hierarchy, ''%'')  AND ounder.hierarchy like CONCAT(''.'', ''%'')  LEFT JOIN (  select   ml.id as loanId, COALESCE(mc.id,mc2.id) as id, COALESCE(mc.firstname,mc2.firstname) as firstname, COALESCE(mc.middlename,COALESCE(mc2.middlename,(''''))) as middlename,   COALESCE(mc.lastname,mc2.lastname) as lastname,   COALESCE(mc.display_name,mc2.display_name) as display_name,   COALESCE(mc.status_enum,mc2.status_enum) as status_enum,  COALESCE(mc.mobile_no,mc2.mobile_no) as mobile_no,  COALESCE(mg.office_id,mc2.office_id) as office_id,  COALESCE(mg.staff_id,mc2.staff_id) as staff_id, mg.id as group_id,  mg.display_name as group_name  from m_loan ml  left join m_group mg on mg.id = ml.group_id  left join m_group_client mgc on mgc.group_id = mg.id  left join m_client mc on mc.id = mgc.client_id  left join m_client mc2 on mc2.id = ml.client_id  order by loanId  ) mc on mc.office_id = ounder.id  left join m_loan ml on ml.id = mc.loanId  WHERE mc.status_enum = 300 and mc.mobile_no is not null  and (mo.id = ''${officeId}'' or ''${officeId}'' = -1)  and (mc.staff_id = ${loanOfficerId} or ${loanOfficerId} = -1) and (ml.id = ${loanId} or ${loanId} = -1) and (mc.id = ${clientId} or ${clientId} = -1) and (mc.group_id = ${groupId} or ${groupId} = -1)  and (ml.loan_type_enum = ${loanType} or ${loanType} = -1)', 'Loan and client data of rejected loan', 'f', 'f', 'f');
INSERT INTO public.stretchy_report (id, report_name, report_type, report_subtype, report_category, report_sql, description, core_report, use_report, self_service_user_report) VALUES ('182', 'Client Rejected', 'SMS', 'Triggered', 'Client', 'SELECT c.id AS id,   c.firstname AS firstName,  c.middlename AS middleName,  c.lastname AS lastName,  c.display_name AS fullName,  c.mobile_no AS mobileNo, CONCAT(REPEAT(''..'', ((LENGTH(ounder.hierarchy) - LENGTH( REPLACE(ounder.hierarchy, ''.'', '''')) - 1))), ounder.name) AS officeName,   o.id AS officeNumber  FROM m_office o  JOIN m_office ounder ON ounder.hierarchy LIKE CONCAT(o.hierarchy, ''%'')  JOIN m_client c ON c.office_id = ounder.id  LEFT JOIN r_enum_value r ON r.enum_name = ''status_enum'' AND r.enum_id = c.status_enum  WHERE o.id = ''${officeId}'' AND c.id = ${clientId} AND (COALESCE(c.staff_id, -10) = ${loanOfficerId} OR ''-1'' = ${loanOfficerId})', 'Client Rejection', 'f', 't', 'f');
INSERT INTO public.stretchy_report (id, report_name, report_type, report_subtype, report_category, report_sql, description, core_report, use_report, self_service_user_report) VALUES ('183', 'Client Activated', 'SMS', 'Triggered', 'Client', 'SELECT c.id AS id,   c.firstname AS firstName,  c.middlename AS middleName,  c.lastname AS lastName,  c.display_name AS fullName,  c.mobile_no AS mobileNo, CONCAT(REPEAT(''..'', ((LENGTH(ounder.hierarchy) - LENGTH( REPLACE(ounder.hierarchy, ''.'', '''')) - 1))), ounder.name) AS officeName,   o.id AS officeNumber  FROM m_office o  JOIN m_office ounder ON ounder.hierarchy LIKE CONCAT(o.hierarchy, ''%'')  JOIN m_client c ON c.office_id = ounder.id  LEFT JOIN r_enum_value r ON r.enum_name = ''status_enum'' AND r.enum_id = c.status_enum  WHERE o.id = ''${officeId}'' AND c.id = ${clientId} AND (COALESCE(c.staff_id, -10) = ${loanOfficerId} OR ''-1'' = ${loanOfficerId})', 'Client Activation', 'f', 't', 'f');
INSERT INTO public.stretchy_report (id, report_name, report_type, report_subtype, report_category, report_sql, description, core_report, use_report, self_service_user_report) VALUES ('184', 'Savings Rejected', 'SMS', 'Triggered', 'Savings', 'SELECT   c.id AS id,  c.firstname AS firstName, c.middlename AS middleName,  c.lastname AS lastName, c.display_name AS fullName,  c.mobile_no AS mobileNo, s.account_no AS savingsAccountNo,  ounder.id AS officeNumber,  ounder.name AS officeName    FROM m_office o JOIN m_office ounder ON ounder.hierarchy LIKE CONCAT(o.hierarchy, ''%'')  JOIN m_client c ON c.office_id = ounder.id  JOIN m_savings_account s ON s.client_id = c.id JOIN m_savings_product sp ON sp.id = s.product_id  LEFT JOIN m_staff st ON st.id = s.field_officer_id  LEFT JOIN m_currency cur ON cur.code = s.currency_code  WHERE o.id = ''${officeId}'' AND (COALESCE(s.field_officer_id, -10) = ${loanOfficerId} OR ''-1'' = ${loanOfficerId}) AND s.id = ${savingsId}', 'Savings Rejected', 'f', 't', 'f');
INSERT INTO public.stretchy_report (id, report_name, report_type, report_subtype, report_category, report_sql, description, core_report, use_report, self_service_user_report) VALUES ('185', 'Savings Activated', 'SMS', 'Triggered', 'Savings', 'SELECT   c.id AS id,  c.firstname AS firstName, c.middlename AS middleName,  c.lastname AS lastName, c.display_name AS fullName,  c.mobile_no AS mobileNo, s.account_no AS savingsAccountNo,  ounder.id AS officeNumber,  ounder.name AS officeName    FROM m_office o JOIN m_office ounder ON ounder.hierarchy LIKE CONCAT(o.hierarchy, ''%'')  JOIN m_client c ON c.office_id = ounder.id  JOIN m_savings_account s ON s.client_id = c.id JOIN m_savings_product sp ON sp.id = s.product_id  LEFT JOIN m_staff st ON st.id = s.field_officer_id  LEFT JOIN m_currency cur ON cur.code = s.currency_code  WHERE o.id = ''${officeId}'' AND (COALESCE(s.field_officer_id, -10) = ${loanOfficerId} OR ''-1'' = ${loanOfficerId}) AND s.id = ${savingsId}', 'Savings Activation', 'f', 't', 'f');
INSERT INTO public.stretchy_report (id, report_name, report_type, report_subtype, report_category, report_sql, description, core_report, use_report, self_service_user_report) VALUES ('192', 'Loan Repayment - Email', 'Email', 'Triggered', NULL, 'select  ml.id as loanId,  COALESCE(mc.id,mc2.id) as id,  COALESCE(mc.firstname,mc2.firstname) as firstname,   COALESCE(mc.middlename,mc2.middlename,(\''\'')) as middlename, COALESCE(mc.lastname,mc2.lastname) as lastname,   COALESCE(mc.display_name,mc2.display_name) as display_name,  COALESCE(mc.status_enum,mc2.status_enum) as status_enum,   COALESCE(mc.mobile_no,mc2.mobile_no) as mobile_no, COALESCE(mg.office_id,mc2.office_id) as office_id, COALESCE(mg.staff_id,mc2.staff_id) as staff_id,  mg.id as group_id, mg.display_name as group_name, COALESCE(mc.email_address,mc2.email_address) as emailAddress, lt.amount as repaymentAmount   from m_loan_transaction lt join m_loan ml on ml.id=lt.loan_id left join m_group mg on mg.id = ml.group_id  left join m_group_client mgc on mgc.group_id = mg.id   left join m_client mc on mc.id = mgc.client_id  left join m_client mc2 on mc2.id = ml.client_id  WHERE (mc.status_enum = 300 or mc2.status_enum = 300) and (mc.email_address is not null or mc2.email_address is not null) and ml.id = ${loanId} and lt.id = ${loanTransactionId} ', 'Loan and client data of loan repayment', 'f', 't', 'f');
INSERT INTO public.stretchy_report (id, report_name, report_type, report_subtype, report_category, report_sql, description, core_report, use_report, self_service_user_report) VALUES ('187', 'Savings Withdrawal', 'SMS', 'Triggered', NULL, 'SELECT sc.savingsId AS savingsId, sc.id AS clientId, sc.firstname, COALESCE(sc.middlename,'''') AS middlename, sc.lastname, sc.display_name AS FullName, sc.mobile_no AS mobileNo,  ms."account_no" AS savingsAccountNo, ROUND(mst.amountPaid, ms.currency_digits) AS withdrawAmount, ms.account_balance_derived AS balance, mst.transactionDate AS transactionDate FROM m_office mo JOIN m_office ounder ON ounder.hierarchy LIKE CONCAT(mo.hierarchy, ''%'') AND ounder.hierarchy LIKE CONCAT(''.'', ''%'') LEFT JOIN (SELECT sa.id AS savingsId, mc.id AS id, mc.firstname AS firstname, mc.middlename AS middlename, mc.lastname AS lastname, mc.display_name AS display_name, mc.status_enum AS status_enum, mc.mobile_no AS mobile_no, mc.office_id AS office_id, mc.staff_id AS staff_id FROM m_savings_account sa LEFT JOIN m_client mc ON mc.id = sa.client_id ORDER BY savingsId) sc ON sc.office_id = ounder.id RIGHT JOIN m_savings_account AS ms ON sc.savingsId = ms.id RIGHT JOIN(SELECT st.amount AS amountPaid, st.id, st.savings_account_id, st.id AS savingsTransactionId, st.transaction_date AS transactionDate FROM m_savings_account_transaction st WHERE st.is_reversed = false GROUP BY st.savings_account_id, st.amount, st.id) AS mst ON mst.savings_account_id = ms.id WHERE sc.mobile_no IS NOT NULL AND (mo.id = ''${officeId}'' OR ''${officeId}'' = -1) AND (sc.staff_id = ${loanOfficerId} OR ${loanOfficerId} = -1) AND mst.savingsTransactionId = ${savingsTransactionId}', 'Savings Withdrawal', 'f', 't', 'f');
INSERT INTO public.stretchy_report (id, report_name, report_type, report_subtype, report_category, report_sql, description, core_report, use_report, self_service_user_report) VALUES ('188', 'ReportCategoryList', 'Table', NULL, '(NULL)', '(NULL)', '(NULL)', 't', 't', 'f');
INSERT INTO public.stretchy_report (id, report_name, report_type, report_subtype, report_category, report_sql, description, core_report, use_report, self_service_user_report) VALUES ('189', 'FullReportList', 'Table', NULL, '(NULL)', '(NULL)', '(NULL)', 't', 't', 'f');
INSERT INTO public.stretchy_report (id, report_name, report_type, report_subtype, report_category, report_sql, description, core_report, use_report, self_service_user_report) VALUES ('190', 'Loan Approved - Email', 'Email', 'Triggered', NULL, 'select  ml.id as loanId,  COALESCE(mc.id,mc2.id) as id,  COALESCE(mc.firstname,mc2.firstname) as firstname,   COALESCE(mc.middlename,mc2.middlename,(\''\'')) as middlename, COALESCE(mc.lastname,mc2.lastname) as lastname,   COALESCE(mc.display_name,mc2.display_name) as display_name, COALESCE(mc.status_enum,mc2.status_enum) as status_enum,  COALESCE(mc.mobile_no,mc2.mobile_no) as mobile_no, COALESCE(mg.office_id,mc2.office_id) as office_id, COALESCE(mg.staff_id,mc2.staff_id) as staff_id,   mg.id as group_id, mg.display_name as group_name, COALESCE(mc.email_address,mc2.email_address) as emailAddress from m_loan ml left join m_group mg on mg.id = ml.group_id   left join m_group_client mgc on mgc.group_id = mg.id  left join m_client mc on mc.id = mgc.client_id   left join m_client mc2 on mc2.id = ml.client_id WHERE (mc.status_enum = 300 or mc2.status_enum = 300) and (mc.email_address is not null or mc2.email_address is not null) and ml.id = ${loanId}', 'Loan and client data of approved loan', 'f', 't', 'f');
INSERT INTO public.stretchy_report (id, report_name, report_type, report_subtype, report_category, report_sql, description, core_report, use_report, self_service_user_report) VALUES ('191', 'Loan Rejected - Email', 'Email', 'Triggered', NULL, 'select  ml.id as loanId,  COALESCE(mc.id,mc2.id) as id,  COALESCE(mc.firstname,mc2.firstname) as firstname,   COALESCE(mc.middlename,mc2.middlename,(\''\'')) as middlename, COALESCE(mc.lastname,mc2.lastname) as lastname,   COALESCE(mc.display_name,mc2.display_name) as display_name, COALESCE(mc.status_enum,mc2.status_enum) as status_enum,  COALESCE(mc.mobile_no,mc2.mobile_no) as mobile_no, COALESCE(mg.office_id,mc2.office_id) as office_id, COALESCE(mg.staff_id,mc2.staff_id) as staff_id,   mg.id as group_id, mg.display_name as group_name, COALESCE(mc.email_address,mc2.email_address) as emailAddress from m_loan ml left join m_group mg on mg.id = ml.group_id   left join m_group_client mgc on mgc.group_id = mg.id  left join m_client mc on mc.id = mgc.client_id   left join m_client mc2 on mc2.id = ml.client_id WHERE (mc.status_enum = 300 or mc2.status_enum = 300) and (mc.email_address is not null or mc2.email_address is not null) and ml.id = ${loanId}', 'Loan and client data of rejected loan', 'f', 't', 'f');
INSERT INTO public.stretchy_report (id, report_name, report_type, report_subtype, report_category, report_sql, description, core_report, use_report, self_service_user_report) VALUES ('193', 'Trial Balance Table', 'Table', NULL, 'Accounting', 'select * from ( select debits.glcode as "glcode", debits.name as "name", (case when debits.type = 1 or debits.type = 5 then coalesce(debits.debitamount, 0) - coalesce(credits.creditamount, 0)else null end ) as "debit", (case when debits.type = 4 or debits.type = 3 or debits.type = 2 then coalesce(credits.creditamount, 0) - coalesce(debits.debitamount, 0)else null end ) as "credit" from ( select acc_gl_account.gl_code as "glcode", name, sum(amount) as "debitamount", acc_gl_account.classification_enum as "type" from acc_gl_journal_entry, acc_gl_account where acc_gl_account.id = acc_gl_journal_entry.account_id and acc_gl_journal_entry.type_enum = 2 and acc_gl_journal_entry.entry_date between ''${startDate}'' and ''${endDate}'' and ( acc_gl_journal_entry.office_id = ${officeId} or ${officeId} = 1 ) group by glcode,acc_gl_account.name,acc_gl_account.classification_enum order by glcode ) debits left outer join ( select acc_gl_account.gl_code as "glcode", name as "name", sum(amount) as "creditamount", acc_gl_account.classification_enum as "type" from acc_gl_journal_entry, acc_gl_account where acc_gl_account.id = acc_gl_journal_entry.account_id and acc_gl_journal_entry.type_enum = 1 and acc_gl_journal_entry.entry_date between ''${startDate}'' and ''${endDate}'' and ( acc_gl_journal_entry.office_id = ${officeId} or ${officeId} = 1 ) group by glcode,acc_gl_account.classification_enum,acc_gl_account.name order by glcode ) credits on debits.glcode = credits.glcode union select credits.glcode as "glcode", credits.name as "name", (case when credits.type = 1 or credits.type = 5 then coalesce(debits.debitamount, 0) - coalesce(credits.creditamount, 0) else null end ) as "debit", (case when credits.type = 4 or credits.type = 3 or credits.type = 2 then coalesce(credits.creditamount, 0) - coalesce(debits.debitamount, 0) else null end ) as "credit" from ( select acc_gl_account.gl_code as "glcode", name, sum(amount) as "debitamount", acc_gl_account.classification_enum as "type" from acc_gl_journal_entry, acc_gl_account where acc_gl_account.id = acc_gl_journal_entry.account_id and acc_gl_journal_entry.type_enum = 2 and acc_gl_journal_entry.entry_date between ''${startDate}'' and ''${endDate}'' and ( acc_gl_journal_entry.office_id = ''${officeId}'' or ''${officeId}'' = 1 ) group by glcode,acc_gl_account.name,acc_gl_account.classification_enum order by glcode ) debits right outer join ( select acc_gl_account.gl_code as "glcode", name as "name", sum(amount) as "creditamount", acc_gl_account.classification_enum as "type" from acc_gl_journal_entry, acc_gl_account where acc_gl_account.id = acc_gl_journal_entry.account_id and acc_gl_journal_entry.type_enum = 1 and acc_gl_journal_entry.entry_date between ''${startDate}'' and ''${endDate}'' and ( acc_gl_journal_entry.office_id = ${officeId} or ${officeId} = 1 ) group by glcode,acc_gl_account.name,acc_gl_account.classification_enum order by glcode ) credits on debits.glcode = credits.glcode ) as fullouterjoinresult order by glcode', 'Trial Balance Report', 't', 't', 'f');
INSERT INTO public.stretchy_report (id, report_name, report_type, report_subtype, report_category, report_sql, description, core_report, use_report, self_service_user_report) VALUES ('194', 'GeneralLedgerReport Table', 'Table', NULL, 'Accounting', 'select details.edate entry_date, sum(details.debit_amount) debit_amount, sum(details.credit_amount) credit_amount, details.description, coalesce(opb.openingbalance, 0) openingbalance, case when details.manual_entry then cast(details.id as text) else cast(''system'' as text) end transtype, case when actype in (1, 5) then ( sum(details.debit_amount) - sum(details.credit_amount) ) else ( sum(details.credit_amount) - sum(details.debit_amount) ) end as cumulative_sum from ( select a.account_id acid1, concat(gl.gl_code, ''-'', gl.name) as report_header, gl.classification_enum actype, gl.gl_code as reportid, j1.entry_date edate, concat(gl1.gl_code, ''-'', gl1.name) as account_name, case when j1.type_enum = 1 then j1.amount else 0 end as debit_amount, case when j1.type_enum = 2 then j1.amount else 0 end as credit_amount, j1.id, j1.office_id, j1.transaction_id, j1.type_enum, j1.office_running_balance as aftertxn, j1.description as description, j1.transaction_id as transactionid, a.manual_entry from acc_gl_journal_entry j1 inner join ( select distinct je.transaction_id tid, je.account_id, je.manual_entry from m_office o left join m_office ounder on ounder.hierarchy like concat(o.hierarchy, ''%'') inner join acc_gl_journal_entry je on je.office_id = ounder.id where je.account_id = cast(''${GLAccountNO}'' as BIGINT) and o.id = ''${officeId}'' and je.entry_date between ''${startDate}'' and ''${endDate}'' ) a on a.tid = j1.transaction_id and j1.account_id <> cast(''${GLAccountNO}'' as BIGINT) left join acc_gl_account gl on gl.id = a.account_id left join acc_gl_account gl1 on gl1.id = j1.account_id order by j1.entry_date, j1.id ) details left join ( select je.account_id acid2, case when aga1.classification_enum in (1, 5) then (( sum(case when je.type_enum = 2 then coalesce(je.amount, 0) else 0 end)) - sum(case when je.type_enum = 1 then coalesce(je.amount, 0) else 0 end)) else( sum(case when je.type_enum = 1 then coalesce(je.amount, 0) else 0 end) - sum(case when je.type_enum = 2 then coalesce(je.amount, 0) else 0 end)) end as openingbalance from m_office o left join m_office ounder on ounder.hierarchy like concat(o.hierarchy, ''%'') left join acc_gl_journal_entry je on je.office_id = ounder.id left join acc_gl_account aga1 on aga1.id = je.account_id where je.entry_date <= date(''${startDate}'')- interval ''3 day'' and je.office_running_balance is not null and (o.id = ''${officeId}'') and je.account_id = cast(''${GLAccountNO}'' as BIGINT) group by je.account_id, aga1.classification_enum ) opb on opb.acid2 = details.acid1 left join ( select name branchname from m_office mo where mo.id = 1 ) branch on details.office_id = ''${officeId}'' group by details.edate, details.acid1, details.report_header, details.reportid, details.account_name, branch.branchname, transtype, details.description, openingbalance, details.actype', NULL, 'f', 't', 'f');
INSERT INTO public.stretchy_report (id, report_name, report_type, report_subtype, report_category, report_sql, description, core_report, use_report, self_service_user_report) VALUES ('195', 'Income Statement Table', 'Table', NULL, 'Accounting', '( select * from ( select debits.glcode as "glcode", debits.name as "name", ''Expense'' as IncomeOrExpense, ( coalesce(debits.debitamount, 0) - coalesce(credits.creditamount, 0) ) as "balance" from ( select acc_gl_account.gl_code as "glcode", name, sum(amount) as "debitamount" from acc_gl_journal_entry, acc_gl_account where acc_gl_account.id = acc_gl_journal_entry.account_id and acc_gl_journal_entry.type_enum = 2 and acc_gl_account.classification_enum in (5) and acc_gl_journal_entry.entry_date between ''${startDate}'' and ''${endDate}'' and ( acc_gl_journal_entry.office_id = ''${officeId}'' or ''${officeId}'' = 1 ) group by gl_code,acc_gl_account.name order by glcode ) debits LEFT OUTER JOIN ( select acc_gl_account.gl_code as "glcode", name, sum(amount) as "creditamount" from acc_gl_journal_entry, acc_gl_account where acc_gl_account.id = acc_gl_journal_entry.account_id and acc_gl_journal_entry.type_enum = 1 and acc_gl_account.classification_enum in (5) and acc_gl_journal_entry.entry_date between ''${startDate}'' and ''${endDate}'' and ( acc_gl_journal_entry.office_id = ''${officeId}'' or ''${officeId}'' = 1 ) group by gl_code,acc_gl_account.name ) credits on debits.glcode = credits.glcode union select credits.glcode as "glcode", credits.name as "name", ''Expense'' as IncomeOrExpense, ( coalesce(debits.debitamount, 0) - coalesce(credits.creditamount, 0) ) as "balance" from ( select acc_gl_account.gl_code as "glcode", name, sum(amount) as "debitamount" from acc_gl_journal_entry, acc_gl_account where acc_gl_account.id = acc_gl_journal_entry.account_id and acc_gl_journal_entry.type_enum = 2 and acc_gl_account.classification_enum in (5) and acc_gl_journal_entry.entry_date between ''${startDate}'' and ''${endDate}'' and ( acc_gl_journal_entry.office_id = ''${officeId}'' or ''${officeId}'' = 1 ) group by gl_code , acc_gl_account.name order by glcode ) debits RIGHT OUTER JOIN ( select acc_gl_account.gl_code as "glcode", name as "name", sum(amount) as "creditamount" from acc_gl_journal_entry, acc_gl_account where acc_gl_account.id = acc_gl_journal_entry.account_id and acc_gl_journal_entry.type_enum = 1 and acc_gl_account.classification_enum in (5) and acc_gl_journal_entry.entry_date between ''${startDate}'' and ''${endDate}'' and ( acc_gl_journal_entry.office_id = ''${officeId}'' or ''${officeId}'' = 1 ) group by gl_code , acc_gl_account.name order by glcode ) credits on debits.glcode = credits.glcode ) as fullouterjoinresult order by glcode ) UNION ( select * from ( select debits.glcode as "glcode", debits.name as "name", ''Income'' as IncomeOrExpense, ( coalesce(credits.creditamount, 0) - coalesce(debits.debitamount, 0) ) as "balance" from ( select acc_gl_account.gl_code as "glcode", name, sum(amount) as "debitamount" from acc_gl_journal_entry, acc_gl_account where acc_gl_account.id = acc_gl_journal_entry.account_id and acc_gl_journal_entry.type_enum = 2 and acc_gl_account.classification_enum in (4) and acc_gl_journal_entry.entry_date between ''${startDate}'' and ''${endDate}'' and ( acc_gl_journal_entry.office_id = ''${officeId}'' or ''${officeId}'' = 1 ) group by glcode, acc_gl_account.name order by glcode ) debits LEFT OUTER JOIN ( select acc_gl_account.gl_code as "glcode", name, sum(amount) as "creditamount" from acc_gl_journal_entry, acc_gl_account where acc_gl_account.id = acc_gl_journal_entry.account_id and acc_gl_journal_entry.type_enum = 1 and acc_gl_account.classification_enum in (4) and acc_gl_journal_entry.entry_date between ''${startDate}'' and ''${endDate}'' and ( acc_gl_journal_entry.office_id = ''${officeId}'' or ''${officeId}'' = 1 ) group by glcode , acc_gl_account.name order by glcode ) credits on debits.glcode = credits.glcode union select credits.glcode as "glcode", credits.name as "name", ''Income'' as IncomeOrExpense, ( coalesce(credits.creditamount, 0) - coalesce(debits.debitamount, 0) ) as "balance" from ( select acc_gl_account.gl_code as "glcode", name, sum(amount) as "debitamount" from acc_gl_journal_entry, acc_gl_account where acc_gl_account.id = acc_gl_journal_entry.account_id and acc_gl_journal_entry.type_enum = 2 and acc_gl_account.classification_enum in (4) and acc_gl_journal_entry.entry_date between ''${startDate}'' and ''${endDate}'' and ( acc_gl_journal_entry.office_id = ''${officeId}'' or ''${officeId}'' = 1 ) group by glcode ,acc_gl_account.name order by glcode ) debits RIGHT OUTER JOIN ( select acc_gl_account.gl_code as glcode, name as "name", sum(amount) as creditamount from acc_gl_journal_entry, acc_gl_account where acc_gl_account.id = acc_gl_journal_entry.account_id and acc_gl_journal_entry.type_enum = 1 and acc_gl_account.classification_enum in (4) and acc_gl_journal_entry.entry_date between ''${startDate}'' and ''${endDate}'' and ( acc_gl_journal_entry.office_id = ''${officeId}'' or ''${officeId}'' = 1 ) group by glcode ,acc_gl_account.name order by glcode ) credits on debits.glcode = credits.glcode ) as fullouterjoinresult order by glcode )', 'Profit and Loss Statement', 't', 't', 'f');
INSERT INTO public.stretchy_report (id, report_name, report_type, report_subtype, report_category, report_sql, description, core_report, use_report, self_service_user_report) VALUES ('196', 'Balance Sheet Table', 'Table', NULL, 'Accounting', ' ( select debits.glcode as glcode, debits.name as "name", ''Assets'' as BalanceType, ( coalesce(debits.debitamount, 0) - coalesce(credits.creditamount, 0) ) as balance from ( select acc_gl_account.gl_code as glcode, name, sum(amount) as debitamount from acc_gl_journal_entry, acc_gl_account where acc_gl_account.id = acc_gl_journal_entry.account_id and acc_gl_journal_entry.type_enum = 2 and acc_gl_account.classification_enum in (1) and acc_gl_journal_entry.entry_date <= ''${endDate}'' and ( acc_gl_journal_entry.office_id = ''${officeId}'' or ''${officeId}'' = 1 ) group by glcode, acc_gl_account.name order by glcode ) debits left outer join ( select acc_gl_account.gl_code as glcode, name, sum(amount) as creditamount from acc_gl_journal_entry, acc_gl_account where acc_gl_account.id = acc_gl_journal_entry.account_id and acc_gl_journal_entry.type_enum = 1 and acc_gl_account.classification_enum in (1) and acc_gl_journal_entry.entry_date <= ''${endDate}'' and ( acc_gl_journal_entry.office_id = ''${officeId}'' or ''${officeId}'' = 1 ) group by glcode, acc_gl_account.name order by glcode ) credits on debits.glcode = credits.glcode union select credits.glcode as glcode, credits.name as "name", ''Assets'' as BalanceType, ( coalesce(debits.debitamount, 0) - coalesce(credits.creditamount, 0) ) as balance from ( select acc_gl_account.gl_code as glcode, name, sum(amount) as debitamount from acc_gl_journal_entry, acc_gl_account where acc_gl_account.id = acc_gl_journal_entry.account_id and acc_gl_journal_entry.type_enum = 2 and acc_gl_account.classification_enum in (1) and acc_gl_journal_entry.entry_date <= ''${endDate}'' and ( acc_gl_journal_entry.office_id = ''${officeId}'' or ''${officeId}'' = 1 ) group by glcode, acc_gl_account.name order by glcode ) debits right outer join ( select acc_gl_account.gl_code as glcode, name, sum(amount) as creditamount from acc_gl_journal_entry, acc_gl_account where acc_gl_account.id = acc_gl_journal_entry.account_id and acc_gl_journal_entry.type_enum = 1 and acc_gl_account.classification_enum in (1) and acc_gl_journal_entry.entry_date <= ''${endDate}'' and ( acc_gl_journal_entry.office_id = ''${officeId}'' or ''${officeId}'' = 1 ) group by glcode, acc_gl_account.name order by glcode ) credits on debits.glcode = credits.glcode union select debits.glcode as glcode, debits.name as "name", ''Liability'' as BalanceType, ( coalesce(credits.creditamount, 0) - coalesce(debits.debitamount, 0) ) as balance from ( select acc_gl_account.gl_code as glcode, name, sum(amount) as debitamount from acc_gl_journal_entry, acc_gl_account where acc_gl_account.id = acc_gl_journal_entry.account_id and acc_gl_journal_entry.type_enum = 2 and acc_gl_account.classification_enum in (2) and acc_gl_journal_entry.entry_date <= ''${endDate}'' and ( acc_gl_journal_entry.office_id = ''${officeId}'' or ''${officeId}'' = 1 ) group by glcode, acc_gl_account.name order by glcode ) debits left outer join ( select acc_gl_account.gl_code as glcode, name, sum(amount) as creditamount from acc_gl_journal_entry, acc_gl_account where acc_gl_account.id = acc_gl_journal_entry.account_id and acc_gl_journal_entry.type_enum = 1 and acc_gl_account.classification_enum in (2) and acc_gl_journal_entry.entry_date <= ''${endDate}'' and ( acc_gl_journal_entry.office_id = ''${officeId}'' or ''${officeId}'' = 1 ) group by glcode, acc_gl_account.name order by glcode ) credits on debits.glcode = credits.glcode union select credits.glcode as glcode, credits.name as "name", ''Liability'' as BalanceType, ( coalesce(credits.creditamount, 0) - coalesce(debits.debitamount, 0) ) as "balance" from ( select acc_gl_account.gl_code as "glcode", name, sum(amount) as "debitamount" from acc_gl_journal_entry, acc_gl_account where acc_gl_account.id = acc_gl_journal_entry.account_id and acc_gl_journal_entry.type_enum = 2 and acc_gl_account.classification_enum in (2) and acc_gl_journal_entry.entry_date <= ''${endDate}'' and ( acc_gl_journal_entry.office_id = ''${officeId}'' or ''${officeId}'' = 1 ) group by glcode, acc_gl_account.name order by glcode ) debits right outer join ( select acc_gl_account.gl_code as "glcode", name, sum(amount) as "creditamount" from acc_gl_journal_entry, acc_gl_account where acc_gl_account.id = acc_gl_journal_entry.account_id and acc_gl_journal_entry.type_enum = 1 and acc_gl_account.classification_enum in (2) and acc_gl_journal_entry.entry_date <= ''${endDate}'' and ( acc_gl_journal_entry.office_id = ''${officeId}'' or ''${officeId}'' = 1 ) group by glcode, acc_gl_account.name order by glcode ) credits on debits.glcode = credits.glcode union select debits.glcode as "glcode", debits.name as "name", ''Equity'' as BalanceType, ( coalesce(credits.creditamount, 0) - coalesce(debits.debitamount, 0) ) as "balance" from ( select acc_gl_account.gl_code as glcode, name, sum(amount) as debitamount from acc_gl_journal_entry, acc_gl_account where acc_gl_account.id = acc_gl_journal_entry.account_id and acc_gl_journal_entry.type_enum = 2 and acc_gl_account.classification_enum in (3) and acc_gl_journal_entry.entry_date <= ''${endDate}'' and ( acc_gl_journal_entry.office_id = ''${officeId}'' or ''${officeId}'' = 1 ) group by glcode, acc_gl_account.name order by glcode ) debits left outer join ( select acc_gl_account.gl_code as glcode, name, sum(amount) as creditamount from acc_gl_journal_entry, acc_gl_account where acc_gl_account.id = acc_gl_journal_entry.account_id and acc_gl_journal_entry.type_enum = 1 and acc_gl_account.classification_enum in (3) and acc_gl_journal_entry.entry_date <= ''${endDate}'' and ( acc_gl_journal_entry.office_id = ''${officeId}'' or ''${officeId}'' = 1 ) group by glcode, acc_gl_account.name order by glcode ) credits on debits.glcode = credits.glcode union select credits.glcode as glcode, credits.name as "name", ''Equity'' as BalanceType, ( coalesce(credits.creditamount, 0) - coalesce(debits.debitamount, 0) ) as balance from ( select acc_gl_account.gl_code as glcode, name, sum(amount) as debitamount from acc_gl_journal_entry, acc_gl_account where acc_gl_account.id = acc_gl_journal_entry.account_id and acc_gl_journal_entry.type_enum = 2 and acc_gl_account.classification_enum in (3) and acc_gl_journal_entry.entry_date <= ''${endDate}'' and ( acc_gl_journal_entry.office_id = ''${officeId}'' or ''${officeId}'' = 1 ) group by glcode, acc_gl_account.name order by glcode ) debits right outer join ( select acc_gl_account.gl_code as glcode, name, sum(amount) as creditamount from acc_gl_journal_entry, acc_gl_account where acc_gl_account.id = acc_gl_journal_entry.account_id and acc_gl_journal_entry.type_enum = 1 and acc_gl_account.classification_enum in (3) and acc_gl_journal_entry.entry_date <= ''${endDate}'' and ( acc_gl_journal_entry.office_id = ''${officeId}'' or ''${officeId}'' = 1 ) group by glcode, acc_gl_account.name order by glcode ) credits on debits.glcode = credits.glcode )', 'Balance Sheet', 't', 't', 'f');
INSERT INTO public.stretchy_report (id, report_name, report_type, report_subtype, report_category, report_sql, description, core_report, use_report, self_service_user_report) VALUES ('174', 'Loan payments received (Active Loans)', 'SMS', 'NonTriggered', 'Loan', 'SELECT mc.id AS id, mc.firstname AS firstName, mc.middlename AS middleName, mc.lastname AS lastName, mc.display_name AS fullName, mc.mobile_no AS mobileNo, ml.principal_amount AS loanAmount, (COALESCE(ml.principal_outstanding_derived, 0) + COALESCE(ml.interest_outstanding_derived, 0) + COALESCE(ml.fee_charges_outstanding_derived, 0) + COALESCE(ml.penalty_charges_outstanding_derived, 0)) AS loanOutstanding, ounder.id AS officeNumber, ml.account_no AS loanAccountNumber, SUM(lt.amount) AS repaymentAmount FROM m_office mo JOIN m_office ounder ON ounder.hierarchy LIKE CONCAT(mo.hierarchy, ''%'') INNER JOIN m_client mc ON mc.office_id = ounder.id INNER JOIN m_loan ml ON ml.client_id = mc.id INNER JOIN r_enum_value rev ON rev.enum_id = ml.loan_status_id AND rev.enum_name = ''loan_status_id'' INNER JOIN m_loan_transaction lt ON lt.loan_id = ml.id INNER JOIN m_appuser au ON au.id = lt.created_by LEFT JOIN m_loan_arrears_aging laa ON laa.loan_id = ml.id LEFT JOIN m_payment_detail mpd ON mpd.id = lt.payment_detail_id LEFT JOIN m_currency cur ON cur.code = ml.currency_code LEFT JOIN m_group_client gc ON gc.client_id = mc.id LEFT JOIN m_group g ON g.id = gc.group_id LEFT JOIN m_staff lo ON lo.id = ml.loan_officer_id LEFT JOIN m_guarantor gua ON gua.loan_id = ml.id WHERE ml.loan_status_id = 300 AND mo.id = ''${officeId}'' AND (COALESCE(ml.loan_officer_id, -10) = ${loanOfficerId} OR ''-1'' = ${loanOfficerId}) AND (EXTRACT(DAY FROM (CURRENT_DATE - lt.transaction_date::TIMESTAMP)) BETWEEN 9${fromX} AND ${toY}) AND lt.is_reversed = false AND lt.transaction_type_enum = 2 AND laa.loan_id IS NULL GROUP BY ml.id, mc.id, ounder.id ORDER BY ounder.hierarchy, ml.currency_code, mc.account_no, ml.account_no', 'Payments received in the last fromX to toY days for any loan with the status Active (on-time)', 'f', 't', 'f');
INSERT INTO public.stretchy_report (id, report_name, report_type, report_subtype, report_category, report_sql, description, core_report, use_report, self_service_user_report) VALUES ('175', 'Loan payments received (Overdue Loans)', 'SMS', 'NonTriggered', 'Loan', 'SELECT ml.id AS loanId, mc.id AS id, mc.firstname AS firstName, mc.middlename AS middleName, mc.lastname AS lastName, mc.display_name AS fullName, mc.mobile_no AS mobileNo, ml.principal_amount AS loanAmount, (COALESCE(ml.principal_outstanding_derived, 0) + COALESCE(ml.interest_outstanding_derived, 0) + COALESCE(ml.fee_charges_outstanding_derived, 0) + COALESCE(ml.penalty_charges_outstanding_derived, 0)) AS loanOutstanding, ounder.id AS officeNumber, ml.account_no AS loanAccountNumber, SUM(lt.amount) AS repaymentAmount FROM m_office mo JOIN m_office ounder ON ounder.hierarchy LIKE CONCAT(mo.hierarchy, ''%'') INNER JOIN m_client mc ON mc.office_id = ounder.id INNER JOIN m_loan ml ON ml.client_id = mc.id INNER JOIN r_enum_value rev ON rev.enum_id = ml.loan_status_id AND rev.enum_name = ''loan_status_id'' INNER JOIN m_loan_arrears_aging laa ON laa.loan_id = ml.id INNER JOIN m_loan_transaction lt ON lt.loan_id = ml.id INNER JOIN m_appuser au ON au.id = lt.created_by LEFT JOIN m_payment_detail mpd ON mpd.id = lt.payment_detail_id LEFT JOIN m_currency cur ON cur.code = ml.currency_code LEFT JOIN m_group_client gc ON gc.client_id = mc.id LEFT JOIN m_group g ON g.id = gc.group_id LEFT JOIN m_staff lo ON lo.id = ml.loan_officer_id LEFT JOIN m_guarantor gua ON gua.loan_id = ml.id WHERE ml.loan_status_id = 300 AND mo.id = ''${officeId}'' AND (COALESCE(ml.loan_officer_id, -10) = ${loanOfficerId} OR ''-1'' = ${loanOfficerId}) AND (EXTRACT(DAY FROM(CURRENT_DATE - lt.transaction_date::TIMESTAMP)) BETWEEN ${fromX} AND ${toY}) AND (EXTRACT(DAY FROM(CURRENT_DATE - laa.overdue_since_date_derived::TIMESTAMP)) BETWEEN ${overdueX} AND ${overdueY}) AND lt.is_reversed = false AND lt.transaction_type_enum = 2 GROUP BY ml.id, mc.id, ounder.id ORDER BY ounder.hierarchy, ml.currency_code, mc.account_no, ml.account_no', 'Payments received in the last fromX to toY days for any loan with the status Overdue (arrears) between overdueX and overdueY days', 'f', 't', 'f');
INSERT INTO public.stretchy_report (id, report_name, report_type, report_subtype, report_category, report_sql, description, core_report, use_report, self_service_user_report) VALUES ('155', 'Demand Vs Collection', 'Table', '', 'Loan', 'SELECT amount.AmountDue-amount.AmountPaid as AmountDue, amount.AmountPaid as AmountPaid FROM             (SELECT             (COALESCE(SUM(ls.principal_amount),0) - COALESCE(SUM(ls.principal_writtenoff_derived),0)              + COALESCE(SUM(ls.interest_amount),0) - COALESCE(SUM(ls.interest_writtenoff_derived),0)              - COALESCE(SUM(ls.interest_waived_derived),0)              + COALESCE(SUM(ls.fee_charges_amount),0) - COALESCE(SUM(ls.fee_charges_writtenoff_derived),0)              - COALESCE(SUM(ls.fee_charges_waived_derived),0)              + COALESCE(SUM(ls.penalty_charges_amount),0) - COALESCE(SUM(ls.penalty_charges_writtenoff_derived),0)              - COALESCE(SUM(ls.penalty_charges_waived_derived),0)             ) AS AmountDue,             (COALESCE(SUM(ls.principal_completed_derived),0) - COALESCE(SUM(ls.principal_writtenoff_derived),0) + COALESCE(SUM(ls.interest_completed_derived),0) - COALESCE(SUM(ls.interest_writtenoff_derived),0)              - COALESCE(SUM(ls.interest_waived_derived),0)              + COALESCE(SUM(ls.fee_charges_completed_derived),0) - COALESCE(SUM(ls.fee_charges_writtenoff_derived),0)              - COALESCE(SUM(ls.fee_charges_waived_derived),0)              + COALESCE(SUM(ls.penalty_charges_completed_derived),0) - COALESCE(SUM(ls.penalty_charges_writtenoff_derived),0)              - COALESCE(SUM(ls.penalty_charges_waived_derived),0)             ) AS AmountPaid             FROM m_office o             LEFT JOIN m_client cl ON o.id = cl.office_id             LEFT JOIN m_loan ln ON cl.id = ln.client_id             LEFT JOIN m_loan_repayment_schedule ls ON ln.id = ls.loan_id             WHERE              (o.hierarchy LIKE CONCAT((SELECT ino.hierarchy FROM m_office ino WHERE ino.id = ${officeId}),''%''))) as amount             ', 'Demand Vs Collection', 't', 'f', 'f');
INSERT INTO public.stretchy_report (id, report_name, report_type, report_subtype, report_category, report_sql, description, core_report, use_report, self_service_user_report) VALUES ('156', 'Disbursal Vs Awaitingdisbursal', 'Table', '', 'Loan', 'SELECT awaitinddisbursal.amount-disbursedAmount.amount as amountToBeDisburse, disbursedAmount.amount as disbursedAmount from             (SELECT COUNT(ln.id) AS noOfLoans, COALESCE(SUM(ln.principal_amount),0) AS amount FROM m_office o             LEFT JOIN m_client cl ON cl.office_id = o.id             LEFT JOIN m_loan ln ON cl.id = ln.client_id             WHERE (ln.loan_status_id=200 OR ln.loan_status_id=300) AND                 o.hierarchy like concat((select ino.hierarchy from m_office ino where ino.id = ${officeId}),''%'' )             ) awaitinddisbursal,             (SELECT COUNT(ltrxn.id) as count, COALESCE(SUM(ltrxn.amount),0) as amount FROM m_office o             LEFT JOIN m_client cl ON cl.office_id = o.id             LEFT JOIN m_loan ln ON cl.id = ln.client_id             LEFT JOIN m_loan_transaction ltrxn ON ln.id = ltrxn.loan_id             WHERE ltrxn.is_reversed = false AND ltrxn.transaction_type_enum=1 AND                 o.hierarchy like concat((select ino.hierarchy from m_office ino where ino.id = ${officeId}),''%'' )             ) disbursedAmount             ', 'Disbursal_Vs_Awaitingdisbursal', 't', 'f', 'f');
INSERT INTO public.stretchy_report (id, report_name, report_type, report_subtype, report_category, report_sql, description, core_report, use_report, self_service_user_report) VALUES ('198', 'Transaction Summary Report', 'Table', NULL, 'Accounting', 'SELECT             ''${endDate}'' AS TransactionDate,             a.product AS Product,             (               SELECT                 enum_message_property               FROM                 r_enum_value               WHERE                 enum_name = ''transaction_type_enum''                 and enum_id = a.transaction_type             ) TransactionType_Name,             (               select                 value               from                 m_payment_type               where                 id = a.payment_type_id             ) as PaymentType_Name,             a.chargetype as chargetype,             a.reversal_indicator AS Reversed,             a.Allocation_Type AS Allocation_Type,             '''' AS Chargeoff_ReasonCode,             case when a.transaction_type IN (2, 23, 21, 22, 24, 4, 5, 8, 6, 27, 9, 26, 41, 43)             AND a.reversal_indicator = false then sum(a.amount) * -1 when a.transaction_type IN (2, 23, 21, 22, 24, 4, 5, 8, 6, 27, 9, 26, 41, 43)             AND a.reversal_indicator = true then sum(a.amount) * + 1 when a.transaction_type IN (1, 10, 25, 20, 40, 42)             AND a.reversal_indicator = false then sum(a.amount) * + 1 when a.transaction_type IN (1, 10, 25, 20, 40, 42)             AND a.reversal_indicator = true then sum(a.amount) * -1 end AS Transaction_Amount           FROM             (               SELECT                 ''${endDate}'' AS transactiondate,                 t.id,                 l.name AS product,                 t.transaction_type_enum AS transaction_type,                 d.payment_type_id,                 '''' as chargetype,                 false AS reversal_indicator,                 ''Principal'' AS Allocation_Type,                 CASE when t.transaction_type_enum in (1) then (                   case when t.amount is null then 0 else t.amount end                 ) else (                   case when t.principal_portion_derived is null then 0 else t.principal_portion_derived end                 ) end amount               FROM                 m_loan_transaction t                 JOIN m_loan m ON m.id = t.loan_id                 JOIN m_product_loan l ON l.id = m.product_id                 left join m_payment_detail d on d.id = t.payment_detail_id               WHERE                 t.submitted_on_date = ''${endDate}''                 and t.transaction_type_enum not in (10, 26)                 and (t.office_id = ${officeId})               UNION ALL               SELECT                 ''${endDate}'' AS transactiondate,                 t.id,                 l.name AS product,                 t.transaction_type_enum AS transaction_type,                 d.payment_type_id,                 '''' as chargetype,                 false AS reversal_indicator,                 ''Interest'' AS Allocation_Type,                 case when t.interest_portion_derived is null then 0 else t.interest_portion_derived end AS amount               FROM                 m_loan_transaction t                 JOIN m_loan m ON m.id = t.loan_id                 JOIN m_product_loan l ON l.id = m.product_id                 left join m_payment_detail d on d.id = t.payment_detail_id               WHERE                 t.submitted_on_date = ''${endDate}''                 and t.transaction_type_enum not in (10, 26)                 and (t.office_id = ${officeId})               UNION ALL               SELECT                 ''${endDate}'' AS transactiondate,                 t.id,                 l.name AS product,                 t.transaction_type_enum AS transaction_type,                 d.payment_type_id,                 '''' as chargetype,                 false AS reversal_indicator,                 ''Fees'' AS Allocation_Type,                 case when t.fee_charges_portion_derived is null then 0 else t.fee_charges_portion_derived end as amount               FROM                 m_loan_transaction t                 JOIN m_loan m ON m.id = t.loan_id                 JOIN m_product_loan l ON l.id = m.product_id                 left join m_payment_detail d on d.id = t.payment_detail_id               WHERE                 t.submitted_on_date = ''${endDate}''                 and t.transaction_type_enum not in (10, 26)                 and (t.office_id = ${officeId})               UNION ALL               SELECT                 ''${endDate}'' AS transactiondate,                 t.id,                 l.name AS product,                 t.transaction_type_enum AS transaction_type,                 d.payment_type_id,                 '''' as chargetype,                 false AS reversal_indicator,                 ''Penalty'' AS Allocation_Type,                 case when t.penalty_charges_portion_derived is null then 0 else t.penalty_charges_portion_derived end as amount               FROM                 m_loan_transaction t                 JOIN m_loan m ON m.id = t.loan_id                 JOIN m_product_loan l ON l.id = m.product_id                 left join m_payment_detail d on d.id = t.payment_detail_id               WHERE                 t.submitted_on_date = ''${endDate}''                 and t.transaction_type_enum not in (10, 26)                 and (t.office_id = ${officeId})               UNION ALL               SELECT                 ''${endDate}'' AS transactiondate,                 t.id,                 l.name AS product,                 t.transaction_type_enum AS transaction_type,                 d.payment_type_id,                 '''' as chargetype,                 false AS reversal_indicator,                 ''Unallocated Credit (UNC)'' AS Allocation_Type,                 case when t.overpayment_portion_derived is null then 0 else t.overpayment_portion_derived end as amount               FROM                 m_loan_transaction t                 JOIN m_loan m ON m.id = t.loan_id                 JOIN m_product_loan l ON l.id = m.product_id                 left join m_payment_detail d on d.id = t.payment_detail_id               WHERE                 t.submitted_on_date = ''${endDate}''                 and t.transaction_type_enum not in (10, 26)                 and (t.office_id = ${officeId})               UNION ALL               SELECT                 ''${endDate}'' AS transactiondate,                 t.id,                 l.name AS product,                 t.transaction_type_enum AS transaction_type,                 null,                 mc.name,                 false AS reversal_indicator,                 ''Fees'' AS Allocation_Type,                 case when pd.amount is null then 0 else pd.amount end as amount               FROM                 m_loan_transaction t                 JOIN m_loan m ON m.id = t.loan_id                 JOIN m_product_loan l ON l.id = m.product_id                 join m_loan_charge_paid_by pd on pd.loan_transaction_id = t.id                 join m_loan_charge c on c.id = pd.loan_charge_id                 join m_charge mc on mc.id = c.charge_id                 and mc.is_penalty = false               WHERE                 t.submitted_on_date = ''${endDate}''                 and t.transaction_type_enum = 10                 and t.is_reversed = false                 and (t.office_id = ${officeId})               UNION ALL               SELECT                 ''${endDate}'' AS transactiondate,                 t.id,                 l.name AS product,                 t.transaction_type_enum AS transaction_type,                 null,                 mc.name,                 false AS reversal_indicator,                 ''Penalty'' AS Allocation_Type,                 case when pd.amount is null then 0 else pd.amount end as amount               FROM                 m_loan_transaction t                 JOIN m_loan m ON m.id = t.loan_id                 JOIN m_product_loan l ON l.id = m.product_id                 join m_loan_charge_paid_by pd on pd.loan_transaction_id = t.id                 join m_loan_charge c on c.id = pd.loan_charge_id                 join m_charge mc on mc.id = c.charge_id                 and mc.is_penalty = true               WHERE                 t.submitted_on_date = ''${endDate}''                 and t.transaction_type_enum = 10                 and t.is_reversed = false                 and (t.office_id = ${officeId})               UNION ALL               SELECT                 ''${endDate}'' AS transactiondate,                 t.id,                 l.name AS product,                 t.transaction_type_enum AS transaction_type,                 null,                 '''' as chargetype,                 false AS reversal_indicator,                 ''Interest'' AS Allocation_Type,                 case when t.interest_portion_derived is null then 0 else t.interest_portion_derived end AS amount               FROM                 m_loan_transaction t                 JOIN m_loan m ON m.id = t.loan_id                 JOIN m_product_loan l ON l.id = m.product_id               WHERE                 t.submitted_on_date = ''${endDate}''                 and t.transaction_type_enum = 10                 and t.is_reversed = false                 and (t.office_id = ${officeId})               UNION ALL               SELECT                 ''${endDate}'' AS transactiondate,                 t.id,                 l.name AS product,                 t.transaction_type_enum AS transaction_type,                 d.payment_type_id,                 mc.name,                 false AS reversal_indicator,                 ''Fees'' AS Allocation_Type,                 case when pd.amount is null then 0 else pd.amount end as amount               FROM                 m_loan_transaction t                 JOIN m_loan m ON m.id = t.loan_id                 JOIN m_product_loan l ON l.id = m.product_id                 join m_loan_charge_paid_by pd on pd.loan_transaction_id = t.id                 join m_loan_charge c on c.id = pd.loan_charge_id                 join m_charge mc on mc.id = c.charge_id                 and mc.is_penalty = false                 left join m_payment_detail d on d.id = t.payment_detail_id               WHERE                 t.submitted_on_date = ''${endDate}''                 and t.transaction_type_enum = 26                 and (t.office_id = ${officeId})               UNION ALL               SELECT                 ''${endDate}'' AS transactiondate,                 t.id,                 l.name AS product,                 t.transaction_type_enum AS transaction_type,                 d.payment_type_id,                 mc.name,                 false AS reversal_indicator,                 ''Penalty'' AS Allocation_Type,                 case when pd.amount is null then 0 else pd.amount end AS amount               FROM                 m_loan_transaction t                 JOIN m_loan m ON m.id = t.loan_id                 JOIN m_product_loan l ON l.id = m.product_id                 join m_loan_charge_paid_by pd on pd.loan_transaction_id = t.id                 join m_loan_charge c on c.id = pd.loan_charge_id                 join m_charge mc on mc.id = c.charge_id                 and mc.is_penalty = true                 left join m_payment_detail d on d.id = t.payment_detail_id               WHERE                 t.submitted_on_date = ''${endDate}''                 and t.transaction_type_enum = 26                 and (t.office_id = ${officeId})               UNION ALL               SELECT                 ''${endDate}'' AS transactiondate,                 t.id,                 l.name AS product,                 t.transaction_type_enum AS transaction_type,                 d.payment_type_id,                 '''' as chargetype,                 false AS reversal_indicator,                 ''Interest'' AS Allocation_Type,                 case when t.interest_portion_derived is null then 0 else t.interest_portion_derived end AS amount               FROM                 m_loan_transaction t                 JOIN m_loan m ON m.id = t.loan_id                 JOIN m_product_loan l ON l.id = m.product_id                 left join m_payment_detail d on d.id = t.payment_detail_id               WHERE                 t.submitted_on_date = ''${endDate}''                 and t.transaction_type_enum = 26                 and (t.office_id = ${officeId})               UNION ALL               SELECT                 ''${endDate}'' AS transactiondate,                 t.id,                 l.name AS product,                 t.transaction_type_enum AS transaction_type,                 d.payment_type_id,                 '''' as chargetype,                 false AS reversal_indicator,                 ''Principal'' AS Allocation_Type,                 case when t.principal_portion_derived is null then 0 else t.principal_portion_derived end AS amount               FROM                 m_loan_transaction t                 JOIN m_loan m ON m.id = t.loan_id                 JOIN m_product_loan l ON l.id = m.product_id                 left join m_payment_detail d on d.id = t.payment_detail_id               WHERE                 t.submitted_on_date = ''${endDate}''                 and t.transaction_type_enum = 26                 and (t.office_id = ${officeId})               UNION ALL               SELECT                 ''${endDate}'' AS transactiondate,                 t.id,                 l.name AS product,                 t.transaction_type_enum AS transaction_type,                 d.payment_type_id,                 '''' as chargetype,                 false AS reversal_indicator,                 ''Unallocated Credit (UNC)'' AS Allocation_Type,                 case when t.overpayment_portion_derived is null then 0 else t.overpayment_portion_derived end AS amount               FROM                 m_loan_transaction t                 JOIN m_loan m ON m.id = t.loan_id                 JOIN m_product_loan l ON l.id = m.product_id                 left join m_payment_detail d on d.id = t.payment_detail_id               WHERE                 t.submitted_on_date = ''${endDate}''                 and t.transaction_type_enum = 26                 and (t.office_id = ${officeId})               UNION ALL               SELECT                 ''${endDate}'' AS transactiondate,                 t.id,                 l.name AS product,                 t.transaction_type_enum AS transaction_type,                 d.payment_type_id,                 '''' as chargetype,                 true AS reversal_indicator,                 ''Principal'' AS Allocation_Type,                 CASE when t.transaction_type_enum in (1) then (                   case when t.amount is null then 0 else t.amount end                 ) else (                   case when t.principal_portion_derived is null then 0 else t.principal_portion_derived end                 ) end amount               FROM                 m_loan_transaction t                 JOIN m_loan m ON m.id = t.loan_id                 JOIN m_product_loan l ON l.id = m.product_id                 left join m_payment_detail d on d.id = t.payment_detail_id               WHERE                 t.reversed_on_date = ''${endDate}''                 and t.transaction_type_enum not in (10, 26)                 and (t.office_id = ${officeId})               UNION ALL               SELECT                 ''${endDate}'' AS transactiondate,                 t.id,                 l.name AS product,                 t.transaction_type_enum AS transaction_type,                 d.payment_type_id,                 '''' as chargetype,                 true AS reversal_indicator,                 ''Interest'' AS Allocation_Type,                 case when t.interest_portion_derived is null then 0 else t.interest_portion_derived end AS amount               FROM                 m_loan_transaction t                 JOIN m_loan m ON m.id = t.loan_id                 JOIN m_product_loan l ON l.id = m.product_id                 left join m_payment_detail d on d.id = t.payment_detail_id               WHERE                 t.reversed_on_date = ''${endDate}''                 and t.transaction_type_enum not in (10, 26)                 and (t.office_id = ${officeId})               UNION ALL               SELECT                 ''${endDate}'' AS transactiondate,                 t.id,                 l.name AS product,                 t.transaction_type_enum AS transaction_type,                 d.payment_type_id,                 '''' as chargetype,                 true AS reversal_indicator,                 ''Fees'' AS Allocation_Type,                 case when t.fee_charges_portion_derived is null then 0 else t.fee_charges_portion_derived end as amount               FROM                 m_loan_transaction t                 JOIN m_loan m ON m.id = t.loan_id                 JOIN m_product_loan l ON l.id = m.product_id                 left join m_payment_detail d on d.id = t.payment_detail_id               WHERE                 t.reversed_on_date = ''${endDate}''                 and t.transaction_type_enum not in (10, 26)                 and (t.office_id = ${officeId})               UNION ALL               SELECT                 ''${endDate}'' AS transactiondate,                 t.id,                 l.name AS product,                 t.transaction_type_enum AS transaction_type,                 d.payment_type_id,                 '''' as chargetype,                 true AS reversal_indicator,                 ''Penalty'' AS Allocation_Type,                 case when t.penalty_charges_portion_derived is null then 0 else t.penalty_charges_portion_derived end as amount               FROM                 m_loan_transaction t                 JOIN m_loan m ON m.id = t.loan_id                 JOIN m_product_loan l ON l.id = m.product_id                 left join m_payment_detail d on d.id = t.payment_detail_id               WHERE                 t.reversed_on_date = ''${endDate}''                 and t.transaction_type_enum not in (10, 26)                 and (t.office_id = ${officeId})               UNION ALL               SELECT                 ''${endDate}'' AS transactiondate,                 t.id,                 l.name AS product,                 t.transaction_type_enum AS transaction_type,                 d.payment_type_id,                 '''' as chargetype,                 true AS reversal_indicator,                 ''Unallocated Credit (UNC)'' AS Allocation_Type,                 case when t.overpayment_portion_derived is null then 0 else t.overpayment_portion_derived end as amount               FROM                 m_loan_transaction t                 JOIN m_loan m ON m.id = t.loan_id                 JOIN m_product_loan l ON l.id = m.product_id                 left join m_payment_detail d on d.id = t.payment_detail_id               WHERE                 t.reversed_on_date = ''${endDate}''                 and t.transaction_type_enum not in (10, 26)                 and (t.office_id = ${officeId})               UNION ALL               SELECT                 ''${endDate}'' AS transactiondate,                 t.id,                 l.name AS product,                 t.transaction_type_enum AS transaction_type,                 null,                 mc.name,                 true AS reversal_indicator,                 ''Fees'' AS Allocation_Type,                 case when pd.amount is null then 0 else pd.amount end as amount               FROM                 m_loan_transaction t                 JOIN m_loan m ON m.id = t.loan_id                 JOIN m_product_loan l ON l.id = m.product_id                 join m_loan_charge_paid_by pd on pd.loan_transaction_id = t.id                 join m_loan_charge c on c.id = pd.loan_charge_id                 join m_charge mc on mc.id = c.charge_id                 and mc.is_penalty = false               WHERE                 t.reversed_on_date = ''${endDate}''                 and t.transaction_type_enum = 10                 and t.is_reversed = true                 and (t.office_id = ${officeId})               UNION ALL               SELECT                 ''${endDate}'' AS transactiondate,                 t.id,                 l.name AS product,                 t.transaction_type_enum AS transaction_type,                 null,                 mc.name,                 true AS reversal_indicator,                 ''Penalty'' AS Allocation_Type,                 case when pd.amount is null then 0 else pd.amount end as amount               FROM                 m_loan_transaction t                 JOIN m_loan m ON m.id = t.loan_id                 JOIN m_product_loan l ON l.id = m.product_id                 join m_loan_charge_paid_by pd on pd.loan_transaction_id = t.id                 join m_loan_charge c on c.id = pd.loan_charge_id                 join m_charge mc on mc.id = c.charge_id                 and mc.is_penalty = true               WHERE                 t.reversed_on_date = ''${endDate}''                 and t.transaction_type_enum = 10                 and t.is_reversed = true                 and (t.office_id = ${officeId})               UNION ALL               SELECT                 ''${endDate}'' AS transactiondate,                 t.id,                 l.name AS product,                 t.transaction_type_enum AS transaction_type,                 null,                 '''' as chargetype,                 true AS reversal_indicator,                 ''Interest'' AS Allocation_Type,                 case when t.interest_portion_derived is null then 0 else t.interest_portion_derived end AS amount               FROM                 m_loan_transaction t                 JOIN m_loan m ON m.id = t.loan_id                 JOIN m_product_loan l ON l.id = m.product_id               WHERE                 t.reversed_on_date = ''${endDate}''                 and t.transaction_type_enum = 10                 and t.is_reversed = true                 and (t.office_id = ${officeId})               UNION ALL               SELECT                 ''${endDate}'' AS transactiondate,                 t.id,                 l.name AS product,                 t.transaction_type_enum AS transaction_type,                 d.payment_type_id,                 mc.name,                 true AS reversal_indicator,                 ''Fees'' AS Allocation_Type,                 case when pd.amount is null then 0 else pd.amount end as amount               FROM                 m_loan_transaction t                 JOIN m_loan m ON m.id = t.loan_id                 JOIN m_product_loan l ON l.id = m.product_id                 join m_loan_charge_paid_by pd on pd.loan_transaction_id = t.id                 join m_loan_charge c on c.id = pd.loan_charge_id                 join m_charge mc on mc.id = c.charge_id                 and mc.is_penalty = false                 left join m_payment_detail d on d.id = t.payment_detail_id               WHERE                 t.reversed_on_date = ''${endDate}''                 and t.transaction_type_enum = 26                 and (t.office_id = ${officeId})               UNION ALL               SELECT                 ''${endDate}'' AS transactiondate,                 t.id,                 l.name AS product,                 t.transaction_type_enum AS transaction_type,                 d.payment_type_id,                 mc.name,                 true AS reversal_indicator,                 ''Penalty'' AS Allocation_Type,                 case when pd.amount is null then 0 else pd.amount end AS amount               FROM                 m_loan_transaction t                 JOIN m_loan m ON m.id = t.loan_id                 JOIN m_product_loan l ON l.id = m.product_id                 join m_loan_charge_paid_by pd on pd.loan_transaction_id = t.id                 join m_loan_charge c on c.id = pd.loan_charge_id                 join m_charge mc on mc.id = c.charge_id                 and mc.is_penalty = true                 left join m_payment_detail d on d.id = t.payment_detail_id               WHERE                 t.reversed_on_date = ''${endDate}''                 and t.transaction_type_enum = 26                 and (t.office_id = ${officeId})               UNION ALL               SELECT                 ''${endDate}'' AS transactiondate,                 t.id,                 l.name AS product,                 t.transaction_type_enum AS transaction_type,                 d.payment_type_id,                 '''' as chargetype,                 true AS reversal_indicator,                 ''Interest'' AS Allocation_Type,                 case when t.interest_portion_derived is null then 0 else t.interest_portion_derived end AS amount               FROM                 m_loan_transaction t                 JOIN m_loan m ON m.id = t.loan_id                 JOIN m_product_loan l ON l.id = m.product_id                 left join m_payment_detail d on d.id = t.payment_detail_id               WHERE                 t.reversed_on_date = ''${endDate}''                 and t.transaction_type_enum = 26                 and (t.office_id = ${officeId})               UNION ALL               SELECT                 ''${endDate}'' AS transactiondate,                 t.id,                 l.name AS product,                 t.transaction_type_enum AS transaction_type,                 d.payment_type_id,                 '''' as chargetype,                 true AS reversal_indicator,                 ''Principal'' AS Allocation_Type,                 case when t.principal_portion_derived is null then 0 else t.principal_portion_derived end AS amount               FROM                 m_loan_transaction t                 JOIN m_loan m ON m.id = t.loan_id                 JOIN m_product_loan l ON l.id = m.product_id                 left join m_payment_detail d on d.id = t.payment_detail_id               WHERE                 t.reversed_on_date = ''${endDate}''                 and t.transaction_type_enum = 26                 and (t.office_id = ${officeId})               UNION ALL               SELECT                 ''${endDate}'' AS transactiondate,                 t.id,                 l.name AS product,                 t.transaction_type_enum AS transaction_type,                 d.payment_type_id,                 '''' as chargetype,                 true AS reversal_indicator,                 ''Unallocated Credit (UNC)'' AS Allocation_Type,                 case when t.overpayment_portion_derived is null then 0 else t.overpayment_portion_derived end AS amount               FROM                 m_loan_transaction t                 JOIN m_loan m ON m.id = t.loan_id                 JOIN m_product_loan l ON l.id = m.product_id                 left join m_payment_detail d on d.id = t.payment_detail_id               WHERE                 t.reversed_on_date = ''${endDate}''                 and t.transaction_type_enum = 26                 and (t.office_id = ${officeId})             ) a           GROUP BY             a.transactiondate,             a.product,             a.transaction_type,             a.payment_type_id,             a.chargetype,             a.reversal_indicator,             a.Allocation_Type           order by             1,             2,             3,             4,             5,             6,             7 ', 'Transaction Summary Report', 'f', 't', 'f');
INSERT INTO public.stretchy_report (id, report_name, report_type, report_subtype, report_category, report_sql, description, core_report, use_report, self_service_user_report) VALUES ('197', 'Trial Balance Summary Report', 'Table', NULL, 'Accounting', 'SELECT *             FROM             (               SELECT                 ''${endDate}'' AS PostingDate,                 loan.pname AS Product,                 loan.gl_code AS GlAcct,                 loan.glname AS Description,                 loan.openingbalance AS BeginningBalance,                 (loan.debitamount * 1) AS DebitMovement,                 (loan.creditamount *-1) AS CreditMovement,                 (                   loan.openingbalance + loan.debitamount - loan.creditamount                 ) AS EndingBalance               FROM                 (                   SELECT                     g.pname,                     g.gl_code,                     g.glname,                     COALESCE(debits.debitamount, 0) - COALESCE(debits.creditamount, 0) AS openingbalance,                     COALESCE(loanproduct.debitamount, 0) AS debitamount,                     COALESCE(loanproduct.creditamount, 0) AS creditamount                   FROM                     (                       SELECT                         ag.gl_code,                         pl.name AS pname,                         ag.name AS glname                       FROM                         acc_gl_account ag                         JOIN acc_product_mapping am ON am.gl_account_id = ag.id                         AND am.product_type = 1                         JOIN m_product_loan pl ON pl.id = am.product_id                     ) g                     LEFT JOIN (                       SELECT                         lp.name AS productname,                         acc_gl_account.gl_code AS glcode,                         acc_gl_account.name AS glname,                         sum(                           case when acc_gl_journal_entry.type_enum = 2 then amount ELSE 0 end                         ) AS debitamount,                         sum(                           case when acc_gl_journal_entry.type_enum = 1 then amount ELSE 0 end                         ) AS creditamount                       FROM                         acc_gl_account                         JOIN acc_gl_journal_entry on acc_gl_account.id = acc_gl_journal_entry.account_id                         JOIN m_loan m ON m.id = acc_gl_journal_entry.entity_id                         JOIN m_product_loan lp ON lp.id = m.product_id                       WHERE                         acc_gl_journal_entry.entity_type_enum = 1                         AND acc_gl_journal_entry.manual_entry = false                         and acc_gl_journal_entry.submitted_on_date < ''${endDate}''                         and (                           acc_gl_journal_entry.office_id = ${officeId}                         )                       group by                         productname,                         glcode,                         glname                       order by                         glcode                     ) debits ON g.gl_code = debits.glcode                     AND debits.productname = g.pname                     LEFT JOIN (                       SELECT                         lp.name AS productname, account_id,                         acc_gl_account.gl_code AS glcode,                         acc_gl_account.name AS glname,                         sum(                           case when acc_gl_journal_entry.type_enum = 2 then amount ELSE 0 END                         ) AS debitamount,                         sum(                           case when acc_gl_journal_entry.type_enum = 1 then amount ELSE 0 END                         ) AS creditamount                       FROM                         acc_gl_journal_entry                         JOIN acc_gl_account on acc_gl_account.id = acc_gl_journal_entry.account_id                         JOIN m_loan m ON m.id = acc_gl_journal_entry.entity_id                         JOIN m_product_loan lp ON lp.id = m.product_id                       WHERE                         acc_gl_journal_entry.entity_type_enum = 1                         AND acc_gl_journal_entry.manual_entry = false                         and acc_gl_journal_entry.submitted_on_date = ''${endDate}''                         AND (                           acc_gl_journal_entry.office_id = ${officeId}                         )                       group by                         productname,                         account_id,                         glcode,                         glname                       order by                         glcode                     ) loanproduct ON g.gl_code = loanproduct.glcode                     AND loanproduct.productname = g.pname                 ) loan               UNION               SELECT                 ''${endDate}'' AS PostingDate,                 loan.pname AS Product,                 loan.gl_code AS GlAcct,                 loan.glname AS Description,                 loan.openingbalance AS Beginning_Balance,                 loan.debitamount AS Debit_Movement,                 loan.creditamount AS Credit_Movement,                 (                   loan.openingbalance + loan.debitamount - loan.creditamount                 ) AS Ending_Balance               FROM                 (                   SELECT                     g.pname,                     g.gl_code,                     g.glname,                     COALESCE(debits.debitamount, 0) - COALESCE(debits.creditamount, 0) AS openingbalance,                     COALESCE(loanproduct.debitamount, 0) AS debitamount,                     COALESCE(loanproduct.creditamount, 0) AS creditamount                   FROM                     (                       SELECT                         ag.gl_code,                         pl.name AS pname,                         ag.name AS glname                       FROM                         acc_gl_account ag                         JOIN acc_product_mapping am ON am.gl_account_id = ag.id                         AND am.product_type = 2                         JOIN m_savings_product pl ON pl.id = am.product_id                     ) g                     LEFT join (                       SELECT                         lp.name productname,                         acc_gl_account.gl_code AS glcode,                         acc_gl_account.name AS glname,                         sum(                           case when acc_gl_journal_entry.type_enum = 2 then amount ELSE 0 end                         ) AS debitamount,                         sum(                           case when acc_gl_journal_entry.type_enum = 1 then amount ELSE 0 end                         ) AS creditamount                       FROM                         acc_gl_account                         join acc_gl_journal_entry on acc_gl_account.id = acc_gl_journal_entry.account_id                         JOIN m_savings_account m ON m.id = acc_gl_journal_entry.entity_id                         JOIN m_savings_product lp ON lp.id = m.product_id                       WHERE                         acc_gl_journal_entry.entity_type_enum = 2                         AND acc_gl_journal_entry.manual_entry = false                         and acc_gl_journal_entry.submitted_on_date < ''${endDate}''                         and (                           acc_gl_journal_entry.office_id = ${officeId}                         )                       group by                         productname,                         glcode,                         glname                       order by                         glcode                     ) debits ON g.gl_code = debits.glcode                     AND debits.productname = g.pname                     left JOIN (                       SELECT                         lp.name productname,                         acc_gl_account.gl_code AS glcode,                         acc_gl_account.name AS glname,                         sum(                           case when acc_gl_journal_entry.type_enum = 2 then amount ELSE 0 end                         ) AS debitamount,                         sum(                           case when acc_gl_journal_entry.type_enum = 1 then amount ELSE 0 end                         ) AS creditamount                       from                         acc_gl_journal_entry                         join acc_gl_account on acc_gl_account.id = acc_gl_journal_entry.account_id                         JOIN m_savings_account m ON m.id = acc_gl_journal_entry.entity_id                         JOIN m_savings_product lp ON lp.id = m.product_id                       WHERE                         acc_gl_journal_entry.entity_type_enum = 2                         AND acc_gl_journal_entry.manual_entry = false                         and acc_gl_journal_entry.submitted_on_date = ''${endDate}''                         and (                           acc_gl_journal_entry.office_id = ${officeId}                         )                       group by                         productname,                         glcode,                         glname                       order by                         glcode                     ) loanproduct ON g.gl_code = loanproduct.glcode                     AND loanproduct.productname = g.pname                 ) loan               UNION               SELECT                 ''${endDate}'' AS PostingDate,                 ''manual'' AS Product,                 loan.gl_code AS GlAcct,                 loan.glname AS Description,                 loan.openingbalance AS Beginning_Balance,                 loan.debitamount AS Debit_Movement,                 loan.creditamount AS Credit_Movement,                 (                   loan.openingbalance + loan.debitamount - loan.creditamount                 ) AS Ending_Balance               FROM                 (                   SELECT                     g.gl_code,                     g.name AS glname,                     COALESCE(debits.debitamount, 0) - COALESCE(debits.creditamount, 0) AS openingbalance,                     COALESCE(loanproduct.debitamount, 0) AS debitamount,                     COALESCE(loanproduct.creditamount, 0) AS creditamount                   FROM                     acc_gl_account g                     LEFT join (                       SELECT                         acc_gl_account.gl_code AS glcode,                         acc_gl_account.name AS glname,                         sum(                           case when acc_gl_journal_entry.type_enum = 2 then amount ELSE 0 end                         ) AS debitamount,                         sum(                           case when acc_gl_journal_entry.type_enum = 1 then amount ELSE 0 end                         ) AS creditamount                       FROM                         acc_gl_account                         JOIN acc_gl_journal_entry on acc_gl_account.id = acc_gl_journal_entry.account_id                       WHERE                         acc_gl_journal_entry.manual_entry = true                         and acc_gl_journal_entry.submitted_on_date < ''${endDate}''                         and (                           acc_gl_journal_entry.office_id = ${officeId}                         )                       group by                         glcode,                         glname                       order by                         glcode                     ) debits ON g.gl_code = debits.glcode                     left JOIN (                       SELECT                         acc_gl_account.gl_code AS glcode,                         acc_gl_account.name AS glname,                         sum(                           case when acc_gl_journal_entry.type_enum = 2 then amount ELSE 0 end                         ) AS debitamount,                         sum(                           case when acc_gl_journal_entry.type_enum = 1 then amount ELSE 0 end                         ) AS creditamount                       FROM                         acc_gl_journal_entry                         join acc_gl_account on acc_gl_account.id = acc_gl_journal_entry.account_id                       where                         acc_gl_journal_entry.manual_entry = true                         and acc_gl_journal_entry.submitted_on_date = ''${endDate}''                         and (                           acc_gl_journal_entry.office_id = ${officeId}                         )                       group by                         glcode,                         glname                       order by                         glcode                     ) loanproduct ON g.gl_code = loanproduct.glcode                 ) loan             ) a           where             a.EndingBalance != 0 or a.DebitMovement != 0 or a.CreditMovement != 0 ', 'Trial Balance Summary Report', 'f', 't', 'f');
INSERT INTO public.stretchy_report (id, report_name, report_type, report_subtype, report_category, report_sql, description, core_report, use_report, self_service_user_report) VALUES ('153', 'LoanTrendsByWeek', 'Table', '', 'Loan', 'SELECT COUNT(ln.id) AS lcount, EXTRACT(WEEK FROM ln.disbursedon_date) AS Weeks             FROM m_office o                 LEFT JOIN m_client cl on o.id = cl.office_id                 LEFT JOIN m_loan ln on cl.id = ln.client_id             WHERE o.hierarchy like concat((select ino.hierarchy from m_office ino where ino.id = ${officeId}),''%'' )                 AND (ln.disbursedon_date BETWEEN (CURRENT_DATE - INTERVAL ''12 weeks'') AND CURRENT_DATE)             GROUP BY EXTRACT(WEEK FROM ln.disbursedon_date)', '', 't', 'f', 'f');
INSERT INTO public.stretchy_report (id, report_name, report_type, report_subtype, report_category, report_sql, description, core_report, use_report, self_service_user_report) VALUES ('199', 'Trial Balance Summary Report with Asset Owner', 'Table', NULL, 'Accounting', 'WITH retained_earning AS (SELECT DISTINCT ''${endDate}'' AS postingdate,\n lp.name AS product,\n gl_code AS glacct,\n COALESCE((SELECT name FROM acc_gl_account WHERE gl_code = e.gl_code),\n '''') AS description,\n COALESCE(e.owner_external_id, ''self'') AS assetowner,\n SUM(opening_balance_amount) AS beginningbalance,\n 0 AS debitmovement,\n 0 AS creditmovement,\n SUM(opening_balance_amount) AS endingbalance,\n COALESCE(e.originator_external_ids, '''') AS originator_external_ids\n FROM acc_gl_journal_entry_annual_summary e,\n m_product_loan lp\n WHERE e.office_id = ${officeId}\n AND lp.id = product_id\n AND EXTRACT(YEAR FROM e.year_end_date) < EXTRACT(YEAR FROM CAST(''${endDate}'' AS DATE))\n GROUP BY gl_code, lp.name, office_id, owner_external_id, originator_external_ids),\n aggregated_date AS (SELECT MAX(aggregated_on_date_to) AS latest\n FROM m_journal_entry_aggregation_tracking\n WHERE aggregated_on_date_to < ''${endDate}''),\n summary_snapshot_baseline_data AS (SELECT lp.NAME AS productname,\n acc_gl_account.gl_code AS glcode,\n acc_gl_account.NAME AS glname,\n CASE\n WHEN ags.external_owner_id IS NULL THEN 0\n ELSE ags.external_owner_id END AS assetowner,\n COALESCE(ags.originator_external_ids, '''') AS originator_external_ids,\n SUM(ags.debit_amount) AS debitamount,\n SUM(ags.credit_amount) AS creditamount\n FROM acc_gl_account\n JOIN m_journal_entry_aggregation_summary ags\n ON acc_gl_account.id = ags.gl_account_id\n JOIN m_product_loan lp ON lp.id = ags.product_id\n WHERE ags.entity_type_enum = 1\n AND ags.manual_entry = FALSE\n AND ags.aggregated_on_date <= (SELECT latest FROM aggregated_date)\n AND (ags.office_id = ${officeId})\n GROUP BY productname, glcode, glname, assetowner, originator_external_ids),\n post_snapshot_delta_data AS (SELECT lp.NAME AS productname,\n acc_gl_account.gl_code AS glcode,\n acc_gl_account.NAME AS glname,\n CASE WHEN aw.owner_id IS NULL THEN 0 ELSE aw.owner_id END AS assetowner,\n SUM(CASE WHEN acc_gl_journal_entry.type_enum = 2 THEN amount ELSE 0 END) AS debitamount,\n SUM(CASE WHEN acc_gl_journal_entry.type_enum = 1 THEN amount ELSE 0 END) AS creditamount,\n COALESCE((SELECT STRING_AGG(DISTINCT mlo.external_id, '', '' ORDER BY mlo.external_id)\n FROM m_loan_originator_mapping mlom\n JOIN m_loan_originator mlo ON mlo.id = mlom.originator_id\n WHERE mlom.loan_id = m.id), '''') AS originator_external_ids\n FROM acc_gl_account\n JOIN acc_gl_journal_entry\n ON acc_gl_account.id = acc_gl_journal_entry.account_id\n JOIN m_loan m ON m.id = acc_gl_journal_entry.entity_id\n JOIN m_product_loan lp ON lp.id = m.product_id\n LEFT JOIN m_external_asset_owner_journal_entry_mapping aw\n ON aw.journal_entry_id = acc_gl_journal_entry.id\n WHERE acc_gl_journal_entry.entity_type_enum = 1\n AND acc_gl_journal_entry.manual_entry = FALSE\n AND (\n (SELECT latest FROM aggregated_date) IS NULL\n OR\n acc_gl_journal_entry.submitted_on_date > (SELECT latest FROM aggregated_date)\n )\n AND acc_gl_journal_entry.submitted_on_date < ''${endDate}''\n AND (acc_gl_journal_entry.office_id = ${officeId})\n GROUP BY productname, glcode, glname, assetowner, originator_external_ids),\n merged_historical_data AS (SELECT COALESCE(s.productname, p.productname) AS productname,\n COALESCE(s.glcode, p.glcode) AS glcode,\n COALESCE(s.glname, p.glname) AS glname,\n COALESCE(s.assetowner, p.assetowner, 0) AS assetowner,\n COALESCE(s.debitamount, 0) + COALESCE(p.debitamount, 0) AS debitamount,\n COALESCE(s.creditamount, 0) + COALESCE(p.creditamount, 0) AS creditamount,\n COALESCE(p.originator_external_ids, s.originator_external_ids, '''') AS originator_external_ids\n FROM summary_snapshot_baseline_data s\n LEFT JOIN post_snapshot_delta_data p\n ON s.glcode = p.glcode\n AND s.productname = p.productname\n AND s.assetowner = p.assetowner\n AND s.originator_external_ids = p.originator_external_ids\n\n UNION ALL\n\n SELECT p.productname AS productname,\n p.glcode AS glcode,\n p.glname AS glname,\n COALESCE(p.assetowner, 0) AS assetowner,\n COALESCE(p.debitamount, 0) AS debitamount,\n COALESCE(p.creditamount, 0) AS creditamount,\n COALESCE(p.originator_external_ids, '''') AS originator_external_ids\n FROM post_snapshot_delta_data p\n LEFT JOIN summary_snapshot_baseline_data s\n ON s.glcode = p.glcode\n AND s.productname = p.productname\n AND s.assetowner = p.assetowner\n AND s.originator_external_ids = p.originator_external_ids\n WHERE s.glcode IS NULL),\n current_cob_data AS (SELECT lp.name AS productname,\n account_id,\n acc_gl_account.gl_code AS glcode,\n acc_gl_account.name AS glname,\n CASE WHEN aw.owner_id IS NULL THEN 0 ELSE aw.owner_id END AS assetowner,\n SUM(CASE WHEN acc_gl_journal_entry.type_enum = 2 THEN amount ELSE 0 END) AS debitamount,\n SUM(CASE WHEN acc_gl_journal_entry.type_enum = 1 THEN amount ELSE 0 END) AS creditamount,\n COALESCE((SELECT STRING_AGG(DISTINCT mlo.external_id, '', '' ORDER BY mlo.external_id)\n FROM m_loan_originator_mapping mlom\n JOIN m_loan_originator mlo ON mlo.id = mlom.originator_id\n WHERE mlom.loan_id = m.id), '''') AS originator_external_ids\n FROM acc_gl_journal_entry\n JOIN acc_gl_account ON acc_gl_account.id = acc_gl_journal_entry.account_id\n JOIN m_loan m ON m.id = acc_gl_journal_entry.entity_id\n JOIN m_product_loan lp ON lp.id = m.product_id\n LEFT JOIN m_external_asset_owner_journal_entry_mapping aw\n ON aw.journal_entry_id = acc_gl_journal_entry.id\n WHERE acc_gl_journal_entry.entity_type_enum = 1\n AND acc_gl_journal_entry.manual_entry = FALSE\n AND acc_gl_journal_entry.submitted_on_date = ''${endDate}''\n AND (acc_gl_journal_entry.office_id = ${officeId})\n GROUP BY productname, account_id, glcode, glname, assetowner, originator_external_ids)\n\nSELECT *\nFROM (SELECT *\n FROM retained_earning\n WHERE glacct = (SELECT gl_code FROM acc_gl_account WHERE name = ''Retained Earnings Prior Year'')\n\n UNION\n\n SELECT txnreport.postingdate,\n txnreport.product,\n txnreport.glacct,\n txnreport.description,\n txnreport.assetowner,\n (COALESCE(txnreport.beginningbalance, 0) + COALESCE(summary.beginningbalance, 0)) AS beginningbalance,\n txnreport.debitmovement AS debitmovement,\n txnreport.creditmovement AS creditmovement,\n (COALESCE(txnreport.endingbalance, 0) + COALESCE(summary.beginningbalance, 0)) AS endingbalance,\n txnreport.originator_external_ids AS originator_external_ids\n FROM (SELECT *\n FROM (SELECT DISTINCT ''${endDate}'' AS postingdate,\n loan.pname AS product,\n loan.gl_code AS glacct,\n loan.glname AS description,\n COALESCE((SELECT external_id FROM m_external_asset_owner WHERE id = loan.assetowner),\n ''self'') AS assetowner,\n loan.openingbalance AS beginningbalance,\n (loan.debitamount * 1) AS debitmovement,\n (loan.creditamount * -1) AS creditmovement,\n (loan.openingbalance + loan.debitamount - loan.creditamount) AS endingbalance,\n loan.originator_external_ids AS originator_external_ids\n FROM (SELECT DISTINCT g.pname AS pname,\n g.gl_code AS gl_code,\n g.glname AS glname,\n COALESCE(mh.assetowner, c.assetowner, 0) AS assetowner,\n COALESCE(mh.debitamount, 0) - COALESCE(mh.creditamount, 0) AS openingbalance,\n COALESCE(c.debitamount, 0) AS debitamount,\n COALESCE(c.creditamount, 0) AS creditamount,\n COALESCE(mh.originator_external_ids, c.originator_external_ids) AS originator_external_ids\n FROM (SELECT DISTINCT ag.gl_code, ag.id, pl.NAME AS pname, ag.NAME AS glname\n FROM acc_gl_account ag\n JOIN acc_product_mapping am ON am.gl_account_id = ag.id AND am.product_type = 1\n JOIN m_product_loan pl ON pl.id = am.product_id) g\n LEFT JOIN merged_historical_data mh\n ON g.gl_code = mh.glcode\n AND mh.productname = g.pname\n LEFT JOIN current_cob_data c\n ON g.gl_code = c.glcode\n AND c.productname = g.pname\n AND mh.assetowner = c.assetowner\n AND mh.originator_external_ids = c.originator_external_ids\n\n UNION ALL\n\n SELECT DISTINCT c.productname AS pname,\n c.glcode AS gl_code,\n c.glname AS glname,\n COALESCE(c.assetowner, 0) AS assetowner,\n 0 AS openingbalance,\n COALESCE(c.debitamount, 0) AS debitamount,\n COALESCE(c.creditamount, 0) AS creditamount,\n COALESCE(matched.originator_external_ids, c.originator_external_ids) AS originator_external_ids\n FROM current_cob_data c\n LEFT JOIN (SELECT g3.gl_code, g3.pname, mh.assetowner, mh.originator_external_ids\n FROM (SELECT DISTINCT ag.gl_code, pl.NAME AS pname\n FROM acc_gl_account ag\n JOIN acc_product_mapping am\n ON am.gl_account_id = ag.id AND am.product_type = 1\n JOIN m_product_loan pl ON pl.id = am.product_id) g3\n LEFT JOIN merged_historical_data mh\n ON g3.gl_code = mh.glcode\n AND mh.productname = g3.pname) matched\n ON matched.gl_code = c.glcode\n AND matched.pname = c.productname\n AND matched.assetowner = c.assetowner\n AND matched.originator_external_ids = c.originator_external_ids\n WHERE matched.gl_code IS NULL) loan) a) AS txnreport\n LEFT JOIN retained_earning summary\n ON txnreport.glacct = summary.glacct\n AND txnreport.assetowner = summary.assetowner\n AND summary.product = txnreport.product\n AND summary.originator_external_ids = txnreport.originator_external_ids) report\nWHERE report.endingbalance != 0\n OR report.debitmovement != 0\n OR report.creditmovement != 0\nORDER BY glacct', 'Trial Balance Summary Report with Asset Owner', 'f', 't', 'f');
INSERT INTO public.stretchy_report (id, report_name, report_type, report_subtype, report_category, report_sql, description, core_report, use_report, self_service_user_report) VALUES ('200', 'Transaction Summary Report with Asset Owner', 'Table', NULL, 'Accounting', 'WITH slt_except_charge_adj_and_accrual AS (SELECT ''${endDate}'' AS transactiondate,\n          t.id,\n          l.name,\n          d.payment_type_id,\n          CASE\n              WHEN d.payment_type_id IS NULL AND t.classification_cv_id IS NOT NULL\n                  THEN (SELECT code_value FROM m_code_value WHERE id = t.classification_cv_id)\n              ELSE NULL END AS classification_name,\n          t.transaction_type_enum,\n          t.amount,\n          t.overpayment_portion_derived,\n          t.principal_portion_derived,\n          t.interest_portion_derived,\n          t.fee_charges_portion_derived,\n          t.penalty_charges_portion_derived,\n          e.status,\n          e.settlement_date,\n          e.owner_id,\n          m.charged_off_on_date,\n          t.transaction_date,\n          m.charge_off_reason_cv_id,\n          (SELECT STRING_AGG(DISTINCT mlo.external_id, '', '' ORDER BY mlo.external_id) FROM m_loan_originator_mapping mlom JOIN m_loan_originator mlo ON mlo.id = mlom.originator_id WHERE mlom.loan_id = t.loan_id) AS originator_external_ids\n   FROM m_loan_transaction t\n            JOIN m_loan m ON m.id = t.loan_id\n            JOIN m_product_loan l ON l.id = m.product_id\n            LEFT JOIN m_payment_detail d ON d.id = t.payment_detail_id\n            LEFT JOIN m_external_asset_owner_transfer e\n                      ON e.loan_id = t.loan_id AND\n     e.settlement_date < ''${endDate}'' AND\n     e.effective_date_to >= ''${endDate}''\n   WHERE t.submitted_on_date = ''${endDate}''\n     AND t.transaction_type_enum not in (10, 26, 32, 34, 36, 39, 42, 43)\n     AND (t.office_id = ${officeId})),\n     slt_charge_adj AS (SELECT ''${endDate}'' AS transactiondate,\n           t.id,\n           l.name,\n           t.transaction_type_enum,\n           d.payment_type_id,\n           t.overpayment_portion_derived,\n           t.principal_portion_derived,\n           t.interest_portion_derived,\n           t.fee_charges_portion_derived,\n           t.penalty_charges_portion_derived,\n           t.amount,\n           e.status,\n           e.settlement_date,\n           e.owner_id,\n           m.charged_off_on_date,\n           t.transaction_date,\n           m.charge_off_reason_cv_id,\n           (SELECT STRING_AGG(DISTINCT mlo.external_id, '', '' ORDER BY mlo.external_id) FROM m_loan_originator_mapping mlom JOIN m_loan_originator mlo ON mlo.id = mlom.originator_id WHERE mlom.loan_id = t.loan_id) AS originator_external_ids\n    FROM m_loan_transaction t\n             JOIN m_loan m ON m.id = t.loan_id\n             JOIN m_product_loan l ON l.id = m.product_id\n             LEFT JOIN m_payment_detail d ON d.id = t.payment_detail_id\n             LEFT JOIN m_external_asset_owner_transfer e\n   ON e.loan_id = t.loan_id AND e.settlement_date < ''${endDate}'' AND\n      e.effective_date_to >= ''${endDate}''\n    WHERE t.submitted_on_date = ''${endDate}''\n      AND t.transaction_type_enum = 26\n      AND (t.office_id = ${officeId})),\n     rlt_except_charge_adj_and_accrual AS (SELECT ''${endDate}'' AS transactiondate,\n          t.id,\n          l.name,\n          t.transaction_type_enum,\n          d.payment_type_id,\n          CASE\n              WHEN d.payment_type_id IS NULL AND t.classification_cv_id IS NOT NULL\n                  THEN (SELECT code_value FROM m_code_value WHERE id = t.classification_cv_id)\n              ELSE NULL END AS classification_name,\n          t.overpayment_portion_derived,\n          t.principal_portion_derived,\n          t.interest_portion_derived,\n          t.fee_charges_portion_derived,\n          t.penalty_charges_portion_derived,\n          t.amount,\n          e.status,\n          e.settlement_date,\n          e.owner_id,\n          m.charged_off_on_date,\n          t.transaction_date,\n          m.charge_off_reason_cv_id,\n          (SELECT STRING_AGG(DISTINCT mlo.external_id, '', '' ORDER BY mlo.external_id) FROM m_loan_originator_mapping mlom JOIN m_loan_originator mlo ON mlo.id = mlom.originator_id WHERE mlom.loan_id = t.loan_id) AS originator_external_ids\n   FROM m_loan_transaction t\n            JOIN m_loan m ON m.id = t.loan_id\n            JOIN m_product_loan l ON l.id = m.product_id\n            LEFT JOIN m_payment_detail d ON d.id = t.payment_detail_id\n            LEFT JOIN m_external_asset_owner_transfer e\n                      ON e.loan_id = t.loan_id AND\n     e.settlement_date < ''${endDate}'' AND\n     e.effective_date_to >= ''${endDate}''\n   WHERE t.reversed_on_date = ''${endDate}''\n     AND t.transaction_type_enum not in (10, 26, 32, 34, 36, 39, 42, 43)\n     AND (t.office_id = ${officeId})),\n     rlt_charge_adj AS (SELECT ''${endDate}'' AS transactiondate,\n           t.id,\n           l.name,\n           t.transaction_type_enum,\n           d.payment_type_id,\n           t.overpayment_portion_derived,\n           t.principal_portion_derived,\n           t.interest_portion_derived,\n           t.fee_charges_portion_derived,\n           t.penalty_charges_portion_derived,\n           t.amount,\n           e.status,\n           e.settlement_date,\n           e.owner_id,\n           m.charged_off_on_date,\n           t.transaction_date,\n           m.charge_off_reason_cv_id,\n           (SELECT STRING_AGG(DISTINCT mlo.external_id, '', '' ORDER BY mlo.external_id) FROM m_loan_originator_mapping mlom JOIN m_loan_originator mlo ON mlo.id = mlom.originator_id WHERE mlom.loan_id = t.loan_id) AS originator_external_ids\n    FROM m_loan_transaction t\n             JOIN m_loan m ON m.id = t.loan_id\n             JOIN m_product_loan l ON l.id = m.product_id\n             LEFT JOIN m_payment_detail d ON d.id = t.payment_detail_id\n             LEFT JOIN m_external_asset_owner_transfer e\n   ON e.loan_id = t.loan_id AND e.settlement_date < ''${endDate}'' AND\n      e.effective_date_to >= ''${endDate}''\n    WHERE t.reversed_on_date = ''${endDate}''\n      AND t.transaction_type_enum = 26\n      AND (t.office_id = ${officeId})),\n     slt_cap_income_amortization AS (SELECT ''${endDate}'' AS transactiondate,\n    t.id,\n    l.name,\n    t.transaction_type_enum,\n    d.payment_type_id,\n    CASE\n        WHEN d.payment_type_id IS NULL AND bt.classification_cv_id IS NOT NULL\n            THEN (SELECT code_value FROM m_code_value WHERE id = bt.classification_cv_id)\n        ELSE NULL END AS classification_name,\n    CASE\n        WHEN t.overpayment_portion_derived IS NOT NULL\n            THEN map.amount\n        ELSE NULL END AS overpayment_portion_derived,\n    CASE\n        WHEN t.principal_portion_derived IS NOT NULL\n            THEN map.amount\n        ELSE NULL END AS principal_portion_derived,\n    CASE\n        WHEN t.interest_portion_derived IS NOT NULL\n            THEN map.amount\n        ELSE NULL END AS interest_portion_derived,\n    CASE\n        WHEN t.fee_charges_portion_derived IS NOT NULL\n            THEN map.amount\n        ELSE NULL END AS fee_charges_portion_derived,\n    CASE\n        WHEN t.penalty_charges_portion_derived IS NOT NULL\n            THEN map.amount\n        ELSE NULL END AS penalty_charges_portion_derived,\n    map.amount,\n    e.status,\n    e.settlement_date,\n    e.owner_id,\n    m.charged_off_on_date,\n    t.transaction_date,\n    m.charge_off_reason_cv_id,\n    (SELECT STRING_AGG(DISTINCT mlo.external_id, '', '' ORDER BY mlo.external_id) FROM m_loan_originator_mapping mlom JOIN m_loan_originator mlo ON mlo.id = mlom.originator_id WHERE mlom.loan_id = t.loan_id) AS originator_external_ids\n                 FROM m_loan_transaction t\n      JOIN m_loan m ON m.id = t.loan_id\n      JOIN m_product_loan l ON l.id = m.product_id\n      LEFT JOIN m_payment_detail d ON d.id = t.payment_detail_id\n      JOIN m_loan_amortization_allocation_mapping map\n           ON map.amortization_loan_transaction_id = t.id\n      JOIN m_loan_transaction bt ON bt.id = map.base_loan_transaction_id\n      LEFT JOIN m_external_asset_owner_transfer e ON e.loan_id = t.loan_id AND\n             e.settlement_date <\n             ''${endDate}'' AND\n             e.effective_date_to >=\n             ''${endDate}''\n                 WHERE t.submitted_on_date = ''${endDate}''\n                   AND t.is_reversed = false\n                   AND t.transaction_type_enum IN (36, 39, 42, 43)\n                   AND (t.office_id = ${officeId})),\n     rlt_cap_income_amortization AS (SELECT ''${endDate}'' AS transactiondate,\n    t.id,\n    l.name,\n    t.transaction_type_enum,\n    d.payment_type_id,\n    CASE\n        WHEN d.payment_type_id IS NULL AND bt.classification_cv_id IS NOT NULL\n            THEN (SELECT code_value FROM m_code_value WHERE id = bt.classification_cv_id)\n        ELSE NULL END AS classification_name,\n    CASE\n        WHEN t.overpayment_portion_derived IS NOT NULL\n            THEN map.amount\n        ELSE NULL END AS overpayment_portion_derived,\n    CASE\n        WHEN t.principal_portion_derived IS NOT NULL\n            THEN map.amount\n        ELSE NULL END AS principal_portion_derived,\n    CASE\n        WHEN t.interest_portion_derived IS NOT NULL\n            THEN map.amount\n        ELSE NULL END AS interest_portion_derived,\n    CASE\n        WHEN t.fee_charges_portion_derived IS NOT NULL\n            THEN map.amount\n        ELSE NULL END AS fee_charges_portion_derived,\n    CASE\n        WHEN t.penalty_charges_portion_derived IS NOT NULL\n            THEN map.amount\n        ELSE NULL END AS penalty_charges_portion_derived,\n    map.amount,\n    e.status,\n    e.settlement_date,\n    e.owner_id,\n    m.charged_off_on_date,\n    t.transaction_date,\n    m.charge_off_reason_cv_id,\n    (SELECT STRING_AGG(DISTINCT mlo.external_id, '', '' ORDER BY mlo.external_id) FROM m_loan_originator_mapping mlom JOIN m_loan_originator mlo ON mlo.id = mlom.originator_id WHERE mlom.loan_id = t.loan_id) AS originator_external_ids\n                 FROM m_loan_transaction t\n      JOIN m_loan m ON m.id = t.loan_id\n      JOIN m_product_loan l ON l.id = m.product_id\n      LEFT JOIN m_payment_detail d ON d.id = t.payment_detail_id\n      JOIN m_loan_amortization_allocation_mapping map\n           ON map.amortization_loan_transaction_id = t.id\n      JOIN m_loan_transaction bt ON bt.id = map.base_loan_transaction_id\n      LEFT JOIN m_external_asset_owner_transfer e ON e.loan_id = t.loan_id AND\n             e.settlement_date <\n             ''${endDate}'' AND\n             e.effective_date_to >=\n             ''${endDate}''\n                 WHERE t.reversed_on_date = ''${endDate}''\n                   AND t.is_reversed = true\n                   AND t.transaction_type_enum IN (36, 39, 42, 43)\n                   AND (t.office_id = ${officeId})),\n     active_external_asset_owner_transfers AS (SELECT ''${endDate}'' AS transactiondate,\n              t.id,\n              p.name,\n              t.owner_id,\n              t.previous_owner_id,\n              dt.principal_outstanding_derived,\n              dt.interest_outstanding_derived,\n              dt.fee_charges_outstanding_derived,\n              dt.penalty_charges_outstanding_derived,\n              dt.total_overpaid_derived,\n              l.charged_off_on_date,\n              t.settlement_date,\n              l.charge_off_reason_cv_id,\n              (SELECT STRING_AGG(DISTINCT mlo.external_id, '', '' ORDER BY mlo.external_id) FROM m_loan_originator_mapping mlom JOIN m_loan_originator mlo ON mlo.id = mlom.originator_id WHERE mlom.loan_id = t.loan_id) AS originator_external_ids\n       FROM m_external_asset_owner_transfer t\n                JOIN m_loan l ON l.id = t.loan_id\n                JOIN m_client c ON c.id = l.client_id\n                JOIN m_product_loan p ON p.id = l.product_id\n                JOIN m_external_asset_owner_transfer_details dt\n                     ON dt.asset_owner_transfer_id = t.id\n       WHERE t.status in (''ACTIVE'', ''ACTIVE_INTERMEDIATE'')\n         AND c.office_id = ${officeId}\n         AND t.settlement_date = ''${endDate}''),\n     buyback_external_asset_owner_transfers AS (SELECT ''${endDate}'' AS transactiondate,\n               t.id,\n               p.name,\n               dt.principal_outstanding_derived,\n               dt.interest_outstanding_derived,\n               dt.fee_charges_outstanding_derived,\n               dt.penalty_charges_outstanding_derived,\n               dt.total_overpaid_derived,\n               l.charged_off_on_date,\n               t.settlement_date,\n               l.charge_off_reason_cv_id,\n               t.owner_id,\n               (SELECT STRING_AGG(DISTINCT mlo.external_id, '', '' ORDER BY mlo.external_id) FROM m_loan_originator_mapping mlom JOIN m_loan_originator mlo ON mlo.id = mlom.originator_id WHERE mlom.loan_id = t.loan_id) AS originator_external_ids\n        FROM m_external_asset_owner_transfer t\n                 JOIN m_loan l ON l.id = t.loan_id\n                 JOIN m_client c ON c.id = l.client_id\n                 JOIN m_product_loan p ON p.id = l.product_id\n                 JOIN m_external_asset_owner_transfer_details dt\n                      ON dt.asset_owner_transfer_id = t.id\n        WHERE t.status in (''BUYBACK'', ''BUYBACK_INTERMEDIATE'')\n          AND c.office_id = ${officeId}\n          AND t.settlement_date = ''${endDate}'')\nSELECT ''${endDate}'' AS TransactionDate,\n       a.product AS Product,\n       CASE\n           WHEN a.transaction_type = 9999 THEN ''Asset Transfer''\n           WHEN a.transaction_type = 99999 THEN ''Asset Buyback''\n           ELSE (SELECT enum_message_property\n                 FROM r_enum_value\n                 WHERE enum_name = ''transaction_type_enum''\n                   AND enum_id = a.transaction_type) END AS TransactionType_Name,\n       COALESCE((SELECT value FROM m_payment_type WHERE id = a.payment_type_id),\n                a.classification_name) AS PaymentType_Name,\n       a.chargetype AS chargetype,\n       a.reversal_indicator AS Reversed,\n       a.Allocation_Type AS Allocation_Type,\n       (SELECT code_value FROM m_code_value WHERE id = a.charge_off_reason_id) AS Chargeoff_ReasonCode,\n       CASE\n           WHEN a.transaction_type = 9999 THEN sum(a.amount) * + 1\n           WHEN a.transaction_type = 99999 THEN sum(a.amount) * - 1\n           WHEN a.transaction_type IN (2, 23, 21, 22, 24, 4, 5, 8, 6, 27, 9, 26, 28, 31, 33, 34, 37, 39, 41, 43) AND\n                a.reversal_indicator = false THEN sum(a.amount) * -1\n           WHEN a.transaction_type IN (2, 23, 21, 22, 24, 4, 5, 8, 6, 27, 9, 26, 28, 31, 33, 34, 37, 39, 41, 43) AND\n                a.reversal_indicator = true THEN sum(a.amount) * + 1\n           WHEN a.transaction_type IN (1, 10, 25, 20, 35, 36, 40, 42) AND a.reversal_indicator = false THEN sum(a.amount) * + 1\n           WHEN a.transaction_type IN (1, 10, 25, 20, 35, 36, 40, 42) AND a.reversal_indicator = true\n               THEN sum(a.amount) * -1 END AS Transaction_Amount,\n       (SELECT external_id\n        FROM m_external_asset_owner\n        WHERE id = a.asset_owner_id) AS Asset_owner_id,\n       (SELECT external_id\n        FROM m_external_asset_owner\n        WHERE id = a.from_asset_owner_id) AS From_asset_owner_id,\n       a.originator_external_ids\nFROM (SELECT t.transactiondate,\n             t.id,\n             t.name AS product,\n             t.transaction_type_enum AS transaction_type,\n             t.payment_type_id,\n             t.classification_name,\n             '''' AS chargetype,\n             false AS reversal_indicator,\n             ''Principal'' AS Allocation_Type,\n             CASE\n                 WHEN t.transaction_type_enum in (1) THEN (CASE\n   WHEN t.amount is null THEN 0\n   WHEN t.overpayment_portion_derived is null THEN t.amount\n   WHEN t.overpayment_portion_derived is not null\n       THEN t.amount - t.overpayment_portion_derived\n   ELSE t.amount END)\n                 ELSE (CASE\n       WHEN t.principal_portion_derived is null THEN 0\n       ELSE t.principal_portion_derived end) END amount,\n             CASE\n                 WHEN t.status in (''ACTIVE'', ''ACTIVE_INTERMEDIATE'') AND t.settlement_date < ''${endDate}''\n THEN t.owner_id END AS asset_owner_id,\n             CASE\n                 WHEN t.transaction_type_enum = 27 OR (t.charged_off_on_date <= t.transaction_date)\n THEN t.charge_off_reason_cv_id END AS charge_off_reason_id,\n             null::bigint AS from_asset_owner_id,\n             t.originator_external_ids\n      FROM slt_except_charge_adj_and_accrual AS t\n      UNION ALL\n      SELECT t.transactiondate,\n             t.id,\n             t.name AS product,\n             t.transaction_type_enum AS transaction_type,\n             t.payment_type_id,\n             t.classification_name,\n             '''' AS chargetype,\n             false AS reversal_indicator,\n             ''Interest'' AS Allocation_Type,\n             CASE WHEN t.interest_portion_derived is null THEN 0 ELSE t.interest_portion_derived END AS amount,\n             CASE\n                 WHEN t.status in (''ACTIVE'', ''ACTIVE_INTERMEDIATE'') AND t.settlement_date < ''${endDate}''\n THEN t.owner_id END AS asset_owner_id,\n             CASE\n                 WHEN t.transaction_type_enum = 27 OR (t.charged_off_on_date <= t.transaction_date)\n THEN t.charge_off_reason_cv_id END AS charge_off_reason_id,\n             null::bigint AS from_asset_owner_id,\n             t.originator_external_ids\n      FROM slt_except_charge_adj_and_accrual AS t\n      UNION ALL\n      SELECT t.transactiondate,\n             t.id,\n             t.name AS product,\n             t.transaction_type_enum AS transaction_type,\n             t.payment_type_id,\n             t.classification_name,\n             '''' AS chargetype,\n             false AS reversal_indicator,\n             ''Fees'' AS Allocation_Type,\n             CASE WHEN t.fee_charges_portion_derived is null THEN 0 ELSE t.fee_charges_portion_derived END AS amount,\n             CASE\n                 WHEN t.status in (''ACTIVE'', ''ACTIVE_INTERMEDIATE'') AND t.settlement_date < ''${endDate}''\n THEN t.owner_id END AS asset_owner_id,\n             CASE\n                 WHEN t.transaction_type_enum = 27 OR (t.charged_off_on_date <= t.transaction_date)\n THEN t.charge_off_reason_cv_id END AS charge_off_reason_id,\n             null::bigint AS from_asset_owner_id,\n             t.originator_external_ids\n      FROM slt_except_charge_adj_and_accrual AS t\n      UNION ALL\n      SELECT t.transactiondate,\n             t.id,\n             t.name AS product,\n             t.transaction_type_enum AS transaction_type,\n             t.payment_type_id,\n             t.classification_name,\n             '''' AS chargetype,\n             false AS reversal_indicator,\n             ''Penalty'' AS Allocation_Type,\n             CASE\n                 WHEN t.penalty_charges_portion_derived is null THEN 0\n                 ELSE t.penalty_charges_portion_derived END AS amount,\n             CASE\n                 WHEN t.status in (''ACTIVE'', ''ACTIVE_INTERMEDIATE'') AND t.settlement_date < ''${endDate}''\n THEN t.owner_id END AS asset_owner_id,\n             CASE\n                 WHEN t.transaction_type_enum = 27 OR (t.charged_off_on_date <= t.transaction_date)\n THEN t.charge_off_reason_cv_id END AS charge_off_reason_id,\n             null::bigint AS from_asset_owner_id,\n             t.originator_external_ids\n      FROM slt_except_charge_adj_and_accrual AS t\n      UNION ALL\n      SELECT t.transactiondate,\n             t.id,\n             t.name AS product,\n             t.transaction_type_enum AS transaction_type,\n             t.payment_type_id,\n             t.classification_name,\n             '''' AS chargetype,\n             false AS reversal_indicator,\n             ''Unallocated Credit (UNC)'' AS Allocation_Type,\n             CASE WHEN t.overpayment_portion_derived is null THEN 0 ELSE t.overpayment_portion_derived END AS amount,\n             CASE\n                 WHEN t.status in (''ACTIVE'', ''ACTIVE_INTERMEDIATE'') AND t.settlement_date < ''${endDate}''\n THEN t.owner_id END AS asset_owner_id,\n             CASE\n                 WHEN t.transaction_type_enum = 27 OR (t.charged_off_on_date <= t.transaction_date)\n THEN t.charge_off_reason_cv_id END AS charge_off_reason_id,\n             null::bigint AS from_asset_owner_id,\n             t.originator_external_ids\n      FROM slt_except_charge_adj_and_accrual AS t\n      UNION ALL\n      SELECT ''${endDate}'' AS transactiondate,\n             t.id,\n             t.name AS product,\n             t.transaction_type_enum AS transaction_type,\n             t.payment_type_id,\n             t.classification_name,\n             '''' AS chargetype,\n             false AS reversal_indicator,\n             ''Fees'' AS Allocation_Type,\n             CASE WHEN t.fee_charges_portion_derived is null THEN 0 ELSE t.fee_charges_portion_derived END AS amount,\n             CASE\n                 WHEN t.status in (''ACTIVE'', ''ACTIVE_INTERMEDIATE'') AND t.settlement_date < ''${endDate}''\n THEN t.owner_id END AS asset_owner_id,\n             CASE\n                 WHEN t.charged_off_on_date <= t.transaction_date\n THEN t.charge_off_reason_cv_id END AS charge_off_reason_id,\n             null::bigint AS from_asset_owner_id,\n             t.originator_external_ids\n      FROM slt_cap_income_amortization AS t\n      UNION ALL\n      SELECT ''${endDate}'' AS transactiondate,\n             t.id,\n             t.name AS product,\n             t.transaction_type_enum AS transaction_type,\n             t.payment_type_id,\n             t.classification_name,\n             '''' AS chargetype,\n             false AS reversal_indicator,\n             ''Penalty'' AS Allocation_Type,\n             CASE\n                 WHEN t.penalty_charges_portion_derived is null THEN 0\n                 ELSE t.penalty_charges_portion_derived END AS amount,\n             CASE\n                 WHEN t.status in (''ACTIVE'', ''ACTIVE_INTERMEDIATE'') AND t.settlement_date < ''${endDate}''\n THEN t.owner_id END AS asset_owner_id,\n             CASE\n                 WHEN t.charged_off_on_date <= t.transaction_date\n THEN t.charge_off_reason_cv_id END AS charge_off_reason_id,\n             null::bigint AS from_asset_owner_id,\n             t.originator_external_ids\n      FROM slt_cap_income_amortization AS t\n      UNION ALL\n      SELECT ''${endDate}'' AS transactiondate,\n             t.id,\n             t.name AS product,\n             t.transaction_type_enum AS transaction_type,\n             t.payment_type_id,\n             t.classification_name,\n             '''' AS chargetype,\n             false AS reversal_indicator,\n             ''Interest'' AS Allocation_Type,\n             CASE WHEN t.interest_portion_derived is null THEN 0 ELSE t.interest_portion_derived END AS amount,\n             CASE\n                 WHEN t.status in (''ACTIVE'', ''ACTIVE_INTERMEDIATE'') AND t.settlement_date < ''${endDate}''\n THEN t.owner_id END AS asset_owner_id,\n             CASE\n                 WHEN t.charged_off_on_date <= t.transaction_date\n THEN t.charge_off_reason_cv_id END AS charge_off_reason_id,\n             null::bigint AS from_asset_owner_id,\n             t.originator_external_ids\n      FROM slt_cap_income_amortization AS t\n      UNION ALL\n      SELECT ''${endDate}'' AS transactiondate,\n             t.id,\n             t.name AS product,\n             t.transaction_type_enum AS transaction_type,\n             t.payment_type_id,\n             t.classification_name,\n             '''' AS chargetype,\n             false AS reversal_indicator,\n             ''Principal'' AS Allocation_Type,\n             CASE WHEN t.principal_portion_derived is null THEN 0 ELSE t.principal_portion_derived END AS amount,\n             CASE\n                 WHEN t.status in (''ACTIVE'', ''ACTIVE_INTERMEDIATE'') AND t.settlement_date < ''${endDate}''\n THEN t.owner_id END AS asset_owner_id,\n             CASE\n                 WHEN t.charged_off_on_date <= t.transaction_date\n THEN t.charge_off_reason_cv_id END AS charge_off_reason_id,\n             null::bigint AS from_asset_owner_id,\n             t.originator_external_ids\n      FROM slt_cap_income_amortization AS t\n      UNION ALL\n      SELECT ''${endDate}'' AS transactiondate,\n             t.id,\n             t.name AS product,\n             t.transaction_type_enum AS transaction_type,\n             t.payment_type_id,\n             t.classification_name,\n             '''' AS chargetype,\n             false AS reversal_indicator,\n             ''Unallocated Credit (UNC)'' AS Allocation_Type,\n             CASE WHEN t.overpayment_portion_derived is null THEN 0 ELSE t.overpayment_portion_derived END AS amount,\n             CASE\n                 WHEN t.status in (''ACTIVE'', ''ACTIVE_INTERMEDIATE'') AND t.settlement_date < ''${endDate}''\n THEN t.owner_id END AS asset_owner_id,\n             CASE\n                 WHEN t.charged_off_on_date <= t.transaction_date\n THEN t.charge_off_reason_cv_id END AS charge_off_reason_id,\n             null::bigint AS from_asset_owner_id,\n             t.originator_external_ids\n      FROM slt_cap_income_amortization AS t\n      UNION ALL\n      SELECT ''${endDate}'' AS transactiondate,\n             t.id,\n             t.name AS product,\n             t.transaction_type_enum AS transaction_type,\n             t.payment_type_id,\n             t.classification_name,\n             '''' AS chargetype,\n             false AS reversal_indicator,\n             ''Fees'' AS Allocation_Type,\n             CASE WHEN t.fee_charges_portion_derived is null THEN 0 ELSE t.fee_charges_portion_derived END AS amount,\n             CASE\n                 WHEN t.status in (''ACTIVE'', ''ACTIVE_INTERMEDIATE'') AND t.settlement_date < ''${endDate}''\n THEN t.owner_id END AS asset_owner_id,\n             CASE\n                 WHEN t.charged_off_on_date <= t.transaction_date\n THEN t.charge_off_reason_cv_id END AS charge_off_reason_id,\n             null::bigint AS from_asset_owner_id,\n             t.originator_external_ids\n      FROM rlt_cap_income_amortization AS t\n      UNION ALL\n      SELECT ''${endDate}'' AS transactiondate,\n             t.id,\n             t.name AS product,\n             t.transaction_type_enum AS transaction_type,\n             t.payment_type_id,\n             t.classification_name,\n             '''' AS chargetype,\n             false AS reversal_indicator,\n             ''Penalty'' AS Allocation_Type,\n             CASE\n                 WHEN t.penalty_charges_portion_derived is null THEN 0\n                 ELSE t.penalty_charges_portion_derived END AS amount,\n             CASE\n                 WHEN t.status in (''ACTIVE'', ''ACTIVE_INTERMEDIATE'') AND t.settlement_date < ''${endDate}''\n THEN t.owner_id END AS asset_owner_id,\n             CASE\n                 WHEN t.charged_off_on_date <= t.transaction_date\n THEN t.charge_off_reason_cv_id END AS charge_off_reason_id,\n             null::bigint AS from_asset_owner_id,\n             t.originator_external_ids\n      FROM rlt_cap_income_amortization AS t\n      UNION ALL\n      SELECT ''${endDate}'' AS transactiondate,\n             t.id,\n             t.name AS product,\n             t.transaction_type_enum AS transaction_type,\n             t.payment_type_id,\n             t.classification_name,\n             '''' AS chargetype,\n             false AS reversal_indicator,\n             ''Interest'' AS Allocation_Type,\n             CASE WHEN t.interest_portion_derived is null THEN 0 ELSE t.interest_portion_derived END AS amount,\n             CASE\n                 WHEN t.status in (''ACTIVE'', ''ACTIVE_INTERMEDIATE'') AND t.settlement_date < ''${endDate}''\n THEN t.owner_id END AS asset_owner_id,\n             CASE\n                 WHEN t.charged_off_on_date <= t.transaction_date\n THEN t.charge_off_reason_cv_id END AS charge_off_reason_id,\n             null::bigint AS from_asset_owner_id,\n             t.originator_external_ids\n      FROM rlt_cap_income_amortization AS t\n      UNION ALL\n      SELECT ''${endDate}'' AS transactiondate,\n             t.id,\n             t.name AS product,\n             t.transaction_type_enum AS transaction_type,\n             t.payment_type_id,\n             t.classification_name,\n             '''' AS chargetype,\n             false AS reversal_indicator,\n             ''Principal'' AS Allocation_Type,\n             CASE WHEN t.principal_portion_derived is null THEN 0 ELSE t.principal_portion_derived END AS amount,\n             CASE\n                 WHEN t.status in (''ACTIVE'', ''ACTIVE_INTERMEDIATE'') AND t.settlement_date < ''${endDate}''\n THEN t.owner_id END AS asset_owner_id,\n             CASE\n                 WHEN t.charged_off_on_date <= t.transaction_date\n THEN t.charge_off_reason_cv_id END AS charge_off_reason_id,\n             null::bigint AS from_asset_owner_id,\n             t.originator_external_ids\n      FROM rlt_cap_income_amortization AS t\n      UNION ALL\n      SELECT ''${endDate}'' AS transactiondate,\n             t.id,\n             t.name AS product,\n             t.transaction_type_enum AS transaction_type,\n             t.payment_type_id,\n             t.classification_name,\n             '''' AS chargetype,\n             false AS reversal_indicator,\n             ''Unallocated Credit (UNC)'' AS Allocation_Type,\n             CASE WHEN t.overpayment_portion_derived is null THEN 0 ELSE t.overpayment_portion_derived END AS amount,\n             CASE\n                 WHEN t.status in (''ACTIVE'', ''ACTIVE_INTERMEDIATE'') AND t.settlement_date < ''${endDate}''\n THEN t.owner_id END AS asset_owner_id,\n             CASE\n                 WHEN t.charged_off_on_date <= t.transaction_date\n THEN t.charge_off_reason_cv_id END AS charge_off_reason_id,\n             null::bigint AS from_asset_owner_id,\n             t.originator_external_ids\n      FROM rlt_cap_income_amortization AS t\n      UNION ALL\n      SELECT ''${endDate}'' AS transactiondate,\n             t.id,\n             l.name AS product,\n             t.transaction_type_enum AS transaction_type,\n             null AS payment_type_id,\n             null AS classification_name,\n             mc.name AS chargetype,\n             false AS reversal_indicator,\n             ''Fees'' AS Allocation_Type,\n             CASE WHEN pd.amount is null THEN 0 ELSE pd.amount END AS amount,\n             CASE\n                 WHEN e.status in (''ACTIVE'', ''ACTIVE_INTERMEDIATE'') AND e.settlement_date < ''${endDate}''\n THEN e.owner_id END AS asset_owner_id,\n             CASE\n                 WHEN t.transaction_type_enum = 27 OR (m.charged_off_on_date <= t.transaction_date)\n THEN m.charge_off_reason_cv_id END AS charge_off_reason_id,\n             null::bigint AS from_asset_owner_id,\n             (SELECT STRING_AGG(DISTINCT mlo.external_id, '', '' ORDER BY mlo.external_id) FROM m_loan_originator_mapping mlom JOIN m_loan_originator mlo ON mlo.id = mlom.originator_id WHERE mlom.loan_id = t.loan_id) AS originator_external_ids\n      FROM m_loan_transaction t\n               JOIN m_loan m ON m.id = t.loan_id\n               JOIN m_product_loan l ON l.id = m.product_id\n               JOIN m_loan_charge_paid_by pd ON pd.loan_transaction_id = t.id\n               JOIN m_loan_charge c ON c.id = pd.loan_charge_id\n               JOIN m_charge mc ON mc.id = c.charge_id AND mc.is_penalty = false\n               LEFT JOIN m_external_asset_owner_transfer e\n     ON e.loan_id = t.loan_id AND e.settlement_date < ''${endDate}'' AND\n        e.effective_date_to >= ''${endDate}''\n      WHERE t.submitted_on_date = ''${endDate}''\n        AND t.transaction_type_enum = 10\n        AND t.is_reversed = false\n        AND (t.office_id = ${officeId})\n      UNION ALL\n      SELECT ''${endDate}'' AS transactiondate,\n             t.id,\n             l.name AS product,\n             t.transaction_type_enum AS transaction_type,\n             null AS payment_type_id,\n             null AS classification_name,\n             mc.name AS chargetype,\n             false AS reversal_indicator,\n             ''Penalty'' AS Allocation_Type,\n             CASE WHEN pd.amount is null THEN 0 ELSE pd.amount END AS amount,\n             CASE\n                 WHEN e.status in (''ACTIVE'', ''ACTIVE_INTERMEDIATE'') AND e.settlement_date < ''${endDate}''\n THEN e.owner_id END AS asset_owner_id,\n             CASE\n                 WHEN t.transaction_type_enum = 27 OR (m.charged_off_on_date <= t.transaction_date)\n THEN m.charge_off_reason_cv_id END AS charge_off_reason_id,\n             null::bigint AS from_asset_owner_id,\n             (SELECT STRING_AGG(DISTINCT mlo.external_id, '', '' ORDER BY mlo.external_id) FROM m_loan_originator_mapping mlom JOIN m_loan_originator mlo ON mlo.id = mlom.originator_id WHERE mlom.loan_id = t.loan_id) AS originator_external_ids\n      FROM m_loan_transaction t\n               JOIN m_loan m ON m.id = t.loan_id\n               JOIN m_product_loan l ON l.id = m.product_id\n               JOIN m_loan_charge_paid_by pd ON pd.loan_transaction_id = t.id\n               JOIN m_loan_charge c ON c.id = pd.loan_charge_id\n               JOIN m_charge mc ON mc.id = c.charge_id AND mc.is_penalty = true\n               LEFT JOIN m_external_asset_owner_transfer e\n     ON e.loan_id = t.loan_id AND e.settlement_date < ''${endDate}'' AND\n        e.effective_date_to >= ''${endDate}''\n      WHERE t.submitted_on_date = ''${endDate}''\n        AND t.transaction_type_enum = 10\n        AND t.is_reversed = false\n        AND (t.office_id = ${officeId})\n      UNION ALL\n      SELECT ''${endDate}'' AS transactiondate,\n             t.id,\n             l.name AS product,\n             t.transaction_type_enum AS transaction_type,\n             null AS payment_type_id,\n             null AS classification_name,\n             '''' AS chargetype,\n             false AS reversal_indicator,\n             ''Interest'' AS Allocation_Type,\n             CASE WHEN t.interest_portion_derived is null THEN 0 ELSE t.interest_portion_derived END AS amount,\n             CASE\n                 WHEN e.status in (''ACTIVE'', ''ACTIVE_INTERMEDIATE'') AND e.settlement_date < ''${endDate}''\n THEN e.owner_id END AS asset_owner_id,\n             CASE\n                 WHEN t.transaction_type_enum = 27 OR (m.charged_off_on_date <= t.transaction_date)\n THEN m.charge_off_reason_cv_id END AS charge_off_reason_id,\n             null::bigint AS from_asset_owner_id,\n             (SELECT STRING_AGG(DISTINCT mlo.external_id, '', '' ORDER BY mlo.external_id) FROM m_loan_originator_mapping mlom JOIN m_loan_originator mlo ON mlo.id = mlom.originator_id WHERE mlom.loan_id = t.loan_id) AS originator_external_ids\n      FROM m_loan_transaction t\n               JOIN m_loan m ON m.id = t.loan_id\n               JOIN m_product_loan l ON l.id = m.product_id\n               LEFT JOIN m_external_asset_owner_transfer e\n     ON e.loan_id = t.loan_id AND e.settlement_date < ''${endDate}'' AND\n        e.effective_date_to >= ''${endDate}''\n      WHERE t.submitted_on_date = ''${endDate}''\n        AND t.transaction_type_enum in (10, 34)\n        AND t.is_reversed = false\n        AND (t.office_id = ${officeId})\n      UNION ALL\n      SELECT ''${endDate}'' AS transactiondate,\n             t.id,\n             t.name AS product,\n             t.transaction_type_enum AS transaction_type,\n             t.payment_type_id,\n             null AS classification_name,\n             '''' AS chargetype,\n             false AS reversal_indicator,\n             ''Fees'' AS Allocation_Type,\n             CASE WHEN t.fee_charges_portion_derived is null THEN 0 ELSE t.fee_charges_portion_derived END AS amount,\n             CASE\n                 WHEN t.status in (''ACTIVE'', ''ACTIVE_INTERMEDIATE'') AND t.settlement_date < ''${endDate}''\n THEN t.owner_id END AS asset_owner_id,\n             CASE\n                 WHEN t.transaction_type_enum = 27 OR (t.charged_off_on_date <= t.transaction_date)\n THEN t.charge_off_reason_cv_id END AS charge_off_reason_id,\n             null::bigint AS from_asset_owner_id,\n             t.originator_external_ids\n      FROM slt_charge_adj AS t\n      UNION ALL\n      SELECT ''${endDate}'' AS transactiondate,\n             t.id,\n             t.name AS product,\n             t.transaction_type_enum AS transaction_type,\n             t.payment_type_id,\n             null AS classification_name,\n             '''' AS chargetype,\n             false AS reversal_indicator,\n             ''Penalty'' AS Allocation_Type,\n             CASE\n                 WHEN t.penalty_charges_portion_derived is null THEN 0\n                 ELSE t.penalty_charges_portion_derived END AS amount,\n             CASE\n                 WHEN t.status in (''ACTIVE'', ''ACTIVE_INTERMEDIATE'') AND t.settlement_date < ''${endDate}''\n THEN t.owner_id END AS asset_owner_id,\n             CASE\n                 WHEN t.transaction_type_enum = 27 OR (t.charged_off_on_date <= t.transaction_date)\n THEN t.charge_off_reason_cv_id END AS charge_off_reason_id,\n             null::bigint AS from_asset_owner_id,\n             t.originator_external_ids\n      FROM slt_charge_adj AS t\n      UNION ALL\n      SELECT ''${endDate}'' AS transactiondate,\n             t.id,\n             t.name AS product,\n             t.transaction_type_enum AS transaction_type,\n             t.payment_type_id,\n             null AS classification_name,\n             '''' AS chargetype,\n             false AS reversal_indicator,\n             ''Interest'' AS Allocation_Type,\n             CASE WHEN t.interest_portion_derived is null THEN 0 ELSE t.interest_portion_derived END AS amount,\n             CASE\n                 WHEN t.status in (''ACTIVE'', ''ACTIVE_INTERMEDIATE'') AND t.settlement_date < ''${endDate}''\n THEN t.owner_id END AS asset_owner_id,\n             CASE\n                 WHEN t.transaction_type_enum = 27 OR (t.charged_off_on_date <= t.transaction_date)\n THEN t.charge_off_reason_cv_id END AS charge_off_reason_id,\n             null::bigint AS from_asset_owner_id,\n             t.originator_external_ids\n      FROM slt_charge_adj AS t\n      UNION ALL\n      SELECT ''${endDate}'' AS transactiondate,\n             t.id,\n             t.name AS product,\n             t.transaction_type_enum AS transaction_type,\n             t.payment_type_id,\n             null AS classification_name,\n             '''' AS chargetype,\n             false AS reversal_indicator,\n             ''Principal'' AS Allocation_Type,\n             CASE WHEN t.principal_portion_derived is null THEN 0 ELSE t.principal_portion_derived END AS amount,\n             CASE\n                 WHEN t.status in (''ACTIVE'', ''ACTIVE_INTERMEDIATE'') AND t.settlement_date < ''${endDate}''\n THEN t.owner_id END AS asset_owner_id,\n             CASE\n                 WHEN t.transaction_type_enum = 27 OR (t.charged_off_on_date <= t.transaction_date)\n THEN t.charge_off_reason_cv_id END AS charge_off_reason_id,\n             null::bigint AS from_asset_owner_id,\n             t.originator_external_ids\n      FROM slt_charge_adj AS t\n      UNION ALL\n      SELECT ''${endDate}'' AS transactiondate,\n             t.id,\n             t.name AS product,\n             t.transaction_type_enum AS transaction_type,\n             t.payment_type_id,\n             null AS classification_name,\n             '''' AS chargetype,\n             false AS reversal_indicator,\n             ''Unallocated Credit (UNC)'' AS Allocation_Type,\n             CASE WHEN t.overpayment_portion_derived is null THEN 0 ELSE t.overpayment_portion_derived END AS amount,\n             CASE\n                 WHEN t.status in (''ACTIVE'', ''ACTIVE_INTERMEDIATE'') AND t.settlement_date < ''${endDate}''\n THEN t.owner_id END AS asset_owner_id,\n             CASE\n                 WHEN t.transaction_type_enum = 27 OR (t.charged_off_on_date <= t.transaction_date)\n THEN t.charge_off_reason_cv_id END AS charge_off_reason_id,\n             null::bigint AS from_asset_owner_id,\n             t.originator_external_ids\n      FROM slt_charge_adj AS t\n      UNION ALL\n      SELECT ''${endDate}'' AS transactiondate,\n             t.id,\n             t.name AS product,\n             t.transaction_type_enum AS transaction_type,\n             t.payment_type_id,\n             t.classification_name,\n             '''' AS chargetype,\n             true AS reversal_indicator,\n             ''Principal'' AS Allocation_Type,\n             CASE\n                 WHEN t.transaction_type_enum in (1) THEN (CASE\n   WHEN t.amount is null THEN 0\n   WHEN t.overpayment_portion_derived is null THEN t.amount\n   WHEN t.overpayment_portion_derived is not null\n       THEN t.amount - t.overpayment_portion_derived\n   ELSE t.amount END)\n                 ELSE (CASE\n       WHEN t.principal_portion_derived is null THEN 0\n       ELSE t.principal_portion_derived end) END amount,\n             CASE\n                 WHEN t.status in (''ACTIVE'', ''ACTIVE_INTERMEDIATE'') AND t.settlement_date < ''${endDate}''\n THEN t.owner_id END AS asset_owner_id,\n             CASE\n                 WHEN t.transaction_type_enum = 27 OR (t.charged_off_on_date <= t.transaction_date)\n THEN t.charge_off_reason_cv_id END AS charge_off_reason_id,\n             null::bigint AS from_asset_owner_id,\n             t.originator_external_ids\n      FROM rlt_except_charge_adj_and_accrual AS t\n      UNION ALL\n      SELECT ''${endDate}'' AS transactiondate,\n             t.id,\n             t.name AS product,\n             t.transaction_type_enum AS transaction_type,\n             t.payment_type_id,\n             t.classification_name,\n             '''' AS chargetype,\n             true AS reversal_indicator,\n             ''Interest'' AS Allocation_Type,\n             CASE WHEN t.interest_portion_derived is null THEN 0 ELSE t.interest_portion_derived END AS amount,\n             CASE\n                 WHEN t.status in (''ACTIVE'', ''ACTIVE_INTERMEDIATE'') AND t.settlement_date < ''${endDate}''\n THEN t.owner_id END AS asset_owner_id,\n             CASE\n                 WHEN t.transaction_type_enum = 27 OR (t.charged_off_on_date <= t.transaction_date)\n THEN t.charge_off_reason_cv_id END AS charge_off_reason_id,\n             null::bigint AS from_asset_owner_id,\n             t.originator_external_ids\n      FROM rlt_except_charge_adj_and_accrual AS t\n      UNION ALL\n      SELECT ''${endDate}'' AS transactiondate,\n             t.id,\n             t.name AS product,\n             t.transaction_type_enum AS transaction_type,\n             t.payment_type_id,\n             t.classification_name,\n             '''' AS chargetype,\n             true AS reversal_indicator,\n             ''Fees'' AS Allocation_Type,\n             CASE WHEN t.fee_charges_portion_derived is null THEN 0 ELSE t.fee_charges_portion_derived END AS amount,\n             CASE\n                 WHEN t.status in (''ACTIVE'', ''ACTIVE_INTERMEDIATE'') AND t.settlement_date < ''${endDate}''\n THEN t.owner_id END AS asset_owner_id,\n             CASE\n                 WHEN t.transaction_type_enum = 27 OR (t.charged_off_on_date <= t.transaction_date)\n THEN t.charge_off_reason_cv_id END AS charge_off_reason_id,\n             null::bigint AS from_asset_owner_id,\n             t.originator_external_ids\n      FROM rlt_except_charge_adj_and_accrual AS t\n      UNION ALL\n      SELECT ''${endDate}'' AS transactiondate,\n             t.id,\n             t.name AS product,\n             t.transaction_type_enum AS transaction_type,\n             t.payment_type_id,\n             t.classification_name,\n             '''' AS chargetype,\n             true AS reversal_indicator,\n             ''Penalty'' AS Allocation_Type,\n             CASE\n                 WHEN t.penalty_charges_portion_derived is null THEN 0\n                 ELSE t.penalty_charges_portion_derived END AS amount,\n             CASE\n                 WHEN t.status in (''ACTIVE'', ''ACTIVE_INTERMEDIATE'') AND t.settlement_date < ''${endDate}''\n THEN t.owner_id END AS asset_owner_id,\n             CASE\n                 WHEN t.transaction_type_enum = 27 OR (t.charged_off_on_date <= t.transaction_date)\n THEN t.charge_off_reason_cv_id END AS charge_off_reason_id,\n             null::bigint AS from_asset_owner_id,\n             t.originator_external_ids\n      FROM rlt_except_charge_adj_and_accrual AS t\n      UNION ALL\n      SELECT ''${endDate}'' AS transactiondate,\n             t.id,\n             t.name AS product,\n             t.transaction_type_enum AS transaction_type,\n             t.payment_type_id,\n             t.classification_name,\n             '''' AS chargetype,\n             true AS reversal_indicator,\n             ''Unallocated Credit (UNC)'' AS Allocation_Type,\n             CASE WHEN t.overpayment_portion_derived is null THEN 0 ELSE t.overpayment_portion_derived END AS amount,\n             CASE\n                 WHEN t.status in (''ACTIVE'', ''ACTIVE_INTERMEDIATE'') AND t.settlement_date < ''${endDate}''\n THEN t.owner_id END AS asset_owner_id,\n             CASE\n                 WHEN t.transaction_type_enum = 27 OR (t.charged_off_on_date <= t.transaction_date)\n THEN t.charge_off_reason_cv_id END AS charge_off_reason_id,\n             null::bigint AS from_asset_owner_id,\n             t.originator_external_ids\n      FROM rlt_except_charge_adj_and_accrual AS t\n      UNION ALL\n      SELECT ''${endDate}'' AS transactiondate,\n             t.id,\n             l.name AS product,\n             t.transaction_type_enum AS transaction_type,\n             null AS payment_type_id,\n             null AS classification_name,\n             mc.name AS chargetype,\n             true AS reversal_indicator,\n             ''Fees'' AS Allocation_Type,\n             CASE WHEN pd.amount is null THEN 0 ELSE pd.amount END AS amount,\n             CASE\n                 WHEN e.status in (''ACTIVE'', ''ACTIVE_INTERMEDIATE'') AND e.settlement_date < ''${endDate}''\n THEN e.owner_id END AS asset_owner_id,\n             CASE\n                 WHEN t.transaction_type_enum = 27 OR (m.charged_off_on_date <= t.transaction_date)\n THEN m.charge_off_reason_cv_id END AS charge_off_reason_id,\n             null::bigint AS from_asset_owner_id,\n             (SELECT STRING_AGG(DISTINCT mlo.external_id, '', '' ORDER BY mlo.external_id) FROM m_loan_originator_mapping mlom JOIN m_loan_originator mlo ON mlo.id = mlom.originator_id WHERE mlom.loan_id = t.loan_id) AS originator_external_ids\n      FROM m_loan_transaction t\n               JOIN m_loan m ON m.id = t.loan_id\n               JOIN m_product_loan l ON l.id = m.product_id\n               JOIN m_loan_charge_paid_by pd ON pd.loan_transaction_id = t.id\n               JOIN m_loan_charge c ON c.id = pd.loan_charge_id\n               JOIN m_charge mc ON mc.id = c.charge_id AND mc.is_penalty = false\n               LEFT JOIN m_external_asset_owner_transfer e\n     ON e.loan_id = t.loan_id AND e.settlement_date < ''${endDate}'' AND\n        e.effective_date_to >= ''${endDate}''\n      WHERE t.reversed_on_date = ''${endDate}''\n        AND t.transaction_type_enum = 10\n        AND t.is_reversed = true\n        AND (t.office_id = ${officeId})\n      UNION ALL\n      SELECT ''${endDate}'' AS transactiondate,\n             t.id,\n             l.name AS product,\n             t.transaction_type_enum AS transaction_type,\n             null AS payment_type_id,\n             null AS classification_name,\n             mc.name AS chargetype,\n             true AS reversal_indicator,\n             ''Penalty'' AS Allocation_Type,\n             CASE WHEN pd.amount is null THEN 0 ELSE pd.amount END AS amount,\n             CASE\n                 WHEN e.status in (''ACTIVE'', ''ACTIVE_INTERMEDIATE'') AND e.settlement_date < ''${endDate}''\n THEN e.owner_id END AS asset_owner_id,\n             CASE\n                 WHEN t.transaction_type_enum = 27 OR (m.charged_off_on_date <= t.transaction_date)\n THEN m.charge_off_reason_cv_id END AS charge_off_reason_id,\n             null::bigint AS from_asset_owner_id,\n             (SELECT STRING_AGG(DISTINCT mlo.external_id, '', '' ORDER BY mlo.external_id) FROM m_loan_originator_mapping mlom JOIN m_loan_originator mlo ON mlo.id = mlom.originator_id WHERE mlom.loan_id = t.loan_id) AS originator_external_ids\n      FROM m_loan_transaction t\n               JOIN m_loan m ON m.id = t.loan_id\n               JOIN m_product_loan l ON l.id = m.product_id\n               JOIN m_loan_charge_paid_by pd ON pd.loan_transaction_id = t.id\n               JOIN m_loan_charge c ON c.id = pd.loan_charge_id\n               JOIN m_charge mc ON mc.id = c.charge_id AND mc.is_penalty = true\n               LEFT JOIN m_external_asset_owner_transfer e\n     ON e.loan_id = t.loan_id AND e.settlement_date < ''${endDate}'' AND\n        e.effective_date_to >= ''${endDate}''\n      WHERE t.reversed_on_date = ''${endDate}''\n        AND t.transaction_type_enum = 10\n        AND t.is_reversed = true\n        AND (t.office_id = ${officeId})\n      UNION ALL\n      SELECT ''${endDate}'' AS transactiondate,\n             t.id,\n             l.name AS product,\n             t.transaction_type_enum AS transaction_type,\n             null AS payment_type_id,\n             null AS classification_name,\n             '''' AS chargetype,\n             true AS reversal_indicator,\n             ''Interest'' AS Allocation_Type,\n             CASE WHEN t.interest_portion_derived is null THEN 0 ELSE t.interest_portion_derived END AS amount,\n             CASE\n                 WHEN e.status in (''ACTIVE'', ''ACTIVE_INTERMEDIATE'') AND e.settlement_date < ''${endDate}''\n THEN e.owner_id END AS asset_owner_id,\n             CASE\n                 WHEN t.transaction_type_enum = 27 OR (m.charged_off_on_date <= t.transaction_date)\n THEN m.charge_off_reason_cv_id END AS charge_off_reason_id,\n             null::bigint AS from_asset_owner_id,\n             (SELECT STRING_AGG(DISTINCT mlo.external_id, '', '' ORDER BY mlo.external_id) FROM m_loan_originator_mapping mlom JOIN m_loan_originator mlo ON mlo.id = mlom.originator_id WHERE mlom.loan_id = t.loan_id) AS originator_external_ids\n      FROM m_loan_transaction t\n               JOIN m_loan m ON m.id = t.loan_id\n               JOIN m_product_loan l ON l.id = m.product_id\n               LEFT JOIN m_external_asset_owner_transfer e\n     ON e.loan_id = t.loan_id AND e.settlement_date < ''${endDate}'' AND\n        e.effective_date_to >= ''${endDate}''\n      WHERE t.reversed_on_date = ''${endDate}''\n        AND t.transaction_type_enum = 10\n        AND t.is_reversed = true\n        AND (t.office_id = ${officeId})\n      UNION ALL\n      SELECT ''${endDate}'' AS transactiondate,\n             t.id,\n             t.name AS product,\n             t.transaction_type_enum AS transaction_type,\n             t.payment_type_id,\n             null AS classification_name,\n             '''' AS chargetype,\n             true AS reversal_indicator,\n             ''Fees'' AS Allocation_Type,\n             CASE WHEN t.fee_charges_portion_derived is null THEN 0 ELSE t.fee_charges_portion_derived END AS amount,\n             CASE\n                 WHEN t.status in (''ACTIVE'', ''ACTIVE_INTERMEDIATE'') AND t.settlement_date < ''${endDate}''\n THEN t.owner_id END AS asset_owner_id,\n             CASE\n                 WHEN t.transaction_type_enum = 27 OR (t.charged_off_on_date <= t.transaction_date)\n THEN t.charge_off_reason_cv_id END AS charge_off_reason_id,\n             null::bigint AS from_asset_owner_id,\n             t.originator_external_ids\n      FROM rlt_charge_adj AS t\n      UNION ALL\n      SELECT ''${endDate}'' AS transactiondate,\n             t.id,\n             t.name AS product,\n             t.transaction_type_enum AS transaction_type,\n             t.payment_type_id,\n             null AS classification_name,\n             '''' AS chargetype,\n             true AS reversal_indicator,\n             ''Penalty'' AS Allocation_Type,\n             CASE\n                 WHEN t.penalty_charges_portion_derived is null THEN 0\n                 ELSE t.penalty_charges_portion_derived END AS amount,\n             CASE\n                 WHEN t.status in (''ACTIVE'', ''ACTIVE_INTERMEDIATE'') AND t.settlement_date < ''${endDate}''\n THEN t.owner_id END AS asset_owner_id,\n             CASE\n                 WHEN t.transaction_type_enum = 27 OR (t.charged_off_on_date <= t.transaction_date)\n THEN t.charge_off_reason_cv_id END AS charge_off_reason_id,\n             null::bigint AS from_asset_owner_id,\n             t.originator_external_ids\n      FROM rlt_charge_adj AS t\n      UNION ALL\n      SELECT ''${endDate}'' AS transactiondate,\n             t.id,\n             t.name AS product,\n             t.transaction_type_enum AS transaction_type,\n             t.payment_type_id,\n             null AS classification_name,\n             '''' AS chargetype,\n             true AS reversal_indicator,\n             ''Interest'' AS Allocation_Type,\n             CASE WHEN t.interest_portion_derived is null THEN 0 ELSE t.interest_portion_derived END AS amount,\n             CASE\n                 WHEN t.status in (''ACTIVE'', ''ACTIVE_INTERMEDIATE'') AND t.settlement_date < ''${endDate}''\n THEN t.owner_id END AS asset_owner_id,\n             CASE\n                 WHEN t.transaction_type_enum = 27 OR (t.charged_off_on_date <= t.transaction_date)\n THEN t.charge_off_reason_cv_id END AS charge_off_reason_id,\n             null::bigint AS from_asset_owner_id,\n             t.originator_external_ids\n      FROM rlt_charge_adj AS t\n      UNION ALL\n      SELECT ''${endDate}'' AS transactiondate,\n             t.id,\n             t.name AS product,\n             t.transaction_type_enum AS transaction_type,\n             t.payment_type_id,\n             null AS classification_name,\n             '''' AS chargetype,\n             true AS reversal_indicator,\n             ''Principal'' AS Allocation_Type,\n             CASE WHEN t.principal_portion_derived is null THEN 0 ELSE t.principal_portion_derived END AS amount,\n             CASE\n                 WHEN t.status in (''ACTIVE'', ''ACTIVE_INTERMEDIATE'') AND t.settlement_date < ''${endDate}''\n THEN t.owner_id END AS asset_owner_id,\n             CASE\n                 WHEN t.transaction_type_enum = 27 OR (t.charged_off_on_date <= t.transaction_date)\n THEN t.charge_off_reason_cv_id END AS charge_off_reason_id,\n             null::bigint AS from_asset_owner_id,\n             t.originator_external_ids\n      FROM rlt_charge_adj AS t\n      UNION ALL\n      SELECT ''${endDate}'' AS transactiondate,\n             t.id,\n             t.name AS product,\n             t.transaction_type_enum AS transaction_type,\n             t.payment_type_id,\n             null AS classification_name,\n             '''' AS chargetype,\n             true AS reversal_indicator,\n             ''Unallocated Credit (UNC)'' AS Allocation_Type,\n             CASE WHEN t.overpayment_portion_derived is null THEN 0 ELSE t.overpayment_portion_derived END AS amount,\n             CASE\n                 WHEN t.status in (''ACTIVE'', ''ACTIVE_INTERMEDIATE'') AND t.settlement_date < ''${endDate}''\n THEN t.owner_id END AS asset_owner_id,\n             CASE\n                 WHEN t.transaction_type_enum = 27 OR (t.charged_off_on_date <= t.transaction_date)\n THEN t.charge_off_reason_cv_id END AS charge_off_reason_id,\n             null::bigint AS from_asset_owner_id,\n             t.originator_external_ids\n      FROM rlt_charge_adj AS t\n      UNION ALL\n      SELECT t.transactiondate,\n             t.id,\n             t.name AS product,\n             9999 AS transaction_type,\n             null AS payment_type_id,\n             null AS classification_name,\n             '''' AS chargetype,\n             false AS reversal_indicator,\n             ''Principal'' AS Allocation_type,\n             t.principal_outstanding_derived AS amount,\n             t.owner_id AS asset_owner_id,\n             CASE\n                 WHEN t.charged_off_on_date <= t.settlement_date\n THEN t.charge_off_reason_cv_id END AS charge_off_reason_id,\n             t.previous_owner_id AS from_asset_owner_id,\n             t.originator_external_ids\n      FROM active_external_asset_owner_transfers AS t\n      WHERE t.principal_outstanding_derived > 0\n      UNION ALL\n      SELECT t.transactiondate,\n             t.id,\n             t.name AS product,\n             9999 AS transaction_type,\n             null AS payment_type_id,\n             null AS classification_name,\n             '''' AS chargetype,\n             false AS reversal_indicator,\n             ''Interest'' AS Allocation_type,\n             t.interest_outstanding_derived AS amount,\n             t.owner_id AS asset_owner_id,\n             CASE\n                 WHEN t.charged_off_on_date <= t.settlement_date\n THEN t.charge_off_reason_cv_id END AS charge_off_reason_id,\n             t.previous_owner_id AS from_asset_owner_id,\n             t.originator_external_ids\n      FROM active_external_asset_owner_transfers AS t\n      WHERE t.interest_outstanding_derived > 0\n      UNION ALL\n      SELECT t.transactiondate,\n             t.id,\n             t.name AS product,\n             9999 AS transaction_type,\n             null AS payment_type_id,\n             null AS classification_name,\n             '''' AS chargetype,\n             false AS reversal_indicator,\n             ''Fees'' AS Allocation_type,\n             t.fee_charges_outstanding_derived AS amount,\n             t.owner_id AS asset_owner_id,\n             CASE\n                 WHEN t.charged_off_on_date <= t.settlement_date\n THEN t.charge_off_reason_cv_id END AS charge_off_reason_id,\n             t.previous_owner_id AS from_asset_owner_id,\n             t.originator_external_ids\n      FROM active_external_asset_owner_transfers AS t\n      WHERE t.fee_charges_outstanding_derived > 0\n      UNION ALL\n      SELECT t.transactiondate,\n             t.id,\n             t.name AS product,\n             9999 AS transaction_type,\n             null AS payment_type_id,\n             null AS classification_name,\n             '''' AS chargetype,\n             false AS reversal_indicator,\n             ''Penalty'' AS Allocation_type,\n             t.penalty_charges_outstanding_derived AS amount,\n             t.owner_id AS asset_owner_id,\n             CASE\n                 WHEN t.charged_off_on_date <= t.settlement_date\n THEN t.charge_off_reason_cv_id END AS charge_off_reason_id,\n             t.previous_owner_id AS from_asset_owner_id,\n             t.originator_external_ids\n      FROM active_external_asset_owner_transfers AS t\n      WHERE t.penalty_charges_outstanding_derived > 0\n      UNION ALL\n      SELECT t.transactiondate,\n             t.id,\n             t.name AS product,\n             9999 AS transaction_type,\n             null AS payment_type_id,\n             null AS classification_name,\n             '''' AS chargetype,\n             false AS reversal_indicator,\n             ''Unallocated Credit (UNC)'' AS Allocation_type,\n             t.total_overpaid_derived AS amount,\n             t.owner_id AS asset_owner_id,\n             CASE\n                 WHEN t.charged_off_on_date <= t.settlement_date\n THEN t.charge_off_reason_cv_id END AS charge_off_reason_id,\n             t.previous_owner_id AS from_asset_owner_id,\n             t.originator_external_ids\n      FROM active_external_asset_owner_transfers AS t\n      WHERE t.total_overpaid_derived > 0\n      UNION ALL\n      SELECT ''${endDate}'' AS transactiondate,\n             t.id,\n             t.name AS product,\n             99999 AS transaction_type,\n             null AS payment_type_id,\n             null AS classification_name,\n             '''' AS chargetype,\n             false AS reversal_indicator,\n             ''Principal'' AS Allocation_type,\n             t.principal_outstanding_derived AS amount,\n             null AS asset_owner_id,\n             CASE\n                 WHEN t.charged_off_on_date <= t.settlement_date\n THEN t.charge_off_reason_cv_id END AS charge_off_reason_id,\n             t.owner_id AS from_asset_owner_id,\n             t.originator_external_ids\n      FROM buyback_external_asset_owner_transfers AS t\n      WHERE t.principal_outstanding_derived > 0\n      UNION ALL\n      SELECT ''${endDate}'' AS transactiondate,\n             t.id,\n             t.name AS product,\n             99999 AS transaction_type,\n             null AS payment_type_id,\n             null AS classification_name,\n             '''' AS chargetype,\n             false AS reversal_indicator,\n             ''Interest'' AS Allocation_type,\n             t.interest_outstanding_derived AS amount,\n             null AS asset_owner_id,\n             CASE\n                 WHEN t.charged_off_on_date <= t.settlement_date\n THEN t.charge_off_reason_cv_id END AS charge_off_reason_id,\n             t.owner_id AS from_asset_owner_id,\n             t.originator_external_ids\n      FROM buyback_external_asset_owner_transfers AS t\n      WHERE t.interest_outstanding_derived > 0\n      UNION ALL\n      SELECT ''${endDate}'' AS transactiondate,\n             t.id,\n             t.name AS product,\n             99999 AS transaction_type,\n             null AS payment_type_id,\n             null AS classification_name,\n             '''' AS chargetype,\n             false AS reversal_indicator,\n             ''Fees'' AS Allocation_type,\n             t.fee_charges_outstanding_derived AS amount,\n             null AS asset_owner_id,\n             CASE\n                 WHEN t.charged_off_on_date <= t.settlement_date\n THEN t.charge_off_reason_cv_id END AS charge_off_reason_id,\n             t.owner_id AS from_asset_owner_id,\n             t.originator_external_ids\n      FROM buyback_external_asset_owner_transfers AS t\n      WHERE t.fee_charges_outstanding_derived > 0\n      UNION ALL\n      SELECT ''${endDate}'' AS transactiondate,\n             t.id,\n             t.name AS product,\n             99999 AS transaction_type,\n             null AS payment_type_id,\n             null AS classification_name,\n             '''' AS chargetype,\n             false AS reversal_indicator,\n             ''Penalty'' AS Allocation_type,\n             t.penalty_charges_outstanding_derived AS amount,\n             null AS asset_owner_id,\n             CASE\n                 WHEN t.charged_off_on_date <= t.settlement_date\n THEN t.charge_off_reason_cv_id END AS charge_off_reason_id,\n             t.owner_id AS from_asset_owner_id,\n             t.originator_external_ids\n      FROM buyback_external_asset_owner_transfers AS t\n      WHERE t.penalty_charges_outstanding_derived > 0\n      UNION ALL\n      SELECT ''${endDate}'' AS transactiondate,\n             t.id,\n             t.name AS product,\n             99999 AS transaction_type,\n             null AS payment_type_id,\n             null AS classification_name,\n             '''' AS chargetype,\n             false AS reversal_indicator,\n             ''Unallocated Credit (UNC)'' AS Allocation_type,\n             t.total_overpaid_derived * -1 AS amount,\n             null AS asset_owner_id,\n             CASE\n                 WHEN t.charged_off_on_date <= t.settlement_date\n THEN t.charge_off_reason_cv_id END AS charge_off_reason_id,\n             t.owner_id AS from_asset_owner_id,\n             t.originator_external_ids\n      FROM buyback_external_asset_owner_transfers AS t\n      WHERE t.total_overpaid_derived > 0) a\nGROUP BY a.transactiondate, a.product, a.transaction_type, a.payment_type_id, a.classification_name, a.chargetype,\n         a.reversal_indicator, a.Allocation_Type, a.asset_owner_id, a.charge_off_reason_id, a.from_asset_owner_id,\n         a.originator_external_ids\nORDER BY 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11', '', 'f', 't', 'f');


-- Data for Name: m_report_mailing_job; Type: TABLE DATA; Schema: public; Owner: -



-- Data for Name: m_report_mailing_job_configuration; Type: TABLE DATA; Schema: public; Owner: -

INSERT INTO public.m_report_mailing_job_configuration (id, name, value) VALUES ('1', 'GMAIL_SMTP_SERVER', 'smtp.gmail.com');
INSERT INTO public.m_report_mailing_job_configuration (id, name, value) VALUES ('2', 'GMAIL_SMTP_PORT', '587');
INSERT INTO public.m_report_mailing_job_configuration (id, name, value) VALUES ('3', 'GMAIL_SMTP_USERNAME', '');
INSERT INTO public.m_report_mailing_job_configuration (id, name, value) VALUES ('4', 'GMAIL_SMTP_PASSWORD', '');


-- Data for Name: m_report_mailing_job_run_history; Type: TABLE DATA; Schema: public; Owner: -



-- Data for Name: m_role_permission; Type: TABLE DATA; Schema: public; Owner: -

INSERT INTO public.m_role_permission (role_id, permission_id) VALUES ('1', '1');
INSERT INTO public.m_role_permission (role_id, permission_id) VALUES ('1', '959');
INSERT INTO public.m_role_permission (role_id, permission_id) VALUES ('1', '960');


-- Data for Name: m_savings_account_charge; Type: TABLE DATA; Schema: public; Owner: -



-- Data for Name: m_savings_account_charge_paid_by; Type: TABLE DATA; Schema: public; Owner: -



-- Data for Name: m_savings_account_interest_rate_chart; Type: TABLE DATA; Schema: public; Owner: -



-- Data for Name: m_savings_account_interest_rate_slab; Type: TABLE DATA; Schema: public; Owner: -



-- Data for Name: m_tax_component; Type: TABLE DATA; Schema: public; Owner: -



-- Data for Name: m_savings_account_transaction_tax_details; Type: TABLE DATA; Schema: public; Owner: -



-- Data for Name: m_savings_interest_incentives; Type: TABLE DATA; Schema: public; Owner: -



-- Data for Name: m_savings_officer_assignment_history; Type: TABLE DATA; Schema: public; Owner: -



-- Data for Name: m_savings_product_charge; Type: TABLE DATA; Schema: public; Owner: -



-- Data for Name: m_share_account_charge; Type: TABLE DATA; Schema: public; Owner: -



-- Data for Name: m_share_account_charge_paid_by; Type: TABLE DATA; Schema: public; Owner: -



-- Data for Name: m_share_product_dividend_pay_out; Type: TABLE DATA; Schema: public; Owner: -



-- Data for Name: m_share_account_dividend_details; Type: TABLE DATA; Schema: public; Owner: -



-- Data for Name: m_share_product_charge; Type: TABLE DATA; Schema: public; Owner: -



-- Data for Name: m_share_product_market_price; Type: TABLE DATA; Schema: public; Owner: -



-- Data for Name: m_staff_assignment_history; Type: TABLE DATA; Schema: public; Owner: -



-- Data for Name: m_surveys; Type: TABLE DATA; Schema: public; Owner: -



-- Data for Name: m_survey_components; Type: TABLE DATA; Schema: public; Owner: -



-- Data for Name: m_survey_lookup_tables; Type: TABLE DATA; Schema: public; Owner: -



-- Data for Name: m_survey_questions; Type: TABLE DATA; Schema: public; Owner: -



-- Data for Name: m_survey_responses; Type: TABLE DATA; Schema: public; Owner: -



-- Data for Name: m_survey_scorecards; Type: TABLE DATA; Schema: public; Owner: -



-- Data for Name: m_tax_component_history; Type: TABLE DATA; Schema: public; Owner: -



-- Data for Name: m_tax_group_mappings; Type: TABLE DATA; Schema: public; Owner: -



-- Data for Name: m_template_m_templatemappers; Type: TABLE DATA; Schema: public; Owner: -



-- Data for Name: m_templatemappers; Type: TABLE DATA; Schema: public; Owner: -



-- Data for Name: m_trial_balance; Type: TABLE DATA; Schema: public; Owner: -



-- Data for Name: m_working_days; Type: TABLE DATA; Schema: public; Owner: -

INSERT INTO public.m_working_days (id, recurrence, repayment_rescheduling_enum, extend_term_daily_repayments, extend_term_holiday_repayment) VALUES ('1', 'FREQ=WEEKLY;INTERVAL=1;BYDAY=MO,TU,WE,TH,FR,SA,SU', '2', 'f', 'f');


-- Data for Name: mix_taxonomy; Type: TABLE DATA; Schema: public; Owner: -

INSERT INTO public.mix_taxonomy (id, name, namespace_id, dimension, type, description, need_mapping) VALUES ('1', 'AdministrativeExpense', '1', NULL, '3', NULL, 't');
INSERT INTO public.mix_taxonomy (id, name, namespace_id, dimension, type, description, need_mapping) VALUES ('2', 'Assets', '3', NULL, '1', 'All outstanding principals due for all outstanding client loans. This includes current, delinquent, and renegotiated loans, but not loans that have been written off. It does not include interest receivable.', 't');
INSERT INTO public.mix_taxonomy (id, name, namespace_id, dimension, type, description, need_mapping) VALUES ('3', 'Assets', '3', 'MaturityDimension:LessThanOneYearMember', '1', 'Segmentation based on the life of an asset or liability.', 't');
INSERT INTO public.mix_taxonomy (id, name, namespace_id, dimension, type, description, need_mapping) VALUES ('4', 'Assets', '3', 'MaturityDimension:MoreThanOneYearMember', '1', 'Segmentation based on the life of an asset or liability.', 't');
INSERT INTO public.mix_taxonomy (id, name, namespace_id, dimension, type, description, need_mapping) VALUES ('5', 'CashAndCashEquivalents', '1', NULL, '1', NULL, 't');
INSERT INTO public.mix_taxonomy (id, name, namespace_id, dimension, type, description, need_mapping) VALUES ('6', 'Deposits', '3', NULL, '1', 'The total value of funds placed in an account with an MFI that are payable to a depositor. This item includes any current, checking, or savings accounts that are payable on demand. It also includes time deposits which have a fixed maturity date and compulsory deposits.', 't');
INSERT INTO public.mix_taxonomy (id, name, namespace_id, dimension, type, description, need_mapping) VALUES ('7', 'Deposits', '3', 'DepositProductsDimension:CompulsoryMember', '1', 'The value of deposits that an MFI''s clients are required to  maintain as a condition of an existing or future loan.', NULL);
INSERT INTO public.mix_taxonomy (id, name, namespace_id, dimension, type, description, need_mapping) VALUES ('8', 'Deposits', '3', 'DepositProductsDimension:VoluntaryMember', '1', 'The value of deposits that an MFI''s clients are not required to  maintain as a condition of an existing or future loan.', NULL);
INSERT INTO public.mix_taxonomy (id, name, namespace_id, dimension, type, description, need_mapping) VALUES ('9', 'Deposits', '3', 'LocationDimension:RuralMember', '1', 'Located in rural areas. Segmentation based on location.', NULL);
INSERT INTO public.mix_taxonomy (id, name, namespace_id, dimension, type, description, need_mapping) VALUES ('10', 'Deposits', '3', 'LocationDimension:UrbanMember', '1', 'Located in urban areas. Segmentation based on location.', NULL);
INSERT INTO public.mix_taxonomy (id, name, namespace_id, dimension, type, description, need_mapping) VALUES ('11', 'Deposits', '3', 'MaturityDimension:LessThanOneYearMember', '1', 'Segmentation based on the life of an asset or liability.', NULL);
INSERT INTO public.mix_taxonomy (id, name, namespace_id, dimension, type, description, need_mapping) VALUES ('12', 'Deposits', '3', 'MaturityDimension:MoreThanOneYearMember', '1', 'Segmentation based on the life of an asset or liability.', NULL);
INSERT INTO public.mix_taxonomy (id, name, namespace_id, dimension, type, description, need_mapping) VALUES ('13', 'EmployeeBenefitsExpense', '1', NULL, '3', NULL, NULL);
INSERT INTO public.mix_taxonomy (id, name, namespace_id, dimension, type, description, need_mapping) VALUES ('14', 'Equity', '1', NULL, '1', NULL, NULL);
INSERT INTO public.mix_taxonomy (id, name, namespace_id, dimension, type, description, need_mapping) VALUES ('15', 'Expense', '1', NULL, '3', NULL, NULL);
INSERT INTO public.mix_taxonomy (id, name, namespace_id, dimension, type, description, need_mapping) VALUES ('16', 'FinancialExpense', '3', NULL, '3', 'All costs All costs incurred in raising funds from third parties, fee expenses from non-financial services, net gains (losses) due to changes in fair value of financial liabilities, impairment losses net of reversals of financial assets other than loan portfolio and net gains (losses) from restatement of financial statements in terms of the measuring unit current at the end of the reporting period.', NULL);
INSERT INTO public.mix_taxonomy (id, name, namespace_id, dimension, type, description, need_mapping) VALUES ('17', 'FinancialRevenueOnLoans', '3', NULL, '2', 'Interest and non-interest income generated by the provision of credit services to the clients. Fees and commissions for late payment are also included.', NULL);
INSERT INTO public.mix_taxonomy (id, name, namespace_id, dimension, type, description, need_mapping) VALUES ('18', 'ImpairmentLossAllowanceGrossLoanPortfolio', '3', NULL, '2', 'An allowance for the risk of losses in the gross loan portfolio due to default .', NULL);
INSERT INTO public.mix_taxonomy (id, name, namespace_id, dimension, type, description, need_mapping) VALUES ('19', 'Liabilities', '1', NULL, '1', NULL, NULL);
INSERT INTO public.mix_taxonomy (id, name, namespace_id, dimension, type, description, need_mapping) VALUES ('20', 'Liabilities', '3', 'MaturityDimension:LessThanOneYearMember', '1', 'Segmentation based on the life of an asset or liability.', NULL);
INSERT INTO public.mix_taxonomy (id, name, namespace_id, dimension, type, description, need_mapping) VALUES ('21', 'Liabilities', '3', 'MaturityDimension:MoreThanOneYearMember', '1', 'Segmentation based on the life of an asset or liability.', NULL);
INSERT INTO public.mix_taxonomy (id, name, namespace_id, dimension, type, description, need_mapping) VALUES ('22', 'LoanPortfolioGross', '3', NULL, '2', 'All outstanding principals due for all outstanding client loans. This includes current, delinquent, and renegotiated loans, but not loans that have been written off. It does not include interest receivable.', NULL);
INSERT INTO public.mix_taxonomy (id, name, namespace_id, dimension, type, description, need_mapping) VALUES ('23', 'LoanPortfolioGross', '3', 'CreditProductsDimension:MicroenterpriseMember', '2', 'Loans that finance the production or trade of goods and  services for an individual''s microenterprise, whether or not the microenterprise is legally registered. Segmentation based on loan product.', NULL);
INSERT INTO public.mix_taxonomy (id, name, namespace_id, dimension, type, description, need_mapping) VALUES ('24', 'LoanPortfolioGross', '3', 'DelinquencyDimension:OneMonthOrMoreMember', '2', 'Segmentation based on the principal balance of all loans outstanding that have one or more installments of principal  past due or renegotiated. Segmentation based on the  principal balance of all loans outstanding that have one or  more installments of principal past due or renegotiated.', NULL);
INSERT INTO public.mix_taxonomy (id, name, namespace_id, dimension, type, description, need_mapping) VALUES ('25', 'LoanPortfolioGross', '3', 'DelinquencyDimension:ThreeMonthsOrMoreMember', '2', 'Segmentation based on the principal balance of all loans outstanding that have one or more installments of principal  past due or renegotiated.? Segmentation based on the  principal balance of all loans outstanding that have one or  more installments of principal past due or renegotiated.', NULL);
INSERT INTO public.mix_taxonomy (id, name, namespace_id, dimension, type, description, need_mapping) VALUES ('26', 'LoanPortfolioGross', '3', 'LocationDimension:RuralMember', '2', 'Located in rural areas. Segmentation based on geographic location.', NULL);
INSERT INTO public.mix_taxonomy (id, name, namespace_id, dimension, type, description, need_mapping) VALUES ('27', 'LoanPortfolioGross', '3', 'LocationDimension:UrbanMember', '2', 'Located in urbal areas. Segmentation based on geographic location.', NULL);
INSERT INTO public.mix_taxonomy (id, name, namespace_id, dimension, type, description, need_mapping) VALUES ('28', 'LoanPortfolioGross', '3', 'MaturityDimension:LessThanOneYearMember', '2', 'Segmentation based on the life of an asset or liability.', NULL);
INSERT INTO public.mix_taxonomy (id, name, namespace_id, dimension, type, description, need_mapping) VALUES ('29', 'LoanPortfolioGross', '3', 'MaturityDimension:MoreThanOneYearMember', '2', 'Segmentation based on the life of an asset or liability.', NULL);
INSERT INTO public.mix_taxonomy (id, name, namespace_id, dimension, type, description, need_mapping) VALUES ('30', 'NetLoanLoss', '3', '', '3', 'Referred to the value of delinquency loans written off net of any principal recovery.', NULL);
INSERT INTO public.mix_taxonomy (id, name, namespace_id, dimension, type, description, need_mapping) VALUES ('31', 'NetLoanLossProvisionExpense', '3', NULL, '3', 'Represent the net value of loan portfolio impairment loss considering any reversal on impairment loss and any recovery on loans written off recognized as a income during the accounting period.', NULL);
INSERT INTO public.mix_taxonomy (id, name, namespace_id, dimension, type, description, need_mapping) VALUES ('32', 'NetOperatingIncome', '3', NULL, '2', 'Total operating revenue less all expenses related to the MFI''s core financial service operation including total financial expense, impairment loss and operating expense. Donations are excluded.', NULL);
INSERT INTO public.mix_taxonomy (id, name, namespace_id, dimension, type, description, need_mapping) VALUES ('33', 'NetOperatingIncomeNetOfTaxExpense', '3', NULL, '3', 'Net operating income reported incorporating the effect of taxes. Taxes include all domestic and foreign taxes which are based on taxable profits, other taxes related to personnel, financial transactions or value-added taxes are not considered in calculation of this value.', NULL);
INSERT INTO public.mix_taxonomy (id, name, namespace_id, dimension, type, description, need_mapping) VALUES ('34', 'NumberOfActiveBorrowers', '3', NULL, '0', 'The number of individuals who currently have an outstanding loan balance with the MFI or are primarily responsible for repaying any portion of the gross loan portfolio. Individuals who have multiple loans with an MFI should be counted as a single borrower.', NULL);
INSERT INTO public.mix_taxonomy (id, name, namespace_id, dimension, type, description, need_mapping) VALUES ('35', 'NumberOfActiveBorrowers', '3', 'GenderDimension:FemaleMember', '0', 'The number of individuals who currently have an outstanding loan balance with the MFI or are primarily responsible for repaying any portion of the gross loan portfolio. Individuals who have multiple loans with an MFI should be counted as a single borrower.', NULL);
INSERT INTO public.mix_taxonomy (id, name, namespace_id, dimension, type, description, need_mapping) VALUES ('36', 'NumberOfBoardMembers', '3', 'GenderDimension:FemaleMember', '0', 'The number of members that comprise the board of directors at the end of the reporting period who are female.', NULL);
INSERT INTO public.mix_taxonomy (id, name, namespace_id, dimension, type, description, need_mapping) VALUES ('37', 'NumberOfDepositAccounts', '3', NULL, '0', 'The number of individuals who currently have funds on deposit with the MFI on a voluntary basis; i.e., they are not required to maintain the deposit account to access a loan. This number applies only to deposits held by an MFI, not to those deposits held in other institutions by the MFI''s clients. The number should be based on the number of individuals rather than the number of groups. A single deposit account may represent multiple depositors.', NULL);
INSERT INTO public.mix_taxonomy (id, name, namespace_id, dimension, type, description, need_mapping) VALUES ('38', 'NumberOfDepositors', '3', '', '0', 'The number of deposit accounts, both voluntary and compulsory, opened at the MFI whose balances the institution is liable to repay. The number should be based on the number of individual accounts rather than on the number of groups.', NULL);
INSERT INTO public.mix_taxonomy (id, name, namespace_id, dimension, type, description, need_mapping) VALUES ('39', 'NumberOfEmployees', '3', NULL, '0', 'The number of individuals who are actively employed by an entity. This number includes contract employees or advisors who dedicate a substantial portion of their time to the entity, even if they are not on the entity''s employees roster.', NULL);
INSERT INTO public.mix_taxonomy (id, name, namespace_id, dimension, type, description, need_mapping) VALUES ('40', 'NumberOfEmployees', '3', 'GenderDimension:FemaleMember', '0', 'The number of individuals who are actively employed by an entity. This number includes contract employees or advisors who dedicate a substantial portion of their time to the entity, even if they are not on the entity''s employees roster.', NULL);
INSERT INTO public.mix_taxonomy (id, name, namespace_id, dimension, type, description, need_mapping) VALUES ('41', 'NumberOfLoanOfficers', '3', NULL, '0', 'The number of employees whose main activity is to manage a portion of the gross loan portfolio. A loan officer is a staff member of record who is directly responsible for arranging and monitoring client loans.', NULL);
INSERT INTO public.mix_taxonomy (id, name, namespace_id, dimension, type, description, need_mapping) VALUES ('42', 'NumberOfLoanOfficers', '3', 'GenderDimension:FemaleMember', '0', 'The number of employees whose main activity is to manage a portion of the gross loan portfolio. A loan officer is a staff member of record who is directly responsible for arranging and monitoring client loans.', NULL);
INSERT INTO public.mix_taxonomy (id, name, namespace_id, dimension, type, description, need_mapping) VALUES ('43', 'NumberOfManagers', '3', 'GenderDimension:FemaleMember', '0', 'The number of members that comprise the management of the institution who are female.', NULL);
INSERT INTO public.mix_taxonomy (id, name, namespace_id, dimension, type, description, need_mapping) VALUES ('44', 'NumberOfOffices', '3', NULL, '0', 'The number of staffed points of service and administrative sites used to deliver or support the delivery of financial services to microfinance clients.', NULL);
INSERT INTO public.mix_taxonomy (id, name, namespace_id, dimension, type, description, need_mapping) VALUES ('45', 'NumberOfOutstandingLoans', '3', NULL, '0', 'The number of loans in the gross loan portfolio. For MFIs using a group lending methodology, the number of loans should refer to the number of individuals receiving loans as part of a group or as part of a group loan.', NULL);
INSERT INTO public.mix_taxonomy (id, name, namespace_id, dimension, type, description, need_mapping) VALUES ('46', 'OperatingExpense', '3', NULL, '3', 'Includes expenses not related to financial and credit loss impairment, such as personnel expenses, depreciation, amortization and administrative expenses.', NULL);
INSERT INTO public.mix_taxonomy (id, name, namespace_id, dimension, type, description, need_mapping) VALUES ('47', 'OperatingIncome', '3', NULL, '2', 'Includes all financial income and other operating revenue which is generated from non-financial services. Operating income also includes net gains (losses) from holding financial assets (changes on their values during the period and foreign exchange differences). Donations or any revenue not related with an MFI''s core business of making loans and providing financial services are not considered under this category.', NULL);
INSERT INTO public.mix_taxonomy (id, name, namespace_id, dimension, type, description, need_mapping) VALUES ('48', 'WriteOffsOnGrossLoanPortfolio', '3', NULL, '2', 'The value of loans that have been recognized as uncollectible for accounting purposes. A write-off is an accounting procedure that removes the outstanding balance of the loan from the gross loan portfolio and impairment loss allowance. Thus, the write-off does not affect the net loan portfolio, total assets, or any equity account. If the impairment loss allowance is insufficient to cover the amount written off, the excess amount will result in an additional impairment loss on loans recognised in profit or loss of the period.', NULL);


-- Data for Name: mix_taxonomy_mapping; Type: TABLE DATA; Schema: public; Owner: -

INSERT INTO public.mix_taxonomy_mapping (id, identifier, config, last_update_date, currency) VALUES ('1', 'default', NULL, NULL, '');


-- Data for Name: mix_xbrl_namespace; Type: TABLE DATA; Schema: public; Owner: -

INSERT INTO public.mix_xbrl_namespace (id, prefix, url) VALUES ('1', 'ifrs', 'http://xbrl.iasb.org/taxonomy/2009-04-01/ifrs');
INSERT INTO public.mix_xbrl_namespace (id, prefix, url) VALUES ('2', 'iso4217', 'http://www.xbrl.org/2003/iso4217');
INSERT INTO public.mix_xbrl_namespace (id, prefix, url) VALUES ('3', 'mix', 'http://www.themix.org/INT/fr/ifrs/basi/YYYY-MM-DD/mx-cor');
INSERT INTO public.mix_xbrl_namespace (id, prefix, url) VALUES ('4', 'xbrldi', 'http://xbrl.org/2006/xbrldi');
INSERT INTO public.mix_xbrl_namespace (id, prefix, url) VALUES ('5', 'xbrli', 'http://www.xbrl.org/2003/instance');
INSERT INTO public.mix_xbrl_namespace (id, prefix, url) VALUES ('6', 'link', 'http://www.xbrl.org/2003/linkbase');
INSERT INTO public.mix_xbrl_namespace (id, prefix, url) VALUES ('7', 'dc-all', 'http://www.themix.org/INT/fr/ifrs/basi/2010-08-31/dc-all');


-- Data for Name: notification_generator; Type: TABLE DATA; Schema: public; Owner: -



-- Data for Name: notification_mapper; Type: TABLE DATA; Schema: public; Owner: -



-- Data for Name: oauth_access_token; Type: TABLE DATA; Schema: public; Owner: -



-- Data for Name: oauth_client_details; Type: TABLE DATA; Schema: public; Owner: -

INSERT INTO public.oauth_client_details (client_id, resource_ids, client_secret, scope, authorized_grant_types, web_server_redirect_uri, authorities, access_token_validity, refresh_token_validity, additional_information, autoapprove) VALUES ('community-app', NULL, '{SHA-256}a665a45920422f9d417e4867efdc4fb8a04a1f3fff1fa07e998e86f7f7a27ae3', 'all', 'password,refresh_token', NULL, NULL, NULL, NULL, NULL, NULL);


-- Data for Name: oauth_refresh_token; Type: TABLE DATA; Schema: public; Owner: -



-- Data for Name: ppi_likelihoods; Type: TABLE DATA; Schema: public; Owner: -



-- Data for Name: ppi_likelihoods_ppi; Type: TABLE DATA; Schema: public; Owner: -



-- Data for Name: ppi_scores; Type: TABLE DATA; Schema: public; Owner: -

INSERT INTO public.ppi_scores (id, score_from, score_to) VALUES ('1', '0', '4');
INSERT INTO public.ppi_scores (id, score_from, score_to) VALUES ('2', '5', '9');
INSERT INTO public.ppi_scores (id, score_from, score_to) VALUES ('3', '10', '14');
INSERT INTO public.ppi_scores (id, score_from, score_to) VALUES ('4', '15', '19');
INSERT INTO public.ppi_scores (id, score_from, score_to) VALUES ('5', '20', '24');
INSERT INTO public.ppi_scores (id, score_from, score_to) VALUES ('6', '25', '29');
INSERT INTO public.ppi_scores (id, score_from, score_to) VALUES ('7', '30', '34');
INSERT INTO public.ppi_scores (id, score_from, score_to) VALUES ('8', '35', '39');
INSERT INTO public.ppi_scores (id, score_from, score_to) VALUES ('9', '40', '44');
INSERT INTO public.ppi_scores (id, score_from, score_to) VALUES ('10', '45', '49');
INSERT INTO public.ppi_scores (id, score_from, score_to) VALUES ('11', '50', '54');
INSERT INTO public.ppi_scores (id, score_from, score_to) VALUES ('12', '55', '59');
INSERT INTO public.ppi_scores (id, score_from, score_to) VALUES ('13', '60', '64');
INSERT INTO public.ppi_scores (id, score_from, score_to) VALUES ('14', '65', '69');
INSERT INTO public.ppi_scores (id, score_from, score_to) VALUES ('15', '70', '74');
INSERT INTO public.ppi_scores (id, score_from, score_to) VALUES ('16', '75', '79');
INSERT INTO public.ppi_scores (id, score_from, score_to) VALUES ('17', '80', '84');
INSERT INTO public.ppi_scores (id, score_from, score_to) VALUES ('18', '85', '89');
INSERT INTO public.ppi_scores (id, score_from, score_to) VALUES ('19', '90', '94');
INSERT INTO public.ppi_scores (id, score_from, score_to) VALUES ('20', '95', '100');


-- Data for Name: r_enum_value; Type: TABLE DATA; Schema: public; Owner: -

INSERT INTO public.r_enum_value (enum_name, enum_id, enum_message_property, enum_value, enum_type) VALUES ('account_type_type_enum', '0', 'INVALID', 'INVALID', 'f');
INSERT INTO public.r_enum_value (enum_name, enum_id, enum_message_property, enum_value, enum_type) VALUES ('account_type_type_enum', '1', 'INDIVIDUAL', 'INDIVIDUAL', 'f');
INSERT INTO public.r_enum_value (enum_name, enum_id, enum_message_property, enum_value, enum_type) VALUES ('account_type_type_enum', '2', 'GROUP', 'GROUP', 'f');
INSERT INTO public.r_enum_value (enum_name, enum_id, enum_message_property, enum_value, enum_type) VALUES ('account_type_type_enum', '3', 'JLG', 'JLG', 'f');
INSERT INTO public.r_enum_value (enum_name, enum_id, enum_message_property, enum_value, enum_type) VALUES ('accrual_accounts_for_loan_type_enum', '1', 'FUND_SOURCE', 'FUND_SOURCE', 'f');
INSERT INTO public.r_enum_value (enum_name, enum_id, enum_message_property, enum_value, enum_type) VALUES ('accrual_accounts_for_loan_type_enum', '2', 'LOAN_PORTFOLIO', 'LOAN_PORTFOLIO', 'f');
INSERT INTO public.r_enum_value (enum_name, enum_id, enum_message_property, enum_value, enum_type) VALUES ('accrual_accounts_for_loan_type_enum', '3', 'INTEREST_ON_LOANS', 'INTEREST_ON_LOANS', 'f');
INSERT INTO public.r_enum_value (enum_name, enum_id, enum_message_property, enum_value, enum_type) VALUES ('accrual_accounts_for_loan_type_enum', '4', 'INCOME_FROM_FEES', 'INCOME_FROM_FEES', 'f');
INSERT INTO public.r_enum_value (enum_name, enum_id, enum_message_property, enum_value, enum_type) VALUES ('accrual_accounts_for_loan_type_enum', '5', 'INCOME_FROM_PENALTIES', 'INCOME_FROM_PENALTIES', 'f');
INSERT INTO public.r_enum_value (enum_name, enum_id, enum_message_property, enum_value, enum_type) VALUES ('accrual_accounts_for_loan_type_enum', '6', 'LOSSES_WRITTEN_OFF', 'LOSSES_WRITTEN_OFF', 'f');
INSERT INTO public.r_enum_value (enum_name, enum_id, enum_message_property, enum_value, enum_type) VALUES ('accrual_accounts_for_loan_type_enum', '7', 'INTEREST_RECEIVABLE', 'INTEREST_RECEIVABLE', 'f');
INSERT INTO public.r_enum_value (enum_name, enum_id, enum_message_property, enum_value, enum_type) VALUES ('accrual_accounts_for_loan_type_enum', '8', 'FEES_RECEIVABLE', 'FEES_RECEIVABLE', 'f');
INSERT INTO public.r_enum_value (enum_name, enum_id, enum_message_property, enum_value, enum_type) VALUES ('accrual_accounts_for_loan_type_enum', '9', 'PENALTIES_RECEIVABLE', 'PENALTIES_RECEIVABLE', 'f');
INSERT INTO public.r_enum_value (enum_name, enum_id, enum_message_property, enum_value, enum_type) VALUES ('accrual_accounts_for_loan_type_enum', '10', 'TRANSFERS_SUSPENSE', 'TRANSFERS_SUSPENSE', 'f');
INSERT INTO public.r_enum_value (enum_name, enum_id, enum_message_property, enum_value, enum_type) VALUES ('accrual_accounts_for_loan_type_enum', '11', 'OVERPAYMENT', 'OVERPAYMENT', 'f');
INSERT INTO public.r_enum_value (enum_name, enum_id, enum_message_property, enum_value, enum_type) VALUES ('accrual_accounts_for_loan_type_enum', '12', 'INCOME_FROM_RECOVERY', 'INCOME_FROM_RECOVERY', 'f');
INSERT INTO public.r_enum_value (enum_name, enum_id, enum_message_property, enum_value, enum_type) VALUES ('amortization_method_enum', '0', 'Equal principle payments', 'Equal principle payments', 'f');
INSERT INTO public.r_enum_value (enum_name, enum_id, enum_message_property, enum_value, enum_type) VALUES ('amortization_method_enum', '1', 'Equal installments', 'Equal installments', 'f');
INSERT INTO public.r_enum_value (enum_name, enum_id, enum_message_property, enum_value, enum_type) VALUES ('calendar_type_enum', '0', 'INVALID', 'INVALID', 'f');
INSERT INTO public.r_enum_value (enum_name, enum_id, enum_message_property, enum_value, enum_type) VALUES ('calendar_type_enum', '1', 'CLIENTS', 'CLIENTS', 'f');
INSERT INTO public.r_enum_value (enum_name, enum_id, enum_message_property, enum_value, enum_type) VALUES ('calendar_type_enum', '2', 'GROUPS', 'GROUPS', 'f');
INSERT INTO public.r_enum_value (enum_name, enum_id, enum_message_property, enum_value, enum_type) VALUES ('calendar_type_enum', '3', 'LOANS', 'LOANS', 'f');
INSERT INTO public.r_enum_value (enum_name, enum_id, enum_message_property, enum_value, enum_type) VALUES ('calendar_type_enum', '4', 'CENTERS', 'CENTERS', 'f');
INSERT INTO public.r_enum_value (enum_name, enum_id, enum_message_property, enum_value, enum_type) VALUES ('calendar_type_enum', '5', 'SAVINGS', 'SAVINGS', 'f');
INSERT INTO public.r_enum_value (enum_name, enum_id, enum_message_property, enum_value, enum_type) VALUES ('calendar_type_enum', '6', 'LOAN_RECALCULATION_REST_DETAIL', 'LOAN_RECALCULATION_REST_DETAIL', 'f');
INSERT INTO public.r_enum_value (enum_name, enum_id, enum_message_property, enum_value, enum_type) VALUES ('calendar_type_enum', '7', 'LOAN_RECALCULATION_COMPOUNDING_DETAIL', 'LOAN_RECALCULATION_COMPOUNDING_DETAIL', 'f');
INSERT INTO public.r_enum_value (enum_name, enum_id, enum_message_property, enum_value, enum_type) VALUES ('cash_accounts_for_loan_type_enum', '1', 'FUND_SOURCE', 'FUND_SOURCE', 'f');
INSERT INTO public.r_enum_value (enum_name, enum_id, enum_message_property, enum_value, enum_type) VALUES ('cash_accounts_for_loan_type_enum', '2', 'LOAN_PORTFOLIO', 'LOAN_PORTFOLIO', 'f');
INSERT INTO public.r_enum_value (enum_name, enum_id, enum_message_property, enum_value, enum_type) VALUES ('cash_accounts_for_loan_type_enum', '3', 'INTEREST_ON_LOANS', 'INTEREST_ON_LOANS', 'f');
INSERT INTO public.r_enum_value (enum_name, enum_id, enum_message_property, enum_value, enum_type) VALUES ('cash_accounts_for_loan_type_enum', '4', 'INCOME_FROM_FEES', 'INCOME_FROM_FEES', 'f');
INSERT INTO public.r_enum_value (enum_name, enum_id, enum_message_property, enum_value, enum_type) VALUES ('cash_accounts_for_loan_type_enum', '5', 'INCOME_FROM_PENALTIES', 'INCOME_FROM_PENALTIES', 'f');
INSERT INTO public.r_enum_value (enum_name, enum_id, enum_message_property, enum_value, enum_type) VALUES ('cash_accounts_for_loan_type_enum', '6', 'LOSSES_WRITTEN_OFF', 'LOSSES_WRITTEN_OFF', 'f');
INSERT INTO public.r_enum_value (enum_name, enum_id, enum_message_property, enum_value, enum_type) VALUES ('cash_accounts_for_loan_type_enum', '10', 'TRANSFERS_SUSPENSE', 'TRANSFERS_SUSPENSE', 'f');
INSERT INTO public.r_enum_value (enum_name, enum_id, enum_message_property, enum_value, enum_type) VALUES ('cash_accounts_for_loan_type_enum', '11', 'OVERPAYMENT', 'OVERPAYMENT', 'f');
INSERT INTO public.r_enum_value (enum_name, enum_id, enum_message_property, enum_value, enum_type) VALUES ('cash_accounts_for_loan_type_enum', '12', 'INCOME_FROM_RECOVERY', 'INCOME_FROM_RECOVERY', 'f');
INSERT INTO public.r_enum_value (enum_name, enum_id, enum_message_property, enum_value, enum_type) VALUES ('cash_accounts_for_savings_type_enum', '1', 'SAVINGS_REFERENCE', 'SAVINGS_REFERENCE', 'f');
INSERT INTO public.r_enum_value (enum_name, enum_id, enum_message_property, enum_value, enum_type) VALUES ('cash_accounts_for_savings_type_enum', '2', 'SAVINGS_CONTROL', 'SAVINGS_CONTROL', 'f');
INSERT INTO public.r_enum_value (enum_name, enum_id, enum_message_property, enum_value, enum_type) VALUES ('cash_accounts_for_savings_type_enum', '3', 'INTEREST_ON_SAVINGS', 'INTEREST_ON_SAVINGS', 'f');
INSERT INTO public.r_enum_value (enum_name, enum_id, enum_message_property, enum_value, enum_type) VALUES ('cash_accounts_for_savings_type_enum', '4', 'INCOME_FROM_FEES', 'INCOME_FROM_FEES', 'f');
INSERT INTO public.r_enum_value (enum_name, enum_id, enum_message_property, enum_value, enum_type) VALUES ('cash_accounts_for_savings_type_enum', '5', 'INCOME_FROM_PENALTIES', 'INCOME_FROM_PENALTIES', 'f');
INSERT INTO public.r_enum_value (enum_name, enum_id, enum_message_property, enum_value, enum_type) VALUES ('cash_accounts_for_savings_type_enum', '10', 'TRANSFERS_SUSPENSE', 'TRANSFERS_SUSPENSE', 'f');
INSERT INTO public.r_enum_value (enum_name, enum_id, enum_message_property, enum_value, enum_type) VALUES ('cash_accounts_for_savings_type_enum', '11', 'OVERDRAFT_PORTFOLIO_CONTROL', 'OVERDRAFT_PORTFOLIO_CONTROL', 'f');
INSERT INTO public.r_enum_value (enum_name, enum_id, enum_message_property, enum_value, enum_type) VALUES ('cash_accounts_for_savings_type_enum', '12', 'INCOME_FROM_INTEREST', 'INCOME_FROM_INTEREST', 'f');
INSERT INTO public.r_enum_value (enum_name, enum_id, enum_message_property, enum_value, enum_type) VALUES ('cash_account_for_shares_type_enum', '1', 'SHARES_REFERENCE', 'SHARES_REFERENCE', 'f');
INSERT INTO public.r_enum_value (enum_name, enum_id, enum_message_property, enum_value, enum_type) VALUES ('cash_account_for_shares_type_enum', '2', 'SHARES_SUSPENSE', 'SHARES_SUSPENSE', 'f');
INSERT INTO public.r_enum_value (enum_name, enum_id, enum_message_property, enum_value, enum_type) VALUES ('cash_account_for_shares_type_enum', '3', 'INCOME_FROM_FEES', 'INCOME_FROM_FEES', 'f');
INSERT INTO public.r_enum_value (enum_name, enum_id, enum_message_property, enum_value, enum_type) VALUES ('cash_account_for_shares_type_enum', '4', 'SHARES_EQUITY', 'SHARES_EQUITY', 'f');
INSERT INTO public.r_enum_value (enum_name, enum_id, enum_message_property, enum_value, enum_type) VALUES ('client_transaction_type_enum', '1', 'PAY_CHARGE', 'PAY_CHARGE', 'f');
INSERT INTO public.r_enum_value (enum_name, enum_id, enum_message_property, enum_value, enum_type) VALUES ('client_transaction_type_enum', '2', 'WAIVE_CHARGE', 'WAIVE_CHARGE', 'f');
INSERT INTO public.r_enum_value (enum_name, enum_id, enum_message_property, enum_value, enum_type) VALUES ('entity_account_type_enum', '1', 'CLIENT', 'CLIENT', 'f');
INSERT INTO public.r_enum_value (enum_name, enum_id, enum_message_property, enum_value, enum_type) VALUES ('entity_account_type_enum', '2', 'LOAN', 'LOAN', 'f');
INSERT INTO public.r_enum_value (enum_name, enum_id, enum_message_property, enum_value, enum_type) VALUES ('entity_account_type_enum', '3', 'SAVINGS', 'SAVINGS', 'f');
INSERT INTO public.r_enum_value (enum_name, enum_id, enum_message_property, enum_value, enum_type) VALUES ('entity_account_type_enum', '4', 'CENTER', 'CENTER', 'f');
INSERT INTO public.r_enum_value (enum_name, enum_id, enum_message_property, enum_value, enum_type) VALUES ('entity_account_type_enum', '5', 'GROUP', 'GROUP', 'f');
INSERT INTO public.r_enum_value (enum_name, enum_id, enum_message_property, enum_value, enum_type) VALUES ('entity_account_type_enum', '6', 'SHARES', 'SHARES', 'f');
INSERT INTO public.r_enum_value (enum_name, enum_id, enum_message_property, enum_value, enum_type) VALUES ('financial_activity_type_enum', '100', 'ASSET_TRANSFER', 'ASSET_TRANSFER', 'f');
INSERT INTO public.r_enum_value (enum_name, enum_id, enum_message_property, enum_value, enum_type) VALUES ('financial_activity_type_enum', '101', 'CASH_AT_MAINVAULT', 'CASH_AT_MAINVAULT', 'f');
INSERT INTO public.r_enum_value (enum_name, enum_id, enum_message_property, enum_value, enum_type) VALUES ('financial_activity_type_enum', '102', 'CASH_AT_TELLER', 'CASH_AT_TELLER', 'f');
INSERT INTO public.r_enum_value (enum_name, enum_id, enum_message_property, enum_value, enum_type) VALUES ('financial_activity_type_enum', '103', 'ASSET_FUND_SOURCE', 'ASSET_FUND_SOURCE', 'f');
INSERT INTO public.r_enum_value (enum_name, enum_id, enum_message_property, enum_value, enum_type) VALUES ('financial_activity_type_enum', '200', 'LIABILITY_TRANSFER', 'LIABILITY_TRANSFER', 'f');
INSERT INTO public.r_enum_value (enum_name, enum_id, enum_message_property, enum_value, enum_type) VALUES ('financial_activity_type_enum', '201', 'PAYABLE_DIVIDENDS', 'PAYABLE_DIVIDENDS', 'f');
INSERT INTO public.r_enum_value (enum_name, enum_id, enum_message_property, enum_value, enum_type) VALUES ('financial_activity_type_enum', '300', 'OPENING_BALANCES_TRANSFER_CONTRA', 'OPENING_BALANCES_TRANSFER_CONTRA', 'f');
INSERT INTO public.r_enum_value (enum_name, enum_id, enum_message_property, enum_value, enum_type) VALUES ('glaccount_type_enum', '1', 'ASSET', 'ASSET', 'f');
INSERT INTO public.r_enum_value (enum_name, enum_id, enum_message_property, enum_value, enum_type) VALUES ('glaccount_type_enum', '2', 'LIABILITY', 'LIABILITY', 'f');
INSERT INTO public.r_enum_value (enum_name, enum_id, enum_message_property, enum_value, enum_type) VALUES ('glaccount_type_enum', '3', 'EQUITY', 'EQUITY', 'f');
INSERT INTO public.r_enum_value (enum_name, enum_id, enum_message_property, enum_value, enum_type) VALUES ('glaccount_type_enum', '4', 'INCOME', 'INCOME', 'f');
INSERT INTO public.r_enum_value (enum_name, enum_id, enum_message_property, enum_value, enum_type) VALUES ('glaccount_type_enum', '5', 'EXPENSE', 'EXPENSE', 'f');
INSERT INTO public.r_enum_value (enum_name, enum_id, enum_message_property, enum_value, enum_type) VALUES ('interest_calculated_in_period_enum', '0', 'Daily', 'Daily', 'f');
INSERT INTO public.r_enum_value (enum_name, enum_id, enum_message_property, enum_value, enum_type) VALUES ('interest_calculated_in_period_enum', '1', 'Same as repayment period', 'Same as repayment period', 'f');
INSERT INTO public.r_enum_value (enum_name, enum_id, enum_message_property, enum_value, enum_type) VALUES ('interest_method_enum', '0', 'Declining Balance', 'Declining Balance', 'f');
INSERT INTO public.r_enum_value (enum_name, enum_id, enum_message_property, enum_value, enum_type) VALUES ('interest_method_enum', '1', 'Flat', 'Flat', 'f');
INSERT INTO public.r_enum_value (enum_name, enum_id, enum_message_property, enum_value, enum_type) VALUES ('interest_period_frequency_enum', '2', 'Per month', 'Per month', 'f');
INSERT INTO public.r_enum_value (enum_name, enum_id, enum_message_property, enum_value, enum_type) VALUES ('interest_period_frequency_enum', '3', 'Per year', 'Per year', 'f');
INSERT INTO public.r_enum_value (enum_name, enum_id, enum_message_property, enum_value, enum_type) VALUES ('journal_entry_type_type_enum', '1', 'CREDIT', 'CREDIT', 'f');
INSERT INTO public.r_enum_value (enum_name, enum_id, enum_message_property, enum_value, enum_type) VALUES ('journal_entry_type_type_enum', '2', 'DEBIT', 'DEBIT', 'f');
INSERT INTO public.r_enum_value (enum_name, enum_id, enum_message_property, enum_value, enum_type) VALUES ('loan_status_id', '0', 'Invalid', 'Invalid', 'f');
INSERT INTO public.r_enum_value (enum_name, enum_id, enum_message_property, enum_value, enum_type) VALUES ('loan_status_id', '100', 'Submitted and awaiting approval', 'Submitted and awaiting approval', 'f');
INSERT INTO public.r_enum_value (enum_name, enum_id, enum_message_property, enum_value, enum_type) VALUES ('loan_status_id', '200', 'Approved', 'Approved', 'f');
INSERT INTO public.r_enum_value (enum_name, enum_id, enum_message_property, enum_value, enum_type) VALUES ('loan_status_id', '300', 'Active', 'Active', 'f');
INSERT INTO public.r_enum_value (enum_name, enum_id, enum_message_property, enum_value, enum_type) VALUES ('loan_status_id', '400', 'Withdrawn by client', 'Withdrawn by client', 'f');
INSERT INTO public.r_enum_value (enum_name, enum_id, enum_message_property, enum_value, enum_type) VALUES ('loan_status_id', '500', 'Rejected', 'Rejected', 'f');
INSERT INTO public.r_enum_value (enum_name, enum_id, enum_message_property, enum_value, enum_type) VALUES ('loan_status_id', '600', 'Closed', 'Closed', 'f');
INSERT INTO public.r_enum_value (enum_name, enum_id, enum_message_property, enum_value, enum_type) VALUES ('loan_status_id', '601', 'Written-Off', 'Written-Off', 'f');
INSERT INTO public.r_enum_value (enum_name, enum_id, enum_message_property, enum_value, enum_type) VALUES ('loan_status_id', '602', 'Rescheduled', 'Rescheduled', 'f');
INSERT INTO public.r_enum_value (enum_name, enum_id, enum_message_property, enum_value, enum_type) VALUES ('loan_status_id', '700', 'Overpaid', 'Overpaid', 'f');
INSERT INTO public.r_enum_value (enum_name, enum_id, enum_message_property, enum_value, enum_type) VALUES ('loan_transaction_strategy_id', '1', 'mifos-standard-strategy', 'Mifos style', 'f');
INSERT INTO public.r_enum_value (enum_name, enum_id, enum_message_property, enum_value, enum_type) VALUES ('loan_transaction_strategy_id', '2', 'heavensfamily-strategy', 'Heavensfamily', 'f');
INSERT INTO public.r_enum_value (enum_name, enum_id, enum_message_property, enum_value, enum_type) VALUES ('loan_transaction_strategy_id', '3', 'creocore-strategy', 'Creocore', 'f');
INSERT INTO public.r_enum_value (enum_name, enum_id, enum_message_property, enum_value, enum_type) VALUES ('loan_transaction_strategy_id', '4', 'rbi-india-strategy', 'RBI (India)', 'f');
INSERT INTO public.r_enum_value (enum_name, enum_id, enum_message_property, enum_value, enum_type) VALUES ('loan_transaction_type_enum', '0', 'INVALID', 'INVALID', 'f');
INSERT INTO public.r_enum_value (enum_name, enum_id, enum_message_property, enum_value, enum_type) VALUES ('loan_transaction_type_enum', '1', 'DISBURSEMENT', 'DISBURSEMENT', 'f');
INSERT INTO public.r_enum_value (enum_name, enum_id, enum_message_property, enum_value, enum_type) VALUES ('loan_transaction_type_enum', '2', 'REPAYMENT', 'REPAYMENT', 'f');
INSERT INTO public.r_enum_value (enum_name, enum_id, enum_message_property, enum_value, enum_type) VALUES ('loan_transaction_type_enum', '3', 'CONTRA', 'CONTRA', 'f');
INSERT INTO public.r_enum_value (enum_name, enum_id, enum_message_property, enum_value, enum_type) VALUES ('loan_transaction_type_enum', '4', 'WAIVE_INTEREST', 'WAIVE_INTEREST', 'f');
INSERT INTO public.r_enum_value (enum_name, enum_id, enum_message_property, enum_value, enum_type) VALUES ('loan_transaction_type_enum', '5', 'REPAYMENT_AT_DISBURSEMENT', 'REPAYMENT_AT_DISBURSEMENT', 'f');
INSERT INTO public.r_enum_value (enum_name, enum_id, enum_message_property, enum_value, enum_type) VALUES ('loan_transaction_type_enum', '6', 'WRITEOFF', 'WRITEOFF', 'f');
INSERT INTO public.r_enum_value (enum_name, enum_id, enum_message_property, enum_value, enum_type) VALUES ('loan_transaction_type_enum', '7', 'MARKED_FOR_RESCHEDULING', 'MARKED_FOR_RESCHEDULING', 'f');
INSERT INTO public.r_enum_value (enum_name, enum_id, enum_message_property, enum_value, enum_type) VALUES ('loan_transaction_type_enum', '8', 'RECOVERY_REPAYMENT', 'RECOVERY_REPAYMENT', 'f');
INSERT INTO public.r_enum_value (enum_name, enum_id, enum_message_property, enum_value, enum_type) VALUES ('loan_transaction_type_enum', '9', 'WAIVE_CHARGES', 'WAIVE_CHARGES', 'f');
INSERT INTO public.r_enum_value (enum_name, enum_id, enum_message_property, enum_value, enum_type) VALUES ('loan_transaction_type_enum', '10', 'ACCRUAL', 'ACCRUAL', 'f');
INSERT INTO public.r_enum_value (enum_name, enum_id, enum_message_property, enum_value, enum_type) VALUES ('loan_transaction_type_enum', '12', 'INITIATE_TRANSFER', 'INITIATE_TRANSFER', 'f');
INSERT INTO public.r_enum_value (enum_name, enum_id, enum_message_property, enum_value, enum_type) VALUES ('loan_transaction_type_enum', '13', 'APPROVE_TRANSFER', 'APPROVE_TRANSFER', 'f');
INSERT INTO public.r_enum_value (enum_name, enum_id, enum_message_property, enum_value, enum_type) VALUES ('loan_transaction_type_enum', '14', 'WITHDRAW_TRANSFER', 'WITHDRAW_TRANSFER', 'f');
INSERT INTO public.r_enum_value (enum_name, enum_id, enum_message_property, enum_value, enum_type) VALUES ('loan_transaction_type_enum', '15', 'REJECT_TRANSFER', 'REJECT_TRANSFER', 'f');
INSERT INTO public.r_enum_value (enum_name, enum_id, enum_message_property, enum_value, enum_type) VALUES ('loan_transaction_type_enum', '16', 'REFUND', 'REFUND', 'f');
INSERT INTO public.r_enum_value (enum_name, enum_id, enum_message_property, enum_value, enum_type) VALUES ('loan_transaction_type_enum', '17', 'CHARGE_PAYMENT', 'CHARGE_PAYMENT', 'f');
INSERT INTO public.r_enum_value (enum_name, enum_id, enum_message_property, enum_value, enum_type) VALUES ('loan_transaction_type_enum', '18', 'REFUND_FOR_ACTIVE_LOAN', 'REFUND_FOR_ACTIVE_LOAN', 'f');
INSERT INTO public.r_enum_value (enum_name, enum_id, enum_message_property, enum_value, enum_type) VALUES ('loan_transaction_type_enum', '19', 'INCOME_POSTING', 'INCOME_POSTING', 'f');
INSERT INTO public.r_enum_value (enum_name, enum_id, enum_message_property, enum_value, enum_type) VALUES ('loan_type_enum', '1', 'Individual Loan', 'Individual Loan', 'f');
INSERT INTO public.r_enum_value (enum_name, enum_id, enum_message_property, enum_value, enum_type) VALUES ('loan_type_enum', '2', 'Group Loan', 'Group Loan', 'f');
INSERT INTO public.r_enum_value (enum_name, enum_id, enum_message_property, enum_value, enum_type) VALUES ('portfolio_account_type_enum', '1', 'LOAN', 'LOAN', 'f');
INSERT INTO public.r_enum_value (enum_name, enum_id, enum_message_property, enum_value, enum_type) VALUES ('portfolio_account_type_enum', '2', 'SAVING', 'EXPENSE', 'f');
INSERT INTO public.r_enum_value (enum_name, enum_id, enum_message_property, enum_value, enum_type) VALUES ('portfolio_account_type_enum', '3', 'PROVISIONING', 'PROVISIONING', 'f');
INSERT INTO public.r_enum_value (enum_name, enum_id, enum_message_property, enum_value, enum_type) VALUES ('portfolio_account_type_enum', '4', 'SHARES', 'SHARES', 'f');
INSERT INTO public.r_enum_value (enum_name, enum_id, enum_message_property, enum_value, enum_type) VALUES ('processing_result_enum', '0', 'invalid', 'Invalid', 'f');
INSERT INTO public.r_enum_value (enum_name, enum_id, enum_message_property, enum_value, enum_type) VALUES ('processing_result_enum', '1', 'processed', 'Processed', 'f');
INSERT INTO public.r_enum_value (enum_name, enum_id, enum_message_property, enum_value, enum_type) VALUES ('processing_result_enum', '2', 'awaiting.approval', 'Awaiting Approval', 'f');
INSERT INTO public.r_enum_value (enum_name, enum_id, enum_message_property, enum_value, enum_type) VALUES ('processing_result_enum', '3', 'rejected', 'Rejected', 'f');
INSERT INTO public.r_enum_value (enum_name, enum_id, enum_message_property, enum_value, enum_type) VALUES ('repayment_period_frequency_enum', '0', 'Days', 'Days', 'f');
INSERT INTO public.r_enum_value (enum_name, enum_id, enum_message_property, enum_value, enum_type) VALUES ('repayment_period_frequency_enum', '1', 'Weeks', 'Weeks', 'f');
INSERT INTO public.r_enum_value (enum_name, enum_id, enum_message_property, enum_value, enum_type) VALUES ('repayment_period_frequency_enum', '2', 'Months', 'Months', 'f');
INSERT INTO public.r_enum_value (enum_name, enum_id, enum_message_property, enum_value, enum_type) VALUES ('savings_transaction_type_enum', '0', 'INVALID', 'INVALID', 'f');
INSERT INTO public.r_enum_value (enum_name, enum_id, enum_message_property, enum_value, enum_type) VALUES ('savings_transaction_type_enum', '1', 'deposit', 'deposit', 'f');
INSERT INTO public.r_enum_value (enum_name, enum_id, enum_message_property, enum_value, enum_type) VALUES ('savings_transaction_type_enum', '2', 'withdrawal', 'withdrawal', 't');
INSERT INTO public.r_enum_value (enum_name, enum_id, enum_message_property, enum_value, enum_type) VALUES ('savings_transaction_type_enum', '3', 'Interest Posting', 'Interest Posting', 'f');
INSERT INTO public.r_enum_value (enum_name, enum_id, enum_message_property, enum_value, enum_type) VALUES ('savings_transaction_type_enum', '4', 'Withdrawal Fee', 'Withdrawal Fee', 't');
INSERT INTO public.r_enum_value (enum_name, enum_id, enum_message_property, enum_value, enum_type) VALUES ('savings_transaction_type_enum', '5', 'Annual Fee', 'Annual Fee', 't');
INSERT INTO public.r_enum_value (enum_name, enum_id, enum_message_property, enum_value, enum_type) VALUES ('savings_transaction_type_enum', '6', 'Waive Charge', 'Waive Charge', 'f');
INSERT INTO public.r_enum_value (enum_name, enum_id, enum_message_property, enum_value, enum_type) VALUES ('savings_transaction_type_enum', '7', 'Pay Charge', 'Pay Charge', 't');
INSERT INTO public.r_enum_value (enum_name, enum_id, enum_message_property, enum_value, enum_type) VALUES ('savings_transaction_type_enum', '8', 'DIVIDEND_PAYOUT', 'DIVIDEND_PAYOUT', 'f');
INSERT INTO public.r_enum_value (enum_name, enum_id, enum_message_property, enum_value, enum_type) VALUES ('savings_transaction_type_enum', '12', 'Initiate Transfer', 'Initiate Transfer', 'f');
INSERT INTO public.r_enum_value (enum_name, enum_id, enum_message_property, enum_value, enum_type) VALUES ('savings_transaction_type_enum', '13', 'Approve Transfer', 'Approve Transfer', 'f');
INSERT INTO public.r_enum_value (enum_name, enum_id, enum_message_property, enum_value, enum_type) VALUES ('savings_transaction_type_enum', '14', 'Withdraw Transfer', 'Withdraw Transfer', 'f');
INSERT INTO public.r_enum_value (enum_name, enum_id, enum_message_property, enum_value, enum_type) VALUES ('savings_transaction_type_enum', '15', 'Reject Transfer', 'Reject Transfer', 'f');
INSERT INTO public.r_enum_value (enum_name, enum_id, enum_message_property, enum_value, enum_type) VALUES ('savings_transaction_type_enum', '16', 'Written-Off', 'Written-Off', 'f');
INSERT INTO public.r_enum_value (enum_name, enum_id, enum_message_property, enum_value, enum_type) VALUES ('savings_transaction_type_enum', '17', 'Overdraft Interest', 'Overdraft Interest', 'f');
INSERT INTO public.r_enum_value (enum_name, enum_id, enum_message_property, enum_value, enum_type) VALUES ('savings_transaction_type_enum', '19', 'WITHHOLD_TAX', 'WITHHOLD_TAX', 'f');
INSERT INTO public.r_enum_value (enum_name, enum_id, enum_message_property, enum_value, enum_type) VALUES ('status_enum', '0', 'Invalid', 'Invalid', 'f');
INSERT INTO public.r_enum_value (enum_name, enum_id, enum_message_property, enum_value, enum_type) VALUES ('status_enum', '100', 'Pending', 'Pending', 'f');
INSERT INTO public.r_enum_value (enum_name, enum_id, enum_message_property, enum_value, enum_type) VALUES ('status_enum', '300', 'Active', 'Active', 'f');
INSERT INTO public.r_enum_value (enum_name, enum_id, enum_message_property, enum_value, enum_type) VALUES ('status_enum', '600', 'Closed', 'Closed', 'f');
INSERT INTO public.r_enum_value (enum_name, enum_id, enum_message_property, enum_value, enum_type) VALUES ('teller_status', '300', 'Active', 'Active', 'f');
INSERT INTO public.r_enum_value (enum_name, enum_id, enum_message_property, enum_value, enum_type) VALUES ('teller_status', '400', 'Inactive', 'Inactive', 'f');
INSERT INTO public.r_enum_value (enum_name, enum_id, enum_message_property, enum_value, enum_type) VALUES ('teller_status', '600', 'Closed', 'Closed', 'f');
INSERT INTO public.r_enum_value (enum_name, enum_id, enum_message_property, enum_value, enum_type) VALUES ('term_period_frequency_enum', '0', 'Days', 'Days', 'f');
INSERT INTO public.r_enum_value (enum_name, enum_id, enum_message_property, enum_value, enum_type) VALUES ('term_period_frequency_enum', '1', 'Weeks', 'Weeks', 'f');
INSERT INTO public.r_enum_value (enum_name, enum_id, enum_message_property, enum_value, enum_type) VALUES ('term_period_frequency_enum', '2', 'Months', 'Months', 'f');
INSERT INTO public.r_enum_value (enum_name, enum_id, enum_message_property, enum_value, enum_type) VALUES ('term_period_frequency_enum', '3', 'Years', 'Years', 'f');
INSERT INTO public.r_enum_value (enum_name, enum_id, enum_message_property, enum_value, enum_type) VALUES ('transaction_type_enum', '1', 'Disbursement', 'Disbursement', 'f');
INSERT INTO public.r_enum_value (enum_name, enum_id, enum_message_property, enum_value, enum_type) VALUES ('transaction_type_enum', '2', 'Repayment', 'Repayment', 'f');
INSERT INTO public.r_enum_value (enum_name, enum_id, enum_message_property, enum_value, enum_type) VALUES ('transaction_type_enum', '3', 'Contra', 'Contra', 'f');
INSERT INTO public.r_enum_value (enum_name, enum_id, enum_message_property, enum_value, enum_type) VALUES ('transaction_type_enum', '4', 'Waive Interest', 'Waive Interest', 'f');
INSERT INTO public.r_enum_value (enum_name, enum_id, enum_message_property, enum_value, enum_type) VALUES ('transaction_type_enum', '5', 'Repayment At Disbursement', 'Repayment At Disbursement', 'f');
INSERT INTO public.r_enum_value (enum_name, enum_id, enum_message_property, enum_value, enum_type) VALUES ('transaction_type_enum', '6', 'Write-Off', 'Write-Off', 'f');
INSERT INTO public.r_enum_value (enum_name, enum_id, enum_message_property, enum_value, enum_type) VALUES ('transaction_type_enum', '7', 'Marked for Rescheduling', 'Marked for Rescheduling', 'f');
INSERT INTO public.r_enum_value (enum_name, enum_id, enum_message_property, enum_value, enum_type) VALUES ('transaction_type_enum', '8', 'Recovery Repayment', 'Recovery Repayment', 'f');
INSERT INTO public.r_enum_value (enum_name, enum_id, enum_message_property, enum_value, enum_type) VALUES ('transaction_type_enum', '9', 'Waive Charges', 'Waive Charges', 'f');
INSERT INTO public.r_enum_value (enum_name, enum_id, enum_message_property, enum_value, enum_type) VALUES ('transaction_type_enum', '10', 'Apply Charges', 'Apply Charges', 'f');
INSERT INTO public.r_enum_value (enum_name, enum_id, enum_message_property, enum_value, enum_type) VALUES ('transaction_type_enum', '11', 'Apply Interest', 'Apply Interest', 'f');
INSERT INTO public.r_enum_value (enum_name, enum_id, enum_message_property, enum_value, enum_type) VALUES ('transaction_type_enum', '12', 'Initiate Transfer', 'Initiate Transfer', 'f');
INSERT INTO public.r_enum_value (enum_name, enum_id, enum_message_property, enum_value, enum_type) VALUES ('transaction_type_enum', '13', 'Approve Transfer', 'Approve Transfer', 'f');
INSERT INTO public.r_enum_value (enum_name, enum_id, enum_message_property, enum_value, enum_type) VALUES ('transaction_type_enum', '14', 'Withdraw Transfer', 'Withdraw Transfer', 'f');
INSERT INTO public.r_enum_value (enum_name, enum_id, enum_message_property, enum_value, enum_type) VALUES ('transaction_type_enum', '15', 'Reject Transfer', 'Reject Transfer', 'f');
INSERT INTO public.r_enum_value (enum_name, enum_id, enum_message_property, enum_value, enum_type) VALUES ('transaction_type_enum', '16', 'Refund', 'Refund', 'f');
INSERT INTO public.r_enum_value (enum_name, enum_id, enum_message_property, enum_value, enum_type) VALUES ('transaction_type_enum', '17', 'Charge Payment', 'Charge Payment', 'f');
INSERT INTO public.r_enum_value (enum_name, enum_id, enum_message_property, enum_value, enum_type) VALUES ('transaction_type_enum', '18', 'Refund for Active Loan', 'Refund for Active Loan', 'f');
INSERT INTO public.r_enum_value (enum_name, enum_id, enum_message_property, enum_value, enum_type) VALUES ('transaction_type_enum', '19', 'Income Posting', 'Income Posting', 'f');
INSERT INTO public.r_enum_value (enum_name, enum_id, enum_message_property, enum_value, enum_type) VALUES ('transaction_type_enum', '20', 'Credit Balance Refund', 'Credit Balance Refund', 'f');
INSERT INTO public.r_enum_value (enum_name, enum_id, enum_message_property, enum_value, enum_type) VALUES ('transaction_type_enum', '21', 'Merchant Issued Refund', 'Merchant Issued Refund', 'f');
INSERT INTO public.r_enum_value (enum_name, enum_id, enum_message_property, enum_value, enum_type) VALUES ('transaction_type_enum', '22', 'Payout Refund', 'Payout Refund', 'f');
INSERT INTO public.r_enum_value (enum_name, enum_id, enum_message_property, enum_value, enum_type) VALUES ('transaction_type_enum', '23', 'Goodwill Credit', 'Goodwill Credit', 'f');
INSERT INTO public.r_enum_value (enum_name, enum_id, enum_message_property, enum_value, enum_type) VALUES ('transaction_type_enum', '24', 'Charge Refund', 'Charge Refund', 'f');
INSERT INTO public.r_enum_value (enum_name, enum_id, enum_message_property, enum_value, enum_type) VALUES ('transaction_type_enum', '25', 'Chargeback', 'Chargeback', 'f');
INSERT INTO public.r_enum_value (enum_name, enum_id, enum_message_property, enum_value, enum_type) VALUES ('transaction_type_enum', '26', 'Charge Adjustment', 'Charge Adjustment', 'f');
INSERT INTO public.r_enum_value (enum_name, enum_id, enum_message_property, enum_value, enum_type) VALUES ('transaction_type_enum', '27', 'Charge-off', 'Charge-off', 'f');
INSERT INTO public.r_enum_value (enum_name, enum_id, enum_message_property, enum_value, enum_type) VALUES ('loan_transaction_strategy_id', '8', 'Due penalty, fee, interest, principal, In advance principal, penalty, fee, interest', 'Due penalty, fee, interest, principal, In advance principal, penalty, fee, interest', 'f');
INSERT INTO public.r_enum_value (enum_name, enum_id, enum_message_property, enum_value, enum_type) VALUES ('loan_transaction_strategy_id', '9', 'Due penalty, interest, principal, fee, In advance penalty, interest, principal, fee', 'Due penalty, interest, principal, fee, In advance penalty, interest, principal, fee', 'f');
INSERT INTO public.r_enum_value (enum_name, enum_id, enum_message_property, enum_value, enum_type) VALUES ('transaction_type_enum', '31', 'Interest Payment Waiver', 'Interest Payment Waiver', 'f');
INSERT INTO public.r_enum_value (enum_name, enum_id, enum_message_property, enum_value, enum_type) VALUES ('transaction_type_enum', '32', 'Accrual Activity', 'Accrual Activity', 'f');
INSERT INTO public.r_enum_value (enum_name, enum_id, enum_message_property, enum_value, enum_type) VALUES ('transaction_type_enum', '33', 'Interest Refund', 'Interest Refund', 'f');
INSERT INTO public.r_enum_value (enum_name, enum_id, enum_message_property, enum_value, enum_type) VALUES ('transaction_type_enum', '36', 'Capitalized Income Amortization', 'Capitalized Income Amortization', 'f');
INSERT INTO public.r_enum_value (enum_name, enum_id, enum_message_property, enum_value, enum_type) VALUES ('transaction_type_enum', '37', 'Capitalized Income Adjustment', 'Capitalized Income Adjustment', 'f');
INSERT INTO public.r_enum_value (enum_name, enum_id, enum_message_property, enum_value, enum_type) VALUES ('transaction_type_enum', '39', 'Capitalized Income Amortization Adjustment', 'Capitalized Income Amortization Adjustment', 'f');
INSERT INTO public.r_enum_value (enum_name, enum_id, enum_message_property, enum_value, enum_type) VALUES ('transaction_type_enum', '40', 'Buy Down Fee', 'Buy Down Fee', 'f');
INSERT INTO public.r_enum_value (enum_name, enum_id, enum_message_property, enum_value, enum_type) VALUES ('transaction_type_enum', '41', 'Buy Down Fee Adjustment', 'Buy Down Fee Adjustment', 'f');
INSERT INTO public.r_enum_value (enum_name, enum_id, enum_message_property, enum_value, enum_type) VALUES ('transaction_type_enum', '42', 'Buy Down Fee Amortization', 'Buy Down Fee Amortization', 'f');
INSERT INTO public.r_enum_value (enum_name, enum_id, enum_message_property, enum_value, enum_type) VALUES ('transaction_type_enum', '43', 'Buy Down Fee Amortization Adjustment', 'Buy Down Fee Amortization Adjustment', 'f');
INSERT INTO public.r_enum_value (enum_name, enum_id, enum_message_property, enum_value, enum_type) VALUES ('processing_result_enum', '4', 'underProcessing', 'Under Processing', 'f');
INSERT INTO public.r_enum_value (enum_name, enum_id, enum_message_property, enum_value, enum_type) VALUES ('processing_result_enum', '5', 'error', 'Error', 'f');


-- Data for Name: ref_loan_transaction_processing_strategy; Type: TABLE DATA; Schema: public; Owner: -

INSERT INTO public.ref_loan_transaction_processing_strategy (id, code, name, sort_order) VALUES ('1', 'mifos-standard-strategy', 'Penalties, Fees, Interest, Principal order', '1');
INSERT INTO public.ref_loan_transaction_processing_strategy (id, code, name, sort_order) VALUES ('2', 'heavensfamily-strategy', 'HeavensFamily Unique', '6');
INSERT INTO public.ref_loan_transaction_processing_strategy (id, code, name, sort_order) VALUES ('3', 'creocore-strategy', 'Creocore Unique', '7');
INSERT INTO public.ref_loan_transaction_processing_strategy (id, code, name, sort_order) VALUES ('4', 'rbi-india-strategy', 'Overdue/Due Fee/Int,Principal', '2');
INSERT INTO public.ref_loan_transaction_processing_strategy (id, code, name, sort_order) VALUES ('5', 'principal-interest-penalties-fees-order-strategy', 'Principal, Interest, Penalties, Fees Order', '3');
INSERT INTO public.ref_loan_transaction_processing_strategy (id, code, name, sort_order) VALUES ('6', 'interest-principal-penalties-fees-order-strategy', 'Interest, Principal, Penalties, Fees Order', '4');
INSERT INTO public.ref_loan_transaction_processing_strategy (id, code, name, sort_order) VALUES ('7', 'early-repayment-strategy', 'Early Repayment Strategy', '5');
INSERT INTO public.ref_loan_transaction_processing_strategy (id, code, name, sort_order) VALUES ('8', 'due-penalty-fee-interest-principal-in-advance-principal-penalty-fee-interest-strategy', 'Due penalty, fee, interest, principal, In advance principal, penalty, fee, interest', '8');
INSERT INTO public.ref_loan_transaction_processing_strategy (id, code, name, sort_order) VALUES ('9', 'due-penalty-interest-principal-fee-in-advance-penalty-interest-principal-fee-strategy', 'Due penalty, interest, principal, fee, In advance penalty, interest, principal, fee', '9');


-- Data for Name: request_audit_table; Type: TABLE DATA; Schema: public; Owner: -



-- Data for Name: rpt_sequence; Type: TABLE DATA; Schema: public; Owner: -



-- Data for Name: scheduled_email_campaign; Type: TABLE DATA; Schema: public; Owner: -



-- Data for Name: scheduled_email_configuration; Type: TABLE DATA; Schema: public; Owner: -

INSERT INTO public.scheduled_email_configuration (id, name, value) VALUES ('1', 'SMTP_SERVER', NULL);
INSERT INTO public.scheduled_email_configuration (id, name, value) VALUES ('2', 'SMTP_PORT', NULL);
INSERT INTO public.scheduled_email_configuration (id, name, value) VALUES ('3', 'SMTP_USERNAME', NULL);
INSERT INTO public.scheduled_email_configuration (id, name, value) VALUES ('4', 'SMTP_PASSWORD', NULL);


-- Data for Name: scheduled_email_messages_outbound; Type: TABLE DATA; Schema: public; Owner: -



-- Data for Name: scheduler_detail; Type: TABLE DATA; Schema: public; Owner: -

INSERT INTO public.scheduler_detail (id, is_suspended, execute_misfired_jobs, reset_scheduler_on_bootup) VALUES ('1', 'f', 't', 't');


-- Data for Name: sms_campaign; Type: TABLE DATA; Schema: public; Owner: -



-- Data for Name: sms_messages_outbound; Type: TABLE DATA; Schema: public; Owner: -



-- Data for Name: stretchy_parameter; Type: TABLE DATA; Schema: public; Owner: -

INSERT INTO public.stretchy_parameter (id, parameter_name, parameter_variable, parameter_label, "parameter_displayType", "parameter_FormatType", parameter_default, special, "selectOne", "selectAll", parameter_sql, parent_id) VALUES ('1', 'startDateSelect', 'startDate', 'startDate', 'date', 'date', 'today', NULL, NULL, NULL, NULL, NULL);
INSERT INTO public.stretchy_parameter (id, parameter_name, parameter_variable, parameter_label, "parameter_displayType", "parameter_FormatType", parameter_default, special, "selectOne", "selectAll", parameter_sql, parent_id) VALUES ('2', 'endDateSelect', 'endDate', 'endDate', 'date', 'date', 'today', NULL, NULL, NULL, NULL, NULL);
INSERT INTO public.stretchy_parameter (id, parameter_name, parameter_variable, parameter_label, "parameter_displayType", "parameter_FormatType", parameter_default, special, "selectOne", "selectAll", parameter_sql, parent_id) VALUES ('3', 'obligDateTypeSelect', 'obligDateType', 'obligDateType', 'select', 'number', '0', NULL, NULL, NULL, 'select * from  (select 1 as id, ''Closed'' AS name union all select 2, ''Disbursal'' ) x  order by x.id', NULL);
INSERT INTO public.stretchy_parameter (id, parameter_name, parameter_variable, parameter_label, "parameter_displayType", "parameter_FormatType", parameter_default, special, "selectOne", "selectAll", parameter_sql, parent_id) VALUES ('5', 'OfficeIdSelectOne', 'officeId', 'Office', 'select', 'number', '0', NULL, 'Y', NULL, 'select id, concat(substring(''........................................'', 1,        ((LENGTH(hierarchy) - LENGTH(REPLACE(hierarchy, ''.'', '''')) - 1) * 4)),      name) as tc  from m_office  where hierarchy like concat(''${currentUserHierarchy}'', ''%'')  order by hierarchy', NULL);
INSERT INTO public.stretchy_parameter (id, parameter_name, parameter_variable, parameter_label, "parameter_displayType", "parameter_FormatType", parameter_default, special, "selectOne", "selectAll", parameter_sql, parent_id) VALUES ('6', 'loanOfficerIdSelectAll', 'loanOfficerId', 'Loan Officer', 'select', 'number', '0', NULL, NULL, 'Y', '(select lo.id, lo.display_name AS name   from m_office o join m_office ounder on ounder.hierarchy like concat(o.hierarchy, ''%'')  join m_staff lo on lo.office_id = ounder.id  where lo.is_loan_officer = true  and o.id = ''${officeId}'')  union all  (select -10, ''-'')  order by 2', '5');
INSERT INTO public.stretchy_parameter (id, parameter_name, parameter_variable, parameter_label, "parameter_displayType", "parameter_FormatType", parameter_default, special, "selectOne", "selectAll", parameter_sql, parent_id) VALUES ('10', 'currencyIdSelectAll', 'currencyId', 'Currency', 'select', 'number', '0', NULL, NULL, 'Y', 'select code, name  from m_organisation_currency  order by code', NULL);
INSERT INTO public.stretchy_parameter (id, parameter_name, parameter_variable, parameter_label, "parameter_displayType", "parameter_FormatType", parameter_default, special, "selectOne", "selectAll", parameter_sql, parent_id) VALUES ('20', 'fundIdSelectAll', 'fundId', 'Fund', 'select', 'number', '0', NULL, NULL, 'Y', '(select id, name  from m_fund)  union all  (select -10, ''-'')  order by 2', NULL);
INSERT INTO public.stretchy_parameter (id, parameter_name, parameter_variable, parameter_label, "parameter_displayType", "parameter_FormatType", parameter_default, special, "selectOne", "selectAll", parameter_sql, parent_id) VALUES ('26', 'loanPurposeIdSelectAll', 'loanPurposeId', 'Loan Purpose', 'select', 'number', '0', NULL, NULL, 'Y', 'select -10 as id, ''-'' as code_value  union all  select * from (select v.id, v.code_value  from m_code c  join m_code_value v on v.code_id = c.id  where c.code_name = ''loanPurpose''  order by v.order_position)  x', NULL);
INSERT INTO public.stretchy_parameter (id, parameter_name, parameter_variable, parameter_label, "parameter_displayType", "parameter_FormatType", parameter_default, special, "selectOne", "selectAll", parameter_sql, parent_id) VALUES ('100', 'parTypeSelect', 'parType', 'parType', 'select', 'number', '0', NULL, NULL, NULL, 'select * from  (select 1 as id, ''Principal Only'' AS name union all  select 2, ''Principal + Interest'' union all select 3, ''Principal + Interest + Fees'' union all  select 4, ''Principal + Interest + Fees + Penalties'') x  order by x.id', NULL);
INSERT INTO public.stretchy_parameter (id, parameter_name, parameter_variable, parameter_label, "parameter_displayType", "parameter_FormatType", parameter_default, special, "selectOne", "selectAll", parameter_sql, parent_id) VALUES ('1004', 'selectAccount', 'accountNo', 'Enter Account No', 'text', 'string', 'n/a', NULL, NULL, NULL, NULL, NULL);
INSERT INTO public.stretchy_parameter (id, parameter_name, parameter_variable, parameter_label, "parameter_displayType", "parameter_FormatType", parameter_default, special, "selectOne", "selectAll", parameter_sql, parent_id) VALUES ('1005', 'savingsProductIdSelectAll', 'savingsProductId', 'Product', 'select', 'number', '0', NULL, NULL, 'Y', 'select p.id, p.name  from m_savings_product p  order by 2', NULL);
INSERT INTO public.stretchy_parameter (id, parameter_name, parameter_variable, parameter_label, "parameter_displayType", "parameter_FormatType", parameter_default, special, "selectOne", "selectAll", parameter_sql, parent_id) VALUES ('1006', 'transactionId', 'transactionId', 'transactionId', 'text', 'string', 'n/a', NULL, NULL, NULL, NULL, NULL);
INSERT INTO public.stretchy_parameter (id, parameter_name, parameter_variable, parameter_label, "parameter_displayType", "parameter_FormatType", parameter_default, special, "selectOne", "selectAll", parameter_sql, parent_id) VALUES ('1007', 'selectCenterId', 'centerId', 'Enter Center Id', 'text', 'string', 'n/a', NULL, NULL, NULL, NULL, NULL);
INSERT INTO public.stretchy_parameter (id, parameter_name, parameter_variable, parameter_label, "parameter_displayType", "parameter_FormatType", parameter_default, special, "selectOne", "selectAll", parameter_sql, parent_id) VALUES ('1008', 'SelectGLAccountNO', 'GLAccountNO', 'GLAccountNO', 'select', 'number', '0', NULL, NULL, NULL, 'select id aid,name aname  from acc_gl_account', NULL);
INSERT INTO public.stretchy_parameter (id, parameter_name, parameter_variable, parameter_label, "parameter_displayType", "parameter_FormatType", parameter_default, special, "selectOne", "selectAll", parameter_sql, parent_id) VALUES ('1009', 'asOnDate', 'asOn', 'As On', 'date', 'date', 'today', NULL, NULL, NULL, NULL, NULL);
INSERT INTO public.stretchy_parameter (id, parameter_name, parameter_variable, parameter_label, "parameter_displayType", "parameter_FormatType", parameter_default, special, "selectOne", "selectAll", parameter_sql, parent_id) VALUES ('1010', 'SavingsAccountSubStatus', 'subStatus', 'SavingsAccountDormancyStatus', 'select', 'number', '100', NULL, NULL, NULL, 'select * from  (select 100 as id, ''Inactive'' as name  union all  select 200 as id, ''Dormant'' as  name union all   select 300 as id, ''Escheat'' as name) x  order by x.id', NULL);
INSERT INTO public.stretchy_parameter (id, parameter_name, parameter_variable, parameter_label, "parameter_displayType", "parameter_FormatType", parameter_default, special, "selectOne", "selectAll", parameter_sql, parent_id) VALUES ('1011', 'cycleXSelect', 'cycleX', 'Cycle X Number', 'text', 'number', 'n/a', NULL, NULL, NULL, NULL, NULL);
INSERT INTO public.stretchy_parameter (id, parameter_name, parameter_variable, parameter_label, "parameter_displayType", "parameter_FormatType", parameter_default, special, "selectOne", "selectAll", parameter_sql, parent_id) VALUES ('1012', 'cycleYSelect', 'cycleY', 'Cycle Y Number', 'text', 'number', 'n/a', NULL, NULL, NULL, NULL, NULL);
INSERT INTO public.stretchy_parameter (id, parameter_name, parameter_variable, parameter_label, "parameter_displayType", "parameter_FormatType", parameter_default, special, "selectOne", "selectAll", parameter_sql, parent_id) VALUES ('1013', 'fromXSelect', 'fromX', 'From X Number', 'text', 'number', 'n/a', NULL, NULL, NULL, NULL, NULL);
INSERT INTO public.stretchy_parameter (id, parameter_name, parameter_variable, parameter_label, "parameter_displayType", "parameter_FormatType", parameter_default, special, "selectOne", "selectAll", parameter_sql, parent_id) VALUES ('1014', 'toYSelect', 'toY', 'To Y Number', 'text', 'number', 'n/a', NULL, NULL, NULL, NULL, NULL);
INSERT INTO public.stretchy_parameter (id, parameter_name, parameter_variable, parameter_label, "parameter_displayType", "parameter_FormatType", parameter_default, special, "selectOne", "selectAll", parameter_sql, parent_id) VALUES ('1015', 'overdueXSelect', 'overdueX', 'Overdue X Number', 'text', 'number', 'n/a', NULL, NULL, NULL, NULL, NULL);
INSERT INTO public.stretchy_parameter (id, parameter_name, parameter_variable, parameter_label, "parameter_displayType", "parameter_FormatType", parameter_default, special, "selectOne", "selectAll", parameter_sql, parent_id) VALUES ('1016', 'overdueYSelect', 'overdueY', 'Overdue Y Number', 'text', 'number', 'n/a', NULL, NULL, NULL, NULL, NULL);
INSERT INTO public.stretchy_parameter (id, parameter_name, parameter_variable, parameter_label, "parameter_displayType", "parameter_FormatType", parameter_default, special, "selectOne", "selectAll", parameter_sql, parent_id) VALUES ('1017', 'DefaultLoan', 'loanId', 'Loan', 'none', 'number', '-1', NULL, NULL, 'Y', 'select ml.id  from m_loan ml  left join m_client mc on mc.id = ml.client_id  left join m_office mo on mo.id = mc.office_id  where mo.id = ''${officeId}'' or ''${officeId}'' = -1', '5');
INSERT INTO public.stretchy_parameter (id, parameter_name, parameter_variable, parameter_label, "parameter_displayType", "parameter_FormatType", parameter_default, special, "selectOne", "selectAll", parameter_sql, parent_id) VALUES ('1018', 'DefaultClient', 'clientId', 'Client', 'none', 'number', '-1', NULL, NULL, 'Y', 'select mc.id  from m_client mc  left join m_office mo on mc.office_id = mo.id  where mo.id = ''${officeId}'' or ''${officeId}'' = -1', '5');
INSERT INTO public.stretchy_parameter (id, parameter_name, parameter_variable, parameter_label, "parameter_displayType", "parameter_FormatType", parameter_default, special, "selectOne", "selectAll", parameter_sql, parent_id) VALUES ('1019', 'DefaultGroup', 'groupId', 'Group', 'none', 'number', '-1', NULL, NULL, 'Y', 'select mg.id  from m_group mg left join m_office mo on mg.office_id = mo.id where mo.id = ''${officeId}'' or ''${officeId}'' = -1', '5');
INSERT INTO public.stretchy_parameter (id, parameter_name, parameter_variable, parameter_label, "parameter_displayType", "parameter_FormatType", parameter_default, special, "selectOne", "selectAll", parameter_sql, parent_id) VALUES ('1020', 'SelectLoanType', 'loanType', 'Loan Type', 'select', 'number', '-1', NULL, NULL, 'Y', 'select enum_id as id, enum_value as value from r_enum_value where enum_name = ''loan_type_enum''', NULL);
INSERT INTO public.stretchy_parameter (id, parameter_name, parameter_variable, parameter_label, "parameter_displayType", "parameter_FormatType", parameter_default, special, "selectOne", "selectAll", parameter_sql, parent_id) VALUES ('1021', 'DefaultSavings', 'savingsId', 'Savings', 'none', 'number', '-1', NULL, NULL, 'Y', NULL, '5');
INSERT INTO public.stretchy_parameter (id, parameter_name, parameter_variable, parameter_label, "parameter_displayType", "parameter_FormatType", parameter_default, special, "selectOne", "selectAll", parameter_sql, parent_id) VALUES ('1022', 'DefaultSavingsTransactionId', 'savingsTransactionId', 'Savings Transaction', 'none', 'number', '-1', NULL, NULL, 'Y', NULL, '5');
INSERT INTO public.stretchy_parameter (id, parameter_name, parameter_variable, parameter_label, "parameter_displayType", "parameter_FormatType", parameter_default, special, "selectOne", "selectAll", parameter_sql, parent_id) VALUES ('25', 'loanProductIdSelectAll', 'loanProductId', 'Product', 'select', 'number', '0', NULL, NULL, 'Y', 'select p.id, p.name\r\nfrom m_product_loan p\r\nwhere (p.currency_code = ''${currencyId}'' or ''-1''= ''${currencyId}'')\r\norder by 2', '10');
INSERT INTO public.stretchy_parameter (id, parameter_name, parameter_variable, parameter_label, "parameter_displayType", "parameter_FormatType", parameter_default, special, "selectOne", "selectAll", parameter_sql, parent_id) VALUES ('1002', 'FullParameterList', NULL, 'n/a', 'n/a', 'n/a', 'n/a', 'Y', NULL, NULL, 'select sp.parameter_name, sp.parameter_variable, sp.parameter_label, sp."parameter_displayType", sp."parameter_FormatType", sp.parameter_default, sp."selectOne", sp."selectAll", spp.parameter_name as parentParameterName from stretchy_parameter sp  left join stretchy_parameter spp on spp.id = sp.parent_id  where sp.special is null  and exists     (select ''f''    from stretchy_report sr    join stretchy_report_parameter srp on srp.report_id = sr.id    where sr.report_name in(${reportListing})    and srp.parameter_id = sp.id   ) order by sp.id', NULL);
INSERT INTO public.stretchy_parameter (id, parameter_name, parameter_variable, parameter_label, "parameter_displayType", "parameter_FormatType", parameter_default, special, "selectOne", "selectAll", parameter_sql, parent_id) VALUES ('1003', 'reportCategoryList', NULL, 'n/a', 'n/a', 'n/a', 'n/a', 'Y', NULL, NULL, 'select  r.id as report_id, r.report_name, r.report_type, r.report_subtype, r.report_category,\n  rp.id as parameter_id, rp.report_parameter_name, p.parameter_name\n  from stretchy_report r\n  left join stretchy_report_parameter rp on rp.report_id = r.id\n  left join stretchy_parameter p on p.id = rp.parameter_id\n  where r.report_category = ''${reportCategory}''\n  and r.use_report is true\n  and exists\n  (select ''f''\n  from m_appuser_role ur \n  join m_role rr on rr.id = ur.role_id\n  join m_role_permission rp on rp.role_id = rr.id\n  join m_permission p on p.id = rp.permission_id\n  where ur.appuser_id = ${currentUserId}\n  and (p.code in (''ALL_FUNCTIONS_READ'', ''ALL_FUNCTIONS'') or p.code = concat(''READ_'', r.report_name)) )\n  order by r.report_category, r.report_name, rp.id', NULL);
INSERT INTO public.stretchy_parameter (id, parameter_name, parameter_variable, parameter_label, "parameter_displayType", "parameter_FormatType", parameter_default, special, "selectOne", "selectAll", parameter_sql, parent_id) VALUES ('1001', 'FullReportList', NULL, 'n/a', 'n/a', 'n/a', 'n/a', 'Y', NULL, NULL, 'select  r.id as report_id, r.report_name, r.report_type, r.report_subtype, r.report_category,\nrp.id as parameter_id, rp.report_parameter_name, p.parameter_name\n  from stretchy_report r\n  left join stretchy_report_parameter rp on rp.report_id = r.id \n  left join stretchy_parameter p on p.id = rp.parameter_id\n  where r.use_report is true\n  and exists\n  ( select ''f''\n  from m_appuser_role ur \n  join m_role rr on rr.id = ur.role_id\n  join m_role_permission rp on rp.role_id = rr.id\n  join m_permission p on p.id = rp.permission_id\n  where ur.appuser_id = ${currentUserId}\n  and (p.code in (''ALL_FUNCTIONS_READ'', ''ALL_FUNCTIONS'') or p.code = concat(''READ_'', r.report_name)) )\n  order by r.report_category, r.report_name, rp.id', NULL);


-- Data for Name: stretchy_report_parameter; Type: TABLE DATA; Schema: public; Owner: -

INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('1', '1', '5', NULL);
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('2', '2', '5', NULL);
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('3', '2', '6', NULL);
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('4', '2', '10', NULL);
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('5', '2', '20', NULL);
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('6', '2', '25', NULL);
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('7', '2', '26', NULL);
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('8', '5', '5', NULL);
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('9', '5', '6', NULL);
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('10', '5', '10', NULL);
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('11', '5', '20', NULL);
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('12', '5', '25', NULL);
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('13', '5', '26', NULL);
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('14', '6', '5', NULL);
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('15', '6', '6', NULL);
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('16', '6', '10', NULL);
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('17', '6', '20', NULL);
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('18', '6', '25', NULL);
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('19', '6', '26', NULL);
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('20', '7', '5', NULL);
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('21', '7', '6', NULL);
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('22', '7', '10', NULL);
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('23', '7', '20', NULL);
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('24', '7', '25', NULL);
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('25', '7', '26', NULL);
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('26', '8', '5', NULL);
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('27', '8', '6', NULL);
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('28', '8', '10', NULL);
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('29', '8', '25', NULL);
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('30', '8', '26', NULL);
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('31', '11', '5', NULL);
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('32', '11', '6', NULL);
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('33', '11', '10', NULL);
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('34', '11', '20', NULL);
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('35', '11', '25', NULL);
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('36', '11', '26', NULL);
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('37', '11', '100', NULL);
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('38', '12', '5', NULL);
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('39', '12', '6', NULL);
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('40', '12', '10', NULL);
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('41', '12', '20', NULL);
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('42', '12', '25', NULL);
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('43', '12', '26', NULL);
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('44', '13', '1', NULL);
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('45', '13', '2', NULL);
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('46', '13', '3', NULL);
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('47', '13', '5', NULL);
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('48', '13', '6', NULL);
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('49', '13', '10', NULL);
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('50', '13', '20', NULL);
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('51', '13', '25', NULL);
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('52', '13', '26', NULL);
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('53', '14', '1', NULL);
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('54', '14', '2', NULL);
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('55', '14', '3', NULL);
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('56', '14', '5', NULL);
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('57', '14', '6', NULL);
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('58', '14', '10', NULL);
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('59', '14', '20', NULL);
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('60', '14', '25', NULL);
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('61', '14', '26', NULL);
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('62', '15', '5', NULL);
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('63', '15', '6', NULL);
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('64', '15', '10', NULL);
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('65', '15', '20', NULL);
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('66', '15', '25', NULL);
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('67', '15', '26', NULL);
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('68', '15', '100', NULL);
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('69', '16', '5', NULL);
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('70', '16', '6', NULL);
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('71', '16', '10', NULL);
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('72', '16', '20', NULL);
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('73', '16', '25', NULL);
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('74', '16', '26', NULL);
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('75', '16', '100', NULL);
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('76', '20', '1', NULL);
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('77', '20', '2', NULL);
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('78', '20', '10', NULL);
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('79', '20', '20', NULL);
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('80', '21', '1', NULL);
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('81', '21', '2', NULL);
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('82', '21', '5', NULL);
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('83', '21', '10', NULL);
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('84', '21', '20', NULL);
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('93', '51', '1', NULL);
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('94', '51', '2', NULL);
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('95', '51', '5', NULL);
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('96', '51', '10', NULL);
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('97', '51', '25', NULL);
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('98', '52', '5', NULL);
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('99', '53', '5', NULL);
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('100', '53', '10', NULL);
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('101', '54', '1', NULL);
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('102', '54', '2', NULL);
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('103', '54', '5', NULL);
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('104', '54', '10', NULL);
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('105', '54', '25', NULL);
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('106', '55', '5', NULL);
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('107', '55', '6', NULL);
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('108', '55', '10', NULL);
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('109', '55', '20', NULL);
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('110', '55', '25', NULL);
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('111', '55', '26', NULL);
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('112', '56', '5', NULL);
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('113', '56', '6', NULL);
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('114', '56', '10', NULL);
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('115', '56', '20', NULL);
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('116', '56', '25', NULL);
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('117', '56', '26', NULL);
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('118', '56', '100', NULL);
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('119', '57', '5', NULL);
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('120', '57', '6', NULL);
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('121', '57', '10', NULL);
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('122', '57', '20', NULL);
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('123', '57', '25', NULL);
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('124', '57', '26', NULL);
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('125', '58', '5', NULL);
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('126', '58', '6', NULL);
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('127', '58', '10', NULL);
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('128', '58', '20', NULL);
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('129', '58', '25', NULL);
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('130', '58', '26', NULL);
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('131', '58', '100', NULL);
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('132', '59', '1', NULL);
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('133', '59', '2', NULL);
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('134', '59', '5', NULL);
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('135', '59', '6', NULL);
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('136', '59', '10', NULL);
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('137', '59', '20', NULL);
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('138', '59', '25', NULL);
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('139', '59', '26', NULL);
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('140', '61', '5', NULL);
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('141', '61', '10', NULL);
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('145', '93', '1', NULL);
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('146', '93', '2', NULL);
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('147', '93', '5', NULL);
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('148', '93', '6', NULL);
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('256', '106', '2', NULL);
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('257', '106', '6', NULL);
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('258', '106', '5', NULL);
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('259', '106', '1', NULL);
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('418', '149', '5', '');
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('419', '150', '5', '');
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('420', '151', '5', '');
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('421', '152', '5', '');
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('422', '153', '5', '');
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('423', '154', '5', '');
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('424', '155', '5', '');
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('425', '156', '5', '');
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('441', '165', '1010', NULL);
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('442', '165', '5', NULL);
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('443', '166', '5', 'officeId');
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('444', '166', '6', 'loanOfficerId');
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('445', '167', '5', 'officeId');
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('446', '167', '6', 'loanOfficerId');
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('447', '168', '5', 'officeId');
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('448', '168', '6', 'loanOfficerId');
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('449', '168', '1011', 'cycleX');
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('450', '168', '1012', 'cycleY');
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('451', '169', '5', 'officeId');
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('452', '169', '6', 'loanOfficerId');
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('453', '169', '1013', 'fromX');
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('454', '169', '1014', 'toY');
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('455', '170', '5', 'officeId');
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('456', '170', '6', 'loanOfficerId');
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('457', '170', '1013', 'fromX');
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('458', '170', '1014', 'toY');
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('459', '171', '5', 'officeId');
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('460', '171', '6', 'loanOfficerId');
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('461', '172', '5', 'officeId');
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('462', '172', '6', 'loanOfficerId');
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('463', '173', '5', 'officeId');
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('464', '173', '6', 'loanOfficerId');
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('465', '173', '1013', 'fromX');
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('466', '173', '1014', 'toY');
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('467', '173', '1015', 'overdueX');
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('468', '173', '1016', 'overdueY');
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('469', '174', '5', 'officeId');
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('470', '174', '6', 'loanOfficerId');
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('471', '174', '1013', 'fromX');
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('472', '174', '1014', 'toY');
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('473', '175', '5', 'officeId');
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('474', '175', '6', 'loanOfficerId');
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('475', '175', '1013', 'fromX');
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('476', '175', '1014', 'toY');
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('477', '175', '1015', 'overdueX');
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('478', '175', '1016', 'overdueY');
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('479', '176', '5', 'officeId');
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('480', '176', '6', 'loanOfficerId');
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('481', '177', '5', 'officeId');
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('482', '177', '6', 'loanOfficerId');
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('483', '177', '1013', 'fromX');
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('484', '177', '1014', 'toY');
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('485', '178', '5', 'officeId');
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('486', '178', '6', 'loanOfficerId');
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('487', '178', '1013', 'fromX');
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('488', '178', '1014', 'toY');
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('489', '181', '5', 'officeId');
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('490', '180', '5', 'officeId');
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('491', '179', '5', 'officeId');
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('492', '181', '6', 'loanOfficerId');
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('493', '180', '6', 'loanOfficerId');
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('494', '179', '6', 'loanOfficerId');
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('495', '181', '1017', 'loanId');
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('496', '180', '1017', 'loanId');
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('497', '181', '1018', 'clientId');
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('498', '180', '1018', 'clientId');
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('499', '181', '1019', 'groupId');
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('500', '180', '1019', 'groupId');
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('501', '181', '1020', 'loanType');
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('502', '180', '1020', 'loanType');
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('503', '179', '1020', 'loanType');
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('504', '182', '5', 'officeId');
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('505', '183', '5', 'officeId');
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('506', '182', '6', 'loanOfficerId');
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('507', '183', '6', 'loanOfficerId');
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('508', '182', '1018', 'clientId');
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('509', '183', '1018', 'clientId');
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('510', '184', '5', 'officeId');
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('511', '184', '6', 'loanOfficerId');
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('512', '184', '1021', 'savingsId');
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('513', '185', '5', 'officeId');
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('514', '185', '6', 'loanOfficerId');
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('515', '185', '1021', 'savingsId');
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('516', '186', '5', 'officeId');
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('517', '186', '6', 'loanOfficerId');
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('518', '186', '1022', 'savingsTransactionId');
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('519', '187', '5', 'officeId');
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('520', '187', '6', 'loanOfficerId');
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('521', '187', '1022', 'savingsTransactionId');
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('522', '193', '1', 'fromDate');
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('523', '193', '5', 'branch');
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('524', '193', '2', 'toDate');
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('525', '194', '1008', 'GLAccountNO');
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('526', '194', '5', 'officeId');
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('527', '194', '2', 'endDate');
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('528', '194', '1', 'startDate');
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('529', '196', '5', 'branch');
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('530', '196', '2', 'date');
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('531', '195', '5', 'branch');
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('532', '195', '1', 'fromDate');
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('533', '195', '2', 'toDate');
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('534', '197', '5', 'officeId');
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('535', '197', '2', 'endDate');
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('536', '198', '5', 'officeId');
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('537', '198', '2', 'endDate');
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('538', '199', '2', 'officeId');
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('539', '199', '5', 'endDate');
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('540', '200', '2', 'officeId');
INSERT INTO public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) VALUES ('541', '200', '5', 'endDate');


-- Data for Name: twofactor_access_token; Type: TABLE DATA; Schema: public; Owner: -



-- Data for Name: twofactor_configuration; Type: TABLE DATA; Schema: public; Owner: -

INSERT INTO public.twofactor_configuration (id, name, value) VALUES ('1', 'otp-delivery-email-enable', 'true');
INSERT INTO public.twofactor_configuration (id, name, value) VALUES ('2', 'otp-delivery-email-subject', 'Fineract Two-Factor Authentication Token');
INSERT INTO public.twofactor_configuration (id, name, value) VALUES ('3', 'otp-delivery-email-body', 'Hello {{username}}.\nYour OTP login token is {{token}}.');
INSERT INTO public.twofactor_configuration (id, name, value) VALUES ('4', 'otp-delivery-sms-enable', 'false');
INSERT INTO public.twofactor_configuration (id, name, value) VALUES ('5', 'otp-delivery-sms-provider', '1');
INSERT INTO public.twofactor_configuration (id, name, value) VALUES ('6', 'otp-delivery-sms-text', 'Your authentication token for Fineract is {{token}}.');
INSERT INTO public.twofactor_configuration (id, name, value) VALUES ('7', 'otp-token-live-time', '300');
INSERT INTO public.twofactor_configuration (id, name, value) VALUES ('8', 'otp-token-length', '5');
INSERT INTO public.twofactor_configuration (id, name, value) VALUES ('9', 'access-token-live-time', '86400');
INSERT INTO public.twofactor_configuration (id, name, value) VALUES ('10', 'access-token-live-time-extended', '604800');


-- Data for Name: x_table_column_code_mappings; Type: TABLE DATA; Schema: public; Owner: -



-- Name: acc_accounting_rule_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -

SELECT pg_catalog.setval('public.acc_accounting_rule_id_seq', 1, false);


-- Name: acc_gl_account_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -

SELECT pg_catalog.setval('public.acc_gl_account_id_seq', 1, false);


-- Name: acc_gl_closure_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -

SELECT pg_catalog.setval('public.acc_gl_closure_id_seq', 1, false);


-- Name: acc_gl_financial_activity_account_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -

SELECT pg_catalog.setval('public.acc_gl_financial_activity_account_id_seq', 1, false);


-- Name: acc_gl_journal_entry_annual_summary_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -

SELECT pg_catalog.setval('public.acc_gl_journal_entry_annual_summary_id_seq', 1, false);


-- Name: acc_gl_journal_entry_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -

SELECT pg_catalog.setval('public.acc_gl_journal_entry_id_seq', 1, false);


-- Name: acc_product_mapping_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -

SELECT pg_catalog.setval('public.acc_product_mapping_id_seq', 1, false);


-- Name: acc_rule_tags_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -

SELECT pg_catalog.setval('public.acc_rule_tags_id_seq', 1, false);


-- Name: batch_custom_job_parameters_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -

SELECT pg_catalog.setval('public.batch_custom_job_parameters_id_seq', 1, false);


-- Name: batch_job_execution_seq; Type: SEQUENCE SET; Schema: public; Owner: -

SELECT pg_catalog.setval('public.batch_job_execution_seq', 1, false);


-- Name: batch_job_seq; Type: SEQUENCE SET; Schema: public; Owner: -

SELECT pg_catalog.setval('public.batch_job_seq', 1, false);


-- Name: batch_step_execution_seq; Type: SEQUENCE SET; Schema: public; Owner: -

SELECT pg_catalog.setval('public.batch_step_execution_seq', 1, false);


-- Name: c_account_number_format_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -

SELECT pg_catalog.setval('public.c_account_number_format_id_seq', 1, false);


-- Name: c_cache_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -

SELECT pg_catalog.setval('public.c_cache_id_seq', 2, false);


-- Name: c_configuration_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -

SELECT pg_catalog.setval('public.c_configuration_id_seq', 74, true);


-- Name: c_external_service_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -

SELECT pg_catalog.setval('public.c_external_service_id_seq', 5, false);


-- Name: glim_accounts_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -

SELECT pg_catalog.setval('public.glim_accounts_id_seq', 1, false);


-- Name: gsim_accounts_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -

SELECT pg_catalog.setval('public.gsim_accounts_id_seq', 1, false);


-- Name: interop_identifier_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -

SELECT pg_catalog.setval('public.interop_identifier_id_seq', 1, false);


-- Name: job_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -

SELECT pg_catalog.setval('public.job_id_seq', 41, true);


-- Name: job_parameters_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -

SELECT pg_catalog.setval('public.job_parameters_id_seq', 6, false);


-- Name: job_run_history_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -

SELECT pg_catalog.setval('public.job_run_history_id_seq', 1, false);


-- Name: m_account_transfer_details_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -

SELECT pg_catalog.setval('public.m_account_transfer_details_id_seq', 1, false);


-- Name: m_account_transfer_standing_instructions_history_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -

SELECT pg_catalog.setval('public.m_account_transfer_standing_instructions_history_id_seq', 1, false);


-- Name: m_account_transfer_standing_instructions_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -

SELECT pg_catalog.setval('public.m_account_transfer_standing_instructions_id_seq', 1, false);


-- Name: m_account_transfer_transaction_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -

SELECT pg_catalog.setval('public.m_account_transfer_transaction_id_seq', 1, false);


-- Name: m_address_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -

SELECT pg_catalog.setval('public.m_address_id_seq', 1, false);


-- Name: m_adhoc_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -

SELECT pg_catalog.setval('public.m_adhoc_id_seq', 1, false);


-- Name: m_appuser_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -

SELECT pg_catalog.setval('public.m_appuser_id_seq', 4, false);


-- Name: m_appuser_previous_password_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -

SELECT pg_catalog.setval('public.m_appuser_previous_password_id_seq', 1, false);


-- Name: m_batch_business_steps_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -

SELECT pg_catalog.setval('public.m_batch_business_steps_id_seq', 6, true);


-- Name: m_business_date_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -

SELECT pg_catalog.setval('public.m_business_date_id_seq', 1, false);


-- Name: m_calendar_history_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -

SELECT pg_catalog.setval('public.m_calendar_history_id_seq', 1, false);


-- Name: m_calendar_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -

SELECT pg_catalog.setval('public.m_calendar_id_seq', 1, false);


-- Name: m_calendar_instance_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -

SELECT pg_catalog.setval('public.m_calendar_instance_id_seq', 1, false);


-- Name: m_cashier_transactions_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -

SELECT pg_catalog.setval('public.m_cashier_transactions_id_seq', 1, false);


-- Name: m_cashiers_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -

SELECT pg_catalog.setval('public.m_cashiers_id_seq', 1, false);


-- Name: m_charge_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -

SELECT pg_catalog.setval('public.m_charge_id_seq', 1, false);


-- Name: m_client_address_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -

SELECT pg_catalog.setval('public.m_client_address_id_seq', 1, false);


-- Name: m_client_attendance_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -

SELECT pg_catalog.setval('public.m_client_attendance_id_seq', 1, false);


-- Name: m_client_charge_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -

SELECT pg_catalog.setval('public.m_client_charge_id_seq', 1, false);


-- Name: m_client_charge_paid_by_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -

SELECT pg_catalog.setval('public.m_client_charge_paid_by_id_seq', 1, false);


-- Name: m_client_collateral_management_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -

SELECT pg_catalog.setval('public.m_client_collateral_management_id_seq', 1, false);


-- Name: m_client_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -

SELECT pg_catalog.setval('public.m_client_id_seq', 1, false);


-- Name: m_client_identifier_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -

SELECT pg_catalog.setval('public.m_client_identifier_id_seq', 1, false);


-- Name: m_client_non_person_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -

SELECT pg_catalog.setval('public.m_client_non_person_id_seq', 1, false);


-- Name: m_client_transaction_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -

SELECT pg_catalog.setval('public.m_client_transaction_id_seq', 1, false);


-- Name: m_client_transfer_details_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -

SELECT pg_catalog.setval('public.m_client_transfer_details_id_seq', 1, false);


-- Name: m_code_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -

SELECT pg_catalog.setval('public.m_code_id_seq', 39, true);


-- Name: m_code_value_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -

SELECT pg_catalog.setval('public.m_code_value_id_seq', 15, false);


-- Name: m_collateral_management_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -

SELECT pg_catalog.setval('public.m_collateral_management_id_seq', 1, false);


-- Name: m_creditbureau_configuration_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -

SELECT pg_catalog.setval('public.m_creditbureau_configuration_id_seq', 9, false);


-- Name: m_creditbureau_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -

SELECT pg_catalog.setval('public.m_creditbureau_id_seq', 2, false);


-- Name: m_creditbureau_loanproduct_mapping_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -

SELECT pg_catalog.setval('public.m_creditbureau_loanproduct_mapping_id_seq', 1, false);


-- Name: m_creditbureau_token_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -

SELECT pg_catalog.setval('public.m_creditbureau_token_id_seq', 1, false);


-- Name: m_creditreport_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -

SELECT pg_catalog.setval('public.m_creditreport_id_seq', 1, false);


-- Name: m_currency_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -

SELECT pg_catalog.setval('public.m_currency_id_seq', 165, false);


-- Name: m_delinquency_bucket_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -

SELECT pg_catalog.setval('public.m_delinquency_bucket_id_seq', 1, false);


-- Name: m_delinquency_bucket_mappings_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -

SELECT pg_catalog.setval('public.m_delinquency_bucket_mappings_id_seq', 1, false);


-- Name: m_delinquency_range_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -

SELECT pg_catalog.setval('public.m_delinquency_range_id_seq', 1, false);


-- Name: m_deposit_account_on_hold_transaction_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -

SELECT pg_catalog.setval('public.m_deposit_account_on_hold_transaction_id_seq', 1, false);


-- Name: m_deposit_account_recurring_detail_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -

SELECT pg_catalog.setval('public.m_deposit_account_recurring_detail_id_seq', 1, false);


-- Name: m_deposit_account_term_and_preclosure_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -

SELECT pg_catalog.setval('public.m_deposit_account_term_and_preclosure_id_seq', 1, false);


-- Name: m_deposit_product_recurring_detail_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -

SELECT pg_catalog.setval('public.m_deposit_product_recurring_detail_id_seq', 1, false);


-- Name: m_deposit_product_term_and_preclosure_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -

SELECT pg_catalog.setval('public.m_deposit_product_term_and_preclosure_id_seq', 1, false);


-- Name: m_document_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -

SELECT pg_catalog.setval('public.m_document_id_seq', 1, false);


-- Name: m_entity_datatable_check_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -

SELECT pg_catalog.setval('public.m_entity_datatable_check_id_seq', 1, false);


-- Name: m_entity_relation_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -

SELECT pg_catalog.setval('public.m_entity_relation_id_seq', 6, false);


-- Name: m_entity_to_entity_access_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -

SELECT pg_catalog.setval('public.m_entity_to_entity_access_id_seq', 1, false);


-- Name: m_entity_to_entity_mapping_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -

SELECT pg_catalog.setval('public.m_entity_to_entity_mapping_id_seq', 1, false);


-- Name: m_external_event_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -

SELECT pg_catalog.setval('public.m_external_event_id_seq', 1, false);


-- Name: m_family_members_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -

SELECT pg_catalog.setval('public.m_family_members_id_seq', 1, false);


-- Name: m_field_configuration_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -

SELECT pg_catalog.setval('public.m_field_configuration_id_seq', 19, false);


-- Name: m_floating_rates_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -

SELECT pg_catalog.setval('public.m_floating_rates_id_seq', 1, false);


-- Name: m_floating_rates_periods_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -

SELECT pg_catalog.setval('public.m_floating_rates_periods_id_seq', 1, false);


-- Name: m_fund_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -

SELECT pg_catalog.setval('public.m_fund_id_seq', 1, false);


-- Name: m_group_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -

SELECT pg_catalog.setval('public.m_group_id_seq', 1, false);


-- Name: m_group_level_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -

SELECT pg_catalog.setval('public.m_group_level_id_seq', 3, false);


-- Name: m_group_roles_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -

SELECT pg_catalog.setval('public.m_group_roles_id_seq', 1, false);


-- Name: m_guarantor_funding_details_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -

SELECT pg_catalog.setval('public.m_guarantor_funding_details_id_seq', 1, false);


-- Name: m_guarantor_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -

SELECT pg_catalog.setval('public.m_guarantor_id_seq', 1, false);


-- Name: m_guarantor_transaction_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -

SELECT pg_catalog.setval('public.m_guarantor_transaction_id_seq', 1, false);


-- Name: m_holiday_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -

SELECT pg_catalog.setval('public.m_holiday_id_seq', 1, false);


-- Name: m_hook_configuration_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -

SELECT pg_catalog.setval('public.m_hook_configuration_id_seq', 1, false);


-- Name: m_hook_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -

SELECT pg_catalog.setval('public.m_hook_id_seq', 1, false);


-- Name: m_hook_registered_events_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -

SELECT pg_catalog.setval('public.m_hook_registered_events_id_seq', 1, false);


-- Name: m_hook_schema_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -

SELECT pg_catalog.setval('public.m_hook_schema_id_seq', 12, false);


-- Name: m_hook_templates_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -

SELECT pg_catalog.setval('public.m_hook_templates_id_seq', 5, false);


-- Name: m_image_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -

SELECT pg_catalog.setval('public.m_image_id_seq', 1, false);


-- Name: m_import_document_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -

SELECT pg_catalog.setval('public.m_import_document_id_seq', 1, false);


-- Name: m_interest_incentives_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -

SELECT pg_catalog.setval('public.m_interest_incentives_id_seq', 1, false);


-- Name: m_interest_rate_chart_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -

SELECT pg_catalog.setval('public.m_interest_rate_chart_id_seq', 1, false);


-- Name: m_interest_rate_slab_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -

SELECT pg_catalog.setval('public.m_interest_rate_slab_id_seq', 1, false);


-- Name: m_journal_entry_aggregation_summary_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -

SELECT pg_catalog.setval('public.m_journal_entry_aggregation_summary_id_seq', 1, false);


-- Name: m_journal_entry_aggregation_tracking_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -

SELECT pg_catalog.setval('public.m_journal_entry_aggregation_tracking_id_seq', 1, false);


-- Name: m_loan_amortization_allocation_mapping_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -

SELECT pg_catalog.setval('public.m_loan_amortization_allocation_mapping_id_seq', 1, false);


-- Name: m_loan_approved_amount_history_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -

SELECT pg_catalog.setval('public.m_loan_approved_amount_history_id_seq', 1, false);


-- Name: m_loan_arrears_aging_loan_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -

SELECT pg_catalog.setval('public.m_loan_arrears_aging_loan_id_seq', 1, false);


-- Name: m_loan_buy_down_fee_balance_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -

SELECT pg_catalog.setval('public.m_loan_buy_down_fee_balance_id_seq', 1, false);


-- Name: m_loan_capitalized_income_balance_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -

SELECT pg_catalog.setval('public.m_loan_capitalized_income_balance_id_seq', 1, false);


-- Name: m_loan_charge_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -

SELECT pg_catalog.setval('public.m_loan_charge_id_seq', 1, false);


-- Name: m_loan_charge_paid_by_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -

SELECT pg_catalog.setval('public.m_loan_charge_paid_by_id_seq', 1, false);


-- Name: m_loan_collateral_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -

SELECT pg_catalog.setval('public.m_loan_collateral_id_seq', 1, false);


-- Name: m_loan_collateral_management_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -

SELECT pg_catalog.setval('public.m_loan_collateral_management_id_seq', 1, false);


-- Name: m_loan_delinquency_tag_history_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -

SELECT pg_catalog.setval('public.m_loan_delinquency_tag_history_id_seq', 1, false);


-- Name: m_loan_disbursement_detail_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -

SELECT pg_catalog.setval('public.m_loan_disbursement_detail_id_seq', 1, false);


-- Name: m_loan_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -

SELECT pg_catalog.setval('public.m_loan_id_seq', 1, false);


-- Name: m_loan_installment_charge_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -

SELECT pg_catalog.setval('public.m_loan_installment_charge_id_seq', 1, false);


-- Name: m_loan_interest_recalculation_additional_details_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -

SELECT pg_catalog.setval('public.m_loan_interest_recalculation_additional_details_id_seq', 1, false);


-- Name: m_loan_officer_assignment_history_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -

SELECT pg_catalog.setval('public.m_loan_officer_assignment_history_id_seq', 1, false);


-- Name: m_loan_overdue_installment_charge_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -

SELECT pg_catalog.setval('public.m_loan_overdue_installment_charge_id_seq', 1, false);


-- Name: m_loan_reage_parameter_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -

SELECT pg_catalog.setval('public.m_loan_reage_parameter_id_seq', 1, false);


-- Name: m_loan_recalculation_details_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -

SELECT pg_catalog.setval('public.m_loan_recalculation_details_id_seq', 1, false);


-- Name: m_loan_repayment_schedule_history_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -

SELECT pg_catalog.setval('public.m_loan_repayment_schedule_history_id_seq', 1, false);


-- Name: m_loan_repayment_schedule_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -

SELECT pg_catalog.setval('public.m_loan_repayment_schedule_id_seq', 1, false);


-- Name: m_loan_reschedule_request_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -

SELECT pg_catalog.setval('public.m_loan_reschedule_request_id_seq', 1, false);


-- Name: m_loan_reschedule_request_term_variations_mapping_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -

SELECT pg_catalog.setval('public.m_loan_reschedule_request_term_variations_mapping_id_seq', 1, false);


-- Name: m_loan_term_variations_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -

SELECT pg_catalog.setval('public.m_loan_term_variations_id_seq', 1, false);


-- Name: m_loan_topup_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -

SELECT pg_catalog.setval('public.m_loan_topup_id_seq', 1, false);


-- Name: m_loan_tranche_charges_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -

SELECT pg_catalog.setval('public.m_loan_tranche_charges_id_seq', 1, false);


-- Name: m_loan_tranche_disbursement_charge_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -

SELECT pg_catalog.setval('public.m_loan_tranche_disbursement_charge_id_seq', 1, false);


-- Name: m_loan_transaction_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -

SELECT pg_catalog.setval('public.m_loan_transaction_id_seq', 1, false);


-- Name: m_loan_transaction_relation_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -

SELECT pg_catalog.setval('public.m_loan_transaction_relation_id_seq', 1, false);


-- Name: m_loan_transaction_repayment_schedule_mapping_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -

SELECT pg_catalog.setval('public.m_loan_transaction_repayment_schedule_mapping_id_seq', 1, false);


-- Name: m_loanproduct_provisioning_entry_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -

SELECT pg_catalog.setval('public.m_loanproduct_provisioning_entry_id_seq', 1, false);


-- Name: m_loanproduct_provisioning_mapping_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -

SELECT pg_catalog.setval('public.m_loanproduct_provisioning_mapping_id_seq', 1, false);


-- Name: m_mandatory_savings_schedule_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -

SELECT pg_catalog.setval('public.m_mandatory_savings_schedule_id_seq', 1, false);


-- Name: m_meeting_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -

SELECT pg_catalog.setval('public.m_meeting_id_seq', 1, false);


-- Name: m_note_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -

SELECT pg_catalog.setval('public.m_note_id_seq', 1, false);


-- Name: m_office_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -

SELECT pg_catalog.setval('public.m_office_id_seq', 2, false);


-- Name: m_office_transaction_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -

SELECT pg_catalog.setval('public.m_office_transaction_id_seq', 1, false);


-- Name: m_organisation_creditbureau_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -

SELECT pg_catalog.setval('public.m_organisation_creditbureau_id_seq', 1, false);


-- Name: m_organisation_currency_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -

SELECT pg_catalog.setval('public.m_organisation_currency_id_seq', 22, false);


-- Name: m_password_validation_policy_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -

SELECT pg_catalog.setval('public.m_password_validation_policy_id_seq', 3, true);


-- Name: m_payment_detail_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -

SELECT pg_catalog.setval('public.m_payment_detail_id_seq', 1, false);


-- Name: m_payment_type_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -

SELECT pg_catalog.setval('public.m_payment_type_id_seq', 3, true);


-- Name: m_permission_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -

SELECT pg_catalog.setval('public.m_permission_id_seq', 960, true);


-- Name: m_portfolio_account_associations_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -

SELECT pg_catalog.setval('public.m_portfolio_account_associations_id_seq', 1, false);


-- Name: m_portfolio_command_source_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -

SELECT pg_catalog.setval('public.m_portfolio_command_source_id_seq', 1, false);


-- Name: m_product_loan_configurable_attributes_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -

SELECT pg_catalog.setval('public.m_product_loan_configurable_attributes_id_seq', 1, false);


-- Name: m_product_loan_floating_rates_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -

SELECT pg_catalog.setval('public.m_product_loan_floating_rates_id_seq', 1, false);


-- Name: m_product_loan_guarantee_details_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -

SELECT pg_catalog.setval('public.m_product_loan_guarantee_details_id_seq', 1, false);


-- Name: m_product_loan_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -

SELECT pg_catalog.setval('public.m_product_loan_id_seq', 1, false);


-- Name: m_product_loan_recalculation_details_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -

SELECT pg_catalog.setval('public.m_product_loan_recalculation_details_id_seq', 1, false);


-- Name: m_product_loan_variable_installment_config_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -

SELECT pg_catalog.setval('public.m_product_loan_variable_installment_config_id_seq', 1, false);


-- Name: m_product_loan_variations_borrower_cycle_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -

SELECT pg_catalog.setval('public.m_product_loan_variations_borrower_cycle_id_seq', 1, false);


-- Name: m_product_mix_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -

SELECT pg_catalog.setval('public.m_product_mix_id_seq', 1, false);


-- Name: m_provision_category_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -

SELECT pg_catalog.setval('public.m_provision_category_id_seq', 5, false);


-- Name: m_provisioning_criteria_definition_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -

SELECT pg_catalog.setval('public.m_provisioning_criteria_definition_id_seq', 1, false);


-- Name: m_provisioning_criteria_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -

SELECT pg_catalog.setval('public.m_provisioning_criteria_id_seq', 1, false);


-- Name: m_provisioning_history_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -

SELECT pg_catalog.setval('public.m_provisioning_history_id_seq', 1, false);


-- Name: m_rate_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -

SELECT pg_catalog.setval('public.m_rate_id_seq', 1, false);


-- Name: m_repayment_with_post_dated_checks_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -

SELECT pg_catalog.setval('public.m_repayment_with_post_dated_checks_id_seq', 1, false);


-- Name: m_report_mailing_job_configuration_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -

SELECT pg_catalog.setval('public.m_report_mailing_job_configuration_id_seq', 5, false);


-- Name: m_report_mailing_job_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -

SELECT pg_catalog.setval('public.m_report_mailing_job_id_seq', 1, false);


-- Name: m_report_mailing_job_run_history_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -

SELECT pg_catalog.setval('public.m_report_mailing_job_run_history_id_seq', 1, false);


-- Name: m_role_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -

SELECT pg_catalog.setval('public.m_role_id_seq', 3, false);


-- Name: m_savings_account_charge_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -

SELECT pg_catalog.setval('public.m_savings_account_charge_id_seq', 1, false);


-- Name: m_savings_account_charge_paid_by_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -

SELECT pg_catalog.setval('public.m_savings_account_charge_paid_by_id_seq', 1, false);


-- Name: m_savings_account_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -

SELECT pg_catalog.setval('public.m_savings_account_id_seq', 1, false);


-- Name: m_savings_account_interest_rate_chart_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -

SELECT pg_catalog.setval('public.m_savings_account_interest_rate_chart_id_seq', 1, false);


-- Name: m_savings_account_interest_rate_slab_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -

SELECT pg_catalog.setval('public.m_savings_account_interest_rate_slab_id_seq', 1, false);


-- Name: m_savings_account_transaction_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -

SELECT pg_catalog.setval('public.m_savings_account_transaction_id_seq', 1, false);


-- Name: m_savings_account_transaction_tax_details_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -

SELECT pg_catalog.setval('public.m_savings_account_transaction_tax_details_id_seq', 1, false);


-- Name: m_savings_interest_incentives_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -

SELECT pg_catalog.setval('public.m_savings_interest_incentives_id_seq', 1, false);


-- Name: m_savings_officer_assignment_history_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -

SELECT pg_catalog.setval('public.m_savings_officer_assignment_history_id_seq', 1, false);


-- Name: m_savings_product_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -

SELECT pg_catalog.setval('public.m_savings_product_id_seq', 1, false);


-- Name: m_share_account_charge_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -

SELECT pg_catalog.setval('public.m_share_account_charge_id_seq', 1, false);


-- Name: m_share_account_charge_paid_by_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -

SELECT pg_catalog.setval('public.m_share_account_charge_paid_by_id_seq', 1, false);


-- Name: m_share_account_dividend_details_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -

SELECT pg_catalog.setval('public.m_share_account_dividend_details_id_seq', 1, false);


-- Name: m_share_account_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -

SELECT pg_catalog.setval('public.m_share_account_id_seq', 1, false);


-- Name: m_share_account_transactions_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -

SELECT pg_catalog.setval('public.m_share_account_transactions_id_seq', 1, false);


-- Name: m_share_product_dividend_pay_out_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -

SELECT pg_catalog.setval('public.m_share_product_dividend_pay_out_id_seq', 1, false);


-- Name: m_share_product_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -

SELECT pg_catalog.setval('public.m_share_product_id_seq', 1, false);


-- Name: m_share_product_market_price_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -

SELECT pg_catalog.setval('public.m_share_product_market_price_id_seq', 1, false);


-- Name: m_staff_assignment_history_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -

SELECT pg_catalog.setval('public.m_staff_assignment_history_id_seq', 1, false);


-- Name: m_staff_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -

SELECT pg_catalog.setval('public.m_staff_id_seq', 1, false);


-- Name: m_survey_components_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -

SELECT pg_catalog.setval('public.m_survey_components_id_seq', 1, false);


-- Name: m_survey_lookup_tables_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -

SELECT pg_catalog.setval('public.m_survey_lookup_tables_id_seq', 1, false);


-- Name: m_survey_questions_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -

SELECT pg_catalog.setval('public.m_survey_questions_id_seq', 1, false);


-- Name: m_survey_responses_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -

SELECT pg_catalog.setval('public.m_survey_responses_id_seq', 1, false);


-- Name: m_survey_scorecards_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -

SELECT pg_catalog.setval('public.m_survey_scorecards_id_seq', 1, false);


-- Name: m_surveys_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -

SELECT pg_catalog.setval('public.m_surveys_id_seq', 1, false);


-- Name: m_tax_component_history_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -

SELECT pg_catalog.setval('public.m_tax_component_history_id_seq', 1, false);


-- Name: m_tax_component_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -

SELECT pg_catalog.setval('public.m_tax_component_id_seq', 1, false);


-- Name: m_tax_group_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -

SELECT pg_catalog.setval('public.m_tax_group_id_seq', 1, false);


-- Name: m_tax_group_mappings_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -

SELECT pg_catalog.setval('public.m_tax_group_mappings_id_seq', 1, false);


-- Name: m_tellers_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -

SELECT pg_catalog.setval('public.m_tellers_id_seq', 1, false);


-- Name: m_template_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -

SELECT pg_catalog.setval('public.m_template_id_seq', 1, false);


-- Name: m_templatemappers_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -

SELECT pg_catalog.setval('public.m_templatemappers_id_seq', 1, false);


-- Name: m_working_days_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -

SELECT pg_catalog.setval('public.m_working_days_id_seq', 2, false);


-- Name: mix_taxonomy_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -

SELECT pg_catalog.setval('public.mix_taxonomy_id_seq', 49, false);


-- Name: mix_taxonomy_mapping_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -

SELECT pg_catalog.setval('public.mix_taxonomy_mapping_id_seq', 2, false);


-- Name: mix_xbrl_namespace_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -

SELECT pg_catalog.setval('public.mix_xbrl_namespace_id_seq', 8, false);


-- Name: notification_generator_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -

SELECT pg_catalog.setval('public.notification_generator_id_seq', 1, false);


-- Name: notification_mapper_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -

SELECT pg_catalog.setval('public.notification_mapper_id_seq', 1, false);


-- Name: ppi_likelihoods_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -

SELECT pg_catalog.setval('public.ppi_likelihoods_id_seq', 1, false);


-- Name: ppi_likelihoods_ppi_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -

SELECT pg_catalog.setval('public.ppi_likelihoods_ppi_id_seq', 1, false);


-- Name: ppi_scores_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -

SELECT pg_catalog.setval('public.ppi_scores_id_seq', 21, false);


-- Name: ref_loan_transaction_processing_strategy_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -

SELECT pg_catalog.setval('public.ref_loan_transaction_processing_strategy_id_seq', 8, false);


-- Name: request_audit_table_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -

SELECT pg_catalog.setval('public.request_audit_table_id_seq', 1, false);


-- Name: rpt_sequence_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -

SELECT pg_catalog.setval('public.rpt_sequence_id_seq', 1, false);


-- Name: scheduled_email_campaign_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -

SELECT pg_catalog.setval('public.scheduled_email_campaign_id_seq', 1, false);


-- Name: scheduled_email_configuration_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -

SELECT pg_catalog.setval('public.scheduled_email_configuration_id_seq', 5, false);


-- Name: scheduled_email_messages_outbound_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -

SELECT pg_catalog.setval('public.scheduled_email_messages_outbound_id_seq', 1, false);


-- Name: scheduler_detail_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -

SELECT pg_catalog.setval('public.scheduler_detail_id_seq', 2, false);


-- Name: sms_campaign_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -

SELECT pg_catalog.setval('public.sms_campaign_id_seq', 1, false);


-- Name: sms_messages_outbound_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -

SELECT pg_catalog.setval('public.sms_messages_outbound_id_seq', 1, false);


-- Name: stretchy_parameter_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -

SELECT pg_catalog.setval('public.stretchy_parameter_id_seq', 1023, false);


-- Name: stretchy_report_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -

SELECT pg_catalog.setval('public.stretchy_report_id_seq', 200, true);


-- Name: stretchy_report_parameter_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -

SELECT pg_catalog.setval('public.stretchy_report_parameter_id_seq', 541, true);


-- Name: twofactor_access_token_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -

SELECT pg_catalog.setval('public.twofactor_access_token_id_seq', 1, false);


-- Name: twofactor_configuration_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -

SELECT pg_catalog.setval('public.twofactor_configuration_id_seq', 11, false);


-- PostgreSQL database dump complete
