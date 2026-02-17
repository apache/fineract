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
package org.apache.fineract.test.stepdef.loan;

import static org.apache.fineract.client.feign.util.FeignCalls.executeVoid;
import static org.apache.fineract.client.feign.util.FeignCalls.fail;
import static org.apache.fineract.client.feign.util.FeignCalls.failVoid;
import static org.apache.fineract.client.feign.util.FeignCalls.ok;
import static org.assertj.core.api.Assertions.assertThat;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.avro.loan.v1.OriginatorDetailsV1;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.models.DeleteLoanOriginatorsResponse;
import org.apache.fineract.client.models.GetCodeValuesDataResponse;
import org.apache.fineract.client.models.GetLoanOriginatorTemplateResponse;
import org.apache.fineract.client.models.GetLoanOriginatorsResponse;
import org.apache.fineract.client.models.GetLoansLoanIdOriginatorData;
import org.apache.fineract.client.models.GetLoansLoanIdResponse;
import org.apache.fineract.client.models.PostClientsResponse;
import org.apache.fineract.client.models.PostLoanOriginatorsRequest;
import org.apache.fineract.client.models.PostLoanOriginatorsResponse;
import org.apache.fineract.client.models.PostLoansLoanIdRequest;
import org.apache.fineract.client.models.PostLoansLoanIdResponse;
import org.apache.fineract.client.models.PostLoansOriginatorData;
import org.apache.fineract.client.models.PostLoansRequest;
import org.apache.fineract.client.models.PostLoansResponse;
import org.apache.fineract.client.models.PutLoanOriginatorsRequest;
import org.apache.fineract.client.models.PutLoanOriginatorsResponse;
import org.apache.fineract.test.api.ApiProperties;
import org.apache.fineract.test.factory.LoanRequestFactory;
import org.apache.fineract.test.helper.ErrorMessageHelper;
import org.apache.fineract.test.messaging.EventAssertion;
import org.apache.fineract.test.messaging.event.loan.LoanApprovedEvent;
import org.apache.fineract.test.messaging.store.EventStore;
import org.apache.fineract.test.stepdef.AbstractStepDef;
import org.apache.fineract.test.support.TestContextKey;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
public class LoanOriginationStepDef extends AbstractStepDef {

    private static final long NON_EXISTENT_ID = Long.MAX_VALUE;

    @Autowired
    private FineractFeignClient fineractClient;

    @Autowired
    private LoanRequestFactory loanRequestFactory;

    @Autowired
    private EventAssertion eventAssertion;

    @Autowired
    private EventStore eventStore;

    @Autowired
    private ApiProperties apiProperties;

    // --- Originator CRUD steps ---

    @When("Admin creates a new loan originator with external ID and name {string}")
    public void createOriginatorWithName(String name) {
        createOriginatorAndStore(name, null, TestContextKey.ORIGINATOR_CREATE_RESPONSE, TestContextKey.ORIGINATOR_EXTERNAL_ID);
    }

    @When("Admin creates a new loan originator with external ID, name {string} and status {string}")
    public void createOriginatorWithNameAndStatus(String name, String status) {
        createOriginatorAndStore(name, status, TestContextKey.ORIGINATOR_CREATE_RESPONSE, TestContextKey.ORIGINATOR_EXTERNAL_ID);
    }

    @When("Admin creates a new loan originator with all fields and name {string}")
    public void createOriginatorWithAllFields(String name) {
        GetLoanOriginatorTemplateResponse template = ok(() -> fineractClient.loanOriginators().retrieveLoanOriginatorTemplate());

        List<GetCodeValuesDataResponse> originatorTypes = template.getOriginatorTypeOptions();
        List<GetCodeValuesDataResponse> channelTypes = template.getChannelTypeOptions();
        assertThat(originatorTypes).as("Originator type options should not be empty").isNotNull().isNotEmpty();
        assertThat(channelTypes).as("Channel type options should not be empty").isNotNull().isNotEmpty();

        GetCodeValuesDataResponse originatorType = originatorTypes.get(0);
        GetCodeValuesDataResponse channelType = channelTypes.get(0);

        String externalId = UUID.randomUUID().toString();
        PostLoanOriginatorsRequest request = new PostLoanOriginatorsRequest().externalId(externalId).name(name)
                .originatorTypeId(originatorType.getId()).channelTypeId(channelType.getId());

        PostLoanOriginatorsResponse response = ok(() -> fineractClient.loanOriginators().create11(request));

        assertThat(response.getResourceId()).isNotNull();
        testContext().set(TestContextKey.ORIGINATOR_CREATE_RESPONSE, response);
        testContext().set(TestContextKey.ORIGINATOR_EXTERNAL_ID, externalId);
        testContext().set(TestContextKey.ORIGINATOR_TYPE_NAME, originatorType.getName());
        testContext().set(TestContextKey.ORIGINATOR_CHANNEL_TYPE_NAME, channelType.getName());
        log.info("Created originator with ID {}, externalId {}, originatorType {}, channelType {}", response.getResourceId(), externalId,
                originatorType.getName(), channelType.getName());
    }

