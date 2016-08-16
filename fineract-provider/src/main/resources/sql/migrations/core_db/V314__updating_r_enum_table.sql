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

INSERT INTO `r_enum_value` (`enum_name`, `enum_id`, `enum_message_property`, `enum_value`, `enum_type`) VALUES ('loan_transaction_type_enum', 0, 'INVALID', 'INVALID', 0);
INSERT INTO `r_enum_value` (`enum_name`, `enum_id`, `enum_message_property`, `enum_value`, `enum_type`) VALUES ('loan_transaction_type_enum', 1, 'DISBURSEMENT', 'DISBURSEMENT', 0);
INSERT INTO `r_enum_value` (`enum_name`, `enum_id`, `enum_message_property`, `enum_value`, `enum_type`) VALUES ('loan_transaction_type_enum', 2, 'REPAYMENT', 'REPAYMENT', 0);
INSERT INTO `r_enum_value` (`enum_name`, `enum_id`, `enum_message_property`, `enum_value`, `enum_type`) VALUES ('loan_transaction_type_enum', 3, 'CONTRA', 'CONTRA', 0);
INSERT INTO `r_enum_value` (`enum_name`, `enum_id`, `enum_message_property`, `enum_value`, `enum_type`) VALUES ('loan_transaction_type_enum', 4, 'WAIVE_INTEREST', 'WAIVE_INTEREST', 0);
INSERT INTO `r_enum_value` (`enum_name`, `enum_id`, `enum_message_property`, `enum_value`, `enum_type`) VALUES ('loan_transaction_type_enum', 5, 'REPAYMENT_AT_DISBURSEMENT', 'REPAYMENT_AT_DISBURSEMENT', 0);
INSERT INTO `r_enum_value` (`enum_name`, `enum_id`, `enum_message_property`, `enum_value`, `enum_type`) VALUES ('loan_transaction_type_enum', 6, 'WRITEOFF', 'WRITEOFF', 0);
INSERT INTO `r_enum_value` (`enum_name`, `enum_id`, `enum_message_property`, `enum_value`, `enum_type`) VALUES ('loan_transaction_type_enum', 7, 'MARKED_FOR_RESCHEDULING', 'MARKED_FOR_RESCHEDULING', 0);
INSERT INTO `r_enum_value` (`enum_name`, `enum_id`, `enum_message_property`, `enum_value`, `enum_type`) VALUES ('loan_transaction_type_enum', 8, 'RECOVERY_REPAYMENT', 'RECOVERY_REPAYMENT', 0);
INSERT INTO `r_enum_value` (`enum_name`, `enum_id`, `enum_message_property`, `enum_value`, `enum_type`) VALUES ('loan_transaction_type_enum', 9, 'WAIVE_CHARGES', 'WAIVE_CHARGES', 0);
INSERT INTO `r_enum_value` (`enum_name`, `enum_id`, `enum_message_property`, `enum_value`, `enum_type`) VALUES ('loan_transaction_type_enum', 10, 'ACCRUAL', 'ACCRUAL', 0);
INSERT INTO `r_enum_value` (`enum_name`, `enum_id`, `enum_message_property`, `enum_value`, `enum_type`) VALUES ('loan_transaction_type_enum', 12, 'INITIATE_TRANSFER', 'INITIATE_TRANSFER', 0);
INSERT INTO `r_enum_value` (`enum_name`, `enum_id`, `enum_message_property`, `enum_value`, `enum_type`) VALUES ('loan_transaction_type_enum', 13, 'APPROVE_TRANSFER', 'APPROVE_TRANSFER', 0);
INSERT INTO `r_enum_value` (`enum_name`, `enum_id`, `enum_message_property`, `enum_value`, `enum_type`) VALUES ('loan_transaction_type_enum', 14, 'WITHDRAW_TRANSFER', 'WITHDRAW_TRANSFER', 0);
INSERT INTO `r_enum_value` (`enum_name`, `enum_id`, `enum_message_property`, `enum_value`, `enum_type`) VALUES ('loan_transaction_type_enum', 15, 'REJECT_TRANSFER', 'REJECT_TRANSFER', 0);
INSERT INTO `r_enum_value` (`enum_name`, `enum_id`, `enum_message_property`, `enum_value`, `enum_type`) VALUES ('loan_transaction_type_enum', 16, 'REFUND', 'REFUND', 0);
INSERT INTO `r_enum_value` (`enum_name`, `enum_id`, `enum_message_property`, `enum_value`, `enum_type`) VALUES ('loan_transaction_type_enum', 17, 'CHARGE_PAYMENT', 'CHARGE_PAYMENT', 0);
INSERT INTO `r_enum_value` (`enum_name`, `enum_id`, `enum_message_property`, `enum_value`, `enum_type`) VALUES ('loan_transaction_type_enum', 18, 'REFUND_FOR_ACTIVE_LOAN', 'REFUND_FOR_ACTIVE_LOAN', 0);
INSERT INTO `r_enum_value` (`enum_name`, `enum_id`, `enum_message_property`, `enum_value`, `enum_type`) VALUES ('loan_transaction_type_enum', 19, 'INCOME_POSTING', 'INCOME_POSTING', 0);
INSERT INTO `r_enum_value` (`enum_name`, `enum_id`, `enum_message_property`, `enum_value`, `enum_type`) VALUES ('savings_transaction_type_enum', 0, 'INVALID', 'INVALID', 0);
INSERT INTO `r_enum_value` (`enum_name`, `enum_id`, `enum_message_property`, `enum_value`, `enum_type`) VALUES ('savings_transaction_type_enum', 8, 'DIVIDEND_PAYOUT', 'DIVIDEND_PAYOUT', 0);
INSERT INTO `r_enum_value` (`enum_name`, `enum_id`, `enum_message_property`, `enum_value`, `enum_type`) VALUES ('savings_transaction_type_enum', 19, 'WITHHOLD_TAX', 'WITHHOLD_TAX', 0);
INSERT INTO `r_enum_value` (`enum_name`, `enum_id`, `enum_message_property`, `enum_value`, `enum_type`) VALUES ('client_transaction_type_enum', 1, 'PAY_CHARGE', 'PAY_CHARGE', 0);
INSERT INTO `r_enum_value` (`enum_name`, `enum_id`, `enum_message_property`, `enum_value`, `enum_type`) VALUES ('client_transaction_type_enum', 2, 'WAIVE_CHARGE', 'WAIVE_CHARGE', 0);
INSERT INTO `r_enum_value` (`enum_name`, `enum_id`, `enum_message_property`, `enum_value`, `enum_type`) VALUES ('entity_account_type_enum', 1, 'CLIENT', 'CLIENT', 0);
INSERT INTO `r_enum_value` (`enum_name`, `enum_id`, `enum_message_property`, `enum_value`, `enum_type`) VALUES ('entity_account_type_enum', 2, 'LOAN', 'LOAN', 0);
INSERT INTO `r_enum_value` (`enum_name`, `enum_id`, `enum_message_property`, `enum_value`, `enum_type`) VALUES ('entity_account_type_enum', 3, 'SAVINGS', 'SAVINGS', 0);
INSERT INTO `r_enum_value` (`enum_name`, `enum_id`, `enum_message_property`, `enum_value`, `enum_type`) VALUES ('entity_account_type_enum', 4, 'CENTER', 'CENTER', 0);
INSERT INTO `r_enum_value` (`enum_name`, `enum_id`, `enum_message_property`, `enum_value`, `enum_type`) VALUES ('entity_account_type_enum', 5, 'GROUP', 'GROUP', 0);
INSERT INTO `r_enum_value` (`enum_name`, `enum_id`, `enum_message_property`, `enum_value`, `enum_type`) VALUES ('entity_account_type_enum', 6, 'SHARES', 'SHARES', 0);
INSERT INTO `r_enum_value` (`enum_name`, `enum_id`, `enum_message_property`, `enum_value`, `enum_type`) VALUES ('calendar_type_enum', 0, 'INVALID', 'INVALID', 0);
INSERT INTO `r_enum_value` (`enum_name`, `enum_id`, `enum_message_property`, `enum_value`, `enum_type`) VALUES ('calendar_type_enum', 1, 'CLIENTS', 'CLIENTS', 0);
INSERT INTO `r_enum_value` (`enum_name`, `enum_id`, `enum_message_property`, `enum_value`, `enum_type`) VALUES ('calendar_type_enum', 2, 'GROUPS', 'GROUPS', 0);
INSERT INTO `r_enum_value` (`enum_name`, `enum_id`, `enum_message_property`, `enum_value`, `enum_type`) VALUES ('calendar_type_enum', 3, 'LOANS', 'LOANS', 0);
INSERT INTO `r_enum_value` (`enum_name`, `enum_id`, `enum_message_property`, `enum_value`, `enum_type`) VALUES ('calendar_type_enum', 4, 'CENTERS', 'CENTERS', 0);
INSERT INTO `r_enum_value` (`enum_name`, `enum_id`, `enum_message_property`, `enum_value`, `enum_type`) VALUES ('calendar_type_enum', 5, 'SAVINGS', 'SAVINGS', 0);
INSERT INTO `r_enum_value` (`enum_name`, `enum_id`, `enum_message_property`, `enum_value`, `enum_type`) VALUES ('calendar_type_enum', 6, 'LOAN_RECALCULATION_REST_DETAIL', 'LOAN_RECALCULATION_REST_DETAIL', 0);
INSERT INTO `r_enum_value` (`enum_name`, `enum_id`, `enum_message_property`, `enum_value`, `enum_type`) VALUES ('calendar_type_enum', 7, 'LOAN_RECALCULATION_COMPOUNDING_DETAIL', 'LOAN_RECALCULATION_COMPOUNDING_DETAIL', 0);
INSERT INTO `r_enum_value` (`enum_name`, `enum_id`, `enum_message_property`, `enum_value`, `enum_type`) VALUES ('glaccount_type_enum', 1, 'ASSET', 'ASSET', 0);
INSERT INTO `r_enum_value` (`enum_name`, `enum_id`, `enum_message_property`, `enum_value`, `enum_type`) VALUES ('glaccount_type_enum', 2, 'LIABILITY', 'LIABILITY', 0);
INSERT INTO `r_enum_value` (`enum_name`, `enum_id`, `enum_message_property`, `enum_value`, `enum_type`) VALUES ('glaccount_type_enum', 3, 'EQUITY', 'EQUITY', 0);
INSERT INTO `r_enum_value` (`enum_name`, `enum_id`, `enum_message_property`, `enum_value`, `enum_type`) VALUES ('glaccount_type_enum', 4, 'INCOME', 'INCOME', 0);
INSERT INTO `r_enum_value` (`enum_name`, `enum_id`, `enum_message_property`, `enum_value`, `enum_type`) VALUES ('glaccount_type_enum', 5, 'EXPENSE', 'EXPENSE', 0);
INSERT INTO `r_enum_value` (`enum_name`, `enum_id`, `enum_message_property`, `enum_value`, `enum_type`) VALUES ('portfolio_account_type_enum', 1, 'LOAN', 'LOAN', 0);
INSERT INTO `r_enum_value` (`enum_name`, `enum_id`, `enum_message_property`, `enum_value`, `enum_type`) VALUES ('portfolio_account_type_enum', 2, 'SAVING', 'EXPENSE', 0);
INSERT INTO `r_enum_value` (`enum_name`, `enum_id`, `enum_message_property`, `enum_value`, `enum_type`) VALUES ('portfolio_account_type_enum', 3, 'PROVISIONING', 'PROVISIONING', 0);
INSERT INTO `r_enum_value` (`enum_name`, `enum_id`, `enum_message_property`, `enum_value`, `enum_type`) VALUES ('portfolio_account_type_enum', 4, 'SHARES', 'SHARES', 0);
INSERT INTO `r_enum_value` (`enum_name`, `enum_id`, `enum_message_property`, `enum_value`, `enum_type`) VALUES ('accrual_accounts_for_loan_type_enum', 1, 'FUND_SOURCE', 'FUND_SOURCE', 0);
INSERT INTO `r_enum_value` (`enum_name`, `enum_id`, `enum_message_property`, `enum_value`, `enum_type`) VALUES ('accrual_accounts_for_loan_type_enum', 2, 'LOAN_PORTFOLIO', 'LOAN_PORTFOLIO', 0);
INSERT INTO `r_enum_value` (`enum_name`, `enum_id`, `enum_message_property`, `enum_value`, `enum_type`) VALUES ('accrual_accounts_for_loan_type_enum', 3, 'INTEREST_ON_LOANS', 'INTEREST_ON_LOANS', 0);
INSERT INTO `r_enum_value` (`enum_name`, `enum_id`, `enum_message_property`, `enum_value`, `enum_type`) VALUES ('accrual_accounts_for_loan_type_enum', 4, 'INCOME_FROM_FEES', 'INCOME_FROM_FEES', 0);
INSERT INTO `r_enum_value` (`enum_name`, `enum_id`, `enum_message_property`, `enum_value`, `enum_type`) VALUES ('accrual_accounts_for_loan_type_enum', 5, 'INCOME_FROM_PENALTIES', 'INCOME_FROM_PENALTIES', 0);
INSERT INTO `r_enum_value` (`enum_name`, `enum_id`, `enum_message_property`, `enum_value`, `enum_type`) VALUES ('accrual_accounts_for_loan_type_enum', 6, 'LOSSES_WRITTEN_OFF', 'LOSSES_WRITTEN_OFF', 0);
INSERT INTO `r_enum_value` (`enum_name`, `enum_id`, `enum_message_property`, `enum_value`, `enum_type`) VALUES ('accrual_accounts_for_loan_type_enum', 7, 'INTEREST_RECEIVABLE', 'INTEREST_RECEIVABLE', 0);
INSERT INTO `r_enum_value` (`enum_name`, `enum_id`, `enum_message_property`, `enum_value`, `enum_type`) VALUES ('accrual_accounts_for_loan_type_enum', 8, 'FEES_RECEIVABLE', 'FEES_RECEIVABLE', 0);
INSERT INTO `r_enum_value` (`enum_name`, `enum_id`, `enum_message_property`, `enum_value`, `enum_type`) VALUES ('accrual_accounts_for_loan_type_enum', 9, 'PENALTIES_RECEIVABLE', 'PENALTIES_RECEIVABLE', 0);
INSERT INTO `r_enum_value` (`enum_name`, `enum_id`, `enum_message_property`, `enum_value`, `enum_type`) VALUES ('accrual_accounts_for_loan_type_enum', 10, 'TRANSFERS_SUSPENSE', 'TRANSFERS_SUSPENSE', 0);
INSERT INTO `r_enum_value` (`enum_name`, `enum_id`, `enum_message_property`, `enum_value`, `enum_type`) VALUES ('accrual_accounts_for_loan_type_enum', 11, 'OVERPAYMENT', 'OVERPAYMENT', 0);
INSERT INTO `r_enum_value` (`enum_name`, `enum_id`, `enum_message_property`, `enum_value`, `enum_type`) VALUES ('accrual_accounts_for_loan_type_enum', 12, 'INCOME_FROM_RECOVERY', 'INCOME_FROM_RECOVERY', 0);
INSERT INTO `r_enum_value` (`enum_name`, `enum_id`, `enum_message_property`, `enum_value`, `enum_type`) VALUES ('cash_accounts_for_loan_type_enum', 1, 'FUND_SOURCE', 'FUND_SOURCE', 0);
INSERT INTO `r_enum_value` (`enum_name`, `enum_id`, `enum_message_property`, `enum_value`, `enum_type`) VALUES ('cash_accounts_for_loan_type_enum', 2, 'LOAN_PORTFOLIO', 'LOAN_PORTFOLIO', 0);
INSERT INTO `r_enum_value` (`enum_name`, `enum_id`, `enum_message_property`, `enum_value`, `enum_type`) VALUES ('cash_accounts_for_loan_type_enum', 3, 'INTEREST_ON_LOANS', 'INTEREST_ON_LOANS', 0);
INSERT INTO `r_enum_value` (`enum_name`, `enum_id`, `enum_message_property`, `enum_value`, `enum_type`) VALUES ('cash_accounts_for_loan_type_enum', 4, 'INCOME_FROM_FEES', 'INCOME_FROM_FEES', 0);
INSERT INTO `r_enum_value` (`enum_name`, `enum_id`, `enum_message_property`, `enum_value`, `enum_type`) VALUES ('cash_accounts_for_loan_type_enum', 5, 'INCOME_FROM_PENALTIES', 'INCOME_FROM_PENALTIES', 0);
INSERT INTO `r_enum_value` (`enum_name`, `enum_id`, `enum_message_property`, `enum_value`, `enum_type`) VALUES ('cash_accounts_for_loan_type_enum', 6, 'LOSSES_WRITTEN_OFF', 'LOSSES_WRITTEN_OFF', 0);
INSERT INTO `r_enum_value` (`enum_name`, `enum_id`, `enum_message_property`, `enum_value`, `enum_type`) VALUES ('cash_accounts_for_loan_type_enum', 10, 'TRANSFERS_SUSPENSE', 'TRANSFERS_SUSPENSE', 0);
INSERT INTO `r_enum_value` (`enum_name`, `enum_id`, `enum_message_property`, `enum_value`, `enum_type`) VALUES ('cash_accounts_for_loan_type_enum', 11, 'OVERPAYMENT', 'OVERPAYMENT', 0);
INSERT INTO `r_enum_value` (`enum_name`, `enum_id`, `enum_message_property`, `enum_value`, `enum_type`) VALUES ('cash_accounts_for_loan_type_enum', 12, 'INCOME_FROM_RECOVERY', 'INCOME_FROM_RECOVERY', 0);
INSERT INTO `r_enum_value` (`enum_name`, `enum_id`, `enum_message_property`, `enum_value`, `enum_type`) VALUES ('cash_accounts_for_savings_type_enum', 1, 'SAVINGS_REFERENCE', 'SAVINGS_REFERENCE', 0);
INSERT INTO `r_enum_value` (`enum_name`, `enum_id`, `enum_message_property`, `enum_value`, `enum_type`) VALUES ('cash_accounts_for_savings_type_enum', 2, 'SAVINGS_CONTROL', 'SAVINGS_CONTROL', 0);
INSERT INTO `r_enum_value` (`enum_name`, `enum_id`, `enum_message_property`, `enum_value`, `enum_type`) VALUES ('cash_accounts_for_savings_type_enum', 3, 'INTEREST_ON_SAVINGS', 'INTEREST_ON_SAVINGS', 0);
INSERT INTO `r_enum_value` (`enum_name`, `enum_id`, `enum_message_property`, `enum_value`, `enum_type`) VALUES ('cash_accounts_for_savings_type_enum', 4, 'INCOME_FROM_FEES', 'INCOME_FROM_FEES', 0);
INSERT INTO `r_enum_value` (`enum_name`, `enum_id`, `enum_message_property`, `enum_value`, `enum_type`) VALUES ('cash_accounts_for_savings_type_enum', 5, 'INCOME_FROM_PENALTIES', 'INCOME_FROM_PENALTIES', 0);
INSERT INTO `r_enum_value` (`enum_name`, `enum_id`, `enum_message_property`, `enum_value`, `enum_type`) VALUES ('cash_accounts_for_savings_type_enum', 10, 'TRANSFERS_SUSPENSE', 'TRANSFERS_SUSPENSE', 0);
INSERT INTO `r_enum_value` (`enum_name`, `enum_id`, `enum_message_property`, `enum_value`, `enum_type`) VALUES ('cash_accounts_for_savings_type_enum', 11, 'OVERDRAFT_PORTFOLIO_CONTROL', 'OVERDRAFT_PORTFOLIO_CONTROL', 0);
INSERT INTO `r_enum_value` (`enum_name`, `enum_id`, `enum_message_property`, `enum_value`, `enum_type`) VALUES ('cash_accounts_for_savings_type_enum', 12, 'INCOME_FROM_INTEREST', 'INCOME_FROM_INTEREST', 0);
INSERT INTO `r_enum_value` (`enum_name`, `enum_id`, `enum_message_property`, `enum_value`, `enum_type`) VALUES ('financial_activity_type_enum', 100, 'ASSET_TRANSFER', 'ASSET_TRANSFER', 0);
INSERT INTO `r_enum_value` (`enum_name`, `enum_id`, `enum_message_property`, `enum_value`, `enum_type`) VALUES ('financial_activity_type_enum', 200, 'LIABILITY_TRANSFER', 'LIABILITY_TRANSFER', 0);
INSERT INTO `r_enum_value` (`enum_name`, `enum_id`, `enum_message_property`, `enum_value`, `enum_type`) VALUES ('financial_activity_type_enum', 101, 'CASH_AT_MAINVAULT', 'CASH_AT_MAINVAULT', 0);
INSERT INTO `r_enum_value` (`enum_name`, `enum_id`, `enum_message_property`, `enum_value`, `enum_type`) VALUES ('financial_activity_type_enum', 102, 'CASH_AT_TELLER', 'CASH_AT_TELLER', 0);
INSERT INTO `r_enum_value` (`enum_name`, `enum_id`, `enum_message_property`, `enum_value`, `enum_type`) VALUES ('financial_activity_type_enum', 300, 'OPENING_BALANCES_TRANSFER_CONTRA', 'OPENING_BALANCES_TRANSFER_CONTRA', 0);
INSERT INTO `r_enum_value` (`enum_name`, `enum_id`, `enum_message_property`, `enum_value`, `enum_type`) VALUES ('financial_activity_type_enum', 103, 'ASSET_FUND_SOURCE', 'ASSET_FUND_SOURCE', 0);
INSERT INTO `r_enum_value` (`enum_name`, `enum_id`, `enum_message_property`, `enum_value`, `enum_type`) VALUES ('financial_activity_type_enum', 201, 'PAYABLE_DIVIDENDS', 'PAYABLE_DIVIDENDS', 0);
INSERT INTO `r_enum_value` (`enum_name`, `enum_id`, `enum_message_property`, `enum_value`, `enum_type`) VALUES ('cash_account_for_shares_type_enum', 1, 'SHARES_REFERENCE', 'SHARES_REFERENCE', 0);
INSERT INTO `r_enum_value` (`enum_name`, `enum_id`, `enum_message_property`, `enum_value`, `enum_type`) VALUES ('cash_account_for_shares_type_enum', 2, 'SHARES_SUSPENSE', 'SHARES_SUSPENSE', 0);
INSERT INTO `r_enum_value` (`enum_name`, `enum_id`, `enum_message_property`, `enum_value`, `enum_type`) VALUES ('cash_account_for_shares_type_enum', 3, 'INCOME_FROM_FEES', 'INCOME_FROM_FEES', 0);
INSERT INTO `r_enum_value` (`enum_name`, `enum_id`, `enum_message_property`, `enum_value`, `enum_type`) VALUES ('cash_account_for_shares_type_enum', 4, 'SHARES_EQUITY', 'SHARES_EQUITY', 0);
INSERT INTO `r_enum_value` (`enum_name`, `enum_id`, `enum_message_property`, `enum_value`, `enum_type`) VALUES ('journal_entry_type_type_enum', 1, 'CREDIT', 'CREDIT', 0);
INSERT INTO `r_enum_value` (`enum_name`, `enum_id`, `enum_message_property`, `enum_value`, `enum_type`) VALUES ('journal_entry_type_type_enum', 2, 'DEBIT', 'DEBIT', 0);
INSERT INTO `r_enum_value` (`enum_name`, `enum_id`, `enum_message_property`, `enum_value`, `enum_type`) VALUES ('account_type_type_enum', 0, 'INVALID', 'INVALID', 0);
INSERT INTO `r_enum_value` (`enum_name`, `enum_id`, `enum_message_property`, `enum_value`, `enum_type`) VALUES ('account_type_type_enum', 1, 'INDIVIDUAL', 'INDIVIDUAL', 0);
INSERT INTO `r_enum_value` (`enum_name`, `enum_id`, `enum_message_property`, `enum_value`, `enum_type`) VALUES ('account_type_type_enum', 2, 'GROUP', 'GROUP', 0);
INSERT INTO `r_enum_value` (`enum_name`, `enum_id`, `enum_message_property`, `enum_value`, `enum_type`) VALUES ('account_type_type_enum', 3, 'JLG', 'JLG', 0);
