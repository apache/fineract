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

import com.google.gson.Gson;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import org.apache.fineract.client.models.GetResourceTypeResourceIdNotesNoteIdResponse;
import org.apache.fineract.client.models.PostResourceTypeResourceIdNotesResponse;
import org.apache.fineract.client.util.JSON;

@SuppressWarnings({ "rawtypes", "unchecked" })
public final class NotesHelper {

    private static final Gson GSON = new JSON().getGson();

    private NotesHelper() {

    }

    private static final String CLIENT_URL = "/fineract-provider/api/v1/clients";

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    public static Integer createClientNote(RequestSpecification requestSpec, ResponseSpecification responseSpec, Integer clientId,
            String request) {
        String createClientNoteURL = CLIENT_URL + "/" + clientId + "/notes?" + Utils.TENANT_IDENTIFIER;
        return Utils.performServerPost(requestSpec, responseSpec, createClientNoteURL, request, "resourceId");
    }

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    public static String getClientNote(RequestSpecification requestSpec, ResponseSpecification responseSpec, Integer clientId,
            Integer noteId) {
        String getClientNoteURL = CLIENT_URL + "/" + clientId + "/notes/" + noteId + "?" + Utils.TENANT_IDENTIFIER;
        return Utils.performServerGet(requestSpec, responseSpec, getClientNoteURL, "note");
    }

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    public static Integer updateClientNote(RequestSpecification requestSpec, ResponseSpecification responseSpec, Integer clientId,
            Integer noteId, String request) {
        String updateClientNoteURL = CLIENT_URL + "/" + clientId + "/notes/" + noteId + "?" + Utils.TENANT_IDENTIFIER;
        return Utils.performServerPut(requestSpec, responseSpec, updateClientNoteURL, request, "resourceId");
    }

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    public static void deleteClientNote(RequestSpecification requestSpec, ResponseSpecification responseSpec, Integer clientId,
            Integer noteId) {
        String deleteClientNoteURL = CLIENT_URL + "/" + clientId + "/notes/" + noteId + "?" + Utils.TENANT_IDENTIFIER;
        Utils.performServerDelete(requestSpec, responseSpec, deleteClientNoteURL, "");
    }

    private static final String GROUP_URL = "/fineract-provider/api/v1/groups";

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    public static Integer createGroupNote(RequestSpecification requestSpec, ResponseSpecification responseSpec, Integer groupId,
            String request) {
        String createGroupNoteURL = GROUP_URL + "/" + groupId + "/notes?" + Utils.TENANT_IDENTIFIER;
        return Utils.performServerPost(requestSpec, responseSpec, createGroupNoteURL, request, "resourceId");
    }

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    public static String getGroupNote(RequestSpecification requestSpec, ResponseSpecification responseSpec, Integer groupId,
            Integer noteId) {
        String getGroupNoteURL = GROUP_URL + "/" + groupId + "/notes/" + noteId + "?" + Utils.TENANT_IDENTIFIER;
        return Utils.performServerGet(requestSpec, responseSpec, getGroupNoteURL, "note");
    }

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    public static Integer updateGroupNote(RequestSpecification requestSpec, ResponseSpecification responseSpec, Integer groupId,
            Integer noteId, String request) {
        String updateGroupNoteURL = GROUP_URL + "/" + groupId + "/notes/" + noteId + "?" + Utils.TENANT_IDENTIFIER;
        return Utils.performServerPut(requestSpec, responseSpec, updateGroupNoteURL, request, "resourceId");
    }

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    public static void deleteGroupNote(RequestSpecification requestSpec, ResponseSpecification responseSpec, Integer groupId,
            Integer noteId) {
        String deleteGroupNoteURL = GROUP_URL + "/" + groupId + "/notes/" + noteId + "?" + Utils.TENANT_IDENTIFIER;
        Utils.performServerDelete(requestSpec, responseSpec, deleteGroupNoteURL, "");
    }

