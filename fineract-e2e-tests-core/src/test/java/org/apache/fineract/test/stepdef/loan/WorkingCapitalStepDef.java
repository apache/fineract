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
import static org.apache.fineract.test.support.TestContextKey.WORKING_CAPITAL_BREACH_ID;
import static org.apache.fineract.test.support.TestContextKey.WORKING_CAPITAL_NEAR_BREACH_ID;
import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.feign.services.WorkingCapitalLoanProductsApi;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.models.CommandProcessingResult;
import org.apache.fineract.client.models.DeleteWorkingCapitalLoanProductsProductIdResponse;
import org.apache.fineract.client.models.GetConfigurableAttributes;
import org.apache.fineract.client.models.GetPaymentAllocation;
import org.apache.fineract.client.models.GetWorkingCapitalLoanDelinquencyRangeScheduleTagHistoryResponse;
import org.apache.fineract.client.models.GetWorkingCapitalLoanProductsProductIdResponse;
import org.apache.fineract.client.models.GetWorkingCapitalLoanProductsResponse;
import org.apache.fineract.client.models.GetWorkingCapitalLoanProductsTemplateResponse;
import org.apache.fineract.client.models.InternalWorkingCapitalLoanPaymentRequest;
import org.apache.fineract.client.models.PostAllowAttributeOverrides;
import org.apache.fineract.client.models.PostWorkingCapitalLoanProductsRequest;
import org.apache.fineract.client.models.PostWorkingCapitalLoanProductsRequest.AccountingRuleEnum;
import org.apache.fineract.client.models.PostWorkingCapitalLoanProductsResponse;
import org.apache.fineract.client.models.PostWorkingCapitalLoansResponse;
import org.apache.fineract.client.models.PutWorkingCapitalLoanProductsProductIdRequest;
import org.apache.fineract.client.models.PutWorkingCapitalLoanProductsProductIdResponse;
import org.apache.fineract.client.models.StringEnumOptionData;
import org.apache.fineract.client.models.WorkingCapitalBreachRequest;
import org.apache.fineract.client.models.WorkingCapitalNearBreachRequest;
import org.apache.fineract.test.data.accounttype.AccountTypeResolver;
import org.apache.fineract.test.data.accounttype.DefaultAccountType;
import org.apache.fineract.test.data.workingcapitalproduct.DefaultWorkingCapitalLoanProduct;
import org.apache.fineract.test.data.workingcapitalproduct.WCGLAccountMapping;
import org.apache.fineract.test.data.workingcapitalproduct.WorkingCapitalBreachFrequencyType;
import org.apache.fineract.test.factory.LoanProductsRequestFactory;
import org.apache.fineract.test.factory.WorkingCapitalRequestFactory;
import org.apache.fineract.test.helper.ErrorMessageHelper;
import org.apache.fineract.test.helper.Utils;
import org.apache.fineract.test.stepdef.AbstractStepDef;
import org.apache.fineract.test.support.TestContextKey;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Assertions;

@Slf4j
@RequiredArgsConstructor
public class WorkingCapitalStepDef extends AbstractStepDef {

    private final WorkingCapitalRequestFactory workingCapitalRequestFactory;
    private final FineractFeignClient fineractFeignClient;
    private final AccountTypeResolver accountTypeResolver;

    public static final String NAME_FIELD_NAME = "name";
    public static final String SHORT_NAME_FIELD = "shortName";
    public static final String DESCRIPTION_FIELD_NAME = "description";
    public static final String CURRENCY_CODE_FIELD_NAME = "currencyCode";
    public static final String DIGITS_AFTER_DECIMAL_FIELD_NAME = "digitsAfterDecimal";
    public static final String IN_MULTIPLES_OF_FIELD_NAME = "inMultiplesOf";
    public static final String AMORTIZATION_TYPE_FIELD_NAME = "amortizationType";
    public static final String NPV_DAY_COUNT_FIELD_NAME = "npvDayCount";
    public static final String PRINCIPAL_FIELD_NAME = "principal";
    public static final String MIN_PRINCIPAL_FIELD_NAME = "minPrincipal";
    public static final String MAX_PRINCIPAL_FIELD_NAME = "maxPrincipal";
    public static final String DISCOUNT_FIELD_NAME = "discount";
    public static final String PERIOD_PAYMENT_RATE_FIELD_NAME = "periodPaymentRate";
    public static final String MIN_PERIOD_PAYMENT_RATE_FIELD_NAME = "minPeriodPaymentRate";
    public static final String MAX_PERIOD_PAYMENT_RATE_FIELD_NAME = "maxPeriodPaymentRate";
    public static final String REPAYMENT_FREQUENCY_TYPE_FIELD_NAME = "repaymentFrequencyType";
    public static final String REPAYMENT_EVERY_FIELD_NAME = "repaymentEvery";
    public static final String EXTERNAL_ID_FIELD_NAME = "externalId";
    public static final String DELINQUENCY_BUCKET_ID_FIELD_NAME = "delinquencyBucketId";
    public static final String DELINQUENCY_GRACE_DAYS_FIELD_NAME = "delinquencyGraceDays";
    public static final String DELINQUENCY_START_TYPE_FIELD_NAME = "delinquencyStartType";
    public static final String BREACH_ID_FIELD_NAME = "breachId";
    public static final String NEAR_BREACH_ID_FIELD_NAME = "nearBreachId";
    public static final String LOCALE_FIELD_NAME = "locale";

    private static final long NON_EXISTENT_GL_ACCOUNT_ID = 999999L;

    private WorkingCapitalLoanProductsApi workingCapitalApi() {
        return fineractFeignClient.workingCapitalLoanProducts();
    }

    @When("Admin creates a new Working Capital Loan Product")
    public void createWorkingCapitalLoanProduct() {
        final String workingCapitalProductDefaultName = DefaultWorkingCapitalLoanProduct.WCLP.getName()
                + Utils.randomStringGenerator("_", 10);
        final PostWorkingCapitalLoanProductsRequest defaultWorkingCapitalLoanProductCreateRequest = workingCapitalRequestFactory
                .defaultWorkingCapitalLoanProductRequest() //
                .name(workingCapitalProductDefaultName); //
        final PostWorkingCapitalLoanProductsResponse responseDefaultWorkingCapitalLoanProductCreate = createWorkingCapitalLoanProduct(
                defaultWorkingCapitalLoanProductCreateRequest);
        testContext().set(TestContextKey.WORKING_CAPITAL_LOAN_PRODUCT_CREATE_RESPONSE, responseDefaultWorkingCapitalLoanProductCreate);
        testContext().set(TestContextKey.WORKING_CAPITAL_LOAN_PRODUCT_CREATE_REQUEST, defaultWorkingCapitalLoanProductCreateRequest);
        checkWorkingCapitalLoanProductCreate();
    }

    @When("Admin creates a new Working Capital Loan Product with breach and near breach")
    public void createWorkingCapitalLoanProductWithBreachAndNearBreach() {
        final Long breachId = getWcBreachIdForFrequency(2, WorkingCapitalBreachFrequencyType.MONTHS.getCode());
        final Long nearBreachId = getWcNearBreachIdForFrequency(1, WorkingCapitalBreachFrequencyType.MONTHS.getCode());

        final String name = DefaultWorkingCapitalLoanProduct.WCLP.getName() + Utils.randomStringGenerator("_", 10);
        final PostWorkingCapitalLoanProductsRequest request = workingCapitalRequestFactory.defaultWorkingCapitalLoanProductRequest() //
                .name(name) //
                .breachId(breachId) //
                .nearBreachId(nearBreachId); //

        final PostWorkingCapitalLoanProductsResponse response = createWorkingCapitalLoanProduct(request);
        testContext().set(TestContextKey.WORKING_CAPITAL_LOAN_PRODUCT_CREATE_RESPONSE, response);
        testContext().set(TestContextKey.WORKING_CAPITAL_LOAN_PRODUCT_CREATE_REQUEST, request);
        checkWorkingCapitalLoanProductCreate();
    }

    @When("Admin creates a new WCLP with breach {int} {string} frequency and near breach {int} {string} frequency")
    public void createWorkingCapitalLoanProductWithBreachAndNearBreach(int breachFrequency, String breachFrequencyType,
            int nearBreachFrequency, String nearBreachFrequencyType) {
        final Long breachId = getWcBreachIdForFrequency(breachFrequency, breachFrequencyType);
        final Long nearBreachId = getWcNearBreachIdForFrequency(nearBreachFrequency, nearBreachFrequencyType);

        final String name = DefaultWorkingCapitalLoanProduct.WCLP.getName() + Utils.randomStringGenerator("_", 10);
        final PostWorkingCapitalLoanProductsRequest request = workingCapitalRequestFactory.defaultWorkingCapitalLoanProductRequest() //
                .name(name) //
                .breachId(breachId) //
                .nearBreachId(nearBreachId); //

        final PostWorkingCapitalLoanProductsResponse response = createWorkingCapitalLoanProduct(request);
        testContext().set(TestContextKey.WORKING_CAPITAL_LOAN_PRODUCT_CREATE_RESPONSE, response);
        testContext().set(TestContextKey.WORKING_CAPITAL_LOAN_PRODUCT_CREATE_REQUEST, request);
        checkWorkingCapitalLoanProductCreate();
    }

    @When("Admin failed to create Working Capital Loan Product without breach, but with near breach specified")
    public void createWorkingCapitalLoanProductWithoutBreachButNearBreachFailure() {
        final Long nearBreachId = getWcNearBreachIdForFrequency(1, WorkingCapitalBreachFrequencyType.MONTHS.getCode());

        final String name = DefaultWorkingCapitalLoanProduct.WCLP.getName() + Utils.randomStringGenerator("_", 10);
        final PostWorkingCapitalLoanProductsRequest request = workingCapitalRequestFactory.defaultWorkingCapitalLoanProductRequest() //
                .name(name) //
                .nearBreachId(nearBreachId); //
        final String errorMessage = ErrorMessageHelper.nearBreachCannotEnableWithoutBreachFailure();
        createWorkingCapitalLoanProductFailure(request, 400, errorMessage);
    }

    public void createWorkingCapitalLoanProductFailure(PostWorkingCapitalLoanProductsRequest request, int statusCode, String errorMessage) {
        CallFailedRuntimeException exception = fail(() -> workingCapitalApi().createWorkingCapitalLoanProduct(request, Map.of()));
        assertThat(exception.getStatus()).as(ErrorMessageHelper.incorrectExpectedValueInResponse()).isEqualTo(statusCode);
        assertThat(exception.getDeveloperMessage()).contains(errorMessage);
    }

    @When("Admin failed to create WCLP with breach {int} {string} frequency lower then near breach {int} {string} frequency")
    public void createWCLPWithBreachFrequencyLowerThenNearBreachFailure(int breachFrequency, String breachFrequencyType,
            int nearBreachFrequency, String nearBreachFrequencyType) {
        final Long breachId = getWcBreachIdForFrequency(breachFrequency, breachFrequencyType);
        final Long nearBreachId = getWcNearBreachIdForFrequency(nearBreachFrequency, nearBreachFrequencyType);

        final String name = DefaultWorkingCapitalLoanProduct.WCLP.getName() + Utils.randomStringGenerator("_", 10);
        final PostWorkingCapitalLoanProductsRequest request = workingCapitalRequestFactory.defaultWorkingCapitalLoanProductRequest() //
                .name(name) //
                .breachId(breachId) //
                .nearBreachId(nearBreachId); //

        final String errorMessage = ErrorMessageHelper.nearBreachMustBeLowerThenBreachFailure();
        createWorkingCapitalLoanProductFailure(request, 400, errorMessage);
    }

    @When("Admin updates a Working Capital Loan Product with breach and near breach")
    public void updateWorkingCapitalLoanProductWithBreachAndNearBreach() throws JsonProcessingException {
        final Long breachId = getWcBreachIdForFrequency(1, WorkingCapitalBreachFrequencyType.YEARS.getCode());
        final Long nearBreachId = getWcNearBreachIdForFrequency(10, WorkingCapitalBreachFrequencyType.DAYS.getCode());

        updateWorkingCapitalLoanProductWithBreachAndNearBreach(breachId, nearBreachId);
    }

    @When("Admin updates a Working Capital Loan Product with near breach")
    public void updateWorkingCapitalLoanProductWithNearBreach() throws JsonProcessingException {
        PostWorkingCapitalLoanProductsRequest workingCapitalLoanProductsRequest = testContext()
                .get(TestContextKey.WORKING_CAPITAL_LOAN_PRODUCT_CREATE_REQUEST);
        final Long breachId = workingCapitalLoanProductsRequest.getBreachId();
        final Long nearBreachId = getWcNearBreachIdForFrequency(10, WorkingCapitalBreachFrequencyType.DAYS.getCode());

        updateWorkingCapitalLoanProductWithBreachAndNearBreach(breachId, nearBreachId);
    }

    @When("Admin updates a WCLP with breach {int} {string} frequency and near breach {int} {string} frequency")
    public void updateWorkingCapitalLoanProductWithBreachAndNearBreach(int breachFrequency, String breachFrequencyType,
            int nearBreachFrequency, String nearBreachFrequencyType) throws JsonProcessingException {

        final Long breachId = getWcBreachIdForFrequency(breachFrequency, breachFrequencyType);
        final Long nearBreachId = getWcNearBreachIdForFrequency(nearBreachFrequency, nearBreachFrequencyType);

        updateWorkingCapitalLoanProductWithBreachAndNearBreach(breachId, nearBreachId);
    }

    public void updateWorkingCapitalLoanProductFailure(Long resourceId,
            PutWorkingCapitalLoanProductsProductIdRequest defaultWorkingCapitalLoanProductUpdateRequest, int statusCode,
            String errorMessage) {
        CallFailedRuntimeException exception = fail(() -> workingCapitalApi().updateWorkingCapitalLoanProduct(resourceId,
                defaultWorkingCapitalLoanProductUpdateRequest, Map.of()));
        assertThat(exception.getStatus()).as(errorMessage).isEqualTo(statusCode);
        assertThat(exception.getDeveloperMessage()).contains(errorMessage);
    }

