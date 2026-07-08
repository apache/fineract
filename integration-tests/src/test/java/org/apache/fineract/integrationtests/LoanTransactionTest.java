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
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.client.models.GetCodesResponse;
import org.apache.fineract.client.models.GetLoansLoanIdTransactionsResponse;
import org.apache.fineract.client.models.GetLoansLoanIdTransactionsTemplateResponse;
import org.apache.fineract.client.models.PostCodeValueDataResponse;
import org.apache.fineract.client.models.PostCodeValuesDataRequest;
import org.apache.fineract.client.models.PostLoanProductsRequest;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsRequest;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsResponse;
import org.apache.fineract.client.models.TransactionType;
import org.apache.fineract.integrationtests.client.feign.FeignLoanTestBase;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.portfolio.loanaccount.api.LoanTransactionApiConstants;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@Slf4j
public class LoanTransactionTest extends FeignLoanTestBase {

    private final String capitalizedIncomeCommand = "capitalizedIncome";
    private final String capitalizedIncomeAdjustmentCommand = "capitalizedIncomeAdjustment";
    private final String buyDownFeeCommand = "buyDownFee";
    private final String buyDownFeeAdjustmentCommand = "buyDownFeeAdjustment";

    @Test
    public void testGetLoanTransactionsFiltering() {
        Long clientId = createClient();
        final AtomicReference<Long> loanIdRef = new AtomicReference<>();
        final Long loanProductId = createLoanProduct(create4IProgressive());
        final String loanExternalIdStr = UUID.randomUUID().toString();

        runAt("20 December 2024", () -> {
            Long loanId = applyAndApproveProgressiveLoan(clientId, loanProductId, "20 December 2024", 430.0, 7.0, 6,
                    (request) -> request.externalId(loanExternalIdStr));
            loanIdRef.set(loanId);
            disburseLoan(loanId, BigDecimal.valueOf(430), "20 December 2024");
        });
        runAt("20 February 2025", () -> {
            Long loanId = loanIdRef.get();
            executeInlineCOB(loanId);
            makeLoanRepayment(loanId, "Repayment", "20 February 2025", 82.20);
        });
        runAt("23 February 2025", () -> {
            Long loanId = loanIdRef.get();
            executeInlineCOB(loanId);
            makeLoanRepayment(loanId, "Repayment", "23 February 2025", 82.20);
            final GetLoansLoanIdTransactionsResponse allLoanTransactionsPage = getLoanTransactions(loanId);
            Assertions.assertEquals(7L, allLoanTransactionsPage.getTotalElements());

            final GetLoansLoanIdTransactionsResponse nonAccrualLoanTransactionsPage = getLoanTransactions(loanId,
                    List.of(TransactionType.ACCRUAL));
            Assertions.assertEquals(3L, nonAccrualLoanTransactionsPage.getTotalElements());

            final GetLoansLoanIdTransactionsResponse allLoanTransactionsByExternalIdPage = getLoanTransactionsByExternalId(
                    loanExternalIdStr);
            Assertions.assertEquals(7L, allLoanTransactionsByExternalIdPage.getTotalElements());

            final GetLoansLoanIdTransactionsResponse nonAccrualLoanTransactionsByExternalIdPage = getLoanTransactionsByExternalId(
                    loanExternalIdStr, List.of(TransactionType.ACCRUAL));
            Assertions.assertEquals(3L, nonAccrualLoanTransactionsByExternalIdPage.getTotalElements());
        });
    }

    @Test
    public void testGetLoanTransactionTemplateForCapitalizedIncomeWithOverAppliedAmount() {
        Long clientId = createClient();
        codeHelper.retrieveCodeByName(LoanTransactionApiConstants.CAPITALIZED_INCOME_CLASSIFICATION_CODE);
        codeHelper.createCodeValue(
                codeHelper.retrieveCodeByName(LoanTransactionApiConstants.CAPITALIZED_INCOME_CLASSIFICATION_CODE).getId(),
                new PostCodeValuesDataRequest().name(Utils.uniqueRandomStringGenerator("CLASS_", 6)).isActive(true).position(10));

        final Long loanProductId = createLoanProduct(create4IProgressive().enableIncomeCapitalization(true)
                .capitalizedIncomeCalculationType(PostLoanProductsRequest.CapitalizedIncomeCalculationTypeEnum.FLAT)
                .capitalizedIncomeStrategy(PostLoanProductsRequest.CapitalizedIncomeStrategyEnum.EQUAL_AMORTIZATION)
                .deferredIncomeLiabilityAccountId(deferredIncomeLiabilityAccount().getAccountID().longValue())
                .incomeFromCapitalizationAccountId(feeIncomeAccount().getAccountID().longValue())
                .capitalizedIncomeType(PostLoanProductsRequest.CapitalizedIncomeTypeEnum.FEE));

        final String loanExternalIdStr = UUID.randomUUID().toString();

        runAt("20 December 2024", () -> {
            Long loanId = applyAndApproveProgressiveLoan(clientId, loanProductId, "20 December 2024", 430.0, 7.0, 6,
                    (request) -> request.externalId(loanExternalIdStr));
            disburseLoan(loanId, BigDecimal.valueOf(230), "20 December 2024");

            final GetLoansLoanIdTransactionsTemplateResponse transactionTemplate = retrieveTransactionTemplate(loanId,
                    capitalizedIncomeCommand, DATETIME_PATTERN, "20 December 2024", "en");

            assertNotNull(transactionTemplate);
            assertEquals("loanTransactionType." + capitalizedIncomeCommand, transactionTemplate.getType().getCode());
            assertEquals(0, BigDecimal.valueOf(transactionTemplate.getAmount()).compareTo(new BigDecimal("415")));
            assertThat(transactionTemplate.getPaymentTypeOptions().size() > 0);
            assertThat(transactionTemplate.getClassificationOptions().size() > 0);
        });
    }

