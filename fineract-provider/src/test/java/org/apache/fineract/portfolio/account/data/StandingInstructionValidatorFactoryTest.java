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

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.function.Function;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.portfolio.account.domain.AccountTransferRecurrenceType;
import org.apache.fineract.portfolio.account.domain.AccountTransferType;
import org.apache.fineract.portfolio.account.domain.StandingInstructionType;
import org.apache.fineract.portfolio.account.validator.StandingInstructionHelper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class StandingInstructionValidatorFactoryTest {
    @Mock
    private StandingInstruction standingInstruction;

    @Mock
    private DataValidatorBuilder baseDataValidator;

    @Test
    public void shouldReturnAnInexistingStandingInstructionInstanceWhenStandingInstructionIsNull() {
        StandingInstruction instruction = null;
        
        StandingInstructionValidator result = StandingInstructionValidatorFactory.getStrategy(instruction, this.baseDataValidator);
        assertTrue(expectedClass.isInstance(result));
    }

    @Test
    public void shouldReturnAnInexistingStandingInstructionInstanceWhenTransferTypeIsNull() {
        setUpInstructionField(StandingInstruction::getTransferType, null);

        assertValidatorInstance(InexistingStandingInstruction.class);
        
    }

    @Test
    public void shouldReturnAnAccountTransferStandingInstructionInstance() {
        setUpInstructionField(StandingInstruction::getTransferType, AccountTransferType.ACCOUNT_TRANSFER.getValue());

        assertValidatorInstance(AccountTransferStandingInstruction.class);
    }

    @Test
    public void shouldReturnAnInexistingStandingInstructionInstanceWhenInstructionTypeIsNull() {
        setUpInstructionField(StandingInstruction::getTransferType, AccountTransferType.LOAN_REPAYMENT.getValue());
        setUpInstructionField(StandingInstruction::getInstructionType, null);
        setUpInstructionField(StandingInstruction::getRecurrenceType, AccountTransferRecurrenceType.PERIODIC.getValue());

        assertValidatorInstance(InexistingStandingInstruction.class);
    }

    @Test
    public void shouldReturnAnInexistingStandingInstructionInstanceWhenRecurrenceTypeIsNull() {
        setUpInstructionField(StandingInstruction::getTransferType, AccountTransferType.LOAN_REPAYMENT.getValue());
        setUpInstructionField(StandingInstruction::getInstructionType, StandingInstructionType.FIXED.getValue());
        setUpInstructionField(StandingInstruction::getRecurrenceType, null);

        assertValidatorInstance(InexistingStandingInstruction.class);
    }

    @Test
    public void shouldReturnAPeriodicFixedAmountLoanRepaymentStandingInstructionInstance() {
        setUpInstructionField(StandingInstruction::getTransferType, AccountTransferType.LOAN_REPAYMENT.getValue());
        setUpInstructionField(StandingInstruction::getInstructionType, StandingInstructionType.FIXED.getValue());
        setUpInstructionField(StandingInstruction::getRecurrenceType, AccountTransferRecurrenceType.PERIODIC.getValue());

        assertValidatorInstance(PeriodicFixedAmountLoanRepaymentStandingInstruction.class);
    }

    @Test
    public void shouldReturnAPeriodicDuesLoanRepaymentStandingInstructionInstance() {
        setUpInstructionField(StandingInstruction::getTransferType, AccountTransferType.LOAN_REPAYMENT.getValue());
        setUpInstructionField(StandingInstruction::getInstructionType, StandingInstructionType.DUES.getValue());
        setUpInstructionField(StandingInstruction::getRecurrenceType, AccountTransferRecurrenceType.PERIODIC.getValue());

        assertValidatorInstance(PeriodicDuesLoanRepaymentStandingInstruction.class);
    }

    @Test
    public void shouldReturnALoanRepaymentStandingInstructionInstance() {
        setUpInstructionField(StandingInstruction::getTransferType, AccountTransferType.LOAN_REPAYMENT.getValue());
        setUpInstructionField(StandingInstruction::getInstructionType, StandingInstructionType.DUES.getValue());
        setUpInstructionField(StandingInstruction::getRecurrenceType, AccountTransferRecurrenceType.AS_PER_DUES.getValue());

        assertValidatorInstance(LoanRepaymentStandingInstruction.class);
    }

    @Test
    public void shouldReturnAnInexistingStandingInstructionInstanceWithFixedInstructionAndAsPerDuesRecurrence() {
        setUpInstructionField(StandingInstruction::getTransferType, AccountTransferType.LOAN_REPAYMENT.getValue());
        setUpInstructionField(StandingInstruction::getInstructionType, StandingInstructionType.FIXED.getValue());
        setUpInstructionField(StandingInstruction::getRecurrenceType, AccountTransferRecurrenceType.AS_PER_DUES.getValue());

        assertValidatorInstance(InexistingStandingInstruction.class);
    }

    private <T> void setUpInstructionField(Function<StandingInstruction, T> getter, T value) {
        when(getter.apply(this.standingInstruction)).thenReturn(value);
    }

    private <T extends StandingInstructionValidator> void assertValidatorInstance(Class<T> expectedClass) {
        StandingInstructionValidator result = StandingInstructionValidatorFactory.getValidator(this.standingInstruction, this.baseDataValidator);
        assertTrue(expectedClass.isInstance(result));
    }
}