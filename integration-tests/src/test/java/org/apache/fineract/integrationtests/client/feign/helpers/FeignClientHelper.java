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

import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.models.AddressData;
import org.apache.fineract.client.models.ClientAddressRequest;
import org.apache.fineract.client.models.ClientAuditFieldsData;
import org.apache.fineract.client.models.ClientIdentifierRequest;
import org.apache.fineract.client.models.ClientTextSearch;
import org.apache.fineract.client.models.DeleteClientsClientIdResponse;
import org.apache.fineract.client.models.GetClientTransferProposalDateResponse;
import org.apache.fineract.client.models.GetClientsClientIdAccountsResponse;
import org.apache.fineract.client.models.GetClientsClientIdIdentifiersResponse;
import org.apache.fineract.client.models.GetClientsClientIdResponse;
import org.apache.fineract.client.models.GetClientsClientIdTransactionsResponse;
import org.apache.fineract.client.models.GetClientsClientIdTransactionsTransactionIdResponse;
import org.apache.fineract.client.models.GetClientsResponse;
import org.apache.fineract.client.models.GetObligeeData;
import org.apache.fineract.client.models.PageClientSearchData;
import org.apache.fineract.client.models.PagedRequestClientTextSearch;
import org.apache.fineract.client.models.PostClientClientIdAddressesResponse;
import org.apache.fineract.client.models.PostClientsClientIdChanges;
import org.apache.fineract.client.models.PostClientsClientIdIdentifiersRequest;
import org.apache.fineract.client.models.PostClientsClientIdIdentifiersResponse;
import org.apache.fineract.client.models.PostClientsClientIdRequest;
import org.apache.fineract.client.models.PostClientsClientIdResponse;
import org.apache.fineract.client.models.PostClientsClientIdTransactionsTransactionIdResponse;
import org.apache.fineract.client.models.PostClientsRequest;
import org.apache.fineract.client.models.PostClientsResponse;
import org.apache.fineract.client.models.PutClientClientIdAddressesResponse;
import org.apache.fineract.client.models.PutClientsClientIdIdentifiersIdentifierIdResponse;
import org.apache.fineract.client.models.PutClientsClientIdRequest;
import org.apache.fineract.client.models.PutClientsClientIdResponse;
import org.apache.fineract.client.models.SortOrder;
import org.apache.fineract.integrationtests.client.feign.modules.ClientRequestBuilders;
import org.apache.fineract.integrationtests.client.feign.modules.ClientTestData;
import org.apache.fineract.integrationtests.client.feign.modules.LoanTestData;
import org.apache.fineract.integrationtests.common.Utils;

public class FeignClientHelper {

    private static final String ACTIVATE_COMMAND = "activate";
    private static final String CLOSE_COMMAND = "close";
    private static final String REJECT_COMMAND = "reject";
    private static final String REACTIVATE_COMMAND = "reactivate";
    private static final String WITHDRAW_COMMAND = "withdraw";
    private static final String UNDO_REJECTION_COMMAND = "undoRejection";
    private static final String UNDO_WITHDRAWAL_COMMAND = "undoWithdrawal";
    private static final String ASSIGN_STAFF_COMMAND = "assignStaff";
    private static final String UNDO_COMMAND = "undo";

    private final FineractFeignClient fineractClient;
    private final FeignCodeHelper codeHelper;
    private final ClientIdentifierCommandsApi clientIdentifierCommandsApi;

    public FeignClientHelper(FineractFeignClient fineractClient) {
        this.fineractClient = fineractClient;
        this.codeHelper = new FeignCodeHelper(fineractClient);
        this.clientIdentifierCommandsApi = fineractClient.create(ClientIdentifierCommandsApi.class);
    }

    public Long createClient() {
        return createClient(org.apache.fineract.integrationtests.common.ClientHelper.DEFAULT_DATE);
    }

    public Long createClient(String activationDate) {
        String externalId = Utils.randomStringGenerator("EXT_", 7);

        PostClientsRequest request = new PostClientsRequest()//
                .officeId(1L)//
                .legalFormId(1L)//
                .firstname(Utils.randomFirstNameGenerator())//
                .lastname(Utils.randomLastNameGenerator())//
                .externalId(externalId)//
                .active(true)//
                .activationDate(activationDate)//
                .dateFormat(LoanTestData.DATETIME_PATTERN)//
                .locale(LoanTestData.LOCALE);

        return createClient(request).getClientId();
    }

    public PostClientsResponse createClient(PostClientsRequest request) {
        return ok(() -> fineractClient.clients().createClient(request));
    }