    @Then("Admin failed to update WCLP with breach {int} {string} frequency lower then near breach {int} {string} frequency")
    public void updateWCLPWithBreachFrequencyLowerThenNearBreachFailure(int breachFrequency, String breachFrequencyType,
            int nearBreachFrequency, String nearBreachFrequencyType) {
        final Long breachId = getWcBreachIdForFrequency(breachFrequency, breachFrequencyType);
        final Long nearBreachId = getWcNearBreachIdForFrequency(nearBreachFrequency, nearBreachFrequencyType);

        PutWorkingCapitalLoanProductsProductIdRequest defaultWorkingCapitalLoanProductUpdateRequest = new PutWorkingCapitalLoanProductsProductIdRequest()
                .breachId(breachId) //
                .nearBreachId(nearBreachId); //

        PostWorkingCapitalLoanProductsResponse workingCapitalLoanProductsResponse = testContext()
                .get(TestContextKey.WORKING_CAPITAL_LOAN_PRODUCT_CREATE_RESPONSE);
        Long resourceId = workingCapitalLoanProductsResponse.getResourceId();

        final String errorMessage = ErrorMessageHelper.nearBreachMustBeLowerThenBreachFailure();
        updateWorkingCapitalLoanProductFailure(resourceId, defaultWorkingCapitalLoanProductUpdateRequest, 400, errorMessage);
    }

    @When("Admin failed to update Working Capital Loan Product without breach, but with near breach specified")
    public void updateWorkingCapitalLoanProductWithoutBreachButNearBreachFailure() {
        final Long nearBreachId = getWcNearBreachIdForFrequency(1, WorkingCapitalBreachFrequencyType.MONTHS.getCode());

        final String name = DefaultWorkingCapitalLoanProduct.WCLP.getName() + Utils.randomStringGenerator("_", 10);
        PutWorkingCapitalLoanProductsProductIdRequest defaultWorkingCapitalLoanProductUpdateRequest = new PutWorkingCapitalLoanProductsProductIdRequest()
                .name(name) //
                .nearBreachId(nearBreachId); //

        PostWorkingCapitalLoanProductsResponse workingCapitalLoanProductsResponse = testContext()
                .get(TestContextKey.WORKING_CAPITAL_LOAN_PRODUCT_CREATE_RESPONSE);
        Long resourceId = workingCapitalLoanProductsResponse.getResourceId();

        final String errorMessage = ErrorMessageHelper.nearBreachCannotEnableWithoutBreachFailure();
        updateWorkingCapitalLoanProductFailure(resourceId, defaultWorkingCapitalLoanProductUpdateRequest, 400, errorMessage);
    }

    @When("Admin creates a new Working Capital Loan Product with breachId")
    public void createWorkingCapitalLoanProductWithBreachId() {
        final CommandProcessingResult breachCreateResponse = ok(() -> fineractFeignClient.workingCapitalBreaches()
                .createWorkingCapitalBreach(workingCapitalRequestFactory.defaultWorkingCapitalBreachRequest()));
        final Long breachId = breachCreateResponse.getResourceId();
        testContext().set(WORKING_CAPITAL_BREACH_ID, breachId);

        final String name = DefaultWorkingCapitalLoanProduct.WCLP.getName() + Utils.randomStringGenerator("_", 10);
        final PostWorkingCapitalLoanProductsRequest request = workingCapitalRequestFactory.defaultWorkingCapitalLoanProductRequest() //
                .name(name) //
                .breachId(breachId);

        final PostWorkingCapitalLoanProductsResponse response = createWorkingCapitalLoanProduct(request);
        testContext().set(TestContextKey.WORKING_CAPITAL_LOAN_PRODUCT_CREATE_RESPONSE, response);
        testContext().set(TestContextKey.WORKING_CAPITAL_LOAN_PRODUCT_CREATE_REQUEST, request);
        checkWorkingCapitalLoanProductCreate();
    }

    @When("Admin creates a new Working Capital Loan Product with breachId and overrides enabled")
    public void createWorkingCapitalLoanProductWithBreachIdAndOverrides() {
        final CommandProcessingResult breachCreateResponse = ok(() -> fineractFeignClient.workingCapitalBreaches()
                .createWorkingCapitalBreach(workingCapitalRequestFactory.defaultWorkingCapitalBreachRequest()));
        final Long breachId = breachCreateResponse.getResourceId();
        testContext().set(TestContextKey.WORKING_CAPITAL_BREACH_ID, breachId);

        final String name = DefaultWorkingCapitalLoanProduct.WCLP.getName() + Utils.randomStringGenerator("_", 10);
        final PostWorkingCapitalLoanProductsRequest request = workingCapitalRequestFactory
                .defaultWorkingCapitalLoanProductAllowAttributesOverrideRequest() //
                .name(name) //
                .breachId(breachId);

        final PostWorkingCapitalLoanProductsResponse response = createWorkingCapitalLoanProduct(request);
        testContext().set(TestContextKey.WORKING_CAPITAL_LOAN_PRODUCT_CREATE_RESPONSE, response);
        testContext().set(TestContextKey.WORKING_CAPITAL_LOAN_PRODUCT_CREATE_REQUEST, request);
        checkWorkingCapitalLoanProductCreate();
    }

    @When("Admin creates a Working Capital Loan Product with custom breach config and overrides enabled:")
    public void createWorkingCapitalLoanProductWithCustomBreachConfig(final DataTable table) {
        final Map<String, String> data = table.asMaps().get(0);

        final String breachName = "WC Breach " + Utils.randomStringGenerator("", 10);
        final WorkingCapitalBreachRequest breachRequest = new WorkingCapitalBreachRequest().name(breachName)
                .breachFrequency(Integer.valueOf(data.get("breachFrequency"))).breachFrequencyType(data.get("breachFrequencyType"))
                .breachAmountCalculationType(data.get("breachAmountCalculationType"))
                .breachAmount(new BigDecimal(data.get("breachAmount")));
        final CommandProcessingResult breachCreateResponse = ok(
                () -> fineractFeignClient.workingCapitalBreaches().createWorkingCapitalBreach(breachRequest));
        final Long breachId = breachCreateResponse.getResourceId();
        testContext().set(TestContextKey.WORKING_CAPITAL_BREACH_ID, breachId);

        final String graceDaysStr = data.get("delinquencyGraceDays");
        final Integer graceDays = graceDaysStr != null && !graceDaysStr.isEmpty() ? Integer.valueOf(graceDaysStr) : null;

        final String name = DefaultWorkingCapitalLoanProduct.WCLP.getName() + Utils.randomStringGenerator("_", 10);
        final PostWorkingCapitalLoanProductsRequest request = workingCapitalRequestFactory
                .defaultWorkingCapitalLoanProductAllowAttributesOverrideRequest() //
                .name(name) //
                .breachId(breachId) //
                .delinquencyGraceDays(graceDays);

        final PostWorkingCapitalLoanProductsResponse response = createWorkingCapitalLoanProduct(request);
        testContext().set(TestContextKey.WORKING_CAPITAL_LOAN_PRODUCT_CREATE_RESPONSE, response);
        testContext().set(TestContextKey.WORKING_CAPITAL_LOAN_PRODUCT_CREATE_REQUEST, request);
        checkWorkingCapitalLoanProductCreate();
    }

    @When("Admin creates a new Working Capital Loan Product with external-id")
    public void createWorkingCapitalLoanProductWithExternalId() {
        final String workingCapitalProductDefaultName = DefaultWorkingCapitalLoanProduct.WCLP.getName()
                + Utils.randomStringGenerator("_", 10);
        final PostWorkingCapitalLoanProductsRequest defaultWorkingCapitalLoanProductCreateRequest = workingCapitalRequestFactory
                .defaultWorkingCapitalLoanProductRequest() //
                .name(workingCapitalProductDefaultName) //
                .externalId("EXT-WCP-" + UUID.randomUUID());//
        final PostWorkingCapitalLoanProductsResponse responseDefaultWorkingCapitalLoanProductCreate = createWorkingCapitalLoanProduct(
                defaultWorkingCapitalLoanProductCreateRequest);
        testContext().set(TestContextKey.WORKING_CAPITAL_LOAN_PRODUCT_CREATE_RESPONSE, responseDefaultWorkingCapitalLoanProductCreate);
        testContext().set(TestContextKey.WORKING_CAPITAL_LOAN_PRODUCT_CREATE_REQUEST, defaultWorkingCapitalLoanProductCreateRequest);
        checkWorkingCapitalLoanProductWithExternalIdCreate();
    }

    @Then("Admin failed to create a new Working Capital Loan Product field {string} with empty or null mandatory data {string}")
    public void createWorkingCapitalLoanProductWithEmptyDataFailed(String fieldName, String value) {
        String errorMessage = ErrorMessageHelper.fieldValueNullOrEmptyMandatoryFailure(fieldName);
        createWorkingCapitalLoanProductWithInvalidDataFailure(fieldName, value, errorMessage);
    }

    @Then("Admin failed to create a new Working Capital Loan Product field {string} with max length data {int} while max allowed is {int}")
    public void createWorkingCapitalLoanProductWithMaxLengthDataFailed(String fieldName, int maxLengthValue, int maxAllowedLengthValue) {
        final String workingCapitalProductDefaultName = DefaultWorkingCapitalLoanProduct.WCLP.getName()
                + Utils.randomStringGenerator("_", 10);
        final PostWorkingCapitalLoanProductsRequest defaultWorkingCapitalLoanProductCreateRequest = workingCapitalRequestFactory
                .defaultWorkingCapitalLoanProductRequest() //
                .name(workingCapitalProductDefaultName); //
        String value = Utils.randomStringGenerator(maxLengthValue);
        final PostWorkingCapitalLoanProductsRequest workingCapitalLoanProductCreateRequestUpdated = setWorkingCapitalLoanProductsCreateFieldValue(
                defaultWorkingCapitalLoanProductCreateRequest, fieldName, value);

        String errorMessage = ErrorMessageHelper.fieldValueMoreMaxLengthAllowedFailure(fieldName, maxAllowedLengthValue);
        checkCreateWorkingCapitalLoanProductWithInvalidDataFailure(workingCapitalLoanProductCreateRequestUpdated, 400, errorMessage);
    }

    @Then("Admin failed to create a new Working Capital Loan Product with field {string} with zero incorrect value")
    public void createWorkingCapitalLoanProductWithZeroValueDataFailed(String fieldName) {
        String errorMessage = ErrorMessageHelper.fieldValueZeroValueFailure(fieldName);
        createWorkingCapitalLoanProductWithInvalidDataFailure(fieldName, "0", errorMessage);
    }

    @Then("Admin failed to create a new Working Capital Loan Product with field {string} invalid data {string} and got an error {string}")
    public void createWorkingCapitalLoanProductWithInvalidDataFailed(String fieldName, String value, String errorMessage) {
        createWorkingCapitalLoanProductWithInvalidDataFailure(fieldName, value, errorMessage);
    }

    @Then("Admin failed to create a new Working Capital Loan Product with breach with field {string} invalid data {string} and got an error {string}")
    public void createWorkingCapitalLoanProductWithBreachWithInvalidDataFailed(String fieldName, String value, String errorMessage) {
        createWorkingCapitalLoanProductWithBreachWithInvalidDataFailure(fieldName, value, errorMessage);
    }

    @Then("Admin failed to create a new Working Capital Loan Product with invalid number of payment allocation rules")
    public void createWorkingCapitalLoanProductWithInvalidNumberPaymentAllocationFailed() {
        final String workingCapitalProductDefaultName = DefaultWorkingCapitalLoanProduct.WCLP.getName()
                + Utils.randomStringGenerator("_", 10);
        final PostWorkingCapitalLoanProductsRequest defaultWorkingCapitalLoanProductCreateRequest = workingCapitalRequestFactory
                .defaultWorkingCapitalLoanProductRequest() //
                .name(workingCapitalProductDefaultName) //
                .paymentAllocation(
                        workingCapitalRequestFactory.invalidNumberOfPaymentAllocationRulesForWorkingCapitalLoanProductCreateRequest());

        String errorMessage = ErrorMessageHelper.paymentAllocationRulesInvalidNumberFailure(4);
        checkCreateWorkingCapitalLoanProductWithInvalidDataFailure(defaultWorkingCapitalLoanProductCreateRequest, 400, errorMessage);
    }

    @Then("Admin failed to create a new Working Capital Loan Product with invalid value of payment allocation rules")
    public void createWorkingCapitalLoanProductWithInvalidPaymentAllocationFailed() {
        final String workingCapitalProductDefaultName = DefaultWorkingCapitalLoanProduct.WCLP.getName()
                + Utils.randomStringGenerator("_", 10);
        final PostWorkingCapitalLoanProductsRequest defaultWorkingCapitalLoanProductCreateRequest = workingCapitalRequestFactory
                .defaultWorkingCapitalLoanProductRequest() //
                .name(workingCapitalProductDefaultName) //
                .paymentAllocation(workingCapitalRequestFactory.invalidPaymentAllocationRulesForWorkingCapitalLoanProductCreateRequest());

        String errorMessage = ErrorMessageHelper.paymentAllocationRulesInvalidValueFailure();
        checkCreateWorkingCapitalLoanProductWithInvalidDataFailure(defaultWorkingCapitalLoanProductCreateRequest, 400, errorMessage);
    }

    @When("Admin updates a Working Capital Loan Product")
    public void updateWorkingCapitalLoanProduct() {
        final String workingCapitalProductDefaultName = DefaultWorkingCapitalLoanProduct.WCLP.getName()
                + Utils.randomStringGenerator("_", 10);
        final String workingCapitalProductDefaultShortName = Utils.randomStringGenerator(4);
        final PutWorkingCapitalLoanProductsProductIdRequest workingCapitalLoanProductUpdateRequest = workingCapitalRequestFactory
                .defaultWorkingCapitalLoanProductRequestUpdate() //
                .name(workingCapitalProductDefaultName) //
                .shortName(workingCapitalProductDefaultShortName)//
                .externalId("EXT-WCP-" + UUID.randomUUID());

        PostWorkingCapitalLoanProductsResponse workingCapitalLoanProductsResponse = testContext()
                .get(TestContextKey.WORKING_CAPITAL_LOAN_PRODUCT_CREATE_RESPONSE);
        Long resourceId = workingCapitalLoanProductsResponse.getResourceId();

        PutWorkingCapitalLoanProductsProductIdResponse responseWorkingCapitalLoanProductUpdate = ok(
                () -> workingCapitalApi().updateWorkingCapitalLoanProduct(resourceId, workingCapitalLoanProductUpdateRequest, Map.of()));

        testContext().set(TestContextKey.WORKING_CAPITAL_LOAN_PRODUCT_UPDATE_RESPONSE, responseWorkingCapitalLoanProductUpdate);
        testContext().set(TestContextKey.WORKING_CAPITAL_LOAN_PRODUCT_UPDATE_REQUEST, workingCapitalLoanProductUpdateRequest);
        checkWorkingCapitalLoanProductUpdate();
    }