    @When("Admin creates a second loan originator with external ID and name {string}")
    public void createSecondOriginatorWithName(String name) {
        createOriginatorAndStore(name, null, TestContextKey.ORIGINATOR_SECOND_CREATE_RESPONSE,
                TestContextKey.ORIGINATOR_SECOND_EXTERNAL_ID);
    }

    @Then("Loan originator is created successfully with status {string}")
    public void verifyOriginatorStatus(String expectedStatus) {
        PostLoanOriginatorsResponse createResponse = testContext().get(TestContextKey.ORIGINATOR_CREATE_RESPONSE);
        Long originatorId = createResponse.getResourceId();

        GetLoanOriginatorsResponse originator = ok(() -> fineractClient.loanOriginators().retrieveOne18(originatorId));

        assertThat(originator.getStatus()).isEqualTo(expectedStatus);
        assertThat(originator.getExternalId()).isEqualTo(testContext().get(TestContextKey.ORIGINATOR_EXTERNAL_ID));
    }

    @Then("Loan originator is created successfully with all fields populated")
    public void verifyOriginatorAllFields() {
        PostLoanOriginatorsResponse createResponse = testContext().get(TestContextKey.ORIGINATOR_CREATE_RESPONSE);
        Long originatorId = createResponse.getResourceId();
        String expectedExternalId = testContext().get(TestContextKey.ORIGINATOR_EXTERNAL_ID);
        String expectedOriginatorTypeName = testContext().get(TestContextKey.ORIGINATOR_TYPE_NAME);
        String expectedChannelTypeName = testContext().get(TestContextKey.ORIGINATOR_CHANNEL_TYPE_NAME);

        GetLoanOriginatorsResponse originator = ok(() -> fineractClient.loanOriginators().retrieveOne18(originatorId));

        assertThat(originator.getId()).as("Originator ID").isNotNull();
        assertThat(originator.getExternalId()).as("Originator externalId").isEqualTo(expectedExternalId);
        assertThat(originator.getStatus()).as("Originator status").isEqualTo("ACTIVE");
        assertThat(originator.getOriginatorType()).as("Originator type").isNotNull();
        assertThat(originator.getOriginatorType().getName()).as("Originator type name").isEqualTo(expectedOriginatorTypeName);
        assertThat(originator.getChannelType()).as("Channel type").isNotNull();
        assertThat(originator.getChannelType().getName()).as("Channel type name").isEqualTo(expectedChannelTypeName);
        log.info("Verified originator {} with all fields: type={}, channel={}", originatorId, expectedOriginatorTypeName,
                expectedChannelTypeName);
    }

    // --- Attach / Detach steps ---

    @When("Admin attaches the originator to the loan")
    public void attachOriginatorToLoan() {
        attachOriginatorInternal(TestContextKey.ORIGINATOR_CREATE_RESPONSE);
    }

    @When("Admin attaches the second originator to the loan")
    public void attachSecondOriginatorToLoan() {
        attachOriginatorInternal(TestContextKey.ORIGINATOR_SECOND_CREATE_RESPONSE);
    }

    @When("Admin detaches the originator from the loan")
    public void detachOriginatorFromLoan() {
        PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        PostLoanOriginatorsResponse originatorResponse = testContext().get(TestContextKey.ORIGINATOR_CREATE_RESPONSE);
        long loanId = loanResponse.getLoanId();
        long originatorId = originatorResponse.getResourceId();

        executeVoid(() -> fineractClient.loanOriginators().detachOriginatorFromLoan(loanId, originatorId));
        log.info("Detached originator {} from loan {}", originatorId, loanId);
    }

    // --- Loan details verification steps ---

