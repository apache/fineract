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
import org.apache.fineract.client.models.PostClientsResponse;
import org.apache.fineract.client.models.PostLoanProductsResponse;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.junit.jupiter.api.Test;

public class ProgressiveLoanMoratoriumIntegrationTest extends BaseLoanIntegrationTest {

    @Test
    public void testProgressivePrincipalMoratoriumSchedule() {
        final PostClientsResponse client = clientHelper.createClient(ClientHelper.defaultClientCreationRequest());

        runAt("1 January 2024", () -> {
            PostLoanProductsResponse loanProduct = loanProductHelper.createLoanProduct(create4IProgressive().principal(100.0)
                    .minPrincipal(100.0).maxPrincipal(100.0).numberOfRepayments(6).interestRatePerPeriod(7.0));

            Long loanId = applyAndApproveProgressiveLoan(client.getClientId(), loanProduct.getResourceId(), "1 January 2024", 100.0, 7.0, 6,
                    request -> request.graceOnPrincipalPayment(2));

            disburseLoan(loanId, BigDecimal.valueOf(100.0), "1 January 2024");

            verifyRepaymentSchedule(loanId, installment(100.0, null, "01 January 2024"),
                    installment(0.0, 0.58, 0.58, false, "01 February 2024"), installment(0.0, 0.58, 0.58, false, "01 March 2024"), //
                    installment(24.79, 0.58, 25.37, false, "01 April 2024"), //
                    installment(24.93, 0.44, 25.37, false, "01 May 2024"), //
                    installment(25.08, 0.29, 25.37, false, "01 June 2024"), //
                    installment(25.20, 0.15, 25.35, false, "01 July 2024"));
        });
    }

    @Test
    public void testProgressiveInterestMoratoriumSchedule() {
        final PostClientsResponse client = clientHelper.createClient(ClientHelper.defaultClientCreationRequest());

        runAt("1 January 2024", () -> {
            PostLoanProductsResponse loanProduct = loanProductHelper.createLoanProduct(create4IProgressive().principal(100.0)
                    .minPrincipal(100.0).maxPrincipal(100.0).numberOfRepayments(6).interestRatePerPeriod(7.0));

            Long loanId = applyAndApproveProgressiveLoan(client.getClientId(), loanProduct.getResourceId(), "1 January 2024", 100.0, 7.0, 6,
                    request -> request.graceOnInterestPayment(2));

            disburseLoan(loanId, BigDecimal.valueOf(100.0), "1 January 2024");

            verifyRepaymentSchedule(loanId, installment(100.0, null, "01 January 2024"),
                    installment(17.01, 0.0, 17.01, false, "01 February 2024"), installment(17.01, 0.0, 17.01, false, "01 March 2024"), //
                    installment(15.57, 1.44, 17.01, false, "01 April 2024"), //
                    installment(16.72, 0.29, 17.01, false, "01 May 2024"), //
                    installment(16.81, 0.20, 17.01, false, "01 June 2024"), //
                    installment(16.88, 0.10, 16.98, false, "01 July 2024"));
        });
    }
}
