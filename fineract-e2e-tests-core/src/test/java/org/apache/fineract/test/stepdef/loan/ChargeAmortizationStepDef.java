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
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.models.ChargeData;
import org.apache.fineract.client.models.ChargeRequest;
import org.apache.fineract.client.models.GetChargesResponse;
import org.apache.fineract.client.models.StringEnumOptionData;
import org.apache.fineract.test.data.ChargeCalculationType;
import org.apache.fineract.test.data.ChargePaymentMode;
import org.apache.fineract.test.data.ChargeProductAppliesTo;
import org.apache.fineract.test.data.ChargeTimeType;
import org.apache.fineract.test.helper.Utils;
import org.apache.fineract.test.stepdef.AbstractStepDef;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Steps for the amortization configuration of a charge definition. In the data tables an empty cell means the field is
 * not sent, or is expected to be empty.
 */
public class ChargeAmortizationStepDef extends AbstractStepDef {

    private static final String CHARGE_ID = "chargeAmortizationChargeId";
    private static final String CURRENCY_CODE = "EUR";
    private static final String LOCALE_EN = "en";
    private static final String PERIOD_TYPE = "amortizationPeriodType";
    private static final String DAYS = "amortizationDays";
    private static final String CALCULATION_TYPE = "amortizationCalculationType";
    private static final String INCOME_TYPE = "amortizationIncomeType";

    @Autowired
    private FineractFeignClient fineractClient;

    @When("Admin creates a loan fee with {string} charge time type and the following amortization configuration:")
    public void createLoanFeeWithAmortization(final String chargeTimeType, final DataTable table) {
        final ChargeRequest request = withAmortization(loanCharge(ChargeTimeType.valueOf(chargeTimeType), false), table.asMaps().get(0));
        final Long chargeId = ok(() -> fineractClient.charges().createCharge(request)).getResourceId();
        testContext().set(CHARGE_ID, chargeId);
    }

    @When("Admin updates the amortization configuration of the charge to:")
    public void updateAmortization(final DataTable table) {
        final ChargeRequest request = withAmortization(new ChargeRequest().locale(LOCALE_EN), table.asMaps().get(0));
        ok(() -> fineractClient.charges().updateCharge(createdChargeId(), request));
    }

    @Then("The charge has the following amortization configuration:")
    public void verifyAmortization(final DataTable table) {
        final Map<String, String> expected = table.asMaps().get(0);
        final GetChargesResponse charge = ok(() -> fineractClient.charges().retrieveOneCharge(createdChargeId()));

        assertThat(idOf(charge.getAmortizationPeriodType())).as(PERIOD_TYPE).isEqualTo(cell(expected, PERIOD_TYPE));
        assertThat(charge.getAmortizationDays() == null ? null : String.valueOf(charge.getAmortizationDays())).as(DAYS)
                .isEqualTo(cell(expected, DAYS));
        assertThat(idOf(charge.getAmortizationCalculationType())).as(CALCULATION_TYPE).isEqualTo(cell(expected, CALCULATION_TYPE));
        assertThat(idOf(charge.getAmortizationIncomeType())).as(INCOME_TYPE).isEqualTo(cell(expected, INCOME_TYPE));
    }

    @Then("Creating a loan charge with {string} charge time type, penalty {string} and the following amortization configuration results an error:")
    public void createLoanChargeWithAmortizationFails(final String chargeTimeType, final String penalty, final DataTable table) {
        final Map<String, String> data = table.asMaps().get(0);
        final ChargeRequest request = withAmortization(loanCharge(ChargeTimeType.valueOf(chargeTimeType), Boolean.parseBoolean(penalty)),
                data);
        assertFailure(fail(() -> fineractClient.charges().createCharge(request)), data);
    }

    @Then("Turning the charge into a penalty results an error:")
    public void turnChargeIntoPenaltyFails(final DataTable table) {
        final ChargeRequest request = new ChargeRequest().penalty(true).locale(LOCALE_EN);
        assertFailure(fail(() -> fineractClient.charges().updateCharge(createdChargeId(), request)), table.asMaps().get(0));
    }

    @Then("The loan charge template lists the following amortization options:")
    public void verifyTemplateOptions(final DataTable table) {
        final Map<String, String> expected = table.asMaps().get(0);
        final ChargeData template = ok(
                () -> fineractClient.charges().retrieveTemplateCharge(ChargeProductAppliesTo.LOAN.value.longValue(), null));

        assertThat(ids(template.getAmortizationPeriodTypeOptions())).as("amortizationPeriodTypeOptions")
                .isEqualTo(List.of(expected.get("amortizationPeriodTypeOptions").split(",\\s*")));
        assertThat(ids(template.getAmortizationCalculationTypeOptions())).as("amortizationCalculationTypeOptions")
                .isEqualTo(List.of(expected.get("amortizationCalculationTypeOptions").split(",\\s*")));
        assertThat(ids(template.getAmortizationIncomeTypeOptions())).as("amortizationIncomeTypeOptions")
                .isEqualTo(List.of(expected.get("amortizationIncomeTypeOptions").split(",\\s*")));
    }

    private Long createdChargeId() {
        final Long chargeId = testContext().get(CHARGE_ID);
        assertThat(chargeId).as("a charge must be created first in this scenario").isNotNull();
        return chargeId;
    }

    private static ChargeRequest loanCharge(final ChargeTimeType chargeTimeType, final boolean penalty) {
        return new ChargeRequest() //
                .chargeAppliesTo(ChargeProductAppliesTo.LOAN.value) //
                .chargeTimeType(chargeTimeType.value) //
                .chargeCalculationType(ChargeCalculationType.FLAT.value) //
                .chargePaymentMode(ChargePaymentMode.REGULAR.value) //
                .name(Utils.randomStringGenerator("AMORTIZED_FEE_", 8)) //
                .amount(10.0D) //
                .active(true) //
                .currencyCode(CURRENCY_CODE) //
                .locale(LOCALE_EN) //
                .penalty(penalty);
    }

    private static ChargeRequest withAmortization(final ChargeRequest request, final Map<String, String> data) {
        final String days = cell(data, DAYS);
        return request.amortizationPeriodType(cell(data, PERIOD_TYPE)) //
                .amortizationDays(days == null ? null : Integer.valueOf(days)) //
                .amortizationCalculationType(cell(data, CALCULATION_TYPE)) //
                .amortizationIncomeType(cell(data, INCOME_TYPE));
    }

    private static void assertFailure(final CallFailedRuntimeException failure, final Map<String, String> data) {
        final int expectedHttpCode = Integer.parseInt(data.get("httpCode"));
        final String expectedErrorMessage = data.get("errorMessage").trim();
        assertThat(failure.getStatus()).as("HTTP status code").isEqualTo(expectedHttpCode);
        assertThat(failure.getMessage()).as("Error message should contain: " + expectedErrorMessage).contains(expectedErrorMessage);
    }

    private static String cell(final Map<String, String> data, final String column) {
        final String value = data.get(column);
        return StringUtils.isBlank(value) ? null : value.trim();
    }

    private static String idOf(final StringEnumOptionData option) {
        return option == null ? null : option.getId();
    }

    private static List<String> ids(final List<StringEnumOptionData> options) {
        assertThat(options).as("template options").isNotEmpty();
        return options.stream().map(StringEnumOptionData::getId).toList();
    }
}