    @When("Admin updates a Working Capital Loan Product via external-id")
    public void updateWorkingCapitalLoanProductViaExternalId() {
        final String workingCapitalProductDefaultName = DefaultWorkingCapitalLoanProduct.WCLP.getName()
                + Utils.randomStringGenerator("_", 10);
        final String workingCapitalProductDefaultShortName = Utils.randomStringGenerator(4);
        final PutWorkingCapitalLoanProductsProductIdRequest workingCapitalLoanProductUpdateRequest = workingCapitalRequestFactory
                .defaultWorkingCapitalLoanProductRequestUpdate() //
                .name(workingCapitalProductDefaultName) //
                .shortName(workingCapitalProductDefaultShortName)//
                .externalId("EXT-WCP-" + UUID.randomUUID());

        PostWorkingCapitalLoanProductsRequest workingCapitalLoanProductsRequest = testContext()
                .get(TestContextKey.WORKING_CAPITAL_LOAN_PRODUCT_CREATE_REQUEST);
        String externalId = workingCapitalLoanProductsRequest.getExternalId();

        PutWorkingCapitalLoanProductsProductIdResponse responseWorkingCapitalLoanProductUpdate = ok(() -> workingCapitalApi()
                .updateWorkingCapitalLoanProductByExternalId(externalId, workingCapitalLoanProductUpdateRequest, Map.of()));

        testContext().set(TestContextKey.WORKING_CAPITAL_LOAN_PRODUCT_UPDATE_RESPONSE, responseWorkingCapitalLoanProductUpdate);
        testContext().set(TestContextKey.WORKING_CAPITAL_LOAN_PRODUCT_UPDATE_REQUEST, workingCapitalLoanProductUpdateRequest);
        checkWorkingCapitalLoanProductWithExternalIdUpdate();
    }

    @Then("Admin failed to update a new Working Capital Loan Product field {string} with max length data {int} while max allowed is {int}")
    public void updateWorkingCapitalLoanProductWithMaxLengthDataFailed(String fieldName, int maxLengthValue, int maxAllowedLengthValue) {
        String value = Utils.randomStringGenerator(maxLengthValue);
        PutWorkingCapitalLoanProductsProductIdRequest defaultWorkingCapitalLoanProductUpdateRequest = new PutWorkingCapitalLoanProductsProductIdRequest();
        final PutWorkingCapitalLoanProductsProductIdRequest workingCapitalLoanProductUpdateRequestUpdated = setWorkingCapitalLoanProductsUpdateRequest(
                defaultWorkingCapitalLoanProductUpdateRequest, fieldName, value);

        PostWorkingCapitalLoanProductsResponse workingCapitalLoanProductsResponse = testContext()
                .get(TestContextKey.WORKING_CAPITAL_LOAN_PRODUCT_CREATE_RESPONSE);
        Long resourceId = workingCapitalLoanProductsResponse.getResourceId();

        String errorMessage = ErrorMessageHelper.fieldValueMoreMaxLengthAllowedFailure(fieldName, maxAllowedLengthValue);
        checkUpdateWorkingCapitalLoanProductWithInvalidDataFailure(resourceId, workingCapitalLoanProductUpdateRequestUpdated, 400,
                errorMessage);
    }

    @Then("Admin failed to update a new Working Capital Loan Product field {string} with zero incorrect value")
    public void updateWorkingCapitalLoanProductWithZeroValueDataFailed(String fieldName) {
        PostWorkingCapitalLoanProductsResponse workingCapitalLoanProductsResponse = testContext()
                .get(TestContextKey.WORKING_CAPITAL_LOAN_PRODUCT_CREATE_RESPONSE);
        Long resourceId = workingCapitalLoanProductsResponse.getResourceId();
        String errorMessage = ErrorMessageHelper.fieldValueZeroValueFailure(fieldName);
        updateWorkingCapitalLoanProductWithInvalidDataFailure(resourceId, fieldName, "0", errorMessage);
    }

    @Then("Admin failed to update a new Working Capital Loan Product field {string} with invalid data {string} and got an error {string}")
    public void updateWorkingCapitalLoanProductWithInvalidDataFailed(String fieldName, String value, String errorMessage) {
        final PostWorkingCapitalLoanProductsRequest workingCapitalProductForUpdateRequest = testContext()
                .get(TestContextKey.WORKING_CAPITAL_LOAN_PRODUCT_CREATE_REQUEST);
        String workingCapitalProductName = workingCapitalProductForUpdateRequest.getName();
        final PutWorkingCapitalLoanProductsProductIdRequest defaultWorkingCapitalLoanProductUpdateRequest = workingCapitalRequestFactory
                .defaultWorkingCapitalLoanProductRequestUpdate() //
                .name(workingCapitalProductName); //

        PostWorkingCapitalLoanProductsResponse workingCapitalLoanProductsResponse = testContext()
                .get(TestContextKey.WORKING_CAPITAL_LOAN_PRODUCT_CREATE_RESPONSE);
        Long resourceId = workingCapitalLoanProductsResponse.getResourceId();
        final PutWorkingCapitalLoanProductsProductIdRequest workingCapitalLoanProductUpdateRequestUpdated = setWorkingCapitalLoanProductsUpdateRequest(
                defaultWorkingCapitalLoanProductUpdateRequest, fieldName, value);
        checkUpdateWorkingCapitalLoanProductWithInvalidDataFailure(resourceId, workingCapitalLoanProductUpdateRequestUpdated, 400,
                errorMessage);
    }

    @Then("Admin failed to update a new Working Capital Loan Product with breach with field {string} invalid data {string} and got an error {string}")
    public void updateWorkingCapitalLoanProductWithBreachWithInvalidDataFailed(String fieldName, String value, String errorMessage) {
        final PostWorkingCapitalLoanProductsRequest workingCapitalProductForUpdateRequest = testContext()
                .get(TestContextKey.WORKING_CAPITAL_LOAN_PRODUCT_CREATE_REQUEST);
        String workingCapitalProductName = workingCapitalProductForUpdateRequest.getName();
        final Long breachId = workingCapitalProductForUpdateRequest.getBreachId();

        final PutWorkingCapitalLoanProductsProductIdRequest defaultWorkingCapitalLoanProductUpdateRequest = workingCapitalRequestFactory
                .defaultWorkingCapitalLoanProductRequestUpdate() //
                .breachId(breachId) //
                .name(workingCapitalProductName); //

        PostWorkingCapitalLoanProductsResponse workingCapitalLoanProductsResponse = testContext()
                .get(TestContextKey.WORKING_CAPITAL_LOAN_PRODUCT_CREATE_RESPONSE);
        Long resourceId = workingCapitalLoanProductsResponse.getResourceId();
        final PutWorkingCapitalLoanProductsProductIdRequest workingCapitalLoanProductUpdateRequestUpdated = setWorkingCapitalLoanProductsUpdateRequest(
                defaultWorkingCapitalLoanProductUpdateRequest, fieldName, value);
        checkUpdateWorkingCapitalLoanProductWithInvalidDataFailure(resourceId, workingCapitalLoanProductUpdateRequestUpdated, 404,
                errorMessage);
    }

    @Then("Admin failed to update a new Working Capital Loan Product with invalid number of payment allocation rules")
    public void updateWorkingCapitalLoanProductWithInvalidNumberPaymentAllocationFailed() {
        final PutWorkingCapitalLoanProductsProductIdRequest defaultWorkingCapitalLoanProductUpdateRequest = new PutWorkingCapitalLoanProductsProductIdRequest()
                .paymentAllocation(
                        workingCapitalRequestFactory.invalidNumberOfPaymentAllocationRulesForWorkingCapitalLoanProductUpdateRequest());
        PostWorkingCapitalLoanProductsResponse workingCapitalLoanProductsResponse = testContext()
                .get(TestContextKey.WORKING_CAPITAL_LOAN_PRODUCT_CREATE_RESPONSE);
        Long resourceId = workingCapitalLoanProductsResponse.getResourceId();

        String errorMessage = ErrorMessageHelper.paymentAllocationRulesInvalidNumberFailure(4);
        checkUpdateWorkingCapitalLoanProductWithInvalidDataFailure(resourceId, defaultWorkingCapitalLoanProductUpdateRequest, 400,
                errorMessage);
    }

    @Then("Admin failed to update a new Working Capital Loan Product with invalid value of payment allocation rules")
    public void updateWorkingCapitalLoanProductWithInvalidPaymentAllocationFailed() {
        final PutWorkingCapitalLoanProductsProductIdRequest defaultWorkingCapitalLoanProductUpdateRequest = new PutWorkingCapitalLoanProductsProductIdRequest()
                .paymentAllocation(workingCapitalRequestFactory.invalidPaymentAllocationRulesForWorkingCapitalLoanProductUpdateRequest());
        PostWorkingCapitalLoanProductsResponse workingCapitalLoanProductsResponse = testContext()
                .get(TestContextKey.WORKING_CAPITAL_LOAN_PRODUCT_CREATE_RESPONSE);
        Long resourceId = workingCapitalLoanProductsResponse.getResourceId();

        String errorMessage = ErrorMessageHelper.paymentAllocationRulesInvalidValueFailure();
        checkUpdateWorkingCapitalLoanProductWithInvalidDataFailure(resourceId, defaultWorkingCapitalLoanProductUpdateRequest, 400,
                errorMessage);
    }

    @Then("Admin failed to retrieve a Working Capital Loan Product with id {int} that doesn't exist")
    public void retrieveWorkingCapitalLoanProductFailure(Integer productId) {
        CallFailedRuntimeException exception = fail(
                () -> workingCapitalApi().retrieveOneWorkingCapitalLoanProduct(Long.valueOf(productId), Map.of()));
        assertThat(exception.getStatus()).as(ErrorMessageHelper.dateFailureErrorCodeMsg()).isEqualTo(404);
        assertThat(exception.getDeveloperMessage())
                .contains(ErrorMessageHelper.workingCapitalLoanProductIdentifiedDoesNotExistFailure(String.valueOf(productId)));
    }

    @Then("Admin deletes a Working Capital Loan Product")
    public void deleteWorkingCapitalLoanProduct() {
        PostWorkingCapitalLoanProductsResponse workingCapitalLoanProductsResponse = testContext()
                .get(TestContextKey.WORKING_CAPITAL_LOAN_PRODUCT_CREATE_RESPONSE);
        Long resourceId = workingCapitalLoanProductsResponse.getResourceId();

        DeleteWorkingCapitalLoanProductsProductIdResponse deleteWorkingCapitalLoanProductResponse = ok(
                () -> workingCapitalApi().deleteWorkingCapitalLoanProduct(resourceId, Map.of()));
        assertThat(deleteWorkingCapitalLoanProductResponse.getResourceId()).isEqualTo(resourceId);
    }

    @Then("Admin deletes a Working Capital Loan Product via external-id")
    public void deleteWorkingCapitalLoanProductViaExternalId() {
        PostWorkingCapitalLoanProductsResponse workingCapitalLoanProductsResponse = testContext()
                .get(TestContextKey.WORKING_CAPITAL_LOAN_PRODUCT_CREATE_RESPONSE);
        Long resourceId = workingCapitalLoanProductsResponse.getResourceId();

        PutWorkingCapitalLoanProductsProductIdRequest workingCapitalLoanProductsUpdateRequest = testContext()
                .get(TestContextKey.WORKING_CAPITAL_LOAN_PRODUCT_UPDATE_REQUEST);
        String externalId = workingCapitalLoanProductsUpdateRequest.getExternalId();

        DeleteWorkingCapitalLoanProductsProductIdResponse deleteWorkingCapitalLoanProductResponse = ok(
                () -> workingCapitalApi().deleteWorkingCapitalLoanProductByExternalId(externalId, Map.of()));
        assertThat(deleteWorkingCapitalLoanProductResponse.getResourceId()).isEqualTo(resourceId);
    }

    @Then("Admin checks a Working Capital Loan Product is deleted and doesn't exist")
    public void checkWorkingCapitalLoanProductIsDeleted() {
        PostWorkingCapitalLoanProductsResponse workingCapitalLoanProductsResponse = testContext()
                .get(TestContextKey.WORKING_CAPITAL_LOAN_PRODUCT_CREATE_RESPONSE);
        Long resourceId = workingCapitalLoanProductsResponse.getResourceId();
        checkWorkingCapitalLoanProductDeleteFailure(resourceId);
    }

    @Then("Admin failed to delete a Working Capital Loan Product with id {int} that doesn't exist")
    public void checkWorkingCapitalLoanProductIsDeleted(Integer productId) {
        checkWorkingCapitalLoanProductDeleteFailure(Long.valueOf(productId));
    }

    @Then("Admin checks a Working Capital Loan Product is deleted and doesn't exist via external-id")
    public void checkWorkingCapitalLoanProductIsDeletedViaExternalId() {
        PutWorkingCapitalLoanProductsProductIdRequest workingCapitalLoanProductsUpdateRequest = testContext()
                .get(TestContextKey.WORKING_CAPITAL_LOAN_PRODUCT_UPDATE_REQUEST);
        String externalId = workingCapitalLoanProductsUpdateRequest.getExternalId();

        CallFailedRuntimeException exception = fail(
                () -> workingCapitalApi().retrieveOneWorkingCapitalLoanProductByExternalId(externalId, Map.of()));
        assertThat(exception.getStatus()).as(ErrorMessageHelper.dateFailureErrorCodeMsg()).isEqualTo(404);
        assertThat(exception.getDeveloperMessage())
                .contains(ErrorMessageHelper.workingCapitalLoanProductIdentifiedDoesNotExistFailure(String.valueOf(externalId)));
    }

    @When("Admin creates a new Working Capital Loan Product with accounting rule {string}")
    public void createWorkingCapitalLoanProductWithAccountingRule(final String accountingRule) {
        final String workingCapitalProductDefaultName = DefaultWorkingCapitalLoanProduct.WCLP.getName()
                + Utils.randomStringGenerator("_", 10);
        final PostWorkingCapitalLoanProductsRequest request;
        if ("CASH_BASED".equals(accountingRule)) {
            request = workingCapitalRequestFactory.defaultWorkingCapitalLoanProductRequestWithCashAccounting()//
                    .name(workingCapitalProductDefaultName);
        } else {
            request = workingCapitalRequestFactory.defaultWorkingCapitalLoanProductRequest()//
                    .name(workingCapitalProductDefaultName)//
                    .accountingRule(AccountingRuleEnum.valueOf(accountingRule));
        }
        final PostWorkingCapitalLoanProductsResponse response = createWorkingCapitalLoanProduct(request);
        testContext().set(TestContextKey.WORKING_CAPITAL_LOAN_PRODUCT_CREATE_RESPONSE, response);
        testContext().set(TestContextKey.WORKING_CAPITAL_LOAN_PRODUCT_CREATE_REQUEST, request);
    }

