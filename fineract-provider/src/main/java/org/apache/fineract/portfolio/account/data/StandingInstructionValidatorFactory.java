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

import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.portfolio.account.validator.StandingInstructionHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class StandingInstructionValidatorFactory {

    private final StandingInstructionHelper standingInstructionHelper;

    @Autowired
    public StandingInstructionValidatorFactory(final StandingInstructionHelper standingInstructionHelper) {
        this.standingInstructionHelper = standingInstructionHelper;
    }

    public StandingInstructionValidator getValidator(final StandingInstruction standingInstruction,
            final DataValidatorBuilder baseDataValidator) {

        if (standingInstruction == null) {
            return new InexistingStandingInstruction(this.standingInstructionHelper, new StandingInstruction(), baseDataValidator);
        }

        final Integer transferType = standingInstruction.getTransferType();

        if (transferType == null) {
            return new InexistingStandingInstruction(this.standingInstructionHelper, standingInstruction, baseDataValidator);
        }

        if (this.standingInstructionHelper.isAccountTransfer(transferType)) {
            return new AccountTransferStandingInstructionValidator(this.standingInstructionHelper, standingInstruction, baseDataValidator);
        }

        final Integer instructionType = standingInstruction.getInstructionType();
        final Integer recurrenceType = standingInstruction.getRecurrenceType();

        if (instructionType == null || recurrenceType == null) {
            return new InexistingStandingInstruction(this.standingInstructionHelper, standingInstruction, baseDataValidator);
        }

        if (this.standingInstructionHelper.isLoanRepayment(transferType)
                && this.standingInstructionHelper.isFixedInstruction(instructionType)
                && this.standingInstructionHelper.isPeriodicRecurrence(recurrenceType)) {
            return new PeriodicFixedAmountLoanRepaymentStandingInstruction(this.standingInstructionHelper, standingInstruction, baseDataValidator);
        }

        if (this.standingInstructionHelper.isLoanRepayment(transferType)
                && this.standingInstructionHelper.isDuesInstruction(instructionType)) {
            if (this.standingInstructionHelper.isPeriodicRecurrence(recurrenceType)) {
                return new PeriodicDuesLoanRepaymentStandingInstruction(this.standingInstructionHelper, standingInstruction, baseDataValidator);
            } else if (this.standingInstructionHelper.isAsPerDuesRecurrence(recurrenceType)) {
                return new LoanRepaymentStandingInstruction(this.standingInstructionHelper, standingInstruction, baseDataValidator);
            }
        }

        return new InexistingStandingInstruction(this.standingInstructionHelper, standingInstruction, baseDataValidator);
    }
}