    @Then("Loan details with association {string} has the originator attached")
    public void verifyOriginatorInLoanDetails(String association) {
        long loanId = getLoanId();
        String expectedExternalId = testContext().get(TestContextKey.ORIGINATOR_EXTERNAL_ID);

        List<GetLoansLoanIdOriginatorData> originators = retrieveLoanOriginators(loanId, association);
        assertThat(originators).as("Originators should not be null or empty").isNotNull().isNotEmpty();

        boolean found = originators.stream().anyMatch(o -> expectedExternalId.equals(o.getExternalId()));
        assertThat(found).as("Expected originator with externalId %s in loan details", expectedExternalId).isTrue();
    }

    @Then("Loan details with association {string} has the originator with all fields attached")
    public void verifyOriginatorWithAllFieldsInLoanDetails(String association) {
        PostLoanOriginatorsResponse createResponse = testContext().get(TestContextKey.ORIGINATOR_CREATE_RESPONSE);
        long loanId = getLoanId();
        long originatorId = createResponse.getResourceId();
        String expectedExternalId = testContext().get(TestContextKey.ORIGINATOR_EXTERNAL_ID);
        String expectedOriginatorTypeName = testContext().get(TestContextKey.ORIGINATOR_TYPE_NAME);
        String expectedChannelTypeName = testContext().get(TestContextKey.ORIGINATOR_CHANNEL_TYPE_NAME);

        List<GetLoansLoanIdOriginatorData> originators = retrieveLoanOriginators(loanId, association);
        assertThat(originators).as("Originators should not be null or empty").isNotNull().isNotEmpty();

        GetLoansLoanIdOriginatorData originator = originators.stream().filter(o -> expectedExternalId.equals(o.getExternalId())).findFirst()
                .orElseThrow(() -> new AssertionError("Originator with externalId " + expectedExternalId + " not found in loan details"));

        assertThat(originator.getId()).as("Originator ID in loan details").isNotNull();
        assertThat(originator.getName()).as("Originator name in loan details").isNotNull();
        assertThat(originator.getStatus()).as("Originator status in loan details").isEqualTo("ACTIVE");

        // Verify type fields via direct originator GET (loan details serializes CodeValueData as nested objects
        // which don't map to the flat fields in the generated client model)
        GetLoanOriginatorsResponse originatorDetails = ok(() -> fineractClient.loanOriginators().retrieveOne18(originatorId));
        assertThat(originatorDetails.getOriginatorType()).as("Originator type").isNotNull();
        assertThat(originatorDetails.getOriginatorType().getName()).as("Originator type name").isEqualTo(expectedOriginatorTypeName);
        assertThat(originatorDetails.getChannelType()).as("Channel type").isNotNull();
        assertThat(originatorDetails.getChannelType().getName()).as("Channel type name").isEqualTo(expectedChannelTypeName);
        log.info("Verified originator with all fields in loan {} details", loanId);
    }

    @Then("Loan details with association {string} has {int} originator(s) attached")
    public void verifyOriginatorCountInLoanDetails(String association, int expectedCount) {
        long loanId = getLoanId();

        List<GetLoansLoanIdOriginatorData> originators = retrieveLoanOriginators(loanId, association);
        int actualCount = (originators == null) ? 0 : originators.size();
        assertThat(actualCount).as("Number of originators in loan details").isEqualTo(expectedCount);
        log.info("Verified loan {} has {} originators attached", loanId, actualCount);
    }

    @Then("Loan details with association {string} has originator with name {string}")
    public void verifyOriginatorNameInLoanDetails(String association, String expectedName) {
        long loanId = getLoanId();

        List<GetLoansLoanIdOriginatorData> originators = retrieveLoanOriginators(loanId, association);
        assertThat(originators).as("Originators should not be null or empty").isNotNull().isNotEmpty();

        boolean found = originators.stream().anyMatch(o -> expectedName.equals(o.getName()));
        assertThat(found).as("Expected originator with name '%s' in loan details", expectedName).isTrue();
        log.info("Verified loan {} has originator with name '{}'", loanId, expectedName);
    }