    @When("Admin creates a new Working Capital Loan Product with Cash based accounting for GL mapping verification")
    public void createWorkingCapitalLoanProductWithCashAccountingForGLMappingVerification() {
        final String workingCapitalProductDefaultName = DefaultWorkingCapitalLoanProduct.WCLP.getName()
                + Utils.randomStringGenerator("_", 10);
        final PostWorkingCapitalLoanProductsRequest request = workingCapitalRequestFactory
                .defaultWorkingCapitalLoanProductRequestWithDistinctCashAccountingMappings()//
                .name(workingCapitalProductDefaultName);

        final PostWorkingCapitalLoanProductsResponse response = createWorkingCapitalLoanProduct(request);
        testContext().set(TestContextKey.WORKING_CAPITAL_LOAN_PRODUCT_CREATE_RESPONSE, response);
        testContext().set(TestContextKey.WORKING_CAPITAL_LOAN_PRODUCT_CREATE_REQUEST, request);
    }

    @Then("Admin verifies Working Capital Loan Product has accounting rule {string}")
    public void verifyWorkingCapitalLoanProductHasAccountingRule(final String expectedAccountingRule) {
        final PostWorkingCapitalLoanProductsResponse createResponse = testContext()
                .get(TestContextKey.WORKING_CAPITAL_LOAN_PRODUCT_CREATE_RESPONSE);
        final Long resourceId = createResponse.getResourceId();

        final GetWorkingCapitalLoanProductsProductIdResponse product = workingCapitalApi().retrieveOneWorkingCapitalLoanProduct(resourceId,
                Map.of());

        final SoftAssertions assertions = new SoftAssertions();
        assertions.assertThat(product.getAccountingRule()).isNotNull();
        assertions.assertThat(product.getAccountingRule().getId()).isEqualTo(expectedAccountingRule);

        if ("CASH_BASED".equals(expectedAccountingRule)) {
            assertions.assertThat(product.getAccountingMappings()).isNotNull();
            assertions.assertThat(product.getAccountingMappings()).isNotEmpty();
            assertions.assertThat(product.getAccountingMappings()).containsKey("fundSourceAccount");
            assertions.assertThat(product.getAccountingMappings()).containsKey("loanPortfolioAccount");
            assertions.assertThat(product.getAccountingMappings()).containsKey("transfersInSuspenseAccount");
            assertions.assertThat(product.getAccountingMappings()).containsKey("deferredIncomeLiabilityAccount");
            assertions.assertThat(product.getAccountingMappings()).containsKey("incomeFromDiscountFeeAccount");
            assertions.assertThat(product.getAccountingMappings()).containsKey("incomeFromFeeAccount");
            assertions.assertThat(product.getAccountingMappings()).containsKey("incomeFromPenaltyAccount");
            assertions.assertThat(product.getAccountingMappings()).containsKey("incomeFromRecoveryAccount");
            assertions.assertThat(product.getAccountingMappings()).containsKey("writeOffAccount");
            assertions.assertThat(product.getAccountingMappings()).containsKey("overpaymentLiabilityAccount");
        } else {
            assertions.assertThat(product.getAccountingMappings()).isNullOrEmpty();
        }
        assertions.assertAll();
    }

    @Then("Admin failed to create a new Working Capital Loan Product with Cash based accounting and missing required GL accounts")
    public void createWorkingCapitalLoanProductWithCashAccountingMissingRequiredAccountsFailed() {
        final String workingCapitalProductDefaultName = DefaultWorkingCapitalLoanProduct.WCLP.getName()
                + Utils.randomStringGenerator("_", 10);
        final PostWorkingCapitalLoanProductsRequest request = workingCapitalRequestFactory.defaultWorkingCapitalLoanProductRequest()//
                .name(workingCapitalProductDefaultName)//
                .accountingRule(AccountingRuleEnum.CASH_BASED);
        // Missing all required GL account IDs

        final CallFailedRuntimeException exception = fail(() -> workingCapitalApi().createWorkingCapitalLoanProduct(request, Map.of()));
        assertThat(exception.getStatus()).as(ErrorMessageHelper.incorrectExpectedValueInResponse()).isEqualTo(400);
    }

    @When("Admin updates Working Capital Loan Product accounting rule from None to Cash based")
    public void updateWorkingCapitalLoanProductAccountingNoneToCash() {
        final PostWorkingCapitalLoanProductsResponse createResponse = testContext()
                .get(TestContextKey.WORKING_CAPITAL_LOAN_PRODUCT_CREATE_RESPONSE);
        final Long resourceId = createResponse.getResourceId();

        final PostWorkingCapitalLoanProductsRequest cashRequest = workingCapitalRequestFactory
                .defaultWorkingCapitalLoanProductRequestWithCashAccounting();
        final PutWorkingCapitalLoanProductsProductIdRequest updateRequest = buildCashBasedUpdateRequest(cashRequest);

        final PutWorkingCapitalLoanProductsProductIdResponse response = ok(
                () -> workingCapitalApi().updateWorkingCapitalLoanProduct(resourceId, updateRequest, Map.of()));

        testContext().set(TestContextKey.WORKING_CAPITAL_LOAN_PRODUCT_UPDATE_RESPONSE, response);
        testContext().set(TestContextKey.WORKING_CAPITAL_LOAN_PRODUCT_UPDATE_REQUEST, updateRequest);
    }

    @When("Admin updates Working Capital Loan Product accounting rule from Cash based to None")
    public void updateWorkingCapitalLoanProductAccountingCashToNone() {
        final PostWorkingCapitalLoanProductsResponse createResponse = testContext()
                .get(TestContextKey.WORKING_CAPITAL_LOAN_PRODUCT_CREATE_RESPONSE);
        final Long resourceId = createResponse.getResourceId();

        final PutWorkingCapitalLoanProductsProductIdRequest updateRequest = new PutWorkingCapitalLoanProductsProductIdRequest()//
                .locale("en")//
                .accountingRule(PutWorkingCapitalLoanProductsProductIdRequest.AccountingRuleEnum.NONE);

        final PutWorkingCapitalLoanProductsProductIdResponse response = ok(
                () -> workingCapitalApi().updateWorkingCapitalLoanProduct(resourceId, updateRequest, Map.of()));

        testContext().set(TestContextKey.WORKING_CAPITAL_LOAN_PRODUCT_UPDATE_RESPONSE, response);
        testContext().set(TestContextKey.WORKING_CAPITAL_LOAN_PRODUCT_UPDATE_REQUEST, updateRequest);
    }

    @When("Admin updates GL account mappings on existing Cash based Working Capital Loan Product")
    public void updateWCGLAccountMappingsOnExistingCashBasedProduct() {
        final PostWorkingCapitalLoanProductsResponse createResponse = testContext()
                .get(TestContextKey.WORKING_CAPITAL_LOAN_PRODUCT_CREATE_RESPONSE);
        final Long resourceId = createResponse.getResourceId();

        final PostWorkingCapitalLoanProductsRequest cashRequest = workingCapitalRequestFactory
                .defaultWorkingCapitalLoanProductRequestWithCashAccounting();

        final PutWorkingCapitalLoanProductsProductIdRequest updateRequest = new PutWorkingCapitalLoanProductsProductIdRequest()//
                .locale("en")//
                .writeOffAccountId(cashRequest.getChargeOffExpenseAccountId());

        final PutWorkingCapitalLoanProductsProductIdResponse response = ok(
                () -> workingCapitalApi().updateWorkingCapitalLoanProduct(resourceId, updateRequest, Map.of()));

        testContext().set(TestContextKey.WORKING_CAPITAL_LOAN_PRODUCT_UPDATE_RESPONSE, response);
        testContext().set(TestContextKey.WORKING_CAPITAL_LOAN_PRODUCT_UPDATE_REQUEST, updateRequest);
    }

    @Then("Admin verifies Working Capital Loan Product template has accounting options")
    public void verifyWorkingCapitalLoanProductTemplateHasAccountingOptions() {
        final GetWorkingCapitalLoanProductsTemplateResponse template = workingCapitalApi()
                .retrieveTemplateWorkingCapitalLoanProduct(Map.of());
        assertThat(template.getAccountingRuleOptions()).isNotNull();
        assertThat(template.getAccountingRuleOptions()).hasSizeGreaterThanOrEqualTo(2);
        assertThat(template.getAccountingMappingOptions()).isNotNull();
        assertThat(template.getAccountingMappingOptions()).isNotEmpty();
    }

    @Then("Admin deletes Working Capital Loan Product and verifies GL account mappings are cleaned up")
    public void deleteWorkingCapitalLoanProductAndVerifyMappingsCleanedUp() {
        final PostWorkingCapitalLoanProductsResponse createResponse = testContext()
                .get(TestContextKey.WORKING_CAPITAL_LOAN_PRODUCT_CREATE_RESPONSE);
        final Long resourceId = createResponse.getResourceId();

        final DeleteWorkingCapitalLoanProductsProductIdResponse deleteResponse = ok(
                () -> workingCapitalApi().deleteWorkingCapitalLoanProduct(resourceId, Map.of()));
        assertThat(deleteResponse.getResourceId()).isEqualTo(resourceId);

        final CallFailedRuntimeException exception = fail(
                () -> workingCapitalApi().retrieveOneWorkingCapitalLoanProduct(resourceId, Map.of()));
        assertThat(exception.getStatus()).isEqualTo(404);
    }

    @Then("Admin verifies Working Capital Loan Product GL account mapping values match the request")
    public void verifyWorkingCapitalLoanProductGLAccountMappingValues() {
        final PostWorkingCapitalLoanProductsResponse createResponse = testContext()
                .get(TestContextKey.WORKING_CAPITAL_LOAN_PRODUCT_CREATE_RESPONSE);
        final PostWorkingCapitalLoanProductsRequest createRequest = testContext()
                .get(TestContextKey.WORKING_CAPITAL_LOAN_PRODUCT_CREATE_REQUEST);
        final Long resourceId = createResponse.getResourceId();

        final GetWorkingCapitalLoanProductsProductIdResponse product = workingCapitalApi().retrieveOneWorkingCapitalLoanProduct(resourceId,
                Map.of());

        final SoftAssertions assertions = new SoftAssertions();
        assertions.assertThat(product.getAccountingRule()).isNotNull();
        assertions.assertThat(product.getAccountingRule().getId()).isEqualTo("CASH_BASED");

        final Map<String, ?> mappings = product.getAccountingMappings();
        final List<WCGLAccountMapping> expectedMappings = WCGLAccountMapping.all().stream()
                .filter(mapping -> mapping.required() || mapping.extractor().apply(createRequest) != null).toList();

        assertions.assertThat(mappings).isNotNull().isNotEmpty().hasSize(expectedMappings.size())
                .containsOnlyKeys(expectedMappings.stream().map(WCGLAccountMapping::responseKey).toArray(String[]::new));

        for (final WCGLAccountMapping mapping : expectedMappings) {
            assertGLAccountMappingId(assertions, mappings, mapping.responseKey(), mapping.extractor().apply(createRequest));
        }

        assertions.assertAll();
    }

    @Then("Admin verifies Working Capital Loan Product has no accounting mappings")
    public void verifyWorkingCapitalLoanProductHasNoAccountingMappings() {
        final PostWorkingCapitalLoanProductsResponse createResponse = testContext()
                .get(TestContextKey.WORKING_CAPITAL_LOAN_PRODUCT_CREATE_RESPONSE);
        final Long resourceId = createResponse.getResourceId();

        final GetWorkingCapitalLoanProductsProductIdResponse product = workingCapitalApi().retrieveOneWorkingCapitalLoanProduct(resourceId,
                Map.of());

        assertThat(product.getAccountingRule()).isNotNull();
        assertThat(product.getAccountingRule().getId()).isEqualTo("NONE");
        assertThat(product.getAccountingMappings()).isNullOrEmpty();
    }

    @Then("Admin failed to create a Working Capital Loan Product with Cash based accounting and non-existent GL account ID with status {int}")
    public void createWorkingCapitalLoanProductWithNonExistentGLAccountFailed(final int expectedStatus) {
        final String name = DefaultWorkingCapitalLoanProduct.WCLP.getName() + Utils.randomStringGenerator("_", 10);
        final PostWorkingCapitalLoanProductsRequest request = workingCapitalRequestFactory
                .defaultWorkingCapitalLoanProductRequestWithCashAccounting()//
                .name(name)//
                .fundSourceAccountId(NON_EXISTENT_GL_ACCOUNT_ID);

        final CallFailedRuntimeException exception = fail(() -> workingCapitalApi().createWorkingCapitalLoanProduct(request, Map.of()));
        assertThat(exception.getStatus()).as(ErrorMessageHelper.incorrectExpectedValueInResponse()).isEqualTo(expectedStatus);
    }

    @Then("Admin failed to update Working Capital Loan Product to Cash based without required GL accounts with status {int}")
    public void updateWorkingCapitalLoanProductToCashBasedWithoutRequiredGLAccountsFailed(final int expectedStatus) {
        final PostWorkingCapitalLoanProductsResponse createResponse = testContext()
                .get(TestContextKey.WORKING_CAPITAL_LOAN_PRODUCT_CREATE_RESPONSE);
        final Long resourceId = createResponse.getResourceId();

        final PutWorkingCapitalLoanProductsProductIdRequest updateRequest = new PutWorkingCapitalLoanProductsProductIdRequest()//
                .locale("en")//
                .accountingRule(PutWorkingCapitalLoanProductsProductIdRequest.AccountingRuleEnum.CASH_BASED);

        final CallFailedRuntimeException exception = fail(
                () -> workingCapitalApi().updateWorkingCapitalLoanProduct(resourceId, updateRequest, Map.of()));
        assertThat(exception.getStatus()).as(ErrorMessageHelper.incorrectExpectedValueInResponse()).isEqualTo(expectedStatus);
    }

    @When("Admin updates writeOff GL account on Cash based Working Capital Loan Product")
    public void updateWriteOffGLAccountOnCashBasedProduct() {
        final PostWorkingCapitalLoanProductsResponse createResponse = testContext()
                .get(TestContextKey.WORKING_CAPITAL_LOAN_PRODUCT_CREATE_RESPONSE);
        final PostWorkingCapitalLoanProductsRequest originalRequest = testContext()
                .get(TestContextKey.WORKING_CAPITAL_LOAN_PRODUCT_CREATE_REQUEST);
        final Long resourceId = createResponse.getResourceId();

        final Long newWriteOffAccountId = accountTypeResolver.resolve(DefaultAccountType.CREDIT_LOSS_BAD_DEBT);

        // Validator requires all mandatory GL accounts when accountingRule is present — re-send originals
        final PutWorkingCapitalLoanProductsProductIdRequest updateRequest = buildCashBasedUpdateRequest(originalRequest)//
                .writeOffAccountId(newWriteOffAccountId);

        ok(() -> workingCapitalApi().updateWorkingCapitalLoanProduct(resourceId, updateRequest, Map.of()));
        testContext().set(TestContextKey.WORKING_CAPITAL_LOAN_PRODUCT_UPDATE_REQUEST, updateRequest);
    }

