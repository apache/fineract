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
package org.apache.fineract.integrationtests.client;

import static org.apache.fineract.integrationtests.client.feign.modules.ClientTestData.DEFAULT_ACTIVATION_DATE;

import java.math.BigDecimal;
import java.util.List;
import org.apache.fineract.client.models.NoteCreateResponse;
import org.apache.fineract.client.models.NoteData;
import org.apache.fineract.client.models.NoteDeleteResponse;
import org.apache.fineract.client.models.NoteUpdateResponse;
import org.apache.fineract.client.models.PostLoansLoanIdRequest;
import org.apache.fineract.client.models.PostLoansRequest;
import org.apache.fineract.client.models.PostLoansRequestCollateralData;
import org.apache.fineract.integrationtests.client.feign.FeignLoanTestBase;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignCollateralHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignGroupHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignNoteHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignSavingsHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignSavingsProductHelper;
import org.apache.fineract.integrationtests.client.feign.modules.LoanRequestBuilders;
import org.apache.fineract.integrationtests.client.feign.modules.LoanTestData;
import org.apache.fineract.integrationtests.client.feign.modules.SavingsRequestBuilders;
import org.apache.fineract.integrationtests.client.feign.modules.SavingsTestData;
import org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class NotesTest extends FeignLoanTestBase {

    private static final int NOT_FOUND = 404;
    private static final String CLIENTS = "clients";
    private static final String GROUPS = "groups";
    private static final String LOANS = "loans";
    private static final String SAVINGS = "savings";
    private static final String LOAN_TRANSACTIONS = "loanTransactions";
    private static final String LOAN_CLIENT_ACTIVATION_DATE = "01 January 2012";
    private static final String LOAN_TRANSACTION_DATE = "02 April 2012";
    private static final String DISBURSE_NOTE = "DISBURSE NOTE";

    private FeignNoteHelper noteHelper;
    private FeignGroupHelper groupHelper;
    private FeignCollateralHelper collateralHelper;
    private FeignSavingsProductHelper savingsProductHelper;
    private FeignSavingsHelper savingsHelper;

    @BeforeAll
    public void setup() {
        noteHelper = new FeignNoteHelper(fineractClient());
        groupHelper = new FeignGroupHelper(fineractClient());
        collateralHelper = new FeignCollateralHelper(fineractClient());
        savingsProductHelper = new FeignSavingsProductHelper(fineractClient());
        savingsHelper = new FeignSavingsHelper(fineractClient());
    }

    @Test
    public void testCreateClientNote() {
        String noteText = "this is a test note";

        Long clientId = createClient(DEFAULT_ACTIVATION_DATE);
        Assertions.assertNotNull(clientId);

        NoteCreateResponse noteCreateResponse = noteHelper.addNote(CLIENTS, clientId, noteText);
        Assertions.assertNotNull(noteCreateResponse);
        Long noteId = noteCreateResponse.getResourceId();
        Assertions.assertNotNull(noteId);

        NoteData noteData = noteHelper.getNote(CLIENTS, clientId, noteId);
        Assertions.assertEquals(noteText, noteData.getNote());
    }

    @Test
    public void testUpdateClientNote() {
        String noteText = "this is a test note";

        Long clientId = createClient(DEFAULT_ACTIVATION_DATE);
        Assertions.assertNotNull(clientId);

        NoteCreateResponse noteCreateResponse = noteHelper.addNote(CLIENTS, clientId, noteText);
        Assertions.assertNotNull(noteCreateResponse);
        Long noteId = noteCreateResponse.getResourceId();
        Assertions.assertNotNull(noteId);

        NoteData noteData = noteHelper.getNote(CLIENTS, clientId, noteId);
        Assertions.assertEquals(noteText, noteData.getNote());

        String updatedNoteText = "this is an updated test note";

        NoteUpdateResponse noteUpdateResponse = noteHelper.updateNote(CLIENTS, clientId, noteId, updatedNoteText);
        Assertions.assertNotNull(noteUpdateResponse);

        noteData = noteHelper.getNote(CLIENTS, clientId, noteId);
        Assertions.assertEquals(updatedNoteText, noteData.getNote());
    }

    @Test
    public void testDeleteClientNote() {
        String noteText = "this is a test note";

        Long clientId = createClient(DEFAULT_ACTIVATION_DATE);
        Assertions.assertNotNull(clientId);

        NoteCreateResponse noteCreateResponse = noteHelper.addNote(CLIENTS, clientId, noteText);
        Assertions.assertNotNull(noteCreateResponse);
        Long noteId = noteCreateResponse.getResourceId();
        Assertions.assertNotNull(noteId);

        NoteData noteData = noteHelper.getNote(CLIENTS, clientId, noteId);
        Assertions.assertEquals(noteText, noteData.getNote());

        NoteDeleteResponse noteDeleteResponse = noteHelper.deleteNote(CLIENTS, clientId, noteId);
        Assertions.assertNotNull(noteDeleteResponse);

        Assertions.assertEquals(NOT_FOUND, noteHelper.getNoteExpectingError(CLIENTS, clientId, noteId).getStatus());
    }

    @Test
    public void testCreateGroupNote() {
        String noteText = "this is a test group note";

        Long groupId = groupHelper.createGroup().getGroupId();
        Assertions.assertNotNull(groupId);

        NoteCreateResponse noteCreateResponse = noteHelper.addNote(GROUPS, groupId, noteText);
        Assertions.assertNotNull(noteCreateResponse);
        Long noteId = noteCreateResponse.getResourceId();
        Assertions.assertNotNull(noteId);

        NoteData noteData = noteHelper.getNote(GROUPS, groupId, noteId);
        Assertions.assertEquals(noteText, noteData.getNote());
    }

    @Test
    public void testUpdateGroupNote() {
        String noteText = "this is a test group note";

        Long groupId = groupHelper.createGroup().getGroupId();
        Assertions.assertNotNull(groupId);

        NoteCreateResponse noteCreateResponse = noteHelper.addNote(GROUPS, groupId, noteText);
        Assertions.assertNotNull(noteCreateResponse);
        Long noteId = noteCreateResponse.getResourceId();
        Assertions.assertNotNull(noteId);

        NoteData noteData = noteHelper.getNote(GROUPS, groupId, noteId);
        Assertions.assertEquals(noteText, noteData.getNote());

        String updatedNoteText = "this is an updated test group note";

        NoteUpdateResponse noteUpdateResponse = noteHelper.updateNote(GROUPS, groupId, noteId, updatedNoteText);
        Assertions.assertNotNull(noteUpdateResponse);

        noteData = noteHelper.getNote(GROUPS, groupId, noteId);
        Assertions.assertEquals(updatedNoteText, noteData.getNote());
    }

    @Test
    public void testDeleteGroupNote() {
        String noteText = "this is a test group note";

        Long groupId = groupHelper.createGroup().getGroupId();
        Assertions.assertNotNull(groupId);

        NoteCreateResponse noteCreateResponse = noteHelper.addNote(GROUPS, groupId, noteText);
        Assertions.assertNotNull(noteCreateResponse);
        Long noteId = noteCreateResponse.getResourceId();
        Assertions.assertNotNull(noteId);

        NoteData noteData = noteHelper.getNote(GROUPS, groupId, noteId);
        Assertions.assertEquals(noteText, noteData.getNote());

        NoteDeleteResponse noteDeleteResponse = noteHelper.deleteNote(GROUPS, groupId, noteId);
        Assertions.assertNotNull(noteDeleteResponse);

        Assertions.assertEquals(NOT_FOUND, noteHelper.getNoteExpectingError(GROUPS, groupId, noteId).getStatus());
    }

    @Test
    public void testCreateLoanNote() {
        String noteText = "this is a test loan note";

        final Long clientID = createClient(LOAN_CLIENT_ACTIVATION_DATE);
        final Long loanProductID = createLoanProduct(new LoanProductTestBuilder().buildRequest());
        final Long loanId = applyForLoanApplication(clientID, loanProductID);
        Assertions.assertNotNull(loanId);

        NoteCreateResponse noteCreateResponse = noteHelper.addNote(LOANS, loanId, noteText);
        Assertions.assertNotNull(noteCreateResponse);
        Long noteId = noteCreateResponse.getResourceId();
        Assertions.assertNotNull(noteId);

        NoteData noteData = noteHelper.getNote(LOANS, loanId, noteId);
        Assertions.assertEquals(noteText, noteData.getNote());
    }

    @Test
    public void testCreateSavingsNote() {
        final String noteText = "this is a test Savings note";
        final String testDate = "01 January 2012";

        final Long clientID = createClient(testDate);
        // Savings Account
        final Long savingsProductId = savingsProductHelper
                .createSavingsProduct(SavingsRequestBuilders.savingsProduct(SavingsTestData.InterestCompoundingPeriodType.DAILY,
                        SavingsTestData.InterestPostingPeriodType.DAILY, SavingsTestData.InterestCalculationType.DAILY_BALANCE))
                .getResourceId();
        final Long savingsId = savingsHelper.submitApplication(clientID, savingsProductId, testDate).getSavingsId();
        Assertions.assertNotNull(savingsId);

        // Notes
        NoteCreateResponse noteCreateResponse = noteHelper.addNote(SAVINGS, savingsId, noteText);
        Assertions.assertNotNull(noteCreateResponse);
        Long noteId = noteCreateResponse.getResourceId();
        Assertions.assertNotNull(noteId);

        NoteData noteData = noteHelper.getNote(SAVINGS, savingsId, noteId);
        Assertions.assertEquals(noteText, noteData.getNote());
    }

    private Long applyForLoanApplication(final Long clientID, final Long loanProductID) {
        final Long collateralId = collateralHelper.createCollateralProduct().getResourceId();
        Assertions.assertNotNull(collateralId);
        final Long clientCollateralId = collateralHelper.createClientCollateral(clientID, collateralId).getResourceId();
        Assertions.assertNotNull(clientCollateralId);

        final PostLoansRequest loanApplication = LoanRequestBuilders
                .legacyIndividualApplication(clientID, loanProductID, "5000", 5, BigDecimal.valueOf(2), "04 April 2012")//
                .submittedOnDate("02 April 2012")//
                .amortizationType(LoanTestData.AmortizationType.EQUAL_PRINCIPAL)//
                .interestType(LoanTestData.InterestType.FLAT)//
                .collateral(List.of(new PostLoansRequestCollateralData().clientCollateralId(clientCollateralId).quantity(BigDecimal.ONE)));
        return applyForLoan(loanApplication);
    }

    private Long createActiveLoanTransaction(final Long loanId) {
        approveLoan(loanId, new PostLoansLoanIdRequest().approvedOnDate(LOAN_TRANSACTION_DATE).locale(LoanTestData.LOCALE)
                .dateFormat(LoanTestData.DATETIME_PATTERN));
        disburseLoan(loanId,
                LoanRequestBuilders
                        .disburseLoanWithNetDisbursalAmount(LOAN_TRANSACTION_DATE, getLoanDetails(loanId).getNetDisbursalAmount())
                        .note(DISBURSE_NOTE));
        return makeLoanRepayment(loanId, "repayment", LOAN_TRANSACTION_DATE, 100.0).getResourceId();
    }

    @Test
    public void testUpdateLoanNote() {
        String noteText = "this is a test loan note";

        final Long clientID = createClient(LOAN_CLIENT_ACTIVATION_DATE);
        final Long loanProductID = createLoanProduct(new LoanProductTestBuilder().buildRequest());
        final Long loanId = applyForLoanApplication(clientID, loanProductID);
        Assertions.assertNotNull(loanId);

        NoteCreateResponse noteCreateResponse = noteHelper.addNote(LOANS, loanId, noteText);
        Assertions.assertNotNull(noteCreateResponse);
        Long noteId = noteCreateResponse.getResourceId();
        Assertions.assertNotNull(noteId);

        NoteData noteData = noteHelper.getNote(LOANS, loanId, noteId);
        Assertions.assertEquals(noteText, noteData.getNote());

        String updatedNoteText = "this is an updated test loan note";

        NoteUpdateResponse noteUpdateResponse = noteHelper.updateNote(LOANS, loanId, noteId, updatedNoteText);
        Assertions.assertNotNull(noteUpdateResponse);

        noteData = noteHelper.getNote(LOANS, loanId, noteId);
        Assertions.assertEquals(updatedNoteText, noteData.getNote());
    }

    @Test
    public void testDeleteLoanNote() {
        String noteText = "this is a test loan note";

        final Long clientID = createClient(LOAN_CLIENT_ACTIVATION_DATE);
        final Long loanProductID = createLoanProduct(new LoanProductTestBuilder().buildRequest());
        final Long loanId = applyForLoanApplication(clientID, loanProductID);
        Assertions.assertNotNull(loanId);

        NoteCreateResponse noteCreateResponse = noteHelper.addNote(LOANS, loanId, noteText);
        Assertions.assertNotNull(noteCreateResponse);
        Long noteId = noteCreateResponse.getResourceId();
        Assertions.assertNotNull(noteId);

        NoteData noteData = noteHelper.getNote(LOANS, loanId, noteId);
        Assertions.assertEquals(noteText, noteData.getNote());

        NoteDeleteResponse noteDeleteResponse = noteHelper.deleteNote(LOANS, loanId, noteId);
        Assertions.assertNotNull(noteDeleteResponse);

        Assertions.assertEquals(NOT_FOUND, noteHelper.getNoteExpectingError(LOANS, loanId, noteId).getStatus());
    }

    @Test
    public void testCreateLoanTransactionNote() {
        String noteText = "this is a test loan transaction note";

        final Long clientID = createClient(LOAN_CLIENT_ACTIVATION_DATE);
        final Long loanProductID = createLoanProduct(new LoanProductTestBuilder().buildRequest());
        final Long loanId = applyForLoanApplication(clientID, loanProductID);
        Assertions.assertNotNull(loanId);

        Long loanTransactionId = createActiveLoanTransaction(loanId);
        Assertions.assertNotNull(loanTransactionId);

        NoteCreateResponse noteCreateResponse = noteHelper.addNote(LOAN_TRANSACTIONS, loanTransactionId, noteText);
        Assertions.assertNotNull(noteCreateResponse);
        Long noteId = noteCreateResponse.getResourceId();
        Assertions.assertNotNull(noteId);

        NoteData noteData = noteHelper.getNote(LOAN_TRANSACTIONS, loanTransactionId, noteId);
        Assertions.assertEquals(noteText, noteData.getNote());
    }

    @Test
    public void testUpdateLoanTransactionNote() {
        String noteText = "this is a test loan transaction note";

        final Long clientID = createClient(LOAN_CLIENT_ACTIVATION_DATE);
        final Long loanProductID = createLoanProduct(new LoanProductTestBuilder().buildRequest());
        final Long loanId = applyForLoanApplication(clientID, loanProductID);
        Assertions.assertNotNull(loanId);

        Long loanTransactionId = createActiveLoanTransaction(loanId);
        Assertions.assertNotNull(loanTransactionId);

        NoteCreateResponse noteCreateResponse = noteHelper.addNote(LOAN_TRANSACTIONS, loanTransactionId, noteText);
        Assertions.assertNotNull(noteCreateResponse);
        Long noteId = noteCreateResponse.getResourceId();
        Assertions.assertNotNull(noteId);

        NoteData noteData = noteHelper.getNote(LOAN_TRANSACTIONS, loanTransactionId, noteId);
        Assertions.assertEquals(noteText, noteData.getNote());

        String updatedNoteText = "this is an updated test loan transaction note";

        NoteUpdateResponse noteUpdateResponse = noteHelper.updateNote(LOAN_TRANSACTIONS, loanTransactionId, noteId, updatedNoteText);
        Assertions.assertNotNull(noteUpdateResponse);

        noteData = noteHelper.getNote(LOAN_TRANSACTIONS, loanTransactionId, noteId);
        Assertions.assertEquals(updatedNoteText, noteData.getNote());
    }

    @Test
    public void testDeleteLoanTransactionNote() {
        String noteText = "this is a test loan transaction note";

        final Long clientID = createClient(LOAN_CLIENT_ACTIVATION_DATE);
        final Long loanProductID = createLoanProduct(new LoanProductTestBuilder().buildRequest());
        final Long loanId = applyForLoanApplication(clientID, loanProductID);
        Assertions.assertNotNull(loanId);

        Long loanTransactionId = createActiveLoanTransaction(loanId);
        Assertions.assertNotNull(loanTransactionId);

        NoteCreateResponse noteCreateResponse = noteHelper.addNote(LOAN_TRANSACTIONS, loanTransactionId, noteText);
        Assertions.assertNotNull(noteCreateResponse);
        Long noteId = noteCreateResponse.getResourceId();
        Assertions.assertNotNull(noteId);

        NoteData noteData = noteHelper.getNote(LOAN_TRANSACTIONS, loanTransactionId, noteId);
        Assertions.assertEquals(noteText, noteData.getNote());

        NoteDeleteResponse noteDeleteResponse = noteHelper.deleteNote(LOAN_TRANSACTIONS, loanTransactionId, noteId);
        Assertions.assertNotNull(noteDeleteResponse);

        Assertions.assertEquals(NOT_FOUND, noteHelper.getNoteExpectingError(LOAN_TRANSACTIONS, loanTransactionId, noteId).getStatus());
    }

}
