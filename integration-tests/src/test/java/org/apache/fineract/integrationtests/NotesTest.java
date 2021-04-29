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
package org.apache.fineract.integrationtests;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.util.HashMap;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.GroupHelper;
import org.apache.fineract.integrationtests.common.NotesHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.loans.LoanApplicationTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@SuppressWarnings({ "rawtypes" })
public class NotesTest {

    private ResponseSpecification responseSpec;
    private RequestSpecification requestSpec;
    private ResponseSpecification responseSpec404;
    private LoanTransactionHelper loanTransactionHelper;

    @BeforeEach
    public void setup() {
        Utils.initializeRESTAssured();
        this.requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        this.requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        this.responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
        this.responseSpec404 = new ResponseSpecBuilder().expectStatusCode(404).build();
        this.loanTransactionHelper = new LoanTransactionHelper(this.requestSpec, this.responseSpec);
    }

    @Test
    public void testCreateClientNote() {
        String noteText = "this is a test note";

        Integer clientId = ClientHelper.createClient(requestSpec, responseSpec);
        Assertions.assertNotNull(clientId);

        String request = "{\"note\": \"" + noteText + "\"}";
        Integer noteId = NotesHelper.createClientNote(requestSpec, responseSpec, clientId, request);
        Assertions.assertNotNull(noteId);

        String receivedNoteText = NotesHelper.getClientNote(requestSpec, responseSpec, clientId, noteId);
        Assertions.assertEquals(noteText, receivedNoteText);
    }

    @Test
    public void testUpdateClientNote() {
        String noteText = "this is a test note";

        Integer clientId = ClientHelper.createClient(requestSpec, responseSpec);
        Assertions.assertNotNull(clientId);

        String request = "{\"note\": \"" + noteText + "\"}";
        Integer noteId = NotesHelper.createClientNote(requestSpec, responseSpec, clientId, request);
        Assertions.assertNotNull(noteId);

        String receivedNoteText = NotesHelper.getClientNote(requestSpec, responseSpec, clientId, noteId);
        Assertions.assertEquals(noteText, receivedNoteText);

        String updatedNoteText = "this is an updated test note";

        String updateRequest = "{\"note\": \"" + updatedNoteText + "\"}";
        NotesHelper.updateClientNote(requestSpec, responseSpec, clientId, noteId, updateRequest);

        receivedNoteText = NotesHelper.getClientNote(requestSpec, responseSpec, clientId, noteId);
        Assertions.assertEquals(updatedNoteText, receivedNoteText);
    }

    @Test
    public void testDeleteClientNote() {
        String noteText = "this is a test note";

        Integer clientId = ClientHelper.createClient(requestSpec, responseSpec);
        Assertions.assertNotNull(clientId);

        String request = "{\"note\": \"" + noteText + "\"}";
        Integer noteId = NotesHelper.createClientNote(requestSpec, responseSpec, clientId, request);
        Assertions.assertNotNull(noteId);

        String receivedNoteText = NotesHelper.getClientNote(requestSpec, responseSpec, clientId, noteId);
        Assertions.assertEquals(noteText, receivedNoteText);

        NotesHelper.deleteClientNote(requestSpec, responseSpec, clientId, noteId);

        NotesHelper.getClientNote(requestSpec, responseSpec404, clientId, noteId);
    }

    @Test
    public void testCreateGroupNote() {
        String noteText = "this is a test group note";

        Integer groupId = GroupHelper.createGroup(requestSpec, responseSpec);
        Assertions.assertNotNull(groupId);

        String request = "{\"note\": \"" + noteText + "\"}";
        Integer noteId = NotesHelper.createGroupNote(requestSpec, responseSpec, groupId, request);
        Assertions.assertNotNull(noteId);

        String receivedNoteText = NotesHelper.getGroupNote(requestSpec, responseSpec, groupId, noteId);
        Assertions.assertEquals(noteText, receivedNoteText);
    }

    @Test
    public void testUpdateGroupNote() {
        String noteText = "this is a test group note";

        Integer groupId = GroupHelper.createGroup(requestSpec, responseSpec);
        Assertions.assertNotNull(groupId);

        String request = "{\"note\": \"" + noteText + "\"}";
        Integer noteId = NotesHelper.createGroupNote(requestSpec, responseSpec, groupId, request);
        Assertions.assertNotNull(noteId);

        String receivedNoteText = NotesHelper.getGroupNote(requestSpec, responseSpec, groupId, noteId);
        Assertions.assertEquals(noteText, receivedNoteText);

        String updatedNoteText = "this is an updated test group note";

        String updateRequest = "{\"note\": \"" + updatedNoteText + "\"}";
        NotesHelper.updateGroupNote(requestSpec, responseSpec, groupId, noteId, updateRequest);

        receivedNoteText = NotesHelper.getGroupNote(requestSpec, responseSpec, groupId, noteId);
        Assertions.assertEquals(updatedNoteText, receivedNoteText);
    }

