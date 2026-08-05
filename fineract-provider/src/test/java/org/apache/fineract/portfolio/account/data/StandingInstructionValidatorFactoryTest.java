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
package org.apache.fineract.portfolio.account.data;

import static org.apache.fineract.portfolio.account.AccountDetailConstants.transferTypeParamName;
import static org.apache.fineract.portfolio.account.api.StandingInstructionApiConstants.instructionTypeParamName;
import static org.apache.fineract.portfolio.account.api.StandingInstructionApiConstants.recurrenceTypeParamName;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.google.gson.JsonElement;
import java.util.Locale;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.portfolio.account.domain.AccountTransferRecurrenceType;
import org.apache.fineract.portfolio.account.domain.AccountTransferType;
import org.apache.fineract.portfolio.account.domain.StandingInstructionType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class StandingInstructionValidatorFactoryTest {

    @Mock
    private FromJsonHelper fromApiJsonHelper;

    @Mock
    private JsonElement element;

    @Mock
    private DataValidatorBuilder baseDataValidator;

    @Test
    public void shouldReturnAnInexistingStandingInstructionInstanceWhenTransferTypeIsNull() {
        actForParamWithValue(transferTypeParamName, null);

        assertInstanceStrategy(InexistingStandingInstruction.class);
        
    }

    @Test
    public void shouldReturnAnAccountTransferStandingInstructionInstance() {
        actForParamWithValue(transferTypeParamName, AccountTransferType.ACCOUNT_TRANSFER.getValue());

        assertInstanceStrategy(AccountTransferStandingInstruction.class);
    }

    @Test
    public void shouldReturnAnInexistingStandingInstructionInstanceWhenInstructionTypeIsNull() {
        actForParamWithValue(transferTypeParamName, AccountTransferType.LOAN_REPAYMENT.getValue());
        actForParamWithValue(instructionTypeParamName, null);
        actForParamWithValue(recurrenceTypeParamName, AccountTransferRecurrenceType.PERIODIC.getValue());

        assertInstanceStrategy(InexistingStandingInstruction.class);
    }

    @Test
    public void shouldReturnAnInexistingStandingInstructionInstanceWhenRecurrenceTypeIsNull() {
        actForParamWithValue(transferTypeParamName, AccountTransferType.LOAN_REPAYMENT.getValue());
        actForParamWithValue(instructionTypeParamName, StandingInstructionType.FIXED.getValue());
        actForParamWithValue(recurrenceTypeParamName, null);

        assertInstanceStrategy(InexistingStandingInstruction.class);
    }

    @Test
    public void shouldReturnAPeriodicFixedAmountLoanRepaymentStandingInstructionInstance() {
        actForParamWithValue(transferTypeParamName, AccountTransferType.LOAN_REPAYMENT.getValue());
        actForParamWithValue(instructionTypeParamName, StandingInstructionType.FIXED.getValue());
        actForParamWithValue(recurrenceTypeParamName, AccountTransferRecurrenceType.PERIODIC.getValue());

        assertInstanceStrategy(PeriodicFixedAmountLoanRepaymentStandingInstruction.class);
    }

    @Test
    public void shouldReturnAPeriodicDuesLoanRepaymentStandingInstructionInstance() {
        actForParamWithValue(transferTypeParamName, AccountTransferType.LOAN_REPAYMENT.getValue());
        actForParamWithValue(instructionTypeParamName, StandingInstructionType.DUES.getValue());
        actForParamWithValue(recurrenceTypeParamName, AccountTransferRecurrenceType.PERIODIC.getValue());

        assertInstanceStrategy(PeriodicDuesLoanRepaymentStandingInstruction.class);
    }

    @Test
    public void shouldReturnALoanRepaymentStandingInstructionInstance() {
        actForParamWithValue(transferTypeParamName, AccountTransferType.LOAN_REPAYMENT.getValue());
        actForParamWithValue(instructionTypeParamName, StandingInstructionType.DUES.getValue());
        actForParamWithValue(recurrenceTypeParamName, AccountTransferRecurrenceType.AS_PER_DUES.getValue());

        assertInstanceStrategy(LoanRepaymentStandingInstruction.class);
    }

    @Test
    public void shouldReturnAnInexistingStandingInstructionInstanceWithFixedInstructionAndAsPerDuesRecurrence() {
        actForParamWithValue(transferTypeParamName, AccountTransferType.LOAN_REPAYMENT.getValue());
        actForParamWithValue(instructionTypeParamName, StandingInstructionType.FIXED.getValue());
        actForParamWithValue(recurrenceTypeParamName, AccountTransferRecurrenceType.AS_PER_DUES.getValue());

        assertInstanceStrategy(InexistingStandingInstruction.class);
    }

    private void actForParamWithValue(final String paramName, final Integer value) {
        when(fromApiJsonHelper.extractIntegerNamed(eq(paramName), eq(element), any(Locale.class))).thenReturn(value);
    }

    private <T extends StandingInstructionValidator> void assertInstanceStrategy(Class<T> expectedClass) {
        StandingInstructionValidator result = StandingInstructionValidatorFactory.getStrategy(fromApiJsonHelper, element, baseDataValidator);
        assertTrue(expectedClass.isInstance(result));
    }
}