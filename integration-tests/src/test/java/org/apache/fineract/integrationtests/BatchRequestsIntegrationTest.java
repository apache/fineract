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

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import org.apache.fineract.client.models.BatchRequest;
import org.apache.fineract.client.models.BatchResponse;
import org.apache.fineract.client.models.Header;
import org.apache.fineract.client.models.PostLoanProductsRequest;
import org.apache.fineract.infrastructure.core.exception.AbstractIdempotentCommandException;
import org.apache.fineract.integrationtests.client.FeignIntegrationTest;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignBatchHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignClientHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignCollateralHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignGroupHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignLoanHelper;
import org.apache.fineract.integrationtests.client.feign.modules.BatchRequestBuilders;
import org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test class for testing the integration of Batch API with custom batch requests and various user defined workflow.
 * Like in the case of mifos community-app
 *
 * @author Rishabh Shukla
 */
public class BatchRequestsIntegrationTest extends FeignIntegrationTest {

    private static final Logger LOG = LoggerFactory.getLogger(BatchRequestsIntegrationTest.class);
    private static final SecureRandom secureRandom = new SecureRandom();

    private FeignClientHelper clientHelper;
    private FeignGroupHelper groupHelper;
    private FeignLoanHelper loanHelper;
    private FeignCollateralHelper collateralHelper;
    private FeignBatchHelper batchHelper;

    @BeforeAll
    public void setup() {
        this.clientHelper = new FeignClientHelper(fineractClient());
        this.groupHelper = new FeignGroupHelper(fineractClient());
        this.loanHelper = new FeignLoanHelper(fineractClient());
        this.collateralHelper = new FeignCollateralHelper(fineractClient());
        this.batchHelper = new FeignBatchHelper(fineractClient());
    }

    @Test
    /**
     * Tests that a loan is successfully applied to client members of a group. Firstly, it'll create a few new clients
     * and then will add those clients to the group. Then a few loans will be created and one of those loans will be
     * chosen at random and similarily a few of the created clients will be chosen on random. Now, the selected loan
     * will be applied to these clients through Batch - API ApplyLoanCommandStrategy.
     */
    public void shouldReturnOkStatusForLoansAppliedToSelectedClients() {

        // Generate a random count of number of clients to be created
        final Integer clientsCount = (int) Math.ceil(secureRandom.nextDouble() * 7) + 3;
        final Long[] clientIDs = new Long[clientsCount];

        // Create a new group and get its groupId
        final Long groupID = groupHelper.createActiveGroup().getGroupId();

        // Create new clients and add those to this group
        for (Integer i = 0; i < clientsCount; i++) {
            clientIDs[i] = clientHelper.createClient();
            groupHelper.associateClient(groupID, clientIDs[i]);
            LOG.info("client {} has been added to the group {}", clientIDs[i], groupID);
        }

        // Generate a random count of number of new loan products to be created
        final Integer loansCount = (int) Math.ceil(secureRandom.nextDouble() * 4) + 1;
        final Long[] loanProducts = new Long[loansCount];

        // Create new loan Products
        for (Integer i = 0; i < loansCount; i++) {
            final PostLoanProductsRequest loanProductRequest = new LoanProductTestBuilder() //
                    .withPrincipal(String.valueOf(10000.00 + Math.ceil(secureRandom.nextDouble() * 1000000.00))) //
                    .withNumberOfRepayments(String.valueOf(2 + (int) Math.ceil(secureRandom.nextDouble() * 36))) //
                    .withRepaymentAfterEvery(String.valueOf(1 + (int) Math.ceil(secureRandom.nextDouble() * 3))) //
                    .withRepaymentTypeAsMonth() //
                    .withinterestRatePerPeriod(String.valueOf(1 + (int) Math.ceil(secureRandom.nextDouble() * 4))) //
                    .withInterestRateFrequencyTypeAsMonths() //
                    .withAmortizationTypeAsEqualPrincipalPayment() //
                    .withInterestTypeAsDecliningBalance() //
                    .currencyDetails("0", "100").buildRequest();

            loanProducts[i] = loanHelper.createLoanProduct(loanProductRequest).getResourceId();
        }

        // Select anyone of the loan products at random
        final Long loanProductID = loanProducts[(int) Math.floor(secureRandom.nextDouble() * (loansCount - 1))];

        final List<BatchRequest> batchRequests = new ArrayList<>();

        // Select a few clients from created group at random
        Integer selClientsCount = (int) Math.ceil(secureRandom.nextDouble() * clientsCount) + 2;
        for (int i = 0; i < selClientsCount; i++) {

            final Long collateralId = collateralHelper.createCollateralProduct().getResourceId();
            Assertions.assertNotNull(collateralId);
            final Long clientCollateralId = collateralHelper
                    .createClientCollateral(clientIDs[(int) Math.floor(secureRandom.nextDouble() * (clientsCount - 1))], collateralId)
                    .getResourceId();
            Assertions.assertNotNull(clientCollateralId);

            BatchRequest br = BatchRequestBuilders.applyLoan((long) selClientsCount, null, loanProductID, clientCollateralId);
            br.setBody(br.getBody().replace("$.clientId",
                    String.valueOf(clientIDs[(int) Math.floor(secureRandom.nextDouble() * (clientsCount - 1))])));
            batchRequests.add(br);
        }

        // Send the request to Batch - API
        final List<BatchResponse> response = batchHelper.executeWithoutEnclosingTransaction(batchRequests);

        // Verify that each loan has been applied successfully
        for (BatchResponse res : response) {
            Assertions.assertEquals(200L, (long) res.getStatusCode(), "Verify Status Code 200");
        }
    }

