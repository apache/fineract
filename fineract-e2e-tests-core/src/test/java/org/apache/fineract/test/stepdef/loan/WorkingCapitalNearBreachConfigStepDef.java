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

import static org.apache.fineract.client.feign.util.FeignCalls.fail;
import static org.apache.fineract.client.feign.util.FeignCalls.ok;
import static org.assertj.core.api.Assertions.assertThat;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.models.CommandProcessingResult;
import org.apache.fineract.client.models.WorkingCapitalNearBreachData;
import org.apache.fineract.client.models.WorkingCapitalNearBreachRequest;
import org.apache.fineract.test.data.workingcapitalproduct.WorkingCapitalBreachFrequencyType;
import org.apache.fineract.test.factory.WorkingCapitalRequestFactory;
import org.apache.fineract.test.helper.ErrorMessageHelper;
import org.apache.fineract.test.helper.Utils;
import org.apache.fineract.test.stepdef.AbstractStepDef;
import org.apache.fineract.test.support.TestContext;
import org.apache.fineract.test.support.TestContextKey;
import org.assertj.core.api.SoftAssertions;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
@RequiredArgsConstructor
public class WorkingCapitalNearBreachConfigStepDef extends AbstractStepDef {

    @Autowired
    private WorkingCapitalRequestFactory workingCapitalRequestFactory;

    private final FineractFeignClient fineractFeignClient;

    @When("Admin creates WC Near Breach With Values")
    public void adminCreatesWCNearBreachWithValues() {
        final WorkingCapitalNearBreachRequest request = workingCapitalRequestFactory.defaultWorkingCapitalNearBreachRequest();
        final CommandProcessingResult response = ok(
                () -> fineractFeignClient.workingCapitalNearBreaches().createWorkingCapitalNearBreach(request));

        assertThat(response).isNotNull();
        assertThat(response.getResourceId()).isNotNull();
        TestContext.INSTANCE.set(TestContextKey.WORKING_CAPITAL_NEAR_BREACH_ID, response.getResourceId());
        TestContext.INSTANCE.set(TestContextKey.WORKING_CAPITAL_NEAR_BREACH_CREATE_REQUEST, request);
    }

    @Then("Check created Near Breach has the following values")
    public void checkCreatedNearBreachHasTheFollowingValues() {
        final Long id = TestContext.INSTANCE.get(TestContextKey.WORKING_CAPITAL_NEAR_BREACH_ID);
        final WorkingCapitalNearBreachData data = ok(
                () -> fineractFeignClient.workingCapitalNearBreaches().retrieveWorkingCapitalNearBreach(id));
        final WorkingCapitalNearBreachRequest request = TestContext.INSTANCE.get(TestContextKey.WORKING_CAPITAL_NEAR_BREACH_CREATE_REQUEST);
        checkNearBreachData(request, data);
    }

    @Then("Admin failed to create a new WC Near Breach for field {string} with invalid data {string} results with an error {}")
    public void createWCBreachWithInvalidDataFailed(final String fieldName, final String value, final String errorMessage) {
        final WorkingCapitalNearBreachRequest request = setWCNearBreachFieldValue(
                workingCapitalRequestFactory.defaultWorkingCapitalNearBreachRequest(), fieldName, value);
        checkCreateWCNearBreachWithInvalidDataFailure(request, errorMessage, 400);
    }

    @When("Admin creates WC Near Breach With Values for update")
    public void adminCreatesWCNearBreachWithValuesForUpdate() {
        final WorkingCapitalNearBreachRequest request = workingCapitalRequestFactory.defaultWorkingCapitalNearBreachRequest()
                .nearBreachFrequency(4) //
                .nearBreachFrequencyType(WorkingCapitalBreachFrequencyType.YEARS.getCode()) //
                .nearBreachThreshold(new BigDecimal("82.34")); //
        final CommandProcessingResult response = ok(
                () -> fineractFeignClient.workingCapitalNearBreaches().createWorkingCapitalNearBreach(request));
        assertThat(response).isNotNull();
        assertThat(response.getResourceId()).isNotNull();
        TestContext.INSTANCE.set(TestContextKey.WORKING_CAPITAL_NEAR_BREACH_ID_FOR_UPDATE, response.getResourceId());
        TestContext.INSTANCE.set(TestContextKey.WORKING_CAPITAL_NEAR_BREACH_CREATE_REQUEST_FOR_UPDATE, request);
    }