    @Then("Loan details with association {string} has the second originator attached")
    public void verifySecondOriginatorInLoanDetails(String association) {
        long loanId = getLoanId();
        String expectedExternalId = testContext().get(TestContextKey.ORIGINATOR_SECOND_EXTERNAL_ID);

        List<GetLoansLoanIdOriginatorData> originators = retrieveLoanOriginators(loanId, association);
        assertThat(originators).as("Originators should not be null or empty").isNotNull().isNotEmpty();

        boolean found = originators.stream().anyMatch(o -> expectedExternalId.equals(o.getExternalId()));
        assertThat(found).as("Expected second originator with externalId %s in loan details", expectedExternalId).isTrue();
    }

    @Then("Loan details with association {string} has no originator attached")
    public void verifyNoOriginatorInLoanDetails(String association) {
        long loanId = getLoanId();

        List<GetLoansLoanIdOriginatorData> originators = retrieveLoanOriginators(loanId, association);
        assertThat(originators).as("Originators list should be empty after detach").isNullOrEmpty();
    }

    // --- Failure / validation steps ---

    @Then("Attaching the originator to the loan should fail")
    public void attachOriginatorShouldFail() {
        long loanId = getLoanId();
        long originatorId = getOriginatorId();

        failVoid(() -> fineractClient.loanOriginators().attachOriginatorToLoan(loanId, originatorId));
        log.info("Attach originator {} to loan {} failed as expected", originatorId, loanId);
    }

    @Then("Attaching the originator to the loan should fail with status {int}")
    public void attachOriginatorShouldFailWithStatus(int expectedStatus) {
        long loanId = getLoanId();
        long originatorId = getOriginatorId();

        CallFailedRuntimeException exception = failVoid(
                () -> fineractClient.loanOriginators().attachOriginatorToLoan(loanId, originatorId));
        assertExpectedStatus(exception, expectedStatus);
        log.info("Attach originator {} to loan {} failed with expected status {}", originatorId, loanId, expectedStatus);
    }

    @Then("Detaching the originator from the loan should fail with status {int}")
    public void detachOriginatorShouldFailWithStatus(int expectedStatus) {
        long loanId = getLoanId();
        long originatorId = getOriginatorId();

        CallFailedRuntimeException exception = failVoid(
                () -> fineractClient.loanOriginators().detachOriginatorFromLoan(loanId, originatorId));
        assertExpectedStatus(exception, expectedStatus);
        log.info("Detach originator {} from loan {} failed with expected status {}", originatorId, loanId, expectedStatus);
    }

    @Then("Attaching the second originator to the loan should fail with status {int}")
    public void attachSecondOriginatorShouldFailWithStatus(int expectedStatus) {
        long loanId = getLoanId();
        PostLoanOriginatorsResponse originatorResponse = testContext().get(TestContextKey.ORIGINATOR_SECOND_CREATE_RESPONSE);
        long originatorId = originatorResponse.getResourceId();

        CallFailedRuntimeException exception = failVoid(
                () -> fineractClient.loanOriginators().attachOriginatorToLoan(loanId, originatorId));
        assertExpectedStatus(exception, expectedStatus);
        log.info("Attach second originator {} to loan {} failed with expected status {}", originatorId, loanId, expectedStatus);
    }

    @Then("Attaching non-existent originator to the loan should fail with status {int}")
    public void attachNonExistentOriginatorShouldFail(int expectedStatus) {
        long loanId = getLoanId();

        CallFailedRuntimeException exception = failVoid(
                () -> fineractClient.loanOriginators().attachOriginatorToLoan(loanId, NON_EXISTENT_ID));
        assertExpectedStatus(exception, expectedStatus);
        log.info("Attach non-existent originator to loan {} failed with expected status {}", loanId, expectedStatus);
    }

    @Then("Attaching the originator to non-existent loan should fail with status {int}")
    public void attachOriginatorToNonExistentLoanShouldFail(int expectedStatus) {
        long originatorId = getOriginatorId();

        CallFailedRuntimeException exception = failVoid(
                () -> fineractClient.loanOriginators().attachOriginatorToLoan(NON_EXISTENT_ID, originatorId));
        assertExpectedStatus(exception, expectedStatus);
        log.info("Attach originator {} to non-existent loan failed with expected status {}", originatorId, expectedStatus);
    }

    @Then("Creating a loan originator without name should fail with status {int}")
    public void createOriginatorWithoutNameShouldFail(int expectedStatus) {
        PostLoanOriginatorsRequest request = new PostLoanOriginatorsRequest().externalId(UUID.randomUUID().toString());

        CallFailedRuntimeException exception = fail(() -> fineractClient.loanOriginators().create11(request));
        assertExpectedStatus(exception, expectedStatus);
        log.info("Create originator without name failed with expected status {}", expectedStatus);
    }

