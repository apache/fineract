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
import java.util.List;
import java.util.Map;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.models.DeleteFixedDepositAccountsAccountIdResponse;
import org.apache.fineract.client.models.GetFixedDepositAccountsAccountIdResponse;
import org.apache.fineract.client.models.GetFixedDepositAccountsAccountIdSummary;
import org.apache.fineract.client.models.GetFixedDepositAccountsAccountIdTransactionsResponse;
import org.apache.fineract.client.models.PostFixedDepositAccountsAccountIdRequest;
import org.apache.fineract.client.models.PostFixedDepositAccountsAccountIdResponse;
import org.apache.fineract.client.models.PostFixedDepositAccountsFixedDepositAccountIdTransactionsRequest;
import org.apache.fineract.client.models.PostFixedDepositAccountsRequest;
import org.apache.fineract.client.models.PostFixedDepositAccountsResponse;
import org.apache.fineract.client.models.PutFixedDepositAccountsAccountIdRequest;
import org.apache.fineract.client.models.PutFixedDepositAccountsAccountIdResponse;
import org.apache.fineract.integrationtests.client.feign.modules.DepositRequestBuilders;
import org.apache.fineract.integrationtests.client.feign.modules.SavingsTestData;

public class FeignFixedDepositHelper {

    private static final String APPROVE = "approve";
    private static final String UNDO_APPROVAL = "undoapproval";
    private static final String REJECT = "reject";
    private static final String WITHDRAWN_BY_APPLICANT = "withdrawnByApplicant";
    private static final String ACTIVATE = "activate";
    private static final String CALCULATE_INTEREST = "calculateInterest";
    private static final String POST_INTEREST = "postInterest";
    private static final String CALCULATE_PREMATURE_AMOUNT = "calculatePrematureAmount";
    private static final String PREMATURE_CLOSE = "prematureClose";
    private static final String CLOSE = "close";
    private static final String MODIFY_TRANSACTION = "modify";
    private static final String UNDO_TRANSACTION = "undo";

    private final FineractFeignClient fineractClient;

    public FeignFixedDepositHelper(FineractFeignClient fineractClient) {
        this.fineractClient = fineractClient;
    }

    public PostFixedDepositAccountsResponse submitApplication(PostFixedDepositAccountsRequest request) {
        return ok(() -> fineractClient.fixedDepositAccount().createFixedDepositAccount(request));
    }

    public PutFixedDepositAccountsAccountIdResponse updateApplication(Long accountId, PutFixedDepositAccountsAccountIdRequest request) {
        return ok(() -> fineractClient.fixedDepositAccount().updateFixedDepositAccount(accountId, request));
    }

    public DeleteFixedDepositAccountsAccountIdResponse deleteApplication(Long accountId) {
        return ok(() -> fineractClient.fixedDepositAccount().deleteFixedDepositAccount(accountId));
    }

    public PostFixedDepositAccountsAccountIdResponse approve(Long accountId, String approvedOnDate) {
        return command(accountId, DepositRequestBuilders.approveFixedDeposit(approvedOnDate), APPROVE);
    }

    /** The undo-approval command accepts a note only; sending locale or dateFormat is rejected as unsupported. */
    public PostFixedDepositAccountsAccountIdResponse undoApproval(Long accountId) {
        return command(accountId, new PostFixedDepositAccountsAccountIdRequest().note("UNDO APPROVAL"), UNDO_APPROVAL);
    }

    public PostFixedDepositAccountsAccountIdResponse reject(Long accountId, String rejectedOnDate) {
        return command(accountId, DepositRequestBuilders.rejectFixedDeposit(rejectedOnDate), REJECT);
    }

    public PostFixedDepositAccountsAccountIdResponse withdrawApplication(Long accountId, String withdrawnOnDate) {
        return command(accountId, DepositRequestBuilders.withdrawFixedDeposit(withdrawnOnDate), WITHDRAWN_BY_APPLICANT);
    }

