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

import io.cucumber.datatable.DataTable;
import io.cucumber.java.After;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.models.ChargeRequest;
import org.apache.fineract.client.models.GetChargesResponse;
import org.apache.fineract.client.models.GetLoanProductsResponse;
import org.apache.fineract.test.data.ChargeCalculationType;
import org.apache.fineract.test.data.ChargePaymentMode;
import org.apache.fineract.test.data.ChargeProductAppliesTo;
import org.apache.fineract.test.data.ChargeProductResolver;
import org.apache.fineract.test.data.ChargeProductType;
import org.apache.fineract.test.data.ChargeTimeType;
import org.apache.fineract.test.data.loanproduct.DefaultLoanProduct;
import org.apache.fineract.test.factory.LoanProductsRequestFactory;
import org.apache.fineract.test.helper.Utils;
import org.apache.fineract.test.stepdef.AbstractStepDef;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
public class ChargeStepDef extends AbstractStepDef {

    private static final String CURRENCY_CODE = "EUR";
    private static final String LOCALE_EN = "en";
    private static final String REMEMBERED_CHARGE_PREFIX = "rememberedCharge:";

    @Autowired
    private FineractFeignClient fineractClient;

    @Autowired
    private ChargeProductResolver chargeProductResolver;

    @Autowired
    private LoanProductsRequestFactory loanProductsRequestFactory;

    /**
     * Down payment fee charges (charge time type 17) are created here rather than in the global initializer, so that
     * the global data stays accepted by a backend without the down payment fee charge type.
     */
    @Given("Down payment fee charges and loan products exist")
    public void downPaymentFeeChargesAndLoanProductsExist() {
        createLoanChargeIfNotExists(ChargeProductType.LOAN_DOWN_PAYMENT_FEE, ChargeCalculationType.FLAT, 10.0D);
        createLoanChargeIfNotExists(ChargeProductType.LOAN_DOWN_PAYMENT_PERCENTAGE_FEE, ChargeCalculationType.PERCENTAGE_AMOUNT, 10.0D);

        final String productName = DefaultLoanProduct.LP2_DOWNPAYMENT_AUTO_ADV_PMT_ALLOC_DP_FEE_ON_PRODUCT.getName();
        final List<GetLoanProductsResponse> products = ok(() -> fineractClient.loanProducts().retrieveAllLoanProducts(Map.of()));
        if (products.stream().noneMatch(product -> productName.equals(product.getName()))) {
            final Long chargeId = chargeProductResolver.resolve(ChargeProductType.LOAN_DOWN_PAYMENT_FEE);
            ok(() -> fineractClient.loanProducts()
                    .createLoanProduct(loanProductsRequestFactory.defaultLoanProductsRequestLP2DownPaymentFeeOnProduct(chargeId)));
        }
    }

    private void createLoanChargeIfNotExists(final ChargeProductType chargeType, final ChargeCalculationType calculationType,
            final double amount) {
        final boolean exists = ok(() -> fineractClient.charges().retrieveAllCharges(Map.of())).stream()
                .anyMatch(charge -> chargeType.getName().equals(charge.getName()));
        if (exists) {
            return;
        }
        final ChargeRequest request = new ChargeRequest() //
                .chargeAppliesTo(ChargeProductAppliesTo.LOAN.value) //
                .chargeTimeType(ChargeTimeType.DOWN_PAYMENT.value) //
                .chargeCalculationType(calculationType.value) //
                .chargePaymentMode(ChargePaymentMode.REGULAR.value) //
                .name(chargeType.getName()) //
                .amount(amount) //
                .active(true) //
                .currencyCode(CURRENCY_CODE) //
                .locale(LOCALE_EN) //
                .penalty(false);
        ok(() -> fineractClient.charges().createCharge(request));
    }

