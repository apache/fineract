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

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.apache.fineract.client.models.NoteCreateRequest;
import org.apache.fineract.client.models.NoteCreateResponse;
import org.apache.fineract.client.models.NoteData;
import org.apache.fineract.client.models.NoteDeleteResponse;
import org.apache.fineract.client.models.NoteUpdateRequest;
import org.apache.fineract.client.models.NoteUpdateResponse;
import org.apache.fineract.client.util.Calls;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.CollateralManagementHelper;
import org.apache.fineract.integrationtests.common.GroupHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.loans.LoanApplicationTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanTestLifecycleExtension;
import org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper;
import org.apache.fineract.integrationtests.common.savings.SavingsAccountHelper;
import org.apache.fineract.integrationtests.common.savings.SavingsProductHelper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import retrofit2.Response;

@SuppressWarnings({ "rawtypes" })
@ExtendWith(LoanTestLifecycleExtension.class)
public class NotesTest extends IntegrationTest {

    private ResponseSpecification responseSpec;
    private RequestSpecification requestSpec;
    private ResponseSpecification responseSpec404;
    private LoanTransactionHelper loanTransactionHelper;
    private SavingsProductHelper savingsProductHelper;
    private SavingsAccountHelper savingsAccountHelper;

    @BeforeEach
    public void setup() {
        Utils.initializeRESTAssured();
        this.requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        this.requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        this.responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
        this.responseSpec404 = new ResponseSpecBuilder().expectStatusCode(404).build();
        this.loanTransactionHelper = new LoanTransactionHelper(this.requestSpec, this.responseSpec);
        this.savingsProductHelper = new SavingsProductHelper();
        this.savingsAccountHelper = new SavingsAccountHelper(this.requestSpec, this.responseSpec);
    }

    @Test
    public void testCreateClientNote() {
        String noteText = "this is a test note";

        Integer clientId = ClientHelper.createClient(requestSpec, responseSpec);
        Assertions.assertNotNull(clientId);

        NoteCreateRequest request = new NoteCreateRequest().note(noteText);
        NoteCreateResponse noteCreateResponse = ok(fineractClient().notes.addNewNote("clients", Long.valueOf(clientId), request));
        Assertions.assertNotNull(noteCreateResponse);
        Long noteId = noteCreateResponse.getResourceId();
        Assertions.assertNotNull(noteId);

        NoteData noteData = ok(fineractClient().notes.retrieveNote("clients", Long.valueOf(clientId), noteId));
        Assertions.assertEquals(noteText, noteData.getNote());
    }

    @Test
    public void testUpdateClientNote() {
        String noteText = "this is a test note";

        Integer clientId = ClientHelper.createClient(requestSpec, responseSpec);
        Assertions.assertNotNull(clientId);

        NoteCreateRequest request = new NoteCreateRequest().note(noteText);
        NoteCreateResponse noteCreateResponse = ok(fineractClient().notes.addNewNote("clients", Long.valueOf(clientId), request));
        Assertions.assertNotNull(noteCreateResponse);
        Long noteId = noteCreateResponse.getResourceId();
        Assertions.assertNotNull(noteId);

        NoteData noteData = ok(fineractClient().notes.retrieveNote("clients", Long.valueOf(clientId), noteId));
        Assertions.assertEquals(noteText, noteData.getNote());

        String updatedNoteText = "this is an updated test note";

        NoteUpdateRequest updateRequest = new NoteUpdateRequest().note(updatedNoteText);
        NoteUpdateResponse noteUpdateResponse = ok(
                fineractClient().notes.updateNote("clients", Long.valueOf(clientId), noteId, updateRequest));
        Assertions.assertNotNull(noteUpdateResponse);

        noteData = ok(fineractClient().notes.retrieveNote("clients", Long.valueOf(clientId), noteId));
        Assertions.assertEquals(updatedNoteText, noteData.getNote());
    }

