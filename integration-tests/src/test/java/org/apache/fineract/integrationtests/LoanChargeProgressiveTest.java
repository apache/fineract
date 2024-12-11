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
import java.time.LocalDate;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.client.models.GetLoansLoanIdResponse;
import org.apache.fineract.client.models.PostChargesResponse;
import org.apache.fineract.client.models.PostLoanProductsResponse;
import org.apache.fineract.client.models.PostLoansResponse;
import org.apache.fineract.infrastructure.configuration.api.GlobalConfigurationConstants;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@Slf4j
public class LoanChargeProgressiveTest extends BaseLoanIntegrationTest {

    private Long clientId;
    private Long loanId;

    // create client, progressive loan product, loan with disburse limit 1000 for the client,
    // and disburse 250 on 01 June 2024
    @BeforeEach
    public void beforeEach() {
        runAt("01 June 2024", () -> {
            clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();
            final PostLoanProductsResponse loanProductsResponse = loanProductHelper.createLoanProduct(create4IProgressive());
            PostLoansResponse postLoansResponse = loanTransactionHelper.applyLoan(
                    applyLP2ProgressiveLoanRequest(clientId, loanProductsResponse.getResourceId(), "01 June 2024", 1000.0, 10.0, 4, null));
            loanId = postLoansResponse.getLoanId();
            loanTransactionHelper.approveLoan(loanId, approveLoanRequest(1000.0, "01 June 2024"));
            disburseLoan(loanId, BigDecimal.valueOf(250.0), "01 June 2024");
        });
    }

    @Test
    public void loanChargeAfterMaturityTest() {
        runAt("02 October 2024", () -> {
            final PostChargesResponse chargeResponse = createCharge(20.0d, "EUR");
            addLoanCharge(loanId, chargeResponse.getResourceId(), "02 October 2024", 20.0d);

            final GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId);
            validateRepaymentPeriod(loanDetails, 5, LocalDate.of(2024, 10, 2), 0, 20, 0, 0);

            executeInlineCOB(loanId);

            final GetLoansLoanIdResponse loanDetails2 = loanTransactionHelper.getLoanDetails(loanId);
            validateRepaymentPeriod(loanDetails2, 5, LocalDate.of(2024, 10, 2), 0, 20, 0, 0);
        });
    }

    @Test
    public void immediateChargeAccrualPostMaturityTest() {
        runAt("03 October 2024", () -> {
            executeInlineCOB(loanId);
            globalConfigurationHelper.manageConfigurations(GlobalConfigurationConstants.ENABLE_IMMEDIATE_CHARGE_ACCRUAL_POST_MATURITY,
                    true);
            final PostChargesResponse chargeResponse = createCharge(20.0d, "EUR");
            addLoanCharge(loanId, chargeResponse.getResourceId(), "03 October 2024", 20.0d);
            final GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId);
            Assertions.assertTrue(
                    loanDetails.getTransactions().stream().noneMatch(t -> t.getType().getAccrual() && t.getAmount().equals(20.0d)));
        });
        runAt("04 October 2024", () -> {
            globalConfigurationHelper.manageConfigurations(GlobalConfigurationConstants.ENABLE_IMMEDIATE_CHARGE_ACCRUAL_POST_MATURITY,
                    false);
            executeInlineCOB(loanId);
            final GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId);
            Assertions.assertTrue(loanDetails.getTransactions().stream()
                    .anyMatch(t -> t.getType().getAccrual() && t.getFeeChargesPortion().equals(20.0d)));
        });
    }

    @AfterEach
    public void afterEach() {
        globalConfigurationHelper.manageConfigurations(GlobalConfigurationConstants.ENABLE_IMMEDIATE_CHARGE_ACCRUAL_POST_MATURITY, false);
    }
}
