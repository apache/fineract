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
package org.apache.fineract.integrationtests.common;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.Gson;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import org.apache.fineract.client.util.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Manthan Surkar
 *
 */

public class AuditHelper {

    private static final Logger LOG = LoggerFactory.getLogger(AuditHelper.class);
    private static final String AUDIT_BASE_URL = "/fineract-provider/api/v1/audits?" + Utils.TENANT_IDENTIFIER;
    private static final String AUDITSEARCH_BASE_URL = "/fineract-provider/api/v1/audits/searchtemplate?" + Utils.TENANT_IDENTIFIER;

    private static final Gson GSON = new JSON().getGson();

    private ResponseSpecification responseSpec;
    private RequestSpecification requestSpec;

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    public AuditHelper(final RequestSpecification requestSpec, final ResponseSpecification responseSpec) {
        this.requestSpec = requestSpec;
        this.responseSpec = responseSpec;
    }

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    public List getAuditDetails(final Integer resourceId, final String actionName, final String entityName) {
        final String AUDIT_URL = AUDIT_BASE_URL + "&entityName=" + entityName + "&resourceId=" + resourceId + "&actionName=" + actionName
                + "&orderBy=id&sortBy=DSC";
        List<HashMap<String, Object>> responseAudits = Utils.performServerGet(requestSpec, responseSpec, AUDIT_URL, "");
        return responseAudits;
    }

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    public List getAuditDetails(final int limit) {
        final String AUDIT_URL = AUDIT_BASE_URL + "&paged=true&limit=" + Integer.toString(limit);
        LinkedHashMap responseAudits = Utils.performServerGet(requestSpec, responseSpec, AUDIT_URL, "");
        return (List) responseAudits.get("pageItems");
    }

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    public LinkedHashMap getAuditSearchTemplate() {
        return Utils.performServerGet(requestSpec, responseSpec, AUDITSEARCH_BASE_URL, "$");
    }

    /**
     * Some audit actions can only be done once Eg: Creation of a client with id 123, hence we verify number of audits
     * For such operations is "equal" to 1 always
     */
    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    public void verifyOneAuditOnly(List<HashMap<String, Object>> auditsToCheck, Integer id, String actionName, String entityType) {
        LOG.info("------------------------------CHECK IF AUDIT CREATED------------------------------------\n");
        assertEquals(1, auditsToCheck.size(), "More than one audit created");
        HashMap<String, Object> auditToCheck = auditsToCheck.get(0);
        String actual = auditToCheck.get("actionName").toString() + " is done on " + auditToCheck.get("entityName").toString() + " with id "
                + auditToCheck.get("resourceId").toString();
        String expected = actionName + " is done on " + entityType + " with id " + id;
        assertEquals(expected, actual, "Error in creating audit!");
    }

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    public void verifyMultipleAuditsOnserver(List<HashMap<String, Object>> auditsRecievedInitial,
            List<HashMap<String, Object>> auditsRecieved, Integer id, String actionName, String entityType) {
        LOG.info("------------------------------CHECK IF AUDIT CREATED------------------------------------\n");
        assertEquals(auditsRecievedInitial.size() + 1, auditsRecieved.size(), "Audit is not Created");

        Comparator<HashMap<String, Object>> compareById = (HashMap<String, Object> a, HashMap<String, Object> b) -> a.get("id").toString()
                .compareTo(b.get("id").toString());
        Collections.sort(auditsRecieved, compareById.reversed());

        // First element is new audit created(Sorted DESC by Id)
        HashMap<String, Object> auditToCheck = auditsRecieved.get(0);
        String actual = auditToCheck.get("actionName").toString() + " is done on " + auditToCheck.get("entityName").toString() + " with id "
                + auditToCheck.get("resourceId").toString();
        String expected = actionName + " is done on " + entityType + " with id " + id;
        assertEquals(expected, actual, "Error in creating audit!");
    }

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    public void verifyLimitParameterfor(final int limit) {
        assertEquals(limit, getAuditDetails(limit).size(), "Incorrect number of audits recieved for limit: " + Integer.toString(limit));
    }

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    public void verifyOrderBysupported(final String orderByValue) {
        final String AUDIT_URL = AUDIT_BASE_URL + "&paged=true&orderBy=" + orderByValue;
        Utils.performServerGet(requestSpec, responseSpec, AUDIT_URL, "");
    }

}
