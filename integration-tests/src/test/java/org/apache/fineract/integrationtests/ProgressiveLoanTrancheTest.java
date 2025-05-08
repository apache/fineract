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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.fineract.client.models.DisbursementDetail;
import org.apache.fineract.client.models.GetLoansLoanIdDisbursementDetails;
import org.apache.fineract.client.models.GetLoansLoanIdResponse;
import org.apache.fineract.client.models.PostClientsResponse;
import org.apache.fineract.client.models.PostLoanProductsResponse;
import org.apache.fineract.client.models.PostLoansDisbursementData;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ProgressiveLoanTrancheTest extends BaseLoanIntegrationTest {

    @Test
    public void testProgressiveLoanTrancheDisbursement() {
        final PostClientsResponse client = clientHelper.createClient(ClientHelper.defaultClientCreationRequest());

        final PostLoanProductsResponse loanProductsResponse = loanProductHelper
                .createLoanProduct(create4IProgressive().disallowExpectedDisbursements(false).allowApprovedDisbursedAmountsOverApplied(null)
                        .overAppliedCalculationType(null).overAppliedNumber(null));

        final AtomicReference<Long> loanIdRef = new AtomicReference<>();

        runAt("20 December 2024", () -> {
            Long loanId = applyAndApproveProgressiveLoan(client.getClientId(), loanProductsResponse.getResourceId(), "20 December 2024",
                    500.0, 7.0, 6, (request) -> request.disbursementData(List.of(new PostLoansDisbursementData()
                            .expectedDisbursementDate("20 December 2024").principal(BigDecimal.valueOf(100.0)))));

            loanIdRef.set(loanId);

            disburseLoan(loanId, BigDecimal.valueOf(100), "20 December 2024");

        });
        runAt("20 January 2025", () -> {
            Long loanId = loanIdRef.get();

            // Can't disburse without undisbursed tranche
            Assertions.assertThrows(RuntimeException.class, () -> {
                disburseLoan(loanId, BigDecimal.valueOf(100), "20 January 2025");
            });

            final GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId);

            ArrayList<DisbursementDetail> disbursementDetails = new ArrayList<>();
            for (GetLoansLoanIdDisbursementDetails disbursementDetail : loanDetails.getDisbursementDetails()) {
                disbursementDetails.add(new DisbursementDetail().id(disbursementDetail.getId()).principal(disbursementDetail.getPrincipal())
                        .expectedDisbursementDate(dateTimeFormatter.format(disbursementDetail.getExpectedDisbursementDate())));
            }
            disbursementDetails.add(new DisbursementDetail().expectedDisbursementDate("20 January 2025").principal(100.0));

            loanTransactionHelper.addAndDeleteDisbursementDetail(loanId, disbursementDetails);

            disburseLoan(loanId, BigDecimal.valueOf(100), "20 January 2025");
        });
    }
}
