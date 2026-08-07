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

import com.google.gson.JsonElement;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;

public class LoanRepaymentStandingInstruction extends CommonStandingInstructionValidations {
    
    public LoanRepaymentStandingInstruction(final StandingInstructionHelper standingInstructionHelper,
            final StandingInstruction standingInstruction,
            final DataValidatorBuilder baseDataValidator) {
        super(standingInstructionHelper,  standingInstruction, baseDataValidator);
    }

    @Override
    protected void validateSpecificFields() {
        validateAmountForFixedInstructionType();
        
        AccountTransferDetails details = this.standingInstruction.getAccountTransferDetails();
        
        final Integer fromAccountType = details.getFromAccountType();
        final Integer toAccountType = details.getToAccountType();

        if (!isValidLoanRepayment(fromAccountType, toAccountType)) {
            this.baseDataValidator.reset().parameter(transferTypeParamName)
                    .failWithCode(StandingInstructionApiConstants.NOT_A_VALID_LOAN_REPAYMENT_ERROR_CODE);
        }
    }
}