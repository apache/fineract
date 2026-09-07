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
package org.apache.fineract.integrationtests.client.feign.helpers;

import static org.apache.fineract.client.feign.util.FeignCalls.ok;

import java.math.BigDecimal;
import java.util.Map;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.models.DeleteRecurringDepositAccountsResponse;
import org.apache.fineract.client.models.GetRecurringDepositAccountsAccountIdResponse;
import org.apache.fineract.client.models.GetRecurringDepositAccountsSummary;
import org.apache.fineract.client.models.PostRecurringDepositAccountsAccountIdRequest;
import org.apache.fineract.client.models.PostRecurringDepositAccountsAccountIdResponse;
import org.apache.fineract.client.models.PostRecurringDepositAccountsRecurringDepositAccountIdTransactionsRequest;
import org.apache.fineract.client.models.PostRecurringDepositAccountsRecurringDepositAccountIdTransactionsResponse;
import org.apache.fineract.client.models.PostRecurringDepositAccountsRequest;
import org.apache.fineract.client.models.PostRecurringDepositAccountsResponse;
import org.apache.fineract.client.models.PutRecurringDepositAccountsAccountIdRequest;
import org.apache.fineract.client.models.PutRecurringDepositAccountsAccountIdResponse;
import org.apache.fineract.integrationtests.client.feign.modules.DepositRequestBuilders;

public class FeignRecurringDepositHelper {

    private static final String APPROVE = "approve";
    private static final String UNDO_APPROVAL = "undoapproval";
    private static final String REJECT = "reject";
    private static final String WITHDRAWN_BY_APPLICANT = "withdrawnByApplicant";
    private static final String ACTIVATE = "activate";
    private static final String CALCULATE_INTEREST = "calculateInterest";
    private static final String POST_INTEREST = "postInterest";
    private static final String CALCULATE_PREMATURE_AMOUNT = "calculatePrematureAmount";
    private static final String PREMATURE_CLOSE = "prematureClose";
    private static final String DEPOSIT = "deposit";
    private static final String MODIFY_TRANSACTION = "modify";
    private static final String UNDO_TRANSACTION = "undo";

    private final FineractFeignClient fineractClient;

    public FeignRecurringDepositHelper(FineractFeignClient fineractClient) {
        this.fineractClient = fineractClient;
    }

    public PostRecurringDepositAccountsResponse submitApplication(PostRecurringDepositAccountsRequest request) {
        return ok(() -> fineractClient.recurringDepositAccount().submitApplicationRecurringDepositAccount(request));
    }

    public PutRecurringDepositAccountsAccountIdResponse updateApplication(Long accountId,
            PutRecurringDepositAccountsAccountIdRequest request) {
        return ok(() -> fineractClient.recurringDepositAccount().updateRecurringDepositAccount(accountId, request));
    }

    public DeleteRecurringDepositAccountsResponse deleteApplication(Long accountId) {
        return ok(() -> fineractClient.recurringDepositAccount().deleteRecurringDepositAccount(accountId));
    }

    public PostRecurringDepositAccountsAccountIdResponse approve(Long accountId, String approvedOnDate) {
        return command(accountId, DepositRequestBuilders.approveRecurringDeposit(approvedOnDate), APPROVE);
    }

    /** The undo-approval command accepts a note only; sending locale or dateFormat is rejected as unsupported. */
    public PostRecurringDepositAccountsAccountIdResponse undoApproval(Long accountId) {
        return command(accountId, new PostRecurringDepositAccountsAccountIdRequest().note("UNDO APPROVAL"), UNDO_APPROVAL);
    }

    public PostRecurringDepositAccountsAccountIdResponse reject(Long accountId, String rejectedOnDate) {
        return command(accountId, DepositRequestBuilders.rejectRecurringDeposit(rejectedOnDate), REJECT);
    }