    @Then("Creating a loan originator without name succeeds")
    public void createOriginatorWithoutNameSucceeds() {
        PostLoanOriginatorsRequest request = new PostLoanOriginatorsRequest().externalId(UUID.randomUUID().toString());

        PostLoanOriginatorsResponse response = ok(() -> fineractClient.loanOriginators().create11(request));
        assertThat(response.getResourceId()).as("Originator created without name").isNotNull();
        log.info("Created originator without name, resourceId {}", response.getResourceId());
    }

    // --- Permission steps ---

    @Then("Created user without ATTACH_LOAN_ORIGINATOR permission fails to attach originator to the loan")
    public void userWithoutAttachPermissionFails() {
        long loanId = getLoanId();
        long originatorId = getOriginatorId();
        FineractFeignClient userClient = createClientForUser();

        CallFailedRuntimeException exception = failVoid(() -> userClient.loanOriginators().attachOriginatorToLoan(loanId, originatorId));
        assertExpectedStatus(exception, 403);
        log.info("User without ATTACH_LOAN_ORIGINATOR permission failed to attach originator {} to loan {} as expected", originatorId,
                loanId);
    }

    @Then("Created user without DETACH_LOAN_ORIGINATOR permission fails to detach originator from the loan")
    public void userWithoutDetachPermissionFails() {
        long loanId = getLoanId();
        long originatorId = getOriginatorId();
        FineractFeignClient userClient = createClientForUser();

        CallFailedRuntimeException exception = failVoid(() -> userClient.loanOriginators().detachOriginatorFromLoan(loanId, originatorId));
        assertExpectedStatus(exception, 403);
        log.info("User without DETACH_LOAN_ORIGINATOR permission failed to detach originator {} from loan {} as expected", originatorId,
                loanId);
    }

    @Then("Created user without CREATE_LOAN_ORIGINATOR permission fails to create an originator")
    public void userWithoutCreatePermissionFails() {
        FineractFeignClient userClient = createClientForUser();

        PostLoanOriginatorsRequest request = new PostLoanOriginatorsRequest().externalId(UUID.randomUUID().toString())
                .name("Should Fail Originator");

        CallFailedRuntimeException exception = fail(() -> userClient.loanOriginators().create11(request));
        assertExpectedStatus(exception, 403);
        log.info("User without CREATE_LOAN_ORIGINATOR permission failed to create originator as expected");
    }

    @Then("Created user without UPDATE_LOAN_ORIGINATOR permission fails to update the originator")
    public void userWithoutUpdatePermissionFails() {
        long originatorId = getOriginatorId();
        FineractFeignClient userClient = createClientForUser();

        PutLoanOriginatorsRequest updateRequest = new PutLoanOriginatorsRequest().name("Should Fail Update");

        CallFailedRuntimeException exception = fail(() -> userClient.loanOriginators().update16(originatorId, updateRequest));
        assertExpectedStatus(exception, 403);
        log.info("User without UPDATE_LOAN_ORIGINATOR permission failed to update originator {} as expected", originatorId);
    }

    @Then("Created user without DELETE_LOAN_ORIGINATOR permission fails to delete the originator")
    public void userWithoutDeletePermissionFails() {
        long originatorId = getOriginatorId();
        FineractFeignClient userClient = createClientForUser();

        CallFailedRuntimeException exception = fail(() -> userClient.loanOriginators().delete14(originatorId));
        assertExpectedStatus(exception, 403);
        log.info("User without DELETE_LOAN_ORIGINATOR permission failed to delete originator {} as expected", originatorId);
    }

    // --- Inline loan creation ---

    @When("Admin creates a new Loan with originator inline submitted on date: {string}")
    public void createLoanWithOriginatorInline(String submitDate) {
        PostClientsResponse clientResponse = testContext().get(TestContextKey.CLIENT_CREATE_RESPONSE);
        Long clientId = clientResponse.getClientId();

        String originatorExternalId = UUID.randomUUID().toString();
        PostLoansOriginatorData originatorData = new PostLoansOriginatorData().externalId(originatorExternalId)
                .name("Inline Created Originator");

        PostLoansRequest loansRequest = loanRequestFactory.defaultLoansRequest(clientId).submittedOnDate(submitDate)
                .expectedDisbursementDate(submitDate).addOriginatorsItem(originatorData);

        PostLoansResponse response = ok(() -> fineractClient.loans().calculateLoanScheduleOrSubmitLoanApplication(loansRequest, Map.of()));

        assertThat(response.getLoanId()).isNotNull();
        testContext().set(TestContextKey.LOAN_CREATE_RESPONSE, response);
        testContext().set(TestContextKey.ORIGINATOR_EXTERNAL_ID, originatorExternalId);
        log.info("Created loan {} with inline originator externalId {}", response.getLoanId(), originatorExternalId);
    }