    @Then("Admin verifies Working Capital Loan Product writeOff GL account was updated")
    public void verifyWriteOffGLAccountWasUpdated() {
        final PostWorkingCapitalLoanProductsResponse createResponse = testContext()
                .get(TestContextKey.WORKING_CAPITAL_LOAN_PRODUCT_CREATE_RESPONSE);
        final PutWorkingCapitalLoanProductsProductIdRequest updateRequest = testContext()
                .get(TestContextKey.WORKING_CAPITAL_LOAN_PRODUCT_UPDATE_REQUEST);
        final Long resourceId = createResponse.getResourceId();

        final GetWorkingCapitalLoanProductsProductIdResponse product = workingCapitalApi().retrieveOneWorkingCapitalLoanProduct(resourceId,
                Map.of());

        final Map<String, ?> mappings = product.getAccountingMappings();
        assertThat(mappings).isNotNull();
        assertGLAccountMappingId(mappings, WCGLAccountMapping.WRITE_OFF.responseKey(), updateRequest.getWriteOffAccountId());
    }

    @Then("Admin verifies Working Capital Loan Product list contains the product with accounting rule {string}")
    public void verifyWorkingCapitalLoanProductListContainsProductWithAccountingRule(final String expectedAccountingRule) {
        final PostWorkingCapitalLoanProductsResponse createResponse = testContext()
                .get(TestContextKey.WORKING_CAPITAL_LOAN_PRODUCT_CREATE_RESPONSE);
        final Long resourceId = createResponse.getResourceId();

        final List<GetWorkingCapitalLoanProductsResponse> allProducts = ok(
                () -> workingCapitalApi().retrieveAllWorkingCapitalLoanProducts(Map.of()));

        final GetWorkingCapitalLoanProductsResponse found = allProducts.stream()//
                .filter(p -> resourceId.equals(p.getId()))//
                .findFirst()//
                .orElse(null);

        assertThat(found).as("Product with id %d should be present in list", resourceId).isNotNull();
        assertThat(found.getAccountingRule()).isNotNull();
        assertThat(found.getAccountingRule().getId()).isEqualTo(expectedAccountingRule);
    }

    @Then("Admin failed to create a Working Capital Loan Product with wrong GL account type for loanPortfolio with status {int}")
    public void createWorkingCapitalLoanProductWithWrongGLAccountTypeFailed(final int expectedStatus) {
        final String name = DefaultWorkingCapitalLoanProduct.WCLP.getName() + Utils.randomStringGenerator("_", 10);
        final Long incomeAccountId = accountTypeResolver.resolve(DefaultAccountType.INTEREST_INCOME);

        final PostWorkingCapitalLoanProductsRequest request = workingCapitalRequestFactory
                .defaultWorkingCapitalLoanProductRequestWithCashAccounting()//
                .name(name)//
                .loanPortfolioAccountId(incomeAccountId);

        final CallFailedRuntimeException exception = fail(() -> workingCapitalApi().createWorkingCapitalLoanProduct(request, Map.of()));
        assertThat(exception.getStatus()).as(ErrorMessageHelper.incorrectExpectedValueInResponse()).isEqualTo(expectedStatus);
    }

    @Then("Admin verifies Working Capital Loan Product template has NONE and CASH_BASED accounting rule options")
    public void verifyWorkingCapitalLoanProductTemplateAccountingRuleOptions() {
        final GetWorkingCapitalLoanProductsTemplateResponse template = workingCapitalApi()
                .retrieveTemplateWorkingCapitalLoanProduct(Map.of());

        assertThat(template.getAccountingRuleOptions()).isNotNull();
        assertThat(template.getAccountingRuleOptions()).hasSizeGreaterThanOrEqualTo(2);

        final List<String> ruleIds = template.getAccountingRuleOptions().stream()//
                .map(StringEnumOptionData::getId)//
                .toList();
        assertThat(ruleIds).contains("NONE", "CASH_BASED");

        assertThat(template.getAccountingMappingOptions()).isNotNull();
        assertThat(template.getAccountingMappingOptions()).isNotEmpty();
    }

    private void assertGLAccountMappingId(final SoftAssertions assertions, final Map<String, ?> mappings, final String key,
            final Long expectedAccountId) {
        assertions.assertThat(mappings).as("accountingMappings should contain key: %s", key).containsKey(key);
        final Object value = mappings.get(key);
        final Long actualId = extractAccountId(value);
        if (actualId == null) {
            assertions.fail("accountingMappings[%s]: could not extract id from %s", key,
                    value != null ? value.getClass().getSimpleName() : "null");
            return;
        }
        assertions.assertThat(actualId).as("GL account id for %s", key).isEqualTo(expectedAccountId);
    }

    private Long extractAccountId(final Object accountValue) {
        if (accountValue instanceof Map<?, ?> map) {
            final Object id = map.get("id");
            return id instanceof Number n ? n.longValue() : null;
        }
        // Handle strongly-typed GLAccountData (CI-generated client)
        try {
            final var method = accountValue.getClass().getMethod("getId");
            final Object id = method.invoke(accountValue);
            return id instanceof Number n ? n.longValue() : null;
        } catch (Exception e) {
            return null;
        }
    }

    private void assertGLAccountMappingId(final Map<String, ?> mappings, final String key, final Long expectedAccountId) {
        final SoftAssertions assertions = new SoftAssertions();
        assertGLAccountMappingId(assertions, mappings, key, expectedAccountId);
        assertions.assertAll();
    }

    private Long getWorkingCapitalLoanResourceId() {
        PostWorkingCapitalLoansResponse response = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        return response.getResourceId();
    }

    @When("Admin makes Internal Payment {string} on {string}")
    public void internalPayWCLoan(String amount, String transactionDate) {
        Long resourceId = getWorkingCapitalLoanResourceId();
        fineractFeignClient.workingCapitalLoans().payment(resourceId, new InternalWorkingCapitalLoanPaymentRequest()
                .amount(BigDecimal.valueOf(Double.parseDouble(amount))).transactionDate(LocalDate.parse(transactionDate)));
    }

    @Then("Delinquency Tag History for Working Capital loan has lines:")
    public void checkDelinquencyHistory(final DataTable table) {
        Long resourceId = getWorkingCapitalLoanResourceId();
        List<GetWorkingCapitalLoanDelinquencyRangeScheduleTagHistoryResponse> actualLines = ok(
                () -> fineractFeignClient.workingCapitalLoans().getDelinquencyRangeScheduleTagHistoryById(resourceId));

        // Sort by addedOnDate (descending), then by periodNumber (descending)
        actualLines.sort((a, b) -> {
            int dateCompare = b.getAddedOnDate().compareTo(a.getAddedOnDate());
            if (dateCompare != 0) {
                return dateCompare;
            }
            return b.getPeriodNumber().compareTo(a.getPeriodNumber());
        });

        log.debug("Sorted Loan Delinquency History: {}", actualLines);
        List<List<String>> rows = table.asLists();
        Assertions.assertEquals(rows.size() - 1, actualLines.size());
        for (int i = 0; i < rows.size() - 1; i++) {
            GetWorkingCapitalLoanDelinquencyRangeScheduleTagHistoryResponse actual = actualLines.get(i);
            Assertions.assertNotNull(actual);
            List<String> expected = rows.get(i + 1);
            Assertions.assertEquals(expected.get(0), actual.getPeriodNumber() != null ? actual.getPeriodNumber().toString() : null);
            Assertions.assertEquals(expected.get(1), actual.getAddedOnDate() != null ? actual.getAddedOnDate().toString() : null);
            Assertions.assertEquals(expected.get(2), actual.getLiftedOnDate() != null ? actual.getLiftedOnDate().toString() : null);

            Assertions.assertNotNull(actual.getDelinquencyRange());
            Assertions.assertEquals(expected.get(3), actual.getDelinquencyRange().getClassification());
            Assertions.assertEquals(expected.get(4), actual.getDelinquencyRange().getMinimumAgeDays() == null ? null
                    : actual.getDelinquencyRange().getMinimumAgeDays().toString());
            Assertions.assertEquals(expected.get(5), actual.getDelinquencyRange().getMaximumAgeDays() == null ? null
                    : actual.getDelinquencyRange().getMaximumAgeDays().toString());
        }
    }

    public PostWorkingCapitalLoanProductsResponse createWorkingCapitalLoanProduct(
            PostWorkingCapitalLoanProductsRequest workingCapitalProductRequest) {
        String workingCapitalProductName = workingCapitalProductRequest.getName();
        log.debug("Creating new working capital product: {}", workingCapitalProductName);
        try {
            PostWorkingCapitalLoanProductsResponse response = ok(
                    () -> workingCapitalApi().createWorkingCapitalLoanProduct(workingCapitalProductRequest, Map.of()));
            log.debug("Successfully created working capital product '{}' with ID: {}", workingCapitalProductName, response.getResourceId());
            return response;
        } catch (Exception e) {
            log.error("FAILED to create working capital product '{}'", workingCapitalProductName, e);
            throw e;
        }
    }

    private PutWorkingCapitalLoanProductsProductIdRequest buildCashBasedUpdateRequest(final PostWorkingCapitalLoanProductsRequest source) {
        return new PutWorkingCapitalLoanProductsProductIdRequest()//
                .locale("en")//
                .accountingRule(PutWorkingCapitalLoanProductsProductIdRequest.AccountingRuleEnum.CASH_BASED)//
                .fundSourceAccountId(source.getFundSourceAccountId())//
                .loanPortfolioAccountId(source.getLoanPortfolioAccountId())//
                .transfersInSuspenseAccountId(source.getTransfersInSuspenseAccountId())//
                .deferredIncomeLiabilityAccountId(source.getDeferredIncomeLiabilityAccountId())//
                .incomeFromDiscountFeeAccountId(source.getIncomeFromDiscountFeeAccountId())//
                .incomeFromFeeAccountId(source.getIncomeFromFeeAccountId())//
                .incomeFromPenaltyAccountId(source.getIncomeFromPenaltyAccountId())//
                .incomeFromRecoveryAccountId(source.getIncomeFromRecoveryAccountId())//
                .writeOffAccountId(source.getWriteOffAccountId())//
                .overpaymentLiabilityAccountId(source.getOverpaymentLiabilityAccountId());
    }

    public void checkWorkingCapitalLoanProductCreate() {
        PostWorkingCapitalLoanProductsRequest workingCapitalLoanProductCreateRequest = testContext()
                .get(TestContextKey.WORKING_CAPITAL_LOAN_PRODUCT_CREATE_REQUEST);

        PostWorkingCapitalLoanProductsResponse workingCapitalLoanProductResponse = testContext()
                .get(TestContextKey.WORKING_CAPITAL_LOAN_PRODUCT_CREATE_RESPONSE);
        Long resourceId = workingCapitalLoanProductResponse.getResourceId();
        GetWorkingCapitalLoanProductsProductIdResponse getWorkingCapitalProductResponse = workingCapitalApi()
                .retrieveOneWorkingCapitalLoanProduct(resourceId, Map.of());
        checkWorkingCapitalLoanProductCreate(workingCapitalLoanProductCreateRequest, getWorkingCapitalProductResponse);
    }

    public void checkWorkingCapitalLoanProductWithExternalIdCreate() {
        PostWorkingCapitalLoanProductsRequest workingCapitalLoanProductCreateRequest = testContext()
                .get(TestContextKey.WORKING_CAPITAL_LOAN_PRODUCT_CREATE_REQUEST);
        String externalId = workingCapitalLoanProductCreateRequest.getExternalId();

        GetWorkingCapitalLoanProductsProductIdResponse getWorkingCapitalProductResponse = workingCapitalApi()
                .retrieveOneWorkingCapitalLoanProductByExternalId(externalId, Map.of());
        checkWorkingCapitalLoanProductCreate(workingCapitalLoanProductCreateRequest, getWorkingCapitalProductResponse);
    }

