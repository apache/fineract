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
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.util.Map;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.models.ChargeRequest;
import org.apache.fineract.test.data.ChargeCalculationType;
import org.apache.fineract.test.data.ChargePaymentMode;
import org.apache.fineract.test.data.ChargeProductAppliesTo;
import org.apache.fineract.test.data.ChargeProductResolver;
import org.apache.fineract.test.data.ChargeProductType;
import org.apache.fineract.test.data.ChargeTimeType;
import org.apache.fineract.test.helper.Utils;
import org.apache.fineract.test.stepdef.AbstractStepDef;
import org.springframework.beans.factory.annotation.Autowired;

public class ChargeStepDef extends AbstractStepDef {

    private static final String CURRENCY_CODE = "EUR";
    private static final String LOCALE_EN = "en";

    @Autowired
    private FineractFeignClient fineractClient;

    @Autowired
    private ChargeProductResolver chargeProductResolver;

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

    private void assertChargeCreateFails(final ChargeRequest request, final DataTable table) {
        final Map<String, String> expectedData = table.asMaps().get(0);
        final int expectedHttpCode = Integer.parseInt(expectedData.get("httpCode"));
        final String expectedErrorMessage = expectedData.get("errorMessage").trim();

        final CallFailedRuntimeException exception = fail(() -> fineractClient.charges().createCharge(request));
        assertThat(exception.getStatus()).as("HTTP status code should be " + expectedHttpCode).isEqualTo(expectedHttpCode);
        assertThat(exception.getMessage()).as("Error message should contain: " + expectedErrorMessage).contains(expectedErrorMessage);
    }
}
