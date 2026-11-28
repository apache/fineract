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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.apache.fineract.portfolio.charge.domain.ChargeTimeType;
import org.junit.jupiter.api.Test;

/**
 * A tranche disbursement charge belongs to its own tranche, compared by id: tranches of the same day share the date and
 * the principal.
 */
public class LoanChargeTrancheTest {

    private static final LocalDate JAN_1 = LocalDate.of(2026, 1, 1);

    @Test
    public void testTrancheDisbursementChargeBelongsOnlyToItsOwnTranche() {
        final LoanDisbursementDetails ownTranche = tranche(1L);
        final LoanDisbursementDetails ownTrancheLoadedAgain = tranche(1L);
        final LoanDisbursementDetails sameDayTranche = tranche(2L);
        final LoanCharge fee = new LoanCharge();
        fee.setChargeTime(ChargeTimeType.TRANCHE_DISBURSEMENT.getValue());
        fee.updateLoanTrancheDisbursementCharge(new LoanTrancheDisbursementCharge(fee, ownTranche));

        assertTrue(fee.isTrancheDisbursementChargeOf(ownTranche));
        assertTrue(fee.isTrancheDisbursementChargeOf(ownTrancheLoadedAgain));
        assertFalse(fee.isTrancheDisbursementChargeOf(sameDayTranche));
        assertFalse(fee.isTrancheDisbursementChargeOf(null));
    }

    @Test
    public void testUnsavedTranchesAreComparedAsInstances() {
        final LoanDisbursementDetails ownTranche = tranche(null);
        final LoanCharge fee = new LoanCharge();
        fee.setChargeTime(ChargeTimeType.TRANCHE_DISBURSEMENT.getValue());
        fee.updateLoanTrancheDisbursementCharge(new LoanTrancheDisbursementCharge(fee, ownTranche));

        assertTrue(fee.isTrancheDisbursementChargeOf(ownTranche));
        assertFalse(fee.isTrancheDisbursementChargeOf(tranche(null)));
    }

    @Test
    public void testOnlyTrancheDisbursementChargesBelongToATranche() {
        final LoanDisbursementDetails ownTranche = tranche(1L);
        final LoanCharge fee = new LoanCharge();
        fee.setChargeTime(ChargeTimeType.DOWN_PAYMENT.getValue());
        fee.updateLoanTrancheDisbursementCharge(new LoanTrancheDisbursementCharge(fee, ownTranche));

        assertFalse(fee.isTrancheDisbursementChargeOf(ownTranche));

        final LoanCharge withoutTranche = new LoanCharge();
        withoutTranche.setChargeTime(ChargeTimeType.TRANCHE_DISBURSEMENT.getValue());
        assertFalse(withoutTranche.isTrancheDisbursementChargeOf(ownTranche));
    }

    private static LoanDisbursementDetails tranche(final Long id) {
        final LoanDisbursementDetails tranche = new LoanDisbursementDetails(JAN_1, JAN_1, BigDecimal.valueOf(500), null, false);
        tranche.setId(id);
        return tranche;
    }
}