    public void checkWorkingCapitalLoanProductCreate(PostWorkingCapitalLoanProductsRequest workingCapitalLoanProductCreateRequest,
            GetWorkingCapitalLoanProductsProductIdResponse getWorkingCapitalProductResponse) {
        PostWorkingCapitalLoanProductsResponse workingCapitalLoanProductResponse = testContext()
                .get(TestContextKey.WORKING_CAPITAL_LOAN_PRODUCT_CREATE_RESPONSE);
        Long resourceId = workingCapitalLoanProductResponse.getResourceId();

        SoftAssertions assertions = new SoftAssertions();
        assertions.assertThat(workingCapitalLoanProductCreateRequest.getName()).isEqualTo(getWorkingCapitalProductResponse.getName());
        assertions.assertThat(workingCapitalLoanProductCreateRequest.getShortName())
                .isEqualTo(getWorkingCapitalProductResponse.getShortName());
        assertions.assertThat(workingCapitalLoanProductCreateRequest.getDescription())
                .isEqualTo(getWorkingCapitalProductResponse.getDescription());
        assertions.assertThat(workingCapitalLoanProductCreateRequest.getExternalId())
                .isEqualTo(getWorkingCapitalProductResponse.getExternalId());
        assertions.assertThat(resourceId).isEqualTo(getWorkingCapitalProductResponse.getId());

        // check currency
        assertions.assertThat(getWorkingCapitalProductResponse.getCurrency()).isNotNull();
        assertions.assertThat(workingCapitalLoanProductCreateRequest.getCurrencyCode())
                .isEqualTo(getWorkingCapitalProductResponse.getCurrency().getCode());
        assertions.assertThat(workingCapitalLoanProductCreateRequest.getDigitsAfterDecimal())
                .isEqualTo(getWorkingCapitalProductResponse.getCurrency().getDecimalPlaces());
        assertions.assertThat(workingCapitalLoanProductCreateRequest.getInMultiplesOf())
                .isEqualTo(getWorkingCapitalProductResponse.getCurrency().getInMultiplesOf());
        assertions.assertThat(workingCapitalLoanProductCreateRequest.getStartDate())
                .isEqualTo(getWorkingCapitalProductResponse.getStartDate());
        assertions.assertThat(workingCapitalLoanProductCreateRequest.getCloseDate())
                .isEqualTo(getWorkingCapitalProductResponse.getCloseDate());

        assertions.assertThat(getWorkingCapitalProductResponse.getAmortizationType()).isNotNull();
        assertions.assertThat(workingCapitalLoanProductCreateRequest.getAmortizationType()).isNotNull();
        assertions.assertThat(workingCapitalLoanProductCreateRequest.getAmortizationType().getValue())
                .isEqualTo(getWorkingCapitalProductResponse.getAmortizationType().getCode());
        assertions.assertThat(workingCapitalLoanProductCreateRequest.getNpvDayCount())
                .isEqualTo(getWorkingCapitalProductResponse.getNpvDayCount());
        if (workingCapitalLoanProductCreateRequest.getDelinquencyBucketId() != null) {
            assertions.assertThat(workingCapitalLoanProductCreateRequest.getDelinquencyBucketId())
                    .isEqualTo(getWorkingCapitalProductResponse.getDelinquencyBucket().getId());
        }
        if (workingCapitalLoanProductCreateRequest.getDelinquencyGraceDays() != null) {
            assertions.assertThat(workingCapitalLoanProductCreateRequest.getDelinquencyGraceDays())
                    .isEqualTo(getWorkingCapitalProductResponse.getDelinquencyGraceDays());
        }
        if (workingCapitalLoanProductCreateRequest.getDelinquencyStartType() != null) {
            assertions.assertThat(workingCapitalLoanProductCreateRequest.getDelinquencyStartType())
                    .isEqualTo(getWorkingCapitalProductResponse.getDelinquencyStartType().getCode());
        }
        if (workingCapitalLoanProductCreateRequest.getBreachId() != null) {
            assertions.assertThat(workingCapitalLoanProductCreateRequest.getBreachId())
                    .isEqualTo(getWorkingCapitalProductResponse.getBreach().getId());
        }
        if (workingCapitalLoanProductCreateRequest.getNearBreachId() != null) {
            assertions.assertThat(workingCapitalLoanProductCreateRequest.getNearBreachId())
                    .isEqualTo(getWorkingCapitalProductResponse.getNearBreach().getId());
        }
        assertions.assertThat(getWorkingCapitalProductResponse.getRepaymentEvery()).isNotNull();
        assertions.assertThat(workingCapitalLoanProductCreateRequest.getRepaymentEvery()).isNotNull();
        assertions.assertThat(
                workingCapitalLoanProductCreateRequest.getRepaymentEvery().compareTo(getWorkingCapitalProductResponse.getRepaymentEvery()))
                .isEqualTo(0);
        assertions.assertThat(workingCapitalLoanProductCreateRequest.getRepaymentFrequencyType()).isNotNull();
        assertions.assertThat(getWorkingCapitalProductResponse.getRepaymentFrequencyType()).isNotNull();
        assertions.assertThat(workingCapitalLoanProductCreateRequest.getRepaymentFrequencyType().getValue())
                .isEqualTo(getWorkingCapitalProductResponse.getRepaymentFrequencyType().getCode());
        assertions.assertThat(workingCapitalLoanProductCreateRequest.getPeriodPaymentRate()).isNotNull();
        assertions.assertThat(workingCapitalLoanProductCreateRequest.getPeriodPaymentRate()
                .compareTo(getWorkingCapitalProductResponse.getPeriodPaymentRate())).isEqualTo(0);
        assertions.assertThat(workingCapitalLoanProductCreateRequest.getMinPeriodPaymentRate())
                .isEqualTo(getWorkingCapitalProductResponse.getMinPeriodPaymentRate());
        assertions.assertThat(workingCapitalLoanProductCreateRequest.getMaxPeriodPaymentRate())
                .isEqualTo(getWorkingCapitalProductResponse.getMaxPeriodPaymentRate());
        if (workingCapitalLoanProductCreateRequest.getDiscount() != null) {
            assertions
                    .assertThat(
                            workingCapitalLoanProductCreateRequest.getDiscount().compareTo(getWorkingCapitalProductResponse.getDiscount()))
                    .isEqualTo(0);
        }

        // check payment allocation rules
        assertions.assertThat(workingCapitalLoanProductCreateRequest.getPaymentAllocation()).isNotNull();
        workingCapitalLoanProductCreateRequest.getPaymentAllocation().forEach(paymentAllocation -> {
            assertThat(getWorkingCapitalProductResponse.getPaymentAllocation()).isNotNull();
            GetPaymentAllocation getPaymentAllocation = getWorkingCapitalProductResponse.getPaymentAllocation().stream()
                    .filter(paymentAllocationSearched -> {
                        assertThat(paymentAllocation.getTransactionType()).isNotNull();
                        return paymentAllocation.getTransactionType().getValue().equals(paymentAllocationSearched.getTransactionType());
                    }).findFirst().orElseThrow(() -> new RuntimeException("No paymentAllocation is found!"));
            assertions.assertThat(paymentAllocation.getPaymentAllocationOrder())
                    .containsAll(getPaymentAllocation.getPaymentAllocationOrder());
        });

        assertions.assertThat(workingCapitalLoanProductCreateRequest.getPrincipal()).isNotNull();
        assertions
                .assertThat(
                        workingCapitalLoanProductCreateRequest.getPrincipal().compareTo(getWorkingCapitalProductResponse.getPrincipal()))
                .isEqualTo(0);
        assertions.assertThat(workingCapitalLoanProductCreateRequest.getMaxPrincipal()).isNotNull();
        assertions.assertThat(
                workingCapitalLoanProductCreateRequest.getMaxPrincipal().compareTo(getWorkingCapitalProductResponse.getMaxPrincipal()))
                .isEqualTo(0);
        assertions.assertThat(workingCapitalLoanProductCreateRequest.getMinPrincipal()).isNotNull();
        assertions.assertThat(
                workingCapitalLoanProductCreateRequest.getMinPrincipal().compareTo(getWorkingCapitalProductResponse.getMinPrincipal()))
                .isEqualTo(0);

        if (workingCapitalLoanProductCreateRequest.getAllowAttributeOverrides() != null) {
            PostAllowAttributeOverrides allowAttributeOverridesCreateResponse = workingCapitalLoanProductCreateRequest
                    .getAllowAttributeOverrides();
            GetConfigurableAttributes allowAttributeOverridesGetResponse = getWorkingCapitalProductResponse.getAllowAttributeOverrides();
            assertions.assertThat(allowAttributeOverridesGetResponse).isNotNull();
            assertions.assertThat(allowAttributeOverridesCreateResponse.getDiscountDefault())
                    .isEqualTo(allowAttributeOverridesGetResponse.getDiscountDefault());
            assertions.assertThat(allowAttributeOverridesCreateResponse.getDelinquencyBucketClassification())
                    .isEqualTo(allowAttributeOverridesGetResponse.getBreach());
            assertions.assertThat(allowAttributeOverridesCreateResponse.getBreach())
                    .isEqualTo(allowAttributeOverridesGetResponse.getDelinquencyBucketClassification());
            assertions.assertThat(allowAttributeOverridesCreateResponse.getPeriodPaymentFrequency())
                    .isEqualTo(allowAttributeOverridesGetResponse.getPeriodPaymentFrequency());
            assertions.assertThat(allowAttributeOverridesCreateResponse.getPeriodPaymentFrequencyType())
                    .isEqualTo(allowAttributeOverridesGetResponse.getPeriodPaymentFrequencyType());
        }
        assertions.assertAll();
    }

    public void updateWorkingCapitalLoanProductWithBreachAndNearBreach(Long breachId, Long nearBreachId) {
        final String workingCapitalProductDefaultName = DefaultWorkingCapitalLoanProduct.WCLP.getName()
                + Utils.randomStringGenerator("_", 10);
        final String workingCapitalProductDefaultShortName = Utils.randomStringGenerator(4);
        final PutWorkingCapitalLoanProductsProductIdRequest workingCapitalLoanProductUpdateRequest = workingCapitalRequestFactory
                .defaultWorkingCapitalLoanProductRequestUpdate() //
                .name(workingCapitalProductDefaultName) //
                .shortName(workingCapitalProductDefaultShortName)// ;
                .breachId(breachId) //
                .nearBreachId(nearBreachId).externalId("EXT-WCP-" + UUID.randomUUID());

        PostWorkingCapitalLoanProductsResponse workingCapitalLoanProductsResponse = testContext()
                .get(TestContextKey.WORKING_CAPITAL_LOAN_PRODUCT_CREATE_RESPONSE);
        Long resourceId = workingCapitalLoanProductsResponse.getResourceId();
        PutWorkingCapitalLoanProductsProductIdResponse responseWorkingCapitalLoanProductUpdate = ok(
                () -> workingCapitalApi().updateWorkingCapitalLoanProduct(resourceId, workingCapitalLoanProductUpdateRequest, Map.of()));

        testContext().set(TestContextKey.WORKING_CAPITAL_LOAN_PRODUCT_UPDATE_RESPONSE, responseWorkingCapitalLoanProductUpdate);
        testContext().set(TestContextKey.WORKING_CAPITAL_LOAN_PRODUCT_UPDATE_REQUEST, workingCapitalLoanProductUpdateRequest);
        checkWorkingCapitalLoanProductUpdate();
    }

    public void checkWorkingCapitalLoanProductUpdate() {
        PutWorkingCapitalLoanProductsProductIdRequest workingCapitalLoanProductsUpdateRequest = testContext()
                .get(TestContextKey.WORKING_CAPITAL_LOAN_PRODUCT_UPDATE_REQUEST);

        PostWorkingCapitalLoanProductsResponse workingCapitalLoanProductResponse = testContext()
                .get(TestContextKey.WORKING_CAPITAL_LOAN_PRODUCT_CREATE_RESPONSE);
        Long resourceId = workingCapitalLoanProductResponse.getResourceId();

        GetWorkingCapitalLoanProductsProductIdResponse getWorkingCapitalProductResponse = workingCapitalApi()
                .retrieveOneWorkingCapitalLoanProduct(resourceId, Map.of());
        checkWorkingCapitalLoanProductUpdate(workingCapitalLoanProductsUpdateRequest, getWorkingCapitalProductResponse);
    }

    public void checkWorkingCapitalLoanProductWithExternalIdUpdate() {
        PutWorkingCapitalLoanProductsProductIdRequest workingCapitalLoanProductsUpdateRequest = testContext()
                .get(TestContextKey.WORKING_CAPITAL_LOAN_PRODUCT_UPDATE_REQUEST);
        String externalId = workingCapitalLoanProductsUpdateRequest.getExternalId();

        GetWorkingCapitalLoanProductsProductIdResponse getWorkingCapitalProductResponse = workingCapitalApi()
                .retrieveOneWorkingCapitalLoanProductByExternalId(externalId, Map.of());
        checkWorkingCapitalLoanProductUpdate(workingCapitalLoanProductsUpdateRequest, getWorkingCapitalProductResponse);
    }

