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
package org.apache.fineract.portfolio.loanaccount.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class LoanDisbursementDetailsTest {

    private static final LocalDate EXPECTED_DATE = LocalDate.of(2026, 1, 15);

    private static LoanDisbursementDetails details(final LocalDate expectedDate, final BigDecimal principal) {
        return new LoanDisbursementDetails(expectedDate, null, principal, null, false);
    }

    private static LoanDisbursementDetails details(final BigDecimal principal) {
        return details(EXPECTED_DATE, principal);
    }

    @Test
    void equalsAndHashCodeIgnorePrincipalScale() {
        final LoanDisbursementDetails stored = details(new BigDecimal("1000.000000"));
        final LoanDisbursementDetails fromRequest = details(new BigDecimal("1000"));

        assertEquals(stored, fromRequest);
        assertEquals(fromRequest, stored);
        assertEquals(stored.hashCode(), fromRequest.hashCode());
    }

    @Test
    void equalsAndHashCodeIgnoreScaleOfZeroPrincipal() {
        final LoanDisbursementDetails zero = details(new BigDecimal("0"));
        final LoanDisbursementDetails zeroWithScale = details(new BigDecimal("0.000000"));

        assertEquals(zero, zeroWithScale);
        assertEquals(zero.hashCode(), zeroWithScale.hashCode());
    }

    @Test
    void equalsDistinguishesDifferentPrincipals() {
        assertNotEquals(details(new BigDecimal("1000")), details(new BigDecimal("1000.01")));
    }

    @Test
    void equalsDistinguishesDifferentExpectedDisbursementDates() {
        assertNotEquals(details(EXPECTED_DATE, new BigDecimal("1000")), details(EXPECTED_DATE.plusDays(1), new BigDecimal("1000")));
    }
}
