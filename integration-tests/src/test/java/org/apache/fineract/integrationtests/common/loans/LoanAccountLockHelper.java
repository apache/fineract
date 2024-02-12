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
package org.apache.fineract.integrationtests.common.loans;

import com.google.gson.Gson;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import org.apache.fineract.client.util.JSON;
import org.apache.fineract.integrationtests.client.IntegrationTest;
import org.apache.fineract.integrationtests.common.Utils;

public class LoanAccountLockHelper extends IntegrationTest {

    private static final String INTERNAL_PLACE_LOCK_ON_LOAN_ACCOUNT_URL = "/fineract-provider/api/v1/internal/loans/";
    private static final Gson GSON = new JSON().getGson();
    private final RequestSpecification requestSpec;
    private final ResponseSpecification responseSpec;

    public LoanAccountLockHelper(final RequestSpecification requestSpec, final ResponseSpecification responseSpec) {
        this.requestSpec = requestSpec;
        this.responseSpec = responseSpec;
    }

    public String placeSoftLockOnLoanAccount(Integer loanId, String lockOwner) {
        return placeSoftLockOnLoanAccount(loanId, lockOwner, null);
    }

    public String placeSoftLockOnLoanAccount(Integer loanId, String lockOwner, String error) {
        return Utils.performServerPost(requestSpec, responseSpec,
                INTERNAL_PLACE_LOCK_ON_LOAN_ACCOUNT_URL + loanId + "/place-lock/" + lockOwner + "?" + Utils.TENANT_IDENTIFIER, error);
    }

}
