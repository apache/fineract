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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;
import java.util.function.Consumer;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.client.models.GetLoansLoanIdTransactions;
import org.apache.fineract.client.models.PostClientsResponse;
import org.apache.fineract.client.models.PostLoanProductsRequest;
import org.apache.fineract.client.models.PostLoanProductsResponse;
import org.apache.fineract.client.models.PostLoansRequest;
import org.apache.fineract.client.models.PutGlobalConfigurationsRequest;
import org.apache.fineract.infrastructure.configuration.api.GlobalConfigurationConstants;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.junit.jupiter.api.Test;

@Slf4j
public class LoanAccrualTransactionOnDisbursementByAccountingTypeTest extends BaseLoanIntegrationTest {

    private static final String DISBURSEMENT_DATE = "01 January 2024";
    private static final Double LOAN_AMOUNT = 1000.0;
    private static final double INTEREST_RATE_PER_PERIOD = 12.0; // 12% annual

    // Customizer to set non-zero interest rate on the loan application
    // (applyLoanRequest defaults to interestRatePerPeriod=0 which would result in no interest and no accrual)
    private static final Consumer<PostLoansRequest> WITH_INTEREST = request -> request
            .interestRatePerPeriod(BigDecimal.valueOf(INTEREST_RATE_PER_PERIOD));

    // ==================== New behavior: allow-cash-and-non-cash-accrual = false ====================