    public PostRecurringDepositAccountsAccountIdResponse withdrawApplication(Long accountId, String withdrawnOnDate) {
        return command(accountId, DepositRequestBuilders.withdrawRecurringDeposit(withdrawnOnDate), WITHDRAWN_BY_APPLICANT);
    }

    public PostRecurringDepositAccountsAccountIdResponse activate(Long accountId, String activatedOnDate) {
        return command(accountId, DepositRequestBuilders.activateRecurringDeposit(activatedOnDate), ACTIVATE);
    }

    /**
     * Both interest commands take an empty body; the legacy helper sends {@code {}} and the validator allows nothing
     * else.
     */
    public PostRecurringDepositAccountsAccountIdResponse calculateInterest(Long accountId) {
        return command(accountId, new PostRecurringDepositAccountsAccountIdRequest(), CALCULATE_INTEREST);
    }

    public PostRecurringDepositAccountsAccountIdResponse postInterest(Long accountId) {
        return command(accountId, new PostRecurringDepositAccountsAccountIdRequest(), POST_INTEREST);
    }

    public PostRecurringDepositAccountsAccountIdResponse calculatePrematureAmount(Long accountId, String closedOnDate) {
        return command(accountId, DepositRequestBuilders.calculateRecurringPrematureAmount(closedOnDate), CALCULATE_PREMATURE_AMOUNT);
    }

    public PostRecurringDepositAccountsAccountIdResponse prematureClose(Long accountId, String closedOnDate, int onAccountClosureId,
            Long toSavingsAccountId) {
        return command(accountId,
                DepositRequestBuilders.prematureCloseRecurringDeposit(closedOnDate, onAccountClosureId, toSavingsAccountId),
                PREMATURE_CLOSE);
    }

    public PostRecurringDepositAccountsAccountIdResponse command(Long accountId, PostRecurringDepositAccountsAccountIdRequest request,
            String command) {
        return ok(() -> fineractClient.recurringDepositAccount().handleCommandsRecurringDepositAccount(accountId, request, command));
    }

    public GetRecurringDepositAccountsAccountIdResponse getAccount(Long accountId) {
        return ok(() -> fineractClient.recurringDepositAccount().retrieveOneRecurringDepositAccount(accountId, Map.of()));
    }

    public GetRecurringDepositAccountsSummary getSummary(Long accountId) {
        return getAccount(accountId).getSummary();
    }

    public PostRecurringDepositAccountsRecurringDepositAccountIdTransactionsResponse deposit(Long accountId, String transactionDate,
            BigDecimal transactionAmount) {
        PostRecurringDepositAccountsRecurringDepositAccountIdTransactionsRequest request = DepositRequestBuilders
                .depositTransaction(transactionDate, transactionAmount);
        return ok(() -> fineractClient.recurringDepositAccountTransactions().transactionRecurringDepositAccountTransaction(accountId,
                request, DEPOSIT));
    }

    /** The undo command carries the transaction date and amount, the same body the modify command takes. */
    public Long undoTransaction(Long accountId, Long transactionId, String transactionDate, BigDecimal transactionAmount) {
        PostRecurringDepositAccountsRecurringDepositAccountIdTransactionsRequest request = DepositRequestBuilders
                .depositTransaction(transactionDate, transactionAmount);
        return ok(() -> fineractClient.recurringDepositAccountTransactions().handleCommandsRecurringDepositAccountTransaction(accountId,
                transactionId, request, UNDO_TRANSACTION)).getResourceId();
    }

    public Long updateTransaction(Long accountId, Long transactionId, String transactionDate, BigDecimal transactionAmount) {
        PostRecurringDepositAccountsRecurringDepositAccountIdTransactionsRequest request = DepositRequestBuilders
                .depositTransaction(transactionDate, transactionAmount);
        return ok(() -> fineractClient.recurringDepositAccountTransactions().handleCommandsRecurringDepositAccountTransaction(accountId,
                transactionId, request, MODIFY_TRANSACTION)).getResourceId();
    }
}