    @Test
    public void testGetLoanTransactionTemplateForCapitalizedIncomeWithoutOverAppliedAmount() {
        Long clientId = createClient();

        final Long loanProductId = createLoanProduct(create4IProgressive().enableIncomeCapitalization(true)
                .capitalizedIncomeCalculationType(PostLoanProductsRequest.CapitalizedIncomeCalculationTypeEnum.FLAT)
                .capitalizedIncomeStrategy(PostLoanProductsRequest.CapitalizedIncomeStrategyEnum.EQUAL_AMORTIZATION)
                .deferredIncomeLiabilityAccountId(deferredIncomeLiabilityAccount().getAccountID().longValue())
                .incomeFromCapitalizationAccountId(feeIncomeAccount().getAccountID().longValue())
                .capitalizedIncomeType(PostLoanProductsRequest.CapitalizedIncomeTypeEnum.FEE).overAppliedCalculationType(null)
                .overAppliedNumber(null).allowApprovedDisbursedAmountsOverApplied(false));

        final String loanExternalIdStr = UUID.randomUUID().toString();

        runAt("20 December 2024", () -> {
            Long loanId = applyAndApproveProgressiveLoan(clientId, loanProductId, "20 December 2024", 430.0, 7.0, 6,
                    (request) -> request.externalId(loanExternalIdStr));
            disburseLoan(loanId, BigDecimal.valueOf(230), "20 December 2024");

            final GetLoansLoanIdTransactionsTemplateResponse transactionTemplate = retrieveTransactionTemplate(loanId,
                    capitalizedIncomeCommand, DATETIME_PATTERN, "20 December 2024", "en");

            assertNotNull(transactionTemplate);
            assertEquals("loanTransactionType." + capitalizedIncomeCommand, transactionTemplate.getType().getCode());
            assertEquals(0, BigDecimal.valueOf(transactionTemplate.getAmount()).compareTo(new BigDecimal("200")));
            assertThat(transactionTemplate.getPaymentTypeOptions().size() > 0);
        });
    }

    @Test
    public void testGetLoanTransactionTemplateForCapitalizedIncomeAdjustment() {
        Long clientId = createClient();

        final Long loanProductId = createLoanProduct(create4IProgressive().enableIncomeCapitalization(true)
                .capitalizedIncomeCalculationType(PostLoanProductsRequest.CapitalizedIncomeCalculationTypeEnum.FLAT)
                .capitalizedIncomeStrategy(PostLoanProductsRequest.CapitalizedIncomeStrategyEnum.EQUAL_AMORTIZATION)
                .deferredIncomeLiabilityAccountId(deferredIncomeLiabilityAccount().getAccountID().longValue())
                .incomeFromCapitalizationAccountId(feeIncomeAccount().getAccountID().longValue())
                .capitalizedIncomeType(PostLoanProductsRequest.CapitalizedIncomeTypeEnum.FEE));

        final String loanExternalIdStr = UUID.randomUUID().toString();

        runAt("20 December 2024", () -> {
            final Long loanId = applyAndApproveProgressiveLoan(clientId, loanProductId, "20 December 2024", 430.0, 7.0, 6,
                    (request) -> request.externalId(loanExternalIdStr));
            disburseLoan(loanId, BigDecimal.valueOf(230), "20 December 2024");

            PostLoansLoanIdTransactionsResponse loanTransactionResponse = executeLoanTransaction(loanId,
                    new PostLoansLoanIdTransactionsRequest().dateFormat(DATETIME_PATTERN).transactionDate("20 December 2024").locale("en")
                            .transactionAmount(150.0),
                    capitalizedIncomeCommand);
            assertNotNull(loanTransactionResponse);
            final Long transactionId = loanTransactionResponse.getResourceId();
            assertNotNull(transactionId);
            log.info("Loan Id {} with transaction id {}", loanId, transactionId);

            final GetLoansLoanIdTransactionsTemplateResponse transactionTemplate = retrieveTransactionTemplate(loanId,
                    capitalizedIncomeAdjustmentCommand, DATETIME_PATTERN, "20 December 2024", "en", transactionId);

            assertNotNull(transactionTemplate);
            assertEquals("loanTransactionType." + capitalizedIncomeAdjustmentCommand, transactionTemplate.getType().getCode());
            assertEquals(0, BigDecimal.valueOf(transactionTemplate.getAmount()).compareTo(new BigDecimal("150")));
        });
    }

