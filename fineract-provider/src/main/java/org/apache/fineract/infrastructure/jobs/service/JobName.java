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
package org.apache.fineract.infrastructure.jobs.service;

public enum JobName {

    UPDATE_LOAN_SUMMARY("Update loan Summary"), //
    UPDATE_LOAN_ARREARS_AGEING("Update Loan Arrears Ageing"), //
    UPDATE_LOAN_PAID_IN_ADVANCE("Update Loan Paid In Advance"), //
    APPLY_ANNUAL_FEE_FOR_SAVINGS("Apply Annual Fee For Savings"), //
    APPLY_HOLIDAYS_TO_LOANS("Apply Holidays To Loans"), //
    POST_INTEREST_FOR_SAVINGS("Post Interest For Savings"), //
    TRANSFER_FEE_CHARGE_FOR_LOANS("Transfer Fee For Loans From Savings"), //
    ACCOUNTING_RUNNING_BALANCE_UPDATE("Update Accounting Running Balances"), //
    PAY_DUE_SAVINGS_CHARGES("Pay Due Savings Charges"), //
    APPLY_CHARGE_TO_OVERDUE_LOAN_INSTALLMENT("Apply penalty to overdue loans"), //
    EXECUTE_STANDING_INSTRUCTIONS("Execute Standing Instruction"), //
    ADD_ACCRUAL_ENTRIES("Add Accrual Transactions"), //
    UPDATE_NPA("Update Non Performing Assets"), //
    UPDATE_DEPOSITS_ACCOUNT_MATURITY_DETAILS("Update Deposit Accounts Maturity details"), //
    TRANSFER_INTEREST_TO_SAVINGS("Transfer Interest To Savings"), //
    ADD_PERIODIC_ACCRUAL_ENTRIES("Add Periodic Accrual Transactions"), //
    RECALCULATE_INTEREST_FOR_LOAN("Recalculate Interest For Loans"), //
    GENERATE_RD_SCEHDULE("Generate Mandatory Savings Schedule"), //
    GENERATE_LOANLOSS_PROVISIONING("Generate Loan Loss Provisioning"), //
    POST_DIVIDENTS_FOR_SHARES("Post Dividends For Shares"), //
    UPDATE_SAVINGS_DORMANT_ACCOUNTS("Update Savings Dormant Accounts"), //
    ADD_PERIODIC_ACCRUAL_ENTRIES_FOR_LOANS_WITH_INCOME_POSTED_AS_TRANSACTIONS("Add Accrual Transactions For Loans With Income Posted As Transactions"),
    EXECUTE_REPORT_MAILING_JOBS("Execute Report Mailing Jobs"),
    UPDATE_SMS_OUTBOUND_WITH_CAMPAIGN_MESSAGE("Update SMS Outbound with Campaign Message"),
    SEND_MESSAGES_TO_SMS_GATEWAY("Send Messages to SMS Gateway"), 
    GET_DELIVERY_REPORTS_FROM_SMS_GATEWAY("Get Delivery Reports from SMS Gateway"),
	GENERATE_ADHOCCLIENT_SCEHDULE("Generate AdhocClient Schedule"),
	SEND_MESSAGES_TO_EMAIL_GATEWAY("Send messages to Email gateway"),
    UPDATE_EMAIL_OUTBOUND_WITH_CAMPAIGN_MESSAGE("Update Email Outbound with campaign message"),
    EXECUTE_EMAIL("Execute Email");

    private final String name;

    private JobName(final String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return this.name;
    }
}