    @Test
    public void testDeleteClientNote() {
        String noteText = "this is a test note";

        Integer clientId = ClientHelper.createClient(requestSpec, responseSpec);
        Assertions.assertNotNull(clientId);

        NoteCreateRequest request = new NoteCreateRequest().note(noteText);
        NoteCreateResponse noteCreateResponse = ok(fineractClient().notes.addNewNote("clients", Long.valueOf(clientId), request));
        Assertions.assertNotNull(noteCreateResponse);
        Long noteId = noteCreateResponse.getResourceId();
        Assertions.assertNotNull(noteId);

        NoteData noteData = ok(fineractClient().notes.retrieveNote("clients", Long.valueOf(clientId), noteId));
        Assertions.assertEquals(noteText, noteData.getNote());

        NoteDeleteResponse noteDeleteResponse = ok(fineractClient().notes.deleteNote("clients", Long.valueOf(clientId), noteId));
        Assertions.assertNotNull(noteDeleteResponse);

        Response<NoteData> response = Calls.executeU(fineractClient().notes.retrieveNote("clients", Long.valueOf(clientId), noteId));
        Assertions.assertEquals(404, response.code());
    }

    @Test
    public void testCreateGroupNote() {
        String noteText = "this is a test group note";

        Integer groupId = GroupHelper.createGroup(requestSpec, responseSpec);
        Assertions.assertNotNull(groupId);

        NoteCreateRequest request = new NoteCreateRequest().note(noteText);
        NoteCreateResponse noteCreateResponse = ok(fineractClient().notes.addNewNote("groups", Long.valueOf(groupId), request));
        Assertions.assertNotNull(noteCreateResponse);
        Long noteId = noteCreateResponse.getResourceId();
        Assertions.assertNotNull(noteId);

        NoteData noteData = ok(fineractClient().notes.retrieveNote("groups", Long.valueOf(groupId), noteId));
        Assertions.assertEquals(noteText, noteData.getNote());
    }

    @Test
    public void testUpdateGroupNote() {
        String noteText = "this is a test group note";

        Integer groupId = GroupHelper.createGroup(requestSpec, responseSpec);
        Assertions.assertNotNull(groupId);

        NoteCreateRequest request = new NoteCreateRequest().note(noteText);
        NoteCreateResponse noteCreateResponse = ok(fineractClient().notes.addNewNote("groups", Long.valueOf(groupId), request));
        Assertions.assertNotNull(noteCreateResponse);
        Long noteId = noteCreateResponse.getResourceId();
        Assertions.assertNotNull(noteId);

        NoteData noteData = ok(fineractClient().notes.retrieveNote("groups", Long.valueOf(groupId), noteId));
        Assertions.assertEquals(noteText, noteData.getNote());

        String updatedNoteText = "this is an updated test group note";

        NoteUpdateRequest updateRequest = new NoteUpdateRequest().note(updatedNoteText);
        NoteUpdateResponse noteUpdateResponse = ok(
                fineractClient().notes.updateNote("groups", Long.valueOf(groupId), noteId, updateRequest));
        Assertions.assertNotNull(noteUpdateResponse);

        noteData = ok(fineractClient().notes.retrieveNote("groups", Long.valueOf(groupId), noteId));
        Assertions.assertEquals(updatedNoteText, noteData.getNote());
    }