    @When("Admin failed to create WC Near Breach With duplicated name")
    public void adminCreateWCNearBreachWithDuplicateNameFailure() {
        Long existingBearBreachId = TestContext.INSTANCE.get(TestContextKey.WORKING_CAPITAL_NEAR_BREACH_ID_FOR_UPDATE);
        WorkingCapitalNearBreachRequest nearBreachRequestForUpdate = TestContext.INSTANCE
                .get(TestContextKey.WORKING_CAPITAL_NEAR_BREACH_CREATE_REQUEST_FOR_UPDATE);
        String name = nearBreachRequestForUpdate.getNearBreachName();
        WorkingCapitalNearBreachRequest breachRequest = workingCapitalRequestFactory.defaultWorkingCapitalNearBreachRequest()
                .nearBreachName(name); //
        String errorMessage = ErrorMessageHelper.workingCapitalBreachDuplicateNameFailure(existingBearBreachId);
        checkCreateWCNearBreachWithInvalidDataFailure(breachRequest, errorMessage, 403);
    }

    @When("Admin modifies WC Near Breach With Values")
    public void adminModifiesWCBreachWithValues() {
        final Long id = TestContext.INSTANCE.get(TestContextKey.WORKING_CAPITAL_NEAR_BREACH_ID);
        final WorkingCapitalNearBreachRequest request = workingCapitalRequestFactory.defaultWorkingCapitalNearBreachRequest()
                .nearBreachName("NearBreach-WCL-" + Utils.randomStringGenerator(8)) //
                .nearBreachFrequency(4) //
                .nearBreachFrequencyType("MONTHS") //
                .nearBreachThreshold(new BigDecimal("59.8")); //
        ok(() -> fineractFeignClient.workingCapitalNearBreaches().updateWorkingCapitalNearBreach(id, request));
        TestContext.INSTANCE.set(TestContextKey.WORKING_CAPITAL_NEAR_BREACH_UPDATE_REQUEST, request);
    }

    @Then("Check updated Near Breach has the following values")
    public void checkUpdatedBreachHasTheFollowingValues() {
        final Long id = TestContext.INSTANCE.get(TestContextKey.WORKING_CAPITAL_NEAR_BREACH_ID);
        final WorkingCapitalNearBreachData data = ok(
                () -> fineractFeignClient.workingCapitalNearBreaches().retrieveWorkingCapitalNearBreach(id));
        final WorkingCapitalNearBreachRequest request = TestContext.INSTANCE.get(TestContextKey.WORKING_CAPITAL_NEAR_BREACH_UPDATE_REQUEST);
        checkNearBreachData(request, data);
    }

    @Then("Admin failed to update WC Near Breach for field {string} with invalid data {string} results with an error {}")
    public void updateWCBreachWithInvalidDataFailed(final String fieldName, final String value, final String errorMessage) {
        Long id = TestContext.INSTANCE.get(TestContextKey.WORKING_CAPITAL_NEAR_BREACH_ID_FOR_UPDATE);
        if (id == null) {
            adminCreatesWCNearBreachWithValuesForUpdate();
            id = TestContext.INSTANCE.get(TestContextKey.WORKING_CAPITAL_NEAR_BREACH_ID_FOR_UPDATE);
        }
        final WorkingCapitalNearBreachRequest request = setWCNearBreachFieldValue(
                workingCapitalRequestFactory.defaultWorkingCapitalNearBreachRequest(), fieldName, value);
        checkUpdateWCNearBreachWithInvalidDataFailure(id, request, errorMessage, 400);
    }

    @When("Admin failed to update WC Near Breach With duplicated name")
    public void adminUpdateWCBreachWithDuplicateNameFailure() {
        Long nearBreachIdForUpdate = TestContext.INSTANCE.get(TestContextKey.WORKING_CAPITAL_NEAR_BREACH_ID_FOR_UPDATE);
        Long existingNearBreachId = TestContext.INSTANCE.get(TestContextKey.WORKING_CAPITAL_NEAR_BREACH_ID);
        WorkingCapitalNearBreachRequest nearBreachRequestForUpdate = TestContext.INSTANCE
                .get(TestContextKey.WORKING_CAPITAL_NEAR_BREACH_CREATE_REQUEST);
        String name = nearBreachRequestForUpdate.getNearBreachName();
        WorkingCapitalNearBreachRequest nearBreachRequest = workingCapitalRequestFactory.defaultWorkingCapitalNearBreachRequest() //
                .nearBreachName(name); //
        String errorMessage = ErrorMessageHelper.workingCapitalBreachDuplicateNameFailure(existingNearBreachId);
        checkUpdateWCNearBreachWithInvalidDataFailure(nearBreachIdForUpdate, nearBreachRequest, errorMessage, 403);
    }

