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
package org.apache.fineract.integrationtests;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.util.List;
import org.apache.fineract.client.models.GetSavingsAccountsTransaction;
import org.apache.fineract.client.models.PostSavingsAccountsRequest;
import org.apache.fineract.client.models.PostSavingsProductsRequest;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.accounting.Account;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BaseSavingsIntegrationTest extends BaseIntegrationTest {

    private static final Logger LOG = LoggerFactory.getLogger(BaseSavingsIntegrationTest.class);

    static {
        Utils.initializeRESTAssured();
    }

    protected PostSavingsProductsRequest createSavingsProductRequest() {
        return new PostSavingsProductsRequest().name(Utils.uniqueRandomStringGenerator("SAVINGS_PRODUCT_", 6)) //
                .shortName(Utils.uniqueRandomStringGenerator("", 4)) //
                .description("Savings Product Description ") //
                .currencyCode("USD") //
                .digitsAfterDecimal(2) //
                .inMultiplesOf(0) //
                .nominalAnnualInterestRate(0.0) //
                .interestCompoundingPeriodType(1) //
                .interestPostingPeriodType(4).interestCalculationType(1) //
                .interestCalculationDaysInYearType(365) //
                .withdrawalFeeForTransfers(false) //
                .enforceMinRequiredBalance(false) //
                .isDormancyTrackingActive(false) //
                .allowOverdraft(false) //
                .withHoldTax(false) //
                .accountingRule(1) //
                .locale(LOCALE); //
    }

    protected PostSavingsAccountsRequest createSavingsAccountRequest(final Integer clientId, final Integer savingsProductId) {
        return new PostSavingsAccountsRequest().productId(savingsProductId) //
                .clientId(clientId) //
                .externalId("") //
                .nominalAnnualInterestRate(0.0) //
                .interestCompoundingPeriodType(1) //
                .interestPostingPeriodType(4).interestCalculationType(1) //
                .interestCalculationDaysInYearType(365) //
                .withdrawalFeeForTransfers(false) //
                .allowOverdraft(false) //
                .enforceMinRequiredBalance(false) //
                .dateFormat(DATETIME_PATTERN) //
                .locale(LOCALE); //
    }

    protected PostSavingsProductsRequest createSavingsProductWithAccountMappingForAccrualBased(Double interestRate,
            final Account... accountList) {
        PostSavingsProductsRequest savingsProductRequest = createSavingsProductRequest().nominalAnnualInterestRate(interestRate)
                .accountingRule(3);
        for (int i = 0; i < accountList.length; i++) {
            if (accountList[i].getAccountType().equals(Account.AccountType.ASSET)) {
                final Long ID = accountList[i].getAccountID().longValue();
                savingsProductRequest = savingsProductRequest.savingsReferenceAccountId(ID).overdraftPortfolioControlId(ID)
                        .feesReceivableAccountId(ID).penaltiesReceivableAccountId(ID);
            }
            if (accountList[i].getAccountType().equals(Account.AccountType.INCOME)) {
                final Long ID = accountList[i].getAccountID().longValue();
                savingsProductRequest = savingsProductRequest.incomeFromFeeAccountId(ID).incomeFromPenaltyAccountId(ID)
                        .incomeFromInterestId(ID);
            }
            if (accountList[i].getAccountType().equals(Account.AccountType.EXPENSE)) {
                final Long ID = accountList[i].getAccountID().longValue();
                savingsProductRequest = savingsProductRequest.interestOnSavingsAccountId(ID).writeOffAccountId(ID);
            }
            if (accountList[i].getAccountType().equals(Account.AccountType.LIABILITY)) {
                final Long ID = accountList[i].getAccountID().longValue();
                savingsProductRequest = savingsProductRequest.savingsControlAccountId(ID).transfersInSuspenseAccountId(ID)
                        .interestPayableAccountId(ID);
            }
        }

        return savingsProductRequest;
    }

    protected void checkSavingsAccrualTransactions(final List<GetSavingsAccountsTransaction> transactions, final Double amountExpected) {
        BigDecimal totalAmount = transactions.stream().filter(transaction -> transaction.getTransactionType().getAccrual() == true)
                .map(transaction -> transaction.getAmount()).reduce(BigDecimal.ZERO, BigDecimal::add);

        LOG.info("Savings Accrual transaction amounts are expected %f and total %f".formatted(amountExpected, totalAmount));
        assertEquals(amountExpected, totalAmount.doubleValue(),
                "Savings transactions are not equal %f and total %f".formatted(amountExpected, totalAmount));
    }

}
