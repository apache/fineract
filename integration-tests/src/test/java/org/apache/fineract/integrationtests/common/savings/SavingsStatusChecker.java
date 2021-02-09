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
package org.apache.fineract.integrationtests.common.savings;

import static org.junit.jupiter.api.Assertions.assertTrue;

import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.util.HashMap;
import org.apache.fineract.integrationtests.common.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("rawtypes")
public final class SavingsStatusChecker {

    private SavingsStatusChecker() {

    }

    private static final Logger LOG = LoggerFactory.getLogger(SavingsStatusChecker.class);
    private static final String SAVINGS_ACCOUNT_URL = "/fineract-provider/api/v1/savingsaccounts";

    public static void verifySavingsIsApproved(final HashMap savingsStatusHashMap) {
        LOG.info("\n-------------------------------------- VERIFYING SAVINGS APPLICATION IS APPROVED ------------------------------------");
        assertTrue(getStatus(savingsStatusHashMap, "approved"), "ERROR IN APPROVING SAVINGS APPLICATION");
        LOG.info("Savings Application Status: {} \n", savingsStatusHashMap);
    }

    public static void verifySavingsIsPending(final HashMap savingsStatusHashMap) {
        LOG.info("\n-------------------------------------- VERIFYING SAVINGS APPLICATION IS PENDING ------------------------------------");
        assertTrue(getStatus(savingsStatusHashMap, "submittedAndPendingApproval"), "SAVINGS ACCOUNT IS NOT IN PENDING STATE");
        LOG.info("Savings Application Status: {} \n", savingsStatusHashMap);
    }

    public static void verifySavingsIsActive(final HashMap savingsStatusHashMap) {
        LOG.info("\n-------------------------------------- VERIFYING SAVINGS APPLICATION IS ACTIVE ------------------------------------");
        assertTrue(getStatus(savingsStatusHashMap, "active"), "ERROR IN ACTIVATING THE SAVINGS APPLICATION");
        LOG.info("Savings Application Status: {} \n", savingsStatusHashMap);
    }

    public static void verifySavingsIsRejected(final HashMap savingsStatusHashMap) {
        LOG.info("\n-------------------------------------- VERIFYING SAVINGS APPLICATION IS REJECTED ------------------------------------");
        assertTrue(getStatus(savingsStatusHashMap, "rejected"), "ERROR IN REJECTING THE SAVINGS APPLICATION");
        LOG.info("Savings Application Status: {} \n", savingsStatusHashMap);
    }

    public static void verifySavingsIsWithdrawn(final HashMap savingsStatusHashMap) {
        LOG.info(
                "\n-------------------------------------- VERIFYING SAVINGS APPLICATION IS WITHDRAWN ------------------------------------");
        assertTrue(getStatus(savingsStatusHashMap, "withdrawnByApplicant"), "ERROR IN WITHDRAW  THE SAVINGS APPLICATION");
        LOG.info("Savings Application Status: {}", savingsStatusHashMap);
    }

    public static void verifySavingsAccountIsClosed(final HashMap savingsStatusHashMap) {
        LOG.info("\n-------------------------------------- VERIFYING SAVINGS APPLICATION IS CLOSED ------------------------------------");
        assertTrue(getStatus(savingsStatusHashMap, "closed"), "ERROR IN CLOSING THE SAVINGS APPLICATION");
        LOG.info("Savings Application Status: {}", savingsStatusHashMap);
    }

    public static void verifySavingsAccountIsNotActive(final HashMap savingsStatusHashMap) {
        LOG.info("\n-------------------------------------- VERIFYING SAVINGS APPLICATION IS INACTIVE ------------------------------------");
        assertTrue(getStatus(savingsStatusHashMap, "active"));
        LOG.info("Savings Application Status: {} \n", savingsStatusHashMap);
    }

    public static HashMap getStatusOfSavings(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            final Integer savingsID) {
        final String url = SAVINGS_ACCOUNT_URL + "/" + savingsID + "?" + Utils.TENANT_IDENTIFIER;
        return Utils.performServerGet(requestSpec, responseSpec, url, "status");
    }

