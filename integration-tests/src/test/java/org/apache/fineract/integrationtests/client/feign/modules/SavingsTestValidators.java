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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import org.apache.fineract.client.models.SavingsAccountStatusEnumData;
import org.apache.fineract.client.models.SavingsAccountTransactionData;

public final class SavingsTestValidators {

    private SavingsTestValidators() {}

    public static void verifySavingsIsPending(final SavingsAccountStatusEnumData status) {
        assertNotNull(status, "Savings status is missing");
        assertTrue(Boolean.TRUE.equals(status.getSubmittedAndPendingApproval()),
                "Savings account is not in pending state, was: " + status.getValue());
    }

    public static void verifySavingsIsApproved(final SavingsAccountStatusEnumData status) {
        assertNotNull(status, "Savings status is missing");
        assertTrue(Boolean.TRUE.equals(status.getApproved()), "Savings account is not approved, was: " + status.getValue());
    }

    public static void verifySavingsIsActive(final SavingsAccountStatusEnumData status) {
        assertNotNull(status, "Savings status is missing");
        assertTrue(Boolean.TRUE.equals(status.getActive()), "Savings account is not active, was: " + status.getValue());
    }

    public static void verifySavingsIsRejected(final SavingsAccountStatusEnumData status) {
        assertNotNull(status, "Savings status is missing");
        assertTrue(Boolean.TRUE.equals(status.getRejected()), "Savings account is not rejected, was: " + status.getValue());
    }

    public static void verifySavingsIsWithdrawn(final SavingsAccountStatusEnumData status) {
        assertNotNull(status, "Savings status is missing");
        assertTrue(Boolean.TRUE.equals(status.getWithdrawnByApplicant()),
                "Savings account is not withdrawn by applicant, was: " + status.getValue());
    }

    public static void verifySavingsIsClosed(final SavingsAccountStatusEnumData status) {
        assertNotNull(status, "Savings status is missing");
        assertTrue(Boolean.TRUE.equals(status.getClosed()), "Savings account is not closed, was: " + status.getValue());
    }

    public static void verifyIsInterestPosting(final SavingsAccountTransactionData transaction) {
        assertNotNull(transaction, "Interest posting transaction is missing");
        assertNotNull(transaction.getTransactionType(), "Transaction type is missing");
        assertTrue(Boolean.TRUE.equals(transaction.getTransactionType().getInterestPosting()),
                "Transaction is not an interest posting, was: " + transaction.getTransactionType().getValue());
    }

    /** Compares by value, ignoring the scale the server returns (0.274 and 0.2740 are equal). */
    public static void verifyAmount(final BigDecimal expected, final BigDecimal actual, final String message) {
        assertNotNull(actual, message + " - amount is missing");
        assertEquals(0, expected.compareTo(actual), message + " - expected " + expected + " but was " + actual);
    }
}
