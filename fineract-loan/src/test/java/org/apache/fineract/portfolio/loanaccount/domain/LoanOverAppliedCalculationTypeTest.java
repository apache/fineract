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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.junit.jupiter.api.Test;

class LoanOverAppliedCalculationTypeTest {

    @Test
    void asEnumOptionDataMatchesEntryInValuesList() {
        List<EnumOptionData> options = LoanOverAppliedCalculationType.getValuesAsEnumOptionDataList();
        for (LoanOverAppliedCalculationType value : LoanOverAppliedCalculationType.values()) {
            assertTrue(options.contains(value.asEnumOptionData()),
                    () -> "asEnumOptionData() for " + value + " must match an entry in getValuesAsEnumOptionDataList()");
        }
    }

    @Test
    void idsStartAtOne() {
        assertEquals(1L, LoanOverAppliedCalculationType.FLAT.asEnumOptionData().getId());
        assertEquals(2L, LoanOverAppliedCalculationType.PERCENTAGE.asEnumOptionData().getId());
    }

    @Test
    void noOptionHasZeroId() {
        for (EnumOptionData option : LoanOverAppliedCalculationType.getValuesAsEnumOptionDataList()) {
            assertTrue(option.getId() != 0L, () -> "Option " + option.getCode() + " must not have id 0");
        }
    }
}
