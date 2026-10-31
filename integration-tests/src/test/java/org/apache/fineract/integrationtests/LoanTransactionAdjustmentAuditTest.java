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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;
import org.apache.fineract.client.models.AuditData;
import org.apache.fineract.integrationtests.client.feign.FeignLoanTestBase;
import org.apache.fineract.integrationtests.client.feign.modules.LoanRequestBuilders;
import org.apache.fineract.integrationtests.common.AuditHelper;
import org.junit.jupiter.api.Test;

/**
 * Adjusting a loan transaction is submitted against the original transaction but returns the replacement transaction.
 * The audit API reports and filters on both: the unprefixed identifiers are the returned ones, as they always were, and
 * the submitted ones carry a "submitted" prefix.
 */
public class LoanTransactionAdjustmentAuditTest extends FeignLoanTestBase {

    @Test
    public void auditApiReportsReturnedAndSubmittedIdentifiersForAdjustedTransaction() {
        runAt("01 June 2024", () -> {
            final Long clientId = createClient();
            final Long loanProductId = createLoanProduct(create4IProgressive());
            final Long loanId = applyForLoan(
                    applyLP2ProgressiveLoanRequest(clientId, loanProductId, "01 June 2024", 1000.0, 10.0, 4, null));
            approveLoan(loanId, LoanRequestBuilders.approveLoan(1000.0, "01 June 2024"));
            disburseLoan(loanId, BigDecimal.valueOf(1000.0), "01 June 2024");
            final Long repaymentId = addRepaymentForLoan(loanId, 100.0, "01 June 2024");

            final Long replacementId = transactionHelper.adjustLoanTransaction(loanId, repaymentId, "01 June 2024", 50.0).getResourceId();
            assertNotEquals(repaymentId, replacementId);

            final List<AuditData> byReturnedId = AuditHelper.getAuditDetails(replacementId.intValue(), "ADJUST", "LOAN");
            assertEquals(1, byReturnedId.size());
            final AuditData audit = byReturnedId.get(0);
            assertEquals(replacementId, audit.getResourceId());
            assertEquals(loanId, audit.getLoanId());
            assertEquals(clientId, audit.getClientId());
            assertEquals(repaymentId, audit.getSubmittedResourceId());

            assertTrue(AuditHelper.getAuditDetails(repaymentId.intValue(), "ADJUST", "LOAN").isEmpty());

            final List<AuditData> bySubmittedId = AuditHelper.getAuditDetailsBySubmittedResourceId(repaymentId, "ADJUST", "LOAN");
            assertEquals(1, bySubmittedId.size());
            assertEquals(audit.getId(), bySubmittedId.get(0).getId());
            assertTrue(AuditHelper.getAuditDetailsBySubmittedResourceId(replacementId, "ADJUST", "LOAN").isEmpty());
        });
    }

    @Test
    public void auditApiReportsSubmittedIdentifiersForFailedAdjustment() {
        runAt("01 June 2024", () -> {
            final Long clientId = createClient();
            final Long loanProductId = createLoanProduct(create4IProgressive());
            final Long loanId = applyForLoan(
                    applyLP2ProgressiveLoanRequest(clientId, loanProductId, "01 June 2024", 1000.0, 10.0, 4, null));
            approveLoan(loanId, LoanRequestBuilders.approveLoan(1000.0, "01 June 2024"));
            disburseLoan(loanId, BigDecimal.valueOf(1000.0), "01 June 2024");
            final Long repaymentId = addRepaymentForLoan(loanId, 100.0, "01 June 2024");

            // a transaction date in the future is rejected
            transactionHelper.adjustLoanTransactionExpectingError(loanId, repaymentId, "02 June 2024", 50.0);

            final List<AuditData> bySubmittedId = AuditHelper.getAuditDetailsBySubmittedResourceId(repaymentId, "ADJUST", "LOAN");
            assertEquals(1, bySubmittedId.size());
            final AuditData audit = bySubmittedId.get(0);
            assertEquals(repaymentId, audit.getSubmittedResourceId());
            assertEquals(loanId, audit.getSubmittedLoanId());
        });
    }
}
