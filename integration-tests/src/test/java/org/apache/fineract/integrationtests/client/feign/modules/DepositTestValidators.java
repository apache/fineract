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
package org.apache.fineract.integrationtests.client.feign.modules;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.fineract.client.models.GetFixedDepositAccountsStatus;
import org.apache.fineract.client.models.GetRecurringDepositAccountsStatus;

/**
 * Status assertions for deposit accounts, replacing the RestAssured {@code FixedDepositAccountStatusChecker} and
 * {@code RecurringDepositAccountStatusChecker} with the typed status the account read already returns.
 */
public final class DepositTestValidators {

    private DepositTestValidators() {}

    public static void verifyFixedDepositIsPending(GetFixedDepositAccountsStatus status) {
        assertTrue(status.getSubmittedAndPendingApproval(), "FIXED DEPOSIT ACCOUNT IS NOT IN PENDING STATE");
    }

    public static void verifyFixedDepositIsApproved(GetFixedDepositAccountsStatus status) {
        assertTrue(status.getApproved(), "Error in Approving Fixed deposit application");
    }

    public static void verifyFixedDepositIsActive(GetFixedDepositAccountsStatus status) {
        assertTrue(status.getActive(), "ERROR IN ACTIVATING THE FIXED DEPOSIT APPLICATION");
    }

    public static void verifyFixedDepositIsRejected(GetFixedDepositAccountsStatus status) {
        assertTrue(status.getRejected(), "ERROR IN REJECTING THE FIXED DEPOSIT APPLICATION");
    }

    public static void verifyFixedDepositIsWithdrawn(GetFixedDepositAccountsStatus status) {
        assertTrue(status.getWithdrawnByApplicant(), "ERROR IN WITHDRAW  THE FIXED DEPOSIT APPLICATION");
    }

    public static void verifyFixedDepositAccountIsClosed(GetFixedDepositAccountsStatus status) {
        assertTrue(status.getClosed(), "ERROR IN CLOSING THE FIXED DEPOSIT APPLICATION");
    }

    public static void verifyFixedDepositAccountIsNotActive(GetFixedDepositAccountsStatus status) {
        assertFalse(status.getActive());
    }

    public static void verifyFixedDepositAccountIsPrematureClosed(GetFixedDepositAccountsStatus status) {
        assertTrue(status.getPrematureClosed(), "ERROR IN PREMATURELY CLOSING THE FIXED DEPOSIT ACCOUNT");
    }

    public static void verifyFixedDepositAccountIsMatured(GetFixedDepositAccountsStatus status) {
        assertTrue(status.getMatured(), "ERROR IN MATURITY JOB OF THE FIXED DEPOSIT ACCOUNT");
    }

    public static void verifyRecurringDepositIsPending(GetRecurringDepositAccountsStatus status) {
        assertTrue(status.getSubmittedAndPendingApproval(), "RECURRING DEPOSIT ACCOUNT IS NOT IN PENDING STATE");
    }

    public static void verifyRecurringDepositIsApproved(GetRecurringDepositAccountsStatus status) {
        assertTrue(status.getApproved(), "ERROR IN APPROVING THE RECURRING DEPOSIT APPLICATION");
    }

    public static void verifyRecurringDepositIsActive(GetRecurringDepositAccountsStatus status) {
        assertTrue(status.getActive(), "ERROR IN ACTIVATING THE RECURRING DEPOSIT APPLICATION");
    }

    public static void verifyRecurringDepositIsRejected(GetRecurringDepositAccountsStatus status) {
        assertTrue(status.getRejected(), "ERROR IN REJECTING THE RECURRING DEPOSIT APPLICATION");
    }

    public static void verifyRecurringDepositIsWithdrawn(GetRecurringDepositAccountsStatus status) {
        assertTrue(status.getWithdrawnByApplicant(), "ERROR IN WITHDRAW  THE RECURRING DEPOSIT APPLICATION");
    }

    public static void verifyRecurringDepositAccountIsClosed(GetRecurringDepositAccountsStatus status) {
        assertTrue(status.getClosed(), "ERROR IN CLOSING THE RECURRING DEPOSIT APPLICATION");
    }

    public static void verifyRecurringDepositAccountIsPrematureClosed(GetRecurringDepositAccountsStatus status) {
        assertTrue(status.getPrematureClosed(), "ERROR IN PREMATURELY CLOSING THE RECURRING DEPOSIT ACCOUNT");
    }

    public static void verifyRecurringDepositAccountIsMatured(GetRecurringDepositAccountsStatus status) {
        assertTrue(status.getMatured(), "ERROR IN MATURITY JOB OF THE RECURRING DEPOSIT ACCOUNT");
    }
}
