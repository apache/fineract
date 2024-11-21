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
import java.util.HashMap;
import org.apache.fineract.client.models.GetLoansLoanIdResponse;
import org.apache.fineract.client.models.PostLoanProductsResponse;
import org.apache.fineract.client.models.PostLoansResponse;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class LoanPayOffTest extends BaseLoanIntegrationTest {

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
    public void testPayOffOnDisbursementDate() {
        runAt("01 June 2024", () -> {
            final HashMap prepayAmount = loanTransactionHelper.getPrepayAmount(requestSpec, responseSpec, loanId.intValue());

            Assertions.assertEquals(250.0f, prepayAmount.get("amount"));

            loanTransactionHelper.makeLoanRepayment("01 June 2024", 250.0f, loanId.intValue());

            final GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId);
            Assertions.assertNotNull(loanDetails.getStatus());
            Assertions.assertEquals(Boolean.TRUE, loanDetails.getStatus().getClosedObligationsMet());
        });
    }
}
