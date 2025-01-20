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
package org.apache.fineract.integrationtests.common.organisation;

import com.google.gson.Gson;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.util.HashMap;
import org.apache.fineract.client.models.PostEntityDatatableChecksTemplateResponse;
import org.apache.fineract.client.util.JSON;
import org.apache.fineract.integrationtests.common.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EntityDatatableChecksHelper {

    private static final Logger LOG = LoggerFactory.getLogger(EntityDatatableChecksHelper.class);
    private final RequestSpecification requestSpec;
    private final ResponseSpecification responseSpec;

    private static final String DATATABLE_CHECK_URL = "/fineract-provider/api/v1/entityDatatableChecks";

    private static final Gson GSON = new JSON().getGson();

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    public EntityDatatableChecksHelper(final RequestSpecification requestSpec, final ResponseSpecification responseSpec) {
        this.requestSpec = requestSpec;
        this.responseSpec = responseSpec;
    }

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    public Integer createEntityDatatableCheck(final String apptableName, final String datatableName, final int status,
            final Integer productId) {
        return Utils.performServerPost(this.requestSpec, this.responseSpec, DATATABLE_CHECK_URL + "?" + Utils.TENANT_IDENTIFIER,
                getTestEdcAsJSON(apptableName, datatableName, status, productId), "resourceId");
    }

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    public PostEntityDatatableChecksTemplateResponse addEntityDatatableCheck(final String apptableName, final String datatableName,
            final int status, final Integer productId) {
        final String response = Utils.performServerPost(this.requestSpec, this.responseSpec,
                DATATABLE_CHECK_URL + "?" + Utils.TENANT_IDENTIFIER, getTestEdcAsJSON(apptableName, datatableName, status, productId),
                null);
        return GSON.fromJson(response, PostEntityDatatableChecksTemplateResponse.class);
    }

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    public Integer deleteEntityDatatableCheck(final Integer entityDatatableCheckId) {
        return Utils.performServerDelete(requestSpec, responseSpec,
                DATATABLE_CHECK_URL + "/" + entityDatatableCheckId + "?" + Utils.TENANT_IDENTIFIER, "resourceId");
    }

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    public String retrieveEntityDatatableCheck() {
        return Utils.performServerGet(requestSpec, responseSpec, DATATABLE_CHECK_URL + "?" + Utils.TENANT_IDENTIFIER, null);
    }

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    public static String getTestEdcAsJSON(final String apptableName, final String datatableName, final int status,
            final Integer productId) {
        final HashMap<String, Object> map = new HashMap<>();
        map.put("entity", apptableName);
        map.put("status", status);
        map.put("datatableName", datatableName);
        if (productId != null) {
            map.put("productId", productId);
        }
        String requestJsonString = new Gson().toJson(map);
        LOG.info("map : {}", requestJsonString);
        return requestJsonString;
    }

}
