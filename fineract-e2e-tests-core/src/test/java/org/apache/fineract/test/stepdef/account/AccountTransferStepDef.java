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
package org.apache.fineract.test.stepdef.account;

import static org.apache.fineract.client.feign.util.FeignCalls.fail;
import static org.apache.fineract.client.feign.util.FeignCalls.ok;
import static org.assertj.core.api.Assertions.assertThat;

import io.cucumber.java.en.When;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.models.AccountTransferRequest;
import org.apache.fineract.client.models.PostAccountTransfersResponse;
import org.apache.fineract.client.models.PostClientsResponse;
import org.apache.fineract.client.models.PostLoansResponse;
import org.apache.fineract.client.models.PostSavingsAccountsResponse;
import org.apache.fineract.test.helper.ErrorMessageHelper;
import org.apache.fineract.test.stepdef.AbstractStepDef;
import org.apache.fineract.test.support.TestContextKey;

@RequiredArgsConstructor
public class AccountTransferStepDef extends AbstractStepDef {

    private static final String DATE_FORMAT = "dd MMMM yyyy";
    private static final String DEFAULT_LOCALE = "en";
    private static final String SAVINGS_ACCOUNT_TYPE = "2";
    private static final String LOAN_ACCOUNT_TYPE = "1";

    private final FineractFeignClient fineractClient;

    @When("Initiate account transfer from savings to loan on {string} for {double}")
    public void initiateAccountTransfer(String date, double amount) {
        PostClientsResponse clientResponse = testContext().get(TestContextKey.CLIENT_CREATE_RESPONSE);
        long clientId = clientResponse.getClientId();
        long savingsId = ((PostSavingsAccountsResponse) testContext().get(TestContextKey.EUR_SAVINGS_ACCOUNT_CREATE_RESPONSE))
                .getSavingsId();
        long loanId = ((PostLoansResponse) testContext().get(TestContextKey.LOAN_CREATE_RESPONSE)).getLoanId();

        createAccountTransfer(clientId, savingsId, SAVINGS_ACCOUNT_TYPE, loanId, LOAN_ACCOUNT_TYPE, date, amount);
    }

    @When("Initiate account transfer from loan to savings on {string} for {double}")
    public void initiateLoanToSavingsTransfer(String date, double amount) {
        PostClientsResponse clientResponse = testContext().get(TestContextKey.CLIENT_CREATE_RESPONSE);
        long clientId = clientResponse.getClientId();
        long loanId = ((PostLoansResponse) testContext().get(TestContextKey.LOAN_CREATE_RESPONSE)).getLoanId();
        long savingsId = ((PostSavingsAccountsResponse) testContext().get(TestContextKey.EUR_SAVINGS_ACCOUNT_CREATE_RESPONSE))
                .getSavingsId();

        createAccountTransfer(clientId, loanId, LOAN_ACCOUNT_TYPE, savingsId, SAVINGS_ACCOUNT_TYPE, date, amount);
    }

    @When("Initiate account transfer from savings {string} to savings {string} on {string} for {double}")
    public void initiateAccountTransferBetweenSavings(String fromAlias, String toAlias, String date, double amount) {
        PostClientsResponse clientResponse = testContext().get(TestContextKey.CLIENT_CREATE_RESPONSE);
        long clientId = clientResponse.getClientId();
        long fromSavingsId = ((PostSavingsAccountsResponse) testContext().get("EUR_SAVINGS_ACCOUNT_CREATE_RESPONSE_" + fromAlias))
                .getSavingsId();
        long toSavingsId = ((PostSavingsAccountsResponse) testContext().get("EUR_SAVINGS_ACCOUNT_CREATE_RESPONSE_" + toAlias))
                .getSavingsId();

        createAccountTransfer(clientId, fromSavingsId, SAVINGS_ACCOUNT_TYPE, toSavingsId, SAVINGS_ACCOUNT_TYPE, date, amount);
    }

    @When("Undo the last account transfer")
    public void undoAccountTransfer() {
        PostAccountTransfersResponse response = testContext().get("accountTransferResponse");
        ok(() -> fineractClient.accountTransfers().accountTransferOperation(response.getResourceId(), "undo"));
    }

    @When("Undo the last account transfer it fails with error: it is already reverted")
    public void undoAccountTransferFail() {
        PostAccountTransfersResponse response = testContext().get("accountTransferResponse");
        CallFailedRuntimeException exception = fail(
                () -> fineractClient.accountTransfers().accountTransferOperation(response.getResourceId(), "undo"));
        assertThat(exception.getStatus()).as(ErrorMessageHelper.dateFailureErrorCodeMsg()).isEqualTo(403);
        assertThat(exception.getMessage()).contains("error.msg.account.transfer.already.reversed");
    }

    private void createAccountTransfer(long clientId, long fromAccountId, String fromAccountType, long toAccountId, String toAccountType,
            String date, double amount) {
        AccountTransferRequest request = new AccountTransferRequest().fromClientId(String.valueOf(clientId))
                .fromAccountId(String.valueOf(fromAccountId)).fromAccountType(fromAccountType).fromOfficeId("1")
                .toClientId(String.valueOf(clientId)).toAccountId(String.valueOf(toAccountId)).toAccountType(toAccountType).toOfficeId("1")
                .transferDate(date).transferAmount(String.valueOf(amount)).transferDescription("Transfer").dateFormat(DATE_FORMAT)
                .locale(DEFAULT_LOCALE);

        PostAccountTransfersResponse response = ok(() -> fineractClient.accountTransfers().createAccountTransfer(request));
        testContext().set("accountTransferResponse", response);
    }
}