    public PostClientsResponse createClientPending() {
        return createClientPending(Utils.dateFormatter.format(Utils.getLocalDateOfTenant()));
    }

    public PostClientsResponse createClientPending(String submittedOnDate) {
        return createClientPending(ClientRequestBuilders.createPendingClient(submittedOnDate));
    }

    public PostClientsResponse createClientPending(PostClientsRequest request) {
        return ok(() -> fineractClient.clients().createClient(request));
    }

    public GetClientsClientIdResponse getClient(Long clientId) {
        return ok(() -> fineractClient.clients().retrieveOneClient(clientId, Collections.emptyMap()));
    }

    public CallFailedRuntimeException getClientExpectingError(Long clientId) {
        return fail(() -> fineractClient.clients().retrieveOneClient(clientId, Collections.emptyMap()));
    }

    public GetClientsClientIdAccountsResponse getClientAccounts(Long clientId) {
        return ok(() -> fineractClient.clients().retrieveAllClientAccounts(clientId));
    }

    public GetClientsClientIdAccountsResponse getClientAccounts(String externalId) {
        return ok(() -> fineractClient.clients().retrieveAllClientAccountsByExternalId(externalId));
    }

    /** Number of clients carrying the given external id; {@code 0} when no client was created with it. */
    public Integer countClientsByExternalId(String externalId) {
        GetClientsResponse clients = ok(() -> fineractClient.clients().retrieveAllClients(Map.of("externalId", externalId)));
        return clients.getTotalFilteredRecords();
    }

    public PageClientSearchData searchClients(String text) {
        ClientTextSearch clientTextSearch = new ClientTextSearch();
        clientTextSearch.setText(text);
        PagedRequestClientTextSearch request = new PagedRequestClientTextSearch();
        request.setRequest(clientTextSearch);
        return ok(() -> fineractClient.clientSearchV2().searchClientsByText(request));
    }

    public CallFailedRuntimeException createClientExpectingError(PostClientsRequest request) {
        return fail(() -> fineractClient.clients().createClient(request));
    }

    public GetClientsClientIdResponse getClient(String externalId) {
        return ok(() -> fineractClient.clients().retrieveOneClientByExternalId(externalId, Collections.emptyMap()));
    }

    public GetClientsResponse retrieveClients(Map<String, Object> queryParams) {
        return ok(() -> fineractClient.clients().retrieveAllClients(queryParams));
    }

    public CallFailedRuntimeException retrieveClientsExpectingError(Map<String, Object> queryParams) {
        return fail(() -> fineractClient.clients().retrieveAllClients(queryParams));
    }

    public PageClientSearchData searchClients(String text, int page, int pageSize) {
        PagedRequestClientTextSearch request = new PagedRequestClientTextSearch().request(new ClientTextSearch().text(text)).page(page)
                .size(pageSize);
        return ok(() -> fineractClient.clientSearchV2().searchClientsByText(request));
    }

    public PageClientSearchData searchClients(String text, SortOrder sortOrder) {
        PagedRequestClientTextSearch request = new PagedRequestClientTextSearch().request(new ClientTextSearch().text(text))
                .sorts(List.of(sortOrder));
        return ok(() -> fineractClient.clientSearchV2().searchClientsByText(request));
    }

    public GetClientTransferProposalDateResponse getProposedTransferDate(String externalId) {
        return ok(() -> fineractClient.clients().retrieveClientTransferTemplateByExternalId(externalId));
    }

    public List<GetObligeeData> getObligeeData(String externalId) {
        return ok(() -> fineractClient.clients().retrieveClientObligeeDetailsByExternalId(externalId));
    }

    public ClientAuditFieldsData getClientAuditFields(Long clientId) {
        return ok(() -> fineractClient.defaultApi().getInternalClientAuditFields(clientId));
    }

    /**
     * Closes the client on the given date for the first {@code ClientClosureReason}, seeding one if the code has none.
     */
    public PostClientsClientIdResponse closeClient(Long clientId, String closureDate) {
        return closeClient(clientId,
                ClientRequestBuilders.closeClient(codeHelper.retrieveOrCreateCodeValueId(ClientTestData.CLOSURE_REASON_CODE), closureDate));
    }

    /**
     * Rejects the client on the given date for the first {@code ClientRejectReason}, seeding one if the code has none.
     */
    public PostClientsClientIdResponse rejectClient(Long clientId, String rejectionDate) {
        return rejectClient(clientId, ClientRequestBuilders
                .rejectClient(codeHelper.retrieveOrCreateCodeValueId(ClientTestData.REJECTION_REASON_CODE), rejectionDate));
    }

