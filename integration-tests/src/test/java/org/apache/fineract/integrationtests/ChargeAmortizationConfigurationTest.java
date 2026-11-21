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
package org.apache.fineract.integrationtests;

import static org.apache.fineract.integrationtests.client.feign.helpers.FeignWorkingCapitalLoanHelper.errorCodesOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.models.ChargeCreateRequest;
import org.apache.fineract.client.models.ChargeData;
import org.apache.fineract.client.models.ChargeUpdateRequest;
import org.apache.fineract.client.models.StringEnumOptionData;
import org.apache.fineract.integrationtests.client.FeignIntegrationTest;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignChargesHelper;
import org.apache.fineract.integrationtests.client.feign.modules.ChargeRequestBuilders;
import org.apache.fineract.portfolio.charge.domain.ChargeAppliesTo;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Amortization configuration on the charge definition (charge amortization FR-01 to FR-07).
 */
public class ChargeAmortizationConfigurationTest extends FeignIntegrationTest {

    private static final double AMOUNT = 100.0;
    private static final String LOCALE = "en";

    private FeignChargesHelper chargesHelper;
    private final List<Long> createdChargeIds = new ArrayList<>();

    @BeforeAll
    public void setupChargeAmortizationHelpers() {
        chargesHelper = new FeignChargesHelper(fineractClient());
    }

    @AfterAll
    public void deleteCreatedCharges() {
        createdChargeIds.forEach(chargesHelper::deleteCharge);
    }

    @Test
    public void chargeWithoutAmortizationFieldsIsNotAmortized() {
        final ChargeData charge = chargesHelper.getCharge(create(ChargeRequestBuilders.loanDisbursementFee(AMOUNT)));

        assertEquals("NONE", charge.getAmortizationPeriodType().getId());
        assertEquals("chargeAmortizationPeriodType.none", charge.getAmortizationPeriodType().getCode());
        assertNull(charge.getAmortizationDays());
        assertNull(charge.getAmortizationCalculationType());
        assertNull(charge.getAmortizationIncomeType());
    }

    @Test
    public void fullLoanTermChargeDefaultsToFlatFee() {
        final Long chargeId = create(ChargeRequestBuilders.loanDisbursementFee(AMOUNT).amortizationPeriodType("FULL_LOAN_TERM"));

        final ChargeData charge = chargesHelper.getCharge(chargeId);
        assertEquals("FULL_LOAN_TERM", charge.getAmortizationPeriodType().getId());
        assertNull(charge.getAmortizationDays());
        assertEquals("FLAT", charge.getAmortizationCalculationType().getId());
        assertEquals("FEE", charge.getAmortizationIncomeType().getId());

        final ChargeData listed = chargesHelper.getAllCharges().stream().filter(c -> chargeId.equals(c.getId())).findFirst().orElseThrow();
        assertEquals("FULL_LOAN_TERM", listed.getAmortizationPeriodType().getId());
        assertEquals("FLAT", listed.getAmortizationCalculationType().getId());
        assertEquals("FEE", listed.getAmortizationIncomeType().getId());
    }

    @Test
    public void numberOfDaysConfigurationCanBeChangedAndRemoved() {
        final Long chargeId = create(ChargeRequestBuilders.loanSpecifiedDueDateFee(AMOUNT));

        final Map<String, Object> amortized = chargesHelper.updateCharge(chargeId, new ChargeUpdateRequest().locale(LOCALE)
                .amortizationPeriodType("NUMBER_OF_DAYS").amortizationDays(30).amortizationIncomeType("INTEREST")).getChanges();
        assertEquals("NUMBER_OF_DAYS", amortized.get("amortizationPeriodType"));
        assertEquals(30, ((Number) amortized.get("amortizationDays")).intValue());
        assertEquals("FLAT", amortized.get("amortizationCalculationType"));
        assertEquals("INTEREST", amortized.get("amortizationIncomeType"));

        ChargeData charge = chargesHelper.getCharge(chargeId);
        assertEquals("NUMBER_OF_DAYS", charge.getAmortizationPeriodType().getId());
        assertEquals(30, charge.getAmortizationDays());
        assertEquals("FLAT", charge.getAmortizationCalculationType().getId());
        assertEquals("INTEREST", charge.getAmortizationIncomeType().getId());

        chargesHelper.updateCharge(chargeId, new ChargeUpdateRequest().locale(LOCALE).amortizationPeriodType("NONE"));

        charge = chargesHelper.getCharge(chargeId);
        assertEquals("NONE", charge.getAmortizationPeriodType().getId());
        assertNull(charge.getAmortizationDays());
        assertNull(charge.getAmortizationCalculationType());
        assertNull(charge.getAmortizationIncomeType());
    }