    public static HashMap getSubStatusOfSavings(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            final Integer savingsID) {
        final String url = SAVINGS_ACCOUNT_URL + "/" + savingsID + "?" + Utils.TENANT_IDENTIFIER;
        return Utils.performServerGet(requestSpec, responseSpec, url, "subStatus");
    }

    private static boolean getStatus(final HashMap savingsStatusMap, final String nameOfSavingsStatusString) {
        return (Boolean) savingsStatusMap.get(nameOfSavingsStatusString);
    }

    public static void verifySavingsSubStatusInactive(HashMap savingsStatusHashMap) {
        LOG.info("\n-------------------------------------- VERIFYING SAVINGS APPLICATION IS ACTIVE ------------------------------------");
        assertTrue(getStatus(savingsStatusHashMap, "inactive"), "UNEXPECTED SAVINGS ACCOUNT SUB STATUS");
        LOG.info("Savings Application Sub Status: {} ", savingsStatusHashMap.toString());
    }

    public static void verifySavingsSubStatusDormant(HashMap savingsStatusHashMap) {
        LOG.info("\n-------------------------------------- VERIFYING SAVINGS APPLICATION IS ACTIVE ------------------------------------");
        assertTrue(getStatus(savingsStatusHashMap, "dormant"), "UNEXPECTED SAVINGS ACCOUNT SUB STATUS");
        LOG.info("Savings Application Sub Status: {} ", savingsStatusHashMap.toString());
    }

    public static void verifySavingsSubStatusEscheat(HashMap savingsStatusHashMap) {
        LOG.info("\n-------------------------------------- VERIFYING SAVINGS APPLICATION IS ACTIVE ------------------------------------");
        assertTrue(getStatus(savingsStatusHashMap, "escheat"), "UNEXPECTED SAVINGS ACCOUNT SUB STATUS");
        LOG.info("Savings Application Sub Status: {} ", savingsStatusHashMap.toString());
    }

    public static void verifySavingsSubStatusNone(HashMap savingsStatusHashMap) {
        LOG.info("\n-------------------------------------- VERIFYING SAVINGS APPLICATION IS ACTIVE ------------------------------------");
        assertTrue(getStatus(savingsStatusHashMap, "none"), "UNEXPECTED SAVINGS ACCOUNT SUB STATUS");
        LOG.info("Savings Application Sub Status: {} ", savingsStatusHashMap.toString());
    }

    public static void verifySavingsSubStatusblock(HashMap savingsStatusHashMap) {
        LOG.info("\n-------------------------------------- VERIFYING SAVINGS ACCOUNT IS BLOCKED ------------------------------------");
        assertTrue(getStatus(savingsStatusHashMap, "block"), "block");
        LOG.info("Savings Application Sub Status: {} ", savingsStatusHashMap.toString());
    }

    public static void verifySavingsSubStatusIsNone(HashMap savingsStatusHashMap) {
        LOG.info(
                "\n------------------------- VERIFYING SAVINGS ACCOUNT IS NOT BLOCKED FOR ANY TYPE OF TRANSACTIONS ---------------------------");
        assertTrue(getStatus(savingsStatusHashMap, "none"), "none");
        LOG.info("Savings Application Sub Status: {} ", savingsStatusHashMap.toString());
    }

    public static void verifySavingsSubStatusIsDebitBlocked(HashMap savingsStatusHashMap) {
        LOG.info("\n--------------------- VERIFYING SAVINGS APPLICATION IS BLOCKED FOR DEBIT TRANSACTIONS ---------------------");
        assertTrue(getStatus(savingsStatusHashMap, "blockDebit"), "status is blockDebit");
        LOG.info("Savings Application Sub Status: {} ", savingsStatusHashMap.toString());
    }

    public static void verifySavingsSubStatusIsCreditBlocked(HashMap savingsStatusHashMap) {
        LOG.info("\n---------------------- VERIFYING SAVINGS APPLICATION IS BLOCKED FOR CREDIT TRANSACTIONS ---------------");
        assertTrue(getStatus(savingsStatusHashMap, "blockCredit"), "blockCredit ");
        LOG.info("Savings Application Sub Status: {} ", savingsStatusHashMap.toString());
    }

}
