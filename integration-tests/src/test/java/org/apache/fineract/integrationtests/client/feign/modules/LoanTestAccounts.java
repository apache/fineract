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
package org.apache.fineract.integrationtests.client.feign.modules;

import org.apache.fineract.integrationtests.client.feign.helpers.FeignAccountHelper;
import org.apache.fineract.integrationtests.common.accounting.Account;

public class LoanTestAccounts implements LoanProductTemplates {

    private final Account loansReceivableAccount;
    private final Account interestReceivableAccount;
    private final Account feeReceivableAccount;
    private final Account penaltyReceivableAccount;
    private final Account suspenseAccount;
    private final Account fundSource;
    private final Account overpaymentAccount;
    private final Account interestIncomeAccount;
    private final Account feeIncomeAccount;
    private final Account penaltyIncomeAccount;
    private final Account feeChargeOffAccount;
    private final Account penaltyChargeOffAccount;
    private final Account recoveriesAccount;
    private final Account interestIncomeChargeOffAccount;
    private final Account chargeOffExpenseAccount;
    private final Account chargeOffFraudExpenseAccount;
    private final Account writtenOffAccount;
    private final Account goodwillExpenseAccount;
    private final Account goodwillIncomeAccount;
    private final Account deferredIncomeLiabilityAccount;
    private final Account buyDownExpenseAccount;

    public LoanTestAccounts(FeignAccountHelper accountHelper) {
        this.loansReceivableAccount = accountHelper.createAssetAccount("loanPortfolio");
        this.interestReceivableAccount = accountHelper.createAssetAccount("interestReceivable");
        this.feeReceivableAccount = accountHelper.createAssetAccount("feeReceivable");
        this.penaltyReceivableAccount = accountHelper.createAssetAccount("penaltyReceivable");
        this.suspenseAccount = accountHelper.createAssetAccount("suspense");
        this.fundSource = accountHelper.createLiabilityAccount("fundSource");
        this.overpaymentAccount = accountHelper.createLiabilityAccount("overpayment");
        this.interestIncomeAccount = accountHelper.createIncomeAccount("interestIncome");
        this.feeIncomeAccount = accountHelper.createIncomeAccount("feeIncome");
        this.penaltyIncomeAccount = accountHelper.createIncomeAccount("penaltyIncome");
        this.feeChargeOffAccount = accountHelper.createIncomeAccount("feeChargeOff");
        this.penaltyChargeOffAccount = accountHelper.createIncomeAccount("penaltyChargeOff");
        this.recoveriesAccount = accountHelper.createIncomeAccount("recoveries");
        this.interestIncomeChargeOffAccount = accountHelper.createIncomeAccount("interestIncomeChargeOff");
        this.chargeOffExpenseAccount = accountHelper.createExpenseAccount("chargeOff");
        this.chargeOffFraudExpenseAccount = accountHelper.createExpenseAccount("chargeOffFraud");
        this.writtenOffAccount = accountHelper.createExpenseAccount("writtenOffAccount");
        this.goodwillExpenseAccount = accountHelper.createExpenseAccount("goodwillExpenseAccount");
        this.goodwillIncomeAccount = accountHelper.createIncomeAccount("goodwillIncomeAccount");
        this.deferredIncomeLiabilityAccount = accountHelper.createLiabilityAccount("deferredIncomeLiabilityAccount");
        this.buyDownExpenseAccount = accountHelper.createExpenseAccount("buyDownExpenseAccount");
    }

    @Override
    public Long getAssetAccountId(String accountName) {
        return switch (accountName) {
            case "loansReceivable" -> loansReceivableAccount.getAccountID().longValue();
            case "interestReceivable" -> interestReceivableAccount.getAccountID().longValue();
            case "feeReceivable" -> feeReceivableAccount.getAccountID().longValue();
            case "penaltyReceivable" -> penaltyReceivableAccount.getAccountID().longValue();
            case "suspense" -> suspenseAccount.getAccountID().longValue();
            default -> throw new IllegalArgumentException("Unknown asset account: " + accountName);
        };
    }