    @When("Admin deletes WC Near Breach With Values")
    public void adminDeletesWCNearBreachWithValues() {
        final Long id = TestContext.INSTANCE.get(TestContextKey.WORKING_CAPITAL_NEAR_BREACH_ID);
        ok(() -> fineractFeignClient.workingCapitalNearBreaches().deleteWorkingCapitalNearBreach(id));
    }

    @When("Admin deletes WC Near Breach override")
    public void adminDeletesWCNearBreachOverride() {
        final Long id = TestContext.INSTANCE.get(TestContextKey.WORKING_CAPITAL_NEAR_BREACH_ID_OVERRIDE);
        ok(() -> fineractFeignClient.workingCapitalNearBreaches().deleteWorkingCapitalNearBreach(id));
    }

    @When("Admin deletes WC Near Breach With Values for update")
    public void adminDeletesWCBreachWithValuesForUpdate() {
        final Long id = TestContext.INSTANCE.get(TestContextKey.WORKING_CAPITAL_NEAR_BREACH_ID_FOR_UPDATE);
        ok(() -> fineractFeignClient.workingCapitalNearBreaches().deleteWorkingCapitalNearBreach(id));
    }

    @Then("Admin failed to delete WC Near Breach that is already deleted")
    public void adminDeleteWCBreachAlreadyDeletedFailure() {
        final Long id = TestContext.INSTANCE.get(TestContextKey.WORKING_CAPITAL_NEAR_BREACH_ID_FOR_UPDATE);
        checkDeleteWCNearBreachNotFoundFailure(id);
    }

    @Then("Admin failed to retrieve WC Near Breach that is already deleted")
    public void adminRetrieveWCNearBreachAlreadyDeletedFailure() {
        final Long id = TestContext.INSTANCE.get(TestContextKey.WORKING_CAPITAL_NEAR_BREACH_ID_FOR_UPDATE);
        checkRetrieveWCNearBreachNotFoundFailure(id);
    }

    @Then("Admin failed to delete WC Near Breach with id {int} that doesn't exist")
    public void adminDeleteWCNearBreachWithIncorrectIdFailure(final Integer id) {
        checkDeleteWCNearBreachNotFoundFailure(Long.valueOf(id));
    }

    @Then("Admin failed to retrieve WC Near Breach with id {int} that is not found")
    public void adminRetrieveWCNearBreachWithIncorrectIdFailure(final Integer id) {
        checkRetrieveWCNearBreachNotFoundFailure(Long.valueOf(id));
    }

    @Then("Admin failed to delete WC Near Breach that is still assigned to WC loan product")
    public void adminDeleteWCBreachAssignedToWCLPFailure() {
        final Long id = TestContext.INSTANCE.get(TestContextKey.WORKING_CAPITAL_NEAR_BREACH_ID);
        String errorMessage = "The request caused a data integrity issue to be fired by the database.";
        // checkDeleteWCNearBreachFailure(id, 403, errorMessage);
        final CallFailedRuntimeException exception = fail(
                () -> fineractFeignClient.workingCapitalNearBreaches().deleteWorkingCapitalNearBreach(id));
        assertThat(exception.getStatus()).isEqualTo(403);
        // assertThat(exception.getDeveloperMessage()).contains(errorMessage);
        assertThat(exception.getMessage()).contains(errorMessage);
    }

    @Then("Admin failed to delete WC Near Breach that is still assigned to WC loan account")
    public void adminDeleteWCBreachAssignedToWCLAccountFailure() {
        final Long id = TestContext.INSTANCE.get(TestContextKey.WORKING_CAPITAL_NEAR_BREACH_ID);
        String errorMessage = "The request caused a data integrity issue to be fired by the database.";
        // checkDeleteWCNearBreachFailure(id, 403, errorMessage);
        final CallFailedRuntimeException exception = fail(
                () -> fineractFeignClient.workingCapitalNearBreaches().deleteWorkingCapitalNearBreach(id));
        assertThat(exception.getStatus()).isEqualTo(403);
        // assertThat(exception.getDeveloperMessage()).contains(errorMessage);
        assertThat(exception.getMessage()).contains(errorMessage);
    }

    private void checkNearBreachData(final WorkingCapitalNearBreachRequest request, final WorkingCapitalNearBreachData response) {
        final SoftAssertions assertions = new SoftAssertions();
        assertions.assertThat(response.getName()).isEqualTo(request.getNearBreachName());
        assert response.getFrequency() != null;
        assert response.getFrequencyType() != null;
        assertions.assertThat(response.getFrequency()).isEqualTo(request.getNearBreachFrequency());
        assertions.assertThat(response.getFrequencyType().getId()).isEqualTo(request.getNearBreachFrequencyType());
        assert response.getThreshold() != null;
        assertions.assertThat(response.getThreshold().compareTo(request.getNearBreachThreshold())).isEqualTo(0);
        assertions.assertAll();
    }

