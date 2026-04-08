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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.util.Collections;
import java.util.HashMap;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.accounting.AccountHelper;
import org.apache.fineract.integrationtests.common.loans.LoanApplicationTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanTestLifecycleExtension;
import org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper;
import org.apache.fineract.integrationtests.common.organisation.StaffHelper;
import org.apache.fineract.integrationtests.useradministration.users.UserHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@Slf4j
@ExtendWith(LoanTestLifecycleExtension.class)
public class AuthenticationIntegrationTest {

    private static final String LOAN_DATE = "11 July 2022";
    private static final String APPROVE_COMMAND = "approve";
    private ResponseSpecification responseSpec;
    private RequestSpecification requestSpec;
    private LoanTransactionHelper loanTransactionHelper;
    private Integer loanID;

    @BeforeEach
    public void setup() {
        Utils.initializeRESTAssured();
        setupAuthenticatedRequestSpec();
        this.responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
        this.loanTransactionHelper = new LoanTransactionHelper(this.requestSpec, this.responseSpec);

        AccountHelper accountHelper = new AccountHelper(this.requestSpec, this.responseSpec);
        Integer staffId = StaffHelper.createStaff(this.requestSpec, this.responseSpec);
        String username = Utils.uniqueRandomStringGenerator("user", 8);
        UserHelper.createUser(this.requestSpec, this.responseSpec, 1, staffId, username, "A1b2c3d4e5f$", "resourceId");
        Integer clientID = ClientHelper.createClient(requestSpec, responseSpec);

        Integer loanProductID = setupLoanProduct(accountHelper);
        this.loanID = loanTransactionHelper.applyForLoanApplicationWithPaymentStrategyAndPastMonth(clientID, loanProductID,
                Collections.emptyList(), null, "10000", LoanApplicationTestBuilder.DEFAULT_STRATEGY, "10 July 2022", LOAN_DATE);
    }

    @Test
    public void shouldAllowAccessForAuthenticatedUser() {
        setupAuthenticatedRequestSpec();
        String loanApprovalCommand = createLoanApprovalCommand();
        String loanApprovalRequest = createLoanApprovalRequest();

        HashMap response = Utils.performServerPost(this.requestSpec, this.responseSpec, loanApprovalCommand, loanApprovalRequest,
                "changes");
        HashMap status = (HashMap) response.get("status");

        assertEquals(200, (Integer) status.get("id"));
    }

    @Test
    public void shouldReturnUnauthorizedForUnauthenticatedAccess() throws JsonProcessingException {
        setupUnauthenticatedRequestSpec();
        this.responseSpec = new ResponseSpecBuilder().expectStatusCode(401).build();

        String loanApprovalCommand = createLoanApprovalCommand();
        String loanApprovalRequest = createLoanApprovalRequest();

        String rawResponse = Utils.performServerPost(this.requestSpec, this.responseSpec, loanApprovalCommand, loanApprovalRequest, null);

        ObjectMapper objectMapper = new ObjectMapper();
        HashMap response = objectMapper.readValue(rawResponse, HashMap.class);

        assertEquals(401, (Integer) response.get("status"));
        assertEquals("Unauthorized", response.get("error"));
    }

    private Integer setupLoanProduct(AccountHelper accountHelper) {
        return this.loanTransactionHelper.createLoanProduct("0", "0", LoanProductTestBuilder.DEFAULT_STRATEGY, "2",
                accountHelper.createAssetAccount(), accountHelper.createIncomeAccount(), accountHelper.createExpenseAccount(),
                accountHelper.createLiabilityAccount());
    }

    private void setupAuthenticatedRequestSpec() {
        this.requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        this.requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
    }

    private void setupUnauthenticatedRequestSpec() {
        this.requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
    }

    private String createLoanApprovalRequest() {
        return this.loanTransactionHelper.getApproveLoanAsJSON(LOAN_DATE);
    }

    private String createLoanApprovalCommand() {
        return this.loanTransactionHelper.createLoanOperationURL(APPROVE_COMMAND, loanID);
    }
}
