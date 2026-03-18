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
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.feign.services.WorkingCapitalLoanProductsApi;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.models.DeleteWorkingCapitalLoanProductsProductIdResponse;
import org.apache.fineract.client.models.GetConfigurableAttributes;
import org.apache.fineract.client.models.GetPaymentAllocation;
import org.apache.fineract.client.models.GetWorkingCapitalLoanProductsProductIdResponse;
import org.apache.fineract.client.models.PostAllowAttributeOverrides;
import org.apache.fineract.client.models.PostWorkingCapitalLoanProductsRequest;
import org.apache.fineract.client.models.PostWorkingCapitalLoanProductsResponse;
import org.apache.fineract.client.models.PutWorkingCapitalLoanProductsProductIdRequest;
import org.apache.fineract.client.models.PutWorkingCapitalLoanProductsProductIdResponse;
import org.apache.fineract.test.data.workingcapitalproduct.DefaultWorkingCapitalLoanProduct;
import org.apache.fineract.test.factory.WorkingCapitalRequestFactory;
import org.apache.fineract.test.helper.ErrorMessageHelper;
import org.apache.fineract.test.helper.Utils;
import org.apache.fineract.test.stepdef.AbstractStepDef;
import org.apache.fineract.test.support.TestContextKey;
import org.assertj.core.api.SoftAssertions;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
@RequiredArgsConstructor
public class WorkingCapitalStepDef extends AbstractStepDef {

    @Autowired
    private WorkingCapitalRequestFactory workingCapitalRequestFactory;

