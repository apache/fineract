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
import org.apache.fineract.client.models.GetLoanProductsProductIdResponse;
import org.apache.fineract.client.models.GetLoansLoanIdResponse;
import org.apache.fineract.client.models.PostClientsResponse;
import org.apache.fineract.client.models.PostLoanProductsRequest;
import org.apache.fineract.client.models.PostLoanProductsResponse;
import org.apache.fineract.client.models.PutLoanProductsProductIdRequest;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.portfolio.loanaccount.domain.LoanBuyDownFeeCalculationType;
import org.apache.fineract.portfolio.loanaccount.domain.LoanBuyDownFeeIncomeType;
import org.apache.fineract.portfolio.loanaccount.domain.LoanBuyDownFeeStrategy;
import org.apache.fineract.portfolio.loanaccount.domain.LoanCapitalizedIncomeCalculationType;
import org.apache.fineract.portfolio.loanaccount.domain.LoanCapitalizedIncomeStrategy;
import org.apache.fineract.portfolio.loanaccount.domain.LoanCapitalizedIncomeType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public class LoanProductTest extends BaseLoanIntegrationTest {

    @Nested
    public class IncomeCapitalizationTest {

        @Test
        public void testIncomeCapitalizationEnabled() {
            final PostClientsResponse client = clientHelper.createClient(ClientHelper.defaultClientCreationRequest());

            final PostLoanProductsResponse loanProductsResponse = loanProductHelper
                    .createLoanProduct(create4IProgressive().enableIncomeCapitalization(true)
                            .capitalizedIncomeCalculationType(PostLoanProductsRequest.CapitalizedIncomeCalculationTypeEnum.FLAT)
                            .capitalizedIncomeStrategy(PostLoanProductsRequest.CapitalizedIncomeStrategyEnum.EQUAL_AMORTIZATION)
                            .deferredIncomeLiabilityAccountId(deferredIncomeLiabilityAccount.getAccountID().longValue())
                            .incomeFromCapitalizationAccountId(feeIncomeAccount.getAccountID().longValue())
                            .capitalizedIncomeType(PostLoanProductsRequest.CapitalizedIncomeTypeEnum.FEE));

            final GetLoanProductsProductIdResponse loanProductsProductIdResponse = loanProductHelper
                    .retrieveLoanProductById(loanProductsResponse.getResourceId());
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
                Long loanId = applyAndApproveProgressiveLoan(client.getClientId(), loanProductsResponse.getResourceId(), "20 December 2024",
                        430.0, 7.0, 6, null);

                final GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId);
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
            final PostClientsResponse client = clientHelper.createClient(ClientHelper.defaultClientCreationRequest());

            final PostLoanProductsResponse loanProductsResponse = loanProductHelper
                    .createLoanProduct(create4IProgressive().enableIncomeCapitalization(false)
                            .capitalizedIncomeCalculationType(PostLoanProductsRequest.CapitalizedIncomeCalculationTypeEnum.FLAT)
                            .capitalizedIncomeStrategy(PostLoanProductsRequest.CapitalizedIncomeStrategyEnum.EQUAL_AMORTIZATION)
                            .deferredIncomeLiabilityAccountId(deferredIncomeLiabilityAccount.getAccountID().longValue())
                            .incomeFromCapitalizationAccountId(feeIncomeAccount.getAccountID().longValue())
                            .capitalizedIncomeType(PostLoanProductsRequest.CapitalizedIncomeTypeEnum.FEE));

            final GetLoanProductsProductIdResponse loanProductsProductIdResponse = loanProductHelper
                    .retrieveLoanProductById(loanProductsResponse.getResourceId());
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
                Long loanId = applyAndApproveProgressiveLoan(client.getClientId(), loanProductsResponse.getResourceId(), "20 December 2024",
                        430.0, 7.0, 6, null);

                final GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId);
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
            final PostLoanProductsResponse loanProductsResponse = loanProductHelper
                    .createLoanProduct(create4IProgressive().enableIncomeCapitalization(true)
                            .capitalizedIncomeCalculationType(PostLoanProductsRequest.CapitalizedIncomeCalculationTypeEnum.FLAT)
                            .capitalizedIncomeStrategy(PostLoanProductsRequest.CapitalizedIncomeStrategyEnum.EQUAL_AMORTIZATION)
                            .deferredIncomeLiabilityAccountId(deferredIncomeLiabilityAccount.getAccountID().longValue())
                            .incomeFromCapitalizationAccountId(feeIncomeAccount.getAccountID().longValue())
                            .capitalizedIncomeType(PostLoanProductsRequest.CapitalizedIncomeTypeEnum.FEE));

            final GetLoanProductsProductIdResponse loanProductsProductIdResponse = loanProductHelper
                    .retrieveLoanProductById(loanProductsResponse.getResourceId());
            Assertions.assertEquals(Boolean.TRUE, loanProductsProductIdResponse.getEnableIncomeCapitalization());
            Assertions.assertNotNull(loanProductsProductIdResponse.getCapitalizedIncomeCalculationType());
            Assertions.assertEquals(LoanCapitalizedIncomeCalculationType.FLAT.getCode(),
                    loanProductsProductIdResponse.getCapitalizedIncomeCalculationType().getCode());
            Assertions.assertNotNull(loanProductsProductIdResponse.getCapitalizedIncomeStrategy());
            Assertions.assertEquals(LoanCapitalizedIncomeStrategy.EQUAL_AMORTIZATION.getCode(),
                    loanProductsProductIdResponse.getCapitalizedIncomeStrategy().getCode());
            Assertions.assertNotNull(loanProductsProductIdResponse.getAccountingMappings());
            Assertions.assertEquals(feeIncomeAccount.getAccountID().longValue(),
                    loanProductsProductIdResponse.getAccountingMappings().getIncomeFromCapitalizationAccount().getId());
            Assertions.assertNotNull(loanProductsProductIdResponse.getCapitalizedIncomeType());
            Assertions.assertEquals(LoanCapitalizedIncomeType.FEE.getCode(),
                    loanProductsProductIdResponse.getCapitalizedIncomeType().getCode());

            loanProductHelper.updateLoanProductById(loanProductsResponse.getResourceId(),
                    new PutLoanProductsProductIdRequest().enableIncomeCapitalization(false)
                            .incomeFromCapitalizationAccountId(interestIncomeAccount.getAccountID().longValue())
                            .capitalizedIncomeType(PutLoanProductsProductIdRequest.CapitalizedIncomeTypeEnum.INTEREST));

            final GetLoanProductsProductIdResponse updatedLoanProductsProductIdResponse = loanProductHelper
                    .retrieveLoanProductById(loanProductsResponse.getResourceId());
            Assertions.assertEquals(Boolean.FALSE, updatedLoanProductsProductIdResponse.getEnableIncomeCapitalization());
            Assertions.assertNotNull(updatedLoanProductsProductIdResponse.getCapitalizedIncomeCalculationType());
            Assertions.assertEquals(LoanCapitalizedIncomeCalculationType.FLAT.getCode(),
                    updatedLoanProductsProductIdResponse.getCapitalizedIncomeCalculationType().getCode());
            Assertions.assertNotNull(updatedLoanProductsProductIdResponse.getCapitalizedIncomeStrategy());
            Assertions.assertEquals(LoanCapitalizedIncomeStrategy.EQUAL_AMORTIZATION.getCode(),
                    updatedLoanProductsProductIdResponse.getCapitalizedIncomeStrategy().getCode());
            Assertions.assertNotNull(updatedLoanProductsProductIdResponse.getAccountingMappings());
            Assertions.assertEquals(interestIncomeAccount.getAccountID().longValue(),
                    updatedLoanProductsProductIdResponse.getAccountingMappings().getIncomeFromCapitalizationAccount().getId());
            Assertions.assertNotNull(updatedLoanProductsProductIdResponse.getCapitalizedIncomeType());
            Assertions.assertEquals(LoanCapitalizedIncomeType.INTEREST.getCode(),
                    updatedLoanProductsProductIdResponse.getCapitalizedIncomeType().getCode());
        }

        @Test
        public void testIncomeCapitalizationCumulativeNotSupported() {
            Assertions
                    .assertThrows(RuntimeException.class,
                            () -> loanProductHelper.createLoanProduct(createOnePeriod30DaysPeriodicAccrualProduct(7.0)
                                    .enableIncomeCapitalization(true)
                                    .capitalizedIncomeCalculationType(PostLoanProductsRequest.CapitalizedIncomeCalculationTypeEnum.FLAT)
                                    .capitalizedIncomeStrategy(PostLoanProductsRequest.CapitalizedIncomeStrategyEnum.EQUAL_AMORTIZATION)
                                    .deferredIncomeLiabilityAccountId(deferredIncomeLiabilityAccount.getAccountID().longValue())
                                    .incomeFromCapitalizationAccountId(feeIncomeAccount.getAccountID().longValue())
                                    .capitalizedIncomeType(PostLoanProductsRequest.CapitalizedIncomeTypeEnum.FEE)));
        }

        @Test
        public void testIncomeCapitalizationEnabledCalculationTypeNotProvided() {
            Assertions.assertThrows(RuntimeException.class,
                    () -> loanProductHelper.createLoanProduct(create4IProgressive().enableIncomeCapitalization(true)
                            .capitalizedIncomeStrategy(PostLoanProductsRequest.CapitalizedIncomeStrategyEnum.EQUAL_AMORTIZATION)
                            .deferredIncomeLiabilityAccountId(deferredIncomeLiabilityAccount.getAccountID().longValue())
                            .incomeFromCapitalizationAccountId(feeIncomeAccount.getAccountID().longValue())
                            .capitalizedIncomeType(PostLoanProductsRequest.CapitalizedIncomeTypeEnum.FEE)));
        }

        @Test
        public void testIncomeCapitalizationEnabledStrategyNotProvided() {
            Assertions.assertThrows(RuntimeException.class,
                    () -> loanProductHelper.createLoanProduct(create4IProgressive().enableIncomeCapitalization(true)
                            .capitalizedIncomeCalculationType(PostLoanProductsRequest.CapitalizedIncomeCalculationTypeEnum.FLAT)
                            .deferredIncomeLiabilityAccountId(deferredIncomeLiabilityAccount.getAccountID().longValue())
                            .incomeFromCapitalizationAccountId(feeIncomeAccount.getAccountID().longValue())
                            .capitalizedIncomeType(PostLoanProductsRequest.CapitalizedIncomeTypeEnum.FEE)));
        }

        @Test
        public void testIncomeCapitalizationEnabledDeferredIncomeLiabilityNotProvided() {
            Assertions.assertThrows(RuntimeException.class,
                    () -> loanProductHelper.createLoanProduct(create4IProgressive().enableIncomeCapitalization(true)
                            .capitalizedIncomeCalculationType(PostLoanProductsRequest.CapitalizedIncomeCalculationTypeEnum.FLAT)
                            .capitalizedIncomeStrategy(PostLoanProductsRequest.CapitalizedIncomeStrategyEnum.EQUAL_AMORTIZATION)
                            .incomeFromCapitalizationAccountId(feeIncomeAccount.getAccountID().longValue())
                            .capitalizedIncomeType(PostLoanProductsRequest.CapitalizedIncomeTypeEnum.FEE)));
        }

        @Test
        public void testIncomeCapitalizationEnabledIncomeFromCapitalizationNotProvided() {
            Assertions.assertThrows(RuntimeException.class,
                    () -> loanProductHelper.createLoanProduct(create4IProgressive().enableIncomeCapitalization(true)
                            .capitalizedIncomeCalculationType(PostLoanProductsRequest.CapitalizedIncomeCalculationTypeEnum.FLAT)
                            .capitalizedIncomeStrategy(PostLoanProductsRequest.CapitalizedIncomeStrategyEnum.EQUAL_AMORTIZATION)
                            .deferredIncomeLiabilityAccountId(deferredIncomeLiabilityAccount.getAccountID().longValue())
                            .capitalizedIncomeType(PostLoanProductsRequest.CapitalizedIncomeTypeEnum.FEE)));
        }

        @Test
        public void testIncomeCapitalizationEnabledIncomeTypeNotProvided() {
            Assertions.assertThrows(RuntimeException.class,
                    () -> loanProductHelper.createLoanProduct(create4IProgressive().enableIncomeCapitalization(true)
                            .capitalizedIncomeCalculationType(PostLoanProductsRequest.CapitalizedIncomeCalculationTypeEnum.FLAT)
                            .capitalizedIncomeStrategy(PostLoanProductsRequest.CapitalizedIncomeStrategyEnum.EQUAL_AMORTIZATION)
                            .deferredIncomeLiabilityAccountId(deferredIncomeLiabilityAccount.getAccountID().longValue())
                            .incomeFromCapitalizationAccountId(feeIncomeAccount.getAccountID().longValue())));
        }
    }

    @Nested
    public class BuyDownFeeTest {

        @Test
        public void testBuyDownFeeEnabled() {
            final PostClientsResponse client = clientHelper.createClient(ClientHelper.defaultClientCreationRequest());

            final PostLoanProductsResponse loanProductsResponse = loanProductHelper.createLoanProduct(create4IProgressive()
                    .enableBuyDownFee(true).buyDownFeeCalculationType(PostLoanProductsRequest.BuyDownFeeCalculationTypeEnum.FLAT)
                    .buyDownFeeStrategy(PostLoanProductsRequest.BuyDownFeeStrategyEnum.EQUAL_AMORTIZATION)
                    .buyDownFeeIncomeType(PostLoanProductsRequest.BuyDownFeeIncomeTypeEnum.FEE));

            final GetLoanProductsProductIdResponse loanProductsProductIdResponse = loanProductHelper
                    .retrieveLoanProductById(loanProductsResponse.getResourceId());
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
                Long loanId = applyAndApproveProgressiveLoan(client.getClientId(), loanProductsResponse.getResourceId(), "20 December 2024",
                        430.0, 7.0, 6, null);

                final GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId);
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
            final PostClientsResponse client = clientHelper.createClient(ClientHelper.defaultClientCreationRequest());

            final PostLoanProductsResponse loanProductsResponse = loanProductHelper.createLoanProduct(create4IProgressive()
                    .enableBuyDownFee(false).buyDownFeeCalculationType(PostLoanProductsRequest.BuyDownFeeCalculationTypeEnum.FLAT)
                    .buyDownFeeStrategy(PostLoanProductsRequest.BuyDownFeeStrategyEnum.EQUAL_AMORTIZATION)
                    .buyDownFeeIncomeType(PostLoanProductsRequest.BuyDownFeeIncomeTypeEnum.FEE));

            final GetLoanProductsProductIdResponse loanProductsProductIdResponse = loanProductHelper
                    .retrieveLoanProductById(loanProductsResponse.getResourceId());
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
                Long loanId = applyAndApproveProgressiveLoan(client.getClientId(), loanProductsResponse.getResourceId(), "20 December 2024",
                        430.0, 7.0, 6, null);

                final GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId);
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
            final PostLoanProductsResponse loanProductsResponse = loanProductHelper.createLoanProduct(create4IProgressive()
                    .enableBuyDownFee(true).buyDownFeeCalculationType(PostLoanProductsRequest.BuyDownFeeCalculationTypeEnum.FLAT)
                    .buyDownFeeStrategy(PostLoanProductsRequest.BuyDownFeeStrategyEnum.EQUAL_AMORTIZATION)
                    .buyDownFeeIncomeType(PostLoanProductsRequest.BuyDownFeeIncomeTypeEnum.FEE));

            final GetLoanProductsProductIdResponse loanProductsProductIdResponse = loanProductHelper
                    .retrieveLoanProductById(loanProductsResponse.getResourceId());
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

            loanProductHelper.updateLoanProductById(loanProductsResponse.getResourceId(), new PutLoanProductsProductIdRequest()
                    .enableBuyDownFee(false).buyDownFeeIncomeType(PutLoanProductsProductIdRequest.BuyDownFeeIncomeTypeEnum.INTEREST));

            final GetLoanProductsProductIdResponse updatedLoanProductsProductIdResponse = loanProductHelper
                    .retrieveLoanProductById(loanProductsResponse.getResourceId());
            Assertions.assertEquals(Boolean.FALSE, updatedLoanProductsProductIdResponse.getEnableBuyDownFee());
            Assertions.assertNotNull(updatedLoanProductsProductIdResponse.getBuyDownFeeCalculationType());
            Assertions.assertEquals(LoanBuyDownFeeCalculationType.FLAT.getCode(),
                    updatedLoanProductsProductIdResponse.getBuyDownFeeCalculationType().getCode());
            Assertions.assertNotNull(updatedLoanProductsProductIdResponse.getBuyDownFeeStrategy());
            Assertions.assertEquals(LoanBuyDownFeeStrategy.EQUAL_AMORTIZATION.getCode(),
                    updatedLoanProductsProductIdResponse.getBuyDownFeeStrategy().getCode());
            Assertions.assertNotNull(updatedLoanProductsProductIdResponse.getBuyDownFeeIncomeType());
            Assertions.assertEquals(LoanBuyDownFeeIncomeType.INTEREST.getCode(),
                    updatedLoanProductsProductIdResponse.getBuyDownFeeIncomeType().getCode());
        }

        @Test
        public void testBuyDownFeeCumulativeNotSupported() {
            Assertions.assertThrows(RuntimeException.class,
                    () -> loanProductHelper.createLoanProduct(createOnePeriod30DaysPeriodicAccrualProduct(7.0).enableBuyDownFee(true)
                            .buyDownFeeCalculationType(PostLoanProductsRequest.BuyDownFeeCalculationTypeEnum.FLAT)
                            .buyDownFeeStrategy(PostLoanProductsRequest.BuyDownFeeStrategyEnum.EQUAL_AMORTIZATION)
                            .buyDownFeeIncomeType(PostLoanProductsRequest.BuyDownFeeIncomeTypeEnum.FEE)));
        }

        @Test
        public void testBuyDownFeeEnabledCalculationTypeNotProvided() {
            Assertions.assertThrows(RuntimeException.class,
                    () -> loanProductHelper.createLoanProduct(create4IProgressive().enableBuyDownFee(true)
                            .buyDownFeeStrategy(PostLoanProductsRequest.BuyDownFeeStrategyEnum.EQUAL_AMORTIZATION)
                            .buyDownFeeIncomeType(PostLoanProductsRequest.BuyDownFeeIncomeTypeEnum.FEE)));
        }

        @Test
        public void testBuyDownFeeEnabledStrategyNotProvided() {
            Assertions.assertThrows(RuntimeException.class,
                    () -> loanProductHelper.createLoanProduct(create4IProgressive().enableBuyDownFee(true)
                            .buyDownFeeCalculationType(PostLoanProductsRequest.BuyDownFeeCalculationTypeEnum.FLAT)
                            .buyDownFeeIncomeType(PostLoanProductsRequest.BuyDownFeeIncomeTypeEnum.FEE)));
        }

        @Test
        public void testBuyDownFeeEnabledIncomeTypeNotProvided() {
            Assertions.assertThrows(RuntimeException.class,
                    () -> loanProductHelper.createLoanProduct(create4IProgressive().enableBuyDownFee(true)
                            .buyDownFeeCalculationType(PostLoanProductsRequest.BuyDownFeeCalculationTypeEnum.FLAT)
                            .buyDownFeeStrategy(PostLoanProductsRequest.BuyDownFeeStrategyEnum.EQUAL_AMORTIZATION)));
        }
    }
}
