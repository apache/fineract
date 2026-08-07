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

import static org.apache.fineract.portfolio.account.api.StandingInstructionApiConstants.instructionTypeParamName;
import static org.apache.fineract.portfolio.account.api.StandingInstructionApiConstants.recurrenceTypeParamName;

import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.portfolio.account.api.StandingInstructionApiConstants;
import org.apache.fineract.portfolio.account.validator.StandingInstructionHelper;

public class AccountTransferStandingInstruction extends CommonStandingInstructionValidations {
    
    public AccountTransferStandingInstruction(final StandingInstructionHelper standingInstructionHelper,
            final StandingInstruction standingInstruction,
            final DataValidatorBuilder baseDataValidator) {
        super(standingInstructionHelper,  standingInstruction, baseDataValidator);
    }

    @Override
    protected void validateSpecificFields() {
        validatePeriodicFields();
        validateAmountForFixedInstructionType();
        
        AccountTransferDetails details = this.standingInstruction.getAccountTransferDetails();
        
        final Integer fromAccountType = details.getFromAccountType();
        final Integer toAccountType = details.getToAccountType();
        
        if (isValidAccountTransfer(fromAccountType, toAccountType)) {
            final Long fromOfficeId = details.getFromOfficeId();
            final Long toOfficeId = details.getToOfficeId();
            final Long fromAccountId = details.getFromAccountId();
            final Long toAccountId = details.getToAccountId();

            if (isSelfAccountTransfer(fromOfficeId, toOfficeId, fromAccountId, toAccountId)) {
                this.baseDataValidator.reset().parameter(toAccountIdParamName)
                    .failWithCode(StandingInstructionApiConstants.CANNOT_TRANSFER_TO_SAME_ACCOUNT_ERROR_CODE);
            }
        } else {
            this.baseDataValidator.reset().parameter(transferTypeParamName)
                .failWithCode(StandingInstructionApiConstants.NOT_A_VALID_ACCOUNT_TRANSFER_ERROR_CODE);
        }

        final Integer instructionType = this.standingInstruction.getInstructionType();
        if (this.standingInstructionHelper.isDuesInstruction(instructionType)) {
            this.baseDataValidator.reset().parameter(instructionTypeParamName)
                    .failWithCode(StandingInstructionApiConstants.INSTRUCTION_TYPE_DUES_NOT_ALLOWED_FOR_ACCOUNT_TRANSFER_ERROR_CODE);
        }

        final Integer recurrenceType = this.standingInstruction.getRecurrenceType();
        if (isAsPerDuesRecurrence(recurrenceType)) {
            this.baseDataValidator.reset().parameter(recurrenceTypeParamName)
                    .failWithCode(StandingInstructionApiConstants.RECURRENCE_AS_PER_DUES_NOT_ALLOWED_FOR_SAVINGS_ERROR_CODE);
        }
    }
}