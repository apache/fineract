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
package org.apache.fineract.portfolio.workingcapitalloan.serialization;

import java.math.BigDecimal;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;

/** Rules on the discount fee shared by every validator that sets it or the principal it is charged on. */
final class WorkingCapitalLoanDiscountValidation {

    private WorkingCapitalLoanDiscountValidation() {}

    /**
     * The discount fee is charged on top of the principal rather than deducted from it, so a fee worth more than the
     * principal means the borrower owes more in fee than in money received. The schedule that follows earns the whole
     * fee over the handful of days the payments take to repay the balance, which solves to an annual EIR of
     * astronomical magnitude - see
     * {@link org.apache.fineract.portfolio.workingcapitalloan.calc.ProjectedAmortizationScheduleModel#MAX_CALCULABLE_ANNUAL_EIR}.
     * Applied at every point the pair can be set, since either side of it moves independently: submission,
     * modification, approval, disbursement and the discount fee transaction itself.
     *
     * @param parameterName
     *            the request parameter the discount arrived in, which differs between submission and the later steps
     */
    static void validateDiscountDoesNotExceedPrincipal(final BigDecimal discount, final BigDecimal principal, final String parameterName,
            final DataValidatorBuilder baseDataValidator) {
        if (discount == null || principal == null || principal.signum() <= 0) {
            return;
        }
        if (discount.compareTo(principal) > 0) {
            baseDataValidator.reset().parameter(parameterName).value(discount).failWithCode("amount.cannot.exceed.principal");
        }
    }
}