    @Test
    public void testDeleteGroupNote() {
        String noteText = "this is a test group note";

        Integer groupId = GroupHelper.createGroup(requestSpec, responseSpec);
        Assertions.assertNotNull(groupId);

        String request = "{\"note\": \"" + noteText + "\"}";
        Integer noteId = NotesHelper.createGroupNote(requestSpec, responseSpec, groupId, request);
        Assertions.assertNotNull(noteId);

        String receivedNoteText = NotesHelper.getGroupNote(requestSpec, responseSpec, groupId, noteId);
        Assertions.assertEquals(noteText, receivedNoteText);

        NotesHelper.deleteGroupNote(requestSpec, responseSpec, groupId, noteId);

        NotesHelper.getGroupNote(requestSpec, responseSpec404, groupId, noteId);
    }

    @Test
    public void testCreateLoanNote() {
        String noteText = "this is a test loan note";

        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec, "01 January 2012");
        final Integer loanProductID = this.loanTransactionHelper.getLoanProductId(new LoanProductTestBuilder().build(null));
        final Integer loanId = applyForLoanApplication(clientID, loanProductID);
        Assertions.assertNotNull(loanId);

        String request = "{\"note\": \"" + noteText + "\"}";
        Integer noteId = NotesHelper.createLoanNote(requestSpec, responseSpec, loanId, request);
        Assertions.assertNotNull(noteId);

