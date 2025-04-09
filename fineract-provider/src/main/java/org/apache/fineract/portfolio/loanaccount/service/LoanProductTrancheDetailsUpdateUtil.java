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
package org.apache.fineract.portfolio.loanaccount.service;

import java.math.BigDecimal;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.portfolio.loanproduct.LoanProductConstants;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProductTrancheDetails;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LoanProductTrancheDetailsUpdateUtil {

    public void update(final LoanProductTrancheDetails loanProductTrancheDetails, final JsonCommand command,
            final Map<String, Object> actualChanges) {
        if (command.isChangeInBooleanParameterNamed(LoanProductConstants.MULTI_DISBURSE_LOAN_PARAMETER_NAME,
                loanProductTrancheDetails.isMultiDisburseLoan())) {
            final boolean newValue = command.booleanPrimitiveValueOfParameterNamed(LoanProductConstants.MULTI_DISBURSE_LOAN_PARAMETER_NAME);
            actualChanges.put(LoanProductConstants.MULTI_DISBURSE_LOAN_PARAMETER_NAME, newValue);
            loanProductTrancheDetails.setMultiDisburseLoan(newValue);
        }

        if (loanProductTrancheDetails.isMultiDisburseLoan()) {
            if (command.isChangeInIntegerParameterNamed(LoanProductConstants.MAX_TRANCHE_COUNT_PARAMETER_NAME,
                    loanProductTrancheDetails.getMaxTrancheCount())) {
                final Integer newValue = command.integerValueOfParameterNamed(LoanProductConstants.MAX_TRANCHE_COUNT_PARAMETER_NAME);
                actualChanges.put(LoanProductConstants.MAX_TRANCHE_COUNT_PARAMETER_NAME, newValue);
                loanProductTrancheDetails.setMaxTrancheCount(newValue);
            }

            if (command.isChangeInBigDecimalParameterNamed(LoanProductConstants.OUTSTANDING_LOAN_BALANCE_PARAMETER_NAME,
                    loanProductTrancheDetails.getOutstandingLoanBalance())) {
                final BigDecimal newValue = command
                        .bigDecimalValueOfParameterNamed(LoanProductConstants.OUTSTANDING_LOAN_BALANCE_PARAMETER_NAME);
                actualChanges.put(LoanProductConstants.OUTSTANDING_LOAN_BALANCE_PARAMETER_NAME, newValue);
                loanProductTrancheDetails.setOutstandingLoanBalance(newValue);
            }
        } else {
            loanProductTrancheDetails.setMaxTrancheCount(null);
            loanProductTrancheDetails.setOutstandingLoanBalance(null);
        }
    }
}
