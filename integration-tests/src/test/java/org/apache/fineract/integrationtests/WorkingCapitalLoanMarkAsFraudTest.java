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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.UUID;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.models.GetWorkingCapitalLoansLoanIdResponse;
import org.apache.fineract.client.models.MarkWorkingCapitalLoanAsFraudRequest;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.workingcapitalloan.WorkingCapitalLoanApplicationTestBuilder;
import org.apache.fineract.integrationtests.common.workingcapitalloan.WorkingCapitalLoanDisbursementTestBuilder;
import org.apache.fineract.integrationtests.common.workingcapitalloan.WorkingCapitalLoanHelper;
import org.apache.fineract.integrationtests.common.workingcapitalloanproduct.WorkingCapitalLoanProductHelper;
import org.apache.fineract.integrationtests.common.workingcapitalloanproduct.WorkingCapitalLoanProductTestBuilder;
import org.junit.jupiter.api.Test;

/**
 * Integration test for the Working Capital Loan mark-as-fraud command, exercised through the generated Feign client.
 * The command is served through the legacy command-source pipeline (audit trail and maker-checker), which enforces the
 * SETFRAUD_WORKINGCAPITALLOAN permission.
 */
public class WorkingCapitalLoanMarkAsFraudTest {

    private final WorkingCapitalLoanHelper applicationHelper = new WorkingCapitalLoanHelper();
    private final WorkingCapitalLoanProductHelper productHelper = new WorkingCapitalLoanProductHelper();

    @Test
    public void testMarkAndUnmarkWorkingCapitalLoanAsFraud() {
        final Long productId = createProduct();
        final Long clientId = createClient();
        final Long loanId = activeLoan(clientId, productId);

        // A freshly disbursed loan is not fraudulent.
        assertFalse(Boolean.TRUE.equals(retrieveLoan(loanId).getFraud()));

        // Mark the loan as fraudulent.
        applicationHelper.markAsFraudById(loanId, new MarkWorkingCapitalLoanAsFraudRequest().fraud(true));
        assertTrue(retrieveLoan(loanId).getFraud());

        // Marking again with the same value is idempotent.
        applicationHelper.markAsFraudById(loanId, new MarkWorkingCapitalLoanAsFraudRequest().fraud(true));
        assertTrue(retrieveLoan(loanId).getFraud());

        // Unmark the loan.
        applicationHelper.markAsFraudById(loanId, new MarkWorkingCapitalLoanAsFraudRequest().fraud(false));
        assertFalse(Boolean.TRUE.equals(retrieveLoan(loanId).getFraud()));
    }

    @Test
    public void testMarkAsFraudFailsWhenFraudFlagMissing() {
        final Long productId = createProduct();
        final Long clientId = createClient();
        final Long loanId = submitLoan(clientId, productId);

        // fraud is a required field; omitting it must be rejected by the request validation.
        final CallFailedRuntimeException ex = applicationHelper.markAsFraudByIdExpectingFailure(loanId,
                new MarkWorkingCapitalLoanAsFraudRequest());
        assertEquals(400, ex.getStatus());
    }

    @Test
    public void testMarkAsFraudFailsWhenLoanNotActive() {
        final Long productId = createProduct();
        final Long clientId = createClient();
        // The loan is only submitted (not approved/disbursed), so it is not active.
        final Long loanId = submitLoan(clientId, productId);

        // Marking a non-active loan as fraud must be rejected with a 400 carrying the specific validation message.
        final CallFailedRuntimeException ex = applicationHelper.markAsFraudByIdExpectingFailure(loanId,
                new MarkWorkingCapitalLoanAsFraudRequest().fraud(true));
        assertEquals(400, ex.getStatus());
        assertNotNull(ex.getDeveloperMessage());
        assertTrue(ex.getDeveloperMessage().contains("Marking a loan as fraud is allowed only for active loans"));
    }

    @Test
    public void testMarkAndUnmarkWorkingCapitalLoanAsFraudByExternalId() {
        final Long productId = createProduct();
        final Long clientId = createClient();
        final String externalId = UUID.randomUUID().toString();
        final Long loanId = activeLoan(clientId, productId, externalId);

        assertFalse(Boolean.TRUE.equals(retrieveLoanByExternalId(externalId).getFraud()));

        applicationHelper.markAsFraudByExternalId(externalId, new MarkWorkingCapitalLoanAsFraudRequest().fraud(true));
        assertTrue(retrieveLoanByExternalId(externalId).getFraud());
        // The by-id and by-external-id endpoints address the same loan.
        assertTrue(retrieveLoan(loanId).getFraud());

        applicationHelper.markAsFraudByExternalId(externalId, new MarkWorkingCapitalLoanAsFraudRequest().fraud(false));
        assertFalse(Boolean.TRUE.equals(retrieveLoanByExternalId(externalId).getFraud()));
    }

    @Test
    public void testMarkAsFraudFailsWhenExternalIdIsUnknown() {
        final CallFailedRuntimeException ex = applicationHelper.markAsFraudByExternalIdExpectingFailure(UUID.randomUUID().toString(),
                new MarkWorkingCapitalLoanAsFraudRequest().fraud(true));
        assertEquals(404, ex.getStatus());
    }

    private Long activeLoan(final Long clientId, final Long productId) {
        return activeLoan(clientId, productId, null);
    }

    private Long activeLoan(final Long clientId, final Long productId, final String externalId) {
        final Long loanId = submitLoan(clientId, productId, externalId);
        final LocalDate currentDate = LocalDate.now(ZoneId.systemDefault());
        applicationHelper.approveById(loanId,
                WorkingCapitalLoanApplicationTestBuilder.buildApproveRequest(currentDate, BigDecimal.valueOf(5000), null));
        applicationHelper.disburseById(loanId,
                WorkingCapitalLoanDisbursementTestBuilder.buildDisburseRequest(currentDate, BigDecimal.valueOf(5000)));
        return loanId;
    }

    private Long submitLoan(final Long clientId, final Long productId) {
        return submitLoan(clientId, productId, null);
    }

    private Long submitLoan(final Long clientId, final Long productId, final String externalId) {
        return applicationHelper.submit(new WorkingCapitalLoanApplicationTestBuilder() //
                .withClientId(clientId) //
                .withProductId(productId) //
                .withPrincipal(BigDecimal.valueOf(5000)) //
                .withPeriodPaymentRate(WorkingCapitalLoanProductTestBuilder.DEFAULT_PERIOD_PAYMENT_RATE_PERCENT) //
                .withTotalPaymentVolume(BigDecimal.valueOf(100000)) //
                .withExternalId(externalId) //
                .buildSubmitRequest());
    }

    private GetWorkingCapitalLoansLoanIdResponse retrieveLoan(final Long loanId) {
        final GetWorkingCapitalLoansLoanIdResponse response = applicationHelper.retrieveById(loanId);
        assertNotNull(response);
        return response;
    }

    private GetWorkingCapitalLoansLoanIdResponse retrieveLoanByExternalId(final String externalId) {
        final GetWorkingCapitalLoansLoanIdResponse response = applicationHelper.retrieveByExternalId(externalId);
        assertNotNull(response);
        return response;
    }

    private Long createProduct() {
        final String uniqueName = "WCL Product " + UUID.randomUUID().toString().substring(0, 8);
        final String uniqueShortName = Utils.uniqueRandomStringGenerator("", 4);
        return productHelper
                .createWorkingCapitalLoanProduct(
                        new WorkingCapitalLoanProductTestBuilder().withName(uniqueName).withShortName(uniqueShortName).build())
                .getResourceId();
    }

    private Long createClient() {
        return ClientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();
    }
}
