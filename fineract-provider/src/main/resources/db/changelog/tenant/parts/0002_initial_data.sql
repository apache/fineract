--
-- PostgreSQL database dump
--

-- Dumped from database version 18.3 (Debian 18.3-1.pgdg13+1)
-- Dumped by pg_dump version 18.3 (Debian 18.3-1.pgdg13+1)

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- Data for Name: m_code; Type: TABLE DATA; Schema: public; Owner: postgres
--

SET SESSION AUTHORIZATION DEFAULT;

ALTER TABLE public.m_code 

COPY public.m_code (id, code_name, is_system_defined) FROM stdin;
1	Customer Identifier	t
2	LoanCollateral	t
3	LoanPurpose	t
4	Gender	t
5	YesNo	t
6	GuarantorRelationship	t
7	AssetAccountTags	t
8	LiabilityAccountTags	t
9	EquityAccountTags	t
10	IncomeAccountTags	t
11	ExpenseAccountTags	t
13	GROUPROLE	t
14	ClientClosureReason	t
15	GroupClosureReason	t
16	ClientType	t
17	ClientClassification	t
18	ClientSubStatus	t
19	ClientRejectReason	t
20	ClientWithdrawReason	t
21	Entity to Entity Access Types	t
22	CenterClosureReason	t
23	LoanRescheduleReason	t
24	Constitution	t
25	Main Business Line	t
26	WriteOffReasons	t
27	STATE	t
28	COUNTRY	t
29	ADDRESS_TYPE	t
30	MARITAL STATUS	t
31	RELATIONSHIP	t
32	PROFESSION	t
33	PaymentType	t
34	Customer Documents	t
35	SavingsAccountBlockReasons	t
36	DebitTransactionFreezeReasons	t
37	CreditTransactionFreezeReasons	t
38	SavingsTransactionFreezeReasons	t
39	ChargeOffReasons	t
40	capitalized_income_transaction_classification	t
41	buydown_fee_transaction_classification	t
42	ReAgeReasons	t
43	ReAmortizationReasons	t
44	working_capital_loan_disbursement_classification	t
45	working_capital_loan_repayment_classification	t
46	working_capital_loan_credit_balance_refund_classification	t
47	working_capital_loan_discount_fee_classification	t
48	LoanOriginatorType	t
49	LoanOriginationChannelType	t
\.


ALTER TABLE public.m_code 

--
-- Data for Name: m_code_value; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_code_value 

COPY public.m_code_value (id, code_id, code_value, code_description, order_position, code_score, is_active, is_mandatory) FROM stdin;
1	1	Passport	\N	1	\N	t	f
2	1	Id	\N	2	\N	t	f
3	1	Drivers License	\N	3	\N	t	f
4	1	Any Other Id Type	\N	4	\N	t	f
5	6	Spouse	\N	0	\N	t	f
6	6	Parent	\N	0	\N	t	f
7	6	Sibling	\N	0	\N	t	f
8	6	Business Associate	\N	0	\N	t	f
9	6	Other	\N	0	\N	t	f
10	21	Office Access to Loan Products	\N	0	\N	t	f
11	21	Office Access to Savings Products	\N	0	\N	t	f
12	21	Office Access to Fees/Charges	\N	0	\N	t	f
13	13	Leader	Group Leader Role	1	\N	t	f
14	33	Money Transfer	\N	1	\N	t	f
15	48	MERCHANT	\N	1	\N	t	f
16	48	BROKER	\N	2	\N	t	f
17	48	AFFILIATE	\N	3	\N	t	f
18	48	PLATFORM	\N	4	\N	t	f
19	49	ONLINE	\N	1	\N	t	f
20	49	IN_STORE	\N	2	\N	t	f
21	49	API	\N	3	\N	t	f
22	49	AGGREGATOR	\N	4	\N	t	f
\.


ALTER TABLE public.m_code_value 

--
-- Data for Name: acc_gl_account; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.acc_gl_account 

COPY public.acc_gl_account (id, name, parent_id, hierarchy, gl_code, disabled, manual_journal_entries_allowed, account_usage, classification_enum, tag_id, description) FROM stdin;
\.


ALTER TABLE public.acc_gl_account 

--
-- Data for Name: m_office; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_office 

COPY public.m_office (id, parent_id, hierarchy, external_id, name, opening_date) FROM stdin;
1	\N	.	1	Head Office	2009-01-01
\.


ALTER TABLE public.m_office 

--
-- Data for Name: acc_accounting_rule; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.acc_accounting_rule 

COPY public.acc_accounting_rule (id, name, office_id, debit_account_id, allow_multiple_debits, credit_account_id, allow_multiple_credits, description, system_defined) FROM stdin;
\.


ALTER TABLE public.acc_accounting_rule 

--
-- Data for Name: m_image; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_image 

COPY public.m_image (id, location, storage_type_enum) FROM stdin;
\.


ALTER TABLE public.m_image 

--
-- Data for Name: m_staff; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_staff 

COPY public.m_staff (id, is_loan_officer, office_id, firstname, lastname, display_name, mobile_no, external_id, organisational_role_enum, organisational_role_parent_staff_id, is_active, joining_date, image_id, email_address) FROM stdin;
\.


ALTER TABLE public.m_staff 

--
-- Data for Name: m_appuser; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_appuser 

COPY public.m_appuser (id, is_deleted, office_id, staff_id, username, firstname, lastname, password, email, firsttime_login_remaining, nonexpired, nonlocked, nonexpired_credentials, enabled, last_time_password_updated, password_never_expires, cannot_change_password, password_reset_required, failed_login_attempts, is_login_retries_enabled, temporary_password, temporary_password_expiry_time, is_password_reset_enabled) FROM stdin;
1	f	1	\N	mifos	App	Administrator	{SHA-256}{1}5787039480429368bf94732aacc771cd0a3ea02bcf504ffe1185ab94213bc63a	demomfi@mifos.org	f	t	t	t	t	2026-08-20	f	\N	f	0	f	\N	\N	f
2	f	1	\N	system	system	system	{SHA-256}{2}5787039480429368bf94732aacc771cd0a3ea02bcf504ffe1185ab94213bc63a	demomfi@mifos.org	f	t	t	t	t	2026-08-20	f	\N	f	0	f	\N	\N	f
3	f	1	\N	interopUser	Interop	User	{SHA-256}{3}5787039480429368bf94732aacc771cd0a3ea02bcf504ffe1185ab94213bc63a	email@email.com	f	t	t	t	t	2026-08-20	f	\N	f	0	f	\N	\N	f
\.


ALTER TABLE public.m_appuser 

--
-- Data for Name: acc_gl_closure; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.acc_gl_closure 

COPY public.acc_gl_closure (id, office_id, closing_date, is_deleted, createdby_id, lastmodifiedby_id, created_date, lastmodified_date, comments) FROM stdin;
\.


ALTER TABLE public.acc_gl_closure 

--
-- Data for Name: acc_gl_financial_activity_account; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.acc_gl_financial_activity_account 

COPY public.acc_gl_financial_activity_account (id, gl_account_id, financial_activity_type) FROM stdin;
\.


ALTER TABLE public.acc_gl_financial_activity_account 

--
-- Data for Name: m_group_level; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_group_level 

COPY public.m_group_level (id, parent_id, super_parent, level_name, recursable, can_have_clients) FROM stdin;
1	\N	t	Center	t	f
2	1	f	Group	f	t
\.


ALTER TABLE public.m_group_level 

--
-- Data for Name: m_group; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_group 

COPY public.m_group (id, external_id, status_enum, activation_date, office_id, staff_id, parent_id, level_id, display_name, hierarchy, closure_reason_cv_id, closedon_date, activatedon_userid, submittedon_date, submittedon_userid, closedon_userid, account_no) FROM stdin;
\.


ALTER TABLE public.m_group 

--
-- Data for Name: glim_accounts; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.glim_accounts 

COPY public.glim_accounts (id, group_id, account_number, principal_amount, child_accounts_count, accepting_child, loan_status_id, application_id) FROM stdin;
\.


ALTER TABLE public.glim_accounts 

--
-- Data for Name: gsim_accounts; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.gsim_accounts 

COPY public.gsim_accounts (id, group_id, account_number, parent_deposit, child_accounts_count, accepting_child, savings_status_id, application_id) FROM stdin;
\.


ALTER TABLE public.gsim_accounts 

--
-- Data for Name: m_tax_group; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_tax_group 

COPY public.m_tax_group (id, name, createdby_id, created_date, lastmodifiedby_id, lastmodified_date) FROM stdin;
\.


ALTER TABLE public.m_tax_group 

--
-- Data for Name: m_savings_product; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_savings_product 

COPY public.m_savings_product (id, name, short_name, description, deposit_type_enum, currency_code, currency_digits, currency_multiplesof, nominal_annual_interest_rate, interest_compounding_period_enum, interest_posting_period_enum, interest_calculation_type_enum, interest_calculation_days_in_year_type_enum, min_required_opening_balance, lockin_period_frequency, lockin_period_frequency_enum, accounting_type, withdrawal_fee_amount, withdrawal_fee_type_enum, withdrawal_fee_for_transfer, allow_overdraft, overdraft_limit, nominal_annual_interest_rate_overdraft, min_overdraft_for_interest_calculation, min_required_balance, enforce_min_required_balance, min_balance_for_interest_calculation, withhold_tax, tax_group_id, is_dormancy_tracking_active, days_to_inactive, days_to_dormancy, days_to_escheat, max_allowed_lien_limit, is_lien_allowed) FROM stdin;
\.


ALTER TABLE public.m_savings_product 

--
-- Data for Name: m_client; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_client 

COPY public.m_client (id, account_no, external_id, status_enum, sub_status, activation_date, office_joining_date, office_id, transfer_to_office_id, staff_id, firstname, middlename, lastname, fullname, display_name, mobile_no, is_staff, gender_cv_id, date_of_birth, image_id, closure_reason_cv_id, closedon_date, updated_by, updated_on, submittedon_date, activatedon_userid, closedon_userid, default_savings_product, default_savings_account, client_type_cv_id, client_classification_cv_id, reject_reason_cv_id, rejectedon_date, rejectedon_userid, withdraw_reason_cv_id, withdrawn_on_date, withdraw_on_userid, reactivated_on_date, reactivated_on_userid, legal_form_enum, reopened_on_date, reopened_by_userid, email_address, proposed_transfer_date, created_on_utc, created_by, last_modified_by, last_modified_on_utc) FROM stdin;
\.


ALTER TABLE public.m_client 

--
-- Data for Name: m_client_transaction; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_client_transaction 

COPY public.m_client_transaction (id, client_id, office_id, currency_code, payment_detail_id, is_reversed, external_id, transaction_date, transaction_type_enum, amount, created_date, created_on_utc, created_by, last_modified_by, last_modified_on_utc, submitted_on_date) FROM stdin;
\.


ALTER TABLE public.m_client_transaction 

--
-- Data for Name: m_delinquency_bucket; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_delinquency_bucket 

COPY public.m_delinquency_bucket (id, name, created_by, created_on_utc, version, last_modified_by, last_modified_on_utc, bucket_type) FROM stdin;
\.


ALTER TABLE public.m_delinquency_bucket 

--
-- Data for Name: m_fund; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_fund 

COPY public.m_fund (id, name, external_id) FROM stdin;
\.


ALTER TABLE public.m_fund 

--
-- Data for Name: m_product_loan; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_product_loan 

COPY public.m_product_loan (id, short_name, currency_code, currency_digits, currency_multiplesof, principal_amount, min_principal_amount, max_principal_amount, arrearstolerance_amount, name, description, fund_id, is_linked_to_floating_interest_rates, allow_variabe_installments, nominal_interest_rate_per_period, min_nominal_interest_rate_per_period, max_nominal_interest_rate_per_period, interest_period_frequency_enum, annual_nominal_interest_rate, interest_method_enum, interest_calculated_in_period_enum, allow_partial_period_interest_calcualtion, repay_every, repayment_period_frequency_enum, number_of_repayments, min_number_of_repayments, max_number_of_repayments, grace_on_principal_periods, recurring_moratorium_principal_periods, grace_on_interest_periods, grace_interest_free_periods, amortization_method_enum, accounting_type, loan_transaction_strategy_id, external_id, include_in_borrower_cycle, use_borrower_cycle, start_date, close_date, allow_multiple_disbursals, max_disbursals, max_outstanding_loan_balance, grace_on_arrears_ageing, overdue_days_for_npa, days_in_month_enum, days_in_year_enum, interest_recalculation_enabled, min_days_between_disbursal_and_first_repayment, hold_guarantee_funds, principal_threshold_for_last_installment, account_moves_out_of_npa_only_on_arrears_completion, can_define_fixed_emi_amount, installment_amount_in_multiples_of, can_use_for_topup, sync_expected_with_disbursement_date, is_equal_amortization, fixed_principal_percentage_per_installment, disallow_expected_disbursements, allow_approved_disbursed_amounts_over_applied, over_applied_calculation_type, over_applied_number, delinquency_bucket_id, loan_transaction_strategy_code, loan_transaction_strategy_name, due_days_for_repayment_event, overdue_days_for_repayment_event, enable_down_payment, disbursed_amount_percentage_for_down_payment, enable_installment_level_delinquency, enable_accrual_activity_posting, days_in_year_custom_strategy, enable_income_capitalization, capitalized_income_calculation_type, capitalized_income_strategy, capitalized_income_type, enable_buy_down_fee, buy_down_fee_calculation_type, buy_down_fee_strategy, buy_down_fee_income_type, allow_full_term_for_tranche, enable_auto_repayment_for_down_payment, repayment_start_date_type_enum, loan_schedule_type, loan_schedule_processing_type, fixed_length, supported_interest_refund_types, charge_off_behaviour, interest_recognition_on_disbursement_date, is_merchant_buy_down_fee) FROM stdin;
\.


ALTER TABLE public.m_product_loan 

--
-- Data for Name: m_loan; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_loan 

COPY public.m_loan (id, account_no, external_id, client_id, group_id, glim_id, product_id, fund_id, loan_officer_id, loanpurpose_cv_id, loan_status_id, loan_type_enum, currency_code, currency_digits, currency_multiplesof, principal_amount_proposed, principal_amount, approved_principal, net_disbursal_amount, arrearstolerance_amount, is_floating_interest_rate, interest_rate_differential, nominal_interest_rate_per_period, interest_period_frequency_enum, annual_nominal_interest_rate, interest_method_enum, interest_calculated_in_period_enum, allow_partial_period_interest_calcualtion, term_frequency, term_period_frequency_enum, repay_every, repayment_period_frequency_enum, number_of_repayments, grace_on_principal_periods, recurring_moratorium_principal_periods, grace_on_interest_periods, grace_interest_free_periods, amortization_method_enum, submittedon_date, approvedon_date, approvedon_userid, expected_disbursedon_date, expected_firstrepaymenton_date, interest_calculated_from_date, disbursedon_date, disbursedon_userid, expected_maturedon_date, maturedon_date, closedon_date, closedon_userid, total_charges_due_at_disbursement_derived, principal_disbursed_derived, principal_repaid_derived, principal_writtenoff_derived, principal_outstanding_derived, interest_charged_derived, interest_repaid_derived, interest_waived_derived, interest_writtenoff_derived, interest_outstanding_derived, fee_charges_charged_derived, fee_charges_repaid_derived, fee_charges_waived_derived, fee_charges_writtenoff_derived, fee_charges_outstanding_derived, penalty_charges_charged_derived, penalty_charges_repaid_derived, penalty_charges_waived_derived, penalty_charges_writtenoff_derived, penalty_charges_outstanding_derived, total_expected_repayment_derived, total_repayment_derived, total_expected_costofloan_derived, total_costofloan_derived, total_waived_derived, total_writtenoff_derived, total_outstanding_derived, total_overpaid_derived, rejectedon_date, rejectedon_userid, rescheduledon_date, rescheduledon_userid, withdrawnon_date, withdrawnon_userid, writtenoffon_date, loan_transaction_strategy_id, sync_disbursement_with_meeting, loan_counter, loan_product_counter, fixed_emi_amount, max_outstanding_loan_balance, grace_on_arrears_ageing, is_npa, total_recovered_derived, accrued_till, interest_recalcualated_on, days_in_month_enum, days_in_year_enum, interest_recalculation_enabled, guarantee_amount_derived, create_standing_instruction_at_disbursement, version, writeoff_reason_cv_id, loan_sub_status_id, is_topup, is_equal_amortization, fixed_principal_percentage_per_installment, created_on_utc, created_by, last_modified_by, last_modified_on_utc, principal_adjustments_derived, is_fraud, loan_transaction_strategy_code, loan_transaction_strategy_name, last_closed_business_date, overpaidon_date, is_charged_off, charged_off_on_date, charge_off_reason_cv_id, charged_off_by_userid, enable_down_payment, disbursed_amount_percentage_for_down_payment, enable_installment_level_delinquency, enable_accrual_activity_posting, days_in_year_custom_strategy, enable_income_capitalization, capitalized_income_calculation_type, capitalized_income_strategy, capitalized_income_type, capitalized_income_derived, capitalized_income_adjustment_derived, total_principal_derived, enable_buy_down_fee, buy_down_fee_calculation_type, buy_down_fee_strategy, buy_down_fee_income_type, allow_full_term_for_tranche, repayment_start_date_type_enum, enable_auto_repayment_for_down_payment, loan_schedule_type, loan_schedule_processing_type, fee_adjustments_derived, penalty_adjustments_derived, fixed_length, supported_interest_refund_types, charge_off_behaviour, interest_recognition_on_disbursement_date, installment_amount_in_multiples_of, is_merchant_buy_down_fee) FROM stdin;
\.


ALTER TABLE public.m_loan 

--
-- Data for Name: m_payment_type; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_payment_type 

COPY public.m_payment_type (id, value, description, is_cash_payment, order_position, code_name, is_system_defined) FROM stdin;
1	Money Transfer	Money Transfer	f	1	\N	f
2	Repayment Adjustment Chargeback	Repayment Adjustment Chargeback	f	1	REPAYMENT_ADJUSTMENT_CHARGEBACK	t
3	Repayment Adjustment Refund	Repayment Adjustment Refund	f	1	REPAYMENT_ADJUSTMENT_REFUND	t
\.


ALTER TABLE public.m_payment_type 

--
-- Data for Name: m_payment_detail; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_payment_detail 

COPY public.m_payment_detail (id, payment_type_id, account_number, check_number, receipt_number, bank_number, routing_code) FROM stdin;
\.


ALTER TABLE public.m_payment_detail 

--
-- Data for Name: m_loan_transaction; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_loan_transaction 

COPY public.m_loan_transaction (id, loan_id, office_id, payment_detail_id, is_reversed, external_id, transaction_type_enum, transaction_date, amount, principal_portion_derived, interest_portion_derived, fee_charges_portion_derived, penalty_charges_portion_derived, overpayment_portion_derived, unrecognized_income_portion, outstanding_loan_balance_derived, submitted_on_date, manually_adjusted_or_reversed, created_date, created_by, last_modified_by, created_on_utc, last_modified_on_utc, charge_refund_charge_type, reversal_external_id, reversed_on_date, version, classification_cv_id) FROM stdin;
\.


ALTER TABLE public.m_loan_transaction 

--
-- Data for Name: m_savings_account; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_savings_account 

COPY public.m_savings_account (id, account_no, external_id, client_id, group_id, gsim_id, product_id, field_officer_id, status_enum, sub_status_enum, account_type_enum, deposit_type_enum, submittedon_date, submittedon_userid, approvedon_date, approvedon_userid, rejectedon_date, rejectedon_userid, withdrawnon_date, withdrawnon_userid, activatedon_date, activatedon_userid, closedon_date, closedon_userid, currency_code, currency_digits, currency_multiplesof, nominal_annual_interest_rate, interest_compounding_period_enum, interest_posting_period_enum, interest_calculation_type_enum, interest_calculation_days_in_year_type_enum, min_required_opening_balance, lockin_period_frequency, lockin_period_frequency_enum, withdrawal_fee_for_transfer, allow_overdraft, overdraft_limit, nominal_annual_interest_rate_overdraft, min_overdraft_for_interest_calculation, lockedin_until_date_derived, total_deposits_derived, total_withdrawals_derived, total_withdrawal_fees_derived, total_fees_charge_derived, total_penalty_charge_derived, total_annual_fees_derived, total_interest_earned_derived, total_interest_posted_derived, total_overdraft_interest_derived, total_withhold_tax_derived, account_balance_derived, min_required_balance, enforce_min_required_balance, min_balance_for_interest_calculation, start_interest_calculation_date, on_hold_funds_derived, version, withhold_tax, tax_group_id, last_interest_calculation_date, total_savings_amount_on_hold, interest_posted_till_date, reason_for_block, max_allowed_lien_limit, is_lien_allowed, created_by, last_modified_by, created_on_utc, last_modified_on_utc, accrued_till_date, last_closed_business_date) FROM stdin;
\.


ALTER TABLE public.m_savings_account 

--
-- Data for Name: m_savings_account_transaction; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_savings_account_transaction 

COPY public.m_savings_account_transaction (id, savings_account_id, office_id, payment_detail_id, transaction_type_enum, is_reversed, transaction_date, amount, overdraft_amount_derived, balance_end_date_derived, balance_number_of_days_derived, running_balance_derived, cumulative_balance_derived, created_date, created_by, is_manual, release_id_of_hold_amount, is_loan_disbursement, ref_no, original_transaction_id, is_reversal, reason_for_block, is_lien_transaction, submitted_on_date, last_modified_by, created_on_utc, last_modified_on_utc, external_id) FROM stdin;
\.


ALTER TABLE public.m_savings_account_transaction 

--
-- Data for Name: m_share_product; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_share_product 

COPY public.m_share_product (id, name, short_name, external_id, description, start_date, end_date, currency_code, currency_digits, currency_multiplesof, total_shares, issued_shares, totalsubscribed_shares, unit_price, capital_amount, minimum_client_shares, nominal_client_shares, maximum_client_shares, minimum_active_period_frequency, minimum_active_period_frequency_enum, lockin_period_frequency, lockin_period_frequency_enum, allow_dividends_inactive_clients, createdby_id, created_date, lastmodifiedby_id, lastmodified_date, accounting_type) FROM stdin;
\.


ALTER TABLE public.m_share_product 

--
-- Data for Name: m_share_account; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_share_account 

COPY public.m_share_account (id, account_no, product_id, client_id, external_id, status_enum, total_approved_shares, total_pending_shares, submitted_date, submitted_userid, approved_date, approved_userid, rejected_date, rejected_userid, activated_date, activated_userid, closed_date, closed_userid, currency_code, currency_digits, currency_multiplesof, savings_account_id, minimum_active_period_frequency, minimum_active_period_frequency_enum, lockin_period_frequency, lockin_period_frequency_enum, allow_dividends_inactive_clients, created_date, lastmodifiedby_id, lastmodified_date) FROM stdin;
\.


ALTER TABLE public.m_share_account 

--
-- Data for Name: m_share_account_transactions; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_share_account_transactions 

COPY public.m_share_account_transactions (id, account_id, transaction_date, total_shares, unit_price, amount, charge_amount, amount_paid, status_enum, type_enum, is_active) FROM stdin;
\.


ALTER TABLE public.m_share_account_transactions 

--
-- Data for Name: acc_gl_journal_entry; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.acc_gl_journal_entry 

COPY public.acc_gl_journal_entry (id, account_id, office_id, reversal_id, currency_code, transaction_id, loan_transaction_id, savings_transaction_id, client_transaction_id, reversed, ref_num, manual_entry, entry_date, type_enum, amount, description, entity_type_enum, entity_id, created_by, last_modified_by, created_date, lastmodified_date, is_running_balance_calculated, office_running_balance, organization_running_balance, payment_details_id, share_transaction_id, transaction_date, created_on_utc, last_modified_on_utc, submitted_on_date) FROM stdin;
\.


ALTER TABLE public.acc_gl_journal_entry 

--
-- Data for Name: acc_gl_journal_entry_annual_summary; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.acc_gl_journal_entry_annual_summary 

COPY public.acc_gl_journal_entry_annual_summary (id, gl_code, product_id, office_id, opening_balance_amount, currency_code, owner_external_id, manual_entry, year_end_date, created_by, created_on_utc, last_modified_by, last_modified_on_utc, originator_external_ids) FROM stdin;
\.


ALTER TABLE public.acc_gl_journal_entry_annual_summary 

--
-- Data for Name: m_charge; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_charge 

COPY public.m_charge (id, name, currency_code, charge_applies_to_enum, charge_time_enum, charge_calculation_enum, charge_payment_mode_enum, amount, fee_on_day, fee_interval, fee_on_month, is_penalty, is_active, is_deleted, min_cap, max_cap, fee_frequency, is_free_withdrawal, free_withdrawal_charge_frequency, restart_frequency, restart_frequency_enum, is_payment_type, payment_type_id, income_or_liability_account_id, tax_group_id) FROM stdin;
\.


ALTER TABLE public.m_charge 

--
-- Data for Name: acc_product_mapping; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.acc_product_mapping 

COPY public.acc_product_mapping (id, gl_account_id, product_id, product_type, payment_type, charge_id, financial_account_type, charge_off_reason_id, capitalized_income_classification_id, buydown_fee_classification_id, write_off_reason_id) FROM stdin;
\.


ALTER TABLE public.acc_product_mapping 

--
-- Data for Name: acc_rule_tags; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.acc_rule_tags 

COPY public.acc_rule_tags (id, acc_rule_id, tag_id, acc_type_enum) FROM stdin;
\.


ALTER TABLE public.acc_rule_tags 

--
-- Data for Name: batch_custom_job_parameters; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.batch_custom_job_parameters 

COPY public.batch_custom_job_parameters (id, parameter_json) FROM stdin;
\.


ALTER TABLE public.batch_custom_job_parameters 

--
-- Data for Name: batch_job_instance; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.batch_job_instance 

COPY public.batch_job_instance (job_instance_id, version, job_name, job_key) FROM stdin;
1	0	SEND_ASYNCHRONOUS_EVENTS	947cce338b790a4bb6cf8425e98bcf94
2	0	EXECUTE_EMAIL	947cce338b790a4bb6cf8425e98bcf94
3	0	SEND_ASYNCHRONOUS_EVENTS	873184ac21ff8558bbd5b28473daefc6
\.


ALTER TABLE public.batch_job_instance 

--
-- Data for Name: batch_job_execution; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.batch_job_execution 

COPY public.batch_job_execution (job_execution_id, version, job_instance_id, create_time, start_time, end_time, status, exit_code, exit_message, last_updated) FROM stdin;
1	2	1	2026-08-20 15:19:00.164245	2026-08-20 15:19:00.218914	2026-08-20 15:19:00.257215	COMPLETED	COMPLETED		2026-08-20 15:19:00.257256
2	2	2	2026-08-20 15:20:00.126029	2026-08-20 15:20:00.133665	2026-08-20 15:20:00.176769	COMPLETED	COMPLETED		2026-08-20 15:20:00.176811
3	2	3	2026-08-20 15:20:00.166068	2026-08-20 15:20:00.171064	2026-08-20 15:20:00.191046	COMPLETED	COMPLETED		2026-08-20 15:20:00.191083
\.


ALTER TABLE public.batch_job_execution 

--
-- Data for Name: batch_job_execution_context; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.batch_job_execution_context 

COPY public.batch_job_execution_context (job_execution_id, short_context, serialized_context) FROM stdin;
1	{"@class":"java.util.HashMap","batch.version":"5.2.6"}	\N
2	{"@class":"java.util.HashMap","batch.version":"5.2.6"}	\N
3	{"@class":"java.util.HashMap","batch.version":"5.2.6"}	\N
\.


ALTER TABLE public.batch_job_execution_context 

--
-- Data for Name: batch_job_execution_params; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.batch_job_execution_params 

COPY public.batch_job_execution_params (job_execution_id, parameter_type, parameter_name, parameter_value, identifying) FROM stdin;
1	java.lang.Long	run.id	1	Y
2	java.lang.Long	run.id	1	Y
3	java.lang.Long	run.id	2	Y
\.


ALTER TABLE public.batch_job_execution_params 

--
-- Data for Name: batch_step_execution; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.batch_step_execution 

COPY public.batch_step_execution (step_execution_id, version, step_name, job_execution_id, start_time, end_time, status, commit_count, read_count, filter_count, write_count, read_skip_count, write_skip_count, process_skip_count, rollback_count, exit_code, exit_message, last_updated, create_time) FROM stdin;
1	3	SEND_ASYNCHRONOUS_EVENTS_STEP	1	2026-08-20 15:19:00.234203	2026-08-20 15:19:00.251579	COMPLETED	1	0	0	0	0	0	0	0	COMPLETED		2026-08-20 15:19:00.253753	2026-08-20 15:19:00.228589
2	3	EXECUTE_EMAIL	2	2026-08-20 15:20:00.144692	2026-08-20 15:20:00.171408	COMPLETED	1	0	0	0	0	0	0	0	COMPLETED		2026-08-20 15:20:00.173227	2026-08-20 15:20:00.140256
3	3	SEND_ASYNCHRONOUS_EVENTS_STEP	3	2026-08-20 15:20:00.178486	2026-08-20 15:20:00.187318	COMPLETED	1	0	0	0	0	0	0	0	COMPLETED		2026-08-20 15:20:00.188628	2026-08-20 15:20:00.175896
\.


ALTER TABLE public.batch_step_execution 

--
-- Data for Name: batch_step_execution_context; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.batch_step_execution_context 

COPY public.batch_step_execution_context (step_execution_id, short_context, serialized_context) FROM stdin;
1	{"@class":"java.util.HashMap","batch.taskletType":"org.apache.fineract.infrastructure.event.external.jobs.SendAsynchronousEventsTasklet","batch.version":"5.2.6","batch.stepType":"org.springframework.batch.core.step.tasklet.TaskletStep"}	\N
2	{"@class":"java.util.HashMap","batch.taskletType":"org.apache.fineract.infrastructure.campaigns.jobs.executeemail.ExecuteEmailTasklet","batch.version":"5.2.6","batch.stepType":"org.springframework.batch.core.step.tasklet.TaskletStep"}	\N
3	{"@class":"java.util.HashMap","batch.taskletType":"org.apache.fineract.infrastructure.event.external.jobs.SendAsynchronousEventsTasklet","batch.version":"5.2.6","batch.stepType":"org.springframework.batch.core.step.tasklet.TaskletStep"}	\N
\.


ALTER TABLE public.batch_step_execution_context 

--
-- Data for Name: c_account_number_format; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.c_account_number_format 

COPY public.c_account_number_format (id, account_type_enum, prefix_type_enum, prefix_character) FROM stdin;
\.


ALTER TABLE public.c_account_number_format 

--
-- Data for Name: c_cache; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.c_cache 

COPY public.c_cache (id, cache_type_enum) FROM stdin;
1	1
\.


ALTER TABLE public.c_cache 

--
-- Data for Name: c_configuration; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.c_configuration 

COPY public.c_configuration (id, name, value, date_value, string_value, enabled, is_trap_door, description) FROM stdin;
1	maker-checker	\N	\N	\N	f	f	\N
4	amazon-s3	\N	\N	\N	f	f	\N
5	reschedule-future-repayments	\N	\N	\N	t	f	\N
6	reschedule-repayments-on-holidays	\N	\N	\N	f	f	\N
7	allow-transactions-on-holiday	\N	\N	\N	f	f	\N
8	allow-transactions-on-non-workingday	\N	\N	\N	f	f	\N
9	constraint-approach-for-datatables	\N	\N	\N	f	f	\N
10	penalty-wait-period	2	\N	\N	t	f	\N
11	force-password-reset-days	0	\N	\N	f	f	\N
12	grace-on-penalty-posting	0	\N	\N	t	f	\N
15	savings-interest-posting-current-period-end	\N	\N	\N	f	f	Recommended to be changed only once during start of production. When set as false(default), interest will be posted on the first date of next period. If set as true, interest will be posted on last date of current period. There is no difference in the interest amount posted.
16	financial-year-beginning-month	1	\N	\N	t	f	Recommended to be changed only once during start of production. Allowed values 1 - 12 (January - December). Interest posting periods are evaluated based on this configuration.
17	min-clients-in-group	5	\N	\N	f	f	Minimum number of Clients that a Group should have
18	max-clients-in-group	5	\N	\N	f	f	Maximum number of Clients that a Group can have
19	meetings-mandatory-for-jlg-loans	\N	\N	\N	f	f	Enforces all JLG loans to follow a meeting schedule belonging to parent group or Center
20	office-specific-products-enabled	0	\N	\N	f	f	Whether products and fees should be office specific or not? This property should NOT be changed once Mifos is Live.
21	restrict-products-to-user-office	0	\N	\N	f	f	This should be enabled only if, products & fees are office specific (i.e. office-specific-products-enabled is enabled). This property specifies if the products should be auto-restricted to office of the user who created the proudct? Note: This property should NOT be changed once Mifos is Live.
22	office-opening-balances-contra-account	0	\N	\N	t	f	\N
23	rounding-mode	6	\N	\N	t	t	0 - UP, 1 - DOWN, 2- CEILING, 3- FLOOR, 4- HALF_UP, 5- HALF_DOWN, 6 - HALF_EVEN
24	backdate-penalties-enabled	0	\N	\N	t	f	If this parameter is disabled penalties will only be added to instalments due moving forward, any old overdue instalments will not be affected.
25	organisation-start-date	0	\N	\N	f	f	\N
26	paymenttype-applicable-for-disbursement-charges	\N	\N	\N	f	f	Is the Disbursement Entry need to be considering the fund source of the paymnet type
27	interest-charged-from-date-same-as-disbursal-date	0	\N	\N	f	f	\N
28	skip-repayment-on-first-day-of-month	14	\N	\N	f	f	skipping repayment on first day of month
29	change-emi-if-repaymentdate-same-as-disbursementdate	0	\N	\N	t	f	In tranche loans, if repayment date is same as tranche disbursement date then allow to change the emi amount
30	daily-tpt-limit	0	\N	\N	f	f	Daily limit for third party transfers
31	enable-address	\N	\N	\N	f	f	\N
32	sub-rates	0	\N	\N	f	f	Enable Rates Module
33	loan-reschedule-is-first-payday-allowed-on-holiday	0	\N	\N	f	f	If enabled, while loan reschedule the first repayment date can be on a holiday/non working day
35	account-mapping-for-payment-type	\N	\N	Asset	t	f	Asset: default for asset, Use comma seperated values for Liability, Asset and Expense accounts
36	account-mapping-for-charge	\N	\N	Income	t	f	Income: default for Income, Use comma seperated values for Income, Liability and Expense accounts
37	fixed-deposit-transfer-interest-next-day-for-period-end-posting	\N	\N	\N	f	f	Transfer fixed transfer interest next day(t+1) for period end posting
38	allow-backdated-transaction-before-interest-posting	0	\N	\N	t	f	Avoid retrieving all transactions in a savings account
39	allow-backdated-transaction-before-interest-posting-date-for-days	0	\N	\N	f	f	One time configuration to relax the backdated transactions
40	custom-account-number-length	\N	\N	\N	f	f	if enabled, the value if this configuration will set accounnumber length
41	random-account-number	\N	\N	\N	f	f	if enabled, the client accounts, saving accounts, loan accounts will be created with Random Account Number
42	is-interest-to-be-recovered-first-when-greater-than-emi	0	\N	\N	f	f	If enabled, when interest amount is greater than EMI, the additional interest is recovered first before principal
43	is-principal-compounding-disabled-for-overdue-loans	0	\N	\N	f	f	If enabled, it donot consider principal of an unpaid installment for calculating interest of next installment. this is for testing back-dated loan schedule
44	enable-business-date	\N	\N	\N	f	f	Whether the logical business date functionality is enabled in the system
45	enable-automatic-cob-date-adjustment	\N	\N	\N	t	f	Whether the cob date will be automatically recalculated based on the business date
46	enable-post-reversal-txns-for-reverse-transactions	\N	\N	\N	f	f	\N
47	purge-external-events-older-than-days	30	\N	\N	f	f	Number of days criteria to purge old external events sent to message channel
48	days-before-repayment-is-due	1	\N	\N	f	f	Number of days before repayment is due to raise event
49	days-after-repayment-is-overdue	1	\N	\N	f	f	Number of days after repayment overdue to raise event
50	enable-auto-generated-external-id	\N	\N	\N	f	f	\N
51	purge-processed-commands-older-than-days	30	\N	\N	f	f	Number of days criteria to purge old processed commands
52	enable-cob-bulk-event	\N	\N	\N	f	f	Whether bulk event for COB is enabled in the system
53	external-event-batch-size	1000	\N	\N	f	f	External event producer batch size
54	report-export-s3-folder-name	\N	\N	reports	t	f	\N
55	loan-arrears-delinquency-display-data	0	\N	\N	t	f	0 - Both, 1 - Arrears, 2- Delinquency
56	charge-accrual-date	\N	\N	due-date	t	f	due-date: default for due-date, Use comma seperated values for due-date, submitted-date
57	asset-externalization-of-non-active-loans	\N	\N	\N	t	f	If enabled: when a loan state is changed to non-active -> pending transfers will be handled
58	enable-same-maker-checker	\N	\N	\N	f	f	\N
59	next-payment-due-date	\N	\N	earliest-unpaid-date	t	f	earliest-unpaid-date: default for next-payment-due-date, Use earliest-unpaid-date or next-unpaid-due-date
60	enable-payment-hub-integration	0	\N	enable payment hub integration	f	f	Use payment hub api's for account withdrawal and loan disbursement to linked interop account
61	enable-immediate-charge-accrual-post-maturity	\N	\N	\N	f	f	Whether the system creates accruals immediately for charge creation after the maturity date
62	password-reuse-check-history-count	3	\N	\N	f	f	When enabled, prevents password reuse. The value specifies how many previous passwords to check (e.g., 3 = last 3 passwords). Set to 0 to check ALL previous passwords. Disable this setting to allow password reuse.
63	force-password-reset-on-first-login	0	\N	\N	f	f	If enabled, users must reset their password upon first login or after an admin reset. Value is unused.
64	allow-force-withdrawal-on-savings-account	0	\N	\N	f	f	If enabled, allows withdrawals to put the account into negative balance up to the configured limit.
65	force-withdrawal-on-savings-account-limit	0	\N	\N	f	f	The maximum negative balance allowed when force withdrawal is enabled.
66	max-login-retry-attempts	5	\N	\N	f	f	Maximum number of failed login attempts before an account is locked
67	allow-cash-and-non-cash-accrual	0	\N	\N	t	f	When enabled (true), accrual transactions are created at disbursement for None, Cash, and Upfront Accrual accounting types (legacy behavior). When disabled (false), accrual transactions at disbursement are created only for Upfront Accrual accounting type.
68	block-transactions-on-closed-overpaid-loans	\N	\N	\N	f	f	If enabled: monetary transactions are blocked on closed and overpaid loan accounts
69	last-day-of-financial-year	31	\N	\N	t	f	31: default for last day of fiscal year
70	last-month-of-financial-year	12	\N	\N	t	f	12: default for last month of fiscal year
71	income-expense-gl-accounts	\N	\N		t	f	List of income/expense GL account codes for retained earning calculation. Can provide multiple accounts separated by comma or ranges separated by dashes (e.g. 4000,5000-5999,7000). Empty by default, please set before executing job
72	retained-gl-account	\N	\N		t	f	Default retained gl account code. Empty by default, please set before executing job
73	office-id	1	\N	\N	t	f	OfficeId for which the report and the retained earning job will be executed
74	retained-earning-used-by-report-name	\N	\N	Trial Balance Summary Report with Asset Owner	t	f	Report name which will be using using retained earning
75	outstanding-interest-calculation-strategy-for-external-asset-transfer	\N	\N	TOTAL_OUTSTANDING_INTEREST	t	f	 Available options: TOTAL_OUTSTANDING_INTEREST, PAYABLE_OUTSTANDING_INTEREST. Define whether total outstanding interest (due + not yet due + projected) or payable till transfer date ( due + not yet due) should be involved in the asset transfer.
76	allowed-loan-statuses-for-external-asset-transfer	\N	\N	ACTIVE,TRANSFER_IN_PROGRESS,TRANSFER_ON_HOLD	t	f	 Available options: Any combination from LoanStatus enum separated by comma.
77	allowed-loan-statuses-of-delayed-settlement-for-external-asset-transfer	\N	\N	ACTIVE,TRANSFER_IN_PROGRESS,TRANSFER_ON_HOLD,OVERPAID,CLOSED_OBLIGATIONS_MET	t	f	 Available options: Any combination from LoanStatus enum separated by comma.
78	enable-instant-delinquency-calculation	\N	\N	\N	t	f	When enabled, allows delinquency evaluation right after monetary transaction processing
79	enable-originator-creation-during-loan-application	\N	\N	\N	f	f	When enabled, allows creating new loan originators on-the-fly during loan application if the provided externalId does not exist in the registry
\.


ALTER TABLE public.c_configuration 

--
-- Data for Name: c_external_service; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.c_external_service 

COPY public.c_external_service (id, name) FROM stdin;
3	MESSAGE_GATEWAY
4	NOTIFICATION
1	S3
2	SMTP_Email_Account
\.


ALTER TABLE public.c_external_service 

--
-- Data for Name: c_external_service_properties; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.c_external_service_properties 

COPY public.c_external_service_properties (name, value, external_service_id) FROM stdin;
s3_access_key	\N	1
s3_bucket_name	\N	1
s3_secret_key	\N	1
username	support@cloudmicrofinance.com	2
password	support81	2
host	localhost	2
port	3025	2
useTLS	true	2
host_name	localhost	3
port_number	9191	3
end_point	/	3
tenant_app_key	\N	3
server_key	AAAAToBmqQQ:APA91bEodkE12CwFl8VHqanUbeJYg1E05TiheVz59CZZYrBnCq3uM40UYhHfdP-JfeTQ0L0zoLqS8orjvW_ze0_VF8DSuyyqkrDibflhtUainsI0lwgVz5u1YP3q3c3erqjlySEuRShS	4
gcm_end_point	https://gcm-http.googleapis.com/gcm/send	4
fcm_end_point	https://fcm.googleapis.com/fcm/send	4
fromEmail	support@cloudmicrofinance.com	2
fromName	support@cloudmicrofinance.com	2
\.


ALTER TABLE public.c_external_service_properties 

--
-- Data for Name: interop_identifier; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.interop_identifier 

COPY public.interop_identifier (id, account_id, type, a_value, sub_value_or_type, created_by, created_on, modified_by, modified_on) FROM stdin;
\.


ALTER TABLE public.interop_identifier 

--
-- Data for Name: job; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.job 

COPY public.job (id, name, display_name, cron_expression, create_time, task_priority, group_name, previous_run_start_time, next_run_time, job_key, initializing_errorlog, is_active, currently_running, updates_allowed, scheduler_group, is_misfired, node_id, is_mismatched_job, short_name) FROM stdin;
2	Update Loan Arrears Ageing	Update Loan Arrears Ageing	0 1 0 1/1 * ? *	2026-08-20 15:18:01.996492	5	\N	\N	2026-08-21 00:01:00	Update Loan Arrears AgeingJobDetail1 _ DEFAULT	\N	t	f	t	0	f	1	t	LA_ARAG
5	Apply Holidays To Loans	Apply Holidays To Loans	0 0 12 * * ?	2026-08-20 15:18:01.996492	5	\N	\N	2026-08-21 12:00:00	Apply Holidays To LoansJobDetail1 _ DEFAULT	\N	t	f	t	0	f	1	t	LA_AHOL
7	Transfer Fee For Loans From Savings	Transfer Fee For Loans From Savings	0 1 0 1/1 * ? *	2026-08-20 15:18:01.996492	5	\N	\N	2026-08-21 00:01:00	Transfer Fee For Loans From SavingsJobDetail1 _ DEFAULT	\N	t	f	t	0	f	1	t	LA_TFFS
39	Accrual Activity Posting	Accrual Activity Posting	0 0 1 * * ?	2026-08-20 15:18:08.979564	5	\N	\N	\N	Accrual Activity PostingJobDetail1 _ DEFAULT	\N	f	f	t	0	f	1	t	ACC_ACPO
32	Increase Business Date by 1 day	Increase Business Date by 1 day	0 0 0 1/1 * ? *	2026-08-20 15:18:06.319526	99	\N	\N	\N	Increase Business Date by 1 dayJobDetail1 _ DEFAULT	\N	f	f	t	0	f	1	f	BDT_INC1
33	Increase COB Date by 1 day	Increase COB Date by 1 day	0 0 0 1/1 * ? *	2026-08-20 15:18:06.322349	98	\N	\N	\N	Increase COB Date by 1 dayJobDetail1 _ DEFAULT	\N	f	f	t	0	f	1	f	BDT_COB1
43	Working Capital Loan COB	Working Capital Loan COB	0 0 0 * * ?	2026-08-20 15:18:14.359961	5	\N	\N	\N	Working Capital Loan COBJobDetail1 _ DEFAULT	\N	f	f	t	0	f	1	f	WC_COB
12	Apply penalty to overdue loans	Apply penalty to overdue loans	0 0 0 1/1 * ? *	2026-08-20 15:18:01.996492	5	\N	\N	2026-08-21 00:00:00	Apply penalty to overdue loansJobDetail1 _ DEFAULT	\N	t	f	t	0	f	1	t	LA_OPEN
13	Update Non Performing Assets	Update Non Performing Assets	0 0 0 1/1 * ? *	2026-08-20 15:18:01.996492	6	\N	\N	2026-08-21 00:00:00	Update Non Performing AssetsJobDetail1 _ DEFAULT	\N	t	f	t	3	f	1	t	LA_UNPA
17	Recalculate Interest For Loans	Recalculate Interest For Loans	0 1 0 1/1 * ? *	2026-08-20 15:18:01.996492	4	\N	\N	2026-08-21 00:01:00	Recalculate Interest For LoansJobDetail1 _ DEFAULT	\N	t	f	t	3	f	1	t	LA_RINT
19	Generate Loan Loss Provisioning	Generate Loan Loss Provisioning	0 0 0 1/1 * ? *	2026-08-20 15:18:01.996492	5	\N	\N	2026-08-21 00:00:00	Generate Loan Loss ProvisioningJobDetail1 _ DEFAULT	\N	t	f	t	0	f	1	t	LA_GLPR
22	Add Accrual Transactions For Loans With Income Posted As Transactions	Add Accrual Transactions For Loans With Income Posted As Transactions	0 1 0 1/1 * ? *	2026-08-20 15:18:01.996492	5	\N	\N	2026-08-21 00:01:00	Add Accrual Transactions For Loans With Income Posted As TransactionsJobDetail1 _ DEFAULT	\N	t	f	t	3	f	1	t	LA_AATR
34	Loan COB	Loan COB	0 0 0 * * ?	2026-08-20 15:18:06.711613	5	\N	\N	\N	Loan COBJobDetail1 _ DEFAULT	\N	f	f	t	0	f	1	f	LA_ECOB
35	Loan Delinquency Classification	Loan Delinquency Classification	0 0 22 1/1 * ? *	2026-08-20 15:18:06.737346	5	\N	\N	2026-08-20 22:00:00	Loan Delinquency ClassificationJobDetail1 _ DEFAULT	\N	t	f	t	0	f	1	t	LA_DECL
4	Apply Annual Fee For Savings	Apply Annual Fee For Savings	0 20 22 1/1 * ? *	2026-08-20 15:18:01.996492	5	\N	\N	2026-08-20 22:20:00	Apply Annual Fee For SavingsJobDetail1 _ DEFAULT	\N	t	f	t	0	f	1	t	SA_AANF
6	Post Interest For Savings	Post Interest For Savings	0 0 0 1/1 * ? *	2026-08-20 15:18:01.996492	5	\N	\N	2026-08-21 00:00:00	Post Interest For SavingsJobDetail1 _ DEFAULT	\N	t	f	t	1	f	1	t	SA_PINT
8	Pay Due Savings Charges	Pay Due Savings Charges	0 0 12 * * ?	2013-09-23 00:00:00	5	\N	\N	2026-08-21 12:00:00	Pay Due Savings ChargesJobDetail1 _ DEFAULT	\N	t	f	t	0	f	1	t	SA_PDCH
14	Transfer Interest To Savings	Transfer Interest To Savings	0 2 0 1/1 * ? *	2026-08-20 15:18:01.996492	4	\N	\N	2026-08-21 00:02:00	Transfer Interest To SavingsJobDetail1 _ DEFAULT	\N	t	f	t	1	f	1	t	SA_TINT
15	Update Deposit Accounts Maturity details	Update Deposit Accounts Maturity details	0 0 0 1/1 * ? *	2026-08-20 15:18:01.996492	5	\N	\N	2026-08-21 00:00:00	Update Deposit Accounts Maturity detailsJobDetail1 _ DEFAULT	\N	t	f	t	0	f	1	t	SA_MATD
18	Generate Mandatory Savings Schedule	Generate Mandatory Savings Schedule	0 5 0 1/1 * ? *	2026-08-20 15:18:01.996492	5	\N	\N	2026-08-21 00:05:00	Generate Mandatory Savings ScheduleJobDetail1 _ DEFAULT	\N	t	f	t	0	f	1	t	SA_GSCH
21	Update Savings Dormant Accounts	Update Savings Dormant Accounts	0 0 0 1/1 * ? *	2026-08-20 15:18:01.996492	3	\N	\N	2026-08-21 00:00:00	Update Savings Dormant AccountsJobDetail1 _ DEFAULT	\N	t	f	t	1	f	1	t	SA_UDOR
20	Post Dividends For Shares	Post Dividends For Shares	0 0 0 1/1 * ? *	2026-08-20 15:18:01.996492	5	\N	\N	2026-08-21 00:00:00	Post Dividends For SharesJobDetail1 _ DEFAULT	\N	t	f	t	0	f	1	t	SH_PDIV
11	Add Accrual Transactions	Add Accrual Transactions	0 1 0 1/1 * ? *	2026-08-20 15:18:01.996492	3	\N	\N	2026-08-21 00:01:00	Add Accrual TransactionsJobDetail1 _ DEFAULT	\N	t	f	t	3	f	1	t	ACC_AATR
16	Add Periodic Accrual Transactions	Add Periodic Accrual Transactions	0 2 0 1/1 * ? *	2026-08-20 15:18:01.996492	2	\N	\N	2026-08-21 00:02:00	Add Periodic Accrual TransactionsJobDetail1 _ DEFAULT	\N	t	f	t	3	f	1	t	ACC_APTR
10	Execute Standing Instruction	Execute Standing Instruction	0 0 0 1/1 * ? *	2026-08-20 15:18:01.996492	5	\N	\N	2026-08-21 00:00:00	Execute Standing InstructionJobDetail1 _ DEFAULT	\N	t	f	t	0	f	1	t	STI_EXEC
23	Execute Report Mailing Jobs	Execute Report Mailing Jobs	0 0/15 * * * ?	2026-08-20 15:18:01.996492	5	\N	\N	2026-08-20 15:30:00	Execute Report Mailing JobsJobDetail1 _ DEFAULT	\N	t	f	t	0	f	1	t	RMJ_EXEC
27	Execute Email	Execute Email	0 0/10 * * * ?	2026-08-20 15:18:01.996492	5	\N	2026-08-20 15:20:00.008	2026-08-20 15:30:00	Execute EmailJobDetail1 _ DEFAULT	\N	t	f	t	0	f	1	f	EM_EXEC
28	Update Email Outbound with campaign message	Update Email Outbound with campaign message	0 0/15 * * * ?	2026-08-20 15:18:01.996492	5	\N	\N	2026-08-20 15:30:00	Update Email Outbound with campaign messageJobDetail1 _ DEFAULT	\N	t	f	t	0	f	1	t	EM_UOUT
24	Update SMS Outbound with Campaign Message	Update SMS Outbound with Campaign Message	0 0 5 1/1 * ? *	2026-08-20 15:18:01.996492	3	\N	\N	2026-08-21 05:00:00	Update SMS Outbound with Campaign MessageJobDetail1 _ DEFAULT	\N	t	f	t	4	f	1	t	SMS_UOUT
25	Send Messages to SMS Gateway	Send Messages to SMS Gateway	0 0 5 1/1 * ? *	2026-08-20 15:18:01.996492	2	\N	\N	2026-08-21 05:00:00	Send Messages to SMS GatewayJobDetail1 _ DEFAULT	\N	t	f	t	4	f	1	t	SMS_SMSG
26	Get Delivery Reports from SMS Gateway	Get Delivery Reports from SMS Gateway	0 0 5 1/1 * ? *	2026-08-20 15:18:01.996492	1	\N	\N	2026-08-21 05:00:00	Get Delivery Reports from SMS GatewayJobDetail1 _ DEFAULT	\N	t	f	t	4	f	1	t	SMS_DRPT
29	Generate AdhocClient Schedule	Generate AdhocClient Schedule	0 0 12 1/1 * ? *	2026-08-20 15:18:01.996492	5	\N	\N	2026-08-21 12:00:00	Generate AdhocClient ScheduleJobDetail1 _ DEFAULT	\N	t	f	t	0	f	1	t	ADH_GSCH
30	Update Trial Balance Details	Update Trial Balance Details	0 1 0 1/1 * ? *	2026-08-20 15:18:01.996492	5	\N	\N	2026-08-21 00:01:00	Update Trial Balance DetailsJobDetail1 _ DEFAULT	\N	t	f	t	0	f	1	t	TBL_UDET
31	Execute All Dirty Jobs	Execute All Dirty Jobs	0 1 0 1/1 * ? *	2026-08-20 15:18:01.996492	5	\N	\N	2026-08-21 00:01:00	Execute All Dirty JobsJobDetail1 _ DEFAULT	\N	t	f	t	0	f	0	f	JOB_EXEC
37	Purge External Events	Purge External Events	0 1 0 1/1 * ? *	2026-08-20 15:18:06.94124	5	\N	\N	\N	Purge External EventsJobDetail1 _ DEFAULT	\N	f	f	t	0	f	1	t	EXE_PURG
40	Journal Entry Aggregation	Journal Entry Aggregation	0 0 6 * * ?	2026-08-20 15:18:11.948607	5	\N	\N	\N	Journal Entry AggregationJobDetail1 _ DEFAULT	\N	f	f	t	0	f	1	t	JRNL_AGG
41	Retained Earning Job	Retained Earning Job	0 0 6 * * ?	2026-08-20 15:18:12.68591	5	\N	\N	\N	Retained Earning JobJobDetail1 _ DEFAULT	\N	f	f	t	0	f	1	f	RE_ERNG
42	Add Accrual Transactions For Savings	Add Accrual Transactions For Savings	0 1 0 1/1 * ? *	2026-08-20 15:18:14.189732	5	\N	\N	\N	Add Accrual Transactions For SavingsJobDetail1 _ DEFAULT	\N	f	f	t	0	f	1	t	ADD_ATFS
9	Update Accounting Running Balances	Update Accounting Running Balances	0 1 0 1/1 * ? *	2026-08-20 15:18:01.996492	5	\N	\N	2026-08-21 00:01:00	Update Accounting Running BalancesJobDetail1 _ DEFAULT	\N	t	f	t	0	f	1	t	ACC_RBAL
38	Purge Processed Commands	Purge Processed Commands	0 0 1 * * ?	2026-08-20 15:18:07.349849	5	\N	\N	\N	Purge Processed CommandsJobDetail1 _ DEFAULT	\N	f	f	t	0	f	1	t	COM_PURG
36	Send Asynchronous Events	Send Asynchronous Events	0 0/1 * * *  ?	2026-08-20 15:18:06.895074	5	\N	2026-08-20 15:20:00.015	2026-08-20 15:21:00	Send Asynchronous EventsJobDetail1 _ DEFAULT	\N	t	f	t	0	f	1	f	ASE_SEND
\.


ALTER TABLE public.job 

--
-- Data for Name: job_parameters; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.job_parameters 

COPY public.job_parameters (id, job_id, parameter_name, parameter_value) FROM stdin;
1	17	thread-pool-size	10
2	17	batch-size	100
3	17	officeId	1
4	6	thread-pool-size	10
5	6	batch-size	100
\.


ALTER TABLE public.job_parameters 

--
-- Data for Name: job_run_history; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.job_run_history 

COPY public.job_run_history (id, job_id, version, start_time, end_time, status, error_message, trigger_type, error_log) FROM stdin;
1	36	1	2026-08-20 15:19:00.015	2026-08-20 15:19:00.265	success	\N	cron	\N
2	27	1	2026-08-20 15:20:00.008	2026-08-20 15:20:00.184	success	\N	cron	\N
3	36	2	2026-08-20 15:20:00.015	2026-08-20 15:20:00.194	success	\N	cron	\N
\.


ALTER TABLE public.job_run_history 

--
-- Data for Name: m_account_transfer_details; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_account_transfer_details 

COPY public.m_account_transfer_details (id, from_office_id, to_office_id, from_client_id, to_client_id, from_savings_account_id, to_savings_account_id, from_loan_account_id, to_loan_account_id, transfer_type) FROM stdin;
\.


ALTER TABLE public.m_account_transfer_details 

--
-- Data for Name: m_account_transfer_standing_instructions; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_account_transfer_standing_instructions 

COPY public.m_account_transfer_standing_instructions (id, name, account_transfer_details_id, priority, status, instruction_type, amount, valid_from, valid_till, recurrence_type, recurrence_frequency, recurrence_interval, recurrence_on_day, recurrence_on_month, last_run_date) FROM stdin;
\.


ALTER TABLE public.m_account_transfer_standing_instructions 

--
-- Data for Name: m_account_transfer_standing_instructions_history; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_account_transfer_standing_instructions_history 

COPY public.m_account_transfer_standing_instructions_history (id, standing_instruction_id, status, execution_time, amount, error_log) FROM stdin;
\.


ALTER TABLE public.m_account_transfer_standing_instructions_history 

--
-- Data for Name: m_account_transfer_transaction; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_account_transfer_transaction 

COPY public.m_account_transfer_transaction (id, account_transfer_details_id, from_savings_transaction_id, from_loan_transaction_id, to_savings_transaction_id, to_loan_transaction_id, is_reversed, transaction_date, currency_code, currency_digits, currency_multiplesof, amount, description) FROM stdin;
\.


ALTER TABLE public.m_account_transfer_transaction 

--
-- Data for Name: m_address; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_address 

COPY public.m_address (id, street, address_line_1, address_line_2, address_line_3, town_village, city, county_district, state_province_id, country_id, postal_code, latitude, longitude, created_by, created_on, updated_by, updated_on) FROM stdin;
\.


ALTER TABLE public.m_address 

--
-- Data for Name: m_adhoc; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_adhoc 

COPY public.m_adhoc (id, name, query, table_name, table_fields, email, is_active, created_date, createdby_id, lastmodifiedby_id, lastmodified_date, report_run_frequency_code, report_run_every, last_run) FROM stdin;
\.


ALTER TABLE public.m_adhoc 

--
-- Data for Name: m_appuser_previous_password; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_appuser_previous_password 

COPY public.m_appuser_previous_password (id, user_id, password, removal_date) FROM stdin;
\.


ALTER TABLE public.m_appuser_previous_password 

--
-- Data for Name: m_role; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_role 

COPY public.m_role (id, name, description, is_disabled) FROM stdin;
1	Super user	This role provides all application permissions.	f
2	Self Service User	self service user role	t
\.


ALTER TABLE public.m_role 

--
-- Data for Name: m_appuser_role; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_appuser_role 

COPY public.m_appuser_role (appuser_id, role_id) FROM stdin;
1	1
3	1
\.


ALTER TABLE public.m_appuser_role 

--
-- Data for Name: m_batch_business_steps; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_batch_business_steps 

COPY public.m_batch_business_steps (id, job_name, step_name, step_order) FROM stdin;
1	LOAN_CLOSE_OF_BUSINESS	APPLY_CHARGE_TO_OVERDUE_LOANS	1
2	LOAN_CLOSE_OF_BUSINESS	LOAN_DELINQUENCY_CLASSIFICATION	2
3	LOAN_CLOSE_OF_BUSINESS	CHECK_LOAN_REPAYMENT_DUE	3
4	LOAN_CLOSE_OF_BUSINESS	CHECK_LOAN_REPAYMENT_OVERDUE	4
5	LOAN_CLOSE_OF_BUSINESS	UPDATE_LOAN_ARREARS_AGING	5
6	LOAN_CLOSE_OF_BUSINESS	ADD_PERIODIC_ACCRUAL_ENTRIES	6
7	WORKING_CAPITAL_LOAN_CLOSE_OF_BUSINESS	DUMMY_BUSINESS_STEP	1
8	WORKING_CAPITAL_LOAN_CLOSE_OF_BUSINESS	WC_DELINQUENCY_RANGE_SCHEDULE	2
9	WORKING_CAPITAL_LOAN_CLOSE_OF_BUSINESS	WC_LOAN_DELINQUENCY_CLASSIFICATION	3
10	WORKING_CAPITAL_LOAN_CLOSE_OF_BUSINESS	WC_BREACH_SCHEDULE	4
11	WORKING_CAPITAL_LOAN_CLOSE_OF_BUSINESS	WC_NEAR_BREACH_EVALUATION	5
12	WORKING_CAPITAL_LOAN_CLOSE_OF_BUSINESS	WC_DISCOUNT_FEE_AMORTIZATION	6
\.


ALTER TABLE public.m_batch_business_steps 

--
-- Data for Name: m_business_date; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_business_date 

COPY public.m_business_date (id, type, date, created_by, created_date, version, last_modified_by, lastmodified_date, created_on_utc, last_modified_on_utc) FROM stdin;
\.


ALTER TABLE public.m_business_date 

--
-- Data for Name: m_calendar; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_calendar 

COPY public.m_calendar (id, title, description, location, start_date, end_date, duration, calendar_type_enum, repeating, recurrence, remind_by_enum, first_reminder, second_reminder, created_by, last_modified_by, created_date, lastmodified_date, meeting_time, created_on_utc, last_modified_on_utc) FROM stdin;
\.


ALTER TABLE public.m_calendar 

--
-- Data for Name: m_calendar_history; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_calendar_history 

COPY public.m_calendar_history (id, calendar_id, title, description, location, start_date, end_date, duration, calendar_type_enum, repeating, recurrence, remind_by_enum, first_reminder, second_reminder) FROM stdin;
\.


ALTER TABLE public.m_calendar_history 

--
-- Data for Name: m_calendar_instance; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_calendar_instance 

COPY public.m_calendar_instance (id, calendar_id, entity_id, entity_type_enum) FROM stdin;
\.


ALTER TABLE public.m_calendar_instance 

--
-- Data for Name: m_tellers; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_tellers 

COPY public.m_tellers (id, office_id, debit_account_id, credit_account_id, name, description, valid_from, valid_to, state) FROM stdin;
\.


ALTER TABLE public.m_tellers 

--
-- Data for Name: m_cashiers; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_cashiers 

COPY public.m_cashiers (id, staff_id, teller_id, description, start_date, end_date, start_time, end_time, full_day) FROM stdin;
\.


ALTER TABLE public.m_cashiers 

--
-- Data for Name: m_cashier_transactions; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_cashier_transactions 

COPY public.m_cashier_transactions (id, cashier_id, txn_type, txn_amount, txn_date, created_date, entity_type, entity_id, txn_note, currency_code) FROM stdin;
\.


ALTER TABLE public.m_cashier_transactions 

--
-- Data for Name: m_client_address; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_client_address 

COPY public.m_client_address (id, client_id, address_id, address_type_id, is_active) FROM stdin;
\.


ALTER TABLE public.m_client_address 

--
-- Data for Name: m_meeting; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_meeting 

COPY public.m_meeting (id, calendar_instance_id, meeting_date) FROM stdin;
\.


ALTER TABLE public.m_meeting 

--
-- Data for Name: m_client_attendance; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_client_attendance 

COPY public.m_client_attendance (id, client_id, meeting_id, attendance_type_enum) FROM stdin;
\.


ALTER TABLE public.m_client_attendance 

--
-- Data for Name: m_client_charge; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_client_charge 

COPY public.m_client_charge (id, client_id, charge_id, is_penalty, charge_time_enum, charge_due_date, charge_calculation_enum, amount, amount_paid_derived, amount_waived_derived, amount_writtenoff_derived, amount_outstanding_derived, is_paid_derived, waived, is_active, inactivated_on_date) FROM stdin;
\.


ALTER TABLE public.m_client_charge 

--
-- Data for Name: m_client_charge_paid_by; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_client_charge_paid_by 

COPY public.m_client_charge_paid_by (id, client_transaction_id, client_charge_id, amount) FROM stdin;
\.


ALTER TABLE public.m_client_charge_paid_by 

--
-- Data for Name: m_currency; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_currency 

COPY public.m_currency (id, code, decimal_places, currency_multiplesof, display_symbol, name, internationalized_name_code) FROM stdin;
1	AED	2	\N	\N	UAE Dirham	currency.AED
2	AFN	2	\N	\N	Afghanistan Afghani	currency.AFN
3	ALL	2	\N	\N	Albanian Lek	currency.ALL
4	AMD	2	\N	\N	Armenian Dram	currency.AMD
5	ANG	2	\N	\N	Netherlands Antillian Guilder	currency.ANG
6	AOA	2	\N	\N	Angolan Kwanza	currency.AOA
7	ARS	2	\N	$	Argentine Peso	currency.ARS
8	AUD	2	\N	A$	Australian Dollar	currency.AUD
9	AWG	2	\N	\N	Aruban Guilder	currency.AWG
10	AZM	2	\N	\N	Azerbaijanian Manat	currency.AZM
11	BAM	2	\N	\N	Bosnia and Herzegovina Convertible Marks	currency.BAM
12	BBD	2	\N	\N	Barbados Dollar	currency.BBD
13	BDT	2	\N	\N	Bangladesh Taka	currency.BDT
14	BGN	2	\N	\N	Bulgarian Lev	currency.BGN
15	BHD	3	\N	\N	Bahraini Dinar	currency.BHD
16	BIF	0	\N	\N	Burundi Franc	currency.BIF
17	BMD	2	\N	\N	Bermudian Dollar	currency.BMD
18	BND	2	\N	B$	Brunei Dollar	currency.BND
19	BOB	2	\N	Bs.	Bolivian Boliviano	currency.BOB
20	BRL	2	\N	R$	Brazilian Real	currency.BRL
21	BSD	2	\N	\N	Bahamian Dollar	currency.BSD
22	BTN	2	\N	\N	Bhutan Ngultrum	currency.BTN
23	BWP	2	\N	\N	Botswana Pula	currency.BWP
24	BYR	0	\N	\N	Belarussian Ruble	currency.BYR
25	BZD	2	\N	BZ$	Belize Dollar	currency.BZD
26	CAD	2	\N	\N	Canadian Dollar	currency.CAD
27	CDF	2	\N	\N	Franc Congolais	currency.CDF
28	CHF	2	\N	\N	Swiss Franc	currency.CHF
29	CLP	0	\N	$	Chilean Peso	currency.CLP
30	CNY	2	\N	\N	Chinese Yuan Renminbi	currency.CNY
31	COP	2	\N	$	Colombian Peso	currency.COP
32	CRC	2	\N	₡	Costa Rican Colon	currency.CRC
33	CSD	2	\N	\N	Serbian Dinar	currency.CSD
34	CUP	2	\N	$MN	Cuban Peso	currency.CUP
35	CVE	2	\N	\N	Cape Verde Escudo	currency.CVE
36	CYP	2	\N	\N	Cyprus Pound	currency.CYP
37	CZK	2	\N	\N	Czech Koruna	currency.CZK
38	DJF	0	\N	\N	Djibouti Franc	currency.DJF
39	DKK	2	\N	\N	Danish Krone	currency.DKK
40	DOP	2	\N	RD$	Dominican Peso	currency.DOP
41	DZD	2	\N	\N	Algerian Dinar	currency.DZD
42	EEK	2	\N	\N	Estonian Kroon	currency.EEK
43	EGP	2	\N	\N	Egyptian Pound	currency.EGP
44	ERN	2	\N	\N	Eritrea Nafka	currency.ERN
45	ETB	2	\N	\N	Ethiopian Birr	currency.ETB
46	EUR	2	\N	€	Euro	currency.EUR
47	FJD	2	\N	\N	Fiji Dollar	currency.FJD
48	FKP	2	\N	\N	Falkland Islands Pound	currency.FKP
49	GBP	2	\N	\N	Pound Sterling	currency.GBP
50	GEL	2	\N	\N	Georgian Lari	currency.GEL
51	GHC	2	\N	GHc	Ghana Cedi	currency.GHC
52	GIP	2	\N	\N	Gibraltar Pound	currency.GIP
53	GMD	2	\N	\N	Gambian Dalasi	currency.GMD
54	GNF	0	\N	\N	Guinea Franc	currency.GNF
55	GTQ	2	\N	Q	Guatemala Quetzal	currency.GTQ
56	GYD	2	\N	\N	Guyana Dollar	currency.GYD
57	HKD	2	\N	\N	Hong Kong Dollar	currency.HKD
58	HNL	2	\N	L	Honduras Lempira	currency.HNL
59	HRK	2	\N	\N	Croatian Kuna	currency.HRK
60	HTG	2	\N	G	Haiti Gourde	currency.HTG
61	HUF	2	\N	\N	Hungarian Forint	currency.HUF
62	IDR	2	\N	\N	Indonesian Rupiah	currency.IDR
63	ILS	2	\N	\N	New Israeli Shekel	currency.ILS
64	INR	2	\N	₹	Indian Rupee	currency.INR
65	IQD	3	\N	\N	Iraqi Dinar	currency.IQD
66	IRR	2	\N	\N	Iranian Rial	currency.IRR
67	ISK	0	\N	\N	Iceland Krona	currency.ISK
68	JMD	2	\N	\N	Jamaican Dollar	currency.JMD
69	JOD	3	\N	\N	Jordanian Dinar	currency.JOD
70	JPY	0	\N	\N	Japanese Yen	currency.JPY
71	KES	2	\N	KSh	Kenyan Shilling	currency.KES
72	KGS	2	\N	\N	Kyrgyzstan Som	currency.KGS
73	KHR	2	\N	\N	Cambodia Riel	currency.KHR
74	KMF	0	\N	\N	Comoro Franc	currency.KMF
75	KPW	2	\N	\N	North Korean Won	currency.KPW
76	KRW	0	\N	\N	Korean Won	currency.KRW
77	KWD	3	\N	\N	Kuwaiti Dinar	currency.KWD
78	KYD	2	\N	\N	Cayman Islands Dollar	currency.KYD
79	KZT	2	\N	\N	Kazakhstan Tenge	currency.KZT
80	LAK	2	\N	\N	Lao Kip	currency.LAK
81	LBP	2	\N	L£	Lebanese Pound	currency.LBP
82	LKR	2	\N	\N	Sri Lanka Rupee	currency.LKR
83	LRD	2	\N	\N	Liberian Dollar	currency.LRD
84	LSL	2	\N	\N	Lesotho Loti	currency.LSL
85	LTL	2	\N	\N	Lithuanian Litas	currency.LTL
86	LVL	2	\N	\N	Latvian Lats	currency.LVL
87	LYD	3	\N	\N	Libyan Dinar	currency.LYD
88	MAD	2	\N	\N	Moroccan Dirham	currency.MAD
89	MDL	2	\N	\N	Moldovan Leu	currency.MDL
90	MGA	2	\N	\N	Malagasy Ariary	currency.MGA
91	MKD	2	\N	\N	Macedonian Denar	currency.MKD
92	MMK	2	\N	K	Myanmar Kyat	currency.MMK
93	MNT	2	\N	\N	Mongolian Tugrik	currency.MNT
94	MOP	2	\N	\N	Macau Pataca	currency.MOP
95	MRO	2	\N	\N	Mauritania Ouguiya	currency.MRO
96	MTL	2	\N	\N	Maltese Lira	currency.MTL
97	MUR	2	\N	\N	Mauritius Rupee	currency.MUR
98	MVR	2	\N	\N	Maldives Rufiyaa	currency.MVR
99	MWK	2	\N	\N	Malawi Kwacha	currency.MWK
100	MXN	2	\N	$	Mexican Peso	currency.MXN
101	MYR	2	\N	\N	Malaysian Ringgit	currency.MYR
102	MZM	2	\N	\N	Mozambique Metical	currency.MZM
103	NAD	2	\N	\N	Namibia Dollar	currency.NAD
104	NGN	2	\N	\N	Nigerian Naira	currency.NGN
105	NIO	2	\N	C$	Nicaragua Cordoba Oro	currency.NIO
106	NOK	2	\N	\N	Norwegian Krone	currency.NOK
107	NPR	2	\N	\N	Nepalese Rupee	currency.NPR
108	NZD	2	\N	\N	New Zealand Dollar	currency.NZD
109	OMR	3	\N	\N	Rial Omani	currency.OMR
110	PAB	2	\N	B/.	Panama Balboa	currency.PAB
111	PEN	2	\N	S/.	Peruvian Nuevo Sol	currency.PEN
112	PGK	2	\N	\N	Papua New Guinea Kina	currency.PGK
113	PHP	2	\N	\N	Philippine Peso	currency.PHP
114	PKR	2	\N	\N	Pakistan Rupee	currency.PKR
115	PLN	2	\N	\N	Polish Zloty	currency.PLN
116	PYG	0	\N	₲	Paraguayan Guarani	currency.PYG
117	QAR	2	\N	\N	Qatari Rial	currency.QAR
118	RON	2	\N	\N	Romanian Leu	currency.RON
119	RUB	2	\N	\N	Russian Ruble	currency.RUB
120	RWF	0	\N	\N	Rwanda Franc	currency.RWF
121	SAR	2	\N	\N	Saudi Riyal	currency.SAR
122	SBD	2	\N	\N	Solomon Islands Dollar	currency.SBD
123	SCR	2	\N	\N	Seychelles Rupee	currency.SCR
124	SDD	2	\N	\N	Sudanese Dinar	currency.SDD
125	SEK	2	\N	\N	Swedish Krona	currency.SEK
126	SGD	2	\N	\N	Singapore Dollar	currency.SGD
127	SHP	2	\N	\N	St Helena Pound	currency.SHP
128	SIT	2	\N	\N	Slovenian Tolar	currency.SIT
129	SKK	2	\N	\N	Slovak Koruna	currency.SKK
130	SLL	2	\N	\N	Sierra Leone Leone	currency.SLL
131	SOS	2	\N	\N	Somali Shilling	currency.SOS
132	SRD	2	\N	\N	Surinam Dollar	currency.SRD
133	STD	2	\N	\N	Sao Tome and Principe Dobra	currency.STD
134	SVC	2	\N	\N	El Salvador Colon	currency.SVC
135	SYP	2	\N	\N	Syrian Pound	currency.SYP
136	SZL	2	\N	\N	Eswatini Lilangeni	currency.SZL
137	THB	2	\N	\N	Thai Baht	currency.THB
138	TJS	2	\N	\N	Tajik Somoni	currency.TJS
139	TMM	2	\N	\N	Turkmenistan Manat	currency.TMM
140	TND	3	\N	DT	Tunisian Dinar	currency.TND
141	TOP	2	\N	\N	Tonga Pa'anga	currency.TOP
142	TRY	2	\N	\N	Turkish Lira	currency.TRY
143	TTD	2	\N	\N	Trinidad and Tobago Dollar	currency.TTD
144	TWD	2	\N	\N	New Taiwan Dollar	currency.TWD
145	TZS	2	\N	\N	Tanzanian Shilling	currency.TZS
146	UAH	2	\N	\N	Ukraine Hryvnia	currency.UAH
147	UGX	2	\N	USh	Uganda Shilling	currency.UGX
148	USD	2	\N	$	US Dollar	currency.USD
149	UYU	2	\N	$U	Peso Uruguayo	currency.UYU
150	UZS	2	\N	\N	Uzbekistan Sum	currency.UZS
151	VEB	2	\N	Bs.F.	Venezuelan Bolivar	currency.VEB
152	VND	2	\N	\N	Vietnamese Dong	currency.VND
153	VUV	0	\N	\N	Vanuatu Vatu	currency.VUV
154	WST	2	\N	\N	Samoa Tala	currency.WST
155	XAF	0	\N	\N	CFA Franc BEAC	currency.XAF
156	XCD	2	\N	\N	East Caribbean Dollar	currency.XCD
157	XDR	5	\N	\N	SDR (Special Drawing Rights)	currency.XDR
158	XOF	0	\N	CFA	CFA Franc BCEAO	currency.XOF
159	XPF	0	\N	\N	CFP Franc	currency.XPF
160	YER	2	\N	\N	Yemeni Rial	currency.YER
161	ZAR	2	\N	R	South African Rand	currency.ZAR
162	ZMK	2	\N	\N	Zambian Kwacha	currency.ZMK
163	ZWD	2	\N	\N	Zimbabwe Dollar	currency.ZWD
164	SSP	2	\N	SS£	South Sudanese Pound	currency.SSP
\.


ALTER TABLE public.m_currency 

--
-- Data for Name: m_collateral_management; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_collateral_management 

COPY public.m_collateral_management (id, name, quality, base_price, unit_type, pct_to_base, currency) FROM stdin;
\.


ALTER TABLE public.m_collateral_management 

--
-- Data for Name: m_client_collateral_management; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_client_collateral_management 

COPY public.m_client_collateral_management (id, quantity, client_id, collateral_id) FROM stdin;
\.


ALTER TABLE public.m_client_collateral_management 

--
-- Data for Name: m_client_identifier; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_client_identifier 

COPY public.m_client_identifier (id, client_id, document_type_id, document_key, status, active, description, created_by, last_modified_by, created_date, lastmodified_date, created_on_utc, last_modified_on_utc) FROM stdin;
\.


ALTER TABLE public.m_client_identifier 

--
-- Data for Name: m_client_non_person; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_client_non_person 

COPY public.m_client_non_person (id, client_id, constitution_cv_id, incorp_no, incorp_validity_till, main_business_line_cv_id, remarks) FROM stdin;
\.


ALTER TABLE public.m_client_non_person 

--
-- Data for Name: m_client_transfer_details; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_client_transfer_details 

COPY public.m_client_transfer_details (id, client_id, from_office_id, to_office_id, proposed_transfer_date, transfer_type, submitted_on, submitted_by) FROM stdin;
\.


ALTER TABLE public.m_client_transfer_details 

--
-- Data for Name: m_command; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_command 

COPY public.m_command (id, created_at, command_id, tenant_id, initiated_by_username, request, updated_at, executed_at, approved_at, rejected_at, idempotency_key, executed_by_username, approved_by_username, rejected_by_username, ip_address, state, response, error) FROM stdin;
\.


ALTER TABLE public.m_command 

--
-- Data for Name: m_creditbureau; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_creditbureau 

COPY public.m_creditbureau (id, name, product, country, implementation_key) FROM stdin;
1	THITSAWORKS	1	Myanmar	1
\.


ALTER TABLE public.m_creditbureau 

--
-- Data for Name: m_creditbureau_configuration; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_creditbureau_configuration 

COPY public.m_creditbureau_configuration (id, configkey, value, organisation_creditbureau_id, description) FROM stdin;
1	Password		1	
2	SubscriptionId		1	
3	SubscriptionKey		1	
4	Username		1	
5	tokenurl		1	
6	searchurl		1	
7	creditReporturl		1	
8	addCreditReporturl		1	
\.


ALTER TABLE public.m_creditbureau_configuration 

--
-- Data for Name: m_organisation_creditbureau; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_organisation_creditbureau 

COPY public.m_organisation_creditbureau (id, alias, creditbureau_id, is_active) FROM stdin;
\.


ALTER TABLE public.m_organisation_creditbureau 

--
-- Data for Name: m_creditbureau_loanproduct_mapping; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_creditbureau_loanproduct_mapping 

COPY public.m_creditbureau_loanproduct_mapping (id, organisation_creditbureau_id, loan_product_id, is_creditcheck_mandatory, skip_creditcheck_in_failure, stale_period, is_active) FROM stdin;
\.


ALTER TABLE public.m_creditbureau_loanproduct_mapping 

--
-- Data for Name: m_creditbureau_token; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_creditbureau_token 

COPY public.m_creditbureau_token (id, username, token, token_type, expires_in, issued, expiry_date) FROM stdin;
\.


ALTER TABLE public.m_creditbureau_token 

--
-- Data for Name: m_creditreport; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_creditreport 

COPY public.m_creditreport (id, credit_bureau_id, national_id, credit_reports) FROM stdin;
\.


ALTER TABLE public.m_creditreport 

--
-- Data for Name: m_delinquency_range; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_delinquency_range 

COPY public.m_delinquency_range (id, classification, min_age_days, max_age_days, created_by, created_on_utc, version, last_modified_by, last_modified_on_utc) FROM stdin;
\.


ALTER TABLE public.m_delinquency_range 

--
-- Data for Name: m_delinquency_bucket_mappings; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_delinquency_bucket_mappings 

COPY public.m_delinquency_bucket_mappings (id, delinquency_range_id, delinquency_bucket_id, created_by, created_on_utc, version, last_modified_by, last_modified_on_utc) FROM stdin;
\.


ALTER TABLE public.m_delinquency_bucket_mappings 

--
-- Data for Name: m_deposit_account_on_hold_transaction; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_deposit_account_on_hold_transaction 

COPY public.m_deposit_account_on_hold_transaction (id, savings_account_id, amount, transaction_type_enum, transaction_date, is_reversed, created_date, created_by, last_modified_by, created_on_utc, last_modified_on_utc) FROM stdin;
\.


ALTER TABLE public.m_deposit_account_on_hold_transaction 

--
-- Data for Name: m_deposit_account_recurring_detail; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_deposit_account_recurring_detail 

COPY public.m_deposit_account_recurring_detail (id, savings_account_id, mandatory_recommended_deposit_amount, is_mandatory, allow_withdrawal, adjust_advance_towards_future_payments, is_calendar_inherited, total_overdue_amount, no_of_overdue_installments) FROM stdin;
\.


ALTER TABLE public.m_deposit_account_recurring_detail 

--
-- Data for Name: m_deposit_account_term_and_preclosure; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_deposit_account_term_and_preclosure 

COPY public.m_deposit_account_term_and_preclosure (id, savings_account_id, min_deposit_term, max_deposit_term, min_deposit_term_type_enum, max_deposit_term_type_enum, in_multiples_of_deposit_term, in_multiples_of_deposit_term_type_enum, pre_closure_penal_applicable, pre_closure_penal_interest, pre_closure_penal_interest_on_enum, deposit_period, deposit_period_frequency_enum, deposit_amount, maturity_amount, maturity_date, on_account_closure_enum, expected_firstdepositon_date, transfer_interest_to_linked_account, transfer_to_savings_account_id) FROM stdin;
\.


ALTER TABLE public.m_deposit_account_term_and_preclosure 

--
-- Data for Name: m_interest_rate_chart; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_interest_rate_chart 

COPY public.m_interest_rate_chart (id, name, description, from_date, end_date, is_primary_grouping_by_amount) FROM stdin;
\.


ALTER TABLE public.m_interest_rate_chart 

--
-- Data for Name: m_deposit_product_interest_rate_chart; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_deposit_product_interest_rate_chart 

COPY public.m_deposit_product_interest_rate_chart (deposit_product_id, interest_rate_chart_id) FROM stdin;
\.


ALTER TABLE public.m_deposit_product_interest_rate_chart 

--
-- Data for Name: m_deposit_product_recurring_detail; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_deposit_product_recurring_detail 

COPY public.m_deposit_product_recurring_detail (id, savings_product_id, is_mandatory, allow_withdrawal, adjust_advance_towards_future_payments) FROM stdin;
\.


ALTER TABLE public.m_deposit_product_recurring_detail 

--
-- Data for Name: m_deposit_product_term_and_preclosure; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_deposit_product_term_and_preclosure 

COPY public.m_deposit_product_term_and_preclosure (id, savings_product_id, min_deposit_term, max_deposit_term, min_deposit_term_type_enum, max_deposit_term_type_enum, in_multiples_of_deposit_term, in_multiples_of_deposit_term_type_enum, pre_closure_penal_applicable, pre_closure_penal_interest, pre_closure_penal_interest_on_enum, min_deposit_amount, max_deposit_amount, deposit_amount) FROM stdin;
\.


ALTER TABLE public.m_deposit_product_term_and_preclosure 

--
-- Data for Name: m_document; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_document 

COPY public.m_document (id, parent_entity_type, parent_entity_id, name, file_name, size, type, description, location, storage_type_enum) FROM stdin;
\.


ALTER TABLE public.m_document 

--
-- Data for Name: x_registered_table; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.x_registered_table 

COPY public.x_registered_table (registered_table_name, application_table_name, entity_subtype, category) FROM stdin;
\.


ALTER TABLE public.x_registered_table 

--
-- Data for Name: m_entity_datatable_check; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_entity_datatable_check 

COPY public.m_entity_datatable_check (id, application_table_name, x_registered_table_name, status_enum, system_defined, product_id) FROM stdin;
\.


ALTER TABLE public.m_entity_datatable_check 

--
-- Data for Name: m_entity_relation; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_entity_relation 

COPY public.m_entity_relation (id, from_entity_type, to_entity_type, code_name) FROM stdin;
1	1	2	office_access_to_loan_products
2	1	3	office_access_to_savings_products
3	1	4	office_access_to_fees/charges
4	5	2	role_access_to_loan_products
5	5	3	role_access_to_savings_products
\.


ALTER TABLE public.m_entity_relation 

--
-- Data for Name: m_entity_to_entity_access; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_entity_to_entity_access 

COPY public.m_entity_to_entity_access (id, entity_type, entity_id, access_type_code_value_id, second_entity_type, second_entity_id) FROM stdin;
\.


ALTER TABLE public.m_entity_to_entity_access 

--
-- Data for Name: m_entity_to_entity_mapping; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_entity_to_entity_mapping 

COPY public.m_entity_to_entity_mapping (id, rel_id, from_id, to_id, start_date, end_date) FROM stdin;
\.


ALTER TABLE public.m_entity_to_entity_mapping 

--
-- Data for Name: m_external_asset_owner; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_external_asset_owner 

COPY public.m_external_asset_owner (id, external_id, created_by, created_on_utc, last_modified_by, last_modified_on_utc) FROM stdin;
\.


ALTER TABLE public.m_external_asset_owner 

--
-- Data for Name: m_external_asset_owner_journal_entry_mapping; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_external_asset_owner_journal_entry_mapping 

COPY public.m_external_asset_owner_journal_entry_mapping (id, journal_entry_id, owner_id, created_by, created_on_utc, last_modified_by, last_modified_on_utc) FROM stdin;
\.


ALTER TABLE public.m_external_asset_owner_journal_entry_mapping 

--
-- Data for Name: m_external_asset_owner_loan_product_configurable_attributes; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_external_asset_owner_loan_product_configurable_attributes 

COPY public.m_external_asset_owner_loan_product_configurable_attributes (id, loan_product_id, attribute_key, attribute_value, created_by, created_on_utc, last_modified_by, last_modified_on_utc) FROM stdin;
\.


ALTER TABLE public.m_external_asset_owner_loan_product_configurable_attributes 

--
-- Data for Name: m_external_asset_owner_transfer; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_external_asset_owner_transfer 

COPY public.m_external_asset_owner_transfer (id, owner_id, external_id, status, purchase_price_ratio, settlement_date, effective_date_from, effective_date_to, created_by, created_on_utc, last_modified_by, last_modified_on_utc, external_loan_id, loan_id, sub_status, external_group_id, previous_owner_id) FROM stdin;
\.


ALTER TABLE public.m_external_asset_owner_transfer 

--
-- Data for Name: m_external_asset_owner_transfer_details; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_external_asset_owner_transfer_details 

COPY public.m_external_asset_owner_transfer_details (id, asset_owner_transfer_id, total_outstanding_derived, principal_outstanding_derived, interest_outstanding_derived, fee_charges_outstanding_derived, penalty_charges_outstanding_derived, total_overpaid_derived, created_by, created_on_utc, last_modified_by, last_modified_on_utc) FROM stdin;
\.


ALTER TABLE public.m_external_asset_owner_transfer_details 

--
-- Data for Name: m_external_asset_owner_transfer_journal_entry_mapping; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_external_asset_owner_transfer_journal_entry_mapping 

COPY public.m_external_asset_owner_transfer_journal_entry_mapping (id, journal_entry_id, owner_transfer_id, created_by, created_on_utc, last_modified_by, last_modified_on_utc) FROM stdin;
\.


ALTER TABLE public.m_external_asset_owner_transfer_journal_entry_mapping 

--
-- Data for Name: m_external_asset_owner_transfer_loan_mapping; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_external_asset_owner_transfer_loan_mapping 

COPY public.m_external_asset_owner_transfer_loan_mapping (id, loan_id, owner_transfer_id, created_by, created_on_utc, last_modified_by, last_modified_on_utc) FROM stdin;
\.


ALTER TABLE public.m_external_asset_owner_transfer_loan_mapping 

--
-- Data for Name: m_external_event; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_external_event 

COPY public.m_external_event (id, type, created_at, status, business_date, data, idempotency_key, sent_at, schema, category, aggregate_root_id) FROM stdin;
\.


ALTER TABLE public.m_external_event 

--
-- Data for Name: m_external_event_configuration; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_external_event_configuration 

COPY public.m_external_event_configuration (type, enabled) FROM stdin;
ClientActivateBusinessEvent	f
ClientCreateBusinessEvent	f
ClientRejectBusinessEvent	f
FixedDepositAccountCreateBusinessEvent	f
RecurringDepositAccountCreateBusinessEvent	f
CentersCreateBusinessEvent	f
GroupsCreateBusinessEvent	f
LoanAddChargeBusinessEvent	f
LoanDeleteChargeBusinessEvent	f
LoanUpdateChargeBusinessEvent	f
LoanWaiveChargeBusinessEvent	f
LoanWaiveChargeUndoBusinessEvent	f
LoanProductCreateBusinessEvent	f
LoanChargePaymentPostBusinessEvent	f
LoanChargePaymentPreBusinessEvent	f
LoanChargeRefundBusinessEvent	f
LoanCreditBalanceRefundPostBusinessEvent	f
LoanCreditBalanceRefundPreBusinessEvent	f
LoanDisbursalTransactionBusinessEvent	f
LoanForeClosurePostBusinessEvent	f
LoanForeClosurePreBusinessEvent	f
LoanRefundPostBusinessEvent	f
LoanRefundPreBusinessEvent	f
LoanTransactionGoodwillCreditPostBusinessEvent	f
LoanTransactionGoodwillCreditPreBusinessEvent	f
LoanTransactionMakeRepaymentPostBusinessEvent	f
LoanTransactionMakeRepaymentPreBusinessEvent	f
LoanTransactionMerchantIssuedRefundPostBusinessEvent	f
LoanTransactionMerchantIssuedRefundPreBusinessEvent	f
LoanTransactionPayoutRefundPostBusinessEvent	f
LoanTransactionPayoutRefundPreBusinessEvent	f
LoanTransactionRecoveryPaymentPostBusinessEvent	f
LoanTransactionRecoveryPaymentPreBusinessEvent	f
LoanUndoWrittenOffBusinessEvent	f
LoanWaiveInterestBusinessEvent	f
LoanWrittenOffPostBusinessEvent	f
LoanWrittenOffPreBusinessEvent	f
LoanAcceptTransferBusinessEvent	f
LoanAdjustTransactionBusinessEvent	f
LoanApplyOverdueChargeBusinessEvent	f
LoanApprovedBusinessEvent	f
LoanBalanceChangedBusinessEvent	f
LoanChargebackTransactionBusinessEvent	f
LoanCloseAsRescheduleBusinessEvent	f
LoanCloseBusinessEvent	f
LoanCreatedBusinessEvent	f
LoanDisbursalBusinessEvent	f
LoanInitiateTransferBusinessEvent	f
LoanInterestRecalculationBusinessEvent	f
LoanReassignOfficerBusinessEvent	f
LoanRejectedBusinessEvent	f
LoanRejectTransferBusinessEvent	f
LoanRemoveOfficerBusinessEvent	f
LoanRescheduledDueCalendarChangeBusinessEvent	f
LoanRescheduledDueHolidayBusinessEvent	f
LoanScheduleVariationsAddedBusinessEvent	f
LoanScheduleVariationsDeletedBusinessEvent	f
LoanStatusChangedBusinessEvent	f
LoanUndoApprovalBusinessEvent	f
LoanUndoDisbursalBusinessEvent	f
LoanUndoLastDisbursalBusinessEvent	f
LoanUpdateDisbursementDataBusinessEvent	f
LoanWithdrawTransferBusinessEvent	f
SavingsDepositBusinessEvent	f
SavingsWithdrawalBusinessEvent	f
SavingsActivateBusinessEvent	f
SavingsApproveBusinessEvent	f
SavingsCloseBusinessEvent	f
SavingsCreateBusinessEvent	f
SavingsPostInterestBusinessEvent	f
SavingsRejectBusinessEvent	f
ShareAccountApproveBusinessEvent	f
ShareAccountCreateBusinessEvent	f
ShareProductDividentsCreateBusinessEvent	f
LoanRepaymentDueBusinessEvent	f
LoanRepaymentOverdueBusinessEvent	f
LoanChargeAdjustmentPostBusinessEvent	f
LoanChargeAdjustmentPreBusinessEvent	f
LoanDelinquencyRangeChangeBusinessEvent	f
LoanAccountsStayedLockedBusinessEvent	f
LoanChargeOffPreBusinessEvent	f
LoanChargeOffPostBusinessEvent	f
LoanUndoChargeOffBusinessEvent	f
LoanAccrualTransactionCreatedBusinessEvent	f
LoanRescheduledDueAdjustScheduleBusinessEvent	f
LoanReAgeTransactionBusinessEvent	f
LoanUndoReAgeTransactionBusinessEvent	f
LoanReAmortizeTransactionBusinessEvent	f
LoanUndoReAmortizeTransactionBusinessEvent	f
LoanReAmortizeBusinessEvent	f
LoanUndoReAmortizeBusinessEvent	f
LoanReAgeBusinessEvent	f
LoanUndoReAgeBusinessEvent	f
LoanTransactionInterestPaymentWaiverPostBusinessEvent	f
LoanTransactionInterestPaymentWaiverPreBusinessEvent	f
LoanTransactionAccrualActivityPostBusinessEvent	f
LoanTransactionAccrualActivityPreBusinessEvent	f
LoanTransactionInterestRefundPostBusinessEvent	f
LoanTransactionInterestRefundPreBusinessEvent	f
LoanCapitalizedIncomeAmortizationTransactionCreatedBusinessEvent	f
LoanCapitalizedIncomeAdjustmentTransactionCreatedBusinessEvent	f
LoanCapitalizedIncomeAmortizationAdjustmentTransactionCreatedBusinessEvent	f
LoanCapitalizedIncomeTransactionCreatedBusinessEvent	f
DocumentCreatedBusinessEvent	f
DocumentDeletedBusinessEvent	f
LoanWithdrawnByApplicantBusinessEvent	f
LoanApplicationModifiedBusinessEvent	f
LoanBuyDownFeeTransactionCreatedBusinessEvent	f
LoanBuyDownFeeAdjustmentTransactionCreatedBusinessEvent	f
LoanBuyDownFeeAmortizationTransactionCreatedBusinessEvent	f
LoanBuyDownFeeAmortizationAdjustmentTransactionCreatedBusinessEvent	f
LoanApprovedAmountChangedBusinessEvent	f
SavingsAccountForceWithdrawalBusinessEvent	f
LoanTransactionDownPaymentPreBusinessEvent	f
LoanTransactionDownPaymentPostBusinessEvent	f
LoanAccountDelinquencyPauseChangedBusinessEvent	f
LoanAccountCustomSnapshotBusinessEvent	f
LoanAccrualAdjustmentTransactionBusinessEvent	f
LoanUndoContractTerminationBusinessEvent	f
LoanOwnershipTransferBusinessEvent	f
LoanAccountSnapshotBusinessEvent	f
SavingsAccountsStayedLockedBusinessEvent	f
LoanTransactionContractTerminationPostBusinessEvent	f
WorkingCapitalLoanDisbursalTransactionBusinessEvent	f
WorkingCapitalLoanUndoDisbursalTransactionBusinessEvent	f
WorkingCapitalLoanRepaymentTransactionBusinessEvent	f
WorkingCapitalLoanCreditBalanceRefundTransactionBusinessEvent	f
WorkingCapitalLoanDiscountFeeTransactionBusinessEvent	f
WorkingCapitalLoanDiscountFeeAdjustmentTransactionBusinessEvent	f
WorkingCapitalLoanChargeAdjustmentPreBusinessEvent	f
WorkingCapitalLoanChargeAdjustmentPostBusinessEvent	f
\.


ALTER TABLE public.m_external_event_configuration 

--
-- Data for Name: m_family_members; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_family_members 

COPY public.m_family_members (id, client_id, firstname, middlename, lastname, qualification, relationship_cv_id, marital_status_cv_id, gender_cv_id, date_of_birth, age, profession_cv_id, mobile_number, is_dependent) FROM stdin;
\.


ALTER TABLE public.m_family_members 

--
-- Data for Name: m_field_configuration; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_field_configuration 

COPY public.m_field_configuration (id, entity, subentity, field, is_enabled, is_mandatory, validation_regex) FROM stdin;
1	ADDRESS	CLIENT	addressType	t	f	
3	ADDRESS	CLIENT	addressLine1	t	f	
4	ADDRESS	CLIENT	addressLine2	t	f	
5	ADDRESS	CLIENT	addressLine3	t	f	
6	ADDRESS	CLIENT	townVillage	f	f	
7	ADDRESS	CLIENT	city	t	f	
8	ADDRESS	CLIENT	countyDistrict	f	f	
9	ADDRESS	CLIENT	stateProvinceId	t	f	
10	ADDRESS	CLIENT	countryId	t	f	
11	ADDRESS	CLIENT	postalCode	t	f	
12	ADDRESS	CLIENT	latitude	f	f	
13	ADDRESS	CLIENT	longitude	f	f	
14	ADDRESS	CLIENT	createdBy	t	f	
15	ADDRESS	CLIENT	createdOn	t	f	
16	ADDRESS	CLIENT	updatedBy	t	f	
17	ADDRESS	CLIENT	updatedOn	t	f	
18	ADDRESS	CLIENT	isActive	t	f	
\.


ALTER TABLE public.m_field_configuration 

--
-- Data for Name: m_floating_rates; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_floating_rates 

COPY public.m_floating_rates (id, name, is_base_lending_rate, is_active, created_by, created_date, last_modified_by, lastmodified_date, created_on_utc, last_modified_on_utc) FROM stdin;
\.


ALTER TABLE public.m_floating_rates 

--
-- Data for Name: m_floating_rates_periods; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_floating_rates_periods 

COPY public.m_floating_rates_periods (id, floating_rates_id, from_date, interest_rate, is_differential_to_base_lending_rate, is_active, created_by, created_date, last_modified_by, lastmodified_date, created_on_utc, last_modified_on_utc) FROM stdin;
\.


ALTER TABLE public.m_floating_rates_periods 

--
-- Data for Name: m_group_client; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_group_client 

COPY public.m_group_client (group_id, client_id) FROM stdin;
\.


ALTER TABLE public.m_group_client 

--
-- Data for Name: m_group_roles; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_group_roles 

COPY public.m_group_roles (id, client_id, group_id, role_cv_id) FROM stdin;
\.


ALTER TABLE public.m_group_roles 

--
-- Data for Name: m_guarantor; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_guarantor 

COPY public.m_guarantor (id, loan_id, client_reln_cv_id, type_enum, entity_id, firstname, lastname, dob, address_line_1, address_line_2, city, state, country, zip, house_phone_number, mobile_number, comment, is_active) FROM stdin;
\.


ALTER TABLE public.m_guarantor 

--
-- Data for Name: m_portfolio_account_associations; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_portfolio_account_associations 

COPY public.m_portfolio_account_associations (id, loan_account_id, savings_account_id, linked_loan_account_id, linked_savings_account_id, association_type_enum, is_active) FROM stdin;
\.


ALTER TABLE public.m_portfolio_account_associations 

--
-- Data for Name: m_guarantor_funding_details; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_guarantor_funding_details 

COPY public.m_guarantor_funding_details (id, guarantor_id, account_associations_id, amount, amount_released_derived, amount_remaining_derived, amount_transfered_derived, status_enum) FROM stdin;
\.


ALTER TABLE public.m_guarantor_funding_details 

--
-- Data for Name: m_guarantor_transaction; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_guarantor_transaction 

COPY public.m_guarantor_transaction (id, guarantor_fund_detail_id, loan_transaction_id, deposit_on_hold_transaction_id, is_reversed) FROM stdin;
\.


ALTER TABLE public.m_guarantor_transaction 

--
-- Data for Name: m_holiday; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_holiday 

COPY public.m_holiday (id, name, from_date, to_date, repayments_rescheduled_to, status_enum, processed, description, rescheduling_type) FROM stdin;
\.


ALTER TABLE public.m_holiday 

--
-- Data for Name: m_holiday_office; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_holiday_office 

COPY public.m_holiday_office (holiday_id, office_id) FROM stdin;
\.


ALTER TABLE public.m_holiday_office 

--
-- Data for Name: m_hook_templates; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_hook_templates 

COPY public.m_hook_templates (id, name) FROM stdin;
1	Web
2	SMS Bridge
3	Elastic Search
4	Message Gateway
\.


ALTER TABLE public.m_hook_templates 

--
-- Data for Name: m_template; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_template 

COPY public.m_template (id, name, text, entity, type) FROM stdin;
\.


ALTER TABLE public.m_template 

--
-- Data for Name: m_hook; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_hook 

COPY public.m_hook (id, template_id, is_active, name, createdby_id, created_date, lastmodifiedby_id, lastmodified_date, ugd_template_id) FROM stdin;
\.


ALTER TABLE public.m_hook 

--
-- Data for Name: m_hook_configuration; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_hook_configuration 

COPY public.m_hook_configuration (id, hook_id, field_type, field_name, field_value) FROM stdin;
\.


ALTER TABLE public.m_hook_configuration 

--
-- Data for Name: m_hook_registered_events; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_hook_registered_events 

COPY public.m_hook_registered_events (id, hook_id, entity_name, action_name) FROM stdin;
\.


ALTER TABLE public.m_hook_registered_events 

--
-- Data for Name: m_hook_schema; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_hook_schema 

COPY public.m_hook_schema (id, hook_template_id, field_type, field_name, placeholder, optional) FROM stdin;
1	1	string	Payload URL	\N	f
2	1	string	Content Type	json / form	f
3	2	string	Payload URL	\N	f
4	2	string	SMS Provider	\N	f
5	2	string	Phone Number	\N	f
6	2	string	SMS Provider Token	\N	f
7	2	string	SMS Provider Account Id	\N	f
8	3	string	Payload URL	http://<host>/<index name>/<type name>	f
9	3	string	Content Type	json	f
10	3	string	Index Name	\N	t
11	4	string	SMS Provider Id	\N	f
\.


ALTER TABLE public.m_hook_schema 

--
-- Data for Name: m_import_document; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_import_document 

COPY public.m_import_document (id, document_id, import_time, end_time, entity_type, completed, total_records, success_count, failure_count, createdby_id) FROM stdin;
\.


ALTER TABLE public.m_import_document 

--
-- Data for Name: m_interest_rate_slab; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_interest_rate_slab 

COPY public.m_interest_rate_slab (id, interest_rate_chart_id, description, period_type_enum, from_period, to_period, amount_range_from, amount_range_to, annual_interest_rate, currency_code) FROM stdin;
\.


ALTER TABLE public.m_interest_rate_slab 

--
-- Data for Name: m_interest_incentives; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_interest_incentives 

COPY public.m_interest_incentives (id, interest_rate_slab_id, entiry_type, attribute_name, condition_type, attribute_value, incentive_type, amount) FROM stdin;
\.


ALTER TABLE public.m_interest_incentives 

--
-- Data for Name: m_journal_entry_aggregation_summary; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_journal_entry_aggregation_summary 

COPY public.m_journal_entry_aggregation_summary (id, gl_account_id, product_id, office_id, entity_type_enum, aggregated_on_date, submitted_on_date, external_owner_id, debit_amount, credit_amount, manual_entry, job_execution_id, created_by, created_on_utc, last_modified_by, last_modified_on_utc, originator_external_ids) FROM stdin;
\.


ALTER TABLE public.m_journal_entry_aggregation_summary 

--
-- Data for Name: m_journal_entry_aggregation_tracking; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_journal_entry_aggregation_tracking 

COPY public.m_journal_entry_aggregation_tracking (id, aggregated_on_date_from, aggregated_on_date_to, submitted_on_date, job_execution_id, created_by, created_on_utc, last_modified_by, last_modified_on_utc) FROM stdin;
\.


ALTER TABLE public.m_journal_entry_aggregation_tracking 

--
-- Data for Name: m_loan_account_locks; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_loan_account_locks 

COPY public.m_loan_account_locks (loan_id, lock_owner, error, version, stacktrace, lock_placed_on, lock_placed_on_cob_business_date) FROM stdin;
\.


ALTER TABLE public.m_loan_account_locks 

--
-- Data for Name: m_loan_amortization_allocation_mapping; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_loan_amortization_allocation_mapping 

COPY public.m_loan_amortization_allocation_mapping (id, loan_id, base_loan_transaction_id, date, amortization_loan_transaction_id, amortization_type, amount, created_by, created_on_utc, last_modified_by, last_modified_on_utc) FROM stdin;
\.


ALTER TABLE public.m_loan_amortization_allocation_mapping 

--
-- Data for Name: m_loan_approved_amount_history; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_loan_approved_amount_history 

COPY public.m_loan_approved_amount_history (id, loan_id, new_approved_amount, old_approved_amount, created_by, created_on_utc, last_modified_by, last_modified_on_utc) FROM stdin;
\.


ALTER TABLE public.m_loan_approved_amount_history 

--
-- Data for Name: m_loan_arrears_aging; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_loan_arrears_aging 

COPY public.m_loan_arrears_aging (loan_id, principal_overdue_derived, interest_overdue_derived, fee_charges_overdue_derived, penalty_charges_overdue_derived, total_overdue_derived, overdue_since_date_derived) FROM stdin;
\.


ALTER TABLE public.m_loan_arrears_aging 

--
-- Data for Name: m_loan_buy_down_fee_balance; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_loan_buy_down_fee_balance 

COPY public.m_loan_buy_down_fee_balance (id, version, loan_id, loan_transaction_id, amount, date, unrecognized_amount, charged_off_amount, amount_adjustment, created_by, created_on_utc, last_modified_by, last_modified_on_utc, is_deleted, is_closed) FROM stdin;
\.


ALTER TABLE public.m_loan_buy_down_fee_balance 

--
-- Data for Name: m_loan_capitalized_income_balance; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_loan_capitalized_income_balance 

COPY public.m_loan_capitalized_income_balance (id, version, loan_id, loan_transaction_id, amount, date, unrecognized_amount, charged_off_amount, amount_adjustment, created_by, created_on_utc, last_modified_by, last_modified_on_utc, is_deleted, is_closed) FROM stdin;
\.


ALTER TABLE public.m_loan_capitalized_income_balance 

--
-- Data for Name: m_loan_charge; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_loan_charge 

COPY public.m_loan_charge (id, loan_id, charge_id, is_penalty, charge_time_enum, due_for_collection_as_of_date, charge_calculation_enum, charge_payment_mode_enum, calculation_percentage, calculation_on_amount, charge_amount_or_percentage, amount, amount_paid_derived, amount_waived_derived, amount_writtenoff_derived, amount_outstanding_derived, is_paid_derived, waived, min_cap, max_cap, is_active, external_id, submitted_on_date, created_on_utc, last_modified_on_utc, created_by, last_modified_by, tax_amount) FROM stdin;
\.


ALTER TABLE public.m_loan_charge 

--
-- Data for Name: m_loan_charge_paid_by; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_loan_charge_paid_by 

COPY public.m_loan_charge_paid_by (id, loan_transaction_id, loan_charge_id, amount, installment_number) FROM stdin;
\.


ALTER TABLE public.m_loan_charge_paid_by 

--
-- Data for Name: m_tax_component; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_tax_component 

COPY public.m_tax_component (id, name, percentage, debit_account_type_enum, debit_account_id, credit_account_type_enum, credit_account_id, start_date, createdby_id, created_date, lastmodifiedby_id, lastmodified_date) FROM stdin;
\.


ALTER TABLE public.m_tax_component 

--
-- Data for Name: m_loan_charge_tax_details; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_loan_charge_tax_details 

COPY public.m_loan_charge_tax_details (id, loan_charge_id, tax_component_id, amount) FROM stdin;
\.


ALTER TABLE public.m_loan_charge_tax_details 

--
-- Data for Name: m_loan_collateral; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_loan_collateral 

COPY public.m_loan_collateral (id, loan_id, type_cv_id, value, description) FROM stdin;
\.


ALTER TABLE public.m_loan_collateral 

--
-- Data for Name: m_loan_collateral_management; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_loan_collateral_management 

COPY public.m_loan_collateral_management (id, quantity, loan_id, client_collateral_id, is_released, transaction_id) FROM stdin;
\.


ALTER TABLE public.m_loan_collateral_management 

--
-- Data for Name: m_loan_credit_allocation_rule; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_loan_credit_allocation_rule 

COPY public.m_loan_credit_allocation_rule (id, loan_id, transaction_type, allocation_types, created_by, last_modified_by, created_on_utc, last_modified_on_utc) FROM stdin;
\.


ALTER TABLE public.m_loan_credit_allocation_rule 

--
-- Data for Name: m_loan_delinquency_action; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_loan_delinquency_action 

COPY public.m_loan_delinquency_action (id, loan_id, action, start_date, end_date, created_by, last_modified_by, created_on_utc, last_modified_on_utc) FROM stdin;
\.


ALTER TABLE public.m_loan_delinquency_action 

--
-- Data for Name: m_loan_delinquency_tag_history; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_loan_delinquency_tag_history 

COPY public.m_loan_delinquency_tag_history (id, delinquency_range_id, loan_id, addedon_date, liftedon_date, created_by, created_on_utc, version, last_modified_by, last_modified_on_utc) FROM stdin;
\.


ALTER TABLE public.m_loan_delinquency_tag_history 

--
-- Data for Name: m_loan_disbursement_detail; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_loan_disbursement_detail 

COPY public.m_loan_disbursement_detail (id, loan_id, expected_disburse_date, disbursedon_date, principal, net_disbursal_amount, is_reversed) FROM stdin;
\.


ALTER TABLE public.m_loan_disbursement_detail 

--
-- Data for Name: m_loan_repayment_schedule; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_loan_repayment_schedule 

COPY public.m_loan_repayment_schedule (id, loan_id, fromdate, duedate, installment, principal_amount, principal_completed_derived, principal_writtenoff_derived, interest_amount, interest_completed_derived, interest_writtenoff_derived, interest_waived_derived, accrual_interest_derived, reschedule_interest_portion, fee_charges_amount, fee_charges_completed_derived, fee_charges_writtenoff_derived, fee_charges_waived_derived, accrual_fee_charges_derived, penalty_charges_amount, penalty_charges_completed_derived, penalty_charges_writtenoff_derived, penalty_charges_waived_derived, accrual_penalty_charges_derived, total_paid_in_advance_derived, total_paid_late_derived, completed_derived, obligations_met_on_date, created_by, created_date, lastmodified_date, last_modified_by, recalculated_interest_component, created_on_utc, last_modified_on_utc, is_additional, credits_amount, is_down_payment, credited_interest, credited_fee, credited_penalty, is_re_aged) FROM stdin;
\.


ALTER TABLE public.m_loan_repayment_schedule 

--
-- Data for Name: m_loan_installment_charge; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_loan_installment_charge 

COPY public.m_loan_installment_charge (id, loan_charge_id, loan_schedule_id, due_date, amount, amount_paid_derived, amount_waived_derived, amount_writtenoff_derived, amount_outstanding_derived, is_paid_derived, waived, amount_through_charge_payment) FROM stdin;
\.


ALTER TABLE public.m_loan_installment_charge 

--
-- Data for Name: m_loan_installment_delinquency_tag; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_loan_installment_delinquency_tag 

COPY public.m_loan_installment_delinquency_tag (id, delinquency_range_id, loan_id, installment_id, addedon_date, first_overdue_date, outstanding_amount, liftedon_date, created_by, version, last_modified_by, created_on_utc, last_modified_on_utc) FROM stdin;
\.


ALTER TABLE public.m_loan_installment_delinquency_tag 

--
-- Data for Name: m_loan_interest_recalculation_additional_details; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_loan_interest_recalculation_additional_details 

COPY public.m_loan_interest_recalculation_additional_details (id, loan_repayment_schedule_id, effective_date, amount) FROM stdin;
\.


ALTER TABLE public.m_loan_interest_recalculation_additional_details 

--
-- Data for Name: m_loan_officer_assignment_history; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_loan_officer_assignment_history 

COPY public.m_loan_officer_assignment_history (id, loan_id, loan_officer_id, start_date, end_date, createdby_id, created_date, lastmodified_date, lastmodifiedby_id) FROM stdin;
\.


ALTER TABLE public.m_loan_officer_assignment_history 

--
-- Data for Name: m_loan_originator; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_loan_originator 

COPY public.m_loan_originator (id, external_id, name, status, originator_type_cv_id, channel_type_cv_id, created_on_utc, created_by, last_modified_on_utc, last_modified_by) FROM stdin;
\.


ALTER TABLE public.m_loan_originator 

--
-- Data for Name: m_loan_originator_mapping; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_loan_originator_mapping 

COPY public.m_loan_originator_mapping (id, loan_id, originator_id, created_on_utc, created_by, last_modified_on_utc, last_modified_by) FROM stdin;
\.


ALTER TABLE public.m_loan_originator_mapping 

--
-- Data for Name: m_loan_overdue_installment_charge; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_loan_overdue_installment_charge 

COPY public.m_loan_overdue_installment_charge (id, loan_charge_id, loan_schedule_id, frequency_number) FROM stdin;
\.


ALTER TABLE public.m_loan_overdue_installment_charge 

--
-- Data for Name: m_loan_payment_allocation_rule; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_loan_payment_allocation_rule 

COPY public.m_loan_payment_allocation_rule (id, loan_id, transaction_type, allocation_types, future_installment_allocation_rule, created_by, last_modified_by, created_on_utc, last_modified_on_utc) FROM stdin;
\.


ALTER TABLE public.m_loan_payment_allocation_rule 

--
-- Data for Name: m_loan_product_credit_allocation_rule; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_loan_product_credit_allocation_rule 

COPY public.m_loan_product_credit_allocation_rule (id, loan_product_id, transaction_type, allocation_types, created_by, last_modified_by, created_on_utc, last_modified_on_utc) FROM stdin;
\.


ALTER TABLE public.m_loan_product_credit_allocation_rule 

--
-- Data for Name: m_loan_product_payment_allocation_rule; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_loan_product_payment_allocation_rule 

COPY public.m_loan_product_payment_allocation_rule (id, loan_product_id, transaction_type, allocation_types, future_installment_allocation_rule, created_by, last_modified_by, created_on_utc, last_modified_on_utc) FROM stdin;
\.


ALTER TABLE public.m_loan_product_payment_allocation_rule 

--
-- Data for Name: m_loan_progressive_model; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_loan_progressive_model 

COPY public.m_loan_progressive_model (id, version, loan_id, json_model, business_date, last_modified_on_utc, json_model_version) FROM stdin;
\.


ALTER TABLE public.m_loan_progressive_model 

--
-- Data for Name: m_rate; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_rate 

COPY public.m_rate (id, name, percentage, active, product_apply, created_date, createdby_id, lastmodifiedby_id, lastmodified_date, approve_user) FROM stdin;
\.


ALTER TABLE public.m_rate 

--
-- Data for Name: m_loan_rate; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_loan_rate 

COPY public.m_loan_rate (loan_id, rate_id) FROM stdin;
\.


ALTER TABLE public.m_loan_rate 

--
-- Data for Name: m_loan_reage_parameter; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_loan_reage_parameter 

COPY public.m_loan_reage_parameter (id, frequency_type, number_of_installments, start_date, created_by, last_modified_by, created_on_utc, last_modified_on_utc, loan_transaction_id, frequency_number, interest_handling_type, reage_reason_code_value_id) FROM stdin;
\.


ALTER TABLE public.m_loan_reage_parameter 

--
-- Data for Name: m_loan_reamortization_parameter; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_loan_reamortization_parameter 

COPY public.m_loan_reamortization_parameter (id, loan_transaction_id, interest_handling_type, reamortization_reason_code_value_id, created_by, last_modified_by, created_on_utc, last_modified_on_utc) FROM stdin;
\.


ALTER TABLE public.m_loan_reamortization_parameter 

--
-- Data for Name: m_loan_recalculation_details; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_loan_recalculation_details 

COPY public.m_loan_recalculation_details (id, loan_id, compound_type_enum, reschedule_strategy_enum, rest_frequency_type_enum, rest_frequency_interval, compounding_frequency_type_enum, compounding_frequency_interval, rest_frequency_nth_day_enum, rest_frequency_on_day, rest_frequency_weekday_enum, compounding_frequency_nth_day_enum, compounding_frequency_on_day, is_compounding_to_be_posted_as_transaction, compounding_frequency_weekday_enum, allow_compounding_on_eod, disallow_interest_calc_on_past_due, pre_close_interest_calculation_strategy) FROM stdin;
\.


ALTER TABLE public.m_loan_recalculation_details 

--
-- Data for Name: m_loan_reschedule_request; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_loan_reschedule_request 

COPY public.m_loan_reschedule_request (id, loan_id, status_enum, reschedule_from_installment, reschedule_from_date, recalculate_interest, reschedule_reason_cv_id, reschedule_reason_comment, submitted_on_date, submitted_by_user_id, approved_on_date, approved_by_user_id, rejected_on_date, rejected_by_user_id) FROM stdin;
\.


ALTER TABLE public.m_loan_reschedule_request 

--
-- Data for Name: m_loan_repayment_schedule_history; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_loan_repayment_schedule_history 

COPY public.m_loan_repayment_schedule_history (id, loan_id, loan_reschedule_request_id, fromdate, duedate, installment, principal_amount, interest_amount, fee_charges_amount, penalty_charges_amount, createdby_id, created_date, lastmodified_date, lastmodifiedby_id, version, created_on_utc, last_modified_on_utc) FROM stdin;
\.


ALTER TABLE public.m_loan_repayment_schedule_history 

--
-- Data for Name: m_loan_term_variations; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_loan_term_variations 

COPY public.m_loan_term_variations (id, loan_id, term_type, applicable_date, decimal_value, date_value, is_specific_to_installment, applied_on_loan_status, is_active, parent_id, created_on_utc, created_by, last_modified_by, last_modified_on_utc) FROM stdin;
\.


ALTER TABLE public.m_loan_term_variations 

--
-- Data for Name: m_loan_reschedule_request_term_variations_mapping; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_loan_reschedule_request_term_variations_mapping 

COPY public.m_loan_reschedule_request_term_variations_mapping (id, loan_reschedule_request_id, loan_term_variations_id) FROM stdin;
\.


ALTER TABLE public.m_loan_reschedule_request_term_variations_mapping 

--
-- Data for Name: m_loan_status_change_history; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_loan_status_change_history 

COPY public.m_loan_status_change_history (id, loan_id, status_code, status_change_business_date, created_by, last_modified_by, created_on_utc, last_modified_on_utc) FROM stdin;
\.


ALTER TABLE public.m_loan_status_change_history 

--
-- Data for Name: m_loan_topup; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_loan_topup 

COPY public.m_loan_topup (id, loan_id, closure_loan_id, account_transfer_details_id, topup_amount) FROM stdin;
\.


ALTER TABLE public.m_loan_topup 

--
-- Data for Name: m_loan_tranche_charges; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_loan_tranche_charges 

COPY public.m_loan_tranche_charges (id, loan_id, charge_id) FROM stdin;
\.


ALTER TABLE public.m_loan_tranche_charges 

--
-- Data for Name: m_loan_tranche_disbursement_charge; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_loan_tranche_disbursement_charge 

COPY public.m_loan_tranche_disbursement_charge (id, loan_charge_id, disbursement_detail_id) FROM stdin;
\.


ALTER TABLE public.m_loan_tranche_disbursement_charge 

--
-- Data for Name: m_loan_transaction_relation; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_loan_transaction_relation 

COPY public.m_loan_transaction_relation (id, from_loan_transaction_id, to_loan_transaction_id, relation_type_enum, created_by, created_on_utc, last_modified_by, last_modified_on_utc, to_loan_charge_id) FROM stdin;
\.


ALTER TABLE public.m_loan_transaction_relation 

--
-- Data for Name: m_loan_transaction_repayment_schedule_mapping; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_loan_transaction_repayment_schedule_mapping 

COPY public.m_loan_transaction_repayment_schedule_mapping (id, loan_transaction_id, loan_repayment_schedule_id, amount, principal_portion_derived, interest_portion_derived, fee_charges_portion_derived, penalty_charges_portion_derived) FROM stdin;
\.


ALTER TABLE public.m_loan_transaction_repayment_schedule_mapping 

--
-- Data for Name: m_provision_category; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_provision_category 

COPY public.m_provision_category (id, category_name, description) FROM stdin;
1	STANDARD	Punctual Payment without any dues
2	SUB-STANDARD	Principal and/or Interest overdue by x days
3	DOUBTFUL	Principal and/or Interest overdue by x days and less than y
4	LOSS	Principal and/or Interest overdue by y days
\.


ALTER TABLE public.m_provision_category 

--
-- Data for Name: m_provisioning_criteria; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_provisioning_criteria 

COPY public.m_provisioning_criteria (id, criteria_name, createdby_id, created_date, lastmodifiedby_id, lastmodified_date) FROM stdin;
\.


ALTER TABLE public.m_provisioning_criteria 

--
-- Data for Name: m_provisioning_history; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_provisioning_history 

COPY public.m_provisioning_history (id, journal_entry_created, createdby_id, created_date, lastmodifiedby_id, lastmodified_date) FROM stdin;
\.


ALTER TABLE public.m_provisioning_history 

--
-- Data for Name: m_loanproduct_provisioning_entry; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_loanproduct_provisioning_entry 

COPY public.m_loanproduct_provisioning_entry (id, history_id, criteria_id, currency_code, office_id, product_id, category_id, overdue_in_days, reseve_amount, liability_account, expense_account) FROM stdin;
\.


ALTER TABLE public.m_loanproduct_provisioning_entry 

--
-- Data for Name: m_loanproduct_provisioning_mapping; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_loanproduct_provisioning_mapping 

COPY public.m_loanproduct_provisioning_mapping (id, product_id, criteria_id) FROM stdin;
\.


ALTER TABLE public.m_loanproduct_provisioning_mapping 

--
-- Data for Name: m_mandatory_savings_schedule; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_mandatory_savings_schedule 

COPY public.m_mandatory_savings_schedule (id, savings_account_id, fromdate, duedate, installment, deposit_amount, deposit_amount_completed_derived, total_paid_in_advance_derived, total_paid_late_derived, completed_derived, obligations_met_on_date, created_by, created_date, lastmodified_date, last_modified_by, created_on_utc, last_modified_on_utc) FROM stdin;
\.


ALTER TABLE public.m_mandatory_savings_schedule 

--
-- Data for Name: m_note; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_note 

COPY public.m_note (id, client_id, group_id, loan_id, loan_transaction_id, savings_account_id, savings_account_transaction_id, share_account_id, note_type_enum, note, created_date, created_by, lastmodified_date, last_modified_by, created_on_utc, last_modified_on_utc) FROM stdin;
\.


ALTER TABLE public.m_note 

--
-- Data for Name: m_office_transaction; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_office_transaction 

COPY public.m_office_transaction (id, from_office_id, to_office_id, currency_code, currency_digits, transaction_amount, transaction_date, description) FROM stdin;
\.


ALTER TABLE public.m_office_transaction 

--
-- Data for Name: m_organisation_currency; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_organisation_currency 

COPY public.m_organisation_currency (id, code, decimal_places, currency_multiplesof, name, display_symbol, internationalized_name_code) FROM stdin;
21	USD	2	\N	US Dollar	$	currency.USD
\.


ALTER TABLE public.m_organisation_currency 

--
-- Data for Name: m_password_validation_policy; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_password_validation_policy 

COPY public.m_password_validation_policy (id, regex, description, active, key) FROM stdin;
2	^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?!.*\\s).{6,50}$	Password must be at least 6 characters, no more than 50 characters long, must include at least one upper case letter, one lower case letter, one numeric digit and no space	f	secure
1	^.{1,50}$	Password most be at least 1 character and not more that 50 characters long	f	simple
3	^(?!.*(.)\\1)(?!.*\\s)(?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[^\\w\\s]).{12,50}$	Password must be 12 to 50 characters long, containing at least one uppercase letter, one lowercase letter, one numeric digit, and one special character, with no spaces or consecutive repeating characters	t	strong
\.


ALTER TABLE public.m_password_validation_policy 

--
-- Data for Name: m_permission; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_permission 

COPY public.m_permission (id, "grouping", code, entity_name, action_name, can_maker_checker) FROM stdin;
1	special	ALL_FUNCTIONS	\N	\N	f
2	special	ALL_FUNCTIONS_READ	\N	\N	f
3	special	CHECKER_SUPER_USER	\N	\N	f
4	special	REPORTING_SUPER_USER	\N	\N	f
5	authorisation	READ_PERMISSION	PERMISSION	READ	f
6	authorisation	PERMISSIONS_ROLE	ROLE	PERMISSIONS	f
7	authorisation	CREATE_ROLE	ROLE	CREATE	f
8	authorisation	CREATE_ROLE_CHECKER	ROLE	CREATE_CHECKER	f
9	authorisation	READ_ROLE	ROLE	READ	f
10	authorisation	UPDATE_ROLE	ROLE	UPDATE	f
11	authorisation	UPDATE_ROLE_CHECKER	ROLE	UPDATE_CHECKER	f
12	authorisation	DELETE_ROLE	ROLE	DELETE	f
13	authorisation	DELETE_ROLE_CHECKER	ROLE	DELETE_CHECKER	f
14	authorisation	CREATE_USER	USER	CREATE	f
15	authorisation	CREATE_USER_CHECKER	USER	CREATE_CHECKER	f
16	authorisation	READ_USER	USER	READ	f
17	authorisation	UPDATE_USER	USER	UPDATE	f
18	authorisation	UPDATE_USER_CHECKER	USER	UPDATE_CHECKER	f
19	authorisation	DELETE_USER	USER	DELETE	f
20	authorisation	DELETE_USER_CHECKER	USER	DELETE_CHECKER	f
21	configuration	READ_CONFIGURATION	CONFIGURATION	READ	f
22	configuration	UPDATE_CONFIGURATION	CONFIGURATION	UPDATE	f
23	configuration	UPDATE_CONFIGURATION_CHECKER	CONFIGURATION	UPDATE_CHECKER	f
24	configuration	READ_CODE	CODE	READ	f
25	configuration	CREATE_CODE	CODE	CREATE	f
26	configuration	CREATE_CODE_CHECKER	CODE	CREATE_CHECKER	f
27	configuration	UPDATE_CODE	CODE	UPDATE	f
28	configuration	UPDATE_CODE_CHECKER	CODE	UPDATE_CHECKER	f
29	configuration	DELETE_CODE	CODE	DELETE	f
30	configuration	DELETE_CODE_CHECKER	CODE	DELETE_CHECKER	f
31	configuration	READ_CODEVALUE	CODEVALUE	READ	f
32	configuration	CREATE_CODEVALUE	CODEVALUE	CREATE	f
33	configuration	CREATE_CODEVALUE_CHECKER	CODEVALUE	CREATE_CHECKER	f
34	configuration	UPDATE_CODEVALUE	CODEVALUE	UPDATE	f
35	configuration	UPDATE_CODEVALUE_CHECKER	CODEVALUE	UPDATE_CHECKER	f
36	configuration	DELETE_CODEVALUE	CODEVALUE	DELETE	f
37	configuration	DELETE_CODEVALUE_CHECKER	CODEVALUE	DELETE_CHECKER	f
38	configuration	READ_CURRENCY	CURRENCY	READ	f
39	configuration	UPDATE_CURRENCY	CURRENCY	UPDATE	f
40	configuration	UPDATE_CURRENCY_CHECKER	CURRENCY	UPDATE_CHECKER	f
41	configuration	UPDATE_PERMISSION	PERMISSION	UPDATE	f
42	configuration	UPDATE_PERMISSION_CHECKER	PERMISSION	UPDATE_CHECKER	f
43	configuration	READ_DATATABLE	DATATABLE	READ	f
44	configuration	REGISTER_DATATABLE	DATATABLE	REGISTER	f
45	configuration	REGISTER_DATATABLE_CHECKER	DATATABLE	REGISTER_CHECKER	f
46	configuration	DEREGISTER_DATATABLE	DATATABLE	DEREGISTER	f
47	configuration	DEREGISTER_DATATABLE_CHECKER	DATATABLE	DEREGISTER_CHECKER	f
48	configuration	READ_AUDIT	AUDIT	READ	f
49	configuration	CREATE_CALENDAR	CALENDAR	CREATE	f
50	configuration	READ_CALENDAR	CALENDAR	READ	f
51	configuration	UPDATE_CALENDAR	CALENDAR	UPDATE	f
52	configuration	DELETE_CALENDAR	CALENDAR	DELETE	f
53	configuration	CREATE_CALENDAR_CHECKER	CALENDAR	CREATE_CHECKER	f
54	configuration	UPDATE_CALENDAR_CHECKER	CALENDAR	UPDATE_CHECKER	f
55	configuration	DELETE_CALENDAR_CHECKER	CALENDAR	DELETE_CHECKER	f
57	organisation	READ_CHARGE	CHARGE	READ	f
58	organisation	CREATE_CHARGE	CHARGE	CREATE	f
59	organisation	CREATE_CHARGE_CHECKER	CHARGE	CREATE_CHECKER	f
60	organisation	UPDATE_CHARGE	CHARGE	UPDATE	f
61	organisation	UPDATE_CHARGE_CHECKER	CHARGE	UPDATE_CHECKER	f
62	organisation	DELETE_CHARGE	CHARGE	DELETE	f
63	organisation	DELETE_CHARGE_CHECKER	CHARGE	DELETE_CHECKER	f
64	organisation	READ_FUND	FUND	READ	f
65	organisation	CREATE_FUND	FUND	CREATE	f
66	organisation	CREATE_FUND_CHECKER	FUND	CREATE_CHECKER	f
67	organisation	UPDATE_FUND	FUND	UPDATE	f
68	organisation	UPDATE_FUND_CHECKER	FUND	UPDATE_CHECKER	f
69	organisation	DELETE_FUND	FUND	DELETE	f
70	organisation	DELETE_FUND_CHECKER	FUND	DELETE_CHECKER	f
71	organisation	READ_LOANPRODUCT	LOANPRODUCT	READ	f
72	organisation	CREATE_LOANPRODUCT	LOANPRODUCT	CREATE	f
73	organisation	CREATE_LOANPRODUCT_CHECKER	LOANPRODUCT	CREATE_CHECKER	f
74	organisation	UPDATE_LOANPRODUCT	LOANPRODUCT	UPDATE	f
75	organisation	UPDATE_LOANPRODUCT_CHECKER	LOANPRODUCT	UPDATE_CHECKER	f
76	organisation	DELETE_LOANPRODUCT	LOANPRODUCT	DELETE	f
77	organisation	DELETE_LOANPRODUCT_CHECKER	LOANPRODUCT	DELETE_CHECKER	f
78	organisation	READ_OFFICE	OFFICE	READ	f
79	organisation	CREATE_OFFICE	OFFICE	CREATE	f
80	organisation	CREATE_OFFICE_CHECKER	OFFICE	CREATE_CHECKER	f
81	organisation	UPDATE_OFFICE	OFFICE	UPDATE	f
82	organisation	UPDATE_OFFICE_CHECKER	OFFICE	UPDATE_CHECKER	f
83	organisation	READ_OFFICETRANSACTION	OFFICETRANSACTION	READ	f
84	organisation	DELETE_OFFICE_CHECKER	OFFICE	DELETE_CHECKER	f
85	organisation	CREATE_OFFICETRANSACTION	OFFICETRANSACTION	CREATE	f
86	organisation	CREATE_OFFICETRANSACTION_CHECKER	OFFICETRANSACTION	CREATE_CHECKER	f
87	organisation	DELETE_OFFICETRANSACTION	OFFICETRANSACTION	DELETE	f
88	organisation	DELETE_OFFICETRANSACTION_CHECKER	OFFICETRANSACTION	DELETE_CHECKER	f
89	organisation	READ_STAFF	STAFF	READ	f
90	organisation	CREATE_STAFF	STAFF	CREATE	f
91	organisation	CREATE_STAFF_CHECKER	STAFF	CREATE_CHECKER	f
92	organisation	UPDATE_STAFF	STAFF	UPDATE	f
93	organisation	UPDATE_STAFF_CHECKER	STAFF	UPDATE_CHECKER	f
94	organisation	DELETE_STAFF	STAFF	DELETE	f
95	organisation	DELETE_STAFF_CHECKER	STAFF	DELETE_CHECKER	f
96	organisation	READ_SAVINGSPRODUCT	SAVINGSPRODUCT	READ	f
97	organisation	CREATE_SAVINGSPRODUCT	SAVINGSPRODUCT	CREATE	f
98	organisation	CREATE_SAVINGSPRODUCT_CHECKER	SAVINGSPRODUCT	CREATE_CHECKER	f
99	organisation	UPDATE_SAVINGSPRODUCT	SAVINGSPRODUCT	UPDATE	f
100	organisation	UPDATE_SAVINGSPRODUCT_CHECKER	SAVINGSPRODUCT	UPDATE_CHECKER	f
101	organisation	DELETE_SAVINGSPRODUCT	SAVINGSPRODUCT	DELETE	f
102	organisation	DELETE_SAVINGSPRODUCT_CHECKER	SAVINGSPRODUCT	DELETE_CHECKER	f
103	portfolio	READ_LOAN	LOAN	READ	f
104	portfolio	CREATE_LOAN	LOAN	CREATE	f
105	portfolio	CREATE_LOAN_CHECKER	LOAN	CREATE_CHECKER	f
106	portfolio	UPDATE_LOAN	LOAN	UPDATE	f
107	portfolio	UPDATE_LOAN_CHECKER	LOAN	UPDATE_CHECKER	f
108	portfolio	DELETE_LOAN	LOAN	DELETE	f
109	portfolio	DELETE_LOAN_CHECKER	LOAN	DELETE_CHECKER	f
110	portfolio	READ_CLIENT	CLIENT	READ	f
111	portfolio	CREATE_CLIENT	CLIENT	CREATE	f
112	portfolio	CREATE_CLIENT_CHECKER	CLIENT	CREATE_CHECKER	f
113	portfolio	UPDATE_CLIENT	CLIENT	UPDATE	f
114	portfolio	UPDATE_CLIENT_CHECKER	CLIENT	UPDATE_CHECKER	f
115	portfolio	DELETE_CLIENT	CLIENT	DELETE	f
116	portfolio	DELETE_CLIENT_CHECKER	CLIENT	DELETE_CHECKER	f
117	portfolio	READ_CLIENTIMAGE	CLIENTIMAGE	READ	f
118	portfolio	CREATE_CLIENTIMAGE	CLIENTIMAGE	CREATE	f
119	portfolio	CREATE_CLIENTIMAGE_CHECKER	CLIENTIMAGE	CREATE_CHECKER	f
120	portfolio	DELETE_CLIENTIMAGE	CLIENTIMAGE	DELETE	f
121	portfolio	DELETE_CLIENTIMAGE_CHECKER	CLIENTIMAGE	DELETE_CHECKER	f
122	portfolio	READ_CLIENTNOTE	CLIENTNOTE	READ	f
123	portfolio	CREATE_CLIENTNOTE	CLIENTNOTE	CREATE	f
124	portfolio	CREATE_CLIENTNOTE_CHECKER	CLIENTNOTE	CREATE_CHECKER	f
125	portfolio	UPDATE_CLIENTNOTE	CLIENTNOTE	UPDATE	f
126	portfolio	UPDATE_CLIENTNOTE_CHECKER	CLIENTNOTE	UPDATE_CHECKER	f
127	portfolio	DELETE_CLIENTNOTE	CLIENTNOTE	DELETE	f
128	portfolio	DELETE_CLIENTNOTE_CHECKER	CLIENTNOTE	DELETE_CHECKER	f
129	portfolio_group	READ_GROUPNOTE	GROUPNOTE	READ	f
130	portfolio_group	CREATE_GROUPNOTE	GROUPNOTE	CREATE	f
131	portfolio_group	UPDATE_GROUPNOTE	GROUPNOTE	UPDATE	f
132	portfolio_group	DELETE_GROUPNOTE	GROUPNOTE	DELETE	f
133	portfolio_group	CREATE_GROUPNOTE_CHECKER	GROUPNOTE	CREATE_CHECKER	f
134	portfolio_group	UPDATE_GROUPNOTE_CHECKER	GROUPNOTE	UPDATE_CHECKER	f
135	portfolio_group	DELETE_GROUPNOTE_CHECKER	GROUPNOTE	DELETE_CHECKER	f
136	portfolio	READ_LOANNOTE	LOANNOTE	READ	f
137	portfolio	CREATE_LOANNOTE	LOANNOTE	CREATE	f
138	portfolio	UPDATE_LOANNOTE	LOANNOTE	UPDATE	f
139	portfolio	DELETE_LOANNOTE	LOANNOTE	DELETE	f
140	portfolio	CREATE_LOANNOTE_CHECKER	LOANNOTE	CREATE_CHECKER	f
141	portfolio	UPDATE_LOANNOTE_CHECKER	LOANNOTE	UPDATE_CHECKER	f
142	portfolio	DELETE_LOANNOTE_CHECKER	LOANNOTE	DELETE_CHECKER	f
143	portfolio	READ_LOANTRANSACTIONNOTE	LOANTRANSACTIONNOTE	READ	f
144	portfolio	CREATE_LOANTRANSACTIONNOTE	LOANTRANSACTIONNOTE	CREATE	f
145	portfolio	UPDATE_LOANTRANSACTIONNOTE	LOANTRANSACTIONNOTE	UPDATE	f
146	portfolio	DELETE_LOANTRANSACTIONNOTE	LOANTRANSACTIONNOTE	DELETE	f
147	portfolio	CREATE_LOANTRANSACTIONNOTE_CHECKER	LOANTRANSACTIONNOTE	CREATE_CHECKER	f
148	portfolio	UPDATE_LOANTRANSACTIONNOTE_CHECKER	LOANTRANSACTIONNOTE	UPDATE_CHECKER	f
149	portfolio	DELETE_LOANTRANSACTIONNOTE_CHECKER	LOANTRANSACTIONNOTE	DELETE_CHECKER	f
150	portfolio	READ_SAVINGNOTE	SAVINGNOTE	READ	f
151	portfolio	CREATE_SAVINGNOTE	SAVINGNOTE	CREATE	f
152	portfolio	UPDATE_SAVINGNOTE	SAVINGNOTE	UPDATE	f
153	portfolio	DELETE_SAVINGNOTE	SAVINGNOTE	DELETE	f
154	portfolio	CREATE_SAVINGNOTE_CHECKER	SAVINGNOTE	CREATE_CHECKER	f
155	portfolio	UPDATE_SAVINGNOTE_CHECKER	SAVINGNOTE	UPDATE_CHECKER	f
156	portfolio	DELETE_SAVINGNOTE_CHECKER	SAVINGNOTE	DELETE_CHECKER	f
157	portfolio	READ_CLIENTIDENTIFIER	CLIENTIDENTIFIER	READ	f
158	portfolio	CREATE_CLIENTIDENTIFIER	CLIENTIDENTIFIER	CREATE	f
159	portfolio	CREATE_CLIENTIDENTIFIER_CHECKER	CLIENTIDENTIFIER	CREATE_CHECKER	f
160	portfolio	UPDATE_CLIENTIDENTIFIER	CLIENTIDENTIFIER	UPDATE	f
161	portfolio	UPDATE_CLIENTIDENTIFIER_CHECKER	CLIENTIDENTIFIER	UPDATE_CHECKER	f
162	portfolio	DELETE_CLIENTIDENTIFIER	CLIENTIDENTIFIER	DELETE	f
163	portfolio	DELETE_CLIENTIDENTIFIER_CHECKER	CLIENTIDENTIFIER	DELETE_CHECKER	f
164	portfolio	READ_DOCUMENT	DOCUMENT	READ	f
165	portfolio	CREATE_DOCUMENT	DOCUMENT	CREATE	f
166	portfolio	CREATE_DOCUMENT_CHECKER	DOCUMENT	CREATE_CHECKER	f
167	portfolio	UPDATE_DOCUMENT	DOCUMENT	UPDATE	f
168	portfolio	UPDATE_DOCUMENT_CHECKER	DOCUMENT	UPDATE_CHECKER	f
169	portfolio	DELETE_DOCUMENT	DOCUMENT	DELETE	f
170	portfolio	DELETE_DOCUMENT_CHECKER	DOCUMENT	DELETE_CHECKER	f
171	portfolio_group	READ_GROUP	GROUP	READ	f
172	portfolio_group	CREATE_GROUP	GROUP	CREATE	f
173	portfolio_group	CREATE_GROUP_CHECKER	GROUP	CREATE_CHECKER	f
174	portfolio_group	UPDATE_GROUP	GROUP	UPDATE	f
175	portfolio_group	UPDATE_GROUP_CHECKER	GROUP	UPDATE_CHECKER	f
176	portfolio_group	DELETE_GROUP	GROUP	DELETE	f
177	portfolio_group	DELETE_GROUP_CHECKER	GROUP	DELETE_CHECKER	f
178	portfolio_group	UNASSIGNSTAFF_GROUP	GROUP	UNASSIGNSTAFF	f
179	portfolio_group	UNASSIGNSTAFF_GROUP_CHECKER	GROUP	UNASSIGNSTAFF_CHECKER	f
180	portfolio	CREATE_LOANCHARGE	LOANCHARGE	CREATE	f
181	portfolio	CREATE_LOANCHARGE_CHECKER	LOANCHARGE	CREATE_CHECKER	f
182	portfolio	UPDATE_LOANCHARGE	LOANCHARGE	UPDATE	f
183	portfolio	UPDATE_LOANCHARGE_CHECKER	LOANCHARGE	UPDATE_CHECKER	f
184	portfolio	DELETE_LOANCHARGE	LOANCHARGE	DELETE	f
185	portfolio	DELETE_LOANCHARGE_CHECKER	LOANCHARGE	DELETE_CHECKER	f
186	portfolio	WAIVE_LOANCHARGE	LOANCHARGE	WAIVE	f
187	portfolio	WAIVE_LOANCHARGE_CHECKER	LOANCHARGE	WAIVE_CHECKER	f
188	portfolio	READ_SAVINGSACCOUNT	SAVINGSACCOUNT	READ	f
189	portfolio	CREATE_SAVINGSACCOUNT	SAVINGSACCOUNT	CREATE	f
190	portfolio	CREATE_SAVINGSACCOUNT_CHECKER	SAVINGSACCOUNT	CREATE_CHECKER	f
191	portfolio	UPDATE_SAVINGSACCOUNT	SAVINGSACCOUNT	UPDATE	f
192	portfolio	UPDATE_SAVINGSACCOUNT_CHECKER	SAVINGSACCOUNT	UPDATE_CHECKER	f
193	portfolio	DELETE_SAVINGSACCOUNT	SAVINGSACCOUNT	DELETE	f
194	portfolio	DELETE_SAVINGSACCOUNT_CHECKER	SAVINGSACCOUNT	DELETE_CHECKER	f
195	portfolio	READ_GUARANTOR	GUARANTOR	READ	f
196	portfolio	CREATE_GUARANTOR	GUARANTOR	CREATE	f
197	portfolio	CREATE_GUARANTOR_CHECKER	GUARANTOR	CREATE_CHECKER	f
198	portfolio	UPDATE_GUARANTOR	GUARANTOR	UPDATE	f
199	portfolio	UPDATE_GUARANTOR_CHECKER	GUARANTOR	UPDATE_CHECKER	f
200	portfolio	DELETE_GUARANTOR	GUARANTOR	DELETE	f
201	portfolio	DELETE_GUARANTOR_CHECKER	GUARANTOR	DELETE_CHECKER	f
202	portfolio	READ_COLLATERAL	COLLATERAL	READ	f
203	portfolio	CREATE_COLLATERAL	COLLATERAL	CREATE	f
204	portfolio	UPDATE_COLLATERAL	COLLATERAL	UPDATE	f
205	portfolio	DELETE_COLLATERAL	COLLATERAL	DELETE	f
206	portfolio	CREATE_COLLATERAL_CHECKER	COLLATERAL	CREATE_CHECKER	f
207	portfolio	UPDATE_COLLATERAL_CHECKER	COLLATERAL	UPDATE_CHECKER	f
208	portfolio	DELETE_COLLATERAL_CHECKER	COLLATERAL	DELETE_CHECKER	f
209	transaction_loan	APPROVE_LOAN	LOAN	APPROVE	f
211	transaction_loan	REJECT_LOAN	LOAN	REJECT	f
213	transaction_loan	WITHDRAW_LOAN	LOAN	WITHDRAW	f
215	transaction_loan	APPROVALUNDO_LOAN	LOAN	APPROVALUNDO	f
216	transaction_loan	DISBURSE_LOAN	LOAN	DISBURSE	f
218	transaction_loan	DISBURSALUNDO_LOAN	LOAN	DISBURSALUNDO	f
219	transaction_loan	REPAYMENT_LOAN	LOAN	REPAYMENT	f
221	transaction_loan	ADJUST_LOAN	LOAN	ADJUST	f
222	transaction_loan	WAIVEINTERESTPORTION_LOAN	LOAN	WAIVEINTERESTPORTION	f
223	transaction_loan	WRITEOFF_LOAN	LOAN	WRITEOFF	f
224	transaction_loan	CLOSE_LOAN	LOAN	CLOSE	f
225	transaction_loan	CLOSEASRESCHEDULED_LOAN	LOAN	CLOSEASRESCHEDULED	f
226	transaction_loan	UPDATELOANOFFICER_LOAN	LOAN	UPDATELOANOFFICER	f
227	transaction_loan	UPDATELOANOFFICER_LOAN_CHECKER	LOAN	UPDATELOANOFFICER_CHECKER	f
228	transaction_loan	REMOVELOANOFFICER_LOAN	LOAN	REMOVELOANOFFICER	f
229	transaction_loan	REMOVELOANOFFICER_LOAN_CHECKER	LOAN	REMOVELOANOFFICER_CHECKER	f
230	transaction_loan	BULKREASSIGN_LOAN	LOAN	BULKREASSIGN	f
231	transaction_loan	BULKREASSIGN_LOAN_CHECKER	LOAN	BULKREASSIGN_CHECKER	f
232	transaction_loan	APPROVE_LOAN_CHECKER	LOAN	APPROVE_CHECKER	f
234	transaction_loan	REJECT_LOAN_CHECKER	LOAN	REJECT_CHECKER	f
236	transaction_loan	WITHDRAW_LOAN_CHECKER	LOAN	WITHDRAW_CHECKER	f
238	transaction_loan	APPROVALUNDO_LOAN_CHECKER	LOAN	APPROVALUNDO_CHECKER	f
239	transaction_loan	DISBURSE_LOAN_CHECKER	LOAN	DISBURSE_CHECKER	f
241	transaction_loan	DISBURSALUNDO_LOAN_CHECKER	LOAN	DISBURSALUNDO_CHECKER	f
242	transaction_loan	REPAYMENT_LOAN_CHECKER	LOAN	REPAYMENT_CHECKER	f
244	transaction_loan	ADJUST_LOAN_CHECKER	LOAN	ADJUST_CHECKER	f
245	transaction_loan	WAIVEINTERESTPORTION_LOAN_CHECKER	LOAN	WAIVEINTERESTPORTION_CHECKER	f
246	transaction_loan	WRITEOFF_LOAN_CHECKER	LOAN	WRITEOFF_CHECKER	f
247	transaction_loan	CLOSE_LOAN_CHECKER	LOAN	CLOSE_CHECKER	f
248	transaction_loan	CLOSEASRESCHEDULED_LOAN_CHECKER	LOAN	CLOSEASRESCHEDULED_CHECKER	f
249	transaction_loan	UNDO_WAIVECHARGE	WAIVECHARGE	UNDO	f
250	transaction_savings	DEPOSIT_SAVINGSACCOUNT	SAVINGSACCOUNT	DEPOSIT	f
251	transaction_savings	DEPOSIT_SAVINGSACCOUNT_CHECKER	SAVINGSACCOUNT	DEPOSIT_CHECKER	f
252	transaction_savings	WITHDRAWAL_SAVINGSACCOUNT	SAVINGSACCOUNT	WITHDRAWAL	f
253	transaction_savings	WITHDRAWAL_SAVINGSACCOUNT_CHECKER	SAVINGSACCOUNT	WITHDRAWAL_CHECKER	f
254	transaction_savings	ACTIVATE_SAVINGSACCOUNT	SAVINGSACCOUNT	ACTIVATE	f
255	transaction_savings	ACTIVATE_SAVINGSACCOUNT_CHECKER	SAVINGSACCOUNT	ACTIVATE_CHECKER	f
256	transaction_savings	CALCULATEINTEREST_SAVINGSACCOUNT	SAVINGSACCOUNT	CALCULATEINTEREST	f
257	transaction_savings	CALCULATEINTEREST_SAVINGSACCOUNT_CHECKER	SAVINGSACCOUNT	CALCULATEINTEREST_CHECKER	f
258	accounting	CREATE_GLACCOUNT	GLACCOUNT	CREATE	f
259	accounting	UPDATE_GLACCOUNT	GLACCOUNT	UPDATE	f
260	accounting	DELETE_GLACCOUNT	GLACCOUNT	DELETE	f
261	accounting	CREATE_GLCLOSURE	GLCLOSURE	CREATE	f
262	accounting	UPDATE_GLCLOSURE	GLCLOSURE	UPDATE	f
263	accounting	DELETE_GLCLOSURE	GLCLOSURE	DELETE	f
264	accounting	CREATE_JOURNALENTRY	JOURNALENTRY	CREATE	f
265	accounting	REVERSE_JOURNALENTRY	JOURNALENTRY	REVERSE	f
266	report	READ_Active Loans - Details	Active Loans - Details	READ	f
267	report	READ_Active Loans - Summary	Active Loans - Summary	READ	f
268	report	READ_Active Loans by Disbursal Period	Active Loans by Disbursal Period	READ	f
269	report	READ_Active Loans in last installment	Active Loans in last installment	READ	f
270	report	READ_Active Loans in last installment Summary	Active Loans in last installment Summary	READ	f
271	report	READ_Active Loans Passed Final Maturity	Active Loans Passed Final Maturity	READ	f
272	report	READ_Active Loans Passed Final Maturity Summary	Active Loans Passed Final Maturity Summary	READ	f
273	report	READ_Aging Detail	Aging Detail	READ	f
274	report	READ_Aging Summary (Arrears in Months)	Aging Summary (Arrears in Months)	READ	f
275	report	READ_Aging Summary (Arrears in Weeks)	Aging Summary (Arrears in Weeks)	READ	f
276	report	READ_Balance Sheet	Balance Sheet	READ	f
277	report	READ_Branch Expected Cash Flow	Branch Expected Cash Flow	READ	f
278	report	READ_Client Listing	Client Listing	READ	f
279	report	READ_Client Loans Listing	Client Loans Listing	READ	f
280	report	READ_Expected Payments By Date - Basic	Expected Payments By Date - Basic	READ	f
281	report	READ_Expected Payments By Date - Formatted	Expected Payments By Date - Formatted	READ	f
282	report	READ_Funds Disbursed Between Dates Summary	Funds Disbursed Between Dates Summary	READ	f
283	report	READ_Funds Disbursed Between Dates Summary by Office	Funds Disbursed Between Dates Summary by Office	READ	f
284	report	READ_Income Statement	Income Statement	READ	f
285	report	READ_Loan Account Schedule	Loan Account Schedule	READ	f
286	report	READ_Loans Awaiting Disbursal	Loans Awaiting Disbursal	READ	f
287	report	READ_Loans Awaiting Disbursal Summary	Loans Awaiting Disbursal Summary	READ	f
288	report	READ_Loans Awaiting Disbursal Summary by Month	Loans Awaiting Disbursal Summary by Month	READ	f
289	report	READ_Loans Pending Approval	Loans Pending Approval	READ	f
290	report	READ_Obligation Met Loans Details	Obligation Met Loans Details	READ	f
291	report	READ_Obligation Met Loans Summary	Obligation Met Loans Summary	READ	f
292	report	READ_Portfolio at Risk	Portfolio at Risk	READ	f
293	report	READ_Portfolio at Risk by Branch	Portfolio at Risk by Branch	READ	f
294	report	READ_Rescheduled Loans	Rescheduled Loans	READ	f
295	report	READ_Trial Balance	Trial Balance	READ	f
296	report	READ_Written-Off Loans	Written-Off Loans	READ	f
297	transaction_savings	POSTINTEREST_SAVINGSACCOUNT	SAVINGSACCOUNT	POSTINTEREST	t
298	transaction_savings	POSTINTEREST_SAVINGSACCOUNT_CHECKER	SAVINGSACCOUNT	POSTINTEREST_CHECKER	f
299	portfolio_center	READ_CENTER	CENTER	READ	f
300	portfolio_center	CREATE_CENTER	CENTER	CREATE	f
301	portfolio_center	CREATE_CENTER_CHECKER	CENTER	CREATE_CHECKER	f
302	portfolio_center	UPDATE_CENTER	CENTER	UPDATE	f
303	portfolio_center	UPDATE_CENTER_CHECKER	CENTER	UPDATE_CHECKER	f
304	portfolio_center	DELETE_CENTER	CENTER	DELETE	f
305	portfolio_center	DELETE_CENTER_CHECKER	CENTER	DELETE_CHECKER	f
306	configuration	READ_REPORT	REPORT	READ	f
307	configuration	CREATE_REPORT	REPORT	CREATE	f
308	configuration	CREATE_REPORT_CHECKER	REPORT	CREATE_CHECKER	f
309	configuration	UPDATE_REPORT	REPORT	UPDATE	f
310	configuration	UPDATE_REPORT_CHECKER	REPORT	UPDATE_CHECKER	f
311	configuration	DELETE_REPORT	REPORT	DELETE	f
312	configuration	DELETE_REPORT_CHECKER	REPORT	DELETE_CHECKER	f
313	portfolio	ACTIVATE_CLIENT	CLIENT	ACTIVATE	t
314	portfolio	ACTIVATE_CLIENT_CHECKER	CLIENT	ACTIVATE_CHECKER	f
315	portfolio_center	ACTIVATE_CENTER	CENTER	ACTIVATE	t
316	portfolio_center	ACTIVATE_CENTER_CHECKER	CENTER	ACTIVATE_CHECKER	f
317	portfolio_group	ACTIVATE_GROUP	GROUP	ACTIVATE	t
318	portfolio_group	ACTIVATE_GROUP_CHECKER	GROUP	ACTIVATE_CHECKER	f
319	portfolio_group	ASSOCIATECLIENTS_GROUP	GROUP	ASSOCIATECLIENTS	f
320	portfolio_group	DISASSOCIATECLIENTS_GROUP	GROUP	DISASSOCIATECLIENTS	f
321	portfolio_group	SAVECOLLECTIONSHEET_GROUP	GROUP	SAVECOLLECTIONSHEET	f
322	portfolio_center	SAVECOLLECTIONSHEET_CENTER	CENTER	SAVECOLLECTIONSHEET	f
324	accounting	DELETE_ACCOUNTINGRULE	ACCOUNTINGRULE	DELETE	f
325	accounting	CREATE_ACCOUNTINGRULE	ACCOUNTINGRULE	CREATE	f
326	accounting	UPDATE_ACCOUNTINGRULE	ACCOUNTINGRULE	UPDATE	f
327	report	READ_GroupSummaryCounts	GroupSummaryCounts	READ	f
328	report	READ_GroupSummaryAmounts	GroupSummaryAmounts	READ	f
329	configuration	CREATE_DATATABLE	DATATABLE	CREATE	f
330	configuration	CREATE_DATATABLE_CHECKER	DATATABLE	CREATE_CHECKER	f
331	configuration	UPDATE_DATATABLE	DATATABLE	UPDATE	f
332	configuration	UPDATE_DATATABLE_CHECKER	DATATABLE	UPDATE_CHECKER	f
333	configuration	DELETE_DATATABLE	DATATABLE	DELETE	f
334	configuration	DELETE_DATATABLE_CHECKER	DATATABLE	DELETE_CHECKER	f
335	organisation	CREATE_HOLIDAY	HOLIDAY	CREATE	f
336	portfolio_group	ASSIGNROLE_GROUP	GROUP	ASSIGNROLE	f
337	portfolio_group	UNASSIGNROLE_GROUP	GROUP	UNASSIGNROLE	f
338	portfolio_group	UPDATEROLE_GROUP	GROUP	UPDATEROLE	f
347	report	READ_TxnRunningBalances	TxnRunningBalances	READ	f
348	portfolio	UNASSIGNSTAFF_CLIENT	CLIENT	UNASSIGNSTAFF	f
349	portfolio	ASSIGNSTAFF_CLIENT	CLIENT	ASSIGNSTAFF	f
350	portfolio	CLOSE_CLIENT	CLIENT	CLOSE	t
351	report	READ_FieldAgentStats	FieldAgentStats	READ	f
352	report	READ_FieldAgentPrograms	FieldAgentPrograms	READ	f
353	report	READ_ProgramDetails	ProgramDetails	READ	f
354	report	READ_ChildrenStaffList	ChildrenStaffList	READ	f
355	report	READ_CoordinatorStats	CoordinatorStats	READ	f
356	report	READ_BranchManagerStats	BranchManagerStats	READ	f
357	report	READ_ProgramDirectorStats	ProgramDirectorStats	READ	f
358	report	READ_ProgramStats	ProgramStats	READ	f
359	transaction_savings	APPROVE_SAVINGSACCOUNT	SAVINGSACCOUNT	APPROVE	t
360	transaction_savings	REJECT_SAVINGSACCOUNT	SAVINGSACCOUNT	REJECT	t
361	transaction_savings	WITHDRAW_SAVINGSACCOUNT	SAVINGSACCOUNT	WITHDRAW	t
362	transaction_savings	APPROVALUNDO_SAVINGSACCOUNT	SAVINGSACCOUNT	APPROVALUNDO	t
363	transaction_savings	CLOSE_SAVINGSACCOUNT	SAVINGSACCOUNT	CLOSE	t
364	transaction_savings	APPROVE_SAVINGSACCOUNT_CHECKER	SAVINGSACCOUNT	APPROVE_CHECKER	f
365	transaction_savings	REJECT_SAVINGSACCOUNT_CHECKER	SAVINGSACCOUNT	REJECT_CHECKER	f
366	transaction_savings	WITHDRAW_SAVINGSACCOUNT_CHECKER	SAVINGSACCOUNT	WITHDRAW_CHECKER	f
367	transaction_savings	APPROVALUNDO_SAVINGSACCOUNT_CHECKER	SAVINGSACCOUNT	APPROVALUNDO_CHECKER	f
368	transaction_savings	CLOSE_SAVINGSACCOUNT_CHECKER	SAVINGSACCOUNT	CLOSE_CHECKER	f
369	transaction_savings	UNDOTRANSACTION_SAVINGSACCOUNT	SAVINGSACCOUNT	UNDOTRANSACTION	t
370	transaction_savings	UNDOTRANSACTION_SAVINGSACCOUNT_CHECKER	SAVINGSACCOUNT	UNDOTRANSACTION_CHECKER	f
371	portfolio	CREATE_PRODUCTMIX	PRODUCTMIX	CREATE	f
372	portfolio	UPDATE_PRODUCTMIX	PRODUCTMIX	UPDATE	f
373	portfolio	DELETE_PRODUCTMIX	PRODUCTMIX	DELETE	f
374	jobs	UPDATE_SCHEDULER	SCHEDULER	UPDATE	f
375	transaction_savings	APPLYANNUALFEE_SAVINGSACCOUNT	SAVINGSACCOUNT	APPLYANNUALFEE	t
376	transaction_savings	APPLYANNUALFEE_SAVINGSACCOUNT_CHECKER	SAVINGSACCOUNT	APPLYANNUALFEE_CHECKER	f
377	portfolio_group	ASSIGNSTAFF_GROUP	GROUP	ASSIGNSTAFF	f
378	transaction_savings	READ_ACCOUNTTRANSFER	ACCOUNTTRANSFER	READ	f
379	transaction_savings	CREATE_ACCOUNTTRANSFER	ACCOUNTTRANSFER	CREATE	t
380	transaction_savings	CREATE_ACCOUNTTRANSFER_CHECKER	ACCOUNTTRANSFER	CREATE_CHECKER	f
381	transaction_savings	ADJUSTTRANSACTION_SAVINGSACCOUNT	SAVINGSACCOUNT	ADJUSTTRANSACTION	f
382	portfolio	CREATE_MEETING	MEETING	CREATE	f
383	portfolio	UPDATE_MEETING	MEETING	UPDATE	f
384	portfolio	DELETE_MEETING	MEETING	DELETE	f
385	portfolio	SAVEORUPDATEATTENDANCE_MEETING	MEETING	SAVEORUPDATEATTENDANCE	f
386	portfolio_group	TRANSFERCLIENTS_GROUP	GROUP	TRANSFERCLIENTS	f
387	portfolio_group	TRANSFERCLIENTS_GROUP_CHECKER	GROUP	TRANSFERCLIENTS_CHECKER	f
390	portfolio	PROPOSETRANSFER_CLIENT	CLIENT	PROPOSETRANSFER	f
391	portfolio	PROPOSETRANSFER_CLIENT_CHECKER	CLIENT	PROPOSETRANSFER_CHECKER	f
392	portfolio	ACCEPTTRANSFER_CLIENT	CLIENT	ACCEPTTRANSFER	f
393	portfolio	ACCEPTTRANSFER_CLIENT_CHECKER	CLIENT	ACCEPTTRANSFER_CHECKER	f
394	portfolio	REJECTTRANSFER_CLIENT	CLIENT	REJECTTRANSFER	f
395	portfolio	REJECTTRANSFER_CLIENT_CHECKER	CLIENT	REJECTTRANSFER_CHECKER	f
396	portfolio	WITHDRAWTRANSFER_CLIENT	CLIENT	WITHDRAWTRANSFER	f
397	portfolio	WITHDRAWTRANSFER_CLIENT_CHECKER	CLIENT	WITHDRAWTRANSFER_CHECKER	f
398	portfolio	CLOSE_GROUP	GROUP	CLOSE	t
399	portfolio	CLOSE_CENTER	CENTER	CLOSE	t
400	xbrlmapping	UPDATE_XBRLMAPPING	XBRLMAPPING	UPDATE	f
401	configuration	READ_CACHE	CACHE	READ	f
402	configuration	UPDATE_CACHE	CACHE	UPDATE	f
403	transaction_loan	PAY_LOANCHARGE	LOANCHARGE	PAY	f
404	portfolio	CREATE_SAVINGSACCOUNTCHARGE	SAVINGSACCOUNTCHARGE	CREATE	f
405	portfolio	CREATE_SAVINGSACCOUNTCHARGE_CHECKER	SAVINGSACCOUNTCHARGE	CREATE_CHECKER	f
406	portfolio	UPDATE_SAVINGSACCOUNTCHARGE	SAVINGSACCOUNTCHARGE	UPDATE	f
407	portfolio	UPDATE_SAVINGSACCOUNTCHARGE_CHECKER	SAVINGSACCOUNTCHARGE	UPDATE_CHECKER	f
408	portfolio	DELETE_SAVINGSACCOUNTCHARGE	SAVINGSACCOUNTCHARGE	DELETE	f
409	portfolio	DELETE_SAVINGSACCOUNTCHARGE_CHECKER	SAVINGSACCOUNTCHARGE	DELETE_CHECKER	f
410	portfolio	WAIVE_SAVINGSACCOUNTCHARGE	SAVINGSACCOUNTCHARGE	WAIVE	f
411	portfolio	WAIVE_SAVINGSACCOUNTCHARGE_CHECKER	SAVINGSACCOUNTCHARGE	WAIVE_CHECKER	f
412	portfolio	PAY_SAVINGSACCOUNTCHARGE	SAVINGSACCOUNTCHARGE	PAY	f
413	portfolio	PAY_SAVINGSACCOUNTCHARGE_CHECKER	SAVINGSACCOUNTCHARGE	PAY_CHECKER	f
414	portfolio	PROPOSEANDACCEPTTRANSFER_CLIENT	CLIENT	PROPOSEANDACCEPTTRANSFER	f
415	portfolio	PROPOSEANDACCEPTTRANSFER_CLIENT_CHECKER	CLIENT	PROPOSEANDACCEPTTRANSFER_CHECKER	f
416	organisation	DELETE_TEMPLATE	TEMPLATE	DELETE	f
417	organisation	CREATE_TEMPLATE	TEMPLATE	CREATE	f
418	organisation	UPDATE_TEMPLATE	TEMPLATE	UPDATE	f
419	organisation	READ_TEMPLATE	TEMPLATE	READ	f
420	accounting	UPDATERUNNINGBALANCE_JOURNALENTRY	JOURNALENTRY	UPDATERUNNINGBALANCE	f
421	organisation	READ_SMS	SMS	READ	f
422	organisation	CREATE_SMS	SMS	CREATE	f
423	organisation	CREATE_SMS_CHECKER	SMS	CREATE_CHECKER	f
424	organisation	UPDATE_SMS	SMS	UPDATE	f
425	organisation	UPDATE_SMS_CHECKER	SMS	UPDATE_CHECKER	f
426	organisation	DELETE_SMS	SMS	DELETE	f
427	organisation	DELETE_SMS_CHECKER	SMS	DELETE_CHECKER	f
428	organisation	CREATE_HOLIDAY_CHECKER	HOLIDAY	CREATE_CHECKER	f
429	organisation	ACTIVATE_HOLIDAY	HOLIDAY	ACTIVATE	f
430	organisation	ACTIVATE_HOLIDAY_CHECKER	HOLIDAY	ACTIVATE_CHECKER	f
431	organisation	UPDATE_HOLIDAY	HOLIDAY	UPDATE	f
432	organisation	UPDATE_HOLIDAY_CHECKER	HOLIDAY	UPDATE_CHECKER	f
433	organisation	DELETE_HOLIDAY	HOLIDAY	DELETE	f
434	organisation	DELETE_HOLIDAY_CHECKER	HOLIDAY	DELETE_CHECKER	f
435	transaction_loan	UNDOWRITEOFF_LOAN	LOAN	UNDOWRITEOFF	f
436	portfolio	READ_SAVINGSACCOUNTCHARGE	SAVINGSACCOUNTCHARGE	READ	f
437	accounting	CREATE_JOURNALENTRY_CHECKER	JOURNALENTRY	CREATE_CHECKER	f
438	portfolio	UPDATE_DISBURSEMENTDETAIL	DISBURSEMENTDETAIL	UPDATE	f
439	portfolio	UPDATESAVINGSACCOUNT_CLIENT	CLIENT	UPDATESAVINGSACCOUNT	f
440	accounting	READ_ACCOUNTINGRULE	ACCOUNTINGRULE	READ	f
441	accounting	READ_JOURNALENTRY	JOURNALENTRY	READ	f
442	accounting	READ_GLACCOUNT	GLACCOUNT	READ	f
443	accounting	READ_GLCLOSURE	GLCLOSURE	READ	f
444	organisation	READ_HOLIDAY	HOLIDAY	READ	f
445	jobs	READ_SCHEDULER	SCHEDULER	READ	f
446	portfolio	READ_PRODUCTMIX	PRODUCTMIX	READ	f
447	portfolio	READ_MEETING	MEETING	READ	f
448	jobs	EXECUTEJOB_SCHEDULER	SCHEDULER	EXECUTEJOB	f
449	account_transfer	READ_STANDINGINSTRUCTION 	STANDINGINSTRUCTION 	READ	f
450	account_transfer	CREATE_STANDINGINSTRUCTION 	STANDINGINSTRUCTION 	CREATE	f
451	account_transfer	UPDATE_STANDINGINSTRUCTION 	STANDINGINSTRUCTION 	UPDATE	f
452	account_transfer	DELETE_STANDINGINSTRUCTION 	STANDINGINSTRUCTION 	DELETE	f
453	portfolio	CREATE_INTERESTRATECHART	INTERESTRATECHART	CREATE	f
454	portfolio	CREATE_INTERESTRATECHART_CHECKER	INTERESTRATECHART	CREATE_CHECKER	f
455	portfolio	UPDATE_INTERESTRATECHART	INTERESTRATECHART	UPDATE	f
456	portfolio	DELETE_INTERESTRATECHART	INTERESTRATECHART	DELETE	f
457	portfolio	UPDATE_INTERESTRATECHART_CHECKER	INTERESTRATECHART	UPDATE_CHECKER	f
458	portfolio	DELETE_INTERESTRATECHART_CHECKER	INTERESTRATECHART	DELETE_CHECKER	f
459	portfolio	CREATE_CHARTSLAB	CHARTSLAB	CREATE	f
460	portfolio	CREATE_CHARTSLAB_CHECKER	CHARTSLAB	CREATE_CHECKER	f
461	portfolio	UPDATE_CHARTSLAB	CHARTSLAB	UPDATE	f
462	portfolio	DELETE_CHARTSLAB	CHARTSLAB	DELETE	f
463	portfolio	UPDATE_CHARTSLAB_CHECKER	CHARTSLAB	UPDATE_CHECKER	f
464	portfolio	DELETE_CHARTSLAB_CHECKER	CHARTSLAB	DELETE_CHECKER	f
465	portfolio	CREATE_FIXEDDEPOSITPRODUCT	FIXEDDEPOSITPRODUCT	CREATE	f
466	portfolio	CREATE_FIXEDDEPOSITPRODUCT_CHECKER	FIXEDDEPOSITPRODUCT	CREATE_CHECKER	f
467	portfolio	UPDATE_FIXEDDEPOSITPRODUCT	FIXEDDEPOSITPRODUCT	UPDATE	f
468	portfolio	DELETE_FIXEDDEPOSITPRODUCT	FIXEDDEPOSITPRODUCT	DELETE	f
469	portfolio	UPDATE_FIXEDDEPOSITPRODUCT_CHECKER	FIXEDDEPOSITPRODUCT	UPDATE_CHECKER	f
470	portfolio	DELETE_FIXEDDEPOSITPRODUCT_CHECKER	FIXEDDEPOSITPRODUCT	DELETE_CHECKER	f
471	portfolio	CREATE_RECURRINGDEPOSITPRODUCT	RECURRINGDEPOSITPRODUCT	CREATE	f
472	portfolio	CREATE_RECURRINGDEPOSITPRODUCT_CHECKER	RECURRINGDEPOSITPRODUCT	CREATE_CHECKER	f
473	portfolio	UPDATE_RECURRINGDEPOSITPRODUCT	RECURRINGDEPOSITPRODUCT	UPDATE	f
474	portfolio	DELETE_RECURRINGDEPOSITPRODUCT	RECURRINGDEPOSITPRODUCT	DELETE	f
475	portfolio	UPDATE_RECURRINGDEPOSITPRODUCT_CHECKER	RECURRINGDEPOSITPRODUCT	UPDATE_CHECKER	f
476	portfolio	DELETE_RECURRINGDEPOSITPRODUCT_CHECKER	RECURRINGDEPOSITPRODUCT	DELETE_CHECKER	f
477	portfolio	READ_FIXEDDEPOSITACCOUNT	FIXEDDEPOSITACCOUNT	READ	f
478	portfolio	CREATE_FIXEDDEPOSITACCOUNT	FIXEDDEPOSITACCOUNT	CREATE	f
479	portfolio	CREATE_FIXEDDEPOSITACCOUNT_CHECKER	FIXEDDEPOSITACCOUNT	CREATE_CHECKER	f
480	portfolio	UPDATE_FIXEDDEPOSITACCOUNT	FIXEDDEPOSITACCOUNT	UPDATE	f
481	portfolio	UPDATE_FIXEDDEPOSITACCOUNT_CHECKER	FIXEDDEPOSITACCOUNT	UPDATE_CHECKER	f
482	portfolio	DELETE_FIXEDDEPOSITACCOUNT	FIXEDDEPOSITACCOUNT	DELETE	f
483	portfolio	DELETE_FIXEDDEPOSITACCOUNT_CHECKER	FIXEDDEPOSITACCOUNT	DELETE_CHECKER	f
484	transaction_savings	DEPOSIT_FIXEDDEPOSITACCOUNT	FIXEDDEPOSITACCOUNT	DEPOSIT	f
485	transaction_savings	DEPOSIT_FIXEDDEPOSITACCOUNT_CHECKER	FIXEDDEPOSITACCOUNT	DEPOSIT_CHECKER	f
486	transaction_savings	WITHDRAWAL_FIXEDDEPOSITACCOUNT	FIXEDDEPOSITACCOUNT	WITHDRAWAL	f
487	transaction_savings	WITHDRAWAL_FIXEDDEPOSITACCOUNT_CHECKER	FIXEDDEPOSITACCOUNT	WITHDRAWAL_CHECKER	f
488	transaction_savings	ACTIVATE_FIXEDDEPOSITACCOUNT	FIXEDDEPOSITACCOUNT	ACTIVATE	f
489	transaction_savings	ACTIVATE_FIXEDDEPOSITACCOUNT_CHECKER	FIXEDDEPOSITACCOUNT	ACTIVATE_CHECKER	f
490	transaction_savings	CALCULATEINTEREST_FIXEDDEPOSITACCOUNT	FIXEDDEPOSITACCOUNT	CALCULATEINTEREST	f
491	transaction_savings	CALCULATEINTEREST_FIXEDDEPOSITACCOUNT_CHECKER	FIXEDDEPOSITACCOUNT	CALCULATEINTEREST_CHECKER	f
492	transaction_savings	POSTINTEREST_FIXEDDEPOSITACCOUNT	FIXEDDEPOSITACCOUNT	POSTINTEREST	t
493	transaction_savings	POSTINTEREST_FIXEDDEPOSITACCOUNT_CHECKER	FIXEDDEPOSITACCOUNT	POSTINTEREST_CHECKER	f
494	transaction_savings	APPROVE_FIXEDDEPOSITACCOUNT	FIXEDDEPOSITACCOUNT	APPROVE	t
495	transaction_savings	REJECT_FIXEDDEPOSITACCOUNT	FIXEDDEPOSITACCOUNT	REJECT	t
496	transaction_savings	WITHDRAW_FIXEDDEPOSITACCOUNT	FIXEDDEPOSITACCOUNT	WITHDRAW	t
497	transaction_savings	APPROVALUNDO_FIXEDDEPOSITACCOUNT	FIXEDDEPOSITACCOUNT	APPROVALUNDO	t
498	transaction_savings	CLOSE_FIXEDDEPOSITACCOUNT	FIXEDDEPOSITACCOUNT	CLOSE	t
499	transaction_savings	APPROVE_FIXEDDEPOSITACCOUNT_CHECKER	FIXEDDEPOSITACCOUNT	APPROVE_CHECKER	f
500	transaction_savings	REJECT_FIXEDDEPOSITACCOUNT_CHECKER	FIXEDDEPOSITACCOUNT	REJECT_CHECKER	f
501	transaction_savings	WITHDRAW_FIXEDDEPOSITACCOUNT_CHECKER	FIXEDDEPOSITACCOUNT	WITHDRAW_CHECKER	f
502	transaction_savings	APPROVALUNDO_FIXEDDEPOSITACCOUNT_CHECKER	FIXEDDEPOSITACCOUNT	APPROVALUNDO_CHECKER	f
503	transaction_savings	CLOSE_FIXEDDEPOSITACCOUNT_CHECKER	FIXEDDEPOSITACCOUNT	CLOSE_CHECKER	f
504	transaction_savings	UNDOTRANSACTION_FIXEDDEPOSITACCOUNT	FIXEDDEPOSITACCOUNT	UNDOTRANSACTION	t
505	transaction_savings	UNDOTRANSACTION_FIXEDDEPOSITACCOUNT_CHECKER	FIXEDDEPOSITACCOUNT	UNDOTRANSACTION_CHECKER	f
506	transaction_savings	ADJUSTTRANSACTION_FIXEDDEPOSITACCOUNT	FIXEDDEPOSITACCOUNT	ADJUSTTRANSACTION	f
507	portfolio	READ_RECURRINGDEPOSITACCOUNT	RECURRINGDEPOSITACCOUNT	READ	f
508	portfolio	CREATE_RECURRINGDEPOSITACCOUNT	RECURRINGDEPOSITACCOUNT	CREATE	f
509	portfolio	CREATE_RECURRINGDEPOSITACCOUNT_CHECKER	RECURRINGDEPOSITACCOUNT	CREATE_CHECKER	f
510	portfolio	UPDATE_RECURRINGDEPOSITACCOUNT	RECURRINGDEPOSITACCOUNT	UPDATE	f
511	portfolio	UPDATE_RECURRINGDEPOSITACCOUNT_CHECKER	RECURRINGDEPOSITACCOUNT	UPDATE_CHECKER	f
512	portfolio	DELETE_RECURRINGDEPOSITACCOUNT	RECURRINGDEPOSITACCOUNT	DELETE	f
513	portfolio	DELETE_RECURRINGDEPOSITACCOUNT_CHECKER	RECURRINGDEPOSITACCOUNT	DELETE_CHECKER	f
514	transaction_savings	DEPOSIT_RECURRINGDEPOSITACCOUNT	RECURRINGDEPOSITACCOUNT	DEPOSIT	f
515	transaction_savings	DEPOSIT_RECURRINGDEPOSITACCOUNT_CHECKER	RECURRINGDEPOSITACCOUNT	DEPOSIT_CHECKER	f
516	transaction_savings	WITHDRAWAL_RECURRINGDEPOSITACCOUNT	RECURRINGDEPOSITACCOUNT	WITHDRAWAL	f
517	transaction_savings	WITHDRAWAL_RECURRINGDEPOSITACCOUNT_CHECKER	RECURRINGDEPOSITACCOUNT	WITHDRAWAL_CHECKER	f
518	transaction_savings	ACTIVATE_RECURRINGDEPOSITACCOUNT	RECURRINGDEPOSITACCOUNT	ACTIVATE	f
519	transaction_savings	ACTIVATE_RECURRINGDEPOSITACCOUNT_CHECKER	RECURRINGDEPOSITACCOUNT	ACTIVATE_CHECKER	f
520	transaction_savings	CALCULATEINTEREST_RECURRINGDEPOSITACCOUNT	RECURRINGDEPOSITACCOUNT	CALCULATEINTEREST	f
521	transaction_savings	CALCULATEINTEREST_RECURRINGDEPOSITACCOUNT_CHECKER	RECURRINGDEPOSITACCOUNT	CALCULATEINTEREST_CHECKER	f
522	transaction_savings	POSTINTEREST_RECURRINGDEPOSITACCOUNT	RECURRINGDEPOSITACCOUNT	POSTINTEREST	t
523	transaction_savings	POSTINTEREST_RECURRINGDEPOSITACCOUNT_CHECKER	RECURRINGDEPOSITACCOUNT	POSTINTEREST_CHECKER	f
524	transaction_savings	APPROVE_RECURRINGDEPOSITACCOUNT	RECURRINGDEPOSITACCOUNT	APPROVE	t
525	transaction_savings	REJECT_RECURRINGDEPOSITACCOUNT	RECURRINGDEPOSITACCOUNT	REJECT	t
526	transaction_savings	WITHDRAW_RECURRINGDEPOSITACCOUNT	RECURRINGDEPOSITACCOUNT	WITHDRAW	t
527	transaction_savings	APPROVALUNDO_RECURRINGDEPOSITACCOUNT	RECURRINGDEPOSITACCOUNT	APPROVALUNDO	t
528	transaction_savings	CLOSE_RECURRINGDEPOSITACCOUNT	RECURRINGDEPOSITACCOUNT	CLOSE	t
529	transaction_savings	APPROVE_RECURRINGDEPOSITACCOUNT_CHECKER	RECURRINGDEPOSITACCOUNT	APPROVE_CHECKER	f
530	transaction_savings	REJECT_RECURRINGDEPOSITACCOUNT_CHECKER	RECURRINGDEPOSITACCOUNT	REJECT_CHECKER	f
531	transaction_savings	WITHDRAW_RECURRINGDEPOSITACCOUNT_CHECKER	RECURRINGDEPOSITACCOUNT	WITHDRAW_CHECKER	f
532	transaction_savings	APPROVALUNDO_RECURRINGDEPOSITACCOUNT_CHECKER	RECURRINGDEPOSITACCOUNT	APPROVALUNDO_CHECKER	f
533	transaction_savings	CLOSE_RECURRINGDEPOSITACCOUNT_CHECKER	RECURRINGDEPOSITACCOUNT	CLOSE_CHECKER	f
534	transaction_savings	UNDOTRANSACTION_RECURRINGDEPOSITACCOUNT	RECURRINGDEPOSITACCOUNT	UNDOTRANSACTION	t
535	transaction_savings	UNDOTRANSACTION_RECURRINGDEPOSITACCOUNT_CHECKER	RECURRINGDEPOSITACCOUNT	UNDOTRANSACTION_CHECKER	f
536	transaction_savings	ADJUSTTRANSACTION_RECURRINGDEPOSITACCOUNT	RECURRINGDEPOSITACCOUNT	ADJUSTTRANSACTION	f
537	transaction_savings	PREMATURECLOSE_FIXEDDEPOSITACCOUNT_CHECKER	FIXEDDEPOSITACCOUNT	PREMATURECLOSE_CHECKER	f
538	transaction_savings	PREMATURECLOSE_FIXEDDEPOSITACCOUNT	FIXEDDEPOSITACCOUNT	PREMATURECLOSE	t
539	transaction_savings	PREMATURECLOSE_RECURRINGDEPOSITACCOUNT_CHECKER	RECURRINGDEPOSITACCOUNT	PREMATURECLOSE_CHECKER	f
540	transaction_savings	PREMATURECLOSE_RECURRINGDEPOSITACCOUNT	RECURRINGDEPOSITACCOUNT	PREMATURECLOSE	t
541	transaction_loan	DISBURSETOSAVINGS_LOAN	LOAN	DISBURSETOSAVINGS	f
542	transaction_loan	RECOVERYPAYMENT_LOAN	LOAN	RECOVERYPAYMENT	f
543	organisation	READ_RECURRINGDEPOSITPRODUCT	RECURRINGDEPOSITPRODUCT	READ	f
544	organisation	READ_FIXEDDEPOSITPRODUCT	FIXEDDEPOSITPRODUCT	READ	f
545	accounting	READ_FINANCIALACTIVITYACCOUNT	FINANCIALACTIVITYACCOUNT	READ	f
546	accounting	CREATE_FINANCIALACTIVITYACCOUNT	FINANCIALACTIVITYACCOUNT	CREATE	f
547	accounting	DELETE_FINANCIALACTIVITYACCOUNT	FINANCIALACTIVITYACCOUNT	DELETE	f
548	accounting	UPDATE_FINANCIALACTIVITYACCOUNT	FINANCIALACTIVITYACCOUNT	UPDATE	f
549	datatable	UPDATE_LIKELIHOOD	likelihood	UPDATE	f
550	survey	REGISTER_SURVEY	survey	CREATE	f
551	accounting	EXECUTE_PERIODICACCRUALACCOUNTING	PERIODICACCRUALACCOUNTING	EXECUTE	f
552	portfolio	INACTIVATE_SAVINGSACCOUNTCHARGE	SAVINGSACCOUNTCHARGE	INACTIVATE	f
553	portfolio	INACTIVATE_SAVINGSACCOUNTCHARGE_CHECKER	SAVINGSACCOUNTCHARGE	INACTIVATE_CHECKER	f
554	portfolio_center	DISASSOCIATEGROUPS_CENTER	CENTER	DISASSOCIATEGROUPS	f
555	portfolio_center	ASSOCIATEGROUPS_CENTER	CENTER	ASSOCIATEGROUPS	f
556	portfolio_center	DISASSOCIATEGROUPS_CENTER_CHECKER	CENTER	DISASSOCIATEGROUPS_CHECKER	f
557	portfolio_center	ASSOCIATEGROUPS_CENTER_CHECKER	CENTER	ASSOCIATEGROUPS_CHECKER	f
558	loan_reschedule	READ_RESCHEDULELOAN	RESCHEDULELOAN	READ	f
559	loan_reschedule	CREATE_RESCHEDULELOAN	RESCHEDULELOAN	CREATE	f
560	loan_reschedule	REJECT_RESCHEDULELOAN	RESCHEDULELOAN	REJECT	f
561	loan_reschedule	APPROVE_RESCHEDULELOAN	RESCHEDULELOAN	APPROVE	f
562	configuration	CREATE_HOOK	HOOK	CREATE	f
563	configuration	READ_HOOK	HOOK	READ	f
564	configuration	UPDATE_HOOK	HOOK	UPDATE	f
565	configuration	DELETE_HOOK	HOOK	DELETE	f
566	portfolio	REMOVESAVINGSOFFICER_SAVINGSACCOUNT	SAVINGSACCOUNT	REMOVESAVINGSOFFICER	t
567	portfolio	UPDATESAVINGSOFFICER_SAVINGSACCOUNT	SAVINGSACCOUNT	UPDATESAVINGSOFFICER	t
578	report	READ_Client Loan Account Schedule	Client Loan Account Schedule	READ	f
580	report	READ_Client Saving Transactions	Client Saving Transactions	READ	f
581	report	READ_Client Savings Summary	Client Savings Summary	READ	f
582	report	READ_ClientSummary 	ClientSummary 	READ	f
583	report	READ_ClientTrendsByDay	ClientTrendsByDay	READ	f
584	report	READ_ClientTrendsByMonth	ClientTrendsByMonth	READ	f
585	report	READ_ClientTrendsByWeek	ClientTrendsByWeek	READ	f
586	report	READ_Demand_Vs_Collection	Demand_Vs_Collection	READ	f
587	report	READ_Disbursal_Vs_Awaitingdisbursal	Disbursal_Vs_Awaitingdisbursal	READ	f
591	report	READ_GroupNamesByStaff	GroupNamesByStaff	READ	f
592	report	READ_GroupSavingSummary	GroupSavingSummary	READ	f
593	report	READ_LoanCyclePerProduct	LoanCyclePerProduct	READ	f
598	report	READ_LoanTrendsByDay	LoanTrendsByDay	READ	f
599	report	READ_LoanTrendsByMonth	LoanTrendsByMonth	READ	f
600	report	READ_LoanTrendsByWeek	LoanTrendsByWeek	READ	f
606	report	READ_Savings Transactions	Savings Transactions	READ	f
609	configuration	CREATE_ACCOUNTNUMBERFORMAT	ACCOUNTNUMBERFORMAT	CREATE	f
610	configuration	READ_ACCOUNTNUMBERFORMAT	ACCOUNTNUMBERFORMAT	READ	f
611	configuration	UPDATE_ACCOUNTNUMBERFORMAT	ACCOUNTNUMBERFORMAT	UPDATE	f
612	configuration	DELETE_ACCOUNTNUMBERFORMAT	HOOK	DELETE	f
613	portfolio	RECOVERGUARANTEES_LOAN	LOAN	RECOVERGUARANTEES	f
614	portfolio	RECOVERGUARANTEES_LOAN_CHECKER	LOAN	RECOVERGUARANTEES_CHECKER	f
615	portfolio	REJECT_CLIENT	CLIENT	REJECT	t
616	portfolio	REJECT_CLIENT_CHECKER	CLIENT	REJECT_CHECKER	f
617	portfolio	WITHDRAW_CLIENT	CLIENT	WITHDRAW	t
618	portfolio	WITHDRAW_CLIENT_CHECKER	CLIENT	WITHDRAW_CHECKER	f
619	portfolio	REACTIVATE_CLIENT	CLIENT	REACTIVATE	t
620	portfolio	REACTIVATE_CLIENT_CHECKER	CLIENT	REACTIVATE_CHECKER	f
621	transaction_savings	UPDATEDEPOSITAMOUNT_RECURRINGDEPOSITACCOUNT	RECURRINGDEPOSITACCOUNT	UPDATEDEPOSITAMOUNT	t
623	transaction_savings	REFUNDBYTRANSFER_ACCOUNTTRANSFER_CHECKER	ACCOUNTTRANSFER	REFUNDBYTRANSFER	f
624	transaction_savings	REFUNDBYTRANSFER_ACCOUNTTRANSFER	ACCOUNTTRANSFER	REFUNDBYTRANSFER	t
625	transaction_loan	REFUNDBYCASH_LOAN	LOAN	REFUNDBYCASH	t
626	transaction_loan	REFUNDBYCASH_LOAN_CHECKER	LOAN	REFUNDBYCASH	f
627	cash_mgmt	CREATE_TELLER	TELLER	CREATE	t
628	cash_mgmt	UPDATE_TELLER	TELLER	UPDATE	t
629	cash_mgmt	ALLOCATECASHIER_TELLER	TELLER	ALLOCATE	t
630	cash_mgmt	UPDATECASHIERALLOCATION_TELLER	TELLER	UPDATECASHIERALLOCATION	t
631	cash_mgmt	DELETECASHIERALLOCATION_TELLER	TELLER	DELETECASHIERALLOCATION	t
632	cash_mgmt	ALLOCATECASHTOCASHIER_TELLER	TELLER	ALLOCATECASHTOCASHIER	t
633	cash_mgmt	SETTLECASHFROMCASHIER_TELLER	TELLER	SETTLECASHFROMCASHIER	t
634	authorisation	DISABLE_ROLE	ROLE	DISABLE	f
635	authorisation	DISABLE_ROLE_CHECKER	ROLE	DISABLE_CHECKER	f
636	authorisation	ENABLE_ROLE	ROLE	ENABLE	f
637	authorisation	ENABLE_ROLE_CHECKER	ROLE	ENABLE_CHECKER	f
638	accounting	DEFINEOPENINGBALANCE_JOURNALENTRY	JOURNALENTRY	DEFINEOPENINGBALANCE	t
639	collection_sheet	READ_COLLECTIONSHEET	COLLECTIONSHEET	READ	f
640	collection_sheet	SAVE_COLLECTIONSHEET	COLLECTIONSHEET	SAVE	f
641	infrastructure	CREATE_ENTITYMAPPING	ENTITYMAPPING	CREATE	f
642	infrastructure	UPDATE_ENTITYMAPPING	ENTITYMAPPING	UPDATE	f
643	infrastructure	DELETE_ENTITYMAPPING	ENTITYMAPPING	DELETE	f
644	organisation	READ_WORKINGDAYS	WORKINGDAYS	READ	f
645	organisation	UPDATE_WORKINGDAYS	WORKINGDAYS	UPDATE	f
646	organisation	UPDATE_WORKINGDAYS_CHECKER	WORKINGDAYS	UPDATE_CHECKER	f
647	authorisation	READ_PASSWORD_PREFERENCES	PASSWORD_PREFERENCES	READ	f
648	authorisation	UPDATE_PASSWORD_PREFERENCES	PASSWORD_PREFERENCES	UPDATE	f
649	authorisation	UPDATE_PASSWORD_PREFERENCES_CHECKER	PASSWORD_PREFERENCES	UPDATE_CHECKER	f
650	portfolio	CREATE_PAYMENTTYPE	PAYMENTTYPE	CREATE	f
651	portfolio	UPDATE_PAYMENTTYPE	PAYMENTTYPE	UPDATE	f
652	portfolio	DELETE_PAYMENTTYPE	PAYMENTTYPE	DELETE	f
653	cash_mgmt	DELETE_TELLER	TELLER	DELETE	t
654	report	READ_General Ledger Report	General Ledger Report	READ	f
655	portfolio	READ_STAFFIMAGE	STAFFIMAGE	READ	f
656	portfolio	CREATE_STAFFIMAGE	STAFFIMAGE	CREATE	t
657	portfolio	CREATE_STAFFIMAGE_CHECKER	STAFFIMAGE	CREATE	f
658	portfolio	DELETE_STAFFIMAGE	STAFFIMAGE	DELETE	t
659	portfolio	DELETE_STAFFIMAGE_CHECKER	STAFFIMAGE	DELETE	f
660	report	READ_Active Loan Summary per Branch	Active Loan Summary per Branch	READ	f
661	report	READ_Disbursal Report	Disbursal Report	READ	f
662	report	READ_Balance Outstanding	Balance Outstanding	READ	f
663	report	READ_Collection Report	Collection Report	READ	f
664	portfolio	READ_PAYMENTTYPE	PAYMENTTYPE	READ	f
666	externalservices	UPDATE_EXTERNALSERVICES	EXTERNALSERVICES	UPDATE	f
667	portfolio	READ_CLIENTCHARGE	CLIENTCHARGE	READ	f
668	portfolio	CREATE_CLIENTCHARGE	CLIENTCHARGE	CREATE	f
669	portfolio	DELETE_CLIENTCHARGE	CLIENTCHARGE	DELETE	f
670	portfolio	WAIVE_CLIENTCHARGE	CLIENTCHARGE	WAIVE	f
671	portfolio	PAY_CLIENTCHARGE	CLIENTCHARGE	PAY	f
673	portfolio	UPDATE_CLIENTCHARGE	CLIENTCHARGE	UPDATE	f
674	portfolio	CREATE_CLIENTCHARGE_CHECKER	CLIENTCHARGE	CREATE_CHECKER	f
675	portfolio	DELETE_CLIENTCHARGE_CHECKER	CLIENTCHARGE	DELETE_CHECKER	f
676	portfolio	WAIVE_CLIENTCHARGE_CHECKER	CLIENTCHARGE	WAIVE_CHECKER	f
677	portfolio	PAY_CLIENTCHARGE_CHECKER	CLIENTCHARGE	PAY_CHECKER	f
679	portfolio	UPDATE_CLIENTCHARGE_CHECKER	CLIENTCHARGE	UPDATE_CHECKER	f
680	transaction_client	READTRANSACTION_CLIENT	CLIENT	READTRANSACTION	f
681	transaction_client	UNDOTRANSACTION_CLIENT	CLIENT	UNDOTRANSACTION	f
682	transaction_client	UNDOTRANSACTION_CLIENT_CHECKER	CLIENT	UNDOTRANSACTION_CHECKER	f
683	LOAN_PROVISIONING	CREATE_PROVISIONCATEGORY	PROVISIONCATEGORY	CREATE	f
684	LOAN_PROVISIONING	DELETE_PROVISIONCATEGORY	PROVISIONCATEGORY	DELETE	f
685	LOAN_PROVISIONING	CREATE_PROVISIONCRITERIA	PROVISIONINGCRITERIA	CREATE	f
686	LOAN_PROVISIONING	UPDATE_PROVISIONCRITERIA	PROVISIONINGCRITERIA	UPDATE	f
687	LOAN_PROVISIONING	DELETE_PROVISIONCRITERIA	PROVISIONINGCRITERIA	DELETE	f
688	LOAN_PROVISIONING	CREATE_PROVISIONENTRIES	PROVISIONINGENTRIES	CREATE	f
771	organisation	DELETE_EMAIL_CHECKER	EMAIL	DELETE_CHECKER	f
689	LOAN_PROVISIONING	CREATE_PROVISIONJOURNALENTRIES	PROVISIONINGENTRIES	CREATE	f
690	LOAN_PROVISIONING	RECREATE_PROVISIONENTRIES	PROVISIONINGENTRIES	RECREATE	f
691	portfolio	READ_FLOATINGRATE	FLOATINGRATE	READ	f
692	portfolio	CREATE_FLOATINGRATE	FLOATINGRATE	CREATE	t
693	portfolio	CREATE_FLOATINGRATE_CHECKER	FLOATINGRATE	CREATE_CHECKER	f
694	portfolio	UPDATE_FLOATINGRATE	FLOATINGRATE	UPDATE	t
695	portfolio	UPDATE_FLOATINGRATE_CHECKER	FLOATINGRATE	UPDATE_CHECKER	f
696	portfolio	CREATESCHEDULEEXCEPTIONS_LOAN	LOAN	CREATESCHEDULEEXCEPTIONS	f
697	portfolio	CREATESCHEDULEEXCEPTIONS_LOAN_CHECKER	LOAN	CREATESCHEDULEEXCEPTIONS_CHECKER	f
698	portfolio	DELETESCHEDULEEXCEPTIONS_LOAN	LOAN	DELETESCHEDULEEXCEPTIONS	f
699	portfolio	DELETESCHEDULEEXCEPTIONS_LOAN_CHECKER	LOAN	DELETESCHEDULEEXCEPTIONS_CHECKER	f
700	transaction_loan	DISBURSALLASTUNDO_LOAN	LOAN	DISBURSALLASTUNDO	f
701	transaction_loan	DISBURSALLASTUNDO_LOAN_CHECKER	LOAN	DISBURSALLASTUNDO_CHECKER	f
702	SHAREPRODUCT	CREATE_SHAREPRODUCT	SHAREPRODUCT	CREATE	f
703	SHAREPRODUCT	UPDATE_SHAREPRODUCT	SHAREPRODUCT	CREATE	f
704	SHAREACCOUNT	CREATE_SHAREACCOUNT	SHAREACCOUNT	CREATE	f
705	SHAREACCOUNT	UPDATE_SHAREACCOUNT	SHAREACCOUNT	CREATE	f
706	organisation	READ_TAXCOMPONENT	TAXCOMPONENT	READ	f
707	organisation	CREATE_TAXCOMPONENT	TAXCOMPONENT	CREATE	f
708	organisation	CREATE_TAXCOMPONENT_CHECKER	TAXCOMPONENT	CREATE_CHECKER	f
709	organisation	UPDATE_TAXCOMPONENT	TAXCOMPONENT	UPDATE	f
710	organisation	UPDATE_TAXCOMPONENT_CHECKER	TAXCOMPONENT	UPDATE_CHECKER	f
711	organisation	READ_TAXGROUP	TAXGROUP	READ	f
712	organisation	CREATE_TAXGROUP	TAXGROUP	CREATE	f
713	organisation	CREATE_TAXGROUP_CHECKER	TAXGROUP	CREATE_CHECKER	f
714	organisation	UPDATE_TAXGROUP	TAXGROUP	UPDATE	f
715	organisation	UPDATE_TAXGROUP_CHECKER	TAXGROUP	UPDATE_CHECKER	f
716	portfolio	UPDATEWITHHOLDTAX_SAVINGSACCOUNT	SAVINGSACCOUNT	UPDATEWITHHOLDTAX	f
717	portfolio	UPDATEWITHHOLDTAX_SAVINGSACCOUNT_CHECKER	SAVINGSACCOUNT	UPDATEWITHHOLDTAX_CHECKER	f
718	SHAREPRODUCT	CREATE_DIVIDEND_SHAREPRODUCT	SHAREPRODUCT	CREATE_DIVIDEND	f
719	SHAREPRODUCT	CREATE_DIVIDEND_SHAREPRODUCT_CHECKER	SHAREPRODUCT	CREATE_DIVIDEND_CHECKER	f
720	SHAREPRODUCT	APPROVE_DIVIDEND_SHAREPRODUCT	SHAREPRODUCT	APPROVE_DIVIDEND	f
721	SHAREPRODUCT	APPROVE_DIVIDEND_SHAREPRODUCT_CHECKER	SHAREPRODUCT	APPROVE_DIVIDEND_CHECKER	f
722	SHAREPRODUCT	DELETE_DIVIDEND_SHAREPRODUCT	SHAREPRODUCT	DELETE_DIVIDEND	f
723	SHAREPRODUCT	DELETE_DIVIDEND_SHAREPRODUCT_CHECKER	SHAREPRODUCT	DELETE_DIVIDEND_CHECKER	f
724	SHAREPRODUCT	READ_DIVIDEND_SHAREPRODUCT	SHAREPRODUCT	READ_DIVIDEND	f
725	SHAREACCOUNT	APPROVE_SHAREACCOUNT	SHAREACCOUNT	APPROVE	f
726	SHAREACCOUNT	ACTIVATE_SHAREACCOUNT	SHAREACCOUNT	ACTIVATE	f
727	SHAREACCOUNT	UNDOAPPROVAL_SHAREACCOUNT	SHAREACCOUNT	UNDOAPPROVAL	f
728	SHAREACCOUNT	REJECT_SHAREACCOUNT	SHAREACCOUNT	REJECT	f
729	SHAREACCOUNT	APPLYADDITIONALSHARES_SHAREACCOUNT	SHAREACCOUNT	APPLYADDITIONALSHARES	f
730	SHAREACCOUNT	APPROVEADDITIONALSHARES_SHAREACCOUNT	SHAREACCOUNT	APPROVEADDITIONALSHARES	f
731	SHAREACCOUNT	REJECTADDITIONALSHARES_SHAREACCOUNT	SHAREACCOUNT	REJECTADDITIONALSHARES	f
732	SHAREACCOUNT	REDEEMSHARES_SHAREACCOUNT	SHAREACCOUNT	REDEEMSHARES	f
733	SHAREACCOUNT	CLOSE_SHAREACCOUNT	SHAREACCOUNT	CLOSE	f
734	SSBENEFICIARYTPT	READ_SSBENEFICIARYTPT	SSBENEFICIARYTPT	READ	f
735	SSBENEFICIARYTPT	CREATE_SSBENEFICIARYTPT	SSBENEFICIARYTPT	CREATE	f
736	SSBENEFICIARYTPT	UPDATE_SSBENEFICIARYTPT	SSBENEFICIARYTPT	UPDATE	f
737	SSBENEFICIARYTPT	DELETE_SSBENEFICIARYTPT	SSBENEFICIARYTPT	DELETE	f
738	portfolio	FORECLOSURE_LOAN	LOAN	FORECLOSURE	f
739	portfolio	FORECLOSURE_LOAN_CHECKER	LOAN	FORECLOSURE_CHECKER	f
740	portfolio	CREATE_ADDRESS	ADDRESS	CREATE	f
742	portfolio	UPDATE_ADDRESS	ADDRESS	UPDATE	f
744	portfolio	READ_ADDRESS	ADDRESS	READ	f
745	portfolio	DELETE_ADDRESS	ADDRESS	DELETE	f
747	jobs	CREATE_REPORTMAILINGJOB	REPORTMAILINGJOB	CREATE	f
748	jobs	UPDATE_REPORTMAILINGJOB	REPORTMAILINGJOB	UPDATE	f
749	jobs	DELETE_REPORTMAILINGJOB	REPORTMAILINGJOB	DELETE	f
750	jobs	READ_REPORTMAILINGJOB	REPORTMAILINGJOB	READ	f
751	portfolio	UNDOREJECT_CLIENT	CLIENT	UNDOREJECT	t
753	portfolio	UNDOWITHDRAWAL_CLIENT	CLIENT	UNDOWITHDRAWAL	t
755	organisation	READ_SMSCAMPAIGN	SMSCAMPAIGN	READ	f
756	organisation	CREATE_SMSCAMPAIGN	SMSCAMPAIGN	CREATE	f
757	organisation	CREATE_SMSCAMPAIGN_CHECKER	SMSCAMPAIGN	CREATE	f
758	organisation	UPDATE_SMSCAMPAIGN	SMSCAMPAIGN	UPDATE	f
759	organisation	UPDATE_SMSCAMPAIGN_CHECKER	SMSCAMPAIGN	UPDATE	f
760	organisation	DELETE_SMSCAMPAIGN	SMSCAMPAIGN	DELETE	f
761	organisation	DELETE_SMSCAMPAIGN_CHECKER	SMSCAMPAIGN	DELETE	f
762	organisation	ACTIVATE_SMSCAMPAIGN	SMSCAMPAIGN	ACTIVATE	f
763	organisation	CLOSE_SMSCAMPAIGN	SMSCAMPAIGN	CLOSE	f
764	organisation	REACTIVATE_SMSCAMPAIGN	SMSCAMPAIGN	REACTIVATE	f
765	organisation	READ_EMAIL	EMAIL	READ	f
766	organisation	CREATE_EMAIL	EMAIL	CREATE	f
767	organisation	CREATE_EMAIL_CHECKER	EMAIL	CREATE_CHECKER	f
768	organisation	UPDATE_EMAIL	EMAIL	UPDATE	f
769	organisation	UPDATE_EMAIL_CHECKER	EMAIL	UPDATE_CHECKER	f
770	organisation	DELETE_EMAIL	EMAIL	DELETE	f
743	portfolio	UPDATE_ADDRESS_CHECKER	ADDRESS	UPDATE_CHECKER	f
746	portfolio	DELETE_ADDRESS_CHECKER	ADDRESS	DELETE_CHECKER	f
754	portfolio	UNDOWITHDRAWAL_CLIENT_CHECKER	CLIENT	UNDOWITHDRAWAL_CHECKER	f
772	organisation	READ_EMAIL_CAMPAIGN	EMAIL_CAMPAIGN	READ	f
773	organisation	CREATE_EMAIL_CAMPAIGN	EMAIL_CAMPAIGN	CREATE	f
774	organisation	CREATE_EMAIL_CAMPAIGN_CHECKER	EMAIL_CAMPAIGN	CREATE_CHECKER	f
775	organisation	UPDATE_EMAIL_CAMPAIGN	EMAIL_CAMPAIGN	UPDATE	f
776	organisation	UPDATE_EMAIL_CAMPAIGN_CHECKER	EMAIL_CAMPAIGN	UPDATE_CHECKER	f
777	organisation	DELETE_EMAIL_CAMPAIGN	EMAIL_CAMPAIGN	DELETE	f
778	organisation	DELETE_EMAIL_CAMPAIGN_CHECKER	EMAIL_CAMPAIGN	DELETE_CHECKER	f
779	organisation	CLOSE_EMAIL_CAMPAIGN	EMAIL_CAMPAIGN	CLOSE	f
780	organisation	ACTIVATE_EMAIL_CAMPAIGN	EMAIL_CAMPAIGN	ACTIVATE	f
781	organisation	REACTIVATE_EMAIL_CAMPAIGN	EMAIL_CAMPAIGN	REACTIVATE	f
782	organisation	READ_EMAIL_CONFIGURATION	EMAIL_CONFIGURATION	READ	f
783	organisation	UPDATE_EMAIL_CONFIGURATION	EMAIL_CONFIGURATION	UPDATE	f
784	report	READ_Active Clients - Email	Active Clients - Email	READ	f
785	report	READ_Prospective Clients - Email	Prospective Clients - Email	READ	f
786	report	READ_Active Loan Clients - Email	Active Loan Clients - Email	READ	f
787	report	READ_Loans in arrears - Email	Loans in arrears - Email	READ	f
788	report	READ_Loans disbursed to clients - Email	Loans disbursed to clients - Email	READ	f
789	report	READ_Loan payments due - Email	Loan payments due - Email	READ	f
790	report	READ_Dormant Prospects - Email	Dormant Prospects - Email	READ	f
791	report	READ_Active Group Leaders - Email	Active Group Leaders - Email	READ	f
792	report	READ_Loan Payments Due (Overdue Loans) - Email	Loan Payments Due (Overdue Loans) - Email	READ	f
793	report	READ_Loan Payments Received (Active Loans) - Email	Loan Payments Received (Active Loans) - Email	READ	f
794	report	READ_Loan Payments Received (Overdue Loans) - Email	Loan Payments Received (Overdue Loans)  - Email	READ	f
795	report	READ_Loan Fully Repaid - Email	Loan Fully Repaid - Email	READ	f
796	report	READ_Loans Outstanding after final instalment date - Email	Loans Outstanding after final instalment date - Email	READ	f
797	report	READ_Happy Birthday - Email	Happy Birthday - Email	READ	f
798	report	READ_Loan Rejected - Email	Loan Rejected - Email	READ	f
799	report	READ_Loan Approved - Email	Loan Approved - Email	READ	f
800	report	READ_Loan Repayment - Email	Loan Repayment - Email	READ	f
801	datatable	READ_ENTITY_DATATABLE_CHECK	ENTITY_DATATABLE_CHECK	READ	f
802	datatable	CREATE_ENTITY_DATATABLE_CHECK	ENTITY_DATATABLE_CHECK	CREATE	f
803	datatable	DELETE_ENTITY_DATATABLE_CHECK	ENTITY_DATATABLE_CHECK	DELETE	f
804	configuration	CREATE_CREDITBUREAU_LOANPRODUCT_MAPPING	CREDITBUREAU_LOANPRODUCT_MAPPING	CREATE	f
805	configuration	CREATE_ORGANISATIONCREDITBUREAU	ORGANISATIONCREDITBUREAU	CREATE	f
806	configuration	UPDATE_ORGANISATIONCREDITBUREAU	ORGANISATIONCREDITBUREAU	UPDATE	f
807	configuration	UPDATE_CREDITBUREAU_LOANPRODUCT_MAPPING	CREDITBUREAU_LOANPRODUCT_MAPPING	UPDATE	f
808	configuration	GET_CREDITREPORT	CREDITREPORT	GET	f
809	configuration	CREATE_CREDITBUREAU_CONFIGURATION	CREDITBUREAU_CONFIGURATION	CREATE	f
810	configuration	UPDATE_CREDITBUREAU_CONFIGURATION	CREDITBUREAU_CONFIGURATION	UPDATE	f
811	configuration	SAVE_CREDITREPORT	CREDITREPORT	SAVE	f
812	configuration	DELETE_CREDITREPORT	CREDITREPORT	DELETE	f
813	portfolio	CREATE_FAMILYMEMBERS	FAMILYMEMBERS	CREATE	f
814	portfolio	UPDATE_FAMILYMEMBERS	FAMILYMEMBERS	UPDATE	f
815	portfolio	DELETE_FAMILYMEMBERS	FAMILYMEMBERS	DELETE	f
816	transaction_savings	HOLDAMOUNT_SAVINGSACCOUNT	SAVINGSACCOUNT	HOLDAMOUNT	f
817	transaction_savings	HOLDAMOUNT_SAVINGSACCOUNT_CHECKER	SAVINGSACCOUNT	HOLDAMOUNT_CHECKER	f
818	transaction_savings	BLOCKDEBIT_SAVINGSACCOUNT	SAVINGSACCOUNT	BLOCKDEBIT	f
819	transaction_savings	BLOCKDEBIT_SAVINGSACCOUNT_CHECKER	SAVINGSACCOUNT	BLOCKDEBIT_CHECKER	f
820	transaction_savings	UNBLOCKDEBIT_SAVINGSACCOUNT	SAVINGSACCOUNT	UNBLOCKDEBIT	f
821	transaction_savings	UNBLOCKDEBIT_SAVINGSACCOUNT_CHECKER	SAVINGSACCOUNT	UNBLOCKDEBIT_CHECKER	f
822	transaction_savings	BLOCKCREDIT_SAVINGSACCOUNT	SAVINGSACCOUNT	BLOCKCREDIT	f
823	transaction_savings	BLOCKCREDIT_SAVINGSACCOUNT_CHECKER	SAVINGSACCOUNT	BLOCKCREDIT_CHECKER	f
824	transaction_savings	UNBLOCKCREDIT_SAVINGSACCOUNT	SAVINGSACCOUNT	UNBLOCKCREDIT	f
825	transaction_savings	UNBLOCKCREDIT_SAVINGSACCOUNT_CHECKER	SAVINGSACCOUNT	UNBLOCKCREDIT_CHECKER	f
826	transaction_savings	BLOCK_SAVINGSACCOUNT	SAVINGSACCOUNT	BLOCK	f
827	transaction_savings	BLOCK_SAVINGSACCOUNT_CHECKER	SAVINGSACCOUNT	BLOCK_CHECKER	f
828	transaction_savings	UNBLOCK_SAVINGSACCOUNT	SAVINGSACCOUNT	UNBLOCK	f
829	transaction_savings	UNBLOCK_SAVINGSACCOUNT_CHECKER	SAVINGSACCOUNT	UNBLOCK_CHECKER	f
830	transaction_savings	RELEASEAMOUNT_SAVINGSACCOUNT	SAVINGSACCOUNT	RELEASEAMOUNT	f
831	transaction_savings	RELEASEAMOUNT_SAVINGSACCOUNT_CHECKER	SAVINGSACCOUNT	RELEASEAMOUNT_CHECKER	f
832	authorisation	UPDATE_ADHOC	ADHOC	UPDATE	t
833	authorisation	UPDATE_ADHOC_CHECKER	ADHOC	UPDATE	f
834	authorisation	DELETE_ADHOC	ADHOC	DELETE	t
835	authorisation	DELETE_ADHOC_CHECKER	ADHOC	DELETE	f
836	authorisation	CREATE_ADHOC	ADHOC	CREATE	t
837	authorisation	CREATE_ADHOC_CHECKER	ADHOC	CREATE	f
838	authorisation	INVALIDATE_TWOFACTOR_ACCESSTOKEN	TWOFACTOR_ACCESSTOKEN	INVALIDATE	f
839	configuration	READ_TWOFACTOR_CONFIGURATION	TWOFACTOR_CONFIGURATION	READ	f
840	configuration	UPDATE_TWOFACTOR_CONFIGURATION	TWOFACTOR_CONFIGURATION	UPDATE	f
841	special	BYPASS_TWOFACTOR	\N	\N	f
842	infrastructure	READ_IMPORT	IMPORT	READ	f
843	portfolio	LINK_ACCOUNT_TO_POCKET	POCKET	LINK_ACCOUNT_TO	f
844	portfolio	DELINK_ACCOUNT_FROM_POCKET	POCKET	DELINK_ACCOUNT_FROM	f
845	interop	READ_INTERID	INTERID	READ	f
846	interop	READ_INTERREQUEST	INTERREQUEST	READ	f
847	interop	READ_INTERQUOTE	INTERQUOTE	READ	f
848	interop	READ_INTERTRANSFER	INTERTRANSFER	READ	f
849	interop	PREPARE_INTERTRANSFER	INTERTRANSFER	PREPARE	f
850	interop	RELEASE_INTERTRANSFER	INTERTRANSFER	RELEASE	f
851	interop	CREATE_INTERID	INTERID	CREATE	f
852	interop	CREATE_INTERREQUEST	INTERREQUEST	CREATE	f
853	interop	CREATE_INTERQUOTE	INTERQUOTE	CREATE	f
854	interop	CREATE_INTERTRANSFER	INTERTRANSFER	CREATE	f
855	interop	DELETE_INTERID	INTERID	DELETE	f
856	organisation	READ_RATE	RATE	CREATE	t
857	organisation	CREATE_RATE	RATE	CREATE	t
858	organisation	UPDATE_RATE	RATE	UPDATE	t
859	portfolio	CREATE_GSIMACCOUNT	GSIMACCOUNT	CREATE	f
860	portfolio	APPROVE_GSIMACCOUNT	GSIMACCOUNT	APPROVE	f
861	portfolio	ACTIVATE_GSIMACCOUNT	GSIMACCOUNT	ACTIVATE	f
862	portfolio	APPROVALUNDO_GSIMACCOUNT	GSIMACCOUNT	APPROVALUNDO	f
863	portfolio	UPDATE_GSIMACCOUNT	GSIMACCOUNT	UPDATE	f
864	portfolio	REJECT_GSIMACCOUNT	GSIMACCOUNT	REJECT	f
865	portfolio	DEPOSIT_GSIMACCOUNT	GSIMACCOUNT	DEPOSIT	f
866	portfolio	CLOSE_GSIMACCOUNT	GSIMACCOUNT	CLOSE	f
867	portfolio	APPROVE_GLIMLOAN	GLIMLOAN	APPROVE	f
868	portfolio	DISBURSE_GLIMLOAN	GLIMLOAN	DISBURSE	f
869	portfolio	REPAYMENT_GLIMLOAN	GLIMLOAN	REPAYMENT	f
870	portfolio	UNDODISBURSAL_GLIMLOAN	GLIMLOAN	UNDODISBURSAL	f
871	portfolio	UNDOAPPROVAL_GLIMLOAN	GLIMLOAN	UNDOAPPROVAL	f
872	portfolio	REJECT_GLIMLOAN	GLIMLOAN	REJECT	f
873	portfolio	CREATE_CLIENT_COLLATERAL_PRODUCT	CLIENT_COLLATERAL_PRODUCT	CREATE	f
874	portfolio	CREATE_COLLATERAL_PRODUCT	COLLATERAL_PRODUCT	CREATE	f
875	portfolio	DELETE_CLIENT_COLLATERAL_PRODUCT	CLIENT_COLLATERAL_PRODUCT	DELETE	f
876	portfolio	DELETE_COLLATERAL_PRODUCT	COLLATERAL_PRODUCT	DELETE	f
877	portfolio	DELETE_LOAN_COLLATERAL_PRODUCT	LOAN_COLLATERAL_PRODUCT	DELETE	f
878	portfolio	UPDATE_CLIENT_COLLATERAL_PRODUCT	CLIENT_COLLATERAL_PRODUCT	UPDATE	f
879	portfolio	UPDATE_COLLATERAL_PRODUCT	COLLATERAL_PRODUCT	UPDATE	f
880	portfolio	UPDATE_REPAYMENT_WITH_POSTDATEDCHECKS	REPAYMENT_WITH_POSTDATEDCHECKS	UPDATE	f
881	portfolio	DELETE_REPAYMENT_WITH_POSTDATEDCHECKS	REPAYMENT_WITH_POSTDATEDCHECKS	DELETE	f
882	portfolio	BOUNCE_REPAYMENT_WITH_POSTDATEDCHECKS	REPAYMENT_WITH_POSTDATEDCHECKS	BOUNCE	f
883	transaction_savings	REVERSETRANSACTION_SAVINGSACCOUNT	SAVINGSACCOUNT	REVERSETRANSACTION	f
884	transaction_savings	REVERSETRANSACTION_SAVINGSACCOUNT_CHECKER	SAVINGSACCOUNT	REVERSETRANSACTION_CHECKER	f
885	transaction_loan	CREDITBALANCEREFUND_LOAN	LOAN	CREDITBALANCEREFUND	f
886	transaction_loan	MERCHANTISSUEDREFUND_LOAN	LOAN	MERCHANTISSUEDREFUND	f
887	transaction_loan	PAYOUTREFUND_LOAN	LOAN	PAYOUTREFUND	f
888	transaction_loan	GOODWILLCREDIT_LOAN	LOAN	GOODWILLCREDIT	f
889	organisation	READ_BUSINESS_DATE	BUSINESS_DATE	READ	f
890	organisation	UPDATE_BUSINESS_DATE	BUSINESS_DATE	UPDATE	f
891	transaction_loan	CHARGEREFUND_LOAN	LOAN	CHARGEREFUND	f
892	organisation	READ_DELINQUENCY_BUCKET	DELINQUENCY_BUCKET	READ	f
893	organisation	CREATE_DELINQUENCY_BUCKET	DELINQUENCY_BUCKET	CREATE	f
894	organisation	UPDATE_DELINQUENCY_BUCKET	DELINQUENCY_BUCKET	UPDATE	f
895	organisation	DELETE_DELINQUENCY_BUCKET	DELINQUENCY_BUCKET	DELETE	f
896	organisation	CREATE_DELINQUENCY_RANGE	DELINQUENCY_RANGE	CREATE	f
897	organisation	UPDATE_DELINQUENCY_RANGE	DELINQUENCY_RANGE	UPDATE	f
898	organisation	DELETE_DELINQUENCY_RANGE	DELINQUENCY_RANGE	DELETE	f
899	organisation	READ_DELINQUENCY_TAGS	DELINQUENCY_TAGS	READ	f
900	organisation	UPDATE_DELINQUENCY_TAGS	DELINQUENCY_TAGS	UPDATE	f
901	organisation	UPDATEDELINQUENCY_LOAN	LOAN	UPDATEDELINQUENCY	f
902	organisation	UPDATE_BATCH_BUSINESS_STEP	BATCH_BUSINESS_STEP	UPDATE	f
903	transaction_loan	CHARGEBACK_LOAN	LOAN	CHARGEBACK	f
904	organisation	EXECUTE_INLINE_JOB	INLINE_JOB	EXECUTE	f
905	portfolio	SETFRAUD_LOAN	LOAN	SETFRAUD	f
906	configuration	UPDATE_EXTERNAL_EVENT_CONFIGURATION	EXTERNAL_EVENT_CONFIGURATION	UPDATE	f
907	configuration	READ_EXTERNAL_EVENT_CONFIGURATION	EXTERNAL_EVENT_CONFIGURATION	READ	f
908	transaction_loan	BYPASS_LOAN_WRITE_PROTECTION	LOAN	BYPASS	f
909	transaction_loan	ADJUSTMENT_LOANCHARGE	LOANCHARGE	ADJUSTMENT	f
910	transaction_loan	CHARGEOFF_LOAN	LOAN	CHARGEOFF	f
911	transaction_loan	UNDOCHARGEOFF_LOAN	LOAN	UNDOCHARGEOFF	f
912	organisation	CREATE_DELINQUENCY_ACTION	DELINQUENCY_ACTION	CREATE	f
913	transaction_loan	DISBURSEWITHOUTAUTODOWNPAYMENT_LOAN	LOAN	DISBURSEWITHOUTAUTODOWNPAYMENT	f
914	transaction_loan	INTERESTPAYMENTWAIVER_LOAN	LOAN	INTERESTPAYMENTWAIVER	f
915	investor	CANCEL_ASSET_OWNER_TRANSACTION	ASSET_OWNER_TRANSACTION	CANCEL	f
916	portfolio	REAGE_LOAN	LOAN	REAGE	f
917	portfolio	UNDO_REAGE_LOAN	LOAN	UNDO_REAGE	f
918	portfolio	REAMORTIZE_LOAN	LOAN	REAMORTIZE	f
919	portfolio	UNDO_REAMORTIZE_LOAN	LOAN	UNDO_REAMORTIZE	f
920	portfolio	CREATE_INTEREST_PAUSE	INTEREST_PAUSE	CREATE	f
921	portfolio	UPDATE_INTEREST_PAUSE	INTEREST_PAUSE	UPDATE	f
922	portfolio	DELETE_INTEREST_PAUSE	INTEREST_PAUSE	DELETE	f
923	LOAN_PROVISIONING	UPDATE_PROVISIONCATEGORY	PROVISIONCATEGORY	UPDATE	f
924	portfolio	DEACTIVATEOVERDUE_LOANCHARGE	LOANCHARGE	DEACTIVATEOVERDUE	f
925	authorisation	CHANGEPWD_USER	USER	CHANGEPWD	f
926	portfolio	CLOSE_CLIENT_CHECKER	CLIENT	CLOSE	f
927	portfolio	CLOSE_GROUP_CHECKER	GROUP	CLOSE	f
928	portfolio	CLOSE_CENTER_CHECKER	CENTER	CLOSE	f
929	portfolio	REMOVESAVINGSOFFICER_SAVINGSACCOUNT_CHECKER	SAVINGSACCOUNT	REMOVESAVINGSOFFICER	f
930	portfolio	UPDATESAVINGSOFFICER_SAVINGSACCOUNT_CHECKER	SAVINGSACCOUNT	UPDATESAVINGSOFFICER	f
931	cash_mgmt	CREATE_TELLER_CHECKER	TELLER	CREATE	f
932	cash_mgmt	UPDATE_TELLER_CHECKER	TELLER	UPDATE	f
933	cash_mgmt	ALLOCATECASHIER_TELLER_CHECKER	TELLER	ALLOCATE	f
934	cash_mgmt	UPDATECASHIERALLOCATION_TELLER_CHECKER	TELLER	UPDATECASHIERALLOCATION	f
935	cash_mgmt	DELETECASHIERALLOCATION_TELLER_CHECKER	TELLER	DELETECASHIERALLOCATION	f
936	cash_mgmt	ALLOCATECASHTOCASHIER_TELLER_CHECKER	TELLER	ALLOCATECASHTOCASHIER	f
937	cash_mgmt	SETTLECASHFROMCASHIER_TELLER_CHECKER	TELLER	SETTLECASHFROMCASHIER	f
938	accounting	DEFINEOPENINGBALANCE_JOURNALENTRY_CHECKER	JOURNALENTRY	DEFINEOPENINGBALANCE	f
939	cash_mgmt	DELETE_TELLER_CHECKER	TELLER	DELETE	f
940	organisation	READ_RATE_CHECKER	RATE	CREATE	f
941	organisation	CREATE_RATE_CHECKER	RATE	CREATE	f
942	organisation	UPDATE_RATE_CHECKER	RATE	UPDATE	f
943	accounting	UPDATEOPENINGBALANCE_JOURNALENTRY	JOURNALENTRY	UPDATEOPENINGBALANCE	t
944	accounting	UPDATEOPENINGBALANCE_JOURNALENTRY_CHECKER	JOURNALENTRY	UPDATEOPENINGBALANCE	f
945	account_transfer	CREATE_STANDINGINSTRUCTION	STANDINGINSTRUCTION	CREATE	f
946	account_transfer	UPDATE_STANDINGINSTRUCTION	STANDINGINSTRUCTION	UPDATE	f
947	account_transfer	DELETE_STANDINGINSTRUCTION	STANDINGINSTRUCTION	DELETE	f
948	collection_sheet	UPDATE_COLLECTIONSHEET	COLLECTIONSHEET	UPDATE	f
949	transaction_savings	POSTINTERESTASONDATE_SAVINGSACCOUNT	SAVINGSACCOUNT	POSTINTERESTASONDATE	t
950	transaction_savings	POSTINTERESTASONDATE_SAVINGSACCOUNT_CHECKER	SAVINGSACCOUNT	POSTINTERESTASONDATE	f
951	transaction_loan	CAPITALIZEDINCOME_LOAN	LOAN	CAPITALIZEDINCOME	f
952	transaction_loan	CAPITALIZEDINCOMEADJUSTMENT_LOAN	LOAN	CAPITALIZEDINCOMEADJUSTMENT	f
953	transaction_loan	BUYDOWNFEEADJUSTMENT_LOAN	LOAN	BUYDOWNFEEADJUSTMENT	f
954	transaction_loan	BUYDOWNFEE_LOAN	LOAN	BUYDOWNFEE	f
955	portfolio	CONTRACT_TERMINATION_UNDO_LOAN	LOAN	CONTRACT_TERMINATION_UNDO	f
956	transaction_loan	UPDATE_APPROVED_AMOUNT_LOAN	LOAN	UPDATE_APPROVED_AMOUNT	f
957	transaction_loan	MANUAL_INTEREST_REFUND_TRANSACTION_LOAN	LOAN	MANUAL_INTEREST_REFUND_TRANSACTION	f
741	portfolio	CREATE_ADDRESS_CHECKER	ADDRESS	CREATE_CHECKER	f
752	portfolio	UNDOREJECT_CLIENT_CHECKER	CLIENT	UNDOREJECT_CHECKER	f
622	transaction_savings	UPDATEDEPOSITAMOUNT_RECURRINGDEPOSITACCOUNT_CHECKER	RECURRINGDEPOSITACCOUNT	UPDATEDEPOSITAMOUNT	f
958	portfolio	READ_FAMILYMEMBERS	FAMILYMEMBERS	READ	f
959	portfolio	FORCE_WITHDRAWAL_SAVINGSACCOUNT	SAVINGSACCOUNT	FORCE_WITHDRAWAL	t
960	portfolio	FORCE_WITHDRAWAL_SAVINGSACCOUNT_CHECKER	SAVINGSACCOUNT	FORCE_WITHDRAWAL_CHECKER	f
961	transaction_loan	DOWNPAYMENT_LOAN	LOAN	DOWNPAYMENT	f
962	transaction_loan	SALE_LOAN	LOAN	SALE	f
963	transaction_loan	BUYBACK_LOAN	LOAN	BUYBACK	f
964	loan_product_attribute	CREATE_EXTERNAL_ASSET_OWNER_LOAN_PRODUCT_ATTRIBUTE	EXTERNAL_ASSET_OWNER_LOAN_PRODUCT_ATTRIBUTE	CREATE	f
965	loan_product_attribute	UPDATE_EXTERNAL_ASSET_OWNER_LOAN_PRODUCT_ATTRIBUTE	EXTERNAL_ASSET_OWNER_LOAN_PRODUCT_ATTRIBUTE	UPDATE	f
966	transaction_loan	INTERMEDIARYSALE_LOAN	LOAN	INTERMEDIARYSALE	f
967	investor	CREATE_EXTERNAL_ASSET_OWNER	EXTERNAL_ASSET_OWNER	CREATE	f
968	accounting	EXECUTEFORSAVINGS	PERIODICACCRUALACCOUNTINGFORSAVINGS	EXECUTE	f
969	portfolio	CONTRACT_TERMINATION_LOAN	LOAN	CONTRACT_TERMINATION	f
970	organisation	READ_WORKINGCAPITALLOANPRODUCT	WORKINGCAPITALLOANPRODUCT	READ	f
971	organisation	CREATE_WORKINGCAPITALLOANPRODUCT	WORKINGCAPITALLOANPRODUCT	CREATE	f
972	organisation	UPDATE_WORKINGCAPITALLOANPRODUCT	WORKINGCAPITALLOANPRODUCT	UPDATE	f
973	organisation	DELETE_WORKINGCAPITALLOANPRODUCT	WORKINGCAPITALLOANPRODUCT	DELETE	f
974	organisation	READ_WORKINGCAPITALLOAN	WORKINGCAPITALLOAN	READ	f
975	organisation	CREATE_WORKINGCAPITALLOAN	WORKINGCAPITALLOAN	CREATE	f
976	organisation	UPDATE_WORKINGCAPITALLOAN	WORKINGCAPITALLOAN	UPDATE	f
977	organisation	DELETE_WORKINGCAPITALLOAN	WORKINGCAPITALLOAN	DELETE	f
978	portfolio	APPROVE_WORKINGCAPITALLOAN	WORKINGCAPITALLOAN	APPROVE	f
979	portfolio	REJECT_WORKINGCAPITALLOAN	WORKINGCAPITALLOAN	REJECT	f
980	portfolio	APPROVALUNDO_WORKINGCAPITALLOAN	WORKINGCAPITALLOAN	APPROVALUNDO	f
981	portfolio	DISBURSE_WORKINGCAPITALLOAN	WORKINGCAPITALLOAN	DISBURSE	f
982	portfolio	DISBURSALUNDO_WORKINGCAPITALLOAN	WORKINGCAPITALLOAN	DISBURSALUNDO	f
983	portfolio	CREATE_WC_DELINQUENCY_ACTION	WC_DELINQUENCY_ACTION	CREATE	f
984	portfolio	READ_WC_DELINQUENCY_ACTION	WC_DELINQUENCY_ACTION	READ	f
985	organisation	READ_WORKINGCAPITALBREACH	WORKINGCAPITALBREACH	READ	f
986	organisation	CREATE_WORKINGCAPITALBREACH	WORKINGCAPITALBREACH	CREATE	f
987	organisation	UPDATE_WORKINGCAPITALBREACH	WORKINGCAPITALBREACH	UPDATE	f
988	organisation	DELETE_WORKINGCAPITALBREACH	WORKINGCAPITALBREACH	DELETE	f
989	portfolio	UPDATEDISCOUNT_WORKINGCAPITALLOAN	WORKINGCAPITALLOAN	UPDATEDISCOUNT	f
990	organisation	READ_WORKINGCAPITALNEARBREACH	WORKINGCAPITALNEARBREACH	READ	f
991	organisation	CREATE_WORKINGCAPITALNEARBREACH	WORKINGCAPITALNEARBREACH	CREATE	f
992	organisation	UPDATE_WORKINGCAPITALNEARBREACH	WORKINGCAPITALNEARBREACH	UPDATE	f
993	organisation	DELETE_WORKINGCAPITALNEARBREACH	WORKINGCAPITALNEARBREACH	DELETE	f
994	portfolio	UPDATERATE_WORKINGCAPITALLOAN	WORKINGCAPITALLOAN	UPDATERATE	f
995	transaction_loan	ADJUSTMENT_WORKINGCAPITALLOANCHARGE	WORKINGCAPITALLOANCHARGE	ADJUSTMENT	f
996	transaction_loan	ADJUSTMENT_WORKINGCAPITALLOANCHARGE_CHECKER	WORKINGCAPITALLOANCHARGE	ADJUSTMENT_CHECKER	f
997	portfolio	CREATE_WC_BREACH_ACTION	WC_BREACH_ACTION	CREATE	f
998	portfolio	READ_WC_BREACH_ACTION	WC_BREACH_ACTION	READ	f
999	portfolio	CREATE_WC_NEAR_BREACH_ACTION	WC_NEAR_BREACH_ACTION	CREATE	f
1000	portfolio	READ_WC_NEAR_BREACH_ACTION	WC_NEAR_BREACH_ACTION	READ	f
1001	portfolio	CREATE_WC_BREACH_RESET	WC_BREACH_RESET	CREATE	f
1002	portfolio	CREATE_WC_BREACH_DISABLE	WC_BREACH_DISABLE	CREATE	f
1003	portfolio	CREATE_LOAN_ORIGINATOR	LOAN_ORIGINATOR	CREATE	f
1004	portfolio	READ_LOAN_ORIGINATOR	LOAN_ORIGINATOR	READ	f
1005	portfolio	UPDATE_LOAN_ORIGINATOR	LOAN_ORIGINATOR	UPDATE	f
1006	portfolio	DELETE_LOAN_ORIGINATOR	LOAN_ORIGINATOR	DELETE	f
1007	portfolio	ATTACH_LOAN_ORIGINATOR	LOAN_ORIGINATOR	ATTACH	f
1008	portfolio	DETACH_LOAN_ORIGINATOR	LOAN_ORIGINATOR	DETACH	f
1009	portfolio	ATTACH_WORKING_CAPITAL_LOAN_ORIGINATOR	WORKING_CAPITAL_LOAN_ORIGINATOR	ATTACH	f
1010	portfolio	DETACH_WORKING_CAPITAL_LOAN_ORIGINATOR	WORKING_CAPITAL_LOAN_ORIGINATOR	DETACH	f
\.


ALTER TABLE public.m_permission 

--
-- Data for Name: m_portfolio_command_source; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_portfolio_command_source 

COPY public.m_portfolio_command_source (id, action_name, entity_name, office_id, group_id, client_id, loan_id, savings_account_id, api_get_url, resource_id, subresource_id, command_as_json, maker_id, made_on_date, checker_id, checked_on_date, status, product_id, transaction_id, creditbureau_id, organisation_creditbureau_id, made_on_date_utc, checked_on_date_utc, job_name, idempotency_key, resource_external_id, subresource_external_id, result, result_status_code, loan_external_id, is_sanitized, client_ip) FROM stdin;
\.


ALTER TABLE public.m_portfolio_command_source 

--
-- Data for Name: m_product_loan_charge; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_product_loan_charge 

COPY public.m_product_loan_charge (product_loan_id, charge_id) FROM stdin;
\.


ALTER TABLE public.m_product_loan_charge 

--
-- Data for Name: m_product_loan_configurable_attributes; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_product_loan_configurable_attributes 

COPY public.m_product_loan_configurable_attributes (id, loan_product_id, amortization_method_enum, interest_method_enum, loan_transaction_strategy_code, interest_calculated_in_period_enum, arrearstolerance_amount, repay_every, moratorium, grace_on_arrears_ageing) FROM stdin;
\.


ALTER TABLE public.m_product_loan_configurable_attributes 

--
-- Data for Name: m_product_loan_floating_rates; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_product_loan_floating_rates 

COPY public.m_product_loan_floating_rates (id, loan_product_id, floating_rates_id, interest_rate_differential, min_differential_lending_rate, default_differential_lending_rate, max_differential_lending_rate, is_floating_interest_rate_calculation_allowed) FROM stdin;
\.


ALTER TABLE public.m_product_loan_floating_rates 

--
-- Data for Name: m_product_loan_guarantee_details; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_product_loan_guarantee_details 

COPY public.m_product_loan_guarantee_details (id, loan_product_id, mandatory_guarantee, minimum_guarantee_from_own_funds, minimum_guarantee_from_guarantor_funds) FROM stdin;
\.


ALTER TABLE public.m_product_loan_guarantee_details 

--
-- Data for Name: m_product_loan_rate; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_product_loan_rate 

COPY public.m_product_loan_rate (product_loan_id, rate_id) FROM stdin;
\.


ALTER TABLE public.m_product_loan_rate 

--
-- Data for Name: m_product_loan_recalculation_details; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_product_loan_recalculation_details 

COPY public.m_product_loan_recalculation_details (id, product_id, compound_type_enum, reschedule_strategy_enum, rest_frequency_type_enum, rest_frequency_interval, arrears_based_on_original_schedule, pre_close_interest_calculation_strategy, compounding_frequency_type_enum, compounding_frequency_interval, rest_frequency_nth_day_enum, rest_frequency_on_day, rest_frequency_weekday_enum, compounding_frequency_nth_day_enum, compounding_frequency_on_day, compounding_frequency_weekday_enum, is_compounding_to_be_posted_as_transaction, allow_compounding_on_eod, disallow_interest_calc_on_past_due) FROM stdin;
\.


ALTER TABLE public.m_product_loan_recalculation_details 

--
-- Data for Name: m_product_loan_variable_installment_config; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_product_loan_variable_installment_config 

COPY public.m_product_loan_variable_installment_config (id, loan_product_id, minimum_gap, maximum_gap) FROM stdin;
\.


ALTER TABLE public.m_product_loan_variable_installment_config 

--
-- Data for Name: m_product_loan_variations_borrower_cycle; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_product_loan_variations_borrower_cycle 

COPY public.m_product_loan_variations_borrower_cycle (id, loan_product_id, borrower_cycle_number, value_condition, param_type, default_value, max_value, min_value) FROM stdin;
\.


ALTER TABLE public.m_product_loan_variations_borrower_cycle 

--
-- Data for Name: m_product_mix; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_product_mix 

COPY public.m_product_mix (id, product_id, restricted_product_id) FROM stdin;
\.


ALTER TABLE public.m_product_mix 

--
-- Data for Name: m_provisioning_criteria_definition; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_provisioning_criteria_definition 

COPY public.m_provisioning_criteria_definition (id, criteria_id, category_id, min_age, max_age, provision_percentage, liability_account, expense_account) FROM stdin;
\.


ALTER TABLE public.m_provisioning_criteria_definition 

--
-- Data for Name: m_repayment_with_post_dated_checks; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_repayment_with_post_dated_checks 

COPY public.m_repayment_with_post_dated_checks (id, check_no, amount, loan_id, repayment_id, account_no, bank_name, repayment_date, status) FROM stdin;
\.


ALTER TABLE public.m_repayment_with_post_dated_checks 

--
-- Data for Name: stretchy_report; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.stretchy_report 

COPY public.stretchy_report (id, report_name, report_type, report_subtype, report_category, report_sql, description, core_report, use_report, self_service_user_report) FROM stdin;
1	Client Listing	Table	\N	Client	SELECT Concat(REPEAT('..', (( Length(ounder.hierarchy) - Length( REPLACE(ounder.hierarchy, '.', '')) - 1 ))) , ounder.name)  AS "Office/Branch", c.account_no  AS "Client Account No.", c.display_name  AS name, r.enum_message_property AS "Status", c.activation_date AS "Activation", c.external_id AS "External Id" FROM   m_office o JOIN m_office ounder ON ounder.hierarchy LIKE Concat(o.hierarchy, '%') AND ounder.hierarchy LIKE Concat('${currentUserHierarchy}', '%') JOIN m_client c ON c.office_id = ounder.id LEFT JOIN r_enum_value r ON r.enum_name = 'status_enum' AND r.enum_id = c.status_enum WHERE  o.id = '${officeId}' ORDER  BY ounder.hierarchy, c.account_no	Individual Client Report    Lists the small number of defined fields on the client table.  Would expect to copy this   report and add any 'one to one' additional data for specific tenant needs.    Can be run for any size MFI but you'd expect it only to be run within a branch for   larger ones.  Depending on how many columns are displayed, there is probably is a limit of about 20/50k clients returned for html display (export to excel doesn't   have that client browser/memory impact).	t	t	f
2	Client Loans Listing	Table	\N	Client	SELECT    Concat(REPEAT('..', ((Length(ounder.hierarchy) - Length(REPLACE(ounder.hierarchy, '.', '')) - 1))), ounder.name) AS "Office/Branch", c.account_no AS "Client Account No.", c.display_name AS name, r.enum_message_property  AS "Client Status", lo.display_name  AS "Loan Officer", l.account_no AS "Loan Account No.", l.external_id  AS "External Id", p.name AS loan, st.enum_message_property AS "Status", f.name AS fund, purp.code_value  AS "Loan Purpose", Coalesce(cur.display_symbol, l.currency_code)  AS currency, l.principal_amount, l.arrearstolerance_amount AS "Arrears Tolerance Amount", l.number_of_repayments  AS "Expected No. Repayments", l.annual_nominal_interest_rate  AS "Annual Nominal Interest Rate", l.nominal_interest_rate_per_period  AS "Nominal Interest Rate Per Period", ipf.enum_message_property AS "Interest Rate Frequency", im.enum_message_property  AS "Interest Method", icp.enum_message_property AS "Interest Calculated in Period", l.term_frequency  AS "Term Frequency", tf.enum_message_property  AS "Term Frequency Period", l.repay_every AS "Repayment Frequency", rf.enum_message_property  AS "Repayment Frequency Period", am.enum_message_property  AS "Amortization", l.total_charges_due_at_disbursement_derived AS "Total Charges Due At Disbursement", DATE_TRUNC('day', l.submittedon_date) AS submitted, DATE_TRUNC('day', l.approvedon_date)                   approved, l.expected_disbursedon_date AS "Expected Disbursal", DATE_TRUNC('day', l.expected_firstrepaymenton_date) AS "Expected First Repayment", DATE_TRUNC('day', l.interest_calculated_from_date)  AS "Interest Calculated From" , DATE_TRUNC('day', l.disbursedon_date) AS disbursed, DATE_TRUNC('day', l.expected_maturedon_date)  AS "Expected Maturity", DATE_TRUNC('day', l.maturedon_date) AS "Matured On", DATE_TRUNC('day', l.closedon_date)  AS closed, DATE_TRUNC('day', l.rejectedon_date)  AS rejected, DATE_TRUNC('day', l.rescheduledon_date) AS rescheduled, DATE_TRUNC('day', l.withdrawnon_date) AS withdrawn, DATE_TRUNC('day', l.writtenoffon_date)  AS "Written Off" FROM      m_office o JOIN      m_office ounder ON        ounder.hierarchy LIKE concat(o.hierarchy, '%') AND       ounder.hierarchy LIKE concat('${currentUserHierarchy}', '%') JOIN      m_client c ON        c.office_id = ounder.id LEFT JOIN r_enum_value r ON        r.enum_name = 'status_enum' AND       r.enum_id = c.status_enum LEFT JOIN m_loan l ON        l.client_id = c.id LEFT JOIN m_staff lo ON        lo.id = l.loan_officer_id LEFT JOIN m_product_loan p ON        p.id = l.product_id LEFT JOIN m_fund f ON        f.id = l.fund_id LEFT JOIN r_enum_value st ON        st.enum_name = 'loan_status_id' AND       st.enum_id = l.loan_status_id LEFT JOIN r_enum_value ipf ON        ipf.enum_name = 'interest_period_frequency_enum' AND       ipf.enum_id = l.interest_period_frequency_enum LEFT JOIN r_enum_value im ON        im.enum_name = 'interest_method_enum' AND       im.enum_id = l.interest_method_enum LEFT JOIN r_enum_value tf ON        tf.enum_name = 'term_period_frequency_enum' AND       tf.enum_id = l.term_period_frequency_enum LEFT JOIN r_enum_value icp ON        icp.enum_name = 'interest_calculated_in_period_enum' AND       icp.enum_id = l.interest_calculated_in_period_enum LEFT JOIN r_enum_value rf ON        rf.enum_name = 'repayment_period_frequency_enum' AND       rf.enum_id = l.repayment_period_frequency_enum LEFT JOIN r_enum_value am ON        am.enum_name = 'amortization_method_enum' AND       am.enum_id = l.amortization_method_enum LEFT JOIN m_code_value purp ON        purp.id = l.loanpurpose_cv_id LEFT JOIN m_currency cur ON        cur.code = l.currency_code WHERE     o.id = '${officeId}' AND       ( l.currency_code = '${currencyId}' OR        '-1' = '${currencyId}') AND       ( l.product_id = '${loanProductId}' OR        '-1' = '${loanProductId}') AND       ( coalesce(l.loan_officer_id, -10) = ${loanOfficerId} OR        '-1' = ${loanOfficerId}) AND       ( coalesce(l.fund_id, -10) = ${fundId} OR        -1 = ${fundId}) AND       ( coalesce(l.loanpurpose_cv_id, -10) = ${loanPurposeId} OR        -1 = ${loanPurposeId}) ORDER BY  ounder.hierarchy, 2 , l.id	Individual Client Report    Pretty   wide report that lists the basic details of client loans.      Can be run for any size MFI but you'd expect it only to be run within a branch for larger ones.    There is probably is a limit of about 20/50k clients returned for html display (export to excel doesn't have that client browser/memory impact).	t	t	f
5	Loans Awaiting Disbursal	Table	\N	Loan	SELECT    Concat(REPEAT('..', ((Length(ounder.hierarchy) - Length(REPLACE(ounder.hierarchy, '.', '')) - 1))), ounder.name) AS "Office/Branch", c.account_no AS "Client Account No", c.display_name AS name, l.account_no AS "Loan Account No.", pl.name  AS "Product", f.name AS fund, Coalesce(cur.display_symbol, l.currency_code)  AS currency, l.principal_amount AS principal, l.term_frequency AS "Term Frequency", tf.enum_message_property AS "Term Frequency Period", l.annual_nominal_interest_rate AS "Annual Nominal Interest Rate", DATE_TRUNC('day', l.approvedon_date) AS "Approved", extract(day FROM (l.expected_disbursedon_date::TIMESTAMP - CURRENT_DATE)) AS "Days to Disbursal", DATE_TRUNC('day', l.expected_disbursedon_date) AS "Expected Disbursal", purp.code_value AS "Loan Purpose", lo.display_name AS "Loan Officer" FROM      m_office o JOIN      m_office ounder ON        ounder.hierarchy LIKE concat(o.hierarchy, '%') AND       ounder.hierarchy LIKE concat('${currentUserHierarchy}', '%') JOIN      m_client c ON        c.office_id = ounder.id JOIN      m_loan l ON        l.client_id = c.id JOIN      m_product_loan pl ON        pl.id = l.product_id LEFT JOIN m_staff lo ON        lo.id = l.loan_officer_id LEFT JOIN m_currency cur ON        cur.code = l.currency_code LEFT JOIN m_fund f ON        f.id = l.fund_id LEFT JOIN m_code_value purp ON        purp.id = l.loanpurpose_cv_id LEFT JOIN r_enum_value tf ON        tf.enum_name = 'term_period_frequency_enum' AND       tf.enum_id = l.term_period_frequency_enum WHERE     o.id = '${officeId}' AND       ( l.currency_code = '${currencyId}' OR        '-1' = '${currencyId}') AND       ( l.product_id = '${loanProductId}' OR        '-1' = '${loanProductId}') AND       ( coalesce(l.loan_officer_id, -10) = '${loanOfficerId}' OR        '-1' = '${loanOfficerId}') AND       ( coalesce(l.fund_id, -10) = '${fundId}' OR        -1 = '${fundId}') AND       ( coalesce(l.loanpurpose_cv_id, -10) = '${loanPurposeId}' OR        -1 = '${loanPurposeId}') AND       l.loan_status_id = 200 ORDER BY  ounder.hierarchy, extract(day FROM (l.expected_disbursedon_date::TIMESTAMP - CURRENT_DATE)), c.account_no	Individual Client Report	t	t	f
6	Loans Awaiting Disbursal Summary	Table	\N	Loan	SELECT    Concat(REPEAT('..', ((Length(ounder.hierarchy) - Length(REPLACE(ounder.hierarchy, '.', '')) - 1))), ounder.name) AS "Office/Branch", pl.name  AS "Product", Coalesce(cur.display_symbol, l.currency_code)  AS currency, f.name AS fund, Sum(l.principal_amount)  AS principal FROM      m_office o JOIN      m_office ounder ON        ounder.hierarchy LIKE Concat(o.hierarchy, '%') AND       ounder.hierarchy LIKE Concat('${currentUserHierarchy}', '%') JOIN      m_client c ON        c.office_id = ounder.id JOIN      m_loan l ON        l.client_id = c.id JOIN      m_product_loan pl ON        pl.id = l.product_id LEFT JOIN m_staff lo ON        lo.id = l.loan_officer_id LEFT JOIN m_currency cur ON        cur.code = l.currency_code LEFT JOIN m_fund f ON        f.id = l.fund_id LEFT JOIN m_code_value purp ON        purp.id = l.loanpurpose_cv_id WHERE     o.id = '${officeId}' AND       ( l.currency_code = '${currencyId}' OR        '-1' = '${currencyId}') AND       ( l.product_id = '${loanProductId}' OR        '-1' = '${loanProductId}') AND       ( coalesce(l.loan_officer_id, -10) = '${loanOfficerId}' OR        '-1' = '${loanOfficerId}') AND       ( coalesce(l.fund_id, -10) = '${fundId}' OR        -1 = '${fundId}') AND       ( coalesce(l.loanpurpose_cv_id, -10) = '${loanPurposeId}' OR        -1 = '${loanPurposeId}') AND       l.loan_status_id = 200 GROUP BY  ounder.hierarchy, pl.name, l.currency_code, f.name, ounder.name, cur.display_symbol ORDER BY  ounder.hierarchy, pl.name, l.currency_code, f.name	Individual Client Report	t	t	f
7	Loans Awaiting Disbursal Summary by Month	Table	\N	Loan	SELECT    Concat(REPEAT('..', ((Length(ounder.hierarchy) - Length(REPLACE(ounder.hierarchy, '.', '')) - 1))), ounder.name) AS "Office/Branch", pl.name  AS "Product", Coalesce(cur.display_symbol, l.currency_code)  AS "currency", extract(year from l.expected_disbursedon_date) AS "Year", to_char(l.expected_disbursedon_date, 'Month')  AS "Month", Sum(l.principal_amount)  AS "principal" FROM      m_office o JOIN      m_office ounder ON        ounder.hierarchy LIKE Concat(o.hierarchy, '%') AND       ounder.hierarchy LIKE Concat('${currentUserHierarchy}', '%') JOIN      m_client c ON        c.office_id = ounder.id JOIN      m_loan l ON        l.client_id = c.id JOIN      m_product_loan pl ON        pl.id = l.product_id LEFT JOIN m_staff lo ON        lo.id = l.loan_officer_id LEFT JOIN m_currency cur ON        cur.code = l.currency_code LEFT JOIN m_fund f ON        f.id = l.fund_id LEFT JOIN m_code_value purp ON        purp.id = l.loanpurpose_cv_id WHERE     o.id = '${officeId}' AND       ( l.currency_code = '${currencyId}' OR        '-1' = '${currencyId}') AND       ( l.product_id = '${loanProductId}' OR        '-1' = '${loanProductId}') AND       ( Coalesce(l.loan_officer_id, -10) = '${loanOfficerId}' OR        '-1' = '${loanOfficerId}') AND       ( coalesce(l.fund_id, -10) = '${fundId}' OR        -1 = '${fundId}') AND       ( coalesce(l.loanpurpose_cv_id, -10) = '${loanPurposeId}' OR        -1 = '${loanPurposeId}') AND       l.loan_status_id = 200 GROUP BY  ounder.hierarchy, ounder.name, cur.display_symbol, pl.name, l.currency_code, l.expected_disbursedon_date ORDER BY  ounder.hierarchy, pl.name, l.currency_code, extract(year from l.expected_disbursedon_date), to_char(l.expected_disbursedon_date, 'Month')	Individual Client Report	t	t	f
8	Loans Pending Approval	Table	\N	Loan	SELECT    Concat(REPEAT('..', ((Length(ounder.hierarchy) - Length(REPLACE(ounder.hierarchy, '.', '')) - 1))), ounder.name) AS "Office/Branch", c.account_no AS "Client Account No.", c.display_name AS "Client Name", Coalesce(cur.display_symbol, l.currency_code)  AS currency, pl.name  AS "Product", l.account_no AS "Loan Account No.", l.principal_amount AS "Loan Amount", l.term_frequency AS "Term Frequency", tf.enum_message_property AS "Term Frequency Period", l.annual_nominal_interest_rate AS " Annual Nominal Interest Rate", Extract(day FROM (CURRENT_DATE - l.submittedon_date::TIMESTAMP)) AS "Days Pending Approval", purp.code_value AS "Loan Purpose", lo.display_name AS "Loan Officer" FROM      m_office o JOIN      m_office ounder ON        ounder.hierarchy LIKE concat(o.hierarchy, '%') AND       ounder.hierarchy LIKE concat('${currentUserHierarchy}', '%') JOIN      m_client c ON        c.office_id = ounder.id JOIN      m_loan l ON        l.client_id = c.id JOIN      m_product_loan pl ON        pl.id = l.product_id LEFT JOIN m_staff lo ON        lo.id = l.loan_officer_id LEFT JOIN m_currency cur ON        cur.code = l.currency_code LEFT JOIN m_code_value purp ON        purp.id = l.loanpurpose_cv_id LEFT JOIN r_enum_value tf ON        tf.enum_name = 'term_period_frequency_enum' AND       tf.enum_id = l.term_period_frequency_enum WHERE     o.id = '${officeId}' AND       ( l.currency_code = '${currencyId}' OR        '-1' = '${currencyId}') AND       ( l.product_id = '${loanProductId}' OR        '-1' = '${loanProductId}') AND       ( coalesce(l.loan_officer_id, -10) = '${loanOfficerId}' OR        '-1' = '${loanOfficerId}') AND       ( coalesce(l.loanpurpose_cv_id, -10) = '${loanPurposeId}' OR        -1 = '${loanPurposeId}') AND       l.loan_status_id = 100 ORDER BY  ounder.hierarchy, l.submittedon_date, l.account_no	Individual Client Report	t	t	f
11	Active Loans - Summary	Table	\N	Loan	SELECT   Concat(REPEAT('..', ((Length(mo.hierarchy) - Length(REPLACE(mo.hierarchy, '.', '')) - 1))), mo.name) AS "Office/Branch", x.currency AS currency, x.client_count AS "No. of Clients", x.active_loan_count  AS "No. Active                    Loans", x. loans_in_arrears_count  AS "No. of Loans in                    Arrears", x.principal  AS "Total Loans Disbursed", x.principal_repaid AS "Principal Repaid", x.principal_outstanding  AS "Principal Outstanding", x.principal_overdue  AS "Principal Overdue", x.interest AS "Total Interest", x.interest_repaid  AS "Interest Repaid", x.interest_outstanding AS "Interest Outstanding", x.interest_overdue AS "Interest Overdue", x.fees AS "Total Fees", x.fees_repaid  AS "Fees Repaid", x.fees_outstanding AS "Fees Outstanding", x.fees_overdue AS "Fees Overdue", x.penalties  AS "Total Penalties", x.penalties_repaid AS "Penalties Repaid", x.penalties_outstanding  AS "Penalties Outstanding", x.penalties_overdue  AS "Penalties Overdue", ( CASE WHEN ${parType} = 1 THEN cast(round((x.principal_overdue * 100) / x.principal_outstanding, 2) AS                                                                                                                                     CHAR) WHEN ${parType} = 2 THEN cast(round(((x.principal_overdue + x.interest_overdue) * 100) / (x.principal_outstanding + x.interest_outstanding), 2) AS                                                                                   CHAR) WHEN ${parType} = 3 THEN cast(round(((x.principal_overdue + x.interest_overdue + x.fees_overdue) * 100) / (x.principal_outstanding + x.interest_outstanding + x.fees_outstanding), 2) AS                                             CHAR) WHEN ${parType} = 4 THEN cast(round(((x.principal_overdue + x.interest_overdue + x.fees_overdue + x.penalties_overdue) * 100) / (x.principal_outstanding + x.interest_outstanding + x.fees_outstanding + x.penalties_overdue), 2) AS CHAR) ELSE 'invalid PAR Type' end) AS "Portfolio at Risk %" FROM     m_office mo JOIN ( SELECT    ounder.id AS branch, coalesce(cur.display_symbol, l.currency_code) AS currency, count(DISTINCT(c.id)) AS client_count, count(DISTINCT(l.id)) AS active_loan_count, count(DISTINCT(coalesce(laa.loan_id, l.id, NULL) )) AS loans_in_arrears_count, sum(l.principal_disbursed_derived)  AS principal, sum(l.principal_repaid_derived) AS principal_repaid, sum(l.principal_outstanding_derived)  AS principal_outstanding, sum(laa.principal_overdue_derived)  AS principal_overdue, sum(l.interest_charged_derived) AS interest, sum(l.interest_repaid_derived)  AS interest_repaid, sum(l.interest_outstanding_derived) AS interest_outstanding, sum(laa.interest_overdue_derived) AS interest_overdue, sum(l.fee_charges_charged_derived)  AS fees, sum(l.fee_charges_repaid_derived) AS fees_repaid, sum(l.fee_charges_outstanding_derived)  AS fees_outstanding, sum(laa.fee_charges_overdue_derived)  AS fees_overdue, sum(l.penalty_charges_charged_derived)  AS penalties, sum(l.penalty_charges_repaid_derived) AS penalties_repaid, sum(l.penalty_charges_outstanding_derived)  AS penalties_outstanding, sum(laa.penalty_charges_overdue_derived)  AS penalties_overdue FROM      m_office o JOIN      m_office ounder ON        ounder.hierarchy LIKE concat(o.hierarchy, '%') AND       ounder.hierarchy LIKE concat('${currentUserHierarchy}', '%') JOIN      m_client c ON        c.office_id = ounder.id JOIN      m_loan l ON        l.client_id = c.id LEFT JOIN m_loan_arrears_aging laa ON        laa.loan_id = l.id LEFT JOIN m_currency cur ON        cur.code = l.currency_code WHERE     o.id = '${officeId}' AND       ( l.currency_code = '${currencyId}' OR        '-1' = '${currencyId}') AND       ( l.product_id = '${loanProductId}' OR        '-1' = '${loanProductId}') AND       ( coalesce(l.loan_officer_id, -10) = ${loanOfficerId} OR        '-1' = ${loanOfficerId}) AND       ( coalesce(l.fund_id, -10) = ${fundId} OR        -1 = ${fundId}) AND       ( coalesce(l.loanpurpose_cv_id, -10) = ${loanPurposeId} OR        -1 = ${loanPurposeId}) AND       l.loan_status_id = 300 GROUP BY  ounder.id, l.currency_code, cur.display_symbol) x ON       x.branch = mo.id ORDER BY mo.hierarchy, x.currency	\N	t	t	f
12	Active Loans - Details	Table	\N	Loan	SELECT    Concat(REPEAT('..', ((Length(ounder.hierarchy) - Length(REPLACE(ounder.hierarchy, '.', '')) - 1))), ounder.name) AS "Office/Branch", Coalesce(cur.display_symbol, l.currency_code)  AS currency, lo.display_name  AS "Loan Officer", c.display_name AS "Client", l.account_no AS "Loan Account No.", pl.name  AS "Product", f.name AS fund, l.principal_amount AS "Loan Amount", l.annual_nominal_interest_rate AS "Annual Nominal Interest Rate", DATE_TRUNC('day', l.disbursedon_date)  AS "Disbursed Date", DATE_TRUNC('day', l.expected_maturedon_date) AS "Expected Matured On", l.principal_repaid_derived AS "Principal Repaid", l.principal_outstanding_derived  AS "Principal Outstanding", laa.principal_overdue_derived  AS "Principal Overdue", l.interest_repaid_derived  AS "Interest Repaid", l.interest_outstanding_derived AS "Interest Outstanding", laa.interest_overdue_derived AS "Interest Overdue", l.fee_charges_repaid_derived AS "Fees Repaid", l.fee_charges_outstanding_derived  AS "Fees Outstanding", laa.fee_charges_overdue_derived  AS "Fees Overdue", l.penalty_charges_repaid_derived AS "Penalties Repaid", l.penalty_charges_outstanding_derived  AS "Penalties Outstanding", penalty_charges_overdue_derived  AS "Penalties Overdue" FROM      m_office o JOIN      m_office ounder ON        ounder.hierarchy LIKE Concat(o.hierarchy, '%') AND       ounder.hierarchy LIKE Concat('${currentUserHierarchy}', '%') JOIN      m_client c ON        c.office_id = ounder.id JOIN      m_loan l ON        l.client_id = c.id JOIN      m_product_loan pl ON        pl.id = l.product_id LEFT JOIN m_staff lo ON        lo.id = l.loan_officer_id LEFT JOIN m_currency cur ON        cur.code = l.currency_code LEFT JOIN m_fund f ON        f.id = l.fund_id LEFT JOIN m_loan_arrears_aging laa ON        laa.loan_id = l.id WHERE     o.id = '${officeId}' AND       ( l.currency_code = '${currencyId}' OR        '-1' = '${currencyId}') AND       ( l.product_id = '${loanProductId}' OR        '-1' = '${loanProductId}') AND       ( Coalesce(l.loan_officer_id, -10) = ${loanOfficerId} OR        '-1' = ${loanOfficerId}) AND       ( coalesce(l.fund_id, -10) = ${fundId} OR        -1 = ${fundId}) AND       ( coalesce(l.loanpurpose_cv_id, -10) = ${loanPurposeId} OR        -1 = ${loanPurposeId}) AND       l.loan_status_id = 300 GROUP BY  l.id, ounder.hierarchy , ounder.name, cur.display_symbol, lo.display_name, c.display_name, pl.name, f.name, laa.principal_overdue_derived, laa.interest_overdue_derived, laa.fee_charges_overdue_derived, laa.penalty_charges_overdue_derived , c.account_no ORDER BY  ounder.hierarchy, l.currency_code, c.account_no, l.account_no	Individual Client   Report	t	t	f
13	Obligation Met Loans Details	Table	\N	Loan	SELECT    Concat(REPEAT('..', ((Length(ounder.hierarchy ) - Length(REPLACE(ounder.hierarchy, '.', '')) - 1))), ounder.name) AS "Office/Branch", Coalesce(cur.display_symbol, l.currency_code) AS currency, c.account_no  AS "Client Account No.", c.display_name  AS "Client", l.account_no  AS "Loan Account No.", pl.name AS "Product", f.name  AS fund, l.principal_amount  AS "Loan Amount", l.total_repayment_derived AS "Total Repaid", l.annual_nominal_interest_rate  AS "Annual Nominal Interest Rate", DATE_TRUNC('day', l.disbursedon_date) AS "Disbursed", DATE_TRUNC('day', l.closedon_date)  AS "Closed", l.principal_repaid_derived  AS "Principal Repaid", l.interest_repaid_derived AS "Interest Repaid", l.fee_charges_repaid_derived  AS "Fees Repaid", l.penalty_charges_repaid_derived  AS "Penalties Repaid", lo.display_name AS "Loan Officer" FROM      m_office o JOIN      m_office ounder ON        ounder.hierarchy LIKE Concat(o.hierarchy, '%') AND       ounder.hierarchy LIKE Concat('${currentUserHierarchy}', '%') JOIN      m_client c ON        c.office_id = ounder.id JOIN      m_loan l ON        l.client_id = c.id JOIN      m_product_loan pl ON        pl.id = l.product_id LEFT JOIN m_staff lo ON        lo.id = l.loan_officer_id LEFT JOIN m_currency cur ON        cur.code = l.currency_code LEFT JOIN m_fund f ON        f.id = l.fund_id WHERE     o.id = '${officeId}' AND       ( l.currency_code = '${currencyId}' OR        '-1' = '${currencyId}') AND       ( l.product_id = '${loanProductId}' OR        '-1' = '${loanProductId}') AND       ( Coalesce(l.loan_officer_id, -10) = ${loanOfficerId} OR        '-1' = ${loanOfficerId}) AND       ( coalesce(l.fund_id, -10) = ${fundId} OR        -1 = ${fundId}) AND       ( coalesce(l.loanpurpose_cv_id, -10) = ${loanPurposeId} OR        -1 = ${loanPurposeId}) AND       ( CASE WHEN ${obligDateType} = 1 THEN l.closedon_date BETWEEN '${startDate}' AND '${endDate}' WHEN ${obligDateType} = 2 THEN l.disbursedon_date BETWEEN '${startDate}' AND '${endDate}' ELSE 1 = 1 end) AND       l.loan_status_id = 600 GROUP BY  l.id, ounder.hierarchy, ounder.name, cur.display_symbol, c.account_no, c.display_name, pl.name, f.name, lo.display_name ORDER BY  ounder.hierarchy, l.currency_code, c.account_no, l.account_no	Individual Client   Report	t	t	f
14	Obligation Met Loans Summary	Table	\N	Loan	SELECT    Concat(REPEAT('..', ((Length(ounder.hierarchy ) - Length(REPLACE(ounder.hierarchy, '.', '')) - 1))), ounder.name) AS "Office/Branch", Coalesce(cur.display_symbol, l.currency_code) AS currency, Count(DISTINCT(c.id)) AS "No. of Clients", Count(DISTINCT(l.id)) AS "No. of Loans", Sum(l.principal_amount) AS "Total Loan Amount", Sum(l.principal_repaid_derived) AS "Total Principal Repaid", Sum(l.interest_repaid_derived)  AS "Total Interest Repaid", Sum(l.fee_charges_repaid_derived) AS "Total Fees Repaid", Sum(l.penalty_charges_repaid_derived) AS "Total Penalties Repaid", Sum(l.interest_waived_derived)  AS "Total Interest Waived", Sum(l.fee_charges_waived_derived) AS "Total Fees Waived", Sum(l.penalty_charges_waived_derived) AS "Total Penalties Waived" FROM      m_office o JOIN      m_office ounder ON        ounder.hierarchy LIKE concat(o.hierarchy, '%') AND       ounder.hierarchy LIKE concat('${currentUserHierarchy}', '%') JOIN      m_client c ON        c.office_id = ounder.id JOIN      m_loan l ON        l.client_id = c.id JOIN      m_product_loan pl ON        pl.id = l.product_id LEFT JOIN m_staff lo ON        lo.id = l.loan_officer_id LEFT JOIN m_currency cur ON        cur.code = l.currency_code LEFT JOIN m_fund f ON        f.id = l.fund_id WHERE     o.id = '${officeId}' AND       ( l.currency_code = '${currencyId}' OR        '-1' = '${currencyId}') AND       ( l.product_id = '${loanProductId}' OR        '-1' = '${loanProductId}') AND       ( coalesce(l.loan_officer_id, -10) = ${loanOfficerId} OR        '-1' = ${loanOfficerId}) AND       ( coalesce(l.fund_id, -10) = ${fundId} OR        -1 = ${fundId}) AND       ( coalesce(l.loanpurpose_cv_id, -10) = ${loanPurposeId} OR        -1 = ${loanPurposeId}) AND       ( CASE WHEN ${obligDateType} = 1 THEN l.closedon_date BETWEEN '${startDate}' AND '${endDate}' WHEN ${obligDateType} = 2 THEN l.disbursedon_date BETWEEN '${startDate}' AND '${endDate}' ELSE 1 = 1 end) AND       l.loan_status_id = 600 GROUP BY  ounder.hierarchy, l.currency_code, ounder.name, cur.display_symbol ORDER BY  ounder.hierarchy, l.currency_code	Individual Client   Report	t	t	f
15	Portfolio at Risk	Table	\N	Loan	SELECT x."Currency", x."Principal Outstanding", x."Principal Overdue", x."Interest Outstanding", x."Interest Overdue", x."Fees Outstanding", x."Fees Overdue", x."Penalties Outstanding", x."Penalties Overdue", ( CASE WHEN ${parType} = 1 THEN cast(round((x."Principal Overdue" * 100) / x."Principal Outstanding", 2) AS CHAR) WHEN ${parType} = 2 THEN cast(round(((x."Principal Overdue" + x."Interest Overdue") * 100) / (x."Principal Outstanding" + x."Interest Outstanding"), 2) AS CHAR) WHEN ${parType} = 3 THEN cast(round(((x."Principal Overdue" + x."Interest Overdue" + x."Fees Overdue") * 100) / (x."Principal Outstanding" + x."Interest Outstanding" + x."Fees Outstanding"), 2) AS CHAR) WHEN ${parType} = 4 THEN cast(round(((x."Principal Overdue" + x."Interest Overdue" + x."Fees Overdue" + x."Penalties Overdue") * 100) / (x."Principal Outstanding" + x."Interest Outstanding" + x."Fees Outstanding" + x."Penalties Overdue"), 2) AS CHAR) ELSE 'invalid PAR Type' end) AS "Portfolio at Risk %" FROM   ( SELECT    coalesce(cur.display_symbol, l.currency_code) AS "Currency", sum(l.principal_outstanding_derived)  AS "Principal Outstanding", sum(laa.principal_overdue_derived)  AS "Principal Overdue", sum(l.interest_outstanding_derived) AS "Interest Outstanding", sum(laa.interest_overdue_derived) AS "Interest Overdue", sum(l.fee_charges_outstanding_derived)  AS "Fees Outstanding", sum(laa.fee_charges_overdue_derived)  AS "Fees Overdue", sum(penalty_charges_outstanding_derived)  AS "Penalties Outstanding", sum(laa.penalty_charges_overdue_derived)  AS "Penalties Overdue" FROM      m_office o JOIN      m_office ounder ON        ounder.hierarchy LIKE concat(o.hierarchy, '%') AND       ounder.hierarchy LIKE concat('${currentUserHierarchy}', '%') JOIN      m_client c ON        c.office_id = ounder.id JOIN      m_loan l ON        l.client_id = c.id LEFT JOIN m_staff lo ON        lo.id = l.loan_officer_id LEFT JOIN m_currency cur ON        cur.code = l.currency_code LEFT JOIN m_fund f ON        f.id = l.fund_id LEFT JOIN m_code_value purp ON        purp.id = l.loanpurpose_cv_id LEFT JOIN m_product_loan p ON        p.id = l.product_id LEFT JOIN m_loan_arrears_aging laa ON        laa.loan_id = l.id WHERE     o.id = '${officeId}' AND       ( l.currency_code = '${currencyId}' OR        '-1' = '${currencyId}') AND       ( l.product_id = '${loanProductId}' OR        '-1' = '${loanProductId}') AND       ( coalesce(l.loan_officer_id, -10) = ${loanOfficerId} OR        '-1' = ${loanOfficerId}) AND       ( coalesce(l.fund_id, -10) = ${fundId} OR        -1 = ${fundId}) and (coalesce(l.loanpurpose_cv_id, -10) = ${loanPurposeId} OR        -1 = ${loanPurposeId}) AND       l.loan_status_id = 300 GROUP BY  l.currency_code, cur.display_symbol ORDER BY  l.currency_code) x	Covers all loans.    For larger MFIs … we should add some derived fields on loan (or a 1:1 loan related table like mifos 2.x does)  Principle, Interest, Fees, Penalties Outstanding and Overdue (possibly waived and written off too)	t	t	f
16	Portfolio at Risk by Branch	Table	\N	Loan	SELECT   Concat(REPEAT('..', ((Length(mo.hierarchy ) - Length(REPLACE(mo.hierarchy, '.', '')) - 1))), mo.name) AS "Office/Branch", x."Currency", x."Principal Outstanding" , x."Principal Overdue", x."Interest Outstanding", x."Interest Overdue", x."Fees Outstanding", x."Fees Overdue", x."Penalties Outstanding", x."Penalties Overdue", ( CASE WHEN ${parType} = 1 THEN cast(round((x."Principal Overdue" * 100) / x."Principal Outstanding", 2) AS  CHAR) WHEN ${parType} = 2 THEN cast(round(((x."Principal Overdue" + x."Interest Overdue") * 100) / (x."Principal Outstanding" + x."Interest Outstanding"), 2) AS      CHAR) WHEN ${parType} = 3 THEN cast(round(((x."Principal Overdue" + x."Interest Overdue" + x."Fees Overdue") * 100) / (x."Principal Outstanding" + x."Interest Outstanding" + x."Fees Outstanding"), 2) AS CHAR) WHEN ${parType} = 4 THEN cast(round(((x."Principal Overdue" + x."Interest Overdue" + x."Fees Overdue" + x."Penalties Overdue") * 100) / (x."Principal Outstanding" + x."Interest Outstanding" + x."Fees Outstanding" + x."Penalties Overdue"), 2) AS           CHAR) ELSE 'invalid PAR Type' end) AS "Portfolio at Risk %" FROM     m_office mo JOIN ( SELECT    ounder.id  AS "branch", coalesce(cur.display_symbol, l.currency_code) AS "Currency", sum(l.principal_outstanding_derived)  AS "Principal Outstanding", sum(laa.principal_overdue_derived)  AS "Principal Overdue", sum(l.interest_outstanding_derived) AS "Interest Outstanding", sum(laa.interest_overdue_derived) AS "Interest Overdue", sum(l.fee_charges_outstanding_derived)  AS "Fees Outstanding", sum(laa.fee_charges_overdue_derived)  AS "Fees Overdue", sum(penalty_charges_outstanding_derived)  AS "Penalties Outstanding", sum(laa.penalty_charges_overdue_derived)  AS "Penalties Overdue" FROM      m_office o JOIN      m_office ounder ON        ounder.hierarchy LIKE concat(o.hierarchy, '%') AND       ounder.hierarchy LIKE concat('${currentUserHierarchy}', '%') JOIN      m_client c ON        c.office_id = ounder.id JOIN      m_loan l ON        l.client_id = c.id LEFT JOIN m_staff lo ON        lo.id = l.loan_officer_id LEFT JOIN m_currency cur ON        cur.code = l.currency_code LEFT JOIN m_fund f ON        f.id = l.fund_id LEFT JOIN m_code_value purp ON        purp.id = l.loanpurpose_cv_id LEFT JOIN m_product_loan p ON        p.id = l.product_id LEFT JOIN m_loan_arrears_aging laa ON        laa.loan_id = l.id WHERE     o.id = '${officeId}' AND       ( l.currency_code = '${currencyId}' OR        '-1' = '${currencyId}') AND       ( l.product_id = '${loanProductId}' OR        '-1' = '${loanProductId}') AND       ( coalesce(l.loan_officer_id, -10) = ${loanOfficerId} OR        '-1' = ${loanOfficerId}) AND       ( coalesce(l.fund_id, -10) = ${fundId} OR        -1 = ${fundId}) and (coalesce(l.loanpurpose_cv_id, -10) = ${loanPurposeId} OR        -1 = ${loanPurposeId}) AND       l.loan_status_id = 300 GROUP BY  ounder.id, l.currency_code, cur.display_symbol ) x ON       x.branch = mo.id ORDER BY mo.hierarchy, x."Currency"	Covers all loans.    For larger MFIs … we should add some derived fields on loan (or a 1:1 loan related table like mifos 2.x does)  Principle, Interest, Fees, Penalties Outstanding and Overdue (possibly waived and written off too)	t	t	f
20	Funds Disbursed Between Dates Summary	Table	\N	Fund	SELECT    Coalesce(f.name, '-') AS fund, Coalesce(cur.display_symbol, l.currency_code) AS currency, Round(Sum(l.principal_amount), 4) AS disbursed_amount FROM      m_office ounder JOIN      m_client c ON        c.office_id = ounder.id JOIN      m_loan l ON        l.client_id = c.id JOIN      m_currency cur ON        cur.code = l.currency_code LEFT JOIN m_fund f ON        f.id = l.fund_id WHERE     disbursedon_date BETWEEN '${startDate}' AND '${endDate}' AND       ( coalesce(l.fund_id, -10) = ${fundId} OR        -1 = ${fundId}) AND       ( l.currency_code = '${currencyId}' OR        '-1' = '${currencyId}') AND       ounder.hierarchy LIKE concat('${currentUserHierarchy}', '%') GROUP BY  coalesce(f.name, '-') , coalesce(cur.display_symbol, l.currency_code) ORDER BY  coalesce(f.name, '-') , coalesce(cur.display_symbol, l.currency_code)	\N	t	t	f
21	Funds Disbursed Between Dates Summary by Office	Table	\N	Fund	SELECT    Concat(REPEAT('..', ((Length(ounder.hierarchy) - Length(REPLACE(ounder.hierarchy, '.', '')) - 1))), ounder.name) AS "Office/Branch", Coalesce(f.name, '-')  AS fund, Coalesce(cur.display_symbol, l.currency_code)  AS currency, Round(Sum(l.principal_amount), 4)  AS disbursed_amount FROM      m_office o JOIN      m_office ounder ON        ounder.hierarchy LIKE Concat(o.hierarchy, '%') AND       ounder.hierarchy LIKE Concat('${currentUserHierarchy}', '%') JOIN      m_client c ON        c.office_id = ounder.id JOIN      m_loan l ON        l.client_id = c.id JOIN      m_currency cur ON        cur.code = l.currency_code LEFT JOIN m_fund f ON        f.id = l.fund_id WHERE     disbursedon_date BETWEEN '${startDate}' AND '${endDate}' AND       o.id = '${officeId}' AND       ( coalesce(l.fund_id, -10) = ${fundId} OR        -1 = ${fundId}) AND       ( l.currency_code = '${currencyId}' OR        '-1' = '${currencyId}') GROUP BY  ounder.name, coalesce(f.name, '-') , coalesce(cur.display_symbol, l.currency_code), ounder.hierarchy ORDER BY  ounder.name, coalesce(f.name, '-') , coalesce(cur.display_symbol, l.currency_code)	\N	t	t	f
51	Written-Off Loans	Table	\N	Loan	SELECT    Concat(REPEAT('..', ((Length(ounder.hierarchy) - Length(REPLACE(ounder.hierarchy, '.', '')) - 1))), ounder.name) AS "Office/Branch", Coalesce(cur.display_symbol, ml.currency_code) AS currency, c.account_no AS "Client Account No.", c.display_name AS "Client Name", ml.account_no  AS "Loan Account No.", mpl.name AS "Product Name", ml.disbursedon_date  AS "Disbursed Date", lt.transaction_date  AS "Written Off date", ml.principal_amount  AS "Loan Amount", Coalesce(lt.principal_portion_derived, 0)  AS "Written-Off                    Principal", Coalesce(lt.interest_portion_derived, 0) AS "Written-Off Interest", Coalesce(lt.fee_charges_portion_derived,0) AS "Written-Off                    Fees", Coalesce(lt.penalty_charges_portion_derived,0) AS "Written-Off Penalties", n.note AS "Reason For Write-Off", Coalesce(ms.display_name,'-')  AS "Loan Officer Name" FROM      m_office o JOIN      m_office ounder ON        ounder.hierarchy LIKE Concat(o.hierarchy, '%') AND       ounder.hierarchy LIKE Concat('${currentUserHierarchy}', '%') JOIN      m_client c ON        c.office_id = ounder.id JOIN      m_loan ml ON        ml.client_id = c.id JOIN      m_product_loan mpl ON        mpl.id=ml.product_id LEFT JOIN m_staff ms ON        ms.id=ml.loan_officer_id JOIN      m_loan_transaction lt ON        lt.loan_id = ml.id LEFT JOIN m_note n ON        n.loan_transaction_id = lt.id LEFT JOIN m_currency cur ON        cur.code = ml.currency_code WHERE     lt.transaction_type_enum = 6 /*write-off */ AND       lt.is_reversed IS FALSE AND       ml.loan_status_id=601 AND       o.id='${officeId}' AND       ( mpl.id='${loanProductId}' OR        '${loanProductId}'=-1) AND       lt.transaction_date BETWEEN '${startDate}' AND '${endDate}' AND       ( ml.currency_code = '${currencyId}' OR        '-1' = '${currencyId}') ORDER BY  ounder.hierarchy, coalesce(cur.display_symbol, ml.currency_code), ml.account_no	Individual Lending Report. Written Off Loans	t	t	f
52	Aging Detail	Table	\N	Loan	SELECT     Concat(Repeat('..', ((Length(ounder.hierarchy) - Length(Replace(ounder.hierarchy , '.', '')) - 1))), ounder.NAME) AS "Office/Branch", COALESCE(cur.display_symbol, ml.currency_code)  AS currency, mc.account_no AS "Client Account No.", mc.display_name  AS "Client Name", ml.account_no  AS "Account Number", ml.principal_amount  AS "Loan Amount", ml.principal_disbursed_derived  AS "Original Principal", ml.interest_charged_derived AS "Original Interest", ml.principal_repaid_derived AS "Principal Paid", ml.interest_repaid_derived  AS "Interest Paid", laa.principal_overdue_derived AS "Principal Overdue", laa.interest_overdue_derived  AS "Interest Overdue", Extract(day FROM (CURRENT_DATE::timestamp - laa.overdue_since_date_derived::timestamp))  AS "Days in Arrears", CASE WHEN Extract(day FROM (CURRENT_DATE::timestamp - laa.overdue_since_date_derived::timestamp))<7 THEN '<1' WHEN extract(day FROM (CURRENT_DATE::timestamp - laa.overdue_since_date_derived::timestamp))<8 THEN ' 1' WHEN extract(day FROM (CURRENT_DATE::timestamp - laa.overdue_since_date_derived::timestamp))<15 THEN '2' WHEN extract(day FROM (CURRENT_DATE::timestamp - laa.overdue_since_date_derived::timestamp))<22 THEN ' 3' WHEN extract(day FROM (CURRENT_DATE::timestamp - laa.overdue_since_date_derived::timestamp))<29 THEN ' 4' WHEN extract(day FROM (CURRENT_DATE::timestamp - laa.overdue_since_date_derived::timestamp))<36 THEN ' 5' WHEN extract(day FROM (CURRENT_DATE::timestamp - laa.overdue_since_date_derived::timestamp))<43 THEN ' 6' WHEN extract(day FROM (CURRENT_DATE::timestamp - laa.overdue_since_date_derived::timestamp))<50 THEN ' 7' WHEN extract(day FROM (CURRENT_DATE::timestamp - laa.overdue_since_date_derived::timestamp))<57 THEN ' 8' WHEN extract(day FROM (CURRENT_DATE::timestamp - laa.overdue_since_date_derived::timestamp))<64 THEN ' 9' WHEN extract(day FROM (CURRENT_DATE::timestamp - laa.overdue_since_date_derived::timestamp))<71 THEN '10' WHEN extract(day FROM (CURRENT_DATE::timestamp - laa.overdue_since_date_derived::timestamp))<78 THEN '11' WHEN extract(day FROM (CURRENT_DATE::timestamp - laa.overdue_since_date_derived::timestamp))<85 THEN '12' ELSE '12+' END AS "Weeks In Arrears Band", CASE WHEN extract(day FROM (CURRENT_DATE::timestamp - laa.overdue_since_date_derived::timestamp))<31 THEN '0 - 30' WHEN extract(day FROM (CURRENT_DATE::timestamp - laa.overdue_since_date_derived::timestamp))<61 THEN '30 - 60' WHEN extract(day FROM (CURRENT_DATE::timestamp - laa.overdue_since_date_derived::timestamp))<91 THEN '60 - 90' WHEN extract(day FROM (CURRENT_DATE::timestamp - laa.overdue_since_date_derived::timestamp))<181 THEN '90 - 180' WHEN extract(day FROM (CURRENT_DATE::timestamp - laa.overdue_since_date_derived::timestamp))<361 THEN '180 - 360' ELSE '> 360' END AS "Days in Arrears Band" FROM       m_office mo JOIN       m_office ounder ON         ounder.hierarchy LIKE concat(mo.hierarchy, '%') AND        ounder.hierarchy LIKE concat('${currentUserHierarchy}', '%') INNER JOIN m_client mc ON         mc.office_id=ounder.id INNER JOIN m_loan ml ON         ml.client_id = mc.id INNER JOIN r_enum_value rev ON         rev.enum_id=ml.loan_status_id AND        rev.enum_name = 'loan_status_id' INNER JOIN m_loan_arrears_aging laa ON         laa.loan_id=ml.id LEFT JOIN  m_currency cur ON         cur.code = ml.currency_code WHERE      ml.loan_status_id=300 AND        mo.id='${officeId}' GROUP BY   ounder.hierarchy, ounder.name, cur.display_symbol, ml.currency_code, mc.account_no, mc.display_name, ml.account_no, ml.principal_amount, ml.principal_disbursed_derived, ml.interest_charged_derived, ml.principal_repaid_derived, ml.interest_repaid_derived, laa.principal_overdue_derived, laa.interest_overdue_derived, laa.overdue_since_date_derived ORDER BY   ounder.hierarchy, COALESCE(cur.display_symbol, ml.currency_code), ml.account_no	Loan arrears aging (Weeks)	t	t	f
113	ProgramDirectorStats	Table	\N	Quipo	SELECT    Coalesce(cur.display_symbol, l.currency_code) AS currency, /*This query will return more than one entry if more than one currency is used */ Count(DISTINCT(c.id))  AS activeclients, Count(*) AS activeloans, Sum(l.principal_disbursed_derived) AS disbursedamount, Sum(l.principal_outstanding_derived) AS loanoutstandingamount, Round((Sum(l.principal_outstanding_derived) * 100) / Sum(l.principal_disbursed_derived),2) AS loanoutstandingpc, Sum(Coalesce(lpa.principal_in_advance_derived,0.0))  AS loanpaidinadvance, sum( CASE WHEN ( CURRENT_DATE - 28 * INTERVAL '1 day') > coalesce(laa.overdue_since_date_derived, CURRENT_DATE) THEN l.principal_outstanding_derived ELSE 0 end) AS portfolioatrisk, round((sum( CASE WHEN ( CURRENT_DATE - 28 * INTERVAL '1 day' ) > coalesce(laa.overdue_since_date_derived, CURRENT_DATE) THEN l.principal_outstanding_derived ELSE 0 end) * 100) / sum(l.principal_outstanding_derived), 2) AS portfolioatriskpc, count(DISTINCT( CASE WHEN ( CURRENT_DATE - 28 * INTERVAL '1 day' ) > coalesce(laa.overdue_since_date_derived, CURRENT_DATE) THEN c.id ELSE NULL end)) AS clientsindefault, round((count(DISTINCT( CASE WHEN ( CURRENT_DATE - 28 * INTERVAL '1 day' ) > coalesce(laa.overdue_since_date_derived, CURRENT_DATE) THEN c.id ELSE NULL end)) * 100) / count(DISTINCT(c.id)), 2)  AS clientsindefaultpc, (sum(l.principal_disbursed_derived) / count(*)) AS averageloanamount FROM      m_staff pd JOIN      m_staff bm ON        bm.organisational_role_parent_staff_id = pd.id JOIN      m_staff coord ON        coord.organisational_role_parent_staff_id = bm.id JOIN      m_staff fa ON        fa.organisational_role_parent_staff_id = coord.id JOIN      m_office o ON        o.id = fa.office_id AND       o.hierarchy LIKE concat('${currentUserHierarchy}', '%') JOIN      m_group pgm ON        pgm.staff_id = fa.id JOIN      m_loan l ON        l.group_id = pgm.id AND       l.client_id IS NOT NULL LEFT JOIN m_currency cur ON        cur.code = l.currency_code LEFT JOIN m_loan_arrears_aging laa ON        laa.loan_id = l.id LEFT JOIN m_loan_paid_in_advance lpa ON        lpa.loan_id = l.id JOIN      m_client c ON        c.id = l.client_id WHERE     pd.id = ${staffId} AND       l.loan_status_id = 300 GROUP BY  l.currency_code, cur.display_symbol	Program DirectorStatistics	f	f	f
53	Aging Summary (Arrears in Weeks)	Table	\N	Loan	SELECT    Coalesce(periods.currencyname, periods.currency) AS currency, periods.period_no AS "Weeks In Arrears (Up To)", coalesce(ars.loanid, 0) AS "No Of Loans", coalesce(ars.principal,0.0) AS "Original Principal", coalesce(ars.interest,0.0) AS "Original Interest", coalesce(ars.prinpaid,0.0) AS "Principal Paid", coalesce(ars.intpaid,0.0) AS "Interest Paid", coalesce(ars.prinoverdue,0.0) AS "Principal Overdue", coalesce(ars.intoverdue,0.0) AS "Interest Overdue" FROM      ( SELECT curs.code AS currency, curs.name AS currencyname, pers.* FROM   ( SELECT 'On Schedule' period_no, 1             pid UNION SELECT '1', 2 UNION SELECT '2', 3 UNION SELECT '3', 4 UNION SELECT '4', 5 UNION SELECT '5', 6 UNION SELECT '6', 7 UNION SELECT '7', 8 UNION SELECT '8', 9 UNION SELECT '9', 10 UNION SELECT '10', 11 UNION SELECT '11', 12 UNION SELECT '12', 13 UNION SELECT '12+', 14) pers, ( SELECT     DISTINCT ON (moc.code) moc.code, moc.name FROM       m_office mo2 INNER JOIN m_office ounder2 ON         ounder2.hierarchy LIKE concat(mo2.hierarchy, '%') AND        ounder2.hierarchy LIKE concat('${currentUserHierarchy}', '%') INNER JOIN m_client mc2 ON         mc2.office_id=ounder2.id INNER JOIN m_loan ml2 ON         ml2.client_id = mc2.id INNER JOIN m_organisation_currency moc ON         moc.code = ml2.currency_code WHERE      ml2.loan_status_id=300 /* active */ AND        mo2.id='${officeId}' AND        ( ml2.currency_code = '${currencyId}' OR         '-1' = '${currencyId}') GROUP BY moc.code, moc.name) curs) periods LEFT JOIN ( SELECT   z.currency, z.arrperiod, count(z.loanid)  AS loanid, sum(z.principal) AS principal, sum(z.interest)  AS interest, sum(z.prinpaid)  AS prinpaid, sum(z.intpaid) AS intpaid, sum(z.prinoverdue) AS prinoverdue, sum(z.intoverdue)  AS intoverdue FROM     ( SELECT x.loanid, x.currency, x.principal, x.interest, x.prinpaid, x.intpaid, x.prinoverdue, x.intoverdue, CASE WHEN extract(day FROM (CURRENT_DATE::TIMESTAMP - MINOVERDUEDATE::TIMESTAMP))<1 THEN 'On Schedule' WHEN extract(day FROM (CURRENT_DATE::TIMESTAMP - MINOVERDUEDATE::TIMESTAMP))<8 THEN '1' WHEN extract(day FROM (CURRENT_DATE::TIMESTAMP - MINOVERDUEDATE::TIMESTAMP))<15 THEN '2' WHEN extract(day FROM (CURRENT_DATE::TIMESTAMP - MINOVERDUEDATE::TIMESTAMP))<22 THEN '3' WHEN extract(day FROM (CURRENT_DATE::TIMESTAMP - MINOVERDUEDATE::TIMESTAMP))<29 THEN '4' WHEN extract(day FROM (CURRENT_DATE::TIMESTAMP - MINOVERDUEDATE::TIMESTAMP))<36 THEN '5' WHEN extract(day FROM (CURRENT_DATE::TIMESTAMP - MINOVERDUEDATE::TIMESTAMP))<43 THEN '6' WHEN extract(day FROM (CURRENT_DATE::TIMESTAMP - MINOVERDUEDATE::TIMESTAMP))<50 THEN '7' WHEN extract(day FROM (CURRENT_DATE::TIMESTAMP - MINOVERDUEDATE::TIMESTAMP))<57 THEN '8' WHEN extract(day FROM (CURRENT_DATE::TIMESTAMP - MINOVERDUEDATE::TIMESTAMP))<64 THEN '9' WHEN extract(day FROM (CURRENT_DATE::TIMESTAMP - MINOVERDUEDATE::TIMESTAMP))<71 THEN '10' WHEN extract(day FROM (CURRENT_DATE::TIMESTAMP - MINOVERDUEDATE::TIMESTAMP))<78 THEN '11' WHEN extract(day FROM (CURRENT_DATE::TIMESTAMP - MINOVERDUEDATE::TIMESTAMP))<85 THEN '12' ELSE '12+' end AS arrperiod FROM   ( SELECT     ml.id  AS loanid, ml.currency_code AS currency, ml.principal_disbursed_derived AS principal, ml.interest_charged_derived  AS interest, ml.principal_repaid_derived  AS prinpaid, ml.interest_repaid_derived  AS intpaid, laa.principal_overdue_derived  AS prinoverdue, laa.interest_overdue_derived AS intoverdue, coalesce(laa.overdue_since_date_derived, CURRENT_DATE) AS minoverduedate FROM       m_office mo INNER JOIN m_office ounder ON         ounder.hierarchy LIKE concat(mo.hierarchy, '%') AND        ounder.hierarchy LIKE concat('${currentUserHierarchy}', '%') INNER JOIN m_client mc ON         mc.office_id=ounder.id INNER JOIN m_loan ml ON         ml.client_id = mc.id LEFT JOIN  m_loan_arrears_aging laa ON         laa.loan_id = ml.id WHERE      ml.loan_status_id=300 AND        mo.id='${officeId}' AND        ( ml.currency_code = '${currencyId}' OR         '-1' = '${currencyId}') GROUP BY   ml.id, laa.principal_overdue_derived, laa.interest_overdue_derived, laa.overdue_since_date_derived) x ) z GROUP BY z.currency, z.arrperiod ) ars ON        ars.arrperiod=periods.period_no AND       ars.currency = periods.currency ORDER BY  periods.currency, periods.pid	Loan amount in arrears by branch	t	t	f
54	Rescheduled Loans	Table	\N	Loan	SELECT    Concat(REPEAT('..', ((Length(ounder.hierarchy) - Length(REPLACE(ounder.hierarchy, '.', '')) - 1))), ounder.name) AS "Office/Branch", Coalesce(cur.display_symbol, ml.currency_code) AS currency, c.account_no AS "Client Account No.", c.display_name AS "Client Name", ml.account_no  AS "Loan Account No.", mpl.name AS "Product Name", ml.disbursedon_date  AS "Disbursed Date", lt.transaction_date  AS "Written Off date", ml.principal_amount  AS "Loan Amount", Coalesce(lt.principal_portion_derived, 0)  AS "Rescheduled Principal", Coalesce(lt.interest_portion_derived, 0) AS "Rescheduled Interest", Coalesce(lt.fee_charges_portion_derived,0) AS "Rescheduled Fees", Coalesce(lt.penalty_charges_portion_derived,0) AS "Rescheduled Penalties", n.note AS "Reason For Rescheduling", Coalesce(ms.display_name,'-')  AS "Loan Officer Name" FROM      m_office o JOIN      m_office ounder ON        ounder.hierarchy LIKE Concat(o.hierarchy, '%') AND       ounder.hierarchy LIKE Concat('${currentUserHierarchy}', '%') JOIN      m_client c ON        c.office_id = ounder.id JOIN      m_loan ml ON        ml.client_id = c.id JOIN      m_product_loan mpl ON        mpl.id=ml.product_id LEFT JOIN m_staff ms ON        ms.id=ml.loan_officer_id JOIN      m_loan_transaction lt ON        lt.loan_id = ml.id LEFT JOIN m_note n ON        n.loan_transaction_id = lt.id LEFT JOIN m_currency cur ON        cur.code = ml.currency_code WHERE     lt.transaction_type_enum = 7 /*marked for rescheduling */ AND       lt.is_reversed IS FALSE AND       ml.loan_status_id=602 AND       o.id='${officeId}' AND       ( mpl.id='${loanProductId}' OR        '${loanProductId}'=-1) AND       lt.transaction_date BETWEEN '${startDate}' AND '${endDate}' AND       ( ml.currency_code = '${currencyId}' OR        '-1' = '${currencyId}') ORDER BY  ounder.hierarchy, coalesce(cur.display_symbol, ml.currency_code), ml.account_no	Individual Lending Report. Rescheduled Loans.  The ability to reschedule (or mark that you have rescheduled the loan elsewhere) is a legacy of the older Mifos product.  Needed for migration.	t	t	f
55	Active Loans Passed Final Maturity	Table	\N	Loan	SELECT    Concat(REPEAT('..', ((Length(ounder.hierarchy) - Length(REPLACE(ounder.hierarchy, '.', '')) - 1))), ounder.name) AS "Office/Branch", Coalesce(cur.display_symbol, l.currency_code)  AS currency, lo.display_name  AS "Loan Officer", c.display_name AS "Client", l.account_no AS "Loan Account No.", pl.name  AS "Product", f.name AS fund, l.principal_amount AS "Loan Amount", l.annual_nominal_interest_rate AS "Annual Nominal Interest Rate", DATE_TRUNC('day', l.disbursedon_date)  AS "Disbursed Date", DATE_TRUNC('day', l.expected_maturedon_date) AS "Expected Matured On", l.principal_repaid_derived AS "Principal Repaid", l.principal_outstanding_derived  AS "Principal Outstanding", laa.principal_overdue_derived  AS "Principal Overdue", l.interest_repaid_derived  AS "Interest Repaid", l.interest_outstanding_derived AS "Interest Outstanding", laa.interest_overdue_derived AS "Interest Overdue", l.fee_charges_repaid_derived AS "Fees Repaid", l.fee_charges_outstanding_derived  AS "Fees Outstanding", laa.fee_charges_overdue_derived  AS "Fees Overdue", l.penalty_charges_repaid_derived AS "Penalties Repaid", l.penalty_charges_outstanding_derived  AS "Penalties Outstanding", laa.penalty_charges_overdue_derived  AS "Penalties Overdue" FROM      m_office o JOIN      m_office ounder ON        ounder.hierarchy LIKE Concat(o.hierarchy, '%') AND       ounder.hierarchy LIKE Concat('${currentUserHierarchy}', '%') JOIN      m_client c ON        c.office_id = ounder.id JOIN      m_loan l ON        l.client_id = c.id JOIN      m_product_loan pl ON        pl.id = l.product_id LEFT JOIN m_staff lo ON        lo.id = l.loan_officer_id LEFT JOIN m_currency cur ON        cur.code = l.currency_code LEFT JOIN m_fund f ON        f.id = l.fund_id LEFT JOIN m_loan_arrears_aging laa ON        laa.loan_id = l.id WHERE     o.id = '${officeId}' AND       ( l.currency_code = '${currencyId}' OR        '-1' = '${currencyId}') AND       ( l.product_id = '${loanProductId}' OR        '-1' = '${loanProductId}') AND       ( Coalesce(l.loan_officer_id, -10) = ${loanOfficerId} OR        '-1' = ${loanOfficerId}) AND       ( coalesce(l.fund_id, -10) = ${fundId} OR        -1 = ${fundId}) AND       ( coalesce(l.loanpurpose_cv_id, -10) = ${loanPurposeId} OR        -1 = ${loanPurposeId}) AND       l.loan_status_id = 300 AND       l.expected_maturedon_date < CURRENT_DATE GROUP BY  l.id, ounder.hierarchy, ounder.name, cur.display_symbol, lo.display_name, c.display_name, f.name, pl.name, laa.principal_overdue_derived, laa.interest_overdue_derived, laa.fee_charges_overdue_derived, laa.penalty_charges_overdue_derived, c.account_no ORDER BY  ounder.hierarchy, l.currency_code, c.account_no, l.account_no	Individual Client   Report	t	t	f
56	Active Loans Passed Final Maturity Summary	Table	\N	Loan	SELECT   Concat(REPEAT('..', ((Length(mo.hierarchy ) - Length(REPLACE(mo.hierarchy, '.', '')) - 1))), mo.name) AS "Office/Branch", x.currency  AS currency, x.client_count  AS "No. of Clients", x.active_loan_count AS "No. Active                    Loans", x. arrears_loan_count AS "No. of Loans in Arrears", x.principal AS "Total Loans Disbursed", x.principal_repaid  AS "Principal Repaid", x.principal_outstanding AS "Principal Outstanding", x.principal_overdue AS "Principal Overdue", x.interest  AS "Total Interest", x.interest_repaid AS "Interest Repaid", x.interest_outstanding  AS "Interest Outstanding", x.interest_overdue  AS "Interest Overdue", x.fees  AS "Total Fees", x.fees_repaid AS "Fees Repaid", x.fees_outstanding  AS "Fees Outstanding", x.fees_overdue  AS "Fees Overdue", x.penalties AS "Total Penalties", x.penalties_repaid  AS "Penalties Repaid", x.penalties_outstanding AS "Penalties Outstanding", x.penalties_overdue AS "Penalties Overdue", ( CASE WHEN ${parType} = 1 THEN cast(round((x.principal_overdue * 100) / x.principal_outstanding, 2) AS                                                                                                                                     CHAR) WHEN ${parType} = 2 THEN cast(round(((x.principal_overdue + x.interest_overdue) * 100) / (x.principal_outstanding + x.interest_outstanding), 2) AS                                                                                   CHAR) WHEN ${parType} = 3 THEN cast(round(((x.principal_overdue + x.interest_overdue + x.fees_overdue) * 100) / (x.principal_outstanding + x.interest_outstanding + x.fees_outstanding), 2) AS                                             CHAR) WHEN ${parType} = 4 THEN cast(round(((x.principal_overdue + x.interest_overdue + x.fees_overdue + x.penalties_overdue) * 100) / (x.principal_outstanding + x.interest_outstanding + x.fees_outstanding + x.penalties_overdue), 2) AS CHAR) ELSE 'invalid PAR Type' end) AS "Portfolio at Risk %" FROM     m_office mo JOIN ( SELECT    ounder.id  AS branch, coalesce(cur.display_symbol, l.currency_code)  AS currency, count(DISTINCT(c.id))  AS client_count, count(DISTINCT(l.id))  AS active_loan_count, count(DISTINCT(laa.loan_id) )  AS arrears_loan_count, sum(l.principal_disbursed_derived) AS principal, sum(l.principal_repaid_derived)  AS principal_repaid, sum(l.principal_outstanding_derived) AS principal_outstanding, sum(coalesce(laa.principal_overdue_derived,0)) AS principal_overdue, sum(l.interest_charged_derived)  AS interest, sum(l.interest_repaid_derived) AS interest_repaid, sum(l.interest_outstanding_derived)  AS interest_outstanding, sum(coalesce(laa.interest_overdue_derived,0))  AS interest_overdue, sum(l.fee_charges_charged_derived) AS fees, sum(l.fee_charges_repaid_derived)  AS fees_repaid, sum(l.fee_charges_outstanding_derived) AS fees_outstanding, sum(coalesce(laa.fee_charges_overdue_derived,0)) AS fees_overdue, sum(l.penalty_charges_charged_derived) AS penalties, sum(l.penalty_charges_repaid_derived)  AS penalties_repaid, sum(l.penalty_charges_outstanding_derived) AS penalties_outstanding, sum(coalesce(laa.penalty_charges_overdue_derived,0)) AS penalties_overdue FROM      m_office o JOIN      m_office ounder ON        ounder.hierarchy LIKE concat(o.hierarchy, '%') AND       ounder.hierarchy LIKE concat('${currentUserHierarchy}', '%') JOIN      m_client c ON        c.office_id = ounder.id JOIN      m_loan l ON        l.client_id = c.id LEFT JOIN m_currency cur ON        cur.code = l.currency_code LEFT JOIN m_loan_arrears_aging laa ON        laa.loan_id = l.id WHERE     o.id = '${officeId}' AND       ( l.currency_code = '${currencyId}' OR        '-1' = '${currencyId}') AND       ( l.product_id = '${loanProductId}' OR        '-1' = '${loanProductId}') AND       ( coalesce(l.loan_officer_id, -10) = ${loanOfficerId} OR        '-1' = ${loanOfficerId}) AND       ( coalesce(l.fund_id, -10) = ${fundId} OR        -1 = ${fundId}) and (coalesce(l.loanpurpose_cv_id, -10) = ${loanPurposeId} OR        -1 = ${loanPurposeId}) AND       l.loan_status_id = 300 AND       l.expected_maturedon_date < CURRENT_DATE GROUP BY  ounder.id, cur.display_symbol, l.currency_code) x ON       x.branch = mo.id ORDER BY mo.hierarchy, x.currency	\N	t	t	f
57	Active Loans in last installment	Table	\N	Loan	SELECT    Concat(REPEAT('..', ((Length(lastinstallment.hierarchy) - Length(REPLACE(lastinstallment.hierarchy, '.', '')) - 1))), lastinstallment.branch) AS "Office/Branch", lastinstallment.currency, lastinstallment."Loan Officer", lastinstallment."Client Account No", lastinstallment."Client", lastinstallment."Loan Account No", lastinstallment."Product", lastinstallment."Fund", lastinstallment."Loan Amount", lastinstallment."Annual Nominal Interest Rate", lastinstallment."Disbursed", lastinstallment."Expected Matured On" , l.principal_repaid_derived  AS "Principal Repaid", l.principal_outstanding_derived AS "Principal Outstanding", laa.principal_overdue_derived AS "Principal Overdue", l.interest_repaid_derived AS "Interest Repaid", l.interest_outstanding_derived  AS "Interest Outstanding", laa.interest_overdue_derived  AS "Interest Overdue", l.fee_charges_repaid_derived  AS "Fees Repaid", l.fee_charges_outstanding_derived AS "Fees Outstanding", laa.fee_charges_overdue_derived AS "Fees Overdue", l.penalty_charges_repaid_derived  AS "Penalties Repaid", l.penalty_charges_outstanding_derived AS "Penalties Outstanding", laa.penalty_charges_overdue_derived AS "Penalties Overdue" FROM      ( SELECT    l.id AS loanid, l.number_of_repayments, Min(r.installment), ounder.id, ounder.hierarchy, ounder.name AS branch, Coalesce(cur.display_symbol, l.currency_code) AS currency, lo.display_name AS "Loan Officer", c.account_no  AS "Client Account No", c.display_name  AS "Client", l.account_no  AS "Loan Account No", pl.name AS "Product", f.name  AS "Fund", l.principal_amount  AS "Loan Amount", l.annual_nominal_interest_rate  AS "Annual Nominal Interest Rate", DATE_TRUNC('day', l.disbursedon_date) AS "Disbursed", DATE_TRUNC('day', l.expected_maturedon_date)  AS "Expected Matured On" FROM      m_office o JOIN      m_office ounder ON        ounder.hierarchy LIKE Concat(o.hierarchy, '%') AND       ounder.hierarchy LIKE Concat('${currentUserHierarchy}', '%') JOIN      m_client c ON        c.office_id = ounder.id JOIN      m_loan l ON        l.client_id = c.id JOIN      m_product_loan pl ON        pl.id = l.product_id LEFT JOIN m_staff lo ON        lo.id = l.loan_officer_id LEFT JOIN m_currency cur ON        cur.code = l.currency_code LEFT JOIN m_fund f ON        f.id = l.fund_id LEFT JOIN m_loan_repayment_schedule r ON        r.loan_id = l.id WHERE     o.id = '${officeId}' AND       ( l.currency_code = '${currencyId}' OR        '-1' = '${currencyId}') AND       ( l.product_id = '${loanProductId}' OR        '-1' = '${loanProductId}') AND       ( Coalesce(l.loan_officer_id, -10) = ${loanOfficerId} OR        '-1' = ${loanOfficerId}) AND       ( coalesce(l.fund_id, -10) = ${fundId} OR        -1 = ${fundId}) AND       ( coalesce(l.loanpurpose_cv_id, -10) = ${loanPurposeId} OR        -1 = ${loanPurposeId}) AND       l.loan_status_id = 300 AND       r.completed_derived IS FALSE AND       r.duedate >= CURRENT_DATE GROUP BY  l.id, ounder.id, cur.display_symbol, lo.display_name, c.account_no, c.display_name, pl.name, f.name HAVING    l.number_of_repayments = min(r.installment)) lastinstallment JOIN      m_loan l ON        l.id = lastinstallment.loanid LEFT JOIN m_loan_arrears_aging laa ON        laa.loan_id = l.id ORDER BY  lastinstallment.hierarchy, lastinstallment.currency, lastinstallment."Client Account No", lastinstallment."Loan Account No"	Individual Client   Report	t	t	f
179	Loan Repayment	SMS	Triggered	\N	select ml.id as loanId, mc.id, mc.firstname, COALESCE(mc.middlename, '') as middlename, mc.lastname, mc.display_name as FullName, mobile_no as mobileNo, mc.group_name as GroupName, round(ml.principal_amount, ml.currency_digits) as LoanAmount, round(ml."total_outstanding_derived", ml.currency_digits) as LoanOutstanding, ml."account_no" as LoanAccountId, round(mlt.amountPaid, ml.currency_digits) as repaymentAmount FROM m_office mo JOIN m_office ounder ON ounder.hierarchy LIKE CONCAT(mo.hierarchy, '%') AND ounder.hierarchy like CONCAT('.', '%') LEFT JOIN (select ml.id as loanId, COALESCE(mc.id, mc2.id) as id, COALESCE(mc.firstname, mc2.firstname) as firstname, COALESCE(mc.middlename, COALESCE(mc2.middlename, (''))) as middlename, COALESCE(mc.lastname, mc2.lastname) as lastname, COALESCE(mc.display_name, mc2.display_name) as display_name, COALESCE(mc.status_enum, mc2.status_enum) as status_enum, COALESCE(mc.mobile_no, mc2.mobile_no) as mobile_no, COALESCE(mg.office_id, mc2.office_id) as office_id, COALESCE(mg.staff_id, mc2.staff_id) as staff_id, mg.id as group_id, mg.display_name as group_name from m_loan ml left join m_group mg on mg.id = ml.group_id left join m_group_client mgc on mgc.group_id = mg.id left join m_client mc on mc.id = mgc.client_id left join m_client mc2 on mc2.id = ml.client_id order by loanId) mc on mc.office_id = ounder.id right join m_loan as ml on mc.loanId = ml.id right join(select mlt.amount as amountPaid, mlt.id, mlt.loan_id from m_loan_transaction mlt where mlt.is_reversed = false group by mlt.loan_id, mlt.id) as mlt on mlt.loan_id = ml.id right join m_loan_repayment_schedule as mls1 on ml.id = mls1.loan_id and mls1."completed_derived" = false and mls1.installment = (SELECT MIN(installment) from m_loan_repayment_schedule where loan_id = ml.id and duedate <= CURRENT_DATE and completed_derived = false) where mc.status_enum = 300 and mobile_no is not null and ml."loan_status_id" = 300 and (mo.id = '${officeId}' or '${officeId}' = -1) and (mc.staff_id = ${loanOfficerId} or ${loanOfficerId} = -1) and (ml.loan_type_enum = ${loanType} or ${loanType} = -1) and ml.id in (select mla.loan_id from m_loan_arrears_aging mla) group by ml.id, mc.id, mc.firstname, mc.middlename, mc.lastname, mc.display_name, mc.mobile_no, mc.group_name, mlt.amountPaid	Loan Repayment	f	f	f
58	Active Loans in last installment Summary	Table	\N	Loan	SELECT   Concat(REPEAT('..', ((Length(mo.hierarchy ) - Length(REPLACE(mo.hierarchy, '.', '')) - 1))), mo.name) AS "Office/Branch", x.currency  AS currency, x.client_count  AS "No. of Clients", x.active_loan_count AS "No. Active Loans", x. arrears_loan_count AS "No. of Loans in Arrears", x.principal AS "Total Loans Disbursed", x.principal_repaid  AS "Principal Repaid", x.principal_outstanding AS "Principal Outstanding", x.principal_overdue AS "Principal Overdue", x.interest  AS "Total Interest", x.interest_repaid AS "Interest Repaid", x.interest_outstanding  AS "Interest Outstanding", x.interest_overdue  AS "Interest Overdue", x.fees  AS "Total Fees", x.fees_repaid AS "Fees Repaid", x.fees_outstanding  AS "Fees Outstanding", x.fees_overdue  AS "Fees Overdue", x.penalties AS "Total Penalties", x.penalties_repaid  AS "Penalties Repaid", x.penalties_outstanding AS "Penalties Outstanding", x.penalties_overdue AS "Penalties Overdue", ( CASE WHEN ${parType} = 1 THEN cast(round((x.principal_overdue * 100) / x.principal_outstanding, 2) AS                                                                                                                                     CHAR) WHEN ${parType} = 2 THEN cast(round(((x.principal_overdue + x.interest_overdue) * 100) / (x.principal_outstanding + x.interest_outstanding), 2) AS                                                                                   CHAR) WHEN ${parType} = 3 THEN cast(round(((x.principal_overdue + x.interest_overdue + x.fees_overdue) * 100) / (x.principal_outstanding + x.interest_outstanding + x.fees_outstanding), 2) AS                                             CHAR) WHEN ${parType} = 4 THEN cast(round(((x.principal_overdue + x.interest_overdue + x.fees_overdue + x.penalties_overdue) * 100) / (x.principal_outstanding + x.interest_outstanding + x.fees_outstanding + x.penalties_overdue), 2) AS CHAR) ELSE 'invalid PAR Type' end) AS "Portfolio at Risk %" FROM     m_office mo JOIN ( SELECT    lastinstallment.branchid AS branchid, lastinstallment.currency, count(DISTINCT(lastinstallment.clientid))  AS client_count, count(DISTINCT(lastinstallment.loanid))  AS active_loan_count, count(DISTINCT(laa.loan_id) )  AS arrears_loan_count, sum(l.principal_disbursed_derived) AS principal, sum(l.principal_repaid_derived)  AS principal_repaid, sum(l.principal_outstanding_derived) AS principal_outstanding, sum(coalesce(laa.principal_overdue_derived,0)) AS principal_overdue, sum(l.interest_charged_derived)  AS interest, sum(l.interest_repaid_derived) AS interest_repaid, sum(l.interest_outstanding_derived)  AS interest_outstanding, sum(coalesce(laa.interest_overdue_derived,0))  AS interest_overdue, sum(l.fee_charges_charged_derived) AS fees, sum(l.fee_charges_repaid_derived)  AS fees_repaid, sum(l.fee_charges_outstanding_derived) AS fees_outstanding, sum(coalesce(laa.fee_charges_overdue_derived,0)) AS fees_overdue, sum(l.penalty_charges_charged_derived) AS penalties, sum(l.penalty_charges_repaid_derived)  AS penalties_repaid, sum(l.penalty_charges_outstanding_derived) AS penalties_outstanding, sum(coalesce(laa.penalty_charges_overdue_derived,0)) AS penalties_overdue FROM      ( SELECT    l.id AS loanid, l.number_of_repayments, min(r.installment), ounder.id AS branchid, ounder.hierarchy, ounder.name AS branch, coalesce(cur.display_symbol, l.currency_code) AS currency, lo.display_name AS "Loan Officer", c.id  AS clientid, c.account_no  AS "Client Account No", c.display_name  AS "Client", l.account_no  AS "Loan Account No", pl.name AS "Product", f.name  AS fund, l.principal_amount  AS "Loan Amount", l.annual_nominal_interest_rate  AS "Annual Nominal Interest Rate", DATE_TRUNC('day', l.disbursedon_date) AS "Disbursed", DATE_TRUNC('day', l.expected_maturedon_date)  AS "Expected Matured On" FROM      m_office o JOIN      m_office ounder ON        ounder.hierarchy LIKE concat(o.hierarchy, '%') AND       ounder.hierarchy LIKE concat('${currentUserHierarchy}', '%') JOIN      m_client c ON        c.office_id = ounder.id JOIN      m_loan l ON        l.client_id = c.id JOIN      m_product_loan pl ON        pl.id = l.product_id LEFT JOIN m_staff lo ON        lo.id = l.loan_officer_id LEFT JOIN m_currency cur ON        cur.code = l.currency_code LEFT JOIN m_fund f ON        f.id = l.fund_id LEFT JOIN m_loan_repayment_schedule r ON        r.loan_id = l.id WHERE     o.id = '${officeId}' AND       ( l.currency_code = '${currencyId}' OR        '-1' = '${currencyId}') AND       ( l.product_id = '${loanProductId}' OR        '-1' = '${loanProductId}') AND       ( coalesce(l.loan_officer_id, -10) = ${loanOfficerId} OR        '-1' = ${loanOfficerId}) AND       ( coalesce(l.fund_id, -10) = ${fundId} OR        -1 = ${fundId}) AND       ( coalesce(l.loanpurpose_cv_id, -10) = ${loanPurposeId} OR        -1 = ${loanPurposeId}) AND       l.loan_status_id = 300 AND       r.completed_derived IS FALSE AND       r.duedate >= CURRENT_DATE GROUP BY  l.id, ounder.id, cur.display_symbol, lo.display_name, c.id, pl.name, f.name HAVING    l.number_of_repayments = min(r.installment)) lastinstallment JOIN      m_loan l ON        l.id = lastinstallment.loanid LEFT JOIN m_loan_arrears_aging laa ON        laa.loan_id = l.id GROUP BY  lastinstallment.branchid, lastinstallment.currency) x ON       x.branchid = mo.id ORDER BY mo.hierarchy, x.currency	Individual Client   Report	t	t	f
59	Active Loans by Disbursal Period	Table	\N	Loan	SELECT    Concat(REPEAT('..', ((Length(ounder.hierarchy) - Length(REPLACE(ounder.hierarchy, '.', '')) - 1))), ounder.name) AS "Office/Branch", Coalesce(cur.display_symbol, l.currency_code)  AS currency, c.account_no AS "Client Account No", c.display_name AS "Client", l.account_no AS "Loan Account No", pl.name  AS "Product", f.name AS fund, l.principal_amount AS "Loan Principal Amount", l.annual_nominal_interest_rate AS "Annual Nominal Interest Rate", DATE_TRUNC('day', l.disbursedon_date)  AS "Disbursed Date", l.total_expected_repayment_derived AS "Total Loan (P+I+F+Pen)", l.total_repayment_derived  AS "Total Repaid (P+I+F+Pen)", lo.display_name  AS "Loan Officer" FROM      m_office o JOIN      m_office ounder ON        ounder.hierarchy LIKE Concat(o.hierarchy, '%') AND       ounder.hierarchy LIKE Concat('${currentUserHierarchy}', '%') JOIN      m_client c ON        c.office_id = ounder.id JOIN      m_loan l ON        l.client_id = c.id JOIN      m_product_loan pl ON        pl.id = l.product_id LEFT JOIN m_staff lo ON        lo.id = l.loan_officer_id LEFT JOIN m_currency cur ON        cur.code = l.currency_code LEFT JOIN m_fund f ON        f.id = l.fund_id LEFT JOIN m_loan_arrears_aging laa ON        laa.loan_id = l.id WHERE     o.id = '${officeId}' AND       (l.currency_code = '${currencyId}' OR '-1' = '${currencyId}') AND       (l.product_id = '${loanProductId}' OR '-1' = '${loanProductId}') AND       (Coalesce(l.loan_officer_id, -10) = ${loanOfficerId} OR '-1' = ${loanOfficerId}) AND       (coalesce(l.fund_id, -10) = ${fundId} OR -1 = ${fundId}) AND       (coalesce(l.loanpurpose_cv_id, -10) = ${loanPurposeId} OR -1 = ${loanPurposeId}) AND       l.disbursedon_date BETWEEN '${startDate}' AND '${endDate}' AND       l.loan_status_id = 300 GROUP BY  l.id, ounder.hierarchy, ounder.name, cur.display_symbol, c.account_no, c.display_name, pl.name, f.name, lo.display_name ORDER BY  ounder.hierarchy, l.currency_code, c.account_no, l.account_no	Individual Client   Report	t	t	f
61	Aging Summary (Arrears in Months)	Table	\N	Loan	SELECT    Coalesce(periods.currencyname, periods.currency) AS currency, periods.period_no AS "Days In Arrears", coalesce(ars.loanid, 0) AS "No Of Loans", coalesce(ars.principal,0.0) AS "Original Principal", coalesce(ars.interest,0.0) AS "Original Interest", coalesce(ars.prinpaid,0.0) AS "Principal Paid", coalesce(ars.intpaid,0.0) AS "Interest Paid", coalesce(ars.prinoverdue,0.0) AS "Principal Overdue", coalesce(ars.intoverdue,0.0) AS "Interest Overdue" FROM ( SELECT curs.code AS currency, curs.name AS currencyname, pers.* FROM   ( SELECT 'On Schedule' period_no, 1 pid UNION SELECT '0 - 30', 2 UNION SELECT '30 - 60', 3 UNION SELECT '60 - 90', 4 UNION SELECT '90 - 180', 5 UNION SELECT '180 - 360', 6 UNION SELECT '> 360', 7 ) pers, ( SELECT  DISTINCT ON (moc.code) moc.code, moc.name FROM       m_office mo2 INNER JOIN m_office ounder2 ON         ounder2.hierarchy LIKE concat(mo2.hierarchy, '%') AND        ounder2.hierarchy LIKE concat('${currentUserHierarchy}', '%') INNER JOIN m_client mc2 ON         mc2.office_id=ounder2.id INNER JOIN m_loan ml2 ON         ml2.client_id = mc2.id INNER JOIN m_organisation_currency moc ON         moc.code = ml2.currency_code WHERE      ml2.loan_status_id=300 /* active */ AND        mo2.id='${officeId}' AND        ( ml2.currency_code = '${currencyId}' OR         '-1' = '${currencyId}') GROUP BY moc.code, moc.name) curs) periods LEFT JOIN /* table of aging periods per currency with gaps if no applicable loans */ ( SELECT   z.currency, z.arrperiod, count(z.loanid)  AS loanid, sum(z.principal) AS principal, sum(z.interest)  AS interest, sum(z.prinpaid)  AS prinpaid, sum(z.intpaid) AS intpaid, sum(z.prinoverdue) AS prinoverdue, sum(z.intoverdue)  AS intoverdue FROM     ( SELECT x.loanid, x.currency, x.principal, x.interest, x.prinpaid, x.intpaid, x.prinoverdue, x.intoverdue, CASE WHEN extract(day FROM (CURRENT_DATE::TIMESTAMP - MINOVERDUEDATE::TIMESTAMP))<1 THEN 'On Schedule' WHEN extract(day FROM (CURRENT_DATE::TIMESTAMP - MINOVERDUEDATE::TIMESTAMP))<31 THEN '0 - 30' WHEN extract(day FROM (CURRENT_DATE::TIMESTAMP - MINOVERDUEDATE::TIMESTAMP))<61 THEN '30 - 60' WHEN extract(day FROM (CURRENT_DATE::TIMESTAMP - MINOVERDUEDATE::TIMESTAMP))<91 THEN '60 - 90' WHEN extract(day FROM (CURRENT_DATE::TIMESTAMP - MINOVERDUEDATE::TIMESTAMP))<181 THEN '90 - 180' WHEN extract(day FROM (CURRENT_DATE::TIMESTAMP - MINOVERDUEDATE::TIMESTAMP))<361 THEN '180 - 360' ELSE '> 360' end AS arrperiod FROM   ( SELECT     ml.id  AS loanid, ml.currency_code AS currency, ml.principal_disbursed_derived AS principal, ml.interest_charged_derived  AS interest, ml.principal_repaid_derived  AS prinpaid, ml.interest_repaid_derived AS intpaid, laa.principal_overdue_derived  AS prinoverdue, laa.interest_overdue_derived AS intoverdue, coalesce(laa.overdue_since_date_derived, CURRENT_DATE) AS minoverduedate FROM       m_office mo INNER JOIN m_office ounder ON         ounder.hierarchy LIKE concat(mo.hierarchy, '%') AND        ounder.hierarchy LIKE concat('${currentUserHierarchy}', '%') INNER JOIN m_client mc ON         mc.office_id=ounder.id INNER JOIN m_loan ml ON         ml.client_id = mc.id LEFT JOIN  m_loan_arrears_aging laa ON         laa.loan_id = ml.id WHERE      ml.loan_status_id=300 AND        mo.id='${officeId}' AND        ( ml.currency_code = '${currencyId}' OR         '-1' = '${currencyId}') GROUP BY   ml.id, laa.principal_overdue_derived, laa.interest_overdue_derived, laa.overdue_since_date_derived) x ) z GROUP BY z.currency, z.arrperiod ) ars ON        ars.arrperiod=periods.period_no AND       ars.currency = periods.currency ORDER BY  periods.currency, periods.pid	Loan amount in arrears by branch	t	t	f
93	Expected Payments By Date - Basic	Table	\N	Loan	SELECT    ounder.name AS "Office", coalesce(ms.display_name,'-') AS "Loan Officer", mc.account_no AS "Client Account Number", mc.display_name AS "Name", mp.name AS "Product", ml.account_no AS "Loan Account Number", mr.duedate AS "Due Date", mr.installment AS "Installment", cu.display_symbol AS "Currency", mr.principal_amount - coalesce(mr.principal_completed_derived,0) AS "Principal Due", mr.interest_amount - coalesce(coalesce(mr.interest_completed_derived,mr.interest_waived_derived),0) AS "Interest Due", coalesce(mr.fee_charges_amount,0) - coalesce(coalesce(mr.fee_charges_completed_derived,mr.fee_charges_waived_derived),0) AS "Fees Due", coalesce(mr.penalty_charges_amount,0) - coalesce(coalesce(mr.penalty_charges_completed_derived,mr.penalty_charges_waived_derived),0) AS "Penalty Due", (mr.principal_amount- coalesce(mr.principal_completed_derived,0)) + (mr.interest_amount- coalesce(coalesce(mr.interest_completed_derived,mr.interest_waived_derived),0)) + (coalesce(mr.fee_charges_amount,0)- coalesce(coalesce(mr.fee_charges_completed_derived,mr.fee_charges_waived_derived),0)) + (coalesce(mr.penalty_charges_amount,0)- coalesce(coalesce(mr.penalty_charges_completed_derived,mr.penalty_charges_waived_derived),0)) AS "Total Due", mlaa.total_overdue_derived AS "Total Overdue" FROM      m_office mo JOIN      m_office ounder ON        ounder.hierarchy LIKE concat(mo.hierarchy, '%') AND       ounder.hierarchy LIKE concat('${currentUserHierarchy}', '%') LEFT JOIN m_client mc ON        mc.office_id=ounder.id LEFT JOIN m_loan ml ON        ml.client_id=mc.id AND       ml.loan_status_id=300 LEFT JOIN m_loan_arrears_aging mlaa ON        mlaa.loan_id=ml.id LEFT JOIN m_loan_repayment_schedule mr ON        mr.loan_id=ml.id AND       mr.completed_derived=false LEFT JOIN m_product_loan mp ON        mp.id=ml.product_id LEFT JOIN m_staff ms ON        ms.id=ml.loan_officer_id LEFT JOIN m_currency cu ON        cu.code=ml.currency_code WHERE     mo.id='${officeId}' AND       ( coalesce(ml.loan_officer_id, -10) = ${loanOfficerId} OR        '-1' = ${loanOfficerId}) AND       mr.duedate BETWEEN '${startDate}' AND '${endDate}' ORDER BY  ounder.id, mr.duedate, ml.account_no	Test	t	t	f
96	GroupSummaryCounts	Table	\N	\N	SELECT    x.* FROM      m_office o,\n          m_group g,\n          (\n                    SELECT    a.activeclients,\n                              (b.activeclientloans + c.activegrouploans) AS activeloans,\n                              b.activeclientloans,\n                              c.activegrouploans,\n                              (b.activeclientborrowers + c.activegroupborrowers) AS activeborrowers,\n                              b.activeclientborrowers,\n                              c.activegroupborrowers,\n                              (b.overdueclientloans + c.overduegrouploans) AS overdueloans,\n                              b.overdueclientloans,\n                              c.overduegrouploans\n                    FROM      (\n                                     SELECT Count(*) AS activeclients\n                                     FROM   m_group topgroup\n                                     JOIN   m_group g\n                                     ON     g.hierarchy LIKE Concat(topgroup.hierarchy, '%')\n                                     JOIN   m_group_client gc\n                                     ON     gc.group_id = g.id\n                                     JOIN   m_client c\n                                     ON     c.id = gc.client_id\n                                     WHERE  topgroup.id = ${groupId}\n                                     AND    c.status_enum = 300) a,\n                              (\n                                     SELECT count(*) AS activeclientloans,\n                                            count(DISTINCT(l.client_id)) AS activeclientborrowers,\n                                            coalesce(sum(\n                                            CASE\n                                                   WHEN laa.loan_id IS NOT NULL THEN 1\n                                                   ELSE 0\n                                            end),\n                                            0) AS overdueclientloans\n                    FROM      m_group topgroup\n                    JOIN      m_group g\n                    ON        g.hierarchy LIKE concat(topgroup.hierarchy, '%')\n                    JOIN      m_loan l\n                    ON        l.group_id = g.id\n                    AND       l.client_id IS NOT NULL\n                    LEFT JOIN m_loan_arrears_aging laa\n                    ON        laa.loan_id = l.id\n                    WHERE     topgroup.id = ${groupId}\n                    AND       l.loan_status_id = 300) b,\n          (\n                 SELECT count(*)  AS activegrouploans,\n                        count(DISTINCT(l.group_id)) AS activegroupborrowers,\n                        coalesce(sum(\n                        CASE\n                               WHEN laa.loan_id IS NOT NULL THEN 1\n                               ELSE 0\n                        end),\n                        0) AS overduegrouploans\nFROM      m_group topgroup JOIN      m_group g ON        g.hierarchy LIKE concat(topgroup.hierarchy, '%') JOIN      m_loan l ON        l.group_id = g.id AND       l.client_id IS NULL LEFT JOIN m_loan_arrears_aging laa ON        laa.loan_id = l.id WHERE     topgroup.id = ${groupId} AND       l.loan_status_id = 300) c ) x WHERE g.id = ${groupId} AND o.id = g.office_id AND o.hierarchy LIKE concat('${currentUserHierarchy}', '%')\n	Utility query for getting group summary count details for a group_id	t	f	f
97	GroupSummaryAmounts	Table	\N	\N	\nSELECT    Coalesce(cur.display_symbol, l.currency_code)  AS currency,\n          Coalesce(Sum(l.principal_disbursed_derived),0) AS totaldisbursedamount,\n          Coalesce(Sum(l.principal_outstanding_derived),0) AS totalloanoutstandingamount,\n          Count(laa.loan_id) AS overdueloans,\n          Coalesce(Sum(laa.total_overdue_derived), 0)  AS totalloanoverdueamount\nFROM      m_group topgroup JOIN      m_office o ON        o.id = topgroup.office_id AND       o.hierarchy LIKE Concat('${currentUserHierarchy}', '%') JOIN      m_group g ON        g.hierarchy LIKE Concat(topgroup.hierarchy, '%') JOIN      m_loan l ON        l.group_id = g.id LEFT JOIN m_loan_arrears_aging laa ON        laa.loan_id = l.id LEFT JOIN m_currency cur ON        cur.code = l.currency_code WHERE     topgroup.id = ${groupId} AND       l.disbursedon_date IS NOT NULL GROUP BY  l.currency_code,\n          cur.display_symbol\n	Utility query for getting group summary currency amount details for a group_id	t	f	f
106	TxnRunningBalances	Table	\N	Transaction	select DATE ${startDate} AS "Transaction Date", 'Opening Balance' AS "Transaction Type", null AS "Office", null AS "Loan Officer", null AS "Loan Account No", null AS "Loan Product", null AS "Currency", null AS "Client Account No", null AS "Client", null AS "Principal", null AS "Interest", COALESCE(round(sum(CASE WHEN txn.transaction_type_enum  = 1 /* disbursement */ THEN COALESCE(txn.interest_portion_derived, 0.00) ELSE 0 END), 2), 0.00) AS "Outstanding Principal", COALESCE(round(sum(CASE WHEN txn.transaction_type_enum in (2, 5, 8) /* repayment, repayment at disbursal, recovery repayment */ THEN COALESCE(txn.interest_portion_derived, 0.00) ELSE 0 END), 2), 0.00) AS "Interest Income", COALESCE(round(sum(CASE WHEN txn.transaction_type_enum = 6 THEN COALESCE(txn.principal_portion_derived, 0.00) ELSE 0 END), 2), 0.00) AS "Principal Write Off" from m_office o join m_office ounder on ounder.hierarchy like concat(o.hierarchy, '%') and ounder.hierarchy like concat('${currentUserHierarchy}', '%') join m_client c on c.office_id = ounder.id join m_loan l on l.client_id = c.id join m_product_loan lp on lp.id = l.product_id join m_loan_transaction txn on txn.loan_id = l.id left join m_currency cur on cur.code = l.currency_code where txn.is_reversed = false and txn.transaction_type_enum not in (10, 11) and o.id = '${officeId}' and txn.transaction_date < DATE ${startDate} union all select txn.transaction_date AS "Transaction Date", cast(COALESCE(re.enum_message_property, concat('Unknown Transaction Type Value:', ' ', txn.transaction_type_enum)) as char) AS "Transaction Type", ounder.name AS "Office", lo.display_name AS "Loan Officer", l.account_no AS "Loan Account No", lp.name AS "Loan Product", COALESCE(cur.display_symbol, l.currency_code) AS "Currency", c.account_no AS "Client Account No", c.display_name AS "Client", COALESCE(txn.principal_portion_derived, 0.00) AS "Principal", COALESCE(txn.interest_portion_derived, 0.00) AS "Interest", COALESCE(round(sum(CASE WHEN txn.transaction_type_enum = 1 /* disbursement */ THEN COALESCE(txn.amount, 0.00) ELSE -1 * COALESCE(txn.principal_portion_derived, 0.00) END), 2), 0.00) AS "Outstanding Principal", COALESCE(round(sum(CASE WHEN txn.transaction_type_enum in (2, 5, 8) /* repayment, repayment at disbursal, recovery repayment */ THEN COALESCE(txn.interest_portion_derived, 0.00) ELSE 0 END), 2), 0.00) AS "Interest Income", COALESCE(round(sum(CASE WHEN txn.transaction_type_enum = 6 THEN COALESCE(txn.principal_portion_derived, 0.00) ELSE 0 END), 2), 0.00) AS "Principal Write Off" from m_office o join m_office ounder on ounder.hierarchy like concat(o.hierarchy, '%') and ounder.hierarchy like concat('${currentUserHierarchy}', '%') join m_client c on c.office_id = ounder.id join m_loan l on l.client_id = c.id left join m_staff lo on lo.id = l.loan_officer_id join m_product_loan lp on lp.id = l.product_id join m_loan_transaction txn on txn.loan_id = l.id left join m_currency cur on cur.code = l.currency_code left join r_enum_value re on re.enum_name = 'transaction_type_enum' AND re.enum_id = txn.transaction_type_enum where txn.is_reversed = false and txn.transaction_type_enum not in (10, 11) and (COALESCE(l.loan_officer_id, -10) = 9 or '-1' = 9) and o.id = '${officeId}' and txn.transaction_date >= DATE ${startDate} and txn.transaction_date <= DATE ${endDate} group by txn.id, ounder.id, lo.id, l.id, lp.id, cur.id, c.id, re.enum_message_property	Running Balance Txn report for Individual Lending.\nSuitable for small MFI's.  Larger could use it using the branch or other parameters.\nBasically, suck it and see if its quick enough for you out-of-te box or whether it needs performance work in your situation.\n	f	f	f
107	FieldAgentStats	Table	\N	Quipo	select COALESCE(cur.display_symbol, l.currency_code) as Currency, /*This query will return more than one entry if more than one currency is used */ count(distinct(c.id)) as activeClients, count(*) as activeLoans, sum(l.principal_disbursed_derived) as disbursedAmount, sum(l.principal_outstanding_derived) as loanOutstandingAmount, round((sum(l.principal_outstanding_derived) * 100) / sum(l.principal_disbursed_derived),2) as loanOutstandingPC, sum(COALESCE(lpa.principal_in_advance_derived,0.0)) as LoanPaidInAdvance, sum( CASE WHEN (CURRENT_DATE - 28 * INTERVAL '1 day') > COALESCE(laa.overdue_since_date_derived, CURRENT_DATE) THEN l.principal_outstanding_derived ELSE 0 END) as portfolioAtRisk, round((sum( CASE WHEN (CURRENT_DATE - 28 * INTERVAL '1 day') > COALESCE(laa.overdue_since_date_derived, CURRENT_DATE) THEN l.principal_outstanding_derived ELSE 0 END) * 100) / sum(l.principal_outstanding_derived), 2) as portfolioAtRiskPC, count(distinct( CASE WHEN (CURRENT_DATE - 28 * INTERVAL '1 day') > COALESCE(laa.overdue_since_date_derived, CURRENT_DATE) THEN c.id ELSE null END)) as clientsInDefault, round((count(distinct( CASE WHEN (CURRENT_DATE - 28 * INTERVAL '1 day') > COALESCE(laa.overdue_since_date_derived, CURRENT_DATE) THEN c.id ELSE null END))) * 100 / count(distinct(c.id)),2) as clientsInDefaultPC, sum(l.principal_disbursed_derived) / count(*) as averageLoanAmount from m_staff fa join m_office o on o.id = fa.office_id AND o.hierarchy like concat('${currentUserHierarchy}', '%') join m_group pgm on pgm.staff_id = fa.id join m_loan l on l.group_id = pgm.id and l.client_id is not null left join m_currency cur on cur.code = l.currency_code left join m_loan_arrears_aging laa on laa.loan_id = l.id left join m_loan_paid_in_advance lpa on lpa.loan_id = l.id join m_client c on c.id = l.client_id where fa.id = ${staffId} and l.loan_status_id = 300 group by l.currency_code, cur.id	Field Agent Statistics	f	f	f
108	FieldAgentPrograms	Table	\N	Quipo	select pgm.id, pgm.display_name as name, sts.enum_message_property as status  from m_group pgm  join m_office o on o.id = pgm.office_id AND o.hierarchy like concat('${currentUserHierarchy}', '%') left join r_enum_value sts on sts.enum_name = 'status_enum' and sts.enum_id = pgm.status_enum  where pgm.staff_id = ${staffId}	List of Field Agent Programs	f	f	f
109	ProgramDetails	Table	\N	Quipo	select l.id as loanId, l.account_no as loanAccountNo, c.id as clientId, c.account_no as clientAccountNo, pgm.display_name as programName,  (select count(*) from m_loan cy where cy.group_id = pgm.id and cy.client_id =c.id and cy.disbursedon_date <= l.disbursedon_date) as loanCycleNo,  c.display_name as clientDisplayName, COALESCE(cur.display_symbol, l.currency_code) as Currency, COALESCE(l.principal_repaid_derived,0.0) as loanRepaidAmount, COALESCE(l.principal_outstanding_derived, 0.0) as loanOutstandingAmount, COALESCE(lpa.principal_in_advance_derived,0.0) as LoanPaidInAdvance,  COALESCE(laa.principal_overdue_derived, 0.0) as loanInArrearsAmount, CASE WHEN COALESCE(laa.principal_overdue_derived, 0.00) > 0 THEN 'Yes' ELSE 'No' END as inDefault,  CASE WHEN (CURRENT_DATE - 28 * INTERVAL '1 day') > COALESCE(laa.overdue_since_date_derived, CURRENT_DATE) THEN l.principal_outstanding_derived ELSE 0 END as portfolioAtRisk   from m_group pgm  join m_office o on o.id = pgm.office_id AND o.hierarchy like concat('${currentUserHierarchy}', '%')  join m_loan l on l.group_id = pgm.id and l.client_id is not null  left join m_currency cur on cur.code = l.currency_code  join m_client c on c.id = l.client_id left join m_loan_arrears_aging laa on laa.loan_id = l.id  left join m_loan_paid_in_advance lpa on lpa.loan_id = l.id  where pgm.id = ${programId}  and l.loan_status_id = 300 order by c.display_name, l.account_no	List of Loans in a Program	f	f	f
110	ChildrenStaffList	Table	\N	Quipo	select s.id, s.display_name, s.firstname, s.lastname, s.organisational_role_enum,\ns.organisational_role_parent_staff_id,\nsp.display_name AS "organisational_role_parent_staff_display_name"\nfrom m_staff s\njoin m_staff sp on s.organisational_role_parent_staff_id = sp.id\nwhere s.organisational_role_parent_staff_id = ${staffId}	Get Next Level Down Staff	f	f	f
111	CoordinatorStats	Table	\N	Quipo	select COALESCE(cur.display_symbol, l.currency_code) as Currency, /*This query will return more than one entry if more than one currency is used */ count(distinct(c.id)) as activeClients, count(*) as activeLoans, sum(l.principal_disbursed_derived) as disbursedAmount, sum(l.principal_outstanding_derived) as loanOutstandingAmount, round((sum(l.principal_outstanding_derived) * 100) / sum(l.principal_disbursed_derived),2) as loanOutstandingPC, sum(COALESCE(lpa.principal_in_advance_derived,0.0)) as LoanPaidInAdvance, sum( CASE WHEN (CURRENT_DATE - 28 * INTERVAL '1 day') > COALESCE(laa.overdue_since_date_derived, CURRENT_DATE) THEN l.principal_outstanding_derived ELSE 0 END) as portfolioAtRisk, round((sum( CASE WHEN (CURRENT_DATE - 28 * INTERVAL '1 day') > COALESCE(laa.overdue_since_date_derived, CURRENT_DATE) THEN l.principal_outstanding_derived ELSE 0 END) * 100) / sum(l.principal_outstanding_derived), 2) as portfolioAtRiskPC, count(distinct( CASE WHEN (CURRENT_DATE - 28 * INTERVAL '1 day') > COALESCE(laa.overdue_since_date_derived, CURRENT_DATE) THEN c.id ELSE null END)) as clientsInDefault, round((count(distinct( CASE WHEN (CURRENT_DATE - 28 * INTERVAL '1 day') > COALESCE(laa.overdue_since_date_derived, CURRENT_DATE) THEN c.id ELSE null END))) * 100 / count(distinct(c.id)),2) as clientsInDefaultPC, sum(l.principal_disbursed_derived) / count(*) as averageLoanAmount from m_staff coord join m_staff fa on fa.organisational_role_parent_staff_id = coord.id join m_office o on o.id = fa.office_id AND o.hierarchy like concat('${currentUserHierarchy}', '%') join m_group pgm on pgm.staff_id = fa.id join m_loan l on l.group_id = pgm.id and l.client_id is not null left join m_currency cur on cur.code = l.currency_code left join m_loan_arrears_aging laa on laa.loan_id = l.id left join m_loan_paid_in_advance lpa on lpa.loan_id = l.id join m_client c on c.id = l.client_id where coord.id = ${staffId} and l.loan_status_id = 300 group by l.currency_code, cur.id	Coordinator Statistics	f	f	f
112	BranchManagerStats	Table	\N	Quipo	SELECT    Coalesce(cur.display_symbol, l.currency_code)  AS currency, Count(DISTINCT(c.id))  AS activeclients, Count(*) AS activeloans, Sum(l.principal_disbursed_derived) AS disbursedamount, Sum(l.principal_outstanding_derived) AS loanoutstandingamount, Round((Sum(l.principal_outstanding_derived) * 100) / Sum(l.principal_disbursed_derived),2) AS loanoutstandingpc, Sum(Coalesce(lpa.principal_in_advance_derived,0.0))  AS loanpaidinadvance, sum( CASE WHEN ( CURRENT_DATE - 28 * INTERVAL '1 day') > coalesce(laa.overdue_since_date_derived, CURRENT_DATE) THEN l.principal_outstanding_derived ELSE 0 end) AS portfolioatrisk, round((sum( CASE WHEN ( CURRENT_DATE - 28 * INTERVAL '1 day' ) > coalesce(laa.overdue_since_date_derived, CURRENT_DATE) THEN l.principal_outstanding_derived ELSE 0 end) * 100) / sum(l.principal_outstanding_derived), 2) AS portfolioatriskpc, count(DISTINCT( CASE WHEN ( CURRENT_DATE - 28 * INTERVAL '1 day' ) > coalesce(laa.overdue_since_date_derived, CURRENT_DATE) THEN c.id ELSE NULL end)) AS clientsindefault, round((count(DISTINCT( CASE WHEN ( CURRENT_DATE - 28 * INTERVAL '1 day' ) > coalesce(laa.overdue_since_date_derived, CURRENT_DATE) THEN c.id ELSE NULL end)) * 100) / count(DISTINCT(c.id)), 2)  AS clientsindefaultpc, (sum(l.principal_disbursed_derived) / count(*)) AS averageloanamount FROM      m_staff bm JOIN      m_staff coord ON        coord.organisational_role_parent_staff_id = bm.id JOIN      m_staff fa ON        fa.organisational_role_parent_staff_id = coord.id JOIN      m_office o ON        o.id = fa.office_id AND       o.hierarchy LIKE concat('${currentUserHierarchy}', '%') JOIN      m_group pgm ON        pgm.staff_id = fa.id JOIN      m_loan l ON        l.group_id = pgm.id AND       l.client_id IS NOT NULL LEFT JOIN m_currency cur ON        cur.code = l.currency_code LEFT JOIN m_loan_arrears_aging laa ON        laa.loan_id = l.id LEFT JOIN m_loan_paid_in_advance lpa ON        lpa.loan_id = l.id JOIN      m_client c ON        c.id = l.client_id WHERE     bm.id = ${staffId} AND       l.loan_status_id = 300 GROUP BY  l.currency_code, cur.display_symbol	Branch Manager Statistics	f	f	f
114	ProgramStats	Table	\N	Quipo	select COALESCE(cur.display_symbol, l.currency_code) as Currency, /*This query will return more than one entry if more than one currency is used */ count(distinct(c.id)) as activeClients, count(*) as activeLoans, sum(l.principal_disbursed_derived) as disbursedAmount, sum(l.principal_outstanding_derived) as loanOutstandingAmount, round((sum(l.principal_outstanding_derived) * 100) / sum(l.principal_disbursed_derived),2) as loanOutstandingPC, sum(COALESCE(lpa.principal_in_advance_derived,0.0)) as LoanPaidInAdvance, sum( CASE WHEN (CURRENT_DATE - 28 * INTERVAL '1 day') > COALESCE(laa.overdue_since_date_derived, CURRENT_DATE) THEN l.principal_outstanding_derived ELSE 0 END) as portfolioAtRisk, round((sum( CASE WHEN (CURRENT_DATE - 28 * INTERVAL '1 day') > COALESCE(laa.overdue_since_date_derived, CURRENT_DATE) THEN l.principal_outstanding_derived ELSE 0 END) * 100) / sum(l.principal_outstanding_derived), 2) as portfolioAtRiskPC, count(distinct( CASE WHEN (CURRENT_DATE - 28 * INTERVAL '1 day') > COALESCE(laa.overdue_since_date_derived, CURRENT_DATE) THEN c.id ELSE null END)) as clientsInDefault, round((count(distinct( CASE WHEN (CURRENT_DATE - 28 * INTERVAL '1 day') > COALESCE(laa.overdue_since_date_derived, CURRENT_DATE) THEN c.id ELSE null END))) * 100 / count(distinct(c.id)),2) as clientsInDefaultPC, sum(l.principal_disbursed_derived) / count(*) as averageLoanAmount from m_group pgm join m_office o on o.id = pgm.office_id AND o.hierarchy like concat('${currentUserHierarchy}', '%') join m_loan l on l.group_id = pgm.id and l.client_id is not null left join m_currency cur on cur.code = l.currency_code left join m_loan_arrears_aging laa on laa.loan_id = l.id left join m_loan_paid_in_advance lpa on lpa.loan_id = l.id join m_client c on c.id = l.client_id where pgm.id = ${programId} and l.loan_status_id = 300 group  by l.currency_code, cur.id	Program Statistics	f	f	f
115	ClientSummary 	Table	\N	\N	SELECT x.* FROM m_client c, m_office o, (SELECT a.loanCycle, a.activeLoans, b.lastLoanAmount, d.activeSavings, d.totalSavings FROM (SELECT COALESCE(MAX(l.loan_counter),0) AS loanCycle, COUNT(l.id) AS activeLoans FROM m_loan l WHERE l.loan_status_id=300 AND l.client_id=${clientId}) a, (SELECT count(l.id), COALESCE(l.principal_amount,0) AS lastLoanAmount FROM m_loan l WHERE l.client_id=${clientId} AND l.disbursedon_date = (SELECT COALESCE(MAX(disbursedon_date),NOW()) FROM m_loan where client_id=${clientId} and loan_status_id=300) group by l.principal_amount) b, (SELECT COUNT(s.id) AS activeSavings, COALESCE(SUM(s.account_balance_derived),0) AS totalSavings FROM m_savings_account s WHERE s.status_enum=300 AND s.client_id=${clientId}) d) x WHERE c.id=${clientId} AND o.id = c.office_id AND o.hierarchy LIKE CONCAT('${currentUserHierarchy}', '%')	Utility query for getting the client summary details	t	f	f
116	LoanCyclePerProduct	Table	\N	\N	SELECT lp.name AS "productName", MAX(l.loan_product_counter) AS "loanProductCycle" FROM m_loan l JOIN m_product_loan lp ON l.product_id=lp.id WHERE lp.include_in_borrower_cycle=true AND l.loan_product_counter IS NOT NULL AND l.client_id=${clientId} GROUP BY lp.id	Utility query for getting the client loan cycle details	t	f	f
117	GroupSavingSummary	Table	\N	\N	select COALESCE(cur.display_symbol, sa.currency_code) as currency, count(sa.id) as totalSavingAccounts, COALESCE(sum(sa.account_balance_derived),0) as totalSavings from m_group topgroup join m_office o on o.id = topgroup.office_id and o.hierarchy like concat('${currentUserHierarchy}', '%') join m_group g on g.hierarchy like concat(topgroup.hierarchy, '%') join m_savings_account sa on sa.group_id = g.id left join m_currency cur on cur.code = sa.currency_code where topgroup.id = ${groupId} and sa.activatedon_date is not null group by sa.currency_code, cur.id	Utility query for getting group or center saving summary details for a group_id	t	f	f
148	GroupNamesByStaff	Table			SELECT gr.id AS id, gr.display_name AS name FROM   m_group gr WHERE  gr.level_id=1 AND    gr.staff_id = ${staffId}		t	f	f
149	ClientTrendsByDay	Table		Client	SELECT COUNT(cl.id) AS count, cl.activation_date AS days             FROM m_office o LEFT JOIN m_client cl on o.id = cl.office_id             WHERE o.hierarchy like concat((select ino.hierarchy from m_office ino where ino.id = ${officeId}),'%' )                 AND (cl.activation_date BETWEEN (current_date - INTERVAL '12 DAY') AND DATE(NOW()- INTERVAL '1 DAY'))             GROUP BY days             	Retrieves the number of clients joined in last 12 days	t	f	f
151	ClientTrendsByMonth	Table		Client	SELECT COUNT(cl.id) AS count, TRIM(TO_CHAR(cl.activation_date, 'Month')) AS Months             FROM m_office o LEFT JOIN m_client cl ON o.id = cl.office_id             WHERE o.hierarchy LIKE CONCAT((SELECT ino.hierarchy FROM m_office ino WHERE ino.id = ${officeId}), '%')                 AND (cl.activation_date BETWEEN (CURRENT_DATE - INTERVAL '12 months') AND CURRENT_DATE)             GROUP BY TRIM(TO_CHAR(cl.activation_date, 'Month')), EXTRACT(MONTH FROM cl.activation_date)             ORDER BY EXTRACT(MONTH FROM cl.activation_date) ASC		t	f	f
154	LoanTrendsByMonth	Table		Loan	SELECT COUNT(ln.id) AS lcount, TRIM(TO_CHAR(ln.disbursedon_date, 'Month')) AS Months             FROM m_office o                 LEFT JOIN m_client cl on o.id = cl.office_id                 LEFT JOIN m_loan ln on cl.id = ln.client_id             WHERE o.hierarchy like concat((select ino.hierarchy from m_office ino where ino.id = ${officeId}),'%' )                 AND (ln.disbursedon_date BETWEEN (CURRENT_DATE - INTERVAL '12 months') AND CURRENT_DATE)             GROUP BY TRIM(TO_CHAR(ln.disbursedon_date, 'Month')), EXTRACT(MONTH FROM ln.disbursedon_date)             ORDER BY EXTRACT(MONTH FROM ln.disbursedon_date) ASC		t	f	f
152	LoanTrendsByDay	Table		Loan	SELECT COUNT(ln.id) AS lcount, ln.disbursedon_date AS days             FROM m_office o LEFT JOIN m_client cl on o.id = cl.office_id                 LEFT JOIN m_loan ln on cl.id = ln.client_id             WHERE o.hierarchy like concat((select ino.hierarchy from m_office ino where ino.id = ${officeId}),'%' )                 AND (ln.disbursedon_date BETWEEN (current_date - INTERVAL '12 DAY') AND DATE(NOW()- INTERVAL '1 DAY'))             GROUP BY days             	Retrieves Number of loans disbursed for last 12 days	t	f	f
150	ClientTrendsByWeek	Table		Client	SELECT COUNT(cl.id) AS count, EXTRACT(WEEK FROM cl.activation_date) AS Weeks             FROM m_office o LEFT JOIN m_client cl on o.id = cl.office_id             WHERE o.hierarchy like concat((select ino.hierarchy from m_office ino where ino.id = ${officeId}),'%' )                 AND (cl.activation_date BETWEEN (CURRENT_DATE - INTERVAL '12 weeks') AND CURRENT_DATE)             GROUP BY EXTRACT(WEEK FROM cl.activation_date)		t	f	f
165	Savings Accounts Dormancy Report	Table	\N	Savings	select cl.display_name AS "Client Display Name", sa.account_no AS "Account Number", cl.mobile_no AS "Mobile Number", (select COALESCE(max(sat.transaction_date), sa.activatedon_date) from m_savings_account_transaction as sat where sat.is_reversed = false and sat.transaction_type_enum in (1, 2) and sat.savings_account_id = sa.id) AS "Date of Last Activity", EXTRACT(DAY FROM (CURRENT_DATE - (select COALESCE(max(sat.transaction_date), sa.activatedon_date) from m_savings_account_transaction as sat where sat.is_reversed = false and sat.transaction_type_enum in (1, 2) and sat.savings_account_id = sa.id)::TIMESTAMP)) AS "Days Since Last Activity" from m_savings_account as sa inner join m_savings_product as sp on (sa.product_id = sp.id and sp.is_dormancy_tracking_active = true) left join m_client as cl on sa.client_id = cl.id where sa.sub_status_enum = ${subStatus} and cl.office_id = '${officeId}'	\N	t	t	f
166	Active Clients	SMS	NonTriggered	Client	SELECT c.id AS id, c.firstname AS firstname, c.middlename AS middlename, c.lastname  AS lastname, c.display_name  AS fullname, c.mobile_no AS mobileno, Concat(REPEAT('..', ((Length(ounder.hierarchy) - Length( REPLACE(ounder.hierarchy, '.', '')) - 1))), ounder.name) AS officename, o.id AS officenumber FROM  m_office o JOIN  m_office ounder ON ounder.hierarchy LIKE Concat(o.hierarchy, '%') JOIN  m_client c ON c.office_id = ounder.id LEFT JOIN r_enum_value r ON r.enum_name = 'status_enum' AND r.enum_id = c.status_enum WHERE o.id = '${officeId}' AND c.status_enum = 300 AND (coalesce(c.staff_id, -10) = ${loanOfficerId} OR'-1' = ${loanOfficerId}) GROUP BY c.id, ounder.hierarchy, ounder.name, o.id ORDER BY ounder.hierarchy, c.account_no	All clients with the status ‘Active’	f	t	f
167	Prospective Clients	SMS	NonTriggered	Client	SELECT c.id AS id, c.firstname AS firstName, c.middlename AS middleName, c.lastname AS lastName, c.display_name AS fullName, c.mobile_no AS mobileNo, CONCAT(REPEAT('..', ((LENGTH(ounder.hierarchy) - LENGTH(REPLACE(ounder.hierarchy, '.', '')) - 1))), ounder.name) AS officeName, o.id AS officeNumber FROM m_office o JOIN m_office ounder ON ounder.hierarchy LIKE CONCAT(o.hierarchy, '%') JOIN m_client c ON c.office_id = ounder.id LEFT JOIN r_enum_value r ON r.enum_name = 'status_enum' AND r.enum_id = c.status_enum LEFT JOIN m_loan l ON l.client_id = c.id WHERE o.id = '${officeId}' AND c.status_enum = 300 AND (COALESCE(c.staff_id, -10) = ${loanOfficerId} OR '-1' = ${loanOfficerId}) AND l.client_id IS NULL GROUP BY c.id, ounder.id, o.id ORDER BY ounder.hierarchy, c.account_no	All clients with the status ‘Active’ who have never had a loan before	f	t	f
168	Active Loan Clients	SMS	NonTriggered	Client	SELECT c.id AS id, c.firstname AS firstName, c.middlename AS middleName, c.lastname AS lastName, c.display_name AS fullName, c.mobile_no AS mobileNo, l.principal_amount AS loanAmount, (COALESCE(l.principal_outstanding_derived, 0) + COALESCE(l.interest_outstanding_derived, 0) + COALESCE(l.fee_charges_outstanding_derived, 0) + COALESCE(l.penalty_charges_outstanding_derived, 0)) AS loanOutstanding, l.principal_disbursed_derived AS loanDisbursed, ounder.id AS officeNumber, l.account_no AS loanAccountId, gua.lastname AS guarantorLastName, COUNT(gua.id) AS numberOfGuarantors, g.display_name AS groupName FROM m_office o JOIN m_office ounder ON ounder.hierarchy LIKE CONCAT(o.hierarchy, '%') JOIN m_client c ON c.office_id = ounder.id JOIN m_loan l ON l.client_id = c.id JOIN m_product_loan pl ON pl.id = l.product_id LEFT JOIN m_group_client gc ON gc.client_id = c.id LEFT JOIN m_group g ON g.id = gc.group_id LEFT JOIN m_staff lo ON lo.id = l.loan_officer_id LEFT JOIN m_currency cur ON cur.code = l.currency_code LEFT JOIN m_guarantor gua ON gua.loan_id = l.id WHERE o.id = '${officeId}' AND (COALESCE(l.loan_officer_id, -10) = ${loanOfficerId} OR '-1' = ${loanOfficerId}) AND l.loan_status_id = 300 AND (EXTRACT(DAY FROM (CURRENT_DATE - l.disbursedon_date::TIMESTAMP)) BETWEEN ${cycleX} AND ${cycleY}) GROUP BY l.id, c.id, ounder.id, gua.id, g.id ORDER BY ounder.hierarchy, l.currency_code, c.account_no, l.account_no	All clients with an outstanding loan between cycleX and cycleY days	f	t	f
169	Loan in arrears	SMS	NonTriggered	Loan	SELECT mc.id AS id, mc.firstname AS firstName, mc.middlename AS middleName, mc.lastname AS lastName, mc.display_name AS fullName, mc.mobile_no AS mobileNo, ml.principal_amount AS loanAmount, (COALESCE(ml.principal_outstanding_derived, 0) + COALESCE(ml.interest_outstanding_derived, 0) + COALESCE(ml.fee_charges_outstanding_derived, 0) + COALESCE(ml.penalty_charges_outstanding_derived, 0)) AS loanOutstanding, ml.principal_disbursed_derived AS loanDisbursed, laa.overdue_since_date_derived AS paymentDueDate, COALESCE(laa.total_overdue_derived, 0) AS totalDue, ounder.id AS officeNumber, ml.account_no AS loanAccountId, gua.lastname AS guarantorLastName, COUNT(gua.id) AS numberOfGuarantors, g.display_name AS groupName FROM m_office mo JOIN m_office ounder ON ounder.hierarchy LIKE CONCAT(mo.hierarchy, '%') INNER JOIN m_client mc ON mc.office_id=ounder.id INNER JOIN m_loan ml ON ml.client_id = mc.id INNER JOIN r_enum_value rev ON rev.enum_id=ml.loan_status_id AND rev.enum_name = 'loan_status_id' INNER JOIN m_loan_arrears_aging laa ON laa.loan_id=ml.id LEFT JOIN m_currency cur ON cur.code = ml.currency_code LEFT JOIN m_group_client gc ON gc.client_id = mc.id LEFT JOIN m_group g ON g.id = gc.group_id LEFT JOIN m_staff lo ON lo.id = ml.loan_officer_id LEFT JOIN m_guarantor gua ON gua.loan_id = ml.id WHERE ml.loan_status_id=300 AND mo.id='${officeId}' AND (COALESCE(ml.loan_officer_id, -10) = ${loanOfficerId} OR '-1' = ${loanOfficerId}) AND (EXTRACT(DAY FROM (CURRENT_DATE - laa.overdue_since_date_derived::TIMESTAMP)) BETWEEN ${fromX} AND ${toY}) GROUP BY ml.id, mc.id, laa.loan_id, ounder.id, gua.id, g.id ORDER BY ounder.hierarchy, ml.currency_code, mc.account_no, ml.account_no	All clients with an outstanding loan in arrears between fromX and toY days	f	t	f
170	Loan payments due	SMS	NonTriggered	Loan	SELECT cl.id AS id, cl.firstname  AS firstName, cl.middlename  AS middleName, cl.lastname AS lastName, cl.display_name AS fullName, cl.mobile_no AS mobileNo, l.principal_amount AS loanAmount, of.id AS officeNumber, (COALESCE(l.principal_outstanding_derived, 0) + COALESCE(l.interest_outstanding_derived, 0) + COALESCE(l.fee_charges_outstanding_derived, 0) + COALESCE(l.penalty_charges_outstanding_derived, 0)) AS loanOutstanding, l.principal_disbursed_derived AS loanDisbursed, ls.duedate AS paymentDueDate, (COALESCE(SUM(ls.principal_amount), 0) - COALESCE(SUM(ls.principal_writtenoff_derived), 0) + COALESCE(SUM(ls.interest_amount), 0) - COALESCE(SUM(ls.interest_writtenoff_derived), 0) - COALESCE(SUM(ls.interest_waived_derived), 0) + COALESCE(SUM(ls.fee_charges_amount), 0) - COALESCE(SUM(ls.fee_charges_writtenoff_derived), 0) - COALESCE(SUM(ls.fee_charges_waived_derived), 0) + COALESCE(SUM(ls.penalty_charges_amount), 0) - COALESCE(SUM(ls.penalty_charges_writtenoff_derived), 0) - COALESCE(SUM(ls.penalty_charges_waived_derived), 0)) AS totalDue, laa.total_overdue_derived AS totalOverdue, l.account_no AS loanAccountId, gua.lastname AS guarantorLastName, COUNT(gua.id) AS numberOfGuarantors, gp.display_name AS groupName FROM m_office of LEFT JOIN m_client cl ON of.id = cl.office_id LEFT JOIN m_loan l ON cl.id = l.client_id LEFT JOIN m_group_client gc ON gc.client_id = cl.id LEFT JOIN m_group gp ON gp.id = l.group_id LEFT JOIN m_loan_repayment_schedule ls ON l.id = ls.loan_id LEFT JOIN m_guarantor gua ON gua.loan_id = l.id INNER JOIN m_loan_arrears_aging laa ON laa.loan_id=l.id WHERE of.id = '${officeId}' AND (COALESCE (l.loan_officer_id, -10) = ${loanOfficerId} OR '-1' = ${loanOfficerId}) AND (EXTRACT(DAY FROM (CURRENT_DATE - ls.duedate::TIMESTAMP)) BETWEEN ${fromX} AND ${toY}) AND (of.hierarchy LIKE CONCAT((SELECT ino.hierarchy FROM m_office ino WHERE ino.id = '${officeId}'), '%')) GROUP BY l.id, cl.id, of.id, ls.id, laa.loan_id, gua.id, gp.id ORDER BY of.hierarchy, l.currency_code, cl.account_no, l.account_no	All clients with an unpaid installment due on their loan between fromX and toY days	f	t	f
171	Dormant Prospects	SMS	NonTriggered	Client	SELECT c.id AS id, CONCAT(REPEAT('..', ((LENGTH(ounder.hierarchy) - LENGTH(REPLACE(ounder.hierarchy, '.', '')) - 1))), ounder.name) AS officeName, c.firstname AS firstName, c.middlename AS middleName, c.lastname AS lastName, c.display_name AS fullName, c.mobile_no AS mobileNo, o.id AS officeNumber, DATE_PART('MONTH', AGE(CURRENT_DATE, c.activation_date)) AS dormant FROM m_office o JOIN m_office ounder ON ounder.hierarchy LIKE CONCAT(o.hierarchy, '%') JOIN m_client c ON c.office_id = ounder.id LEFT JOIN r_enum_value r ON r.enum_name = 'status_enum' AND r.enum_id = c.status_enum LEFT JOIN m_loan l ON l.client_id = c.id WHERE o.id = '${officeId}' AND c.status_enum = 300 AND (COALESCE(c.staff_id, -10) = ${loanOfficerId} OR '-1' = ${loanOfficerId}) AND l.client_id IS NULL AND (DATE_PART('MONTH', AGE(CURRENT_DATE, c.activation_date)) > 3) GROUP BY c.id, ounder.id, o.id ORDER BY ounder.hierarchy, c.account_no	All individuals who have not yet received a loan but were also entered into the system more than 3 months	f	t	f
172	Active group leaders	SMS	NonTriggered	Client	SELECT c.id AS id, c.firstname AS firstName, c.middlename AS middleName, c.lastname AS lastName, c.display_name AS fullName, c.mobile_no AS mobileNo, CONCAT(REPEAT('..', ((LENGTH(ounder.hierarchy) - LENGTH(REPLACE(ounder.hierarchy, '.', '')) - 1))), ounder.name) AS officeName, o.id AS officeNumber FROM m_office o JOIN m_office ounder ON ounder.hierarchy LIKE CONCAT(o.hierarchy, '%') JOIN m_group g ON g.office_id = ounder.id JOIN m_client c ON c.office_id = ounder.id LEFT JOIN m_group_client gc ON gc.group_id = g.id AND gc.client_id = c.id LEFT JOIN m_group_roles gr ON gr.group_id = g.id AND gr.client_id = c.id LEFT JOIN m_staff ms ON ms.id = c.staff_id LEFT JOIN r_enum_value r ON r.enum_name = 'status_enum' AND r.enum_id = c.status_enum LEFT JOIN m_code_value cv ON cv.id = gr.role_cv_id LEFT JOIN m_code code ON code.id = cv.code_id WHERE o.id = '${officeId}' AND g.status_enum = 300 AND c.status_enum = 300 AND (COALESCE(c.staff_id, -10) = ${loanOfficerId} OR '-1' = ${loanOfficerId}) AND code.code_name = 'GROUPROLE' AND cv.code_value = 'Leader' GROUP BY c.id, ounder.id, o.id ORDER BY ounder.hierarchy, c.account_no	All active group chairmen	f	t	f
173	Loan payments due (Overdue Loans)	SMS	NonTriggered	Loan	SELECT mc.id AS id, mc.firstname AS firstName, mc.middlename AS middleName, mc.lastname AS lastName, mc.display_name AS fullName, mc.mobile_no AS mobileNo, ml.principal_amount AS loanAmount, (COALESCE(ml.principal_outstanding_derived, 0) + COALESCE(ml.interest_outstanding_derived, 0) + COALESCE(ml.fee_charges_outstanding_derived, 0) + COALESCE(ml.penalty_charges_outstanding_derived, 0)) AS loanOutstanding, ml.principal_disbursed_derived AS loanDisbursed, laa.overdue_since_date_derived AS paymentDueDate, (COALESCE(SUM(ls.principal_amount), 0) - COALESCE(SUM(ls.principal_writtenoff_derived), 0) + COALESCE(SUM(ls.interest_amount), 0) - COALESCE(SUM(ls.interest_writtenoff_derived), 0) - COALESCE(SUM(ls.interest_waived_derived), 0) + COALESCE(SUM(ls.fee_charges_amount), 0) - COALESCE(SUM(ls.fee_charges_writtenoff_derived), 0) - COALESCE(SUM(ls.fee_charges_waived_derived), 0) + COALESCE(SUM(ls.penalty_charges_amount), 0) - COALESCE(SUM(ls.penalty_charges_writtenoff_derived), 0) - COALESCE(SUM(ls.penalty_charges_waived_derived), 0)) AS totalDue, laa.total_overdue_derived AS totalOverdue, ounder.id AS officeNumber, ml.account_no AS loanAccountId, gua.lastname AS guarantorLastName, COUNT(gua.id) AS numberOfGuarantors, g.display_name AS groupName FROM m_office mo JOIN m_office ounder ON ounder.hierarchy LIKE CONCAT(mo.hierarchy, '%') INNER JOIN m_client mc ON mc.office_id = ounder.id INNER JOIN m_loan ml ON ml.client_id = mc.id INNER JOIN r_enum_value rev ON rev.enum_id = ml.loan_status_id AND rev.enum_name = 'loan_status_id' INNER JOIN m_loan_arrears_aging laa ON laa.loan_id = ml.id LEFT JOIN m_loan_repayment_schedule ls ON ls.loan_id = ml.id LEFT JOIN m_currency cur ON cur.code = ml.currency_code LEFT JOIN m_group_client gc ON gc.client_id = mc.id LEFT JOIN m_group g ON g.id = gc.group_id LEFT JOIN m_staff lo ON lo.id = ml.loan_officer_id LEFT JOIN m_guarantor gua ON gua.loan_id = ml.id WHERE ml.loan_status_id = 300 AND mo.id = '${officeId}' AND (COALESCE(ml.loan_officer_id, -10) = ${loanOfficerId} OR '-1' = ${loanOfficerId}) AND (EXTRACT(DAY FROM (CURRENT_DATE - ls.duedate::TIMESTAMP)) BETWEEN ${fromX} AND ${toY}) AND (EXTRACT(DAY FROM (CURRENT_DATE - laa.overdue_since_date_derived::TIMESTAMP)) BETWEEN ${overdueX} AND ${overdueY}) GROUP BY ml.id, mc.id, laa.loan_id, ounder.id, gua.id, g.id ORDER BY ounder.hierarchy, ml.currency_code, mc.account_no, ml.account_no	Loan Payments Due between fromX to toY days for clients in arrears between overdueX and overdueY days	f	t	f
186	Savings Deposit	SMS	Triggered	\N	SELECT sc.savingsId AS savingsId, sc.id AS clientId, sc.firstname, COALESCE(sc.middlename,'') AS middlename, sc.lastname, sc.display_name AS FullName, sc.mobile_no AS mobileNo, ms."account_no" AS savingsAccountNo, ROUND(mst.amountPaid, ms.currency_digits) AS depositAmount, ms.account_balance_derived AS balance, mst.transactionDate AS transactionDate FROM m_office mo JOIN m_office ounder ON ounder.hierarchy LIKE CONCAT(mo.hierarchy, '%') AND ounder.hierarchy LIKE CONCAT('.', '%') LEFT JOIN (SELECT sa.id AS savingsId, mc.id AS id, mc.firstname AS firstname, mc.middlename AS middlename, mc.lastname AS lastname, mc.display_name AS display_name, mc.status_enum AS status_enum, mc.mobile_no AS mobile_no, mc.office_id AS office_id, mc.staff_id AS staff_id FROM m_savings_account sa LEFT JOIN m_client mc ON mc.id = sa.client_id ORDER BY savingsId) sc ON sc.office_id = ounder.id RIGHT JOIN m_savings_account AS ms ON sc.savingsId = ms.id RIGHT JOIN(SELECT st.amount AS amountPaid, st.id, st.savings_account_id, st.id AS savingsTransactionId, st.transaction_date AS transactionDate FROM m_savings_account_transaction st WHERE st.is_reversed = false GROUP BY st.savings_account_id, st.amount, st.id) AS mst ON mst.savings_account_id = ms.id WHERE sc.mobile_no IS NOT NULL AND (mo.id = '${officeId}' OR '${officeId}' = -1) AND (sc.staff_id = ${loanOfficerId} OR ${loanOfficerId} = -1) AND mst.savingsTransactionId = ${savingsTransactionId}	Savings Deposit	f	t	f
176	Happy Birthday	SMS	NonTriggered	Client	SELECT c.id AS id, c.firstname AS firstName, c.middlename AS middleName,  c.lastname AS lastName, c.display_name AS fullName,  c.mobile_no AS mobileNo, CONCAT(REPEAT('..', ((LENGTH(ounder.hierarchy) - LENGTH( REPLACE(ounder.hierarchy, '.', '')) - 1))), ounder.name) AS officeName,   o.id AS officeNumber, c.date_of_birth AS dateOfBirth,  CASE WHEN c.date_of_birth IS NULL THEN 0 ELSE CEIL(EXTRACT(DAY FROM (CURRENT_DATE - c.date_of_birth))/365) END AS age  FROM m_office o  JOIN m_office ounder ON ounder.hierarchy LIKE CONCAT(o.hierarchy, '%')  JOIN m_client c ON c.office_id = ounder.id  LEFT JOIN r_enum_value r ON r.enum_name = 'status_enum' AND r.enum_id = c.status_enum  LEFT JOIN m_staff ms ON ms.id = c.staff_id  WHERE o.id = '${officeId}' AND c.status_enum = 300 AND (COALESCE(c.staff_id, -10) = -1 OR '-1' = -1) AND c.date_of_birth IS NOT NULL AND (DATE_TRUNC('day', c.date_of_birth)=DATE_TRUNC('day', NOW())) AND (DATE_TRUNC('month', c.date_of_birth)=DATE_TRUNC('month', NOW()))  ORDER BY ounder.hierarchy, c.account_no	This sends a message to all clients with the status Active on their Birthday	f	t	f
177	Loan fully repaid	SMS	NonTriggered	Loan	SELECT c.id AS id, c.firstname AS firstName, c.middlename AS middleName, c.lastname AS lastName, c.display_name AS fullName, c.mobile_no AS mobileNo, l.principal_amount AS loanAmount, (COALESCE(l.principal_outstanding_derived, 0) + COALESCE(l.interest_outstanding_derived, 0) + COALESCE(l.fee_charges_outstanding_derived, 0) + COALESCE(l.penalty_charges_outstanding_derived, 0)) AS loanOutstanding, l.principal_disbursed_derived AS loanDisbursed, o.id AS officeNumber, l.account_no AS loanAccountId, gua.lastname AS guarantorLastName, COUNT(gua.id) AS numberOfGuarantors, ls.duedate AS dueDate, laa.total_overdue_derived AS totalDue, gp.display_name AS groupName, l.total_repayment_derived AS "totalFullyPaid" FROM m_office o JOIN m_office ounder ON ounder.hierarchy LIKE CONCAT(o.hierarchy, '%') JOIN m_client c ON c.office_id = ounder.id JOIN m_loan l ON l.client_id = c.id LEFT JOIN m_staff lo ON lo.id = l.loan_officer_id LEFT JOIN m_currency cur ON cur.code = l.currency_code LEFT JOIN m_group_client gc ON gc.client_id = c.id LEFT JOIN m_group gp ON gp.id = l.group_id LEFT JOIN m_loan_repayment_schedule ls ON l.id = ls.loan_id LEFT JOIN m_guarantor gua ON gua.loan_id = l.id LEFT JOIN m_loan_arrears_aging laa ON laa.loan_id = l.id WHERE o.id = '${officeId}' AND (COALESCE(l.loan_officer_id, -10) = ${loanOfficerId} OR '-1' = ${loanOfficerId}) AND (EXTRACT(DAY FROM (CURRENT_DATE - l.closedon_date::TIMESTAMP)) BETWEEN ${fromX} AND ${toY}) AND (l.loan_status_id IN (600, 700)) GROUP BY l.id, c.id, o.id, gua.id, ls.id,laa.loan_id, gp.id, ounder.id ORDER BY ounder.hierarchy, l.currency_code, c.account_no, l.account_no	All loans that have been fully repaid (Closed or Overpaid) in the last fromX to toY days	f	t	f
178	Loan outstanding after final instalment date	SMS	NonTriggered	Loan	SELECT c.id AS id, c.firstname AS firstName, c.middlename AS middleName, c.lastname AS lastName, c.display_name AS fullName, c.mobile_no AS mobileNo, l.principal_amount AS loanAmount, o.id AS officeNumber, (COALESCE(l.principal_outstanding_derived, 0) + COALESCE(l.interest_outstanding_derived, 0) + COALESCE(l.fee_charges_outstanding_derived, 0) + COALESCE(l.penalty_charges_outstanding_derived, 0)) AS loanOutstanding, l.principal_disbursed_derived AS loanDisbursed, ls.duedate AS paymentDueDate, (COALESCE(SUM(ls.principal_amount), 0) - COALESCE(SUM(ls.principal_writtenoff_derived), 0) + COALESCE(SUM(ls.interest_amount), 0) - COALESCE(SUM(ls.interest_writtenoff_derived), 0) - COALESCE(SUM(ls.interest_waived_derived), 0) + COALESCE(SUM(ls.fee_charges_amount), 0) - COALESCE(SUM(ls.fee_charges_writtenoff_derived), 0) - COALESCE(SUM(ls.fee_charges_waived_derived), 0) + COALESCE(SUM(ls.penalty_charges_amount), 0) - COALESCE(SUM(ls.penalty_charges_writtenoff_derived), 0) - COALESCE(SUM(ls.penalty_charges_waived_derived), 0)) AS totalDue, laa.total_overdue_derived AS totalOverdue, l.account_no AS loanAccountId, gua.lastname AS guarantorLastName, COUNT(gua.id) AS numberOfGuarantors, gp.display_name AS groupName FROM m_office o JOIN m_office ounder ON ounder.hierarchy LIKE CONCAT(o.hierarchy, '%') JOIN m_client c ON c.office_id = ounder.id JOIN m_loan l ON l.client_id = c.id LEFT JOIN m_staff lo ON lo.id = l.loan_officer_id LEFT JOIN m_currency cur ON cur.code = l.currency_code LEFT JOIN m_loan_arrears_aging laa ON laa.loan_id = l.id LEFT JOIN m_group_client gc ON gc.client_id = c.id LEFT JOIN m_group gp ON gp.id = l.group_id LEFT JOIN m_loan_repayment_schedule ls ON l.id = ls.loan_id LEFT JOIN m_guarantor gua ON gua.loan_id = l.id WHERE o.id = '${officeId}' AND (COALESCE(l.loan_officer_id, -10) = ${loanOfficerId} OR '-1' = ${loanOfficerId}) AND l.loan_status_id = 300 AND l.expected_maturedon_date < CURRENT_DATE AND (EXTRACT(DAY FROM (CURRENT_DATE - l.expected_maturedon_date::TIMESTAMP)) BETWEEN ${fromX} AND ${toY}) GROUP BY l.id, c.id, o.id, ls.id, laa.loan_id, gua.id, gp.id, ounder.id ORDER BY ounder.hierarchy, l.currency_code, c.account_no, l.account_no	All active loans (with an outstanding balance) between fromX to toY days after the final instalment date on their loan schedule	f	t	f
180	Loan Approved	SMS	Triggered	\N	SELECT mc.id, mc.firstname, mc.middlename as middlename, mc.lastname, mc.display_name as FullName, mc.mobile_no as mobileNo, mc.group_name as GroupName, mo.name as officename, ml.id as loanId, ml.account_no as accountnumber, ml.principal_amount_proposed as loanamount, ml.annual_nominal_interest_rate as annualinterestrate FROM m_office mo JOIN m_office ounder ON ounder.hierarchy LIKE CONCAT(mo.hierarchy, '%') AND ounder.hierarchy like CONCAT('.', '%') LEFT JOIN ( select  ml.id as loanId, COALESCE(mc.id,mc2.id) as id, COALESCE(mc.firstname,mc2.firstname) as firstname, COALESCE(mc.middlename,COALESCE(mc2.middlename,(''))) as middlename,  COALESCE(mc.lastname,mc2.lastname) as lastname,  COALESCE(mc.display_name,mc2.display_name) as display_name,  COALESCE(mc.status_enum,mc2.status_enum) as status_enum, COALESCE(mc.mobile_no,mc2.mobile_no) as mobile_no, COALESCE(mg.office_id,mc2.office_id) as office_id, COALESCE(mg.staff_id,mc2.staff_id) as staff_id, mg.id as group_id, mg.display_name as group_name from m_loan ml left join m_group mg on mg.id = ml.group_id left join m_group_client mgc on mgc.group_id = mg.id left join m_client mc on mc.id = mgc.client_id left join m_client mc2 on mc2.id = ml.client_id order by loanId ) mc on mc.office_id = ounder.id  left join m_loan ml on ml.id = mc.loanId WHERE mc.status_enum = 300 and mc.mobile_no is not null and (mo.id = '${officeId}' or '${officeId}' = -1) and (mc.staff_id = ${loanOfficerId} or ${loanOfficerId} = -1)and (ml.id = ${loanId} or ${loanId} = -1)and (mc.id = ${clientId} or ${clientId} = -1)and (mc.group_id = ${groupId} or ${groupId} = -1)and (ml.loan_type_enum = ${loanType} or ${loanType} = -1)	Loan and client data of approved loan	f	f	f
181	Loan Rejected	SMS	Triggered	\N	SELECT mc.id, mc.firstname, mc.middlename as middlename, mc.lastname, mc.display_name as FullName, mc.mobile_no as mobileNo, mc.group_name as GroupName,  mo.name as officename, ml.id as loanId, ml.account_no as accountnumber, ml.principal_amount_proposed as loanamount, ml.annual_nominal_interest_rate as annualinterestrate  FROM m_office mo  JOIN m_office ounder ON ounder.hierarchy LIKE CONCAT(mo.hierarchy, '%')  AND ounder.hierarchy like CONCAT('.', '%')  LEFT JOIN (  select   ml.id as loanId, COALESCE(mc.id,mc2.id) as id, COALESCE(mc.firstname,mc2.firstname) as firstname, COALESCE(mc.middlename,COALESCE(mc2.middlename,(''))) as middlename,   COALESCE(mc.lastname,mc2.lastname) as lastname,   COALESCE(mc.display_name,mc2.display_name) as display_name,   COALESCE(mc.status_enum,mc2.status_enum) as status_enum,  COALESCE(mc.mobile_no,mc2.mobile_no) as mobile_no,  COALESCE(mg.office_id,mc2.office_id) as office_id,  COALESCE(mg.staff_id,mc2.staff_id) as staff_id, mg.id as group_id,  mg.display_name as group_name  from m_loan ml  left join m_group mg on mg.id = ml.group_id  left join m_group_client mgc on mgc.group_id = mg.id  left join m_client mc on mc.id = mgc.client_id  left join m_client mc2 on mc2.id = ml.client_id  order by loanId  ) mc on mc.office_id = ounder.id  left join m_loan ml on ml.id = mc.loanId  WHERE mc.status_enum = 300 and mc.mobile_no is not null  and (mo.id = '${officeId}' or '${officeId}' = -1)  and (mc.staff_id = ${loanOfficerId} or ${loanOfficerId} = -1) and (ml.id = ${loanId} or ${loanId} = -1) and (mc.id = ${clientId} or ${clientId} = -1) and (mc.group_id = ${groupId} or ${groupId} = -1)  and (ml.loan_type_enum = ${loanType} or ${loanType} = -1)	Loan and client data of rejected loan	f	f	f
182	Client Rejected	SMS	Triggered	Client	SELECT c.id AS id,   c.firstname AS firstName,  c.middlename AS middleName,  c.lastname AS lastName,  c.display_name AS fullName,  c.mobile_no AS mobileNo, CONCAT(REPEAT('..', ((LENGTH(ounder.hierarchy) - LENGTH( REPLACE(ounder.hierarchy, '.', '')) - 1))), ounder.name) AS officeName,   o.id AS officeNumber  FROM m_office o  JOIN m_office ounder ON ounder.hierarchy LIKE CONCAT(o.hierarchy, '%')  JOIN m_client c ON c.office_id = ounder.id  LEFT JOIN r_enum_value r ON r.enum_name = 'status_enum' AND r.enum_id = c.status_enum  WHERE o.id = '${officeId}' AND c.id = ${clientId} AND (COALESCE(c.staff_id, -10) = ${loanOfficerId} OR '-1' = ${loanOfficerId})	Client Rejection	f	t	f
183	Client Activated	SMS	Triggered	Client	SELECT c.id AS id,   c.firstname AS firstName,  c.middlename AS middleName,  c.lastname AS lastName,  c.display_name AS fullName,  c.mobile_no AS mobileNo, CONCAT(REPEAT('..', ((LENGTH(ounder.hierarchy) - LENGTH( REPLACE(ounder.hierarchy, '.', '')) - 1))), ounder.name) AS officeName,   o.id AS officeNumber  FROM m_office o  JOIN m_office ounder ON ounder.hierarchy LIKE CONCAT(o.hierarchy, '%')  JOIN m_client c ON c.office_id = ounder.id  LEFT JOIN r_enum_value r ON r.enum_name = 'status_enum' AND r.enum_id = c.status_enum  WHERE o.id = '${officeId}' AND c.id = ${clientId} AND (COALESCE(c.staff_id, -10) = ${loanOfficerId} OR '-1' = ${loanOfficerId})	Client Activation	f	t	f
184	Savings Rejected	SMS	Triggered	Savings	SELECT   c.id AS id,  c.firstname AS firstName, c.middlename AS middleName,  c.lastname AS lastName, c.display_name AS fullName,  c.mobile_no AS mobileNo, s.account_no AS savingsAccountNo,  ounder.id AS officeNumber,  ounder.name AS officeName    FROM m_office o JOIN m_office ounder ON ounder.hierarchy LIKE CONCAT(o.hierarchy, '%')  JOIN m_client c ON c.office_id = ounder.id  JOIN m_savings_account s ON s.client_id = c.id JOIN m_savings_product sp ON sp.id = s.product_id  LEFT JOIN m_staff st ON st.id = s.field_officer_id  LEFT JOIN m_currency cur ON cur.code = s.currency_code  WHERE o.id = '${officeId}' AND (COALESCE(s.field_officer_id, -10) = ${loanOfficerId} OR '-1' = ${loanOfficerId}) AND s.id = ${savingsId}	Savings Rejected	f	t	f
185	Savings Activated	SMS	Triggered	Savings	SELECT   c.id AS id,  c.firstname AS firstName, c.middlename AS middleName,  c.lastname AS lastName, c.display_name AS fullName,  c.mobile_no AS mobileNo, s.account_no AS savingsAccountNo,  ounder.id AS officeNumber,  ounder.name AS officeName    FROM m_office o JOIN m_office ounder ON ounder.hierarchy LIKE CONCAT(o.hierarchy, '%')  JOIN m_client c ON c.office_id = ounder.id  JOIN m_savings_account s ON s.client_id = c.id JOIN m_savings_product sp ON sp.id = s.product_id  LEFT JOIN m_staff st ON st.id = s.field_officer_id  LEFT JOIN m_currency cur ON cur.code = s.currency_code  WHERE o.id = '${officeId}' AND (COALESCE(s.field_officer_id, -10) = ${loanOfficerId} OR '-1' = ${loanOfficerId}) AND s.id = ${savingsId}	Savings Activation	f	t	f
192	Loan Repayment - Email	Email	Triggered	\N	select  ml.id as loanId,  COALESCE(mc.id,mc2.id) as id,  COALESCE(mc.firstname,mc2.firstname) as firstname,   COALESCE(mc.middlename,mc2.middlename,(\\'\\')) as middlename, COALESCE(mc.lastname,mc2.lastname) as lastname,   COALESCE(mc.display_name,mc2.display_name) as display_name,  COALESCE(mc.status_enum,mc2.status_enum) as status_enum,   COALESCE(mc.mobile_no,mc2.mobile_no) as mobile_no, COALESCE(mg.office_id,mc2.office_id) as office_id, COALESCE(mg.staff_id,mc2.staff_id) as staff_id,  mg.id as group_id, mg.display_name as group_name, COALESCE(mc.email_address,mc2.email_address) as emailAddress, lt.amount as repaymentAmount   from m_loan_transaction lt join m_loan ml on ml.id=lt.loan_id left join m_group mg on mg.id = ml.group_id  left join m_group_client mgc on mgc.group_id = mg.id   left join m_client mc on mc.id = mgc.client_id  left join m_client mc2 on mc2.id = ml.client_id  WHERE (mc.status_enum = 300 or mc2.status_enum = 300) and (mc.email_address is not null or mc2.email_address is not null) and ml.id = ${loanId} and lt.id = ${loanTransactionId} 	Loan and client data of loan repayment	f	t	f
187	Savings Withdrawal	SMS	Triggered	\N	SELECT sc.savingsId AS savingsId, sc.id AS clientId, sc.firstname, COALESCE(sc.middlename,'') AS middlename, sc.lastname, sc.display_name AS FullName, sc.mobile_no AS mobileNo,  ms."account_no" AS savingsAccountNo, ROUND(mst.amountPaid, ms.currency_digits) AS withdrawAmount, ms.account_balance_derived AS balance, mst.transactionDate AS transactionDate FROM m_office mo JOIN m_office ounder ON ounder.hierarchy LIKE CONCAT(mo.hierarchy, '%') AND ounder.hierarchy LIKE CONCAT('.', '%') LEFT JOIN (SELECT sa.id AS savingsId, mc.id AS id, mc.firstname AS firstname, mc.middlename AS middlename, mc.lastname AS lastname, mc.display_name AS display_name, mc.status_enum AS status_enum, mc.mobile_no AS mobile_no, mc.office_id AS office_id, mc.staff_id AS staff_id FROM m_savings_account sa LEFT JOIN m_client mc ON mc.id = sa.client_id ORDER BY savingsId) sc ON sc.office_id = ounder.id RIGHT JOIN m_savings_account AS ms ON sc.savingsId = ms.id RIGHT JOIN(SELECT st.amount AS amountPaid, st.id, st.savings_account_id, st.id AS savingsTransactionId, st.transaction_date AS transactionDate FROM m_savings_account_transaction st WHERE st.is_reversed = false GROUP BY st.savings_account_id, st.amount, st.id) AS mst ON mst.savings_account_id = ms.id WHERE sc.mobile_no IS NOT NULL AND (mo.id = '${officeId}' OR '${officeId}' = -1) AND (sc.staff_id = ${loanOfficerId} OR ${loanOfficerId} = -1) AND mst.savingsTransactionId = ${savingsTransactionId}	Savings Withdrawal	f	t	f
188	ReportCategoryList	Table	\N	(NULL)	(NULL)	(NULL)	t	t	f
189	FullReportList	Table	\N	(NULL)	(NULL)	(NULL)	t	t	f
190	Loan Approved - Email	Email	Triggered	\N	select  ml.id as loanId,  COALESCE(mc.id,mc2.id) as id,  COALESCE(mc.firstname,mc2.firstname) as firstname,   COALESCE(mc.middlename,mc2.middlename,(\\'\\')) as middlename, COALESCE(mc.lastname,mc2.lastname) as lastname,   COALESCE(mc.display_name,mc2.display_name) as display_name, COALESCE(mc.status_enum,mc2.status_enum) as status_enum,  COALESCE(mc.mobile_no,mc2.mobile_no) as mobile_no, COALESCE(mg.office_id,mc2.office_id) as office_id, COALESCE(mg.staff_id,mc2.staff_id) as staff_id,   mg.id as group_id, mg.display_name as group_name, COALESCE(mc.email_address,mc2.email_address) as emailAddress from m_loan ml left join m_group mg on mg.id = ml.group_id   left join m_group_client mgc on mgc.group_id = mg.id  left join m_client mc on mc.id = mgc.client_id   left join m_client mc2 on mc2.id = ml.client_id WHERE (mc.status_enum = 300 or mc2.status_enum = 300) and (mc.email_address is not null or mc2.email_address is not null) and ml.id = ${loanId}	Loan and client data of approved loan	f	t	f
191	Loan Rejected - Email	Email	Triggered	\N	select  ml.id as loanId,  COALESCE(mc.id,mc2.id) as id,  COALESCE(mc.firstname,mc2.firstname) as firstname,   COALESCE(mc.middlename,mc2.middlename,(\\'\\')) as middlename, COALESCE(mc.lastname,mc2.lastname) as lastname,   COALESCE(mc.display_name,mc2.display_name) as display_name, COALESCE(mc.status_enum,mc2.status_enum) as status_enum,  COALESCE(mc.mobile_no,mc2.mobile_no) as mobile_no, COALESCE(mg.office_id,mc2.office_id) as office_id, COALESCE(mg.staff_id,mc2.staff_id) as staff_id,   mg.id as group_id, mg.display_name as group_name, COALESCE(mc.email_address,mc2.email_address) as emailAddress from m_loan ml left join m_group mg on mg.id = ml.group_id   left join m_group_client mgc on mgc.group_id = mg.id  left join m_client mc on mc.id = mgc.client_id   left join m_client mc2 on mc2.id = ml.client_id WHERE (mc.status_enum = 300 or mc2.status_enum = 300) and (mc.email_address is not null or mc2.email_address is not null) and ml.id = ${loanId}	Loan and client data of rejected loan	f	t	f
193	Trial Balance Table	Table	\N	Accounting	select * from ( select debits.glcode as "glcode", debits.name as "name", (case when debits.type = 1 or debits.type = 5 then coalesce(debits.debitamount, 0) - coalesce(credits.creditamount, 0)else null end ) as "debit", (case when debits.type = 4 or debits.type = 3 or debits.type = 2 then coalesce(credits.creditamount, 0) - coalesce(debits.debitamount, 0)else null end ) as "credit" from ( select acc_gl_account.gl_code as "glcode", name, sum(amount) as "debitamount", acc_gl_account.classification_enum as "type" from acc_gl_journal_entry, acc_gl_account where acc_gl_account.id = acc_gl_journal_entry.account_id and acc_gl_journal_entry.type_enum = 2 and acc_gl_journal_entry.entry_date between '${startDate}' and '${endDate}' and ( acc_gl_journal_entry.office_id = ${officeId} or ${officeId} = 1 ) group by glcode,acc_gl_account.name,acc_gl_account.classification_enum order by glcode ) debits left outer join ( select acc_gl_account.gl_code as "glcode", name as "name", sum(amount) as "creditamount", acc_gl_account.classification_enum as "type" from acc_gl_journal_entry, acc_gl_account where acc_gl_account.id = acc_gl_journal_entry.account_id and acc_gl_journal_entry.type_enum = 1 and acc_gl_journal_entry.entry_date between '${startDate}' and '${endDate}' and ( acc_gl_journal_entry.office_id = ${officeId} or ${officeId} = 1 ) group by glcode,acc_gl_account.classification_enum,acc_gl_account.name order by glcode ) credits on debits.glcode = credits.glcode union select credits.glcode as "glcode", credits.name as "name", (case when credits.type = 1 or credits.type = 5 then coalesce(debits.debitamount, 0) - coalesce(credits.creditamount, 0) else null end ) as "debit", (case when credits.type = 4 or credits.type = 3 or credits.type = 2 then coalesce(credits.creditamount, 0) - coalesce(debits.debitamount, 0) else null end ) as "credit" from ( select acc_gl_account.gl_code as "glcode", name, sum(amount) as "debitamount", acc_gl_account.classification_enum as "type" from acc_gl_journal_entry, acc_gl_account where acc_gl_account.id = acc_gl_journal_entry.account_id and acc_gl_journal_entry.type_enum = 2 and acc_gl_journal_entry.entry_date between '${startDate}' and '${endDate}' and ( acc_gl_journal_entry.office_id = '${officeId}' or '${officeId}' = 1 ) group by glcode,acc_gl_account.name,acc_gl_account.classification_enum order by glcode ) debits right outer join ( select acc_gl_account.gl_code as "glcode", name as "name", sum(amount) as "creditamount", acc_gl_account.classification_enum as "type" from acc_gl_journal_entry, acc_gl_account where acc_gl_account.id = acc_gl_journal_entry.account_id and acc_gl_journal_entry.type_enum = 1 and acc_gl_journal_entry.entry_date between '${startDate}' and '${endDate}' and ( acc_gl_journal_entry.office_id = ${officeId} or ${officeId} = 1 ) group by glcode,acc_gl_account.name,acc_gl_account.classification_enum order by glcode ) credits on debits.glcode = credits.glcode ) as fullouterjoinresult order by glcode	Trial Balance Report	t	t	f
194	GeneralLedgerReport Table	Table	\N	Accounting	select details.edate entry_date, sum(details.debit_amount) debit_amount, sum(details.credit_amount) credit_amount, details.description, coalesce(opb.openingbalance, 0) openingbalance, case when details.manual_entry then cast(details.id as text) else cast('system' as text) end transtype, case when actype in (1, 5) then ( sum(details.debit_amount) - sum(details.credit_amount) ) else ( sum(details.credit_amount) - sum(details.debit_amount) ) end as cumulative_sum from ( select a.account_id acid1, concat(gl.gl_code, '-', gl.name) as report_header, gl.classification_enum actype, gl.gl_code as reportid, j1.entry_date edate, concat(gl1.gl_code, '-', gl1.name) as account_name, case when j1.type_enum = 1 then j1.amount else 0 end as debit_amount, case when j1.type_enum = 2 then j1.amount else 0 end as credit_amount, j1.id, j1.office_id, j1.transaction_id, j1.type_enum, j1.office_running_balance as aftertxn, j1.description as description, j1.transaction_id as transactionid, a.manual_entry from acc_gl_journal_entry j1 inner join ( select distinct je.transaction_id tid, je.account_id, je.manual_entry from m_office o left join m_office ounder on ounder.hierarchy like concat(o.hierarchy, '%') inner join acc_gl_journal_entry je on je.office_id = ounder.id where je.account_id = cast('${GLAccountNO}' as BIGINT) and o.id = '${officeId}' and je.entry_date between '${startDate}' and '${endDate}' ) a on a.tid = j1.transaction_id and j1.account_id <> cast('${GLAccountNO}' as BIGINT) left join acc_gl_account gl on gl.id = a.account_id left join acc_gl_account gl1 on gl1.id = j1.account_id order by j1.entry_date, j1.id ) details left join ( select je.account_id acid2, case when aga1.classification_enum in (1, 5) then (( sum(case when je.type_enum = 2 then coalesce(je.amount, 0) else 0 end)) - sum(case when je.type_enum = 1 then coalesce(je.amount, 0) else 0 end)) else( sum(case when je.type_enum = 1 then coalesce(je.amount, 0) else 0 end) - sum(case when je.type_enum = 2 then coalesce(je.amount, 0) else 0 end)) end as openingbalance from m_office o left join m_office ounder on ounder.hierarchy like concat(o.hierarchy, '%') left join acc_gl_journal_entry je on je.office_id = ounder.id left join acc_gl_account aga1 on aga1.id = je.account_id where je.entry_date <= date('${startDate}')- interval '3 day' and je.office_running_balance is not null and (o.id = '${officeId}') and je.account_id = cast('${GLAccountNO}' as BIGINT) group by je.account_id, aga1.classification_enum ) opb on opb.acid2 = details.acid1 left join ( select name branchname from m_office mo where mo.id = 1 ) branch on details.office_id = '${officeId}' group by details.edate, details.acid1, details.report_header, details.reportid, details.account_name, branch.branchname, transtype, details.description, openingbalance, details.actype	\N	f	t	f
195	Income Statement Table	Table	\N	Accounting	( select * from ( select debits.glcode as "glcode", debits.name as "name", 'Expense' as IncomeOrExpense, ( coalesce(debits.debitamount, 0) - coalesce(credits.creditamount, 0) ) as "balance" from ( select acc_gl_account.gl_code as "glcode", name, sum(amount) as "debitamount" from acc_gl_journal_entry, acc_gl_account where acc_gl_account.id = acc_gl_journal_entry.account_id and acc_gl_journal_entry.type_enum = 2 and acc_gl_account.classification_enum in (5) and acc_gl_journal_entry.entry_date between '${startDate}' and '${endDate}' and ( acc_gl_journal_entry.office_id = '${officeId}' or '${officeId}' = 1 ) group by gl_code,acc_gl_account.name order by glcode ) debits LEFT OUTER JOIN ( select acc_gl_account.gl_code as "glcode", name, sum(amount) as "creditamount" from acc_gl_journal_entry, acc_gl_account where acc_gl_account.id = acc_gl_journal_entry.account_id and acc_gl_journal_entry.type_enum = 1 and acc_gl_account.classification_enum in (5) and acc_gl_journal_entry.entry_date between '${startDate}' and '${endDate}' and ( acc_gl_journal_entry.office_id = '${officeId}' or '${officeId}' = 1 ) group by gl_code,acc_gl_account.name ) credits on debits.glcode = credits.glcode union select credits.glcode as "glcode", credits.name as "name", 'Expense' as IncomeOrExpense, ( coalesce(debits.debitamount, 0) - coalesce(credits.creditamount, 0) ) as "balance" from ( select acc_gl_account.gl_code as "glcode", name, sum(amount) as "debitamount" from acc_gl_journal_entry, acc_gl_account where acc_gl_account.id = acc_gl_journal_entry.account_id and acc_gl_journal_entry.type_enum = 2 and acc_gl_account.classification_enum in (5) and acc_gl_journal_entry.entry_date between '${startDate}' and '${endDate}' and ( acc_gl_journal_entry.office_id = '${officeId}' or '${officeId}' = 1 ) group by gl_code , acc_gl_account.name order by glcode ) debits RIGHT OUTER JOIN ( select acc_gl_account.gl_code as "glcode", name as "name", sum(amount) as "creditamount" from acc_gl_journal_entry, acc_gl_account where acc_gl_account.id = acc_gl_journal_entry.account_id and acc_gl_journal_entry.type_enum = 1 and acc_gl_account.classification_enum in (5) and acc_gl_journal_entry.entry_date between '${startDate}' and '${endDate}' and ( acc_gl_journal_entry.office_id = '${officeId}' or '${officeId}' = 1 ) group by gl_code , acc_gl_account.name order by glcode ) credits on debits.glcode = credits.glcode ) as fullouterjoinresult order by glcode ) UNION ( select * from ( select debits.glcode as "glcode", debits.name as "name", 'Income' as IncomeOrExpense, ( coalesce(credits.creditamount, 0) - coalesce(debits.debitamount, 0) ) as "balance" from ( select acc_gl_account.gl_code as "glcode", name, sum(amount) as "debitamount" from acc_gl_journal_entry, acc_gl_account where acc_gl_account.id = acc_gl_journal_entry.account_id and acc_gl_journal_entry.type_enum = 2 and acc_gl_account.classification_enum in (4) and acc_gl_journal_entry.entry_date between '${startDate}' and '${endDate}' and ( acc_gl_journal_entry.office_id = '${officeId}' or '${officeId}' = 1 ) group by glcode, acc_gl_account.name order by glcode ) debits LEFT OUTER JOIN ( select acc_gl_account.gl_code as "glcode", name, sum(amount) as "creditamount" from acc_gl_journal_entry, acc_gl_account where acc_gl_account.id = acc_gl_journal_entry.account_id and acc_gl_journal_entry.type_enum = 1 and acc_gl_account.classification_enum in (4) and acc_gl_journal_entry.entry_date between '${startDate}' and '${endDate}' and ( acc_gl_journal_entry.office_id = '${officeId}' or '${officeId}' = 1 ) group by glcode , acc_gl_account.name order by glcode ) credits on debits.glcode = credits.glcode union select credits.glcode as "glcode", credits.name as "name", 'Income' as IncomeOrExpense, ( coalesce(credits.creditamount, 0) - coalesce(debits.debitamount, 0) ) as "balance" from ( select acc_gl_account.gl_code as "glcode", name, sum(amount) as "debitamount" from acc_gl_journal_entry, acc_gl_account where acc_gl_account.id = acc_gl_journal_entry.account_id and acc_gl_journal_entry.type_enum = 2 and acc_gl_account.classification_enum in (4) and acc_gl_journal_entry.entry_date between '${startDate}' and '${endDate}' and ( acc_gl_journal_entry.office_id = '${officeId}' or '${officeId}' = 1 ) group by glcode ,acc_gl_account.name order by glcode ) debits RIGHT OUTER JOIN ( select acc_gl_account.gl_code as glcode, name as "name", sum(amount) as creditamount from acc_gl_journal_entry, acc_gl_account where acc_gl_account.id = acc_gl_journal_entry.account_id and acc_gl_journal_entry.type_enum = 1 and acc_gl_account.classification_enum in (4) and acc_gl_journal_entry.entry_date between '${startDate}' and '${endDate}' and ( acc_gl_journal_entry.office_id = '${officeId}' or '${officeId}' = 1 ) group by glcode ,acc_gl_account.name order by glcode ) credits on debits.glcode = credits.glcode ) as fullouterjoinresult order by glcode )	Profit and Loss Statement	t	t	f
196	Balance Sheet Table	Table	\N	Accounting	 ( select debits.glcode as glcode, debits.name as "name", 'Assets' as BalanceType, ( coalesce(debits.debitamount, 0) - coalesce(credits.creditamount, 0) ) as balance from ( select acc_gl_account.gl_code as glcode, name, sum(amount) as debitamount from acc_gl_journal_entry, acc_gl_account where acc_gl_account.id = acc_gl_journal_entry.account_id and acc_gl_journal_entry.type_enum = 2 and acc_gl_account.classification_enum in (1) and acc_gl_journal_entry.entry_date <= '${endDate}' and ( acc_gl_journal_entry.office_id = '${officeId}' or '${officeId}' = 1 ) group by glcode, acc_gl_account.name order by glcode ) debits left outer join ( select acc_gl_account.gl_code as glcode, name, sum(amount) as creditamount from acc_gl_journal_entry, acc_gl_account where acc_gl_account.id = acc_gl_journal_entry.account_id and acc_gl_journal_entry.type_enum = 1 and acc_gl_account.classification_enum in (1) and acc_gl_journal_entry.entry_date <= '${endDate}' and ( acc_gl_journal_entry.office_id = '${officeId}' or '${officeId}' = 1 ) group by glcode, acc_gl_account.name order by glcode ) credits on debits.glcode = credits.glcode union select credits.glcode as glcode, credits.name as "name", 'Assets' as BalanceType, ( coalesce(debits.debitamount, 0) - coalesce(credits.creditamount, 0) ) as balance from ( select acc_gl_account.gl_code as glcode, name, sum(amount) as debitamount from acc_gl_journal_entry, acc_gl_account where acc_gl_account.id = acc_gl_journal_entry.account_id and acc_gl_journal_entry.type_enum = 2 and acc_gl_account.classification_enum in (1) and acc_gl_journal_entry.entry_date <= '${endDate}' and ( acc_gl_journal_entry.office_id = '${officeId}' or '${officeId}' = 1 ) group by glcode, acc_gl_account.name order by glcode ) debits right outer join ( select acc_gl_account.gl_code as glcode, name, sum(amount) as creditamount from acc_gl_journal_entry, acc_gl_account where acc_gl_account.id = acc_gl_journal_entry.account_id and acc_gl_journal_entry.type_enum = 1 and acc_gl_account.classification_enum in (1) and acc_gl_journal_entry.entry_date <= '${endDate}' and ( acc_gl_journal_entry.office_id = '${officeId}' or '${officeId}' = 1 ) group by glcode, acc_gl_account.name order by glcode ) credits on debits.glcode = credits.glcode union select debits.glcode as glcode, debits.name as "name", 'Liability' as BalanceType, ( coalesce(credits.creditamount, 0) - coalesce(debits.debitamount, 0) ) as balance from ( select acc_gl_account.gl_code as glcode, name, sum(amount) as debitamount from acc_gl_journal_entry, acc_gl_account where acc_gl_account.id = acc_gl_journal_entry.account_id and acc_gl_journal_entry.type_enum = 2 and acc_gl_account.classification_enum in (2) and acc_gl_journal_entry.entry_date <= '${endDate}' and ( acc_gl_journal_entry.office_id = '${officeId}' or '${officeId}' = 1 ) group by glcode, acc_gl_account.name order by glcode ) debits left outer join ( select acc_gl_account.gl_code as glcode, name, sum(amount) as creditamount from acc_gl_journal_entry, acc_gl_account where acc_gl_account.id = acc_gl_journal_entry.account_id and acc_gl_journal_entry.type_enum = 1 and acc_gl_account.classification_enum in (2) and acc_gl_journal_entry.entry_date <= '${endDate}' and ( acc_gl_journal_entry.office_id = '${officeId}' or '${officeId}' = 1 ) group by glcode, acc_gl_account.name order by glcode ) credits on debits.glcode = credits.glcode union select credits.glcode as glcode, credits.name as "name", 'Liability' as BalanceType, ( coalesce(credits.creditamount, 0) - coalesce(debits.debitamount, 0) ) as "balance" from ( select acc_gl_account.gl_code as "glcode", name, sum(amount) as "debitamount" from acc_gl_journal_entry, acc_gl_account where acc_gl_account.id = acc_gl_journal_entry.account_id and acc_gl_journal_entry.type_enum = 2 and acc_gl_account.classification_enum in (2) and acc_gl_journal_entry.entry_date <= '${endDate}' and ( acc_gl_journal_entry.office_id = '${officeId}' or '${officeId}' = 1 ) group by glcode, acc_gl_account.name order by glcode ) debits right outer join ( select acc_gl_account.gl_code as "glcode", name, sum(amount) as "creditamount" from acc_gl_journal_entry, acc_gl_account where acc_gl_account.id = acc_gl_journal_entry.account_id and acc_gl_journal_entry.type_enum = 1 and acc_gl_account.classification_enum in (2) and acc_gl_journal_entry.entry_date <= '${endDate}' and ( acc_gl_journal_entry.office_id = '${officeId}' or '${officeId}' = 1 ) group by glcode, acc_gl_account.name order by glcode ) credits on debits.glcode = credits.glcode union select debits.glcode as "glcode", debits.name as "name", 'Equity' as BalanceType, ( coalesce(credits.creditamount, 0) - coalesce(debits.debitamount, 0) ) as "balance" from ( select acc_gl_account.gl_code as glcode, name, sum(amount) as debitamount from acc_gl_journal_entry, acc_gl_account where acc_gl_account.id = acc_gl_journal_entry.account_id and acc_gl_journal_entry.type_enum = 2 and acc_gl_account.classification_enum in (3) and acc_gl_journal_entry.entry_date <= '${endDate}' and ( acc_gl_journal_entry.office_id = '${officeId}' or '${officeId}' = 1 ) group by glcode, acc_gl_account.name order by glcode ) debits left outer join ( select acc_gl_account.gl_code as glcode, name, sum(amount) as creditamount from acc_gl_journal_entry, acc_gl_account where acc_gl_account.id = acc_gl_journal_entry.account_id and acc_gl_journal_entry.type_enum = 1 and acc_gl_account.classification_enum in (3) and acc_gl_journal_entry.entry_date <= '${endDate}' and ( acc_gl_journal_entry.office_id = '${officeId}' or '${officeId}' = 1 ) group by glcode, acc_gl_account.name order by glcode ) credits on debits.glcode = credits.glcode union select credits.glcode as glcode, credits.name as "name", 'Equity' as BalanceType, ( coalesce(credits.creditamount, 0) - coalesce(debits.debitamount, 0) ) as balance from ( select acc_gl_account.gl_code as glcode, name, sum(amount) as debitamount from acc_gl_journal_entry, acc_gl_account where acc_gl_account.id = acc_gl_journal_entry.account_id and acc_gl_journal_entry.type_enum = 2 and acc_gl_account.classification_enum in (3) and acc_gl_journal_entry.entry_date <= '${endDate}' and ( acc_gl_journal_entry.office_id = '${officeId}' or '${officeId}' = 1 ) group by glcode, acc_gl_account.name order by glcode ) debits right outer join ( select acc_gl_account.gl_code as glcode, name, sum(amount) as creditamount from acc_gl_journal_entry, acc_gl_account where acc_gl_account.id = acc_gl_journal_entry.account_id and acc_gl_journal_entry.type_enum = 1 and acc_gl_account.classification_enum in (3) and acc_gl_journal_entry.entry_date <= '${endDate}' and ( acc_gl_journal_entry.office_id = '${officeId}' or '${officeId}' = 1 ) group by glcode, acc_gl_account.name order by glcode ) credits on debits.glcode = credits.glcode )	Balance Sheet	t	t	f
174	Loan payments received (Active Loans)	SMS	NonTriggered	Loan	SELECT mc.id AS id, mc.firstname AS firstName, mc.middlename AS middleName, mc.lastname AS lastName, mc.display_name AS fullName, mc.mobile_no AS mobileNo, ml.principal_amount AS loanAmount, (COALESCE(ml.principal_outstanding_derived, 0) + COALESCE(ml.interest_outstanding_derived, 0) + COALESCE(ml.fee_charges_outstanding_derived, 0) + COALESCE(ml.penalty_charges_outstanding_derived, 0)) AS loanOutstanding, ounder.id AS officeNumber, ml.account_no AS loanAccountNumber, SUM(lt.amount) AS repaymentAmount FROM m_office mo JOIN m_office ounder ON ounder.hierarchy LIKE CONCAT(mo.hierarchy, '%') INNER JOIN m_client mc ON mc.office_id = ounder.id INNER JOIN m_loan ml ON ml.client_id = mc.id INNER JOIN r_enum_value rev ON rev.enum_id = ml.loan_status_id AND rev.enum_name = 'loan_status_id' INNER JOIN m_loan_transaction lt ON lt.loan_id = ml.id INNER JOIN m_appuser au ON au.id = lt.created_by LEFT JOIN m_loan_arrears_aging laa ON laa.loan_id = ml.id LEFT JOIN m_payment_detail mpd ON mpd.id = lt.payment_detail_id LEFT JOIN m_currency cur ON cur.code = ml.currency_code LEFT JOIN m_group_client gc ON gc.client_id = mc.id LEFT JOIN m_group g ON g.id = gc.group_id LEFT JOIN m_staff lo ON lo.id = ml.loan_officer_id LEFT JOIN m_guarantor gua ON gua.loan_id = ml.id WHERE ml.loan_status_id = 300 AND mo.id = '${officeId}' AND (COALESCE(ml.loan_officer_id, -10) = ${loanOfficerId} OR '-1' = ${loanOfficerId}) AND (EXTRACT(DAY FROM (CURRENT_DATE - lt.transaction_date::TIMESTAMP)) BETWEEN 9${fromX} AND ${toY}) AND lt.is_reversed = false AND lt.transaction_type_enum = 2 AND laa.loan_id IS NULL GROUP BY ml.id, mc.id, ounder.id ORDER BY ounder.hierarchy, ml.currency_code, mc.account_no, ml.account_no	Payments received in the last fromX to toY days for any loan with the status Active (on-time)	f	t	f
175	Loan payments received (Overdue Loans)	SMS	NonTriggered	Loan	SELECT ml.id AS loanId, mc.id AS id, mc.firstname AS firstName, mc.middlename AS middleName, mc.lastname AS lastName, mc.display_name AS fullName, mc.mobile_no AS mobileNo, ml.principal_amount AS loanAmount, (COALESCE(ml.principal_outstanding_derived, 0) + COALESCE(ml.interest_outstanding_derived, 0) + COALESCE(ml.fee_charges_outstanding_derived, 0) + COALESCE(ml.penalty_charges_outstanding_derived, 0)) AS loanOutstanding, ounder.id AS officeNumber, ml.account_no AS loanAccountNumber, SUM(lt.amount) AS repaymentAmount FROM m_office mo JOIN m_office ounder ON ounder.hierarchy LIKE CONCAT(mo.hierarchy, '%') INNER JOIN m_client mc ON mc.office_id = ounder.id INNER JOIN m_loan ml ON ml.client_id = mc.id INNER JOIN r_enum_value rev ON rev.enum_id = ml.loan_status_id AND rev.enum_name = 'loan_status_id' INNER JOIN m_loan_arrears_aging laa ON laa.loan_id = ml.id INNER JOIN m_loan_transaction lt ON lt.loan_id = ml.id INNER JOIN m_appuser au ON au.id = lt.created_by LEFT JOIN m_payment_detail mpd ON mpd.id = lt.payment_detail_id LEFT JOIN m_currency cur ON cur.code = ml.currency_code LEFT JOIN m_group_client gc ON gc.client_id = mc.id LEFT JOIN m_group g ON g.id = gc.group_id LEFT JOIN m_staff lo ON lo.id = ml.loan_officer_id LEFT JOIN m_guarantor gua ON gua.loan_id = ml.id WHERE ml.loan_status_id = 300 AND mo.id = '${officeId}' AND (COALESCE(ml.loan_officer_id, -10) = ${loanOfficerId} OR '-1' = ${loanOfficerId}) AND (EXTRACT(DAY FROM(CURRENT_DATE - lt.transaction_date::TIMESTAMP)) BETWEEN ${fromX} AND ${toY}) AND (EXTRACT(DAY FROM(CURRENT_DATE - laa.overdue_since_date_derived::TIMESTAMP)) BETWEEN ${overdueX} AND ${overdueY}) AND lt.is_reversed = false AND lt.transaction_type_enum = 2 GROUP BY ml.id, mc.id, ounder.id ORDER BY ounder.hierarchy, ml.currency_code, mc.account_no, ml.account_no	Payments received in the last fromX to toY days for any loan with the status Overdue (arrears) between overdueX and overdueY days	f	t	f
155	Demand Vs Collection	Table		Loan	SELECT amount.AmountDue-amount.AmountPaid as AmountDue, amount.AmountPaid as AmountPaid FROM             (SELECT             (COALESCE(SUM(ls.principal_amount),0) - COALESCE(SUM(ls.principal_writtenoff_derived),0)              + COALESCE(SUM(ls.interest_amount),0) - COALESCE(SUM(ls.interest_writtenoff_derived),0)              - COALESCE(SUM(ls.interest_waived_derived),0)              + COALESCE(SUM(ls.fee_charges_amount),0) - COALESCE(SUM(ls.fee_charges_writtenoff_derived),0)              - COALESCE(SUM(ls.fee_charges_waived_derived),0)              + COALESCE(SUM(ls.penalty_charges_amount),0) - COALESCE(SUM(ls.penalty_charges_writtenoff_derived),0)              - COALESCE(SUM(ls.penalty_charges_waived_derived),0)             ) AS AmountDue,             (COALESCE(SUM(ls.principal_completed_derived),0) - COALESCE(SUM(ls.principal_writtenoff_derived),0) + COALESCE(SUM(ls.interest_completed_derived),0) - COALESCE(SUM(ls.interest_writtenoff_derived),0)              - COALESCE(SUM(ls.interest_waived_derived),0)              + COALESCE(SUM(ls.fee_charges_completed_derived),0) - COALESCE(SUM(ls.fee_charges_writtenoff_derived),0)              - COALESCE(SUM(ls.fee_charges_waived_derived),0)              + COALESCE(SUM(ls.penalty_charges_completed_derived),0) - COALESCE(SUM(ls.penalty_charges_writtenoff_derived),0)              - COALESCE(SUM(ls.penalty_charges_waived_derived),0)             ) AS AmountPaid             FROM m_office o             LEFT JOIN m_client cl ON o.id = cl.office_id             LEFT JOIN m_loan ln ON cl.id = ln.client_id             LEFT JOIN m_loan_repayment_schedule ls ON ln.id = ls.loan_id             WHERE              (o.hierarchy LIKE CONCAT((SELECT ino.hierarchy FROM m_office ino WHERE ino.id = ${officeId}),'%'))) as amount             	Demand Vs Collection	t	f	f
156	Disbursal Vs Awaitingdisbursal	Table		Loan	SELECT awaitinddisbursal.amount-disbursedAmount.amount as amountToBeDisburse, disbursedAmount.amount as disbursedAmount from             (SELECT COUNT(ln.id) AS noOfLoans, COALESCE(SUM(ln.principal_amount),0) AS amount FROM m_office o             LEFT JOIN m_client cl ON cl.office_id = o.id             LEFT JOIN m_loan ln ON cl.id = ln.client_id             WHERE (ln.loan_status_id=200 OR ln.loan_status_id=300) AND                 o.hierarchy like concat((select ino.hierarchy from m_office ino where ino.id = ${officeId}),'%' )             ) awaitinddisbursal,             (SELECT COUNT(ltrxn.id) as count, COALESCE(SUM(ltrxn.amount),0) as amount FROM m_office o             LEFT JOIN m_client cl ON cl.office_id = o.id             LEFT JOIN m_loan ln ON cl.id = ln.client_id             LEFT JOIN m_loan_transaction ltrxn ON ln.id = ltrxn.loan_id             WHERE ltrxn.is_reversed = false AND ltrxn.transaction_type_enum=1 AND                 o.hierarchy like concat((select ino.hierarchy from m_office ino where ino.id = ${officeId}),'%' )             ) disbursedAmount             	Disbursal_Vs_Awaitingdisbursal	t	f	f
198	Transaction Summary Report	Table	\N	Accounting	SELECT             '${endDate}' AS TransactionDate,             a.product AS Product,             (               SELECT                 enum_message_property               FROM                 r_enum_value               WHERE                 enum_name = 'transaction_type_enum'                 and enum_id = a.transaction_type             ) TransactionType_Name,             (               select                 value               from                 m_payment_type               where                 id = a.payment_type_id             ) as PaymentType_Name,             a.chargetype as chargetype,             a.reversal_indicator AS Reversed,             a.Allocation_Type AS Allocation_Type,             '' AS Chargeoff_ReasonCode,             case when a.transaction_type IN (2, 23, 21, 22, 24, 4, 5, 8, 6, 27, 9, 26, 41, 43)             AND a.reversal_indicator = false then sum(a.amount) * -1 when a.transaction_type IN (2, 23, 21, 22, 24, 4, 5, 8, 6, 27, 9, 26, 41, 43)             AND a.reversal_indicator = true then sum(a.amount) * + 1 when a.transaction_type IN (1, 10, 25, 20, 40, 42)             AND a.reversal_indicator = false then sum(a.amount) * + 1 when a.transaction_type IN (1, 10, 25, 20, 40, 42)             AND a.reversal_indicator = true then sum(a.amount) * -1 end AS Transaction_Amount           FROM             (               SELECT                 '${endDate}' AS transactiondate,                 t.id,                 l.name AS product,                 t.transaction_type_enum AS transaction_type,                 d.payment_type_id,                 '' as chargetype,                 false AS reversal_indicator,                 'Principal' AS Allocation_Type,                 CASE when t.transaction_type_enum in (1) then (                   case when t.amount is null then 0 else t.amount end                 ) else (                   case when t.principal_portion_derived is null then 0 else t.principal_portion_derived end                 ) end amount               FROM                 m_loan_transaction t                 JOIN m_loan m ON m.id = t.loan_id                 JOIN m_product_loan l ON l.id = m.product_id                 left join m_payment_detail d on d.id = t.payment_detail_id               WHERE                 t.submitted_on_date = '${endDate}'                 and t.transaction_type_enum not in (10, 26)                 and (t.office_id = ${officeId})               UNION ALL               SELECT                 '${endDate}' AS transactiondate,                 t.id,                 l.name AS product,                 t.transaction_type_enum AS transaction_type,                 d.payment_type_id,                 '' as chargetype,                 false AS reversal_indicator,                 'Interest' AS Allocation_Type,                 case when t.interest_portion_derived is null then 0 else t.interest_portion_derived end AS amount               FROM                 m_loan_transaction t                 JOIN m_loan m ON m.id = t.loan_id                 JOIN m_product_loan l ON l.id = m.product_id                 left join m_payment_detail d on d.id = t.payment_detail_id               WHERE                 t.submitted_on_date = '${endDate}'                 and t.transaction_type_enum not in (10, 26)                 and (t.office_id = ${officeId})               UNION ALL               SELECT                 '${endDate}' AS transactiondate,                 t.id,                 l.name AS product,                 t.transaction_type_enum AS transaction_type,                 d.payment_type_id,                 '' as chargetype,                 false AS reversal_indicator,                 'Fees' AS Allocation_Type,                 case when t.fee_charges_portion_derived is null then 0 else t.fee_charges_portion_derived end as amount               FROM                 m_loan_transaction t                 JOIN m_loan m ON m.id = t.loan_id                 JOIN m_product_loan l ON l.id = m.product_id                 left join m_payment_detail d on d.id = t.payment_detail_id               WHERE                 t.submitted_on_date = '${endDate}'                 and t.transaction_type_enum not in (10, 26)                 and (t.office_id = ${officeId})               UNION ALL               SELECT                 '${endDate}' AS transactiondate,                 t.id,                 l.name AS product,                 t.transaction_type_enum AS transaction_type,                 d.payment_type_id,                 '' as chargetype,                 false AS reversal_indicator,                 'Penalty' AS Allocation_Type,                 case when t.penalty_charges_portion_derived is null then 0 else t.penalty_charges_portion_derived end as amount               FROM                 m_loan_transaction t                 JOIN m_loan m ON m.id = t.loan_id                 JOIN m_product_loan l ON l.id = m.product_id                 left join m_payment_detail d on d.id = t.payment_detail_id               WHERE                 t.submitted_on_date = '${endDate}'                 and t.transaction_type_enum not in (10, 26)                 and (t.office_id = ${officeId})               UNION ALL               SELECT                 '${endDate}' AS transactiondate,                 t.id,                 l.name AS product,                 t.transaction_type_enum AS transaction_type,                 d.payment_type_id,                 '' as chargetype,                 false AS reversal_indicator,                 'Unallocated Credit (UNC)' AS Allocation_Type,                 case when t.overpayment_portion_derived is null then 0 else t.overpayment_portion_derived end as amount               FROM                 m_loan_transaction t                 JOIN m_loan m ON m.id = t.loan_id                 JOIN m_product_loan l ON l.id = m.product_id                 left join m_payment_detail d on d.id = t.payment_detail_id               WHERE                 t.submitted_on_date = '${endDate}'                 and t.transaction_type_enum not in (10, 26)                 and (t.office_id = ${officeId})               UNION ALL               SELECT                 '${endDate}' AS transactiondate,                 t.id,                 l.name AS product,                 t.transaction_type_enum AS transaction_type,                 null,                 mc.name,                 false AS reversal_indicator,                 'Fees' AS Allocation_Type,                 case when pd.amount is null then 0 else pd.amount end as amount               FROM                 m_loan_transaction t                 JOIN m_loan m ON m.id = t.loan_id                 JOIN m_product_loan l ON l.id = m.product_id                 join m_loan_charge_paid_by pd on pd.loan_transaction_id = t.id                 join m_loan_charge c on c.id = pd.loan_charge_id                 join m_charge mc on mc.id = c.charge_id                 and mc.is_penalty = false               WHERE                 t.submitted_on_date = '${endDate}'                 and t.transaction_type_enum = 10                 and t.is_reversed = false                 and (t.office_id = ${officeId})               UNION ALL               SELECT                 '${endDate}' AS transactiondate,                 t.id,                 l.name AS product,                 t.transaction_type_enum AS transaction_type,                 null,                 mc.name,                 false AS reversal_indicator,                 'Penalty' AS Allocation_Type,                 case when pd.amount is null then 0 else pd.amount end as amount               FROM                 m_loan_transaction t                 JOIN m_loan m ON m.id = t.loan_id                 JOIN m_product_loan l ON l.id = m.product_id                 join m_loan_charge_paid_by pd on pd.loan_transaction_id = t.id                 join m_loan_charge c on c.id = pd.loan_charge_id                 join m_charge mc on mc.id = c.charge_id                 and mc.is_penalty = true               WHERE                 t.submitted_on_date = '${endDate}'                 and t.transaction_type_enum = 10                 and t.is_reversed = false                 and (t.office_id = ${officeId})               UNION ALL               SELECT                 '${endDate}' AS transactiondate,                 t.id,                 l.name AS product,                 t.transaction_type_enum AS transaction_type,                 null,                 '' as chargetype,                 false AS reversal_indicator,                 'Interest' AS Allocation_Type,                 case when t.interest_portion_derived is null then 0 else t.interest_portion_derived end AS amount               FROM                 m_loan_transaction t                 JOIN m_loan m ON m.id = t.loan_id                 JOIN m_product_loan l ON l.id = m.product_id               WHERE                 t.submitted_on_date = '${endDate}'                 and t.transaction_type_enum = 10                 and t.is_reversed = false                 and (t.office_id = ${officeId})               UNION ALL               SELECT                 '${endDate}' AS transactiondate,                 t.id,                 l.name AS product,                 t.transaction_type_enum AS transaction_type,                 d.payment_type_id,                 mc.name,                 false AS reversal_indicator,                 'Fees' AS Allocation_Type,                 case when pd.amount is null then 0 else pd.amount end as amount               FROM                 m_loan_transaction t                 JOIN m_loan m ON m.id = t.loan_id                 JOIN m_product_loan l ON l.id = m.product_id                 join m_loan_charge_paid_by pd on pd.loan_transaction_id = t.id                 join m_loan_charge c on c.id = pd.loan_charge_id                 join m_charge mc on mc.id = c.charge_id                 and mc.is_penalty = false                 left join m_payment_detail d on d.id = t.payment_detail_id               WHERE                 t.submitted_on_date = '${endDate}'                 and t.transaction_type_enum = 26                 and (t.office_id = ${officeId})               UNION ALL               SELECT                 '${endDate}' AS transactiondate,                 t.id,                 l.name AS product,                 t.transaction_type_enum AS transaction_type,                 d.payment_type_id,                 mc.name,                 false AS reversal_indicator,                 'Penalty' AS Allocation_Type,                 case when pd.amount is null then 0 else pd.amount end AS amount               FROM                 m_loan_transaction t                 JOIN m_loan m ON m.id = t.loan_id                 JOIN m_product_loan l ON l.id = m.product_id                 join m_loan_charge_paid_by pd on pd.loan_transaction_id = t.id                 join m_loan_charge c on c.id = pd.loan_charge_id                 join m_charge mc on mc.id = c.charge_id                 and mc.is_penalty = true                 left join m_payment_detail d on d.id = t.payment_detail_id               WHERE                 t.submitted_on_date = '${endDate}'                 and t.transaction_type_enum = 26                 and (t.office_id = ${officeId})               UNION ALL               SELECT                 '${endDate}' AS transactiondate,                 t.id,                 l.name AS product,                 t.transaction_type_enum AS transaction_type,                 d.payment_type_id,                 '' as chargetype,                 false AS reversal_indicator,                 'Interest' AS Allocation_Type,                 case when t.interest_portion_derived is null then 0 else t.interest_portion_derived end AS amount               FROM                 m_loan_transaction t                 JOIN m_loan m ON m.id = t.loan_id                 JOIN m_product_loan l ON l.id = m.product_id                 left join m_payment_detail d on d.id = t.payment_detail_id               WHERE                 t.submitted_on_date = '${endDate}'                 and t.transaction_type_enum = 26                 and (t.office_id = ${officeId})               UNION ALL               SELECT                 '${endDate}' AS transactiondate,                 t.id,                 l.name AS product,                 t.transaction_type_enum AS transaction_type,                 d.payment_type_id,                 '' as chargetype,                 false AS reversal_indicator,                 'Principal' AS Allocation_Type,                 case when t.principal_portion_derived is null then 0 else t.principal_portion_derived end AS amount               FROM                 m_loan_transaction t                 JOIN m_loan m ON m.id = t.loan_id                 JOIN m_product_loan l ON l.id = m.product_id                 left join m_payment_detail d on d.id = t.payment_detail_id               WHERE                 t.submitted_on_date = '${endDate}'                 and t.transaction_type_enum = 26                 and (t.office_id = ${officeId})               UNION ALL               SELECT                 '${endDate}' AS transactiondate,                 t.id,                 l.name AS product,                 t.transaction_type_enum AS transaction_type,                 d.payment_type_id,                 '' as chargetype,                 false AS reversal_indicator,                 'Unallocated Credit (UNC)' AS Allocation_Type,                 case when t.overpayment_portion_derived is null then 0 else t.overpayment_portion_derived end AS amount               FROM                 m_loan_transaction t                 JOIN m_loan m ON m.id = t.loan_id                 JOIN m_product_loan l ON l.id = m.product_id                 left join m_payment_detail d on d.id = t.payment_detail_id               WHERE                 t.submitted_on_date = '${endDate}'                 and t.transaction_type_enum = 26                 and (t.office_id = ${officeId})               UNION ALL               SELECT                 '${endDate}' AS transactiondate,                 t.id,                 l.name AS product,                 t.transaction_type_enum AS transaction_type,                 d.payment_type_id,                 '' as chargetype,                 true AS reversal_indicator,                 'Principal' AS Allocation_Type,                 CASE when t.transaction_type_enum in (1) then (                   case when t.amount is null then 0 else t.amount end                 ) else (                   case when t.principal_portion_derived is null then 0 else t.principal_portion_derived end                 ) end amount               FROM                 m_loan_transaction t                 JOIN m_loan m ON m.id = t.loan_id                 JOIN m_product_loan l ON l.id = m.product_id                 left join m_payment_detail d on d.id = t.payment_detail_id               WHERE                 t.reversed_on_date = '${endDate}'                 and t.transaction_type_enum not in (10, 26)                 and (t.office_id = ${officeId})               UNION ALL               SELECT                 '${endDate}' AS transactiondate,                 t.id,                 l.name AS product,                 t.transaction_type_enum AS transaction_type,                 d.payment_type_id,                 '' as chargetype,                 true AS reversal_indicator,                 'Interest' AS Allocation_Type,                 case when t.interest_portion_derived is null then 0 else t.interest_portion_derived end AS amount               FROM                 m_loan_transaction t                 JOIN m_loan m ON m.id = t.loan_id                 JOIN m_product_loan l ON l.id = m.product_id                 left join m_payment_detail d on d.id = t.payment_detail_id               WHERE                 t.reversed_on_date = '${endDate}'                 and t.transaction_type_enum not in (10, 26)                 and (t.office_id = ${officeId})               UNION ALL               SELECT                 '${endDate}' AS transactiondate,                 t.id,                 l.name AS product,                 t.transaction_type_enum AS transaction_type,                 d.payment_type_id,                 '' as chargetype,                 true AS reversal_indicator,                 'Fees' AS Allocation_Type,                 case when t.fee_charges_portion_derived is null then 0 else t.fee_charges_portion_derived end as amount               FROM                 m_loan_transaction t                 JOIN m_loan m ON m.id = t.loan_id                 JOIN m_product_loan l ON l.id = m.product_id                 left join m_payment_detail d on d.id = t.payment_detail_id               WHERE                 t.reversed_on_date = '${endDate}'                 and t.transaction_type_enum not in (10, 26)                 and (t.office_id = ${officeId})               UNION ALL               SELECT                 '${endDate}' AS transactiondate,                 t.id,                 l.name AS product,                 t.transaction_type_enum AS transaction_type,                 d.payment_type_id,                 '' as chargetype,                 true AS reversal_indicator,                 'Penalty' AS Allocation_Type,                 case when t.penalty_charges_portion_derived is null then 0 else t.penalty_charges_portion_derived end as amount               FROM                 m_loan_transaction t                 JOIN m_loan m ON m.id = t.loan_id                 JOIN m_product_loan l ON l.id = m.product_id                 left join m_payment_detail d on d.id = t.payment_detail_id               WHERE                 t.reversed_on_date = '${endDate}'                 and t.transaction_type_enum not in (10, 26)                 and (t.office_id = ${officeId})               UNION ALL               SELECT                 '${endDate}' AS transactiondate,                 t.id,                 l.name AS product,                 t.transaction_type_enum AS transaction_type,                 d.payment_type_id,                 '' as chargetype,                 true AS reversal_indicator,                 'Unallocated Credit (UNC)' AS Allocation_Type,                 case when t.overpayment_portion_derived is null then 0 else t.overpayment_portion_derived end as amount               FROM                 m_loan_transaction t                 JOIN m_loan m ON m.id = t.loan_id                 JOIN m_product_loan l ON l.id = m.product_id                 left join m_payment_detail d on d.id = t.payment_detail_id               WHERE                 t.reversed_on_date = '${endDate}'                 and t.transaction_type_enum not in (10, 26)                 and (t.office_id = ${officeId})               UNION ALL               SELECT                 '${endDate}' AS transactiondate,                 t.id,                 l.name AS product,                 t.transaction_type_enum AS transaction_type,                 null,                 mc.name,                 true AS reversal_indicator,                 'Fees' AS Allocation_Type,                 case when pd.amount is null then 0 else pd.amount end as amount               FROM                 m_loan_transaction t                 JOIN m_loan m ON m.id = t.loan_id                 JOIN m_product_loan l ON l.id = m.product_id                 join m_loan_charge_paid_by pd on pd.loan_transaction_id = t.id                 join m_loan_charge c on c.id = pd.loan_charge_id                 join m_charge mc on mc.id = c.charge_id                 and mc.is_penalty = false               WHERE                 t.reversed_on_date = '${endDate}'                 and t.transaction_type_enum = 10                 and t.is_reversed = true                 and (t.office_id = ${officeId})               UNION ALL               SELECT                 '${endDate}' AS transactiondate,                 t.id,                 l.name AS product,                 t.transaction_type_enum AS transaction_type,                 null,                 mc.name,                 true AS reversal_indicator,                 'Penalty' AS Allocation_Type,                 case when pd.amount is null then 0 else pd.amount end as amount               FROM                 m_loan_transaction t                 JOIN m_loan m ON m.id = t.loan_id                 JOIN m_product_loan l ON l.id = m.product_id                 join m_loan_charge_paid_by pd on pd.loan_transaction_id = t.id                 join m_loan_charge c on c.id = pd.loan_charge_id                 join m_charge mc on mc.id = c.charge_id                 and mc.is_penalty = true               WHERE                 t.reversed_on_date = '${endDate}'                 and t.transaction_type_enum = 10                 and t.is_reversed = true                 and (t.office_id = ${officeId})               UNION ALL               SELECT                 '${endDate}' AS transactiondate,                 t.id,                 l.name AS product,                 t.transaction_type_enum AS transaction_type,                 null,                 '' as chargetype,                 true AS reversal_indicator,                 'Interest' AS Allocation_Type,                 case when t.interest_portion_derived is null then 0 else t.interest_portion_derived end AS amount               FROM                 m_loan_transaction t                 JOIN m_loan m ON m.id = t.loan_id                 JOIN m_product_loan l ON l.id = m.product_id               WHERE                 t.reversed_on_date = '${endDate}'                 and t.transaction_type_enum = 10                 and t.is_reversed = true                 and (t.office_id = ${officeId})               UNION ALL               SELECT                 '${endDate}' AS transactiondate,                 t.id,                 l.name AS product,                 t.transaction_type_enum AS transaction_type,                 d.payment_type_id,                 mc.name,                 true AS reversal_indicator,                 'Fees' AS Allocation_Type,                 case when pd.amount is null then 0 else pd.amount end as amount               FROM                 m_loan_transaction t                 JOIN m_loan m ON m.id = t.loan_id                 JOIN m_product_loan l ON l.id = m.product_id                 join m_loan_charge_paid_by pd on pd.loan_transaction_id = t.id                 join m_loan_charge c on c.id = pd.loan_charge_id                 join m_charge mc on mc.id = c.charge_id                 and mc.is_penalty = false                 left join m_payment_detail d on d.id = t.payment_detail_id               WHERE                 t.reversed_on_date = '${endDate}'                 and t.transaction_type_enum = 26                 and (t.office_id = ${officeId})               UNION ALL               SELECT                 '${endDate}' AS transactiondate,                 t.id,                 l.name AS product,                 t.transaction_type_enum AS transaction_type,                 d.payment_type_id,                 mc.name,                 true AS reversal_indicator,                 'Penalty' AS Allocation_Type,                 case when pd.amount is null then 0 else pd.amount end AS amount               FROM                 m_loan_transaction t                 JOIN m_loan m ON m.id = t.loan_id                 JOIN m_product_loan l ON l.id = m.product_id                 join m_loan_charge_paid_by pd on pd.loan_transaction_id = t.id                 join m_loan_charge c on c.id = pd.loan_charge_id                 join m_charge mc on mc.id = c.charge_id                 and mc.is_penalty = true                 left join m_payment_detail d on d.id = t.payment_detail_id               WHERE                 t.reversed_on_date = '${endDate}'                 and t.transaction_type_enum = 26                 and (t.office_id = ${officeId})               UNION ALL               SELECT                 '${endDate}' AS transactiondate,                 t.id,                 l.name AS product,                 t.transaction_type_enum AS transaction_type,                 d.payment_type_id,                 '' as chargetype,                 true AS reversal_indicator,                 'Interest' AS Allocation_Type,                 case when t.interest_portion_derived is null then 0 else t.interest_portion_derived end AS amount               FROM                 m_loan_transaction t                 JOIN m_loan m ON m.id = t.loan_id                 JOIN m_product_loan l ON l.id = m.product_id                 left join m_payment_detail d on d.id = t.payment_detail_id               WHERE                 t.reversed_on_date = '${endDate}'                 and t.transaction_type_enum = 26                 and (t.office_id = ${officeId})               UNION ALL               SELECT                 '${endDate}' AS transactiondate,                 t.id,                 l.name AS product,                 t.transaction_type_enum AS transaction_type,                 d.payment_type_id,                 '' as chargetype,                 true AS reversal_indicator,                 'Principal' AS Allocation_Type,                 case when t.principal_portion_derived is null then 0 else t.principal_portion_derived end AS amount               FROM                 m_loan_transaction t                 JOIN m_loan m ON m.id = t.loan_id                 JOIN m_product_loan l ON l.id = m.product_id                 left join m_payment_detail d on d.id = t.payment_detail_id               WHERE                 t.reversed_on_date = '${endDate}'                 and t.transaction_type_enum = 26                 and (t.office_id = ${officeId})               UNION ALL               SELECT                 '${endDate}' AS transactiondate,                 t.id,                 l.name AS product,                 t.transaction_type_enum AS transaction_type,                 d.payment_type_id,                 '' as chargetype,                 true AS reversal_indicator,                 'Unallocated Credit (UNC)' AS Allocation_Type,                 case when t.overpayment_portion_derived is null then 0 else t.overpayment_portion_derived end AS amount               FROM                 m_loan_transaction t                 JOIN m_loan m ON m.id = t.loan_id                 JOIN m_product_loan l ON l.id = m.product_id                 left join m_payment_detail d on d.id = t.payment_detail_id               WHERE                 t.reversed_on_date = '${endDate}'                 and t.transaction_type_enum = 26                 and (t.office_id = ${officeId})             ) a           GROUP BY             a.transactiondate,             a.product,             a.transaction_type,             a.payment_type_id,             a.chargetype,             a.reversal_indicator,             a.Allocation_Type           order by             1,             2,             3,             4,             5,             6,             7 	Transaction Summary Report	f	t	f
197	Trial Balance Summary Report	Table	\N	Accounting	SELECT *             FROM             (               SELECT                 '${endDate}' AS PostingDate,                 loan.pname AS Product,                 loan.gl_code AS GlAcct,                 loan.glname AS Description,                 loan.openingbalance AS BeginningBalance,                 (loan.debitamount * 1) AS DebitMovement,                 (loan.creditamount *-1) AS CreditMovement,                 (                   loan.openingbalance + loan.debitamount - loan.creditamount                 ) AS EndingBalance               FROM                 (                   SELECT                     g.pname,                     g.gl_code,                     g.glname,                     COALESCE(debits.debitamount, 0) - COALESCE(debits.creditamount, 0) AS openingbalance,                     COALESCE(loanproduct.debitamount, 0) AS debitamount,                     COALESCE(loanproduct.creditamount, 0) AS creditamount                   FROM                     (                       SELECT                         ag.gl_code,                         pl.name AS pname,                         ag.name AS glname                       FROM                         acc_gl_account ag                         JOIN acc_product_mapping am ON am.gl_account_id = ag.id                         AND am.product_type = 1                         JOIN m_product_loan pl ON pl.id = am.product_id                     ) g                     LEFT JOIN (                       SELECT                         lp.name AS productname,                         acc_gl_account.gl_code AS glcode,                         acc_gl_account.name AS glname,                         sum(                           case when acc_gl_journal_entry.type_enum = 2 then amount ELSE 0 end                         ) AS debitamount,                         sum(                           case when acc_gl_journal_entry.type_enum = 1 then amount ELSE 0 end                         ) AS creditamount                       FROM                         acc_gl_account                         JOIN acc_gl_journal_entry on acc_gl_account.id = acc_gl_journal_entry.account_id                         JOIN m_loan m ON m.id = acc_gl_journal_entry.entity_id                         JOIN m_product_loan lp ON lp.id = m.product_id                       WHERE                         acc_gl_journal_entry.entity_type_enum = 1                         AND acc_gl_journal_entry.manual_entry = false                         and acc_gl_journal_entry.submitted_on_date < '${endDate}'                         and (                           acc_gl_journal_entry.office_id = ${officeId}                         )                       group by                         productname,                         glcode,                         glname                       order by                         glcode                     ) debits ON g.gl_code = debits.glcode                     AND debits.productname = g.pname                     LEFT JOIN (                       SELECT                         lp.name AS productname, account_id,                         acc_gl_account.gl_code AS glcode,                         acc_gl_account.name AS glname,                         sum(                           case when acc_gl_journal_entry.type_enum = 2 then amount ELSE 0 END                         ) AS debitamount,                         sum(                           case when acc_gl_journal_entry.type_enum = 1 then amount ELSE 0 END                         ) AS creditamount                       FROM                         acc_gl_journal_entry                         JOIN acc_gl_account on acc_gl_account.id = acc_gl_journal_entry.account_id                         JOIN m_loan m ON m.id = acc_gl_journal_entry.entity_id                         JOIN m_product_loan lp ON lp.id = m.product_id                       WHERE                         acc_gl_journal_entry.entity_type_enum = 1                         AND acc_gl_journal_entry.manual_entry = false                         and acc_gl_journal_entry.submitted_on_date = '${endDate}'                         AND (                           acc_gl_journal_entry.office_id = ${officeId}                         )                       group by                         productname,                         account_id,                         glcode,                         glname                       order by                         glcode                     ) loanproduct ON g.gl_code = loanproduct.glcode                     AND loanproduct.productname = g.pname                 ) loan               UNION               SELECT                 '${endDate}' AS PostingDate,                 loan.pname AS Product,                 loan.gl_code AS GlAcct,                 loan.glname AS Description,                 loan.openingbalance AS Beginning_Balance,                 loan.debitamount AS Debit_Movement,                 loan.creditamount AS Credit_Movement,                 (                   loan.openingbalance + loan.debitamount - loan.creditamount                 ) AS Ending_Balance               FROM                 (                   SELECT                     g.pname,                     g.gl_code,                     g.glname,                     COALESCE(debits.debitamount, 0) - COALESCE(debits.creditamount, 0) AS openingbalance,                     COALESCE(loanproduct.debitamount, 0) AS debitamount,                     COALESCE(loanproduct.creditamount, 0) AS creditamount                   FROM                     (                       SELECT                         ag.gl_code,                         pl.name AS pname,                         ag.name AS glname                       FROM                         acc_gl_account ag                         JOIN acc_product_mapping am ON am.gl_account_id = ag.id                         AND am.product_type = 2                         JOIN m_savings_product pl ON pl.id = am.product_id                     ) g                     LEFT join (                       SELECT                         lp.name productname,                         acc_gl_account.gl_code AS glcode,                         acc_gl_account.name AS glname,                         sum(                           case when acc_gl_journal_entry.type_enum = 2 then amount ELSE 0 end                         ) AS debitamount,                         sum(                           case when acc_gl_journal_entry.type_enum = 1 then amount ELSE 0 end                         ) AS creditamount                       FROM                         acc_gl_account                         join acc_gl_journal_entry on acc_gl_account.id = acc_gl_journal_entry.account_id                         JOIN m_savings_account m ON m.id = acc_gl_journal_entry.entity_id                         JOIN m_savings_product lp ON lp.id = m.product_id                       WHERE                         acc_gl_journal_entry.entity_type_enum = 2                         AND acc_gl_journal_entry.manual_entry = false                         and acc_gl_journal_entry.submitted_on_date < '${endDate}'                         and (                           acc_gl_journal_entry.office_id = ${officeId}                         )                       group by                         productname,                         glcode,                         glname                       order by                         glcode                     ) debits ON g.gl_code = debits.glcode                     AND debits.productname = g.pname                     left JOIN (                       SELECT                         lp.name productname,                         acc_gl_account.gl_code AS glcode,                         acc_gl_account.name AS glname,                         sum(                           case when acc_gl_journal_entry.type_enum = 2 then amount ELSE 0 end                         ) AS debitamount,                         sum(                           case when acc_gl_journal_entry.type_enum = 1 then amount ELSE 0 end                         ) AS creditamount                       from                         acc_gl_journal_entry                         join acc_gl_account on acc_gl_account.id = acc_gl_journal_entry.account_id                         JOIN m_savings_account m ON m.id = acc_gl_journal_entry.entity_id                         JOIN m_savings_product lp ON lp.id = m.product_id                       WHERE                         acc_gl_journal_entry.entity_type_enum = 2                         AND acc_gl_journal_entry.manual_entry = false                         and acc_gl_journal_entry.submitted_on_date = '${endDate}'                         and (                           acc_gl_journal_entry.office_id = ${officeId}                         )                       group by                         productname,                         glcode,                         glname                       order by                         glcode                     ) loanproduct ON g.gl_code = loanproduct.glcode                     AND loanproduct.productname = g.pname                 ) loan               UNION               SELECT                 '${endDate}' AS PostingDate,                 'manual' AS Product,                 loan.gl_code AS GlAcct,                 loan.glname AS Description,                 loan.openingbalance AS Beginning_Balance,                 loan.debitamount AS Debit_Movement,                 loan.creditamount AS Credit_Movement,                 (                   loan.openingbalance + loan.debitamount - loan.creditamount                 ) AS Ending_Balance               FROM                 (                   SELECT                     g.gl_code,                     g.name AS glname,                     COALESCE(debits.debitamount, 0) - COALESCE(debits.creditamount, 0) AS openingbalance,                     COALESCE(loanproduct.debitamount, 0) AS debitamount,                     COALESCE(loanproduct.creditamount, 0) AS creditamount                   FROM                     acc_gl_account g                     LEFT join (                       SELECT                         acc_gl_account.gl_code AS glcode,                         acc_gl_account.name AS glname,                         sum(                           case when acc_gl_journal_entry.type_enum = 2 then amount ELSE 0 end                         ) AS debitamount,                         sum(                           case when acc_gl_journal_entry.type_enum = 1 then amount ELSE 0 end                         ) AS creditamount                       FROM                         acc_gl_account                         JOIN acc_gl_journal_entry on acc_gl_account.id = acc_gl_journal_entry.account_id                       WHERE                         acc_gl_journal_entry.manual_entry = true                         and acc_gl_journal_entry.submitted_on_date < '${endDate}'                         and (                           acc_gl_journal_entry.office_id = ${officeId}                         )                       group by                         glcode,                         glname                       order by                         glcode                     ) debits ON g.gl_code = debits.glcode                     left JOIN (                       SELECT                         acc_gl_account.gl_code AS glcode,                         acc_gl_account.name AS glname,                         sum(                           case when acc_gl_journal_entry.type_enum = 2 then amount ELSE 0 end                         ) AS debitamount,                         sum(                           case when acc_gl_journal_entry.type_enum = 1 then amount ELSE 0 end                         ) AS creditamount                       FROM                         acc_gl_journal_entry                         join acc_gl_account on acc_gl_account.id = acc_gl_journal_entry.account_id                       where                         acc_gl_journal_entry.manual_entry = true                         and acc_gl_journal_entry.submitted_on_date = '${endDate}'                         and (                           acc_gl_journal_entry.office_id = ${officeId}                         )                       group by                         glcode,                         glname                       order by                         glcode                     ) loanproduct ON g.gl_code = loanproduct.glcode                 ) loan             ) a           where             a.EndingBalance != 0 or a.DebitMovement != 0 or a.CreditMovement != 0 	Trial Balance Summary Report	f	t	f
153	LoanTrendsByWeek	Table		Loan	SELECT COUNT(ln.id) AS lcount, EXTRACT(WEEK FROM ln.disbursedon_date) AS Weeks             FROM m_office o                 LEFT JOIN m_client cl on o.id = cl.office_id                 LEFT JOIN m_loan ln on cl.id = ln.client_id             WHERE o.hierarchy like concat((select ino.hierarchy from m_office ino where ino.id = ${officeId}),'%' )                 AND (ln.disbursedon_date BETWEEN (CURRENT_DATE - INTERVAL '12 weeks') AND CURRENT_DATE)             GROUP BY EXTRACT(WEEK FROM ln.disbursedon_date)		t	f	f
199	Trial Balance Summary Report with Asset Owner	Table	\N	Accounting	WITH retained_earning AS (SELECT DISTINCT '${endDate}' AS postingdate,\n lp.name AS product,\n gl_code AS glacct,\n COALESCE((SELECT name FROM acc_gl_account WHERE gl_code = e.gl_code),\n '') AS description,\n COALESCE(e.owner_external_id, 'self') AS assetowner,\n SUM(opening_balance_amount) AS beginningbalance,\n 0 AS debitmovement,\n 0 AS creditmovement,\n SUM(opening_balance_amount) AS endingbalance,\n COALESCE(e.originator_external_ids, '') AS originator_external_ids\n FROM acc_gl_journal_entry_annual_summary e,\n m_product_loan lp\n WHERE e.office_id = ${officeId}\n AND lp.id = product_id\n AND EXTRACT(YEAR FROM e.year_end_date) < EXTRACT(YEAR FROM CAST('${endDate}' AS DATE))\n GROUP BY gl_code, lp.name, office_id, owner_external_id, originator_external_ids),\n aggregated_date AS (SELECT MAX(aggregated_on_date_to) AS latest\n FROM m_journal_entry_aggregation_tracking\n WHERE aggregated_on_date_to < '${endDate}'),\n summary_snapshot_baseline_data AS (SELECT lp.NAME AS productname,\n acc_gl_account.gl_code AS glcode,\n acc_gl_account.NAME AS glname,\n CASE\n WHEN ags.external_owner_id IS NULL THEN 0\n ELSE ags.external_owner_id END AS assetowner,\n COALESCE(ags.originator_external_ids, '') AS originator_external_ids,\n SUM(ags.debit_amount) AS debitamount,\n SUM(ags.credit_amount) AS creditamount\n FROM acc_gl_account\n JOIN m_journal_entry_aggregation_summary ags\n ON acc_gl_account.id = ags.gl_account_id\n JOIN m_product_loan lp ON lp.id = ags.product_id\n WHERE ags.entity_type_enum = 1\n AND ags.manual_entry = FALSE\n AND ags.aggregated_on_date <= (SELECT latest FROM aggregated_date)\n AND (ags.office_id = ${officeId})\n GROUP BY productname, glcode, glname, assetowner, originator_external_ids),\n post_snapshot_delta_data AS (SELECT lp.NAME AS productname,\n acc_gl_account.gl_code AS glcode,\n acc_gl_account.NAME AS glname,\n CASE WHEN aw.owner_id IS NULL THEN 0 ELSE aw.owner_id END AS assetowner,\n SUM(CASE WHEN acc_gl_journal_entry.type_enum = 2 THEN amount ELSE 0 END) AS debitamount,\n SUM(CASE WHEN acc_gl_journal_entry.type_enum = 1 THEN amount ELSE 0 END) AS creditamount,\n COALESCE((SELECT STRING_AGG(DISTINCT mlo.external_id, ', ' ORDER BY mlo.external_id)\n FROM m_loan_originator_mapping mlom\n JOIN m_loan_originator mlo ON mlo.id = mlom.originator_id\n WHERE mlom.loan_id = m.id), '') AS originator_external_ids\n FROM acc_gl_account\n JOIN acc_gl_journal_entry\n ON acc_gl_account.id = acc_gl_journal_entry.account_id\n JOIN m_loan m ON m.id = acc_gl_journal_entry.entity_id\n JOIN m_product_loan lp ON lp.id = m.product_id\n LEFT JOIN m_external_asset_owner_journal_entry_mapping aw\n ON aw.journal_entry_id = acc_gl_journal_entry.id\n WHERE acc_gl_journal_entry.entity_type_enum = 1\n AND acc_gl_journal_entry.manual_entry = FALSE\n AND (\n (SELECT latest FROM aggregated_date) IS NULL\n OR\n acc_gl_journal_entry.submitted_on_date > (SELECT latest FROM aggregated_date)\n )\n AND acc_gl_journal_entry.submitted_on_date < '${endDate}'\n AND (acc_gl_journal_entry.office_id = ${officeId})\n GROUP BY productname, glcode, glname, assetowner, originator_external_ids),\n merged_historical_data AS (SELECT COALESCE(s.productname, p.productname) AS productname,\n COALESCE(s.glcode, p.glcode) AS glcode,\n COALESCE(s.glname, p.glname) AS glname,\n COALESCE(s.assetowner, p.assetowner, 0) AS assetowner,\n COALESCE(s.debitamount, 0) + COALESCE(p.debitamount, 0) AS debitamount,\n COALESCE(s.creditamount, 0) + COALESCE(p.creditamount, 0) AS creditamount,\n COALESCE(p.originator_external_ids, s.originator_external_ids, '') AS originator_external_ids\n FROM summary_snapshot_baseline_data s\n LEFT JOIN post_snapshot_delta_data p\n ON s.glcode = p.glcode\n AND s.productname = p.productname\n AND s.assetowner = p.assetowner\n AND s.originator_external_ids = p.originator_external_ids\n\n UNION ALL\n\n SELECT p.productname AS productname,\n p.glcode AS glcode,\n p.glname AS glname,\n COALESCE(p.assetowner, 0) AS assetowner,\n COALESCE(p.debitamount, 0) AS debitamount,\n COALESCE(p.creditamount, 0) AS creditamount,\n COALESCE(p.originator_external_ids, '') AS originator_external_ids\n FROM post_snapshot_delta_data p\n LEFT JOIN summary_snapshot_baseline_data s\n ON s.glcode = p.glcode\n AND s.productname = p.productname\n AND s.assetowner = p.assetowner\n AND s.originator_external_ids = p.originator_external_ids\n WHERE s.glcode IS NULL),\n current_cob_data AS (SELECT lp.name AS productname,\n account_id,\n acc_gl_account.gl_code AS glcode,\n acc_gl_account.name AS glname,\n CASE WHEN aw.owner_id IS NULL THEN 0 ELSE aw.owner_id END AS assetowner,\n SUM(CASE WHEN acc_gl_journal_entry.type_enum = 2 THEN amount ELSE 0 END) AS debitamount,\n SUM(CASE WHEN acc_gl_journal_entry.type_enum = 1 THEN amount ELSE 0 END) AS creditamount,\n COALESCE((SELECT STRING_AGG(DISTINCT mlo.external_id, ', ' ORDER BY mlo.external_id)\n FROM m_loan_originator_mapping mlom\n JOIN m_loan_originator mlo ON mlo.id = mlom.originator_id\n WHERE mlom.loan_id = m.id), '') AS originator_external_ids\n FROM acc_gl_journal_entry\n JOIN acc_gl_account ON acc_gl_account.id = acc_gl_journal_entry.account_id\n JOIN m_loan m ON m.id = acc_gl_journal_entry.entity_id\n JOIN m_product_loan lp ON lp.id = m.product_id\n LEFT JOIN m_external_asset_owner_journal_entry_mapping aw\n ON aw.journal_entry_id = acc_gl_journal_entry.id\n WHERE acc_gl_journal_entry.entity_type_enum = 1\n AND acc_gl_journal_entry.manual_entry = FALSE\n AND acc_gl_journal_entry.submitted_on_date = '${endDate}'\n AND (acc_gl_journal_entry.office_id = ${officeId})\n GROUP BY productname, account_id, glcode, glname, assetowner, originator_external_ids)\n\nSELECT *\nFROM (SELECT *\n FROM retained_earning\n WHERE glacct = (SELECT gl_code FROM acc_gl_account WHERE name = 'Retained Earnings Prior Year')\n\n UNION\n\n SELECT txnreport.postingdate,\n txnreport.product,\n txnreport.glacct,\n txnreport.description,\n txnreport.assetowner,\n (COALESCE(txnreport.beginningbalance, 0) + COALESCE(summary.beginningbalance, 0)) AS beginningbalance,\n txnreport.debitmovement AS debitmovement,\n txnreport.creditmovement AS creditmovement,\n (COALESCE(txnreport.endingbalance, 0) + COALESCE(summary.beginningbalance, 0)) AS endingbalance,\n txnreport.originator_external_ids AS originator_external_ids\n FROM (SELECT *\n FROM (SELECT DISTINCT '${endDate}' AS postingdate,\n loan.pname AS product,\n loan.gl_code AS glacct,\n loan.glname AS description,\n COALESCE((SELECT external_id FROM m_external_asset_owner WHERE id = loan.assetowner),\n 'self') AS assetowner,\n loan.openingbalance AS beginningbalance,\n (loan.debitamount * 1) AS debitmovement,\n (loan.creditamount * -1) AS creditmovement,\n (loan.openingbalance + loan.debitamount - loan.creditamount) AS endingbalance,\n loan.originator_external_ids AS originator_external_ids\n FROM (SELECT DISTINCT g.pname AS pname,\n g.gl_code AS gl_code,\n g.glname AS glname,\n COALESCE(mh.assetowner, c.assetowner, 0) AS assetowner,\n COALESCE(mh.debitamount, 0) - COALESCE(mh.creditamount, 0) AS openingbalance,\n COALESCE(c.debitamount, 0) AS debitamount,\n COALESCE(c.creditamount, 0) AS creditamount,\n COALESCE(mh.originator_external_ids, c.originator_external_ids) AS originator_external_ids\n FROM (SELECT DISTINCT ag.gl_code, ag.id, pl.NAME AS pname, ag.NAME AS glname\n FROM acc_gl_account ag\n JOIN acc_product_mapping am ON am.gl_account_id = ag.id AND am.product_type = 1\n JOIN m_product_loan pl ON pl.id = am.product_id) g\n LEFT JOIN merged_historical_data mh\n ON g.gl_code = mh.glcode\n AND mh.productname = g.pname\n LEFT JOIN current_cob_data c\n ON g.gl_code = c.glcode\n AND c.productname = g.pname\n AND mh.assetowner = c.assetowner\n AND mh.originator_external_ids = c.originator_external_ids\n\n UNION ALL\n\n SELECT DISTINCT c.productname AS pname,\n c.glcode AS gl_code,\n c.glname AS glname,\n COALESCE(c.assetowner, 0) AS assetowner,\n 0 AS openingbalance,\n COALESCE(c.debitamount, 0) AS debitamount,\n COALESCE(c.creditamount, 0) AS creditamount,\n COALESCE(matched.originator_external_ids, c.originator_external_ids) AS originator_external_ids\n FROM current_cob_data c\n LEFT JOIN (SELECT g3.gl_code, g3.pname, mh.assetowner, mh.originator_external_ids\n FROM (SELECT DISTINCT ag.gl_code, pl.NAME AS pname\n FROM acc_gl_account ag\n JOIN acc_product_mapping am\n ON am.gl_account_id = ag.id AND am.product_type = 1\n JOIN m_product_loan pl ON pl.id = am.product_id) g3\n LEFT JOIN merged_historical_data mh\n ON g3.gl_code = mh.glcode\n AND mh.productname = g3.pname) matched\n ON matched.gl_code = c.glcode\n AND matched.pname = c.productname\n AND matched.assetowner = c.assetowner\n AND matched.originator_external_ids = c.originator_external_ids\n WHERE matched.gl_code IS NULL) loan) a) AS txnreport\n LEFT JOIN retained_earning summary\n ON txnreport.glacct = summary.glacct\n AND txnreport.assetowner = summary.assetowner\n AND summary.product = txnreport.product\n AND summary.originator_external_ids = txnreport.originator_external_ids) report\nWHERE report.endingbalance != 0\n OR report.debitmovement != 0\n OR report.creditmovement != 0\nORDER BY glacct	Trial Balance Summary Report with Asset Owner	f	t	f
200	Transaction Summary Report with Asset Owner	Table	\N	Accounting	WITH slt_except_charge_adj_and_accrual AS (SELECT '${endDate}' AS transactiondate,\n          t.id,\n          l.name,\n          d.payment_type_id,\n          CASE\n              WHEN d.payment_type_id IS NULL AND t.classification_cv_id IS NOT NULL\n                  THEN (SELECT code_value FROM m_code_value WHERE id = t.classification_cv_id)\n              ELSE NULL END AS classification_name,\n          t.transaction_type_enum,\n          t.amount,\n          t.overpayment_portion_derived,\n          t.principal_portion_derived,\n          t.interest_portion_derived,\n          t.fee_charges_portion_derived,\n          t.penalty_charges_portion_derived,\n          e.status,\n          e.settlement_date,\n          e.owner_id,\n          m.charged_off_on_date,\n          t.transaction_date,\n          m.charge_off_reason_cv_id,\n          (SELECT STRING_AGG(DISTINCT mlo.external_id, ', ' ORDER BY mlo.external_id) FROM m_loan_originator_mapping mlom JOIN m_loan_originator mlo ON mlo.id = mlom.originator_id WHERE mlom.loan_id = t.loan_id) AS originator_external_ids\n   FROM m_loan_transaction t\n            JOIN m_loan m ON m.id = t.loan_id\n            JOIN m_product_loan l ON l.id = m.product_id\n            LEFT JOIN m_payment_detail d ON d.id = t.payment_detail_id\n            LEFT JOIN m_external_asset_owner_transfer e\n                      ON e.loan_id = t.loan_id AND\n     e.settlement_date < '${endDate}' AND\n     e.effective_date_to >= '${endDate}'\n   WHERE t.submitted_on_date = '${endDate}'\n     AND t.transaction_type_enum not in (10, 26, 32, 34, 36, 39, 42, 43)\n     AND (t.office_id = ${officeId})),\n     slt_charge_adj AS (SELECT '${endDate}' AS transactiondate,\n           t.id,\n           l.name,\n           t.transaction_type_enum,\n           d.payment_type_id,\n           t.overpayment_portion_derived,\n           t.principal_portion_derived,\n           t.interest_portion_derived,\n           t.fee_charges_portion_derived,\n           t.penalty_charges_portion_derived,\n           t.amount,\n           e.status,\n           e.settlement_date,\n           e.owner_id,\n           m.charged_off_on_date,\n           t.transaction_date,\n           m.charge_off_reason_cv_id,\n           (SELECT STRING_AGG(DISTINCT mlo.external_id, ', ' ORDER BY mlo.external_id) FROM m_loan_originator_mapping mlom JOIN m_loan_originator mlo ON mlo.id = mlom.originator_id WHERE mlom.loan_id = t.loan_id) AS originator_external_ids\n    FROM m_loan_transaction t\n             JOIN m_loan m ON m.id = t.loan_id\n             JOIN m_product_loan l ON l.id = m.product_id\n             LEFT JOIN m_payment_detail d ON d.id = t.payment_detail_id\n             LEFT JOIN m_external_asset_owner_transfer e\n   ON e.loan_id = t.loan_id AND e.settlement_date < '${endDate}' AND\n      e.effective_date_to >= '${endDate}'\n    WHERE t.submitted_on_date = '${endDate}'\n      AND t.transaction_type_enum = 26\n      AND (t.office_id = ${officeId})),\n     rlt_except_charge_adj_and_accrual AS (SELECT '${endDate}' AS transactiondate,\n          t.id,\n          l.name,\n          t.transaction_type_enum,\n          d.payment_type_id,\n          CASE\n              WHEN d.payment_type_id IS NULL AND t.classification_cv_id IS NOT NULL\n                  THEN (SELECT code_value FROM m_code_value WHERE id = t.classification_cv_id)\n              ELSE NULL END AS classification_name,\n          t.overpayment_portion_derived,\n          t.principal_portion_derived,\n          t.interest_portion_derived,\n          t.fee_charges_portion_derived,\n          t.penalty_charges_portion_derived,\n          t.amount,\n          e.status,\n          e.settlement_date,\n          e.owner_id,\n          m.charged_off_on_date,\n          t.transaction_date,\n          m.charge_off_reason_cv_id,\n          (SELECT STRING_AGG(DISTINCT mlo.external_id, ', ' ORDER BY mlo.external_id) FROM m_loan_originator_mapping mlom JOIN m_loan_originator mlo ON mlo.id = mlom.originator_id WHERE mlom.loan_id = t.loan_id) AS originator_external_ids\n   FROM m_loan_transaction t\n            JOIN m_loan m ON m.id = t.loan_id\n            JOIN m_product_loan l ON l.id = m.product_id\n            LEFT JOIN m_payment_detail d ON d.id = t.payment_detail_id\n            LEFT JOIN m_external_asset_owner_transfer e\n                      ON e.loan_id = t.loan_id AND\n     e.settlement_date < '${endDate}' AND\n     e.effective_date_to >= '${endDate}'\n   WHERE t.reversed_on_date = '${endDate}'\n     AND t.transaction_type_enum not in (10, 26, 32, 34, 36, 39, 42, 43)\n     AND (t.office_id = ${officeId})),\n     rlt_charge_adj AS (SELECT '${endDate}' AS transactiondate,\n           t.id,\n           l.name,\n           t.transaction_type_enum,\n           d.payment_type_id,\n           t.overpayment_portion_derived,\n           t.principal_portion_derived,\n           t.interest_portion_derived,\n           t.fee_charges_portion_derived,\n           t.penalty_charges_portion_derived,\n           t.amount,\n           e.status,\n           e.settlement_date,\n           e.owner_id,\n           m.charged_off_on_date,\n           t.transaction_date,\n           m.charge_off_reason_cv_id,\n           (SELECT STRING_AGG(DISTINCT mlo.external_id, ', ' ORDER BY mlo.external_id) FROM m_loan_originator_mapping mlom JOIN m_loan_originator mlo ON mlo.id = mlom.originator_id WHERE mlom.loan_id = t.loan_id) AS originator_external_ids\n    FROM m_loan_transaction t\n             JOIN m_loan m ON m.id = t.loan_id\n             JOIN m_product_loan l ON l.id = m.product_id\n             LEFT JOIN m_payment_detail d ON d.id = t.payment_detail_id\n             LEFT JOIN m_external_asset_owner_transfer e\n   ON e.loan_id = t.loan_id AND e.settlement_date < '${endDate}' AND\n      e.effective_date_to >= '${endDate}'\n    WHERE t.reversed_on_date = '${endDate}'\n      AND t.transaction_type_enum = 26\n      AND (t.office_id = ${officeId})),\n     slt_cap_income_amortization AS (SELECT '${endDate}' AS transactiondate,\n    t.id,\n    l.name,\n    t.transaction_type_enum,\n    d.payment_type_id,\n    CASE\n        WHEN d.payment_type_id IS NULL AND bt.classification_cv_id IS NOT NULL\n            THEN (SELECT code_value FROM m_code_value WHERE id = bt.classification_cv_id)\n        ELSE NULL END AS classification_name,\n    CASE\n        WHEN t.overpayment_portion_derived IS NOT NULL\n            THEN map.amount\n        ELSE NULL END AS overpayment_portion_derived,\n    CASE\n        WHEN t.principal_portion_derived IS NOT NULL\n            THEN map.amount\n        ELSE NULL END AS principal_portion_derived,\n    CASE\n        WHEN t.interest_portion_derived IS NOT NULL\n            THEN map.amount\n        ELSE NULL END AS interest_portion_derived,\n    CASE\n        WHEN t.fee_charges_portion_derived IS NOT NULL\n            THEN map.amount\n        ELSE NULL END AS fee_charges_portion_derived,\n    CASE\n        WHEN t.penalty_charges_portion_derived IS NOT NULL\n            THEN map.amount\n        ELSE NULL END AS penalty_charges_portion_derived,\n    map.amount,\n    e.status,\n    e.settlement_date,\n    e.owner_id,\n    m.charged_off_on_date,\n    t.transaction_date,\n    m.charge_off_reason_cv_id,\n    (SELECT STRING_AGG(DISTINCT mlo.external_id, ', ' ORDER BY mlo.external_id) FROM m_loan_originator_mapping mlom JOIN m_loan_originator mlo ON mlo.id = mlom.originator_id WHERE mlom.loan_id = t.loan_id) AS originator_external_ids\n                 FROM m_loan_transaction t\n      JOIN m_loan m ON m.id = t.loan_id\n      JOIN m_product_loan l ON l.id = m.product_id\n      LEFT JOIN m_payment_detail d ON d.id = t.payment_detail_id\n      JOIN m_loan_amortization_allocation_mapping map\n           ON map.amortization_loan_transaction_id = t.id\n      JOIN m_loan_transaction bt ON bt.id = map.base_loan_transaction_id\n      LEFT JOIN m_external_asset_owner_transfer e ON e.loan_id = t.loan_id AND\n             e.settlement_date <\n             '${endDate}' AND\n             e.effective_date_to >=\n             '${endDate}'\n                 WHERE t.submitted_on_date = '${endDate}'\n                   AND t.is_reversed = false\n                   AND t.transaction_type_enum IN (36, 39, 42, 43)\n                   AND (t.office_id = ${officeId})),\n     rlt_cap_income_amortization AS (SELECT '${endDate}' AS transactiondate,\n    t.id,\n    l.name,\n    t.transaction_type_enum,\n    d.payment_type_id,\n    CASE\n        WHEN d.payment_type_id IS NULL AND bt.classification_cv_id IS NOT NULL\n            THEN (SELECT code_value FROM m_code_value WHERE id = bt.classification_cv_id)\n        ELSE NULL END AS classification_name,\n    CASE\n        WHEN t.overpayment_portion_derived IS NOT NULL\n            THEN map.amount\n        ELSE NULL END AS overpayment_portion_derived,\n    CASE\n        WHEN t.principal_portion_derived IS NOT NULL\n            THEN map.amount\n        ELSE NULL END AS principal_portion_derived,\n    CASE\n        WHEN t.interest_portion_derived IS NOT NULL\n            THEN map.amount\n        ELSE NULL END AS interest_portion_derived,\n    CASE\n        WHEN t.fee_charges_portion_derived IS NOT NULL\n            THEN map.amount\n        ELSE NULL END AS fee_charges_portion_derived,\n    CASE\n        WHEN t.penalty_charges_portion_derived IS NOT NULL\n            THEN map.amount\n        ELSE NULL END AS penalty_charges_portion_derived,\n    map.amount,\n    e.status,\n    e.settlement_date,\n    e.owner_id,\n    m.charged_off_on_date,\n    t.transaction_date,\n    m.charge_off_reason_cv_id,\n    (SELECT STRING_AGG(DISTINCT mlo.external_id, ', ' ORDER BY mlo.external_id) FROM m_loan_originator_mapping mlom JOIN m_loan_originator mlo ON mlo.id = mlom.originator_id WHERE mlom.loan_id = t.loan_id) AS originator_external_ids\n                 FROM m_loan_transaction t\n      JOIN m_loan m ON m.id = t.loan_id\n      JOIN m_product_loan l ON l.id = m.product_id\n      LEFT JOIN m_payment_detail d ON d.id = t.payment_detail_id\n      JOIN m_loan_amortization_allocation_mapping map\n           ON map.amortization_loan_transaction_id = t.id\n      JOIN m_loan_transaction bt ON bt.id = map.base_loan_transaction_id\n      LEFT JOIN m_external_asset_owner_transfer e ON e.loan_id = t.loan_id AND\n             e.settlement_date <\n             '${endDate}' AND\n             e.effective_date_to >=\n             '${endDate}'\n                 WHERE t.reversed_on_date = '${endDate}'\n                   AND t.is_reversed = true\n                   AND t.transaction_type_enum IN (36, 39, 42, 43)\n                   AND (t.office_id = ${officeId})),\n     active_external_asset_owner_transfers AS (SELECT '${endDate}' AS transactiondate,\n              t.id,\n              p.name,\n              t.owner_id,\n              t.previous_owner_id,\n              dt.principal_outstanding_derived,\n              dt.interest_outstanding_derived,\n              dt.fee_charges_outstanding_derived,\n              dt.penalty_charges_outstanding_derived,\n              dt.total_overpaid_derived,\n              l.charged_off_on_date,\n              t.settlement_date,\n              l.charge_off_reason_cv_id,\n              (SELECT STRING_AGG(DISTINCT mlo.external_id, ', ' ORDER BY mlo.external_id) FROM m_loan_originator_mapping mlom JOIN m_loan_originator mlo ON mlo.id = mlom.originator_id WHERE mlom.loan_id = t.loan_id) AS originator_external_ids\n       FROM m_external_asset_owner_transfer t\n                JOIN m_loan l ON l.id = t.loan_id\n                JOIN m_client c ON c.id = l.client_id\n                JOIN m_product_loan p ON p.id = l.product_id\n                JOIN m_external_asset_owner_transfer_details dt\n                     ON dt.asset_owner_transfer_id = t.id\n       WHERE t.status in ('ACTIVE', 'ACTIVE_INTERMEDIATE')\n         AND c.office_id = ${officeId}\n         AND t.settlement_date = '${endDate}'),\n     buyback_external_asset_owner_transfers AS (SELECT '${endDate}' AS transactiondate,\n               t.id,\n               p.name,\n               dt.principal_outstanding_derived,\n               dt.interest_outstanding_derived,\n               dt.fee_charges_outstanding_derived,\n               dt.penalty_charges_outstanding_derived,\n               dt.total_overpaid_derived,\n               l.charged_off_on_date,\n               t.settlement_date,\n               l.charge_off_reason_cv_id,\n               t.owner_id,\n               (SELECT STRING_AGG(DISTINCT mlo.external_id, ', ' ORDER BY mlo.external_id) FROM m_loan_originator_mapping mlom JOIN m_loan_originator mlo ON mlo.id = mlom.originator_id WHERE mlom.loan_id = t.loan_id) AS originator_external_ids\n        FROM m_external_asset_owner_transfer t\n                 JOIN m_loan l ON l.id = t.loan_id\n                 JOIN m_client c ON c.id = l.client_id\n                 JOIN m_product_loan p ON p.id = l.product_id\n                 JOIN m_external_asset_owner_transfer_details dt\n                      ON dt.asset_owner_transfer_id = t.id\n        WHERE t.status in ('BUYBACK', 'BUYBACK_INTERMEDIATE')\n          AND c.office_id = ${officeId}\n          AND t.settlement_date = '${endDate}')\nSELECT '${endDate}' AS TransactionDate,\n       a.product AS Product,\n       CASE\n           WHEN a.transaction_type = 9999 THEN 'Asset Transfer'\n           WHEN a.transaction_type = 99999 THEN 'Asset Buyback'\n           ELSE (SELECT enum_message_property\n                 FROM r_enum_value\n                 WHERE enum_name = 'transaction_type_enum'\n                   AND enum_id = a.transaction_type) END AS TransactionType_Name,\n       COALESCE((SELECT value FROM m_payment_type WHERE id = a.payment_type_id),\n                a.classification_name) AS PaymentType_Name,\n       a.chargetype AS chargetype,\n       a.reversal_indicator AS Reversed,\n       a.Allocation_Type AS Allocation_Type,\n       (SELECT code_value FROM m_code_value WHERE id = a.charge_off_reason_id) AS Chargeoff_ReasonCode,\n       CASE\n           WHEN a.transaction_type = 9999 THEN sum(a.amount) * + 1\n           WHEN a.transaction_type = 99999 THEN sum(a.amount) * - 1\n           WHEN a.transaction_type IN (2, 23, 21, 22, 24, 4, 5, 8, 6, 27, 9, 26, 28, 31, 33, 34, 37, 39, 41, 43) AND\n                a.reversal_indicator = false THEN sum(a.amount) * -1\n           WHEN a.transaction_type IN (2, 23, 21, 22, 24, 4, 5, 8, 6, 27, 9, 26, 28, 31, 33, 34, 37, 39, 41, 43) AND\n                a.reversal_indicator = true THEN sum(a.amount) * + 1\n           WHEN a.transaction_type IN (1, 10, 25, 20, 35, 36, 40, 42) AND a.reversal_indicator = false THEN sum(a.amount) * + 1\n           WHEN a.transaction_type IN (1, 10, 25, 20, 35, 36, 40, 42) AND a.reversal_indicator = true\n               THEN sum(a.amount) * -1 END AS Transaction_Amount,\n       (SELECT external_id\n        FROM m_external_asset_owner\n        WHERE id = a.asset_owner_id) AS Asset_owner_id,\n       (SELECT external_id\n        FROM m_external_asset_owner\n        WHERE id = a.from_asset_owner_id) AS From_asset_owner_id,\n       a.originator_external_ids\nFROM (SELECT t.transactiondate,\n             t.id,\n             t.name AS product,\n             t.transaction_type_enum AS transaction_type,\n             t.payment_type_id,\n             t.classification_name,\n             '' AS chargetype,\n             false AS reversal_indicator,\n             'Principal' AS Allocation_Type,\n             CASE\n                 WHEN t.transaction_type_enum in (1) THEN (CASE\n   WHEN t.amount is null THEN 0\n   WHEN t.overpayment_portion_derived is null THEN t.amount\n   WHEN t.overpayment_portion_derived is not null\n       THEN t.amount - t.overpayment_portion_derived\n   ELSE t.amount END)\n                 ELSE (CASE\n       WHEN t.principal_portion_derived is null THEN 0\n       ELSE t.principal_portion_derived end) END amount,\n             CASE\n                 WHEN t.status in ('ACTIVE', 'ACTIVE_INTERMEDIATE') AND t.settlement_date < '${endDate}'\n THEN t.owner_id END AS asset_owner_id,\n             CASE\n                 WHEN t.transaction_type_enum = 27 OR (t.charged_off_on_date <= t.transaction_date)\n THEN t.charge_off_reason_cv_id END AS charge_off_reason_id,\n             null::bigint AS from_asset_owner_id,\n             t.originator_external_ids\n      FROM slt_except_charge_adj_and_accrual AS t\n      UNION ALL\n      SELECT t.transactiondate,\n             t.id,\n             t.name AS product,\n             t.transaction_type_enum AS transaction_type,\n             t.payment_type_id,\n             t.classification_name,\n             '' AS chargetype,\n             false AS reversal_indicator,\n             'Interest' AS Allocation_Type,\n             CASE WHEN t.interest_portion_derived is null THEN 0 ELSE t.interest_portion_derived END AS amount,\n             CASE\n                 WHEN t.status in ('ACTIVE', 'ACTIVE_INTERMEDIATE') AND t.settlement_date < '${endDate}'\n THEN t.owner_id END AS asset_owner_id,\n             CASE\n                 WHEN t.transaction_type_enum = 27 OR (t.charged_off_on_date <= t.transaction_date)\n THEN t.charge_off_reason_cv_id END AS charge_off_reason_id,\n             null::bigint AS from_asset_owner_id,\n             t.originator_external_ids\n      FROM slt_except_charge_adj_and_accrual AS t\n      UNION ALL\n      SELECT t.transactiondate,\n             t.id,\n             t.name AS product,\n             t.transaction_type_enum AS transaction_type,\n             t.payment_type_id,\n             t.classification_name,\n             '' AS chargetype,\n             false AS reversal_indicator,\n             'Fees' AS Allocation_Type,\n             CASE WHEN t.fee_charges_portion_derived is null THEN 0 ELSE t.fee_charges_portion_derived END AS amount,\n             CASE\n                 WHEN t.status in ('ACTIVE', 'ACTIVE_INTERMEDIATE') AND t.settlement_date < '${endDate}'\n THEN t.owner_id END AS asset_owner_id,\n             CASE\n                 WHEN t.transaction_type_enum = 27 OR (t.charged_off_on_date <= t.transaction_date)\n THEN t.charge_off_reason_cv_id END AS charge_off_reason_id,\n             null::bigint AS from_asset_owner_id,\n             t.originator_external_ids\n      FROM slt_except_charge_adj_and_accrual AS t\n      UNION ALL\n      SELECT t.transactiondate,\n             t.id,\n             t.name AS product,\n             t.transaction_type_enum AS transaction_type,\n             t.payment_type_id,\n             t.classification_name,\n             '' AS chargetype,\n             false AS reversal_indicator,\n             'Penalty' AS Allocation_Type,\n             CASE\n                 WHEN t.penalty_charges_portion_derived is null THEN 0\n                 ELSE t.penalty_charges_portion_derived END AS amount,\n             CASE\n                 WHEN t.status in ('ACTIVE', 'ACTIVE_INTERMEDIATE') AND t.settlement_date < '${endDate}'\n THEN t.owner_id END AS asset_owner_id,\n             CASE\n                 WHEN t.transaction_type_enum = 27 OR (t.charged_off_on_date <= t.transaction_date)\n THEN t.charge_off_reason_cv_id END AS charge_off_reason_id,\n             null::bigint AS from_asset_owner_id,\n             t.originator_external_ids\n      FROM slt_except_charge_adj_and_accrual AS t\n      UNION ALL\n      SELECT t.transactiondate,\n             t.id,\n             t.name AS product,\n             t.transaction_type_enum AS transaction_type,\n             t.payment_type_id,\n             t.classification_name,\n             '' AS chargetype,\n             false AS reversal_indicator,\n             'Unallocated Credit (UNC)' AS Allocation_Type,\n             CASE WHEN t.overpayment_portion_derived is null THEN 0 ELSE t.overpayment_portion_derived END AS amount,\n             CASE\n                 WHEN t.status in ('ACTIVE', 'ACTIVE_INTERMEDIATE') AND t.settlement_date < '${endDate}'\n THEN t.owner_id END AS asset_owner_id,\n             CASE\n                 WHEN t.transaction_type_enum = 27 OR (t.charged_off_on_date <= t.transaction_date)\n THEN t.charge_off_reason_cv_id END AS charge_off_reason_id,\n             null::bigint AS from_asset_owner_id,\n             t.originator_external_ids\n      FROM slt_except_charge_adj_and_accrual AS t\n      UNION ALL\n      SELECT '${endDate}' AS transactiondate,\n             t.id,\n             t.name AS product,\n             t.transaction_type_enum AS transaction_type,\n             t.payment_type_id,\n             t.classification_name,\n             '' AS chargetype,\n             false AS reversal_indicator,\n             'Fees' AS Allocation_Type,\n             CASE WHEN t.fee_charges_portion_derived is null THEN 0 ELSE t.fee_charges_portion_derived END AS amount,\n             CASE\n                 WHEN t.status in ('ACTIVE', 'ACTIVE_INTERMEDIATE') AND t.settlement_date < '${endDate}'\n THEN t.owner_id END AS asset_owner_id,\n             CASE\n                 WHEN t.charged_off_on_date <= t.transaction_date\n THEN t.charge_off_reason_cv_id END AS charge_off_reason_id,\n             null::bigint AS from_asset_owner_id,\n             t.originator_external_ids\n      FROM slt_cap_income_amortization AS t\n      UNION ALL\n      SELECT '${endDate}' AS transactiondate,\n             t.id,\n             t.name AS product,\n             t.transaction_type_enum AS transaction_type,\n             t.payment_type_id,\n             t.classification_name,\n             '' AS chargetype,\n             false AS reversal_indicator,\n             'Penalty' AS Allocation_Type,\n             CASE\n                 WHEN t.penalty_charges_portion_derived is null THEN 0\n                 ELSE t.penalty_charges_portion_derived END AS amount,\n             CASE\n                 WHEN t.status in ('ACTIVE', 'ACTIVE_INTERMEDIATE') AND t.settlement_date < '${endDate}'\n THEN t.owner_id END AS asset_owner_id,\n             CASE\n                 WHEN t.charged_off_on_date <= t.transaction_date\n THEN t.charge_off_reason_cv_id END AS charge_off_reason_id,\n             null::bigint AS from_asset_owner_id,\n             t.originator_external_ids\n      FROM slt_cap_income_amortization AS t\n      UNION ALL\n      SELECT '${endDate}' AS transactiondate,\n             t.id,\n             t.name AS product,\n             t.transaction_type_enum AS transaction_type,\n             t.payment_type_id,\n             t.classification_name,\n             '' AS chargetype,\n             false AS reversal_indicator,\n             'Interest' AS Allocation_Type,\n             CASE WHEN t.interest_portion_derived is null THEN 0 ELSE t.interest_portion_derived END AS amount,\n             CASE\n                 WHEN t.status in ('ACTIVE', 'ACTIVE_INTERMEDIATE') AND t.settlement_date < '${endDate}'\n THEN t.owner_id END AS asset_owner_id,\n             CASE\n                 WHEN t.charged_off_on_date <= t.transaction_date\n THEN t.charge_off_reason_cv_id END AS charge_off_reason_id,\n             null::bigint AS from_asset_owner_id,\n             t.originator_external_ids\n      FROM slt_cap_income_amortization AS t\n      UNION ALL\n      SELECT '${endDate}' AS transactiondate,\n             t.id,\n             t.name AS product,\n             t.transaction_type_enum AS transaction_type,\n             t.payment_type_id,\n             t.classification_name,\n             '' AS chargetype,\n             false AS reversal_indicator,\n             'Principal' AS Allocation_Type,\n             CASE WHEN t.principal_portion_derived is null THEN 0 ELSE t.principal_portion_derived END AS amount,\n             CASE\n                 WHEN t.status in ('ACTIVE', 'ACTIVE_INTERMEDIATE') AND t.settlement_date < '${endDate}'\n THEN t.owner_id END AS asset_owner_id,\n             CASE\n                 WHEN t.charged_off_on_date <= t.transaction_date\n THEN t.charge_off_reason_cv_id END AS charge_off_reason_id,\n             null::bigint AS from_asset_owner_id,\n             t.originator_external_ids\n      FROM slt_cap_income_amortization AS t\n      UNION ALL\n      SELECT '${endDate}' AS transactiondate,\n             t.id,\n             t.name AS product,\n             t.transaction_type_enum AS transaction_type,\n             t.payment_type_id,\n             t.classification_name,\n             '' AS chargetype,\n             false AS reversal_indicator,\n             'Unallocated Credit (UNC)' AS Allocation_Type,\n             CASE WHEN t.overpayment_portion_derived is null THEN 0 ELSE t.overpayment_portion_derived END AS amount,\n             CASE\n                 WHEN t.status in ('ACTIVE', 'ACTIVE_INTERMEDIATE') AND t.settlement_date < '${endDate}'\n THEN t.owner_id END AS asset_owner_id,\n             CASE\n                 WHEN t.charged_off_on_date <= t.transaction_date\n THEN t.charge_off_reason_cv_id END AS charge_off_reason_id,\n             null::bigint AS from_asset_owner_id,\n             t.originator_external_ids\n      FROM slt_cap_income_amortization AS t\n      UNION ALL\n      SELECT '${endDate}' AS transactiondate,\n             t.id,\n             t.name AS product,\n             t.transaction_type_enum AS transaction_type,\n             t.payment_type_id,\n             t.classification_name,\n             '' AS chargetype,\n             false AS reversal_indicator,\n             'Fees' AS Allocation_Type,\n             CASE WHEN t.fee_charges_portion_derived is null THEN 0 ELSE t.fee_charges_portion_derived END AS amount,\n             CASE\n                 WHEN t.status in ('ACTIVE', 'ACTIVE_INTERMEDIATE') AND t.settlement_date < '${endDate}'\n THEN t.owner_id END AS asset_owner_id,\n             CASE\n                 WHEN t.charged_off_on_date <= t.transaction_date\n THEN t.charge_off_reason_cv_id END AS charge_off_reason_id,\n             null::bigint AS from_asset_owner_id,\n             t.originator_external_ids\n      FROM rlt_cap_income_amortization AS t\n      UNION ALL\n      SELECT '${endDate}' AS transactiondate,\n             t.id,\n             t.name AS product,\n             t.transaction_type_enum AS transaction_type,\n             t.payment_type_id,\n             t.classification_name,\n             '' AS chargetype,\n             false AS reversal_indicator,\n             'Penalty' AS Allocation_Type,\n             CASE\n                 WHEN t.penalty_charges_portion_derived is null THEN 0\n                 ELSE t.penalty_charges_portion_derived END AS amount,\n             CASE\n                 WHEN t.status in ('ACTIVE', 'ACTIVE_INTERMEDIATE') AND t.settlement_date < '${endDate}'\n THEN t.owner_id END AS asset_owner_id,\n             CASE\n                 WHEN t.charged_off_on_date <= t.transaction_date\n THEN t.charge_off_reason_cv_id END AS charge_off_reason_id,\n             null::bigint AS from_asset_owner_id,\n             t.originator_external_ids\n      FROM rlt_cap_income_amortization AS t\n      UNION ALL\n      SELECT '${endDate}' AS transactiondate,\n             t.id,\n             t.name AS product,\n             t.transaction_type_enum AS transaction_type,\n             t.payment_type_id,\n             t.classification_name,\n             '' AS chargetype,\n             false AS reversal_indicator,\n             'Interest' AS Allocation_Type,\n             CASE WHEN t.interest_portion_derived is null THEN 0 ELSE t.interest_portion_derived END AS amount,\n             CASE\n                 WHEN t.status in ('ACTIVE', 'ACTIVE_INTERMEDIATE') AND t.settlement_date < '${endDate}'\n THEN t.owner_id END AS asset_owner_id,\n             CASE\n                 WHEN t.charged_off_on_date <= t.transaction_date\n THEN t.charge_off_reason_cv_id END AS charge_off_reason_id,\n             null::bigint AS from_asset_owner_id,\n             t.originator_external_ids\n      FROM rlt_cap_income_amortization AS t\n      UNION ALL\n      SELECT '${endDate}' AS transactiondate,\n             t.id,\n             t.name AS product,\n             t.transaction_type_enum AS transaction_type,\n             t.payment_type_id,\n             t.classification_name,\n             '' AS chargetype,\n             false AS reversal_indicator,\n             'Principal' AS Allocation_Type,\n             CASE WHEN t.principal_portion_derived is null THEN 0 ELSE t.principal_portion_derived END AS amount,\n             CASE\n                 WHEN t.status in ('ACTIVE', 'ACTIVE_INTERMEDIATE') AND t.settlement_date < '${endDate}'\n THEN t.owner_id END AS asset_owner_id,\n             CASE\n                 WHEN t.charged_off_on_date <= t.transaction_date\n THEN t.charge_off_reason_cv_id END AS charge_off_reason_id,\n             null::bigint AS from_asset_owner_id,\n             t.originator_external_ids\n      FROM rlt_cap_income_amortization AS t\n      UNION ALL\n      SELECT '${endDate}' AS transactiondate,\n             t.id,\n             t.name AS product,\n             t.transaction_type_enum AS transaction_type,\n             t.payment_type_id,\n             t.classification_name,\n             '' AS chargetype,\n             false AS reversal_indicator,\n             'Unallocated Credit (UNC)' AS Allocation_Type,\n             CASE WHEN t.overpayment_portion_derived is null THEN 0 ELSE t.overpayment_portion_derived END AS amount,\n             CASE\n                 WHEN t.status in ('ACTIVE', 'ACTIVE_INTERMEDIATE') AND t.settlement_date < '${endDate}'\n THEN t.owner_id END AS asset_owner_id,\n             CASE\n                 WHEN t.charged_off_on_date <= t.transaction_date\n THEN t.charge_off_reason_cv_id END AS charge_off_reason_id,\n             null::bigint AS from_asset_owner_id,\n             t.originator_external_ids\n      FROM rlt_cap_income_amortization AS t\n      UNION ALL\n      SELECT '${endDate}' AS transactiondate,\n             t.id,\n             l.name AS product,\n             t.transaction_type_enum AS transaction_type,\n             null AS payment_type_id,\n             null AS classification_name,\n             mc.name AS chargetype,\n             false AS reversal_indicator,\n             'Fees' AS Allocation_Type,\n             CASE WHEN pd.amount is null THEN 0 ELSE pd.amount END AS amount,\n             CASE\n                 WHEN e.status in ('ACTIVE', 'ACTIVE_INTERMEDIATE') AND e.settlement_date < '${endDate}'\n THEN e.owner_id END AS asset_owner_id,\n             CASE\n                 WHEN t.transaction_type_enum = 27 OR (m.charged_off_on_date <= t.transaction_date)\n THEN m.charge_off_reason_cv_id END AS charge_off_reason_id,\n             null::bigint AS from_asset_owner_id,\n             (SELECT STRING_AGG(DISTINCT mlo.external_id, ', ' ORDER BY mlo.external_id) FROM m_loan_originator_mapping mlom JOIN m_loan_originator mlo ON mlo.id = mlom.originator_id WHERE mlom.loan_id = t.loan_id) AS originator_external_ids\n      FROM m_loan_transaction t\n               JOIN m_loan m ON m.id = t.loan_id\n               JOIN m_product_loan l ON l.id = m.product_id\n               JOIN m_loan_charge_paid_by pd ON pd.loan_transaction_id = t.id\n               JOIN m_loan_charge c ON c.id = pd.loan_charge_id\n               JOIN m_charge mc ON mc.id = c.charge_id AND mc.is_penalty = false\n               LEFT JOIN m_external_asset_owner_transfer e\n     ON e.loan_id = t.loan_id AND e.settlement_date < '${endDate}' AND\n        e.effective_date_to >= '${endDate}'\n      WHERE t.submitted_on_date = '${endDate}'\n        AND t.transaction_type_enum = 10\n        AND t.is_reversed = false\n        AND (t.office_id = ${officeId})\n      UNION ALL\n      SELECT '${endDate}' AS transactiondate,\n             t.id,\n             l.name AS product,\n             t.transaction_type_enum AS transaction_type,\n             null AS payment_type_id,\n             null AS classification_name,\n             mc.name AS chargetype,\n             false AS reversal_indicator,\n             'Penalty' AS Allocation_Type,\n             CASE WHEN pd.amount is null THEN 0 ELSE pd.amount END AS amount,\n             CASE\n                 WHEN e.status in ('ACTIVE', 'ACTIVE_INTERMEDIATE') AND e.settlement_date < '${endDate}'\n THEN e.owner_id END AS asset_owner_id,\n             CASE\n                 WHEN t.transaction_type_enum = 27 OR (m.charged_off_on_date <= t.transaction_date)\n THEN m.charge_off_reason_cv_id END AS charge_off_reason_id,\n             null::bigint AS from_asset_owner_id,\n             (SELECT STRING_AGG(DISTINCT mlo.external_id, ', ' ORDER BY mlo.external_id) FROM m_loan_originator_mapping mlom JOIN m_loan_originator mlo ON mlo.id = mlom.originator_id WHERE mlom.loan_id = t.loan_id) AS originator_external_ids\n      FROM m_loan_transaction t\n               JOIN m_loan m ON m.id = t.loan_id\n               JOIN m_product_loan l ON l.id = m.product_id\n               JOIN m_loan_charge_paid_by pd ON pd.loan_transaction_id = t.id\n               JOIN m_loan_charge c ON c.id = pd.loan_charge_id\n               JOIN m_charge mc ON mc.id = c.charge_id AND mc.is_penalty = true\n               LEFT JOIN m_external_asset_owner_transfer e\n     ON e.loan_id = t.loan_id AND e.settlement_date < '${endDate}' AND\n        e.effective_date_to >= '${endDate}'\n      WHERE t.submitted_on_date = '${endDate}'\n        AND t.transaction_type_enum = 10\n        AND t.is_reversed = false\n        AND (t.office_id = ${officeId})\n      UNION ALL\n      SELECT '${endDate}' AS transactiondate,\n             t.id,\n             l.name AS product,\n             t.transaction_type_enum AS transaction_type,\n             null AS payment_type_id,\n             null AS classification_name,\n             '' AS chargetype,\n             false AS reversal_indicator,\n             'Interest' AS Allocation_Type,\n             CASE WHEN t.interest_portion_derived is null THEN 0 ELSE t.interest_portion_derived END AS amount,\n             CASE\n                 WHEN e.status in ('ACTIVE', 'ACTIVE_INTERMEDIATE') AND e.settlement_date < '${endDate}'\n THEN e.owner_id END AS asset_owner_id,\n             CASE\n                 WHEN t.transaction_type_enum = 27 OR (m.charged_off_on_date <= t.transaction_date)\n THEN m.charge_off_reason_cv_id END AS charge_off_reason_id,\n             null::bigint AS from_asset_owner_id,\n             (SELECT STRING_AGG(DISTINCT mlo.external_id, ', ' ORDER BY mlo.external_id) FROM m_loan_originator_mapping mlom JOIN m_loan_originator mlo ON mlo.id = mlom.originator_id WHERE mlom.loan_id = t.loan_id) AS originator_external_ids\n      FROM m_loan_transaction t\n               JOIN m_loan m ON m.id = t.loan_id\n               JOIN m_product_loan l ON l.id = m.product_id\n               LEFT JOIN m_external_asset_owner_transfer e\n     ON e.loan_id = t.loan_id AND e.settlement_date < '${endDate}' AND\n        e.effective_date_to >= '${endDate}'\n      WHERE t.submitted_on_date = '${endDate}'\n        AND t.transaction_type_enum in (10, 34)\n        AND t.is_reversed = false\n        AND (t.office_id = ${officeId})\n      UNION ALL\n      SELECT '${endDate}' AS transactiondate,\n             t.id,\n             t.name AS product,\n             t.transaction_type_enum AS transaction_type,\n             t.payment_type_id,\n             null AS classification_name,\n             '' AS chargetype,\n             false AS reversal_indicator,\n             'Fees' AS Allocation_Type,\n             CASE WHEN t.fee_charges_portion_derived is null THEN 0 ELSE t.fee_charges_portion_derived END AS amount,\n             CASE\n                 WHEN t.status in ('ACTIVE', 'ACTIVE_INTERMEDIATE') AND t.settlement_date < '${endDate}'\n THEN t.owner_id END AS asset_owner_id,\n             CASE\n                 WHEN t.transaction_type_enum = 27 OR (t.charged_off_on_date <= t.transaction_date)\n THEN t.charge_off_reason_cv_id END AS charge_off_reason_id,\n             null::bigint AS from_asset_owner_id,\n             t.originator_external_ids\n      FROM slt_charge_adj AS t\n      UNION ALL\n      SELECT '${endDate}' AS transactiondate,\n             t.id,\n             t.name AS product,\n             t.transaction_type_enum AS transaction_type,\n             t.payment_type_id,\n             null AS classification_name,\n             '' AS chargetype,\n             false AS reversal_indicator,\n             'Penalty' AS Allocation_Type,\n             CASE\n                 WHEN t.penalty_charges_portion_derived is null THEN 0\n                 ELSE t.penalty_charges_portion_derived END AS amount,\n             CASE\n                 WHEN t.status in ('ACTIVE', 'ACTIVE_INTERMEDIATE') AND t.settlement_date < '${endDate}'\n THEN t.owner_id END AS asset_owner_id,\n             CASE\n                 WHEN t.transaction_type_enum = 27 OR (t.charged_off_on_date <= t.transaction_date)\n THEN t.charge_off_reason_cv_id END AS charge_off_reason_id,\n             null::bigint AS from_asset_owner_id,\n             t.originator_external_ids\n      FROM slt_charge_adj AS t\n      UNION ALL\n      SELECT '${endDate}' AS transactiondate,\n             t.id,\n             t.name AS product,\n             t.transaction_type_enum AS transaction_type,\n             t.payment_type_id,\n             null AS classification_name,\n             '' AS chargetype,\n             false AS reversal_indicator,\n             'Interest' AS Allocation_Type,\n             CASE WHEN t.interest_portion_derived is null THEN 0 ELSE t.interest_portion_derived END AS amount,\n             CASE\n                 WHEN t.status in ('ACTIVE', 'ACTIVE_INTERMEDIATE') AND t.settlement_date < '${endDate}'\n THEN t.owner_id END AS asset_owner_id,\n             CASE\n                 WHEN t.transaction_type_enum = 27 OR (t.charged_off_on_date <= t.transaction_date)\n THEN t.charge_off_reason_cv_id END AS charge_off_reason_id,\n             null::bigint AS from_asset_owner_id,\n             t.originator_external_ids\n      FROM slt_charge_adj AS t\n      UNION ALL\n      SELECT '${endDate}' AS transactiondate,\n             t.id,\n             t.name AS product,\n             t.transaction_type_enum AS transaction_type,\n             t.payment_type_id,\n             null AS classification_name,\n             '' AS chargetype,\n             false AS reversal_indicator,\n             'Principal' AS Allocation_Type,\n             CASE WHEN t.principal_portion_derived is null THEN 0 ELSE t.principal_portion_derived END AS amount,\n             CASE\n                 WHEN t.status in ('ACTIVE', 'ACTIVE_INTERMEDIATE') AND t.settlement_date < '${endDate}'\n THEN t.owner_id END AS asset_owner_id,\n             CASE\n                 WHEN t.transaction_type_enum = 27 OR (t.charged_off_on_date <= t.transaction_date)\n THEN t.charge_off_reason_cv_id END AS charge_off_reason_id,\n             null::bigint AS from_asset_owner_id,\n             t.originator_external_ids\n      FROM slt_charge_adj AS t\n      UNION ALL\n      SELECT '${endDate}' AS transactiondate,\n             t.id,\n             t.name AS product,\n             t.transaction_type_enum AS transaction_type,\n             t.payment_type_id,\n             null AS classification_name,\n             '' AS chargetype,\n             false AS reversal_indicator,\n             'Unallocated Credit (UNC)' AS Allocation_Type,\n             CASE WHEN t.overpayment_portion_derived is null THEN 0 ELSE t.overpayment_portion_derived END AS amount,\n             CASE\n                 WHEN t.status in ('ACTIVE', 'ACTIVE_INTERMEDIATE') AND t.settlement_date < '${endDate}'\n THEN t.owner_id END AS asset_owner_id,\n             CASE\n                 WHEN t.transaction_type_enum = 27 OR (t.charged_off_on_date <= t.transaction_date)\n THEN t.charge_off_reason_cv_id END AS charge_off_reason_id,\n             null::bigint AS from_asset_owner_id,\n             t.originator_external_ids\n      FROM slt_charge_adj AS t\n      UNION ALL\n      SELECT '${endDate}' AS transactiondate,\n             t.id,\n             t.name AS product,\n             t.transaction_type_enum AS transaction_type,\n             t.payment_type_id,\n             t.classification_name,\n             '' AS chargetype,\n             true AS reversal_indicator,\n             'Principal' AS Allocation_Type,\n             CASE\n                 WHEN t.transaction_type_enum in (1) THEN (CASE\n   WHEN t.amount is null THEN 0\n   WHEN t.overpayment_portion_derived is null THEN t.amount\n   WHEN t.overpayment_portion_derived is not null\n       THEN t.amount - t.overpayment_portion_derived\n   ELSE t.amount END)\n                 ELSE (CASE\n       WHEN t.principal_portion_derived is null THEN 0\n       ELSE t.principal_portion_derived end) END amount,\n             CASE\n                 WHEN t.status in ('ACTIVE', 'ACTIVE_INTERMEDIATE') AND t.settlement_date < '${endDate}'\n THEN t.owner_id END AS asset_owner_id,\n             CASE\n                 WHEN t.transaction_type_enum = 27 OR (t.charged_off_on_date <= t.transaction_date)\n THEN t.charge_off_reason_cv_id END AS charge_off_reason_id,\n             null::bigint AS from_asset_owner_id,\n             t.originator_external_ids\n      FROM rlt_except_charge_adj_and_accrual AS t\n      UNION ALL\n      SELECT '${endDate}' AS transactiondate,\n             t.id,\n             t.name AS product,\n             t.transaction_type_enum AS transaction_type,\n             t.payment_type_id,\n             t.classification_name,\n             '' AS chargetype,\n             true AS reversal_indicator,\n             'Interest' AS Allocation_Type,\n             CASE WHEN t.interest_portion_derived is null THEN 0 ELSE t.interest_portion_derived END AS amount,\n             CASE\n                 WHEN t.status in ('ACTIVE', 'ACTIVE_INTERMEDIATE') AND t.settlement_date < '${endDate}'\n THEN t.owner_id END AS asset_owner_id,\n             CASE\n                 WHEN t.transaction_type_enum = 27 OR (t.charged_off_on_date <= t.transaction_date)\n THEN t.charge_off_reason_cv_id END AS charge_off_reason_id,\n             null::bigint AS from_asset_owner_id,\n             t.originator_external_ids\n      FROM rlt_except_charge_adj_and_accrual AS t\n      UNION ALL\n      SELECT '${endDate}' AS transactiondate,\n             t.id,\n             t.name AS product,\n             t.transaction_type_enum AS transaction_type,\n             t.payment_type_id,\n             t.classification_name,\n             '' AS chargetype,\n             true AS reversal_indicator,\n             'Fees' AS Allocation_Type,\n             CASE WHEN t.fee_charges_portion_derived is null THEN 0 ELSE t.fee_charges_portion_derived END AS amount,\n             CASE\n                 WHEN t.status in ('ACTIVE', 'ACTIVE_INTERMEDIATE') AND t.settlement_date < '${endDate}'\n THEN t.owner_id END AS asset_owner_id,\n             CASE\n                 WHEN t.transaction_type_enum = 27 OR (t.charged_off_on_date <= t.transaction_date)\n THEN t.charge_off_reason_cv_id END AS charge_off_reason_id,\n             null::bigint AS from_asset_owner_id,\n             t.originator_external_ids\n      FROM rlt_except_charge_adj_and_accrual AS t\n      UNION ALL\n      SELECT '${endDate}' AS transactiondate,\n             t.id,\n             t.name AS product,\n             t.transaction_type_enum AS transaction_type,\n             t.payment_type_id,\n             t.classification_name,\n             '' AS chargetype,\n             true AS reversal_indicator,\n             'Penalty' AS Allocation_Type,\n             CASE\n                 WHEN t.penalty_charges_portion_derived is null THEN 0\n                 ELSE t.penalty_charges_portion_derived END AS amount,\n             CASE\n                 WHEN t.status in ('ACTIVE', 'ACTIVE_INTERMEDIATE') AND t.settlement_date < '${endDate}'\n THEN t.owner_id END AS asset_owner_id,\n             CASE\n                 WHEN t.transaction_type_enum = 27 OR (t.charged_off_on_date <= t.transaction_date)\n THEN t.charge_off_reason_cv_id END AS charge_off_reason_id,\n             null::bigint AS from_asset_owner_id,\n             t.originator_external_ids\n      FROM rlt_except_charge_adj_and_accrual AS t\n      UNION ALL\n      SELECT '${endDate}' AS transactiondate,\n             t.id,\n             t.name AS product,\n             t.transaction_type_enum AS transaction_type,\n             t.payment_type_id,\n             t.classification_name,\n             '' AS chargetype,\n             true AS reversal_indicator,\n             'Unallocated Credit (UNC)' AS Allocation_Type,\n             CASE WHEN t.overpayment_portion_derived is null THEN 0 ELSE t.overpayment_portion_derived END AS amount,\n             CASE\n                 WHEN t.status in ('ACTIVE', 'ACTIVE_INTERMEDIATE') AND t.settlement_date < '${endDate}'\n THEN t.owner_id END AS asset_owner_id,\n             CASE\n                 WHEN t.transaction_type_enum = 27 OR (t.charged_off_on_date <= t.transaction_date)\n THEN t.charge_off_reason_cv_id END AS charge_off_reason_id,\n             null::bigint AS from_asset_owner_id,\n             t.originator_external_ids\n      FROM rlt_except_charge_adj_and_accrual AS t\n      UNION ALL\n      SELECT '${endDate}' AS transactiondate,\n             t.id,\n             l.name AS product,\n             t.transaction_type_enum AS transaction_type,\n             null AS payment_type_id,\n             null AS classification_name,\n             mc.name AS chargetype,\n             true AS reversal_indicator,\n             'Fees' AS Allocation_Type,\n             CASE WHEN pd.amount is null THEN 0 ELSE pd.amount END AS amount,\n             CASE\n                 WHEN e.status in ('ACTIVE', 'ACTIVE_INTERMEDIATE') AND e.settlement_date < '${endDate}'\n THEN e.owner_id END AS asset_owner_id,\n             CASE\n                 WHEN t.transaction_type_enum = 27 OR (m.charged_off_on_date <= t.transaction_date)\n THEN m.charge_off_reason_cv_id END AS charge_off_reason_id,\n             null::bigint AS from_asset_owner_id,\n             (SELECT STRING_AGG(DISTINCT mlo.external_id, ', ' ORDER BY mlo.external_id) FROM m_loan_originator_mapping mlom JOIN m_loan_originator mlo ON mlo.id = mlom.originator_id WHERE mlom.loan_id = t.loan_id) AS originator_external_ids\n      FROM m_loan_transaction t\n               JOIN m_loan m ON m.id = t.loan_id\n               JOIN m_product_loan l ON l.id = m.product_id\n               JOIN m_loan_charge_paid_by pd ON pd.loan_transaction_id = t.id\n               JOIN m_loan_charge c ON c.id = pd.loan_charge_id\n               JOIN m_charge mc ON mc.id = c.charge_id AND mc.is_penalty = false\n               LEFT JOIN m_external_asset_owner_transfer e\n     ON e.loan_id = t.loan_id AND e.settlement_date < '${endDate}' AND\n        e.effective_date_to >= '${endDate}'\n      WHERE t.reversed_on_date = '${endDate}'\n        AND t.transaction_type_enum = 10\n        AND t.is_reversed = true\n        AND (t.office_id = ${officeId})\n      UNION ALL\n      SELECT '${endDate}' AS transactiondate,\n             t.id,\n             l.name AS product,\n             t.transaction_type_enum AS transaction_type,\n             null AS payment_type_id,\n             null AS classification_name,\n             mc.name AS chargetype,\n             true AS reversal_indicator,\n             'Penalty' AS Allocation_Type,\n             CASE WHEN pd.amount is null THEN 0 ELSE pd.amount END AS amount,\n             CASE\n                 WHEN e.status in ('ACTIVE', 'ACTIVE_INTERMEDIATE') AND e.settlement_date < '${endDate}'\n THEN e.owner_id END AS asset_owner_id,\n             CASE\n                 WHEN t.transaction_type_enum = 27 OR (m.charged_off_on_date <= t.transaction_date)\n THEN m.charge_off_reason_cv_id END AS charge_off_reason_id,\n             null::bigint AS from_asset_owner_id,\n             (SELECT STRING_AGG(DISTINCT mlo.external_id, ', ' ORDER BY mlo.external_id) FROM m_loan_originator_mapping mlom JOIN m_loan_originator mlo ON mlo.id = mlom.originator_id WHERE mlom.loan_id = t.loan_id) AS originator_external_ids\n      FROM m_loan_transaction t\n               JOIN m_loan m ON m.id = t.loan_id\n               JOIN m_product_loan l ON l.id = m.product_id\n               JOIN m_loan_charge_paid_by pd ON pd.loan_transaction_id = t.id\n               JOIN m_loan_charge c ON c.id = pd.loan_charge_id\n               JOIN m_charge mc ON mc.id = c.charge_id AND mc.is_penalty = true\n               LEFT JOIN m_external_asset_owner_transfer e\n     ON e.loan_id = t.loan_id AND e.settlement_date < '${endDate}' AND\n        e.effective_date_to >= '${endDate}'\n      WHERE t.reversed_on_date = '${endDate}'\n        AND t.transaction_type_enum = 10\n        AND t.is_reversed = true\n        AND (t.office_id = ${officeId})\n      UNION ALL\n      SELECT '${endDate}' AS transactiondate,\n             t.id,\n             l.name AS product,\n             t.transaction_type_enum AS transaction_type,\n             null AS payment_type_id,\n             null AS classification_name,\n             '' AS chargetype,\n             true AS reversal_indicator,\n             'Interest' AS Allocation_Type,\n             CASE WHEN t.interest_portion_derived is null THEN 0 ELSE t.interest_portion_derived END AS amount,\n             CASE\n                 WHEN e.status in ('ACTIVE', 'ACTIVE_INTERMEDIATE') AND e.settlement_date < '${endDate}'\n THEN e.owner_id END AS asset_owner_id,\n             CASE\n                 WHEN t.transaction_type_enum = 27 OR (m.charged_off_on_date <= t.transaction_date)\n THEN m.charge_off_reason_cv_id END AS charge_off_reason_id,\n             null::bigint AS from_asset_owner_id,\n             (SELECT STRING_AGG(DISTINCT mlo.external_id, ', ' ORDER BY mlo.external_id) FROM m_loan_originator_mapping mlom JOIN m_loan_originator mlo ON mlo.id = mlom.originator_id WHERE mlom.loan_id = t.loan_id) AS originator_external_ids\n      FROM m_loan_transaction t\n               JOIN m_loan m ON m.id = t.loan_id\n               JOIN m_product_loan l ON l.id = m.product_id\n               LEFT JOIN m_external_asset_owner_transfer e\n     ON e.loan_id = t.loan_id AND e.settlement_date < '${endDate}' AND\n        e.effective_date_to >= '${endDate}'\n      WHERE t.reversed_on_date = '${endDate}'\n        AND t.transaction_type_enum = 10\n        AND t.is_reversed = true\n        AND (t.office_id = ${officeId})\n      UNION ALL\n      SELECT '${endDate}' AS transactiondate,\n             t.id,\n             t.name AS product,\n             t.transaction_type_enum AS transaction_type,\n             t.payment_type_id,\n             null AS classification_name,\n             '' AS chargetype,\n             true AS reversal_indicator,\n             'Fees' AS Allocation_Type,\n             CASE WHEN t.fee_charges_portion_derived is null THEN 0 ELSE t.fee_charges_portion_derived END AS amount,\n             CASE\n                 WHEN t.status in ('ACTIVE', 'ACTIVE_INTERMEDIATE') AND t.settlement_date < '${endDate}'\n THEN t.owner_id END AS asset_owner_id,\n             CASE\n                 WHEN t.transaction_type_enum = 27 OR (t.charged_off_on_date <= t.transaction_date)\n THEN t.charge_off_reason_cv_id END AS charge_off_reason_id,\n             null::bigint AS from_asset_owner_id,\n             t.originator_external_ids\n      FROM rlt_charge_adj AS t\n      UNION ALL\n      SELECT '${endDate}' AS transactiondate,\n             t.id,\n             t.name AS product,\n             t.transaction_type_enum AS transaction_type,\n             t.payment_type_id,\n             null AS classification_name,\n             '' AS chargetype,\n             true AS reversal_indicator,\n             'Penalty' AS Allocation_Type,\n             CASE\n                 WHEN t.penalty_charges_portion_derived is null THEN 0\n                 ELSE t.penalty_charges_portion_derived END AS amount,\n             CASE\n                 WHEN t.status in ('ACTIVE', 'ACTIVE_INTERMEDIATE') AND t.settlement_date < '${endDate}'\n THEN t.owner_id END AS asset_owner_id,\n             CASE\n                 WHEN t.transaction_type_enum = 27 OR (t.charged_off_on_date <= t.transaction_date)\n THEN t.charge_off_reason_cv_id END AS charge_off_reason_id,\n             null::bigint AS from_asset_owner_id,\n             t.originator_external_ids\n      FROM rlt_charge_adj AS t\n      UNION ALL\n      SELECT '${endDate}' AS transactiondate,\n             t.id,\n             t.name AS product,\n             t.transaction_type_enum AS transaction_type,\n             t.payment_type_id,\n             null AS classification_name,\n             '' AS chargetype,\n             true AS reversal_indicator,\n             'Interest' AS Allocation_Type,\n             CASE WHEN t.interest_portion_derived is null THEN 0 ELSE t.interest_portion_derived END AS amount,\n             CASE\n                 WHEN t.status in ('ACTIVE', 'ACTIVE_INTERMEDIATE') AND t.settlement_date < '${endDate}'\n THEN t.owner_id END AS asset_owner_id,\n             CASE\n                 WHEN t.transaction_type_enum = 27 OR (t.charged_off_on_date <= t.transaction_date)\n THEN t.charge_off_reason_cv_id END AS charge_off_reason_id,\n             null::bigint AS from_asset_owner_id,\n             t.originator_external_ids\n      FROM rlt_charge_adj AS t\n      UNION ALL\n      SELECT '${endDate}' AS transactiondate,\n             t.id,\n             t.name AS product,\n             t.transaction_type_enum AS transaction_type,\n             t.payment_type_id,\n             null AS classification_name,\n             '' AS chargetype,\n             true AS reversal_indicator,\n             'Principal' AS Allocation_Type,\n             CASE WHEN t.principal_portion_derived is null THEN 0 ELSE t.principal_portion_derived END AS amount,\n             CASE\n                 WHEN t.status in ('ACTIVE', 'ACTIVE_INTERMEDIATE') AND t.settlement_date < '${endDate}'\n THEN t.owner_id END AS asset_owner_id,\n             CASE\n                 WHEN t.transaction_type_enum = 27 OR (t.charged_off_on_date <= t.transaction_date)\n THEN t.charge_off_reason_cv_id END AS charge_off_reason_id,\n             null::bigint AS from_asset_owner_id,\n             t.originator_external_ids\n      FROM rlt_charge_adj AS t\n      UNION ALL\n      SELECT '${endDate}' AS transactiondate,\n             t.id,\n             t.name AS product,\n             t.transaction_type_enum AS transaction_type,\n             t.payment_type_id,\n             null AS classification_name,\n             '' AS chargetype,\n             true AS reversal_indicator,\n             'Unallocated Credit (UNC)' AS Allocation_Type,\n             CASE WHEN t.overpayment_portion_derived is null THEN 0 ELSE t.overpayment_portion_derived END AS amount,\n             CASE\n                 WHEN t.status in ('ACTIVE', 'ACTIVE_INTERMEDIATE') AND t.settlement_date < '${endDate}'\n THEN t.owner_id END AS asset_owner_id,\n             CASE\n                 WHEN t.transaction_type_enum = 27 OR (t.charged_off_on_date <= t.transaction_date)\n THEN t.charge_off_reason_cv_id END AS charge_off_reason_id,\n             null::bigint AS from_asset_owner_id,\n             t.originator_external_ids\n      FROM rlt_charge_adj AS t\n      UNION ALL\n      SELECT t.transactiondate,\n             t.id,\n             t.name AS product,\n             9999 AS transaction_type,\n             null AS payment_type_id,\n             null AS classification_name,\n             '' AS chargetype,\n             false AS reversal_indicator,\n             'Principal' AS Allocation_type,\n             t.principal_outstanding_derived AS amount,\n             t.owner_id AS asset_owner_id,\n             CASE\n                 WHEN t.charged_off_on_date <= t.settlement_date\n THEN t.charge_off_reason_cv_id END AS charge_off_reason_id,\n             t.previous_owner_id AS from_asset_owner_id,\n             t.originator_external_ids\n      FROM active_external_asset_owner_transfers AS t\n      WHERE t.principal_outstanding_derived > 0\n      UNION ALL\n      SELECT t.transactiondate,\n             t.id,\n             t.name AS product,\n             9999 AS transaction_type,\n             null AS payment_type_id,\n             null AS classification_name,\n             '' AS chargetype,\n             false AS reversal_indicator,\n             'Interest' AS Allocation_type,\n             t.interest_outstanding_derived AS amount,\n             t.owner_id AS asset_owner_id,\n             CASE\n                 WHEN t.charged_off_on_date <= t.settlement_date\n THEN t.charge_off_reason_cv_id END AS charge_off_reason_id,\n             t.previous_owner_id AS from_asset_owner_id,\n             t.originator_external_ids\n      FROM active_external_asset_owner_transfers AS t\n      WHERE t.interest_outstanding_derived > 0\n      UNION ALL\n      SELECT t.transactiondate,\n             t.id,\n             t.name AS product,\n             9999 AS transaction_type,\n             null AS payment_type_id,\n             null AS classification_name,\n             '' AS chargetype,\n             false AS reversal_indicator,\n             'Fees' AS Allocation_type,\n             t.fee_charges_outstanding_derived AS amount,\n             t.owner_id AS asset_owner_id,\n             CASE\n                 WHEN t.charged_off_on_date <= t.settlement_date\n THEN t.charge_off_reason_cv_id END AS charge_off_reason_id,\n             t.previous_owner_id AS from_asset_owner_id,\n             t.originator_external_ids\n      FROM active_external_asset_owner_transfers AS t\n      WHERE t.fee_charges_outstanding_derived > 0\n      UNION ALL\n      SELECT t.transactiondate,\n             t.id,\n             t.name AS product,\n             9999 AS transaction_type,\n             null AS payment_type_id,\n             null AS classification_name,\n             '' AS chargetype,\n             false AS reversal_indicator,\n             'Penalty' AS Allocation_type,\n             t.penalty_charges_outstanding_derived AS amount,\n             t.owner_id AS asset_owner_id,\n             CASE\n                 WHEN t.charged_off_on_date <= t.settlement_date\n THEN t.charge_off_reason_cv_id END AS charge_off_reason_id,\n             t.previous_owner_id AS from_asset_owner_id,\n             t.originator_external_ids\n      FROM active_external_asset_owner_transfers AS t\n      WHERE t.penalty_charges_outstanding_derived > 0\n      UNION ALL\n      SELECT t.transactiondate,\n             t.id,\n             t.name AS product,\n             9999 AS transaction_type,\n             null AS payment_type_id,\n             null AS classification_name,\n             '' AS chargetype,\n             false AS reversal_indicator,\n             'Unallocated Credit (UNC)' AS Allocation_type,\n             t.total_overpaid_derived AS amount,\n             t.owner_id AS asset_owner_id,\n             CASE\n                 WHEN t.charged_off_on_date <= t.settlement_date\n THEN t.charge_off_reason_cv_id END AS charge_off_reason_id,\n             t.previous_owner_id AS from_asset_owner_id,\n             t.originator_external_ids\n      FROM active_external_asset_owner_transfers AS t\n      WHERE t.total_overpaid_derived > 0\n      UNION ALL\n      SELECT '${endDate}' AS transactiondate,\n             t.id,\n             t.name AS product,\n             99999 AS transaction_type,\n             null AS payment_type_id,\n             null AS classification_name,\n             '' AS chargetype,\n             false AS reversal_indicator,\n             'Principal' AS Allocation_type,\n             t.principal_outstanding_derived AS amount,\n             null AS asset_owner_id,\n             CASE\n                 WHEN t.charged_off_on_date <= t.settlement_date\n THEN t.charge_off_reason_cv_id END AS charge_off_reason_id,\n             t.owner_id AS from_asset_owner_id,\n             t.originator_external_ids\n      FROM buyback_external_asset_owner_transfers AS t\n      WHERE t.principal_outstanding_derived > 0\n      UNION ALL\n      SELECT '${endDate}' AS transactiondate,\n             t.id,\n             t.name AS product,\n             99999 AS transaction_type,\n             null AS payment_type_id,\n             null AS classification_name,\n             '' AS chargetype,\n             false AS reversal_indicator,\n             'Interest' AS Allocation_type,\n             t.interest_outstanding_derived AS amount,\n             null AS asset_owner_id,\n             CASE\n                 WHEN t.charged_off_on_date <= t.settlement_date\n THEN t.charge_off_reason_cv_id END AS charge_off_reason_id,\n             t.owner_id AS from_asset_owner_id,\n             t.originator_external_ids\n      FROM buyback_external_asset_owner_transfers AS t\n      WHERE t.interest_outstanding_derived > 0\n      UNION ALL\n      SELECT '${endDate}' AS transactiondate,\n             t.id,\n             t.name AS product,\n             99999 AS transaction_type,\n             null AS payment_type_id,\n             null AS classification_name,\n             '' AS chargetype,\n             false AS reversal_indicator,\n             'Fees' AS Allocation_type,\n             t.fee_charges_outstanding_derived AS amount,\n             null AS asset_owner_id,\n             CASE\n                 WHEN t.charged_off_on_date <= t.settlement_date\n THEN t.charge_off_reason_cv_id END AS charge_off_reason_id,\n             t.owner_id AS from_asset_owner_id,\n             t.originator_external_ids\n      FROM buyback_external_asset_owner_transfers AS t\n      WHERE t.fee_charges_outstanding_derived > 0\n      UNION ALL\n      SELECT '${endDate}' AS transactiondate,\n             t.id,\n             t.name AS product,\n             99999 AS transaction_type,\n             null AS payment_type_id,\n             null AS classification_name,\n             '' AS chargetype,\n             false AS reversal_indicator,\n             'Penalty' AS Allocation_type,\n             t.penalty_charges_outstanding_derived AS amount,\n             null AS asset_owner_id,\n             CASE\n                 WHEN t.charged_off_on_date <= t.settlement_date\n THEN t.charge_off_reason_cv_id END AS charge_off_reason_id,\n             t.owner_id AS from_asset_owner_id,\n             t.originator_external_ids\n      FROM buyback_external_asset_owner_transfers AS t\n      WHERE t.penalty_charges_outstanding_derived > 0\n      UNION ALL\n      SELECT '${endDate}' AS transactiondate,\n             t.id,\n             t.name AS product,\n             99999 AS transaction_type,\n             null AS payment_type_id,\n             null AS classification_name,\n             '' AS chargetype,\n             false AS reversal_indicator,\n             'Unallocated Credit (UNC)' AS Allocation_type,\n             t.total_overpaid_derived * -1 AS amount,\n             null AS asset_owner_id,\n             CASE\n                 WHEN t.charged_off_on_date <= t.settlement_date\n THEN t.charge_off_reason_cv_id END AS charge_off_reason_id,\n             t.owner_id AS from_asset_owner_id,\n             t.originator_external_ids\n      FROM buyback_external_asset_owner_transfers AS t\n      WHERE t.total_overpaid_derived > 0) a\nGROUP BY a.transactiondate, a.product, a.transaction_type, a.payment_type_id, a.classification_name, a.chargetype,\n         a.reversal_indicator, a.Allocation_Type, a.asset_owner_id, a.charge_off_reason_id, a.from_asset_owner_id,\n         a.originator_external_ids\nORDER BY 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11		f	t	f
\.


ALTER TABLE public.stretchy_report 

--
-- Data for Name: m_report_mailing_job; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_report_mailing_job 

COPY public.m_report_mailing_job (id, name, description, start_datetime, recurrence, created_date, createdby_id, lastmodified_date, lastmodifiedby_id, email_recipients, email_subject, email_message, email_attachment_file_format, stretchy_report_id, stretchy_report_param_map, previous_run_datetime, next_run_datetime, previous_run_status, previous_run_error_log, previous_run_error_message, number_of_runs, is_active, is_deleted, run_as_userid) FROM stdin;
\.


ALTER TABLE public.m_report_mailing_job 

--
-- Data for Name: m_report_mailing_job_configuration; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_report_mailing_job_configuration 

COPY public.m_report_mailing_job_configuration (id, name, value) FROM stdin;
1	GMAIL_SMTP_SERVER	smtp.gmail.com
2	GMAIL_SMTP_PORT	587
3	GMAIL_SMTP_USERNAME	
4	GMAIL_SMTP_PASSWORD	
\.


ALTER TABLE public.m_report_mailing_job_configuration 

--
-- Data for Name: m_report_mailing_job_run_history; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_report_mailing_job_run_history 

COPY public.m_report_mailing_job_run_history (id, job_id, start_datetime, end_datetime, status, error_message, error_log) FROM stdin;
\.


ALTER TABLE public.m_report_mailing_job_run_history 

--
-- Data for Name: m_role_permission; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_role_permission 

COPY public.m_role_permission (role_id, permission_id) FROM stdin;
1	1
1	959
1	960
\.


ALTER TABLE public.m_role_permission 

--
-- Data for Name: m_savings_account_charge; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_savings_account_charge 

COPY public.m_savings_account_charge (id, savings_account_id, charge_id, is_penalty, charge_time_enum, charge_due_date, fee_on_month, fee_on_day, fee_interval, free_withdrawal_count, charge_reset_date, charge_calculation_enum, calculation_percentage, calculation_on_amount, amount, amount_paid_derived, amount_waived_derived, amount_writtenoff_derived, amount_outstanding_derived, is_paid_derived, waived, is_active, inactivated_on_date, created_by, last_modified_by, created_on_utc, last_modified_on_utc) FROM stdin;
\.


ALTER TABLE public.m_savings_account_charge 

--
-- Data for Name: m_savings_account_charge_paid_by; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_savings_account_charge_paid_by 

COPY public.m_savings_account_charge_paid_by (id, savings_account_transaction_id, savings_account_charge_id, amount) FROM stdin;
\.


ALTER TABLE public.m_savings_account_charge_paid_by 

--
-- Data for Name: m_savings_account_interest_rate_chart; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_savings_account_interest_rate_chart 

COPY public.m_savings_account_interest_rate_chart (id, savings_account_id, name, description, from_date, end_date, is_primary_grouping_by_amount) FROM stdin;
\.


ALTER TABLE public.m_savings_account_interest_rate_chart 

--
-- Data for Name: m_savings_account_interest_rate_slab; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_savings_account_interest_rate_slab 

COPY public.m_savings_account_interest_rate_slab (id, savings_account_interest_rate_chart_id, description, period_type_enum, from_period, to_period, amount_range_from, amount_range_to, annual_interest_rate, currency_code) FROM stdin;
\.


ALTER TABLE public.m_savings_account_interest_rate_slab 

--
-- Data for Name: m_savings_account_transaction_tax_details; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_savings_account_transaction_tax_details 

COPY public.m_savings_account_transaction_tax_details (id, savings_transaction_id, tax_component_id, amount) FROM stdin;
\.


ALTER TABLE public.m_savings_account_transaction_tax_details 

--
-- Data for Name: m_savings_interest_incentives; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_savings_interest_incentives 

COPY public.m_savings_interest_incentives (id, deposit_account_interest_rate_slab_id, entiry_type, attribute_name, condition_type, attribute_value, incentive_type, amount) FROM stdin;
\.


ALTER TABLE public.m_savings_interest_incentives 

--
-- Data for Name: m_savings_officer_assignment_history; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_savings_officer_assignment_history 

COPY public.m_savings_officer_assignment_history (id, account_id, savings_officer_id, start_date, end_date, created_by, created_date, lastmodified_date, last_modified_by, created_on_utc, last_modified_on_utc) FROM stdin;
\.


ALTER TABLE public.m_savings_officer_assignment_history 

--
-- Data for Name: m_savings_product_charge; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_savings_product_charge 

COPY public.m_savings_product_charge (savings_product_id, charge_id) FROM stdin;
\.


ALTER TABLE public.m_savings_product_charge 

--
-- Data for Name: m_share_account_charge; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_share_account_charge 

COPY public.m_share_account_charge (id, account_id, charge_id, charge_time_enum, charge_calculation_enum, charge_payment_mode_enum, calculation_percentage, calculation_on_amount, charge_amount_or_percentage, amount, amount_paid_derived, amount_waived_derived, amount_writtenoff_derived, amount_outstanding_derived, is_paid_derived, waived, min_cap, max_cap, is_active) FROM stdin;
\.


ALTER TABLE public.m_share_account_charge 

--
-- Data for Name: m_share_account_charge_paid_by; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_share_account_charge_paid_by 

COPY public.m_share_account_charge_paid_by (id, share_transaction_id, charge_transaction_id, amount) FROM stdin;
\.


ALTER TABLE public.m_share_account_charge_paid_by 

--
-- Data for Name: m_share_product_dividend_pay_out; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_share_product_dividend_pay_out 

COPY public.m_share_product_dividend_pay_out (id, product_id, amount, dividend_period_start_date, dividend_period_end_date, status, createdby_id, created_date, lastmodifiedby_id, lastmodified_date) FROM stdin;
\.


ALTER TABLE public.m_share_product_dividend_pay_out 

--
-- Data for Name: m_share_account_dividend_details; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_share_account_dividend_details 

COPY public.m_share_account_dividend_details (id, dividend_pay_out_id, account_id, amount, status, savings_transaction_id) FROM stdin;
\.


ALTER TABLE public.m_share_account_dividend_details 

--
-- Data for Name: m_share_product_charge; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_share_product_charge 

COPY public.m_share_product_charge (product_id, charge_id) FROM stdin;
\.


ALTER TABLE public.m_share_product_charge 

--
-- Data for Name: m_share_product_market_price; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_share_product_market_price 

COPY public.m_share_product_market_price (id, product_id, from_date, share_value) FROM stdin;
\.


ALTER TABLE public.m_share_product_market_price 

--
-- Data for Name: m_staff_assignment_history; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_staff_assignment_history 

COPY public.m_staff_assignment_history (id, centre_id, staff_id, start_date, end_date, createdby_id, created_date, lastmodified_date, lastmodifiedby_id) FROM stdin;
\.


ALTER TABLE public.m_staff_assignment_history 

--
-- Data for Name: m_surveys; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_surveys 

COPY public.m_surveys (id, a_key, a_name, description, country_code, valid_from, valid_to) FROM stdin;
\.


ALTER TABLE public.m_surveys 

--
-- Data for Name: m_survey_components; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_survey_components 

COPY public.m_survey_components (id, survey_id, a_key, a_text, description, sequence_no) FROM stdin;
\.


ALTER TABLE public.m_survey_components 

--
-- Data for Name: m_survey_lookup_tables; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_survey_lookup_tables 

COPY public.m_survey_lookup_tables (id, survey_id, a_key, description, value_from, value_to, score) FROM stdin;
\.


ALTER TABLE public.m_survey_lookup_tables 

--
-- Data for Name: m_survey_questions; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_survey_questions 

COPY public.m_survey_questions (id, survey_id, component_key, a_key, a_text, description, sequence_no) FROM stdin;
\.


ALTER TABLE public.m_survey_questions 

--
-- Data for Name: m_survey_responses; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_survey_responses 

COPY public.m_survey_responses (id, question_id, a_text, a_value, sequence_no) FROM stdin;
\.


ALTER TABLE public.m_survey_responses 

--
-- Data for Name: m_survey_scorecards; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_survey_scorecards 

COPY public.m_survey_scorecards (id, survey_id, question_id, response_id, user_id, client_id, created_on, a_value) FROM stdin;
\.


ALTER TABLE public.m_survey_scorecards 

--
-- Data for Name: m_tax_component_history; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_tax_component_history 

COPY public.m_tax_component_history (id, tax_component_id, percentage, start_date, end_date, createdby_id, created_date, lastmodifiedby_id, lastmodified_date) FROM stdin;
\.


ALTER TABLE public.m_tax_component_history 

--
-- Data for Name: m_tax_group_mappings; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_tax_group_mappings 

COPY public.m_tax_group_mappings (id, tax_group_id, tax_component_id, start_date, end_date, createdby_id, created_date, lastmodifiedby_id, lastmodified_date) FROM stdin;
\.


ALTER TABLE public.m_tax_group_mappings 

--
-- Data for Name: m_template_m_templatemappers; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_template_m_templatemappers 

COPY public.m_template_m_templatemappers (m_template_id, mappers_id) FROM stdin;
\.


ALTER TABLE public.m_template_m_templatemappers 

--
-- Data for Name: m_templatemappers; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_templatemappers 

COPY public.m_templatemappers (id, mapperkey, mapperorder, mappervalue) FROM stdin;
\.


ALTER TABLE public.m_templatemappers 

--
-- Data for Name: m_trial_balance; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_trial_balance 

COPY public.m_trial_balance (office_id, account_id, amount, entry_date, created_date, closing_balance) FROM stdin;
\.


ALTER TABLE public.m_trial_balance 

--
-- Data for Name: m_wc_breach_configuration; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_wc_breach_configuration 

COPY public.m_wc_breach_configuration (id, name, breach_frequency, breach_frequency_type, breach_amount_calculation_type, breach_amount) FROM stdin;
\.


ALTER TABLE public.m_wc_breach_configuration 

--
-- Data for Name: m_wc_delinquency_configuration; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_wc_delinquency_configuration 

COPY public.m_wc_delinquency_configuration (id, created_by, last_modified_by, bucket_id, frequency, frequency_type, minimum_payment, minimum_payment_type, created_on_utc, last_modified_on_utc) FROM stdin;
\.


ALTER TABLE public.m_wc_delinquency_configuration 

--
-- Data for Name: m_wc_near_breach; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_wc_near_breach 

COPY public.m_wc_near_breach (id, near_breach_name, near_breach_frequency, near_breach_frequency_type, near_breach_threshold) FROM stdin;
\.


ALTER TABLE public.m_wc_near_breach 

--
-- Data for Name: m_wc_loan_product; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_wc_loan_product 

COPY public.m_wc_loan_product (id, name, short_name, external_id, fund_id, start_date, close_date, description, currency_code, currency_digits, currency_multiplesof, amortization_type, delinquency_bucket_classification_id, npv_day_count, min_principal_amount, principal_amount, max_principal_amount, min_period_payment_rate, period_payment_rate, max_period_payment_rate, discount, repayment_every, repayment_frequency_enum, delinquency_grace_days, delinquency_start_type, breach_id, accounting_type, near_breach_id, breach_grace_days) FROM stdin;
\.


ALTER TABLE public.m_wc_loan_product 

--
-- Data for Name: m_wc_loan; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_wc_loan 

COPY public.m_wc_loan (id, version, created_by, last_modified_by, created_on_utc, last_modified_on_utc, loan_status_id, last_closed_business_date, account_no, external_id, disbursedon_date, client_id, fund_id, product_id, submittedon_date, rejectedon_date, rejectedon_userid, approvedon_date, approvedon_userid, closedon_date, closedon_userid, expected_maturedon_date, maturedon_date, principal_amount_proposed, approved_principal, currency_code, currency_digits, currency_multiplesof, principal_amount, period_payment_rate, repayment_every, repayment_frequency_enum, amortization_type, npv_day_count, discount, delinquency_bucket_classification_id, loan_counter, loan_product_counter, delinquency_grace_days, delinquency_start_type, wc_loan_product_id, breach_id, near_breach_id, discount_proposed, discount_approved, total_payment_volume, breach_grace_days) FROM stdin;
\.


ALTER TABLE public.m_wc_loan 

--
-- Data for Name: m_wc_loan_account_locks; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_wc_loan_account_locks 

COPY public.m_wc_loan_account_locks (loan_id, version, lock_owner, lock_placed_on, error, stacktrace, lock_placed_on_cob_business_date) FROM stdin;
\.


ALTER TABLE public.m_wc_loan_account_locks 

--
-- Data for Name: m_wc_loan_amortization_model; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_wc_loan_amortization_model 

COPY public.m_wc_loan_amortization_model (id, version, loan_id, json_model, business_date, json_model_version, last_modified_on_utc) FROM stdin;
\.


ALTER TABLE public.m_wc_loan_amortization_model 

--
-- Data for Name: m_wc_loan_balance; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_wc_loan_balance 

COPY public.m_wc_loan_balance (id, wc_loan_id, principal_paid, realized_income_from_discount_fee, version, created_by, last_modified_by, created_on_utc, last_modified_on_utc, overpayment_amount, principal, fee, fee_paid, penalty, penalty_paid, total_disbursement, total_discount_fee, total_discount_fee_adjustment) FROM stdin;
\.


ALTER TABLE public.m_wc_loan_balance 

--
-- Data for Name: m_wc_loan_breach_action; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_wc_loan_breach_action 

COPY public.m_wc_loan_breach_action (id, wc_loan_id, action, start_date, end_date, created_by, last_modified_by, created_on_utc, last_modified_on_utc, minimum_payment, minimum_payment_type, frequency, frequency_type) FROM stdin;
\.


ALTER TABLE public.m_wc_loan_breach_action 

--
-- Data for Name: m_wc_loan_breach_schedule; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_wc_loan_breach_schedule 

COPY public.m_wc_loan_breach_schedule (id, wc_loan_id, period_number, from_date, to_date, number_of_days, min_payment_amount, paid_amount, outstanding_amount, near_breach, breach, created_by, last_modified_by, created_on_utc, last_modified_on_utc, reset) FROM stdin;
\.


ALTER TABLE public.m_wc_loan_breach_schedule 

--
-- Data for Name: m_wc_loan_breach_reset_history; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_wc_loan_breach_reset_history 

COPY public.m_wc_loan_breach_reset_history (id, breach_action_id, breach_schedule_id, outstanding_amount, breach, near_breach, created_by, last_modified_by, created_on_utc, last_modified_on_utc, min_payment_amount) FROM stdin;
\.


ALTER TABLE public.m_wc_loan_breach_reset_history 

--
-- Data for Name: m_wc_loan_charge; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_wc_loan_charge 

COPY public.m_wc_loan_charge (id, loan_id, charge_id, is_penalty, charge_time_type, charge_calculation_type, charge_payment_mode, calculation_on_amount, amount_paid, amount, is_paid, is_active, due_date, created_by, last_modified_by, external_id, submitted_on_date, created_on_utc, last_modified_on_utc) FROM stdin;
\.


ALTER TABLE public.m_wc_loan_charge 

--
-- Data for Name: m_wc_loan_delinquency_action; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_wc_loan_delinquency_action 

COPY public.m_wc_loan_delinquency_action (id, wc_loan_id, action, start_date, end_date, created_by, last_modified_by, created_on_utc, last_modified_on_utc, minimum_payment, minimum_payment_type, frequency, frequency_type) FROM stdin;
\.


ALTER TABLE public.m_wc_loan_delinquency_action 

--
-- Data for Name: m_wc_loan_delinquency_range_schedule; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_wc_loan_delinquency_range_schedule 

COPY public.m_wc_loan_delinquency_range_schedule (id, wc_loan_id, period_number, from_date, to_date, expected_amount, paid_amount, outstanding_amount, min_payment_criteria_met, created_by, last_modified_by, created_on_utc, last_modified_on_utc, delinquent_days, delinquent_amount, version) FROM stdin;
\.


ALTER TABLE public.m_wc_loan_delinquency_range_schedule 

--
-- Data for Name: m_wc_loan_disbursement_detail; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_wc_loan_disbursement_detail 

COPY public.m_wc_loan_disbursement_detail (id, wc_loan_id, expected_disburse_date, expected_amount, expected_maturity_date, actual_disburse_date, actual_amount, disbursedon_userid) FROM stdin;
\.


ALTER TABLE public.m_wc_loan_disbursement_detail 

--
-- Data for Name: m_wc_loan_near_breach_action; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_wc_loan_near_breach_action 

COPY public.m_wc_loan_near_breach_action (id, wc_loan_id, action, threshold, frequency, frequency_type, created_by, last_modified_by, created_on_utc, last_modified_on_utc) FROM stdin;
\.


ALTER TABLE public.m_wc_loan_near_breach_action 

--
-- Data for Name: m_wc_loan_note; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_wc_loan_note 

COPY public.m_wc_loan_note (id, wc_loan_id, note, created_by, last_modified_by, created_on_utc, last_modified_on_utc) FROM stdin;
\.


ALTER TABLE public.m_wc_loan_note 

--
-- Data for Name: m_wc_loan_originator_mapping; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_wc_loan_originator_mapping 

COPY public.m_wc_loan_originator_mapping (id, loan_id, originator_id, created_on_utc, created_by, last_modified_on_utc, last_modified_by) FROM stdin;
\.


ALTER TABLE public.m_wc_loan_originator_mapping 

--
-- Data for Name: m_wc_loan_payment_allocation_rule; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_wc_loan_payment_allocation_rule 

COPY public.m_wc_loan_payment_allocation_rule (id, wc_loan_id, transaction_type, allocation_types, created_by, last_modified_by, created_on_utc, last_modified_on_utc) FROM stdin;
\.


ALTER TABLE public.m_wc_loan_payment_allocation_rule 

--
-- Data for Name: m_wc_loan_period_payment_rate_change; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_wc_loan_period_payment_rate_change 

COPY public.m_wc_loan_period_payment_rate_change (id, wc_loan_id, effective_date, previous_rate, new_rate, is_reversed, reversed_on_date, created_by, last_modified_by, version, created_on_utc, last_modified_on_utc) FROM stdin;
\.


ALTER TABLE public.m_wc_loan_period_payment_rate_change 

--
-- Data for Name: m_wc_loan_product_configurable_attributes; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_wc_loan_product_configurable_attributes 

COPY public.m_wc_loan_product_configurable_attributes (id, wc_loan_product_id, delinquency_bucket_classification_overridable, discount_default_overridable, period_payment_frequency_overridable, period_payment_frequency_type_overridable, breach_overridable) FROM stdin;
\.


ALTER TABLE public.m_wc_loan_product_configurable_attributes 

--
-- Data for Name: m_wc_loan_product_payment_allocation_rule; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_wc_loan_product_payment_allocation_rule 

COPY public.m_wc_loan_product_payment_allocation_rule (id, wc_loan_product_id, transaction_type, allocation_types, created_by, last_modified_by, created_on_utc, last_modified_on_utc) FROM stdin;
\.


ALTER TABLE public.m_wc_loan_product_payment_allocation_rule 

--
-- Data for Name: m_wc_loan_range_delinquency_tag; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_wc_loan_range_delinquency_tag 

COPY public.m_wc_loan_range_delinquency_tag (id, created_by, last_modified_by, delinquency_range_id, loan_id, range_id, addedon_date, liftedon_date, outstanding_amount, version, created_on_utc, last_modified_on_utc) FROM stdin;
\.


ALTER TABLE public.m_wc_loan_range_delinquency_tag 

--
-- Data for Name: m_wc_loan_transaction; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_wc_loan_transaction 

COPY public.m_wc_loan_transaction (id, wc_loan_id, payment_detail_id, classification_cv_id, external_id, transaction_type_id, transaction_date, submitted_on_date, transaction_amount, version, created_by, last_modified_by, created_on_utc, last_modified_on_utc, is_reversed, reversal_external_id, reversed_on_date) FROM stdin;
\.


ALTER TABLE public.m_wc_loan_transaction 

--
-- Data for Name: m_wc_loan_transaction_allocation; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_wc_loan_transaction_allocation 

COPY public.m_wc_loan_transaction_allocation (id, wc_loan_transaction_id, principal_portion, fee_charges_portion, penalty_charges_portion, version, created_by, last_modified_by, created_on_utc, last_modified_on_utc) FROM stdin;
\.


ALTER TABLE public.m_wc_loan_transaction_allocation 

--
-- Data for Name: m_wc_loan_transaction_relation; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_wc_loan_transaction_relation 

COPY public.m_wc_loan_transaction_relation (id, created_by, last_modified_by, from_loan_transaction_id, to_loan_transaction_id, relation_type_enum, created_on_utc, last_modified_on_utc, to_loan_charge_id) FROM stdin;
\.


ALTER TABLE public.m_wc_loan_transaction_relation 

--
-- Data for Name: m_working_days; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.m_working_days 

COPY public.m_working_days (id, recurrence, repayment_rescheduling_enum, extend_term_daily_repayments, extend_term_holiday_repayment) FROM stdin;
1	FREQ=WEEKLY;INTERVAL=1;BYDAY=MO,TU,WE,TH,FR,SA,SU	2	f	f
\.


ALTER TABLE public.m_working_days 

--
-- Data for Name: mix_taxonomy; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.mix_taxonomy 

COPY public.mix_taxonomy (id, name, namespace_id, dimension, type, description, need_mapping) FROM stdin;
1	AdministrativeExpense	1	\N	3	\N	t
2	Assets	3	\N	1	All outstanding principals due for all outstanding client loans. This includes current, delinquent, and renegotiated loans, but not loans that have been written off. It does not include interest receivable.	t
3	Assets	3	MaturityDimension:LessThanOneYearMember	1	Segmentation based on the life of an asset or liability.	t
4	Assets	3	MaturityDimension:MoreThanOneYearMember	1	Segmentation based on the life of an asset or liability.	t
5	CashAndCashEquivalents	1	\N	1	\N	t
6	Deposits	3	\N	1	The total value of funds placed in an account with an MFI that are payable to a depositor. This item includes any current, checking, or savings accounts that are payable on demand. It also includes time deposits which have a fixed maturity date and compulsory deposits.	t
7	Deposits	3	DepositProductsDimension:CompulsoryMember	1	The value of deposits that an MFI's clients are required to  maintain as a condition of an existing or future loan.	\N
8	Deposits	3	DepositProductsDimension:VoluntaryMember	1	The value of deposits that an MFI's clients are not required to  maintain as a condition of an existing or future loan.	\N
9	Deposits	3	LocationDimension:RuralMember	1	Located in rural areas. Segmentation based on location.	\N
10	Deposits	3	LocationDimension:UrbanMember	1	Located in urban areas. Segmentation based on location.	\N
11	Deposits	3	MaturityDimension:LessThanOneYearMember	1	Segmentation based on the life of an asset or liability.	\N
12	Deposits	3	MaturityDimension:MoreThanOneYearMember	1	Segmentation based on the life of an asset or liability.	\N
13	EmployeeBenefitsExpense	1	\N	3	\N	\N
14	Equity	1	\N	1	\N	\N
15	Expense	1	\N	3	\N	\N
16	FinancialExpense	3	\N	3	All costs All costs incurred in raising funds from third parties, fee expenses from non-financial services, net gains (losses) due to changes in fair value of financial liabilities, impairment losses net of reversals of financial assets other than loan portfolio and net gains (losses) from restatement of financial statements in terms of the measuring unit current at the end of the reporting period.	\N
17	FinancialRevenueOnLoans	3	\N	2	Interest and non-interest income generated by the provision of credit services to the clients. Fees and commissions for late payment are also included.	\N
18	ImpairmentLossAllowanceGrossLoanPortfolio	3	\N	2	An allowance for the risk of losses in the gross loan portfolio due to default .	\N
19	Liabilities	1	\N	1	\N	\N
20	Liabilities	3	MaturityDimension:LessThanOneYearMember	1	Segmentation based on the life of an asset or liability.	\N
21	Liabilities	3	MaturityDimension:MoreThanOneYearMember	1	Segmentation based on the life of an asset or liability.	\N
22	LoanPortfolioGross	3	\N	2	All outstanding principals due for all outstanding client loans. This includes current, delinquent, and renegotiated loans, but not loans that have been written off. It does not include interest receivable.	\N
23	LoanPortfolioGross	3	CreditProductsDimension:MicroenterpriseMember	2	Loans that finance the production or trade of goods and  services for an individual's microenterprise, whether or not the microenterprise is legally registered. Segmentation based on loan product.	\N
24	LoanPortfolioGross	3	DelinquencyDimension:OneMonthOrMoreMember	2	Segmentation based on the principal balance of all loans outstanding that have one or more installments of principal  past due or renegotiated. Segmentation based on the  principal balance of all loans outstanding that have one or  more installments of principal past due or renegotiated.	\N
25	LoanPortfolioGross	3	DelinquencyDimension:ThreeMonthsOrMoreMember	2	Segmentation based on the principal balance of all loans outstanding that have one or more installments of principal  past due or renegotiated.? Segmentation based on the  principal balance of all loans outstanding that have one or  more installments of principal past due or renegotiated.	\N
26	LoanPortfolioGross	3	LocationDimension:RuralMember	2	Located in rural areas. Segmentation based on geographic location.	\N
27	LoanPortfolioGross	3	LocationDimension:UrbanMember	2	Located in urbal areas. Segmentation based on geographic location.	\N
28	LoanPortfolioGross	3	MaturityDimension:LessThanOneYearMember	2	Segmentation based on the life of an asset or liability.	\N
29	LoanPortfolioGross	3	MaturityDimension:MoreThanOneYearMember	2	Segmentation based on the life of an asset or liability.	\N
30	NetLoanLoss	3		3	Referred to the value of delinquency loans written off net of any principal recovery.	\N
31	NetLoanLossProvisionExpense	3	\N	3	Represent the net value of loan portfolio impairment loss considering any reversal on impairment loss and any recovery on loans written off recognized as a income during the accounting period.	\N
32	NetOperatingIncome	3	\N	2	Total operating revenue less all expenses related to the MFI's core financial service operation including total financial expense, impairment loss and operating expense. Donations are excluded.	\N
33	NetOperatingIncomeNetOfTaxExpense	3	\N	3	Net operating income reported incorporating the effect of taxes. Taxes include all domestic and foreign taxes which are based on taxable profits, other taxes related to personnel, financial transactions or value-added taxes are not considered in calculation of this value.	\N
34	NumberOfActiveBorrowers	3	\N	0	The number of individuals who currently have an outstanding loan balance with the MFI or are primarily responsible for repaying any portion of the gross loan portfolio. Individuals who have multiple loans with an MFI should be counted as a single borrower.	\N
35	NumberOfActiveBorrowers	3	GenderDimension:FemaleMember	0	The number of individuals who currently have an outstanding loan balance with the MFI or are primarily responsible for repaying any portion of the gross loan portfolio. Individuals who have multiple loans with an MFI should be counted as a single borrower.	\N
36	NumberOfBoardMembers	3	GenderDimension:FemaleMember	0	The number of members that comprise the board of directors at the end of the reporting period who are female.	\N
37	NumberOfDepositAccounts	3	\N	0	The number of individuals who currently have funds on deposit with the MFI on a voluntary basis; i.e., they are not required to maintain the deposit account to access a loan. This number applies only to deposits held by an MFI, not to those deposits held in other institutions by the MFI's clients. The number should be based on the number of individuals rather than the number of groups. A single deposit account may represent multiple depositors.	\N
38	NumberOfDepositors	3		0	The number of deposit accounts, both voluntary and compulsory, opened at the MFI whose balances the institution is liable to repay. The number should be based on the number of individual accounts rather than on the number of groups.	\N
39	NumberOfEmployees	3	\N	0	The number of individuals who are actively employed by an entity. This number includes contract employees or advisors who dedicate a substantial portion of their time to the entity, even if they are not on the entity's employees roster.	\N
40	NumberOfEmployees	3	GenderDimension:FemaleMember	0	The number of individuals who are actively employed by an entity. This number includes contract employees or advisors who dedicate a substantial portion of their time to the entity, even if they are not on the entity's employees roster.	\N
41	NumberOfLoanOfficers	3	\N	0	The number of employees whose main activity is to manage a portion of the gross loan portfolio. A loan officer is a staff member of record who is directly responsible for arranging and monitoring client loans.	\N
42	NumberOfLoanOfficers	3	GenderDimension:FemaleMember	0	The number of employees whose main activity is to manage a portion of the gross loan portfolio. A loan officer is a staff member of record who is directly responsible for arranging and monitoring client loans.	\N
43	NumberOfManagers	3	GenderDimension:FemaleMember	0	The number of members that comprise the management of the institution who are female.	\N
44	NumberOfOffices	3	\N	0	The number of staffed points of service and administrative sites used to deliver or support the delivery of financial services to microfinance clients.	\N
45	NumberOfOutstandingLoans	3	\N	0	The number of loans in the gross loan portfolio. For MFIs using a group lending methodology, the number of loans should refer to the number of individuals receiving loans as part of a group or as part of a group loan.	\N
46	OperatingExpense	3	\N	3	Includes expenses not related to financial and credit loss impairment, such as personnel expenses, depreciation, amortization and administrative expenses.	\N
47	OperatingIncome	3	\N	2	Includes all financial income and other operating revenue which is generated from non-financial services. Operating income also includes net gains (losses) from holding financial assets (changes on their values during the period and foreign exchange differences). Donations or any revenue not related with an MFI's core business of making loans and providing financial services are not considered under this category.	\N
48	WriteOffsOnGrossLoanPortfolio	3	\N	2	The value of loans that have been recognized as uncollectible for accounting purposes. A write-off is an accounting procedure that removes the outstanding balance of the loan from the gross loan portfolio and impairment loss allowance. Thus, the write-off does not affect the net loan portfolio, total assets, or any equity account. If the impairment loss allowance is insufficient to cover the amount written off, the excess amount will result in an additional impairment loss on loans recognised in profit or loss of the period.	\N
\.


ALTER TABLE public.mix_taxonomy 

--
-- Data for Name: mix_taxonomy_mapping; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.mix_taxonomy_mapping 

COPY public.mix_taxonomy_mapping (id, identifier, config, last_update_date, currency) FROM stdin;
1	default	\N	\N	
\.


ALTER TABLE public.mix_taxonomy_mapping 

--
-- Data for Name: mix_xbrl_namespace; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.mix_xbrl_namespace 

COPY public.mix_xbrl_namespace (id, prefix, url) FROM stdin;
1	ifrs	http://xbrl.iasb.org/taxonomy/2009-04-01/ifrs
2	iso4217	http://www.xbrl.org/2003/iso4217
3	mix	http://www.themix.org/INT/fr/ifrs/basi/YYYY-MM-DD/mx-cor
4	xbrldi	http://xbrl.org/2006/xbrldi
5	xbrli	http://www.xbrl.org/2003/instance
6	link	http://www.xbrl.org/2003/linkbase
7	dc-all	http://www.themix.org/INT/fr/ifrs/basi/2010-08-31/dc-all
\.


ALTER TABLE public.mix_xbrl_namespace 

--
-- Data for Name: notification_generator; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.notification_generator 

COPY public.notification_generator (id, object_type, object_identifier, action, actor, is_system_generated, notification_content, created_at) FROM stdin;
\.


ALTER TABLE public.notification_generator 

--
-- Data for Name: notification_mapper; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.notification_mapper 

COPY public.notification_mapper (id, notification_id, user_id, is_read, created_at) FROM stdin;
\.


ALTER TABLE public.notification_mapper 

--
-- Data for Name: oauth_access_token; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.oauth_access_token 

COPY public.oauth_access_token (token_id, token, authentication_id, user_name, client_id, authentication, refresh_token) FROM stdin;
\.


ALTER TABLE public.oauth_access_token 

--
-- Data for Name: oauth_client_details; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.oauth_client_details 

COPY public.oauth_client_details (client_id, resource_ids, client_secret, scope, authorized_grant_types, web_server_redirect_uri, authorities, access_token_validity, refresh_token_validity, additional_information, autoapprove) FROM stdin;
community-app	\N	{SHA-256}a665a45920422f9d417e4867efdc4fb8a04a1f3fff1fa07e998e86f7f7a27ae3	all	password,refresh_token	\N	\N	\N	\N	\N	\N
\.


ALTER TABLE public.oauth_client_details 

--
-- Data for Name: oauth_refresh_token; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.oauth_refresh_token 

COPY public.oauth_refresh_token (token_id, token, authentication) FROM stdin;
\.


ALTER TABLE public.oauth_refresh_token 

--
-- Data for Name: ppi_likelihoods; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.ppi_likelihoods 

COPY public.ppi_likelihoods (id, code, name) FROM stdin;
\.


ALTER TABLE public.ppi_likelihoods 

--
-- Data for Name: ppi_likelihoods_ppi; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.ppi_likelihoods_ppi 

COPY public.ppi_likelihoods_ppi (id, likelihood_id, ppi_name, enabled) FROM stdin;
\.


ALTER TABLE public.ppi_likelihoods_ppi 

--
-- Data for Name: ppi_scores; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.ppi_scores 

COPY public.ppi_scores (id, score_from, score_to) FROM stdin;
1	0	4
2	5	9
3	10	14
4	15	19
5	20	24
6	25	29
7	30	34
8	35	39
9	40	44
10	45	49
11	50	54
12	55	59
13	60	64
14	65	69
15	70	74
16	75	79
17	80	84
18	85	89
19	90	94
20	95	100
\.


ALTER TABLE public.ppi_scores 

--
-- Data for Name: r_enum_value; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.r_enum_value 

COPY public.r_enum_value (enum_name, enum_id, enum_message_property, enum_value, enum_type) FROM stdin;
account_type_type_enum	0	INVALID	INVALID	f
account_type_type_enum	1	INDIVIDUAL	INDIVIDUAL	f
account_type_type_enum	2	GROUP	GROUP	f
account_type_type_enum	3	JLG	JLG	f
accrual_accounts_for_loan_type_enum	1	FUND_SOURCE	FUND_SOURCE	f
accrual_accounts_for_loan_type_enum	2	LOAN_PORTFOLIO	LOAN_PORTFOLIO	f
accrual_accounts_for_loan_type_enum	3	INTEREST_ON_LOANS	INTEREST_ON_LOANS	f
accrual_accounts_for_loan_type_enum	4	INCOME_FROM_FEES	INCOME_FROM_FEES	f
accrual_accounts_for_loan_type_enum	5	INCOME_FROM_PENALTIES	INCOME_FROM_PENALTIES	f
accrual_accounts_for_loan_type_enum	6	LOSSES_WRITTEN_OFF	LOSSES_WRITTEN_OFF	f
accrual_accounts_for_loan_type_enum	7	INTEREST_RECEIVABLE	INTEREST_RECEIVABLE	f
accrual_accounts_for_loan_type_enum	8	FEES_RECEIVABLE	FEES_RECEIVABLE	f
accrual_accounts_for_loan_type_enum	9	PENALTIES_RECEIVABLE	PENALTIES_RECEIVABLE	f
accrual_accounts_for_loan_type_enum	10	TRANSFERS_SUSPENSE	TRANSFERS_SUSPENSE	f
accrual_accounts_for_loan_type_enum	11	OVERPAYMENT	OVERPAYMENT	f
accrual_accounts_for_loan_type_enum	12	INCOME_FROM_RECOVERY	INCOME_FROM_RECOVERY	f
amortization_method_enum	0	Equal principle payments	Equal principle payments	f
amortization_method_enum	1	Equal installments	Equal installments	f
calendar_type_enum	0	INVALID	INVALID	f
calendar_type_enum	1	CLIENTS	CLIENTS	f
calendar_type_enum	2	GROUPS	GROUPS	f
calendar_type_enum	3	LOANS	LOANS	f
calendar_type_enum	4	CENTERS	CENTERS	f
calendar_type_enum	5	SAVINGS	SAVINGS	f
calendar_type_enum	6	LOAN_RECALCULATION_REST_DETAIL	LOAN_RECALCULATION_REST_DETAIL	f
calendar_type_enum	7	LOAN_RECALCULATION_COMPOUNDING_DETAIL	LOAN_RECALCULATION_COMPOUNDING_DETAIL	f
cash_accounts_for_loan_type_enum	1	FUND_SOURCE	FUND_SOURCE	f
cash_accounts_for_loan_type_enum	2	LOAN_PORTFOLIO	LOAN_PORTFOLIO	f
cash_accounts_for_loan_type_enum	3	INTEREST_ON_LOANS	INTEREST_ON_LOANS	f
cash_accounts_for_loan_type_enum	4	INCOME_FROM_FEES	INCOME_FROM_FEES	f
cash_accounts_for_loan_type_enum	5	INCOME_FROM_PENALTIES	INCOME_FROM_PENALTIES	f
cash_accounts_for_loan_type_enum	6	LOSSES_WRITTEN_OFF	LOSSES_WRITTEN_OFF	f
cash_accounts_for_loan_type_enum	10	TRANSFERS_SUSPENSE	TRANSFERS_SUSPENSE	f
cash_accounts_for_loan_type_enum	11	OVERPAYMENT	OVERPAYMENT	f
cash_accounts_for_loan_type_enum	12	INCOME_FROM_RECOVERY	INCOME_FROM_RECOVERY	f
cash_accounts_for_savings_type_enum	1	SAVINGS_REFERENCE	SAVINGS_REFERENCE	f
cash_accounts_for_savings_type_enum	2	SAVINGS_CONTROL	SAVINGS_CONTROL	f
cash_accounts_for_savings_type_enum	3	INTEREST_ON_SAVINGS	INTEREST_ON_SAVINGS	f
cash_accounts_for_savings_type_enum	4	INCOME_FROM_FEES	INCOME_FROM_FEES	f
cash_accounts_for_savings_type_enum	5	INCOME_FROM_PENALTIES	INCOME_FROM_PENALTIES	f
cash_accounts_for_savings_type_enum	10	TRANSFERS_SUSPENSE	TRANSFERS_SUSPENSE	f
cash_accounts_for_savings_type_enum	11	OVERDRAFT_PORTFOLIO_CONTROL	OVERDRAFT_PORTFOLIO_CONTROL	f
cash_accounts_for_savings_type_enum	12	INCOME_FROM_INTEREST	INCOME_FROM_INTEREST	f
cash_account_for_shares_type_enum	1	SHARES_REFERENCE	SHARES_REFERENCE	f
cash_account_for_shares_type_enum	2	SHARES_SUSPENSE	SHARES_SUSPENSE	f
cash_account_for_shares_type_enum	3	INCOME_FROM_FEES	INCOME_FROM_FEES	f
cash_account_for_shares_type_enum	4	SHARES_EQUITY	SHARES_EQUITY	f
client_transaction_type_enum	1	PAY_CHARGE	PAY_CHARGE	f
client_transaction_type_enum	2	WAIVE_CHARGE	WAIVE_CHARGE	f
entity_account_type_enum	1	CLIENT	CLIENT	f
entity_account_type_enum	2	LOAN	LOAN	f
entity_account_type_enum	3	SAVINGS	SAVINGS	f
entity_account_type_enum	4	CENTER	CENTER	f
entity_account_type_enum	5	GROUP	GROUP	f
entity_account_type_enum	6	SHARES	SHARES	f
financial_activity_type_enum	100	ASSET_TRANSFER	ASSET_TRANSFER	f
financial_activity_type_enum	101	CASH_AT_MAINVAULT	CASH_AT_MAINVAULT	f
financial_activity_type_enum	102	CASH_AT_TELLER	CASH_AT_TELLER	f
financial_activity_type_enum	103	ASSET_FUND_SOURCE	ASSET_FUND_SOURCE	f
financial_activity_type_enum	200	LIABILITY_TRANSFER	LIABILITY_TRANSFER	f
financial_activity_type_enum	201	PAYABLE_DIVIDENDS	PAYABLE_DIVIDENDS	f
financial_activity_type_enum	300	OPENING_BALANCES_TRANSFER_CONTRA	OPENING_BALANCES_TRANSFER_CONTRA	f
glaccount_type_enum	1	ASSET	ASSET	f
glaccount_type_enum	2	LIABILITY	LIABILITY	f
glaccount_type_enum	3	EQUITY	EQUITY	f
glaccount_type_enum	4	INCOME	INCOME	f
glaccount_type_enum	5	EXPENSE	EXPENSE	f
interest_calculated_in_period_enum	0	Daily	Daily	f
interest_calculated_in_period_enum	1	Same as repayment period	Same as repayment period	f
interest_method_enum	0	Declining Balance	Declining Balance	f
interest_method_enum	1	Flat	Flat	f
interest_period_frequency_enum	2	Per month	Per month	f
interest_period_frequency_enum	3	Per year	Per year	f
journal_entry_type_type_enum	1	CREDIT	CREDIT	f
journal_entry_type_type_enum	2	DEBIT	DEBIT	f
loan_status_id	0	Invalid	Invalid	f
loan_status_id	100	Submitted and awaiting approval	Submitted and awaiting approval	f
loan_status_id	200	Approved	Approved	f
loan_status_id	300	Active	Active	f
loan_status_id	400	Withdrawn by client	Withdrawn by client	f
loan_status_id	500	Rejected	Rejected	f
loan_status_id	600	Closed	Closed	f
loan_status_id	601	Written-Off	Written-Off	f
loan_status_id	602	Rescheduled	Rescheduled	f
loan_status_id	700	Overpaid	Overpaid	f
loan_transaction_strategy_id	1	mifos-standard-strategy	Mifos style	f
loan_transaction_strategy_id	2	heavensfamily-strategy	Heavensfamily	f
loan_transaction_strategy_id	3	creocore-strategy	Creocore	f
loan_transaction_strategy_id	4	rbi-india-strategy	RBI (India)	f
loan_transaction_type_enum	0	INVALID	INVALID	f
loan_transaction_type_enum	1	DISBURSEMENT	DISBURSEMENT	f
loan_transaction_type_enum	2	REPAYMENT	REPAYMENT	f
loan_transaction_type_enum	3	CONTRA	CONTRA	f
loan_transaction_type_enum	4	WAIVE_INTEREST	WAIVE_INTEREST	f
loan_transaction_type_enum	5	REPAYMENT_AT_DISBURSEMENT	REPAYMENT_AT_DISBURSEMENT	f
loan_transaction_type_enum	6	WRITEOFF	WRITEOFF	f
loan_transaction_type_enum	7	MARKED_FOR_RESCHEDULING	MARKED_FOR_RESCHEDULING	f
loan_transaction_type_enum	8	RECOVERY_REPAYMENT	RECOVERY_REPAYMENT	f
loan_transaction_type_enum	9	WAIVE_CHARGES	WAIVE_CHARGES	f
loan_transaction_type_enum	10	ACCRUAL	ACCRUAL	f
loan_transaction_type_enum	12	INITIATE_TRANSFER	INITIATE_TRANSFER	f
loan_transaction_type_enum	13	APPROVE_TRANSFER	APPROVE_TRANSFER	f
loan_transaction_type_enum	14	WITHDRAW_TRANSFER	WITHDRAW_TRANSFER	f
loan_transaction_type_enum	15	REJECT_TRANSFER	REJECT_TRANSFER	f
loan_transaction_type_enum	16	REFUND	REFUND	f
loan_transaction_type_enum	17	CHARGE_PAYMENT	CHARGE_PAYMENT	f
loan_transaction_type_enum	18	REFUND_FOR_ACTIVE_LOAN	REFUND_FOR_ACTIVE_LOAN	f
loan_transaction_type_enum	19	INCOME_POSTING	INCOME_POSTING	f
loan_type_enum	1	Individual Loan	Individual Loan	f
loan_type_enum	2	Group Loan	Group Loan	f
portfolio_account_type_enum	1	LOAN	LOAN	f
portfolio_account_type_enum	2	SAVING	EXPENSE	f
portfolio_account_type_enum	3	PROVISIONING	PROVISIONING	f
portfolio_account_type_enum	4	SHARES	SHARES	f
processing_result_enum	0	invalid	Invalid	f
processing_result_enum	1	processed	Processed	f
processing_result_enum	2	awaiting.approval	Awaiting Approval	f
processing_result_enum	3	rejected	Rejected	f
repayment_period_frequency_enum	0	Days	Days	f
repayment_period_frequency_enum	1	Weeks	Weeks	f
repayment_period_frequency_enum	2	Months	Months	f
savings_transaction_type_enum	0	INVALID	INVALID	f
savings_transaction_type_enum	1	deposit	deposit	f
savings_transaction_type_enum	2	withdrawal	withdrawal	t
savings_transaction_type_enum	3	Interest Posting	Interest Posting	f
savings_transaction_type_enum	4	Withdrawal Fee	Withdrawal Fee	t
savings_transaction_type_enum	5	Annual Fee	Annual Fee	t
savings_transaction_type_enum	6	Waive Charge	Waive Charge	f
savings_transaction_type_enum	7	Pay Charge	Pay Charge	t
savings_transaction_type_enum	8	DIVIDEND_PAYOUT	DIVIDEND_PAYOUT	f
savings_transaction_type_enum	12	Initiate Transfer	Initiate Transfer	f
savings_transaction_type_enum	13	Approve Transfer	Approve Transfer	f
savings_transaction_type_enum	14	Withdraw Transfer	Withdraw Transfer	f
savings_transaction_type_enum	15	Reject Transfer	Reject Transfer	f
savings_transaction_type_enum	16	Written-Off	Written-Off	f
savings_transaction_type_enum	17	Overdraft Interest	Overdraft Interest	f
savings_transaction_type_enum	19	WITHHOLD_TAX	WITHHOLD_TAX	f
status_enum	0	Invalid	Invalid	f
status_enum	100	Pending	Pending	f
status_enum	300	Active	Active	f
status_enum	600	Closed	Closed	f
teller_status	300	Active	Active	f
teller_status	400	Inactive	Inactive	f
teller_status	600	Closed	Closed	f
term_period_frequency_enum	0	Days	Days	f
term_period_frequency_enum	1	Weeks	Weeks	f
term_period_frequency_enum	2	Months	Months	f
term_period_frequency_enum	3	Years	Years	f
transaction_type_enum	1	Disbursement	Disbursement	f
transaction_type_enum	2	Repayment	Repayment	f
transaction_type_enum	3	Contra	Contra	f
transaction_type_enum	4	Waive Interest	Waive Interest	f
transaction_type_enum	5	Repayment At Disbursement	Repayment At Disbursement	f
transaction_type_enum	6	Write-Off	Write-Off	f
transaction_type_enum	7	Marked for Rescheduling	Marked for Rescheduling	f
transaction_type_enum	8	Recovery Repayment	Recovery Repayment	f
transaction_type_enum	9	Waive Charges	Waive Charges	f
transaction_type_enum	10	Apply Charges	Apply Charges	f
transaction_type_enum	11	Apply Interest	Apply Interest	f
transaction_type_enum	12	Initiate Transfer	Initiate Transfer	f
transaction_type_enum	13	Approve Transfer	Approve Transfer	f
transaction_type_enum	14	Withdraw Transfer	Withdraw Transfer	f
transaction_type_enum	15	Reject Transfer	Reject Transfer	f
transaction_type_enum	16	Refund	Refund	f
transaction_type_enum	17	Charge Payment	Charge Payment	f
transaction_type_enum	18	Refund for Active Loan	Refund for Active Loan	f
transaction_type_enum	19	Income Posting	Income Posting	f
transaction_type_enum	20	Credit Balance Refund	Credit Balance Refund	f
transaction_type_enum	21	Merchant Issued Refund	Merchant Issued Refund	f
transaction_type_enum	22	Payout Refund	Payout Refund	f
transaction_type_enum	23	Goodwill Credit	Goodwill Credit	f
transaction_type_enum	24	Charge Refund	Charge Refund	f
transaction_type_enum	25	Chargeback	Chargeback	f
transaction_type_enum	26	Charge Adjustment	Charge Adjustment	f
transaction_type_enum	27	Charge-off	Charge-off	f
loan_transaction_strategy_id	8	Due penalty, fee, interest, principal, In advance principal, penalty, fee, interest	Due penalty, fee, interest, principal, In advance principal, penalty, fee, interest	f
loan_transaction_strategy_id	9	Due penalty, interest, principal, fee, In advance penalty, interest, principal, fee	Due penalty, interest, principal, fee, In advance penalty, interest, principal, fee	f
transaction_type_enum	31	Interest Payment Waiver	Interest Payment Waiver	f
transaction_type_enum	32	Accrual Activity	Accrual Activity	f
transaction_type_enum	33	Interest Refund	Interest Refund	f
transaction_type_enum	36	Capitalized Income Amortization	Capitalized Income Amortization	f
transaction_type_enum	37	Capitalized Income Adjustment	Capitalized Income Adjustment	f
transaction_type_enum	39	Capitalized Income Amortization Adjustment	Capitalized Income Amortization Adjustment	f
transaction_type_enum	40	Buy Down Fee	Buy Down Fee	f
transaction_type_enum	41	Buy Down Fee Adjustment	Buy Down Fee Adjustment	f
transaction_type_enum	42	Buy Down Fee Amortization	Buy Down Fee Amortization	f
transaction_type_enum	43	Buy Down Fee Amortization Adjustment	Buy Down Fee Amortization Adjustment	f
processing_result_enum	4	underProcessing	Under Processing	f
processing_result_enum	5	error	Error	f
transaction_type_enum	28	Down Payment	Down Payment	f
transaction_type_enum	34	Accrual Adjustment	Accrual Adjustment	f
transaction_type_enum	35	Capitalized Income	Capitalized Income	f
transaction_type_enum	38	Contract Termination	Contract Termination	f
\.


ALTER TABLE public.r_enum_value 

--
-- Data for Name: ref_loan_transaction_processing_strategy; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.ref_loan_transaction_processing_strategy 

COPY public.ref_loan_transaction_processing_strategy (id, code, name, sort_order) FROM stdin;
1	mifos-standard-strategy	Penalties, Fees, Interest, Principal order	1
2	heavensfamily-strategy	HeavensFamily Unique	6
3	creocore-strategy	Creocore Unique	7
4	rbi-india-strategy	Overdue/Due Fee/Int,Principal	2
5	principal-interest-penalties-fees-order-strategy	Principal, Interest, Penalties, Fees Order	3
6	interest-principal-penalties-fees-order-strategy	Interest, Principal, Penalties, Fees Order	4
7	early-repayment-strategy	Early Repayment Strategy	5
8	due-penalty-fee-interest-principal-in-advance-principal-penalty-fee-interest-strategy	Due penalty, fee, interest, principal, In advance principal, penalty, fee, interest	8
9	due-penalty-interest-principal-fee-in-advance-penalty-interest-principal-fee-strategy	Due penalty, interest, principal, fee, In advance penalty, interest, principal, fee	9
\.


ALTER TABLE public.ref_loan_transaction_processing_strategy 

--
-- Data for Name: request_audit_table; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.request_audit_table 

COPY public.request_audit_table (id, lastname, username, mobile_number, firstname, authentication_token, password, email, client_id, created_date, account_number) FROM stdin;
\.


ALTER TABLE public.request_audit_table 

--
-- Data for Name: rpt_sequence; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.rpt_sequence 

COPY public.rpt_sequence (id) FROM stdin;
\.


ALTER TABLE public.rpt_sequence 

--
-- Data for Name: scheduled_email_campaign; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.scheduled_email_campaign 

COPY public.scheduled_email_campaign (id, campaign_name, campaign_type, business_rule_id, param_value, status_enum, email_subject, email_message, email_attachment_file_format, stretchy_report_id, stretchy_report_param_map, closedon_date, closedon_userid, submittedon_date, submittedon_userid, approvedon_date, approvedon_userid, recurrence, next_trigger_date, last_trigger_date, recurrence_start_date, is_visible, previous_run_error_log, previous_run_error_message, previous_run_status) FROM stdin;
\.


ALTER TABLE public.scheduled_email_campaign 

--
-- Data for Name: scheduled_email_configuration; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.scheduled_email_configuration 

COPY public.scheduled_email_configuration (id, name, value) FROM stdin;
1	SMTP_SERVER	\N
2	SMTP_PORT	\N
3	SMTP_USERNAME	\N
4	SMTP_PASSWORD	\N
\.


ALTER TABLE public.scheduled_email_configuration 

--
-- Data for Name: scheduled_email_messages_outbound; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.scheduled_email_messages_outbound 

COPY public.scheduled_email_messages_outbound (id, group_id, client_id, staff_id, email_campaign_id, status_enum, email_address, email_subject, message, campaign_name, submittedon_date, error_message) FROM stdin;
\.


ALTER TABLE public.scheduled_email_messages_outbound 

--
-- Data for Name: scheduler_detail; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.scheduler_detail 

COPY public.scheduler_detail (id, is_suspended, execute_misfired_jobs, reset_scheduler_on_bootup) FROM stdin;
1	f	t	t
\.


ALTER TABLE public.scheduler_detail 

--
-- Data for Name: sms_campaign; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.sms_campaign 

COPY public.sms_campaign (id, campaign_name, campaign_type, campaign_trigger_type, report_id, provider_id, param_value, status_enum, message, submittedon_date, submittedon_userid, approvedon_date, approvedon_userid, closedon_date, closedon_userid, recurrence, next_trigger_date, last_trigger_date, recurrence_start_date, is_visible, is_notification) FROM stdin;
\.


ALTER TABLE public.sms_campaign 

--
-- Data for Name: sms_messages_outbound; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.sms_messages_outbound 

COPY public.sms_messages_outbound (id, group_id, client_id, staff_id, status_enum, mobile_no, message, campaign_id, external_id, submittedon_date, delivered_on_date, is_notification) FROM stdin;
\.


ALTER TABLE public.sms_messages_outbound 

--
-- Data for Name: stretchy_parameter; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.stretchy_parameter 

COPY public.stretchy_parameter (id, parameter_name, parameter_variable, parameter_label, "parameter_displayType", "parameter_FormatType", parameter_default, special, "selectOne", "selectAll", parameter_sql, parent_id) FROM stdin;
1	startDateSelect	startDate	startDate	date	date	today	\N	\N	\N	\N	\N
2	endDateSelect	endDate	endDate	date	date	today	\N	\N	\N	\N	\N
3	obligDateTypeSelect	obligDateType	obligDateType	select	number	0	\N	\N	\N	select * from  (select 1 as id, 'Closed' AS name union all select 2, 'Disbursal' ) x  order by x.id	\N
5	OfficeIdSelectOne	officeId	Office	select	number	0	\N	Y	\N	select id, concat(substring('........................................', 1,        ((LENGTH(hierarchy) - LENGTH(REPLACE(hierarchy, '.', '')) - 1) * 4)),      name) as tc  from m_office  where hierarchy like concat('${currentUserHierarchy}', '%')  order by hierarchy	\N
6	loanOfficerIdSelectAll	loanOfficerId	Loan Officer	select	number	0	\N	\N	Y	(select lo.id, lo.display_name AS name   from m_office o join m_office ounder on ounder.hierarchy like concat(o.hierarchy, '%')  join m_staff lo on lo.office_id = ounder.id  where lo.is_loan_officer = true  and o.id = '${officeId}')  union all  (select -10, '-')  order by 2	5
10	currencyIdSelectAll	currencyId	Currency	select	number	0	\N	\N	Y	select code, name  from m_organisation_currency  order by code	\N
20	fundIdSelectAll	fundId	Fund	select	number	0	\N	\N	Y	(select id, name  from m_fund)  union all  (select -10, '-')  order by 2	\N
26	loanPurposeIdSelectAll	loanPurposeId	Loan Purpose	select	number	0	\N	\N	Y	select -10 as id, '-' as code_value  union all  select * from (select v.id, v.code_value  from m_code c  join m_code_value v on v.code_id = c.id  where c.code_name = 'loanPurpose'  order by v.order_position)  x	\N
100	parTypeSelect	parType	parType	select	number	0	\N	\N	\N	select * from  (select 1 as id, 'Principal Only' AS name union all  select 2, 'Principal + Interest' union all select 3, 'Principal + Interest + Fees' union all  select 4, 'Principal + Interest + Fees + Penalties') x  order by x.id	\N
1004	selectAccount	accountNo	Enter Account No	text	string	n/a	\N	\N	\N	\N	\N
1005	savingsProductIdSelectAll	savingsProductId	Product	select	number	0	\N	\N	Y	select p.id, p.name  from m_savings_product p  order by 2	\N
1006	transactionId	transactionId	transactionId	text	string	n/a	\N	\N	\N	\N	\N
1007	selectCenterId	centerId	Enter Center Id	text	string	n/a	\N	\N	\N	\N	\N
1008	SelectGLAccountNO	GLAccountNO	GLAccountNO	select	number	0	\N	\N	\N	select id aid,name aname  from acc_gl_account	\N
1009	asOnDate	asOn	As On	date	date	today	\N	\N	\N	\N	\N
1010	SavingsAccountSubStatus	subStatus	SavingsAccountDormancyStatus	select	number	100	\N	\N	\N	select * from  (select 100 as id, 'Inactive' as name  union all  select 200 as id, 'Dormant' as  name union all   select 300 as id, 'Escheat' as name) x  order by x.id	\N
1011	cycleXSelect	cycleX	Cycle X Number	text	number	n/a	\N	\N	\N	\N	\N
1012	cycleYSelect	cycleY	Cycle Y Number	text	number	n/a	\N	\N	\N	\N	\N
1013	fromXSelect	fromX	From X Number	text	number	n/a	\N	\N	\N	\N	\N
1014	toYSelect	toY	To Y Number	text	number	n/a	\N	\N	\N	\N	\N
1015	overdueXSelect	overdueX	Overdue X Number	text	number	n/a	\N	\N	\N	\N	\N
1016	overdueYSelect	overdueY	Overdue Y Number	text	number	n/a	\N	\N	\N	\N	\N
1017	DefaultLoan	loanId	Loan	none	number	-1	\N	\N	Y	select ml.id  from m_loan ml  left join m_client mc on mc.id = ml.client_id  left join m_office mo on mo.id = mc.office_id  where mo.id = '${officeId}' or '${officeId}' = -1	5
1018	DefaultClient	clientId	Client	none	number	-1	\N	\N	Y	select mc.id  from m_client mc  left join m_office mo on mc.office_id = mo.id  where mo.id = '${officeId}' or '${officeId}' = -1	5
1019	DefaultGroup	groupId	Group	none	number	-1	\N	\N	Y	select mg.id  from m_group mg left join m_office mo on mg.office_id = mo.id where mo.id = '${officeId}' or '${officeId}' = -1	5
1020	SelectLoanType	loanType	Loan Type	select	number	-1	\N	\N	Y	select enum_id as id, enum_value as value from r_enum_value where enum_name = 'loan_type_enum'	\N
1021	DefaultSavings	savingsId	Savings	none	number	-1	\N	\N	Y	\N	5
1022	DefaultSavingsTransactionId	savingsTransactionId	Savings Transaction	none	number	-1	\N	\N	Y	\N	5
25	loanProductIdSelectAll	loanProductId	Product	select	number	0	\N	\N	Y	select p.id, p.name\r\nfrom m_product_loan p\r\nwhere (p.currency_code = '${currencyId}' or '-1'= '${currencyId}')\r\norder by 2	10
1002	FullParameterList	\N	n/a	n/a	n/a	n/a	Y	\N	\N	select sp.parameter_name, sp.parameter_variable, sp.parameter_label, sp."parameter_displayType", sp."parameter_FormatType", sp.parameter_default, sp."selectOne", sp."selectAll", spp.parameter_name as parentParameterName from stretchy_parameter sp  left join stretchy_parameter spp on spp.id = sp.parent_id  where sp.special is null  and exists     (select 'f'    from stretchy_report sr    join stretchy_report_parameter srp on srp.report_id = sr.id    where sr.report_name in(${reportListing})    and srp.parameter_id = sp.id   ) order by sp.id	\N
1003	reportCategoryList	\N	n/a	n/a	n/a	n/a	Y	\N	\N	select  r.id as report_id, r.report_name, r.report_type, r.report_subtype, r.report_category,\n  rp.id as parameter_id, rp.report_parameter_name, p.parameter_name\n  from stretchy_report r\n  left join stretchy_report_parameter rp on rp.report_id = r.id\n  left join stretchy_parameter p on p.id = rp.parameter_id\n  where r.report_category = '${reportCategory}'\n  and r.use_report is true\n  and exists\n  (select 'f'\n  from m_appuser_role ur \n  join m_role rr on rr.id = ur.role_id\n  join m_role_permission rp on rp.role_id = rr.id\n  join m_permission p on p.id = rp.permission_id\n  where ur.appuser_id = ${currentUserId}\n  and (p.code in ('ALL_FUNCTIONS_READ', 'ALL_FUNCTIONS') or p.code = concat('READ_', r.report_name)) )\n  order by r.report_category, r.report_name, rp.id	\N
1001	FullReportList	\N	n/a	n/a	n/a	n/a	Y	\N	\N	select  r.id as report_id, r.report_name, r.report_type, r.report_subtype, r.report_category,\nrp.id as parameter_id, rp.report_parameter_name, p.parameter_name\n  from stretchy_report r\n  left join stretchy_report_parameter rp on rp.report_id = r.id \n  left join stretchy_parameter p on p.id = rp.parameter_id\n  where r.use_report is true\n  and exists\n  ( select 'f'\n  from m_appuser_role ur \n  join m_role rr on rr.id = ur.role_id\n  join m_role_permission rp on rp.role_id = rr.id\n  join m_permission p on p.id = rp.permission_id\n  where ur.appuser_id = ${currentUserId}\n  and (p.code in ('ALL_FUNCTIONS_READ', 'ALL_FUNCTIONS') or p.code = concat('READ_', r.report_name)) )\n  order by r.report_category, r.report_name, rp.id	\N
\.


ALTER TABLE public.stretchy_parameter 

--
-- Data for Name: stretchy_report_parameter; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.stretchy_report_parameter 

COPY public.stretchy_report_parameter (id, report_id, parameter_id, report_parameter_name) FROM stdin;
1	1	5	\N
2	2	5	\N
3	2	6	\N
4	2	10	\N
5	2	20	\N
6	2	25	\N
7	2	26	\N
8	5	5	\N
9	5	6	\N
10	5	10	\N
11	5	20	\N
12	5	25	\N
13	5	26	\N
14	6	5	\N
15	6	6	\N
16	6	10	\N
17	6	20	\N
18	6	25	\N
19	6	26	\N
20	7	5	\N
21	7	6	\N
22	7	10	\N
23	7	20	\N
24	7	25	\N
25	7	26	\N
26	8	5	\N
27	8	6	\N
28	8	10	\N
29	8	25	\N
30	8	26	\N
31	11	5	\N
32	11	6	\N
33	11	10	\N
34	11	20	\N
35	11	25	\N
36	11	26	\N
37	11	100	\N
38	12	5	\N
39	12	6	\N
40	12	10	\N
41	12	20	\N
42	12	25	\N
43	12	26	\N
44	13	1	\N
45	13	2	\N
46	13	3	\N
47	13	5	\N
48	13	6	\N
49	13	10	\N
50	13	20	\N
51	13	25	\N
52	13	26	\N
53	14	1	\N
54	14	2	\N
55	14	3	\N
56	14	5	\N
57	14	6	\N
58	14	10	\N
59	14	20	\N
60	14	25	\N
61	14	26	\N
62	15	5	\N
63	15	6	\N
64	15	10	\N
65	15	20	\N
66	15	25	\N
67	15	26	\N
68	15	100	\N
69	16	5	\N
70	16	6	\N
71	16	10	\N
72	16	20	\N
73	16	25	\N
74	16	26	\N
75	16	100	\N
76	20	1	\N
77	20	2	\N
78	20	10	\N
79	20	20	\N
80	21	1	\N
81	21	2	\N
82	21	5	\N
83	21	10	\N
84	21	20	\N
93	51	1	\N
94	51	2	\N
95	51	5	\N
96	51	10	\N
97	51	25	\N
98	52	5	\N
99	53	5	\N
100	53	10	\N
101	54	1	\N
102	54	2	\N
103	54	5	\N
104	54	10	\N
105	54	25	\N
106	55	5	\N
107	55	6	\N
108	55	10	\N
109	55	20	\N
110	55	25	\N
111	55	26	\N
112	56	5	\N
113	56	6	\N
114	56	10	\N
115	56	20	\N
116	56	25	\N
117	56	26	\N
118	56	100	\N
119	57	5	\N
120	57	6	\N
121	57	10	\N
122	57	20	\N
123	57	25	\N
124	57	26	\N
125	58	5	\N
126	58	6	\N
127	58	10	\N
128	58	20	\N
129	58	25	\N
130	58	26	\N
131	58	100	\N
132	59	1	\N
133	59	2	\N
134	59	5	\N
135	59	6	\N
136	59	10	\N
137	59	20	\N
138	59	25	\N
139	59	26	\N
140	61	5	\N
141	61	10	\N
145	93	1	\N
146	93	2	\N
147	93	5	\N
148	93	6	\N
256	106	2	\N
257	106	6	\N
258	106	5	\N
259	106	1	\N
418	149	5	
419	150	5	
420	151	5	
421	152	5	
422	153	5	
423	154	5	
424	155	5	
425	156	5	
441	165	1010	\N
442	165	5	\N
443	166	5	officeId
444	166	6	loanOfficerId
445	167	5	officeId
446	167	6	loanOfficerId
447	168	5	officeId
448	168	6	loanOfficerId
449	168	1011	cycleX
450	168	1012	cycleY
451	169	5	officeId
452	169	6	loanOfficerId
453	169	1013	fromX
454	169	1014	toY
455	170	5	officeId
456	170	6	loanOfficerId
457	170	1013	fromX
458	170	1014	toY
459	171	5	officeId
460	171	6	loanOfficerId
461	172	5	officeId
462	172	6	loanOfficerId
463	173	5	officeId
464	173	6	loanOfficerId
465	173	1013	fromX
466	173	1014	toY
467	173	1015	overdueX
468	173	1016	overdueY
469	174	5	officeId
470	174	6	loanOfficerId
471	174	1013	fromX
472	174	1014	toY
473	175	5	officeId
474	175	6	loanOfficerId
475	175	1013	fromX
476	175	1014	toY
477	175	1015	overdueX
478	175	1016	overdueY
479	176	5	officeId
480	176	6	loanOfficerId
481	177	5	officeId
482	177	6	loanOfficerId
483	177	1013	fromX
484	177	1014	toY
485	178	5	officeId
486	178	6	loanOfficerId
487	178	1013	fromX
488	178	1014	toY
489	181	5	officeId
490	180	5	officeId
491	179	5	officeId
492	181	6	loanOfficerId
493	180	6	loanOfficerId
494	179	6	loanOfficerId
495	181	1017	loanId
496	180	1017	loanId
497	181	1018	clientId
498	180	1018	clientId
499	181	1019	groupId
500	180	1019	groupId
501	181	1020	loanType
502	180	1020	loanType
503	179	1020	loanType
504	182	5	officeId
505	183	5	officeId
506	182	6	loanOfficerId
507	183	6	loanOfficerId
508	182	1018	clientId
509	183	1018	clientId
510	184	5	officeId
511	184	6	loanOfficerId
512	184	1021	savingsId
513	185	5	officeId
514	185	6	loanOfficerId
515	185	1021	savingsId
516	186	5	officeId
517	186	6	loanOfficerId
518	186	1022	savingsTransactionId
519	187	5	officeId
520	187	6	loanOfficerId
521	187	1022	savingsTransactionId
522	193	1	fromDate
523	193	5	branch
524	193	2	toDate
525	194	1008	GLAccountNO
526	194	5	officeId
527	194	2	endDate
528	194	1	startDate
529	196	5	branch
530	196	2	date
531	195	5	branch
532	195	1	fromDate
533	195	2	toDate
534	197	5	officeId
535	197	2	endDate
536	198	5	officeId
537	198	2	endDate
538	199	2	officeId
539	199	5	endDate
540	200	2	officeId
541	200	5	endDate
\.


ALTER TABLE public.stretchy_report_parameter 

--
-- Data for Name: twofactor_access_token; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.twofactor_access_token 

COPY public.twofactor_access_token (id, token, appuser_id, valid_from, valid_to, enabled) FROM stdin;
\.


ALTER TABLE public.twofactor_access_token 

--
-- Data for Name: twofactor_configuration; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.twofactor_configuration 

COPY public.twofactor_configuration (id, name, value) FROM stdin;
1	otp-delivery-email-enable	true
2	otp-delivery-email-subject	Fineract Two-Factor Authentication Token
3	otp-delivery-email-body	Hello {{username}}.\nYour OTP login token is {{token}}.
4	otp-delivery-sms-enable	false
5	otp-delivery-sms-provider	1
6	otp-delivery-sms-text	Your authentication token for Fineract is {{token}}.
7	otp-token-live-time	300
8	otp-token-length	5
9	access-token-live-time	86400
10	access-token-live-time-extended	604800
\.


ALTER TABLE public.twofactor_configuration 

--
-- Data for Name: x_table_column_code_mappings; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.x_table_column_code_mappings 

COPY public.x_table_column_code_mappings (column_alias_name, code_id) FROM stdin;
\.


ALTER TABLE public.x_table_column_code_mappings 

--
-- Name: acc_accounting_rule_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.acc_accounting_rule_id_seq', 1, false);


--
-- Name: acc_gl_account_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.acc_gl_account_id_seq', 1, false);


--
-- Name: acc_gl_closure_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.acc_gl_closure_id_seq', 1, false);


--
-- Name: acc_gl_financial_activity_account_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.acc_gl_financial_activity_account_id_seq', 1, false);


--
-- Name: acc_gl_journal_entry_annual_summary_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.acc_gl_journal_entry_annual_summary_id_seq', 1, false);


--
-- Name: acc_gl_journal_entry_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.acc_gl_journal_entry_id_seq', 1, false);


--
-- Name: acc_product_mapping_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.acc_product_mapping_id_seq', 1, false);


--
-- Name: acc_rule_tags_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.acc_rule_tags_id_seq', 1, false);


--
-- Name: batch_custom_job_parameters_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.batch_custom_job_parameters_id_seq', 1, false);


--
-- Name: batch_job_execution_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.batch_job_execution_seq', 3, true);


--
-- Name: batch_job_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.batch_job_seq', 3, true);


--
-- Name: batch_step_execution_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.batch_step_execution_seq', 3, true);


--
-- Name: c_account_number_format_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.c_account_number_format_id_seq', 1, false);


--
-- Name: c_cache_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.c_cache_id_seq', 2, false);


--
-- Name: c_configuration_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.c_configuration_id_seq', 79, true);


--
-- Name: c_external_service_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.c_external_service_id_seq', 5, false);


--
-- Name: glim_accounts_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.glim_accounts_id_seq', 1, false);


--
-- Name: gsim_accounts_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.gsim_accounts_id_seq', 1, false);


--
-- Name: interop_identifier_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.interop_identifier_id_seq', 1, false);


--
-- Name: job_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.job_id_seq', 43, true);


--
-- Name: job_parameters_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.job_parameters_id_seq', 6, false);


--
-- Name: job_run_history_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.job_run_history_id_seq', 3, true);


--
-- Name: m_account_transfer_details_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.m_account_transfer_details_id_seq', 1, false);


--
-- Name: m_account_transfer_standing_instructions_history_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.m_account_transfer_standing_instructions_history_id_seq', 1, false);


--
-- Name: m_account_transfer_standing_instructions_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.m_account_transfer_standing_instructions_id_seq', 1, false);


--
-- Name: m_account_transfer_transaction_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.m_account_transfer_transaction_id_seq', 1, false);


--
-- Name: m_address_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.m_address_id_seq', 1, false);


--
-- Name: m_adhoc_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.m_adhoc_id_seq', 1, false);


--
-- Name: m_appuser_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.m_appuser_id_seq', 4, false);


--
-- Name: m_appuser_previous_password_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.m_appuser_previous_password_id_seq', 1, false);


--
-- Name: m_batch_business_steps_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.m_batch_business_steps_id_seq', 12, true);


--
-- Name: m_business_date_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.m_business_date_id_seq', 1, false);


--
-- Name: m_calendar_history_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.m_calendar_history_id_seq', 1, false);


--
-- Name: m_calendar_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.m_calendar_id_seq', 1, false);


--
-- Name: m_calendar_instance_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.m_calendar_instance_id_seq', 1, false);


--
-- Name: m_cashier_transactions_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.m_cashier_transactions_id_seq', 1, false);


--
-- Name: m_cashiers_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.m_cashiers_id_seq', 1, false);


--
-- Name: m_charge_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.m_charge_id_seq', 1, false);


--
-- Name: m_client_address_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.m_client_address_id_seq', 1, false);


--
-- Name: m_client_attendance_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.m_client_attendance_id_seq', 1, false);


--
-- Name: m_client_charge_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.m_client_charge_id_seq', 1, false);


--
-- Name: m_client_charge_paid_by_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.m_client_charge_paid_by_id_seq', 1, false);


--
-- Name: m_client_collateral_management_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.m_client_collateral_management_id_seq', 1, false);


--
-- Name: m_client_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.m_client_id_seq', 1, false);


--
-- Name: m_client_identifier_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.m_client_identifier_id_seq', 1, false);


--
-- Name: m_client_non_person_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.m_client_non_person_id_seq', 1, false);


--
-- Name: m_client_transaction_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.m_client_transaction_id_seq', 1, false);


--
-- Name: m_client_transfer_details_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.m_client_transfer_details_id_seq', 1, false);


--
-- Name: m_code_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.m_code_id_seq', 49, true);


--
-- Name: m_code_value_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.m_code_value_id_seq', 22, true);


--
-- Name: m_collateral_management_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.m_collateral_management_id_seq', 1, false);


--
-- Name: m_command_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.m_command_id_seq', 1, false);


--
-- Name: m_creditbureau_configuration_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.m_creditbureau_configuration_id_seq', 9, false);


--
-- Name: m_creditbureau_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.m_creditbureau_id_seq', 2, false);


--
-- Name: m_creditbureau_loanproduct_mapping_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.m_creditbureau_loanproduct_mapping_id_seq', 1, false);


--
-- Name: m_creditbureau_token_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.m_creditbureau_token_id_seq', 1, false);


--
-- Name: m_creditreport_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.m_creditreport_id_seq', 1, false);


--
-- Name: m_currency_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.m_currency_id_seq', 165, false);


--
-- Name: m_delinquency_bucket_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.m_delinquency_bucket_id_seq', 1, false);


--
-- Name: m_delinquency_bucket_mappings_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.m_delinquency_bucket_mappings_id_seq', 1, false);


--
-- Name: m_delinquency_range_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.m_delinquency_range_id_seq', 1, false);


--
-- Name: m_deposit_account_on_hold_transaction_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.m_deposit_account_on_hold_transaction_id_seq', 1, false);


--
-- Name: m_deposit_account_recurring_detail_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.m_deposit_account_recurring_detail_id_seq', 1, false);


--
-- Name: m_deposit_account_term_and_preclosure_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.m_deposit_account_term_and_preclosure_id_seq', 1, false);


--
-- Name: m_deposit_product_recurring_detail_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.m_deposit_product_recurring_detail_id_seq', 1, false);


--
-- Name: m_deposit_product_term_and_preclosure_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.m_deposit_product_term_and_preclosure_id_seq', 1, false);


--
-- Name: m_document_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.m_document_id_seq', 1, false);


--
-- Name: m_entity_datatable_check_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.m_entity_datatable_check_id_seq', 1, false);


--
-- Name: m_entity_relation_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.m_entity_relation_id_seq', 6, false);


--
-- Name: m_entity_to_entity_access_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.m_entity_to_entity_access_id_seq', 1, false);


--
-- Name: m_entity_to_entity_mapping_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.m_entity_to_entity_mapping_id_seq', 1, false);


--
-- Name: m_external_asset_owner_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.m_external_asset_owner_id_seq', 1, false);


--
-- Name: m_external_asset_owner_journal_entry_mapping_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.m_external_asset_owner_journal_entry_mapping_id_seq', 1, false);


--
-- Name: m_external_asset_owner_loan_product_configurable_attribu_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.m_external_asset_owner_loan_product_configurable_attribu_id_seq', 1, false);


--
-- Name: m_external_asset_owner_transfer_details_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.m_external_asset_owner_transfer_details_id_seq', 1, false);


--
-- Name: m_external_asset_owner_transfer_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.m_external_asset_owner_transfer_id_seq', 1, false);


--
-- Name: m_external_asset_owner_transfer_journal_entry_mapping_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.m_external_asset_owner_transfer_journal_entry_mapping_id_seq', 1, false);


--
-- Name: m_external_asset_owner_transfer_loan_mapping_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.m_external_asset_owner_transfer_loan_mapping_id_seq', 1, false);


--
-- Name: m_external_event_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.m_external_event_id_seq', 1, false);


--
-- Name: m_family_members_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.m_family_members_id_seq', 1, false);


--
-- Name: m_field_configuration_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.m_field_configuration_id_seq', 19, false);


--
-- Name: m_floating_rates_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.m_floating_rates_id_seq', 1, false);


--
-- Name: m_floating_rates_periods_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.m_floating_rates_periods_id_seq', 1, false);


--
-- Name: m_fund_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.m_fund_id_seq', 1, false);


--
-- Name: m_group_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.m_group_id_seq', 1, false);


--
-- Name: m_group_level_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.m_group_level_id_seq', 3, false);


--
-- Name: m_group_roles_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.m_group_roles_id_seq', 1, false);


--
-- Name: m_guarantor_funding_details_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.m_guarantor_funding_details_id_seq', 1, false);


--
-- Name: m_guarantor_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.m_guarantor_id_seq', 1, false);


--
-- Name: m_guarantor_transaction_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.m_guarantor_transaction_id_seq', 1, false);


--
-- Name: m_holiday_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.m_holiday_id_seq', 1, false);


--
-- Name: m_hook_configuration_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.m_hook_configuration_id_seq', 1, false);


--
-- Name: m_hook_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.m_hook_id_seq', 1, false);


--
-- Name: m_hook_registered_events_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.m_hook_registered_events_id_seq', 1, false);


--
-- Name: m_hook_schema_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.m_hook_schema_id_seq', 12, false);


--
-- Name: m_hook_templates_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.m_hook_templates_id_seq', 5, false);


--
-- Name: m_image_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.m_image_id_seq', 1, false);


--
-- Name: m_import_document_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.m_import_document_id_seq', 1, false);


--
-- Name: m_interest_incentives_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.m_interest_incentives_id_seq', 1, false);


--
-- Name: m_interest_rate_chart_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.m_interest_rate_chart_id_seq', 1, false);


--
-- Name: m_interest_rate_slab_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.m_interest_rate_slab_id_seq', 1, false);


--
-- Name: m_journal_entry_aggregation_summary_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.m_journal_entry_aggregation_summary_id_seq', 1, false);


--
-- Name: m_journal_entry_aggregation_tracking_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.m_journal_entry_aggregation_tracking_id_seq', 1, false);


--
-- Name: m_loan_amortization_allocation_mapping_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.m_loan_amortization_allocation_mapping_id_seq', 1, false);


--
-- Name: m_loan_approved_amount_history_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.m_loan_approved_amount_history_id_seq', 1, false);


--
-- Name: m_loan_arrears_aging_loan_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.m_loan_arrears_aging_loan_id_seq', 1, false);


--
-- Name: m_loan_buy_down_fee_balance_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.m_loan_buy_down_fee_balance_id_seq', 1, false);


--
-- Name: m_loan_capitalized_income_balance_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.m_loan_capitalized_income_balance_id_seq', 1, false);


--
-- Name: m_loan_charge_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.m_loan_charge_id_seq', 1, false);


--
-- Name: m_loan_charge_paid_by_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.m_loan_charge_paid_by_id_seq', 1, false);


--
-- Name: m_loan_charge_tax_details_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.m_loan_charge_tax_details_id_seq', 1, false);


--
-- Name: m_loan_collateral_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.m_loan_collateral_id_seq', 1, false);


--
-- Name: m_loan_collateral_management_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.m_loan_collateral_management_id_seq', 1, false);


--
-- Name: m_loan_credit_allocation_rule_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.m_loan_credit_allocation_rule_id_seq', 1, false);


--
-- Name: m_loan_delinquency_action_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.m_loan_delinquency_action_id_seq', 1, false);


--
-- Name: m_loan_delinquency_tag_history_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.m_loan_delinquency_tag_history_id_seq', 1, false);


--
-- Name: m_loan_disbursement_detail_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.m_loan_disbursement_detail_id_seq', 1, false);


--
-- Name: m_loan_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.m_loan_id_seq', 1, false);


--
-- Name: m_loan_installment_charge_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.m_loan_installment_charge_id_seq', 1, false);


--
-- Name: m_loan_installment_delinquency_tag_history_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.m_loan_installment_delinquency_tag_history_id_seq', 1, false);


--
-- Name: m_loan_interest_recalculation_additional_details_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.m_loan_interest_recalculation_additional_details_id_seq', 1, false);


--
-- Name: m_loan_officer_assignment_history_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.m_loan_officer_assignment_history_id_seq', 1, false);


--
-- Name: m_loan_originator_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.m_loan_originator_id_seq', 1, false);


--
-- Name: m_loan_originator_mapping_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.m_loan_originator_mapping_id_seq', 1, false);


--
-- Name: m_loan_overdue_installment_charge_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.m_loan_overdue_installment_charge_id_seq', 1, false);


--
-- Name: m_loan_payment_allocation_rule_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.m_loan_payment_allocation_rule_id_seq', 1, false);


--
-- Name: m_loan_product_credit_allocation_rule_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.m_loan_product_credit_allocation_rule_id_seq', 1, false);


--
-- Name: m_loan_product_payment_allocation_rule_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.m_loan_product_payment_allocation_rule_id_seq', 1, false);


--
-- Name: m_loan_progressive_model_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.m_loan_progressive_model_id_seq', 1, false);


--
-- Name: m_loan_reage_parameter_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.m_loan_reage_parameter_id_seq', 1, false);


--
-- Name: m_loan_reamortization_parameter_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.m_loan_reamortization_parameter_id_seq', 1, false);


--
-- Name: m_loan_recalculation_details_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.m_loan_recalculation_details_id_seq', 1, false);


--
-- Name: m_loan_repayment_schedule_history_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.m_loan_repayment_schedule_history_id_seq', 1, false);


--
-- Name: m_loan_repayment_schedule_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.m_loan_repayment_schedule_id_seq', 1, false);


--
-- Name: m_loan_reschedule_request_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.m_loan_reschedule_request_id_seq', 1, false);


--
-- Name: m_loan_reschedule_request_term_variations_mapping_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.m_loan_reschedule_request_term_variations_mapping_id_seq', 1, false);


--
-- Name: m_loan_status_change_history_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.m_loan_status_change_history_id_seq', 1, false);


--
-- Name: m_loan_term_variations_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.m_loan_term_variations_id_seq', 1, false);


--
-- Name: m_loan_topup_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.m_loan_topup_id_seq', 1, false);


--
-- Name: m_loan_tranche_charges_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.m_loan_tranche_charges_id_seq', 1, false);


--
-- Name: m_loan_tranche_disbursement_charge_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.m_loan_tranche_disbursement_charge_id_seq', 1, false);


--
-- Name: m_loan_transaction_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.m_loan_transaction_id_seq', 1, false);


--
-- Name: m_loan_transaction_relation_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.m_loan_transaction_relation_id_seq', 1, false);


--
-- Name: m_loan_transaction_repayment_schedule_mapping_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.m_loan_transaction_repayment_schedule_mapping_id_seq', 1, false);


--
-- Name: m_loanproduct_provisioning_entry_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.m_loanproduct_provisioning_entry_id_seq', 1, false);


--
-- Name: m_loanproduct_provisioning_mapping_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.m_loanproduct_provisioning_mapping_id_seq', 1, false);


--
-- Name: m_mandatory_savings_schedule_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.m_mandatory_savings_schedule_id_seq', 1, false);


--
-- Name: m_meeting_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.m_meeting_id_seq', 1, false);


--
-- Name: m_note_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.m_note_id_seq', 1, false);


--
-- Name: m_office_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.m_office_id_seq', 2, false);


--
-- Name: m_office_transaction_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.m_office_transaction_id_seq', 1, false);


--
-- Name: m_organisation_creditbureau_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.m_organisation_creditbureau_id_seq', 1, false);


--
-- Name: m_organisation_currency_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.m_organisation_currency_id_seq', 22, false);


--
-- Name: m_password_validation_policy_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.m_password_validation_policy_id_seq', 3, true);


--
-- Name: m_payment_detail_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.m_payment_detail_id_seq', 1, false);


--
-- Name: m_payment_type_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.m_payment_type_id_seq', 3, true);


--
-- Name: m_permission_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.m_permission_id_seq', 1010, true);


--
-- Name: m_portfolio_account_associations_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.m_portfolio_account_associations_id_seq', 1, false);


--
-- Name: m_portfolio_command_source_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.m_portfolio_command_source_id_seq', 1, false);


--
-- Name: m_product_loan_configurable_attributes_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.m_product_loan_configurable_attributes_id_seq', 1, false);


--
-- Name: m_product_loan_floating_rates_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.m_product_loan_floating_rates_id_seq', 1, false);


--
-- Name: m_product_loan_guarantee_details_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.m_product_loan_guarantee_details_id_seq', 1, false);


--
-- Name: m_product_loan_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.m_product_loan_id_seq', 1, false);


--
-- Name: m_product_loan_recalculation_details_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.m_product_loan_recalculation_details_id_seq', 1, false);


--
-- Name: m_product_loan_variable_installment_config_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.m_product_loan_variable_installment_config_id_seq', 1, false);


--
-- Name: m_product_loan_variations_borrower_cycle_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.m_product_loan_variations_borrower_cycle_id_seq', 1, false);


--
-- Name: m_product_mix_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.m_product_mix_id_seq', 1, false);


--
-- Name: m_provision_category_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.m_provision_category_id_seq', 5, false);


--
-- Name: m_provisioning_criteria_definition_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.m_provisioning_criteria_definition_id_seq', 1, false);


--
-- Name: m_provisioning_criteria_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.m_provisioning_criteria_id_seq', 1, false);


--
-- Name: m_provisioning_history_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.m_provisioning_history_id_seq', 1, false);


--
-- Name: m_rate_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.m_rate_id_seq', 1, false);


--
-- Name: m_repayment_with_post_dated_checks_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.m_repayment_with_post_dated_checks_id_seq', 1, false);


--
-- Name: m_report_mailing_job_configuration_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.m_report_mailing_job_configuration_id_seq', 5, false);


--
-- Name: m_report_mailing_job_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.m_report_mailing_job_id_seq', 1, false);


--
-- Name: m_report_mailing_job_run_history_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.m_report_mailing_job_run_history_id_seq', 1, false);


--
-- Name: m_role_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.m_role_id_seq', 3, false);


--
-- Name: m_savings_account_charge_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.m_savings_account_charge_id_seq', 1, false);


--
-- Name: m_savings_account_charge_paid_by_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.m_savings_account_charge_paid_by_id_seq', 1, false);


--
-- Name: m_savings_account_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.m_savings_account_id_seq', 1, false);


--
-- Name: m_savings_account_interest_rate_chart_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.m_savings_account_interest_rate_chart_id_seq', 1, false);


--
-- Name: m_savings_account_interest_rate_slab_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.m_savings_account_interest_rate_slab_id_seq', 1, false);


--
-- Name: m_savings_account_transaction_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.m_savings_account_transaction_id_seq', 1, false);


--
-- Name: m_savings_account_transaction_tax_details_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.m_savings_account_transaction_tax_details_id_seq', 1, false);


--
-- Name: m_savings_interest_incentives_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.m_savings_interest_incentives_id_seq', 1, false);


--
-- Name: m_savings_officer_assignment_history_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.m_savings_officer_assignment_history_id_seq', 1, false);


--
-- Name: m_savings_product_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.m_savings_product_id_seq', 1, false);


--
-- Name: m_share_account_charge_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.m_share_account_charge_id_seq', 1, false);


--
-- Name: m_share_account_charge_paid_by_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.m_share_account_charge_paid_by_id_seq', 1, false);


--
-- Name: m_share_account_dividend_details_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.m_share_account_dividend_details_id_seq', 1, false);


--
-- Name: m_share_account_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.m_share_account_id_seq', 1, false);


--
-- Name: m_share_account_transactions_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.m_share_account_transactions_id_seq', 1, false);


--
-- Name: m_share_product_dividend_pay_out_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.m_share_product_dividend_pay_out_id_seq', 1, false);


--
-- Name: m_share_product_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.m_share_product_id_seq', 1, false);


--
-- Name: m_share_product_market_price_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.m_share_product_market_price_id_seq', 1, false);


--
-- Name: m_staff_assignment_history_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.m_staff_assignment_history_id_seq', 1, false);


--
-- Name: m_staff_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.m_staff_id_seq', 1, false);


--
-- Name: m_survey_components_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.m_survey_components_id_seq', 1, false);


--
-- Name: m_survey_lookup_tables_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.m_survey_lookup_tables_id_seq', 1, false);


--
-- Name: m_survey_questions_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.m_survey_questions_id_seq', 1, false);


--
-- Name: m_survey_responses_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.m_survey_responses_id_seq', 1, false);


--
-- Name: m_survey_scorecards_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.m_survey_scorecards_id_seq', 1, false);


--
-- Name: m_surveys_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.m_surveys_id_seq', 1, false);


--
-- Name: m_tax_component_history_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.m_tax_component_history_id_seq', 1, false);


--
-- Name: m_tax_component_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.m_tax_component_id_seq', 1, false);


--
-- Name: m_tax_group_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.m_tax_group_id_seq', 1, false);


--
-- Name: m_tax_group_mappings_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.m_tax_group_mappings_id_seq', 1, false);


--
-- Name: m_tellers_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.m_tellers_id_seq', 1, false);


--
-- Name: m_template_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.m_template_id_seq', 1, false);


--
-- Name: m_templatemappers_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.m_templatemappers_id_seq', 1, false);


--
-- Name: m_wc_breach_configuration_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.m_wc_breach_configuration_id_seq', 1, false);


--
-- Name: m_wc_delinquency_configuration_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.m_wc_delinquency_configuration_id_seq', 1, false);


--
-- Name: m_wc_loan_amortization_model_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.m_wc_loan_amortization_model_id_seq', 1, false);


--
-- Name: m_wc_loan_balance_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.m_wc_loan_balance_id_seq', 1, false);


--
-- Name: m_wc_loan_breach_action_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.m_wc_loan_breach_action_id_seq', 1, false);


--
-- Name: m_wc_loan_breach_reset_history_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.m_wc_loan_breach_reset_history_id_seq', 1, false);


--
-- Name: m_wc_loan_breach_schedule_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.m_wc_loan_breach_schedule_id_seq', 1, false);


--
-- Name: m_wc_loan_charge_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.m_wc_loan_charge_id_seq', 1, false);


--
-- Name: m_wc_loan_delinquency_action_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.m_wc_loan_delinquency_action_id_seq', 1, false);


--
-- Name: m_wc_loan_delinquency_range_schedule_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.m_wc_loan_delinquency_range_schedule_id_seq', 1, false);


--
-- Name: m_wc_loan_disbursement_detail_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.m_wc_loan_disbursement_detail_id_seq', 1, false);


--
-- Name: m_wc_loan_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.m_wc_loan_id_seq', 1, false);


--
-- Name: m_wc_loan_near_breach_action_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.m_wc_loan_near_breach_action_id_seq', 1, false);


--
-- Name: m_wc_loan_note_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.m_wc_loan_note_id_seq', 1, false);


--
-- Name: m_wc_loan_originator_mapping_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.m_wc_loan_originator_mapping_id_seq', 1, false);


--
-- Name: m_wc_loan_payment_allocation_rule_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.m_wc_loan_payment_allocation_rule_id_seq', 1, false);


--
-- Name: m_wc_loan_period_payment_rate_change_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.m_wc_loan_period_payment_rate_change_id_seq', 1, false);


--
-- Name: m_wc_loan_product_configurable_attributes_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.m_wc_loan_product_configurable_attributes_id_seq', 1, false);


--
-- Name: m_wc_loan_product_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.m_wc_loan_product_id_seq', 1, false);


--
-- Name: m_wc_loan_product_payment_allocation_rule_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.m_wc_loan_product_payment_allocation_rule_id_seq', 1, false);


--
-- Name: m_wc_loan_range_delinquency_tag_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.m_wc_loan_range_delinquency_tag_id_seq', 1, false);


--
-- Name: m_wc_loan_transaction_allocation_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.m_wc_loan_transaction_allocation_id_seq', 1, false);


--
-- Name: m_wc_loan_transaction_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.m_wc_loan_transaction_id_seq', 1, false);


--
-- Name: m_wc_loan_transaction_relation_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.m_wc_loan_transaction_relation_id_seq', 1, false);


--
-- Name: m_wc_near_breach_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.m_wc_near_breach_id_seq', 1, false);


--
-- Name: m_working_days_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.m_working_days_id_seq', 2, false);


--
-- Name: mix_taxonomy_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.mix_taxonomy_id_seq', 49, false);


--
-- Name: mix_taxonomy_mapping_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.mix_taxonomy_mapping_id_seq', 2, false);


--
-- Name: mix_xbrl_namespace_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.mix_xbrl_namespace_id_seq', 8, false);


--
-- Name: notification_generator_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.notification_generator_id_seq', 1, false);


--
-- Name: notification_mapper_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.notification_mapper_id_seq', 1, false);


--
-- Name: ppi_likelihoods_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.ppi_likelihoods_id_seq', 1, false);


--
-- Name: ppi_likelihoods_ppi_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.ppi_likelihoods_ppi_id_seq', 1, false);


--
-- Name: ppi_scores_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.ppi_scores_id_seq', 21, false);


--
-- Name: ref_loan_transaction_processing_strategy_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.ref_loan_transaction_processing_strategy_id_seq', 8, false);


--
-- Name: request_audit_table_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.request_audit_table_id_seq', 1, false);


--
-- Name: rpt_sequence_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.rpt_sequence_id_seq', 1, false);


--
-- Name: scheduled_email_campaign_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.scheduled_email_campaign_id_seq', 1, false);


--
-- Name: scheduled_email_configuration_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.scheduled_email_configuration_id_seq', 5, false);


--
-- Name: scheduled_email_messages_outbound_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.scheduled_email_messages_outbound_id_seq', 1, false);


--
-- Name: scheduler_detail_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.scheduler_detail_id_seq', 2, false);


--
-- Name: sms_campaign_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.sms_campaign_id_seq', 1, false);


--
-- Name: sms_messages_outbound_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.sms_messages_outbound_id_seq', 1, false);


--
-- Name: stretchy_parameter_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.stretchy_parameter_id_seq', 1023, false);


--
-- Name: stretchy_report_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.stretchy_report_id_seq', 200, true);


--
-- Name: stretchy_report_parameter_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.stretchy_report_parameter_id_seq', 541, true);


--
-- Name: twofactor_access_token_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.twofactor_access_token_id_seq', 1, false);


--
-- Name: twofactor_configuration_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.twofactor_configuration_id_seq', 11, false);


--
-- PostgreSQL database dump complete
--
