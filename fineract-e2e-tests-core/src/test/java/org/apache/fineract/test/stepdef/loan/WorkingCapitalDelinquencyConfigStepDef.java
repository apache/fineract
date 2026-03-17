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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.models.DelinquencyBucketData;
import org.apache.fineract.client.models.DelinquencyBucketRequest;
import org.apache.fineract.client.models.DelinquencyMinimumPaymentPeriodAndRuleData;
import org.apache.fineract.client.models.DelinquencyRangeData;
import org.apache.fineract.client.models.MinimumPaymentPeriodAndRule;
import org.apache.fineract.client.models.PostDelinquencyBucketResponse;
import org.apache.fineract.test.data.delinquency.DelinquencyBucketType;
import org.apache.fineract.test.data.delinquency.DelinquencyFrequencyType;
import org.apache.fineract.test.data.delinquency.DelinquencyMinimumPayment;
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
public class WorkingCapitalDelinquencyConfigStepDef extends AbstractStepDef {

    @Autowired
    private WorkingCapitalRequestFactory workingCapitalRequestFactory;

    private final FineractFeignClient fineractFeignClient;

    @When("Admin Calls Delinquency Template")
    public void adminCallsDelinquencyTemplate() {
        DelinquencyBucketData template = ok(() -> fineractFeignClient.delinquencyRangeAndBucketsManagement().getTemplate3());
        assertThat(template).isNotNull();
        log.info("Template DelinquencyBucketData: {}", template);
    }

    @Then("Get Delinquency Bucket With Template has the following values")
    public void getDelinquencyBucketWithTemplateHasTheFollowingValues() {
        Long id = TestContext.GLOBAL.get(TestContextKey.DELINQUENCY_BUCKET_ID);
        DelinquencyBucketData delinquencyBucketData = ok(() -> fineractFeignClient.delinquencyRangeAndBucketsManagement()
                .getDelinquencyBucketUniversal(id, Map.of("template", "true")));
        log.info("Get Template for Delinquency Bucket Data: {}", delinquencyBucketData);
    }

    @When("Admin creates WC Delinquency Bucket With Values")
    public void adminCreatesWCDelinquencyBucketWithValues() {
        DelinquencyBucketRequest delinquencyBucketRequest = new DelinquencyBucketRequest() //
                .name("DB-WCL-" + Utils.randomStringGenerator(12)) //
                .bucketType(DelinquencyBucketType.WORKING_CAPITAL.getValue().toString())//
                .ranges(List.of(1L)) //
                .minimumPaymentPeriodAndRule(new MinimumPaymentPeriodAndRule() //
                        .frequency(1L) //
                        .minimumPaymentType(DelinquencyMinimumPayment.PERCENTAGE.getValue()) //
                        .frequencyType(DelinquencyFrequencyType.WEEKS.getValue()) //
                        .minimumPayment(BigDecimal.valueOf(1.23D))); //
        PostDelinquencyBucketResponse ok = ok(
                () -> fineractFeignClient.delinquencyRangeAndBucketsManagement().createDelinquencyBucket(delinquencyBucketRequest));
        assertThat(ok).isNotNull();
        assertThat(ok.getResourceId()).isNotNull();
        TestContext.GLOBAL.set(TestContextKey.DELINQUENCY_BUCKET_ID, ok.getResourceId());
        TestContext.GLOBAL.set(TestContextKey.DELINQUENCY_BUCKET_CREATE_REQUEST, delinquencyBucketRequest);
    }

    @When("Admin creates WC Delinquency Bucket With Values for update")
    public void adminCreatesWCDelinquencyBucketWithValuesForUpdate() {
        DelinquencyBucketRequest delinquencyBucketRequest = workingCapitalRequestFactory.defaultWorkingCapitalDelinquencyBucketRequest()
                .name("DB-WCL-" + Utils.randomStringGenerator(12)); //
        PostDelinquencyBucketResponse ok = ok(
                () -> fineractFeignClient.delinquencyRangeAndBucketsManagement().createDelinquencyBucket(delinquencyBucketRequest));
        assertThat(ok).isNotNull();
        assertThat(ok.getResourceId()).isNotNull();
        TestContext.GLOBAL.set(TestContextKey.DELINQUENCY_BUCKET_ID_FOR_UPDATE, ok.getResourceId());
        TestContext.GLOBAL.set(TestContextKey.DELINQUENCY_BUCKET_CREATE_REQUEST_FOR_UPDATE, delinquencyBucketRequest);
    }

