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
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.client.models.GetLoansLoanIdResponse;
import org.apache.fineract.client.models.PostLoanProductsResponse;
import org.apache.fineract.client.models.PostLoansResponse;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;

@Slf4j
public class LoanPrepayAmountTest extends BaseLoanIntegrationTest {

    Long clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();
    Long loanId;

    @Test
    public void testLoanPrepayAmountProgressive() {
        runAt("1 January 2024", () -> {
            final PostLoanProductsResponse loanProductsResponse = loanProductHelper.createLoanProduct(create4IProgressive());
            PostLoansResponse postLoansResponse = loanTransactionHelper.applyLoan(applyLP2ProgressiveLoanRequest(clientId,
                    loanProductsResponse.getResourceId(), "01 January 2024", 1000.0, 9.99, 6, null));
            loanId = postLoansResponse.getLoanId();
            loanTransactionHelper.approveLoan(loanId, approveLoanRequest(1000.0, "01 January 2024"));
            disburseLoan(loanId, BigDecimal.valueOf(250.0), "01 January 2024");
        });
        runAt("7 january 2024", () -> {
            disburseLoan(loanId, BigDecimal.valueOf(350.0), "04 January 2024");
            disburseLoan(loanId, BigDecimal.valueOf(400.0), "05 January 2024");
        });
        for (int i = 7; i <= 31; i++) {
            runAt(i + " January 2024", () -> {
                GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId);
                HashMap prepayAmount = loanTransactionHelper.getPrepayAmount(requestSpec, responseSpec, loanId.intValue());
                Assertions.assertEquals((float) prepayAmount.get("interestPortion"),
                        loanDetails.getSummary().getTotalUnpaidPayableNotDueInterest().floatValue());
            });
        }
    }

}