    @When("Admin remembers charge {string} for restoration")
    public void rememberChargeForRestoration(final String chargeType) {
        final ChargeProductType chargeProductType = ChargeProductType.valueOf(chargeType);
        final Long chargeId = chargeProductResolver.resolve(chargeProductType);
        final GetChargesResponse charge = ok(() -> fineractClient.charges().retrieveOneCharge(chargeId));
        assertThat(charge).as("Charge %s before modification", chargeId).isNotNull();
        assertThat(charge.getId()).as("Charge ID before modification").isEqualTo(chargeId);
        assertThat(charge.getAmount()).as("Charge %s amount before modification", chargeId).isNotNull();
        assertThat(charge.getChargeCalculationType()).as("Charge %s calculation type before modification", chargeId).isNotNull();
        assertThat(charge.getChargeCalculationType().getId()).as("Charge %s calculation type ID before modification", chargeId).isNotNull();
        assertThat(testContext().get().containsKey(REMEMBERED_CHARGE_PREFIX + chargeId))
                .as("Charge %s has already been remembered for restoration", chargeId).isFalse();
        testContext().set(REMEMBERED_CHARGE_PREFIX + chargeId, charge);
    }

    @After(value = "@ProgressiveLoanBackdatedPeriodicAccrual", order = Integer.MAX_VALUE - 1)
    public void restoreRememberedCharges() {
        final List<GetChargesResponse> charges = testContext().get().entrySet().stream()
                .filter(entry -> entry.getKey().startsWith(REMEMBERED_CHARGE_PREFIX)).map(entry -> (GetChargesResponse) entry.getValue())
                .toList();
        final IllegalStateException cleanupFailure = new IllegalStateException("Unable to restore remembered charges");
        charges.forEach((final GetChargesResponse charge) -> {
            try {
                final ChargeRequest request = new ChargeRequest().amount(charge.getAmount())
                        .chargeCalculationType(Math.toIntExact(charge.getChargeCalculationType().getId())).locale(LOCALE_EN);
                ok(() -> fineractClient.charges().updateCharge(charge.getId(), request));
            } catch (final RuntimeException | AssertionError exception) {
                log.error("Failed to restore remembered charge {}", charge.getId(), exception);
                cleanupFailure.addSuppressed(new IllegalStateException("Charge " + charge.getId(), exception));
            }
        });
        if (cleanupFailure.getSuppressed().length > 0) {
            throw cleanupFailure;
        }
    }

    @When("Admin updates charge {string} with {string} calculation type and {double} % of transaction amount")
    public void updateCharge(String chargeType, String chargeCalculationType, double amount) {
        ChargeRequest disbursementChargeUpdateRequest = new ChargeRequest();
        ChargeCalculationType chargeProductTypeValue = ChargeCalculationType.valueOf(chargeCalculationType);
        disbursementChargeUpdateRequest.chargeCalculationType(chargeProductTypeValue.value).amount(amount).locale("en");

        ChargeProductType chargeProductType = ChargeProductType.valueOf(chargeType);
        Long chargeId = chargeProductResolver.resolve(chargeProductType);

        ok(() -> fineractClient.charges().updateCharge(chargeId, disbursementChargeUpdateRequest));
    }

    @When("Admin updates charge {string} with {string} calculation type and {double} EUR amount")
    public void updateChargeWithFlatAmount(String chargeType, String chargeCalculationType, double flatAmount) {
        ChargeRequest disbursementChargeUpdateRequest = new ChargeRequest();
        ChargeCalculationType chargeProductTypeValue = ChargeCalculationType.valueOf(chargeCalculationType);
        disbursementChargeUpdateRequest.chargeCalculationType(chargeProductTypeValue.value).amount(flatAmount).locale("en");

        ChargeProductType chargeProductType = ChargeProductType.valueOf(chargeType);
        Long chargeId = chargeProductResolver.resolve(chargeProductType);

        ok(() -> fineractClient.charges().updateCharge(chargeId, disbursementChargeUpdateRequest));
    }

