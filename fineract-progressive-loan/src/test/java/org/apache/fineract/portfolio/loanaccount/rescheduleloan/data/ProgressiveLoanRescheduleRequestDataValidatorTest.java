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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepaymentScheduleInstallment;
import org.apache.fineract.portfolio.loanaccount.rescheduleloan.RescheduleLoansApiConstants;
import org.apache.fineract.portfolio.loanaccount.rescheduleloan.domain.LoanRescheduleRequestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ProgressiveLoanRescheduleRequestDataValidatorTest {

    @Mock
    private DataValidatorBuilder dataValidatorBuilder;

    @Mock
    private LoanRepaymentScheduleInstallment installment;

    @Mock
    private FromJsonHelper fromJsonHelper;

    @Mock
    private LoanRescheduleRequestRepository loanRescheduleRequestRepository;

    private ProgressiveLoanRescheduleRequestDataValidator progressiveLoanRescheduleRequestDataValidator;

    @BeforeEach
    void setUp() {
        progressiveLoanRescheduleRequestDataValidator = new ProgressiveLoanRescheduleRequestDataValidator(fromJsonHelper,
                loanRescheduleRequestRepository);
        when(dataValidatorBuilder.reset()).thenReturn(dataValidatorBuilder);
        when(dataValidatorBuilder.parameter(anyString())).thenReturn(dataValidatorBuilder);
    }

    @Test
    void shouldFailWhenInstallmentIsNull() {
        progressiveLoanRescheduleRequestDataValidator.validateReschedulingInstallment(dataValidatorBuilder, null);

        verify(dataValidatorBuilder).reset();
        verify(dataValidatorBuilder).parameter(RescheduleLoansApiConstants.rescheduleFromDateParamName);
        verify(dataValidatorBuilder).failWithCode("repayment.schedule.installment.does.not.exist",
                "Repayment schedule installment does not exist");
    }

    @Test
    void shouldNotFailWhenInstallmentIsNotNull() {
        assertDoesNotThrow(
                () -> progressiveLoanRescheduleRequestDataValidator.validateReschedulingInstallment(dataValidatorBuilder, installment));

        verify(dataValidatorBuilder, never()).failWithCode(anyString(), anyString());
    }
}