    @When("Admin failed to create WC Delinquency Bucket With duplicated name")
    public void adminCreateWCDelinquencyBucketWithDuplicateNameFailure() {
        Long delinquencyBucketIdForUpdate = TestContext.GLOBAL.get(TestContextKey.DELINQUENCY_BUCKET_ID_FOR_UPDATE);
        DelinquencyBucketRequest delinquencyBucketRequestForUpdate = TestContext.GLOBAL
                .get(TestContextKey.DELINQUENCY_BUCKET_CREATE_REQUEST_FOR_UPDATE);
        String name = delinquencyBucketRequestForUpdate.getName();
        DelinquencyBucketRequest delinquencyBucketRequest = workingCapitalRequestFactory.defaultWorkingCapitalDelinquencyBucketRequest()
                .name(name); //
        String errorMessage = ErrorMessageHelper.workingCapitalDelinquencyBucketCreateDuplicateNameFailure(delinquencyBucketIdForUpdate);
        checkCreateWCDelinquencyBucketWithInvalidDataFailure(delinquencyBucketRequest, errorMessage, 403);
    }

    @Then("Admin failed to create a new WC Delinquency Bucket for field {string} with invalid data {string} results with an error {}")
    public void createWCDelinquencyBucketWithInvalidDataFailed(String fieldName, String value, String errorMessage) {
        final String workingCapitalDelinquencyBucketName = "DB-WCL-" + Utils.randomStringGenerator(12); //
        final DelinquencyBucketRequest defaultWCDelinquencyBucketCreateRequest = workingCapitalRequestFactory
                .defaultWorkingCapitalDelinquencyBucketRequest() //
                .name(workingCapitalDelinquencyBucketName); //

        final DelinquencyBucketRequest wcDelinquencyBucketCreateRequestUpdated = setWCDelinquencyBucketCreateFieldValue(
                defaultWCDelinquencyBucketCreateRequest, fieldName, value);
        checkCreateWCDelinquencyBucketWithInvalidDataFailure(wcDelinquencyBucketCreateRequestUpdated, errorMessage, 400);
    }

    @When("Admin modifies WC Delinquency Bucket With Values")
    public void adminModifiesWCDelinquencyBucketWithValues() {
        Long id = TestContext.GLOBAL.get(TestContextKey.DELINQUENCY_BUCKET_ID);
        DelinquencyBucketData delinquencyBucketData = ok(
                () -> fineractFeignClient.delinquencyRangeAndBucketsManagement().getDelinquencyBucket(id));
        DelinquencyBucketRequest delinquencyBucketRequest = new DelinquencyBucketRequest() //
                .ranges(delinquencyBucketData.getRanges().stream().map(DelinquencyRangeData::getId).toList()) //
                .name(delinquencyBucketData.getName()) //
                .bucketType(DelinquencyBucketType.WORKING_CAPITAL.getValue().toString())//
                .minimumPaymentPeriodAndRule(new MinimumPaymentPeriodAndRule() //
                        .minimumPayment(BigDecimal.valueOf(7.89D)) //
                        .minimumPaymentType(DelinquencyMinimumPayment.FLAT.getValue()) //
                        .frequencyType(DelinquencyFrequencyType.YEARS.getValue()) //
                        .frequency(4L) //
                );
        ok(() -> fineractFeignClient.delinquencyRangeAndBucketsManagement().updateDelinquencyBucket(id, delinquencyBucketRequest));
        TestContext.GLOBAL.set(TestContextKey.DELINQUENCY_BUCKET_UPDATE_REQUEST, delinquencyBucketRequest);
    }

