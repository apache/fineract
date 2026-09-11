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
import org.apache.fineract.client.models.AccountTransferRequest;
import org.apache.fineract.client.models.CommandProcessingResult;
import org.apache.fineract.client.models.GetAccountTransfersPageItems;
import org.apache.fineract.client.models.PostAccountTransfersRefundByTransferResponse;
import org.apache.fineract.client.models.PostAccountTransfersResponse;
import org.apache.fineract.integrationtests.client.feign.modules.AccountTransferRequestBuilders;
import org.apache.fineract.portfolio.account.PortfolioAccountType;

public class FeignAccountTransferHelper {

    private final FineractFeignClient fineractClient;

    public FeignAccountTransferHelper(FineractFeignClient fineractClient) {
        this.fineractClient = fineractClient;
    }

    public PostAccountTransfersResponse createAccountTransfer(AccountTransferRequest request) {
        return ok(() -> fineractClient.accountTransfers().createAccountTransfer(request));
    }

    public CallFailedRuntimeException createAccountTransferExpectingError(AccountTransferRequest request) {
        return fail(() -> fineractClient.accountTransfers().createAccountTransfer(request));
    }

    /** The transfers of one account transfer detail, which is what a transfer's resourceId identifies. */
    public List<GetAccountTransfersPageItems> retrieveTransfersByAccountDetailId(Long accountDetailId) {
        return List
                .copyOf(ok(() -> fineractClient.accountTransfers().retrieveAllAccountTransfers(Map.of("accountDetailId", accountDetailId)))
                        .getPageItems());
    }

    /**
     * Refunds an overpaid loan by transferring the overpayment into a savings account of the same client.
     */
    public PostAccountTransfersRefundByTransferResponse refundLoanByTransfer(String transferDate, Long clientId, Long loanId,
            Long savingsId, String transferAmount) {
        AccountTransferRequest request = AccountTransferRequestBuilders.transfer(transferDate, clientId, loanId, PortfolioAccountType.LOAN,
                clientId, savingsId, PortfolioAccountType.SAVINGS, transferAmount);
        return refundLoanByTransfer(request);
    }

    public PostAccountTransfersRefundByTransferResponse refundLoanByTransfer(AccountTransferRequest request) {
        return ok(() -> fineractClient.accountTransfers().refundByTransfer(request));
    }

    /** Undoing a transfer reverses both of its legs, returning each account to the balance it had before. */
    public CommandProcessingResult undoTransfer(Long accountTransferId) {
        return ok(() -> fineractClient.accountTransfers().accountTransferOperation(accountTransferId, "undo"));
    }
}
