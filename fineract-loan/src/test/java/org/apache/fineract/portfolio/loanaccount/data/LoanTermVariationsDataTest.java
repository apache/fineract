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
package org.apache.fineract.portfolio.loanaccount.data;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTermVariationType;
import org.junit.jupiter.api.Test;

/**
 * Same-date term variations must sort in an order that does not depend on how they were collected. A reschedule request
 * hands its variations over from an unordered set.
 */
class LoanTermVariationsDataTest {

    private static final LocalDate DATE = LocalDate.of(2026, 11, 10);

    @Test
    void graceOnPrincipalSortsBeforeItsExtensionWhicheverComesFirst() {
        assertEquals(List.of(LoanTermVariationType.GRACE_ON_PRINCIPAL, LoanTermVariationType.EXTEND_REPAYMENT_PERIOD),
                sortedTypes(LoanTermVariationType.EXTEND_REPAYMENT_PERIOD, LoanTermVariationType.GRACE_ON_PRINCIPAL));
        assertEquals(List.of(LoanTermVariationType.GRACE_ON_PRINCIPAL, LoanTermVariationType.EXTEND_REPAYMENT_PERIOD),
                sortedTypes(LoanTermVariationType.GRACE_ON_PRINCIPAL, LoanTermVariationType.EXTEND_REPAYMENT_PERIOD));
    }

    @Test
    void bothGracesSortBeforeTheExtensions() {
        assertEquals(
                List.of(LoanTermVariationType.GRACE_ON_PRINCIPAL, LoanTermVariationType.GRACE_ON_INTEREST,
                        LoanTermVariationType.EXTEND_REPAYMENT_PERIOD, LoanTermVariationType.EXTEND_REPAYMENT_PERIOD),
                sortedTypes(LoanTermVariationType.EXTEND_REPAYMENT_PERIOD, LoanTermVariationType.GRACE_ON_PRINCIPAL,
                        LoanTermVariationType.EXTEND_REPAYMENT_PERIOD, LoanTermVariationType.GRACE_ON_INTEREST));
    }

    @Test
    void dueDateVariationStillSortsFirst() {
        assertEquals(List.of(LoanTermVariationType.DUE_DATE, LoanTermVariationType.GRACE_ON_PRINCIPAL),
                sortedTypes(LoanTermVariationType.GRACE_ON_PRINCIPAL, LoanTermVariationType.DUE_DATE));
    }

    @Test
    void otherVariationsSortAfterTheExtension() {
        assertEquals(List.of(LoanTermVariationType.EXTEND_REPAYMENT_PERIOD, LoanTermVariationType.EMI_AMOUNT),
                sortedTypes(LoanTermVariationType.EMI_AMOUNT, LoanTermVariationType.EXTEND_REPAYMENT_PERIOD));
    }

    @Test
    void earlierDateStillWins() {
        final List<LoanTermVariationsData> variations = new ArrayList<>();
        variations.add(variation(LoanTermVariationType.GRACE_ON_PRINCIPAL, DATE.plusMonths(1)));
        variations.add(variation(LoanTermVariationType.EXTEND_REPAYMENT_PERIOD, DATE));
        Collections.sort(variations);

        assertEquals(LoanTermVariationType.EXTEND_REPAYMENT_PERIOD, variations.get(0).getTermVariationType());
    }

    private static List<LoanTermVariationType> sortedTypes(final LoanTermVariationType... types) {
        final List<LoanTermVariationsData> variations = new ArrayList<>();
        for (final LoanTermVariationType type : types) {
            variations.add(variation(type, DATE));
        }
        Collections.sort(variations);
        return variations.stream().map(LoanTermVariationsData::getTermVariationType).toList();
    }

    private static LoanTermVariationsData variation(final LoanTermVariationType type, final LocalDate date) {
        return new LoanTermVariationsData(null, type.getValue(), date, BigDecimal.valueOf(2), null, false);
    }
}