    @Test
    public void shouldReturnOkStatusWithIdempotencySupport() {

        // Generate a random count of number of clients to be created
        final Integer clientsCount = (int) Math.ceil(secureRandom.nextDouble() * 7) + 3;
        final Long[] clientIDs = new Long[clientsCount];

        // Create a new group and get its groupId
        final Long groupID = groupHelper.createActiveGroup().getGroupId();

        // Create new clients and add those to this group
        for (Integer i = 0; i < clientsCount; i++) {
            clientIDs[i] = clientHelper.createClient();
            groupHelper.associateClient(groupID, clientIDs[i]);
            LOG.info("client {} has been added to the group {}", clientIDs[i], groupID);
        }

        // Generate a random count of number of new loan products to be created
        final Integer loansCount = (int) Math.ceil(secureRandom.nextDouble() * 4) + 1;
        final Long[] loanProducts = new Long[loansCount];

        // Create new loan Products
        for (Integer i = 0; i < loansCount; i++) {
            final PostLoanProductsRequest loanProductRequest = new LoanProductTestBuilder() //
                    .withPrincipal(String.valueOf(10000.00 + Math.ceil(secureRandom.nextDouble() * 1000000.00))) //
                    .withNumberOfRepayments(String.valueOf(2 + (int) Math.ceil(secureRandom.nextDouble() * 36))) //
                    .withRepaymentAfterEvery(String.valueOf(1 + (int) Math.ceil(secureRandom.nextDouble() * 3))) //
                    .withRepaymentTypeAsMonth() //
                    .withinterestRatePerPeriod(String.valueOf(1 + (int) Math.ceil(secureRandom.nextDouble() * 4))) //
                    .withInterestRateFrequencyTypeAsMonths() //
                    .withAmortizationTypeAsEqualPrincipalPayment() //
                    .withInterestTypeAsDecliningBalance() //
                    .currencyDetails("0", "100").buildRequest();

            loanProducts[i] = loanHelper.createLoanProduct(loanProductRequest).getResourceId();
        }

        // Select anyone of the loan products at random
        final Long loanProductID = loanProducts[(int) Math.floor(secureRandom.nextDouble() * (loansCount - 1))];

        final List<BatchRequest> batchRequests = new ArrayList<>();

        // Select a few clients from created group at random
        Integer selClientsCount = (int) Math.ceil(secureRandom.nextDouble() * clientsCount) + 2;
        for (int i = 0; i < selClientsCount; i++) {

            final Long collateralId = collateralHelper.createCollateralProduct().getResourceId();
            Assertions.assertNotNull(collateralId);
            final Long clientCollateralId = collateralHelper
                    .createClientCollateral(clientIDs[(int) Math.floor(secureRandom.nextDouble() * (clientsCount - 1))], collateralId)
                    .getResourceId();
            Assertions.assertNotNull(clientCollateralId);

            BatchRequest br = BatchRequestBuilders.applyLoan((long) selClientsCount, null, loanProductID, clientCollateralId);
            br.setBody(br.getBody().replace("$.clientId",
                    String.valueOf(clientIDs[(int) Math.floor(secureRandom.nextDouble() * (clientsCount - 1))])));
            br.setHeaders(new HashSet<>());
            br.getHeaders().add(new Header().name("Idempotency-Key").value(UUID.randomUUID().toString()));
            batchRequests.add(br);
        }

        // Send the request to Batch - API
        final List<BatchResponse> response = batchHelper.executeWithoutEnclosingTransaction(batchRequests);

        // Verify that each loan has been applied successfully
        for (BatchResponse res : response) {
            Assertions.assertFalse(
                    res.getHeaders().stream()
                            .anyMatch(header -> header.getName().equals(AbstractIdempotentCommandException.IDEMPOTENT_CACHE_HEADER)),
                    "First can not be cached!");
            Assertions.assertEquals(200L, (long) res.getStatusCode(), "Verify Status Code 200");
        }

        final List<BatchResponse> secondResponse = batchHelper.executeWithoutEnclosingTransaction(batchRequests);

        // Verify that each loan has been applied successfully
        for (BatchResponse res : secondResponse) {
            Assertions.assertEquals("true",
                    res.getHeaders().stream()
                            .filter(header -> header.getName().equals(AbstractIdempotentCommandException.IDEMPOTENT_CACHE_HEADER))
                            .map(Header::getValue).findAny().get(),
                    "Not cached by idempotency key!");
            Assertions.assertEquals(200L, (long) res.getStatusCode(), "Verify Status Code 200");
        }
    }
}
