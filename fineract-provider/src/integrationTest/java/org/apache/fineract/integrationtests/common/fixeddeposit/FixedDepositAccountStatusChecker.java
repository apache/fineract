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
package org.apache.fineract.integrationtests.common.fixeddeposit;

import static org.junit.jupiter.api.Assertions.assertTrue;

import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.util.HashMap;
import org.apache.fineract.integrationtests.common.Utils;
import org.junit.jupiter.api.Assertions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("rawtypes")
public final class FixedDepositAccountStatusChecker {

    private FixedDepositAccountStatusChecker() {

    }

    private static final Logger LOG = LoggerFactory.getLogger(FixedDepositAccountStatusChecker.class);
    private static final String FIXED_DEPOSIT_ACCOUNT_URL = "/fineract-provider/api/v1/fixeddepositaccounts";

    public static void verifyFixedDepositIsApproved(final HashMap fixedDepositStatusHashMap) {
        LOG.info("-------------------- VERIFYING FIXED DEPOSIT APPLICATION IS APPROVED --------------------");
        assertTrue(getStatus(fixedDepositStatusHashMap, "approved"), "Error in Approving Fixed deposit application");
        LOG.info("{}", fixedDepositStatusHashMap.toString());
    }

    public static void verifyFixedDepositIsPending(final HashMap fixedDepositStatusHashMap) {
        LOG.info("-------------------- VERIFYING FIXED DEPOSIT APPLICATION IS PENDING --------------------");
        assertTrue(getStatus(fixedDepositStatusHashMap, "submittedAndPendingApproval"), "FIXED DEPOSIT ACCOUNT IS NOT IN PENDING STATE");
        LOG.info("{}", fixedDepositStatusHashMap.toString());
    }

    public static void verifyFixedDepositIsActive(final HashMap fixedDepositStatusHashMap) {
        LOG.info("----------------- VERIFYING FIXED DEPOSIT APPLICATION IS ACTIVE -----------------");
        assertTrue(getStatus(fixedDepositStatusHashMap, "active"), "ERROR IN ACTIVATING THE FIXED DEPOSIT APPLICATION");
        LOG.info("{}", fixedDepositStatusHashMap.toString());
    }

    public static void verifyFixedDepositIsRejected(final HashMap fixedDepositStatusHashMap) {
        LOG.info("-------------- VERIFYING FIXED DEPOSIT APPLICATION IS REJECTED ----------------");
        assertTrue(getStatus(fixedDepositStatusHashMap, "rejected"), "ERROR IN REJECTING THE FIXED DEPOSIT APPLICATION");
        LOG.info("{}", fixedDepositStatusHashMap.toString());
    }

    public static void verifyFixedDepositIsWithdrawn(final HashMap fixedDepositStatusHashMap) {
        LOG.info("---------------- VERIFYING FIXED DEPOSIT APPLICATION IS WITHDRAWN ----------------");
        assertTrue(getStatus(fixedDepositStatusHashMap, "withdrawnByApplicant"), "ERROR IN WITHDRAW  THE FIXED DEPOSIT APPLICATION");
        LOG.info("{}", fixedDepositStatusHashMap.toString());
    }

    public static void verifyFixedDepositAccountIsClosed(final HashMap fixedDepositStatusHashMap) {
        LOG.info("--------------------- VERIFYING FIXED DEPOSIT APPLICATION IS CLOSED ---------------------");
        assertTrue(getStatus(fixedDepositStatusHashMap, "closed"), "ERROR IN CLOSING THE FIXED DEPOSIT APPLICATION");
        LOG.info("{}", fixedDepositStatusHashMap.toString());
    }

    public static void verifyFixedDepositAccountIsNotActive(final HashMap fixedDepositStatusHashMap) {
        LOG.info("------------------ VERIFYING FIXED DEPOSIT APPLICATION IS INACTIVE --------------------");
        Assertions.assertFalse(getStatus(fixedDepositStatusHashMap, "active"));
        LOG.info("{}", fixedDepositStatusHashMap.toString());
    }

    public static HashMap getStatusOfFixedDepositAccount(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            final String fixedDepositAccountID) {
        final String GET_STATUS_OF_FIXED_DEPOSIT_ACCOUNT_URL = FIXED_DEPOSIT_ACCOUNT_URL + "/" + fixedDepositAccountID + "?"
                + Utils.TENANT_IDENTIFIER;
        return Utils.performServerGet(requestSpec, responseSpec, GET_STATUS_OF_FIXED_DEPOSIT_ACCOUNT_URL, "status");
    }

    public static void verifyFixedDepositAccountIsPrematureClosed(HashMap fixedDepositStatusHashMap) {
        LOG.info("--------------------- VERIFYING FIXED DEPOSIT APPLICATION IS CLOSED ---------------------");
        assertTrue(getStatus(fixedDepositStatusHashMap, "prematureClosed"), "ERROR IN PREMATURELY CLOSING THE FIXED DEPOSIT ACCOUNT");
        LOG.info("{}", fixedDepositStatusHashMap.toString());
    }

    public static void verifyFixedDepositAccountIsMatured(HashMap fixedDepositStatusHashMap) {
        LOG.info("--------------------- VERIFYING FIXED DEPOSIT APPLICATION IS MATURED ---------------------");
        assertTrue(getStatus(fixedDepositStatusHashMap, "matured"), "ERROR IN MATURITY JOB OF THE FIXED DEPOSIT ACCOUNT");
        LOG.info("{}", fixedDepositStatusHashMap.toString());
    }

    private static boolean getStatus(final HashMap fixedDepositStatusMap, final String fixedDepositStatusString) {
        return (Boolean) fixedDepositStatusMap.get(fixedDepositStatusString);
    }
}
