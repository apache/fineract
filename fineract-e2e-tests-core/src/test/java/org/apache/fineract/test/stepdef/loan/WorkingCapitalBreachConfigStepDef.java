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
import org.apache.fineract.client.models.StringEnumOptionData;
import org.apache.fineract.client.models.WorkingCapitalBreachData;
import org.apache.fineract.client.models.WorkingCapitalBreachRequest;
import org.apache.fineract.client.models.WorkingCapitalBreachTemplateResponse;
import org.apache.fineract.test.factory.WorkingCapitalRequestFactory;
import org.apache.fineract.test.helper.ErrorMessageHelper;
import org.apache.fineract.test.stepdef.AbstractStepDef;
import org.apache.fineract.test.support.TestContext;
import org.apache.fineract.test.support.TestContextKey;
import org.assertj.core.api.SoftAssertions;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
@RequiredArgsConstructor
public class WorkingCapitalBreachConfigStepDef extends AbstractStepDef {

    @Autowired
    private WorkingCapitalRequestFactory workingCapitalRequestFactory;

    private final FineractFeignClient fineractFeignClient;

    @When("Admin Calls Breach Template")
    public void adminCallsBreachTemplate() {
        final WorkingCapitalBreachTemplateResponse template = ok(
                () -> fineractFeignClient.workingCapitalBreaches().retrieveWorkingCapitalBreachTemplate());
        assertThat(template).isNotNull();
        assertThat(template.getBreachFrequencyTypeOptions()).isNotNull().isNotEmpty();
        assertThat(template.getBreachAmountCalculationTypeOptions()).isNotNull().isNotEmpty();
        log.info("Template WorkingCapitalBreach: {}", template);
    }

    @When("Admin creates WC Breach With Values")
    public void adminCreatesWCBreachWithValues() {
        final WorkingCapitalBreachRequest request = workingCapitalRequestFactory.defaultWorkingCapitalBreachRequest();
        final CommandProcessingResult response = ok(() -> fineractFeignClient.workingCapitalBreaches().createWorkingCapitalBreach(request));
        assertThat(response).isNotNull();
        assertThat(response.getResourceId()).isNotNull();
        TestContext.GLOBAL.set(TestContextKey.WORKING_CAPITAL_BREACH_ID, response.getResourceId());
        TestContext.GLOBAL.set(TestContextKey.WORKING_CAPITAL_BREACH_CREATE_REQUEST, request);
    }

    @When("Admin creates WC Breach With Values for update")
    public void adminCreatesWCBreachWithValuesForUpdate() {
        final WorkingCapitalBreachRequest request = workingCapitalRequestFactory.defaultWorkingCapitalBreachRequest()
                .breachAmount(new BigDecimal("2.34"));
        final CommandProcessingResult response = ok(() -> fineractFeignClient.workingCapitalBreaches().createWorkingCapitalBreach(request));
        assertThat(response).isNotNull();
        assertThat(response.getResourceId()).isNotNull();
        TestContext.GLOBAL.set(TestContextKey.WORKING_CAPITAL_BREACH_ID_FOR_UPDATE, response.getResourceId());
        TestContext.GLOBAL.set(TestContextKey.WORKING_CAPITAL_BREACH_CREATE_REQUEST_FOR_UPDATE, request);
    }

    @Then("Check created Breach has the following values")
    public void checkCreatedBreachHasTheFollowingValues() {
        final Long id = TestContext.GLOBAL.get(TestContextKey.WORKING_CAPITAL_BREACH_ID);
        final WorkingCapitalBreachData data = ok(() -> fineractFeignClient.workingCapitalBreaches().retrieveWorkingCapitalBreach(id));
        final WorkingCapitalBreachRequest request = TestContext.GLOBAL.get(TestContextKey.WORKING_CAPITAL_BREACH_CREATE_REQUEST);
        checkBreachData(request, data);
    }

    @Then("Get Breach With Template has the following values")
    public void getBreachWithTemplateHasTheFollowingValues() {
        final Long id = TestContext.GLOBAL.get(TestContextKey.WORKING_CAPITAL_BREACH_ID);
        final WorkingCapitalBreachData data = ok(() -> fineractFeignClient.workingCapitalBreaches().retrieveWorkingCapitalBreach(id));
        final WorkingCapitalBreachTemplateResponse template = ok(
                () -> fineractFeignClient.workingCapitalBreaches().retrieveWorkingCapitalBreachTemplate());
        assertThat(data).isNotNull();
        assertThat(template).isNotNull();
        assertThat(template.getBreachFrequencyTypeOptions()).isNotNull().isNotEmpty();
        assertThat(template.getBreachAmountCalculationTypeOptions()).isNotNull().isNotEmpty();
    }