    public void checkWorkingCapitalLoanProductUpdate(PutWorkingCapitalLoanProductsProductIdRequest workingCapitalLoanProductsUpdateRequest,
            GetWorkingCapitalLoanProductsProductIdResponse getWorkingCapitalProductResponse) {
        PostWorkingCapitalLoanProductsResponse workingCapitalLoanProductResponse = testContext()
                .get(TestContextKey.WORKING_CAPITAL_LOAN_PRODUCT_CREATE_RESPONSE);
        Long resourceId = workingCapitalLoanProductResponse.getResourceId();

        SoftAssertions assertions = new SoftAssertions();
        assertions.assertThat(workingCapitalLoanProductsUpdateRequest.getName()).isEqualTo(getWorkingCapitalProductResponse.getName());
        assertions.assertThat(workingCapitalLoanProductsUpdateRequest.getShortName())
                .isEqualTo(getWorkingCapitalProductResponse.getShortName());
        assertions.assertThat(workingCapitalLoanProductsUpdateRequest.getDescription())
                .isEqualTo(getWorkingCapitalProductResponse.getDescription());
        assertions.assertThat(workingCapitalLoanProductsUpdateRequest.getExternalId())
                .isEqualTo(getWorkingCapitalProductResponse.getExternalId());
        assertions.assertThat(resourceId).isEqualTo(getWorkingCapitalProductResponse.getId());

        // check currency
        assertions.assertThat(getWorkingCapitalProductResponse.getCurrency()).isNotNull();
        assertions.assertThat(workingCapitalLoanProductsUpdateRequest.getCurrencyCode())
                .isEqualTo(getWorkingCapitalProductResponse.getCurrency().getCode());
        assertions.assertThat(workingCapitalLoanProductsUpdateRequest.getDigitsAfterDecimal())
                .isEqualTo(getWorkingCapitalProductResponse.getCurrency().getDecimalPlaces());
        assertions.assertThat(workingCapitalLoanProductsUpdateRequest.getInMultiplesOf())
                .isEqualTo(getWorkingCapitalProductResponse.getCurrency().getInMultiplesOf());
        assertions.assertThat(workingCapitalLoanProductsUpdateRequest.getStartDate())
                .isEqualTo(getWorkingCapitalProductResponse.getStartDate());
        assertions.assertThat(workingCapitalLoanProductsUpdateRequest.getCloseDate())
                .isEqualTo(getWorkingCapitalProductResponse.getCloseDate());

        assertions.assertThat(getWorkingCapitalProductResponse.getAmortizationType()).isNotNull();
        assertions.assertThat(workingCapitalLoanProductsUpdateRequest.getAmortizationType()).isNotNull();
        assertions.assertThat(workingCapitalLoanProductsUpdateRequest.getAmortizationType().getValue())
                .isEqualTo(getWorkingCapitalProductResponse.getAmortizationType().getCode());
        assertions.assertThat(workingCapitalLoanProductsUpdateRequest.getNpvDayCount())
                .isEqualTo(getWorkingCapitalProductResponse.getNpvDayCount());
        if (workingCapitalLoanProductsUpdateRequest.getDelinquencyBucketId() != null) {
            assertions.assertThat(workingCapitalLoanProductsUpdateRequest.getDelinquencyBucketId())
                    .isEqualTo(getWorkingCapitalProductResponse.getDelinquencyBucket().getId());
        }
        if (workingCapitalLoanProductsUpdateRequest.getDelinquencyGraceDays() != null) {
            assertions.assertThat(workingCapitalLoanProductsUpdateRequest.getDelinquencyGraceDays())
                    .isEqualTo(getWorkingCapitalProductResponse.getDelinquencyGraceDays());
        }
        if (workingCapitalLoanProductsUpdateRequest.getDelinquencyStartType() != null) {
            assertions.assertThat(workingCapitalLoanProductsUpdateRequest.getDelinquencyStartType())
                    .isEqualTo(getWorkingCapitalProductResponse.getDelinquencyStartType().getCode());
        }
        if (workingCapitalLoanProductsUpdateRequest.getBreachId() != null) {
            assertions.assertThat(workingCapitalLoanProductsUpdateRequest.getBreachId())
                    .isEqualTo(getWorkingCapitalProductResponse.getBreach().getId());
        }
        if (workingCapitalLoanProductsUpdateRequest.getNearBreachId() != null) {
            assertions.assertThat(workingCapitalLoanProductsUpdateRequest.getNearBreachId())
                    .isEqualTo(getWorkingCapitalProductResponse.getNearBreach().getId());
        }

        assertions.assertThat(getWorkingCapitalProductResponse.getRepaymentEvery()).isNotNull();
        assertions.assertThat(workingCapitalLoanProductsUpdateRequest.getRepaymentEvery()).isNotNull();
        assertions.assertThat(
                workingCapitalLoanProductsUpdateRequest.getRepaymentEvery().compareTo(getWorkingCapitalProductResponse.getRepaymentEvery()))
                .isEqualTo(0);
        assertions.assertThat(workingCapitalLoanProductsUpdateRequest.getRepaymentFrequencyType()).isNotNull();
        assertions.assertThat(getWorkingCapitalProductResponse.getRepaymentFrequencyType()).isNotNull();
        assertions.assertThat(workingCapitalLoanProductsUpdateRequest.getRepaymentFrequencyType().getValue())
                .isEqualTo(getWorkingCapitalProductResponse.getRepaymentFrequencyType().getCode());
        assertions.assertThat(workingCapitalLoanProductsUpdateRequest.getPeriodPaymentRate()).isNotNull();
        assertions.assertThat(workingCapitalLoanProductsUpdateRequest.getPeriodPaymentRate()
                .compareTo(getWorkingCapitalProductResponse.getPeriodPaymentRate())).isEqualTo(0);
        assertions.assertThat(workingCapitalLoanProductsUpdateRequest.getMinPeriodPaymentRate())
                .isEqualTo(getWorkingCapitalProductResponse.getMinPeriodPaymentRate());
        assertions.assertThat(workingCapitalLoanProductsUpdateRequest.getMaxPeriodPaymentRate())
                .isEqualTo(getWorkingCapitalProductResponse.getMaxPeriodPaymentRate());
        if (workingCapitalLoanProductsUpdateRequest.getDiscount() != null) {
            assertions
                    .assertThat(
                            workingCapitalLoanProductsUpdateRequest.getDiscount().compareTo(getWorkingCapitalProductResponse.getDiscount()))
                    .isEqualTo(0);
        }

        // check payment allocation rules
        assertions.assertThat(workingCapitalLoanProductsUpdateRequest.getPaymentAllocation()).isNotNull();
        workingCapitalLoanProductsUpdateRequest.getPaymentAllocation().forEach(paymentAllocation -> {
            assertThat(getWorkingCapitalProductResponse.getPaymentAllocation()).isNotNull();
            GetPaymentAllocation getPaymentAllocation = getWorkingCapitalProductResponse.getPaymentAllocation().stream()
                    .filter(paymentAllocationSearched -> {
                        assertThat(paymentAllocation.getTransactionType()).isNotNull();
                        return paymentAllocation.getTransactionType().getValue().equals(paymentAllocationSearched.getTransactionType());
                    }).findFirst().orElseThrow(() -> new RuntimeException("No paymentAllocation is found!"));
            assertions.assertThat(paymentAllocation.getPaymentAllocationOrder())
                    .containsAll(getPaymentAllocation.getPaymentAllocationOrder());
        });

        assertions
                .assertThat(
                        workingCapitalLoanProductsUpdateRequest.getPrincipal().compareTo(getWorkingCapitalProductResponse.getPrincipal()))
                .isEqualTo(0);
        assertions.assertThat(
                workingCapitalLoanProductsUpdateRequest.getMaxPrincipal().compareTo(getWorkingCapitalProductResponse.getMaxPrincipal()))
                .isEqualTo(0);
        assertions.assertThat(
                workingCapitalLoanProductsUpdateRequest.getMinPrincipal().compareTo(getWorkingCapitalProductResponse.getMinPrincipal()))
                .isEqualTo(0);

        if (workingCapitalLoanProductsUpdateRequest.getAllowAttributeOverrides() != null) {
            PostAllowAttributeOverrides allowAttributeOverridesCreateResponse = workingCapitalLoanProductsUpdateRequest
                    .getAllowAttributeOverrides();
            GetConfigurableAttributes allowAttributeOverridesGetResponse = getWorkingCapitalProductResponse.getAllowAttributeOverrides();
            assert allowAttributeOverridesGetResponse != null;
            assertions.assertThat(allowAttributeOverridesCreateResponse.getDiscountDefault())
                    .isEqualTo(allowAttributeOverridesGetResponse.getDiscountDefault());
            assertions.assertThat(allowAttributeOverridesCreateResponse.getDelinquencyBucketClassification())
                    .isEqualTo(allowAttributeOverridesGetResponse.getDelinquencyBucketClassification());
            assertions.assertThat(allowAttributeOverridesCreateResponse.getBreach())
                    .isEqualTo(allowAttributeOverridesGetResponse.getBreach());
            assertions.assertThat(allowAttributeOverridesCreateResponse.getPeriodPaymentFrequency())
                    .isEqualTo(allowAttributeOverridesGetResponse.getPeriodPaymentFrequency());
            assertions.assertThat(allowAttributeOverridesCreateResponse.getPeriodPaymentFrequencyType())
                    .isEqualTo(allowAttributeOverridesGetResponse.getPeriodPaymentFrequencyType());
        }
        assertions.assertAll();
    }

    public void createWorkingCapitalLoanProductWithInvalidDataFailure(String fieldName, String value, String errorMessage) {
        final String workingCapitalProductDefaultName = DefaultWorkingCapitalLoanProduct.WCLP.getName()
                + Utils.randomStringGenerator("_", 10);
        final PostWorkingCapitalLoanProductsRequest defaultWorkingCapitalLoanProductCreateRequest = workingCapitalRequestFactory
                .defaultWorkingCapitalLoanProductRequest() //
                .name(workingCapitalProductDefaultName); //

        final PostWorkingCapitalLoanProductsRequest workingCapitalLoanProductCreateRequestUpdated = setWorkingCapitalLoanProductsCreateFieldValue(
                defaultWorkingCapitalLoanProductCreateRequest, fieldName, value);
        checkCreateWorkingCapitalLoanProductWithInvalidDataFailure(workingCapitalLoanProductCreateRequestUpdated, 400, errorMessage);
    }

    public void createWorkingCapitalLoanProductWithBreachWithInvalidDataFailure(String fieldName, String value, String errorMessage) {

        final Long breachId = getWcBreachIdForFrequency(3, WorkingCapitalBreachFrequencyType.MONTHS.getCode());

        final String workingCapitalProductDefaultName = DefaultWorkingCapitalLoanProduct.WCLP.getName()
                + Utils.randomStringGenerator("_", 10);
        final PostWorkingCapitalLoanProductsRequest defaultWorkingCapitalLoanProductCreateRequest = workingCapitalRequestFactory
                .defaultWorkingCapitalLoanProductRequest() //
                .breachId(breachId).name(workingCapitalProductDefaultName); //

        final PostWorkingCapitalLoanProductsRequest workingCapitalLoanProductCreateRequestUpdated = setWorkingCapitalLoanProductsCreateFieldValue(
                defaultWorkingCapitalLoanProductCreateRequest, fieldName, value);
        checkCreateWorkingCapitalLoanProductWithInvalidDataFailure(workingCapitalLoanProductCreateRequestUpdated, 404, errorMessage);
    }

    public void checkCreateWorkingCapitalLoanProductWithInvalidDataFailure(
            PostWorkingCapitalLoanProductsRequest workingCapitalLoanProductCreateRequest, int statusCode, String errorMessage) {
        CallFailedRuntimeException exception = fail(
                () -> workingCapitalApi().createWorkingCapitalLoanProduct(workingCapitalLoanProductCreateRequest, Map.of()));
        assertThat(exception.getStatus()).as(ErrorMessageHelper.incorrectExpectedValueInResponse()).isEqualTo(statusCode);
        assertThat(exception.getDeveloperMessage()).contains(errorMessage);
    }

    public PostWorkingCapitalLoanProductsRequest setWorkingCapitalLoanProductsCreateFieldValue(
            PostWorkingCapitalLoanProductsRequest defaultWorkingCapitalLoanProductCreateRequest, String fieldName, String fieldValue) {
        if (fieldValue.equals("null")) {
            fieldValue = null;
        }
        Integer valueInteger = null;
        BigDecimal valueBigDecimal = null;
        Long valueLong = null;
        if (fieldName.equalsIgnoreCase(DIGITS_AFTER_DECIMAL_FIELD_NAME) || fieldName.equalsIgnoreCase(IN_MULTIPLES_OF_FIELD_NAME)
                || fieldName.equalsIgnoreCase(NPV_DAY_COUNT_FIELD_NAME) || fieldName.equalsIgnoreCase(REPAYMENT_EVERY_FIELD_NAME)
                || fieldName.equalsIgnoreCase(DELINQUENCY_GRACE_DAYS_FIELD_NAME)) {
            valueInteger = fieldValue != null ? Integer.valueOf(fieldValue) : null;
        }
        if (fieldName.equalsIgnoreCase(PRINCIPAL_FIELD_NAME) || fieldName.equalsIgnoreCase(MIN_PRINCIPAL_FIELD_NAME)
                || fieldName.equalsIgnoreCase(MAX_PRINCIPAL_FIELD_NAME) || fieldName.equalsIgnoreCase(PERIOD_PAYMENT_RATE_FIELD_NAME)
                || fieldName.equalsIgnoreCase(MIN_PERIOD_PAYMENT_RATE_FIELD_NAME)
                || fieldName.equalsIgnoreCase(MAX_PERIOD_PAYMENT_RATE_FIELD_NAME) || fieldName.equalsIgnoreCase(DISCOUNT_FIELD_NAME)) {
            valueBigDecimal = fieldValue != null ? new BigDecimal(fieldValue) : null;
        }
        if (fieldName.equalsIgnoreCase(BREACH_ID_FIELD_NAME) || fieldName.equalsIgnoreCase(NEAR_BREACH_ID_FIELD_NAME)
                || fieldName.equalsIgnoreCase(DELINQUENCY_BUCKET_ID_FIELD_NAME)) {
            valueLong = fieldValue != null ? Long.valueOf(fieldValue) : null;
        }

        switch (fieldName) {
            case NAME_FIELD_NAME:
                defaultWorkingCapitalLoanProductCreateRequest.setName(fieldValue);
            break;
            case SHORT_NAME_FIELD:
                defaultWorkingCapitalLoanProductCreateRequest.setShortName(fieldValue);
            break;
            case DESCRIPTION_FIELD_NAME:
                defaultWorkingCapitalLoanProductCreateRequest.setDescription(fieldValue);
            break;
            case CURRENCY_CODE_FIELD_NAME:
                defaultWorkingCapitalLoanProductCreateRequest.setCurrencyCode(fieldValue);
            break;
            case DIGITS_AFTER_DECIMAL_FIELD_NAME:
                defaultWorkingCapitalLoanProductCreateRequest.setDigitsAfterDecimal(valueInteger);
            break;
            case IN_MULTIPLES_OF_FIELD_NAME:
                defaultWorkingCapitalLoanProductCreateRequest.setInMultiplesOf(valueInteger);
            break;
            case AMORTIZATION_TYPE_FIELD_NAME:
                defaultWorkingCapitalLoanProductCreateRequest.setAmortizationType(
                        fieldValue == null ? null : PostWorkingCapitalLoanProductsRequest.AmortizationTypeEnum.valueOf(fieldValue));
            break;
            case NPV_DAY_COUNT_FIELD_NAME:
                defaultWorkingCapitalLoanProductCreateRequest.setNpvDayCount(valueInteger);
            break;
            case PRINCIPAL_FIELD_NAME:
                defaultWorkingCapitalLoanProductCreateRequest.setPrincipal(valueBigDecimal);
            break;
            case MIN_PRINCIPAL_FIELD_NAME:
                defaultWorkingCapitalLoanProductCreateRequest.setMinPrincipal(valueBigDecimal);
            break;
            case MAX_PRINCIPAL_FIELD_NAME:
                defaultWorkingCapitalLoanProductCreateRequest.setMaxPrincipal(valueBigDecimal);
            break;
            case DISCOUNT_FIELD_NAME:
                defaultWorkingCapitalLoanProductCreateRequest.setDiscount(valueBigDecimal);
            break;
            case PERIOD_PAYMENT_RATE_FIELD_NAME:
                defaultWorkingCapitalLoanProductCreateRequest.setPeriodPaymentRate(valueBigDecimal);
            break;
            case MIN_PERIOD_PAYMENT_RATE_FIELD_NAME:
                defaultWorkingCapitalLoanProductCreateRequest.setMinPeriodPaymentRate(valueBigDecimal);
            break;
            case MAX_PERIOD_PAYMENT_RATE_FIELD_NAME:
                defaultWorkingCapitalLoanProductCreateRequest.setMaxPeriodPaymentRate(valueBigDecimal);
            break;
            case REPAYMENT_FREQUENCY_TYPE_FIELD_NAME:
                defaultWorkingCapitalLoanProductCreateRequest.setRepaymentFrequencyType(
                        fieldValue == null ? null : PostWorkingCapitalLoanProductsRequest.RepaymentFrequencyTypeEnum.valueOf(fieldValue));
            break;
            case REPAYMENT_EVERY_FIELD_NAME:
                defaultWorkingCapitalLoanProductCreateRequest.setRepaymentEvery(valueInteger);
            break;
            case EXTERNAL_ID_FIELD_NAME:
                defaultWorkingCapitalLoanProductCreateRequest.setExternalId(fieldValue);
            break;
            case DELINQUENCY_BUCKET_ID_FIELD_NAME:
                defaultWorkingCapitalLoanProductCreateRequest.setDelinquencyBucketId(valueLong);
            break;
            case DELINQUENCY_GRACE_DAYS_FIELD_NAME:
                defaultWorkingCapitalLoanProductCreateRequest.setDelinquencyGraceDays(valueInteger);
            break;
            case DELINQUENCY_START_TYPE_FIELD_NAME:
                defaultWorkingCapitalLoanProductCreateRequest.setDelinquencyStartType(fieldValue);
            break;
            case BREACH_ID_FIELD_NAME:
                defaultWorkingCapitalLoanProductCreateRequest.setBreachId(valueLong);
            break;
            case NEAR_BREACH_ID_FIELD_NAME:
                defaultWorkingCapitalLoanProductCreateRequest.setNearBreachId(valueLong);
            break;
            case LOCALE_FIELD_NAME:
                defaultWorkingCapitalLoanProductCreateRequest.setLocale(fieldValue);
            break;
            default:
            break;
        }
        return defaultWorkingCapitalLoanProductCreateRequest;
    }