    @Override
    public Long getLiabilityAccountId(String accountName) {
        return switch (accountName) {
            case "fundSource" -> fundSource.getAccountID().longValue();
            case "overpayment" -> overpaymentAccount.getAccountID().longValue();
            case "deferredIncomeLiability" -> deferredIncomeLiabilityAccount.getAccountID().longValue();
            default -> throw new IllegalArgumentException("Unknown liability account: " + accountName);
        };
    }

    @Override
    public Long getIncomeAccountId(String accountName) {
        return switch (accountName) {
            case "interestIncome" -> interestIncomeAccount.getAccountID().longValue();
            case "feeIncome" -> feeIncomeAccount.getAccountID().longValue();
            case "penaltyIncome" -> penaltyIncomeAccount.getAccountID().longValue();
            case "feeChargeOff" -> feeChargeOffAccount.getAccountID().longValue();
            case "penaltyChargeOff" -> penaltyChargeOffAccount.getAccountID().longValue();
            case "recoveries" -> recoveriesAccount.getAccountID().longValue();
            case "interestIncomeChargeOff" -> interestIncomeChargeOffAccount.getAccountID().longValue();
            case "goodwillIncome" -> goodwillIncomeAccount.getAccountID().longValue();
            default -> throw new IllegalArgumentException("Unknown income account: " + accountName);
        };
    }

    @Override
    public Long getExpenseAccountId(String accountName) {
        return switch (accountName) {
            case "chargeOff" -> chargeOffExpenseAccount.getAccountID().longValue();
            case "chargeOffFraud" -> chargeOffFraudExpenseAccount.getAccountID().longValue();
            case "writtenOff" -> writtenOffAccount.getAccountID().longValue();
            case "goodwillExpense" -> goodwillExpenseAccount.getAccountID().longValue();
            case "buyDownExpense" -> buyDownExpenseAccount.getAccountID().longValue();
            default -> throw new IllegalArgumentException("Unknown expense account: " + accountName);
        };
    }

    public Account getLoansReceivableAccount() {
        return loansReceivableAccount;
    }

    public Account getInterestReceivableAccount() {
        return interestReceivableAccount;
    }

    public Account getFeeReceivableAccount() {
        return feeReceivableAccount;
    }

    public Account getPenaltyReceivableAccount() {
        return penaltyReceivableAccount;
    }

    public Account getSuspenseAccount() {
        return suspenseAccount;
    }

    public Account getFundSource() {
        return fundSource;
    }

    public Account getOverpaymentAccount() {
        return overpaymentAccount;
    }

    public Account getInterestIncomeAccount() {
        return interestIncomeAccount;
    }

    public Account getFeeIncomeAccount() {
        return feeIncomeAccount;
    }

    public Account getPenaltyIncomeAccount() {
        return penaltyIncomeAccount;
    }

    public Account getFeeChargeOffAccount() {
        return feeChargeOffAccount;
    }

    public Account getPenaltyChargeOffAccount() {
        return penaltyChargeOffAccount;
    }

    public Account getRecoveriesAccount() {
        return recoveriesAccount;
    }

    public Account getInterestIncomeChargeOffAccount() {
        return interestIncomeChargeOffAccount;
    }

    public Account getChargeOffExpenseAccount() {
        return chargeOffExpenseAccount;
    }

    public Account getChargeOffFraudExpenseAccount() {
        return chargeOffFraudExpenseAccount;
    }

    public Account getWrittenOffAccount() {
        return writtenOffAccount;
    }

    public Account getGoodwillExpenseAccount() {
        return goodwillExpenseAccount;
    }

    public Account getGoodwillIncomeAccount() {
        return goodwillIncomeAccount;
    }

    public Account getDeferredIncomeLiabilityAccount() {
        return deferredIncomeLiabilityAccount;
    }

    public Account getBuyDownExpenseAccount() {
        return buyDownExpenseAccount;
    }
}
