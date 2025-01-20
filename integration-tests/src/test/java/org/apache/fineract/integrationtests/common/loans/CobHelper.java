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

import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.integrationtests.common.Utils;

@Slf4j
public final class CobHelper {

    private CobHelper() {}

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    public static List<Map<String, Object>> getCobPartitions(final RequestSpecification requestSpec,
            final ResponseSpecification responseSpec, int partitionSize, final String jsonReturn) {
        final String url = "/fineract-provider/api/v1/internal/cob/partitions/" + partitionSize + "?" + Utils.TENANT_IDENTIFIER;
        log.info("---------------------------------GET COB PARTITIONS---------------------------------------------");
        return Utils.performServerGet(requestSpec, responseSpec, url, jsonReturn);
    }

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    public static void fastForwardLoansLastCOBDate(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            final Integer loanId, final String cobDate) {
        final String url = "/fineract-provider/api/v1/internal/cob/fast-forward-cob-date-of-loan/" + loanId + "?" + Utils.TENANT_IDENTIFIER;
        log.info("-------------------- -----------FAST FORWARD LAST COB DATE OF LOAN ----------------------------------------");
        Utils.performServerPost(requestSpec, responseSpec, url, "{\"lastClosedBusinessDate\":\"" + cobDate + "\"}");
    }

}
