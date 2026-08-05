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

import com.google.gson.JsonElement;
import java.util.Locale;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.portfolio.account.api.StandingInstructionApiConstants;

public class InexistingStandingInstruction extends CommonStandingInstructionValidations {
    
    public InexistingStandingInstruction(final FromJsonHelper fromApiJsonHelper, final JsonElement element, final DataValidatorBuilder baseDataValidator) {
        super(fromApiJsonHelper, element, baseDataValidator);
    }

    @Override
    protected void validateSpecificFields() {
        final Integer transferType = fromApiJsonHelper.extractIntegerNamed(transferTypeParamName, this.element, this.locale);
        final Integer instructionType = fromApiJsonHelper.extractIntegerNamed(instructionTypeParamName, this.element, this.locale);
        final Integer recurrenceType = fromApiJsonHelper.extractIntegerNamed(recurrenceTypeParamName, this.element, this.locale);

        if (isLoanRepayment(transferType) && isFixedInstruction(instructionType) && isAsPerDuesRecurrence(recurrenceType)) {
            this.baseDataValidator.reset().parameter(recurrenceTypeParamName)
                    .failWithCode(StandingInstructionApiConstants.RECURRENCE_AS_PER_DUES_NOT_ALLOWED_WITH_FIXED_INSTRUCTION_ERROR_CODE);
        }
    }
}