    @Test
    public void testDeleteGroupNote() {
        String noteText = "this is a test group note";

        Integer groupId = GroupHelper.createGroup(requestSpec, responseSpec);
        Assertions.assertNotNull(groupId);

        NoteCreateRequest request = new NoteCreateRequest().note(noteText);
        NoteCreateResponse noteCreateResponse = ok(fineractClient().notes.addNewNote("groups", Long.valueOf(groupId), request));
        Assertions.assertNotNull(noteCreateResponse);
        Long noteId = noteCreateResponse.getResourceId();
        Assertions.assertNotNull(noteId);

        NoteData noteData = ok(fineractClient().notes.retrieveNote("groups", Long.valueOf(groupId), noteId));
        Assertions.assertEquals(noteText, noteData.getNote());

        NoteDeleteResponse noteDeleteResponse = ok(fineractClient().notes.deleteNote("groups", Long.valueOf(groupId), noteId));
        Assertions.assertNotNull(noteDeleteResponse);

        Response<NoteData> response = Calls.executeU(fineractClient().notes.retrieveNote("groups", Long.valueOf(groupId), noteId));
        Assertions.assertEquals(404, response.code());
    }

    @Test
    public void testCreateLoanNote() {
        String noteText = "this is a test loan note";

        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec, "01 January 2012");
        final Integer loanProductID = this.loanTransactionHelper.getLoanProductId(new LoanProductTestBuilder().build(null));
        final Integer loanId = applyForLoanApplication(clientID, loanProductID);
        Assertions.assertNotNull(loanId);

        NoteCreateRequest request = new NoteCreateRequest().note(noteText);
        NoteCreateResponse noteCreateResponse = ok(fineractClient().notes.addNewNote("loans", Long.valueOf(loanId), request));
        Assertions.assertNotNull(noteCreateResponse);
        Long noteId = noteCreateResponse.getResourceId();
        Assertions.assertNotNull(noteId);