    @Then("Creating loan charge with {string} chargeTimeType and {string} chargeCalculationType results an error with the following data:")
    public void createLoanChargeWithInvalidParamsFails(String chargeTimeTypeName, String chargeCalcTypeName, DataTable table) {
        final ChargeTimeType timeType = ChargeTimeType.valueOf(chargeTimeTypeName);
        final ChargeCalculationType calcType = ChargeCalculationType.valueOf(chargeCalcTypeName);
        final ChargeRequest request = new ChargeRequest() //
                .chargeAppliesTo(ChargeProductAppliesTo.LOAN.value) //
                .chargeTimeType(timeType.value) //
                .chargeCalculationType(calcType.value) //
                .chargePaymentMode(ChargePaymentMode.REGULAR.value) //
                .name(Utils.randomStringGenerator("DP_FEE_", 8)) //
                .amount(10.0D) //
                .active(true) //
                .currencyCode(CURRENCY_CODE) //
                .locale(LOCALE_EN) //
                .penalty(false);

        assertChargeCreateFails(request, table);
    }

    @Then("Creating {string} charge with {string} chargeTimeType and {string} chargeCalculationType results an error with the following data:")
    public void createChargeWithAppliesToAndInvalidParamsFails(String appliesToName, String chargeTimeTypeName, String chargeCalcTypeName,
            DataTable table) {
        final ChargeProductAppliesTo appliesTo = ChargeProductAppliesTo.valueOf(appliesToName);
        final ChargeTimeType timeType = ChargeTimeType.valueOf(chargeTimeTypeName);
        final ChargeCalculationType calcType = ChargeCalculationType.valueOf(chargeCalcTypeName);
        final ChargeRequest request = new ChargeRequest() //
                .chargeAppliesTo(appliesTo.value) //
                .chargeTimeType(timeType.value) //
                .chargeCalculationType(calcType.value) //
                .name(Utils.randomStringGenerator("DP_FEE_", 8)) //
                .amount(10.0D) //
                .active(true) //
                .currencyCode(CURRENCY_CODE) //
                .locale(LOCALE_EN) //
                .penalty(false);
        if (appliesTo == ChargeProductAppliesTo.LOAN) {
            request.chargePaymentMode(ChargePaymentMode.REGULAR.value);
        }

        assertChargeCreateFails(request, table);
    }

    @Then("Changing a charge attached to a loan product without down payment to {string} chargeTimeType results an error with the following data:")
    public void changeChargeTimeTypeOfChargeOnProductWithoutDownPaymentFails(final String chargeTimeTypeName, final DataTable table) {
        final ChargeRequest chargeRequest = new ChargeRequest() //
                .chargeAppliesTo(ChargeProductAppliesTo.LOAN.value) //
                .chargeTimeType(ChargeTimeType.SPECIFIED_DUE_DATE.value) //
                .chargeCalculationType(ChargeCalculationType.FLAT.value) //
                .chargePaymentMode(ChargePaymentMode.REGULAR.value) //
                .name(Utils.randomStringGenerator("DP_FEE_", 8)) //
                .amount(10.0D) //
                .active(true) //
                .currencyCode(CURRENCY_CODE) //
                .locale(LOCALE_EN) //
                .penalty(false);
        final Long chargeId = ok(() -> fineractClient.charges().createCharge(chargeRequest)).getResourceId();
        ok(() -> fineractClient.loanProducts()
                .createLoanProduct(loanProductsRequestFactory.defaultLoanProductsRequestLP2WithoutDownPayment(chargeId)));

        final ChargeRequest updateRequest = new ChargeRequest().chargeTimeType(ChargeTimeType.valueOf(chargeTimeTypeName).value)
                .locale(LOCALE_EN);
        assertCallFails(() -> fineractClient.charges().updateCharge(chargeId, updateRequest), table);
    }

    private void assertChargeCreateFails(final ChargeRequest request, final DataTable table) {
        assertCallFails(() -> fineractClient.charges().createCharge(request), table);
    }

    private void assertCallFails(final Supplier<?> call, final DataTable table) {
        final Map<String, String> expectedData = table.asMaps().get(0);
        final int expectedHttpCode = Integer.parseInt(expectedData.get("httpCode"));
        final String expectedErrorMessage = expectedData.get("errorMessage").trim();

        final CallFailedRuntimeException exception = fail(call);
        assertThat(exception.getStatus()).as("HTTP status code should be " + expectedHttpCode).isEqualTo(expectedHttpCode);
        assertThat(exception.getMessage()).as("Error message should contain: " + expectedErrorMessage).contains(expectedErrorMessage);
    }
}