    @When("Admin modifies WC Breach With Values")
    public void adminModifiesWCBreachWithValues() {
        final Long id = TestContext.GLOBAL.get(TestContextKey.WORKING_CAPITAL_BREACH_ID);
        final WorkingCapitalBreachRequest request = workingCapitalRequestFactory.defaultWorkingCapitalBreachRequest() //
                .breachFrequency(4) //
                .breachFrequencyType("MONTHS") //
                .breachAmountCalculationType("FLAT") //
                .breachAmount(new BigDecimal("7.89"));
        ok(() -> fineractFeignClient.workingCapitalBreaches().updateWorkingCapitalBreach(id, request));
        TestContext.GLOBAL.set(TestContextKey.WORKING_CAPITAL_BREACH_UPDATE_REQUEST, request);
    }

    @Then("Check updated Breach has the following values")
    public void checkUpdatedBreachHasTheFollowingValues() {
        final Long id = TestContext.GLOBAL.get(TestContextKey.WORKING_CAPITAL_BREACH_ID);
        final WorkingCapitalBreachData data = ok(() -> fineractFeignClient.workingCapitalBreaches().retrieveWorkingCapitalBreach(id));
        final WorkingCapitalBreachRequest request = TestContext.GLOBAL.get(TestContextKey.WORKING_CAPITAL_BREACH_UPDATE_REQUEST);
        checkBreachData(request, data);
    }

    @When("Admin deletes WC Breach With Values")
    public void adminDeletesWCBreachWithValues() {
        final Long id = TestContext.GLOBAL.get(TestContextKey.WORKING_CAPITAL_BREACH_ID);
        ok(() -> fineractFeignClient.workingCapitalBreaches().deleteWorkingCapitalBreach(id));
    }

    @When("Admin deletes WC Breach With Values for update")
    public void adminDeletesWCBreachWithValuesForUpdate() {
        final Long id = TestContext.GLOBAL.get(TestContextKey.WORKING_CAPITAL_BREACH_ID_FOR_UPDATE);
        ok(() -> fineractFeignClient.workingCapitalBreaches().deleteWorkingCapitalBreach(id));
    }

    @Then("Admin failed to create a new WC Breach for field {string} with invalid data {string} results with an error {}")
    public void createWCBreachWithInvalidDataFailed(final String fieldName, final String value, final String errorMessage) {
        final WorkingCapitalBreachRequest request = setWCBreachFieldValue(workingCapitalRequestFactory.defaultWorkingCapitalBreachRequest(),
                fieldName, value);
        checkCreateWCBreachWithInvalidDataFailure(request, errorMessage, 400);
    }

    @Then("Admin failed to update WC Breach for field {string} with invalid data {string} results with an error {}")
    public void updateWCBreachWithInvalidDataFailed(final String fieldName, final String value, final String errorMessage) {
        Long id = TestContext.GLOBAL.get(TestContextKey.WORKING_CAPITAL_BREACH_ID_FOR_UPDATE);
        if (id == null) {
            adminCreatesWCBreachWithValuesForUpdate();
            id = TestContext.GLOBAL.get(TestContextKey.WORKING_CAPITAL_BREACH_ID_FOR_UPDATE);
        }
        final WorkingCapitalBreachRequest request = setWCBreachFieldValue(workingCapitalRequestFactory.defaultWorkingCapitalBreachRequest(),
                fieldName, value);
        checkUpdateWCBreachWithInvalidDataFailure(id, request, errorMessage, 400);
    }

    @Then("Admin failed to delete WC Breach that is already deleted")
    public void adminDeleteWCBreachAlreadyDeletedFailure() {
        final Long id = TestContext.GLOBAL.get(TestContextKey.WORKING_CAPITAL_BREACH_ID_FOR_UPDATE);
        checkDeleteWCBreachNotFoundFailure(id);
    }

    @Then("Admin failed to retrieve WC Breach that is already deleted")
    public void adminRetrieveWCBreachAlreadyDeletedFailure() {
        final Long id = TestContext.GLOBAL.get(TestContextKey.WORKING_CAPITAL_BREACH_ID_FOR_UPDATE);
        checkRetrieveWCBreachNotFoundFailure(id);
    }