        String receivedNoteText = NotesHelper.getLoanNote(requestSpec, responseSpec, loanId, noteId);
        Assertions.assertEquals(noteText, receivedNoteText);

    }

    private Integer applyForLoanApplication(final Integer clientID, final Integer loanProductID) {
        final String loanApplication = new LoanApplicationTestBuilder().withPrincipal("5000").withLoanTermFrequency("5")
                .withLoanTermFrequencyAsMonths().withNumberOfRepayments("5").withRepaymentEveryAfter("1")
                .withRepaymentFrequencyTypeAsMonths().withInterestRatePerPeriod("2").withExpectedDisbursementDate("04 April 2012")
                .withSubmittedOnDate("02 April 2012").build(clientID.toString(), loanProductID.toString(), null);
        return this.loanTransactionHelper.getLoanId(loanApplication);
    }

    @Test
    public void testUpdateLoanNote() {
        String noteText = "this is a test loan note";

        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec, "01 January 2012");
        final Integer loanProductID = this.loanTransactionHelper.getLoanProductId(new LoanProductTestBuilder().build(null));
        final Integer loanId = applyForLoanApplication(clientID, loanProductID);
        Assertions.assertNotNull(loanId);

        String request = "{\"note\": \"" + noteText + "\"}";
        Integer noteId = NotesHelper.createLoanNote(requestSpec, responseSpec, loanId, request);
        Assertions.assertNotNull(noteId);

        String receivedNoteText = NotesHelper.getLoanNote(requestSpec, responseSpec, loanId, noteId);
        Assertions.assertEquals(noteText, receivedNoteText);

        String updatedNoteText = "this is an updated test loan note";

        String updateRequest = "{\"note\": \"" + updatedNoteText + "\"}";
        NotesHelper.updateLoanNote(requestSpec, responseSpec, loanId, noteId, updateRequest);

        receivedNoteText = NotesHelper.getLoanNote(requestSpec, responseSpec, loanId, noteId);
        Assertions.assertEquals(updatedNoteText, receivedNoteText);
    }

    @Test
    public void testDeleteLoanNote() {
        String noteText = "this is a test loan note";

        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec, "01 January 2012");
        final Integer loanProductID = this.loanTransactionHelper.getLoanProductId(new LoanProductTestBuilder().build(null));
        final Integer loanId = applyForLoanApplication(clientID, loanProductID);
        Assertions.assertNotNull(loanId);

        String request = "{\"note\": \"" + noteText + "\"}";
        Integer noteId = NotesHelper.createLoanNote(requestSpec, responseSpec, loanId, request);
        Assertions.assertNotNull(noteId);

        String receivedNoteText = NotesHelper.getLoanNote(requestSpec, responseSpec, loanId, noteId);
        Assertions.assertEquals(noteText, receivedNoteText);

        NotesHelper.deleteLoanNote(requestSpec, responseSpec, loanId, noteId);

        NotesHelper.getLoanNote(requestSpec, responseSpec404, loanId, noteId);
    }

    @Test
    public void testCreateLoanTransactionNote() {
        String noteText = "this is a test loan transaction note";

        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec, "01 January 2012");
        final Integer loanProductID = this.loanTransactionHelper.getLoanProductId(new LoanProductTestBuilder().build(null));
        final Integer loanId = applyForLoanApplication(clientID, loanProductID);
        Assertions.assertNotNull(loanId);

        this.loanTransactionHelper.approveLoan("02 April 2012", loanId);
        String loanDetails = this.loanTransactionHelper.getLoanDetails(this.requestSpec, this.responseSpec, loanId);
        this.loanTransactionHelper.disburseLoan("02 April 2012", loanId, JsonPath.from(loanDetails).get("netDisbursalAmount").toString());
        HashMap repayment = this.loanTransactionHelper.makeRepayment("02 April 2012", 100.0f, loanId);
        Integer loanTransactionId = (Integer) repayment.get("resourceId");
        Assertions.assertNotNull(loanTransactionId);

        String request = "{\"note\": \"" + noteText + "\"}";
        Integer noteId = NotesHelper.createLoanTransactionNote(requestSpec, responseSpec, loanTransactionId, request);
        Assertions.assertNotNull(noteId);

        String receivedNoteText = NotesHelper.getLoanTransactionNote(requestSpec, responseSpec, loanTransactionId, noteId);
        Assertions.assertEquals(noteText, receivedNoteText);
    }

    @Test
    public void testUpdateLoanTransactionNote() {
        String noteText = "this is a test loan transaction note";

        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec, "01 January 2012");
        final Integer loanProductID = this.loanTransactionHelper.getLoanProductId(new LoanProductTestBuilder().build(null));
        final Integer loanId = applyForLoanApplication(clientID, loanProductID);
        Assertions.assertNotNull(loanId);

        this.loanTransactionHelper.approveLoan("02 April 2012", loanId);
        String loanDetails = this.loanTransactionHelper.getLoanDetails(this.requestSpec, this.responseSpec, loanId);
        this.loanTransactionHelper.disburseLoan("02 April 2012", loanId, JsonPath.from(loanDetails).get("netDisbursalAmount").toString());
        HashMap repayment = this.loanTransactionHelper.makeRepayment("02 April 2012", 100.0f, loanId);
        Integer loanTransactionId = (Integer) repayment.get("resourceId");
        Assertions.assertNotNull(loanTransactionId);

        String request = "{\"note\": \"" + noteText + "\"}";
        Integer noteId = NotesHelper.createLoanTransactionNote(requestSpec, responseSpec, loanTransactionId, request);
        Assertions.assertNotNull(noteId);

        String receivedNoteText = NotesHelper.getLoanTransactionNote(requestSpec, responseSpec, loanTransactionId, noteId);
        Assertions.assertEquals(noteText, receivedNoteText);

        String updatedNoteText = "this is an updated test loan transaction note";

        String updateRequest = "{\"note\": \"" + updatedNoteText + "\"}";
        NotesHelper.updateLoanTransactionNote(requestSpec, responseSpec, loanTransactionId, noteId, updateRequest);

        receivedNoteText = NotesHelper.getLoanTransactionNote(requestSpec, responseSpec, loanTransactionId, noteId);
        Assertions.assertEquals(updatedNoteText, receivedNoteText);

    }

    @Test
    public void testDeleteLoanTransactionNote() {
        String noteText = "this is a test loan transaction note";

        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec, "01 January 2012");
        final Integer loanProductID = this.loanTransactionHelper.getLoanProductId(new LoanProductTestBuilder().build(null));
        final Integer loanId = applyForLoanApplication(clientID, loanProductID);
        Assertions.assertNotNull(loanId);

        this.loanTransactionHelper.approveLoan("02 April 2012", loanId);
        String loanDetails = this.loanTransactionHelper.getLoanDetails(this.requestSpec, this.responseSpec, loanId);
        this.loanTransactionHelper.disburseLoan("02 April 2012", loanId, JsonPath.from(loanDetails).get("netDisbursalAmount").toString());
        HashMap repayment = this.loanTransactionHelper.makeRepayment("02 April 2012", 100.0f, loanId);
        Integer loanTransactionId = (Integer) repayment.get("resourceId");
        Assertions.assertNotNull(loanTransactionId);

        String request = "{\"note\": \"" + noteText + "\"}";
        Integer noteId = NotesHelper.createLoanTransactionNote(requestSpec, responseSpec, loanTransactionId, request);
        Assertions.assertNotNull(noteId);

        String receivedNoteText = NotesHelper.getLoanTransactionNote(requestSpec, responseSpec, loanTransactionId, noteId);
        Assertions.assertEquals(noteText, receivedNoteText);

        NotesHelper.deleteLoanTransactionNote(requestSpec, responseSpec, loanTransactionId, noteId);

        NotesHelper.getLoanTransactionNote(requestSpec, responseSpec404, loanTransactionId, noteId);
    }

}