    private final FineractFeignClient fineractFeignClient;

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
    public static final String PERIOD_PAYMENT_RATE_FIELD_NAME = "periodPaymentRate";
    public static final String MIN_PERIOD_PAYMENT_RATE_FIELD_NAME = "minPeriodPaymentRate";
    public static final String MAX_PERIOD_PAYMENT_RATE_FIELD_NAME = "maxPeriodPaymentRate";
    public static final String REPAYMENT_FREQUENCY_TYPE_FIELD_NAME = "repaymentFrequencyType";
    public static final String REPAYMENT_EVERY_FIELD_NAME = "repaymentEvery";
    public static final String EXTERNAL_ID_FIELD_NAME = "externalId";
    public static final String DELINQUENCY_BUCKET_ID_FIELD_NAME = "delinquencyBucketId";
    public static final String LOCALE_FIELD_NAME = "locale";

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
        checkCreateWorkingCapitalLoanProductWithInvalidDataFailure(workingCapitalLoanProductCreateRequestUpdated, errorMessage);
    }

    @Then("Admin failed to create a new Working Capital Loan Product field {string} with zero incorrect value")
    public void createWorkingCapitalLoanProductWithZeroValueDataFailed(String fieldName) {
        String errorMessage = ErrorMessageHelper.fieldValueZeroValueFailure(fieldName);
        createWorkingCapitalLoanProductWithInvalidDataFailure(fieldName, "0", errorMessage);
    }

    @Then("Admin failed to create a new Working Capital Loan Product field {string} with invalid data {string} and got an error {string}")
    public void createWorkingCapitalLoanProductWithInvalidDataFailed(String fieldName, String value, String errorMessage) {
        createWorkingCapitalLoanProductWithInvalidDataFailure(fieldName, value, errorMessage);
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
        checkCreateWorkingCapitalLoanProductWithInvalidDataFailure(defaultWorkingCapitalLoanProductCreateRequest, errorMessage);
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
        checkCreateWorkingCapitalLoanProductWithInvalidDataFailure(defaultWorkingCapitalLoanProductCreateRequest, errorMessage);
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
        checkUpdateWorkingCapitalLoanProductWithInvalidDataFailure(resourceId, workingCapitalLoanProductUpdateRequestUpdated, errorMessage);
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
        updateWorkingCapitalLoanProductWithInvalidDataFailure(defaultWorkingCapitalLoanProductUpdateRequest, resourceId, fieldName, value,
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
        checkUpdateWorkingCapitalLoanProductWithInvalidDataFailure(resourceId, defaultWorkingCapitalLoanProductUpdateRequest, errorMessage);
    }

    @Then("Admin failed to update a new Working Capital Loan Product with invalid value of payment allocation rules")
    public void updateWorkingCapitalLoanProductWithInvalidPaymentAllocationFailed() {
        final PutWorkingCapitalLoanProductsProductIdRequest defaultWorkingCapitalLoanProductUpdateRequest = new PutWorkingCapitalLoanProductsProductIdRequest()
                .paymentAllocation(workingCapitalRequestFactory.invalidPaymentAllocationRulesForWorkingCapitalLoanProductUpdateRequest());
        PostWorkingCapitalLoanProductsResponse workingCapitalLoanProductsResponse = testContext()
                .get(TestContextKey.WORKING_CAPITAL_LOAN_PRODUCT_CREATE_RESPONSE);
        Long resourceId = workingCapitalLoanProductsResponse.getResourceId();

        String errorMessage = ErrorMessageHelper.paymentAllocationRulesInvalidValueFailure();
        checkUpdateWorkingCapitalLoanProductWithInvalidDataFailure(resourceId, defaultWorkingCapitalLoanProductUpdateRequest, errorMessage);
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
        assert getWorkingCapitalProductResponse.getCurrency() != null;
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

        assert getWorkingCapitalProductResponse.getAmortizationType() != null;
        assert workingCapitalLoanProductCreateRequest.getAmortizationType() != null;
        assertions.assertThat(workingCapitalLoanProductCreateRequest.getAmortizationType().getValue())
                .isEqualTo(getWorkingCapitalProductResponse.getAmortizationType().getCode());
        assertions.assertThat(workingCapitalLoanProductCreateRequest.getNpvDayCount())
                .isEqualTo(getWorkingCapitalProductResponse.getNpvDayCount());
        assertions.assertThat(
                workingCapitalLoanProductCreateRequest.getRepaymentEvery().compareTo(getWorkingCapitalProductResponse.getRepaymentEvery()))
                .isEqualTo(0);
        assertions.assertThat(workingCapitalLoanProductCreateRequest.getRepaymentFrequencyType().getValue())
                .isEqualTo(getWorkingCapitalProductResponse.getRepaymentFrequencyType().getCode());
        assertions.assertThat(workingCapitalLoanProductCreateRequest.getPeriodPaymentRate()
                .compareTo(getWorkingCapitalProductResponse.getPeriodPaymentRate())).isEqualTo(0);
        assertions.assertThat(workingCapitalLoanProductCreateRequest.getMinPeriodPaymentRate())
                .isEqualTo(getWorkingCapitalProductResponse.getMinPeriodPaymentRate());
        assertions.assertThat(workingCapitalLoanProductCreateRequest.getMaxPeriodPaymentRate())
                .isEqualTo(getWorkingCapitalProductResponse.getMaxPeriodPaymentRate());

        // check payment allocation rules
        assert workingCapitalLoanProductCreateRequest.getPaymentAllocation() != null;
        workingCapitalLoanProductCreateRequest.getPaymentAllocation().forEach(paymentAllocation -> {
            assert getWorkingCapitalProductResponse.getPaymentAllocation() != null;
            GetPaymentAllocation getPaymentAllocation = getWorkingCapitalProductResponse.getPaymentAllocation().stream()
                    .filter(paymentAllocationSearched -> {
                        assert paymentAllocation.getTransactionType() != null;
                        return paymentAllocation.getTransactionType().getValue().equals(paymentAllocationSearched.getTransactionType());
                    }).findFirst().orElseThrow(() -> new RuntimeException("No paymentAllocation is found!"));
            assertions.assertThat(paymentAllocation.getPaymentAllocationOrder())
                    .containsAll(getPaymentAllocation.getPaymentAllocationOrder());
        });

        assertions
                .assertThat(
                        workingCapitalLoanProductCreateRequest.getPrincipal().compareTo(getWorkingCapitalProductResponse.getPrincipal()))
                .isEqualTo(0);
        assertions.assertThat(
                workingCapitalLoanProductCreateRequest.getMaxPrincipal().compareTo(getWorkingCapitalProductResponse.getMaxPrincipal()))
                .isEqualTo(0);
        assertions.assertThat(
                workingCapitalLoanProductCreateRequest.getMinPrincipal().compareTo(getWorkingCapitalProductResponse.getMinPrincipal()))
                .isEqualTo(0);

        if (workingCapitalLoanProductCreateRequest.getAllowAttributeOverrides() != null) {
            PostAllowAttributeOverrides allowAttributeOverridesCreateResponse = workingCapitalLoanProductCreateRequest
                    .getAllowAttributeOverrides();
            GetConfigurableAttributes allowAttributeOverridesGetResponse = getWorkingCapitalProductResponse.getAllowAttributeOverrides();
            assert allowAttributeOverridesGetResponse != null;
            assertions.assertThat(allowAttributeOverridesCreateResponse.getDiscountDefault())
                    .isEqualTo(allowAttributeOverridesGetResponse.getDiscountDefault());
            assertions.assertThat(allowAttributeOverridesCreateResponse.getDelinquencyBucketClassification())
                    .isEqualTo(allowAttributeOverridesGetResponse.getDelinquencyBucketClassification());
            assertions.assertThat(allowAttributeOverridesCreateResponse.getPeriodPaymentFrequency())
                    .isEqualTo(allowAttributeOverridesGetResponse.getPeriodPaymentFrequency());
            assertions.assertThat(allowAttributeOverridesCreateResponse.getPeriodPaymentFrequencyType())
                    .isEqualTo(allowAttributeOverridesGetResponse.getPeriodPaymentFrequencyType());
        }
        assertions.assertAll();
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
        assert getWorkingCapitalProductResponse.getCurrency() != null;
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

        assert getWorkingCapitalProductResponse.getAmortizationType() != null;
        assert workingCapitalLoanProductsUpdateRequest.getAmortizationType() != null;
        assertions.assertThat(workingCapitalLoanProductsUpdateRequest.getAmortizationType().getValue())
                .isEqualTo(getWorkingCapitalProductResponse.getAmortizationType().getCode());
        assertions.assertThat(workingCapitalLoanProductsUpdateRequest.getNpvDayCount())
                .isEqualTo(getWorkingCapitalProductResponse.getNpvDayCount());
        assertions.assertThat(
                workingCapitalLoanProductsUpdateRequest.getRepaymentEvery().compareTo(getWorkingCapitalProductResponse.getRepaymentEvery()))
                .isEqualTo(0);
        assertions.assertThat(workingCapitalLoanProductsUpdateRequest.getRepaymentFrequencyType().getValue())
                .isEqualTo(getWorkingCapitalProductResponse.getRepaymentFrequencyType().getCode());
        assertions.assertThat(workingCapitalLoanProductsUpdateRequest.getPeriodPaymentRate()
                .compareTo(getWorkingCapitalProductResponse.getPeriodPaymentRate())).isEqualTo(0);
        assertions.assertThat(workingCapitalLoanProductsUpdateRequest.getMinPeriodPaymentRate())
                .isEqualTo(getWorkingCapitalProductResponse.getMinPeriodPaymentRate());
        assertions.assertThat(workingCapitalLoanProductsUpdateRequest.getMaxPeriodPaymentRate())
                .isEqualTo(getWorkingCapitalProductResponse.getMaxPeriodPaymentRate());

        // check payment allocation rules
        assert workingCapitalLoanProductsUpdateRequest.getPaymentAllocation() != null;
        workingCapitalLoanProductsUpdateRequest.getPaymentAllocation().forEach(paymentAllocation -> {
            assert getWorkingCapitalProductResponse.getPaymentAllocation() != null;
            GetPaymentAllocation getPaymentAllocation = getWorkingCapitalProductResponse.getPaymentAllocation().stream()
                    .filter(paymentAllocationSearched -> {
                        assert paymentAllocation.getTransactionType() != null;
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
        checkCreateWorkingCapitalLoanProductWithInvalidDataFailure(workingCapitalLoanProductCreateRequestUpdated, errorMessage);
    }

    public void checkCreateWorkingCapitalLoanProductWithInvalidDataFailure(
            PostWorkingCapitalLoanProductsRequest workingCapitalLoanProductCreateRequestUpdated, String errorMessage) {
        CallFailedRuntimeException exception = fail(
                () -> workingCapitalApi().createWorkingCapitalLoanProduct(workingCapitalLoanProductCreateRequestUpdated, Map.of()));
        assertThat(exception.getStatus()).as(ErrorMessageHelper.incorrectExpectedValueInResponse()).isEqualTo(400);
        assertThat(exception.getDeveloperMessage()).contains(errorMessage);
    }

    public PostWorkingCapitalLoanProductsRequest setWorkingCapitalLoanProductsCreateFieldValue(
            PostWorkingCapitalLoanProductsRequest defaultWorkingCapitalLoanProductCreateRequest, String fieldName, String fieldValue) {
        if (fieldValue.equals("null")) {
            fieldValue = null;
        }
        Integer valueInteger = null;
        BigDecimal valueBigDecimal = null;
        if (fieldName.equalsIgnoreCase(DIGITS_AFTER_DECIMAL_FIELD_NAME) || fieldName.equalsIgnoreCase(IN_MULTIPLES_OF_FIELD_NAME)
                || fieldName.equalsIgnoreCase(NPV_DAY_COUNT_FIELD_NAME) || fieldName.equalsIgnoreCase(REPAYMENT_EVERY_FIELD_NAME)) {
            valueInteger = fieldValue != null ? Integer.valueOf(fieldValue) : null;
        }
        if (fieldName.equalsIgnoreCase(PRINCIPAL_FIELD_NAME) || fieldName.equalsIgnoreCase(MIN_PRINCIPAL_FIELD_NAME)
                || fieldName.equalsIgnoreCase(MAX_PRINCIPAL_FIELD_NAME) || fieldName.equalsIgnoreCase(PERIOD_PAYMENT_RATE_FIELD_NAME)
                || fieldName.equalsIgnoreCase(MIN_PERIOD_PAYMENT_RATE_FIELD_NAME)
                || fieldName.equalsIgnoreCase(MAX_PERIOD_PAYMENT_RATE_FIELD_NAME)) {
            valueBigDecimal = fieldValue != null ? new BigDecimal(fieldValue) : null;
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
                defaultWorkingCapitalLoanProductCreateRequest
                        .setDelinquencyBucketId(fieldValue != null ? Long.parseLong(fieldValue) : null);
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
        updateWorkingCapitalLoanProductWithInvalidDataFailure(defaultWorkingCapitalLoanProductUpdateRequest, productId, fieldName, value,
                errorMessage);
    }

    public void updateWorkingCapitalLoanProductWithInvalidDataFailure(
            PutWorkingCapitalLoanProductsProductIdRequest defaultWorkingCapitalLoanProductUpdateRequest, Long productId, String fieldName,
            String value, String errorMessage) {
        final PutWorkingCapitalLoanProductsProductIdRequest workingCapitalLoanProductUpdateRequestUpdated = setWorkingCapitalLoanProductsUpdateRequest(
                defaultWorkingCapitalLoanProductUpdateRequest, fieldName, value);
        checkUpdateWorkingCapitalLoanProductWithInvalidDataFailure(productId, workingCapitalLoanProductUpdateRequestUpdated, errorMessage);
    }

    public void checkUpdateWorkingCapitalLoanProductWithInvalidDataFailure(Long productId,
            PutWorkingCapitalLoanProductsProductIdRequest workingCapitalLoanProductUpdateRequestUpdated, String errorMessage) {
        CallFailedRuntimeException exception = fail(() -> workingCapitalApi().updateWorkingCapitalLoanProduct(productId,
                workingCapitalLoanProductUpdateRequestUpdated, Map.of()));
        assertThat(exception.getStatus()).as(ErrorMessageHelper.incorrectExpectedValueInResponse()).isEqualTo(400);
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
        if (fieldName.equalsIgnoreCase(DIGITS_AFTER_DECIMAL_FIELD_NAME) || fieldName.equalsIgnoreCase(IN_MULTIPLES_OF_FIELD_NAME)
                || fieldName.equalsIgnoreCase(NPV_DAY_COUNT_FIELD_NAME) || fieldName.equalsIgnoreCase(REPAYMENT_EVERY_FIELD_NAME)) {
            valueInteger = fieldValue != null ? Integer.valueOf(fieldValue) : null;
        }
        if (fieldName.equalsIgnoreCase(PRINCIPAL_FIELD_NAME) || fieldName.equalsIgnoreCase(MIN_PRINCIPAL_FIELD_NAME)
                || fieldName.equalsIgnoreCase(MAX_PRINCIPAL_FIELD_NAME) || fieldName.equalsIgnoreCase(PERIOD_PAYMENT_RATE_FIELD_NAME)
                || fieldName.equalsIgnoreCase(MIN_PERIOD_PAYMENT_RATE_FIELD_NAME)
                || fieldName.equalsIgnoreCase(MAX_PERIOD_PAYMENT_RATE_FIELD_NAME)) {
            valueBigDecimal = fieldValue != null ? new BigDecimal(fieldValue) : null;
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
                defaultWorkingCapitalLoanProductUpdateRequest
                        .setDelinquencyBucketId(fieldValue != null ? Long.parseLong(fieldValue) : null);
            break;
            case LOCALE_FIELD_NAME:
                defaultWorkingCapitalLoanProductUpdateRequest.setLocale(fieldValue);
            break;
            default:
            break;
        }
        return defaultWorkingCapitalLoanProductUpdateRequest;
    }

    public void checkWorkingCapitalLoanProductDeleteFailure(Long productId) {
        CallFailedRuntimeException exception = fail(() -> workingCapitalApi().retrieveOneWorkingCapitalLoanProduct(productId, Map.of()));
        assertThat(exception.getStatus()).as(ErrorMessageHelper.dateFailureErrorCodeMsg()).isEqualTo(404);
        assertThat(exception.getDeveloperMessage())
                .contains(ErrorMessageHelper.workingCapitalLoanProductIdentifiedDoesNotExistFailure(String.valueOf(productId)));
    }

}