    @Test
    public void testNoAccrualTransactionCreatedForNoneAccountingType() {
        runAt(DISBURSEMENT_DATE, () -> {
            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ALLOW_CASH_AND_NON_CASH_ACCRUAL,
                    new PutGlobalConfigurationsRequest().enabled(false));
            try {
                PostClientsResponse client = clientHelper.createClient(ClientHelper.defaultClientCreationRequest());

                PostLoanProductsRequest productRequest = createOnePeriod30DaysPeriodicAccrualProduct(INTEREST_RATE_PER_PERIOD)
                        .accountingRule(1) // NONE
                        .fundSourceAccountId(null) //
                        .loanPortfolioAccountId(null) //
                        .transfersInSuspenseAccountId(null) //
                        .interestOnLoanAccountId(null) //
                        .incomeFromFeeAccountId(null) //
                        .incomeFromPenaltyAccountId(null) //
                        .incomeFromRecoveryAccountId(null) //
                        .writeOffAccountId(null) //
                        .overpaymentLiabilityAccountId(null) //
                        .receivableInterestAccountId(null) //
                        .receivableFeeAccountId(null) //
                        .receivablePenaltyAccountId(null) //
                        .goodwillCreditAccountId(null) //
                        .incomeFromGoodwillCreditInterestAccountId(null) //
                        .incomeFromGoodwillCreditFeesAccountId(null) //
                        .incomeFromGoodwillCreditPenaltyAccountId(null) //
                        .incomeFromChargeOffInterestAccountId(null) //
                        .incomeFromChargeOffFeesAccountId(null) //
                        .incomeFromChargeOffPenaltyAccountId(null) //
                        .chargeOffExpenseAccountId(null) //
                        .chargeOffFraudExpenseAccountId(null);

                PostLoanProductsResponse loanProduct = loanProductHelper.createLoanProduct(productRequest);
                Long loanId = applyAndApproveLoan(client.getClientId(), loanProduct.getResourceId(), DISBURSEMENT_DATE, LOAN_AMOUNT, 1,
                        WITH_INTEREST);
                disburseLoan(loanId, BigDecimal.valueOf(LOAN_AMOUNT), DISBURSEMENT_DATE);

                List<GetLoansLoanIdTransactions> accrualTransactions = loanTransactionHelper.getLoanDetails(loanId).getTransactions()
                        .stream().filter(t -> "Accrual".equals(t.getType().getValue())).toList();

                assertTrue(accrualTransactions.isEmpty(),
                        "No accrual transactions should be created for None accounting type, but found: " + accrualTransactions.size());
            } finally {
                globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ALLOW_CASH_AND_NON_CASH_ACCRUAL,
                        new PutGlobalConfigurationsRequest().enabled(true));
            }
        });
    }

    @Test
    public void testNoAccrualTransactionCreatedForCashAccountingType() {
        runAt(DISBURSEMENT_DATE, () -> {
            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ALLOW_CASH_AND_NON_CASH_ACCRUAL,
                    new PutGlobalConfigurationsRequest().enabled(false));
            try {
                PostClientsResponse client = clientHelper.createClient(ClientHelper.defaultClientCreationRequest());

                PostLoanProductsRequest productRequest = createOnePeriod30DaysPeriodicAccrualProduct(INTEREST_RATE_PER_PERIOD)
                        .accountingRule(2) // CASH_BASED
                        .receivableInterestAccountId(null) //
                        .receivableFeeAccountId(null) //
                        .receivablePenaltyAccountId(null);

                PostLoanProductsResponse loanProduct = loanProductHelper.createLoanProduct(productRequest);
                Long loanId = applyAndApproveLoan(client.getClientId(), loanProduct.getResourceId(), DISBURSEMENT_DATE, LOAN_AMOUNT, 1,
                        WITH_INTEREST);
                disburseLoan(loanId, BigDecimal.valueOf(LOAN_AMOUNT), DISBURSEMENT_DATE);

                List<GetLoansLoanIdTransactions> accrualTransactions = loanTransactionHelper.getLoanDetails(loanId).getTransactions()
                        .stream().filter(t -> "Accrual".equals(t.getType().getValue())).toList();

                assertTrue(accrualTransactions.isEmpty(),
                        "No accrual transactions should be created for Cash accounting type, but found: " + accrualTransactions.size());
            } finally {
                globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ALLOW_CASH_AND_NON_CASH_ACCRUAL,
                        new PutGlobalConfigurationsRequest().enabled(true));
            }
        });
    }

    @Test
    public void testAccrualTransactionCreatedForUpfrontAccrualAccountingType() {
        runAt(DISBURSEMENT_DATE, () -> {
            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ALLOW_CASH_AND_NON_CASH_ACCRUAL,
                    new PutGlobalConfigurationsRequest().enabled(false));
            try {
                PostClientsResponse client = clientHelper.createClient(ClientHelper.defaultClientCreationRequest());

                PostLoanProductsRequest productRequest = createOnePeriod30DaysPeriodicAccrualProduct(INTEREST_RATE_PER_PERIOD)
                        .accountingRule(4); // ACCRUAL_UPFRONT

                PostLoanProductsResponse loanProduct = loanProductHelper.createLoanProduct(productRequest);
                Long loanId = applyAndApproveLoan(client.getClientId(), loanProduct.getResourceId(), DISBURSEMENT_DATE, LOAN_AMOUNT, 1,
                        WITH_INTEREST);
                disburseLoan(loanId, BigDecimal.valueOf(LOAN_AMOUNT), DISBURSEMENT_DATE);

                List<GetLoansLoanIdTransactions> accrualTransactions = loanTransactionHelper.getLoanDetails(loanId).getTransactions()
                        .stream().filter(t -> "Accrual".equals(t.getType().getValue())).toList();

                assertEquals(1, accrualTransactions.size(),
                        "Exactly one accrual transaction should be created for Accrual Upfront accounting type");
                BigDecimal interestPortion = accrualTransactions.getFirst().getInterestPortion();
                assertTrue(interestPortion != null && interestPortion.compareTo(BigDecimal.ZERO) > 0,
                        "Accrual transaction should have a positive interest portion");
            } finally {
                globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ALLOW_CASH_AND_NON_CASH_ACCRUAL,
                        new PutGlobalConfigurationsRequest().enabled(true));
            }
        });
    }

    @Test
    public void testNoAccrualTransactionAtDisbursementForPeriodicAccrualAccountingType() {
        runAt(DISBURSEMENT_DATE, () -> {
            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ALLOW_CASH_AND_NON_CASH_ACCRUAL,
                    new PutGlobalConfigurationsRequest().enabled(false));
            try {
                PostClientsResponse client = clientHelper.createClient(ClientHelper.defaultClientCreationRequest());

                PostLoanProductsRequest productRequest = createOnePeriod30DaysPeriodicAccrualProduct(INTEREST_RATE_PER_PERIOD);
                // accountingRule is already 3 (ACCRUAL_PERIODIC) by default in this method

                PostLoanProductsResponse loanProduct = loanProductHelper.createLoanProduct(productRequest);
                Long loanId = applyAndApproveLoan(client.getClientId(), loanProduct.getResourceId(), DISBURSEMENT_DATE, LOAN_AMOUNT, 1,
                        WITH_INTEREST);
                disburseLoan(loanId, BigDecimal.valueOf(LOAN_AMOUNT), DISBURSEMENT_DATE);

                List<GetLoansLoanIdTransactions> accrualTransactions = loanTransactionHelper.getLoanDetails(loanId).getTransactions()
                        .stream().filter(t -> "Accrual".equals(t.getType().getValue())).toList();

                assertTrue(accrualTransactions.isEmpty(),
                        "No accrual transactions should be created at disbursement for Periodic Accrual accounting type");
            } finally {
                globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ALLOW_CASH_AND_NON_CASH_ACCRUAL,
                        new PutGlobalConfigurationsRequest().enabled(true));
            }
        });
    }

    // ==================== Legacy behavior: allow-cash-and-non-cash-accrual = true (default) ====================

    @Test
    public void testAccrualTransactionCreatedForNoneAccountingTypeWhenLegacyBehaviorEnabled() {
        runAt(DISBURSEMENT_DATE, () -> {
            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ALLOW_CASH_AND_NON_CASH_ACCRUAL,
                    new PutGlobalConfigurationsRequest().enabled(true));

            // Legacy behavior: flag is enabled (true), accruals are created for NONE accounting type
            PostClientsResponse client = clientHelper.createClient(ClientHelper.defaultClientCreationRequest());

            PostLoanProductsRequest productRequest = createOnePeriod30DaysPeriodicAccrualProduct(INTEREST_RATE_PER_PERIOD).accountingRule(1) // NONE
                    .fundSourceAccountId(null) //
                    .loanPortfolioAccountId(null) //
                    .transfersInSuspenseAccountId(null) //
                    .interestOnLoanAccountId(null) //
                    .incomeFromFeeAccountId(null) //
                    .incomeFromPenaltyAccountId(null) //
                    .incomeFromRecoveryAccountId(null) //
                    .writeOffAccountId(null) //
                    .overpaymentLiabilityAccountId(null) //
                    .receivableInterestAccountId(null) //
                    .receivableFeeAccountId(null) //
                    .receivablePenaltyAccountId(null) //
                    .goodwillCreditAccountId(null) //
                    .incomeFromGoodwillCreditInterestAccountId(null) //
                    .incomeFromGoodwillCreditFeesAccountId(null) //
                    .incomeFromGoodwillCreditPenaltyAccountId(null) //
                    .incomeFromChargeOffInterestAccountId(null) //
                    .incomeFromChargeOffFeesAccountId(null) //
                    .incomeFromChargeOffPenaltyAccountId(null) //
                    .chargeOffExpenseAccountId(null) //
                    .chargeOffFraudExpenseAccountId(null);

            PostLoanProductsResponse loanProduct = loanProductHelper.createLoanProduct(productRequest);
            Long loanId = applyAndApproveLoan(client.getClientId(), loanProduct.getResourceId(), DISBURSEMENT_DATE, LOAN_AMOUNT, 1,
                    WITH_INTEREST);
            disburseLoan(loanId, BigDecimal.valueOf(LOAN_AMOUNT), DISBURSEMENT_DATE);

            List<GetLoansLoanIdTransactions> accrualTransactions = loanTransactionHelper.getLoanDetails(loanId).getTransactions().stream()
                    .filter(t -> "Accrual".equals(t.getType().getValue())).toList();

            assertEquals(1, accrualTransactions.size(),
                    "One accrual transaction should be created for None accounting type when legacy behavior is enabled");
            BigDecimal interestPortion = accrualTransactions.getFirst().getInterestPortion();
            assertTrue(interestPortion != null && interestPortion.compareTo(BigDecimal.ZERO) > 0,
                    "Accrual transaction should have a positive interest portion");
        });
    }

    @Test
    public void testAccrualTransactionCreatedForCashAccountingTypeWhenLegacyBehaviorEnabled() {
        runAt(DISBURSEMENT_DATE, () -> {
            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ALLOW_CASH_AND_NON_CASH_ACCRUAL,
                    new PutGlobalConfigurationsRequest().enabled(true));

            // Legacy behavior: flag is enabled (true), accruals are created for CASH accounting type
            PostClientsResponse client = clientHelper.createClient(ClientHelper.defaultClientCreationRequest());

            PostLoanProductsRequest productRequest = createOnePeriod30DaysPeriodicAccrualProduct(INTEREST_RATE_PER_PERIOD).accountingRule(2) // CASH_BASED
                    .receivableInterestAccountId(null) //
                    .receivableFeeAccountId(null) //
                    .receivablePenaltyAccountId(null);

            PostLoanProductsResponse loanProduct = loanProductHelper.createLoanProduct(productRequest);
            Long loanId = applyAndApproveLoan(client.getClientId(), loanProduct.getResourceId(), DISBURSEMENT_DATE, LOAN_AMOUNT, 1,
                    WITH_INTEREST);
            disburseLoan(loanId, BigDecimal.valueOf(LOAN_AMOUNT), DISBURSEMENT_DATE);

            List<GetLoansLoanIdTransactions> accrualTransactions = loanTransactionHelper.getLoanDetails(loanId).getTransactions().stream()
                    .filter(t -> "Accrual".equals(t.getType().getValue())).toList();

            assertEquals(1, accrualTransactions.size(),
                    "One accrual transaction should be created for Cash accounting type when legacy behavior is enabled");
            BigDecimal interestPortion = accrualTransactions.getFirst().getInterestPortion();
            assertTrue(interestPortion != null && interestPortion.compareTo(BigDecimal.ZERO) > 0,
                    "Accrual transaction should have a positive interest portion");

        });
    }
}
