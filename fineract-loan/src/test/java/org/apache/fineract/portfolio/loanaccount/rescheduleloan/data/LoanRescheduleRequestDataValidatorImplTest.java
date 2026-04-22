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
package org.apache.fineract.portfolio.loanaccount.rescheduleloan.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.gson.JsonElement;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.portfolio.loanaccount.rescheduleloan.RescheduleLoansApiConstants;
import org.junit.jupiter.api.Test;

class LoanRescheduleRequestDataValidatorImplTest {

    @Test
    void validateEMIAndEndDateShouldFailWhenEndDateBeforeRescheduleFromDate() {
        FromJsonHelper fromJsonHelper = mock(FromJsonHelper.class);
        JsonElement jsonElement = mock(JsonElement.class);
        LocalDate rescheduleFromDate = LocalDate.of(2026, 3, 10);
        LocalDate endDate = LocalDate.of(2026, 3, 9);

        when(fromJsonHelper.extractLocalDateNamed(RescheduleLoansApiConstants.endDateParamName, jsonElement)).thenReturn(endDate);
        when(fromJsonHelper.extractBigDecimalWithLocaleNamed(RescheduleLoansApiConstants.emiParamName, jsonElement))
                .thenReturn(BigDecimal.valueOf(100));

        List<ApiParameterError> errors = new ArrayList<>();
        DataValidatorBuilder dataValidatorBuilder = new DataValidatorBuilder(errors).resource("loanreschedule");

        LoanRescheduleRequestDataValidatorImpl.validateEMIAndEndDate(fromJsonHelper, jsonElement, rescheduleFromDate, dataValidatorBuilder);

        assertEquals(1, errors.size());
        assertEquals(RescheduleLoansApiConstants.endDateParamName, errors.getFirst().getParameterName());
        assertTrue(errors.getFirst().getUserMessageGlobalisationCode().contains("end.date.before.reschedule.from.date"));
    }

    @Test
    void validateEMIAndEndDateShouldAllowNonInstallmentEndDate() {
        FromJsonHelper fromJsonHelper = mock(FromJsonHelper.class);
        JsonElement jsonElement = mock(JsonElement.class);
        LocalDate rescheduleFromDate = LocalDate.of(2026, 3, 10);
        LocalDate endDate = LocalDate.of(2026, 4, 13);

        when(fromJsonHelper.extractLocalDateNamed(RescheduleLoansApiConstants.endDateParamName, jsonElement)).thenReturn(endDate);
        when(fromJsonHelper.extractBigDecimalWithLocaleNamed(RescheduleLoansApiConstants.emiParamName, jsonElement))
                .thenReturn(BigDecimal.valueOf(100));

        List<ApiParameterError> errors = new ArrayList<>();
        DataValidatorBuilder dataValidatorBuilder = new DataValidatorBuilder(errors).resource("loanreschedule");

        LoanRescheduleRequestDataValidatorImpl.validateEMIAndEndDate(fromJsonHelper, jsonElement, rescheduleFromDate, dataValidatorBuilder);

        assertTrue(errors.isEmpty());
    }
}
