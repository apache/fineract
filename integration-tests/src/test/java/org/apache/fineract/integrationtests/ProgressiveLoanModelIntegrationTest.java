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
import java.util.concurrent.atomic.AtomicLong;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.client.models.GetLoansLoanIdResponse;
import org.apache.fineract.client.models.ProgressiveLoanInterestScheduleModel;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@Slf4j
public class ProgressiveLoanModelIntegrationTest extends BaseLoanIntegrationTest {

    private final Long clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();
    private final Long loanProductId = loanProductHelper.createLoanProduct(create4IProgressive().isInterestRecalculationEnabled(true))
            .getResourceId();

    @Test
    public void testModelReturnsSavedModelAfterDisbursement() {
        AtomicLong loanIdA = new AtomicLong();
        runAt("1 January 2024", () -> {
            Long loanId = applyAndApproveProgressiveLoan(clientId, loanProductId, "1 January 2024", 1000.0, 96.32, 6, null);
            loanIdA.set(loanId);
            log.info("Testing on loanId: {}", loanId);
            loanTransactionHelper.disburseLoan(loanId, "1 January 2024", 1000.0);
            // Model saved automatically, fetching it. It should return non null
            ProgressiveLoanInterestScheduleModel model = assertNotNullAndChanged(null, loanId);
            loanTransactionHelper.makeLoanRepayment(loanId, "Repayment", "1 January 2024", 12.78);
            model = assertNotNullAndChanged(model, loanId);
            assertNotNullAndSame(model, loanId);
        });
        runAt("28 February 2024", () -> {
            Long loanId = loanIdA.get();
            GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId);
            BigDecimal totalUnpaidPayableNotDueInterest = loanDetails.getSummary().getTotalUnpaidPayableNotDueInterest();
            BigDecimal totalUnpaidDueInterest = loanDetails.getSummary().getTotalUnpaidPayableDueInterest();

            Assertions.assertEquals(73.78, totalUnpaidPayableNotDueInterest.doubleValue(), 0.001);
            Assertions.assertEquals(79.24, totalUnpaidDueInterest.doubleValue(), 0.001);
        });
    }

    private ProgressiveLoanInterestScheduleModel assertNotNullAndChanged(ProgressiveLoanInterestScheduleModel prev, Long loanId) {
        return assertNotNullAndChanged(prev, ok(fineractClient().progressiveLoanApi.fetchModel(loanId)));
    }

    private ProgressiveLoanInterestScheduleModel assertNotNullAndSame(ProgressiveLoanInterestScheduleModel prev, Long loanId) {
        return assertNotNullAndSame(prev, ok(fineractClient().progressiveLoanApi.fetchModel(loanId)));
    }

    private ProgressiveLoanInterestScheduleModel assertNotNullAndSame(ProgressiveLoanInterestScheduleModel prev,
            ProgressiveLoanInterestScheduleModel actual) {
        if (actual == null) {
            Assertions.fail("Model is null");
        }
        Assertions.assertEquals(prev.toString(), actual.toString());
        return actual;
    }

    private ProgressiveLoanInterestScheduleModel assertNotNullAndChanged(ProgressiveLoanInterestScheduleModel prev,
            ProgressiveLoanInterestScheduleModel actual) {
        if (actual == null) {
            Assertions.fail("Model is null");
        }
        if (prev != null) {
            Assertions.assertNotEquals(prev.toString(), actual.toString());
        }
        return actual;
    }

}
