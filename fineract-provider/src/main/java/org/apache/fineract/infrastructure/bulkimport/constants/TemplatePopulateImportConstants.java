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
package org.apache.fineract.infrastructure.bulkimport.constants;

public class TemplatePopulateImportConstants {

    //columns sizes
    public final static int SMALL_COL_SIZE =4000;
    public final static int MEDIUM_COL_SIZE =6000;
    public final static int LARGE_COL_SIZE=8000;
    public final static int EXTRALARGE_COL_SIZE=10000;

    //Sheet names
    public static final String OFFICE_SHEET_NAME ="Offices";
    public static final String CENTER_SHEET_NAME="Centers";
    public static final String STAFF_SHEET_NAME="Staff";
    public static final String GROUP_SHEET_NAME="Groups";
    public static final String CHART_OF_ACCOUNTS_SHEET_NAME="ChartOfAccounts";
    public static final String CLIENT_ENTITY_SHEET_NAME="ClientEntity";
    public static final String CLIENT_PERSON_SHEET_NAME="ClientPerson";
    public static final String CLIENT_SHEET_NAME="Clients";
    public static final String FIXED_DEPOSIT_SHEET_NAME="FixedDeposit";
    public static final String FIXED_DEPOSIT_TRANSACTION_SHEET_NAME="FixedDepositTransactions";
    public static final String PRODUCT_SHEET_NAME="Products";
    public static final String GUARANTOR_SHEET_NAME="guarantor";
    public static final String EXTRAS_SHEET_NAME="Extras";
    public static final String GL_ACCOUNTS_SHEET_NAME="GlAccounts";
    public static final String SAVINGS_ACCOUNTS_SHEET_NAME="SavingsAccounts";
    public static final String SHARED_PRODUCTS_SHEET_NAME="SharedProducts";
    public static final String JOURNAL_ENTRY_SHEET_NAME="AddJournalEntries";
    public static final String LOANS_SHEET_NAME="Loans";
    public static final String LOAN_REPAYMENT_SHEET_NAME="LoanRepayment";
    public static final String RECURRING_DEPOSIT_SHEET_NAME="RecurringDeposit";
    public static final String SAVINGS_TRANSACTION_SHEET_NAME="SavingsTransaction";
    public static final String SHARED_ACCOUNTS_SHEET_NAME="SharedAccounts";
    public static final String EMPLOYEE_SHEET_NAME="Employee";
    public static final String ROLES_SHEET_NAME="Roles";
    public static final String USER_SHEET_NAME="Users";

    public final static int ROWHEADER_INDEX=0;
    public final static short ROW_HEADER_HEIGHT =500;
    public final static int FIRST_COLUMN_INDEX=0;

    //Status column
    public final static String STATUS_CELL_IMPORTED="Imported";
    public final static String STATUS_CREATION_FAILED="Creation failed";
    public final static String STATUS_APPROVAL_FAILED="Approval failed";
    public final static String STATUS_ACTIVATION_FAILED="Activation failed";
    public final static String STATUS_MEETING_FAILED="Meeting failed";
    public final static String STATUS_DISBURSAL_FAILED="Disbursal failed";
    public final static String STATUS_DISBURSAL_REPAYMENT_FAILED="Repayment failed";
    public final static String STATUS_COLUMN_HEADER="Status";

    //Frequency Calender
    public static final String FREQUENCY_DAILY="Daily";
    public static final String FREQUENCY_WEEKLY="Weekly";
    public static final String FREQUENCY_MONTHLY="Monthly";
    public static final String FREQUENCY_YEARLY= "Yearly";

    //InterestCompoundingPeriod
    public static final String INTEREST_COMPOUNDING_PERIOD_DAILY="Daily";
    public static final String INTEREST_COMPOUNDING_PERIOD_MONTHLY="Monthly";
    public static final String INTEREST_COMPOUNDING_PERIOD_QUARTERLY="Quarterly";
    public static final String INTEREST_COMPOUNDING_PERIOD_SEMI_ANNUALLY="Semi-Annual";
    public static final String INTEREST_COMPOUNDING_PERIOD_ANNUALLY="Annually";

    //InterestPostingPeriod
    public static final String INTEREST_POSTING_PERIOD_MONTHLY="Monthly";
    public static final String INTEREST_POSTING_PERIOD_QUARTERLY="Quarterly";
    public static final String INTEREST_POSTING_PERIOD_BIANUALLY="BiAnnual";
    public static final String INTEREST_POSTING_PERIOD_ANNUALLY="Annually";

    //InterestCalculation
    public static final String INTEREST_CAL_DAILY_BALANCE="Daily Balance";
    public static final String INTEREST_CAL_AVG_BALANCE="Average Daily Balance";

    //InterestCalculation Day in Year
    public static final String INTEREST_CAL_DAYS_IN_YEAR_360="360 Days";
    public static final String INTEREST_CAL_DAYS_IN_YEAR_365="365 Days";

    //Frequency
    public static final String FREQUENCY_DAYS="Days";
    public static final String FREQUENCY_WEEKS="Weeks";
    public static final String FREQUENCY_MONTHS="Months";
    public static final String FREQUENCY_YEARS="Years";

    //Day Of Week
    public static final String MONDAY ="Mon";
    public static final String TUESDAY ="Tue";
    public static final String WEDNESDAY ="Wed";
    public static final String THURSDAY ="Thu";
    public static final String FRIDAY ="Fri";
    public static final String SATURDAY ="Sat";
    public static final String SUNDAY ="Sun";

    //Entity types
    public static final String CENTER_ENTITY_TYPE="CENTER";
    public static final String OFFICE_ENTITY_TYPE="OFFICE";
    public static final String STAFF_ENTITY_TYPE="STAFF";
    public static final String USER_ENTITY_TYPE="USER";
    public static final String GROUP_ENTITY_TYPE="GROUP";
    public static final String CLIENT_ENTITY_TYPE="CLIENT";
    public static final String LOAN_PRODUCT_ENTITY_TYPE="LOANPRODUCT";
    public static final String FUNDS_ENTITY_TYPE="FUNDS";
    public static final String PAYMENT_TYPE_ENTITY_TYPE="PAYMENTTYPE";
    public static final String CURRENCY_ENTITY_TYPE="CURRENCY";
    public static final String GL_ACCOUNT_ENTITY_TYPE="GLACCOUNT";
    public static final String SHARED_ACCOUNT_ENTITY_TYPE="SHAREDACCOUNT";
    public static final String SAVINGS_PRODUCT_ENTITY_TYPE="SAVINGSPRODUCT";
    public static final String RECURRING_DEPOSIT_PRODUCT_ENTITY_TYPE="RECURRINGDEPOSITPRODUCT";
    public static final String FIXED_DEPOSIT_PRODUCT_ENTITY_TYPE="FIXEDDEPOSITPRODUCT";

    //ReportHeader Values
    public static final String STATUS_COL_REPORT_HEADER="Status";
    public static final String CENTERID_COL_REPORT_HEADER="Center Id";
    public static final String SAVINGS_ID_COL_REPORT_HEADER="Savings ID";
    public static final String GROUP_ID_COL_REPORT_HEADER="Group ID";
    public static final String FAILURE_COL_REPORT_HEADER="Failure Report";

    //Guarantor Types
    public static final String GUARANTOR_INTERNAL="Internal";
    public static final String GUARANTOR_EXTERNAL="External";

    //Loan Account/Loan repayment Client External Id
    public static final Boolean CONTAINS_CLIENT_EXTERNAL_ID=true;

}
