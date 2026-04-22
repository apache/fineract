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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.util.HashMap;
import java.util.UUID;
import org.apache.fineract.client.models.GetLoanProductsProductIdResponse;
import org.apache.fineract.client.models.PutLoanProductsProductIdRequest;
import org.apache.fineract.client.models.PutLoanProductsProductIdResponse;
import org.apache.fineract.client.util.CallFailedRuntimeException;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.loans.LoanProductHelper;
import org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class LoanProductExternalIdTest {

    private ResponseSpecification responseSpec;
    private RequestSpecification requestSpec;
    private LoanTransactionHelper loanTransactionHelper;
    private LoanProductHelper loanProductHelper;

    @BeforeEach
    public void setup() {
        Utils.initializeRESTAssured();
        this.requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        this.requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        this.responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
        this.loanTransactionHelper = new LoanTransactionHelper(requestSpec, responseSpec);
        this.loanProductHelper = new LoanProductHelper();
    }

    @Test
    public void testLoanProductWithExternalId() {
        String externalId = UUID.randomUUID().toString();
        HashMap<String, Object> request = new LoanProductTestBuilder().withExternalId(externalId).build(null, null);
        Integer loanProductId = loanTransactionHelper.getLoanProductId(Utils.convertToJson(request));
        assertNotNull(loanProductId);

        GetLoanProductsProductIdResponse getLoanProductsProductIdResponse = loanProductHelper.retrieveLoanProductByExternalId(externalId);
        assertNotNull(getLoanProductsProductIdResponse.getId());
        assertEquals(loanProductId, getLoanProductsProductIdResponse.getId().intValue());

        final PutLoanProductsProductIdRequest requestModifyLoan = new PutLoanProductsProductIdRequest()
                .shortName(Utils.uniqueRandomStringGenerator("", 3));
        PutLoanProductsProductIdResponse putLoanProductsProductIdResponse = loanProductHelper.updateLoanProductByExternalId(externalId,
                requestModifyLoan);
        assertNotNull(putLoanProductsProductIdResponse.getResourceId());
        assertEquals(loanProductId, putLoanProductsProductIdResponse.getResourceId().intValue());
    }

    @Test
    public void testLoanProductWithInvalidExternalId() {
        String externalId = UUID.randomUUID().toString();
        HashMap<String, Object> request = new LoanProductTestBuilder().withExternalId(externalId).build(null, null);
        Integer loanProductId = loanTransactionHelper.getLoanProductId(Utils.convertToJson(request));
        assertNotNull(loanProductId);

        GetLoanProductsProductIdResponse getLoanProductsProductIdResponse = loanProductHelper.retrieveLoanProductByExternalId(externalId);
        assertNotNull(getLoanProductsProductIdResponse.getId());
        assertEquals(loanProductId, getLoanProductsProductIdResponse.getId().intValue());

        CallFailedRuntimeException exception = assertThrows(CallFailedRuntimeException.class,
                () -> loanProductHelper.retrieveLoanProductByExternalId(externalId.substring(2)));
        assertEquals(404, exception.getResponse().code());
        assertTrue(exception.getMessage().contains("error.msg.loanproduct.id.invalid"));
    }

}
