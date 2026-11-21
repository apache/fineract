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
import org.apache.fineract.portfolio.loanproduct.domain.LoanProductGuaranteeDetails;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LoanProductGuaranteeDetailsUpdateUtil {

    public void update(final LoanProductGuaranteeDetails loanProductGuaranteeDetails, final JsonCommand command,
            final Map<String, Object> actualChanges) {

        if (command.isChangeInBigDecimalParameterNamed(LoanProductConstants.mandatoryGuaranteeParamName,
                loanProductGuaranteeDetails.getMandatoryGuarantee())) {
            final BigDecimal newValue = command.bigDecimalValueOfParameterNamed(LoanProductConstants.mandatoryGuaranteeParamName);
            actualChanges.put(LoanProductConstants.mandatoryGuaranteeParamName, newValue);
            loanProductGuaranteeDetails.setMandatoryGuarantee(newValue);
        }

        if (command.isChangeInBigDecimalParameterNamed(LoanProductConstants.minimumGuaranteeFromGuarantorParamName,
                loanProductGuaranteeDetails.getMinimumGuaranteeFromGuarantor())) {
            final BigDecimal newValue = command
                    .bigDecimalValueOfParameterNamed(LoanProductConstants.minimumGuaranteeFromGuarantorParamName);
            actualChanges.put(LoanProductConstants.minimumGuaranteeFromGuarantorParamName, newValue);
            loanProductGuaranteeDetails.setMinimumGuaranteeFromGuarantor(newValue);
        }

        if (command.isChangeInBigDecimalParameterNamed(LoanProductConstants.minimumGuaranteeFromOwnFundsParamName,
                loanProductGuaranteeDetails.getMinimumGuaranteeFromOwnFunds())) {
            final BigDecimal newValue = command.bigDecimalValueOfParameterNamed(LoanProductConstants.minimumGuaranteeFromOwnFundsParamName);
            actualChanges.put(LoanProductConstants.minimumGuaranteeFromOwnFundsParamName, newValue);
            loanProductGuaranteeDetails.setMinimumGuaranteeFromOwnFunds(newValue);
        }

    }
}
