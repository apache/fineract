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
import org.apache.fineract.portfolio.loanaccount.domain.LoanCapitalizedIncomeCalculationType;
import org.apache.fineract.portfolio.loanaccount.domain.LoanCapitalizedIncomeStrategy;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class LoanProductTest extends BaseLoanIntegrationTest {

    @Test
    public void testIncomeCapitalizationEnabled() {
        final PostClientsResponse client = clientHelper.createClient(ClientHelper.defaultClientCreationRequest());

        final PostLoanProductsResponse loanProductsResponse = loanProductHelper
                .createLoanProduct(create4IProgressive().enableIncomeCapitalization(true)
                        .capitalizedIncomeCalculationType(PostLoanProductsRequest.CapitalizedIncomeCalculationTypeEnum.FLAT)
                        .capitalizedIncomeStrategy(PostLoanProductsRequest.CapitalizedIncomeStrategyEnum.EQUAL_AMORTIZATION));

        final GetLoanProductsProductIdResponse loanProductsProductIdResponse = loanProductHelper
                .retrieveLoanProductById(loanProductsResponse.getResourceId());
        Assertions.assertEquals(Boolean.TRUE, loanProductsProductIdResponse.getEnableIncomeCapitalization());
        Assertions.assertNotNull(loanProductsProductIdResponse.getCapitalizedIncomeCalculationType());
        Assertions.assertEquals(LoanCapitalizedIncomeCalculationType.FLAT.getCode(),
                loanProductsProductIdResponse.getCapitalizedIncomeCalculationType().getCode());
        Assertions.assertNotNull(loanProductsProductIdResponse.getCapitalizedIncomeStrategy());
        Assertions.assertEquals(LoanCapitalizedIncomeStrategy.EQUAL_AMORTIZATION.getCode(),
                loanProductsProductIdResponse.getCapitalizedIncomeStrategy().getCode());

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

            Assertions.assertDoesNotThrow(() -> disburseLoan(loanId, BigDecimal.valueOf(430), "20 December 2024"));
        });
    }

    @Test
    public void testIncomeCapitalizationDisabled() {
        final PostClientsResponse client = clientHelper.createClient(ClientHelper.defaultClientCreationRequest());

        final PostLoanProductsResponse loanProductsResponse = loanProductHelper
                .createLoanProduct(create4IProgressive().enableIncomeCapitalization(false)
                        .capitalizedIncomeCalculationType(PostLoanProductsRequest.CapitalizedIncomeCalculationTypeEnum.FLAT)
                        .capitalizedIncomeStrategy(PostLoanProductsRequest.CapitalizedIncomeStrategyEnum.EQUAL_AMORTIZATION));

        final GetLoanProductsProductIdResponse loanProductsProductIdResponse = loanProductHelper
                .retrieveLoanProductById(loanProductsResponse.getResourceId());
        Assertions.assertEquals(Boolean.FALSE, loanProductsProductIdResponse.getEnableIncomeCapitalization());
        Assertions.assertNotNull(loanProductsProductIdResponse.getCapitalizedIncomeCalculationType());
        Assertions.assertEquals(LoanCapitalizedIncomeCalculationType.FLAT.getCode(),
                loanProductsProductIdResponse.getCapitalizedIncomeCalculationType().getCode());
        Assertions.assertNotNull(loanProductsProductIdResponse.getCapitalizedIncomeStrategy());
        Assertions.assertEquals(LoanCapitalizedIncomeStrategy.EQUAL_AMORTIZATION.getCode(),
                loanProductsProductIdResponse.getCapitalizedIncomeStrategy().getCode());

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

            Assertions.assertDoesNotThrow(() -> disburseLoan(loanId, BigDecimal.valueOf(430), "20 December 2024"));
        });
    }

    @Test
    public void testIncomeCapitalizationUpdateProduct() {
        final PostLoanProductsResponse loanProductsResponse = loanProductHelper
                .createLoanProduct(create4IProgressive().enableIncomeCapitalization(true)
                        .capitalizedIncomeCalculationType(PostLoanProductsRequest.CapitalizedIncomeCalculationTypeEnum.FLAT)
                        .capitalizedIncomeStrategy(PostLoanProductsRequest.CapitalizedIncomeStrategyEnum.EQUAL_AMORTIZATION));

        final GetLoanProductsProductIdResponse loanProductsProductIdResponse = loanProductHelper
                .retrieveLoanProductById(loanProductsResponse.getResourceId());
        Assertions.assertEquals(Boolean.TRUE, loanProductsProductIdResponse.getEnableIncomeCapitalization());
        Assertions.assertNotNull(loanProductsProductIdResponse.getCapitalizedIncomeCalculationType());
        Assertions.assertEquals(LoanCapitalizedIncomeCalculationType.FLAT.getCode(),
                loanProductsProductIdResponse.getCapitalizedIncomeCalculationType().getCode());
        Assertions.assertNotNull(loanProductsProductIdResponse.getCapitalizedIncomeStrategy());
        Assertions.assertEquals(LoanCapitalizedIncomeStrategy.EQUAL_AMORTIZATION.getCode(),
                loanProductsProductIdResponse.getCapitalizedIncomeStrategy().getCode());

        loanProductHelper.updateLoanProductById(loanProductsResponse.getResourceId(),
                new PutLoanProductsProductIdRequest().enableIncomeCapitalization(false));

        final GetLoanProductsProductIdResponse updatedLoanProductsProductIdResponse = loanProductHelper
                .retrieveLoanProductById(loanProductsResponse.getResourceId());
        Assertions.assertEquals(Boolean.FALSE, updatedLoanProductsProductIdResponse.getEnableIncomeCapitalization());
        Assertions.assertNotNull(updatedLoanProductsProductIdResponse.getCapitalizedIncomeCalculationType());
        Assertions.assertEquals(LoanCapitalizedIncomeCalculationType.FLAT.getCode(),
                updatedLoanProductsProductIdResponse.getCapitalizedIncomeCalculationType().getCode());
        Assertions.assertNotNull(updatedLoanProductsProductIdResponse.getCapitalizedIncomeStrategy());
        Assertions.assertEquals(LoanCapitalizedIncomeStrategy.EQUAL_AMORTIZATION.getCode(),
                updatedLoanProductsProductIdResponse.getCapitalizedIncomeStrategy().getCode());
    }

    @Test
    public void testIncomeCapitalizationCumulativeNotSupported() {
        Assertions.assertThrows(RuntimeException.class,
                () -> loanProductHelper.createLoanProduct(createOnePeriod30DaysPeriodicAccrualProduct(7.0).enableIncomeCapitalization(true)
                        .capitalizedIncomeCalculationType(PostLoanProductsRequest.CapitalizedIncomeCalculationTypeEnum.FLAT)
                        .capitalizedIncomeStrategy(PostLoanProductsRequest.CapitalizedIncomeStrategyEnum.EQUAL_AMORTIZATION)));
    }

}