    @Then("Admin failed to update WC Delinquency Bucket for field {string} with invalid data {string} results with an error {}")
    public void updateWCDelinquencyBucketWithInvalidDataFailed(String fieldName, String value, String errorMessage) {

        Long delinquencyBucketIdForUpdate = TestContext.GLOBAL.get(TestContextKey.DELINQUENCY_BUCKET_ID_FOR_UPDATE);
        DelinquencyBucketRequest delinquencyBucketRequestForUpdate = TestContext.GLOBAL
                .get(TestContextKey.DELINQUENCY_BUCKET_CREATE_REQUEST_FOR_UPDATE);
        String delinquencyBucketNameForUpdate = delinquencyBucketRequestForUpdate.getName();
        final DelinquencyBucketRequest defaultWCDelinquencyBucketCreateRequest = workingCapitalRequestFactory
                .defaultWorkingCapitalDelinquencyBucketRequest().name(delinquencyBucketNameForUpdate); //

        final DelinquencyBucketRequest wcDelinquencyBucketCreateRequestUpdated = setWCDelinquencyBucketCreateFieldValue(
                defaultWCDelinquencyBucketCreateRequest, fieldName, value);
        checkUpdateWCDelinquencyBucketWithInvalidDataFailure(delinquencyBucketIdForUpdate, wcDelinquencyBucketCreateRequestUpdated,
                errorMessage, 400);
    }

    @When("Admin failed to update WC Delinquency Bucket With duplicated name")
    public void adminUpdateWCDelinquencyBucketWithDuplicateNameFailure() {
        Long delinquencyBucketIdForUpdate = TestContext.GLOBAL.get(TestContextKey.DELINQUENCY_BUCKET_ID_FOR_UPDATE);
        Long delinquencyBucketId = TestContext.GLOBAL.get(TestContextKey.DELINQUENCY_BUCKET_ID);
        DelinquencyBucketRequest delinquencyBucketRequestForUpdate = TestContext.GLOBAL
                .get(TestContextKey.DELINQUENCY_BUCKET_CREATE_REQUEST);
        String name = delinquencyBucketRequestForUpdate.getName();
        DelinquencyBucketRequest delinquencyBucketRequest = workingCapitalRequestFactory.defaultWorkingCapitalDelinquencyBucketRequest() //
                .name(name); //
        String errorMessage = ErrorMessageHelper.workingCapitalDelinquencyBucketCreateDuplicateNameFailure(delinquencyBucketId);
        checkUpdateWCDelinquencyBucketWithInvalidDataFailure(delinquencyBucketIdForUpdate, delinquencyBucketRequest, errorMessage, 403);
    }

    @Then("Check created Delinquency Bucket has the following values")
    public void checkCreatedDelinquencyBucketHasTheFollowingValues() {
        Long id = TestContext.GLOBAL.get(TestContextKey.DELINQUENCY_BUCKET_ID);
        DelinquencyBucketData delinquencyBucketData = ok(
                () -> fineractFeignClient.delinquencyRangeAndBucketsManagement().getDelinquencyBucket(id));
        DelinquencyBucketRequest delinquencyBucketRequest = TestContext.GLOBAL.get(TestContextKey.DELINQUENCY_BUCKET_CREATE_REQUEST);
        checkDelinquencyBucketData(delinquencyBucketRequest, delinquencyBucketData);
        log.info("Get DelinquencyBucketData : {} matches with create request data: {}", delinquencyBucketData, delinquencyBucketRequest);
    }

    @Then("Check updated Delinquency Bucket has the following values")
    public void checkUpdatedDelinquencyBucketHasTheFollowingValues() {
        Long id = TestContext.GLOBAL.get(TestContextKey.DELINQUENCY_BUCKET_ID);
        DelinquencyBucketData delinquencyBucketData = ok(
                () -> fineractFeignClient.delinquencyRangeAndBucketsManagement().getDelinquencyBucket(id));
        DelinquencyBucketRequest delinquencyBucketRequest = TestContext.GLOBAL.get(TestContextKey.DELINQUENCY_BUCKET_UPDATE_REQUEST);
        checkDelinquencyBucketData(delinquencyBucketRequest, delinquencyBucketData);
        log.info("Get DelinquencyBucketData : {} matches with update request data: {}", delinquencyBucketData, delinquencyBucketRequest);
    }

    @Then("Admin failed to retrieve WC Delinquency Bucket with id {int} that is not found")
    public void adminRetrieveWCDelinquencyBucketAlreadyDeletedFailure(Integer id) {
        checkRetrieveWCDelinquencyBucketNotFoundFailure(Long.valueOf(id));
    }

    @When("Admin deletes WC Delinquency Bucket With Values")
    public void adminDeletesWCDelinquencyBucketWithValues() {
        Long id = TestContext.GLOBAL.get(TestContextKey.DELINQUENCY_BUCKET_ID);
        ok(() -> fineractFeignClient.delinquencyRangeAndBucketsManagement().deleteDelinquencyBucket(id));
    }

