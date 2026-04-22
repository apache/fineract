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
package org.apache.fineract.test.data.workingcapitalproduct;

import java.util.List;
import java.util.function.Function;
import org.apache.fineract.client.models.PostWorkingCapitalLoanProductsRequest;

public record WCGLAccountMapping(String responseKey, boolean required, Function<PostWorkingCapitalLoanProductsRequest, Long> extractor) {

    // Assets (required)
    public static final WCGLAccountMapping FUND_SOURCE = new WCGLAccountMapping("fundSourceAccount", true,
            PostWorkingCapitalLoanProductsRequest::getFundSourceAccountId);
    public static final WCGLAccountMapping LOAN_PORTFOLIO = new WCGLAccountMapping("loanPortfolioAccount", true,
            PostWorkingCapitalLoanProductsRequest::getLoanPortfolioAccountId);
    public static final WCGLAccountMapping TRANSFERS_IN_SUSPENSE = new WCGLAccountMapping("transfersInSuspenseAccount", true,
            PostWorkingCapitalLoanProductsRequest::getTransfersInSuspenseAccountId);
    // Liabilities (required)
    public static final WCGLAccountMapping DEFERRED_INCOME_LIABILITY = new WCGLAccountMapping("deferredIncomeLiabilityAccount", true,
            PostWorkingCapitalLoanProductsRequest::getDeferredIncomeLiabilityAccountId);
    public static final WCGLAccountMapping OVERPAYMENT_LIABILITY = new WCGLAccountMapping("overpaymentLiabilityAccount", true,
            PostWorkingCapitalLoanProductsRequest::getOverpaymentLiabilityAccountId);

    // Income (required)
    public static final WCGLAccountMapping INCOME_FROM_DISCOUNT_FEE = new WCGLAccountMapping("incomeFromDiscountFeeAccount", true,
            PostWorkingCapitalLoanProductsRequest::getIncomeFromDiscountFeeAccountId);
    public static final WCGLAccountMapping INCOME_FROM_FEE = new WCGLAccountMapping("incomeFromFeeAccount", true,
            PostWorkingCapitalLoanProductsRequest::getIncomeFromFeeAccountId);
    public static final WCGLAccountMapping INCOME_FROM_PENALTY = new WCGLAccountMapping("incomeFromPenaltyAccount", true,
            PostWorkingCapitalLoanProductsRequest::getIncomeFromPenaltyAccountId);
    public static final WCGLAccountMapping INCOME_FROM_RECOVERY = new WCGLAccountMapping("incomeFromRecoveryAccount", true,
            PostWorkingCapitalLoanProductsRequest::getIncomeFromRecoveryAccountId);

    // Expense (required)
    public static final WCGLAccountMapping WRITE_OFF = new WCGLAccountMapping("writeOffAccount", true,
            PostWorkingCapitalLoanProductsRequest::getWriteOffAccountId);

    // Optional — expense
    public static final WCGLAccountMapping GOODWILL_CREDIT = new WCGLAccountMapping("goodwillCreditAccount", false,
            PostWorkingCapitalLoanProductsRequest::getGoodwillCreditAccountId);
    public static final WCGLAccountMapping CHARGE_OFF_EXPENSE = new WCGLAccountMapping("chargeOffExpenseAccount", false,
            PostWorkingCapitalLoanProductsRequest::getChargeOffExpenseAccountId);
    public static final WCGLAccountMapping CHARGE_OFF_FRAUD_EXPENSE = new WCGLAccountMapping("chargeOffFraudExpenseAccount", false,
            PostWorkingCapitalLoanProductsRequest::getChargeOffFraudExpenseAccountId);

    // Optional — income (charge-off / goodwill)
    public static final WCGLAccountMapping INCOME_FROM_CHARGE_OFF_FEES = new WCGLAccountMapping("incomeFromChargeOffFeesAccount", false,
            PostWorkingCapitalLoanProductsRequest::getIncomeFromChargeOffFeesAccountId);
    public static final WCGLAccountMapping INCOME_FROM_CHARGE_OFF_PENALTY = new WCGLAccountMapping("incomeFromChargeOffPenaltyAccount",
            false, PostWorkingCapitalLoanProductsRequest::getIncomeFromChargeOffPenaltyAccountId);
    public static final WCGLAccountMapping INCOME_FROM_GOODWILL_CREDIT_FEES = new WCGLAccountMapping("incomeFromGoodwillCreditFeesAccount",
            false, PostWorkingCapitalLoanProductsRequest::getIncomeFromGoodwillCreditFeesAccountId);
    public static final WCGLAccountMapping INCOME_FROM_GOODWILL_CREDIT_PENALTY = new WCGLAccountMapping(
            "incomeFromGoodwillCreditPenaltyAccount", false,
            PostWorkingCapitalLoanProductsRequest::getIncomeFromGoodwillCreditPenaltyAccountId);

    private static final List<WCGLAccountMapping> VALUES = List.of(FUND_SOURCE, LOAN_PORTFOLIO, TRANSFERS_IN_SUSPENSE,
            DEFERRED_INCOME_LIABILITY, OVERPAYMENT_LIABILITY, INCOME_FROM_DISCOUNT_FEE, INCOME_FROM_FEE, INCOME_FROM_PENALTY,
            INCOME_FROM_RECOVERY, WRITE_OFF, GOODWILL_CREDIT, CHARGE_OFF_EXPENSE, CHARGE_OFF_FRAUD_EXPENSE, INCOME_FROM_CHARGE_OFF_FEES,
            INCOME_FROM_CHARGE_OFF_PENALTY, INCOME_FROM_GOODWILL_CREDIT_FEES, INCOME_FROM_GOODWILL_CREDIT_PENALTY);

    public static List<WCGLAccountMapping> all() {
        return VALUES;
    }
}
