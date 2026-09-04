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
package org.apache.fineract.portfolio.workingcapitalloan.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.math.MathContext;
import java.time.LocalDate;
import org.apache.fineract.organisation.monetary.data.CurrencyData;
import org.apache.fineract.portfolio.workingcapitalloan.calc.ProjectedAmortizationScheduleModel;
import org.apache.fineract.portfolio.workingcapitalloan.calc.ProjectedAmortizationScheduleModel.PrincipalAdjustment;
import org.apache.fineract.portfolio.workingcapitalloan.calc.ProjectedPayment;
import org.junit.jupiter.api.Test;

class ProjectedAmortizationScheduleModelParserServiceGsonImplTest {

    private static final MathContext MC = MathContext.DECIMAL128;
    private static final CurrencyData CURRENCY = new CurrencyData("EUR", 2, null);
    private static final LocalDate DISBURSEMENT = LocalDate.of(2026, 1, 1);
    private static final LocalDate ADJUSTMENT_DATE = LocalDate.of(2026, 1, 10);

    private final ProjectedAmortizationScheduleModelParserServiceGsonImpl parser = new ProjectedAmortizationScheduleModelParserServiceGsonImpl();

    private ProjectedAmortizationScheduleModel model() {
        return ProjectedAmortizationScheduleModel.generate(new BigDecimal("500"), new BigDecimal("9000"), new BigDecimal("100000"),
                new BigDecimal("18"), 360, DISBURSEMENT, MC, CURRENCY, DISBURSEMENT);
    }

    @Test
    void restoresPrincipalAdjustments() {
        final ProjectedAmortizationScheduleModel model = model();
        model.applyPrincipalAdjustment(ADJUSTMENT_DATE, new BigDecimal("200"));

        final ProjectedAmortizationScheduleModel restored = parser.fromJson(parser.toJson(model), MC, CURRENCY);

        assertNotNull(restored);
        assertEquals(1, restored.principalAdjustments().size());
        final PrincipalAdjustment adjustment = restored.principalAdjustments().getFirst();
        assertEquals(ADJUSTMENT_DATE, adjustment.date());
        assertEquals(0, adjustment.amount().getAmount().compareTo(new BigDecimal("200")));

        final ProjectedPayment adjustedPayment = restored.projectedPayments().stream()
                .filter(payment -> ADJUSTMENT_DATE.equals(payment.date())).findFirst().orElseThrow();
        assertEquals(0, adjustedPayment.expectedPaymentAmount().getAmount().compareTo(new BigDecimal("250")),
                "the adjusted expected payment must survive the round trip");
    }

    /**
     * Rate changes moved from a list of precomputed segments to the date and rate they were booked with, so a model
     * persisted at the old version carries a field this one does not read.
     *
     * <p>
     * It must load rather than fail: the rate changes themselves live in {@code m_wc_loan_period_payment_rate_change},
     * not here, so the schedule is restated from that table the next time anything writes the loan. What must not
     * happen is the old field being read into the new one - the two have no fields in common, so every entry would come
     * back empty and the walk would be handed a rate change with no date and no rate.
     */
    @Test
    void readsAModelPersistedWithTheOldRateSegmentShape() {
        final ProjectedAmortizationScheduleModel model = model();
        final String legacyJson = parser.toJson(model).replace("\"rateChanges\":[]",
                "\"rateSegments\":[{\"startDayIndex\":9,\"segmentTerm\":180,\"effectiveInterestRate\":0.001,"
                        + "\"expectedPaymentAmount\":40.00,\"netDisbursementAtSplit\":8000.00,\"discountAtSplit\":400.00,"
                        + "\"finalPaymentAmount\":20.00}]");
        assertTrue(legacyJson.contains("rateSegments"), "the legacy fixture must carry the old field");
        assertTrue(!legacyJson.contains("rateChanges"), "and must not carry the new one");

        final ProjectedAmortizationScheduleModel restored = parser.fromJson(legacyJson, MC, CURRENCY);

        assertNotNull(restored, "an old model must still load");
        assertTrue(restored.rateChanges().isEmpty(), "the old shape must be ignored, not misread");
        assertEquals(190, restored.projectedPayments().size() - 1, "the schedule rebuilds at the rate the loan was written with");
    }

    /** Models persisted before principal adjustments existed carry no such field and must still deserialize. */
    @Test
    void readsAModelPersistedWithoutPrincipalAdjustments() {
        final String legacyJson = parser.toJson(model()).replace("\"principalAdjustments\":[],", "");
        assertTrue(!legacyJson.contains("principalAdjustments"), "the legacy fixture must not carry the field");

        final ProjectedAmortizationScheduleModel restored = parser.fromJson(legacyJson, MC, CURRENCY);

        assertNotNull(restored);
        assertTrue(restored.principalAdjustments().isEmpty());
    }
}
