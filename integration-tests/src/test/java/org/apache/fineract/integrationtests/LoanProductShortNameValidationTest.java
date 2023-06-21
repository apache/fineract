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

import static io.restassured.http.ContentType.JSON;
import static org.apache.fineract.integrationtests.common.Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey;
import static org.apache.fineract.integrationtests.common.Utils.uniqueRandomStringGenerator;
import static org.hamcrest.Matchers.equalTo;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanTestLifecycleExtension;
import org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(LoanTestLifecycleExtension.class)
public class LoanProductShortNameValidationTest {

    private RequestSpecification requestSpec;

    @BeforeEach
    public void setup() {
        requestSpec = new RequestSpecBuilder().setContentType(JSON)
                .addHeader("Authorization", "Basic " + loginIntoServerAndGetBase64EncodedAuthenticationKey()).build();
    }

    @Test
    public void createLoanProductsWithSameShortName() {
        String shortName = uniqueRandomStringGenerator("", 4);
        createLoanProduct(shortName, successResponseSpec());
        createLoanProduct(shortName, validationFailedResponseSpec());
    }

    private ResponseSpecification successResponseSpec() {
        return new ResponseSpecBuilder().expectStatusCode(200).build();
    }

    private ResponseSpecification validationFailedResponseSpec() {
        return new ResponseSpecBuilder().expectBody("userMessageGlobalisationCode", equalTo("error.msg.product.loan.duplicate.short.name"))
                .expectStatusCode(403).build();
    }

    private void createLoanProduct(String shortName, ResponseSpecification responseSpec) {
        LoanTransactionHelper loanTransactionHelper = new LoanTransactionHelper(requestSpec, responseSpec);

        final String loanProductJSON = new LoanProductTestBuilder().withPrincipal("10000").withRepaymentAfterEvery("1")
                .withShortName(shortName).withRepaymentTypeAsMonth().withInterestRateFrequencyTypeAsMonths().build(null);

        loanTransactionHelper.getLoanProductId(loanProductJSON);
    }
}