    @Then("Admin failed to delete WC Breach with id {int} that doesn't exist")
    public void adminDeleteWCBreachWithIncorrectIdFailure(final Integer id) {
        checkDeleteWCBreachNotFoundFailure(Long.valueOf(id));
    }

    @Then("Admin failed to retrieve WC Breach with id {int} that is not found")
    public void adminRetrieveWCBreachWithIncorrectIdFailure(final Integer id) {
        checkRetrieveWCBreachNotFoundFailure(Long.valueOf(id));
    }

    private void checkCreateWCBreachWithInvalidDataFailure(final WorkingCapitalBreachRequest request, final String errorMessage,
            final int errorCode) {
        final CallFailedRuntimeException exception = fail(
                () -> fineractFeignClient.workingCapitalBreaches().createWorkingCapitalBreach(request));
        assertThat(exception.getStatus()).as(ErrorMessageHelper.incorrectExpectedValueInResponse()).isEqualTo(errorCode);
        assertThat(exception.getDeveloperMessage()).contains(errorMessage);
    }

    private void checkUpdateWCBreachWithInvalidDataFailure(final Long id, final WorkingCapitalBreachRequest request,
            final String errorMessage, final int errorCode) {
        final CallFailedRuntimeException exception = fail(
                () -> fineractFeignClient.workingCapitalBreaches().updateWorkingCapitalBreach(id, request));
        assertThat(exception.getStatus()).as(ErrorMessageHelper.incorrectExpectedValueInResponse()).isEqualTo(errorCode);
        assertThat(exception.getDeveloperMessage()).contains(errorMessage);
    }

    private void checkDeleteWCBreachNotFoundFailure(final Long id) {
        final CallFailedRuntimeException exception = fail(
                () -> fineractFeignClient.workingCapitalBreaches().deleteWorkingCapitalBreach(id));
        assertThat(exception.getStatus()).isEqualTo(404);
        assertThat(exception.getDeveloperMessage()).contains(ErrorMessageHelper.workingCapitalBreachNotFoundFailure(id));
    }

    private void checkRetrieveWCBreachNotFoundFailure(final Long id) {
        final CallFailedRuntimeException exception = fail(
                () -> fineractFeignClient.workingCapitalBreaches().retrieveWorkingCapitalBreach(id));
        assertThat(exception.getStatus()).isEqualTo(404);
        assertThat(exception.getDeveloperMessage()).contains(ErrorMessageHelper.workingCapitalBreachNotFoundFailure(id));
    }

    private WorkingCapitalBreachRequest setWCBreachFieldValue(final WorkingCapitalBreachRequest request, final String fieldName,
            String fieldValue) {
        if ("null".equals(fieldValue)) {
            fieldValue = null;
        }
        final Integer intValue = fieldValue != null && "breachFrequency".equals(fieldName) ? Integer.valueOf(fieldValue) : null;
        final BigDecimal decimalValue = fieldValue != null && "breachAmount".equals(fieldName) ? new BigDecimal(fieldValue) : null;
        switch (fieldName) {
            case "breachFrequency":
                request.setBreachFrequency(intValue);
            break;
            case "breachFrequencyType":
                request.setBreachFrequencyType(fieldValue);
            break;
            case "breachAmountCalculationType":
                request.setBreachAmountCalculationType(fieldValue);
            break;
            case "breachAmount":
                request.setBreachAmount(decimalValue);
            break;
            default:
            break;
        }
        return request;
    }

    private void checkBreachData(final WorkingCapitalBreachRequest request, final WorkingCapitalBreachData response) {
        final SoftAssertions assertions = new SoftAssertions();
        assertions.assertThat(response.getBreachFrequency()).isEqualTo(request.getBreachFrequency());
        assertions.assertThat(enumId(response.getBreachFrequencyType())).isEqualTo(request.getBreachFrequencyType());
        assertions.assertThat(enumId(response.getBreachAmountCalculationType())).isEqualTo(request.getBreachAmountCalculationType());
        assert response.getBreachAmount() != null;
        assertions.assertThat(response.getBreachAmount().compareTo(request.getBreachAmount())).isEqualTo(0);
        assertions.assertAll();
    }

    private String enumId(final StringEnumOptionData option) {
        return option != null ? option.getId() : null;
    }
}