    @When("Admin deletes WC Delinquency Bucket With Values for update")
    public void adminDeletesWCDelinquencyBucketWithValuesForUpdate() {
        Long id = TestContext.GLOBAL.get(TestContextKey.DELINQUENCY_BUCKET_ID_FOR_UPDATE);
        ok(() -> fineractFeignClient.delinquencyRangeAndBucketsManagement().deleteDelinquencyBucket(id));
    }

    @Then("Admin failed to delete WC Delinquency Bucket that is already deleted")
    public void adminDeleteWCDelinquencyBucketAlreadyDeletedFailure() {
        Long id = TestContext.GLOBAL.get(TestContextKey.DELINQUENCY_BUCKET_ID_FOR_UPDATE);
        checkDeleteWCDelinquencyBucketDoesntExistFailure(id);
    }

    @Then("Admin failed to delete WC Delinquency Bucket with id {int} that doesn't exist")
    public void adminDeleteWCDelinquencyBucketAlreadyDeletedFailure(Integer id) {
        checkDeleteWCDelinquencyBucketDoesntExistFailure(Long.valueOf(id));
    }

    @Then("Admin failed to retrieve WC Delinquency Bucket that is already deleted")
    public void adminRetrieveWCDelinquencyBucketAlreadyDeletedFailure() {
        Long id = TestContext.GLOBAL.get(TestContextKey.DELINQUENCY_BUCKET_ID_FOR_UPDATE);
        checkRetrieveWCDelinquencyBucketNotFoundFailure(id);
    }

    public void checkRetrieveWCDelinquencyBucketNotFoundFailure(Long id) {
        CallFailedRuntimeException exception = fail(
                () -> fineractFeignClient.delinquencyRangeAndBucketsManagement().getDelinquencyBucket(id));
        String errorMessage = ErrorMessageHelper.workingCapitalDelinquencyBucketNotFoundFailure(id);
        assertThat(exception.getStatus()).as(errorMessage).isEqualTo(404);
        assertThat(exception.getDeveloperMessage()).contains(errorMessage);
    }

    public void checkCreateWCDelinquencyBucketWithInvalidDataFailure(DelinquencyBucketRequest defaultWCDelinquencyBucketCreateRequest,
            String errorMessage, int errorCode) {
        CallFailedRuntimeException exception = fail(() -> fineractFeignClient.delinquencyRangeAndBucketsManagement()
                .createDelinquencyBucket(defaultWCDelinquencyBucketCreateRequest));
        assertThat(exception.getStatus()).as(ErrorMessageHelper.incorrectExpectedValueInResponse()).isEqualTo(errorCode);
        assertThat(exception.getDeveloperMessage()).contains(errorMessage);
    }

    public DelinquencyBucketRequest setWCDelinquencyBucketCreateFieldValue(DelinquencyBucketRequest delinquencyBucketRequest,
            String fieldName, String fieldValue) {
        if (fieldValue.equals("null")) {
            fieldValue = null;
        }
        Long valueLong = null;
        BigDecimal valueBigDecimal = null;
        if (fieldName.equalsIgnoreCase("frequency") || fieldName.equalsIgnoreCase("minimumPaymentType")) {
            valueLong = fieldValue != null ? Long.valueOf(fieldValue) : null;
        }
        if (fieldName.equalsIgnoreCase("minimumPayment")) {
            valueBigDecimal = fieldValue != null ? new BigDecimal(fieldValue) : null;
        }
        List<Long> valueArrayList = new ArrayList<>();
        if (fieldName.equalsIgnoreCase("ranges")) {
            assert fieldValue != null;
            valueArrayList = fieldValue.equalsIgnoreCase("[]") ? new ArrayList<>() : new ArrayList<>(List.of(Long.valueOf(fieldValue)));
        }
        MinimumPaymentPeriodAndRule minimumPaymentPeriodAndRuleRequest = delinquencyBucketRequest.getMinimumPaymentPeriodAndRule();
        assert minimumPaymentPeriodAndRuleRequest != null;

        switch (fieldName) {
            case "name":
                delinquencyBucketRequest.setName(fieldValue);
            break;
            case "ranges":
                delinquencyBucketRequest.setRanges(valueArrayList);
            break;
            case "bucketType":
                delinquencyBucketRequest.setBucketType(fieldValue);
            break;
            case "minimumPaymentPeriodAndRule":
                delinquencyBucketRequest.setMinimumPaymentPeriodAndRule(fieldValue == null ? null : minimumPaymentPeriodAndRuleRequest);
            break;
            case "frequency":
                minimumPaymentPeriodAndRuleRequest.setFrequency(valueLong);
            break;
            case "frequencyType":
                minimumPaymentPeriodAndRuleRequest.setFrequencyType(fieldValue == null ? null : Integer.valueOf(fieldValue));
            break;
            case "minimumPayment":
                minimumPaymentPeriodAndRuleRequest.setMinimumPayment(valueBigDecimal);
            break;
            case "minimumPaymentType":
                minimumPaymentPeriodAndRuleRequest.setMinimumPaymentType(valueLong);
            break;
            default:
            break;
        }
        return delinquencyBucketRequest;
    }