    private static final String LOAN_URL = "/fineract-provider/api/v1/loans";

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    public static Integer createLoanNote(RequestSpecification requestSpec, ResponseSpecification responseSpec, Integer loanId,
            String request) {
        String createLoanNoteURL = LOAN_URL + "/" + loanId + "/notes?" + Utils.TENANT_IDENTIFIER;
        return Utils.performServerPost(requestSpec, responseSpec, createLoanNoteURL, request, "resourceId");
    }

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    public static String getLoanNote(RequestSpecification requestSpec, ResponseSpecification responseSpec, Integer loanId, Integer noteId) {
        String getLoanNoteURL = LOAN_URL + "/" + loanId + "/notes/" + noteId + "?" + Utils.TENANT_IDENTIFIER;
        return Utils.performServerGet(requestSpec, responseSpec, getLoanNoteURL, "note");
    }

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    public static Integer updateLoanNote(RequestSpecification requestSpec, ResponseSpecification responseSpec, Integer loanId,
            Integer noteId, String updateRequest) {
        String updateLoanNoteURL = LOAN_URL + "/" + loanId + "/notes/" + noteId + "?" + Utils.TENANT_IDENTIFIER;
        return Utils.performServerPut(requestSpec, responseSpec, updateLoanNoteURL, updateRequest, "resourceId");
    }

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    public static void deleteLoanNote(RequestSpecification requestSpec, ResponseSpecification responseSpec, Integer loanId,
            Integer noteId) {
        String deleteLoanNoteURL = LOAN_URL + "/" + loanId + "/notes/" + noteId + "?" + Utils.TENANT_IDENTIFIER;
        Utils.performServerDelete(requestSpec, responseSpec, deleteLoanNoteURL, "");
    }

    private static final String LOAN_TRANSACTION_URL = "/fineract-provider/api/v1/loanTransactions";

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    public static Integer createLoanTransactionNote(RequestSpecification requestSpec, ResponseSpecification responseSpec,
            Integer loanTransactionId, String request) {
        String createLoanTransactionNoteURL = LOAN_TRANSACTION_URL + "/" + loanTransactionId + "/notes?" + Utils.TENANT_IDENTIFIER;
        return Utils.performServerPost(requestSpec, responseSpec, createLoanTransactionNoteURL, request, "resourceId");
    }

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    public static String getLoanTransactionNote(RequestSpecification requestSpec, ResponseSpecification responseSpec,
            Integer loanTransactionId, Integer noteId) {
        String getLoanTransactionNoteURL = LOAN_TRANSACTION_URL + "/" + loanTransactionId + "/notes/" + noteId + "?"
                + Utils.TENANT_IDENTIFIER;
        return Utils.performServerGet(requestSpec, responseSpec, getLoanTransactionNoteURL, "note");
    }

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    public static Integer updateLoanTransactionNote(RequestSpecification requestSpec, ResponseSpecification responseSpec,
            Integer loanTransactionId, Integer noteId, String updateRequest) {
        String updateLoanTransactionNoteURL = LOAN_TRANSACTION_URL + "/" + loanTransactionId + "/notes/" + noteId + "?"
                + Utils.TENANT_IDENTIFIER;
        return Utils.performServerPut(requestSpec, responseSpec, updateLoanTransactionNoteURL, updateRequest, "resourceId");
    }

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    public static void deleteLoanTransactionNote(RequestSpecification requestSpec, ResponseSpecification responseSpec,
            Integer loanTransactionId, Integer noteId) {
        String deleteLoanTransactionNoteURL = LOAN_TRANSACTION_URL + "/" + loanTransactionId + "/notes/" + noteId + "?"
                + Utils.TENANT_IDENTIFIER;
        Utils.performServerDelete(requestSpec, responseSpec, deleteLoanTransactionNoteURL, "");
    }

    private static final String SAVINGS_URL = "/fineract-provider/api/v1/savings";

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    public static PostResourceTypeResourceIdNotesResponse createSavingsNote(RequestSpecification requestSpec,
            ResponseSpecification responseSpec, Integer savingsId, String request) {
        final String noteURL = SAVINGS_URL + "/" + savingsId + "/notes?" + Utils.TENANT_IDENTIFIER;
        final String response = Utils.performServerPost(requestSpec, responseSpec, noteURL, request);
        return GSON.fromJson(response, PostResourceTypeResourceIdNotesResponse.class);
    }

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    public static GetResourceTypeResourceIdNotesNoteIdResponse getSavingsNote(RequestSpecification requestSpec,
            ResponseSpecification responseSpec, Integer savingsId, Integer noteId) {
        final String noteURL = SAVINGS_URL + "/" + savingsId + "/notes/" + noteId + "?" + Utils.TENANT_IDENTIFIER;
        final String response = Utils.performServerGet(requestSpec, responseSpec, noteURL);
        return GSON.fromJson(response, GetResourceTypeResourceIdNotesNoteIdResponse.class);
    }

}
