/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.fineract.infrastructure.configuration.api;

public final class GlobalConfigurationConstants {

    public static final String MAKER_CHECKER = "maker-checker";
    public static final String AMAZON_S3 = "amazon-s3";
    public static final String RESCHEDULE_FUTURE_REPAYMENTS = "reschedule-future-repayments";
    public static final String RESCHEDULE_REPAYMENTS_ON_HOLIDAYS = "reschedule-repayments-on-holidays";
    public static final String ALLOW_TRANSACTIONS_ON_HOLIDAY = "allow-transactions-on-holiday";
    public static final String ALLOW_TRANSACTIONS_ON_NON_WORKING_DAY = "allow-transactions-on-non-workingday";
    public static final String CONSTRAINT_APPROACH_FOR_DATATABLES = "constraint-approach-for-datatables";
    public static final String PENALTY_WAIT_PERIOD = "penalty-wait-period";
    public static final String FORCE_PASSWORD_RESET_DAYS = "force-password-reset-days";
    public static final String GRACE_ON_PENALTY_POSTING = "grace-on-penalty-posting";
    public static final String SAVINGS_INTEREST_POSTING_CURRENT_PERIOD_END = "savings-interest-posting-current-period-end";
    public static final String FINANCIAL_YEAR_BEGINNING_MONTH = "financial-year-beginning-month";
    public static final String MIN_CLIENTS_IN_GROUP = "min-clients-in-group";
    public static final String MAX_CLIENTS_IN_GROUP = "max-clients-in-group";
    public static final String MEETINGS_MANDATORY_FOR_JLG_LOANS = "meetings-mandatory-for-jlg-loans";
    public static final String OFFICE_SPECIFIC_PRODUCTS_ENABLED = "office-specific-products-enabled";
    public static final String RESTRICT_PRODUCTS_TO_USER_OFFICE = "restrict-products-to-user-office";
    public static final String OFFICE_OPENING_BALANCES_CONTRA_ACCOUNT = "office-opening-balances-contra-account";
    public static final String ROUNDING_MODE = "rounding-mode";
    public static final String BACKDATE_PENALTIES_ENABLED = "backdate-penalties-enabled";
    public static final String ORGANISATION_START_DATE = "organisation-start-date";
    public static final String PAYMENT_TYPE_APPLICABLE_FOR_DISBURSEMENT_CHARGES = "paymenttype-applicable-for-disbursement-charges";
    public static final String INTEREST_CHARGED_FROM_DATE_SAME_AS_DISBURSAL_DATE = "interest-charged-from-date-same-as-disbursal-date";
    public static final String SKIP_REPAYMENT_ON_FIRST_DAY_OF_MONTH = "skip-repayment-on-first-day-of-month";
    public static final String CHANGE_EMI_IF_REPAYMENT_DATE_SAME_AS_DISBURSEMENT_DATE = "change-emi-if-repaymentdate-same-as-disbursementdate";
    public static final String DAILY_TPT_LIMIT = "daily-tpt-limit";
    public static final String ENABLE_ADDRESS = "enable-address";
    public static final String SUB_RATES = "sub-rates";
    public static final String LOAN_RESCHEDULE_IS_FIRST_PAYDAY_ALLOWED_ON_HOLIDAY = "loan-reschedule-is-first-payday-allowed-on-holiday";
    public static final String ACCOUNT_MAPPING_FOR_PAYMENT_TYPE = "account-mapping-for-payment-type";
    public static final String ACCOUNT_MAPPING_FOR_CHARGE = "account-mapping-for-charge";
    public static final String FIXED_DEPOSIT_TRANSFER_INTEREST_NEXT_DAY_FOR_PERIOD_END_POSTING = "fixed-deposit-transfer-interest-next-day-for-period-end-posting";
    public static final String ALLOW_BACKDATED_TRANSACTION_BEFORE_INTEREST_POSTING = "allow-backdated-transaction-before-interest-posting";
    public static final String ALLOW_BACKDATED_TRANSACTION_BEFORE_INTEREST_POSTING_DATE_FOR_DAYS = "allow-backdated-transaction-before-interest-posting-date-for-days";
    public static final String CUSTOM_ACCOUNT_NUMBER_LENGTH = "custom-account-number-length";
    public static final String RANDOM_ACCOUNT_NUMBER = "random-account-number";
    public static final String IS_INTEREST_TO_BE_RECOVERED_FIRST_WHEN_GREATER_THAN_EMI = "is-interest-to-be-recovered-first-when-greater-than-emi";
    public static final String IS_PRINCIPAL_COMPOUNDING_DISABLED_FOR_OVERDUE_LOANS = "is-principal-compounding-disabled-for-overdue-loans";
    public static final String ENABLE_BUSINESS_DATE = "enable-business-date";
    public static final String ENABLE_AUTOMATIC_COB_DATE_ADJUSTMENT = "enable-automatic-cob-date-adjustment";
    public static final String ENABLE_POST_REVERSAL_TXNS_FOR_REVERSE_TRANSACTIONS = "enable-post-reversal-txns-for-reverse-transactions";
    public static final String PURGE_EXTERNAL_EVENTS_OLDER_THAN_DAYS = "purge-external-events-older-than-days";
    public static final String DAYS_BEFORE_REPAYMENT_IS_DUE = "days-before-repayment-is-due";
    public static final String DAYS_AFTER_REPAYMENT_IS_OVERDUE = "days-after-repayment-is-overdue";
    public static final String ENABLE_AUTO_GENERATED_EXTERNAL_ID = "enable-auto-generated-external-id";
    public static final String PURGE_PROCESSED_COMMANDS_OLDER_THAN_DAYS = "purge-processed-commands-older-than-days";
    public static final String ENABLE_COB_BULK_EVENT = "enable-cob-bulk-event";
    public static final String EXTERNAL_EVENT_BATCH_SIZE = "external-event-batch-size";
    public static final String REPORT_EXPORT_S3_FOLDER_NAME = "report-export-s3-folder-name";
    public static final String LOAN_ARREARS_DELINQUENCY_DISPLAY_DATA = "loan-arrears-delinquency-display-data";
    public static final String CHARGE_ACCRUAL_DATE = "charge-accrual-date";
    public static final String ASSET_EXTERNALIZATION_OF_NON_ACTIVE_LOANS = "asset-externalization-of-non-active-loans";
    public static final String ENABLE_SAME_MAKER_CHECKER = "enable-same-maker-checker";
    public static final String NEXT_PAYMENT_DUE_DATE = "next-payment-due-date";
    public static final String ENABLE_PAYMENT_HUB_INTEGRATION = "enable-payment-hub-integration";
    public static final String ENABLE_IMMEDIATE_CHARGE_ACCRUAL_POST_MATURITY = "enable-immediate-charge-accrual-post-maturity";

    private GlobalConfigurationConstants() {}
}