    // --- Approve without event check + event verification ---

    @When("Admin approves the loan on {string} with {string} amount and expected disbursement date on {string}")
    public void approveLoanWithoutEventCheck(String approveDate, String approvedAmount, String expectedDisbursementDate) {
        long loanId = getLoanId();
        eventStore.reset();

        PostLoansLoanIdRequest approveRequest = LoanRequestFactory.defaultLoanApproveRequest().approvedOnDate(approveDate)
                .approvedLoanAmount(new BigDecimal(approvedAmount)).expectedDisbursementDate(expectedDisbursementDate);

        PostLoansLoanIdResponse loanApproveResponse = ok(
                () -> fineractClient.loans().stateTransitions(loanId, approveRequest, Map.of("command", "approve")));
        testContext().set(TestContextKey.LOAN_APPROVAL_RESPONSE, loanApproveResponse);
        log.info("Loan {} approved (event check skipped for separate verification)", loanId);
    }

    @Then("LoanApprovedBusinessEvent is created with originator details")
    public void verifyOriginatorInApprovalEvent() {
        long loanId = getLoanId();
        String expectedExternalId = testContext().get(TestContextKey.ORIGINATOR_EXTERNAL_ID);

        eventAssertion.assertEvent(LoanApprovedEvent.class, loanId).extractingData(loanAccountDataV1 -> {
            List<OriginatorDetailsV1> originators = loanAccountDataV1.getOriginators();
            assertThat(originators).as("Originators in LoanApprovedEvent should not be null or empty").isNotNull().isNotEmpty();
            assertThat(originators.get(0).getExternalId()).as("Originator externalId in LoanApprovedEvent").isEqualTo(expectedExternalId);
            assertThat(originators.get(0).getStatus()).as("Originator status in LoanApprovedEvent").isEqualTo("ACTIVE");
            return loanAccountDataV1.getId();
        }).isEqualTo(loanId);
        log.info("Verified originator {} in LoanApprovedEvent for loan {}", expectedExternalId, loanId);
    }

    // --- Organization-level CRUD steps ---

    @Then("Loan originator is retrieved successfully by external ID with all fields")
    public void retrieveOriginatorByExternalIdWithAllFields() {
        String expectedExternalId = testContext().get(TestContextKey.ORIGINATOR_EXTERNAL_ID);
        String expectedOriginatorTypeName = testContext().get(TestContextKey.ORIGINATOR_TYPE_NAME);
        String expectedChannelTypeName = testContext().get(TestContextKey.ORIGINATOR_CHANNEL_TYPE_NAME);

        GetLoanOriginatorsResponse originator = ok(() -> fineractClient.loanOriginators().retrieveByExternalId(expectedExternalId));

        assertThat(originator.getId()).as("Originator ID").isNotNull();
        assertThat(originator.getExternalId()).as("Originator externalId").isEqualTo(expectedExternalId);
        assertThat(originator.getName()).as("Originator name").isNotNull();
        assertThat(originator.getStatus()).as("Originator status").isEqualTo("ACTIVE");
        assertThat(originator.getOriginatorType()).as("Originator type").isNotNull();
        assertThat(originator.getOriginatorType().getName()).as("Originator type name").isEqualTo(expectedOriginatorTypeName);
        assertThat(originator.getChannelType()).as("Channel type").isNotNull();
        assertThat(originator.getChannelType().getName()).as("Channel type name").isEqualTo(expectedChannelTypeName);
        log.info("Retrieved originator by externalId {} with all fields", expectedExternalId);
    }

