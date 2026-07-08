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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.apache.fineract.accounting.common.AccountingConstants;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.models.ExternalAssetOwnerRequest;
import org.apache.fineract.client.models.ExternalAssetOwnerSearchRequest;
import org.apache.fineract.client.models.ExternalOwnerJournalEntryData;
import org.apache.fineract.client.models.ExternalOwnerTransferJournalEntryData;
import org.apache.fineract.client.models.ExternalTransferData;
import org.apache.fineract.client.models.ExternalTransferOwnerData;
import org.apache.fineract.client.models.GetFinancialActivityAccountsResponse;
import org.apache.fineract.client.models.PageExternalTransferData;
import org.apache.fineract.client.models.PagedRequestExternalAssetOwnerSearchRequest;
import org.apache.fineract.client.models.PostExternalAssetOwnerRequest;
import org.apache.fineract.client.models.PostExternalAssetOwnerResponse;
import org.apache.fineract.client.models.PostFinancialActivityAccountsRequest;
import org.apache.fineract.client.models.PostInitiateTransferResponse;
import org.apache.fineract.integrationtests.common.accounting.Account;

public class FeignExternalAssetOwnerHelper {

    private static final int DEFAULT_OFFSET = 0;
    private static final int DEFAULT_LIMIT = 100;
    // increase it if tests create more than 200 items
    private static final int DEFAULT_SEARCH_PAGE_SIZE = 200;
    private static final int FORBIDDEN = 403;
    private static final String EFFECTIVE_ATTRIBUTE = "effective";
    private static final String SETTLEMENT_ATTRIBUTE = "settlement";
    private static final String CANCEL_COMMAND = "cancel";

    private final FineractFeignClient fineractClient;

    public FeignExternalAssetOwnerHelper(FineractFeignClient fineractClient) {
        this.fineractClient = fineractClient;
    }

    public PostExternalAssetOwnerResponse createExternalAssetOwner(PostExternalAssetOwnerRequest request) {
        return ok(() -> fineractClient.externalAssetOwners().createExternalAssetOwner(request));
    }

    public List<ExternalTransferOwnerData> retrieveExternalAssetOwners() {
        return ok(() -> fineractClient.externalAssetOwners().retrieveExternalAssetOwners());
    }

    public PostInitiateTransferResponse initiateTransferByLoanId(Long loanId, String command, ExternalAssetOwnerRequest request) {
        return ok(() -> fineractClient.externalAssetOwners().transferRequestWithLoanId(loanId, request, command));
    }

    public void cancelTransferByTransferExternalId(String transferExternalId) {
        ok(() -> fineractClient.externalAssetOwners().transferRequestWithIdByExternalId(transferExternalId, CANCEL_COMMAND));
    }

    public void cancelTransferByTransferExternalIdError(String transferExternalId) {
        CallFailedRuntimeException exception = assertThrows(CallFailedRuntimeException.class,
                () -> ok(() -> fineractClient.externalAssetOwners().transferRequestWithIdByExternalId(transferExternalId, CANCEL_COMMAND)));
        assertEquals(FORBIDDEN, exception.getStatus());
    }

    public PageExternalTransferData retrieveTransfersByLoanId(Long loanId) {
        return ok(() -> fineractClient.externalAssetOwners().getTransfers(null, loanId, null, DEFAULT_OFFSET, DEFAULT_LIMIT));
    }

    public ExternalTransferData retrieveActiveTransferByTransferExternalId(String transferExternalId) {
        return ok(() -> fineractClient.externalAssetOwners().getActiveTransfer(transferExternalId, null, null));
    }

    public ExternalTransferData retrieveActiveTransferByLoanId(Long loanId) {
        return ok(() -> fineractClient.externalAssetOwners().getActiveTransfer(null, loanId, null));
    }

    public ExternalOwnerTransferJournalEntryData retrieveJournalEntriesOfTransfer(Long transferId) {
        return ok(() -> fineractClient.externalAssetOwners().getJournalEntriesOfTransfer(transferId, DEFAULT_OFFSET, DEFAULT_LIMIT));
    }

    public ExternalOwnerJournalEntryData retrieveJournalEntriesOfOwner(String ownerExternalId) {
        return ok(() -> fineractClient.externalAssetOwners().getJournalEntriesOfOwner(ownerExternalId, DEFAULT_OFFSET, DEFAULT_LIMIT));
    }

    public PageExternalTransferData searchExternalAssetOwnerTransfer(PagedRequestExternalAssetOwnerSearchRequest request) {
        return ok(() -> fineractClient.externalAssetOwners().searchInvestorData(request));
    }

    public PagedRequestExternalAssetOwnerSearchRequest buildExternalAssetOwnerSearchRequest(String text, String attribute,
            LocalDate fromDate, LocalDate toDate, Integer page, Integer size) {
        PagedRequestExternalAssetOwnerSearchRequest pagedRequest = new PagedRequestExternalAssetOwnerSearchRequest();
        ExternalAssetOwnerSearchRequest searchRequest = new ExternalAssetOwnerSearchRequest();
        searchRequest.text(text);
        if (EFFECTIVE_ATTRIBUTE.equals(attribute)) {
            searchRequest.setEffectiveFromDate(fromDate);
            searchRequest.setEffectiveToDate(toDate);
        } else if (SETTLEMENT_ATTRIBUTE.equals(attribute)) {
            searchRequest.setSubmittedFromDate(fromDate);
            searchRequest.setSubmittedToDate(toDate);
        }
        pagedRequest.setRequest(searchRequest);
        pagedRequest.setSorts(new ArrayList<>());
        pagedRequest.setPage(page != null ? page : DEFAULT_OFFSET);
        pagedRequest.setSize(size != null ? size : DEFAULT_SEARCH_PAGE_SIZE);
        return pagedRequest;
    }

    public void setProperFinancialActivity(Account transferAccount) {
        List<GetFinancialActivityAccountsResponse> financialMappings = ok(
                () -> fineractClient.mappingFinancialActivitiesToAccounts().retrieveAll());
        financialMappings.forEach(mapping -> ok(() -> fineractClient.mappingFinancialActivitiesToAccounts()
                .deleteGLAccountMappingFinancialActivityAccount(mapping.getId())));
        ok(() -> fineractClient.mappingFinancialActivitiesToAccounts()
                .createGLAccountMappingFinancialActivityAccount(new PostFinancialActivityAccountsRequest()
                        .financialActivityId((long) AccountingConstants.FinancialActivity.ASSET_TRANSFER.getValue())
                        .glAccountId((long) transferAccount.getAccountID())));
    }
}
