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
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.client.models.GetLoanProductsProductIdResponse;
import org.apache.fineract.client.models.GetLoanProductsTemplateResponse;
import org.apache.fineract.client.models.GetLoanProductsWriteOffReasonOptions;
import org.apache.fineract.client.models.GetLoansLoanIdResponse;
import org.apache.fineract.client.models.PostClientsResponse;
import org.apache.fineract.client.models.PostCodeValueDataResponse;
import org.apache.fineract.client.models.PostCodeValuesDataRequest;
import org.apache.fineract.client.models.PostLoanProductsRequest;
import org.apache.fineract.client.models.PostLoanProductsResponse;
import org.apache.fineract.client.models.PostWriteOffReasonToExpenseAccountMappings;
import org.apache.fineract.client.models.PutLoanProductsProductIdRequest;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.FineractClientHelper;
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

@Slf4j
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
                    new PutLoanProductsProductIdRequest()
                            .incomeFromCapitalizationAccountId(interestIncomeAccount.getAccountID().longValue())
                            .capitalizedIncomeType(PutLoanProductsProductIdRequest.CapitalizedIncomeTypeEnum.INTEREST));
            GetLoanProductsProductIdResponse updatedLoanProductsProductIdResponse = loanProductHelper
                    .retrieveLoanProductById(loanProductsResponse.getResourceId());
            Assertions.assertEquals(Boolean.TRUE, updatedLoanProductsProductIdResponse.getEnableIncomeCapitalization());
            Assertions.assertNotNull(updatedLoanProductsProductIdResponse.getCapitalizedIncomeCalculationType());
            Assertions.assertEquals(LoanCapitalizedIncomeCalculationType.FLAT.getCode(),
                    updatedLoanProductsProductIdResponse.getCapitalizedIncomeCalculationType().getCode());
            Assertions.assertNotNull(updatedLoanProductsProductIdResponse.getCapitalizedIncomeStrategy());
            Assertions.assertEquals(LoanCapitalizedIncomeStrategy.EQUAL_AMORTIZATION.getCode(),
                    updatedLoanProductsProductIdResponse.getCapitalizedIncomeStrategy().getCode());
            Assertions.assertNotNull(updatedLoanProductsProductIdResponse.getAccountingMappings());
            Assertions.assertEquals(interestIncomeAccount.getAccountID().longValue(),
                    updatedLoanProductsProductIdResponse.getAccountingMappings().getIncomeFromCapitalizationAccount().getId());

            loanProductHelper.updateLoanProductById(loanProductsResponse.getResourceId(),
                    new PutLoanProductsProductIdRequest().enableIncomeCapitalization(false));

            updatedLoanProductsProductIdResponse = loanProductHelper.retrieveLoanProductById(loanProductsResponse.getResourceId());
            Assertions.assertEquals(Boolean.FALSE, updatedLoanProductsProductIdResponse.getEnableIncomeCapitalization());
            Assertions.assertNotNull(updatedLoanProductsProductIdResponse.getAccountingMappings());
            Assertions.assertNull(updatedLoanProductsProductIdResponse.getAccountingMappings().getIncomeFromCapitalizationAccount());
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
                    .buyDownFeeIncomeType(PostLoanProductsRequest.BuyDownFeeIncomeTypeEnum.FEE)
                    .buyDownExpenseAccountId(buyDownExpenseAccount.getAccountID().longValue()).merchantBuyDownFee(true)
                    .incomeFromBuyDownAccountId(feeIncomeAccount.getAccountID().longValue()));

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
                    .buyDownFeeIncomeType(PostLoanProductsRequest.BuyDownFeeIncomeTypeEnum.FEE)
                    .buyDownExpenseAccountId(buyDownExpenseAccount.getAccountID().longValue())
                    .incomeFromBuyDownAccountId(feeIncomeAccount.getAccountID().longValue()));

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
                    .buyDownFeeIncomeType(PostLoanProductsRequest.BuyDownFeeIncomeTypeEnum.FEE)
                    .buyDownExpenseAccountId(buyDownExpenseAccount.getAccountID().longValue()).merchantBuyDownFee(true)
                    .incomeFromBuyDownAccountId(feeIncomeAccount.getAccountID().longValue()));

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

            Assertions.assertNotNull(loanProductsProductIdResponse.getAccountingMappings());
            Assertions.assertEquals(buyDownExpenseAccount.getAccountID().longValue(),
                    loanProductsProductIdResponse.getAccountingMappings().getBuyDownExpenseAccount().getId());
            Assertions.assertEquals(feeIncomeAccount.getAccountID().longValue(),
                    loanProductsProductIdResponse.getAccountingMappings().getIncomeFromBuyDownAccount().getId());

            loanProductHelper.updateLoanProductById(loanProductsResponse.getResourceId(),
                    new PutLoanProductsProductIdRequest()
                            .buyDownFeeIncomeType(PutLoanProductsProductIdRequest.BuyDownFeeIncomeTypeEnum.INTEREST)
                            .incomeFromBuyDownAccountId(interestIncomeAccount.getAccountID().longValue()));

            GetLoanProductsProductIdResponse updatedLoanProductsProductIdResponse = loanProductHelper
                    .retrieveLoanProductById(loanProductsResponse.getResourceId());
            Assertions.assertEquals(Boolean.TRUE, updatedLoanProductsProductIdResponse.getEnableBuyDownFee());
            Assertions.assertEquals(LoanBuyDownFeeStrategy.EQUAL_AMORTIZATION.getCode(),
                    updatedLoanProductsProductIdResponse.getBuyDownFeeStrategy().getCode());
            Assertions.assertNotNull(updatedLoanProductsProductIdResponse.getBuyDownFeeIncomeType());
            Assertions.assertEquals(LoanBuyDownFeeIncomeType.INTEREST.getCode(),
                    updatedLoanProductsProductIdResponse.getBuyDownFeeIncomeType().getCode());
            Assertions.assertNotNull(updatedLoanProductsProductIdResponse.getAccountingMappings());
            Assertions.assertEquals(buyDownExpenseAccount.getAccountID().longValue(),
                    updatedLoanProductsProductIdResponse.getAccountingMappings().getBuyDownExpenseAccount().getId());
            Assertions.assertEquals(interestIncomeAccount.getAccountID().longValue(),
                    updatedLoanProductsProductIdResponse.getAccountingMappings().getIncomeFromBuyDownAccount().getId());

            loanProductHelper.updateLoanProductById(loanProductsResponse.getResourceId(),
                    new PutLoanProductsProductIdRequest().enableBuyDownFee(false));

            updatedLoanProductsProductIdResponse = loanProductHelper.retrieveLoanProductById(loanProductsResponse.getResourceId());
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
            Assertions.assertThrows(RuntimeException.class,
                    () -> loanProductHelper.createLoanProduct(createOnePeriod30DaysPeriodicAccrualProduct(7.0).enableBuyDownFee(true)
                            .buyDownFeeCalculationType(PostLoanProductsRequest.BuyDownFeeCalculationTypeEnum.FLAT)
                            .buyDownFeeStrategy(PostLoanProductsRequest.BuyDownFeeStrategyEnum.EQUAL_AMORTIZATION)
                            .buyDownFeeIncomeType(PostLoanProductsRequest.BuyDownFeeIncomeTypeEnum.FEE)
                            .buyDownExpenseAccountId(buyDownExpenseAccount.getAccountID().longValue()).merchantBuyDownFee(true)
                            .incomeFromBuyDownAccountId(feeIncomeAccount.getAccountID().longValue())));
        }

        @Test
        public void testBuyDownFeeEnabledCalculationTypeNotProvided() {
            Assertions.assertThrows(RuntimeException.class,
                    () -> loanProductHelper.createLoanProduct(create4IProgressive().enableBuyDownFee(true)
                            .buyDownFeeStrategy(PostLoanProductsRequest.BuyDownFeeStrategyEnum.EQUAL_AMORTIZATION)
                            .buyDownFeeIncomeType(PostLoanProductsRequest.BuyDownFeeIncomeTypeEnum.FEE)
                            .buyDownExpenseAccountId(buyDownExpenseAccount.getAccountID().longValue()).merchantBuyDownFee(true)
                            .incomeFromBuyDownAccountId(feeIncomeAccount.getAccountID().longValue())));
        }

        @Test
        public void testBuyDownFeeEnabledStrategyNotProvided() {
            Assertions.assertThrows(RuntimeException.class,
                    () -> loanProductHelper.createLoanProduct(create4IProgressive().enableBuyDownFee(true)
                            .buyDownFeeCalculationType(PostLoanProductsRequest.BuyDownFeeCalculationTypeEnum.FLAT)
                            .buyDownFeeIncomeType(PostLoanProductsRequest.BuyDownFeeIncomeTypeEnum.FEE)
                            .buyDownExpenseAccountId(buyDownExpenseAccount.getAccountID().longValue()).merchantBuyDownFee(true)
                            .incomeFromBuyDownAccountId(feeIncomeAccount.getAccountID().longValue())));
        }

        @Test
        public void testBuyDownFeeEnabledIncomeTypeNotProvided() {
            Assertions.assertThrows(RuntimeException.class,
                    () -> loanProductHelper.createLoanProduct(create4IProgressive().enableBuyDownFee(true)
                            .buyDownFeeCalculationType(PostLoanProductsRequest.BuyDownFeeCalculationTypeEnum.FLAT)
                            .buyDownFeeStrategy(PostLoanProductsRequest.BuyDownFeeStrategyEnum.EQUAL_AMORTIZATION)
                            .buyDownExpenseAccountId(buyDownExpenseAccount.getAccountID().longValue()).merchantBuyDownFee(true)
                            .incomeFromBuyDownAccountId(feeIncomeAccount.getAccountID().longValue())));
        }

        @Test
        public void testBuyDownFeeEnabledBuyDownExpenseNotProvided() {
            Assertions.assertThrows(RuntimeException.class,
                    () -> loanProductHelper.createLoanProduct(create4IProgressive().enableBuyDownFee(true)
                            .buyDownFeeCalculationType(PostLoanProductsRequest.BuyDownFeeCalculationTypeEnum.FLAT)
                            .buyDownFeeStrategy(PostLoanProductsRequest.BuyDownFeeStrategyEnum.EQUAL_AMORTIZATION)
                            .buyDownFeeIncomeType(PostLoanProductsRequest.BuyDownFeeIncomeTypeEnum.FEE).merchantBuyDownFee(true)
                            .incomeFromBuyDownAccountId(feeIncomeAccount.getAccountID().longValue())));
        }

        @Test
        public void testBuyDownFeeEnabledIncomeFromBuyDownNotProvided() {
            Assertions.assertThrows(RuntimeException.class,
                    () -> loanProductHelper.createLoanProduct(create4IProgressive().enableBuyDownFee(true)
                            .buyDownFeeCalculationType(PostLoanProductsRequest.BuyDownFeeCalculationTypeEnum.FLAT)
                            .buyDownFeeStrategy(PostLoanProductsRequest.BuyDownFeeStrategyEnum.EQUAL_AMORTIZATION)
                            .buyDownFeeIncomeType(PostLoanProductsRequest.BuyDownFeeIncomeTypeEnum.FEE).merchantBuyDownFee(true)
                            .buyDownExpenseAccountId(buyDownExpenseAccount.getAccountID().longValue())));
        }
    }

    @Nested
    public class WriteOffReasonsToExpenseMappings {

        @Test
        public void testWriteOffReasonToExpenseAccountMapping_shouldFail_on_nonExistingGLAccount_And_nonExistingWriteOffReason() {
            try {
                loanProductHelper.createLoanProduct(
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
                loanProductHelper.createLoanProduct(create4IProgressive().addWriteOffReasonsToExpenseMappingsItem(
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
                loanProductHelper.createLoanProduct(create4IProgressive().addWriteOffReasonsToExpenseMappingsItem(
                        new PostWriteOffReasonToExpenseAccountMappings().expenseAccountId("111").writeOffReasonCodeValueId("asdf323")));
                Assertions.fail("Should have thrown an IllegalArgumentException");
            } catch (final RuntimeException ex) {
                log.info("Exception: {}", ex.getMessage());
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
            GetLoanProductsTemplateResponse loanProductTemplate = loanProductHelper.getLoanProductTemplate(false);
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
            String expenseAccountId = buyDownExpenseAccount.getAccountID().toString();

            Long loanProductId = loanProductHelper.createLoanProduct(
                    create4IProgressive().addWriteOffReasonsToExpenseMappingsItem(new PostWriteOffReasonToExpenseAccountMappings()
                            .expenseAccountId(expenseAccountId).writeOffReasonCodeValueId(reasonCodeId)))
                    .getResourceId();

            // Verify that get loan product API has the corresponding fields
            GetLoanProductsProductIdResponse getLoanProductsProductIdResponse = loanProductHelper.retrieveLoanProductById(loanProductId);
            List<PostWriteOffReasonToExpenseAccountMappings> writeOffReasonToExpenseAccountMappings = getLoanProductsProductIdResponse
                    .getWriteOffReasonsToExpenseMappings();
            Assertions.assertNotNull(writeOffReasonToExpenseAccountMappings);
            Assertions.assertEquals(1, writeOffReasonToExpenseAccountMappings.size());
            PostWriteOffReasonToExpenseAccountMappings writeOffMapping = writeOffReasonToExpenseAccountMappings.getFirst();
            Assertions.assertNotNull(writeOffMapping);
            Assertions.assertEquals(expenseAccountId, writeOffMapping.getExpenseAccountId());
            Assertions.assertEquals(reasonCodeId, writeOffMapping.getWriteOffReasonCodeValueId());

            List<GetLoanProductsWriteOffReasonOptions> writeOffReasonOptionsResultNonTemplate = getLoanProductsProductIdResponse
                    .getWriteOffReasonOptions();
            if (writeOffReasonOptionsResultNonTemplate != null && !writeOffReasonOptionsResultNonTemplate.isEmpty()) {
                Assertions.fail("Write-off reason options with no template setting should be empty");
            }

            // test Update loan product API - delete writeOffReasonsToExpenseMappings

            GetLoanProductsProductIdResponse getLoanProductsProductId = loanProductHelper.retrieveLoanProductById(loanProductId);

            loanProductHelper.updateLoanProductById(loanProductId,
                    update4IProgressive(getLoanProductsProductId.getName(), getLoanProductsProductId.getShortName(),
                            getLoanProductsProductId.getDelinquencyBucket().getId()).writeOffReasonsToExpenseMappings(List.of()));

            // Verify that get loan product API has the corresponding fields
            Assertions.assertNull(loanProductHelper.retrieveLoanProductById(loanProductId).getWriteOffReasonsToExpenseMappings());
        }
    }

    private Long createTestWriteOffReason() {
        PostCodeValueDataResponse response = okR(FineractClientHelper.getFineractClient().codeValues.createCodeValue(26L,
                new PostCodeValuesDataRequest().name(Utils.uniqueRandomStringGenerator("TestWriteOffReason_1_", 6))
                        .description("Test write off reason value 1").isActive(true).position(0)))
                .body();
        Assertions.assertNotNull(response);
        Assertions.assertNotNull(response.getSubResourceId());
        return response.getSubResourceId();
    }
}