    public void checkDelinquencyBucketData(DelinquencyBucketRequest delinquencyBucketRequest, DelinquencyBucketData delinquencyBucketData) {
        SoftAssertions assertions = new SoftAssertions();
        assertions.assertThat(delinquencyBucketRequest.getName()).isEqualTo(delinquencyBucketData.getName());
        assert delinquencyBucketRequest.getBucketType() != null;
        assertions.assertThat(Long.valueOf(delinquencyBucketRequest.getBucketType())).isEqualTo(delinquencyBucketData.getBucketType());

        // minimum payment period and rule
        MinimumPaymentPeriodAndRule minimumPaymentPeriodAndRuleRequest = delinquencyBucketRequest.getMinimumPaymentPeriodAndRule();
        DelinquencyMinimumPaymentPeriodAndRuleData minimumPaymentPeriodAndRuleResponse = delinquencyBucketData
                .getMinimumPaymentPeriodAndRule();
        assert minimumPaymentPeriodAndRuleRequest != null;
        assert minimumPaymentPeriodAndRuleResponse != null;
        assertions.assertThat(
                minimumPaymentPeriodAndRuleRequest.getMinimumPayment().compareTo(minimumPaymentPeriodAndRuleResponse.getMinimumPayment()))
                .isEqualTo(0);
        assert minimumPaymentPeriodAndRuleResponse.getMinimumPaymentType() != null;
        assertions.assertThat(Long.valueOf(minimumPaymentPeriodAndRuleRequest.getMinimumPaymentType()))
                .isEqualTo(minimumPaymentPeriodAndRuleResponse.getMinimumPaymentType().getId());
        assertions.assertThat(minimumPaymentPeriodAndRuleRequest.getFrequency())
                .isEqualTo(minimumPaymentPeriodAndRuleResponse.getFrequency());
        assert minimumPaymentPeriodAndRuleResponse.getFrequencyType() != null;
        assertions.assertThat(Long.valueOf(minimumPaymentPeriodAndRuleRequest.getFrequencyType()))
                .isEqualTo(minimumPaymentPeriodAndRuleResponse.getFrequencyType().getId());

        // ranges
        assert delinquencyBucketData.getRanges() != null;
        assertions.assertThat(delinquencyBucketRequest.getRanges())
                .containsAll(delinquencyBucketData.getRanges().stream().map(DelinquencyRangeData::getId).toList());

        assertions.assertAll();
    }

    public void checkUpdateWCDelinquencyBucketWithInvalidDataFailure(Long id, DelinquencyBucketRequest defaultWCDelinquencyBucketRequest,
            String errorMessage, int errorCode) {
        CallFailedRuntimeException exception = fail(() -> fineractFeignClient.delinquencyRangeAndBucketsManagement()
                .updateDelinquencyBucket(id, defaultWCDelinquencyBucketRequest));
        assertThat(exception.getStatus()).as(ErrorMessageHelper.incorrectExpectedValueInResponse()).isEqualTo(errorCode);
        assertThat(exception.getDeveloperMessage()).contains(errorMessage);
    }

    public void checkDeleteWCDelinquencyBucketDoesntExistFailure(Long id) {
        CallFailedRuntimeException exception = fail(
                () -> fineractFeignClient.delinquencyRangeAndBucketsManagement().deleteDelinquencyBucket(id));
        String errorMessage = ErrorMessageHelper.workingCapitalDelinquencyBucketDoesntExistFailure(id);
        assertThat(exception.getStatus()).as(errorMessage).isEqualTo(404);
        assertThat(exception.getDeveloperMessage()).contains(errorMessage);
    }

}
