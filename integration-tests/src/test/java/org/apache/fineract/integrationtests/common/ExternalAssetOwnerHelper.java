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
package org.apache.fineract.integrationtests.common;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.apache.fineract.accounting.common.AccountingConstants;
import org.apache.fineract.client.models.ExternalAssetOwnerSearchRequest;
import org.apache.fineract.client.models.ExternalOwnerJournalEntryData;
import org.apache.fineract.client.models.ExternalOwnerTransferJournalEntryData;
import org.apache.fineract.client.models.ExternalTransferData;
import org.apache.fineract.client.models.GetFinancialActivityAccountsResponse;
import org.apache.fineract.client.models.PageExternalTransferData;
import org.apache.fineract.client.models.PagedRequestExternalAssetOwnerSearchRequest;
import org.apache.fineract.client.models.PostFinancialActivityAccountsRequest;
import org.apache.fineract.client.models.PostInitiateTransferRequest;
import org.apache.fineract.client.models.PostInitiateTransferResponse;
import org.apache.fineract.client.util.Calls;
import org.apache.fineract.integrationtests.client.IntegrationTest;
import org.apache.fineract.integrationtests.common.accounting.Account;
import org.apache.fineract.integrationtests.common.accounting.FinancialActivityAccountHelper;
import retrofit2.Response;

public class ExternalAssetOwnerHelper extends IntegrationTest {

    public ExternalAssetOwnerHelper() {}

    public PostInitiateTransferResponse initiateTransferByLoanId(Long loanId, String command, PostInitiateTransferRequest request) {
        return ok(fineract().externalAssetOwners.transferRequestWithLoanId(loanId, request, command));
    }

    public void cancelTransferByTransferExternalId(String transferExternalId) {
        ok(fineract().externalAssetOwners.transferRequestWithId1(transferExternalId, "cancel"));
    }

    public void cancelTransferByTransferExternalIdError(String transferExternalId) {
        Response<PostInitiateTransferResponse> response = Calls
                .executeU(fineract().externalAssetOwners.transferRequestWithId1(transferExternalId, "cancel"));
        assertThat(response.code()).isEqualTo(403);
    }

    public PageExternalTransferData retrieveTransferByTransferExternalId(String transferExternalId) {
        return ok(fineract().externalAssetOwners.getTransfers(transferExternalId, null, null, 0, 100));
    }

    public PageExternalTransferData retrieveTransferByLoanExternalId(String loanExternalId) {
        return ok(fineract().externalAssetOwners.getTransfers(null, null, loanExternalId, 0, 100));
    }

    public PageExternalTransferData retrieveTransfersByLoanId(Long loanId) {
        return ok(fineract().externalAssetOwners.getTransfers(null, loanId, null, 0, 100));
    }

    public PageExternalTransferData retrieveTransfersByLoanId(Long loanId, int offset, int limit) {
        return ok(fineract().externalAssetOwners.getTransfers(null, loanId, null, offset, limit));
    }

    public ExternalTransferData retrieveActiveTransferByLoanExternalId(String loanExternalId) {
        return ok(fineract().externalAssetOwners.getActiveTransfer(null, null, loanExternalId));
    }

    public ExternalTransferData retrieveActiveTransferByTransferExternalId(String transferExternalId) {
        return ok(fineract().externalAssetOwners.getActiveTransfer(transferExternalId, null, null));
    }

    public ExternalTransferData retrieveActiveTransferByLoanId(Long loanId) {
        return ok(fineract().externalAssetOwners.getActiveTransfer(null, loanId, null));
    }

    public ExternalOwnerTransferJournalEntryData retrieveJournalEntriesOfTransfer(Long transferId) {
        return ok(fineract().externalAssetOwners.getJournalEntriesOfTransfer(transferId, 0, 100));
    }

    public ExternalOwnerJournalEntryData retrieveJournalEntriesOfOwner(String ownerExternalId) {
        return ok(fineract().externalAssetOwners.getJournalEntriesOfOwner(ownerExternalId, 0, 100));
    }

    public PageExternalTransferData searchExternalAssetOwnerTransfer(PagedRequestExternalAssetOwnerSearchRequest request) {
        return ok(fineract().externalAssetOwners.searchInvestorData(request));
    }

    public PagedRequestExternalAssetOwnerSearchRequest buildExternalAssetOwnerSearchRequest(String text, String attribute,
            LocalDate fromDate, LocalDate toDate, Integer page, Integer size) {
        // increase it if tests create more than 100 items
        final Integer DEFAULT_PAGE_SIZE = 100;
        PagedRequestExternalAssetOwnerSearchRequest pagedRequest = new PagedRequestExternalAssetOwnerSearchRequest();
        ExternalAssetOwnerSearchRequest searchRequest = new ExternalAssetOwnerSearchRequest();
        searchRequest.text(text);
        if (attribute.equals("effective")) {
            searchRequest.setEffectiveFromDate(fromDate);
            searchRequest.setEffectiveToDate(toDate);
        } else if (attribute.equals("settlement")) {
            searchRequest.setSubmittedFromDate(fromDate);
            searchRequest.setSubmittedToDate(toDate);
        }
        pagedRequest.setRequest(searchRequest);
        pagedRequest.setSorts(new ArrayList<>());
        pagedRequest.setPage(page != null ? page : 0);
        pagedRequest.setSize(size != null ? size : DEFAULT_PAGE_SIZE);
        return pagedRequest;
    }

    public void setProperFinancialActivity(FinancialActivityAccountHelper financialActivityAccountHelper, Account transferAccount) {
        List<GetFinancialActivityAccountsResponse> financialMappings = financialActivityAccountHelper.getAllFinancialActivityAccounts();
        financialMappings.forEach(mapping -> financialActivityAccountHelper.deleteFinancialActivityAccount(mapping.getId()));
        financialActivityAccountHelper.createFinancialActivityAccount(new PostFinancialActivityAccountsRequest()
                .financialActivityId((long) AccountingConstants.FinancialActivity.ASSET_TRANSFER.getValue())
                .glAccountId((long) transferAccount.getAccountID()));
    }

}