    public void updateWorkingCapitalLoanProductWithInvalidDataFailure(Long productId, String fieldName, String value, String errorMessage) {
        final PutWorkingCapitalLoanProductsProductIdRequest defaultWorkingCapitalLoanProductUpdateRequest = new PutWorkingCapitalLoanProductsProductIdRequest();

        final PutWorkingCapitalLoanProductsProductIdRequest workingCapitalLoanProductUpdateRequestUpdated = setWorkingCapitalLoanProductsUpdateRequest(
                defaultWorkingCapitalLoanProductUpdateRequest, fieldName, value);
        checkUpdateWorkingCapitalLoanProductWithInvalidDataFailure(productId, workingCapitalLoanProductUpdateRequestUpdated, 400,
                errorMessage);
    }

    public void checkUpdateWorkingCapitalLoanProductWithInvalidDataFailure(Long productId,
            PutWorkingCapitalLoanProductsProductIdRequest workingCapitalLoanProductUpdateRequest, int statusCode, String errorMessage) {
        CallFailedRuntimeException exception = fail(
                () -> workingCapitalApi().updateWorkingCapitalLoanProduct(productId, workingCapitalLoanProductUpdateRequest, Map.of()));
        assertThat(exception.getStatus()).as(ErrorMessageHelper.incorrectExpectedValueInResponse()).isEqualTo(statusCode);
        assertThat(exception.getDeveloperMessage()).contains(errorMessage);
    }

    public PutWorkingCapitalLoanProductsProductIdRequest setWorkingCapitalLoanProductsUpdateRequest(
            PutWorkingCapitalLoanProductsProductIdRequest defaultWorkingCapitalLoanProductUpdateRequest, String fieldName,
            String fieldValue) {
        if (fieldValue.equals("null")) {
            fieldValue = null;
        }

        Integer valueInteger = null;
        BigDecimal valueBigDecimal = null;
        Long valueLong = null;
        if (fieldName.equalsIgnoreCase(DIGITS_AFTER_DECIMAL_FIELD_NAME) || fieldName.equalsIgnoreCase(IN_MULTIPLES_OF_FIELD_NAME)
                || fieldName.equalsIgnoreCase(NPV_DAY_COUNT_FIELD_NAME) || fieldName.equalsIgnoreCase(REPAYMENT_EVERY_FIELD_NAME)
                || fieldName.equalsIgnoreCase(DELINQUENCY_GRACE_DAYS_FIELD_NAME)) {
            valueInteger = fieldValue != null ? Integer.valueOf(fieldValue) : null;
        }
        if (fieldName.equalsIgnoreCase(PRINCIPAL_FIELD_NAME) || fieldName.equalsIgnoreCase(MIN_PRINCIPAL_FIELD_NAME)
                || fieldName.equalsIgnoreCase(MAX_PRINCIPAL_FIELD_NAME) || fieldName.equalsIgnoreCase(PERIOD_PAYMENT_RATE_FIELD_NAME)
                || fieldName.equalsIgnoreCase(MIN_PERIOD_PAYMENT_RATE_FIELD_NAME)
                || fieldName.equalsIgnoreCase(MAX_PERIOD_PAYMENT_RATE_FIELD_NAME) || fieldName.equalsIgnoreCase(DISCOUNT_FIELD_NAME)) {
            valueBigDecimal = fieldValue != null ? new BigDecimal(fieldValue) : null;
        }
        if (fieldName.equalsIgnoreCase(BREACH_ID_FIELD_NAME) || fieldName.equalsIgnoreCase(NEAR_BREACH_ID_FIELD_NAME)
                || fieldName.equalsIgnoreCase(DELINQUENCY_BUCKET_ID_FIELD_NAME)) {
            valueLong = fieldValue != null ? Long.valueOf(fieldValue) : null;
        }

        switch (fieldName) {
            case NAME_FIELD_NAME:
                defaultWorkingCapitalLoanProductUpdateRequest.setName(fieldValue);
            break;
            case SHORT_NAME_FIELD:
                defaultWorkingCapitalLoanProductUpdateRequest.setShortName(fieldValue);
            break;
            case DESCRIPTION_FIELD_NAME:
                defaultWorkingCapitalLoanProductUpdateRequest.setDescription(fieldValue);
            break;
            case CURRENCY_CODE_FIELD_NAME:
                defaultWorkingCapitalLoanProductUpdateRequest.setCurrencyCode(fieldValue);
            break;
            case DIGITS_AFTER_DECIMAL_FIELD_NAME:
                defaultWorkingCapitalLoanProductUpdateRequest.setDigitsAfterDecimal(valueInteger);
            break;
            case IN_MULTIPLES_OF_FIELD_NAME:
                defaultWorkingCapitalLoanProductUpdateRequest.setInMultiplesOf(valueInteger);
            break;
            case AMORTIZATION_TYPE_FIELD_NAME:
                defaultWorkingCapitalLoanProductUpdateRequest.setAmortizationType(
                        fieldValue == null ? null : PutWorkingCapitalLoanProductsProductIdRequest.AmortizationTypeEnum.valueOf(fieldValue));
            break;
            case NPV_DAY_COUNT_FIELD_NAME:
                defaultWorkingCapitalLoanProductUpdateRequest.setNpvDayCount(valueInteger);
            break;
            case PRINCIPAL_FIELD_NAME:
                defaultWorkingCapitalLoanProductUpdateRequest.setPrincipal(valueBigDecimal);
            break;
            case MIN_PRINCIPAL_FIELD_NAME:
                defaultWorkingCapitalLoanProductUpdateRequest.setMinPrincipal(valueBigDecimal);
            break;
            case DISCOUNT_FIELD_NAME:
                defaultWorkingCapitalLoanProductUpdateRequest.setDiscount(valueBigDecimal);
            break;
            case MAX_PRINCIPAL_FIELD_NAME:
                defaultWorkingCapitalLoanProductUpdateRequest.setMaxPrincipal(valueBigDecimal);
            break;
            case PERIOD_PAYMENT_RATE_FIELD_NAME:
                defaultWorkingCapitalLoanProductUpdateRequest.setPeriodPaymentRate(valueBigDecimal);
            break;
            case MIN_PERIOD_PAYMENT_RATE_FIELD_NAME:
                defaultWorkingCapitalLoanProductUpdateRequest.setMinPeriodPaymentRate(valueBigDecimal);
            break;
            case MAX_PERIOD_PAYMENT_RATE_FIELD_NAME:
                defaultWorkingCapitalLoanProductUpdateRequest.setMaxPeriodPaymentRate(valueBigDecimal);
            break;
            case REPAYMENT_FREQUENCY_TYPE_FIELD_NAME:
                defaultWorkingCapitalLoanProductUpdateRequest.setRepaymentFrequencyType(fieldValue == null ? null
                        : PutWorkingCapitalLoanProductsProductIdRequest.RepaymentFrequencyTypeEnum.valueOf(fieldValue));
            break;
            case REPAYMENT_EVERY_FIELD_NAME:
                defaultWorkingCapitalLoanProductUpdateRequest.setRepaymentEvery(valueInteger);
            break;
            case EXTERNAL_ID_FIELD_NAME:
                defaultWorkingCapitalLoanProductUpdateRequest.setExternalId(fieldValue);
            break;
            case DELINQUENCY_BUCKET_ID_FIELD_NAME:
                defaultWorkingCapitalLoanProductUpdateRequest.setDelinquencyBucketId(valueLong);
            break;
            case DELINQUENCY_GRACE_DAYS_FIELD_NAME:
                defaultWorkingCapitalLoanProductUpdateRequest.setDelinquencyGraceDays(valueInteger);
            break;
            case DELINQUENCY_START_TYPE_FIELD_NAME:
                defaultWorkingCapitalLoanProductUpdateRequest.setDelinquencyStartType(fieldValue);
            break;
            case BREACH_ID_FIELD_NAME:
                defaultWorkingCapitalLoanProductUpdateRequest.setBreachId(valueLong);
            break;
            case NEAR_BREACH_ID_FIELD_NAME:
                defaultWorkingCapitalLoanProductUpdateRequest.setNearBreachId(valueLong);
            break;
            case LOCALE_FIELD_NAME:
                defaultWorkingCapitalLoanProductUpdateRequest.setLocale(fieldValue);
            break;
            default:
            break;
        }
        return defaultWorkingCapitalLoanProductUpdateRequest;
    }

    public Long getWcBreachIdForFrequency(Integer breachFrequency, String breachFrequencyType) {
        final WorkingCapitalBreachRequest breachRequest = workingCapitalRequestFactory.defaultWorkingCapitalBreachRequest()
                .name("Breach_WC_" + Utils.randomStringGenerator(10)).breachFrequency(breachFrequency)
                .breachFrequencyType(breachFrequencyType);
        final CommandProcessingResult breachCreateResponse = ok(
                () -> fineractFeignClient.workingCapitalBreaches().createWorkingCapitalBreach(breachRequest));
        final Long breachId = breachCreateResponse.getResourceId();
        testContext().set(WORKING_CAPITAL_BREACH_ID, breachId);
        return breachId;
    }

    public Long getWcNearBreachIdForFrequency(Integer nearBreachFrequency, String nearBreachFrequencyType) {
        final WorkingCapitalNearBreachRequest nearBreachRequest = workingCapitalRequestFactory.defaultWorkingCapitalNearBreachRequest()
                .nearBreachName("NearBreach_WC_" + Utils.randomStringGenerator(10)).nearBreachFrequency(nearBreachFrequency)
                .nearBreachFrequencyType(nearBreachFrequencyType);
        final CommandProcessingResult nearBreachCreateResponse = ok(
                () -> fineractFeignClient.workingCapitalNearBreaches().createWorkingCapitalNearBreach(nearBreachRequest));
        final Long nearBreachId = nearBreachCreateResponse.getResourceId();
        testContext().set(WORKING_CAPITAL_NEAR_BREACH_ID, nearBreachId);
        return nearBreachId;
    }

    public void checkWorkingCapitalLoanProductDeleteFailure(Long productId) {
        CallFailedRuntimeException exception = fail(() -> workingCapitalApi().retrieveOneWorkingCapitalLoanProduct(productId, Map.of()));
        assertThat(exception.getStatus()).as(ErrorMessageHelper.dateFailureErrorCodeMsg()).isEqualTo(404);
        assertThat(exception.getDeveloperMessage())
                .contains(ErrorMessageHelper.workingCapitalLoanProductIdentifiedDoesNotExistFailure(String.valueOf(productId)));
    }

    @When("Admin creates a new Working Capital Loan Product with delinquencyGraceDays {int} and delinquencyStartType {string}")
    public void createWorkingCapitalLoanProductWithGraceDays(int graceDays, String startType) {
        final String name = DefaultWorkingCapitalLoanProduct.WCLP.getName() + Utils.randomStringGenerator("_", 10);
        final PostWorkingCapitalLoanProductsRequest request = workingCapitalRequestFactory.defaultWorkingCapitalLoanProductRequest() //
                .name(name) //
                .delinquencyGraceDays(graceDays) //
                .delinquencyStartType(startType);
        final PostWorkingCapitalLoanProductsResponse response = createWorkingCapitalLoanProduct(request);
        testContext().set(TestContextKey.WORKING_CAPITAL_LOAN_PRODUCT_CREATE_RESPONSE, response);
        testContext().set(TestContextKey.WORKING_CAPITAL_LOAN_PRODUCT_CREATE_REQUEST, request);
    }

    @Then("Working Capital Loan Product has delinquencyGraceDays {int} and delinquencyStartType {string}")
    public void verifyProductGraceDays(int expectedGraceDays, String expectedStartType) {
        final GetWorkingCapitalLoanProductsProductIdResponse product = retrieveCreatedProduct();
        assertThat(product.getDelinquencyGraceDays()).isEqualTo(expectedGraceDays);
        assertThat(product.getDelinquencyStartType()).isNotNull();
        assertThat(product.getDelinquencyStartType().getCode()).isEqualTo(expectedStartType);
    }

    @Then("Working Capital Loan Product has null delinquencyGraceDays and null delinquencyStartType")
    public void verifyProductNullGraceDays() {
        final GetWorkingCapitalLoanProductsProductIdResponse product = retrieveCreatedProduct();
        assertThat(product.getDelinquencyGraceDays()).isNull();
        assertThat(product.getDelinquencyStartType()).isNull();
    }

    private GetWorkingCapitalLoanProductsProductIdResponse retrieveCreatedProduct() {
        final PostWorkingCapitalLoanProductsResponse productResponse = testContext()
                .get(TestContextKey.WORKING_CAPITAL_LOAN_PRODUCT_CREATE_RESPONSE);
        return retrieveCreatedProduct(productResponse.getResourceId());
    }

    private GetWorkingCapitalLoanProductsProductIdResponse retrieveCreatedProduct(Long loanProductId) {
        return workingCapitalApi().retrieveOneWorkingCapitalLoanProduct(loanProductId, Map.of());
    }

    @When("Admin updates Working Capital Loan Product with delinquencyGraceDays {int} and delinquencyStartType {string}")
    public void updateProductGraceDays(int graceDays, String startType) {
        final Long resourceId = retrieveCreatedProductId();
        final PutWorkingCapitalLoanProductsProductIdRequest updateRequest = new PutWorkingCapitalLoanProductsProductIdRequest() //
                .delinquencyGraceDays(graceDays) //
                .delinquencyStartType(startType) //
                .locale(LoanProductsRequestFactory.LOCALE_EN);
        ok(() -> workingCapitalApi().updateWorkingCapitalLoanProduct(resourceId, updateRequest, Map.of()));
    }

    private Long retrieveCreatedProductId() {
        final PostWorkingCapitalLoanProductsResponse productResponse = testContext()
                .get(TestContextKey.WORKING_CAPITAL_LOAN_PRODUCT_CREATE_RESPONSE);
        return productResponse.getResourceId();
    }

    @When("Admin retrieves the Working Capital Loan Product template")
    public void retrieveProductTemplate() {
        final GetWorkingCapitalLoanProductsTemplateResponse template = ok(
                () -> workingCapitalApi().retrieveTemplateWorkingCapitalLoanProduct(Map.of()));
        testContext().set(TestContextKey.WORKING_CAPITAL_LOAN_PRODUCT_TEMPLATE_RESPONSE, template);
    }

    @Then("Working Capital Loan Product template has delinquencyStartTypeOptions containing:")
    public void verifyTemplateDelinquencyStartTypeOptions(final DataTable table) {
        final List<String> expectedOptions = table.asList();
        final GetWorkingCapitalLoanProductsTemplateResponse template = testContext()
                .get(TestContextKey.WORKING_CAPITAL_LOAN_PRODUCT_TEMPLATE_RESPONSE);
        assertThat(template.getDelinquencyStartTypeOptions()).isNotNull().isNotEmpty();
        final List<String> actualCodes = template.getDelinquencyStartTypeOptions().stream().map(StringEnumOptionData::getCode).toList();
        assertThat(actualCodes).containsAll(expectedOptions);
    }

}