    /**
     * Withdraws the client on the given date for the first {@code ClientWithdrawReason}, seeding one if the code has
     * none.
     */
    public PostClientsClientIdResponse withdrawClient(Long clientId, String withdrawalDate) {
        return withdrawClient(clientId, ClientRequestBuilders
                .withdrawClient(codeHelper.retrieveOrCreateCodeValueId(ClientTestData.WITHDRAWAL_REASON_CODE), withdrawalDate));
    }

    public PostClientsClientIdResponse activateClient(Long clientId, PostClientsClientIdRequest request) {
        return ok(() -> fineractClient.clients().handleCommandClient(clientId, request, ACTIVATE_COMMAND));
    }

    public PostClientsClientIdResponse closeClient(Long clientId, PostClientsClientIdRequest request) {
        return ok(() -> fineractClient.clients().handleCommandClient(clientId, request, CLOSE_COMMAND));
    }

    public PostClientsClientIdResponse rejectClient(Long clientId, PostClientsClientIdRequest request) {
        return ok(() -> fineractClient.clients().handleCommandClient(clientId, request, REJECT_COMMAND));
    }

    public PostClientsClientIdResponse reactivateClient(Long clientId, PostClientsClientIdRequest request) {
        return ok(() -> fineractClient.clients().handleCommandClient(clientId, request, REACTIVATE_COMMAND));
    }

    public PostClientsClientIdResponse withdrawClient(Long clientId, PostClientsClientIdRequest request) {
        return ok(() -> fineractClient.clients().handleCommandClient(clientId, request, WITHDRAW_COMMAND));
    }

    public PostClientsClientIdResponse undoRejectClient(Long clientId, PostClientsClientIdRequest request) {
        return ok(() -> fineractClient.clients().handleCommandClient(clientId, request, UNDO_REJECTION_COMMAND));
    }

    public PostClientsClientIdResponse undoWithdrawnClient(Long clientId, PostClientsClientIdRequest request) {
        return ok(() -> fineractClient.clients().handleCommandClient(clientId, request, UNDO_WITHDRAWAL_COMMAND));
    }

    public CallFailedRuntimeException activateClientExpectingError(Long clientId, PostClientsClientIdRequest request) {
        return fail(() -> fineractClient.clients().handleCommandClient(clientId, request, ACTIVATE_COMMAND));
    }

    public CallFailedRuntimeException undoRejectClientExpectingError(Long clientId, PostClientsClientIdRequest request) {
        return fail(() -> fineractClient.clients().handleCommandClient(clientId, request, UNDO_REJECTION_COMMAND));
    }

    public CallFailedRuntimeException undoWithdrawnClientExpectingError(Long clientId, PostClientsClientIdRequest request) {
        return fail(() -> fineractClient.clients().handleCommandClient(clientId, request, UNDO_WITHDRAWAL_COMMAND));
    }

    public PostClientsClientIdResponse activateClient(String externalId, PostClientsClientIdRequest request) {
        return ok(() -> fineractClient.clients().handleCommandClientByExternalId(externalId, request, ACTIVATE_COMMAND));
    }

    public PostClientsClientIdResponse closeClient(String externalId, PostClientsClientIdRequest request) {
        return ok(() -> fineractClient.clients().handleCommandClientByExternalId(externalId, request, CLOSE_COMMAND));
    }

    public PostClientsClientIdResponse rejectClient(String externalId, PostClientsClientIdRequest request) {
        return ok(() -> fineractClient.clients().handleCommandClientByExternalId(externalId, request, REJECT_COMMAND));
    }

    public PostClientsClientIdResponse reactivateClient(String externalId, PostClientsClientIdRequest request) {
        return ok(() -> fineractClient.clients().handleCommandClientByExternalId(externalId, request, REACTIVATE_COMMAND));
    }

    public PutClientsClientIdResponse updateClient(Long clientId, PutClientsClientIdRequest request) {
        return ok(() -> fineractClient.clients().updateClient(clientId, request));
    }

    public CallFailedRuntimeException updateClientExpectingError(Long clientId, PutClientsClientIdRequest request) {
        return fail(() -> fineractClient.clients().updateClient(clientId, request));
    }

    public PutClientsClientIdResponse updateClient(String externalId, PutClientsClientIdRequest request) {
        return ok(() -> fineractClient.clients().updateClientByExternalId(externalId, request));
    }

    public DeleteClientsClientIdResponse deleteClient(Long clientId) {
        return ok(() -> fineractClient.clients().deleteClient(clientId));
    }

