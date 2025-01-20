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
package org.apache.fineract.integrationtests.inlinecob;

import com.google.gson.Gson;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.util.HashMap;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.integrationtests.client.IntegrationTest;
import org.apache.fineract.integrationtests.common.Utils;

@Slf4j
public class InlineLoanCOBHelper extends IntegrationTest {

    private final RequestSpecification requestSpec;
    private final ResponseSpecification responseSpec;

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    public InlineLoanCOBHelper(final RequestSpecification requestSpec, final ResponseSpecification responseSpec) {
        this.requestSpec = requestSpec;
        this.responseSpec = responseSpec;
    }

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    public String executeInlineCOB(List<Long> loanIds) {
        final String EXECUTE_INLINE_COB_API = "/fineract-provider/api/v1/jobs/LOAN_COB/inline";
        log.info("------------------EXECUTE INLINE COB----------------------");
        log.info("------------------Loan IDs: {}----------------------", loanIds);
        return Utils.performServerPost(requestSpec, responseSpec, EXECUTE_INLINE_COB_API, buildInlineCOBRequest(loanIds));
    }

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    public String executeInlineCOB(List<Long> loanIds, String responseAttribute) {
        final String EXECUTE_INLINE_COB_API = "/fineract-provider/api/v1/jobs/LOAN_COB/inline";
        log.info("------------------EXECUTE INLINE COB----------------------");
        log.info("------------------Loan IDs: {}----------------------", loanIds);
        return Utils.performServerPost(requestSpec, responseSpec, EXECUTE_INLINE_COB_API, buildInlineCOBRequest(loanIds),
                responseAttribute);
    }

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    private static String buildInlineCOBRequest(List<Long> loanIds) {
        final HashMap<String, List<Long>> map = new HashMap<>();
        map.put("loanIds", loanIds);
        log.info("map :  {}", map);
        return new Gson().toJson(map);
    }
}
