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
package org.apache.fineract.portfolio.charge.validation;

import java.util.ArrayList;
import java.util.List;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.portfolio.charge.domain.Charge;
import org.apache.fineract.portfolio.charge.domain.ChargeAppliesTo;
import org.apache.fineract.portfolio.charge.domain.ChargeCalculationType;
import org.apache.fineract.portfolio.charge.domain.ChargeTimeType;
import org.apache.fineract.portfolio.charge.exception.ChargeDueAtDisbursementCannotBePenaltyException;
import org.apache.fineract.portfolio.charge.exception.ChargeMustBePenaltyException;

/**
 * Checks the charge definition rules of a create request that the bean validation of the request does not cover.
 * <p>
 * The rules and their error codes are the ones the {@link Charge} constructor used to check:
 * <ul>
 * <li>a savings charge can only be a percentage of the amount when it is a withdrawal fee or a no activity fee;</li>
 * <li>a loan charge due at disbursement or tranche disbursement cannot be a penalty;</li>
 * <li>a loan charge due for an overdue installment must be a penalty.</li>
 * </ul>
 */
public final class ChargeDefinitionValidator {

    private static final String RESOURCE = "charges";

    private ChargeDefinitionValidator() {}

    public static void validateForCreate(final String name, final ChargeAppliesTo chargeAppliesTo, final ChargeTimeType chargeTime,
            final ChargeCalculationType chargeCalculationType, final boolean penalty) {
        if (chargeAppliesTo.isSavingsCharge()) {
            if (!(chargeTime.isWithdrawalFee() || chargeTime.isSavingsNoActivityFee()) && chargeCalculationType.isPercentageOfAmount()) {
                final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
                new DataValidatorBuilder(dataValidationErrors).resource(RESOURCE).parameter(Charge.CHARGE_CALCULATION_TYPE_PARAM_NAME)
                        .value(chargeCalculationType.getValue()).failWithCodeNoParameterAddedToErrorCode(
                                "savings.charge.calculation.type.percentage.allowed.only.for.withdrawal.or.NoActivity");
                throw new PlatformApiDataValidationException(dataValidationErrors);
            }
        } else if (chargeAppliesTo.isLoanCharge()) {
            if (penalty && (chargeTime.isTimeOfDisbursement() || chargeTime.isTrancheDisbursement())) {
                throw new ChargeDueAtDisbursementCannotBePenaltyException(name);
            }
            if (!penalty && chargeTime.isOverdueInstallment()) {
                throw new ChargeMustBePenaltyException(name);
            }
        }
    }
}
