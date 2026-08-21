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

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import org.apache.fineract.client.models.GetChargeOffReasonToExpenseAccountMappings;
import org.apache.fineract.client.models.GetLoanProductsProductIdResponse;
import org.apache.fineract.client.models.GetLoanProductsTemplateResponse;
import org.apache.fineract.client.models.GetLoanProductsWriteOffReasonOptions;
import org.apache.fineract.client.models.GetLoansLoanIdResponse;
import org.apache.fineract.client.models.PostCodeValueDataResponse;
import org.apache.fineract.client.models.PostCodeValuesDataRequest;
import org.apache.fineract.client.models.PostLoanProductsRequest;
import org.apache.fineract.client.models.PostWriteOffReasonToExpenseAccountMappings;
import org.apache.fineract.client.models.PutLoanProductsProductIdRequest;
import org.apache.fineract.integrationtests.client.feign.FeignLoanTestBase;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.portfolio.loanaccount.domain.LoanBuyDownFeeCalculationType;
import org.apache.fineract.portfolio.loanaccount.domain.LoanBuyDownFeeIncomeType;
import org.apache.fineract.portfolio.loanaccount.domain.LoanBuyDownFeeStrategy;
import org.apache.fineract.portfolio.loanaccount.domain.LoanCapitalizedIncomeCalculationType;
import org.apache.fineract.portfolio.loanaccount.domain.LoanCapitalizedIncomeStrategy;
import org.apache.fineract.portfolio.loanaccount.domain.LoanCapitalizedIncomeType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public class LoanProductTest extends FeignLoanTestBase {

    private static final Long WRITE_OFF_REASON_CODE_ID = 26L;

    @Nested
    public class IncomeCapitalizationTest {

        @Test
        public void testIncomeCapitalizationEnabled() {
            final Long clientId = createClient();

            final Long loanProductId = createLoanProduct(create4IProgressive().enableIncomeCapitalization(true)
                    .capitalizedIncomeCalculationType(PostLoanProductsRequest.CapitalizedIncomeCalculationTypeEnum.FLAT)
                    .capitalizedIncomeStrategy(PostLoanProductsRequest.CapitalizedIncomeStrategyEnum.EQUAL_AMORTIZATION)
                    .deferredIncomeLiabilityAccountId(getAccounts().getDeferredIncomeLiabilityAccount().getAccountID().longValue())
                    .incomeFromCapitalizationAccountId(getAccounts().getFeeIncomeAccount().getAccountID().longValue())
                    .capitalizedIncomeType(PostLoanProductsRequest.CapitalizedIncomeTypeEnum.FEE));

            final GetLoanProductsProductIdResponse loanProductsProductIdResponse = retrieveLoanProduct(loanProductId);
            Assertions.assertEquals(Boolean.TRUE, loanProductsProductIdResponse.getEnableIncomeCapitalization());
            Assertions.assertNotNull(loanProductsProductIdResponse.getCapitalizedIncomeCalculationType());
            Assertions.assertEquals(LoanCapitalizedIncomeCalculationType.FLAT.getCode(),
                    loanProductsProductIdResponse.getCapitalizedIncomeCalculationType().getCode());
            Assertions.assertNotNull(loanProductsProductIdResponse.getCapitalizedIncomeStrategy());
            Assertions.assertEquals(LoanCapitalizedIncomeStrategy.EQUAL_AMORTIZATION.getCode(),
                    loanProductsProductIdResponse.getCapitalizedIncomeStrategy().getCode());
            Assertions.assertNotNull(loanProductsProductIdResponse.getCapitalizedIncomeType());
            Assertions.assertEquals(LoanCapitalizedIncomeType.FEE.getCode(),
                    loanProductsProductIdResponse.getCapitalizedIncomeType().getCode());

            runAt("20 December 2024", () -> {
                Long loanId = applyAndApproveProgressiveLoan(clientId, loanProductId, "20 December 2024", 430.0, 7.0, 6, null);

                final GetLoansLoanIdResponse loanDetails = getLoanDetails(loanId);
                Assertions.assertEquals(Boolean.TRUE, loanDetails.getEnableIncomeCapitalization());
                Assertions.assertNotNull(loanDetails.getCapitalizedIncomeCalculationType());
                Assertions.assertEquals(LoanCapitalizedIncomeCalculationType.FLAT.getCode(),
                        loanDetails.getCapitalizedIncomeCalculationType().getCode());
                Assertions.assertNotNull(loanDetails.getCapitalizedIncomeStrategy());
                Assertions.assertEquals(LoanCapitalizedIncomeStrategy.EQUAL_AMORTIZATION.getCode(),
                        loanDetails.getCapitalizedIncomeStrategy().getCode());
                Assertions.assertNotNull(loanDetails.getCapitalizedIncomeType());
                Assertions.assertEquals(LoanCapitalizedIncomeType.FEE.getCode(), loanDetails.getCapitalizedIncomeType().getCode());

                Assertions.assertDoesNotThrow(() -> disburseLoan(loanId, BigDecimal.valueOf(430), "20 December 2024"));
            });
        }

        @Test
        public void testIncomeCapitalizationDisabled() {
            final Long clientId = createClient();

            final Long loanProductId = createLoanProduct(create4IProgressive().enableIncomeCapitalization(false)
                    .capitalizedIncomeCalculationType(PostLoanProductsRequest.CapitalizedIncomeCalculationTypeEnum.FLAT)
                    .capitalizedIncomeStrategy(PostLoanProductsRequest.CapitalizedIncomeStrategyEnum.EQUAL_AMORTIZATION)
                    .deferredIncomeLiabilityAccountId(getAccounts().getDeferredIncomeLiabilityAccount().getAccountID().longValue())
                    .incomeFromCapitalizationAccountId(getAccounts().getFeeIncomeAccount().getAccountID().longValue())
                    .capitalizedIncomeType(PostLoanProductsRequest.CapitalizedIncomeTypeEnum.FEE));

            final GetLoanProductsProductIdResponse loanProductsProductIdResponse = retrieveLoanProduct(loanProductId);
            Assertions.assertEquals(Boolean.FALSE, loanProductsProductIdResponse.getEnableIncomeCapitalization());
            Assertions.assertNotNull(loanProductsProductIdResponse.getCapitalizedIncomeCalculationType());
            Assertions.assertEquals(LoanCapitalizedIncomeCalculationType.FLAT.getCode(),
                    loanProductsProductIdResponse.getCapitalizedIncomeCalculationType().getCode());
            Assertions.assertNotNull(loanProductsProductIdResponse.getCapitalizedIncomeStrategy());
            Assertions.assertEquals(LoanCapitalizedIncomeStrategy.EQUAL_AMORTIZATION.getCode(),
                    loanProductsProductIdResponse.getCapitalizedIncomeStrategy().getCode());
            Assertions.assertNotNull(loanProductsProductIdResponse.getCapitalizedIncomeType());
            Assertions.assertEquals(LoanCapitalizedIncomeType.FEE.getCode(),
                    loanProductsProductIdResponse.getCapitalizedIncomeType().getCode());

            runAt("20 December 2024", () -> {
                Long loanId = applyAndApproveProgressiveLoan(clientId, loanProductId, "20 December 2024", 430.0, 7.0, 6, null);

                final GetLoansLoanIdResponse loanDetails = getLoanDetails(loanId);
                Assertions.assertEquals(Boolean.FALSE, loanDetails.getEnableIncomeCapitalization());
                Assertions.assertNotNull(loanDetails.getCapitalizedIncomeCalculationType());
                Assertions.assertEquals(LoanCapitalizedIncomeCalculationType.FLAT.getCode(),
                        loanDetails.getCapitalizedIncomeCalculationType().getCode());
                Assertions.assertNotNull(loanDetails.getCapitalizedIncomeStrategy());
                Assertions.assertEquals(LoanCapitalizedIncomeStrategy.EQUAL_AMORTIZATION.getCode(),
                        loanDetails.getCapitalizedIncomeStrategy().getCode());
                Assertions.assertNotNull(loanDetails.getCapitalizedIncomeType());
                Assertions.assertEquals(LoanCapitalizedIncomeType.FEE.getCode(), loanDetails.getCapitalizedIncomeType().getCode());

                Assertions.assertDoesNotThrow(() -> disburseLoan(loanId, BigDecimal.valueOf(430), "20 December 2024"));
            });
        }

        @Test
        public void testIncomeCapitalizationUpdateProduct() {
            final Long loanProductId = createLoanProduct(create4IProgressive().enableIncomeCapitalization(true)
                    .capitalizedIncomeCalculationType(PostLoanProductsRequest.CapitalizedIncomeCalculationTypeEnum.FLAT)
                    .capitalizedIncomeStrategy(PostLoanProductsRequest.CapitalizedIncomeStrategyEnum.EQUAL_AMORTIZATION)
                    .deferredIncomeLiabilityAccountId(getAccounts().getDeferredIncomeLiabilityAccount().getAccountID().longValue())
                    .incomeFromCapitalizationAccountId(getAccounts().getFeeIncomeAccount().getAccountID().longValue())
                    .capitalizedIncomeType(PostLoanProductsRequest.CapitalizedIncomeTypeEnum.FEE));

            final GetLoanProductsProductIdResponse loanProductsProductIdResponse = retrieveLoanProduct(loanProductId);
            Assertions.assertEquals(Boolean.TRUE, loanProductsProductIdResponse.getEnableIncomeCapitalization());
            Assertions.assertNotNull(loanProductsProductIdResponse.getCapitalizedIncomeCalculationType());
            Assertions.assertEquals(LoanCapitalizedIncomeCalculationType.FLAT.getCode(),
                    loanProductsProductIdResponse.getCapitalizedIncomeCalculationType().getCode());
            Assertions.assertNotNull(loanProductsProductIdResponse.getCapitalizedIncomeStrategy());
            Assertions.assertEquals(LoanCapitalizedIncomeStrategy.EQUAL_AMORTIZATION.getCode(),
                    loanProductsProductIdResponse.getCapitalizedIncomeStrategy().getCode());
            Assertions.assertNotNull(loanProductsProductIdResponse.getAccountingMappings());
            Assertions.assertEquals(getAccounts().getFeeIncomeAccount().getAccountID().longValue(),
                    loanProductsProductIdResponse.getAccountingMappings().getIncomeFromCapitalizationAccount().getId());
            Assertions.assertNotNull(loanProductsProductIdResponse.getCapitalizedIncomeType());
            Assertions.assertEquals(LoanCapitalizedIncomeType.FEE.getCode(),
                    loanProductsProductIdResponse.getCapitalizedIncomeType().getCode());

            updateLoanProduct(loanProductId,
                    new PutLoanProductsProductIdRequest()
                            .incomeFromCapitalizationAccountId(getAccounts().getInterestIncomeAccount().getAccountID().longValue())
                            .capitalizedIncomeType(PutLoanProductsProductIdRequest.CapitalizedIncomeTypeEnum.INTEREST));
            GetLoanProductsProductIdResponse updatedLoanProductsProductIdResponse = retrieveLoanProduct(loanProductId);
            Assertions.assertEquals(Boolean.TRUE, updatedLoanProductsProductIdResponse.getEnableIncomeCapitalization());
            Assertions.assertNotNull(updatedLoanProductsProductIdResponse.getCapitalizedIncomeCalculationType());
            Assertions.assertEquals(LoanCapitalizedIncomeCalculationType.FLAT.getCode(),
                    updatedLoanProductsProductIdResponse.getCapitalizedIncomeCalculationType().getCode());
            Assertions.assertNotNull(updatedLoanProductsProductIdResponse.getCapitalizedIncomeStrategy());
            Assertions.assertEquals(LoanCapitalizedIncomeStrategy.EQUAL_AMORTIZATION.getCode(),
                    updatedLoanProductsProductIdResponse.getCapitalizedIncomeStrategy().getCode());
            Assertions.assertNotNull(updatedLoanProductsProductIdResponse.getAccountingMappings());
            Assertions.assertEquals(getAccounts().getInterestIncomeAccount().getAccountID().longValue(),
                    updatedLoanProductsProductIdResponse.getAccountingMappings().getIncomeFromCapitalizationAccount().getId());

            updateLoanProduct(loanProductId, new PutLoanProductsProductIdRequest().enableIncomeCapitalization(false));

            updatedLoanProductsProductIdResponse = retrieveLoanProduct(loanProductId);
            Assertions.assertEquals(Boolean.FALSE, updatedLoanProductsProductIdResponse.getEnableIncomeCapitalization());
            Assertions.assertNotNull(updatedLoanProductsProductIdResponse.getAccountingMappings());
            Assertions.assertNull(updatedLoanProductsProductIdResponse.getAccountingMappings().getIncomeFromCapitalizationAccount());
            Assertions.assertNotNull(updatedLoanProductsProductIdResponse.getCapitalizedIncomeType());
            Assertions.assertEquals(LoanCapitalizedIncomeType.INTEREST.getCode(),
                    updatedLoanProductsProductIdResponse.getCapitalizedIncomeType().getCode());
        }

        @Test
        public void testIncomeCapitalizationCumulativeNotSupported() {
            Assertions.assertThrows(RuntimeException.class,
                    () -> createLoanProduct(createOnePeriod30DaysPeriodicAccrualProduct(7.0).enableIncomeCapitalization(true)
                            .capitalizedIncomeCalculationType(PostLoanProductsRequest.CapitalizedIncomeCalculationTypeEnum.FLAT)
                            .capitalizedIncomeStrategy(PostLoanProductsRequest.CapitalizedIncomeStrategyEnum.EQUAL_AMORTIZATION)
                            .deferredIncomeLiabilityAccountId(getAccounts().getDeferredIncomeLiabilityAccount().getAccountID().longValue())
                            .incomeFromCapitalizationAccountId(getAccounts().getFeeIncomeAccount().getAccountID().longValue())
                            .capitalizedIncomeType(PostLoanProductsRequest.CapitalizedIncomeTypeEnum.FEE)));
        }

        @Test
        public void testIncomeCapitalizationEnabledCalculationTypeNotProvided() {
            Assertions.assertThrows(RuntimeException.class,
                    () -> createLoanProduct(create4IProgressive().enableIncomeCapitalization(true)
                            .capitalizedIncomeStrategy(PostLoanProductsRequest.CapitalizedIncomeStrategyEnum.EQUAL_AMORTIZATION)
                            .deferredIncomeLiabilityAccountId(getAccounts().getDeferredIncomeLiabilityAccount().getAccountID().longValue())
                            .incomeFromCapitalizationAccountId(getAccounts().getFeeIncomeAccount().getAccountID().longValue())
                            .capitalizedIncomeType(PostLoanProductsRequest.CapitalizedIncomeTypeEnum.FEE)));
        }

        @Test
        public void testIncomeCapitalizationEnabledStrategyNotProvided() {
            Assertions.assertThrows(RuntimeException.class,
                    () -> createLoanProduct(create4IProgressive().enableIncomeCapitalization(true)
                            .capitalizedIncomeCalculationType(PostLoanProductsRequest.CapitalizedIncomeCalculationTypeEnum.FLAT)
                            .deferredIncomeLiabilityAccountId(getAccounts().getDeferredIncomeLiabilityAccount().getAccountID().longValue())
                            .incomeFromCapitalizationAccountId(getAccounts().getFeeIncomeAccount().getAccountID().longValue())
                            .capitalizedIncomeType(PostLoanProductsRequest.CapitalizedIncomeTypeEnum.FEE)));
        }

        @Test
        public void testIncomeCapitalizationEnabledDeferredIncomeLiabilityNotProvided() {
            Assertions.assertThrows(RuntimeException.class,
                    () -> createLoanProduct(create4IProgressive().enableIncomeCapitalization(true)
                            .capitalizedIncomeCalculationType(PostLoanProductsRequest.CapitalizedIncomeCalculationTypeEnum.FLAT)
                            .capitalizedIncomeStrategy(PostLoanProductsRequest.CapitalizedIncomeStrategyEnum.EQUAL_AMORTIZATION)
                            .incomeFromCapitalizationAccountId(getAccounts().getFeeIncomeAccount().getAccountID().longValue())
                            .capitalizedIncomeType(PostLoanProductsRequest.CapitalizedIncomeTypeEnum.FEE)));
        }

        @Test
        public void testIncomeCapitalizationEnabledIncomeFromCapitalizationNotProvided() {
            Assertions.assertThrows(RuntimeException.class,
                    () -> createLoanProduct(create4IProgressive().enableIncomeCapitalization(true)
                            .capitalizedIncomeCalculationType(PostLoanProductsRequest.CapitalizedIncomeCalculationTypeEnum.FLAT)
                            .capitalizedIncomeStrategy(PostLoanProductsRequest.CapitalizedIncomeStrategyEnum.EQUAL_AMORTIZATION)
                            .deferredIncomeLiabilityAccountId(getAccounts().getDeferredIncomeLiabilityAccount().getAccountID().longValue())
                            .capitalizedIncomeType(PostLoanProductsRequest.CapitalizedIncomeTypeEnum.FEE)));
        }

        @Test
        public void testIncomeCapitalizationEnabledIncomeTypeNotProvided() {
            Assertions.assertThrows(RuntimeException.class,
                    () -> createLoanProduct(create4IProgressive().enableIncomeCapitalization(true)
                            .capitalizedIncomeCalculationType(PostLoanProductsRequest.CapitalizedIncomeCalculationTypeEnum.FLAT)
                            .capitalizedIncomeStrategy(PostLoanProductsRequest.CapitalizedIncomeStrategyEnum.EQUAL_AMORTIZATION)
                            .deferredIncomeLiabilityAccountId(getAccounts().getDeferredIncomeLiabilityAccount().getAccountID().longValue())
                            .incomeFromCapitalizationAccountId(getAccounts().getFeeIncomeAccount().getAccountID().longValue())));
        }
    }

    @Nested
    public class BuyDownFeeTest {

        @Test
        public void testBuyDownFeeEnabled() {
            final Long clientId = createClient();

            final Long loanProductId = createLoanProduct(create4IProgressive().enableBuyDownFee(true)
                    .buyDownFeeCalculationType(PostLoanProductsRequest.BuyDownFeeCalculationTypeEnum.FLAT)
                    .buyDownFeeStrategy(PostLoanProductsRequest.BuyDownFeeStrategyEnum.EQUAL_AMORTIZATION)
                    .buyDownFeeIncomeType(PostLoanProductsRequest.BuyDownFeeIncomeTypeEnum.FEE)
                    .buyDownExpenseAccountId(getAccounts().getBuyDownExpenseAccount().getAccountID().longValue()).merchantBuyDownFee(true)
                    .incomeFromBuyDownAccountId(getAccounts().getFeeIncomeAccount().getAccountID().longValue()));

            final GetLoanProductsProductIdResponse loanProductsProductIdResponse = retrieveLoanProduct(loanProductId);
            Assertions.assertEquals(Boolean.TRUE, loanProductsProductIdResponse.getEnableBuyDownFee());
            Assertions.assertNotNull(loanProductsProductIdResponse.getBuyDownFeeCalculationType());
            Assertions.assertEquals(LoanBuyDownFeeCalculationType.FLAT.getCode(),
                    loanProductsProductIdResponse.getBuyDownFeeCalculationType().getCode());
            Assertions.assertNotNull(loanProductsProductIdResponse.getBuyDownFeeStrategy());
            Assertions.assertEquals(LoanBuyDownFeeStrategy.EQUAL_AMORTIZATION.getCode(),
                    loanProductsProductIdResponse.getBuyDownFeeStrategy().getCode());
            Assertions.assertNotNull(loanProductsProductIdResponse.getBuyDownFeeIncomeType());
            Assertions.assertEquals(LoanBuyDownFeeIncomeType.FEE.getCode(),
                    loanProductsProductIdResponse.getBuyDownFeeIncomeType().getCode());

            runAt("20 December 2024", () -> {
                Long loanId = applyAndApproveProgressiveLoan(clientId, loanProductId, "20 December 2024", 430.0, 7.0, 6, null);

                final GetLoansLoanIdResponse loanDetails = getLoanDetails(loanId);
                Assertions.assertEquals(Boolean.TRUE, loanDetails.getEnableBuyDownFee());
                Assertions.assertNotNull(loanDetails.getBuyDownFeeCalculationType());
                Assertions.assertEquals(LoanBuyDownFeeCalculationType.FLAT.getCode(), loanDetails.getBuyDownFeeCalculationType().getCode());
                Assertions.assertNotNull(loanDetails.getBuyDownFeeStrategy());
                Assertions.assertEquals(LoanBuyDownFeeStrategy.EQUAL_AMORTIZATION.getCode(), loanDetails.getBuyDownFeeStrategy().getCode());
                Assertions.assertNotNull(loanDetails.getBuyDownFeeIncomeType());
                Assertions.assertEquals(LoanBuyDownFeeIncomeType.FEE.getCode(), loanDetails.getBuyDownFeeIncomeType().getCode());

                Assertions.assertDoesNotThrow(() -> disburseLoan(loanId, BigDecimal.valueOf(430), "20 December 2024"));
            });
        }

        @Test
        public void testBuyDownFeeDisabled() {
            final Long clientId = createClient();

            final Long loanProductId = createLoanProduct(create4IProgressive().enableBuyDownFee(false)
                    .buyDownFeeCalculationType(PostLoanProductsRequest.BuyDownFeeCalculationTypeEnum.FLAT)
                    .buyDownFeeStrategy(PostLoanProductsRequest.BuyDownFeeStrategyEnum.EQUAL_AMORTIZATION)
                    .buyDownFeeIncomeType(PostLoanProductsRequest.BuyDownFeeIncomeTypeEnum.FEE)
                    .buyDownExpenseAccountId(getAccounts().getBuyDownExpenseAccount().getAccountID().longValue())
                    .incomeFromBuyDownAccountId(getAccounts().getFeeIncomeAccount().getAccountID().longValue()));

            final GetLoanProductsProductIdResponse loanProductsProductIdResponse = retrieveLoanProduct(loanProductId);
            Assertions.assertEquals(Boolean.FALSE, loanProductsProductIdResponse.getEnableBuyDownFee());
            Assertions.assertNotNull(loanProductsProductIdResponse.getBuyDownFeeCalculationType());
            Assertions.assertEquals(LoanBuyDownFeeCalculationType.FLAT.getCode(),
                    loanProductsProductIdResponse.getBuyDownFeeCalculationType().getCode());
            Assertions.assertNotNull(loanProductsProductIdResponse.getBuyDownFeeStrategy());
            Assertions.assertEquals(LoanBuyDownFeeStrategy.EQUAL_AMORTIZATION.getCode(),
                    loanProductsProductIdResponse.getBuyDownFeeStrategy().getCode());
            Assertions.assertNotNull(loanProductsProductIdResponse.getBuyDownFeeIncomeType());
            Assertions.assertEquals(LoanBuyDownFeeIncomeType.FEE.getCode(),
                    loanProductsProductIdResponse.getBuyDownFeeIncomeType().getCode());

            runAt("20 December 2024", () -> {
                Long loanId = applyAndApproveProgressiveLoan(clientId, loanProductId, "20 December 2024", 430.0, 7.0, 6, null);

                final GetLoansLoanIdResponse loanDetails = getLoanDetails(loanId);
                Assertions.assertEquals(Boolean.FALSE, loanDetails.getEnableBuyDownFee());
                Assertions.assertNotNull(loanDetails.getBuyDownFeeCalculationType());
                Assertions.assertEquals(LoanBuyDownFeeCalculationType.FLAT.getCode(), loanDetails.getBuyDownFeeCalculationType().getCode());
                Assertions.assertNotNull(loanDetails.getBuyDownFeeStrategy());
                Assertions.assertEquals(LoanBuyDownFeeStrategy.EQUAL_AMORTIZATION.getCode(), loanDetails.getBuyDownFeeStrategy().getCode());
                Assertions.assertNotNull(loanDetails.getBuyDownFeeIncomeType());
                Assertions.assertEquals(LoanBuyDownFeeIncomeType.FEE.getCode(), loanDetails.getBuyDownFeeIncomeType().getCode());

                Assertions.assertDoesNotThrow(() -> disburseLoan(loanId, BigDecimal.valueOf(430), "20 December 2024"));
            });
        }

        @Test
        public void testBuyDownFeeUpdateProduct() {
            final Long loanProductId = createLoanProduct(create4IProgressive().enableBuyDownFee(true)
                    .buyDownFeeCalculationType(PostLoanProductsRequest.BuyDownFeeCalculationTypeEnum.FLAT)
                    .buyDownFeeStrategy(PostLoanProductsRequest.BuyDownFeeStrategyEnum.EQUAL_AMORTIZATION)
                    .buyDownFeeIncomeType(PostLoanProductsRequest.BuyDownFeeIncomeTypeEnum.FEE)
                    .buyDownExpenseAccountId(getAccounts().getBuyDownExpenseAccount().getAccountID().longValue()).merchantBuyDownFee(true)
                    .incomeFromBuyDownAccountId(getAccounts().getFeeIncomeAccount().getAccountID().longValue()));

            final GetLoanProductsProductIdResponse loanProductsProductIdResponse = retrieveLoanProduct(loanProductId);
            Assertions.assertEquals(Boolean.TRUE, loanProductsProductIdResponse.getEnableBuyDownFee());
            Assertions.assertNotNull(loanProductsProductIdResponse.getBuyDownFeeCalculationType());
            Assertions.assertEquals(LoanBuyDownFeeCalculationType.FLAT.getCode(),
                    loanProductsProductIdResponse.getBuyDownFeeCalculationType().getCode());
            Assertions.assertNotNull(loanProductsProductIdResponse.getBuyDownFeeStrategy());
            Assertions.assertEquals(LoanBuyDownFeeStrategy.EQUAL_AMORTIZATION.getCode(),
                    loanProductsProductIdResponse.getBuyDownFeeStrategy().getCode());
            Assertions.assertNotNull(loanProductsProductIdResponse.getBuyDownFeeIncomeType());
            Assertions.assertEquals(LoanBuyDownFeeIncomeType.FEE.getCode(),
                    loanProductsProductIdResponse.getBuyDownFeeIncomeType().getCode());

            Assertions.assertNotNull(loanProductsProductIdResponse.getAccountingMappings());
            Assertions.assertEquals(getAccounts().getBuyDownExpenseAccount().getAccountID().longValue(),
                    loanProductsProductIdResponse.getAccountingMappings().getBuyDownExpenseAccount().getId());
            Assertions.assertEquals(getAccounts().getFeeIncomeAccount().getAccountID().longValue(),
                    loanProductsProductIdResponse.getAccountingMappings().getIncomeFromBuyDownAccount().getId());

            updateLoanProduct(loanProductId,
                    new PutLoanProductsProductIdRequest()
                            .buyDownFeeIncomeType(PutLoanProductsProductIdRequest.BuyDownFeeIncomeTypeEnum.INTEREST)
                            .incomeFromBuyDownAccountId(getAccounts().getInterestIncomeAccount().getAccountID().longValue()));

            GetLoanProductsProductIdResponse updatedLoanProductsProductIdResponse = retrieveLoanProduct(loanProductId);
            Assertions.assertEquals(Boolean.TRUE, updatedLoanProductsProductIdResponse.getEnableBuyDownFee());
            Assertions.assertEquals(LoanBuyDownFeeStrategy.EQUAL_AMORTIZATION.getCode(),
                    updatedLoanProductsProductIdResponse.getBuyDownFeeStrategy().getCode());
            Assertions.assertNotNull(updatedLoanProductsProductIdResponse.getBuyDownFeeIncomeType());
            Assertions.assertEquals(LoanBuyDownFeeIncomeType.INTEREST.getCode(),
                    updatedLoanProductsProductIdResponse.getBuyDownFeeIncomeType().getCode());
            Assertions.assertNotNull(updatedLoanProductsProductIdResponse.getAccountingMappings());
            Assertions.assertEquals(getAccounts().getBuyDownExpenseAccount().getAccountID().longValue(),
                    updatedLoanProductsProductIdResponse.getAccountingMappings().getBuyDownExpenseAccount().getId());
            Assertions.assertEquals(getAccounts().getInterestIncomeAccount().getAccountID().longValue(),
                    updatedLoanProductsProductIdResponse.getAccountingMappings().getIncomeFromBuyDownAccount().getId());

            updateLoanProduct(loanProductId, new PutLoanProductsProductIdRequest().enableBuyDownFee(false));

            updatedLoanProductsProductIdResponse = retrieveLoanProduct(loanProductId);
            Assertions.assertEquals(Boolean.FALSE, updatedLoanProductsProductIdResponse.getEnableBuyDownFee());
            Assertions.assertNotNull(updatedLoanProductsProductIdResponse.getBuyDownFeeCalculationType());
            Assertions.assertEquals(LoanBuyDownFeeCalculationType.FLAT.getCode(),
                    updatedLoanProductsProductIdResponse.getBuyDownFeeCalculationType().getCode());
            Assertions.assertNotNull(updatedLoanProductsProductIdResponse.getBuyDownFeeStrategy());
            Assertions.assertEquals(LoanBuyDownFeeStrategy.EQUAL_AMORTIZATION.getCode(),
                    updatedLoanProductsProductIdResponse.getBuyDownFeeStrategy().getCode());
            Assertions.assertNotNull(updatedLoanProductsProductIdResponse.getBuyDownFeeIncomeType());
            Assertions.assertNotNull(updatedLoanProductsProductIdResponse.getAccountingMappings());
            Assertions.assertNull(updatedLoanProductsProductIdResponse.getAccountingMappings().getBuyDownExpenseAccount());
            Assertions.assertNull(updatedLoanProductsProductIdResponse.getAccountingMappings().getIncomeFromBuyDownAccount());
        }

        @Test
        public void testBuyDownFeeCumulativeNotSupported() {
            Assertions.assertThrows(RuntimeException.class, () -> createLoanProduct(createOnePeriod30DaysPeriodicAccrualProduct(7.0)
                    .enableBuyDownFee(true).buyDownFeeCalculationType(PostLoanProductsRequest.BuyDownFeeCalculationTypeEnum.FLAT)
                    .buyDownFeeStrategy(PostLoanProductsRequest.BuyDownFeeStrategyEnum.EQUAL_AMORTIZATION)
                    .buyDownFeeIncomeType(PostLoanProductsRequest.BuyDownFeeIncomeTypeEnum.FEE)
                    .buyDownExpenseAccountId(getAccounts().getBuyDownExpenseAccount().getAccountID().longValue()).merchantBuyDownFee(true)
                    .incomeFromBuyDownAccountId(getAccounts().getFeeIncomeAccount().getAccountID().longValue())));
        }

        @Test
        public void testBuyDownFeeEnabledCalculationTypeNotProvided() {
            Assertions.assertThrows(RuntimeException.class, () -> createLoanProduct(create4IProgressive().enableBuyDownFee(true)
                    .buyDownFeeStrategy(PostLoanProductsRequest.BuyDownFeeStrategyEnum.EQUAL_AMORTIZATION)
                    .buyDownFeeIncomeType(PostLoanProductsRequest.BuyDownFeeIncomeTypeEnum.FEE)
                    .buyDownExpenseAccountId(getAccounts().getBuyDownExpenseAccount().getAccountID().longValue()).merchantBuyDownFee(true)
                    .incomeFromBuyDownAccountId(getAccounts().getFeeIncomeAccount().getAccountID().longValue())));
        }

        @Test
        public void testBuyDownFeeEnabledStrategyNotProvided() {
            Assertions.assertThrows(RuntimeException.class, () -> createLoanProduct(create4IProgressive().enableBuyDownFee(true)
                    .buyDownFeeCalculationType(PostLoanProductsRequest.BuyDownFeeCalculationTypeEnum.FLAT)
                    .buyDownFeeIncomeType(PostLoanProductsRequest.BuyDownFeeIncomeTypeEnum.FEE)
                    .buyDownExpenseAccountId(getAccounts().getBuyDownExpenseAccount().getAccountID().longValue()).merchantBuyDownFee(true)
                    .incomeFromBuyDownAccountId(getAccounts().getFeeIncomeAccount().getAccountID().longValue())));
        }

        @Test
        public void testBuyDownFeeEnabledIncomeTypeNotProvided() {
            Assertions.assertThrows(RuntimeException.class, () -> createLoanProduct(create4IProgressive().enableBuyDownFee(true)
                    .buyDownFeeCalculationType(PostLoanProductsRequest.BuyDownFeeCalculationTypeEnum.FLAT)
                    .buyDownFeeStrategy(PostLoanProductsRequest.BuyDownFeeStrategyEnum.EQUAL_AMORTIZATION)
                    .buyDownExpenseAccountId(getAccounts().getBuyDownExpenseAccount().getAccountID().longValue()).merchantBuyDownFee(true)
                    .incomeFromBuyDownAccountId(getAccounts().getFeeIncomeAccount().getAccountID().longValue())));
        }

        @Test
        public void testBuyDownFeeEnabledBuyDownExpenseNotProvided() {
            Assertions.assertThrows(RuntimeException.class,
                    () -> createLoanProduct(create4IProgressive().enableBuyDownFee(true)
                            .buyDownFeeCalculationType(PostLoanProductsRequest.BuyDownFeeCalculationTypeEnum.FLAT)
                            .buyDownFeeStrategy(PostLoanProductsRequest.BuyDownFeeStrategyEnum.EQUAL_AMORTIZATION)
                            .buyDownFeeIncomeType(PostLoanProductsRequest.BuyDownFeeIncomeTypeEnum.FEE).merchantBuyDownFee(true)
                            .incomeFromBuyDownAccountId(getAccounts().getFeeIncomeAccount().getAccountID().longValue())));
        }

        @Test
        public void testBuyDownFeeEnabledIncomeFromBuyDownNotProvided() {
            Assertions.assertThrows(RuntimeException.class,
                    () -> createLoanProduct(create4IProgressive().enableBuyDownFee(true)
                            .buyDownFeeCalculationType(PostLoanProductsRequest.BuyDownFeeCalculationTypeEnum.FLAT)
                            .buyDownFeeStrategy(PostLoanProductsRequest.BuyDownFeeStrategyEnum.EQUAL_AMORTIZATION)
                            .buyDownFeeIncomeType(PostLoanProductsRequest.BuyDownFeeIncomeTypeEnum.FEE).merchantBuyDownFee(true)
                            .buyDownExpenseAccountId(getAccounts().getBuyDownExpenseAccount().getAccountID().longValue())));
        }
    }

    @Nested
    public class WriteOffReasonsToExpenseMappings {

        @Test
        public void testWriteOffReasonToExpenseAccountMapping_shouldFail_on_nonExistingGLAccount_And_nonExistingWriteOffReason() {
            try {
                createLoanProduct(
                        create4IProgressive().addWriteOffReasonsToExpenseMappingsItem(new PostWriteOffReasonToExpenseAccountMappings()
                                .expenseAccountId("101230023").writeOffReasonCodeValueId("201230023")));
                Assertions.fail("Should have thrown an IllegalArgumentException");
            } catch (final RuntimeException ex) {
                Assertions.assertTrue(
                        ex.getMessage().contains("GL Account with ID 101230023 does not exist or is not an Expense GL account"));
                Assertions.assertTrue(ex.getMessage().contains("Write-off reason with ID 201230023 does not exist"));
            }
        }

        @Test
        public void testWriteOffReasonToExpenseAccountMapping_shouldFail_on_nonExistingGLAccount_And_Invalid_expenseAccountId() {
            try {
                createLoanProduct(create4IProgressive().addWriteOffReasonsToExpenseMappingsItem(
                        new PostWriteOffReasonToExpenseAccountMappings().expenseAccountId("asdf323").writeOffReasonCodeValueId("111")));
                Assertions.fail("Should have thrown an IllegalArgumentException");
            } catch (final RuntimeException ex) {
                Assertions.assertTrue(ex.getMessage()
                        .contains("validation.msg.loanproduct.writeOffReasonsToExpenseMappings[0].expenseAccountId.not.a.number"));
                Assertions.assertTrue(
                        ex.getMessage().contains("The parameter `writeOffReasonsToExpenseMappings[0].expenseAccountId` must be a number."));
            }
        }

        @Test
        public void testWriteOffReasonToExpenseAccountMapping_shouldFail_on_nonExistingGLAccount_And_Invalid_writeOffReasonCodeValueId() {
            try {
                createLoanProduct(create4IProgressive().addWriteOffReasonsToExpenseMappingsItem(
                        new PostWriteOffReasonToExpenseAccountMappings().expenseAccountId("111").writeOffReasonCodeValueId("asdf323")));
                Assertions.fail("Should have thrown an IllegalArgumentException");
            } catch (final RuntimeException ex) {
                Assertions.assertTrue(ex.getMessage()
                        .contains("validation.msg.loanproduct.writeOffReasonsToExpenseMappings[0].writeOffReasonCodeValueId.not.a.number"));
                Assertions.assertTrue(ex.getMessage()
                        .contains("The parameter `writeOffReasonsToExpenseMappings[0].writeOffReasonCodeValueId` must be a number."));
            }
        }

        @Test
        public void testWriteOffReasonsToExpenseMappings() {
            // create Write Off reasons
            Long reasonCode1 = createTestWriteOffReason();
            Long reasonCode2 = createTestWriteOffReason();

            // check if write Off reasons appears on loan product template
            GetLoanProductsTemplateResponse loanProductTemplate = getLoanProductTemplate(false);
            List<GetLoanProductsWriteOffReasonOptions> writeOffReasonOptions = loanProductTemplate.getWriteOffReasonOptions();
            Assertions.assertNotNull(writeOffReasonOptions);

            boolean isReasonCode1InTemplate = writeOffReasonOptions.stream().map(GetLoanProductsWriteOffReasonOptions::getId)
                    .anyMatch(id -> Objects.equals(id, reasonCode1));
            boolean isReasonCode2InTemplate = writeOffReasonOptions.stream().map(GetLoanProductsWriteOffReasonOptions::getId)
                    .anyMatch(id -> Objects.equals(id, reasonCode2));
            Assertions.assertTrue(isReasonCode1InTemplate);
            Assertions.assertTrue(isReasonCode2InTemplate);

            // Create Test Loan Product
            String reasonCodeId = reasonCode1.toString();
            String expenseAccountId = getAccounts().getBuyDownExpenseAccount().getAccountID().toString();

            Long loanProductId = createLoanProduct(
                    create4IProgressive().addWriteOffReasonsToExpenseMappingsItem(new PostWriteOffReasonToExpenseAccountMappings()
                            .expenseAccountId(expenseAccountId).writeOffReasonCodeValueId(reasonCodeId)));

            // Verify that get loan product API has the corresponding fields
            GetLoanProductsProductIdResponse getLoanProductsProductIdResponse = retrieveLoanProduct(loanProductId);
            List<GetChargeOffReasonToExpenseAccountMappings> writeOffReasonToExpenseAccountMappings = getLoanProductsProductIdResponse
                    .getWriteOffReasonsToExpenseMappings();
            Assertions.assertNotNull(writeOffReasonToExpenseAccountMappings);
            Assertions.assertEquals(1, writeOffReasonToExpenseAccountMappings.size());
            GetChargeOffReasonToExpenseAccountMappings writeOffMapping = writeOffReasonToExpenseAccountMappings.getFirst();
            Assertions.assertNotNull(writeOffMapping);
            Assertions.assertEquals(expenseAccountId, writeOffMapping.getExpenseAccount().getId().toString());
            Assertions.assertEquals(reasonCodeId, writeOffMapping.getReasonCodeValue().getId().toString());

            List<GetLoanProductsWriteOffReasonOptions> writeOffReasonOptionsResultNonTemplate = getLoanProductsProductIdResponse
                    .getWriteOffReasonOptions();
            if (writeOffReasonOptionsResultNonTemplate != null && !writeOffReasonOptionsResultNonTemplate.isEmpty()) {
                Assertions.fail("Write-off reason options with no template setting should be empty");
            }

            // test Update loan product API - delete writeOffReasonsToExpenseMappings
            GetLoanProductsProductIdResponse getLoanProductsProductId = retrieveLoanProduct(loanProductId);

            updateLoanProduct(loanProductId,
                    update4IProgressive(getLoanProductsProductId.getName(), getLoanProductsProductId.getShortName(),
                            getLoanProductsProductId.getDelinquencyBucket().getId()).writeOffReasonsToExpenseMappings(List.of()));

            // Verify that get loan product API has the corresponding fields
            Assertions.assertNull(retrieveLoanProduct(loanProductId).getWriteOffReasonsToExpenseMappings());
        }
    }

    private Long createTestWriteOffReason() {
        PostCodeValueDataResponse response = codeHelper.createCodeValue(WRITE_OFF_REASON_CODE_ID,
                new PostCodeValuesDataRequest().name(Utils.uniqueRandomStringGenerator("TestWriteOffReason_1_", 6))
                        .description("Test write off reason value 1").isActive(true).position(0));
        Assertions.assertNotNull(response);
        Assertions.assertNotNull(response.getSubResourceId());
        return response.getSubResourceId();
    }
}
