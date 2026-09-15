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

import static org.apache.fineract.client.feign.util.FeignCalls.fail;
import static org.apache.fineract.client.feign.util.FeignCalls.ok;

import java.util.List;
import java.util.Map;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.models.GetGroupsGroupIdGsimAccountsResponse;
import org.apache.fineract.client.models.PostSavingsAccountTransactionsRequest;
import org.apache.fineract.client.models.PostSavingsAccountTransactionsResponse;
import org.apache.fineract.client.models.PostSavingsAccountsAccountIdRequest;
import org.apache.fineract.client.models.PostSavingsAccountsAccountIdResponse;
import org.apache.fineract.client.models.PostSavingsAccountsGsimClient;
import org.apache.fineract.client.models.PostSavingsAccountsGsimRequest;
import org.apache.fineract.client.models.PostSavingsAccountsGsimResponse;
import org.apache.fineract.client.models.PostSavingsAccountsGsimSavings;
import org.apache.fineract.client.models.PutSavingsAccountsGsimRequest;
import org.apache.fineract.integrationtests.client.feign.modules.SavingsRequestBuilders;

/**
 * The group savings (GSIM) endpoints. A GSIM application creates one parent account and a child account per client, and
 * the commands act on the whole set through the parent's gsimId rather than on an individual savings id.
 */
public class FeignGsimHelper {

    private final FineractFeignClient fineractClient;

    public FeignGsimHelper(FineractFeignClient fineractClient) {
        this.fineractClient = fineractClient;
    }

    public PostSavingsAccountsGsimResponse submitApplication(List<PostSavingsAccountsGsimClient> clientArray) {
        PostSavingsAccountsGsimRequest request = new PostSavingsAccountsGsimRequest().clientArray(clientArray);
        return ok(() -> fineractClient.savingsAccount().submitGSIMApplication(request));
    }

    public Long submitApplication(Long clientId, Long groupId, Long productId, String submittedOnDate) {
        return submitApplication(List.of(SavingsRequestBuilders.gsimClient(clientId, groupId, productId, submittedOnDate, true)))
                .getGsimId();
    }

    public PostSavingsAccountsAccountIdResponse approve(Long gsimId, String approvedOnDate) {
        return command(gsimId, SavingsRequestBuilders.approveSavings(approvedOnDate), "approve");
    }

    public PostSavingsAccountsAccountIdResponse undoApproval(Long gsimId) {
        return command(gsimId, new PostSavingsAccountsAccountIdRequest(), "undoapproval");
    }

    public PostSavingsAccountsAccountIdResponse activate(Long gsimId, String activatedOnDate) {
        return command(gsimId, SavingsRequestBuilders.activateSavings(activatedOnDate), "activate");
    }

    public PostSavingsAccountsAccountIdResponse reject(Long gsimId, String rejectedOnDate) {
        return command(gsimId, SavingsRequestBuilders.rejectSavings(rejectedOnDate), "reject");
    }

    public CallFailedRuntimeException rejectExpectingError(Long gsimId, String rejectedOnDate) {
        PostSavingsAccountsAccountIdRequest request = SavingsRequestBuilders.rejectSavings(rejectedOnDate);
        return fail(() -> fineractClient.savingsAccount().handleGSIMCommands(gsimId, request, "reject"));
    }

    public PostSavingsAccountsAccountIdResponse close(Long gsimId, String closedOnDate, boolean withdrawBalance) {
        return command(gsimId, SavingsRequestBuilders.closeSavings(closedOnDate, withdrawBalance), "close");
    }

    public CallFailedRuntimeException closeExpectingError(Long gsimId, String closedOnDate, boolean withdrawBalance) {
        PostSavingsAccountsAccountIdRequest request = SavingsRequestBuilders.closeSavings(closedOnDate, withdrawBalance);
        return fail(() -> fineractClient.savingsAccount().handleGSIMCommands(gsimId, request, "close"));
    }

    public PostSavingsAccountsGsimResponse updateApplication(Long gsimId, Long clientId, Long groupId, Long productId) {
        PutSavingsAccountsGsimRequest request = new PutSavingsAccountsGsimRequest().clientId(clientId).groupId(groupId)
                .productId(productId);
        return ok(() -> fineractClient.savingsAccount().updateGsim(gsimId, request));
    }

    /** A GSIM deposit is posted to the parent account and carries one entry per child account it splits into. */
    public PostSavingsAccountTransactionsResponse deposit(Long savingsId, List<PostSavingsAccountsGsimSavings> savingsArray) {
        PostSavingsAccountTransactionsRequest request = new PostSavingsAccountTransactionsRequest().savingsArray(savingsArray);
        return ok(() -> fineractClient.savingsAccountTransactions().createSavingsAccountTransaction(savingsId, request, "gsimDeposit"));
    }

    public List<GetGroupsGroupIdGsimAccountsResponse> retrieveGsimAccounts(Long groupId) {
        return ok(() -> fineractClient.groups().retrieveGsimAccounts(groupId, Map.of()));
    }

    /** How many child accounts the group's GSIM application created. */
    public int childAccountCount(Long groupId) {
        return retrieveGsimAccounts(groupId).get(0).getChildGSIMAccounts().size();
    }

    private PostSavingsAccountsAccountIdResponse command(Long gsimId, PostSavingsAccountsAccountIdRequest request, String command) {
        return ok(() -> fineractClient.savingsAccount().handleGSIMCommands(gsimId, request, command));
    }
}