    public PostFixedDepositAccountsAccountIdResponse activate(Long accountId, String activatedOnDate) {
        return command(accountId, DepositRequestBuilders.activateFixedDeposit(activatedOnDate), ACTIVATE);
    }

    public PostFixedDepositAccountsAccountIdResponse calculateInterest(Long accountId) {
        return command(accountId, DepositRequestBuilders.fixedDepositCommand(), CALCULATE_INTEREST);
    }

    public PostFixedDepositAccountsAccountIdResponse postInterest(Long accountId) {
        return command(accountId, DepositRequestBuilders.fixedDepositCommand(), POST_INTEREST);
    }

    public PostFixedDepositAccountsAccountIdResponse calculatePrematureAmount(Long accountId, String closedOnDate) {
        return command(accountId, DepositRequestBuilders.calculatePrematureAmount(closedOnDate), CALCULATE_PREMATURE_AMOUNT);
    }

    public PostFixedDepositAccountsAccountIdResponse prematureClose(Long accountId, String closedOnDate, int onAccountClosureId,
            Long toSavingsAccountId) {
        return command(accountId, DepositRequestBuilders.prematureCloseFixedDeposit(closedOnDate, onAccountClosureId, toSavingsAccountId),
                PREMATURE_CLOSE);
    }

    /** Closes a matured account; {@code onAccountClosureId} selects withdraw, transfer to savings or re-invest. */
    public PostFixedDepositAccountsAccountIdResponse close(Long accountId, String closedOnDate, int onAccountClosureId,
            Long toSavingsAccountId) {
        return command(accountId, DepositRequestBuilders.prematureCloseFixedDeposit(closedOnDate, onAccountClosureId, toSavingsAccountId),
                CLOSE);
    }

    public PostFixedDepositAccountsAccountIdResponse command(Long accountId, PostFixedDepositAccountsAccountIdRequest request,
            String command) {
        return ok(() -> fineractClient.fixedDepositAccount().handleCommandsFixedDepositAccount(accountId, request, command));
    }

    public GetFixedDepositAccountsAccountIdResponse getAccount(Long accountId) {
        return ok(() -> fineractClient.fixedDepositAccount().retrieveOneFixedDepositAccount(accountId, Map.of()));
    }

    public GetFixedDepositAccountsAccountIdSummary getSummary(Long accountId) {
        return getAccount(accountId).getSummary();
    }

    public List<GetFixedDepositAccountsAccountIdTransactionsResponse> getTransactions(Long accountId) {
        return ok(() -> fineractClient.fixedDepositAccountTransactions().retrieveAllFixedDepositAccountTransactions(accountId));
    }

    public Long undoTransaction(Long accountId, Long transactionId) {
        PostFixedDepositAccountsFixedDepositAccountIdTransactionsRequest request = new PostFixedDepositAccountsFixedDepositAccountIdTransactionsRequest();
        return ok(() -> fineractClient.fixedDepositAccountTransactions().handleCommandsFixedDepositAccountTransaction(accountId,
                transactionId, request, UNDO_TRANSACTION)).getResourceId();
    }

    public Long adjustTransaction(Long accountId, Long transactionId, String transactionDate, BigDecimal transactionAmount) {
        PostFixedDepositAccountsFixedDepositAccountIdTransactionsRequest request = new PostFixedDepositAccountsFixedDepositAccountIdTransactionsRequest()//
                .locale(SavingsTestData.LOCALE)//
                .dateFormat(SavingsTestData.DATETIME_PATTERN)//
                .transactionDate(transactionDate)//
                .transactionAmount(transactionAmount.doubleValue());
        return ok(() -> fineractClient.fixedDepositAccountTransactions().handleCommandsFixedDepositAccountTransaction(accountId,
                transactionId, request, MODIFY_TRANSACTION)).getResourceId();
    }
}