    private WorkingCapitalNearBreachRequest setWCNearBreachFieldValue(final WorkingCapitalNearBreachRequest request, final String fieldName,
            String fieldValue) {
        if ("null".equals(fieldValue)) {
            fieldValue = null;
        }
        final Integer intValue = fieldValue != null && "nearBreachFrequency".equals(fieldName) ? Integer.valueOf(fieldValue) : null;
        final BigDecimal decimalValue = fieldValue != null && "nearBreachThreshold".equals(fieldName) ? new BigDecimal(fieldValue) : null;
        switch (fieldName) {
            case "nearBreachName":
                request.setNearBreachName(fieldValue);
            break;
            case "nearBreachFrequency":
                request.setNearBreachFrequency(intValue);
            break;
            case "nearBreachFrequencyType":
                request.setNearBreachFrequencyType(fieldValue);
            break;
            case "nearBreachThreshold":
                request.setNearBreachThreshold(decimalValue);
            break;
            default:
            break;
        }
        return request;
    }

    private void checkCreateWCNearBreachWithInvalidDataFailure(final WorkingCapitalNearBreachRequest request, final String errorMessage,
            final int errorCode) {
        final CallFailedRuntimeException exception = fail(
                () -> fineractFeignClient.workingCapitalNearBreaches().createWorkingCapitalNearBreach(request));
        assertThat(exception.getStatus()).as(ErrorMessageHelper.incorrectExpectedValueInResponse()).isEqualTo(errorCode);
        assertThat(exception.getDeveloperMessage()).contains(errorMessage);
    }

    private void checkUpdateWCNearBreachWithInvalidDataFailure(final Long id, final WorkingCapitalNearBreachRequest request,
            final String errorMessage, final int errorCode) {
        final CallFailedRuntimeException exception = fail(
                () -> fineractFeignClient.workingCapitalNearBreaches().updateWorkingCapitalNearBreach(id, request));
        assertThat(exception.getStatus()).as(ErrorMessageHelper.incorrectExpectedValueInResponse()).isEqualTo(errorCode);
        assertThat(exception.getDeveloperMessage()).contains(errorMessage);
    }

    private void checkDeleteWCNearBreachNotFoundFailure(final Long id) {
        /*
         * final CallFailedRuntimeException exception = fail( () ->
         * fineractFeignClient.workingCapitalNearBreaches().deleteWorkingCapitalNearBreach(id));
         * assertThat(exception.getStatus()).isEqualTo(404);
         * assertThat(exception.getDeveloperMessage()).contains(ErrorMessageHelper.
         * workingCapitalNearBreachNotFoundFailure(id));
         */
        String errorMessage = ErrorMessageHelper.workingCapitalNearBreachNotFoundFailure(id);
        checkDeleteWCNearBreachFailure(id, 404, errorMessage);
    }

    private void checkDeleteWCNearBreachFailure(final Long id, int statusCode, String errorMessage) {
        final CallFailedRuntimeException exception = fail(
                () -> fineractFeignClient.workingCapitalNearBreaches().deleteWorkingCapitalNearBreach(id));
        assertThat(exception.getStatus()).isEqualTo(statusCode);
        assertThat(exception.getDeveloperMessage()).contains(errorMessage);
        // assertThat(exception.getMessage()).contains(errorMessage);
    }

    /*
     * private void checkDeleteWCNearBreachFailure(final Long id, int statusCode, String errorMessage) { final
     * CallFailedRuntimeException exception = fail( () ->
     * fineractFeignClient.workingCapitalNearBreaches().deleteWorkingCapitalNearBreach(id));
     * assertThat(exception.getStatus()).isEqualTo(statusCode); //
     * assertThat(exception.getDeveloperMessage()).contains(errorMessage);
     * assertThat(exception.getMessage()).contains(errorMessage); }
     */

    private void checkRetrieveWCNearBreachNotFoundFailure(final Long id) {
        final CallFailedRuntimeException exception = fail(
                () -> fineractFeignClient.workingCapitalNearBreaches().retrieveWorkingCapitalNearBreach(id));
        assertThat(exception.getStatus()).isEqualTo(404);
        assertThat(exception.getDeveloperMessage()).contains(ErrorMessageHelper.workingCapitalNearBreachNotFoundFailure(id));
    }

}
