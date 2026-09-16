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
package org.apache.fineract.portfolio.charge.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class ChargeCalculationTypeTest {

    @Test
    void everyWorkingCapitalProductTimeTypeHasCalculationTypes() {
        for (ChargeTimeType chargeTimeType : ChargeTimeType.validWorkingCapitalLoanProduct()) {
            assertFalse(ChargeCalculationType.validEnumsForWorkingCapitalLoan(chargeTimeType).isEmpty(),
                    chargeTimeType + " is a valid WC product time type but maps to no calculation type");
        }
    }

    @Test
    void workingCapitalCalculationTypesPerTimeType() {
        assertEquals(List.of(ChargeCalculationType.FLAT, ChargeCalculationType.PERCENT_OF_AMOUNT),
                ChargeCalculationType.validEnumsForWorkingCapitalLoan(ChargeTimeType.DISBURSEMENT));
        assertEquals(List.of(ChargeCalculationType.FLAT),
                ChargeCalculationType.validEnumsForWorkingCapitalLoan(ChargeTimeType.SPECIFIED_DUE_DATE));
        assertTrue(ChargeCalculationType.validEnumsForWorkingCapitalLoan(ChargeTimeType.INSTALMENT_FEE).isEmpty());
        assertTrue(ChargeCalculationType.validEnumsForWorkingCapitalLoan(null).isEmpty());
    }

    @Test
    void workingCapitalUnionIsDerivedFromProductTimeTypes() {
        assertEquals(List.of(ChargeCalculationType.FLAT, ChargeCalculationType.PERCENT_OF_AMOUNT),
                ChargeCalculationType.validEnumsForWorkingCapitalLoan());
    }
}