        NoteData noteData = ok(fineractClient().notes.retrieveNote("loans", Long.valueOf(loanId), noteId));
        Assertions.assertEquals(noteText, noteData.getNote());
    }

    @Test
    public void testCreateSavingsNote() {
        final String noteText = "this is a test Savings note";
        final String testDate = "01 January 2012";

        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec, testDate);
        // Savings Account
        final String savingsProductJSON = this.savingsProductHelper.withInterestCompoundingPeriodTypeAsDaily()
                .withInterestPostingPeriodTypeAsDaily().withInterestCalculationPeriodTypeAsDailyBalance().build();
        final Integer savingsProductId = SavingsProductHelper.createSavingsProduct(savingsProductJSON, requestSpec, responseSpec);
        final Integer savingsId = this.savingsAccountHelper.applyForSavingsApplicationOnDate(clientID, savingsProductId, "INDIVIDUAL",
                testDate);
        Assertions.assertNotNull(savingsId);

        // Notes
        NoteCreateRequest request = new NoteCreateRequest().note(noteText);
        NoteCreateResponse noteCreateResponse = ok(fineractClient().notes.addNewNote("savings", Long.valueOf(savingsId), request));
        Assertions.assertNotNull(noteCreateResponse);
        Long noteId = noteCreateResponse.getResourceId();
        Assertions.assertNotNull(noteId);

        NoteData noteData = ok(fineractClient().notes.retrieveNote("savings", Long.valueOf(savingsId), noteId));
        Assertions.assertEquals(noteText, noteData.getNote());
    }

    private Integer applyForLoanApplication(final Integer clientID, final Integer loanProductID) {
        List<HashMap> collaterals = new ArrayList<>();
        final Integer collateralId = CollateralManagementHelper.createCollateralProduct(this.requestSpec, this.responseSpec);
        Assertions.assertNotNull(collateralId);
        final Integer clientCollateralId = CollateralManagementHelper.createClientCollateral(this.requestSpec, this.responseSpec,
                clientID.toString(), collateralId);
        Assertions.assertNotNull(clientCollateralId);
        addCollaterals(collaterals, clientCollateralId, BigDecimal.valueOf(1));

        final String loanApplication = new LoanApplicationTestBuilder().withPrincipal("5000").withLoanTermFrequency("5")
                .withLoanTermFrequencyAsMonths().withNumberOfRepayments("5").withRepaymentEveryAfter("1")
                .withRepaymentFrequencyTypeAsMonths().withInterestRatePerPeriod("2").withExpectedDisbursementDate("04 April 2012")
                .withCollaterals(collaterals).withSubmittedOnDate("02 April 2012")
                .build(clientID.toString(), loanProductID.toString(), null);
        return this.loanTransactionHelper.getLoanId(loanApplication);
    }

    private void addCollaterals(List<HashMap> collaterals, Integer collateralId, BigDecimal quantity) {
        collaterals.add(collaterals(collateralId, quantity));
    }

    private HashMap<String, String> collaterals(Integer collateralId, BigDecimal quantity) {
        HashMap<String, String> collateral = new HashMap<String, String>(2);
        collateral.put("clientCollateralId", collateralId.toString());
        collateral.put("quantity", quantity.toString());
        return collateral;
    }

    @Test
    public void testUpdateLoanNote() {
        String noteText = "this is a test loan note";

        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec, "01 January 2012");
        final Integer loanProductID = this.loanTransactionHelper.getLoanProductId(new LoanProductTestBuilder().build(null));
        final Integer loanId = applyForLoanApplication(clientID, loanProductID);
        Assertions.assertNotNull(loanId);

        NoteCreateRequest request = new NoteCreateRequest().note(noteText);
        NoteCreateResponse noteCreateResponse = ok(fineractClient().notes.addNewNote("loans", Long.valueOf(loanId), request));
        Assertions.assertNotNull(noteCreateResponse);
        Long noteId = noteCreateResponse.getResourceId();
        Assertions.assertNotNull(noteId);

        NoteData noteData = ok(fineractClient().notes.retrieveNote("loans", Long.valueOf(loanId), noteId));
        Assertions.assertEquals(noteText, noteData.getNote());

        String updatedNoteText = "this is an updated test loan note";

        NoteUpdateRequest updateRequest = new NoteUpdateRequest().note(updatedNoteText);
        NoteUpdateResponse noteUpdateResponse = ok(fineractClient().notes.updateNote("loans", Long.valueOf(loanId), noteId, updateRequest));
        Assertions.assertNotNull(noteUpdateResponse);

        noteData = ok(fineractClient().notes.retrieveNote("loans", Long.valueOf(loanId), noteId));
        Assertions.assertEquals(updatedNoteText, noteData.getNote());
    }

    @Test
    public void testDeleteLoanNote() {
        String noteText = "this is a test loan note";

        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec, "01 January 2012");
        final Integer loanProductID = this.loanTransactionHelper.getLoanProductId(new LoanProductTestBuilder().build(null));
        final Integer loanId = applyForLoanApplication(clientID, loanProductID);
        Assertions.assertNotNull(loanId);

        NoteCreateRequest request = new NoteCreateRequest().note(noteText);
        NoteCreateResponse noteCreateResponse = ok(fineractClient().notes.addNewNote("loans", Long.valueOf(loanId), request));
        Assertions.assertNotNull(noteCreateResponse);
        Long noteId = noteCreateResponse.getResourceId();
        Assertions.assertNotNull(noteId);

        NoteData noteData = ok(fineractClient().notes.retrieveNote("loans", Long.valueOf(loanId), noteId));
        Assertions.assertEquals(noteText, noteData.getNote());

        NoteDeleteResponse noteDeleteResponse = ok(fineractClient().notes.deleteNote("loans", Long.valueOf(loanId), noteId));
        Assertions.assertNotNull(noteDeleteResponse);

        Response<NoteData> response = Calls.executeU(fineractClient().notes.retrieveNote("loans", Long.valueOf(loanId), noteId));
        Assertions.assertEquals(404, response.code());
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
        this.loanTransactionHelper.disburseLoanWithNetDisbursalAmount("02 April 2012", loanId,
                JsonPath.from(loanDetails).get("netDisbursalAmount").toString());
        HashMap repayment = this.loanTransactionHelper.makeRepayment("02 April 2012", 100.0f, loanId);
        Integer loanTransactionId = (Integer) repayment.get("resourceId");
        Assertions.assertNotNull(loanTransactionId);

        NoteCreateRequest request = new NoteCreateRequest().note(noteText);
        NoteCreateResponse noteCreateResponse = ok(
                fineractClient().notes.addNewNote("loanTransactions", Long.valueOf(loanTransactionId), request));
        Assertions.assertNotNull(noteCreateResponse);
        Long noteId = noteCreateResponse.getResourceId();
        Assertions.assertNotNull(noteId);

        NoteData noteData = ok(fineractClient().notes.retrieveNote("loanTransactions", Long.valueOf(loanTransactionId), noteId));
        Assertions.assertEquals(noteText, noteData.getNote());
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
        this.loanTransactionHelper.disburseLoanWithNetDisbursalAmount("02 April 2012", loanId,
                JsonPath.from(loanDetails).get("netDisbursalAmount").toString());
        HashMap repayment = this.loanTransactionHelper.makeRepayment("02 April 2012", 100.0f, loanId);
        Integer loanTransactionId = (Integer) repayment.get("resourceId");
        Assertions.assertNotNull(loanTransactionId);

        NoteCreateRequest request = new NoteCreateRequest().note(noteText);
        NoteCreateResponse noteCreateResponse = ok(
                fineractClient().notes.addNewNote("loanTransactions", Long.valueOf(loanTransactionId), request));
        Assertions.assertNotNull(noteCreateResponse);
        Long noteId = noteCreateResponse.getResourceId();
        Assertions.assertNotNull(noteId);

        NoteData noteData = ok(fineractClient().notes.retrieveNote("loanTransactions", Long.valueOf(loanTransactionId), noteId));
        Assertions.assertEquals(noteText, noteData.getNote());

        String updatedNoteText = "this is an updated test loan transaction note";

        NoteUpdateRequest updateRequest = new NoteUpdateRequest().note(updatedNoteText);
        NoteUpdateResponse noteUpdateResponse = ok(
                fineractClient().notes.updateNote("loanTransactions", Long.valueOf(loanTransactionId), noteId, updateRequest));
        Assertions.assertNotNull(noteUpdateResponse);

        noteData = ok(fineractClient().notes.retrieveNote("loanTransactions", Long.valueOf(loanTransactionId), noteId));
        Assertions.assertEquals(updatedNoteText, noteData.getNote());
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
        this.loanTransactionHelper.disburseLoanWithNetDisbursalAmount("02 April 2012", loanId,
                JsonPath.from(loanDetails).get("netDisbursalAmount").toString());
        HashMap repayment = this.loanTransactionHelper.makeRepayment("02 April 2012", 100.0f, loanId);
        Integer loanTransactionId = (Integer) repayment.get("resourceId");
        Assertions.assertNotNull(loanTransactionId);

        NoteCreateRequest request = new NoteCreateRequest().note(noteText);
        NoteCreateResponse noteCreateResponse = ok(
                fineractClient().notes.addNewNote("loanTransactions", Long.valueOf(loanTransactionId), request));
        Assertions.assertNotNull(noteCreateResponse);
        Long noteId = noteCreateResponse.getResourceId();
        Assertions.assertNotNull(noteId);

        NoteData noteData = ok(fineractClient().notes.retrieveNote("loanTransactions", Long.valueOf(loanTransactionId), noteId));
        Assertions.assertEquals(noteText, noteData.getNote());

        NoteDeleteResponse noteDeleteResponse = ok(
                fineractClient().notes.deleteNote("loanTransactions", Long.valueOf(loanTransactionId), noteId));
        Assertions.assertNotNull(noteDeleteResponse);

        Response<NoteData> response = Calls
                .executeU(fineractClient().notes.retrieveNote("loanTransactions", Long.valueOf(loanTransactionId), noteId));
        Assertions.assertEquals(404, response.code());
    }

}
