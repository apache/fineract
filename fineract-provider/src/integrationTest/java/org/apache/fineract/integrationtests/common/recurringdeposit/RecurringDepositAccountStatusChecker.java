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
package org.apache.fineract.integrationtests.common.recurringdeposit;

import static org.junit.Assert.assertTrue;

import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.util.HashMap;
import org.apache.fineract.integrationtests.common.Utils;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("rawtypes")
public class RecurringDepositAccountStatusChecker {
  private static final Logger LOG =
      LoggerFactory.getLogger(RecurringDepositAccountStatusChecker.class);
  private static final String RECURRING_DEPOSIT_ACCOUNT_URL =
      "/fineract-provider/api/v1/recurringdepositaccounts";

  public static void verifyRecurringDepositIsApproved(final HashMap recurringDepositStatusHashMap) {
    LOG.info(
        "-------------------- VERIFYING RECURRING DEPOSIT APPLICATION IS APPROVED"
            + " --------------------");
    assertTrue(
        "Error in Approving Recurring deposit application",
        getStatus(recurringDepositStatusHashMap, "approved"));
    LOG.info("{}", recurringDepositStatusHashMap.toString());
  }

  public static void verifyRecurringDepositIsPending(final HashMap recurringDepositStatusHashMap) {
    LOG.info(
        "-------------------- VERIFYING RECURRING DEPOSIT APPLICATION IS PENDING"
            + " --------------------");
    assertTrue(
        "RECURRING DEPOSIT ACCOUNT IS NOT IN PENDING STATE",
        getStatus(recurringDepositStatusHashMap, "submittedAndPendingApproval"));
    LOG.info("{}", recurringDepositStatusHashMap.toString());
  }

  public static void verifyRecurringDepositIsActive(final HashMap recurringDepositStatusHashMap) {
    LOG.info(
        "----------------- VERIFYING RECURRING DEPOSIT APPLICATION IS ACTIVE -----------------");
    assertTrue(
        "ERROR IN ACTIVATING THE RECURRING DEPOSIT APPLICATION",
        getStatus(recurringDepositStatusHashMap, "active"));
    LOG.info("{}", recurringDepositStatusHashMap.toString());
  }

  public static void verifyRecurringDepositIsRejected(final HashMap recurringDepositStatusHashMap) {
    LOG.info("-------------- VERIFYING RECURRING DEPOSIT APPLICATION IS REJECTED ----------------");
    assertTrue(
        "ERROR IN REJECTING THE RECURRING DEPOSIT APPLICATION",
        getStatus(recurringDepositStatusHashMap, "rejected"));
    LOG.info("{}", recurringDepositStatusHashMap.toString());
  }

  public static void verifyRecurringDepositIsWithdrawn(
      final HashMap recurringDepositStatusHashMap) {
    LOG.info(
        "---------------- VERIFYING RECURRING DEPOSIT APPLICATION IS WITHDRAWN ----------------");
    assertTrue(
        "ERROR IN WITHDRAW  THE RECURRING DEPOSIT APPLICATION",
        getStatus(recurringDepositStatusHashMap, "withdrawnByApplicant"));
    LOG.info("{}", recurringDepositStatusHashMap.toString());
  }

  public static void verifyRecurringDepositAccountIsClosed(
      final HashMap recurringDepositStatusHashMap) {
    LOG.info(
        "--------------------- VERIFYING RECURRING DEPOSIT APPLICATION IS CLOSED"
            + " ---------------------");
    assertTrue(
        "ERROR IN CLOSING THE RECURRING DEPOSIT APPLICATION",
        getStatus(recurringDepositStatusHashMap, "closed"));
    LOG.info("{}", recurringDepositStatusHashMap.toString());
  }

  public static void verifyRecurringDepositAccountIsNotActive(
      final HashMap recurringDepositStatusHashMap) {
    LOG.info(
        "------------------ VERIFYING RECURRING DEPOSIT APPLICATION IS INACTIVE"
            + " --------------------");
    Assert.assertFalse(getStatus(recurringDepositStatusHashMap, "active"));
    LOG.info("{}", recurringDepositStatusHashMap.toString());
  }

  public static HashMap getStatusOfRecurringDepositAccount(
      final RequestSpecification requestSpec,
      final ResponseSpecification responseSpec,
      final String recurringDepositAccountID) {
    final String GET_STATUS_OF_RECURRING_DEPOSIT_ACCOUNT_URL =
        RECURRING_DEPOSIT_ACCOUNT_URL
            + "/"
            + recurringDepositAccountID
            + "?"
            + Utils.TENANT_IDENTIFIER;
    return Utils.performServerGet(
        requestSpec, responseSpec, GET_STATUS_OF_RECURRING_DEPOSIT_ACCOUNT_URL, "status");
  }

  public static void verifyRecurringDepositAccountIsPrematureClosed(
      HashMap recurringDepositStatusHashMap) {
    LOG.info(
        "--------------------- VERIFYING RECURRING DEPOSIT APPLICATION IS CLOSED"
            + " ---------------------");
    assertTrue(
        "ERROR IN PREMATURELY CLOSING THE RECURRING DEPOSIT ACCOUNT",
        getStatus(recurringDepositStatusHashMap, "prematureClosed"));
    LOG.info("{}", recurringDepositStatusHashMap.toString());
  }

  public static void verifyRecurringDepositAccountIsMatured(HashMap recurringDepositStatusHashMap) {
    LOG.info(
        "--------------------- VERIFYING RECURRING DEPOSIT APPLICATION IS MATURED"
            + " ---------------------");
    assertTrue(
        "ERROR IN MATURITY JOB OF THE RECURRING DEPOSIT ACCOUNT",
        getStatus(recurringDepositStatusHashMap, "matured"));
    LOG.info("{}", recurringDepositStatusHashMap.toString());
  }

  private static boolean getStatus(
      final HashMap recurringDepositStatusMap, final String recurringDepositStatusString) {
    return (Boolean) recurringDepositStatusMap.get(recurringDepositStatusString);
  }
}