    @Test
    public void testGetLoanTransactionTemplateForBuyDownFeeAdjustment() {
        Long clientId = createClient();

        final Long loanProductId = createLoanProduct(create4IProgressive().enableBuyDownFee(true)
                .buyDownFeeStrategy(PostLoanProductsRequest.BuyDownFeeStrategyEnum.EQUAL_AMORTIZATION)
                .buyDownFeeCalculationType(PostLoanProductsRequest.BuyDownFeeCalculationTypeEnum.FLAT)
                .buyDownFeeIncomeType(PostLoanProductsRequest.BuyDownFeeIncomeTypeEnum.INTEREST).merchantBuyDownFee(true)
                .deferredIncomeLiabilityAccountId(deferredIncomeLiabilityAccount().getAccountID().longValue())
                .incomeFromCapitalizationAccountId(feeIncomeAccount().getAccountID().longValue())
                .capitalizedIncomeType(PostLoanProductsRequest.CapitalizedIncomeTypeEnum.FEE)
                .buyDownExpenseAccountId(buyDownExpenseAccount().getAccountID().longValue())
                .incomeFromBuyDownAccountId(feeIncomeAccount().getAccountID().longValue()));

        final String loanExternalIdStr = UUID.randomUUID().toString();

        runAt("20 December 2024", () -> {
            final Long loanId = applyAndApproveProgressiveLoan(clientId, loanProductId, "20 December 2024", 430.0, 7.0, 6,
                    (request) -> request.externalId(loanExternalIdStr));
            disburseLoan(loanId, BigDecimal.valueOf(230), "20 December 2024");

            PostLoansLoanIdTransactionsResponse loanTransactionResponse = executeLoanTransaction(loanId,
                    new PostLoansLoanIdTransactionsRequest().dateFormat(DATETIME_PATTERN).transactionDate("20 December 2024").locale("en")
                            .transactionAmount(150.0),
                    buyDownFeeCommand);
            assertNotNull(loanTransactionResponse);
            final Long transactionId = loanTransactionResponse.getResourceId();
            assertNotNull(transactionId);
            log.info("Loan Id {} with transaction id {}", loanId, transactionId);

            final GetLoansLoanIdTransactionsTemplateResponse transactionTemplate = retrieveTransactionTemplate(loanId,
                    buyDownFeeAdjustmentCommand, DATETIME_PATTERN, "20 December 2024", "en", transactionId);

            assertNotNull(transactionTemplate);
            assertEquals("loanTransactionType." + buyDownFeeAdjustmentCommand, transactionTemplate.getType().getCode());
            assertEquals(0, BigDecimal.valueOf(transactionTemplate.getAmount()).compareTo(new BigDecimal("150")));
        });
    }

    @Test
    public void testGetLoanTransactionTemplateForBuyDownFee() {
        Long clientId = createClient();
        final GetCodesResponse code = codeHelper.retrieveCodeByName(LoanTransactionApiConstants.BUY_DOWN_FEE_CLASSIFICATION_CODE);
        final PostCodeValueDataResponse classificationCode = codeHelper.createCodeValue(code.getId(),
                new PostCodeValuesDataRequest().name(Utils.uniqueRandomStringGenerator("CLASS_", 6)).isActive(true).position(10));

        final Long loanProductId = createLoanProduct(create4IProgressive().enableIncomeCapitalization(true)
                .capitalizedIncomeCalculationType(PostLoanProductsRequest.CapitalizedIncomeCalculationTypeEnum.FLAT)
                .capitalizedIncomeStrategy(PostLoanProductsRequest.CapitalizedIncomeStrategyEnum.EQUAL_AMORTIZATION)
                .deferredIncomeLiabilityAccountId(deferredIncomeLiabilityAccount().getAccountID().longValue())
                .incomeFromCapitalizationAccountId(feeIncomeAccount().getAccountID().longValue())
                .capitalizedIncomeType(PostLoanProductsRequest.CapitalizedIncomeTypeEnum.FEE));

        final String loanExternalIdStr = UUID.randomUUID().toString();

        runAt("20 December 2024", () -> {
            Long loanId = applyAndApproveProgressiveLoan(clientId, loanProductId, "20 December 2024", 430.0, 7.0, 6,
                    (request) -> request.externalId(loanExternalIdStr));
            disburseLoan(loanId, BigDecimal.valueOf(230), "20 December 2024");

            final GetLoansLoanIdTransactionsTemplateResponse transactionTemplate = retrieveTransactionTemplate(loanId, buyDownFeeCommand,
                    DATETIME_PATTERN, "20 December 2024", "en");

            assertNotNull(transactionTemplate);
            assertEquals("loanTransactionType." + buyDownFeeCommand, transactionTemplate.getType().getCode());
            assertEquals(0, BigDecimal.valueOf(transactionTemplate.getAmount()).compareTo(BigDecimal.ZERO));
            assertThat(transactionTemplate.getPaymentTypeOptions().size() > 0);
            assertThat(transactionTemplate.getClassificationOptions().size() > 0);
        });
    }
}