    @Test
    public void numberOfDaysWithoutDaysIsRejected() {
        assertRejected("validation.msg.charge.amortizationDays.cannot.be.blank", chargesHelper
                .createChargeExpectingError(ChargeRequestBuilders.loanDisbursementFee(AMOUNT).amortizationPeriodType("NUMBER_OF_DAYS")));
    }

    @Test
    public void penaltyCannotBeAmortized() {
        assertRejected("validation.msg.charge.amortizationPeriodType.not.supported.for.penalty", chargesHelper.createChargeExpectingError(
                ChargeRequestBuilders.loanSpecifiedDueDatePenalty(AMOUNT).amortizationPeriodType("FULL_LOAN_TERM")));
    }

    @Test
    public void instalmentFeeCannotBeAmortized() {
        assertRejected("validation.msg.charge.amortizationPeriodType.not.supported.for.charge.time.type", chargesHelper
                .createChargeExpectingError(ChargeRequestBuilders.loanInstallmentFee(AMOUNT).amortizationPeriodType("FULL_LOAN_TERM")));
    }

    @Test
    public void effectiveRateRequiresFullLoanTerm() {
        assertRejected("validation.msg.charge.amortizationCalculationType.effective.rate.supported.only.for.full.loan.term",
                chargesHelper.createChargeExpectingError(ChargeRequestBuilders.loanDisbursementFee(AMOUNT)
                        .amortizationPeriodType("NUMBER_OF_DAYS").amortizationDays(30).amortizationCalculationType("EFFECTIVE_RATE")));
    }

    @Test
    public void amortizedChargeCannotBeTurnedIntoAPenalty() {
        final Long chargeId = create(ChargeRequestBuilders.loanSpecifiedDueDateFee(AMOUNT).amortizationPeriodType("FULL_LOAN_TERM"));

        assertRejected("validation.msg.charge.amortizationPeriodType.not.supported.for.penalty",
                chargesHelper.updateChargeExpectingError(chargeId, new ChargeUpdateRequest().locale(LOCALE).penalty(true)));
        assertEquals("FULL_LOAN_TERM", chargesHelper.getCharge(chargeId).getAmortizationPeriodType().getId());
    }

    @Test
    public void templateListsTheAmortizationOptions() {
        final ChargeData template = chargesHelper.getChargeTemplate(ChargeAppliesTo.LOAN.getValue().longValue(), null);

        assertEquals(List.of("NONE", "FULL_LOAN_TERM", "NUMBER_OF_DAYS"), ids(template.getAmortizationPeriodTypeOptions()));
        assertEquals(List.of("FLAT", "EFFECTIVE_RATE"), ids(template.getAmortizationCalculationTypeOptions()));
        assertEquals(List.of("FEE", "INTEREST"), ids(template.getAmortizationIncomeTypeOptions()));
    }

    private Long create(final ChargeCreateRequest request) {
        final Long chargeId = chargesHelper.createCharge(request).getResourceId();
        createdChargeIds.add(chargeId);
        return chargeId;
    }

    private static void assertRejected(final String expectedCode, final CallFailedRuntimeException failure) {
        assertEquals(400, failure.getStatus(), "a validation rejection is reported as 400");
        assertEquals(List.of(expectedCode), errorCodesOf(failure));
    }

    private static List<String> ids(final List<StringEnumOptionData> options) {
        assertTrue(options != null && !options.isEmpty(), "the template must list the options");
        return options.stream().map(StringEnumOptionData::getId).toList();
    }
}