    public DeleteClientsClientIdResponse deleteClient(String externalId) {
        return ok(() -> fineractClient.clients().deleteClientByExternalId(externalId));
    }

    public PostClientClientIdAddressesResponse createClientAddress(Long clientId, Long addressTypeId, ClientAddressRequest request) {
        return ok(() -> fineractClient.clientsAddress().createClientAddress(clientId, request, addressTypeId));
    }

    public PutClientClientIdAddressesResponse updateClientAddress(Long clientId, ClientAddressRequest request) {
        return ok(() -> fineractClient.clientsAddress().updateClientAddress(clientId, request));
    }

    public List<AddressData> getClientAddresses(Long clientId) {
        return ok(() -> fineractClient.clientsAddress().retrieveAllClientAddresses(clientId, Collections.emptyMap()));
    }

    public PostClientsClientIdIdentifiersResponse createClientIdentifier(Long clientId, PostClientsClientIdIdentifiersRequest request) {
        return ok(() -> fineractClient.clientIdentifier().createClientIdentifier(clientId, request));
    }

    public GetClientsClientIdIdentifiersResponse getClientIdentifier(Long clientId, Long identifierId) {
        return ok(() -> fineractClient.clientIdentifier().retrieveOneClientIdentifier(clientId, identifierId));
    }

    public PutClientsClientIdIdentifiersIdentifierIdResponse updateClientIdentifier(Long clientId, Long identifierId,
            ClientIdentifierRequest request) {
        return ok(() -> fineractClient.clientIdentifier().updateClientIdentifier(clientId, identifierId, request));
    }

    /**
     * Removes the identifier's issuance and expiry dates; see {@link ClientIdentifierCommandsApi} for why it is not
     * generated.
     */
    public PutClientsClientIdIdentifiersIdentifierIdResponse clearClientIdentifierDates(Long clientId, Long identifierId) {
        return ok(() -> clientIdentifierCommandsApi.clearIdentifierDates(clientId, identifierId,
                new ClientIdentifierCommandsApi.ClearIdentifierDatesRequest()));
    }

    public GetClientsClientIdTransactionsResponse getClientTransactions(String externalId) {
        return ok(() -> fineractClient.clientTransaction().retrieveAllClientTransactionsByClientExternalId(externalId, 0, 100));
    }

    public GetClientsClientIdTransactionsTransactionIdResponse getClientTransaction(Long clientId, Long transactionId) {
        return ok(() -> fineractClient.clientTransaction().retrieveClientTransaction(clientId, transactionId));
    }

    public GetClientsClientIdTransactionsTransactionIdResponse getClientTransaction(String externalId, Long transactionId) {
        return ok(() -> fineractClient.clientTransaction().retrieveClientTransactionByClientExternalId(externalId, transactionId));
    }

    public GetClientsClientIdTransactionsTransactionIdResponse getClientTransactionByTransactionExternalId(Long clientId,
            String transactionExternalId) {
        return ok(
                () -> fineractClient.clientTransaction().retrieveClientTransactionByTransactionExternalId(clientId, transactionExternalId));
    }

    public PostClientsClientIdTransactionsTransactionIdResponse undoClientTransaction(Long clientId, Long transactionId) {
        return ok(() -> fineractClient.clientTransaction().undoClientTransaction(clientId, transactionId, UNDO_COMMAND));
    }

    public PostClientsClientIdTransactionsTransactionIdResponse undoClientTransaction(String externalId, Long transactionId) {
        return ok(
                () -> fineractClient.clientTransaction().undoClientTransactionByClientExternalId(externalId, transactionId, UNDO_COMMAND));
    }

    public PostClientsClientIdTransactionsTransactionIdResponse undoClientTransactionByTransactionExternalId(Long clientId,
            String transactionExternalId) {
        return ok(() -> fineractClient.clientTransaction().undoClientTransactionByTransactionExternalId(clientId, transactionExternalId,
                UNDO_COMMAND));
    }

    /** Assigns a staff member to the client; returns the {@code changes} object. */
    public PostClientsClientIdChanges assignStaffToClient(Long clientId, Long staffId) {
        PostClientsClientIdRequest request = new PostClientsClientIdRequest().staffId(staffId);
        return ok(() -> fineractClient.clients().handleCommandClient(clientId, request, ASSIGN_STAFF_COMMAND)).getChanges();
    }

    /** The staff id currently assigned to the client, or {@code null} if none. */
    public Long getClientStaffId(Long clientId) {
        return getClient(clientId).getStaffId();
    }
}
