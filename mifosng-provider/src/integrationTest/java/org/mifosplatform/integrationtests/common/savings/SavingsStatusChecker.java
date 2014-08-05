/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.integrationtests.common.savings;

import static org.junit.Assert.assertTrue;

import java.util.HashMap;

import org.mifosplatform.integrationtests.common.Utils;

import com.jayway.restassured.specification.RequestSpecification;
import com.jayway.restassured.specification.ResponseSpecification;

@SuppressWarnings("rawtypes")
public class SavingsStatusChecker {
    
    private static final String SAVINGS_ACCOUNT_URL = "/mifosng-provider/api/v1/savingsaccounts";

    public static void verifySavingsIsApproved(final HashMap savingsStatusHashMap) {
        System.out.println("\n-------------------------------------- VERIFYING SAVINGS APPLICATION IS APPROVED ------------------------------------");
        assertTrue("ERROR IN APPROVING SAVINGS APPLICATION", getStatus(savingsStatusHashMap, "approved"));
        System.out.println("Savings Application Status:" + savingsStatusHashMap + "\n");
    }

    public static void verifySavingsIsPending(final HashMap savingsStatusHashMap) {
        System.out.println("\n-------------------------------------- VERIFYING SAVINGS APPLICATION IS PENDING ------------------------------------");
        assertTrue("SAVINGS ACCOUNT IS NOT IN PENDING STATE", getStatus(savingsStatusHashMap, "submittedAndPendingApproval"));
        System.out.println("Savings Application Status:" + savingsStatusHashMap + "\n");
    }

    public static void verifySavingsIsActive(final HashMap savingsStatusHashMap) {
        System.out.println("\n-------------------------------------- VERIFYING SAVINGS APPLICATION IS ACTIVE ------------------------------------");
        assertTrue("ERROR IN ACTIVATING THE SAVINGS APPLICATION", getStatus(savingsStatusHashMap, "active"));
        System.out.println("Savings Application Status:" + savingsStatusHashMap + "\n");
    }
    
    public static void verifySavingsIsRejected(final HashMap savingsStatusHashMap) {
        System.out.println("\n-------------------------------------- VERIFYING SAVINGS APPLICATION IS REJECTED ------------------------------------");
        assertTrue("ERROR IN REJECTING THE SAVINGS APPLICATION", getStatus(savingsStatusHashMap, "rejected"));
        System.out.println("Savings Application Status:" + savingsStatusHashMap + "\n");
    }
    
    public static void verifySavingsIsWithdrawn(final HashMap savingsStatusHashMap) {
        System.out.println("\n-------------------------------------- VERIFYING SAVINGS APPLICATION IS WITHDRAWN ------------------------------------");
        assertTrue("ERROR IN WITHDRAW  THE SAVINGS APPLICATION", getStatus(savingsStatusHashMap, "withdrawnByApplicant"));
        System.out.println("Savings Application Status:" + savingsStatusHashMap + "\n");
    }
    
    public static void verifySavingsAccountIsClosed(final HashMap savingsStatusHashMap) {
        System.out.println("\n-------------------------------------- VERIFYING SAVINGS APPLICATION IS CLOSED ------------------------------------");
        assertTrue("ERROR IN CLOSING THE SAVINGS APPLICATION", getStatus(savingsStatusHashMap, "closed"));
        System.out.println("Savings Application Status:" + savingsStatusHashMap + "\n");
    }

    public static void verifySavingsAccountIsNotActive(final HashMap savingsStatusHashMap) {
        System.out.println("\n-------------------------------------- VERIFYING SAVINGS APPLICATION IS INACTIVE ------------------------------------");
        assertTrue(getStatus(savingsStatusHashMap, "active"));
        System.out.println("Savings Application Status:" + savingsStatusHashMap + "\n");
    }

    public static HashMap getStatusOfSavings(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            final Integer savingsID) {
        final String url = SAVINGS_ACCOUNT_URL+"/" + savingsID + "?"+Utils.TENANT_IDENTIFIER;
        return Utils.performServerGet(requestSpec, responseSpec, url, "status");
    }

    private static boolean getStatus(final HashMap savingsStatusMap, final String nameOfSavingsStatusString) {
        return (Boolean) savingsStatusMap.get(nameOfSavingsStatusString);
    }
}