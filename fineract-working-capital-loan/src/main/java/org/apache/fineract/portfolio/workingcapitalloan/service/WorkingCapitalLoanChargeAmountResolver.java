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
package org.apache.fineract.portfolio.workingcapitalloan.service;

import java.math.BigDecimal;
import org.apache.fineract.infrastructure.core.service.MathUtil;
import org.apache.fineract.organisation.monetary.domain.MoneyHelper;
import org.apache.fineract.portfolio.charge.domain.ChargeCalculationType;
import org.springframework.stereotype.Component;

/**
 * Resolves the money amount of a Working Capital loan charge from its calculation type.
 *
 * <p>
 * Flat charges carry their amount as-is. Percentage charges carry a rate, applied to the principal the charge is
 * computed against - for a disbursement charge, the amount actually disbursed. Mirrors Term Loan's
 * {@code LoanChargeService.getDerivedAmountForCharge} for the single-disbursement case, which is the only one WC has.
 */
@Component
public class WorkingCapitalLoanChargeAmountResolver {

    private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);

    /**
     * @param calculationType
     *            the charge's calculation type
     * @param amountOrRate
     *            the flat amount, or the percentage rate for percentage-based types
     * @param principal
     *            the base a percentage applies to; ignored for flat charges
     * @return the resolved amount, rounded with the platform's money rounding
     */
    public BigDecimal resolve(final ChargeCalculationType calculationType, final BigDecimal amountOrRate, final BigDecimal principal) {
        final BigDecimal value = MathUtil.nullToZero(amountOrRate);
        if (calculationType == null || !calculationType.isPercentageBased()) {
            return value;
        }
        final BigDecimal base = MathUtil.nullToZero(principal);
        return base.multiply(value, MoneyHelper.getMathContext()).divide(ONE_HUNDRED, MoneyHelper.getMathContext()).setScale(6,
                MoneyHelper.getRoundingMode());
    }
}
