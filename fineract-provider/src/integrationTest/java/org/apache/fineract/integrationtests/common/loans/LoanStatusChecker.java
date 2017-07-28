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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import java.util.HashMap;

import org.apache.fineract.integrationtests.common.Utils;

import com.jayway.restassured.specification.RequestSpecification;
import com.jayway.restassured.specification.ResponseSpecification;

@SuppressWarnings("rawtypes")
public class LoanStatusChecker {

    public static void verifyLoanIsApproved(final HashMap loanStatusHashMap) {
        assertFalse(getStatus(loanStatusHashMap, "pendingApproval"));
    }

    public static void verifyLoanIsWaitingForDisbursal(final HashMap loanStatusHashMap) {
        assertTrue(getStatus(loanStatusHashMap, "waitingForDisbursal"));
    }

    public static void verifyLoanIsPending(final HashMap loanStatusHashMap) {
        assertTrue(getStatus(loanStatusHashMap, "pendingApproval"));
    }

    public static void verifyLoanIsActive(final HashMap loanStatusHashMap) {
        assertTrue(getStatus(loanStatusHashMap, "active"));
    }

    public static void verifyLoanAccountIsClosed(final HashMap loanStatusHashMap) {
        assertTrue(getStatus(loanStatusHashMap, "closed"));
    }

    public static void verifyLoanAccountIsNotActive(final HashMap loanStatusHashMap) {
        assertFalse(getStatus(loanStatusHashMap, "active"));
    }

    public static void verifyLoanAccountIsOverPaid(final HashMap loanStatusHashMap) {
        assertTrue(getStatus(loanStatusHashMap, "overpaid"));
    }

    public static void verifyLoanAccountForeclosed(final HashMap loanSubStatusHashMap) {
        assertEquals("Foreclosed", getSubStatus(loanSubStatusHashMap, "value"));
    }

    public static HashMap<String, Object> getStatusOfLoan(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            final Integer loanID) {
        final String url = "/fineract-provider/api/v1/loans/" + loanID + "?" + Utils.TENANT_IDENTIFIER;
        return Utils.performServerGet(requestSpec, responseSpec, url, "status");
    }

    public static HashMap<String, Object> getSubStatusOfLoan(final RequestSpecification requestSpec,
            final ResponseSpecification responseSpec, final Integer loanID) {
        final String url = "/fineract-provider/api/v1/loans/" + loanID + "?" + Utils.TENANT_IDENTIFIER;
        return Utils.performServerGet(requestSpec, responseSpec, url, "subStatus");
    }

    private static boolean getStatus(final HashMap loanStatusMap, final String nameOfLoanStatusString) {
        return (Boolean) loanStatusMap.get(nameOfLoanStatusString);
    }

    private static String getSubStatus(final HashMap loanStatusMap, final String nameOfLoanStatusString) {
        return (String) loanStatusMap.get(nameOfLoanStatusString);
    }
}
