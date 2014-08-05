/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.integrationtests;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mifosplatform.batch.domain.BatchRequest;
import org.mifosplatform.batch.domain.BatchResponse;
import org.mifosplatform.integrationtests.common.BatchHelper;
import org.mifosplatform.integrationtests.common.ClientHelper;
import org.mifosplatform.integrationtests.common.GroupHelper;
import org.mifosplatform.integrationtests.common.Utils;
import org.mifosplatform.integrationtests.common.loans.LoanProductTestBuilder;
import org.mifosplatform.integrationtests.common.loans.LoanTransactionHelper;

import com.jayway.restassured.builder.RequestSpecBuilder;
import com.jayway.restassured.builder.ResponseSpecBuilder;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.specification.RequestSpecification;
import com.jayway.restassured.specification.ResponseSpecification;

/**
 * Test class for testing the integration of Batch API with custom batch
 * requests and various user defined workflow. Like in the case of mifos
 * community-app
 * 
 * @author Rishabh Shukla
 */
public class BatchRequestsIntegrationTest {

    private ResponseSpecification responseSpec;
    private RequestSpecification requestSpec;

    public BatchRequestsIntegrationTest() {
        super();
    }

    /**
     * Sets up the essential settings for the TEST like contentType,
     * expectedStatusCode. It uses the '@Before' annotation provided by jUnit.
     */
    @Before
    public void setup() {

        Utils.initializeRESTAssured();
        this.requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        this.requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        this.responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
    }

    @Test
    /**
     * Tests that a loan is successfully applied to client members of a group. 
     * Firstly, it'll create a few new clients and then will add those clients
     * to the group. Then a few loans will be created and one of those loans
     * will be chosen at random and similarily a few of the created clients will
     * be chosen on random. Now, the selected loan will be applied to these
     * clients through Batch - API ApplyLoanCommandStrategy.  
     */
    public void shouldReturnOkStatusForLoansAppliedToSelectedClients() {

        // Generate a random count of number of clients to be created
        final Integer clientsCount = (int) Math.ceil(Math.random() * 7) + 3;
        final Integer[] clientIDs = new Integer[clientsCount];

        // Create a new group and get its groupId
        Integer groupID = GroupHelper.createGroup(this.requestSpec, this.responseSpec, true);

        // Create new clients and add those to this group
        for (Integer i = 0; i < clientsCount; i++) {
            clientIDs[i] = ClientHelper.createClient(this.requestSpec, this.responseSpec);
            groupID = GroupHelper.associateClient(this.requestSpec, this.responseSpec, groupID.toString(), clientIDs[i].toString());
            System.out.println("client " + clientIDs[i] + " has been added to the group " + groupID);
        }

        // Generate a random count of number of new loan products to be created
        final Integer loansCount = (int) Math.ceil(Math.random() * 4) + 1;
        final Integer[] loanProducts = new Integer[loansCount];

        // Create new loan Products
        for (Integer i = 0; i < loansCount; i++) {
            final String loanProductJSON = new LoanProductTestBuilder() //
                    .withPrincipal(String.valueOf(10000.00 + Math.ceil(Math.random() * 1000000.00))) //
                    .withNumberOfRepayments(String.valueOf(2 + (int) Math.ceil(Math.random() * 36))) //
                    .withRepaymentAfterEvery(String.valueOf(1 + (int) Math.ceil(Math.random() * 3))) //
                    .withRepaymentTypeAsMonth() //
                    .withinterestRatePerPeriod(String.valueOf(1 + (int) Math.ceil(Math.random() * 4))) //
                    .withInterestRateFrequencyTypeAsMonths() //
                    .withAmortizationTypeAsEqualPrincipalPayment() //
                    .withInterestTypeAsDecliningBalance() //
                    .currencyDetails("0", "100").build(null);

            loanProducts[i] = new LoanTransactionHelper(this.requestSpec, this.responseSpec).getLoanProductId(loanProductJSON);
        }

        // Select anyone of the loan products at random
        final Integer loanProductID = loanProducts[(int) Math.floor(Math.random() * (loansCount - 1))];

        final List<BatchRequest> batchRequests = new ArrayList<>();

        // Select a few clients from created group at random
        Integer selClientsCount = (int) Math.ceil(Math.random() * clientsCount) + 2;
        for (int i = 0; i < selClientsCount; i++) {
            BatchRequest br = BatchHelper.applyLoanRequest((long) selClientsCount, null, loanProductID);
            br.setBody(br.getBody().replace("$.clientId", String.valueOf(clientIDs[(int) Math.floor(Math.random() * (clientsCount - 1))])));
            batchRequests.add(br);
        }

        // Send the request to Batch - API
        final String jsonifiedRequest = BatchHelper.toJsonString(batchRequests);

        final List<BatchResponse> response = BatchHelper.postBatchRequestsWithoutEnclosingTransaction(this.requestSpec, this.responseSpec,
                jsonifiedRequest);

        // Verify that each loan has been applied successfully
        for (BatchResponse res : response) {
            Assert.assertEquals("Verify Status Code 200", 200L, (long) res.getStatusCode());
        }
    }
}