    @Then("Loan originator list contains the created originator")
    public void verifyOriginatorInList() {
        long expectedId = getOriginatorId();

        List<GetLoanOriginatorsResponse> allOriginators = ok(() -> fineractClient.loanOriginators().retrieveAll28());

        assertThat(allOriginators).as("Originator list should not be null or empty").isNotNull().isNotEmpty();

        boolean found = allOriginators.stream().anyMatch(o -> o.getId() != null && o.getId() == expectedId);
        assertThat(found).as("Expected originator with ID %d in list", expectedId).isTrue();
        log.info("Verified originator {} is present in the list of {} originators", expectedId, allOriginators.size());
    }

    @Then("Loan originator template contains status options, originator type options and channel type options")
    public void verifyOriginatorTemplate() {
        GetLoanOriginatorTemplateResponse template = ok(() -> fineractClient.loanOriginators().retrieveLoanOriginatorTemplate());

        assertThat(template.getStatusOptions()).as("Status options").isNotNull().isNotEmpty();
        assertThat(template.getOriginatorTypeOptions()).as("Originator type options").isNotNull().isNotEmpty();
        assertThat(template.getChannelTypeOptions()).as("Channel type options").isNotNull().isNotEmpty();
        assertThat(template.getExternalId()).as("Template generated externalId").isNotNull().isNotEmpty();

        log.info("Verified template: {} status options, {} originator types, {} channel types", template.getStatusOptions().size(),
                template.getOriginatorTypeOptions().size(), template.getChannelTypeOptions().size());
    }

    @When("Admin updates the originator name to {string} and status to {string}")
    public void updateOriginatorById(String newName, String newStatus) {
        long originatorId = getOriginatorId();

        PutLoanOriginatorsRequest updateRequest = new PutLoanOriginatorsRequest().name(newName).status(newStatus);

        PutLoanOriginatorsResponse updateResponse = ok(() -> fineractClient.loanOriginators().update16(originatorId, updateRequest));

        assertThat(updateResponse.getResourceId()).as("Updated originator resource ID").isEqualTo(originatorId);
        log.info("Updated originator {} with name={}, status={}", originatorId, newName, newStatus);
    }

    @When("Admin updates the originator by external ID with name {string}")
    public void updateOriginatorByExternalId(String newName) {
        String externalId = testContext().get(TestContextKey.ORIGINATOR_EXTERNAL_ID);

        PutLoanOriginatorsRequest updateRequest = new PutLoanOriginatorsRequest().name(newName);

        PutLoanOriginatorsResponse updateResponse = ok(
                () -> fineractClient.loanOriginators().updateByExternalId(externalId, updateRequest));

        assertThat(updateResponse.getResourceId()).as("Updated originator resource ID").isNotNull();
        log.info("Updated originator by externalId {} with name={}", externalId, newName);
    }

    @Then("Loan originator has name {string} and status {string}")
    public void verifyOriginatorNameAndStatus(String expectedName, String expectedStatus) {
        long originatorId = getOriginatorId();

        GetLoanOriginatorsResponse originator = ok(() -> fineractClient.loanOriginators().retrieveOne18(originatorId));

        assertThat(originator.getName()).as("Originator name").isEqualTo(expectedName);
        assertThat(originator.getStatus()).as("Originator status").isEqualTo(expectedStatus);
        log.info("Verified originator {} has name={}, status={}", originatorId, expectedName, expectedStatus);
    }

    @Then("Loan originator retrieved by external ID has name {string}")
    public void verifyOriginatorByExternalIdHasName(String expectedName) {
        String externalId = testContext().get(TestContextKey.ORIGINATOR_EXTERNAL_ID);

        GetLoanOriginatorsResponse originator = ok(() -> fineractClient.loanOriginators().retrieveByExternalId(externalId));

        assertThat(originator.getName()).as("Originator name").isEqualTo(expectedName);
        log.info("Verified originator with externalId {} has name={}", externalId, expectedName);
    }

    @When("Admin deletes the originator by ID")
    public void deleteOriginatorById() {
        long originatorId = getOriginatorId();

        DeleteLoanOriginatorsResponse deleteResponse = ok(() -> fineractClient.loanOriginators().delete14(originatorId));

        assertThat(deleteResponse.getResourceId()).as("Deleted originator resource ID").isEqualTo(originatorId);
        log.info("Deleted originator by ID {}", originatorId);
    }

    @When("Admin deletes the originator by external ID")
    public void deleteOriginatorByExternalId() {
        String externalId = testContext().get(TestContextKey.ORIGINATOR_EXTERNAL_ID);

        DeleteLoanOriginatorsResponse deleteResponse = ok(() -> fineractClient.loanOriginators().deleteByExternalId(externalId));

        assertThat(deleteResponse.getResourceId()).as("Deleted originator resource ID").isNotNull();
        log.info("Deleted originator by externalId {}", externalId);
    }

    @Then("Retrieving the deleted originator by ID should fail with status {int}")
    public void retrieveDeletedOriginatorByIdShouldFail(int expectedStatus) {
        long originatorId = getOriginatorId();

        CallFailedRuntimeException exception = fail(() -> fineractClient.loanOriginators().retrieveOne18(originatorId));
        assertExpectedStatus(exception, expectedStatus);
        log.info("Retrieving deleted originator {} failed with expected status {}", originatorId, expectedStatus);
    }

    @Then("Retrieving the deleted originator by external ID should fail with status {int}")
    public void retrieveDeletedOriginatorByExternalIdShouldFail(int expectedStatus) {
        String externalId = testContext().get(TestContextKey.ORIGINATOR_EXTERNAL_ID);

        CallFailedRuntimeException exception = fail(() -> fineractClient.loanOriginators().retrieveByExternalId(externalId));
        assertExpectedStatus(exception, expectedStatus);
        log.info("Retrieving deleted originator by externalId {} failed with expected status {}", externalId, expectedStatus);
    }

    @Then("Deleting the originator should fail with status {int}")
    public void deleteOriginatorShouldFailWithStatus(int expectedStatus) {
        long originatorId = getOriginatorId();

        CallFailedRuntimeException exception = fail(() -> fineractClient.loanOriginators().delete14(originatorId));
        assertExpectedStatus(exception, expectedStatus);
        log.info("Deleting originator {} failed with expected status {}", originatorId, expectedStatus);
    }

    // --- Helper methods ---

    private long getLoanId() {
        PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        return loanResponse.getLoanId();
    }

    private long getOriginatorId() {
        PostLoanOriginatorsResponse response = testContext().get(TestContextKey.ORIGINATOR_CREATE_RESPONSE);
        return response.getResourceId();
    }

    private void createOriginatorAndStore(String name, String status, String responseKey, String externalIdKey) {
        String externalId = UUID.randomUUID().toString();
        PostLoanOriginatorsRequest request = new PostLoanOriginatorsRequest().externalId(externalId).name(name);
        if (status != null) {
            request.status(status);
        }

        PostLoanOriginatorsResponse response = ok(() -> fineractClient.loanOriginators().create11(request));

        assertThat(response.getResourceId()).isNotNull();
        testContext().set(responseKey, response);
        testContext().set(externalIdKey, externalId);
        log.info("Created originator with ID {} and externalId {}", response.getResourceId(), externalId);
    }

    private void attachOriginatorInternal(String originatorContextKey) {
        PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        PostLoanOriginatorsResponse originatorResponse = testContext().get(originatorContextKey);
        long loanId = loanResponse.getLoanId();
        long originatorId = originatorResponse.getResourceId();

        executeVoid(() -> fineractClient.loanOriginators().attachOriginatorToLoan(loanId, originatorId));
        log.info("Attached originator {} to loan {}", originatorId, loanId);
    }

    private List<GetLoansLoanIdOriginatorData> retrieveLoanOriginators(long loanId, String association) {
        GetLoansLoanIdResponse loanDetails = ok(() -> fineractClient.loans().retrieveLoan(loanId,
                Map.of("staffInSelectedOfficeOnly", false, "associations", association, "exclude", "", "fields", "")));
        return loanDetails.getOriginators();
    }

    private void assertExpectedStatus(CallFailedRuntimeException exception, int expectedStatus) {
        assertThat(exception.getStatus()).as(ErrorMessageHelper.wrongErrorCode(exception.getStatus(), expectedStatus))
                .isEqualTo(expectedStatus);
    }

    private FineractFeignClient createClientForUser() {
        String username = testContext().get(TestContextKey.CREATED_SIMPLE_USER_USERNAME);
        String password = testContext().get(TestContextKey.CREATED_SIMPLE_USER_PASSWORD);
        String apiBaseUrl = apiProperties.getBaseUrl() + "/fineract-provider/api/";

        return FineractFeignClient.builder().baseUrl(apiBaseUrl).credentials(username, password).tenantId(apiProperties.getTenantId())
                .disableSslVerification(true).connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout((int) apiProperties.getReadTimeout(), TimeUnit.SECONDS).build();
    }
}
